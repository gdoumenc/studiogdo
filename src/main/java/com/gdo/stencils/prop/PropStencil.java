/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.prop;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.io.IOUtils;

import com.gdo.helper.StringHelper;
import com.gdo.stencils.Keywords;
import com.gdo.stencils.Result;
import com.gdo.stencils._Stencil;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.event.IPropertyChangeListener;
import com.gdo.stencils.event.PropertyChangeEvent;
import com.gdo.stencils.faces.RenderContext;
import com.gdo.stencils.facet.FacetResult;
import com.gdo.stencils.factory.StencilFactory;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.util.SlotUtils;
import com.gdo.stencils.util.StencilUtils;
import com.gdo.util.XmlWriter;

/**
 * <p>
 * Simple property stencil.
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

public class PropStencil<C extends _StencilContext, S extends _PStencil<C, S>> extends _Stencil<C, S> {

	// property value
	public String _value;

	// value type when saving (string by default)
	protected String _type = Keywords.STRING;
	protected boolean _expand; // expand the string when read it (use format,

	// default false - as in dtd)

	public interface Slot extends _Stencil.Slot {
		String LISTENERS = "Listeners";
	}

	public PropStencil(C stclContext, String value) {
		super(stclContext);

		this._value = value;

		multiSlot(Slot.LISTENERS, PSlot.ANY, true, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdo.stencils.Stencil#asProp(com.gdo.stencils.StencilContext,
	 * com.gdo.stencils.plug.PStencil)
	 */
	@Override
	public IPPropStencil<C, S> asProp(C stclContext, S self) {
		return self;
	}

	public String getType(C stclContext, S self) {
		return this._type;
	}

	public void setType(C stclContext, String type, S self) {
		this._type = type;
	}

	public boolean isExpand(C stclContext, S self) {
		return this._expand;
	}

	public void setExpand(C stclContext, boolean expand, S self) {
		this._expand = expand;
	}

	public String getValue(C stclContext, S self) {
		return this._value;
	}

	/**
	 * Sets the property value.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param value
	 *          the property value.
	 * @param self
	 *          the stencil as a plugged stencil.
	 * @return the old property value.
	 */
	public String setValue(C stclContext, String value, S self) {

		// sets in contained value
		String old = this._value;
		this._value = value;
		notifyListeners(stclContext, value, old, self);

		return value;
	}

	public Reader getReader(C stclContext, S self) {
		if (this._value == null)
			return StringHelper.EMPTY_STRING_READER;
		return new StringReader(this._value.toString());
	}

	public InputStream getInputStream(C stclContext, S self) {
		if (this._value != null) {
			try {
				return IOUtils.toInputStream(this._value.toString(), _StencilContext.getCharacterEncoding());
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
	 * Notifies all property change listeners that the property value has changed.
	 * 
	 * @param stclContext
	 *          stencil context.
	 * @param value
	 *          new value.
	 * @param old
	 *          old value.
	 * @param self
	 *          the property as a plugged stencil.
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
					this._value = old;
					break;
				}
			}
		} catch (Exception e) {
			logWarn(stclContext, "Exception in property listener : %s", e);
		}
	}

	// defined right generics property
	@Override
	@SuppressWarnings("unchecked")
	public S self(C stclContext, S container) {

		// stand alone plugged property
		if (StencilUtils.isNull(container)) {

			// if not already defined creates it
			if (StencilUtils.isNull(this._self)) {
				StencilFactory<C, S> factory = (StencilFactory<C, S>) stclContext.<C, S> getStencilFactory();
				this._self = factory.newPPropStencil(stclContext, null, Key.NO_KEY, this);
			}
			return (S) this._self;
		}

		// creates the plugged property structure
		StencilFactory<C, S> factory = (StencilFactory<C, S>) stclContext.<C, S> getStencilFactory();
		return factory.newPPropStencil(stclContext, container.getContainingSlot(), Key.NO_KEY, this);
	}

	/**
	 * @return the formated text using the stencil in extensions.
	 */
	@Override
	public FacetResult getFacet(RenderContext<C, S> renderContext) {
		return super.getFacet(renderContext);
	}

	// redefined in EnumProp or other specific property as false
	public boolean shouldBeSavedAsProp(C stclContext, S self) {
		// TODO to optimize : slots should not be taken in account to test == 1
		if (self.isPluggedOnce(stclContext)) {
			return SlotUtils.isSingle(stclContext, self.getContainingSlot(stclContext));
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public void saveAsProp(C stclContext, String name, XmlWriter writer) throws IOException {
		writer.startElement("prop");
		writer.writeAttribute("name", name);
		writer.writeAttribute("type", getType());
		IPPropStencil<C, S> plugged = (IPPropStencil<C, S>) self();
		writer.writeAttribute("expand", isExpand(stclContext, (S) plugged));
		String value = plugged.getNotExpandedValue(stclContext); // never expand
		// when saving
		if (value != null) {
			writer.startElement("data");
			writer.writeCDATAAndEndElement(value);
		}
		writer.endElement("prop");
	}

	@Override
	protected void saveConstructorParameters(C stclContext, XmlWriter writer, S self) {
		try {
			writer.startElement("param");
			writer.writeAttribute("index", 0);
			writer.writeAttribute("type", getType());

			// never expand when saving
			String value = self.getNotExpandedValue(stclContext);
			if (value != null) {
				writer.writeCDATA(value); // never expand when saving
			}
			writer.endElement("param", false);
		} catch (IOException e) {
			logError(stclContext, "Cannot save constructor parameters : %s", e);
		}
	}

	// no slot are saved in a property
	@Override
    protected void saveSlots(C stclContext, XmlWriter descPart, XmlWriter plugPart, S self) throws IOException {
	}
	
	/**
	 * Returns value type of the property. TODO should return class (see also with
	 * PropertyCalculator) May be redefined in subclasses to cover new basic
	 * types.
	 * 
	 * @return the XML code for the type.
	 */
	protected String getType() {
		return Keywords.STRING;
	}

	@Override
	public String toString() {
		if (this._value != null) {
			StringBuffer str = new StringBuffer();
			str.append('"').append(this._value.toString()).append('"');
			str.append('[').append(getType()).append(']');
			str.append('<').append(getClass()).append('>');
			return str.toString();
		}
		return super.toString();
	}

	private StencilIterator<C, S> getListeners(C stclContext, S self) {
		return getStencils(stclContext, IPPropStencil.Slot.LISTENERS, self);
	}

}