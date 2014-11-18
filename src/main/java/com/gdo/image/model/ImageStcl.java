/**
 * Copyright GDO - 2005
 */
package com.gdo.image.model;

import com.gdo.resource.model.FileResourceStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.StencilUtils;

public class ImageStcl extends FileResourceStcl {

    private PStcl _file; // used by creator

    public interface Slot extends FileResourceStcl.Slot {
        String HEIGHT = "Height";
        String WIDTH = "Width";
    }

    public ImageStcl(StclContext stclContext) {
        super(stclContext);
    }

    public ImageStcl(StclContext stclContext, PStcl file) {
        super(stclContext);
        _file = file;
    }

    @Override
    public void afterCompleted(StclContext stclContext, PStcl self) {
        if (StencilUtils.isNotNull(_file)) {
            self.plug(stclContext, _file, Slot.FILE);
        }
    }

}
