/**
 * Copyright GDO - 2004
 */
package com.gdo.inscription.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;

import com.gdo.mail.model.RecipientStcl;
import com.gdo.sql.model.SQLStcl;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PStcl;

public class InscriptionStcl extends SQLStcl {

	public interface Slot extends SQLStcl.Slot {
		String EMAIL = "Email";
		String RECIPIENT = "Recipient";
	}

	public InscriptionStcl(StclContext stclContext) {
		super(stclContext);

		propSlot(Slot.EMAIL, "");
		singleSlot(Slot.RECIPIENT);
	}

	@Override
	public String getName(StclContext stclContext, PStcl self) {
		String name = getJavaName(stclContext, self);
		return (StringUtils.isEmpty(name)) ? "" : name;
	}

	/**
	 * Creates the contained recipient (may be redefined in subclasses to hve
	 * specific recipient template)
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param rs
	 *          the result set.
	 * @param self
	 *          this stencil as a plugged stencil.
	 * @return the created recipient.
	 */
	protected PStcl createRecipient(StclContext stclContext, ResultSet rs, PStcl self) throws SQLException {
		String email = rs.getString(Slot.EMAIL);
		PStcl rec = self.newPStencil(stclContext, Slot.RECIPIENT, Key.NO_KEY, RecipientStcl.class.getName(), email);
		return rec;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdo.sql.model.SQLStcl#completeSQLStencil(com.gdo.project.StclContext,
	 * java.sql.ResultSet, com.gdo.project.PStcl)
	 */
	@Override
	public Result completeSQLStencil(StclContext stclContext, ResultSet rs, PStcl self) throws SQLException {
		Result result = super.completeSQLStencil(stclContext, rs, self);

		self.setString(stclContext, Slot.EMAIL, rs.getString(Slot.EMAIL));
		createRecipient(stclContext, rs, self);

		return result;
	}
}