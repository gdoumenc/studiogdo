/*
 * Copyright GDO - 2004
 */
package com.gdo.ftp.cmd;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;

import com.gdo.ftp.model.FileStcl;
import com.gdo.ftp.model.FtpContextStcl;
import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.StencilUtils;

/**
 * Copies file to a new one.
 * <p>
 * <ol>
 * <li>File name</li>
 * <li>Accept intermediate folder creation.</li>
 * <li>Creation type</li>
 * </ol>
 * </p>
 */
public class CreateFile extends AtomicActionStcl {

	public interface Status {
		int NO_NAME_DEFINED = 1;
	}

	private String _name; // file name
	private boolean _folder_creation; // file name
	private int _mode; // creation mode

	public CreateFile(StclContext stclContext) {
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
			Result result = ftpContext.connect(stclContext, pftpContext);
			if (result.isNotSuccess()) {
				return error(cmdContext, self, result);
			}
			try {

				// in case of creation only, verifies the file doesn't exist
				if (_mode == com.gdo.context.model.FolderStcl.CreationMode.CREATE_ONLY) {
					if (ftpContext.exists(stclContext, _name, false, pftpContext)) {
						String msg = String.format("cannot put in file as file %s already exist", _name);
						return error(cmdContext, self, msg);
					}
				}

				// in case of creation only if doesn't exist, verifies the file
				// doesn't already exist
				if (_mode == com.gdo.context.model.FolderStcl.CreationMode.ONLY_IF_DOESNT_EXIST) {
					if (ftpContext.exists(stclContext, _name, false, pftpContext)) {
						String msg = String.format("The file %s already exist", _name);
						return success(cmdContext, self, msg);
					}
				}

				// does creation
				InputStream in = new ByteArrayInputStream(new byte[0]);
				result = ftpContext.put(stclContext, in, _name, _folder_creation, null, false, pftpContext);
				if (result.isNotSuccess()) {
					return error(cmdContext, self, result);
				}
			} finally {

				// closes connection
				result = ftpContext.close(stclContext, pftpContext);
				if (result.isNotSuccess()) {
					return error(cmdContext, self, result);
				}
			}

			// returns file created
			return success(cmdContext, self);
		} catch (Exception e) {
			String msg = logError(stclContext, "cannot create file %s (%s)", _name, e);
			return error(cmdContext, self, 0, msg);
		}
	}

	@Override
	protected CommandStatus<StclContext, PStcl> verifyContext(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {

		// file name should be defined in param1
		_name = getParameter(cmdContext, 1, null);
		if (StringUtils.isBlank(_name)) {
			return error(cmdContext, self, "no name defined for create file command (param0)");
		}

		// if param2 set to true, then accept create intermediate folders
		_folder_creation = getParameter(cmdContext, 2, true);

		// param3 is creation mode
		_mode = getParameter(cmdContext, 3, 0);

		return super.verifyContext(cmdContext, self);
	}

}