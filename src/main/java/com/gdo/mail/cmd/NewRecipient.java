package com.gdo.mail.cmd;

import com.gdo.mail.model.SQLDistributionListStcl;
import com.gdo.sql.cmd.NewSQLStcl;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.CalculatedStringPropertySlot;

public class NewRecipient extends NewSQLStcl {

	public interface Slot extends Stcl.Slot {
		String FROM_TABLE = SQLDistributionListStcl.Slot.FROM_TABLE;
	}

	public NewRecipient(StclContext stclContext) {
		super(stclContext);

		new FromTableSlot(stclContext);
	}

	private class FromTableSlot extends CalculatedStringPropertySlot<StclContext, PStcl> {

		public FromTableSlot(StclContext stclContext) {
			super(stclContext, NewRecipient.this, Slot.FROM_TABLE);
		}

		@Override
        public String getValue(StclContext stclContext, PStcl self) throws Exception {
			return NewRecipient.this._slot.getContainer().getString(stclContext, Slot.FROM_TABLE);
		}
	}
}
