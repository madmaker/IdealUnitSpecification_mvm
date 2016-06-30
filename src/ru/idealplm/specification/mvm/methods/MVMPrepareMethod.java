package ru.idealplm.specification.mvm.methods;

import ru.idealplm.utils.specification.Block;
import ru.idealplm.utils.specification.Specification;
import ru.idealplm.utils.specification.Specification.BlockContentType;
import ru.idealplm.utils.specification.methods.PrepareMethod;

public class MVMPrepareMethod implements PrepareMethod{

	@Override
	public void prepareBlocks(Specification specification) {
		System.out.println("...METHOD...  PrepareMethod");
		for(Block block:specification.getBlockList()) block.run();
		//specification.getBlockList().getBlock(BlockContentType.DOCS, "Default").run();
	}

}
