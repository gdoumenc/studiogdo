package com.gdo.website.cmd;

import com.gdo.project.model.AtomicActionStcl;
import com.gdo.site.model.SimplePageStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;
import com.gdo.website.model.SQLSimplePageStcl;

public class UpdateSQLPage extends AtomicActionStcl {

	public UpdateSQLPage(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	protected CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		PStcl target = cmdContext.getTarget();

		PStcl page = target.getStencil(stclContext, SQLSimplePageStcl.Slot.PAGE);

		return page.call(stclContext, SimplePageStcl.Command.UPDATE);
	}

}
