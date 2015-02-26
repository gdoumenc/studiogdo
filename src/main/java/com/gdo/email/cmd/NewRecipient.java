package com.gdo.email.cmd;

import com.gdo.email.model.SQLDistributionListStcl;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.CalculatedStringPropertySlot;

public class NewRecipient extends com.gdo.mail.cmd.NewRecipient {

	public interface Slot extends Stcl.Slot {
		String STATUS = SQLDistributionListStcl.Slot.STATUS;
	}

	public NewRecipient(StclContext stclContext) {
		super(stclContext);

		new FromTableSlot(stclContext);
	}

	private class FromTableSlot extends CalculatedStringPropertySlot<StclContext, PStcl> {

		public FromTableSlot(StclContext stclContext) {
			super(stclContext, NewRecipient.this, Slot.STATUS);
		}

		public String getValue(StclContext stclContext, PStcl self) throws Exception {
			return NewRecipient.this._slot.getContainer().getString(stclContext, Slot.STATUS);
		}
	}
}
