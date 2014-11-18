package com.gdo.sql.slot;

import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;

public interface SQLSlotFilter {

    /**
     * Add filter on condition. Default : does nothing on condition.
     * 
     * @param stclContext
     *            : The stencil context.
     * @param cond
     *            : The initial condition.
     * @param self
     *            : The slot as plugged slot.
     * @return The new filtered condition.
     */
    default public String addFilter(StclContext stclContext, String cond, PSlot<StclContext, PStcl> self) {
        return cond;
    }
}
