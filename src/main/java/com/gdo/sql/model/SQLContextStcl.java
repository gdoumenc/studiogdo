/**
 * Copyright GDO - 2004
 */
package com.gdo.sql.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;

import com.gdo.context.model.ContextStcl;
import com.gdo.helper.ConverterHelper;
import com.gdo.helper.StringHelper;
import com.gdo.project.util.SqlUtils;
import com.gdo.sql.cmd.SelectQuery;
import com.gdo.sql.cmd.TestSqlConnection;
import com.gdo.sql.cmd.UpdateQuery;
import com.gdo.stencils.Result;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.event.IPropertyChangeListener;
import com.gdo.stencils.event.PropertyChangeEvent;
import com.gdo.stencils.facet.FacetResult;
import com.gdo.stencils.factory.StencilFactory;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.log.StencilLog;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.CalculatedBooleanPropertySlot;
import com.gdo.stencils.util.PathUtils;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class SQLContextStcl extends Stcl implements IPropertyChangeListener<StclContext, PStcl> {

    private static final String CONNECTION = "SQL_CONNECTION";

    // if set to true, no SQL call anyway
    private static final boolean NO_CALL = false;

    // if set to true, trace all SQL call
    private static final boolean TRACE_ALL = true;

    // if set to false, call only read-only SQL call
    private static final boolean EXECUTE_UPDATE = true;

    public static final String DRIVER = "com.mysql.jdbc.Driver";
    public static final String URL_INITIAL = "jdbc:mysql://";

    private Set<String> _initialized_table = new HashSet<String>();

    public interface Slot extends ContextStcl.Slot {
        String URL = "Url";
        String DATABASE = "DatabaseName";
        String USER = "UserName";
        String PASSWD = "Password";
        String CONNECTED = "Connected";
    }

    public interface Command extends ContextStcl.Command {
        String TEST_SQL_CONNECTION = "TestSqlConnection";
        String SELECT_QUERY = "SelectQuery";
        String UPDATE_QUERY = "UpdateQuery";
    }

    public SQLContextStcl(StclContext stclContext) {
        super(stclContext);

        // SLOT PART

        propSlot(Slot.URL, "");
        propSlot(Slot.DATABASE, "");
        propSlot(Slot.USER, "");
        propSlot(Slot.PASSWD, "");

        new ConnectedSlot(stclContext);

        // COMMAND PART

        command(Command.TEST_CONNEXION, TestSqlConnection.class);
        command(Command.TEST_SQL_CONNECTION, TestSqlConnection.class);
        command(Command.SELECT_QUERY, SelectQuery.class);
        command(Command.UPDATE_QUERY, UpdateQuery.class);
    }

    // add listeners to property changes
    @Override
    public void afterCompleted(StclContext stclContext, PStcl self) {
        super.afterCompleted(stclContext, self);
        self.plug(stclContext, self, PathUtils.compose(Slot.URL, Slot.LISTENERS));
        self.plug(stclContext, self, PathUtils.compose(Slot.DATABASE, Slot.LISTENERS));
        self.plug(stclContext, self, PathUtils.compose(Slot.PASSWD, Slot.LISTENERS));
        self.plug(stclContext, self, PathUtils.compose(Slot.USER, Slot.LISTENERS));
    }

    @Override
    public PStcl clone(StclContext stclContext, PSlot<StclContext, PStcl> slot, IKey key, PStcl self) {
        PStcl clone = super.clone(stclContext, slot, key, self);
        clone.setString(stclContext, Slot.URL, self.getNotExpandedString(stclContext, Slot.URL, URL_INITIAL));
        clone.setString(stclContext, Slot.DATABASE, self.getNotExpandedString(stclContext, Slot.DATABASE, ""));
        clone.setString(stclContext, Slot.PASSWD, self.getNotExpandedString(stclContext, Slot.PASSWD, ""));
        clone.setString(stclContext, Slot.USER, self.getNotExpandedString(stclContext, Slot.USER, ""));
        return clone;
    }

    @Override
    public void beforeClear(StclContext stclContext, PStcl self) {
        close(stclContext, self);
        super.beforeClear(stclContext, self);
    }

    /**
     * IPropertyChangeListener interface.
     */
    // if any parameter changes, then close the connection
    @Override
    public Result propertyChange(PropertyChangeEvent<StclContext, PStcl> evt) {
        PStcl prop = evt.getPluggedProperty();
        PStcl container = prop.getContainer(evt.getStencilContext());
        close(evt.getStencilContext(), container);
        return Result.success();
    }

    /**
     * Opens the connection and stores it in the stencil context.
     */
    public boolean connect(StclContext stclContext, PStcl self) {

        // does nothing if already connected
        if (isConnected(stclContext, self)) {
            return true;
        }

        // set connection
        try {

            // gets connection parameters
            String url = self.getString(stclContext, SQLContextStcl.Slot.URL, "");
            String databaseName = self.getString(stclContext, SQLContextStcl.Slot.DATABASE, "");
            String user = self.getString(stclContext, SQLContextStcl.Slot.USER, "");
            String passwd = self.getString(stclContext, SQLContextStcl.Slot.PASSWD, "");

            // tries connection
            MysqlDataSource data_source = new MysqlDataSource();
            data_source.setServerName(url);
            data_source.setDatabaseName(databaseName);
            data_source.setUser(user);
            data_source.setPassword(passwd);
            data_source.setNoDatetimeStringSync(true);
            Connection connection = data_source.getConnection();
            if (!connection.isValid(0)) {
                logError(stclContext, "not valid connection");
            }
            setConnection(stclContext, connection, self);
            logWarn(stclContext, "Connect to database %s", self);
            return isConnected(stclContext, self);
        } catch (Exception e) {
            logError(stclContext, "Exception on SQLContextStcl connect : %s", e);
            return false;
        }
    }

    public boolean isConnected(StclContext stclContext, PStcl self) {
        try {
            Connection connection = getConnection(stclContext, self);
            if (connection != null) {
                if (connection.isValid(0)) {
                    return true;
                }
                connection.close();
            }
        } catch (SQLException e) {
            // if exception then not connected
        }
        setConnection(stclContext, null, self);
        return false;
    }

    /**
     * Close the current connection.
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            the SQL context stencil.
     */
    private void close(StclContext stclContext, PStcl self) {
        Connection connection = getConnection(stclContext, self);
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                logWarn(stclContext, "Cannot close connection", e);
            }
        }
        setConnection(stclContext, null, self);
    }

    /**
     * Checks connection and returns a new statement.
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            the SQL context stencil.
     * @return the new sql statement.
     */
    public Statement getStatement(StclContext stclContext, PStcl self) {
        try {
            if (connect(stclContext, self)) {
                Connection connection = getConnection(stclContext, self);
                return connection.createStatement();
            }
        } catch (Exception e) {
            logError(stclContext, "Cannot create statement", e);
        }
        return null;
    }

    /**
     * Does an update query.
     * 
     * @param stclContext
     *            the stencil context.
     * @param query
     *            the updatation query.
     * @param self
     *            the SQL context stencil.
     */
    public Result updateQuery(StclContext stclContext, String query, PStcl self) {

        // if no sql call anyway
        if (NO_CALL) {
            return Result.success();
        }

        // perform sql call
        Statement stmt = getStatement(stclContext, self);
        if (stmt != null) {
            try {
                if (TRACE_ALL) {
                    String url = self.getString(stclContext, SQLContextStcl.Slot.URL, "");
                    String databaseName = self.getString(stclContext, SQLContextStcl.Slot.DATABASE, "");
                    logWarn(stclContext, "SQL (%s: %s) : %s", url, databaseName, query);
                }
                if (EXECUTE_UPDATE) {
                    stmt.executeUpdate(query);
                }
                return Result.success();
            } catch (Exception e) {
                logError(stclContext, "error on update query %s : %s", query, e);
                return Result.error(e);
            } finally {
                SqlUtils.closeStatement(stclContext, stmt);
            }
        }
        String msg = logError(stclContext, "cannot get statement for update query %s", query);
        return Result.error(msg);
    }

    public Result batchQuery(StclContext stclContext, String query, PStcl self) {

        // if no sql call anyway
        if (NO_CALL)
            return Result.success();

        // traces the query
        if (getLog().isTraceEnabled()) {
            String msg = "Execute updateQuery : " + query;
            getLog().trace(stclContext, msg);
        }

        // perform sql call
        Statement stmt = getStatement(stclContext, self);
        if (stmt != null) {
            try {
                stmt.addBatch(query);
                stmt.executeBatch();
                return Result.success();
            } catch (Exception e) {
                logError(stclContext, "query : %s, exception : %s", query, e);
                return Result.error(e);
            } finally {
                SqlUtils.closeStatement(stclContext, stmt);
            }
        }
        return Result.error("cannot create statement");
    }

    /**
     * Does a simple query. The <tt>ResultSet</tt> should be closed once used.
     * 
     * @param stclContext
     *            the stencil context.
     * @param query
     *            the updatation query.
     * @param self
     *            this stencil as a plugged stencil.
     */
    public ResultSet selectQuery(StclContext stclContext, String query, PStcl self) {

        // if no sql call anyway
        if (NO_CALL) {
            return null;
        }

        // perform sql call
        Statement stmt = getStatement(stclContext, self);
        if (stmt != null) {
            try {
                if (TRACE_ALL) {
                    String url = self.getString(stclContext, SQLContextStcl.Slot.URL, "");
                    String databaseName = self.getString(stclContext, SQLContextStcl.Slot.DATABASE, "");
                    logWarn(stclContext, "SQL (%s:%s) : %s", url, databaseName, query);
                }
                return stmt.executeQuery(query);
            } catch (Exception e) {
                logError(stclContext, "error on select query %s : %s", query, e);
                closeStatement(stclContext, stmt);
            }
        }
        logError(stclContext, "cannot get statement for select query %s", query);
        return null;
    }

    // queries for a string value
    public String queryString(StclContext stclContext, String query, PStcl self) {
        try {
            logTrace(stclContext, "Execute queryString : %s", query);
            ResultSet rs = selectQuery(stclContext, query, self);
            if (rs != null && rs.next()) {
                return rs.getString(1);
            }
            return "";
        } catch (Exception e) {
            logError(stclContext, "query : %s, exception : %s", query, e);
            return e.getMessage();
        }

    }

    // queries for an integer value
    public int queryInteger(StclContext stclContext, String query, PStcl self) {
        try {
            logTrace(stclContext, "Execute queryInteger : %s", query);
            ResultSet rs = selectQuery(stclContext, query, self);
            if (rs != null && rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            logError(stclContext, "query : %s, exception : %s", query, e);
        }
        return 0;
    }

    // queries last entered id
    public int queryLastInsertID(StclContext stclContext, PStcl self) {
        return queryInteger(stclContext, "SELECT LAST_INSERT_ID()", self);
    }

    public PStcl queryStencil(StclContext stclContext, String template, String query, PSlot<StclContext, PStcl> slot, String key, PStcl self) {
        ResultSet rs = selectQuery(stclContext, query, self);
        if (rs != null) {
            try {
                PStcl stcl = null;
                while (rs.next()) {
                    if (stcl == null) {
                        StencilFactory<StclContext, PStcl> factory = (StencilFactory<StclContext, PStcl>) stclContext.getStencilFactory();
                        stcl = factory.createPStencil(stclContext, slot, new Key<String>(key), template);
                    }
                    int cols = rs.getMetaData().getColumnCount();
                    for (int i = 1; i <= cols; i++) {
                        String path = rs.getMetaData().getColumnName(i);
                        String value = rs.getString(i);
                        try {
                            stcl.setString(stclContext, path, value);
                        } catch (Exception e) {
                            logError(stclContext, "query : %s, exception : %s", query, e);
                        }
                    }
                }
                return stcl;
            } catch (Exception e) {
                logError(stclContext, "query : %s, exception : %s", query, e);
            } finally {
                SqlUtils.closeResultSet(stclContext, rs);
            }
        }
        return null;
    }

    // get table initialization status
    public boolean isTableInitialized(StclContext stclContext, String from, PStcl self) {
        return _initialized_table.contains(from);
    }

    // initialize the table
    public void initializeTable(StclContext stclContext, String from, PStcl self) {
        if (!isTableInitialized(stclContext, from, self)) {
            String query = String.format("DELETE FROM %s WHERE Id < 0", from);
            updateQuery(stclContext, query, self);
            _initialized_table.add(from);
        }
    }

    public Reader e4xFacet(StclContext stclContext, String query, char sep, char newLine, PStcl self) {
        if (StringUtils.isEmpty(query)) {
            if (getLog().isWarnEnabled()) {
                getLog().warn(stclContext, "SqlUtils:e4xFacet empty query...");
            }
            return StringHelper.EMPTY_STRING_READER;
        }
        try {
            Statement stmt = getStatement(stclContext, self);
            if (stmt != null) {
                ResultSet rs = stmt.executeQuery(query);
                if (rs != null) {
                    StringBuffer e4x = new StringBuffer();
                    boolean first = true;
                    while (rs.next()) {
                        int cols = rs.getMetaData().getColumnCount();

                        // write column title
                        if (first) {
                            for (int j = 1; j <= cols; j++) {
                                e4x.append(String.format("%s", rs.getMetaData().getColumnName(j)));
                                if (j < cols)
                                    e4x.append(sep);
                            }
                            e4x.append(newLine);
                        }

                        // write row content
                        for (int j = 1; j <= cols; j++) {
                            e4x.append(String.format("%s", rs.getString(j)));
                            if (j < cols)
                                e4x.append(sep);
                        }
                        e4x.append(newLine);
                        first = false;
                    }
                    return new StringReader(e4x.toString());
                }
            }
        } catch (SQLException e) {
            logError(stclContext, "query : %s, exception : %s", query, e);
        }
        return StringHelper.EMPTY_STRING_READER;
    }

    /**
     * @return an input stream on an excel file with one worksheet per excel
     *         query defined.
     */
    public FacetResult excelFileFacet(StclContext stclContext, ExcelQuery[] queries, PStcl self) {

        // checks parameters
        if (queries == null) {
            return new FacetResult(StringHelper.EMPTY_STRING_INPUT_STREAM, "text/html");
        }

        // trace
        if (getLog().isTraceEnabled()) {
            for (ExcelQuery query : queries) {
                String msg = "Execute excelFileFacet : " + query;
                getLog().trace(stclContext, msg);
            }
        }

        try {
            HSSFWorkbook workBook = new HSSFWorkbook();

            // gets SQL statement
            Statement stmt = getStatement(stclContext, self);
            if (stmt == null) {
                return new FacetResult(StringHelper.EMPTY_STRING_INPUT_STREAM, "text/html");
            }

            // ceates worksheets
            for (ExcelQuery query : queries) {
                HSSFSheet sheet = workBook.createSheet(query.getSheetName());
                ResultSet rs = stmt.executeQuery(query.getSqlQuery());
                if (rs != null) {
                    int cols = rs.getMetaData().getColumnCount();

                    // write title row
                    if (query.hasTitleRow()) {
                        HSSFRow title = sheet.createRow(0);
                        for (int j = 1; j <= cols; j++) {
                            HSSFCell cell = title.createCell(j - 1);
                            cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                            String str = rs.getMetaData().getColumnLabel(j);
                            if (query.getHeaders() != null && query.getHeaders().length >= j) {
                                if (StringUtils.isNotBlank(query.getHeaders()[j - 1]))
                                    str = query.getHeaders()[j - 1];
                            }
                            HSSFRichTextString value = new HSSFRichTextString(str);
                            cell.setCellValue(value);
                        }
                    }

                    // write row content
                    int i = 1;
                    while (rs.next()) {
                        HSSFRow row = sheet.createRow(i);
                        for (int j = 1; j <= cols; j++) {
                            HSSFCell cell = row.createCell(j - 1);

                            // gets type
                            String type = "string";
                            if (query.getTypes() != null && query.getTypes().length >= j) {
                                if (StringUtils.isNotBlank(query.getTypes()[j - 1]))
                                    type = query.getTypes()[j - 1];
                            }

                            // sets cell value
                            if ("int".equals(type)) {
                                DataFormat format = workBook.createDataFormat();
                                CellStyle style = workBook.createCellStyle();
                                style.setDataFormat(format.getFormat("0"));
                                cell.setCellStyle(style);
                                int val = rs.getInt(j);
                                cell.setCellValue(val);
                            } else if ("boolean_int".equals(type)) {
                                DataFormat format = workBook.createDataFormat();
                                CellStyle style = workBook.createCellStyle();
                                style.setDataFormat(format.getFormat("0"));
                                cell.setCellStyle(style);
                                String str = rs.getString(j);
                                if (StringUtils.isNotEmpty(str)) {
                                    Boolean b = ConverterHelper.parseBoolean(str);
                                    int val = b ? 1 : 0;
                                    cell.setCellValue(val);
                                }
                            } else if ("cent".equals(type)) {
                                DataFormat format = workBook.createDataFormat();
                                CellStyle style = workBook.createCellStyle();
                                style.setDataFormat(format.getFormat("0\\.00€"));
                                cell.setCellStyle(style);
                                cell.setCellValue(rs.getInt(j));
                            } else {
                                String str = rs.getString(j);
                                if (StringUtils.isNotEmpty(str)) {
                                    str = new String(str.getBytes(StclContext.getCharacterEncoding()));
                                    HSSFRichTextString value = new HSSFRichTextString(str);
                                    cell.setCellValue(value);
                                }
                            }
                        }
                        i++;
                    }
                }
            }

            // close statement
            SqlUtils.closeStatement(stclContext, stmt);

            // use temporary file stream
            File file = File.createTempFile("sql", null);
            file.deleteOnExit();
            FileOutputStream out = new FileOutputStream(file);
            workBook.write(out);
            out.flush();
            out.close();
            FacetResult result = new FacetResult(new FileInputStream(file), "application/vnd.ms-excel");
            result.setContentLength(file.length());
            return result;
        } catch (Exception e) {
            logError(stclContext, "sql context excelFacet exception %s (cannot create SQL temporary file)", e.getMessage());
            InputStream is = IOUtils.toInputStream(e.getMessage());
            FacetResult result = new FacetResult(is, "text/html");
            return result;
        }
    }

    // connections are stored in the request attributes
    @SuppressWarnings("unchecked")
    private Connection getConnection(StclContext stclContext, PStcl self) {
        HttpServletRequest request = stclContext.getRequest();
        Map<String, Connection> cons = (Map<String, Connection>) request.getAttribute(CONNECTION);
        if (cons == null)
            return null;
        return cons.get(self.getUId(stclContext));
    }

    @SuppressWarnings("unchecked")
    private void setConnection(StclContext stclContext, Connection connection, PStcl self) {
        HttpServletRequest request = stclContext.getRequest();
        Map<String, Connection> cons = (Map<String, Connection>) request.getAttribute(CONNECTION);
        if (cons == null) {
            cons = new ConcurrentHashMap<String, Connection>();
            request.setAttribute(CONNECTION, cons);
        }
        if (connection == null)
            cons.remove(self.getUId(stclContext));
        else
            cons.put(self.getUId(stclContext), connection);
    }

    @SuppressWarnings("unchecked")
    public static void closeConnection(StclContext stclContext) {
        try {
            HttpServletRequest request = stclContext.getRequest();
            Map<String, Connection> cons = (Map<String, Connection>) request.getAttribute(CONNECTION);
            if (cons != null) {
                for (Connection con : cons.values()) {
                    con.close();
                }
            }
        } catch (SQLException e) {
        }
    }

    private void closeStatement(StclContext stclContext, Statement stmt) {
        try {
            if (stmt != null)
                stmt.close();
        } catch (SQLException e) {
            logWarn(stclContext, e.toString());
        }
    }

    /**
     * Class to check the Connection is connected or not.
     */
    private class ConnectedSlot extends CalculatedBooleanPropertySlot<StclContext, PStcl> {
        public ConnectedSlot(StclContext stclContext) {
            super(stclContext, SQLContextStcl.this, Slot.CONNECTED);
        }

        @Override
        public boolean getBooleanValue(StclContext stclContext, PStcl self) {
            return isConnected(stclContext, self.getContainer(stclContext));
        }

        @Override
        public boolean setBooleanValue(StclContext stclContext, boolean value, PStcl self) {
            boolean old = isConnected(stclContext, self.getContainer(stclContext));
            try {
                if (value)
                    connect(stclContext, self);
                else
                    close(stclContext, self);
            } catch (Exception e) {
                return false;
            }
            return old;
        }
    }

    //
    // LOG PART
    //

    // redefines log for specific trace
    private static final StencilLog LOG = new StencilLog(SQLContextStcl.class);

    @Override
    public StencilLog getLog() {
        return LOG;
    }

}