/**
 * Copyright GDO - 2004
 */
package com.gdo.sms.model;

import com.gdo.mail.model.MailContextStcl;
import com.gdo.stencils.StclContext;

public class SMSContextStcl extends MailContextStcl {

	public interface Slot extends MailContextStcl.Slot {
		String ACCOUNT = "Account";
		String PASSWORD = "Password";
		String EMAIL = "Email";
		String CLASS_TYPE = "ClassType";
		String ROUTE_TYPE = "RouteType";
	}

	public SMSContextStcl(StclContext stclContext) {
		super(stclContext);

		propSlot(Slot.ACCOUNT, "");
		propSlot(Slot.PASSWORD, "");
		propSlot(Slot.EMAIL, "");
		propSlot(Slot.CLASS_TYPE, "");
		propSlot(Slot.ROUTE_TYPE, "");
	}

}