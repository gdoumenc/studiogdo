/*
 * Copyright GDO - 2004
 */
package com.gdo.file.cmd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.gdo.file.model.FolderStcl;
import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.PathUtils;

/**
 * @author gdo
 * 
 */
public class CopyDir extends AtomicActionStcl {

	public interface Status {
		int NO_NAME_DEFINED = 1;
	}

	private String _name; // file name

	public CopyDir(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		PStcl target = cmdContext.getTarget();

		try {
			FolderStcl df = (FolderStcl) target.getReleasedStencil(stclContext);
			File source = df.getFile(stclContext, target);

			String path = PathUtils.compose(source.getParent(), _name);
			File dest = new File(path);
			copy(cmdContext, source, dest, self);
			return success(cmdContext, self, dest.hashCode());
		} catch (Exception e) {
			String msg = logError(stclContext, "exception when copying %s (%s)", target.getName(stclContext), e);
			return error(cmdContext, self, msg);
		}
	}

	@Override
	protected CommandStatus<StclContext, PStcl> verifyContext(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {

		// get directory target name
		_name = getParameter(cmdContext, 1, null);
		if (StringUtils.isEmpty(_name)) {
			return error(cmdContext, self, Status.NO_NAME_DEFINED, "no name to copy directory");
		}

		// other verification
		return super.verifyContext(cmdContext, self);
	}

	private CommandStatus<StclContext, PStcl> copy(CommandContext<StclContext, PStcl> cmdContext, File source, File dest, PStcl self) throws Exception {
		if (source.isDirectory()) {
			if (!dest.exists() && !dest.mkdir()) {
				String msg = String.format("cannot create directory %s for copy", dest.getAbsolutePath());
				return error(cmdContext, self, msg);
			}
			for (File s : source.listFiles()) {
				String path = PathUtils.compose(dest.getCanonicalPath(), s.getName());
				File d = new File(path);
				copy(cmdContext, s, d, self);
			}
		} else {
			FileInputStream reader = new FileInputStream(source);
			FileOutputStream writer = new FileOutputStream(dest);
			IOUtils.copy(reader, writer);
		}
		return success(cmdContext, self);
	}
}