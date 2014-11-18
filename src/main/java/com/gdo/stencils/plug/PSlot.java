package com.gdo.stencils.plug;

import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.log.StencilLog;
import com.gdo.stencils.slot._Slot;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.SlotUtils;
import com.gdo.stencils.util.StencilUtils;

/**
 * <p>
 * Basic implementation of {@link com.gdo.stencils.plug.PSlot}
 * </p>

 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>

 */
public class PSlot<C extends _StencilContext, S extends _PStencil<C, S>> {

    private _Slot<C, S> _slot; // the slot plugged
    private S _container; // the container stencil
    protected Result _result; // when null, the reason why

    /**
     * The arity <tt>ONE</tt> defines a slot where one stencil must allways been
     * plugged in the slot.
     */
    public static final char ONE = '1';

    /**
     * The arity <tt>NONE_OR_ONE</tt> defines a slot where one or none stencil
     * may be plugged in the slot.
     */
    public static final char NONE_OR_ONE = '?';

    /**
     * The arity <tt>ANY</tt> defines a slot where several or none stencil may
     * be plugged in the slot.
     */
    public static final char ANY = '*';

    /**
     * The arity <tt>AT_LEAST_ONE</tt> defines a slot where several but at least
     * one stencil may be plugged in the slot.
     */
    public static final char AT_LEAST_ONE = '+';

    /**
     * The arity <tt>HIDDEN</tt> removes a slot in extension.
     */
    public static final char HIDDEN = '-';

    /* used as returned error */
    public static final char UNDEFINED = ' ';

    /**
     * Creates a new plugged slot.
     * 
     * @param slot
     *            the slot plugged.
     * @param container
     *            the container stencil.
     */
    public PSlot(_Slot<C, S> slot, S container) {
        _slot = slot;
        _container = container;
    }

    /**
     * Error constructor used when the slot is not available.
     * 
     * @param result
     *            the reason for unavailability.
     */
    public PSlot(Result result) {
        _slot = null;
        _container = null;

        // add error status
        if (result != null && result.isSuccess() && getLog().isWarnEnabled()) {
            String msg = "should not create a null slot on success result";
            getLog().warn(StclContext.defaultContext(), msg);
        }
        _result = (result != null) ? result : Result.error("empty slot without any reason");
    }

    /**
     * Clears all internal structures to free memory.
     */
    public void clear() {
        if (_slot != null)
            _slot.clear();
    }

    /**
     * Checks if the plugged slot is null.
     * 
     * @return <tt>true</tt> if the slot is null, <tt>false</tt> otherwise.
     */
    public boolean isNull() {
        return _slot == null;
    }

    /**
     * Checks if the plugged slot is not null.
     * 
     * @return <tt>true</tt> if the slot is not null, <tt>false</tt> otherwise.
     */
    public final boolean isNotNull() {
        return !isNull();
    }

    /**
     * Returns the reason why the slot is null.
     * 
     * @return the reason why the slot is null (never empty).
     */
    public final Result getResult() {
        return _result;
    }

    /**
     * Adds another reason why the slot is null.
     * 
     * @return the reason why the slot is null (never empty).
     */
    public final Result addResult(Result result) {
        if (isNotNull()) {
            _slot = null;
            _container = null;

            // add error status
            if (result != null && result.isSuccess() && getLog().isWarnEnabled()) {
                String msg = "should not add a success reason on slot";
                getLog().warn(StclContext.defaultContext(), msg);
            }
            _result = (result != null) ? result : Result.error("empty slot without any reason");
        } else {
            if (result != null)
                _result.addOther(result);
        }
        return _result;
    }

    /**
     * Returns the reason why the slot is null.
     * 
     * @return the reason why, if there is one, this slot is null.
     */
    public final String getNullReason() {
        String msg = getResult().getMessage(); // get result is never null
        return (msg != null) ? msg : "empty stencil without any reason";
    }

    /**
     * Sets the slot structure.
     */
    public <K extends _Slot<C, S>> void setSlot(K slot) {
        _slot = slot;
    }

    /**
     * Returns the slot structure.
     * 
     * @return the slot structure (should be used only to call java class
     *         methods).
     */
    @SuppressWarnings("unchecked")
    public <K extends _Slot<C, S>> K getSlot() {
        return (K) _slot;
    }

    /**
     * Returns the plugged stencil container ot the slot.
     * 
     * @return the plugged stencil container ot the slot.
     */
    public S getContainer() {
        return _container;
    }

    public final S plug(C stclContext, S stencil, IKey key) {
        if (_slot != null)
            return _slot.plug(stclContext, stencil, key, this);
        return StencilUtils.<C, S> nullPStencil(stclContext, Result.error("cannot plug in an empty slot"));
    }

    public final void unplug(C stclContext, S stencil, IKey key) {
        if (_slot != null)
            _slot.unplug(stclContext, stencil, key, this);
    }

    public final void unplugAll(C stclContext) {
        if (_slot != null)
            _slot.unplugAll(stclContext, this);
    }

    public String getName(C stclContext) {
        if (_slot == null) {
            return getNullReason();
        }
        return _slot.getName(stclContext);
    }

    public char getArity(C stclContext) {
        if (_slot == null) {
            return PSlot.UNDEFINED;
        }
        return _slot.getArity(stclContext, this);
    }

    public boolean isCursorBased(C stclContext) {
        if (_slot == null) {
            return false;
        }
        return _slot.isCursorBased(stclContext);
    }

    // gets the property defined in the slot
    public String getProperty(C stclContext, IKey key, String name, PSlot<C, S> self) {
        if (_slot == null) {
            return null;
        }
        return _slot.getProperty(stclContext, key, name, self);
    }

    public int size(C stclContext, StencilCondition<C, S> cond) {
        if (_slot == null) {
            return -1;
        }
        return _slot.size(stclContext, cond, this);
    }

    public boolean hasStencils(C stclContext, StencilCondition<C, S> cond) {
        if (_slot == null) {
            return false;
        }
        return _slot.hasStencils(stclContext, cond, this);
    }

    public S getStencil(C stclContext, StencilCondition<C, S> cond) {
        if (_slot == null) {
            return StencilUtils.<C, S> nullPStencil(stclContext, getResult());
        }
        return _slot.getStencil(stclContext, cond, this);
    }

    public StencilIterator<C, S> getStencils(C stclContext, StencilCondition<C, S> cond) {
        if (_slot == null) {
            return StencilUtils.<C, S> iterator(getResult());
        }
        return _slot.getStencils(stclContext, cond, this);
    }

    /**
     * @return <tt>true</tt> if the stencil is plugged in the slot and verify
     *         the consition.
     */
    public boolean contains(C stclContext, StencilCondition<C, S> cond, S searched) {
        if (_slot == null) {
            return false;
        }
        return _slot.contains(stclContext, cond, searched, this);
    }

    public String pwd(C stclContext) {
        try {
            if (StencilUtils.isNull(getContainer()))
                return PathUtils.ROOT;
            String pwd = getContainer().pwd(stclContext);
            StringBuffer str = new StringBuffer(pwd);
            if (!pwd.endsWith(PathUtils.SEP_STR))
                str.append(PathUtils.SEP);
            str.append(getName(stclContext));
            return str.toString();
        } catch (Exception e) {
            return getName(null);
        }
    }

    public boolean changeKey(C stclContext, S searched, String key) {
        return _slot.changeKey(stclContext, searched, key, this);
    }

    public boolean canChangeOrder(C stclContext) {
        return _slot.canChangeOrder(stclContext, this);
    }

    public boolean isFirst(C stclContext, S searched) {
        return _slot.isFirst(stclContext, searched, this);
    }

    public boolean isLast(C stclContext, S searched) {
        return _slot.isLast(stclContext, searched, this);
    }

    /**
     * May be redefined in sub class implementation.
     */
    protected boolean isValid() {
        return (!StencilUtils.isNull(getContainer()) && !SlotUtils.isNull(getSlot()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        _Slot<C, ?> slot = (_Slot<C, ?>) getSlot();
        if (obj == null) {
            return slot == null;
        }
        if (obj instanceof _Slot) {
            return slot == (_Slot<C, ?>) obj;
        }
        if (obj instanceof PSlot) {
            return slot == ((PSlot<C, ?>) obj).getSlot();
        }
        return false;
    }

    //
    // LOG PART
    //

    private static final StencilLog LOG = new StencilLog(PSlot.class);

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

    @SuppressWarnings("unchecked")
    @Override
    public String toString() {
        S container = getContainer();
        if (StencilUtils.isNull(container)) {
            return getSlot().toString();
        }
        C stclContext = (C) StclContext.defaultContext();
        String pwd = container.pwd(stclContext);
        return PathUtils.compose(pwd, getSlot().toString());
    }

}