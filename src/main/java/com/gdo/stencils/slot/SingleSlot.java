/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.slot;

import com.gdo.project.adaptor.ISlotEmulator;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils._Stencil;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.factory.StencilFactory;
import com.gdo.stencils.interpreted.DefaultDescriptor;
import com.gdo.stencils.iterator.EmptyIterator;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.util.SlotUtils;
import com.gdo.stencils.util.StencilUtils;

/**
 * <p>
 * A single slot is a slot in which none or one stencil may be plugged in.
 * </p>
 * <p>
 * When the arity of the slot is one (@see PSlot#ONE), a default stencil
 * constructor may be defined to construct the default stencil plugged into the
 * slot.
 * </p>
 */
public class SingleSlot<C extends _StencilContext, S extends _PStencil<C, S>> extends _Slot<C, S> {

    protected S _containedStcl; // contained stencil
    protected String _defaultValue; // used to construct the property if the
                                    // arity
                                    // is ONE (default property value)

    public SingleSlot(C stclContext, _Stencil<C, S> in, String name, char arity, boolean tranzient) {
        super(stclContext, in, name, arity, tranzient);

        // check parameters
        if (!SlotUtils.isSingle(arity)) {
            logWarn(stclContext, "Single slot %s created with strange arity %s", name, Character.toString(arity));
        }
    }

    public SingleSlot(C stclContext, _Stencil<C, S> in, String name, char arity) {
        this(stclContext, in, name, arity, false);
    }

    public SingleSlot(C stclContext, _Stencil<C, S> in, String name) {
        this(stclContext, in, name, PSlot.NONE_OR_ONE, false);
    }

    @Override
    public boolean contains(C stclContext, StencilCondition<C, S> cond, S searched, PSlot<C, S> self) {
        S contained = getStencil(stclContext, cond, self);
        return searched.equals(contained);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean hasStencils(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {

        // check parameters
        S contained = getContainedStencilOrCreateDefault(stclContext, self);
        if (StencilUtils.isNull(contained))
            return false;

        // in case of slot emulator, propagate to it
        if (contained.isLink(stclContext)) {
            _Stencil<C, S> s = contained.getReleasedStencil(stclContext);
            return ((ISlotEmulator<C, S>) s).hasStencils(stclContext, cond, self, contained);
        }

        // check condition
        return (cond == null || cond.verify(stclContext, contained));
    }

    @Override
    public int size(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
        return hasStencils(stclContext, cond, self) ? 1 : 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public S getStencil(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {

        // get stencil contained (may be null)
        S contained = getContainedStencilOrCreateDefault(stclContext, self);
        if (StencilUtils.isNull(contained))
            return self.getContainer().nullPStencil(stclContext, StencilUtils.getResult(contained));

        // in case of slot emulator, propagate to it
        if (contained.isLink(stclContext)) {
            _Stencil<C, S> cont = contained.getReleasedStencil(stclContext);
            StencilIterator<C, S> conts = ((ISlotEmulator<C, S>) cont).getStencils(stclContext, cond, self, contained);
            if (conts.hasNext())
                return conts.next();
            return self.getContainer().nullPStencil(stclContext);
        }

        // return stencil if conditition verified
        if (cond == null || cond.verify(stclContext, contained))
            return contained;
        return self.getContainer().nullPStencil(stclContext);
    }

    /* (non-Javadoc)
     * @see com.gdo.stencils.slot._Slot#getStencils(com.gdo.stencils.StencilContext, com.gdo.stencils.cond.StencilCondition, com.gdo.stencils.plug.PSlot)
     * 
     * Cannot use getStencil as may contain links.
     */
    @SuppressWarnings("unchecked")
    @Override
    public StencilIterator<C, S> getStencils(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {

        // get stencil contained (may be null)
        S contained = getContainedStencilOrCreateDefault(stclContext, self);
        if (StencilUtils.isNull(contained))
            return StencilUtils.<C, S> iterator(StencilUtils.getResult(contained));

        // nothing found
        if (contained.isNull())
            return new EmptyIterator<C, S>();

        // in case of slot emulator, propagate to it
        if (contained.isLink(stclContext)) {
            _Stencil<C, S> cont = contained.getReleasedStencil(stclContext);
            StencilIterator<C, S> conts = ((ISlotEmulator<C, S>) cont).getStencils(stclContext, cond, self, contained);
            return conts;
        }

        // return stencil if conditition verified
        if (cond == null || cond.verify(stclContext, contained))
            return StencilUtils.<C, S> iterator(stclContext, contained, self);
        return StencilUtils.<C, S> iterator();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.gdo.stencils.slot._Slot#beforePlug(com.gdo.stencils.StencilContext,
     * com.gdo.stencils.plug.PlugOrder, com.gdo.stencils.plug.PSlot)
     */
    @Override
    public Result beforePlug(C stclContext, S stencil, IKey key, PSlot<C, S> self) {

        // does super
        Result result = super.beforePlug(stclContext, stencil, key, self);
        if (result.isNotSuccess())
            return result;

        // if key defined, removes it and adds warning
        if (key.isNotEmpty()) {
            logWarn(stclContext, "a key '%s' is not needed for a single slot '%s'", key, self);
        }
        return result;
    }

    @Override
    public StencilIterator<C, S> getStencilsToSave(C stclContext, PSlot<C, S> self) {

        // gets real contained stencil (do not propagate to stencil contained in
        // slot emulation)
        S contained = getContainedStencilOrCreateDefault(stclContext, self);
        return StencilUtils.<C, S> iterator(stclContext, contained, self);
    }

    @Override
    protected S doPlug(C stclContext, S stencil, IKey key, PSlot<C, S> self) {

        // checks stencil to plug
        if (StencilUtils.isNull(stencil)) {
            String msg = String.format("no stencil to plug on %s for single slot %s", stencil, self);
            return self.getContainer().nullPStencil(stclContext, Result.error(msg));
        }

        // removes previous content
        if (StencilUtils.isNotNull(_containedStcl)) {
            S previous = _containedStcl;

            // does nothing if the stencil is already plugged
            if (StencilUtils.equals(previous, stencil))
                return stencil;

            // removes previous slot content
            unplug(stclContext, previous, previous.getKey(), self);
        }

        // creates the new plugged stencil
        setContainedStencil(stclContext, stencil, self);
        return _containedStcl;
    }

    @Override
    public Result beforeUnplug(C stclContext, S stencil, IKey key, PSlot<C, S> self) {

        if (StencilUtils.isNull(stencil))
            return Result.warn("SingleSlot.beforeUnplug", "no stencil to unplug");

        if (StencilUtils.isNotNull(_containedStcl) && !StencilUtils.equals(_containedStcl, stencil))
            logWarn(stclContext, "Wrong stencil %s for unplugging in %s (present %s)", stencil, this, _containedStcl);

        return super.beforeUnplug(stclContext, stencil, key, self);
    }

    @Override
    protected void doUnplug(C stclContext, S stcl, IKey key, PSlot<C, S> self) {

        // do nothing if no stencil already plugged
        if (StencilUtils.isNull(_containedStcl))
            return;

        // unplug the contained stencil
        _containedStcl = null;
    }

    @Override
    protected void doUnplugAll(C stclContext, PSlot<C, S> self) {
        doUnplug(stclContext, _containedStcl, Key.NO_KEY, self);
    }

    @Override
    public boolean hasAdaptorStencil(C stclContext, PSlot<C, S> self) {
        S contained = getContainedStencilOrCreateDefault(stclContext, self);
        if (StencilUtils.isNull(contained))
            return false;

        return (contained.isLink(stclContext));
    }

    @Override
    public S getAdaptorStencil(C stclContext, PSlot<C, S> self) {
        S contained = getContainedStencilOrCreateDefault(stclContext, self);
        if (StencilUtils.isNull(contained))
            return null;

        return (contained.isLink(stclContext)) ? contained : null;
    }

    @Override
    public boolean changeKey(C stclContext, S searched, String key, PSlot<C, S> self) {
        return false;
    }

    @Override
    public boolean canChangeOrder(C stclContext, PSlot<C, S> self) {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isFirst(C stclContext, S searched, PSlot<C, S> self) {
        if (StencilUtils.isNull(searched))
            return false;
        S contained = getContainedStencilOrCreateDefault(stclContext, self);
        if (StencilUtils.isNull(contained))
            return false;

        if (contained.isLink(stclContext)) {
            return ((ISlotEmulator<C, S>) contained.getReleasedStencil(stclContext)).isFirst(stclContext, searched, self, contained);
        }
        return searched.equals(contained);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isLast(C stclContext, S searched, PSlot<C, S> self) {
        if (StencilUtils.isNull(searched))
            return false;
        S contained = getContainedStencilOrCreateDefault(stclContext, self);
        if (StencilUtils.isNull(contained))
            return false;

        if (contained.isLink(stclContext)) {
            return ((ISlotEmulator<C, S>) contained.getReleasedStencil(stclContext)).isLast(stclContext, searched, self, contained);
        }
        return searched.equals(contained);
    }

    /**
     * Sets the unique contained stencil.
     * 
     * @param stclContext
     *            the stencil context.
     * @param plugged
     *            the plugged stencil.
     * @param self
     *            this slot as a plugged slot
     */
    public void setContainedStencil(C stclContext, S plugged, PSlot<C, S> self) {
        StencilFactory<C, S> factory = (StencilFactory<C, S>) stclContext.<C, S> getStencilFactory();
        _containedStcl = factory.createPStencil(stclContext, self, Key.NO_KEY, plugged);
    }

    // try to create the stencil plugged by default descriptor
    protected S getContainedStencilOrCreateDefault(C stclContext, PSlot<C, S> self) {

        // creates it only if doesn't already exist
        if (StencilUtils.isNotNull(_containedStcl)) {

            if (_containedStcl instanceof PStcl) {
                PStcl pstcl = (PStcl) _containedStcl;
                if (pstcl.isCursorBased()) {
                    pstcl.updateCursor((StclContext) stclContext);
                }
            }
            if (_containedStcl != null)
                _containedStcl.setContainingSlot(self);

            return _containedStcl;
        }

        // creates it only arity is set to one
        if (getArity(stclContext) != PSlot.ONE) {
            String msg = String.format("empty single slot %s", self);
            return StencilUtils.<C, S> nullPStencil(stclContext, Result.error(msg));
        }

        // uses default descriptor for creation
        if (getDescriptor() != null && getDescriptor().getDefault() != null) {
            DefaultDescriptor<C, S> def = getDescriptor().getDefault();
            try {

                // creates it
                S stcl = def.newInstance(stclContext, self);
                if (StencilUtils.isNull(stcl)) {
                    logError(stclContext, "cannot create default instance %s in %s : %s", def, this, StencilUtils.getNullReason(stcl));
                    return stcl;
                }

                // plugs it in the slot
                return plug(stclContext, stcl, stcl.getKey(), self);
            } catch (Exception e) {
                String msg = logError(stclContext, "exception when creating default instance %s in %s", def, this);
                return StencilUtils.<C, S> nullPStencil(stclContext, Result.error(msg));
            }
        }

        // use default value
        if (_defaultValue != null) {
            try {
                StencilFactory<C, S> factory = (StencilFactory<C, S>) stclContext.<C, S> getStencilFactory();
                if (factory == null) {
                    String msg = String.format("no default factory to create default property value %s in %s", _defaultValue, self);
                    if (getLog().isErrorEnabled())
                        getLog().error(stclContext, msg);
                    return StencilUtils.<C, S> nullPStencil(stclContext, Result.error(msg));
                }
                S prop = factory.createPProperty(stclContext, self, Key.NO_KEY, _defaultValue);
                if (StencilUtils.isNull((S) prop)) {
                    String msg = String.format("cannot create default property value %s in %s", _defaultValue, self);
                    if (getLog().isErrorEnabled())
                        getLog().error(stclContext, msg);
                    return StencilUtils.<C, S> nullPStencil(stclContext, Result.error(msg));
                }
                return prop;
            } catch (Exception e) {
                String msg = logError(stclContext, "exception when creating default value %s in %s", _defaultValue, self);
                return StencilUtils.<C, S> nullPStencil(stclContext, Result.error(msg));
            }
        }

        String msg = String.format("Cannot create default stencil as no default descriptor and no default value for %s", self);
        return StencilUtils.<C, S> nullPStencil(stclContext, Result.error(msg));
    }
}