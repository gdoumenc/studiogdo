/**
 * Copyright GDO - 2004
 */
package com.gdo.mail.model;

import java.util.Date;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.Base64;

import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.PathUtils;

public class SQLMailStcl extends MailStcl {

	public interface Slot extends MailStcl.Slot {
		String SQL_CONTEXT = "SqlContext";
		String SQL_SEGMENT = "SqlSegment";

		String TRACKER_PATTERN = "TrackerPattern";
		String TRACKER_REPLACEMENT = "TrackerReplacement";
		String TRACKER_DATA_BASE = "TrackerDataBase";
	}

	public SQLMailStcl(StclContext stclContext) {
		super(stclContext);
	}

	/**
	 * Replace resource references to tracking info.
	 */
	@Override
	public String getContent(StclContext stclContext, Properties props, PStcl self) {
		String content;

		// try {
		PStcl generator = self.getStencil(stclContext, Slot.GENERATOR);
		if (generator.isNotNull()) {
			content = generator.getString(stclContext, "FilesGenerated($content)", "");
		} else {
			content = self.getExpandedString(stclContext, Slot.CONTENT, "");
		}

		// if to list not defined from sql slot then don't track
		if (props.get(CommandContext.PARAM(3)) != null) {
			return content;
		}

		// replaces tracker
		String tracker = self.getString(stclContext, Slot.TRACKER_PATTERN, "");
		if (StringUtils.isNotBlank(tracker)) {
			PStcl to = self.getStencil(stclContext, SQLMailStcl.Slot.TO);
			String id = to.getString(stclContext, SQLRecipientStcl.Slot.ID, "");
			String from = PathUtils.compose(SQLMailStcl.Slot.SQL_SEGMENT, SQLSegmentStcl.Slot.TO, SQLDistributionListStcl.Slot.FROM_TABLE);
			String table = self.getString(stclContext, from, "");

			// replacement can be done only iif id and table are not blank
			if (StringUtils.isNotBlank(id) && StringUtils.isNotBlank(table)) {

				// get database
				String database = self.getString(stclContext, Slot.TRACKER_DATA_BASE, "");
				String replacement = self.getString(stclContext, Slot.TRACKER_REPLACEMENT, "");
				if (StringUtils.isNotEmpty(database) && StringUtils.isNotEmpty(replacement)) {
					content = content.replaceAll(tracker, replacement);

					Base64 base = new Base64();
					String param = database + "/" + table + "/" + id + "/" + (new Date()).getTime();
					String encoded = new String(base.encode(param.getBytes()));
					content = content.replaceAll(tracker, encoded);
				}
			} else {
				content = content.replaceAll(tracker, "");
			}
		}

		return content;
		/*
		 * // cryption parameters File file = CatalinaUtils.getFile(stclContext,
		 * "key"); Key key = (Key) CryptoHelper.deserialize(file); String project =
		 * stclContext.getConfigParameter(StudioConfig.PROJECT_NAME); String operId
		 * = self.getString(stclContext, PathUtils.compose(Slot.SQL_SEGMENT,
		 * SQLSegmentStcl.Slot.SQL_ID), ""); String toId = getTo(stclContext,
		 * self).getString(stclContext, SQLRecipientStcl.Slot.ID, null); // at least
		 * once already verified // for each counters change url link
		 * StencilIterator<StclContext, PStcl> counters =
		 * self.getStencils(stclContext, "COUNTERS"); for (PStcl stcl : counters) {
		 * // remove scheme String web = stcl.getString(stclContext,
		 * "FtpContext/HttpDir", ""); if (web.startsWith("http:/")) web =
		 * web.substring("http:/".length()); if (web.startsWith("/")) web =
		 * web.substring("/".length()); // get url without ext String url =
		 * stcl.getString(stclContext, "Url", ""); String url2 = "http://" + web +
		 * "/" + url; String res = url; String ext = ""; int index =
		 * res.lastIndexOf("."); if (index > 0) { ext = res.substring(index + 1);
		 * res = res.substring(0, index); } String crypted =
		 * CryptoHelper.byteToHex(CryptoHelper.encrypt(key, web + "/" + res)); if
		 * (!StringUtils.isEmpty(ext)) crypted += "." + ext; // create track url
		 * String service = "1"; String track =
		 * String.format("res1.studiogdo.com/%s/%s/%s/%s", service, project, operId,
		 * toId); // replace in content String newRef =
		 * String.format("href=http://%s/%s", track, crypted); String newSrc =
		 * String.format("src=http://%s/%s", track, crypted); String ref =
		 * String.format("href=\"[/]?%s\"", url); content =
		 * StringHelper.replaceAll(content, ref, newRef); String src =
		 * String.format("src=\"[/]?%s\"", url); content =
		 * StringHelper.replaceAll(content, src, newSrc); String ref2 =
		 * String.format("href=\"%s\"", url2); content =
		 * StringHelper.replaceAll(content, ref2, newRef); String src2 =
		 * String.format("src=\"%s\"", url2); content =
		 * StringHelper.replaceAll(content, src2, newSrc); } // for each counters
		 * change url link StencilIterator<StclContext, PStcl> loaded =
		 * self.getStencils(stclContext, "Loaded"); for (PStcl stcl : loaded) { //
		 * remove scheme String web = stcl.getString(stclContext,
		 * "FtpContext/HttpDir", ""); if (web.startsWith("http:/")) web =
		 * web.substring("http:/".length()); if (web.startsWith("/")) web =
		 * web.substring("/".length()); // get url without ext String url =
		 * stcl.getString(stclContext, "Url", ""); String url2 = "http://" + web +
		 * "/" + url; String res = url; String ext = ""; int index =
		 * res.lastIndexOf("."); if (index > 0) { ext = res.substring(index + 1);
		 * res = res.substring(0, index); } String crypted =
		 * CryptoHelper.byteToHex(CryptoHelper.encrypt(key, web + "/" + res)); if
		 * (!StringUtils.isEmpty(ext)) crypted += "." + ext; // create track url
		 * String service = "2"; String track =
		 * String.format("res1.studiogdo.com/%s/%s/%s/%s", service, project, operId,
		 * toId); // replace in content String newRef =
		 * String.format("href=http://%s/%s", track, crypted); String newSrc =
		 * String.format("src=http://%s/%s", track, crypted); String ref =
		 * String.format("href=\"[/]?%s\"", url); content =
		 * StringHelper.replaceAll(content, ref, newRef); String src =
		 * String.format("src=\"[/]?%s\"", url); content =
		 * StringHelper.replaceAll(content, src, newSrc); String ref2 =
		 * String.format("href=\"%s\"", url2); content =
		 * StringHelper.replaceAll(content, ref2, newRef); String src2 =
		 * String.format("src=\"%s\"", url2); content =
		 * StringHelper.replaceAll(content, src2, newSrc); } //content =
		 * content.replaceAll("[&][#]160;", ""); return content; } catch (Exception
		 * e) { if (getLog().isWarnEnabled()) getLog().warn(stclContext, e); throw
		 * new StencilException("no content", e); }
		 */
	}
	/*
	 * private PStcl getTo(StclContext stclContext, PStcl self) { if
	 * (StencilUtils.isNull(self)) return null; StencilIterator<StclContext,
	 * PStcl> to = self.getStencils(stclContext, Slot.TO); return to.next(); // at
	 * least once already verified }
	 */
}