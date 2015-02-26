/**
 * Copyright GDO - 2004
 */
package com.gdo.crm.mail.model;

import com.gdo.stencils.StclContext;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.plug.PStcl;

public class SQLDistributionListStcl extends com.gdo.mail.model.SQLDistributionListStcl {

	public SQLDistributionListStcl(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public String getDeleteQuery(StclContext stclContext, IKey key, PStcl stencil, PStcl self) {
		String delete = self.getString(stclContext, Slot.DELETE_QUERY, "");
		return self.format(stclContext, delete);
	}

}