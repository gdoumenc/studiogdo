/*
 * Copyright GDO - 2004
 */
package com.gdo.website.model;

import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;

public abstract class _ChapterStcl extends Stcl {

	public interface Slot extends Stcl.Slot {
		String CONTENT = "Content";
	}

	public _ChapterStcl(StclContext stclContext) {
		super(stclContext);
	}

}