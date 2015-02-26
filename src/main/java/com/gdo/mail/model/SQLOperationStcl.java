/**
 * Copyright GDO - 2004
 */
package com.gdo.mail.model;

import com.gdo.stencils.StclContext;

public class SQLOperationStcl extends OperationStcl {

	public interface Slot extends OperationStcl.Slot {
		String SQL_CONTEXT = "SqlContext";
	}

	public SQLOperationStcl(StclContext stclContext) {
		super(stclContext);
	}

}