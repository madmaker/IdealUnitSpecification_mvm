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
		ArrayList<String> acceptableTypesOfPart = new ArrayList<String>(Arrays.asList("Сборочная единица", "Complex"));
		
		TCComponentBOMLine topBOMLine = specification.getTopBOMLine();
		
		if(topBOMLine==null){
			errorList.addError(new Error("ERROR", "Отсутствует состав для построения спецификации"));
			return false;
		}
		
		try{
			TCComponentItem item = topBOMLine.getItem();
			if(!"M9_CompanyPart".equals(item.getType())){
				errorList.addError(new Error("ERROR", "Недопустимый вид изделия!"));
				return false;
			} else if(!acceptableTypesOfPart.contains(item.getProperty("m9_TypeOfPart"))){
				errorList.addError(new Error("ERROR", "Недопустимый тип изделия!"));
				return false;
			}
		}catch(Exception ex){
			ex.printStackTrace();
			return false;
		}
		
		return true;
		
	}

}
