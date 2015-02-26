/**
 * Copyright GDO - 2005
 */
package com.gdo.website.model;

import com.gdo.sql.model.SQLStcl;
import com.gdo.stencils.StclContext;

public class SQLSimplePageStcl extends SQLStcl {

	public interface Slot extends SQLStcl.Slot {
		String PAGE = "Page";
	}

	public SQLSimplePageStcl(StclContext stclContext) {
		super(stclContext);
	}

}
