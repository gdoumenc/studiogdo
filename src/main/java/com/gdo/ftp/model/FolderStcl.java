/**
 * Copyright GDO - 2005
 */
package com.gdo.ftp.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;

import com.gdo.ftp.cmd.CreateDir;
import com.gdo.ftp.cmd.CreateFile;
import com.gdo.stencils.Result;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cond.PathCondition;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.factory.StclFactory;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.MultiCalculatedSlot;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;

public class FolderStcl extends _FileStcl {

	public interface Slot extends com.gdo.context.model.FolderStcl.Slot {
	}

	public interface Command extends com.gdo.context.model.FolderStcl.Command {
	}

	/**
	 * Constructor used to save the file.
	 */
	public FolderStcl(StclContext stclContext, String path) {
		super(stclContext, path);

		// SLOT PART

		propSlot(Slot.FOLDER_TEMPLATE, FolderStcl.class.getName());
		propSlot(Slot.FILE_TEMPLATE, FileStcl.class.getName());

		new FilesSlot(stclContext, this, Slot.FILES);
		new FilesOnlySlot(stclContext, this, Slot.FILES_ONLY);
		new FoldersOnlySlot(stclContext, this, Slot.FOLDERS_ONLY);
		new GetSlot(stclContext);

		// COMMAND PART

		command(Command.CREATE_FILE, CreateFile.class);
		command(Command.CREATE_FOLDER, CreateDir.class);
	}

	public Filter getFilter() {
		return new DirectoryFilter();
	}

	private class FilesSlot extends MultiCalculatedSlot<StclContext, PStcl> {

		protected Filter _filter;

		public FilesSlot(StclContext stclContext, Stcl in, String name) {
			super(stclContext, in, name, PSlot.ANY);
			setDoPlug(false);
		}

		@Override
		protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
			try {
				PStcl container = self.getContainer();
				PStcl pftpContext = container.getStencil(stclContext, Slot.CONTEXT);

				// gets default templates
				String folderTemplate = self.getContainer().getString(stclContext, Slot.FOLDER_TEMPLATE, FolderStcl.class.getName());
				if (StringUtils.isEmpty(folderTemplate)) {
					logWarn(stclContext, "No folder template class in slot %s in %s", Slot.FOLDER_TEMPLATE, self);
					folderTemplate = FolderStcl.class.getName();
				}
				String fileTemplate = self.getContainer().getString(stclContext, Slot.FILE_TEMPLATE, FileStcl.class.getName());
				if (StringUtils.isEmpty(fileTemplate)) {
					logWarn(stclContext, "No file template class in slot %s in %s", Slot.FILE_TEMPLATE, self);
					fileTemplate = FileStcl.class.getName();
				}

				// gets files list
				FTPClient client = newClient(stclContext, container);
				if (client == null) {
					return cleanList(stclContext, cond, self);
				}

				try {
					if (StringUtils.isNotBlank(FolderStcl.this._path) && !client.changeWorkingDirectory(FolderStcl.this._path)) {
						return cleanList(stclContext, cond, self);
					}
					FTPFile[] files = null;
					if (this._filter == null) {
						files = client.mlistDir();
					} else {
						this._filter._cond = cond;
						files = client.mlistDir(null, this._filter);
					}

					// creates files list
					for (FTPFile file : files) {
						IKey key = new Key<String>(file.getName());

						// if already in list, do nothing
						if (getStencilFromList(stclContext, key, self) != null) {
							keepStencilInList(stclContext, key, self);
							continue;
						}

						// create ftp file stencil
						String path = PathUtils.compose(FolderStcl.this._path, file.getName());
						String template = file.isDirectory() ? folderTemplate : fileTemplate;
						StclFactory stclFactory = (StclFactory) stclContext.getStencilFactory();
						PStcl stcl = null;
						if (file.getName().equals(".")) {
							stcl = stclFactory.createPStencil(stclContext, self, key, self.getContainer());
						} else if (file.getName().equals("..")) {
							PStcl parent = self.getContainer().getContainer(stclContext);
							stcl = stclFactory.createPStencil(stclContext, self, key, parent);
						} else {
							stcl = stclFactory.createPStencil(stclContext, self, key, template, path);
						}
						if (StencilUtils.isNull(stcl))
							continue;

						// plug context
						stcl.plug(stclContext, pftpContext, Slot.CONTEXT);

						// add it in list
						addStencilInList(stclContext, stcl, self);
					}
					return cleanList(stclContext, cond, self);
				} finally {
					closeClient(stclContext, client, container);
				}
			} catch (Exception e) {
				String msg = logWarn(stclContext, "Cannot get files list : %s", e);
				return StencilUtils.iterator(Result.error(msg));
			}
		}
	}

	private class FilesOnlySlot extends FilesSlot {

		public FilesOnlySlot(StclContext stclContext, Stcl in, String name) {
			super(stclContext, in, name);
			this._filter = new FileFilter();
		}

	}

	private class FoldersOnlySlot extends FilesSlot {
		// si on est dans le meme répertoir que le context.DIR

		public FoldersOnlySlot(StclContext stclContext, Stcl in, String name) {
			super(stclContext, in, name);
			this._filter = getFilter();
		}
	}

	public class GetSlot extends MultiCalculatedSlot<StclContext, PStcl> {

		public GetSlot(StclContext stclContext) {
			super(stclContext, FolderStcl.this, Slot.GET, PSlot.ANY);
			setDoPlug(false);
		}

		@Override
		protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> condition, PSlot<StclContext, PStcl> self) {
			try {

				// cannot accept no path condition as key contained
				if (!(condition instanceof PathCondition))
					return StencilUtils.iterator();
				String pathCond = ((PathCondition<StclContext, PStcl>) condition).getCondition();
				if (!PathUtils.isKeyContained(pathCond))
					return StencilUtils.iterator();

				// gets default templates
				PStcl dir = self.getContainer();
				String folderTemplate = self.getContainer().getString(stclContext, Slot.FOLDER_TEMPLATE, FolderStcl.class.getName());
				if (StringUtils.isEmpty(folderTemplate)) {
					logWarn(stclContext, "No folder template class in slot %s in %s", Slot.FOLDER_TEMPLATE, self);
					folderTemplate = FolderStcl.class.getName();
				}
				String fileTemplate = self.getContainer().getString(stclContext, Slot.FILE_TEMPLATE, FileStcl.class.getName());
				if (StringUtils.isEmpty(fileTemplate)) {
					logWarn(stclContext, "No file template class in slot %s in %s", Slot.FILE_TEMPLATE, self);
					fileTemplate = FileStcl.class.getName();
				}

				// opens connection
				PStcl pftpContext = dir.getStencil(stclContext, Slot.CONTEXT);
				FtpContextStcl ftpContext = ((FtpContextStcl) pftpContext.getReleasedStencil(stclContext));
				Result result = ftpContext.connect(stclContext, pftpContext);
				if (result.isNotSuccess())
					return StencilUtils.iterator(result);

				// get folder and file name and change directory if needed
				String path = PathUtils.getKeyContained(pathCond);
				String name = path;
				if (PathUtils.isComposed(name)) {
					String folder = PathUtils.getPathName(path);
					ftpContext.cd(stclContext, folder, false, pftpContext);
					name = PathUtils.getLastName(path);
				}

				// get FTP files list
				result = ftpContext.ls(stclContext, name, false, pftpContext);
				if (result.isNotSuccess())
					return StencilUtils.iterator(result);
				FTPFile[] files = (FTPFile[]) result.getSuccessValue(FtpContextStcl.PREFIX);

				// creates files list
				for (FTPFile file : files) {

					// if already in list, do nothing
					IKey key = new Key<String>(file.getName());
					if (getStencilFromList(stclContext, key, self) != null) {
						keepStencilInList(stclContext, key, self);
						continue;
					}

					String template = file.isDirectory() ? folderTemplate : fileTemplate;
					StclFactory factory = (StclFactory) stclContext.getStencilFactory();
					PStcl stcl = factory.createPStencil(stclContext, self, key, template, file.getName());
					if (StencilUtils.isNull(stcl))
						continue;

					// plug context
					stcl.plug(stclContext, pftpContext, Slot.CONTEXT);

					// add it in list
					addStencilInList(stclContext, stcl, self);
				}

				// closes connection
				result = ftpContext.close(stclContext, pftpContext);
				if (result.isNotSuccess())
					return StencilUtils.iterator(result);

				return cleanList(stclContext, null, self);
			} catch (Exception e) {
				String msg = logWarn(stclContext, "Cannot get files list", e);
				return StencilUtils.iterator(Result.error(msg));
			}
		}
	}

	protected abstract class Filter implements FTPFileFilter {

		public StencilCondition<StclContext, PStcl> _cond;

		@Override
		public boolean accept(FTPFile file) {
			if (!checkType(file)) {
				return false;
			}

			String key = PathCondition.getKeyCondition(this._cond);
			if (StringUtils.isBlank(key)) {
				return true;
			}

			boolean negation = false;
			if (key.startsWith(PathCondition.NOT)) {
				negation = true;
				key = key.substring(1);
			}
			if (StringUtils.isEmpty(key))
				return false;

			// composed expression
			return (negation) ? !file.getName().matches(key) : file.getName().matches(key);
		}

		public abstract boolean checkType(FTPFile file);
	}

	private class DirectoryFilter extends Filter {
		@Override
		public boolean checkType(FTPFile file) {
			return file.isDirectory();
		}
	}

	private class FileFilter extends Filter {
		@Override
		public boolean checkType(FTPFile file) {
			return !file.isDirectory();
		}
	}
}
