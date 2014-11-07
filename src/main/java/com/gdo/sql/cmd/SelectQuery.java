/**
 * Copyright GDO - 2005
 * Date             Author      Changes     Status
 * 28/Aug/2008      Perminder   Created     Running
 */
package com.gdo.sql.cmd;

import java.sql.ResultSet;

import org.apache.commons.lang3.StringUtils;

import com.gdo.project.model.AtomicActionStcl;
import com.gdo.sql.model.SQLContextStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.plug.PStcl;

public class SelectQuery extends AtomicActionStcl {

    public SelectQuery(StclContext stclContext) {
        super(stclContext);
    }

    /**
     * Method to Test the Connection for the SqlContext.
     */
    @Override
    protected CommandStatus<StclContext, PStcl> doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
        try {
            StclContext stclContext = cmdContext.getStencilContext();

            // get the query from param1
            String query = getParameter(cmdContext, 1, "");
            if (StringUtils.isBlank(query)) {
                return error(cmdContext, self, "No query in SelectQuery command for SqlContext");
            }

            // get the result type from param2
            String type = getParameter(cmdContext, 2, "");

            // do the sql query
            PStcl sqlContext = cmdContext.getTarget();
            SQLContextStcl ctxt = sqlContext.getReleasedStencil(stclContext);
            ResultSet rs = ctxt.selectQuery(stclContext, query, sqlContext);

            if (StringUtils.isEmpty(type)) {
                return success(cmdContext, self, rs);
            } else if (StringUtils.equals(type, "e4x")) {
                if (rs != null) {
                    StringBuffer e4x = new StringBuffer();
                    e4x.append("<records>");
                    while (rs.next()) {
                        e4x.append(" <record>\n");
                        int cols = rs.getMetaData().getColumnCount();
                        for (int j = 1; j <= cols; j++) {
                            String tag = rs.getMetaData().getColumnLabel(j);
                            String tag1 = new String(tag.getBytes(), StclContext.getCharacterEncoding()); // db
                            // connection
                            String value = rs.getString(j);
                            String value1 = "";
                            if (value != null)
                                value1 = new String(value.getBytes(), StclContext.getCharacterEncoding()); // db
                            // value
                            e4x.append(String.format("  <%1$s>%2$s</%1$s>\n", tag1, value1));
                        }
                        e4x.append(" </record>\n");
                    }
                    e4x.append("</records>\n");
                    return success(cmdContext, self, e4x.toString());
                }
            }
            return success(cmdContext, self);

        } catch (Exception e) {
            return error(cmdContext, self, e);
        }
    }

}