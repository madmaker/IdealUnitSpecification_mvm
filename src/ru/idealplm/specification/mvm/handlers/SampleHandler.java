package ru.idealplm.specification.mvm.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.dialogs.MessageDialog;

import ru.idealplm.specification.mvm.comparators.DocumentComparator;
import ru.idealplm.specification.mvm.comparators.DefaultComparator;
import ru.idealplm.specification.mvm.comparators.KitComparator;
import ru.idealplm.specification.mvm.comparators.PositionComparator;
import ru.idealplm.specification.mvm.gui.ErrorListDialog;
import ru.idealplm.specification.mvm.gui.MainSpecificationDialog;
import ru.idealplm.specification.mvm.methods.MVMAttachMethod;
import ru.idealplm.specification.mvm.methods.MVMDataReaderMethod;
import ru.idealplm.specification.mvm.methods.MVMPrepareMethod;
import ru.idealplm.specification.mvm.methods.MVMReportBuilderMethod;
import ru.idealplm.specification.mvm.methods.MVMValidateMethod;
import ru.idealplm.specification.mvm.methods.MVMXmlBuilderMethod;
import ru.idealplm.specification.mvm.util.PerfTrack;
import ru.idealplm.utils.specification.Error;
import ru.idealplm.utils.specification.Block;
import ru.idealplm.utils.specification.BlockList;
import ru.idealplm.utils.specification.Specification;
import ru.idealplm.utils.specification.Specification.BlockContentType;
import ru.idealplm.utils.specification.Specification.BlockType;
import ru.idealplm.utils.specification.SpecificationSettings;

import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.pse.plugin.Activator;

import ru.idealplm.utils.specification.Specification.FormField;
import ru.idealplm.utils.specification.comparators.NumberComparator;
/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
@SuppressWarnings("restriction")
public class SampleHandler extends AbstractHandler {
	
	public SampleHandler(){}
	
	@SuppressWarnings("restriction")
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		TCComponentBOMLine topBomLine = Activator.getPSEService().getTopBOMLine();
		Specification specification = Specification.getInstance();
		specification.init(topBomLine, new MVMValidateMethod(), new MVMDataReaderMethod(), new MVMPrepareMethod(), new MVMXmlBuilderMethod(),new MVMReportBuilderMethod() , new MVMAttachMethod());
		
		DefaultComparator defaultComparator = new DefaultComparator(Specification.FormField.POSITION);
		DefaultComparator defaultNameComparator = new DefaultComparator(Specification.FormField.NAME);
		DefaultComparator defaultIDComparator = new DefaultComparator(Specification.FormField.ID);
		DocumentComparator docComparator = new DocumentComparator();
		KitComparator kitComparator = new KitComparator();
		PositionComparator posComparator = new PositionComparator();
		
		BlockList blockList = specification.getBlockList();
		blockList.addBlock(new Block(BlockContentType.DOCS, BlockType.DEFAULT, docComparator, docComparator, 0));
		blockList.addBlock(new Block(BlockContentType.COMPLEXES, BlockType.DEFAULT, defaultIDComparator, posComparator, 0));
		blockList.addBlock(new Block(BlockContentType.ASSEMBLIES, BlockType.DEFAULT, defaultIDComparator, posComparator, 0));
		blockList.addBlock(new Block(BlockContentType.DETAILS, BlockType.DEFAULT, posComparator, posComparator, 0));
		blockList.addBlock(new Block(BlockContentType.STANDARDS, BlockType.DEFAULT, defaultNameComparator, posComparator, 0));
		blockList.addBlock(new Block(BlockContentType.OTHERS, BlockType.DEFAULT, defaultNameComparator, posComparator, 0));
		blockList.addBlock(new Block(BlockContentType.MATERIALS, BlockType.DEFAULT, posComparator, posComparator, 0));
		blockList.addBlock(new Block(BlockContentType.KITS, BlockType.DEFAULT, kitComparator, kitComparator, 0));
		blockList.addBlock(new Block(BlockContentType.COMPLEXES, BlockType.ME, defaultIDComparator, defaultComparator, 0));
		blockList.addBlock(new Block(BlockContentType.ASSEMBLIES, BlockType.ME, defaultIDComparator, defaultComparator, 0));
		blockList.addBlock(new Block(BlockContentType.DETAILS, BlockType.ME, defaultComparator, defaultComparator, 0));
		blockList.addBlock(new Block(BlockContentType.STANDARDS, BlockType.ME, defaultNameComparator, defaultComparator, 0));
		blockList.addBlock(new Block(BlockContentType.OTHERS, BlockType.ME, defaultNameComparator, posComparator, 0));
		blockList.addBlock(new Block(BlockContentType.MATERIALS, BlockType.ME, defaultComparator, defaultComparator, 0));
		blockList.addBlock(new Block(BlockContentType.KITS, BlockType.ME, kitComparator, kitComparator, 0));
		
		blockList.getBlock(BlockContentType.DOCS, BlockType.DEFAULT).setIsRenumerizable(false);
		blockList.getBlock(BlockContentType.KITS, BlockType.DEFAULT).setIsRenumerizable(false);
		blockList.getBlock(BlockContentType.KITS, BlockType.ME).setIsRenumerizable(false);
		
		Specification.settings.setColumnLength(FormField.FORMAT, 3);
		Specification.settings.setColumnLength(FormField.ZONE, 3);
		Specification.settings.setColumnLength(FormField.ID, 3);
		Specification.settings.setColumnLength(FormField.NAME, 194.0);
		Specification.settings.setColumnLength(FormField.POSITION, 3);
		Specification.settings.setColumnLength(FormField.QUANTITY, 3);
		Specification.settings.setColumnLength(FormField.REMARK, 88);
		
		
		if(!specification.validate()){
			System.out.println(specification.getErrorList().toString());
		} else {
			try{
				//ReportBuilder reportBuilder = new PDFBuilder(Specification.getDefaultSpecificationPDFTemplate(), Specification.getDefaultSpecificationPDFConfig());
				PerfTrack.prepare("readBOMData");
				specification.readBOMData();
				PerfTrack.addToLog("readBOMData");
				
				if(specification.getErrorList().size()>0){
					System.out.println("TODO: INFORM ABOUT ERRORS");
					ErrorListDialog elg = new ErrorListDialog(HandlerUtil.getActiveShell(event).getShell(), specification.getErrorList().errorList);
					return null;
				}
				
				readSettings(specification.getBlockList());
				
				PerfTrack.prepare("Creating dialog");
				MainSpecificationDialog mainDialog = new MainSpecificationDialog(HandlerUtil.getActiveShell(event).getShell(), SWT.CLOSE, specification);
				PerfTrack.addToLog("Creating dialog");
				

				mainDialog.open();
				for(int i = 0; i < specification.getBlockList().size(); i++){
					if(specification.getBlockList().get(i).getListOfLines()!=null) System.out.println("Size of " + specification.getBlockList().get(i).getBlockTitle() + " = " + specification.getBlockList().get(i).getListOfLines().size());
				}
				
				if (!Specification.settings.getBooleanProperty("bOkPressed")) { return null; }
				
				saveSettings(specification.getBlockList());
				
				if(Specification.settings.getBooleanProperty("doRenumerize")){
					PerfTrack.prepare("Locking BOM");
					topBomLine.lock();
					PerfTrack.addToLog("Locking BOM");
				}
				
				PerfTrack.prepare("prepareBlocks");
				specification.prepareBlocks();
				PerfTrack.addToLog("prepareBlocks");
				try{
					Thread.sleep(1000);
				}catch(Exception ex){};
				PerfTrack.prepare("makeXmlFile");
				specification.makeXmlFile();
				PerfTrack.addToLog("makeXmlFile");
				PerfTrack.prepare("makeReport");
				specification.makeReportFile();
				PerfTrack.addToLog("makeReport");
				PerfTrack.prepare("putInTeamcenter");
				specification.putInTeamcenter();
				PerfTrack.addToLog("putInTeamcenter");
				System.out.println("--- ERROR LIST ---");
				System.out.println(specification.getErrorList().toString());
				System.out.println("--- ERROR LIST ---");
				
				if(Specification.settings.getBooleanProperty("doRenumerize")){
					PerfTrack.prepare("Saving&unlocking BOM");
					topBomLine.save();
					topBomLine.unlock();
					PerfTrack.addToLog("Saving&unlocking BOM");
				}
				
				PerfTrack.printLog();
			} catch (TCException e) {
				e.printStackTrace();
			} finally {
				specification.cleanUp();
				specification = null;
			}
		}
		return null;
	}
	
	void readSettings(BlockList blockList){
		String settingsString = Specification.settings.getStringProperty("blockSettings");
		System.out.println("BLOCK_SETTINGS"+settingsString);
		if(settingsString==null || settingsString.isEmpty()) return;
		for(String blockProps:settingsString.split("&")){
			System.out.println(blockProps);
			String[] props = blockProps.split(":");
			if(props.length!=4) continue;
			System.out.println(props[0]);
			Block block = blockList.getBlock(BlockContentType.values()[Character.getNumericValue(props[0].charAt(0))], props[0].charAt(1)=='0'?BlockType.DEFAULT:BlockType.ME);
			if(block!=null){
				block.setReservePosNum(Integer.parseInt(props[1]));
				block.setReserveLinesNum(Integer.parseInt(props[2]));
				block.setIntervalPosNum(Integer.parseInt(props[3]));
			}
		}
	}
	
	void saveSettings(BlockList blockList){
		String settingsString = "";
		String del = ":";
		for(Block block:blockList){
			settingsString+=block.getBlockContentType().ordinal()+(block.getBlockType()==BlockType.DEFAULT?"0":"1")+del+block.getReservePosNum()+del+block.getReserveLinesNum()+del+block.getIntervalPosNum();
			settingsString+="&";
		}
		if(settingsString.endsWith("&")){
			settingsString = settingsString.substring(0, settingsString.length()-1);
		}
		Specification.settings.addStringProperty("blockSettings", settingsString);
	}
}
