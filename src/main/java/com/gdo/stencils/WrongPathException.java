/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils;

import com.gdo.stencils.plug._PStencil;

/**
 * <p>
 * Exception raised when a path is invalid.
 * </p>
 * <p>
 * A path is composed of a stencil path prefix and a slot name. Eventually a
 * condition is added after the slot name. The prefix may be null.
 * </p>
 * <p>
 * A path is said invalid in two cases :
 * <ul>
 * <li>the path syntax is wrong,
 * <li>the prefix path doesn't exist.
 * </ul>
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
@SuppressWarnings("serial")
public class WrongPathException extends RuntimeException {

	public WrongPathException(String msg) {
		super(msg);
	}

	public WrongPathException(String msg, _Stencil<?, ?> stencil) {
		super(msg + " in " + stencil);
	}

	public WrongPathException(String msg, _PStencil<?, ?> stencil) {
		super(msg + " in " + stencil);
	}
}