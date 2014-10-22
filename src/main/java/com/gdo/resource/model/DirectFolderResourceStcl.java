/**
 * Copyright GDO - 2004
 */
package com.gdo.resource.model;

import org.apache.commons.lang3.StringUtils;

import com.gdo.context.model.FolderStcl;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cond.PathCondition;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.factory.StencilFactory;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.MultiCalculatedSlot;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;

public class DirectFolderResourceStcl extends FolderResourceStcl {

	public interface Slot extends FolderResourceStcl.Slot {
		String FOLDER_ENCAPSULATED = "FolderEncapsulated";

		String FOLDER_TEMPLATE = "FolderTemplate";
		String FILE_TEMPLATE = "FileTemplate";
	}

	private PStcl _stencil; // used by creator

	public DirectFolderResourceStcl(StclContext stclContext) {
		super(stclContext);

		new FileResourcesSlot(stclContext);
		new FolderResourcesSlot(stclContext);
	}

	public DirectFolderResourceStcl(StclContext stclContext, PStcl stencil) {
		this(stclContext);
		_stencil = stencil;
	}

	@Override
	public void afterCompleted(StclContext stclContext, PStcl self) {
		if (StencilUtils.isNotNull(_stencil)) {
			self.plug(stclContext, _stencil, Slot.FOLDER_ENCAPSULATED);
		}
	}

	/**
	 * The name cannot be defined locally on stencil as this stencil is created
	 * from the file.
	 */
	@Override
	public String getName(StclContext stclContext, PStcl self) {
		PStcl file = getStencil(stclContext, Slot.FILE, self);
		if (StencilUtils.isNotNull(file))
			return file.getName(stclContext);
		return super.getName(stclContext, self);
	}

	private class FileResourcesSlot extends MultiCalculatedSlot<StclContext, PStcl> {
		public FileResourcesSlot(StclContext stclContext) {
			super(stclContext, DirectFolderResourceStcl.this, Slot.FILE_RESOURCES, PSlot.ANY);
		}

		@Override
		public StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
			PStcl container = self.getContainer();

			// gets the template used to create the file resource
			String template = container.getString(stclContext, Slot.FILE_TEMPLATE, "");
			if (StringUtils.isEmpty(template))
				return StencilUtils.< StclContext, PStcl> iterator(Result.error("File template property undefined"));

			// creates the file resources
			PStcl folder = container.getStencil(stclContext, Slot.FOLDER_ENCAPSULATED);
			StencilFactory<StclContext, PStcl> factory = (StencilFactory<StclContext, PStcl>) stclContext.getStencilFactory();
			for (PStcl stcl : folder.getStencils(stclContext, FolderStcl.Slot.FILES_ONLY, cond)) {
				if (getStencilFromList(stclContext, stcl.getKey(), self) != null) {
					keepStencilInList(stclContext, stcl.getKey(), self);
					continue;
				}
				PStcl s = factory.createPStencil(stclContext, self, stcl.getKey(), template, stcl);
				s.plug(stclContext, container, Slot.CONTAINER_FOLDER);
				PStcl manager = container.getStencil(stclContext, Slot.CONTAINER_MANAGER);
				s.plug(stclContext, manager, Slot.CONTAINER_MANAGER);
				addStencilInList(stclContext, s, self);
			}
			return cleanList(stclContext, cond, self);
		}
	}

	private class FolderResourcesSlot extends MultiCalculatedSlot<StclContext, PStcl> {
		public FolderResourcesSlot(StclContext stclContext) {
			super(stclContext, DirectFolderResourceStcl.this, Slot.FOLDER_RESOURCES, PSlot.ANY);
		}

		@Override
		public StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
			PStcl container = self.getContainer();

			// if path is root then returns containing manager
			if (cond != null && cond instanceof PathCondition) {
				PathCondition<StclContext, PStcl> c = (PathCondition<StclContext, PStcl>) cond;
				String path = PathUtils.getKeyContained(c.getCondition());
				if (PathUtils.ROOT.equals(path)) {
					return container.getStencils(stclContext, Slot.CONTAINER_MANAGER);
				}
			}

			// gets the template used to create the folder resource
			String template = container.getString(stclContext, Slot.FOLDER_TEMPLATE, "");
			if (StringUtils.isEmpty(template))
				return StencilUtils.< StclContext, PStcl> iterator(Result.error("Folder template property undefined"));

			// creates the folder resources
			PStcl folder = container.getStencil(stclContext, Slot.FOLDER_ENCAPSULATED);
			StencilFactory<StclContext, PStcl> factory = (StencilFactory<StclContext, PStcl>) stclContext.getStencilFactory();
			for (PStcl stcl : folder.getStencils(stclContext, FolderStcl.Slot.FOLDERS_ONLY, cond)) {
				if (getStencilFromList(stclContext, stcl.getKey(), self) != null) {
					keepStencilInList(stclContext, stcl.getKey(), self);
					continue;
				}
				PStcl s = factory.createPStencil(stclContext, self, stcl.getKey(), template, stcl);
				s.plug(stclContext, container, Slot.CONTAINER_FOLDER);
				PStcl manager = container.getStencil(stclContext, Slot.CONTAINER_MANAGER);
				s.plug(stclContext, manager, Slot.CONTAINER_MANAGER);
				addStencilInList(stclContext, s, self);
			}
			return cleanList(stclContext, cond, self);
		}
	}

}