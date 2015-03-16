package com.gdo.project.model;

import org.apache.poi.ss.formula.eval.NotImplementedException;

import com.gdo.project.cmd.CommandAction;
import com.gdo.project.cmd.Nothing;
import com.gdo.stencils.CommandStcl;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.key.IKeyGenerator;
import com.gdo.stencils.key.StringKeyGenerator;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.CalculatedIntegerPropertySlot;
import com.gdo.stencils.slot.SingleCalculatedSlot;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;

/**
 * <p>
 * A composed action is a command with steps and having performing views.
 * </p>
 */
public abstract class ComposedActionStcl extends CommandStcl {

    public interface Slot extends CommandStcl.Slot {
        String STEPS = "Steps";
        String ACTIVE_STEP = "ActiveStep";
        String ACTIVE_STEP_INDEX = "ActiveStepIndex";
    }

    public interface Command extends CommandStcl.Command {
        String GOTO_STEP = "GotoStep";
        String PREVIOUS_STEP = "PreviousStep";
        String NEXT_STEP = "NextStep";
        String RESET = "Reset";
        String CANCEL = "Cancel";

        // a composed action may contains slot, prop.. so update command may be
        // called if one change
        String UPDATE = Stcl.Command.UPDATE;
    }

    public interface Status extends CommandStcl.Status {
        int LAUNCH_PATH = 1;
        int LAUNCHED = 2;
        int WRONG_STEP = 3;
    }

    // index of the first step
    public static final int FIRST_STEP = 1;

    private int _activeStepIndex = FIRST_STEP; // the index of the current step
    private int _previousStepIndex = 0; // the index of the previous step

    /**
     * Constructor
     * 
     * @param stclContext
     *            the stencil context.
     */
    public ComposedActionStcl(StclContext stclContext) {
        super(stclContext);

        multiSlot(Slot.STEPS);
        new ActiveStepSlot(stclContext);
        new ActiveStepIndexSlot(stclContext);

        command(Command.GOTO_STEP, CommandAction.class, 0);
        command(Command.PREVIOUS_STEP, CommandAction.class, 0, -1);
        command(Command.NEXT_STEP, CommandAction.class, 0, 1);
        command(Command.RESET, CommandAction.class, 1);
        command(Command.CANCEL, CommandAction.class, 2);

        command(Command.UPDATE, Nothing.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gdo.project.CommandStcl#isAtomic()
     */
    @Override
    public final boolean isAtomic() {
        return false;
    }

    @Override
    public PStcl clone(StclContext stclContext, PSlot<StclContext, PStcl> slot, IKey key, PStcl self) {
        PStcl clone = super.clone(stclContext, slot, key, self);
        ComposedActionStcl cmd = (ComposedActionStcl) clone.getReleasedStencil(stclContext);
        cmd._activeStepIndex = FIRST_STEP;
        return clone;
    }

    /**
     * Launches the command at a given path. If the path is a slot path then
     * giving first available key.
     * 
     * @return The launch path at index Status.LAUNCH_PATH and comand status in
     *         complement.
     */
    public CommandStatus<StclContext, PStcl> launch(CommandContext<StclContext, PStcl> cmdContext, String path, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();

        // if the path contains a key, then removes previous launched command at
        // this path if one
        String launchedPath;
        if (PathUtils.isKeyContained(path)) {
            self.unplugOtherStencilFrom(stclContext, path); // if one plugged,
            // should cancel?
            launchedPath = path;
        } else {
            PSlot<StclContext, PStcl> slot = self.getSlot(stclContext, path);
            IKeyGenerator keyGen = new StringKeyGenerator<StclContext, PStcl>(stclContext, "a", slot);
            launchedPath = PathUtils.createPath(path, keyGen.getKey().toString());
        }

        // plugs the new instance to store it until it terminates
        PStcl launched = self.plug(stclContext, self, launchedPath);
        if (StencilUtils.isNull(launched)) {
            return error(cmdContext, self, launched.getResult());
        }

        // executes it
        ComposedActionStcl launchedStcl = (ComposedActionStcl) launched.getReleasedStencil(stclContext);
        CommandStatus<StclContext, PStcl> status = launchedStcl.execute(cmdContext, launched);

        // returns status, current lauched command and lauching path
//        String prefix = ComposedActionStcl.class.getName();
        //status = new CommandStatus<StclContext, PStcl>(prefix, CommandStatus.SUCCESS, Status.LAUNCHED, launched, status);
        //return new CommandStatus<StclContext, PStcl>(prefix, CommandStatus.SUCCESS, Status.LAUNCH_PATH, launchedPath, status);
        CommandStatus<StclContext, PStcl> s = new CommandStatus<StclContext, PStcl>(ComposedActionStcl.class.getName(), CommandStatus.SUCCESS, Status.LAUNCHED, launched, null);
        s = new CommandStatus<StclContext, PStcl>(ComposedActionStcl.class.getName(), CommandStatus.SUCCESS, Status.LAUNCH_PATH, launchedPath, s);
        status.addOther(s);
        return status;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gdo.stencils.cmd.CommandStencil#doAction(com.gdo.stencils.cmd.
     * CommandContext, com.gdo.stencils.plug.PStencil) In case of composed action,
     * the action performed is done by step by step.
     */
    @Override
    public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {

        // performs action steps
        CommandStatus<StclContext, PStcl> status = performSteps(cmdContext, self);

        return status;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gdo.stencils.cmd.CommandStencil#verifyContext(com.gdo.stencils.cmd
     * .CommandContext, com.gdo.stencils.plug.PStencil) In a composed action, this
     * code is checked each time the step is changing. In case the status returned
     * is not success, the action will not goes to next step.
     */
    @Override
    protected CommandStatus<StclContext, PStcl> verifyContext(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        return super.verifyContext(cmdContext, self);
    }

    // should be overriden for specific bahaviour
    public abstract CommandStatus<StclContext, PStcl> performSteps(CommandContext<StclContext, PStcl> cmdContext, PStcl self);

    /**
     * @return The current active step.
     */
    public int getActiveStepIndex() {
        return _activeStepIndex;
    }

    /**
     * @return The previous active step.
     */
    public int getPreviousStepIndex() {
        return _previousStepIndex;
    }

    /**
     * Reset the composed action.
     * 
     * @return The status of reseted command excecution.
     */
    @Override
    public CommandStatus<StclContext, PStcl> reset(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        _activeStepIndex = FIRST_STEP;
        return execute(cmdContext, self);
    }

    /**
     * Removes this command from all slots.
     */
    public CommandStatus<StclContext, PStcl> cancel(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        self.unplugFromAllSlots(cmdContext.getStencilContext());
        return success(cmdContext, self);
    }

    /**
     * @param increment
     * @return Command status of the incrementation possibility.
     */
    protected CommandStatus<StclContext, PStcl> beforeIncrementStep(CommandContext<StclContext, PStcl> cmdContext, int increment, PStcl self) {
        return success(cmdContext, self);
    }

    /**
     * Increments the current step index (may be negative).
     * 
     * @return Execution status.
     */
    public CommandStatus<StclContext, PStcl> incrementStep(CommandContext<StclContext, PStcl> cmdContext, int increment, PStcl self) {

        // the command context must be changed before calling composed action
        // methods
        StclContext stclContext = cmdContext.getStencilContext();
        PStcl target = _cmdContext.getTarget();
        CommandContext<StclContext, PStcl> newCmdContext = new CommandContext<StclContext, PStcl>(stclContext, target);

        // first test if next step is valid
        CommandStatus<StclContext, PStcl> before = beforeIncrementStep(newCmdContext, increment, self);
        if (!before.isSuccess())
            return before;

        // increment active step and execute command
        try {

            // does incrementation
            doIncrement(increment);

            // execute command
            CommandStatus<StclContext, PStcl> status = execute(newCmdContext, self);
            if (status.isNotSuccess())
                _activeStepIndex -= increment;

            // once terminated removes it from executable slot
            if (isTerminated(stclContext, self)) {
                self.unplugFromAllSlots(stclContext);
            }
            return status;
        } catch (Exception e) {
            _activeStepIndex -= increment;
            return error(cmdContext, self, e);
        }
    }

    /**
     * @return <code>true</code> if the composed command is terminated.
     */
    public boolean isTerminated(StclContext stclContext, PStcl self) {
        return getActiveStepIndex() > self.getStencils(stclContext, Slot.STEPS).size();
    }

    /**
     * Performs the increment on active step.
     * 
     * @param increment
     *            incrementation value.
     */
    protected void doIncrement(int increment) {
        _previousStepIndex = _activeStepIndex;
        _activeStepIndex += increment;
    }

    // contains current active step
    private class ActiveStepSlot extends SingleCalculatedSlot<StclContext, PStcl> {
        public ActiveStepSlot(StclContext stclContext) {
            super(stclContext, ComposedActionStcl.this, Slot.ACTIVE_STEP, PSlot.ONE);
        }

        @Override
        public boolean hasStencils(StclContext stclContext, StencilCondition<StclContext, PStcl> condition, PSlot<StclContext, PStcl> self) {
            return (getActiveStepIndex() > 0 && !isTerminated(stclContext, self.getContainer()));
        }

        @Override
        public PStcl getCalculatedStencil(StclContext stclContext, StencilCondition<StclContext, PStcl> condition, PSlot<StclContext, PStcl> self) {
            String stepPath = PathUtils.createPath(Slot.STEPS, getActiveStepIndex());
            return self.getContainer().getStencil(stclContext, stepPath);
        }
    }

    // contains current active step index
    private class ActiveStepIndexSlot extends CalculatedIntegerPropertySlot<StclContext, PStcl> {
        public ActiveStepIndexSlot(StclContext stclContext) {
            super(stclContext, ComposedActionStcl.this, Slot.ACTIVE_STEP_INDEX);
        }

        @Override
        public int getIntegerValue(StclContext stclContext, PStcl self) {
            return getActiveStepIndex();
        }

        @Override
        public int setIntegerValue(StclContext stclContext, int value, PStcl self) {
            throw new NotImplementedException("Cannot change active step index directly");
        }
    }
}
