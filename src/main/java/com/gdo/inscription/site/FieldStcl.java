/**
 * Copyright GDO - 2005
 */
package com.gdo.inscription.site;

import com.gdo.project.util.model.NamedStcl;
import com.gdo.stencils.StclContext;

public class FieldStcl extends NamedStcl {

	public interface Slot extends NamedStcl.Slot {
		String LABEL = "Label";
		String TYPE = "Type";
		String ERROR_TIP = "ErrorTip";
		String TOOL_TIP = "ToolTip";
		String DB_FIELD = "DBField";
		String INPUT = "Input";
	}

	public FieldStcl(StclContext stclContext) {
		super(stclContext);
	}

}
