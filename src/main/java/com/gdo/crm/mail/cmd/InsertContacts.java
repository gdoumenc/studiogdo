/**
 * Copyright GDO - 2004
 */
/*
 * Copyright GDO - 2004
 */
package com.gdo.crm.mail.cmd;

import com.gdo.crm.CrmResources.Resource;
import com.gdo.crm.commercial.model.CommercialStcl;
import com.gdo.mail.model.DistributionListStcl;
import com.gdo.mail.model.SQLDistributionListStcl;
import com.gdo.project.model.ComposedActionStcl;
import com.gdo.sql.model.ExcelQuery;
import com.gdo.sql.model.SQLContextStcl;
import com.gdo.sql.model.SQLStcl;
import com.gdo.sql.slot.SQLSlot;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.faces.RenderContext;
import com.gdo.stencils.facet.FacetResult;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.MultiCalculatedSlot;

public class InsertContacts extends ComposedActionStcl {

	public interface Slot extends ComposedActionStcl.Slot {
		String CONTACT = "Contact";
	}

	public InsertContacts(StclContext stclContext) {
		super(stclContext);

		new ContactSlot(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> performSteps(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		PStcl target = cmdContext.getTarget();
		int current = getActiveStepIndex();

		// add all filtered contacts in table
		if (current == 3) {

			// set distribution list in reading process
			target.setBoolean(stclContext, DistributionListStcl.Slot.IN_INSERTING_PROCESS, true);

			// insert all addresses from commercial
			String comPath = getResourcePath(stclContext, Resource.COMMERCIAL_CONNECTED, self);
			PStcl commercial = target.getStencil(stclContext, comPath);
			String id = commercial.getString(stclContext, SQLStcl.Slot.ID, "");
			String contacts = commercial.getString(stclContext, CommercialStcl.Slot.CONTACT_QUERY, null);

			String from = target.getString(stclContext, SQLDistributionListStcl.Slot.FROM_TABLE, null);
			String query = String.format("INSERT INTO %s SELECT NULL, '%s', _i.Email, 'tbs', NOW(), NULL, NULL FROM (%s) _i WHERE _i.Email != '' AND _i.NoEmail = '0'", from, id, contacts);
			PStcl sqlContext = target.getStencil(stclContext, SQLDistributionListStcl.Slot.SQL_CONTEXT);
			SQLContextStcl stcl = (SQLContextStcl) sqlContext.getReleasedStencil(stclContext);
			Result result = stcl.updateQuery(stclContext, query, sqlContext);
			if (result.isNotSuccess())
				return error(cmdContext, self, result);

			// insert error contacts
			query = String.format("INSERT INTO %s SELECT NULL, '%s', _i.Email, 'err', NOW(), 'Pas d\\\'addrese', NULL FROM (%s) _i WHERE _i.Email is null  OR _i.Email = ''", from, id, contacts);
			result = stcl.updateQuery(stclContext, query, sqlContext);
			if (result.isNotSuccess())
				return error(cmdContext, self, result);

			// unset distribution list in reading process
			target.setBoolean(stclContext, DistributionListStcl.Slot.IN_INSERTING_PROCESS, false);
		}
		return success(cmdContext, self);
	}

	@Override
	public FacetResult getFacet(RenderContext<StclContext, PStcl> renderContext) {
		StclContext stclContext = renderContext.getStencilContext();
		PStcl self = renderContext.getStencilRendered();
		String mode = renderContext.getFacetMode();

		// query to get all contacts
		if ("contacts.excel".equals(mode)) {
			PSlot<StclContext, PStcl> pslot = getFilteredContactSlot(stclContext, self);
			SQLSlot slot = pslot.getSlot();
			PStcl sqlContext = slot.getSQLContext(stclContext, pslot);
			String query = slot.getKeysQuery(stclContext, null, pslot);
			ExcelQuery query1 = new ExcelQuery("Contacts", query, true, null, null);

			ExcelQuery[] queries = new ExcelQuery[] { query1 };
			return ((SQLContextStcl) sqlContext.getReleasedStencil(stclContext)).excelFileFacet(stclContext, queries, sqlContext);
		}

		return super.getFacet(renderContext);
	}

	protected StencilIterator<StclContext, PStcl> getFilteredContact(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PStcl self) {
		PStcl commercial = self.getResourceStencil(stclContext, Resource.COMMERCIAL_CONNECTED);
		return commercial.getStencils(stclContext, CommercialStcl.Slot.CONTACTS, cond);
	}

	protected PSlot<StclContext, PStcl> getFilteredContactSlot(StclContext stclContext, PStcl self) {
		PStcl commercial = self.getResourceStencil(stclContext, Resource.COMMERCIAL_CONNECTED);
		return commercial.getSlot(stclContext, CommercialStcl.Slot.CONTACTS);
	}

	private class ContactSlot extends MultiCalculatedSlot<StclContext, PStcl> {

		public ContactSlot(StclContext stclContext) {
			super(stclContext, InsertContacts.this, Slot.CONTACT, PSlot.ANY);
		}

		@Override
		protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
			return getFilteredContact(stclContext, cond, self.getContainer());
		}

	}
}