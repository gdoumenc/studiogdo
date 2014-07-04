/*
 * Copyright GDO - 2004
 */
package com.gdo.ftp.cmd;

import com.gdo.ftp.model.FtpContextStcl;
import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.Result;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;

public class TestConnection extends AtomicActionStcl {

	public TestConnection(StclContext stclContext) {
		super(stclContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdo.stencils.cmd.CommandStencil#doAction(com.gdo.stencils.cmd.
	 * CommandContext, com.gdo.stencils.plug.PStencil)
	 */
	@Override
	public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		PStcl target = cmdContext.getTarget();

		// the target must be a FTP context
		Stcl stcl = target.getReleasedStencil(stclContext);
		if (!(stcl instanceof FtpContextStcl))
			return error(cmdContext, self, "Le contexte n'est pas un context FTP");

		// tests connection
		FtpContextStcl ftpContext = (FtpContextStcl) stcl;
		Result result = ftpContext.connect(stclContext, target);
		if (result.isNotSuccess())
			return error(cmdContext, self, 0, "Connexion refusée", result);

		// closes connection
		Result closed = ftpContext.close(stclContext, self);
		if (closed.isNotSuccess())
			return error(cmdContext, self, closed);

		return success(cmdContext, self, "La connexion a été établie");
	}
}