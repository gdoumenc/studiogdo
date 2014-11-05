/**
 * Copyright GDO - 2004
 */
package com.gdo.project.slot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import com.gdo.stencils.Result;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.atom.Atom;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.key.Key;
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
public abstract class _SlotCursor extends Atom<StclContext, PStcl> {

    public static int STRATEGY = 0;
    private int _transaction_id;

    // semaphore to block stencil in buffer
    private Semaphore _available = new Semaphore(1, false);

    // container slot name (used for debug only)
    private String _name;

    // cursor size
    private int _size;

    // stencils stored in cursor (key -> stencil)
    protected Map<String, PStcl> _stencils;

    // properties stored in cursor (key => (prop => value))
    protected Map<String, Map<String, String>> _properties;

    // set to true if properties list of a stencil was modified since last set
    // of those properties (key -> value)
    public Map<String, Boolean> _modified;

    // locked stencils (key -> stencil plugged in $locked slot)
    protected Map<String, PStcl> _locked;

    /**
     * Slot cursor constructor.
     * 
     * @param size
     *            the cursor size.
     */
    public _SlotCursor(String name, int size) {
        _name = name;
        _size = size;
        _stencils = new ConcurrentHashMap<String, PStcl>(_size);
        _properties = new ConcurrentHashMap<String, Map<String, String>>();
        _modified = new ConcurrentHashMap<String, Boolean>();
        _locked = new ConcurrentHashMap<String, PStcl>(_size);
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
    }

    // TODO complete by purge before and stencils created only on needs
    public void expunge() {
        clear();
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
     * @param list
     * @return
     */
    protected abstract PStcl createStencil(StclContext stclContext, PSlot<StclContext, PStcl> container, PSlot<StclContext, PStcl> slot, String key, List<Object> list);

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
     * @param list
     * @return
     */
    protected abstract PStcl completeCreatedStencil(StclContext stclContext, PSlot<StclContext, PStcl> container, PSlot<StclContext, PStcl> slot, IKey key, PStcl stcl, List<Object> list);

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
    public PStcl getStencil(StclContext stclContext, PSlot<StclContext, PStcl> container, PSlot<StclContext, PStcl> slot, String key) {

        // if already in cursor returns it
        PStcl contained = inCursor(stclContext, key);
        if (StencilUtils.isNotNull(contained))
            return contained;

        // creates the stencil in memory
        synchronized (this) {

            // blocks if no more place
            if (_stencils.size() >= _size) {
                try {
                    logWarn(stclContext, "block for %s in %s", key, _name);
                    _available.acquire();
                } catch (InterruptedException e) {
                    logError(stclContext, e.toString());
                    return null;
                }
            }

            // creates the stencil
            List<Object> list = new ArrayList<Object>();
            PStcl stcl = createStencil(stclContext, container, slot, key, list);
            if (StencilUtils.isNull(stcl))
                return Stcl.nullPStencil(stclContext, Result.error("cannot create cursor stencil from empty stencil"));

            // stores the stencil in cursor
            addInCursor(stclContext, stcl);
            stcl.addThisReferenceToStencil(stclContext);

            // adds locked stencil if locked
            PStcl locked = _locked.get(key);
            if (StencilUtils.isNotNull(locked))
                stcl.plug(stclContext, locked, Stcl.Slot.$LOCKED_BY);

            return completeCreatedStencil(stclContext, container, slot, new Key<String>(key), stcl, list);
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
        if (_stencils.size() >= _size) {

            // needs to release more space in memory
            // (release memory only when full)
            synchronized (this) {

                // keys to release
                Stack<String> keys = new Stack<String>();

                // releases all except the one required
                Iterator<String> iter = _stencils.keySet().iterator();
                while (iter.hasNext()) {
                    String k = iter.next();

                    // not same as key as was the last used and may be reused
                    // and stencil must not be locked
                    if (!k.equals(key) && _locked.get(k) != null)
                        keys.push(k);
                }

                // removes them from local structure
                for (String k : keys) {
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
     * @return the stencil previously plugged.
     */
    public void remove(StclContext stclContext, PSlot<StclContext, PStcl> container, PSlot<StclContext, PStcl> slot, String key) {

        // gets the stencil to be removed
        PStcl stcl = inCursor(stclContext, key.toString());
        if (StencilUtils.isNotNull(stcl)) {

            // checks was not modified before removing
            // (negative id may be released without being saved)
            Boolean modified = _modified.get(key);
            if (modified != null) {
                logWarn(stclContext, "Stencil removed from cursor without being updated : %s", stcl);
                stcl.afterRPCSet(stclContext);
            }

            // removes it from cursor
            removeFromCursor(stclContext, key);

            // replaces the stencil by cursor
            for (PStcl s : stcl.getStencilOtherPluggedReferences(stclContext))
                s.release(stclContext, container, this, new Key<String>(key));
        }
    }

    public void lock(StclContext stclContext, PStcl stencil, IKey key) {
        _locked.put(key.toString(), stencil);
    }

    public void unlock(StclContext stclContext, IKey key) {
        _locked.remove(key.toString());
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
        String k = key.toString();

        // checks the properties were not modified before reset them
        Boolean modified = _modified.get(k);
        if (modified != null) {
            logError(stclContext, "Property values set in cursor without being updated (in %s at key %s)", slot, key);
        }

        // changes properties values and sets properties not modified
        _properties.put(k, values);
        _modified.remove(k);
    }

    /**
     * Returns the property values associated to a stencil (null if not set).
     * 
     * @param stclContext
     *            the stencil context.
     * @param key
     *            the stencil key.
     * @param path
     *            the string property path.
     * @return the properties values.
     */
    public Map<String, String> getPropertiesValues(StclContext stclContext, PSlot<StclContext, PStcl> slot, String key) {
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
        _modified.remove(key.toString());
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
    public String getPropertyValue(StclContext stclContext, PSlot<StclContext, PStcl> slot, String key, String path) {
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
    public String addPropertyValue(StclContext stclContext, PSlot<StclContext, PStcl> container, PSlot<StclContext, PStcl> slot, String key, String path, String value) {
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
     * @param slot
     *            the cursor slot.
     * @param key
     *            the stencil key.
     * @return the stencil contained in the cursor (and not recreated);
     */
    public synchronized PStcl inCursor(StclContext stclContext, String key) {

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
     * @param slot
     *            the cursor slot.
     * @param key
     *            the stencil key.
     */
    public synchronized void addInCursor(StclContext stclContext, PStcl stcl) {
        String key = stcl.getKey().toString();
        _stencils.put(key, stcl);
    }

    /**
     * Removes the stencil in the cursor at a specific key.
     * 
     * @param stclContext
     *            the stencil context.
     * @param slot
     *            the cursor slot.
     * @param key
     *            the stencil key.
     */
    public synchronized void removeFromCursor(StclContext stclContext, String key) {
        _stencils.remove(key);
        _properties.remove(key);
        _modified.remove(key);
        _locked.remove(key);
    }

    @Override
    public String toString() {
        return _name;
    }

    @Override
    public int compareTo(PStcl o) {
        return 0;
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

}