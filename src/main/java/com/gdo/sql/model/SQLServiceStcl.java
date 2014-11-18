/**
 * Copyright GDO - 2004
 */
package com.gdo.sql.model;

import com.gdo.project.model.ServiceStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PStcl;

public class SQLServiceStcl extends ServiceStcl {

    public interface Slot extends ServiceStcl.Slot {
        String SQL_CONTEXT = "SqlContext";
    }

    public SQLServiceStcl(StclContext stclContext) {
        super(stclContext);

        singleSlot(Slot.SQL_CONTEXT);
    }

    /**
     * Returns the SQL context used to store inscriptions.
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            this stencil as a plugged stencil.
     * @return the SQL context used to store inscriptions.
     */
    public PStcl getSqlContext(StclContext stclContext, PStcl self) {
        return self.getStencil(stclContext, Slot.SQL_CONTEXT);
    }

}