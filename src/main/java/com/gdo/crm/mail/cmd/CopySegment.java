/*
 * Copyright GDO - 2004
 */
package com.gdo.crm.mail.cmd;

import com.gdo.crm.mail.model.SQLSegmentStcl;
import com.gdo.mail.model.DistributionListStcl;
import com.gdo.mail.model.SQLDistributionListStcl;
import com.gdo.mail.model.SQLMailStcl;
import com.gdo.mail.model.SegmentStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;

public class CopySegment extends NewSegment {

	public CopySegment(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	protected CommandStatus<StclContext, PStcl> afterCreate(CommandContext<StclContext, PStcl> cmdContext, PStcl created, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();

		// super called
		CommandStatus<StclContext, PStcl> status = super.afterCreate(cmdContext, created, self);
		if (status.isNotSuccess()) {
			return status;
		}

		// copy segment parameters
		PStcl target = cmdContext.getTarget();

		copyString(stclContext, target, created, SQLSegmentStcl.Slot.TITLE);

		copyString(stclContext, target, created, SQLSegmentStcl.Slot.CONTENT1);
		copyString(stclContext, target, created, SQLSegmentStcl.Slot.CONTENT2);
		copyString(stclContext, target, created, SQLSegmentStcl.Slot.CONTENT3);
		copyString(stclContext, target, created, SQLSegmentStcl.Slot.CONTENT4);
		copyString(stclContext, target, created, SQLSegmentStcl.Slot.CONTENT5);
		copyString(stclContext, target, created, SQLSegmentStcl.Slot.CONTENT6);
		copyString(stclContext, target, created, SQLSegmentStcl.Slot.CONTENT7);
		copyString(stclContext, target, created, SQLSegmentStcl.Slot.CONTENT8);

		plug(stclContext, target, created, SQLSegmentStcl.Slot.IMAGE1);
		plug(stclContext, target, created, SQLSegmentStcl.Slot.IMAGE2);

		plug(stclContext, target, created, SQLSegmentStcl.Slot.GENERATOR);

		// copy mail parameters
		PStcl targetMail = target.getStencil(stclContext, SQLSegmentStcl.Slot.MAIL);
		PStcl createdMail = created.getStencil(stclContext, SQLSegmentStcl.Slot.MAIL);

		copyString(stclContext, targetMail, createdMail, SQLMailStcl.Slot.TITLE);
		copyString(stclContext, targetMail, createdMail, SQLMailStcl.Slot.FROM_NAME);
		PStcl to = created.getStencil(stclContext, SegmentStcl.Slot.TO);
		to.setString(stclContext, SQLDistributionListStcl.Slot.MODE, DistributionListStcl.TEST_MODE);

		return success(cmdContext, self);
	}

	private void copyString(StclContext stclContext, PStcl from, PStcl to, String path) {
		String copy = from.getString(stclContext, path);
		to.setString(stclContext, path, copy);
	}

	private void plug(StclContext stclContext, PStcl from, PStcl to, String path) {
		PStcl stcl = from.getStencil(stclContext, path);
		to.plug(stclContext, stcl, path);
	}

}