/**
 * Copyright GDO - 2005
 */
package com.gdo.util;

import java.io.IOException;
import java.io.StringWriter;

/**
 * <p>
 * XML writer to a string.
 * </p>
 * 

 * <p>
 * &copy; 2004, 2005 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo & Guillaume Doumenc. Use
 * is subject to license terms.
 * </p>

 * 
 * @author Guillaume Doumenc (<a>
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */
public class XmlStringWriter extends XmlWriter {

    /**
     * Creates a new XML writer in string.
     * 
     * @param header
     *            if <tt>true</tt>, writes XML header.
     * @param indent
     *            starting indentation level.
     * @param encoding
     *            character encoding used.
     */
    public XmlStringWriter(boolean header, int indent, String encoding) throws IOException {
        super(new StringWriter(), indent, encoding);
        if (header) {
            writeHeader();
        }
    }

    /**
     * Creates a new XML writer in string with XML header.
     * 
     * @param encoding
     *            character encoding used.
     */
    public XmlStringWriter(String encoding) throws IOException {
        this(true, 0, encoding);
    }

    /**
     * @return the XML string generated.
     */
    public String getString() {
        StringWriter writer = (StringWriter) getWriter();
        return writer.getBuffer().toString();
    }

}
