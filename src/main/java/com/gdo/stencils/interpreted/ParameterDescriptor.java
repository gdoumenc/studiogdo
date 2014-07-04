/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.interpreted;

import java.io.IOException;

import com.gdo.helper.ConverterHelper;
import com.gdo.stencils.Keywords;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.plug._PStencil;
import com.gdo.util.XmlWriter;

/**
 * <p>
 * A parameter may be defined to be used in a stencil constructor.
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
public final class ParameterDescriptor<C extends _StencilContext, S extends _PStencil<C, S>> extends _Descriptor<C, S> {

	private String _index; // parameter position index (starts from 0)
	private String _type; // parameter type
	private String _value; // default value

	// if name is simply getIndex then digester won't use setIndex
	public byte getIndexAsByte() {
		try {
			return Byte.parseByte(this._index);
		} catch (Exception e) {
			if (getLog().isWarnEnabled()) {
				String msg = String.format("Wrong value %s for parameter index", this._index);
				getLog().warn(null, msg);
			}
		}
		return 0;
	}

	// used by digester
	public void setIndex(String index) {
		this._index = index;
	}

	// used by digester
	public void setType(String type) {
		this._type = type;
	}

	// convert from expected types
	public Object getValue() {
		if (Keywords.INT.equals(this._type)) {
			return ConverterHelper.stringToInteger(this._value);
		} else if (Keywords.BOOLEAN.equals(this._type)) {
			return ConverterHelper.parseBoolean(this._value);
		} else if (Keywords.STRING.equals(this._type)) {
			return this._value;
		}
		if (getLog().isWarnEnabled()) {
			String msg = String.format("Unknow type %s for parameter", this._type);
			getLog().warn(null, msg);
		}
		return this._value;
	}

	// used by digester
	public void setValue(String value) {
		this._value = value.replaceAll("<]>", "]]");
	}

	@Override
	public void save(C stclContext, XmlWriter instPart, XmlWriter plugPart) throws IOException {
		instPart.startElement("param");
		instPart.writeAttribute("index", this._index);
		instPart.writeAttribute("type", this._type);
		instPart.writeCDATAAndEndElement(this._value);
	}

}