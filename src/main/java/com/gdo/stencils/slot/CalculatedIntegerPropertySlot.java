/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.slot;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.poi.ss.formula.eval.NotImplementedException;

import com.gdo.helper.IOHelper;
import com.gdo.stencils._Stencil;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.prop.IPropCalculator;

/**
 * <p>
 * This slot contains a calculated integer property stencil.
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
public abstract class CalculatedIntegerPropertySlot<C extends _StencilContext, S extends _PStencil<C, S>> extends SingleCalculatedPropertySlot<C, S> implements IPropCalculator<C, S> {

    public CalculatedIntegerPropertySlot(C stclContext, _Stencil<C, S> in, String name) {
        super(stclContext, in, name, null);
        setCalculator(this);
    }

    /**
     * Returns the calculated value of the contained property.
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            the calculated property contained in the slot.
     * @return the calculated value of the contained property.
     */
    public abstract int getIntegerValue(C stclContext, S self);

    /**
     * Sets the calculated value of the contained property (by default throws an
     * exception).
     * 
     * @param stclContext
     *            the stencil context.
     * @param value
     *            the new value.
     * @param self
     *            the calculated property contained in the slot.
     * @return the new value set (may not be exactly the one set...)
     */
    public int setIntegerValue(C stclContext, int value, S self) {
        String msg = String.format("Cannot change %s value", self.getContainingSlot().getName(stclContext));
        throw new NotImplementedException(msg);
    }

    @Override
    public final String getValue(C stclContext, S self) {
        return Integer.toString(getIntegerValue(stclContext, self));
    }

    @Override
    public final String setValue(C stclContext, String value, S self) {
        return Integer.toString(setIntegerValue(stclContext, Integer.parseInt(value), self));
    }

    @Override
    public InputStream getInputStream(C stclContext, S self) {
        _Stencil<C, S> prop = getProperty(stclContext);
        if (prop == null) {
            return IOHelper.EMPTY_INPUT_STREAM;
        }
        return prop.getInputStream(stclContext, self);
    }

    @Override
    public OutputStream getOutputStream(C stclContext, S self) {
        _Stencil<C, S> prop = getProperty(stclContext);
        if (prop == null) {
            return IOHelper.EMPTY_OUTPUT_STREAM;
        }
        return prop.getOutputStream(stclContext, self);
    }

}
