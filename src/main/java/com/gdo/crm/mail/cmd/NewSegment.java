/*
 * Copyright GDO - 2004
 */
package com.gdo.crm.mail.cmd;

import com.gdo.mail.model.SQLDistributionListStcl;
import com.gdo.mail.model.SegmentStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;

public class NewSegment extends com.gdo.email.cmd.NewSegment {

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

		// set same select and condition to all distribution list
		PStcl to = created.getStencil(stclContext, SegmentStcl.Slot.TO);
		String cond = to.getNotExpandedString(stclContext, SQLDistributionListStcl.Slot.CONDITION, null);
		String select = to.getNotExpandedString(stclContext, SQLDistributionListStcl.Slot.SELECT, null);
		PStcl sent = created.getStencil(stclContext, SegmentStcl.Slot.SENT);
		sent.setString(stclContext, SQLDistributionListStcl.Slot.CONDITION, cond);
		sent.setString(stclContext, SQLDistributionListStcl.Slot.SELECT, select);
		PStcl err = created.getStencil(stclContext, SegmentStcl.Slot.ERROR);
		err.setString(stclContext, SQLDistributionListStcl.Slot.CONDITION, cond);
		err.setString(stclContext, SQLDistributionListStcl.Slot.SELECT, select);
		PStcl already = created.getStencil(stclContext, SegmentStcl.Slot.ALREADY);
		already.setString(stclContext, SQLDistributionListStcl.Slot.CONDITION, cond);
		already.setString(stclContext, SQLDistributionListStcl.Slot.SELECT, select);

		return status;
	}
}