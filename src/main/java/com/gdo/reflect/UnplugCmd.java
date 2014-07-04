/*
 * Copyright GDO - 2004
 */
package com.gdo.reflect;

import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.WrongPathException;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.cond.PathCondition;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;

public class UnplugCmd extends AtomicActionStcl {

	public UnplugCmd(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		try {
			StclContext stclContext = cmdContext.getStencilContext();

			// get slot
			PStcl slotStcl = cmdContext.getTarget();
			PSlot<StclContext, PStcl> slot = ((com.gdo.reflect.SlotStcl) slotStcl.getReleasedStencil(stclContext)).getSlot();

			// get key
			String key = getExpandedParameter(cmdContext, 1, null, self);
			if (key == null) {
				slot.unplugAll(stclContext);
			} else {

				// verify key
				StencilCondition<StclContext, PStcl> cond = PathCondition.newKeyCondition(stclContext, new Key<String>(key), self);
				int size = slot.getStencils(stclContext, cond).size();
				if (size == 0) {
					String msg = String.format("Wrong key %s for unplug", key);
					return warn(cmdContext, self, 1, msg);
				}

				// performs unplug
				IKey k = new Key<String>(key);
				slot.unplug(stclContext, null, k);
			}

			// all was good
			return success(cmdContext, self);
		} catch (WrongPathException e) {
			return error(cmdContext, self, 1, e);
		}
	}
}