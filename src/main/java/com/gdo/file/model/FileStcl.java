/**
 * Copyright GDO - 2005
 */
package com.gdo.file.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.eval.NotImplementedException;

import com.gdo.file.cmd.CopyFile;
import com.gdo.file.cmd.DeleteFile;
import com.gdo.file.cmd.RenameFile;
import com.gdo.helper.StringHelper;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cond.PathCondition;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.faces.RenderContext;
import com.gdo.stencils.facet.FacetResult;
import com.gdo.stencils.facet.FacetType;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.CalculatedIntegerPropertySlot;
import com.gdo.stencils.slot.CalculatedStringPropertySlot;
import com.gdo.stencils.slot.MultiCalculatedSlot;
import com.gdo.stencils.slot.SingleCalculatedSlot;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;
import com.gdo.util.XmlWriter;

public class FileStcl extends com.gdo.context.model.FileStcl {

    public interface Slot extends com.gdo.context.model.FileStcl.Slot {
        String DIR_CONTAINER = "DirContainer";
    }

    private File _file;

    public FileStcl(StclContext stclContext, File file) {
        super(stclContext);

        this._file = file;

        new PathSlot(stclContext);
        new AbsolutePathSlot(stclContext);
        new LastModifiedSlot(stclContext);
        new DirContainerSlot(stclContext);

        new MimeTypeSlot(stclContext);
        new SizeSlot(stclContext);
        new ContentSlot(stclContext);

        // COMMAND PART

        command(Command.COPY, CopyFile.class);
        command(Command.RENAME, RenameFile.class);
        command(Command.DELETE, DeleteFile.class);
    }

    public FileStcl(StclContext stclContext, String path) {
        this(stclContext, new File(path));
    }

    @Override
    public String getName(StclContext stclContext, PStcl self) {
        File file = getFile(stclContext, self);
        if (file != null)
            return file.getName();
        return super.getName(stclContext, self);
    }

    // this method is public to allow commands to access it
    public File getFile(StclContext stclContext, PStcl self) {
        if (this._file == null) {
            String name = getName(stclContext, self);
            this._file = new File(name);
        }
        return this._file;
    }

    /**
     * @return The file context stencil from which this stencil was created.
     */
    protected PStcl getContext(StclContext stclContext, PStcl self) {
        return self.getStencil(stclContext, Slot.CONTEXT);
    }

    @Override
    protected void saveConstructorParameters(StclContext stclContext,
            XmlWriter writer, PStcl self) {
        try {
            writer.startElement("param");
            writer.writeAttribute("index", Integer.toString(0));
            writer.writeAttribute("type", "string");
            String path = self.getNotExpandedString(stclContext,
                    Slot.ABSOLUTE_PATH, null); // never
            // expand
            // when
            // saving
            if (StringUtils.isNotEmpty(path))
                writer.writeCDATAAndEndElement(path);
            else
                writer.writeCDATAAndEndElement(StringHelper.EMPTY_STRING);
        } catch (IOException e) {
            logError(stclContext, "Cannot save constructor parameters (%s)", e);
        }
    }

    private class PathSlot extends
            CalculatedStringPropertySlot<StclContext, PStcl> {
        public PathSlot(StclContext stclContext) {
            super(stclContext, FileStcl.this, Slot.PATH);
        }

        @Override
        public String getValue(StclContext stclContext, PStcl self) {
            PStcl file = self.getContainer(stclContext);
            try {
                if (true) {
                    File f = getFile(stclContext,
                            self.getContainer(stclContext));
                    if (f == null)
                        return StringHelper.EMPTY_STRING;
                    return f.getName();
                }
                @SuppressWarnings("unused")
                String path = file.getString(stclContext, Slot.ABSOLUTE_PATH,
                        StringHelper.EMPTY_STRING);
                if (StringUtils.isEmpty(path))
                    return StringHelper.EMPTY_STRING;
                PStcl context = file.getStencil(stclContext, Slot.CONTEXT);
                if (StencilUtils.isNull(context))
                    return StringHelper.EMPTY_STRING;
                String dir = context.getString(stclContext,
                        FileContextStcl.Slot.DIR, StringHelper.EMPTY_STRING);
                return path.substring(dir.length());
            } catch (Exception e) {
                logError(stclContext, "cannot get absolute path for %s (%s)",
                        file, e);
                return StringHelper.EMPTY_STRING;
            }
        }

        @Override
        public String setValue(StclContext stclContext, String value, PStcl self) {
            FileStcl.this._file = new File(value);
            return null;
        }
    }

    private class AbsolutePathSlot extends
            CalculatedStringPropertySlot<StclContext, PStcl> {
        public AbsolutePathSlot(StclContext stclContext) {
            super(stclContext, FileStcl.this, Slot.ABSOLUTE_PATH);
        }

        @Override
        public String getValue(StclContext stclContext, PStcl self) {
            File file = getFile(stclContext, self.getContainer(stclContext));
            if (file == null)
                return StringHelper.EMPTY_STRING;
            try {
                return file.getCanonicalPath();
            } catch (IOException e) {
                logError(stclContext, "cannot get path for %s (%s)",
                        file.getName(), e);
                return StringHelper.EMPTY_STRING;
            }
        }

        @Override
        public String setValue(StclContext stclContext, String value, PStcl self) {
            throw new NotImplementedException("Cannot change file path");
        }
    }

    // uses key as date format
    private class LastModifiedSlot extends
            MultiCalculatedSlot<StclContext, PStcl> {
        public LastModifiedSlot(StclContext stclContext) {
            super(stclContext, FileStcl.this, Slot.LAST_MODIFIED, PSlot.ANY);
        }

        @Override
        protected StencilIterator<StclContext, PStcl> getStencilsList(
                StclContext stclContext,
                StencilCondition<StclContext, PStcl> cond,
                PSlot<StclContext, PStcl> self) {

            // gets the associated file
            File file = getFile(stclContext, self.getContainer());
            if (file == null)
                return StencilUtils.< StclContext, PStcl> iterator();

            // if key defined, then used as format
            PStcl prop = null;
            if (cond instanceof PathCondition) {
                String pathCondition = ((PathCondition<StclContext, PStcl>) cond)
                        .getCondition();
                String format = PathUtils.getKeyContained(pathCondition);
                DateFormat dateFormat = new SimpleDateFormat(format,
                        stclContext.getLocale());
                Date lastModified = new Date(file.lastModified());
                String date = dateFormat.format(lastModified);
                self.getContainer().newPProperty(stclContext, self,
                        new Key<String>(format), date);
            } else {

                // if no key then returns integer value
                String date = Long.toString(file.lastModified());
                prop = self.getContainer().newPProperty(stclContext, self,
                        Key.NO_KEY, date);
            }

            return StencilUtils.< StclContext, PStcl> iterator(stclContext, prop, self);
        }
    }

    private class DirContainerSlot extends
            SingleCalculatedSlot<StclContext, PStcl> {
        public DirContainerSlot(StclContext stclContext) {
            super(stclContext, FileStcl.this, Slot.DIR_CONTAINER, PSlot.ONE);
        }

        @Override
        public boolean hasStencils(StclContext stclContext,
                StencilCondition<StclContext, PStcl> cond,
                PSlot<StclContext, PStcl> self) {
            File file = getFile(stclContext, self.getContainer());
            if (file == null)
                return false;
            return (file.getParentFile() != null);
        }

        @Override
        public PStcl getCalculatedStencil(StclContext stclContext,
                StencilCondition<StclContext, PStcl> cond,
                PSlot<StclContext, PStcl> self) {
            File file = getFile(stclContext, self.getContainer());
            if (file == null)
                return null;
            // File parent = file.getParentFile();
            // return createFile(stclContext, parent, Key.NO_KEY, self);
            return self.getContainer().nullPStencil(stclContext,
                    Result.error("DirContainerSlot"));
        }
    }

    @Override
    public FacetResult getFacet(RenderContext<StclContext, PStcl> renderContext) {
        StclContext stclContext = renderContext.getStencilContext();
        PStcl self = renderContext.getStencilRendered();

        // return file facet as content
        if (FacetType.FILE.equalsIgnoreCase(renderContext.getFacetType())) {
            InputStream is = self.getInputStream(stclContext, Slot.CONTENT);
            String mime = self.getString(stclContext, Slot.MIME_TYPE, "");
            return new FacetResult(is, mime);
        }

        return super.getFacet(renderContext);
    }

    @Override
    public void multipart(StclContext stclContext, String filename, FileItem item, PStcl self) {
        File file = getFile(stclContext, self);
        try {
            if (file.exists() && !file.canWrite()) {
                logError(stclContext, "cannot write in %s", file.getAbsolutePath());
                return;
            }
            InputStream input = item.getInputStream();
            OutputStream output = new FileOutputStream(file);
            IOUtils.copyLarge(input, output);
            output.close();
            input.close();
        } catch (Exception e) {
            if (file != null) {
                logError(stclContext, "cannot load file %s in %s", file.getAbsolutePath(), self);
            } else {
                logError(stclContext, "cannot get file in %s", self);
            }
        }
    }

    private class MimeTypeSlot extends
            CalculatedStringPropertySlot<StclContext, PStcl> {
        public MimeTypeSlot(StclContext stclContext) {
            super(stclContext, FileStcl.this, Slot.MIME_TYPE);
        }

        @Override
        public String getValue(StclContext stclContext, PStcl self) {
            File file = getFile(stclContext, self.getContainer(stclContext));
            if (file == null)
                return "";
            String mime = stclContext.getServletContext().getMimeType(
                    file.getName());
            return mime;
        }

        @Override
        public String setValue(StclContext stclContext, String value, PStcl self) {
            throw new NotImplementedException("Cannot set file mime type");
        }
    }

    private class SizeSlot extends
            CalculatedIntegerPropertySlot<StclContext, PStcl> {
        public SizeSlot(StclContext stclContext) {
            super(stclContext, FileStcl.this, Slot.SIZE);
        }

        @Override
        public int getIntegerValue(StclContext stclContext, PStcl self) {
            File file = getFile(stclContext, self.getContainer(stclContext));
            if (file == null)
                return -1;
            return (int) file.length();
        }

        @Override
        public int setIntegerValue(StclContext stclContext, int value,
                PStcl self) {
            throw new NotImplementedException("Cannot set file size");
        }
    }

    private class ContentSlot extends
            CalculatedStringPropertySlot<StclContext, PStcl> {
        public ContentSlot(StclContext stclContext) {
            super(stclContext, FileStcl.this, Slot.CONTENT);
        }

        @Override
        public InputStream getInputStream(StclContext stclContext, PStcl self) {
            File file = getFile(stclContext, self.getContainer(stclContext));
            if (file == null)
                return StringHelper.EMPTY_STRING_INPUT_STREAM;
            try {
                return new FileInputStream(file);
            } catch (IOException e) {
                logError(stclContext, "cannot read content from %s (%s)",
                        file.getName(), e);
                return StringHelper.EMPTY_STRING_INPUT_STREAM;
            }
        }

        @Override
        public String getValue(StclContext stclContext, PStcl self) {
            try {
                InputStream is = getInputStream(stclContext, self);
                StringWriter str = new StringWriter();
                IOUtils.copy(is, str);
                return str.toString();
            } catch (IOException e) {
                if (getLog().isErrorEnabled()) {
                    File file = getFile(stclContext,
                            self.getContainer(stclContext));
                    logError(stclContext, "cannot get value from %s (%s)",
                            file.getName(), e);
                }
                return "";
            }
        }

        @Override
        public String setValue(StclContext stclContext, String value, PStcl self) {
            File file = getFile(stclContext, self.getContainer(stclContext));
            if (file == null)
                return null;
            try {
                StringReader str = new StringReader(value);
                FileWriter writer = new FileWriter(file);
                IOUtils.copy(str, writer);
            } catch (IOException e) {
                logError(stclContext, "cannot write content to %s (%s)",
                        file.getName(), e);
            }
            return null;
        }
    }
}
