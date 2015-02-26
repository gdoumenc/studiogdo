/**
 * Copyright GDO - 2005
 */
package com.gdo.crm.commercial.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.gdo.project.util.SqlUtils.SqlAssoc;
import com.gdo.sql.model.SQLStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PStcl;

public class ActiviteStcl extends SQLStcl {

	public interface Slot extends SQLStcl.Slot {
		String LIBELLE = "Libelle";
	}

	public ActiviteStcl(StclContext stclContext) {
		super(stclContext);
		
		propSlot(Slot.LIBELLE);
	}

	@Override
	public void addInSqlAssoc(StclContext stclContext, SqlAssoc assoc, PStcl stencil, PStcl container) {
		super.addInSqlAssoc(stclContext, assoc, stencil, container);

		assoc.pushString(stclContext, ActiviteStcl.Slot.LIBELLE);
	}

	public static Map<String, String> getPropertiesValuesFromKeyResults(StclContext stclContext, ResultSet rs) throws SQLException {
		Map<String, String> map = SQLStcl.getPropertiesValuesFromKeyResults(stclContext, rs);

		map.put(ActiviteStcl.Slot.LIBELLE, rs.getString(ActiviteStcl.Slot.LIBELLE));

		return map;
	}
}