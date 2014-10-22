/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.slot;

import com.gdo.project.cmd.Plug;
import com.gdo.project.cmd.Unplug;
import com.gdo.stencils.Result;
import com.gdo.stencils._Stencil;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.util.StencilUtils;

/**
 * <p>
 * This slot determines its plugged stencils thru calculation.
 * </p>
 * <p>
 * Pluging or unplugging a stencil in a single calculated slot is possible but
 * in this case the two following methods
 * {@link SingleSlot#doPlug(_StencilContext, Plug)} and
 * {@link SingleSlot#doUnplug(_StencilContext, Unplug)} must be redefined
 * otherwise nothing is performed.
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
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */
public abstract class SingleCalculatedSlot<C extends _StencilContext, S extends _PStencil<C, S>> extends SingleSlot<C, S> {

	private boolean _acceptPlug = false; // by default cannot plug in a

	// calculated slot

	public SingleCalculatedSlot(C stclContext, _Stencil<C, S> in, String name, char arity) {
		super(stclContext, in, name, arity, true, false);
	}

	public SingleCalculatedSlot(C stclContext, _Stencil<C, S> in, String name) {
		this(stclContext, in, name, PSlot.ONE);
	}

	public boolean acceptPlug() {
		return _acceptPlug;
	}

	public void setAcceptPlug(boolean accept) {
		_acceptPlug = accept;
	}

	@Override
	public boolean hasStencils(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
		if (self.getArity(stclContext) == PSlot.ONE)
			return true;

		if (acceptPlug()) {
			return super.hasStencils(stclContext, cond, self);
		}
		String msg = String.format("Function hasStencils in calculated slot %s not redefined", self);
		throw new UnsupportedOperationException(msg);
	}

	@Override
	public S getStencil(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
		
		// if accepts plug then returns stencils plugged
		if (acceptPlug()) {
			return super.getStencil(stclContext, cond, self);
		}
		
		// returns calculated stencil
		return getCalculatedStencil(stclContext, cond, self);
	}

	@Override
	public StencilIterator<C, S> getStencils(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
		return StencilUtils.<C, S> iterator(stclContext, getStencil(stclContext, cond, self), self);
	}

	public abstract S getCalculatedStencil(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self);

	/**
	 * If a property can be plugged in place of the calculated property, then if
	 * no stencil plugged used the calculated stencil.
	 */
	@Override
	protected S getContainedStencilOrCreateDefault(C stclContext, PSlot<C, S> self) {
		if (acceptPlug())
			return getCalculatedStencil(stclContext, null, self);
		String msg = String.format("Cannot create default calculated property for %s [sgould not goes here]", self);
		return StencilUtils.<C, S> nullPStencil(stclContext, Result.error(msg));
	}

	@Override
	protected S doPlug(C stclContext, S stencil, IKey key, PSlot<C, S> self) {
		if (acceptPlug())
			return super.doPlug(stclContext, stencil, key, self);
		String msg = String.format("Cannot plug in the calculated slot %s", self);
		return StencilUtils.<C, S> nullPStencil(stclContext, Result.error(msg));
	}

	@Override
	protected void doUnplug(C stclContext, S stencil, IKey key, PSlot<C, S> self) {
		if (acceptPlug()) {
			super.doUnplug(stclContext, stencil, key, self);
		}
		logWarn(stclContext, "Cannot unplug from the calculated slot %s", self);
	}

	@Override
	protected void doUnplugAll(C stclContext, PSlot<C, S> self) {
		if (acceptPlug()) {
			super.doUnplugAll(stclContext, self);
			return;
		}
		logWarn(stclContext, "Cannot unplugall from the calculated slot %s", self);
	}

	@Override
	public StencilIterator<C, S> getStencilsToSave(C stclContext, PSlot<C, S> self) {
		if (acceptPlug())
			return super.getStencilsToSave(stclContext, self);
		return StencilUtils.<C, S> iterator();
	}

	/**
	 * Expunge the calculated stencil to force reclaculation. Made public to allow
	 * command Expunge to use it.
	 */
	@Override
	public void expunge(C stclContext, PSlot<C, S> self) {
		_containedStcl = null;
	}

}