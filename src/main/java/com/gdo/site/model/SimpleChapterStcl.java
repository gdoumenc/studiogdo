package com.gdo.site.model;

import com.gdo.project.util.model.NamedStcl;
import com.gdo.stencils.StclContext;

public class SimpleChapterStcl extends NamedStcl {
	public interface Slot extends NamedStcl.Slot {
		String CONTENT = "Content";
		String X_INHA_PLUGINS = "XinhaPlugins";
		String X_INHA_SCRIPT = "XinhaScript";
	}
	

	public SimpleChapterStcl(StclContext stclContext) {
		super(stclContext);
		
		propSlot(Slot.CONTENT, "");
		propSlot(Slot.X_INHA_PLUGINS, "");
		propSlot(Slot.X_INHA_SCRIPT, "c.fullPage=true;");
	}

}
