/**
 * Copyright GDO - 2004
 */
package com.gdo.mail.cmd;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.GenericValidator;

import com.gdo.mail.model.MailStcl;
import com.gdo.mail.model.RecipientStcl;
import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.StencilUtils;

public class IsMailValid extends AtomicActionStcl {

    public IsMailValid(StclContext stclContext) {
        super(stclContext);
    }

    @Override
    public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {

        // get message and set global parameters
        StclContext stclContext = cmdContext.getStencilContext();
        PStcl mail = cmdContext.getTarget();

        PStcl from = mail.getStencil(stclContext, MailStcl.Slot.FROM);
        if (!isValid(stclContext, from))
            return success(cmdContext, self, false);

        String name = mail.getExpandedString(stclContext, MailStcl.Slot.FROM_NAME, null);
        if (StringUtils.isEmpty(name))
            return success(cmdContext, self, false);

        String title = mail.getExpandedString(stclContext, MailStcl.Slot.TITLE, null);
        if (StringUtils.isEmpty(title))
            return success(cmdContext, self, false);

        return success(cmdContext, self, true);
    }

    private boolean isValid(StclContext stclContext, PStcl rec) {
        if (StencilUtils.isNull(rec))
            return false;
        String add = rec.getExpandedString(stclContext, RecipientStcl.Slot.ADDRESS, null);
        return (!StringUtils.isEmpty(add) && GenericValidator.isEmail(add));
    }
}