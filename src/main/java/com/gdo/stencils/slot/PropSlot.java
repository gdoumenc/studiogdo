package com.gdo.stencils.slot;

import com.gdo.stencils._Stencil;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.util.StencilUtils;

public class PropSlot<C extends _StencilContext, S extends _PStencil<C, S>, K> extends SingleSlot<C, S> {

	private K _initial;

	public PropSlot(C stclContext, _Stencil<C, S> in, String name, K initial) {
		super(stclContext, in, name, PSlot.ONE);
		this._initial = initial;
	}

	@Override
	protected S getContainedStencilOrCreateDefault(C stclContext, PSlot<C, S> self) {

		// creates it only if doesn't already exist
		if (StencilUtils.isNotNull(this._containedStcl)) {
			this._containedStcl.setContainingSlot(self);
			return this._containedStcl;
		}

		this._containedStcl = self.getContainer().newPProperty(stclContext, self, Key.NO_KEY, this._initial);
		return this._containedStcl;
	}
}