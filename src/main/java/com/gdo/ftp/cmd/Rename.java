/*
 * Copyright GDO - 2004
 */
package com.gdo.ftp.cmd;

import org.apache.commons.lang3.StringUtils;

import com.gdo.context.model.FileStcl;
import com.gdo.ftp.model.FtpContextStcl;
import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;

/**
 * Renames file or folder.
 * <p>
 * <ol>
 * <li>File name</li>
 * </ol>
 * </p>
 */
public class Rename extends AtomicActionStcl {

	public interface Status {
		int NO_NAME_DEFINED = 1;
		int CLASS_CAST_EXCEPTION = 2;
		int ALREADY_EXIST = 3;
	}

	private String _name; // new file name

	public Rename(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		try {
			PStcl target = cmdContext.getTarget();

			// gets ftp context
			PStcl pftpContext = target.getStencil(stclContext, FileStcl.Slot.CONTEXT);
			if (StencilUtils.isNull(pftpContext)) {
				String msg = String.format("There is no context for FTP create file command");
				return error(cmdContext, self, msg);
			}
			FtpContextStcl ftpContext = (FtpContextStcl) pftpContext.getReleasedStencil(stclContext);

			// opens connection
			String old = target.getString(stclContext, FileStcl.Slot.PATH);
			Result result = ftpContext.rename(stclContext, old, _name, true, pftpContext);
			if (result.isSuccess()) {
				target.setString(stclContext, FileStcl.Slot.PATH, _name);
			}
			return success(cmdContext, self, result);
		} catch (Exception e) {
			String msg = logError(stclContext, "cannot create file %s (%s)", _name, e);
			return error(cmdContext, self, msg);
		}
	}

	@Override
	protected CommandStatus<StclContext, PStcl> verifyContext(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {

		_name = getParameter(cmdContext, 1, null);
		if (StringUtils.isBlank(_name))
			return error(cmdContext, self, "no name defined to rename file");
		if (PathUtils.isComposed(_name))
			return error(cmdContext, self, "cannot use composed name to rename a file");

		return super.verifyContext(cmdContext, self);
	}
}