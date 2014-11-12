/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils;

import java.io.File;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

import com.gdo.project.model.ServletStcl;
import com.gdo.project.model.SessionStcl;
import com.gdo.project.slot._SlotCursor;
import com.gdo.servlet.RpcArgs;
import com.gdo.servlet.StudioGdoServlet;
import com.gdo.stencils.atom.Atom;
import com.gdo.stencils.factory.IStencilFactory;
import com.gdo.stencils.factory.StclFactory;
import com.gdo.stencils.log.StencilLog;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.StencilUtils;

/**
 * <p>
 * A project context includes http infos and my faces context.
 * </p>
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo & Guillaume Doumenc. Use
 * is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */
public class StclContext extends _StencilContext {

    // webapp properties
    public static final String PROJECT_NAME = "com.gdo.project.name";
    public static final String PROJECT_CONF_FILE = "com.gdo.project.conf.file";

    public static final String PROJECT_CONF_DIR = "com.gdo.project.conf.dir";
    public static final String PROJECT_TMP_DIR = "com.gdo.project.tmp.dir";
    // public static final String PROJECT_WWW_DIR = "com.gdo.project.www.dir";
    public static final String PROJECT_SKEL_DIR = "com.gdo.project.skel.dir";

    public static final String AUTO_SAVE = "com.gdo.project.auto.save";
    public static final String CURSOR_STRATEGY = "com.gdo.project.cursor.strategy";

    public static final String FILE_UPLOAD_THRESHOLD = "com.gdo.project.file.upload.threshold";
    public static final String PROJECT_MAIL_SERVER = "com.gdo.project.mail.server";
    public static final String JDBC_URL = "com.gdo.project.jdbc.url";
    public static final String JDBC_USERNAME = "com.gdo.project.jdbc.user";
    public static final String JDBC_PASSWORD = "com.gdo.project.jdbc.password";

    // default context used when no context defined
    private static StclContext DEFAULT_CONTEXT;

    // factory used to create stencils
    private static final StclFactory FACTORY = new StclFactory();

    // servlet request and response
    private HttpServletRequest _request;
    private HttpServletResponse _response;

    // the session (must be saved as upload can be done from another session (FF
    // issue)) TODO BADLY RESOLVED
    private HttpSession _session;
    private RpcArgs _args;
    private int _id;

    private RpcArgs _request_parameters;

    /**
     * Returns a default stencil context which may be used in case of no stencil
     * context defined.
     * 
     * @return a stencil context.
     */
    public static StclContext defaultContext() {
        return DEFAULT_CONTEXT;
    }

    /**
     * Loads the project stencil if not already loaded, and creates the session
     * stencil associated.
     * 
     * @throws Exception
     */
    public StclContext(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession session = request.getSession();

        // stores last stencil context as default one
        DEFAULT_CONTEXT = this;

        // sets servlet entries
        _request = request;
        _response = response;

        // set stencil context id
        _id = Atom.uniqueInt();

        // set request arguments
        _args = new RpcArgs(this);

        // loads project if not already loaded (only one load at a time)
        PStcl servletStcl = getServletStcl();
        if (StencilUtils.isNull(servletStcl)) {
            servletStcl = loadServlet(request);
        }

        // creates session if the session was removed
        if (!SessionStcl.HTTP_SESSIONS.containsKey(session.getId())) {

            // creates session stencil
            SessionStcl.createSessionStcl(this);

            // handler on after session created
            ((ServletStcl) servletStcl.getReleasedStencil(this)).afterSessionCreated(this, servletStcl);
        }
    }

    // the factory is stored in the servlet context
    @Override
    @SuppressWarnings("unchecked")
    public IStencilFactory<StclContext, PStcl> getStencilFactory() {
        return FACTORY;
    }

    /**
     * Returns the servlet request.
     * 
     * @return the servlet request.
     */
    public final HttpServletRequest getRequest() {
        checkValidity();
        return _request;
    }

    /**
     * Returns the servlet response.
     * 
     * @return the servlet response.
     */
    public final HttpServletResponse getResponse() {
        return _response;
    }

    public final RpcArgs getRpcArgs() {
        return _args;
    }

    /**
     * Returns the locale used for this session.
     * 
     * @return the locale used for this session.
     */
    @Override
    public Locale getLocale() {

        // if the locale is not set then session one (if defined) may be used
        if (_locale == null) {
            HttpSession session = getHttpSession();
            Locale locale = (Locale) session.getAttribute(StudioGdoServlet.LOCALE_ENTRY);
            if (locale != null) {
                return locale;
            }
        }

        // return default locale
        return super.getLocale();
    }

    /**
     * Returns the servlet session used for this context.
     * 
     * @return the servlet session used for this context.
     */
    public final HttpSession getHttpSession() {
        if (_session == null) {
            HttpServletRequest request = getRequest();
            if (request == null) {
                return null;
            }
            _session = request.getSession();
        }
        return _session;
    }

    /**
     * Stores the
     * 
     * @param session
     */
    public final void setHttpSession(HttpSession session) {
        _session = session;
    }

    /**
     * Returns the servlet context used for this context.
     * 
     * @return the servlet context used for this context.
     */
    public final ServletContext getServletContext() {
        HttpSession session = getHttpSession();
        return (session == null) ? null : session.getServletContext();
    }

    /**
     * @return servlet stencil from servlet context (one stencil per servlet
     *         context).
     */
    public PStcl getServletStcl() {
        ServletContext context = getServletContext();
        return (PStcl) context.getAttribute(ServletStcl.class.getName());
    }

    /**
     * Stores servlet stencil from servlet context (one stencil per servlet
     * context).
     */
    public void setServletStcl(PStcl servletStcl) {
        ServletContext context = getServletContext();
        context.setAttribute(ServletStcl.class.getName(), servletStcl);
    }

    /**
     * Gets the request parameters as a decomposed structure.
     * 
     * @return the request parameters as a decomposed structure.
     */
    public RpcArgs getRequestParameters() {
        return _request_parameters;
    }

    /**
     * Sets the request parameters.
     * 
     * @param params
     *            the request parameters.
     */
    public void setRequestParameters(RpcArgs params) {
        _request_parameters = params;
    }

    public int getTransactionId() {
        int ti = _args.getTransactionId();
        if (ti > 0)
            return ti;
        return getId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gdo.stencils.StencilContext#getName()
     */
    @Override
    public String getName() {
        ServletContext servContext = getServletContext();
        return servContext.getServletContextName();
    }

    public String getConfigParameter(String key) {
        ServletContext servContext = getServletContext();

        // project name
        if (PROJECT_NAME.equals(key)) {
            return servContext.getServletContextName();
        }

        // others parameters
        return servContext.getInitParameter(key);
    }

    /**
     * Returns the login of the user making this request, if the user has been
     * authenticated, or <tt>null</tt> if the user has not been authenticated.
     * 
     * @return the login of the user making this request, if the user has been
     *         authenticated, or <tt>null</tt> if the user has not been
     *         authenticated.
     */
    public String getRemoteUser() {
        HttpServletRequest request = getRequest();
        return request.getRemoteUser();
    }

    /**
     * Returns a boolean indicating whether the authenticated user is included
     * in the specified logical "role".
     * 
     * @param role
     *            the name of the role
     * @return a <tt>boolean</tt> indicating whether the user making this
     *         request belongs to a given role; <tt>false</tt> if the user has
     *         not been authenticated
     */
    public boolean isUserInRole(String role) {
        HttpServletRequest request = getRequest();
        return request.isUserInRole(role);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gdo.stencils.StencilContext#getTemplatePathes()
     */
    @Override
    public String[] getTemplatePathes() {
        checkValidity();
        ServletContext servletContext = getServletContext();
        if (servletContext != null) {
            String p1 = servletContext.getRealPath("WEB-INF/classes");
            String p2 = servletContext.getRealPath("../build/classes");
            return new String[] { p1, p2 };
        }
        return super.getTemplatePathes();
    }

    public int getId() {
        return _id;
    }

    /**
     * Releases the current session. Once released this session cannot be used
     * anymore.
     */
    @Override
    public void release() {
        _request.getSession().invalidate();
        super.release();
    }

    @Override
    protected void checkValidity() {

        // _request or _response may be null
        /*
         * if (_request != null && _request.isRequestedSessionIdValid()) {
         * throw new IllegalStateException("Session no more valid"); } if
         * (_response != null && _response.isCommitted()) { throw new
         * IllegalStateException("Response already committed"); }
         */
        super.checkValidity();
    }

    private synchronized PStcl loadServlet(HttpServletRequest request) throws Exception {
        HttpSession session = request.getSession();
        ServletContext servletContext = session.getServletContext();
        String context = servletContext.getServletContextName();

        // set project auto-save
        String autosave = getConfigParameter(StclContext.AUTO_SAVE);
        if (StringUtils.isNotBlank(autosave))
            ServletStcl.AUTO_SAVE = Boolean.parseBoolean(autosave);

        // set cursor strategy
        String strategy = getConfigParameter(StclContext.CURSOR_STRATEGY);
        if (StringUtils.isNotBlank(strategy))
            _SlotCursor.STRATEGY = Integer.parseInt(strategy);

        // clean tmp dir
        String tmpDir = getConfigParameter(StclContext.PROJECT_TMP_DIR);
        if (StringUtils.isNotBlank(tmpDir)) {
            File tmp = new File(tmpDir);
            if (!tmp.exists()) {
                logWarn(this, "cannot get temporary directory : %s", tmpDir);
            } else {
                String[] files = tmp.list();
                if (files == null) {
                    logWarn(this, "cannot read temporary directory : %s", tmpDir);
                } else {
                    for (String file : files) {
                        File f = new File(tmp, file);
                        if (!f.delete()) {
                            logWarn(this, "cannot delete temporary file : %s", f.getAbsolutePath());
                        }
                    }
                }
            }
        }

        // loads project
        logWarn(this, "Loading project %s", context);
        PStcl servletStcl = ServletStcl.load(this);
        setServletStcl(servletStcl);
        logWarn(this, "Project %s loaded", context);
        return servletStcl;
    }

    //
    // LOG PART
    //

    private static final StencilLog LOG = new StencilLog(StclContext.class);

    private static final void logWarn(StclContext stclContext, String format, Object... params) {
        if (LOG.isWarnEnabled()) {
            String msg = String.format(format, params);
            LOG.warn(stclContext, msg);
        }
    }

}
