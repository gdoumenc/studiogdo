package com.gdo.stencils.descriptor;

import com.gdo.project.adaptor.LinkStcl;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.factory.StencilFactory;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.slot.MultiSlot;
import com.gdo.stencils.slot._Slot;
import com.gdo.stencils.util.PathUtils;

public class DelegateSlotDescriptor<C extends _StencilContext, S extends _PStencil<C, S>> extends _SlotDescriptor<C, S> {
	String _path = null;

	public DelegateSlotDescriptor(String path) {
		super();
		_path = path;
	}

	@SuppressWarnings("unchecked")
	@Override
	public _Slot<C, S> add(C stclContext, String name, S self) {

		// get the link path and add .. as path is relative to link not to
		// stencil
		String path = PathUtils.compose(PathUtils.PARENT, _path);

		// adds link stencil
		MultiSlot<C, S> slot = new MultiSlot<C, S>(stclContext, self.getReleasedStencil(stclContext), name, PSlot.AT_LEAST_ONE, true, false);
		PSlot<C, S> pslot = new PSlot<C, S>(slot, self);
		StencilFactory<C, S> factory = (StencilFactory<C, S>) stclContext.<C, S> getStencilFactory();
		S plugged = factory.createPStencil(stclContext, pslot, Key.NO_KEY, LinkStcl.class.getName(), path);
		//slot.setContainedStencil(stclContext, plugged, pslot);
		slot.addStencilInList(stclContext, plugged, pslot);
		return slot;
	}
}
