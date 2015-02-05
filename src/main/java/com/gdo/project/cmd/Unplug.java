/*
 * Copyright GDO - 2004
 */
package com.gdo.project.cmd;

import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;

import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.plug.WrongPathException;
import com.gdo.stencils.util.StencilUtils;

/**
 * The stencil unplug command.<br>
 * Parameters are :
 * <ol>
 * <li>path to the stencil(s) to be removed (relative to command, default is
 * "Target").</li>
 * <li>slot(s) path from where the stencil(s) will be removed (relative to
 * command). If empty then removes the stencil from all slots.</li>
 * <li>1: only for this stencil should be at the same key), 2: this stencil
 * found at another specific key, 3 : all instances (all same stencil no matter
 * on key).</li>
 * <li>the key if type is 2.</li>
 * <ol>
 */
public class Unplug extends AtomicActionStcl {

    public interface Index {
        int NUMBER_OF_STENCILS_UNPLUGGED = 1;
        int NUMBER_OF_SLOTS_USED = 2;
    }

    public Unplug(StclContext stclContext) {
        super(stclContext);
    }

    @Override
    public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        try {
            StclContext stclContext = cmdContext.getStencilContext();

            // checks which parameters
            String which = getParameter(cmdContext, 1, Slot.TARGET);
            if (StringUtils.isEmpty(which)) {
                String msg = String.format("Which param cannot be null for unplug (param1)", which);
                return error(cmdContext, self, 1, msg);
            }

            // gets which stencils should be unplugged
            StencilIterator<StclContext, PStcl> toBeUnplugged = self.getStencils(stclContext, which);
            if (toBeUnplugged.isNotValid()) {
                return error(cmdContext, self, toBeUnplugged.getStatus());
            }
            if (toBeUnplugged.size() == 0) {
                return warn(cmdContext, self, "No stencil to unplug");
            }

            // unplugs from all slots if from is empty
            String from = getParameter(cmdContext, 2, null);
            if (StringUtils.isEmpty(from)) {
                int stencilsCounter = 0;
                for (PStcl whichStcl : toBeUnplugged) {
                    whichStcl.unplugFromAllSlots(stclContext);
                    stencilsCounter++;
                }
                return success(cmdContext, self, Index.NUMBER_OF_STENCILS_UNPLUGGED, stencilsCounter);
            }

            // get unplug type
            String type = getParameter(cmdContext, 3, null);
            if (StringUtils.isEmpty(type)) {
                type = "1";
            }

            // unplug from slots
            int slotsCounter = 0;
            Iterator<PSlot<StclContext, PStcl>> slots = StencilUtils.getSlots(stclContext, self, from);
            while (slots.hasNext()) {
                PSlot<StclContext, PStcl> fromSlot = slots.next();
                if (toBeUnplugged.size() > 0)
                    slotsCounter++;
                for (PStcl whichStcl : toBeUnplugged) {
                    if (fromSlot.contains(stclContext, null, whichStcl)) {

                        // creates order
                        if ("1".equals(type)) {
                            fromSlot.unplug(stclContext, whichStcl, whichStcl.getKey());
                        } else if ("2".equals(type)) {
                            String key = getParameter(cmdContext, 4, null);
                            fromSlot.unplug(stclContext, whichStcl, new Key(key));
                        } else if ("3".equals(type)) {
                            fromSlot.unplug(stclContext, whichStcl, Key.NO_KEY);
                        } else {
                            String msg = String.format("unknown type %s (param3)", type);
                            return error(cmdContext, self, msg);
                        }
                    }
                }
            }

            // return status
            CommandStatus<StclContext, PStcl> status = success(cmdContext, self);
            status.addOther(success(cmdContext, self, Index.NUMBER_OF_SLOTS_USED, slotsCounter));
            return status;
        } catch (WrongPathException e) {
            return error(cmdContext, self, 1, e);
        }
    }
}