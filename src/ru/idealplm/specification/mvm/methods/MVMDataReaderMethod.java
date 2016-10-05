package ru.idealplm.specification.mvm.methods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.services.impl.TCOperationService;

import ru.idealplm.specification.mvm.handlers.MVMBlockLineFactory;
import ru.idealplm.specification.mvm.handlers.linehandlers.MVMBlockLineHandler;
import ru.idealplm.specification.mvm.util.PerfTrack;
import ru.idealplm.utils.specification.BlockLine;
import ru.idealplm.utils.specification.BlockList;
import ru.idealplm.utils.specification.Error;
import ru.idealplm.utils.specification.Specification;
import ru.idealplm.utils.specification.Specification.BlockContentType;
import ru.idealplm.utils.specification.Specification.BlockType;
import ru.idealplm.utils.specification.Specification.FormField;
import ru.idealplm.utils.specification.methods.DataReaderMethod;
import ru.idealplm.utils.specification.util.GeneralUtils;

public class MVMDataReaderMethod implements DataReaderMethod{
	
	private Specification specification = Specification.getInstance();
	private BlockList blockList;
	private BlockingQueue<AIFComponentContext> bomQueue;
	private ArrayList<String> bl_sequence_noList;
	private HashMap<String, Boolean> m9_IsFromEAsmList;
	private HashMap<String, Boolean> m9_DisableChangeFindNoList;
	private ArrayList<String> docTypesShort;
	private ArrayList<String> docTypesLong;
	private ArrayList<String> docKitTypesShort;
	private ArrayList<String> docKitTypesLong;
	private HashMap<String, BlockLine> materialUIDs;
	private HashMap<String, BlockLine> uids;
	private HashMap<String, BlockLine> uidsSubstitute;
	
	public MVMDataReaderMethod() {
		bl_sequence_noList = new ArrayList<String>();
		m9_IsFromEAsmList = new HashMap<String, Boolean>();
		m9_DisableChangeFindNoList = new HashMap<String, Boolean>();
		docTypesShort = new ArrayList<String>();
		docTypesLong = new ArrayList<String>();
		docKitTypesShort = new ArrayList<String>();
		docKitTypesLong = new ArrayList<String>();
		materialUIDs = new HashMap<String, BlockLine>();
		uids = new HashMap<String, BlockLine>();
		uidsSubstitute = new HashMap<String, BlockLine>();
	}
	
	boolean atLeastOnePosIsFixed = false;
	boolean atLeastOneME = false;
	boolean hasPrevRev = false;
	
	private class MVMBOMLineProcessor implements Runnable{

		private int id;
		public MVMBOMLineProcessor(int pid) {
			id = pid;
		}
		
		@Override
		public void run() {
			TCComponentBOMLine bomLine;
			MVMBlockLineFactory blFactory = new MVMBlockLineFactory();
			while(!bomQueue.isEmpty()){
				try {
					bomLine = (TCComponentBOMLine) bomQueue.take().getComponent();
					BlockLine line = blFactory.newBlockLine(bomLine);
					line.isSubstitute = false;
					uids.put(line.uid, line);
					for(TCComponentBOMLine comp : bomLine.listSubstitutes()){
						BlockLine substituteLine = blFactory.newBlockLine(comp);
						substituteLine.attributes.setPosition(line.attributes.getPosition()+"*");
						substituteLine.attributes.setQuantity("-1");
						substituteLine.isSubstitute = true;
						line.addSubstituteBlockLine(substituteLine);
						blockList.getBlock(substituteLine.blockContentType, substituteLine.blockType).addBlockLine(substituteLine.uid, substituteLine); //Possible fix for substitute lines
						uidsSubstitute.put(substituteLine.uid, substituteLine);
					}
					if(line.blockType == BlockType.ME) atLeastOneME = true;
					if(!line.isRenumerizable) {
						atLeastOnePosIsFixed = true;
						System.out.println("NOTRENUM:"+line.attributes.getId());
					}
					validateBOMLineAttributess(line);
					if(line.blockContentType == BlockContentType.MATERIALS){
						if(materialUIDs.containsKey(line.uid)){
							BlockLine storedLine = materialUIDs.get(line.uid);
							if(storedLine.attributes.getPosition().isEmpty()){ // Update line position if it was empty (and hope for the best)
								storedLine.attributes.setPosition(line.attributes.getPosition());
							}
							storedLine.attributes.createKits();
							storedLine.attributes.addKit(line.attributes.getKits());
							storedLine.addRefBOMLine(line.getRefBOMLines().get(0));
							storedLine.attributes.addQuantity(line.attributes.getStringValueFromField(FormField.QUANTITY));
						} else {
							materialUIDs.put(line.uid, line);
							blockList.getBlock(line.blockContentType, line.blockType).addBlockLine(line.uid, line);
						}
					} else {
						blockList.getBlock(line.blockContentType, line.blockType).addBlockLine(line.uid, line);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	@Override
	public void readBOMData() {
		try{
			loadDocumentTypes();
			blockList = specification.getBlockList();
			
			PerfTrack.prepare("Getting BOM");
			TCComponentBOMLine topBOMLine = specification.getTopBOMLine();
			
			TCComponentItem topItem = topBOMLine.getItem();
			TCComponentItemRevision topItemR = topBOMLine.getItemRevision();
			TCComponent[] revisions = topItem.getRelatedComponents("revision_list");
			for(int i = 0; i < revisions.length; i++){
				if(revisions[i].getUid().equals(topItemR.getUid()) && i>0){
					hasPrevRev = true;
					break;
				}
			}
			
			PerfTrack.prepare("Getting BOM unpacked");
			AIFComponentContext[] childBOMLines = topBOMLine.getChildren();
			
			for (AIFComponentContext currBOMLine : childBOMLines) {
				TCComponentBOMLine bl = (TCComponentBOMLine) currBOMLine.getComponent();
				if (bl.isPacked()) {
					bl.unpack();
					bl.refresh();
				}
			}
			topBOMLine.refresh();
			
			childBOMLines = topBOMLine.getChildren();
			PerfTrack.addToLog("Getting BOM unpacked");
		
			readSpecifiedItemData(topBOMLine);
			readTopIRDocuments(topBOMLine);
			readGeneralNoteForm();

			if(childBOMLines.length>0){
				bomQueue = new ArrayBlockingQueue<AIFComponentContext>(childBOMLines.length);
				bomQueue.addAll(Arrays.asList(childBOMLines));
				PerfTrack.addToLog("Getting BOM");
				/*ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
				for(int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
					service.submit(new MVMBOMLineProcessor(i));
				}
				
				service.shutdown();
				service.awaitTermination(3, TimeUnit.MINUTES);
				while(!service.isTerminated()){
					Thread.sleep(100);
				}*/
				MVMBOMLineProcessor bomLineProcessor = new MVMBOMLineProcessor(0);
				bomLineProcessor.run();
			}
			
			for (AIFComponentContext currBOMLineContext : childBOMLines)
				((TCComponentBOMLine) currBOMLineContext.getComponent()).pack();
			
			BlockList tempList = new BlockList();
			for(int i = 0; i < blockList.size(); i++){
				if(blockList.get(i).size()!=0) {
					tempList.addBlock(blockList.get(i));
					for(BlockLine line:blockList.get(i).getListOfLines()){
						line.build();
					}
				}
			}
			specification.setBlockList(tempList);
			if(tempList.size()==0){
				specification.getErrorList().addError(new Error("ERROR", "Отсутствуют разделы спецификации."));
			}
			if(atLeastOneME && Specification.settings.getStringProperty("MEDocumentId")==null){
				specification.getErrorList().addError(new Error("ERROR", "Отсутствует документ МЭ."));
			}
			for(Entry<String,BlockLine> entry:uidsSubstitute.entrySet()){
				if(uids.containsKey(entry.getKey()))	{
					specification.getErrorList().addError(new Error("ERROR", "Объект с идентификатором " + entry.getValue().attributes.getId() + " присутствует в составе и заменах одновременно."));
				}
			}
			
			Specification.settings.addBooleanProperty("canRenumerize", !atLeastOnePosIsFixed);
			Specification.settings.addBooleanProperty("canUseReservePos", atLeastOnePosIsFixed && hasPrevRev);
			Specification.settings.addBooleanProperty("canReadLastRevPos", !atLeastOnePosIsFixed && hasPrevRev);
			
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}

	
	private synchronized void validateBOMLineAttributess(BlockLine line){
		if(line.blockType==BlockType.ME) atLeastOneME = true;
		if(atLeastOneME) Specification.settings.addBooleanProperty("hasMEBlocks", true);
		String bl_sequence_no = line.attributes.getPosition();
		int posInList = bl_sequence_noList.indexOf(bl_sequence_no);
		if(posInList==-1){
			bl_sequence_noList.add(bl_sequence_no);
			m9_IsFromEAsmList.put(bl_sequence_no, line.blockType!=BlockType.ME);
			m9_DisableChangeFindNoList.put(bl_sequence_no, line.isRenumerizable);
		} else {
			if(m9_IsFromEAsmList.get(bl_sequence_no)!=(line.blockType!=BlockType.ME)){
				this.specification.getErrorList().addError(new Error("ERROR", "У вхождений с номером позиции "+bl_sequence_no+"разные значения свойства \"Позиция из МЭ\""));
			} else if (m9_DisableChangeFindNoList.get(bl_sequence_no)!=line.isRenumerizable){
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
			String uid;
			String format;
			String id;
			String name;
			String object_name;
			String shortType;
			boolean isBaseDoc = true;
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
				uid = documentIR.getUid();
				format = documentIR.getProperty("m9_Format");
				id = document.getProperty("item_id");
				object_name = documentIR.getProperty("object_name"); 
				shortType = getType(id);
				if(id.equals(IRid)){
					specification.setSpecificationItemRevision(documentIR);
					Specification.settings.addStringProperty("LITERA1", documentIR.getProperty("m9_Litera1"));
					Specification.settings.addStringProperty("LITERA2", documentIR.getProperty("m9_Litera2"));
					Specification.settings.addStringProperty("LITERA3", documentIR.getProperty("m9_Litera3"));
					Specification.settings.addStringProperty("PERVPRIM", documentIR.getItem().getProperty("m9_PrimaryApp"));
					AIFComponentContext[] basedocs = documentIR.getItem().getRelated("m9_DocumentBaseRel");
					if(basedocs.length>0){
						Specification.settings.addStringProperty("BASEDOC", basedocs[0].getComponent().getProperty("object_name"));						
					}
					try{
						for (AIFComponentContext compContext : documentIR.getChildren()){
							if ((compContext.getComponent() instanceof TCComponentDataset) 
									&& compContext.getComponent().getProperty("object_desc").equals("Спецификация")) {
								if(((TCComponent)compContext.getComponent()).isCheckedOut()){
									specification.getErrorList().addError(new Error("ERROR", "Набор данных заблокирован."));
								}
							}
	
						}
					} catch(Exception ex) {
						ex.printStackTrace();
					}
					continue;
				}
				if(shortType!=null){
					gostNameIsFalse = documentIR.getProperty("m9_GOSTName").equalsIgnoreCase("нет");
					isBaseDoc = id.substring(0, id.lastIndexOf(" ")).equals(IRid);
					//name = (!gostName || !isBaseDoc) ? object_name : docTypesLong.get(docTypesShort.indexOf(shortType));
					if(gostNameIsFalse || !isBaseDoc){
						name += object_name;
					}
					if(!gostNameIsFalse){
						name += "\n" + docTypesLong.get(docTypesShort.indexOf(shortType));
					}
					
					BlockLine resultBlockLine = new BlockLine(blockLineHandler);
					resultBlockLine.attributes.setFormat(format);
					resultBlockLine.attributes.setId(id);
					resultBlockLine.attributes.setName(name);
					resultBlockLine.attributes.setQuantity("-1");
					resultBlockLine.addProperty("Type", shortType);
					resultBlockLine.build();
					blockList.getBlock(BlockContentType.DOCS, BlockType.DEFAULT).addBlockLine(uid, resultBlockLine);
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
						resultBlockLine.attributes.setFormat(format);
						resultBlockLine.attributes.setId(id);
						resultBlockLine.attributes.setName(name);
						resultBlockLine.attributes.setQuantity("-1");
						resultBlockLine.addProperty("Type", shortType);
						resultBlockLine.build();
						blockList.getBlock(BlockContentType.KITS, BlockType.DEFAULT).addBlockLine(uid, resultBlockLine);
					}
				}
				
				if(shortType!=null){
					if(shortType.equals("МЭ")){
						if(Specification.settings.getStringProperty("MEDocumentId")!=null) {
							specification.getErrorList().addError(new Error("ERROR", "Определено более одного документа МЭ."));
						} else {
							Specification.settings.addStringProperty("MEDocumentId", id);
						}
					}
				} else {
					specification.getErrorList().addError(new Error("ERROR", "Не определен тип для документа: " + id));
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void readGeneralNoteForm(){
		try{
			TCComponentItemRevision specIR = specification.getSpecificationItemRevision();
			TCComponent tempComp;
			if(specIR!=null){
				if((tempComp = specIR.getRelatedComponent("M9_SignRel"))!=null){
					Specification.settings.addStringProperty("Designer", tempComp.getProperty("m9_Designer"));
					Specification.settings.addStringProperty("Check", tempComp.getProperty("m9_Check"));
					Specification.settings.addStringProperty("AddCheckPost", tempComp.getProperty("m9_AddCheckPost"));
					Specification.settings.addStringProperty("AddCheck", tempComp.getProperty("m9_AddCheck"));
					Specification.settings.addStringProperty("NCheck", tempComp.getProperty("m9_NCheck"));
					Specification.settings.addStringProperty("Approver", tempComp.getProperty("m9_Approver"));
					//TODO in case of return of date
					/*String designerDate = GeneralUtils.parseDateFromTC(tempComp.getProperty("m9_DesignDate"));
					System.out.println(":DATE1:"+tempComp.getProperty("m9_DesignDate"));
					System.out.println(":DATE2:"+designerDate);
					Specification.settings.addStringProperty("DesignDate", designerDate);*/
				}
				if(specIR.getRelatedComponent("IMAN_master_form_rev")!=null){
					Specification.settings.addStringProperty("blockSettings", specIR.getRelatedComponent("IMAN_master_form_rev").getProperty("object_desc"));
				}
			}
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	private void readSpecifiedItemData(TCComponentBOMLine bomLine){
		try{
			Specification.settings.addStringProperty("AddedText", bomLine.getItemRevision().getProperty("m9_AddNote").trim().equals("")?null:bomLine.getItemRevision().getProperty("m9_AddNote").trim());
			Specification.settings.addStringProperty("OBOZNACH", bomLine.getItem().getProperty("item_id"));
			Specification.settings.addStringProperty("NAIMEN", bomLine.getItemRevision().getProperty("object_name"));
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
			}
		}
		String[] docKitTypes = Specification.preferenceService.getStringArray(Specification.preferenceService.TC_preference_site, "M9_Spec_DocumentComplexTypesPriority");
		for(String docKitType : docKitTypes){
			int posOfFirstSpace = docKitType.indexOf(" ");
			if(posOfFirstSpace!=-1){
				docKitTypesShort.add(docKitType.substring(0, posOfFirstSpace));
				docKitTypesLong.add(docKitType.substring(posOfFirstSpace + 1, docKitType.length()));
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
