/*
 * Copyright GDO - 2004
 */
package com.gdo.project.cmd;

import org.apache.commons.lang3.StringUtils;

import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.Keywords;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.cond.PathCondition;
import com.gdo.stencils.factory.StclFactory;
import com.gdo.stencils.key.IKeyGenerator;
import com.gdo.stencils.key.IntKeyGenerator;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.key.StringKeyGenerator;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.GlobalCounter;
import com.gdo.stencils.util.SlotUtils;
import com.gdo.stencils.util.StencilUtils;

/**
 * The trace command.<br>
 * Parameters are :
 * <ol>
 * <li>stencil created class (stencil template full name)</li>
 * <li>destination slot path (where the stencil created will be plugged in -
 * relative to action)</li>
 * <li>key type (none, fixed, unique, string, int - default : none)</li>
 * <li>initial key value (default 1 for int, "a" for string)</li>
 * <li>initial parameter for constructor if needed</li>
 * <ol>
 */
public class CreateAtomic extends AtomicActionStcl {

    public interface Status {
        int STENCIL_PATH = 0;
        int STENCIL_CREATED = 1;
        int KEY_USED = 2;

        int NO_CLASS_DEFINED = 1;
        int CANNOT_CREATE_STENCIL = 2;
        int CANNOT_GET_SLOT = 3;
        int WRONG_KEY_TYPE = 4;
        int NO_SLOT_DEFINED = 5;
        int CANNOT_PLUG_STENCIL = 6;
    }

    // parameters defined in context
    protected String _className;
    protected PSlot<StclContext, PStcl> _slot;
    protected String _keyType;
    protected String _key;

    public CreateAtomic(StclContext stclContext) {
        super(stclContext);
    }

    @Override
    public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();

        // calls before creation
        CommandStatus<StclContext, PStcl> before = beforeCreate(cmdContext, self);
        if (before.isNotSuccess()) {
            return before;
        }

        // creates the stencil
        PStcl created = createStencil(cmdContext, _className, self);
        if (StencilUtils.isNull(created)) {
            String msg = String.format("Cannot create stencil %s", _className);
            return error(cmdContext, self, Status.CANNOT_CREATE_STENCIL, msg);
        }

        // calls after creation
        CommandStatus<StclContext, PStcl> after = afterCreate(cmdContext, created, self);
        if (after.isNotSuccess()) {
            return after;
        }

        // calls before plug
        before = beforePlug(cmdContext, created, self);
        if (before.isNotSuccess()) {
            return before;
        }

        // plugs it

        PStcl plugged = plugCreatedStencil(cmdContext, created, _slot, _keyType, _key, self);
        if (StencilUtils.isNull(plugged)) {
            String msg = String.format("Cannot create stencil %s", _className);
            return error(cmdContext, self, Status.CANNOT_CREATE_STENCIL, msg, plugged.getResult());
        }

        // calls after plug
        after = afterPlug(cmdContext, plugged, self);
        if (after.isNotSuccess()) {
            return error(cmdContext, self, 0, after);
        }

        // returns success status complements
        CommandStatus<StclContext, PStcl> s = success(cmdContext, self, Status.KEY_USED, plugged.getKey());
        s = success(cmdContext, self, Status.STENCIL_CREATED, plugged, s);
        return success(cmdContext, self, Status.STENCIL_PATH, plugged.pwd(stclContext), s);
    }

    @Override
    protected CommandStatus<StclContext, PStcl> verifyContext(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {

        // verifies class name
        _className = getTemplate(cmdContext, self);
        if (StringUtils.isEmpty(_className))
            return error(cmdContext, self, Status.NO_CLASS_DEFINED, "No class name defined (param1)");

        // verifies destination slot
        _slot = getSlot(cmdContext, self);
        if (SlotUtils.isNull(_slot)) {
            String msg = String.format("No slot in %s to plug the created stencil %s", self, _className);
            return error(cmdContext, self, Status.CANNOT_GET_SLOT, msg);
        }

        // verifies key type
        _keyType = getType(cmdContext, self);
        if (_keyType == null
                || !(_keyType.equals(Keywords.NONE) || _keyType.equals(Keywords.FIXED) || _keyType.equals(Keywords.UNIQUE) || _keyType.equals(Keywords.STRING) || _keyType
                        .equals(Keywords.INT))) {
            String msg = String.format("Wrong key type %s (param3 should be %s, %s, %s, %s or %s)", _keyType, Keywords.NONE, Keywords.FIXED, Keywords.UNIQUE, Keywords.STRING, Keywords.INT);
            return error(cmdContext, self, Status.WRONG_KEY_TYPE, msg);
        }

        // gets key
        _key = getKey(cmdContext, self);

        // return success status
        return success(cmdContext, self);
    }

    /**
     * Creates the stencil from class name and parameters.
     * 
     * @param cmdContext
     *            the command context.
     * @param className
     *            the template class name.
     * @param self
     *            the command as a plugged stencil.
     * @return the stencil created as a plugged stencil (even if not already
     *         plugged..).
     */
    protected PStcl createStencil(CommandContext<StclContext, PStcl> cmdContext, String className, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();
        StclFactory factory = (StclFactory) stclContext.getStencilFactory();

        // an initial value can be used to construct the stencil
        String param = getValueForConstructor(cmdContext, self);
        if (param != null) {
            return factory.createPStencil(stclContext, _slot, Key.NO_KEY, className, param);
        }
        return factory.createPStencil(stclContext, _slot, Key.NO_KEY, className);
    }

    /**
     * May be overriden for specific bahaviour.
     * 
     * @param cmdContext
     *            the command context.
     * @param self
     *            the command as a plugged command.
     * @return
     */
    protected CommandStatus<StclContext, PStcl> beforeCreate(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        return success(cmdContext, self);
    }

    /**
     * May be overriden for specific bahaviour.
     * 
     * @param cmdContext
     *            the command context.
     * @param created
     *            the stencil created.
     * @param self
     *            the command as a plugged command.
     * @return
     */
    protected CommandStatus<StclContext, PStcl> afterCreate(CommandContext<StclContext, PStcl> cmdContext, PStcl created, PStcl self) {
        return success(cmdContext, self);
    }

    /**
     * May be overriden for specific bahaviour.
     * 
     * @param cmdContext
     *            the command context.
     * @param created
     *            the stencil created.
     * @param self
     *            the command as a plugged command.
     * @return
     */
    protected CommandStatus<StclContext, PStcl> beforePlug(CommandContext<StclContext, PStcl> cmdContext, PStcl created, PStcl self) {
        return success(cmdContext, self);
    }

    /**
     * May be overriden for specific bahaviour.
     * 
     * @param cmdContext
     *            the command context.
     * @param created
     *            the stencil created.
     * @param self
     *            the command as a plugged command.
     * @return
     */
    protected CommandStatus<StclContext, PStcl> afterPlug(CommandContext<StclContext, PStcl> cmdContext, PStcl created, PStcl self) {
        return success(cmdContext, self);
    }

    // /// TODO should be defined in an utils helper as been used every
    // where!!!!

    /**
     * Static function to be used by other stencil creation actions.
     * 
     * @param cmdContext
     *            the command context.
     * @param created
     *            the stencil created.
     * @param slot
     *            the slot where the stencil will be plugged.
     * @param keyType
     *            the key type (none, fixed, single, unique, string, int).
     * @param self
     *            teh command as a plugged stencil.
     * @return the created stencil plugged
     */
    public static PStcl plugCreatedStencil(CommandContext<StclContext, PStcl> cmdContext, PStcl created, PSlot<StclContext, PStcl> slot, String keyType, String initialKey, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();

        // do the plug (key defined in paramter 4)
        if (keyType.equals(Keywords.NONE)) {
            return self.plug(stclContext, created, slot, Key.NO_KEY);
        } else if (keyType.equals(Keywords.FIXED)) {
            if (StringUtils.isBlank(initialKey)) {
                initialKey = "a";
            }
            return self.plug(stclContext, created, slot, initialKey);
        } else if (keyType.equals(Keywords.SINGLE)) {
            if (StringUtils.isBlank(initialKey)) {
                initialKey = "a";
            }
            if (slot.hasStencils(stclContext, PathCondition.<StclContext, PStcl> newKeyCondition(stclContext, new Key(initialKey), self))) {
                return nullPStencil(stclContext, Result.error("A stencil is already plugged at this key"));
            }
            return self.plug(stclContext, created, slot, initialKey);
        } else if (keyType.equals(Keywords.UNIQUE)) {
            Integer unique = GlobalCounter.uniqueInt();
            return self.plug(stclContext, created, slot, unique);
        } else if (keyType.equals(Keywords.STRING)) {
            if (StringUtils.isBlank(initialKey)) {
                initialKey = "a";
            }
            IKeyGenerator keyGen = new StringKeyGenerator<StclContext, PStcl>(stclContext, initialKey, slot);
            return slot.plug(stclContext, created, keyGen.getKey());
        } else if (keyType.equals(Keywords.INT)) {
            if (StringUtils.isBlank(initialKey)) {
                initialKey = "1";
            }
            IKeyGenerator keyGen = new IntKeyGenerator<StclContext, PStcl>(stclContext, Integer.parseInt(initialKey), slot);
            return slot.plug(stclContext, created, keyGen.getKey());
        }

        // should not go there
        return nullPStencil(stclContext, Result.error("Should not goes there (type has been verified)"));
    }

    protected String getTemplate(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        return getParameter(cmdContext, 1, null);
    }

    protected PSlot<StclContext, PStcl> getSlot(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();
        String slot = getParameter(cmdContext, 2, null);
        return self.getSlot(stclContext, slot);
    }

    /**
     * Gets key type.
     * 
     * @param cmdContext
     * @param self
     * @return
     */
    protected String getType(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        return getParameter(cmdContext, 3, null);
    }

    protected String getKey(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        return getParameter(cmdContext, 4, null);
    }

    protected String getValueForConstructor(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        return getParameter(cmdContext, 5, null);
    }
}