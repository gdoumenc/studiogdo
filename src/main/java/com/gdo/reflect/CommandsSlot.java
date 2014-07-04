/**
 * Copyright GDO - 2005
 */
package com.gdo.reflect;

import com.gdo.stencils.StclContext;
import com.gdo.stencils._Stencil;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.MultiCalculatedSlot;

public class CommandsSlot extends MultiCalculatedSlot<StclContext, PStcl> {

	public interface Slot {
		String COMMAND = "Command";
	}

	public CommandsSlot(StclContext stclContext, _Stencil<StclContext, PStcl> stcl, String name) {
		super(stclContext, stcl, name, PSlot.ANY);
	}

	@Override
	protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> condition, PSlot<StclContext, PStcl> self) {
		/*
		 * Stencil<StclContext, PStcl> stcl =
		 * self.getContainer().getReleasedStencil(stclContext); for (String cmdName
		 * : stcl.getCommands().keySet()) { PStcl cmdStcl =
		 * self.getContainer().getCommand(stclContext, cmdName); IKey key = new
		 * Key<String>(cmdName); if (getStencilFromList(stclContext, key, self) !=
		 * null) { keepStencilInList(stclContext, key, self); } else { PStcl cmd =
		 * self.getContainer().newPStencil(stclContext, self, key,
		 * CommandStcl.class.getName(), cmdStcl.getReleasedStencil(stclContext));
		 * cmd.plug(stclContext, cmdStcl, Slot.COMMAND);
		 * addStencilInList(stclContext, cmd, self); } }
		 */
		return cleanList(stclContext, condition, self);
	}
}
