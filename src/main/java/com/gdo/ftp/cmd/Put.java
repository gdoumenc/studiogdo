/*
 * Copyright GDO - 2004
 */
package com.gdo.ftp.cmd;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;

import com.gdo.ftp.model.FtpContextStcl;
import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;

public class Put extends AtomicActionStcl {

	public interface Status {
		int NO_NAME = 1;
		int IO_EXCEPTION = 2;
	}

	public Put(StclContext stclContext) {
		super(stclContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdo.stencils.cmd.CommandStencil#doAction(com.gdo.stencils.cmd.
	 * CommandContext, com.gdo.stencils.plug.PStencil)
	 */
	@Override
	public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		try {
			PStcl target = cmdContext.getTarget();

			// gets ftp context
			if (!(target.getReleasedStencil(stclContext) instanceof FtpContextStcl)) {
				String msg = String.format("wrong target type %s (should be subclass of FtpContextStcl)", target);
				return error(cmdContext, self, msg);
			}
			FtpContextStcl ftpContext = (FtpContextStcl) target.getReleasedStencil(stclContext);

			// gets content
			String content = getParameter(cmdContext, 1, "");
			String name = getParameter(cmdContext, 2, "");
			if (StringUtils.isBlank(name)) {
				return error(cmdContext, self, Status.NO_NAME, "No file name defined for ftp put");
			}

			// perhaps encodes content before transfering
			byte[] bytes;
			String encodingType = target.getString(stclContext, FtpContextStcl.Slot.ENCODING_TYPE, StclContext.getCharacterEncoding());
			if (StringUtils.isNotEmpty(encodingType)) {
				// bytes = content.getBytes(encodingType);
				bytes = content.getBytes();
			} else {
				bytes = content.getBytes();
			}

			// does transfer
			InputStream is = new ByteArrayInputStream(bytes);
			Result result = ftpContext.put(stclContext, is, name, true, ".back", false, target);
			is.close();
			return success(cmdContext, self, result);
		} catch (Exception e) {
			logError(stclContext, e.getMessage());
			return error(cmdContext, self, Status.IO_EXCEPTION, e);
		}
	}

}