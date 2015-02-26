/*
 * Copyright GDO - 2004
 */
package com.gdo.mail.cmd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.commons.lang3.StringUtils;

import com.gdo.mail.model.DistributionListStcl;
import com.gdo.mail.model.RecipientStcl;
import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PStcl;

public class AddRecipientsFromFile extends AtomicActionStcl {

	public AddRecipientsFromFile(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();

		// get parameters
		String from = getParameter(cmdContext, 1, null);
		if (StringUtils.isEmpty(from))
			return error(cmdContext, self, "no file defined (param1)");

		try {
			File file = new File(from);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				self.newPStencil(stclContext, DistributionListStcl.Slot.TO, Key.NO_KEY, RecipientStcl.class, line);
			}
			br.close();
		} catch (Exception e) {
			String msg = logError(stclContext, e.toString());
			return error(cmdContext, self, msg);
		}

		return success(cmdContext, self);
	}

}