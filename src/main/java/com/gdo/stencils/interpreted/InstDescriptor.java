/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.interpreted;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.gdo.stencils._Stencil;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.factory.IStencilFactory;
import com.gdo.stencils.factory.InterpretedStencilFactory;
import com.gdo.stencils.plug._PStencil;
import com.gdo.util.XmlWriter;

/**
 * <p>
 * Instance inherits from a template as it is a local template description with
 * local declarations.
 * <p>
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
 * @see com.gdo.stencils.cmd.CommandContext Context
 */
public class InstDescriptor<C extends _StencilContext, S extends _PStencil<C, S>> extends TemplateDescriptor<C, S> {
    private String _id; // instance id
    private String _file; // file name if the instance is saved in another file
    private String _encoding; // encoding used if the instance is saved in
    // another file
    private boolean _overloadable; // can overload an already defined instance
    private IStencilFactory.Mode _on = IStencilFactory.Mode.ON_CREATION; // when

    // the
    // plug
    // should
    // be
    // performed
    // (usually
    // in
    // create)

    // storing means the instance is already saved)

    // the id is used to reference the instance for plug
    public final String getId() {
        String id = _id;
        if (StringUtils.isBlank(id)) {
            if (getLog().isWarnEnabled()) {
                String msg = String.format("Intance declared witout id (use name %s by default)", getName());
                getLog().warn(null, msg);
            }
            id = getName();
        }
        return id;
    }

    public final void setId(String id) {
        _id = id;
    }

    @Override
    public String getFacets(C stclContext) {
        TemplateDescriptor<C, S> tempDesc = super.getSuperDescriptor(stclContext);
        return (tempDesc != null) ? tempDesc.getFacets(stclContext) : null;
    }

    public final String getFile() {
        return _file;
    }

    public final void setFile(String file) {
        _file = file;
    }

    public final String getEncoding() {
        return _encoding;
    }

    public final void setEncoding(String encoding) {
        _encoding = encoding;
    }

    @Override
    public String getTemplateName() {
        return getTemplate();
    }

    /**
     * Same name as the template used (even if a subtemplate of it).
     */
    public final String getTemplate() {
        return super.getExtends();
    }

    public final void setTemplate(String template) {
        setExtends(template);
    }

    public boolean isOverloadable() {
        return _overloadable;
    }

    public void setOverloadable(boolean overloadable) {
        _overloadable = overloadable;
    }

    public IStencilFactory.Mode getMode() {
        return _on;
    }

    public void setOn(IStencilFactory.Mode on) {
        _on = on;
    }

    // an instance in a load instance is again a load instance (same for create)
    @Override
    public void addInstDescriptor(InstDescriptor<C, S> inst) {
        inst.setOn(getMode());
        for (PlugDescriptor<C, S> plug : inst.getPlugDescriptors()) {
            plug.setOnAsMode(getMode());
        }
        super.addInstDescriptor(inst);
    }

    // an plug in a load instance is again a load plug (same for create)
    @Override
    public void addPlugDescriptor(PlugDescriptor<C, S> plug) {
        plug.setOnAsMode(getMode());
        super.addPlugDescriptor(plug);
    }

    /**
     * Search for the real upper template.
     */
    @Override
    public TemplateDescriptor<C, S> getSuperTemplateDescriptor(C stclContext) {
        TemplateDescriptor<C, S> tempDesc = super.getSuperDescriptor(stclContext);
        return (tempDesc != null) ? tempDesc.getSuperTemplateDescriptor(stclContext) : null;
    }

    public _Stencil<C, S> createInstance(C stclContext, String ref, InstanceRepository<C, S> instances, int completionLevel) {
        InterpretedStencilFactory<C, S> factory = (InterpretedStencilFactory<C, S>) stclContext.<C, S> getStencilFactory();

        // an instance should not be created twice
        /*
         * if (_created) { if (getLog().isErrorEnabled()) { String msg =
         * String.format("An instance cannot be created twice : %s", this);
         * getLog().error(msg); } } _created = true;
         */

        // instance are always created as stencil in load mode
        IStencilFactory.Mode mode = instances.getMode();
        // instances.setMode(Mode.ON_LOAD);
        if (getMode() != null) {
            instances.setMode(getMode());
        }

        // create instance from template
        Object[] params = getParameters(stclContext);
        _Stencil<C, S> stcl = factory.newStencil(stclContext, instances, ref, this, params);
        if (stcl == null) {
            return null;
        }

        // this is the root stencil descriptor (so no push of path)
        // boolean isRoot = instances.isEmpty();

        // store and complete from local definitions (access path is modified)
        // S stored = instances.store(stclContext, ref, stcl);
        // instances.store(stclContext, ref, stcl);
        /*
         * if (!isRoot) instances.push(ref); try { completeStencil(stclContext,
         * stored, instances, completionLevel); stcl.afterComplete(stclContext); }
         * catch (Exception e) { if (getLog().isWarnEnabled()) { String msg =
         * String.format("Cannot complete stencil of type %s", getTemplate());
         * getLog().warn(msg, e); } return null; } if (!isRoot) instances.pop();
         */

        // finish stencil creation
        // ((Stencil<C, ?>) stencil).setInCreation(false);
        // stcl.setDescriptor(this);
        // reset instance repository mode
        instances.setMode(mode);
        return stcl;
    }

    @SuppressWarnings("unchecked")
    public InstDescriptor<C, S> copy() {
        try {
            InstDescriptor<C, S> copy = (InstDescriptor<C, S>) clone();
            return copy;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * When a slot is declared in an instance, it should be stored in it on
     * save.
     */
    @Override
    public void addSlotDescriptor(SlotDescriptor<C, S> slot) {
        slot.setRedefined(true);
        super.addSlotDescriptor(slot);
    }

    @Override
    public void completeStencil(C stclContext, S stencil, InstanceRepository<C, S> instances, int completionLevel) {

        // adds instance name
        String name = getName();
        if (name != null) {
            stencil.setName(stclContext, name);
        }

        // completes as template descriptor (here should use annotation)
        super.completeStencil(stclContext, stencil, instances, completionLevel);
    }

    @Override
    public void save(C stclContext, XmlWriter declPart, XmlWriter plugPart) throws IOException {
        logError(stclContext, "should be never called (except for trace) for : %s", _id);
        /*
         * // checks parameters if (plugPart != null) { throw new
         * IllegalArgumentException
         * ("plug xml writer should be null for instance descriptor"); }
         * 
         * // does nothing if the descriptor already saved if
         * (declPart.equals(_doc)) { return; } _doc = declPart;
         * 
         * declPart.startElement(getRootTag());
         * 
         * declPart.writeAttribute("template", getTemplate());
         * declPart.writeAttribute("id", getId()); saveParameters(stclContext,
         * declPart); declPart.closeElement();
         * 
         * // first pass (decl part) StringWriter decl = new StringWriter();
         * StringWriter body = new StringWriter(); saveDescription(stclContext, new
         * XmlWriter(decl), new XmlWriter(body));
         * 
         * // writes in order (now body part)
         * declPart.write(decl.getBuffer().toString());
         * declPart.write(body.getBuffer().toString());
         * 
         * // closes stencil tag declPart.endElement(getRootTag());
         */
    }

    protected String getRootTag() {
        return InterpretedStencilFactory.INSTANCE;
    }

}
