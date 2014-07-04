/**
 * Copyright GDO - 2004
 */
package com.gdo.ftp.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import com.gdo.context.model.ContextStcl;
import com.gdo.ftp.cmd.Put;
import com.gdo.ftp.cmd.TestConnection;
import com.gdo.stencils.Result;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.event.IPropertyChangeListener;
import com.gdo.stencils.event.PropertyChangeEvent;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.prop.IPPropStencil;
import com.gdo.stencils.slot.CalculatedBooleanPropertySlot;
import com.gdo.stencils.util.PathUtils;

public class FtpContextStcl extends FolderStcl implements IPropertyChangeListener<StclContext, PStcl> {

	public interface Slot extends FolderStcl.Slot {
		String HOST = "FtpAddress";
		String PORT = "FtpPort";
		String USER = "FtpUser";
		String PASSWD = "FtpPasswd";

		String FTP_DIR = "FtpDir"; // initial dir
		String RELATIVE_DIR = "RelativeDir";

		String ENCODING_TYPE = "EncodingType";

		String DIR = "Dir";
		String HTTP_DIR = "HttpDir"; // http access to the ftp space (if exists)

		String CONNECTED = "Connected"; // check is currently connected
	}

	public interface Command extends ContextStcl.Command, FolderStcl.Command {
		String TEST_FTP_CONNECTION = "TestFtpConnection";
		String PUT = "Put";
	}

	public interface Status {
		int CONNECT = 1;
		int EXISTS = 2;
		int CLOSE = 3;
	}

	public static final String PREFIX = "ftp";

	public FTPClient _client;// ftp context client saved during all session
	private String _pwd;// initial pwd at connection

	public FtpContextStcl(StclContext stclContext) {
		super(stclContext, "");

		// SLOT PART

		propSlot(Slot.HOST);
		propSlot(Slot.PORT);
		propSlot(Slot.USER);
		propSlot(Slot.PASSWD);

		propSlot(Slot.FTP_DIR);
		propSlot(Slot.RELATIVE_DIR, "false");

		delegateSlot(Slot.DIR, Slot.FTP_DIR);

		propSlot(Slot.ENCODING_TYPE, "ISO-8859-1");
		propSlot(Slot.HTTP_DIR);

		new ConnectedSlot(stclContext, this, Slot.CONNECTED);

		// COMMAND PART

		command(Command.TEST_CONNEXION, TestConnection.class);
		command(Command.TEST_FTP_CONNECTION, TestConnection.class);
		command(Command.PUT, Put.class);
	}

	// adds listeners to property changes
	@Override
	public void afterCompleted(StclContext stclContext, PStcl self) {
		super.afterCompleted(stclContext, self);

		// puts self as context for folder interface
		self.plug(stclContext, self, Slot.CONTEXT);

		// puts listeners to close connection if value changes
		self.plug(stclContext, self, PathUtils.compose(Slot.HOST, IPPropStencil.Slot.LISTENERS));
		self.plug(stclContext, self, PathUtils.compose(Slot.PORT, IPPropStencil.Slot.LISTENERS));
		self.plug(stclContext, self, PathUtils.compose(Slot.USER, IPPropStencil.Slot.LISTENERS));
		self.plug(stclContext, self, PathUtils.compose(Slot.PASSWD, IPPropStencil.Slot.LISTENERS));
		self.plug(stclContext, self, PathUtils.compose(Slot.DIR, IPPropStencil.Slot.LISTENERS));
	}

	/**
	 * IPropertyChangeListener interface.
	 */
	// if any parameter changes, then closes the connection
	@Override
	public Result propertyChange(PropertyChangeEvent<StclContext, PStcl> evt) {
		StclContext stclContext = evt.getStencilContext();
		return close(stclContext, evt.getPluggedProperty().getContainer(stclContext));
	}

	@Override
	public Filter getFilter() {
		return new DirectoryFilter();
	}

	/**
	 * Tests if the connection is established.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param self
	 *          the stencil as a plugged stencil.
	 * @return the connection status.
	 */
	private boolean isConnected(StclContext stclContext, PStcl self) {
		try {
			return this._client != null && this._client.isConnected();
		} catch (Exception e) {
			close(stclContext, self);
			logError(stclContext, "cannot test FTP connection : %s", e);
			return false;
		}
	}

	public synchronized Result connect(StclContext stclContext, PStcl self) {
		try {

			// resets initial directory if already connected
			if (isConnected(stclContext, self)) {
				try {
					if (!this._client.changeWorkingDirectory(this._pwd)) {
						String msg = logError(stclContext, "cannot reset initial FTP dir %s", this._pwd);
						return Result.error(PREFIX, msg);
					}
					logWarn(stclContext, "FTP connected to %s", this._pwd);
					return Result.success();
				} catch (Exception e) {
					close(stclContext, self);
					String msg = logError(stclContext, "cannot change initial FTP directory : %s", e);
					return Result.error(PREFIX, msg);
				}
			}

			// connects to server
			this._client = newClient(stclContext, self);
			this._pwd = this._client.printWorkingDirectory();

			// sets result
			String msg = logTrace(stclContext, "FTP connected on %s", this._client.getRemoteAddress().getHostName());
			return Result.success(PREFIX, msg);
		} catch (Exception e) {
			logError(stclContext, "error while connecting : %s", e);
			close(stclContext, self);
			return Result.error(PREFIX, Status.CONNECT, e);
		}
	}

	/**
	 * Closes the connection to the FTP server.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param self
	 *          the stencil as a plugged stencil.
	 * @return the closing status.
	 */
	public synchronized Result close(StclContext stclContext, PStcl self) {
		if (this._client != null) {
			closeClient(stclContext, this._client, self);
			this._client = null;
		}
		return Result.success();
	}

	/**
	 * Returns in result the list of files in the <tt>dir</tt> directory.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param path
	 *          folder path.
	 * @param self
	 *          the stencil as a plugged stencil.
	 * @return the list of files from path.
	 */
	public synchronized Result ls(StclContext stclContext, String path, boolean connect, PStcl self) {
		try {

			// path should not be empty
			if (StringUtils.isEmpty(path)) {
				return Result.error(PREFIX, "empty folder path for FTP ls");
			}

			// connects if neeeded
			if (connect) {
				Result connected = connect(stclContext, self);
				if (connected.isNotSuccess()) {
					return connected;
				}
			}

			// gets files info
			FTPFile[] files = this._client.listFiles(path);

			// closes if neeeded
			if (connect) {
				Result closed = close(stclContext, self);
				if (closed.isNotSuccess()) {
					return closed;
				}
			}

			return Result.success(PREFIX, files);
		} catch (Exception e) {
			logError(stclContext, "error in FTP list : %s", e);
			close(stclContext, self);
			return Result.error(PREFIX, e);
		}
	}

	/**
	 * Tests if a file or folder exists.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param path
	 *          the file or folder path.
	 * @param self
	 *          the stencil as a plugged stencil.
	 * @return
	 */
	public synchronized boolean exists(StclContext stclContext, String path, boolean connect, PStcl self) {
		try {

			// path must not be empty
			if (StringUtils.isBlank(path)) {
				return false;
			}

			// connects if needed
			if (connect) {
				Result connected = connect(stclContext, self);
				if (connected.isNotSuccess()) {
					return false;
				}
			}

			// checks file exist
			FTPFile[] files = this._client.listFiles(path);
			boolean exists = (files != null && files.length > 0);

			// closes if needed
			if (connect) {
				Result closed = close(stclContext, self);
				if (closed.isNotSuccess()) {
					return false;
				}
			}

			return exists;
		} catch (Exception e) {
			logError(stclContext, "cannot test FTP exists : %s", e);
			return false;
		}
	}

	/**
	 * Returns in result the size of the file (name may be composed). The name
	 * cannot be a regular expression.
	 */
	public synchronized Result size(StclContext stclContext, String name, boolean connect, PStcl self) {

		// gets file
		Result result = ls(stclContext, name, connect, self);
		if (result.isNotSuccess()) {
			return result;
		}

		// gets files info
		FTPFile[] files = result.getInfo(Result.SUCCESS, PREFIX, 0);
		int size = 0;
		if (files != null && files.length == 1 && files[0] != null) {
			size = (int) files[0].getSize();
		}

		// returns size
		return Result.success(PREFIX, size);
	}

	/**
	 * Creates a new directory.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param path
	 *          the folder path.
	 * @param allowCreation
	 *          if <tt>true</tt> then can create intermediate folders.
	 * @param self
	 *          the stencil as a plugged stencil.
	 * @return
	 */
	public synchronized Result mkdir(StclContext stclContext, String path, boolean connect, PStcl self) {
		try {

			// connects if needed
			if (connect) {
				Result connected = connect(stclContext, self);
				if (connected.isNotSuccess()) {
					return connected;
				}
			}

			// verifies it doesn't already exist
			if (exists(stclContext, path, false, self)) {
				if (connect)
					close(stclContext, self);
				String msg = logWarn(stclContext, "FTP file or folder %s already exists", path);
				return Result.error(PREFIX, Status.EXISTS, msg);
			}

			// creates directory
			boolean created = this._client.makeDirectory(path);

			// closes if needed
			if (connect) {
				Result closed = close(stclContext, self);
				if (closed.isNotSuccess()) {
					return closed;
				}
			}

			// succeed
			if (created) {
				String msg = String.format("dir %s created", path);
				return Result.success(PREFIX, msg);
			}
			String msg = String.format("dir %s not created", path);
			return Result.error(PREFIX, msg);
		} catch (Exception e) {
			logError(stclContext, "cannot create FTP directory : %s", e);
			close(stclContext, self);
			return Result.error(PREFIX, e);
		}
	}

	/**
	 * Puts content file performing a backup before.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param in
	 *          the input stream.
	 * @param path
	 *          the file path.
	 * @param allowCreation
	 *          creation of intermediate folder.
	 * @param backupSuffix
	 *          the backup suffix for the backup file (if defined to blank then no
	 *          backup).
	 * @param self
	 *          the stencil as a plugged stencil.
	 * @return
	 */
	public synchronized Result put(StclContext stclContext, InputStream in, String path, boolean allowCreation, String backupSuffix, boolean connect, PStcl self) {
		try {

			// connects
			if (connect) {
				Result connected = connect(stclContext, self);
				if (connected.isNotSuccess())
					return connected;
			}

			// changes directory
			String folderPath = PathUtils.getPathName(path);
			String fileName = PathUtils.getLastName(path);
			if (StringUtils.isNotBlank(folderPath)) {
				Result result = cd(stclContext, folderPath, allowCreation, self);
				if (result.isNotSuccess()) {
					if (connect)
						close(stclContext, self);
					return result;
				}
			}

			// saves old version if exists
			if (StringUtils.isNotBlank(backupSuffix) && exists(stclContext, fileName, false, self)) {
				String fileBack = fileName.concat(backupSuffix);
				boolean done = this._client.rename(fileName, fileBack);
				if (!done) {
					if (connect)
						close(stclContext, self);
					String msg = logWarn(stclContext, "cannot create FTP backup file %s", fileBack);
					return Result.error(PREFIX, msg);
				}
			}

			// copies file
			OutputStream out = this._client.storeFileStream(fileName);
			if (!FTPReply.isPositivePreliminary(this._client.getReplyCode())) {
				int code = this._client.getReplyCode();
				in.close();
				if (out != null) {
					out.close();
				}
				if (connect)
					close(stclContext, self);
				String msg = logWarn(stclContext, "cannot get store FTP file stream %s (code:%s)", path, code);
				return Result.error(PREFIX, msg);
			}

			IOUtils.copy(in, out);
			in.close();
			out.close();

			// must call completePendingCommand() to finish command.
			if (!this._client.completePendingCommand()) {
				String msg = logWarn(stclContext, "Cannot complete pending FTP command");
				if (connect)
					close(stclContext, self);
				return Result.error(PREFIX, msg);
			}

			// closes
			if (connect) {
				Result closed = close(stclContext, self);
				if (closed.isNotSuccess()) {
					return closed;
				}
			}

			// succeed
			return Result.success();
		} catch (Exception e) {
			logError(stclContext, "cannot put FTP file in %s : %s", path, e);
			close(stclContext, self);
			return Result.error(PREFIX, e);
		}
	}

	/**
	 * Gets file content. This method doesn NOT close the output stream.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param out
	 *          the output stream.
	 * @param name
	 *          the file path
	 * @param self
	 *          the stencil as a pluged stencil.
	 * @return
	 */
	public synchronized Result get(StclContext stclContext, OutputStream out, String path, boolean connect, PStcl self) {
		try {

			// connects
			if (connect) {
				Result connected = connect(stclContext, self);
				if (connected.isNotSuccess()) {
					return connected;
				}
			}

			// gets content
			boolean done = this._client.retrieveFile(path, out);

			// closes
			if (connect) {
				Result closed = close(stclContext, self);
				if (!closed.isSuccess()) {
					return closed;
				}
			}

			if (!done) {
				String msg = String.format("cannot retrieve FTP file %s", path);
				return Result.error(PREFIX, msg);
			}
			return Result.success();
		} catch (Exception e) {
			logError(stclContext, "cannot get file : %s", e);
			close(stclContext, self);
			return Result.error(PREFIX, e);
		}
	}

	public synchronized InputStream getFileInputStream(StclContext stclContext, String path, boolean connect, PStcl self) {
		try {

			// connects
			if (connect) {
				Result connected = connect(stclContext, self);
				if (connected.isNotSuccess()) {
					return null;
				}
			}

			// gets content
			InputStream is = this._client.retrieveFileStream(path);
			if (!FTPReply.isPositivePreliminary(this._client.getReplyCode())) {
				is.close();
				if (connect)
					close(stclContext, self);
				return null;
			}

			// must call completePendingCommand() to finish command.
			if (!this._client.completePendingCommand()) {
				logWarn(stclContext, "cannot complete pending FTP command");
				if (connect)
					close(stclContext, self);
				return null;
			}

			// succeed
			if (connect) {
				Result closed = close(stclContext, self);
				if (closed.isNotSuccess()) {
					return null;
				}
			}

			return is;
		} catch (IOException e) {
			logError(stclContext, "Exception in retrieving FTP file : %s", e);
		}
		return null;
	}

	/**
	 * Deletes a FTP file.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param path
	 *          the path of the file to be deleted.
	 * @param self
	 *          the stencil as a plugged stencil.
	 * @return
	 */
	public synchronized Result delete(StclContext stclContext, String path, boolean connect, PStcl self) {
		try {

			// connects
			if (connect) {
				Result connected = connect(stclContext, self);
				if (connected.isNotSuccess()) {
					return connected;
				}
			}

			// deletes file
			this._client.deleteFile(path);

			// closes
			if (connect) {
				Result closed = close(stclContext, self);
				if (closed.isNotSuccess()) {
					return closed;
				}
			}

			return Result.success();
		} catch (Exception e) {
			String msg = logError(stclContext, "cannot FTP delete : %s", e);
			close(stclContext, self);
			return Result.error(PREFIX, msg);
		}
	}

	/**
	 * Renames a file.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param fromPath
	 *          from path (may be composed).
	 * @param toName
	 *          to file name (may not be composed).
	 * @param self
	 *          the stencil as a plugged stencil.
	 * @return
	 */
	public synchronized Result rename(StclContext stclContext, String fromPath, String toName, boolean connect, PStcl self) {
		try {

			// connects
			if (connect) {
				Result connected = connect(stclContext, self);
				if (connected.isNotSuccess()) {
					return connected;
				}
			}

			// checks if from file exist
			if (!exists(stclContext, fromPath, false, self)) {
				if (connect)
					close(stclContext, self);
				String msg = logWarn(stclContext, "Cannot FTP rename as not exist");
				return Result.error(msg);
			}

			// changes directory
			String folder = PathUtils.getPathName(fromPath);
			String old = PathUtils.getLastName(fromPath);
			if (StringUtils.isNotBlank(folder)) {
				Result result = cd(stclContext, folder, false, self);
				if (result.isNotSuccess()) {
					if (connect)
						close(stclContext, self);
					return result;
				}
			}

			// renames
			this._client.rename(old, toName);

			// closes
			if (connect) {
				Result closed = close(stclContext, self);
				if (!closed.isSuccess()) {
					return closed;
				}
			}

			return Result.success();
		} catch (Exception e) {
			String msg = logError(stclContext, "cannot FTP remane : %s", e);
			close(stclContext, self);
			return Result.error(PREFIX, msg);
		}
	}

	/**
	 * Changes the current directory to go to the last folder of the path.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param name
	 *          the path.
	 * @param allowFolderCreation
	 *          creates the folder if doesn't exist when <tt>true</tt>.
	 * @param self
	 *          the stencil as a plugged stencil.
	 * @return the last file name
	 */
	public Result cd(StclContext stclContext, String path, boolean allowFolderCreation, PStcl self) throws IOException {

		// if creation not allowed, can go directly
		if (!allowFolderCreation) {
			boolean done = this._client.changeWorkingDirectory(path);
			if (done) {
				return Result.success();
			}
			String msg = String.format("cannot FTP cd to %s", path);
			return Result.error(PREFIX, msg);
		}

		// if absolute name, resets from root
		if (path.startsWith(PathUtils.ROOT)) {
			boolean done = this._client.changeWorkingDirectory(PathUtils.ROOT);
			if (!done) {
				return Result.error(PREFIX, "cannot FTP cd to root");
			}
		}

		// changes folder
		for (String d : StringUtils.split(path, PathUtils.SEP)) {
			if (!this._client.changeWorkingDirectory(d)) {
				boolean done = this._client.makeDirectory(d);
				if (!done) {
					String msg = String.format("cannot create FTPdirectory %s", d);
					return Result.error(PREFIX, msg);
				}
				done = this._client.changeWorkingDirectory(d);
				if (!done) {
					String msg = String.format("cannot FTP cd to %s", d);
					return Result.error(PREFIX, msg);
				}
			}
		}

		// returns status;
		return Result.success();
	}

	private class ConnectedSlot extends CalculatedBooleanPropertySlot<StclContext, PStcl> {
		public ConnectedSlot(StclContext stclContext, Stcl in, String name) {
			super(stclContext, in, name);
		}

		@Override
		public boolean getBooleanValue(StclContext stclContext, PStcl self) {
			return isConnected(stclContext, self);
		}
	}

	public class DirectoryFilter extends Filter {
		@Override
		public boolean checkType(FTPFile file) {
			return file.isDirectory() && !file.getName().equals("..");
		}
	}
}