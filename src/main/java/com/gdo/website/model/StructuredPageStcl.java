/*
 * Copyright GDO - 2004
 */
package com.gdo.website.model;

import com.gdo.stencils.StclContext;

public class StructuredPageStcl extends SimplePageStcl {

	public interface Slot extends SimplePageStcl.Slot {
		String CHAPTERS = "Chapters";
	}

	public StructuredPageStcl(StclContext stclContext) {
		super(stclContext);
	}

}