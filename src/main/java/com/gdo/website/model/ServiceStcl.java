/*
 * Copyright GDO - 2004
 */
package com.gdo.website.model;

import com.gdo.stencils.StclContext;

public class ServiceStcl extends com.gdo.project.model.ServiceStcl {

	public interface Slot extends com.gdo.project.model.ServiceStcl.Slot {
		String PAGES = "Pages";

		String FTP_CONTEXT = "FtpContext";
		String RES_FTP_CONTEXT = "ResFtpContext";
	}

	public interface Command extends com.gdo.project.model.ServiceStcl.Command {
		String ADD_SIMPLE_PAGE = "AddSimplePage";
		String ADD_STRUCTURED_PAGE = "AddStructuredPage";

	}

	public ServiceStcl(StclContext stclContext) {
		super(stclContext);
	}
}