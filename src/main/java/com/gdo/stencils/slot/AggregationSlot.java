/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils.slot;

import java.util.ArrayList;
import java.util.List;

import com.gdo.helper.ClassHelper;
import com.gdo.helper.StringHelper;
import com.gdo.stencils._Stencil;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;

/**
 * <p>
 * The stencils plugged in this slot come from several slots.
 * </p>
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo & Guillaume Doumenc. Use
 * is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc
 */
public class AggregationSlot<C extends _StencilContext, S extends _PStencil<C, S>> extends MultiCalculatedSlot<C, S> {

    private String[] _pathes;

    /**
     * @param pathes
     *            ':' separated list of slots where the plugged stencils are
     *            found.
     */
    public AggregationSlot(C stclContext, _Stencil<C, S> in, String name, String pathes) {
        super(stclContext, in, name, PSlot.ANY);
        setPathes(pathes);
    }

    public void setPathes(String pathes) {
        _pathes = StringHelper.splitShortStringAndTrim(pathes, PathUtils.MULTI);
    }

    @Override
    protected StencilIterator<C, S> getStencilsList(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
        if (ClassHelper.isEmpty(_pathes))
            return StencilUtils.<C, S> iterator();
        List<S> stencils = new ArrayList<S>();
        for (String path : _pathes) {
            StencilIterator<C, S> iter = self.getContainer().getStencils(stclContext, path);
            for (S stcl : iter) {
                stencils.add(stcl);
            }
        }
        return StencilUtils.<C, S> iterator(stclContext, stencils, cond, self);
    }
}
