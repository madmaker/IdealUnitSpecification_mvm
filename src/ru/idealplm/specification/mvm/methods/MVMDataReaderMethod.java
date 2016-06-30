package ru.idealplm.specification.mvm.methods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.omg.CORBA.OMGVMCID;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.RelatedSubstituteComp;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;

import ru.idealplm.specification.mvm.handlers.linehandlers.MVMBlockLineHandler;
import ru.idealplm.specification.mvm.util.PerfTrack;
import ru.idealplm.utils.specification.Block;
import ru.idealplm.utils.specification.BlockLine;
import ru.idealplm.utils.specification.BlockLineHandler;
import ru.idealplm.utils.specification.BlockList;
import ru.idealplm.utils.specification.Error;
import ru.idealplm.utils.specification.ErrorList;
import ru.idealplm.utils.specification.Specification;
import ru.idealplm.utils.specification.Specification.BlockContentType;
import ru.idealplm.utils.specification.Specification.FormField;
import ru.idealplm.utils.specification.methods.DataReaderMethod;
import ru.idealplm.utils.specification.util.LineUtil;

public class MVMDataReaderMethod implements DataReaderMethod{
	
	private Specification specification;
	private BlockList blockList;
	private BlockingQueue<AIFComponentContext> bomQueue;
	private ArrayList<String> bl_sequence_noList = new ArrayList<String>();
	private ArrayList<String> m9_IsFromEAsmList = new ArrayList<String>();
	private ArrayList<String> m9_DisableChangeFindNoList = new ArrayList<String>();
	private ArrayList<String> docTypesShort = new ArrayList<String>();
	private ArrayList<String> docTypesLong = new ArrayList<String>();
	private ArrayList<String> docKitTypesShort = new ArrayList<String>();
	private ArrayList<String> docKitTypesLong = new ArrayList<String>();
	private HashMap<String, BlockLine> materialUIDs = new HashMap<String, BlockLine>();
	
	boolean atLeastOnePosIsFixed = false;
	boolean atLeastOnePosIsNotFixed = false;
	
	private class MVMBOMLineProcessor implements Runnable{

		private int id;
		public MVMBOMLineProcessor(int pid) {
			id = pid;
		}
		
		private final String[] blProps = new String[] { 
				"m9_Zone",
				"bl_sequence_no",
				"bl_quantity",
				"M9_Note",
				"m9_IsFromEAsm", //у вхождений с одинаковым sequence_no должно быть одинаковое значение
				"m9_DisableChangeFindNo", //у вхождений с одинаковым sequence_no должно быть одинаковое значение
				"m9_m9_KITName",
				"bl_item_uom_tag"
		};
		
		public BlockLine parseLine(TCComponentBOMLine bomLine){
			try{
				TCComponent item = bomLine.getItem();
				TCComponentItemRevision itemIR = bomLine.getItemRevision();
				String[] properties = bomLine.getProperties(blProps);
				boolean isDefault = properties[4].trim().isEmpty();
				
				if(properties[5].trim().equals("")){
					atLeastOnePosIsNotFixed = true;
				} else {
					atLeastOnePosIsFixed = true;
				}
				
				System.out.println("_processing by processor " + id + " *** *** " + bomLine.getItem().getType() + " --> " + Arrays.toString(properties));
				validateBOMLineAttributess(properties[0], properties[5], properties[6]);
				
				MVMBlockLineHandler blockLineHandler = new MVMBlockLineHandler();
				BlockLine resultBlockLine = new BlockLine(blockLineHandler);
				resultBlockLine.setZone(properties[0]);
				resultBlockLine.setPosition(properties[1]);
				
				if(item.getType().equals("M9_CompanyPart")){
					String typeOfPart = item.getProperty("m9_TypeOfPart");
					if(typeOfPart.equals("Сборочная единица") || typeOfPart.equals("Комплекс")){
						/*********************** Сборки и Комплексы ***********************/
						if(typeOfPart.equals("Сборочная единица")){								
							blockList.getBlock(BlockContentType.ASSEMBLIES, isDefault?"Default":"ME").addBlockLine(resultBlockLine);
						} else {
							blockList.getBlock(BlockContentType.COMPLEXES, isDefault?"Default":"ME").addBlockLine(resultBlockLine);
						}
						AIFComponentContext[] relatedDocs = bomLine.getItemRevision().getRelated("M9_DocRel");
						for(AIFComponentContext relatedDoc : relatedDocs){
							String docID = relatedDoc.getComponent().getProperty("item_id");
							if(docID.substring(0, docID.lastIndexOf(" ")).equals(bomLine.getItem().getProperty("item_id"))){
								String format = ((TCComponentItem)relatedDoc.getComponent()).getLatestItemRevision().getProperty("m9_Format");
								resultBlockLine.setFormat(format);
								break;
							}
						}
						resultBlockLine.setId(item.getProperty("item_id"));
						resultBlockLine.setName(itemIR.getProperty("object_name"));
						resultBlockLine.setQuantity(properties[2]);
						resultBlockLine.setRemark(properties[3]);
						resultBlockLine.addRefBOMLine(bomLine);
						//resultBlockLine.build();
					} else if(typeOfPart.equals("Деталь")){
						/*****************************Детали*********************************/
						boolean hasDraft = false;
						blockList.getBlock(BlockContentType.DETAILS, isDefault?"Default":"ME").addBlockLine(resultBlockLine);
						AIFComponentContext[] relatedDocs = bomLine.getItemRevision().getRelated("M9_DocRel");
						for(AIFComponentContext relatedDoc : relatedDocs){
							String docID = relatedDoc.getComponent().getProperty("item_id");
							if(docID.substring(0, docID.lastIndexOf(" ")).equals(bomLine.getItem().getProperty("item_id"))){
								String format = ((TCComponentItem)relatedDoc.getComponent()).getLatestItemRevision().getProperty("m9_Format");
								System.out.println("+++ got a format = "+format);
								resultBlockLine.setFormat(format);
								hasDraft = true;
								break;
							}
						}
						if(!hasDraft){
							resultBlockLine.setFormat("БЧ");
							resultBlockLine.setName(itemIR.getProperty("object_name") + "\n" + itemIR.getProperty("m9_CADMaterial") + " " + itemIR.getProperty("m9_AddNote"));
						} else {
							resultBlockLine.setName(itemIR.getProperty("object_name"));
						}
						resultBlockLine.setId(item.getProperty("item_id"));
						resultBlockLine.setQuantity(properties[2]);
						if(hasDraft){
							resultBlockLine.setRemark(properties[3]);
						} else {
							if(!itemIR.getProperty("m9_mass").trim().equals("")) {
								resultBlockLine.setRemark(itemIR.getProperty("m9_mass") + " кг\n" + properties[3]);
							} else {
								resultBlockLine.setRemark(properties[3]);
							}
						}
						resultBlockLine.addRefBOMLine(bomLine);
						//resultBlockLine.build();
					} else if(typeOfPart.equals("Комплект")){
						/****************************Комплекты********************************/
						resultBlockLine.setId(item.getProperty("item_id"));
						resultBlockLine.setName(itemIR.getProperty("object_name"));
						resultBlockLine.setQuantity(properties[2]);
						blockList.getBlock(BlockContentType.KITS, isDefault?"Default":"ME").addBlockLine(resultBlockLine);
						resultBlockLine.addRefBOMLine(bomLine);
						//resultBlockLine.build();
					} else if(typeOfPart.equals("")){
						specification.getErrorList().addError(new Error("ERROR", "У вхождения с обозначением " + properties[2] + "отсутствует значение свойства \"Тип изделия\""));
					}
				} else if(item.getType().equals("CommercialPart")){
					/****************************Коммерческие********************************/
					resultBlockLine.setName(itemIR.getProperty("object_name"));
					resultBlockLine.setQuantity(properties[2]);
					if(item.getProperty("m9_TypeOfPart").equals("Other")){
						blockList.getBlock(BlockContentType.OTHERS, isDefault?"Default":"ME").addBlockLine(resultBlockLine);
					} else {
						blockList.getBlock(BlockContentType.STANDARDS, isDefault?"Default":"ME").addBlockLine(resultBlockLine);
					}
					resultBlockLine.addRefBOMLine(bomLine);
					//resultBlockLine.build();
				} else if(item.getType().equals("M9_Material")){
					/****************************Материалы********************************/
					if(materialUIDs.containsKey(itemIR.getUid())){
						resultBlockLine = materialUIDs.get(itemIR.getUid());
						resultBlockLine.addQuantity(properties[2]);
					} else {
						materialUIDs.put(itemIR.getUid(), resultBlockLine);
						resultBlockLine.setName(itemIR.getProperty("object_name"));
						resultBlockLine.setQuantity(properties[2]);
						resultBlockLine.addRefBOMLine(bomLine);
						resultBlockLine.addProperty("UOM", properties[7]);
						//resultBlockLine.build();
						blockList.getBlock(BlockContentType.MATERIALS , isDefault?"Default":"ME").addBlockLine(resultBlockLine);
					}
				} else if(item.getType().equals("M9_GeomOfMat")){
					/*************************Геометрии материалов****************************/
					AIFComponentContext[] materialBOMLines = bomLine.getChildren();
					if(materialBOMLines.length>0){
						TCComponentItemRevision materialIR = ((TCComponentBOMLine) materialBOMLines[0].getComponent()).getItemRevision();
						if(materialUIDs.containsKey(materialIR.getUid())){
							String quantityMS = ((TCComponentBOMLine) materialBOMLines[0].getComponent()).getProperty("bl_quantity");
							double quantityMD = Double.parseDouble(quantityMS.equals("")?"1":quantityMS);
							double quantotyGD = Double.parseDouble(properties[2].equals("")?"1":properties[2]);
							resultBlockLine = materialUIDs.get(materialIR.getUid());
							resultBlockLine.addQuantity(String.valueOf(quantityMD*quantotyGD));
						} else {
							String quantityMS = ((TCComponentBOMLine) materialBOMLines[0].getComponent()).getProperty("bl_quantity");
							double quantityMD = Double.parseDouble(quantityMS.equals("")?"1":quantityMS);
							double quantotyGD = Double.parseDouble(properties[2].equals("")?"1":properties[2]);
							String uom = ((TCComponentBOMLine) materialBOMLines[0].getComponent()).getProperty("bl_item_uom_tag");
							uom = uom.equals("*")?"":uom;
							materialUIDs.put(materialIR.getUid(), resultBlockLine);
							resultBlockLine.setName(materialIR.getItem().getProperty("object_name"));
							resultBlockLine.setQuantity(String.valueOf(quantityMD*quantotyGD));
							resultBlockLine.addRefBOMLine(bomLine);
							resultBlockLine.setRemark(uom);
							//resultBlockLine.build();
							blockList.getBlock(BlockContentType.MATERIALS , isDefault?"Default":"ME").addBlockLine(resultBlockLine);
						}
					} else {
						specification.getErrorList().addError(new Error("ERROR", "В составе геометрии материала с идентификатором " + item.getProperty("item_id") + " отсутствует материал."));
					}
				}
				return resultBlockLine;
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}
		
		@Override
		public void run() {
			TCComponentBOMLine bomLine;
			while(!bomQueue.isEmpty()){
				try {
					bomLine = (TCComponentBOMLine) bomQueue.take().getComponent();
					if(bomLine.hasSubstitutes()) System.out.println("+++++++++++ HAS REPLACEMENTS ++++++++++++");
					BlockLine line = parseLine(bomLine);
					for(TCComponentBOMLine comp : bomLine.listSubstitutes()){
						System.out.println("REPLACEMENT=" + comp.getItem().getProperty("object_name") + " OF TYPE="+comp.getType());
						BlockLine substituteLine = parseLine(comp);
						substituteLine.setIsSubstitute(true);
						substituteLine.addRefBOMLine(bomLine);
						substituteLine.setPosition(line.getPosition()+"*");
						substituteLine.build();
						line.addSubstituteBOMLine(substituteLine);
					}
					line.build();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	@Override
	public void readBOMData(Specification specification) {
		try{
			this.specification = specification;
			loadDocumentTypes();
			blockList = specification.getBlockList();
			
			PerfTrack.prepare("Getting BOM");
			TCComponentBOMLine topBOMLine = specification.getTopBOMLine();
			AIFComponentContext[] childBOMLines = topBOMLine.getChildren();
		
			bomQueue = new ArrayBlockingQueue<AIFComponentContext>(childBOMLines.length);
			bomQueue.addAll(Arrays.asList(childBOMLines));
			PerfTrack.addToLog("Getting BOM");
			
			readSpecifiedItemData(topBOMLine);
			readTopIRDocuments(topBOMLine);
			
			ExecutorService service = Executors.newFixedThreadPool(/*Runtime.getRuntime().availableProcessors()*/2);
			for(int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
				service.submit(new MVMBOMLineProcessor(i));
			}
			
			service.shutdown();
			service.awaitTermination(3, TimeUnit.MINUTES);
			while(!service.isTerminated()){
				Thread.sleep(100);
			}
			
			BlockList tempList = new BlockList(specification);
			for(int i = 0; i < blockList.size(); i++){
				if(blockList.get(i).size()!=0) {
					tempList.addBlock(blockList.get(i));
				}
			}
			specification.setBlockList(tempList);
			
			System.out.println("SIZE: "+blockList.size());
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	private synchronized void validateBOMLineAttributess(String bl_sequence_no, String m9_IsFromEAsm, String m9_DisableChangeFindNo){
		int posInList = bl_sequence_noList.indexOf(bl_sequence_no);
		if(posInList==-1){
			bl_sequence_noList.add(bl_sequence_no);
			m9_IsFromEAsmList.add(m9_IsFromEAsm);
			m9_DisableChangeFindNoList.add(m9_DisableChangeFindNo);
			if(!m9_IsFromEAsm.isEmpty()) specification.addStringProperty("HasMEBlocks", "true");
		} else {
			if(!m9_IsFromEAsmList.get(posInList).equals(m9_IsFromEAsm)){
				this.specification.getErrorList().addError(new Error("ERROR", "У вхождений с номером позиции "+bl_sequence_no+"разные значения свойства \"Позиция из МЭ\""));
			} else if (!m9_DisableChangeFindNoList.get(posInList).equals(m9_DisableChangeFindNo)){
				this.specification.getErrorList().addError(new Error("ERROR", "У вхождений с номером позиции "+bl_sequence_no+"разные значения свойства \"Запрет смены позиции\""));
			}
		}
	}
	
	private void readTopIRDocuments(TCComponentBOMLine bomLine){
		try {
			
			TCComponentItemRevision topIR = bomLine.getItemRevision();
			TCComponent[] documents = topIR.getRelatedComponents("M9_DocRel");
			TCComponentItemRevision documentIR;
			String IRid = topIR.getItem().getProperty("item_id");
			String format;
			String id;
			String name;
			String remark;
			String object_name;
			String shortType;
			boolean isBaseDoc;
			boolean gostNameIsFalse;
			MVMBlockLineHandler blockLineHandler = new MVMBlockLineHandler();
			
			for(TCComponent document : documents){
				documentIR = ((TCComponentItem)document).getLatestItemRevision();
				name = "";
				/*if(documentIR.getProperty("m9_Format").length() > Specification.columnLengths.get(Specification.FormField.FORMAT)-1){					
					format = "*)";
					remark = "*) " + documentIR.getProperty("m9_Format");
				} else {
				}*/
				
				format = documentIR.getProperty("m9_Format");
				id = document.getProperty("item_id");
				object_name = documentIR.getProperty("object_name"); 
				shortType = getType(id);
				if(id.equals(IRid)){
					System.out.println("---- Found a SPEC!");
					specification.setSpecificationItemRevision(documentIR);
					specification.addStringProperty("LITERA1", documentIR.getProperty("m9_Litera1"));
					specification.addStringProperty("LITERA2", documentIR.getProperty("m9_Litera2"));
					specification.addStringProperty("LITERA3", documentIR.getProperty("m9_Litera3"));
					specification.addStringProperty("PERVPRIM", documentIR.getItem().getProperty("m9_PrimaryApp"));
					continue;
				}
				if(shortType!=null){
					gostNameIsFalse = documentIR.getProperty("m9_GOSTName").equalsIgnoreCase("нет");
					isBaseDoc = id.substring(0, id.lastIndexOf(" ")).equals(IRid);
					System.out.println(object_name + " --- base=" + isBaseDoc + " --- isFalse="+gostNameIsFalse);
					//name = (!gostName || !isBaseDoc) ? object_name : docTypesLong.get(docTypesShort.indexOf(shortType));
					if(gostNameIsFalse || !isBaseDoc){
						name += object_name;
					}
					if(!gostNameIsFalse){
						name += "\n" + docTypesLong.get(docTypesShort.indexOf(shortType));
					}
					
					BlockLine resultBlockLine = new BlockLine(blockLineHandler);
					resultBlockLine.setFormat(format);
					resultBlockLine.setId(id);
					resultBlockLine.setName(name);
					resultBlockLine.setQuantity("-1");
					resultBlockLine.addProperty("Type", shortType);
					resultBlockLine.build();
					blockList.getBlock(BlockContentType.DOCS, "Default").addBlockLine(resultBlockLine);
				} else if(shortType==null){
					shortType = getKitType(id);
					if(shortType!=null){
						gostNameIsFalse = documentIR.getProperty("m9_GOSTName").equalsIgnoreCase("нет");
						isBaseDoc = id.substring(0, id.lastIndexOf(" ")).equals(IRid);
						//name = (!gostName || !isBaseDoc) ? object_name : docKitTypesLong.get(docKitTypesShort.indexOf(shortType));
						name += object_name;
						if(!gostNameIsFalse){
							name += "\n" + docKitTypesLong.get(docKitTypesShort.indexOf(shortType));
						}
						
						BlockLine resultBlockLine = new BlockLine(blockLineHandler);
						resultBlockLine.setFormat(format);
						resultBlockLine.setId(id);
						resultBlockLine.setName(name);
						resultBlockLine.setQuantity("-1");
						resultBlockLine.addProperty("Type", shortType);
						resultBlockLine.build();
						blockList.getBlock(BlockContentType.KITS, "Default").addBlockLine(resultBlockLine);
					}
				}
				
				System.out.println("[Processing document] type:\"" + shortType + "\" ID:\"" + id + "\" name: \"" + name + "\"");
				
				if(shortType!=null){
					if(shortType.equals("МЭ")){
						if(specification.getStringProperty("MEDocumentId")!=null) {
							specification.getErrorList().addError(new Error("ERROR", "Определено более одного документа МЭ"));
						} else {
							specification.addStringProperty("MEDocumentId", id);
						}
					}
				} else {
					specification.getErrorList().addError(new Error("ERROR", "Не определен тип для документа: " + id));
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		
	}
	
	private void readSpecifiedItemData(TCComponentBOMLine bomLine){
		try{
			specification.addStringProperty("Note", bomLine.getItemRevision().getProperty("m9_AddNote").trim().equals("")?null:bomLine.getItemRevision().getProperty("m9_AddNote").trim());
			specification.addStringProperty("OBOZNACH", bomLine.getItem().getProperty("item_id"));
			specification.addStringProperty("NAIMEN", bomLine.getItemRevision().getProperty("object_name"));
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	private void loadDocumentTypes(){
		String[] docTypes = Specification.preferenceService.getStringArray(Specification.preferenceService.TC_preference_site, "M9_Spec_DocumentTypesPriority");
		for(String docType : docTypes){
			int posOfFirstSpace = docType.indexOf(" ");
			if(posOfFirstSpace!=-1){
				docTypesShort.add(docType.substring(0, posOfFirstSpace));
				docTypesLong.add(docType.substring(posOfFirstSpace + 1, docType.length()));
				System.out.println("\"" + docTypesShort.get(docTypesShort.size()-1) + "\" \"" + docTypesLong.get(docTypesShort.size()-1) + "\"");
			}
		}
		String[] docKitTypes = Specification.preferenceService.getStringArray(Specification.preferenceService.TC_preference_site, "M9_Spec_DocumentComplexTypesPriority");
		for(String docKitType : docKitTypes){
			int posOfFirstSpace = docKitType.indexOf(" ");
			if(posOfFirstSpace!=-1){
				docKitTypesShort.add(docKitType.substring(0, posOfFirstSpace));
				docKitTypesLong.add(docKitType.substring(posOfFirstSpace + 1, docKitType.length()));
				System.out.println("\"" + docKitTypesShort.get(docKitTypesShort.size()-1) + "\" \"" + docKitTypesLong.get(docKitTypesShort.size()-1) + "\"");
			}
		}
	}
	
	private String getType(String input){
		String result = null;
		String symbolPart = input.replaceAll("[^А-Яа-я]+", "");
		for(String type : docTypesShort){
			if(type.equals(input) && type.length()==input.length()){
				result = type;
				break;
			} else if(type.equals(symbolPart) && type.length()!=input.length()){
				result = type;
			} else if(type.equals(symbolPart) && type.length()==input.length()){
				result = type;
				break;
			}
		}
		return result;
	}
	
	private String getKitType(String input){
		String result = null;
		String symbolPart = input.replaceAll("[^А-Яа-я]+", "");
		for(String type : docKitTypesShort){
			if(type.equals(input) && type.length()==input.length()){
				result = type;
				break;
			} else if(type.equals(symbolPart) && type.length()!=input.length()){
				result = type;
			} else if(type.equals(symbolPart) && type.length()==input.length()){
				result = type;
				break;
			}
		}
		return result;
	}
	
}
