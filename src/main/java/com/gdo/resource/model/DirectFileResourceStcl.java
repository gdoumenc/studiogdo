/**
 * Copyright GDO - 2004
 */
package com.gdo.resource.model;

import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.StencilUtils;

public class DirectFileResourceStcl extends FileResourceStcl {

    public interface Slot extends FileResourceStcl.Slot {
        String FILE_ENCAPSULATED = "FileEncapsulated";
    }

    private PStcl _stencil; // used by creator

    public DirectFileResourceStcl(StclContext stclContext) {
        super(stclContext);
        
        singleSlot(Slot.FILE_ENCAPSULATED);
        delegateSlot(Slot.FILE, Slot.FILE_ENCAPSULATED);
    }

    public DirectFileResourceStcl(StclContext stclContext, PStcl stencil) {
        this(stclContext);
        _stencil = stencil;
    }

    @Override
    public void afterCompleted(StclContext stclContext, PStcl self) {
        if (StencilUtils.isNotNull(_stencil)) {
            self.plug(stclContext, _stencil, Slot.FILE_ENCAPSULATED);
        }
    }

    /**
     * The name cannot be defined locally on stencil as this stencil is created
     * from the file.
     */
    @Override
    public String getName(StclContext stclContext, PStcl self) {
        PStcl file = getStencil(stclContext, Slot.FILE, self);
        if (StencilUtils.isNotNull(file))
            return file.getName(stclContext);
        return super.getName(stclContext, self);
    }

}