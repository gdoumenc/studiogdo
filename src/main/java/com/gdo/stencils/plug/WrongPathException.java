/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.plug;

import com.gdo.stencils._Stencil;

/**
 * <p>
 * Exception raised when a path is invalid.
 * </p>
 * <p>
 * A path is composed of a stencil path prefix and a slot name. Eventually a
 * condition is added after the slot name. The prefix may be null.
 * </p>
 * A path is said invalid in two cases :
 * <ul>
 * <li>the path syntax is wrong,
 * <li>the prefix path doesn't exist.
 * </ul>
 */
@SuppressWarnings("serial")
public class WrongPathException extends RuntimeException {

    public WrongPathException(String msg) {
        super(msg);
    }

    public WrongPathException(String msg, _PStencil<?, ?> stencil) {
        super(msg + " in " + stencil);
    }
}