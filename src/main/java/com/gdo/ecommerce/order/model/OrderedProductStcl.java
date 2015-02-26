package com.gdo.ecommerce.order.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.gdo.sql.model.SQLStcl;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PStcl;

public class OrderedProductStcl extends SQLStcl {

	public interface Slot extends SQLStcl.Slot {
		String PRODUIT = "Produit";
		String QUANTITE = "Quantite";
		String PRIX_TTC = "PrixTTC";
	}

	public OrderedProductStcl(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public Result completeSQLStencil(StclContext stclContext, ResultSet rs, PStcl self) throws SQLException {
		Result result = super.completeSQLStencil(stclContext, rs, self);

		// plugFromId(stclContext, ProjectStcl.Path.ALL_COMPLETE_PRODUCTS,
		// rs.getInt(Slot.PRODUIT), Slot.PRODUIT, self);

		self.setInt(stclContext, Slot.QUANTITE, rs.getInt(Slot.QUANTITE));
		self.setInt(stclContext, Slot.PRIX_TTC, rs.getInt(Slot.PRIX_TTC));

		return result;
	}
}
