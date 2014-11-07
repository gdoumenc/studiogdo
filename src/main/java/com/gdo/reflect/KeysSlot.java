/**
 * Copyright GDO - 2004
 */
package com.gdo.reflect;

import com.gdo.stencils.StclContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.factory.StclFactory;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.MultiCalculatedSlot;

/**
 * <p>
 * Reflexive calculated slot for the slots list.
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
public class KeysSlot extends MultiCalculatedSlot<StclContext, PStcl> {
    private PSlot<StclContext, PStcl> _slot;

    public KeysSlot(StclContext stclContext, SlotStcl in) {
        super(stclContext, in, SlotStcl.Slot.KEYS, PSlot.ANY);
        _slot = in.getSlot();
    }

    @Override
    protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
        for (PStcl stencil : _slot.getStencils(stclContext, cond)) {

            // creates the key
            IKey key = stencil.getKey();
            if (stencil.isLink(stclContext)) {
                key = new Key<String>("$" + key.toString());
            }

            // if already in list, does nothing
            if (getStencilFromList(stclContext, key, self) != null) {
                keepStencilInList(stclContext, key, self);
                continue;
            }

            // creates the new key stencil
            StclFactory factory = (StclFactory) stclContext.getStencilFactory();
            PStcl keyStcl = factory.createPStencil(stclContext, self, key, KeyStcl.class, stencil);
            addStencilInList(stclContext, keyStcl, self);
        }
        return cleanList(stclContext, cond, self);
    }
}