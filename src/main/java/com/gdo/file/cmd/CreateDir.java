/*
 * Copyright GDO - 2004
 */
package com.gdo.file.cmd;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import com.gdo.file.model.FolderStcl;
import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.PathUtils;

/**
 * <param0>Directory name</param0>
 * 
 */
public class CreateDir extends AtomicActionStcl {

	public interface Status {
		int NO_NAME_DEFINED = 1;
		int IO_EXCEPTION = 2;
	}

	private String _name; // file name

	public CreateDir(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		PStcl target = cmdContext.getTarget();
		String name = target.getName(stclContext);

		try {
			FolderStcl df = (FolderStcl) target.getReleasedStencil(stclContext);
			File dir = df.getFile(stclContext, target);
			String path = PathUtils.compose(dir.getAbsolutePath(), _name);
			File file = new File(path);
			if (!file.mkdir()) {
				String msg = String.format("directory %s not created", path);
				return error(cmdContext, self, msg);
			}
			return success(cmdContext, self, file.hashCode());
		} catch (Exception e) {
			String msg = logError(stclContext, "cannot create directory %s in (%s)", _name, name, e);
			return error(cmdContext, self, msg);
		}
	}

	@Override
	protected CommandStatus<StclContext, PStcl> verifyContext(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {

		// get directory name
		_name = getParameter(cmdContext, 1, null);
		if (StringUtils.isEmpty(_name)) {
			return error(cmdContext, self, Status.NO_NAME_DEFINED, "no name to create directory");
		}

		// other verification
		return super.verifyContext(cmdContext, self);
	}

}