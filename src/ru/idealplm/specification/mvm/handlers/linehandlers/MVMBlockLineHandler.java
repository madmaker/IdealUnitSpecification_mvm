package ru.idealplm.specification.mvm.handlers.linehandlers;

import ru.idealplm.utils.specification.BlockLine;
import ru.idealplm.utils.specification.BlockLineHandler;
import ru.idealplm.utils.specification.Specification;
import ru.idealplm.utils.specification.Specification.FormField;
import ru.idealplm.utils.specification.util.LineUtil;

public class MVMBlockLineHandler implements BlockLineHandler{

	@Override
	public synchronized void prepareBlockLine(BlockLine bomLine) {
		System.out.println("PREPARING:"+bomLine.getId());
		if(bomLine.getFormat()==null){
			bomLine.setFormat("");
		}
		if(bomLine.getZone()==null){
			bomLine.setZone("");
		}
		if(bomLine.getZone().exceedsLimit){
			//bomLine.getRemark().insertAt(0, LineUtil.getFittedLines(bomLine.getZone().toString(),Specification.columnLengths.get(FormField.REMARK)));
			bomLine.getRemark().insertAt(0,bomLine.getZone().toString());
			bomLine.setZone("*)");
		}
		if(bomLine.getFormat().exceedsLimit){
			System.out.println("EXCEEDS");
			//bomLine.getRemark().insertAt(0, LineUtil.getFittedLines(bomLine.getFormat().toString(),Specification.columnLengths.get(FormField.REMARK)));
			bomLine.getRemark().insertAt(0,bomLine.getFormat().toString());
			bomLine.setFormat("*)");
		}
		if(bomLine.getKits()!=null){
			bomLine.getRemark().insert(bomLine.getKits().getKits());
		}
		if(bomLine.getSubstituteBOMLines()!=null){
			bomLine.getRemark().insertAt(0, "Осн.");
		} else if(bomLine.isSubstitute()){
			System.out.println("BUILDING WITH QUANTITYFORS:"+bomLine.getQuantity());
			bomLine.getRemark().insertAt(0, "*Допуск. зам.");
		}
		if(bomLine.getProperty("UOM")!=null && !bomLine.getProperty("UOM").equals("*")){
			bomLine.getRemark().insertAt(0, bomLine.getProperty("UOM"));
		}
		bomLine.getRemark().build();
		if(bomLine.getRemark().size() > bomLine.getLineHeight()) bomLine.setLineHeight(bomLine.getRemark().size());
		System.out.println("REMARK="+bomLine.getRemark().toString());
	}

}
