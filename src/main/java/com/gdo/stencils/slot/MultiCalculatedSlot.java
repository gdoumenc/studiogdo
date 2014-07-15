/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.slot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gdo.stencils.Result;
import com.gdo.stencils._Stencil;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.util.SlotUtils;
import com.gdo.stencils.util.StencilUtils;

/**
 * <p>
 * The stencils plugged in this slot are calculated by a java code.
 * </p>
 * <p>
 * The stencils map is calculated by the method
 * <tt>Map getStencils(StencilContext stclContext)</tt>. I the calculation must
 * be done once then public StencilIterator getStencils(StencilContext context,
 * String condition, PStencilStencil parent) {
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
 */
public abstract class MultiCalculatedSlot<C extends _StencilContext, S extends _PStencil<C, S>> extends MultiSlot<C, S> {

	private CalculatedMap _map = new CalculatedMap(); // stored map if needed
	protected boolean _doPlug = true; // if <tt>true</tt> the stencils must be

	// simulated as plugged in this slot
	// (default).

	public MultiCalculatedSlot(C stclContext, _Stencil<C, S> in, String name, char arity) {
		super(stclContext, in, name, arity, true, false);
	}

	/**
	 * Set to <tt>true</tt> if the stencils must be simulated as plugged in this
	 * slot (default). May be set to false for optimization (long list of stencil)
	 */
	protected void setDoPlug(boolean plug) {
		this._doPlug = plug;
	}

	@Override
	abstract protected StencilIterator<C, S> getStencilsList(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self);

	@Override
	protected S doPlug(C stclContext, S stencil, IKey key, PSlot<C, S> self) {
		String msg = String.format("by default cannot plug in a multi calculated slot %s", self);
		return StencilUtils.<C, S> nullPStencil(stclContext, Result.error(msg));
	}

	@Override
	protected void doUnplug(C stclContext, S stencil, IKey key, PSlot<C, S> self) {
		logWarn(stclContext, "by default cannot unplug in a multi calculated slot %s", self);
	}

	/*
	 * @Override public boolean setKey(C stclContext, S stencil, String key,
	 * PSlot<C, S> self) { return false; }
	 */

	//
	// Stored stencil list
	//

	/*
	 * setDoPlug must be setted?? while() { if (getStencilFromList(stclContext,
	 * new Key<String>(id), self) != null) { keepStencilInList(stclContext, new
	 * Key<String>(id), self); continue; } // create session stencil
	 * StencilFactory<StclContext, PStcl> factory = (StencilFactory<StclContext,
	 * PStcl>) stclContext.getStencilFactory(); PStcl session =
	 * factory.createPStencil(stclContext, self, new Key<String>(id),
	 * SessionStcl.class.getName(), sessions.get(id));
	 * session.newPStencil(stclContext, Stcl.Slot.NAME, Key.NO_KEY,
	 * LinkStencil.class.getName(), "../Name"); session.setString(stclContext,
	 * SessionStcl.Slot.ID, id); addStencilInList(stclContext, session, self); }
	 * return cleanList(stclContext, condition, self);
	 */

	public S getStencilFromList(C stclContext, IKey key, PSlot<C, S> self) {
		return this._map.get(key);
	}

	protected void keepStencilInList(C stclContext, IKey key, PSlot<C, S> self) {
		this._map.keep(key);
	}

	@Override
	public void addStencilInList(C stclContext, S stcl, PSlot<C, S> self) {
		if (StencilUtils.isNull(stcl)) {
			logWarn(stclContext, "try to add an empty stencil in the multi calculated slot %s", self);
		} else {
			this._map.add(stcl);
		}
	}

	// needed to avoid stack recursion (cleaning list may call get stencils...)
	@Override
	public Result beforeUnplug(C stclContext, S stencil, IKey key, PSlot<C, S> self) {
		return Result.success(getClass().getName());
	}

	protected StencilIterator<C, S> cleanList(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {

		// removes old unplugged stencils
		Collection<S> list = this._map.clean(stclContext, self);

		// normally should not occur...
		if (!SlotUtils.isMultiple(stclContext, self)) {
			logWarn(stclContext, "Using a stored list in the single slot %s", self);
		}

		// creates the iterator from list
		return StencilUtils.<C, S> iterator(stclContext, list, cond, self);
	}

	class CalculatedMap {
		private Map<IKey, S> _oldMap;
		private List<String> _keep;

		S get(IKey key) {
			return getOldList().get(key);
		}

		void keep(IKey key) {
			getKeep().add(key.toString());
		}

		void add(S stcl) {
			IKey key = stcl.getKey();
			getOldList().put(key, stcl);
			keep(key);
		}

		Collection<S> clean(C stclContext, PSlot<C, S> self) {
			Map<IKey, S> map = new HashMap<IKey, S>(getOldList().size());
			for (S stcl : getOldList().values()) {
				if (getKeep().contains(stcl.getKey().toString())) {
					map.put(stcl.getKey(), stcl);
					if (MultiCalculatedSlot.this._doPlug) {
						self.plug(stclContext, stcl, stcl.getKey());
					}
				} else {
					if (MultiCalculatedSlot.this._doPlug) {
						self.unplug(stclContext, stcl, stcl.getKey());
					}
				}
			}
			this._oldMap = map;
			this._keep = null;
			return map.values();
		}

		private Map<IKey, S> getOldList() {
			if (this._oldMap == null) {
				this._oldMap = new HashMap<IKey, S>();
			}
			return this._oldMap;
		}

		private List<String> getKeep() {
			if (this._keep == null) {
				this._keep = new ArrayList<String>();
			}
			return this._keep;
		}
	}
}