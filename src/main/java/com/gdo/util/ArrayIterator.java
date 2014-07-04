/*
 * Copyright GDO - 2004
 */
package com.gdo.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Simple iterator on array. This class implements the
 * {@link java.util.Iterator} and {@link java.util.Iterable} interfaces.
 */
public class ArrayIterator<T> implements Iterator<T>, Iterable<T> {
	private T _array[]; // the array to iterate on
	private int _next; // the next array index.

	public ArrayIterator(T array[]) {
		this._array = array;
		this._next = 0;
	}

	public ArrayIterator(T array[], int start) {
		this._array = array;
		this._next = start;
	}

	public int size() {
		return this._array.length;
	}

	public int getNextIndex() {
		return this._next;
	}

	@Override
	public boolean hasNext() {
		return (this._array != null) && (this._next < this._array.length);
	}

	@Override
	public T next() throws NoSuchElementException {
		try {
			return this._array[this._next++];
		} catch (Exception ex) {
			throw new NoSuchElementException();
		}
	}

	public T previous() throws NoSuchElementException {
		try {
			return this._array[--this._next];
		} catch (Exception ex) {
			throw new NoSuchElementException();
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<T> iterator() {
		return this;
	}

    @Override
    public void forEach(Consumer<? super T> action) {
        // TODO Auto-generated method stub
    }

    @Override
    public Spliterator<T> spliterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        // TODO Auto-generated method stub
        
    }
}
