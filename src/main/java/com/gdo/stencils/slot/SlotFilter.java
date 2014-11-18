package com.gdo.stencils.slot;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.plug._PStencil;

/**
 * <p>
 * This interface is used to filter a slot.
 * </p>
 * <p>
 * Should return <tt>true</tt> to keep the stencil in the slot when filtering.
 * </p>
 */
public interface SlotFilter<C extends _StencilContext, S extends _PStencil<C, S>> {

    // funtion to be implemented to keep the stencil from the filter
    boolean keep(C stclContext, S plugged);

}