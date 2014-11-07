/**
 * Copyright GDO - 2005
 * Date             Author      Changes     Status
 * 28/Aug/2008      Perminder   Created     Running
 */
package com.gdo.sql.cmd;

import org.apache.commons.lang3.StringUtils;

import com.gdo.project.model.AtomicActionStcl;
import com.gdo.sql.model.SQLContextStcl;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;

public class UpdateQuery extends AtomicActionStcl {

    public UpdateQuery(StclContext stclContext) {
        super(stclContext);
    }

    @Override
    protected CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();

        // gets the query from param1
        String query = getParameter(cmdContext, 1, "");
        if (StringUtils.isEmpty(query)) {
            return error(cmdContext, self, "empty query (param1) in command UpdateQuery");
        }

        // does the updatation
        PStcl sqlContext = cmdContext.getTarget();
        if (!(sqlContext.getReleasedStencil(stclContext) instanceof SQLContextStcl)) {
            String msg = String.format("Context is not a SqlContext stencil : %s", sqlContext);
            return error(cmdContext, self, msg);
        }
        SQLContextStcl ctxt = (SQLContextStcl) sqlContext.getReleasedStencil(stclContext);
        Result result = ctxt.updateQuery(stclContext, query, sqlContext);
        return success(cmdContext, self, result);
    }
}