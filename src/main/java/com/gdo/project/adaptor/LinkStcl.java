/**
 * Copyright GDO - 2005
 */
package com.gdo.project.adaptor;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.gdo.helper.StringHelper;
import com.gdo.project.cmd.Plug;
import com.gdo.project.cmd.Unplug;
import com.gdo.stencils.Result;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cond.AndCondition;
import com.gdo.stencils.cond.LinkCondition;
import com.gdo.stencils.cond.PathCondition;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.CalculatedStringPropertySlot;
import com.gdo.stencils.slot._Slot;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.SlotUtils;
import com.gdo.stencils.util.StencilUtils;
import com.gdo.util.XmlWriter;

/**
 * <p>
 * Stencil which emulates a delegated slot.
 * </p>
 * <p>
 * To retrieve the link stencil, a condition should be defined slot[$], will
 * return it.
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
public class LinkStcl extends Stcl implements ISlotEmulator<StclContext, PStcl> {

	public interface Slot extends Stcl.Slot {
		String PATH = "Path";
		String LOCAL = "Local";
		String IN = "In";
	}

	public interface Command extends Stcl.Command {
	}

	// initial path if defined by constructor
	protected String _path;

	public LinkStcl(StclContext stclContext) {
		this(stclContext, null);
	}

	public LinkStcl(StclContext stclContext, String path) {
		super(stclContext);
		this._path = path;

		// SLOT PART

		propSlot(Slot.PATH);
		propSlot(Slot.LOCAL, false);
		multiSlot(Slot.IN, PSlot.ANY, true, null);

		// COMMAND PART

		command(Command.PLUG, Plug.class);
		command(Command.UNPLUG, Unplug.class);
	}

	@Override
	protected _Slot<StclContext, PStcl> createPwdSlot(StclContext stclContext) {
		return new PwdSlot(stclContext, this, Slot.$PWD);
	}

	@Override
	public void complete(StclContext stclContext, PStcl self) {
		super.complete(stclContext, self);

		// sets property path
		if (StringUtils.isNotBlank(this._path)) {
			self.setString(stclContext, Slot.PATH, this._path);
		}
	}

	@Override
	public int size(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> slot, PStcl self) {
		PSlot<StclContext, PStcl> s = getSlot(stclContext, slot, self);
		if (!SlotUtils.isNull(s)) {
			return s.size(stclContext, addLinkPathCondition(stclContext, cond, self));
		}
		return -1;
	}

	@Override
	public boolean contains(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PStcl searched, PSlot<StclContext, PStcl> slot, PStcl self) {
		PSlot<StclContext, PStcl> s = getSlot(stclContext, slot, self);
		if (!SlotUtils.isNull(s))
			return s.contains(stclContext, addLinkPathCondition(stclContext, cond, self), searched);
		return false;
	}

	@Override
	public boolean hasStencils(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> slot, PStcl self) {
		PSlot<StclContext, PStcl> s = getSlot(stclContext, slot, self);
		if (!SlotUtils.isNull(s)) {
			return s.hasStencils(stclContext, addLinkPathCondition(stclContext, cond, self));
		}
		return false;
	}

	@Override
	public StencilIterator<StclContext, PStcl> getStencils(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> slot, PStcl self) {

		// if the condition is defined to retrieve the link itself
		if (LinkCondition.isWithLinksCondition(stclContext, self, cond)) {
			return StencilUtils.iterator(stclContext, self, slot);
		}

		// get stencils from slot
		return getStencils(stclContext, addLinkPathCondition(stclContext, cond, self), self);
	}

	@Override
	public boolean canChangeOrder(StclContext stclContext, PSlot<StclContext, PStcl> slot, PStcl self) {
		PSlot<StclContext, PStcl> s = getSlot(stclContext, slot, self);
		if (!SlotUtils.isNull(s))
			return s.canChangeOrder(stclContext);
		return false;
	}

	@Override
	public boolean isLink(StclContext stclContext, PStcl self) {
		return true;
	}

	@Override
	public boolean isFirst(StclContext stclContext, PStcl searched, PSlot<StclContext, PStcl> slot, PStcl self) {
		PSlot<StclContext, PStcl> s = getSlot(stclContext, slot, self);
		if (!SlotUtils.isNull(s))
			return s.isFirst(stclContext, searched);
		return false;
	}

	@Override
	public boolean isLast(StclContext stclContext, PStcl searched, PSlot<StclContext, PStcl> slot, PStcl self) {
		PSlot<StclContext, PStcl> s = getSlot(stclContext, slot, self);
		if (!SlotUtils.isNull(s))
			return s.isLast(stclContext, searched);
		return false;
	}

	/**
	 * Returns all stencils defined at link path.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param cond
	 *          the stencil condition.
	 * @param self
	 *          the stencil as a plugged stencil.
	 * @return the stencils list.
	 */
	private StencilIterator<StclContext, PStcl> getStencils(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PStcl self) {
		try {

			// gets link path
			String path = self.getString(stclContext, Slot.PATH, null);
			if (StringUtils.isBlank(path)) {
				String msg = logWarn(stclContext, "No path defined for the link %s", self);
				return StencilUtils.iterator(Result.error(msg));
			}

			// returns stencil list
			return self.getStencils(stclContext, path, cond);
		} catch (Exception e) {
			logWarn(stclContext, "exception in getStencils for link %s : %s", self, e);
		}
		return null;
	}

	/**
	 * @return either the slot to which the link points to, either the stencil
	 *         iterators if points to same slot
	 */
	private PSlot<StclContext, PStcl> getSlot(StclContext stclContext, PSlot<StclContext, PStcl> slot, PStcl self) {
		try {
			String path = self.getString(stclContext, Slot.PATH, null);
			if (StringUtils.isEmpty(path)) {
				logWarn(stclContext, "No path defined for the link %s", self);
				return null;
			}
			String slotPath = PathUtils.getSlotPath(path);
			PSlot<StclContext, PStcl> s = self.getSlot(stclContext, slotPath);

			// changes container for local link (s container is not this slot)
			if (self.getBoolean(stclContext, Slot.LOCAL, false)) {
				return new PSlot<StclContext, PStcl>(s.getSlot(), self.getContainer(stclContext));
			}

			// verifies circular path error
			if (s.equals(slot)) {
				logWarn(stclContext, "Circular path %s defined for the link %s", path, self);
				return null;
			}
			return s;
		} catch (Exception e) {
			logWarn(stclContext, e.toString());
		}
		return null;
	}

	/**
	 * If the link path contains a condition, then add it to the main condition.
	 */
	private StencilCondition<StclContext, PStcl> addLinkPathCondition(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PStcl self) {
		String path = self.getString(stclContext, Slot.PATH, null);

		// if the link path contains a path condition, add it to the main
		// condition
		if (!StringUtils.isEmpty(path)) {
			if (PathUtils.isKeyContained(path) || PathUtils.isExpContained(path)) {
				path = PathUtils.getLastName(path);
				StencilCondition<StclContext, PStcl> c = new PathCondition<StclContext, PStcl>(stclContext, path, self);
				if (cond == null) {
					return c;
				}
				return new AndCondition<StclContext, PStcl>(c, cond);
			}
		}

		// else stay same
		return cond;
	}

	@Override
	protected void saveConstructorParameters(StclContext stclContext, XmlWriter writer, PStcl self) {
		try {
			writer.startElement("param");
			writer.writeAttribute("index", 0);
			writer.writeAttribute("type", "string");

			// never expand path when saving
			if (StringUtils.isNotBlank(this._path)) {
				writer.writeCDATAAndEndElement(this._path);
			} else {
				writer.writeCDATAAndEndElement(StringHelper.EMPTY_STRING);
			}
		} catch (IOException e) {
			logError(stclContext, "Cannot save constructor parameters", e);
		}
	}

	@Override
	public String toString() {
		return String.format("link stencil to %s", this._path);
	}

	private class PwdSlot extends CalculatedStringPropertySlot<StclContext, PStcl> {
		public PwdSlot(StclContext stclContext, LinkStcl in, String name) {
			super(stclContext, in, name);
		}

		@Override
		public String getValue(StclContext stclContext, PStcl self) {
			return self.getContainer(stclContext).pwd(stclContext);
		}
	}

}