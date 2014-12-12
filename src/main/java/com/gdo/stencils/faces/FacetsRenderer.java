/**
 * <p>Common UI component.<p>
 *

 * <p>&copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved.
 * This software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.</p>

 */
package com.gdo.stencils.faces;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.gdo.helper.ClassHelper;
import com.gdo.stencils._Stencil;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.facet.FacetResult;
import com.gdo.stencils.factory.StencilFactory;
import com.gdo.stencils.log.StencilLog;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;
import com.gdo.util.TreeNode;

public abstract class FacetsRenderer<C extends _StencilContext, S extends _PStencil<C, S>> extends TreeNode<FacetsRenderer<C, S>> implements IFacetsRenderer<C, S> {
    private static final StencilLog LOG = new StencilLog(FacetsRenderer.class);

    private RenderContext<C, S> _renderCtxt; // rendering context
    private Map<String, Object> _attributes; // tag attributes defined
    private boolean _isExpanded = false; // expnasion should be done once

    public FacetsRenderer(RenderContext<C, S> renderCtxt) {
        _renderCtxt = renderCtxt;
    }

    @Override
    public final RenderContext<C, S> getRenderContext() {
        return _renderCtxt;
    }

    public final void setRenderContext(RenderContext<C, S> renderCtxt) {
        _renderCtxt = renderCtxt;
    }

    @Override
    public final Map<String, Object> getAttributes() {
        return _attributes;
    }

    public final Object getAttribute(String att) {
        if (_attributes == null)
            return null;
        return _attributes.get(att);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void setAttributes(Map<String, ?> parameters) {
        _attributes = (Map<String, Object>) parameters;
    }

    public final void setParameter(String att, Object o) {
        _attributes.put(att, o);
    }

    public final String getUID(C stclContext) {
        return getRenderContext().getStencilRendered().getUId(stclContext);
    }

    @Override
    public void init(C stclContext) {
    }

    @Override
    public void expand(C stclContext) throws WrongTagSyntax {
        if (_isExpanded && getLog().isWarnEnabled()) {
            String msg = String.format("The faces renderer %s is already expanded", this);
            getLog().warn(stclContext, msg);
        }
        _isExpanded = true;
    }

    @Override
    public String getHtmlContent(C stclContext) {
        return "";
    }

    // return stencil rendered facet
    @Override
    public FacetResult getFacet(C stclContext) {
        S stclRendered = getRenderContext().getStencilRendered();
        if (StencilUtils.isNull(stclRendered)) {
            return new FacetResult(FacetResult.ERROR, "Try to render a null stencil", null);
        }
        return stclRendered.getFacet(getRenderContext());
    }

    // create a command from a factory or a name
    // should be divided in two functions
    // BEWARE : CommandStencil should be released once no more used!!!
    public S getCommandFromParameters(C stclContext, String factory, String command) {
        S stclRendered = getRenderContext().getStencilRendered();
        try {

            // command defined by a factory
            if (!StringUtils.isEmpty(factory)) {

                // create command from factory
                StencilFactory<C, S> f = (StencilFactory<C, S>) stclContext.<C, S> getStencilFactory();
                Map<String, Object> params = getAttributes();
                Class<? extends _Stencil<C, S>> factoryClass = ClassHelper.loadClass(factory);
                return (S) f.createStencil(stclContext, factoryClass, params).self();
            }

            // command defined by a name
            if (!StringUtils.isEmpty(command)) {
                String name = command;

                // get command
                if (PathUtils.isComposed(name)) {
                    stclRendered = stclRendered.getStencil(stclContext, PathUtils.getPathName(name));
                    name = PathUtils.getLastName(name);
                }
                S cmd = stclRendered.getCommand(stclContext, name);
                if (StencilUtils.isNull(cmd) && getLog().isWarnEnabled()) {
                    String msg = String.format("Cannot found command %s in %s", name, stclRendered);
                    getLog().warn(stclContext, msg);
                }
                return cmd;
            }
        } catch (Exception e) {
            logWarn(stclContext, "Exception when trying to found command (factory:%s, command:%s) in %s (%s)", factory, command, stclRendered, e);
        }
        logWarn(stclContext, "Wrong command (factory:%s, command:%s) in %s", factory, command, stclRendered);
        return null;
    }

    public void putParamsInCommandContext(CommandContext<?, ?> context) {
        for (String param : CommandContext.PARAMS) {
            if (_attributes.containsKey(param)) {
                context.setRedefinedParameter(param, _attributes.get(param));
            }
        }
    }

    public void render(C stclContext, Writer writer) throws IOException {
        renderBegin(stclContext, writer);
        renderChildren(stclContext, writer);
        renderEnd(stclContext, writer);
    }

    /**
     * @throws IOException
     */
    public void renderBegin(C stclContext, Writer writer) throws IOException {
    }

    /**
     * @throws IOException
     */
    public void renderChildren(C stclContext, Writer writer) throws IOException {
        if (!hasChildren())
            return;
        for (FacetsRenderer<C, S> child : getChildren()) {
            child.render(stclContext, writer);
        }
    }

    /**
     * @throws IOException
     */
    public void renderEnd(C stclContext, Writer writer) throws IOException {
    }

    @Override
    public FacetsRenderer<C, S> getParent() {
        return super.getParent();
    }

    public static StencilLog getLog() {
        return LOG;
    }

    public String logWarn(C stclContext, String format, Object... params) {
        return getLog().logWarn(stclContext, format, params);
    }
}
