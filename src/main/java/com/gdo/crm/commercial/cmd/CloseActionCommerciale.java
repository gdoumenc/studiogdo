/*
 * Copyright GDO - 2004
 */
package com.gdo.crm.commercial.cmd;

import com.gdo.crm.CrmResources.Resource;
import com.gdo.crm.commercial.model._ActionCommercialeStcl;
import com.gdo.crm.commercial.model.CommandeStcl;
import com.gdo.crm.commercial.model.ServiceStcl;
import com.gdo.crm.commercial.model.SocieteStcl;
import com.gdo.project.model.ComposedActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;

public class CloseActionCommerciale extends ComposedActionStcl {

	public interface Slot extends ComposedActionStcl.Slot {
		String HAS_COMMANDE = "HasCommande";
		String HAS_SUITE = "HasSuite";

		String COMMANDE = "Commande";

		String QUAND = "Quand";
		String REMARQUES = "Remarques";
		String TYPE = "Type";
	}

	public CloseActionCommerciale(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> performSteps(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		PStcl target = cmdContext.getTarget();
		int currentStep = getActiveStepIndex();

		// set possible command paramters
		if (currentStep == 1) {
			createCommande(cmdContext, self);
		}

		// create devis/commande and/or suite
		if (currentStep == 2) {
			boolean commande = self.getBoolean(stclContext, Slot.HAS_COMMANDE, false);
			if (commande) {
				PStcl stcl = self.getStencil(stclContext, Slot.COMMANDE);
				self.plug(stclContext, stcl, self.getResourceSlot(stclContext, Resource.COMMANDE));
			}

			boolean suite = self.getBoolean(stclContext, Slot.HAS_SUITE, false);
			if (suite) {
				PStcl service = self.getResourceStencil(stclContext, Resource.SERVICE_CRM);
				String template = service.getString(stclContext, ServiceStcl.Slot.ACTION_COMMERCIALE_TEMPLATE);
				PSlot<StclContext, PStcl> slot = self.getResourceSlot(stclContext, Resource.ACTION_COMMERCIALE);
				PStcl stcl = self.newPStencil(stclContext, slot, Key.NO_KEY, template);

				// add new parameters
				PStcl type = self.getStencil(stclContext, Slot.TYPE);
				stcl.plug(stclContext, type, _ActionCommercialeStcl.Slot.TYPE);
				stcl.setString(stclContext, _ActionCommercialeStcl.Slot.QUAND, self.getString(stclContext, Slot.QUAND, ""));

				// concatenate old and suite remarks
				String s1 = target.getString(stclContext, _ActionCommercialeStcl.Slot.REMARQUES, "");
				String s2 = self.getString(stclContext, Slot.REMARQUES, "");
				stcl.setString(stclContext, _ActionCommercialeStcl.Slot.REMARQUES, s1 + "\n" + s2);

				// set default values
				stcl.setString(stclContext, _ActionCommercialeStcl.Slot.LIBELLE_TYPE, target.getString(stclContext, _ActionCommercialeStcl.Slot.LIBELLE_TYPE, ""));
				stcl.setString(stclContext, _ActionCommercialeStcl.Slot.NOM_SOCIETE, target.getString(stclContext, _ActionCommercialeStcl.Slot.NOM_SOCIETE, ""));
				stcl.setString(stclContext, _ActionCommercialeStcl.Slot.NOM_CONTACT, target.getString(stclContext, _ActionCommercialeStcl.Slot.NOM_CONTACT, ""));
				stcl.setString(stclContext, _ActionCommercialeStcl.Slot.ID_SOCIETE, target.getString(stclContext, _ActionCommercialeStcl.Slot.ID_SOCIETE, ""));
				stcl.setString(stclContext, _ActionCommercialeStcl.Slot.ID_CONTACT, target.getString(stclContext, _ActionCommercialeStcl.Slot.ID_CONTACT, ""));

				PStcl societe = target.getStencil(stclContext, _ActionCommercialeStcl.Slot.SOCIETE);
				stcl.plug(stclContext, societe, _ActionCommercialeStcl.Slot.SOCIETE);

				PStcl user = target.getStencil(stclContext, _ActionCommercialeStcl.Slot.EST_REALISEE_PAR);
				stcl.plug(stclContext, user, _ActionCommercialeStcl.Slot.EST_REALISEE_PAR);
				target.plug(stclContext, stcl, _ActionCommercialeStcl.Slot.NECESSITE_PROCHAINE_ACTION);

				PStcl contact = target.getStencil(stclContext, _ActionCommercialeStcl.Slot.EST_REALISEE_AVEC);
				stcl.plug(stclContext, contact, _ActionCommercialeStcl.Slot.EST_REALISEE_AVEC);

				stcl.call(stclContext, CommandeStcl.Command.UPDATE);
			}

			// update closed action
			target.setBoolean(stclContext, _ActionCommercialeStcl.Slot.FAITE, true);
			target.call(stclContext, _ActionCommercialeStcl.Command.UPDATE);
		}

		return success(cmdContext, self);
	}

	protected PStcl createCommande(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		PStcl target = cmdContext.getTarget();

		PStcl service = self.getResourceStencil(stclContext, Resource.SERVICE_CRM);
		String template = service.getString(stclContext, ServiceStcl.Slot.COMMANDE_TEMPLATE);
		PStcl stcl = self.newPStencil(stclContext, Slot.COMMANDE, Key.NO_KEY, template);

		PStcl contact = target.getStencil(stclContext, _ActionCommercialeStcl.Slot.EST_REALISEE_AVEC);
		stcl.plug(stclContext, contact, CommandeStcl.Slot.PASSE_AVEC);
		PStcl societe = target.getStencil(stclContext, _ActionCommercialeStcl.Slot.SOCIETE);
		stcl.plug(stclContext, societe, CommandeStcl.Slot.SOCIETE);
		stcl.setInt(stclContext, CommandeStcl.Slot.ID_SOCIETE, societe.getInt(stclContext, SocieteStcl.Slot.ID, 0));

		stcl.setString(stclContext, CommandeStcl.Slot.NOM_SOCIETE, target.getString(stclContext, _ActionCommercialeStcl.Slot.NOM_SOCIETE));
		stcl.setString(stclContext, CommandeStcl.Slot.NOM_CONTACT, target.getString(stclContext, _ActionCommercialeStcl.Slot.NOM_CONTACT));

		PStcl commercial = target.getStencil(stclContext, _ActionCommercialeStcl.Slot.EST_REALISEE_PAR);
		stcl.plug(stclContext, commercial, CommandeStcl.Slot.COMMERCIAL);
		stcl.setString(stclContext, CommandeStcl.Slot.NOM_COMMERCIAL, target.getString(stclContext, _ActionCommercialeStcl.Slot.EST_REALISEE_PAR_NOM));

		return stcl;
	}

}