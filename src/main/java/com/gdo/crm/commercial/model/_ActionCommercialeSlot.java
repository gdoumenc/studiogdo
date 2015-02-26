package com.gdo.crm.commercial.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.gdo.crm.CrmResources.Resource;
import com.gdo.crm.commercial.model._ActionCommercialeStcl.Slot;
import com.gdo.sql.slot.SQLSlot;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;

public class _ActionCommercialeSlot extends SQLSlot {

	public _ActionCommercialeSlot(StclContext stclContext, Stcl in, String name) {
		super(stclContext, in, name, 50);
	}

	@Override
	public String getTableName(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		return "actions_commerciales";
	}

	@Override
	public String getStencilTemplate(StclContext stclContext, ResultSet rs, PSlot<StclContext, PStcl> self) {
		PStcl service = self.getContainer().getResourceStencil(stclContext, Resource.SERVICE_CRM);
		return service.getString(stclContext, ServiceStcl.Slot.ACTION_COMMERCIALE_TEMPLATE);
	}

	@Override
	public String getKeysSelect(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		return "a.*, x.Id EstRealiseePar1, s.Id IdSociete, c.Id IdContact, s.Id Societe, s.RaisonSociale NomSociete, c.Nom NomContact"
				+ ", t.Libelle LibelleType, c.telephone TelephoneContact, c.Mobile MobileContact, x.Nom EstRealiseeParNom"
				+ ", c.Prenom PrenomContact, c.Fonction FonctionContact"
				+ ", c.Ville VilleContact, c.CodePostal CodePostalContact, s.Telephone SocieteTel";
	}

	@Override
	public String getTableAliasForProperty(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		return "a";
	}

	protected String getCommercialJoin(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		return "LEFT JOIN commerciaux x ON s.APourResponsable = x.Id";
	}

	@Override
	public String getKeysFrom(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		_ActionCommercialeSlot slot = self.getSlot();

		String from = "actions_commerciales a";
		String contact = "LEFT JOIN contacts c ON a.EstRealiseeAvec = c.Id";
		String societe = "LEFT JOIN societes s ON c.Societe = s.Id";
		String commercial = slot.getCommercialJoin(stclContext, self);
		String type = "LEFT JOIN types_d_action_commerciale t ON a.Type = t.Id";
		return String.format("%s %s %s %s %s", from, contact, societe, commercial, type);
	}

	@Override
	public Map<String, String> getPropertiesValuesFromKeyResults(StclContext stclContext, ResultSet rs, PSlot<StclContext, PStcl> self) throws SQLException {
		Map<String, String> map = super.getPropertiesValuesFromKeyResults(stclContext, rs, self);

		map.put(Slot.DATE_CREATION, rs.getString(Slot.DATE_CREATION));
		map.put(Slot.QUAND, rs.getString(Slot.QUAND));
		map.put(Slot.HEURE, rs.getString(Slot.HEURE));
		map.put(Slot.FAITE, rs.getString(Slot.FAITE));

		map.put(Slot.LIBELLE_TYPE, rs.getString(Slot.LIBELLE_TYPE));
		map.put(Slot.NOM_SOCIETE, rs.getString(Slot.NOM_SOCIETE));
		map.put(Slot.NOM_CONTACT, rs.getString(Slot.NOM_CONTACT));
		map.put(Slot.TELEPHONE_CONTACT, rs.getString(Slot.TELEPHONE_CONTACT));
		map.put(Slot.MOBILE_CONTACT, rs.getString(Slot.MOBILE_CONTACT));
		map.put(Slot.REMARQUES, rs.getString(Slot.REMARQUES));
		map.put(Slot.ID_SOCIETE, rs.getString(Slot.ID_SOCIETE));
		map.put(Slot.ID_CONTACT, rs.getString(Slot.ID_CONTACT));
		map.put(Slot.EST_REALISEE_PAR_NOM, rs.getString(Slot.EST_REALISEE_PAR_NOM));

		map.put(Slot.PRENOM_CONTACT, rs.getString(Slot.PRENOM_CONTACT));
		map.put(Slot.FONCTION_CONTACT, rs.getString(Slot.FONCTION_CONTACT));
		map.put(Slot.VILLE_CONTACT, rs.getString(Slot.VILLE_CONTACT));
		map.put(Slot.CODE_POSTAL_CONTACT, rs.getString(Slot.CODE_POSTAL_CONTACT));
		map.put(Slot.SOCIETE_TEL, rs.getString(Slot.SOCIETE_TEL));

		return map;
	}

	@Override
	public String getKeysOrder(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		String order = super.getKeysOrder(stclContext, self);
		if (StringUtils.isNotBlank(order)) {
			return order;
		}
		return "ORDER BY a.Quand ASC, a.Heure ASC";
	}

}