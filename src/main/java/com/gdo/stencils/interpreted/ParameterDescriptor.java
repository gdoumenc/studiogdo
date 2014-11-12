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
            return Byte.parseByte(_index);
        } catch (Exception e) {
            if (getLog().isWarnEnabled()) {
                String msg = String.format("Wrong value %s for parameter index", _index);
                getLog().warn(null, msg);
            }
        }
        return 0;
    }

    // used by digester
    public void setIndex(String index) {
        _index = index;
    }

    // used by digester
    public void setType(String type) {
        _type = type;
    }

    // convert from expected types
    public Object getValue() {
        if (Keywords.INT.equals(_type)) {
            return ConverterHelper.stringToInteger(_value);
        } else if (Keywords.BOOLEAN.equals(_type)) {
            return ConverterHelper.parseBoolean(_value);
        } else if (Keywords.STRING.equals(_type)) {
            return _value;
        }
        if (getLog().isWarnEnabled()) {
            String msg = String.format("Unknow type %s for parameter", _type);
            getLog().warn(null, msg);
        }
        return _value;
    }

    // used by digester
    public void setValue(String value) {
        _value = value.replaceAll("<]>", "]]");
    }

    @Override
    public void save(C stclContext, XmlWriter instPart, XmlWriter plugPart) throws IOException {
        instPart.startElement("param");
        instPart.writeAttribute("index", _index);
        instPart.writeAttribute("type", _type);
        instPart.writeCDATAAndEndElement(_value);
    }

}