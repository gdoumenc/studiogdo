/**
 * Copyright GDO - 2005
 */
package com.gdo.project.adaptor;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug._PStencil;

/**
 * <p>
 * Interface of a stencil which emulates a slot.
 * </p>
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo & Guillaume Doumenc. Use
 * is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a>
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */
public interface ISlotEmulator<C extends _StencilContext, S extends _PStencil<C, S>> {

    public int size(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> slot, S self);

    public boolean hasStencils(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> slot, S self);

    public StencilIterator<C, S> getStencils(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> slot, S self);

    public boolean contains(C stclContext, StencilCondition<C, S> cond, S searched, PSlot<C, S> slot, S self);

    public boolean canChangeOrder(C stclContext, PSlot<C, S> slot, S self);

    public boolean isFirst(C stclContext, S searched, PSlot<C, S> slot, S self);

    public boolean isLast(C stclContext, S searched, PSlot<C, S> slot, S self);

}
