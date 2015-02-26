/**
 * Copyright GDO - 2005
 */
package com.gdo.ecommerce;

import com.gdo.ecommerce.order.model.ServiceStcl.Slot;
import com.gdo.stencils.util.PathUtils;

public class Resources {

	// resources availables
	public static final String E_COMMERCE = "E_COMMERCE";
	public static final String ORDERS = E_COMMERCE + "_ORDERS";

	// default resources path
	public static String getPath(String service, String path) {
		if (ORDERS.equals(path))
			return PathUtils.compose(service, Slot.ORDERS);
		return "";
	}

}
