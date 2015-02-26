/**
 * Copyright GDO - 2004
 */
/*
 * Copyright GDO - 2004
 */
package com.gdo.mail.cmd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.gdo.mail.model.DistributionListStcl;
import com.gdo.mail.model.SQLRecipientStcl;
import com.gdo.project.model.ComposedActionStcl;
import com.gdo.project.model._CommandThread;
import com.gdo.stencils.CommandStcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.CalculatedIntegerPropertySlot;
import com.gdo.stencils.slot.CalculatedStringPropertySlot;
//import com.gdo.stencils.util.GlobalCounter;

public class ReadRecipientsFromFile extends ComposedActionStcl {

	public interface Slot extends ComposedActionStcl.Slot {
		String FIRST_LINES = "FirstLines";

		String SKIP_FIRST_LINE = "SkipFirstLine";
		String SPLITED_BY = "SplitedBy";
		String COLUMN_INDEX = "ColumnIndex";
		String ENCLOSED_BY = "EnclosedBy";

		String TEST = "Test";
		String COUNTER = "Counter";
	}

	public interface Status {
		int NO_RESSOURCE_CLASS_NAME = 1;
		int NO_CONTEXT_SELECTED = 2;
		int FILE_NOT_FOUND = 3;
	}

	private File _file; // temporary file used to store addresses
	private int _counter; // number of addresses added

	public ReadRecipientsFromFile(StclContext stclContext) {
		super(stclContext);

		new FirstLineSlot(stclContext);
		new TestSlot(stclContext);
		new CounterSlot(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> performSteps(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		int current = getActiveStepIndex();

		// at step 3 read file to insert emails
		if (current == 3) {
			new InsertThread(cmdContext, self);
		}
		return success(cmdContext, self);
	}

//	@Override
//	public void multipart(StclContext stclContext, String fileName, FileItem item, PStcl self) {
//		try {
//			if (this._file == null) {
//				this._file = File.createTempFile(GlobalCounter.uniqueID(), "");
//			}
//			FileOutputStream out = new FileOutputStream(this._file);
//			IOUtils.copy(item.getInputStream(), out);
//			out.close();
//		} catch (IOException e) {
//			logWarn(stclContext, e.toString());
//		}
//	}

	private String getAddress(StclContext stclContext, String string, PStcl self) {

		// searches for the splitted element
		String split = self.getString(stclContext, Slot.SPLITED_BY, "");
		if (StringUtils.isBlank(split)) {
			return removeEnclosing(stclContext, string, self);
		}

		// returns column index found
		int index = self.getInt(stclContext, Slot.COLUMN_INDEX, 1);
		String[] adds = string.split(split);
		if (adds.length >= index) {
			return removeEnclosing(stclContext, adds[index - 1], self);
		}
		return "";
	}

	private String removeEnclosing(StclContext stclContext, String string, PStcl self) {
		String enclosing = self.getString(stclContext, Slot.ENCLOSED_BY, "");
		if (StringUtils.isBlank(enclosing)) {
			return string;
		}
		int start = string.indexOf(enclosing);
		int stop = string.lastIndexOf(enclosing);
		if (start >= 0 && stop > start && start < string.length()) {
			return string.substring(start + 1, stop);
		}
		return string;
	}

	private class FirstLineSlot extends CalculatedStringPropertySlot<StclContext, PStcl> {

		public FirstLineSlot(StclContext stclContext) {
			super(stclContext, ReadRecipientsFromFile.this, Slot.FIRST_LINES);
		}

		public String getValue(StclContext stclContext, PStcl self) {
			try {
				if (ReadRecipientsFromFile.this._file != null) {
					BufferedReader reader = new BufferedReader(new FileReader(ReadRecipientsFromFile.this._file));
					String content = "";
					for (int i = 0; i < 5; i++) {
						String line = reader.readLine();
						if (line == null)
							break;
						content += line + "\n";
					}
					reader.close();
					return content;
				}
				return "";
			} catch (IOException e) {
				if (getLog().isWarnEnabled()) {
					getLog().warn(stclContext, e);
				}
				return e.getMessage();
			}
		}
	}

	private class TestSlot extends CalculatedStringPropertySlot<StclContext, PStcl> {

		public TestSlot(StclContext stclContext) {
			super(stclContext, ReadRecipientsFromFile.this, Slot.TEST);
		}

		public String getValue(StclContext stclContext, PStcl self) {
			try {
				if (ReadRecipientsFromFile.this._file != null) {
					PStcl command = self.getContainer(stclContext);
					BufferedReader reader = new BufferedReader(new FileReader(ReadRecipientsFromFile.this._file));

					// reads line
					String content = reader.readLine();
					if (command.getBoolean(stclContext, Slot.SKIP_FIRST_LINE, false)) {
						content = reader.readLine();
					}

					reader.close();

					// returns the first address matched
					return getAddress(stclContext, content, command);
				}
				return "";
			} catch (IOException e) {
				if (getLog().isWarnEnabled()) {
					getLog().warn(stclContext, e);
				}
				return e.getMessage();
			}
		}
	}

	private class CounterSlot extends CalculatedIntegerPropertySlot<StclContext, PStcl> {

		public CounterSlot(StclContext stclContext) {
			super(stclContext, ReadRecipientsFromFile.this, Slot.COUNTER);
		}

		@Override
		public int getIntegerValue(StclContext stclContext, PStcl self) {
			return ReadRecipientsFromFile.this._counter;
		}
	}

	private class InsertThread extends _CommandThread {

		public InsertThread(CommandContext<StclContext, PStcl> cmdContext, PStcl reference) {
			super(cmdContext, reference);
		}

		public void run() {
			try {
				StclContext stclContext = getStencilContext();
				PStcl cmd = getReference();
				PStcl target = cmd.getStencil(stclContext, CommandStcl.Slot.TARGET);

				// set distribution list in reading process
				target.setBoolean(stclContext, DistributionListStcl.Slot.IN_INSERTING_PROCESS, true);

				// reads the file
				BufferedReader in = new BufferedReader(new FileReader(ReadRecipientsFromFile.this._file));
				String line;
				int counter = 0;
				while ((line = in.readLine()) != null) {
					String add = getAddress(stclContext, line, cmd);
					if (StringUtils.isBlank(add))
						continue;
					target.newPStencil(stclContext, DistributionListStcl.Slot.TO, new Key(add), SQLRecipientStcl.class, add);
					target.setInt(stclContext, DistributionListStcl.Slot.INSERTING_COUNTER, ++counter);
				}

				// set distribution list no more in reading process
				target.setBoolean(stclContext, DistributionListStcl.Slot.IN_INSERTING_PROCESS, false);

				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

/*
 * package com.gdo.mail.cmd;
 * 
 * import java.io.BufferedReader; import java.io.FileNotFoundException; import
 * java.io.FileReader; import java.io.IOException;
 * 
 * import javax.mail.internet.AddressException; import
 * javax.mail.internet.InternetAddress;
 * 
 * import org.apache.commons.lang.StringUtils;
 * 
 * import com.gdo.mail.cmd.RenameFolder.Status; import
 * com.gdo.mail.model.RecipientStcl; import com.gdo.mail.model.SQLRecipientStcl;
 * import com.gdo.project.PStcl; import com.gdo.project.StclContext; import
 * com.gdo.project.model.AtomicActionStcl; import
 * com.gdo.stencils.cmd.CommandContext; import
 * com.gdo.stencils.cmd.CommandException; import
 * com.gdo.stencils.cmd.CommandStatus; import com.gdo.stencils.key.Key; import
 * com.gdo.stencils.plug.PSlot;
 * 
 * public class ReadRecipientsFromFile extends AtomicActionStcl {
 * 
 * public ReadRecipientsFromFile(StclContext stclContext) { super(stclContext);
 * }
 * 
 * @Override public CommandStatus<StclContext, PStcl>
 * doAction(CommandContext<StclContext, PStcl> cmdContext, PStcl self) throws
 * CommandException { StclContext stclContext = cmdContext.getStencilContext();
 * PStcl target = cmdContext.getTarget();
 * 
 * // clear to slot PSlot<StclContext, PStcl> to = target.getSlot(stclContext,
 * "To"); //to.getSlot().unplugAll(stclContext, to);
 * 
 * String file = getParameter(cmdContext, CommandContext.PARAM1, null); if
 * (StringUtils.isEmpty(file)) return error(this, Status.NO_NAME_DEFINED,
 * "no name defined to rename file");
 * 
 * // read all lines try { BufferedReader in = new BufferedReader(new
 * FileReader(file)); String line; while ((line = in.readLine()) != null) {
 * String[] adds = line.split("\t"); String add = adds[0]; InternetAddress a =
 * new InternetAddress(add); target.newPStencil(stclContext, "To", add,
 * RecipientStcl.class.getName(), a); } } catch (FileNotFoundException e) { //
 * TODO Auto-generated catch block e.printStackTrace(); } catch (IOException e)
 * { // TODO Auto-generated catch block e.printStackTrace(); } catch
 * (AddressException e) { // TODO Auto-generated catch block
 * e.printStackTrace(); } return success(this); } }
 */