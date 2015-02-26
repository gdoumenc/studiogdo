package com.gdo.crm.commercial.filter;

import org.apache.commons.lang3.StringUtils;

import com.gdo.crm.CrmResources;
import com.gdo.helper.StringHelper;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;

public class CommandeFilter extends _Filter {

	@Override
	public String getKeysCondition(StclContext stclContext, String c, PSlot<StclContext, PStcl> self) {
		PStcl container = self.getContainer();

		// get filter parameters
		String filter1 = StringHelper.escapeSql(container.getString(stclContext, CrmResources.Path.SOCIETE_FILTER, ""));
		String filter2 = StringHelper.escapeSql(container.getString(stclContext, CrmResources.Path.DEBUT_FILTER, ""));
		String filter3 = StringHelper.escapeSql(container.getString(stclContext, CrmResources.Path.FIN_FILTER, ""));
		String filter4 = StringHelper.escapeSql(container.getString(stclContext, CrmResources.Path.COMMANDE_FILTER, ""));
		String filter5 = StringHelper.escapeSql(container.getString(stclContext, CrmResources.Path.CREATION_DEBUT_FILTER, ""));
		String filter6 = StringHelper.escapeSql(container.getString(stclContext, CrmResources.Path.CREATION_FIN_FILTER, ""));

		// create filter condition
		String filters = "";
		if (StringUtils.isNotBlank(filter1)) {
			filters = String.format("%s LCASE(s.RaisonSociale) LIKE LCASE('%s') AND ", filters, filter1);
		}
		if (StringUtils.isNotBlank(filter2)) {
			filters = String.format("%s c.DatePrevisionnelle >= '%s' AND ", filters, filter2);
		}
		if (StringUtils.isNotBlank(filter3)) {
			filters = String.format("%s c.DatePrevisionnelle <= '%s' AND ", filters, filter3);
		}
		if (StringUtils.isNotBlank(filter5)) {
			filters = String.format("%s c.DateCreation >= '%s' AND ", filters, filter5);
		}
		if (StringUtils.isNotBlank(filter6)) {
			filters = String.format("%s c.DateCreation <= '%s' AND ", filters, filter6);
		}
		if (StringUtils.isNotBlank(filter4)) {
			if (filter4.equals("0")) {
				filters = String.format("%s c.EstUnDevis AND !c.Perdu AND ", filters);
			} else if (filter4.equals("1")) {
				filters = String.format("%s !c.EstUnDevis AND ", filters);
			} else if (filter4.equals("2")) {
				filters = String.format("%s c.EstUnDevis AND c.Perdu AND ", filters);
			}
		}

		// create full condition
		return String.format("%s %s", filters, c);
	}
}