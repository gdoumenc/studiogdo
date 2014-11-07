/*
 * Copyright GDO - 2004
 */
package com.gdo.project.cmd.eval;

import org.apache.commons.lang3.StringUtils;

import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.SlotUtils;

public class Expunge extends AtomicActionStcl {

    public Expunge(StclContext stclContext) {
        super(stclContext);
    }

    @Override
    public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();
        PStcl target = cmdContext.getTarget();

        // gets slot path
        String path = getParameter(cmdContext, 2, null);
        if (StringUtils.isEmpty(path)) {
            return error(cmdContext, self, "no path defined for Flush command (param2)");
        }

        // gets slot
        PSlot<StclContext, PStcl> slot = target.getSlot(stclContext, path);
        if (SlotUtils.isNull(slot)) {
            String msg = String.format("no slot %s in stencil %s for Flush command", slot, target);
            return error(cmdContext, self, msg);
        }

        // expunge single calculated slot
        slot.getSlot().expunge(stclContext, slot);
        return success(cmdContext, self, true);
    }
}
