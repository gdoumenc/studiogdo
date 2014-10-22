package com.gdo.stencils.descriptor;

import java.util.Hashtable;
import java.util.Map;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.slot._Slot;

public abstract class _SlotDescriptor<C extends _StencilContext, S extends _PStencil<C, S>> {

	/**
	 * The stencil which will contain the slot.
	 */
    public Map<String, String> _links;

    /**
     * The slot will be transient or not.
     */
    protected boolean _tranzient = true;

    public _SlotDescriptor() {
	}

	public _SlotDescriptor(Map<String, String> links) {
		this();
		_links = links;
	}

	public void addLink(String slot, String path) {
		if (_links == null) {
			_links = new Hashtable<String, String>();
		}
		_links.put(slot, path);
	}

	public abstract _Slot<C, S> add(C stclContext, String name, S self);
}
