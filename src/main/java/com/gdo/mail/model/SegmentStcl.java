/**
 * Copyright GDO - 2004
 */
package com.gdo.mail.model;

import com.gdo.mail.cmd.SendSegment;
import com.gdo.stencils.Result;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.descriptor.Links;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.CalculatedIntegerPropertySlot;
import com.gdo.stencils.util.PathUtils;

public class SegmentStcl extends Stcl implements IMailSendListener {

	public interface Slot extends Stcl.Slot {
		String MAIL_CONTEXT = "MailContext";

		String MAIL = "Mail";
		String CONTENT = "Content";

		String TO = "To";
		String SENT = "Sent";
		String ERROR = "Error";
		String ALREADY = "Already";
		String BCC = "BCC";

		String NUMBER_OF_TO = "NumberOfTo";
		String NUMBER_OF_SENT = "NumberOfSent";
		String NUMBER_OF_ERROR = "NumberOfError";
		String NUMBER_OF_ALREADY = "NumberOfAlready";
		String NUMBER_OF_BCC = "NumberOfBCC";
		
		String GENERATORS = "Generators";
		
		String SERVICES = "Services";
	}
	
	public interface Command extends Stcl.Command {
		String SEND = "Send";
	}

	public SegmentStcl(StclContext stclContext) {
		super(stclContext);
		
		singleSlot(Slot.MAIL_CONTEXT);
		delegateSlot(Slot.CONTENT, "Mail/Content");

		Links links = new Links();
		links.put("MailContext", "../MailContext");
		links.put("Generator", "../Generator");
		links.put("To", "../To/To");
		links.put("BCC", "../BCC/To");
		singleSlot(Slot.MAIL, links);
		
		new NumberOfToSlot(stclContext);
		new NumberOfSentSlot(stclContext);
		new NumberOfErrorSlot(stclContext);
		new NumberOfAlreadySlot(stclContext);
		new NumberOfBccSlot(stclContext);
		
		multiSlot(Slot.GENERATORS);
		
		multiSlot(Slot.SERVICES);
		
		command(Command.SEND, SendSegment.class);
	}

	/**
	 * Returns the test mode of the TO distribution list.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param self
	 *          this stencil as a plugged tencil.
	 * @return the test mode.
	 */
	protected String getTestMode(StclContext stclContext, PStcl self) {
		PStcl to = self.getStencil(stclContext, Slot.TO);
		return to.getString(stclContext, SQLDistributionListStcl.Slot.MODE, SQLDistributionListStcl.TEST_MODE);
	}

	//
	// IMailSendListener implementation
	//

	public Result afterSend(CommandContext<StclContext, PStcl> cmdContext, PStcl mail, PStcl recipient, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();

		// plugs it in sent segment
		self.plug(stclContext, recipient, PathUtils.compose(Slot.SENT, DistributionListStcl.Slot.TO));
		self.unplugOtherStencilFrom(stclContext, PathUtils.compose(Slot.TO, DistributionListStcl.Slot.TO), recipient);
		return Result.success();
	}

	public Result afterError(CommandContext<StclContext, PStcl> cmdContext, PStcl mail, PStcl recipient, String reason, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();

		// plugs it in error segment
		self.plug(stclContext, recipient, PathUtils.compose(Slot.ERROR, DistributionListStcl.Slot.TO));
		self.unplugOtherStencilFrom(stclContext, PathUtils.compose(Slot.TO, DistributionListStcl.Slot.TO), recipient);
		return Result.success();
	}

	public Result beforeFirst(CommandContext<StclContext, PStcl> cmdContext, PStcl mail, PStcl self) {
		return Result.success();
	}

	public Result afterLast(CommandContext<StclContext, PStcl> cmdContext, PStcl mail, PStcl self) {
		return Result.success();
	}

	//
	// Slots
	//

	private class NumberOfToSlot extends CalculatedIntegerPropertySlot<StclContext, PStcl> {

		public NumberOfToSlot(StclContext stclContext) {
			super(stclContext, SegmentStcl.this, Slot.NUMBER_OF_TO);
		}

		@Override
		public int getIntegerValue(StclContext stclContext, PStcl self) {
			PStcl segment = self.getContainer(stclContext);
			PSlot<StclContext, PStcl> to = segment.getSlot(stclContext, Slot.TO);
			return to.size(stclContext, null);
		}

	}

	private class NumberOfSentSlot extends CalculatedIntegerPropertySlot<StclContext, PStcl> {

		public NumberOfSentSlot(StclContext stclContext) {
			super(stclContext, SegmentStcl.this, Slot.NUMBER_OF_SENT);
		}

		@Override
		public int getIntegerValue(StclContext stclContext, PStcl self) {
			PStcl segment = self.getContainer(stclContext);
			PSlot<StclContext, PStcl> sent = segment.getSlot(stclContext, Slot.SENT);
			return sent.size(stclContext, null);
		}

	}

	private class NumberOfErrorSlot extends CalculatedIntegerPropertySlot<StclContext, PStcl> {

		public NumberOfErrorSlot(StclContext stclContext) {
			super(stclContext, SegmentStcl.this, Slot.NUMBER_OF_ERROR);
		}

		@Override
		public int getIntegerValue(StclContext stclContext, PStcl self) {
			PStcl segment = self.getContainer(stclContext);
			PSlot<StclContext, PStcl> error = segment.getSlot(stclContext, Slot.ERROR);
			return error.size(stclContext, null);
		}

	}

	private class NumberOfAlreadySlot extends CalculatedIntegerPropertySlot<StclContext, PStcl> {

		public NumberOfAlreadySlot(StclContext stclContext) {
			super(stclContext, SegmentStcl.this, Slot.NUMBER_OF_ALREADY);
		}

		@Override
		public int getIntegerValue(StclContext stclContext, PStcl self) {
			PStcl segment = self.getContainer(stclContext);
			PSlot<StclContext, PStcl> already = segment.getSlot(stclContext, Slot.ALREADY);
			return already.size(stclContext, null);
		}

	}

	private class NumberOfBccSlot extends CalculatedIntegerPropertySlot<StclContext, PStcl> {

		public NumberOfBccSlot(StclContext stclContext) {
			super(stclContext, SegmentStcl.this, Slot.NUMBER_OF_BCC);
		}

		@Override
		public int getIntegerValue(StclContext stclContext, PStcl self) {
			PStcl segment = self.getContainer(stclContext);
			PSlot<StclContext, PStcl> bcc = segment.getSlot(stclContext, Slot.BCC);
			return bcc.size(stclContext, null);
		}

	}

	//
	// IMailSendListener implementation
	//

	public Result beforeSend(CommandContext<StclContext, PStcl> cmdContext, PStcl mail, PStcl recipient, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();

		// searches in sent segment
		String add = recipient.getString(stclContext, RecipientStcl.Slot.ADDRESS, "");
		String path = PathUtils.createPath(PathUtils.compose(Slot.SENT, DistributionListStcl.Slot.TO), RecipientStcl.Slot.ADDRESS, add);
		PStcl found = self.getStencil(stclContext, path);

		// if already sent
		if (found.isNotNull()) {
			self.plug(stclContext, recipient, PathUtils.compose(Slot.ALREADY, DistributionListStcl.Slot.TO));
			self.unplugOtherStencilFrom(stclContext, PathUtils.compose(Slot.TO, DistributionListStcl.Slot.TO), recipient);
			return Result.error("already sent");
		}
		return Result.success();
	}
}