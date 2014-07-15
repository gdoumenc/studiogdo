package com.gdo.servlet;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.Base64;

import com.gdo.helper.ConverterHelper;
import com.gdo.helper.StringHelper;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;
import com.gdo.util.XmlStringWriter;

/**
 * Structure containing all common RPC entry parameters.
 */
public class RpcArgs {

    // stencils pool stored to save
    public static final int STENCILS_POOL_MAX_SIZE = 500;

    // private static final String POOL_ATTRIBUTE = "POOL_ATTRIBUTE";

    private int _transaction_id;
    private Map<String, String[]> _params;
    private FileItem _fileContent;
    private String[] _attrPathes;
    private boolean _acceptNoStencil;
    private String _enc;
    private String[] _facets;
    private String _format;
    private String[] _modes;
    private String _path;
    private Boolean _save;

    private PStcl _stencil; // stencil defined from path

    public RpcArgs(StclContext stclContext) throws Exception {
        Base64 encoder = new Base64();
        setRequestParams(stclContext);

        // transaction id
        int tid = getIntegerParameter(stclContext, RpcWrapper.TRANSACTION_ID_PARAM, 0);
        this._transaction_id = tid;

        // encoding charset is used for all parameters
        String enc = getStringParameter(stclContext, RpcWrapper.ENC_PARAM);
        this._enc = enc;

        // gets path
        this._path = getStringParameter(stclContext, RpcWrapper.PATH_PARAM);
        String apath = getStringParameter(stclContext, RpcWrapper.ABSOLUTE_PATH_PARAM);
        if (StringUtils.isBlank(this._path) && StringUtils.isNotBlank(apath)) {
            this._path = new String(encoder.decode(apath.getBytes()));
        }

        // gets complementary path
        // ap1 has predominence on p1
        String p1 = getStringParameter(stclContext, RpcWrapper.COMPLEMENT_PATH_PARAM);
        String ap1 = getStringParameter(stclContext, RpcWrapper.ABSOLUTE_COMPLEMENT_PATH_PARAM);
        if (StringUtils.isNotBlank(ap1)) {
            p1 = new String(encoder.decode(ap1.getBytes()));
            // if returned decoded path is empty, then throw error
            // WARNING: it does not throw error when encoded path is wrong but
            // return non empty string (though it should)
            if (StringUtils.isBlank(p1)) {
                RpcWrapper.logError(stclContext, "unable to decode ap1, ap1 was: \"%s\"", ap1);
                throw new Exception(String.format("unable to decode ap1, ap1 was: \"%s\"", ap1));
            }
        }
        if (StringUtils.isNotBlank(p1)) {
            this._path = PathUtils.compose(this._path, p1);
        }

        // gets complementary key
        // ak1 has predominence on k1
        String k1 = getStringParameter(stclContext, RpcWrapper.COMPLEMENT_KEY_PARAM);
        String ak1 = getStringParameter(stclContext, RpcWrapper.ABSOLUTE_COMPLEMENT_KEY_PARAM);
        if (StringUtils.isNotBlank(ak1)) {
            k1 = new String(encoder.decode(ak1.getBytes()));
            // if returned decoded path is empty, then throw error
            // WARNING: it does not throw error when encoded path is wrong but
            // return non empty string (though it should)
            if (StringUtils.isBlank(k1)) {
                RpcWrapper.logError(stclContext, "unable to decode ak1, ak1 was: \"%s\"", ak1);
                throw new Exception(String.format("unable to decode ak1, ak1 was: \"%s\"", ak1));
            }
        }
        if (StringUtils.isNotBlank(k1)) {
            this._path = PathUtils.createPath(this._path, k1);
        }

        // starts by encoding as will be used to decode all other ones
        this._acceptNoStencil = getBooleanParameter(stclContext, RpcWrapper.ACCEPT_NO_STENCIL, false);
        this._attrPathes = getStringParameters(stclContext, RpcWrapper.ATTRS_PARAM);
        this._facets = getStringParameters(stclContext, RpcWrapper.FACETS_PARAM);
        this._format = getStringParameter(stclContext, RpcWrapper.FORMAT_PARAM);
        this._modes = getStringParameters(stclContext, RpcWrapper.MODES_PARAM);
        this._save = getBooleanParameter(stclContext, RpcWrapper.SAVE_PARAM);

        // may force the locale of the context
        String locale = getStringParameter(stclContext, RpcWrapper.LOCALE_PARAM);
        if (StringUtils.isNotEmpty(locale)) {
            int pos = locale.indexOf('_');
            if (pos != -1) {
                String language = locale.substring(0, pos);
                String country = locale.substring(pos + 1);
                stclContext.setLocale(new Locale(language, country));
            } else {
                stclContext.setLocale(new Locale(locale));
            }
        }

        // sets this arguments in contexts
        stclContext.setRequestParameters(this);
    }

    /**
     * Gets the full parameters list.
     * 
     * @param stclContext
     *            the stencil context.
     * @return Gets the full parameters list.
     */
    public Map<String, String[]> getParams(StclContext stclContext) {
        return this._params;
    }

    public int getTransactionId() {
        return this._transaction_id;
    }

   /**
     * Gets the charset encoding used for this response.
     * 
     * @param stclContext
     *            the stencil context.
     * @return the character set encoding.
     */
    public String getCharacterEncoding(StclContext stclContext) {
        if (StringUtils.isNotBlank(this._enc)) {
            return this._enc;
        }
        String enc = stclContext.getRequest().getCharacterEncoding();
        if (StringUtils.isNotBlank(enc)) {
            return enc;
        }
        return StclContext.getCharacterEncoding();
    }

    /**
     * Returns the list of attribute pathes.
     * 
     * @return the list of attribute pathes.
     */
    public String[] getAttributePathes() {
        return this._attrPathes;
    }

    /**
     * Checks if the request contains uploaded file.
     * 
     * @return <tt>true</tt> if the request contains uploaded file.
     */
    public boolean hasUploadedFile() {
        return this._fileContent != null;
    }

    /**
     * Returns the file uploaded content.
     * 
     * @return the file uploaded content.
     */
    public FileItem fileUploadedContent() {
        return this._fileContent;
    }

    /**
     * Checks if the request can be called a non existing stencil.
     * 
     * @return <tt>true</tt> if the request can be called a non existing
     *         stencil.
     */
    public boolean acceptNoStencil() {
        return this._acceptNoStencil;
    }

    /**
     * Checks if request forces saving.
     * 
     * @return <tt>true</tt> if the project must be saved.
     */
    public Boolean mustSaveProject() {
        return this._save;
    }

    public void setSaveProject(Boolean bool) {
        this._save = bool;
    }

    public void setPath(String path) {
        this._path = path;
    }

    public String getPath() {
        return this._path;
    }

    /**
     * Gets stencil from path parameter.
     * 
     * @param stclContext
     *            the stencil context.
     * @return the stencil referenced.
     */
    public PStcl getStencilFromPath(StclContext stclContext) {
        if (this._stencil == null) {
            PStcl stcl = stclContext.getServletStcl();
            if (StringUtils.isBlank(this._path))
                this._stencil = stcl;
            else
                this._stencil = stcl.getStencil(stclContext, this._path);
        }
        return this._stencil;
    }

    public StencilIterator<StclContext, PStcl> getStencilsFromPath(StclContext stclContext) {
        PStcl stcl = stclContext.getServletStcl();

        if (StringUtils.isBlank(this._path)) {
            return StencilUtils.< StclContext, PStcl> iterator(stclContext, stcl, null);
        }
        return stcl.getStencils(stclContext, this._path);
    }

    /**
     * Adds the attributes values and the facets (if needed) of a stencil in
     * writer.
     * 
     * @param stclContext
     *            the stencil context.
     * @param stcl
     *            the stencil on which attributes are defined.
     * @param writeFacets
     *            determine if the facets should also be written.
     * @param writer
     *            the writer.
     */
    public void writeAttributes(StclContext stclContext, PStcl stcl, boolean writeFacets, XmlStringWriter writer) throws IOException {

        // checks the stencil is not null (acceptNoStencil may be set to true)
        if (stcl.isNull()) {
            return;
        }

        // write the number of attributes and facets
        writer.writeAttribute("attributes", this._attrPathes.length);
        if (writeFacets) {
            writer.writeAttribute("facets", this._facets.length);
        }

        // adds attributes and facets
        addAttributes(stclContext, writer, stcl, this._attrPathes);
        if (writeFacets) {
            addFacets(stclContext, writer, stcl, this._facets, this._modes);
        }

        addFormat(stclContext, writer, stcl, this._format);
    }

    /**
     * Add stencil attribute values in XML writer.
     * 
     * @param stclContext
     *            the stencil context.
     * @param writer
     *            the XML writer.
     * @param stcl
     *            the stencil.
     * @param attrPathes
     *            a multi-path to attributes.
     */
    private void addAttributes(StclContext stclContext, XmlStringWriter writer, PStcl stcl, String[] pathes) throws IOException {
        int index = 0;
        for (String att : pathes) {
            String value = stcl.getString(stclContext, att, null);
            writer.writeAttribute("attr" + index, value);
            index++;
        }
    }

    /**
     * Add stencil facets values in XML writer.
     * 
     * @param stclContext
     *            the stencil context.
     * @param writer
     *            the XML writer.
     * @param stcl
     *            the stencil.
     * @param facets
     *            a multi-path to facets.
     * @param modes
     *            a multi-path to modes.
     */
    private void addFacets(StclContext stclContext, XmlStringWriter writer, PStcl stcl, String[] facets, String[] modes) throws IOException {
        int index = 0;
        for (String facet : facets) {
            if (index >= modes.length) {
                break;
            }
            String mode = modes[index];
            String f = String.format("<$stencil facet='%s' mode='%s'/>", facet, mode);
            String value = stcl.format(stclContext, f);
            if (value != null) {
                writer.writeCDATAElement("facet" + index, value);
            }
            index++;
        }
    }

    /**
     * Add stencil format value in XML writer.
     * 
     * @param stclContext
     *            the stencil context.
     * @param writer
     *            the XML writer.
     * @param stcl
     *            the stencil.
     * @param format
     *            a format.
     */
    private void addFormat(StclContext stclContext, XmlStringWriter writer, PStcl stcl, String format) throws IOException {
        if (!StringUtils.isEmpty(format)) {
            writer.writeCDATAElement("format", stcl.format(stclContext, format));
        }
    }

    /**
     * Returns the string parameter defined by name.
     * 
     * @param stclContext
     *            the stencil context.
     * @param name
     *            the parameter name.
     * @param charset
     *            the charset used to encode parameter.
     * @return the string parameter.
     */
    public String getStringParameter(StclContext stclContext, String name) {
        if (this._params == null) {
            return null;
        }
        String[] values = this._params.get(name);
        if (values == null) {
            return null;
        }
        String value = this._params.get(name)[0];

        // flex issue (parameters are encoded in utf-8 not in iso ????)
        String type = stclContext.getRequest().getContentType();
        if (StringUtils.isNotEmpty(value) && "application/x-www-form-urlencoded".equals(type)) {
            try {
                value = new String(value.getBytes("iso-8859-1"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return value;
    }

    /**
     * Returns the string parameters defined by name (':' separated).
     * 
     * @param stclContext
     *            the stencil context.
     * @param name
     *            the parameter name.
     * @param charset
     *            the charset used to encode parameter.
     * @return the parameters as an array of string.
     */
    public String[] getStringParameters(StclContext stclContext, String name) {
        return StringHelper.splitShortString(getStringParameter(stclContext, name), PathUtils.MULTI);
    }

    /**
     * Returns the integer parameter defined by name.
     * 
     * @param stclContext
     *            the stencil context.
     * @param name
     *            the parameter name.
     * @param def
     *            the default value if the parameter is not defined.
     * @return the integer parameter.
     */
    public int getIntegerParameter(StclContext stclContext, String name, int def) {
        String value = getStringParameter(stclContext, name);
        if (StringUtils.isBlank(value)) {
            return def;
        }
        return Integer.parseInt(value);
    }

   /**
     * Returns the boolean parameter defined by name.
     * 
     * @param stclContext
     *            the stencil context.
     * @param name
     *            the parameter name.
     * @param def
     *            the default value if the parameter is not defined.
     * @param charset
     *            the charset used to encode parameter.
     * @return the boolean parameter.
     */
    public boolean getBooleanParameter(StclContext stclContext, String name, boolean def) {
        String value = getStringParameter(stclContext, name);
        if (StringUtils.isBlank(value)) {
            return new Boolean(def);
        }
        return ConverterHelper.parseBoolean(value);
    }

    public Boolean getBooleanParameter(StclContext stclContext, String name) {
        String value = getStringParameter(stclContext, name);
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return ConverterHelper.parseBoolean(value);
    }

    @SuppressWarnings("unchecked")
    private void setRequestParams(StclContext stclContext) {
        try {
            HttpServletRequest request = stclContext.getRequest();
            boolean isMultipart = ServletFileUpload.isMultipartContent(request);
            if (isMultipart) {
                this._params = new HashMap<String, String[]>();

                // multi part environment
                DiskFileItemFactory factory = new DiskFileItemFactory();
                String threshold = stclContext.getConfigParameter(StclContext.FILE_UPLOAD_THRESHOLD);
                if (!StringUtils.isEmpty(threshold)) {
                    factory.setSizeThreshold(Integer.parseInt(threshold));
                }
                String tmpDir = stclContext.getConfigParameter(StclContext.PROJECT_TMP_DIR);
                if (!StringUtils.isEmpty(tmpDir)) {
                    factory.setRepository(new File(tmpDir));
                }

                ServletFileUpload upload = new ServletFileUpload(factory);

                // get parameters
                List<FileItem> list = upload.parseRequest(request);
                Iterator<FileItem> items = list.iterator();
                while (items.hasNext()) {
                    FileItem item = items.next();
                    if (item.isFormField()) {
                        String value = item.getString();

                        // gets value from request
                        String type = stclContext.getRequest().getContentType();
                        if (StringUtils.isNotEmpty(value) && type.startsWith("multipart/form-data")) {
                            try {
                                value = new String(value.getBytes("iso-8859-1"));
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }

                        // recreates parameters map
                        String param = item.getFieldName();
                        if (this._params.containsKey(param)) {
                            String[] values = this._params.get(param);
                            this._params.put(param, ArrayUtils.<String> add(values, value));
                        } else {
                            this._params.put(param, new String[] { value });
                        }
                    } else {
                        this._fileContent = item;
                    }
                }
            } else {
                this._params = request.getParameterMap();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //
    // POOL PART
    //

    /*
     * @SuppressWarnings("unchecked") private Map<String, PStcl>
     * getStencilsPool(StclContext stclContext, boolean createIfNull) {
     * HttpSession session = stclContext.getHttpSession(); Map<String, PStcl>
     * stencilsPool = (Map<String, PStcl>) session.getAttribute(POOL_ATTRIBUTE);
     * if (stencilsPool == null && createIfNull) { stencilsPool = new
     * HashMap<String, PStcl>(STENCILS_POOL_MAX_SIZE);
     * session.setAttribute(POOL_ATTRIBUTE, stencilsPool); } return stencilsPool;
     * }
     */

    /**
     * Format arguments for trace.
     * 
     * @param entry
     *            the RPC entry.
     * @return a formatted string of all parameters.
     */
    public String formatForTrace() {
        return String.format("path=%s", this._path);
    }

}