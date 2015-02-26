/*
 * Copyright GDO - 2004
 */
package com.gdo.crm.commercial.cmd;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.gdo.crm.CrmResources.Resource;
import com.gdo.crm.commercial.model._ActionCommercialeStcl;
import com.gdo.crm.commercial.model.ServiceStcl;
import com.gdo.crm.commercial.model.SocieteStcl;
import com.gdo.sql.cmd.NewSQLStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;

public class NewActionCommerciale extends NewSQLStcl {

	public interface Slot extends NewSQLStcl.Slot {
		String SERVICE_PATH = "ServicePath";
	}

	private PStcl _societe;
	private PStcl _contact;
	private PStcl _commercial;

	public NewActionCommerciale(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	protected CommandStatus<StclContext, PStcl> beforeSQLCreate(CommandContext<StclContext, PStcl> cmdContext, PStcl created, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();

		// calls super
		CommandStatus<StclContext, PStcl> status = super.beforeSQLCreate(cmdContext, created, self);
		if (status.isNotSuccess()) {
			return status;
		}

		// plugs contact and commercial if done
		if (StencilUtils.isNotNull(this._societe)) {
			created.plug(stclContext, this._societe, _ActionCommercialeStcl.Slot.SOCIETE);
		}
		if (StencilUtils.isNotNull(this._contact)) {
			created.plug(stclContext, this._contact, _ActionCommercialeStcl.Slot.EST_REALISEE_AVEC);
		}
		if (StencilUtils.isNotNull(this._commercial)) {
			created.plug(stclContext, this._commercial, _ActionCommercialeStcl.Slot.EST_REALISEE_PAR);
		} else {
			PStcl com = self.getResourceStencil(stclContext, Resource.COMMERCIAL_CONNECTED);
			created.plug(stclContext, com, _ActionCommercialeStcl.Slot.EST_REALISEE_PAR);
		}

		// set date now
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String date = dateFormat.format(new Date());
		created.setString(stclContext, _ActionCommercialeStcl.Slot.DATE_CREATION, date);

		// return ok
		return status;
	}

	@Override
	protected CommandStatus<StclContext, PStcl> verifyContext(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();

		if (getActiveStepIndex() == 1) {

			// store given parameters
			String societe = getParameter(cmdContext, 1, null);
			if (StringUtils.isNotBlank(societe)) {
				String path = PathUtils.createPath(getResourcePath(stclContext, Resource.SOCIETE, self), societe);
				this._societe = self.getStencil(stclContext, path);
			}
			String contact = getParameter(cmdContext, 2, null);
			if (StringUtils.isNotBlank(contact)) {
				String path = PathUtils.createPath(getResourcePath(stclContext, Resource.CONTACT, self), contact);
				this._contact = self.getStencil(stclContext, path);
			} else {

				// then the company should not be null
				if (StencilUtils.isNull(this._societe)) {
					return error(cmdContext, self, "La société doit être définie");
				}
				this._contact = this._societe.getStencil(stclContext, SocieteStcl.Slot.POSSEDE);
				if (StencilUtils.isNull(this._contact)) {
					return error(cmdContext, self, "La société doit avoir des contacts");
				}
			}
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
		String servPath = getResourcePath(stclContext, Resource.SERVICE_CRM, self);
		PStcl service = self.getStencil(stclContext, servPath);
		return service.getString(stclContext, ServiceStcl.Slot.ACTION_COMMERCIALE_TEMPLATE);
	}

	@Override
	protected PSlot<StclContext, PStcl> getSlot(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		return self.getResourceSlot(stclContext, Resource.ACTION_COMMERCIALE);
	}

}