package com.gdo.stencils.slot;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.plug._PStencil;

/**
 * <p>
 * This interface is used to filter a slot.
 * </p>
 * <p>
 * Should return <tt>true</tt> to keep the stencil in the slot when filtering.
 * </p>
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */
public interface SlotFilter<C extends _StencilContext, S extends _PStencil<C, S>> {

    // funtion to be implemented to keep the stencil from the filter
    boolean keep(C stclContext, S plugged);

}