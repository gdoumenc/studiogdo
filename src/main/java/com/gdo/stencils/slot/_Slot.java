/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.slot;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.gdo.helper.ClassHelper;
import com.gdo.helper.ConverterHelper;
import com.gdo.helper.StringHelper;
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
    private Annotation _annoted; // a slot may be annoted (error, missing, ..)

    private int _completionLevel; // accept plug in this slot only if completion

    // level of plug is lower
    // else means slot redefined and then plugs are no more visible

    protected _Slot(C stclContext, _Stencil<C, S> container, String name, char arity, boolean tranzient, boolean override) {

        // does nothing if the slot is hidden
        String[] discardedSlots = container.discardedSlots(stclContext);
        if (ClassHelper.contains(discardedSlots, name)) {
            return;
        }

        // rename it if needed
        String n = name;
        Map<String, String> renamedSlots = container.renamedSlots(stclContext);
        if (renamedSlots != null) {
            String renamed = renamedSlots.get(name);
            if (renamed != null) {
                n = renamed;
            }
        }

        // verifie unique
        _Slot<C, S> slot = container.getSlots().get(name);
        if (slot != null && !override)
            logWarn(stclContext, "slot %s is already defined in %s (will be redefined...)", name, container);

        // set characteristics
        _container = container;
        _name = n;
        _arity = arity;
        _tranzient = tranzient;

        // store structure
        _container.addSlot(stclContext, this);
    }

    public void setTransient() {
        _tranzient = true;
    }

    /**
     * Clears all internal structures to free memory.
     */
    public void clear() {
        _desc = null;
        _container = null;
        _name = null;
        _annoted = null;
    }

    public void expunge(C stclContext, PSlot<C, S> self) {
    }

    /**
     * Returns the name of the slot.
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
     * @return the slot's arity.
     */
    public char getArity(C stclContext, PSlot<C, S> self) {
        return _arity;
    }

    /**
     * @return <tt>true</tt> if the stencils plugged in the slot must not be
     *         saved in stencil configuration.
     */
    public boolean isTransient(C stclContext) {
        return _tranzient;
    }

    // slot may have parameters to defined them (search in another slot first
    // for example)
    @SuppressWarnings("unchecked")
    public <T> T getParameter(C stclContext, int index) {
        if (getDescriptor() == null) {
            throw new NullPointerException("parameters are defined for slot only if a descriptor is associated");
        }
        return (T) getDescriptor().getParameter(stclContext, index);
    }

    /**
     * @return <tt>true</tt> if the slot is redefined locally . Not same as the
     *         one defined on template descriptors.
     */
    public boolean isRedefined(C stclContext) {
        SlotDescriptor<C, S> slotDesc = getDescriptor();
        return (slotDesc != null && slotDesc.isRedefined());
    }

    /**
     * Checks if a slot is a slot cursor.
     * 
     * @param stclContext
     *            the stencil context.
     * @return <tt>true</t> if the slot is a cursor slot.
     */
    public boolean isCursorBased(C stclContext) {
        return false;
    }

    public String getProperty(C stclContext, IKey key, String name, PSlot<C, S> self) {
        return null;
    }

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

    /*
     * Annotation par (not used for now)
     */
    public final Annotation getAnnotation(C stclContext, String type, PSlot<C, S> self) {
        return _annoted;
    }

    public final void setAnnotation(C stclContext, String type, Annotation annotation, PSlot<C, S> self) {
        _annoted = annotation;
    }

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
    protected String[] getSlotsProto(C stclContext) {
        SlotDescriptor<C, S> slotDesc = getDescriptor();
        if (slotDesc != null && slotDesc.getProto() != null) {
            String list = slotDesc.getProto().getSlots();
            return StringHelper.splitShortString(list, PathUtils.MULTI);
        }
        return StringHelper.EMPTY_STRINGS;
    }

    /**
     * @return the prop names prototype signature. Should be redefined in each
     *         specific inner declaration. By default, no signature means any
     *         kind of stencil is accepted.
     */
    protected String[] getPropsProto(C stclContext) {
        SlotDescriptor<C, S> slotDesc = getDescriptor();
        if (slotDesc != null && slotDesc.getProto() != null) {
            String list = slotDesc.getProto().getProps();
            return StringHelper.splitShortString(list, PathUtils.MULTI);
        }
        return StringHelper.EMPTY_STRINGS;
    }

    /**
     * @return the prop names prototype signature. Should be redefined in each
     *         specific inner declaration. By default, no signature means any
     *         kind of stencil is accepted.
     */
    protected String[] getCommandsProto(C stclContext) {
        SlotDescriptor<C, S> slotDesc = getDescriptor();
        if (slotDesc != null && slotDesc.getProto() != null) {
            String list = slotDesc.getProto().getCommands();
            return StringHelper.splitShortString(list, PathUtils.MULTI);
        }
        return StringHelper.EMPTY_STRINGS;
    }

    /**
     * Method called before the plug order will be executed for this slot and if
     * it returns <tt>false</tt> then the order is not executed. Default method
     * verifies prototypes if defined.
     * 
     * @param stclContext
     *            the stencil context.
     * @param order
     *            the plig order.
     * @param self
     *            this stencil as a plugged stencil.
     * @return
     */
    public Result beforePlug(C stclContext, S stencil, IKey key, PSlot<C, S> self) {

        // the stencil must be defined in the plug order
        if (StencilUtils.isNull(stencil)) {
            String msg = logWarn(stclContext, "no stencil defined when plugging in %s", self);
            return Result.error(getClass().getName(), 1, msg);
        }

        // production optimization
        if (!_Stencil.STRICT_MODE) {
            return Result.success();
        }

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

            // test props prototype
            for (String prop : getPropsProto(stclContext)) {
                if (!stencil.getStencil(stclContext, prop).isProp(stclContext)) {
                    String msg = logWarn(stclContext, "Should not plug %s in %s : this stencil plugged doesn't have the prop %s", stencil, self, prop);
                    result = Result.warn(getClass().getName(), 4, msg, result);
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
        return Result.success(getClass().getName(), 0, null, result);
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
        return Result.success(getClass().getName());
    }

    public Result beforeUnplugAll(C stclContext, PSlot<C, S> self) {
        return Result.success(getClass().getName());
    }

    /**
     * This method is called after an unplug order is executed.
     * 
     * @param stclContext
     *            the stenicl context.
     * @param stencil
     *            the stencil unplugged.
     * @param self
     *            this slot as a plugged slot.
     */
    public Result afterUnplug(C stclContext, S stencil, PSlot<C, S> self) {
        return Result.success(getClass().getName());
    }

    /**
     * Internal use only.
     */
    public final S plug(C stclContext, S stencil, IKey key, PSlot<C, S> self) {

        // verifies the plug is enabled
        try {
            Result before = beforePlug(stclContext, stencil, key, self);
            if (!before.isSuccess()) {
                return StencilUtils.<C, S> nullPStencil(stclContext, before);
            }
        } catch (Exception e) {
            String msg = logError(stclContext, "Exception in beforePlug method on %s in %s: %s", stencil, self, e);
            return StencilUtils.<C, S> nullPStencil(stclContext, Result.error(msg));
        }

        try {

            // performs the real plug
            S plugged = doPlug(stclContext, stencil, key, self);
            if (StencilUtils.isNotNull(plugged)) {
                logTrace(stclContext, "Plug %s in %s", plugged, self);
                plugged.addThisReferenceToStencil(stclContext);
            }

            afterPlug(stclContext, plugged, self);

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

        // verify the unplug is enabled
        Result before = null;
        try {
            before = beforeUnplug(stclContext, stencil, key, self);
            if (before.isNotSuccess())
                return;
        } catch (Exception e) {
            logError(stclContext, "Exception in before unplug method on %s in %s: %s", stencil, self, e);
            return;
        }

        try {

            // perform the real unplug
            doUnplug(stclContext, stencil, key, self);
            stencil.removeThisReferenceFromStencil(stclContext);

            // trace
            logTrace(stclContext, "Unplug %s from %s", stencil, self);

            // performs extra jobs after unplugging
            try {
                afterUnplug(stclContext, stencil, self);
            } catch (Exception e) {
                logError(stclContext, "Exception in after unplug method on %s in %s: %s", stencil, self, e);
            }
        } catch (Exception e) {
            logError(stclContext, "Exception in unplug method on %s in %s: %s", stencil, self, e);
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
     * @return the list of stencils unplugged.
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

    /**
     * Declared public to be accessed by stencil.
     */
    public void savePlugs(C stclContext, XmlWriter declPart, XmlWriter plugPart, PSlot<C, S> self) throws IOException {

        // does nothing on transient slot
        if (isTransient(stclContext)) {
            return;
        }

        // save plugs
        StencilIterator<C, S> stencils = getStencilsToSave(stclContext, self);
        for (S plugged : stencils) {

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
        IKey key = (linkDesc.getKey() != null) ? new Key<String>(linkDesc.getKey()) : Key.NO_KEY;
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
        S link = stencil.newPStencil(stclContext, slot, Key.NO_KEY, LinkStcl.class.getName(), path);
        link.setTransient(stclContext, true);
    }

    @Override
    public String toString() {
        try {
            PSlot<C, S> self = new PSlot<C, S>(this, null);
            StringBuffer str = new StringBuffer(getName(null));
            str.append('{').append(getArity(null, self)).append('}');
            return str.toString();
        } catch (Exception e) {
            return getName(null);
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