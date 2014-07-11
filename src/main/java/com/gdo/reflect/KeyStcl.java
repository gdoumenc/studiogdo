/**
 * Copyright GDO - 2004
 */
package com.gdo.reflect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.eval.NotImplementedException;

import com.gdo.helper.IOHelper;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.prop.IPropCalculator;
import com.gdo.stencils.slot.CalculatedStringPropertySlot;
import com.gdo.stencils.slot.SingleCalculatedPropertySlot;
import com.gdo.stencils.slot.SingleCalculatedSlot;
import com.gdo.stencils.util.StencilUtils;

/**
 * <p>
 * Reflexive slot descriptor stencil.
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
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */
public class KeyStcl extends Stcl {

	public interface Slot extends Stcl.Slot {
		String TYPE = "Type";
		String VALUE = "Value";
		String STENCIL = "Stencil";
		String PWD = "Pwd";
	}

	public interface Command extends Stcl.Command {
		String CHANGE = "Change";
	}

	private PStcl _stencil; // stencil defined at the key

	public KeyStcl(StclContext stclContext, PStcl stencil) {
		super(stclContext);
		this._stencil = stencil;

		propSlot(Slot.TYPE, "key");
		delegateSlot(Slot.VALUE, "Name");

		// change $TemplateName calculator to give contained property template name
		PSlot<StclContext, PStcl> name = stencil.getSlot(stclContext, Slot.$TEMPLATE_NAME);
		SingleCalculatedPropertySlot<StclContext, PStcl> n = (SingleCalculatedPropertySlot<StclContext, PStcl>) name.getSlot();
		n.setCalculator(new TemplateNameCalculator());

		// the stencil return is the one plugged when construct
		// (not the key stencil as parent)
		new StencilSlot(stclContext);
		new PwdSlot(stclContext);
		delegateSlot("Stencil$Slots", "Stencil/$Slots");

		command(Command.CHANGE, SetKeyCmd.class);
	}

	@Override
	public String getName(StclContext stclContext, PStcl self) {
		return self.getKey().toString();
	}

	public PStcl getStcl() {
		return this._stencil;
	}

	public PSlot<StclContext, PStcl> getSlot() {
		return this._stencil.getContainingSlot();
	}

	/**
	 * Stencil slot
	 */
	private class StencilSlot extends SingleCalculatedSlot<StclContext, PStcl> {
		public StencilSlot(StclContext stclContext) {
			super(stclContext, KeyStcl.this, Slot.STENCIL);
		}

		@Override
		public PStcl getCalculatedStencil(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
			return KeyStcl.this._stencil;
		}

		@Override
		public StencilIterator<StclContext, PStcl> getStencils(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
			// change slot container to have original one
			return StencilUtils.< StclContext, PStcl> iter(stclContext, KeyStcl.this._stencil, KeyStcl.this._stencil.getContainingSlot());
		}
	}

	private class TemplateNameCalculator implements IPropCalculator<StclContext, PStcl> {
		@Override
		public String getValue(StclContext stclContext, PStcl self) {
			return KeyStcl.this._stencil.getString(stclContext, Stcl.Slot.$TEMPLATE_NAME, null);
		}

		@Override
		public String setValue(StclContext stclContext, String value, PStcl self) {
			String msg = String.format("Cannot change %s value", Stcl.Slot.$TEMPLATE_NAME);
			throw new NotImplementedException(msg);
		}

		@Override
		public InputStream getInputStream(StclContext stclContext, PStcl self) {
			try {
				String value = getValue(stclContext, self);
				if (StringUtils.isNotEmpty(value)) {
					return IOUtils.toInputStream(value, StclContext.getCharacterEncoding());
				}
			} catch (IOException e) {
			}
			return IOHelper.EMPTY_INPUT_STREAM;
		}

		@Override
		public OutputStream getOutputStream(StclContext stclContext, PStcl self) {
			return IOHelper.EMPTY_OUTPUT_STREAM;
		}
	}

	/**
	 * Path prop value is calculated from slot's path.
	 */
	private class PwdSlot extends CalculatedStringPropertySlot<StclContext, PStcl> {
		public PwdSlot(StclContext stclContext) {
			super(stclContext, KeyStcl.this, Slot.PWD);
		}

		@Override
		public String getValue(StclContext stclContext, PStcl self) {
			String pwd = KeyStcl.this._stencil.getContainingSlot().pwd(stclContext);
			String key = KeyStcl.this._stencil.getKey().toString();
			if (StringUtils.isEmpty(key))
				return String.format("%s", pwd);
			return String.format("%s(%s)", pwd.replaceAll("//", "/"), key);
		}

		@Override
		public String setValue(StclContext stclContext, String value, PStcl self) {
			String msg = String.format("Cannot change %s value", Slot.PWD);
			throw new IllegalStateException(msg);
		}
	}

}