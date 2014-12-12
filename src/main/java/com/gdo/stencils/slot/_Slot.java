/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.slot;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.gdo.helper.ConverterHelper;
import com.gdo.project.adaptor.LinkStcl;
import com.gdo.stencils.Result;
import com.gdo.stencils._Stencil;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.descriptor.Links;
import com.gdo.stencils.interpreted.LinkDescriptor;
import com.gdo.stencils.interpreted.SlotDescriptor;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.log.StencilLog;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.SlotUtils;
import com.gdo.stencils.util.StencilUtils;
import com.gdo.util.XmlWriter;

/**
 * <p>
 * Abstract common class for all slot classes.
 * </p>
 */
public abstract class _Slot<C extends _StencilContext, S extends _PStencil<C, S>> {

    // associated links
    public boolean _fromXML;
    private Map<String, String> _links; // plug link stencil on stencils in slot

    // meta data
    private SlotDescriptor<C, S> _desc; // slot descriptor used (if exists)

    // attributes
    protected _Stencil<C, S> _container; // stencil in which the slot is defined
    private String _name; // unique slot name
    private char _arity; // arity
    private boolean _tranzient; // should the plugged stencils be saved?
    private boolean _read_only; // the slot is read only

    private int _completionLevel; // accept plug in this slot only if completion

    // level of plug is lower
    // else means slot redefined and then plugs are no more visible

    protected _Slot(C stclContext, _Stencil<C, S> container, String name, char arity, boolean tranzient) {

        // verifies unique
        _Slot<C, S> slot = container.getSlots().get(name);
        if (slot != null) {
            logWarn(stclContext, "slot %s is already defined in %s (will be redefined...)", name, container);
        }

        // set characteristics
        _container = container;
        _name = name;
        _arity = arity;
        _tranzient = tranzient;

        // stores structure
        _container.addSlot(stclContext, this);
    }

    /**
     * Clears all internal structures to free memory.
     */
    public void clear() {
        _links = null;
        _desc = null;
        _container = null;
    }

    /**
     * Returns the name of this slot.
     * 
     * @param stclContext
     *            the stencil context.
     * @return the slot's name.
     */
    public String getName(C stclContext) {
        return _name;
    }

    /**
     * @return the slot's descriptor.
     */
    public SlotDescriptor<C, S> getDescriptor() {
        return _desc;
    }

    /**
     * Sets the slot's descriptor.
     */
    public void setDescriptor(SlotDescriptor<C, S> desc) {
        _desc = desc;
    }

    /**
     * Returns the arity of this slot.
     * 
     * @return the slot's arity.
     */
    public char getArity(C stclContext) {
        return _arity;
    }

    /**
     * Checks if this slot is transient.
     * 
     * @return <tt>true</tt> if the stencils plugged in this slot must not be
     *         saved in stencil configuration.
     */
    public boolean isTransient(C stclContext) {
        return _tranzient;
    }

    /**
     * Set this slot transient.
     */
    public void setTransient(C stclContext) {
        _tranzient = true;
    }

    /**
     * Checks if this slot is read only.
     * 
     * @return <tt>true</tt> if the slot is read only.
     */
    public boolean isReadOnly(C stclContext) {
        return _read_only;
    }

    /**
     * Set this slot read only.
     */
    public void setReadOnly(C stclContext) {
        _read_only = true;
    }

    /**
     * Checks if a slot is a slot cursor.
     * 
     * @param stclContext
     *            the stencil context.
     * @return <tt>true</tt> if the slot is cursor based.
     */
    public boolean isCursorBased(C stclContext) {
        return false;
    }

    /**
     * 
     * A property can be accessed directly from this slot.
     * 
     * @param stclContext
     *            the stencil context.
     * @param key
     *            the stencil key.
     * @param name
     *            the property's name.
     * @param self
     *            this slot as a plugged slot.
     * @return the property value. <tt>null</tt> if the property is not defined
     *         by this slot.
     */
    public String getProperty(C stclContext, IKey key, String name, PSlot<C, S> self) {
        return null;
    }

    /**
     * Set the property accessed directly from this slot.
     * 
     * @param stclContext
     *            the stencil context.
     * @param value
     *            the property value.
     * @param key
     *            the stencil key.
     * @param name
     *            the property's name
     * @param self
     *            this slot as a plugged slot.
     */
    public void setProperty(C stclContext, String value, IKey key, String name, PSlot<C, S> self) {
    }

    // LINK PART

    public abstract boolean hasAdaptorStencil(C stclContext, PSlot<C, S> self);

    public abstract S getAdaptorStencil(C stclContext, PSlot<C, S> self);

    public abstract int size(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self);

    public abstract boolean hasStencils(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self);

    public abstract S getStencil(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self);

    public abstract StencilIterator<C, S> getStencils(C stclContext, StencilCondition<C, S> cond, PSlot<C, S> self);

    public abstract boolean contains(C stclContext, StencilCondition<C, S> cond, S searched, PSlot<C, S> self);

    /**
     * Changes the key used for the stencil in the slot.
     * 
     * @param stclContext
     *            stencil context.
     * @param searched
     *            the stencil which key must change.
     * @param key
     *            the new key.
     * @param self
     *            this slot as a plugged slot.
     * @return <tt>true</tt> if the change was done.
     */
    public abstract boolean changeKey(C stclContext, S searched, String key, PSlot<C, S> self);

    public abstract boolean canChangeOrder(C stclContext, PSlot<C, S> self);

    public abstract boolean isFirst(C stclContext, S searched, PSlot<C, S> self);

    public abstract boolean isLast(C stclContext, S searched, PSlot<C, S> self);

    /**
     * @return the java classes prototype signature. Should be redefined in each
     *         specific slot declaration. By default, no signature means any
     *         kind of stencil is accepted.
     */
    protected Iterable<Class<?>> getClassesProto(C stclContext) {
        return Collections.<Class<?>> emptyList();
    }

    /**
     * @return the slot names prototype signature. Should be redefined in each
     *         specific inner declaration. By default, no signature means any
     *         kind of stencil is accepted.
     */
    protected Iterable<String> getSlotsProto(C stclContext) {
        return Collections.<String> emptyList();
    }

    /**
     * @return the prop names prototype signature. Should be redefined in each
     *         specific inner declaration. By default, no signature means any
     *         kind of stencil is accepted.
     */
    protected Iterable<String> getCommandsProto(C stclContext) {
        return Collections.<String> emptyList();
    }

    //
    // PLUG PART
    //

    /**
     * Method called before the plug order will be executed for this slot and if
     * it returns <tt>false</tt> then the order is not executed. Default method
     * verifies prototypes if defined.
     * 
     * @param stclContext
     *            the stencil context.
     * @param stencil
     *            stencil to be plugged.
     * @param self
     *            this stencil as a plugged stencil.
     */
    public Result beforePlug(C stclContext, S stencil, IKey key, PSlot<C, S> self) {

        // the stencil must be defined in the plug order
        if (StencilUtils.isNull(stencil)) {
            String msg = logWarn(stclContext, "no stencil defined when plugging in %s", self);
            return Result.error(msg);
        }

        // production optimization
        if (!_Stencil.STRICT_MODE) {
            return Result.success();
        }

        // checks prototypes
        Result result = null; // warnings can be raised
        if (!stencil.isLink(stclContext)) {

            // test java classes prototype
            Class<?> clazz = stencil.getClass();
            for (Class<?> cl : getClassesProto(stclContext)) {
                if (!cl.isAssignableFrom(clazz)) {
                    String msg = logWarn(stclContext, "Should not plug %s in %s : wrong java prototype (found %s, expected %s)", stencil, self, clazz, cl);
                    result = Result.warn(getClass().getName(), 2, msg, result);
                }
            }

            // test slots prototype
            for (String cmd : getCommandsProto(stclContext)) {
                if (!stencil.hasCommand(stclContext, cmd)) {
                    String msg = String.format("Should not plug %s in %s : this stencil plugged doesn't have the command %s", stencil, self, cmd);
                    if (getLog().isWarnEnabled())
                        getLog().warn(stclContext, msg);
                    result = Result.warn(getClass().getName(), 5, msg, result);
                }
            }
        }

        // all was ok, the plug can be performed
        return Result.success();
    }

    /**
     * This method is called after a plug order is executed.
     * 
     * @param stclContext
     *            the stencil context.
     * @param stencil
     *            the stencil plugged.
     * @param self
     *            this slot as a contained slot.
     */
    public void afterPlug(C stclContext, S stencil, PSlot<C, S> self) {

        // plugs links associated in XML slot descriptor
        SlotDescriptor<C, S> desc = getDescriptor();
        if (desc != null && desc.getLinkDescriptors() != null && _links == null) {
            for (LinkDescriptor<C, S> linkDesc : desc.getLinkDescriptors()) {
                plugLink(stclContext, stencil, linkDesc, self);
            }
        }

        // plus links associated in slot descriptor
        if (_links != null) {
            for (Map.Entry<String, String> link : _links.entrySet()) {
                plugLink(stclContext, stencil, link.getKey(), link.getValue(), self);
            }
        }
    }

    /**
     * This method is called before the unplug order will be executed and if it
     * returns false then the order is not executed. Default method accepts
     * order if stencil is not empty.
     */
    public Result beforeUnplug(C stclContext, S stencil, IKey key, PSlot<C, S> self) {
        return Result.success();
    }

    public Result beforeUnplugAll(C stclContext, PSlot<C, S> self) {
        return Result.success();
    }

    /**
     * This method is called after an unplug order is executed.
     * 
     * @param stclContext
     *            the stencil context.
     * @param stencil
     *            the stencil unplugged.
     * @param self
     *            this slot as a plugged slot.
     */
    public Result afterUnplug(C stclContext, S stencil, PSlot<C, S> self) {
        return Result.success();
    }

    /**
     * Internal use only (cannot be private as used by plug package).
     */
    public final S plug(C stclContext, S stencil, IKey key, PSlot<C, S> self) {

        // verifies the plug is enabled
        try {
            Result before = beforePlug(stclContext, stencil, key, self);
            if (before.isNotSuccess()) {
                return StencilUtils.<C, S> nullPStencil(stclContext, before);
            }
        } catch (Exception e) {
            String msg = logError(stclContext, "Exception in beforePlug method on %s in %s: %s", stencil, self, e);
            return StencilUtils.<C, S> nullPStencil(stclContext, Result.error(msg));
        }

        // performs the real plug
        try {
            S plugged = doPlug(stclContext, stencil, key, self);
            if (StencilUtils.isNotNull(plugged)) {
                logTrace(stclContext, "Plug %s in %s", plugged, self);
                plugged.addThisReferenceToStencil(stclContext);
            }

            // performs extra jobs after plugging
            try {
                afterPlug(stclContext, plugged, self);
            } catch (Exception e) {
                String msg = logError(stclContext, "Exception in afterPlug method on %s in %s: %s", stencil, self, e);
                return StencilUtils.<C, S> nullPStencil(stclContext, Result.error(msg));
            }
            return plugged;
        } catch (Exception e) {
            String msg = logError(stclContext, "Exception in doPlug method on %s in %s: %s", stencil, self, e);
            return StencilUtils.<C, S> nullPStencil(stclContext, Result.error(msg));
        }
    }

    /**
     * Should be redefined in subclasses.doPlug
     */
    protected abstract S doPlug(C stclContext, S stencil, IKey key, PSlot<C, S> self);

    /**
     * Internal use only (cannot be private as used by plug package).
     */
    public final void unplug(C stclContext, S stencil, IKey key, PSlot<C, S> self) {

        // verifies the unplug is enabled
        try {
            Result before = beforeUnplug(stclContext, stencil, key, self);
            if (before.isNotSuccess()) {
                return;
            }
        } catch (Exception e) {
            logError(stclContext, "Exception in before unplug method on %s in %s: %s", stencil, self, e);
            return;
        }

        // performs the real unplug
        try {
            doUnplug(stclContext, stencil, key, self);
            stencil.removeThisReferenceFromStencil(stclContext);
            logTrace(stclContext, "Unplug %s from %s", stencil, self);

            // performs extra jobs after unplugging
            try {
                afterUnplug(stclContext, stencil, self);
            } catch (Exception e) {
                logError(stclContext, "Exception in after unplug method on %s in %s: %s", stencil, self, e);
                return;
            }
        } catch (Exception e) {
            logError(stclContext, "Exception in unplug method on %s in %s: %s", stencil, self, e);
            return;
        }
    }

    /**
     * Should be redefined in subclasses.
     */
    protected abstract void doUnplug(C stclContext, S stencil, IKey key, PSlot<C, S> self);

    /**
     * Unplugs all stencils contained in this slot.
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            this slot as a plugged slot.
     */
    public void unplugAll(C stclContext, PSlot<C, S> self) {
        try {

            // verifies the unplug is enabled
            Result before = beforeUnplugAll(stclContext, self);
            if (before.isNotSuccess())
                return;

            // performs the real unplug on all stencils contained
            doUnplugAll(stclContext, self);
        } catch (Exception e) {
            logError(stclContext, "Exception in unplug all method in %s", self);
        }
    }

    protected abstract void doUnplugAll(C stclContext, PSlot<C, S> self);

    //
    // SAVE PART
    //

    /**
     * Returns the stencils to be saved as plugged in the description.
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            the slot as a plugged slot.
     * @return an iteraotr to the stencils to be saved.
     */
    protected abstract StencilIterator<C, S> getStencilsToSave(C stclContext, PSlot<C, S> self);

    /**
     * Declared public to be accessed by stencil.
     */
    public void savePlugs(C stclContext, XmlWriter declPart, XmlWriter plugPart, PSlot<C, S> self) throws IOException {

        // does nothing on transient slot
        if (isTransient(stclContext)) {
            return;
        }

        // saves plugs
        for (S plugged : getStencilsToSave(stclContext, self)) {

            // don't save transient stencils
            if (StencilUtils.isNull(plugged) || plugged.isTransient(stclContext)) {
                continue;
            }

            // saves stencil plugged
            savePlugged(stclContext, declPart, plugPart, plugged);
        }
    }

    private void savePlugged(C stclContext, XmlWriter declPart, XmlWriter plugPart, S plugged) throws IOException {

        // saves instance in global name space
        String ref = plugged.saveAsInstance(stclContext, "/", declPart);
        if (StringUtils.isEmpty(ref)) {
            return;
        }

        // creates plug descriptor
        plugPart.startElement("plug");
        plugPart.writeAttribute("ref", ref);
        plugPart.writeAttribute("slot", getName(stclContext));
        IKey key = plugged.getKey();
        if (!key.isEmpty()) {
            plugPart.writeAttribute("key", key);
        }
        plugPart.endElement("plug");
    }

    public int getCompletionLevel() {
        return _completionLevel;
    }

    public void setCompletionLevel(int completionLevel) {
        _completionLevel = completionLevel;
    }

    public void setLinks(Map<String, String> links) {
        _links = links;
    }

    public void addLink(String slot, String to) {
        if (_links == null) {
            _links = new Links();
        }
        _links.put(slot, to);
    }

    // plug the link described by the descriptor when a stencil is plugged in
    // the slot
    @SuppressWarnings("deprecation")
    private void plugLink(C stclContext, S stencil, LinkDescriptor<C, S> linkDesc, PSlot<C, S> self) {

        // get slot where the link will be plugged
        String slotPath = linkDesc.getSlot();
        PSlot<C, S> slot = stencil.getSlot(stclContext, slotPath);
        if (SlotUtils.isNull(slot)) {
            logWarn(stclContext, "cannot found slot %s in %s for link in %s", slotPath, stencil, self);
            return;
        }

        // get the link path and add .. as path is relative to link not to
        // stencil
        String path = linkDesc.getPath();
        if (StringUtils.isEmpty(path)) {
            if (getLog().isWarnEnabled()) {
                String msg = String.format("link path may not be empty for %s for link in %s", slotPath, self);
                getLog().warn(stclContext, msg);
            }
            return;
        }
        path = PathUtils.compose(PathUtils.PARENT, path);

        // create link stencil in slot
        IKey key = (linkDesc.getKey() != null) ? new Key(linkDesc.getKey()) : Key.NO_KEY;
        S link = stencil.newPStencil(stclContext, slot, key, LinkStcl.class.getName(), path);
        if (ConverterHelper.parseBoolean(linkDesc.getLocal())) {
            link.setBoolean(stclContext, LinkStcl.Slot.LOCAL, true);
        }
        link.setTransient(stclContext, true);
    }

    private void plugLink(C stclContext, S stencil, String slotPath, String path, PSlot<C, S> self) {

        // get slot where the link will be plugged
        PSlot<C, S> slot = stencil.getSlot(stclContext, slotPath);
        if (SlotUtils.isNull(slot)) {
            logWarn(stclContext, "cannot found slot %s in %s for link in %s", slotPath, stencil, self);
            return;
        }

        // get the link path and add .. as path is relative to link not to
        // stencil
        if (StringUtils.isEmpty(path)) {
            logWarn(stclContext, "link path may not be empty for %s for link in %s", slotPath, self);
            return;
        }
        path = PathUtils.compose(PathUtils.PARENT, path);

        // create link stencil in slot
        @SuppressWarnings("deprecation")
        S link = stencil.newPStencil(stclContext, slot, Key.NO_KEY, LinkStcl.class.getName(), path);
        link.setTransient(stclContext, true);
    }

    //
    // MISC
    //

    @Override
    public String toString() {
        try {
            StringBuffer str = new StringBuffer(_name);
            str.append('{').append(_arity).append('}');
            return str.toString();
        } catch (Exception e) {
            return _name;
        }
    }

    //
    // LOG PART
    //

    protected static final StencilLog LOG = new StencilLog(_Slot.class);

    public static StencilLog getLog() {
        return LOG;
    }

    public static <C extends _StencilContext> String logWarn(C stclContext, String format, Object... params) {
        if (LOG.isWarnEnabled()) {
            String msg = (params.length == 0) ? format : String.format(format, params);
            LOG.warn(stclContext, msg);
            return msg;
        }
        return "";
    }

    public static <C extends _StencilContext> String logError(C stclContext, String format, Object... params) {
        if (LOG.isErrorEnabled()) {
            String msg = (params.length == 0) ? format : String.format(format, params);
            LOG.error(stclContext, msg);
            return msg;
        }
        return "";
    }

    public static <C extends _StencilContext> String logTrace(C stclContext, String format, Object... params) {
        if (LOG.isTraceEnabled()) {
            String msg = (params.length == 0) ? format : String.format(format, params);
            LOG.trace(stclContext, msg);
            return msg;
        }
        return "";
    }

}