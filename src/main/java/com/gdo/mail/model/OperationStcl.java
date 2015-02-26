/**
 * Copyright GDO - 2004
 */
package com.gdo.mail.model;

import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.CalculatedIntegerPropertySlot;

public class OperationStcl extends Stcl {

	public interface Slot extends Stcl.Slot {
		String MAIL_CONTEXT = "MailContext";

		String SEGMENTS = "Segments";
		String NUMBER_OF_SEGMENTS = "NumberOfSegments";
		String GENERATORS = "Generators";

		String SERVICES = "Services";
	}

	public OperationStcl(StclContext stclContext) {
		super(stclContext);

		new NumberOfSegmentsSlot(stclContext);
	}

	private class NumberOfSegmentsSlot extends CalculatedIntegerPropertySlot<StclContext, PStcl> {

		public NumberOfSegmentsSlot(StclContext stclContext) {
			super(stclContext, OperationStcl.this, Slot.NUMBER_OF_SEGMENTS);
		}

		@Override
		public int getIntegerValue(StclContext stclContext, PStcl self) {
			return self.getContainer(stclContext).getSlot(stclContext, Slot.SEGMENTS).size(stclContext, null);
		}

	}

}