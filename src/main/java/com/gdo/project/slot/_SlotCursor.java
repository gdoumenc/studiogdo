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
 * stored to be reused without recreation if available memory. Some attributes
 * my be also stored for each stencil to allow getString returns without
 * creating the stencil.
 */
public abstract class _SlotCursor extends Atom<StclContext, PStcl> {

    public static int STRATEGY = 0;

    // semaphore to block stencil in buffer
    private Semaphore _available = new Semaphore(1, false);

    // container slot name (used for debug only)
    private String _name;

    // cursor size
    private int _size;

    // stencils stored in cursor (key -> stencil)
    protected Map<String, PStcl> _stencils;

    // locked stencils (key -> stencil plugged in $locked slot)
    protected Map<String, PStcl> _locked;

    // properties stored in cursor (key => (prop => value))
    protected Map<String, Map<String, String>> _properties;

    // set to true if properties list of a stencil was modified since last set
    // of those properties (key -> value)
    public Map<String, Boolean> _modified;

    /**
     * Slot cursor constructor.
     * 
     * @param size
     *            the cursor size.
     */
    public _SlotCursor(String name, int size) {
        this._name = name;
        this._size = size;
        this._stencils = new ConcurrentHashMap<String, PStcl>(this._size);
        this._locked = new ConcurrentHashMap<String, PStcl>(this._size);
        this._properties = new ConcurrentHashMap<String, Map<String, String>>();
        this._modified = new ConcurrentHashMap<String, Boolean>();
    }

    /**
     * Removes all of the mappings from this map (optional operation). The
     * cursor will be empty after this call returns.
     */
    public void clear() {
        if (this._stencils != null)
            this._stencils.clear();
        if (this._locked != null)
            this._locked.clear();
        if (this._properties != null)
            this._properties.clear();
        if (this._modified != null)
            this._modified.clear();
        this._available = null;
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
    protected abstract PStcl createStencil(StclContext stclContext, PSlot<StclContext, PStcl> container, PSlot<StclContext, PStcl> slot, IKey key, List<Object> list);

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
    public synchronized void release(StclContext stclContext, PSlot<StclContext, PStcl> container, PSlot<StclContext, PStcl> slot, IKey key) {
        int size = this._stencils.size();
        if (size >= this._size) {

            // keys to release
            Stack<String> keys = new Stack<String>();

            // releases all except the one required
            Iterator<String> iter = this._stencils.keySet().iterator();
            while (iter.hasNext()) {
                String k = iter.next();

                // not same as key as was the last used and may be reused and
                // stencil must not be locked
                if (!k.equals(key.toString()) && this._locked.get(k) != null)
                    keys.push(k);
            }

            // removes them from local structure
            for (String k : keys) {
                PStcl stcl = remove(stclContext, container, slot, new Key<String>(k));
                if (StencilUtils.isNull(stcl))
                    logError(stclContext, "internal error in cursor slot");
            }

            logWarn(stclContext, "release for %s in slot %s in %s", key, slot, container);
            this._available.release();
        }
    }

    /**
     * Returns the stencil at a specific key. Creates it if needed. This stencil
     * must be released after use.
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
        List<Object> list;
        PStcl stcl;

        // if already in cursor returns it
        PStcl contained = inCursor(stclContext, key.toString());
        if (StencilUtils.isNotNull(contained))
            return contained;

        synchronized (this) {

            // blocks if no more place
            if (this._stencils.size() >= this._size) {
                try {
                    logWarn(stclContext, "block for %s in %s", key, this._name);
                    this._available.acquire();
                } catch (InterruptedException e) {
                    logError(stclContext, e.toString());
                    return null;
                }
            }

            // creates the stencil
            list = new ArrayList<Object>();
            stcl = createStencil(stclContext, container, slot, key, list);
            if (StencilUtils.isNull(stcl))
                return Stcl.nullPStencil(stclContext, Result.error("cannot create cursor stencil from empty stencil"));

            // stores the stencil in cursor
            addInCursor(stclContext, stcl);
            stcl.addThisReferenceToStencil(stclContext);
            
            // add locked stencil if locked
            PStcl locked = this._locked.get(key.toString());
            if (locked != null)
                stcl.plug(stclContext, locked, Stcl.Slot.$LOCKED_BY);

            return completeCreatedStencil(stclContext, container, slot, key, stcl, list);
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
    public PStcl remove(StclContext stclContext, PSlot<StclContext, PStcl> container, PSlot<StclContext, PStcl> slot, IKey key) {

        // get stencil to be removed
        PStcl stcl = inCursor(stclContext, key.toString());
        if (StencilUtils.isNotNull(stcl)) {
            String k = key.toString();

            // checks was not modified before removing
            // (negative id may be released without being saved)
            Boolean modified = this._modified.get(k);
            if (modified != null) {
                try {
                    int id = Integer.parseInt(k);
                    if (id >= 0) {
                        logWarn(stclContext, "Stencil removed from cursor without being updated : %s", stcl);
                        stcl.afterRPCSet(stclContext);
                    }
                } catch (NumberFormatException e) {
                    // not an id key
                }
            }

            // removes it from cursor
            removeFromCursor(stclContext, k);

            // replaces the stencil by cursor
            for (PStcl s : stcl.getStencilOtherPluggedReferences(stclContext))
                s.release(stclContext, container, this, key);
        }
        return stcl;
    }

    public void lock(StclContext stclContext, PStcl stencil, IKey key) {
        this._locked.put(key.toString(), stencil);
    }

    public void unlock(StclContext stclContext, IKey key) {
        this._locked.remove(key.toString());
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
        Boolean modified = this._modified.get(key);
        if (modified != null) {
            logError(stclContext, "Property values set in cursor without being updated (in %s at key %s)", slot, key);
            setPropertiesValuesNotModified(stclContext, key);
        }

        // changes properties values and sets properties not modified
        this._properties.put(key.toString(), values);
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
    public Map<String, String> getPropertiesValues(StclContext stclContext, PSlot<StclContext, PStcl> slot, IKey key) {
        return this._properties.get(key.toString());
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
        this._modified.remove(key.toString());
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
        this._modified.put(key.toString(), Boolean.TRUE);
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
    private int tid;

    public synchronized PStcl inCursor(StclContext stclContext, String key) {

        // in cursor only on same session
        if (STRATEGY == 1 && this.tid != stclContext.getTransactionId()) {
            this._stencils.clear();
            this.tid = stclContext.getTransactionId();
            return null;
        }

        // else checks in map
        if (this._stencils.containsKey(key))
            return this._stencils.get(key);
        return null;
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
        this._stencils.put(key, stcl);
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
        this._stencils.remove(key);
    }

    @Override
    public String toString() {
        return this._name;
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