package com.gdo.crm.commercial.cmd;

import com.gdo.crm.commercial.model.ContactStcl;
import com.gdo.project.cmd.Unplug;
import com.gdo.sql.slot.SQLSlot;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;

public class DeleteSociete extends Unplug {

	public DeleteSociete(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		PStcl societe = cmdContext.getTarget();

		// removes all actions done with this company
		PSlot<StclContext, PStcl> pslot = societe.getSlot(stclContext, ContactStcl.Slot.ACTION_COMMERCIALE);
		SQLSlot slot = pslot.getSlot();
		slot.unplugAll(stclContext, pslot);

		// removes all actions done with this company
		pslot = societe.getSlot(stclContext, ContactStcl.Slot.COMMANDE);
		slot = pslot.getSlot();
		slot.unplugAll(stclContext, pslot);

		// removes the contact
		return super.doAction(cmdContext, self);
	}

}