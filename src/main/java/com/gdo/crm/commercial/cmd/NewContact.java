/*
 * Copyright GDO - 2004
 */
package com.gdo.crm.commercial.cmd;

import com.gdo.crm.CrmResources.Resource;
import com.gdo.crm.commercial.model.ContactStcl;
import com.gdo.crm.commercial.model.ServiceStcl;
import com.gdo.sql.cmd.NewSQLStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;

public class NewContact extends NewSQLStcl {

	public interface Slot extends NewSQLStcl.Slot {
		String SERVICE_PATH = "ServicePath";
	}

	public NewContact(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	protected CommandStatus<StclContext, PStcl> beforeSQLCreate(CommandContext<StclContext, PStcl> cmdContext, PStcl created, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();

		// set negative id until not updated
		CommandStatus<StclContext, PStcl> status = super.beforeSQLCreate(cmdContext, created, self);
		if (status.isNotSuccess())
			return status;

		// plug societe
		PStcl societe = cmdContext.getTarget();
		created.plug(stclContext, societe, ContactStcl.Slot.SOCIETE);

		// return ok
		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdo.project.cmd.CreateInSteps#getTemplate(com.gdo.stencils.cmd.
	 * CommandContext, com.gdo.project.PStcl)
	 */
	@Override
	protected String getTemplate(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		PStcl service = self.getResourceStencil(stclContext, Resource.SERVICE_CRM);
		return service.getString(stclContext, ServiceStcl.Slot.CONTACT_TEMPLATE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdo.project.cmd.CreateInSteps#getSlot(CommandContext, PStcl)
	 */
	@Override
	protected PSlot<StclContext, PStcl> getSlot(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		return self.getResourceSlot(stclContext, Resource.CONTACT);
	}

}