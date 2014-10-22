package com.gdo.stencils.descriptor;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.slot.PropSlot;
import com.gdo.stencils.slot._Slot;

public class PropSlotDescriptor<C extends _StencilContext, S extends _PStencil<C, S>, K> extends _SlotDescriptor<C, S> {
	K _initial = null;

	public PropSlotDescriptor(K initial) {
		super();
		_initial = initial;
	}

	@Override
	public _Slot<C, S> add(C stclContext, String name, S self) {
	    _Slot<C, S> slot = new PropSlot<C, S, K>(stclContext, self.getReleasedStencil(stclContext), name, _initial);
	    if (_tranzient)
	        slot.setTransient();
	    return slot;
	}
}
