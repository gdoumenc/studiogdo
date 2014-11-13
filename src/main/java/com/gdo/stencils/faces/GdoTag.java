/**
 * <p>...<p>
 *
 * <blockquote>
 * <p>&copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved.
 * This software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.</p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a> href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com)</a>
 */
package com.gdo.stencils.faces;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.faces.iterator.GdoFirstIterator;
import com.gdo.stencils.faces.iterator.GdoIterator;
import com.gdo.stencils.faces.iterator.GdoLastIterator;
import com.gdo.stencils.faces.iterator.GdoNotFirstIterator;
import com.gdo.stencils.faces.iterator.GdoNotLastIterator;
import com.gdo.stencils.faces.iterator.GdoNotSelectedIterator;
import com.gdo.stencils.faces.iterator.GdoSelectedIterator;
import com.gdo.stencils.faces.stencil.GdoStencil;
import com.gdo.stencils.faces.visible.GdoVisible;
import com.gdo.stencils.plug._PStencil;

public class GdoTag<C extends _StencilContext, S extends _PStencil<C, S>> {

    // tag labels
    public static final String CHILD_TAG = "child";
    public static final String ITERATOR_TAG = "iterator";
    public static final String LABEL_TAG = "label";
    public static final String PANEL_TAG = "panel";
    public static final String PATTERN_TAG = "pattern";
    public static final String STENCIL_TAG = "stencil";
    public static final String TOGGLE_TAG = "toggle";
    public static final String VISIBLE_TAG = "visible";

    // iterator sub tags
    public static final String STENCIL_SUB_TAG = "stencil";
    public static final String FIRST_SUB_TAG = "first";
    public static final String NOT_FIRST_SUB_TAG = "not_first";
    public static final String LAST_SUB_TAG = "last";
    public static final String NOT_LAST_SUB_TAG = "not_last";
    public static final String SELECTED_SUB_TAG = "selected";
    public static final String NOT_SELECTED_SUB_TAG = "not_selected";
    public static final String ELSE_SUB_TAG = "else";

    // tag parameters
    public static final String ARITY = "arity";
    public static final String AFTER_LABEL = "afterLabel";
    public static final String AFTER_STYLE = "afterStyle";
    public static final String AFTER_STYLE_CLASS = "afterStyleClass";
    public static final String BEFORE_LABEL = "beforeLabel";
    public static final String BEFORE_STYLE = "beforeStyle";
    public static final String BEFORE_STYLE_CLASS = "beforeStyleClass";
    public static final String CHECK = "check";
    public static final String CLOSE_LABEL = "closeLabel";
    public static final String CLOSE_STYLE = "closeStyle";
    public static final String CLOSE_STYLE_CLASS = "closeStyleClass";
    public static final String CLOSE_TITLE = "closeTitle";
    public static final String COLUMNS = "columns";
    public static final String COMMAND = "command";
    public static final String DEFAULT = "default";
    public static final String ESCAPE = "escape";
    public static final String EXPANDED = "expanded";
    public static final String FACET = "facet";
    public static final String FACTORY = "factory";
    public static final String FILE = "file";
    public static final String FORCED = "forced";
    public static final String IMAGE = "image";
    public static final String LABEL = "label";
    public static final String LABEL_FACET = "labelFacet";
    public static final String LABEL_MODE = "labelMode";
    public static final String LOCAL = "local";
    public static final String LOCALE = "locale";
    public static final String MODE = "mode";
    public static final String MODES = "modes";
    public static final String NAME = "name";
    public static final String OPEN_AFTER_LABEL = "openAfterLabel";
    public static final String OPEN_AFTER_STYLE = "openAfterStyle";
    public static final String OPEN_AFTER_STYLE_CLASS = "openAfterStyleClass";
    public static final String OPEN_BEFORE_LABEL = "openBeforeLabel";
    public static final String OPEN_BEFORE_STYLE = "openBeforeStyle";
    public static final String OPEN_BEFORE_STYLE_CLASS = "openBeforeStyleClass";
    public static final String OPEN_LABEL = "openLabel";
    public static final String OPEN_STYLE = "openStyle";
    public static final String OPEN_STYLE_CLASS = "openStyleClass";
    public static final String OPEN_TITLE = "openTitle";
    public static final String PANEL = "panel";
    public static final String PANEL_FACET = "panelFacet";
    public static final String PANEL_MODE = "panelMode";
    public static final String PATH = "path";
    public static final String PREFIX = "prefix";
    public static final String REPLACE = "replace";
    public static final String SELECT = "select";
    public static final String SIZE = "size";
    public static final String SUFFIX = "suffix";
    public static final String TARGET = "target";
    public static final String THREAD = "thread";
    public static final String VALUE = "value";

    protected final String _label; // tag's label
    protected final String _subTagLabel; // subtag's label
    protected Map<String, String> _attributes; // attributes map defined as
    // value binding
    // (attribute=value)
    protected final boolean _isEndTag; // true if the tag is a closing tag
    protected boolean _isClosed; // true if the tag is self closed (ends with /)
    protected FacetsRenderer<C, S> _component; // associated faces UI component

    public static boolean isValid(String label) {
        if (CHILD_TAG.equals(label))
            return true;
        if (ITERATOR_TAG.equals(label))
            return true;
        if (LABEL_TAG.equals(label))
            return true;
        if (PANEL_TAG.equals(label))
            return true;
        if (PATTERN_TAG.equals(label))
            return true;
        if (STENCIL_TAG.equals(label))
            return true;
        if (TOGGLE_TAG.equals(label))
            return true;
        if (VISIBLE_TAG.equals(label))
            return true;
        return false;
    }

    public GdoTag(String label, int len) throws WrongTagSyntax {
        String lbl = label;

        // define tag type
        _isEndTag = (lbl.charAt(0) == '/');
        _isClosed = (lbl.charAt(len - 1) == '/');

        if (_isEndTag && _isClosed) {
            throw new WrongTagSyntax("Wrong tag syntax : " + lbl);
        }

        if (_isClosed)
            lbl = lbl.substring(0, len - 1);
        if (_isEndTag)
            lbl = lbl.substring(1);

        // set tag and subtag label
        int index = lbl.indexOf(':');
        if (index == -1) {
            _label = lbl;
            _subTagLabel = null;
        } else {
            if (index == 0) {
                throw new WrongTagSyntax("Subtag with not associated tag : " + lbl);
            }
            if (index == len - 1) {
                throw new WrongTagSyntax("Subtag undefined : " + lbl);
            }
            _label = lbl.substring(0, index);
            _subTagLabel = lbl.substring(index + 1);
        }
    }

    public GdoTag(String label) throws WrongTagSyntax {
        this(label, label.length());
    }

    public final String getLabel() {
        return _label;
    }

    public final String getSubTagLabel() {
        return _subTagLabel;
    }

    public final String getCompleteTagLabel() {
        if (StringUtils.isEmpty(getSubTagLabel()))
            return getLabel();
        return getLabel() + ":" + getSubTagLabel();
    }

    public final boolean isEndTag() {
        return _isEndTag;
    }

    public final boolean isClosed() {
        return _isClosed;
    }

    public final void setClosed() {
        _isClosed = true;
    }

    public final void setAttributes(Map<String, String> attributes) {
        _attributes = attributes;
    }

    public final boolean isEndTagOf(FacetsRenderer<C, ? extends S> component) {
        if (_isEndTag) { // TODO mettre le component dans le tag pour
            // tester les sous tag.. et pas que le tag
            return true;
        }
        return false;
    }

    public final FacetsRenderer<C, S> getComponent(C stclContext, RenderContext<C, S> renderContext) throws WrongTagSyntax {
        if (_component != null)
            return _component;

        _component = createComponent(stclContext, renderContext);
        populateParameters();
        return _component;
    }

    // creates the UI component associated to the tag
    protected FacetsRenderer<C, S> createComponent(C stclContext, RenderContext<C, S> renderContext) throws WrongTagSyntax {
        RenderContext<C, S> newContext = renderContext.clone();
        String tagLabel = getLabel();
        if (CHILD_TAG.equals(tagLabel)) {
            return null;
        }
        if (ITERATOR_TAG.equals(tagLabel)) {
            if (StringUtils.isEmpty(getSubTagLabel()))
                return new GdoIterator<C, S>(newContext);
            if (FIRST_SUB_TAG.equals(getSubTagLabel()))
                return new GdoFirstIterator<C, S>(newContext);
            if (LAST_SUB_TAG.equals(getSubTagLabel()))
                return new GdoLastIterator<C, S>(newContext);
            if (NOT_FIRST_SUB_TAG.equals(getSubTagLabel()))
                return new GdoNotFirstIterator<C, S>(newContext);
            if (NOT_LAST_SUB_TAG.equals(getSubTagLabel()))
                return new GdoNotLastIterator<C, S>(newContext);
            if (SELECTED_SUB_TAG.equals(getSubTagLabel()))
                return new GdoSelectedIterator<C, S>(newContext);
            if (NOT_SELECTED_SUB_TAG.equals(getSubTagLabel()))
                return new GdoNotSelectedIterator<C, S>(newContext);
        }
        if (LABEL_TAG.equals(tagLabel)) {
            System.out.println("label tag TBD");
            return null;
        }
        if (PANEL_TAG.equals(tagLabel)) {
            System.out.println("panel tag TBD");
            return null;
        }
        if (PATTERN_TAG.equals(tagLabel)) {
            System.out.println("pattern tag TBD");
            return null;
        }
        if (STENCIL_TAG.equals(tagLabel)) {
            return new GdoStencil<C, S>(newContext);
        }
        if (VISIBLE_TAG.equals(tagLabel)) {
            return new GdoVisible<C, S>(newContext);
        }
        throw new WrongTagSyntax("Unknown tag label : " + getCompleteTagLabel());
    }

    private final void populateParameters() {
        if (_component == null)
            return;

        // set paramters list
        if (_attributes != null) {
            _component.setAttributes(_attributes);

            // set rendering parameters
            String mode = _attributes.get(GdoTag.MODE);
            if (!StringUtils.isEmpty(mode)) {
                _component.getRenderContext().setFacetMode(mode);
            }
            String facet = _attributes.get(GdoTag.FACET);
            if (!StringUtils.isEmpty(facet)) {
                _component.getRenderContext().setFacetType(facet);
            }
        }

    }

    @Override
    public String toString() {
        String str = getCompleteTagLabel();
        if (_attributes != null) {
            for (String name : _attributes.keySet()) {
                str += " " + name + "=" + _attributes.get(name);
            }
        }
        if (isEndTag())
            str += "(end)";
        return str;
    }
}
