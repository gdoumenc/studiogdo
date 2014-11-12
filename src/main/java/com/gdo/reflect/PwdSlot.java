package com.gdo.reflect;

import com.gdo.stencils.StclContext;
import com.gdo.stencils._Stencil;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.CalculatedStringPropertySlot;

public class PwdSlot extends CalculatedStringPropertySlot<StclContext, PStcl> {

    public PwdSlot(StclContext stclContext, _Stencil<StclContext, PStcl> in, String name) {
        super(stclContext, in, name);
    }

    @Override
    public String getValue(StclContext stclContext, PStcl self) {
        return self.getContainer(stclContext).pwd(stclContext);
    }
}
