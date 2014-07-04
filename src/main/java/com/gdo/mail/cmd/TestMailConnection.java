/**
 * Copyright GDO - 2004
 */
package com.gdo.mail.cmd;

import javax.mail.Session;

import com.gdo.mail.model.MailContextStcl;
import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;

/**
 * @author gdo <param0>Title</param0> <param1>From</param1> <param2>To</param2>
 *         <param3>CC</param3> <param4>Content</param4>
 */
public class TestMailConnection extends AtomicActionStcl {

	public TestMailConnection(StclContext stclContext) {
		super(stclContext);
	}

	/**
	 * Method to Test the Connection for the MailContext.
	 */
	@Override
	protected CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		try {
			PStcl mailContext = cmdContext.getTarget();

			// test connection
			MailContextStcl ctxt = (MailContextStcl) mailContext.getReleasedStencil(stclContext);
			Session session = ctxt.connectSMTP(stclContext, mailContext);
			if (session == null) {
				return error(cmdContext, self, "Cannot connect");
			}
			return success(cmdContext, self, "Connexion established");
		} catch (Exception e) {
			String msg = logError(stclContext, "Cannot connect to SMTP server : %s", e);
			return error(cmdContext, self, msg);
		}

	}
}