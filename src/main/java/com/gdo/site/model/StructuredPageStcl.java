/*
 * Copyright GDO - 2004
 */
package com.gdo.site.model;

import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;

public class StructuredPageStcl extends Stcl {

	public interface Slot extends Stcl.Slot {
		String CHAPTERS = "Chapters";
	}

	public StructuredPageStcl(StclContext stclContext) {
		super(stclContext);
	}

}