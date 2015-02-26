/**
 * Copyright GDO - 2004
 */
package com.gdo.email.model;

import com.gdo.stencils.StclContext;

public class SQLDistributionListStcl extends com.gdo.mail.model.SQLDistributionListStcl {

	public interface Slot extends com.gdo.mail.model.SQLDistributionListStcl.Slot {
		String STATUS = "Status";
	}

	public SQLDistributionListStcl(StclContext stclContext) {
		super(stclContext);
	}

}