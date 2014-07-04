/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.interpreted;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.factory.StencilFactory;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug._PStencil;

/**
 * <p>
 * Default stencil class descriptor associated to a slot.
 * </p>
 * <p>
 * When a stencil must be created for a single slot which has a default
 * descriptor, this default one is used.
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
public class DefaultDescriptor<C extends _StencilContext, S extends _PStencil<C, S>> extends TemplateDescriptor<C, S> {

	public final void setTemplate(String template) {
		setName(template);
		// setExtends(template);
	}

	// create the stencil with parameters
	public S newInstance(C stclContext, PSlot<C, S> self) {
		Object[] params = getParameters(stclContext);
		StencilFactory<C, S> factory = (StencilFactory<C, S>) stclContext.<C, S> getStencilFactory();
		return factory.createPStencil(stclContext, self, Key.NO_KEY, getName(), params);
	}

}