/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.slot;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import com.gdo.stencils.Result;
import com.gdo.stencils._Stencil;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.util.SlotUtils;
import com.gdo.stencils.util.StencilUtils;

/**
 * <p>
 * The stencils plugged in this slot are calculated by a java code.
 * </p>
 * <p>
 * The stencils map is calculated by the method
 * <tt>Map getStencils(StencilContext stclContext)</tt>. I the calculation must
 * be done once then public StencilIterator getStencils(StencilContext context,
 * String condition, PStencilStencil parent) {
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
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com)</a>
 */
public abstract class _MultiSlot<C extends _StencilContext, S extends _PStencil<C, S>> extends _Slot<C, S> implements Comparator<S> {

    public _MultiSlot(C stclContext, _Stencil<C, S> in, String name, char arity, boolean tranzient, boolean override) {
        super(stclContext, in, name, arity, tranzient, override);
        if (!SlotUtils.isMultiple(arity)) {
            logWarn(stclContext, "Multi slot %s created with strange arity %s in %s", name, Character.toString(arity), in);
        }
    }

    /**
     * This methods is called to retrieve the stencils in the slot.
     */
    abstract protected StencilIterator<C, S> getStencilsList(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self);

    @Override
    protected S doPlug(C stclContext, S stencil, IKey key, PSlot<C, S> self) {
        return StencilUtils.<C, S> nullPStencil(stclContext, Result.error("cannot plug [_MultiSlot]"));
    }

    @Override
    protected void doUnplug(C stclContext, S stencil, IKey key, PSlot<C, S> self) {
    }

    public Result doMultiUnplug(C stclContext, String keys, PSlot<C, S> self) {
        return Result.error("internal error, multi unplug undefined");
    }

    @Override
    public int compare(S arg0, S arg1) {
        return arg0.getKey().compareTo(arg1.getKey());
    }

    @Override
    public Comparator<S> reversed() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Comparator<S> thenComparing(Comparator<? super S> other) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <U> Comparator<S> thenComparing(Function<? super S, ? extends U> keyExtractor, Comparator<? super U> keyComparator) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <U extends Comparable<? super U>> Comparator<S> thenComparing(Function<? super S, ? extends U> keyExtractor) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Comparator<S> thenComparingInt(ToIntFunction<? super S> keyExtractor) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Comparator<S> thenComparingLong(ToLongFunction<? super S> keyExtractor) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Comparator<S> thenComparingDouble(ToDoubleFunction<? super S> keyExtractor) {
        // TODO Auto-generated method stub
        return null;
    }

}