/*
 * Copyright GDO - 2004
 */
package com.gdo.file.cmd;

import java.io.File;

import com.gdo.file.model.FileStcl;
import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;

public class DeleteFile extends AtomicActionStcl {

	public interface Status {
		int CLASS_CAST_EXCEPTION = 1;
	}

	public DeleteFile(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		PStcl target = cmdContext.getTarget();

		// gets file name
		String name = target.getName(stclContext);

		// gets file
		if (!(target.getReleasedStencil(stclContext) instanceof FileStcl)) {
			String msg = String.format("internal error: target %s must be instance of %s", target, FileStcl.class.getName());
			return error(cmdContext, self, Status.CLASS_CAST_EXCEPTION, msg);
		}
		FileStcl df = (FileStcl) target.getReleasedStencil(stclContext);
		File file = df.getFile(stclContext, target);

		// deletes it
		if (!file.delete()) {
			String msg = String.format("%s not deleted", name);
			return error(cmdContext, self, 0, msg);
		}
		target.unplugFromAllSlots(stclContext);

		// resturns success
		String msg = String.format("%s deleted", name);
		return success(cmdContext, self, 0, msg);
	}
}