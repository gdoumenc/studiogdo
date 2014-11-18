package com.gdo.sql.cmd;

import com.gdo.project.model.AtomicActionStcl;
import com.gdo.sql.model.SQLStcl;
import com.gdo.sql.slot.SQLSlot;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils._Stencil;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.StencilUtils;

public class UpdateSQLStcl extends AtomicActionStcl {

    public UpdateSQLStcl(StclContext stclContext) {
        super(stclContext);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gdo.stencils.cmd.CommandStencil#doAction(com.gdo.stencils.cmd.
     * CommandContext, com.gdo.stencils.plug.PStencil)
     */
    @Override
    protected CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();
        PStcl pstcl = cmdContext.getTarget();

        // checks the stencil is a SQL stencil
        _Stencil<StclContext, PStcl> stcl = pstcl.getReleasedStencil(stclContext);
        if (!(stcl instanceof SQLStcl)) {
            String msg = logWarn(stclContext, "command UpdateSQLStcl can be called only on SQLStcl not on %s", pstcl);
            return error(cmdContext, self, msg);
        }
        SQLStcl sqlStcl = (SQLStcl) stcl;

        // checks the stencil is in a SQL slot then does nothing
        PSlot<StclContext, PStcl> pslot = sqlStcl.getSQLContainerSlot();
        if (StencilUtils.isNull(pslot) || !(pslot.getSlot() instanceof SQLSlot)) {
            return success(cmdContext, self);
        }

        // does the updatation on database
        Result result = sqlStcl.update(stclContext, pstcl);
        return success(cmdContext, self, result);
    }
}