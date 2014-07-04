/**
 * Copyright GDO - 2004
 */
package com.gdo.sql.model;

import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;

public class DataBaseStoredStencil extends Stcl {

	public interface Slot extends Stcl.Slot {
		String ID = "Id";
		String SQL_CONTEXT = "SqlContext";
	}

	public DataBaseStoredStencil(StclContext stclContext) {
		super(stclContext);
	}
}