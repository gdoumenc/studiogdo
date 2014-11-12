package com.gdo.reflect;

import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;

public class DelegatedSlotStcl extends SlotStcl {

    public interface Slot extends SlotStcl.Slot {
        String DELEGATE_PATH = "DelegatePath";
    }

    public DelegatedSlotStcl(StclContext stclContext, PSlot<StclContext, PStcl> slot) {
        super(stclContext, slot);

        propSlot(Slot.DELEGATE_PATH);
    }

}
