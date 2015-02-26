/**
 * Copyright GDO - 2005
 */
package com.gdo.inscription.site;

import org.apache.commons.lang3.StringUtils;

import com.gdo.project.util.model.NamedStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PStcl;

public class ToolTipStcl extends NamedStcl {

	public interface Slot extends NamedStcl.Slot {
	}

	public ToolTipStcl(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public String getName(StclContext stclContext, PStcl self) {
		String name = getJavaName(stclContext, self);
		return (StringUtils.isEmpty(name)) ? "" : name;
	}

}
