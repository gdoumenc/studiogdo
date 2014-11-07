/**
 * Copyright GDO - 2005
 */
package com.gdo.context.model;

import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;

public abstract class ContextStcl extends Stcl {

    public interface Slot extends FolderStcl.Slot {
    }

    public interface Command extends FolderStcl.Command {
        String TEST_CONNEXION = "TestConnexion";
    }

    public ContextStcl(StclContext stclContext) {
        super(stclContext);
    }
}