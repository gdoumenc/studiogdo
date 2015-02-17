package com.gdo.stencils.facet;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import com.gdo.helper.ConverterHelper;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.iterator.SingleIterator;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.PathUtils;

/**
 * @author gdo
 * 
 */
public class HTML5SectionCompleter {

    protected static final String NOT = "!";
    protected static final String EQUALS = "==";
    protected static final String DIFF = "!=";
    protected static final String LESS = "<";
    protected static final String LESS_OR_EQUAL = "<=";
    protected static final String GREATER = ">";
    protected static final String GREATER_OR_EQUAL = ">=";

    protected static final String DATA_LIST = "datalist";
    protected static final String DIV = "div";
    protected static final String INPUT = "input";
    protected static final String METER = "meter";
    protected static final String OL = "ol";
    protected static final String OPTION = "option";
    protected static final String OPTGROUP = "optgroup";
    protected static final String OUTPUT = "output";
    protected static final String PROGRESS = "progress";
    protected static final String SECTION = "section";
    protected static final String SELECT = "select";
    protected static final String SPAN = "span";
    protected static final String TABLE = "table";
    protected static final String TEXTAREA = "textarea";
    protected static final String TH = "th";
    protected static final String UL = "ul";
    protected static final String LEGEND = "legend";

    protected static final String DATA_APATH_ATTRIBUTE = "data-apath";
    protected static final String DATA_PATH_ATTRIBUTE = "data-path";
    protected static final String DATA_VALUE_ATTRIBUTE = "data-value";
    protected static final String CONDITION_ATTRIBUTE = "data-cond";

    protected static final String DATA_ABSOLUTE_COMPLEMENT_PATH_ATTRIBUTE = "data-ap1";
    protected static final String DATA_COMPLEMENT_PATH_ATTRIBUTE = "data-p1";
    protected static final String DATA_ABSOLUTE_COMPLEMENT_KEY_ATTRIBUTE = "data-ak1";
    protected static final String DATA_COMPLEMENT_KEY_ATTRIBUTE = "data-k1";

    protected static final String CLASS_ATTRIBUTE = "data-class";
    protected static final String LABEL_ATTRIBUTE = "data-label";
    protected static final String MODE_ATTRIBUTE = "data-mode";
    protected static final String VALUE_PREFIX_ATTRIBUTE = "data-value-prefix";
    protected static final String VALUE_SUFFIX_ATTRIBUTE = "data-value-suffix";

    protected static final String MODE_BUTTON = "button";
    protected static final String MODE_CHECKBOX = "checkbox";
    protected static final String MODE_DATE_FROM_SQL = "dateFromSql";
    protected static final String MODE_EURO_FROM_CENT = "euroFromCent";
    protected static final String MODE_TEXT = "text";
    protected static final String MODE_HTML = "html";

    protected static final String DATA_PROP = "data-prop-";
    protected static final String DATA_PROP_PLUS = "data-prop+";
    protected static final String DATA_TEXT = "data-text-";
    protected static final String DATA_TEXT_PLUS = "data-text+";
    protected static final String DATA_HTML = "data-html-";
    protected static final String DATA_HTML_PLUS = "data-html+";
    protected static final String DATA_PROP_CSS = "data-prop-css-";
    protected static final String DATA_TEXT_CSS = "data-text-css-";
    protected static final String DATA_CSS = "data-css-";

    private HashMap<String, String> _values = new HashMap<String, String>();
    protected PStcl prop;
    protected PStcl prop_stcl;
    protected String prop_path;
    protected boolean trans_mod;

    /**
     * Retrieves a facet from a template descriptor.
     */
    public HTML5SectionCompleter() {
    }

    /**
     * Set translation mode enabled.
     */
    public void setTransMode() {
        trans_mod = true;
    }

    /**
     * Returns the empty skeleton as facet.
     * 
     * @param stclContext
     *            the stencil context.
     * @param skel
     *            the skeleton file name.
     * @return the empty skeleton.
     */
    public FacetResult getSkeleton(StclContext stclContext, String skel) {
        try {
            if (StringUtils.isBlank(skel)) {
                return new FacetResult(FacetResult.ERROR, "No HTML skeleton file defined", null);
            }

            Document doc = getSkelDocument(stclContext, skel);

            for (Element elt : doc.select("input")) {
                String valuePath = getDataValuePath(elt);
                String name = elt.attr("name");
                elt.attr("name", name + valuePath);
            }

            InputStream reader = IOUtils.toInputStream(doc.body().html());
            return new FacetResult(reader, "text/html");
        } catch (Exception e) {
            e.printStackTrace();
            return new FacetResult(FacetResult.ERROR, e.toString(), null);
        }
    }

    /**
     * Constructs facet in HTML mode.
     * 
     * @param stclContext
     *            the stencil context.
     * @param stcl
     *            the root stencil.
     * @param skel
     *            the skel file path
     * @return the completed HTML string.
     */
    public FacetResult getFacetFromSkeleton(StclContext stclContext, PStcl stcl, String skel) {
        try {
            if (StringUtils.isBlank(skel)) {
                return new FacetResult(FacetResult.ERROR, "No HTML skeleton file defined", null);
            }

            // expands HTML content
            String html = _getFacetFromSkeleton(stclContext, stcl, skel);
            InputStream reader = IOUtils.toInputStream(html);
            return new FacetResult(reader, "text/html");
        } catch (Exception e) {
            e.printStackTrace();
            return new FacetResult(FacetResult.ERROR, e.toString(), null);
        }
    }

    private String _getFacetFromSkeleton(StclContext stclContext, PStcl stcl, String skel) throws Exception {

        // gets document and parses from mode
        Document doc = getSkelDocument(stclContext, skel);

        // expands HTML content
        doc.body().attr("data-skel", skel);
        expand(stclContext, stcl, doc.body());

        return doc.body().html();
    }

    /**
     * Constructs facet in DOM5 mode.
     * 
     * @param stclContext
     *            the stencil context.
     * @param stcl
     *            the root stencil.
     * @param dom
     *            the DOM string
     * @return the completed DOM string.
     */
    public FacetResult getFacetFromDOM(StclContext stclContext, PStcl stcl, String dom) {
        try {
            if (StringUtils.isBlank(dom)) {
                return new FacetResult(FacetResult.ERROR, "No DOM defined", null);
            }

            Document doc = Jsoup.parseBodyFragment(dom);
            expand(stclContext, stcl, doc.body());

            doc.outputSettings().escapeMode(EscapeMode.xhtml);
            InputStream reader = IOUtils.toInputStream(doc.body().html());
            return new FacetResult(reader, "text/html");
        } catch (Exception e) {
            System.out.println(dom);
            e.printStackTrace();
            return new FacetResult(FacetResult.ERROR, e.toString(), null);
        }
    }

    public FacetResult getFacetFromSkeletonToText(StclContext stclContext, PStcl stcl, String skel) {
        try {
            if (StringUtils.isBlank(skel)) {
                return new FacetResult(FacetResult.ERROR, "No HTML skeleton file defined", null);
            }

            // gets document and parses from mode
            Document doc = getSkelDocument(stclContext, skel);

            // expands HTML content
            doc.body().attr("data-skel", skel);
            expand(stclContext, stcl, doc.body());
            doc.body().wrap("<pre></pre>");
            String text = doc.text();
            text = text.replaceAll("\u00A0", " ");// Converting nbsp entities

            InputStream reader = new ByteArrayInputStream(text.getBytes());
            return new FacetResult(reader, "text/html");
        } catch (Exception e) {
            e.printStackTrace();
            return new FacetResult(FacetResult.ERROR, e.toString(), null);
        }
    }

    public FacetResult getFacetFromDOMToText(StclContext stclContext, PStcl stcl, String dom) {
        try {
            if (StringUtils.isBlank(dom)) {
                return new FacetResult(FacetResult.ERROR, "No DOM defined", null);
            }

            Document doc = Jsoup.parseBodyFragment(dom);
            expand(stclContext, stcl, doc.body());
            doc.body().wrap("<pre></pre>");
            String text = doc.text();
            text = text.replaceAll("\u00A0", " ");// Converting nbsp entities

            InputStream reader = new ByteArrayInputStream(text.getBytes());
            return new FacetResult(reader, "text/html");
        } catch (Exception e) {
            System.out.println(dom);
            e.printStackTrace();
            return new FacetResult(FacetResult.ERROR, e.toString(), null);
        }
    }

    private void expand(StclContext stclContext, PStcl stcl, Element elt) {

        if ("body".equalsIgnoreCase(elt.tagName())) {
            for (Element e : elt.children()) {
                expand(stclContext, stcl, e);
            }
            return;
        }

        // does nothing on node if no path or no value or no class
        String path = getDataPath(elt);
        String valuePath = getDataValuePath(elt);
        String classPath = elt.attr(CLASS_ATTRIBUTE);
        String cond = elt.attr(CONDITION_ATTRIBUTE);
        if (StringUtils.isBlank(path) && StringUtils.isBlank(valuePath) && StringUtils.isBlank(classPath) && StringUtils.isBlank(cond)) {
            return;
        }

        // checks condition
        if (!satisfyDataCondition(stclContext, cond, stcl)) {
            elt.remove();
            return;
        }

        // sets absolute path
        String apath = "";
        if (StringUtils.isNotBlank(path) || path.equals(PathUtils.THIS) || path.startsWith("!")) {
            apath = getPwd(stclContext, stcl);
        } else {
            apath = PathUtils.compose(getPwd(stclContext, stcl), path);
        }
        setDataAPath(stclContext, elt, apath);

        // expands component
        if (OPTGROUP.equalsIgnoreCase(elt.tagName()) || DATA_LIST.equalsIgnoreCase(elt.tagName())) {
            expandOptGroupDataList(stclContext, stcl, elt);
        } else if (SELECT.equalsIgnoreCase(elt.tagName())) {
            expandSelect(stclContext, stcl, elt, path);
        } else if (OPTION.equalsIgnoreCase(elt.tagName())) {
            expandOption(stclContext, stcl, elt);
        } else if (OL.equalsIgnoreCase(elt.tagName()) || UL.equalsIgnoreCase(elt.tagName())) {
            expandOlUl(stclContext, stcl, elt, path);
        } else if (SECTION.equalsIgnoreCase(elt.tagName())) {
            expandIterContainer(stclContext, stcl, elt, path);
        } else if (TABLE.equalsIgnoreCase(elt.tagName())) {
            expandTable(stclContext, stcl, elt, path);
        } else if (isPostElement(elt)) {
            expandPostElement(stclContext, stcl, elt, path);
        } else if (isFinalElement(elt)) {
            expandFinalElement(stclContext, stcl, elt, path);
        } else if (isContainerElement(elt)) {
            expandContainerElement(stclContext, stcl, elt, path);
        }

        removePathAttribute(stclContext, elt);
    }

    private void completeChildren(StclContext stclContext, PStcl stcl, Element element) {
        for (Element child : element.children()) {
            expand(stclContext, stcl, child);
        }
    }

    private void expandContainerElement(StclContext stclContext, PStcl stcl, Element container, String path) {

        // expands attributes
        expandAttributes(stclContext, stcl, container);

        // replaces content by value if data value defined
        String valuePath = getDataValuePath(container);
        String format = getDataValueFormat(container);
        if (StringUtils.isNotBlank(valuePath)) {

            if (valuePath.startsWith("http:")) {
                valuePath = valuePath.substring("http:".length());
                String p = PathUtils.compose(getPwd(stclContext, stcl), valuePath);
                container.attr(DATA_PATH_ATTRIBUTE, p);
                container.attr(MODE_ATTRIBUTE, MODE_HTML);
                String value = "";
                expandToText(stclContext, container, value, format);
                return;
            } else if (valuePath.startsWith("html:")) {
                valuePath = valuePath.substring("html:".length());
                String p = PathUtils.compose(getPwd(stclContext, stcl), valuePath);
                container.attr(DATA_PATH_ATTRIBUTE, p);
                container.attr(MODE_ATTRIBUTE, MODE_HTML);
                String value = getPropertyValue(stclContext, stcl, valuePath);
                expandToText(stclContext, container, value, format);
                return;
            } else if (valuePath.startsWith("skel:")) {
                String skeleton = valuePath.substring("skel:".length());
                String value = "";
                try {
                    PStcl s = null;
                    if (StringUtils.isNotBlank(path)) {
                        s = stcl.getStencil(stclContext, path);
                    } else {
                        s = stcl;
                    }
                    value = _getFacetFromSkeleton(stclContext, s, skeleton);
                } catch (Exception e) {
                    value = String.format("Exception in expanding facet %s : %s", skeleton, e);
                }
                container.attr(MODE_ATTRIBUTE, MODE_HTML);
                expandToText(stclContext, container, value, format);
                return;
            } else {

                // compose with path if defined
                if (StringUtils.isNotBlank(path)) {
                    valuePath = PathUtils.compose(path, valuePath);
                }

                // sets value
                String value = getPropertyValue(stclContext, stcl, valuePath);
                String p = PathUtils.compose(getPwd(stclContext, stcl), valuePath);
                container.attr(DATA_PATH_ATTRIBUTE, p);
                expandToText(stclContext, container, value, format);
                return;
            }
        }

        // gets path condition
        if (StringUtils.isNotEmpty(path)) {
            ConditionTestResult result = evaluateConditionConstraint(stclContext, path, stcl);
            if (result.result) {
                completeChildren(stclContext, result.currentStcl, container);
            } else {
                container.remove();
            }
        }
        return;
        /*
         * boolean expandChildren = StringUtils.isNotBlank(path); boolean not =
         * expandChildren && path.startsWith(NOT); if (not) { path =
         * path.substring(1); expandChildren = StringUtils.isNotBlank(path); }
         * String condValue = ""; boolean equalCond = expandChildren &&
         * (path.indexOf("==") != -1); if (equalCond) { int index =
         * path.indexOf("=="); condValue = path.substring(0, index); path =
         * path.substring(index + 2); expandChildren = StringUtils.isNotBlank(path);
         * } boolean notEqualCond = expandChildren && (path.indexOf("!=") != -1); if
         * (notEqualCond) { int index = path.indexOf("!="); condValue =
         * path.substring(0, index); path = path.substring(index + 2);
         * expandChildren = StringUtils.isNotBlank(path); }
         * 
         * // expands children if (expandChildren) {
         * 
         * // checks other container constraints if
         * (!satisfyTemplateConstraint(stclContext, container, stcl)) {
         * container.remove(); return; }
         * 
         * // condition cases if (equalCond) { String value =
         * getPropertyValue(stclContext, stcl, path); if (condValue.equals(value)) {
         * completeChildren(stclContext, stcl, container); } else {
         * container.remove(); } return; } if (notEqualCond) { String value =
         * getPropertyValue(stclContext, stcl, path); if (!condValue.equals(value))
         * { completeChildren(stclContext, stcl, container); } else {
         * container.remove(); } return; }
         * 
         * // change current stencil PStcl currentStcl =
         * stcl.getStencil(stclContext, path);
         * 
         * // not path case if (not) {
         * 
         * // completes now on current stencil if (currentStcl.isNull()) {
         * completeChildren(stclContext, stcl, container); return; }
         * 
         * // removes content if the stencil exists container.remove(); return; }
         * 
         * // removes content if the stencil not exists if (currentStcl.isNull()) {
         * container.remove(); return; }
         * 
         * // completes now on new current stencil completeChildren(stclContext,
         * currentStcl, container); return; }
         */
    }

    private void expandFinalElement(StclContext stclContext, PStcl stcl, Element elt, String path) {

        // expands attributes
        expandAttributes(stclContext, stcl, elt);

        // expand to text value
        String valuePath = getDataValuePath(elt);
        String format = getDataValueFormat(elt);
        if (StringUtils.isNotBlank(valuePath)) {
            String value = getPropertyValue(stclContext, stcl, valuePath);
            String p = PathUtils.compose(path, valuePath);
            elt.attr(DATA_PATH_ATTRIBUTE, p);
            expandToText(stclContext, elt, value, format);
        }
    }

    private void expandIterContainer(StclContext stclContext, PStcl stcl, Element container, String path) {

        // expands attributes
        expandAttributes(stclContext, stcl, container);

        // checks path defined
        boolean hasPath = StringUtils.isNotBlank(path);
        boolean not = hasPath && path.startsWith(NOT);
        if (not) {
            path = path.substring(1);
            hasPath = StringUtils.isNotBlank(path);
        }
        if (!hasPath) {
            return;
        }

        // not path case
        StencilIterator<StclContext, PStcl> iter;
        if (path.equals(PathUtils.THIS)) {
            iter = new SingleIterator<StclContext, PStcl>(stcl);
        } else {
            iter = stcl.getStencils(stclContext, path);
        }
        if (not) {
            if (!iter.hasNext()) {
                completeChildren(stclContext, stcl, container);
                return;
            }
            container.remove();
            return;
        }

        // expands content
        if (!iter.hasNext()) {
            container.remove();
            return;
        }

        // expands content
        for (PStcl s : iter) {
            Element cont = container.clone();
            container.before(cont);
            copyAndExpandAttributes(stclContext, s, container, cont);
            setDataAPath(stclContext, cont, s.pwd(stclContext));
            completeChildren(stclContext, s, cont);
        }
        container.remove();
    }

    private void expandTable(StclContext stclContext, PStcl stcl, Element table, String path) {

        // expands attributes
        expandAttributes(stclContext, stcl, table);

        // checks path defined
        boolean hasPath = StringUtils.isNotBlank(path);
        boolean not = hasPath && path.startsWith(NOT);
        if (not) {
            path = path.substring(1);
            hasPath = StringUtils.isNotBlank(path);
        }
        if (!hasPath) {
            return;
        }

        // not path case
        StencilIterator<StclContext, PStcl> iter;
        if (path.equals(PathUtils.THIS)) {
            iter = new SingleIterator<StclContext, PStcl>(stcl);
        } else {
            iter = stcl.getStencils(stclContext, path);
        }
        if (not) {
            if (iter.hasNext()) {
                completeChildren(stclContext, stcl, table);
                return;
            }
            table.remove();
            return;
        }

        // expands content
        if (!iter.hasNext()) {
            table.remove();
            return;
        }

        // expands content
        for (PStcl s : iter) {
            Element thead = table.select("thead").first();
            if (thead != null) {
                expandToSimpleTable(stclContext, s, table);
            } else {
                completeChildren(stclContext, s, table);
                expandToPivotTable(stclContext, s, table);
            }
        }
    }

    /**
     * Adds value to imput element (selected on button).
     * 
     * @param stclContext
     *            the stencil context.
     * @param stcl
     *            the current stencil.
     * @param elt
     *            the DOM element.
     * @param path
     *            the property path.
     */
    private void expandPostElement(StclContext stclContext, PStcl stcl, Element elt, String path) {

        // expands attributes
        expandAttributes(stclContext, stcl, elt);

        // changes current stencil if needed
        if (StringUtils.isNotBlank(path)) {
            stcl = stcl.getStencil(stclContext, path);
        }

        // gets value property
        String valuePath = getDataValuePath(elt);
        String format = getDataValueFormat(elt);
        if (StringUtils.isBlank(valuePath)) {
            return;
        }
        String value = getPropertyValue(stclContext, stcl, valuePath);

        // sets value or select attribute
        String type = elt.attr("type");
        if ("textarea".equalsIgnoreCase(elt.tagName())) {
            elt.val(formatValue(stclContext, elt, value, format, null));
        } else if ("checkbox".equalsIgnoreCase(type)) {

            // compares to value defined if exists
            String v = elt.attr("value");
            if (StringUtils.isBlank(v)) {
                v = "true";
            }
            if (ConverterHelper.parseBoolean(v) && ConverterHelper.parseBoolean(value)) {
                elt.attr("checked", "checked");
            }
        } else if ("radio".equalsIgnoreCase(type)) {

            // compares to value defined if exists
            String v = elt.attr("value");
            if (value.equals(v)) {
                elt.attr("checked", "checked");
            }
        } else {
            elt.attr("value", formatValue(stclContext, elt, value, format, null));
        }

        // in any case, completes name with absolute path
        String name = elt.attr("name");
        String apath = PathUtils.compose(getPwd(stclContext, stcl), valuePath);
        elt.attr("name", name + encode(apath));
    }

    /**
     * Creates an option group or data list for all stencil in the path.
     * 
     * @param stclContext
     *            the stencil context.
     * @param stcl
     *            the current stencil.
     * @param elt
     *            the DOM element.
     * @param path
     *            the stencil path.
     */
    private void expandOptGroupDataList(StclContext stclContext, PStcl stcl, Element elt) {
        String path = getDataPath(elt);

        // checks condition
        String cond = elt.attr(CONDITION_ATTRIBUTE);
        if (!satisfyDataCondition(stclContext, cond, stcl)) {
            elt.remove();
            return;
        }

        // checks attributes
        if (StringUtils.isBlank(path)) {
            return;
        }

        /*
        // creates optgroup for each stencil
        StencilIterator<StclContext, PStcl> iter;
        if (path.equals(PathUtils.THIS)) {
            iter = new SingleIterator<StclContext, PStcl>(stcl);
        } else {
            iter = stcl.getStencils(stclContext, path);
        }
        for (PStcl s : iter) {
            Element parent = elt.parent();
            Element groupOrList = parent.appendChild(elt.clone());
            String apath = PathUtils.compose(getPwd(stclContext, s));
            setDataAPath(stclContext, groupOrList, apath);

            // if OPTGROUP has data-value then adds label
            if (OPTGROUP.equalsIgnoreCase(elt.tagName())) {
                String valuePath = getDataValuePath(elt);
                if (StringUtils.isNotBlank(valuePath)) {
                    String label = s.getString(stclContext, valuePath);
                    groupOrList.attr("label", label);
                }
            }

            // generates options from data-path and data-value
            for (Element option : groupOrList.select("options")) {
                expandOption(stclContext, s, option);
            }
        }

        // removes pattern
        elt.remove();
        */
        for (Element child : elt.children()) {

            // checks condition
            cond = elt.attr(CONDITION_ATTRIBUTE);
            if (!satisfyDataCondition(stclContext, cond, stcl)) {
                child.remove();
                continue;
            }

            // expands child
            if (OPTGROUP.equalsIgnoreCase(child.tagName())) {
                expandOptGroupDataList(stclContext, stcl, child);
            } else if (OPTION.equalsIgnoreCase(child.tagName())) {
                expandOption(stclContext, stcl, child);
            }
        }
    }

    private void expandOption(StclContext stclContext, PStcl stcl, Element option) {
        String path = getDataPath(option);
        String valuePath = getDataValuePath(option);
        String labelPath = option.attr(LABEL_ATTRIBUTE);

        // checks condition
        String cond = option.attr(CONDITION_ATTRIBUTE);
        if (!satisfyDataCondition(stclContext, cond, stcl)) {
            option.remove();
            return;
        }

        // the options are not generated from the stencil
        if (StringUtils.isBlank(path)) {

            // if value path defined, then changes label and value
            if (StringUtils.isNotBlank(valuePath)) {
                completeOption(stclContext, stcl, valuePath, labelPath, option);
            }
        }

        // the options are generated
        else {
            Element last = option; // for insertion order

            // iterates over data-path of the option
            StencilIterator<StclContext, PStcl> iter;
            if (path.equals(PathUtils.THIS)) {
                iter = new SingleIterator<StclContext, PStcl>(stcl);
            } else {
                iter = stcl.getStencils(stclContext, path);
            }
            for (PStcl s : iter) {
                Element opt = option.clone();
                opt.removeAttr(LABEL_ATTRIBUTE);
                opt.removeAttr(DATA_VALUE_ATTRIBUTE);
                completeOption(stclContext, s, valuePath, labelPath, opt);
                last.after(opt);
                last = opt;
            }

            // removes option template
            option.remove();
        }
    }

    private void completeOption(StclContext stclContext, PStcl stcl, String valuePath, String labelPath, Element option) {

        // expands attributes
        expandAttributes(stclContext, stcl, option);

        // sets label
        if (StringUtils.isBlank(labelPath)) {
            labelPath = valuePath;
        }
        String label = getPropertyValue(stclContext, stcl, labelPath);
        option.appendText(addBlockTrans(label));

        // sets path value
        String apath = getPwd(stclContext, stcl);
        setDataAPath(stclContext, option, apath);

        // sets value
        String value = getPropertyValue(stclContext, stcl, valuePath);
        option.attr("value", addBlockTrans(value));
    }

    /**
     * Sets selected value for a select or expands with all options.
     * 
     * @param stclContext
     *            the stencil context.
     * @param stcl
     *            the current stencil.
     * @param elt
     *            the DOM element.
     * @param path
     *            the stencil path.
     */
    private void expandSelect(StclContext stclContext, PStcl stcl, Element elt, String path) {
        if (StringUtils.isBlank(path)) {
            elt.appendText("select must have a data-path (generally '.')");
            return;
        }

        // expands attributes
        expandAttributes(stclContext, stcl, elt);

        // replaces by generated options
        for (Element child : elt.children()) {

            // checks condition
            String cond = elt.attr(CONDITION_ATTRIBUTE);
            if (!satisfyDataCondition(stclContext, cond, stcl)) {
                child.remove();
                continue;
            }

            // expands child
            if (OPTGROUP.equalsIgnoreCase(child.tagName())) {
                expandOptGroupDataList(stclContext, stcl, child);
            } else if (OPTION.equalsIgnoreCase(child.tagName())) {
                expandOption(stclContext, stcl, child);
            }
        }

        // the selected stencil is a property
        String valuePath = getDataValuePath(elt);
        String labelPath = elt.attr(LABEL_ATTRIBUTE);
        boolean property = (StringUtils.isBlank(valuePath) && StringUtils.isBlank(labelPath)) || PathUtils.THIS.equals(valuePath) || PathUtils.THIS.equals(labelPath);
        if (property) {
            if (!PathUtils.THIS.equals(path)) {
                valuePath = path;
            }
            path = PathUtils.THIS;
        }

        // changes current stencil if not simple property
        PStcl currentStcl = stcl.getStencil(stclContext, path);

        // completes name if serves as input (may be used only for list)
        String name = elt.attr("name");

        // as the current stencil may be null, composed path should be used
        if (StringUtils.isNotBlank(name)) {
            String apath = "";
            if (!property) {
                apath = PathUtils.compose(getPwd(stclContext, stcl), path);
            } else {
                apath = PathUtils.compose(getPwd(stclContext, stcl), valuePath);
            }

            // set name and path attribute (path is reset)
            setDataAPath(stclContext, elt, apath);
            elt.attr("name", name + encode(apath));
        }

        // sets selected one

        // compares by label (if data-label defined)
        if (StringUtils.isNotBlank(labelPath)) {
            if (PathUtils.THIS.equals(labelPath)) {
                labelPath = valuePath;
            }
            String value = getPropertyValue(stclContext, currentStcl, labelPath);
            value = addBlockTrans(value);
            for (Element option : elt.select(OPTION)) {
                if (StringUtils.isNotBlank(value) && value.equals(option.text())) {
                    option.attr("selected", "selected");
                    break;
                }
            }
        }

        // compares by value (multiple selection available in this case)
        else {
            if (StringUtils.isBlank(valuePath)) {
                valuePath = PathUtils.THIS;
            }
            String value = getPropertyValue(stclContext, currentStcl, valuePath);

            // multiple selection
            if (name.startsWith("m_")) {
                String sep = currentStcl.getMultiPostSep(stclContext);
                for (String v : value.split(sep)) {
                    for (Element option : elt.select(OPTION)) {
                        if (StringUtils.isNotBlank(value) && v.equals(option.attr("value"))) {
                            option.attr("selected", "selected");
                        }
                    }
                }
            }

            // single selection
            else {
                for (Element opt : elt.select(OPTION)) {
                    if (StringUtils.isNotBlank(value) && value.equals(opt.attr("value"))) {
                        opt.attr("selected", "selected");
                        break;
                    }
                }
            }
        }
    }

    private void expandOlUl(StclContext stclContext, PStcl stcl, Element elt, String path) {

        // checks attribute
        if (StringUtils.isBlank(path)) {
            elt.appendText("OL or UL must have a data-path");
            return;
        }

        // expands all items
        for (Element li : elt.children()) {

            // only take first li elements
            if (!li.tagName().equalsIgnoreCase("li"))
                continue;

            // checks condition
            String cond = li.attr(CONDITION_ATTRIBUTE);

            // expands li items
            if (PathUtils.THIS.equals(path)) {
                if (satisfyDataCondition(stclContext, cond, stcl)) {
                    // creates li element
                    String apath = getPwd(stclContext, stcl);
                    setDataAPath(stclContext, li, apath);
                    Element i = li.clone();
                    li.parent().appendChild(i);

                    // if the li template is empty adds value
                    if (li.children().size() == 0) {
                        Element span = i.appendElement("span");
                        String valuePath = getDataValuePath(li);
                        String value = getPropertyValue(stclContext, stcl, valuePath);
                        if (StringUtils.isNotBlank(valuePath)) {
                            span.appendText(value);
                        }
                    }

                    // expands li template content
                    else {
                        for (Element c : i.children()) {
                            expand(stclContext, stcl, c);
                        }
                    }

                    expandAttributes(stclContext, stcl, i);
                }

            }
            else {
                for (PStcl s : stcl.getStencils(stclContext, path)) {

                    if (satisfyDataCondition(stclContext, cond, s)) {
                        // creates li element
                        String apath = getPwd(stclContext, s);
                        setDataAPath(stclContext, li, apath);
                        Element i = li.clone();
                        li.parent().appendChild(i);

                        // if the li template is empty adds value
                        if (li.children().size() == 0) {
                            Element span = i.appendElement("span");
                            String valuePath = getDataValuePath(li);
                            String value = getPropertyValue(stclContext, s, valuePath);
                            if (StringUtils.isNotBlank(valuePath)) {
                                span.appendText(value);
                            }
                        }

                        // expands li template content
                        else {
                            for (Element c : i.children()) {
                                expand(stclContext, s, c);
                            }
                        }

                        expandAttributes(stclContext, s, i);
                    }
                }
            }

            // recursive expansion
            /*
             * for (String tag : new String[] { "ol", "ul" }) { List<Element> ols =
             * getElementChildren(li, tag); for (Element ol : ols) { String p =
             * ol.attr(PATH_ATTRIBUTE); if (StringUtils.isNotBlank(p)) { Element
             * subList = l.appendElement(tag); List<Element> olul =
             * expandOlUl(stclContext, s, ol, p); for (Element e : olul) {
             * subList.appendChild(e); } } } }
             */

            // removes li template
            li.remove();
        }
    }

    private void expandToSimpleTable(StclContext stclContext, PStcl stcl, Element table) {

        // the last tr in thead serves as a template for each row
        Element thead = table.select("thead").first();
        Elements trs = thead.select("tr");
        if (trs.size() == 0) {
            completeChildren(stclContext, stcl, table);
            return;
        }

        // last tr in header must have a path attribute
        Element thr = trs.last();
        String p = getDataPath(thr);
        if (StringUtils.isBlank(p)) {
            completeChildren(stclContext, stcl, table);
            return;
        }

        // completes others elements in table
        for (Element elt : table.children()) {
            if (!elt.tagName().equals("thead") && !elt.tagName().equals("tbody")) {
                expand(stclContext, stcl, elt);
            }
        }

        // tr in body may serve as template
        Elements thh = thr.select("th");
        Element tbody = table.select("tbody").first();
        Element tbr = (tbody != null) ? tbody.select("tr").first() : null;
        Elements tbd = (tbr != null) ? tbr.select("td") : null;

        // creates tbody if doesn(t exists
        if (tbody == null) {
            tbody = table.appendElement("tbody");
        }

        // creates a row for each stencils
        StencilIterator<StclContext, PStcl> iter;
        if (p.equals(PathUtils.THIS)) {
            iter = new SingleIterator<StclContext, PStcl>(stcl);
        } else {
            iter = stcl.getStencils(stclContext, p);
        }
        for (PStcl s : iter) {
            String pwd = getPwd(stclContext, s);
            Element ntr = tbody.appendElement("tr");
            if (tbr != null) {
                copyAndExpandAttributes(stclContext, s, tbr, ntr);
            } else {
                copyAndExpandAttributes(stclContext, s, thr, ntr);
            }
            setDataAPath(stclContext, ntr, pwd);

            // for each columns declared in the header
            int tbdIndex = 0;
            for (Element th : thh) {
                String valuePath = getDataValuePath(th);
                String format = getDataValueFormat(th);
                String labelPath = th.attr(LABEL_ATTRIBUTE);
                Element td;

                // creates td from template
                if (tdFromHeader(th)) {
                    td = ntr.appendElement("td");

                    // adds attributes and data class
                    copyAndExpandAttributes(stclContext, s, th, td);
                    String classPath = th.attr(CLASS_ATTRIBUTE);
                    addClassToElement(stclContext, s, td, classPath);

                    // expands container associated for each row (only if
                    // data-path
                    // defined)
                    // others children (note relative to path) stay in header
                    for (Element f : th.select("*[data-path]")) {
                        if (isContainerElement(f)) {
                            Element ff = f.clone();
                            td.appendChild(ff);
                            expand(stclContext, s, ff);
                        }
                    }
                }

                // td are defined in body (no content from stencil)
                else {
                    if (tbd != null && tbd.size() > tbdIndex) {
                        td = tbd.get(tbdIndex).clone();
                        tbdIndex++;
                        valuePath = getDataValuePath(td);
                        labelPath = td.attr(LABEL_ATTRIBUTE); // should not be
                                                              // used
                        // expandAttributes(stclContext, s, td);
                        ntr.appendChild(td);
                    } else {
                        td = ntr.appendElement("TD");
                    }
                }

                // adds cell content
                if (tdFromHeader(th)) {

                    // uses label for content (value can also be used)
                    if (StringUtils.isBlank(labelPath)) {
                        labelPath = valuePath;
                    }
                    if (StringUtils.isNotBlank(labelPath) && !labelPath.startsWith("!")) {
                        String value = getPropertyValue(stclContext, s, labelPath);
                        expandToText(stclContext, td, value, format);
                    }
                } else {
                    String path = getDataPath(td);
                    if (StringUtils.isBlank(path)) {
                        path = ".";
                    }
                    expandContainerElement(stclContext, s, td, path);
                }

                // add apath
                String apath = PathUtils.compose(pwd, valuePath);
                setDataAPath(stclContext, td, apath);
            }
        }

        // suppresses
        // cohttp://jsoup.org/apidocs/org/jsoup/nodes/Element.html#appendElement(java.lang.String)ntainer
        // with data-path from header (div, form)
        // (edit form should be first element)
        for (Element th : thh) {
            if (tdFromHeader(th)) {
                for (Element f : th.select("*[data-path]")) {
                    if (isContainerElement(f)) {
                        f.remove();
                    }
                }
            }
            completeChildren(stclContext, stcl, th);
        }

        // removes initial tbody content
        if (tbr != null) {
            tbr.remove();
        }

        // expands the tfoot if exists
        // Element tfoot = table.select("tfoot").first();
        // if (tfoot != null) {
        // completeChildren(stclContext, stcl, tfoot);
        // }

    }

    private Element expandToPivotTable(StclContext stclContext, PStcl stcl, Element table) {
        List<Element> trs = table.select("tr");

        // gets all attributes needed to expland
        Element htr = trs.get(0);
        Element btr = trs.get(1);
        Element hth = htr.select("th").first();
        Element bth = btr.select("th").first();
        Element btd = btr.select("td").first();
        String hp = getDataPath(hth);
        String hv = getDataValuePath(hth);
        String bhp = getDataPath(bth);
        String bhv = getDataValuePath(bth);
        String bdv = getDataValuePath(btd);
        String bdc = btd.attr(CLASS_ATTRIBUTE);

        // removestable description
        for (Element body : table.select("tbody")) {
            body.remove();
        }

        // removeElementChildren(table, "tr");

        // will contains all rows elements (row header as key)
        Map<String, Element> rows = new LinkedHashMap<String, Element>();

        // sets theader and creates empty rows
        String pwd = getPwd(stclContext, stcl);
        Element thead = table.appendElement("thead");
        Element thtr = thead.appendElement("tr");
        setDataAPath(stclContext, thtr, pwd);

        copyAndExpandAttributes(stclContext, stcl, thtr, htr);
        Element tbody = table.appendElement("tbody");
        int columnNumber = 0;

        // adds cell at (0,0)
        thtr.appendElement("th");

        // iteration on first index
        for (PStcl s : stcl.getStencils(stclContext, hp)) {

            // adds header column
            Element thth = thtr.appendElement("th");
            copyAndExpandAttributes(stclContext, s, hth, thth);
            String apath = PathUtils.compose(getPwd(stclContext, s), hv);
            setDataAPath(stclContext, thth, apath);

            String value = getPropertyValue(stclContext, s, hv);
            Element span = thth.appendElement("span");
            span.appendText(value);
            boolean columnHasRows = false;

            // initializes rows array
            for (PStcl ss : s.getStencils(stclContext, bhp)) {
                String key = ss.getString(stclContext, bhv);
                Element tbtr = null;
                Element tbtd = null;

                // gets already created rows
                tbtr = rows.get(key);

                // creates empty rows
                if (tbtr == null) {
                    tbtr = tbody.appendElement("tr");
                    copyAndExpandAttributes(stclContext, s, btr, tbtr);
                    rows.put(key, tbtr);
                    tbtd = tbtr.appendElement("th");
                    copyAndExpandAttributes(stclContext, s, bth, tbtd);
                    if (StringUtils.isNotBlank(bhv)) {
                        String val = getPropertyValue(stclContext, ss, bhv);
                        span = tbtd.appendElement("span");
                        span.appendText(val);
                        String ap = PathUtils.compose(getPwd(stclContext, ss), bhv);
                        setDataAPath(stclContext, tbtd, ap);
                    } else {
                        String ap = getPwd(stclContext, ss);
                        setDataAPath(stclContext, tbtd, ap);
                    }

                    // adds missing cells
                    for (int i = 0; i < columnNumber; i++) {
                        tbtd = tbtr.appendElement("td");
                        addClassToElement(stclContext, ss, tbtd, bdc);
                    }
                }

                // add cells
                tbtd = tbtr.appendElement("td");
                columnHasRows = true;
                if (StringUtils.isNotBlank(bdv)) {
                    String val = getPropertyValue(stclContext, ss, bdv);
                    span = tbtd.appendElement("span");
                    span.appendText(val);
                    String ap = PathUtils.compose(getPwd(stclContext, ss), bdv);
                    setDataAPath(stclContext, tbtd, ap);
                } else {
                    for (Node child : btd.childNodes()) {
                        tbtd.appendChild(child.clone());
                        completeChildren(stclContext, ss, tbtd);
                    }
                    String ap = getPwd(stclContext, ss);
                    setDataAPath(stclContext, tbtd, ap);
                }

                // expands form associated for edition (only if data-path
                // defined)
                for (Element f : btd.select("form")) {
                    if (StringUtils.isNotBlank(getDataPath(f))) {
                        Element ff = f.clone();
                        expand(stclContext, s, ff);
                        tbtd.appendChild(ff);
                    }
                }

                // adds calculated class
                addClassToElement(stclContext, ss, tbtd, bdc);
            }

            // if the stencil doesn't exist, adds empty cells for the column
            if (!columnHasRows) {
                for (Element row : rows.values()) {
                    row.appendElement("td");
                }
            }
            columnNumber++;
        }

        rows.clear();
        return table;
    }

    private void expandAttributes(StclContext stclContext, PStcl stcl, Element elt) {
        String classPath = elt.attr(CLASS_ATTRIBUTE);
        addClassToElement(stclContext, stcl, elt, classPath);

        for (Attribute attr : elt.attributes()) {
            String key = attr.getKey();

            if (key.startsWith(DATA_TEXT_CSS)) {
                String name = key.substring(DATA_TEXT_CSS.length());
                String value = stcl.getDOM5TextFacet(stclContext, attr.getValue());
                if (StringUtils.isNotBlank(value)) {
                    if (StringUtils.isNotBlank(value)) {
                        String attribute = elt.attr("style");
                        elt.attr("style", String.format("%s %s:%s;", attribute, name, value));
                    }
                }
                elt.removeAttr(key);
            }
            else if (key.startsWith(DATA_PROP_CSS) || key.startsWith(DATA_CSS)) {
                String suffix = "";
                String valueName = attr.getValue();
                int indexOf = valueName.indexOf("|");
                /* check for suffix (SHOULD NOT BE USED ANY MORE AS DATA_TEXT_CSS can be used) */
                if (indexOf > 0) {
                    suffix = valueName.substring(indexOf + 1);
                    valueName = valueName.substring(0, indexOf);
                }
                String value = getPropertyValue(stclContext, stcl, valueName);
                if (StringUtils.isNotBlank(value)) {
                    String property = key.substring(DATA_CSS.length());
                    String attribute = elt.attr("style");
                    elt.attr("style", String.format("%s %s:%s%s;", attribute, property, value, suffix));
                }
                elt.removeAttr(key);
            }
            else if (key.startsWith(DATA_PROP)) {
                String name = key.substring(DATA_PROP.length());
                String value = getPropertyValue(stclContext, stcl, attr.getValue());
                if (StringUtils.isNotBlank(value)) {
                    elt.attr(name, value);
                } else {
                    elt.removeAttr(name);
                }
                elt.removeAttr(key);
            }
            else if (key.startsWith(DATA_PROP_PLUS)) {
                String value = getPropertyValue(stclContext, stcl, attr.getValue());
                if (StringUtils.isNotBlank(value)) {
                    String name = key.substring(DATA_PROP_PLUS.length());
                    elt.attr(name, elt.attr(name) + " " + value);
                }
                elt.removeAttr(key);
            }
            else if (key.startsWith(DATA_TEXT)) {
                String name = key.substring(DATA_TEXT.length());
                String value = stcl.getDOM5TextFacet(stclContext, attr.getValue());
                if (StringUtils.isNotBlank(value)) {
                    elt.attr(name, value);
                } else {
                    elt.removeAttr(name);
                }
                elt.removeAttr(key);
            }
            else if (key.startsWith(DATA_TEXT_PLUS)) {
                String value = stcl.getDOM5TextFacet(stclContext, attr.getValue());
                if (StringUtils.isNotBlank(value)) {
                    String name = key.substring(DATA_TEXT_PLUS.length());
                    elt.attr(name, elt.attr(name) + " " + value);
                }
                elt.removeAttr(key);
            }
            else if (key.startsWith(DATA_HTML)) {
                String name = key.substring(DATA_HTML.length());
                String value = stcl.getDOM5Facet(stclContext, attr.getValue());
                if (StringUtils.isNotBlank(value)) {
                    elt.attr(name, value);
                } else {
                    elt.removeAttr(name);
                }
                elt.removeAttr(key);
            }
            else if (key.startsWith(DATA_HTML_PLUS)) {
                String value = stcl.getDOM5Facet(stclContext, attr.getValue());
                if (StringUtils.isNotBlank(value)) {
                    String name = key.substring(DATA_HTML_PLUS.length());
                    elt.attr(name, elt.attr(name) + " " + value);
                }
                elt.removeAttr(key);
            }
        }

        elt.removeAttr("data-path");
    }

    private void addClassToElement(StclContext stclContext, PStcl stcl, Element elt, String classAttributes) {

        // add class attributes
        if (StringUtils.isNotBlank(classAttributes)) {
            StringBuffer clazz = null;
            for (String m : PathUtils.split(classAttributes)) {
                boolean slot = false;
                boolean not = false;
                if (m.endsWith("@")) {
                    m = m.substring(0, m.length() - 1);
                    slot = true;
                }
                if (m.endsWith("!")) {
                    m = m.substring(0, m.length() - 1);
                    not = true;
                }
                String c = getPropertyValue(stclContext, stcl, m);
                if ((slot && ConverterHelper.parseBoolean(c)) || (not && StringUtils.isBlank(c))) {
                    c = m;
                }
                if (clazz == null) {
                    clazz = new StringBuffer(c);
                } else {
                    clazz.append(" ").append(c);
                }
            }
            elt.attr("class", clazz.append(" ").append(elt.attr("class")).toString());
        }
    }

    /**
     * Expands the text element with text value (may be in html mode).
     * 
     * @param stclContext
     *            the stencil context.
     * @param container
     *            the container element (where attributes are defined).
     * @param text
     *            the text element.
     * @param value
     *            the property value.
     */
    private void expandToText(StclContext stclContext, Element container, String value, String format) {
        String mode = container.attr(MODE_ATTRIBUTE);
        String path = getDataPath(container);

        // button mode
        if (mode.startsWith(MODE_BUTTON)) {
            Element button = container.appendElement("button");
            button.appendText(value);
            return;
        }

        // sets value as checkbox
        if (MODE_CHECKBOX.equalsIgnoreCase(mode)) {

            // true value
            Element input = container.appendElement("input");
            input.attr("type", "checkbox");
            input.attr("value", "true");
            input.attr("onclick", "nextSiblingByTagName(event.target, \"INPUT\").setAttribute(\"checked\", (event.target.checked)?\"checked\":\"\")");
            if (ConverterHelper.parseBoolean(value)) {
                input.attr("checked", "checked");
            }
            // String path = getPathAttribute(container);
            setDataAPath(stclContext, input, path);
            input.attr("name", "b_" + encode(path));

            // false value
            input = container.appendElement("input");
            input.attr("type", "checkbox");
            input.attr("value", "false");
            input.attr("class", "hidden");
            if (ConverterHelper.parseBoolean(value)) {
                input.attr("checked", "checked");
            }
            setDataAPath(stclContext, input, path);
            input.attr("name", "b_" + encode(path));

            return;
        }

        // injects HTML directly
        if (MODE_HTML.equalsIgnoreCase(mode)) {
            Document doc = Jsoup.parseBodyFragment(value);
            Element body = doc.body();
            for (Element elt : body.children()) {
                container.appendChild(elt);
            }
            if (body.hasText()) {
                container.appendText(body.text());
            }
            return;
        }

        Element span = container.appendElement("span");
        String val = formatValue(stclContext, container, value, format, span);
        if (val != null) {
            span.appendText(val);
        }
        setDataAPath(stclContext, span, path);
    }

    /**
     * New format syntax : path = path%format
     * 
     * @throws ParseException
     */
    private String formattedValue(StclContext stclContext, Element elt, String value, String format, Element span) throws ParseException {
        if (StringUtils.isNotEmpty(format)) {
            if (format.startsWith("i")) {
                return formatIntegerValue(stclContext, value, format.substring(1));
            }
            if (format.startsWith("s")) {
                String v = formatStringValue(stclContext, value, format.substring(1), span);
                return addBlockTrans(v);
            }
            if (format.startsWith("dt")) {
                return formatDateTimeValue(stclContext, value, format.substring(3));
            }
        }
        return null;
    }

    private String formatIntegerValue(StclContext stclContext, String value, String format) {
        Locale locale = Locale.FRENCH;
        try {
            if (StringUtils.isBlank(format) || format.length() == 0)
                return value;

            // empty value
            if (format.endsWith("?")) {
                if (StringUtils.isBlank(value))
                    return "";
                format = format.substring(0, format.length() - 1);
            }

            // suffix
            if (format.endsWith(" €")) {
                return formatIntegerValue(stclContext, value, format.substring(0, format.length() - 2)) + " €";
            }
            if (format.endsWith("€")) {
                return formatIntegerValue(stclContext, value, format.substring(0, format.length() - 1)) + "€";
            }
            if (format.endsWith(" %")) {
                return formatIntegerValue(stclContext, value, format.substring(0, format.length() - 2)) + " %";
            }
            if (format.endsWith("%")) {
                return formatIntegerValue(stclContext, value, format.substring(0, format.length() - 1)) + "%";
            }

            // decimal format
            if (format.equals(",##")) {
                return String.format(locale, "%.2f", Float.parseFloat(value));
            }
            if (format.equals("/100,##")) {
                return String.format(locale, "%.2f", Float.parseFloat(value) / 100);
            }
            if (format.equals("/-100,##")) {
                return String.format(locale, "%.2f", -Float.parseFloat(value) / 100);
            }
            if (format.equals("/1000,##")) {
                return String.format(locale, "%.2f", Float.parseFloat(value) / 1000);
            }
            if (format.equals("/-1000,##")) {
                return String.format(locale, "%.2f", -Float.parseFloat(value) / 1000);
            }
            if (format.equals("/10000,##")) {
                return String.format(locale, "%.2f", Float.parseFloat(value) / 10000);
            }
            if (format.equals("/-10000,##")) {
                return String.format(locale, "%.2f", -Float.parseFloat(value) / 10000);
            }

            return String.format(locale, "%d", Integer.parseInt(value));
        } catch (Exception e) {
            return "";
        }
    }

    private String formatStringValue(StclContext stclContext, String value, String format, Element span) {
        if (StringUtils.isBlank(value) || StringUtils.isBlank(format) || format.length() == 0)
            return value;

        if (format.equals("w")) {
            for (String s : value.split("\n")) {
                span.appendText(addBlockTrans(s));
                Element parent = span.parent();
                parent.appendElement("br");
                span = parent.appendElement("span");
            }
            return "";
        }

        return value;
    }

    private String formatDateTimeValue(StclContext stclContext, String value, String format) throws ParseException {
        if (StringUtils.isBlank(value) || StringUtils.isBlank(format) || format.length() == 0)
            return value;

        if ("dd/MM/yyyy".equals(format)) {
            DateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sqlDateFormat.parse(value);
            DateFormat dateFormat = new SimpleDateFormat(format);
            return dateFormat.format(date);
        }

        if ("dd/MM/yyyy HH:mm".equals(format)) {
            DateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date date = sqlDateFormat.parse(value);
            DateFormat dateFormat = new SimpleDateFormat(format);
            return dateFormat.format(date);
        }

        if ("utc_dd/MM/yyyy".equals(format)) {
            format = format.substring(4);
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = dateFormat.parse(value);
            dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
            return dateFormat.format(date);
        }

        if ("utc_dd/MM/yyyy HH:mm".equals(format)) {
            format = format.substring(4);
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = dateFormat.parse(value);
            dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
            return dateFormat.format(date);
        }
        return value;
    }

    private String formatValue(StclContext stclContext, Element container, String value, String format, Element span) {
        try {

            // new format syntax
            String formatted = formattedValue(stclContext, container, value, format, span);
            if (formatted != null)
                return formatted;

            // format from name attribute
            String name = container.attr("name");
            if (StringUtils.isBlank(name) || name.indexOf('_') < 0)
                return addBlockTrans(value);
            format = name.substring(0, name.lastIndexOf('_'));
            formatted = formattedValue(stclContext, container, value, name, span);
            if (formatted != null)
                return formatted;

            // no format found
            return addBlockTrans(value);

        } catch (Exception e) {
            return e.toString();
        }
    }

    private String addBlockTrans(String value) {
        if (trans_mod && StringUtils.isNotBlank(value)) {
            value = String.format("{%% blocktrans %%}%s{%% endblocktrans %%}", value);
        }
        return value;
    }

    /**
     * Returns the value of the property defined for the slot path of the
     * section.
     * 
     * @param stclContext
     *            the stencil context.
     * @param stcl
     *            the stencil rendered.
     * @param propertyPath
     *            property path to render.
     * @return the value of the property defined for the slot path of the
     *         section.
     */
    protected String getPropertyValue(StclContext stclContext, PStcl stcl, String propertyPath) {
        try {

            if (StringUtils.isBlank(propertyPath)) {
                return "the property path should not be null";
            }

            // accepts no stencil
            if (stcl.isNull()) {
                return "";
            }

            // specific property stencil case
            if (".".equals(propertyPath)) {
                return stcl.getString(stclContext, propertyPath);
            }

            // simple property path
            PStcl s = stcl;
            String save_index = s.getId(stclContext) + propertyPath;

            // checks value not already read
            String value = null; // _values.get(save_index);
            if (value == null) {
                if (PathUtils.isComposed(propertyPath)) {
                    s = stcl.getStencil(stclContext, PathUtils.getPathName(propertyPath));
                    if (s.isNull()) {
                        return "";
                    }
                    propertyPath = PathUtils.getLastName(propertyPath);
                }
                try {
                    value = s.getString(stclContext, propertyPath);
                } catch (IllegalStateException e) {
                    if (stclContext.getRpcArgs().acceptNoStencil())
                        value = "";
                    else
                        value = e.getMessage();
                }
                if (propertyPath.endsWith(PathUtils.ABSOLUTE_PATH)) {
                    value = encode(value);
                }

                // stores values for next use
                // _values.put(save_index, value);
            }
            return value;

        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Ads prefix after name attribute in element and in all its children.
     * 
     * @param element
     *            the element where renaming is done.
     * @param name
     *            the new suffix name.
     */
    /*
     * private void renameAllNames(Element element, String prefix) { List<Element>
     * list = element.getChildren(); for (Element elt : list) { String n =
     * elt.attr("name"); if (StringUtils.isNotBlank(n)) { elt.attr("name", n +
     * prefix); } renameAllNames(elt, prefix); } }
     */

    /**
     * Checks if a DOM element is a containing block.
     * 
     * @param element
     *            the DOM element
     * @return <tt>true</tt> if the DOM is a block element, <tt>false</tt>
     *         otherwise.
     */
    protected boolean isPostElement(Element element) {
        String name = element.tagName();
        if ("input".equalsIgnoreCase(name) || "textarea".equalsIgnoreCase(name) || "output".equalsIgnoreCase(name)) {
            return true;
        }
        return false;
    }

    protected boolean isFinalElement(Element element) {
        String name = element.tagName();
        if ("figcaption".equalsIgnoreCase(name) || "img".equalsIgnoreCase(name)) {
            return true;
        }
        if ("meter".equalsIgnoreCase(name) || "pre".equalsIgnoreCase(name) || "progress".equalsIgnoreCase(name)) {
            return true;
        }
        return false;
    }

    protected boolean isContainerElement(Element element) {
        String name = element.tagName().toLowerCase();
        if ("a".equals(name) || "abbr".equals(name) || "address".equals(name) || "article".equals(name) || "aside".equals(name)) {
            return true;
        }
        if ("button".equals(name) || "blockquote".equals(name) || "caption".equals(name)) {
            return true;
        }
        if ("div".equals(name) || "fieldset".equals(name) || "figcaption".equals(name) || "footer".equals(name) || "form".equals(name) || "legend".equals(name)) {
            return true;
        }
        if ("h1".equals(name) || "h2".equals(name) || "h3".equals(name) || "h4".equals(name) || "h5".equals(name) || "h6".equals(name)) {
            return true;
        }
        if ("header".equals(name) || "hgroup".equals(name) || "label".equals(name) || "nav".equals(name) || "p".equals(name)) {
            return true;
        }
        if ("pre".equals(name) || "span".equals(name) || "section".equals(name)) {
            return true;
        }
        if ("tbody".equals(name) || "tr".equals(name) || "td".equals(name) || "thead".equals(name) || "tfoot".equals(name)) {
            return true;
        }
        return false;
    }

    private void copyAndExpandAttributes(StclContext stclContext, PStcl stcl, Element src, Element dest) {

        // copies all attributes
        for (Attribute attr : src.attributes()) {
            String key = attr.getKey();
            if (!key.equals("data-path")) {
                dest.attr(key, attr.getValue());
            }
        }

        // expands data attributes
        expandAttributes(stclContext, stcl, dest);
    }

    private Document getSkelDocument(StclContext stclContext, String skel) throws URISyntaxException, MalformedURLException, IOException {

        // skel file on http
        URI uri = new URI(skel);
        if ("http".equals(uri.getScheme())) {
            return Jsoup.parse(uri.toURL(), 1000);
        }

        // skel file on local file system
        String skelFilename = skel;
        if (skel.startsWith("file:")) {
            File skelFile = new File(uri);
            return Jsoup.parse(skelFile, "utf-8");
        }

        // skel file as relative paths
        String skelDir = stclContext.getConfigParameter(StclContext.PROJECT_SKEL_DIR);
        skelFilename = PathUtils.compose(skelDir, skel);
        try {
            uri = new URI(skelFilename);
            if ("http".equals(uri.getScheme())) {
                return Jsoup.parse(uri.toURL(), 1000);
            }
        } catch (Exception e) {
        }
        File skelFile = new File(skelFilename);
        return Jsoup.parse(skelFile, "utf-8");
    }

    private String getDataPath(Element elt) {
        String apath = elt.attr(DATA_APATH_ATTRIBUTE);
        if (StringUtils.isNotBlank(apath))
            return decode(apath);
        return elt.attr(DATA_PATH_ATTRIBUTE);
    }

    private String getDataValuePath(Element elt) {
        String path = elt.attr(DATA_VALUE_ATTRIBUTE);
        if (StringUtils.isNotBlank(path)) {
            int index = path.indexOf("%");
            if (StringUtils.isNotBlank(path) && index >= 0)
                return path.substring(0, index);
        }
        return path;
    }

    private String getDataValueFormat(Element elt) {
        String path = elt.attr(DATA_VALUE_ATTRIBUTE);
        int index = path.indexOf('%');
        if (index < 0) {
            return null;
        }
        String format = path.substring(index + 1);
        if (StringUtils.isBlank(format) || format.length() == 0)
            return null;

        return format;
    }

    // set encoded absolute path
    private void setDataAPath(StclContext stclContext, Element elt, String path) {
        elt.attr(DATA_APATH_ATTRIBUTE, encode(path));
    }

    private void removePathAttribute(StclContext stclContext, Element elt) {
        elt.removeAttr(DATA_PATH_ATTRIBUTE);
        elt.removeAttr(DATA_VALUE_ATTRIBUTE);
        elt.removeAttr(CONDITION_ATTRIBUTE);
        elt.removeAttr(CLASS_ATTRIBUTE);
        elt.removeAttr(LABEL_ATTRIBUTE);
        elt.removeAttr(MODE_ATTRIBUTE);
    }

    protected String encode(String path) {
        Base64 base = new Base64();
        try {
            String encoded = new String(base.encode(path.getBytes()));
            return encoded.replaceAll("\\r\\n|\\r|\\n", "");
        } catch (Exception e) {
            return "";
        }
    }

    protected String decode(String path) {
        Base64 base = new Base64();
        return new String(base.decode(path.getBytes()));
    }

    private boolean tdFromHeader(Element th) {
        String valuePath = getDataValuePath(th);
        String labelPath = th.attr(LABEL_ATTRIBUTE);
        return StringUtils.isNotBlank(valuePath) || StringUtils.isNotBlank(labelPath);
    }

    protected class ConditionTestResult {

        boolean result;
        PStcl currentStcl;

        ConditionTestResult() {
            result = false;
        }

        ConditionTestResult(PStcl current) {
            result = current.isNotNull();
            currentStcl = current;
        }
    }

    protected boolean satisfyDataCondition(StclContext stclContext, String cond, PStcl stcl) {

        // checks only if condition defined
        if (StringUtils.isNotBlank(cond)) {
            ConditionTestResult result = evaluateConditionConstraint(stclContext, cond, stcl);
            return result.result;
        }
        return true;
    }

    protected ConditionTestResult evaluateConditionConstraint(StclContext stclContext, String path, PStcl stcl) {
        try {
            if (stcl.isNull()) {
                return new ConditionTestResult();
            }
            if (StringUtils.isBlank(path)) {
                return new ConditionTestResult();
            }

            // checks equals value condition
            if (PathUtils.indexOf(path, EQUALS) != -1) {
                int index = PathUtils.indexOf(path, EQUALS);
                String c = path.substring(0, index);
                String p = path.substring(index + EQUALS.length());
                if (StringUtils.isBlank(p)) {
                    return new ConditionTestResult();
                }

                // checks value
                String value = getPropertyValue(stclContext, stcl, p);
                if (!value.equals(c)) {
                    return new ConditionTestResult();
                }
                return new ConditionTestResult(stcl);
            }

            // checks equals value condition
            if (PathUtils.indexOf(path, DIFF) != -1) {
                int index = PathUtils.indexOf(path, DIFF);
                String c = path.substring(0, index);
                String p = path.substring(index + DIFF.length());
                if (StringUtils.isBlank(p)) {
                    return new ConditionTestResult();
                }

                // checks value
                String value = getPropertyValue(stclContext, stcl, p);
                if (value.equals(c)) {
                    return new ConditionTestResult();
                }
                return new ConditionTestResult(stcl);
            }

            // checks less value condition
            if (PathUtils.indexOf(path, LESS_OR_EQUAL) != -1) {
                int index = PathUtils.indexOf(path, LESS_OR_EQUAL);
                String c = path.substring(0, index);
                String p = path.substring(index + LESS_OR_EQUAL.length());
                if (StringUtils.isBlank(p)) {
                    return new ConditionTestResult();
                }

                // checks value
                String value = getPropertyValue(stclContext, stcl, p);
                if (Integer.parseInt(c) > Integer.parseInt(value)) {
                    return new ConditionTestResult();
                }
                return new ConditionTestResult(stcl);
            }

            // checks less value condition
            if (PathUtils.indexOf(path, LESS) != -1) {
                int index = PathUtils.indexOf(path, LESS);
                String c = path.substring(0, index);
                String p = path.substring(index + LESS.length());
                if (StringUtils.isBlank(p)) {
                    return new ConditionTestResult();
                }

                // checks value
                String value = getPropertyValue(stclContext, stcl, p);
                if (Integer.parseInt(c) >= Integer.parseInt(value)) {
                    return new ConditionTestResult();
                }
                return new ConditionTestResult(stcl);
            }

            // checks greater value condition
            if (PathUtils.indexOf(path, GREATER_OR_EQUAL) != -1) {
                int index = PathUtils.indexOf(path, GREATER_OR_EQUAL);
                String c = path.substring(0, index);
                String p = path.substring(index + GREATER_OR_EQUAL.length());
                if (StringUtils.isBlank(p)) {
                    return new ConditionTestResult();
                }

                // checks value
                String value = getPropertyValue(stclContext, stcl, p);
                if (Integer.parseInt(c) < Integer.parseInt(value)) {
                    return new ConditionTestResult();
                }
                return new ConditionTestResult(stcl);
            }

            // checks greater value condition
            if (PathUtils.indexOf(path, GREATER) != -1) {
                int index = PathUtils.indexOf(path, GREATER);
                String c = path.substring(0, index);
                String p = path.substring(index + GREATER.length());
                if (StringUtils.isBlank(p)) {
                    return new ConditionTestResult();
                }

                // checks value
                String value = getPropertyValue(stclContext, stcl, p);
                if (Integer.parseInt(c) <= Integer.parseInt(value)) {
                    return new ConditionTestResult();
                }
                return new ConditionTestResult(stcl);
            }

            // checks not condition
            boolean not = path.startsWith(NOT);
            if (not) {
                path = path.substring(NOT.length());
                if (StringUtils.isBlank(path)) {
                    return new ConditionTestResult();
                }

                // checks stencil exists
                PStcl target = stcl.getStencil(stclContext, path);
                if (target.isNotNull()) {
                    return new ConditionTestResult();
                }
                return new ConditionTestResult(stcl);
            }

            PStcl target = stcl.getStencil(stclContext, path);
            return new ConditionTestResult(target);
        } catch (Exception e) {
            return new ConditionTestResult();
        }
    }

    private String getPwd(StclContext stclContext, PStcl stcl) {
        if (stcl == prop) {
            return PathUtils.compose(prop_stcl.pwd(stclContext), prop_path);
        }
        return stcl.pwd(stclContext);
    }
}