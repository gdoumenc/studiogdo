/*
 * Copyright GDO - 2004
 */
package com.gdo.site.model;

import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;

public class PagesMgrStcl extends Stcl {

	public final static String FTP_CONTEXT = "FtpContext";
	public final static String PAGES = "Pages";

	public PagesMgrStcl(StclContext stclContext) {
		super(stclContext);
		// new ContentSlot(stclContext);
		// new _PageStcl(stclContext);
		// new PageSlot(stclContext);
	}

	/*
	 * private class PageSlot extends MultiCalculatedSlot<StclContext, PStcl> {
	 * 
	 * public PageSlot(StclContext stclContext) { super(stclContext,
	 * PagesMgrStcl.this, PAGES, PSlot.ANY); }
	 * 
	 * @Override protected StencilIterator<StclContext, PStcl>
	 * getStencilsList(StclContext stclContext, StencilCondition<StclContext,
	 * PStcl> condition, PSlot<StclContext, PStcl> self) {
	 * 
	 * // get template used to create stencils StclFactory factory = (StclFactory)
	 * stclContext.<StclContext, PStcl> getStencilFactory();
	 * TemplateDescriptor<StclContext, PStcl> desc =
	 * factory.getTemplateDescriptor(stclContext,
	 * "com.gdo.site.model.StructuredPageStcl");
	 * 
	 * // iterate over slots list PStcl container = self.getContainer(); for
	 * (PStcl stcl : container.getStencils(stclContext, "FtpContext/Files")) {
	 * 
	 * // if already in list, do nothing if (getStencilFromList(stclContext,
	 * stcl.getKey(), self) != null) { keepStencilInList(stclContext,
	 * stcl.getKey(), self); continue; }
	 * 
	 * // create the slot stencil PStcl pages = factory.newPStencil(stclContext,
	 * self, stcl.getKey(), desc); pages.setName(stclContext, stcl.getKey());
	 * addStencilInList(stclContext, pages, self);
	 * 
	 * }
	 * 
	 * return cleanList(stclContext, condition, self); }
	 * 
	 * }
	 */

	/*
	 * private class ContentSlot extends CalculatedPropertySlot<StclContext,
	 * PStcl, String> implements IPropCalculator<StclContext, PStcl, String> {
	 * 
	 * public ContentSlot(StclContext stclContext) { super(stclContext,
	 * PagesMgrStcl.this, "Content", null); setCalculator(this); }
	 * 
	 * public Class<String> getValueClass() { return String.class; }
	 * 
	 * public String getValue(StclContext stclContext, PStcl self) { try {
	 * //XmlToCsv xmlParser = new XmlToCsv(); String res = new String(); //String
	 * dir = self.getString(stclContext, "FtpContext/FtpDir", ""); //dir =
	 * dir.substring(dir.lastIndexOf('/') + 1);
	 * 
	 * for (PStcl file : self.getStencils(stclContext, "../FtpContext/Files")) {
	 * String content = file.getString(stclContext, "Content", ""); res +=
	 * content;
	 * 
	 * }
	 * 
	 * return res; } catch (Exception e) { return "error"; } }
	 * 
	 * public void setValue(StclContext stclContext, String value, PStcl self) {
	 * //throw new NotImplementedException("Cannot set results file"); }
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
	 * stcl.getStencil(); ftpContext.size(stclContext, path, stcl); } } return
	 * null; }
	 */
}