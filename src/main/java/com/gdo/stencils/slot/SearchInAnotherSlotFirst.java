/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils.slot;

import com.gdo.stencils.WrongPathException;
import com.gdo.stencils._Stencil;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug._PStencil;

/**
 * <p>
 * Search in container if exists before inside itself.
 * </p>
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo & Guillaume Doumenc. Use
 * is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a>
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */
public class SearchInAnotherSlotFirst<C extends _StencilContext, S extends _PStencil<C, S>> extends SingleSlot<C, S> {

    public SearchInAnotherSlotFirst(C stclContext, _Stencil<C, S> in, String name, char arity, boolean tranzient) {
        super(stclContext, in, name, arity, tranzient, false);
    }

    public SearchInAnotherSlotFirst(C stclContext, _Stencil<C, S> in, String name, char arity) {
        this(stclContext, in, name, arity, false);
    }

    public SearchInAnotherSlotFirst(C stclContext, _Stencil<C, S> in, String name) {
        this(stclContext, in, name, PSlot.NONE_OR_ONE, false);
    }

    @Override
    public boolean isTransient(C stclContext) {

        // test if explicitly defined transient
        if (super.isTransient(stclContext))
            return true;

        // don't store the stencil if find in another one or not already
        // created...
        return _containedStcl == null;
    }

    @Override
    public boolean contains(C stclContext, StencilCondition<C, S> cond, S searched, PSlot<C, S> self) {

        // if not created search in another slot first
        if (_containedStcl == null) {
            String path = getParameter(stclContext, 0);
            try {
                PSlot<C, S> slot = self.getContainer().getSlot(stclContext, path);
                if (slot.contains(stclContext, cond, searched))
                    return true;
            } catch (WrongPathException e) {
            }
        }
        return super.contains(stclContext, cond, searched, self);
    }

    @Override
    public boolean hasStencils(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {

        // if not created search in another slot first
        if (_containedStcl == null) {
            try {
                String path = getParameter(stclContext, 0);
                if (self.getContainer().hasStencils(stclContext, path, null))
                    return true;
            } catch (WrongPathException e) {
            }
        }
        return super.hasStencils(stclContext, cond, self);
    }

    @Override
    public StencilIterator<C, S> getStencils(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {

        // if not created search in another slot first
        if (_containedStcl == null) // only if not created
        {
            try {
                String path = getParameter(stclContext, 0);
                return self.getContainer().getStencils(stclContext, path);
            } catch (WrongPathException e) {
            }
        }
        return super.getStencils(stclContext, cond, self);
    }

}
