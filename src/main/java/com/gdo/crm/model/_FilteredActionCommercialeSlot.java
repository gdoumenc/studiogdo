package com.gdo.crm.model;

import com.gdo.crm.CrmResources.Resource;
import com.gdo.crm.commercial.model._ActionCommercialeSlot;
import com.gdo.sql.slot.SQLCursor;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;

public abstract class _FilteredActionCommercialeSlot extends _ActionCommercialeSlot {

	protected _Filter _cond;

	public _FilteredActionCommercialeSlot(StclContext stclContext, Stcl in, String name) {
		super(stclContext, in, name);
		this._cond = getFilter();
	}

	public _ActionCommercialeSlot getParentSlot(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		PStcl container = self.getContainer();
		return container.getResourceSlot(stclContext, Resource.ACTION_COMMERCIALE).getSlot();
	}

	@Override
	public SQLCursor getCursor(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		return getParentSlot(stclContext, self).getCursor(stclContext, self);
	}

	@Override
	public String getKeysCondition(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
		String c = super.getKeysCondition(stclContext, cond, self);

		// add societes filter
		return this._cond.getKeysCondition(stclContext, c, self);
	}

	protected abstract _Filter getFilter();
}