/*
 * Copyright GDO - 2004
 */
package com.gdo.project.cmd.eval;

import org.apache.commons.lang3.StringUtils;

import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.Keywords;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.cond.PathCondition;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.plug.PStcl;

public class HasValue extends AtomicActionStcl {

	public HasValue(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		PStcl target = cmdContext.getTarget();

		// first, verifies if slot defined by path contains stencils
		String path = getParameter(cmdContext, 2, null);
		if (StringUtils.isBlank(path)) {
			return error(cmdContext, self, "no path defined for command HasVAlue");
		}

		// at least a stencil should be defined
		StencilIterator<StclContext, PStcl> iter = target.getStencils(stclContext, path);
		if (iter.isNotValid() || iter.size() == 0) {
			return success(cmdContext, self, false);
		}

		// if not type just verified not empty
		String type = getParameter(cmdContext, 3, null);
		if (StringUtils.isEmpty(type)) {
			return success(cmdContext, self, true);
		}

		// default test
		String value = getParameter(cmdContext, 4, null);
		if (value == null) {
			if (Keywords.STRING.equals(type))
				return success(cmdContext, self, true);

			// one property have a value > 0
			if (Keywords.INT.equals(type)) {
				for (PStcl stcl : iter) {
					int prop = Integer.parseInt(stcl.getValue(stclContext));
					if (prop > 0)
						return success(cmdContext, self, true);
				}
				return success(cmdContext, self, false);
			}

			// one property have a true value
			if (Keywords.BOOLEAN.equals(type)) {
				for (PStcl stcl : iter) {
					boolean prop = Boolean.parseBoolean(stcl.getValue(stclContext));
					if (prop)
						return success(cmdContext, self, true);
				}
				return success(cmdContext, self, false);
			}
		}

		// only one condition is enough
		String operator = getParameter(cmdContext, 5, "==");
		for (PStcl stcl : iter) {
			String prop = stcl.getValue(stclContext);
			boolean comp = PathCondition.compare(type, prop, value, operator);
			if (comp)
				return success(cmdContext, self, true);
		}

		return success(cmdContext, self, false);
	}

}
