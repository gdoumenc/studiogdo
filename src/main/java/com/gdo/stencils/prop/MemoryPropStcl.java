/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.prop;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;

import com.gdo.project.cmd.Unplug;
import com.gdo.project.slot.RootSlot;
import com.gdo.reflect.CommandsSlot;
import com.gdo.reflect.PwdSlot;
import com.gdo.reflect.SlotsSlot;
import com.gdo.reflect.TemplateNameSlot;
import com.gdo.reflect.WhereSlot;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.faces.RenderContext;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot._Slot;
import com.gdo.stencils.util.PathUtils;

/**
 * <p>
 * Basic implementation of the studiogdo property stencil.
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
public class MemoryPropStcl extends PropStencil<StclContext, PStcl> {

	public static final int MAX_VALUE_SIZE = 100;

	public interface Slot extends Stcl.Slot {
	}

	public interface Command extends Stcl.Command {
	}

	public MemoryPropStcl(StclContext stclContext, String value) {
		super(stclContext, value);

		// SLOT PART

		// reflexive slots
		createTemplateNameSlot(stclContext);
		createPwdSlot(stclContext);
		emptySlot(Slot.$KEY);
		createSlotSlot(stclContext);
		createCommandSlot(stclContext);
		createWhereSlot(stclContext);

		// COMMAND PART

		command(Command.UNPLUG, Unplug.class);
	}

	public MemoryPropStcl(StclContext stclContext, Boolean value) {
		this(stclContext, value.toString());
	}

	public MemoryPropStcl(StclContext stclContext, Integer value) {
		this(stclContext, value.toString());
	}

	@Override
	protected _Slot<StclContext, PStcl> createRootSlot(StclContext stclContext) {
		return new RootSlot(stclContext, this, PathUtils.ROOT);
	}

	protected _Slot<StclContext, PStcl> createTemplateNameSlot(StclContext stclContext) {
		return new TemplateNameSlot(stclContext, this, Slot.$TEMPLATE_NAME);
	}

	protected _Slot<StclContext, PStcl> createPwdSlot(StclContext stclContext) {
		return new PwdSlot(stclContext, this, Slot.$PWD);
	}

	protected _Slot<StclContext, PStcl> createSlotSlot(StclContext stclContext) {
		return new SlotsSlot(stclContext, this, Slot.$SLOTS);
	}

	protected _Slot<StclContext, PStcl> createCommandSlot(StclContext stclContext) {
		return new CommandsSlot(stclContext, this, Slot.$COMMANDS);
	}

	protected _Slot<StclContext, PStcl> createWhereSlot(StclContext stclContext) {
		return new WhereSlot(stclContext, this, Slot.$WHERE);
	}

	@Override
	public PStcl clone(StclContext stclContext, PSlot<StclContext, PStcl> slot, IKey key, PStcl self) throws CloneNotSupportedException {
		PStcl clone = super.clone(stclContext, slot, key, self);
		clone.setValue(stclContext, self.getValue(stclContext));
		return clone;
	}

	@Override
	public void multipart(StclContext stclContext, String fileName, FileItem item, PStcl self) {
		StringWriter writer = new StringWriter();
		try {
			IOUtils.copy(item.getInputStream(), writer);
		} catch (IOException e) {
			if (getLog().isWarnEnabled())
				getLog().warn(stclContext, e);
		}
		setValue(stclContext, writer.toString(), self);
	}

	public String getHtml(StclContext stclContext, RenderContext<StclContext, PStcl> renderContext, String mode) {
		if (getLog().isErrorEnabled()) {
			getLog().error(stclContext, "getHtml(PropertyStcl) : Should not goes here");
		}
		return "";
		/*
		 * StclContext stclContext = renderContext.getStencilContext(); PStcl
		 * plugged = renderContext.getStencilRendered(); PStencil<StclContext,
		 * IProperty<StclContext, PStcl, ?>> prop = (PStencil<StclContext,
		 * IProperty<StclContext, PStcl, ?>>) plugged; return (String)
		 * getValue(stclContext, prop);
		 */
	}

	/*
	 * @Override public Reader getFacet(StclContext stclContext,
	 * RenderContext<StclContext, PStcl> renderContext) { String value =
	 * format(stclContext, getAdapter(stclContext, String.class, self()), self());
	 * // default mode is label String mode = renderContext.getFacetMode(); if
	 * (true || mode == null || Keywords.LABEL.equals(mode)) { if
	 * (StringUtils.isEmpty(value)) return StringHelper.EMPTY_STRING_READER;
	 * return new StringReader(value); } return StringHelper.EMPTY_STRING_READER;
	 * }
	 */

}