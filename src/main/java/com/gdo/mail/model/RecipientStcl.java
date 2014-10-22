/**
 * Copyright GDO - 2004
 */
package com.gdo.mail.model;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;

import com.gdo.stencils.Result;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.CalculatedBooleanPropertySlot;
import com.gdo.stencils.slot.CalculatedStringPropertySlot;
import com.gdo.util.XmlWriter;

public class RecipientStcl extends Stcl implements IRecipient {

	public interface Slot extends Stcl.Slot {
		String ADDRESS = "Address";
		String IS_VALID = "IsValid";
	}

	private String _address;

	// defined in constructor parameter for compatibility reason
	public RecipientStcl(StclContext stclContext, String add) {
		this(stclContext);
		_address = add;
	}

	public RecipientStcl(StclContext stclContext) {
		super(stclContext);

		new AddressSlot(stclContext);
		new IsValidSlot(stclContext);
	}

	@Override
	public String getName(StclContext stclContext, PStcl self) {

		// test if a name is specifically defined
		String name = getJavaName(stclContext, self);

		// return address if no name
		if (StringUtils.isBlank(name)) {
			return self.getString(stclContext, Slot.ADDRESS, "");
		}

		// return no name
		return "";
	}

	// saves address as parameter (why not...)
	@Override
	protected void saveConstructorParameters(StclContext stclContext, XmlWriter writer, PStcl self) {
		try {
			writer.startElement("param");
			writer.writeAttribute("index", Integer.toString(0));
			writer.writeAttribute("type", "string");
			String add = (_address != null) ? _address : "";
			writer.writeCDATAAndEndElement(add);
		} catch (Exception e) {
			logError(stclContext, "Cannot save constructor parameters for %s", getClass().getName());
		}
	}

	/**
	 * Checks the address validity (may be redefined in subclasses).
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param email
	 *          the address email.
	 * @param self
	 *          this stencil as a plugged stencil.
	 * @return <tt>true</tt> if the address is valid, <tt>false</tt> otherwise.
	 */
	protected boolean isValid(StclContext stclContext, String email, PStcl self) {
		if (StringUtils.isBlank(email)) {
			return false;
		}
		// return EmailValidator.getInstance().isValid(email);
		return true;
	}

	/**
	 * IRecipient interface method.
	 */
	@Override
	public Result getInternetAddress(StclContext stclContext, PStcl self) {
		if (StringUtils.isBlank(RecipientStcl.this._address)) {
			Result.error("blank address");
		}
		try {
			InternetAddress add = new InternetAddress(RecipientStcl.this._address);
			return Result.success(add);
		} catch (AddressException e) {
			String msg = String.format("wrong email address %s : %s", RecipientStcl.this._address, e);
			if (getLog().isWarnEnabled())
				getLog().warn(stclContext, msg);
			return Result.error(e.getMessage());
		}
	}

	private class AddressSlot extends CalculatedStringPropertySlot<StclContext, PStcl> {

		public AddressSlot(StclContext stclContext) {
			super(stclContext, RecipientStcl.this, Slot.ADDRESS);
		}

		@Override
		public String getValue(StclContext stclContext, PStcl self) {
			if (RecipientStcl.this._address != null) {
				return RecipientStcl.this._address;
			}
			return "";
		}

		@Override
		public String setValue(StclContext stclContext, String value, PStcl self) {
			if (isValid(stclContext, value, self)) {
				RecipientStcl.this._address = value;
			}
			return value;
		}
	}

	private class IsValidSlot extends CalculatedBooleanPropertySlot<StclContext, PStcl> {

		public IsValidSlot(StclContext stclContext) {
			super(stclContext, RecipientStcl.this, Slot.IS_VALID);
		}

		@Override
		public boolean getBooleanValue(StclContext stclContext, PStcl self) {
			return isValid(stclContext, RecipientStcl.this._address, self);
		}
	}
}