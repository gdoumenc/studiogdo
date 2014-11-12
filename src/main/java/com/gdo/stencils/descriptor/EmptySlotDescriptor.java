package com.gdo.stencils.descriptor;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.slot.EmptySlot;
import com.gdo.stencils.slot._Slot;

public class EmptySlotDescriptor<C extends _StencilContext, S extends _PStencil<C, S>> extends _SlotDescriptor<C, S> {

    public EmptySlotDescriptor() {
        super();
    }

    @Override
    public _Slot<C, S> add(C stclContext, String name, S self) {
        return new EmptySlot<C, S>(stclContext, self.getReleasedStencil(stclContext), name);
    }
}
