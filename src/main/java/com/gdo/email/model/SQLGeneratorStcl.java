/**
 * Copyright GDO - 2004
 */
package com.gdo.email.model;

import com.gdo.generator.model.GeneratorStcl;
import com.gdo.mail.model.SegmentStcl;
import com.gdo.stencils.StclContext;

public class SQLGeneratorStcl extends GeneratorStcl {

	public interface Slot extends SegmentStcl.Slot {
		String TRACKER_PATTERN = "TrackerPattern";
		String TRACKER_REPLACEMENT = "TrackerReplacement";
	}

	public SQLGeneratorStcl(StclContext stclContext) {
		super(stclContext);
	}
}