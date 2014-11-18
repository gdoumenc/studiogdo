/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils.slot;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug._PStencil;

/**
 * <p>
 * Extends the slot interface with setter/getter for property value.
 * </p>
 */
public interface IPropertySlot<C extends _StencilContext, S extends _PStencil<C, S>> {

    String getValue(C stclContext, PSlot<C, S> self);

    void setValue(C stclContext, String value, PSlot<C, S> self);

}
