/*
 * Copyright GDO - 2004
 */
package com.gdo.site.cmd;

import org.apache.commons.lang3.StringUtils;

import com.gdo.ftp.model.FtpContextStcl;
import com.gdo.generator.model.GeneratorStcl;
import com.gdo.project.model.AtomicActionStcl;
import com.gdo.site.model._PageStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.StencilUtils;

public class Update extends AtomicActionStcl {

	public interface Slot extends AtomicActionStcl.Slot {
		String ALLOW_DIR_CREATION = "AllowDirCreation";
	}

	public Update(StclContext stclContext) {
		super(stclContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * Writes file content on ftp.
	 * 
	 * @see com.gdo.stencils.cmd.CommandStencil#doAction(com.gdo.stencils.cmd.
	 * CommandContext, com.gdo.stencils.plug.PStencil)
	 */
	@Override
	protected CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		PStcl page = cmdContext.getTarget();

		// gets the ftp context
		PStcl ftpContextStcl = page.getStencil(stclContext, _PageStcl.Slot.FTP_CONTEXT);
		if (StencilUtils.isNull(ftpContextStcl)) {
			return error(cmdContext, self, 1, "no FTP context defined");
		}

		// gets the generator
		PStcl generatorStcl = page.getStencil(stclContext, _PageStcl.Slot.GENERATOR);
		if (StencilUtils.isNull(generatorStcl)) {
			return error(cmdContext, self, 2, "no generator defined");
		}

		// updates all files
		CommandStatus<StclContext, PStcl> status = success(cmdContext, self);
		int filesUpdated = 0;
		for (PStcl fileStcl : generatorStcl.getStencils(stclContext, GeneratorStcl.Slot.FILES_GENERATED)) {

			// gets url (don't consider empty or internal url)
			String url = fileStcl.getKey().toString();
			if (StringUtils.isBlank(url) || url.startsWith("$")) {
				continue;
			}

			// puts file content
			String content = fileStcl.getReleasedStencil(stclContext).getValue(stclContext, fileStcl);
			CommandStatus<StclContext, PStcl> s = ftpContextStcl.call(stclContext, FtpContextStcl.Command.PUT, content, url);
			if (s.isSuccess()) {
				filesUpdated++;
			} else {
				status.addOther(s);
			}
		}

		// final status (counter value added)
		String msg = String.format("%d fichiers mis à jour", filesUpdated);
		status.addOther(success(cmdContext, self, msg));
		return status;
	}
}