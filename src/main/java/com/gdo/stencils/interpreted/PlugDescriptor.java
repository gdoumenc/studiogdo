/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.interpreted;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.gdo.stencils.Keywords;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.factory.IStencilFactory;
import com.gdo.stencils.factory.IStencilFactory.Mode;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.util.StencilUtils;
import com.gdo.util.XmlWriter;

/**
 * <p>
 * Plug descriptor class.
 * <p>
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
 * @see com.gdo.stencils.cmd.CommandContext Context
 */
public final class PlugDescriptor<C extends _StencilContext, S extends _PStencil<C, S>> extends _Descriptor<C, S> {
	// private S _stencil; // some plugs are defined with the stencil not as a
	// reference (prop tag)
	private String _ref; // reference to the instance stencil plugged
	private String _path; // path from reference (for substencil plug)
	private String _slot; // slot name
	private String _key; // plug key
	private IStencilFactory.Mode _on = IStencilFactory.Mode.ON_CREATION;// when

	// the
	// plug
	// should
	// be
	// performed
	// (by
	// default
	// on
	// create
	// by
	// digester)

	public String getRef() {
		return _ref;
	}

	// used by digester
	public void setRef(String ref) {
		_ref = ref;
	}

	public String getPath() {
		return _path;
	}

	// used by digester
	public void setPath(String path) {
		_path = path;
	}

	public String getSlot() {
		return _slot;
	}

	// used by digester
	public void setSlot(String slot) {
		_slot = slot;
	}

	public String getKey() {
		return _key;
	}

	// used by digester
	public void setKey(String key) {
		_key = key;
	}

	public IStencilFactory.Mode getOnAsMode() {
		return _on;
	}

	public void setOnAsMode(IStencilFactory.Mode mode) {
		_on = mode;
	}

	// used by digester
	public void setOn(String on) {
		if (Keywords.CREATE.equals(on)) { // used by digester

			_on = Mode.ON_CREATION;
		} else if (Keywords.LOAD.equals(on)) {
			_on = Mode.ON_LOAD;
		} else if (Keywords.ALWAYS.equals(on)) {
			_on = Mode.ON_ALWAYS;
		} else {
			if (getLog().isWarnEnabled()) {
				getLog().warn(null, "Unknow plug on mode : " + on);
			}
		}
	}

	// set the plug on slot to be used on lazy evaluation
	public void setOnSlot(C stclContext, S container, InstanceRepository<C, S> instances, int completionLevel) {

		// if the plug should be done only for creation, do nothing in loading
		// and vice-versa
		IStencilFactory.Mode mode = getOnAsMode();
		if (!mode.equals(Mode.ON_ALWAYS) && mode != instances.getMode()) {
			return;
		}

		// set the plug on the slot
		try {
			String slotName = getSlot();
			PSlot<C, S> slot = container.getSlot(stclContext, slotName);

			// plug in slot only if plug is defined after the slot (no plug in
			// case of redefinition)
			if (completionLevel <= slot.getSlot().getCompletionLevel()) {

				// get the absolute reference path
				S toBePlugged = instances.getInstance(stclContext, getRef());
				if (StencilUtils.isNull(toBePlugged)) {
					if (getLog().isWarnEnabled()) {
						String msg = String.format("Cannot found stencil %s in %s to plug it in %s", getRef(), container, slotName);
						getLog().warn(stclContext, msg);
					}
					return;
				}

				// change target if path is defined
				if (!StringUtils.isEmpty(getPath())) {
					toBePlugged = toBePlugged.getStencil(stclContext, getPath());
				}

				// do the plug right now
				IKey key = (StringUtils.isEmpty(getKey())) ? Key.NO_KEY : new Key<String>(getKey());
				slot.getSlot().plug(stclContext, toBePlugged, key, slot);
			}
		} catch (Exception e) {
			if (getLog().isWarnEnabled()) {
				String msg = String.format("Exception when plugging stencil %s in %s in slot %s :%s ", getRef(), container, getSlot(), e.getMessage());
				getLog().warn(stclContext, msg);
			}
		}
	}

	@Override
	public void save(C stclContext, XmlWriter declPart, XmlWriter plugPart) throws IOException {

		// checks parameters
		if (declPart != null) {
			logError(stclContext, "decl xml writer should be null for plug descriptor (perhaps for trace..?)");
			return;
			// throw new
			// IllegalArgumentException("decl xml writer should be null for plug descriptor");
		}

		// don't save creation plug (it is defined in template description)
		if (getOnAsMode() == IStencilFactory.Mode.ON_CREATION) {
			return;
		}

		// if the ref is not a UID (starts with _), the ref has changed
		String ref = getRef();
		/*
		 * if (instances.refHasChanged(stclContext, ref)) { ref =
		 * instances.getNewRef(stclContext, ref); }
		 */

		// starts the plug tag
		plugPart.startElement("plug");

		// saves key only if exists (not single) and not internal (starts with
		// _)
		plugPart.writeAttribute("slot", getSlot());
		String key = getKey();
		if (!StringUtils.isEmpty(key)) {
			plugPart.writeAttribute("key", key);
		}

		// substencil ref and path
		plugPart.writeAttribute("ref", ref);
		String path = getPath();
		if (!StringUtils.isEmpty(path)) {
			plugPart.writeAttribute("path", path);
		}

		// closes plug tag
		plugPart.endElement("plug");
	}
}