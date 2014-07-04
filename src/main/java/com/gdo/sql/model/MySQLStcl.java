/**
 * Copyright GDO - 2005
 */
package com.gdo.sql.model;

import com.gdo.project.util.model.NamedStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.MultiCalculatedSlot;

/**
 * <p>
 * Multi calculated slot which contains SqlStatement stencil produced by MySQL
 * request.
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

public class MySQLStcl extends NamedStcl {

	// private static final String MESSAGES_FILE = "com.gdo.commons.messages";
	// private static final String RECORD_TEMPLATE_NAME =
	// "com.gdo.sql.model.SQLRecordStcl";
	public static final String RECORDS = "Records";
	public static final String QUERY = "Query";

	public MySQLStcl(StclContext stclContext) {
		super(stclContext);

		/*
		 * private List _params; // query parameters public List getParams() {
		 * return _params; } public void setParams(List params) { this._params =
		 * params; }
		 */

		new MultiCalculatedSlot<StclContext, PStcl>(stclContext, this, RECORDS, PSlot.ANY) {

			@Override
			protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
				// TODO Auto-generated method stub
				return null;
			}

			/*
			 * @Override protected Map<String, PStcl> getStencilsMapTmp(StclContext
			 * stclContext, StencilCondition<StclContext, PStcl> condition,
			 * PSlot<StclContext, PStcl> self) { Map<String, PStcl> _stencils = new
			 * ArrayMap<String, PStcl>(); // get query String query =
			 * container.getNotExpandedString(stclContext, QUERY, null); if
			 * (StringUtils.isEmpty(query)) { if (getLog().isWarnEnabled()) {
			 * getLog().warn("No query associated to the MySQL stencil"); } return
			 * Collections.emptyMap(); } PreparedStatement stmt = null; ResultSet rs =
			 * null; try { // set statement stmt =
			 * SqlUtils.prepareStatement(stclContext, query); if (_params != null &&
			 * _params.size() > 0) { int i = 1; for (Iterator itr =
			 * _params.iterator(); itr.hasNext();) { stmt.setObject(i++, itr.next());
			 * } } // execute query rs = SqlUtils.preparedSelect(stmt); if (rs !=
			 * null) { int i = 0; while (rs.next()) { PStcl record =
			 * createStencil(stclContext, RECORD_TEMPLATE_NAME); _stencils.put(new
			 * Integer(i).toString(), record); // for each fields create a property //
			 * the key for plugging is the name of the field int cols =
			 * rs.getMetaData().getColumnCount(); for (int j = 1; j <= cols; j++) {
			 * IPropertyStcl<?> prop = createProperty(stclContext, rs.getString(j));
			 * _log.info("Adding-" + rs.getMetaData().getColumnName(j) + " " + prop);
			 * record.plug(stclContext, prop, rs.getMetaData().getColumnName(j),
			 * "Fields", record.self()); } i++; } } } catch (SQLException e) { // TODO
			 * why not put the error in the record? String msg =
			 * Messages.getString(stclContext, MESSAGES_FILE,
			 * "MySQLMultPSlot.Error.0"); //$NON-NLS-1$ getLog().error(msg, e); }
			 * finally { SqlUtils.closeResultSet(rs); } return _stencils; }
			 */
		};
	}

}