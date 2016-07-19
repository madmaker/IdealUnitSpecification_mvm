package ru.idealplm.specification.mvm.gui;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ListIterator;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;

import ru.idealplm.specification.mvm.util.PerfTrack;
import ru.idealplm.utils.specification.Block;
import ru.idealplm.utils.specification.BlockList;
import ru.idealplm.utils.specification.Specification;
import ru.idealplm.utils.specification.util.GeneralUtils;

import org.eclipse.swt.widgets.DateTime;

import com.teamcenter.rac.util.DateButton;
import com.teamcenter.rac.util.date.*;

public class MainSpecificationDialog extends Dialog {

	protected Object result;
	protected Shell shlCgtwbabrfwbz;
	private TabFolder tabFolder;
	private TabItem tabMain;
	private TabItem tabSignatures;
	private Composite compositeMain;
	private Composite compositeSignatures;

	private Table table;
	private Text text_AddedText;
	private Text text_PrimaryApp;
	private Text text_Litera1;
	private Text text_Litera2;
	private Text text_Litera3;
	private Specification specification;
	private Text textDesigner;
	private Text textCheck;
	private Text textAddCheckPost;
	private Text textAddCheck;
	private Text textNCheck;
	private Text textApprover;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public MainSpecificationDialog(Shell parent, int style, Specification specification) {
		super(parent, style);
		PerfTrack.prepare("Dialog constructor");
		this.specification = specification;
		//setText("SWT Dialog");
		PerfTrack.addToLog("Dialog constructor");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		fillContents();
		shlCgtwbabrfwbz.setLayout(new FillLayout());
		//shlCgtwbabrfwbz.layout();
		shlCgtwbabrfwbz.open();
		Display display = getParent().getDisplay();
		while (!shlCgtwbabrfwbz.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}
	
	/**
	 * Fill contents of the dialog
	 */

	private void fillContents(){
		PerfTrack.prepare("Filling contents");
		BlockList blockList = specification.getBlockList();
		ListIterator<Block> iterator = blockList.listIterator();
		Block block;
		TableItem blockItem;
		while(iterator.hasNext()){
			block = iterator.next();
			System.out.println("In fill contents: " + block.getBlockTitle() + "=" + block.getListOfLines().size());
			blockItem = new TableItem(table, SWT.NONE);
			if(!block.getBlockTitle().equals("Документация")){
				blockItem.setText(new String[]{block.getBlockTitle(), String.valueOf(block.getReservePosNum()), String.valueOf(block.getReserveLinesNum()), String.valueOf(block.getIntervalPosNum())});
			} else {
				blockItem.setText(new String[]{block.getBlockTitle(), String.valueOf(block.getReservePosNum()), String.valueOf(block.getReserveLinesNum()), String.valueOf(block.getIntervalPosNum())});
			}
			if(block.getBlockType().equals("Default") && iterator.nextIndex()!=blockList.size()){
				if(blockList.get(iterator.nextIndex()).getBlockType().equals("ME")){
					blockItem = new TableItem(table, SWT.NONE);
					blockItem.setText(new String[]{"Устанавливается по " + Specification.settings.getStringProperty("MEDocumentId")});
					blockItem.setGrayed(true);
				}
			}
		}
		text_AddedText.setText(Specification.settings.getStringProperty("AddedText")==null?"":Specification.settings.getStringProperty("AddedText"));
		text_Litera1.setText(Specification.settings.getStringProperty("LITERA1")==null?"":Specification.settings.getStringProperty("LITERA1"));
		text_Litera2.setText(Specification.settings.getStringProperty("LITERA2")==null?"":Specification.settings.getStringProperty("LITERA2"));
		text_Litera3.setText(Specification.settings.getStringProperty("LITERA3")==null?"":Specification.settings.getStringProperty("LITERA3"));
		text_PrimaryApp.setText(Specification.settings.getStringProperty("PERVPRIM")==null?"":Specification.settings.getStringProperty("PERVPRIM"));
		
		textDesigner.setText(Specification.settings.getStringProperty("Designer")==null?"":Specification.settings.getStringProperty("Designer"));
		textCheck.setText(Specification.settings.getStringProperty("Check")==null?"":Specification.settings.getStringProperty("Check"));
		textNCheck.setText(Specification.settings.getStringProperty("NCheck")==null?"":Specification.settings.getStringProperty("NCheck"));
		textApprover.setText(Specification.settings.getStringProperty("Approver")==null?"":Specification.settings.getStringProperty("Approver"));
		textAddCheckPost.setText(Specification.settings.getStringProperty("AddCheckPost")==null?"":Specification.settings.getStringProperty("AddCheckPost"));
		textAddCheck.setText(Specification.settings.getStringProperty("AddCheck")==null?"":Specification.settings.getStringProperty("AddCheck"));

		PerfTrack.addToLog("Filling contents");
	}
	
	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		PerfTrack.prepare("Creating contents");
		shlCgtwbabrfwbz = new Shell();
		shlCgtwbabrfwbz.setSize(470, 595);
		shlCgtwbabrfwbz.setText("\u0421\u043F\u0435\u0446\u0438\u0444\u0438\u043A\u0430\u0446\u0438\u044F");
		tabFolder = new TabFolder(shlCgtwbabrfwbz, SWT.NONE);
		tabMain = new TabItem(tabFolder, SWT.BORDER);
		tabSignatures = new TabItem(tabFolder, SWT.BORDER);
		compositeMain = new Composite(tabFolder, SWT.NONE);
		compositeSignatures = new Composite(tabFolder, SWT.NONE);
		compositeMain.setLayout(null);
		compositeSignatures.setLayout(null);
		tabMain.setControl(compositeMain);
		tabSignatures.setControl(compositeSignatures);
		
		Label labelDesigner = new Label(compositeSignatures, SWT.NONE);
		labelDesigner.setText("\u0420\u0430\u0437\u0440\u0430\u0431\u043E\u0442\u0430\u043B");
		labelDesigner.setBounds(37, 47, 65, 13);
		
		textDesigner = new Text(compositeSignatures, SWT.BORDER);
		textDesigner.setBounds(144, 44, 110, 23);
		
		Label labelCheck = new Label(compositeSignatures, SWT.NONE);
		labelCheck.setBounds(37, 88, 49, 13);
		labelCheck.setText("\u041F\u0440\u043E\u0432\u0435\u0440\u0438\u043B");
		
		textCheck = new Text(compositeSignatures, SWT.BORDER);
		textCheck.setBounds(144, 85, 110, 23);
		
		Label labelAddCheck = new Label(compositeSignatures, SWT.NONE);
		labelAddCheck.setBounds(173, 20, 49, 13);
		labelAddCheck.setText("\u0424\u0430\u043C\u0438\u043B\u0438\u044F");
		
		textAddCheck = new Text(compositeSignatures, SWT.BORDER);
		textAddCheck.setBounds(144, 128, 110, 23);
		
		Label labelNCheck = new Label(compositeSignatures, SWT.NONE);
		labelNCheck.setBounds(37, 173, 65, 13);
		labelNCheck.setText("\u041D.\u043A\u043E\u043D\u0442\u0440\u043E\u043B\u044C");
		
		textAddCheckPost = new Text(compositeSignatures, SWT.BORDER);
		textAddCheckPost.setBounds(37, 128, 92, 23);
		
		Label labelApprover = new Label(compositeSignatures, SWT.NONE);
		labelApprover.setBounds(37, 215, 49, 13);
		labelApprover.setText("\u0423\u0442\u0432\u0435\u0440\u0434\u0438\u043B");
		
		textNCheck = new Text(compositeSignatures, SWT.BORDER);
		textNCheck.setBounds(144, 170, 110, 23);
		
		textApprover = new Text(compositeSignatures, SWT.BORDER);
		textApprover.setBounds(144, 212, 110, 23);
		
	    tabMain.setText("Настройки");
	    tabSignatures.setText("\u041F\u043E\u0434\u043F\u0438\u0441\u0430\u043D\u0442\u044B");

		
		final Button button_Renumerize = new Button(compositeMain, SWT.CHECK);
		button_Renumerize.setBounds(10, 10, 213, 16);
		button_Renumerize.setText("\u041F\u0435\u0440\u0435\u043D\u0443\u043C\u0435\u0440\u043E\u0432\u0430\u0442\u044C \u043F\u043E\u0437\u0438\u0446\u0438\u0438");
		button_Renumerize.setEnabled(Specification.settings.getBooleanProperty("canRenumerize"));
		if(!button_Renumerize.isEnabled()) {
			button_Renumerize.setToolTipText("Недоступно, указан запрет смены позиций.");
		} else {
			button_Renumerize.setToolTipText("Перенумерация позиций с возможностью резерва строк и позиций.");
		}
		
		Group group = new Group(compositeMain, SWT.NONE);
		group.setText("\u0420\u0435\u0437\u0435\u0440\u0432 \u043F\u043E\u0437\u0438\u0446\u0438\u0439 \u043F\u043E \u0440\u0430\u0437\u0434\u0435\u043B\u0430\u043C");
		group.setBounds(10, 32, 424, 220);
		
		table = new Table(group, SWT.BORDER | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setBounds(10, 21, 404, 189);
		table.setEnabled(false);
		
		final TableEditor editor = new TableEditor(table);
		editor.horizontalAlignment = SWT.RIGHT;
		editor.grabHorizontal = true;
		editor.minimumWidth = 50;
		
		table.addListener(SWT.MouseDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Rectangle clientArea = table.getClientArea();
				Point pt = new Point(event.x, event.y);
				int index = table.getTopIndex();
				while (index < table.getItemCount()) {
					boolean visible = false;
					final TableItem item = table.getItem(index);
					if(!Specification.blockTitles.containsValue(item.getText(0))) {
						index++;
						continue;
					};
					for (int i = 1; i < table.getColumnCount(); i++) {
						if((item.getText(0).equals("Документация") || item.getText(0).equals("Комплекты")) && i!=2){ continue; };
						Rectangle rect = item.getBounds(i);
						if (rect.contains(pt)) {
							final int column = i;
							final Text text = new Text(table, SWT.NONE);
							Listener textListener = new Listener() {
								@Override
								public void handleEvent(final Event e) {
									switch (e.type) {
									case SWT.FocusOut:
										item.setText(column, text.getText());
										text.dispose();
										break;
									case SWT.Traverse:
										switch (e.detail) {
										case SWT.TRAVERSE_RETURN:
											item.setText(column, text.getText());
											// FALL THROUGH
										case SWT.TRAVERSE_ESCAPE:
											text.dispose();
											e.doit = false;
										}
										break;
									}
								}
							};
							text.addListener(SWT.FocusOut, textListener);
							text.addListener(SWT.Traverse, textListener);
							text.addListener(SWT.Verify, new Listener() {
							      public void handleEvent(Event e) {
							        String string = e.text;
							        char[] chars = new char[string.length()];
							        string.getChars(0, chars.length, chars, 0);
							        for (int i = 0; i < chars.length; i++) {
							          if (!('0' <= chars[i] && chars[i] <= '9')) {
							            e.doit = false;
							            return;
							          }
							        }
							      }
							});
							editor.setEditor(text, item, i);
							text.setText(item.getText(i));
							text.selectAll();
							text.setFocus();
							return;
						}
						if (!visible && rect.intersects(clientArea)) {
							visible = true;
						}
					}
					if (!visible)
						return;
					index++;
				}
			}
		});
		
		TableColumn tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setWidth(149);
		tableColumn.setText("\u0420\u0430\u0437\u0434\u0435\u043B");
		
		TableColumn tableColumn_1 = new TableColumn(table, SWT.NONE);
		tableColumn_1.setWidth(74);
		tableColumn_1.setText("\u0420\u0435\u0437\u0435\u0440\u0432 \u043F\u043E\u0437.");
		
		TableColumn tableColumn_2 = new TableColumn(table, SWT.NONE);
		tableColumn_2.setWidth(82);
		tableColumn_2.setText("\u0420\u0435\u0437\u0435\u0440\u0432 \u0441\u0442\u0440\u043E\u043A");
		
		TableColumn tableColumn_3 = new TableColumn(table, SWT.NONE);
		tableColumn_3.setWidth(93);
		tableColumn_3.setText("\u0418\u043D\u0442\u0435\u0440\u0432\u0430\u043B \u043F\u043E\u0437.");
		
		final Button button_ReadLastRevPos = new Button(compositeMain, SWT.CHECK);
		button_ReadLastRevPos.setText("\u0417\u0430\u0447\u0438\u0442\u0430\u0442\u044C \u043F\u043E\u0437\u0438\u0446\u0438\u0438 \u0441 \u043F\u0440\u043E\u0448\u043B\u043E\u0439 \u0440\u0435\u0432\u0438\u0437\u0438\u0438");
		button_ReadLastRevPos.setBounds(10, 258, 225, 16);
		//button_ReadLastRevPos.setEnabled(Specification.settings.getBooleanProperty("canReadLastRevPos"));
		button_ReadLastRevPos.setEnabled(false);
		
		final Button button_UseReservePos = new Button(compositeMain, SWT.CHECK);
		button_UseReservePos.setText("\u0418\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u044C \u0440\u0435\u0437\u0435\u0440\u0432 \u043F\u043E\u0437\u0438\u0446\u0438\u0439");
		button_UseReservePos.setBounds(10, 282, 225, 16);
		//button_UseReservePos.setEnabled(Specification.settings.getBooleanProperty("canUseReservePos"));
		button_UseReservePos.setEnabled(false);
		
		Label label = new Label(compositeMain, SWT.NONE);
		label.setBounds(10, 317, 136, 13);
		label.setText("\u0414\u043E\u043F\u043E\u043B\u043D\u0438\u0442\u0435\u043B\u044C\u043D\u044B\u0439 \u0442\u0435\u043A\u0441\u0442");
		
		text_AddedText = new Text(compositeMain, SWT.BORDER | SWT.V_SCROLL);
		text_AddedText.setBounds(10, 336, 424, 78);
		
		text_PrimaryApp = new Text(compositeMain, SWT.BORDER);
		text_PrimaryApp.setBounds(10, 437, 154, 19);
		
		Label label_1 = new Label(compositeMain, SWT.NONE);
		label_1.setText("\u041F\u0435\u0440\u0432\u0438\u0447\u043D\u0430\u044F \u043F\u0440\u0438\u043C\u0435\u043D\u044F\u0435\u043C\u043E\u0441\u0442\u044C");
		label_1.setBounds(10, 420, 136, 13);
		
		Label label_litera_1 = new Label(compositeMain, SWT.NONE);
		label_litera_1.setText("\u041B\u0438\u0442\u0435\u0440\u0430 1");
		label_litera_1.setBounds(10, 460, 76, 13);
		
		text_Litera1 = new Text(compositeMain, SWT.BORDER);
		text_Litera1.setBounds(10, 477, 76, 19);
		
		Label label_litera_2 = new Label(compositeMain, SWT.NONE);
		label_litera_2.setText("\u041B\u0438\u0442\u0435\u0440\u0430 2");
		label_litera_2.setBounds(96, 460, 76, 13);
		
		text_Litera2 = new Text(compositeMain, SWT.BORDER);
		text_Litera2.setBounds(96, 477, 76, 19);
		
		Label label_litera_3 = new Label(compositeMain, SWT.NONE);
		label_litera_3.setText("\u041B\u0438\u0442\u0435\u0440\u0430 3");
		label_litera_3.setBounds(182, 460, 76, 13);
		
		text_Litera3 = new Text(compositeMain, SWT.BORDER);
		text_Litera3.setBounds(182, 477, 76, 19);
		
		Button btnOk = new Button(compositeMain, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				specification.isOkPressed = true;
				Specification.settings.addBooleanProperty("doRenumerize", button_Renumerize.getSelection());
				Specification.settings.addBooleanProperty("doReadLastRevPos", button_ReadLastRevPos.getSelection());
				Specification.settings.addBooleanProperty("doUseReservePos", button_UseReservePos.getSelection());
				
				Specification.settings.addStringProperty("AddedText", text_AddedText.getText());
				Specification.settings.addStringProperty("LITERA1", text_Litera1.getText());
				Specification.settings.addStringProperty("LITERA2", text_Litera2.getText());
				Specification.settings.addStringProperty("LITERA3", text_Litera3.getText());
				Specification.settings.addStringProperty("PERVPRIM", text_PrimaryApp.getText());
				
				Specification.settings.addStringProperty("Designer", textDesigner.getText());
				Specification.settings.addStringProperty("Check", textCheck.getText());
				Specification.settings.addStringProperty("AddCheckPost", textAddCheckPost.getText());
				Specification.settings.addStringProperty("AddCheck", textAddCheck.getText());
				Specification.settings.addStringProperty("NCheck", textNCheck.getText());
				Specification.settings.addStringProperty("Approver", textApprover.getText());
				
				BlockList blockList = specification.getBlockList();
				int j = 0;
				for(int i = 0; i < table.getItemCount(); i++){
					TableItem tableItem = table.getItem(i);
					if(tableItem.getText(0).startsWith("Устанавливается")) continue;
					blockList.get(j).setReservePosNum(Integer.parseInt(tableItem.getText(1).isEmpty()?"0":tableItem.getText(1)));
					blockList.get(j).setReserveLinesNum(Integer.parseInt(tableItem.getText(2).isEmpty()?"0":tableItem.getText(2)));
					blockList.get(j).setIntervalPosNum(Integer.parseInt(tableItem.getText(3).isEmpty()?"0":tableItem.getText(3)));
					j++;
				}
				/*ListIterator<Block> iterator = blockList.listIterator();
				Block block;
				TableItem blockItem;
				while(iterator.hasNext()){
					block = iterator.next();
					System.out.println("LOL+"+block.getReservePosNum());
					blockItem = new TableItem(table, SWT.NONE);
					if(!block.getBlockTitle().equals("Документация")){
						blockItem.setText(new String[]{block.getBlockTitle(), String.valueOf(block.getReservePosNum()), String.valueOf(block.getReserveLinesNum()), String.valueOf(block.getIntervalPosNum())});
					} else {
						blockItem.setText(new String[]{block.getBlockTitle(), String.valueOf(block.getReservePosNum()), String.valueOf(block.getReserveLinesNum()), String.valueOf(block.getIntervalPosNum())});
					}
					if(block.getBlockType().equals("Default") && iterator.nextIndex()!=blockList.size()){
						if(blockList.get(iterator.nextIndex()).getBlockType().equals("ME")){
							blockItem = new TableItem(table, SWT.NONE);
							blockItem.setText(new String[]{"Устанавливается по " + Specification.settings.getStringProperty("MEDocumentId")});
							blockItem.setGrayed(true);
						}
					}
				}*/
				
				shlCgtwbabrfwbz.dispose();
				System.out.println("OK!");
			}
		});
		
		btnOk.setBounds(139, 512, 68, 23);
		btnOk.setText("OK");
		
		button_Renumerize.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				table.setEnabled(button_Renumerize.getSelection());
			}
		});
		
		Button btnCancel = new Button(compositeMain, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlCgtwbabrfwbz.dispose();
			}
		});
		btnCancel.setText("Cancel");
		btnCancel.setBounds(229, 512, 68, 23);
		PerfTrack.addToLog("Creating contents");
	}
}
