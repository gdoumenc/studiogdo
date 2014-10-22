/*
 * Copyright GDO - 2004
 */
package com.gdo.ftp.cmd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.commons.lang3.StringUtils;

import com.gdo.file.model.FileStcl;
import com.gdo.ftp.model.FtpContextStcl;
import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.atom.Atom;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.PathUtils;

/**
 * Copies file to a new one.
 * <p>
 * <ol>
 * <li>File name</li>
 * </ol>
 * </p>
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
			// String path = target.getString(stclContext, "Path", "");
			String path = target.getString(stclContext, FileStcl.Slot.ABSOLUTE_PATH, "");
			// path = path.substring(0, path.lastIndexOf("/"));
			String newName = getParameter(cmdContext, 1, null);
			String newN = PathUtils.compose(PathUtils.getPathName(path), newName);

			// PStcl ftp = target.getContainer();
			PStcl pftpContext = target.getStencil(stclContext, FileStcl.Slot.CONTEXT);
			FtpContextStcl context = (FtpContextStcl) pftpContext.getReleasedStencil(stclContext);

			// check if file exist
			Result result = context.ls(stclContext, newName, true, pftpContext);
			if (result.isSuccess()) {
				Boolean found = result.getInfo(Result.SUCCESS, FtpContextStcl.PREFIX, 0);// FtpContextStcl.Index.EXISTS
				// this
				// index
				// was
				// generating
				// error
				if (found) {
					String msg = String.format("cannot copy as the file %s already exist", newName);
					return error(cmdContext, self, msg);
				} else {
					File tmp = File.createTempFile(Atom.uniqueID(), "");
					FileOutputStream out = new FileOutputStream(tmp);
					result = context.get(stclContext, out, path, true, self);

					FileInputStream in = new FileInputStream(tmp);
					result = context.put(stclContext, in, newN, false, ".back", true, self);
					return success(cmdContext, self);
				}
			} else {
				return error(cmdContext, self, result);
			}

		} catch (Exception e) {
			String msg = logError(stclContext, "exception when copying %s (%s)", name, e);
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