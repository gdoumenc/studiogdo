package com.gdo.stencils.facet;

import java.io.InputStream;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.log.StencilLog;

/**
 * <p>
 * Generic abstract class for facet finder classes.
 * </p>
 */
public abstract class FacetFinder<C extends _StencilContext> {
    static final private StencilLog LOG = new StencilLog(FacetFinder.class);

    /**
     * @return the file's name where facet definition can be found by default
     *         (using template or stencil if defined).
     */
    public abstract String getFacetClassName(C stclContext);

    /**
     * @return the facet founds for this context.
     */
    public abstract InputStream getFacet(C stclContext, FacetContext facetContext);

    public static StencilLog getLog() {
        return LOG;
    }

    public String logWarn(C stclContext, String format, Object... params) {
        return getLog().logWarn(stclContext, format, params);
    }
}