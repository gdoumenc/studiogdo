package com.gdo.ecommerce.order.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.gdo.ecommerce.Resources;
import com.gdo.sql.model.SQLStcl;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PStcl;

public abstract class OrderStcl extends SQLStcl {

	public interface Slot extends SQLStcl.Slot {
		String CLIENT = "Client";

		String PRODUITS = "Produits";
		String ADRESSE_LIVRAISON = "AdresseLivraison";
		String ADRESSE_PAIEMENT = "AdressePaiement";
	}

	public OrderStcl(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public Result completeSQLStencil(StclContext stclContext, ResultSet rs, PStcl self) throws SQLException {
		Result result = super.completeSQLStencil(stclContext, rs, self);

		plugFromId(stclContext, self.getResourceSlot(stclContext, Resources.ORDERS), rs.getInt(Slot.CLIENT), Slot.CLIENT, self);

		PStcl bill = createBillingAddress(stclContext, self);
		result = ((SQLStcl) bill.getReleasedStencil(stclContext)).completeSQLStencil(stclContext, rs, bill);
		PStcl ship = createShippingAddress(stclContext, self);
		result = ((SQLStcl) ship.getReleasedStencil(stclContext)).completeSQLStencil(stclContext, rs, ship);

		return result;
	}

	public abstract String getAllClientsPath(StclContext stclContext, PStcl self);

	public abstract PStcl createBillingAddress(StclContext stclContext, PStcl self);

	public abstract PStcl createShippingAddress(StclContext stclContext, PStcl self);
}
