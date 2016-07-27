package ru.idealplm.specification.mvm.comparators;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.idealplm.utils.specification.BlockLine;

public class PositionComparator implements Comparator<BlockLine> {

	@Override
	public int compare(BlockLine bl0, BlockLine bl1) {
		Pattern pattern = Pattern.compile("[0-9]+");
		Matcher matcher0 = pattern.matcher(bl0.attributes.getPosition());
		Matcher matcher1 = pattern.matcher(bl1.attributes.getPosition());
		boolean hasAsterisk0 = bl0.attributes.getPosition().endsWith("*");
		boolean hasAsterisk1 = bl1.attributes.getPosition().endsWith("*");
 		int result0_i;
		int result1_i;
		try{
			result0_i = Integer.parseInt(matcher0.find()?matcher0.group():"-1");
		} catch (NumberFormatException ex) {
			return -1;
		}
		try{
			result1_i = Integer.parseInt(matcher1.find()?matcher1.group():"-1");
		} catch (NumberFormatException ex) {
			return 1;
		}
		if(result0_i < result1_i){
			return -1;
		} else if (result0_i > result1_i) {
			return 1;
		} else {
			if(hasAsterisk0 && !hasAsterisk1){
				return 1;
			} else if (!hasAsterisk0 && hasAsterisk1){
				return -1;
			} else {
				return 0;
			}
		}
	}

}
