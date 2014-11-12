/**
 * Copyright GDO - 2003
 */
package com.gdo.mail.model;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import com.gdo.stencils.Result;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.descriptor._SlotDescriptor;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.MultiCalculatedSlot;
import com.gdo.stencils.slot._Slot;
import com.gdo.stencils.util.StencilUtils;

public class FolderStcl extends com.gdo.context.model.FolderStcl {

    private Folder _folder;

    public FolderStcl(StclContext stclContext, Folder folder) {
        super(stclContext);
        _folder = folder;

        singleSlot(Slot.CONTEXT);
        singleSlot(Slot.PARENT);

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
    }

    // this method is public to allow commands to access it
    public Folder getFolder(StclContext stclContext, PStcl self) {
        if (_folder == null) {
            PStcl parent = self.getStencil(stclContext, Slot.PARENT);
            Folder f = ((FolderStcl) parent.getReleasedStencil(stclContext)).getFolder(stclContext, parent);
            try {
                String name = getName(stclContext, self);
                _folder = f.getFolder(name);
            } catch (MessagingException e) {
            }
        }
        return _folder;
    }

    private class FilesOnlySlot extends MultiCalculatedSlot<StclContext, PStcl> {
        public FilesOnlySlot(StclContext stclContext, Stcl in, String name) {
            super(stclContext, in, name, PSlot.ANY);
        }

        @Override
        protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
            try {
                Folder folder = getFolder(stclContext, self.getContainer());
                if (folder == null)
                    return StencilUtils.<StclContext, PStcl> iterator(Result.error("cannot connect"));

                String fileTemplate = self.getContainer().getString(stclContext, Slot.FILE_TEMPLATE, "");

                // try to open read/write and if that fails try read-only
                try {
                    folder.open(Folder.READ_WRITE);
                } catch (MessagingException e) {
                    try {
                        folder.open(Folder.READ_ONLY);
                    } catch (MessagingException ex) {
                        String msg = logWarn(stclContext, "cannot get mail folder : %s", e);
                        return StencilUtils.<StclContext, PStcl> iterator(Result.error(msg));
                    }
                }

                // for all messages
                List<PStcl> list = null;
                Message[] msgs = folder.getMessages();
                if (msgs != null) {
                    list = new ArrayList<PStcl>(msgs.length);
                    for (Message msg : msgs) {
                        IKey key = new Key(msg.getMessageNumber());

                        // create mail stencil
                        PStcl mail = self.getContainer().newPStencil(stclContext, self, key, fileTemplate, msg);
                        if (StencilUtils.isNull(mail))
                            continue;

                        // set same mail context
                        mail.plug(stclContext, self.getContainer(), MailStcl.Slot.MAIL_CONTEXT);

                        // add it in list
                        list.add(mail);
                    }
                }
                folder.close(true);
                folder.getStore().close();

                if (list == null)
                    return StencilUtils.<StclContext, PStcl> iterator();
                return StencilUtils.<StclContext, PStcl> iterator(stclContext, list.iterator(), cond, self);
            } catch (Exception e) {
                logError(stclContext, e.toString());
                return StencilUtils.<StclContext, PStcl> iterator(Result.error(e.getMessage()));
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
                Folder folder = getFolder(stclContext, self.getContainer());
                if (folder == null)
                    return StencilUtils.<StclContext, PStcl> iterator(Result.error("cannot connect"));

                String folderTemplate = self.getContainer().getString(stclContext, Slot.FOLDER_TEMPLATE, "");

                // try to open read/write and if that fails try read-only
                try {
                    folder.open(Folder.READ_WRITE);
                } catch (MessagingException e) {
                    try {
                        folder.open(Folder.READ_ONLY);
                    } catch (MessagingException ex) {
                        logWarn(stclContext, "cannot get mail folder (%s)", e);
                        return StencilUtils.<StclContext, PStcl> iterator(Result.error("cannot get mail folder"));
                    }
                }

                // for all sub folder
                if ((folder.getType() & Folder.HOLDS_FOLDERS) != 0) {
                    for (Folder f : folder.list("%")) {
                        IKey key = new Key(f.getName());

                        // if already in list, do nothing
                        if (getStencilFromList(stclContext, key, self) != null) {
                            keepStencilInList(stclContext, key, self);
                            continue;
                        }

                        // create folder stencil
                        PStcl stcl = self.getContainer().newPStencil(stclContext, self, key, folderTemplate, f);
                        if (StencilUtils.isNull(stcl))
                            continue;

                        // set same mail context
                        stcl.plug(stclContext, self.getContainer(), Slot.CONTEXT);

                        // add it in list
                        addStencilInList(stclContext, stcl, self);
                    }
                }
                folder.close(true);
                folder.getStore().close();
                return cleanList(stclContext, condition, self);
            } catch (Exception e) {
                logError(stclContext, e.toString());
                return StencilUtils.<StclContext, PStcl> iterator(Result.error(e.getMessage()));
            }
        }
    }

    private class GetSlot extends MultiCalculatedSlot<StclContext, PStcl> {
        public GetSlot(StclContext stclContext) {
            super(stclContext, FolderStcl.this, Slot.GET, PSlot.ANY);
        }

        @Override
        protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> condition, PSlot<StclContext, PStcl> self) {
            /*
             * try { // cannot accept no path condition as key contained if
             * (!(condition instanceof PathCondition)) return StencilUtils.iterator();
             * String path = ((PathCondition<StclContext, PStcl>)
             * condition).getCondition(); if (!PathUtils.isKeyContained(path)) return
             * StencilUtils.iterator(); // get name File dir = getFile(stclContext,
             * self.getContainer(stclContext)); String key =
             * PathUtils.getKeyContained(path); boolean isComposed =
             * PathUtils.isComposed(key); // create filter String name = key; String
             * tail = ""; if (isComposed) { name = PathUtils.getFirstName(key); tail =
             * PathUtils.getTailName(key); } FileFilter filter = new PathFilter(name);
             * // create resulting list ArrayList<PStcl> list = new
             * ArrayList<PStcl>(); for (File file : dir.listFiles(filter)) { if
             * (file.isDirectory()) { if (!isComposed) continue;
             * StencilFactory<StclContext, PStcl> factory =
             * (StencilFactory<StclContext, PStcl>) stclContext.<StclContext, PStcl>
             * getStencilFactory(); PStcl stcl = factory.newPStencil(stclContext,
             * self, Key.NO_KEY, DirStcl.class.getName(), file);
             * StencilIterator<StclContext, PStcl> iter =
             * stcl.getStencils(stclContext, PathUtils.addKey(Slot.GET, tail)); for
             * (PStcl s : iter) { list.add(s); } } if (file.isFile()) { if
             * (isComposed) continue; StencilFactory<StclContext, PStcl> factory =
             * (StencilFactory<StclContext, PStcl>) stclContext.<StclContext, PStcl>
             * getStencilFactory(); PStcl stcl = factory.newPStencil(stclContext,
             * self, Key.NO_KEY, FileStcl.class.getName(), file); list.add(stcl); } }
             * return StencilUtils.iterator(stclContext, list,
             * StencilCondition.<StclContext, PStcl> NONE(), self); } catch (Exception
             * e) { throw new StencilException("Cannot get files list", e); }
             */
            return StencilUtils.<StclContext, PStcl> iterator();
        }
    }
}