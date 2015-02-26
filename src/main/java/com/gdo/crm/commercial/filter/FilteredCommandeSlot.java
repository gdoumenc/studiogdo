package com.gdo.crm.commercial.filter;

import com.gdo.crm.CrmResources.Resource;
import com.gdo.crm.commercial.model.CommandeSlot;
import com.gdo.sql.slot.SQLCursor;
import com.gdo.sql.slot.SQLSlot;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;

public class FilteredCommandeSlot extends CommandeSlot {

	protected _Filter _cond;

	public FilteredCommandeSlot(StclContext stclContext, Stcl in, String name) {
		super(stclContext, in, name);
		this._cond = new CommandeFilter();
	}

	@Override
	public SQLCursor getCursor(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		PStcl container = self.getContainer();
		SQLSlot slot = container.getResourceSlot(stclContext, Resource.COMMANDE).getSlot();
		return slot.getCursor(stclContext, self);
	}

	@Override
	public String getKeysCondition(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
		String c = super.getKeysCondition(stclContext, cond, self);

		// add societes filter
		return this._cond.getKeysCondition(stclContext, c, self);
	}

}