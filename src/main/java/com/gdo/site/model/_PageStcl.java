/*
 * Copyright GDO - 2004
 */
package com.gdo.site.model;

import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;

public class _PageStcl extends Stcl {

	public interface Slot extends Stcl.Slot {
		String CONTENT = "Content";

		// resources part
		String RES_FTP_CONTEXT = "ResFtpContext";
		String IMAGES = "Images";
		String DOCUMENTS = "Documents";

		// generation/upload part
		String FTP_CONTEXT = "FtpContext";
		String GENERATOR = "Generator";
	}

	public interface Command {
		String UPDATE = "Update";
	}

	public _PageStcl(StclContext stclContext) {
		super(stclContext);
	}

}