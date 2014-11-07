/**
 * Copyright GDO - 2005
 */
package com.gdo.mail.model;

import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.plug.PStcl;

public interface IMailSendListener {

    Result beforeSend(CommandContext<StclContext, PStcl> cmdContext, PStcl mail, PStcl recipient, PStcl self);

    Result afterSend(CommandContext<StclContext, PStcl> cmdContext, PStcl mail, PStcl recipient, PStcl self);

    Result afterError(CommandContext<StclContext, PStcl> cmdContext, PStcl mail, PStcl recipient, String reason, PStcl self);

    Result beforeFirst(CommandContext<StclContext, PStcl> cmdContext, PStcl mail, PStcl self);

    Result afterLast(CommandContext<StclContext, PStcl> cmdContext, PStcl mail, PStcl self);
}
