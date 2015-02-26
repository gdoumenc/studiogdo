package com.gdo.crm.commercial.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.gdo.crm.CrmResources.Resource;
import com.gdo.crm.commercial.model.ContactStcl.Slot;
import com.gdo.sql.model.SQLContextStcl;
import com.gdo.sql.slot.SQLSlot;
import com.gdo.stencils.Result;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;

public class ContactSlot extends SQLSlot {

	public ContactSlot(StclContext stclContext, Stcl in, String name) {
		super(stclContext, in, name, 10);
	}

	@Override
	public String getTableName(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		return "contacts";
	}

	@Override
	public String getStencilTemplate(StclContext stclContext, ResultSet rs, PSlot<StclContext, PStcl> self) {
		PStcl service = self.getContainer().getResourceStencil(stclContext, Resource.SERVICE_CRM);
		return service.getString(stclContext, ServiceStcl.Slot.CONTACT_TEMPLATE);
	}

	@Override
	public String getKeysSelect(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		return "c.*, s.RaisonSociale SocieteRaisonSociale, s.EstUnProspect SocieteEstUnProspect, s.Priorite SocietePriorite"
				+ ", a.Libelle SocieteActiviteLibelle, s.Id IdSociete, x.Id IdCommercialResponsable, x.Nom APourResponsableNom"
				+ ", s.Adresse1 SocieteAdresse1, s.Adresse2 SocieteAdresse2, s.Adresse3 SocieteAdresse3"
				+ ", s.CodePostal SocieteCodePostal, s.Ville SocieteVille, s.Telephone SocieteTel";
	}

	protected String getCommercialJoin(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		return "LEFT JOIN `commerciaux` x ON s.APourResponsable = x.Id";
	}

	@Override
	public String getKeysFrom(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		ContactSlot slot = self.getSlot();

		String from = "contacts c";
		String societe = "LEFT JOIN `societes` s ON c.Societe = s.Id";
		String commercial = slot.getCommercialJoin(stclContext, self);
		String activite = "LEFT JOIN `activites` a ON s.APourActivite = a.Id";
		return String.format("%s %s %s %s", from, societe, commercial, activite);
	}

	@Override
	public String getTableAliasForProperty(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		return "c";
	}

	@Override
	public String getKeysOrder(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		String order = super.getKeysOrder(stclContext, self);
		if (StringUtils.isNotBlank(order)) {
			return order;
		}
		return "ORDER BY s.RaisonSociale";
	}

	@Override
	public Result beforeUpdateStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {

		// removes previous referent
		boolean referent = stencil.getBoolean(stclContext, Slot.REFERENT);
		if (referent) {
			PStcl societe = stencil.getStencil(stclContext, Slot.SOCIETE);
			String sid = societe.getString(stclContext, Slot.ID);
			String query = "UPDATE contacts c JOIN societes s ON s.Id=%s AND c.Societe=s.Id AND c.Referent=1 SET c.Referent=0";
			Result result = sqlContext.call(stclContext, SQLContextStcl.Command.UPDATE_QUERY, String.format(query, sid));
			if (result.isNotSuccess()) {
				return result;
			}
		}
		return Result.success();
	}

	@Override
	public Map<String, String> getPropertiesValuesFromKeyResults(StclContext stclContext, ResultSet rs, PSlot<StclContext, PStcl> self) throws SQLException {
		return ContactStcl.getPropertiesValuesFromKeyResults(stclContext, rs);
	}

}