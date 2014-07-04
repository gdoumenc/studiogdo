/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils.faces.iterator;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.faces.RenderContext;
import com.gdo.stencils.plug._PStencil;

/**
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
public class GdoNotLastIterator<C extends _StencilContext, S extends _PStencil<C, S>> extends GdoIterator<C, S> {

	private static final String[] NOT_LAST_ITERATORS_TAGS = new String[] { "iterator:not_last", "/iterator:not_last" };
	private static final int[] NOT_LAST_ITERATORS_LENGTHS = new int[] { NOT_LAST_ITERATORS_TAGS[0].length(), NOT_LAST_ITERATORS_TAGS[1].length() };

	public GdoNotLastIterator(RenderContext<C, S> renderContext) {
		super(renderContext);
	}

	@Override
	public String[] getTags() {
		return NOT_LAST_ITERATORS_TAGS;
	}

	@Override
	public int[] getTagsLength() {
		return NOT_LAST_ITERATORS_LENGTHS;
	}

	@Override
	public boolean isSubTag() {
		return true;
	}

}
