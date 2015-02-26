/*
 * Copyright GDO - 2004
 */
package com.gdo.email.cmd;

import org.apache.commons.lang3.StringUtils;

import com.gdo.email.model.SQLSegmentStcl;
import com.gdo.mail.model.SQLOperationStcl;
import com.gdo.project.cmd.Unplug;
import com.gdo.sql.model.SQLContextStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;

public class DeleteSegment extends Unplug {

	public DeleteSegment(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {

		CommandStatus<StclContext, PStcl> status = deleteDataBaseTable(cmdContext, self);
		if (status.isNotSuccess())
			return status;

		return super.doAction(cmdContext, self);
	}

	protected CommandStatus<StclContext, PStcl> deleteDataBaseTable(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		PStcl target = cmdContext.getTarget();

		String name = target.getString(stclContext, SQLSegmentStcl.Slot.FROM_TABLE);
		if (StringUtils.isNotBlank(name)) {
			PStcl sqlContext = target.getStencil(stclContext, SQLOperationStcl.Slot.SQL_CONTEXT);
			String query = String.format("DROP TABLE %s", name);
			return sqlContext.call(stclContext, SQLContextStcl.Command.UPDATE_QUERY, query);
		}
		return success(cmdContext, self);
	}
}