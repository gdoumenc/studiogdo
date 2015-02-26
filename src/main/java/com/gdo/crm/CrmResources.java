/**
 * Copyright GDO - 2005
 */
package com.gdo.crm;

import com.gdo.crm.commercial.model.ServiceStcl.Slot;
import com.gdo.project.model.ProjectStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.PathUtils;

public class CrmResources {

	public interface Resource extends ProjectStcl.Resource {

		// services
		String SERVICE_CRM = "SERVICE_CRM";

		// resources availables
		String COMMERCIAL_CONNECTED = "COMMERCIAL_CONNECTED";
		String SOCIETE = "SOCIETE";
		String CONTACT = "CONTACT";
		String ACTION_COMMERCIALE = "ACTION";
		String COMMANDE = "COMMANDE";
		String COMMERCIAL = "COMMERCIAL";
		String ACTIVITE = "ACTIVITE";
		String TYPE_ACTION = "TYPE_ACTION";

		String FILTERED_CONTACT = "FILTERED_CONTACT";

		String MAILING_SERVICE = "MAILING_SERVICE";
	}

	// default resources path
	public static String getPath(String service, String path) {
		if (Resource.COMMERCIAL_CONNECTED.equals(path))
			return "/Session/Commercial(1)";
		if (Resource.SOCIETE.equals(path))
			return PathUtils.compose(service, Slot.ALL_SOCIETES);
		if (Resource.CONTACT.equals(path))
			return PathUtils.compose(service, Slot.ALL_CONTACTS);
		if (Resource.ACTION_COMMERCIALE.equals(path))
			return PathUtils.compose(service, Slot.ALL_ACTIONS_COMMERCIALES);
		if (Resource.COMMANDE.equals(path))
			return PathUtils.compose(service, Slot.ALL_COMMANDES);
		if (Resource.COMMERCIAL.equals(path))
			return PathUtils.compose(service, Slot.COMMERCIAUX);
		if (Resource.ACTIVITE.equals(path))
			return PathUtils.compose(service, Slot.ACTIVITES);
		if (Resource.TYPE_ACTION.equals(path))
			return PathUtils.compose(service, Slot.TYPES_D_ACTION_COMMERCIALE);

		if (Resource.FILTERED_CONTACT.equals(path))
			return PathUtils.compose(service, Slot.CONTACTS);

		if (Resource.MAILING_SERVICE.equals(path))
			return "/Services(mailing)/Operations(global)";

		return "";
	}

	public interface Path {

		// session slots
		String ACTION_FAITE_FILTER = "/Session/ActionFaiteFilter";
		String ACTIVITE_FILTER = "/Session/ActiviteFilter";
		String BASE_FILTER = "/Session/BaseFilter";
		String COMMANDE_FILTER = "/Session/CommandeFilter";
		String CONTACT_FILTER = "/Session/ContactFilter";
		String CP_FILTER = "/Session/CodePostalFilter";
		String CREATION_DEBUT_FILTER = "/Session/ActionCreationDebutFilter";
		String CREATION_FIN_FILTER = "/Session/ActionCreationFinFilter";
		String DEBUT_FILTER = "/Session/ActionDebutFilter";
		String FIN_FILTER = "/Session/ActionFinFilter";
		String PRIORITE_FILTER = "/Session/PrioriteFilter";
		String PROSPECT_FILTER = "/Session/EstUnProspectFilter";
		String SIRET_FILTER = "/Session/SiretFilter";
		String SOCIETE_FILTER = "/Session/SocieteFilter";
		String TEL_FILTER = "/Session/TelFilter";
		String TYPE_ACTION_FILTER = "/Session/TypeActionFilter";
		String VILLE_FILTER = "/Session/VilleFilter";

	}

	public static void afterSessionCreated(StclContext stclContext, PStcl self) {

		// temporary stencils used by flex
		self.newPProperty(stclContext, Path.SOCIETE_FILTER, Key.NO_KEY, "");
		self.newPProperty(stclContext, Path.CONTACT_FILTER, Key.NO_KEY, "");
		self.newPProperty(stclContext, Path.TEL_FILTER, Key.NO_KEY, "");

		self.newPProperty(stclContext, Path.CREATION_DEBUT_FILTER, Key.NO_KEY, "");
		self.newPProperty(stclContext, Path.CREATION_FIN_FILTER, Key.NO_KEY, "");
		self.newPProperty(stclContext, Path.DEBUT_FILTER, Key.NO_KEY, "");
		self.newPProperty(stclContext, Path.FIN_FILTER, Key.NO_KEY, "");

		self.newPProperty(stclContext, Path.ACTION_FAITE_FILTER, Key.NO_KEY, "0");
		self.newPProperty(stclContext, Path.PROSPECT_FILTER, Key.NO_KEY, "0");
		self.newPProperty(stclContext, Path.ACTIVITE_FILTER, Key.NO_KEY, "");
		self.newPProperty(stclContext, Path.COMMANDE_FILTER, Key.NO_KEY, "0");
		self.newPProperty(stclContext, Path.PRIORITE_FILTER, Key.NO_KEY, "");
		self.newPProperty(stclContext, Path.VILLE_FILTER, Key.NO_KEY, "");
		self.newPProperty(stclContext, Path.CP_FILTER, Key.NO_KEY, "");
		self.newPProperty(stclContext, Path.BASE_FILTER, Key.NO_KEY, "");
		self.newPProperty(stclContext, Path.TYPE_ACTION_FILTER, Key.NO_KEY, "");
		self.newPProperty(stclContext, Path.SIRET_FILTER, Key.NO_KEY, "");
	}
}
