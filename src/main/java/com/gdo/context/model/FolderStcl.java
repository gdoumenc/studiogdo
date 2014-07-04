/**
 */
package com.gdo.context.model;

import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;

public abstract class FolderStcl extends Stcl {

	protected static final String FOLDER_PATH = "FolderPath";

	public interface Slot extends Stcl.Slot, _FileStcl.Slot {

		String FOLDER_TEMPLATE = "FolderTemplate";
		String FILE_TEMPLATE = "FileTemplate";

		/**
		 * Files and folders defined in the directory.
		 */
		String FILES = "Files";

		/**
		 * Files only defined in the directory.
		 */
		String FILES_ONLY = "FilesOnly";

		/**
		 * Folders only defined in the directory.
		 */
		String FOLDERS_ONLY = "FoldersOnly";

		/**
		 * File or foler defined at a given path as key (a key must be provided and
		 * may be composed).
		 */
		String GET = "Get";

		String ALLOWS_FOLDER_CREATION = "AllowsFolderCreation";
		String ALLOWS_FILE_CREATION = "AllowsFileCreation";

		String HTTP_DIR = "HttpDir"; // if defined, the web access of the ressource
	}

	public interface Command extends Stcl.Command, _FileStcl.Command {
		String CREATE_FILE = "CreateFile";
		String CREATE_FOLDER = "CreateFolder";
	}

	public interface CreationMode {
		int CREATE = 0; // create or replace
		int APPEND = 1; // not done
		int CREATE_ONLY = 2; // returns error if file already exists
		int ONLY_IF_DOESNT_EXIST = 3; // only if file doesn't exist
	}

	public FolderStcl(StclContext stclContext) {
		super(stclContext);

		singleSlot(Slot.CONTEXT);
		propSlot(Slot.PATH);
		propSlot(Slot.ABSOLUTE_PATH);

		propSlot(Slot.FOLDER_TEMPLATE);
		propSlot(Slot.FILE_TEMPLATE);

		propSlot(Slot.ALLOWS_FOLDER_CREATION, false);
		propSlot(Slot.ALLOWS_FILE_CREATION, false);

		multiSlot(Slot.FILES);
		multiSlot(Slot.FILES_ONLY);
		multiSlot(Slot.FOLDERS_ONLY);
	}
}
