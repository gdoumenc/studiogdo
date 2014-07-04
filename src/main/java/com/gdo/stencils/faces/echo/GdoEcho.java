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
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */
public class GdoEcho<C extends _StencilContext, S extends _PStencil<C, S>> extends FacetsRenderer<C, S> {

	private StringBuffer _content;
	private boolean _asText;

	public GdoEcho(RenderContext<C, S> renderContext, String content) {
		super(renderContext);
		this._content = new StringBuffer(content);
	}

	public GdoEcho(RenderContext<C, S> renderContext) {
		this(renderContext, "");
	}

	public StringBuffer getContent() {
		return this._content;
	}

	public void setContent(StringBuffer content) {
		this._content = content;
	}

	public void appendContent(char ch) {
		this._content.append(ch);
	}

	public void appendContent(String added) {
		this._content.append(added);
	}

	public void appendContent(String text, int pos, int len) {
		if (len == -1)
			this._content.append(text.substring(pos));
		else
			this._content.append(text.substring(pos, len));
	}

	@Override
	public boolean needExpansion(C stclContext) {
		return false;
	}

	public boolean isAsText() {
		return this._asText;
	}

	public void setAsText(boolean asText) {
		this._asText = asText;
	}

	@Override
	public void renderEnd(C stclContext, Writer writer) throws IOException {
		writer.write(getContent().toString());
	}

}
