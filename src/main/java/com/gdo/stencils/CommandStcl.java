/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils;

import java.io.InputStream;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;

import com.gdo.helper.ClassHelper;
import com.gdo.project.model.ProjectStcl;
import com.gdo.project.slot.RootSlot;
import com.gdo.reflect.CommandsSlot;
import com.gdo.reflect.SlotsSlot;
import com.gdo.reflect.TemplateNameSlot;
import com.gdo.reflect.WhereSlot;
import com.gdo.stencils.Stcl.IMaskFacetGenerator;
import com.gdo.stencils.cmd.CommandStencil;
import com.gdo.stencils.faces.RenderContext;
import com.gdo.stencils.facet.FacetResult;
import com.gdo.stencils.facet.FacetType;
import com.gdo.stencils.facet.HTML5SectionCompleter;
import com.gdo.stencils.facet.JSONSectionCompleter;
import com.gdo.stencils.facet.PythonSectionCompleter;
import com.gdo.stencils.factory.StclFactory;
import com.gdo.stencils.factory.StencilFactory;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.CalculatedStringPropertySlot;
import com.gdo.stencils.slot._Slot;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;

/**
 * <p>
 * Command stencil defined to performs some actions (the C of the MVC model).
 * </p>
 * <p>
 * </p>
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a>
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */
public abstract class CommandStcl extends CommandStencil<StclContext, PStcl> {

    public interface Slot extends CommandStencil.Slot, Stcl.Slot {
        // just combined from command and stcl
    }

    public interface Command extends CommandStencil.Command, Stcl.Command {
        // just combined from command and stcl
    }

    public CommandStcl(StclContext stclContext) {
        super(stclContext);
    }

    @Override
    public void command(String name, Class<? extends CommandStencil<StclContext, PStcl>> clazz, Object... params) {
        super.command(name, clazz, params);
    }

    public static PStcl nullPStencil(StclContext stclContext, Result reasons) {
        StencilFactory<StclContext, PStcl> factory = (StencilFactory<StclContext, PStcl>) stclContext.getStencilFactory();
        Class<? extends PStcl> c = factory.getDefaultPStencilClass(stclContext);
        return ClassHelper.newInstance(c, reasons);
    }

    @Override
    protected _Slot<StclContext, PStcl> createRootSlot(StclContext stclContext) {
        return new RootSlot(stclContext, this, PathUtils.ROOT);
    }

    protected _Slot<StclContext, PStcl> createTemplateNameSlot(StclContext stclContext) {
        return new TemplateNameSlot(stclContext, this, Slot.$TEMPLATE_NAME);
    }

    protected _Slot<StclContext, PStcl> createPwdSlot(StclContext stclContext) {
        return new PwdSlot(stclContext, this, Slot.$PWD);
    }

    protected _Slot<StclContext, PStcl> createSlotSlot(StclContext stclContext) {
        return new SlotsSlot(stclContext, this, Slot.$SLOTS);
    }

    protected _Slot<StclContext, PStcl> createCommandSlot(StclContext stclContext) {
        return new CommandsSlot(stclContext, this, Slot.$COMMANDS);
    }

    protected _Slot<StclContext, PStcl> createWhereSlot(StclContext stclContext) {
        return new WhereSlot(stclContext, this, Slot.$WHERE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gdo.stencils.Stencil#clone(com.gdo.stencils.StencilContext,
     * com.gdo.stencils.plug.PSlot, com.gdo.stencils.key.IKey,
     * com.gdo.stencils.plug.PStencil)
     */
    @Override
    public PStcl clone(StclContext stclContext, PSlot<StclContext, PStcl> slot, IKey key, PStcl self) {
        StclFactory factory = (StclFactory) stclContext.getStencilFactory();
        PStcl clone = factory.cloneCommand(stclContext, slot, key, self);
        CommandStcl cmd = clone.getReleasedStencil(stclContext);
        cmd._defParams = _defParams;
        return clone;
    }

    /**
     * Returns the path of some resources depending on the project
     * configuration.
     * 
     * @param stclContext
     *            the stencil context.
     * @param resources
     *            the resources searched.
     * @param self
     *            this stencil as a plugged stencil.
     * @return the resources slot path.
     */
    protected String getResourcePath(StclContext stclContext, String resources, PStcl self) {
        ProjectStcl project = stclContext.getServletStcl().getReleasedStencil(stclContext);
        return project.getResourcePath(stclContext, resources, self);
    }

    /**
     * Returns <tt>true</tt>, if and only if, the action is atomic (an atomic
     * action has no facet view).
     * 
     * @return <tt>true</tt> if the action is atomic, <tt>else</tt> otherwise.
     */
    abstract public boolean isAtomic();

    // SHOULD BE UNUSEFULL!!!
    @Override
    public FacetResult getFacet(RenderContext<StclContext, PStcl> renderContext) {
        StclContext stclContext = renderContext.getStencilContext();
        PStcl self = renderContext.getStencilRendered();
        String facet = renderContext.getFacetType();

        // HTML 5 case
        if (FacetType.HTML5.equals(facet)) {
            String skel = renderContext.getFacetMode();
            HTML5SectionCompleter completer = new HTML5SectionCompleter();
            return completer.getFacetFromSkeleton(stclContext, self, skel);
        }
        if (FacetType.DOM5.equals(facet)) {
            String dom = renderContext.getFacetMode();
            HTML5SectionCompleter completer = new HTML5SectionCompleter();
            return completer.getFacetFromDOM(stclContext, self, dom);
        }

        // JSON case
        if (FacetType.JSON.equals(facet)) {
            String mode = renderContext.getFacetMode();
            JSONSectionCompleter completer = new JSONSectionCompleter();
            return completer.getFacetFromDOM(stclContext, self, mode);
        }
        if (FacetType.JSKEL.equals(facet)) {
            String mode = renderContext.getFacetMode();
            JSONSectionCompleter completer = new JSONSectionCompleter();
            return completer.getFacetFromSkeleton(stclContext, self, mode);
        }

        // PYTHON case
        if (FacetType.PYTHON.equals(facet)) {
            String mode = renderContext.getFacetMode();
            PythonSectionCompleter completer = new PythonSectionCompleter();
            return completer.getFacetFromDOM(stclContext, self, mode);
        }

        // generator mask case
        if (FacetType.MASK.equals(facet)) {
            String mode = renderContext.getFacetMode();
            PStcl generator = self.getStencil(stclContext, Slot.GENERATOR);
            if (StencilUtils.isNull(generator)) {
                String msg = String.format("Mask facet must be used with a generator not defined in %s (mode %s) : %s", this, mode, generator.getNullReason());
                return new FacetResult(FacetResult.ERROR, msg, null);
            }
            IMaskFacetGenerator gen = (IMaskFacetGenerator) generator.getReleasedStencil(stclContext);
            InputStream reader = gen.getFacet(stclContext, mode, self, generator, generator);
            return new FacetResult(reader, "text/plain");
        }

        // for flex type, the content is defined specifically to be able to set
        // created stencil mode
        String type = renderContext.getFacetType();
        if (FacetType.FLEX.equals(type)) {
            String mode = renderContext.getFacetMode();
            String name = getClass().getName();
            int index = name.lastIndexOf('.');
            name = name.substring(0, index) + "::" + name.substring(index + 1);
            String xml = String.format("<flex><className>%s</className><initialState>%s</initialState></flex>", name, mode);
            InputStream reader = IOUtils.toInputStream(xml);
            FacetResult result = new FacetResult(reader, "text/plain");
            result.setContentLength(xml.length());
            return result;
        }

        // as before
        return super.getFacet(renderContext);
    }

    @Override
    public void multipart(StclContext stclContext, String fileName, FileItem item, PStcl self) throws Exception {
        // by default receiving data does nothing
    }

    /**
     * Path prop value is calculated from slot's path.
     */
    private class PwdSlot extends CalculatedStringPropertySlot<StclContext, PStcl> {

        public PwdSlot(StclContext stclContext, CommandStcl in, String name) {
            super(stclContext, in, name);
        }

        @Override
        public String getValue(StclContext stclContext, PStcl self) {
            return self.getContainer(stclContext).pwd(stclContext);
        }

        @Override
        public String setValue(StclContext stclContext, String value, PStcl self) {
            String msg = String.format("Cannot change %s value", Slot.$PWD);
            throw new IllegalStateException(msg);
        }
    }

}
