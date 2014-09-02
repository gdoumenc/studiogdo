package com.gdo.sql.slot;

import java.sql.ResultSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.gdo.project.slot._SlotCursor;
import com.gdo.project.util.SqlUtils;
import com.gdo.sql.model.SQLContextStcl;
import com.gdo.sql.model.SQLStcl;
import com.gdo.stencils.Result;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cond.PathCondition;
import com.gdo.stencils.factory.StencilFactory;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.StencilUtils;

public class SQLCursor extends _SlotCursor {

	private boolean _initialized;

	public SQLCursor(String name, int size) {
		super(name, size);
	}

	/**
	 * Checks if the slot was initialized.
	 * 
	 * @return
	 */
	public boolean isInitialized() {
		return this._initialized;
	}

	public void setInitialized() {
		this._initialized = true;
	}

	@Override
	protected PStcl createStencil(StclContext stclContext, PSlot<StclContext, PStcl> container, PSlot<StclContext, PStcl> slot, IKey key, List<Object> list) {

		// get keys query
		SQLSlot sqlSlot = container.getSlot();
        PathCondition<StclContext, PStcl> cond = PathCondition.newKeyCondition(stclContext, key, container.getContainer());
		String query = sqlSlot.getKeysQuery(stclContext, cond, container);
		if (StringUtils.isEmpty(query)) {
			String msg = logWarn(stclContext, "Stencil query not defined for slot %s for create stencil", slot);
			return Stcl.nullPStencil(stclContext, Result.error(msg));
		}

		// get sql context
		PStcl sqlContext = sqlSlot.getSQLContext(stclContext, container);
		if (StencilUtils.isNull(sqlContext)) {
			String msg = logWarn(stclContext, "No SQL context defined for slot %s for create stencil", slot);
			return Stcl.nullPStencil(stclContext, Result.error(msg));
		}
		SQLContextStcl context = (SQLContextStcl) sqlContext.getReleasedStencil(stclContext);

		// creates the stencil
		ResultSet rs = context.selectQuery(stclContext, query, sqlContext);
		if (rs != null) {
			try {
				StencilFactory<StclContext, PStcl> factory = (StencilFactory<StclContext, PStcl>) stclContext.getStencilFactory();

				if (!rs.next()) {
					String msg = logWarn(stclContext, "Stencil was removed from database in %s at key (no more in cursor)", slot, key);
					return Stcl.nullPStencil(stclContext, Result.error(msg));
				}

				// creates the stencil
				String template = sqlSlot.getStencilTemplate(stclContext, rs, container);
				PStcl stcl;
				if (StringUtils.isEmpty(template)) {
					stcl = factory.createPProperty(stclContext, slot, key, "");
				} else {
					Object params[] = sqlSlot.getStencilParameters(stclContext, rs, container);
					stcl = factory.createPStencil(stclContext, slot, key, template, params);
				}

				// checks the stencil is a SQLStcl
				if (!(stcl.getReleasedStencil(stclContext) instanceof SQLStcl)) {
					String msg = logWarn(stclContext, "The template %s must be an instance of SQLStcl to be inserted in the SQLSlot %s", template, slot);
					return Stcl.nullPStencil(stclContext, Result.error(msg));
				}

				// adds containing slot
				SQLStcl sql = (SQLStcl) stcl.getReleasedStencil(stclContext);
				sql.setSQLContext(sqlContext);
				sql.setSQLContainerSlot(container);

				// adds result set in parameter
				list.add(rs);

				return stcl;
			} catch (Exception e) {
				String msg = logError(stclContext, "%s", e);
				return Stcl.nullPStencil(stclContext, Result.error(msg));
			}
		}

		// no stencil found from query
		String msg = logError(stclContext, "No stencil found (%s)", query);
		return Stcl.nullPStencil(stclContext, Result.error(msg));
	}

	@Override
	protected PStcl completeCreatedStencil(StclContext stclContext, PSlot<StclContext, PStcl> container, PSlot<StclContext, PStcl> slot, IKey key, PStcl stencil, List<Object> list) {
		ResultSet rs = (ResultSet) list.get(0);
		if (rs != null) {
			try {

				// completes stencil
				Stcl stcl = stencil.getReleasedStencil(stclContext);
				if (stcl instanceof SQLStcl) {
					((SQLStcl) stcl).completeCreatedSQLStencil(stclContext, rs, stencil);
				} else {
					SQLSlot sqlSlot = slot.getSlot();
					sqlSlot.completeStencil(stclContext, stencil, rs, container);
					logError(stclContext, "Should not use any more non SQLStcl %s in SQLSlot %s", stcl, slot);
				}
				
				// completes plug operation
				slot.getSlot().afterPlug(stclContext, stencil, slot);
			} catch (Exception e) {
				logError(stclContext, "%s", e);
			} finally {
				SqlUtils.closeResultSet(stclContext, rs);
			}
		}
		return stencil;
	}
}