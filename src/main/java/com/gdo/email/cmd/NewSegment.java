/*
 * Copyright GDO - 2004
 */
package com.gdo.email.cmd;

import com.gdo.email.model.SQLGeneratorStcl;
import com.gdo.email.model.SQLSegmentStcl;
import com.gdo.generator.model.GeneratorStcl;
import com.gdo.mail.model.OperationStcl;
import com.gdo.mail.model.SQLDistributionListStcl;
import com.gdo.mail.model.SQLMailStcl;
import com.gdo.mail.model.SQLOperationStcl;
import com.gdo.project.cmd.CreateAtomic;
import com.gdo.sql.model.SQLContextStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.PathUtils;

public class NewSegment extends CreateAtomic {

	public NewSegment(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	protected CommandStatus<StclContext, PStcl> afterPlug(CommandContext<StclContext, PStcl> cmdContext, PStcl created, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();

		// super called
		CommandStatus<StclContext, PStcl> status = super.afterPlug(cmdContext, created, self);
		if (status.isNotSuccess()) {
			return status;
		}

		// plug current Generator of operation container in segment Generator
		// slot
		PStcl operation = created.getStencil(stclContext, PathUtils.PARENT);
		PStcl generator = operation.getStencil(stclContext, OperationStcl.Slot.GENERATOR);
		if (generator.isNotNull()) { // only if exists
			created.plug(stclContext, generator, SQLSegmentStcl.Slot.GENERATOR);

			// link mail Content to Generator/FilesGenerated($content)
			PStcl mail = created.getStencil(stclContext, SQLSegmentStcl.Slot.MAIL);
			PStcl mask = generator.getStencil(stclContext, GeneratorStcl.Slot.MASK);
			mail.plug(stclContext, mask, "Generator/Mask", Key.NO_KEY);

			// set trackers
			String pattern = generator.getString(stclContext, SQLGeneratorStcl.Slot.TRACKER_PATTERN, null);
			mail.setString(stclContext, SQLMailStcl.Slot.TRACKER_PATTERN, pattern);
			String tracker = generator.getString(stclContext, SQLGeneratorStcl.Slot.TRACKER_REPLACEMENT, null);
			mail.setString(stclContext, SQLMailStcl.Slot.TRACKER_REPLACEMENT, tracker);
		}

		// create database table
		String name = getDataBaseTableName(cmdContext, created, self);
		created.setString(stclContext, SQLSegmentStcl.Slot.FROM_TABLE, name);
		return createDataBaseTable(cmdContext, created, name, self);
	}

	protected String getDataBaseTableName(CommandContext<StclContext, PStcl> cmdContext, PStcl created, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		return created.format(stclContext, "addresses_<$stencil facet='@'/>");
	}

	protected CommandStatus<StclContext, PStcl> createDataBaseTable(CommandContext<StclContext, PStcl> cmdContext, PStcl created, String name, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		PStcl target = cmdContext.getTarget();

		// create database table
		PStcl sqlContext = target.getStencil(stclContext, SQLOperationStcl.Slot.SQL_CONTEXT);
		String query = String.format("CREATE TABLE %s LIKE addresses", name);
		CommandStatus<StclContext, PStcl> result = sqlContext.call(stclContext, SQLContextStcl.Command.UPDATE_QUERY, query);
		if (result.isNotSuccess())
			return result;

		// change FROM value of all distribution lists
		String fromPath = PathUtils.compose(SQLSegmentStcl.Slot.TO, SQLDistributionListStcl.Slot.FROM);
		created.setString(stclContext, fromPath, name);
		String sentPath = PathUtils.compose(SQLSegmentStcl.Slot.SENT, SQLDistributionListStcl.Slot.FROM);
		created.setString(stclContext, sentPath, name);
		String errorPath = PathUtils.compose(SQLSegmentStcl.Slot.ERROR, SQLDistributionListStcl.Slot.FROM);
		created.setString(stclContext, errorPath, name);
		String alreadyPath = PathUtils.compose(SQLSegmentStcl.Slot.ALREADY, SQLDistributionListStcl.Slot.FROM);
		created.setString(stclContext, alreadyPath, name);
		String bccPath = PathUtils.compose(SQLSegmentStcl.Slot.BCC, SQLDistributionListStcl.Slot.FROM);
		created.setString(stclContext, bccPath, name);
		String readPath = PathUtils.compose(SQLSegmentStcl.Slot.READ, SQLDistributionListStcl.Slot.FROM);
		created.setString(stclContext, readPath, name);

		return success(cmdContext, self);
	}

}