/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.slot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.gdo.helper.ClassHelper;
import com.gdo.stencils.Result;
import com.gdo.stencils._Stencil;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.cond.LinkCondition;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.factory.StencilFactory;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.util.GlobalCounter;
import com.gdo.stencils.util.StencilUtils;

/**
 * <p>
 * A multi slot is a slot in which several stencils may be plugged in.
 * </p>
 * 
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 * 
 * 
 * @author Guillaume Doumenc
 */
public class MultiSlot<C extends _StencilContext, S extends _PStencil<C, S>> extends _MultiSlot<C, S> {

    protected List<S> _stencils; // stencils plugged in the slot
    protected boolean _verify_unique = true; // will verify the stencil not
    // already there at same key before
    // if the order can be done
    protected boolean _force_unique = true; // will force the order even if

    // something wrong was found

    public MultiSlot(C stclContext, _Stencil<C, S> in, String name, char arity, boolean tranzient) {
        super(stclContext, in, name, arity, tranzient);
    }

    public MultiSlot(C stclContext, _Stencil<C, S> in, String name) {
        this(stclContext, in, name, PSlot.ANY, false);
    }

    @Override
    public void clear() {
        super.clear();
        if (_stencils != null) {
            _stencils.clear();
        }
    }

    public boolean isVerifyUnique() {
        return _verify_unique;
    }

    public void setVerifyUnique(boolean verify) {
        _verify_unique = verify;
    }

    public boolean isForceUnique() {
        return _force_unique;
    }

    public void setForceUnique(boolean forced) {
        _force_unique = forced;
    }

    @Override
    protected StencilIterator<C, S> getStencilsList(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
        if (ClassHelper.isEmpty(_stencils)) {
            return StencilUtils.<C, S> iterator();
        }
        return StencilUtils.<C, S> iterator(stclContext, _stencils, cond, self);
    }

    private synchronized StencilIterator<C, S> getSynchronizedStencilsList(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
        return getStencilsList(stclContext, cond, self);
    }

    @Override
    public boolean changeKey(C stclContext, S searched, String key, PSlot<C, S> self) {
        for (S s : _stencils) {
            if (s.equals(searched)) {
                s.setKey(new Key(key));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean contains(C stclContext, StencilCondition<C, S> cond, S searched, PSlot<C, S> self) {
        StencilIterator<C, S> stencils = getSynchronizedStencilsList(stclContext, cond, self);
        for (S s : stencils) {
            if (s.equals(searched))
                return true;
        }
        return false;
    }

    @Override
    public boolean hasStencils(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
        return size(stclContext, cond, self) > 0;
    }

    @Override
    public int size(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
        StencilIterator<C, S> stencils = getSynchronizedStencilsList(stclContext, cond, self);
        return stencils.size();
    }

    @Override
    public S getStencil(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
        StencilIterator<C, S> stencils = getSynchronizedStencilsList(stclContext, cond, self);
        if (stencils.hasNext()) {
            return stencils.next();
        }
        String msg = String.format("no stencil in %s for cond %s", self, cond);
        return StencilUtils.<C, S> nullPStencil(stclContext, Result.error(msg));
    }

    @Override
    public StencilIterator<C, S> getStencils(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
        return getSynchronizedStencilsList(stclContext, cond, self);
    }

    @Override
    public StencilIterator<C, S> getStencilsToSave(C stclContext, PSlot<C, S> self) {
        StencilCondition<C, S> c = LinkCondition.withLinksCondition(stclContext, self.getContainer());
        return getStencils(stclContext, c, self);
    }

    /**
     * @return the keys of the stencil is this slot.
     */
    // TODO should return an iterator
    public IKey[] getKeys(C stclContext, PSlot<C, S> self) {
        StencilIterator<C, S> stencils = getSynchronizedStencilsList(stclContext, null, self);

        IKey[] keys = new IKey[stencils.size()];
        int i = 0;
        for (S plugged : stencils) {
            keys[i++] = plugged.getKey();
        }
        return keys;
    }

    @Override
    protected S doPlug(C stclContext, S stencil, IKey key, PSlot<C, S> self) {

        // get key (define it unique if not defined)
        if (key.isEmpty()) {
            key = new Key(GlobalCounter.uniqueInt());
        }

        // create the plugged stencil
        // TODO here also should use cursor to create this stencil
        StencilFactory<C, S> factory = (StencilFactory<C, S>) stclContext.<C, S> getStencilFactory();
        S plugged = factory.createPStencil(stclContext, self, key, stencil);
        addStencilInList(stclContext, plugged, self);

        // dual plug in parent if defined
        /*
         * if (getDescriptor() != null) { String parentKey =
         * getDescriptor().getParent(); if (!StringUtils.isEmpty(parentKey)) {
         * stcl.plug(stclContext, self.getContainer(stclContext), "Parent",
         * parentKey); } }
         */

        return plugged;
    }

    synchronized public void addStencilInList(C stclContext, S stcl, PSlot<C, S> self) {
        if (_stencils == null) {
            _stencils = new StencilList();
        }
        _stencils.add(stcl);
    }

    @Override
    protected void doUnplug(C stclContext, S stencil, IKey key, PSlot<C, S> self) {

        // verify the key is correct
        if (!key.isEmpty()) {

            // get list of stencils with links if needed
            StencilCondition<C, S> cond = null;
            if (LinkCondition.isLinkKey(key.toString())) {
                cond = LinkCondition.<C, S> onlyLinksCondition(stclContext, self.getContainer());
            }
            StencilIterator<C, S> stencils = getSynchronizedStencilsList(stclContext, cond, self);

            // check the stencil exists in the list
            /*
             * if (LinkCondition.isLinkKey(key)) key = key.substring(1); if
             * (!StencilUtils.keyMatches(stencils, key)) { String msg =
             * String.format("Unplug order with wrong key %s in slot %s", key, this);
             * if (getLog().isWarnEnabled()) getLog().warn(stclContext, msg); return
             * StencilUtils.iterator(msg); }
             */

            // unplug the stencils
            List<S> initial = new ArrayList<S>(stencils.size());
            for (S stcl : stencils) {
                initial.add(stcl);
            }
            List<S> removed = new ArrayList<S>();
            for (S stcl : initial) {
                if (stcl.getKey().toString().matches(key.toString())) {
                    _stencils.remove(stcl.getKey());
                    removed.add(stcl);
                }
            }
            return;
        }

        // if no key and no stencil
        if (StencilUtils.isNull(stencil)) {
            logWarn(stclContext, "Unplug order with no key and no stencil in slot %s", this);
            return;
        }

        // get list of stencils with links if needed
        StencilCondition<C, S> cond = null;
        if (stencil.isLink(stclContext)) {
            cond = LinkCondition.<C, S> onlyLinksCondition(stclContext, self.getContainer());
        }
        StencilIterator<C, S> stencils = getSynchronizedStencilsList(stclContext, cond, self);

        // check the stencil exists in the list
        if (!stencils.contains(stencil)) {
            logWarn(stclContext, "Unplug order with wrong stencil %s in slot %s", stencil, this);
            return;
        }

        // get the stencils to be unplugged
        List<S> initial = new ArrayList<S>(stencils.size());
        for (S s : stencils) {
            initial.add(s);
        }
        List<S> removed = new ArrayList<S>();
        for (S s : initial) {
            if (s.equals(stencil)) {
                _stencils.remove(s);
                removed.add(s);
            }
        }
    }

    @Override
    protected void doUnplugAll(C stclContext, PSlot<C, S> self) {

        // get the stencils in the slot (do not follow links if want to remove a
        // link)
        StencilCondition<C, S> cond = LinkCondition.<C, S> withLinksCondition(stclContext, self.getContainer());
        StencilIterator<C, S> stencils = getSynchronizedStencilsList(stclContext, cond, self);

        // unplug all stencils
        List<S> initial = new ArrayList<S>(stencils.size());
        for (S stcl : stencils) {
            initial.add(stcl);
        }
        // List<S> removed = new ArrayList<S>();
        for (S stcl : initial) {
            _stencils.remove(stcl);
            // removed.add(stcl);
        }
    }

    @Override
    public boolean hasAdaptorStencil(C stclContext, PSlot<C, S> self) {
        StencilIterator<C, S> stencils = getSynchronizedStencilsList(stclContext, null, self);
        if (stencils.hasNext()) {
            return (stencils.next().isLink(stclContext));
        }
        return false;
    }

    @Override
    public S getAdaptorStencil(C stclContext, PSlot<C, S> self) {
        StencilIterator<C, S> stencils = getSynchronizedStencilsList(stclContext, null, self);
        if (stencils.hasNext()) {
            S stcl = stencils.next();
            if (stcl.isLink(stclContext))
                return stcl;
        }
        return null;
    }

    @Override
    public boolean canChangeOrder(C stclContext, PSlot<C, S> self) {
        return true;
    }

    @Override
    public boolean isFirst(C stclContext, S searched, PSlot<C, S> self) {
        StencilIterator<C, S> stencils = getSynchronizedStencilsList(stclContext, null, self);
        return stencils.getIndex(searched) == 0;
    }

    @Override
    public boolean isLast(C stclContext, S searched, PSlot<C, S> self) {
        StencilIterator<C, S> stencils = getSynchronizedStencilsList(stclContext, null, self);
        return stencils.getIndex(searched) == stencils.size() - 1;
    }

    class StencilList extends Vector<S> {
        private static final long serialVersionUID = 0L;

        @Override
        public boolean remove(Object obj) {
            if (obj instanceof IKey) {
                Iterator<S> iter = iterator();
                while (iter.hasNext()) {
                    S stcl = iter.next();
                    if (stcl.getKey().equals(obj)) {
                        return remove(stcl);
                    }
                }
                return false;
            }
            return super.remove(obj);
        }
    }
}