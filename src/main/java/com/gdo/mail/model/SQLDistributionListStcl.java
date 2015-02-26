/**
 * Copyright GDO - 2004
 */
package com.gdo.mail.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.gdo.project.util.SqlUtils;
import com.gdo.sql.model.SQLContextStcl;
import com.gdo.sql.slot.SQLSlot;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;

public class SQLDistributionListStcl extends com.gdo.mail.model.DistributionListStcl {

	public interface Slot extends com.gdo.mail.model.DistributionListStcl.Slot {
		String SQL_CONTEXT = "SqlContext";

		String TABLE = "Table";
		String SELECT = "Select";
		String FROM_TABLE = "FromTable";
		String CONDITION = "Condition";
		String ALIAS = "Alias";

		String INSERT_QUERY = "InsertQuery";
		String UPDATE_QUERY = "UpdateQuery";
		String DELETE_QUERY = "DeleteQuery";
	}

	private boolean _superIsCreated; // used to redefined slots

	protected Boolean _hasName = null;
	protected Boolean _hasMessage = null;

	public SQLDistributionListStcl(StclContext stclContext) {
		super(stclContext);

		// redefined slots
		this._superIsCreated = true;
		new ToSlot(stclContext);
	}

	public String getRecipientTemplate(StclContext stclContext, ResultSet rs, PStcl self) {
		return SQLRecipientStcl.class.getName();
	}

	/**
	 * Name of the table containing all addresses. By default same as from (wrong
	 * if union done).
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param self
	 *          this stencil as a plugged stencil.
	 * @return the table name.
	 */
	public String getTableName(StclContext stclContext, PStcl self) {
		return self.getExpandedString(stclContext, Slot.TABLE, "");
	}

	public String getSelect(StclContext stclContext, PStcl self) {
		return self.getExpandedString(stclContext, Slot.SELECT, "");
	}

	public String getFromTable(StclContext stclContext, PStcl self) {
		return self.getExpandedString(stclContext, Slot.FROM_TABLE, "");
	}

	public String getTableAliasForProperty(StclContext stclContext, PStcl self) {
		return self.getExpandedString(stclContext, Slot.ALIAS, "");
	}

	public String getKeysCondition(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PStcl self) {
		return self.getExpandedString(stclContext, Slot.CONDITION, "");
	}

	public String getInsertQuery(StclContext stclContext, PStcl self) {
		return self.getString(stclContext, Slot.INSERT_QUERY, "");
	}

	public String getUpdateQuery(StclContext stclContext, PStcl self) {
		return self.getString(stclContext, Slot.UPDATE_QUERY, "");
	}

	public String getDeleteQuery(StclContext stclContext, IKey key, PStcl stencil, PStcl self) {
		return self.getString(stclContext, Slot.DELETE_QUERY, "");
	}

	public Map<String, String> getPropertiesValuesFromKeyResults(StclContext stclContext, Map<String, String> map, ResultSet rs, PSlot<StclContext, PStcl> self) throws SQLException {
		map.put(SQLRecipientStcl.Slot.ADDRESS, rs.getString(SQLRecipientStcl.Slot.ADDRESS));

		// put name if defined
		if (SQLDistributionListStcl.this._hasName == null) {
			SQLDistributionListStcl.this._hasName = new Boolean(SqlUtils.hasColumn(SQLRecipientStcl.Slot.NAME, rs));
		}
		if (SQLDistributionListStcl.this._hasName) {
			map.put(SQLRecipientStcl.Slot.NAME, rs.getString(SQLRecipientStcl.Slot.NAME));
		} else {
			map.put(SQLRecipientStcl.Slot.NAME, "");
		}

		// put message if defined
		if (SQLDistributionListStcl.this._hasMessage == null) {
			SQLDistributionListStcl.this._hasMessage = new Boolean(SqlUtils.hasColumn(SQLRecipientStcl.Slot.MESSAGE, rs));
		}
		if (SQLDistributionListStcl.this._hasMessage) {
			map.put(SQLRecipientStcl.Slot.MESSAGE, rs.getString(SQLRecipientStcl.Slot.MESSAGE));
		} else {
			map.put(SQLRecipientStcl.Slot.MESSAGE, "");
		}

		return map;
	}

	public SqlUtils.SqlAssoc getSqlAssoc(StclContext stclContext, SqlUtils.SqlAssoc assoc, PStcl stencil, PSlot<StclContext, PStcl> self) {
		assoc.pushString(stclContext, SQLRecipientStcl.Slot.ADDRESS, SQLRecipientStcl.Slot.ADDRESS);
		if (SQLDistributionListStcl.this._hasName) {
			assoc.pushString(stclContext, SQLRecipientStcl.Slot.NAME, SQLRecipientStcl.Slot.NAME);
		}
		if (SQLDistributionListStcl.this._hasMessage) {
			assoc.pushString(stclContext, SQLRecipientStcl.Slot.MESSAGE, SQLRecipientStcl.Slot.MESSAGE);
		}
		return assoc;
	}

	private class ToSlot extends SQLSlot {

		public ToSlot(StclContext stclContext) {
			super(stclContext, SQLDistributionListStcl.this, Slot.TO, 50);
		}

		@Override
		protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
			PStcl container = self.getContainer();
			String mode = container.getString(stclContext, Slot.MODE, TEST_MODE);

			if (TEST_MODE.equals(mode)) {
				return container.getStencils(stclContext, Slot.TEST);
			}
			return super.getStencilsList(stclContext, cond, self);
		}

		@Override
		public String getTableName(StclContext stclContext, PSlot<StclContext, PStcl> self) {
			return SQLDistributionListStcl.this.getTableName(stclContext, self.getContainer());
		}

		@Override
		public String getStencilTemplate(StclContext stclContext, ResultSet rs, PSlot<StclContext, PStcl> self) {
			return getRecipientTemplate(stclContext, rs, self.getContainer());
		}

		@Override
		public String getKeysSelect(StclContext stclContext, PSlot<StclContext, PStcl> self) {
			String select = SQLDistributionListStcl.this.getSelect(stclContext, self.getContainer());
			if (StringUtils.isNotBlank(select))
				return select;
			return super.getKeysSelect(stclContext, self);
		}

		@Override
		public String getKeysFrom(StclContext stclContext, PSlot<StclContext, PStcl> self) {
			String from = SQLDistributionListStcl.this.getFromTable(stclContext, self.getContainer());
			if (StringUtils.isNotBlank(from)) {
				return from;
			}
			return super.getKeysFrom(stclContext, self);
		}

		@Override
		public String getTableAliasForProperty(StclContext stclContext, PSlot<StclContext, PStcl> self) {
			String field = SQLDistributionListStcl.this.getTableAliasForProperty(stclContext, self.getContainer());
			if (StringUtils.isNotBlank(field))
				return field;
			return super.getTableAliasForProperty(stclContext, self);
		}

		@Override
		public String getKeysCondition(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
			String condition = SQLDistributionListStcl.this.getKeysCondition(stclContext, cond, self.getContainer());
			if (StringUtils.isNotBlank(condition))
				return condition;
			return super.getKeysCondition(stclContext, cond, self);
		}

		@Override
		public String getStencilSelect(StclContext stclContext, PSlot<StclContext, PStcl> self) {
			String select = SQLDistributionListStcl.this.getSelect(stclContext, self.getContainer());
			if (StringUtils.isNotBlank(select))
				return select;
			return super.getStencilSelect(stclContext, self);
		}

		@Override
		public String getStencilFrom(StclContext stclContext, PSlot<StclContext, PStcl> self) {
			String from = SQLDistributionListStcl.this.getFromTable(stclContext, self.getContainer());
			if (StringUtils.isNotBlank(from))
				return from;
			return super.getStencilFrom(stclContext, self);
		}

		@Override
		public Result insertStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
			try {

				// checks if a specific insertion query is defined
				String insert = getInsertQuery(stclContext, self.getContainer());
				if (StringUtils.isBlank(insert)) {
					return super.insertStencilQuery(stclContext, stencil, sqlContext, self);
				}

				// does the query
				String query = stencil.format(stclContext, insert);
				SQLContextStcl stcl = (SQLContextStcl) sqlContext.getReleasedStencil(stclContext);
				Result result = stcl.updateQuery(stclContext, query, sqlContext);
				if (result.isNotSuccess()) {
					return result;
				}

				// does after insertion
				return afterInsertStencilQuery(stclContext, stencil, sqlContext, self);
			} catch (Exception e) {
				String msg = logError(stclContext, e.toString());
				return Result.error(msg);
			}
		}

		@Override
		public Result updateStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
			try {

				// checks if a specific updatation query is defined
				String update = getUpdateQuery(stclContext, self.getContainer());
				if (StringUtils.isBlank(update)) {
					return super.updateStencilQuery(stclContext, stencil, sqlContext, self);
				}

				// does the query
				String query = stencil.format(stclContext, update);
				SQLContextStcl stcl = (SQLContextStcl) sqlContext.getReleasedStencil(stclContext);
				Result result = stcl.updateQuery(stclContext, query, sqlContext);
				if (result.isNotSuccess())
					return result;

				// does after updatation
				return afterUpdateStencilQuery(stclContext, stencil, sqlContext, self);
			} catch (Exception e) {
				if (getLog().isErrorEnabled())
					getLog().error(stclContext, e);
				return Result.error(e);
			}
		}

		@Override
		public Result deleteStencilQuery(StclContext stclContext, IKey key, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
			try {

				// checks if a specific deletion query is defined
				String delete = getDeleteQuery(stclContext, key, stencil, self.getContainer());
				if (StringUtils.isBlank(delete)) {
					return super.deleteStencilQuery(stclContext, key, stencil, sqlContext, self);
				}

				// does the query
				String query = stencil.format(stclContext, delete);
				SQLContextStcl stcl = (SQLContextStcl) sqlContext.getReleasedStencil(stclContext);
				Result result = stcl.updateQuery(stclContext, query, sqlContext);
				if (result.isNotSuccess())
					return result;

				// does after updatation
				return afterDeleteStencilQuery(stclContext, key, stencil, sqlContext, self);
			} catch (Exception e) {
				if (getLog().isErrorEnabled())
					getLog().error(stclContext, e);
				return Result.error(e);
			}
		}

		@Override
		public Map<String, String> getPropertiesValuesFromKeyResults(StclContext stclContext, ResultSet rs, PSlot<StclContext, PStcl> self) throws SQLException {
			Map<String, String> map = super.getPropertiesValuesFromKeyResults(stclContext, rs, self);
			return SQLDistributionListStcl.this.getPropertiesValuesFromKeyResults(stclContext, map, rs, self);
		}

		@Override
		public SqlUtils.SqlAssoc getSqlAssoc(StclContext stclContext, PStcl stencil, PSlot<StclContext, PStcl> self) {
			SqlUtils.SqlAssoc assoc = super.getSqlAssoc(stclContext, stencil, self);
			return SQLDistributionListStcl.this.getSqlAssoc(stclContext, assoc, stencil, self);
		}
	}
}