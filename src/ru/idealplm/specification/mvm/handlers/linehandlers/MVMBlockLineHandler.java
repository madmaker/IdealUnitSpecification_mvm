package ru.idealplm.specification.mvm.handlers.linehandlers;

import ru.idealplm.utils.specification.BlockLine;
import ru.idealplm.utils.specification.BlockLineHandler;
import ru.idealplm.utils.specification.Specification;
import ru.idealplm.utils.specification.Specification.FormField;
import ru.idealplm.utils.specification.util.LineUtil;

public class MVMBlockLineHandler implements BlockLineHandler{

	@Override
	public synchronized void prepareBlockLine(BlockLine bomLine) {
		if(bomLine.attributes.getFormat()==null){
			bomLine.attributes.setFormat("");
		}
		if(bomLine.attributes.getZone()==null){
			bomLine.attributes.setZone("");
		}
		if(bomLine.attributes.getZone().exceedsLimit){
			bomLine.attributes.getRemark().insertAt(0,bomLine.attributes.getZone().toString());
			bomLine.attributes.setZone("*)");
		}
		if(bomLine.attributes.getFormat().exceedsLimit){
			bomLine.attributes.getRemark().insertAt(0,bomLine.attributes.getFormat().toString());
			bomLine.attributes.setFormat("*)");
		}
		if(bomLine.attributes.getKits()!=null){
			bomLine.attributes.getRemark().insert(bomLine.attributes.getKits().getKits());
		}
		if(bomLine.getSubstituteBlockLines()!=null){
			bomLine.attributes.getRemark().insertAt(0, "Осн.");
		} else if(bomLine.isSubstitute){
			bomLine.attributes.getRemark().insertAt(0, "*Допуск. зам.");
		}
		if(bomLine.getProperty("UOM")!=null && !bomLine.getProperty("UOM").equals("*")){
			bomLine.attributes.getRemark().insertAt(0, bomLine.getProperty("UOM"));
		}
		bomLine.attributes.getRemark().build();
		if(bomLine.attributes.getRemark().size() > bomLine.lineHeight) bomLine.lineHeight = bomLine.attributes.getRemark().size();
	}

}
