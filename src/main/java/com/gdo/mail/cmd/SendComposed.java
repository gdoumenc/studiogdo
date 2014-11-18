/* Copyright GDO - 2005*/

package com.gdo.mail.cmd;

import com.gdo.mail.model.MailStcl;
import com.gdo.project.model.ComposedActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;

public class SendComposed extends ComposedActionStcl {

    public interface Slot extends ComposedActionStcl.Slot {
        String MAIL = "Mail";
    }

    public SendComposed(StclContext stclContext) {
        super(stclContext);
    }

    @Override
    public CommandStatus<StclContext, PStcl> performSteps(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();
        int currentStep = getActiveStepIndex();

        if (currentStep == 2) {
            String mode = getParameter(cmdContext, 1, "simple");

            // replacing content's \n by <br/>
            String content = cmdContext.getTarget().getString(stclContext, MailStcl.Slot.CONTENT, "");
            content = content.replaceAll("\n", "<br/>");
            content = content.replaceAll("\r", "<br/>");
            cmdContext.getTarget().setString(stclContext, MailStcl.Slot.CONTENT, content);

            if ("simple".equals(mode)) {
                return cmdContext.getTarget().call(cmdContext, MailStcl.Command.SEND);
            }
            if ("multi".equals(mode)) {
                return cmdContext.getTarget().call(cmdContext, MailStcl.Command.MULTI_SEND);
            }
            String msg = String.format("Unknown sending mode %s", mode);
            return error(cmdContext, self, msg);
        }

        return success(cmdContext, self);
    }

}