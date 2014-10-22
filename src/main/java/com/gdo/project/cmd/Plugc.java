/*
 * Copyright GDO - 2004
 */
package com.gdo.project.cmd;

import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.Base64;

import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.Keywords;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.IKeyGenerator;
import com.gdo.stencils.key.IntKeyGenerator;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.key.StringKeyGenerator;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;

/**
 * The plug command removes the command target from :
 * <ul>
 * <li>the stencil to plug plugged as first parameter ,
 * <li>specific slots which paths are defined as second parameter separated by
 * ",",
 * <ul>
 */
public class Plugc extends AtomicActionStcl {

	// parameters defined in context
	protected String _from; // slot where the stencil was
	protected Iterator<PSlot<StclContext, PStcl>> _slots; // slots where the
	// stencil will be
	// plugged
	protected String _keyType; // key type when plugging
	protected String _key; // key for plugging

	public Plugc(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();

		// performs the plug action
		int stencilsCounter = 0;
		int slotsCounter = 0;
		CommandStatus<StclContext, PStcl> status = success(cmdContext, self);
		while (_slots.hasNext()) {
			PSlot<StclContext, PStcl> slot = _slots.next();
			StencilIterator<StclContext, PStcl> toBePlugged = self.getStencils(stclContext, _from);

			// increments counter
			if (toBePlugged.size() > 0) {
				slotsCounter++;
			}

			// performs plug on all stencil
			for (PStcl stencil : toBePlugged) {
				PStcl plugged = plugStencil(cmdContext, stencil, slot, _keyType, self);

				// was not able to plug
				if (StencilUtils.isNull(plugged)) {
					status.addOther(warn(cmdContext, self, plugged.getNullReason()));
				} else {
					stencilsCounter++;
				}

				// should unplug the source
				boolean unplug = getExpandedParameter(cmdContext, 5, false, self);
				if (unplug) {
					self.unplugOtherStencilFrom(stclContext, _from, stencil);
				}
			}
		}

		// all was ok
		status.addOther(success(cmdContext, self, stencilsCounter));
		status.addOther(success(cmdContext, self, slotsCounter));
		return status;
	}

	@Override
	protected CommandStatus<StclContext, PStcl> verifyContext(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();

		// verifies the stencil to be plugged
		String from = getParameter(cmdContext, 1, Slot.TARGET);
		if (StringUtils.isEmpty(from)) {
			return error(cmdContext, self, "No path (param1)");
		}
		Base64 base = new Base64();
		_from = new String(base.decode(from.getBytes()));

		// verify the slot and key where it should be plugged
		String to = getParameter(cmdContext, 2, null);
		if (StringUtils.isEmpty(to)) {
			return error(cmdContext, self, "No destination path (param2)");
		}
		to = new String(base.decode(to.getBytes()));
		_slots = StencilUtils.getSlots(stclContext, self, to);
		if (!_slots.hasNext()) {
			String msg = String.format("Cannot get destination slot at path %s (param2) from %s", to, self);
			return error(cmdContext, self, msg);
		}

		// verify key type
		_keyType = getParameter(cmdContext, 3, Keywords.NONE);
		if (!(_keyType.equals(Keywords.NONE) || _keyType.equals(Keywords.FIXED) || _keyType.equals(Keywords.STRING) || _keyType.equals(Keywords.INT))) {
			String msg = String.format("Wrong key type %s (param3 should be %s, %s, %s or %s)", _keyType, Keywords.NONE, Keywords.FIXED, Keywords.STRING, Keywords.INT);
			return error(cmdContext, self, msg);
		}

		// get key
		if (_keyType.equals(Keywords.FIXED)) {
			_key = getExpandedParameter(cmdContext, 4, "a", self);
		} else if (_keyType.equals(Keywords.STRING)) {
			_key = getExpandedParameter(cmdContext, 4, "a", self);
		} else if (_keyType.equals(Keywords.INT)) {
			_key = getExpandedParameter(cmdContext, 4, "1", self);
		}
		return success(cmdContext, self);
	}

	protected PStcl plugStencil(CommandContext<StclContext, PStcl> cmdContext, PStcl plugged, PSlot<StclContext, PStcl> slot, String keyType, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();

		// do the plug
		if (keyType.equals(Keywords.NONE)) {
			return self.plug(stclContext, plugged, slot, Key.NO_KEY);
		}
		if (keyType.equals(Keywords.FIXED)) {
			// unplug previous stencil if already there.
			String path = PathUtils.createPath(slot.pwd(stclContext), _key);
			PStcl stcl = self.getStencil(stclContext, path);
			if (stcl.isNotNull()) {
				stcl.unplugFrom(stclContext, path);
			}
			return self.plug(stclContext, plugged, slot, _key);
		}
		if (keyType.equals(Keywords.STRING)) {
			IKeyGenerator keyGen = new StringKeyGenerator<StclContext, PStcl>(stclContext, _key, slot);
			return slot.plug(stclContext, plugged, keyGen.getKey());
		}
		if (keyType.equals(Keywords.INT)) {
			IKeyGenerator keyGen = new IntKeyGenerator<StclContext, PStcl>(stclContext, Integer.parseInt(_key), slot);
			return slot.plug(stclContext, plugged, keyGen.getKey());
		}

		// should not go there
		return null;
	}

}