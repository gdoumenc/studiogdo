/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.gdo.helper.ClassHelper;
import com.gdo.project.cmd.CreateAtomic;
import com.gdo.project.cmd.Eval;
import com.gdo.project.cmd.Load;
import com.gdo.project.cmd.Plug;
import com.gdo.project.cmd.Plugc;
import com.gdo.project.cmd.Save;
import com.gdo.project.cmd.Trace;
import com.gdo.project.cmd.Unplug;
import com.gdo.project.model.ComposedActionStcl;
import com.gdo.project.model.ProjectStcl;
import com.gdo.project.slot.RootSlot;
import com.gdo.reflect.CommandsSlot;
import com.gdo.reflect.PwdSlot;
import com.gdo.reflect.SlotsSlot;
import com.gdo.reflect.TemplateNameSlot;
import com.gdo.reflect.WhereSlot;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.cmd.Lock;
import com.gdo.stencils.cmd.Unlock;
import com.gdo.stencils.faces.RenderContext;
import com.gdo.stencils.facet.FacetResult;
import com.gdo.stencils.facet.FacetType;
import com.gdo.stencils.factory.StclFactory;
import com.gdo.stencils.factory.StencilFactory;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.CalculatedBooleanPropertySlot;
import com.gdo.stencils.slot.CalculatedStringPropertySlot;
import com.gdo.stencils.slot._Slot;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;

/**
 * <p>
 * Basic implementation of the studiogdo stencil (the M of the MVC model).
 * </p>
 * <p>
 * Should be used only in a plugged form {@link PStcl} or directly for java
 * interface.
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
public class Stcl extends _Stencil<StclContext, PStcl> {

    public interface Slot extends _Stencil.Slot {
        String $REF = "$Ref";
        String $TEMPLATE_NAME = "$TemplateName";
        String $PWD = "$Pwd";
        String $KEY = "$Key";
        String $SLOTS = "$Slots";
        String $COMMANDS = "$Commands";
        String $WHERE = "$Where";

        String $IS_LOCKED = "$IsLocked";
        String $LOCKED_BY = "$LockedBy";

        String GENERATOR = "Generator";
        String ACTIVE_ACTIONS = "ActiveActions";
    }

    public interface Command extends _Stencil.Command {
        String $LOCK = "$Lock";
        String $UNLOCK = "$Unlock";

        String CREATE_ATOMIC = "CreateAtomic";
        String PLUG = "Plug";
        String PLUG_C = "Plugc"; // coded plug
        String UNPLUG = "Unplug";

        String EVAL = "Eval";

        String SAVE = "Save";
        String LOAD = "Load";

        String UPDATE = "Update";
    }

    public interface IMaskFacetGenerator {
        InputStream getFacet(StclContext stclContext, String mode, PStcl on, PStcl main, PStcl self);
    }

    public Stcl(StclContext stclContext) {
        super(stclContext);

        // SLOT PART

        // global slots
        singleSlot(Slot.GENERATOR);
        singleSlot(Slot.ACTIVE_ACTIONS);

        // reflexive slots
        createTemplateNameSlot(stclContext);
        createPwdSlot(stclContext);
        createKeySlot(stclContext);
        createSlotSlot(stclContext);
        createCommandSlot(stclContext);
        createWhereSlot(stclContext);

        addDescriptor(Slot.$IS_LOCKED, IsLockedSlot.class);
        singleSlot(Slot.$LOCKED_BY);

        // COMMAND PART

        command(Command.$LOCK, Lock.class);
        command(Command.$UNLOCK, Unlock.class);

        command(Command.CREATE_ATOMIC, CreateAtomic.class);
        command(Command.PLUG, Plug.class);
        command(Command.PLUG_C, Plugc.class);
        command(Command.UNPLUG, Unplug.class);
        command(Command.EVAL, Eval.class);
        command(Command.SAVE, Save.class);
        command(Command.LOAD, Load.class);
        command(Command.UPDATE, Trace.class);
    }

    /**
     * Creates a null plugged stencil with the reason why.
     * 
     * @param stclContext
     *            the stencil context.
     * @param reasons
     *            the error results which produced the null plugged stencil.
     * @return a null plugged stencil.
     */
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

    protected _Slot<StclContext, PStcl> createKeySlot(StclContext stclContext) {
        return new KeySlot(stclContext, this, Slot.$KEY);
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
     * com.gdo.stencils.key.IKey, com.gdo.stencils.plug.PStencil)
     */
    @Override
    public PStcl clone(StclContext stclContext, PSlot<StclContext, PStcl> slot, IKey key, PStcl self) {
        StclFactory factory = (StclFactory) stclContext.getStencilFactory();
        return factory.cloneStencil(stclContext, slot, key, self);
    }

    public CommandStatus<StclContext, PStcl> launch(StclContext stclContext, String name, String path, PStcl self, Object... params) throws Exception {

        // create context with parameters
        CommandContext<StclContext, PStcl> cmdContext = new CommandContext<StclContext, PStcl>(stclContext, self);
        for (int i = 0; i < params.length; i++) {
            cmdContext.setRedefinedParameter(CommandContext.PARAMS[i], params[i]);
        }

        // execute the command
        return launch(cmdContext, name, path, self);
    }

    public CommandStatus<StclContext, PStcl> launch(CommandContext<StclContext, PStcl> cmdContext, String name, String path, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();

        // get command stencil
        PStcl cmdStcl = getCommand(stclContext, name, self);
        if (StencilUtils.isNull(cmdStcl)) {
            String prefix = ComposedActionStcl.class.getName();
            String msg = String.format("Command %s not defined for %s", name, self);
            return new CommandStatus<StclContext, PStcl>(prefix, CommandStatus.ERROR, 0, msg, null);
        }
        if (!(cmdStcl.getReleasedStencil(stclContext) instanceof ComposedActionStcl)) {
            String prefix = ComposedActionStcl.class.getName();
            String msg = "can launch only a composed command";
            return new CommandStatus<StclContext, PStcl>(prefix, CommandStatus.ERROR, 0, msg, null);
        }

        // create the new command context
        CommandContext<StclContext, PStcl> newContext = cmdContext.clone();
        newContext.setTarget(cmdStcl.getContainer(stclContext));

        // execute the command
        return ((ComposedActionStcl) cmdStcl.getReleasedStencil(stclContext)).launch(newContext, path, cmdStcl);
    }
    
    public class IsLockedSlot extends CalculatedBooleanPropertySlot<StclContext, PStcl> {
        
        public IsLockedSlot(StclContext stclContext) {
            super(stclContext, Stcl.this, Slot.$IS_LOCKED);
        }

        @Override
        public boolean getBooleanValue(StclContext stclContext, PStcl self) {
            return StencilUtils.isNull(self.getContainer(stclContext).getStencil(stclContext, Slot.$LOCKED_BY));
        }
        
    }

    // --------------------------------------------------------------------------
    //
    // Resource management.
    //
    // --------------------------------------------------------------------------

    public static String getResourcePath(StclContext stclContext, String resource) {
        PStcl servlet = stclContext.getServletStcl();
        ProjectStcl project = servlet.getReleasedStencil(stclContext);
        return project.getResourcePath(stclContext, resource, servlet);
    }

    /**
     * Key prop value .
     */
    private class KeySlot extends CalculatedStringPropertySlot<StclContext, PStcl> {

        public KeySlot(StclContext stclContext, Stcl in, String name) {
            super(stclContext, in, name);
        }

        @Override
        public String getValue(StclContext stclContext, PStcl self) {
            return self.getContainer(stclContext).getKey().toString();
        }
    }

    @Override
    public FacetResult getFacet(RenderContext<StclContext, PStcl> renderContext) {

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

        return super.getFacet(renderContext);
    }

    // --------------------------------------------------------------------------
    //
    // LOG
    //
    // --------------------------------------------------------------------------

    public String logTrace(StclContext stclContext, String format, Object... params) {
        if (stclContext != null && stclContext.getRequest() != null && stclContext.getRequest().getSession() != null) {
            String session_id = stclContext.getRequest().getSession().getId();
            format += " -- [session:" + session_id + "] ";
        }
        return super.logTrace(stclContext, format, params);
    }

    public String logWarn(StclContext stclContext, String format, Object... params) {
        if (stclContext != null && stclContext.getRequest() != null && stclContext.getRequest().getSession() != null) {
            String session_id = stclContext.getRequest().getSession().getId();
            format += " -- [session:" + session_id + "] ";
        }
        return super.logWarn(stclContext, format, params);
    }

    public String logError(StclContext stclContext, String format, Object... params) {
        if (stclContext != null && stclContext.getRequest() != null && stclContext.getRequest().getSession() != null) {
            String session_id = stclContext.getRequest().getSession().getId();
            format += " -- [session:" + session_id + "] ";
        }
        return super.logError(stclContext, format, params);
    }

}