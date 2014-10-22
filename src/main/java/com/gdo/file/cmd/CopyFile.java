/*
 * Copyright GDO - 2004
 */
package com.gdo.file.cmd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.gdo.file.model.FileStcl;
import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.PathUtils;

/**
 * <param0>File name</param0>
 * 
 */
public class CopyFile extends AtomicActionStcl {

	public interface Status {
		int NO_NAME_DEFINED = 1;
	}

	private String _name; // file name

	public CopyFile(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		PStcl target = cmdContext.getTarget();
		String name = target.getName(stclContext);
		try {
			FileStcl df = (FileStcl) target.getReleasedStencil(stclContext);
			File source = df.getFile(stclContext, target);

			String path = PathUtils.compose(source.getParent(), _name);
			File dest = new File(path);
			if (!dest.createNewFile()) {
				String msg = String.format("cannot create file %s for copy", path);
				return error(cmdContext, self, msg);
			}
			FileInputStream reader = new FileInputStream(source);
			FileOutputStream writer = new FileOutputStream(dest);
			IOUtils.copy(reader, writer);
			return success(cmdContext, self, dest.hashCode());
		} catch (Exception e) {
			String msg = String.format("exception when copying %s", name);
			logError(stclContext, msg, e.toString());
			return error(cmdContext, self, msg);
		}
	}

	@Override
	protected CommandStatus<StclContext, PStcl> verifyContext(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {

		// get directory or file name
		_name = getParameter(cmdContext, 1, null);
		if (StringUtils.isEmpty(_name)) {
			return error(cmdContext, self, Status.NO_NAME_DEFINED, "no name to copy file");
		}

		// other verification
		return super.verifyContext(cmdContext, self);
	}
}