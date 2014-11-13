/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils.faces;

import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import com.gdo.helper.ClassHelper;
import com.gdo.helper.StringHelper;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.faces.echo.GdoEcho;
import com.gdo.stencils.faces.root.GdoRootLabel;
import com.gdo.stencils.log.StencilLog;
import com.gdo.stencils.plug._PStencil;
import com.gdo.util.XmlStringWriter;

/**
 * <p>
 * Gdo tag expander matcher.
 * </p>
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo & Guillaume Doumenc. Use
 * is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a>
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */
public class GdoTagExpander<C extends _StencilContext, S extends _PStencil<C, S>> {
    private static final StencilLog LOG = new StencilLog(GdoTagExpander.class);

    public static final String BLANK = "%20";
    public static final String QUOTE = "%27";
    private static final String LT1 = "&lt;";
    private static final String LT2 = "%3C";
    private static final String GT1 = "&gt;";
    private static final String GT2 = "%3E";

    private final String _html; // html string to be expanded
    private final int _len; // length of this string (avoiding recalculate it)
    private int _pos; // position of the character reader
    private FacetsRenderer<C, S> _parent; // current parent component when the
    // text is expanded
    private final Stack<FacetsRenderer<C, S>> _openTagsStack = new Stack<FacetsRenderer<C, S>>(); // tags

    // stack

    public GdoTagExpander(String html, FacetsRenderer<C, S> parent) {
        _html = removeEscapedDoubleQuote(html);
        _pos = 0;
        _len = _html.length();
        _parent = parent;
    }

    public GdoTagExpander(String html, RenderContext<C, S> renderContext) {
        this(html, new GdoRootLabel<C, S>(renderContext));
    }

    public String expand(C stclContext) {

        // on debug, the context may be not defined, so no expansion available
        if (stclContext == null)
            return _html;

        // optimization (do nothing if <$ is not in the text to be expanded)
        if (!containsGdoTags())
            return _html;

        // if extension then get context (parent must be defined)
        if (_parent == null) {
            if (getLog().isWarnEnabled()) {
                getLog().warn(stclContext, "The tag expander was not able to create its root facets renderer");
            }
            return _html;
        }
        RenderContext<C, S> renderContext = _parent.getRenderContext();

        // expand text and render
        try {
            expand(stclContext, renderContext);
            XmlStringWriter writer = new XmlStringWriter(false, 0, _StencilContext.getCharacterEncoding());
            _parent.render(stclContext, writer);
            return writer.getString();
        } catch (Exception e) {
            String msg = logWarn(stclContext, "cannot expand %s to string in context %s (%s)", _html, renderContext, e);
            return msg;
        }
    }

    /**
     * Creates renderer components tree from text.
     * 
     * @throws WrongTagSyntax
     */
    public void expand(C stclContext, RenderContext<C, S> renderContext) throws WrongTagSyntax {
        while (!atEnd()) {
            // add text before '<' if exists
            int start = _pos;
            int skipped = skipUntilLtOrEnd();
            if (skipped > 0) {
                addEcho(renderContext, _html, start, _pos);
            }
            if (atEnd())
                break;

            // skip '<'
            start = _pos;
            skipLt();

            // expand if this is a tag
            if (!atEnd()) {
                expandTag(stclContext, renderContext, null);
            } else {
                addEcho(renderContext, _html, start, _len); // echo
                // '<' or
                // equivalent
            }
        }
    }

    // a '<' has been found so try to match the tag structure
    public void expandTag(C stclContext, RenderContext<C, S> renderContext, StringBuffer res) throws WrongTagSyntax {

        // not a gdo tag
        if (_html.charAt(_pos) != '$') {
            // TODO should rewrite LT (not only <)
            addEcho(renderContext, '<');

            // add comment as it (echo) but not on res (USEFULL to add '<'
            // ???????)
            int index = skipComment(renderContext);
            if (index >= 0) {
                // script are defined in comment and may contain gdo tags
                String comment = _html.substring(index + 3, _pos - 3);
                GdoTagExpander<C, S> exp = new GdoTagExpander<C, S>(comment, renderContext);
                comment = String.format("!-- %s -->", exp.expand(stclContext));
                GdoEcho<C, S> echo = createEcho(renderContext);
                echo.appendContent(comment);
            }
            if (res != null)
                res.append('<');
            return;
        }

        // expand tag
        int start = ++_pos; // skip $
        int skipped = skipUntilSpaceOrGT('>');

        // get gdo tag label and create tag
        String label = _html.substring(start, start + skipped);
        GdoTag<C, S> tag = new GdoTag<C, S>(label);

        // find blank before '>' means attributes are defined
        if (atEnd()) {
            throw new WrongTagSyntax("Unclosed tag " + _html.substring(start));
        }
        if (isSpace() > 0) {
            skipSpace();
            expandAttributes(stclContext, renderContext, tag);
        }
        skipGt();

        // expand tag
        if (tag.isEndTag()) {
            // pop current parent
            if (wasPreviouslyOpened(tag)) {
                _parent = _openTagsStack.pop();
            } else {
                throw new WrongTagSyntax("The closing tag " + tag + " was not previously opened");
            }
        } else if (tag.isClosed()) {
            FacetsRenderer<C, S> component = tag.getComponent(stclContext, renderContext);
            expandAndAddChildComponent(stclContext, renderContext, component, res);
        } else {
            // unclosed tag must have a content
            if (!hasContent(tag)) {
                throw new WrongTagSyntax("The tag " + tag + " cannot be unclosed");
            }

            // get content before expanding
            FacetsRenderer<C, S> component = tag.getComponent(stclContext, renderContext);
            if (component instanceof FacetsRendererWithContent) {
                FacetsRendererWithContent<C, S> compContent = (FacetsRendererWithContent<C, S>) component;
                String under = skipUnderTag(compContent);
                if (!StringUtils.isEmpty(under))
                    compContent.setContent(under);
            }
            expandAndAddChildComponent(stclContext, renderContext, component, res);

            // push current tag as opened
            if (wasPreviouslyOpened(tag)) {
                _openTagsStack.push(_parent);
                _parent = component;
            }
        }
    }

    private void expandAndAddChildComponent(C stclContext, RenderContext<C, S> renderContext, FacetsRenderer<C, S> component, StringBuffer res) {
        if (res == null) {
            _parent.addChild(component);
            try {
                component.init(stclContext);
            } catch (Exception e) {
                String msg = String.format("Error in initializing tag %s : %s", component.getClass().getName(), e.getMessage());
                if (getLog().isWarnEnabled()) {
                    getLog().warn(stclContext, msg);
                }
                addEcho(renderContext, msg, 0, -1);
            }
            if (component.needExpansion(stclContext)) {
                try {
                    component.expand(stclContext);
                } catch (Exception e) {
                    String msg = String.format("Error in initializing or expanding tag %s : %s", component.getClass().getName(), e.getMessage());
                    if (getLog().isWarnEnabled()) {
                        getLog().warn(stclContext, msg);
                    }
                    addEcho(renderContext, msg, 0, -1);
                }
            }
        } else {
            getLog().warn(stclContext, "expansion of tag in string : TO BE DONE");
        }
    }

    public void expandAttributes(C stclContext, RenderContext<C, S> renderContext, GdoTag<C, S> tag) throws WrongTagSyntax {
        StringBuffer attributes = new StringBuffer();

        // set the attributes text (remove ending characters '>' or '<' for
        // recursive expansion)
        while (!atEnd()) {
            int start = _pos;
            boolean found = skipUntilGtOrLt('>', '<');
            if (!found) {
                throw new WrongTagSyntax("A quoted attribute is not closed : " + _html.substring(start));
            }
            if (isLt() > 0) {
                StringHelper.append(attributes, _html, start, _pos);
                _pos++; // skip '<' and recursive call
                expandTag(stclContext, renderContext, attributes);
            } else if (isGt() > 0) {
                // if closed tag
                if (_html.charAt(_pos - 1) == '/') {
                    StringHelper.append(attributes, _html, start, _pos - 1);
                    tag.setClosed();
                } else {
                    StringHelper.append(attributes, _html, start, _pos);
                }
                break;
            } else {
                throw new WrongTagSyntax("Internal error in expandAttributes");
            }
        }

        // do nothing if the attributes text is empty
        if (StringUtils.isEmpty(attributes.toString()))
            return;

        // blank separated parameters
        String[] elts = attributes.toString().replaceAll(BLANK, "").split("(\\s)+");
        Map<String, String> map = new ConcurrentHashMap<String, String>(elts.length);
        for (int i = 0; i < elts.length; i++) {
            // search (param=value) pairs
            String elt = elts[i];
            if (StringUtils.isEmpty(elt))
                continue;
            String[] str = StringHelper.splitShortStringAndTrim(elt, '=');

            // not simple param= value
            if (str.length < 2) {
                if (str.length == 1 && str[0].charAt(0) == '\\')
                    continue; // skip single '\' at end of line
                throw new WrongTagSyntax("Stencil tag parameters are defined as value binding (param=value) : " + elt);
            }
            if (str.length > 2) // value with = inside
            {
                for (int j = 2; j < str.length; j++) {
                    str[1] += "=" + str[j];
                }
            }

            // get param and value in the array
            String param = str[0];
            StringBuffer value;

            // the value is not enclosed
            if (str[1].charAt(0) != '\'' && str[1].charAt(0) != '"') {
                value = new StringBuffer(str[1]);
            }
            // if the value contains ' then spaces may be defined and should add
            // next elements (like param='v a l u e')
            else if (str[1].charAt(0) == '\'') {
                if (str[1].length() > 1 && str[1].charAt(str[1].length() - 1) == '\'') {
                    value = new StringBuffer(str[1].substring(1, str[1].length() - 1)); // simple
                    // attribute
                } else {
                    value = new StringBuffer(str[1].substring(1));
                    i++;

                    // while the ' at end is not found then add separated
                    // elements to the value
                    while (i < elts.length && !elts[i].endsWith("'")) {
                        value.append(' ');
                        value.append(elts[i]); // add next element to the value
                        i++; // skip next element
                    }

                    // add the element with the ' at end
                    if (i < elts.length) {
                        value.append(' ');
                        value.append(elts[i].substring(0, elts[i].length() - 1));
                    } else {
                        throw new WrongTagSyntax("A value binding (param='value') is not well structured : " + elts[i - 1]);
                    }
                }
            } else {
                // if the value contains '"' then spaces may be defined and
                // should add next elements (like param="v a l u e")
                value = new StringBuffer(str[1]); // could be optimized to avoid
                // the delete after (if char
                // at 0 is '"'
                // str[1].substring(1))
                if (str[1].lastIndexOf('"') != str[1].length() - 1) {
                    i++;

                    // while the '"' alone is not found add elements to the
                    // value
                    while (i < elts.length && (!elts[i].endsWith("\"") || elts[i].endsWith("\\\""))) {
                        value.append(' ');
                        value.append(elts[i]); // add next element to the value
                        i++; // skip next element
                    }

                    // add the element with the '"' at end
                    if (i < elts.length) {
                        value.append(' ');
                        value.append(elts[i]);
                    } else {
                        throw new WrongTagSyntax("A value binding (param=value) is not ended with \" : " + elts[i - 1]);
                    }
                }

                // remove enclosing '"' (at start and end)
                if (value.charAt(0) == '"')
                    value.deleteCharAt(0); // could be optimized look previous
                final int last = value.length() - 1;
                if (value.charAt(last) == '"')
                    value.deleteCharAt(last);
            }

            // add param/value
            map.put(param, value.toString());
        }

        // set created attribute map
        if (!ClassHelper.isEmpty(map)) {
            tag.setAttributes(map);
        }
    }

    protected boolean wasPreviouslyOpened(GdoTag<C, S> tag) {
        return false;
    }

    protected boolean hasContent(GdoTag<C, S> tag) {
        if (GdoTag.ITERATOR_TAG.equals(tag.getLabel()))
            return true;
        if (GdoTag.TOGGLE_TAG.equals(tag.getLabel()))
            return true;
        if (GdoTag.VISIBLE_TAG.equals(tag.getLabel()))
            return true;
        return false;
    }

    public void addEcho(RenderContext<C, S> renderContext, char c) {
        GdoEcho<C, S> echo = createEcho(renderContext);
        echo.appendContent(c);
    }

    public void addEcho(RenderContext<C, S> renderContext, String text, int pos, int len) {
        GdoEcho<C, S> echo = createEcho(renderContext);
        echo.setAsText(renderContext.isAsText());
        echo.appendContent(text, pos, len);
    }

    private GdoEcho<C, S> createEcho(RenderContext<C, S> renderContext) {

        // reuse existing echo tag if exists
        List<FacetsRenderer<C, S>> children = _parent.getChildren();
        if (children != null && children.size() > 0) {
            int size = children.size();
            FacetsRenderer<C, S> last = children.get(size - 1);
            if (last instanceof GdoEcho) {
                return (GdoEcho<C, S>) last;
            }
        }

        // or create new one
        GdoEcho<C, S> echo = new GdoEcho<C, S>(renderContext);
        _parent.addChild(echo);
        return echo;
    }

    /**
     * Searchs the string contents gdo tags
     */
    public boolean containsGdoTags() {
        if (_html.indexOf("<$") != -1)
            return true;
        if (_html.indexOf(LT1 + "$") != -1)
            return true;
        if (_html.indexOf(LT2 + "$") != -1)
            return true;
        return false;
    }

/**
     * Searchs until found '<' or equivalent
     * @return the number of characters before the '<'
     */
    private int skipUntilLtOrEnd() {
        int skipped = 0;
        while (_pos < _len) {
            if (isLt() > 0)
                return skipped;
            _pos++;
            skipped++;
        }
        return skipped;
    }

    private void skipSpace() {
        _pos += isSpace();
    }

/**
     * Skip '<' or equivalent
     * @return the number of characters before the '<'
     */
    private void skipLt() {
        _pos += isLt();
    }

/**
     * Skip '>' or equivalent
     * @return the number of characters before the '<'
     */
    private void skipGt() {
        _pos += isGt();
    }

    /**
     * Searchs until found white space (or equivalent) or '>' (or equivalent).
     * Calls only when gdo tag found.
     * 
     * @return the number of characters representing the tag label
     */
    private int skipUntilSpaceOrGT(char until) {
        int skipped = 0;
        while (_pos < _len) {
            if (isSpace() > 0)
                return skipped;
            if (isGt() > 0)
                return skipped;
            _pos++;
            skipped++;
        }
        return skipped;
    }

    private boolean skipUntilGtOrLt(char until1, char until2) {
        boolean inSimpleQuote = false;
        boolean inDoubleQuote = false;
        while (_pos < _len) {
            // deals with simple/double quote status
            if (!inDoubleQuote && _html.charAt(_pos) == '\'') {
                inSimpleQuote = !inSimpleQuote;
            }
            if (!inSimpleQuote && _html.charAt(_pos) == '"' && (_pos > 1 && _html.charAt(_pos - 1) != '\\')) {
                inDoubleQuote = !inDoubleQuote;
            }

            // find until characters only if not quoted
            else if (!inSimpleQuote && !inDoubleQuote) {
                if (isLt() > 0 || isGt() > 0)
                    return true;
            }
            _pos++;
        }
        return false;
    }

    // returns start position of the comment (-1 if not a comment)
    private int skipComment(RenderContext<C, S> renderContext) {
        if (_html.charAt(_pos) == '!' && _html.charAt(_pos + 1) == '-' && _html.charAt(_pos + 2) == '-') {
            int start = _pos;
            _pos += 2;
            while (_pos < _len) {
                if (_html.charAt(_pos) == '-' && _html.charAt(_pos + 1) == '-' && _html.charAt(_pos + 2) == '>') {
                    _pos += 3; // skip "-->"
                    return start;
                }
                _pos++;
            }
        }
        return -1;
    }

    private String skipUnderTag(FacetsRendererWithContent<C, S> compContent) throws WrongTagSyntax {
        String[] tags = compContent.getTags();
        int[] lengths = compContent.getTagsLength();
        int count = 0;
        int start = _pos;
        while (_pos < _len) {

            // search for opening tag
            if (isLt() > 0) {

                // escaped <
                if (_html.charAt(_pos - 1) == '\\') {
                    break;
                }

                int ltPos = _pos;
                skipLt(); // skip <
                if (_html.charAt(_pos) == '$') {
                    _pos++; // skip '$'
                    int index = startsWith(2, tags, lengths);
                    if (index == 1) {
                        count++;
                    } else if (index == 2) {

                        // found tag
                        if (count > 0)
                            count--;
                        else {
                            return _html.substring(start, ltPos);
                        }
                    }
                }
            }
            _pos++;
        }
        throw new WrongTagSyntax("Missing closing '>' for tag " + tags[0] + " in " + _html);
    }

    private int startsWith(int size, String[] starts, int[] length) {
        int number = size;
        boolean fail[] = new boolean[size];
        int start = 0;
        while (number > 0) {
            final char c = _html.charAt(_pos);
            for (int i = 0; i < size; i++) {
                if (fail[i])
                    continue;
                if (start == length[i]) {
                    if (c != ':') {
                        // find the tag skip until next >
                        while (isGt() == 0) {
                            _pos++;
                        }
                        skipGt(); // skip '>'
                        return i + 1;
                    }
                    number--;
                    fail[i] = true;
                }
                if (start < length[i]) {
                    if (c != starts[i].charAt(start)) {
                        number--;
                        fail[i] = true;
                    }
                }
            }
            _pos++;
            start++;
        }
        return 0;
    }

    /**
     * Replaces \" by " in text. TODO : Should give an example.
     * 
     * @param text
     *            tecxt transformed.
     * @return the text with escaped double quote replaced by double quote.
     */
    private String removeEscapedDoubleQuote(String text) {
        boolean inSimpleQuote = false;
        boolean inDoubleQuote = false;
        StringBuffer result = new StringBuffer(text.length());

        int pos = 0;
        int len = text.length();
        while (pos < len) {

            // deals with simple/double quote status
            if (!inDoubleQuote && text.charAt(pos) == '\'') {
                inSimpleQuote = !inSimpleQuote;
            }
            if (!inSimpleQuote && text.charAt(pos) == '"' && (pos >= 1 && text.charAt(pos - 1) != '\\')) {
                inDoubleQuote = !inDoubleQuote;
            }

            // copy character if not escaped
            if (inDoubleQuote && pos >= 1 && text.charAt(pos - 1) == '\\' && text.charAt(pos) == '"') {
                result.setCharAt(result.length() - 1, '"');
            } else {
                result.append(text.charAt(pos));
            }

            pos++;
        }
        return result.toString();
    }

    // TODO : to be optimized (avoiding doing substring)
    private int isSpace() {
        char c = _html.charAt(_pos);
        if (Character.isWhitespace(c)) {
            return 1;
        } else if (c == '%') {
            String str = _html.substring(_pos, _pos + BLANK.length());
            if (BLANK.equals(str))
                return BLANK.length();
        }
        return 0;
    }

    // TODO : to be optimized (avoiding doing substring)
    private int isLt() {
        char c = _html.charAt(_pos);
        if (c == '<') {
            return 1;
        } else if (c == '&') {
            if (_pos + LT1.length() > _len)
                return 0;
            String str = _html.substring(_pos, _pos + LT1.length());
            if (LT1.equals(str))
                return LT1.length();
        } else if (c == '%') {
            if (_pos + LT2.length() > _len)
                return 0;
            String str = _html.substring(_pos, _pos + LT2.length());
            if (LT2.equals(str))
                return LT2.length();
        }
        return 0;
    }

    // TODO : to be optimized (avoiding doing substring)
    private int isGt() {
        char c = _html.charAt(_pos);
        if (c == '>') {
            return 1;
        } else if (c == '&') {
            if (_pos + GT1.length() > _len)
                return 0;
            String str = _html.substring(_pos, _pos + GT1.length());
            if (GT1.equals(str))
                return GT1.length();
        } else if (c == '%') {
            if (_pos + GT2.length() > _len)
                return 0;
            String str = _html.substring(_pos, _pos + GT2.length());
            if (GT2.equals(str))
                return GT2.length();
        }
        return 0;
    }

    /**
     * @return <tt>true</tt> if the current cursor is at the end of the text to
     *         be expanded.
     */
    private boolean atEnd() {
        return (_pos >= _len);
    }

    protected StencilLog getLog() {
        return LOG;
    }

    public String logTrace(C stclContext, String format, Object... params) {
        return getLog().logTrace(stclContext, format, params);
    }

    public String logWarn(C stclContext, String format, Object... params) {
        return getLog().logWarn(stclContext, format, params);
    }

    public String logError(C stclContext, String format, Object... params) {
        return getLog().logError(stclContext, format, params);
    }

}
