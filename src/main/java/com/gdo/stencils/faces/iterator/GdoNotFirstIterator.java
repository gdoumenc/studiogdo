/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils.faces.iterator;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.faces.RenderContext;
import com.gdo.stencils.plug._PStencil;

public class GdoNotFirstIterator<C extends _StencilContext, S extends _PStencil<C, S>> extends GdoIterator<C, S> {

    private static final String[] NOT_FIRST_ITERATORS_TAGS = new String[] { "iterator:not_first", "/iterator:not_first" };
    private static final int[] NOT_FIRST_ITERATORS_LENGTHS = new int[] { NOT_FIRST_ITERATORS_TAGS[0].length(), NOT_FIRST_ITERATORS_TAGS[1].length() };

    public GdoNotFirstIterator(RenderContext<C, S> renderContext) {
        super(renderContext);
    }

    @Override
    public String[] getTags() {
        return NOT_FIRST_ITERATORS_TAGS;
    }

    @Override
    public int[] getTagsLength() {
        return NOT_FIRST_ITERATORS_LENGTHS;
    }

    @Override
    public boolean isSubTag() {
        return true;
    }
}
