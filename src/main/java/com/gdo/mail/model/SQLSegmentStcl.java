/**
 * Copyright GDO - 2004
 */
package com.gdo.mail.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.gdo.project.util.Query;
import com.gdo.project.util.SqlUtils;
import com.gdo.sql.model.SQLContextStcl;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.descriptor.Links;
import com.gdo.stencils.faces.RenderContext;
import com.gdo.stencils.facet.FacetResult;
import com.gdo.stencils.facet.FacetType;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.MultiCalculatedSlot;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;

public class SQLSegmentStcl extends SegmentStcl {

    public interface Slot extends SegmentStcl.Slot {
        /*SQL context to retrieve addresses and counters..*/
        String SQL_CONTEXT = "SqlContext"; // this id is "fixed" when the table
        // is created
        /*Unique id for database tracking table.*/
        String SQL_ID = "SqlId"; // this id is "fixed" when the table is created

        String BCC = "BCC";
        /*Specific information used in mail content.*/
        String DATA = "Data";
        /*Keeps the resource server url.*/
        String TRACKED = "Tracked";
        /*Contains the resources that are loaded by the tracker server*/
        String LOADED = "Loaded";
    }

    public interface Command extends SegmentStcl.Command {
        String UPDATE_STATUS = "UpdateStatus";
    }

    public static final String RE_SEND = "ReSend";
    public static final String RE_SENT = "ReSent";

    private static final String CREATION_ADDRESSES_TABLE_QUERY = "CREATE TABLE `%s`.`@%s` (`key` INT(4) NOT NULL AUTO_INCREMENT,"
            + "`id` VARCHAR(20) NOT NULL,`address` VARCHAR(50) NOT NULL,`status` ENUM('tbs','sent','err','tbr','resent', 'test') NOT NULL,"
            + "`msg` VARCHAR(100) NOT NULL,`date` datetime NOT NULL,PRIMARY KEY (`key`)) TYPE = MYISAM;";
    private static final String CREATION_COUNTER_TABLE_QUERY = "CREATE TABLE `%s`.`%s` (`key` int(4) NOT NULL AUTO_INCREMENT,"
            + "`id` VARCHAR(20) NOT NULL DEFAULT '',`res` VARCHAR(50) NOT NULL DEFAULT '',`date` datetime NOT NULL,PRIMARY KEY (`key`)) TYPE = MYISAM;";

    private static final String DROP_TABLE_ADDRESSES = "DROP TABLE `%s`.`@%s`;";
    private static final String DROP_TABLES_COUNTER = "DROP TABLE `%s`.`%s`;";

    private static final String INSERT_ADDRESS_QUERY = "INSERT INTO `%s`.`@%s` (`key`,`id`,`address`,`status`,`msg`,`date`) VALUES (NULL ,'%s','%s','%s','%s','%s');";
    private static final String UPDATE_ADDRESS_QUERY = "UPDATE `%s`.`@%s` SET `status`='%s',`msg`='%s',`date`='%s' WHERE `@%2$s`.`id` = '%s';";

    private static final String STATUS_QUERY = "SELECT %4$s FROM `%1$s`.`@%2$s` WHERE `status`='%3$s'";
    // private static final String LIST_QUERY =
    // "SELECT %4$s FROM `%1$s`.`@%2$s` AS A, `%1$s`.`%2$s` AS B WHERE A.id = B.id AND B.res LIKE '%%%3$s' GROUP BY address";
    // private static final String COUNTER_QUERY =
    // "SELECT %4$s FROM `%1$s`.`@%2$s` AS A, `%1$s`.`%2$s` AS B WHERE A.id = B.id AND B.res LIKE '%%%3$s'";

    private String _appName; // web application name
    private String _sqlId; // segment sql id

    public SQLSegmentStcl(StclContext stclContext) {
        super(stclContext);
        this._appName = stclContext.getConfigParameter(StclContext.PROJECT_NAME);

        propSlot(Slot.SQL_ID);

        singleSlot(Slot.SQL_CONTEXT);
        multiSlot(Slot.DATA);
        multiSlot(Slot.TRACKED);
        multiSlot(Slot.LOADED);

        Links links = new Links();
        links.put("SqlSegment", "..");
        links.put("SqlContext", "../SqlContext");
        links.put("MailContext", "../MailContext");
        links.put("Generator", "../Generator");
        links.put("To", "../To/To");
        links.put("BCC", "../CBB/To");
        singleSlot(Slot.MAIL, links);

        links = new Links();
        links.put(SQLDistributionListStcl.Slot.SQL_CONTEXT, "../SqlContext");
        singleSlot(Slot.TO, links);

        links = new Links();
        links.put(SQLDistributionListStcl.Slot.SQL_CONTEXT, "../SqlContext");
        singleSlot(Slot.SENT, links);

        links = new Links();
        links.put(SQLDistributionListStcl.Slot.SQL_CONTEXT, "../SqlContext");
        singleSlot(Slot.ERROR, links);

        links = new Links();
        links.put(SQLDistributionListStcl.Slot.SQL_CONTEXT, "../SqlContext");
        singleSlot(Slot.ALREADY, links);

        links = new Links();
        links.put(SQLDistributionListStcl.Slot.SQL_CONTEXT, "../SqlContext");
        singleSlot(Slot.BCC, links);

        // getLog().warn(stclContext, "APP NAME = " + this._appName);

        /*
         * new MySQLSlot(stclContext, SEND, SQLRecipientStcl.TBS_STATUS) {
         * 
         * @Override protected Result beforePlug(StclContext stclContext,
         * PlugOrder<StclContext, PStcl> order, PSlot<StclContext, PStcl> container)
         * { Result result = super.beforePlug(stclContext, order, container); if
         * (result.isNotSuccess()) return result;
         * 
         * // test if the adress is well formed SQLRecipientStcl r =
         * (SQLRecipientStcl) order.getStencil().getStencil(); PStcl rec = r.self();
         * if (r.verify(stclContext, rec)) { return
         * Result.error(getClass().getName(), 0, null, result); } return result; }
         * 
         * @Override protected PStcl doPlug(StclContext stclContext,
         * PlugOrder<StclContext, PStcl> order, PSlot<StclContext, PStcl> self) {
         * SQLRecipientStcl r = (SQLRecipientStcl) order.getStencil().getStencil();
         * PStcl rec = r.self();
         * 
         * // add in table try { String date = SqlUtils.getDate();
         * 
         * // recipient informations String id = rec.getString(stclContext,
         * SQLRecipientStcl.ID, null); if (StringUtils.isEmpty(id)) return null;
         * String add = rec.getString(stclContext, SQLRecipientStcl.ADDRESS, null);
         * if (StringUtils.isEmpty(add)) return null; String status =
         * rec.getString(stclContext, SQLRecipientStcl.STATUS, null); if
         * (StringUtils.isEmpty(status)) return null;
         * 
         * // verifies that status cannot be tbs if already sent //String query =
         * ""; if (status.equals(SQLRecipientStcl.TBS_STATUS)) { /*query =
         * String.format(
         * "SELECT COUNT(*) FROM `%s`.`@%s` WHERE `address`='%s' AND `status`='%s'"
         * , SQLSegmentStcl.this._appName, SQLSegmentStcl.this._sqlId, add,
         * RecipientStcl.SENT_STATUS); Object[] params = {
         * SQLSegmentStcl.this._appName, SQLSegmentStcl.this._sqlId, add,
         * SQLRecipientStcl.SENT_STATUS }; Query query_Status = new Query(
         * "SELECT COUNT(*) FROM `%s`.`@%s` WHERE `address`='%s' AND `status`='%s'"
         * , params);
         * 
         * if (SqlUtils.queryInt(stclContext, query_Status, 1) > 0) { status =
         * SQLRecipientStcl.TBR_STATUS; } }
         * 
         * // verifies if not already present /*query = String.format(
         * "SELECT COUNT(*) FROM `%s`.`@%s` WHERE `address`='%s' AND `status`='%s'"
         * , SQLSegmentStcl.this._appName, SQLSegmentStcl.this._sqlId, add, status);
         * Object[] params = { SQLSegmentStcl.this._appName,
         * SQLSegmentStcl.this._sqlId, add, status }; Query query_verify = new Query
         * ("SELECT COUNT(*) FROM `%s`.`@%s` WHERE `address`='%s' AND `status`='%s'"
         * , params);
         * 
         * if (SqlUtils.queryInt(stclContext, query_verify, 1) > 0) { return null; }
         * 
         * // add the address in table /*query = String.format(INSERT_ADDRESS_QUERY,
         * SQLSegmentStcl.this._appName, SQLSegmentStcl.this._sqlId, id, add,
         * status, StringHelper.EMPTY_STRING, date); Object[] params_add = {
         * SQLSegmentStcl.this._appName, SQLSegmentStcl.this._sqlId, id, add,
         * status, StringHelper.EMPTY_STRING, date }; Query query_count1 = new
         * Query(INSERT_ADDRESS_QUERY, params_add);
         * 
         * SqlUtils.update(stclContext, query_count1); if
         * (getLog().isTraceEnabled()) { String msg =
         * String.format("add recipient in %s segment tables for %s",
         * getName(stclContext), SQLSegmentStcl.this._appName);
         * getLog().trace(stclContext, msg); } return rec; } catch (Exception e) {
         * if (getLog().isErrorEnabled()) { String msg =
         * String.format("cannot addrecipient in %s segment tables for %s",
         * getName(stclContext), SQLSegmentStcl.this._appName);
         * getLog().error(stclContext, msg, e); } } return null; } };
         */

        /*
         * new MySQLSlot(stclContext, SENT, SQLRecipientStcl.SENT_STATUS) {};
         * 
         * new MySQLSlot(stclContext, RE_SEND, SQLRecipientStcl.TBR_STATUS) {};
         * 
         * new MySQLSlot(stclContext, RE_SENT, SQLRecipientStcl.RESENT_STATUS) {};
         * 
         * new MySQLSlot(stclContext, ERROR, SQLRecipientStcl.ERR_STATUS) {};
         */
    }

    @Override
    public void afterCompleted(StclContext stclContext, PStcl self) {
        super.afterCompleted(stclContext, self);

        // create ID and stores it as definitive sql id
        this._sqlId = self.getString(stclContext, Slot.SQL_ID, null);
        if (StringUtils.isEmpty(this._sqlId)) {
            this._sqlId = getUId(stclContext).substring(1);
            self.setString(stclContext, Slot.SQL_ID, this._sqlId);
        }

        self.newPStencil(stclContext, Slot.TO, Key.NO_KEY, SQLDistributionListStcl.class);
        self.newPStencil(stclContext, Slot.SENT, Key.NO_KEY, SQLDistributionListStcl.class);
        self.newPStencil(stclContext, Slot.ERROR, Key.NO_KEY, SQLDistributionListStcl.class);
        self.newPStencil(stclContext, Slot.ALREADY, Key.NO_KEY, SQLDistributionListStcl.class);
        self.newPStencil(stclContext, Slot.BCC, Key.NO_KEY, SQLDistributionListStcl.class);
    }

    //
    // IMailSendListener implementation
    //

    @Override
    public Result beforeSend(CommandContext<StclContext, PStcl> cmdContext, PStcl mail, PStcl recipient, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();

        // no check if in test mode
        String mode = getTestMode(stclContext, self);
        if (!SQLDistributionListStcl.TEST_MODE.equals(mode)) {
            return super.beforeSend(cmdContext, mail, recipient, self);
        }
        return Result.success();
    }

    @Override
    public Result afterSend(CommandContext<StclContext, PStcl> cmdContext, PStcl mail, PStcl recipient, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();

        // no trace if in test mode
        String mode = getTestMode(stclContext, self);
        if (!SQLDistributionListStcl.TEST_MODE.equals(mode)) {
            return super.afterSend(cmdContext, mail, recipient, self);
        }
        return Result.success();
    }

    @Override
    public Result afterError(CommandContext<StclContext, PStcl> cmdContext, PStcl mail, PStcl recipient, String reason, PStcl self) {
        StclContext stclContext = cmdContext.getStencilContext();

        // add error message to recipient
        recipient.setString(stclContext, SQLRecipientStcl.Slot.MESSAGE, reason);
        Result result = recipient.call(stclContext, SQLRecipientStcl.Command.UPDATE);
        if (result.isNotSuccess())
            return result;

        // put it to error table
        return super.afterError(cmdContext, mail, recipient, reason, self);
    }

    @Override
    public void beforeClear(StclContext stclContext, PStcl self) {
        // dropTables(stclContext);
        super.beforeClear(stclContext, self);
    }

    /**
     * Creates tracking tables.
     */
    public void createTables(StclContext stclContext) {
        CreatesTablesThread thread = new CreatesTablesThread(stclContext);
        thread.start();
    }

    /**
     * Drops tracking tables.
     */
    public void dropTables(StclContext stclContext) {
        // DropTablesThread thread = new DropTablesThread(stclContext);
        // thread.start();
    }

    /**
     * Inserts user address in tracking tables.
     */
    public void insertInAddressesTable(StclContext stclContext, PStcl rec, String msg) {
        InsertInAdressesTable thread = new InsertInAdressesTable(stclContext, rec, msg);
        thread.start();
    }

    /**
     * Updates user address in tracking tables.
     */
    public void updateInAddressesTable(StclContext stclContext, PStcl rec, String msg) {
        UpdateInAddressesTable thread = new UpdateInAddressesTable(stclContext, rec, msg);
        thread.start();
    }

    @Override
    public FacetResult getFacet(RenderContext<StclContext, PStcl> renderContext) {
        StclContext stclContext = renderContext.getStencilContext();
        String facet = renderContext.getFacetType();
        String mode = renderContext.getFacetMode();

        // return the list of adress records
        if (FacetType.E4X.equals(facet)) {
            // return counters analysis
            /*
             * if (mode.startsWith("counter")) { String url =
             * getCounterUrl(stclContext, mode); String query =
             * String.format(LIST_QUERY, this._appName, this._sqlId, url,
             * "*, count(address) as counter"); return SqlUtils.e4xFacet(stclContext,
             * query); }
             */

            // use mode as sending status
            String query = String.format(STATUS_QUERY, this._appName, this._sqlId, mode, "*");
            InputStream reader = SqlUtils.e4xFacet(stclContext, query);
            return new FacetResult(reader, "text/xml");
        }

        // return the number of address
        else if (PathUtils.NUMBER.equals(facet)) {
            PStcl segment = renderContext.getStencilRendered();
            PStcl sql_context = segment.getStencil(stclContext, Slot.SQL_CONTEXT);
            /*
             * if (mode.startsWith("counter")) { String url =
             * getCounterUrl(stclContext, mode); String query =
             * String.format(COUNTER_QUERY, this._appName, this._sqlId, url,
             * "count(distinct address) as nb"); return new
             * StringReader(SqlUtils.queryString(stclContext, query, 1)); }
             */
            String query = String.format(STATUS_QUERY, this._appName, this._sqlId, mode, "COUNT(distinct id) as nb");
            String result = ((SQLContextStcl) sql_context.getReleasedStencil(stclContext)).queryString(stclContext, query, sql_context);
            InputStream reader = new ByteArrayInputStream(result.getBytes());
            return new FacetResult(reader, "text/plain");
        }

        // return file content
        else if (FacetType.CSV.equals(facet) || FacetType.XLS.equals(facet)) {
            /*
             * if (mode.startsWith("counter")) { String url =
             * getCounterUrl(stclContext, mode); String query =
             * String.format(LIST_QUERY, this._appName, this._sqlId, url,
             * "address, A.date as sent, B.date as received, count(address) as counter"
             * ); return SqlUtils.e4xFacet(stclContext, query, ',', '\n'); }
             */
            String query = String.format(STATUS_QUERY, this._appName, this._sqlId, mode, "*");
            InputStream reader = SqlUtils.e4xFacet(stclContext, query, ',', '\n');
            return new FacetResult(reader, "text/plain");
        }
        return super.getFacet(renderContext);
    }

    /*
     * private String getCounterUrl(StclContext stclContext, String mode) { String
     * ref = mode.substring("counter".length()); PStcl stcl =
     * RpcWrapper.getStclFromRef(stclContext, ref); String url =
     * stcl.getString(stclContext, ResourceStcl.Slot.URL, ""); if (url.length() >
     * 50) url = url.substring(url.length() - 50); return url; }
     */

    /**
     * Thread to create tracking tables.
     */
    private class CreatesTablesThread extends Thread {
        private StclContext _stclContext;

        CreatesTablesThread(StclContext stclContext) {
            this._stclContext = stclContext;
            // Connection con = (Connection)
            // stclContext.getParameter(SqlUtils.class, SqlUtils.SQL_DRIVER);
            // this._stclContext.putParameter(SqlUtils.class,
            // SqlUtils.SQL_DRIVER, con);
        }

        @Override
        public void run() {

            // create tables on SQL server
            try {
                String query1 = String.format(SQLSegmentStcl.CREATION_ADDRESSES_TABLE_QUERY, SQLSegmentStcl.this._appName, SQLSegmentStcl.this._sqlId);
                String query2 = String.format(SQLSegmentStcl.CREATION_COUNTER_TABLE_QUERY, SQLSegmentStcl.this._appName, SQLSegmentStcl.this._sqlId);
                SqlUtils.batchUpdate(this._stclContext, new String[] { query1, query2 });

                if (getLog().isTraceEnabled()) {
                    getLog().trace(this._stclContext, "create segment tables for" + SQLSegmentStcl.this._appName);
                }
            } catch (SQLException e) {
                if (getLog().isErrorEnabled()) {
                    getLog().error(this._stclContext, "cannot create segment tables", e);
                }
            }
        }
    }

    /**
     * Thread to drop tracking tables.
     */
    private class DropTablesThread extends Thread {
        private StclContext _stclContext;

        DropTablesThread(StclContext stclContext) {
            this._stclContext = stclContext;
            // Connection con = (Connection)
            // stclContext.getParameter(SqlUtils.class, SqlUtils.SQL_DRIVER);
            // this._stclContext.putParameter(SqlUtils.class,
            // SqlUtils.SQL_DRIVER, con);
        }

        @Override
        public void run() {
            try {
                String query1 = String.format(DROP_TABLE_ADDRESSES, SQLSegmentStcl.this._appName, SQLSegmentStcl.this._sqlId);
                String query2 = String.format(DROP_TABLES_COUNTER, SQLSegmentStcl.this._appName, SQLSegmentStcl.this._sqlId);
                SqlUtils.batchUpdate(this._stclContext, new String[] { query1, query2 });

                if (getLog().isTraceEnabled()) {
                    getLog().trace(this._stclContext, "drop segment tables for" + SQLSegmentStcl.this._appName);
                }
            } catch (SQLException e) {
                logError(this._stclContext, "cannot drop segment tables", e);
            }
        }
    }

    /**
     * Thread to insert address in tracking tables.
     */
    private class InsertInAdressesTable extends Thread {
        private StclContext _stclContext;
        String _id;
        String _add;
        String _status;
        String _msg;

        InsertInAdressesTable(StclContext stclContext, PStcl rec, String msg) {
            // try {
            // create a new context to let the old one be released
            this._stclContext = stclContext;
            // Connection con = (Connection)
            // stclContext.getParameter(SqlUtils.class, SqlUtils.SQL_DRIVER);
            // this._stclContext.putParameter(SqlUtils.class,
            // SqlUtils.SQL_DRIVER, con);

            // share the same connection for optimization
            // SqlUtils.copyConnection(stclContext, this._stclContext);

            // retrive trace infos
            if (!StencilUtils.isNull(rec)) {
                this._id = rec.getString(stclContext, SQLRecipientStcl.Slot.ID, null);
                this._add = rec.getString(this._stclContext, SQLRecipientStcl.Slot.ADDRESS, "");
                this._status = rec.getString(this._stclContext, SQLRecipientStcl.Slot.STATUS, "tbs");
                this._msg = msg;
            }
            // } catch (SQLException e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
        }

        @Override
        public void run() {
            try {
                if (StringUtils.isEmpty(this._id)) {
                    if (getLog().isErrorEnabled()) {
                        getLog().error(this._stclContext, "no recipient in table");
                    }
                    return;
                }
                String date = SqlUtils.getDate();

                String query1 = String.format(INSERT_ADDRESS_QUERY, SQLSegmentStcl.this._appName, SQLSegmentStcl.this._sqlId, this._id, this._add, this._status, this._msg, date);
                SqlUtils.batchUpdate(this._stclContext, new String[] { query1 });

                if (getLog().isTraceEnabled()) {
                    getLog().trace(this._stclContext, "add recipient in send segment tables for" + SQLSegmentStcl.this._appName);
                }
            } catch (SQLException e) {
                if (getLog().isErrorEnabled()) {
                    getLog().error(this._stclContext, "cannot insert recipient in send segment tables", e);
                }
            }
        }
    }

    /**
     * Thread to update address in tracking tables.
     */
    private class UpdateInAddressesTable extends Thread {
        private StclContext _stclContext;
        String _id;
        String _status;
        String _msg;

        UpdateInAddressesTable(StclContext stclContext, PStcl rec, String msg) {
            this._stclContext = stclContext;
            // Connection con = (Connection)
            // stclContext.getParameter(SqlUtils.class, SqlUtils.SQL_DRIVER);
            // this._stclContext.putParameter(SqlUtils.class,
            // SqlUtils.SQL_DRIVER, con);

            if (!StencilUtils.isNull(rec)) {
                this._id = rec.getString(stclContext, SQLRecipientStcl.Slot.ID, null);
                this._status = rec.getString(this._stclContext, SQLRecipientStcl.Slot.STATUS, "tbs");
                this._msg = msg;
            }
        }

        @Override
        public void run() {
            try {
                String date = SqlUtils.getDate();
                // String query = String.format(UPDATE_ADDRESS_QUERY,
                // SQLSegmentStcl.this._appName, SQLSegmentStcl.this._sqlId,
                // this._status, this._msg, date, this._id);
                Object[] params = { SQLSegmentStcl.this._appName, SQLSegmentStcl.this._sqlId, this._status, this._msg, date, this._id };
                Query query = new Query(UPDATE_ADDRESS_QUERY, params);
                SqlUtils.update(this._stclContext, query);

                if (getLog().isTraceEnabled()) {
                    getLog().trace(this._stclContext, "update recipient in send segment tables for" + SQLSegmentStcl.this._appName);
                }
            } catch (Exception e) {
                if (getLog().isErrorEnabled()) {
                    getLog().error(this._stclContext, "cannot update recipient in send segment tables", e);
                }
            }
        }
    }

}