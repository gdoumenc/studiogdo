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
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo & Guillaume Doumenc. Use
 * is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */
public abstract class AttachmentStcl extends NamedStcl implements DataSource {

	public interface Slot extends NamedStcl.Slot {
		String TYPE = "Type";
	}

	public AttachmentStcl(StclContext stclContext) {
		super(stclContext);
	}
}