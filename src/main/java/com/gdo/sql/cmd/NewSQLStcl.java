package com.gdo.sql.cmd;

import org.apache.commons.lang3.StringUtils;

import com.gdo.project.cmd.CreateInOneStep;
import com.gdo.sql.model.SQLContextStcl;
import com.gdo.sql.model.SQLStcl;
import com.gdo.sql.slot.SQLSlot;
import com.gdo.stencils.Keywords;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.cond.PathCondition;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.SlotUtils;
import com.gdo.stencils.util.StencilUtils;

public class NewSQLStcl extends CreateInOneStep {

    private String _oldId;
    private String _newId;

    public NewSQLStcl(StclContext stclContext) {
        super(stclContext);
    }

    /**
     * Determines the step index when the SQL stencil should be inserted in the
     * database (with an id > 0.
     * 
     * @return the insertion step index.
     */
    @Override
    protected int getPlugStep() {
        return getCreationStep() + 1;
    }

    public String getOldId() {
        return _oldId;
    }

    public String getNewId() {
        return _newId;
    }

    @Override
    protected CommandStatus<StclContext, PStcl> createStencil(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();

        // create it on local slot
        Result result = super.createStencil(cmdContext, self);
        if (result.isNotSuccess()) {
            return error(cmdContext, self, result);
        }

        // set negative id
        int id = -uniqueInt();
        _created.setInt(stclContext, SQLStcl.Slot.ID, id);
        _oldId = Integer.toString(id);

        // in case the plugged slot is not already set on created stencil
        PSlot<StclContext, PStcl> slot = ((SQLStcl) _created.getStencil(stclContext)).getSQLContainerSlot();
        if (SlotUtils.isNull(slot)) {
            SQLStcl sql = _created.getReleasedStencil(stclContext);
            sql.setSQLContainerSlot(_slot);
        }

        // plug it in database
        beforeSQLCreate(cmdContext, _created, self);
        result = super.plugStencil(cmdContext, self);
        afterSQLCreate(cmdContext, _created, self);
        if (result.isNotSuccess()) {
            return error(cmdContext, self, result);
        }
        return success(cmdContext, self);
    }

    @Override
    protected CommandStatus<StclContext, PStcl> plugStencil(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        try {
            StclContext stclContext = cmdContext.getStencilContext();

            // retrieve the created stencil (may be no more in memory)
            PathCondition<StclContext, PStcl> cond = PathCondition.<StclContext, PStcl> newKeyCondition(stclContext, new Key(_oldId), self);
            _created = _slot.getStencil(stclContext, cond);

            // sets SQL stencil final
            CommandStatus<StclContext, PStcl> status = beforeSQLPlug(cmdContext, _created, self);
            if (status.isNotSuccess()) {
                return status;
            }

            // sets final id to the plugged stencil
            SQLStcl sql = _created.getReleasedStencil(stclContext);
            _plugged = sql.setFinal(stclContext, _created);
            status = afterSQLPlug(cmdContext, _plugged, self);
            if (status.isNotSuccess()) {
                return status;
            }

            // modifies tables with temporary id
            _newId = _plugged.getString(stclContext, SQLStcl.Slot.ID, "");
            PStcl sqlContext = getSQLContext(stclContext, self);
            Result result = updateTablesWithTemporaryId(stclContext, sqlContext, _oldId, _newId);
            if (result.isNotSuccess()) {
                return error(cmdContext, self, result);
            }

            // all done
            return success(cmdContext, self);
        } catch (Exception e) {
            return error(cmdContext, self, e);
        }
    }

    @Override
    public CommandStatus<StclContext, PStcl> cancel(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();

        // if the stencil was created, removes it from database
        if (StencilUtils.isNotNull(_plugged)) {

            // removes tables with temporary id
            PStcl sqlContext = getSQLContext(stclContext, self);
            Result result = deleteTableWithTemporaryId(stclContext, sqlContext, _newId);
            if (result.isNotSuccess()) {
                return error(cmdContext, self, result);
            }

            // removes plugged stencil
            if (StringUtils.isNotBlank(_newId)) {
                _slot.unplug(stclContext, _plugged, new Key(_newId));
            }
        }

        // don't call super which will try to remove again the stencil holder
        // content.
        return success(cmdContext, self);
    }

    /**
     * Returns the SQL context defined for the target plug slot.
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            This stencil as a plugged stencil.
     * @return The SQL context of the plug slot.
     */
    protected PStcl getSQLContext(StclContext stclContext, PStcl self) {
        if (SlotUtils.isNull(_slot)) {
            String msg = String.format("No target plug slot defined for %s", self.getTemplateName(stclContext));
            return nullPStencil(stclContext, Result.error(msg));
        }
        if (!(_slot.getSlot() instanceof SQLSlot)) {
            String msg = String.format("The target plug slot %s for %s is not a SQLSlot", _slot.getName(stclContext), self.getTemplateName(stclContext));
            return nullPStencil(stclContext, Result.error(msg));
        }
        SQLSlot sqlSlot = (SQLSlot) _slot.getSlot();
        return sqlSlot.getSQLContext(stclContext, _slot);
    }

    @Override
    protected String getType(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        return Keywords.INT;
    }

    protected CommandStatus<StclContext, PStcl> beforeSQLCreate(CommandContext<StclContext, PStcl> cmdContext, PStcl created, PStcl self) {
        return success(cmdContext, self);
    }

    /**
     * Method called after the SQL stencil is created in database with negative
     * id.
     * 
     * <p>
     * A SQL stencil is created twice: first with negative id, second with
     * positive id. So classical <tt>afterCreate</tt> and <tt>afterPlug</tt>
     * methods cannot be used.
     * </p>
     * 
     * @param cmdContext
     *            the command context;
     * @param created
     *            the stencil created.
     * @param self
     *            this stencil as a plugged stencil.
     */
    protected CommandStatus<StclContext, PStcl> afterSQLCreate(CommandContext<StclContext, PStcl> cmdContext, PStcl created, PStcl self) {
        return success(cmdContext, self);
    }

    /**
     * Method called before the SQL stencil is definitively created in database
     * with positive id.
     * 
     * <p>
     * A SQL stencil is created twice: first with negative id, second with
     * positive id. So classical <tt>afterCreate</tt> and <tt>afterPlug</tt>
     * methods cannot be used.
     * </p>
     * 
     * @param cmdContext
     *            the command context;
     * @param plugged
     *            the stencil plugged.
     * @param self
     *            this stencil as a plugged stencil.
     */
    protected CommandStatus<StclContext, PStcl> beforeSQLPlug(CommandContext<StclContext, PStcl> cmdContext, PStcl plugged, PStcl self) {
        return success(cmdContext, self);
    }

    /**
     * Method called after the SQL stencil is definitively created in database
     * with positive id.
     * 
     * <p>
     * A SQL stencil is created twice: first with negative id, second with
     * positive id. So classical <tt>afterCreate</tt> and <tt>afterPlug</tt>
     * methods cannot be used.
     * </p>
     * 
     * @param cmdContext
     *            the command context;
     * @param plugged
     *            the stencil plugged.
     * @param self
     *            this stencil as a plugged stencil.
     */
    protected CommandStatus<StclContext, PStcl> afterSQLPlug(CommandContext<StclContext, PStcl> cmdContext, PStcl plugged, PStcl self) {
        return success(cmdContext, self);
    }

    protected Result updateTablesWithTemporaryId(StclContext stclContext, PStcl sqlContext, String oldId, String newId) {
        return Result.success();
    }

    protected Result deleteTableWithTemporaryId(StclContext stclContext, PStcl sqlContext, String newId) {
        return Result.success();
    }

    //
    // useful functions
    //

    public Result update(StclContext stclContext, PStcl sqlContext, String oldId, String newId, String table, String field) {
        SQLContextStcl sql = (SQLContextStcl) sqlContext.getReleasedStencil(stclContext);
        String query = String.format("UPDATE `%s` SET %s = '%s' WHERE %s = '%s'", table, field, newId, field, oldId);
        Result result = sql.updateQuery(stclContext, query, sqlContext);
        if (result.isNotSuccess()) {
            logWarn(stclContext, result.getMessage());
        }
        return result;
    }

    protected Result delete(StclContext stclContext, PStcl sqlContext, String oldId, String table, String field) {
        SQLContextStcl sql = (SQLContextStcl) sqlContext.getReleasedStencil(stclContext);
        String query = String.format("DELETE FROM `%s` WHERE %s = '%s'", table, field, oldId);
        Result result = sql.updateQuery(stclContext, query, sqlContext);
        if (result.isNotSuccess()) {
            logWarn(stclContext, result.getMessage());
        }
        return result;
    }

}