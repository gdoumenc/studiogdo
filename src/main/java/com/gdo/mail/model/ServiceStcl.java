/**
 * Copyright GDO - 2004
 */
package com.gdo.mail.model;

import com.gdo.project.cmd.CreateAtomic;
import com.gdo.stencils.StclContext;

public class ServiceStcl extends com.gdo.project.model.ServiceStcl {

	public interface Slot extends com.gdo.project.model.ServiceStcl.Slot {
		String MAIL_CONTEXT = "MailContext";

		String OPERATIONS = "Operations";
		String GENERATORS = "Generators";

		String DISTRIBUTION_LISTS = "DistributionLists";

		String MODELS = "Models";
	}
	
	public interface Command extends com.gdo.project.model.ServiceStcl.Command {
		String ADD_OPERATION = "AddOperation";
		String ADD_FILE_OPERATION = "AddFileOperation";
		String ADD_SQL_OPERATION = "AddSqlOperation";
		String ADD_DISTRIBUTION_LIST = "AddDistributionList";
		String ADD_DISTRIBUTION_LIST_FROM_FILE = "AddDistributionListFromFile";
	}

	public ServiceStcl(StclContext stclContext) {
		super(stclContext);
		
		multiSlot(Slot.MAIL_CONTEXT);
		multiSlot(Slot.OPERATIONS);
		multiSlot(Slot.GENERATORS);
		multiSlot(Slot.DISTRIBUTION_LISTS);
		multiSlot(Slot.MODELS);
		
		command(Command.ADD_OPERATION, CreateAtomic.class, OperationStcl.class.getName(), "Target/Operations", "int", "1");
		command(Command.ADD_FILE_OPERATION, CreateAtomic.class, "com.gdo.mail.model.FileOperationStcl", "Target/Operations", "int", "1");
		command(Command.ADD_SQL_OPERATION, CreateAtomic.class, SQLOperationStcl.class.getName(), "Target/Operations", "int", "1");
		command(Command.ADD_DISTRIBUTION_LIST, CreateAtomic.class, DistributionListStcl.class.getName(), "Target/DistributionLists", "int", "1");
		command(Command.ADD_DISTRIBUTION_LIST_FROM_FILE, CreateAtomic.class, "com.gdo.mail.model.DistributionListFromFileStcl", "Target/DistributionLists", "int", "1");
	}

}