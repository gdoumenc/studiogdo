/*
 * Copyright GDO - 2004
 */
package com.gdo.project.cmd;

import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;

public class DeleteSelected extends Selected {

    public DeleteSelected(StclContext stclContext) {
        super(stclContext);
    }

    @Override
    public CommandStatus<StclContext, PStcl> performSteps(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();

        int deletionStep = getParameter(cmdContext, 1, 0);
        if (getActiveStepIndex() == deletionStep) {
            for (PStcl selected : getStencils(stclContext, SELECTED, self())) {
                if (beforeDelete(cmdContext, selected)) {
                    selected.unplugFromAllSlots(stclContext);
                    afterDelete(cmdContext);
                }
            }
        }
        return success(cmdContext, self);
    }

    // may be overriden for specific bahaviour
    protected boolean beforeDelete(CommandContext<StclContext, PStcl> cmdContext, PStcl selected) {
        return true;
    }

    // may be overriden for specific bahaviour
    protected void afterDelete(CommandContext<StclContext, PStcl> cmdContext) {
    }

}