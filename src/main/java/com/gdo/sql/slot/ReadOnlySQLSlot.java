package com.gdo.sql.slot;

import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;

public abstract class ReadOnlySQLSlot extends SQLSlot {
    
    public ReadOnlySQLSlot(StclContext stclContext, Stcl in, String name) {
        super(stclContext, in, name, 10);
        readOnly();
        loadAllAtStart();
    }

}
