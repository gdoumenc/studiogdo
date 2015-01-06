/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils.event;

import com.gdo.stencils.Result;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.plug._PStencil;

/**
 * <p>
 * Interface for listening property value change.
 * </p>
 * 
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 */
public interface IPropertyChangeListener<C extends _StencilContext, S extends _PStencil<C, S>> {

    /**
     * Called on property value change.
     * 
     * @param evt
     * @return <tt></tt> if no issue in listening.
     */
    public Result propertyChange(PropertyChangeEvent<C, S> evt);

}
