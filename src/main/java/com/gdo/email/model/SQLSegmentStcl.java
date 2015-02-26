package com.gdo.email.model;

import com.gdo.stencils.StclContext;

public class SQLSegmentStcl extends com.gdo.mail.model.SQLSegmentStcl {

	public interface Slot extends com.gdo.mail.model.SQLSegmentStcl.Slot {
		String TITLE = "Title";
		String CONTENT = "Content";

		String FROM_TABLE = "FromTable";

		String READ = "Read";
	}

	public SQLSegmentStcl(StclContext stclContext) {
		super(stclContext);
	}

}
