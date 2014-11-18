/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils;

import java.io.InputStream;
import java.util.Locale;

import com.gdo.helper.IOHelper;
import com.gdo.helper.StringHelper;
import com.gdo.stencils.factory.IStencilFactory;
import com.gdo.stencils.factory.InterpretedStencilFactory;
import com.gdo.stencils.plug._PStencil;
import com.gdo.util.XmlWriter;

/**
 * <p>
 * Specific context used when manipulating stencils.
 * </p>
 * <p>
 * Parameters can be stored in context with class name used as name space.
 * </p>
 */

public abstract class _StencilContext implements Cloneable {

    private static final String CHARACTER_ENCODING = System.getProperty("file.encoding");

    // once released, should not be used
    private boolean _released = false;

    // this context may be in another locale to
    // retrieve locale sensitive information
    protected Locale _locale;

    // xml writer in save process
    protected XmlWriter _writer;

    /**
     * @return default system character encoding used.
     */
    public static String getCharacterEncoding() {
        return CHARACTER_ENCODING;
    }

    /**
     * Returns the context name (used for log trace)
     * 
     * @return the context name.
     */
    public String getName() {
        return "stencil context";
    }

    /**
     * Verifies the context is valid (should be used in all prefix method
     * verification).
     */
    protected void checkValidity() throws IllegalStateException {
        if (_released) {
            throw new IllegalStateException("StencilContext already released, should not be used");
        }
    }

    /**
     * Should be called when the context should be no more used.
     */
    public void release() {
        checkValidity();
        _released = true;
    }

    /**
     * Returns the class loader defined for this context.
     * 
     * @return the thread class loader.
     */
    public ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * Returns the default stencil factory used to create stencil.
     * 
     * @return the default stencil factory.
     */
    public <C extends _StencilContext, S extends _PStencil<C, S>> IStencilFactory<C, S> getStencilFactory() {
        return new InterpretedStencilFactory<C, S>();
    }

    /**
     * Returns the locale used in this context.
     * 
     * @return the locale used.
     */
    public Locale getLocale() {
        return (_locale != null) ? _locale : Locale.getDefault();
    }

    /**
     * Sets the locale for this context.
     * 
     * @param locale
     *            the locale used.
     */
    public void setLocale(Locale locale) {
        _locale = locale;
    }

    /**
     * Returns the current writer used to save the root stencil.
     * 
     * @return the current writer used to save the root stencil.
     */
    public XmlWriter getSaveWriter() {
        return _writer;
    }

    /**
     * Set the current writer used to save the root stencil.
     * 
     * @param writer
     *            the XML wrtier used to save the root stencil.
     */
    public void setSaveWriter(XmlWriter writer) {
        _writer = writer;
    }

    /**
     * @return pathes where the template descriptors can be found.
     */
    public String[] getTemplatePathes() {
        checkValidity();
        return StringHelper.EMPTY_STRINGS;
    }

    public InputStream getInputStream(String file, boolean searchInClassPath) {
        return IOHelper.getInputStream(file, null, getLocale(), searchInClassPath);
    }

    /**
     * Used for all parallel execution (thread, ...)
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
