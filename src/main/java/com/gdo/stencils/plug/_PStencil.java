/*
 * Copyright GDO - 2004
 */
package com.gdo.stencils.plug;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.gdo.helper.ClassHelper;
import com.gdo.helper.StringHelper;
import com.gdo.stencils.Keywords;
import com.gdo.stencils.Result;
import com.gdo.stencils._Stencil;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.atom.Atom;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.cond.PathCondition;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.faces.GdoTagExpander;
import com.gdo.stencils.faces.RenderContext;
import com.gdo.stencils.facet.FacetContext;
import com.gdo.stencils.facet.FacetResult;
import com.gdo.stencils.facet.FacetType;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.key.LinkedKey;
import com.gdo.stencils.log.StencilLog;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.SlotUtils;
import com.gdo.stencils.util.StencilUtils;
import com.gdo.util.XmlWriter;

/**
 * <p>
 * Basic implementation of a plugged stencil.
 * </p>
 * <p>
 * <i>WARNING</i> : Don't create a plugged stencil directly, use the factory
 * associated to the context.
 * </p>
 * <p>
 * A plugged stencil is a stencil with a containing slot and the associated
 * unique key if plugged in a multi slot.
 * </p>
 * <p>
 * When there was an error or a warning retrieving the plugged stencil, a status
 * is associated to the plugged stencil.
 * </p>

 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>

 */
public abstract class _PStencil<C extends _StencilContext, S extends _PStencil<C, S>> extends Atom<S> implements Cloneable {

    // maximum level search for root
    private static final int MAX_ROOT_LEVEL = 20;

    // the stencil plugged
    // (may be null then a status may be defined to explain why)
    protected _Stencil<C, S> _stencil;

    // the slot in where the stencil is plugged (null if not plugged)
    protected PSlot<C, S> _slot;

    // key if plugged in a multi slot (never empty - Key.NO_KEY)
    protected IKey _key;

    // status associated to the plugged stencil (OK/NOK)
    protected Result _result;

    // stored to avoid recalculation
    private String __pwd;

    /**
     * Plugged stencil constructor.
     * 
     * @param stencil
     *            the stencil plugged.
     * @param slot
     *            the container slot.
     * @param key
     *            the plug key.
     */
    public _PStencil(C stclContext, _Stencil<C, S> stencil, PSlot<C, S> slot, IKey key) {
        initialize(stclContext, stencil, slot, key);
    }

    /**
     * Plugged stencil constructor.
     * 
     * @param pstencil
     *            the stencil plugged.
     * @param slot
     *            the container slot.
     * @param key
     *            the plug key.
     */
    public _PStencil(C stclContext, S pstencil, PSlot<C, S> slot, IKey key) {
        initialize(stclContext, pstencil, slot, key);
    }

    /**
     * Error constructor used when the stencil is not available.
     * 
     * @param result
     *            the reason for unavailability.
     */
    public _PStencil(Result result) {
        initialize(null, (_Stencil<C, S>) null, null, Key.NO_KEY);
        _result = (result != null) ? result : Result.error("empty stencil without any reason");
    }

    /**
     * Initialize (used only internaly and by factory pool).
     * 
     * @param stencil
     *            the stencil plugged.
     * @param slot
     *            the container slot.
     * @param key
     *            the plug key.
     */
    public void initialize(C stclContext, _Stencil<C, S> stencil, PSlot<C, S> slot, IKey key) {
        _stencil = stencil;
        _slot = slot;
        _key = (key != null) ? key : Key.NO_KEY;
        _result = Result.success();
    }

    /**
     * Initialize (used only internaly and by factory pool).
     * 
     * @param pstencil
     *            the stencil plugged.
     * @param slot
     *            the container slot.
     * @param key
     *            the plug key.
     */
    public void initialize(C stclContext, S pstencil, PSlot<C, S> slot, IKey key) {
        initialize(stclContext, (_Stencil<C, S>) pstencil.getReleasedStencil(stclContext), slot, key);
    }

    /**
     * Redefine it to perform specific code before cleaning..
     * 
     * @param stclContext
     *            the stencil context.
     */
    protected void beforeClear(C stclContext) {
        if (_stencil != null) {
            _stencil.beforeClear(stclContext, self());
        }
    }

    /**
     * Clears all internal structures to free memory.
     * 
     * @param stclContext
     *            the stencil context.
     */
    public void clear(C stclContext) {
        if (_stencil != null) {
            _stencil.clear(stclContext, self());
        }
    }

    /**
     * Checks if the plugged stencil is null.
     */
    public boolean isNull() {
        if (_stencil != null && _stencil.isCleared()) {
            return true;
        }
        return (_stencil == null);
    }

    /**
     * Checks if the plugged stencil is not null.
     */
    public boolean isNotNull() {
        return !isNull();
    }

    /**
     * Return the reason why the stencil is null.
     * 
     * @return the <tt>null</tt> reason.
     */
    public final Result getResult() {
        assert (_result != null);
        return _result;
    }

    /**
     * Add another reason if the stencil is null.
     * 
     * @return the composed <tt>null</tt> reason.
     */
    public final Result addResult(Result result) {
        if (result == null) {
            throw new IllegalArgumentException("Illegal null result parameter");
        }
        if (_result == null)
            _result = result;
        else
            _result.addOther(result);
        return _result;
    }

    /**
     * Return the message why the stencil is null.
     * 
     * @return the reason why, if there is one, this stencil is null. The
     *         message is never <tt>null</tt>
     */
    public final String getNullReason() {
        String msg = getResult().getMessage(); // get result is never null
        return (msg != null) ? msg : "empty stencil without any reason";
    }

    /**
     * When accessing a stencil directly, you have to release its access after.
     * The stencil may not be available all the time (memory optimization), so
     * each time you access it, it locks the stencil in memory.
     * <p>
     * BEWARE : A released stencil is not locked, so it should be used only to
     * call java class methods.
     * </p>
     * 
     * @param stclContext
     *            stencil context.
     * @return the stencil structure (should be used only to call java class
     *         methods).
     */
    @SuppressWarnings("unchecked")
    public final <K extends _Stencil<C, S>> K getReleasedStencil(_StencilContext stclContext) {
        K stcl = (K) getStencil((C) stclContext);
        release((C) stclContext);
        return stcl;
    }

    /**
     * When accessing a stencil directly, you have to release its access after.
     * The stencil may not be available all the time (memory optimization), so
     * each time you access it, it locks the stencil in memory.
     * 
     * @param stclContext
     *            stencil context.
     * @return the stencil structure.
     */
    // must be non final (redefined in Stcl)
    @SuppressWarnings("unchecked")
    public <K extends _Stencil<C, S>> K getStencil(C stclContext) {
        return (K) _stencil;
    }

    /**
     * Releases the stencil, so it may not be available in further code.
     * 
     * @param stclContext
     *            stencil context.
     */
    public void release(C stclContext) {
        // no code as release function is defined only for specific cursor
        // plugged stencil implementation
    }

    /**
     * Checks if the stencil is a property.
     * 
     * @param stclContext
     *            the stencil context.
     * @return <tt>true</tt> if the stencil is a property.
     */
    public boolean isProp(C stclContext) {
        return false;
    }

    public boolean isLink(C stclContext) {
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        return stcl.isLink(stclContext, self());
    }

    /**
     * Returns the slot containing this plugged stencil.
     * 
     * @return the container slot.
     */
    public final PSlot<C, S> getContainingSlot() {
        return _slot;
    }

    // should be used internaly only.
    public final void setContainingSlot(PSlot<C, S> slot) {
        _slot = slot;
    }

    /**
     * The key used for the plug. This key is never <tt>null</tt> so it can be
     * transformed to a string at anytime.
     * 
     * @return the plug key.
     */
    public IKey getKey() {
        if (_key == Key.NO_KEY) {
            return _key;
        }
        return new LinkedKey(_key);
    }

    /**
     * A new linked key used for the plug. The returned key is a link to the
     * real one to propagate value changes if key changes.
     * <p>
     * Use this function only if you want to keep current key value all time.
     * </p>
     * 
     * @return the plug key.
     */
    public final IKey getLinkedKey() {
        return new LinkedKey(_key);
    }

    /**
     * Changes the key. Should be used only by slot implementation.
     * 
     * @param key
     *            the new key.
     */
    public final void setKey(IKey key) {
        if (key == null) {
            throw new IllegalArgumentException("Illegal null key parameter");
        }
        _key = key;
    }

    /**
     * Returns the plugged stencil container.
     * 
     * @return the plugged stencil container.
     */
    public S getContainer(C stclContext) {
        if (SlotUtils.isNull(getContainingSlot())) {
            return nullPStencil(stclContext, Result.error("no container stencil"));
        }
        return getContainingSlot().getContainer();
    }

    /**
     * Test if a stencil is transient (will be not stored in configuration).
     * 
     * @param stclContext
     *            the stencil context.
     * @return <tt>true</tt> if the stencil is transient.
     */
    public boolean isTransient(C stclContext) {
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        return stcl.isTransient(stclContext, self());
    }

    /**
     * Set the transient property of the stencil.
     * 
     * @param stclContext
     *            the stencil context.
     * @param value
     *            the transient value.
     */
    public void setTransient(C stclContext, boolean value) {
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        stcl.setTransient(stclContext, value, self());
    }

    /**
     * Test if the stencil is not plugged anywhere.
     * 
     * @param stclContext
     *            the stencil context.
     * @return <tt>true</tt> if the stencil is not plugged anywhere.
     */
    public final boolean isNotPlugged(C stclContext) {
        List<S> list = getStencilOtherPluggedReferences(stclContext);
        return (list == null || list.size() == 0);
    }

    /**
     * Test if the stencil is plugged in one slot only.
     * 
     * @param stclContext
     *            the stencil context.
     * @return <tt>true</tt> if the stencil is plugged in one slot only.
     */
    public boolean isPluggedOnce(C stclContext) {
        List<S> list = getStencilOtherPluggedReferences(stclContext);
        return (list != null && list.size() == 1);
    }

    /**
     * Return the hierarchical plugging root of the stencil. A counter is set to
     * avoid circular hierarchy search.
     * 
     * @param stclContext
     *            the stencil context.
     * @return the root stencil container of this plugged stencil.
     */
    public S getRootStencil(C stclContext) {
        int i = 0;
        S root = self();
        while (root.getContainer(stclContext).isNotNull()) {
            root = root.getContainer(stclContext);
            if (MAX_ROOT_LEVEL != -1 && i++ > MAX_ROOT_LEVEL) {
                if (getLog().isWarnEnabled()) {
                    getLog().warn(stclContext, "Cannot get root : maximum search level reached");
                }
                break;
            }
        }
        return root;
    }

    /**
     * Return the name of the stencil.
     * 
     * @param stclContext
     *            the stencil context.
     * @return the name of the stencil.
     */
    public String getName(C stclContext) {
        if (isNull()) {
            return "unvalid stencil name: " + getResult().getMessage();
        }
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        return stcl.getName(stclContext, self());
    }

    /**
     * Return the full name of the stencil (including its path).
     * 
     * @param stclContext
     *            the stencil context.
     * @return the full name of the stencil.
     */
    public String getFullName(C stclContext) {
        if (isNull()) {
            return "unvalid stencil full name: " + getResult().getMessage();
        }
        return pwd(stclContext) + "[" + getName(stclContext) + "]";
    }

    /**
     * Returns the absolute path of the plugged stencil.
     * 
     * @param stclContext
     *            the stencil context.
     * @return the absolute path.
     */
    public String pwd(C stclContext) {

        // checks the stencil
        if (isNull()) {
            return "unvalid pwd on null stencil: " + getResult().getMessage();
        }

        // calculate pwd only once
        if (__pwd == null) {
            StringBuffer path = new StringBuffer();
            pwd(stclContext, self(), path, 0);
            __pwd = path.toString();
        }
        return __pwd;
    }

    /**
     * Appends in the path, the stencil absolute path.
     * 
     * @param stclContext
     *            the stencil context.
     * @param stcl
     *            the plugged stencil
     * @param path
     *            the string buffer containning the path.
     * @param counter
     *            recursive limitation.
     * @return <tt>true</tt> if the path terminates by SEP.
     */
    private boolean pwd(C stclContext, S stcl, StringBuffer path, int counter) {

        // limits recursion
        if (counter > MAX_ROOT_LEVEL || stcl.isNull()) {
            return true;
        }

        // specific root case
        S container = stcl.getContainer(stclContext);
        if (StencilUtils.isNull(container)) {
            path.append(PathUtils.ROOT);
            return true;
        }

        // recursive parent path creation
        boolean sep = pwd(stclContext, container, path, counter + 1);
        if (!sep) {
            path.append(PathUtils.SEP);
        }

        // add slot and key (if not THIS)
        String slotName = stcl.getContainingSlot().getName(stclContext);
        if (PathUtils.THIS.equals(slotName)) {
            return true;
        }
        IKey key = stcl.getKey();
        path.append(slotName);
        if (key != null && key.isNotEmpty()) {
            path.append(PathUtils.KEY_SEP_OPEN).append(key.toString()).append(PathUtils.KEY_SEP_CLOSE);
        }
        return false;
    }

    public String setName(C stclContext, String name) {
        if (isNull())
            return null;
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        return stcl.setName(stclContext, name, self());
    }

    public boolean canChangeOrder(C stclContext) {
        if (isNull())
            return false;
        return getContainingSlot().canChangeOrder(stclContext);
    }

    public final boolean isFirst(C stclContext) {
        if (isNull())
            return false;
        return getContainingSlot().isFirst(stclContext, self());
    }

    public final boolean isLast(C stclContext) {
        if (isNull())
            return false;
        return getContainingSlot().isLast(stclContext, self());
    }

    // --------------------------------------------------------------------------
    //
    // Modification management.
    //
    // --------------------------------------------------------------------------

    public Result afterRPCSet(C stclContext) {
        if (isNull()) {
            throw new IllegalStateException("cannot call cursor after set actions from an unvalid stencil: " + getNullReason());
        }
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        return stcl.afterRPCSet(stclContext, self());
    }

    /*
     * Stencil interface reported
     */

    public final String getTemplateName(C stclContext) {
        if (isNull()) {
            return "unvalid stencil template name: " + getNullReason();
        }
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        return stcl.getTemplateName();
    }

    /**
     * Checks if a command is defined.
     * 
     * @param stclContext
     *            the stencil context.
     * @param name
     *            the command name.
     * @return <tt>true</tt> if the command is defined.
     */
    public final boolean hasCommand(C stclContext, String name) {
        if (isNull()) {
            return false;
        }
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        return stcl.hasCommand(stclContext, name, self());
    }

    /**
     * Returns the command defined by a name or by a template class name.
     * 
     * @param stclContext
     *            the stencil context.
     * @param name
     *            the command name or the command template name.
     * @return the plugged command.
     */
    public S getCommand(C stclContext, String name) {
        if (isNull()) {
            throw new IllegalStateException("cannot get a command from an unvalid stencil: " + getNullReason());
        }
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        return stcl.getCommand(stclContext, name, self());
    }

    /**
     * Calls a command with parameters.
     * 
     * @param stclContext
     *            the stencil context.
     * @param name
     *            the command name or the command template name.
     * @param params
     *            the parameters redefined.
     * @return the command status.
     */
    public final CommandStatus<C, S> call(C stclContext, String name, Object... params) {
        if (isNull()) {
            throw new IllegalStateException("cannot call from an unvalid stencil: " + getNullReason());
        }
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        return stcl.call(stclContext, name, self(), params);
    }

    /**
     * Calls another command with the same command context. TODO should be
     * defined for command stencil only
     * 
     * @param cmdContext
     *            the command context.
     * @param name
     *            the command name or the command template name.
     * @return the command status.
     */
    public final CommandStatus<C, S> call(CommandContext<C, S> cmdContext, String name) {
        if (isNull()) {
            throw new IllegalStateException("cannot call from an unvalid stencil: " + getNullReason());
        }
        _Stencil<C, S> stcl = getReleasedStencil(cmdContext.getStencilContext());
        return stcl.call(cmdContext, name, self());
    }

    @Deprecated
    public boolean hasSlot(C stclContext, String path) {
        if (isNull())
            return false;
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        return stcl.hasSlot(stclContext, path, self());
    }

    public PSlot<C, S> getSlot(C stclContext, String slotPath) {

        if (isNull()) {
            throw new IllegalStateException("cannot get a slot from an unvalid stencil: " + getNullReason());
        }
        String path = slotPath;

        // verifies path is correct
        if (StringUtils.isEmpty(path)) {
            String msg = logWarn(stclContext, "Cannot retrieve a slot from an empty path");
            return new PSlot<C, S>(Result.error(msg));
        }

        // removes last '/' if exists
        if (path.length() > 1 && path.endsWith(PathUtils.SEP_STR)) {
            path = StringHelper.substringEnd(path, PathUtils.SEP_INT);
        }

        // composed path
        if (PathUtils.isComposed(path)) {
            String first = PathUtils.getPathName(path);
            String tail = PathUtils.getLastName(path);

            // gets container stencil
            S stcl = getStencil(stclContext, first);
            if (StencilUtils.isNull(stcl)) {
                String msg = String.format("Cannot get slot %s in %s as stencil at path %s doesn't exists", path, this, first);
                if (getLog().isWarnEnabled())
                    getLog().warn(stclContext, msg);
                return new PSlot<C, S>(Result.error(msg));
            }

            return stcl.getSlot(stclContext, tail);
        }

        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        return stcl.getSlot(stclContext, path, self());
    }

    public final int size(C stclContext, String path, StencilCondition<C, S> cond) {
        if (isNull())
            throw new IllegalStateException("cannot get slot size from an unvalid stencil: " + getNullReason());
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        return stcl.size(stclContext, path, cond, self());
    }

    public final boolean hasStencils(C stclContext, String path, StencilCondition<C, S> cond) {

        // checks validity
        if (isNull()) {
            throw new IllegalStateException("cannot test if a slot has stencils from an unvalid stencil: " + getNullReason());
        }

        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        return stcl.hasStencils(stclContext, path, cond, self());
    }

    public final S getStencil(C stclContext, String path) {

        // absolute path often from relative
        if (PathUtils.isAbsolute(path)) {
            String pwd = pwd(stclContext);
            if (path.startsWith(pwd)) {
                String p = path.substring(pwd.length());
                if (p.startsWith("/"))
                    p = p.substring(1);
                if (StringUtils.isBlank(p))
                    return self();
                else
                    return getStencil(stclContext, p);
            }
        }

        // else
        return getStencil(stclContext, path, (StencilCondition<C, S>) null);
    }

    public final <K> S getStencil(C stclContext, String path, IKey key) {
        StencilCondition<C, S> cond = PathCondition.<C, S> newKeyCondition(stclContext, key, self());
        return getStencil(stclContext, path, cond);
    }

    public final S getStencil(C stclContext, String path, StencilCondition<C, S> cond) {

        // checks validity
        if (isNull()) {
            throw new IllegalStateException("cannot get a stencil from an unvalid stencil: " + getNullReason());
        }

        // optimization
        if (cond == null) {
            if (PathUtils.ROOT.equals(path)) {
                return getRootStencil(stclContext);
            } else if (PathUtils.THIS.equals(path)) {
                return self();
            } else if (PathUtils.PARENT.equals(path)) {
                return getContainer(stclContext);
            }
        }

        // optimization for absolute pathes
        if (PathUtils.isAbsolute(path)) {
            S root = getRootStencil(stclContext);
            if (PathUtils.ROOT.equals(path)) {
                return root;
            }
            String tail = PathUtils.getTailName(path);
            return root.getStencil(stclContext, tail, cond);
        }

        // propagates to the structure
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        return stcl.getStencil(stclContext, path, cond, self());
    }

    public final StencilIterator<C, S> getStencils(C stclContext, String path) {
        return getStencils(stclContext, path, null);
    }

    public final StencilIterator<C, S> getStencils(C stclContext, String path, StencilCondition<C, S> cond) {

        // absolute pathes can reached even on empty stencil
        if (PathUtils.isAbsolute(path)) {
            S root = getRootStencil(stclContext);
            if (PathUtils.ROOT.equals(path)) {
                return StencilUtils.<C, S> iterator(stclContext, root, root.getContainingSlot());
            }
            String tail = PathUtils.getTailName(path);
            return root.getStencils(stclContext, tail, cond);
        }

        // checks validity
        if (isNull()) {
            throw new IllegalStateException("cannot get stencils from an unvalid stencil: " + getNullReason());
        }

        // propagates to the structure
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        return stcl.getStencils(stclContext, path, cond, self());
    }

    /**
     * Returns the map of attributes defined on a single stencil.
     * 
     * @param stclContext
     *            the stencil context.
     * @param attributes
     *            the attributes path.
     * @return the attributes map.
     */
    public final SortedMap<IKey, String[]> getAttributes(C stclContext, String[] attributes) {
        if (isNull()) {
            throw new IllegalStateException("cannot get attributes from an unvalid stencil: " + getNullReason());
        }
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        return stcl.getAttributes(stclContext, attributes, self());
    }

    /**
     * Returns the map of attributes defined over several stencils. Should not
     * be used for getting attributes list of several stencils (use stencils
     * entry).
     * 
     * @param stclContext
     *            the stencil context.
     * @param path
     *            the stencils path.
     * @param attributes
     *            the attributes path.
     * @return the attributes map.
     */
    public final SortedMap<IKey, String[]> getAttributes(C stclContext, String path, String[] attributes) {
        if (isNull()) {
            throw new IllegalStateException("cannot get attributes from an unvalid stencil: " + getNullReason());
        }
        SortedMap<IKey, String[]> map = new TreeMap<IKey, String[]>();
        StencilIterator<C, S> iter = getStencils(stclContext, path);
        while (iter.hasNext()) {
            S stcl = iter.next();
            Map<IKey, String[]> m = stcl.getAttributes(stclContext, attributes);
            for (Entry<IKey, String[]> e : m.entrySet()) {
                map.put(e.getKey(), e.getValue());
            }
        }
        return map;
    }

    // TODO usefull ??
    @Deprecated
    public final boolean isPluggedIn(C stclContext, S stencil, String path) {
        if (isNull())
            throw new IllegalStateException("cannot check isPluggedIn from an unvalid stencil: " + getNullReason());
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        return stcl.isPluggedIn(stclContext, stencil, path, self());
    }

    // --------------------------------------------------------------------------
    //
    // Properties management.
    //
    // --------------------------------------------------------------------------

    public String getType(C stclContext, String path) {

        // checks validity
        if (isNull()) {
            throw new IllegalStateException("cannot get a type from an unvalid stencil: " + getNullReason());
        }

        // gets type from structure
        S stcl = getStencil(stclContext, path);
        if (stcl.isNull()) {
            return Keywords.STRING;
        }
        return stcl.getReleasedStencil(stclContext).getType(stclContext, stcl);
    }

    public String getString(C stclContext, String path, String def) {

        // checks validity
        if (isNull()) {
            throw new IllegalStateException("cannot get a string from an unvalid stencil: " + getNullReason());
        }

        // gets it from container if composed path
        if (PathUtils.isComposed(path)) {
            String p = PathUtils.getPathName(path);
            String l = PathUtils.getLastName(path);
            S container = getStencil(stclContext, p);
            return container.getString(stclContext, l, def);
        }

        // if the property path is the stencil id
        if (path.endsWith(PathUtils.UID)) {
            String p = PathUtils.getPathOrThis(StringHelper.substringEnd(path, 1));
            S s = getStencil(stclContext, format(stclContext, p));
            return s.getUId(stclContext);
        }

        // if the property path is the stencil absolute path
        if (path.endsWith(PathUtils.ABSOLUTE_PATH)) {
            String p = PathUtils.getPathOrThis(StringHelper.substringEnd(path, 1));
            S s = getStencil(stclContext, format(stclContext, p));
            return s.pwd(stclContext);
        }

        // if the property path is the stencil plugging key
        if (path.endsWith(PathUtils.KEY)) {
            String p = PathUtils.getPathOrThis(StringHelper.substringEnd(path, 1));
            S s = getStencil(stclContext, format(stclContext, p));
            return s.getKey().toString();
        }

        if (path.endsWith(PathUtils.NUMBER_NUMBER)) {
            String slotPath = StringHelper.substringEnd(path, 2);

            // the slot path must be defined
            if (StringUtils.isBlank(slotPath)) {
                return "no slot defined for slot full size facet";
            }

            // get slot
            String expandedPath = format(stclContext, slotPath);
            PSlot<C, S> slot = getSlot(stclContext, expandedPath);

            // the slot must be defined
            if (SlotUtils.isNull(slot)) {
                return String.format("wrong path %s for slot size facet", slotPath);
            }

            // get slot size
            int size = slot.getStencils(stclContext, null).size();
            return Integer.toString(size);
        }

        // if the property path is the is a slot size
        if (path.endsWith(PathUtils.NUMBER)) {
            String slotPath = StringHelper.substringEnd(path, 1);

            // the slot path must be defined
            if (StringUtils.isBlank(slotPath)) {
                return "no slot defined for slot size facet";
            }

            // gets slot
            String expandedPath = format(stclContext, slotPath);
            PSlot<C, S> slot = getSlot(stclContext, expandedPath);

            // the slot must be defined
            if (SlotUtils.isNull(slot)) {
                return String.format("wrong slot path %s for slot size facet on %s", slotPath, this);
            }

            // gets slot size
            StencilCondition<C, S> cond = null;
            if (PathUtils.isKeyContained(path) || PathUtils.isExpContained(path)) {
                cond = new PathCondition<C, S>(stclContext, PathUtils.getCondition(expandedPath), slot.getContainer());
            }
            int size = slot.size(stclContext, cond);
            return Integer.toString(size);
        }

        // gets string from value
        S prop = getStencil(stclContext, path);
        if (StencilUtils.isNull(prop)) {
            throw new IllegalStateException(prop.getNullReason());
        }

        String value = prop.getReleasedStencil(stclContext).getValue(stclContext, prop);
        if (value != null) {
            return value;
        }
        return def;
    }

    public String getExpandedString(C stclContext, String path, String def) {

        // checks validity
        if (isNull()) {
            throw new IllegalStateException("cannot get a property from an unvalid stencil: " + getNullReason());
        }

        // return expanded value
        String value = getString(stclContext, path, def);
        return format(stclContext, value);
    }

    public String getNotExpandedString(C stclContext, String path, String def) {

        // checks validity
        if (isNull()) {
            throw new IllegalStateException("cannot get a property from an unvalid stencil: " + getNullReason());
        }

        // return expanded value
        return getString(stclContext, path, def);
    }

    public int getInt(C stclContext, String path, int def) {
        try {
            String value = getString(stclContext, path, null);
            return Integer.parseInt(value);
        } catch (Exception e) {
            return def;
        }
    }

    public boolean getBoolean(C stclContext, String path, boolean def) {
        try {
            String value = getString(stclContext, path, null);
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
            return def;
        }
    }

    public double getDouble(C stclContext, String path, double def) {
        try {
            String value = getString(stclContext, path, null);
            return Double.parseDouble(value);
        } catch (Exception e) {
            return def;
        }
    }

    public InputStream getInputStream(C stclContext, String path) {
        String value = getString(stclContext, path, "");
        return IOUtils.toInputStream(value);
    }

    /*
     * Set property values.
     */

    public void setString(C stclContext, String path, String value) {

        // checks validity
        if (isNull()) {
            throw new IllegalStateException("cannot set a property from an unvalid stencil: " + getNullReason());
        }

        // sets it to container if composed path
        if (PathUtils.isComposed(path)) {
            String p = PathUtils.getPathName(path);
            String l = PathUtils.getLastName(path);
            S container = getStencil(stclContext, p);
            container.setString(stclContext, l, value);
            return;
        }

        // sets it to this stencil
        S prop = getStencil(stclContext, path);
        if (StencilUtils.isNull(prop)) {
            throw new IllegalStateException(prop.getNullReason());
        }
        prop.setValue(stclContext, value);
    }

    public int setInt(C stclContext, String path, int value) {
        setString(stclContext, path, Integer.toString(value));
        return value;
    }

    public boolean setBoolean(C stclContext, String path, boolean value) {
        setString(stclContext, path, Boolean.toString(value));
        return value;
    }

    public double setDouble(C stclContext, String path, double value) {
        setString(stclContext, path, Double.toString(value));
        return value;
    }

    /*
     * factory
     */

    public S nullPStencil(C stclContext) {
        return StencilUtils.<C, S> nullPStencil(stclContext, Result.success());
    }

    public S nullPStencil(C stclContext, Result reasons) {
        return StencilUtils.<C, S> nullPStencil(stclContext, reasons);
    }

    public S newPStencil(C stclContext, String path, IKey key, Class<? extends _Stencil<C, S>> clazz, Object... params) {
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        PSlot<C, S> slot = getSlot(stclContext, path);
        return stcl.newPStencil(stclContext, slot, key, clazz, self(), params);
    }

    @Deprecated
    public S newPStencil(C stclContext, String path, IKey key, String stencilClassName, Object... params) {
        Class<? extends _Stencil<C, S>> clazz = ClassHelper.loadClass(stencilClassName);
        return newPStencil(stclContext, path, key, clazz, params);
    }

    /**
     * Creates a new stencil and plugs it in a slot.
     * 
     * @param stclContext
     *            the stencil context.
     * @param slot
     *            the slot in where the created stencil will be plugged.
     * @param key
     *            the key for the plug in slot.
     * @param clazz
     *            the stencil stencil class name (template).
     * @param params
     *            the parameters for stencil constructor (if needed).
     * @return the new plugged stencil.
     */
    public S newPStencil(C stclContext, PSlot<C, S> slot, IKey key, Class<? extends _Stencil<C, S>> clazz, Object... params) {
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        return stcl.newPStencil(stclContext, slot, key, clazz, self(), params);
    }

    @Deprecated
    public S newPStencil(C stclContext, PSlot<C, S> slot, IKey key, String stencilClassName, Object... params) {
        Class<? extends _Stencil<C, S>> clazz = ClassHelper.loadClass(stencilClassName);
        return newPStencil(stclContext, slot, key, clazz, params);
    }

    public <V> S newPProperty(C stclContext, PSlot<C, S> slot, IKey key, V value, Object... params) {
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        return stcl.newPProperty(stclContext, slot, key, value, self(), params);
    }

    public <V> S newPProperty(C stclContext, String slotName, IKey key, V value, Object... params) {
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        PSlot<C, S> slot = getSlot(stclContext, slotName);
        return stcl.newPProperty(stclContext, slot, key, value, self(), params);
    }

    /*
     * view
     */

    public FacetResult getFacet(RenderContext<C, S> renderContext) {

        // checks validity
        if (isNull()) {
            return new FacetResult(FacetResult.ERROR, "invalid stencil", null);
        }

        C stclContext = renderContext.getStencilContext();
        String facet = renderContext.getFacetType();

        // if facet is stencil id
        if (facet.endsWith(PathUtils.UID)) {
            S stcl = renderContext.getStencilRendered();
            String path = StringHelper.substringEnd(facet, 1);
            if (!StringUtils.isEmpty(path)) {
                String expandedPath = stcl.format(stclContext, path);
                stcl = stcl.getStencil(stclContext, expandedPath);
            }
            if (StencilUtils.isNull(stcl)) {
                String msg = String.format("wrong path %s for facet %s(uid)", path, PathUtils.UID);
                return new FacetResult(FacetResult.ERROR, msg, null);
            }
            return getReaderFromStringFacet(stclContext, stcl.getUId(stclContext), renderContext);
        }

        // stencil absolute path
        if (facet.endsWith(PathUtils.ABSOLUTE_PATH)) {
            return getReaderFromStringFacet(stclContext, pwd(stclContext), renderContext);
        }

        // stencil plugging key
        if (facet.endsWith(PathUtils.KEY)) {
            S stcl = renderContext.getStencilRendered();
            String path = StringHelper.substringEnd(facet, 1);
            if (!StringUtils.isEmpty(path)) {
                String expandedPath = stcl.format(stclContext, path);
                stcl = stcl.getStencil(stclContext, expandedPath);
            }
            if (StencilUtils.isNull(stcl)) {
                String msg = String.format("wrong path %s for facet %s(key)", path, PathUtils.KEY);
                return new FacetResult(FacetResult.ERROR, msg, null);
            }
            return getReaderFromStringFacet(stclContext, stcl.getKey().toString(), renderContext);
        }

        // facet is slot size (calculated from stencils enumeration)
        if (facet.endsWith(PathUtils.NUMBER_NUMBER)) {
            String slotPath = StringHelper.substringEnd(facet, 2);

            // checks the slot path (must be defined)
            if (StringUtils.isBlank(slotPath)) {
                return new FacetResult(FacetResult.ERROR, "no slot defined for slot size facet", null);
            }

            // gets slot
            String expandedPath = format(stclContext, slotPath);
            PSlot<C, S> slot = getSlot(stclContext, expandedPath);

            // checks slot exist
            if (SlotUtils.isNull(slot)) {
                String msg = String.format("wrong path %s for slot size facet", slotPath);
                return new FacetResult(FacetResult.ERROR, msg, null);
            }

            // gets slot size
            int size = slot.getStencils(stclContext, null).size();
            return getReaderFromStringFacet(stclContext, Integer.toString(size), renderContext);
        }

        // facet is slot size (calculated from size function)
        if (facet.endsWith(PathUtils.NUMBER)) {
            String slotPath = StringHelper.substringEnd(facet, 1);

            // checks the slot path (must be defined)
            if (StringUtils.isBlank(slotPath)) {
                return new FacetResult(FacetResult.ERROR, "no slot defined for slot size facet", null);
            }

            // gets slot
            String expandedPath = format(stclContext, slotPath);
            PSlot<C, S> slot = getSlot(stclContext, expandedPath);

            // checks slot exist
            if (SlotUtils.isNull(slot)) {
                String msg = String.format("wrong path %s for slot size facet", slotPath);
                return new FacetResult(FacetResult.ERROR, msg, null);
            }

            // gets slot size
            int size = slot.size(stclContext, null);
            return getReaderFromStringFacet(stclContext, Integer.toString(size), renderContext);
        }

        // searches in stencil itself
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        FacetResult result = stcl.getFacet(renderContext);
        if (result.isSuccess()) {
            return result;
        }

        // if not found search in default substitution
        String mode = renderContext.getFacetMode();
        String msg = String.format("No facet found for %s for type %s in mode %s", this, facet, mode);
        return new FacetResult(FacetResult.ERROR, msg, null);
    }

    /**
     * @return a reader on the formatted string facet (format is taken from
     *         mode)
     */
    private FacetResult getReaderFromStringFacet(C stclContext, String str, FacetContext facetContext) {

        // get string format used to render string value
        String mode = facetContext.getFacetMode();
        String format = StringUtils.isEmpty(mode) ? "%s" : mode;

        // trace facet found
        if (getLog().isTraceEnabled()) {
            String facet = facetContext.getFacetType();
            String msg = String.format("Facet found for %s for facet type %s in mode %s", this, facet, mode);
            getLog().trace(stclContext, msg);
        }

        // return reader on formatted value
        InputStream is;
        try {
            is = new ByteArrayInputStream(String.format(format, str).getBytes(_StencilContext.getCharacterEncoding()));
            return new FacetResult(is, "text/plain");
        } catch (UnsupportedEncodingException e) {
        }
        return new FacetResult(FacetResult.ERROR, "", null);
    }

    public String format(C stclContext, String text) {

        // optimization cases
        if (isNull() || StringUtils.isBlank(text) || !StencilUtils.containsStencilTag(text)) {
            return text;
        }

        // call DSL expanser
        RenderContext<C, S> renderContext = new RenderContext<C, S>(stclContext, self(), FacetType.LABEL, StringHelper.EMPTY_STRING);
        GdoTagExpander<C, S> exp = new GdoTagExpander<C, S>(text, renderContext);
        return exp.expand(stclContext);
    }

    public S plug(C stclContext, S stencil, String slotPath, IKey key) {
        if (isNull()) {
            return this.self();
        }
        if (StencilUtils.isNull(stencil)) {
            String msg = String.format("try to plug a null stencil in %s", this);
            return nullPStencil(stclContext, Result.error(msg));
        }
        if (StringUtils.isEmpty(slotPath)) {
            String msg = String.format("slot path not defined to plug %s in %s", stencil, this);
            return nullPStencil(stclContext, Result.error(msg));
        }
        if (PathUtils.isKeyContained(slotPath)) {
            String msg = String.format("should not call plug in a slot path  %s with key %s", slotPath, key.toString());
            getLog().warn(stclContext, msg);
        }

        PSlot<C, S> slot = getSlot(stclContext, slotPath);
        if (SlotUtils.isNull(slot)) {
            String msg = logWarn(stclContext, "Cannot plug as slot %s doesn't exist in %s", slotPath, this);
            return StencilUtils.<C, S> nullPStencil(stclContext, Result.error(msg));
        }
        return plug(stclContext, stencil, slot, key);
    }

    public S plug(C stclContext, S stencil, String path) {
        if (PathUtils.isKeyContained(path)) {
            String slotPath = PathUtils.getSlotPath(path);
            String key = PathUtils.getKeyContained(path);
            return plug(stclContext, stencil, slotPath, key);
        }
        return plug(stclContext, stencil, path, Key.NO_KEY);
    }

    public S plug(C stclContext, S stencil, String slotPath, String key) {
        return plug(stclContext, stencil, slotPath, new Key(key));
    }

    public S plug(C stclContext, S stencil, String slotPath, int key) {
        return plug(stclContext, stencil, slotPath, new Key(key));
    }

    public S plug(C stclContext, S stencil, PSlot<C, S> slot, IKey key) {
        if (isNull()) {
            return nullPStencil(stclContext, Result.error("try to plug in an invalid stencil"));
        }
        if (StencilUtils.isNull(stencil)) {
            String msg = String.format("try to plug a null stencil in %s", this);
            return nullPStencil(stclContext, Result.error(msg));
        }
        if (SlotUtils.isNull(slot)) {
            String msg = String.format("slot not defined for plug %s in %s", stencil, this);
            return nullPStencil(stclContext, Result.error(msg));
        }

        return slot.plug(stclContext, stencil, key);
    }

    public S plug(C stclContext, S stencil, PSlot<C, S> slot) {
        return plug(stclContext, stencil, slot, Key.NO_KEY);
    }

    public S plug(C stclContext, S stencil, PSlot<C, S> slot, String key) {
        return plug(stclContext, stencil, slot, new Key(key));
    }

    public S plug(C stclContext, S stencil, PSlot<C, S> slot, int key) {
        return plug(stclContext, stencil, slot, new Key(key));
    }

    // TODO all following method should return a S value
    /**
     * Unplugs the stencil from the path.
     * 
     * @param path
     *            path from where the stencil should be removed (usually a slot
     *            path).
     */
    public void unplugFrom(C stclContext, String path) {
        if (isNull())
            return;
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        stcl.unplugFrom(stclContext, path, self());
    }

    /**
     * Removes this stencil from all containing slots and releases memory.
     */
    public void unplugFromAllSlots(C stclContext) {

        // never knows
        if (isNull()) {
            return;
        }

        // removes from each containing slot
        List<S> list = getStencilOtherPluggedReferences(stclContext);
        while (list.size() > 0) {
            S reference = list.remove(0);
            PSlot<C, S> slot = reference.getContainingSlot();

            // checks the containig slot still exist
            if (SlotUtils.isNull(slot)) {
                continue;
            }

            // creates the order
            IKey key = reference.getKey();
            slot.unplug(stclContext, self(), key);

            // recalculate containing list
            list = getStencilOtherPluggedReferences(stclContext);
        }

        // remove myself from slot
        PSlot<C, S> slot = getContainingSlot();
        IKey key = getKey();
        slot.unplug(stclContext, self(), key);

        // releases memory
        beforeClear(stclContext);
        // clear(stclContext);
    }

    /**
     * Unplugs another stencil from a path.
     * 
     * @param path
     *            path to the stencil to be removed.
     */
    public void unplugOtherStencilFrom(C stclContext, String path) {
        if (isNull()) {
            return;
        }
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        stcl.unplugOtherStencilFrom(stclContext, path, self());
    }

    /**
     * Unplugs another stencil.
     * 
     * @param path
     *            path from where the stencil should be removed (usually a slot
     *            path).
     * @param stencil
     *            the stencil to be removed.
     */
    public void unplugOtherStencilFrom(C stclContext, String path, S stencil) {
        if (isNull())
            return;
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        stcl.unplugOtherStencilFrom(stclContext, path, stencil, self());
    }

    /**
     * Removes all contained stencils from a slot.
     * 
     * @param stclContext
     *            the stencil context.
     * @param slotPath
     *            the path to the slot to be cleared.
     * @return the slot cleared.
     */
    public PSlot<C, S> clearSlot(C stclContext, String slotPath) {
        if (isNull()) {
            throw new IllegalStateException("cannot clear a slot from an unvalid stencil: " + getNullReason());
        }

        PSlot<C, S> slot = getSlot(stclContext, slotPath);
        if (SlotUtils.isNotNull(slot)) {
            slot.unplugAll(stclContext);
        }
        return slot;
    }

    /*
     * IAtom interface
     */

    @Override
    public String getId(_StencilContext stclContext) {
        if (isNull()) {
            throw new UnsupportedOperationException("cannot get the id of an empty stencil: " + getNullReason());
        }
        return getReleasedStencil(stclContext).getId(stclContext);
    }

    //
    // Informations relative to containing slots (to understand where the
    // stencil is..)
    //

    // should be used internally (used only if isPluggedOnce checked before)
    public PSlot<C, S> getContainingSlot(C stclContext) {
        List<S> list = getStencilOtherPluggedReferences(stclContext);
        return list.get(0).getContainingSlot();
    }

    public List<S> getStencilOtherPluggedReferences(C stclContext) {
        _Stencil<C, S> stcl = getStencil(stclContext);
        List<S> list = stcl.getPluggedReferences(stclContext);
        release(stclContext);
        return list;
    }

    /**
     * Stores a reference to the slot where the stencil is plugged in. As the
     * same stencil can be plugged in the same slot with another key, we store
     * the stencil so we can retrieve the slot and the key.
     */
    public void addThisReferenceToStencil(C stclContext) {
        List<S> list = getStencilOtherPluggedReferences(stclContext);
        list.add(self());
    }

    /**
     * Removes the reference to the slot where the stencil was plugged in.
     */
    public void removeThisReferenceFromStencil(C stclContext) {
        List<S> list = getStencilOtherPluggedReferences(stclContext);
        list.remove(self());
    }

    /**
     * Deprecated : Use beforeClear
     */
    @Deprecated
    public void afterLastUnplug(C stclContext) {
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        stcl.afterLastUnplug(stclContext, self());
    }

    @Override
    public int hashCode() {
        int result = ((_key == null) ? 0 : _key.hashCode());
        result += ((_slot == null) ? 0 : _slot.hashCode());
        result += ((_stencil == null) ? 0 : _stencil.hashCode());
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null) {
            return _stencil == null;
        }
        if (obj instanceof _Stencil) {
            return _stencil == (_Stencil<C, ?>) obj;
        }
        if (obj instanceof _PStencil) {
            return _stencil == ((_PStencil<C, ?>) obj)._stencil;
        }
        if (obj instanceof String) {
            return obj.equals(getKey().toString());
        }
        return false;
    }

    @Override
    public String toString() {
        if (isNull()) {
            return "invalid plugged stencil: " + getNullReason();
        }
        StringBuffer str = new StringBuffer();
        if (getKey() != null) {
            str.append('(').append(getKey().toString()).append(')');
        }
        str.append(_stencil.toString());
        str.append('<').append(getClass()).append('>');
        str.append('[').append(getContainingSlot()).append(']');
        return str.toString();
    }

    @Override
    public int compareTo(S obj) {
        if (!(obj instanceof _PStencil))
            return 0;
        _PStencil<C, S> stcl = (_PStencil<C, S>) obj;
        if (StencilUtils.isNull(stcl))
            return 0;
        return getKey().compareTo(stcl.getKey());
    }

    /*
     * IPPropStencil<C, S, String> interface
     */

    public String saveAsInstance(C stclContext, String dir, XmlWriter out) {
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        return stcl.saveAsInstance(stclContext, "/", out, self());
    }

    public String getType(C stclContext) {
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        return stcl.getType(stclContext, self());
    }

    public String getValue(C stclContext) {
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        return stcl.getValue(stclContext, self());
    }

    public void setValue(C stclContext, String value) {
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        stcl.setValue(stclContext, value, self());
    }

    public boolean isExpand(C stclContext) {
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        return stcl.isExpand(stclContext, self());
    }

    public void setExpand(C stclContext, boolean expand) {
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        stcl.setExpand(stclContext, expand, self());
    }

    public InputStream getInputStream(C stclContext) {
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        return stcl.getInputStream(stclContext, self());
    }

    public String getExpandedValue(C stclContext) {
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        return stcl.getValue(stclContext, self());
    }

    public String getNotExpandedValue(C stclContext) {
        _Stencil<C, S> stcl = getReleasedStencil(stclContext);
        return stcl.getValue(stclContext, self());
    }

    //
    // LOG PART
    //

    private static final StencilLog LOG = new StencilLog(_PStencil.class);

    protected StencilLog getLog() {
        return LOG;
    }

    public String logWarn(C stclContext, String format, Object... params) {
        return getLog().logWarn(stclContext, format, params);
    }

    public String logError(C stclContext, String format, Object... params) {
        return getLog().logError(stclContext, format, params);
    }

    public String logTrace(C stclContext, String format, Object... params) {
        return getLog().logTrace(stclContext, format, params);
    }
}