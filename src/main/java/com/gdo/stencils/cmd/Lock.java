package com.gdo.stencils.cmd;

import com.gdo.project.model.AtomicActionStcl;
import com.gdo.project.model.ProjectStcl.Resource;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PStcl;

public class Lock extends AtomicActionStcl {

    public Lock(StclContext stclContext) {
        super(stclContext);
    }

    @Override
    public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();
        PStcl target = cmdContext.getTarget();
        
        target.clearSlot(stclContext, Stcl.Slot.$LOCKED);
        PStcl user = self.getStencil(stclContext, Resource.USER_CONNECTED);
        target.plug(stclContext, user, Stcl.Slot.$LOCKED);
        
        return success(cmdContext, self);
    }
}
