/**
 * Copyright GDO - 2004
 */
package com.gdo.project.model;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.gdo.helper.StringHelper;
import com.gdo.project.cmd.Connect;
import com.gdo.project.cmd.Trace;
import com.gdo.project.util.model.DateStcl;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.factory.StclFactory;
import com.gdo.stencils.factory.StencilFactory;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.prop.PropStencil;
import com.gdo.stencils.slot.CalculatedStringPropertySlot;
import com.gdo.stencils.slot.MultiCalculatedSlot;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;
import com.gdo.util.XmlWriter;

public class ServletStcl extends Stcl {

    public static boolean AUTO_SAVE = true;

    private static final String BACKUP_EXTENSION = "bak";
    private static final int BACKUP_NUMBER = 5;

    private static final String MULTI_POST_SEP = ":";

    public interface Slot extends Stcl.Slot {
        String SESSION = "Session";
        String LOCALE = "Locale";
        String TMP = "Tmp";
        String UTILS = "Utils";
        String USERS = "Users";
        String DATE = "Date";
        String REQUEST_URI = "RequestURI";
    }

    public interface Command extends Stcl.Command {
        String CONNECT = "Connect";
        String DISCONNECT = "Disconnect";
        String LOAD = "Load";
        String TRACE = "Trace";
    }

    public interface Resource {
        String DATE = "/Date";
        String DATE_NOW = "/Date/Now";

        String SESSION = "/Session";
    }

    public ServletStcl(StclContext stclContext) {
        super(stclContext);

        // SLOT PART

        multiSlot(Slot.USERS);
        multiSlot(Slot.TMP, PSlot.ANY, true, null);

        new SessionSlot(stclContext, this);
        new LocaleSlot(stclContext, this, Slot.LOCALE);
        new RequestURISlot(stclContext, this, Slot.REQUEST_URI);

        singleSlot(Slot.DATE, PSlot.ONE, true, null);

        // COMMAND PART

        command(Command.CONNECT, Connect.class);
        command(Command.TRACE, Trace.class);
    }

    @Override
    public void complete(StclContext stclContext, PStcl self) {
        super.complete(stclContext, self);
        self.newPStencil(stclContext, Slot.DATE, Key.NO_KEY, DateStcl.class);
    }

    /**
     * Called just after the project is loaded in servlet context.
     */
    public void afterLoaded(StclContext stclContext, PStcl self) {
        // no behavior by default
    }

    /**
     * Called just after a new session is created for this servlet stencil.
     * 
     * @param stclContext
     *            the stencil context.
     */
    public void afterSessionCreated(StclContext stclContext, PStcl self) {
        // no behavior by default
    }

    /**
     * @return The root servlet stencil is loaded from configuration file.
     */
    public static PStcl load(StclContext stclContext) throws IllegalStateException, IOException {
        StclFactory factory = (StclFactory) stclContext.getStencilFactory();
        ServletStcl servletStcl = null;

        // try to get from servlet file
        Reader in = null;
        try {
            in = confProjectReader(stclContext, null);
            servletStcl = (ServletStcl) factory.loadStencil(stclContext, in, "project.xml");
        } catch (Exception e) {
            // alert admin
            e.printStackTrace();

            // try to get from servlet file backups
            for (int i = 0; i < BACKUP_NUMBER; i++) {
                try {
                    in = confProjectReader(stclContext, BACKUP_EXTENSION + i);
                    servletStcl = (ServletStcl) factory.loadStencil(stclContext, in, "project.xml");
                    break;
                } catch (Exception e1) {
                    // in case of exception take the following one
                }
            }
        } finally {
            if (in != null)
                in.close();
        }

        // was not able to load the project
        if (servletStcl == null) {
            String home = stclContext.getConfigParameter(StclContext.PROJECT_CONF_DIR);
            String msg = String.format("Cannot create a project from %s", home);
            throw new IllegalStateException(msg);
        }

        // sets servlet as root and returns it
        PStcl servlet = servletStcl.self(stclContext, null);
        stclContext.setServletStcl(servlet);
        servletStcl.afterLoaded(stclContext, servlet);
        return servlet;
    }

    /**
     * @return The root servlet stencil is reloaded from configuration backup
     *         file.
     */
    public static PStcl reload(StclContext stclContext, String ext) throws Exception {

        // remove servlet stencil
        stclContext.setServletStcl(null);

        // copy a backup version as default configuration before reloading
        if (!StringUtils.isEmpty(ext)) {
            Reader in = null;
            Writer out = null;
            try {
                in = confProjectReader(stclContext, ext);
                out = confProjectWriter(stclContext, StringHelper.EMPTY_STRING);
                IOUtils.copy(in, out);
            } finally {
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
            }
        }

        // reloads it
        return load(stclContext);
    }

    /**
     * Saves the servlet configuration file and backups (using
     * BACKUP_EXTENSION).
     */
    public synchronized void save(StclContext stclContext) throws IOException {
        Writer out = null;

        // auto save project
        if (AUTO_SAVE) {

            // save previous backups
            saveBackups(stclContext, BACKUP_EXTENSION);

            // save first backup
            Reader in = null;
            try {
                in = confProjectReader(stclContext, StringHelper.EMPTY_STRING);
                out = confProjectWriter(stclContext, BACKUP_EXTENSION + "0");
                IOUtils.copy(in, out);
            } catch (Exception e) {
                logError(stclContext, "Cannot save first backup ", e);
            } finally {
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
            }

            // save servlet stencil
            try {
                StclFactory factory = (StclFactory) stclContext.getStencilFactory();
                out = confProjectWriter(stclContext, StringHelper.EMPTY_STRING);
                XmlWriter writer = new XmlWriter(out, 0);
                factory.saveStencil(stclContext, stclContext.getServletStcl(), writer);
                writer.close();
            } catch (Exception e) {
                logError(stclContext, "Cannot save servlet stencil", e);
            } finally {
                if (out != null)
                    out.close();
            }
        }
    }

    /**
     * Saves backups configuration files with specific extension (.back,
     * .session)
     */
    private void saveBackups(StclContext stclContext, String ext) throws IOException {

        // rotates files
        for (int i = BACKUP_NUMBER - 1; i >= 0; i--) {
            Reader in = null;
            Writer out = null;
            try {
                in = confProjectReader(stclContext, ext + i);
                out = confProjectWriter(stclContext, ext + (i + 1));
                IOUtils.copy(in, out);
            } catch (Exception e) {
                logError(stclContext, "Cannot save backup %s", i);
            } finally {
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
            }
        }
    }

    public String getMultiPostSep() {
        return MULTI_POST_SEP;
    }

    /**
     * @return complete file name of the servlet stencil configuration.
     */
    private static Reader confProjectReader(StclContext stclContext, String suffix) throws Exception {
        String fileName = getConfigurationFileName(stclContext, suffix);
        return new FileReader(new File(fileName));
    }

    private static Writer confProjectWriter(StclContext stclContext, String suffix) throws Exception {
        String fileName = getConfigurationFileName(stclContext, suffix);
        return new FileWriter(new File(fileName));
    }

    private static String getConfigurationFileName(StclContext stclContext, String suffix) {
        String home = stclContext.getConfigParameter(StclContext.PROJECT_CONF_DIR);
        String file = stclContext.getConfigParameter(StclContext.PROJECT_CONF_FILE);
        if (!StringUtils.isEmpty(suffix)) {
            file += "." + suffix;
        }
        return PathUtils.compose(home, file);
    }

    public class LocaleSlot extends MultiCalculatedSlot<StclContext, PStcl> {
        private List<PStcl> _values = new ArrayList<PStcl>();

        public LocaleSlot(StclContext stclContext, Stcl in, String name) {
            super(stclContext, in, name, PSlot.ANY);

            // create locale values : language and country
            PSlot<StclContext, PStcl> self = new PSlot<StclContext, PStcl>(this, null);
            Locale locale = stclContext.getLocale();
            StencilFactory<StclContext, PStcl> factory = (StencilFactory<StclContext, PStcl>) stclContext.getStencilFactory();
            PropStencil<StclContext, PStcl> prop = factory.createPropStencil(stclContext, locale.getLanguage());
            this._values.add(factory.newPPropStencil(stclContext, self, new Key<String>("language"), prop));
            prop = factory.createPropStencil(stclContext, locale.getCountry());
            this._values.add(factory.newPPropStencil(stclContext, self, new Key<String>("country"), prop));
        }

        @Override
        protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
            return StencilUtils.iterator(stclContext, this._values.iterator(), cond, self);
        }
    }

    private class RequestURISlot extends CalculatedStringPropertySlot<StclContext, PStcl> {

        public RequestURISlot(StclContext stclContext, Stcl in, String name) {
            super(stclContext, in, name);
        }

        @Override
        public String getValue(StclContext stclContext, PStcl self) throws Exception {
            return stclContext.getRequest().getRequestURI();
        }

    }
}