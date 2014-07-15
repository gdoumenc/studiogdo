/**
 * Copyright GDO - 2004
 */
package com.gdo.resource.model;

import org.apache.commons.lang3.StringUtils;

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

public class FolderResourceStcl extends _ResourceStcl {

	public interface Slot extends _ResourceStcl.Slot {
		String RESOURCES = "Resources";
		String FILE_RESOURCES = "FileResources";
		String FOLDER_RESOURCES = "FolderResources";

		String GET = "Get";
	}

	public FolderResourceStcl(StclContext stclContext) {
		super(stclContext);

		new GetSlot(stclContext);
	}

	private class GetSlot extends MultiCalculatedSlot<StclContext, PStcl> {
		public GetSlot(StclContext stclContext) {
			super(stclContext, FolderResourceStcl.this, Slot.GET, PSlot.ANY);
		}

		@Override
		protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
			PStcl folder = self.getContainer();

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

			// key should be the path of the resource
			if (PathUtils.isComposed(path)) {
				if (PathUtils.ROOT.equals(path)) {
					return folder.getStencils(stclContext, Slot.CONTAINER_MANAGER);
				}
				String first = PathUtils.getFirstName(path);
				String tail = PathUtils.getTailName(path);
				PStcl f = folder.getStencil(stclContext, PathUtils.createPath(Slot.FOLDER_RESOURCES, first));
				if (StencilUtils.isNull(f))
					return StencilUtils.< StclContext, PStcl> iterator(Result.error(f.getNullReason()));
				return f.getStencils(stclContext, PathUtils.createPath(Slot.GET, tail));
			}
			return folder.getStencils(stclContext, PathUtils.createPath(Slot.FILE_RESOURCES, path));
		}
	}
}