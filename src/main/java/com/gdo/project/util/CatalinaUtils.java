/**
 * Copyright GDO - 2004
 */
package com.gdo.project.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.gdo.stencils.StclContext;
import com.gdo.stencils.facet.FacetResult;

public class CatalinaUtils {

    private static final int BUFFER_SIZE = 512;

    private CatalinaUtils() {
        // utility class, disables instanciation
    }

    /**
     * Writes HTTP answer for file transfer from a facet result.
     * 
     * @param stclContext
     *            the stencil context.
     * @param facetResult
     *            the facet result containing all file informations.
     */
    public static void writeFileResponse(StclContext stclContext, FacetResult facetResult) throws IOException {
        HttpServletResponse response = stclContext.getResponse();

        // sends error status if not success
        if (facetResult.isNotSuccess()) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, facetResult.getMessage());
        }

        // sends file
        else {

            // sends success header
            response.setStatus(HttpServletResponse.SC_OK);

            // sets content type (mime + encoding)
            response.setContentType(facetResult.getMimeType());
            response.setCharacterEncoding(StclContext.getCharacterEncoding());

            // sets content length
            long length = facetResult.getContentLength();
            response.setHeader("content-length", Long.toString(length));

            // adds others header info
            if (facetResult.getHeader() != null) {
                for (String name : facetResult.getHeader().keySet()) {
                    String value = facetResult.getHeader().get(name);
                    response.setHeader(name, value);
                }
            }

            // copies content
            if (facetResult.getContentLength() != 0) {
                response.setBufferSize(BUFFER_SIZE);
                InputStream in = facetResult.getInputStream();
                OutputStream out = response.getOutputStream();
                if (in != null && out != null) {
                    IOUtils.copy(in, out);
                    facetResult.closeInputStream();
                }
            }
        }
    }

}