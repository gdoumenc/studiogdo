/**
 * Copyright GDO - 2004
 */
package com.gdo.reflect;

import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;

/**
 * <p>
 * Contained package in the servlet class loader.
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
public class PackageStcl extends Stcl {

	public interface Slot extends Stcl.Slot {
		String PACKAGES = "Packages";
		String FILES = "Fiels";
	}

	public PackageStcl(StclContext stclContext) {
		super(stclContext);
	}

}