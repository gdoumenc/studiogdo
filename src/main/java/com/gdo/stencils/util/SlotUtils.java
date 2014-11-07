/*
 * Copyright GDO - 2004
 */
package com.gdo.stencils.util;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.interpreted.SlotDescriptor;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.slot._Slot;

public class SlotUtils {

    private SlotUtils() {
        // utility class, disable instanciation
    }

    /**
     * @return <tt>true</tt> if the slot is not defined.
     */
    public static <C extends _StencilContext, S extends _PStencil<C, S>> boolean isNull(PSlot<C, S> slot) {
        return (slot == null || slot.getSlot() == null);
    }

    public static <C extends _StencilContext, S extends _PStencil<C, S>> boolean isNull(_Slot<C, S> slot) {
        return (slot == null);
    }

    /**
     * @return <tt>true</tt> if the slot is defined.
     */
    public static <C extends _StencilContext, S extends _PStencil<C, S>> boolean isNotNull(PSlot<C, S> slot) {
        return (slot != null && slot.getSlot() != null);
    }

    public static <C extends _StencilContext, S extends _PStencil<C, S>> boolean isNotNull(_Slot<C, S> slot) {
        return (slot != null);
    }

    /**
     * Tests if two slots are not null and are same.
     * 
     * @return <tt>true</tt> if the two slots are defined and are same.
     */
    public static final <C extends _StencilContext, S extends _PStencil<C, S>> boolean equals(PSlot<C, S> slot1, PSlot<C, S> slot2) {
        if (slot1 != null)
            return slot1.equals(slot2);
        if (slot2 != null)
            return slot2.equals(slot1);
        return false;
    }

    /**
     * @return <tt>true</tt> if the slot is single.
     */
    public static boolean isSingle(char arity) {
        return (arity == PSlot.NONE_OR_ONE || arity == PSlot.ONE);
    }

    public static <C extends _StencilContext, S extends _PStencil<C, S>> boolean isSingle(C stclContext, PSlot<C, S> slot) {
        return isSingle(slot.getArity(stclContext));
    }

    public static <C extends _StencilContext, S extends _PStencil<C, S>> boolean isSingle(SlotDescriptor<C, S> slotDesc) {
        return isSingle(slotDesc.getArity());
    }

    /**
     * @return <tt>true</tt> if the slot is multiple.
     */
    public static boolean isMultiple(char arity) {
        return (arity == PSlot.ANY || arity == PSlot.AT_LEAST_ONE);
    }

    public static <C extends _StencilContext, S extends _PStencil<C, S>> boolean isMultiple(C stclContext, PSlot<C, S> slot) {
        return isMultiple(slot.getArity(stclContext));
    }

    public static <C extends _StencilContext, S extends _PStencil<C, S>> boolean isMultiple(SlotDescriptor<C, S> slotDesc) {
        return isMultiple(slotDesc.getArity());
    }

    /**
     * @return <tt>true</tt> if the slot may be empty.
     */
    public static boolean allowEmpty(char arity) {
        return (arity == PSlot.NONE_OR_ONE || arity == PSlot.ANY);
    }

    /**
     * @return <tt>true</tt> if the slot is hidden.
     */
    public static boolean isHidden(char arity) {
        return (arity == PSlot.HIDDEN);
    }

    public static <C extends _StencilContext, S extends _PStencil<C, S>> boolean isHidden(SlotDescriptor<C, S> slotDesc) {
        return isHidden(slotDesc.getArity());
    }

    /**
     * @return the slot defined by a name in a list of plugged slots (slots list
     *         must not be null)
     */
    /*
     * public static <C extends StencilContext, S extends PStencil<C, S>> PSlot<C,
     * S> get(C stclContext, Collection<PSlot<C, S>> slots, String name) { if
     * (slots == null) { throw new
     * IllegalArgumentException("empty stencils list in SlotUtils.get"); } for
     * (PSlot<C, S> slot : slots) { if (slot.getName(stclContext).equals(name))
     * return slot; } return null; }
     */
    /*
     * public static <C extends StencilContext, S extends PStencil<C, S>> _Slot<C,
     * S> get(C stclContext, Collection<_Slot<C, S>> slots, String name) { if
     * (slots == null) { throw new
     * IllegalArgumentException("empty stencils list in SlotUtils.get"); } for
     * (_Slot<C, S> slot : slots) { String n = slot.getName(stclContext); if
     * (StringUtils.equals(n, name)) return slot; } return null; }
     */

}
