package com.gdo.ecommerce.contact.model;

import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PStcl;

public abstract class ServiceStcl extends com.gdo.project.model.ServiceStcl {

	public interface Slot extends com.gdo.project.model.ServiceStcl.Slot {
		String SQL_CONTEXT = "SqlContext";
	}

	public ServiceStcl(StclContext stclContext) {
		super(stclContext);
	}

	public abstract Result updateContact(StclContext stclContext, PStcl stencil, PStcl self);
}
