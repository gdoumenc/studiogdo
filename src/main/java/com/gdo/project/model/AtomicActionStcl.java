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
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
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
