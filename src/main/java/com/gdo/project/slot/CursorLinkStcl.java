package com.gdo.project.slot;

import com.gdo.project.adaptor.LinkStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.factory.StclFactory;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.StencilUtils;

/**
 * Implements the saving of a cursor based plugged stencil.
 */
public class CursorLinkStcl extends LinkStcl {

	public CursorLinkStcl(StclContext stclContext, String path) {
		super(stclContext, path);
	}

	@Override
	public StencilIterator<StclContext, PStcl> getStencils(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> slot, PStcl self) {

		// the link stencil must verify the condition (as condition is propagated to
		// the link and don't apply to the stencil referenced as for usual links)
		if (cond != null && !cond.verify(stclContext, self)) {
			return StencilUtils.< StclContext, PStcl> iterator();
		}

		// gets the stencil
		PStcl stcl = self.getStencil(stclContext, _path);

		// changes containing slot and key
		StclFactory factory = (StclFactory) stclContext.getStencilFactory();
		PStcl created = factory.createPStencil(stclContext, slot, self.getKey(), stcl);
		return StencilUtils.< StclContext, PStcl> iterator(stclContext, created, slot);
	}

	@Override
	public int size(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> slot, PStcl self) {
		StencilIterator<StclContext, PStcl> iter = getStencils(stclContext, cond, slot, self);
		return iter.size();
	}

	@Override
	public boolean contains(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PStcl searched, PSlot<StclContext, PStcl> slot, PStcl self) {
		StencilIterator<StclContext, PStcl> iter = getStencils(stclContext, cond, slot, self);
		return iter.size() > 0;
	}

	@Override
	public boolean hasStencils(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> slot, PStcl self) {
		StencilIterator<StclContext, PStcl> iter = getStencils(stclContext, cond, slot, self);
		return iter.size() > 0;
	}

}
