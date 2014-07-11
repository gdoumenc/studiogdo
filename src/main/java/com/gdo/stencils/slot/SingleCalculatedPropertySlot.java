/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.slot;

import com.gdo.helper.ClassHelper;
import com.gdo.stencils.Result;
import com.gdo.stencils._Stencil;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.factory.StencilFactory;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.prop.CalculatedPropStencil;
import com.gdo.stencils.prop.IPropCalculator;
import com.gdo.stencils.prop.PropStencil;
import com.gdo.stencils.util.StencilUtils;

/**
 * <p>
 * This slot contains a calculated property stencil.
 * </p>
 * <p>
 * The default property plugged in this slot is a calculated property. The
 * calculated property class is defined by the factory (for stencil :
 * {@link CalculatedPropStencil}).
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
public class SingleCalculatedPropertySlot<C extends _StencilContext, S extends _PStencil<C, S>> extends SingleCalculatedSlot<C, S> {

	// property calculator used
	private IPropCalculator<C, S> _calculator = null;
	// calculated property (only the PProp is created each time)
	private PropStencil<C, S> _prop = null;

	public SingleCalculatedPropertySlot(C stclContext, _Stencil<C, S> in, String name, IPropCalculator<C, S> calculator) {
		super(stclContext, in, name, PSlot.ONE);
		this._calculator = calculator;
	}

	/**
	 * @return the property calculator associated to the slot.
	 */
	public IPropCalculator<C, S> getCalculator() {
		return this._calculator;
	}

	/**
	 * Sets the property calculator associated to the slot.
	 */
	public void setCalculator(IPropCalculator<C, S> calculator) {
		this._calculator = calculator;
	}

	/**
	 * @return the plugged calculated property stencil.
	 */
	@Override
	public S getCalculatedStencil(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
		StencilFactory<C, S> factory = (StencilFactory<C, S>) stclContext.<C, S> getStencilFactory();

		// checks the calculator is defined
		if (this._calculator == null) {
			String msg = String.format("no calculator associated to the calculated property %s", self);
			return StencilUtils.<C, S> nullPStencil(stclContext, Result.error(msg));
		}

		// creates the new property (each time as self may not be same)
		return factory.newPPropStencil(stclContext, self, Key.NO_KEY, getProperty(stclContext));
	}

	protected PropStencil<C, S> getProperty(C stclContext) {

		// if the calculated property is not already created
		if (this._prop == null) {
			StencilFactory<C, S> factory = (StencilFactory<C, S>) stclContext.<C, S> getStencilFactory();
			Class<? extends PropStencil<C, S>> p = ClassHelper.loadClass(factory.getCalculatedPropertyDefaultTemplateName(stclContext));
			this._prop = factory.createStencil(stclContext, p, this._calculator);
		}

		// calculated property defined
		return this._prop;
	}

}