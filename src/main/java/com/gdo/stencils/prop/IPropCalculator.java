/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.prop;

import java.io.InputStream;
import java.io.OutputStream;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.plug._PStencil;

/**
 * <p>
 * Interface used for by a calculated property to retrieve its value.
 * <p>
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com)</a>
 * @see com.gdo.stencils.cmd.CommandContext Context
 */

public interface IPropCalculator<C extends _StencilContext, S extends _PStencil<C, S>> {

    /**
     * Gets the calculated value of the property.
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            the property as a plugged stencil.
     * @return the value of the calculated property.
     */
    public String getValue(C stclContext, S self) throws Exception;

    /**
     * Sets the calculated value of the property.
     * 
     * @param stclContext
     *            the stencil context.
     * @param value
     *            the value which should be set (may serve for calculation).
     * @param self
     *            the property as a plugged stencil.
     * @return the old value of the calculated property.
     */
    public String setValue(C stclContext, String value, S self);

    public InputStream getInputStream(C stclContext, S self);

    public OutputStream getOutputStream(C stclContext, S self);
}
