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
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a>
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
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
