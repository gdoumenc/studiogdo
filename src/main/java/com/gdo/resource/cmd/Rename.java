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

public class Rename extends AtomicActionStcl {

    public Rename(StclContext stclContext) {
        super(stclContext);
    }

    @Override
    public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();
        PStcl target = cmdContext.getTarget();

        // rename the associated file
        PStcl file = target.getStencil(stclContext, _ResourceStcl.Slot.FILE);
        if (StencilUtils.isNull(file))
            return error(cmdContext, self, 0, "no file");
        return file.call(cmdContext, _ResourceStcl.Command.RENAME);
    }

}