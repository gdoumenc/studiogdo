/**
 * Copyright GDO - 2005
 */

package com.gdo.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.gdo.project.model.ServletStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.StencilUtils;

/**
 * StudioGdo local main servlet for debug.
 * <p>
 * This servlet won't check session validity to accept any entry.
 * </p>
 */
@SuppressWarnings("serial")
public class LocalStudioGdoServlet extends StudioGdoServlet {

	@Override
	protected boolean isCallValid(StclContext stclContext, String entry, RpcArgs args) {
		HttpServletRequest request = stclContext.getRequest();
		HttpSession session = request.getSession();

		// if the session is new accepts only connect command
		if (session.isNew()) {
			return true;
		}

		// else checks the session is not dead internally (tomcat restarted or ..)
		PStcl servletStcl = stclContext.getServletStcl();
		PStcl active = servletStcl.getStencil(stclContext, ServletStcl.Slot.SESSION);
		return StencilUtils.isNotNull(active);
	}
}