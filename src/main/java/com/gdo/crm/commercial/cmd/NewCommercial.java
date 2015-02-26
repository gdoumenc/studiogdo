/*
 * Copyright GDO - 2004
 */
package com.gdo.crm.commercial.cmd;

import com.gdo.crm.CrmResources.Resource;
import com.gdo.crm.commercial.model.CommercialStcl;
import com.gdo.crm.commercial.model.ServiceStcl;
import com.gdo.sql.cmd.NewSQLStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;

public class NewCommercial extends NewSQLStcl {

	public interface Slot extends NewSQLStcl.Slot {
		String SERVICE_PATH = "ServicePath";
	}

	public NewCommercial(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	protected CommandStatus<StclContext, PStcl> afterSQLPlug(CommandContext<StclContext, PStcl> cmdContext, PStcl created, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();

		// calls super
		CommandStatus<StclContext, PStcl> status = super.afterSQLPlug(cmdContext, created, self);
		if (status.isNotSuccess()) {
			return status;
		}

		// plugs global mailing operation
		String servPath = getResourcePath(stclContext, Resource.MAILING_SERVICE, self);
		PStcl operation = created.getStencil(stclContext, servPath);
		created.plug(stclContext, operation, CommercialStcl.Slot.MAILINGS);

		// returns ok
		return status;
	}

	@Override
	protected String getTemplate(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		String servPath = getResourcePath(stclContext, Resource.SERVICE_CRM, self);
		PStcl service = self.getStencil(stclContext, servPath);
		return service.getString(stclContext, ServiceStcl.Slot.COMMERCIAL_TEMPLATE);
	}

	@Override
	protected PSlot<StclContext, PStcl> getSlot(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		return self.getResourceSlot(stclContext, Resource.COMMERCIAL);
	}

}