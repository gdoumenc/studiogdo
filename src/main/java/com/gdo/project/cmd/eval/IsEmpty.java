/*
 * Copyright GDO - 2004
 */
package com.gdo.project.cmd.eval;

import org.apache.commons.lang3.StringUtils;

import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.PathUtils;

public class IsEmpty extends AtomicActionStcl {

	public IsEmpty(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		PStcl target = cmdContext.getTarget();

		// first, verifies if slot defined by path contains stencils
		String path = getParameter(cmdContext, 2, null);
		if (StringUtils.isEmpty(path)) {
			return error(cmdContext, self, "no path defined");
		}

		// at least a stencil should be defined
        String parent_path = PathUtils.getPathName(path);
        String slot_path = PathUtils.getLastName(path);
		StencilIterator<StclContext, PStcl> iter = target.getStencils(stclContext, parent_path);
		if (iter.isNotValid() || iter.size() == 0) {
			return success(cmdContext, self, false);
		}

		// all properties should be empty
		for (PStcl stcl : iter) {
			String prop = stcl.getString(stclContext, slot_path);
			if (StringUtils.isNotEmpty(prop)) {
				return success(cmdContext, self, false);
			}
		}
		return success(cmdContext, self, true);
	}

}
