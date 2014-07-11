/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils.event;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.plug._PStencil;

/**
 * <p>
 * Event dispatched when the value of a property is changed.
 * </p>
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 * </blockquote>
 */
public class PropertyChangeEvent<C extends _StencilContext, S extends _PStencil<C, S>> {

	private C _stclContext;
	private S _prop;
	private String _old;
	private String _new;

	public PropertyChangeEvent(C stclContext, S prop, String oldValue, String newValue) {
		this._stclContext = stclContext;
		this._prop = prop;
		this._old = oldValue;
		this._new = newValue;
	}

	public C getStencilContext() {
		return this._stclContext;
	}

	public S getPluggedProperty() {
		return this._prop;
	}

	public String getNewValue() {
		return this._new;
	}

	public String getOldValue() {
		return this._old;
	}

}