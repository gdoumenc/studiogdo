/*
 * Copyright GDO - 2004
 */
package com.gdo.resource.cmd;

import com.gdo.project.model.AtomicActionStcl;
import com.gdo.project.model.ComposedActionStcl;
import com.gdo.resource.model.ResourcesMgrStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.StencilUtils;

/**
 * Command calling <tt>AddResource</tt> command several times to behave like an
 * atomic command.
 */
public class AddResourceInOneStep extends AtomicActionStcl {

	public interface Slot extends AtomicActionStcl.Slot, AddResource.Slot {
	}

	public interface Status extends AtomicActionStcl.Status, AddResource.Status {
	}

	private static final String LAUNCH_PATH = "/Session/AddResource";

	public AddResourceInOneStep(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();

		// launches the add resource composed action
		PStcl manager = cmdContext.getTarget();
		CommandStatus<StclContext, PStcl> status = manager.launch(cmdContext, ResourcesMgrStcl.Command.ADD_RESOURCE, LAUNCH_PATH);
		if (status.isNotSuccess())
			return status;

		// get the launched command to perform the next following steps
		PStcl launched = status.getInfo(CommandStatus.SUCCESS, ComposedActionStcl.class.getName(), ComposedActionStcl.Status.LAUNCHED);
		status = launched.call(stclContext, ComposedActionStcl.Command.NEXT_STEP);
		if (status.isNotSuccess())
			return status;
		status = launched.call(stclContext, ComposedActionStcl.Command.NEXT_STEP);
		if (status.isNotSuccess())
			return status;

		// plugs created resource in the created resource slot
		PStcl res = launched.getStencil(stclContext, AddResource.Slot.RESOURCE_CREATED);
		if (StencilUtils.isNull(res))
			return status;
		res = self.plug(stclContext, res, Slot.RESOURCE_CREATED);
		if (StencilUtils.isNull(res))
			return status;
		return success(cmdContext, self, 0, res, status);
	}
}