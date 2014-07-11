/**
 * Copyright GDO - 2004
 */
package com.gdo.generator.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.gdo.helper.StringHelper;
import com.gdo.project.util.model.NamedStcl;
import com.gdo.stencils.Stcl.IMaskFacetGenerator;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cond.PathCondition;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.faces.stencil.GdoStencil;
import com.gdo.stencils.factory.StencilFactory;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.MultiCalculatedSlot;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;

public class GeneratorStcl extends NamedStcl implements IMaskFacetGenerator {

	public interface Slot extends NamedStcl.Slot {

		// mask parameters
		String MASK = "Mask";
		String CHILDREN = "Children";
		String ITERATION = "Iteration";
		String ESCAPE = "Escape";
		String REMOVE = "Remove";

		// file genretation parameters
		String FTP_CONTEXT = "FtpContext";
		String GENERATE_FILE = "GenerateFile";
		String GENERATE_TEXT = "GenerateText";
		String APPEND_IN_FILE = "AppendInFile";
		String FILES_GENERATED = "FilesGenerated";
		String DIR = "Dir";
		String URL = "Url";
		String TRACE = "HtmlTrace";
		String CREATE_DIR = "CreateDir"; // boolean prop to
	}

	private List<PStcl> _filesGenerated; // name as key,

	public GeneratorStcl(StclContext stclContext) {
		super(stclContext);

		new FilesGeneratedSlot(stclContext);
	}

	@Override
	public InputStream getFacet(StclContext stclContext, String mode, PStcl on, PStcl main, PStcl self) {

		// no mode means get content from mask
		if (StringUtils.isEmpty(mode)) {
			return getGeneratedContent(stclContext, on, main, self);
		}

		// mode is the children key used for generation
		StencilCondition<StclContext, PStcl> cond = PathCondition.<StclContext, PStcl> newKeyCondition(stclContext, new Key<String>(mode), self);
		PStcl sub = self.getStencil(stclContext, Slot.CHILDREN, cond);
		if (StencilUtils.isNull(sub)) {
			String res = String.format("<!--no generator child defined at key %s for %s-->", mode, on);
			return IOUtils.toInputStream(res);
		}

		// gets the facet from children generator
		IMaskFacetGenerator gen = (IMaskFacetGenerator) sub.getReleasedStencil(stclContext);
		return gen.getFacet(stclContext, null, on, self, sub);
	}

	// give the mask facet calculated
	private InputStream getGeneratedContent(StclContext stclContext, PStcl on, PStcl main, PStcl self) {

		// get content for mask extension
		String mask = self.getNotExpandedString(stclContext, Slot.MASK, StringHelper.EMPTY_STRING);

		// iteration format
		String content = mask;
		int iteration = self.getInt(stclContext, Slot.ITERATION, 1);
		for (int i = 0; i < iteration; i++) {
			try {
				content = on.format(stclContext, content);
			} catch (Exception e) {
				content = String.format("Exception %s in %s", e, mask);
			}
		}

		// removes some contents declared to be removed
		for (PStcl rem : self.getStencils(stclContext, Slot.REMOVE)) {
			String remove = rem.getString(stclContext, PathUtils.THIS, "");
			if (StringUtils.isNotEmpty(remove)) {
				content = content.replaceAll(remove, "");
			}
		}

		// escape result
		String escape = self.getString(stclContext, Slot.ESCAPE, null);
		if (GdoStencil.ESCAPE_XML.equals(escape)) {
			content = StringEscapeUtils.escapeXml(content);
		} else if (GdoStencil.ESCAPE_HTML.equals(escape)) {
			content = StringEscapeUtils.escapeHtml3(content);
		} else if (GdoStencil.ESCAPE_JAVA.equals(escape)) {
			content = StringEscapeUtils.escapeJava(content);
		} else if (GdoStencil.ESCAPE_JAVA_SCRIPT.equals(escape)) {
			// content = StringEscapeUtils.escapeJavaScript(content);
		} else if (GdoStencil.ESCAPE_SQL.equals(escape)) {
			content = StringHelper.escapeSql(content);
		}

		// generate file on main
		boolean generateFile = self.getBoolean(stclContext, Slot.GENERATE_FILE, false);
		if (generateFile) {
			String url = getFullUrl(stclContext, on, self);
			boolean append = self.getBoolean(stclContext, Slot.APPEND_IN_FILE, false);
			if (append) {
				getLog().error(stclContext, "Append in generation not done");
				// to be done
				// prop = (PropertyStcl<String>) _filesGenerated.get(url);
				// PStcl self = prop.self();
				// String previous = prop.getStringValue(stclContext, "", self);
				// content = previous + content;
				// prop.setValue(stclContext, content, self);
			} else {
				main.newPProperty(stclContext, Slot.FILES_GENERATED, new Key<String>(url), content);
			}
		}

		// generate text
		boolean generateText = self.getBoolean(stclContext, Slot.GENERATE_TEXT, true);
		if (generateText)
			return new ByteArrayInputStream(content.getBytes());
		return StringHelper.EMPTY_STRING_INPUT_STREAM;
	}

	/**
	 * Concats dir and url to create full url and expands format from on stencil.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param on
	 *          the stencil on which generation is done.
	 * @param self
	 *          the generator as a plugged stencil.
	 * @return the complete url expanded.
	 */
	private String getFullUrl(StclContext stclContext, PStcl on, PStcl self) {

		// get url name
		String url = self.getString(stclContext, Slot.URL, StringHelper.EMPTY_STRING);
		if (StringUtils.isEmpty(url)) {
			return StringHelper.EMPTY_STRING;
		}

		// get directory
		String dir = self.getString(stclContext, Slot.DIR, StringHelper.EMPTY_STRING);

		// composes and formats
		String full = PathUtils.compose(dir, url);
		return on.format(stclContext, full);
	}

	private class FilesGeneratedSlot extends MultiCalculatedSlot<StclContext, PStcl> {
		public FilesGeneratedSlot(StclContext stclContext) {
			super(stclContext, GeneratorStcl.this, Slot.FILES_GENERATED, PSlot.ANY);
		}

		@Override
		protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
			PStcl generator = self.getContainer();
			PStcl on = generator.getStencil(stclContext, PathUtils.PARENT);
			GeneratorStcl.this._filesGenerated = null;
			InputStream is = getFacet(stclContext, null, on, generator, generator);
			Reader reader = new InputStreamReader(is);
			generator.newPProperty(stclContext, Slot.FILES_GENERATED, new Key<String>("$content"), StringHelper.read(reader));
			return StencilUtils.< StclContext, PStcl> iter(stclContext, GeneratorStcl.this._filesGenerated.iterator(), cond, self);
		}

		@Override
		protected PStcl doPlug(StclContext stclContext, PStcl stencil, IKey key, PSlot<StclContext, PStcl> self) {
			if (GeneratorStcl.this._filesGenerated == null)
				GeneratorStcl.this._filesGenerated = new ArrayList<PStcl>();

			// remove previous already existing stencil at key
			List<PStcl> toBeRemoved = new ArrayList<PStcl>();
			for (PStcl stcl : GeneratorStcl.this._filesGenerated) {
				if (stcl.getKey().equals(key))
					toBeRemoved.add(stcl);
			}
			for (PStcl stcl : toBeRemoved) {
				GeneratorStcl.this._filesGenerated.remove(stcl);
			}

			// create the plugged stencil
			StencilFactory<StclContext, PStcl> factory = (StencilFactory<StclContext, PStcl>) stclContext.getStencilFactory();
			PStcl plugged = factory.createPStencil(stclContext, self, key, stencil);
			GeneratorStcl.this._filesGenerated.add(plugged);
			return plugged;
		}

		@Override
		protected void doUnplug(StclContext stclContext, PStcl stencil, IKey key, PSlot<StclContext, PStcl> self) {
			GeneratorStcl.this._filesGenerated = null;
		}
	}

}