package com.gdo.site.model;

import com.gdo.stencils.StclContext;

public class DeliveryPageChapterStcl extends SimpleChapterStcl {
	public interface Slot extends SimpleChapterStcl.Slot {
		String TITLE = "Title";
		String PRIORITY = "Priority";
		String VERSION = "Version";
		String MISSING = "Missing";
		String COMMENT = "Comment";
		String INTERNAL_COMMENT = "InternalComment";
		String LINK = "Link";
		String DELIVERED = "Delivered";
		String ACCEPTED = "Accepted";
		
		String RESOURCE_MGR = "ResourcesMgr";
	}

	public DeliveryPageChapterStcl(StclContext stclContext) {
		super(stclContext);
		
		propSlot(Slot.TITLE);
		propSlot(Slot.PRIORITY);
		propSlot(Slot.VERSION);
		propSlot(Slot.MISSING);
		propSlot(Slot.COMMENT);
		propSlot(Slot.INTERNAL_COMMENT);
		propSlot(Slot.LINK);
		propSlot(Slot.DELIVERED, "false");
		propSlot(Slot.ACCEPTED, "false");
		
		singleSlot(Slot.RESOURCE_MGR);
	}

}
