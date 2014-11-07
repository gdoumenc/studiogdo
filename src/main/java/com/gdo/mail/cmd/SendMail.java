/**
 * Copyright GDO - 2004
 */
package com.gdo.mail.cmd;

import com.gdo.mail.model.IMail;
import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils._Stencil;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;

/**
 * @author gdo <param0>Title</param0> <param1>From</param1> <param2>To</param2>
 *         <param3>CC : if null, then get from slot, if empty nothing else
 *         ...</param3> <param4>Content</param4> <param5>BCC : if null, then get
 *         from slot, if empty nothing else ...</param5>
 * 
 */
public class SendMail extends AtomicActionStcl {

    public SendMail(StclContext stclContext) {
        super(stclContext);
    }

    @Override
    public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();

        // checks target is a mail implementation
        PStcl target = self.getStencil(stclContext, Slot.TARGET);
        _Stencil<StclContext, PStcl> mail = target.getReleasedStencil(stclContext);
        if (!(mail instanceof IMail)) {
            return error(cmdContext, self, "Target not implementing IMail interface");
        }

        // sends message and returns status
        Result result = ((IMail) mail).send(cmdContext, cmdContext, target);
        return success(cmdContext, self, result);
    }
}