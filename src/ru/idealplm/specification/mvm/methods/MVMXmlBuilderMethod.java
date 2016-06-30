package ru.idealplm.specification.mvm.methods;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.idealplm.utils.specification.Block;
import ru.idealplm.utils.specification.BlockLine;
import ru.idealplm.utils.specification.BlockList;
import ru.idealplm.utils.specification.Specification;
import ru.idealplm.utils.specification.Specification.FormField;
import ru.idealplm.utils.specification.methods.XmlBuilderMethod;

public class MVMXmlBuilderMethod implements XmlBuilderMethod{
	
	private ArrayList<Integer> max_cols_sise_a1;
	private final int MAX_LINES_FIRST = 29;
	private final int MAX_LINES_OTHER = 32;
	private int currentLineNum = 1;
	private int currentPageNum = 1;
	
	private DocumentBuilderFactory documentBuilderFactory;
	private DocumentBuilder builder;
	private Document document;
	
	Element node_root;
	Element node;
	Element node_block = null;
	Element node_occ_title;
	Element node_occ;
	
	private BlockLine emptyLine;
	
	/******temp******/
	public static HashMap<String,String> stampMap = new HashMap<String,String>();
	static{
		stampMap.put("APRDATE", " ");
		stampMap.put("CHKDATE", " ");
		stampMap.put("CRTDATE", " ");
		stampMap.put("CTRLDATE", " ");
		stampMap.put("LITERA1", " ");
		stampMap.put("LITERA2", " ");
		stampMap.put("LITERA3", " ");
		stampMap.put("NAIMEN", "null");
		stampMap.put("NORM", " ");
		stampMap.put("OBOZNACH", "null");
		stampMap.put("PAGEQTY", " ");
		stampMap.put("PERVPRIM", " ");
		stampMap.put("PROJECTNAME", " ");
		stampMap.put("PROV", " ");
		stampMap.put("RAZR", " ");
		stampMap.put("SPCODE", " ");
		stampMap.put("UTV", " ");
		stampMap.put("ZAVOD", " ");
	}
	/****************/

	@Override
	public File makeXmlFile(Specification specification) {
		System.out.println("...METHOD... XmlBuilderhMethod");
		stampMap.put("NAIMEN", specification.getStringProperty("NAIMEN"));
		stampMap.put("OBOZNACH", specification.getStringProperty("OBOZNACH"));
		stampMap.put("PERVPRIM", specification.getStringProperty("PERVPRIM"));
		stampMap.put("LITERA1", specification.getStringProperty("LITERA1"));
		stampMap.put("LITERA2", specification.getStringProperty("LITERA2"));
		stampMap.put("LITERA3", specification.getStringProperty("LITERA3"));
		
		emptyLine = (new BlockLine()).build();
		emptyLine.setQuantity("-1.0");
		try{
			documentBuilderFactory = DocumentBuilderFactory.newInstance();
			builder = documentBuilderFactory.newDocumentBuilder();
			document = builder.newDocument();
			node_root = document.createElement("root");
			document.appendChild(node_root);
			
			String[] cs_a1 = { "Format:3", "Zone:3", "Position:3", "Denotation:25", "Nomination:22", "Quantity:4", "Notes:8" };
			max_cols_sise_a1 = new ArrayList<Integer>();
			for (int i = 0; i < cs_a1.length; i++) {
				String[] cd = cs_a1[i].split(":");
				if (cd.length != 2)
					continue;
				max_cols_sise_a1.add(Integer.valueOf(Integer.parseInt(cd[1])));
			}
			node = document.createElement("Max_Cols_Size");
			for (Integer ii = Integer.valueOf(0); ii.intValue() < max_cols_sise_a1
					.size(); ii = Integer.valueOf(ii.intValue() + 1))
				node.setAttribute("Col_" + Integer.toString(ii.intValue() + 1),
						Integer.toString(max_cols_sise_a1.get(ii
								.intValue()).intValue()));
			node_root.appendChild(node);
			
			BlockList blockList = specification.getBlockList();
			ListIterator<Block> iterator = blockList.listIterator();
			Block block;
			
			while(iterator.hasNext()){
				block = iterator.next();
				processBlock(block);
				if(block.getBlockType().equals("Default") && iterator.nextIndex()!=blockList.size()){
					if(blockList.get(iterator.nextIndex()).getBlockType().equals("ME")){
						newPage();
						//addEmptyLines(1);
						String string = "��������������� �� " + specification.getStringProperty("MEDocumentId");
						node_occ = document.createElement("Occurrence");
						node = document.createElement("Col_" + 4);
						node.setTextContent(string.substring(0, string.length()/2));
						node.setAttribute("align", "right");
						node_occ.appendChild(node);
						node = document.createElement("Col_" + 5);
						node.setTextContent(string.substring(string.length()/2));
						node.setAttribute("align", "left");
						node_occ.appendChild(node);
						node_block.appendChild(node_occ);
						currentLineNum++;
						addEmptyLines(1);
					}
				}
			}
			
			/*****temp*****/
			node = document.createElement("Izdelie_osnovnai_nadpis");
			Set<String> keys = stampMap.keySet();
			for (String idx_form_block : keys)
				if (stampMap.get(idx_form_block) != null)
					node.setAttribute(idx_form_block, stampMap.get(idx_form_block));
			
			node_root.appendChild(node);
			/**************/
			
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			DOMSource source = new DOMSource(document);
			File xmlFile = File.createTempFile("spec_export", ".xml");
			StreamResult result = new StreamResult(xmlFile);
			transformer.transform(source, result);
			return xmlFile;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public void processBlock(Block block){
		System.out.println("Writing block: " + Specification.blockTitles.get(block.getBlockContentType()));
		if(block.getListOfLines()!=null){
			if (node_block == null) {
				node_block = document.createElement("Block");
				node_root.appendChild(node_block);
				addEmptyLines(1);
			}
			if(getFreeLinesNum() < 3 + block.getListOfLines().get(0).getLineHeight()){
				System.out.println("Free:"+getFreeLinesNum() + " lineHeight:"+block.getListOfLines().get(0).getLineHeight());
				newPage();
			}
			//addEmptyLines(1);
			node_root.appendChild(node_block);
			node_occ_title = document.createElement("Occurrence");
			node_occ_title.setAttribute("font", "underline,bold");
			node = document.createElement("Col_" + 5);
			node.setAttribute("align", "center");
			node.setTextContent(block.getBlockTitle());
			node_occ_title.appendChild(node);
			node_block.appendChild(node_occ_title);
			currentLineNum++;
			addEmptyLines(1);
			for(BlockLine blockLine : block.getListOfLines()){
				newLine(block, blockLine);
			}
			addEmptyLines(block.gerReserveLinesNum());
			node_root.appendChild(node_block);
		}
	}
	
	public int countSublines(BlockLine line){
		int result = 0;
		if(line.getAttachedLines() == null) return result;
		for(BlockLine attachedLine : line.getAttachedLines()){
			result += attachedLine.getLineHeight();
		}
		return result;
	}
	
	public void newPage(){
		addEmptyLines(getFreeLinesNum());
		node_block = document.createElement("Block");
		node_root.appendChild(node_block);
		currentLineNum = 1;
		currentPageNum += 1;
		addEmptyLines(1);
	}
	
	public void newLine(Block block, BlockLine line){
		//boolean isLastLineInBlock = block.getListOfLines().get(block.getListOfLines().size()-1)!=line;
		
		if(getFreeLinesNum() < (line.getLineHeight() + 1 + countSublines(line))) newPage();
		
		if(block.getListOfLines().indexOf(line) != block.size()-1){
			int thisLinePos = 0;
			int nextLinePos = 0;
			boolean flag = true;
			try{
				thisLinePos = Integer.parseInt(line.getPosition());
			} catch	(NumberFormatException ex){
				flag = false;
			}
			try {
				nextLinePos = Integer.parseInt(block.getListOfLines().get(block.getListOfLines().indexOf(line)+1).getPosition());
			} catch (NumberFormatException ex){
				flag = false;
			}
			if(flag && (nextLinePos > thisLinePos)) {
				for(int i = 0; i < nextLinePos-thisLinePos-1; i++){
					line.attachLine(emptyLine);
				}
				//addEmptyLines((nextLinePos-thisLinePos-1)*2);
			}
		}
		
		for(int i = 0; i < line.getLineHeight(); i++){
			node_occ = document.createElement("Occurrence");
			if(i==0){
				node = document.createElement("Col_" + 1);
				node.setAttribute("align", "center");
				node.setTextContent(line.getFormat().toString());
				node_occ.appendChild(node);
				node = document.createElement("Col_" + 3);
				node.setAttribute("align", "center");
				node.setTextContent(line.getPosition());
				node_occ.appendChild(node);
				node = document.createElement("Col_" + 4);
				node.setTextContent(line.getId());
				node_occ.appendChild(node);
				node = document.createElement("Col_" + 6);
				node.setAttribute("align", "center");
				node.setTextContent(line.getStringValueFromField(FormField.QUANTITY).equals("-1")?" ":line.getStringValueFromField(FormField.QUANTITY));
				//node.setTextContent(String.valueOf(line.getQuantity()).equals("-1.0")?" ":String.valueOf(line.getQuantity()));
				node_occ.appendChild(node);
			}
			node = document.createElement("Col_" + 5);
			node.setTextContent((line.getName()!=null && (i < line.getName().size())) ? line.getName().get(i) : "");
			node_occ.appendChild(node);
			node = document.createElement("Col_" + 7);
			node.setTextContent((line.getRemark()!=null && (i < line.getRemark().size())) ? line.getRemark().get(i) : "");
			node_occ.appendChild(node);
			if (i==line.getLineHeight()-1){
			}
			
			node_block.appendChild(node_occ);
		}
		
		currentLineNum += line.getLineHeight();
		addEmptyLines(1);

		if(line.getAttachedLines()!=null){
			for(BlockLine attachedLine : line.getAttachedLines()){
				newLine(block, attachedLine);
			}
		}
		
	}
	
	public void addEmptyLines(int num){
		for(int i = 0; i < num; i++){
			if(getFreeLinesNum() <= 0){
				newPage();
			}
			currentLineNum++;
			node_occ = document.createElement("Occurrence");
			node_block.appendChild(node_occ);
		}
	}
	
	int getFreeLinesNum(){
		if(currentPageNum==1) return (MAX_LINES_FIRST - currentLineNum + 1);
		return (MAX_LINES_OTHER - currentLineNum + 1);
	}

}