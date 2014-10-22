/* Copyright GDO - 2005*/

package com.gdo.project.cmd;

import com.gdo.stencils.StclContext;

public class CreateFilePropertyStencilInOneStep extends CreateInOneStep {

	//private String _value; // store the value when replacing simple property to

	// file property

	public CreateFilePropertyStencilInOneStep(StclContext stclContext) {
		super(stclContext);
	}

	
	/***
	@Override
	protected CommandStatus<StclContext, PStcl> beforePlug(CommandContext<StclContext, PStcl> cmdContext, PStcl created, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		_value = _created.getValue(stclContext);

		// creates the final real file property stencil and replaces old simple
		// property used to store the value
		_created = self.newPStencil(stclContext, Slot.STENCIL_HOLDER, Key.NO_KEY, PropStcl.class);
		return super.beforePlug(cmdContext, created, self);
	}

	@Override
	protected CommandStatus<StclContext, PStcl> afterPlug(CommandContext<StclContext, PStcl> cmdContext, PStcl created, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		created.setValue(stclContext, _value);
		return super.beforePlug(cmdContext, created, self);
	}
	***/

}
