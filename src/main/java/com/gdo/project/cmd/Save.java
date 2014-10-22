/*
 * Copyright GDO - 2004
 */
package com.gdo.project.cmd;

import java.io.File;
import java.io.FileWriter;

import org.apache.commons.lang3.StringUtils;

import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.factory.IStencilFactory;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.PathUtils;
import com.gdo.util.XmlWriter;

/**
 * Command to save stencil on a file.<br>
 * Parameters are :
 * <ol>
 * <li>file path (may be relative to project configuration folder or absolute)</li>
 * <ol>
 */
public class Save extends AtomicActionStcl {

	public interface Status {
		int NO_FILE_NAME = 0;
		int CANNOT_WRITE = 1;
	}

	// file name for saving
	private String _fileName;

	public Save(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		try {
			StclContext stclContext = cmdContext.getStencilContext();
			PStcl target = cmdContext.getTarget();

			// gets file (creates folder if needed)
			String dirs = PathUtils.getPathName(_fileName);
			if (StringUtils.isNotEmpty(dirs)) {
				new File(dirs).mkdirs();
			}
			File file = new File(_fileName);
			/*
			 * if (!file.canWrite()) { String msg =
			 * String.format("cannot write in file : %s", file.getAbsolutePath());
			 * return error(cmdContext, self, Status.CANNOT_WRITE, msg); }
			 */

			// saves target in file
			FileWriter out = new FileWriter(file);
			XmlWriter writer = new XmlWriter(out, 0);
			IStencilFactory<StclContext, PStcl> factory = stclContext.getStencilFactory();
			factory.saveStencil(stclContext, target, writer);
			writer.close();

			// returns status
			String msg = String.format("Stencil %s saved in %s", target, file.getAbsolutePath());
			return success(cmdContext, self, msg);
		} catch (Exception e) {
			return error(cmdContext, self, e.toString());
		}
	}

	@Override
	protected CommandStatus<StclContext, PStcl> verifyContext(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();

		// gets file name
		// (absolute path ore relative to configuration directory)
		_fileName = getParameter(cmdContext, 1, null);
		if (StringUtils.isBlank(_fileName)) {
			return error(cmdContext, self, Status.NO_FILE_NAME, "wrong empty file name (param1)");
		}
		if (!_fileName.startsWith(PathUtils.ROOT)) {
			String home = stclContext.getConfigParameter(StclContext.PROJECT_CONF_DIR);
			_fileName = PathUtils.compose(home, _fileName);
		}

		return super.verifyContext(cmdContext, self);
	}

}
