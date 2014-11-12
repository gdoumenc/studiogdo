/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.interpreted;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.gdo.helper.StringHelper;
import com.gdo.stencils.Keywords;
import com.gdo.stencils._Stencil;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.factory.StencilFactory;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug._PStencil;
import com.gdo.util.XmlWriter;

/**
 * <p>
 * Property descriptor class.
 * <p>
 * <p>
 * A property descriptor is a shortcut combining :
 * <ul>
 * <li>a slot descriptor (where the property will be defined)
 * <li>an instance descriptor (the property itself)
 * <li>a plug descriptor (plug the property in the slot)
 * </ul>
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
public final class PropDescriptor<C extends _StencilContext, S extends _PStencil<C, S>> extends _Descriptor<C, S> {
    private enum Type {
        STRING, INT, BOOLEAN
    }

    private String _name; // property name (slot's name)
    private Type _type; // property type
    private String _value; // property value
    private boolean _tranzient; // the property value is not saved
    private String _file; // property file (where the value is defined for
    // locale sensitive value)
    private String _id; // property id (if the value is defined in a file)
    private boolean _expand = true; // the text is formatted by default
    private boolean _calculated; // a calculated prop is java implemented

    private boolean _final; // this slot override another slot
    private boolean _override; // this slot override another slot

    /**
     * @return property (slot's) name.
     */
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    /**
     * @return property type.
     */
    protected String getType() {
        if (_type == Type.BOOLEAN)
            return Keywords.BOOLEAN;
        if (_type == Type.INT)
            return Keywords.INT;
        return Keywords.STRING;
    }

    // property type (string, int, boolean)
    public void setType(String type) {
        if (Keywords.STRING.equals(type)) {
            _type = Type.STRING;
        } else if (Keywords.INT.equals(type)) {
            _type = Type.INT;
        } else if (Keywords.BOOLEAN.equals(type)) {
            _type = Type.BOOLEAN;
        } else {
            if (getLog().isWarnEnabled()) {
                String msg = String.format("Unknow type %s for parameter", type);
                getLog().warn(null, msg);
            }
            _type = Type.STRING;
        }
    }

    // property file for locale sensitive value
    public String getFile() {
        return _file;
    }

    public void setFile(String file) {
        _file = file;
    }

    // property file id for locale sensitive value
    public String getId() {
        return _id;
    }

    public void setId(String id) {
        _id = id;
    }

    // property value in descriptor
    public String getValue() {
        return _value;
    }

    public void setValue(String value) {
        _value = value.replaceAll("<]>", "]]");
    }

    public boolean isTransient() {
        return _tranzient;
    }

    public void setTransient(boolean value) {
        _tranzient = value;
    }

    public boolean isExpand() {
        return _expand;
    }

    public void setExpand(boolean value) {
        _expand = value;
    }

    public boolean isCalculated() {
        return _calculated;
    }

    public void setCalculated(boolean value) {
        _calculated = value;
    }

    /**
     * @return <tt>true</tt> if this slot declaration may not be override in sub
     *         template.
     */
    public boolean isFinal() {
        return _final;
    }

    /**
     * Marks the slot as final and cannot be overriden.
     */
    public void setFinal(boolean value) {
        _final = value;
    }

    /**
     * @return <tt>true</tt> if this slot declaration override an existing slot.
     */
    public boolean isOverride() {
        return _override;
    }

    /**
     * Marks the slot as overriding an existing slot.
     */
    public void setOverride(boolean override) {
        _override = override;
    }

    /**
     * @return the property value in good type.
     * @param tempDesc
     *            template descriptor used only for error message.
     */
    public Object getValue(C stclContext, TemplateDescriptor<C, S> tempDesc) {
        String value = getValue();

        try {
            if (Type.STRING.equals(_type)) {
                if (StringUtils.isEmpty(value))
                    return StringHelper.EMPTY_STRING;
                return value;
            } else if (Type.INT.equals(_type)) {
                if (StringUtils.isEmpty(value))
                    return Integer.valueOf(0);
                return Integer.valueOf(value);
            } else if (Type.BOOLEAN.equals(_type)) {
                if (StringUtils.isEmpty(value))
                    return Boolean.FALSE;
                return Boolean.valueOf(value);
            } else {
                if (getLog().isWarnEnabled()) {
                    getLog().warn(stclContext, "Unknown property type " + _type);
                }
                return null;
            }
        } catch (Exception e) {
            logWarn(stclContext, "Cannot get property value in descriptor %s (%s)", this, e);
        }
        if (Type.INT.equals(_type))
            return new Integer(0);
        if (Type.BOOLEAN.equals(_type))
            return Boolean.FALSE;
        return StringHelper.EMPTY_STRING;
    }

    /**
     * Creates the new local slot.
     * 
     * @param tempDesc
     *            template descriptor used to get the properties file.
     */
    public SlotDescriptor<C, S> expandToSlotDescriptor(C stclContext, TemplateDescriptor<C, S> tempDesc) {
        String slotName = getName();

        // create the new slot descriptor
        SlotDescriptor<C, S> slotDesc = new SlotDescriptor<C, S>(slotName);
        slotDesc.setArity(PSlot.ONE);

        // create the property default with the value
        if (getId() == null) {
            Object value = getValue(stclContext, tempDesc);
            slotDesc.setDefault(new PropertyValueDescriptor(value));
        } else {
            slotDesc.setDefault(new PropertyFileDescriptor());
        }

        // TODO
        // add property proto
        slotDesc.setTransient(isTransient());
        slotDesc.setCalculated(isCalculated());
        slotDesc.setOverride(isOverride());
        slotDesc.expandedFromProp(this);
        return slotDesc;
    }

    @Override
    public void save(C stclContext, XmlWriter declPart, XmlWriter plugPart) throws IOException {

        // checks parameters
        if (plugPart != null) {
            logError(stclContext, "plug xml writer should be null for prop descriptor");
            return;
        }

        // saves prop descriptor
        declPart.startElement("prop");
        declPart.writeAttribute("name", getName());
        declPart.writeAttribute("type", getType());
        if (isExpand()) {
            declPart.writeAttribute("expand", Boolean.TRUE);
        }
        if (!StringUtils.isEmpty(getId())) {
            declPart.writeAttribute("id", getId());
            if (!StringUtils.isEmpty(getFile())) {
                declPart.writeAttribute("file", getId());
            }
        } else {
            declPart.startElement("data");
            declPart.writeCDATAAndEndElement(getValue());
        }
        declPart.endElement("prop");
    }

    private class PropertyValueDescriptor extends DefaultDescriptor<C, S> {
        private Object _value; // value read from the property file

        PropertyValueDescriptor(Object value) {
            _value = value;
        }

        @Override
        public S newInstance(C stclContext, PSlot<C, S> self) {

            // creates the property with initial value
            StencilFactory<C, S> factory = (StencilFactory<C, S>) stclContext.<C, S> getStencilFactory();
            S prop = factory.createPProperty(stclContext, self, Key.NO_KEY, _value);

            // sets the type
            _Stencil<C, S> p = (_Stencil<C, S>) prop.getReleasedStencil(stclContext);
            p.setType(stclContext, getType(), prop);
            p.setExpand(stclContext, isExpand(), prop);

            // completes it
            p.complete(stclContext, prop);

            // prop.setExpand(stclContext, isExpand());
            return prop;
        }
    }

    private class PropertyFileDescriptor extends DefaultDescriptor<C, S> {

        @Override
        public S newInstance(C stclContext, PSlot<C, S> self) {
            StencilFactory<C, S> factory = (StencilFactory<C, S>) stclContext.<C, S> getStencilFactory();
            _Stencil<C, S> stcl = self.getContainer().getReleasedStencil(stclContext);
            List<String> pathes = null;

            // get properties files
            if (StringUtils.isEmpty(PropDescriptor.this._file)) {

                // get hierachy files list if file not defined
                if (stcl.getDescriptor() != null) {
                    pathes = stcl.getDescriptor().getSuperNames(stclContext);
                }
                if (pathes == null)
                    pathes = new ArrayList<String>();

            } else {

                // add the specific file only if defined
                pathes = new ArrayList<String>();
                pathes.add(0, PropDescriptor.this._file);
            }

            // get value from files
            String value = null;
            try {
                Properties properties = new Properties();
                // for (String path : pathes) {
                // InputStream in =
                // ClassUtils.getResourceAsStream(path.replaceAll("[.]", "/") +
                // ".properties", stclContext.getLocale());
                FileInputStream fi = new FileInputStream("/home/studiogdo/java_workspace/ZZZ/src/zh.properties");
                if (fi != null)
                    properties.load(fi);
                // }
                value = properties.getProperty(PropDescriptor.this._id);
            } catch (Exception e) {
                value = e.getMessage();
            }
            if (value == null)
                value = StringHelper.EMPTY_STRING;

            // create the property
            S prop = factory.createPProperty(stclContext, self, Key.NO_KEY, value);
            ((_Stencil<C, S>) prop.getReleasedStencil(stclContext)).setExpand(stclContext, isExpand(), prop);
            return (S) prop;
        }
    }
}