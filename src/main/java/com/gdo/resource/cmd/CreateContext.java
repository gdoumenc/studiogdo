/*
 * Copyright GDO - 2004
 */
package com.gdo.resource.cmd;

import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PStcl;

public class CreateContext extends AtomicActionStcl {

    public CreateContext(StclContext stclContext) {
        super(stclContext);
    }

    @Override
    public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();
        PStcl target = cmdContext.getTarget();

        try {
            // creating new context to Contexts Slot
            String ctxtTemplate = getParameter(cmdContext, 1, "");
            String key = "" + System.currentTimeMillis();
            target.newPStencil(stclContext, "Contexts", new Key(key), ctxtTemplate);

            // Linking new plugged contexts Files slot to Resources
            // PStcl newContext = target.newPStencil(stclContext, "Contexts",
            // key, ctxtTemplate, "");
            // String contextKey = newContext.getKey().toString();
            // target.newPStencil(stclContext, "Resources", key,
            // LinkStcl.class.getName(), "../Contexts(" + contextKey +
            // ")/Files");
            // target.newPStencil(stclContext, "FoldersOnly", key,
            // LinkStcl.class.getName(), "../Contexts(" + contextKey +
            // ")/FoldersOnly");
            // target.newPStencil(stclContext, "FilesOnly", key,
            // LinkStcl.class.getName(), "../Contexts(" + contextKey +
            // ")/FilesOnly");

        } catch (Exception e) {
            logError(stclContext, e.toString());
            return error(cmdContext, self, 0, e.getMessage());
        }
        return success(cmdContext, self);
    }

}