package com.gdo.stencils.key;

import com.gdo.stencils.log.StencilLog;

/**
 * <p>
 * The <tt>IKey</tt> interface is used to defined a key for plugging stencil
 * instances.
 * </p>
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 * </blockquote>
 */
public interface IKey extends Comparable<IKey> {

	public static final StencilLog LOG = new StencilLog(Key.class);

	/**
	 * @return <tt>true</tt> if the key is null
	 */
	boolean isEmpty();

	/**
	 * @return <tt>true</tt> if the key is not null
	 */
	boolean isNotEmpty();

}
