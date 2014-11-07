/**
 * Copyright GDO - 2004
 */
package com.gdo.mail.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Writer;

import com.gdo.project.util.model.NamedStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.faces.RenderContext;
import com.gdo.stencils.facet.FacetResult;
import com.gdo.stencils.facet.FacetType;
import com.gdo.stencils.plug.PStcl;

public class IMAPResourceStcl extends NamedStcl {

    private Writer _out;

    public interface Slot extends NamedStcl.Slot {
        String CONTENT = "Content";
        String SIZE = "Size";
    }

    public IMAPResourceStcl(StclContext stclContext, Writer out) {
        super(stclContext);
        _out = out;
    }

    @Override
    public FacetResult getFacet(RenderContext<StclContext, PStcl> renderContext) {
        String type = renderContext.getFacetType();
        if (FacetType.FILE.equals(type)) {
            InputStream reader = new ByteArrayInputStream(_out.toString().getBytes());
            return new FacetResult(reader, "");
        }
        return super.getFacet(renderContext);
    }

}