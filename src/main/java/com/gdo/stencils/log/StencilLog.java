/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils.log;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.gdo.stencils.WrongPathException;
import com.gdo.stencils._StencilContext;

/**
 * <p>
 * Encapsulating log levels.
 * </p>
 * Only available levels are : error, warn and trace. All other levels are
 * declared deprecated. <blockquote>
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
public class StencilLog {
    private static final String SEP = " : ";

    private final Log _log; // real logger associated

    public StencilLog(Class<?> clazz) {
        _log = LogFactory.getLog(clazz);
    }

    /**
     * Is trace logging currently enabled?
     * 
     * <p>
     * Call this method to prevent having to perform expensive operations (for
     * example, String concatenation) when the log level is more than warn.
     * </p>
     * 
     * @return true if trace is enabled in the underlying logger.
     */
    public boolean isTraceEnabled() {
        return _log.isTraceEnabled();
    }

    /**
     * Is warn logging currently enabled?
     * 
     * <p>
     * Call this method to prevent having to perform expensive operations (for
     * example, String concatenation) when the log level is more than warn.
     * </p>
     * 
     * @return true if warn is enabled in the underlying logger.
     */
    public boolean isWarnEnabled() {
        return _log.isWarnEnabled();
    }

    /**
     * Is error logging currently enabled?
     * 
     * <p>
     * Call this method to prevent having to perform expensive operations (for
     * example, String concatenation) when the log level is more than warn.
     * </p>
     * 
     * @return true if error is enabled in the underlying logger.
     */
    public boolean isErrorEnabled() {
        return _log.isErrorEnabled();
    }

    public <C extends _StencilContext> String logTrace(C stclContext, String format, Object... params) {
        if (_log.isTraceEnabled()) {
            String msg = (params.length == 0) ? format : String.format(format, params);
            trace(stclContext, msg);
            return msg;
        }
        return "";
    }

    public <C extends _StencilContext> String logInfo(C stclContext, String format, Object... params) {
        String msg = (params.length == 0) ? format : String.format(format, params);
        info(stclContext, msg);
        return msg;
    }

    public <C extends _StencilContext> String logWarn(C stclContext, String format, Object... params) {
        String msg = (params.length == 0) ? format : String.format(format, params);
        warn(stclContext, msg);
        return msg;
    }

    public <C extends _StencilContext> String logError(C stclContext, String format, Object... params) {
        String msg = (params.length == 0) ? format : String.format(format, params);
        error(stclContext, msg);
        return msg;
    }

    public <C extends _StencilContext> void trace(C stclContext, Object arg) {
        _log.trace(argToString(stclContext, arg));
    }

    private <C extends _StencilContext> void info(C stclContext, Object arg) {
        _log.trace(argToString(stclContext, arg));
    }

    public <C extends _StencilContext> void warn(C stclContext, Object arg) {
        _log.warn(argToString(stclContext, arg));
    }

    public <C extends _StencilContext> void error(C stclContext, Object arg) {
        _log.error(argToString(stclContext, arg));
        _log.error(getStackTrace(new Throwable("error trace")));
    }

    // should be no more used
    @Deprecated
    public <C extends _StencilContext> void warn(C stclContext, Object arg, Throwable t) {
        if (arg == null) {
            return;
        }
        String msg = arg.toString();
        if (stclContext != null) {
            String name = stclContext.getName();
            if (name != null)
                msg = name + SEP + msg;
        }

        if (t != null)
            _log.error(msg + SEP + t);
        else
            _log.error(msg);

        // add trace is not wrong path exception
        if (t != null) {
            if (t instanceof WrongPathException)
                return;
            _log.warn(getStackTrace(t));
        }
    }

    @Deprecated
    public <C extends _StencilContext> void error(C stclContext, Exception e) {
        if (e == null)
            return;
        String msg = getStackTrace(e);
        if (stclContext != null) {
            String name = stclContext.getName();
            if (name != null)
                msg = name + SEP + msg;
        }
        _log.error(msg);
    }

    @Deprecated
    public <C extends _StencilContext> void error(C stclContext, Object arg, Throwable t) {
        if (arg == null)
            return;
        String msg = arg.toString();
        if (stclContext != null) {
            String name = stclContext.getName();
            if (name != null)
                msg = name + SEP + msg;
        }

        if (t != null)
            _log.error(msg + SEP + t);
        else
            _log.error(msg);

        // add trace is not wrong path exception
        if (t != null) {
            if (t instanceof WrongPathException)
                return;
            _log.error(getStackTrace(t));
        }
    }

    /**
     * Creates a readable message for trace.
     * 
     * @param stclContext
     *            the stencil context.
     * @param arg
     *            the trace argument.
     * @return a readable message for trace.
     */
    private <C extends _StencilContext> String argToString(C stclContext, Object arg) {
        if (arg == null) {
            return "";
        }
        if (stclContext == null) {
            return arg.toString();
        }
        String name = stclContext.getName();
        if (name != null) {
            StringBuffer msg = new StringBuffer(name);
            msg.append(SEP);
            msg.append(arg.toString());
            return msg.toString();
        }
        return arg.toString();
    }

    private String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

}
