/*
 * Copyright GDO - 2004
 */
package com.gdo.project.cmd;

import com.gdo.project.model.ComposedActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;

public class Help extends ComposedActionStcl {

    public Help(StclContext stclContext) {
        super(stclContext);
    }

    @Override
    public CommandStatus<StclContext, PStcl> performSteps(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        /*
         * StclContext stclContext = context.getStencilContext(); String msg =
         * getParameter(context, CommandContext.PARAM1, "No help"); PropStcl<?> prop
         * = createProperty(stclContext, msg); plug(context.getStencilContext(),
         * (PStcl) prop, "Result", self());
         */
        return success(cmdContext, self);
    }
}