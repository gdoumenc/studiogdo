/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils.slot;

import com.gdo.stencils._Stencil;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug._PStencil;

public class EncapsulateSlot<C extends _StencilContext, S extends _PStencil<C, S>> extends SingleCalculatedSlot<C, S> {

	private S _encapsulated; // stencil encapsulated

	public EncapsulateSlot(C stclContext, _Stencil<C, S> in, String name, S stencil) {
		super(stclContext, in, name, PSlot.ONE);
		this._encapsulated = stencil;
	}

	@Override
	public S getCalculatedStencil(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
		return this._encapsulated;
	}

}
