package com.gdo.stencils.cmd;

import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PStcl;

public class Unlock extends AtomicActionStcl {

    public Unlock(StclContext stclContext) {
        super(stclContext);
    }

    @Override
    public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();
        PStcl target = cmdContext.getTarget();

        target.clearSlot(stclContext, Stcl.Slot.$LOCKED_BY);

        return success(cmdContext, self);
    }
}
