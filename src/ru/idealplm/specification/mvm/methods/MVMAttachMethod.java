package ru.idealplm.specification.mvm.methods;

import java.awt.Desktop;
import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2006_03.DataManagement.CreateItemsOutput;
import com.teamcenter.services.rac.core._2006_03.DataManagement.CreateItemsResponse;
import com.teamcenter.services.rac.core._2006_03.DataManagement.GenerateItemIdsAndInitialRevisionIdsProperties;
import com.teamcenter.services.rac.core._2006_03.DataManagement.GenerateItemIdsAndInitialRevisionIdsResponse;
import com.teamcenter.services.rac.core._2006_03.DataManagement.ItemIdsAndInitialRevisionIds;
import com.teamcenter.services.rac.core._2006_03.DataManagement.ItemProperties;
import com.teamcenter.services.rac.core._2006_03.DataManagement.ReviseProperties;
import com.teamcenter.services.rac.core._2008_06.DataManagement.ReviseInfo;
import com.teamcenter.services.rac.core._2008_06.DataManagement.ReviseOutput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.ReviseResponse2;

import ru.idealplm.utils.specification.Specification;
import ru.idealplm.utils.specification.methods.AttachMethod;

public class MVMAttachMethod implements AttachMethod{
	
	private Specification specification;
	private TCComponentBOMLine topBOMLine;
	private TCComponentItemRevision topIR;
	private TCComponentItemRevision specIR;
	private File reportFile;
	
	public TCSession session;
	public DataManagementService dmService;

	@Override
	public void putInTeamcenter(Specification specification) {
		try{
			this.specification = specification;
			this.topBOMLine = specification.getTopBOMLine();
			this.topIR = topBOMLine.getItemRevision();
			this.reportFile = specification.getReportFile();
			this.session = Specification.session;
			this.dmService = DataManagementService.getService(session);
			this.specIR = specification.getSpecificationItemRevision();
			System.out.println("...METHOD...  AttachMethod");
			if(specIR != null) {
				System.out.println("+++++++++++  SPREV!=NULL");
				deletePrevSpecDatasetOfKd();
			} else if (specIR == null) {
				System.out.println("+++++++++++  SPREV==NULL");
				TCComponentItem kdDoc = findKDDocItem();
				if (kdDoc == null) {
					System.out.println("CREATING KD ITEM WITH FIRST ITEMREVISION + SignForm!");
					TCComponentItemRevision newItemRev = (TCComponentItemRevision)createItem("M9_KD", topIR.getProperty("item_id"),
							topIR.getProperty("object_name"),
							"Создано утилитой по генерации документа \"Спецификация\"")[1];
					specIR = newItemRev;
				} else {
					System.out.println("+++++++++++  KD!=NULL");
					System.out.println(kdDoc.getProperty("item_id"));
					if (isKdLastRevHasAssemblyRev(kdDoc)) {
						System.out.println("REVISE AND REMOVE SP + SignForm...");
						specIR = createNextRevisionBasedOn(getLastRevOfItem(kdDoc));
						
						if (specIR != null) {
							deleteRelationsToCompanyPart(specIR);
							deletePrevSpecDatasetOfKd();
						}
					} else {
						System.out.println("REPLACING LAST REVISION!");
						specIR = kdDoc.getLatestItemRevision();
						deletePrevSpecDatasetOfKd();
					}
				}
				
				if(specIR!=null){
					specIR.setProperty("m9_Format", "A4");
					specIR.lock();
					specIR.save();
					specIR.unlock();
					topBOMLine.getItemRevision().add("M9_DocRel", new TCComponent[]{specIR.getItem()});
				}
				//spRev.getItem().("M9_DocRel", topBOMLine.getItemRevision());
				//spRev.setProperty("pm8_Format", finalFormat(page));
			}
	
			TCComponentDataset ds_new = createDatasetAndAddFile(specification.getReportFile().getAbsolutePath());
			if (ds_new != null) {
				System.out.println("Adding to item_id: " + specIR.getProperty("item_id"));
				TCComponent tempComp;
				if((tempComp = specIR.getRelatedComponent("M9_SignRel"))!=null){
					System.out.println("+++++FOUND SIGN FORM!!!!");
					tempComp.setProperty("m9_Designer", Specification.settings.getStringProperty("Designer"));
					tempComp.setProperty("m9_Check", Specification.settings.getStringProperty("Check"));
					tempComp.setProperty("m9_AddCheckPost", Specification.settings.getStringProperty("AddCheckPost"));
					tempComp.setProperty("m9_AddCheck", Specification.settings.getStringProperty("AddCheck"));
					tempComp.setProperty("m9_NCheck", Specification.settings.getStringProperty("NCheck"));
					tempComp.setProperty("m9_Approver", Specification.settings.getStringProperty("Approver"));
				}
				if(specIR.getRelatedComponent("IMAN_master_form_rev")!=null){
					specIR.getRelatedComponent("IMAN_master_form_rev").setProperty("object_desc", Specification.settings.getStringProperty("blockSettings"));
					/*Specification.settings.addStringProperty("blockSettings", specIR.getRelatedComponent("IMAN_master_form_rev").getProperty("object_desc"));*/
				}
				specIR.add("IMAN_specification", ds_new);
				specIR.lock();
				topBOMLine.getItemRevision().setProperty("m9_AddNote", Specification.settings.getStringProperty("AddedText"));
				//topBOMLine.getItemRevision().setProperty("m9_AddNote", Specification.settings.getStringProperty("blockSettings"));
				specIR.setProperty("m9_Litera1", specification.settings.getStringProperty("LITERA1"));
				specIR.setProperty("m9_Litera2", specification.settings.getStringProperty("LITERA2"));
				specIR.setProperty("m9_Litera3", specification.settings.getStringProperty("LITERA3"));
				specIR.getItem().setProperty("m9_PrimaryApp", specification.settings.getStringProperty("PERVPRIM"));
				specIR.save();
				specIR.unlock();
				
				Desktop.getDesktop().open(ds_new.getFiles("")[0]);
			}
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	private TCComponentItemRevision createNextRevisionBasedOn(TCComponentItemRevision itemRev) {
		TCComponentItemRevision out = null;
		
		ReviseProperties revProp = new ReviseProperties();
		ReviseInfo revInfo = new ReviseInfo();
		revInfo.baseItemRevision = itemRev;
		ReviseResponse2 response = dmService.revise2(new ReviseInfo[] {revInfo});
		
		System.out.println("MAP SIZE = " + response.reviseOutputMap.size());
		Iterator it = response.reviseOutputMap.entrySet().iterator();
		if (it.hasNext()) {
			System.out.println("trying to return itemRev...");
			Map.Entry entry = (Entry) it.next();
			System.out.println("Class NAME VALUE: " + entry.getValue().getClass().getName() + " = " + entry.getKey()
					+ "\nClass NAME KEY: " + entry.getKey().getClass().getName()
					);
			out = ((ReviseOutput)entry.getValue()).newItemRev;
		}
		
		return out;
	}
	
	@SuppressWarnings("unchecked")
	private CreateItemsOutput[] createItems(final ItemIdsAndInitialRevisionIds[] itemIds, final String itemType, final String itemName, final String itemDesc)
			throws TCException {
//		final GetItemCreationRelatedInfoResponse relatedResponse = BuildSpec2G.dmService.getItemCreationRelatedInfo(itemType, null);
		final ItemProperties[] itemProps = new ItemProperties[itemIds.length];
		for (int i = 0; i < itemIds.length; i++) {
			final ItemProperties itemProperty = new ItemProperties();
			itemProperty.clientId = Specification.CLIENT_ID;
			itemProperty.itemId = itemIds[i].newItemId;
			itemProperty.revId = itemIds[i].newRevId;
			itemProperty.name = itemName;
			itemProperty.type = itemType;
			itemProperty.description = itemDesc;
			itemProperty.uom = "";
			itemProps[i] = itemProperty;
		}

		final CreateItemsResponse response = dmService.createItems(
				itemProps, null, null);
		return response.output;
	}
	
	public TCComponent[] createItem(final String type, final String id,
			final String name, final String desc) throws TCException {
		
		final ItemIdsAndInitialRevisionIds[] itemIds = generateItemIds(1, type);
		final CreateItemsOutput[] newItems = createItems(itemIds, type, name, desc);
		
		newItems[0].item.setProperty("item_id", id);

		return new TCComponent[] { newItems[0].item, newItems[0].itemRev };
	}
	
	private boolean isKdLastRevHasAssemblyRev(TCComponentItem kdDoc) throws TCException {
		boolean out = false;
		TCComponentItemRevision lastRev = getLastRevOfItem(kdDoc);
		if (lastRev != null) {
			AIFComponentContext[] relatedComp = lastRev.getRelated("TC_DrawingOf");
			System.out.println("got " + relatedComp.length + " Specs from LAST REVISIONS");
			
			for (AIFComponentContext currConetext : relatedComp) {
				System.out.println("TYPE: " + currConetext.getComponent().getType());
				if (currConetext.getComponent().getType().equals("Pm8_CompanyPartRevision")) {
					TCComponentItemRevision currItemRev = (TCComponentItemRevision) currConetext.getComponent(); 
					if (currItemRev.getProperty("pm8_Designation").equals(lastRev.getProperty("item_id")))
						out = true;
				}
			}
		}
		System.out.println("IS KD LAST REV HAS ASSEMBLY? >> " + out);
		return out;
	}
	
	@SuppressWarnings("unchecked")
	private ItemIdsAndInitialRevisionIds[] generateItemIds(final int numberOfIds, final String type) throws TCException {
		final GenerateItemIdsAndInitialRevisionIdsProperties property = new GenerateItemIdsAndInitialRevisionIdsProperties();
		property.count = numberOfIds;
		property.itemType = type;
		property.item = null; // Not used
		final GenerateItemIdsAndInitialRevisionIdsResponse response = dmService
				.generateItemIdsAndInitialRevisionIds(new GenerateItemIdsAndInitialRevisionIdsProperties[] { property });
		final BigInteger bIkey = new BigInteger("0");
		final Map<BigInteger, ItemIdsAndInitialRevisionIds[]> allNewIds = response.outputItemIdsAndInitialRevisionIds;
		final ItemIdsAndInitialRevisionIds[] myNewIds = allNewIds.get(bIkey);
		return myNewIds;
	}
	
	private TCComponentDataset createDatasetAndAddFile(String file_path)
			throws TCException {
		TCComponentDataset ret = null;
		String dataset_tool = null;
		String dataset_type = null;
		dataset_tool = "PDF_Reference";
		dataset_type = "PDF";
		TCComponentDatasetType dst = (TCComponentDatasetType) topIR.getSession().getTypeComponent("Dataset");
		ret = dst.create(gen_dataset_name(), "Спецификация", dataset_type);
		ret.setFiles(new String[] { reportFile.getAbsolutePath() }, new String[] { dataset_tool });
		ret.lock();
		ret.save();
		ret.unlock();

		return ret;
	}

	private String gen_dataset_name() throws TCException {
		String ret = null;
		if (topIR != null)
			ret = "Спецификация - "
					+ topIR.getTCProperty("object_name").getStringValue();
		return ret;
	}
	
	private void deletePrevSpecDatasetOfKd() throws Exception {
		
		for (AIFComponentContext compContext : specIR.getChildren()){
			System.out.println(">>> TYPE: " + compContext.getComponent().getProperty("object_type"));
			if ((compContext.getComponent() instanceof TCComponentDataset) 
					&& compContext.getComponent().getProperty("object_desc").equals("Спецификация")) {
				System.out.println("Deleting Spec Dataset in KD");
				System.out.println("Deleting Spec Dataset in KD");
				((TCComponent) compContext.getComponent()).removeAndDestroy("IMAN_specification", specIR);
				System.out.println("after destroying");
				specIR.lock();
				specIR.save();
				specIR.unlock();
			}

		}
	}
	
	private TCComponentItemRevision getLastRevOfItem(TCComponentItem item) throws TCException {
		TCComponentItemRevision out = null;
		Map<Integer, TCComponentItemRevision> mapItemRevByRev = new HashMap<Integer, TCComponentItemRevision>();
		ArrayList<Integer> revisions = new ArrayList<Integer>();
		AIFComponentContext[] contextArray = item.getChildren();
		System.out.println("Children of ITEM: " + contextArray.length);
		for (int i=0; i<contextArray.length; i++) {
			System.out.println("~~~~ TYPE: " + contextArray[i].getComponent().getType());
			if (contextArray[i].getComponent().getType().equals("M9_KDRevision")) {
				TCComponentItemRevision currItemRev = (TCComponentItemRevision)contextArray[i].getComponent();
				if(currItemRev.getProperty("item_id").equals(item.getProperty("item_id"))) {
					System.out.println("ADDING TO MAP!");
					Integer rev = Integer.valueOf(currItemRev.getProperty("current_revision_id"));
					mapItemRevByRev.put(rev, currItemRev);
					revisions.add(rev);
				}
			}
		}
		Collections.sort(revisions);
		if (revisions.size() > 0) 
			out = mapItemRevByRev.get(revisions.get(revisions.size()-1)); 
		
		System.out.println("returning: " + out.getProperty("item_id"));
		return out;
	}
	
	private static void deleteRelationsToCompanyPart(TCComponentItemRevision rev) throws Exception {
		ArrayList<TCComponentItemRevision> list4Removing = new ArrayList<TCComponentItemRevision>();
		AIFComponentContext[] itemRev4Delete = rev.getItem().getRelated("M9_DocRel");
		for (AIFComponentContext currContext : itemRev4Delete) {
			if (((TCComponentItemRevision)currContext.getComponent()).getProperty("item_id")
					.equals(rev.getItem().getProperty("item_id"))) {
				System.out.println("~~~ Added to delete");
				list4Removing.add((TCComponentItemRevision)currContext.getComponent());
			}
		}
		rev.remove("M9_DocRel", list4Removing);
	}
	
	private TCComponentItem findKDDocItem() throws TCException {
		TCComponentItem result = null;
		TCComponentItemType itemType = (TCComponentItemType) session.getTypeComponent("M9_KD");
		String criteria = topIR.getProperty("item_id");
		TCComponentItem[] items = itemType.findItems(criteria);
		if (items != null && items.length > 0) {
			for(TCComponentItem item : items){
				System.out.println("Found item " + item.getProperty("item_id") + " of type " + item.getType());
				if(item.getType().equals("M9_KD")){
					result = item;
					break;
				}
			}
		}
		
		return result;
	}

}
