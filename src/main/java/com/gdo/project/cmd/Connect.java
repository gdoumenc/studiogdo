/*
 * Copyright GDO - 2004
 */
package com.gdo.project.cmd;

import org.apache.commons.lang3.StringUtils;

import com.gdo.project.model.AtomicActionStcl;
import com.gdo.project.model.ProjectStcl;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils._Stencil;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.PathUtils;

public class Connect extends AtomicActionStcl {

	// login access view
	protected static final String LOGIN_PATH = "/";
	protected static final String LOGIN_MODE = "login";
	protected static final String WRONG_LOGIN_PATH = "/";
	protected static final String WRONG_LOGIN_MODE = "login.wrong";

	// dev interface
	protected static final String DEV_LOGIN = "dev";
	private static final String DEV_PASSWD = "67hH:.az";
	private static final String DEV_MODE = "dev.explorer";

	public interface Slot extends AtomicActionStcl.Slot {
		String USERS = "Users";
		String USER_TEMPLATE = "UserTemplate";

		public interface User extends Stcl.Slot {
			String PASSWD = "Passwd";
			String PATH = "Path";
			String MODE = "Mode";
		}
	}

	protected interface Status {
		int CONNECTED = 0;
		int INITIAL_PATH = 1;
		int INITIAL_MODE = 2;
		int NOT_CONNECTED = 1;
	}

	public Connect(StclContext stclContext) {
		super(stclContext);
	}

	/**
	 * Checks if the login/passwd are valid.
	 * 
	 * @param cmdContext
	 *          the command context.
	 * @param login
	 *          the login value.
	 * @param passwd
	 *          the password value.
	 * @param self
	 *          the stencil as a plugged stencil.
	 * @return command status info to render the project.
	 *         <ul>
	 *         <li>index 0 : connection status</li>
	 *         <li>index 1 : initial path</li>
	 *         <li>index 2 : initial mode</li>
	 *         </ul>
	 */
	protected CommandStatus<StclContext, PStcl> isValid(CommandContext<StclContext, PStcl> cmdContext, String login, String passwd, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();

		// in name is users defined
		for (PStcl user : getStencils(stclContext, Slot.USERS, self)) {
			String name = user.getString(stclContext, Slot.User.NAME, null);
			if (login.equals(name)) {
				String p = user.getString(stclContext, Slot.User.PASSWD, null);
				if (StringUtils.isEmpty(p) || p.equals(passwd)) {
					String path = user.getString(stclContext, Slot.User.PATH, null);
					String mode = user.getString(stclContext, Slot.User.MODE, null);
					CommandStatus<StclContext, PStcl> status = success(cmdContext, self, Status.INITIAL_PATH, path);
					status = success(cmdContext, self, Status.INITIAL_MODE, mode, status);
					return success(cmdContext, self, Status.CONNECTED, null, status);
				}
			}
		}

		return error(cmdContext, self, Status.NOT_CONNECTED, null, getWrongLoginValues(cmdContext, self));
	}

	/**
	 * The template used to create the session user.
	 * 
	 * @param cmdContext
	 *          the command context.
	 * @param self
	 *          the stencil as a plugged stencil.
	 * @return the template used to create the session user.
	 */
	protected String getUserTemplate(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();

		// get it from parameter 3 or property
		String template = getParameter(cmdContext, 3, null);
		if (StringUtils.isBlank(template)) {
			template = self.getString(stclContext, Slot.USER_TEMPLATE, null);
		}
		if (StringUtils.isBlank(template)) {
			template = Stcl.class.getName();
		}
		return template;
	}

	/**
	 * Completes the user stencil created in session (plugged in /Session/User).
	 * 
	 * @param cmdContext
	 *          the command context.
	 * @param user
	 *          the user stencil created.
	 */
	protected void completeUser(CommandContext<StclContext, PStcl> cmdContext, PStcl user) {
		StclContext stclContext = cmdContext.getStencilContext();

		String login = getParameter(cmdContext, 1, null);
		user.setString(stclContext, _Stencil.Slot.NAME, login);
	}

	/**
	 * Should be redefined to perform other actions after connection.
	 * 
	 * @param cmdContext
	 *          the command context.
	 */
	protected CommandStatus<StclContext, PStcl> afterConnection(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		return success(cmdContext, self);
	}

	/**
	 * Returns infos to render the project after connection.
	 * 
	 * @param cmdContext
	 *          the stencil context.
	 * @param self
	 *          this stencil as plugged stencil.
	 * @return command status info to render login panel.
	 *         <ul>
	 *         <li>index 0 : connection status</li>
	 *         <li>index 1 : initial path</li>
	 *         <li>index 2 : initial mode</li>
	 *         </ul>
	 */
	@Override
	public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		PStcl target = cmdContext.getTarget();
		CommandStatus<StclContext, PStcl> status;

		// get login/passwd user info
		String login = getParameter(cmdContext, 1, null);
		String passwd = getParameter(cmdContext, 2, null);

		// if login is null then ask for anonymous login
		if (login == null) {
			return success(cmdContext, self, Status.CONNECTED, null, getLoginValues(cmdContext, self));
		}

		// login may be empty for anonymous login
		if (StringUtils.isEmpty(login)) {

			// anonymous login if defined in default parameters
			String path = getParameter(cmdContext, 4, null);
			String mode = getParameter(cmdContext, 5, null);
			if (StringUtils.isEmpty(path) || StringUtils.isEmpty(mode)) {
				return error(cmdContext, self, Status.NOT_CONNECTED, null, getWrongLoginValues(cmdContext, self));
			}
			status = createConnectionStatus(cmdContext, path, mode, self);
		}

		// then neither login or passwd may be empty
		else {

			// dev case
			if (DEV_LOGIN.equals(login)) {
				if (!DEV_PASSWD.equals(passwd)) {
					return error(cmdContext, self, Status.NOT_CONNECTED, null, getWrongLoginValues(cmdContext, self));
				}
				status = createConnectionStatus(cmdContext, PathUtils.ROOT, DEV_MODE, self);
			}

			// check login/passwd and render user starting facet
			else {
				status = isValid(cmdContext, login, passwd, self);
			}
		}

		// DEBUG Force connexion if no SQL server available
		/*
		 * if (status.isNotSuccess()) { status = createConnectionStatus(cmdContext,
		 * "/", "admin", self); }
		 */

		// returns in cas on no success
		if (status.isNotSuccess()) {
			if (getLog().isWarnEnabled()) {
				String msg = String.format("Error connecting %s/%s", login, passwd);
				getLog().warn(stclContext, msg);
			}
			return status;
		}

		// creates the connected user in session
		String userPath = getResourcePath(stclContext, ProjectStcl.Resource.USER_CONNECTED, self);
		target.clearSlot(stclContext, userPath);

		// creates user by template if template defined
		String template = getUserTemplate(cmdContext, self);
		if (StringUtils.isNotBlank(template)) {
			PStcl user = target.newPStencil(stclContext, userPath, Key.NO_KEY, template);
			completeUser(cmdContext, user);
		}

		// returns after calling after connection
		status.addOther(afterConnection(cmdContext, self));
		return status;
	}

	/**
	 * Returns infos to render the login panel.
	 * 
	 * @param cmdContext
	 *          the stencil context.
	 * @param self
	 *          this stencil as plugged stencil.
	 * @return command status info to render login panel.
	 *         <ul>
	 *         <li>index 1 : initial path</li>
	 *         <li>index 2 : initial mode</li>
	 *         </ul>
	 */
	protected CommandStatus<StclContext, PStcl> getLoginValues(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		CommandStatus<StclContext, PStcl> status = success(cmdContext, self, Status.INITIAL_PATH, LOGIN_PATH);
		return success(cmdContext, self, Status.INITIAL_MODE, LOGIN_MODE, status);
	}

	/**
	 * Returns infos to render the login panel with error message.
	 * 
	 * @param cmdContext
	 *          the stencil context.
	 * @param self
	 *          this stencil as plugged stencil.
	 * @return command status info to render login panel.
	 *         <ul>
	 *         <li>index 1 : initial path</li>
	 *         <li>index 2 : initial mode</li>
	 *         </ul>
	 */
	protected CommandStatus<StclContext, PStcl> getWrongLoginValues(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		CommandStatus<StclContext, PStcl> status = success(cmdContext, self, Status.INITIAL_PATH, WRONG_LOGIN_PATH);
		return success(cmdContext, self, Status.INITIAL_MODE, WRONG_LOGIN_MODE, status);
	}

	/**
	 * FOrece a connexion with path and mode defined.
	 * 
	 * @param cmdContext
	 *          the command context.
	 * @param path
	 *          the initial path.
	 * @param mode
	 *          the initial mode.
	 * @param self
	 *          the command as a plugged stencil.
	 * @return the success status.
	 */
	protected CommandStatus<StclContext, PStcl> createConnectionStatus(CommandContext<StclContext, PStcl> cmdContext, String path, String mode, PStcl self) {
		CommandStatus<StclContext, PStcl> status = success(cmdContext, self, Status.INITIAL_PATH, path);
		status = success(cmdContext, self, Status.INITIAL_MODE, mode, status);
		status = success(cmdContext, self, Status.CONNECTED, null, status);
		return status;
	}
}
