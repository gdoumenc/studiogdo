package com.gdo.site.model;

import com.gdo.stencils.StclContext;

public class SimplePageStcl extends _PageStcl {

	public interface Command extends _PageStcl.Command {
		String UPDATE = "Update";
	}

	public SimplePageStcl(StclContext stclContext) {
		super(stclContext);
	}

}
