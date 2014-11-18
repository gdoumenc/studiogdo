/**
 * Copyright GDO - 2005
 */
package com.gdo.util;

import java.io.IOException;
import java.io.Writer;
import java.security.InvalidParameterException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.gdo.helper.StringHelper;

/**
 * <p>
 * Very simple XML writer.
 * </p>
 * 

 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>

 */
public class XmlWriter extends Writer {

    private static final String CHARACTER_ENCODING = System.getProperty("file.encoding");

    private Writer _writer; // output writer
    private String _encoding; // character encoding used
    private int _indent; // indentation level
    private String _tag; // current tag manipulated

    /**
     * Creates a new XML writer.
     * 
     * @param writer
     *            underlying writer.
     * @param indent
     *            starting indentation level.
     * @param encoding
     *            character encoding used.
     */
    public XmlWriter(Writer writer, int indent, String encoding) {
        if (writer == null) {
            throw new NullPointerException("Cannot create a XML writer without an underlying writer");
        }
        if (StringUtils.isEmpty(encoding)) {
            throw new NullPointerException("Cannot create a XML writer without charset encoding");
        }
        _writer = writer;
        _encoding = encoding;
        _indent = indent;
    }

    /**
     * Creates a new XML writer with default system file encoding.
     * 
     * @param writer
     *            underlying writer.
     * @param indent
     *            starting indentation level.
     */
    public XmlWriter(Writer writer, int indent) {
        this(writer, indent, CHARACTER_ENCODING);
    }

    /**
     * Creates a new XML writer with default system file encoding and no initial
     * indentation.
     * 
     * @param writer
     *            underlying writer.
     */
    public XmlWriter(Writer writer) {
        this(writer, 0, CHARACTER_ENCODING);
    }

    /**
     * Returns the underlying writer used to write content.
     * 
     * @return the underlying writer.
     */
    public Writer getWriter() {
        return _writer;
    }

    /**
     * Returns the character encoding used.
     * 
     * @return the character encoding used.
     */
    public String getEncoding() {
        return _encoding;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.Writer#flush()
     */
    @Override
    public void flush() throws IOException {
        _writer.flush();
    }

    /**
     * Writes the XML header.
     * 
     * @throws IOException
     */
    public void writeHeader() throws IOException {
        writeText(String.format("<?xml version=\"1.0\" encoding=\"%s\"?>\n", _encoding));
    }

    /**
     * Writes a new XML starting tag, closing previous one if needed.
     * 
     * @param name
     *            opened tag's name.
     * @throws IOException
     */
    public void startElement(String name) throws IOException {
        if (StringUtils.isEmpty(name)) {
            throw new InvalidParameterException("XML tag name must not be null");
        }

        closeTagIfNeeded(true);
        writeIndent();
        _writer.write('<');
        _writer.write(name);

        _tag = name; // now a tag is opened
        _indent++;
    }

    /**
     * Writes a XML closing tag.
     * 
     * @throws IOException
     */
    public void closeElement() throws IOException {
        closeTagIfNeeded(true);
    }

    /**
     * @param name
     *            tag's name.
     * @param indent
     *            <tt>true</tt> if tag closed is indented (not the case for
     *            CDATA).
     * @throws IOException
     */
    public void endElement(String name, boolean indent) throws IOException {
        if (StringUtils.isEmpty(name)) {
            throw new NullPointerException("XML tag name must not be null");
        }

        _indent--;

        // tag on a single line
        if (_tag != null) {

            // _tag should be equals to name
            if (!_tag.equals(name)) {
                throw new InvalidParameterException("Ending a tag which is not opened");
            }

            // closing directly open tag
            _writer.write("/>\n");
            _tag = null;
            return;
        }

        // close tag
        if (indent) {
            writeIndent();
        }
        _writer.write("</");
        _writer.write(name);
        _writer.write(">\n");
    }

    /**
     * @param name
     * @throws IOException
     */
    public void endElement(String name) throws IOException {
        endElement(name, true);
    }

    /**
     * Writes an empty element.
     * 
     * @param name
     *            element name.
     * @param indent
     *            if <tt>ture</tt> then do indentation.
     * @throws IOException
     */
    public void writeElement(String name, boolean indent) throws IOException {
        startElement(name);
        endElement(name, indent);
    }

    /**
     * Writes attribute to the opened tag (the value is XML escaped).
     * 
     * @param name
     *            attribute's name.
     * @param value
     *            string attribute's value.
     */
    public void writeAttribute(String name, Object value) throws IOException {
        if (StringUtils.isEmpty(name)) {
            throw new NullPointerException("XML attribute name must not be null");
        }
        if (value != null) {
            _writer.write(' ');
            _writer.write(name);
            _writer.write("=\"");
            _writer.write(StringEscapeUtils.escapeXml(value.toString()));
            _writer.write('"');
        }
    }

    /**
     * Writes text directly at place (text may be null).
     * 
     * @param txt
     *            text written.
     */
    public void writeText(String txt) throws IOException {
        if (StringUtils.isEmpty(txt))
            return;

        _writer.write(txt);
    }

    /**
     * Writes text as an XML content, so indent on each line.
     * 
     * @param txt
     *            text written.
     */
    public void writeXML(String txt) throws IOException {
        if (StringUtils.isEmpty(txt))
            return;

        closeTagIfNeeded(true);
        for (String line : StringHelper.splitShortString(txt, '\n')) {
            writeIndent();
            _writer.write(line);
            _writer.write("\n");
        }
    }

    /**
     * Writes text in a CDATA section. The text included may be an XML content,
     * so if text written contains a ]] it will be replaced by <]>.
     */
    public String writeCDATA(String data) throws IOException {
        String tag = closeTagIfNeeded(false);
        if (StringUtils.isEmpty(tag)) {
            throw new IllegalStateException("CDATA not inside a tag");
        }

        _writer.write("<![CDATA[");
        if (!StringUtils.isEmpty(data)) {
            _writer.write(data.replaceAll("]]", "<]>"));
        }
        _writer.write("]]>");

        return tag;
    }

    /**
     * Writes text in a CDATA section and ends current open tag.
     */
    public void writeCDATAAndEndElement(String text) throws IOException {
        String tag = writeCDATA(text);
        endElement(tag, false);
    }

    /**
     * Writes an element with text in a CDATA section.
     */
    public void writeCDATAElement(String name, String text) throws IOException {
        startElement(name);
        writeCDATAAndEndElement(text);
    }

    /**
     * Writes a comment.
     */
    public void writeComment(Object value) throws IOException {
        writeIndent();
        _writer.write("<!--");
        if (value != null)
            _writer.write(value.toString());
        _writer.write("-->");
    }

    public int getIndent() {
        return _indent;
    }

    public void setIndent(int indent) {
        _indent = indent;
    }

    /**
     * @param newLine
     *            <tt>true</tt> if a new line should be added after the closed
     *            tag.
     * @return the tag closed.
     */
    public String closeTagIfNeeded(boolean newLine) throws IOException {
        String tag = _tag;
        if (!StringUtils.isEmpty(tag)) {
            _writer.write('>');
            if (newLine)
                _writer.write(StringHelper.NEW_LINE);
        }
        _tag = null;
        return tag;
    }

    // write blanks for indentation
    private void writeIndent() throws IOException {
        for (int i = 0; i < _indent; i++) {
            _writer.write(' ');
        }
    }

    // Writer interface

    @Override
    public void close() throws IOException {
        _writer.close();
    }

    @Override
    public void write(char cbuf[], int off, int len) throws IOException {
        _writer.write(cbuf, off, len);
    }

    @Override
    public void write(int c) throws IOException {
        _writer.write(c);
    }

    @Override
    public void write(char cbuf[]) throws IOException {
        _writer.write(cbuf);
    }

    @Override
    public void write(String str) throws IOException {
        if (StringUtils.isNotEmpty(str)) {
            String value = new String(str.getBytes(_encoding));
            _writer.write(value);
        }
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        write(str.substring(off, off + len));
    }

}
