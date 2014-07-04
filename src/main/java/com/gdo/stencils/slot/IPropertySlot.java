/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils.slot;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug._PStencil;

/**
 * <p>
 * Extends the slot interface with setter/getter for property value.
 * </p>
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo & Guillaume Doumenc. Use
 * is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */
public interface IPropertySlot<C extends _StencilContext, S extends _PStencil<C, S>> {

	String getValue(C stclContext, PSlot<C, S> self);

	void setValue(C stclContext, String value, PSlot<C, S> self);

}
