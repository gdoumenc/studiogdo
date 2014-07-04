/*
 * Copyright GDO - 2004
 */
package com.gdo.project.cmd;

import com.gdo.project.model.ComposedActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;

/**
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo & Guillaume Doumenc. Use
 * is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */
public class Selected extends ComposedActionStcl {

	public static final String SELECTED = "Selected";

	public Selected(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> reset(CommandContext<StclContext, PStcl> context, PStcl self) {
		self.clearSlot(context.getStencilContext(), SELECTED);
		return super.reset(context, self);
	}

	@Override
	public CommandStatus<StclContext, PStcl> performSteps(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		return success(cmdContext, self);
	}
}