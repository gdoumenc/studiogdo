package com.gdo.crm.commercial.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.gdo.crm.CrmResources.Resource;
import com.gdo.sql.slot.SQLSlot;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;

public class CommercialSlot extends SQLSlot {

	public CommercialSlot(StclContext stclContext, Stcl in, String name) {
		super(stclContext, in, name, 10);
	}

	@Override
	public String getTableName(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		return "commerciaux";
	}

	@Override
	public String getStencilTemplate(StclContext stclContext, ResultSet rs, PSlot<StclContext, PStcl> self) {
		PStcl service = self.getContainer().getResourceStencil(stclContext, Resource.SERVICE_CRM);
		return service.getString(stclContext, ServiceStcl.Slot.COMMERCIAL_TEMPLATE);
	}

	@Override
	public String getKeysOrder(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		String order = super.getKeysOrder(stclContext, self);
		if (StringUtils.isNotBlank(order)) {
			return order;
		}
		return "ORDER BY Nom ASC";
	}

	@Override
	public Map<String, String> getPropertiesValuesFromKeyResults(StclContext stclContext, ResultSet rs, PSlot<StclContext, PStcl> self) throws SQLException {
		Map<String, String> map = super.getPropertiesValuesFromKeyResults(stclContext, rs, self);

		map.put(CommercialStcl.Slot.NOM, rs.getString(CommercialStcl.Slot.NOM));
		map.put(CommercialStcl.Slot.PRENOM, rs.getString(CommercialStcl.Slot.PRENOM));
		map.put(CommercialStcl.Slot.NAME, rs.getString(CommercialStcl.Slot.NAME));
		map.put(CommercialStcl.Slot.PASSWD, rs.getString(CommercialStcl.Slot.PASSWD));
		map.put(CommercialStcl.Slot.MODE, rs.getString(CommercialStcl.Slot.MODE));

		return map;
	}

}