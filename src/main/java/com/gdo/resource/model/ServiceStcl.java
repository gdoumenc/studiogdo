/**
 * Copyright GDO - 2005
 */
package com.gdo.resource.model;

import com.gdo.stencils.StclContext;

public class ServiceStcl extends com.gdo.project.model.ServiceStcl {

    public interface Slot extends com.gdo.project.model.ServiceStcl.Slot {
        String CONTEXTS = "Contexts";

        String RESOURCES_MANAGERS = "ResourcesMgrs";
    }

    public ServiceStcl(StclContext stclContext) {
        super(stclContext);

    }

}
