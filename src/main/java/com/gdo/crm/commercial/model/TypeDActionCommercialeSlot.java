package com.gdo.crm.commercial.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.gdo.crm.CrmResources.Resource;
import com.gdo.crm.commercial.model.ServiceStcl.Slot;
import com.gdo.sql.slot.SQLSlot;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;

public class TypeDActionCommercialeSlot extends SQLSlot {

	public TypeDActionCommercialeSlot(StclContext stclContext, Stcl in) {
		super(stclContext, in, Slot.TYPES_D_ACTION_COMMERCIALE, 10);
	}

	@Override
	public String getTableName(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		return "types_d_action_commerciale";
	}

	@Override
	public String getStencilTemplate(StclContext stclContext, ResultSet rs, PSlot<StclContext, PStcl> self) {
		PStcl service = self.getContainer().getResourceStencil(stclContext, Resource.SERVICE_CRM);
		return service.getString(stclContext, ServiceStcl.Slot.TYPE_D_ACTION_COMMERCIALE_TEMPLATE);
	}

	@Override
	public Map<String, String> getPropertiesValuesFromKeyResults(StclContext stclContext, ResultSet rs, PSlot<StclContext, PStcl> self) throws SQLException {
		Map<String, String> map = super.getPropertiesValuesFromKeyResults(stclContext, rs, self);

		map.put(TypeDActionCommercialeStcl.Slot.LIBELLE, rs.getString(TypeDActionCommercialeStcl.Slot.LIBELLE));

		return map;
	}
}