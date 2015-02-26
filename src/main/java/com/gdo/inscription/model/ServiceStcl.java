/**
 * Copyright GDO - 2005
 */
package com.gdo.inscription.model;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.gdo.mail.model.RecipientStcl;
import com.gdo.project.util.SqlUtils;
import com.gdo.sql.model.SQLContextStcl;
import com.gdo.sql.model.SQLServiceStcl;
import com.gdo.sql.slot.SQLSlot;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.factory.StencilFactory;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.MultiCalculatedSlot;
import com.gdo.stencils.util.StencilUtils;

/**
 * <p>
 * Service to manage a list of inscriptions in a database.
 * </p>
 * 
 * <blockquote>
 * <p>
 * &copy; 2004, 2005 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo & Guillaume Doumenc. Use
 * is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */
public class ServiceStcl extends SQLServiceStcl {

	public interface Slot extends SQLServiceStcl.Slot {
		String INSCRIPTION_TABLE = "InscriptionTable";
		String INSCRIPTIONS = "Inscriptions";

		String EMAILS = "EMails";
		String DISTRIBUTION_LIST = "DistributionList";
		String MAIL = "Mail";
	}

	protected InscriptionsSlot _inscriptionsSlot;
	protected EmailsSlot _emailsSlot;

	public ServiceStcl(StclContext stclContext) {
		super(stclContext);

		this._inscriptionsSlot = new InscriptionsSlot(stclContext);
		this._emailsSlot = new EmailsSlot(stclContext);
	}

	/**
	 * Returns the stencil template class name which is used to create the sencil.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param self
	 *          the stencil as a plugged stencil.
	 * @return the template class name used to create the sencil.
	 */
	public String getInscriptionTemplate(StclContext stclContext, PStcl self) {
		return InscriptionStcl.class.getName();
	}

	/**
	 * Returns the table name used for inscriptions.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param self
	 *          this stencil as a plugged stencil.
	 * @return the table name used for inscriptions.
	 */
	public String getInscriptionTable(StclContext stclContext, PStcl self) {
		return self.getExpandedString(stclContext, Slot.INSCRIPTION_TABLE, "inscriptions");
	}

	public String getInscriptionsKeysSelect(StclContext stclContext, PStcl self) {
		return null;
	}

	public String getInscriptionsTableAliasForProperty(StclContext stclContext, PStcl self) {
		return null;
	}

	public String getInscriptionsKeysIdField(StclContext stclContext, PStcl self) {
		return null;
	}

	public String getInscriptionsKeysFrom(StclContext stclContext, PStcl self) {
		return null;
	}

	/**
	 * Returns a specific SQL clause which must be added to the query to retrieve
	 * the inscription keys.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param cond
	 *          the stencil condition.
	 * @param self
	 *          this stencil as a plugged stencil.
	 * @return the specific clause.
	 */
	public String getInscriptionsKeysCondition(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PStcl self) {
		return "Id > 0 ORDER BY Name ASC";
	}

	// inscription cursor function extracted to stencil
	public Map<String, String> getInscriptionPropertiesValuesFromKeyResults(StclContext stclContext, ResultSet rs) {
		try {
			Map<String, String> map = new HashMap<String, String>();
			map.put(InscriptionStcl.Slot.ID, rs.getString(InscriptionStcl.Slot.ID));
			map.put(InscriptionStcl.Slot.EMAIL, rs.getString(InscriptionStcl.Slot.EMAIL));
			return map;
		} catch (Exception e) {
			if (getLog().isWarnEnabled())
				getLog().warn(stclContext, e);
			return null;
		}
	}

	/**
	 * Returns a specific stencil query to retrieve the inscriptions.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param key
	 *          the key returned from keys query.
	 * @param self
	 *          this stencil as a plugged stencil.
	 * @return the specific query.
	 */
	public String getInscriptionQuery(StclContext stclContext, IKey key, PStcl self) {
		return null;
	}

	public String getInscriptionsStencilIdField(StclContext stclContext, PStcl self) {
		return "id";
	}

	public Result insertInscriptionQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PStcl container) {
		PSlot<StclContext, PStcl> slot = new PSlot<StclContext, PStcl>(this._inscriptionsSlot, container);
		return this._inscriptionsSlot.superInsertStencilQuery(stclContext, stencil, sqlContext, slot);
	}

	public Result beforeInsertInscriptionQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PStcl container) {
		PSlot<StclContext, PStcl> slot = new PSlot<StclContext, PStcl>(this._inscriptionsSlot, container);
		return this._inscriptionsSlot.superBeforeInsertStencilQuery(stclContext, stencil, sqlContext, slot);
	}

	public Result afterInsertInscriptionQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PStcl container) {
		PSlot<StclContext, PStcl> slot = new PSlot<StclContext, PStcl>(this._inscriptionsSlot, container);
		return this._inscriptionsSlot.superAfterInsertStencilQuery(stclContext, stencil, sqlContext, slot);
	}

	public Result updateInscriptionQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PStcl container) {
		PSlot<StclContext, PStcl> slot = new PSlot<StclContext, PStcl>(this._inscriptionsSlot, container);
		return this._inscriptionsSlot.superUpdateStencilQuery(stclContext, stencil, sqlContext, slot);
	}

	public Result beforeUpdateInscriptionQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PStcl container) {
		PSlot<StclContext, PStcl> slot = new PSlot<StclContext, PStcl>(this._inscriptionsSlot, container);
		return this._inscriptionsSlot.superBeforeUpdateStencilQuery(stclContext, stencil, sqlContext, slot);
	}

	public Result afterUpdateInscriptionQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PStcl container) {
		PSlot<StclContext, PStcl> slot = new PSlot<StclContext, PStcl>(this._inscriptionsSlot, container);
		return this._inscriptionsSlot.superAfterUpdateStencilQuery(stclContext, stencil, sqlContext, slot);
	}

	public Result deleteInscriptionQuery(StclContext stclContext, IKey key, PStcl stencil, PStcl sqlContext, PStcl container) {
		PSlot<StclContext, PStcl> slot = new PSlot<StclContext, PStcl>(this._inscriptionsSlot, container);
		return this._inscriptionsSlot.superDeleteStencilQuery(stclContext, key, stencil, sqlContext, slot);
	}

	public Result beforeDeleteInscriptionQuery(StclContext stclContext, IKey key, PStcl stencil, PStcl sqlContext, PStcl container) {
		PSlot<StclContext, PStcl> slot = new PSlot<StclContext, PStcl>(this._inscriptionsSlot, container);
		return this._inscriptionsSlot.superBeforeDeleteStencilQuery(stclContext, key, stencil, sqlContext, slot);
	}

	public Result afterDeleteInscriptionQuery(StclContext stclContext, IKey key, PStcl stencil, PStcl sqlContext, PStcl container) {
		PSlot<StclContext, PStcl> slot = new PSlot<StclContext, PStcl>(this._inscriptionsSlot, container);
		return this._inscriptionsSlot.superAfterDeleteStencilQuery(stclContext, key, stencil, sqlContext, slot);
	}

	public SqlUtils.SqlAssoc getInscriptionSqlAssoc(StclContext stclContext, PStcl stencil, PStcl container) {
		PSlot<StclContext, PStcl> slot = new PSlot<StclContext, PStcl>(this._inscriptionsSlot, container);
		SqlUtils.SqlAssoc assoc = this._inscriptionsSlot.newSqlAssoc(stclContext, stencil, slot);
		assoc.pushAutoIncrement(stclContext, InscriptionStcl.Slot.ID);
		assoc.pushString(stclContext, InscriptionStcl.Slot.EMAIL);
		return assoc;
	}

	/**
	 * Returns the query to get the list of emails of inscriptions. The returned
	 * variable should be named as InscriptionStcl.Slot.EMAIL.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param self
	 *          this stencil as a plugged stencil.
	 * @return the query to get the list of emails.
	 */
	public String getEmailsQuery(StclContext stclContext, PStcl self) {
		String table = getInscriptionTable(stclContext, self);
		return String.format("SELECT `%s` FROM `%s`;", InscriptionStcl.Slot.EMAIL, table);
	}

	/**
	 * Returns template class name used to create the recipients.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param self
	 *          the stencils as a plugged stencil.
	 * @return the recipient template class used.
	 */
	public String getRecipientTemplate(StclContext stclContext, PStcl self) {
		return RecipientStcl.class.getName();
	}

	protected class InscriptionsSlot extends SQLSlot {

		public InscriptionsSlot(StclContext stclContext) {
			super(stclContext, ServiceStcl.this, Slot.INSCRIPTIONS, 50);
		}

		@Override
		public String getTableName(StclContext stclContext, PSlot<StclContext, PStcl> self) {
			return getInscriptionTable(stclContext, self.getContainer());
		}

		@Override
		public String getKeysSelect(StclContext stclContext, PSlot<StclContext, PStcl> self) {
			String select = getInscriptionsKeysSelect(stclContext, self.getContainer());
			if (StringUtils.isNotBlank(select))
				return select;
			return super.getKeysSelect(stclContext, self);
		}

		@Override
		public String getTableAliasForProperty(StclContext stclContext, PSlot<StclContext, PStcl> self) {
			String field = getInscriptionsTableAliasForProperty(stclContext, self.getContainer());
			if (StringUtils.isNotBlank(field))
				return field;
			return super.getTableAliasForProperty(stclContext, self);
		}

		@Override
		public String getKeysIdField(StclContext stclContext, PSlot<StclContext, PStcl> self) {
			String field = getInscriptionsKeysIdField(stclContext, self.getContainer());
			if (StringUtils.isNotBlank(field))
				return field;
			return super.getKeysIdField(stclContext, self);
		}

		@Override
		public String getKeysFrom(StclContext stclContext, PSlot<StclContext, PStcl> self) {
			String from = getInscriptionsKeysFrom(stclContext, self.getContainer());
			if (StringUtils.isNotBlank(from))
				return from;
			return super.getKeysFrom(stclContext, self);
		}

		@Override
		public String getKeysCondition(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
			String clause = getInscriptionsKeysCondition(stclContext, cond, self.getContainer());
			if (StringUtils.isNotBlank(clause))
				return clause;
			return super.getKeysCondition(stclContext, cond, self);
		}

		@Override
		public Map<String, String> getPropertiesValuesFromKeyResults(StclContext stclContext, ResultSet rs, PSlot<StclContext, PStcl> self) {
			return getInscriptionPropertiesValuesFromKeyResults(stclContext, rs);
		}

		@Override
		public String getStencilQuery(StclContext stclContext, IKey key, boolean withWhere, PSlot<StclContext, PStcl> self) {
			String query = getInscriptionQuery(stclContext, key, self.getContainer());
			if (StringUtils.isNotBlank(query))
				return query;
			return super.getStencilQuery(stclContext, key, withWhere, self);
		}

		@Override
		public String getStencilIdField(StclContext stclContext, PSlot<StclContext, PStcl> self) {
			String field = getInscriptionsStencilIdField(stclContext, self.getContainer());
			if (StringUtils.isNotBlank(field))
				return field;
			return super.getStencilIdField(stclContext, self);
		}

		@Override
		public String getStencilTemplate(StclContext stclContext, ResultSet rs, PSlot<StclContext, PStcl> self) {
			return getInscriptionTemplate(stclContext, self.getContainer());
		}

		public Result superInsertStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
			return super.insertStencilQuery(stclContext, stencil, sqlContext, self);
		}

		@Override
		public Result insertStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
			return insertInscriptionQuery(stclContext, stencil, sqlContext, self.getContainer());
		}

		public Result superBeforeInsertStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
			return super.beforeInsertStencilQuery(stclContext, stencil, sqlContext, self);
		}

		@Override
		public Result beforeInsertStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
			return beforeInsertInscriptionQuery(stclContext, stencil, sqlContext, self.getContainer());
		}

		public Result superAfterInsertStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
			return super.afterInsertStencilQuery(stclContext, stencil, sqlContext, self);
		}

		@Override
		public Result afterInsertStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
			return afterInsertInscriptionQuery(stclContext, stencil, sqlContext, self.getContainer());
		}

		public Result superUpdateStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
			return super.updateStencilQuery(stclContext, stencil, sqlContext, self);
		}

		@Override
		public Result updateStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
			return updateInscriptionQuery(stclContext, stencil, sqlContext, self.getContainer());
		}

		public Result superBeforeUpdateStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
			return super.beforeUpdateStencilQuery(stclContext, stencil, sqlContext, self);
		}

		@Override
		public Result beforeUpdateStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
			return beforeUpdateInscriptionQuery(stclContext, stencil, sqlContext, self.getContainer());
		}

		public Result superAfterUpdateStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
			return super.afterUpdateStencilQuery(stclContext, stencil, sqlContext, self);
		}

		@Override
		public Result afterUpdateStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
			return afterUpdateInscriptionQuery(stclContext, stencil, sqlContext, self.getContainer());
		}

		public Result superDeleteStencilQuery(StclContext stclContext, IKey key, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
			return super.deleteStencilQuery(stclContext, key, stencil, sqlContext, self);
		}

		@Override
		public Result deleteStencilQuery(StclContext stclContext, IKey key, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
			return deleteInscriptionQuery(stclContext, key, stencil, sqlContext, self.getContainer());
		}

		public Result superBeforeDeleteStencilQuery(StclContext stclContext, IKey key, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
			return super.beforeDeleteStencilQuery(stclContext, key, stencil, sqlContext, self);
		}

		@Override
		public Result beforeDeleteStencilQuery(StclContext stclContext, IKey key, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
			return beforeDeleteInscriptionQuery(stclContext, key, stencil, sqlContext, self.getContainer());
		}

		public Result superAfterDeleteStencilQuery(StclContext stclContext, IKey key, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
			return super.afterDeleteStencilQuery(stclContext, key, stencil, sqlContext, self);
		}

		@Override
		public Result afterDeleteStencilQuery(StclContext stclContext, IKey key, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
			return afterDeleteInscriptionQuery(stclContext, key, stencil, sqlContext, self.getContainer());
		}

		@Override
		public SqlUtils.SqlAssoc getSqlAssoc(StclContext stclContext, PStcl stencil, PSlot<StclContext, PStcl> self) {
			return getInscriptionSqlAssoc(stclContext, stencil, self.getContainer());
		}
	}

	private class EmailsSlot extends MultiCalculatedSlot<StclContext, PStcl> {

		public EmailsSlot(StclContext stclContext) {
			super(stclContext, ServiceStcl.this, Slot.EMAILS, PSlot.ANY);
		}

		@Override
		protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
			PStcl container = self.getContainer();

			// creates query
			String query = getEmailsQuery(stclContext, container);
			PStcl sqlContext = getSqlContext(stclContext, container);
			SQLContextStcl stcl = sqlContext.getReleasedStencil(stclContext);
			ResultSet rs = stcl.selectQuery(stclContext, query, sqlContext);

			// creates recipients
			try {
				while (rs.next()) {
					String email = rs.getString(InscriptionStcl.Slot.EMAIL);
					IKey key = new Key(email);
					if (getStencilFromList(stclContext, key, self) != null) {
						keepStencilInList(stclContext, key, self);
					} else {
						StencilFactory<StclContext, PStcl> factory = (StencilFactory<StclContext, PStcl>) stclContext.getStencilFactory();
						String template = getRecipientTemplate(stclContext, container);
						PStcl rec = factory.createPStencil(stclContext, self, key, template, email);
						addStencilInList(stclContext, rec, self);
					}
				}
				return cleanList(stclContext, cond, self);
			} catch (Exception e) {
				return StencilUtils.< StclContext, PStcl> iterator(Result.error(e.getMessage()));
			}
		}
	}
}
