/**
 * Copyright GDO - 2005
 */
package com.gdo.reflect;

import com.gdo.stencils.StclContext;
import com.gdo.stencils._Stencil;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.MultiCalculatedSlot;

public class WhereSlot extends MultiCalculatedSlot<StclContext, PStcl> {

	public WhereSlot(StclContext stclContext, _Stencil<StclContext, PStcl> stcl, String name) {
		super(stclContext, stcl, name, PSlot.ANY);
	}

	@Override
	protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> condition, PSlot<StclContext, PStcl> self) {
		/*
		 * StclFactory factory = (StclFactory) stclContext.getStencilFactory();
		 * TemplateDescriptor<StclContext, PStcl> slotTempDesc =
		 * factory.getTemplateDescriptor(stclContext, SlotStcl.class.getName());
		 * Stencil<StclContext, PStcl> stcl =
		 * self.getContainer(stclContext).getStencil(); for (PSlot<StclContext,
		 * PStcl> slot : stcl.getContainingSlots().values()) { String slotName =
		 * slot.getName(stclContext); if (getStencilFromList(stclContext, new
		 * Key<String>(slotName), self) != null) { keepStencilInList(stclContext,
		 * new Key<String>(slot.getName(stclContext)), self); } else { PStcl
		 * slotStcl = factory.newPStencil(stclContext, self, new
		 * Key<String>(slotName), slotTempDesc, slot); addStencilInList(stclContext,
		 * slotStcl, self); } }
		 */
		return cleanList(stclContext, condition, self);
	}
}
