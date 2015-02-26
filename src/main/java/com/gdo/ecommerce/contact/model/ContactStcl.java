package com.gdo.ecommerce.contact.model;

import com.gdo.sql.model.SQLStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PStcl;

public abstract class ContactStcl extends SQLStcl {

	public ContactStcl(StclContext stclContext) {
		super(stclContext);
	}

	public abstract String getContactsService(StclContext stclContext, PStcl self);
}
