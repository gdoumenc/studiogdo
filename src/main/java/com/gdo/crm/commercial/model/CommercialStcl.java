/**
 * Copyright GDO - 2005
 */
package com.gdo.crm.commercial.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.gdo.crm.CrmResources.Resource;
import com.gdo.crm.commercial.filter.FilteredActionCommercialeSlot;
import com.gdo.crm.commercial.filter.FilteredCommandeSlot;
import com.gdo.crm.commercial.filter.FilteredContactSlot;
import com.gdo.crm.commercial.filter.FilteredSocieteSlot;
import com.gdo.project.util.SqlUtils.SqlAssoc;
import com.gdo.sql.model.ExcelQuery;
import com.gdo.sql.model.SQLContextStcl;
import com.gdo.sql.model.SQLStcl;
import com.gdo.sql.slot.SQLCursor;
import com.gdo.sql.slot.SQLSlot;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.faces.RenderContext;
import com.gdo.stencils.facet.FacetResult;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.CalculatedStringPropertySlot;

public class CommercialStcl extends SQLStcl {

	public interface Slot extends SQLStcl.Slot {
		String SERVICE_PATH = "ServicePath";

		String NOM = "Nom";
		String PRENOM = "Prenom";
		String FONCTION = "Fonction";
		String TELEPHONE = "Telephone";
		String FAX = "Fax";
		String MOBILE = "Mobile";
		String EMAIL = "Email";

		String MANAGER = "Manager";
		String CHEF_DE = "ChefDe";

		String EST_RESPONSABLE_DE = "EstResponsableDe";
		String DOIT_FAIRE = "DoitFaire";

		String CONTACTS = "Contacts";
		String COMMANDES = "Commandes";

		String NAME = "name";
		String PASSWD = "passwd";
		String PATH = "path";
		String MODE = "mode";

		String MAILINGS = "Mailings";

		String EXCEL_SOCIETES = "ExcelSocietes";
		String CONTACT_QUERY = "ContactsQuery";
	}

	private SocieteSlot _responsable_de_slot;
	private ContactSlot _contacts_slot;
	private _ActionCommercialeSlot _doit_faire_slot;
	private CommandeSlot _commandes_slot;

	private SocieteSlot _excel_responsable_de_slot;
	private ContactSlot _excel_contacts_slot;
	private _ActionCommercialeSlot _excel_doit_faire_slot;
	private CommandeSlot _excel_commandes_slot;

	public CommercialStcl(StclContext stclContext) {
		super(stclContext);

		new ContactsQuerySlot(stclContext);

		// main slots
		createChefDeSlot(stclContext);
		this._responsable_de_slot = createEstResponsableDeSlot(stclContext);
		this._contacts_slot = createContactSlot(stclContext);
		this._doit_faire_slot = createDoitFaireSlot(stclContext);
		this._commandes_slot = createCommandeSlot(stclContext);

		// excel extraction
		this._excel_responsable_de_slot = createExcelSocietesSlot(stclContext);
		this._excel_contacts_slot = createExcelContactsSlot(stclContext);
		this._excel_doit_faire_slot = createExcelActionsCommercialesSlot(stclContext);
		this._excel_commandes_slot = createExcelCommandesSlot(stclContext);
	}

	@Override
	public Result completeSQLStencil(StclContext stclContext, ResultSet rs, PStcl self) throws SQLException {
		Result result = super.completeSQLStencil(stclContext, rs, self);

		self.setString(stclContext, Slot.NOM, rs.getString(Slot.NOM));
		self.setString(stclContext, Slot.PRENOM, rs.getString(Slot.PRENOM));
		self.setString(stclContext, Slot.FONCTION, rs.getString(Slot.FONCTION));
		self.setString(stclContext, Slot.TELEPHONE, rs.getString(Slot.TELEPHONE));
		self.setString(stclContext, Slot.FAX, rs.getString(Slot.FAX));
		self.setString(stclContext, Slot.MOBILE, rs.getString(Slot.MOBILE));
		self.setString(stclContext, Slot.EMAIL, rs.getString(Slot.EMAIL));

		plugFromId(stclContext, self.getResourceSlot(stclContext, Resource.COMMERCIAL), rs.getInt(Slot.MANAGER), Slot.MANAGER, self);

		self.setString(stclContext, Slot.NAME, rs.getString(Slot.NAME));
		self.setString(stclContext, Slot.PASSWD, rs.getString(Slot.PASSWD));
		self.setString(stclContext, Slot.MODE, rs.getString(Slot.MODE));

		PStcl mailing = self.getResourceStencil(stclContext, Resource.MAILING_SERVICE);
		self.plug(stclContext, mailing, Slot.MAILINGS);

		return result;
	}

	@Override
	public void addInSqlAssoc(StclContext stclContext, SqlAssoc assoc, PStcl stencil, PStcl container) {
		super.addInSqlAssoc(stclContext, assoc, stencil, container);

		assoc.pushString(stclContext, CommercialStcl.Slot.NOM);
		assoc.pushString(stclContext, CommercialStcl.Slot.PRENOM);
		assoc.pushString(stclContext, CommercialStcl.Slot.FONCTION);
		assoc.pushString(stclContext, CommercialStcl.Slot.TELEPHONE);
		assoc.pushString(stclContext, CommercialStcl.Slot.FAX);
		assoc.pushString(stclContext, CommercialStcl.Slot.MOBILE);
		assoc.pushString(stclContext, CommercialStcl.Slot.EMAIL);

		assoc.pushId(stclContext, CommercialStcl.Slot.MANAGER);

		assoc.pushString(stclContext, CommercialStcl.Slot.NAME);
		assoc.pushString(stclContext, CommercialStcl.Slot.PASSWD);
		assoc.push(CommercialStcl.Slot.PATH, "/");
		assoc.pushString(stclContext, CommercialStcl.Slot.MODE);
	}

	protected CommercialSlot createChefDeSlot(StclContext stclContext) {
		return new ChefDeSlot(stclContext);
	}

	protected SocieteSlot createEstResponsableDeSlot(StclContext stclContext) {
		return new EstResponsableDeSlot(stclContext);
	}

	protected ContactSlot createContactSlot(StclContext stclContext) {
		return new CommercialContactSlot(stclContext);
	}

	protected _ActionCommercialeSlot createDoitFaireSlot(StclContext stclContext) {
		return new DoitFaireSlot(stclContext, Slot.DOIT_FAIRE);
	}

	protected CommandeSlot createCommandeSlot(StclContext stclContext) {
		return new CommercialCommandeSlot(stclContext);
	}

	// by default excel slot is same as simple slot
	protected SocieteSlot createExcelSocietesSlot(StclContext stclContext) {
		return this._responsable_de_slot;
	}

	// by default excel slot is same as simple slot
	protected ContactSlot createExcelContactsSlot(StclContext stclContext) {
		return this._contacts_slot;
	}

	// by default excel slot is same as simple slot
	protected _ActionCommercialeSlot createExcelActionsCommercialesSlot(StclContext stclContext) {
		return this._doit_faire_slot;
	}

	// by default excel slot is same as simple slot
	protected CommandeSlot createExcelCommandesSlot(StclContext stclContext) {
		return this._commandes_slot;
	}

	@Override
	public FacetResult getFacet(RenderContext<StclContext, PStcl> renderContext) {
		StclContext stclContext = renderContext.getStencilContext();
		String mode = renderContext.getFacetMode();

		if ("societes.excel".equals(mode)) {

			// get sql context
			PStcl service = renderContext.getStencilRendered();
			PStcl sqlContext = service.getStencil(stclContext, Slot.SQL_CONTEXT);

			// query to get all products
			PSlot<StclContext, PStcl> slot = new PSlot<StclContext, PStcl>(this._excel_responsable_de_slot, service);
			String query = this._excel_responsable_de_slot.getKeysQuery(stclContext, null, slot);
			ExcelQuery query1 = new ExcelQuery("Sociétés", query, true, null, null);

			ExcelQuery[] queries = new ExcelQuery[] { query1 };
			return ((SQLContextStcl) sqlContext.getReleasedStencil(stclContext)).excelFileFacet(stclContext, queries, sqlContext);
		} else if ("contacts.excel".equals(mode)) {

			// get sql context
			PStcl service = renderContext.getStencilRendered();
			PStcl sqlContext = service.getStencil(stclContext, Slot.SQL_CONTEXT);

			// query to get all products
			PSlot<StclContext, PStcl> slot = new PSlot<StclContext, PStcl>(this._excel_contacts_slot, service);
			String query = this._excel_contacts_slot.getKeysQuery(stclContext, null, slot);
			ExcelQuery query1 = new ExcelQuery("Contacts", query, true, null, null);

			ExcelQuery[] queries = new ExcelQuery[] { query1 };
			return ((SQLContextStcl) sqlContext.getReleasedStencil(stclContext)).excelFileFacet(stclContext, queries, sqlContext);
		} else if ("actions.excel".equals(mode)) {

			// get sql context
			PStcl service = renderContext.getStencilRendered();
			PStcl sqlContext = service.getStencil(stclContext, Slot.SQL_CONTEXT);

			// query to get all products
			PSlot<StclContext, PStcl> slot = new PSlot<StclContext, PStcl>(this._excel_doit_faire_slot, service);
			String query = this._excel_doit_faire_slot.getKeysQuery(stclContext, null, slot);
			ExcelQuery query1 = new ExcelQuery("Actions", query, true, null, null);

			ExcelQuery[] queries = new ExcelQuery[] { query1 };
			return ((SQLContextStcl) sqlContext.getReleasedStencil(stclContext)).excelFileFacet(stclContext, queries, sqlContext);
		} else if ("commandes.excel".equals(mode)) {

			// get sql context
			PStcl service = renderContext.getStencilRendered();
			PStcl sqlContext = service.getStencil(stclContext, Slot.SQL_CONTEXT);

			// query to get all products
			PSlot<StclContext, PStcl> slot = new PSlot<StclContext, PStcl>(this._excel_commandes_slot, service);
			String query = this._excel_commandes_slot.getKeysQuery(stclContext, null, slot);
			ExcelQuery query1 = new ExcelQuery("Commandes", query, true, null, null);

			ExcelQuery[] queries = new ExcelQuery[] { query1 };
			return ((SQLContextStcl) sqlContext.getReleasedStencil(stclContext)).excelFileFacet(stclContext, queries, sqlContext);
		}

		return super.getFacet(renderContext);
	}

	public String getCRMServicePath(StclContext stclContext, PStcl self) {
		return self.getString(stclContext, Slot.SERVICE_PATH);
	}

	protected SQLCursor getCommercialCursor(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		PStcl container = self.getContainer();
		SQLSlot slot = container.getResourceSlot(stclContext, Resource.COMMERCIAL).getSlot();
		return slot.getCursor(stclContext, self);
	}

	protected String getChefDeKeysCondition(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
		int id = self.getContainer().getInt(stclContext, Slot.ID, 0);
		return String.format("Manager = '%s'", id);
	}

	protected String getDoitFaireCommercialJoin(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		int id = self.getContainer().getInt(stclContext, Slot.ID, 0);
		return String.format("JOIN `commerciaux` x ON s.APourResponsable = x.Id AND s.APourResponsable = '%s'", id);
	}

	protected String getEstResponsableDeCommercialJoin(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		int id = self.getContainer().getInt(stclContext, Slot.ID, 0);
		return String.format("JOIN `commerciaux` x ON s.APourResponsable = x.Id AND s.APourResponsable = '%s'", id);
	}

	protected String getContactCommercialJoin(StclContext stclContext, PSlot<StclContext, PStcl> self) {
		int id = self.getContainer().getInt(stclContext, Slot.ID, 0);
		return String.format("JOIN `commerciaux` x ON s.APourResponsable = x.Id AND s.APourResponsable = '%s'", id);
	}

	protected String getCommercialCommandeKeysCondition(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
		int id = self.getContainer().getInt(stclContext, Slot.ID, 0);
		return String.format("s.APourResponsable = '%s' AND", id);
	}

	private class ChefDeSlot extends CommercialSlot {

		public ChefDeSlot(StclContext stclContext) {
			super(stclContext, CommercialStcl.this, Slot.CHEF_DE);
		}

		@Override
		public SQLCursor getCursor(StclContext stclContext, PSlot<StclContext, PStcl> self) {
			return getCommercialCursor(stclContext, self);
		}

		@Override
		public String getKeysCondition(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
			return getChefDeKeysCondition(stclContext, cond, self);
		}

	}

	private class DoitFaireSlot extends FilteredActionCommercialeSlot {

		public DoitFaireSlot(StclContext stclContext, String name) {
			super(stclContext, CommercialStcl.this, name);
		}

		@Override
		protected String getCommercialJoin(StclContext stclContext, PSlot<StclContext, PStcl> self) {
			return getDoitFaireCommercialJoin(stclContext, self);
		}

	}

	private class EstResponsableDeSlot extends FilteredSocieteSlot {

		public EstResponsableDeSlot(StclContext stclContext) {
			super(stclContext, CommercialStcl.this, Slot.EST_RESPONSABLE_DE);
		}

		@Override
		protected String getCommercialJoin(StclContext stclContext, PSlot<StclContext, PStcl> self) {
			return getEstResponsableDeCommercialJoin(stclContext, self);
		}

	}

	private class CommercialContactSlot extends FilteredContactSlot {

		public CommercialContactSlot(StclContext stclContext) {
			super(stclContext, CommercialStcl.this, Slot.CONTACTS);
		}

		@Override
		protected String getCommercialJoin(StclContext stclContext, PSlot<StclContext, PStcl> self) {
			return getContactCommercialJoin(stclContext, self);
		}

	}

	private class CommercialCommandeSlot extends FilteredCommandeSlot {

		public CommercialCommandeSlot(StclContext stclContext) {
			super(stclContext, CommercialStcl.this, Slot.COMMANDES);
		}

		@Override
		public String getKeysCondition(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
			String c = super.getKeysCondition(stclContext, cond, self);
			String filters = getCommercialCommandeKeysCondition(stclContext, cond, self);
			return String.format("%s %s", filters, c);
		}
	}

	private class ContactsQuerySlot extends CalculatedStringPropertySlot<StclContext, PStcl> {

		public ContactsQuerySlot(StclContext stclContext) {
			super(stclContext, CommercialStcl.this, Slot.CONTACT_QUERY);
		}

		@Override
		public String getValue(StclContext stclContext, PStcl self) {
			PSlot<StclContext, PStcl> slot = new PSlot<StclContext, PStcl>(_contacts_slot, self.getContainer(stclContext));
			return _contacts_slot.getKeysQuery(stclContext, null, slot);
		}

	}
}