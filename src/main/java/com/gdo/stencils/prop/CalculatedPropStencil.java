/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.prop;

import java.io.InputStream;
import java.io.OutputStream;

import com.gdo.helper.IOHelper;
import com.gdo.stencils._Stencil;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.plug._PStencil;

/**
 * <p>
 * Calculated property stencil.
 * <p>
 * <p>
 * This property use a calculator to retrieve its value ({@link IPropCalculator}
 * ).
 * </p>
 * 
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 */
public class CalculatedPropStencil<C extends _StencilContext, S extends _PStencil<C, S>> extends _Stencil<C, S> {

    private IPropCalculator<C, S> _calculator; // calculator used to create the

    // value

    public CalculatedPropStencil(C stclContext, IPropCalculator<C, S> calculator) {
        super(stclContext, null); // no value when created
        _calculator = calculator;
    }

    // value is get by property calculator
    @Override
    public String getValue(C stclContext, S self) {
        if (calculatorIsDefined(stclContext, self)) {
            try {
                return _calculator.getValue(stclContext, self);
            } catch (Exception e) {
                return logWarn(stclContext, "exception in the CalculatedPropStencil %s :%s", this, e);
            }
        }
        return "";
    }

    // value is set by property calculator
    @Override
    public String setValue(C stclContext, String value, S self) {
        if (calculatorIsDefined(stclContext, self)) {
            String old = _calculator.setValue(stclContext, value, self);
            notifyListeners(stclContext, value, old, self);
            return old;
        }
        return "";
    }

    // value is get by property calculator
    @Override
    public InputStream getInputStream(C stclContext, S self) {
        if (calculatorIsDefined(stclContext, self)) {
            return _calculator.getInputStream(stclContext, self);
        }
        return IOHelper.EMPTY_INPUT_STREAM;
    }

    // value is set by property calculator
    @Override
    public OutputStream getOutputStream(C stclContext, S self) {
        if (calculatorIsDefined(stclContext, self)) {
            return _calculator.getOutputStream(stclContext, self);
        }
        return IOHelper.EMPTY_OUTPUT_STREAM;
    }

    // check if the calculator is defined
    private boolean calculatorIsDefined(C stclContext, S self) {
        if (_calculator == null) {
            if (getLog().isWarnEnabled()) {
                String msg = String.format("no calculator defined for the CalculatedPropStencil %s", this);
                getLog().warn(stclContext, msg);
            }
            return false;
        }
        return true;
    }
}
