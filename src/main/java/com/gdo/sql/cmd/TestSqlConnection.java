/**
 * Copyright GDO - 2005
 * Date             Author      Changes     Status
 * 28/Aug/2008      Perminder   Created     Running
 */
package com.gdo.sql.cmd;

import com.gdo.project.model.AtomicActionStcl;
import com.gdo.sql.model.SQLContextStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;

public class TestSqlConnection extends AtomicActionStcl {

    public TestSqlConnection(StclContext stclContext) {
        super(stclContext);
    }

    /**
     * Method to Test the Connection for the SqlContext.
     */
    @Override
    protected CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();
        try {
            PStcl sqlContext = cmdContext.getTarget();

            // test connection
            SQLContextStcl ctxt = (SQLContextStcl) sqlContext.getReleasedStencil(stclContext);
            return success(cmdContext, self, ctxt.connect(stclContext, sqlContext));
        } catch (Exception e) {
            String msg = logError(stclContext, "TestSqlConnection : Cannot connect to database : %s", e);
            return error(cmdContext, self, msg);
        }

    }

}