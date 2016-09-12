package ru.idealplm.specification.mvm.comparators;

import java.util.Comparator;

import ru.idealplm.utils.specification.BlockLine;
import ru.idealplm.utils.specification.Specification.FormField;

public class DetailComparator implements Comparator<BlockLine> {

	@Override
	public int compare(BlockLine bl0, BlockLine bl1) {
		
		String format0 = bl0.attributes.getStringValueFromField(FormField.FORMAT);
		String format1 = bl1.attributes.getStringValueFromField(FormField.FORMAT);
		if(format0.equals("ав") && !format1.equals("")){
			return 1;
		}  else if (!format0.equals("ав") && format1.equals("ав")){
			return -1;
		}
		
		DefaultComparator dc = new DefaultComparator(FormField.NAME);
		
		return dc.compare(bl0, bl1);
	}

}
