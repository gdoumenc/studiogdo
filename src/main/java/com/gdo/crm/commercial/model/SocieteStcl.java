/**
 * Copyright GDO - 2005
 */
package com.gdo.crm.commercial.model;

import java.sql.ResultSet;
import java.sql.SQLException;

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

public class SocieteStcl extends SQLStcl {

	public interface Slot extends SQLStcl.Slot {

		String RAISON_SOCIALE = "RaisonSociale";
		String TYPE = "Type";
		String EST_UN_PROSPECT = "EstUnProspect";

		String SIRET = "Siret";
		String NAF = "Naf";
		String EFFECTIF = "Effectif";
		String CHIFFRE_D_AFFAIRE = "ChiffreDAffaire";

		String ADRESSE1 = "Adresse1";
		String ADRESSE2 = "Adresse2";
		String ADRESSE3 = "Adresse3";
		String CODE_POSTAL = "CodePostal";
		String VILLE = "Ville";
		String TELEPHONE = "Telephone";
		String FAX = "Fax";
		String EMAIL = "Email";
		String SITE_WEB = "SiteWeb";

		String POSSEDE = "Possede";
		String A_POUR_RESPONSABLE = "APourResponsable";
		String A_POUR_ACTIVITE = "APourActivite";

		String ACTION_COMMERCIALE = "ActionCommerciale";
		String COMMANDE = "Commande";

		String REFERENT = "Referent";

		String REMARQUES = "Remarques";

		String ACTIVITE_LIBELLE = "ActiviteLibelle";
		String A_POUR_RESPONSABLE_NOM = "APourResponsableNom";
	}
	
	public interface Command extends SQLStcl.Command {
		String NEW_CONTACT= "NewContact";
	}

	public SocieteStcl(StclContext stclContext) {
		super(stclContext);
		
		// SLOT PART
		
		propSlot(Slot.RAISON_SOCIALE);
		propSlot(Slot.TYPE, 0);
		propSlot(Slot.EST_UN_PROSPECT);
		
		propSlot(Slot.SIRET);
		propSlot(Slot.NAF);
		propSlot(Slot.EFFECTIF);
		propSlot(Slot.CHIFFRE_D_AFFAIRE);
		
		propSlot(Slot.ADRESSE1);
		propSlot(Slot.ADRESSE2);
		propSlot(Slot.ADRESSE3);
		propSlot(Slot.CODE_POSTAL);
		propSlot(Slot.VILLE);
		propSlot(Slot.TELEPHONE);
		propSlot(Slot.FAX);
		propSlot(Slot.EMAIL);
		propSlot(Slot.SITE_WEB);
		
		singleSlot(Slot.A_POUR_RESPONSABLE);
		singleSlot(Slot.A_POUR_ACTIVITE);
		
		createPossedeSlot(stclContext, this);
		createActionCommercialeSlot(stclContext, this);
		createCommandeSlot(stclContext, this);
		
		propSlot(Slot.REFERENT);
		propSlot(Slot.REMARQUES);
		
		propSlot(Slot.ACTIVITE_LIBELLE);
		propSlot(Slot.A_POUR_RESPONSABLE_NOM);
	}

	@Override
	public Result completeSQLStencil(StclContext stclContext, ResultSet rs, PStcl self) throws SQLException {
		Result result = super.completeSQLStencil(stclContext, rs, self);

		self.setString(stclContext, Slot.RAISON_SOCIALE, rs.getString(Slot.RAISON_SOCIALE));
		self.setInt(stclContext, Slot.TYPE, rs.getInt(Slot.TYPE));
		self.setBoolean(stclContext, Slot.EST_UN_PROSPECT, ConverterHelper.parseBoolean(rs.getString(Slot.EST_UN_PROSPECT)));

		self.setString(stclContext, Slot.SIRET, rs.getString(Slot.SIRET));
		self.setString(stclContext, Slot.NAF, rs.getString(Slot.NAF));
		self.setInt(stclContext, Slot.EFFECTIF, rs.getInt(Slot.EFFECTIF));
		self.setInt(stclContext, Slot.CHIFFRE_D_AFFAIRE, rs.getInt(Slot.CHIFFRE_D_AFFAIRE));

		self.setString(stclContext, Slot.ADRESSE1, rs.getString(Slot.ADRESSE1));
		self.setString(stclContext, Slot.ADRESSE2, rs.getString(Slot.ADRESSE2));
		self.setString(stclContext, Slot.ADRESSE3, rs.getString(Slot.ADRESSE3));
		self.setString(stclContext, Slot.CODE_POSTAL, rs.getString(Slot.CODE_POSTAL));
		self.setString(stclContext, Slot.VILLE, rs.getString(Slot.VILLE));
		self.setString(stclContext, Slot.TELEPHONE, rs.getString(Slot.TELEPHONE));
		self.setString(stclContext, Slot.FAX, rs.getString(Slot.FAX));
		self.setString(stclContext, Slot.EMAIL, rs.getString(Slot.EMAIL));
		self.setString(stclContext, Slot.SITE_WEB, rs.getString(Slot.SITE_WEB));

		plugFromId(stclContext, self.getResourceSlot(stclContext, Resource.COMMERCIAL), rs.getInt(Slot.A_POUR_RESPONSABLE), Slot.A_POUR_RESPONSABLE, self);
		plugFromId(stclContext, self.getResourceSlot(stclContext, Resource.ACTIVITE), rs.getInt(Slot.A_POUR_ACTIVITE), Slot.A_POUR_ACTIVITE, self);

		self.setString(stclContext, Slot.REFERENT, rs.getString(Slot.REFERENT));

		self.setString(stclContext, Slot.ACTIVITE_LIBELLE, rs.getString(Slot.ACTIVITE_LIBELLE));
		self.setString(stclContext, Slot.A_POUR_RESPONSABLE_NOM, rs.getString(Slot.A_POUR_RESPONSABLE_NOM));

		self.setString(stclContext, Slot.REMARQUES, rs.getString(Slot.REMARQUES));

		return result;
	}

	@Override
	public void addInSqlAssoc(StclContext stclContext, SqlAssoc assoc, PStcl stencil, PStcl container) {
		super.addInSqlAssoc(stclContext, assoc, stencil, container);

		assoc.pushString(stclContext, Slot.RAISON_SOCIALE);
		assoc.pushInt(stclContext, Slot.TYPE);
		assoc.pushBoolean(stclContext, Slot.EST_UN_PROSPECT, "1", "0");

		assoc.pushString(stclContext, Slot.SIRET);
		assoc.pushString(stclContext, Slot.NAF);
		assoc.pushInt(stclContext, Slot.EFFECTIF);
		assoc.pushInt(stclContext, Slot.CHIFFRE_D_AFFAIRE);

		assoc.pushString(stclContext, Slot.ADRESSE1);
		assoc.pushString(stclContext, Slot.ADRESSE2);
		assoc.pushString(stclContext, Slot.ADRESSE3);
		assoc.pushString(stclContext, Slot.CODE_POSTAL);
		assoc.pushString(stclContext, Slot.VILLE);
		assoc.pushString(stclContext, Slot.TELEPHONE);
		assoc.pushString(stclContext, Slot.FAX);
		assoc.pushString(stclContext, Slot.EMAIL);
		assoc.pushString(stclContext, Slot.SITE_WEB);

		assoc.pushId(stclContext, Slot.A_POUR_RESPONSABLE);
		assoc.pushId(stclContext, Slot.A_POUR_ACTIVITE);

		assoc.pushString(stclContext, Slot.REMARQUES);
	}

	protected ContactSlot createPossedeSlot(StclContext stclContext, Stcl in) {
		return new PossedeSlot(stclContext, this);
	}

	protected _ActionCommercialeSlot createActionCommercialeSlot(StclContext stclContext, Stcl in) {
		return new ActionSlot(stclContext, this);
	}

	protected CommandeSlot createCommandeSlot(StclContext stclContext, Stcl in) {
		return new CommandeDevisSlot(stclContext, this);
	}

	private class PossedeSlot extends ContactSlot {

		public PossedeSlot(StclContext stclContext, Stcl in) {
			super(stclContext, in, Slot.POSSEDE);
		}

		@Override
		public SQLCursor getCursor(StclContext stclContext, PSlot<StclContext, PStcl> self) {
			PStcl container = self.getContainer();
			SQLSlot slot = container.getResourceSlot(stclContext, Resource.CONTACT).getSlot();
			return slot.getCursor(stclContext, self);
		}

		@Override
		public String getKeysCondition(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
			String c = super.getKeysCondition(stclContext, cond, self);

			// get societe ID
			int id = self.getContainer().getInt(stclContext, Slot.ID, 0);
			return String.format("c.Societe='%s' AND %s", id, c);
		}

		@Override
		public String getKeysOrder(StclContext stclContext, PSlot<StclContext, PStcl> self) {
			return "ORDER BY c.Nom";
		}

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
			return String.format("c.Societe='%s' AND %s", id, c);
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
			return String.format("m.Societe='%s' AND %s", id, c);
		}

		@Override
		public String getKeysOrder(StclContext stclContext, PSlot<StclContext, PStcl> self) {
			return "ORDER BY c.DatePrevisionnelle";
		}

	}
}