/**
 * Copyright GDO - 2005
 */
package com.gdo.sql.model;

/**
 * <p>
 * Class defining the excel worksheet structure completed from a SQL query.
 * </p>
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo & Guillaume Doumenc. Use
 * is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */
public class ExcelQuery {

	private String _sheetName; // the worksheet name
	private String _sqlQuery; // the query will be used to stuff the worksheet
	private boolean _titleRow = true; // title row should be added
	private String[] _headers; // list of hearder column text in title row
	private String[] _types; // list of column type

	public ExcelQuery(String sheetName, String sqlQuery, boolean titleRow, String[] headers, String[] types) {
		_sheetName = sheetName;
		_sqlQuery = sqlQuery;
		_titleRow = titleRow;
		_headers = headers;
		_types = types;
	}

	public ExcelQuery(String sheetName, String sqlQuery) {
		this(sheetName, sqlQuery, true, null, null);
	}

	public String getSheetName() {
		return _sheetName;
	}

	public void setSheetName(String name) {
		_sheetName = name;
	}

	public String getSqlQuery() {
		return _sqlQuery;
	}

	public void setSqlQuery(String query) {
		_sqlQuery = query;
	}

	public boolean hasTitleRow() {
		return _titleRow;
	}

	public void setTitleRow(boolean value) {
		_titleRow = value;
	}

	public String[] getHeaders() {
		return _headers;
	}

	public void setHeaders(String[] headers) {
		_headers = headers;
	}

	public String[] getTypes() {
		return _types;
	}

	public void setTypes(String[] types) {
		_types = types;
	}

	@Override
	public String toString() {
		return _sqlQuery;
	}

}