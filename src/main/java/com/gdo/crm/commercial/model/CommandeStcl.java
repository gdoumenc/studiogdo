/**
 * Copyright GDO - 2005
 */
package com.gdo.crm.commercial.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import com.gdo.crm.CrmResources.Resource;
import com.gdo.helper.ConverterHelper;
import com.gdo.project.util.SqlUtils.SqlAssoc;
import com.gdo.sql.model.SQLStcl;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.PathUtils;

public class CommandeStcl extends SQLStcl {

	public interface Slot extends SQLStcl.Slot {
		String LIBELLE = "Libelle";
		String COMMENTAIRE = "Commentaire";
		String MONTANT_HT = "MontantHT";
		String DATE = "Date";
		String DATE_PREVISIONNELLE = "DatePrevisionnelle";
		String EST_UN_DEVIS = "EstUnDevis";
		String PERDU = "Perdu";
		String TYPE_PERTE = "TypePerte";
		String RAISON_PERTE = "RaisonPerte";
		String TYPE_GAIN = "TypeGain";
		String RAISON_GAIN = "RaisonGain";

		String PASSE_AVEC = "PasseAvec";

		String COMMERCIAL = "Commercial";
		String SOCIETE = "Societe";

		String ID_SOCIETE = "IdSociete";
		String NOM_SOCIETE = "NomSociete";
		String NOM_CONTACT = "NomContact";
		String NOM_COMMERCIAL = "NomCommercial";
	}

	public CommandeStcl(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public void afterCompleted(StclContext stclContext, PStcl self) {

		// set date now
		String now = self.getString(stclContext, PathUtils.createPath(Resource.DATE_NOW, "yyyy-MM-dd"));
		self.setString(stclContext, CommandeStcl.Slot.DATE, now);

		// add commercial
		PStcl com = self.getResourceStencil(stclContext, Resource.COMMERCIAL_CONNECTED);
		self.plug(stclContext, com, CommandeStcl.Slot.COMMERCIAL);

		super.afterCompleted(stclContext, self);
	}

	@Override
	public Result completeSQLStencil(StclContext stclContext, ResultSet rs, PStcl self) throws SQLException {
		Result result = super.completeSQLStencil(stclContext, rs, self);

		self.setString(stclContext, Slot.LIBELLE, rs.getString(Slot.LIBELLE));
		self.setString(stclContext, Slot.COMMENTAIRE, rs.getString(Slot.COMMENTAIRE));
		self.setInt(stclContext, Slot.MONTANT_HT, rs.getInt(Slot.MONTANT_HT));
		if (StringUtils.isNotBlank(rs.getString(Slot.DATE))) {
			self.setString(stclContext, Slot.DATE, DateFormatUtils.format(rs.getDate(Slot.DATE), "yyyy-MM-dd"));
		}
		if (StringUtils.isNotBlank(rs.getString(Slot.DATE_PREVISIONNELLE))) {
			self.setString(stclContext, Slot.DATE_PREVISIONNELLE, DateFormatUtils.format(rs.getDate(Slot.DATE_PREVISIONNELLE), "yyyy-MM-dd"));
		}
		self.setBoolean(stclContext, Slot.EST_UN_DEVIS, ConverterHelper.parseBoolean(rs.getString(Slot.EST_UN_DEVIS)));
		self.setBoolean(stclContext, Slot.PERDU, ConverterHelper.parseBoolean(rs.getString(Slot.PERDU)));
		self.setString(stclContext, Slot.TYPE_PERTE, rs.getString(Slot.TYPE_PERTE));
		self.setString(stclContext, Slot.RAISON_PERTE, rs.getString(Slot.RAISON_PERTE));
		self.setString(stclContext, Slot.TYPE_GAIN, rs.getString(Slot.TYPE_GAIN));
		self.setString(stclContext, Slot.RAISON_GAIN, rs.getString(Slot.RAISON_GAIN));

		plugFromId(stclContext, self.getResourceSlot(stclContext, Resource.CONTACT), rs.getInt(Slot.PASSE_AVEC), Slot.PASSE_AVEC, self);

		plugFromId(stclContext, self.getResourceSlot(stclContext, Resource.COMMERCIAL), rs.getInt(Slot.COMMERCIAL), Slot.COMMERCIAL, self);
		plugFromId(stclContext, self.getResourceSlot(stclContext, Resource.SOCIETE), rs.getInt(Slot.SOCIETE), Slot.SOCIETE, self);

		self.setInt(stclContext, Slot.ID_SOCIETE, rs.getInt(Slot.SOCIETE));
		self.setString(stclContext, Slot.NOM_SOCIETE, rs.getString(Slot.NOM_SOCIETE));
		self.setString(stclContext, Slot.NOM_CONTACT, rs.getString(Slot.NOM_CONTACT));
		self.setString(stclContext, Slot.NOM_COMMERCIAL, rs.getString(Slot.NOM_COMMERCIAL));

		return result;
	}

	@Override
	public void addInSqlAssoc(StclContext stclContext, SqlAssoc assoc, PStcl stencil, PStcl container) {
		super.addInSqlAssoc(stclContext, assoc, stencil, container);

		assoc.pushString(stclContext, Slot.LIBELLE);
		assoc.pushString(stclContext, Slot.COMMENTAIRE);
		assoc.pushInt(stclContext, Slot.MONTANT_HT);
		assoc.pushDate(stclContext, Slot.DATE, "yyyy-MM-dd");
		assoc.pushDate(stclContext, Slot.DATE_PREVISIONNELLE, "yyyy-MM-dd");
		assoc.pushBoolean(stclContext, Slot.EST_UN_DEVIS, "1", "0");
		assoc.pushBoolean(stclContext, Slot.PERDU, "1", "0");
		assoc.pushString(stclContext, Slot.TYPE_PERTE);
		assoc.pushString(stclContext, Slot.RAISON_PERTE);
		assoc.pushString(stclContext, Slot.TYPE_GAIN);
		assoc.pushString(stclContext, Slot.RAISON_GAIN);

		assoc.pushId(stclContext, Slot.PASSE_AVEC);
	}
}