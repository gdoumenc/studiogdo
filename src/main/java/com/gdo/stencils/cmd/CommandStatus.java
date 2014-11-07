/*
 * Copyright GDO - 2005
 */
package com.gdo.stencils.cmd;

import com.gdo.stencils.Result;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.plug._PStencil;

/**
 * <p>
 * A command status is the command execution result.
 * </p>
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo & Guillaume Doumenc. Use
 * is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */

public class CommandStatus<C extends _StencilContext, S extends _PStencil<C, S>> extends Result {

    // TODO do redirection on result
    public String redirection = null;

    /**
     * Simple command status constructor in case of success.
     */
    public CommandStatus(CommandStencil<?, ?> cmd, Result result) {
        super(Result.SUCCESS, cmd.getClass().getName(), 0, null, result);
    }

    /**
     * Constructor with fixed string prefix.
     */
    public CommandStatus(String prefix, byte level, int index, Object value, Result other) {
        super(level, prefix, index, value, other);
    }

    /**
     * Constructor using command class name as prefix.
     */
    public CommandStatus(C stclContext, S cmd, byte level, int index, Object value, Result other) {
        this(cmd.getName(stclContext), level, index, value, other);
    }

    @Deprecated
    public CommandStatus(CommandStencil<?, ?> cmd, byte level, int index, Object value, Result other) {
        this(cmd.getName(null, null), level, index, value, other);
    }

}