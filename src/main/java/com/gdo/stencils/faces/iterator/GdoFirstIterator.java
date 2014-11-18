/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils.faces.iterator;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.faces.RenderContext;
import com.gdo.stencils.plug._PStencil;

public class GdoFirstIterator<C extends _StencilContext, S extends _PStencil<C, S>> extends GdoIterator<C, S> {

    private static final String[] FIRST_ITERATORS_TAGS = new String[] { "iterator:first", "/iterator:first" };
    private static final int[] FIRST_ITERATORS_LENGTHS = new int[] { FIRST_ITERATORS_TAGS[0].length(), FIRST_ITERATORS_TAGS[1].length() };

    public GdoFirstIterator(RenderContext<C, S> renderContext) {
        super(renderContext);
    }

    @Override
    public String[] getTags() {
        return FIRST_ITERATORS_TAGS;
    }

    @Override
    public int[] getTagsLength() {
        return FIRST_ITERATORS_LENGTHS;
    }

    @Override
    public boolean isSubTag() {
        return true;
    }

}
