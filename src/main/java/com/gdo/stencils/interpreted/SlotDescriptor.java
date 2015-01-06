/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.interpreted;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;

import com.gdo.helper.ClassHelper;
import com.gdo.project.adaptor.LinkStcl;
import com.gdo.stencils._Stencil;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.slot.MultiSlot;
import com.gdo.stencils.slot.SingleSlot;
import com.gdo.stencils.slot._Slot;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.SlotUtils;
import com.gdo.util.XmlStringWriter;
import com.gdo.util.XmlWriter;

/**
 * <p>
 * _Slot descriptor class.
 * <p>
 * 
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 */
public final class SlotDescriptor<C extends _StencilContext, S extends _PStencil<C, S>> extends _Descriptor<C, S> {

    private String _name; // slot name
    private char _arity = PSlot.UNDEFINED; // slot arity (may be defined only
                                           // thru
                                           // methods)
    private String _parentKey; // parent key for dual slot (parent hierarchy)
    private boolean _tranzient; // a slot is not saved
    private boolean _calculated; // a calulcated slot is java implemented
    private DefaultDescriptor<C, S> _default; // default instance plugged
    private ArrayList<LinkDescriptor<C, S>> _links; // list of link descriptors
    private ProtoDescriptor<C, S> _proto; // stencil prototype expected for plug
    private String _factory; // factory class for the slot (must extends the
    // _Slot class)

    private boolean _redefined; // the slot is redefined in the instance

    private boolean _final; // this slot override another slot

    private String _delegatePath; // slot path for delegation
    private boolean _local; // the slot is considered as local (the container is
    // not the target container)

    private String _includePath; // slot paths for composite slot

    private PropDescriptor<C, S> _propDesc; // if created by a prop tag (must be

    // saved as a prop tag)

    public SlotDescriptor() {
        // used by digester
    }

    // used to create it directly (prop, delegate, ...)
    public SlotDescriptor(String name) {
        setName(name);
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public char getArity() {
        return _arity;
    }

    public void setArity(char arity) {
        _arity = arity;
    }

    /**
     * Sets parent key.
     * 
     * @param parent
     *            parent's key.
     */
    public void setParent(String parent) {
        _parentKey = parent;
    }

    public String getParent() {
        return _parentKey;
    }

    public boolean isTransient() {
        return (isDelegated() || isCalculated()) ? true : _tranzient;
    }

    public void setTransient(boolean value) {
        _tranzient = value;
    }

    public boolean isCalculated() {
        return _calculated;
    }

    public void setCalculated(boolean value) {
        _calculated = value;
    }

    public boolean isDelegated() {
        return !StringUtils.isEmpty(_delegatePath);
    }

    public String getDelegate() {
        return _delegatePath;
    }

    public void setDelegate(String path) {
        _delegatePath = path;
    }

    public boolean isLocal() {
        return _local;
    }

    public void setLocal(boolean value) {
        _local = value;
    }

    public DefaultDescriptor<C, S> getDefault() {
        return _default;
    }

    public void setDefault(DefaultDescriptor<C, S> def) {
        _default = def;
    }

    /**
     * @return the factory that must be used to create the slot.
     */
    public String getFactory() {
        return _factory;
    }

    /**
     * Sets the factory that must be used to create the slot.
     */
    public void setFactory(String factory) {
        _factory = factory;
    }

    /**
     * @return <tt>true</tt> if the slot is redefined locally.
     */
    public boolean isRedefined() {
        return _redefined;
    }

    /**
     * Marks the slot as redefined locally. The slot description will be saved
     * in the instance description.
     */
    public void setRedefined(boolean value) {
        _redefined = value;
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

    public Collection<LinkDescriptor<C, S>> getLinkDescriptors() {
        if (_links != null) {
            return _links;
        }
        return Collections.emptyList();
    }

    public void addLinkDescriptor(LinkDescriptor<C, S> link) {
        if (_links == null) {
            _links = new ArrayList<LinkDescriptor<C, S>>();
        }
        _links.add(link);
    }

    public ProtoDescriptor<C, S> getProto() {
        return _proto;
    }

    public void addProtoDescriptor(ProtoDescriptor<C, S> proto) {
        _proto = proto;
    }

    public void setPath(String path) {
        _includePath = path;
    }

    public boolean isComposite() {
        return (_includePath != null);
    }

    public final PropDescriptor<C, S> expandedFromProp() {
        return _propDesc;
    }

    public final void expandedFromProp(PropDescriptor<C, S> propDesc) {
        _propDesc = propDesc;
    }

    /**
     * Creates the slot in the container.
     * 
     * @param completionLevel
     *            if more than the slot's one do nothing as redefined.
     */
    @SuppressWarnings("deprecation")
    public void createSlot(C stclContext, S container, int completionLevel) {

        // if the slot is renamed then this descriptor should be also renamed
        String name = getName();
        _Stencil<C, S> stcl = container.getReleasedStencil(stclContext);

        // verify parameters
        if (isCalculated() && isDelegated()) {
            logWarn(stclContext, "The slot %s cannot be both calculated and delegated", name);
        }

        // get already existing status
        boolean hasSlot = (stcl.getSlots().get(name) != null);
        if (hasSlot) {
            _Slot<C, S> s = stcl.getSlots().get(name);
            if (!s._fromXML) {
                return;
            }
        }

        /*
         * if (isCalculated() && !hasSlot && false) { String msg = String.format(
         * "The slot %s is calculated but has no java implementation", name); if
         * (getLog().isWarnEnabled()) { getLog().warn(stclContext, msg); } }
         */

        // hides the slot
        if (SlotUtils.isHidden(this)) {
            if (hasSlot) {
                if (getLog().isTraceEnabled()) {
                    String msg = String.format("Hiding slot %s in %s", name, container);
                    getLog().trace(stclContext, msg);
                }
                stcl.removeSlot(stclContext, name);
            } else {
                if (getLog().isWarnEnabled()) {
                    String msg = String.format("Cannot hide undeclared slot %s in %s", name, container);
                    getLog().warn(stclContext, msg);
                }
            }
            return;
        }

        // if slot exists perhaps it is a mistake
        if (hasSlot) {
            if (isCalculated()) {
                return; // already defined as defined in java (normal)
            }
            if (completionLevel > 0) {

                // final slot cannot be redefined, will override previously
                // defined slot
                if (isFinal()) {
                    String msg = String.format("Final slot %s in %s cannot be override", name, container);
                    getLog().warn(stclContext, msg);
                }
                return; // the slot is not created by this descriptor
            }

            // may be redefined over an existing one.
            if (getLog().isWarnEnabled()) {
                String msg = String.format("Duplicate slot %s definition in %s", name, container);
                getLog().warn(stclContext, msg);
            }
        }

        // creates the slot from factory class name
        if (!StringUtils.isEmpty(getFactory())) {
            if (getLog().isTraceEnabled()) {
                getLog().trace(stclContext, "Creating slot from factory " + getFactory());
            }
            _Slot<C, S> slot = createSlotFromFactory(stclContext, completionLevel, container);
            slot._fromXML = true;
            return;
        }

        // creates delegated slot
        if (isDelegated()) {

            // create delagated sot
            if (getLog().isTraceEnabled()) {
                String msg = String.format("Creating delegate slot in %s", container);
                getLog().trace(stclContext, msg);
            }

            // default arity is any
            if (getArity() == PSlot.UNDEFINED)
                setArity(PSlot.ANY);

            // create a the slot
            _Slot<C, S> slot;
            if (SlotUtils.isSingle(this))
                slot = createSingle(stclContext, completionLevel, container);
            else
                slot = createMulti(stclContext, completionLevel, container);
            slot.setDescriptor(this);
            slot._fromXML = true;

            // REPLACED = plug link stencil in it
            // set default as link delegation
            String path = PathUtils.compose(PathUtils.PARENT, getDelegate()); // delegate
            // path
            // is
            // relative
            // to
            // slot,
            // not
            // the
            // link
            /*
             * DefaultDescriptor<C, S> def = new DefaultDescriptor<C, S>();
             * def.setTemplate(factory.getLinkDefaultTemplateName(stclContext));
             * ParameterDescriptor<C, S> param = new ParameterDescriptor<C, S>();
             * param.setIndex("0"); param.setValue(path);
             * def.addParamDescriptor(param); setDefault(def);
             */
            container.newPStencil(stclContext, name, Key.NO_KEY, LinkStcl.class.getName(), path);
            return;
        }

        // creates single slot
        if (SlotUtils.isSingle(this)) {
            _Slot<C, S> slot = createSingle(stclContext, completionLevel, container);
            slot._fromXML = true;
            return;
        }

        // creates multi slots
        else if (SlotUtils.isMultiple(this)) {
            _Slot<C, S> slot = createMulti(stclContext, completionLevel, container);
            slot._fromXML = true;
            return;
        }

        // strange arity...
        else {
            if (getLog().isWarnEnabled()) {
                String msg = String.format("Strange slot arity for %s in stencil %s (arity should be defined by java)", this, container);
                getLog().warn(stclContext, msg);
            }
            _Slot<C, S> slot = createMulti(stclContext, completionLevel, container);
            slot._fromXML = true;
            return;
        }
    }

    @Override
    public void save(C stclContext, XmlWriter declPart, XmlWriter plugPart) throws IOException {

        // checks parameters
        if (plugPart != null) {
            logError(stclContext, "plug xml writer should be null for prop descriptor");
            return;
        }

        declPart.startElement("slot");
        declPart.writeAttribute("name", getName());
        declPart.writeAttribute("final", isFinal());
        if (isDelegated()) {
            declPart.writeAttribute("delegate", getDelegate());
            declPart.writeAttribute("local", Boolean.toString(isLocal()));
        } else {
            declPart.writeAttribute("arity", getArity());
            if (!StringUtils.isEmpty(getFactory()))
                declPart.writeAttribute("factory", getFactory());
        }
        declPart.writeAttribute("calculated", isCalculated());
        declPart.endElement("slot");
    }

    private void verifySingle(C stclContext, S container) {
    }

    private void verifyMulti(C stclContext, S container) {
    }

    /**
     * Creates a slot from factory class.
     */
    private _Slot<C, S> createSlotFromFactory(C stclContext, int completionLevel, S container) {
        if (getLog().isTraceEnabled()) {
            String msg = String.format("Creating slot %s in %s from factory %s", this, container, getFactory());
            getLog().trace(stclContext, msg);
        }

        try {
            Class<_Slot<C, S>> cl = ClassHelper.loadClass(getFactory());
            if (cl == null) {
                if (getLog().isWarnEnabled()) {
                    String msg = String.format("Cannot find slot class %s for slot %s in %s", getFactory(), getName(), container);
                    getLog().warn(stclContext, msg);
                }
                return null;
            }
            Object[] params = new Object[] { stclContext, container, getName(), new Character(getArity()), new Boolean(isTransient()) };
            _Slot<C, S> slot = ClassHelper.newInstance(cl, params);
            if (slot != null) {
                slot.setDescriptor(this);
                slot.setCompletionLevel(completionLevel);
            }
            return slot;
        } catch (Exception e) {
            logWarn(stclContext, "Cannot create slot instance %s for slot %s in %s (%s)", getFactory(), getName(), container, e);
            return null;
        }
    }

    private _Slot<C, S> createSingle(C stclContext, int completionLevel, S container) {
        if (getLog().isTraceEnabled()) {
            String msg = String.format("Creating single slot %s in %s", this, container);
            getLog().trace(stclContext, msg);
        }

        if (getLog().isWarnEnabled()) {
            verifySingle(stclContext, container);
        }

        _Stencil<C, S> stcl = container.getReleasedStencil(stclContext);
        SingleSlot<C, S> single = new SingleSlot<C, S>(stclContext, stcl, getName(), getArity(), isTransient());
        single.setDescriptor(this);
        single.setCompletionLevel(completionLevel);
        return single;
    }

    private _Slot<C, S> createMulti(C stclContext, int completionLevel, S container) {
        if (getLog().isTraceEnabled()) {
            String msg = String.format("Creating multi slot %s in %s", this, container);
            getLog().trace(stclContext, msg);
        }

        if (getLog().isWarnEnabled()) {
            verifyMulti(stclContext, container);
        }

        // no slot factory defined
        MultiSlot<C, S> multi = null;
        _Stencil<C, S> stcl = container.getReleasedStencil(stclContext);
        multi = new MultiSlot<C, S>(stclContext, stcl, getName(), getArity(), isTransient());
        multi.setDescriptor(this);
        multi.setCompletionLevel(completionLevel);
        return multi;
    }

    @Override
    public String toString() {
        try {
            XmlStringWriter declPart = new XmlStringWriter(false, 0, _StencilContext.getCharacterEncoding());
            save(null, declPart, null);
            return ((StringWriter) declPart.getWriter()).getBuffer().toString();
        } catch (IOException e) {
            return logError(null, "error in descriptor toString");
        }
    }

}