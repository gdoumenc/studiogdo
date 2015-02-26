/**
 * Copyright GDO - 2005
 */
package com.gdo.website;

import com.gdo.site.model.ServiceStcl.Slot;
import com.gdo.stencils.util.PathUtils;

public class Resources {

	// resources availables
	public static final String SITE = "SITE";
	public static final String PAGES = SITE + "_PAGES";

	// default resources path
	public static String getPath(String service, String path) {
		if (PAGES.equals(path))
			return PathUtils.compose(service, Slot.PAGES);
		return "";
	}

}
