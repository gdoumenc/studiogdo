/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils.faces;

import org.apache.commons.lang3.StringUtils;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.plug._PStencil;

/**
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */
public abstract class FacetsRendererWithContent<C extends _StencilContext, S extends _PStencil<C, S>> extends FacetsRenderer<C, S> {

    private String _content; // code under tag with sub tags (iterator, visible,

    // ...)

    protected FacetsRendererWithContent(RenderContext<C, S> renderContext) {
        super(renderContext);
    }

    public abstract String[] getTags();

    public abstract int[] getTagsLength();

    public String getContent() {
        return _content;
    }

    public void setContent(String content) {
        _content = content;
    }

    @Override
    public String getHtmlContent(C stclContext) {
        String content = _content;
        if (StringUtils.isEmpty(content)) {
            if (getLog().isWarnEnabled())
                getLog().warn(stclContext, "iterator tag with no content...");
            content = ""; // at least not null;
        }
        return content;
    }

}
