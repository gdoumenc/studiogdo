/*
 * Copyright GDO - 2004
 */
package com.gdo.project.cmd;

import com.gdo.project.model.AtomicActionStcl;
import com.gdo.project.model.ProjectStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;

public class ConnectFromSessionUser extends AtomicActionStcl {

	private static final String PASSWD = "Passwd";

	public ConnectFromSessionUser(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();

		// get login user info
		String p = getResourcePath(stclContext, ProjectStcl.Resource.USER_CONNECTED, self);
		String userPath = getParameter(cmdContext, 1, p);
		PStcl userTested = self.getStencil(stclContext, userPath);
		String name = userTested.getName(stclContext);
		String passwd = userTested.getString(stclContext, PASSWD, "");

		// search on project users list
		PStcl project = stclContext.getServletStcl();
		for (PStcl user : project.getStencils(stclContext, ProjectStcl.Slot.USERS)) {
			if (name.equals(user.getName(stclContext)) && passwd.equals(user.getExpandedString(stclContext, PASSWD, ""))) {
				self.clearSlot(stclContext, p);
				self.plug(stclContext, user, p);
				return success(cmdContext, self);
			}
		}

		return error(cmdContext, self, 0, "wrong login");
	}
}
