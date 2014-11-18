/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils.faces.root;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.faces.FacetsRenderer;
import com.gdo.stencils.faces.RenderContext;
import com.gdo.stencils.plug._PStencil;

/**
 * <p>
 * Used only as root for tag expansion in string.
 * </p>
 */
public class GdoRootLabel<C extends _StencilContext, S extends _PStencil<C, S>> extends FacetsRenderer<C, S> {

    public GdoRootLabel(RenderContext<C, S> renderContext) {
        super(renderContext);
    }

    // no code between tag (as root used only to store children..)
    @Override
    public boolean needExpansion(C stclContext) {
        return false;
    }

}
