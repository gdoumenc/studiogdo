/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.slot;

import com.gdo.stencils.Result;
import com.gdo.stencils._Stencil;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.util.StencilUtils;

/**
 * <p>
 * An empty slot is a slot with no stencil in it and in which no stencil can be
 * plugged.
 * </p>

 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>

 */
public class EmptySlot<C extends _StencilContext, S extends _PStencil<C, S>> extends _Slot<C, S> {

    public EmptySlot(C stclContext, _Stencil<C, S> in, String name) {
        super(stclContext, in, name, PSlot.ANY, true);
    }

    @Override
    public boolean contains(C stclContext, StencilCondition<C, S> cond, S searched, PSlot<C, S> self) {
        return false;
    }

    @Override
    public boolean hasStencils(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
        return false;
    }

    @Override
    public int size(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
        return 0;
    }

    @Override
    public StencilIterator<C, S> getStencils(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
        return StencilUtils.<C, S> iterator();
    }

    @Override
    public S getStencil(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
        return StencilUtils.<C, S> nullPStencil(stclContext, Result.error("No stencil in empty slot"));
    }

    @Override
    public boolean hasAdaptorStencil(C stclContext, PSlot<C, S> self) {
        return false;
    }

    @Override
    public S getAdaptorStencil(C stclContext, PSlot<C, S> self) {
        return StencilUtils.<C, S> nullPStencil(stclContext, Result.error("No stencil in empty slot"));
    }

    @Override
    public boolean changeKey(C stclContext, S searched, String key, PSlot<C, S> self) {
        return false;
    }

    @Override
    public boolean canChangeOrder(C stclContext, PSlot<C, S> self) {
        return false;
    }

    @Override
    public boolean isFirst(C stclContext, S searched, PSlot<C, S> self) {
        return false;
    }

    @Override
    public boolean isLast(C stclContext, S searched, PSlot<C, S> self) {
        return false;
    }

    @Override
    protected S doPlug(C stclContext, S stencil, IKey key, PSlot<C, S> self) {
        return StencilUtils.<C, S> nullPStencil(stclContext, Result.error("Cannot plug in an empty slot"));
    }

    @Override
    protected void doUnplug(C stclContext, S stencil, IKey key, PSlot<C, S> self) {
    }

    @Override
    protected void doUnplugAll(C stclContext, PSlot<C, S> self) {
    }

    @Override
    protected StencilIterator<C, S> getStencilsToSave(C stclContext, PSlot<C, S> self) {
        return StencilUtils.<C, S> iterator();
    }

}