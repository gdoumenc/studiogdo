/*
 * Copyright GDO - 2004
 */
package com.gdo.helper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * Helpers for O/I operations.
 * </p>
 * 
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
public class IOHelper {

    public static InputStream EMPTY_INPUT_STREAM = new InputStream() {
        @Override
        public int read() throws IOException {
            return -1;
        }
    };
    public static OutputStream EMPTY_OUTPUT_STREAM = new OutputStream() {
        @Override
        public void write(int b) throws IOException {
        }
    };

    private static String FILE_SEP = System.getProperty("file.separator");

    private IOHelper() {
        // utility class, disable instanciation
    }

    public static InputStream getInputStream(String fileName, Locale locale, boolean addSearchInClassPath) {

        // locale sensitive search
        int index = fileName.lastIndexOf('.');
        if ((locale != null) && (index != -1)) {
            // split name and suffix
            String name = fileName.substring(0, index);
            String suffix = fileName.substring(index + 1);
            StringBuffer file = new StringBuffer();

            // add language suffix
            String language = locale.getLanguage();
            language = language.toLowerCase(locale);
            file.append(name).append('_').append(language);
            if (!StringUtils.isEmpty(language)) {
                String country = locale.getCountry();
                if (!StringUtils.isEmpty(country)) {
                    country = country.toUpperCase();
                    StringBuffer f = new StringBuffer(file);
                    f.append('_').append(country).append('.').append(suffix);
                    InputStream is = IOHelper.getFileInputStream(f.toString());
                    if (is != null) {
                        return is;
                    }
                }
                file.append('.').append(suffix);
                InputStream is = IOHelper.getFileInputStream(file.toString());
                if (is != null) {
                    return is;
                }
            }
        }

        // non locale sensitive search
        InputStream is = IOHelper.getFileInputStream(fileName);
        if (is != null) {
            return is;
        }

        // search in class path
        if (addSearchInClassPath) {
            return ClassHelper.getResourceAsStream(fileName, locale);
        }

        // noting founded
        return null;
    }

    /**
     * Finds a file with locale sensitive approach using a list of possible
     * pathes for the file and may using also class path (if not found)
     */
    public static InputStream getInputStream(String fileName, String[] pathes, Locale locale, boolean searchInClassPath) {

        // search in file directories
        if (!ClassHelper.isEmpty(pathes)) {
            for (String path : pathes) {
                if (StringUtils.isEmpty(path))
                    continue;

                StringBuffer p = new StringBuffer(path);
                if (!path.endsWith(IOHelper.FILE_SEP)) {
                    p.append(IOHelper.FILE_SEP);
                }
                InputStream is = IOHelper.getInputStream(p.append(fileName).toString(), locale, false);
                if (is != null)
                    return is;
            }
        }

        // search also in jar
        if (searchInClassPath) {
            return ClassHelper.getResourceAsStream(fileName, locale);
        }

        // noting founded
        return null;
    }

    @Deprecated
    private static InputStream getFileInputStream(String fileName) {
        try {
            return new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
        }
        return null;
    }

}