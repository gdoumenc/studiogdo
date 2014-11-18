/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils;

import com.gdo.project.slot.RootSlot;
import com.gdo.reflect.CommandsSlot;
import com.gdo.reflect.PwdSlot;
import com.gdo.reflect.SlotsSlot;
import com.gdo.reflect.TemplateNameSlot;
import com.gdo.reflect.WhereSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.prop.CalculatedPropStencil;
import com.gdo.stencils.prop.IPropCalculator;
import com.gdo.stencils.slot._Slot;
import com.gdo.stencils.util.PathUtils;

/**
 * <p>
 * Basic implementation of the studiogdo property stencil.
 * </p>

 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>

 */
public class CalculatedPropStcl extends CalculatedPropStencil<StclContext, PStcl> {

    public interface Slot extends Stcl.Slot {
    }

    public CalculatedPropStcl(StclContext stclContext, IPropCalculator<StclContext, PStcl> calculator) {
        super(stclContext, calculator);

        // reflexive slots
        createTemplateNameSlot(stclContext);
        createPwdSlot(stclContext);
        createSlotSlot(stclContext);
        createCommandSlot(stclContext);
        createWhereSlot(stclContext);
    }

    @Override
    protected _Slot<StclContext, PStcl> createRootSlot(StclContext stclContext) {
        return new RootSlot(stclContext, this, PathUtils.ROOT);
    }

    protected _Slot<StclContext, PStcl> createTemplateNameSlot(StclContext stclContext) {
        return new TemplateNameSlot(stclContext, this, Slot.$TEMPLATE_NAME);
    }

    protected _Slot<StclContext, PStcl> createPwdSlot(StclContext stclContext) {
        return new PwdSlot(stclContext, this, Slot.$PWD);
    }

    protected _Slot<StclContext, PStcl> createSlotSlot(StclContext stclContext) {
        return new SlotsSlot(stclContext, this, Slot.$SLOTS);
    }

    protected _Slot<StclContext, PStcl> createCommandSlot(StclContext stclContext) {
        return new CommandsSlot(stclContext, this, Slot.$COMMANDS);
    }

    protected _Slot<StclContext, PStcl> createWhereSlot(StclContext stclContext) {
        return new WhereSlot(stclContext, this, Slot.$WHERE);
    }

}