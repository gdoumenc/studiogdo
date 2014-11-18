/*
 * Copyright GDO - 2004
 */
package com.gdo.stencils.factory;

import com.gdo.stencils.atom.IAtom;

/**
 * <p>
 * The <tt>GdoClassLoader</tt> class is the simpliest abstract class that
 * implements the {@link IAtom} interface.
 * </p>
 * <p>
 * Main classes defined in this package are subclasses of this atom class.
 * </p>

 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>

 */
public class ClassLoader extends java.lang.ClassLoader {

    private static final ClassLoader INSTANCE = new ClassLoader();

    public static ClassLoader getImplementation() {
        return INSTANCE;
    }

    private ClassLoader() {
        // singleton
    }

    @Override
    public Package[] getPackages() {
        return super.getPackages();
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(name);
        } catch (ClassNotFoundException e) {
            return super.loadClass(name);
        }
    }
}