package com.gdo.stencils.descriptor;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.slot.MultiSlot;
import com.gdo.stencils.slot._Slot;

public class MultiSlotDescriptor<C extends _StencilContext, S extends _PStencil<C, S>> extends _SlotDescriptor<C, S> {
    char _arity = PSlot.ANY;
    boolean _tranzient = false;

    public MultiSlotDescriptor() {
        super();
    }

    public MultiSlotDescriptor(char arity, boolean tranzient, Links links) {
        super();
        _arity = arity;
        _links = links;
        _tranzient = tranzient;
    }

    @Override
    public _Slot<C, S> add(C stclContext, String name, S self) {
        return new MultiSlot<C, S>(stclContext, self.getReleasedStencil(stclContext), name, _arity, _tranzient);
    }
}
