/**
 * Copyright GDO - 2004
 */
package com.gdo.mail.model;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.fileupload.FileItem;

import com.gdo.mail.cmd.AddRecipientsFromFile;
import com.gdo.mail.cmd.ReadRecipientsFromFile;
import com.gdo.project.cmd.CreateAtomic;
import com.gdo.project.cmd.CreateInOneStep;
import com.gdo.project.util.model.NamedStcl;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.faces.RenderContext;
import com.gdo.stencils.facet.FacetResult;
import com.gdo.stencils.facet.FacetType;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.MultiSlot;

public class DistributionListStcl extends Stcl {

	public static final String NORMAL_MODE = "";
	public static final String TEST_MODE = "test";

	public interface Slot extends Stcl.Slot {
		String MODE = "Mode";

		String FROM = "From";
		String TO = "To";
		String TEST = "Test";

		String CHILDREN = "Children";

		// status of emails insertion process
		String IN_INSERTING_PROCESS = "InInsertingProcess";
		String INSERTING_COUNTER = "InsertingCounter";
	}

	public interface Command extends NamedStcl.Command {
		String ADD_TO = "AddTo";
		String ADD_FROM_FILE = "AddFromFile";
		String NEW_RECIPIENT = "NewRecipient";
		String READ = "Read";
	}

	public interface Facet {
		String EXCEL = "excel";
		String TEXT = "text";
		String PROPERTIES = "properties";
	}

	public DistributionListStcl(StclContext stclContext) {
		super(stclContext);

		propSlot(Slot.MODE);

		new ToSlot(stclContext);
		singleSlot(Slot.TEST);
		singleSlot(Slot.FROM);
		multiSlot(Slot.CHILDREN);

		propSlot(Slot.IN_INSERTING_PROCESS, false);
		propSlot(Slot.INSERTING_COUNTER, 0);

		command(Command.ADD_TO, CreateAtomic.class, RecipientStcl.class.getName(), "Target/To", "int", 1);
		command(Command.NEW_RECIPIENT, CreateInOneStep.class, RecipientStcl.class.getName(), "Target/To", "int", 1);
		command(Command.ADD_FROM_FILE, AddRecipientsFromFile.class);
		command(Command.READ, ReadRecipientsFromFile.class);
	}

	@Override
	public FacetResult getFacet(RenderContext<StclContext, PStcl> renderContext) {
		StclContext stclContext = renderContext.getStencilContext();
		PStcl self = renderContext.getStencilRendered();
		String type = renderContext.getFacetType();
		String mode = renderContext.getFacetMode();

		if (FacetType.FILE.equals(type)) {
			if (Facet.TEXT.equals(mode)) {
				StringBuffer res = new StringBuffer();
				for (PStcl stcl : self.getStencils(stclContext, Slot.TO)) {
					String add = stcl.getString(stclContext, RecipientStcl.Slot.ADDRESS, "");
					res.append(add);
					res.append("\r\n");
				}
				InputStream is = new ByteArrayInputStream(res.toString().getBytes());
				FacetResult result = new FacetResult(is, "text/text");
				result.setContentLength(res.length());
				return result;
			}
			if (Facet.PROPERTIES.equals(mode)) {
				StringBuffer res = new StringBuffer();
				for (PStcl stcl : self.getStencils(stclContext, Slot.TO)) {
					String name = stcl.getName(stclContext);
					String add = stcl.getString(stclContext, RecipientStcl.Slot.ADDRESS, "");
					res.append(name).append('=').append(add);
					res.append("\r\n");
				}
				InputStream is = new ByteArrayInputStream(res.toString().getBytes());
				FacetResult result = new FacetResult(is, "text/text");
				result.setContentLength(res.length());
				return result;
			}
			if (Facet.EXCEL.equals(mode)) {
				// InputStream is = new
				// ByteArrayInputStream(res.toString().getBytes());
				// return new FacetResult(is, "application/vnd.ms-excel");
			}
		}
		return super.getFacet(renderContext);
	}

	@Override
	public void multipart(StclContext stclContext, String fileName, FileItem item, PStcl self) {
		try {
			InputStream is = item.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = reader.readLine()) != null) {
				PStcl index = self.newPStencil(stclContext, Slot.TO, Key.NO_KEY, RecipientStcl.class);
				index.setString(stclContext, RecipientStcl.Slot.ADDRESS, line);
			}
		} catch (Exception e) {
			logError(stclContext, e.toString());
		}
	}

	
	
	@Override
	public void afterCompleted(StclContext stclContext, PStcl self) {
		super.afterCompleted(stclContext, self);
		
		self.newPStencil(stclContext, Slot.TEST, Key.NO_KEY, RecipientStcl.class);
		self.newPStencil(stclContext, Slot.FROM, Key.NO_KEY, RecipientStcl.class);
	}



	private class ToSlot extends MultiSlot<StclContext, PStcl> {
		public ToSlot(StclContext stclContext) {
			super(stclContext, DistributionListStcl.this, Slot.TO);
		}

		@Override
		protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
			PStcl container = self.getContainer();
			String mode = container.getString(stclContext, Slot.MODE, TEST_MODE);

			if (TEST_MODE.equals(mode)) {
				return container.getStencils(stclContext, Slot.TEST);
			}
			return super.getStencilsList(stclContext, cond, self);
		}
	}
}