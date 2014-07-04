/**
 * Copyright GDO - 2004
 */
package com.gdo.mail.cmd;

import com.gdo.mail.model.MailStcl;
import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;

public class MultiSendMail extends AtomicActionStcl {

	public MultiSendMail(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();

		// get message and set global parameters (target may never be null)
		PStcl mail = self.getStencil(stclContext, Slot.TARGET);
		MailStcl m = (MailStcl) mail.getReleasedStencil(stclContext);

		// send and return status
		Result result = m.multiSend(cmdContext, cmdContext, mail);
		return success(cmdContext, self, result);
	}
}