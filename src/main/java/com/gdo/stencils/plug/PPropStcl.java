package com.gdo.stencils.plug;

import java.io.InputStream;

import com.gdo.stencils.StclContext;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.prop.PropStencil;

public class PPropStcl extends PStcl {

	public PPropStcl(StclContext stclContext, PropStencil<StclContext, PStcl> prop, PSlot<StclContext, PStcl> pslot, IKey key) {
		super(stclContext, prop, pslot, key);
	}

	public PPropStcl(StclContext stclContext, PPropStcl prop, PSlot<StclContext, PStcl> slot, IKey key) {
		super(stclContext, prop, slot, key);
	}

	@Override
	public boolean isProp(StclContext stclContext) {
		return true;
	}

	@Override
	public String getType(StclContext stclContext) {
		try {
			PropStencil<StclContext, PStcl> prop = getStencil(stclContext);
			return prop.getType(stclContext, this);
		} finally {
			release(stclContext);
		}
	}

	@Override
	public boolean isExpand(StclContext stclContext) {
		try {
			PropStencil<StclContext, PStcl> prop = getStencil(stclContext);
			return prop.isExpand(stclContext, this);
		} finally {
			release(stclContext);
		}
	}

	@Override
	public void setExpand(StclContext stclContext, boolean expand) {
		try {
			PropStencil<StclContext, PStcl> prop = getStencil(stclContext);
			prop.setExpand(stclContext, expand, this);
		} finally {
			release(stclContext);
		}
	}

	@Override
	public String getValue(StclContext stclContext) {

		// gets first from properties of container (if calculated from cursor)
		String prop = getContainingSlot().getName(stclContext);
		IKey key = getContainer(stclContext).getKey();
		PSlot<StclContext, PStcl> pslot = getContainer(stclContext).getContainingSlot();
		if (pslot != null && pslot.getSlot() != null) {
			String value = pslot.getSlot().getProperty(stclContext, key, prop, pslot);
			if (value != null) {
				return value;
			}
		}

		// gets from property content
		try {
			PropStencil<StclContext, PStcl> p = getStencil(stclContext);
			return p.getValue(stclContext, this);
		} finally {
			release(stclContext);
		}
	}

	@Override
	public void setValue(StclContext stclContext, String value) {

		// sets first from properties of container (if calculated from cursor)
		String prop = getContainingSlot().getName(stclContext);
		IKey key = getContainer(stclContext).getKey();
		PSlot<StclContext, PStcl> pslot = getContainingSlot();
		pslot.getSlot().setProperty(stclContext, value, key, prop, pslot);

		// sets in property content
		try {
			PropStencil<StclContext, PStcl> p = getReleasedStencil(stclContext);
			p.setValue(stclContext, value, this);
		} finally {
			release(stclContext);
		}
	}

	@Override
	public String getExpandedValue(StclContext stclContext) {
		return getValue(stclContext);
	}

	@Override
	public String getNotExpandedValue(StclContext stclContext) {
		return getValue(stclContext);
	}

	@Override
	public InputStream getInputStream(StclContext stclContext) {
		try {
			PropStencil<StclContext, PStcl> prop = getStencil(stclContext);
			return prop.getInputStream(stclContext, this);
		} finally {
			release(stclContext);
		}
	}

}
