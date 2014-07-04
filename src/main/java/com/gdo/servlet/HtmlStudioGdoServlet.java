/**
 * Copyright GDO - 2005
 */

package com.gdo.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.Base64;

import com.gdo.helper.ConverterHelper;
import com.gdo.helper.StringHelper;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;

/**
 * StudioGdo html main servlet based on HTTP/HTML.
 * 
 * <p>
 * This servlet won't check session validity to accept any entry.and use
 * POST/GET interface
 * </p>
 */
@SuppressWarnings("serial")
public class HtmlStudioGdoServlet extends LocalStudioGdoServlet {

    private static final Pattern POST_PATTERN = Pattern.compile("(.*)_([^_]*)");
    private static final Pattern CURRENCY = Pattern.compile("(\\S*)\\s*€?");

    /**
     * Common servlet answer.
     * 
     * @param request
     *            the HTTP request.
     * @param response
     *            the HTTP response.
     * @param post
     *            <tt>true<//t> if the request was a POST, <tt>false</tt>
     *            otherwise.
     */
    @Override
    protected StclContext studiogdo(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {

            // traces
            String path = request.getPathInfo();
            logTrace("Servlet path: " + path);
            logTrace("Servlet query: " + request.getQueryString());

            // checks service entry defined and has gdo extension
            if (StringUtils.isBlank(path)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Empty path");
                return null;
            }
            String service = StringHelper.substringEnd(path.substring(1), GDO_EXT.length());
            if (StringUtils.isBlank(service)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Empty entry service");
                return null;
            }

            // org.apache.catalina.Session session1 =
            // (org.apache.catalina.Session)
            // request.getSession();
            // String username1 =
            // (String) session1.getNote(Constants.SESS_USERNAME_NOTE);
            // returns first role defined
            // if ("/j_security_check".equals(path) || "login".equals(service))
            // {
            if ("login".equals(service)) {

                // rechecks from login info
                Principal userPrincipal = request.getUserPrincipal();

                /*
                Realm realm = genericPrincipal.getRealm();
                String username = request.getParameter(Constants.FORM_USERNAME);
                String password = request.getParameter(Constants.FORM_PASSWORD);
                userPrincipal = realm.authenticate(username, password);
                /* */
                /*
                userPrincipal = genericPrincipal.getUserPrincipal();
                /* */
                if (userPrincipal == null) {
                    HttpSession session = request.getSession();
                    session.invalidate();
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    return null;
                }

                // returns associated role
                GenericPrincipal genericPrincipal = (GenericPrincipal) userPrincipal;
                logUserConnected(request);
                String[] roles = genericPrincipal.getRoles();
                InputStream in = new ByteArrayInputStream(roles[0].getBytes());
                StudioGdoServlet.writeResponse(response, HttpServletResponse.SC_OK, "text/html", in, null);
                return null;
            }

            // forces javascript redirection to login page
            if ("login-form".equals(service)) {

                // rechecks from login info
                String username = request.getParameter(Constants.FORM_USERNAME);
                // String password =
                // request.getParameter(Constants.FORM_PASSWORD);
                if (StringUtils.isBlank(username) || true) {
                    response.sendError(HttpServletResponse.SC_CREATED);
                    return null;
                }
            }

            // shows login fail message
            if ("login-failed".equals(service)) {
                response.sendError(418, "Connexion invalide");
                return null;
            }

            // creates stencil context
            StclContext stclContext = new StclContext(request, response);
            RpcArgs args = stclContext.getRpcArgs();

            // multi part upload and post values must be done before real
            // request
            if (args.hasUploadedFile()) {
                try {
                    doUpload(stclContext, args);
                } catch (Exception e) {
                    String msg = e.getMessage();
                    CommandStatus<StclContext, PStcl> result = new CommandStatus<StclContext, PStcl>("", CommandStatus.ERROR, 0, msg, null);
                    String text = result.jsonValue(result.getStatus());
                    StudioGdoServlet.writeHTMLResponse(stclContext.getResponse(), text, args.getCharacterEncoding(stclContext));
                    return null;
                }
            }

            // set post values
            if (!setPostValues(stclContext, args)) {
                return null;
            }

            // call service wrapper
            RpcWrapper wrapper = RpcWrapper.getInstance(stclContext);
            wrapper.service(stclContext, service, args);
            return stclContext;
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.toString());
        }
        return null;
    }

    @Override
    protected void doUpload(StclContext stclContext, RpcArgs args) throws Exception {

        // checks the stencil target
        PStcl stcl = args.getStencilFromPath(stclContext);
        if (StencilUtils.isNull(stcl)) {
            logWarn("multi part form call be done on stencil only");
            return;
        }

        // does the upload
        if (args.hasUploadedFile()) {
            Map<String, String[]> params = args.getParams(stclContext);
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

    private boolean setPostValues(StclContext stclContext, RpcArgs args) throws IOException {
        try {

            // does set service for each value
            Map<String, String[]> params = args.getParams(stclContext);
            Iterator<String> k = params.keySet().iterator();
            Base64 base = new Base64();
            PStcl stcl = null;
            Map<String, PStcl> modified = null;

            while (k.hasNext()) {
                String param = k.next();
                /*
                 * for (String p : params.get(param)) { if (param.matches(".*_.*")) {
                 * String type = param.substring(0, 1); String path_decode = new
                 * String(base.decode(param.substring(2).getBytes()));
                 * logWarn(" param -> %s_%s : %s", type, path_decode, p); } else if
                 * ("ap".equals(param)) { String p_decode = new
                 * String(base.decode(p.getBytes())); logWarn(" param -> %s : %s", param,
                 * p_decode); } else { logWarn(" param -> %s : %s", param, p); } }
                 */
                if ("$".equals(param)) {
                    for (String p : params.get(param)) {
                        logTrace("env : %s", p);
                    }
                }

                // all input values are defined as type_value
                if (StringUtils.isNotBlank(param)) {
                    Matcher m = POST_PATTERN.matcher(param);
                    if (m.matches()) {

                        // initialize stencil and map
                        if (stcl == null) {
                            stcl = args.getStencilFromPath(stclContext);
                            if (StencilUtils.isNull(stcl)) {
                                if (!args.acceptNoStencil()) {
                                    HttpServletResponse response = stclContext.getResponse();
                                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, StencilUtils.getNullReason(stcl));
                                    return false;
                                }
                            }
                            modified = new HashMap<String, PStcl>();
                        }

                        // gets format
                        String format = m.group(1);

                        // gets encoded slot path
                        String encoded = m.group(2);
                        if (StringUtils.isBlank(encoded)) {
                            continue;
                        }
                        String slot = new String(base.decode(encoded.getBytes()));
                        for (String p : params.get(param)) {
                            logWarn("param -> %s_%s : %s", format, slot, p);
                        }

                        // gets or stores modified stencil
                        PStcl modifiedStcl = stcl;
                        if (PathUtils.isComposed(slot)) {
                            String path = PathUtils.getPathName(slot);
                            slot = PathUtils.getLastName(slot);
                            if (modified.containsKey(path)) {
                                modifiedStcl = modified.get(path);
                            } else {
                                modifiedStcl = stcl.getStencil(stclContext, path);
                                if (!slot.startsWith("$"))
                                    modified.put(path, modifiedStcl);
                            }
                        } else {

                            // $slot are not concerned for modification (mainly
                            // $lock)
                            if (!slot.startsWith("$"))
                                modified.put(PathUtils.THIS, modifiedStcl);
                        }

                        // sets value
                        try {
                            String value = params.get(param)[0];

                            // empty value
                            if (format.endsWith("?")) {
                                if (StringUtils.isBlank(value)) {
                                    modifiedStcl.setString(stclContext, slot, "");
                                    continue;
                                }
                                format = format.substring(0, format.length() - 1);
                            }

                            if (format.startsWith("s")) {
                                postString(stclContext, modifiedStcl, slot, format, params.get(param)[0]);
                                continue;
                            }
                            if ("m".equals(format)) {
                                postMultipeString(stclContext, modifiedStcl, slot, param, params);
                                continue;
                            }
                            if (format.startsWith("i")) {
                                postInt(stclContext, modifiedStcl, slot, format, params.get(param)[0]);
                                continue;
                            }
                            if ("b".equals(format)) {
                                postBoolean(stclContext, modifiedStcl, slot, param, params);
                                continue;
                            }
                            if ("d".equals(format)) {
                                postDouble(stclContext, modifiedStcl, slot, param, params);
                                continue;
                            }
                            if ("p".equals(format)) {
                                postPlug(stclContext, modifiedStcl, slot, param, params);
                                continue;
                            }
                            if (format.startsWith("dt")) {
                                postDateTime(stclContext, modifiedStcl, slot, format, params.get(param)[0]);
                                continue;
                            }
                        } catch (NumberFormatException e) {
                            HttpServletResponse response = stclContext.getResponse();
                            String msg = e.toString() + " for parameter " + slot;
                            InputStream in = new ByteArrayInputStream(msg.toString().getBytes());
                            StudioGdoServlet.writeResponse(response, 418, "text/html", in, null);
                            return false;
                        } catch (Exception e) {
                            HttpServletResponse response = stclContext.getResponse();
                            String msg = e.toString() + " for parameter " + slot;
                            InputStream in = new ByteArrayInputStream(msg.toString().getBytes());
                            StudioGdoServlet.writeResponse(response, 418, "text/html", in, null);
                            return false;
                        }
                    }
                }
            }

            // verify/updates all modified stencils
            if (modified != null) {
                for (PStcl modifiedStcl : modified.values()) {
                    Result result = modifiedStcl.afterRPCSet(stclContext);
                    if (result.isNotSuccess()) {
                        HttpServletResponse response = stclContext.getResponse();
                        response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, result.getMessage());
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            HttpServletResponse response = stclContext.getResponse();
            InputStream in = new ByteArrayInputStream(e.toString().getBytes());
            StudioGdoServlet.writeResponse(response, 418, "text/html", in, null);
            return false;
        }
    }

    /**
     * Sets the property value as a string.
     * 
     * @param stclContext
     *            the stencil context.
     * @param stcl
     *            the stencil modified.
     * @param prop
     *            the property.
     * @param param
     *            the parameter name.
     * @param params
     *            the parameters list.
     */
    private void postString(StclContext stclContext, PStcl stcl, String prop, String format, String value) {
        if (StringUtils.isEmpty(value)) {
            stcl.setString(stclContext, prop, "");
        } else {
            stcl.setString(stclContext, prop, value);
        }
    }

    /**
     * Sets the property value as a composed string concatanated from multiple
     * entries..
     * 
     * 
     * @param stclContext
     *            the stencil context.
     * @param stcl
     *            the stencil modified.
     * @param prop
     *            the property.
     * @param param
     *            the parameter name.
     * @param params
     *            the parameters list.
     */
    private void postMultipeString(StclContext stclContext, PStcl stcl, String prop, String value, Map<String, String[]> params) {
        String str = "";

        // concatenates multiple entry values
        for (String param : params.get(value)) {
            if (StringUtils.isNotEmpty(str))
                str += stcl.getMultiPostSep(stclContext);
            str += param;
        }

        // sets string property
        if (StringUtils.isEmpty(str)) {
            stcl.setString(stclContext, prop, "");
        } else {
            stcl.setString(stclContext, prop, str);
        }
    }

    /**
     * Sets the property value as an integer.
     * 
     * @param stclContext
     *            the stencil context.
     * @param stcl
     *            the stencil modified.
     * @param prop
     *            the property.
     * @param param
     *            the parameter name.
     * @param params
     *            the parameters list.
     */
    private void postInt(StclContext stclContext, PStcl stcl, String prop, String format, String value) {
        if (StringUtils.isBlank(value)) {
            stcl.setInt(stclContext, prop, 0);
        } else {

            // currency
            if ("i€".equals(format)) {
                Matcher m = CURRENCY.matcher(value);
                if (m.matches()) {
                    int val = Integer.parseInt(m.group(1));
                    stcl.setInt(stclContext, prop, val);
                }
            } else if ("i/100,##€".equals(format) || "i/100,##%".equals(format)) {
                Matcher m = CURRENCY.matcher(value);
                if (m.matches()) {
                    String str = m.group(1);
                    if (StringUtils.endsWith(str, "€")) {
                        str = str.substring(0, str.length() - 1);
                    }
                    if (StringUtils.endsWith(str, "%")) {
                        str = str.substring(0, str.length() - 1);
                    }
                    int val = (int) (Float.parseFloat(str.trim().replace(',', '.')) * 100);
                    stcl.setInt(stclContext, prop, val);
                }
            } else if ("i/100,##".equals(format) || "i/100,##".equals(format)) {
                int val = (int) (Float.parseFloat(value.replace(',', '.')) * 100);
                stcl.setInt(stclContext, prop, val);
            } else if ("i".equals(format)) {
                int val = Integer.parseInt(value);
                stcl.setInt(stclContext, prop, val);
            } else {
                String msg = String.format("format %s inconnu", format);
                throw new NumberFormatException(msg);
            }
        }
    }

    /**
     * Sets the property value as a boolean.
     * 
     * @param stclContext
     *            the stencil context.
     * @param stcl
     *            the stencil modified.
     * @param prop
     *            the property.
     * @param param
     *            the parameter name.
     * @param params
     *            the parameters list.
     */
    private void postBoolean(StclContext stclContext, PStcl stcl, String prop, String param, Map<String, String[]> params) {
        String value = params.get(param)[0];
        if (StringUtils.isBlank(value)) {
            stcl.setBoolean(stclContext, prop, false);
        } else {
            stcl.setBoolean(stclContext, prop, ConverterHelper.parseBoolean(value));
        }
    }

    /**
     * Sets the property value as a double.
     * 
     * @param stclContext
     *            the stencil context.
     * @param stcl
     *            the stencil modified.
     * @param prop
     *            the property.
     * @param param
     *            the parameter name.
     * @param params
     *            the parameters list.
     */
    private void postDouble(StclContext stclContext, PStcl stcl, String prop, String param, Map<String, String[]> params) {
        String value = params.get(param)[0];
        if (StringUtils.isBlank(value)) {
            stcl.setDouble(stclContext, prop, 0);
        } else {
            stcl.setDouble(stclContext, prop, Double.parseDouble(value));
        }
    }

    private void postDateTime(StclContext stclContext, PStcl stcl, String prop, String format, String value) throws ParseException {
        if (StringUtils.isEmpty(value)) {
            stcl.setString(stclContext, prop, "");
        } else {

            // date
            if ("dt_dd/MM/yyyy".equals(format)) {
                DateFormat dateFormat = new SimpleDateFormat(format.substring(3));
                Date date = dateFormat.parse(value);
                DateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                stcl.setString(stclContext, prop, sqlDateFormat.format(date));
            }

            // date
            if ("dt_dd/MM/yyyy HH:mm".equals(format)) {
                DateFormat dateFormat = new SimpleDateFormat(format.substring(3));
                Date date = dateFormat.parse(value);
                DateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                stcl.setString(stclContext, prop, sqlDateFormat.format(date));
            }
        }
    }

    /**
     * Sets the property value as a plug action (value is path).
     * 
     * @param stclContext
     *            the stencil context.
     * @param stcl
     *            the stencil modified.
     * @param prop
     *            the property.
     * @param param
     *            the parameter name.
     * @param params
     *            the parameters list.
     */
    private void postPlug(StclContext stclContext, PStcl stcl, String slot, String value, Map<String, String[]> params) {
        String apath = params.get(value)[0];
        if (StringUtils.isNotBlank(apath)) {
            Base64 base = new Base64();
            PStcl toBePlugged = stcl.getStencil(stclContext, new String(base.decode(apath.getBytes())));
            stcl.plug(stclContext, toBePlugged, slot);
        } else {
            stcl.clearSlot(stclContext, slot);
        }
    }
}