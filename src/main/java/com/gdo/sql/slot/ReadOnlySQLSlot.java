package com.gdo.sql.slot;

import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;

public abstract class ReadOnlySQLSlot extends SQLSlot {

    public ReadOnlySQLSlot(StclContext stclContext, Stcl in, String name) {
        super(stclContext, in, name, 0);
        loadAllAtStart();
    }

    public void reload(StclContext stclContext, PSlot<StclContext, PStcl> self) {
        int size = getStencilsList(stclContext, null, self).size();
        _cursor.size(size);
    }

}
