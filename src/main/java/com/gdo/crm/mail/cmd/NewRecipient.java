/*
 * Copyright GDO - 2004
 */
package com.gdo.crm.mail.cmd;

import org.apache.commons.lang3.StringUtils;

import com.gdo.crm.CrmResources.Resource;
import com.gdo.mail.model.RecipientStcl;
import com.gdo.mail.model.SQLDistributionListStcl;
import com.gdo.sql.model.SQLContextStcl;
import com.gdo.sql.model.SQLStcl;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.factory.StencilFactory;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PStcl;

public class NewRecipient extends com.gdo.email.cmd.NewRecipient {

	public NewRecipient(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	protected CommandStatus<StclContext, PStcl> plugStencil(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		PStcl target = cmdContext.getTarget();

		// calls before plug
		CommandStatus<StclContext, PStcl> before = beforePlug(cmdContext, this._created, self);
		if (before.isNotSuccess()) {
			return error(cmdContext, self, 0, before);
		}

		// plug it by sql update query
		PStcl sqlContext = target.getStencil(stclContext, SQLDistributionListStcl.Slot.SQL_CONTEXT);
		SQLContextStcl stcl = (SQLContextStcl) sqlContext.getReleasedStencil(stclContext);
		String query = "INSERT INTO `%s` (`Id` ,`Commercial` ,`Email` ,`Status` ,`Date` , `Msg` ,`DateRead`) ";
		query += "VALUES (%s , '%s' , '%s', 'tbs', NOW(), NULL , NULL)";
		String id = this._created.getString(stclContext, SQLStcl.Slot.ID, "");
		if (StringUtils.isNotBlank(id)) {
			id = "'" + id + "'";
		} else {
			id = "NULL";
		}
		String from = target.getString(stclContext, SQLDistributionListStcl.Slot.FROM_TABLE, null);
		String comPath = getResourcePath(stclContext, Resource.COMMERCIAL_CONNECTED, self);
		PStcl commercial = target.getStencil(stclContext, comPath);
		String com = commercial.getString(stclContext, SQLStcl.Slot.ID, "");
		String address = this._created.getString(stclContext, RecipientStcl.Slot.ADDRESS, "");
		Result result = stcl.updateQuery(stclContext, String.format(query, from, id, com, address), sqlContext);
		if (result.isNotSuccess())
			return error(cmdContext, self, result);

		// create the plugged stencil directly
		StencilFactory<StclContext, PStcl> factory = (StencilFactory<StclContext, PStcl>) stclContext.getStencilFactory();
		this._plugged = factory.createPStencil(stclContext, this._slot, Key.NO_KEY, _created);

		// calls after plug
		CommandStatus<StclContext, PStcl> after = afterPlug(cmdContext, this._plugged, self);
		if (after.isNotSuccess()) {
			return error(cmdContext, self, 0, after);
		}

		// returns the plugged path
		return success(cmdContext, self);
	}

}