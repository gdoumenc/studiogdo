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

/**
 * <p>
 * Empty plugged stencil iterator.
 * </p>
 */
public final class EmptyIterator<C extends _StencilContext, S extends _PStencil<C, S>> implements StencilIterator<C, S> {

    // result explaining why the list is null
    private Result _result;

    public EmptyIterator(Result result) {
        _result = result;
    }

    public EmptyIterator() {
        _result = Result.success();
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public S next() {
        throw new NoSuchElementException();
    }

    @Override
    public StencilIterator<C, S> reset() {
        return this;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean contains(S stencil) {
        return false;
    }

    @Override
    public boolean contains(IKey key) {
        return false;
    }

    @Override
    public S getPlugged(S stencil) {
        throw new NoSuchElementException();
    }

    @Override
    public S getPlugged(IKey key) {
        throw new NoSuchElementException();
    }

    @Override
    public int getIndex(S stencil) {
        return -1;
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
        return _result.isSuccess();
    }

    @Override
    public boolean isNotValid() {
        return !isValid();
    }

    @Override
    public Result getStatus() {
        return _result;
    }

    @Override
    public void addStatus(Result status) {
        _result.addOther(status);
    }

    @Override
    public StencilIterator<C, S> clone() {
        return this;
    }

    @Override
    public void forEachRemaining(Consumer<? super S> action) {
        // TODO Auto-generated method stub

    }

    @Override
    public void forEach(Consumer<? super S> action) {
    }

    @Override
    public Spliterator<S> spliterator() {
        return null;
    }
}