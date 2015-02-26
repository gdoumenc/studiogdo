package com.gdo.crm.commercial.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.gdo.crm.CrmResources.Resource;
import com.gdo.crm.commercial.model.CommandeStcl.Slot;
import com.gdo.sql.slot.SQLSlot;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;

public class CommandeSlot extends SQLSlot {

	public CommandeSlot(StclContext stclContext, Stcl in, String name) {
		super(stclContext, in, name, 10);
	}

	@Override
	public String getTableName(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		return "commandes";
	}

	@Override
	public String getStencilTemplate(StclContext stclContext, ResultSet rs, PSlot<StclContext, PStcl> self) {
		PStcl service = self.getContainer().getResourceStencil(stclContext, Resource.SERVICE_CRM);
		return service.getString(stclContext, ServiceStcl.Slot.COMMANDE_TEMPLATE);
	}

	@Override
	public String getTableAliasForProperty(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		return "c";
	}

	@Override
	public String getKeysSelect(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		return "c.*, m.Nom NomContact, s.RaisonSociale NomSociete, s.Id Societe"
				+ ", x.Id Commercial, x.Nom NomCommercial";
	}

	@Override
	public String getKeysFrom(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		return "`commandes` c LEFT JOIN `contacts` m ON c.PasseAvec = m.Id"
				+ " LEFT JOIN `societes` s ON m.Societe = s.Id"
				+ " LEFT JOIN `commerciaux` x ON s.APourResponsable = x.Id";
	}

	@Override
	public String getKeysIdField(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		return "c.Id";
	}

	@Override
	public String getKeysOrder(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		return "ORDER BY c.DatePrevisionnelle ASC";
	}

	@Override
	public Map<String, String> getPropertiesValuesFromKeyResults(StclContext stclContext, ResultSet rs, PSlot<StclContext, PStcl> self) throws SQLException {
		Map<String, String> map = super.getPropertiesValuesFromKeyResults(stclContext, rs, self);

		map.put(Slot.DATE, rs.getString(Slot.DATE));
		map.put(Slot.DATE_PREVISIONNELLE, rs.getString(Slot.DATE_PREVISIONNELLE));
		map.put(Slot.EST_UN_DEVIS, rs.getString(Slot.EST_UN_DEVIS));
		map.put(Slot.PERDU, rs.getString(Slot.PERDU));
		map.put(Slot.MONTANT_HT, rs.getString(Slot.MONTANT_HT));
		map.put(Slot.NOM_SOCIETE, rs.getString(Slot.NOM_SOCIETE));
		map.put(Slot.NOM_CONTACT, rs.getString(Slot.NOM_CONTACT));
		map.put(Slot.NOM_COMMERCIAL, rs.getString(Slot.NOM_COMMERCIAL));
		map.put(Slot.LIBELLE, rs.getString(Slot.LIBELLE));

		return map;
	}
}