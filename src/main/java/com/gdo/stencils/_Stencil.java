/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.StringUtils;

import com.gdo.helper.StringHelper;
import com.gdo.stencils.atom.Atom;
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
import com.gdo.stencils.faces.RenderContext;
import com.gdo.stencils.facet.FacetResult;
import com.gdo.stencils.factory.InterpretedStencilFactory;
import com.gdo.stencils.factory.StencilFactory;
import com.gdo.stencils.interpreted.SlotDescriptor;
import com.gdo.stencils.interpreted.TemplateDescriptor;
import com.gdo.stencils.iterator.ListIterator;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.log.StencilLog;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.prop.IPPropStencil;
import com.gdo.stencils.slot.CalculatedStringPropertySlot;
import com.gdo.stencils.slot.MultiCalculatedSlot;
import com.gdo.stencils.slot.MultiSlot;
import com.gdo.stencils.slot.SingleCalculatedSlot;
import com.gdo.stencils.slot._Slot;
import com.gdo.stencils.util.ClassUtils;
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
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo & Guillaume Doumenc. Use
 * is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */
public abstract class _Stencil<C extends _StencilContext, S extends _PStencil<C, S>> extends Atom<C, S> {

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

		String $LOCKED = "$Locked";
		String DESCRIPTION = "$Description";
	}

	/**
	 * Interface to define command names as constants.
	 */
	public interface Command {
		// no command defined
	}

	// java attributes

	/**
	 * Set to <tt>true</tt> when this stencil was cleared (so should not be
	 * referenced anymore). Defined to warn when cleared stencil are used.
	 */
	private boolean cleared = false;

	// associated default plugged stencil for interface manipulation
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
	 * List of plugged references to this stencil (containing slot + key)
	 * As the stencil can be plugged in a same slot with different key, the list is S composed.
	 */
	private List<S> _plugged_references = new ArrayList<S>();

	/**
	 * Internal slot when commands are defined.
	 */
	private MultiSlot<C, S> _commandSlot;

	// writer used for saving this stencil
	// used to know if the stencil has been already stored in the current writer
	// when saving (each saving is done in a specific writer)
	protected XmlWriter _writer;

	/**
	 * Should be used only internally by digester.
	 */
	public _Stencil(C stclContext) {

		// internal slot (used temporary to retrieve command stencil)
		this._commandSlot = new MultiSlot<C, S>(stclContext, this, ".Commands", PSlot.ANY, true, false);

		// predefined slots
        addDescriptor(Slot.THIS, new _SlotDescriptor<C, S>() {
            @Override
            public _Slot<C, S> add(C stclContext, String name, S self) {
                return new ThisSlot(stclContext, _Stencil.this, name);
            }
        });
		createParentSlot(stclContext);
		createRootSlot(stclContext);

		singleSlot(Slot.$LOCKED);

		createNameSlot(stclContext);
		createDescriptionSlot(stclContext);
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
	 *          the stencil context.
	 * 
	 *          TODO should replace beforeLastUnplug.
	 * @throws Exception
	 */
	public void beforeClear(C stclContext, S self) {
	}

	/**
	 * Clears all internal structures to free memory.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 */
	public void clear(C stclContext) {

		// checks not already cleared
		if (this.cleared) {
			logError(stclContext, "A stencil should not be cleared twice");
		}
		this.cleared = true;

		// slots part
		if (this._slots != null) {
			for (_Slot<C, S> slot : this._slots.values()) {
				slot.clear();
			}
			this._slots.clear();
			this._slots = null;
		}

		// TODO : should not be cleared as if the command clears the stencil get
		// error
		// for (CommandStencil<C, S> cmd : this._commands.values()) {
		// cmd.clear(stclContext);
		// }
		// commands part
		if (this._commandSlot != null) {
			this._commandSlot.clear();
			this._commandSlot = null;
		}

		// plugged references part
		if (this._plugged_references != null) {
			this._plugged_references.clear();
			this._plugged_references = null;
		}

		this._self = null;
		this._desc = null;
	}

	/**
	 * Checks if a stencil is available : when a stencil is cleared, it should be
	 * not be used any more.
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
		return this._desc;
	}

	/**
	 * Sets the descriptor which has used to create the stencil. TODO: In deployed
	 * mode, the descriptor should not be defined for memory optimization.
	 * 
	 * @param desc
	 *          the template descriptor.
	 */
	public final void setDescriptor(TemplateDescriptor<C, S> desc) {
		this._desc = desc;
		this._template_name = desc.getTemplateName();
	}

	/**
	 * Gets the transient status of the stencil.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param self
	 *          the stencil as a plugged stencil.
	 * @return <tt>true</tt> if the stencil is transient (should not be stored).
	 */
	public boolean isTransient(C stclContext, S self) {
		return this._transient;
	}

	public boolean isLink(C stclContext, S self) {
		return false;
	}

	/**
	 * Sets the transient status of the stencil.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param value
	 *          the transient value.
	 * @param self
	 *          the stencil as a plugged stencil.
	 */
	public void setTransient(C stclContext, boolean value, S self) {
		this._transient = value;
	}

	/**
	 * Gets the java name value (not default one is not defined).
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param self
	 *          the stencil as a plugged stencil.
	 * @return the stencil property name (even if empty).
	 */
	public String getJavaName(C stclContext, S self) {
		return this._name;
	}

	/**
	 * Gets the name value (never empty, using template name if empty).
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param self
	 *          the stencil as a plugged stencil.
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
	 *          the stencil context.
	 * @param name
	 *          the new name.
	 * @param self
	 *          the stencil as a plugged stencil.
	 * @return the stencil name set in stencil.
	 */
	public String setName(C stclContext, String name, S self) {
		this._name = name;
		return name;
	}

	/**
	 * Gets the comment value.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param self
	 *          the stencil as a plugged stencil.
	 * @return the comment (never empty).
	 */
	public String getComment(C stclContext, S self) {
		return this._comment;
	}

	/**
	 * Sets the comment.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param comment
	 *          the new comment.
	 * @param self
	 *          the stencil as a plugged stencil.
	 */
	public void setComment(C stclContext, String comment, S self) {
		this._comment = comment;
	}

	/**
	 * Gets the template name.
	 * 
	 * @return stencil template name (stencil class name if no descriptor).
	 */
	public String getTemplateName() {
		if (StringUtils.isNotBlank(this._template_name)) {
			return this._template_name;
		}
		return getClass().getName();
	}

	/**
	 * Creates a new plugged stencil from a template class name and parameters and
	 * performing the real plug operation.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param slot
	 *          the slot where the created stencil is plugged.
	 * @param key
	 *          the key for plugging.
	 * @param stencilClassName
	 *          the stencil class name.
	 * @param self
	 *          the stencil as a plugged stencil.
	 * @param params
	 *          the stencil constructor parameters.
	 * @return a new plugged stencil.
	 */
	@SuppressWarnings("unchecked")
	public S newPStencil(C stclContext, PSlot<C, S> slot, IKey key, Class<? extends _Stencil<C, ? extends S>> clazz, S self, Object... params) {

		// creates the stencil (without plugging it)
		StencilFactory<C, S> factory = (StencilFactory<C, S>) stclContext.<C, S> getStencilFactory();
		S stcl = factory.createPStencil(stclContext, null, key, (Class<? extends _Stencil<C, S>>) clazz, params);
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
	 *          the stencil context.
	 * @param slot
	 *          the slot where the created stencil is plugged.
	 * @param key
	 *          the key for plugging.
	 * @param value
	 *          the initial property value.
	 * @param self
	 *          the stencil as a plugged stencil.
	 * @param params
	 *          the stencil constructor parameters.
	 * @return a new plugged property.
	 */
	@SuppressWarnings("unchecked")
	public <V, P extends IPPropStencil<C, S>> P newPProperty(C stclContext, PSlot<C, S> slot, IKey key, V value, S self, Object... params) {

		// creates the property (without plugging it)
		StencilFactory<C, S> factory = (StencilFactory<C, S>) stclContext.<C, S> getStencilFactory();
		S prop = factory.createPProperty(stclContext, slot, Key.NO_KEY, value, params);
		if (StencilUtils.isNull((S) prop)) {
			logWarn(stclContext, "cannot create property stencil for value %s", value);
			return null;
		}

		// plugs it in the slot (if defined)
		if (SlotUtils.isNull(slot)) {
			return (P) prop;
		}
		return (P) self.plug(stclContext, (S) prop, slot, key);
	}

	/**
	 * Completes stencil with declared slots and plugs.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param self
	 *          this stencils as a plugged stencil.
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
	 *          the stencil context.
	 * @param self
	 *          the stencil as a plugged stencil.
	 */
	public void beforeCompleted(C stclContext, S self) {
		// nothing by default
	}

	/**
	 * Overridable method for actions to be performed on stencil creation.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param self
	 *          the stencil as a plugged stencil.
	 */
	public void afterCompleted(C stclContext, S self) {
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
     *          the stencil context.
     * @param self
     *          the stencil as a plugged stencil.
     * @return the verification performed result.
     */
    public Result verify(C stclContext, S self) {
        return Result.success();        
    }
    
	/**
	 * Perform actions which must be done after a set/mset RPC call.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param self
	 *          the stencil as a plugged stencil.
	 * @return the action performed result.
	 */
	public Result afterRPCSet(C stclContext, S self) {
	    return verify(stclContext, self);
	}
	
	/**
	 * Called when the stencil is unplugged from a slot.
	 */
	@Deprecated
	public void afterUnplug(C stclContext, PSlot<C, S> from, IKey key, S self) {
	}

	/**
	 * Deprecated : Use beforeClear
	 */
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
	 *          the stencil context.
	 * @param key
	 *          the plug key for the clone.
	 * @param self
	 *          this stencil as a plugged stencil
	 * @return the stencil cloned
	 */
	public S clone(C stclContext, PSlot<C, S> slot, IKey key, S self) throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	/**
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
	 * Gets the map of defined slots. This method should be used only for internal
	 * implementation.
	 * 
	 * @return The slot map (may be empty but never <tt>null</tt>).
	 */
	public final Map<String, _Slot<C, S>> getSlots() {
		return this._slots;
	}

	/**
	 * Adds a local slot. This method should be used only for internal
	 * implementation.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param slot
	 *          the slot added.
	 */
	public void addSlot(C stclContext, _Slot<C, S> slot) {

		// checks the slot not already defined
		String slotName = slot.getName(stclContext);
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
	 *          the stencil context.
	 * @param name
	 *          the slot's name.
	 */
	public _Slot<C, S> removeSlot(C stclContext, String name) {
		return getSlots().remove(name);
	}

	/**
	 * Defines the list of slots which should not be created. This allow stencil
	 * implementation to override slots from super implementation. A discarded
	 * slot must be redefined or the behavior may be unprevisisble.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @return the array of discarded slots (those slot won't be created even if
	 *         defined in super).
	 */
	public String[] discardedSlots(C stclContext) {
		return StringHelper.EMPTY_STRINGS;
	}

	/**
	 * Defines the list of slots which should be renamed. This allow stencil
	 * implementation to override slots from super implementation.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @return the map of discarded slots (key is previous name).
	 */
	public Map<String, String> renamedSlots(C stclContext) {
		return null;
	}

	// TODO should be defined in plugged stencil
	/*
	 * public Map<String, String> renamedSlots(C stclContext) { return
	 * StringHelper.EMPTY_STRINGS_MAP; }
	 */

	// TODO should be defined in plugged stencil
	/*
	 * @return the array of hidden slots (those slot may be created in super but
	 * are no more visible). public String[] hiddenSlots(C stclContext) { return
	 * StringHelper.EMPTY_STRINGS; }
	 */

	//
	// Informations relative to containing slots (to understand where the
	// stencil is..)
	//

	//
	// Informations relative to contained slots
	//

	/**
	 * Checks if a slot is defined locally.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param slotName
	 *          the slot's name.
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
	 *          the stencil context.
	 * @param slotName
	 *          the slot's name (should be not composed).
	 * @param self
	 *          the stencil as a plugged stencil.
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
	/**
	 * should used get slot and test if null
	 * @return <tt>true</tt> is the slot defined by the name exists.
	 */
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
	 *          the stencil context.
	 * @param slotPath
	 *          the path to the slot.
	 * @param self
	 *          the stencil as a plugged stencil.
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
	 * @return the number of stencils in path.
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
	 * Defines the default property when this stencil is considered as a property.
	 * Default is <tt>Name</tt>
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param self
	 *          the stencil the stencil as a plugged stencil.
	 * @return the slot path to the default property.
	 */
	public String defaultProperty(C stclContext, S self) {
		return _Stencil.Slot.NAME;
	}

	/**
	 * Returns the stencil as a property (default property is Name).
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param self
	 *          the stencil as a plugged stencil.
	 * @return the property interface (default property if not a property).
	 */
	public IPPropStencil<C, S> asProp(C stclContext, S self) {
		return getStencil(stclContext, defaultProperty(stclContext, self), self);
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
	 * @return the first stencil in path. May be redefined for efficiency reason.
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
							last.setKey(new Key<String>(stcl.getUId(stclContext)));
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
	 *          the stencil context.
	 * @param attributes
	 *          the attributes path.
	 * @param self
	 *          this stencil as a plugged stencil.
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
	 * Returns the list of plugged slots (slot + key) referencing this stencil.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @return the list of plugged references.
	 */
	public List<S> getPluggedReferences(C stclContext) {
		return this._plugged_references;
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
	 * return plug(stclContext, stcl, slotPath, new Key<String>(key), self); }
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
		return new PSlot<C, S>(this._commandSlot, container);
	}

	/*
	 * public final Map<String, CommandStencil<C, S>> getCommands() { return
	 * this._commands; }
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

	/** @see{PStencil.getCommand **/
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
		S pcmd = factory.createPStencil(stclContext, getCommandsSlot(self), new Key<String>(name), clazz);

		// adds paramters list
		((CommandStencil<C, S>) pcmd.getReleasedStencil(stclContext))._defParams = desc.getDefaultParams();

		// returns command created
		return pcmd;
	}

	/**
	 * @throws Exception
	 * @see{PStencil.call
	 **/
	public CommandStatus<C, S> call(C stclContext, String name, S self, Object... params) {

		// creates a command context with parameters
		CommandContext<C, S> cmdContext = new CommandContext<C, S>(stclContext, self);
		for (int i = 0; i < params.length; i++) {
			cmdContext.setRedefinedParameter(CommandContext.PARAMS[i], params[i]);
		}

		// execute the command
		return call(cmdContext, name, self);
	}

	/**
	 * @throws Exception
	 * @see{PStencil.call
	 **/
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
	 * @return <tt>true</tt> if there is a property in path. May be redefined for
	 *         efficiency reason.
	 */
	/*
	 * public boolean hasProperty(C stclContext, String path, S self) { if
	 * (!hasSlot(stclContext, PathUtils.getSlotPath(path), self)) return false;
	 * IPPropStencil<C, S> prop = getPropertyStencil(stclContext, path, self);
	 * return (prop != null); }
	 */

	public <V> IPPropStencil<C, S> getPropertyStencil(C stclContext, String path, S self) {
		try {
			S prop = self.getStencil(stclContext, path);
			if (StencilUtils.isNotNull(prop))
				// if (StencilUtils.isNotNull(prop) && prop.isProp(stclContext))
				// {
				return (IPPropStencil<C, S>) prop.asProp(stclContext);
			/*
			 * } String msg =
			 * String.format("the stencil at path %s from %s is not a property", path,
			 * self); StencilFactory<C, S> factory = (StencilFactory<C, S>)
			 * stclContext.<C, S> getStencilFactory(); IPPropStencil<C, S> p =
			 * (IPPropStencil<C, S>) factory.newPProperty(stclContext, null,
			 * Key.NO_KEY, msg); return p;
			 */
			return (IPPropStencil<C, S>) prop;
		} catch (WrongPathException e) {
			S stcl = StencilUtils.<C, S> nullPStencil(stclContext, Result.error(e));
			return (IPPropStencil<C, S>) stcl;
		}
	}

	//
	// Wrappers for facet
	//

	public FacetResult getFacet(RenderContext<C, S> renderContext) {
		String facet = renderContext.getFacetType();
		String mode = renderContext.getFacetMode();
		String msg = String.format("No facet found for %s for type %s in mode %s", this, facet, mode);
		return new FacetResult(FacetResult.ERROR, msg, null);
	}

	public InputStream getResourceAsStream(C stclContext, String path, S self) {
		return ClassUtils.getResourceAsStream(path, stclContext.getLocale());
	}

	public void multipart(StclContext stclContext, String fileName, FileItem item, PStcl self) throws Exception {
	}

	/**
	 * Saves the stencil as XML description in a file.
	 * 
	 * @param stclContext
	 *          stencil context used.
	 * @param out
	 *          XML writer used.
	 * @param self
	 *          self as plugged stencil.
	 * @throws IOException
	 */
	public void saveAsStencil(C stclContext, XmlWriter out, S self) throws IOException {

		// if already saved in this document does nothing
		if (this._writer == stclContext.getSaveWriter()) {
			return;
		}
		this._writer = stclContext.getSaveWriter();

		// writer header
		out.writeHeader();
		out.write("<!DOCTYPE stencil PUBLIC \"-//StudioGdo//DTD Stencil 1.0//EN\" \"stencil.dtd\">\n");

		// opens stencil tag
		out.startElement(InterpretedStencilFactory.STENCIL);
		out.writeAttribute("id", getUId(stclContext));
		if (StringUtils.isNotEmpty(this._name)) {
			out.writeAttribute("name", this._name);
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
	 *          the stencil context.
	 * @param dir
	 *          the container path (in instance repository).
	 * @param container
	 *          the writerused to save the stencil (to avoid writting twice).
	 * @param self
	 *          the stencil as plugged stencil.
	 * @returnthe reference id used to retrieve the instance declaration in the
	 *            file.
	 */
	public String saveAsInstance(C stclContext, String dir, XmlWriter out, S self) {
		try {

			// composed id
			String id = self.getUId(stclContext);
			String instance = PathUtils.compose(dir, id);

			// if already saved in this document does nothing
			if (this._writer == stclContext.getSaveWriter()) {
				return instance;
			}
			this._writer = stclContext.getSaveWriter();

			// adds other instances declared
			XmlStringWriter declPart = new XmlStringWriter(false, 1, out.getEncoding());
			XmlStringWriter plugPart = new XmlStringWriter(false, 2, out.getEncoding());
			saveSlots(stclContext, declPart, plugPart, self);
			out.write(declPart.getString());
			declPart.close();

			// starts instance tag
			out.startElement(InterpretedStencilFactory.INSTANCE);
			out.writeAttribute("id", id);
			if (StringUtils.isNotBlank(this._name)) {
				out.writeAttribute("name", this._name);
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
	 */
	protected void saveConstructorParameters(C stclContext, XmlWriter writer, S self) {
	}

	/**
	 * Saves the slot and its content.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param descPart
	 *          the description part.
	 * @param plugPart
	 *          the plug part.
	 * @param self
	 *          this stencil as a plugged stencil.
	 */
	protected void saveSlots(C stclContext, XmlWriter descPart, XmlWriter plugPart, S self) throws IOException {
		PSlot<C, S> pslot = null;

		// saves all slots content
		for (_Slot<C, S> slot : getSlots().values()) {

			// the slot as a plugged slot
			if (pslot == null) {
				pslot = new PSlot<C, S>(slot, self);
			} else {
				pslot.setSlot(slot);
			}

			// if the slot is redefined locally, then saves the local description
			if (slot.isRedefined(stclContext)) {

				// the local slot declaration is done in plug part
				SlotDescriptor<C, S> slotDesc = slot.getDescriptor();
				slotDesc.save(stclContext, plugPart, null);
			}

			// saves all the plugs in the the slot
			slot.savePlugs(stclContext, descPart, plugPart, pslot);
		}
	}

	@Override
	public int compareTo(S obj) {
		return 0;
	}

	/**
	 * Returns this stencil as a plugged one (container only used for cloning)
	 * 
	 * @param stclContext
	 *          stencil context used.
	 * @param container
	 *          container stencil.
	 */
	@SuppressWarnings("unchecked")
	public S self(C stclContext, S container) {

		// stand alone plugged stencil
		if (StencilUtils.isNull(container)) {

			// if not already defined creates it
			if (StencilUtils.isNull(this._self)) {
				StencilFactory<C, S> factory = (StencilFactory<C, S>) stclContext.<C, S> getStencilFactory();
				this._self = factory.newPStencil(stclContext, (PSlot<C, S>) null, Key.NO_KEY, this);
			}
			return (S) this._self;
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
		StringBuffer name = new StringBuffer(super.toString());
		name.append('-').append(StringUtils.isEmpty(this._name) ? getUId(null) : this._name);
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
		return _slot_descs.put(name, desc);
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
		_SlotDescriptor<C, S> desc = new PropSlotDescriptor<C, S, Boolean>(initial);
		return addDescriptor(name, desc);
	}

	public _SlotDescriptor<C, S> propSlot(String name, int initial) {
		_SlotDescriptor<C, S> desc = new PropSlotDescriptor<C, S, Integer>(initial);
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
			this._clazz = clazz;
			this._params = params;
		}

		Map<String, Object> getDefaultParams() {
			Map<String, Object> map = new ConcurrentHashMap<String, Object>();
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
			_command_descs = new HashMap<String, CommandDescriptor>();
		}
		_command_descs.put(name, new CommandDescriptor(clazz, params));
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
