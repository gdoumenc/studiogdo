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
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.PathUtils;

/**
 * <param0>File name</param0>
 * 
 */
public class CreateFile extends AtomicActionStcl {

	public interface Status {
		int FILE = 1;
		int NO_NAME_DEFINED = 1;
		int IO_EXCEPTION = 2;
	}

	private String _name; // file name

	public CreateFile(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		PStcl target = cmdContext.getTarget();
		String name = target.getName(stclContext);

		try {

			// create the file
			FolderStcl df = (FolderStcl) target.getReleasedStencil(stclContext);
			File dir = df.getFile(stclContext, target);
            if (dir == null) {
                String msg = String.format("directory not found");
                return error(cmdContext, self, msg);
            }
			String path = PathUtils.compose(dir.getAbsolutePath(), this._name);
			File file = new File(path);
			if (!file.createNewFile()) {
				String msg = String.format("file %s not created", path);
				return error(cmdContext, self, msg);
			}

			// create the file stencil
			PStcl fileStcl = self.newPStencil(stclContext, (PSlot<StclContext, PStcl>) null, Key.NO_KEY, FileStcl.class, file);
			CommandStatus<StclContext, PStcl> res = success(cmdContext, self, Status.FILE, fileStcl, null);
			return success(cmdContext, self, 0, file.hashCode(), res);
		} catch (Exception e) {
			String msg = logError(stclContext, "cannot create file %s in %s (%s)", this._name, name, e);
			return error(cmdContext, self, msg);
		}
	}

	@Override
	protected CommandStatus<StclContext, PStcl> verifyContext(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {

		// get file name
		this._name = getParameter(cmdContext, 1, null);
		if (StringUtils.isEmpty(this._name)) {
			return error(cmdContext, self, Status.NO_NAME_DEFINED, "no name to create file");
		}

		// other verification
		return super.verifyContext(cmdContext, self);
	}

}