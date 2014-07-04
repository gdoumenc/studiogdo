/*
 * Copyright GDO - 2004
 */
package com.gdo.stencils.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.gdo.helper.StringHelper;

/**
 * <p>
 * Basic stencil class.
 * </p>
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo & Guillaume Doumenc. Use
 * is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */
public class ClassUtils {
	protected static final Log _log = LogFactory.getLog(ClassUtils.class);

	protected ClassUtils() {
	}

	public static InputStream getInputStream(String resource, Locale locale, String path) {
		if (getLog().isTraceEnabled()) {
			getLog().trace("Getting input stream " + resource); //$NON-NLS-1$
		}

		if (!StringUtils.isEmpty(path)) {
			String name = path;
			if (!path.endsWith("" + PathUtils.SEP))
				name += PathUtils.SEP;
			InputStream is = getInputStream(name + resource, locale);
			if (is != null)
				return is;
		}
		InputStream is = getInputStream(resource, locale);
		if (is != null)
			return is;

		if (getLog().isTraceEnabled()) {
			if (!StringUtils.isEmpty(path)) {
				getLog().trace("Cannot get input stream for " + resource + " in [" + path + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else {
				getLog().trace("Cannot get input stream for " + resource); //$NON-NLS-1$
			}
		}
		return null;
	}

	public static InputStream getInputStream(String resource, Locale locale) {

		// locale sensitive search
		if (locale != null) {
			String language = locale.getLanguage();
			String country = locale.getCountry();

			String name = resource;
			String suffix = StringHelper.EMPTY_STRING;
			int index = resource.lastIndexOf("."); //$NON-NLS-1$
			if (index != -1) {
				name = resource.substring(0, index);
				suffix = resource.substring(index + 1);
			}

			if (language != null) {
				language = language.toLowerCase();
				if (country != null) {
					country = country.toUpperCase();
					InputStream is = getInputStream(name + '_' + language + '_' + country + '.' + suffix, null);
					if (is != null)
						return is;
				}
				InputStream is = getInputStream(name + '_' + language + '.' + suffix, null);
				if (is != null)
					return is;
			}
		}

		// search locally
		InputStream is = null;
		try {
			is = new FileInputStream(resource);
		} catch (FileNotFoundException e) {
		}
		if (is != null)
			return is;

		// search in class path
		return getResourceAsStream(resource);
	}

	public static URL getResource(String resource, Locale locale) {
		if (locale != null) {
			String language = locale.getLanguage();
			String country = locale.getCountry();

			int index = resource.lastIndexOf("."); //$NON-NLS-1$
			String res = resource;
			String suffix = ""; //$NON-NLS-1$
			if (index != -1) {
				res = resource.substring(0, index);
				suffix = resource.substring(index + 1);
			}

			if (language != null) {
				language = language.toLowerCase();
				URL url = getResource(res + '_' + language + '.' + suffix);
				if (url != null)
					return url;

				if (!StringUtils.isEmpty(country)) {
					country = country.toUpperCase();
					url = getResource(res + '_' + language + '_' + country + '.' + suffix);
					if (url != null)
						return url;
				}
			}
		}
		return getResource(resource);
	}

	public static InputStream getResourceAsReloadableStream(String resource, Locale locale) {

		// try to get it as file
		try {
			URL url = getResource(resource, locale);
			String path = url.toURI().getPath();
			File file = new File(path);
			return new FileInputStream(file);
		} catch (Exception e) {
		}
		return getResourceAsStream(resource, locale);
	}

	/**
	 * @return input stream from this thread or this class loader with local
	 *         sensitive search.
	 */
	public static InputStream getResourceAsStream(String resource, Locale locale) {
		if (locale != null) {
			String language = locale.getLanguage();
			String country = locale.getCountry();

			int index = resource.lastIndexOf(StringHelper.DOT);
			String res = resource;
			String suffix = StringHelper.EMPTY_STRING;
			if (index != -1) {
				res = resource.substring(0, index);
				suffix = resource.substring(index + 1);
			}

			if (!StringUtils.isEmpty(language)) {
				language = language.toLowerCase();
				StringBuffer str = new StringBuffer(res);
				str.append(StringHelper.UNDERSCORE).append(language).append(StringHelper.UNDERSCORE).append(suffix);
				InputStream input = getResourceAsStream(str.toString());
				if (input != null)
					return input;

				if (!StringUtils.isEmpty(country)) {
					country = country.toLowerCase();
					str = new StringBuffer(res);
					str.append(StringHelper.UNDERSCORE).append(language).append(StringHelper.UNDERSCORE).append(suffix);
					input = getResourceAsStream(str.toString());
					if (input != null)
						return input;
				}
			}
		}
		return getResourceAsStream(resource);
	}

	public static long lastModifiedResource(String resource, Locale locale) {
		try {
			URL url = getResource(resource, locale);
			if (url == null) {
				return 0;
			}
			String path = url.toURI().getPath();
			if (path != null) {
				File file = new File(url.toURI().getPath());
				return file.lastModified();
			}
		} catch (Exception e) {
		}
		return 0;
	}

	@SuppressWarnings("unchecked")
	public static <T> Class<T> loadClass(String className) throws ClassNotFoundException {
		try {
			return (Class<T>) Thread.currentThread().getContextClassLoader().loadClass(className);
		} catch (ClassNotFoundException e) {
			return (Class<T>) ClassUtils.class.getClassLoader().loadClass(className);
		}
	}

	/**
	 * @return URL from this thread or this class loader.
	 */
	public static URL getResource(String resource) {
		URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
		if (url != null)
			return url;

		url = ClassUtils.class.getClassLoader().getResource(resource);
		return url;
	}

	/**
	 * @return input stream from this thread or this class loader.
	 */
	public static InputStream getResourceAsStream(String resource) {
		InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
		if (input != null)
			return input;

		input = ClassUtils.class.getClassLoader().getResourceAsStream(resource);
		return input;
	}

	private static ResourceBundle clearCacheAndGetBundle(String fileName, Locale locale, ClassLoader loader) {
		ResourceBundle bundle = ResourceBundle.getBundle(fileName, locale, loader);
		/*
		 * try { Class klass = bundle.getClass().getSuperclass(); Field field =
		 * klass.getDeclaredField("cacheList"); field.setAccessible(true);
		 * sun.misc.SoftCache cache = (sun.misc.SoftCache) field.get(null);
		 * cache.clear(); field.setAccessible(false); } catch (Exception e) { if
		 * (log.isErrorEnabled()) log.error("Cannot cache ResourceBundle", e); }
		 */
		return bundle;
	}

	public static Log getLog() {
		return _log;
	}

}