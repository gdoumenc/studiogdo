/*
 * Copyright GDO - 2004
 */
package com.gdo.ftp.cmd;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;

import com.gdo.ftp.model.FolderStcl;
import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.PathUtils;

public class Delete extends AtomicActionStcl {

	public Delete(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		try {
			PStcl target = cmdContext.getTarget();

			// gets ftp context
			FolderStcl file = target.getReleasedStencil(stclContext);
			FTPClient client = file.newClient(stclContext, target);

			try {

				// get file
				String path = target.getString(stclContext, FolderStcl.Slot.PATH);

				// changes directory
				if (PathUtils.isComposed(path)) {
					String last = PathUtils.getLastName(path);
					path = PathUtils.getPathName(path);
					if (!client.changeWorkingDirectory(path)) {
						String msg = logError(stclContext, "cannot change to dir %s", path);
						return error(cmdContext, self, msg);
					}
					path = last;
				}

				// does deletion
				FTPFile[] files = client.mlistDir(null, new Filter(path));
				boolean deleted = false;
				if (files[0].isDirectory()) {
					deleted = client.removeDirectory(path);
				} else {
					deleted = client.deleteFile(path);
				}

				// succeed
				if (deleted) {
					String msg = String.format("%s deleted", path);
					return success(cmdContext, self, msg);
				}
				String msg = String.format("%s not deleted", path);
				return error(cmdContext, self, msg);
			} finally {
				file.closeClient(stclContext, client, target);
			}
		} catch (Exception e) {
			String msg = logError(stclContext, "cannot delete dir %s : %s", this._name, e);
			return error(cmdContext, self, msg);
		}
	}

	private class Filter implements FTPFileFilter {
		String _path;

		Filter(String path) {
			_path = path;
		}

		@Override
		public boolean accept(FTPFile file) {
			return file.getName().equals(_path);
		}
	}

}