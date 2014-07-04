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

public class DirectResourcesMgrStcl extends ResourcesMgrStcl {

	public interface Slot extends ResourcesMgrStcl.Slot {
		String FOLDER_TEMPLATE = "FolderTemplate";
		String FILE_TEMPLATE = "FileTemplate";
	}

	public DirectResourcesMgrStcl(StclContext stclContext) {
		super(stclContext);

		new FileResourcesSlot(stclContext);
		new FolderResourcesSlot(stclContext);
	}

	private class FileResourcesSlot extends MultiCalculatedSlot<StclContext, PStcl> {
		public FileResourcesSlot(StclContext stclContext) {
			super(stclContext, DirectResourcesMgrStcl.this, Slot.FILE_RESOURCES, PSlot.ANY);
		}

		@Override
		public StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
			PStcl manager = self.getContainer();

			// a context needs to be defined
			if (!hasContext(stclContext, manager)) {
				String msg = String.format("no context defined for the resource manager %s", manager);
				return StencilUtils.iterator(Result.error(msg));
			}

			// get the template used to create the file resource
			String template = self.getContainer().getString(stclContext, Slot.FILE_TEMPLATE, "");
			if (StringUtils.isEmpty(template))
				return StencilUtils.iterator(Result.error("File template property undefined"));

			// creates the file resources
			String path = PathUtils.compose(Slot.CONTEXTS, FolderStcl.Slot.FILES_ONLY);
			StencilFactory<StclContext, PStcl> factory = (StencilFactory<StclContext, PStcl>) stclContext.getStencilFactory();
			for (PStcl stcl : self.getContainer().getStencils(stclContext, path, cond)) {
				if (getStencilFromList(stclContext, stcl.getKey(), self) != null) {
					keepStencilInList(stclContext, stcl.getKey(), self);
					continue;
				}
				PStcl s = factory.createPStencil(stclContext, self, stcl.getKey(), template, stcl);
				s.plug(stclContext, self.getContainer(), Slot.CONTAINER_MANAGER);
				s.plug(stclContext, self.getContainer(), Slot.CONTAINER_FOLDER);
				addStencilInList(stclContext, s, self);
			}
			return cleanList(stclContext, cond, self);
		}
	}

	private class FolderResourcesSlot extends MultiCalculatedSlot<StclContext, PStcl> {
		public FolderResourcesSlot(StclContext stclContext) {
			super(stclContext, DirectResourcesMgrStcl.this, Slot.FOLDER_RESOURCES, PSlot.ANY);
		}

		@Override
		public StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
			PStcl manager = self.getContainer();

			// a context needs to be defined
			if (!hasContext(stclContext, manager)) {
				String msg = String.format("no context defined for the resource manager %s", manager);
				return StencilUtils.iterator(Result.error(msg));
			}

			// if path is root then returns this manager
			if (cond != null && cond instanceof PathCondition) {
				PathCondition<StclContext, PStcl> c = (PathCondition<StclContext, PStcl>) cond;
				String path = PathUtils.getKeyContained(c.getCondition());
				if (PathUtils.ROOT.equals(path)) {
					return StencilUtils.iterator(stclContext, manager, self);
				}
			}

			// get the template used to create the folder resource
			String template = self.getContainer().getString(stclContext, Slot.FOLDER_TEMPLATE, "");
			if (StringUtils.isEmpty(template))
				return StencilUtils.iterator(Result.error("Folder template property undefined"));

			// creates the folder resources
			String path = PathUtils.compose(Slot.CONTEXTS, FolderStcl.Slot.FOLDERS_ONLY);
			StencilFactory<StclContext, PStcl> factory = (StencilFactory<StclContext, PStcl>) stclContext.getStencilFactory();
			for (PStcl stcl : self.getContainer().getStencils(stclContext, path, cond)) {
				if (getStencilFromList(stclContext, stcl.getKey(), self) != null) {
					keepStencilInList(stclContext, stcl.getKey(), self);
					continue;
				}
				PStcl s = factory.createPStencil(stclContext, self, stcl.getKey(), template, stcl);
				s.plug(stclContext, self.getContainer(), Slot.CONTAINER_MANAGER);
				s.plug(stclContext, self.getContainer(), Slot.CONTAINER_FOLDER);
				addStencilInList(stclContext, s, self);
			}
			return cleanList(stclContext, cond, self);
		}
	}

	private boolean hasContext(StclContext stclContext, PStcl self) {
		StencilIterator<StclContext, PStcl> contexts = self.getStencils(stclContext, Slot.CONTEXTS);
		return (contexts.isValid() && contexts.size() > 0);
	}
}