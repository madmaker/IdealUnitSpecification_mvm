package ru.idealplm.specification.mvm.handlers;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;

import ru.idealplm.specification.mvm.handlers.linehandlers.MVMBlockLineHandler;
import ru.idealplm.utils.specification.BlockLine;
import ru.idealplm.utils.specification.BlockLineFactory;
import ru.idealplm.utils.specification.Error;
import ru.idealplm.utils.specification.Specification;
import ru.idealplm.utils.specification.Specification.BlockContentType;
import ru.idealplm.utils.specification.Specification.BlockType;

public class MVMBlockLineFactory extends BlockLineFactory{
	
	private final String[] blProps = new String[] { 
			"M9_Zone",
			"bl_sequence_no",
			"bl_quantity",
			"M9_Note",
			"M9_IsFromEAssembly", //у вхождений с одинаковым sequence_no должно быть одинаковое значение
			"M9_DisChangeFindNo", //у вхождений с одинаковым sequence_no должно быть одинаковое значение
			"m9_KITName",
			"bl_item_uom_tag",
			"M9_KITs"
	};

	@Override
	public BlockLine newBlockLine(TCComponentBOMLine bomLine) {
		try{
			TCComponent item = bomLine.getItem();
			TCComponentItemRevision itemIR = bomLine.getItemRevision();
			String uid = itemIR.getUid();
			String[] properties = bomLine.getProperties(blProps);
			boolean isDefault = properties[4].trim().isEmpty();
			
			//System.out.println("_processing by processor " + id + " *** *** " + bomLine.getItem().getType() + " --> " + Arrays.toString(properties));
			//TODO validateBOMLineAttributes(properties[1], properties[4], properties[5]);
			
			MVMBlockLineHandler blockLineHandler = new MVMBlockLineHandler();
			BlockLine resultBlockLine = new BlockLine(blockLineHandler);
			resultBlockLine.attributes.setZone(properties[0]);
			resultBlockLine.attributes.setPosition(properties[1]);
			resultBlockLine.isRenumerizable = properties[5].trim().equals("");
			resultBlockLine.uid = uid;
			
			if(item.getType().equals("M9_CompanyPart")){
				if(!properties[8].isEmpty()){
					Specification.errorList.addError(new Error("ERROR", "Объект с идентификатором " + item.getProperty("item_id") + " имеет ссылку на комплект."));
				}
				String typeOfPart = item.getProperty("m9_TypeOfPart");
				if(typeOfPart.equals("Сборочная единица") || typeOfPart.equals("Комплекс")){
					/*********************** Сборки и Комплексы ***********************/
					AIFComponentContext[] relatedDocs = bomLine.getItemRevision().getRelated("M9_DocRel");
					for(AIFComponentContext relatedDoc : relatedDocs){
						String docID = relatedDoc.getComponent().getProperty("item_id");
						if(docID.equals(bomLine.getItem().getProperty("item_id"))){
							String format = ((TCComponentItem)relatedDoc.getComponent()).getLatestItemRevision().getProperty("m9_Format");
							resultBlockLine.attributes.setFormat(format);
							break;
						}
					}
					resultBlockLine.attributes.setId(item.getProperty("item_id"));
					resultBlockLine.attributes.setName(itemIR.getProperty("object_name"));
					resultBlockLine.attributes.setQuantity(properties[2]);
					resultBlockLine.attributes.setRemark(properties[3]);
					resultBlockLine.addRefBOMLine(bomLine);
					if(typeOfPart.equals("Сборочная единица")){
						resultBlockLine.blockContentType = BlockContentType.ASSEMBLIES;
						//blockList.getBlock(BlockContentType.ASSEMBLIES, isDefault?"Default":"ME").addBlockLine(uid, resultBlockLine);
					} else {
						resultBlockLine.blockContentType = BlockContentType.COMPLEXES;
						//blockList.getBlock(BlockContentType.COMPLEXES, isDefault?"Default":"ME").addBlockLine(uid, resultBlockLine);
					}
					resultBlockLine.blockType = isDefault?BlockType.DEFAULT:BlockType.ME;
				} else if(typeOfPart.equals("Деталь")){
					/*****************************Детали*********************************/
					boolean hasDraft = false;
					AIFComponentContext[] relatedDocs = bomLine.getItemRevision().getRelated("M9_DocRel");
					for(AIFComponentContext relatedDoc : relatedDocs){
						String docID = relatedDoc.getComponent().getProperty("item_id");
						if(docID.equals(bomLine.getItem().getProperty("item_id"))){
							String format = ((TCComponentItem)relatedDoc.getComponent()).getLatestItemRevision().getProperty("m9_Format");
							resultBlockLine.attributes.setFormat(format);
							hasDraft = true;
							break;
						}
					}
					if(!hasDraft){
						if(itemIR.getProperty("m9_CADMaterial").equals("")){
							Specification.errorList.addError(new Error("ERROR", "У БЧ-детали с идентификатором " + item.getProperty("item_id") + " не заполнен атрибут \"Исходный материал\""));
						}
						resultBlockLine.attributes.setFormat("БЧ");
						resultBlockLine.attributes.setName(itemIR.getProperty("object_name") + "\n" + itemIR.getProperty("m9_CADMaterial") + " " + itemIR.getProperty("m9_AddNote"));
					} else {
						resultBlockLine.attributes.setName(itemIR.getProperty("object_name"));
					}
					resultBlockLine.attributes.setId(item.getProperty("item_id"));
					resultBlockLine.attributes.setQuantity(properties[2]);
					if(hasDraft){
						resultBlockLine.attributes.setRemark(properties[3]);
					} else {
						if(!itemIR.getProperty("m9_mass").trim().equals("")) {
							resultBlockLine.attributes.setRemark(itemIR.getProperty("m9_mass") + " кг"/* + properties[3]*/);
							resultBlockLine.attributes.getRemark().insert(properties[3]);
						} else {
							resultBlockLine.attributes.setRemark(properties[3]);
						}
					}
					resultBlockLine.addRefBOMLine(bomLine);
					AIFComponentContext[] relatedBlanks = bomLine.getItemRevision().getRelated("M9_StockRel");
					if(relatedBlanks.length>0){
						BlockLine blank = new BlockLine(blockLineHandler);
						TCComponentItem blankItem = (TCComponentItem)relatedBlanks[0].getComponent();
						blank.attributes.setPosition("-");
						blank.attributes.setName(blankItem.getLatestItemRevision().getProperty("object_name") + " " + "Изделие-заготовка для " + resultBlockLine.attributes.getId());
						if(!blankItem.getType().equals("CommercialPart")){
							relatedDocs = ((TCComponentItem)relatedBlanks[0].getComponent()).getLatestItemRevision().getRelated("M9_DocRel");
							for(AIFComponentContext relatedDoc : relatedDocs){
								String docID = relatedDoc.getComponent().getProperty("item_id");
								if(docID.equals(blankItem.getProperty("item_id"))){
									String format = ((TCComponentItem)relatedDoc.getComponent()).getLatestItemRevision().getProperty("m9_Format");
									System.out.println("FORMATFOR:"+format);
									blank.attributes.setFormat(format);
									break;
								}
							}
							blank.attributes.setId(relatedBlanks[0].getComponent().getProperty("item_id"));
						}
						resultBlockLine.getAttachedLines().add(blank);
					}
					resultBlockLine.blockContentType = BlockContentType.DETAILS;
					resultBlockLine.blockType = isDefault?BlockType.DEFAULT:BlockType.ME;
					//blockList.getBlock(BlockContentType.DETAILS, isDefault?"Default":"ME").addBlockLine(uid, resultBlockLine);
				} else if(typeOfPart.equals("Комплект")){
					/****************************Комплекты********************************/
					resultBlockLine.attributes.setId(item.getProperty("item_id"));
					resultBlockLine.attributes.setName(itemIR.getProperty("object_name"));
					resultBlockLine.attributes.setQuantity(properties[2]);
					resultBlockLine.addRefBOMLine(bomLine);
					resultBlockLine.blockContentType = BlockContentType.KITS;
					resultBlockLine.blockType = isDefault?BlockType.DEFAULT:BlockType.ME;
					//blockList.getBlock(BlockContentType.KITS, isDefault?"Default":"ME").addBlockLine(uid, resultBlockLine);
				} else if(typeOfPart.equals("")){
					Specification.errorList.addError(new Error("ERROR", "У вхождения с обозначением " + properties[2] + "отсутствует значение свойства \"Тип изделия\""));
				}
			} else if(item.getType().equals("CommercialPart")){
				/****************************Коммерческие********************************/
				String name = "";
				AIFComponentContext[] cod1CRels = itemIR.getItem().getRelated("M9_Cod1CRel");
				if(cod1CRels.length>0){
					TCComponentForm cod1CForm = (TCComponentForm) cod1CRels[0].getComponent();
					name = cod1CForm.getProperty("m9_FullName1C");
				}
				if(name.isEmpty()){
					name = "Наименование в 1С не согласовано";
					resultBlockLine.addProperty("bNameNotApproved", "true");
				}
				resultBlockLine.attributes.setName(name);
				resultBlockLine.attributes.setQuantity(properties[2]);
				resultBlockLine.attributes.setRemark(properties[3]);
				resultBlockLine.addRefBOMLine(bomLine);
				if(!properties[8].isEmpty()){
					resultBlockLine.attributes.createKits();
					resultBlockLine.attributes.addKit(properties[8], properties[6], properties[2].isEmpty()?1:Integer.parseInt(properties[2]));
				}
				if(item.getProperty("m9_TypeOfPart").equals("Прочее изделие")){
					resultBlockLine.blockContentType = BlockContentType.OTHERS;
					resultBlockLine.blockType = isDefault?BlockType.DEFAULT:BlockType.ME;
					//blockList.getBlock(BlockContentType.OTHERS, isDefault?"Default":"ME").addBlockLine(uid, resultBlockLine);
				} else {
					resultBlockLine.blockContentType = BlockContentType.STANDARDS;
					resultBlockLine.blockType = isDefault?BlockType.DEFAULT:BlockType.ME;
					//blockList.getBlock(BlockContentType.STANDARDS, isDefault?"Default":"ME").addBlockLine(uid, resultBlockLine);
				}
			} else if(item.getType().equals("M9_Material")){
				/****************************Материалы********************************/
				//resultBlockLine.attributes.setName(itemIR.getProperty("object_name"));
				String name = "";
				AIFComponentContext[] cod1CRels = itemIR.getItem().getRelated("M9_Cod1CRel");
				if(cod1CRels.length>0){
					TCComponentForm cod1CForm = (TCComponentForm) cod1CRels[0].getComponent();
					name = cod1CForm.getProperty("m9_FullName1C");
				}
				if(name.isEmpty()){
					name = "Наименование в 1С не согласовано";
					resultBlockLine.addProperty("bNameNotApproved", "true");
				}
				resultBlockLine.attributes.setName(name);
				resultBlockLine.attributes.setQuantity(properties[2]);
				resultBlockLine.addRefBOMLine(bomLine);
				resultBlockLine.addProperty("UOM", properties[7]);
				if(!properties[8].isEmpty()){
					resultBlockLine.attributes.createKits();
					resultBlockLine.attributes.addKit(properties[8], properties[6], properties[2].isEmpty()?1:Double.parseDouble(properties[2]));
				}
				resultBlockLine.blockContentType = BlockContentType.MATERIALS;
				resultBlockLine.blockType = isDefault?BlockType.DEFAULT:BlockType.ME;
			} else if(item.getType().equals("M9_GeomOfMat")){
				/*************************Геометрии материалов****************************/
				AIFComponentContext[] materialBOMLines = bomLine.getChildren();
				if(materialBOMLines.length>0){
					if(materialBOMLines.length>1){
						Specification.errorList.addError(new Error("ERROR", "В составе геометрии материала с идентификатором " + item.getProperty("item_id") + " присутствует более одного материала."));
					}
					TCComponentItemRevision materialIR = ((TCComponentBOMLine) materialBOMLines[0].getComponent()).getItemRevision();
					String quantityMS = ((TCComponentBOMLine) materialBOMLines[0].getComponent()).getProperty("bl_quantity");
					float quantityMD = Float.parseFloat(quantityMS.equals("")?"1":quantityMS);
					int quantotyGD = Integer.parseInt(properties[2].equals("")?"1":properties[2]);
					String uom = ((TCComponentBOMLine) materialBOMLines[0].getComponent()).getProperty("bl_item_uom_tag");
					uom = uom.equals("*")?"":uom;
					
					String name = "";
					AIFComponentContext[] cod1CRels = materialIR.getItem().getRelated("M9_Cod1CRel");
					if(cod1CRels.length>0){
						TCComponentForm cod1CForm = (TCComponentForm) cod1CRels[0].getComponent();
						name = cod1CForm.getProperty("m9_FullName1C");
					}
					if(name.isEmpty()){
						name = "Наименование в 1С не согласовано";
						resultBlockLine.addProperty("bNameNotApproved", "true");
					}
					resultBlockLine.attributes.setName(name);
					//resultBlockLine.attributes.setName(materialIR.getItem().getProperty("object_name"));
					resultBlockLine.attributes.setQuantity(String.valueOf(quantityMD*quantotyGD));
					resultBlockLine.attributes.setRemark(uom);
					resultBlockLine.addRefBOMLine(bomLine);
					resultBlockLine.uid = materialIR.getUid();
					resultBlockLine.blockContentType = BlockContentType.MATERIALS;
					resultBlockLine.blockType = isDefault?BlockType.DEFAULT:BlockType.ME;
				} else {
					Specification.errorList.addError(new Error("ERROR", "В составе геометрии материала с идентификатором " + item.getProperty("item_id") + " отсутствует материал."));
				}
			}
			return resultBlockLine;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

}
