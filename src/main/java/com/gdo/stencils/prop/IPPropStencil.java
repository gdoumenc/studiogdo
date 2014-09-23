package com.gdo.stencils.prop;

import java.io.InputStream;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.atom.IAtom;
import com.gdo.stencils.plug._PStencil;

/**
 * <p>
 * Interface for a property stencil.
 * </p>
 * <p>
 * A property stencil is a stencil with a string value associated. A property
 * stencil is the atomic component encapsulating a java data.
 * <p>
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com)</a>
 * @see com.gdo.stencils.cmd.CommandContext Context
 */
public interface IPPropStencil<C extends _StencilContext, S extends _PStencil<C, S>> extends IAtom<C, S> {

	/**
	 * Returns the property type used to implement it in java.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @return the property type (used mainly when saving).
	 */
	String getType(C stclContext);

	/**
	 * Returns the property value.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @return the property value.
	 */
	String getValue(C stclContext);

	/**
	 * Sets the property value.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param value
	 *          the property value.
	 */
	void setValue(C stclContext, String value);

	/**
	 * Tests if the property value will be expanded before reading.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 */
	boolean isExpand(C stclContext);

	/**
	 * Sets if the property value will be expanded before reading.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param expand
	 *          <tt>true</tt> if expansion should be done.
	 */
	void setExpand(C stclContext, boolean expand);

	InputStream getInputStream(C stclContext);

	String getExpandedValue(C stclContext);

	String getNotExpandedValue(C stclContext);

}