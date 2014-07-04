/**
 * Copyright GDO - 2005
 */
package com.gdo.mail.model;

import java.util.Properties;

import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.plug.PStcl;

public interface IMail {

	/**
	 * Interface defined to format mail content if needed before sending.
	 */
	public interface ContentFormatter {
		String getContent(StclContext stclContext, String content, PStcl self);
	}

	/**
	 * Adds a listener which will be notified at each mail sending.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param listener
	 *          the mail listener (must implement the <tt>IMailSendListener</tt>
	 *          interface).
	 * @param self
	 *          this stencil as a plugged stencil.
	 */
	public void addSendListener(StclContext stclContext, PStcl listener, PStcl self);

	public void setContentFormatter(StclContext stclContext, ContentFormatter formatter, PStcl self);

	public Result send(CommandContext<StclContext, PStcl> cmdContext, Properties props, PStcl self);

	public Result multiSend(CommandContext<StclContext, PStcl> cmdContext, Properties props, PStcl self);

}
