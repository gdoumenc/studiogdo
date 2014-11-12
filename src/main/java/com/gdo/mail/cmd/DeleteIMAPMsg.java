/*
 * Copyright GDO - 2004
 */
package com.gdo.mail.cmd;

import javax.mail.MessagingException;

import com.gdo.mail.model.IMAPMailStcl;
import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;

public class DeleteIMAPMsg extends AtomicActionStcl {

    public DeleteIMAPMsg(StclContext stclContext) {
        super(stclContext);
    }

    @Override
    public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();
        PStcl target = cmdContext.getTarget();

        try {
            if (target.getReleasedStencil(stclContext) instanceof IMAPMailStcl) {
                ((IMAPMailStcl) target.getReleasedStencil(stclContext)).delete();
                return success(cmdContext, self);
            }
            return error(cmdContext, self, "deleteIMAP");
        } catch (MessagingException e) {
            return error(cmdContext, self, e);
        }
    }

}