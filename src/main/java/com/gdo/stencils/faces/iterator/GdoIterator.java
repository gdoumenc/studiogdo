/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils.faces.iterator;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.faces.FacetsRenderer;
import com.gdo.stencils.faces.FacetsRendererWithContent;
import com.gdo.stencils.faces.GdoTag;
import com.gdo.stencils.faces.GdoTagExpander;
import com.gdo.stencils.faces.RenderContext;
import com.gdo.stencils.faces.WrongTagSyntax;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.plug._PStencil;

/**
 * <p>
 * Iterator component.
 * </p>
 * An iterator is defined by its path. <$iterator path="SlotName"> ...
 * <$/iterator> The text between those tags is expansed for all stencils in the
 * iterator. <$iterator:first>First<$/iterator:first>
 * <$iterator:last>Last<$/iterator:last> <$button path="." type="submit"
 * value="Aller..."/> <$iterator:not_last>,<$/iterator:not_last>
 * <$iterator:not_selected> <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */
public class GdoIterator<C extends _StencilContext, S extends _PStencil<C, S>> extends FacetsRendererWithContent<C, S> {

	private static final String[] ITERATORS_TAGS = new String[] { "iterator", "/iterator" };
	private static final int[] ITERATORS_LENGTHS = new int[] { ITERATORS_TAGS[0].length(), ITERATORS_TAGS[1].length() };

	private StencilIterator<C, S> _iterator;
	private S _current; // cuurent stencil rendered (used for subtag)

	public GdoIterator(RenderContext<C, S> renderContext) {
		super(renderContext);
	}

	public String getPath() {
		return (String) getAttribute(GdoTag.PATH);
	}

	public String getName() {
		return (String) getAttribute(GdoTag.NAME);
	}

	public void setPath(String path) {
		if (path.startsWith(GdoTagExpander.QUOTE))
			path = path.replaceAll(GdoTagExpander.QUOTE, "\"");
		setParameter(GdoTag.PATH, path);
	}

	public boolean isSubTag() {
		return false;
	}

	/**
	 * Return <tt>true</tt> if the iteration has more elements.
	 * 
	 * @param context
	 * @return <tt>true</tt> if the iteration has more elements.
	 */
	public boolean hasNext(C stclContext) {

		// creates iterator only if needed
		if (this._iterator == null) {
			S parent = getRenderContext().getStencilRendered();
			this._iterator = parent.getStencils(stclContext, getPath());
		}

		// returns iteration test
		return this._iterator.hasNext();
	}

	/**
	 * Returns the next stencil to be rendered in the iteration.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @return the next stencil to be rendered.
	 */
	public S next(C stclContext) {
		this._current = this._iterator.next();
		return this._current;
	}

	public S getCurrentStencil() {
		return this._current;
	}

	@Override
	public boolean needExpansion(C stclContext) {
		return true;
	}

	@Override
	public void expand(C stclContext) throws WrongTagSyntax {

		// expand for each stencil in iteration
		if (!isSubTag()) {
			String content = getContent();
			while (hasNext(stclContext)) {
				S current = next(stclContext);
				RenderContext<C, S> childContext = getRenderContext().clone(current);
				GdoTagExpander<C, S> expander = new GdoTagExpander<C, S>(content, this);
				expander.expand(stclContext, childContext);
			}
		} else {
			GdoIterator<C, S> parentIterator = findIteratorParent(this);
			if (parentIterator == null) {
				throw new WrongTagSyntax("Sub iterator tag defined out of iterator scope");
			}
			S plugged = parentIterator.getCurrentStencil();
			if (plugged == null) {
				throw new WrongTagSyntax("Worng iteration stencil");
			}
			if (this instanceof GdoFirstIterator) {
				if (plugged.isFirst(stclContext)) {
					GdoTagExpander<C, S> expander = new GdoTagExpander<C, S>(getContent(), this);
					expander.expand(stclContext, getRenderContext());
				}
			} else if (this instanceof GdoLastIterator) {
				if (plugged.isLast(stclContext)) {
					GdoTagExpander<C, S> expander = new GdoTagExpander<C, S>(getContent(), this);
					expander.expand(stclContext, getRenderContext());
				}
			} else if (this instanceof GdoNotFirstIterator) {
				if (!plugged.isFirst(stclContext)) {
					GdoTagExpander<C, S> expander = new GdoTagExpander<C, S>(getContent(), this);
					expander.expand(stclContext, getRenderContext());
				}
			} else if (this instanceof GdoNotLastIterator) {
				if (!plugged.isLast(stclContext)) {
					GdoTagExpander<C, S> expander = new GdoTagExpander<C, S>(getContent(), this);
					expander.expand(stclContext, getRenderContext());
				}
			} else if (this instanceof GdoSelectedIterator) {
				S selected = parentIterator.getRenderContext().getStencilRendered();
				if (selected.equals(plugged)) {
					GdoTagExpander<C, S> expander = new GdoTagExpander<C, S>(getContent(), this);
					expander.expand(stclContext, getRenderContext());
				}
			} else if (this instanceof GdoNotSelectedIterator) {
				S selected = parentIterator.getRenderContext().getStencilRendered();
				if (selected.equals(plugged)) {
					GdoTagExpander<C, S> expander = new GdoTagExpander<C, S>(getContent(), this);
					expander.expand(stclContext, getRenderContext());
				}
			}
		}
	}

	private GdoIterator<C, S> findIteratorParent(FacetsRenderer<C, S> parent) {
		FacetsRenderer<C, S> iter = parent;
		while (iter != null && !(iter instanceof GdoIterator && !((GdoIterator<C, S>) iter).isSubTag())) {
			iter = iter.getParent();
		}
		return (GdoIterator<C, S>) iter;
	}

	@Override
	public String[] getTags() {
		return ITERATORS_TAGS;
	}

	@Override
	public int[] getTagsLength() {
		return ITERATORS_LENGTHS;
	}
}
