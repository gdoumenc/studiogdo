/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils.faces.echo;

import java.io.IOException;
import java.io.Writer;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.faces.FacetsRenderer;
import com.gdo.stencils.faces.RenderContext;
import com.gdo.stencils.plug._PStencil;

/**
 * <p>
 * A component just echoing its content on the render output.
 * </p>
 */
public class GdoEcho<C extends _StencilContext, S extends _PStencil<C, S>> extends FacetsRenderer<C, S> {

    private StringBuffer _content;
    private boolean _asText;

    public GdoEcho(RenderContext<C, S> renderContext, String content) {
        super(renderContext);
        _content = new StringBuffer(content);
    }

    public GdoEcho(RenderContext<C, S> renderContext) {
        this(renderContext, "");
    }

    public StringBuffer getContent() {
        return _content;
    }

    public void setContent(StringBuffer content) {
        _content = content;
    }

    public void appendContent(char ch) {
        _content.append(ch);
    }

    public void appendContent(String added) {
        _content.append(added);
    }

    public void appendContent(String text, int pos, int len) {
        if (len == -1)
            _content.append(text.substring(pos));
        else
            _content.append(text.substring(pos, len));
    }

    @Override
    public boolean needExpansion(C stclContext) {
        return false;
    }

    public boolean isAsText() {
        return _asText;
    }

    public void setAsText(boolean asText) {
        _asText = asText;
    }

    @Override
    public void renderEnd(C stclContext, Writer writer) throws IOException {
        writer.write(getContent().toString());
    }

}
