/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils.faces.iterator;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.faces.RenderContext;
import com.gdo.stencils.plug._PStencil;

public class GdoSelectedIterator<C extends _StencilContext, S extends _PStencil<C, S>> extends GdoIterator<C, S> {

    private static final String[] SELECTED_ITERATORS_TAGS = new String[] { "iterator:selected", "/iterator:selected" };
    private static final int[] SELECTED_ITERATORS_LENGTHS = new int[] { SELECTED_ITERATORS_TAGS[0].length(), SELECTED_ITERATORS_TAGS[1].length() };

    public GdoSelectedIterator(RenderContext<C, S> renderContext) {
        super(renderContext);
    }

    @Override
    public String[] getTags() {
        return SELECTED_ITERATORS_TAGS;
    }

    @Override
    public int[] getTagsLength() {
        return SELECTED_ITERATORS_LENGTHS;
    }

    @Override
    public boolean isSubTag() {
        return true;
    }

}
