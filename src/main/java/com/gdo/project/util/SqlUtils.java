package com.gdo.project.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

import com.gdo.helper.ConverterHelper;
import com.gdo.helper.StringHelper;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.log.StencilLog;
import com.gdo.stencils.plug.PStcl;

/**
 * <p>
 * Utility clas for SQL interface.
 * </p>

 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>

 * 
 * @author Guillaume Doumenc (<a>
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */
public class SqlUtils {

    // sql interface
    public static final String DEFAULT_SQL_DATE_FORMAT = "yyyy-MM-dd";

    private static final SqlUtils INSTANCE = new SqlUtils();

    private SqlUtils() {
        // utility class, disable instanciation
    }

    /**
     * Tests if a column exists in the result set.
     * 
     * @param field
     *            the label column searched.
     * @param rs
     *            the result set.
     * @return <tt>true</tt> if the column exists, <tt>false</tt> otherwise.
     */
    public static boolean hasColumn(String field, ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int numCol = meta.getColumnCount();

        for (int i = 1; i < numCol + 1; i++) {
            if (meta.getColumnLabel(i).equals(field)) {
                return true;
            }
        }
        return false;
    }

    private static void closeStatement(StclContext stclContext, Statement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            logWarn(stclContext, e.toString());
        }
    }

    /**
     * Returns the id of the element or NULL value to creates new value.
     * 
     * @param stclContext
     *            the stencil context.
     * @param stencil
     *            the stencil containing the numeric value.
     * @param slot
     *            the name of the slot containing the numeric value.
     * @return the numeric value or NULL.
     */
    public static String getSqlAutoIncrementKey(StclContext stclContext, PStcl stencil, String slot) {
        String id = stencil.getString(stclContext, slot, null);
        if (StringUtils.isBlank(id)) {
            return "NULL";
        }
        return id;
    }

    // retrieves SQL values from stencil properties
    public static String getStringFromStencil(StclContext stclContext, PStcl stencil, String slot) {
        return StringHelper.escapeSql(stencil.getString(stclContext, slot, ""));
    }

    public static int getIntFromStencil(StclContext stclContext, PStcl stencil, String slot) {
        return stencil.getInt(stclContext, slot, 0);
    }

    public static String getBooleanFromStencil(StclContext stclContext, PStcl stencil, String slot, String trueValue, String falseValue) {
        boolean value = stencil.getBoolean(stclContext, slot, false);
        return value ? trueValue : falseValue;
    }

    public static String setStringFromParameter(CommandContext<StclContext, PStcl> cmdContext, int index, PStcl stencil, String slot) {
        String value = cmdContext.getProperty(CommandContext.PARAM(index));
        if (value == null)
            value = "";
        stencil.setString(cmdContext.getStencilContext(), slot, value);
        return StringHelper.escapeSql(value);
    }

    @Deprecated
    public static String setStringFromResultSet(StclContext stclContext, ResultSet rs, String label, PStcl stencil, String slot) {
        String value = null;
        try {
            value = rs.getString(label);
        } catch (SQLException e) {
            if (getLog().isWarnEnabled())
                getLog().warn(stclContext, e);
        }
        if (value == null)
            value = "";
        stencil.setString(stclContext, slot, value);
        return StringHelper.escapeSql(value);
    }

    // returns 0 or 1 for int(1) type
    public static int setBooleanFromParameter(CommandContext<StclContext, PStcl> cmdContext, int index, PStcl stencil, String slot) {
        boolean value;
        String str = cmdContext.getProperty(CommandContext.PARAM(index));
        if (StringUtils.isEmpty(str))
            value = false;
        else
            value = ConverterHelper.parseBoolean(str);
        stencil.setBoolean(cmdContext.getStencilContext(), slot, value);
        return value ? 1 : 0;
    }

    @Deprecated
    public static void setBooleanFromResultSet(StclContext stclContext, ResultSet rs, String label, PStcl stencil, String slot, String trueValue) {
        String value = null;
        try {
            value = rs.getString(label);
        } catch (SQLException e) {
            if (getLog().isWarnEnabled())
                getLog().warn(stclContext, e);
        }
        if (value == null)
            value = "";
        stencil.setBoolean(stclContext, slot, value.equals(trueValue));
    }

    public static int setIntFromParameter(CommandContext<StclContext, PStcl> cmdContext, int index, PStcl stencil, String slot) {
        int value;
        String str = cmdContext.getProperty(CommandContext.PARAM(index));
        if (StringUtils.isEmpty(str))
            value = 0;
        else
            value = Integer.parseInt(str);
        stencil.setInt(cmdContext.getStencilContext(), slot, value);
        return value;
    }

    /**
     * Returns the date in datetime format.
     */
    @Deprecated
    public static String getDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date());
    }

    /**
     * Copies the connection in a new stencil context.
     * 
     * @param fromContext
     * @param toContext
     * @throws SQLException
     */
    @Deprecated
    public static void copyConnection(StclContext fromContext, StclContext toContext) throws SQLException {
        // Connection con = getConnection(fromContext);
        // toContext.putParameter(SqlUtils.class, SQL_DRIVER, con);
    }

    /**
     * Opens the connection and stores it in the stencil context.
     */
    /**
     * @param stclContext
     */
    @Deprecated
    public static Connection getConnection(StclContext stclContext) throws SQLException {
        /*
         * Connection con = (Connection) stclContext.getParameter(SqlUtils.class,
         * SQL_DRIVER); if (con == null || con.isClosed()) { String url =
         * stclContext.getConfigParameter(StudioConfig.JDBC_URL); String user =
         * stclContext.getConfigParameter(StudioConfig.JDBC_USERNAME); String passwd
         * = stclContext.getConfigParameter(StudioConfig.JDBC_PASSWORD); con =
         * DriverManager.getConnection(url, user, passwd);
         * //stclContext.putParameter(SqlUtils.class, SQL_DRIVER, con); } return
         * con;
         */
        return null;
    }

    @Deprecated
    public static Connection getConnection(String url, String user, String passwd) throws SQLException {
        return DriverManager.getConnection(url, user, passwd);
    }

    public static void update(StclContext stclContext, Query query) {
        Statement stmt = null;
        try {
            stmt = createStatement(stclContext);
            stmt.executeUpdate(query.query());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stmt != null)
                closeStatement(stclContext, stmt);
        }
    }

    @Deprecated
    public static void batchUpdate(StclContext stclContext, String[] queries) throws SQLException {
        Connection con = null;
        Statement stmt = null;
        try {
            con = getConnection(stclContext);
            stmt = con.createStatement();
            for (String query : queries) {
                if (!StringUtils.isEmpty(query))
                    stmt.addBatch(query);
            }
            stmt.executeBatch();
        } finally {
            if (stmt != null)
                closeStatement(stclContext, stmt);
        }
    }

    @Deprecated
    public static ResultSet query(StclContext stclContext, String query) throws SQLException {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            con = getConnection(stclContext);
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
        } finally {
            /**/
        }
        return rs;
    }

    @Deprecated
    public static Statement createStatement(StclContext stclContext) throws SQLException {
        try {
            return getConnection(stclContext).createStatement();
        } finally {
            /**/
        }
    }

    @Deprecated
    public static PreparedStatement prepareStatement(StclContext stclContext, String query) throws SQLException {
        try {
            return getConnection(stclContext).prepareStatement(query);
        } finally {
            /**/
        }
    }

    @Deprecated
    public static void preparedUpdate(PreparedStatement stmt) throws SQLException {
        try {
            stmt.executeUpdate();
        } finally {
            /**/
        }
    }

    @Deprecated
    public static ResultSet preparedSelect(PreparedStatement stmt) throws SQLException {
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery();
        } finally {
            /**/
        }
        return rs;
    }

    @Deprecated
    public static InputStream e4xFacet(StclContext stclContext, String query) {
        if (StringUtils.isEmpty(query)) {
            if (getLog().isWarnEnabled()) {
                getLog().warn(stclContext, "SqlUtils:e4xFacet empty query...");
            }
            return StringHelper.EMPTY_STRING_INPUT_STREAM;
        }
        try {
            PreparedStatement stmt = SqlUtils.prepareStatement(stclContext, query);
            ResultSet rs = SqlUtils.preparedSelect(stmt);
            if (rs != null) {
                StringBuffer e4x = new StringBuffer();
                e4x.append("<records>");
                while (rs.next()) {
                    e4x.append(" <record>\n");
                    int cols = rs.getMetaData().getColumnCount();
                    for (int j = 1; j <= cols; j++) {
                        String tag = rs.getMetaData().getColumnName(j);
                        String tag1 = new String(tag.getBytes(), "UTF-8"); // db
                        // connection
                        String value = rs.getString(j);
                        String value1 = new String(value.getBytes(), "ISO-8859-1"); // db
                        // value
                        e4x.append(String.format("  <%1$s>%2$s</%1$s>\n", tag1, value1));
                    }
                    e4x.append(" </record>\n");
                }
                e4x.append("</records>\n");
                return new ByteArrayInputStream(e4x.toString().getBytes());
            }
        } catch (SQLException e) {
            if (getLog().isWarnEnabled()) {
                getLog().warn(stclContext, "SqlUtils:e4xFacet error", e);
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return StringHelper.EMPTY_STRING_INPUT_STREAM;
    }

    /**
     * @param stclContext
     * @param col
     * @param value
     */
    @Deprecated
    public static StringReader e4xFacet(StclContext stclContext, String col, int value) {
        StringBuffer e4x = new StringBuffer();
        e4x.append("<facet>\n");
        e4x.append(" <record>\n");
        e4x.append(String.format(" <%1$s><![CDATA[%2$d]]></%1$s>\n", col, value));
        e4x.append(" </record>\n");
        e4x.append("</facet>\n");
        return new StringReader(e4x.toString());
    }

    @Deprecated
    public static InputStream e4xFacet(StclContext stclContext, String query, char sep, char newLine) {
        if (StringUtils.isEmpty(query)) {
            if (getLog().isWarnEnabled()) {
                getLog().warn(stclContext, "SqlUtils:e4xFacet empty query...");
            }
            return StringHelper.EMPTY_STRING_INPUT_STREAM;
        }
        try {
            ResultSet rs = query(stclContext, query);
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
                return new ByteArrayInputStream(e4x.toString().getBytes());
            }
        } catch (SQLException e) {
            if (getLog().isWarnEnabled()) {
                getLog().warn(stclContext, "SqlUtils:e4xFacet error", e);
            }
        }
        return StringHelper.EMPTY_STRING_INPUT_STREAM;
    }

    /**
     * Creates a new empty SQL association structure (like PHP association). A
     * SQL association defines one unique element in a database table.
     * 
     * @param table
     *            the table where all specific field values are defined.
     * @return an SQL association structure.
     */
    public static SqlAssoc newSqlAssoc(String from, String table, String alias, PStcl stcl) {
        return INSTANCE.new SqlAssoc(from, table, alias, stcl);
    }

    /**
     * Creates a new empty SQL condition structure. A SQL association defines
     * one unique element in a database table.
     * 
     * @param table
     *            the table where all specific field values are defined.
     * @return an SQL association structure.
     */
    public static SqlCondition newSqlCondition(String from, String table, String alias, PStcl stcl) {
        return INSTANCE.new SqlCondition(from, table, alias, stcl);
    }

    /**
     * A map with a table associated and a stencil for value source.
     */
    private abstract class SqlMap extends HashMap<String, String> {
        private static final long serialVersionUID = 1L;

        protected String _from;// multi table selection
        protected String _table;// the database table name
        protected String _alias; // the database table alias
        protected PStcl _stcl; // the stencil used

        public SqlMap(String from, String table, String alias, PStcl stcl) {
            _from = from;
            _table = table;
            _stcl = stcl;
            _alias = alias;
        }

        /**
         * Adds a new string value to this association.
         * 
         * @param label
         *            the database field label.
         * @param value
         *            the value.
         * @return the field value.
         */
        public String push(String label, String value) {
            put(label, "'" + StringHelper.escapeSql(value) + "'");
            return value;
        }

        public String push(String label, String value, int max) {
            if (value.length() > max) {
                value = value.substring(0, max - 1);
            }
            return push(label, value);
        }

        public void pushNull(String label) {
            put(label, "NULL");
        }

        /**
         * Adds a new auto incremented field value to this association.
         * 
         * @param stclContext
         *            the stencil context.
         * @param label
         *            the database field label.
         * @param slot
         *            the slot path where the value can be found.
         * @return the field value.
         */
        public String pushAutoIncrement(StclContext stclContext, String label, String slot) {
            String value = SqlUtils.getSqlAutoIncrementKey(stclContext, _stcl, slot);
            put(label, value);
            return value;
        }

        /**
         * Adds a new auto incremented field value to this association if the
         * slot and the label field are same (which is certainly a very good
         * idea).
         * 
         * @param stclContext
         *            the stencil context.
         * @param slot
         *            the slot path where the value can be found.
         * @return the field value.
         */
        public String pushAutoIncrement(StclContext stclContext, String slot) {
            return pushAutoIncrement(stclContext, "`" + slot + "`", slot);
        }

        /**
         * Adds a new string field value to this association.
         * 
         * @param stclContext
         *            the stencil context.
         * @param label
         *            the database field label.
         * @param slot
         *            the slot path where the value can be found.
         * @return the field value.
         */
        public String pushString(StclContext stclContext, String label, String slot) {
            String value = _stcl.getString(stclContext, slot, "");
            push(label, value);
            return value;
        }

        /**
         * Adds a new string field value to this association.
         * 
         * @param stclContext
         *            the stencil context.
         * @param label
         *            the database field label.
         * @param slot
         *            the slot path where the value can be found.
         * @param max
         *            the maximum size of the string to be set.
         * @return the field value.
         */
        public String pushString(StclContext stclContext, String label, String slot, int max) {
            String value = _stcl.getString(stclContext, slot, "");
            if (value.length() > max) {
                value = value.substring(0, max - 1);
            }
            push(label, value);
            return value;
        }

        /**
         * Adds a new string field value (or <tt>NULL</tt> if value is blank)to
         * this association.
         * 
         * @param stclContext
         *            the stencil context.
         * @param label
         *            the database field label.
         * @param slot
         *            the slot path where the value can be found.
         * @return the field value.
         */
        public String pushStringOrNull(StclContext stclContext, String label, String slot) {
            String value = _stcl.getString(stclContext, slot, "");
            if (StringUtils.isNotBlank(value)) {
                push(label, value);
            } else {
                pushNull(label);
            }
            return value;
        }

        /**
         * Adds a new string field value to this association if the slot and the
         * label field are same (which is certainly a very good idea).
         * 
         * @param stclContext
         *            the stencil context.
         * @param slot
         *            the slot path where the value can be found.
         * @return the field value.
         */
        public String pushString(StclContext stclContext, String slot) {
            return pushString(stclContext, "`" + slot + "`", slot);
        }

        /**
         * Adds a new string field value to this association if the slot and the
         * label field are same (which is certainly a very good idea).
         * 
         * @param stclContext
         *            the stencil context.
         * @param slot
         *            the slot path where the value can be found.
         * @param max
         *            the maximum size of the string to be set.
         * @return the field value.
         */
        public String pushString(StclContext stclContext, String slot, int max) {
            return pushString(stclContext, "`" + slot + "`", slot, max);
        }

        /**
         * Adds a new string field value (or <tt>NULL</tt> if value is blank) to
         * this association if the slot and the label field are same (which is
         * certainly a very good idea).
         * 
         * @param stclContext
         *            the stencil context.
         * @param slot
         *            the slot path where the value can be found.
         * @return the field value.
         */
        public String pushStringOrNull(StclContext stclContext, String slot) {
            return pushStringOrNull(stclContext, "`" + slot + "`", slot);
        }

        /**
         * Adds a new integer field value to this association.
         * 
         * @param stclContext
         *            the stencil context.
         * @param label
         *            the database field label.
         * @param slot
         *            the slot path where the value can be found.
         * @return the field value.
         */
        public int pushInt(StclContext stclContext, String label, String slot) {
            int value = _stcl.getInt(stclContext, slot, 0);
            push(label, Integer.toString(value));
            return value;
        }

        /**
         * Adds a new integer field value to this association if the slot and
         * the label field are same (which is certainly a very good idea).
         * 
         * @param stclContext
         *            the stencil context.
         * @param slot
         *            the slot path where the value can be found.
         * @return the field value.
         */
        public int pushInt(StclContext stclContext, String slot) {
            return pushInt(stclContext, "`" + slot + "`", slot);
        }

        /**
         * Adds a new string field value to this association.
         * 
         * @param stclContext
         *            the stencil context.
         * @param label
         *            the database field label.
         * @param slot
         *            the slot path where the value can be found.
         * @return the field value.
         */
        public String pushEnum(StclContext stclContext, String label, String slot) {
            String value = _stcl.getString(stclContext, slot, "");
            if (StringUtils.isNotBlank(value)) {
                push(label, value);
            }
            return value;
        }

        /**
         * Adds a new enum field value to this association if the slot and the
         * label field are same (which is certainly a very good idea).
         * 
         * @param stclContext
         *            the stencil context.
         * @param slot
         *            the slot path where the value can be found.
         * @return the field value.
         */
        public String pushEnum(StclContext stclContext, String slot) {
            return pushEnum(stclContext, "`" + slot + "`", slot);
        }

        /**
         * Adds a new integer field value to this association which value is the
         * id of the stencil in the slot.
         * 
         * @param stclContext
         *            the stencil context.
         * @param label
         *            the database field label.
         * @param slot
         *            the slot path where the value can be found.
         * @return the id of the stencil in the slot.
         */
        public int pushId(StclContext stclContext, String label, String slot) {

            // no stencil plugged, then push NULL as may have been removed
            PStcl stcl = _stcl.getStencil(stclContext, slot);
            if (stcl.isNull()) {
                pushNull(label);
                return 0;
            }

            // id may be negative (do nothing if id=0)
            int value = stcl.getInt(stclContext, "Id", 0);
            if (value != 0) {
                push(label, Integer.toString(value));
            }

            // returns value stored
            return value;
        }

        /**
         * Adds a new integer field value to this association which value is the
         * id of the stencil in the slot.
         * 
         * @param stclContext
         *            the stencil context.
         * @param slot
         *            the slot path where the value can be found.
         * @return the id of the stencil in the slot (<tt>0</tt> if
         *         <tt>NULL</tt>).
         */
        public int pushId(StclContext stclContext, String slot) {
            return pushId(stclContext, "`" + slot + "`", slot);
        }

        /**
         * Adds a new boolean field value (0 or 1) to this association.
         * 
         * @param stclContext
         *            the stencil context.
         * @param label
         *            the database field label.
         * @param slot
         *            the slot path where the value can be found.
         * @param trueValue
         *            the string value stored in database for true.
         * @param falseValue
         *            the string value stored in database for false.
         * @return the field value.
         */
        public boolean pushBoolean(StclContext stclContext, String label, String slot, String trueValue, String falseValue) {
            boolean value = _stcl.getBoolean(stclContext, slot, false);
            push(label, (value ? trueValue : falseValue));
            return value;
        }

        /**
         * Adds a new boolean field value (0 or 1) to this association if the
         * slot and the label field are same (which is certainly a very good
         * idea).
         * 
         * @param stclContext
         *            the stencil context.
         * @param slot
         *            the slot path where the value can be found.
         * @param trueValue
         *            the string value stored in database for true.
         * @param falseValue
         *            the string value stored in database for false.
         * @return the field value.
         */
        public boolean pushBoolean(StclContext stclContext, String slot, String trueValue, String falseValue) {
            return pushBoolean(stclContext, "`" + slot + "`", slot, trueValue, falseValue);
        }

        public boolean pushBoolean(StclContext stclContext, String slot) {
            return pushBoolean(stclContext, "`" + slot + "`", slot, "1", "0");
        }

        /**
         * Adds a new double field value to this association.
         * 
         * @param stclContext
         *            the stencil context.
         * @param label
         *            the database field label.
         * @param slot
         *            the slot path where the value can be found.
         * @return the field value.
         */
        public double pushDouble(StclContext stclContext, String label, String slot) {
            double value = _stcl.getDouble(stclContext, slot, 0);
            push(label, Double.toString(value));
            return value;
        }

        public double pushDouble(StclContext stclContext, String slot) {
            return pushDouble(stclContext, "`" + slot + "`", slot);
        }

        /**
         * Adds a new date field value (or <tt>NOW()</tt> if date is blank) to
         * this association.
         * 
         * @param stclContext
         *            the stencil context.
         * @param label
         *            the database field label.
         * @param slot
         *            the slot path where the value can be found.
         * @param formatRead
         *            the date format in property (for NOW value and to be able
         *            to read it).
         * @return the field value.
         */
        public String pushDate(StclContext stclContext, String label, String slot, String formatRead) {
            String value = _stcl.getString(stclContext, slot, "");
            if (StringUtils.isBlank(value)) {
                DateFormat dateFormat = new SimpleDateFormat(formatRead);
                value = dateFormat.format(new Date());
                _stcl.setString(stclContext, slot, value);
            }
            push(label, ConverterHelper.dateConverter(value, formatRead, "yyyy-MM-dd"));
            return value;
        }

        /**
         * Adds a date field value (or <tt>NOW()</tt> if date is blank) to this
         * association if the slot and the label field are same (which is
         * certainly a very good idea).
         * 
         * @param stclContext
         *            the stencil context.
         * @param slot
         *            the slot path where the value can be found.
         * @param formatRead
         *            the date format in property (for NOW value and to be able
         *            to read it).
         * @return the field value (not converted).
         */

        public String pushDate(StclContext stclContext, String slot, String formatRead) {
            return pushDate(stclContext, "`" + slot + "`", slot, formatRead);
        }

        /**
         * Adds a new date field value (or <tt>NULL</tt> if no date) to this
         * association.
         * 
         * @param stclContext
         *            the stencil context.
         * @param slot
         *            the slot path where the value can be found.
         * @param format
         *            the date format in property (to be converted).
         * @return the date value (not converted).
         */
        public String pushDateOrNull(StclContext stclContext, String label, String slot, String format) {
            String value = _stcl.getString(stclContext, slot, "");
            if (StringUtils.isNotBlank(value)) {
                push(label, ConverterHelper.dateConverter(value, format, "yyyy-MM-dd"));
            } else {
                pushNull(label);
            }
            return value;
        }

        /**
         * Adds a new date field value (or <tt>NULL</tt> if no date) to this
         * association.
         * 
         * @param stclContext
         *            the stencil context.
         * @param slot
         *            the slot path where the value can be found.
         * @param format
         *            the date format in property (to be converted).
         * @return the date value (not converted).
         */
        public String pushDateTimeOrNull(StclContext stclContext, String label, String slot, String format) {
            String value = _stcl.getString(stclContext, slot, "");
            if (StringUtils.isNotBlank(value)) {
                push(label, ConverterHelper.dateConverter(value, format, "yyyy-MM-dd HH:mm:ss"));
            } else {
                pushNull(label);
            }
            return value;
        }

        /**
         * Adds a new date field value (or <tt>NULL</tt> if no date) to this
         * association if the slot and the label field are same (which is
         * certainly a very good idea).
         * 
         * @param stclContext
         *            the stencil context.
         * @param slot
         *            the slot path where the value can be found.
         * @param format
         *            the date format in property (to be converted).
         * @return the date value (not converted).
         */
        public String pushDateTimeOrNull(StclContext stclContext, String slot, String format) {
            return pushDateTimeOrNull(stclContext, "`" + slot + "`", slot, format);
        }

        /**
         * Adds a new date field value (or <tt>NULL</tt> if no date) to this
         * association if the slot and the label field are same (which is
         * certainly a very good idea).
         * 
         * @param stclContext
         *            the stencil context.
         * @param slot
         *            the slot path where the value can be found.
         * @param format
         *            the date format in property (to be converted).
         * @return the date value (not converted).
         */
        public String pushDateOrNull(StclContext stclContext, String slot, String format) {
            return pushDateOrNull(stclContext, "`" + slot + "`", slot, format);
        }

        /**
         * Adds current date field value to this association.
         * 
         * @param stclContext
         *            the stencil context.
         * @param label
         *            the database field label.
         * @param slot
         *            the slot path where the value can be found.
         * @param format_read
         *            the date format in property.
         * @return the field value.
         */
        public String pushDateNow(StclContext stclContext, String label, String slot, String format_read) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format_read);
            Date now = new Date();
            String strDate = dateFormat.format(now);
            _stcl.setString(stclContext, slot, strDate);
            push(label, ConverterHelper.dateConverter(strDate, format_read, "yyyy-MM-dd"));
            return strDate;
        }

        /**
         * Adds a current date field value to this association if the slot and
         * the label field are same (which is certainly a very good idea).
         * 
         * @param stclContext
         *            the stencil context.
         * @param slot
         *            the slot path where the value can be found.
         * @param format_read
         *            the date format in property.
         * @return the field value.
         */
        public String pushDateNow(StclContext stclContext, String slot, String format_read) {
            return pushDateNow(stclContext, "`" + slot + "`", slot, format_read);
        }

        public String pushDateOrNow(StclContext stclContext, String label, String slot, String format_read) {
            String value = _stcl.getString(stclContext, slot, "");
            if (StringUtils.isNotBlank(value)) {
                push(label, ConverterHelper.dateConverter(value, format_read, "yyyy-MM-dd"));
            } else {
                pushDateNow(stclContext, "`" + slot + "`", slot, format_read);
            }
            return value;
        }

        public String pushDateOrNow(StclContext stclContext, String slot, String format_read) {
            return pushDateOrNow(stclContext, "`" + slot + "`", slot, format_read);
        }

        public String pushDateTimeNow(StclContext stclContext, String slot) {
            return pushDateTimeNow(stclContext, "`" + slot + "`", slot);
        }

        public String pushDateTimeNow(StclContext stclContext, String label, String slot) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date now = new Date();
            String strDate = dateFormat.format(now);
            _stcl.setString(stclContext, slot, strDate);
            push(label, strDate);
            return strDate;
        }

        public String pushTimeOrNull(StclContext stclContext, String label, String slot) {
            String value = _stcl.getString(stclContext, slot, "");
            if (StringUtils.isNotBlank(value)) {
                push(label, value);
            } else {
                pushNull(label);
            }
            return value;
        }

        public String pushTimeOrNull(StclContext stclContext, String slot) {
            return pushTimeOrNull(stclContext, "`" + slot + "`", slot);
        }

        /**
         * Adds current time value to this association.
         * 
         * @param stclContext
         *            the stencil context.
         * @param label
         *            the database field label.
         * @param slot
         *            the slot path where the value can be found.
         * @return the field value.
         */
        public String pushTimeNow(StclContext stclContext, String label, String slot) {
            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
            Date now = new Date();
            String strTime = sdfTime.format(now);
            _stcl.setString(stclContext, slot, strTime);
            pushString(stclContext, label, slot);
            return strTime;
        }

        /**
         * Adds current time value to this association if the slot and the label
         * field are same (which is certainly a very good idea).
         * 
         * @param stclContext
         *            the stencil context.
         * @param slot
         *            the slot path where the value can be found.
         * @return the field value.
         */
        public String pushTimeNow(StclContext stclContext, String slot) {
            return pushTimeNow(stclContext, "`" + slot + "`", slot);
        }

        /**
         * Removes the value from the association.
         * 
         * @param stclContext
         *            the stencil context.
         * @param slot
         *            the slot not put in SQL query.
         * @return the key removed.
         */
        public String pop(StclContext stclContext, String slot) {
            return remove("`" + slot + "`");
        }

    }

    public class SqlAssoc extends SqlMap {

        private static final long serialVersionUID = 1L;

        public SqlAssoc(String from, String table, String alias, PStcl stcl) {
            super(from, table, alias, stcl);
        }

        /**
         * Returns the insertion query for this SQL association.
         * 
         * @return the insertion query for this SQL association.
         */
        public String getInsertQuery() {
            StringBuffer labels = new StringBuffer();
            for (String label : keySet()) {
                if (labels.length() > 0)
                    labels.append(',');
                labels.append(label);
            }
            StringBuffer values = new StringBuffer();
            for (String value : values()) {
                if (values.length() > 0)
                    values.append(',');
                values.append(value);
            }

            String table = _table;
            if (table.indexOf("`") < 0)
                table = "`" + table + "`";

            String query = String.format("INSERT INTO %s (%s) VALUES (%s);", table, labels, values);
            return query;
        }

        /**
         * Returns the update query for this SQL association.
         * 
         * @return the update query for this SQL association.
         */
        public String getUpdateQuery(SqlCondition where) {
            StringBuffer set = new StringBuffer();
            for (String label : keySet()) {
                if (set.length() > 0)
                    set.append(',');
                if (StringUtils.isNotBlank(_alias) && label.indexOf('.') == -1) {
                    set.append(_alias).append('.');
                }
                set.append(label).append("=").append(get(label));
            }
            String query = String.format("UPDATE %s SET %s %s", _from, set, where.getAndWhereQuery());
            return query;
        }
    }

    public class SqlCondition extends SqlMap {

        private static final long serialVersionUID = 1L;

        public SqlCondition(String from, String table, String alias, PStcl stcl) {
            super(from, table, alias, stcl);
        }

        String getAndWhereQuery() {
            StringBuffer where = new StringBuffer();
            for (String label : keySet()) {
                if (where.length() > 0)
                    where.append(" AND ");
                if (StringUtils.isNotBlank(_alias) && label.indexOf('.') == -1) {
                    where.append(_alias).append('.');
                }
                where.append(label).append('=').append(get(label));
            }
            return String.format("WHERE %s", where);
        }

        String getOrWhereQuery() {
            StringBuffer where = new StringBuffer();
            for (String label : keySet()) {
                if (where.length() > 0)
                    where.append(" OR ");
                where.append(label).append('=').append(get(label));
                if (StringUtils.isNotBlank(_alias) && label.indexOf('.') == -1) {
                    where.append(_alias).append('.');
                }
            }
            return String.format("WHERE %s", where);
        }

        /**
         * Returns the delete query for this SQL association.
         * 
         * @return the delete query for this SQL association.
         */
        public String getDeleteQuery() {
            String table = _table;
            if (table.indexOf("`") < 0)
                table = "`" + table + "`";

            if (StringUtils.isBlank(_alias)) {
                return String.format("DELETE FROM %s %s LIMIT 1", table, getAndWhereQuery());
            }
            table = String.format("%s %s", table, _alias);
            return String.format("DELETE %s FROM %s %s", _alias, table, getAndWhereQuery());
        }

    }

    //
    // LOG PART
    //

    private static final StencilLog LOG = new StencilLog(SqlUtils.class);

    protected static StencilLog getLog() {
        return LOG;
    }

    public static String logTrace(StclContext stclContext, String format, Object... params) {
        return getLog().logTrace(stclContext, format, params);
    }

    public static String logWarn(StclContext stclContext, String format, Object... params) {
        return getLog().logWarn(stclContext, format, params);
    }

    public static String logError(StclContext stclContext, String format, Object... params) {
        return getLog().logError(stclContext, format, params);
    }

}
