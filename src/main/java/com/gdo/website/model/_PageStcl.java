/*
 * Copyright GDO - 2004
 */
package com.gdo.website.model;

import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PStcl;

public abstract class _PageStcl extends Stcl {

	public interface Slot extends Stcl.Slot {
		String CONTENT = "Content";

		// resources part
		String IMAGES = "Images";
		String DOCUMENTS = "Documents";

		// generation/upload part
		String GENERATOR = "Generator";
		String FTP_CONTEXT = "FtpContext";
		String RES_FTP_CONTEXT = "ResFtpContext";
	}

	public interface Command {
		String UPDATE = "Update";
	}

	public _PageStcl(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public void afterCompleted(StclContext stclContext, PStcl self) {
		super.afterCompleted(stclContext, self);

		self.setString(stclContext, Slot.NAME, "Page");
	}

}