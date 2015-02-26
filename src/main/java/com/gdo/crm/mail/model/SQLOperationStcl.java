package com.gdo.crm.mail.model;

import com.gdo.stencils.StclContext;

public class SQLOperationStcl extends com.gdo.email.model.SQLOperationStcl {

	public interface Slot extends com.gdo.email.model.SQLOperationStcl.Slot {
		String COMMERCIAL = "Commercial";
	}

	public SQLOperationStcl(StclContext stclContext) {
		super(stclContext);
	}

}
