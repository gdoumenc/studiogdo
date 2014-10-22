/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.interpreted;

import static com.gdo.stencils.util.PathUtils.MULTI;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.gdo.helper.StringHelper;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.plug._PStencil;
import com.gdo.util.XmlWriter;

/**
 * <p>
 * Prototype descriptor class defined for a plug descriptor.
 * <p>
 * <p>
 * A prototype descriptor is a shortcut combining :
 * <ul>
 * <li>either classes expected (: separated)
 * <li>either slots expected (: separated)
 * <li>either commands expected (: separated)
 * </ul>
 * in the stencil plugged.
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
public final class ProtoDescriptor<C extends _StencilContext, S extends _PStencil<C, S>> extends _Descriptor<C, S> {

	private String _classes; // classes expected (: separated)
	private String _slots; // slots expected (: separated)
	private String _props; // props expected (: separated)
	private String _commands; // commands expected (: separated)
	private String _templates; // templates expected : slots, props, ... (:

	// separated)

	// classes expected
	public String[] getClasses() {
		return StringHelper.splitShortString(_classes, MULTI);
	}

	public void setClasses(String classes) {
		_classes = classes;
	}

	// slots expected
	public String getSlots() {
		return _slots;
	}

	public void setSlots(String slots) {
		_slots = slots;
	}

	// properties expected
	public String getProps() {
		return _props;
	}

	public void setProps(String props) {
		_props = props;
	}

	// commands expected
	public String getCommands() {
		return _commands;
	}

	public void setCommands(String commands) {
		_commands = commands;
	}

	// template expected
	public String getTemplates() {
		return _templates;
	}

	public void setTemplates(String templates) {
		_templates = templates;
	}

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer("proto(");
		if (!StringUtils.isEmpty(_classes))
			str.append("classes:").append(_classes);
		if (!StringUtils.isEmpty(_slots))
			str.append(", slots:").append(_slots);
		if (!StringUtils.isEmpty(_props))
			str.append(", props:").append(_props);
		if (!StringUtils.isEmpty(_commands))
			str.append(", commands:").append(_commands);
		str.append(')');
		return str.toString();
	}

	@Override
	public void save(C stclContext, XmlWriter instOut, XmlWriter plugOut) throws IOException {
		if (isEmpty())
			return;
		instOut.startElement("proto");
		if (!StringUtils.isEmpty(_classes))
			instOut.writeAttribute("classes", _classes);
		if (!StringUtils.isEmpty(_slots))
			instOut.writeAttribute("slots", _slots);
		if (!StringUtils.isEmpty(_props))
			instOut.writeAttribute("props", _props);
		if (!StringUtils.isEmpty(_commands))
			instOut.writeAttribute("commands", _commands);
		instOut.endElement("proto");
	}

	// return <tt>true</tt> if the proto contains no information
	private boolean isEmpty() {
		return (StringUtils.isEmpty(_classes) && StringUtils.isEmpty(_slots) && StringUtils.isEmpty(_props) && StringUtils.isEmpty(_commands));
	}

}