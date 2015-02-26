package com.gdo.ecommerce.order.model;

import com.gdo.stencils.StclContext;

public class ServiceStcl extends com.gdo.project.model.ServiceStcl {

	public interface Slot extends com.gdo.project.model.ServiceStcl.Slot {
		String SQL_CONTEXT = "SqlContext";

		String ORDERS = "Orders";
	}

	public ServiceStcl(StclContext stclContext) {
		super(stclContext);
	}

}
