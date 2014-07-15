/**
 * Copyright GDO - 2004
 */
package com.gdo.resource.model;

import org.apache.commons.lang3.StringUtils;

import com.gdo.project.util.model.NamedStcl;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cond.PathCondition;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.MultiCalculatedSlot;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;

public class ResourcesContainerStcl extends NamedStcl {

	public interface Slot extends NamedStcl.Slot {
		String CONTEXTS = "Contexts";
		String CHILDREN = "Children";

		String FILES = "Files";
		String FILE_ONLY = "FilesOnly";
		String FOLDERS_ONLY = "FoldersOnly";

		String GET = "Get";

		String CONTAINER_MANAGER = "ContainerManager";
		String CONTAINER_FOLDER = "ContainerFolder";

		String URL = "Url";

		String SERVICES = "Services";
	}

	public interface Command extends NamedStcl.Command {
		String DELETE = "Delete";
		String RENAME = "Rename";
		String ADD_RESOURCE = "AddResource";
		String ADD_RESOURCE_IN_ONE_STEP = "AddResourceInOneStep";
		String CREATE_CONTEXT = "CreateContext";
	}

	public ResourcesContainerStcl(StclContext stclContext) {
		super(stclContext);

		new GetSlot(stclContext);
	}

	private class GetSlot extends MultiCalculatedSlot<StclContext, PStcl> {
		public GetSlot(StclContext stclContext) {
			super(stclContext, ResourcesContainerStcl.this, Slot.GET, PSlot.ANY);
		}

		@Override
		protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {

			// key should be the path of the resource (in get slot, path must be
			// defined)
			if (cond == null || !(cond instanceof PathCondition)) {
				return StencilUtils.< StclContext, PStcl> iterator(Result.error("Can get resource only if a path is defined"));
			}
			PathCondition<StclContext, PStcl> c = (PathCondition<StclContext, PStcl>) cond;
			String path = PathUtils.getKeyContained(c.getCondition());
			if (StringUtils.isEmpty(path)) {
				return StencilUtils.< StclContext, PStcl> iterator(Result.error("Path condition cannot be empty to get a resource"));
			}

			// gets the resource
			PStcl manager = self.getContainer();
			if (PathUtils.isComposed(path)) {
				if (PathUtils.ROOT.equals(path)) {
					return StencilUtils.< StclContext, PStcl> iterator(stclContext, manager, self);
				}
				String first = PathUtils.getFirstName(path);
				String tail = PathUtils.getTailName(path);
				PStcl f = manager.getStencil(stclContext, PathUtils.createPath(Slot.FOLDERS_ONLY, first));
				if (StencilUtils.isNull(f)) {
					String msg = String.format("cannot found %s folder", first);
					return StencilUtils.< StclContext, PStcl> iterator(Result.error(msg));
				}
				return f.getStencils(stclContext, PathUtils.createPath(Slot.GET, tail));
			}
			return manager.getStencils(stclContext, PathUtils.createPath(Slot.FILE_ONLY, path));
		}
	}
}