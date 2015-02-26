package com.gdo.ecommerce.order.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.gdo.sql.model.SQLStcl;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PStcl;

public abstract class AddressStcl extends SQLStcl {

	public interface Slot extends SQLStcl.Slot {
		String NOM = "Nom";
		String PRENOM = "Prenom";
		String ADRESSE1 = "Adresse1";
		String ADRESSE2 = "Adresse2";
		String CODE_POSTAL = "CodePostal";
		String VILLE = "Ville";
		String PAYS = "Pays";
		String TELEPHONE = "Telephone";
	}

	public AddressStcl(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public Result completeSQLStencil(StclContext stclContext, ResultSet rs, PStcl self) throws SQLException {
		Result result = super.completeSQLStencil(stclContext, rs, self);

		self.setString(stclContext, Slot.NOM, rs.getString(fieldLabel(Slot.NOM)));
		self.setString(stclContext, Slot.PRENOM, rs.getString(fieldLabel(Slot.PRENOM)));
		self.setString(stclContext, Slot.ADRESSE1, rs.getString(fieldLabel(Slot.ADRESSE1)));
		self.setString(stclContext, Slot.ADRESSE2, rs.getString(fieldLabel(Slot.ADRESSE2)));
		self.setString(stclContext, Slot.CODE_POSTAL, rs.getString(fieldLabel(Slot.CODE_POSTAL)));
		self.setString(stclContext, Slot.VILLE, rs.getString(fieldLabel(Slot.VILLE)));
		self.setString(stclContext, Slot.PAYS, rs.getString(fieldLabel(Slot.PAYS)));
		self.setString(stclContext, Slot.TELEPHONE, rs.getString(fieldLabel(Slot.TELEPHONE)));

		return result;
	}

	public abstract String fieldLabel(String field);

}
