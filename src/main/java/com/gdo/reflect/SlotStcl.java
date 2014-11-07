/**
 * Copyright GDO - 2004
 */
package com.gdo.reflect;

import org.apache.poi.ss.formula.eval.NotImplementedException;

import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.CalculatedIntegerPropertySlot;
import com.gdo.stencils.slot.CalculatedStringPropertySlot;

/**
 * <p>
 * Reflexive slot descriptor stencil.
 * </p>
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */
public class SlotStcl extends Stcl {

    public interface Slot extends Stcl.Slot {
        String TYPE = "Type";

        String PWD = "Pwd";
        String ARITY = "Arity";
        String SIZE = "Size";
        String KEYS = "Keys";
        String CONTAINER = "Container";
    }

    private PSlot<StclContext, PStcl> _slot; // slot described

    public SlotStcl(StclContext stclContext, PSlot<StclContext, PStcl> slot) {
        super(stclContext);
        _slot = slot;

        // SLOT PART

        propSlot(Slot.TYPE, "slot");

        new PwdSlot(stclContext);
        new AritySlot(stclContext);
        new SizeSlot(stclContext);
        new KeysSlot(stclContext, this);

        singleSlot(Slot.CONTAINER);

        // COMMAND PART

        command(Command.PLUG, PlugCmd.class);
        command(Command.UNPLUG, UnplugCmd.class);
    }

    @Override
    public String getName(StclContext stclContext, PStcl self) {
        return getSlot().getName(stclContext);
    }

    public PSlot<StclContext, PStcl> getSlot() {
        return _slot;
    }

    /**
     * Path prop value is calculated from slot's path.
     */
    private class PwdSlot extends CalculatedStringPropertySlot<StclContext, PStcl> {
        public PwdSlot(StclContext stclContext) {
            super(stclContext, SlotStcl.this, Slot.PWD);
        }

        @Override
        public String getValue(StclContext stclContext, PStcl self) {
            return getSlot().pwd(stclContext).replaceAll("//", "/");
        }

        @Override
        public String setValue(StclContext stclContext, String value, PStcl self) {
            String msg = String.format("Cannot change %s value", Slot.PWD);
            throw new NotImplementedException(msg);
        }
    }

    /**
     * Arity prop value is calculated from slot's arity.
     */
    protected class AritySlot extends CalculatedStringPropertySlot<StclContext, PStcl> {
        public AritySlot(StclContext stclContext) {
            super(stclContext, SlotStcl.this, Slot.ARITY);
        }

        @Override
        public String getValue(StclContext stclContext, PStcl self) {
            return Character.toString(getSlot().getArity(stclContext));
        }

        @Override
        public String setValue(StclContext stclContext, String value, PStcl self) {
            String msg = String.format("Cannot change %s value", Slot.ARITY);
            throw new NotImplementedException(msg);
        }
    }

    /**
     * Arity prop value is calculated from slot's arity.
     */
    protected class SizeSlot extends CalculatedIntegerPropertySlot<StclContext, PStcl> {
        public SizeSlot(StclContext stclContext) {
            super(stclContext, SlotStcl.this, Slot.SIZE);
        }

        @Override
        public int getIntegerValue(StclContext stclContext, PStcl self) {
            return getSlot().size(stclContext, null);
        }

        @Override
        public int setIntegerValue(StclContext stclContext, int value, PStcl self) {
            String msg = String.format("Cannot change %s value", Slot.SIZE);
            throw new NotImplementedException(msg);
        }
    }

}