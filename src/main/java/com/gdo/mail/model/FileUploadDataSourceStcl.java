/**
 * Copyright GDO - 2003
 */
package com.gdo.mail.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import org.apache.commons.fileupload.FileItem;
import org.apache.poi.ss.formula.eval.NotImplementedException;

import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.CalculatedIntegerPropertySlot;
import com.gdo.stencils.slot.CalculatedStringPropertySlot;

/**
 * <p>
 * Mail attachement.
 * </p>
 */
public class FileUploadDataSourceStcl extends AttachmentStcl implements DataSource {

    public interface Slot extends AttachmentStcl.Slot {
        String CONTENT_TYPE = "ContentType";
        String SIZE = "Size";
    }

    private FileItem _fileItem; // file item uploaded

    public FileUploadDataSourceStcl(StclContext stclContext, FileItem item) {
        super(stclContext);
        _fileItem = item;

        new ContentTypeSlot(stclContext);
        new SizeSlot(stclContext);
    }

    @Override
    public String getName(StclContext stclContext, PStcl self) {
        return _fileItem.getName();
    }

    @Override
    public String getContentType() {
        return _fileItem.getContentType();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return _fileItem.getInputStream();
    }

    @Override
    public String getName() {
        return _fileItem.getName();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return _fileItem.getOutputStream();
    }

    private class ContentTypeSlot extends CalculatedStringPropertySlot<StclContext, PStcl> {

        public ContentTypeSlot(StclContext stclContext) {
            super(stclContext, FileUploadDataSourceStcl.this, Slot.CONTENT_TYPE);
        }

        @Override
        public String getValue(StclContext stclContext, PStcl self) {
            return FileUploadDataSourceStcl.this._fileItem.getContentType();
        }

        @Override
        public String setValue(StclContext stclContext, String value, PStcl self) {
            String msg = String.format("Cannot change %s value", getName());
            throw new NotImplementedException(msg);
        }

    }

    private class SizeSlot extends CalculatedIntegerPropertySlot<StclContext, PStcl> {

        public SizeSlot(StclContext stclContext) {
            super(stclContext, FileUploadDataSourceStcl.this, Slot.SIZE);
        }

        @Override
        public int getIntegerValue(StclContext stclContext, PStcl self) {
            return (int) FileUploadDataSourceStcl.this._fileItem.getSize();
        }

        @Override
        public int setIntegerValue(StclContext stclContext, int value, PStcl self) {
            String msg = String.format("Cannot change %s value", getName());
            throw new NotImplementedException(msg);
        }

    }

}