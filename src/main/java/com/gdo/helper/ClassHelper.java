/*
 * Copyright GDO - 2004
 */
package com.gdo.helper;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Helper for class and resource manipulations.
 * </p>
 * 

 */
public class ClassHelper {

    private ClassHelper() {
        // utility class, disable instanciation
    }

    /**
     * Loads a class from a class name.
     * 
     * @param className
     *            the new class name.
     * @return the class loaded or <tt>null</tt> if the class cannot be loaded.
     */
    @SuppressWarnings("unchecked")
    public static <K> Class<K> loadClass(String className) {
        try {
            return (Class<K>) Class.forName(className);
            // ClassLoader loader =
            // Thread.currentThread().getContextClassLoader();
            // return (Class<K>) loader.loadClass(className);
        } catch (Exception e) {
            logWarn("Exception while loading class %s: %s", className, e);
        }
        return null;
    }

    /**
     * Creates a new class instance from parameters.
     * 
     * @param clazz
     *            the class to be instanciated.
     * @param params
     *            the constructor parameters.
     * @return the new instance (throw <tt>InvocationTargetException</tt> if the
     *         class cannot be instanciated).
     */
    @SuppressWarnings("unchecked")
    public static <K> K newInstance(Class<K> clazz, Object... params) {

        // tests first with no parameter
        if (ClassHelper.isEmpty(params)) {
            try {
                return clazz.newInstance();
            } catch (Exception e) {
                logError("No available constructor %s() (or internal error %s)", clazz, e);
                return null;
            }
        }

        // tests from all constructors (cannot use get constructor from type as
        // using generic)
        for (Constructor<?> constructor : clazz.getConstructors()) {

            // must have same parameters numbers
            if (constructor.getParameterTypes().length != params.length) {
                continue;
            }

            try {
                return (K) constructor.newInstance(params);
            } catch (Exception e) {
                // try another constructor
            }
        }

        logError("No available constructor %s(%s)", clazz, params);
        return null;
    }

    /**
     * Searches a resource URL in class loader.
     * 
     * @param resource
     *            the resource searched.
     * @return the url of the resource (<tt>null</tt> if not found).
     */
    public static URL getResource(String resource) {

        /* searches in current thread
        URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
        if (url != null) {
            return url;
        }*/

        // searches in class loader
        URL url = ClassHelper.class.getClassLoader().getResource(resource);
        if (url != null) {
            return url;
        }

        return null;
    }

    /**
     * Searches a resource as an input stream in class loader.
     * 
     * @param resource
     *            the resource searched.
     * @return an input stream on the resource (<tt>null</tt> if not found).
     */
    public static InputStream getResourceAsStream(String resource) {

        // searches in current thread
        InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        if (input != null) {
            return input;
        }

        // searches in class loader
        input = ClassHelper.class.getClassLoader().getResourceAsStream(resource);
        if (input != null) {
            return input;
        }

        return null;
    }

    /**
     * Search a resource in the current thread class loader and if not found in
     * the class loader of this file.
     * 
     * TODO TO BE REMOVED
     */
    public static InputStream getResourceAsStream(String resource, Locale locale) {

        // locale sensitive search
        int index = resource.lastIndexOf('.');
        if ((locale != null) && (index != -1)) {
            // split name and suffix
            String res = resource.substring(0, index);
            String suffix = resource.substring(index + 1);
            StringBuffer file = new StringBuffer();

            // add language suffix
            String language = locale.getLanguage();
            language = language.toLowerCase(locale);
            file.append(res).append('_').append(language);
            if (!StringUtils.isEmpty(language)) {
                // add country suffix
                String country = locale.getCountry();
                if (!StringUtils.isEmpty(country)) {
                    country = country.toUpperCase(locale);
                    StringBuffer f = new StringBuffer(file);
                    f.append('_').append(country).append('.').append(suffix);
                    InputStream input = ClassHelper.getResourceAsStream(f.toString());
                    if (input != null) {
                        return input;
                    }
                }
            }
            file.append('.').append(suffix);
            InputStream input = ClassHelper.getResourceAsStream(file.toString());
            if (input != null) {
                return input;
            }
        }

        // non locale sensitive search
        return ClassHelper.getResourceAsStream(resource);
    }

    //
    // Array wrapper
    //

    /**
     * Tests if an array is null or empty.
     * 
     * @return <tt>true</tt> if the array is null or empty.
     */
    public static boolean isEmpty(Object[] array) {
        return (array == null || array.length == 0);
    }

    /**
     * Tests if a collection is not null and not empty.
     * 
     * @param list
     *            the collection.
     * @return <tt>true</tt> if the array is null or empty.
     */
    public static boolean isEmpty(Collection<?> list) {
        return (list == null || list.size() == 0);
    }

    /**
     * Tests if a map is not null and not empty.
     * 
     * @param map
     *            the map.
     * @return <tt>true</tt> if the array is null or empty.
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return ((map == null) || (map.size() == 0));
    }

    //
    // LOG PART
    //

    private static final Log LOG = LogFactory.getLog(ClassHelper.class);

    private static final void logWarn(String format, Object... params) {
        if (LOG.isWarnEnabled()) {
            String msg = String.format(format, params);
            LOG.warn(msg);
        }
    }

    private static final void logError(String format, Object... params) {
        if (LOG.isErrorEnabled()) {
            String msg = String.format(format, params);
            LOG.error(msg);
        }
    }

}