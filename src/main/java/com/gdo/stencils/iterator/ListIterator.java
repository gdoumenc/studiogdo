/*
 * Copyright GDO - 2004
 */
package com.gdo.stencils.iterator;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;

import com.gdo.stencils.Result;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.plug._PStencil;

/**
 * <p>
 * Iterator over a list of plugged stencils.
 * </p>
 * <p>
 * The stencils are given respecting the key order.
 * </p>
 */
public final class ListIterator<C extends _StencilContext, S extends _PStencil<C, S>> implements StencilIterator<C, S> {

	private Collection<S> _stencils; // stencils collection
	private Iterator<S> _iterator; // iterator;
	private Result _result;

	public ListIterator(Collection<S> stencils) {
		this._stencils = stencils;
		reset();
	}

	@Override
	public boolean hasNext() {
		return (this._iterator != null && this._iterator.hasNext());
	}

	@Override
	public S next() {
		if (this._iterator == null) {
			throw new NoSuchElementException();
		}
		return this._iterator.next();
	}

	@Override
	public StencilIterator<C, S> reset() {
		if (this._stencils != null) {
			this._iterator = this._stencils.iterator();
		}
		return this;
	}

	@Override
	public int size() {
		return (this._stencils == null) ? 0 : this._stencils.size();
	}

	@Override
	public boolean contains(S stencil) {
		return getPlugged(stencil) != null;
	}

	@Override
	public boolean contains(IKey key) {
		return getPlugged(key) != null;
	}

	@Override
	public S getPlugged(S stcl) {
		if (size() > 0) {
			for (S s : this._stencils) {
				if (s.equals(stcl))
					return s;
			}
		}
		return null;
	}

	@Override
	public S getPlugged(IKey key) {
		if (size() > 0) {
			for (S s : this._stencils) {
				if (s.getKey().equals(key))
					return s;
			}
		}
		return null;
	}

	@Override
	public int getIndex(S stencil) {
		int index = 0;
		if (size() > 0) {
			for (S s : this._stencils) {
				if (s.equals(stencil))
					return index;
				index++;
			}
		}
		return -1;
	}

	@Override
	public Iterator<S> iterator() {
		return this;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException(getClass().getName() + " cannot remove");
	}

	@Override
	public boolean isValid() {
		return (this._result != null) ? this._result.isSuccess() : true;
	}

	@Override
	public boolean isNotValid() {
		return !isValid();
	}

	@Override
	public Result getStatus() {
		return this._result;
	}

	@Override
	public void addStatus(Result status) {
		if (this._result == null) {
			this._result = status;
		} else {
			this._result.addOther(status);
		}
	}

	@Override
    @SuppressWarnings("unchecked")
	public StencilIterator<C, S> clone() {
		try {
			ListIterator<C, S> clone = (ListIterator<C, S>) super.clone();
			clone._iterator = this._stencils.iterator();
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