/**
 * Copyright GDO - 2004
 */
package com.gdo.project.util.model;

import org.apache.commons.lang3.StringUtils;

import com.gdo.stencils.Result;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.factory.StencilFactory;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.MultiCalculatedSlot;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;

public class ConstructorStcl extends Stcl {

	public interface Slot extends Stcl.Slot {
		String TEMPLATE_NAME = "TemplateName";

		String PARAM1 = "Param1";
		String PARAM2 = "Param2";

		String CREATED = "Created";
	}

	public ConstructorStcl(StclContext stclContext) {
		super(stclContext);

		new CreatedSlot(stclContext);
	}

	private class CreatedSlot extends MultiCalculatedSlot<StclContext, PStcl> {

		public CreatedSlot(StclContext stclContext) {
			super(stclContext, ConstructorStcl.this, Slot.CREATED, PSlot.ANY);
		}

		@Override
		protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
			PStcl container = self.getContainer();

			// gets template name
			String template = container.getString(stclContext, Slot.TEMPLATE_NAME, null);
			if (StringUtils.isEmpty(template)) {
				return StencilUtils.< StclContext, PStcl> iterator(Result.error("tempalte name cannot be empty in ConstructorStcl"));
			}

			StencilFactory<StclContext, PStcl> factory = (StencilFactory<StclContext, PStcl>) stclContext.getStencilFactory();
			for (PStcl param1 : container.getStencils(stclContext, Slot.PARAM1)) {
				String value = param1.getString(stclContext, PathUtils.THIS, "");
				PStcl created = factory.createPStencil(stclContext, null, Key.NO_KEY, template, value);
				addStencilInList(stclContext, created, self);
			}
			return cleanList(stclContext, cond, self);
		}

	}

}