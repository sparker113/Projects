import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import frame.GUIUtilities;
import mrl.DataRequest;

public class MapChannels implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1312974161987549487L;
	DataChannels dataChannels;
	private String projectName;
	private Collection<String> headers;
	private List<String> wellNames;
	private List<String> offsetWells;
	public MapChannels(Collection<String> headers,List<String> wellNames,List<String> offsetWells,String projectName) throws ClassNotFoundException, IOException, InterruptedException {
		this.projectName = projectName;
		this.headers = headers;
		this.offsetWells = offsetWells;
		this.wellNames = wellNames;
		dataChannels = DataChannels.getDataChannelsInst(projectName);
		setChannels(wellNames,offsetWells);
	}

	
	public void setChannels(List<String> wellNames,List<String> offsetWells) throws IOException, InterruptedException {
		setChannelMap(dataChannels,DataChannels.FRAC_CHANNELS,getMnemonics(),"Set Frac Data Channels");
		setChannelMap(dataChannels,DataChannels.OFFSET_CHANNELS,offsetWells,"Set Offset Channels");
		setChannelMap(dataChannels,DataChannels.DEFINED_CHANNELS,"Set User Defined Channels");
		setChannelMap(dataChannels,DataChannels.WELL_PRESSURE_CHANNELS,wellNames,"Set Well Pressure Channels");
		dataChannels.saveChannels();
	}
	
	
	public List<String> getOffsetWells(){
		return offsetWells;
	}
	public List<String> getWellNames(){
		return wellNames;
	}
	public String getJobName() {
		return projectName;
	}
	
	private void setChannelMap(DataChannels dataChannels,String mapName,String title) throws InterruptedException {
		ChannelFrame channelFrame = new ChannelFrame(headers,title,dataChannels.getChannelMap(mapName));
		channelFrame.getPermit();
	}
	
	private void setChannelMap(DataChannels dataChannels,String mapName,Collection<String> mnemonics,String title) throws InterruptedException {
		ChannelFrame channelFrame = new ChannelFrame(headers,title,dataChannels.getChannelMap(mapName),mnemonics);
		channelFrame.getPermit();
	}

	/*
	 * @Deprecated private void setChannelMaps(Map<Integer, List<String>>
	 * tableInputs) { for (int i = 0; i <
	 * tableInputs.get(ChannelFrame.CONST_COLUMN).size(); i++) { String input =
	 * tableInputs.get(ChannelFrame.CONST_COLUMN).get(i); String dataHeader =
	 * tableInputs.get(ChannelFrame.CHANNEL_COLUMN).get(i); if
	 * (isOffsetInput(input)) { addToOffsets(input, dataHeader); } else if
	 * (isWellInput(input)) { addToWellPres(input, dataHeader); continue; }
	 * addToChannels(input, dataHeader); } }
	 */
	public final static String ADD_CHANNEL_DELIM = "&&";


	/*
	 * private void writeAllMapsToFiles() throws IOException {
	 * writeMapToFile(CHANNEL_MAP_FILENAME, projectName, channelMap);
	 * writeMapToFile(OFFSET_MAP_FILENAME, projectName, offsetMap);
	 * writeMapToFile(WELL_PRES_FILENAME, projectName, wellPresMap); }
	 */


	/*
	 * private boolean isOffsetInput(String input) { return
	 * input.matches(OFFSET_PREFACE + ".+"); }
	 * 
	 * private boolean isWellInput(String input) { return input.matches(WELL_PREFACE
	 * + ".+"); }
	 * 
	 * private void addToOffsets(String offsetStr, String dataHeader) { String
	 * wellName = offsetStr.replaceFirst(OFFSET_PREFACE, "");
	 * offsetMap.put(wellName, dataHeader); }
	 * 
	 * private void addToWellPres(String fracWell, String dataHeader) { String
	 * wellName = fracWell.replaceFirst(WELL_PREFACE, ""); wellPresMap.put(wellName,
	 * dataHeader); }
	 * 
	 * private void addToChannels(String dataName, String dataHeader) { if
	 * (channelMap.containsKey(dataName)) {
	 * channelMap.put(getNextDuplicateName(dataName), dataHeader); return; }
	 * channelMap.put(dataName, dataHeader); }
	 */
	/*
	 * public String[] getDataHeaders(String dataName) { String[] headers = new
	 * String[0]; for (String s : channelMap.keySet()) { Matcher matcher =
	 * Pattern.compile("(" + dataName + ")" + MULTI_CHANNEL_REGEX).matcher(s); if
	 * (matcher.find()) { addToArr(headers, matcher.group(1)); } } return headers; }
	 */
	


	/*
	 * private String getNextDuplicateName(String dataName) { int i = 1; for (String
	 * s : channelMap.keySet()) { if (s.matches(dataName + MULTI_CHANNEL_REGEX)) {
	 * i++; } } return dataName + MULTI_CHANNEL_IDENT + i; }
	 */

	class ChannelFrame extends JFrame {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		ExecutorService executor;
		
		/*
		 * channelMap should be an empty map that will be populated
		 * by the inputs when save button is clicked
		 */
		Semaphore semaphore;
		ChannelFrame(Collection<String> headers,String title,Map<String,String> channelMap) {
			executor = Executors.newCachedThreadPool();
			nittyGritty(headers,channelMap,Collections.emptyList(),title);
		}
		ChannelFrame(Collection<String> headers,String title,Map<String,String> channelMap,String...mnemonics) {
			executor = Executors.newCachedThreadPool();
			nittyGritty(headers,channelMap,getMnemonicsCollection(mnemonics),title);
		}
		ChannelFrame(Collection<String> headers,String title,Map<String,String> channelMap,Collection<String> mnemonics) {
			executor = Executors.newCachedThreadPool();
			nittyGritty(headers,channelMap,mnemonics,title);
		}
		private Collection<String> getMnemonicsCollection(String...mnemonics){
			Collection<String> mnemonicCollection = new ArrayList<>();
			for(String s:mnemonics) {
				mnemonicCollection.add(s);
			}
			return mnemonicCollection;
		}
	

		private static String getValue(JTable table, int row, int column) {
			String value = (String) table.getValueAt(row, column);
			return value == null ? "" : value;
		}

		private Map<String,String> getTableInputs(JTable table) {
			Map<String, String> map = new LinkedHashMap<>();
			for (int i = 0; i < table.getRowCount(); i++) {
				String constValue = getValue(table, i, CONST_COLUMN);
				String channelValue = getValue(table, i, CHANNEL_COLUMN);
				if (constValue.equals("")) {
					break;
				}
				map.put(constValue,channelValue);
			}
			return map;
		}

		private void nittyGritty(Collection<String> headers,Map<String,String> channelMap,Collection<String> mnemonics,String title) {
			semaphore = new Semaphore(0);
			Rectangle frameRect = getFrameRect();
			setTitle(title);
			setBounds(frameRect);
			setLayout(null);
			add(constrScrollPane(frameRect, headers,mnemonics));
			add(constrButtonPanel(frameRect,channelMap));
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setVisible(true);
		}
		

		private final static float CENTERED_MARGIN = 0.25f;
		private final static float BUTTON_PANEL_MARGIN = 0.25f;

		private Rectangle getFrameRect() {
			return GUIUtilities.getCenterRectangle(CENTERED_MARGIN);
		}

		private final static int MAX_BUTTON_PANEL_HEIGHT = 100;

		private JPanel constrButtonPanel(Rectangle frameRect,Map<String,String> channelMap) {
			JPanel panel = new JPanel();
			panel.setBackground(Color.DARK_GRAY);
			Rectangle panelBounds = getButtonPanelBounds(frameRect);
			panel.setBounds(panelBounds);
			panel.add(getSaveButton(panelBounds,channelMap));
			panel.setVisible(true);
			return panel;
		}

		private Rectangle getButtonPanelBounds(Rectangle frameRect) {
			Rectangle scrollPaneRect = getScrollPaneBounds(frameRect);

			Rectangle rectangle = new Rectangle(0, scrollPaneRect.height, scrollPaneRect.width,
					getButtonPanelHeight(frameRect.height, scrollPaneRect.height));
			System.out.println("Button Panel Bounds: " + rectangle);
			return rectangle;
		}

		private int getButtonPanelHeight(int frameHeight, int scrollPaneHeight) {
			int panelHeight = frameHeight - scrollPaneHeight;
			panelHeight = panelHeight > MAX_BUTTON_PANEL_HEIGHT ? MAX_BUTTON_PANEL_HEIGHT : panelHeight;
			return panelHeight;
		}

		private final static String BUTTON_NAME = "Save Button";
		private final static String BUTTON_TEXT = "Save";

		private final static float BUTTON_WIDTH = 0.50f;
		private final static float BUTTON_HEIGHT = 0.35f;

		private JButton getSaveButton(Rectangle panelRect,Map<String,String> channelMap) {
			JButton button = new JButton();
			button.setName(BUTTON_NAME);
			button.setText(BUTTON_TEXT);
			button.setBounds(getButtonBounds(panelRect));
			button.addActionListener(getButtonAction(channelMap));
			button.setEnabled(true);
			//button.setVisible(true);
			return button;
		}
		public void getPermit() throws InterruptedException{
			semaphore.acquire();
		}
		private boolean buttonClicked = false;

		private AbstractAction getButtonAction(Map<String,String> channelMap) {
			return new AbstractAction() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					executor.execute(() -> {
						buttonClicked = true;
						JTable table = (JTable) GUIUtilities.getComponentByName(ChannelFrame.this, TABLE_NAME);
						channelMap.putAll(getTableInputs(table));
						semaphore.release();
						dispose();
					});
				}
			};
		}

		@Override
		public void dispose() {
			super.dispose();
			semaphore.release();
			if (!buttonClicked) {
				executor.shutdown();
			}
		}

		private Rectangle getButtonBounds(Rectangle panelRect) {
			Rectangle buttonBounds = new Rectangle(getButtonX(panelRect.width), getButtonY(panelRect.height),
					getButtonWidth(panelRect.width), getButtonHeight(panelRect.height));
			System.out.println("Button Bounds: "+buttonBounds);
			return buttonBounds;
		}

		private int getButtonWidth(int panelWidth) {
			return (int) (panelWidth * BUTTON_WIDTH);
		}

		private int getButtonHeight(int panelHeight) {
			return (int) (panelHeight * BUTTON_HEIGHT);
		}

		private int getButtonX(int panelWidth) {
			int buttonX = (int) ((panelWidth - getButtonWidth(panelWidth)) / 2f);
			return buttonX;

		}

		private int getButtonY(int panelHeight) {
			int buttonY = (int) ((panelHeight - getButtonHeight(panelHeight)) / 2f);
			return buttonY;
		}

		private final static int NUM_COLUMNS = 2;
		private final static int NUM_ROWS = 100;

		private final static String SCROLL_PANE_NAME = "Channel_Mapping_Scroll";
		private final static String USER_DEFINED_CHANNEL = "User Defined Channel";

		private JScrollPane constrScrollPane(Rectangle frameRect, Collection<String> headers,Collection<String> mnemonics) {
			JTable table = constrChannelTable(frameRect, headers,mnemonics);
			JScrollPane scrollPane = new JScrollPane(table);
			scrollPane.setName(SCROLL_PANE_NAME);
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			scrollPane.setBounds(getScrollPaneBounds(frameRect));
			return scrollPane;
		}

		//private final static int MIN_BUTTON_PANEL_HEIGHT = 100;

		private int getScrollPaneHeight(Rectangle frameRect) {
			int scrollHeight = (int) (frameRect.getHeight() * (1.0f - BUTTON_PANEL_MARGIN));
			//scrollHeight = (frameRect.getHeight() - scrollHeight) < MIN_BUTTON_PANEL_HEIGHT ? ((int)(frameRect.getHeight()-MIN_BUTTON_PANEL_HEIGHT)) : scrollHeight;
			return scrollHeight;
		}

		private Rectangle getScrollPaneBounds(Rectangle frameRect) {
			int scrollHeight = getScrollPaneHeight(frameRect);
			System.out.println("Scroll Height: " + scrollHeight);
			Rectangle rectangle = new Rectangle(0, 0, frameRect.width - 15, scrollHeight);
			return rectangle;
		}

		private final static String TABLE_NAME = "Channel_Mapping";

		private JTable constrChannelTable(Rectangle frameRect, Collection<String> headers,Collection<String> mnemonics) {
			JTable table = new JTable(NUM_ROWS, NUM_COLUMNS);
			setMnemonicsInTable(table,mnemonics);
			setColumnProps(table, headers, frameRect.width);
			table.setCellSelectionEnabled(true);
			table.setName(TABLE_NAME);
			return table;
		}
		private final static int KEY_COLUMN = 0;
		private void setMnemonicsInTable(JTable table,Collection<String> mnemonics) {
			if(mnemonics.isEmpty()) {
				return;
			}
			int i = 0;
			for(String s:mnemonics) {
				table.setValueAt(s, i, KEY_COLUMN);
				i++;
			}
		}
		//private final static String CHANNEL_POPUP_MENU = "channel_add_popup_menu";

		private static String getAddChanString(String cellString, String addChann) {

			if (cellString.equals("")) {
				return addChann;
			}
			return cellString + ";" + addChann;
		}

		//private final static String ADD_CHANNEL_TEXT = "Add Channel";

		private static String getTableValueAtPoint(int x, int y, JTable table) {
			int row = getRowAtY(y, table);
			int col = getColAtX(x, table);
			String cellValue = getValue(table, row, col);
			System.out.println("Cell Value: " + cellValue);
			return cellValue;
		}

		private static void setTableValueAtPoint(int x, int y, JTable table, String value) {
			int row = getRowAtY(y, table);
			int col = getColAtX(x, table);
			table.setValueAt(value, row, col);
		}

		private static int getColAtX(int x, JTable table) {
			int w = 0;
			int col = 0;
			while (w < x) {
				w += table.getColumnModel().getColumn(col).getWidth();
				col++;
			}
			return col - 1;
		}

		private static int getRowAtY(int y, JTable table) {
			int rowHeight = table.getRowHeight();
			//int headerHeight = table.getTableHeader().getHeight();
			int netY = y;
			int row = netY / rowHeight;
			return row;
		}

		private MouseListener getAddChanMouseListener(String[] headers) {
			return new MouseListener() {

				@Override
				public void mouseClicked(MouseEvent e) {
					executor.execute(()->{
						JTable table = (JTable)e.getSource();
						String cellValue = getTableValueAtPoint(e.getPoint().x,e.getPoint().y,table);
						String selected = (String) JOptionPane.showInputDialog(null
								,"Select Channel to Add","Add Channel",JOptionPane.PLAIN_MESSAGE,null,headers,headers[0]);
						setTableValueAtPoint(e.getPoint().x,e.getPoint().y,table,getAddChanString(cellValue,selected));
					});
				}

				@Override
				public void mousePressed(MouseEvent e) {

					
				}

				@Override
				public void mouseReleased(MouseEvent e) {

					
				}

				@Override
				public void mouseEntered(MouseEvent e) {

					
				}

				@Override
				public void mouseExited(MouseEvent e) {
					
				}
				
			};
		}

		private PopupMenuListener getAssignValueAction(JTable table) {
			PopupMenuListener action = new PopupMenuListener() {
				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
					@SuppressWarnings("unchecked")
					JComboBox<String> comboBox = ((JComboBox<String>) e.getSource());
					System.out.println("Action Command: " + e.getSource());
					String selection = (String) comboBox.getSelectedItem();
					if (selection == null) {
						return;
					}
					executor.execute(() -> {
						if (selection.equals(OFFSET_WELL_PRESSURE)) {
							String input = getOffsetWellInput();
							addSetSelectedValue(comboBox, table, input);
						} else if (selection.equals(WELL_PRESSURE)) {
							String input = getWellInput();
							addSetSelectedValue(comboBox, table, input);
						} else if (selection.equals(USER_DEFINED_CHANNEL)) {

						}
					});

				}

				@Override
				public void popupMenuCanceled(PopupMenuEvent e) {
					// TODO Auto-generated method stub

				}
			};
			return action;
		}

		private static void addSetSelectedValue(JComboBox<String> comboBox, JTable table, String input) {
			if (input == null) {
				return;
			}
			SwingWorker<Void, String> worker = new SwingWorker<>() {
				public Void doInBackground() {
					publish(input);
					return null;
				}

				public void process(List<String> chunks) {
					System.out.println("This is the input that should be displaying in the cell: " + input);
					comboBox.addItem(chunks.get(0));
					System.out.println("Last Item in the List: " + comboBox.getItemAt(comboBox.getItemCount() - 1));
				}

				@Override
				protected void done() {
					int col = table.getSelectedColumn();
					int row = table.getSelectedRow();
					table.setValueAt(comboBox.getItemAt(comboBox.getItemCount() - 1), row, col);
					super.done();
				}

			};
			worker.execute();
		}

		private final static String OFFSET_PREFACE = "Offset_";

		private static String getOffsetWellInput() {
			String input = JOptionPane.showInputDialog("Input the offset well's name");
			return input == null ? null : OFFSET_PREFACE + input;
		}

		private final static String WELL_PREFACE = "Frac_";

		private static String getWellInput() {
			String input = JOptionPane.showInputDialog("Input the well's name");
			return input == null ? null : WELL_PREFACE + input;
		}

		private final static int V_SCROLL_WIDTH = 45;

		private static int getColumnWidth(int frameWidth) {
			return (frameWidth - V_SCROLL_WIDTH) / 2;
		}

		private final static int CONST_COLUMN = ChannelFrame.KEY_COLUMN;
		private final static int CHANNEL_COLUMN = 1;

		private final static String CONST_COLUMN_HEADER = "Channels";
		private final static String DATA_HEADERS = "Data Headers";

		private void setColumnProps(JTable table, Collection<String> headers, int frameWidth) {
			for (int i = 0; i < table.getColumnCount(); i++) {
				table.getColumnModel().getColumn(i).setMinWidth(getColumnWidth(frameWidth));
				if (i == CONST_COLUMN) {
					table.getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(getTableCellEditor(
							CONST_COLUMN_HEADER, table, getAssignValueAction(table), TREATING_PRESSURE, SLURRY_RATE,
							CLEAN_RATE, PROPPANT_CONC, SLURRY_TOTAL, CLEAN_TOTAL, CLEAN_STAGE_TOTAL, SLURRY_STAGE_TOTAL,
							BLENDER_STAGE, WELL_PRESSURE, OFFSET_WELL_PRESSURE, WELL_NAME, USER_DEFINED_CHANNEL)));

					table.getColumnModel().getColumn(i).setHeaderValue(CONST_COLUMN_HEADER);

				} else {
					table.getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(
							getTableCellEditor(DATA_HEADERS, table, getAssignValueAction(table), headers.toArray())));
					table.getColumnModel().getColumn(i).setHeaderValue(DATA_HEADERS);
				}
			}
			table.addMouseListener(getAddChanMouseListener(getStringArr(headers.toArray())));

		}
		private static String[] getStringArr(Object[] objArr) {
			String[] stringArr = new String[objArr.length];
			int i =0;
			for(Object obj:objArr) {
				stringArr[i] = (String)obj;
				i++;
			}
			return stringArr;
		}
		/*
		 * private static JComboBox<String> getTableCellEditor(String name, Object...
		 * options) { JComboBox<String> comboBox = new JComboBox<>(); for (Object s :
		 * options) { comboBox.addItem((String) s); } comboBox.setName(name);
		 * comboBox.setAlignmentX(JComboBox.CENTER_ALIGNMENT); ((JTextField)
		 * comboBox.getEditor()).setHorizontalAlignment(JTextField.CENTER);
		 * comboBox.setEnabled(true); return comboBox; }
		 */

		private JComboBox<String> getTableCellEditor(String name, JTable table, PopupMenuListener action,
				Object... options) {
			JComboBox<String> comboBox = new JComboBox<>();
			for (Object s : options) {
				comboBox.addItem((String) s);
			}
			comboBox.addPopupMenuListener(action);
			comboBox.setName(name);
			comboBox.setAlignmentX(JComboBox.CENTER_ALIGNMENT);
			comboBox.setEditable(false);
			comboBox.setEnabled(true);
			return comboBox;
		}
		
		

	}
	static class DataChannels implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 6897902002806508403L;
		Map<String,String> channelMap;
		Map<String,String> offsetMap;
		Map<String,String> definedMap;
		Map<String,String> wellPressMap;
		
		public final static String FRAC_CHANNELS = "channel_map";
		public final static String OFFSET_CHANNELS = "offset_map";
		public final static String DEFINED_CHANNELS = "defined_map";
		public final static String WELL_PRESSURE_CHANNELS = "well_pressure_map";
		
		String projectName;
		String baseDir;
		DataChannels(String projectName){
			channelMap = new LinkedHashMap<>();
			offsetMap = new LinkedHashMap<>();
			definedMap = new LinkedHashMap<>();
			wellPressMap = new LinkedHashMap<>();
			this.projectName = projectName;
			setBaseDir(projectName);
		}
		
		public static DataChannels getDataChannelsInst(String projectName) throws ClassNotFoundException, IOException {
			if(!checkForFile(projectName)) {
				return new DataChannels(projectName);
			}
			return loadChannels(projectName);
		}
		public static Map<String,Map<String,String>> getDataChannelMaps(String projectName) throws IOException,ClassNotFoundException{
			Map<String,Map<String,String>> map = new HashMap<>();
			DataChannels dataChannels = loadChannels(projectName);
			map.put(FRAC_CHANNELS, dataChannels.getChannelMap(FRAC_CHANNELS));
			map.put(OFFSET_CHANNELS, dataChannels.getChannelMap(OFFSET_CHANNELS));
			map.put(DEFINED_CHANNELS, dataChannels.getChannelMap(DEFINED_CHANNELS));
			map.put(WELL_PRESSURE_CHANNELS, dataChannels.getChannelMap(WELL_PRESSURE_CHANNELS));
			return map;
		}
		public List<String> getListOfChannelsUsed(){
			List<String> channels = new ArrayList<>();
			channels.addAll(getListFromMap(FRAC_CHANNELS));
			channels.addAll(getListFromMap(OFFSET_CHANNELS));
			channels.addAll(getListFromMap(DEFINED_CHANNELS));
			channels.addAll(getListFromMap(WELL_PRESSURE_CHANNELS));
			return channels;
		}
		private List<String> getListFromMap(String mapName){
			List<String> list = new ArrayList<>();
			Map<String,String> map = getChannelMap(mapName);
			for(String s:map.values()) {
				if(s.contains(";")) {
					addValuesToList(list,s.split(";"));
					continue;
				}
				addValuesToList(list,s);
			}
			list.add(DataRequest.DataChannels.TIME.getValue());
			return list;
		}
		private void addValuesToList(List<String> list,String...add) {
			for(String s:add) {
				list.add(s);
			}
		}
		
		private void setBaseDir(String projectName) {
			baseDir = Project.PROJECTS_DIR+projectName+"/"+CHANNEL_DIR;
		}
		public String getChannelFileDir() {
			return baseDir+DATA_CHANNEL_FILE;
		}
		public static String getChannelFileDir(String projectName) {
			return Project.PROJECTS_DIR+projectName+"/"+CHANNEL_DIR+DATA_CHANNEL_FILE;
		}
		static String getChannelDir(String projectName) {
			return Project.PROJECTS_DIR+projectName+"/"+CHANNEL_DIR;
		}
		public final static String CHANNEL_DIR = "channels/";
		public final static String DATA_CHANNEL_FILE = "data_channels.map";
		void saveChannels() throws IOException{
			checkForDir();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(new File(getChannelFileDir())));
			objectOutputStream.writeObject(this);
			objectOutputStream.close();
		}
		static boolean checkForFile(String projectName) {
			String filePath = getChannelFileDir(projectName);
			File file = new File(filePath);
			return file.exists();
		}
		public static DataChannels loadChannels(String projectName) throws ClassNotFoundException,IOException{
			if(!checkForFile(projectName)) {
				return null;
			}
			ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(new File(getChannelFileDir(projectName))));
			DataChannels dataChannels = (DataChannels)objectInputStream.readObject();
			objectInputStream.close();
			return dataChannels;
		}
		public Map<String,String> getChannelMap(String name){
			switch(name) {
			case FRAC_CHANNELS:
				return channelMap;
			case OFFSET_CHANNELS:
				return offsetMap;
			case DEFINED_CHANNELS:
				return definedMap;
			case WELL_PRESSURE_CHANNELS:
				return wellPressMap;
			default:
				return channelMap;
			}
		}

		private void checkForDir() {
			File file = new File(baseDir);
			if(!file.exists()) {
				file.mkdirs();
			}
		}
	}
	
	public static List<String> getMnemonics(){
		List<String> list = new ArrayList<>();
		list.add(TREATING_PRESSURE);
		list.add(SLURRY_RATE);
		list.add(CLEAN_RATE);
		list.add(PROPPANT_CONC);
		list.add(SLURRY_TOTAL);
		list.add(CLEAN_TOTAL);
		list.add(CLEAN_STAGE_TOTAL);
		list.add(SLURRY_STAGE_TOTAL);
		list.add(BLENDER_STAGE);
		return list;
	}
	
	public final static String TREATING_PRESSURE = "Treating Pressure";
	public final static String SLURRY_RATE = "Slurry Rate";
	public final static String CLEAN_RATE = "Clean Rate";
	public final static String PROPPANT_CONC = "Proppant Concentration";
	public final static String SLURRY_TOTAL = "Slurry Grand Total";
	public final static String CLEAN_TOTAL = "Clean Grand Total";
	public final static String CLEAN_STAGE_TOTAL = "Clean Stage Total";
	public final static String SLURRY_STAGE_TOTAL = "Slurry Stage Total";
	public final static String BLENDER_STAGE = "Blender Stage";
	public final static String WELL_PRESSURE = "Well Pressure";
	public final static String OFFSET_WELL_PRESSURE = "Offset Well Pressure";
	public final static String WELL_NAME = "Well Name";
}
