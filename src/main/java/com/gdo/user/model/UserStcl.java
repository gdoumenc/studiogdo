/**
 * Copyright GDO - 2004
 */
package com.gdo.user.model;

import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;

public class UserStcl extends Stcl {

	public interface Slot extends Stcl.Slot {
		String ID = "Id";
		String PASSWD = "Passwd";
		String EMAIL = "EMail";
	}

	public UserStcl(StclContext stclContext) {
		super(stclContext);

		propSlot(Slot.ID);
		propSlot(Slot.PASSWD);
		propSlot(Slot.EMAIL);
	}

	@Override
	public PStcl clone(StclContext stclContext, PSlot<StclContext, PStcl> slot, IKey key, PStcl self) {
		PStcl clone = super.clone(stclContext, slot, key, self);
		clone.setString(stclContext, Slot.EMAIL, self.getString(stclContext, Slot.EMAIL, ""));
		clone.setString(stclContext, Slot.ID, self.getString(stclContext, Slot.ID, ""));
		clone.setString(stclContext, Slot.PASSWD, self.getString(stclContext, Slot.PASSWD, ""));
		return clone;
	}
}