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
 * This slot contains a calculated string property stencil.
 * </p>
 */
public abstract class CalculatedStringPropertySlot<C extends _StencilContext, S extends _PStencil<C, S>> extends SingleCalculatedPropertySlot<C, S> implements IPropCalculator<C, S> {

    public CalculatedStringPropertySlot(C stclContext, _Stencil<C, S> in, String name) {
        super(stclContext, in, name, null);
        setCalculator(this);
    }

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
    @Override
    public String setValue(C stclContext, String value, S self) {
        String msg = String.format("Cannot change %s value", self.getContainingSlot().getName(stclContext));
        throw new NotImplementedException(msg);
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