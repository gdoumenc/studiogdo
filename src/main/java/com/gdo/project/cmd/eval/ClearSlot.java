/*
 * Copyright GDO - 2004
 */
package com.gdo.project.cmd.eval;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.Base64;

import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;

/**
 * Clears a slot.<br>
 * Parameters are :
 * <ol>
 * <li>'ClearSlot'.</li>
 * <li>the coded path to the slot to be cleared./li>
 * </ol>
 *
 */
public class ClearSlot extends AtomicActionStcl {

    public ClearSlot(StclContext stclContext) {
        super(stclContext);
    }

    @Override
    public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();
        PStcl target = cmdContext.getTarget();

        // first, verifies if slot defined by path contains stencils
        String path = getParameter(cmdContext, 2, null);
        if (StringUtils.isBlank(path)) {
            return error(cmdContext, self, "no slot defined for ClearSlot (param2)");
        }
        Base64 base = new Base64();
        path = new String(base.decode(path.getBytes()));

        target.clearSlot(stclContext, path);

        return success(cmdContext, self, false);
    }

}
