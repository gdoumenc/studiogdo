package com.gdo.inscription.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.gdo.project.util.SqlUtils;
import com.gdo.sql.slot.SQLSlot;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;

public class InscriptionSlot extends SQLSlot {

	private ServiceStcl _in;

	public InscriptionSlot(StclContext stclContext, ServiceStcl in, String name) {
		super(stclContext, in, name, 50);
		this._in = in;
	}

	private ServiceStcl getService(StclContext stclContext) {
		return this._in;
	}

	@Override
	public String getTableName(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		return getService(stclContext).getInscriptionTable(stclContext, self.getContainer());
	}

	@Override
	public String getKeysSelect(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		String select = getService(stclContext).getInscriptionsKeysSelect(stclContext, self.getContainer());
		if (StringUtils.isNotBlank(select))
			return select;
		return super.getKeysSelect(stclContext, self);
	}

	@Override
	public String getTableAliasForProperty(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		String field = getService(stclContext).getInscriptionsTableAliasForProperty(stclContext, self.getContainer());
		if (StringUtils.isNotBlank(field))
			return field;
		return super.getTableAliasForProperty(stclContext, self);
	}

	@Override
	public String getKeysIdField(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		String field = getService(stclContext).getInscriptionsKeysIdField(stclContext, self.getContainer());
		if (StringUtils.isNotBlank(field))
			return field;
		return super.getKeysIdField(stclContext, self);
	}

	@Override
	public String getKeysFrom(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		String from = getService(stclContext).getInscriptionsKeysFrom(stclContext, self.getContainer());
		if (StringUtils.isNotBlank(from))
			return from;
		return super.getKeysFrom(stclContext, self);
	}

	@Override
	public String getKeysCondition(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
		String clause = getService(stclContext).getInscriptionsKeysCondition(stclContext, cond, self.getContainer());
		if (StringUtils.isNotBlank(clause))
			return clause;
		return super.getKeysCondition(stclContext, cond, self);
	}

	@Override
	public Map<String, String> getPropertiesValuesFromKeyResults(StclContext stclContext, ResultSet rs, PSlot<StclContext, PStcl> self) throws SQLException {
		return getService(stclContext).getInscriptionPropertiesValuesFromKeyResults(stclContext, rs);
	}

	@Override
	public String getStencilQuery(StclContext stclContext, IKey key, boolean withWhere, PSlot<StclContext, PStcl> self) {
		String query = getService(stclContext).getInscriptionQuery(stclContext, key, self.getContainer());
		if (StringUtils.isNotBlank(query))
			return query;
		return super.getStencilQuery(stclContext, key, withWhere, self);
	}

	@Override
	public String getStencilIdField(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		String field = getService(stclContext).getInscriptionsStencilIdField(stclContext, self.getContainer());
		if (StringUtils.isNotBlank(field))
			return field;
		return super.getStencilIdField(stclContext, self);
	}

	@Override
	public String getStencilTemplate(StclContext stclContext, ResultSet rs, PSlot<StclContext, PStcl> self) {
		return getService(stclContext).getInscriptionTemplate(stclContext, self.getContainer());
	}

	public Result superInsertStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
		return super.insertStencilQuery(stclContext, stencil, sqlContext, self);
	}

	@Override
	public Result insertStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
		return getService(stclContext).insertInscriptionQuery(stclContext, stencil, sqlContext, self.getContainer());
	}

	public Result superBeforeInsertStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
		return super.beforeInsertStencilQuery(stclContext, stencil, sqlContext, self);
	}

	@Override
	public Result beforeInsertStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
		return getService(stclContext).beforeInsertInscriptionQuery(stclContext, stencil, sqlContext, self.getContainer());
	}

	public Result superAfterInsertStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
		return super.afterInsertStencilQuery(stclContext, stencil, sqlContext, self);
	}

	@Override
	public Result afterInsertStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
		return getService(stclContext).afterInsertInscriptionQuery(stclContext, stencil, sqlContext, self.getContainer());
	}

	public Result superUpdateStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
		return super.updateStencilQuery(stclContext, stencil, sqlContext, self);
	}

	@Override
	public Result updateStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
		return getService(stclContext).updateInscriptionQuery(stclContext, stencil, sqlContext, self.getContainer());
	}

	public Result superBeforeUpdateStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
		return super.beforeUpdateStencilQuery(stclContext, stencil, sqlContext, self);
	}

	@Override
	public Result beforeUpdateStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
		return getService(stclContext).beforeUpdateInscriptionQuery(stclContext, stencil, sqlContext, self.getContainer());
	}

	public Result superAfterUpdateStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
		return super.afterUpdateStencilQuery(stclContext, stencil, sqlContext, self);
	}

	@Override
	public Result afterUpdateStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
		return getService(stclContext).afterUpdateInscriptionQuery(stclContext, stencil, sqlContext, self.getContainer());
	}

	public Result superDeleteStencilQuery(StclContext stclContext, IKey key, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
		return super.deleteStencilQuery(stclContext, key, stencil, sqlContext, self);
	}

	@Override
	public Result deleteStencilQuery(StclContext stclContext, IKey key, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
		return getService(stclContext).deleteInscriptionQuery(stclContext, key, stencil, sqlContext, self.getContainer());
	}

	public Result superBeforeDeleteStencilQuery(StclContext stclContext, IKey key, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
		return super.beforeDeleteStencilQuery(stclContext, key, stencil, sqlContext, self);
	}

	@Override
	public Result beforeDeleteStencilQuery(StclContext stclContext, IKey key, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
		return getService(stclContext).beforeDeleteInscriptionQuery(stclContext, key, stencil, sqlContext, self.getContainer());
	}

	public Result superAfterDeleteStencilQuery(StclContext stclContext, IKey key, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
		return super.afterDeleteStencilQuery(stclContext, key, stencil, sqlContext, self);
	}

	@Override
	public Result afterDeleteStencilQuery(StclContext stclContext, IKey key, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
		return getService(stclContext).afterDeleteInscriptionQuery(stclContext, key, stencil, sqlContext, self.getContainer());
	}

	@Override
	public SqlUtils.SqlAssoc getSqlAssoc(StclContext stclContext, PStcl stencil, PSlot<StclContext, PStcl> self) {
		return getService(stclContext).getInscriptionSqlAssoc(stclContext, stencil, self.getContainer());
	}
}
