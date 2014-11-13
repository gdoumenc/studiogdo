/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.interpreted;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.factory.IStencilFactory;
import com.gdo.stencils.factory.IStencilFactory.Mode;
import com.gdo.stencils.factory.InterpretedStencilFactory;
import com.gdo.stencils.plug._PStencil;

/**
 * <p>
 * A stencil descriptor is a simple instance descriptor with root tag defined as
 * stencil.
 * <p>
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a>
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com)</a>
 */
public class StencilDescriptor<C extends _StencilContext, S extends _PStencil<C, S>> extends InstDescriptor<C, S> {

    // root tag is stencil instead of inst
    @Override
    protected String getRootTag() {
        return InterpretedStencilFactory.STENCIL;
    }

    // all instances and plug are in load mode (as all plugs are stored in
    // stencil descriptor)
    @Override
    public IStencilFactory.Mode getMode() {
        return Mode.ON_LOAD;
    }

}