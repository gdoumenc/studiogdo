package com.gdo.crm.commercial.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.gdo.crm.CrmResources.Resource;
import com.gdo.crm.commercial.model.SocieteStcl.Slot;
import com.gdo.sql.slot.SQLSlot;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;

public class SocieteSlot extends SQLSlot {

	public SocieteSlot(StclContext stclContext, Stcl in, String name) {
		super(stclContext, in, name, 10);
	}

	@Override
	public String getTableName(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		return "societes";
	}

	@Override
	public String getStencilTemplate(StclContext stclContext, ResultSet rs, PSlot<StclContext, PStcl> self) {
		PStcl service = self.getContainer().getResourceStencil(stclContext, Resource.SERVICE_CRM);
		return service.getString(stclContext, ServiceStcl.Slot.SOCIETE_TEMPLATE);
	}

	@Override
	public String getKeysSelect(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		return "s.*, a.Libelle ActiviteLibelle, c.Nom Referent, x.Nom APourResponsableNom";
	}

	protected String getCommercialJoin(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		return "LEFT JOIN commerciaux x ON s.APourResponsable = x.Id";
	}

	@Override
	public String getKeysFrom(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		SocieteSlot slot = self.getSlot();

		String from = "societes s";
		String commercial = slot.getCommercialJoin(stclContext, self);
		String referent = "LEFT JOIN contacts c ON c.Societe = s.Id AND c.Referent = 1";
		String activite = "LEFT JOIN activites a ON s.APourActivite = a.Id";
		return String.format("%s %s %s %s", from, commercial, referent, activite);
	}

	@Override
	public String getTableAliasForProperty(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		return "s";
	}

	@Override
	public String getKeysOrder(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		return "ORDER BY s.RaisonSociale";
	}

	@Override
	public Map<String, String> getPropertiesValuesFromKeyResults(StclContext stclContext, ResultSet rs, PSlot<StclContext, PStcl> self) throws SQLException {
		Map<String, String> map = super.getPropertiesValuesFromKeyResults(stclContext, rs, self);

		map.put(Slot.RAISON_SOCIALE, rs.getString(Slot.RAISON_SOCIALE));
		map.put(Slot.TELEPHONE, rs.getString(Slot.TELEPHONE));
		map.put(Slot.CODE_POSTAL, rs.getString(Slot.CODE_POSTAL));
		map.put(Slot.VILLE, rs.getString(Slot.VILLE));
		map.put(Slot.ACTIVITE_LIBELLE, rs.getString(Slot.ACTIVITE_LIBELLE));
		map.put(Slot.EST_UN_PROSPECT, rs.getString(Slot.EST_UN_PROSPECT));
		map.put(Slot.REFERENT, rs.getString(Slot.REFERENT));
		map.put(Slot.A_POUR_RESPONSABLE_NOM, rs.getString(Slot.A_POUR_RESPONSABLE_NOM));

		return map;
	}
}