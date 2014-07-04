/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils.faces.stencil;

import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.gdo.helper.StringHelper;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.faces.FacetsRenderer;
import com.gdo.stencils.faces.GdoTag;
import com.gdo.stencils.faces.GdoTagExpander;
import com.gdo.stencils.faces.RenderContext;
import com.gdo.stencils.facet.FacetResult;
import com.gdo.stencils.facet.FacetType;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;

public class GdoStencil<C extends _StencilContext, S extends _PStencil<C, S>> extends FacetsRenderer<C, S> {

	public static final String ESCAPE_HTML = "html";
	public static final String ESCAPE_XML = "xml";
	public static final String ESCAPE_JAVA = "java";
	public static final String ESCAPE_JAVA_SCRIPT = "javascript";
	public static final String ESCAPE_SQL = "sql";
	public static final String ESCAPE_AUTO_INC_SQL = "sql_auto_inc";

	String _path; // path to the property

	public GdoStencil(RenderContext<C, S> renderContext) {
		super(renderContext);
	}

	/**
	 * Gets the path to the stencil to be rendered.
	 * 
	 * @return the path to the stencil to be rendered.
	 */
	public String getPath() {
		return (String) getAttribute(GdoTag.PATH);
	}

	/**
	 * Gets the string format for this facet.
	 * 
	 * @return the string format.
	 */
	public String getMode() {
		return (String) getAttribute(GdoTag.MODE);
	}

	// the replace attribute is used to replace text in the expansion
	public String getReplace() {
		return (String) getAttribute(GdoTag.REPLACE);
	}

	// the locale attribute is used to get locale property value
	public String getLocale() {
		return (String) getAttribute(GdoTag.LOCALE);
	}

	// the escape attribute is used to escape text in the expansion
	// values may be xml, html, java, javascript, sql
	public String getEscape() {
		return (String) getAttribute(GdoTag.ESCAPE);
	}

	public String setPathParameter(String path) {
		setParameter(GdoTag.PATH, path);
		return path;
	}

	// the expanded attribute is used to expand the html before returning
	public boolean getExpanded() {
		return Boolean.parseBoolean((String) getAttribute(GdoTag.EXPANDED));
	}

	public String getPrefix() {
		return (String) getAttribute(GdoTag.PREFIX);
	}

	public String getSuffix() {
		return (String) getAttribute(GdoTag.SUFFIX);
	}

	public String getDefault() {
		return (String) getAttribute(GdoTag.DEFAULT);
	}

	@Override
	public boolean needExpansion(C stclContext) {
		return true;
	}

	@Override
	public void init(C stclContext) {
		try {

			// gets stencil path
			String path = getPath();
			if (StringUtils.isEmpty(path)) { // no path is same as this
				path = PathUtils.THIS;
			}

			// TODO should call normalize method to expannd all html escaped
			// character
			if (path.startsWith(GdoTagExpander.QUOTE)) {
				path = path.replaceAll(GdoTagExpander.QUOTE, "\"");
			}

			// path is always expanded
			RenderContext<C, S> renderContext = getRenderContext();
			S rendered = renderContext.getStencilRendered();
			this._path = rendered.format(stclContext, path);
		} catch (Exception e) {
			if (getLog().isWarnEnabled())
				getLog().warn(stclContext, e);
		}
	}

	@Override
	public String getHtmlContent(C stclContext) {
		try {
			RenderContext<C, S> renderContext = getRenderContext();
			String html;
			String facet = renderContext.getFacetType();

			// gets facet
			S rendered = getRenderContext().getStencilRendered();
			if (FacetType.LABEL.equals(facet)) {

				// the stencil rendered is just a property, so we get the value
				html = rendered.getString(stclContext, this._path, "");

				// then we format the value (as a string)
				String mode = getMode();
				if (StringUtils.isBlank(mode)) {
					mode = renderContext.getFacetMode();
				}
				if (StringUtils.isNotBlank(mode)) {
					html = String.format(mode, html);
				}
			} else {
				S stcl = rendered;

				// changes rendered stencil if path composed
				if (PathUtils.isComposed(this._path)) {

					// gets the stencil rendered
					stcl = rendered.getStencil(stclContext, this._path);

					// checks the stencil exists
					if (StencilUtils.isNull(stcl)) {
						if (getLog().isWarnEnabled()) {
							String msg = String.format("Cannot render stencil at path %s not defined on %s", this._path, renderContext.getStencilRendered());
							getLog().warn(stclContext, msg);
						}
					}
				}

				// creates new render context on this stencil
				String mode = getMode();
				if (StringUtils.isBlank(mode)) {
					mode = renderContext.getFacetMode();
				}
				RenderContext<C, S> newContext = new RenderContext<C, S>(stclContext, stcl, facet, mode);

				// get the facet from this new context
				FacetResult result = stcl.getFacet(newContext);
				if (result.isSuccess()) {
					Reader reader = new InputStreamReader(result.getInputStream());
					StringBuffer content = new StringBuffer(StringHelper.read(reader));
					result.closeInputStream();
					html = content.toString();
				} else {
					html = result.getMessage();
				}
			}

			// the resulting facet may be expanded
			if (getExpanded() && StencilUtils.containsStencilTag(html)) {
				S stcl = rendered.getStencil(stclContext, this._path);
				html = stcl.format(stclContext, html);
			}

			// do escape and replace on content
			String value = replace(escape(html));

			// add prefix (if content not empty)
			String prefix = getPrefix();
			if (StringUtils.isNotBlank(prefix) && StringUtils.isNotEmpty(html)) {
				value = prefix + value;
			}

			// add suffix (if content not empty)
			String suffix = getSuffix();
			if (StringUtils.isNotBlank(suffix) && StringUtils.isNotEmpty(html)) {
				value = value + suffix;
			}

			// default value (if no content)
			String def = getDefault();
			if (StringUtils.isNotEmpty(def) && StringUtils.isEmpty(html)) {
				value = def;
			}

			// returns the formated content
			return value;
		} catch (Exception e) {
			String msg = String.format("Cannot render property %s : %s", this._path, e);
			if (getLog().isWarnEnabled())
				getLog().warn(stclContext, msg);
			return msg;
		}
	}

	private String replace(String value) {
		String replace = getReplace();
		if (StringUtils.isEmpty(replace)) {
			return value;
		}

		// iterate over replacement definitions
		String[] replacements = StringHelper.splitShortString(replace, ':');
		String res = value;
		for (String replacement : replacements) {
			String[] replaces = StringHelper.splitShortString(replacement, ',');
			String regex = replaces[0];
			regex = regex.replaceAll("\\n", "\n");
			regex = regex.replaceAll("\\t", "\t");
			if (replaces.length < 2) {
				res = res.replaceAll(regex, "");
			} else {
				res = res.replaceAll(regex, replaces[1]);
			}
		}

		return res;
	}

	/**
	 * Escapes characters depending on format chosen
	 * 
	 * @param content
	 *          the content to be escaped.
	 * @return the escaped content.
	 */
	private String escape(String content) {

		// gets escape mode
		String escape = getEscape();
		if (StringUtils.isBlank(escape)) {
			return content;
		}

		// does escape
		if (GdoStencil.ESCAPE_XML.equals(escape)) {
			return StringEscapeUtils.escapeXml(content);
		} else if (GdoStencil.ESCAPE_HTML.equals(escape)) {
			return StringEscapeUtils.escapeHtml3(content);
		} else if (GdoStencil.ESCAPE_JAVA.equals(escape)) {
			return StringEscapeUtils.escapeJava(content);
		} else if (GdoStencil.ESCAPE_JAVA_SCRIPT.equals(escape)) {
			return content;
			// return StringEscapeUtils.escapeJavaScript(content);
		} else if (GdoStencil.ESCAPE_SQL.equals(escape)) {
			return StringHelper.escapeSql(content);
		} else if (GdoStencil.ESCAPE_AUTO_INC_SQL.equals(escape)) {
			if (StringUtils.isBlank(content))
				return "NULL";
			return "'" + StringHelper.escapeSql(content) + "'";
		}

		// returns same if other mode
		if (getLog().isWarnEnabled()) {
			String msg = String.format("escape mode %s not defined in GdoStencil", escape);
			getLog().warn(null, msg);
		}
		return content;
	}

	@Override
	public void expand(C stclContext) {

		// expand content
		String html = getHtmlContent(stclContext);
		GdoTagExpander<C, S> expander = new GdoTagExpander<C, S>(html, this);
		expander.addEcho(getRenderContext(), html, 0, html.length());
		/*
		 * GdoTagExpander<C, S> expander = new GdoTagExpander<C, S>(html, this); if
		 * (expander.containsGdoTags()) { expander.expand(stclContext,
		 * getRenderContext()); } else { expander.addEcho(getRenderContext(), html,
		 * 0, html.length()); }
		 */
	}

}