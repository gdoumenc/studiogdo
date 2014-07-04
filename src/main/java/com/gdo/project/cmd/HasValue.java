/*
 * Copyright GDO - 2004
 */
package com.gdo.project.cmd;

import org.apache.commons.lang3.StringUtils;

import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.cond.PathCondition;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.plug.PStcl;

@Deprecated
// replaced by Eval/HasValue
public class HasValue extends AtomicActionStcl {

	public HasValue(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		PStcl target = cmdContext.getTarget();

		// first, verifies if slot defined by path contains stencils
		String path = getParameter(cmdContext, 1, null);
		if (StringUtils.isEmpty(path)) {
			return error(cmdContext, self, "no path defined");
		}

		// at least a stencil should be defined
		StencilIterator<StclContext, PStcl> iter = target.getStencils(stclContext, path);
		if (iter.isNotValid() || iter.size() == 0) {
			return success(cmdContext, self, false);
		}

		// if not type just verified not empty
		String type = getParameter(cmdContext, 2, null);
		if (StringUtils.isEmpty(type)) {
			return success(cmdContext, self, true);
		}

		// if no value then only checks not empty (as previous)
		String value = getParameter(cmdContext, 3, null);
		if (value == null) {
			return success(cmdContext, self, true);
		}

		// only one condition is enough
		String operator = getParameter(cmdContext, 4, "==");
		for (PStcl stcl : iter) {
			String prop = stcl.getValue(stclContext);
			boolean comp = PathCondition.compare(type, prop, value, operator);
			if (comp)
				return success(cmdContext, self, true);
		}

		return success(cmdContext, self, false);
	}

}
