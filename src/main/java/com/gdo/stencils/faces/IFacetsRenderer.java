/**
 * <p>A <tt>HtmlComponent</tt> is linked to a plugged stencil and a render context.<p>
 *
 * <blockquote>
 * <p>&copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved.
 * This software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.</p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a> href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com)</a>
 */
package com.gdo.stencils.faces;

import java.util.Map;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.facet.FacetResult;
import com.gdo.stencils.plug._PStencil;

/**
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
public interface IFacetsRenderer<C extends _StencilContext, S extends _PStencil<C, S>> {

    RenderContext<C, S> getRenderContext();

    Map<String, Object> getAttributes();

    void setAttributes(Map<String, ?> parameters);

    void init(C stclContext);

    /**
     * Returns true if the component needs to be expanded
     */
    boolean needExpansion(C stclContext);

    /**
     * Realises the expansion.
     */
    void expand(C stclContext) throws WrongTagSyntax;

    public String getHtmlContent(C stclContext);

    public FacetResult getFacet(C stclContext);
}
