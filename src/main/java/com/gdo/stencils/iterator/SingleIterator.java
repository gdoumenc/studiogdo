/*
 * Copyright GDO - 2004
 */
package com.gdo.stencils.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;

import com.gdo.stencils.Result;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.util.StencilUtils;

/**
 * <p>
 * Iterator over one single plugged stencil.
 * </p>
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
public final class SingleIterator<C extends _StencilContext, S extends _PStencil<C, S>> implements StencilIterator<C, S> {

	private final S _plugged; // single stencil
	private boolean _given; // given stencil status

	public SingleIterator(S plugged) {
		_plugged = plugged;
		_given = false;
	}

	@Override
	public boolean hasNext() {
		return !_given;
	}

	@Override
	public S next() {
		if (_given)
			throw new NoSuchElementException();
		_given = true;
		return _plugged;
	}

	@Override
	public StencilIterator<C, S> reset() {
		_given = false;
		return this;
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public boolean contains(S stencil) {
		if (StencilUtils.isNull(_plugged))
			return false;
		return _plugged.equals(stencil);
	}

	@Override
	public boolean contains(IKey key) {
		return false;
	}

	@Override
	public S getPlugged(S stcl) {
		if (contains(stcl))
			return _plugged;
		return null;
	}

	@Override
	public S getPlugged(IKey key) {
		throw new NoSuchElementException();
	}

	@Override
	public int getIndex(S stencil) {
		if (StencilUtils.isNull(stencil))
			return -1;
		return contains(stencil) ? 0 : -1;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException(getClass().getName() + " cannot remove");
	}

	@Override
	public Iterator<S> iterator() {
		return this;
	}

	@Override
	public boolean isValid() {
		return StencilUtils.isNotNull(_plugged);
	}

	@Override
	public final boolean isNotValid() {
		return !isValid();
	}

	@Override
	public Result getStatus() {
		if (_plugged == null)
			return Result.error("invalid single iterator with empty stencil");
		return _plugged.getResult();
	}

	@Override
	public void addStatus(Result status) {
		if (_plugged != null)
			_plugged.addResult(status);
	}

	@Override
    @SuppressWarnings("unchecked")
	public StencilIterator<C, S> clone() {
		try {
			SingleIterator<C, S> clone = (SingleIterator<C, S>) super.clone();
			clone._given = false;
			return clone;
		} catch (Exception e) {
			return this;
		}
	}

    @Override
    public void forEachRemaining(Consumer<? super S> action) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void forEach(Consumer<? super S> action) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Spliterator<S> spliterator() {
        // TODO Auto-generated method stub
        return null;
    }

}