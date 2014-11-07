/*
 * Copyright GDO - 2004
 */
package com.gdo.project.cmd.eval;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.PathUtils;

public class IsPlugged extends AtomicActionStcl {

    public IsPlugged(StclContext stclContext) {
        super(stclContext);
    }

    @Override
    public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();
        PStcl target = cmdContext.getTarget();
        List<PStcl> slots = target.getStencilOtherPluggedReferences(stclContext);

        // from which slots (if no slot checks if plugged at another place)
        String path = getParameter(cmdContext, 2, null);
        if (StringUtils.isEmpty(path)) {
            return success(cmdContext, self, slots.size() > 1);
        }

        // get operator in case of multi slot
        String oper = getParameter(cmdContext, 3, null);
        if (StringUtils.isEmpty(oper))
            oper = "&";

        // checks if the target is plugged in the slots
        for (String p : PathUtils.splitMultiPath(path)) {
            PSlot<StclContext, PStcl> slot = target.getSlot(stclContext, p);
            if ("&".equals(oper) && !contains(stclContext, slot, target))
                return success(cmdContext, self, false);
            if ("|".equals(oper) && contains(stclContext, slot, target))
                return success(cmdContext, self, true);
        }

        if ("&".equals(oper))
            return success(cmdContext, self, true);
        return success(cmdContext, self, false);
    }

    private boolean contains(StclContext stclContext, PSlot<StclContext, PStcl> slot, PStcl stcl) {
        return slot.contains(stclContext, null, stcl);
    }

}
