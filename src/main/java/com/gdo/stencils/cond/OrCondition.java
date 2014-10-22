/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils.cond;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.plug._PStencil;

/**
 * Condition implementation that returns <tt>true</tt> if either of the
 * conditions is verified. <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 * </blockquote>
 */
public class OrCondition<C extends _StencilContext, S extends _PStencil<C, S>> extends StencilCondition<C, S> {

	private final StencilCondition<C, S> _cond1;
	private final StencilCondition<C, S> _cond2;

	public OrCondition(StencilCondition<C, S> cond1, StencilCondition<C, S> cond2) {
		_cond1 = cond1;
		_cond2 = cond2;
	}

	@Override
	public boolean verify(C stclContext, S stencil) {
		if (_cond1 == null) {
			if (_cond2 == null)
				return true;
			return _cond2.verify(stclContext, stencil);
		}
		if (_cond2 == null)
			return _cond1.verify(stclContext, stencil);
		return _cond1.verify(stclContext, stencil) || _cond2.verify(stclContext, stencil);
	}

	@Override
	public String toSQL(C stclContext, String alias, S stencil) {
		if (_cond1 == null) {
			if (_cond2 == null) {
				return "1";
			}
			return _cond2.toSQL(stclContext, alias, stencil);
		}
		if (_cond2 == null) {
			return _cond1.toSQL(stclContext, alias, stencil);
		}
		return String.format("%s OR %s", _cond1.toSQL(stclContext, alias, stencil), _cond2.toSQL(stclContext, alias, stencil));
	}
}
