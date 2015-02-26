/*
 * Copyright GDO - 2004
 */
package com.gdo.mail.cmd;

import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;

public class ReadFirstTenRecipients extends AtomicActionStcl {

	public ReadFirstTenRecipients(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();

		// get file path
		String filePath = cmdContext.getTarget().getString(stclContext, "FilePath", null);

		String splitedBy = cmdContext.getTarget().getString(stclContext, "SplitedBy", null);
		String startTag = cmdContext.getTarget().getString(stclContext, "StartTag", null);
		String endTag = cmdContext.getTarget().getString(stclContext, "EndTag", null);

		// please do code here to show the 10 emails from file

		return success(cmdContext, self, 0, filePath + "\n" + splitedBy + "\n" + startTag + "\n" + endTag);
	}
}