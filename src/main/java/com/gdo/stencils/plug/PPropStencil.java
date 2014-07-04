/*
 * Copyright GDO - 2004
 */
package com.gdo.stencils.plug;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import com.gdo.helper.StringHelper;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.prop.IPPropStencil;
import com.gdo.stencils.prop.PropStencil;

/**
 * <p>
 * Basic implementation of {@link com.gdo.stencils.plug._PStencil}
 * </p>
 * <p>
 * As the <tt>PluggedStencil</tt> structure is very often used in this model,
 * all methods are final to optimize memory allocation.
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
public class PPropStencil<C extends _StencilContext, S extends _PStencil<C, S>, V> extends _PStencil<C, S> {

	public PPropStencil(PropStencil<C, S> prop, PSlot<C, S> slot, IKey key) {
		super(null, prop, slot, key);
	}

	/*
	 * public PPropStencil(PropStencil<C, S, V> prop, PSlot<C, S> slot, String
	 * key) { super(prop, slot, key); } public PPropStencil(PropStencil<C, S, V>
	 * prop, PSlot<C, S> slot, int key) { super(prop, slot, key); }
	 */

	@Override
	public boolean isProp(C stclContext) {
		return true;
	}

	@Override
	public IPPropStencil<C, S> asProp(C stclContext) {
		return this;
	}

	@Override
	public String getType(C stclContext) {
		PropStencil<C, S> prop = getProperty(stclContext);
		return prop.getType(stclContext, self());
	}

	@Override
	public boolean isExpand(C stclContext) {
		PropStencil<C, S> prop = getProperty(stclContext);
		return prop.isExpand(stclContext, self());
	}

	@Override
	public void setExpand(C stclContext, boolean expand) {
		PropStencil<C, S> prop = getProperty(stclContext);
		prop.setExpand(stclContext, expand, self());
	}

	public Reader getReader(C stclContext) {
		if (getValue(stclContext) == null)
			return StringHelper.EMPTY_STRING_READER;
		return new StringReader(getValue(stclContext).toString());
	}

	@Override
	public InputStream getInputStream(C stclContext) {
		try {
			if (getValue(stclContext) != null) {
				return new BufferedInputStream(new ByteArrayInputStream(getValue(stclContext).toString().getBytes(_StencilContext.getCharacterEncoding())));
			}
		} catch (UnsupportedEncodingException e) {
		}
		return StringHelper.EMPTY_STRING_INPUT_STREAM;
	}

	@Override
	public String getValue(C stclContext) {
		PropStencil<C, S> prop = getProperty(stclContext);
		return prop.getValue(stclContext, self());
	}

	@Override
	public void setValue(C stclContext, String value) {
		PropStencil<C, S> prop = getProperty(stclContext);
		prop.setValue(stclContext, value, self());
	}

	private PropStencil<C, S> getProperty(C stclContext) {
		return (PropStencil<C, S>) getStencil(stclContext);
	}

}