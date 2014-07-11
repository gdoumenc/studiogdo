package com.gdo.sql.slot;

import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;

public interface SQLSlotFilter {

    default public String addFilter(StclContext stclContext, String c, PSlot<StclContext, PStcl> self) {
        return c;
    }
}
