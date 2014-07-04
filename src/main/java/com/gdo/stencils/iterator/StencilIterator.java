/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.iterator;

import java.util.Iterator;

import com.gdo.stencils.Result;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.plug._PStencil;

/**
 * <p>
 * Iterator over plugged stencils.
 * </p>
 * {@see com.gdo.stencils.java.plugged.PluggedStencil} <blockquote>
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
public interface StencilIterator<C extends _StencilContext, S extends _PStencil<C, S>> extends Iterator<S>, Iterable<S>, Cloneable {

	/**
	 * @return <tt>true</tt> if the iteration has more stencils.
	 */
	@Override
	boolean hasNext();

	/**
	 * @return the next plugged stencil in the iteration.
	 */
	@Override
	S next();

	/**
	 * Reset the stencil iterator (to be able to iterate again on it).
	 */
	StencilIterator<C, S> reset();

	/**
	 * @return the number of stencils plugged.
	 */
	int size();

	/**
	 * @return <tt>true</true> if the key exists in the iterator.
	 */
	boolean contains(IKey key);

	/**
	 * @return the plugged stencil in the iteration if the stencil is in
	 *         iteration.
	 */
	S getPlugged(IKey key);

	/**
	 * @return <tt>true</true> if the stencil exists in the iterator.
	 */
	boolean contains(S stencil);

	/**
	 * @return the plugged stencil in the iteration if the stencil is in
	 *         iteration.
	 */
	S getPlugged(S stencil);

	/**
	 * @return the index of the plugged stencil in the iteration if the stencil is
	 *         in iteration.
	 */
	int getIndex(S stencil);

	/**
	 * @return <tt>true</tt> if the iterator was created without errors.
	 */
	boolean isValid();

	/**
	 * @return <tt>true</tt> if the iterator encountered error during creation.
	 */
	boolean isNotValid();

	/**
	 * @return The result status on this iterator.
	 */
	Result getStatus();

	/**
	 * Add result status on this iterator.
	 */
	void addStatus(Result result);

	StencilIterator<C, S> clone();
}
