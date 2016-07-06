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
import ru.idealplm.specification.mvm.comparators.PositionComparator;
import ru.idealplm.specification.mvm.gui.MainSpecificationDialog;
import ru.idealplm.specification.mvm.methods.MVMAttachMethod;
import ru.idealplm.specification.mvm.methods.MVMDataReaderMethod;
import ru.idealplm.specification.mvm.methods.MVMPrepareMethod;
import ru.idealplm.specification.mvm.methods.MVMReportBuilderMethod;
import ru.idealplm.specification.mvm.methods.MVMValidateMethod;
import ru.idealplm.specification.mvm.methods.MVMXmlBuilderMethod;
import ru.idealplm.specification.mvm.util.PerfTrack;
import ru.idealplm.utils.specification.Block;
import ru.idealplm.utils.specification.BlockList;
import ru.idealplm.utils.specification.Specification;
import ru.idealplm.utils.specification.Specification.BlockContentType;

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
		Specification specification = new Specification(topBomLine, new MVMValidateMethod(), new MVMDataReaderMethod(), new MVMPrepareMethod(), new MVMXmlBuilderMethod(),new MVMReportBuilderMethod() , new MVMAttachMethod());
		
		DefaultComparator defaultComparator = new DefaultComparator(Specification.FormField.POSITION);
		DocumentComparator docComparator = new DocumentComparator(specification);
		NumberComparator numComparator = new NumberComparator(Specification.FormField.POSITION);
		PositionComparator posComparator = new PositionComparator();
		
		BlockList blockList = specification.getBlockList();
		blockList.addBlock(new Block(BlockContentType.DOCS, "Default", docComparator, docComparator, 0));
		blockList.addBlock(new Block(BlockContentType.COMPLEXES, "Default", posComparator, posComparator, 0));
		blockList.addBlock(new Block(BlockContentType.ASSEMBLIES, "Default", posComparator, posComparator, 0));
		blockList.addBlock(new Block(BlockContentType.DETAILS, "Default", posComparator, posComparator, 0));
		blockList.addBlock(new Block(BlockContentType.STANDARDS, "Default", posComparator, posComparator, 0));
		blockList.addBlock(new Block(BlockContentType.OTHERS, "Default", posComparator, posComparator, 0));
		blockList.addBlock(new Block(BlockContentType.MATERIALS, "Default", posComparator, posComparator, 0));
		blockList.addBlock(new Block(BlockContentType.KITS, "Default", posComparator, posComparator, 0));
		blockList.addBlock(new Block(BlockContentType.COMPLEXES, "ME", defaultComparator, defaultComparator, 0));
		blockList.addBlock(new Block(BlockContentType.ASSEMBLIES, "ME", defaultComparator, defaultComparator, 0));
		blockList.addBlock(new Block(BlockContentType.DETAILS, "ME", defaultComparator, defaultComparator, 0));
		blockList.addBlock(new Block(BlockContentType.STANDARDS, "ME", defaultComparator, defaultComparator, 0));
		blockList.addBlock(new Block(BlockContentType.OTHERS, "ME", posComparator, posComparator, 0));
		blockList.addBlock(new Block(BlockContentType.MATERIALS, "ME", defaultComparator, defaultComparator, 0));
		
		specification.setColumnLength(FormField.FORMAT, 3);
		specification.setColumnLength(FormField.ZONE, 3);
		specification.setColumnLength(FormField.ID, 3);
		specification.setColumnLength(FormField.NAME, 194.0);
		specification.setColumnLength(FormField.POSITION, 3);
		specification.setColumnLength(FormField.QUANTITY, 3);
		specification.setColumnLength(FormField.REMARK, 76);
		
		
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
				}
				
				PerfTrack.prepare("Creating dialog");
				MainSpecificationDialog mainDialog = new MainSpecificationDialog(HandlerUtil.getActiveShell(event).getShell(), SWT.CLOSE, specification);
				PerfTrack.addToLog("Creating dialog");
				

				mainDialog.open();
				for(int i = 0; i < specification.getBlockList().size(); i++){
					if(specification.getBlockList().get(i).getListOfLines()!=null) System.out.println("Size of " + specification.getBlockList().get(i).getBlockTitle() + " = " + specification.getBlockList().get(i).getListOfLines().size());
				}
				
				if (!specification.isOkPressed) { return null; }
				
				if(Specification.renumerize){
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
				
				if(Specification.renumerize){
					PerfTrack.prepare("Saving&unlocking BOM");
					topBomLine.save();
					topBomLine.unlock();
					PerfTrack.addToLog("Saving&unlocking BOM");
				}
				PerfTrack.printLog();
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
