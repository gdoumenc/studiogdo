/**
 * Copyright GDO - 2004
 */
package com.gdo.project.slot;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import com.gdo.stencils.Result;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.log.StencilLog;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;

/**
 * Abstract class implementing a stencil cursor navigator. Some stencils are
 * stored to be reused without recreation if available memory.
 * 
 * Some attributes my be also stored for each stencil to allow getString returns
 * without creating the stencil.
 */
public abstract class _SlotCursor {

    public static int STRATEGY = 0;
    private int _transaction_id;

    // semaphore to block stencil in buffer
    private Semaphore _available = new Semaphore(1, false);

    // container slot name (used for debug only)
    private String _name;

    // cursor size (0 for unlimited ursor)
    private int _size;

    // stencils stored in cursor (key -> stencil)
    protected Map<IKey, PStcl> _stencils;

    // properties stored in cursor (key => (prop => value))
    protected Map<IKey, Map<String, String>> _properties;

    // set to true if properties list of a stencil was modified since last set
    // of those properties (key -> value)
    public Map<IKey, Boolean> _modified;
    public Map<IKey, Boolean> _in_completion;

    // locked stencils (key -> stencil plugged in $locked slot)
    protected Map<IKey, PStcl> _locked;

    /**
     * Slot cursor constructor.
     * 
     * @param size
     *            the cursor size.
     */
    public _SlotCursor(String name, int size) {
        _name = name;
        _size = size;
        _stencils = new ConcurrentHashMap<>(_size);
        _properties = new ConcurrentHashMap<>();
        _modified = new ConcurrentHashMap<>();
        _locked = new ConcurrentHashMap<>(_size);
    }

    /**
     * Removes all of the mappings from this map (optional operation). The
     * cursor will be empty after this call returns.
     */
    public void clear() {
        if (_stencils != null)
            _stencils.clear();
        if (_locked != null)
            _locked.clear();
        if (_properties != null)
            _properties.clear();
        if (_modified != null)
            _modified.clear();
        if (_in_completion != null)
            _in_completion.clear();
    }

    public void size(int size) {
        _size = size + 1;
    }

    /**
     * Abstract method to create stencil if not in cursor.
     * 
     * @param stclContext
     *            the stencil context.
     * @param container
     *            initial slot container.
     * @param slot
     *            this slot as a plugged slot.
     * @param key
     *            the plugging key.
     * @return
     */
    protected abstract CreatedStcl createStencil(StclContext stclContext, PSlot<StclContext, PStcl> container, PSlot<StclContext, PStcl> slot, IKey key);

    /**
     * Abstract method to complete stencil after created.
     * 
     * @param stclContext
     *            the stencil context.
     * @param container
     *            initial slot container.
     * @param slot
     *            this slot as a plugged slot.
     * @param key
     *            the plugging key.
     * @param stcl
     *            the stencil created.
     * @return
     */
    protected abstract PStcl completeCreatedStencil(StclContext stclContext, PSlot<StclContext, PStcl> container, PSlot<StclContext, PStcl> slot, IKey key, CreatedStcl created);

    /**
     * Returns the stencil at a specific key.
     * 
     * Creates it if needed. This stencil must be released after use.
     * 
     * @param stclContext
     *            the stencil context.
     * @param container
     *            the cursor slot container.
     * @param slot
     *            the containing slot.
     * @param key
     *            the stencil plug key.
     * @return the stencil.
     */
    public PStcl getStencil(StclContext stclContext, PSlot<StclContext, PStcl> container, PSlot<StclContext, PStcl> slot, IKey key) {

        // if already in cursor returns it
        PStcl contained = inCursor(stclContext, key);
        if (StencilUtils.isNotNull(contained))
            return contained;

        // creates the stencil in memory
        synchronized (this) {

            // blocks if no more place
            if (_size > 0 && _stencils.size() >= _size) {
                try {
                    logWarn(stclContext, "block for %s in %s", key, _name);
                    _available.acquire();
                } catch (InterruptedException e) {
                    logError(stclContext, e.toString());
                    return null;
                }
            }

            // creates the stencil
            CreatedStcl stcl = createStencil(stclContext, container, slot, key);
            if (StencilUtils.isNull(stcl._created))
                return Stcl.nullPStencil(stclContext, Result.error("cannot create cursor stencil from empty stencil"));

            // stores the stencil in cursor
            addInCursor(stclContext, stcl._created);
            stcl._created.addThisReferenceToStencil(stclContext);

            // adds locked stencil if locked
            PStcl locked = _locked.get(key);
            if (StencilUtils.isNotNull(locked))
                stcl._created.plug(stclContext, locked, Stcl.Slot.$LOCKED_BY);

            if (_in_completion == null) {
                _in_completion = new HashMap<IKey, Boolean>();
            }
            _in_completion.put(key, Boolean.TRUE);
            PStcl completed =  completeCreatedStencil(stclContext, container, slot, key, stcl);
            _in_completion.remove(key);
            return completed;
        }

    }

    /**
     * Releases the stencil at a specific key, so the cursor can release the
     * memory if needed.
     * 
     * @param stclContext
     *            the stencil context.
     * @param slot
     *            the cursor slot.
     * @param key
     *            the stencil key.
     */
    public void release(StclContext stclContext, PSlot<StclContext, PStcl> container, PSlot<StclContext, PStcl> slot, String key) {
        if (_size > 0 && _stencils.size() >= _size) {

            // needs to release more space in memory
            // (release memory only when full)
            synchronized (this) {

                // keys to release
                Stack<IKey> keys = new Stack<>();

                // releases all except the one required
                Iterator<IKey> iter = _stencils.keySet().iterator();
                while (iter.hasNext()) {
                    IKey k = iter.next();

                    // not same as key as was the last used and may be reused
                    // and stencil must not be locked
                    if (!k.equals(key) && !_locked.containsKey(k))
                        keys.push(k);
                }

                // removes them from local structure
                for (IKey k : keys) {
                    remove(stclContext, container, slot, k);
                }

                // releases now as more place
                _available.release();
                logWarn(stclContext, "release for %s in slot %s in %s", key, slot, container);
            }
        }
    }

    /**
     * Removes the stencil at a specified key.
     * 
     * @param stclContext
     *            the stencil context.
     * @param key
     *            the plugged key
     */
    public void remove(StclContext stclContext, PSlot<StclContext, PStcl> container, PSlot<StclContext, PStcl> slot, IKey key) {

        // gets the stencil to be removed
        PStcl stcl = inCursor(stclContext, key);
        if (StencilUtils.isNotNull(stcl)) {
            
            // lock the stencil to force it to stay in memory
            lock(stclContext, stcl, key);

            // checks was not modified before removing
            // (negative id may be released without being saved)
            Boolean modified = _modified.get(key);
            if (modified != null) {
                logWarn(stclContext, "Stencil removed from cursor without being updated : %s", stcl);
                stcl.afterRPCSet(stclContext);
            }

            // replaces the stencil by cursor
            for (PStcl s : stcl.getStencilOtherPluggedReferences(stclContext))
                s.release(stclContext, container, this, key);
            
            // removes it from cursor
            removeFromCursor(stclContext, key);
            
            // unlock the stencil as it is removed
            unlock(stclContext, key);
        }
    }

    public void lock(StclContext stclContext, PStcl stencil, IKey key) {
        _locked.put(key, stencil);
    }

    public void unlock(StclContext stclContext, IKey key) {
        _locked.remove(key);
    }

    // --------------------------------------------------------------------------
    //
    // Cursor stencils properties management.
    //
    // --------------------------------------------------------------------------

    /**
     * Sets the properties values in cursor to avoid creating the stencil.
     * 
     * @param stclContext
     *            the stencil context.
     * @param key
     *            the stencil key.
     * @param values
     *            the properties values.
     */
    public void setPropertiesValues(StclContext stclContext, PSlot<StclContext, PStcl> slot, IKey key, Map<String, String> values) {

        // checks the properties were not modified before reset them
        Boolean modified = _modified.get(key);
        if (modified != null) {
            logError(stclContext, "Property values set in cursor without being updated (in %s at key %s)", slot, key);
        }

        // changes properties values and sets properties not modified
        _properties.put(key, values);
        _modified.remove(key);
    }

    /**
     * Returns the property values associated to a stencil (null if not set).
     * 
     * @param stclContext
     *            the stencil context.
     * @param slot
     *            the slot.
     * @param key
     *            the stencil key.
     * @return the properties values.
     */
    public Map<String, String> getPropertiesValues(StclContext stclContext, PSlot<StclContext, PStcl> slot, IKey key) {
        return _properties.get(key);
    }

    /**
     * Sets the properties list is not modified from last setting.
     * 
     * @param stclContext
     *            the stencil context.
     * @param key
     *            the stencil key.
     */
    public void setPropertiesValuesNotModified(StclContext stclContext, IKey key) {
        _modified.remove(key);
    }

    /**
     * Returns the property value associated to a stencil (null if not set).
     * 
     * @param stclContext
     *            the stencil context.
     * @param key
     *            the stencil key.
     * @param path
     *            the string property path.
     * @return the property value (<tt>null</tt> if not set).
     */
    public String getPropertyValue(StclContext stclContext, PSlot<StclContext, PStcl> slot, IKey key, String path) {
        if (PathUtils.isComposed(path)) {
            logWarn(stclContext, "Cannot use a composed path %s for getPropertyValue", path);
            return null;
        }
        Map<String, String> attributes = getPropertiesValues(stclContext, slot, key);
        if (attributes == null || !attributes.containsKey(path)) {
            return null;
        }
        String value = attributes.get(path);
        return (value == null) ? "" : value;
    }

    /**
     * Adds a new value in a property.
     * 
     * @param stclContext
     *            the stencil context.
     * @param key
     *            the stencil key.
     * @param path
     *            the string property path.
     * @param value
     *            the value set.
     * @return the previous value associated with key, or <tt>null</tt> if there
     *         was no mapping for key.
     */
    public String addPropertyValue(StclContext stclContext, PSlot<StclContext, PStcl> container, PSlot<StclContext, PStcl> slot, IKey key, String path, String value) {
        if (PathUtils.isComposed(path)) {
            logWarn(stclContext, "Cannot use a composed path %s for addPropertyValue", path);
            return null;
        }

        // get the stencil to force stencil to be in memory
        PStcl stcl = getStencil(stclContext, container, slot, key);

        // puts attribute
        Map<String, String> attributes = getPropertiesValues(stclContext, slot, key);
        if (attributes == null) {
            logError(stclContext, "Properties values should never be null (in %s)", stcl);
            return null;
        }

        // sets cursor modified for this stencil
        // if in completion then this is not a modification
        if (!_in_completion.containsKey(key))
            _modified.put(key, Boolean.TRUE);
        
        return attributes.put(path, value);
    }

    // --------------------------------------------------------------------------
    //
    // Cursor stencils list management.
    //
    // --------------------------------------------------------------------------

    /**
     * Returns the stencil if it is still inside the cursor.
     * 
     * @param stclContext
     *            the stencil context.
     * @param key
     *            the stencil key.
     * @return the stencil contained in the cursor (and not recreated);
     */
    public synchronized PStcl inCursor(StclContext stclContext, IKey key) {

        // in cursor only on same session
        if (STRATEGY == 1 && _transaction_id != stclContext.getTransactionId()) {
            _stencils.clear();
            _transaction_id = stclContext.getTransactionId();
            return null;
        }

        // else checks in map
        return _stencils.get(key);
    }

    /**
     * Adds the stencil in the cursor at a specific key.
     * 
     * @param stclContext
     *            the stencil context.
     * @param stcl
     *            the stencil to be added.
     */
    public synchronized void addInCursor(StclContext stclContext, PStcl stcl) {
        _stencils.put(stcl.getKey(), stcl);
    }

    /**
     * Removes the stencil in the cursor at a specific key.
     * 
     * @param stclContext
     *            the stencil context.
     * @param key
     *            the stencil key.
     */
    public synchronized void removeFromCursor(StclContext stclContext, IKey key) {
        PStcl stcl = _stencils.get(key);
        _stencils.remove(key);
        //_properties.remove(key);
        _modified.remove(key);
        _locked.remove(key);

        // done after removing from list to avoid recursion
        if (stcl != null) {
            //stcl.clear(stclContext);
        }
    }

    @Override
    public String toString() {
        return _name;
    }

    //
    // LOG PART
    //

    public static final StencilLog LOG = new StencilLog(_SlotCursor.class);

    public static StencilLog getLog() {
        return LOG;
    }

    protected String logWarn(StclContext stclContext, String format, Object... params) {
        if (LOG.isWarnEnabled()) {
            String msg = String.format(format, params);
            LOG.warn(stclContext, msg);
            return msg;
        }
        return "";
    }

    protected String logError(StclContext stclContext, String format, Object... params) {
        if (LOG.isErrorEnabled()) {
            String msg = String.format(format, params);
            LOG.error(stclContext, msg);
            return msg;
        }
        return "";
    }

    protected class CreatedStcl {
        public PStcl _created;

        protected CreatedStcl(PStcl created) {
            _created = created;
        }
    }

}