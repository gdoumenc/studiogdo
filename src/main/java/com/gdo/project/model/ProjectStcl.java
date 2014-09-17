/**
 * Copyright GDO - 2004
 */
package com.gdo.project.model;

import com.gdo.project.cmd.CreateAtomic;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PStcl;
import com.gdo.user.model.ProfileStcl;
import com.gdo.user.model.UserStcl;

public class ProjectStcl extends ServletStcl {

	public interface Resource extends ServletStcl.Resource {
		String USER_CONNECTED = "/Session/User(1)";
		String ROLE_CONNECTED = "/Session/Role(1)";
 	}

	public interface Slot extends ServletStcl.Slot {
		String LOGO = "Logo";

		String PROFILES = "Profiles";

		String SERVICES = "Services";
		String CONTEXTS = "Contexts";
		String RESOURCES = "Resources";
		String TMP = "Tmp";
		String SETTINGS = "Settings";
	}

	public interface Command extends ServletStcl.Command {
		// String RELOAD = "reload";
		String ADD_PROFILE = "AddProfile";
		String ADD_USER = "AddUser";
	}

	public ProjectStcl(StclContext stclContext) {
		super(stclContext);

		// sets default temporary folder
		String tmp = stclContext.getConfigParameter(StclContext.PROJECT_TMP_DIR);
		System.setProperty("java.io.tmpdir", tmp);

		// SLOT PART

		propSlot(Slot.LOGO, "");
		multiSlot(Slot.PROFILES);
		multiSlot(Slot.SERVICES);
		multiSlot(Slot.CONTEXTS);
		multiSlot(Slot.RESOURCES);
		multiSlot(Slot.TMP);
		singleSlot(Slot.SETTINGS);

		// COMMAND PART

		command(Command.ADD_PROFILE, CreateAtomic.class, ProfileStcl.class.getName(), "Target/Profiles");
		command(Command.ADD_USER, CreateAtomic.class, UserStcl.class.getName(), "Target/Users");
	}

	public String getResourcePath(StclContext stclContext, String resource, PStcl stcl) {
		if (Resource.USER_CONNECTED.equals(resource)) {
			return Resource.USER_CONNECTED;
		}
		if (Resource.ROLE_CONNECTED.equals(resource)) {
			return Resource.ROLE_CONNECTED;
		}

		return String.format("The resource path %s is not defined for %s", resource, stcl);
	}

	@Override
	public void afterSessionCreated(StclContext stclContext, PStcl self) {
		super.afterSessionCreated(stclContext, self);
	}
}