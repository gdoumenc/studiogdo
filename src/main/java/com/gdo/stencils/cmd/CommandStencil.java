/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.cmd;

import java.util.Map;

import com.gdo.helper.ConverterHelper;
import com.gdo.stencils.Keywords;
import com.gdo.stencils.Result;
import com.gdo.stencils._Stencil;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.factory.StencilFactory;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.slot.SingleCalculatedSlot;
import com.gdo.stencils.util.StencilUtils;
import com.gdo.util.ArrayMap;

/**
 * <p>
 * A command stencil is a stencil which can perform an action.
 * </p>
 * <p>
 * A command stencil has a target stencil on which the action will be performed.
 * The <tt>Target</tt> slot contains this stencil. The <tt>PluggedTarget</tt>
 * slot contains this stencil but with this command as parent.
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
 * @see com.gdo.stencils.cmd.CommandContext Context
 */
public abstract class CommandStencil<C extends _StencilContext, S extends _PStencil<C, S>> extends _Stencil<C, S> {

	public static final String PARAM_PREFIX = "param";

	public interface Slot extends _Stencil.Slot {
		String TARGET = "Target";
		String PLUGGED_TARGET = "PluggedTarget";
	}

	public interface Status {
		// result status of a command}
	}

	// default parameters if not defined in context
	public Map<String, Object> _defParams = new ArrayMap<String, Object>();

	// default context used when methods (except call method) are called not in
	// one call (thread)
	protected CommandContext<C, S> _cmdContext;

	/**
	 * @return a command status from a result.
	 */
	@Deprecated
	public final CommandStatus<C, S> result(CommandStencil<?, ?> cmd, Result result) {
		return new CommandStatus<C, S>(cmd.getClass().getName(), result.getStatus(), 0, null, result);
	}

	public final CommandStatus<C, S> toCommandStatus(CommandContext<C, S> cmdContext, S cmd, Result result) {
		return new CommandStatus<C, S>(cmd.getName(cmdContext.getStencilContext()), result.getStatus(), 0, null, result);
	}

	/**
	 * self may be null if command cancelled (stencil cleared)
	 * 
	 * @return a success status with value.
	 */
	@Deprecated
	public final CommandStatus<C, S> success(CommandStencil<?, ?> cmd) {
		return new CommandStatus<C, S>(cmd.getClass().getName(), CommandStatus.SUCCESS, 0, null, null);
	}

	@Deprecated
	public final CommandStatus<C, S> success(CommandStencil<?, ?> cmd, Object value) {
		return new CommandStatus<C, S>(cmd.getClass().getName(), CommandStatus.SUCCESS, 0, value, null);
	}

	@Deprecated
	public final CommandStatus<C, S> success(CommandStencil<?, ?> cmd, Result other) {
		return new CommandStatus<C, S>(cmd.getClass().getName(), CommandStatus.SUCCESS, 0, null, other);
	}

	@Deprecated
	public final CommandStatus<C, S> success(CommandStencil<?, ?> cmd, int id, Object value) {
		return new CommandStatus<C, S>(cmd.getClass().getName(), CommandStatus.SUCCESS, id, value, null);
	}

	@Deprecated
	public final CommandStatus<C, S> success(CommandStencil<?, ?> cmd, int id, Object value, Result other) {
		return new CommandStatus<C, S>(cmd.getClass().getName(), CommandStatus.SUCCESS, id, value, other);
	}

	public final CommandStatus<C, S> success(CommandContext<C, S> cmdContext, S self) {
		C stclContext = cmdContext.getStencilContext();
		String name = (self.isNotNull()) ? self.getName(stclContext) : "";
		return new CommandStatus<C, S>(name, CommandStatus.SUCCESS, 0, null, null);
	}

	public final CommandStatus<C, S> success(CommandContext<C, S> cmdContext, S self, Object value) {
		C stclContext = cmdContext.getStencilContext();
		String name = (self.isNotNull()) ? self.getName(stclContext) : "";
		return new CommandStatus<C, S>(name, CommandStatus.SUCCESS, 0, value, null);
	}

	public final CommandStatus<C, S> success(CommandContext<C, S> cmdContext, S self, Result other) {
		C stclContext = cmdContext.getStencilContext();
		String name = (self.isNotNull()) ? self.getName(stclContext) : "";
		return new CommandStatus<C, S>(name, CommandStatus.SUCCESS, 0, null, other);
	}

	public final CommandStatus<C, S> success(CommandContext<C, S> cmdContext, S self, int id, Object value) {
		C stclContext = cmdContext.getStencilContext();
		String name = (self.isNotNull()) ? self.getName(stclContext) : "";
		return new CommandStatus<C, S>(name, CommandStatus.SUCCESS, id, value, null);
	}

	public final CommandStatus<C, S> success(CommandContext<C, S> cmdContext, S self, int id, Object value, Result other) {
		C stclContext = cmdContext.getStencilContext();
		String name = (self.isNotNull()) ? self.getName(stclContext) : "";
		return new CommandStatus<C, S>(name, CommandStatus.SUCCESS, id, value, other);
	}

	// boolean value
	@Deprecated
	public final CommandStatus<C, S> success(CommandStencil<?, ?> cmd, boolean value) {
		return new CommandStatus<C, S>(cmd.getClass().getName(), CommandStatus.SUCCESS, 0, value, null);
	}

	@Deprecated
	public final CommandStatus<C, S> success(CommandStencil<?, ?> cmd, boolean value, String msg) {
		CommandStatus<C, S> comp = new CommandStatus<C, S>(cmd.getClass().getName(), CommandStatus.SUCCESS, 2, msg, null);
		return new CommandStatus<C, S>(cmd.getClass().getName(), CommandStatus.SUCCESS, 0, value, comp);
	}

	public final CommandStatus<C, S> success(CommandContext<C, S> cmdContext, S self, boolean value) {
		C stclContext = cmdContext.getStencilContext();
		String name = (self.isNotNull()) ? self.getName(stclContext) : "";
		return new CommandStatus<C, S>(name, CommandStatus.SUCCESS, 0, value, null);
	}

	public final CommandStatus<C, S> success(CommandContext<C, S> cmdContext, S self, boolean value, String msg) {
		C stclContext = cmdContext.getStencilContext();
		String name = (self.isNotNull()) ? self.getName(stclContext) : "";
		CommandStatus<C, S> comp = new CommandStatus<C, S>(name, CommandStatus.SUCCESS, 2, msg, null);
		return new CommandStatus<C, S>(name, CommandStatus.SUCCESS, 0, value, comp);
	}

	/**
	 * @return a warning status with value.
	 */
	@Deprecated
	public final CommandStatus<C, S> warn(CommandStencil<?, ?> cmd) {
		return new CommandStatus<C, S>(cmd.getClass().getName(), CommandStatus.WARNING, 0, null, null);
	}

	@Deprecated
	public final CommandStatus<C, S> warn(CommandStencil<?, ?> cmd, Object value) {
		return new CommandStatus<C, S>(cmd.getClass().getName(), CommandStatus.WARNING, 0, value, null);
	}

	@Deprecated
	public final CommandStatus<C, S> warn(CommandStencil<?, ?> cmd, int id, Object value) {
		return new CommandStatus<C, S>(cmd.getClass().getName(), CommandStatus.WARNING, id, value, null);
	}

	@Deprecated
	public final CommandStatus<C, S> warn(CommandStencil<?, ?> cmd, int id, Object value, Result other) {
		return new CommandStatus<C, S>(cmd.getClass().getName(), CommandStatus.WARNING, id, value, other);
	}

	public final CommandStatus<C, S> warn(CommandContext<C, S> cmdContext, S self) {
		C stclContext = cmdContext.getStencilContext();
		String name = (self.isNotNull()) ? self.getName(stclContext) : "";
		return new CommandStatus<C, S>(name, CommandStatus.WARNING, 0, null, null);
	}

	public final CommandStatus<C, S> warn(CommandContext<C, S> cmdContext, S self, Object value) {
		C stclContext = cmdContext.getStencilContext();
		String name = (self.isNotNull()) ? self.getName(stclContext) : "";
		return new CommandStatus<C, S>(name, CommandStatus.WARNING, 0, value, null);
	}

	public final CommandStatus<C, S> warn(CommandContext<C, S> cmdContext, S self, int id, Object value) {
		C stclContext = cmdContext.getStencilContext();
		String name = (self.isNotNull()) ? self.getName(stclContext) : "";
		return new CommandStatus<C, S>(name, CommandStatus.WARNING, id, value, null);
	}

	public final CommandStatus<C, S> warn(CommandContext<C, S> cmdContext, S self, int id, Object value, Result other) {
		C stclContext = cmdContext.getStencilContext();
		String name = (self.isNotNull()) ? self.getName(stclContext) : "";
		return new CommandStatus<C, S>(name, CommandStatus.WARNING, id, value, other);
	}

	/**
	 * @return an error status with value.
	 */
	@Deprecated
	public final CommandStatus<C, S> error(CommandStencil<?, ?> cmd) {
		return new CommandStatus<C, S>(cmd.getClass().getName(), CommandStatus.ERROR, 0, null, null);
	}

	@Deprecated
	public final CommandStatus<C, S> error(CommandStencil<?, ?> cmd, Result other) {
		return new CommandStatus<C, S>(cmd.getClass().getName(), CommandStatus.ERROR, 0, null, other);
	}

	@Deprecated
	public final CommandStatus<C, S> error(CommandStencil<?, ?> cmd, Object value) {
		return new CommandStatus<C, S>(cmd.getClass().getName(), CommandStatus.ERROR, 0, value, null);
	}

	@Deprecated
	public final CommandStatus<C, S> error(CommandStencil<?, ?> cmd, int id, Object value) {
		return new CommandStatus<C, S>(cmd.getClass().getName(), CommandStatus.ERROR, id, value, null);
	}

	@Deprecated
	public final CommandStatus<C, S> error(CommandStencil<?, ?> cmd, int id, Object value, Result other) {
		return new CommandStatus<C, S>(cmd.getClass().getName(), CommandStatus.ERROR, id, value, other);
	}

	@Deprecated
	public final CommandStatus<C, S> error(CommandContext<C, S> cmdContext, S cmd) {
		return new CommandStatus<C, S>(cmd.getName(cmdContext.getStencilContext()), CommandStatus.ERROR, 0, null, null);
	}

	public final CommandStatus<C, S> error(CommandContext<C, S> cmdContext, S self, Result other) {
		C stclContext = cmdContext.getStencilContext();
		String name = (self.isNotNull()) ? self.getName(stclContext) : "";
		return new CommandStatus<C, S>(name, CommandStatus.ERROR, 0, null, other);
	}

	public final CommandStatus<C, S> error(CommandContext<C, S> cmdContext, S self, Object value) {
		C stclContext = cmdContext.getStencilContext();
		String name = (self.isNotNull()) ? self.getName(stclContext) : "";
		return new CommandStatus<C, S>(name, CommandStatus.ERROR, 0, value, null);
	}

	public final CommandStatus<C, S> error(CommandContext<C, S> cmdContext, S self, int id, Object value) {
		C stclContext = cmdContext.getStencilContext();
		String name = (self.isNotNull()) ? self.getName(stclContext) : "";
		return new CommandStatus<C, S>(name, CommandStatus.ERROR, id, value, null);
	}

	public final CommandStatus<C, S> error(CommandContext<C, S> cmdContext, S self, int id, Object value, Result other) {
		C stclContext = cmdContext.getStencilContext();
		String name = (self.isNotNull()) ? self.getName(stclContext) : "";
		return new CommandStatus<C, S>(name, CommandStatus.ERROR, id, value, other);
	}

	public CommandStencil(C stclContext) {
		super(stclContext);

		// local slots
		new TargetSlot(stclContext);
		new PluggedTargetSlot(stclContext);
	}

	/**
	 * Searches for indexed parameter. Searches first in command context and then
	 * in template descriptor. <b>Beware</b> : index starts from 1. (starts from 0
	 * in declaration)
	 * 
	 * @return indexed parameter or default value if not defined.
	 */
	public <K> K getParameter(CommandContext<C, S> cmdContext, int index, K def) {
		String key = PARAM_PREFIX + index;
		if (cmdContext.containsKey(key)) {
			K value = castParameter(cmdContext.getRedefinedParameter(key), def);
			if (value != null) {
				return value;
			}
		}
		if (this._defParams.containsKey(key)) {
			K value = castParameter(this._defParams.get(key), def);
			if (value != null) {
				return value;
			}
		}
		return def;
	}

	@SuppressWarnings("unchecked")
	private <K> K castParameter(Object value, K def) {
		if (def == null || value == null || value.getClass().isAssignableFrom(def.getClass())) {
			return (K) value;
		}
		if (def instanceof Boolean)
			return (K) new Boolean(value.toString());
		if (def instanceof Integer)
			return (K) new Integer(value.toString());
		if (def instanceof String)
			return (K) value.toString();
		return def;
	}

	/**
	 * @return expanded parameter in command execution context first, if not found
	 *         then gets in default command context.
	 */
	public String getExpandedParameter(CommandContext<C, S> cmdContext, int index, String def, S self) {
		String result = getParameter(cmdContext, index, def);
		return (result != null) ? self.format(cmdContext.getStencilContext(), result) : def;
	}

	public boolean getExpandedParameter(CommandContext<C, S> cmdContext, int index, boolean def, S self) {
		String result = getExpandedParameter(cmdContext, index, null, self);
		return (result != null) ? ConverterHelper.parseBoolean(result) : def;
	}

	public int getExpandedParameter(CommandContext<C, S> cmdContext, int index, int def, S self) {
		String result = getExpandedParameter(cmdContext, index, null, self);
		return (result != null) ? Integer.parseInt(result) : def;
	}

	/**
	 * Reset the command. Overload this method when a specific code must be
	 * performed when the command is reset.
	 * 
	 * @param cmdContext
	 * @param self
	 * @return
	 */
	public CommandStatus<C, S> reset(CommandContext<C, S> cmdContext, S self) throws Exception {
		return success(cmdContext, self);
	}

	/**
	 * Top level method for command execution.
	 */
	public CommandStatus<C, S> execute(CommandContext<C, S> cmdContext, S self) {
		if (getLog().isTraceEnabled()) {
			String msg = String.format("Execute command %s from %s", this, self);
			getLog().trace(cmdContext.getStencilContext(), msg);
		}

		// execute action stacking command context
		this._cmdContext = cmdContext; // needed to be able to access target
		// during the execution
		CommandStatus<C, S> status = verifyContext(cmdContext, self);
		if (!status.isSuccess()) {
			return status;
		}
		status = doAction(cmdContext, self);

		return status;
	}

	/**
	 * Sets parameter in default command context.
	 */
	public final <K> void setParameter(int index, K value) {
		String key = Keywords.PARAM + Integer.toString(index);
		this._defParams.put(key, value);
	}

	/**
	 * This method implements the code checked before the command is executed. In
	 * case the status returned is not success, the command is not performed.
	 */
	protected CommandStatus<C, S> verifyContext(CommandContext<C, S> cmdContext, S self) {
		return success(cmdContext, self);
	}

	/**
	 * This method implements the code performed when the command is executed.
	 */
	protected abstract CommandStatus<C, S> doAction(CommandContext<C, S> cmdContext, S self);

	/**
	 * Overload this method when a specific code must be executed to undo the
	 * command.
	 */
	public CommandStatus<C, S> doUndo(CommandContext<C, S> cmdContext, S self) {
		return success(cmdContext, self);
	}

	private S getTarget() {
		if (this._cmdContext != null)
			return this._cmdContext.getTarget();
		return null;
	}

	private class TargetSlot extends SingleCalculatedSlot<C, S> {
		public TargetSlot(C stclContext) {
			super(stclContext, CommandStencil.this, Slot.TARGET, PSlot.ONE);
		}

		@Override
		public boolean hasStencils(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
			return true;
		}

		@Override
		public S getStencil(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
			return getCalculatedStencil(stclContext, cond, self);
		}

		@Override
		public StencilIterator<C, S> getStencils(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
			S contained = getStencil(stclContext, cond, self);
			if (StencilUtils.isNull(contained))
				return StencilUtils.<C, S> iterator();
			return StencilUtils.<C, S> iterator(stclContext, contained, contained.getContainingSlot());
		}

		@Override
		public S getCalculatedStencil(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
			S target = getTarget();
			if (StencilUtils.isNull(target))
				return null;
			if (cond != null && !cond.verify(stclContext, target))
				return null;
			return target;
		}
	}

	private class PluggedTargetSlot extends SingleCalculatedSlot<C, S> {
		public PluggedTargetSlot(C stclContext) {
			super(stclContext, CommandStencil.this, Slot.PLUGGED_TARGET, PSlot.ONE);
		}

		@Override
		public boolean hasStencils(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
			return true;
		}

		@Override
		public S getStencil(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
			return getCalculatedStencil(stclContext, cond, self);
		}

		@Override
		public StencilIterator<C, S> getStencils(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
			S contained = getStencil(stclContext, cond, self);
			return StencilUtils.<C, S> iterator(stclContext, contained, self);
		}

		@Override
		public S getCalculatedStencil(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
			StencilFactory<C, S> factory = (StencilFactory<C, S>) stclContext.<C, S> getStencilFactory();

			// the target is the stencil plugged in the command stencil
			S target = getTarget();
			if (StencilUtils.isNull(target))
				return null;

			// TODO should use the same sursor if exist so target will be
			// calculatd dynamically
			S pluggedTarget = factory.createPStencil(stclContext, self, Key.NO_KEY, target);
			if (cond != null && !cond.verify(stclContext, pluggedTarget))
				return null;
			return pluggedTarget;
		}

	}
}