/**
 * Copyright GDO - 2005
 */
package com.gdo.project.model;

import com.gdo.stencils.CommandStcl;
import com.gdo.stencils.StclContext;

/**
 * <p>
 * An atomic action is a command which execution terminates in one call.
 * </p>
 * <p>
 * An atomic action cannot be rendered. It has no views associated.
 * </p>
 */
public abstract class AtomicActionStcl extends CommandStcl {

    public AtomicActionStcl(StclContext stclContext) {
        super(stclContext);
    }

    @Override
    public final boolean isAtomic() {
        return true;
    }

}
