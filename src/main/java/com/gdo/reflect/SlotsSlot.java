/**
 * Copyright GDO - 2004
 */
package com.gdo.reflect;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import org.apache.commons.lang3.StringUtils;

import com.gdo.stencils.StclContext;
import com.gdo.stencils._Stencil;
import com.gdo.stencils.cond.PathCondition;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.MultiCalculatedSlot;
import com.gdo.stencils.slot._Slot;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;

/**
 * <p>
 * Reflexive calculated slot for the slots list.
 * </p>
 * 
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 * 
 * 
 * @author Guillaume Doumenc (<a>
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */
public class SlotsSlot extends MultiCalculatedSlot<StclContext, PStcl> {

    public SlotsSlot(StclContext stclContext, _Stencil<StclContext, PStcl> in, String name) {
        super(stclContext, in, name, PSlot.ANY);
    }

    @Override
    protected PStcl doPlug(StclContext stclContext, PStcl stencil, IKey key, PSlot<StclContext, PStcl> self) {
        return stencil;
    }

    @Override
    protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {

        // iterates over slots list
        String pattern = PathCondition.getKeyCondition(cond);
        PStcl container = self.getContainer();
        for (_Slot<StclContext, PStcl> slot : container.getReleasedStencil(stclContext).getSlots().values()) {
            PSlot<StclContext, PStcl> pslot = new PSlot<StclContext, PStcl>(slot, container);

            // don't put internal slots
            String slotName = slot.getName();
            if (StringUtils.isBlank(slotName)) {
                slotName = "$EMPTY$";
            } else if (slotName.equals(PathUtils.ROOT)) {
                continue;
            } else if (slotName.startsWith("$")) {
                continue;
            }
            if (slotName.matches("[.].*")) {
                continue;
            }

            // gets slot's name
            String name = slot.getName();
            if (StringUtils.isNotBlank(pattern) && !name.matches(pattern)) {
                continue;
            }

            // if already in list, do nothing
            IKey key = (StringUtils.isEmpty(name)) ? Key.NO_KEY : new Key(name);
            if (getStencilFromList(stclContext, key, self) != null) {
                keepStencilInList(stclContext, key, self);
                continue;
            }

            // creates the slot stencil
            PStcl slotStcl = container.newPStencil(stclContext, self, key, SlotStcl.class, pslot);
            slotStcl.plug(stclContext, container, SlotStcl.Slot.CONTAINER);
            addStencilInList(stclContext, slotStcl, self);
        }

        // orders the list by slot name
        StencilIterator<StclContext, PStcl> slots = cleanList(stclContext, cond, self);
        return StencilUtils.sort(stclContext, slots, new SlotComparable());
    }

    public class SlotComparable implements Comparator<PStcl> {

        @Override
        public int compare(PStcl o1, PStcl o2) {
            return o1.getKey().compareTo(o2.getKey());
        }

        @Override
        public Comparator<PStcl> reversed() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Comparator<PStcl> thenComparing(Comparator<? super PStcl> other) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <U> Comparator<PStcl> thenComparing(Function<? super PStcl, ? extends U> keyExtractor, Comparator<? super U> keyComparator) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <U extends Comparable<? super U>> Comparator<PStcl> thenComparing(Function<? super PStcl, ? extends U> keyExtractor) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Comparator<PStcl> thenComparingInt(ToIntFunction<? super PStcl> keyExtractor) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Comparator<PStcl> thenComparingLong(ToLongFunction<? super PStcl> keyExtractor) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Comparator<PStcl> thenComparingDouble(ToDoubleFunction<? super PStcl> keyExtractor) {
            // TODO Auto-generated method stub
            return null;
        }

    }
}