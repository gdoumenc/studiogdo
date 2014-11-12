/*
 * Copyright GDO - 2004
 */
package com.gdo.mail.cmd;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.URLName;

import org.apache.commons.lang3.StringUtils;

import com.gdo.context.model.FileStcl;
import com.gdo.mail.model.FolderStcl;
import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;

public class RenameFolder extends AtomicActionStcl {

    public interface Status {
        int NO_NAME_DEFINED = 1;
        int CLASS_CAST_EXCEPTION = 2;
        int ALREADY_EXIST = 3;
        int EXCEPTION = 4;
    }

    private String _name; // new folder name

    public RenameFolder(StclContext stclContext) {
        super(stclContext);
    }

    @Override
    public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();
        PStcl target = cmdContext.getTarget();
        String name = target.getName(stclContext);
        try {

            // get source folder
            if (!(target.getReleasedStencil(stclContext) instanceof FolderStcl)) {
                String msg = String.format("internal error: target %s must be instance of %s", target, FileStcl.class.getName());
                return error(cmdContext, self, 0, msg);
            }
            FolderStcl df = (FolderStcl) target.getReleasedStencil(stclContext);
            Folder source = df.getFolder(stclContext, self);

            // get destination folder
            Store store = source.getStore();
            Folder dest = store.getFolder(urlName(source.getURLName(), _name));
            if (!dest.exists()) {
                dest.create(Folder.HOLDS_MESSAGES);
            } else {
                String msg = String.format("destination file %s already exist", _name);
                return error(cmdContext, self, Status.ALREADY_EXIST, msg);
            }
            if (!source.renameTo(dest)) {
                String msg = String.format("cannot rename file %s to %s");
                return error(cmdContext, self, 0, msg);
            }
            return success(cmdContext, self, 0, dest.hashCode());
        } catch (MessagingException e) {
            String msg = logError(stclContext, "exception when renaming %s (%s)", name, e);
            return error(cmdContext, self, Status.EXCEPTION, msg);
        }
    }

    @Override
    protected CommandStatus<StclContext, PStcl> verifyContext(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {

        _name = getParameter(cmdContext, 1, null);
        if (StringUtils.isEmpty(_name))
            return error(cmdContext, self, Status.NO_NAME_DEFINED, "no name defined to rename file");

        return super.verifyContext(cmdContext, self);
    }

    private URLName urlName(URLName url, String name) {
        return new URLName(url.getProtocol(), url.getHost(), url.getPort(), name, url.getUsername(), url.getPassword());
    }
}