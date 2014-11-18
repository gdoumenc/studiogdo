/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils.facet;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.gdo.helper.StringHelper;
import com.gdo.stencils.Result;

/**
 * <p>
 * Result when getting a facet from a stencil.
 * </p>
 * A facet result contains :
 * <ul>
 * <li>a facet input stream
 * <li>a facet mime type
 * <li>a content length
 * <li>a map of HTML header properties
 * </ul>
 */

public class FacetResult extends Result {

    /**
     * Facet input stream (always defined)
     */
    private IFacetInputStream _input;

    /**
     * Mime type (always defined)
     */
    private String _mime;

    /**
     * -1 if not defined
     */
    private long _length = -1L;

    /**
     * Specific header information (as Content-Disposition)
     */
    private Map<String, String> _header;

    public FacetResult(IFacetInputStream input, String mime) {
        super(SUCCESS, FacetResult.class.getName(), 0, null, null);
        _input = input;
        _mime = mime;
    }

    /**
     * Constructor to be used when facet found.
     * 
     * @param input
     *            input stream to the facet.
     * @param mime
     *            mime type of the facet.
     */
    public FacetResult(InputStream input, String mime) {
        super(SUCCESS, FacetResult.class.getName(), 0, null, null);
        _input = new FacetInputStream(input);
        _mime = mime;
    }

    /**
     * Constructors to be used on error.
     * 
     * @param status
     *            error status.
     * @param msg
     *            error message.
     * @param other
     *            other result.
     */
    public FacetResult(byte status, String msg, Result other) {
        super(status, FacetResult.class.getName(), 0, msg, other);
    }

    /**
     * Gets the input stream of content.
     * 
     * @return the facet's input stream.
     * @throws IOException
     */
    public InputStream getInputStream() throws IOException {
        if (_input == null) {
            return StringHelper.EMPTY_STRING_INPUT_STREAM;
        }
        return _input.getInputStream();
    }

    /**
     * Closes inputStream (must be called after inputStream used).
     */
    public void closeInputStream() throws IOException {
        if (_input != null) {
            _input.closeInputStream();
        }
    }

    /**
     * Gets the mime type.
     * 
     * @return the facet mime type (text/plain if not defined)
     */
    public String getMimeType() {
        return (_mime != null) ? _mime : "text/plain";
    }

    /**
     * Gets the content length.
     * 
     * @return the facet content length.
     */
    public long getContentLength() {
        return _length;
    }

    /**
     * Sets the content length.
     * 
     * @param length
     *            the content length.
     */
    public void setContentLength(long length) {
        _length = length;
    }

    /**
     * Gets header defined.
     * 
     * @return the map of headers defined.
     */
    public Map<String, String> getHeader() {
        return _header;
    }

    /**
     * Sets a header with the given name and value.
     * 
     * @param name
     *            the name of the header.
     * @param value
     *            the header value.
     */
    public void setHeader(String name, String value) {
        if (_header == null) {
            _header = new HashMap<String, String>();
        }
        _header.put(name, value);
    }

    private class FacetInputStream implements IFacetInputStream {

        private InputStream _input;

        public FacetInputStream(InputStream input) {
            _input = input;
        }

        @Override
        public InputStream getInputStream() {
            return _input;
        }

        @Override
        public void closeInputStream() throws IOException {
            _input.close();
        }

    }
}