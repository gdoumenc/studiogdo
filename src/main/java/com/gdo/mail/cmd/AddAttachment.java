/* Copyright GDO - 2005*/

package com.gdo.mail.cmd;

import org.apache.commons.fileupload.FileItem;

import com.gdo.mail.model.AttachmentStcl;
import com.gdo.mail.model.FileUploadDataSourceStcl;
import com.gdo.mail.model.MailStcl;
import com.gdo.project.model.ComposedActionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.StencilUtils;

public class AddAttachment extends ComposedActionStcl {

    public interface Slot extends ComposedActionStcl.Slot {
        String TYPE = "Type";
    }

    public AddAttachment(StclContext stclContext) {
        super(stclContext);
    }

    private FileItem _fileItem; // file item uploaded
    private int _type; // type of the resource attached

    @Override
    public CommandStatus<StclContext, PStcl> performSteps(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();
        int currentStep = getActiveStepIndex();

        // creates the attachments
        if (currentStep == 2) {
            if (_type == 1) {
                PStcl target = cmdContext.getTarget();
                PStcl att = target.newPStencil(stclContext, MailStcl.Slot.ATTACHMENTS, Key.NO_KEY, FileUploadDataSourceStcl.class, _fileItem);
                if (StencilUtils.isNull(att)) {
                    return error(cmdContext, self, StencilUtils.getNullReason(att));
                }
                att.setInt(stclContext, AttachmentStcl.Slot.TYPE, _type);
            } else if (_type == 2) {
                PStcl target = cmdContext.getTarget();
                PStcl att = target.newPStencil(stclContext, MailStcl.Slot.ATTACHMENTS, Key.NO_KEY, FileUploadDataSourceStcl.class, _fileItem);
                if (StencilUtils.isNull(att)) {
                    return error(cmdContext, self, StencilUtils.getNullReason(att));
                }
                att.setInt(stclContext, AttachmentStcl.Slot.TYPE, _type);
            }
        }

        return success(cmdContext, self);
    }

    @Override
    protected CommandStatus<StclContext, PStcl> verifyContext(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();
        int currentStep = getActiveStepIndex();

        // verifies attachment type
        if (currentStep == 2) {
            _type = self.getInt(stclContext, com.gdo.mail.model.AttachmentStcl.Slot.TYPE, 0);
            if (_type == 0) {
                String msg = "wrong attachment type";
                if (getLog().isWarnEnabled()) {
                    getLog().warn(stclContext, msg);
                }
                return error(cmdContext, self, msg);
            }
        }

        return super.verifyContext(cmdContext, self);
    }

    @Override
    public void multipart(StclContext stclContext, String fileName, FileItem item, PStcl self) {
        _fileItem = item;
    }

}