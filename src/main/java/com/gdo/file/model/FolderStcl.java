/**
 * Copyright GDO - 2005
 */
package com.gdo.file.model;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import com.gdo.file.cmd.CopyDir;
import com.gdo.file.cmd.CreateDir;
import com.gdo.file.cmd.CreateFile;
import com.gdo.file.cmd.DeleteFile;
import com.gdo.file.cmd.RenameFile;
import com.gdo.stencils.Result;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cond.PathCondition;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.descriptor._SlotDescriptor;
import com.gdo.stencils.factory.StclFactory;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.MultiCalculatedSlot;
import com.gdo.stencils.slot._Slot;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;

public class FolderStcl extends com.gdo.context.model.FolderStcl {

    public interface Slot extends com.gdo.context.model.FolderStcl.Slot {
    }

    public interface Command extends com.gdo.context.model.FolderStcl.Command {
    }

    private File _dir;

    public FolderStcl(StclContext stclContext) {
        this(stclContext, (File) null);
    }

    public FolderStcl(StclContext stclContext, String path) {
        this(stclContext, new File(path));
    }

    public FolderStcl(StclContext stclContext, File dir) {
        super(stclContext);
        _dir = dir;

        propSlot(Slot.FOLDER_TEMPLATE, FolderStcl.class.getName());
        propSlot(Slot.FILE_TEMPLATE, FileStcl.class.getName());

        addDescriptor(Slot.FILES, new _SlotDescriptor<StclContext, PStcl>() {
            @Override
            public _Slot<StclContext, PStcl> add(StclContext stclContext, String name, PStcl self) {
                return new FilesSlot(stclContext, FolderStcl.this, name);
            }
        });
        addDescriptor(Slot.FILES_ONLY, new _SlotDescriptor<StclContext, PStcl>() {
            @Override
            public _Slot<StclContext, PStcl> add(StclContext stclContext, String name, PStcl self) {
                return new FilesOnlySlot(stclContext, FolderStcl.this, name);
            }
        });
        addDescriptor(Slot.FOLDERS_ONLY, new _SlotDescriptor<StclContext, PStcl>() {
            @Override
            public _Slot<StclContext, PStcl> add(StclContext stclContext, String name, PStcl self) {
                return new FoldersOnlySlot(stclContext, FolderStcl.this, name);
            }
        });
        new GetSlot(stclContext);

        command(Command.CREATE_FILE, CreateFile.class);
        command(Command.CREATE_FOLDER, CreateDir.class);
        command(Command.COPY, CopyDir.class);
        command(Command.RENAME, RenameFile.class);
        command(Command.DELETE, DeleteFile.class);
    }

    /**
     * @return the java file associated.
     */
    public File getFile(StclContext stclContext, PStcl self) {
        if (_dir == null || !_dir.isDirectory())
            return null;
        return _dir;
    }

    /**
     * @return The file stencil created from a java file instance.
     */
    protected PStcl createFile(StclContext stclContext, File file, IKey key, PSlot<StclContext, PStcl> self) {
        StclFactory factory = (StclFactory) stclContext.getStencilFactory();

        // gets the template name
        String template = self.getContainer().getString(stclContext, Slot.FILE_TEMPLATE, "");
        if (StringUtils.isEmpty(template)) {
            return Stcl.nullPStencil(stclContext, Result.error("File template property undefined"));
        }

        // creates the stencil
        PStcl stcl = factory.createPStencil(stclContext, self, key, template, file);
        if (StencilUtils.isNull(stcl))
            return stcl;

        // plugs context in it
        PStcl context = self.getContainer().getStencil(stclContext, Slot.CONTEXT);
        stcl.plug(stclContext, context, Slot.CONTEXT);

        // returns created stencil
        return stcl;
    }

    /**
     * @return The folder stencil created from a java folder instance.
     */
    protected PStcl createFolder(StclContext stclContext, File file, IKey key, PSlot<StclContext, PStcl> self) {
        StclFactory factory = (StclFactory) stclContext.getStencilFactory();

        // gets the template name
        String template = self.getContainer().getString(stclContext, Slot.FOLDER_TEMPLATE, "");
        if (StringUtils.isEmpty(template)) {
            return Stcl.nullPStencil(stclContext, Result.error("Folder template property undefined"));
        }

        // creates the stencil
        PStcl stcl = factory.createPStencil(stclContext, self, key, template, file);
        if (StencilUtils.isNull(stcl))
            return stcl;

        // plug context in it
        PStcl context = self.getContainer().getStencil(stclContext, Slot.CONTEXT);
        stcl.plug(stclContext, context, Slot.CONTEXT);

        // returns created stencil
        return stcl;
    }

    private class FilesSlot extends MultiCalculatedSlot<StclContext, PStcl> {
        public FilesSlot(StclContext stclContext, Stcl in, String name) {
            super(stclContext, in, name, PSlot.ANY);
        }

        @Override
        protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> condition, PSlot<StclContext, PStcl> self) {
            try {
                File dir = getFile(stclContext, self.getContainer());
                if (dir == null)
                    return StencilUtils.<StclContext, PStcl> iterator();

                // for all files
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        IKey key = new Key(file.getName());

                        // if already in list, do nothing
                        if (getStencilFromList(stclContext, key, self) != null) {
                            keepStencilInList(stclContext, key, self);
                            continue;
                        }

                        // create file stencil
                        PStcl stcl = null;
                        if (file.isDirectory())
                            stcl = createFolder(stclContext, file, key, self);
                        else
                            stcl = createFile(stclContext, file, key, self);

                        // add it in list
                        addStencilInList(stclContext, stcl, self);
                    }
                }
                return cleanList(stclContext, condition, self);
            } catch (Exception e) {
                String msg = logWarn(stclContext, "Cannot get files list", e);
                return StencilUtils.<StclContext, PStcl> iterator(Result.error(msg));
            }
        }
    }

    private class FilesOnlySlot extends MultiCalculatedSlot<StclContext, PStcl> {
        public FilesOnlySlot(StclContext stclContext, Stcl in, String name) {
            super(stclContext, in, name, PSlot.ANY);
        }

        @Override
        protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> condition, PSlot<StclContext, PStcl> self) {
            try {
                File dir = getFile(stclContext, self.getContainer());
                if (dir == null) {
                    String msg = logWarn(stclContext, "Cannot get root dir");
                    return StencilUtils.<StclContext, PStcl> iterator(Result.error(msg));
                }

                String nom = PathCondition.getKeyCondition(condition);
                if (StringUtils.isNotBlank(nom)) {
                    FilenameFilter filter = new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return name.equals(nom);
                        }
                    };
                    File[] files = dir.listFiles(filter);
                    if (files.length > 0) {
                        PStcl stcl = createFile(stclContext, files[0], new Key(nom), self);
                        return StencilUtils.iterator(stclContext, stcl, self);
                    }
                    return StencilUtils.<StclContext, PStcl> iterator();
                }

                // for all files
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        IKey key = new Key(file.getName());

                        // if already in list, do nothing
                        if (getStencilFromList(stclContext, key, self) != null) {
                            keepStencilInList(stclContext, key, self);
                            continue;
                        }

                        // create file stencil
                        if (!file.isDirectory()) {
                            PStcl stcl = createFile(stclContext, file, key, self);
                            addStencilInList(stclContext, stcl, self);
                        }
                    }
                }
                return cleanList(stclContext, condition, self);
            } catch (Exception e) {
                String msg = logWarn(stclContext, "Cannot get files list", e);
                return StencilUtils.<StclContext, PStcl> iterator(Result.error(msg));
            }
        }
    }

    private class FoldersOnlySlot extends MultiCalculatedSlot<StclContext, PStcl> {
        public FoldersOnlySlot(StclContext stclContext, Stcl in, String name) {
            super(stclContext, in, name, PSlot.ANY);
        }

        @Override
        protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> condition, PSlot<StclContext, PStcl> self) {
            try {
                File dir = getFile(stclContext, self.getContainer());
                if (dir == null) {
                    String msg = logWarn(stclContext, "Cannot get root dir");
                    return StencilUtils.<StclContext, PStcl> iterator(Result.error(msg));
                }

                // for all files
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        IKey key = new Key(file.getName());

                        // if already in list, do nothing
                        if (getStencilFromList(stclContext, key, self) != null) {
                            keepStencilInList(stclContext, key, self);
                            continue;
                        }

                        // create file stencil
                        if (file.isDirectory()) {
                            PStcl stcl = createFolder(stclContext, file, key, self);
                            addStencilInList(stclContext, stcl, self);
                        }
                    }
                }
                return cleanList(stclContext, condition, self);
            } catch (Exception e) {
                String msg = logWarn(stclContext, "Cannot get files list", e);
                return StencilUtils.<StclContext, PStcl> iterator(Result.error(msg));
            }
        }
    }

    private class GetSlot extends MultiCalculatedSlot<StclContext, PStcl> {
        public GetSlot(StclContext stclContext) {
            super(stclContext, FolderStcl.this, Slot.GET, PSlot.ANY);
        }

        @Override
        protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> condition, PSlot<StclContext, PStcl> self) {
            try {

                // cannot accept no path condition as key contained
                if (!(condition instanceof PathCondition))
                    return StencilUtils.<StclContext, PStcl> iterator();
                String path = ((PathCondition<StclContext, PStcl>) condition).getCondition();
                if (!PathUtils.isKeyContained(path))
                    return StencilUtils.<StclContext, PStcl> iterator();
                path = PathUtils.getKeyContained(path);

                // get initial directory for searching the file
                File dir;
                if (path.startsWith(PathUtils.ROOT)) {
                    dir = new File(PathUtils.ROOT);
                    path = path.substring(1);
                } else {
                    dir = getFile(stclContext, self.getContainer());
                }
                if (dir == null) {
                    return StencilUtils.<StclContext, PStcl> iterator(Result.error("dir not found"));
                }

                // get name
                boolean isComposed = PathUtils.isComposed(path);

                // create filter
                String name = path;
                String tail = "";
                if (isComposed) {
                    name = PathUtils.getFirstName(path);
                    tail = PathUtils.getTailName(path);
                }
                if (StringUtils.isBlank(name)) {
                    String msg = logWarn(stclContext, "Cannot get files list without key");
                    return StencilUtils.<StclContext, PStcl> iterator(Result.error(msg));
                }
                FileFilter filter = new PathFilter(name);

                // create resulting list
                ArrayList<PStcl> list = new ArrayList<PStcl>();
                for (File file : dir.listFiles(filter)) {
                    if (file.isDirectory()) {
                        if (!isComposed)
                            continue;
                        PStcl stcl = createFolder(stclContext, file, Key.NO_KEY, self);
                        StencilIterator<StclContext, PStcl> iter = stcl.getStencils(stclContext, PathUtils.createPath(Slot.GET, tail));
                        for (PStcl s : iter) {
                            list.add(s);
                        }
                    }
                    if (file.isFile()) {
                        if (isComposed)
                            continue;
                        PStcl stcl = createFile(stclContext, file, Key.NO_KEY, self);
                        list.add(stcl);
                    }
                }

                return StencilUtils.<StclContext, PStcl> iterator(stclContext, list.iterator(), null, self);
            } catch (Exception e) {
                String msg = logWarn(stclContext, "Cannot get files list", e);
                return StencilUtils.<StclContext, PStcl> iterator(Result.error(msg));
            }
        }
    }

    private class PathFilter implements FileFilter {
        private String _path;

        public PathFilter(String path) {
            _path = path;
        }

        @Override
        public boolean accept(File pathname) {
            return _path.matches(pathname.getName());
        }
    }
}
