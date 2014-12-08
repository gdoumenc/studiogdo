/**
 * Copyright GDO - 2005
 */
package com.gdo.sql.slot;

import java.lang.ref.SoftReference;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.gdo.helper.StringHelper;
import com.gdo.project.util.SqlUtils;
import com.gdo.project.util.SqlUtils.SqlAssoc;
import com.gdo.sql.model.SQLContextStcl;
import com.gdo.sql.model.SQLStcl;
import com.gdo.sql.model.SQLStcl.Slot;
import com.gdo.stencils.Result;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.Stcl.Command;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cond.PathCondition;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.iterator.ListIterator;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.MultiSlot;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;

/**
 * <p>
 * A SQL slot contains stencil constructed from database.
 * </p>
 * <p>
 * Temporary stencils may be stored in database using negative Id.
 * </p>

 * <p>
 * &copy; 2004, 2005 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo & Guillaume Doumenc. Use
 * is subject to license terms.
 * </p>

 * 
 * @author Guillaume Doumenc (<a>
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */
public abstract class SQLSlot extends MultiSlot<StclContext, PStcl> implements SQLSlotMixin {

    // prefix to retrieve the plugged stencil after insertion
    public static final String PLUGGED_PREFIX = "plugged";

    // initialized flags
    private boolean _initialized = false;

    // SQL context associated to this slot
    protected PStcl _sql_context;

    // cursor associated to this slot
    protected SQLCursor _cursor;

    // optimization
    boolean _read_only;
    boolean _load_all;
    int _stencil_context_uid; // context id

    // calculated map stored in each context id
    SoftReference<StencilIterator<StclContext, PStcl>> _stencil_context_map;

    /**
     * Simple constructor.
     * 
     * @param stclContext
     *            the stencil context.
     * @param in
     *            the stencil container.
     * @param name
     *            the slot name.
     * @param size
     *            the number of stencils stored in memory (when more place is
     *            needed then remove previous ones)
     */
    public SQLSlot(StclContext stclContext, Stcl in, String name, int size) {
        super(stclContext, in, name, PSlot.ANY, true);
        _cursor = new SQLCursor(name, size);
    }

    // creator for sub slot
    // the cursor should be defined later
    protected SQLSlot(StclContext stclContext, Stcl in, String name) {
        super(stclContext, in, name, PSlot.ANY, true);
    }

    public void readOnly() {
        _read_only = true;
    }

    public void loadAllAtStart() {
        _load_all = true;
    }

    /* (non-Javadoc)
     * @see com.gdo.stencils.slot._Slot#isCursorBased(com.gdo.stencils._StencilContext)
     */
    @Override
    public boolean isCursorBased(StclContext stclContext) {
        return true;
    }

    /**
     * Retrieves the slot cursor. This method should be redefined in case of
     * subslot.
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            this stencil as a plugged stencil.
     * @return the slot cursor.
     */
    public SQLCursor getCursor(StclContext stclContext, PSlot<StclContext, PStcl> self) {
        return _cursor;
    }

    /**
     * Utility function to retrieve cursor slot from resource slot.
     * 
     * @param stclContext
     *            the stencil context.
     * @param resource
     *            the resource slot name.
     * @param self
     *            this slot as a plugged slot.
     * @return the SQL cursor.
     */
    public SQLCursor getResourceSlotCursor(StclContext stclContext, String resource, PSlot<StclContext, PStcl> self) {
        PSlot<StclContext, PStcl> slot = self.getContainer().getResourceSlot(stclContext, resource);
        return ((SQLSlot) slot.getSlot()).getCursor(stclContext, slot);
    }

    @Override
    public String getProperty(StclContext stclContext, IKey key, String prop, PSlot<StclContext, PStcl> self) {
        SQLCursor cursor = getCursor(stclContext, self);
        String value = cursor.getPropertyValue(stclContext, self, key, prop);
        if (value != null) {
            return value;
        }
        return super.getProperty(stclContext, key, prop, self);
    }

    @Override
    public void setProperty(StclContext stclContext, String value, IKey key, String prop, PSlot<StclContext, PStcl> self) {
        SQLCursor cursor = getCursor(stclContext, self);
        cursor.addPropertyValue(stclContext, self, self, key, prop, value);
        super.setProperty(stclContext, value, key, prop, self); // only in
                                                                // cursor should
                                                                // be sufficient
    }

    /**
     * Initializes the slot (deleting all negative records).
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            this slot as a plugged slot.
     * @return <tt>true</tt> if the initialization has been done properly.
     * @throws Exception
     */
    protected boolean initialize(StclContext stclContext, PSlot<StclContext, PStcl> self) {
        if (!_initialized) {
            _initialized = true;

            // get SQL context
            PStcl sqlContext = getSQLContext(stclContext, self);
            if (StencilUtils.isNull(sqlContext)) {
                logWarn(stclContext, "No SQL context defined for slot %s for create stencil", self);
                return false;
            }

            // initializes SQL table
            if (!_read_only) {
                SQLContextStcl context = (SQLContextStcl) sqlContext.getReleasedStencil(stclContext);
                String from = getKeysFromWithoutAlias(stclContext, self).toString();
                context.initializeTable(stclContext, from, sqlContext);
            }

            // reads all values at initialization
            if (_load_all) {
                int size = getStencilsList(stclContext, null, self).size();
                _cursor.size(size);
            }
        }
        return true;
    }

    @Override
    public int size(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {

        // should be initialized before used
        if (!initialize(stclContext, self)) {
            return 0;
        }

        // get keys query
        String query = getSizeQuery(stclContext, cond, self);
        if (StringUtils.isBlank(query)) {
            logWarn(stclContext, "Count query not defined for slot %s for size", self);
            return 0;
        }

        // get SQL context
        PStcl sqlContext = getSQLContext(stclContext, self);
        if (StencilUtils.isNull(sqlContext)) {
            logWarn(stclContext, "No SQL context defined for slot %s for size", self);
            return 0;
        }

        // creates list
        SQLContextStcl sql = (SQLContextStcl) sqlContext.getReleasedStencil(stclContext);
        ResultSet rs = sql.selectQuery(stclContext, query, sqlContext);
        if (rs != null) {
            try {
                rs.next();
                return rs.getInt(1);
            } catch (Exception e) {
                logError(stclContext, e.toString());
            } finally {
                SQLContextStcl.closeResultSet(rs);
            }
        }
        return 0;
    }

    @Override
    public void clear() {
        _cursor.clear();
        super.clear();
    }

    /**
     * Returns the stencil context for query. The stencil context is get from
     * stencil container.
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            the plugged slot.
     * @return the stencil context.
     */
    public PStcl getSQLContext(StclContext stclContext, PSlot<StclContext, PStcl> self) {
        if (_sql_context == null) {
            PStcl container = self.getContainer();
            _sql_context = container.getStencil(stclContext, SQLStcl.Slot.SQL_CONTEXT);
        }
        return _sql_context;
    }

    /**
     * Abstract method to retrieve the template class of stencils defined in the
     * slot.
     * 
     * @param stclContext
     *            the stencil context.
     * @param rs
     *            result set containing all data on stencil.
     * @param self
     *            this slot as a plugged slot.
     * @return template used to construct the stencil with value as constructor
     *         parameter.
     */
    public abstract String getStencilTemplate(StclContext stclContext, ResultSet rs, PSlot<StclContext, PStcl> self);

    /**
     * Returns the list of parameters needed to create the stencil.
     * 
     * @param stclContext
     *            the stencil context.
     * @param rs
     *            result set containing all data on stencil.
     * @param self
     *            this slot as a plugged slot.
     * @return the parameters array.
     */
    public Object[] getStencilParameters(StclContext stclContext, ResultSet rs, PSlot<StclContext, PStcl> self) {
        return new Object[0];
    }

    //
    // SQL BEHAVIOR DEFINED THROUGH MIXIN
    //

    public String getDatabaseName(StclContext stclContext, PSlot<StclContext, PStcl> self) {
        return databaseName(stclContext, self);
    }

    public String getTableName(StclContext stclContext, PSlot<StclContext, PStcl> self) {
        return tableName(stclContext, self);
    }

    /**
     * Returns the database table alias name for getting stencil property. For
     * example the id is retrieved from
     * <tt>String.format("%s.Id", getTableAliasForProperty)</tt>
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            the plugged slot.
     * @return the database table alias.
     */
    public String getTableAliasForProperty(StclContext stclContext, PSlot<StclContext, PStcl> self) {
        return getTableName(stclContext, self);
    }

    /**
     * Returns a Id field which must be used to the query to retrieve the
     * stencil key. This specific clause is used only if no specific keys query
     * is defined.
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            the plugged slot.
     * @return the specific SQL id field.
     */
    public String getKeysIdField(StclContext stclContext, PSlot<StclContext, PStcl> self) {
        String alias = getTableAliasForProperty(stclContext, self);
        if (StringUtils.isNotBlank(alias)) {
            if (alias.indexOf("`") < 0)
                alias = "`" + alias + "`";
            return String.format("%s.Id", alias);
        }
        return "Id";
    }

    private String getKeysFromWithoutAlias(StclContext stclContext, PSlot<StclContext, PStcl> self) {
        StringBuffer from = new StringBuffer();
        String database = getDatabaseName(stclContext, self);
        if (StringUtils.isNotBlank(database)) {
            if (database.indexOf("`") < 0)
                from.append("`").append(database).append("`");
            else
                from.append(database);
            from.append(".");
        }
        String table = getTableName(stclContext, self);
        if (table.indexOf("`") < 0)
            from.append("`").append(table).append("`");
        else
            from.append(table);
        return from.toString();
    }

    public String getKeysSelect(StclContext stclContext, PSlot<StclContext, PStcl> self) {
        return keysSelect(stclContext, self);
    }

    public String getKeysFrom(StclContext stclContext, PSlot<StclContext, PStcl> self) {
        String from = getKeysFromWithoutAlias(stclContext, self);
        String alias = getTableAliasForProperty(stclContext, self);
        return keysFrom(stclContext, from, alias, self);
    }

    public String getKeysCondition(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
        String id_field = getKeysIdField(stclContext, self);
        return keysCondition(stclContext, cond, id_field, self);
    }

    public String getKeysOrder(StclContext stclContext, PSlot<StclContext, PStcl> self) {
        String id_field = getKeysIdField(stclContext, self);
        return order(stclContext, id_field, self);
    }

    public String getKeysGroup(StclContext stclContext, PSlot<StclContext, PStcl> self) {
        return group(stclContext, self);
    }

    public String getKeysLimit(StclContext stclContext, PSlot<StclContext, PStcl> self) {
        return limit(stclContext, self);
    }

    public String getKeysHaving(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
        return (cond == null) ? null : cond.toSQL(stclContext, getTableAliasForProperty(stclContext, self), self.getContainer());
    }

    //
    // INTERNAL CODE
    //

    private String getInternalKeysOrder(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {

        // check if key is contains "1 order"
        String key = PathCondition.getKeyCondition(cond);
        if (StringUtils.isNotBlank(key)) {
            String lower = key.toLowerCase();
            int start = lower.indexOf(" order ");
            if (start != -1) {
                int stop = lower.indexOf(" limit ");
                if (stop > 0) {
                    return key.substring(start + 1, stop);
                }
                return key.substring(start + 1);
            }
        }

        // no limit
        return getKeysOrder(stclContext, self);
    }

    private String getInternalKeysLimit(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {

        // check if key is contains " limit"
        String key = PathCondition.getKeyCondition(cond);
        if (StringUtils.isNotBlank(key)) {
            String lower = key.toLowerCase();
            int index = lower.indexOf(" limit ");
            if (index != -1) {
                return key.substring(index + 1);
            }
        }

        // otherwise returns defined limit
        return getKeysLimit(stclContext, self);
    }

    protected String getSizeCounter(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
        return String.format("Count(*)");
    }

    /**
     * Returns a specific SQL query which must be used to retrieve the slot
     * size.
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            this slot as a plugged slot.
     * @return the query to fetch all keys.
     */
    public String getSizeQuery(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
        String counter = getSizeCounter(stclContext, cond, self);
        return getInternalKeysQuery(stclContext, cond, counter, null, null, null, self);
        // String alias = getTableAliasForProperty(stclContext, self);
        // return getInternalKeysQuery(stclContext, cond, String.format("%s.*",
        // alias), null, null, self);
        // return String.format("SELECT Count(*) FROM (%s) _internal_counter",
        // keys);
    }

    /**
     * Returns a specific SQL query which must be used to retrieve the stencil
     * keys.
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            this slot as a plugged slot.
     * @return the query to fetch all keys.
     */
    public String getKeysQuery(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {

        // if only searching a stencil from its id
        if (cond != null) {
            try {
                int k = Integer.parseInt(PathCondition.getKeyCondition(cond));
                return getStencilQuery(stclContext, new Key(k), k > 0, self);
            } catch (NumberFormatException e) {
            }
        }

        return getInternalKeysQuery(stclContext, cond, true, true, self);
    }

    public String getKeysQueryWithoutCondition(StclContext stclContext, String key, PSlot<StclContext, PStcl> self) {
        return getStencilQuery(stclContext, new Key(key), false, self);
    }

    /**
     * Returns the SQL query constructed for this slot.
     * 
     * @param stclContext
     *            the stencil context.
     * @param cond
     *            the stencil condition.
     * @param withOrder
     *            <tt>true</tt> if order should be used
     * @param withLimit
     *            <tt>true</tt> if limit should be used
     * @param self
     *            this slot as a plugged slot.
     * @return the SQL query
     */
    public String getInternalKeysQuery(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, boolean withOrder, boolean withLimit, PSlot<StclContext, PStcl> self) {
        String select = getKeysSelect(stclContext, self);
        String order = null;
        String limit = null;

        // set order if needed
        if (withOrder) {
            String o = getInternalKeysOrder(stclContext, cond, self);
            if (StringUtils.isNotBlank(o)) {
                order = o;
            }
        }

        // sets limit if needed
        if (withLimit) {
            String l = getInternalKeysLimit(stclContext, cond, self);
            if (StringUtils.isNotBlank(l)) {
                limit = l;
            }
        }

        // add group
        String group = getKeysGroup(stclContext, self);

        return getInternalKeysQuery(stclContext, cond, select, order, limit, group, self);
    }

    /**
     * Returns the SQL query constructed for this slot.
     * 
     * @param stclContext
     *            the stencil context.
     * @param cond
     *            the stencil condition.
     * @param select
     *            the select part used.
     * @param order
     *            the order part used.
     * @param limit
     *            the limit part used.
     * @param self
     *            this slot as a plugged slot.
     * @return the SQL query
     */
    public String getInternalKeysQuery(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, String select, String order, String limit, String group, PSlot<StclContext, PStcl> self) {

        // gets query components
        String from = getKeysFrom(stclContext, self);
        String where = getKeysCondition(stclContext, cond, self);

        // creates query
        String query = String.format("SELECT %s FROM %s ", select, from);

        // adds having (expression condition)
        String having = getKeysHaving(stclContext, cond, self);

        // adds where, group, having, order and limit
        if (StringUtils.isNotBlank(where)) {
            query = String.format("%s WHERE %s", query, where);
        }
        if (StringUtils.isNotBlank(group)) {
            query = String.format("%s %s", query, group);
        }
        if (StringUtils.isNotBlank(having)) {
            query = String.format("%s HAVING %s", query, having);
        }
        if (StringUtils.isNotBlank(order)) {
            query = String.format("%s %s", query, order);
        }
        if (StringUtils.isNotBlank(limit)) {
            query = String.format("%s %s", query, limit);
        }

        // returns created query
        return query;
    }

    /**
     * Return the properties values stored for optimization (this map is updated
     * each time the keys are calculated). <b>Beware</b>Think to update the map
     * when you save values.
     * 
     * @param stclContext
     *            the stencil context.
     * @param rs
     *            the result set of values fetch from keys query.
     * @param self
     *            this slot as a plugged slot.
     * @return a map with property path as key and value associated
     *         <tt>null if not optimization/tt>.
     */
    public Map<String, String> getPropertiesValuesFromKeyResults(StclContext stclContext, ResultSet rs, PSlot<StclContext, PStcl> self) throws SQLException {
        Map<String, String> map = new HashMap<String, String>();
        map.put(SQLStcl.Slot.ID, rs.getString(SQLStcl.Slot.ID));
        return map;
        /*
         * Map<String, String> map =
         * super.getPropertiesValuesFromKeyResults(stclContext, rs, self);
         * map.put(Stcl.Slot.ID, rs.getString(Stcl.Slot.ID)); return map;
         */
    }

    /**
     * Returns a specific SQL selection clause which must be used to the query
     * to retrieve the stencil. This specific clause is used only if no specific
     * keys query is defined.
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            this slot as a plugged slot.
     * @return the specific SQL selection.
     */
    public String getStencilSelect(StclContext stclContext, PSlot<StclContext, PStcl> self) {
        return getKeysSelect(stclContext, self);
    }

    /**
     * Returns a Id field which must be used to the query to retrieve the
     * stencil. This specific clause is used only if no specific keys query is
     * defined.
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            this slot as a plugged slot.
     * @return the specific SQL id field.
     */
    public String getStencilIdField(StclContext stclContext, PSlot<StclContext, PStcl> self) {
        return getKeysIdField(stclContext, self);
    }

    /**
     * Returns a specific SQL from clause which must be used to the query to
     * retrieve the stencil.
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            this slot as a plugged slot.
     * @return the specific SQL selection.
     */
    public String getStencilFrom(StclContext stclContext, PSlot<StclContext, PStcl> self) {
        return getKeysFrom(stclContext, self);
    }

    /**
     * Returns a specific SQL group clause which must be used to the query to
     * retrieve the stencil.
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            this slot as a plugged slot.
     * @return the specific SQL selection.
     */
    public String getStencilGroup(StclContext stclContext, PSlot<StclContext, PStcl> self) {
        return getKeysGroup(stclContext, self);
    }

    /**
     * Abstract method to retrieve the query string which will return the result
     * set used to complete the stencil.
     * 
     * @param stclContext
     *            the stencil context.
     * @param key
     *            the plugged key.
     * @param self
     *            this slot as a plugged slot.
     * @return the query to fetch the stencil values.
     */
    public String getStencilQuery(StclContext stclContext, IKey key, boolean withWhere, PSlot<StclContext, PStcl> self) {
        String select = getStencilSelect(stclContext, self);
        String from = getStencilFrom(stclContext, self);
        String group = getStencilGroup(stclContext, self);
        String id = getStencilIdField(stclContext, self);

        // creates query
        String query = null;
        if (withWhere) {
            String where = getKeysCondition(stclContext, null, self);
            query = String.format("SELECT %s FROM %s WHERE %s='%s' AND %s", select, from, id, key, where);
        } else
            query = String.format("SELECT %s FROM %s WHERE %s='%s'", select, from, id, key);

        // adds group, having, order and limit
        if (StringUtils.isNotBlank(group)) {
            query = String.format("%s %s", query, group);
        }

        return query;
    }

    /**
     * Method to complete the stencil from SQL values (by default all stencils
     * must have a sql context, so it will be plugged).
     * 
     * @param stclContext
     *            the stencil context.
     * @param stencil
     *            the created stencil.
     * @param rs
     *            the result set of values.
     * @param self
     *            this slot as a plugged slot.
     * @return the completion result.
     */
    public Result completeStencil(StclContext stclContext, PStcl stencil, ResultSet rs, PSlot<StclContext, PStcl> self) {

        // completes at least id
        try {
            stencil.setString(stclContext, Slot.ID, rs.getString(Slot.ID));
        } catch (SQLException e) {
            logWarn(stclContext, e.toString());
        }

        // plugs SQL context
        PStcl sqlContext = self.getContainer().getStencil(stclContext, SQLStcl.Slot.SQL_CONTEXT);
        stencil.plug(stclContext, sqlContext, SQLStcl.Slot.SQL_CONTEXT);

        return Result.success();
    }

    /**
     * Calls the insertion query to the database (this method is called by plug
     * order. By default all stencils must have a sql context, so it will be
     * plugged in its SQL slot and the last inserted index is returned. If you
     * override it don't forget to call afterInsertStencilQuery (or doing
     * content in your code). <b>BEWARE</b>: do not do another update query (or
     * last inserted index will be wrong)
     * 
     * @param stclContext
     *            the stencil context.
     * @param stencil
     *            the stencil to be inserted.
     * @param sqlContext
     *            the sql context stencil.
     * @param self
     *            this slot as a plugged slot.
     * @return the key of the plugged stencil.
     */
    public synchronized Result insertStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
        try {

            // verifies if insert can be done
            Result result = beforeInsertStencilQuery(stclContext, stencil, sqlContext, self);
            if (result.isNotSuccess())
                return result;

            // creates the query
            SqlUtils.SqlAssoc assoc = getSqlAssoc(stclContext, stencil, self);
            String query = assoc.getInsertQuery();

            // does the query
            SQLContextStcl stcl = (SQLContextStcl) sqlContext.getReleasedStencil(stclContext);
            result = stcl.updateQuery(stclContext, query, sqlContext);
            if (result.isNotSuccess())
                return result;

            // does after insertion
            return afterInsertStencilQuery(stclContext, stencil, sqlContext, self);
        } catch (Exception e) {
            logError(stclContext, e.toString());
            return Result.error(e);
        }
    }

    protected Result beforeInsertStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
        return Result.success();
    }

    protected Result afterInsertStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
        PStcl plugged = stencil;

        // returns key created if was not defined
        String id = stencil.getString(stclContext, SQLStcl.Slot.ID, "");
        if (StringUtils.isBlank(id)) {
            SQLContextStcl stcl = (SQLContextStcl) sqlContext.getReleasedStencil(stclContext);
            int last_inserted_id = stcl.queryLastInsertID(stclContext, sqlContext);
            plugged.setInt(stclContext, SQLStcl.Slot.ID, last_inserted_id);
            plugged.call(stclContext, Command.UPDATE);
            plugged.plug(stclContext, sqlContext, SQLStcl.Slot.SQL_CONTEXT);
            plugged.addCursor(stclContext, self, _cursor, new Key(last_inserted_id));
        }

        return Result.success(PLUGGED_PREFIX, plugged);
    }

    /**
     * Calls the update query to the database.
     * 
     * @param stclContext
     *            the stencil context.
     * @param stencil
     *            the stencil to be updated.
     * @param sqlContext
     *            the sql context stencil.
     * @param self
     *            this slot as a plugged slot.
     * @return the key of the plugged stencil.
     */
    public Result updateStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
        try {

            // verifies if update can be done
            Result result = beforeUpdateStencilQuery(stclContext, stencil, sqlContext, self);
            if (result.isNotSuccess())
                return result;

            // creates the query
            SqlUtils.SqlAssoc assoc = getSqlAssoc(stclContext, stencil, self);
            SqlUtils.SqlCondition sql_where = newSqlCondition(stclContext, stencil, self);
            sql_where.pushString(stclContext, SQLStcl.Slot.ID, SQLStcl.Slot.ID);
            String query = assoc.getUpdateQuery(sql_where);

            // does the query
            SQLContextStcl stcl = (SQLContextStcl) sqlContext.getReleasedStencil(stclContext);
            result = stcl.updateQuery(stclContext, query, sqlContext);
            if (result.isNotSuccess()) {
                getCursor(stclContext, self).clear();
                return result;
            }

            // does after updatation
            return afterUpdateStencilQuery(stclContext, stencil, sqlContext, self);
        } catch (Exception e) {
            logError(stclContext, e.toString());
            return Result.error(e);
        }
    }

    public Result beforeUpdateStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
        return Result.success();
    }

    public Result afterUpdateStencilQuery(StclContext stclContext, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
        return Result.success();
    }

    /**
     * Calls the deletion query to the database (this method is called by unplug
     * order).
     * 
     * @param stclContext
     *            the stencil context.
     * @param key
     *            the plugged key (may not be <tt>null</tt>).
     * @param stencil
     *            the stencil to be deleted.
     * @param sqlContext
     *            the sql context stencil.
     * @param container
     *            the container stencil.
     */
    protected Result deleteStencilQuery(StclContext stclContext, IKey key, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
        try {

            // verifies if delete can be done
            Result result = beforeDeleteStencilQuery(stclContext, key, stencil, sqlContext, self);
            if (result.isNotSuccess())
                return result;

            String from = getKeysFrom(stclContext, self);
            String table = getTableName(stclContext, self);
            String alias = getTableAliasForProperty(stclContext, self);
            SqlUtils.SqlCondition sql_where = SqlUtils.newSqlCondition(from, table, alias, stencil);

            // creates the query
            sql_where.put(SQLStcl.Slot.ID, "'" + key.toString() + "'");
            String query = sql_where.getDeleteQuery();

            // does the query
            SQLContextStcl stcl = (SQLContextStcl) sqlContext.getReleasedStencil(stclContext);
            result = stcl.updateQuery(stclContext, query, sqlContext);
            if (result.isNotSuccess())
                return result;

            // does after updatation
            return afterDeleteStencilQuery(stclContext, key, stencil, sqlContext, self);
        } catch (Exception e) {
            logError(stclContext, e.toString());
            return Result.error(e);
        }
    }

    protected Result beforeDeleteStencilQuery(StclContext stclContext, IKey key, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
        return Result.success();
    }

    protected Result afterDeleteStencilQuery(StclContext stclContext, IKey key, PStcl stencil, PStcl sqlContext, PSlot<StclContext, PStcl> self) {
        return Result.success();
    }

    /**
     * Retrieves the key used in slot for the stencil. When the stencil is
     * unplugged without a key, this key must be retrieved from the stencil
     * content.
     * 
     * @param stclContext
     *            the stencil context.
     * @param stencil
     *            the stencil to be deleted.
     * @param sqlContext
     *            the sql context stencil.
     * @param container
     *            the container stencil.
     * @return the key from database information.
     */
    public IKey retrieveKeyFromStencil(StclContext stclContext, PStcl stencil, PStcl sqlContext, PStcl container) {
        String id = SqlUtils.getStringFromStencil(stclContext, stencil, SQLStcl.Slot.ID);
        return new Key(id);
    }

    public SqlAssoc getSqlAssoc(StclContext stclContext, PStcl stencil, PSlot<StclContext, PStcl> self) {
        SqlAssoc assoc = newSqlAssoc(stclContext, stencil, self);

        // add stored fields in sql assoc
        Stcl stcl = stencil.getReleasedStencil(stclContext);
        if (stcl instanceof SQLStcl) {
            ((SQLStcl) stcl).addInSqlAssoc(stclContext, assoc, stencil, self.getContainer());
        } else {
            if (getLog().isTraceEnabled()) {
                String msg = String.format("The stencil %s is not a SQL stencil", stencil);
                getLog().warn(stclContext, msg);
            }
        }

        return assoc;
    }

    public ResultSet getKeysResultSet(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {

        // get keys query
        String query = getKeysQuery(stclContext, cond, self);
        if (StringUtils.isBlank(query)) {
            logWarn(stclContext, "Keys query not defined for slot %s for key result set", self);
            return null;
        }

        // get sql context
        PStcl sqlContext = getSQLContext(stclContext, self);
        if (StencilUtils.isNull(sqlContext)) {
            logWarn(stclContext, "No SQL context defined for slot %s for key result set", self);
            return null;
        }

        // creates list
        SQLContextStcl sql = (SQLContextStcl) sqlContext.getReleasedStencil(stclContext);
        return sql.selectQuery(stclContext, query, sqlContext);
    }

    // TO BE REMOVED (getStencil should be used)
    public ResultSet getKeysResultSet(StclContext stclContext, IKey key, PSlot<StclContext, PStcl> self) {

        // get keys query
        PathCondition<StclContext, PStcl> cond = PathCondition.newKeyCondition(stclContext, key, self.getContainer());
        String query = getKeysQuery(stclContext, cond, self);
        if (StringUtils.isBlank(query)) {
            logWarn(stclContext, "Keys query not defined for slot %s for key result set", self);
            return null;
        }

        // get sql context
        PStcl sqlContext = getSQLContext(stclContext, self);
        if (StencilUtils.isNull(sqlContext)) {
            logWarn(stclContext, "No SQL context defined for slot %s for key result set", self);
            return null;
        }

        // creates list
        SQLContextStcl sql = (SQLContextStcl) sqlContext.getReleasedStencil(stclContext);
        return sql.selectQuery(stclContext, query, sqlContext);
    }

    @Override
    protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {

        // should be initialized before used
        if (!initialize(stclContext, self)) {
            String msg = logWarn(stclContext, "Cannot initialize slot %s", self);
            return StencilUtils.<StclContext, PStcl> iterator(Result.error(msg));
        }

        // if the list was already created for the same stencil context and
        // without any condition
        if ((_stencil_context_uid == stclContext.getId() || _read_only) && _stencil_context_map != null) {
            return StencilUtils.<StclContext, PStcl> iterator(stclContext, _stencil_context_map.get().clone(), cond, self);
        }

        // creates the stencil list
        SQLCursor cursor = getCursor(stclContext, self);
        List<IKey> keys = getKeys(stclContext, cond, self);
        List<PStcl> stencils = new Vector<PStcl>(keys.size());
        for (IKey key : keys) {
            PStcl stencil = new PStcl(stclContext, self, key, cursor);
            stencils.add(stencil);
        }

        // returns the iterator (save for optimization on complete list)
        StencilIterator<StclContext, PStcl> map = new ListIterator<StclContext, PStcl>(stencils);
        if (cond == null) {
            _stencil_context_uid = stclContext.getId();
            _stencil_context_map = new SoftReference<>(map);
        }
        return map;
    }

    // not using other condition than ID...
    protected PStcl getStencil(StclContext stclContext, int key, PSlot<StclContext, PStcl> self) {
        SQLCursor cursor = getCursor(stclContext, self);
        return new PStcl(stclContext, self, new Key(key), cursor);
    }

    /**
     * Returns the list of plugged stencils with cursor.
     * 
     * @param stclContext
     *            the stencil context.
     * @param cond
     * @param self
     *            this slot as a plugged slot.
     * @return
     */
    protected List<IKey> getKeys(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {

        // get keys query
        ResultSet rs = getKeysResultSet(stclContext, cond, self);
        if (rs != null) {
            try {
                SQLCursor cursor = getCursor(stclContext, self);
                List<IKey> keys = new ArrayList<>();
                while (rs.next()) {

                    // keys are id
                    String id = rs.getString(SQLStcl.Slot.ID);

                    // for some jointure the id may be null (such result should
                    // not be taken in account)
                    if (StringUtils.isBlank(id))
                        continue;

                    // adds slot value attributes to get string optimization
                    // only if not currently already modified
                    // (or properties are more uptodate that from request)
                    IKey key = new Key(id);
                    Boolean modified = cursor._modified.get(key);
                    if (modified == null || !modified) {
                        try {
                            Map<String, String> attributes = getPropertiesValuesFromKeyResults(stclContext, rs, self);
                            cursor.setPropertiesValues(stclContext, self, key, attributes);
                            keys.add(key);
                        } catch (Exception e) {
                            // don't add key in list if cannot set property
                            // values
                            logError(stclContext, e.toString());
                        }
                    } else {
                        keys.add(key);
                    }
                }
                return keys;
            } catch (Exception e) {
                logError(stclContext, e.toString());
            } finally {
                SQLContextStcl.closeResultSet(rs);
            }
        }
        return new ArrayList<IKey>();
    }

    @Override
    protected PStcl doPlug(StclContext stclContext, PStcl stencil, IKey key, PSlot<StclContext, PStcl> self) {

        // should be initialized before used
        if (!initialize(stclContext, self)) {
            String msg = logWarn(stclContext, "Cannot initialize slot %s", self);
            return Stcl.nullPStencil(stclContext, Result.error(msg));
        }
        
        if (_read_only) {
            logWarn(stclContext, "Plug in read only slot : %s", self);
            _read_only = false;
        }

        // creates new plugged stencil
        // StencilFactory<StclContext, PStcl> factory =
        // (StencilFactory<StclContext,
        // PStcl>) stclContext.getStencilFactory();
        // PStcl toBePlugged = factory.createPStencil(stclContext, self, key,
        // order.getStencil());

        // cheks the stencil plugged is a SQL stencil
        if (!(stencil.getReleasedStencil(stclContext) instanceof SQLStcl)) {
            String msg = String.format("Cannot plug a non SQLStcl %s in a SQLSlot %s", stencil, self);
            return Stcl.nullPStencil(stclContext, Result.error(msg));
        }

        // gets sql context from slot
        PStcl sqlContext = getSQLContext(stclContext, self);
        if (StencilUtils.isNull(sqlContext)) {

            // may be defined on stencil plugged
            sqlContext = stencil.getStencil(stclContext, SQLStcl.Slot.SQL_CONTEXT);
            if (StencilUtils.isNull(sqlContext)) {
                String msg = logWarn(stclContext, "No SQL context defined for doPlug in stencil %s", self);
                return Stcl.nullPStencil(stclContext, Result.error(msg));
            }
        }

        // the stencil context map is then obsolete
        _stencil_context_uid = 0;

        // does insert query
        Result result = insertStencilQuery(stclContext, stencil, sqlContext, self);
        if (result.isNotSuccess()) {
            PStcl container = self.getContainer();
            return container.nullPStencil(stclContext, result);
        }

        // returns the stencil plugged
        PStcl plugged = result.getSuccessValue(PLUGGED_PREFIX);
        if (StencilUtils.isNull(plugged)) {
            PStcl container = self.getContainer();
            String msg = logWarn(stclContext, "was not able to perform the insertion of %s at key %s in database", stencil, key);
            return container.nullPStencil(stclContext, Result.error(msg));
        }

        // PathCondition<StclContext, PStcl> cond =
        // PathCondition.newKeyCondition(plugged.getKey());
        // PStcl p = self.getStencil(stclContext, cond);

        // add containing slot
        SQLStcl sql = (SQLStcl) plugged.getReleasedStencil(stclContext);
        sql.setSQLContext(sqlContext);
        sql.setSQLContainerSlot(self);

        // return plugged;
        return plugged;
    }

    @Override
    protected void doUnplug(StclContext stclContext, PStcl stencil, IKey key, PSlot<StclContext, PStcl> self) {
        SQLCursor cursor = getCursor(stclContext, self);
        PStcl container = self.getContainer();

        // should be initialized before used
        if (!initialize(stclContext, self)) {
            logWarn(stclContext, "Cannot initialize slot %s", self);
            return;
        }

        if (_read_only) {
            logWarn(stclContext, "Unplug in read only slot : %s", self);
            _read_only = false;
        }

        // gets SQL context
        PStcl sqlContext = getSQLContext(stclContext, self);
        if (StencilUtils.isNull(sqlContext)) {
            logWarn(stclContext, "No SQL context defined for doUnplug in stencil %s", self);
            return;
        }

        // get key
        if (key.isEmpty()) {
            key = retrieveKeyFromStencil(stclContext, stencil, sqlContext, container);
        }
        if (key.isEmpty()) {
            return;
        }

        // removes the stencil from this slot
        stencil.removeThisReferenceFromStencil(stclContext);

        // remove stencil from cursor (if not in cursor (negative id for ex)
        // then get order stencil)
        int id = key.toInt();
        if (id >= 0)
            cursor.remove(stclContext, self, self, key);

        // does deletion query (if key can be found)
        Result result = deleteStencilQuery(stclContext, key, stencil, sqlContext, self);
        if (result.isNotSuccess()) {
            logWarn(stclContext, result.getMessage());
            return;
        }

        // the stencil context map is then obsolete
        _stencil_context_uid = 0;
    }

    @Override
    protected void doUnplugAll(StclContext stclContext, PSlot<StclContext, PStcl> self) {

        // should be initialized before used
        if (!initialize(stclContext, self)) {
            logWarn(stclContext, "Cannot initialize slot %s", self);
            return;
        }

        if (_read_only) {
            logWarn(stclContext, "Unplug in read only slot : %s", self);
            _read_only = false;
        }

        // get SQL context
        PStcl sqlContext = getSQLContext(stclContext, self);
        if (StencilUtils.isNull(sqlContext)) {
            logWarn(stclContext, "No SQL context defined for doUnplugAll in stencil %s", self);
            return;
        }

        // create the query
        String table = getTableAliasForProperty(stclContext, self);
        if (StringUtils.isBlank(table)) {
            table = getTableName(stclContext, self);
        }
        String from = getKeysFrom(stclContext, self);
        String condition = getKeysCondition(stclContext, null, self);
        String query = String.format("DELETE %s FROM %s WHERE %s", table, from, condition);

        // does the query
        SQLContextStcl stcl = (SQLContextStcl) sqlContext.getReleasedStencil(stclContext);
        stcl.updateQuery(stclContext, query, sqlContext);

        // the stencil context map is then obsolete
        _stencil_context_uid = 0;
    }

    @Override
    public Result doMultiUnplug(StclContext stclContext, String keys, PSlot<StclContext, PStcl> self) {
        return doMultiUnplug(stclContext, StringHelper.splitShortStringAndTrim(keys, PathUtils.MULTI), self);
    }

    /**
     * Removes several stencils in one query.
     * 
     * @param stclContext
     *            the stencil context.
     * @param keys
     *            the array of keys to be removed.
     * @param self
     *            this slot as a plugged slot.
     * @return
     */
    private Result doMultiUnplug(StclContext stclContext, String[] keys, PSlot<StclContext, PStcl> self) {

        // should be initialized before used
        if (!initialize(stclContext, self)) {
            String msg = logWarn(stclContext, "Cannot initialize slot %s", self);
            return Result.error(msg);
        }

        if (_read_only) {
            logWarn(stclContext, "Multi unplug in read only slot : %s", self);
            _read_only = false;
        }

        // get SQL context
        PStcl sqlContext = getSQLContext(stclContext, self);
        if (StencilUtils.isNull(sqlContext)) {
            String msg = logWarn(stclContext, "no SQL context defined for doMultiUnplug instencil %s", self);
            return Result.error(msg);
        }

        // create the query
        String table = getTableName(stclContext, self);
        String sql_where = null;
        for (String key : keys) {
            String c = String.format("%s='%s'", SQLStcl.Slot.ID, key);
            if (sql_where != null) {
                sql_where = String.format("%s OR %s", sql_where, c);
            } else {
                sql_where = c;
            }
        }
        String query = String.format("DELETE FROM %s WHERE %s", table, sql_where);

        // does the query
        SQLContextStcl stcl = (SQLContextStcl) sqlContext.getReleasedStencil(stclContext);
        Result result = stcl.updateQuery(stclContext, query, sqlContext);

        // the stencil context map is then obsolete
        _stencil_context_uid = 0;

        return result;
    }

    public SqlUtils.SqlAssoc newSqlAssoc(StclContext stclContext, PStcl stencil, PSlot<StclContext, PStcl> self) {
        String from = getKeysFrom(stclContext, self);
        String table = getTableName(stclContext, self);
        String alias = getTableAliasForProperty(stclContext, self);
        return SqlUtils.newSqlAssoc(from, table, alias, stencil);
    }

    public SqlUtils.SqlCondition newSqlCondition(StclContext stclContext, PStcl stencil, PSlot<StclContext, PStcl> self) {
        String from = getKeysFrom(stclContext, self);
        String table = getTableName(stclContext, self);
        String alias = getTableAliasForProperty(stclContext, self);
        return SqlUtils.newSqlCondition(from, table, alias, stencil);
    }
}
