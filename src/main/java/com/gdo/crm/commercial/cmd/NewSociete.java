package com.gdo.crm.commercial.cmd;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.io.IOUtils;

import com.gdo.crm.CrmResources.Resource;
import com.gdo.crm.commercial.model.ContactStcl;
import com.gdo.crm.commercial.model.ServiceStcl;
import com.gdo.crm.commercial.model.SocieteStcl;
import com.gdo.helper.StringHelper;
import com.gdo.sql.cmd.NewSQLStcl;
import com.gdo.sql.model.SQLContextStcl;
import com.gdo.sql.slot.SQLSlot;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.faces.RenderContext;
import com.gdo.stencils.facet.FacetResult;
import com.gdo.stencils.facet.FacetType;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;

public class NewSociete extends NewSQLStcl {

	public interface Slot extends NewSQLStcl.Slot {
		String CREATED_SOUNDEX = "CreatedSoundex";
		String CLOSE_SOUNDEX = "CloseSoundex";
	}

	public NewSociete(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	protected int getCreationStep() {
		return 1;
	}

	@Override
	protected int getPlugStep() {
		return 3;
	}

	@Override
	protected String getTemplate(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		String servPath = getResourcePath(stclContext, Resource.SERVICE_CRM, self);
		PStcl service = self.getStencil(stclContext, servPath);
		return service.getString(stclContext, ServiceStcl.Slot.SOCIETE_TEMPLATE);
	}

	@Override
	protected PSlot<StclContext, PStcl> getSlot(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		return self.getResourceSlot(stclContext, Resource.SOCIETE);
	}

	@Override
	protected CommandStatus<StclContext, PStcl> beforeIncrementStep(CommandContext<StclContext, PStcl> cmdContext, int increment, PStcl self) {

		// checks not already a societe with exactly the same name in database
		// before creating it
		if (getActiveStepIndex() == getCreationStep()) {
			StclContext stclContext = cmdContext.getStencilContext();

			// gets created stencil
			PStcl created = self.getStencil(stclContext, Slot.STENCIL_HOLDER);
			if (created.isNull()) {
				String msg = String.format("Internal error : %s", created.getNullReason());
				return error(cmdContext, self, msg);
			}

			// gets table name from slot
			PSlot<StclContext, PStcl> pslot = self.getResourceSlot(stclContext, Resource.SOCIETE);
			SQLSlot slot = pslot.getSlot();
			String table = slot.getTableName(stclContext, pslot);

			// creates the exact match query
			String query = getUniqueCounterQuery(stclContext, created, table);

			// does the exact match query
			try {
				PStcl sqlContainer = self.getResourceStencil(stclContext, Resource.SERVICE_CRM);
				PStcl sqlContext = sqlContainer.getStencil(stclContext, ServiceStcl.Slot.SQL_CONTEXT);
				SQLContextStcl ctxt = sqlContext.getReleasedStencil(stclContext);
				ResultSet rs = ctxt.selectQuery(stclContext, query, sqlContext);
				if (rs.next()) {
					int count = rs.getInt(1);
					if (count > 0) {
						return error(cmdContext, self, "Cette société existe déjà dans la base");
					}
				}
			} catch (Exception e) {
				String msg = String.format("Error in query %s", query);
				return error(cmdContext, self, msg);
			}

			// creates the soundex match query
			query = getSoundexQuery(stclContext, created, table);

			// does the soundex query
			String soundex = "";
			try {
				PStcl sqlContainer = self.getResourceStencil(stclContext, Resource.SERVICE_CRM);
				PStcl sqlContext = sqlContainer.getStencil(stclContext, ServiceStcl.Slot.SQL_CONTEXT);
				SQLContextStcl ctxt = sqlContext.getReleasedStencil(stclContext);
				ResultSet rs = ctxt.selectQuery(stclContext, query, sqlContext);
				while (rs.next()) {
					if (soundex.length() > 0) {
						soundex += '\n';
					}
					soundex += getCloseSoundex(stclContext, rs);
				}
				self.setString(stclContext, Slot.CLOSE_SOUNDEX, soundex);

				// creates it directly if no soundex like
				if (soundex.length() == 0) {
					doIncrement(1);
				}

				// set created soundex
				soundex = getCreatedSoundex(stclContext, created);
				self.setString(stclContext, Slot.CREATED_SOUNDEX, soundex);

			} catch (Exception e) {
				String msg = String.format("Error in query %s", query);
				return error(cmdContext, self, msg);
			}
		}

		// does super
		return super.beforeIncrementStep(cmdContext, increment, self);
	}

	@Override
	protected Result updateTablesWithTemporaryId(StclContext stclContext, PStcl sqlContext, String oldId, String newId) {
		return update(stclContext, sqlContext, oldId, newId, "contacts", ContactStcl.Slot.SOCIETE);
	}

	@Override
	protected Result deleteTableWithTemporaryId(StclContext stclContext, PStcl sqlContext, String oldId) {
		return delete(stclContext, sqlContext, oldId, "contacts", ContactStcl.Slot.SOCIETE);
	}

	@Override
	public FacetResult getFacet(RenderContext<StclContext, PStcl> renderContext) {
		String type = renderContext.getFacetType();

		// for flex type, the content is defined specifically to be able to set
		// created stencil mode
		if (FacetType.FLEX.equals(type)) {
			try {
				String flex = "com.gdo.crm.commercial.cmd::NewSociete";
				String state = renderContext.getFacetMode();
				String xml = "<flex><className>%s</className><initialState>%s</initialState></flex>";
				String facet = String.format(xml, flex, state);
				InputStream is = IOUtils.toInputStream(facet, StclContext.getCharacterEncoding());
				return new FacetResult(is, "text/plain");
			} catch (Exception e) {

			}
		}

		return super.getFacet(renderContext);
	}

	/**
	 * Gets the query to count if this company already exist.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param created
	 *          the company created.
	 * @param table
	 *          the database table containing the companies.
	 * @return the count query.
	 */
	protected String getUniqueCounterQuery(StclContext stclContext, PStcl created, String table) {
		String name = created.getString(stclContext, SocieteStcl.Slot.RAISON_SOCIALE);
		return String.format("SELECT count(*) FROM %s WHERE RaisonSociale='%s' AND  Id > 0", table, StringHelper.escapeSql(name));
	}

	/**
	 * Gets the query to retrieve close soundex companies.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param created
	 *          the company created.
	 * @param table
	 *          the database table containing the companies.
	 * @return the soundex query.
	 */
	protected String getSoundexQuery(StclContext stclContext, PStcl created, String table) {
		String name = StringHelper.escapeSql(created.getString(stclContext, SocieteStcl.Slot.RAISON_SOCIALE));
		return String.format("SELECT * FROM %s WHERE %s SOUNDS LIKE '%s' and Id > 0", table, SocieteStcl.Slot.RAISON_SOCIALE, name);
	}

	/**
	 * Returns the string identifiing the company created.
	 * 
	 * @param stclContext
	 * @param created
	 * @return
	 */
	protected String getCreatedSoundex(StclContext stclContext, PStcl created) throws SQLException {
		String name = created.getString(stclContext, SocieteStcl.Slot.RAISON_SOCIALE);
		String ad1 = created.getString(stclContext, SocieteStcl.Slot.ADRESSE1);
		String cp = created.getString(stclContext, SocieteStcl.Slot.CODE_POSTAL);
		String ville = created.getString(stclContext, SocieteStcl.Slot.VILLE);
		return String.format("%s (%s %s %s)", name, ad1, cp, ville);
	}

	/**
	 * Returns the string identifiing the close soundex companies.
	 * 
	 * @param stclContext
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	protected String getCloseSoundex(StclContext stclContext, ResultSet rs) throws SQLException {
		String name = rs.getString(SocieteStcl.Slot.RAISON_SOCIALE);
		String ad1 = rs.getString(SocieteStcl.Slot.ADRESSE1);
		String cp = rs.getString(SocieteStcl.Slot.CODE_POSTAL);
		String ville = rs.getString(SocieteStcl.Slot.VILLE);
		return String.format("%s (%s %s %s)", name, ad1, cp, ville);
	}

}