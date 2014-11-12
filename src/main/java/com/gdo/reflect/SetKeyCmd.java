/*
 * Copyright GDO - 2004
 */
package com.gdo.reflect;

import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.WrongPathException;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;

public class SetKeyCmd extends AtomicActionStcl {

    public SetKeyCmd(StclContext stclContext) {
        super(stclContext);
    }

    @Override
    public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        try {
            StclContext stclContext = cmdContext.getStencilContext();

            // get slot
            PStcl keyStcl = cmdContext.getTarget();
            PSlot<StclContext, PStcl> slot = ((com.gdo.reflect.KeyStcl) keyStcl.getReleasedStencil(stclContext)).getSlot();
            PStcl stcl = ((com.gdo.reflect.KeyStcl) keyStcl.getReleasedStencil(stclContext)).getStcl();

            // get key
            String key = getExpandedParameter(cmdContext, 1, null, self);
            if (slot.changeKey(stclContext, stcl, key)) {
                return success(cmdContext, self);
            }
            return error(cmdContext, self, "was not able to change key");
        } catch (WrongPathException e) {
            return error(cmdContext, self, 1, e);
        }
    }
}