/**
 * Copyright GDO - 2005
 */
package com.gdo.ftp.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import com.gdo.ftp.cmd.CopyFile;
import com.gdo.helper.StringHelper;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils._Stencil;
import com.gdo.stencils.faces.RenderContext;
import com.gdo.stencils.facet.FacetResult;
import com.gdo.stencils.facet.FacetType;
import com.gdo.stencils.facet.IFacetInputStream;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.CalculatedStringPropertySlot;
import com.gdo.stencils.util.GlobalCounter;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;

public class FileStcl extends _FileStcl {

    public interface Slot extends com.gdo.context.model.FileStcl.Slot {
        String HTTP = "Http"; // http access to the ftp space (if exists)
    }

    public interface Command extends com.gdo.context.model.FileStcl.Command {
    }

    /**
     * Constructor used to save the file.
     */
    public FileStcl(StclContext stclContext, String path) {
        super(stclContext, path);

        // SLOT PART

        new MimeTypeSlot(stclContext, this, Slot.MIME_TYPE);
        new ContentSlot(stclContext, this, Slot.CONTENT);
        new HttpSlot(stclContext, this, Slot.HTTP);

        // COMMAND PART

        command(Command.COPY, CopyFile.class);
    }

    private class MimeTypeSlot extends CalculatedStringPropertySlot<StclContext, PStcl> {

        public MimeTypeSlot(StclContext stclContext, Stcl in, String name) {
            super(stclContext, in, name);
        }

        @Override
        public String getValue(StclContext stclContext, PStcl self) {
            return stclContext.getServletContext().getMimeType(FileStcl.this._path);
        }
    }

    public class ContentSlot extends CalculatedStringPropertySlot<StclContext, PStcl> {

        public ContentSlot(StclContext stclContext, Stcl in, String name) {
            super(stclContext, in, name);
        }

        @Override
        public String getValue(StclContext stclContext, PStcl self) {
            try {
                PStcl container = self.getContainer(stclContext);
                FTPClient client = newClient(stclContext, container);
                if (client == null) {
                    return "";
                }
                try {
                    InputStream is = client.retrieveFileStream(FileStcl.this._path);
                    if (!FTPReply.isPositivePreliminary(client.getReplyCode())) {
                        return "";
                    }
                    StringWriter sw = new StringWriter();
                    IOUtils.copy(is, sw);
                    if (!client.completePendingCommand()) {
                        return "";
                    }
                    return sw.toString();
                } finally {
                    closeClient(stclContext, client, container);
                }
            } catch (Exception e) {
                return e.toString();
            }
        }

        @Override
        public String setValue(StclContext stclContext, String value, PStcl self) {
            PStcl container = self.getContainer(stclContext);

            // gets the FTP context
            PStcl fileStcl = self.getContainer(stclContext);
            PStcl pftpContext = fileStcl.getStencil(stclContext, FileStcl.Slot.CONTEXT);
            if (StencilUtils.isNull(pftpContext)) {
                return StringHelper.EMPTY_STRING;
            }
            FtpContextStcl ftpContext = (FtpContextStcl) pftpContext.getReleasedStencil(stclContext);

            // sets content
            ByteArrayInputStream in = new ByteArrayInputStream(value.getBytes());
            ftpContext.put(stclContext, in, container.getName(stclContext), true, ".back", true, pftpContext);
            return value;
        }
    }

    @Override
    public FacetResult getFacet(RenderContext<StclContext, PStcl> renderContext) {
        StclContext stclContext = renderContext.getStencilContext();
        PStcl self = renderContext.getStencilRendered();
        String facet = renderContext.getFacetType();

        // read facet from generator if mask facet
        if (FacetType.FILE.equals(facet)) {

            // gets FTP client
            FTPClient client = newClient(stclContext, self);
            if (client == null) {
                return new FacetResult(FacetResult.ERROR, "no FTP client available for file facet", null);
            }

            // creates facet
            try {
                FTPFile file = getFile(stclContext, client, self);
                String mime = stclContext.getServletContext().getMimeType(file.getName());
                FacetResult result = new FacetResult(new FTPFacetInputStream(client), mime);
                result.setHeader("Content-Type", "application/octet-stream");
                result.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
                result.setContentLength(file.getSize());

                return result;
            } catch (Exception e) {
                closeClient(stclContext, client, self);
                logError(stclContext, e.toString());
                return new FacetResult(FacetResult.ERROR, e.toString(), null);
            }
        }
        return super.getFacet(renderContext);
    }

    @Override
    public void multipart(StclContext stclContext, String fileName, FileItem item, PStcl self) {

        // if no name creates one
        if (StringUtils.isBlank(_path)) {
            _path = GlobalCounter.uniqueID() + fileName;
        }

        // gets the FTP context
        PStcl container = self.getContainer(stclContext);
        PStcl pftpContext = container.getStencil(stclContext, FileStcl.Slot.CONTEXT);
        if (StencilUtils.isNull(pftpContext)) {
            logWarn(stclContext, "No FTP context defined for file multipart");
            return;
        }

        // gets FTP client
        FtpContextStcl ftpContext = (FtpContextStcl) pftpContext.getReleasedStencil(stclContext);
        FTPClient client = ftpContext.newClient(stclContext, pftpContext);
        if (client == null) {
            return;
        }

        // uploads file
        try {

            // sets folder
            String folderPath = PathUtils.getPathName(_path);
            if (StringUtils.isNotBlank(folderPath)) {
                client.changeWorkingDirectory(folderPath);
            }

            // backup previous file
            String path = PathUtils.getLastName(_path);
            client.rename(path, path.concat(".back"));

            // uploads file
            OutputStream out = client.storeFileStream(_path);
            InputStream in = item.getInputStream();
            IOUtils.copy(in, out);
            in.close();
            out.close();
            client.completePendingCommand();

            // check if extension has changed then renames the file
            String newExtension = "";
            if (fileName.lastIndexOf(".") != -1) {
                newExtension = fileName.substring(fileName.lastIndexOf("."));
            }
            String oldExtension = "";
            if (path.lastIndexOf(".") != -1) {
                oldExtension = path.substring(path.lastIndexOf("."));
            }

            // if changed, then change name
            if (!oldExtension.equals(newExtension)) {
                String newPath = StringHelper.substringEnd(path, oldExtension.length()) + newExtension;

                // renames file
                client.rename(path, newPath);
                client.completePendingCommand();

                // updates file path (compose new name from path)
                _path = PathUtils.compose(folderPath, newPath);

            }
        } catch (Exception e) {
            logError(stclContext, e.toString());
        } finally {
            closeClient(stclContext, client, self);
        }
    }

    private class FTPFacetInputStream implements IFacetInputStream {

        private FTPClient _client;
        private InputStream _input;

        public FTPFacetInputStream(FTPClient client) {
            _client = client;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            _input = _client.retrieveFileStream(FileStcl.this._path);
            return _input;
        }

        @Override
        public void closeInputStream() throws IOException {
            // _client.completePendingCommand();
            if (_input != null) {
                _input.close();
            }
            _client.logout();
            _client.disconnect();
        }
    }

    private class HttpSlot extends CalculatedStringPropertySlot<StclContext, PStcl> {

        public HttpSlot(StclContext stclContext, _Stencil<StclContext, PStcl> in, String name) {
            super(stclContext, in, name);
        }

        @Override
        public String getValue(StclContext stclContext, PStcl self) throws Exception {
            PStcl context = self.getContainer(stclContext).getStencil(stclContext, Slot.CONTEXT);

            String http_dir = context.getString(stclContext, FtpContextStcl.Slot.HTTP_DIR);
            return String.format("%s/%s", http_dir, FileStcl.this._path);
        }

    }
}
