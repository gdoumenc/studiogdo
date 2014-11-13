/**
 * Copyright GDO - 2005
 */
package com.gdo.project.model;

import org.apache.commons.fileupload.FileItem;

import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.faces.RenderContext;
import com.gdo.stencils.facet.FacetResult;
import com.gdo.stencils.facet.FacetType;
import com.gdo.stencils.plug.PStcl;

/**
 * <p>
 * StepStcl represents the composed action steps.
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

public class StepStcl extends Stcl {

    private final static String PERFORMING = "performing";

    public StepStcl(StclContext stclContext) {
        super(stclContext);
    }

    // propagates to the container composed action
    @Override
    public void multipart(StclContext stclContext, String fileName, FileItem item, PStcl self) throws Exception {
        PStcl cmd = self.getContainer(stclContext);
        cmd.multipart(stclContext, fileName, item);
    }

    // returns the container composed action with step suffix
    @Override
    public FacetResult getFacet(RenderContext<StclContext, PStcl> renderContext) {

        // for flex type, the views are defined from the composed action
        String type = renderContext.getFacetType();
        String mode = renderContext.getFacetMode();
        if (FacetType.FLEX.equals(type) && PERFORMING.equals(mode)) {
            StclContext stclContext = renderContext.getStencilContext();
            PStcl self = renderContext.getStencilRendered();
            PStcl cmd = self.getContainer(stclContext);
            String m = String.format("step%s", self.getKey());
            RenderContext<StclContext, PStcl> cmdContext = new RenderContext<StclContext, PStcl>(stclContext, cmd, FacetType.FLEX, m);
            return cmd.getFacet(cmdContext);
        }

        return super.getFacet(renderContext);
    }
}
