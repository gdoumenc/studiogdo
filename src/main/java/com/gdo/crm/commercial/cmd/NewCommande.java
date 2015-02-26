/*
 * Copyright GDO - 2004
 */
package com.gdo.crm.commercial.cmd;

import org.apache.commons.lang3.StringUtils;

import com.gdo.crm.CrmResources.Resource;
import com.gdo.crm.commercial.model.CommandeStcl;
import com.gdo.crm.commercial.model.ServiceStcl;
import com.gdo.sql.cmd.NewSQLStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;

public class NewCommande extends NewSQLStcl {

	public interface Slot extends NewSQLStcl.Slot {
		String SERVICE_PATH = "ServicePath";
	}

	private PStcl _societe;
	private PStcl _contact;
	private PStcl _commercial;

	public NewCommande(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	protected CommandStatus<StclContext, PStcl> beforeSQLCreate(CommandContext<StclContext, PStcl> cmdContext, PStcl created, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();

		// set negative id until not updated
		CommandStatus<StclContext, PStcl> status = super.beforeSQLCreate(cmdContext, created, self);
		if (status.isNotSuccess())
			return status;

		// plug societe, contact and commercial if done
		if (StencilUtils.isNotNull(this._societe)) {
			created.plug(stclContext, this._societe, CommandeStcl.Slot.SOCIETE);
		}
		if (StencilUtils.isNotNull(this._contact)) {
			created.plug(stclContext, this._contact, CommandeStcl.Slot.PASSE_AVEC);
		}
		if (StencilUtils.isNotNull(this._commercial)) {
			created.plug(stclContext, this._commercial, CommandeStcl.Slot.COMMERCIAL);
		}

		// return ok
		return status;
	}

	@Override
	protected CommandStatus<StclContext, PStcl> verifyContext(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();

		// store given parameters
		if (this._societe == null) {
			String societe = getParameter(cmdContext, 1, null);
			if (StringUtils.isNotBlank(societe)) {
				String path = PathUtils.createPath(getResourcePath(stclContext, Resource.SOCIETE, self), societe);
				this._societe = self.getStencil(stclContext, path);
			}
		}
		if (this._contact == null) {
			String contact = getParameter(cmdContext, 2, null);
			if (StringUtils.isNotBlank(contact)) {
				String path = PathUtils.createPath(getResourcePath(stclContext, Resource.CONTACT, self), contact);
				this._contact = self.getStencil(stclContext, path);
			}
		}
		if (this._commercial == null) {
			String commercial = getParameter(cmdContext, 3, null);
			if (StringUtils.isNotBlank(commercial)) {
				this._commercial = self.getStencil(stclContext, commercial);
			}
		}

		return super.verifyContext(cmdContext, self);
	}

	@Override
	protected String getTemplate(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		PStcl service = self.getResourceStencil(stclContext, Resource.SERVICE_CRM);
		return service.getString(stclContext, ServiceStcl.Slot.COMMANDE_TEMPLATE);
	}

	@Override
	protected PSlot<StclContext, PStcl> getSlot(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		return self.getResourceSlot(stclContext, Resource.COMMANDE);
	}

}