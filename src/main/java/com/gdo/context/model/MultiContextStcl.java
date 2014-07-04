/**
 * Copyright GDO - 2004
 */
package com.gdo.context.model;

import com.gdo.stencils.StclContext;

public class MultiContextStcl extends ContextStcl {

	public interface Slot extends ContextStcl.Slot {
		String CONTEXT = "Context";
		String SELECTED = "Selected";
	}

	public MultiContextStcl(StclContext stclContext) {
		super(stclContext);

		// SLOT PART

		multiSlot(Slot.CONTEXT);
		singleSlot(Slot.SELECTED);
	}
}