package ru.idealplm.specification.mvm.methods;

import java.util.ArrayList;
import java.util.Arrays;

import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;

import ru.idealplm.utils.specification.Error;
import ru.idealplm.utils.specification.ErrorList;
import ru.idealplm.utils.specification.Specification;
import ru.idealplm.utils.specification.methods.ValidateMethod;

public class MVMValidateMethod implements ValidateMethod{

	@Override
	public boolean validate(Specification specification, ErrorList errorList) {
		System.out.println("...METHOD... ValidateMethod");
		ArrayList<String> acceptableTypesOfPart = new ArrayList<String>(Arrays.asList("��������� �������", "Complex"));
		
		TCComponentBOMLine topBOMLine = specification.getTopBOMLine();
		
		if(topBOMLine==null){
			System.out.println("Top is null");
			errorList.addError(new Error("ERROR", "����������� ������ ��� ���������� ������������"));
			return false;
		}
		
		try{
			TCComponentItem item = topBOMLine.getItem();
			if(!"M9_CompanyPart".equals(item.getType())){
				System.out.println("Wrong object type");
				errorList.addError(new Error("ERROR", "������������ ��� �������!"));
				return false;
			} else if(!acceptableTypesOfPart.contains(item.getProperty("m9_TypeOfPart"))){
				System.out.println("Wrong type of part");
				errorList.addError(new Error("ERROR", "������������ ��� �������!"));
				return false;
			}
		}catch(Exception ex){
			ex.printStackTrace();
			return false;
		}
		
		return true;
		
	}

}
