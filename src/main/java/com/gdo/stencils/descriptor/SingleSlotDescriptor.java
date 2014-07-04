package com.gdo.stencils.descriptor;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.slot.SingleSlot;
import com.gdo.stencils.slot._Slot;

public class SingleSlotDescriptor<C extends _StencilContext, S extends _PStencil<C, S>> extends _SlotDescriptor<C, S> {
	char _arity = PSlot.NONE_OR_ONE;
	boolean _tranzient = false;

	public SingleSlotDescriptor() {
		super();
	}

	public SingleSlotDescriptor(Links links) {
		super(links);
	}

	public SingleSlotDescriptor(char arity, boolean tranzient, Links links) {
		super(links);
		_arity = arity;
		_tranzient = tranzient;
	}

	@Override
	public _Slot<C, S> add(C stclContext, String name, S self) {
		return new SingleSlot<C, S>(stclContext, self.getReleasedStencil(stclContext), name, _arity, _tranzient, false);
	}
}
