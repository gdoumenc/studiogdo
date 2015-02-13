/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.gdo.helper.StringHelper;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.cmd.CommandStencil;
import com.gdo.stencils.cond.AndCondition;
import com.gdo.stencils.cond.PathCondition;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.descriptor.DelegateSlotDescriptor;
import com.gdo.stencils.descriptor.EmptySlotDescriptor;
import com.gdo.stencils.descriptor.Links;
import com.gdo.stencils.descriptor.MultiSlotDescriptor;
import com.gdo.stencils.descriptor.PropSlotDescriptor;
import com.gdo.stencils.descriptor.SingleSlotDescriptor;
import com.gdo.stencils.descriptor._SlotDescriptor;
import com.gdo.stencils.event.IPropertyChangeListener;
import com.gdo.stencils.event.PropertyChangeEvent;
import com.gdo.stencils.faces.RenderContext;
import com.gdo.stencils.facet.FacetResult;
import com.gdo.stencils.factory.InterpretedStencilFactory;
import com.gdo.stencils.factory.StencilFactory;
import com.gdo.stencils.interpreted.TemplateDescriptor;
import com.gdo.stencils.iterator.ListIterator;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.log.StencilLog;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.plug.WrongPathException;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.slot.CalculatedStringPropertySlot;
import com.gdo.stencils.slot.MultiCalculatedSlot;
import com.gdo.stencils.slot.MultiSlot;
import com.gdo.stencils.slot.SingleCalculatedSlot;
import com.gdo.stencils.slot._Slot;
import com.gdo.stencils.util.GlobalCounter;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.SlotUtils;
import com.gdo.stencils.util.StencilUtils;
import com.gdo.util.XmlStringWriter;
import com.gdo.util.XmlWriter;

/**
 * <p>
 * Basic stencil class (the M of the MVC model).
 * </p>
 * <p>
 * Should be used only in a plugged form {@link _PStencil} or directly for java
 * interface.
 * </p>
 */
public abstract class _Stencil<C extends _StencilContext, S extends _PStencil<C, S>> {

    // defines to true if performs lot of checking (decrease performance..)
    public static final boolean STRICT_MODE = false;

    // the project root slot
    private static PSlot<?, ?> ROOT_SLOT;

    /**
     * Interface to define slot names as constants.
     */
    public interface Slot {
        String THIS = PathUtils.THIS;
        String PARENT = PathUtils.PARENT;
        String ROOT = PathUtils.ROOT;

        String NAME = "Name";

        String DESCRIPTION = "$Description";

        String LISTENERS = "Listeners";
    }

    /**
     * Interface to define command names as constants.
     */
    public interface Command {
        // no command as defined in Stcl
    }

    // java attributes

    /**
     * Set to <tt>true</tt> when this stencil was cleared (so should not be
     * referenced anymore). Defined to warn when cleared stencil are used.
     */
    private boolean cleared = false;

    // associated default plugged stencil for interface manipulation
    private String _id = GlobalCounter.ID();
    private String _uid = GlobalCounter.uniqueID();
    protected _PStencil<C, S> _self;

    // template descriptor used to create the stencil (may be null)
    protected TemplateDescriptor<C, S> _desc;
    protected Map<String, _SlotDescriptor<C, S>> _slot_descs;
    protected Map<String, CommandDescriptor> _command_descs;

    // instance name and template name
    protected String _name;
    protected String _template_name;

    // description usefull for maintenance
    protected String _comment = "";

    // this stencil should be stored or not;
    protected boolean _transient = false;

    //
    // slots and commands structures
    //

    /**
     * Map of slots defined in the stencil (slots' name as key)
     */
    protected Map<String, _Slot<C, S>> _slots = new HashMap<String, _Slot<C, S>>();

    /**
     * Internal slot when commands are defined.
     */
    private MultiSlot<C, S> _commandSlot;

    // writer used for saving this stencil
    // used to know if the stencil has been already stored in the current writer
    // when saving (each saving is done in a specific writer)
    protected XmlWriter _writer;

    // property part
    public String _value;
    protected String _type = Keywords.STRING;
    protected boolean _expand;

    /**
     * Should be used only internally by digester.
     */
    public _Stencil(C stclContext) {

        // internal slot (used temporary to retrieve command stencil)
        _commandSlot = new MultiSlot<C, S>(stclContext, this, ".Commands", PSlot.ANY, true);

        // predefined slots
        addDescriptor(Slot.THIS, new _SlotDescriptor<C, S>() {
            @Override
            public _Slot<C, S> add(C stclContext, String name, S self) {
                return new ThisSlot(stclContext, _Stencil.this, name);
            }
        });
        createParentSlot(stclContext);
        createRootSlot(stclContext);

        createNameSlot(stclContext);
        createDescriptionSlot(stclContext);
    }

    public _Stencil(C stclContext, String value) {
        this(stclContext);

        _value = value;

        multiSlot(Slot.LISTENERS, PSlot.ANY, true, null);
    }

    @Override
    public void finalize() {

    }

    protected _Slot<C, S> createParentSlot(C stclContext) {
        return new ParentSlot(stclContext, _Stencil.this, PathUtils.PARENT);
    }

    protected _Slot<C, S> createRootSlot(C stclContext) {
        return new RootSlot(stclContext, _Stencil.this, PathUtils.ROOT);
    }

    protected _Slot<C, S> createNameSlot(C stclContext) {
        return new NameSlot(stclContext, this, Slot.NAME);
    }

    protected _Slot<C, S> createDescriptionSlot(C stclContext) {
        return new DescriptionSlot(stclContext, this, Slot.DESCRIPTION);
    }

    /**
     * Redefine to perform specific code before cleaning..
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            this stencils as a plugged stencil.
     * 
     *            TODO should replace beforeLastUnplug.
     */
    public void beforeClear(C stclContext, S self) {
    }

    /**
     * Clears all internal structures to free memory.
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            this stencils as a plugged stencil.
     */
    public void clear(C stclContext, S self) {

        beforeClear(stclContext, self);

        // checks not already cleared
        if (this.cleared) {
            logError(stclContext, "A stencil should not be cleared twice");
        }
        this.cleared = true;
        _self = null;
        _desc = null;

        // slots and commands
        if (_slots != null) {
            for (_Slot<C, S> slot : _slots.values()) {
                slot.clear();
            }
            _slots.clear();
            _slots = null;
        }
        if (_commandSlot != null) {
            _commandSlot.clear();
            _commandSlot = null;
        }

        // descriptors
        if (_slot_descs != null) {
            _slot_descs.clear();
            _slot_descs = null;
        }
        if (_command_descs != null) {
            _command_descs.clear();
            _command_descs = null;
        }
    }

    /**
     * Checks if a stencil is available : when a stencil is cleared, it should
     * be not be used any more.
     * 
     * @return <tt>true</tt> if the stencil was cleared.
     */
    public boolean isCleared() {
        return this.cleared;
    }

    //
    // Java attributes
    //

    /**
     * Gets the descriptor which has used to create the stencil.
     * 
     * @return the descriptor which has used to create the stencil.
     */
    public final TemplateDescriptor<C, S> getDescriptor() {
        return _desc;
    }

    /**
     * Sets the descriptor which has used to create the stencil. TODO: In
     * deployed mode, the descriptor should not be defined for memory
     * optimization.
     * 
     * @param desc
     *            the template descriptor.
     */
    public final void setDescriptor(TemplateDescriptor<C, S> desc) {
        _desc = desc;
        _template_name = desc.getTemplateName();
    }

    /**
     * Gets the transient status of the stencil.
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            the stencil as a plugged stencil.
     * @return <tt>true</tt> if the stencil is transient (should not be stored).
     */
    public boolean isLink(C stclContext, S self) {
        return false;
    }

    public boolean isTransient(C stclContext, S self) {
        return _transient;
    }

    /**
     * Sets the stencil transient.
     */
    public void setTransient() {
        _transient = true;
    }

    public String getId(C stclContext) {
        return _id;
    }

    public String getUId(C stclContext) {
        return _uid;
    }

    /**
     * Gets the java name value (not default one is not defined).
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            the stencil as a plugged stencil.
     * @return the stencil property name (even if empty).
     */
    public String getJavaName(C stclContext, S self) {
        return _name;
    }

    /**
     * Gets the name value (never empty, using template name if empty).
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            the stencil as a plugged stencil.
     * @return the stencil name (never empty, using template name if empty).
     */
    public String getName(C stclContext, S self) {
        String name = getJavaName(stclContext, self);
        return (StringUtils.isNotBlank(name)) ? name : getTemplateName();
    }

    /**
     * Sets the stencil name value.
     * 
     * @param stclContext
     *            the stencil context.
     * @param name
     *            the new name.
     * @param self
     *            the stencil as a plugged stencil.
     * @return the stencil name set in stencil.
     */
    public String setName(C stclContext, String name, S self) {
        _name = name;
        return name;
    }

    /**
     * Gets the comment value.
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            the stencil as a plugged stencil.
     * @return the comment (never empty).
     */
    public String getComment(C stclContext, S self) {
        return _comment;
    }

    /**
     * Sets the comment.
     * 
     * @param stclContext
     *            the stencil context.
     * @param comment
     *            the new comment.
     * @param self
     *            the stencil as a plugged stencil.
     */
    public void setComment(C stclContext, String comment, S self) {
        _comment = comment;
    }

    /**
     * Gets the template name.
     * 
     * @return stencil template name (stencil class name if no descriptor).
     */
    public String getTemplateName() {
        if (StringUtils.isNotBlank(_template_name)) {
            return _template_name;
        }
        return getClass().getName();
    }

    /**
     * Creates a new plugged stencil from a template class name and parameters
     * and performing the real plug operation.
     * 
     * @param stclContext
     *            the stencil context.
     * @param slot
     *            the slot where the created stencil is plugged.
     * @param key
     *            the key for plugging.
     * @param clazz
     *            the stencil class name.
     * @param self
     *            the stencil as a plugged stencil.
     * @param params
     *            the stencil constructor parameters.
     * @return a new plugged stencil.
     */
    @SuppressWarnings("unchecked")
    public S newPStencil(C stclContext, PSlot<C, S> slot, IKey key, Class<? extends _Stencil<?, ?>> clazz, S self, Object... params) {

        // creates the stencil (without plugging it)
        StencilFactory<C, S> factory = (StencilFactory<C, S>) stclContext.<C, S> getStencilFactory();
        S stcl = factory.createPStencil(stclContext, slot, key, (Class<? extends _Stencil<C, S>>) clazz, params);
        if (StencilUtils.isNull(stcl)) {
            String reason = stcl.getNullReason();
            logWarn(stclContext, "Cannot create stencil %s (%s)", clazz, reason);
            return StencilUtils.<C, S> nullPStencil(stclContext, Result.error(reason));
        }

        // plugs it in the slot (if defined)
        if (SlotUtils.isNull(slot)) {
            return stcl;
        }
        return self.plug(stclContext, stcl, slot, key);
    }

    /**
     * Creates a new plugged property from an initial value and possibly other
     * parameters.
     * 
     * @param stclContext
     *            the stencil context.
     * @param slot
     *            the slot where the created stencil is plugged.
     * @param key
     *            the key for plugging.
     * @param value
     *            the initial property value.
     * @param self
     *            the stencil as a plugged stencil.
     * @param params
     *            the stencil constructor parameters.
     * @return a new plugged property.
     */
    public S newPProperty(C stclContext, PSlot<C, S> slot, IKey key, Object value, S self, Object... params) {

        // creates the property (without plugging it)
        StencilFactory<C, S> factory = (StencilFactory<C, S>) stclContext.<C, S> getStencilFactory();
        S prop = factory.createPProperty(stclContext, slot, Key.NO_KEY, value, params);
        if (StencilUtils.isNull((S) prop)) {
            logWarn(stclContext, "cannot create property stencil for value %s", value);
            return null;
        }

        // plugs it in the slot (if defined)
        if (SlotUtils.isNull(slot)) {
            return prop;
        }
        return self.plug(stclContext, prop, slot, key);
    }

    /**
     * Completes stencil with declared slots and plugs.
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            this stencils as a plugged stencil.
     */
    public void complete(C stclContext, S self) {

        // completes slots
        if (_slot_descs != null) {
            for (Map.Entry<String, _SlotDescriptor<C, S>> desc : _slot_descs.entrySet()) {
                String slotName = desc.getKey();

                // adds slot from descriptor only if not already defined
                // if (!getSlots().containsKey(slotName)) {
                _Slot<C, S> slot = desc.getValue().add(stclContext, slotName, self);

                // adds linked slots
                if (desc.getValue()._links != null) {
                    slot.setLinks(desc.getValue()._links);
                }
                // }
            }
            _slot_descs.clear();
        }
        _slot_descs = null;
    }

    // --------------------------------------------------------------------------
    //
    // Creation management.
    //
    // --------------------------------------------------------------------------

    /**
     * Overridable method for actions to be performed on stencil creation.
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            the stencil as a plugged stencil.
     */
    public void beforeCompleted(C stclContext, S self) {
        // nothing by default
    }

    /**
     * Overridable method for actions to be performed on stencil creation.
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            the stencil as a plugged stencil.
     */
    public void afterCompleted(C stclContext, S self) {
        // nothing by default
    }

    /**
     * Overridable method for actions to be performed on stencil first plug.
     * 
     * @param stclContext
     *            the stencil context.
     * @param slot
     *            the containing slot.
     * @param key
     *            the key.
     * @param self
     *            the stencil as a plugged stencil.
     */
    public void afterPlugged(C stclContext, PSlot<C, S> slot, IKey key, S self) {
        // nothing by default
    }

    // --------------------------------------------------------------------------
    //
    // Modification management.
    //
    // --------------------------------------------------------------------------

    /**
     * Verification function to be done after RPC modification.
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            the stencil as a plugged stencil.
     * @return the verification performed result.
     */
    public Result verify(C stclContext, S self) {
        return Result.success();
    }

    /**
     * Perform actions which must be done after a set/mset RPC call.
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            the stencil as a plugged stencil.
     * @return the action performed result.
     */
    public Result afterRPCSet(C stclContext, S self) {
        return verify(stclContext, self);
    }

    @Deprecated
    public void afterUnplug(C stclContext, PSlot<C, S> from, IKey key, S self) {
    }

    @Deprecated
    public void afterLastUnplug(C stclContext, S last) {
        // TODO remove all stencils from slots
        /*
         * for (_Slot<C, ? extends I> slot : getSlots().values()) { for (PStencil<C,
         * ? extends I> s : slot.getStencils(stclContext, null, slotContainer)) {
         * unplug(stclContext, slot.getName(stclContext), s.getStencil(), self()); }
         * }
         */
    }

    /**
     * Creates a copy of this stencil. The precise meaning of "copy" depends of
     * the template.
     * 
     * @param stclContext
     *            the stencil context.
     * @param slot
     *            the containing slot.
     * @param key
     *            the plug key for the clone.
     * @param self
     *            this stencil as a plugged stencil
     * @throws CloneNotSupportedException
     * @return the stencil cloned
     */
    public S clone(C stclContext, PSlot<C, S> slot, IKey key, S self) throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * @param stclContext
     *            the stencil context.
     * @param self
     *            this stencil as a plugged stencil
     * @return <tt>true</tt> if the stencil is complete, then it can be
     *         manipulated without structural error. Should be redefined for all
     *         sub stencil class.
     */
    protected boolean isComplete(C stclContext, S self) {
        return true;
    }

    //
    // Slots ans commands structure
    //

    /**
     * Gets the map of defined slots. This method should be used only for
     * internal implementation.
     * 
     * @return The slot map (may be empty but never <tt>null</tt>).
     */
    public final Map<String, _Slot<C, S>> getSlots() {
        return _slots;
    }

    /**
     * Adds a local slot. This method should be used only for internal
     * implementation.
     * 
     * @param stclContext
     *            the stencil context.
     * @param slot
     *            the slot added.
     */
    public void addSlot(C stclContext, _Slot<C, S> slot) {

        // checks the slot not already defined
        String slotName = slot.getName();
        if (getSlots().get(slotName) != null) {
            logWarn(stclContext, "adding slot %s which is already defined in %s", slotName, this);
        }

        // adds this slot to the slots list
        getSlots().put(slotName, slot);
    }

    /**
     * Removes a local slot. This method should be used only for internal
     * implementation.
     * 
     * @param stclContext
     *            the stencil context.
     * @param name
     *            the slot's name.
     * @return the removed slot.
     */
    public _Slot<C, S> removeSlot(C stclContext, String name) {
        return getSlots().remove(name);
    }

    //
    // Informations relative to contained slots
    //

    /**
     * Checks if a slot is defined locally.
     * 
     * @param stclContext
     *            the stencil context.
     * @param slotName
     *            the slot's name.
     * @return <tt>true</tt> if the slot defined by the local name exists.
     */
    @Deprecated
    protected boolean hasLocalSlot(C stclContext, String slotName) {
        String name = slotName;

        // verifies path is correct
        if (StringUtils.isEmpty(name) || PathUtils.isComposed(name))
            return false;

        // remove last '/' if exists
        if (name.length() > 1 && name.endsWith(PathUtils.SEP_STR)) {
            name = StringHelper.substringEnd(name, 1);
        }

        // checks in slots map
        return getSlots().containsKey(name);
    }

    /**
     * Retrieves a local slot.
     * 
     * @param stclContext
     *            the stencil context.
     * @param slotName
     *            the slot's name (should be not composed).
     * @param self
     *            the stencil as a plugged stencil.
     * @return the slot (if exists locally).
     */
    @SuppressWarnings("unchecked")
    protected PSlot<C, S> getLocalSlot(C stclContext, String slotName, S self) {
        try {
            String name = slotName;

            // verify path
            if (StringUtils.isBlank(name)) {
                String msg = logWarn(stclContext, "Blank path for function getLocalSlot");
                return new PSlot<C, S>(Result.error(msg));
            }
            if (PathUtils.isComposed(name)) {
                String msg = logWarn(stclContext, "Wrong composed slot path %s for local slot", name);
                return new PSlot<C, S>(Result.error(msg));
            }

            // removes last '/' if exists
            if (name.length() > 1 && name.endsWith(PathUtils.SEP_STR))
                name = StringHelper.substringEnd(name, 1);

            // gets the slot
            _Slot<C, S> slot = getSlots().get(name);

            // verifies the slot is correct
            if (slot == null) {
                String msg = logWarn(stclContext, "%s slot not defined in %s", name, self);
                return new PSlot<C, S>(Result.error(msg));
            }

            // returns plugged slot
            if (PathUtils.ROOT.equals(name)) {
                if (ROOT_SLOT == null)
                    ROOT_SLOT = new PSlot<C, S>(slot, null);
                return (PSlot<C, S>) ROOT_SLOT;
            }
            return new PSlot<C, S>(slot, self);
        } catch (WrongPathException e) {
            logError(stclContext, "slot %s not defined in %s : %s", slotName, self, e);
            return new PSlot<C, S>(Result.error(e.getMessage()));
        }
    }

    @Deprecated
    public boolean hasSlot(C stclContext, String slotPath, S self) {
        if (StringUtils.isEmpty(slotPath))
            return false;

        if (PathUtils.isComposed(slotPath)) {
            String first = PathUtils.getPathName(slotPath);
            S stcl = getStencil(stclContext, first, self);
            if (StencilUtils.isNull(stcl))
                return false;

            String tail = PathUtils.getLastName(slotPath);
            return stcl.hasSlot(stclContext, tail);
        }

        return hasLocalSlot(stclContext, slotPath);
    }

    /**
     * Retrieves a slot.
     * 
     * @param stclContext
     *            the stencil context.
     * @param slotPath
     *            the path to the slot.
     * @param self
     *            the stencil as a plugged stencil.
     * @return the slot found.
     */
    public PSlot<C, S> getSlot(C stclContext, String slotPath, S self) {

        // TODO replace by PathUtils.getSlotPath
        // removes path condition
        if (PathUtils.isKeyContained(slotPath)) {
            slotPath = PathUtils.getKeyContainer(slotPath);
        } else if (PathUtils.isExpContained(slotPath)) {
            slotPath = PathUtils.getExpContainer(slotPath);
        }

        // slot found
        return getLocalSlot(stclContext, slotPath, self);
    }

    /**
     * Returns the number of stencils in the path.
     * 
     * @param stclContext
     *            the stencil context.
     * @param path
     *            the seraching path.
     * @param self
     *            this stencil as a plugged stencil.
     * @return the number of stencils in path.
     * @throws WrongPathException
     */
    public int size(C stclContext, String path, S self) throws WrongPathException {
        return size(stclContext, path, null, self);
    }

    public int size(C stclContext, String path, StencilCondition<C, S> cond, S self) throws WrongPathException {

        // tests in slot if exists
        PSlot<C, S> slot = self.getSlot(stclContext, path);
        if (SlotUtils.isNull(slot)) {
            String msg = logWarn(stclContext, "Cannot get stencil at slot %s as this slot doesn't exists", path);
            throw new WrongPathException(msg);
        }

        // add path condition
        if (PathUtils.isKeyContained(path) || PathUtils.isExpContained(path)) {
            StencilCondition<C, S> c = new AndCondition<C, S>(new PathCondition<C, S>(stclContext, path, self), cond);
            return slot.size(stclContext, c);
        }

        return slot.size(stclContext, cond);
    }

    /**
     * Defines the default property when this stencil is considered as a
     * property. Default is <tt>Name</tt>
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            the stencil the stencil as a plugged stencil.
     * @return the slot path to the default property.
     */
    public String defaultProperty(C stclContext, S self) {
        return _Stencil.Slot.NAME;
    }

    /**
     * @return <tt>true</tt> if there is a stencil in path. May be redefined for
     *         efficiency reason.
     */
    public boolean hasStencils(C stclContext, String path, S self) throws WrongPathException {
        return hasStencils(stclContext, path, null, self);
    }

    public boolean hasStencils(C stclContext, String path, StencilCondition<C, S> cond, S self) throws WrongPathException {
        return size(stclContext, path, cond, self) > 0;
    }

    /**
     * @return the first stencil in path. May be redefined for efficiency
     *         reason.
     */
    public S getStencil(C stclContext, String path, S self) {
        return getStencil(stclContext, path, null, self);
    }

    public S getStencil(C stclContext, String path, StencilCondition<C, S> cond, S self) {

        // if composed path
        if (PathUtils.isComposed(path)) {

            // get first stencil
            String first = PathUtils.getPathName(path);
            StencilIterator<C, S> firsts = self.getStencils(stclContext, first);
            if (firsts.isNotValid() || firsts.size() == 0) {
                return self.nullPStencil(stclContext, firsts.getStatus());
            }

            // if no aggregation
            String tail = PathUtils.getLastName(path);
            if (firsts.size() == 1) {
                S stcl = firsts.next();
                return stcl.getStencil(stclContext, tail, cond);
            }

            // return first found
            for (S stcl : firsts) {
                S s = stcl.getStencil(stclContext, tail, cond);
                if (s.isNotNull())
                    return s;
            }

            // not found
            return self.nullPStencil(stclContext);
        }

        // tests in slot if exists
        self.getStencil(stclContext); // to block the slot
        try {

            // get local slot
            String slotPath = PathUtils.getSlotPath(path);
            PSlot<C, S> slot = getLocalSlot(stclContext, slotPath, self);
            if (SlotUtils.isNull(slot)) {
                String msg = logWarn(stclContext, "Cannot get stencil at slot %s as the slot %s doesn't exists in %s", path, slotPath, self);
                return self.nullPStencil(stclContext, Result.error(msg));
            }

            // add path condition
            return slot.getStencil(stclContext, getCombinedCondition(stclContext, cond, path, self));
        } finally {
            self.release(stclContext);
        }
    }

    /**
     * @return the stencils in path.
     */
    public StencilIterator<C, S> getStencils(C stclContext, String path, S self) {
        return getStencils(stclContext, path, null, self);
    }

    // TODO should verify cond tested before creating the iterator
    public StencilIterator<C, S> getStencils(C stclContext, String path, StencilCondition<C, S> cond, S self) {

        // if composed path
        if (PathUtils.isComposed(path)) {

            // get first stencils
            String first = PathUtils.getPathName(path);
            StencilIterator<C, S> firsts = self.getStencils(stclContext, first);
            if (firsts.isNotValid() || firsts.size() == 0) {
                return firsts;
            }

            // if no aggregation
            String tail = PathUtils.getLastName(path);
            if (firsts.size() == 1) {
                S stcl = firsts.next();
                return stcl.getStencils(stclContext, tail, cond);
            }

            // get last stencils
            Result status = Result.success();
            List<S> list = new ArrayList<S>();

            // do aggregation
            for (S stcl : firsts) {
                StencilIterator<C, S> iter = stcl.getStencils(stclContext, tail, cond);
                if (iter.isNotValid()) {
                    status.addOther(iter.getStatus());
                } else {
                    for (S last : iter) {

                        // the key may be empty if stencil get from simple slot
                        if (last.getKey().isEmpty()) {
                            last.setKey(new Key(stcl.getUId(stclContext)));
                        }

                        // adds the stencil to the list
                        list.add(last);
                    }
                }
            }

            // create the iterator with error status if exist
            StencilIterator<C, S> iter = new ListIterator<C, S>(list);
            if (status.isNotSuccess()) {
                iter.addStatus(status);
            }
            return iter;
        }

        // tests in slot if exists
        self.getStencil(stclContext); // to block the slot
        try {

            if (PathUtils.THIS.equals(path)) {
                return StencilUtils.<C, S> iterator(stclContext, self, self.getContainingSlot());
            }

            // get local slot
            String slotPath = PathUtils.getSlotPath(path);
            PSlot<C, S> slot = getLocalSlot(stclContext, slotPath, self);
            if (SlotUtils.isNull(slot)) {
                String msg = logWarn(stclContext, "Cannot get stencils at slot %s as the slot %s doesn't exists in %s", path, slotPath, self);
                return StencilUtils.<C, S> iterator(Result.error(msg));
            }

            // add path condition
            return slot.getStencils(stclContext, getCombinedCondition(stclContext, cond, path, self));
        } finally {
            self.release(stclContext);
        }
    }

    private StencilCondition<C, S> getCombinedCondition(C stclContext, StencilCondition<C, S> cond, String path, S self) {
        if (PathUtils.isKeyContained(path) || PathUtils.isExpContained(path)) {
            if (cond == null)
                return new PathCondition<C, S>(stclContext, PathUtils.getCondition(path), self);
            return new AndCondition<C, S>(new PathCondition<C, S>(stclContext, path, self), cond);
        }
        return cond;
    }

    /**
     * Returns the map of attributes defined.
     * 
     * @param stclContext
     *            the stencil context.
     * @param attributes
     *            the attributes path.
     * @param self
     *            this stencil as a plugged stencil.
     * @return the attributes map.
     */
    public SortedMap<IKey, String[]> getAttributes(C stclContext, String[] attributes, S self) {
        SortedMap<IKey, String[]> map = new TreeMap<IKey, String[]>();
        String[] m = new String[attributes.length];
        map.put(self.getKey(), m);
        int index = 0;
        for (String att : attributes) {
            String value = self.getString(stclContext, att, "");
            m[index] = value;
            index++;
        }
        return map;
    }

    /**
     * @return <tt>true</tt> if this stencil is plugged in another stencil slot.
     */
    public final boolean isPluggedIn(C stclContext, S stcl, String path, S self) {
        if (StencilUtils.isNull(stcl)) {
            return false;
        }
        PSlot<C, S> slot = stcl.getSlot(stclContext, path);
        if (SlotUtils.isNull(slot)) {
            return false;
        }
        return slot.contains(stclContext, null, self);
    }

    /**
     * Plug another stencil in a slot.
     */
    /*
     * public final S plug(C stclContext, S stcl, String path, S self) { if
     * (PathUtils.isKeyContained(path)) { String slotPath =
     * PathUtils.getSlotPath(path); String key = PathUtils.getKeyContained(path);
     * return plug(stclContext, stcl, slotPath, new Key(key), self); }
     * return plug(stclContext, stcl, path, Key.NO_KEY, self); }
     * 
     * public final S plug(C stclContext, S stcl, String slotPath, IKey key, S
     * self) { if (StencilUtils.isNull(stcl)) { String msg =
     * String.format("Cannot plug an empty stencil in %s in %s", slotPath, this);
     * if (getLog().isWarnEnabled()) getLog().warn(stclContext, msg); return
     * Stencil.<C, S> nullPStencil(stclContext, Result.error(msg)); } if
     * (PathUtils.isKeyContained(slotPath)) { String msg =
     * String.format("should not call plug in a slot path  %s with key %s",
     * slotPath, key.toString()); getLog().warn(stclContext, msg); } PSlot<C, S>
     * slot = getSlot(stclContext, slotPath, self); if (SlotUtils.isNull(slot)) {
     * String msg = String.format("Cannot plug as slot %s doesn't exist in %s",
     * slotPath, this); if (getLog().isWarnEnabled()) getLog().warn(stclContext,
     * msg); return Stencil.<C, S> nullPStencil(stclContext, Result.error(msg)); }
     * return plug(stclContext, stcl, slot, key, self); }
     * 
     * public final S plug(C stclContext, S stcl, PSlot<C, S> slot, IKey key, S
     * self) { PlugOrder<C, S> order = new PlugOrder<C, S>(stcl, key); return
     * order.plug(stclContext, slot); }
     */

    /**
     * Unplug this stencil from a slot.
     */
    public final void unplugFrom(C stclContext, String path, S self) {
        unplugOtherStencilFrom(stclContext, path, self, self);
    }

    public final void unplugOtherStencilFrom(C stclContext, String path, S self) {
        StencilIterator<C, S> iter = getStencils(stclContext, path, self);
        if (!iter.hasNext()) {
            return;
        }
        unplugOtherStencilFrom(stclContext, path, iter.next(), self);
    }

    public final void unplugOtherStencilFrom(C stclContext, String path, S stencil, S self) {
        String slotPath = PathUtils.getSlotPath(path);
        PSlot<C, S> slot = self.getSlot(stclContext, slotPath);
        if (SlotUtils.isNull(slot)) {
            logWarn(stclContext, "Cannot unplug as slot %s doesn't exist in %s", slotPath, this);
            return;
        }
        slot.unplug(stclContext, stencil, stencil.getKey());
    }

    //
    // Wrappers for commands
    //

    public final PSlot<C, S> getCommandsSlot(S container) {
        return new PSlot<C, S>(_commandSlot, container);
    }

    /*
     * public final Map<String, CommandStencil<C, S>> getCommands() { return
     * _commands; }
     * 
     * public final void addCommand(C stclContext, String name, CommandStencil<C,
     * S> cmd, S self) { if (cmd == null) { return; }
     * 
     * // adds it to the list cmd.setName(stclContext, name, cmd.self(stclContext,
     * null)); getCommands().put(name, cmd); }
     */

    public boolean hasCommand(C stclContext, String name, S self) {
        if (_command_descs == null) {
            return false;
        }
        return _command_descs.containsKey(name);
    }

    public S getCommand(C stclContext, String name, S self) {

        // checks name is not empty
        if (StringUtils.isEmpty(name)) {
            return StencilUtils.<C, S> nullPStencil(stclContext, Result.error("empty command name"));
        }

        // if name is a template class name
        if (name.indexOf('.') != -1) {
            String msg = String.format("cannot create command %s (deprecated)", name);
            return StencilUtils.<C, S> nullPStencil(stclContext, Result.error(msg));
        }

        // if the name is composed
        if (PathUtils.isComposed(name)) {
            String first = PathUtils.getPathName(name);
            String tail = PathUtils.getLastName(name);
            S target = self.getStencil(stclContext, first);
            return target.getCommand(stclContext, tail);
        }

        // checks an associated command descriptor exists in stencil
        if (_command_descs == null || !_command_descs.containsKey(name)) {
            String msg = String.format("cannot found command %s in %s", name, self);
            return StencilUtils.<C, S> nullPStencil(stclContext, Result.error(msg));
        }

        // creates command
        CommandDescriptor desc = _command_descs.get(name);
        StencilFactory<C, S> factory = (StencilFactory<C, S>) stclContext.<C, S> getStencilFactory();
        Class<? extends CommandStencil<C, S>> clazz = desc._clazz;
        S pcmd = factory.createPStencil(stclContext, getCommandsSlot(self), new Key(name), clazz);

        // adds paramters list
        ((CommandStencil<C, S>) pcmd.getReleasedStencil(stclContext))._defParams = desc.getDefaultParams();

        // returns command created
        return pcmd;
    }

    public CommandStatus<C, S> call(C stclContext, String name, S self, Object... params) {

        // creates a command context with parameters
        CommandContext<C, S> cmdContext = new CommandContext<C, S>(stclContext, self);
        for (int i = 0; i < params.length; i++) {
            cmdContext.setRedefinedParameter(CommandContext.PARAMS[i], params[i]);
        }

        // execute the command
        return call(cmdContext, name, self);
    }

    public CommandStatus<C, S> call(CommandContext<C, S> cmdContext, String name, S self) {
        C stclContext = cmdContext.getStencilContext();

        // get command stencil
        S cmdStcl = getCommand(stclContext, name, self);
        if (StencilUtils.isNull(cmdStcl)) {
            String msg = String.format("Command %s not defined for %s", name, self);
            throw new WrongPathException(msg);
        }

        // create the new command context
        CommandContext<C, S> newContext = cmdContext.clone();
        newContext.setTarget(cmdStcl.getContainer(stclContext));

        // execute the command
        CommandStencil<C, S> cmd = ((CommandStencil<C, S>) cmdStcl.getReleasedStencil(stclContext));
        return cmd.execute(newContext, cmdStcl);
    }

    // --------------------------------------------------------------------------
    //
    // Properties management.
    //
    // --------------------------------------------------------------------------

    /**
     * @return <tt>true</tt> if there is a property in path. May be redefined
     *         for efficiency reason.
     */
    /*
     * public boolean hasProperty(C stclContext, String path, S self) { if
     * (!hasSlot(stclContext, PathUtils.getSlotPath(path), self)) return false;
     * IPPropStencil<C, S> prop = getPropertyStencil(stclContext, path, self);
     * return (prop != null); }
     */

    //
    // Wrappers for facet
    //

    public FacetResult getFacet(RenderContext<C, S> renderContext) {
        String facet = renderContext.getFacetType();
        String mode = renderContext.getFacetMode();
        String msg = String.format("No facet found for %s for type %s in mode %s", this, facet, mode);
        return new FacetResult(FacetResult.ERROR, msg, null);
    }

    public void multipart(StclContext stclContext, String fileName, FileItem item, PStcl self) throws Exception {
    }

    /**
     * Saves the stencil as XML description in a file.
     * 
     * @param stclContext
     *            stencil context used.
     * @param out
     *            XML writer used.
     * @param self
     *            self as plugged stencil.
     * @throws IOException
     */
    public void saveAsStencil(C stclContext, XmlWriter out, S self) throws IOException {

        // if already saved in this document does nothing
        if (_writer == stclContext.getSaveWriter()) {
            return;
        }
        _writer = stclContext.getSaveWriter();

        // writer header
        out.writeHeader();
        out.write("<!DOCTYPE stencil PUBLIC \"-//StudioGdo//DTD Stencil 1.0//EN\" \"stencil.dtd\">\n");

        // opens stencil tag
        out.startElement(InterpretedStencilFactory.STENCIL);
        out.writeAttribute("id", getUId(stclContext));
        if (StringUtils.isNotEmpty(_name)) {
            out.writeAttribute("name", _name);
        }
        out.writeAttribute(InterpretedStencilFactory.TEMPLATE, getTemplateName());
        out.closeElement();
        saveConstructorParameters(stclContext, out, self);

        // adds plug part
        XmlStringWriter plugPart = new XmlStringWriter(false, 1, out.getEncoding());
        saveSlots(stclContext, out, plugPart, self);
        out.write(plugPart.getString());
        plugPart.close();

        // ends stencil tag
        out.endElement(InterpretedStencilFactory.STENCIL);
    }

    /**
     * Saves the stencil as an instance description.
     * 
     * @param stclContext
     *            the stencil context.
     * @param dir
     *            the container path (in instance repository).
     * @param out
     *            the writer used to save the stencil (to avoid writting twice).
     * @param self
     *            the stencil as plugged stencil.
     * @return the reference id used to retrieve the instance declaration in the
     *         file.
     */
    public String saveAsInstance(C stclContext, String dir, XmlWriter out, S self) {
        try {

            // composed id
            String id = self.getUId(stclContext);
            String instance = PathUtils.compose(dir, id);

            // if already saved in this document does nothing
            if (_writer == stclContext.getSaveWriter()) {
                return instance;
            }
            _writer = stclContext.getSaveWriter();

            // adds other instances declared
            XmlStringWriter declPart = new XmlStringWriter(false, 1, out.getEncoding());
            XmlStringWriter plugPart = new XmlStringWriter(false, 2, out.getEncoding());
            saveSlots(stclContext, declPart, plugPart, self);
            out.write(declPart.getString());
            declPart.close();

            // starts instance tag
            out.startElement(InterpretedStencilFactory.INSTANCE);
            out.writeAttribute("id", id);
            if (StringUtils.isNotBlank(_name)) {
                out.writeAttribute("name", _name);
            }
            out.writeAttribute(InterpretedStencilFactory.TEMPLATE, getTemplateName());
            saveConstructorParameters(stclContext, out, self);
            out.closeElement();
            out.write(plugPart.getString());
            plugPart.close();
            out.endElement(InterpretedStencilFactory.INSTANCE);
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Should be redefined if the stencil needs parameters at creation.
     * 
     * @param stclContext
     * @param writer
     * @param self
     */
    protected void saveConstructorParameters(C stclContext, XmlWriter writer, S self) {
        try {
            if (_value != null) {
                writer.startElement("param");
                writer.writeAttribute("index", 0);
                writer.writeAttribute("type", getType());

                // never expand when saving
                String value = getValue(stclContext, self);
                if (value != null) {
                    writer.writeCDATA(value); // never expand when saving
                }
                writer.endElement("param", false);
            }
        } catch (IOException e) {
            logError(stclContext, "Cannot save constructor parameters : %s", e);
        }
    }

    /**
     * Saves the slot and its content.
     * 
     * @param stclContext
     *            the stencil context.
     * @param descPart
     *            the description part.
     * @param plugPart
     *            the plug part.
     * @param self
     *            this stencil as a plugged stencil.
     */
    protected void saveSlots(C stclContext, XmlWriter descPart, XmlWriter plugPart, S self) throws IOException {

        // no slot saved in case of property
        if (_value != null) {
            return;
        }

        // saves all slots content
        PSlot<C, S> pslot = null;
        for (_Slot<C, S> slot : getSlots().values()) {

            // the slot as a plugged slot
            if (pslot == null) {
                pslot = new PSlot<C, S>(slot, self);
            } else {
                pslot.setSlot(slot);
            }

            // saves all the plugs in the the slot
            slot.savePlugs(stclContext, descPart, plugPart, pslot);
        }
    }

    /**
     * Returns this stencil as a plugged one (container only used for cloning)
     * 
     * @param stclContext
     *            stencil context used.
     * @param container
     *            container stencil.
     */
    @SuppressWarnings("unchecked")
    public S self(C stclContext, S container) {

        // stand alone plugged stencil
        if (StencilUtils.isNull(container)) {

            // if not already defined creates it
            if (StencilUtils.isNull(_self)) {
                StencilFactory<C, S> factory = (StencilFactory<C, S>) stclContext.<C, S> getStencilFactory();
                _self = factory.newPStencil(stclContext, (PSlot<C, S>) null, Key.NO_KEY, this);
            }
            return (S) _self;
        }

        // creates the plugged stencil structure
        StencilFactory<C, S> factory = (StencilFactory<C, S>) stclContext.<C, S> getStencilFactory();
        return factory.newPStencil(stclContext, container.getContainingSlot(), Key.NO_KEY, this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof _Stencil<?, ?>) {
            return super.equals(obj);
        }
        if (obj instanceof _PStencil<?, ?>) {
            return obj.equals(this);
            // return super.equals(((PStencil<C, ?>) obj).getStencil());
        }
        return false;
    }

    @Override
    public String toString() {
        if (_value != null) {
            StringBuffer str = new StringBuffer();
            str.append('"').append(_value.toString()).append('"');
            str.append('[').append(getType()).append(']');
            str.append('<').append(getClass()).append('>');
            return str.toString();
        }
        StringBuffer name = new StringBuffer(super.toString());
        name.append('-').append(StringUtils.isEmpty(_name) ? getUId(null) : _name);
        name.append('[').append(getTemplateName()).append(']');
        return name.toString();
    }

    /**
     * @return the unique internal containing key (container + slot [+ key]).
     */
    String getContainingKey(C stclContext, PSlot<C, S> slot, IKey key) {

        // prefix verification
        if (SlotUtils.isNull(slot)) {
            if (getLog().isWarnEnabled())
                getLog().warn(stclContext, "Internal warning 1 : see Stencil.getContainingKey");
            return null;
        }
        if (StencilUtils.isNull(slot.getContainer())) {
            if (getLog().isWarnEnabled())
                getLog().warn(stclContext, "Internal warning 2 : see Stencil.getContainingKey");
            return null;
        }

        StringBuffer unique = new StringBuffer(slot.getContainer().getId(stclContext));
        unique.append(slot.getName(stclContext));
        if (key != null)
            unique.append(key);
        return unique.toString();
    }

    /**
     * Implementation of the THIS slot.
     */
    class ThisSlot extends SingleCalculatedSlot<C, S> {

        public ThisSlot(C stclContext, _Stencil<C, S> in, String path) {
            super(stclContext, in, path, PSlot.ONE);
            setAcceptPlug(false);
        }

        @Override
        public S getCalculatedStencil(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
            return self.getContainer();
        }
    }

    /**
     * Implementation of the ROOT slot.
     */
    private class RootSlot extends SingleCalculatedSlot<C, S> {

        public RootSlot(C stclContext, _Stencil<C, S> in, String name) {
            super(stclContext, in, name, PSlot.ONE);
        }

        @Override
        public S getStencil(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
            return getCalculatedStencil(stclContext, cond, self);
        }

        @Override
        public StencilIterator<C, S> getStencils(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
            return StencilUtils.<C, S> iterator(stclContext, getCalculatedStencil(stclContext, cond, self), self);
        }

        @Override
        public S getCalculatedStencil(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
            S container = self.getContainer();
            return container.getRootStencil(stclContext);
        }
    }

    /**
     * Implementation of the PARENT slot. Defined as a multi slot to have a key
     * defined.
     */
    class ParentSlot extends MultiCalculatedSlot<C, S> {
        public ParentSlot(C stclContext, _Stencil<C, S> in, String name) {
            super(stclContext, in, name, PSlot.AT_LEAST_ONE);
        }

        @Override
        public boolean hasStencils(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
            S container = self.getContainer();
            return StencilUtils.isNotNull(container) && StencilUtils.isNotNull(container.getContainer(stclContext));
        }

        @Override
        public S getStencil(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
            S container = self.getContainer();
            return container.getContainer(stclContext);
        }

        @Override
        protected StencilIterator<C, S> getStencilsList(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self) {
            S contained = getStencil(stclContext, cond, self);
            if (StencilUtils.isNull(contained)) {
                logWarn(stclContext, "Cannot get stencils list from %s", self);
                return StencilUtils.<C, S> iterator();
            }
            return StencilUtils.<C, S> iterator(stclContext, contained, contained.getContainingSlot());
        }
    }

    /**
     * Implementation of the NAME slot.
     */
    private class NameSlot extends CalculatedStringPropertySlot<C, S> {

        public NameSlot(C stclContext, _Stencil<C, S> in, String name) {
            super(stclContext, in, name);
            setAcceptPlug(true);
        }

        @Override
        public String getValue(C stclContext, S self) {
            return self.getContainer(stclContext).getName(stclContext);
        }

        @Override
        public String setValue(C stclContext, String value, S self) {
            S container = self.getContainer(stclContext);
            String old = container.getName(stclContext);
            container.setName(stclContext, value);
            return old;
        }
    }

    /**
     * Implementation of the COMMENT slot.
     */
    /*
     * protected class CommentSlot extends CalculatedStringPropertySlot<C, S> {
     * public CommentSlot(C stclContext) { super(stclContext, Stencil.this,
     * Slot.COMMENT); setAcceptPlug(true); }
     * 
     * @Override public String getValue(C stclContext, S self) { S container =
     * self.getContainer(stclContext); return
     * container.getReleasedStencil(stclContext).getComment(stclContext,
     * container); }
     * 
     * @Override public String setValue(C stclContext, String value, S self) { S
     * container = self.getContainer(stclContext); String old =
     * container.getReleasedStencil(stclContext).getComment(stclContext,
     * container);
     * container.getReleasedStencil(stclContext).setComment(stclContext, value,
     * container); return old; } }
     */

    protected class DescriptionSlot extends CalculatedStringPropertySlot<C, S> {
        public DescriptionSlot(C stclContext, _Stencil<C, S> in, String name) {
            super(stclContext, in, name);
        }

        @Override
        public String getValue(C stclContext, S self) {
            return "Not done (should read template descriptor)";
        }
    }

    //
    // Slot description
    //

    protected _SlotDescriptor<C, S> addDescriptor(String name, _SlotDescriptor<C, S> desc) {
        if (_slot_descs == null) {
            _slot_descs = new HashMap<String, _SlotDescriptor<C, S>>();
        }
        if (desc != null)
            return _slot_descs.put(name, desc);
        return null;
    }

    protected _SlotDescriptor<C, S> addDescriptor(String name, Class<? extends _Slot<C, S>> clazz) {
        _SlotDescriptor<C, S> desc = new _SlotDescriptor<C, S>() {
            @Override
            public _Slot<C, S> add(C stclContext, String name, S self) {
                try {
                    Constructor<? extends _Slot<C, S>> c = clazz.getConstructor(stclContext.getClass());
                    return c.newInstance(stclContext);
                } catch (Exception e) {
                    logError(stclContext, "Cannot add slot %s : %s", name, e);
                    return null;
                }
            }
        };
        return addDescriptor(name, desc);
    }

    public _SlotDescriptor<C, S> emptySlot(String name) {
        _SlotDescriptor<C, S> desc = new EmptySlotDescriptor<C, S>();
        return addDescriptor(name, desc);
    }

    public _SlotDescriptor<C, S> singleSlot(String name) {
        _SlotDescriptor<C, S> desc = new SingleSlotDescriptor<C, S>();
        return addDescriptor(name, desc);
    }

    public _SlotDescriptor<C, S> singleSlot(String name, Links links) {
        _SlotDescriptor<C, S> desc = new SingleSlotDescriptor<C, S>(links);
        return addDescriptor(name, desc);
    }

    public _SlotDescriptor<C, S> singleSlot(String name, char arity, boolean tranzient, Links links) {
        _SlotDescriptor<C, S> desc = new SingleSlotDescriptor<C, S>(arity, tranzient, links);
        return addDescriptor(name, desc);
    }

    public _SlotDescriptor<C, S> multiSlot(String name) {
        _SlotDescriptor<C, S> desc = new MultiSlotDescriptor<C, S>();
        return addDescriptor(name, desc);
    }

    public _SlotDescriptor<C, S> multiSlot(String name, char arity, boolean tranzient, Links links) {
        _SlotDescriptor<C, S> desc = new MultiSlotDescriptor<C, S>(arity, tranzient, links);
        return addDescriptor(name, desc);
    }

    public _SlotDescriptor<C, S> propSlot(String name, boolean initial) {
        _SlotDescriptor<C, S> desc = new PropSlotDescriptor<C, S, Boolean>(Boolean.valueOf(initial));
        return addDescriptor(name, desc);
    }

    public _SlotDescriptor<C, S> propSlot(String name, int initial) {
        _SlotDescriptor<C, S> desc = new PropSlotDescriptor<C, S, Integer>(Integer.valueOf(initial));
        return addDescriptor(name, desc);
    }

    public _SlotDescriptor<C, S> propSlot(String name) {
        return propSlot(name, null);
    }

    public _SlotDescriptor<C, S> propSlot(String name, String initial) {
        _SlotDescriptor<C, S> desc = new PropSlotDescriptor<C, S, String>(initial);
        return addDescriptor(name, desc);
    }

    public _SlotDescriptor<C, S> delegateSlot(String name, String path) {
        _SlotDescriptor<C, S> desc = new DelegateSlotDescriptor<C, S>(path);
        return addDescriptor(name, desc);
    }

    //
    // Command description
    //

    private class CommandDescriptor {
        Class<? extends CommandStencil<C, S>> _clazz;
        Object[] _params;

        CommandDescriptor(Class<? extends CommandStencil<C, S>> clazz, Object... params) {
            _clazz = clazz;
            _params = params;
        }

        Map<String, Object> getDefaultParams() {
            Map<String, Object> map = new HashMap<>(); // not ConcurrentHashMap
                                                       // because map can't be
                                                       // null see:
                                                       // http://stackoverflow.com/questions/698638/why-does-concurrenthashmap-prevent-null-keys-and-values
            int index = 0;
            for (Object param : _params) {
                String key = CommandStencil.PARAM_PREFIX + ++index;
                map.put(key, param);
            }
            return map;
        }
    }

    public void command(String name, Class<? extends CommandStencil<C, S>> clazz, Object... params) {
        if (_command_descs == null) {
            _command_descs = new HashMap<>();
        }
        _command_descs.put(name, new CommandDescriptor(clazz, params));
    }

    //
    // PROPERTY PART
    //

    public String getType(C stclContext, S self) {
        return _type;
    }

    public void setType(C stclContext, String type, S self) {
        _type = type;
    }

    public boolean isExpand(C stclContext, S self) {
        return _expand;
    }

    public void setExpand(C stclContext, boolean expand, S self) {
        _expand = expand;
    }

    public String getValue(C stclContext, S self) {
        return _value;
    }

    /**
     * Sets the property value.
     * 
     * @param stclContext
     *            the stencil context.
     * @param value
     *            the property value.
     * @param self
     *            the stencil as a plugged stencil.
     * @return the old property value.
     */
    public String setValue(C stclContext, String value, S self) {

        // sets in contained value
        String old = _value;
        _value = value;
        notifyListeners(stclContext, value, old, self);

        return old;
    }

    public Reader getReader(C stclContext, S self) {
        if (_value == null)
            return StringHelper.EMPTY_STRING_READER;
        return new StringReader(_value.toString());
    }

    public InputStream getInputStream(C stclContext, S self) {
        if (_value != null) {
            try {
                return IOUtils.toInputStream(_value.toString(), _StencilContext.getCharacterEncoding());
            } catch (IOException e) {
                logError(stclContext, e.toString());
            }
        }
        return StringHelper.EMPTY_STRING_INPUT_STREAM;
    }

    public OutputStream getOutputStream(C stclContext, S self) {
        return null;
    }

    /**
     * Notifies all property change listeners that the property value has
     * changed.
     * 
     * @param stclContext
     *            stencil context.
     * @param value
     *            new value.
     * @param old
     *            old value.
     * @param self
     *            the property as a plugged stencil.
     */
    @SuppressWarnings("unchecked")
    public void notifyListeners(C stclContext, String value, String old, S self) {

        // notify listeners
        try {
            PropertyChangeEvent<C, S> event = new PropertyChangeEvent<C, S>(stclContext, self, old, value);
            for (S listener : getListeners(stclContext, self)) {
                IPropertyChangeListener<C, S> l = (IPropertyChangeListener<C, S>) (listener).getReleasedStencil(stclContext);
                Result result = l.propertyChange(event);
                if (!result.isSuccess()) {
                    if (getLog().isWarnEnabled()) {
                        getLog().warn(stclContext, "Property listener action not succeed");
                    }
                    _value = old;
                    break;
                }
            }
        } catch (Exception e) {
            logWarn(stclContext, "Exception in property listener : %s", e);
        }
    }

    // redefined in EnumProp or other specific property as false
    public boolean shouldBeSavedAsProp(C stclContext, S self) {
        // TODO to optimize : slots should not be taken in account to test == 1
        if (self.isPluggedOnce(stclContext)) {
            return SlotUtils.isSingle(stclContext, self.getContainingSlot(stclContext));
        }
        return false;
    }

    public void saveAsProp(C stclContext, String name, XmlWriter writer) throws IOException {
        writer.startElement("prop");
        writer.writeAttribute("name", name);
        writer.writeAttribute("type", getType());
        String value = self(stclContext, null).getNotExpandedValue(stclContext); // never
                                                                                 // expand
        // when saving
        if (value != null) {
            writer.startElement("data");
            writer.writeCDATAAndEndElement(value);
        }
        writer.endElement("prop");
    }

    /**
     * Returns value type of the property. TODO should return class (see also
     * with PropertyCalculator) May be redefined in subclasses to cover new
     * basic types.
     * 
     * @return the XML code for the type.
     */
    protected String getType() {
        return Keywords.STRING;
    }

    private StencilIterator<C, S> getListeners(C stclContext, S self) {
        return getStencils(stclContext, Slot.LISTENERS, self);
    }

    //
    // LOG PART
    //

    public static final StencilLog _LOG = new StencilLog(_Stencil.class);

    protected StencilLog getLog() {
        return _LOG;
    }

    public String logTrace(_StencilContext stclContext, String format, Object... params) {
        return getLog().logTrace(stclContext, format, params);
    }

    public String logWarn(_StencilContext stclContext, String format, Object... params) {
        return getLog().logWarn(stclContext, format, params);
    }

    public String logError(_StencilContext stclContext, String format, Object... params) {
        return getLog().logError(stclContext, format, params);
    }

}
