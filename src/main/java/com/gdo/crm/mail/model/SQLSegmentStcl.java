package com.gdo.crm.mail.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import com.gdo.crm.CrmResources.Resource;
import com.gdo.crm.commercial.model._ActionCommercialeStcl;
import com.gdo.crm.commercial.model.ServiceStcl;
import com.gdo.mail.model.DistributionListStcl;
import com.gdo.mail.model.MailStcl;
import com.gdo.mail.model.RecipientStcl;
import com.gdo.mail.model.SQLDistributionListStcl;
import com.gdo.sql.model.SQLContextStcl;
import com.gdo.sql.model.SQLStcl;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.CalculatedStringPropertySlot;
import com.gdo.stencils.util.PathUtils;

public class SQLSegmentStcl extends com.gdo.email.model.SQLSegmentStcl {

	public interface Slot extends com.gdo.email.model.SQLSegmentStcl.Slot {
		String CONTENT1 = "Content1";
		String CONTENT2 = "Content2";
		String CONTENT3 = "Content3";
		String CONTENT4 = "Content4";
		String CONTENT5 = "Content5";
		String CONTENT6 = "Content6";
		String CONTENT7 = "Content7";
		String CONTENT8 = "Content8";
		String IMAGE1 = "Image1";
		String IMAGE2 = "Image2";
	}

	public SQLSegmentStcl(StclContext stclContext) {
		super(stclContext);
		new FromTableSlot(stclContext);
	}

	@Override
	public void afterCompleted(StclContext stclContext, PStcl self) {

		// set mail from to user session
		PStcl mail = self.getStencil(stclContext, Slot.MAIL);
		mail.newPStencil(stclContext, MailStcl.Slot.FROM, Key.NO_KEY, FromRecipientStcl.class);

		super.afterCompleted(stclContext, self);
	}

	@Override
	public String getName(StclContext stclContext, PStcl self) {
		PStcl mail = self.getStencil(stclContext, Slot.MAIL);
		String title = mail.getString(stclContext, MailStcl.Slot.TITLE, null);
		if (StringUtils.isNotBlank(title)) {
			return title;
		}
		return "Mailing";
	}

	@Override
	public Result beforeSend(CommandContext<StclContext, PStcl> cmdContext, PStcl mail, PStcl recipient, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();

		// no check in test mode
		String mode = getTestMode(stclContext, self);
		if (SQLDistributionListStcl.TEST_MODE.equals(mode)) {
			return Result.success();
		}

		// checks not already sent
		String add = recipient.getString(stclContext, RecipientStcl.Slot.ADDRESS, "");
		String path = PathUtils.createPath(PathUtils.compose(Slot.SENT, DistributionListStcl.Slot.TO), "Email", add);
		PStcl found = self.getStencil(stclContext, path);
		if (found.isNotNull()) {
			self.plug(stclContext, recipient, PathUtils.compose(Slot.ALREADY, DistributionListStcl.Slot.TO));
			self.unplugOtherStencilFrom(stclContext, PathUtils.compose(Slot.TO, DistributionListStcl.Slot.TO), recipient);
			return Result.error("already sent");
		}
		return Result.success();
	}

	@Override
	public Result afterSend(CommandContext<StclContext, PStcl> cmdContext, PStcl mail, PStcl recipient, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		String with = null;

		// create action commerciale
		String address = mail.getString(stclContext, "To/Address", null);
		if (StringUtils.isNotBlank(address)) {
			try {
				PStcl sql = self.getStencil(stclContext, Slot.SQL_CONTEXT);
				SQLContextStcl sqlContext = sql.getReleasedStencil(stclContext);
				String query = String.format("SELECT Id FROM contacts WHERE Email='%s'", address);
				ResultSet rs = sqlContext.selectQuery(stclContext, query, sql);
				if (rs.next()) {
					with = rs.getString(SQLStcl.Slot.ID);
				}
			} catch (SQLException e) {
				logWarn(stclContext, e.toString());
			}
		}

		// store in database
		Result res = super.afterSend(cmdContext, mail, recipient, self);
		if (res.isNotSuccess())
			return res;

		// create action commercial if "estRealiseAvec" found
		if (StringUtils.isNotBlank(with)) {
			PStcl service = self.getResourceStencil(stclContext, Resource.SERVICE_CRM);
			String template = service.getString(stclContext, ServiceStcl.Slot.ACTION_COMMERCIALE_TEMPLATE);
			PSlot<StclContext, PStcl> slot = self.getResourceSlot(stclContext, Resource.ACTION_COMMERCIALE);
			PStcl action = self.newPStencil(stclContext, slot, Key.NO_KEY, template);

			// set values
			String title = mail.getString(stclContext, MailStcl.Slot.TITLE, "");
			action.setString(stclContext, _ActionCommercialeStcl.Slot.REMARQUES, "EMailing : " + title);
			String path = PathUtils.createPath(self.getResourcePath(stclContext, Resource.TYPE_ACTION), 1);
			PStcl type = mail.getStencil(stclContext, path);
			action.plug(stclContext, type, _ActionCommercialeStcl.Slot.TYPE);
			action.setBoolean(stclContext, _ActionCommercialeStcl.Slot.FAITE, false);

			// set date + 30 days
			Calendar cal = Calendar.getInstance(TimeZone.getDefault(), stclContext.getLocale());
			cal.setTime(new Date());
			cal.add(Calendar.DAY_OF_YEAR, 14);
			String quand = DateFormatUtils.format(cal.getTime(), "yyyy-MM-dd");
			action.setString(stclContext, _ActionCommercialeStcl.Slot.QUAND, quand);

			// add related users
			PStcl c = mail.getResourceStencil(stclContext, Resource.COMMERCIAL_CONNECTED);
			action.plug(stclContext, c, _ActionCommercialeStcl.Slot.EST_REALISEE_PAR);
			path = PathUtils.createPath(self.getResourcePath(stclContext, Resource.CONTACT), with);
			PStcl w = mail.getStencil(stclContext, path);
			action.plug(stclContext, w, _ActionCommercialeStcl.Slot.EST_REALISEE_AVEC);

			// update to database
			action.call(stclContext, _ActionCommercialeStcl.Command.UPDATE);
		}

		return res;
	}

	private class FromTableSlot extends CalculatedStringPropertySlot<StclContext, PStcl> {

		public FromTableSlot(StclContext stclContext) {
			super(stclContext, SQLSegmentStcl.this, Slot.FROM_TABLE);
		}

		@Override
		public String getValue(StclContext stclContext, PStcl self) {
			PStcl segment = self.getContainer(stclContext);
			return String.format("addresses_%s", segment.getKey());
		}

		@Override
		public String setValue(StclContext stclContext, String value, PStcl self) {
			return value;
		}

	}
}
