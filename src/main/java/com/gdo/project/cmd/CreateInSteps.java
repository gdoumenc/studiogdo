/*
 * Copyright GDO - 2004
 */
package com.gdo.project.cmd;

import org.apache.commons.lang3.StringUtils;

import com.gdo.helper.ClassHelper;
import com.gdo.project.model.ComposedActionStcl;
import com.gdo.stencils.CommandStcl;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.factory.StencilFactory;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.SingleSlot;
import com.gdo.stencils.util.SlotUtils;
import com.gdo.stencils.util.StencilUtils;

/**
 * Creates a stencil and plug created stencil in a slot. The java methods
 * createStencil and beforePlug may be redefined for specific stencil creation.
 * <p>
 * Parameters are :
 * <ol>
 * <li>stencil created class (stencil template full name)</li>
 * <li>destination slot path (where the stencil created will be plugged in -
 * relative to action)</li>
 * <li>key type (none or string, int, uniquefor unicity - default : none)</li>
 * <li>initial key value (default 1 for int, "a" for string)</li>
 * <li>initial parameter for constructor if needed</li>
 * </ol>
 * </p>
 */
public abstract class CreateInSteps extends ComposedActionStcl {

    public interface Slot extends ComposedActionStcl.Slot {
        String STENCIL_HOLDER = "StencilHolder";
        String PLUGGED_STENCIL_HOLDER = "PluggedStencilHolder";
    }

    protected String _template; // template class name (defined from parameter)
    protected PSlot<StclContext, PStcl> _slot; // slot where the stencil will be
    // plugged (defined from
    // parameter)
    protected String _type; // key type (defined from parameter)
    protected PStcl _created; // stencil created
    protected PStcl _plugged; // stencil created

    /**
     * Constructor.
     * 
     * @param stclContext
     *            the stencil context.
     */
    public CreateInSteps(StclContext stclContext) {
        super(stclContext);

        new StencilHolderSlot(stclContext, this, Slot.STENCIL_HOLDER);
        new PluggedStencilHolderSlot(stclContext, this, Slot.PLUGGED_STENCIL_HOLDER);
    }

    /**
     * Returns the index of the creation step (the step where the stencil is
     * created).
     * 
     * @return the index of the creation step.
     */
    protected abstract int getCreationStep();

    /**
     * Returns the index of the plug step (the step where the created stencil is
     * plugged into its destination).
     * 
     * @return the index of the plug step.
     */
    protected abstract int getPlugStep();

    /*
     * (non-Javadoc)
     * 
     * @see com.gdo.project.model.ComposedActionStcl#performSteps(com.gdo.stencils
     * .cmd.CommandContext, com.gdo.project.PStcl)
     */
    @Override
    public CommandStatus<StclContext, PStcl> performSteps(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        int currentStep = getActiveStepIndex();
        CommandStatus<StclContext, PStcl> status = success(cmdContext, self);

        // creates the stencil by supplying its constructor value if defined
        // (creates it only on start)
        if (currentStep == getCreationStep() && getPreviousStepIndex() == getCreationStep() - 1) {
            status = createStencil(cmdContext, self);
            if (status.isNotSuccess()) {
                return status;
            }
        }

        // plugs the created stencil after validation
        if (currentStep == getPlugStep() && getPreviousStepIndex() == getPlugStep() - 1) {
            status = plugStencil(cmdContext, self);
            if (status.isNotSuccess()) {
                return status;
            }
        }

        return status;
    }

    protected CommandStatus<StclContext, PStcl> createStencil(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();

        // calls before creation
        CommandStatus<StclContext, PStcl> before = beforeCreate(cmdContext, self);
        if (before.isNotSuccess()) {
            return error(cmdContext, self, 0, before);
        }

        // creates the stencil
        String valueForConstructor = getValueForConstructor(cmdContext, self);
        Class<? extends Stcl> clazz = ClassHelper.loadClass(_template);
        if (valueForConstructor != null) {
            _created = self.newPStencil(stclContext, Slot.STENCIL_HOLDER, Key.NO_KEY, clazz, valueForConstructor);
        } else {
            _created = self.newPStencil(stclContext, Slot.STENCIL_HOLDER, Key.NO_KEY, clazz);
        }
        if (StencilUtils.isNull(_created)) {
            return error(cmdContext, self, _created.getNullReason());
        }

        // calls after creation
        CommandStatus<StclContext, PStcl> after = afterCreate(cmdContext, _created, self);
        if (after.isNotSuccess()) {
            return error(cmdContext, self, 0, after);
        }

        return success(cmdContext, self);
    }

    protected CommandStatus<StclContext, PStcl> plugStencil(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {

        // calls before plug
        CommandStatus<StclContext, PStcl> before = beforePlug(cmdContext, _created, self);
        if (before.isNotSuccess()) {
            return error(cmdContext, self, 0, before);
        }

        String key = getKey(cmdContext, self);
        _plugged = CreateAtomic.plugCreatedStencil(cmdContext, _created, _slot, _type, key, self);
        if (StencilUtils.isNull(_plugged)) {
            String msg = String.format("was not able to plug %s in %s:%s", _created, _slot, StencilUtils.getNullReason(_plugged));
            return error(cmdContext, self, msg);
        }

        // calls after plug
        CommandStatus<StclContext, PStcl> after = afterPlug(cmdContext, _plugged, self);
        if (after.isNotSuccess()) {
            return error(cmdContext, self, 0, after);
        }

        // set reate same as plugged
        _created = _plugged;

        // returns the plugged path
        return success(cmdContext, self);
    }

    @Override
    public CommandStatus<StclContext, PStcl> reset(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();
        self.unplugOtherStencilFrom(stclContext, Slot.STENCIL_HOLDER);
        return super.reset(cmdContext, self);
    }

    /**
     * Removes this command from all slots.
     */
    @Override
    public CommandStatus<StclContext, PStcl> cancel(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();
        self.unplugOtherStencilFrom(stclContext, Slot.STENCIL_HOLDER);
        return super.cancel(cmdContext, self);
    }

    /**
     * May be overriden for specific bahaviour.
     * 
     * @param cmdContext
     *            the command context.
     * @param self
     *            the command as a plugged command.
     * @return the execution status.
     */
    protected CommandStatus<StclContext, PStcl> beforeCreate(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        return success(cmdContext, self);
    }

    /**
     * May be overriden for specific bahaviour.
     * 
     * @param cmdContext
     *            the command context.
     * @param created
     *            the stencil created.
     * @param self
     *            the command as a plugged command.
     * @return the execution status.
     */
    protected CommandStatus<StclContext, PStcl> afterCreate(CommandContext<StclContext, PStcl> cmdContext, PStcl created, PStcl self) {
        return success(cmdContext, self);
    }

    /**
     * May be overriden for specific bahaviour.
     * 
     * @param cmdContext
     *            the command context.
     * @param created
     *            the stencil created.
     * @param self
     *            the command as a plugged command.
     * @return the execution status.
     */
    protected CommandStatus<StclContext, PStcl> beforePlug(CommandContext<StclContext, PStcl> cmdContext, PStcl plugged, PStcl self) {
        return success(cmdContext, self);
    }

    /**
     * May be overriden for specific bahaviour.
     * 
     * @param cmdContext
     *            the command context.
     * @param created
     *            the stencil created.
     * @param self
     *            the command as a plugged command.
     * @return the execution status.
     */
    protected CommandStatus<StclContext, PStcl> afterPlug(CommandContext<StclContext, PStcl> cmdContext, PStcl plugged, PStcl self) {
        return success(cmdContext, self);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.gdo.project.model.ComposedActionStcl#verifyContext(com.gdo.stencils
     * .cmd.CommandContext, com.gdo.project.PStcl)
     */
    @Override
    protected CommandStatus<StclContext, PStcl> verifyContext(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        PStcl target = cmdContext.getTarget();
        int activeStep = getActiveStepIndex();

        // checks initial parameters
        if (activeStep == FIRST_STEP) {

            // checks parameters
            _template = getTemplate(cmdContext, self);
            if (StringUtils.isBlank(_template)) {
                return error(cmdContext, self, "no template defined for create in one step command (param1)");
            }
            _slot = getSlot(cmdContext, self);
            if (SlotUtils.isNull(_slot)) {
                String msg = String.format("cannot get slot in %s for create in one step command (param2)", target);
                return error(cmdContext, self, msg);
            }
            _type = getType(cmdContext, self);
            if (StringUtils.isEmpty(_type)) {
                return error(cmdContext, self, "no key type defined for create in one step command (param3)");
            }
        }

        // no specific check on other steps
        return super.verifyContext(cmdContext, self);
    }

    protected String getTemplate(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        return getParameter(cmdContext, 1, null);
    }

    /**
     * Gets the slot where the stencil is plugged.
     * 
     * @param cmdContext
     *            the command context.
     * @param self
     *            the command as a plugged stencil.
     * @return the slot name.
     */
    protected PSlot<StclContext, PStcl> getSlot(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();
        String slot = getParameter(cmdContext, 2, null);
        return self.getSlot(stclContext, slot);
    }

    protected String getType(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        return getParameter(cmdContext, 3, null);
    }

    protected String getKey(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        return getParameter(cmdContext, 4, null);
    }

    protected String getValueForConstructor(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        return getParameter(cmdContext, 5, null);
    }

    private class StencilHolderSlot extends SingleSlot<StclContext, PStcl> {

        public StencilHolderSlot(StclContext stclContext, CommandStcl in, String name) {
            super(stclContext, in, name);
        }

        @Override
        protected PStcl getContainedStencilOrCreateDefault(StclContext stclContext, PSlot<StclContext, PStcl> self) {
            if (StencilUtils.isNotNull(CreateInSteps.this._plugged)) {
                return CreateInSteps.this._plugged;
            }
            return CreateInSteps.this._created;
        }

    }

    private class PluggedStencilHolderSlot extends SingleSlot<StclContext, PStcl> {

        public PluggedStencilHolderSlot(StclContext stclContext, CommandStcl in, String name) {
            super(stclContext, in, name);
        }

        @Override
        protected PStcl getContainedStencilOrCreateDefault(StclContext stclContext, PSlot<StclContext, PStcl> self) {
            StencilFactory<StclContext, PStcl> factory = (StencilFactory<StclContext, PStcl>) stclContext.getStencilFactory();
            PStcl stcl = null;
            if (StencilUtils.isNotNull(CreateInSteps.this._plugged)) {
                stcl = CreateInSteps.this._plugged;
            } else {
                stcl = CreateInSteps.this._created;
            }
            return factory.createPStencil(stclContext, self, Key.NO_KEY, stcl);
        }

    }
}