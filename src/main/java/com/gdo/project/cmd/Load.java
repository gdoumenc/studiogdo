/*
 * Copyright GDO - 2004
 */
package com.gdo.project.cmd;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

import org.apache.commons.lang3.StringUtils;

import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.factory.IStencilFactory;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.PathUtils;

/**
 * Command to load stencil form a file.<br>
 * Parameters are :
 * <ol>
 * <li>file path (may be relative to project configuration folder or absolute)</li>
 * <li>plug path (relative to target) where the stencil must be plugged after
 * creation (if none then the stenci will replace the project)</li>
 * <li>key (may be not defined)</li>
 * <ol>
 */
public class Load extends AtomicActionStcl {

	public interface Status {
		int NO_FILE_NAME = 0;
		int CANNOT_READ = 1;
	}

	private String _fileName; // file name for reading

	public Load(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		try {
			StclContext stclContext = cmdContext.getStencilContext();

			// loads stencil from file
			File file = new File(this._fileName);
			if (!file.canRead()) {
				String msg = String.format("cannot read file : %s", this._fileName);
				return error(cmdContext, self, Status.CANNOT_READ, msg);
			}
			Reader in = new FileReader(file);
			IStencilFactory<StclContext, PStcl> factory = stclContext.getStencilFactory();
			PStcl stcl = factory.loadStencil(stclContext, in, this._fileName).self(stclContext, null);

			// plugs the stencil
			String plugPath = getParameter(cmdContext, 2, null);
			String key = getParameter(cmdContext, 3, null);
			if (StringUtils.isNotEmpty(plugPath)) {
				PStcl target = cmdContext.getTarget();
				if (StringUtils.isNotEmpty(key))
					target.plug(stclContext, stcl, plugPath, key);
				else
					target.plug(stclContext, stcl, plugPath);
			} else {
				plugPath = PathUtils.ROOT;
				stclContext.setServletStcl(stcl);
			}

			// returns status
			String msg = String.format("Stencil loaded from %s and plugged in %s (at key %s)", file.getAbsolutePath(), plugPath, key);
			return success(cmdContext, self, msg);
		} catch (Exception e) {
			return error(cmdContext, self, e);
		}
	}

	@Override
	protected CommandStatus<StclContext, PStcl> verifyContext(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();

		// gets file name (absolute path ore relative to configuration directory)
		this._fileName = getParameter(cmdContext, 1, null);
		if (StringUtils.isBlank(this._fileName)) {
			return error(cmdContext, self, Status.NO_FILE_NAME, "wrong empty file name (param1)");
		}
		if (!this._fileName.startsWith(PathUtils.ROOT)) {
			String home = stclContext.getConfigParameter(StclContext.PROJECT_CONF_DIR);
			this._fileName = PathUtils.compose(home, this._fileName);
		}

		return super.verifyContext(cmdContext, self);
	}

}
