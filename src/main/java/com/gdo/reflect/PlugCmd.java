/*
 * Copyright GDO - 2004
 */
package com.gdo.reflect;

import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;

/**
 * The plug command removes the command target from :
 * <ul>
 * <li>the stencil to plug plugged as first parameter ,
 * <li>specific slots which paths are defined as second parameter separated by
 * ",",
 * <ul>
 */
public class PlugCmd extends com.gdo.project.cmd.Plug {

    public PlugCmd(StclContext stclContext) {
        super(stclContext);
    }

    @Override
    public CommandStatus<StclContext, PStcl> reset(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        return success(cmdContext, self);
    }

}