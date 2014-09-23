/**
 * Copyright GDO - 2004
 */
package com.gdo.file.model;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import com.gdo.file.cmd.CopyDir;
import com.gdo.file.cmd.CreateDir;
import com.gdo.file.cmd.CreateFile;
import com.gdo.file.cmd.DeleteFolder;
import com.gdo.file.cmd.RenameFolder;
import com.gdo.helper.StringHelper;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.event.IPropertyChangeListener;
import com.gdo.stencils.event.PropertyChangeEvent;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.PathUtils;
import com.gdo.util.XmlWriter;

public class FileContextStcl extends FolderStcl implements IPropertyChangeListener<StclContext, PStcl> {

	public interface Slot extends FolderStcl.Slot {
		String DIR = "Dir";
		String ENCODING_TYPE = "EncodingType";
	}

	public interface Command extends FolderStcl.Command {
		String CREATE_DIR = "CreateDir";
		String COPY = "CopyDir";
	}

	private File _dir; // the file context home directory

	public FileContextStcl(StclContext stclContext) {
		super(stclContext);

		propSlot(Slot.DIR);
		propSlot(Slot.ENCODING_TYPE, "ISO-8859-1");

		command(Command.CREATE_FILE, CreateFile.class);
		command(Command.CREATE_FOLDER, CreateDir.class);
		command(Command.CREATE_DIR, CreateDir.class);
		command(Command.COPY, CopyDir.class);
		command(Command.RENAME, RenameFolder.class);
		command(Command.DELETE, DeleteFolder.class);
	}

	// add listeners to property changes
	@Override
	public void afterCompleted(StclContext stclContext, PStcl self) {
		super.afterCompleted(stclContext, self);
		self.plug(stclContext, self, PathUtils.compose(Slot.DIR, Slot.LISTENERS));
	}

	/**
	 * IPropertyChangeListener interface.
	 */
	// if dir property changes, then remove previous file created
	@Override
	public Result propertyChange(PropertyChangeEvent<StclContext, PStcl> evt) {
		this._dir = null;
		return Result.success();
	}

	@Override
	public PStcl clone(StclContext stclContext, PSlot<StclContext, PStcl> slot, IKey key, PStcl self) {
		PStcl clone = super.clone(stclContext, slot, key, self);
		clone.setString(stclContext, Slot.DIR, self.getNotExpandedString(stclContext, Slot.DIR, StringHelper.EMPTY_STRING));
		clone.setString(stclContext, Slot.ENCODING_TYPE, self.getNotExpandedString(stclContext, Slot.ENCODING_TYPE, StringHelper.EMPTY_STRING));
		return clone;
	}

	/**
	 * @return the java file root.
	 */
	@Override
	public File getFile(StclContext stclContext, PStcl self) {

		// if file root not defined
		if (this._dir == null) {
			String dir = self.getString(stclContext, Slot.DIR, StringHelper.EMPTY_STRING);
			if (StringUtils.isBlank(dir)) {
				logWarn(stclContext, "initial context dir is empty", dir);
				return null;
			}
			this._dir = new File(dir);
			if (this._dir == null) {
				logWarn(stclContext, "cannot open dir %s", dir);
				return null;
			}
		}
		
		// file root must be a directory
		if (!this._dir.isDirectory()) {
			logWarn(stclContext, "initial context dir (%s) is not a directory", this._dir.getPath());
			this._dir = null;
		}

		// returns java file root
		return this._dir;
	}

	@Override
	protected void saveConstructorParameters(StclContext stclContext, XmlWriter writer, PStcl self) {
		// no parameter
	}
}