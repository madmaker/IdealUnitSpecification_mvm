package ru.idealplm.specification.mvm.methods;

import java.util.Collections;
import java.util.HashMap;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMView;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevision;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCComponentRevisionRuleType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

import ru.idealplm.specification.mvm.comparators.PositionComparator;
import ru.idealplm.utils.specification.Block;
import ru.idealplm.utils.specification.BlockLine;
import ru.idealplm.utils.specification.Specification;
import ru.idealplm.utils.specification.Specification.FormField;
import ru.idealplm.utils.specification.methods.PrepareMethod;

public class MVMPrepareMethod implements PrepareMethod{
	
	private Specification specification;
	int firstPos = 1;
	private HashMap<String,String> prevPosMap = new HashMap<String,String>();

	@Override
	public void prepareBlocks() {
		try{
			this.specification = Specification.getInstance();
			System.out.println("...METHOD...  PrepareMethod");
			for(Block block:specification.getBlockList()) {
				if(!block.isRenumerizable()) continue;
				block.setFirstPosNo(firstPos);
				System.out.println("Setting firstpos:" + firstPos + " For block:" + block.getBlockTitle());
				firstPos = firstPos + block.getReservePosNum() + block.getRenumerizableLinesCount() + (block.getRenumerizableLinesCount()-1)*block.getIntervalPosNum();
				System.out.println("next:"+block.getReservePosNum()+":"+block.getRenumerizableLinesCount()+":"+block.getIntervalPosNum());
			}
			for(Block block:specification.getBlockList()) block.run();
			
			if(Specification.settings.getBooleanProperty("doReadLastRevPos")){
				System.out.println("...READING LAST REV");
				TCComponentItemRevision prevRev = null;
				TCComponentItem topItem = Specification.getInstance().getTopBOMLine().getItem();
				TCComponentItemRevision topItemR = Specification.getInstance().getTopBOMLine().getItemRevision();
				TCComponent[] revisions = topItem.getRelatedComponents("revision_list");
				
				for(int i = 0; i < revisions.length; i++){
					if(revisions[i].getUid().equals(topItemR.getUid()) && i>0){
						prevRev = (TCComponentItemRevision) revisions[i-1];
						System.out.println(revisions[i].getProperty("object_name"));
						System.out.println(revisions[i-1].getProperty("object_name"));
						break;
					}
				}
				readDataFromPrevRev(prevRev);
				for(Block block:specification.getBlockList()) {
					if(!block.isRenumerizable()) continue;
					for(BlockLine bl:block.getListOfLines()){
						if(!bl.isSubstitute){
							String currentPos = prevPosMap.get(bl.uid);
							if(currentPos==null) continue;
							System.out.println("FOUND PREV FOR " + bl.uid);
							try {
								//bl.renumerize(String.valueOf(currentPos));
								bl.attributes.setPosition(currentPos);
								if(bl.getRefBOMLines()!=null && !bl.isSubstitute){
									for(TCComponentBOMLine chbl:bl.getRefBOMLines()){
										chbl.setProperty("bl_sequence_no", currentPos);
									}
								}
								if(bl.getSubstituteBlockLines()!=null){
									for(BlockLine sbl:bl.getSubstituteBlockLines()){
										sbl.attributes.setPosition(currentPos+"*");
									}
								}
							} catch (TCException e) {
								e.printStackTrace();
							}
						}
					}
					Collections.sort(block.getListOfLines(), new PositionComparator());
				}
			}
			
			if(Specification.settings.getBooleanProperty("doRenumerize")){
				System.out.println("...RENUMERIZING");
				for(Block block:specification.getBlockList()) {
					String currentPos = String.valueOf(block.getFirstPosNo());
					if(!block.isRenumerizable()) continue;
					for(BlockLine bl:block.getListOfLines()){
						if(!bl.isSubstitute){
							try {
								//bl.renumerize(String.valueOf(currentPos));
								bl.attributes.setPosition(currentPos);
								if(bl.getRefBOMLines()!=null && !bl.isSubstitute){
									for(TCComponentBOMLine chbl:bl.getRefBOMLines()){
										//System.out.println("Setting pos:" + pos + " for bl=" + getStringValueFromField(FormField.NAME));
										chbl.setProperty("bl_sequence_no", currentPos);
									}
								}
								if(bl.getSubstituteBlockLines()!=null){
									for(BlockLine sbl:bl.getSubstituteBlockLines()){
										sbl.attributes.setPosition(currentPos+"*");
									}
								}
							} catch (TCException e) {
								e.printStackTrace();
							}
							currentPos = String.valueOf(Integer.parseInt(currentPos) + block.getIntervalPosNum() + 1);
						}
					}
					Collections.sort(block.getListOfLines(), new PositionComparator());
				}
			}
			if(Specification.settings.getBooleanProperty("doUseReservePos")){
				for(Block block:specification.getBlockList()) {
					String currentPos = String.valueOf(block.getFirstPosNo());
					if(!block.isRenumerizable()) continue;
					for(BlockLine bl:block.getListOfLines()){
						if(!bl.isSubstitute){
							try {
								//bl.renumerize(String.valueOf(currentPos));
								bl.attributes.setPosition(currentPos);
								if(bl.getRefBOMLines()!=null && !bl.isSubstitute){
									for(TCComponentBOMLine chbl:bl.getRefBOMLines()){
										chbl.setProperty("bl_sequence_no", currentPos);
									}
								}
								if(bl.getSubstituteBlockLines()!=null){
									for(BlockLine sbl:bl.getSubstituteBlockLines()){
										sbl.attributes.setPosition(currentPos+"*");
									}
								}
							} catch (TCException e) {
								e.printStackTrace();
							}
							currentPos = String.valueOf(Integer.parseInt(currentPos) + block.getIntervalPosNum() + 1);
						}
					}
					Collections.sort(block.getListOfLines(), new PositionComparator());
				}
			}
			//specification.getBlockList().getBlock(BlockContentType.DOCS, "Default").run();
	
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	void readDataFromPrevRev(TCComponentItemRevision rev){
		try{
			System.out.println("REVNO:"+String.valueOf(rev.getProperty("revision_number")));
			TCComponentBOMLine topBOMLine = getBOMLine(rev, "view", "Released", Specification.session);
			System.out.println("REVNO:"+String.valueOf(topBOMLine.getProperty("bl_rev_revision_number")));
			AIFComponentContext[] childBOMLines = topBOMLine.getChildren();
			
			for (AIFComponentContext currBOMLine : childBOMLines) {
				TCComponentBOMLine bl = (TCComponentBOMLine) currBOMLine.getComponent();
				prevPosMap.put(bl.getItemRevision().getUid(), bl.getProperty("bl_sequence_no"));
				System.out.println("PUT_"+bl.getItemRevision().getUid()+" FOR "+bl.getProperty("bl_sequence_no"));
			}
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
		public TCComponentBOMLine getBOMLine(TCComponentItemRevision rev, String bvrType, String revisionRule, TCSession session) 
			                        throws Exception
			        {
			                TCComponentBOMViewRevision bomViewRevision = null;
			                TCComponentBOMWindow bomWindow = null;
			                TCComponentRevisionRule revRule = null;
			                bomViewRevision = getBomViewRevByType(rev, bvrType);
			                if(revisionRule.equals("")){
			                        revRule = null;
			                } else {
			                        revRule = (TCComponentRevisionRule)getRevRule(session, revisionRule);
			                }
			                bomWindow = createBomWindow(session, revRule);
			                TCComponentBOMView bomView = getBomView(rev.getItem(), bvrType);
			                TCComponentBOMLine bomLine = bomWindow.setWindowTopLine(rev.getItem(), rev, bomView, bomViewRevision);
			                return bomLine;
			        }
			        
			        public TCComponentBOMViewRevision getBomViewRevByType(TCComponentItemRevision rev, String type){
			                try{
			                        TCComponent[] structureRevisions = rev.getRelatedComponents("structure_revisions");
			                        if(structureRevisions!=null){
			                                return (TCComponentBOMViewRevision)structureRevisions[0];
			                        } else {
			                                return null;
			                        }
			                } catch(Exception ex){
			                        ex.printStackTrace();
			                        return null;
		                }
			        }
			        
			        public TCComponent getRevRule(TCSession session, String rule){
			                try{
			                        TCComponentRevisionRuleType rrType = (TCComponentRevisionRuleType)session.getTypeComponent("RevisionRule");
			                        TCComponent[] rrComponents = rrType.extent();
			                        for(TCComponent rrComponent:rrComponents){
			                                if(rrComponent.toString().equalsIgnoreCase(rule)){
			                                        return rrComponent;
			                                }
			                        }
			                        return null;
			                } catch (Exception ex){
			                        ex.printStackTrace();
			                        return null;
			                }
			        }
			        
			        public TCComponentBOMWindow createBomWindow(TCSession session, TCComponentRevisionRule rule){
			                try{
			                        TCComponentBOMWindowType bwType = (TCComponentBOMWindowType)session.getTypeComponent("BOMWindow");
			                        return bwType.create(rule);
		                } catch (Exception ex){
			                        ex.printStackTrace();
			                        return null;
			                }
			        }
			        
			        public TCComponentBOMView getBomView(TCComponentItem item, String type){
			                try{
			                        TCComponent[] components = item.getRelatedComponents("bom_view_tags");
			                        if(components!=null){
			                                return (TCComponentBOMView)components[0];
			                        }
			                        return null;
			                } catch (Exception ex){
			                        ex.printStackTrace();
			                        return null;
			                }
			        }
	}


