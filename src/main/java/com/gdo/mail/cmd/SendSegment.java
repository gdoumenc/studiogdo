/*
 * Copyright GDO - 2004
 */
package com.gdo.mail.cmd;

import com.gdo.mail.model.IMail;
import com.gdo.mail.model.SegmentStcl;
import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.StencilUtils;

public class SendSegment extends AtomicActionStcl {

	public SendSegment(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		PStcl segment = cmdContext.getTarget();

		// gets mail message
		PStcl mail = segment.getStencil(stclContext, SegmentStcl.Slot.MAIL);
		if (StencilUtils.isNull(mail)) {
			return error(cmdContext, self, 1, "no mail defined");
		}
		IMail m = (IMail) mail.getReleasedStencil(stclContext);

		// add tracking counters
		m.addSendListener(stclContext, segment, segment);
		// m.setContentFormatter(stclContext, new ContentFormat(), segment);

		// sends to each recipient
		Result result = m.multiSend(cmdContext, cmdContext, mail);
		return success(cmdContext, self, result);
	}

	/*
	 * protected class ContentFormat implements MailStcl.ContentFormatter {
	 * //private int _counter;
	 * 
	 * @Override public String getContent(StclContext stclContext, String content,
	 * PStcl self) {
	 * 
	 * this._counter++; String counter1 = String.format(
	 * "res1.studiogdo.com/1/coworks/405/%s/882dae1b0460246bfa1d282675fec8b09a4fa9978217b2c582c208cadcf9d01c"
	 * , this._counter); content =
	 * content.replaceAll("www.coworks.pro/images/globe", counter1); String
	 * counter2 = String.format(
	 * "res1.studiogdo.com/1/coworks/405/%s/882dae1b0460246bfa1d282675fec8b0bc6567ccb264b6a7266a847566bc5286"
	 * , this._counter); content =
	 * content.replaceAll("www.coworks.pro/presentation", counter2);
	 * 
	 * return content; }
	 * 
	 * }
	 */
}
