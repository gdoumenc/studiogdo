/**
 * Copyright GDO - 2004
 */
package com.gdo.resource.model;

import java.io.InputStream;

import org.apache.commons.fileupload.FileItem;

import com.gdo.stencils.StclContext;
import com.gdo.stencils.faces.RenderContext;
import com.gdo.stencils.facet.FacetResult;
import com.gdo.stencils.facet.FacetType;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.StencilUtils;

public class FileResourceStcl extends _ResourceStcl {

    public interface Slot extends _ResourceStcl.Slot {
        String CONTENT = "Content";
        String MIME_TYPE = "MimeType";
    }

    public FileResourceStcl(StclContext stclContext) {
        super(stclContext);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gdo.project.Stcl#getFacet(com.gdo.stencils.faces.RenderContext)
     */
    @Override
    public FacetResult getFacet(RenderContext<StclContext, PStcl> renderContext) {
        StclContext stclContext = renderContext.getStencilContext();
        String type = renderContext.getFacetType();

        // gets content as file
        if (FacetType.FILE.equals(type)) {
            PStcl self = renderContext.getStencilRendered();
            String name = self.getString(stclContext, Slot.NAME, "");

            // gets length
            int length = self.getInt(stclContext, Slot.SIZE, -1);

            // creates facet result
            InputStream is = self.getInputStream(stclContext, Slot.CONTENT);
            String mime = self.getString(stclContext, Slot.MIME_TYPE, name);
            FacetResult result = new FacetResult(is, mime);

            // adds content length
            result.setContentLength(length);

            // adds file name
            result.setHeader("Content-Disposition", String.format("attachment; filename=%s", name));

            return result;
        }

        return super.getFacet(renderContext);
    }

    @Override
    public void multipart(StclContext stclContext, String fileName, FileItem item, PStcl self) throws Exception {
        PStcl file = self.getStencil(stclContext, Slot.FILE);
        if (StencilUtils.isNotNull(file)) {
            file.multipart(stclContext, fileName, item);
        }
    }

}