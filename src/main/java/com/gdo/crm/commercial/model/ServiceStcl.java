package com.gdo.crm.commercial.model;

import com.gdo.crm.commercial.filter.FilteredActionCommercialeSlot;
import com.gdo.crm.commercial.filter.FilteredCommandeSlot;
import com.gdo.crm.commercial.filter.FilteredContactSlot;
import com.gdo.crm.commercial.filter.FilteredSocieteSlot;
import com.gdo.sql.model.ExcelQuery;
import com.gdo.sql.model.SQLContextStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.faces.RenderContext;
import com.gdo.stencils.facet.FacetResult;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;

public class ServiceStcl extends com.gdo.project.model.ServiceStcl {

    // slots defined
    public interface Slot extends com.gdo.project.model.ServiceStcl.Slot {
        String SQL_CONTEXT = "SqlContext";

        String COMMERCIAUX = "Commerciaux";

        String SOCIETES = "Societes";
        String ACTIVITES = "Activites";
        String CONTACTS = "Contacts";
        String ACTIONS_COMMERCIALES = "ActionsCommerciales";
        String COMMANDES = "Commandes";

        String ALL_SOCIETES = "AllSocietes";
        String ALL_CONTACTS = "AllContacts";
        String ALL_COMMANDES = "AllCommandes";
        String ALL_ACTIONS_COMMERCIALES = "AllActionsCommerciales";
        String TYPES_D_ACTION_COMMERCIALE = "TypesDActionCommerciale";

        String EXCEL_SOCIETE = "ExcelSociete";
        String EXCEL_CONTACT = "ExcelContact";
        String EXCEL_ACTION_COMMERCIALE = "ExcelActionsCommerciale";
        String EXCEL_COMMANDE = "ExcelCommande";

        String ACTION_COMMERCIALE_TEMPLATE = "ActionCommercialeTemplate";
        String ACTIVITE_TEMPLATE = "ActiviteTemplate";
        String COMMANDE_TEMPLATE = "CommandeTemplate";
        String COMMERCIAL_TEMPLATE = "CommercialTemplate";
        String CONTACT_TEMPLATE = "ContactTemplate";
        String SOCIETE_TEMPLATE = "SocieteTemplate";
        String TYPE_D_ACTION_COMMERCIALE_TEMPLATE = "TypeDActionCommercialeTemplate";
    }

    protected SocieteSlot _societes;
    protected ActiviteSlot _activites;
    protected CommercialSlot _commerciaux;
    protected ContactSlot _contacts;
    protected CommandeSlot _commandes;
    protected _ActionCommercialeSlot _actions_commerciales;
    protected TypeDActionCommercialeSlot _types_d_action_commerciale;

    private SocieteSlot _filteredSocietes;
    private ContactSlot _filteredContacts;
    private _ActionCommercialeSlot _filteredActionsCommerciales;
    private CommandeSlot _filteredCommandes;

    public ServiceStcl(StclContext stclContext) {
        super(stclContext);

        // admin slots
        this._activites = createActivitesSlot(stclContext);
        this._commerciaux = createCommerciauxSlot(stclContext);
        this._types_d_action_commerciale = createTypesDActionCommercialeSlot(stclContext);

        // main slots
        this._societes = createAllSocietesSlot(stclContext);
        this._contacts = createAllContactsSlot(stclContext);
        this._commandes = createAllCommandesSlot(stclContext);
        this._actions_commerciales = createAllActionsCommercialesSlot(stclContext);

        // filtered slots
        this._filteredSocietes = createFilteredSocietesSlot(stclContext);
        this._filteredContacts = createFilteredContactsSlot(stclContext);
        this._filteredActionsCommerciales = createFilteredActionsCommercialesSlot(stclContext);
        this._filteredCommandes = createFilteredCommandesSlot(stclContext);
    }

    protected ActiviteSlot createActivitesSlot(StclContext stclContext) {
        return new ActiviteSlot(stclContext, this);
    }

    protected CommercialSlot createCommerciauxSlot(StclContext stclContext) {
        return new CommercialSlot(stclContext, this, Slot.COMMERCIAUX);
    }

    protected TypeDActionCommercialeSlot createTypesDActionCommercialeSlot(StclContext stclContext) {
        return new TypeDActionCommercialeSlot(stclContext, this);
    }

    protected SocieteSlot createAllSocietesSlot(StclContext stclContext) {
        return new SocieteSlot(stclContext, this, Slot.ALL_SOCIETES);
    }

    protected ContactSlot createAllContactsSlot(StclContext stclContext) {
        return new ContactSlot(stclContext, this, Slot.ALL_CONTACTS);
    }

    protected _ActionCommercialeSlot createAllActionsCommercialesSlot(StclContext stclContext) {
        return new _ActionCommercialeSlot(stclContext, this, Slot.ALL_ACTIONS_COMMERCIALES);
    }

    protected CommandeSlot createAllCommandesSlot(StclContext stclContext) {
        return new CommandeSlot(stclContext, this, Slot.ALL_COMMANDES);
    }

    protected SocieteSlot createFilteredSocietesSlot(StclContext stclContext) {
        return new FilteredSocieteSlot(stclContext, this, Slot.SOCIETES);
    }

    protected ContactSlot createFilteredContactsSlot(StclContext stclContext) {
        return new FilteredContactSlot(stclContext, this, Slot.CONTACTS);
    }

    protected _ActionCommercialeSlot createFilteredActionsCommercialesSlot(StclContext stclContext) {
        return new FilteredActionCommercialeSlot(stclContext, this, Slot.ACTIONS_COMMERCIALES);
    }

    protected CommandeSlot createFilteredCommandesSlot(StclContext stclContext) {
        return new FilteredCommandeSlot(stclContext, this, Slot.COMMANDES);
    }

    @Override
    public FacetResult getFacet(RenderContext<StclContext, PStcl> renderContext) {
        StclContext stclContext = renderContext.getStencilContext();
        String mode = renderContext.getFacetMode();

        if ("societes.excel".equals(mode)) {

            // get sql context
            PStcl service = renderContext.getStencilRendered();
            PStcl sqlContext = service.getStencil(stclContext, Slot.SQL_CONTEXT);

            // query to get all societes
            PSlot<StclContext, PStcl> slot = new PSlot<>(this._filteredSocietes, service);
            String query = this._filteredSocietes.getKeysQuery(stclContext, null, slot);
            ExcelQuery query1 = new ExcelQuery("Sociétés", query, true, null, null);

            ExcelQuery[] queries = new ExcelQuery[] { query1 };
            return ((SQLContextStcl) sqlContext.getReleasedStencil(stclContext)).excelFileFacet(stclContext, queries, sqlContext);
        } else if ("contacts.excel".equals(mode)) {

            // get sql context
            PStcl service = renderContext.getStencilRendered();
            PStcl sqlContext = service.getStencil(stclContext, Slot.SQL_CONTEXT);

            // query to get all contacts
            PSlot<StclContext, PStcl> slot = new PSlot<>(this._filteredContacts, service);
            String query = this._filteredContacts.getKeysQuery(stclContext, null, slot);
            ExcelQuery query1 = new ExcelQuery("Contacts", query, true, null, null);

            ExcelQuery[] queries = new ExcelQuery[] { query1 };
            return ((SQLContextStcl) sqlContext.getReleasedStencil(stclContext)).excelFileFacet(stclContext, queries, sqlContext);
        } else if ("actions.excel".equals(mode)) {

            // get sql context
            PStcl service = renderContext.getStencilRendered();
            PStcl sqlContext = service.getStencil(stclContext, Slot.SQL_CONTEXT);

            // query to get all actions
            PSlot<StclContext, PStcl> slot = new PSlot<>(this._filteredActionsCommerciales, service);
            String contacts = this._filteredActionsCommerciales.getKeysQuery(stclContext, null, slot);
            ExcelQuery query1 = new ExcelQuery("Actions", contacts, true, null, null);

            ExcelQuery[] queries = new ExcelQuery[] { query1 };
            return ((SQLContextStcl) sqlContext.getReleasedStencil(stclContext)).excelFileFacet(stclContext, queries, sqlContext);
        } else if ("commandes.excel".equals(mode)) {

            // get sql context
            PStcl service = renderContext.getStencilRendered();
            PStcl sqlContext = service.getStencil(stclContext, Slot.SQL_CONTEXT);

            // query to get all commandes
            PSlot<StclContext, PStcl> slot = new PSlot<>(this._filteredCommandes, service);
            String contacts = this._filteredCommandes.getKeysQuery(stclContext, null, slot);
            ExcelQuery query1 = new ExcelQuery("Devis_Commandes", contacts, true, null, null);

            ExcelQuery[] queries = new ExcelQuery[] { query1 };
            return ((SQLContextStcl) sqlContext.getReleasedStencil(stclContext)).excelFileFacet(stclContext, queries, sqlContext);
        }

        return super.getFacet(renderContext);
    }

}
