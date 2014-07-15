/**
 * Copyright GDO - 2005
 */
package com.gdo.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.Base64;

import com.gdo.helper.ConverterHelper;
import com.gdo.helper.StringHelper;
import com.gdo.project.model.AtomicActionStcl;
import com.gdo.project.model.ComposedActionStcl;
import com.gdo.project.model.ComposedActionStcl.Status;
import com.gdo.project.model.ServletStcl;
import com.gdo.project.util.CatalinaUtils;
import com.gdo.servlet.xml.XmlBuilder;
import com.gdo.stencils.Keywords;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils._Stencil;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.atom.Atom;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.cmd.CommandStencil;
import com.gdo.stencils.faces.RenderContext;
import com.gdo.stencils.facet.FacetResult;
import com.gdo.stencils.facet.FacetType;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.log.StencilLog;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.prop.IPPropStencil;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;
import com.gdo.util.XmlStringWriter;

/**
 * <p>
 * StudioGdo RPC interface wrapper.
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
public class RpcWrapper {

    public static boolean FACETS_SAME_FACET = false;

    //
    // RPC services
    //

    // call command on a stencil
    public static final String APPLY_SERVICE = "apply";

    // call command on a stencil
    public static final String CALL_SERVICE = "call";

    // disconnect to the server
    public static final String DISCONNECT_SERVICE = "disconnect";

    // call command on a stencil
    public static final String EMPTY_SERVICE = "empty";

    // get a facet of a stencil
    public static final String FACET_SERVICE = "facet";

    // get a facet of a stencil
    public static final String FACETS_SERVICE = "facets";

    // get formatted string
    public static final String FORMAT_SERVICE = "format";

    // get property value
    public static final String GET_SERVICE = "get";

    // launch a composed command on a stencil
    public static final String LAUNCH_SERVICE = "launch";

    // multi set property value
    public static final String MULTI_SET_SERVICE = "mset";

    // get property value without status
    public static final String PROP_SERVICE = "prop";

    // classical post service
    public static final String POST_SERVICE = "post";

    // set property value
    public static final String SET_SERVICE = "set";

    // get stencils list in slot
    public static final String STENCILS_SERVICE = "stencils";

    // get attributes list in slot
    public static final String ATTRIBUTES_SERVICE = "attributes";

    //
    // RPC parameters
    //

    /**
     * Encoded path to the stencil on which entry will be applied.
     */
    public static final String ABSOLUTE_PATH_PARAM = "ap";

    /**
     * Encoded complement path to the stencil on which entry will be applied.
     */
    public static final String ABSOLUTE_COMPLEMENT_PATH_PARAM = "ap1";
    public static final String ABSOLUTE_COMPLEMENT_KEY_PARAM = "ak1";

    /**
     * Stencils attributes added to the STENCILS_SERVICE entry.
     */
    public static final String ATTRS_PARAM = "a";

    // accept entry even if no stencil
    public static final String ACCEPT_NO_STENCIL = "acceptNoStencil";

    // command name (if contains '.' then create instance from template name)
    public static final String CMD_PARAM = "c";

    // resulting data format (xml, json)
    public static final String DATA_FORMAT = "df";

    // encoding
    public static final String ENC_PARAM = "enc";

    // value expansion (none, 1=true, 0=false)
    public static final String EXP_PARAM = "exp";

    // facets
    public static final String FACETS_PARAM = "f";

    // expanded format
    public static final String FORMAT_PARAM = "format";

    // path where the command will be launched (defined for launch entry)
    public static final String LAUNCH_PATH_PARAM = "l";

    // locale
    public static final String LOCALE_PARAM = "locale";

    // modes
    public static final String MODES_PARAM = "m";

    // number (used for parameters, values, ..)
    public static final String NUMBER_PARAM = "n";
    public static final String PARAM_PREFIX = "param"; // parameters prefix

    // path to the stencil on which entry will be applied
    public static final String PATH_PARAM = "p";

    /*
     * complementary path and key
     * WARNING: p1 has predominence on k1
     * WARNING: ap1 has predominence on p1
     * WARNING: ak1 has predominence on ak1 
     */
    public static final String COMPLEMENT_PATH_PARAM = "p1";
    public static final String COMPLEMENT_KEY_PARAM = "k1";

    // release session before returning
    public static final String RELEASE_PARAM = "r";

    // save project before returning
    public static final String SAVE_PARAM = "s";

    // command target (same as stencil if not defined)
    public static final String TARGET_PARAM = "tg";
    public static final String TYPE_PARAM = "t"; // value type
    public static final String VALUE_PARAM = "v"; // value defined for set and
    public static final String TRANSACTION_ID_PARAM = "tid";
    // format entries

    // public static final String[] PARAMS_PARAM = new String[] { PARAM1_PARAM,
    // PARAM2_PARAM, PARAM3_PARAM, PARAM4_PARAM, PARAM5_PARAM };

    // property value type
    public static final String TYPE_TEXT = Keywords.TEXT; // return value as it
    public static final String TYPE_STRING = Keywords.STRING;
    public static final String TYPE_INT = Keywords.INT;
    public static final String TYPE_BOOLEAN = Keywords.BOOLEAN;
    public static final String TYPE_XINHA = Keywords.XINHA; // return value in

    // number of hits from service start
    public static int HITS = 0;

    private RpcWrapper(StclContext stclContext) {
        // singleton pattern
    }

    /**
     * @return <tt>true</tt> if the call is a connection call (used by servlet
     *         to accept entry).
     */
    public boolean isConnectCommand(StclContext stclContext, String entry, RpcArgs args) {
        if (CALL_SERVICE.equals(entry)) {
            logTrace(stclContext, "Checking connection command on new session");
            String cmd = args.getStringParameter(stclContext, CMD_PARAM);
            return ServletStcl.Command.CONNECT.equals(cmd);
        }
        return false;
    }

    /**
     * Main entry service.
     * 
     * @param stclContext
     *            the stencil context.
     * @param entry
     *            the RPC entry called.
     */
    public void service(StclContext stclContext, String entry, RpcArgs args) {
        try {
            boolean disconnect = false;

            // increments hits
            if (HITS != -1 && HITS < Integer.MAX_VALUE) {
                HITS++;
            } else {
                HITS = -1;
            }

            // gets arguments
            String trace = args.formatForTrace();
            if (CALL_SERVICE.equals(entry) || LAUNCH_SERVICE.equals(entry) || APPLY_SERVICE.equals(entry)) {
                String cmd = args.getStringParameter(stclContext, RpcWrapper.CMD_PARAM);
                logInfo(stclContext, "RPC %s:%s, cmd=%s", entry, trace, cmd);
            } else {
                logInfo(stclContext, "RPC %s:%s", entry, trace);
            }

            // checks entry not empty
            if (StringUtils.isBlank(entry)) {
                fault(stclContext, "", "Empty RPC entry", args);
            }

            // empty entry (used only to have post alone)
            if (EMPTY_SERVICE.equals(entry)) {
                String result = Result.success().jsonValue(Result.SUCCESS);
                StudioGdoServlet.writeHTMLResponse(stclContext.getResponse(), result, args.getCharacterEncoding(stclContext));
                return;
            }

            // executes service
            if (STENCILS_SERVICE.equals(entry)) {
                stencils(stclContext, args);
            } else if (ATTRIBUTES_SERVICE.equals(entry)) {
                attributes(stclContext, args);
            } else if (PROP_SERVICE.equals(entry)) {
                prop(stclContext, args);
            } else if (GET_SERVICE.equals(entry)) {
                get(stclContext, args);
            } else if (FACET_SERVICE.equals(entry)) {
                facet(stclContext, args);
            } else if (FACETS_SERVICE.equals(entry)) {
                if (FACETS_SAME_FACET)
                    facet(stclContext, args);
                else
                    facets(stclContext, args);
            } else if (SET_SERVICE.equals(entry)) {
                set(stclContext, args);
            } else if (MULTI_SET_SERVICE.equals(entry)) {
                mset(stclContext, args);
            } else if (APPLY_SERVICE.equals(entry)) {
                apply(stclContext, args);
            } else if (CALL_SERVICE.equals(entry)) {
                call(stclContext, args);
            } else if (LAUNCH_SERVICE.equals(entry)) {
                launch(stclContext, args);
            } else if (FORMAT_SERVICE.equals(entry)) {
                format(stclContext, args);
            } else if (DISCONNECT_SERVICE.equals(entry)) {
                disconnect = true;
                StudioGdoServlet.writeHTMLResponse(stclContext.getResponse(), "", args.getCharacterEncoding(stclContext));
            } else {
                // response.sendError(HttpServletResponse.SC_BAD_REQUEST, path);
                String msg = String.format("Unknown RPC entry %s", entry);
                fault(stclContext, entry, msg, args);
            }

            // releases session if required
            boolean release = args.getBooleanParameter(stclContext, RELEASE_PARAM, false);
            if (release || disconnect) {
                disconnect(stclContext);
            }

        } catch (Exception e) {
            fault(stclContext, entry, e, null);
            return;
        }
    }

    /**
     * Disconnect session.
     * 
     * @param stclContext
     *            the stencil context.
     * @param args
     *            the RPC arguments.
     */
    private void disconnect(StclContext stclContext) {
        StudioGdoServlet.logUserDisconnected(stclContext.getRequest());
        stclContext.getHttpSession().invalidate();
        stclContext.release();
    }

    /**
     * Stencils list entry.
     * 
     * @param stclContext
     *            the stencil context.
     * @param args
     *            the RPC arguments.
     * @throws IOException
     */
    private void stencils(StclContext stclContext, RpcArgs args) throws IOException {

        // gets stencils iterator
        StencilIterator<StclContext, PStcl> iter = getStencilsFromArgPath(stclContext, args);
        if (iter.isNotValid() && !args.acceptNoStencil()) {
            String reason = iter.getStatus().getMessage();
            logWarn(stclContext, reason);
            fault(stclContext, STENCILS_SERVICE, reason, args);
            return;
        }

        // returns stencils found
        XmlBuilder builder = new XmlBuilder();
        String xml = builder.stencils(stclContext, args, iter);
        StudioGdoServlet.writeXMLResponse(stclContext.getResponse(), xml, args.getCharacterEncoding(stclContext));
    }

    public void attributes(StclContext stclContext, RpcArgs args) {
        try {

            // gets stencils
            PStcl stcl = stclContext.getServletStcl();

            // returns no attribute if no path
            if (StringUtils.isBlank(args.getPath())) {
                fault(stclContext, STENCILS_SERVICE, "empty path", args);
                return;
            }

            // returns attributes found
            // !! TODO Path attribute seems wrong
            SortedMap<IKey, String[]> map = stcl.getAttributes(stclContext, args.getPath(), args.getAttributePathes());
            XmlStringWriter writer = new XmlStringWriter(args.getCharacterEncoding(stclContext));
            writer.startElement("result");
            addStatus(writer, Result.success());
            writer.startElement("stencils");
            writer.writeAttribute("size", map.size());
            for (Entry<IKey, String[]> e : map.entrySet()) {
                writer.startElement("stencil");
                writer.writeAttribute("key", e.getKey().toString());
                writer.writeAttribute("attributes", e.getValue().length);
                int index = 0;
                for (String att : e.getValue()) {
                    writer.writeAttribute("attr" + index, att);
                    index++;
                }
                writer.endElement("stencil");
            }
            writer.endElement("stencils");
            writer.endElement("result");

            // traces and responds
            String xml = writer.getString();
            logTrace(stclContext, xml);
            StudioGdoServlet.writeXMLResponse(stclContext.getResponse(), xml, args.getCharacterEncoding(stclContext));
        } catch (Exception e) {
            fault(stclContext, STENCILS_SERVICE, e, null);
            return;
        }
    }

    private void prop(StclContext stclContext, RpcArgs args) {
        try {

            // gets stencil
            /*
            String path = args.getPath();
            String parent = PathUtils.getPathName(path);
            String prop = PathUtils.getLastName(path);
            args.setPath(parent);
            */

            PStcl stcl = args.getStencilFromPath(stclContext);
            // stcl = stcl.getContainer(stclContext);

            // gets property type
            String type = args.getStringParameter(stclContext, TYPE_PARAM);
            if (StringUtils.isEmpty(type)) {
                type = TYPE_STRING;
            }

            // gets value found
            String value = "";
            if (StencilUtils.isNull(stcl)) {
                if (args.acceptNoStencil()) {
                    value = "";
                } else {
                    value = StencilUtils.getNullReason(stcl);
                }
            } else {
                if (TYPE_STRING.equals(type)) {
                    value = getStringWithOrWithoutExpansion(stclContext, stcl, args);
                } else if (TYPE_INT.equals(type)) {
                    value = stcl.getValue(stclContext);
                } else if (TYPE_BOOLEAN.equals(type)) {
                    value = stcl.getValue(stclContext);
                } else {
                    String msg = String.format("unknown type %s", type);
                    fault(stclContext, GET_SERVICE, msg, args);
                }
                if (value == null)
                    value = "";
            }

            // writes result
            logTrace(stclContext, "gets property value '%s' from stencil %s", value, stcl);
            StudioGdoServlet.writeHTMLResponse(stclContext.getResponse(), value, args.getCharacterEncoding(stclContext));
        } catch (Exception e) {
            fault(stclContext, PROP_SERVICE, e, null);
            return;
        }
    }

    private void get(StclContext stclContext, RpcArgs args) {
        try {

            // gets stencil
            PStcl stcl = args.getStencilFromPath(stclContext);

            // gets type
            String type = args.getStringParameter(stclContext, TYPE_PARAM);
            if (StringUtils.isBlank(type)) {
                type = TYPE_STRING;
            }

            // gets value found
            Result result = Result.success();
            String value = "";
            if (StencilUtils.isNull(stcl)) {
                if (args.acceptNoStencil()) {
                    value = "";
                } else {
                    result = stcl.getResult();
                    value = StencilUtils.getNullReason(stcl);
                }
            } else {
                if (TYPE_STRING.equals(type)) {
                    value = getStringWithOrWithoutExpansion(stclContext, stcl, args);
                } else if (TYPE_INT.equals(type)) {
                    value = stcl.getValue(stclContext);
                } else if (TYPE_BOOLEAN.equals(type)) {
                    value = stcl.getValue(stclContext);
                } else {
                    String msg = String.format("unknown type %s", type);
                    fault(stclContext, GET_SERVICE, msg, args);
                }
            }

            // returns value found
            XmlBuilder builder = new XmlBuilder();
            String xml = builder.get(stclContext, args, stcl, value, type, result);
            StudioGdoServlet.writeXMLResponse(stclContext.getResponse(), xml, args.getCharacterEncoding(stclContext));
        } catch (Exception e) {
            fault(stclContext, GET_SERVICE, e, null);
            return;
        }
    }

    private void set(StclContext stclContext, RpcArgs args) {
        try {

            // gets stencil
            PStcl stcl = args.getStencilFromPath(stclContext);
            if (StencilUtils.isNull(stcl)) {
                if (!args.acceptNoStencil()) {
                    fault(stclContext, SET_SERVICE, StencilUtils.getNullReason(stcl), args);
                }
                return;
            }
            stcl = stcl.getContainer(stclContext);
            String p = PathUtils.getLastName(args.getPath());

            // gets value (if comming from www-form-encoded should decode
            // content)
            String value = args.getStringParameter(stclContext, VALUE_PARAM);
            value = getValueWithOrWithoutExpansion(stclContext, stcl, value, args);

            // gets type
            String type = args.getStringParameter(stclContext, TYPE_PARAM);
            if (StringUtils.isEmpty(type)) {
                type = TYPE_STRING;
            }

            // sets value
            if (TYPE_STRING.equals(type)) {
                stcl.setString(stclContext, p, value);
            } else if (TYPE_INT.equals(type)) {
                stcl.setInt(stclContext, p, Integer.parseInt(value));
            } else if (TYPE_BOOLEAN.equals(type)) {
                Boolean bool = ConverterHelper.parseBoolean(value);
                stcl.setBoolean(stclContext, p, bool);
            } else {
                String msg = String.format("unknown type %s", type);
                fault(stclContext, SET_SERVICE, msg, args);
                return;
            }

            // performs command after set/mset RPC calls
            stcl.afterRPCSet(stclContext);

            // writes result
            XmlStringWriter writer = new XmlStringWriter(args.getCharacterEncoding(stclContext));
            writer.startElement("result");
            args.writeAttributes(stclContext, stcl, true, writer);
            addStatus(writer, Result.success());
            writer.endElement("result");

            // traces and responds
            String xml = writer.getString();
            logTrace(stclContext, xml);
            StudioGdoServlet.writeXMLResponse(stclContext.getResponse(), xml, args.getCharacterEncoding(stclContext));
        } catch (Exception e) {
            fault(stclContext, SET_SERVICE, e, null);
            return;
        }
    }

    private void mset(StclContext stclContext, RpcArgs args) {
        try {
            Result result = Result.success();

            // gets stencil
            PStcl stcl = args.getStencilFromPath(stclContext);
            if (StencilUtils.isNull(stcl)) {
                if (!args.acceptNoStencil()) {
                    fault(stclContext, SET_SERVICE, StencilUtils.getNullReason(stcl), args);
                }
                return;
            }

            // do set service for each value
            Enumeration<String> e = stclContext.getRequest().getParameterNames();
            while (e.hasMoreElements()) {
                String param = e.nextElement();
                if (param.startsWith("param_")) {
                    int l = "param_".length();
                    String type = param.substring(l, l + 1);
                    String slot = param.substring(l + 2);

                    // string
                    if ("s".equals(type)) {

                        // gets encoded value
                        String value = args.getStringParameter(stclContext, param);
                        /*
                         * if
                         * (StringUtils.isNotEmpty(args.getCharacterEncoding(stclContext)))
                         * value = new
                         * String(value.getBytes(),args.getCharacterEncoding(stclContext));
                         * }
                         */
                        if (value == null) {
                            value = "";
                        }

                        // sets value
                        stcl.setString(stclContext, slot, value);
                    } else

                    // integer
                    if ("i".equals(type)) {
                        int value;
                        String str = stclContext.getRequest().getParameter(param);
                        if (StringUtils.isEmpty(str)) {
                            value = 0;
                        } else {
                            value = Integer.parseInt(str);
                        }
                        stcl.setInt(stclContext, slot, value);
                    } else

                    // boolean
                    if ("b".equals(type)) {
                        boolean value;
                        String str = stclContext.getRequest().getParameter(param);
                        if (StringUtils.isEmpty(str)) {
                            value = false;
                        } else {
                            value = ConverterHelper.parseBoolean(str);
                        }
                        stcl.setBoolean(stclContext, slot, value);
                    } else

                    // plug
                    if ("p".equals(type)) {
                        String path = stclContext.getRequest().getParameter(param);
                        if (StringUtils.isNotBlank(path)) {
                            PStcl toBePlugged = stcl.getStencil(stclContext, path);
                            stcl.plug(stclContext, toBePlugged, slot);
                        } else {
                            stcl.clearSlot(stclContext, slot);
                        }
                    }
                }
            }

            // performs command after set/mset RPC calls
            stcl.afterRPCSet(stclContext);

            // writes result
            XmlStringWriter writer = new XmlStringWriter(args.getCharacterEncoding(stclContext));
            writer.startElement("result");
            args.writeAttributes(stclContext, stcl, true, writer);
            addStatus(writer, result);
            writer.endElement("result");

            // traces and responds
            String xml = writer.getString();
            logTrace(stclContext, xml);
            StudioGdoServlet.writeXMLResponse(stclContext.getResponse(), xml, args.getCharacterEncoding(stclContext));
        } catch (Exception e) {
            fault(stclContext, SET_SERVICE, e, null);
            return;
        }
    }

    /**
     * Executes a call entry.
     * 
     * @param stclContext
     *            the stencil context.
     * @param args
     *            the RPC arguments.
     */
    private void call(StclContext stclContext, RpcArgs args) {
        try {

            // gets stencils
            StencilIterator<StclContext, PStcl> iter = getStencilsFromArgPath(stclContext, args);
            if (iter.isNotValid()) {
                String reason = iter.getStatus().getMessage();
                String msg = logWarn(stclContext, "call service : cannot found stcl at path %s : %s", args.getPath(), reason);
                fault(stclContext, CALL_SERVICE, msg, args);
                return;
            }

            // gets command parameter
            String cmd = args.getStringParameter(stclContext, CMD_PARAM);
            if (StringUtils.isBlank(cmd)) {
                String msg = logWarn(stclContext, "no command name defined (param %s)", CMD_PARAM);
                fault(stclContext, CALL_SERVICE, msg, args);
                return;
            }

            // executes command on every stencil
            CommandStatus<StclContext, PStcl> status = null;
            for (PStcl stcl : iter) {

                // gets command
                PStcl cmdStcl = stcl.getCommand(stclContext, cmd);
                if (StencilUtils.isNull(cmdStcl)) {
                    String msg = logWarn(stclContext, "cannot get command %s for stencil %s", cmd, stcl);
                    fault(stclContext, CALL_SERVICE, msg, args);
                    continue;
                }

                // executes command
                CommandContext<StclContext, PStcl> cmdContext = createCommandContext(stclContext, args, stcl);
                CommandStencil<StclContext, PStcl> actionStcl = (CommandStencil<StclContext, PStcl>) cmdStcl.getReleasedStencil(stclContext);
                CommandStatus<StclContext, PStcl> res = actionStcl.execute(cmdContext, cmdStcl);
                if (status == null) {
                    status = res;
                } else {
                    status.addOther(res);
                }
            }

            // if the command forces redirection
            if (status != null && StringUtils.isNotEmpty(status.redirection)) {
                stclContext.getResponse().sendRedirect(status.redirection);
                return;
            }

            // writes result
            XmlStringWriter writer = new XmlStringWriter(args.getCharacterEncoding(stclContext));
            writer.startElement("result");
            // args.writeAttributes(stclContext, cmdStcl, true, writer);
            addStatus(writer, status);
            writer.endElement("result");

            // traces and response
            String xml = writer.getString();
            logTrace(stclContext, xml);
            StudioGdoServlet.writeXMLResponse(stclContext.getResponse(), xml, args.getCharacterEncoding(stclContext));
        } catch (Exception e) {
            fault(stclContext, CALL_SERVICE, e, null);
        }
    }

    private void launch(StclContext stclContext, RpcArgs args) {
        try {

            // gets stencil
            PStcl stcl = args.getStencilFromPath(stclContext);
            if (StencilUtils.isNull(stcl)) {
                String reason = StencilUtils.getNullReason(stcl);
                String msg = logWarn(stclContext, "launch service : cannot found stcl at path %s : %s", args.getPath(), reason);
                fault(stclContext, LAUNCH_SERVICE, msg, args);
                return;
            }

            // gets launch path and key
            String launchPath = args.getStringParameter(stclContext, LAUNCH_PATH_PARAM);
            if (StringUtils.isBlank(launchPath)) {
                launchPath = "/Session/Launched";
            }

            // gets command parameter
            String cmd = args.getStringParameter(stclContext, CMD_PARAM);
            if (StringUtils.isEmpty(cmd)) {
                String msg = logWarn(stclContext, "no launch name defined (param %s)", CMD_PARAM);
                fault(stclContext, LAUNCH_SERVICE, msg, args);
                return;
            }

            // gets command and verifies it is composed
            PStcl cmdStcl = stcl.getCommand(stclContext, cmd);
            if (StencilUtils.isNull(cmdStcl)) {
                String msg = logWarn(stclContext, "cannot get command %s for stencil %s", cmd, stcl);
                fault(stclContext, LAUNCH_SERVICE, msg, args);
                return;
            }
            if (!(cmdStcl.getReleasedStencil(stclContext) instanceof ComposedActionStcl)) {
                String msg = logWarn(stclContext, "command %s in stencil %s is not a composed action", cmd, stcl);
                fault(stclContext, LAUNCH_SERVICE, msg, args);
                return;
            }

            // executes command and adds current launch path
            CommandContext<StclContext, PStcl> cmdContext = createCommandContext(stclContext, args, stcl);
            ComposedActionStcl actionStcl = (ComposedActionStcl) cmdStcl.getReleasedStencil(stclContext);
            CommandStatus<StclContext, PStcl> status = actionStcl.launch(cmdContext, launchPath, cmdStcl);

            // writes result
            XmlStringWriter writer = new XmlStringWriter(args.getCharacterEncoding(stclContext));
            writer.startElement("result");
            // args.writeAttributes(stclContext, stcl, true, writer);
            addStatus(writer, status);
            writer.endElement("result");

            // traces and response
            String xml = writer.getString();
            logTrace(stclContext, xml);
            StudioGdoServlet.writeXMLResponse(stclContext.getResponse(), xml, args.getCharacterEncoding(stclContext));
        } catch (Exception e) {
            fault(stclContext, LAUNCH_SERVICE, e, null);
        }
    }

    private void format(StclContext stclContext, RpcArgs args) {
        try {

            // get stencil
            PStcl stcl = args.getStencilFromPath(stclContext);
            if (StencilUtils.isNull(stcl)) {
                String reason = StencilUtils.getNullReason(stcl);
                String msg = logWarn(stclContext, "format service : cannot found stcl at path %s : %s", args.getPath(), reason);
                fault(stclContext, FORMAT_SERVICE, msg, args);
                return;
            }

            // get format value
            String format = args.getStringParameter(stclContext, VALUE_PARAM);
            if (StringUtils.isEmpty(format)) {
                String msg = logWarn(stclContext, "no format value (param %s)", VALUE_PARAM);
                fault(stclContext, FORMAT_SERVICE, msg, args);
                return;
            }

            // write result
            XmlStringWriter writer = new XmlStringWriter(args.getCharacterEncoding(stclContext));
            writer.startElement("result");
            args.writeAttributes(stclContext, stcl, true, writer);
            addStatus(writer, Result.success());
            writer.startElement("value");
            writer.writeCDATAElement("data", stcl.format(stclContext, format));
            writer.endElement("value");
            writer.endElement("result");

            // trace and response
            String xml = writer.getString();
            logTrace(stclContext, xml);
            StudioGdoServlet.writeXMLResponse(stclContext.getResponse(), xml, args.getCharacterEncoding(stclContext));
        } catch (Exception e) {
            fault(stclContext, FORMAT_SERVICE, e, null);
            return;
        }
    }

    private void facet(StclContext stclContext, RpcArgs args) throws IOException {
        HttpServletResponse response = stclContext.getResponse();

        try {

            // gets facet type and mode
            String type = args.getStringParameter(stclContext, FACETS_PARAM);
            String mode = args.getStringParameter(stclContext, MODES_PARAM);
            if (StringUtils.isBlank(type)) {

                // the type is undefined
                String msg = String.format("no facet defined (param %s)", FACETS_PARAM);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
                return;
            }

            // gets stencil
            PStcl stcl = args.getStencilFromPath(stclContext);
            if (StencilUtils.isNull(stcl)) {

                // null stencil may be accepted
                if (args.acceptNoStencil()) {
                    StudioGdoServlet.writeHTMLResponse(stclContext.getResponse(), "", args.getCharacterEncoding(stclContext));
                    return;
                }

                // stencil may not be null
                String reason = StencilUtils.getNullReason(stcl);
                String msg = String.format("facet service : cannot found stencil at path %s : %s", args.getPath(), reason);
                response.sendError(HttpServletResponse.SC_NO_CONTENT, msg);
                return;
            }

            // searches facet from stencil
            RenderContext<StclContext, PStcl> renderCtxt = new RenderContext<StclContext, PStcl>(stclContext, stcl, type, mode);
            FacetResult facetResult = stcl.getFacet(renderCtxt);
            if (facetResult.isNotSuccess()) {

                // error in facet
                response.sendError(HttpServletResponse.SC_NOT_FOUND, facetResult.getMessage());
                return;
            }

            // HTML facet
            if (FacetType.HTML.equals(type)) {
                StringWriter writer = new StringWriter();
                writer.write("<html>\n");
                writer.write(" <META HTTP-EQUIV=\"Pragma\" CONTENT=\"no-cache\">\n");
                writer.write(" <META HTTP-EQUIV=\"Expires\" CONTENT=\"-1\">\n");
                writer.write(" <META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=utf-8\">\n");
                writer.write("<body>\n");
                IOUtils.copy(facetResult.getInputStream(), writer);
                facetResult.closeInputStream();
                writer.write("</body>\n</html>\n");
                String content = stcl.format(stclContext, writer.getBuffer().toString());
                StudioGdoServlet.writeHTMLResponse(response, content, args.getCharacterEncoding(stclContext));
                return;
            }

            // HTML 5 facet or JSON facet
            if (FacetType.HTML5.equals(type) || FacetType.DOM5.equals(type) || FacetType.JSON.equals(type) || FacetType.JSKEL.equals(type) || FacetType.PYTHON.equals(type)) {
                String mime = facetResult.getMimeType();
                InputStream in = facetResult.getInputStream();
                StudioGdoServlet.writeResponse(stclContext.getResponse(), HttpServletResponse.SC_OK, mime, in, StclContext.getCharacterEncoding());
                facetResult.closeInputStream();
                return;
            }

            // file facet
            if (FacetType.FILE.equals(type)) {
                if (FacetType.E4X.equals(mode)) {
                    InputStream in = facetResult.getInputStream();
                    String enc = StclContext.getCharacterEncoding();
                    StudioGdoServlet.writeXMLResponse(stclContext.getResponse(), in, enc);
                    facetResult.closeInputStream();
                    return;
                }
                CatalinaUtils.writeFileResponse(stclContext, facetResult);
                return;
            }

            // write result
            Reader reader = new InputStreamReader(facetResult.getInputStream());
            XmlStringWriter writer = new XmlStringWriter(args.getCharacterEncoding(stclContext));
            writer.startElement("result");
            args.writeAttributes(stclContext, stcl, false, writer);
            addStatus(writer, Result.success());

            // not escaped as XML may be used in data
            writer.writeCDATAElement("data", StringHelper.read(reader));
            writer.endElement("result");

            // trace and response
            String xml = writer.getString();
            logTrace(stclContext, xml);
            StudioGdoServlet.writeXMLResponse(stclContext.getResponse(), xml, args.getCharacterEncoding(stclContext));
            facetResult.closeInputStream();
        } catch (Exception e) {
            String msg = logError(stclContext, e.toString());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
            return;
        }
    }

    private void facets(StclContext stclContext, RpcArgs args) throws IOException {
        HttpServletResponse response = stclContext.getResponse();
        try {

            // gets facet type and mode
            String type = args.getStringParameter(stclContext, FACETS_PARAM);
            String mode = args.getStringParameter(stclContext, MODES_PARAM);
            if (StringUtils.isBlank(type)) {

                // the type is undefined
                String msg = String.format("no facet defined (param %s)", FACETS_PARAM);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
                return;
            }

            // HTML 5 facet or JSON facet
            if (FacetType.HTML5.equals(type) || FacetType.DOM5.equals(type) || FacetType.JSON.equals(type) || FacetType.JSKEL.equals(type) || FacetType.PYTHON.equals(type)) {
                String mime = null;
                StringWriter str = new StringWriter();

                if (FacetType.JSON.equals(type) || FacetType.JSKEL.equals(type))
                    str.write("[");

                for (PStcl stcl : args.getStencilsFromPath(stclContext)) {

                    // on first stencil, mime variable is nor defined
                    if (mime != null && (FacetType.JSON.equals(type) || FacetType.JSKEL.equals(type)))
                        str.write(",");

                    // searches facet from stencil
                    RenderContext<StclContext, PStcl> renderCtxt = new RenderContext<StclContext, PStcl>(stclContext, stcl, type, mode);
                    FacetResult facetResult = stcl.getFacet(renderCtxt);
                    if (facetResult.isNotSuccess()) {

                        // error in facet
                        response.sendError(HttpServletResponse.SC_NOT_FOUND, facetResult.getMessage());
                        return;
                    }

                    InputStream in = facetResult.getInputStream();
                    IOUtils.copy(in, str);
                    facetResult.closeInputStream();

                    if (mime == null)
                        mime = facetResult.getMimeType();
                }

                if (FacetType.JSON.equals(type) || FacetType.JSKEL.equals(type))
                    str.write("]");

                InputStream in = new ByteArrayInputStream(str.toString().getBytes(StclContext.getCharacterEncoding()));
                StudioGdoServlet.writeResponse(stclContext.getResponse(), HttpServletResponse.SC_OK, mime, in, StclContext.getCharacterEncoding());
                return;
            }

            // the mode is undefined
            String msg = String.format("cannot call facets on mode %s", mode);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
            return;
        } catch (Exception e) {
            String msg = logError(stclContext, e.toString());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
            return;
        }
    }

    // --------------------------------------------------------------------------
    //
    // HTML5 entries.
    //
    // --------------------------------------------------------------------------

    private void apply(StclContext stclContext, RpcArgs args) {
        try {

            String enc = args.getCharacterEncoding(stclContext);

            // gets stencil
            PStcl stcl = args.getStencilFromPath(stclContext);
            if (StencilUtils.isNull(stcl)) {

                // null stencil may be accepted
                if (args.acceptNoStencil()) {
                    StudioGdoServlet.writeHTMLResponse(stclContext.getResponse(), "", enc);
                    return;
                }

                // return error
                HttpServletResponse response = stclContext.getResponse();
                String msg = logInfo(stclContext, "apply : cannot found stencil at path %s : %s", args.getPath(), StencilUtils.getNullReason(stcl));
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
                return;
            }

            // gets command parameter
            String cmd = args.getStringParameter(stclContext, CMD_PARAM);
            if (StringUtils.isEmpty(cmd)) {
                HttpServletResponse response = stclContext.getResponse();
                String msg = logWarn(stclContext, "apply : no command name defined (param %s)", CMD_PARAM);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
                return;
            }

            // gets command and verifies it exist
            PStcl cmdStcl = stcl.getCommand(stclContext, cmd);
            if (StencilUtils.isNull(cmdStcl)) {
                HttpServletResponse response = stclContext.getResponse();
                String msg = logWarn(stclContext, "apply : cannot get command %s for stencil %s", cmd, stcl);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
                return;
            }

            // executes atomic action
            if (cmdStcl.getReleasedStencil(stclContext) instanceof AtomicActionStcl) {

                // calls execution
                CommandContext<StclContext, PStcl> cmdContext = createCommandContext(stclContext, args, stcl);
                CommandStencil<StclContext, PStcl> actionStcl = (CommandStencil<StclContext, PStcl>) cmdStcl.getReleasedStencil(stclContext);
                CommandStatus<StclContext, PStcl> result = actionStcl.execute(cmdContext, cmdStcl);

                // returns JSON result
                StudioGdoServlet.writeHTMLResponse(stclContext.getResponse(), result.jsonValue(result.getStatus()), enc);
            } else

            // executes composed action
            if (cmdStcl.getReleasedStencil(stclContext) instanceof ComposedActionStcl) {

                // calls execution
                String launchPath = PathUtils.createPath("/Session/Launch", Atom.uniqueInt());
                CommandContext<StclContext, PStcl> cmdContext = createCommandContext(stclContext, args, stcl);
                ComposedActionStcl actionStcl = (ComposedActionStcl) cmdStcl.getReleasedStencil(stclContext);
                CommandStatus<StclContext, PStcl> result = actionStcl.launch(cmdContext, launchPath, cmdStcl);

                // encodes launched path
                Base64 base = new Base64();
                String launched = result.getInfo(CommandStatus.SUCCESS, ComposedActionStcl.class.getName(), Status.LAUNCH_PATH);
                String encoded = new String(base.encode(launched.getBytes()));
                result.setInfo(CommandStatus.SUCCESS, ComposedActionStcl.class.getName(), Status.LAUNCH_PATH, encoded);

                // returns JSON result
                StudioGdoServlet.writeHTMLResponse(stclContext.getResponse(), result.jsonValue(result.getStatus()), enc);
            }

            // there is no other case
            else {
                HttpServletResponse response = stclContext.getResponse();
                String msg = logWarn(stclContext, "command %s in stencil %s is not an action", cmd, stcl);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
                return;
            }
        } catch (Exception e) {
            fault(stclContext, LAUNCH_SERVICE, e, null);
        }
    }

    public void doPost(StclContext stclContext, RpcArgs args) {
        try {

            // gets stencil
            PStcl stcl = args.getStencilFromPath(stclContext);
            if (StencilUtils.isNull(stcl)) {
                if (!args.acceptNoStencil()) {
                    fault(stclContext, SET_SERVICE, StencilUtils.getNullReason(stcl), args);
                }
                return;
            }

            // do set service for each value
            Enumeration<String> e = stclContext.getRequest().getParameterNames();
            while (e.hasMoreElements()) {
                String param = e.nextElement();
                if (StringUtils.isNotBlank(param) && param.length() > 2) {
                    String type = param.substring(0, 1);
                    String slot = param.substring(2);

                    // sets string value
                    if ("s".equals(type)) {
                        String value = args.getStringParameter(stclContext, param);
                        if (StringUtils.isEmpty(value)) {
                            stcl.setString(stclContext, slot, "");
                        } else {
                            stcl.setString(stclContext, slot, value);
                        }
                        continue;
                    }

                    // sets integer value
                    if ("i".equals(type)) {
                        String str = stclContext.getRequest().getParameter(param);
                        if (StringUtils.isBlank(str)) {
                            stcl.setInt(stclContext, slot, 0);
                        } else {
                            stcl.setInt(stclContext, slot, Integer.parseInt(str));
                        }
                        continue;
                    }

                    // sets boolean value
                    if ("b".equals(type)) {
                        String str = stclContext.getRequest().getParameter(param);
                        if (StringUtils.isBlank(str)) {
                            stcl.setBoolean(stclContext, slot, false);
                        } else {
                            stcl.setBoolean(stclContext, slot, ConverterHelper.parseBoolean(str));
                        }
                        continue;
                    }

                    // plugs value (value is path)
                    if ("p".equals(type)) {
                        String path = stclContext.getRequest().getParameter(param);
                        if (StringUtils.isNotBlank(path)) {
                            PStcl toBePlugged = stcl.getStencil(stclContext, path);
                            stcl.plug(stclContext, toBePlugged, slot);
                        } else {
                            stcl.clearSlot(stclContext, slot);
                        }
                    }
                }
            }

            // performs command after set/mset RPC calls
            stcl.afterRPCSet(stclContext);

        } catch (Exception e) {
            logError(stclContext, e.toString());
            fault(stclContext, FACET_SERVICE, e, null);
            return;
        }
    }

    /**
     * Returns a fault status as return.
     * 
     * @param stclContext
     *            the stencil context.
     * @param entry
     *            the servet entry called.
     * @param msg
     *            the displayed message.
     * @param args
     *            the RPC call arguments.
     */
    private void fault(StclContext stclContext, String entry, String msg, RpcArgs args) {
        try {
            String enc = (args != null) ? args.getCharacterEncoding(stclContext) : StclContext.getCharacterEncoding();
            XmlStringWriter writer = new XmlStringWriter(enc);
            writer.startElement("fault");
            writer.writeAttribute("entry", entry);
            writer.writeAttribute("path", stclContext.getRequest().getParameter(PATH_PARAM));
            writer.writeCDATAElement("msg", msg);
            writer.endElement("fault");
            String content = writer.getString();
            StudioGdoServlet.writeXMLResponse(stclContext.getResponse(), content, enc);
            writer.close();
            logWarn(stclContext, content);
        } catch (IOException e) {
            logError(stclContext, "Cannot write %s message fault", msg);
        }
    }

    /**
     * Returns the stack trace of exception in fault.
     * 
     * @param stclContext
     *            the stencil context.
     * @param entry
     *            the RPC entry.
     * @param e
     *            the exception throwed.
     */
    private void fault(StclContext stclContext, String entry, Exception e, RpcArgs args) {
        String content = "";
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        content += sw.getBuffer().toString();
        fault(stclContext, entry, content, args);
    }

    private String getStringWithOrWithoutExpansion(StclContext stclContext, IPPropStencil<StclContext, PStcl> prop, RpcArgs args) {
        String exp = args.getStringParameter(stclContext, EXP_PARAM);
        if ("1".equals(exp) || "true".equals(exp)) {
            return prop.getExpandedValue(stclContext);
        }
        if ("0".equals(exp) || "false".equals(exp)) {
            return prop.getNotExpandedValue(stclContext);
        }
        return prop.getValue(stclContext);
    }

    private String getValueWithOrWithoutExpansion(StclContext stclContext, PStcl stcl, String value, RpcArgs args) {
        String exp = args.getStringParameter(stclContext, EXP_PARAM);
        if ("1".equals(exp) || "true".equals(exp)) {
            return stcl.format(stclContext, value);
        }
        return value;
    }

    /**
     * Creates the command context with associated parameters.
     * 
     * @param stclContext
     *            the stencil context.
     * @param args
     *            RPC arguments
     * @param stcl
     *            the stencil on which the command is called.
     * @return the command context.
     */
    private CommandContext<StclContext, PStcl> createCommandContext(StclContext stclContext, RpcArgs args, PStcl stcl) {

        // creates context
        PStcl targetStcl = stcl;
        String target = args.getStringParameter(stclContext, TARGET_PARAM);
        if (!StringUtils.isEmpty(target)) {
            targetStcl = stcl.getStencil(stclContext, target);
        }
        CommandContext<StclContext, PStcl> context = new CommandContext<StclContext, PStcl>(stclContext, targetStcl);

        // sets parameters
        Map<String, String[]> params = args.getParams(stclContext);
        Iterator<String> iter = params.keySet().iterator();
        while (iter.hasNext()) {
            String name = iter.next();
            if (name.startsWith(PARAM_PREFIX)) {
                String[] param = params.get(name);
                if (param != null) {
                    context.setRedefinedParameter(name, param[0]);
                }
            }
        }

        return context;
    }

    /*
     * private String getXinhaFromMode(StclContext stclContext, PStcl stcl, String
     * path, String mode) throws UnsupportedEncodingException { String file =
     * mode; if (StringUtils.isEmpty(file)) file = "xinha/simple.html";
     * InputStream in = ClassHelper.getResourceAsStream(file,
     * stclContext.getLocale()); if (in == null) return ""; String out =
     * StringHelper.read(new InputStreamReader(in)); String content =
     * getStringFromMode(stclContext, stcl, path, "false", null);
     * HttpServletRequest request = stclContext.getRequest(); String baseHref =
     * request.getRequestURL().substring(0, request.getRequestURL().length() -
     * request.getRequestURI().length()); out = String.format(out, content,
     * stclContext.getLocale().getLanguage(), baseHref, stcl.getId(), path);
     * return stcl.format(stclContext, out); }
     */

    /**
     * Gets the unique servlet wrapper.
     * 
     * @param stclContext
     *            the stencil context
     * @return the unique servlet wrapper.
     */
    public static RpcWrapper getInstance(StclContext stclContext) {
        // ServletContext servContext =
        // stclContext.getHttpSession().getServletContext();
        // RpcWrapper wrapper = (RpcWrapper)
        // servContext.getAttribute(RpcWrapper.class.getName());
        // if (wrapper == null) {
        // wrapper = new RpcWrapper(stclContext);
        // servContext.setAttribute(RpcWrapper.class.getName(), wrapper);
        // }
        // return wrapper;
        return new RpcWrapper(stclContext);
    }

    /**
     * Returns a stencil iterator from the request argument path.
     * 
     * @param stclContext
     *            the stencil context.
     * @param args
     *            the request arguments.
     * @return a stencil iterator.
     */
    private StencilIterator<StclContext, PStcl> getStencilsFromArgPath(StclContext stclContext, RpcArgs args) {
        PStcl stcl = stclContext.getServletStcl();
        if (StringUtils.isBlank(args.getPath())) {
            return StencilUtils.< StclContext, PStcl> iterator(stclContext, stcl, stcl.getContainingSlot());
        }
        return stcl.getStencils(stclContext, args.getPath());
    }

    /**
     * Adds status info to XML answer.
     * 
     * @param writer
     *            the XML answer writer.
     * @param status
     *            the status to add.
     */
    private void addStatus(XmlStringWriter writer, Result status) throws IOException {

        // if status null (on iterator or stencil)
        if (status == null) {
            writer.startElement("status");
            writer.writeAttribute("level", Byte.toString(CommandStatus.SUCCESS));
            writer.endElement("status");
            return;
        }

        // writes status
        writer.startElement("status");
        writer.writeAttribute("level", Byte.toString(status.getStatus()));
        for (CommandStatus.ResultInfo comp : status.getInfos(CommandStatus.SUCCESS)) {
            if (comp != null) {
                writer.startElement("ok");
                writer.writeAttribute("cmdName", comp.getPrefix());
                writer.writeAttribute("index", comp.getIndex());
                if (comp.getValue() != null) {
                    writer.writeCDATA(comp.getValue().toString());
                }
                writer.endElement("ok");
            }
        }
        for (CommandStatus.ResultInfo comp : status.getInfos(CommandStatus.WARNING)) {
            if (comp != null) {
                writer.startElement("warn");
                writer.writeAttribute("cmdName", comp.getPrefix());
                writer.writeAttribute("index", comp.getIndex());
                if (comp.getValue() != null) {
                    writer.writeCDATA(comp.getValue().toString());
                }
                writer.endElement("warn");
            }
        }
        for (CommandStatus.ResultInfo comp : status.getInfos(CommandStatus.ERROR)) {
            if (comp != null) {
                writer.startElement("error");
                writer.writeAttribute("cmdName", comp.getPrefix());
                writer.writeAttribute("index", comp.getIndex());
                if (comp.getValue() != null) {
                    writer.writeCDATA(comp.getValue().toString());
                }
                writer.endElement("error");
            }
        }
        writer.endElement("status");
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    //
    // LOG PART
    //

    public static StencilLog getLog() {
        return _Stencil._LOG;
    }

    public static <C extends _StencilContext> String logTrace(C stclContext, String format, Object... params) {
        return getLog().logTrace(stclContext, format, params);
    }

    public static <C extends _StencilContext> String logInfo(C stclContext, String format, Object... params) {
        return getLog().logInfo(stclContext, format, params);
    }

    public static <C extends _StencilContext> String logWarn(C stclContext, String format, Object... params) {
        return getLog().logWarn(stclContext, format, params);
    }

    public static <C extends _StencilContext> String logError(C stclContext, String format, Object... params) {
        return getLog().logError(stclContext, format, params);
    }

}
