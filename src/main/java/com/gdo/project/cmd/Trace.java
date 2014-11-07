/*
 * Copyright GDO - 2004
 */
package com.gdo.project.cmd;

import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;

/**
 * The trace command.<br>
 * Parameters are :
 * <ol>
 * <li>String format</li>
 * <li>first format parameter</li>
 * <li>second format parameter</li>
 * <li>third format parameter</li>
 * <li>fourth format parameter</list>
 * <ol>
 */
public class Trace extends AtomicActionStcl {

    public Trace(StclContext stclContext) {
        super(stclContext);
    }

    @Override
    public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();

        // get parameters for format
        String p1 = self.format(stclContext, getParameter(cmdContext, 2, ""));
        String p2 = self.format(stclContext, getParameter(cmdContext, 3, ""));
        String p3 = self.format(stclContext, getParameter(cmdContext, 4, ""));
        String p4 = self.format(stclContext, getParameter(cmdContext, 5, ""));

        // print formatted string
        String format = self.format(stclContext, getParameter(cmdContext, 1, "trace command"));
        String msg = String.format(format, p1, p2, p3, p4);
        logWarn(stclContext, msg);

        return success(cmdContext, self, msg);
    }

}