/*
 * Copyright GDO - 2004
 */
package com.gdo.project.cmd;

import org.apache.commons.lang3.StringUtils;

import com.gdo.project.cmd.eval.ClearSlot;
import com.gdo.project.cmd.eval.HasValue;
import com.gdo.project.cmd.eval.IsEmpty;
import com.gdo.project.cmd.eval.IsPlugged;
import com.gdo.project.cmd.eval.MultiUnplug;
import com.gdo.project.cmd.eval.SetValue;
import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;

/**
 * Generic command for many commands on a stencil (in one command for efficiency
 * reason).<br>
 * Parameters are :
 * <ol>
 * <li>Available commands are :
 * <ul>
 * <li>- MultiUnplug : Unplug several stencils in one command. param 2 : pathes
 * to stencils to be unpluged - HasValue : Tests if there is a stencil in a
 * slot. param 2 : tested slot path (if path to a multi slot, search if one
 * stencil plugged has value) param 3 : type to be verified (default null) null
 * : just test if the slot is not empty string : the stencil is a property
 * containing a string int : the stencil is a property containing an integer
 * boolean : the stencil is a property containing a boolean param4 : value to be
 * compared (default null) param5 : operator for testing string : "==" is equals
 * (default), "!=" is not equals, "^=" is startsWith, "=^" is endsWith, "~=" is
 * matches int : all integer operations (default "==") boolean : "==" (default)
 * or "!=" - IsEmpty : checks if a property is empty param 2 : slot path (if
 * path to a multi slot, all properties must be empty) - SetValue : Set a value
 * in a property slot (usefull in formatted string) param2 : destination
 * property path param3 : property type (default "string") param4 : property
 * value (default "") - Flush : removes all stencils from a cursor slot. param 2
 * : slot path - IsPlugged : tests if this stencil is plugged in some slots.
 * param 2 : slot path (if undefined, checks if plugged anywhere else) param 3 :
 * "&" or "|" (condition if several slots)</li>
 * <ol>
 */
public class Eval extends AtomicActionStcl {

    public enum Function {
        /*
         * Eval {
         * 
         * @Override CommandStatus<StclContext, PStcl>
         * doAction(CommandContext<StclContext, PStcl> cmdContext, Eval eval, PStcl
         * self) { try { ContextFactory contextFactory = new ContextFactory();
         * Context jsContext = contextFactory.enterContext(); Scriptable scope =
         * jsContext.initStandardObjects(); Object res =
         * jsContext.evaluateString(scope, "function f(x){return x+1} f(7)",
         * "<cmd>", 1, null); System.err.println(Context.toString(res)); return
         * eval.success(cmdContext, self); } finally { Context.exit(); } } },
         */
        ClearSlot {
            @Override
            CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, Eval eval, PStcl self) {
                return new ClearSlot(cmdContext.getStencilContext()).execute(cmdContext, self);
            }
        },
        MultiUnplug {
            @Override
            CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, Eval eval, PStcl self) {
                return new MultiUnplug(cmdContext.getStencilContext()).execute(cmdContext, self);
            }
        },
        HasValue {
            @Override
            CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, Eval eval, PStcl self) {
                return new HasValue(cmdContext.getStencilContext()).execute(cmdContext, self);
            }
        },
        IsEmpty {
            @Override
            CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, Eval eval, PStcl self) {
                return new IsEmpty(cmdContext.getStencilContext()).execute(cmdContext, self);
            }
        },
        SetValue {
            @Override
            CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, Eval eval, PStcl self) {
                return new SetValue(cmdContext.getStencilContext()).execute(cmdContext, self);
            }
        },
        IsPlugged {
            @Override
            CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, Eval eval, PStcl self) {
                return new IsPlugged(cmdContext.getStencilContext()).execute(cmdContext, self);
            }
        };

        abstract CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, Eval eval, PStcl self);
    }

    private Function _function;

    public Eval(StclContext stclContext) {
        super(stclContext);
    }

    @Override
    public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        return _function.doAction(cmdContext, this, self);
    }

    // verify function name
    @Override
    protected CommandStatus<StclContext, PStcl> verifyContext(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {

        String function = getParameter(cmdContext, 1, null);
        if (StringUtils.isBlank(function)) {
            return error(cmdContext, self, "the function name should be defined for Eval command (param1)");
        }

        try {
            _function = Function.valueOf(function);
        } catch (Exception e) {
            String msg = String.format("exeption %s in finding function name %s for Eval command", e, function);
            return error(cmdContext, self, msg);
        }

        return success(cmdContext, self);
    }
}