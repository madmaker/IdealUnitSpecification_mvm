package ru.idealplm.specification.mvm.comparators;

import java.util.Comparator;

import ru.idealplm.utils.specification.BlockLine;
import ru.idealplm.utils.specification.Specification;

public class DefaultComparator implements Comparator<BlockLine> {
	
	Specification.FormField sortField;
	public DefaultComparator(Specification.FormField sortField){
		this.sortField = sortField;
	}
	
	@Override
	public int compare(BlockLine line0, BlockLine line1) {
		
		String field0 = line0.attributes.getStringValueFromField(sortField);
		String field1 = line1.attributes.getStringValueFromField(sortField);
		
		int result = 0;

		int len, arg0len, arg1len;

		arg0len = field0.length();
		arg1len = field1.length();

		if (arg0len == arg1len)
			len = arg0len;
		if (arg0len > arg1len)
			len = arg1len;
		else
			len = arg0len;

		int i;
		boolean isEq = true;
		for (i = 0; (i < len) && (isEq); i++)
			if (field0.charAt(i) != field1.charAt(i))
				isEq = false;

		if (i == field0.length() && isEq)
			return -1;
		if (i == field1.length() && isEq)
			return 1;

		i--;
		boolean isArg0Digit = Character.isDigit(field0.charAt(i));
		boolean isArg1Digit = Character.isDigit(field1.charAt(i));

		if (isArg0Digit && isArg1Digit && i > 0) {
			while (((Character.isDigit(field0.charAt(i))) || (Character.isDigit(field1.charAt(i)))) && i>0)
				i--;
			i++;

			if (Integer.valueOf(getCompString(field0, i)) < Integer.valueOf(getCompString(field1, i)))
				result = -1;
			else
				result = 1;
		} else {
			if (field0.charAt(i) < field1.charAt(i))
				result = -1;
			else
				result = 1;
		}

		return result;
	}

	private String getCompString(String arg0, int i) {
		boolean isNumericArg0 = Character.isDigit(arg0.charAt(i));
		boolean controlState = isNumericArg0;
		String cStringArg0 = "";

		for (int j = i; (isNumericArg0 == controlState) && (j < arg0.length()); j++) {
			if ((Character.isDigit(arg0.charAt(j)) && isNumericArg0) || (!Character.isDigit(arg0.charAt(j)) && !isNumericArg0))
				cStringArg0 = cStringArg0 + arg0.charAt(j);
			else
				controlState = !controlState;
		}

		return cStringArg0;
	}
}
