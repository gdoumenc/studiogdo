/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils.plug;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.gdo.helper.ConverterHelper;
import com.gdo.project.model.ComposedActionStcl;
import com.gdo.project.model.ServletStcl;
import com.gdo.project.slot.CursorLinkStcl;
import com.gdo.project.slot._SlotCursor;
import com.gdo.stencils.Keywords;
import com.gdo.stencils.Result;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.Stcl.IMaskFacetGenerator;
import com.gdo.stencils.StclContext;
import com.gdo.stencils._Stencil;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.cond.PathCondition;
import com.gdo.stencils.faces.RenderContext;
import com.gdo.stencils.facet.FacetResult;
import com.gdo.stencils.facet.FacetType;
import com.gdo.stencils.facet.HTML5SectionCompleter;
import com.gdo.stencils.facet.JSONSectionCompleter;
import com.gdo.stencils.facet.PythonSectionCompleter;
import com.gdo.stencils.factory.StclFactory;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;
import com.gdo.util.XmlWriter;

/**
 * <p>
 * Basic implementation of the studiogdo plugged stencil.
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
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com)</a>
 */
public class PStcl extends _PStencil<StclContext, PStcl> {

    //
    // cursor implementation (SQL, ...)
    //

    // cursor on contained stencils
    private _SlotCursor _cursor;

    // the slot containing the cursor
    private PSlot<StclContext, PStcl> _cursor_container;

    // cursor key may be not same as key :
    // modele(2) pluged in tmp (single slot)
    // ->
    // cursor_key=2, key=none
    private String _cursor_key;

    // list of plugged references
    // TODO should be defined in cursor
    private List<PStcl> _cursor_references;

    /**
     * Creates a new classical plugged stencil.
     * 
     * @param stclContext
     *            the stencil context.
     * @param stencil
     *            the contained stencil.
     * @param slot
     *            the containing slot.
     * @param key
     *            the key identifier.
     */
    public PStcl(StclContext stclContext, _Stencil<StclContext, PStcl> stencil, PSlot<StclContext, PStcl> slot, IKey key) {
        super(stclContext, stencil, slot, key);
    }

    /**
     * Creates a new plugged stencil based on a cursor.
     * 
     * @param stclContext
     *            the stencil context.
     * @param slot
     *            the containing slot.
     * @param key
     *            the key identifier.
     * @param cursor
     *            the stencils cursor.
     */
    public PStcl(StclContext stclContext, PSlot<StclContext, PStcl> slot, IKey key, _SlotCursor cursor) {
        super(stclContext, (_Stencil<StclContext, PStcl>) null, slot, key);

        // creates cursor informations
        this._cursor = cursor;
        this._cursor_references = new ArrayList<PStcl>();
        this._cursor_container = slot;
        this._cursor_key = key.toString();

        // adds this new instance as a new reference to the cursor
        addThisReferenceToStencil(stclContext);
    }

    /**
     * Creates a new plugged stencil from already plugged stencil.
     * 
     * @param stclContext
     *            the stencil context.
     * @param pstencil
     *            the plugged stencil as source for creation.
     * @param slot
     *            the containing slot.
     * @param key
     *            the key identifier.
     */
    public PStcl(StclContext stclContext, PStcl pstencil, PSlot<StclContext, PStcl> slot, IKey key) {
        super(stclContext, pstencil, slot, key);
    }

    /**
     * Plugged null error stencil.
     * 
     * @param result
     *            the error reason.
     */
    public PStcl(Result result) {
        super(result);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.gdo.stencils.plug.PStencil#initialize(com.gdo.stencils.StencilContext,
     * com.gdo.stencils.plug.PStencil, com.gdo.stencils.plug.PSlot,
     * com.gdo.stencils.key.IKey)
     */
    @Override
    public void initialize(StclContext stclContext, PStcl pstencil, PSlot<StclContext, PStcl> slot, IKey key) {
        if (pstencil.isCursorBased()) {
            super.initialize(stclContext, (_Stencil<StclContext, PStcl>) null, slot, key);
            this._cursor = pstencil._cursor;
            this._cursor_references = pstencil._cursor_references;
            this._cursor_container = pstencil._cursor_container;
            this._cursor_key = pstencil._cursor_key;
            addThisReferenceToStencil(stclContext);
        } else {
            super.initialize(stclContext, pstencil, slot, key);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gdo.stencils.plug.PStencil#isNull()
     */
    @Override
    public boolean isNull() {
        if (isCursorBased()) {
            return false;
        }
        return super.isNull();
    }

    @Override
    public boolean isLink(StclContext stclContext) {
        if (isCursorBased()) {
            return false;
        }
        return super.isLink(stclContext);
    }

    public boolean isCursorBased() {
        return this._cursor != null;
    }

    public void addCursor(StclContext stclContext, PSlot<StclContext, PStcl> cursorContainer, _SlotCursor cursor, String cursorKey) {
        _cursor_container = cursorContainer;
        _cursor = cursor;
        _cursor_key = cursorKey;
    }

    public void updateCursor(StclContext stclContext) {
        PathCondition<StclContext, PStcl> cond = PathCondition.<StclContext, PStcl> newKeyCondition(stclContext, new Key<>(_cursor_key), null);
        this._cursor_container.getStencils(stclContext, cond);
    }
    
    public String getPropertyValue(StclContext stclContext, String path) {
        return _cursor.getPropertyValue(stclContext, _cursor_container, _cursor_key, path);
    }

    @Override
    public void clear(StclContext stclContext) {

        // if a cursor is defined then removes it from cursor
        if (isCursorBased()) {
            this._cursor.removeFromCursor(stclContext, this._cursor_key);
            this._cursor = null;
            this._cursor_container = null;
            this._cursor_key = null;
            this._cursor_references = null;
        }

        // does classical clear
        super.clear(stclContext);
    }

    // to be removed after
    @Deprecated
    public _Stencil<StclContext, PStcl> getStencil() {

        // if a cursor is defined then get stencil from it
        if (isCursorBased()) {
            PStcl stcl = this._cursor.getStencil(StclContext.defaultContext(), this._cursor_container, getContainingSlot(), this._cursor_key);
            return stcl.getStencil(StclContext.defaultContext());
        }

        return super.getStencil(StclContext.defaultContext());
    }

    // cannot return Stcl as CommandStcl and PropStcl are not Stcl
    @Override
    public <K extends _Stencil<StclContext, PStcl>> K getStencil(StclContext stclContext) {

        // if a cursor is defined then get stencil from it
        if (isCursorBased()) {
            PStcl stcl = this._cursor.getStencil(stclContext, this._cursor_container, getContainingSlot(), this._cursor_key);
            return stcl.getStencil(stclContext);
        }

        return super.getStencil(stclContext);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.gdo.stencils.plug.PStencil#release(com.gdo.stencils.StencilContext)
     */
    @Override
    public void release(StclContext stclContext) {
        if (isCursorBased()) {
            this._cursor.release(stclContext, this._cursor_container, getContainingSlot(), this._cursor_key);
        }
        super.release(stclContext);
    }

    /**
     * TODO : TO BE CHECKED Releases the stencil as a cursor (the stencil
     * referenced is no more in memory but still in some slot)
     * 
     * @param stclContext
     *            the stencil context.
     * @param cursor
     *            the cursor to retrieve the stencil.
     * @param key
     *            the key to retrieve the stencil in the cursor.
     */
    public void release(StclContext stclContext, PSlot<StclContext, PStcl> container, _SlotCursor cursor, IKey key) {
        this._cursor_container = container;
        this._cursor = cursor;
        this._cursor_references = this._stencil.getPluggedReferences(stclContext);
        this._cursor_key = key.toString();

        this._stencil = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gdo.stencils.plug.PStencil#getId(com.gdo.stencils.StencilContext)
     */
    @Override
    public String getId(StclContext stclContext) {
        if (isCursorBased()) {
            return this._cursor.getId(stclContext) + this._cursor_key;
        }
        return super.getId(stclContext);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gdo.stencils.plug.PStencil#getUID(com.gdo.stencils.StencilContext)
     */
    @Override
    public String getUId(StclContext stclContext) {
        if (isCursorBased()) {
            return this._cursor.getUId(stclContext) + this._cursor_key;
        }
        return super.getUId(stclContext);
    }

    /**
     * Returns a cloned stencil (which is never in cursor).
     * 
     * @param stclContext
     *            the stencil context.
     * @param key
     *            the plug key.
     * @return the cloned stencil.
     */
    public PStcl clone(StclContext stclContext, PSlot<StclContext, PStcl> slot, IKey key) {
        try {
            return getReleasedStencil(stclContext).clone(stclContext, slot, key, self());
        } catch (CloneNotSupportedException e) {
            return nullPStencil(stclContext, Result.error(e.getMessage()));
        }
    }

    /*
     * @Override public PStcl clone(StclContext stclContext) throws
     * CloneNotSupportedException { if (this._cursor != null) { if
     * (getLog().isWarnEnabled()) { getLog().warn(stclContext,
     * "PStcl clone not implemented on cursor stencil"); } return new
     * PStcl(stclContext, getContainingSlot(), getKey(), this._cursor); }
     * Stencil<StclContext, PStcl> stcl = getReleasedStencil(stclContext);
     * Stencil<StclContext, PStcl> clone = stcl.clone(stclContext, this); return
     * new PStcl(clone, getContainingSlot(), getKey()); }
     */

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.gdo.stencils.plug.PStencil#getRootStencil(com.gdo.stencils.StencilContext
     * )
     */
    @Override
    public PStcl getRootStencil(StclContext stclContext) {
        return stclContext.getServletStcl();
    }

    /**
     * Returns the multi post values separator.
     * 
     * @param stclContext
     *            the stencil context.
     * @return the multi post values separator.
     */
    public String getMultiPostSep(StclContext stclContext) {
        ServletStcl project = stclContext.getServletStcl().getReleasedStencil(stclContext);
        return project.getMultiPostSep();
    }

    public FacetResult getFacet(StclContext stclContext, String facet, String mode) {
        RenderContext<StclContext, PStcl> renderCtxt = new RenderContext<StclContext, PStcl>(stclContext, this, facet, mode);
        return getFacet(renderCtxt);
    }

    @Override
    public FacetResult getFacet(RenderContext<StclContext, PStcl> renderContext) {

        // checks validity
        if (isNull()) {
            return new FacetResult(FacetResult.ERROR, "invalid stencil", null);
        }

        StclContext stclContext = renderContext.getStencilContext();
        String facet = renderContext.getFacetType();

        // HTML 5 case
        if (FacetType.SKEL5.equals(facet)) {
            String skel = renderContext.getFacetMode();
            HTML5SectionCompleter completer = new HTML5SectionCompleter();
            return completer.getSkeleton(stclContext, skel);
        }
        if (FacetType.HTML5.equals(facet)) {
            String skel = renderContext.getFacetMode();
            HTML5SectionCompleter completer = new HTML5SectionCompleter();
            return completer.getFacetFromSkeleton(stclContext, this, skel);
        }
        if (FacetType.DOM5.equals(facet)) {
            String dom = renderContext.getFacetMode();
            HTML5SectionCompleter completer = new HTML5SectionCompleter();
            return completer.getFacetFromDOM(stclContext, this, dom);
        }
        if (FacetType.HTML5_TEXT.equals(facet)) {
            String skel = renderContext.getFacetMode();
            HTML5SectionCompleter completer = new HTML5SectionCompleter();
            return completer.getFacetFromSkeletonToText(stclContext, this, skel);
        }
        if (FacetType.DOM5_TEXT.equals(facet)) {
            String dom = renderContext.getFacetMode();
            HTML5SectionCompleter completer = new HTML5SectionCompleter();
            return completer.getFacetFromDOMToText(stclContext, this, dom);
        }

        // JSON case
        if (FacetType.JSON.equals(facet)) {
            String mode = renderContext.getFacetMode();
            JSONSectionCompleter completer = new JSONSectionCompleter();
            return completer.getFacetFromDOM(stclContext, this, mode);
        }
        if (FacetType.JSKEL.equals(facet)) {
            String mode = renderContext.getFacetMode();
            JSONSectionCompleter completer = new JSONSectionCompleter();
            return completer.getFacetFromSkeleton(stclContext, this, mode);
        }

        // PYTHON case
        if (FacetType.PYTHON.equals(facet)) {
            String mode = renderContext.getFacetMode();
            PythonSectionCompleter completer = new PythonSectionCompleter();
            return completer.getFacetFromDOM(stclContext, this, mode);
        }

        // generator mask case
        if (FacetType.MASK.equals(facet)) {
            String mode = renderContext.getFacetMode();
            PStcl generator = getStencil(stclContext, Stcl.Slot.GENERATOR);
            if (StencilUtils.isNull(generator)) {
                String msg = String.format("Mask facet must be used with a generator not defined in %s (mode %s) : %s", this, mode, generator.getNullReason());
                return new FacetResult(FacetResult.ERROR, msg, null);
            }
            IMaskFacetGenerator gen = (IMaskFacetGenerator) generator.getReleasedStencil(stclContext);
            InputStream reader = gen.getFacet(stclContext, mode, this, generator, generator);
            return new FacetResult(reader, "text/plain");
        }

        // default flex facet from template class name
        if (FacetType.FLEX.equals(facet)) {
            _Stencil<StclContext, PStcl> stcl = getReleasedStencil(stclContext);
            return stcl.getFacet(renderContext);
        }

        // default model facet
        if (FacetType.MODEL.equals(facet)) {
            String mode = renderContext.getFacetMode();
            if (StringUtils.isEmpty(mode) || FacetType.NONE.equals(mode)) {
                String model = "<model/>";
                FacetResult result = new FacetResult(new ByteArrayInputStream(model.getBytes()), "text/plain");
                result.setContentLength(model.length());
                return result;
            }
            if (mode.startsWith("$")) {

                // decomposes pathes
                String treePath = mode.substring(1);
                String treeModelMode = "";
                int pos = treePath.indexOf(PathUtils.MULTI);
                if (pos != -1) {
                    treeModelMode = treePath.substring(pos + 1);
                    treePath = treePath.substring(0, pos);
                }

                StringBuffer xml = new StringBuffer();
                xml.append("<model>");
                xml.append("<subTreePath>%s</subTreePath>");
                xml.append("<subTreeModelMode>%s</subTreeModelMode>");
                xml.append("<stclSelectedMode/>");
                xml.append("</model>");
                String model = String.format(xml.toString(), treePath, treeModelMode);
                InputStream reader = IOUtils.toInputStream(model);
                FacetResult result = new FacetResult(reader, "text/plain");
                result.setContentLength(model.length());
                return result;
            }
        }

        return super.getFacet(renderContext);
    }

    public String getStringFacet(StclContext stclContext, String facet, String mode) {
        try {
            FacetResult facetResult = getFacet(stclContext, facet, mode);
            StringWriter sw = new StringWriter();
            InputStream input = facetResult.getInputStream();
            IOUtils.copy(input, sw);
            facetResult.closeInputStream();
            return sw.getBuffer().toString();
        } catch (Exception e) {
            return e.toString();
        }
    }

    public String getHTML5Facet(StclContext stclContext, String skeleton) {
        return getStringFacet(stclContext, FacetType.HTML5, skeleton);
    }

    public String getDOM5Facet(StclContext stclContext, String html) {
        try {
            FacetResult facetResult = getFacet(stclContext, FacetType.DOM5, html);
            StringWriter sw = new StringWriter();
            InputStream input = facetResult.getInputStream();
            IOUtils.copy(input, sw);
            facetResult.closeInputStream();
            return sw.getBuffer().toString();
        } catch (Exception e) {
            return e.toString();
        }
    }

    public String getHTML5TextFacet(StclContext stclContext, String skeleton) {
        try {
            FacetResult facetResult = getFacet(stclContext, FacetType.HTML5_TEXT, skeleton);
            StringWriter sw = new StringWriter();
            InputStream input = facetResult.getInputStream();
            IOUtils.copy(input, sw);
            facetResult.closeInputStream();
            return sw.getBuffer().toString();
        } catch (Exception e) {
            return e.toString();
        }
    }

    public String getDOM5TextFacet(StclContext stclContext, String html) {
        try {
            FacetResult facetResult = getFacet(stclContext, FacetType.DOM5_TEXT, html);
            StringWriter sw = new StringWriter();
            InputStream input = facetResult.getInputStream();
            IOUtils.copy(input, sw);
            facetResult.closeInputStream();
            return sw.getBuffer().toString();
        } catch (Exception e) {
            return e.toString();
        }
    }

    // --------------------------------------------------------------------------
    //
    // Resources management
    //
    // --------------------------------------------------------------------------

    /**
     * Returns the stencil resource depending on the project configuration.
     * 
     * @param stclContext
     *            the stencil context.
     * @param resource
     *            the resource searched.
     * @return the resource stencil.
     */
    public PStcl getResourceStencil(StclContext stclContext, String resource) {
        String path = getResourcePath(stclContext, resource);
        if (StringUtils.isBlank(path)) {
            String msg = logWarn(stclContext, "The ressource %s is not defined", resource);
            return nullPStencil(stclContext, Result.error(msg));
        }
        PStcl servlet = stclContext.getServletStcl();
        return servlet.getStencil(stclContext, path);
    }

    public String getResourceValue(StclContext stclContext, String resource, String def) {
        String path = getResourcePath(stclContext, resource);
        if (StringUtils.isBlank(path)) {
            return logWarn(stclContext, "The ressource %s is not defined", resource);
        }
        PStcl servlet = stclContext.getServletStcl();
        String value = servlet.getString(stclContext, path);
        return (value != null) ? value : def;
    }

    public int getResourceValue(StclContext stclContext, String resource, int def) {
        String path = getResourcePath(stclContext, resource);
        if (StringUtils.isBlank(path)) {
            logWarn(stclContext, "The ressource %s is not defined", resource);
            return 0;
        }
        PStcl servlet = stclContext.getServletStcl();
        return servlet.getInt(stclContext, path, def);
    }

    public boolean getResourceValue(StclContext stclContext, String resource, boolean def) {
        String path = getResourcePath(stclContext, resource);
        if (StringUtils.isBlank(path)) {
            logWarn(stclContext, "The ressource %s is not defined", resource);
            return false;
        }
        PStcl servlet = stclContext.getServletStcl();
        return servlet.getBoolean(stclContext, path, def);
    }

    public void setResourceValue(StclContext stclContext, String resource, String value) {
        String path = getResourcePath(stclContext, resource);
        if (StringUtils.isBlank(path)) {
            logWarn(stclContext, "The ressource %s is not defined", resource);
            return;
        }
        PStcl servlet = stclContext.getServletStcl();
        servlet.setString(stclContext, path, value);
    }

    /**
     * Returns the slot containning some resources depending on the project
     * configuration.
     * 
     * @param stclContext
     *            the stencil context.
     * @param resources
     *            the resources searched.
     * @return the resources slot.
     */
    public PSlot<StclContext, PStcl> getResourceSlot(StclContext stclContext, String resources) {
        String path = getResourcePath(stclContext, resources);
        if (StringUtils.isBlank(path)) {
            String msg = logWarn(stclContext, "The ressources slot %s is not defined", resources);
            return new PSlot<StclContext, PStcl>(Result.error(msg));
        }
        PStcl servlet = stclContext.getServletStcl();
        return servlet.getSlot(stclContext, path);
    }

    /**
     * Returns the path of some resources depending on the project
     * configuration.
     * 
     * @param stclContext
     *            the stencil context.
     * @param resource
     *            the resources searched.
     * @param self
     *            this stencil as a plugged stencil.
     * @return the resources slot path.
     */
    public String getResourcePath(StclContext stclContext, String resource) {
        return Stcl.getResourcePath(stclContext, resource);
    }

    // --------------------------------------------------------------------------
    //
    // Pluging informations
    //
    // --------------------------------------------------------------------------

    @Override
    public List<PStcl> getStencilOtherPluggedReferences(StclContext stclContext) {
        if (this._cursor_references != null) {
            return this._cursor_references;
        }
        return super.getStencilOtherPluggedReferences(stclContext);
    }

    @Override
    public void addThisReferenceToStencil(StclContext stclContext) {
        if (this._cursor_references != null) {
            this._cursor_references.add(self());
        } else {
            super.addThisReferenceToStencil(stclContext);
        }
    }

    @Override
    public void removeThisReferenceFromStencil(StclContext stclContext) {
        if (this._cursor_references != null) {
            this._cursor_references.remove(self());
        } else {
            super.removeThisReferenceFromStencil(stclContext);
        }
    }

    // should be called on stencil only (not a plugged stencil)
    @Deprecated
    @Override
    public void afterUnplug(StclContext stclContext, PSlot<StclContext, PStcl> slot) {

        // does nothing if defined on cursor (afterPlug and afterLastUnplug is
        // already called - before removed)
        if (isCursorBased()) {
            return; // should never goes here.. but
        }
        super.afterUnplug(stclContext, slot);
    }

    // should be called on stencil only (not a plugged stencil)
    @Deprecated
    @Override
    public void afterLastUnplug(StclContext stclContext) {

        // does nothing if defined on cursor (afterPlug and afterLastUnplug is
        // already called - before removed)
        if (isCursorBased()) {
            return; // should never goes here.. but
        }
        super.afterLastUnplug(stclContext);
    }

    // --------------------------------------------------------------------------
    //
    // Cursor management
    //
    // --------------------------------------------------------------------------

    // --------------------------------------------------------------------------
    //
    // Properties management.
    //
    // --------------------------------------------------------------------------

    @Override
    public String getType(StclContext stclContext, String path) {
        if (isNull()) {
            throw new IllegalStateException("cannot get a property type from an unvalid stencil: " + getNullReason());
        }

        // searches if a property was associated at this key in the cursor
        if (!PathUtils.isComposed(path) && isCursorBased()) {
            return Keywords.STRING;
        }

        // search string in slot
        return super.getType(stclContext, path);
    }

    @Override
    public String getString(StclContext stclContext, String path, String def) {
        if (isNull()) {
            throw new IllegalStateException("cannot get a string property from an unvalid stencil: " + getNullReason());
        }

        // searches if a property was associated at this key in the cursor
        if (!PathUtils.isComposed(path) && isCursorBased()) {
            String value = _cursor.getPropertyValue(stclContext, _cursor_container, _cursor_key, path);
            if (value != null) {
                return value;
            }
        }

        // search string in slot
        return super.getString(stclContext, path, def);
    }

    @Override
    public int getInt(StclContext stclContext, String path, int def) {

        // checks validity
        if (isNull()) {
            throw new IllegalStateException("cannot get an int property from an unvalid stencil: " + getNullReason());
        }

        // searches if a property was associated at this key in the cursor
        if (isCursorBased() && !PathUtils.isComposed(path)) {
            String value = this._cursor.getPropertyValue(stclContext, getContainingSlot(), this._cursor_key, path);
            if (value != null) {
                try {
                    return Integer.parseInt(value);
                } catch (Exception e) {
                    return def;
                }
            }
        }

        // searches string in stencil
        return super.getInt(stclContext, path, def);
    }

    @Override
    public boolean getBoolean(StclContext stclContext, String path, boolean def) {

        // checks validity
        if (isNull()) {
            throw new IllegalStateException("cannot get a boolean property from an unvalid stencil: " + getNullReason());
        }

        // searches if a property was associated at this key in the cursor
        if (isCursorBased() && !PathUtils.isComposed(path)) {
            String value = this._cursor.getPropertyValue(stclContext, getContainingSlot(), this._cursor_key, path);
            if (value != null) {
                return ConverterHelper.parseBoolean(value);
            }
        }

        // searches string in stencil
        return super.getBoolean(stclContext, path, def);
    }

    @Override
    public double getDouble(StclContext stclContext, String path, double def) {

        // checks validity
        if (isNull()) {
            throw new IllegalStateException("cannot get a boolean property from an unvalid stencil: " + getNullReason());
        }

        // searches if a property was associated at this key in the cursor
        if (isCursorBased() && !PathUtils.isComposed(path)) {
            String value = this._cursor.getPropertyValue(stclContext, getContainingSlot(), this._cursor_key, path);
            if (value != null) {
                return Double.parseDouble(value);
            }
        }

        // searches string in stencil
        return super.getDouble(stclContext, path, def);
    }

    public void addPropertyValue(StclContext stclContext, String path, String value) {

        // checks validity
        if (isNull()) {
            throw new IllegalStateException("cannot add a string property from an unvalid stencil: " + getNullReason());
        }
        if (PathUtils.isComposed(path)) {
            throw new IllegalStateException("cannot add a string property on a composed path stencil: " + path);
        }

        // adds this property to the cursor properties
        if (isCursorBased()) {
            this._cursor.addPropertyValue(stclContext, getContainingSlot(), getContainingSlot(), this._cursor_key, path, value);
            return;
        }

        // search string in slot
        super.setString(stclContext, path, value);
    }

    /*
     * Very often used getter.
     */

    public String getString(StclContext stclContext, String path) {
        return getString(stclContext, path, "");
    }

    public int getInt(StclContext stclContext, String path) {
        return getInt(stclContext, path, 0);
    }

    public boolean getBoolean(StclContext stclContext, String path) {
        return getBoolean(stclContext, path, false);
    }

    public double getDouble(StclContext stclContext, String path) {
        return getDouble(stclContext, path, 0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.gdo.stencils.plug.PStencil#setString(com.gdo.stencils.StencilContext,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void setString(StclContext stclContext, String path, String value) {
        if (isNull()) {
            throw new IllegalStateException("cannot set a string property from an unvalid stencil: " + getNullReason());
        }

        // searches if a property was associated at this key in the cursor
        if (isCursorBased() && !PathUtils.isComposed(path)) {
            String old = this._cursor.getPropertyValue(stclContext, getContainingSlot(), this._cursor_key, path);
            if (old != null) {

                // then replaces it
                this._cursor.addPropertyValue(stclContext, this._cursor_container, getContainingSlot(), this._cursor_key, path, value);
                super.setString(stclContext, path, value);
                return;
            }
        }

        // search string in slot
        super.setString(stclContext, path, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gdo.stencils.plug.PStencil#setInt(com.gdo.stencils.StencilContext,
     * java.lang.String, int)
     */
    @Override
    public void setInt(StclContext stclContext, String path, int value) {

        // searches if a property was associated at this key in the cursor
        if (isCursorBased()) {
            String old = this._cursor.getPropertyValue(stclContext, getContainingSlot(), this._cursor_key, path);

            // replaces it if already defined
            if (old != null) {
                this._cursor.addPropertyValue(stclContext, this._cursor_container, getContainingSlot(), this._cursor_key, path, Integer.toString(value));
            }
        }

        // in any case set the value
        super.setInt(stclContext, path, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.gdo.stencils.plug.PStencil#setBoolean(com.gdo.stencils.StencilContext,
     * java.lang.String, boolean)
     */
    @Override
    public void setBoolean(StclContext stclContext, String path, boolean value) {

        // searches if a property was associated at this key in the cursor
        if (isCursorBased()) {
            String old = this._cursor.getPropertyValue(stclContext, getContainingSlot(), this._cursor_key, path);
            if (old != null) {

                // then replaces it
                this._cursor.addPropertyValue(stclContext, this._cursor_container, getContainingSlot(), this._cursor_key, path, Boolean.toString(value));
                super.setBoolean(stclContext, path, value);
                return;
            }
        }

        // search string in slot
        super.setBoolean(stclContext, path, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.gdo.stencils.plug.PStencil#setDouble(com.gdo.stencils.StencilContext,
     * java.lang.String, double)
     */
    @Override
    public void setDouble(StclContext stclContext, String path, double value) {

        // searches if a property was associated at this key in the cursor
        if (isCursorBased()) {
            String old = this._cursor.getPropertyValue(stclContext, getContainingSlot(), this._cursor_key, path);
            if (old != null) {

                // then replaces it
                this._cursor.addPropertyValue(stclContext, this._cursor_container, getContainingSlot(), this._cursor_key, path, Double.toString(value));
                super.setDouble(stclContext, path, value);
                return;
            }
        }

        // search string in slot
        super.setDouble(stclContext, path, value);
    }

    @Override
    public PStcl plug(StclContext stclContext, PStcl stencil, String slotPath, IKey key) {
        if (Stcl.Slot.$LOCKED_BY.equals(slotPath) && isCursorBased()) {
            this._cursor.lock(stclContext, stencil, getKey());
        }
        return super.plug(stclContext, stencil, slotPath, key);
    }

    public void unplug(StclContext stclContext, PStcl stencil, String slotPath, IKey key) {
        if (Stcl.Slot.$LOCKED_BY.equals(slotPath) && isCursorBased()) {
            this._cursor.unlock(stclContext, key);
        }
        super.unplugOtherStencilFrom(stclContext, slotPath, stencil);
    }

    @Override
    public PSlot<StclContext, PStcl> clearSlot(StclContext stclContext, String slotPath) {
        if (Stcl.Slot.$LOCKED_BY.equals(slotPath) && isCursorBased()) {
            this._cursor.unlock(stclContext, getKey());
        }
        return super.clearSlot(stclContext, slotPath);
    }

    /**
     * Launches the command in a new command context.
     * 
     * @throws Exception
     */
    public final CommandStatus<StclContext, PStcl> launch(StclContext stclContext, String name, String path, Object... params) throws Exception {
        if (isNull()) {
            String prefix = ComposedActionStcl.class.getName();
            String msg = "cannot launch from an unvalid stencil: " + getNullReason();
            return new CommandStatus<StclContext, PStcl>(prefix, CommandStatus.ERROR, 0, msg, null);
        }
        Stcl stcl = getReleasedStencil(stclContext);
        return stcl.launch(stclContext, name, path, this, params);
    }

    /**
     * Launches the command in the same command context.
     * 
     * @throws Exception
     */
    public final CommandStatus<StclContext, PStcl> launch(CommandContext<StclContext, PStcl> cmdContext, String name, String path) {
        StclContext stclContext = cmdContext.getStencilContext();
        if (isNull()) {
            String prefix = ComposedActionStcl.class.getName();
            String msg = "cannot launch from an unvalid stencil: " + getNullReason();
            return new CommandStatus<StclContext, PStcl>(prefix, CommandStatus.ERROR, 0, msg, null);
        }
        Stcl stcl = getReleasedStencil(stclContext);
        return stcl.launch(cmdContext, name, path, this);
    }

    /**
     * A studiogdo stencil may have multi part upload entry.
     * 
     * @param stclContext
     *            the stencil context.
     * @param items
     *            the upload file item list.
     * @param fileName
     *            the upload file name.
     * @param item
     *            the file item.
     * @throws Exception
     */
    public void multipart(StclContext stclContext, String fileName, FileItem item) throws Exception {
        getReleasedStencil(stclContext).multipart(stclContext, fileName, item, this);
    }

    @Override
    public String saveAsInstance(StclContext stclContext, String dir, XmlWriter container) {

        // saves it a link if cursor
        if (isCursorBased()) {

            // gets sql path
            PSlot<StclContext, PStcl> slot = this._cursor_container;
            String path = PathUtils.createPath(this._cursor_container.pwd(stclContext), this._cursor_key);

            // xreates and saves link
            StclFactory factory = (StclFactory) stclContext.getStencilFactory();
            PStcl link = factory.createPStencil(stclContext, slot, getKey(), CursorLinkStcl.class.getName(), path);
            return link.saveAsInstance(stclContext, dir, container);
        }

        // super saves
        return super.saveAsInstance(stclContext, dir, container);
    }

    public StclContext defaultContext() {
        return StclContext.defaultContext();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        // compares cursor based plugged stencil
        if (obj instanceof PStcl) {
            PStcl o = (PStcl) obj;

            // cursor cases
            if (isCursorBased()) {
                return this._cursor.equals(o._cursor) && this._cursor_key.equals(o._cursor_key);
            }
        }

        // usual comparition
        return super.equals(obj);
    }

    @Override
    public IKey getKey() {
        return super.getKey();
    }

    @Override
    public String toString() {
        if (isNull()) {
            return "invalid plugged stencil: " + getNullReason();
        }
        if (isCursorBased()) {
            StringBuffer str = new StringBuffer();
            if (getKey() != null) {
                str.append('(').append(getKey().toString()).append(")");
            } else {
                str.append('(').append(this._cursor_key).append(")");
            }
            str.append("cursor slot ").append(this._cursor.toString());
            return str.toString();
        }
        return super.toString();
    }
}
