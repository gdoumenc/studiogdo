/**
 * Copyright GDO - 2005
 */
package com.gdo.crm.commercial.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.gdo.project.util.SqlUtils.SqlAssoc;
import com.gdo.sql.model.SQLStcl;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PStcl;

public class TypeDActionCommercialeStcl extends SQLStcl {

	public interface Slot extends SQLStcl.Slot {
		String LIBELLE = "Libelle";
	}

	public TypeDActionCommercialeStcl(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public Result completeSQLStencil(StclContext stclContext, ResultSet rs, PStcl self) throws SQLException {
		Result result = super.completeSQLStencil(stclContext, rs, self);

		self.setString(stclContext, Slot.LIBELLE, rs.getString(Slot.LIBELLE));

		return result;
	}

	@Override
	public void addInSqlAssoc(StclContext stclContext, SqlAssoc assoc, PStcl stencil, PStcl container) {
		super.addInSqlAssoc(stclContext, assoc, stencil, container);

		assoc.pushString(stclContext, TypeDActionCommercialeStcl.Slot.LIBELLE);
	}
}