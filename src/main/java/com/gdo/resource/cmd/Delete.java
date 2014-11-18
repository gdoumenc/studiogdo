/*
 * Copyright GDO - 2004
 */
package com.gdo.resource.cmd;

import com.gdo.project.model.AtomicActionStcl;
import com.gdo.resource.model._ResourceStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.StencilUtils;

public class Delete extends AtomicActionStcl {

    public Delete(StclContext stclContext) {
        super(stclContext);
    }

    @Override
    public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();
        PStcl target = cmdContext.getTarget();
        PStcl file = target.getStencil(stclContext, _ResourceStcl.Slot.FILE);
        if (!StencilUtils.isNull(file)) {
            CommandStatus<StclContext, PStcl> status = file.call(stclContext, _ResourceStcl.Command.DELETE);
            target.unplugFromAllSlots(stclContext);
            return status;
        }
        target.unplugFromAllSlots(stclContext);
        return success(cmdContext, self);
    }

}