/*
 * Copyright GDO - 2004
 */
package com.gdo.website.model;

import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;

public class StructuredChapterStcl extends Stcl {

	public interface Slot extends Stcl.Slot {
		String CHAPTERS = "Chapters";
	}

	public StructuredChapterStcl(StclContext stclContext) {
		super(stclContext);
	}

}