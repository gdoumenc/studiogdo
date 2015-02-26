/*
 * Copyright GDO - 2004
 */
package com.gdo.site.model;

import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;

public class ServiceStcl extends Stcl {

	public static final String FTP_CONTEXT = "FtpContext";
	public static final String PAGES_MGR = "PagesMgr";
	public static final String RES_MGR = "ResMgr";

	public interface Slot extends Stcl.Slot {
		String FTP_CONTEXT = "FtpContext";
		String PAGES = "Pages";
	}

	public ServiceStcl(StclContext stclContext) {
		super(stclContext);
		// new PagesMgrSlot(stclContext);

	}

	/*
	 * private class PagesMgrSlot extends MultiCalculatedSlot<StclContext, PStcl>
	 * {
	 * 
	 * public PagesMgrSlot(StclContext stclContext) { super(stclContext,
	 * ServiceStcl.this, PAGES_MGR, PSlot.ANY); }
	 * 
	 * @Override protected StencilIterator<StclContext, PStcl>
	 * getStencilsList(StclContext stclContext, StencilCondition<StclContext,
	 * PStcl> condition, PSlot<StclContext, PStcl> self) {
	 * 
	 * // get template used to create stencils StclFactory factory = (StclFactory)
	 * stclContext.<StclContext, PStcl> getStencilFactory();
	 * TemplateDescriptor<StclContext, PStcl> desc =
	 * factory.getTemplateDescriptor(stclContext,
	 * "com.gdo.site.model.PagesMgrStcl");
	 * 
	 * // iterate over slots list PStcl container = self.getContainer(); for
	 * (PStcl stcl : container.getStencils(stclContext, "FtpContext/Files")) {
	 * 
	 * // if already in list, do nothing if (getStencilFromList(stclContext,
	 * stcl.getKey(), self) != null) { keepStencilInList(stclContext,
	 * stcl.getKey(), self); continue; }
	 * 
	 * 
	 * // create the slot stencil PStcl pages = factory.newPStencil(stclContext,
	 * self, stcl.getKey(), desc); pages.setName(stclContext, stcl.getKey());
	 * addStencilInList(stclContext, pages, self);
	 * 
	 * 
	 * }
	 * 
	 * return cleanList(stclContext, condition, self); }
	 * 
	 * }
	 */
	/*
	 * @Override public InputStream getResourceAsStream(StclContext stclContext,
	 * String path, PStcl asPlugged) {
	 * 
	 * // by default, use the FtpContext slot if defined if
	 * (hasStencils(stclContext, FTP_CONTEXT, null)) { PStcl stcl =
	 * asPlugged.getStencil(stclContext, FTP_CONTEXT); if (stcl instanceof
	 * FtpContextStcl) { FtpContextStcl ftpContext = (FtpContextStcl)
	 * stcl.getStencil(); return ftpContext.get(stclContext, path, stcl); } }
	 * return super.getResourceAsStream(stclContext, path, asPlugged); }
	 */
}