/**
 * Copyright GDO - 2005
 */

package com.gdo.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.Principal;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.catalina.realm.GenericPrincipal;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.gdo.helper.ClassHelper;
import com.gdo.helper.StringHelper;
import com.gdo.project.model.ServletStcl;
import com.gdo.project.model.SessionStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils._Stencil;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.log.StencilLog;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.StencilUtils;

/**
 * <p>
 * StudioGdo main servlet.
 * </p>
 * <p>
 * This servlet will check the session validity to accept entry. Else only
 * Connect command is accept.
 * </p>
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */
@SuppressWarnings("serial")
public class StudioGdoServlet extends HttpServlet {

    // command entries
    public static final String GDO_EXT = ".gdo"; // common extension
    public static final String LOCALE_ENTRY = "/locale" + GDO_EXT; // change
    // locale

    // rpc entries
    public static final String RPC_PATH = "/rpc/"; // RPC entries

    public static final String SERVLET_INFO = "StudioGdo servlet implementation";

    // files used to render message to end user
    private static final String ERROR_FILE = "com/gdo/jsf/webapp/error.html";

    /**
     * Write error file to browser and send alert to administrator.
     */
    protected static void alertResponse(HttpServletRequest request, HttpServletResponse response, String msg, Exception e) {
        try {
            StclContext stclContext = new StclContext(request, response);
            writeResponse(stclContext, ERROR_FILE);
        } catch (Exception ee) {
        }

        // construct message content
        String content = msg;
        if (StringUtils.isEmpty(content)) {
            content = "";
        }
        if (e != null) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            content += ':' + sw.getBuffer().toString();
        }
        logError(content);
    }

    protected static void writeResponse(StclContext stclContext, String fileName, Object... objects) throws IOException {

        // read content from file and format it with parameters
        InputStream file = ClassHelper.getResourceAsStream(fileName, stclContext.getResponse().getLocale());

        if (file == null) {
            for (Object o : objects) {
                writeHTMLResponse(stclContext.getResponse(), o.toString(), "");
            }
            return;
        }

        String content = StringHelper.read(new InputStreamReader(file));
        String out = String.format(content, objects);

        // write content in response and log
        writeHTMLResponse(stclContext.getResponse(), out, "");
        for (Object o : objects) {
            logError(o.toString());
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {

            // performs studiogdo entries
            StclContext stclContext = studiogdo(request, response);
            if (stclContext == null)
                return;

            // by default doesn't save project configuration
            RpcArgs args = stclContext.getRequestParameters();
            if (args.mustSaveProject() == null)
                args.setSaveProject(false);

            // saves project if needed
            if (args.mustSaveProject())
                saveProject(stclContext);
        } catch (Exception e) {
            alertResponse(request, response, "", e);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {

            // performs studiogdo entries
            StclContext stclContext = studiogdo(request, response);
            if (stclContext == null)
                return;

            // by default saves project configuration
            RpcArgs args = stclContext.getRequestParameters();
            if (args.mustSaveProject() == null)
                args.setSaveProject(true);

            // saves project if needed
            if (args.mustSaveProject())
                saveProject(stclContext);
        } catch (Exception e) {
            alertResponse(request, response, "", e);
        }
    }

    @Override
    public String getServletInfo() {
        return SERVLET_INFO;
    }

    /**
     * Returns the request parameters.
     * 
     * @param stclContext
     *            the stencil context.
     * @return the request parameters.
     */
    /*
     * protected Map<String, String[]> getRequestParams(StclContext stclContext)
     * throws Exception { HttpServletRequest request = stclContext.getRequest();
     * return request.getParameterMap(); }
     */

    /**
     * Main servlet entry.
     * 
     * @param request
     *            the HTTP request.
     * @param response
     *            the HTTP response.
     */
    protected StclContext studiogdo(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {

            // service response depends from path (path may not be null)
            String path = request.getPathInfo();
            logTrace("Servlet path: " + path);
            logTrace("Servlet query: " + request.getQueryString());

            // checks service entry defined
            if (StringUtils.isBlank(path)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Empty path");
                return null;
            }

            // locale service
            if (LOCALE_ENTRY.equals(path)) {
                setSessionLocale(request, response);
                return null;
            }

            // rpc interface
            StclContext stclContext = new StclContext(request, response);
            RpcArgs args = stclContext.getRpcArgs();

            if (path.startsWith(RPC_PATH)) {
                String service = StringHelper.substringEnd(path.substring(RPC_PATH.length()), GDO_EXT.length());

                // checks validity or closes connection
                if (!isCallValid(stclContext, service, args)) {
                    logWarn("Invalid connexion");
                    writeXMLResponse(stclContext.getResponse(), "<disconnected/>", StclContext.getCharacterEncoding());
                    stclContext.release();
                    return null;
                }

                // returns rpc result
                RpcWrapper wrapper = RpcWrapper.getInstance(stclContext);
                wrapper.service(stclContext, service, args);
                return null;
            }

            // multi part upload
            boolean isMultipart = ServletFileUpload.isMultipartContent(request);
            if (isMultipart) {
                doUpload(stclContext, args);
                return null;
            }

            response.sendError(HttpServletResponse.SC_BAD_REQUEST, path);

            return stclContext;
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.toString());
        }
        return null;
    }

    protected void doUpload(StclContext stclContext, RpcArgs args) throws Exception {
        Map<String, String[]> params = args.getParams(stclContext);

        // sets usefull parameters
        String ids[] = params.get("session");
        if (ids != null && ids.length > 0) {
            // upload can be done from another session (FF issue) so should
            // force
            // another session
            // TODO Badly resolved!!!!!
            String id = ids[0];
            HttpSession session = SessionStcl.HTTP_SESSIONS.get(id);
            if (session != null) {
                stclContext.setHttpSession(session);
            }
        }

        PStcl stcl = args.getStencilFromPath(stclContext);
        if (StencilUtils.isNull(stcl)) {
            logWarn("multi part form call be done on stencil only");
            return;
        }

        if (args.hasUploadedFile()) {
            String fileNames[] = params.get("Filename");
            if (fileNames != null && fileNames.length > 0) {
                stcl.multipart(stclContext, fileNames[0], args.fileUploadedContent());
            } else {
                FileItem item = args.fileUploadedContent();
                String fileName = item.getName();
                stcl.multipart(stclContext, fileName, item);
            }
        }
    }

    /**
     * Stores the forced locale in session parameters.
     */
    protected void setSessionLocale(HttpServletRequest request, HttpServletResponse response) {

        // creates locale
        String l = request.getParameter("l"); // language
        if (StringUtils.isBlank(l)) {
            l = "";
        }
        String c = request.getParameter("c"); // country
        if (StringUtils.isBlank(c)) {
            c = "";
        }
        Locale locale = new Locale(l, c);

        // saves it in session attribute
        HttpSession session = request.getSession();
        session.setAttribute(StudioGdoServlet.LOCALE_ENTRY, locale);
    }

    /**
     * Checks if the RPC call is valid.
     * 
     * @param stclContext
     *            the stencil context.
     * @param entry
     *            the RPC entry.
     * @return <tt>true</tt> if the call is valid, <tt>false</tt> if not.
     */
    protected boolean isCallValid(StclContext stclContext, String entry, RpcArgs args) {
        HttpServletRequest request = stclContext.getRequest();
        HttpSession session = request.getSession();

        // if the session is new accepts only connect command
        if (session.isNew()) {

            // connects command is a connection command
            return RpcWrapper.getInstance(stclContext).isConnectCommand(stclContext, entry, args);
        }

        // else checks the session is not dead internally (tomcat restarted
        // or..)
        PStcl servletStcl = stclContext.getServletStcl();
        PStcl active = servletStcl.getStencil(stclContext, ServletStcl.Slot.SESSION);
        return StencilUtils.isNotNull(active);
    }

    private void saveProject(StclContext stclContext) throws IOException {
        PStcl project = stclContext.getServletStcl();
        ((ServletStcl) project.getReleasedStencil(stclContext)).save(stclContext);
        logTrace("project saved");
    }

    //
    // CATALINA PART
    //

    /**
     * Writes the HTTP response from an input stream.
     * 
     * @param stclContext
     *            the stencil context.
     * @param type
     *            the mime content type.
     * @param in
     *            the input stream.
     * @param enc
     *            the encoding used.
     */
    public static void writeResponse(HttpServletResponse response, int status, String type, InputStream in, String charset) throws IOException {
        response.setStatus(status);
        if (StringUtils.isNotBlank(charset)) {
            response.setCharacterEncoding(charset);
        }
        response.setContentType(type);
        response.addHeader("Cache-Control", "no-cache");
        response.addHeader("Cache-Control", "no-store");
        response.addHeader("Access-Control-Allow-Methods", "POST, GET");
        response.setDateHeader("Expires", 0);
        if (in != null) {
            IOUtils.copy(in, response.getOutputStream());
        }
    }

    public static void writeHTMLResponse(HttpServletResponse response, String text, String enc) throws IOException {
        InputStream in = IOUtils.toInputStream(text);
        writeResponse(response, HttpServletResponse.SC_OK, "text/html", in, enc);
        in.close();
    }

    public static void writeXMLResponse(HttpServletResponse response, InputStream in, String enc) throws IOException {
        writeResponse(response, HttpServletResponse.SC_OK, "text/xml", in, enc);
    }

    public static void writeXMLResponse(HttpServletResponse response, String text, String enc) throws IOException {
        InputStream in = IOUtils.toInputStream(text);
        writeXMLResponse(response, in, enc);
        in.close();
    }

    //
    // LOG PART
    //

    public static StencilLog getLog() {
        return _Stencil._LOG;
    }

    public static <C extends _StencilContext> String logTrace(String format, Object... params) {
        return getLog().logTrace(null, format, params);
    }

    public static <C extends _StencilContext> String logWarn(String format, Object... params) {
        return getLog().logWarn(null, format, params);
    }

    public static <C extends _StencilContext> String logError(String format, Object... params) {
        return getLog().logError(null, format, params);
    }

    private static final Log SESSION_LOG = LogFactory.getLog(StudioGdoServlet.class);

    protected static void logUserConnected(HttpServletRequest request) {
        Principal userPrincipal = request.getUserPrincipal();
        GenericPrincipal genericPrincipal = (GenericPrincipal) userPrincipal;
        String roles = "";
        for (String role : genericPrincipal.getRoles()) {
            if (roles.length() > 0) {
                roles += ",";
            }
            roles += role;
        }
        String msg = String.format("%s [%s] connected", genericPrincipal.getName(), roles);
        SESSION_LOG.info(msg);
    }

    protected static void logUserDisconnected(HttpServletRequest request) {
        Principal userPrincipal = request.getUserPrincipal();
        GenericPrincipal genericPrincipal = (GenericPrincipal) userPrincipal;
        if (genericPrincipal != null) {
            String msg = String.format("%s disconnected", genericPrincipal.getName());
            SESSION_LOG.info(msg);
        }
    }
}