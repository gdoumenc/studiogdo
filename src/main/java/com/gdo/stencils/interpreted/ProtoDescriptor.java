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
		return StringHelper.splitShortString(this._classes, MULTI);
	}

	public void setClasses(String classes) {
		this._classes = classes;
	}

	// slots expected
	public String getSlots() {
		return this._slots;
	}

	public void setSlots(String slots) {
		this._slots = slots;
	}

	// properties expected
	public String getProps() {
		return this._props;
	}

	public void setProps(String props) {
		this._props = props;
	}

	// commands expected
	public String getCommands() {
		return this._commands;
	}

	public void setCommands(String commands) {
		this._commands = commands;
	}

	// template expected
	public String getTemplates() {
		return this._templates;
	}

	public void setTemplates(String templates) {
		this._templates = templates;
	}

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer("proto(");
		if (!StringUtils.isEmpty(this._classes))
			str.append("classes:").append(this._classes);
		if (!StringUtils.isEmpty(this._slots))
			str.append(", slots:").append(this._slots);
		if (!StringUtils.isEmpty(this._props))
			str.append(", props:").append(this._props);
		if (!StringUtils.isEmpty(this._commands))
			str.append(", commands:").append(this._commands);
		str.append(')');
		return str.toString();
	}

	@Override
	public void save(C stclContext, XmlWriter instOut, XmlWriter plugOut) throws IOException {
		if (isEmpty())
			return;
		instOut.startElement("proto");
		if (!StringUtils.isEmpty(this._classes))
			instOut.writeAttribute("classes", this._classes);
		if (!StringUtils.isEmpty(this._slots))
			instOut.writeAttribute("slots", this._slots);
		if (!StringUtils.isEmpty(this._props))
			instOut.writeAttribute("props", this._props);
		if (!StringUtils.isEmpty(this._commands))
			instOut.writeAttribute("commands", this._commands);
		instOut.endElement("proto");
	}

	// return <tt>true</tt> if the proto contains no information
	private boolean isEmpty() {
		return (StringUtils.isEmpty(this._classes) && StringUtils.isEmpty(this._slots) && StringUtils.isEmpty(this._props) && StringUtils.isEmpty(this._commands));
	}

}