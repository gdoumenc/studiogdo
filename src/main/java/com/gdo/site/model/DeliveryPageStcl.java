package com.gdo.site.model;

import com.gdo.project.cmd.CreateAtomic;
import com.gdo.stencils.StclContext;

public class DeliveryPageStcl extends StructuredPageStcl {
	public interface Slot extends StructuredPageStcl.Slot {
		String RESOURCES_MGR = "ResourcesMgr";
	}
	
	public interface Command extends StructuredPageStcl.Command {
		String ADD_DELIVERY_CHAPTER = "AddDeliveryChapter";
	}

	public DeliveryPageStcl(StclContext stclContext) {
		super(stclContext);
		
		singleSlot(Slot.RESOURCES_MGR);
		multiSlot(Slot.CHAPTERS);
		
		command(Command.ADD_DELIVERY_CHAPTER, CreateAtomic.class, DeliveryPageChapterStcl.class.getName(), "Target/Chapters", "int", "1");
	}

}
