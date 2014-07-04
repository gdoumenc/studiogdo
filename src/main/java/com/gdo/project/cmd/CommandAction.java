/*
 * Copyright GDO - 2004
 */
package com.gdo.project.cmd;

import com.gdo.project.model.AtomicActionStcl;
import com.gdo.project.model.ComposedActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;

public class CommandAction extends AtomicActionStcl {

	private static final int INCREMENT = 0;
	private static final int RESET = 1;
	private static final int CANCEL = 2;

	public CommandAction(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		PStcl action = cmdContext.getTarget();
		ComposedActionStcl composed = (ComposedActionStcl) action.getReleasedStencil(cmdContext.getStencilContext());

		// gets command type
		Integer type = getParameter(cmdContext, 1, 0);
		switch (type.intValue()) {
		case RESET:
			return composed.reset(cmdContext, action);
		case INCREMENT:
			break;
		case CANCEL:
			return composed.cancel(cmdContext, action);
		default:
			String msg = String.format("Undefined command action type %s (param1)", type);
			return error(cmdContext, self, msg);
		}

		// performs step increment
		int increment = getParameter(cmdContext, 2, 0);
		return composed.incrementStep(cmdContext, increment, action);
	}
}