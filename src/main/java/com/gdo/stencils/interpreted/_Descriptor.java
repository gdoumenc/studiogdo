/**
 * <p>Base abstract class for all descriptors.<p>
 *
 * <p>
 * Descriptors are linked by extend hierarchy (as java classes)
 * </p>
 * 

 * <p>&copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved.
 * This software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.</p>

 */
package com.gdo.stencils.interpreted;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.log.StencilLog;
import com.gdo.stencils.plug._PStencil;
import com.gdo.util.XmlStringWriter;
import com.gdo.util.XmlWriter;

/**
 * Common abstract class for all descriptor classes.
 */
public abstract class _Descriptor<C extends _StencilContext, S extends _PStencil<C, S>> {

    private _Descriptor<C, S> _superDesc; // descriptor hierarchy
    private ArrayList<ParameterDescriptor<C, S>> _params; // parameter

    // description list
    // (if exists)

    /**
     * @return super descriptor in class hierarchy.
     */
    // not final for generics type redefinition
    public _Descriptor<C, S> getSuperDescriptor(C stclContext) {
        return _superDesc;
    }

    /**
     * Should be only used by XML parser.
     */
    // not final for generics type redefinition
    public void setSuperDescriptor(C stclContext, _Descriptor<C, S> superDesc) {
        _superDesc = superDesc;
    }

    /**
     * @return the list of parameter descriptors.
     */
    public List<ParameterDescriptor<C, S>> getParameterDescriptors() {
        if (_params == null) {
            return Collections.emptyList();
        }
        return _params;
    }

    /**
     * Adds a new parameter descriptor.
     */
    public final void addParamDescriptor(ParameterDescriptor<C, S> param) {
        if (_params == null) {
            _params = new ArrayList<ParameterDescriptor<C, S>>();
        }

        // verify index not already used
        int index = param.getIndexAsByte();
        for (ParameterDescriptor<C, S> p : _params) {
            if (index == p.getIndexAsByte()) {
                if (getLog().isWarnEnabled()) {
                    String msg = String.format("Index %s already used for parameter", Integer.toString(index));
                    getLog().warn(null, msg);
                }
                return;
            }
        }
        _params.add(param);
    }

    /**
     * Returns the array of parameter values defined (String, Integer, Boolean).
     * 
     * @return <tt>null</tt> if no parameter defined.
     */
    public Object getParameter(C stclContext, int index) {
        if (_params != null) {
            for (ParameterDescriptor<C, S> p : _params) {
                if (index == p.getIndexAsByte()) {
                    return p.getValue();
                }
            }
        }
        return null;
    }

    public abstract void save(C stclContext, XmlWriter instOut, XmlWriter plugOut) throws IOException;

    @Override
    public String toString() {
        try {
            XmlStringWriter declPart = new XmlStringWriter(false, 0, _StencilContext.getCharacterEncoding());
            XmlStringWriter plugPart = new XmlStringWriter(false, 0, _StencilContext.getCharacterEncoding());
            save(null, declPart, plugPart);
            return ((StringWriter) declPart.getWriter()).getBuffer().toString() + ((StringWriter) plugPart.getWriter()).getBuffer().toString();
        } catch (IOException e) {
            return logError(null, "error in descriptor toString");
        }
    }

    //
    // LOG PART
    //

    private static final StencilLog LOG = new StencilLog(_Descriptor.class);

    // TODO should be set protected
    public static StencilLog getLog() {
        return LOG;
    }

    protected static <C extends _StencilContext> String logWarn(C stclContext, String format, Object... params) {
        if (LOG.isWarnEnabled()) {
            String msg = (params.length == 0) ? format : String.format(format, params);
            LOG.warn(stclContext, msg);
            return msg;
        }
        return "";
    }

    protected static <C extends _StencilContext> String logError(C stclContext, String format, Object... params) {
        if (LOG.isErrorEnabled()) {
            String msg = (params.length == 0) ? format : String.format(format, params);
            LOG.error(stclContext, msg);
            return msg;
        }
        return "";
    }
}
