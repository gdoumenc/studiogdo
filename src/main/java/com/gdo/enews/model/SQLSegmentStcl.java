package com.gdo.enews.model;

import com.gdo.stencils.StclContext;

public class SQLSegmentStcl extends com.gdo.mail.model.SQLSegmentStcl {

	public interface Slot extends com.gdo.mail.model.SQLSegmentStcl.Slot {
		String READ = "Read";
	}

	public SQLSegmentStcl(StclContext stclContext) {
		super(stclContext);
	}

}
