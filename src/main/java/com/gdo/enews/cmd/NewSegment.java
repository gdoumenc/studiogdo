/*
 * Copyright GDO - 2004
 */
package com.gdo.enews.cmd;

import com.gdo.generator.model.GeneratorStcl;
import com.gdo.mail.model.OperationStcl;
import com.gdo.mail.model.SQLDistributionListStcl;
import com.gdo.mail.model.SQLOperationStcl;
import com.gdo.mail.model.SegmentStcl;
import com.gdo.project.adaptor.LinkStcl;
import com.gdo.project.cmd.CreateAtomic;
import com.gdo.site.model.SimplePageStcl;
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

		// set name
		String name = created.format(stclContext, "Lettre n°<$stencil facet='@'/>");
		created.setString(stclContext, SegmentStcl.Slot.NAME, name);

		// plug current Generator of operation container in segment Generator
		// slot
		PStcl operation = created.getStencil(stclContext, PathUtils.PARENT);
		PStcl generator = operation.getStencil(stclContext, OperationStcl.Slot.GENERATOR);
		created.plug(stclContext, generator, SegmentStcl.Slot.GENERATOR);

		// link mail Content to Generator/FilesGenerated($content)
		PStcl mail = created.getStencil(stclContext, SegmentStcl.Slot.MAIL);
		PStcl mask = generator.getStencil(stclContext, GeneratorStcl.Slot.MASK);
		mail.plug(stclContext, mask, "Generator/Mask", Key.NO_KEY);

		// create page, set segment content and generator in page
		PStcl page = created.newPStencil(stclContext, "Page", Key.NO_KEY, SimplePageStcl.class.getName());
		page.clearSlot(stclContext, SimplePageStcl.Slot.CONTENT);
		page.plug(stclContext, created.getStencil(stclContext, SegmentStcl.Slot.CONTENT), SimplePageStcl.Slot.CONTENT);
		page.plug(stclContext, generator, SimplePageStcl.Slot.GENERATOR);
		page.newPStencil(stclContext, SimplePageStcl.Slot.FTP_CONTEXT, Key.NO_KEY, LinkStcl.class.getName(), "/Contexts(ftp)");

		// create database table
		return createDataBaseTable(cmdContext, created, self);
	}

	protected CommandStatus<StclContext, PStcl> createDataBaseTable(CommandContext<StclContext, PStcl> cmdContext, PStcl created, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		PStcl target = cmdContext.getTarget();

		// create database table
		PStcl sqlContext = target.getStencil(stclContext, SQLOperationStcl.Slot.SQL_CONTEXT);
		String table = created.format(stclContext, "addresses_<$stencil facet='@'/>");
		String query = String.format("CREATE TABLE %s LIKE addresses", table);
		CommandStatus<StclContext, PStcl> result = sqlContext.call(stclContext, SQLContextStcl.Command.UPDATE_QUERY, query);
		if (result.isNotSuccess())
			return result;

		// change FROM value of TO distribution list
		String fromPath = PathUtils.compose(SegmentStcl.Slot.TO, SQLDistributionListStcl.Slot.FROM);
		created.setString(stclContext, fromPath, table);

		return success(cmdContext, self);
	}

}