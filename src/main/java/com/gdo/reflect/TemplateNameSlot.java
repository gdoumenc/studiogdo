/**
 * Copyright GDO - 2005
 */
package com.gdo.reflect;

import com.gdo.stencils.StclContext;
import com.gdo.stencils._Stencil;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.CalculatedStringPropertySlot;

/**
 * Slot class to retrieve the template name of the container stencil.
 */
public class TemplateNameSlot extends CalculatedStringPropertySlot<StclContext, PStcl> {

	public TemplateNameSlot(StclContext stclContext, _Stencil<StclContext, PStcl> stcl, String name) {
		super(stclContext, stcl, name);
	}

	@Override
	public String getValue(StclContext stclContext, PStcl self) {
		PStcl container = self.getContainer(stclContext);
		return container.getTemplateName(stclContext);
	}
}
