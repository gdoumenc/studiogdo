package com.gdo.crm.commercial.filter;

import org.apache.commons.lang3.StringUtils;

import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;

public abstract class _Filter {

	public abstract String getKeysCondition(StclContext stclContext, String c, PSlot<StclContext, PStcl> self);

	public static String addRaisonSociale(String filters, String as, String filter) {
		if (StringUtils.isNotBlank(filter)) {
			filters = String.format("%s LCASE(TRIM(%s.RaisonSociale)) LIKE LCASE('%s') AND ", filters, as, filter);
		}
		return filters;
	}

	public static String addSiret(String filters, String as, String filter) {
		if (StringUtils.isNotBlank(filter)) {
			filters = String.format("%s %s.Siret = '%s' AND ", filters, as, filter);
		}
		return filters;
	}

	public static String addProspect(String filters, String as, String filter) {
		if (StringUtils.isNotBlank(filter)) {
			if (filter.equals("1")) {
				filters = String.format("%s %s.EstUnProspect = '1' AND ", filters, as, filters);
			}
			if (filter.equals("2")) {
				filters = String.format("%s %s.EstUnProspect = '0' AND ", filters, as, filters);
			}
		}
		return filters;
	}

	public static String addPriorite(String filters, String as, String filter) {
		if (StringUtils.isNotBlank(filter)) {
			String cond = null;
			for (String s : filter.split(":")) {
				if (cond == null) {
					cond = String.format("%s.Priorite LIKE '%s'", as, s);
				} else {
					cond = String.format("%s OR %s.Priorite LIKE '%s'", cond, as, s);
				}
			}
			filters = String.format("%s (%s) AND ", filters, cond);
		}
		return filters;
	}

	public static String addLibelle(String filters, String as, String filter) {
		if (StringUtils.isNotBlank(filter)) {
			filters = String.format("%s LCASE(TRIM(%s.Libelle)) LIKE LCASE('%s') AND ", filters, as, filter);
		}
		return filters;
	}

	public static String addVille(String filters, String as, String filter) {
		if (StringUtils.isNotBlank(filter)) {
			filters = String.format("%s LCASE(TRIM(%s.Ville)) LIKE LCASE('%s') AND ", filters, as, filter);
		}
		return filters;
	}

	public static String addContactVille(String filters, String asContact, String asSociete, String filter) {
		if (StringUtils.isNotBlank(filter)) {
			String as = String.format("IF(%1$s.Ville IS NULL OR %1$s.Ville = '', %2$s.Ville, %1$s.Ville)", asContact, asSociete);
			filters = String.format("%s LCASE(TRIM(%s)) LIKE LCASE(TRIM('%s')) AND ", filters, as, filter);
		}
		return filters;
	}

	public static String addCodePostal(String filters, String as, String filter) {
		if (StringUtils.isNotBlank(filter)) {
			String cond = null;
			for (String s : filter.split(":")) {
				if (cond == null) {
					cond = String.format("%s.CodePostal LIKE '%s%%'", as, s);
				} else {
					cond = String.format("%s OR %s.CodePostal LIKE '%s%%'", cond, as, s);
				}
			}
			filters = String.format("%s (%s) AND ", filters, cond);
		}
		return filters;
	}

	public static String addContactCodePostal(String filters, String asContact, String asSociete, String filter) {
		if (StringUtils.isNotBlank(filter)) {
			String cond = null;
			for (String s : filter.split(":")) {
				String as = String.format("IF(%1$s.CodePostal IS NULL OR %1$s.CodePostal = '', %2$s.CodePostal, %1$s.CodePostal)", asContact, asSociete);
				if (cond == null) {
					cond = String.format("%s LIKE '%s%%'", as, s);
				} else {
					cond = String.format("%s OR %s LIKE '%s%%'", cond, as, s);
				}
			}
			filters = String.format("%s (%s) AND ", filters, cond);
		}
		return filters;
	}

	public static String addNom(String filters, String as, String filter) {
		if (StringUtils.isNotBlank(filter)) {
			filters = String.format("%s LCASE(TRIM(%s.Nom)) LIKE LCASE('%s') AND ", filters, as, filter);
		}
		return filters;
	}

	public static String addTelephone(String filters, String as, String filter) {
		if (StringUtils.isNotBlank(filter)) {
			filters = String.format("%s (%s.Telephone LIKE '%s' OR %s.Mobile LIKE '%s') AND ", filters, as, filter, as, filter);
		}
		return filters;
	}

	public static String addBase(String filters, String as, String filter) {
		if (StringUtils.isNotBlank(filter)) {
			filters = String.format("%s LCASE(TRIM(%s.IdentifiantBase)) LIKE LCASE('%s') AND ", filters, as, filter);
		}
		return filters;
	}

}
