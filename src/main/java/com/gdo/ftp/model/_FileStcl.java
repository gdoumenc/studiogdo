/**
 * Copyright GDO - 2005
 */
package com.gdo.ftp.model;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import com.gdo.context.model.FileStcl;
import com.gdo.ftp.cmd.Delete;
import com.gdo.ftp.cmd.Rename;
import com.gdo.helper.StringHelper;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cond.PathCondition;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.CalculatedIntegerPropertySlot;
import com.gdo.stencils.slot.CalculatedStringPropertySlot;
import com.gdo.stencils.slot.MultiCalculatedSlot;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;
import com.gdo.util.XmlWriter;

/**
 * <p>
 * Common implementation class for FTP File and Folder.
 * </p>
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo & Guillaume Doumenc. Use
 * is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a>
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */
public class _FileStcl extends com.gdo.context.model._FileStcl {

    private static final int TIMEOUT = 5000;

    protected String _path; // relative path defined from initial directory
                            // context

    public _FileStcl(StclContext stclContext, String path) {
        super(stclContext);

        _path = path;

        // SLOT PART

        singleSlot(Slot.CONTEXT);
        new PathSlot(stclContext, this, Slot.PATH);
        new SizeSlot(stclContext, this, Slot.SIZE);
        new LastModifiedSlot(stclContext, this, Slot.LAST_MODIFIED);

        // COMMAND PART

        command(Command.DELETE, Delete.class);
        command(Command.RENAME, Rename.class);
    }

    @Override
    public String getName(StclContext stclContext, PStcl self) {
        return _path;
    }

    public class PathSlot extends CalculatedStringPropertySlot<StclContext, PStcl> {

        public PathSlot(StclContext stclContext, Stcl in, String name) {
            super(stclContext, in, name);
        }

        @Override
        public String getValue(StclContext stclContext, PStcl self) {
            return _FileStcl.this._path;
        }

        @Override
        public String setValue(StclContext stclContext, String value, PStcl self) {
            String old = _FileStcl.this._path;
            _FileStcl.this._path = value;
            return old;
        }
    }

    public class SizeSlot extends CalculatedIntegerPropertySlot<StclContext, PStcl> {

        public SizeSlot(StclContext stclContext, Stcl in, String name) {
            super(stclContext, in, name);
        }

        @Override
        public int getIntegerValue(StclContext stclContext, PStcl self) {
            PStcl container = self.getContainer(stclContext);
            FTPClient client = newClient(stclContext, container);
            FTPFile file = getFile(stclContext, client, container);
            int size = (file == null) ? -1 : (int) file.getSize();
            closeClient(stclContext, client, container);
            return size;
        }
    }

    public class LastModifiedSlot extends MultiCalculatedSlot<StclContext, PStcl> {
        public LastModifiedSlot(StclContext stclContext, Stcl in, String name) {
            super(stclContext, in, name, PSlot.ANY);
        }

        @Override
        protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
            PStcl container = self.getContainer();
            FTPClient client = newClient(stclContext, container);
            FTPFile file = getFile(stclContext, client, container);
            if (file == null) {
                closeClient(stclContext, client, container);
                return StencilUtils.<StclContext, PStcl> iterator();
            }

            Date lastModified = file.getTimestamp().getTime();
            String format = "hh'h'mm dd-MM-yy";
            if (cond instanceof PathCondition) {
                String pathCondition = ((PathCondition<StclContext, PStcl>) cond).getCondition();
                format = PathUtils.getKeyContained(pathCondition);
            }
            DateFormat dateFormat = new SimpleDateFormat(format, stclContext.getLocale());
            String date = dateFormat.format(lastModified);

            PStcl prop = self.getContainer().newPProperty(stclContext, (PSlot<StclContext, PStcl>) null, Key.NO_KEY, date);
            closeClient(stclContext, client, container);
            return StencilUtils.<StclContext, PStcl> iterator(stclContext, prop, self);
        }
    }

    /**
     * Connects to the FTP server if not already connected and sets (resets)
     * initial directory.
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            the stencil as a plugged stencil.
     * @return the connection status.
     */
    public FTPClient newClient(StclContext stclContext, PStcl self) {
        FTPClient client = new FTPClient();
        try {

            // gets the FTP context
            PStcl pftpContext = self.getStencil(stclContext, FileStcl.Slot.CONTEXT);
            if (StencilUtils.isNull(pftpContext)) {
                return null;
            }

            // gets connection parameters
            String host = pftpContext.getString(stclContext, FtpContextStcl.Slot.HOST, StringHelper.EMPTY_STRING);
            int port = pftpContext.getInt(stclContext, FtpContextStcl.Slot.PORT, 21);
            String user = pftpContext.getString(stclContext, FtpContextStcl.Slot.USER, StringHelper.EMPTY_STRING);
            String passwd = pftpContext.getString(stclContext, FtpContextStcl.Slot.PASSWD, StringHelper.EMPTY_STRING);

            // connects to server
            client.setConnectTimeout(TIMEOUT);
            client.connect(host, port);
            boolean connected = client.login(user, passwd);
            if (!connected) {
                client.disconnect();
                return null;
            }

            // sets connection parameters
            client.enterLocalPassiveMode();
            client.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
            client.setFileType(FTP.BINARY_FILE_TYPE);
            client.setSoTimeout(TIMEOUT);
            client.setDataTimeout(TIMEOUT);

            // _client.setKeepAlive(true);
            // _client.setControlKeepAliveReplyTimeout(TIMEOUT);
            // _client.setControlKeepAliveTimeout(2);

            // sets initial directory
            String dir = pftpContext.getString(stclContext, FtpContextStcl.Slot.DIR, StringHelper.EMPTY_STRING);
            if (StringUtils.isNotBlank(dir)) {
                boolean relative = pftpContext.getBoolean(stclContext, FtpContextStcl.Slot.RELATIVE_DIR);
                if (!relative) {
                    dir = PathUtils.compose(PathUtils.ROOT, dir);
                }
            }
            if (StringUtils.isNotBlank(dir)) {
                boolean changed = client.changeWorkingDirectory(dir);
                if (!changed) {
                    client.logout();
                    client.disconnect();
                    return null;
                }
            }

            // returns client
            return client;
        } catch (Exception e) {
            logWarn(stclContext, e.toString());
            closeClient(stclContext, client, self);
            return null;
        }
    }

    public FTPFile getFile(StclContext stclContext, FTPClient client, PStcl self) {
        try {
            FTPFile[] files = client.listFiles(_path);
            if (files == null || files.length != 1) {
                logWarn(stclContext, "no file %s in FTP context %s", _path, self);
                return null;
            } else {
                return files[0];
            }
        } catch (Exception e) {
            logWarn(stclContext, e.toString());
            return null;
        }
    }

    public void closeClient(StclContext stclContext, FTPClient client, PStcl self) {
        try {
            if (client.isConnected()) {
                client.logout();
                client.disconnect();
            }
        } catch (Exception e) {
            logWarn(stclContext, e.toString());
        }
    }

    /**
     * Adds file path as first parameter for constructor.
     * 
     * @param writer
     *            writer uses to output.
     */
    public void saveConstructorParameters(XmlWriter writer) throws IOException {
        writer.startElement("param");
        writer.writeAttribute("index", 0);
        writer.writeAttribute("type", "string");
        if (StringUtils.isNotBlank(_path)) {
            writer.writeCDATAAndEndElement(_path);
        } else {
            writer.writeCDATAAndEndElement(StringHelper.EMPTY_STRING);
        }
    }

}
