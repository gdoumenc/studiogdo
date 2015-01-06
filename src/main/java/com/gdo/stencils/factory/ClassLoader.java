/*
 * Copyright GDO - 2004
 */
package com.gdo.stencils.factory;

public class ClassLoader extends java.lang.ClassLoader {

    private static final ClassLoader INSTANCE = new ClassLoader();

    public static ClassLoader getImplementation() {
        return INSTANCE;
    }

    private ClassLoader() {
        // singleton
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