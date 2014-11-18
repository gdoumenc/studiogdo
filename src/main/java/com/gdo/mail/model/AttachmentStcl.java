/**
 * Copyright GDO - 2003
 */
package com.gdo.mail.model;

import javax.activation.DataSource;

import com.gdo.project.util.model.NamedStcl;
import com.gdo.stencils.StclContext;

/**
 * <p>
 * Mail attachement.
 * </p>
 */
public abstract class AttachmentStcl extends NamedStcl implements DataSource {

    public interface Slot extends NamedStcl.Slot {
        String TYPE = "Type";
    }

    public AttachmentStcl(StclContext stclContext) {
        super(stclContext);
    }
}