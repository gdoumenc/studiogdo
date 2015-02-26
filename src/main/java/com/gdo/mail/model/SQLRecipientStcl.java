/**
 * Copyright GDO - 2004
 */
package com.gdo.mail.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;

import com.gdo.helper.StringHelper;
import com.gdo.project.util.SqlUtils;
import com.gdo.sql.model.SQLStcl;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PStcl;

public class SQLRecipientStcl extends SQLStcl implements IRecipient {

	public interface Slot extends RecipientStcl.Slot, SQLStcl.Slot {
		String STATUS = "Status";
		String MESSAGE = "Message";
	}

	public interface Status {
		String TBS = "tbs";
		String SENT = "sent";
		String ERR = "err";
		String TBR = "tbr";
		String RESENT = "resent";
		String TEST = "test";
	}

	private String _address; // initial value used in creator

	// USEFULL ??
	public SQLRecipientStcl(StclContext stclContext) {
		super(stclContext);
	}

	public SQLRecipientStcl(StclContext stclContext, String add) {
		super(stclContext);
		this._address = add;
	}

	// sets initial address value if exists
	@Override
	public void afterCompleted(StclContext stclContext, PStcl self) {
		super.afterCompleted(stclContext, self);
		if (this._address != null)
			self.setString(stclContext, Slot.ADDRESS, this._address);
	}

	// recipients are completed in SQL distribution list
	@Override
	public Result completeSQLStencil(StclContext stclContext, ResultSet rs, PStcl self) throws SQLException {
		Result result = super.completeSQLStencil(stclContext, rs, self);
		if (result.isNotSuccess()) {
			return result;
		}

		// sets address
		self.setString(stclContext, Slot.ADDRESS, rs.getString(Slot.ADDRESS));

		// sets name if defined in database
		if (SqlUtils.hasColumn(SQLRecipientStcl.Slot.NAME, rs)) {
			self.setString(stclContext, SQLRecipientStcl.Slot.NAME, rs.getString(SQLRecipientStcl.Slot.NAME));
		}

		// sets message if defined in database
		if (SqlUtils.hasColumn(SQLRecipientStcl.Slot.MESSAGE, rs)) {
			self.setString(stclContext, SQLRecipientStcl.Slot.MESSAGE, rs.getString(SQLRecipientStcl.Slot.MESSAGE));
		}

		return result;
	}

	/**
	 * @return the java internet address.
	 */
	public Result getInternetAddress(StclContext stclContext, PStcl self) {
		try {
			String add = self.getExpandedString(stclContext, Slot.ADDRESS, StringHelper.EMPTY_STRING);
			if (StringUtils.isBlank(add))
				return Result.error("Blank address");
			return Result.success(new InternetAddress(add));
		} catch (AddressException e) {
			String msg = logWarn(stclContext, "Cannot getInternetAddress for %s: %s", this, e);
			return Result.error(msg);
		}
	}
}