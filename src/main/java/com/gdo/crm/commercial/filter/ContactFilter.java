package com.gdo.crm.commercial.filter;

import com.gdo.crm.CrmResources;
import com.gdo.helper.StringHelper;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;

public class ContactFilter extends _Filter {

	@Override
	public String getKeysCondition(StclContext stclContext, String c, PSlot<StclContext, PStcl> self) {
		PStcl container = self.getContainer();

		// get filter parameters
		String filter1 = StringHelper.escapeSql(container.getString(stclContext, CrmResources.Path.SOCIETE_FILTER, null));
		String filter2 = StringHelper.escapeSql(container.getString(stclContext, CrmResources.Path.PROSPECT_FILTER, null));
		String filter3 = StringHelper.escapeSql(container.getString(stclContext, CrmResources.Path.PRIORITE_FILTER, null));
		String filter4 = StringHelper.escapeSql(container.getString(stclContext, CrmResources.Path.ACTIVITE_FILTER, null));
		String filter5 = StringHelper.escapeSql(container.getString(stclContext, CrmResources.Path.VILLE_FILTER, null));
		String filter6 = StringHelper.escapeSql(container.getString(stclContext, CrmResources.Path.CP_FILTER, null));
		String filter7 = StringHelper.escapeSql(container.getString(stclContext, CrmResources.Path.CONTACT_FILTER, null));
		String filter8 = StringHelper.escapeSql(container.getString(stclContext, CrmResources.Path.TEL_FILTER, null));
		String filter9 = StringHelper.escapeSql(container.getString(stclContext, CrmResources.Path.BASE_FILTER, null));

		// create filter condition
		String filters = "";
		filters = addRaisonSociale(filters, "s", filter1);
		filters = addProspect(filters, "s", filter2);
		filters = addPriorite(filters, "s", filter3);
		filters = addLibelle(filters, "a", filter4);
		filters = addContactVille(filters, "c", "s", filter5);
		filters = addContactCodePostal(filters, "c", "s", filter6);
		filters = addNom(filters, "c", filter7);
		filters = addTelephone(filters, "c", filter8);
		filters = addBase(filters, "c", filter9);

		// create full condition
		return String.format("%s %s", filters, c);
	}
}