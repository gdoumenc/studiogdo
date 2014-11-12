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
import com.gdo.stencils.slot.MultiSlot;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.SlotUtils;

public class MultiUnplug extends AtomicActionStcl {

    public MultiUnplug(StclContext stclContext) {
        super(stclContext);
    }

    @Override
    public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();
        PStcl target = cmdContext.getTarget();

        // first, verifies if slot defined by path contains stencils
        String path = getParameter(cmdContext, 2, null);
        if (StringUtils.isBlank(path)) {
            return error(cmdContext, self, "no slot defined for MultiUnplug (param2)");
        }
        PSlot<StclContext, PStcl> slot = target.getSlot(stclContext, PathUtils.getSlotPath(path));
        if (slot.isNull()) {
            String msg = String.format("Slot %s not defined in %s for MultiUnplug (param2)", path, target);
            return error(cmdContext, self, msg);
        }

        // get keys (multi separated string)
        String keys = getParameter(cmdContext, 3, null);
        if (StringUtils.isBlank(keys)) {
            return error(cmdContext, self, "no keys defined for MultiUnplug (param3)");
        }

        if (SlotUtils.isMultiple(stclContext, slot)) {
            ((MultiSlot<StclContext, PStcl>) slot.getSlot()).doMultiUnplug(stclContext, keys, slot);
        }

        return success(cmdContext, self, false);
    }

}
