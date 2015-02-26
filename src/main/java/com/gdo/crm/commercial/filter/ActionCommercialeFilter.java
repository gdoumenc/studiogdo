package com.gdo.crm.commercial.filter;

import org.apache.commons.lang3.StringUtils;

import com.gdo.crm.CrmResources;
import com.gdo.helper.StringHelper;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;

public class ActionCommercialeFilter extends _Filter {

	@Override
	public String getKeysCondition(StclContext stclContext, String c, PSlot<StclContext, PStcl> self) {
		PStcl container = self.getContainer();

		// get filter parameters
		String filter1 = StringHelper.escapeSql(container.getString(stclContext, CrmResources.Path.ACTION_FAITE_FILTER, ""));
		String filter2 = StringHelper.escapeSql(container.getString(stclContext, CrmResources.Path.DEBUT_FILTER, ""));
		String filter3 = StringHelper.escapeSql(container.getString(stclContext, CrmResources.Path.FIN_FILTER, ""));
		String filter4 = StringHelper.escapeSql(container.getString(stclContext, CrmResources.Path.SOCIETE_FILTER, null));
		String filter5 = StringHelper.escapeSql(container.getString(stclContext, CrmResources.Path.PROSPECT_FILTER, null));
		String filter6 = StringHelper.escapeSql(container.getString(stclContext, CrmResources.Path.PRIORITE_FILTER, null));
		String filter7 = StringHelper.escapeSql(container.getString(stclContext, CrmResources.Path.ACTIVITE_FILTER, null));
		String filter8 = StringHelper.escapeSql(container.getString(stclContext, CrmResources.Path.VILLE_FILTER, null));
		String filter9 = StringHelper.escapeSql(container.getString(stclContext, CrmResources.Path.CP_FILTER, null));
		String filter10 = StringHelper.escapeSql(container.getString(stclContext, CrmResources.Path.CONTACT_FILTER, ""));
		String filter11 = StringHelper.escapeSql(container.getString(stclContext, CrmResources.Path.TEL_FILTER, ""));
		String filter12 = StringHelper.escapeSql(container.getString(stclContext, CrmResources.Path.TYPE_ACTION_FILTER, ""));

		// create filter condition
		String filters = "";
		if (StringUtils.isNotBlank(filter1)) {
			if (filter1.equals("0")) {
				filters = String.format("%s a.Faite = '0' AND ", filters);
			} else if (filter1.equals("1")) {
				filters = String.format("%s a.Faite = '1' AND ", filters);
			}
		}
		if (StringUtils.isNotBlank(filter2)) {
			filters = String.format("%s a.Quand >= '%s' AND ", filters, filter2);
		}
		if (StringUtils.isNotBlank(filter3)) {
			filters = String.format("%s a.Quand <= '%s' AND ", filters, filter3);
		}
		filters = _Filter.addRaisonSociale(filters, "s", filter4);
		filters = _Filter.addProspect(filters, "s", filter5);
		filters = _Filter.addPriorite(filters, "s", filter6);
		filters = _Filter.addLibelle(filters, "a", filter7);
		filters = _Filter.addVille(filters, "c", filter8);
		filters = _Filter.addCodePostal(filters, "c", filter9);
		filters = _Filter.addNom(filters, "c", filter10);
		filters = _Filter.addTelephone(filters, "c", filter11);

		if (StringUtils.isNotBlank(filter12)) {
			filters = String.format("%s t.Libelle = '%s' AND ", filters, filter12);
		}

		// create full condition
		return String.format("%s %s", filters, c);
	}
}