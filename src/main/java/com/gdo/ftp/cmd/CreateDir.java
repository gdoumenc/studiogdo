/*
 * Copyright GDO - 2004
 */
package com.gdo.ftp.cmd;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;

import com.gdo.ftp.model.FolderStcl;
import com.gdo.project.model.AtomicActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.PathUtils;

/**
 * Creates folder.
 * <p>
 * <ol>
 * <li>Folder name</li>
 * <li>Accept intermediate folder creation.</li>
 * </ol>
 * </p>
 */
public class CreateDir extends AtomicActionStcl {

    public interface Status {
        int NO_NAME_DEFINED = 1;
    }

    private String _name; // file name
    private boolean _folder_creation; // file name

    public CreateDir(StclContext stclContext) {
        super(stclContext);
    }

    @Override
    public CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();
        try {
            PStcl target = cmdContext.getTarget();

            // gets ftp context
            FolderStcl folder = target.getReleasedStencil(stclContext);
            FTPClient client = folder.newClient(stclContext, target);

            try {

                // changes directory
                String path = target.getString(stclContext, FolderStcl.Slot.PATH);
                if (!client.changeWorkingDirectory(path)) {
                    String msg = logError(stclContext, "cannot change to dir %s", path);
                    return error(cmdContext, self, msg);
                }

                // creates intermediate folders
                if (_folder_creation) {
                    if (PathUtils.isComposed(_name)) {
                        String first = PathUtils.getFirstName(_name);
                        _name = PathUtils.getTailName(_name);
                        if (!client.changeWorkingDirectory(_name)) {
                            if (!client.makeDirectory(_name)) {
                                String msg = logError(stclContext, "cannot create dir %s", _name);
                                return error(cmdContext, self, msg);
                            }
                            client.changeWorkingDirectory(_name);
                        }
                        _name = first;
                    }
                }

                // does creation
                boolean created = client.makeDirectory(_name);

                // succeed
                if (created) {
                    String msg = String.format("dir %s/%s created", path, _name);
                    return success(cmdContext, self, msg);
                }
                String msg = String.format("dir %s/%s not created", path, _name);
                return error(cmdContext, self, msg);
            } finally {
                folder.closeClient(stclContext, client, target);
            }
        } catch (Exception e) {
            String msg = logError(stclContext, "cannot create dir %s : %s", _name, e);
            return error(cmdContext, self, msg);
        }
    }

    @Override
    protected CommandStatus<StclContext, PStcl> verifyContext(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {

        _name = getParameter(cmdContext, 1, null);
        if (StringUtils.isEmpty(_name))
            return error(cmdContext, self, Status.NO_NAME_DEFINED, "no name to create file");

        _folder_creation = getParameter(cmdContext, 2, true);

        return super.verifyContext(cmdContext, self);
    }

}