/**
 * Copyright GDO - 2005
 */
package com.gdo.crm.commercial.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import com.gdo.crm.CrmResources.Resource;
import com.gdo.crm.commercial.cmd.CloseActionCommerciale;
import com.gdo.helper.ConverterHelper;
import com.gdo.project.util.SqlUtils.SqlAssoc;
import com.gdo.sql.model.SQLStcl;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PStcl;

public class _ActionCommercialeStcl extends SQLStcl {

	public interface Slot extends SQLStcl.Slot {
		String DATE_CREATION = "DateCreation";
		String QUAND = "Quand";
		String HEURE = "Heure";
		String REMARQUES = "Remarques";
		String TYPE = "Type";
		String EST_REALISEE_PAR = "EstRealiseePar";
		String EST_REALISEE_PAR1 = "EstRealiseePar1";
		String NECESSITE_PROCHAINE_ACTION = "NecessiteProchaineAction";
		String EST_REALISEE_AVEC = "EstRealiseeAvec";
		String FAITE = "Faite";
		String A_ABOUTI = "AAbouti";

		String SOCIETE = "Societe";

		String LIBELLE_TYPE = "LibelleType";
		String NOM_SOCIETE = "NomSociete";
		String NOM_CONTACT = "NomContact";
		String TELEPHONE_CONTACT = "TelephoneContact";
		String MOBILE_CONTACT = "MobileContact";
		String ID_SOCIETE = "IdSociete";
		String ID_CONTACT = "IdContact";
		String EST_REALISEE_PAR_NOM = "EstRealiseeParNom";

		String PRENOM_CONTACT = "PrenomContact";
		String FONCTION_CONTACT = "FonctionContact";
		String VILLE_CONTACT = "VilleContact";
		String CODE_POSTAL_CONTACT = "CodePostalContact";
		String SOCIETE_TEL = "SocieteTel";
	}

	public interface Command extends SQLStcl.Command {
		String CLOSE = "Close";
	}
	
	public _ActionCommercialeStcl(StclContext stclContext) {
		super(stclContext);

		// SLOT PART

		propSlot(Slot.DATE_CREATION);
		propSlot(Slot.QUAND);
		propSlot(Slot.HEURE);
		propSlot(Slot.REMARQUES);

		singleSlot(Slot.TYPE);
		singleSlot(Slot.EST_REALISEE_PAR);
		singleSlot(Slot.NECESSITE_PROCHAINE_ACTION);
		singleSlot(Slot.EST_REALISEE_AVEC);
		propSlot(Slot.FAITE);
		singleSlot(Slot.A_ABOUTI);

		singleSlot(Slot.SOCIETE);

		propSlot(Slot.LIBELLE_TYPE);
		propSlot(Slot.NOM_SOCIETE);
		propSlot(Slot.NOM_CONTACT);
		propSlot(Slot.TELEPHONE_CONTACT);
		propSlot(Slot.MOBILE_CONTACT);
		propSlot(Slot.ID_SOCIETE);
		propSlot(Slot.ID_CONTACT);
		propSlot(Slot.EST_REALISEE_PAR_NOM);
		
		propSlot(Slot.PRENOM_CONTACT);
		propSlot(Slot.FONCTION_CONTACT);
		propSlot(Slot.VILLE_CONTACT);
		propSlot(Slot.CODE_POSTAL_CONTACT);
		propSlot(Slot.SOCIETE_TEL);

		// COMMAND PART

		command(Command.CLOSE, CloseActionCommerciale.class);
	}

	@Override
	public Result completeSQLStencil(StclContext stclContext, ResultSet rs, PStcl self) throws SQLException {
		Result result = super.completeSQLStencil(stclContext, rs, self);

		if (StringUtils.isNotBlank(rs.getString(Slot.DATE_CREATION))) {
			self.setString(stclContext, Slot.DATE_CREATION, DateFormatUtils.format(rs.getDate(Slot.DATE_CREATION), "yyyy-MM-dd"));
		}
		if (StringUtils.isNotBlank(rs.getString(Slot.QUAND))) {
			self.setString(stclContext, Slot.QUAND, DateFormatUtils.format(rs.getDate(Slot.QUAND), "yyyy-MM-dd"));
		}
		self.setString(stclContext, Slot.HEURE, ConverterHelper.timeToString(rs.getTime(Slot.HEURE), "HH:mm"));
		self.setString(stclContext, Slot.REMARQUES, rs.getString(Slot.REMARQUES));
		self.setBoolean(stclContext, Slot.FAITE, ConverterHelper.parseBoolean(rs.getString(Slot.FAITE)));

		plugFromId(stclContext, self.getResourceSlot(stclContext, Resource.TYPE_ACTION), rs.getInt(Slot.TYPE), Slot.TYPE, self);
		plugFromId(stclContext, self.getResourceSlot(stclContext, Resource.COMMERCIAL), rs.getInt(Slot.EST_REALISEE_PAR), Slot.EST_REALISEE_PAR, self);
		plugFromId(stclContext, self.getResourceSlot(stclContext, Resource.ACTION_COMMERCIALE), rs.getInt(Slot.NECESSITE_PROCHAINE_ACTION), Slot.NECESSITE_PROCHAINE_ACTION, self);
		plugFromId(stclContext, self.getResourceSlot(stclContext, Resource.CONTACT), rs.getInt(Slot.EST_REALISEE_AVEC), Slot.EST_REALISEE_AVEC, self);
		plugFromId(stclContext, self.getResourceSlot(stclContext, Resource.COMMANDE), rs.getInt(Slot.A_ABOUTI), Slot.A_ABOUTI, self);

		plugFromId(stclContext, self.getResourceSlot(stclContext, Resource.COMMERCIAL), rs.getInt(Slot.EST_REALISEE_PAR1), Slot.EST_REALISEE_PAR, self);
		plugFromId(stclContext, self.getResourceSlot(stclContext, Resource.SOCIETE), rs.getInt(Slot.SOCIETE), Slot.SOCIETE, self);

		self.setString(stclContext, Slot.LIBELLE_TYPE, rs.getString(Slot.LIBELLE_TYPE));
		self.setString(stclContext, Slot.NOM_SOCIETE, rs.getString(Slot.NOM_SOCIETE));
		self.setString(stclContext, Slot.NOM_CONTACT, rs.getString(Slot.NOM_CONTACT));
		self.setString(stclContext, Slot.TELEPHONE_CONTACT, rs.getString(Slot.TELEPHONE_CONTACT));
		self.setString(stclContext, Slot.MOBILE_CONTACT, rs.getString(Slot.MOBILE_CONTACT));
		self.setInt(stclContext, Slot.ID_SOCIETE, rs.getInt(Slot.ID_SOCIETE));
		self.setInt(stclContext, Slot.ID_CONTACT, rs.getInt(Slot.ID_CONTACT));
		self.setString(stclContext, Slot.EST_REALISEE_PAR_NOM, rs.getString(Slot.EST_REALISEE_PAR_NOM));

		self.setString(stclContext, Slot.PRENOM_CONTACT, rs.getString(Slot.PRENOM_CONTACT));
		self.setString(stclContext, Slot.FONCTION_CONTACT, rs.getString(Slot.FONCTION_CONTACT));
		self.setString(stclContext, Slot.VILLE_CONTACT, rs.getString(Slot.VILLE_CONTACT));
		self.setString(stclContext, Slot.CODE_POSTAL_CONTACT, rs.getString(Slot.CODE_POSTAL_CONTACT));

		return result;
	}

	@Override
	public void addInSqlAssoc(StclContext stclContext, SqlAssoc assoc, PStcl stencil, PStcl container) {
		super.addInSqlAssoc(stclContext, assoc, stencil, container);

		assoc.pushDate(stclContext, Slot.DATE_CREATION, "yyyy-MM-dd");
		assoc.pushDate(stclContext, Slot.QUAND, "yyyy-MM-dd");
		assoc.pushTimeOrNull(stclContext, Slot.HEURE);
		assoc.pushString(stclContext, Slot.REMARQUES);
		assoc.pushBoolean(stclContext, Slot.FAITE, "1", "0");

		assoc.pushId(stclContext, Slot.TYPE);
		assoc.pushId(stclContext, Slot.EST_REALISEE_PAR);
		assoc.pushId(stclContext, Slot.NECESSITE_PROCHAINE_ACTION);
		assoc.pushId(stclContext, Slot.EST_REALISEE_AVEC);
		assoc.pushId(stclContext, Slot.A_ABOUTI);
	}
}