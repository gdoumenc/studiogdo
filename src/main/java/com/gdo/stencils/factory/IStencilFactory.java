/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.factory;

import java.io.Reader;

import com.gdo.stencils._Stencil;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.plug._PStencil;
import com.gdo.util.XmlWriter;

/**
 * <p>
 * Interface to stencil factory.
 * <p>

 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>

 * 
 * @author Guillaume Doumenc
 */
public interface IStencilFactory<C extends _StencilContext, S extends _PStencil<C, S>> {

    /**
     * Modes for stencil creation. The plug descriptors use this mode to be
     * performed or not depending on how the stencil is created.
     */
    enum Mode {
        ON_CREATION, ON_LOAD, ON_ALWAYS
    }

    /**
     * Returns the default class used to create a plugged stencil.
     */
    Class<? extends S> getDefaultPStencilClass(C stclContext);

    /**
     * Returns the default template name used to create stencil.
     */
    String getStencilDefaultTemplateName(C stclContext);

    /**
     * Returns the default template name used to create property stencil.
     */
    String getPropertyDefaultTemplateName(C stclContext);

    /**
     * Returns the default template name used to create calculated property
     * stencil.
     */
    String getCalculatedPropertyDefaultTemplateName(C stclContext);

    /**
     * Loads a stencil from a stencil description reader.
     * 
     * @param name
     *            the name of the description (used only for trace in case of
     *            error).
     * @return the stencil created from the description.
     */
    _Stencil<C, S> loadStencil(C stclContext, Reader in, String name);

    /**
     * Saves a stencil in a stencil description stream.
     */
    void saveStencil(C stclContext, S stencil, XmlWriter writer);
}