/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils.faces;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.facet.FacetContext;
import com.gdo.stencils.plug._PStencil;

/**
 * A <tt>RenderContext</tt> determines :
 * <ul>
 * <li>which stencil must be rendered (with a plugged trail associated),
 * <li>in which facet context.
 * </ul>
 * <p>
 * The stencil rendered is a plugged stencil. The trail path is defined from
 * this stencil (bottom) to the root (project at top).
 * </p>
 */
public final class RenderContext<C extends _StencilContext, S extends _PStencil<C, S>> extends FacetContext {

    private C _stclContext;
    private S _stencil;
    private boolean _asText;

    public RenderContext(C stclContext, S stencil, String facet, String mode) {
        super(facet, mode);
        _stclContext = stclContext;
        setStencilRendered(stencil);
    }

    @Override
    public RenderContext<C, S> clone() {
        RenderContext<C, S> clone = clone(null);
        clone.setStencilRendered(getStencilRendered());
        return clone;
    }

    // when a copy is done the stencil rendered is the stencil selected
    @SuppressWarnings("unchecked")
    public RenderContext<C, S> clone(S stencil) {
        RenderContext<C, S> clone = (RenderContext<C, S>) super.clone();
        clone.setStencilRendered(stencil);
        return clone;
    }

    public final C getStencilContext() {
        return _stclContext;
    }

    /**
     * @return the stencil currently rendered.
     */
    public final S getStencilRendered() {
        return _stencil;
    }

    public final void setStencilRendered(S stencil) {
        _stencil = stencil;
    }

    public boolean isAsText() {
        return _asText;
    }

    public void setAsText(boolean asText) {
        _asText = asText;
    }

    @Override
    public String toString() {
        return String.format("stencil rendered %s (facet %s)", getStencilRendered(), super.toString());
    }

}