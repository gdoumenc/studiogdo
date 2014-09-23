/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.factory;

import java.io.Reader;

import com.gdo.helper.ClassHelper;
import com.gdo.stencils.Result;
import com.gdo.stencils._Stencil;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.log.StencilLog;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.prop.CalculatedPropStencil;
import com.gdo.stencils.util.StencilUtils;
import com.gdo.util.XmlWriter;

/**
 * <p>
 * A stencil factory creates stencils.
 * <p>
 * <p>
 * Stencil factories are defined globally by their class name.
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
public abstract class StencilFactory<C extends _StencilContext, S extends _PStencil<C, S>> implements IStencilFactory<C, S> {

	// default classes
	private static final Class<?> DEFAULT_PSTENCIL_CLASS = _PStencil.class;
	private static final String STENCIL_DEFAULT_TEMPLATE_NAME = _Stencil.class.getName();
	private static final String PROPERTY_DEFAULT_TEMPLATE_NAME = _Stencil.class.getName();
	private static final String CALCULATED_PROPERTY_DEFAULT_TEMPLATE_NAME = CalculatedPropStencil.class.getName();

	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends S> getDefaultPStencilClass(C stclContext) {
		return (Class<? extends S>) DEFAULT_PSTENCIL_CLASS;
	}

	@Override
	public String getStencilDefaultTemplateName(C stclContext) {
		return STENCIL_DEFAULT_TEMPLATE_NAME;
	}

	@Override
	public String getPropertyDefaultTemplateName(C stclContext) {
		return PROPERTY_DEFAULT_TEMPLATE_NAME;
	}

	@Override
	public String getCalculatedPropertyDefaultTemplateName(C stclContext) {
		return CALCULATED_PROPERTY_DEFAULT_TEMPLATE_NAME;
	}

	/**
	 * Creates a new plugged stencil from a stencil, but doesn't perfom the plug.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param slot
	 *          the containing slot.
	 * @param key
	 *          the key.
	 * @param stcl
	 *          the stencil.
	 * @return the plugged stencil.
	 */
	public S newPStencil(C stclContext, PSlot<C, S> slot, IKey key, _Stencil<C, S> stencil) {
		Class<? extends S> pstencilClass = getDefaultPStencilClass(stclContext);
		return ClassHelper.newInstance(pstencilClass, stclContext, stencil, slot, key);
	}

	/**
	 * Creates a plugged stencil from a stencil class name, but doesn't perfom the
	 * plug.
	 * 
	 * @param stclContext
	 *          the stencil conetxt.
	 * @param slot
	 *          the slot containing the stencil.
	 * @param key
	 *          the key for plug.
	 * @param stencilClassName
	 *          the template class name.
	 * @param params
	 *          the tempalte constructor parameters
	 * @return the stencil plugged.
	 */
	public S createPStencil(C stclContext, PSlot<C, S> slot, IKey key, Class<? extends _Stencil<C, S>> clazz, Object... params) {
		_Stencil<C, S> stcl = createStencil(stclContext, clazz, params);

		// an error may occur in construction
		if (stcl == null) {
			String msg = logError(stclContext, "error when creating plugged stencil with class %s", clazz);
			return StencilUtils.<C, S> nullPStencil(stclContext, Result.error(msg));
		}

		// returns plugged stencil
		return newPStencil(stclContext, slot, key, stcl);
	}

	public S createPStencil(C stclContext, PSlot<C, S> slot, IKey key, String className, Object... params) {
		Class<? extends _Stencil<C, S>> clazz = ClassHelper.loadClass(className);
		return createPStencil(stclContext, slot, key, clazz, params);
	}

	/**
	 * Creates a new plugged stencil from an existing plugged stencil.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param slot
	 *          the slot where the stencil is plugged.
	 * @param key
	 *          the key where the stencil is plugged.
	 * @param pstencil
	 *          the previous plugged stencil.
	 * @return the plugged stencil created.
	 */
	public S createPStencil(C stclContext, PSlot<C, S> slot, IKey key, S pstencil) {
        Class<? extends S> pstencilClass = getDefaultPStencilClass(stclContext);

		// checks not a null stencil
		if (pstencil.isNull()) {
			String msg = String.format("cannot create a stencil from a null stencil (%s)", pstencil.getNullReason());
			return ClassHelper.newInstance(pstencilClass, msg);
		}

		// returns plugged stencil
		return ClassHelper.newInstance(pstencilClass, stclContext, pstencil, slot, key);
	}

	/**
	 * Creates a property from default property class name.
	 */
	public <V> S createPProperty(C stclContext, PSlot<C, S> slot, IKey key, V value, Object... params) {
		String v = (value != null) ? value.toString() : "";
		_Stencil<C, S> prop = createPropStencil(stclContext, v, params);

		// an error may occur in construction
		if (prop == null) {
			String msg = logError(stclContext, "error when creating plugged property");
			return StencilUtils.<C, S> nullPStencil(stclContext, Result.error(msg));
		}

		// returns plugged property
		S pprop = newPStencil(stclContext, slot, key, prop);
		prop.complete(stclContext, pprop);
		return pprop;
	}

	/**
	 * Creates a stencil implementation from a stencil class. The first
	 * constructor parameter (C class) is added automatically.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param clazz
	 *          the template class.
	 * @param params
	 *          the class constructor parameters.
	 * @return the stencil created.
	 */
	public <T extends _Stencil<C, S>> T createStencil(C stclContext, Class<T> clazz, Object... params) {
		T stcl = null;

		// calls directly constructor if no parameter
		if (ClassHelper.isEmpty(params)) {
			stcl = ClassHelper.newInstance(clazz, stclContext);
		} else {

			// first null parameters stops parameters list
			int length = 0;
			for (Object p : params) {
				if (p == null) {
					break;
				}
				length++;
			}

			// adds stencil context as first parameters (first null parameters stops)
			Object p[] = new Object[length + 1];
			
			if (clazz.getEnclosingClass() == null) {
				p[0] = stclContext;
				for (int i = 0; i < length; i++) {
					p[i + 1] = params[i];
				}
			} else {
				p[0] = params[0];
				p[1] = stclContext;
				for (int i = 1; i < length; i++) {
					p[i + 1] = params[i];
				}
			}

			// creates the instance
			stcl = ClassHelper.newInstance(clazz, p);
		}

		// completes it
		if (stcl != null) {
			Class<? extends S> pstencilClass = getDefaultPStencilClass(stclContext);
			S pstcl = ClassHelper.newInstance(pstencilClass, stclContext, stcl, null, Key.NO_KEY);
			stcl.beforeCompleted(stclContext, pstcl);
			stcl.complete(stclContext, pstcl);
			stcl.afterCompleted(stclContext, pstcl);
		}
		return stcl;
	}

	/**
	 * Creates a property implementation using default property class name.
	 * 
	 * @param value
	 *          initial value of the property.
	 * @return the property created. <tt>null</tt> if cannot be created.
	 */
	public _Stencil<C, S> createPropStencil(C stclContext, String value, Object... params) {
		Class<? extends _Stencil<C, S>> propClass = ClassHelper.loadClass(getPropertyDefaultTemplateName(stclContext));
		try {
			int i = 0;
			Object[] p = new Object[params.length + 1];
			p[i++] = value;
			for (Object param : params) {
				p[i++] = param;
			}
			_Stencil<C, S> stencil = createStencil(stclContext, propClass, p);
			return stencil;
		} catch (Exception e) {
			logError(stclContext, "Cannot create a default property %s", propClass);
		}
		return null;
	}

	public _Stencil<C, S> loadStencil(C stclContext, Reader in) {
		throw new UnsupportedOperationException("StencilFactory : cannot load stencil");
	}

	@Override
	public void saveStencil(C stclContext, S stencil, XmlWriter writer) {
		throw new UnsupportedOperationException("StencilFactory : cannot save stencil");
	}

	//
	// LOG PART
	//

	private static final StencilLog _LOG = new StencilLog(StencilFactory.class);

	protected static <C extends _StencilContext> String logError(C stclContext, String format, Object... params) {
		if (_LOG.isErrorEnabled()) {
			String msg = (params.length == 0) ? format : String.format(format, params);
			_LOG.error(stclContext, msg);
			return msg;
		}
		return "";
	}
}