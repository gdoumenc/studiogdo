/**
 * Copyright GDO - 2004
 */
package com.gdo.resource.model;

import java.net.URI;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.eval.NotImplementedException;

import com.gdo.context.model.FileStcl;
import com.gdo.context.model.FolderStcl;
import com.gdo.project.util.model.NamedStcl;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils._Stencil;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.CalculatedIntegerPropertySlot;
import com.gdo.stencils.slot.CalculatedStringPropertySlot;
import com.gdo.stencils.slot.MultiCalculatedSlot;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;

public abstract class _ResourceStcl extends Stcl {

	public interface Slot extends Stcl.Slot {
		String CONTEXT = "Context";
		String PATH = "Path";
		String FILE = "File";

		String SIZE = "Size";
		String LAST_MODIFIED = "LastModified";

		String CONTAINER_MANAGER = "ContainerManager";
		String CONTAINER_FOLDER = "ContainerFolder";

		String URL = "Url";
	}

	public interface Command extends NamedStcl.Command {
		String DELETE = "Delete";
		String RENAME = "Rename";
	}

	public _ResourceStcl(StclContext stclContext) {
		super(stclContext);

		new PathSlot(stclContext);
		new SizeSlot(stclContext);
		new LastModifiedSlot(stclContext);
		new UrlSlot(stclContext);
	}

	/**
	 * The name is defined localy on the stencil, or from the file name if no name
	 * defined.
	 */
	@Override
	public String getName(StclContext stclContext, PStcl self) {

		// gets internal name (no default template name)
		_Stencil<StclContext, PStcl> stcl = self.getReleasedStencil(stclContext);
		String name = stcl.getJavaName(stclContext, self);
		if (StringUtils.isNotEmpty(name))
			return name;

		// if empty gets associated file name
		PStcl file = getStencil(stclContext, Slot.FILE, self);
		if (StencilUtils.isNotNull(file))
			return file.getName(stclContext);

		// no name then
		return "";
	}

	// gets associated file path (can change path -> changes file)
	private class PathSlot extends CalculatedStringPropertySlot<StclContext, PStcl> {
		public PathSlot(StclContext stclContext) {
			super(stclContext, _ResourceStcl.this, Slot.PATH);
		}

		@Override
		public String getValue(StclContext stclContext, PStcl self) {
			PStcl container = self.getContainer(stclContext);

			// gets folder path prefix
			PStcl folder = container.getStencil(stclContext, Slot.CONTAINER_FOLDER);
			if (StencilUtils.isNull(folder))
				return "";
			String folderPath = folder.getString(stclContext, Slot.PATH, "");

			// gets file name
			PStcl file = container.getStencil(stclContext, Slot.FILE);
			if (StencilUtils.isNull(file))
				return "";
			String filePath = file.getString(stclContext, Slot.PATH, "");

			return PathUtils.compose(folderPath, filePath);
		}

		@Override
		public String setValue(StclContext stclContext, String value, PStcl self) {
			PStcl container = self.getContainer(stclContext);

			// gets context
			PStcl file = container.getStencil(stclContext, Slot.FILE);
			PStcl context = file.getStencil(stclContext, FileStcl.Slot.CONTEXT);

			// removes old file
			container.clearSlot(stclContext, Slot.FILE);

			// adds new file (TODO : WRONG should use folder container not
			// context)
			if (!StencilUtils.isNull(context) && StringUtils.isNotEmpty(value)) {
				file = context.getStencil(stclContext, PathUtils.createPath(FolderStcl.Slot.GET, value));
				if (!StencilUtils.isNull(file)) {
					container.plug(stclContext, file, Slot.FILE);
				}
			}
			return value;
		}
	}

	// gets associated file size
	private class SizeSlot extends CalculatedIntegerPropertySlot<StclContext, PStcl> {
		public SizeSlot(StclContext stclContext) {
			super(stclContext, _ResourceStcl.this, Slot.SIZE);
		}

		@Override
		public int getIntegerValue(StclContext stclContext, PStcl self) {
			PStcl file = self.getContainer(stclContext).getStencil(stclContext, Slot.FILE);
			if (StencilUtils.isNull(file))
				return -1;
			return file.getInt(stclContext, FileStcl.Slot.SIZE, -1);
		}

		@Override
		public int setIntegerValue(StclContext stclContext, int value, PStcl self) {
			throw new NotImplementedException("Cannot set resource size");
		}
	}

	// gets associated file last modified value (can use key as date format)
	private class LastModifiedSlot extends MultiCalculatedSlot<StclContext, PStcl> {
		public LastModifiedSlot(StclContext stclContext) {
			super(stclContext, _ResourceStcl.this, FileStcl.Slot.LAST_MODIFIED, PSlot.ANY);
		}

		@Override
		protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
			PStcl file = self.getContainer().getStencil(stclContext, Slot.FILE);
			if (StencilUtils.isNull(file))
				return StencilUtils.< StclContext, PStcl> iterator();
			return file.getStencils(stclContext, FileStcl.Slot.LAST_MODIFIED, cond);
		}
	}

	// concats associated folder url and file name
	private class UrlSlot extends CalculatedStringPropertySlot<StclContext, PStcl> {
		public UrlSlot(StclContext stclContext) {
			super(stclContext, _ResourceStcl.this, Slot.URL);
		}

		@Override
		public String getValue(StclContext stclContext, PStcl self) {
			try {
				PStcl res = self.getContainer(stclContext);

				// a file must be defined
				PStcl file = res.getStencil(stclContext, Slot.FILE);
				if (StencilUtils.isNull(file))
					return "";

				// if a container folder is defined, uses its url
				PStcl folder = res.getStencil(stclContext, Slot.CONTAINER_FOLDER);
				if (StencilUtils.isNotNull(folder)) {

					// adds file name to folder url
					String folderUrl = folder.getString(stclContext, Slot.URL, "");
					String name = file.getString(stclContext, FileStcl.Slot.NAME, "");
					URI uri = new URI(null, name, null); // use uri for encoding
					// specific char
					return PathUtils.compose(folderUrl, uri.toString());
				} else {
					String path = file.getString(stclContext, Slot.PATH, "");
					PStcl context = file.getStencil(stclContext, FileStcl.Slot.CONTEXT);
					String http = context.getString(stclContext, FolderStcl.Slot.HTTP_DIR, "");
					return PathUtils.compose(http, path);
				}
			} catch (Exception e) {
				logError(stclContext, e.toString());
				return "";
			}
		}

		@Override
		public String setValue(StclContext stclContext, String value, PStcl self) {
			throw new NotImplementedException("Cannot set resource url (set name and folder url)");
		}
	}

}