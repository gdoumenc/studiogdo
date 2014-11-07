/*
 * Copyright GDO - 2004
 */
package com.gdo.project.cmd;

import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;

public class Move extends AtomicActionStcl {

    public static final String UP = "up";
    public static final String DOWN = "down";
    public static final String FIRST = "first";
    public static final String LAST = "last";
    public static final String RELATIVE = "relative";

    // private String _mode;

    // private String _key;

    public Move(StclContext stclContext) {
        super(stclContext);
    }

    @Override
    public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        /*
         * StclContext stclContext = cmdContext.getStencilContext(); PStcl target =
         * cmdContext.getTarget();
         * 
         * if (_mode.equals(UP)) { //target.moveUp(stclContext); } else if
         * (_mode.equals(DOWN)) { //target.moveDown(stclContext); } else if
         * (_mode.equals(FIRST)) { //target.moveFirst(stclContext); } else if
         * (_mode.equals(LAST)) { //target.moveLast(stclContext); }
         */

        return success(cmdContext, self);
    }

    @Override
    protected CommandStatus<StclContext, PStcl> verifyContext(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        /*
         * // gets move mode String mode = getParameter(cmdContext, 1, null); if
         * (StringUtils.isEmpty(mode)) { String msg = String.format(
         * "mode should be up, down, first, last or relative not empty (param1)" );
         * return error(cmdContext, self, msg); } if (!mode.equals(UP) &&
         * !mode.equals(DOWN) && !mode.equals(FIRST) && !mode.equals(LAST) &&
         * !mode.equals(RELATIVE)) { String msg = String.format(
         * "mode should be up, down, first, last or relative not %s (param1)",
         * mode); return error(cmdContext, self, msg); } _mode = mode;
         * 
         * // _key = getParameter(cmdContext, CommandContext.PARAM2, null);
         */
        return super.verifyContext(cmdContext, self);
    }

}