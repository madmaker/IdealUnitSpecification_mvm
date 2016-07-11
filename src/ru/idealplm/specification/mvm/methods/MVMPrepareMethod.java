package ru.idealplm.specification.mvm.methods;

import java.util.Collections;

import com.teamcenter.rac.kernel.TCException;

import ru.idealplm.specification.mvm.comparators.PositionComparator;
import ru.idealplm.utils.specification.Block;
import ru.idealplm.utils.specification.BlockLine;
import ru.idealplm.utils.specification.Specification;
import ru.idealplm.utils.specification.methods.PrepareMethod;

public class MVMPrepareMethod implements PrepareMethod{
	
	int firstPos = 1;

	@Override
	public void prepareBlocks(Specification specification) {
		System.out.println("...METHOD...  PrepareMethod");
		for(Block block:specification.getBlockList()) {
			if(!block.isRenumerizable()) continue;
			block.setFirstPosNo(firstPos);
			firstPos = firstPos + block.getReservePosNum() + block.getRenumerizableLinesCount() + (block.getRenumerizableLinesCount()-1)*block.getIntervalPosNum();
		}
		for(Block block:specification.getBlockList()) block.run();
		if(Specification.settings.getBooleanProperty("doRenumerize")){
			for(Block block:specification.getBlockList()) {
				String currentPos = String.valueOf(block.getFirstPosNo());
				if(!block.isRenumerizable()) continue;
				for(BlockLine bl:block.getListOfLines()){
					if(!bl.isSubstitute()){
						try {
							bl.renumerize(String.valueOf(currentPos));
						} catch (TCException e) {
							e.printStackTrace();
						}
						currentPos = String.valueOf(Integer.parseInt(currentPos) + block.getIntervalPosNum() + 1);
					}
				}
				Collections.sort(block.getListOfLines(), new PositionComparator());
			}
		}
		//specification.getBlockList().getBlock(BlockContentType.DOCS, "Default").run();
	}

}
