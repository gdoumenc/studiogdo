/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils.faces.iterator;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.faces.RenderContext;
import com.gdo.stencils.plug._PStencil;

public class GdoLastIterator<C extends _StencilContext, S extends _PStencil<C, S>> extends GdoIterator<C, S> {

    private static final String[] LAST_ITERATORS_TAGS = new String[] { "iterator:last", "/iterator:last" };
    private static final int[] LAST_ITERATORS_LENGTHS = new int[] { LAST_ITERATORS_TAGS[0].length(), LAST_ITERATORS_TAGS[1].length() };

    public GdoLastIterator(RenderContext<C, S> renderContext) {
        super(renderContext);
    }

    @Override
    public String[] getTags() {
        return LAST_ITERATORS_TAGS;
    }

    @Override
    public int[] getTagsLength() {
        return LAST_ITERATORS_LENGTHS;
    }

    @Override
    public boolean isSubTag() {
        return true;
    }

}
