/*
 * Copyright GDO - 2004
 */
package com.gdo.file.cmd;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import com.gdo.file.model.FileStcl;
import com.gdo.file.model.FolderStcl;
import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.PathUtils;

/**
 * <param0>New file name</param0> <param1>UrlEncoding (value =
 * true,false)</param1>
 * 
 * 
 */
public class RenameFolder extends AtomicActionStcl {

	public interface Status {
		int NO_NAME_DEFINED = 1;
		int CLASS_CAST_EXCEPTION = 2;
		int ALREADY_EXIST = 3;
	}

	private String _name; // new file name

	public RenameFolder(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		PStcl target = cmdContext.getTarget();

		// get source file
		if (!(target.getReleasedStencil(stclContext) instanceof FolderStcl)) {
			String msg = String.format("internal error: target %s must be instance of %s", target, FolderStcl.class.getName());
			return error(cmdContext, self, Status.CLASS_CAST_EXCEPTION, msg);
		}
		FolderStcl df = (FolderStcl) target.getReleasedStencil(stclContext);
		File source = df.getFile(stclContext, target);

		// get destination file
		String path = PathUtils.compose(source.getParent(), _name);
		File dest = new File(path);
		if (dest.exists()) {
			String msg = String.format("destination file %s already exist", path);
			return error(cmdContext, self, Status.ALREADY_EXIST, msg);
		}

		// rename it
		if (!source.renameTo(dest)) {
			String msg = String.format("cannot rename file %s to %s", source.getAbsolutePath(), path);
			return error(cmdContext, self, msg);
		}

		target.setString(stclContext, FileStcl.Slot.PATH, dest.getAbsolutePath());
		target.setString(stclContext, FileStcl.Slot.NAME, dest.getName());
		return success(cmdContext, self, dest.hashCode());
	}

	@Override
	protected CommandStatus<StclContext, PStcl> verifyContext(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {

		// get directory or file name
		_name = getParameter(cmdContext, 1, null);
		if (StringUtils.isEmpty(_name)) {
			return error(cmdContext, self, Status.NO_NAME_DEFINED, "no name defined to rename file");
		}

		// other verification
		return super.verifyContext(cmdContext, self);
	}
}