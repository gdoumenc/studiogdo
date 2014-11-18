/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.interpreted;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import com.gdo.helper.ClassHelper;
import com.gdo.stencils.Keywords;
import com.gdo.stencils._Stencil;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.atom.Atom;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStencil;
import com.gdo.stencils.factory.IStencilFactory.Mode;
import com.gdo.stencils.factory.InterpretedStencilFactory;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.slot._Slot;
import com.gdo.stencils.util.PathUtils;
import com.gdo.util.XmlWriter;

/**
 * <p>
 * A template descriptor contains all informations to create an interpreted
 * stencil.
 * <p>

 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>

 */
public class TemplateDescriptor<C extends _StencilContext, S extends _PStencil<C, S>> extends _Descriptor<C, S> {
    private String _name; // complete _templateDesc _name defined by the java
    // class
    private String _java; // java class associated to the stencil
    private String _extends; // stencil descriptor from where the stencil
    // inherits
    private String _facets; // facet file name where facets are defined
    private Map<String, SlotDescriptor<C, S>> _slots; // list of slot
    // descriptors
    private Map<String, CommandDescriptor<C, S>> _commands; // list of command
    // descriptors
    private List<PlugDescriptor<C, S>> _plugs; // list of plug descriptors when
    // slot are not defined
    // (inheritance)
    private List<UnplugDescriptor<C, S>> _unplugs; // list of unplug descriptors
    // when slot are not defined
    // (inheritance)
    private Map<String, InstDescriptor<C, S>> _insts; // map of instance
    // descriptors
    // referenced by ids
    private Map<String, PropDescriptor<C, S>> _props; // list of property
    // descriptors
    private List<Parameter> _parameters = new ArrayList<Parameter>(); // parameters

    // list
    // (if
    // exists)

    /**
     * Get the template name.
     * 
     * @return the template's name.
     */
    public String getName() {
        return _name;
    }

    /**
     * Set the template name.
     * 
     * @param name
     *            the template's name.
     */
    public final void setName(String name) {

        // cannot be empty
        if (StringUtils.isBlank(name)) {
            if (getLog().isWarnEnabled()) {
                getLog().warn(null, "Template name cannot be null");
            }
            return;
        }

        _name = name;
        verifyNameNotEqualsExtends();
    }

    public String getTemplateName() {
        return getName();
    }

    public List<String> getSuperNames(C stclContext) {
        TemplateDescriptor<C, S> superDesc = getSuperTemplateDescriptor(stclContext);
        List<String> list;
        if (superDesc != null) {
            list = superDesc.getSuperNames(stclContext);
        } else {
            list = new ArrayList<String>();
        }
        list.add(getTemplateName());
        return list;
    }

    // used when the template is read from digester
    public String getJava() {
        return _java;
    }

    public final void setJava(String java) {
        _java = java;
    }

    /**
     * @return the "extends" string if defined.
     */
    public String getExtends() {
        return _extends;
    }

    public void setExtends(String extend) {
        _extends = extend;
        verifyNameNotEqualsExtends();
    }

    public String getFacets(C stclContext) {
        return _facets;
    }

    public void setFacets(String facets) {
        _facets = facets;
    }

    /**
     * @return the "extends" string if defined or the super java class name.
     */
    public String getExtends(C stclContext) {

        // defined in the descriptor
        if (_extends != null) {
            return _extends;
        }

        // follow java class hierarchy
        try {
            InterpretedStencilFactory<C, S> factory = (InterpretedStencilFactory<C, S>) stclContext.<C, S> getStencilFactory();
            if (_java == null) {
                return null;
            }

            // TODO : perhaps this could be changed by a while do (instead of a
            // do while)
            if (!_java.equals(_name) && factory.getTemplateDescriptor(stclContext, _java) != null) {
                _extends = _java;
                return _extends;
            }

            // search from java classes hierachy the first template defined
            Class<?> clazz = TemplateDescriptor.class.getClassLoader().loadClass(_java);
            do {
                Class<?> superClass = clazz.getSuperclass();
                if (superClass == null || superClass.equals(Atom.class)) {
                    return null;
                }
                _extends = superClass.getName();
                clazz = superClass;
            } while (factory.getTemplateDescriptor(stclContext, _extends) == null);
            return _extends;
        } catch (Exception e) {
            logWarn(stclContext, "Cannot get extending descriptor for %s", getTemplateName());
        }
        return null;
    }

    /**
     * Parameters are searched in hierarchy as extends inherits such info
     */
    @Override
    public Object getParameter(C stclContext, int index) {

        // searches in memorized parameters
        for (Parameter p : _parameters) {
            if (p.index == index)
                return p.value;
        }

        // returns if found here
        Object value = super.getParameter(stclContext, index);

        // searches in hierarchy if not found
        if (value == null) {
            TemplateDescriptor<C, S> parent = getSuperDescriptor(stclContext);
            if (parent != null && parent != this) {
                value = parent.getParameter(stclContext, index);
            }
        }

        // stores in memorized parameters
        Parameter param = new Parameter(index, value);
        _parameters.add(param);

        // returns the parameter found
        return value;
    }

    // used when factory creates the stencil
    public Object[] getParameters(C stclContext) {
        int size = CommandContext.PARAMS.length;
        Object[] params = new Object[size];
        for (int i = 0; i < size; i++) {
            params[i] = getParameter(stclContext, i);
        }
        return params;
    }

    public final Class<? extends _Stencil<C, S>> getStencilClass(C stclContext) {
        try {
            InterpretedStencilFactory<C, S> factory = (InterpretedStencilFactory<C, S>) stclContext.<C, S> getStencilFactory();

            // java is defined
            String java = getJava();
            if (StringUtils.isNotBlank(java)) {
                Class<_Stencil<C, S>> c = ClassHelper.loadClass(java);
                if (c == null) {
                    logError(stclContext, "Cannot load java class %s", java);
                }
                return c;
            }

            // extends is defined
            String ext = getExtends(stclContext);
            if (StringUtils.isNotBlank(ext)) {

                // in case of multi extension, take first template name
                // TODO should check all super has same java class!!!
                if (ext.indexOf(PathUtils.MULTI) != -1) {
                    ext = ext.substring(0, ext.indexOf(":"));
                }

                // gets class name from template descriptor
                TemplateDescriptor<C, S> extendsDesc = factory.getTemplateDescriptor(stclContext, ext);
                if (extendsDesc != null) {
                    return extendsDesc.getStencilClass(stclContext);
                }

                // tries with the template name as java class
                Class<_Stencil<C, S>> c = ClassHelper.loadClass(ext);
                if (c == null) {
                    logError(stclContext, "No inherited descriptor %s defined for %s", ext, getTemplateName());
                }
                return c;
            }
        } catch (Exception e) {
            logError(stclContext, "Cannot get java class for %s : %s", getTemplateName(), e);
        }
        return null;
    }

    public SlotDescriptor<C, S> getLocalSlotDescriptor(String name) {
        return (_slots != null) ? _slots.get(name) : null;
    }

    public Collection<SlotDescriptor<C, S>> getLocalSlotDescriptors() {
        if (_slots != null) {
            return _slots.values();
        }
        return Collections.emptyList();
    }

    // TODO should be renamed getSlotDescriptor (with boolean for local search)
    // and remove getLocal descritor;
    public SlotDescriptor<C, S> getInheritedSlotDescriptor(C stclContext, String name) {

        if (_slots != null) {
            SlotDescriptor<C, S> slotDesc = _slots.get(name);
            if (slotDesc != null) {
                return slotDesc;
            }
        }

        TemplateDescriptor<C, S> superDesc = getSuperDescriptor(stclContext);
        if (superDesc != null) {
            return superDesc.getInheritedSlotDescriptor(stclContext, name);
        }

        return null;
    }

    public void addSlotDescriptor(SlotDescriptor<C, S> slotDesc) {
        if (_slots == null) {
            _slots = new ConcurrentHashMap<String, SlotDescriptor<C, S>>();
        }
        _slots.put(slotDesc.getName(), slotDesc);
    }

    public final Collection<CommandDescriptor<C, S>> getCommandDescriptors() {
        if (_commands != null) {
            return _commands.values();
        }
        return Collections.emptyList();
    }

    // TODO : same as slot descriptor (add boolean parameter for local search)
    public final CommandDescriptor<C, S> getCommandDescriptor(C stclContext, String name) {

        // find in local command
        if (_commands != null) {
            CommandDescriptor<C, S> cmdDesc = _commands.get(name);
            if (cmdDesc != null)
                return cmdDesc;
        }

        // search in template hierarchy
        TemplateDescriptor<C, S> superDesc = getSuperDescriptor(stclContext);
        return (superDesc != null) ? superDesc.getCommandDescriptor(stclContext, name) : null;
    }

    public void addCommandDescriptor(CommandDescriptor<C, S> command) {
        if (_commands == null) {
            _commands = new HashMap<String, CommandDescriptor<C, S>>();
        }
        _commands.put(command.getName(), command);
    }

    protected Collection<InstDescriptor<C, S>> getInstDescriptors() {
        if (_insts != null) {
            return _insts.values();
        }
        return Collections.emptyList();
    }

    public void addInstDescriptor(InstDescriptor<C, S> inst) {
        if (_insts == null) {
            _insts = new ConcurrentHashMap<String, InstDescriptor<C, S>>();
        }
        _insts.put(inst.getId(), inst);
    }

    public Collection<PlugDescriptor<C, S>> getPlugDescriptors() {
        if (_plugs != null) {
            return _plugs;
        }
        return Collections.emptyList();
    }

    public void addPlugDescriptor(PlugDescriptor<C, S> plug) {
        if (_plugs == null) {
            _plugs = new ArrayList<PlugDescriptor<C, S>>();
        }
        _plugs.add(plug);
    }

    public Collection<UnplugDescriptor<C, S>> getUnplugDescriptors() {
        if (_unplugs != null) {
            return _unplugs;
        }
        return Collections.emptyList();
    }

    public final void addUnplugDescriptor(UnplugDescriptor<C, S> plug) {
        if (_unplugs == null) {
            _unplugs = new ArrayList<UnplugDescriptor<C, S>>();
        }
        _unplugs.add(plug);
    }

    public final void forcePlugsInCreationMode() {
        for (PlugDescriptor<C, S> plug : getPlugDescriptors()) {
            if (!plug.getOnAsMode().equals(Mode.ON_ALWAYS)) {
                plug.setOn(Keywords.CREATE);
            }
        }
        for (UnplugDescriptor<C, S> unplug : getUnplugDescriptors()) {
            if (!unplug.getOnAsMode().equals(Mode.ON_ALWAYS)) {
                unplug.setOn(Keywords.CREATE);
            }
        }
    }

    private Collection<PropDescriptor<C, S>> getPropDescriptors() {
        if (_props != null) {
            return _props.values();
        }
        return Collections.emptyList();
    }

    public final void addPropDescriptor(PropDescriptor<C, S> prop) {
        if (_props == null) {
            _props = new ConcurrentHashMap<String, PropDescriptor<C, S>>();
        }
        _props.put(prop.getName(), prop);
    }

    @SuppressWarnings("unchecked")
    public void completeStencil(C stclContext, S stencil, InstanceRepository<C, S> instances, int completionLevel) {

        // expand properties as slot with default value (if not already done)
        for (PropDescriptor<C, S> propDesc : getPropDescriptors()) {
            String slotName = propDesc.getName();
            SlotDescriptor<C, S> slotDesc = getLocalSlotDescriptor(slotName);

            // verifies the slot is not already defined locally before
            if (slotDesc != null && completionLevel > 0) {
                if (completionLevel > 0 && slotDesc.expandedFromProp() == null && getLog().isWarnEnabled()) {
                    String msg = String.format("Property defined in template %s with a slot %s already defined", getTemplateName(), slotName);
                    getLog().warn(stclContext, msg);
                }
            } else {

                _Slot<C, S> ss = stencil.getReleasedStencil(stclContext).getSlots().get(slotName);
                if (ss != null && !ss._fromXML) {
                    stencil.newPProperty(stclContext, slotName, Key.NO_KEY, propDesc.getValue(stclContext, this));
                }

                // create the slot with default value (TODO should be done once
                // for the template)
                slotDesc = propDesc.expandToSlotDescriptor(stclContext, this);
                if (completionLevel == 0)
                    addSlotDescriptor(slotDesc); // the slot will be created
                // after
                else {
                    slotDesc.createSlot(stclContext, stencil, completionLevel);
                }
            }
        }
        // TODO we should be able to remove all prop if expanded...

        // adds local instances
        for (InstDescriptor<C, S> instDesc : getInstDescriptors()) {
            instances.store(instDesc);
        }

        // adds local slots
        for (SlotDescriptor<C, S> slotDesc : getLocalSlotDescriptors()) {
            slotDesc.createSlot(stclContext, stencil, completionLevel);
        }

        // add local commands
        for (CommandDescriptor<C, S> cmdDesc : getCommandDescriptors()) {
            if (!stencil.hasCommand(stclContext, cmdDesc.getName())) {
                Class<? extends CommandStencil<C, S>> clazz = (Class<? extends CommandStencil<C, S>>) cmdDesc.getStencilClass(stclContext);
                _Stencil<C, S> stcl = stencil.getReleasedStencil(stclContext);
                stcl.command(cmdDesc.getName(), clazz, cmdDesc.getParameters(stclContext));
            }
        }

        // super hierarchy completion
        String superTempName = getExtends(stclContext);
        if (!StringUtils.isEmpty(superTempName)) {

            // iterates over multi ext
            for (String superName : StringUtils.split(superTempName, PathUtils.MULTI)) {
                if (superName.equals(getName())) {
                    logWarn(stclContext, "Circular template definition %s", superName);
                    continue;
                }

                // completes from hierarchy template descriptors
                try {
                    Class<?> clazz = TemplateDescriptor.class.getClassLoader().loadClass(superName);
                    if (clazz.equals(_Stencil.class)) {
                        continue;
                    }

                    TemplateDescriptor<C, S> tempDesc = getTemplateDescriptor(stclContext, superName);
                    while (tempDesc == null && !clazz.equals(_Stencil.class)) {
                        clazz = clazz.getSuperclass();
                        tempDesc = getTemplateDescriptor(stclContext, clazz.getName());
                    }
                    if (clazz.equals(_Stencil.class)) {
                        continue;
                    }
                    int compLevel = completionLevel + 1;
                    // if (this instanceof InstDescriptor) {
                    // compLevel = completionLevel;
                    // }
                    tempDesc.completeStencil(stclContext, stencil, instances, compLevel);
                } catch (Exception e) {
                    continue;
                }
            }
        }

        // unplugs (before plug..)
        for (UnplugDescriptor<C, S> unplugDesc : getUnplugDescriptors()) {
            unplugDesc.setOnSlot(stclContext, stencil, instances, completionLevel);
        }

        // add plugs (only after slot has been created)
        for (PlugDescriptor<C, S> plugDesc : getPlugDescriptors()) {
            plugDesc.setOnSlot(stclContext, stencil, instances, completionLevel);
        }
    }

    @Override
    public TemplateDescriptor<C, S> getSuperDescriptor(C stclContext) {

        // stencil context may be null when called from toString() method
        if (stclContext == null)
            return null;

        // search super descriptor from template hierarchy
        TemplateDescriptor<C, S> superDesc = (TemplateDescriptor<C, S>) super.getSuperDescriptor(stclContext);
        if (superDesc != null)
            return superDesc;
        String ext = getExtends(stclContext);
        if (!StringUtils.isEmpty(ext)) {
            InterpretedStencilFactory<C, S> factory = (InterpretedStencilFactory<C, S>) stclContext.<C, S> getStencilFactory();
            return factory.getTemplateDescriptor(stclContext, ext);
        }
        return null;
    }

    public TemplateDescriptor<C, S> getSuperTemplateDescriptor(C stclContext) {
        return getSuperDescriptor(stclContext);
    }

    /**
     * Returns the template descriptor from XML file or java inheritance.
     * 
     * @param stclContext
     *            the stencil context.
     * @param name
     *            the template name.
     * @return the template descriptor, <tt>null</tt> if not found.
     */
    private TemplateDescriptor<C, S> getTemplateDescriptor(C stclContext, String name) {
        InterpretedStencilFactory<C, S> factory = (InterpretedStencilFactory<C, S>) stclContext.<C, S> getStencilFactory();

        // gets the descriptor from factory (XML files)
        TemplateDescriptor<C, S> tempDesc = factory.getTemplateDescriptor(stclContext, name);
        if (tempDesc != null) {
            return tempDesc;
        }

        // perhaps the super template is just defined by a java class
        Class<?> clazz = ClassHelper.loadClass(name);
        if (clazz == null) {
            logWarn(stclContext, "Missing template description %s for %s", name, getTemplateName());
            return null;
        }
        do {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass == null || superClass.equals(Atom.class)) {
                break;
            }
            clazz = superClass;
            tempDesc = factory.getTemplateDescriptor(stclContext, clazz.getName());
        } while (tempDesc == null);
        return tempDesc;
    }

    @Override
    public void save(C stclContext, XmlWriter declPart, XmlWriter plugPart) throws IOException {
        declPart.startElement("template");
        declPart.writeAttribute("name", getName());
        declPart.writeAttribute("extends", getExtends());
        declPart.writeAttribute("java", getJava());

        saveDescription(stclContext, declPart, declPart);

        declPart.endElement("template");
    }

    protected void saveParameters(C stclContext, XmlWriter instPart) throws IOException {
        for (ParameterDescriptor<C, S> paramDesc : getParameterDescriptors()) {
            paramDesc.save(stclContext, instPart, null);
        }
    }

    protected void saveDescription(C stclContext, XmlWriter declPart, XmlWriter plugPart) throws IOException {
        for (PropDescriptor<C, S> propDesc : getPropDescriptors()) {
            propDesc.save(stclContext, declPart, null);
        }
        for (InstDescriptor<C, S> instDesc : getInstDescriptors()) {
            instDesc.save(stclContext, declPart, null);
        }
        for (SlotDescriptor<C, S> slotDesc : getLocalSlotDescriptors()) {
            slotDesc.save(stclContext, declPart, null);
        }
        for (PlugDescriptor<C, S> plugDesc : getPlugDescriptors()) {
            String slotName = plugDesc.getSlot();

            // find the slot (may not exist in case of prop slot) and save if
            // not transient
            SlotDescriptor<C, S> slotDesc = getInheritedSlotDescriptor(stclContext, slotName);
            if (slotDesc == null || !slotDesc.isTransient()) {
                plugDesc.save(stclContext, null, plugPart);
            }
        }
        for (CommandDescriptor<C, S> cmdDesc : getCommandDescriptors()) {
            cmdDesc.save(stclContext, declPart, plugPart);
        }
    }

    /**
     * Verifies that extension has not same value (occured when java replaced by
     * extends and then no sense...)
     */
    private void verifyNameNotEqualsExtends() {

        if (StringUtils.isEmpty(_name))
            return;
        if (_name.equals(_extends)) {
            if (getLog().isWarnEnabled()) {
                String msg = String.format("Extends is same as template name (should be perhaps java name but never extends) : %s", _name);
                getLog().warn(null, msg);
            }
            setJava(_name);
            setExtends(null);
        }
    }

    private class Parameter {
        int index;
        Object value;

        Parameter(int i, Object v) {
            this.index = i;
            this.value = v;
        }
    }

}