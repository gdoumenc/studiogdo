/**
 * Copyright GDO - 2004
 */
package com.gdo.project.model;

import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;

public class ServiceStcl extends Stcl {

	public interface Slot extends Stcl.Slot {
		String SERVICES = "Services";
	}

	public ServiceStcl(StclContext stclContext) {
		super(stclContext);

		multiSlot(Slot.SERVICES);
	}

}