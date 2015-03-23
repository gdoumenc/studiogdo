/* Copyright GDO - 2005*/

package com.gdo.project.cmd;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.gdo.project.model.StepStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.faces.RenderContext;
import com.gdo.stencils.facet.FacetResult;
import com.gdo.stencils.facet.FacetType;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PStcl;

/**
 * Create a stencil, validate it and then only plug it in target slot.
 * <ol>
 * <li>stencil created class (stencil template full name)</li>
 * <li>destination slot path (where the stencil created will be plugged in -
 * relative to action)</li>
 * <li>key type (none or string, int, uniquefor unicity - default : none)</li>
 * <li>initial key value (default 1 for int, "a" for string)</li>
 * <li>initial parameter for constructor if needed</li>
 * </ol>
 */
public class CreateInOneStep extends CreateInSteps {

    public interface Slot extends CreateInSteps.Slot {
        String STCL_MODE = "StclMode"; // flex mode
    }

    public CreateInOneStep(StclContext stclContext) {
        super(stclContext);

        propSlot(Slot.STCL_MODE);
    }

    @Override
    public void complete(StclContext stclContext, PStcl self) {
        super.complete(stclContext, self);

        self.newPStencil(stclContext, Slot.STEPS, new Key(1), StepStcl.class);
    }

    @Override
    protected int getCreationStep() {
        return 1;
    }

    @Override
    protected int getPlugStep() {
        return getCreationStep();
    }

    @Override
    public FacetResult getFacet(RenderContext<StclContext, PStcl> renderContext) {
        StclContext stclContext = renderContext.getStencilContext();
        PStcl self = renderContext.getStencilRendered();
        String type = renderContext.getFacetType();

        // for flex type, the content is defined specifically to be able to set
        // created stencil mode
        if (FacetType.FLEX.equals(type)) {
            try {
                String flex = "com.gdo.project.cmd::CreateInOneStep";
                String state = renderContext.getFacetMode();
                String mode = self.getString(stclContext, Slot.STCL_MODE, "");
                String xml = "<flex><className>%s</className><initialState>%s</initialState><properties><stencilMode>%s</stencilMode></properties></flex>";
                String facet = String.format(xml, flex, state, mode);
                InputStream is = IOUtils.toInputStream(facet, StclContext.getCharacterEncoding());
                return new FacetResult(is, "text/plain");
            } catch (Exception e) {
                logError(stclContext, e.getMessage());
            }
        }

        return super.getFacet(renderContext);
    }

}
