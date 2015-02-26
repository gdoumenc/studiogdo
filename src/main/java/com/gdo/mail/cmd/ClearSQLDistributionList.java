/*
 * Copyright GDO - 2004
 */
package com.gdo.mail.cmd;

import com.gdo.mail.model.SQLDistributionListStcl;
import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;

public class ClearSQLDistributionList extends AtomicActionStcl {

	public ClearSQLDistributionList(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		PStcl list = cmdContext.getTarget();

		// removes all from to
		list.clearSlot(stclContext, SQLDistributionListStcl.Slot.TO);

		// all ok
		return success(cmdContext, self);
	}

}