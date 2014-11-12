/**
 * Copyright GDO - 2004
 */
package com.gdo.user.model;

import com.gdo.project.util.model.NamedStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PSlot;

public class ProfileStcl extends NamedStcl {

    public interface Slot extends NamedStcl.Slot {
        String ID = "Id";
        String USERS = "Users";
    }

    public ProfileStcl(StclContext stclContext) {
        super(stclContext);

        propSlot(Slot.ID);
        multiSlot(Slot.USERS, PSlot.ANY, true, null);
    }
}