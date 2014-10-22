/*
 * Copyright GDO - 2004
 */
package com.gdo.sql.cmd;

import java.sql.ResultSet;

import org.apache.commons.lang3.StringUtils;

import com.gdo.project.cmd.Connect;
import com.gdo.project.util.SqlUtils;
import com.gdo.sql.model.SQLContextStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.StencilUtils;

/**
 * <param1>The user login</param1> <param2>The user passwd</param2>
 * <param3>Template class name for user plugged in /Session/User</param3>
 * <param4>The table (override the Table slot and added to the request
 * format)</param4>
 */
public class ConnectFromSql extends Connect {

	public final static String QUERY = "query";
	private static final String DEFAULT_SQL_QUERY = "SELECT `path`,`mode` FROM `%s` WHERE `name` = '%s' AND `passwd` = '%s' ;";

	private PStcl _sqlContext; // sql context used for the request

	public interface Slot extends Connect.Slot {
		String SQL_CONTEXT = "SqlContext";
		String REQUEST = "Request";
		String TABLE = "Table";
	}

	public ConnectFromSql(StclContext stclContext) {
		super(stclContext);

		singleSlot(Slot.SQL_CONTEXT);
		propSlot(Slot.TABLE, "users");
		propSlot(Slot.REQUEST, "SELECT `path`,`mode` FROM `%s` WHERE `name` = '%s' AND `passwd` = '%s'");
	}

	/**
	 * The result set of the query is in the command context.
	 */
	@Override
	protected void completeUser(CommandContext<StclContext, PStcl> cmdContext, PStcl user) {
		super.completeUser(cmdContext, user);
		ResultSet rs = (ResultSet) cmdContext.get(QUERY);
		SqlUtils.closeResultSet(cmdContext.getStencilContext(), rs);
	}

	@Override
	protected CommandStatus<StclContext, PStcl> isValid(CommandContext<StclContext, PStcl> cmdContext, String login, String passwd, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();

		try {

			// creates the query
			String q = self.getString(stclContext, Slot.REQUEST, DEFAULT_SQL_QUERY);
			String table = getParameter(cmdContext, 4, null);
			if (StringUtils.isEmpty(table)) {
				table = self.getString(stclContext, Slot.TABLE, null);
				if (StringUtils.isEmpty(table)) {
					return error(cmdContext, self, "table name cannot be null in Connect command");
				}
			}
			String query = String.format(q, table, login, passwd);
			SQLContextStcl sqlContext = (SQLContextStcl) _sqlContext.getReleasedStencil(stclContext);

			// does the query
			ResultSet rs = sqlContext.selectQuery(stclContext, query, _sqlContext);
			if (rs != null && rs.next()) {
				cmdContext.put(QUERY, rs); // used in completeUser
				CommandStatus<StclContext, PStcl> status = success(cmdContext, self, Status.INITIAL_PATH, rs.getString(1));
				status = success(cmdContext, self, Status.INITIAL_MODE, rs.getString(2), status);
				return success(cmdContext, self, Status.CONNECTED, null, status);
			}
		} catch (Exception e) {
			logError(stclContext, e.toString());
			return error(cmdContext, self, e);
		}

		// default login mode
		return error(cmdContext, self, Status.NOT_CONNECTED);
	}

	@Override
	protected CommandStatus<StclContext, PStcl> verifyContext(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();

		// no check for dev login
		String login = getParameter(cmdContext, 1, null);
		if (DEV_LOGIN.equals(login))
			return success(cmdContext, self);

		// checks the sql context is defined
		PStcl sqlStcl = getStencil(stclContext, Slot.SQL_CONTEXT, self);
		if (StencilUtils.isNull(sqlStcl)) {
			String msg = String.format("The SQL context should be defined for connection command in %s", cmdContext.getTarget());
			if (getLog().isErrorEnabled())
				getLog().error(stclContext, msg);
			return error(cmdContext, self, msg);
		}
		_sqlContext = sqlStcl;
		return super.verifyContext(cmdContext, self);
	}

}
