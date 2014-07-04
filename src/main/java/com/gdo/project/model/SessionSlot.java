package com.gdo.project.model;

import com.gdo.project.model.ServletStcl.Slot;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.factory.StclFactory;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.SingleCalculatedSlot;

public class SessionSlot extends SingleCalculatedSlot<StclContext, PStcl> {

	public SessionSlot(StclContext stclContext, Stcl in) {
		super(stclContext, in, Slot.SESSION);
	}

	@Override
	public PStcl getCalculatedStencil(StclContext stclContext, StencilCondition<StclContext, PStcl> condition, PSlot<StclContext, PStcl> self) {
		StclFactory factory = (StclFactory) stclContext.getStencilFactory();
		Stcl stcl = SessionStcl.getSessionStcl(stclContext);
		return factory.newPStencil(stclContext, self, Key.NO_KEY, stcl);
	}
}
