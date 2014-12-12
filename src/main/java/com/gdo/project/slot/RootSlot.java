/**
 * Copyright GDO - 2004
 */
package com.gdo.project.slot;

import com.gdo.stencils.StclContext;
import com.gdo.stencils._Stencil;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.SingleCalculatedSlot;

/**
 * <p>
 * Redefined root slot to get servlet stencil.
 * </p>
 * 
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 * 
 * 
 * @author Guillaume Doumenc (<a>
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */
public class RootSlot extends SingleCalculatedSlot<StclContext, PStcl> {

    public RootSlot(StclContext stclContext, _Stencil<StclContext, PStcl> in, String name) {
        super(stclContext, in, name, PSlot.ONE);
    }

    @Override
    public PStcl getCalculatedStencil(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
        return stclContext.getServletStcl();
    }
}