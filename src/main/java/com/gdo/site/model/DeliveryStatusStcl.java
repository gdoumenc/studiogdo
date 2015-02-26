package com.gdo.site.model;

import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;

public class DeliveryStatusStcl extends Stcl {
	public interface Slot extends Stcl.Slot {
		String TITLE = "Title";
		String PRIORITY = "Priority";
		String VERSION = "Version";
		String COMMENT = "Comment";
		
		String DELIVERED = "Delivered";
		String ACCEPTED = "Accepted";
		
		String DELIVERED_DATE = "DeliveredDate";
		String ACCEPTED_DATE = "AcceptedDate";
		
		String LINK = "Link";
		String RESOURCES_MGR = "ResourcesMgr";
	}

	public DeliveryStatusStcl(StclContext stclContext) {
		super(stclContext);
		
		/* general page informations */
		propSlot(Slot.TITLE);
		propSlot(Slot.PRIORITY);
		propSlot(Slot.VERSION);
		propSlot(Slot.COMMENT);
		
		/* delivery status */
		propSlot(Slot.DELIVERED, "false");
		propSlot(Slot.ACCEPTED, "false");
		
		propSlot(Slot.DELIVERED_DATE);
		propSlot(Slot.ACCEPTED_DATE);
		
		/* complementary informations */
		propSlot(Slot.LINK); // URL to be able to see the page to deliver.
		singleSlot(Slot.RESOURCES_MGR); //Resource manager to manage resources associated to the page delivery process.
	}

}
