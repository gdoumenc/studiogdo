/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.factory;

import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import com.gdo.helper.ClassHelper;
import com.gdo.stencils._Stencil;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.interpreted.CommandDescriptor;
import com.gdo.stencils.interpreted.DefaultDescriptor;
import com.gdo.stencils.interpreted.InstDescriptor;
import com.gdo.stencils.interpreted.InstanceRepository;
import com.gdo.stencils.interpreted.LinkDescriptor;
import com.gdo.stencils.interpreted.ParameterDescriptor;
import com.gdo.stencils.interpreted.PlugDescriptor;
import com.gdo.stencils.interpreted.PropDescriptor;
import com.gdo.stencils.interpreted.ProtoDescriptor;
import com.gdo.stencils.interpreted.SlotDescriptor;
import com.gdo.stencils.interpreted.StencilDescriptor;
import com.gdo.stencils.interpreted.TemplateDescriptor;
import com.gdo.stencils.interpreted.UnplugDescriptor;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.util.PathUtils;
import com.gdo.util.XmlWriter;

/**
 * <p>
 * This stencil factory creates stencils from XML stencil description file.
 * <p>
 * <p>
 * Stencil description file syntax is defined in <tt>stencil.dtd</tt>.
 * </p>
 * <p>
 * <tt>S</tt> is the stencil class for stencils created, <tt>T</tt> is the
 * default class used to create empty stencil.
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
public class InterpretedStencilFactory<C extends _StencilContext, S extends _PStencil<C, S>> extends StencilFactory<C, S> {

	private static final String DTD_TYPE = "-//StudioGdo//DTD Stencil 1.0//EN";
	private static final String DTD_FILE = "com/gdo/stencils/interpreted/stencil.dtd";

	// DTD keywords
	public static final String STENCIL = "stencil";
	public static final String INSTANCE = "inst";
	public static final String TEMPLATE = "template";

	private static final String COMMAND = "/command";
	private static final String DATA = "/data";
	private static final String DEFAULT = "/default";
	private static final String INST = "/inst";
	private static final String LINK = "/link";
	private static final String PARAM = "/param";
	private static final String PLUG = "/plug";
	private static final String UNPLUG = "/unplug";
	private static final String PROP = "/prop";
	private static final String PROTO = "/proto";
	private static final String SLOT = "/slot";

	// digester methods
	private static final String ADD_COMMAND_DESC = "addCommandDescriptor";
	private static final String ADD_INST_DESC = "addInstDescriptor";
	private static final String ADD_SLOT_DESC = "addSlotDescriptor";
	private static final String ADD_PARAM_DESC = "addParamDescriptor";
	private static final String ADD_LINK_DESC = "addLinkDescriptor";
	private static final String ADD_PLUG_DESC = "addPlugDescriptor";
	private static final String ADD_UNPLUG_DESC = "addUnplugDescriptor";
	private static final String ADD_PROP_DESC = "addPropDescriptor";
	private static final String ADD_PROTO_DESC = "addProtoDescriptor";

	private static final String SET_DEFAULT = "setDefault";
	private static final String SET_VALUE = "setValue";

	// private static final boolean reload = true;
	// //Boolean.valueOf(ClassHelper.getConfigProperty("template.reload")).booleanValue();;

	private final Digester _digester;
	private static final Map<String, TemplateDescriptor<?, ?>> TEMPLATE_DESCRIPTORS = Collections.synchronizedMap(new WeakHashMap<String, TemplateDescriptor<?, ?>>());

	public InterpretedStencilFactory() {

		// create digester
		this._digester = new Digester();
		URL dtd = ClassHelper.getResource(DTD_FILE);
		this._digester.register(DTD_TYPE, dtd.toString());
		this._digester.setValidating(false);
		this._digester.setUseContextClassLoader(true);
		this._digester.setErrorHandler(new MyErrorHandler());

		/*
		 * template part
		 */
		this._digester.addObjectCreate(TEMPLATE, TemplateDescriptor.class);
		this._digester.addSetProperties(TEMPLATE);

		// local definition
		addParam(TEMPLATE);
		addCommand(TEMPLATE, 0);
		addInst(TEMPLATE, 0);
		addSlot(TEMPLATE);
		addProp(TEMPLATE);
		addPlug(TEMPLATE);
		addUnplug(TEMPLATE);

		/*
		 * stencil part
		 */
		this._digester.addObjectCreate(STENCIL, StencilDescriptor.class);
		this._digester.addSetProperties(STENCIL);

		// slot and command part
		addParam(STENCIL);
		addCommand(STENCIL, 0);
		addInst(STENCIL, 0);
		addSlot(STENCIL);
		addProp(STENCIL);
		addPlug(STENCIL);
		addUnplug(STENCIL);
	}

	@SuppressWarnings("unchecked")
	public <T extends _Stencil<C, S>> T createStencil(C stclContext, TemplateDescriptor<C, S> tempDesc, Object... params) {
		InstanceRepository<C, S> instances = new InstanceRepository<C, S>();
		return (T) newStencil(stclContext, instances, PathUtils.THIS, tempDesc, params);
	}

	@Override
	public <T extends _Stencil<C, S>> T createStencil(C stclContext, Class<T> clazz, Object... params) {
		TemplateDescriptor<C, S> tempDesc = getTemplateDescriptor(stclContext, clazz.getName());
		if (tempDesc != null) {
			return createStencil(stclContext, tempDesc, params);
		}

		// creates it from class
		T stcl = super.createStencil(stclContext, clazz, params);

		// completes from hierarchy template descriptors
		if (clazz.equals(_Stencil.class)) {
			return stcl;
		}
		Class<?> c = clazz;
		while (tempDesc == null && !c.equals(_Stencil.class)) {
			c = c.getSuperclass();
			tempDesc = getTemplateDescriptor(stclContext, c.getName());
		}
		if (c.equals(_Stencil.class)) {
			return stcl;
		}
		InstanceRepository<C, S> instances = new InstanceRepository<C, S>();
		S stored = instances.store(stclContext, PathUtils.THIS, stcl);
		stcl.beforeCompleted(stclContext, stored);
		tempDesc.completeStencil(stclContext, stored, instances, 0);
		stcl.afterCompleted(stclContext, stored);

		return stcl;
	}

	@SuppressWarnings("unchecked")
	public <T extends _Stencil<C, S>> T newStencil(C stclContext, InstanceRepository<C, S> instances, String path, TemplateDescriptor<C, S> tempDesc, Object... params) {
		if (tempDesc == null) {
			return null;
		}

		// gets default parameters from descriptor if no parameters
		Object[] parameters = params;
		if (parameters == null) {
			parameters = tempDesc.getParameters(stclContext);
		}

		// creates stencil from class, add descriptor
		Class<? extends _Stencil<C, S>> clazz = tempDesc.getStencilClass(stclContext);
		if (clazz == null) {
			logError(stclContext, "Cannot load class for template %s", tempDesc.getTemplateName());
			return null;
		}

		// creates it without template completion
		_Stencil<C, S> stcl = super.createStencil(stclContext, clazz, params);
		if (stcl == null) {
			return null;
		}

		// stores the stencil as it can be used in completion
		stcl.setDescriptor(tempDesc);
		S stored = instances.store(stclContext, path, stcl);

		// completes stencil from XLM descriptor
		// stcl.beforeCompleted(stclContext, stored);
		try {
			instances.push(path);
			completeStencilFromTemplate(stclContext, stored, instances, 0);
			instances.pop();
		} catch (Exception e) {
			logError(stclContext, "Cannot complete stencil of type %s", tempDesc);
		}
		// stcl.afterCompleted(stclContext, stored);

		return (T) stcl;
	}

	/**
	 * Returns the template descriptor from name (parsing template description
	 * file if needed). Need to be synchronized as using parser (TODO replace it
	 * by SAX!!!)
	 */
	@SuppressWarnings({ "unchecked", "static-access" })
	public TemplateDescriptor<C, S> getTemplateDescriptor(C stclContext, String className) {

		// template class name cannot be blank
		if (StringUtils.isBlank(className)) {
			return null;
		}

		// synchronized to avoid several parsing at same time
		synchronized (TEMPLATE_DESCRIPTORS) {

			// change template class name to template stencil xml filename
			String res = className.replace('.', '/') + ".xml";

			// if already exists
			if (this.TEMPLATE_DESCRIPTORS.containsKey(className)) {
				return (TemplateDescriptor<C, S>) this.TEMPLATE_DESCRIPTORS.get(className);
			}

			// parses it and set descriptor attributes
			try {

				// InputStream in = IOHelper.getInputStream(res,
				// stclContext.getTemplatePathes(), null, true);
				InputStream in = ClassHelper.getResourceAsStream(res);
				if (in == null) {
					return null;
				}

				// parses the description
				InterpretedStencilFactory<C, S> factory = (InterpretedStencilFactory<C, S>) stclContext.<C, S> getStencilFactory();
				TemplateDescriptor<C, S> tempDesc = (TemplateDescriptor<C, S>) factory._digester.parse(in);

				// all plugs in a template are in creation mode
				tempDesc.forcePlugsInCreationMode();
				tempDesc.setName(className);

				// stores descriptor
				this.TEMPLATE_DESCRIPTORS.put(className, tempDesc);

				return tempDesc;
			} catch (SAXParseException e) {
				int col = e.getColumnNumber();
				int line = e.getLineNumber();
				logError(stclContext, "cannot parse template %s (l:%s, c%s)", className, Integer.toString(line), Integer.toString(col));
				return null;
			} catch (Exception e) {
				logError(stclContext, "cannot parse template %s", className);
				return null;
			}
		}
	}

	private void completeStencilFromTemplate(C stclContext, S stencil, InstanceRepository<C, S> instances, int completionLevel) {
		try {

			// gets template descriptor
			_Stencil<C, S> stcl = stencil.getReleasedStencil(stclContext);
			Class<?> clazz = stcl.getClass();
			if (clazz.equals(_Stencil.class)) {
				return;
			}

			TemplateDescriptor<C, S> tempDesc = stcl.getDescriptor();
			while (tempDesc == null && !clazz.equals(_Stencil.class)) {
				clazz = clazz.getSuperclass();
				tempDesc = getTemplateDescriptor(stclContext, clazz.getName());
			}
			if (clazz.equals(_Stencil.class)) {
				return;
			}
			tempDesc.completeStencil(stclContext, stencil, instances, completionLevel);
		} catch (Exception e) {
			logError(stclContext, "Error when completing stencil : %s", e);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public synchronized _Stencil<C, S> loadStencil(C stclContext, Reader in, String name) {
		try {
			Object parsed = this._digester.parse(in);
			if (parsed instanceof StencilDescriptor) {
				StencilDescriptor<C, S> descriptor = (StencilDescriptor<C, S>) parsed;
				String rootId = descriptor.getId();
				InstanceRepository<C, S> instances = new InstanceRepository<C, S>(rootId);
				return descriptor.createInstance(stclContext, rootId, instances, 0);
			}
		} catch (SAXParseException e) {
			logError(stclContext, "'t:%s, l:%s, c:%s : %s)", name, Integer.toString(e.getLineNumber()), Integer.toString(e.getColumnNumber()), e.getMessage());
		} catch (Exception e) {
			logError(stclContext, "Error when loading stencil %s", e);
		}
		return null;
	}

	@Override
	public void saveStencil(C stclContext, S stencil, XmlWriter writer) {
		try {
			stclContext.setSaveWriter(writer);
			_Stencil<C, S> stcl = stencil.getReleasedStencil(stclContext);
			if (stcl != null) {
				stcl.saveAsStencil(stclContext, writer, stencil);
			} else {
				logError(stclContext, "Null stencil when saving!!!");
			}
		} catch (Exception e) {
			logError(stclContext, "Error when saving stencil : %s", e);
		}
	}

	private void addCommand(String prefix, int level) {
		String path = prefix + COMMAND;
		add(path, CommandDescriptor.class, ADD_COMMAND_DESC);
		addParam(path);
		if (level < 10)
			addInst(path, level + 1);
		if (level < 10)
			addCommand(path, level + 1);
		addProp(path);
		addSlot(path);
		addPlug(path);
		addUnplug(path);
	}

	private void addInst(String prefix, int level) {
		String path = prefix + INST;
		add(path, InstDescriptor.class, ADD_INST_DESC);
		addParam(path);
		if (level < 10)
			addInst(path, level + 1);
		if (level < 10)
			addCommand(path, level + 1);
		addProp(path);
		addSlot(path);
		addPlug(path);
		addUnplug(path);
	}

	private void addSlot(String prefix) {
		String path = prefix + SLOT;
		add(path, SlotDescriptor.class, ADD_SLOT_DESC);
		addParam(path); // parameters for factory
		addProto(path); // add prototypes
		add(path + DEFAULT, DefaultDescriptor.class, SET_DEFAULT);
		addParam(path + DEFAULT); // parameters for default creation
		addLink(path);
	}

	private void addPlug(String prefix) {
		String path = prefix + PLUG;
		add(path, PlugDescriptor.class, ADD_PLUG_DESC);
	}

	private void addUnplug(String prefix) {
		String path = prefix + UNPLUG;
		add(path, UnplugDescriptor.class, ADD_UNPLUG_DESC);
	}

	private void addLink(String prefix) {
		String path = prefix + LINK;
		add(path, LinkDescriptor.class, ADD_LINK_DESC);
	}

	private void addParam(String prefix) {
		String path = prefix + PARAM;
		this._digester.addObjectCreate(path, ParameterDescriptor.class);
		this._digester.addSetProperties(path);
		this._digester.addCallMethod(path, SET_VALUE, 0);
		this._digester.addSetNext(path, ADD_PARAM_DESC);
	}

	private void addProp(String prefix) {
		String path = prefix + PROP;
		this._digester.addObjectCreate(path, PropDescriptor.class);
		this._digester.addSetProperties(path);
		this._digester.addCallMethod(path + DATA, SET_VALUE, 0);
		this._digester.addSetNext(path, ADD_PROP_DESC);
	}

	private void addProto(String prefix) {
		String path = prefix + PROTO;
		this._digester.addObjectCreate(path, ProtoDescriptor.class);
		this._digester.addSetProperties(path);
		this._digester.addSetNext(path, ADD_PROTO_DESC);
	}

	private void add(String path, Class<?> clazz, String call) {
		this._digester.addObjectCreate(path, clazz);
		this._digester.addSetProperties(path);
		this._digester.addSetNext(path, call);
	}

	class MyErrorHandler implements ErrorHandler {
		@Override
		public void warning(SAXParseException e) throws SAXParseException {
			throw e;
		}

		@Override
		public void error(SAXParseException e) throws SAXParseException {
			throw e;
		}

		@Override
		public void fatalError(SAXParseException e) throws SAXParseException {
			throw e;
		}
	}

}