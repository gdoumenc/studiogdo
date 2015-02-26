package com.gdo.crm.commercial.cmd;

import com.gdo.crm.commercial.model.ContactStcl;
import com.gdo.project.cmd.Unplug;
import com.gdo.sql.slot.SQLSlot;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;

public class DeleteContact extends Unplug {

	public DeleteContact(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		PStcl contact = cmdContext.getTarget();

		// removes all actions done with this contact
		PSlot<StclContext, PStcl> pslot = contact.getSlot(stclContext, ContactStcl.Slot.ACTION_COMMERCIALE);
		SQLSlot slot = pslot.getSlot();
		slot.unplugAll(stclContext, pslot);

		// removes all actions done with this contact
		pslot = contact.getSlot(stclContext, ContactStcl.Slot.COMMANDE);
		slot = pslot.getSlot();
		slot.unplugAll(stclContext, pslot);

		// removes the contact
		return super.doAction(cmdContext, self);
	}

}