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
		this._sheetName = sheetName;
		this._sqlQuery = sqlQuery;
		this._titleRow = titleRow;
		this._headers = headers;
		this._types = types;
	}

	public ExcelQuery(String sheetName, String sqlQuery) {
		this(sheetName, sqlQuery, true, null, null);
	}

	public String getSheetName() {
		return this._sheetName;
	}

	public void setSheetName(String name) {
		this._sheetName = name;
	}

	public String getSqlQuery() {
		return this._sqlQuery;
	}

	public void setSqlQuery(String query) {
		this._sqlQuery = query;
	}

	public boolean hasTitleRow() {
		return this._titleRow;
	}

	public void setTitleRow(boolean value) {
		this._titleRow = value;
	}

	public String[] getHeaders() {
		return this._headers;
	}

	public void setHeaders(String[] headers) {
		this._headers = headers;
	}

	public String[] getTypes() {
		return this._types;
	}

	public void setTypes(String[] types) {
		this._types = types;
	}

	@Override
	public String toString() {
		return this._sqlQuery;
	}

}