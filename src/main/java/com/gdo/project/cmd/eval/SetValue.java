/*
 * Copyright GDO - 2004
 */
package com.gdo.project.cmd.eval;

import org.apache.commons.lang3.StringUtils;

import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.WrongPathException;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;

public class SetValue extends AtomicActionStcl {

	public SetValue(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {

		String path = getParameter(cmdContext, 2, null);
		if (StringUtils.isEmpty(path)) {
			return error(cmdContext, self, "no path defined for SetValue (param2)");
		}

		String type = getParameter(cmdContext, 3, "string");
		String value = getParameter(cmdContext, 4, "");

		try {
			PStcl target = cmdContext.getTarget();

			if (path == null) {
				throw new NullPointerException("No path defined");
			}

			StclContext stclContext = cmdContext.getStencilContext();
			PStcl prop = target.getStencil(stclContext, path);
			if (type.equals("string")) {
				prop.setValue(stclContext, value);
			} else if (type.equals("int")) {
				prop.setValue(stclContext, new Integer(value).toString());
			} else if (type.equals("boolean")) {
				prop.setValue(stclContext, new Boolean(value).toString());
			} else {
				return error(cmdContext, self, "wrong type for SetValue (param3)");
			}
			return success(cmdContext, self);
		} catch (WrongPathException e) {
			throw new NullPointerException("the param1 (path) is wrong : " + path);
		} catch (ClassCastException e) {
			throw new NullPointerException("the stencil in param1 (path) must be a Property");
		}
	}
}
