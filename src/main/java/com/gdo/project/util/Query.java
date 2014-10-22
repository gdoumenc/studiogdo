/**
 * Copyright GDO - 2005
 */
package com.gdo.project.util;

public class Query {

	private String _query;

	public Query(String format, Object... params) {
		Object[] escaped = new Object[params.length];
		int i = 0;
		for (Object param : params) {

			// we escape dangerous params here
			if (param instanceof String) {
				String p = (String) param;
				p = p.replaceAll("\\\\", "\\\\\\\\");
				p = p.replaceAll("'", "\\\\'");
				p = p.replaceAll("\"", "\\\\\"");
				escaped[i] = p;
			} else {
				escaped[i] = param;
			}
			i++;
		}
		_query = String.format(format, escaped);
	}

	public String query() {
		return _query;
	}
}
