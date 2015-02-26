/**
 * Copyright GDO - 2005
 */
package com.gdo.crm.commercial.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.gdo.crm.CrmResources.Resource;
import com.gdo.helper.ConverterHelper;
import com.gdo.project.util.SqlUtils.SqlAssoc;
import com.gdo.sql.model.SQLStcl;
import com.gdo.sql.slot.SQLCursor;
import com.gdo.sql.slot.SQLSlot;
import com.gdo.stencils.Result;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;

public class ContactStcl extends SQLStcl {

	public interface Slot extends SQLStcl.Slot {
		String CIVILITE = "Civilite";
		String NOM = "Nom";
		String PRENOM = "Prenom";
		String FONCTION = "Fonction";
		String ADRESSE1 = "Adresse1";
		String ADRESSE2 = "Adresse2";
		String ADRESSE3 = "Adresse3";
		String CODE_POSTAL = "CodePostal";
		String VILLE = "Ville";
		String TELEPHONE = "Telephone";
		String MOBILE = "Mobile";
		String FAX = "Fax";
		String EMAIL = "Email";
		String REFERENT = "Referent";

		String ACTION_COMMERCIALE = "ActionCommerciale";
		String COMMANDE = "Commande";

		String IDENTIFIANT_BASE = "IdentifiantBase";

		String SOCIETE = "Societe";

		String REMARQUES = "Remarques";

		String SOCIETE_RAISON_SOCIALE = "SocieteRaisonSociale";
		String SOCIETE_EST_UN_PROSPECT = "SocieteEstUnProspect";
		String SOCIETE_PRIORITE = "SocietePriorite";
		String SOCIETE_ACTIVITE_LIBELLE = "SocieteActiviteLibelle";

		String ID_SOCIETE = "IdSociete";
		String ID_COMMERCIAL_RESPONSABLE = "IdCommercialResponsable";
		String A_POUR_RESPONSABLE_NOM = "APourResponsableNom";

		String SOCIETE_ADRESSE1 = "SocieteAdresse1";
		String SOCIETE_ADRESSE2 = "SocieteAdresse2";
		String SOCIETE_ADRESSE3 = "SocieteAdresse3";
		String SOCIETE_CODE_POSTAL = "SocieteCodePostal";
		String SOCIETE_VILLE = "SocieteVille";
		String SOCIETE_TEL = "SocieteTel";

		String NOM_PRENOM = "NomPrenom";
	}

	public ContactStcl(StclContext stclContext) {
		super(stclContext);

		createActionCommercialeSlot(stclContext, this);
		createCommandeSlot(stclContext, this);
	}

	public static Map<String, String> getPropertiesValuesFromKeyResults(StclContext stclContext, ResultSet rs) throws SQLException {
		Map<String, String> map = SQLStcl.getPropertiesValuesFromKeyResults(stclContext, rs);

		String nom = rs.getString(Slot.NOM);
		map.put(Slot.NOM, nom);
		String prenom = rs.getString(Slot.PRENOM);
		map.put(Slot.PRENOM, prenom);
		map.put(Slot.NOM_PRENOM, nom + " " + prenom);
		map.put(Slot.CODE_POSTAL, rs.getString(Slot.CODE_POSTAL));
		map.put(Slot.VILLE, rs.getString(Slot.VILLE));
		map.put(Slot.TELEPHONE, rs.getString(Slot.TELEPHONE));
		map.put(Slot.MOBILE, rs.getString(Slot.MOBILE));
		map.put(Slot.FONCTION, rs.getString(Slot.FONCTION));
		map.put(Slot.EMAIL, rs.getString(Slot.EMAIL));
		map.put(Slot.SOCIETE_RAISON_SOCIALE, rs.getString(Slot.SOCIETE_RAISON_SOCIALE));
		map.put(Slot.SOCIETE_EST_UN_PROSPECT, rs.getString(Slot.SOCIETE_EST_UN_PROSPECT));
		map.put(Slot.SOCIETE_PRIORITE, rs.getString(Slot.SOCIETE_PRIORITE));
		map.put(Slot.SOCIETE_ACTIVITE_LIBELLE, rs.getString(Slot.SOCIETE_ACTIVITE_LIBELLE));
		map.put(Slot.ID_SOCIETE, rs.getString(Slot.ID_SOCIETE));
		map.put(Slot.ID_COMMERCIAL_RESPONSABLE, rs.getString(Slot.ID_COMMERCIAL_RESPONSABLE));
		map.put(Slot.IDENTIFIANT_BASE, rs.getString(Slot.IDENTIFIANT_BASE));
		map.put(Slot.A_POUR_RESPONSABLE_NOM, rs.getString(Slot.A_POUR_RESPONSABLE_NOM));
		map.put(Slot.REFERENT, rs.getString(Slot.REFERENT));

		map.put(Slot.SOCIETE_ADRESSE1, rs.getString(Slot.SOCIETE_ADRESSE1));
		map.put(Slot.SOCIETE_ADRESSE2, rs.getString(Slot.SOCIETE_ADRESSE2));
		map.put(Slot.SOCIETE_ADRESSE3, rs.getString(Slot.SOCIETE_ADRESSE3));
		map.put(Slot.SOCIETE_CODE_POSTAL, rs.getString(Slot.SOCIETE_CODE_POSTAL));
		map.put(Slot.SOCIETE_VILLE, rs.getString(Slot.SOCIETE_VILLE));
		map.put(Slot.SOCIETE_TEL, rs.getString(Slot.SOCIETE_TEL));

		return map;
	}

	@Override
	public Result completeSQLStencil(StclContext stclContext, ResultSet rs, PStcl self) throws SQLException {
		Result result = super.completeSQLStencil(stclContext, rs, self);

		self.setString(stclContext, Slot.CIVILITE, rs.getString(Slot.CIVILITE));
		self.setString(stclContext, Slot.NOM, rs.getString(Slot.NOM));
		self.setString(stclContext, Slot.PRENOM, rs.getString(Slot.PRENOM));
		self.setString(stclContext, Slot.FONCTION, rs.getString(Slot.FONCTION));
		self.setString(stclContext, Slot.ADRESSE1, rs.getString(Slot.ADRESSE1));
		self.setString(stclContext, Slot.ADRESSE2, rs.getString(Slot.ADRESSE2));
		self.setString(stclContext, Slot.ADRESSE3, rs.getString(Slot.ADRESSE3));
		self.setString(stclContext, Slot.CODE_POSTAL, rs.getString(Slot.CODE_POSTAL));
		self.setString(stclContext, Slot.VILLE, rs.getString(Slot.VILLE));
		self.setString(stclContext, Slot.TELEPHONE, rs.getString(Slot.TELEPHONE));
		self.setString(stclContext, Slot.MOBILE, rs.getString(Slot.MOBILE));
		self.setString(stclContext, Slot.FAX, rs.getString(Slot.FAX));
		self.setString(stclContext, Slot.EMAIL, rs.getString(Slot.EMAIL));
		self.setBoolean(stclContext, Slot.REFERENT, ConverterHelper.parseBoolean(rs.getString(Slot.REFERENT)));
		self.setString(stclContext, Slot.IDENTIFIANT_BASE, rs.getString(Slot.IDENTIFIANT_BASE));

		plugFromId(stclContext, self.getResourceSlot(stclContext, Resource.SOCIETE), rs.getInt(Slot.SOCIETE), Slot.SOCIETE, self);

		self.setString(stclContext, Slot.REMARQUES, rs.getString(Slot.REMARQUES));

		self.setString(stclContext, Slot.SOCIETE_RAISON_SOCIALE, rs.getString(Slot.SOCIETE_RAISON_SOCIALE));
		self.setString(stclContext, Slot.SOCIETE_EST_UN_PROSPECT, rs.getString(Slot.SOCIETE_EST_UN_PROSPECT));
		self.setString(stclContext, Slot.SOCIETE_PRIORITE, rs.getString(Slot.SOCIETE_PRIORITE));
		self.setString(stclContext, Slot.SOCIETE_ACTIVITE_LIBELLE, rs.getString(Slot.SOCIETE_ACTIVITE_LIBELLE));

		self.setInt(stclContext, Slot.ID_SOCIETE, rs.getInt(Slot.ID_SOCIETE));
		self.setInt(stclContext, Slot.ID_COMMERCIAL_RESPONSABLE, rs.getInt(Slot.ID_COMMERCIAL_RESPONSABLE));
		self.setString(stclContext, Slot.A_POUR_RESPONSABLE_NOM, rs.getString(Slot.A_POUR_RESPONSABLE_NOM));
		return result;
	}

	@Override
	public void addInSqlAssoc(StclContext stclContext, SqlAssoc assoc, PStcl stencil, PStcl container) {
		super.addInSqlAssoc(stclContext, assoc, stencil, container);

		assoc.pushEnum(stclContext, Slot.CIVILITE);
		assoc.pushString(stclContext, Slot.NOM);
		assoc.pushString(stclContext, Slot.PRENOM);
		assoc.pushString(stclContext, Slot.FONCTION);
		assoc.pushString(stclContext, Slot.ADRESSE1);
		assoc.pushString(stclContext, Slot.ADRESSE2);
		assoc.pushString(stclContext, Slot.ADRESSE3);
		assoc.pushString(stclContext, Slot.CODE_POSTAL);
		assoc.pushString(stclContext, Slot.VILLE);
		assoc.pushString(stclContext, Slot.TELEPHONE);
		assoc.pushString(stclContext, Slot.MOBILE);
		assoc.pushString(stclContext, Slot.FAX);
		assoc.pushString(stclContext, Slot.EMAIL);
		assoc.pushBoolean(stclContext, Slot.REFERENT, "1", "0");
		assoc.pushString(stclContext, Slot.IDENTIFIANT_BASE);

		assoc.pushId(stclContext, Slot.SOCIETE);
		assoc.pushString(stclContext, Slot.REMARQUES);
	}

	protected _ActionCommercialeSlot createActionCommercialeSlot(StclContext stclContext, Stcl in) {
		return new ActionSlot(stclContext, this);
	}

	protected CommandeSlot createCommandeSlot(StclContext stclContext, Stcl in) {
		return new CommandeDevisSlot(stclContext, this);
	}

	private class ActionSlot extends _ActionCommercialeSlot {

		public ActionSlot(StclContext stclContext, Stcl in) {
			super(stclContext, in, Slot.ACTION_COMMERCIALE);
		}

		@Override
		public SQLCursor getCursor(StclContext stclContext, PSlot<StclContext, PStcl> self) {
			PStcl container = self.getContainer();
			SQLSlot slot = container.getResourceSlot(stclContext, Resource.ACTION_COMMERCIALE).getSlot();
			return slot.getCursor(stclContext, self);
		}

		@Override
		public String getKeysCondition(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
			String c = super.getKeysCondition(stclContext, cond, self);

			// get societe ID
			int id = self.getContainer().getInt(stclContext, Slot.ID, 0);
			return String.format("a.EstRealiseeAvec='%s' AND %s", id, c);
		}

		@Override
		public String getKeysOrder(StclContext stclContext, PSlot<StclContext, PStcl> self) {
			return "ORDER BY a.Quand";
		}

	}

	private class CommandeDevisSlot extends CommandeSlot {

		public CommandeDevisSlot(StclContext stclContext, Stcl in) {
			super(stclContext, in, Slot.COMMANDE);
		}

		@Override
		public SQLCursor getCursor(StclContext stclContext, PSlot<StclContext, PStcl> self) {
			PStcl container = self.getContainer();
			SQLSlot slot = container.getResourceSlot(stclContext, Resource.ACTION_COMMERCIALE).getSlot();
			return slot.getCursor(stclContext, self);
		}

		@Override
		public String getKeysCondition(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
			String c = super.getKeysCondition(stclContext, cond, self);

			// get societe ID
			int id = self.getContainer().getInt(stclContext, Slot.ID, 0);
			return String.format("c.PasseAvec='%s' AND %s", id, c);
		}

		@Override
		public String getKeysOrder(StclContext stclContext, PSlot<StclContext, PStcl> self) {
			return "ORDER BY a.Quand";
		}

	}
}