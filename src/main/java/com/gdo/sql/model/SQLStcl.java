/**
 * Copyright GDO - 2005
 */
package com.gdo.sql.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.gdo.project.util.SqlUtils;
import com.gdo.project.util.SqlUtils.SqlAssoc;
import com.gdo.sql.cmd.ReloadSQLStcl;
import com.gdo.sql.cmd.UpdateSQLStcl;
import com.gdo.sql.slot.SQLCursor;
import com.gdo.sql.slot.SQLSlot;
import com.gdo.stencils.Result;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cond.PathCondition;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.descriptor._SlotDescriptor;
import com.gdo.stencils.factory.StclFactory;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.SingleCalculatedSlot;
import com.gdo.stencils.slot._Slot;
import com.gdo.stencils.util.StencilUtils;

public class SQLStcl extends Stcl {

    public interface Slot extends Stcl.Slot {
        String SQL_CONTEXT = "SqlContext";
        String SQL_CONTAINER = "SqlContainer";

        String ID = "Id";
    }

    public interface Command extends Stcl.Command {
        String RELOAD = "Reload"; // reloads stencil from database
    }

    // SQL container slot.
    private PSlot<StclContext, PStcl> _sqlSlot;

    // SQL context.
    private PStcl _sqlContext;

    public SQLStcl(StclContext stclContext) {
        super(stclContext);

        // SLOT PART

        propSlot(Slot.ID);

        addDescriptor(Slot.SQL_CONTEXT, new _SlotDescriptor<StclContext, PStcl>() {
            @Override
            public _Slot<StclContext, PStcl> add(StclContext stclContext, String name, PStcl self) {
                return new SQLContextSlot(stclContext, SQLStcl.this, name);
            }
        });
        new SQLContainerSlot(stclContext, this, Slot.SQL_CONTAINER);

        // COMMAND PART

        command(Command.UPDATE, UpdateSQLStcl.class);
        command(Command.RELOAD, ReloadSQLStcl.class);
    }

    /**
     * Abstract method to complete a stencil from SQL values.
     * 
     * @param stclContext
     *            the stencil context.
     * @param rs
     *            the result set of values.
     * @param self
     *            the stencil as plugged.
     * @return the completion result.
     */
    public Result completeSQLStencil(StclContext stclContext, ResultSet rs, PStcl self) throws SQLException {

        // completes at least id
        self.setInt(stclContext, Slot.ID, rs.getInt(Slot.ID));

        // plugs sql context
        // self.plug(stclContext, _sqlContext, Slot.SQL_CONTEXT);

        // returns status
        return Result.success();
    }

    public static Map<String, String> getPropertiesValuesFromKeyResults(StclContext stclContext, ResultSet rs) throws SQLException {
        Map<String, String> map = new HashMap<String, String>();
        map.put(Slot.ID, rs.getString(Slot.ID));
        return map;
    }

    /**
     * @param stclContext
     *            the stencil context.
     * @param assoc
     *            the SQL association.
     * @param self
     *            this stencil as a plugged stencil.
     * @param container
     *            the container stencil.
     */
    public void addInSqlAssoc(StclContext stclContext, SqlAssoc assoc, PStcl self, PStcl container) {
        assoc.pushAutoIncrement(stclContext, SQLStcl.Slot.ID);
        /*
         * usual code could be what is below
         * assoc.pushString(stclContext, MetierStcl.Slot.NOM, MetierStcl.Slot.NOM);
         */
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gdo.project.Stcl#clone(com.gdo.project.StclContext,
     * com.gdo.stencils.key.IKey, com.gdo.project.PStcl)
     */
    @Override
    public PStcl clone(StclContext stclContext, PSlot<StclContext, PStcl> slot, IKey key, PStcl self) {
        PStcl clone = super.clone(stclContext, slot, key, self);
        ((SQLStcl) clone.getReleasedStencil(stclContext)).setSQLContainerSlot(slot);
        clone.setString(stclContext, Slot.ID, key.toString());
        clone.plug(stclContext, _sqlContext, Slot.SQL_CONTEXT);
        return clone;
    }

    public PStcl setFinal(StclContext stclContext, PStcl self) throws SQLException {
        String oldId = self.getString(stclContext, SQLStcl.Slot.ID, "");
        if (Integer.parseInt(oldId) < 0) {

            // plug final object
            self.setString(stclContext, SQLStcl.Slot.ID, "");
            PStcl plugged = _sqlSlot.plug(stclContext, self, Key.NO_KEY);

            // set new id
            String newId = plugged.getString(stclContext, SQLStcl.Slot.ID);
            SQLSlot sqlSlot = _sqlSlot.getSlot();
            SQLCursor cursor = sqlSlot.getCursor(stclContext, _sqlSlot);
            cursor.setPropertiesValuesNotModified(stclContext, new Key<String>(newId));

            // modify tables with temporary id
            PStcl sqlContext = plugged.getStencil(stclContext, SQLStcl.Slot.SQL_CONTEXT);
            Result result = updateTablesWithTemporaryId(stclContext, sqlContext, oldId, newId, plugged);
            if (result.isNotSuccess()) {
                return self.nullPStencil(stclContext, result);
            }

            // unplug temporary object
            _sqlSlot.unplug(stclContext, self, new Key<String>(oldId));

            // return plugged stencil
            return plugged;
        }
        return self;
    }

    /**
     * Empty method to be redefined when the stencil is referenced in another
     * table.
     * 
     * @param stclContext
     *            the stencil context.
     * @param sqlContext
     *            the SQL context.
     * @param oldId
     *            the old stencil ID.
     * @param newId
     *            the new stencil ID.
     */
    protected Result updateTablesWithTemporaryId(StclContext stclContext, PStcl sqlContext, String oldId, String newId, PStcl self) {
        return Result.success();
    }

    /**
     * Utility method to update stencil ID in SQL table. Should be redefined in
     * SQL stencil with relation.
     * 
     * @param stclContext
     *            the stencil context.
     * @param sqlContext
     *            the SQL context.
     * @param table
     *            the table containing the stencil ID.
     * @param field
     *            the SQL field containing the stencil ID.
     * @param oldId
     *            the old stencil ID.
     * @param newId
     *            the new stencil ID.
     */
    protected void updateIdInTable(StclContext stclContext, PStcl sqlContext, String table, String field, String oldId, String newId) {
        SQLContextStcl sql = (SQLContextStcl) sqlContext.getReleasedStencil(stclContext);
        String query = String.format("UPDATE `%s` SET `%s` = '%s' WHERE `%s` = '%s'", table, field, newId, field, oldId);
        Result result = sql.updateQuery(stclContext, query, sqlContext);
        if (result.isNotSuccess()) {
            logWarn(stclContext, result.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.gdo.stencils.Stencil#afterRPCSet(com.gdo.stencils.cmd.CommandContext,
     * com.gdo.stencils.plug.PStencil)
     */
    @Override
    public Result afterRPCSet(StclContext stclContext, PStcl self) {
        Result result = super.afterRPCSet(stclContext, self);
        if (result.isNotSuccess())
            return result;
        return update(stclContext, self);
    }

    /**
     * Sets a negative value in ID to store temporary this stencil.
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            this stencil as a plugged stencil.
     * @return the negative id.
     */
    // TODO TO BE REMOVED
    public int setNegativeId(StclContext stclContext, PStcl self) {
        int id = -uniqueInt();
        self.setInt(stclContext, Slot.ID, id);
        return id;
    }

    /**
     * Utility function to plug a stencil defined by an id.
     * 
     * @param stclContext
     *            the stencil context.
     * @param source
     *            the slot where the stencils are defined (from id).
     * @param id
     *            the id of the stencil to be plugged.
     * @param slot
     *            the slot path where the stencil will be plugged.
     * @param self
     *            the stencil as a plugged stencil.
     * @return the plugged stencil.
     */
    public PStcl plugFromId(StclContext stclContext, PSlot<StclContext, PStcl> source, Key<?> key, String slot, boolean checkNotEmpty, PStcl self) {

        // checks id
        if (key == null || key.toString().equals("0")) {
            if (checkNotEmpty) {
                String msg = logWarn(stclContext, "nothing to plug in slot %s for stencil %s", slot, self);
                return Stcl.nullPStencil(stclContext, Result.error(msg));
            }
            return Stcl.nullPStencil(stclContext, Result.error("nothing was plugged from database"));
        }

        // does the plug
        PathCondition<StclContext, PStcl> cond = PathCondition.newKeyCondition(stclContext, key, self);
        PStcl stcl = source.getStencil(stclContext, cond);
        PSlot<StclContext, PStcl> pslot = getSlot(stclContext, slot, self);
        return self.plug(stclContext, stcl, pslot);
    }

    public PStcl plugFromId(StclContext stclContext, PSlot<StclContext, PStcl> source, int id, String slot, PStcl self) {
        return plugFromId(stclContext, source, new Key<Integer>(id), slot, false, self);
    }

    // --------------------------------------------------------------------------
    //
    // SQL management.
    //
    // --------------------------------------------------------------------------

    public PStcl getSQLContext() {
        return _sqlContext;
    }

    public void setSQLContext(PStcl sqlContext) {
        _sqlContext = sqlContext;
    }

    public PSlot<StclContext, PStcl> getSQLContainerSlot() {
        return _sqlSlot;
    }

    public void setSQLContainerSlot(PSlot<StclContext, PStcl> slot) {
        _sqlSlot = slot;
    }

    /**
     * Removes the stencil from the cursor (should be called only if value
     * changed).
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            this stencil as a plugged stencil.
     */
    public void removeFromCursor(StclContext stclContext, PStcl self) {

        // updates first on database
        update(stclContext, self);

        // then removes from cursor
        PSlot<StclContext, PStcl> slot = getSQLContainerSlot();
        SQLCursor cursor = ((SQLSlot) slot.getSlot()).getCursor(stclContext, slot);
        String id = self.getString(stclContext, Slot.ID);
        cursor.removeFromCursor(stclContext, new Key<String>(id));
    }

    // --------------------------------------------------------------------------
    //
    // SQL usefule functions.
    //
    // --------------------------------------------------------------------------

    public Result insert(StclContext stclContext, PStcl self) {

        // creates the query
        SQLSlot slot = _sqlSlot.getSlot();
        SqlUtils.SqlAssoc assoc = slot.getSqlAssoc(stclContext, self, _sqlSlot);
        String query = assoc.getInsertQuery();

        // does the query
        SQLContextStcl stcl = (SQLContextStcl) _sqlContext.getReleasedStencil(stclContext);
        return stcl.updateQuery(stclContext, query, _sqlContext);
    }

    /**
     * Completes the stencil and set properties modification status to non
     * modified.
     * 
     * @param stclContext
     *            the stencil context.
     * @param rs
     *            the query result set.
     * @param self
     *            this stencil as a plugged stencil.
     * @return
     */
    public Result completeCreatedSQLStencil(StclContext stclContext, ResultSet rs, PStcl self) throws SQLException {

        // stencil structure completion
        complete(stclContext, self);

        // completes the stencil
        completeSQLStencil(stclContext, rs, self);

        return Result.success();
    }

    /**
     * Updates the stencil on database.
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            this stencil as a plugged stencil.
     * @return the updatation result.
     * @throws SQLException
     */
    public Result update(StclContext stclContext, PStcl self) {
        PStcl sqlContext = self.getStencil(stclContext, SQLStcl.Slot.SQL_CONTEXT);

        // the stencil may not be plugged from a sql slot
        if (_sqlSlot == null) {
            return Result.success();
        }

        // does updatation
        SQLSlot sqlSlot = _sqlSlot.getSlot();
        Result result = sqlSlot.updateStencilQuery(stclContext, self, sqlContext, _sqlSlot);
        if (result.isNotSuccess()) {
            return result;
        }

        // resets properties values status as non modified (so reload can be
        // done
        // witout warning)
        String id = self.getString(stclContext, SQLStcl.Slot.ID);
        SQLCursor cursor = sqlSlot.getCursor(stclContext, _sqlSlot);
        cursor.setPropertiesValuesNotModified(stclContext, new Key<String>(id));

        // needs to reload from database to reload databased calculated
        // properties
        return reload(stclContext, self);
    }

    /**
     * Reloads the structure from database.
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            this stencil as a plugged stencil.
     * @return the reloading result.
     */
    public Result reload(StclContext stclContext, PStcl self) {
        try {

            // the stencil may not be plugged in a sql slot
            if (_sqlSlot != null) {
                SQLSlot sqlSlot = _sqlSlot.getSlot();
                int id = self.getInt(stclContext, SQLStcl.Slot.ID);

                // does nothing if the id is empty (not already in database)
                if (id == 0) {
                    return Result.success();
                }

                ResultSet rs = sqlSlot.getKeysResultSet(stclContext, new Key<Integer>(id), _sqlSlot);
                if (rs.next()) {
                    IKey key = new Key<Integer>(id);

                    // sets properties values in cursor
                    SQLCursor cursor = sqlSlot.getCursor(stclContext, _sqlSlot);
                    Map<String, String> attributes = sqlSlot.getPropertiesValuesFromKeyResults(stclContext, rs, _sqlSlot);
                    cursor.setPropertiesValues(stclContext, _sqlSlot, key, attributes);

                    // completes stencil
                    Result result = completeCreatedSQLStencil(stclContext, rs, self);

                    // sets all values not modified after completion (should be
                    // done after
                    // completeCreatedSQLStencil)
                    cursor.setPropertiesValuesNotModified(stclContext, key);

                    return result;
                }
            }
            return Result.success();
        } catch (Exception e) {
            return Result.error(e);
        }
    }

    /**
     * Does a simple query. The <tt>ResultSet</tt> should be closed once used.
     * 
     * @param stclContext
     *            the stencil context.
     * @param query
     *            the query.
     * @param self
     *            this stencil as a plugged stencil.
     * @return the SQL result set.
     */
    public ResultSet query(StclContext stclContext, String query, PStcl self) {
        PStcl sqlContext = getSQLContext();
        return ((SQLContextStcl) sqlContext.getReleasedStencil(stclContext)).selectQuery(stclContext, query, sqlContext);
    }

    /**
     * Queries a string value.
     * 
     * @param stclContext
     *            the stencil context.
     * @param query
     *            the query.
     * @param self
     *            this stencil as a plugged stencil.
     * @return the string result.
     */
    public String queryString(StclContext stclContext, String query, PStcl self) {
        PStcl sqlContext = getSQLContext();
        return ((SQLContextStcl) sqlContext.getReleasedStencil(stclContext)).queryString(stclContext, query, sqlContext);
    }

    /**
     * Queries an integer value.
     * 
     * @param stclContext
     *            the stencil context.
     * @param query
     *            the query.
     * @param self
     *            this stencil as a plugged stencil.
     * @return the integer result.
     */
    public int queryInt(StclContext stclContext, String query, PStcl self) {
        PStcl sqlContext = getSQLContext();
        return ((SQLContextStcl) sqlContext.getReleasedStencil(stclContext)).queryInteger(stclContext, query, sqlContext);
    }

    /**
     * Does a simple query. The <tt>ResultSet</tt> should be closed once used.
     * 
     * @param stclContext
     *            the stencil context.
     * @param query
     *            the query.
     * @param self
     *            this stencil as a plugged stencil.
     * @return the SQL result set.
     */
    public Result update(StclContext stclContext, String query, PStcl self) {
        PStcl sqlContext = getSQLContext();
        return ((SQLContextStcl) sqlContext.getReleasedStencil(stclContext)).updateQuery(stclContext, query, sqlContext);
    }

    public Result createTable(StclContext stclContext, PStcl sqlContext, String newTable, String likeTable) {
        SQLContextStcl sql = (SQLContextStcl) sqlContext.getReleasedStencil(stclContext);
        String query = String.format("CREATE TABLE %s LIKE %s", newTable, likeTable);
        Result result = sql.updateQuery(stclContext, query, sqlContext);
        return result;
    }

    // method to check if table already exist in db or not
    public boolean checkTableExist(StclContext stclContext, PStcl sqlContext, String tableName) {
        try {
            SQLContextStcl sql = (SQLContextStcl) sqlContext.getReleasedStencil(stclContext);

            String query = String.format("SHOW TABLES LIKE '%s'", tableName);
            ResultSet rs = sql.selectQuery(stclContext, query, sqlContext);
            boolean tacheTblAlreadyExist = rs.next();
            return tacheTblAlreadyExist;
        } catch (Exception e) {
            logError(stclContext, e.toString());
        }
        return false;
    }

    // method to empty the table
    public Result emptyTable(StclContext stclContext, PStcl sqlContext, String tableName) {
        SQLContextStcl sql = (SQLContextStcl) sqlContext.getReleasedStencil(stclContext);
        String query = String.format("DELETE FROM %s", tableName);
        Result result = sql.updateQuery(stclContext, query, sqlContext);
        return result;
    }

    protected class SQLContextSlot extends SingleCalculatedSlot<StclContext, PStcl> {

        public SQLContextSlot(StclContext stclContext, Stcl in, String name) {
            super(stclContext, in, name, PSlot.ONE);
            setAcceptPlug(true);
        }

        @Override
        public boolean hasStencils(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
            return StencilUtils.isNotNull(SQLStcl.this._sqlContext);
        }

        @Override
        public PStcl getStencil(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
            return getCalculatedStencil(stclContext, cond, self);
        }

        @Override
        public PStcl getCalculatedStencil(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
            return SQLStcl.this._sqlContext;
        }

        @Override
        protected PStcl doPlug(StclContext stclContext, PStcl stencil, IKey key, PSlot<StclContext, PStcl> self) {
            StclFactory factory = (StclFactory) stclContext.getStencilFactory();
            SQLStcl.this._sqlContext = factory.createPStencil(stclContext, self, Key.NO_KEY, stencil);
            return SQLStcl.this._sqlContext;
        }

    }

    private class SQLContainerSlot extends SingleCalculatedSlot<StclContext, PStcl> {

        public SQLContainerSlot(StclContext stclContext, Stcl in, String name) {
            super(stclContext, in, name);
            setAcceptPlug(false);
        }

        @Override
        public boolean hasStencils(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
            return true;
        }

        @Override
        public PStcl getStencil(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
            return getCalculatedStencil(stclContext, cond, self);
        }

        @Override
        public PStcl getCalculatedStencil(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
            return _sqlSlot.getContainer();
        }

    }

}
