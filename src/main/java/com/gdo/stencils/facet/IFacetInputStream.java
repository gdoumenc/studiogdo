package com.gdo.stencils.facet;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author gdo
 * 
 */
public interface IFacetInputStream {

    /**
     * Retrieve facet input stream.
     * 
     * @return the facet input stream.
     */
    public InputStream getInputStream() throws IOException;

    /**
     * Called when the servlet closed connection.
     * 
     * @throws IOException
     */
    public void closeInputStream() throws IOException;

}
