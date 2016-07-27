package ru.idealplm.specification.mvm.comparators;

import java.util.ArrayList;
import java.util.Comparator;

import ru.idealplm.utils.specification.BlockLine;
import ru.idealplm.utils.specification.Specification;
import ru.idealplm.utils.specification.Specification.FormField;

public class KitComparator implements Comparator<BlockLine>{
	
	private ArrayList<String> docTypesPriority = new ArrayList<String>();
	
	public KitComparator(){
		loadDocumentTypes();
	}

	@Override
	public int compare(BlockLine bl0, BlockLine bl1) {
		
		if(bl0.getRefBOMLines() == null && bl1.getRefBOMLines() != null){
			return -1;
		} else if(bl0.getRefBOMLines() != null && bl1.getRefBOMLines() == null){
			return 1;
		} else if (bl0.getRefBOMLines() != null && bl1.getRefBOMLines() != null){
			DefaultComparator dc = new DefaultComparator(FormField.ID);
			int result = dc.compare(bl0, bl1);
			if(result == 0){
				dc = new DefaultComparator(FormField.NAME);
				return dc.compare(bl0, bl1);
			} else {
				return result;
			}
		}
		
		boolean bl0_is_base = Specification.settings.getStringProperty("OBOZNACH").equals(bl0.attributes.getId().substring(0, bl0.attributes.getId().lastIndexOf(" ")));
		boolean bl1_is_base = Specification.settings.getStringProperty("OBOZNACH").equals(bl1.attributes.getId().substring(0, bl1.attributes.getId().lastIndexOf(" ")));
	
		if(bl0_is_base && bl1_is_base){
			return (docTypesPriority.indexOf(bl0.getProperty("Type")) < docTypesPriority.indexOf(bl1.getProperty("Type"))) ? -1 : 1;
		} else if (bl0_is_base && !bl1_is_base){
			return -1;
		} else if (!bl0_is_base && bl1_is_base) {
			return 1;
		} else {
			return compareIDs(bl0, bl1);
		}
	}
	
	private void loadDocumentTypes(){
		String[] docTypes = Specification.preferenceService.getStringArray(Specification.preferenceService.TC_preference_site, "M9_Spec_DocumentTypesPriority");
		for(String docType : docTypes){
			int posOfFirstSpace = docType.indexOf(" ");
			if(posOfFirstSpace!=-1){
				docTypesPriority.add(docType.substring(0, posOfFirstSpace));
			}
		}
	}
	
	private int compareIDs(BlockLine bl0, BlockLine bl1) {
		
		String field0 = bl0.attributes.getId().substring(0, bl0.attributes.getId().lastIndexOf(" "));
		String field1 = bl1.attributes.getId().substring(0, bl1.attributes.getId().lastIndexOf(" "));
		
		int result = 0;
		
		if(!field0.equals(field1)){

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
		
		} else {
			result = (docTypesPriority.indexOf(bl0.getProperty("Type")) < docTypesPriority.indexOf(bl1.getProperty("Type"))) ? -1 : 1;
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
