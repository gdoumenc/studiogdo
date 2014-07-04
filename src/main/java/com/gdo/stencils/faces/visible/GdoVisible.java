/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils.faces.visible;

import org.apache.commons.lang3.StringUtils;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.cmd.CommandStencil;
import com.gdo.stencils.faces.FacetsRendererWithContent;
import com.gdo.stencils.faces.GdoTag;
import com.gdo.stencils.faces.GdoTagExpander;
import com.gdo.stencils.faces.RenderContext;
import com.gdo.stencils.faces.WrongTagSyntax;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.util.StencilUtils;

public class GdoVisible<C extends _StencilContext, S extends _PStencil<C, S>> extends FacetsRendererWithContent<C, S> {

	private static final String[] VISIBLE_TAGS = new String[] { "visible", "/visible" };
	private static final int[] VISIBLE_LENGTHS = new int[] { VISIBLE_TAGS[0].length(), VISIBLE_TAGS[1].length() };

	private GdoVisible<C, S> _parent; // parent visible if sub tag else
	private String _subTagLabel;

	public GdoVisible(RenderContext<C, S> context) {
		super(context);
	}

	@Override
	public boolean needExpansion(C stclContext) {
		return true;
	}

	@Override
	public void expand(C stclContext) throws WrongTagSyntax {

		// get content part depending of evaluation result
		String html;
		if (evaluate(stclContext)) {
			html = splitContent()[0];
		} else {
			html = splitContent()[1];
		}

		// expand content
		GdoTagExpander<C, S> expander = new GdoTagExpander<C, S>(html, this);
		if (expander.containsGdoTags()) {
			expander.expand(stclContext, getRenderContext());
		} else {
			expander.addEcho(getRenderContext(), html, 0, html.length());
		}
	}

	public GdoVisible<C, S> getParentTag() {
		return this._parent;
	}

	public void setParentTag(GdoVisible<C, S> parent) {
		this._parent = parent;
	}

	public String getSubTagLabel() {
		return this._subTagLabel;
	}

	public void setSubTag(GdoTag<C, S> tag) {
		this._subTagLabel = tag.getSubTagLabel();
	}

	public boolean isSubTag() {
		return this._subTagLabel != null;
	}

	public String getPath() {
		return (String) getAttributes().get(GdoTag.PATH);
	}

	public String getCommand() {
		return (String) getAttributes().get(GdoTag.COMMAND);
	}

	public String getFactory() {
		return (String) getAttributes().get(GdoTag.FACTORY);
	}

	@Override
	public String[] getTags() {
		return VISIBLE_TAGS;
	}

	@Override
	public int[] getTagsLength() {
		return VISIBLE_LENGTHS;
	}

	private String[] splitContent() {
		String[] res = new String[2];
		int start = 0;
		int count = 0; // count included visible tag

		// get string content under tag
		String content = getContent();
		if (StringUtils.isEmpty(content)) {
			return new String[] { "", "" };
		}

		int len = content.length();
		while (true) {
			while (start < len && content.charAt(start) != '<') {
				start++;
			}
			if (start >= len)
				break;
			start++;
			if (content.charAt(start++) != '$')
				continue;
			if (start >= len)
				break;
			int tagPos = start;
			if (content.charAt(tagPos) == '/')
				start++;
			if (content.charAt(start++) != 'v')
				continue;
			if (start >= len)
				break;
			if (content.charAt(start++) != 'i')
				continue;
			if (start >= len)
				break;
			if (content.charAt(start++) != 's')
				continue;
			if (start >= len)
				break;
			if (content.charAt(start++) != 'i')
				continue;
			if (start >= len)
				break;
			if (content.charAt(start++) != 'b')
				continue;
			if (start >= len)
				break;
			if (content.charAt(start++) != 'l')
				continue;
			if (start >= len)
				break;
			if (content.charAt(start++) != 'e')
				continue;
			if (start >= len)
				break;
			char ch = content.charAt(start++);
			if (ch != ':') {
				if (ch != '>') {
					while (start < len && content.charAt(start) != '>') {
						start++;
					}
				}
				if (content.charAt(tagPos) == '/') {
					count--;
				} else {
					count++;
				}
				continue;
			}
			if (count > 0)
				continue;
			if (start >= len)
				break;
			if (content.charAt(start++) != 'e')
				continue;
			if (start >= len)
				break;
			if (content.charAt(start++) != 'l')
				continue;
			if (start >= len)
				break;
			if (content.charAt(start++) != 's')
				continue;
			if (start >= len)
				break;
			if (content.charAt(start++) != 'e')
				continue;
			if (start >= len)
				break;
			if (content.charAt(start++) != '/') {
				if (getLog().isWarnEnabled()) {
					getLog().warn(null, "tag <visible:else/> not closed");
				}
				start--;
			}
			if (start >= len)
				break;
			if (content.charAt(start++) != '>')
				continue;
			res[0] = content.substring(0, start - 16);
			res[1] = content.substring(start);
			return res;
		}
		res[0] = content;
		res[1] = "";
		return res;
	}

	// TODO should use th call command of stencil
	public boolean evaluate(C stclContext) {
		RenderContext<C, S> renderContext = getRenderContext();
		S stencil = renderContext.getStencilRendered();
		try {

			// search in a boolean property if a path is defined
			String path = getPath();
			if (!StringUtils.isEmpty(path)) {
				return stencil.getBoolean(stclContext, path, false);
			}

			// search in a commandhttp://www.ecoemballages.fr/enseignants/
			// CommandStencil<C, S> cmdStcl =
			// getCommandFromParameters(stclContext, getFactory(),
			// getCommand());
			S cmdStcl = getCommandFromParameters(stclContext, getFactory(), getCommand());
			if (StencilUtils.isNotNull(cmdStcl)) {

				// create context with
				// paramethttp://www.ecoemballages.fr/enseignants/ers
				CommandContext<C, S> context = new CommandContext<C, S>(stclContext, stencil);
				putParamsInCommandContext(context);

				// execute the command
				CommandStencil<C, S> cmd = (CommandStencil<C, S>) cmdStcl.getReleasedStencil(stclContext);
				CommandStatus<C, S> status = cmd.execute(context, cmdStcl);
				Boolean value = status.getSuccessValue(cmdStcl.getName(stclContext));
				if (value == null) {
					String msg = String.format("The command %s doesn't return a boolean value", cmdStcl.getName(stclContext));
					getLog().warn(stclContext, msg);
				}
				return value.booleanValue();
			} else {
				if (getLog().isWarnEnabled()) {
					String msg = String.format("The command %s doesn't exist for evaluation in tag <$visible/>", getCommand());
					getLog().warn(stclContext, msg);
				}
			}
		} catch (Exception e) {
			logWarn(stclContext, "Cannot get visible result value", e);
		}
		return false;
	}

}
