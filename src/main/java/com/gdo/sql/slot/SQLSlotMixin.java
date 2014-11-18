package com.gdo.sql.slot;

import org.apache.commons.lang3.StringUtils;

import com.gdo.stencils.StclContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;

public interface SQLSlotMixin extends SQLSlotFilter {

    /**
     * Returns the database name.
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            this slot as a plugged slot.
     * @return the database name.
     */
    default public String databaseName(StclContext stclContext, PSlot<StclContext, PStcl> self) {
        return null;
    }

    /**
     * Returns the database table name. If the table used for query is a
     * jointure you have to change <tt>getKeysFrom</tt>
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            this slot as a plugged slot.
     * @return the database table name.
     */
    default public String tableName(StclContext stclContext, PSlot<StclContext, PStcl> self) {
        return "table not defined";
    }

    /**
     * Returns a specific SQL selection clause which must be used to the query
     * to retrieve the stencil keys. This specific clause is used only if no
     * specific keys query is defined.
     * 
     * @param stclContext
     *            the stencil context.
     * @param self
     *            the plugged slot.
     * @return the specific SQL selection.
     */
    default public String keysSelect(StclContext stclContext, PSlot<StclContext, PStcl> self) {
        return "*";
    }

    /**
     * Returns a specific SQL from clause which must be used to the query to
     * retrieve the stencil keys. This specific clause is used only if no
     * specific keys query is defined.
     * 
     * @param stclContext
     *            the stencil context.
     * @param from
     * @param alias
     * @param self
     *            the plugged slot.
     * @return the specific SQL selection.
     */
    default public String keysFrom(StclContext stclContext, String from, String alias, PSlot<StclContext, PStcl> self) {
        if (StringUtils.isNotBlank(alias)) {
            if (alias.indexOf("`") < 0)
                alias = "`" + alias + "`";
            return String.format("%s %s", from, alias);
        }
        return from;
    }

    /**
     * Returns a specific SQL condition clause which must be added to the query
     * to retrieve the stencilkeys. This specific clause is used only if no
     * specific keys query is defined.
     * 
     * @param stclContext
     *            the stencil context.
     * @param cond
     *            the path condition.
     * @param self
     *            the plugged slot.
     * @return the specific SQL clause.
     */
    default public String keysCondition(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, String id_field, PSlot<StclContext, PStcl> self) {
        String c = String.format("%s >= 0", id_field);
        return addFilter(stclContext, c, self);
    }

    default public String order(StclContext stclContext, String id_field, PSlot<StclContext, PStcl> self) {
        return String.format("ORDER BY %s", id_field);
    }

    default public String group(StclContext stclContext, PSlot<StclContext, PStcl> self) {
        return null;
    }

    default public String limit(StclContext stclContext, PSlot<StclContext, PStcl> self) {
        return null;
    }

}
