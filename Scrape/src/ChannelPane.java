import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

public class ChannelPane implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private JFrame channelFrame = new JFrame();
	private JTabbedPane tabbedPane = new JTabbedPane();
	private JPanel mainPanel = new JPanel();
	private JPanel savePanel = new JPanel();
	private JTable channelTable = new JTable(10, 2);
	private JTable backChannelTable = new JTable(100, 2);
	private JPanel backPanel = new JPanel();
	private JButton saveButton = new JButton();
	private HashMap<String, String> channelMap = new HashMap<>();
	private TreeMap<String, String> channelMnemonics;
	private CellComboBox cellComboBox;
	private HashMap<String, String> savedBacksides;
	private Boolean backsides;
	private String fileName;
	SerSpringLayout backLayout;
	SerSpringLayout channelLayout;
	SerSpringLayout frameLayout;

	public ChannelPane(TreeMap<String, String> channelMnemonics) {
		this.channelMnemonics = channelMnemonics;
		this.fileName = "channels.txt";
		backsides = true;
		constructMainPanel();
		tabbedPane.add(mainPanel);
		constructBacksideChannels();
		tabbedPane.add(backPanel);
		tabbedPane.add(constructChemicalPanel(FracCalculations.getArrayOfStringKeys(channelMnemonics)));
		tabbedNittyGritty();
		channelFrame.add(savePanel);
		channelFrame.add(tabbedPane);
		frameNittyGritty();

	}

	public ChannelPane(TreeMap<String, String> channelMnemonics, HashMap<String, String> savedBacksides) {
		this.channelMnemonics = channelMnemonics;
		this.savedBacksides = savedBacksides;
		this.fileName = "channels.txt";
		backsides = true;
		constructMainPanel();
		tabbedPane.add(mainPanel);
		constructBacksideChannels();
		tabbedPane.add(backPanel);
		tabbedPane.add(constructChemicalPanel(FracCalculations.getArrayOfStringKeys(channelMnemonics)));
		tabbedNittyGritty();
		channelFrame.add(savePanel);
		channelFrame.add(tabbedPane);
		frameNittyGritty();
	}

	public ChannelPane(String fileName) {
		this.channelMnemonics = null;
		this.fileName = fileName;
		backsides = false;
		constructMainPanel();
		tabbedPane.add(mainPanel);
		tabbedNittyGritty();
		channelFrame.add(savePanel);
		channelFrame.add(tabbedPane);
		frameNittyGritty();
	}

	private void tabbedNittyGritty() {
		tabbedPane.setTitleAt(0, "Main Channels");
		if (backsides) {
			tabbedPane.setTitleAt(1, "Backside Assignment");
			tabbedPane.setTitleAt(2, "Chem. Assignment");
		}
	}

	public void constructBacksideChannels() {
		JScrollPane scrollPane = constructBackTable();
		constructBackPanel();
		backPanel.add(scrollPane);
		constructBackLayout(backPanel, scrollPane);
	}

	public void constructBackPanel() {
		backPanel.setBounds(0, 0, 400, 180);
		backPanel.setOpaque(true);
		backPanel.setVisible(true);
	}

	private JButton constructUpdateButton() {
		JButton button = new JButton();
		button.setText("Update Wells");
		button.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> pathWells = null;
				try {
					pathWells = WellListSelection.getWellsInPathWithFilter("\\d");
				} catch (IOException e1) {
					System.out.println("Exception caught ChannelPane::WellListSelection::getWellsInPathWithFilter");
					return;
				}
				addWellsToBacksideTable(pathWells);
			}
		});
		button.setVisible(true);
		button.setSize(100, 35);
		return button;
	}

	private void addWellsToBacksideTable(ArrayList<String> wells) {
		int i = 0;
		for (String s : wells) {
			backChannelTable.setValueAt(s, i, 0);
			i++;
		}
	}

	private JScrollPane constructBackTable() {
		backChannelTable.setName("backChannels");
		backChannelTable.getColumnModel().getColumn(0).setHeaderValue("Wells");
		backChannelTable.getColumnModel().getColumn(1).setHeaderValue("Backside Channel");
		backChannelTable.addKeyListener(new TableKeyPressed(backChannelTable));
		if (savedBacksides != null) {
			int i = 0;
			for (String s : savedBacksides.keySet()) {
				backChannelTable.setValueAt(s, i, 0);
				backChannelTable.setValueAt(savedBacksides.get(s), i, 1);
				i++;
			}
		}
		CellComboBox comboBox = new CellComboBox(backChannelTable, 1, getPressureChannels(channelMnemonics));
		backChannelTable.setVisible(true);
		JScrollPane scrollPane = new JScrollPane(backChannelTable);
		return scrollPane;
	}

	public HashMap<String, String> getBacksideMap() {
		HashMap<String, String> backsideMap = new HashMap<>();
		int i = 0;
		while (backChannelTable.getValueAt(i, 0) != null && backChannelTable.getValueAt(i, 0).toString() != ""
				&& backChannelTable.getValueAt(i, 1) != null
				&& !backChannelTable.getValueAt(i, 1).toString().equals("")) {
			backsideMap.put(backChannelTable.getValueAt(i, 0).toString().toLowerCase(),
					backChannelTable.getValueAt(i, 1).toString().replace("\\n", ""));
			i++;
		}
		return backsideMap;
	}

	public static void writeObjectToFile(ChannelPane channelPane) throws IOException {
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(
				new FileOutputStream(new File("C:\\Scrape\\channels.scp")));
		objectOutputStream.writeObject(channelPane);
		objectOutputStream.close();
	}

	public static void setBacksideWells(ChannelPane channelPane, ArrayList<String> viewedWells) {
		if (viewedWells == null || viewedWells.isEmpty()) {
			return;
		}
		int i = 0;
		for (String s : viewedWells) {
			channelPane.backChannelTable.setValueAt(s, i, 0);
		}
	}

	public static ChannelPane readObjectFromFile(ArrayList<String> viewedWells)
			throws IOException, ClassNotFoundException {
		if (!checkFile()) {
			return null;
		}
		ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream("C:\\Scrape\\channels.scp"));
		ChannelPane channelPane = (ChannelPane) objectInputStream.readObject();
		setBacksideWells(channelPane, viewedWells);
		return channelPane;
	}

	public final static String ADD_CHANNELS_FILENAME = "add_channels.txt";

	public static void writeAddMnemonics(ArrayList<String> addChannels, String fileName) throws IOException {
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(new File(fileName)));
		objectOutputStream.writeObject(addChannels);
		objectOutputStream.close();
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<String> readAddMnemonics(String fileName) throws IOException, ClassNotFoundException {
		File file = new File(fileName);
		if (!file.exists()) {
			return new ArrayList<>();
		}
		ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
		ArrayList<String> array = (ArrayList<String>) objectInputStream.readObject();
		objectInputStream.close();
		return array;
	}

	public static ChannelPane readObjectFromFile() throws IOException, ClassNotFoundException {
		if (!checkFile()) {
			return null;
		}
		ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream("C:\\Scrape\\channels.scp"));
		ChannelPane channelPane = (ChannelPane) objectInputStream.readObject();
		return channelPane;
	}

	private static Boolean checkFile() {
		File file = new File("C:\\Scrape\\channels.scp");
		return file.exists();
	}

	private ArrayList<String> getPressureChannels(TreeMap<String, String> channelMnemonics) {
		ArrayList<String> pressureChannels = new ArrayList<>();
		for (String s : channelMnemonics.keySet()) {
			Matcher matcher = Pattern.compile("PRESSURE (\\d+)").matcher(s.toUpperCase());
			if (matcher.find()) {
				pressureChannels.add(s);
			}
		}
		return pressureChannels;
	}

	void constructBackLayout(JPanel cont, JScrollPane scroll) {
		backLayout = new SerSpringLayout();
		backLayout.putConstraint(SpringLayout.NORTH, scroll, 0, SpringLayout.NORTH, cont);
		backLayout.putConstraint(SpringLayout.SOUTH, scroll, 180, SpringLayout.NORTH, cont);
		backLayout.putConstraint(SpringLayout.WEST, scroll, 0, SpringLayout.WEST, cont);
		backLayout.putConstraint(SpringLayout.EAST, scroll, 400, SpringLayout.WEST, cont);
		cont.setLayout(backLayout);
	}

	void constructMainPanel() {

		TableKeyPressed tableKeyPressed = new TableKeyPressed(channelTable);
		channelTable.setCellSelectionEnabled(true);
		if (backsides) {
			constructChannelTable(channelMnemonics);
		} else {
			constructChannelTable();
		}
		channelFrame.setTitle("Channels");
		ImageIcon scrape = new ImageIcon("Scrape.png");
		Image scrape1 = scrape.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
		channelFrame.setIconImage(scrape1);
		channelFrame.setBounds(400, 400, 420, 260);
		// mainPanel.setBounds(400,400,400,260);
		channelTable.getColumnModel().getColumn(0).setHeaderValue("Channels");
		channelTable.getColumnModel().getColumn(1).setHeaderValue("Channel Name");
		saveButton.setSize(100, 35);
		saveButton.setHorizontalAlignment(SwingConstants.CENTER);
		saveButton.setVerticalAlignment(SwingConstants.CENTER);
		saveButton.setVisible(true);
		saveButton.setText("SAVE");
		SaveChannels saveC = new SaveChannels();
		saveButton.addActionListener(saveC);
		savePanel.add(saveButton);
		if (backsides) {
			savePanel.add(constructUpdateButton());
		}
		savePanel.setLayout(new FlowLayout());
		savePanel.setBackground(Color.DARK_GRAY);
		savePanel.setVisible(true);

		JScrollPane channelScroll = new JScrollPane(channelTable);
		// channelScroll.setBounds(0, 0, channelFrame.getWidth(),(int)
		// Math.round(channelFrame.getHeight() * .9));
		channelScroll.setVisible(true);
		mainPanel.add(channelScroll);
		// mainPanel.add(savePanel);
		setChannelLayout(channelScroll, mainPanel);

	}

	void setChannelLayout(JScrollPane channelScroll, JPanel mainPanel) {
		channelLayout = new SerSpringLayout();
		channelLayout.putConstraint(SpringLayout.NORTH, channelScroll, 0, SpringLayout.NORTH, mainPanel);
		channelLayout.putConstraint(SpringLayout.SOUTH, channelScroll, 180, SpringLayout.NORTH, mainPanel);
		channelLayout.putConstraint(SpringLayout.WEST, channelScroll, 0, SpringLayout.WEST, mainPanel);
		channelLayout.putConstraint(SpringLayout.EAST, channelScroll, 400, SpringLayout.WEST, mainPanel);
		mainPanel.setLayout(channelLayout);
	}

	private void setFrameLayout(JPanel savePanel, JFrame channelFrame) {
		frameLayout = new SerSpringLayout();
		frameLayout.putConstraint(SpringLayout.NORTH, savePanel, 180, SpringLayout.NORTH, channelFrame);
		frameLayout.putConstraint(SpringLayout.SOUTH, savePanel, 220, SpringLayout.NORTH, channelFrame);
		frameLayout.putConstraint(SpringLayout.WEST, savePanel, 0, SpringLayout.WEST, channelFrame);
		frameLayout.putConstraint(SpringLayout.EAST, savePanel, 400, SpringLayout.WEST, channelFrame);
		frameLayout.putConstraint(SpringLayout.NORTH, tabbedPane, 0, SpringLayout.NORTH, channelFrame);
		frameLayout.putConstraint(SpringLayout.SOUTH, tabbedPane, 180, SpringLayout.NORTH, channelFrame);
		frameLayout.putConstraint(SpringLayout.WEST, tabbedPane, 0, SpringLayout.WEST, channelFrame);
		frameLayout.putConstraint(SpringLayout.EAST, tabbedPane, 400, SpringLayout.WEST, channelFrame);
		channelFrame.setLayout(frameLayout);
	}

	private void frameNittyGritty() {
		setFrameLayout(savePanel, channelFrame);
		channelFrame.setResizable(false);
		channelFrame.setVisible(false);
		channelFrame.setIconImage(
				new ImageIcon("C:\\Scrape\\Scrape.png").getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
		channelFrame.setAlwaysOnTop(true);
		if (fileName.equals("channels.txt")) {
			channelFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		} else {
			channelFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		}
	}

	public void setChannelMnemonics(TreeMap<String, String> channelMnemonics) {
		this.channelMnemonics = channelMnemonics;
	}

	public TreeMap<String, String> getChannelMnemonics() {
		return this.channelMnemonics;
	}

	public void constructChannelTable(ArrayList<String> allChannels) {
		channelMap = getChannelList(Main.yess.getWellName(), fileName);
		String[] channelDefault = getChannelDefault();
		int i = 0;
		for (String s : channelDefault) {
			channelTable.setValueAt(s, i, 0);
			channelTable.setValueAt(channelMap.get(s), i, 1);
			i++;
		}
		CellComboBox comboBox = new CellComboBox(channelTable, 1, allChannels);
	}

	public void constructChannelTable(ArrayList<String> allChannels, JTable table) {
		channelMap = getChannelList(Main.yess.getWellName(), fileName);
		String[] channelDefault = getChannelDefault();
		int i = 0;
		for (String s : channelDefault) {
			table.setValueAt(s, i, 0);
			table.setValueAt(channelMap.get(s), i, 1);
			i++;
		}
		CellComboBox comboBox = new CellComboBox(table, 1, allChannels);
	}

	private HashMap<String, HashSet<String>> getChemChannelsFromTable() {
		JTable table = (JTable) GUIUtilities.getComponentByName(tabbedPane, CHEMTABLE);
		HashMap<String, HashSet<String>> chemMap = new HashMap<>();
		for (int i = 0; i < table.getRowCount(); i++) {
			if ((table.getValueAt(i, 0) == null || table.getValueAt(i, 0).toString().equals(""))
					| (table.getValueAt(i, 1) == null || table.getValueAt(i, 1).toString().equals(""))) {
				continue;
			}
			addValueToMap(chemMap, table.getValueAt(i, 0).toString(), table.getValueAt(i, 1).toString());
		}
		return chemMap;
	}

	private void writeChemsToFile() throws IOException {
		HashMap<String, HashSet<String>> chemMap = getChemChannelsFromTable();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(new File(CHEMPATH)));
		objectOutputStream.writeObject(chemMap);
		objectOutputStream.flush();
		objectOutputStream.close();
	}

	private void addValueToMap(HashMap<String, HashSet<String>> map, String key, String value) {
		if (map.containsKey(key)) {
			map.get(key).add(value);
			return;
		}
		map.put(key, new HashSet<String>());
		map.get(key).add(value);
	}

	private ArrayList<String> getChemChannels(ArrayList<String> allChannels) {
		ArrayList<String> chemChannels = new ArrayList<>();
		for (String s : allChannels) {
			if (s.toLowerCase().contains("chem")) {
				chemChannels.add(s);
			}
		}
		return chemChannels;
	}

	public static final String CHEMTABLE = "chemTable";

	private JTable constructChemicalTable(ArrayList<String> chemChannels) {
		JTable table = new JTable(20, 2);
		table.setName(CHEMTABLE);
		table.getColumnModel().getColumn(0).setHeaderValue("Chem. Name");
		table.getColumnModel().getColumn(1).setHeaderValue("Chem. Channel");
		new CellComboBox(table, 1, chemChannels);
		fillChemTable(table);
		table.setCellSelectionEnabled(true);
		new TableKeyPressed(table);
		return table;
	}

	private void fillChemTable(JTable chemTable) {
		HashMap<String, HashSet<String>> chemMap = null;
		try {
			chemMap = getChemicalMap();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		int i = 0;
		for (String s : chemMap.keySet()) {
			for (String ss : chemMap.get(s)) {
				chemTable.setValueAt(s, i, 0);
				chemTable.setValueAt(ss, i, 1);
				i++;
			}
		}
	}

	private JScrollPane constructChemicalScroll(ArrayList<String> chemChannels) {
		JScrollPane scrollPane = new JScrollPane(constructChemicalTable(chemChannels));
		return scrollPane;
	}

	private JPanel constructChemicalPanel(ArrayList<String> allChannels) {
		JPanel panel = new JPanel();
		JScrollPane scrollPane = constructChemicalScroll(getChemChannels(allChannels));
		panel.add(scrollPane);
		setChannelLayout(scrollPane, panel);
		panel.setVisible(true);
		return panel;
	}

	public static String getUserCrew() throws IOException {
		File file = new File("C:\\Scrape\\crew.txt");
		if (!file.exists()) {
			return "PPS6";
		} else {
			return ArgumentsToText.readStringFromFile(file.getAbsolutePath());
		}
	}

	public ArrayList<String> getActualChannels(String wellName) {
		ArrayList<String> actualChannels = new ArrayList<>();
		int i = 0;
		HashMap<String, String> backsideMap = getBacksideMap();
		while (channelTable.getValueAt(i, 1) != null && !String.valueOf(channelTable.getValueAt(i, 1)).equals("")
				&& !String.valueOf(channelTable.getValueAt(i, 1)).equals("null") && i < channelTable.getRowCount()) {
			if (channelTable.getValueAt(i, 0).equals(BACKSIDE) && checkForKeyInMap(backsideMap.keySet(),wellName)) {
				actualChannels.add(channelMnemonics.get(getValueOfStandardizedKey(backsideMap,wellName)));
			} else {
				String key = String.valueOf(channelTable.getValueAt(i, 1));

				actualChannels.add(channelMnemonics.containsKey(key) ? channelMnemonics.get(key) : key);
			}
			i++;
		}

		return actualChannels;
	}

	public final static String BACKSIDE = "Backside";

	public HashMap<String, String> getActualChannelsMap(String wellName) {
		HashMap<String, String> actualChannels = new HashMap<>();
		wellName = wellName.toLowerCase();
		int i = 0;
		HashMap<String, String> backsideMap = getBacksideMap();
		while (channelTable.getValueAt(i, 1) != null && !String.valueOf(channelTable.getValueAt(i, 1)).equals("")
				&& !String.valueOf(channelTable.getValueAt(i, 1)).equals("null") && i < channelTable.getRowCount()) {
			if (channelTable.getValueAt(i, 0).toString().equals("Backside") && checkForKeyInMap(backsideMap.keySet(), wellName)) {
				actualChannels.put(channelMnemonics.get(getValueOfStandardizedKey(backsideMap, wellName)), BACKSIDE);
			} else {
				String key = String.valueOf(channelTable.getValueAt(i, 1));
				actualChannels.put(channelMnemonics.containsKey(key) ? channelMnemonics.get(key) : key,
						channelTable.getValueAt(i, 0).toString());
			}
			i++;
		}

		return actualChannels;
	}

	public String getValueOfStandardizedKey(Map<String, String> map, String key) {
		for (String s : map.keySet()) {
			if (mainFrame.removeSpecialCharacters(s).toLowerCase()
					.equals(mainFrame.removeSpecialCharacters(key).toLowerCase())) {
				return map.get(s);
			}
		}
		return null;
	}

	public boolean checkForKeyInMap(Set<String> mapKeySet, String key) {
		for (String s : mapKeySet) {
			if (mainFrame.removeSpecialCharacters(s).toLowerCase()
					.equals(mainFrame.removeSpecialCharacters(key).toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public HashMap<String, String> getReverseChannelMap(String backsideChannel) {
		HashMap<String, String> reverseMap = new HashMap<>();
		int i = 0;
		while (channelTable.getValueAt(i, 1) != null && !String.valueOf(channelTable.getValueAt(i, 1)).equals("")
				&& !String.valueOf(channelTable.getValueAt(i, 1)).equals("null") && i < channelTable.getRowCount()) {
			String channel = String.valueOf(channelTable.getValueAt(i, 0));
			if (channel.equals("Backside") && backsideChannel != null) {
				reverseMap.put(backsideChannel, channel);
				i++;
				continue;
			}
			reverseMap.put(String.valueOf(channelTable.getValueAt(i, 1)), channel);
			i++;
		}
		return reverseMap;
	}

	public static HashMap<String, HashSet<String>> getChemicalMap() throws IOException, ClassNotFoundException {
		if (!checkFile(CHEMPATH)) {
			return new HashMap<>();
		}
		ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(new File(CHEMPATH)));
		@SuppressWarnings("unchecked")
		HashMap<String, HashSet<String>> chemicalMap = (HashMap<String, HashSet<String>>) objectInputStream
				.readObject();
		return chemicalMap;
	}

	public LinkedHashMap<String, ArrayList<String>> getMapWithConstantKeys(
			LinkedHashMap<String, ArrayList<String>> dataMap, String wellName) {
		LinkedHashMap<String, ArrayList<String>> newDataMap = new LinkedHashMap<>();
		HashMap<String, String> reverseKeysMap = getActualChannelsMap(wellName);

		for (String key : dataMap.keySet()) {

			if (reverseKeysMap.get(key) != null) {
				newDataMap.put(reverseKeysMap.get(key), dataMap.get(key));

			}else {
				newDataMap.put(key, dataMap.get(key));
			}

		}
		return newDataMap;
	}

	public static String getKeyForValue(Map<String, String> map, String value) {
		for (String s : map.keySet()) {
			if (map.get(s).equals(value)) {
				return s;
			}
		}
		return null;
	}

	public static LinkedHashMap<String, ArrayList<String>> getMapWithConstantKeys(
			LinkedHashMap<String, ArrayList<String>> dataMap, HashMap<String, String> channelMap) {
		LinkedHashMap<String, ArrayList<String>> newDataMap = new LinkedHashMap<>();
		for (Map.Entry<String, ArrayList<String>> entry : dataMap.entrySet()) {
			if (channelMap.containsValue(entry.getKey())) {
				newDataMap.put(getKeyForValue(channelMap, entry.getKey()), entry.getValue());
				continue;
			}
			newDataMap.put(entry.getKey(), entry.getValue());
		}
		return newDataMap;
	}

	public void resetMnemonicChannels(TreeMap<String, String> channelMnemonics) {
		cellComboBox.resetKeyValues(channelMnemonics);
	}

	public int getRowAtPoint(Point point) {
		int y = point.y;
		int rowHeight = channelTable.getRowHeight();
		return y / rowHeight;
	}

	public MouseListener getTableMouseListener() {
		return new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				Executors.newSingleThreadExecutor().execute(() -> {
					if (e.getButton() > 1 & channelTable.getCellEditor() != null) {
						channelTable.getCellEditor().cancelCellEditing();
					} else if (channelTable.getCellEditor() == null) {
						return;
					}
					AdjustChannelsMenu adjustChannelsMenu = (AdjustChannelsMenu) channelTable.getComponentPopupMenu();
					int row;
					if ((row = channelTable.getEditingRow()) == -1) {
						row = channelTable.getSelectedRow() != -1 ? channelTable.getSelectedRow()
								: getRowAtPoint(e.getPoint());
						adjustChannelsMenu.setRow(row);
						return;
					}
					adjustChannelsMenu.setRow(row);
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

	public void addListenerToSubComps(MouseListener mouseListener, JComponent component) {
		component.addMouseListener(mouseListener);
		for (Component c : component.getComponents()) {
			System.out.println(c.getClass().getGenericSuperclass().toString());
			if (c.getClass().getGenericSuperclass().toString().equals("java.awt.Container")) {
				addListenerToSubComps(mouseListener, (JComponent) c);
			}
		}
	}

	public void constructChannelTable(TreeMap<String, String> channelMnemonics) {
		channelMap = getChannelList();
		String[] channelDefault = getChannelDefault();
		int i = 0;
		for (String s : channelDefault) {
			channelTable.setValueAt(s, i, 0);
			channelTable.setValueAt(channelMap.get(s), i, 1);
			i++;
		}
		channelTable.setComponentPopupMenu(new AdjustChannelsMenu());

		cellComboBox = new CellComboBox(channelTable, 1, channelMnemonics);
		cellComboBox.setEditable(true);
		addListenerToSubComps(getTableMouseListener(), channelTable);
	}

	public void constructChannelTable() {
		channelMap = getChannelList(fileName);
		String[] channelDefault = getChannelDefault();
		if (channelMap == null) {
			setKeysInTable(channelTable, channelDefault);
			return;
		}
		int i = 0;
		for (String s : channelDefault) {
			channelTable.setValueAt(s, i, 0);
			channelTable.setValueAt(channelMap.get(s), i, 1);
			i++;
		}
	}

	private void setKeysInTable(JTable table, String[] keys) {
		int i = 0;
		for (String s : keys) {
			table.setValueAt(s, i, 0);
			i++;
		}
	}

	private String[] getChannelDefault() {
		return new String[] { PROP_CONC,CLEAN_GRAND,TREATING_PRESSURE,SLURRY_RATE,SLURRY_GRAND,STAGE_NUMBER,BACKSIDE,CLEAN_RATE};
	}
	public final static String PROP_CONC = "Prop. Concentration";
	public final static String CLEAN_GRAND = "Clean Grand Total";
	public final static String TREATING_PRESSURE = "Treating Pressure";
	public final static String SLURRY_RATE = "Slurry Rate";
	public final static String SLURRY_GRAND = "Slurry Grand Total";
	public final static String STAGE_NUMBER = "Stage Number";
	public final static String CLEAN_RATE = "Clean Rate";
	public final static String TIMESTAMP = "timestamp";
	public static HashMap<String, String> readBacksideMap() throws IOException {
		if (!checkBacksideFile()) {
			return null;
		}
		HashMap<String, String> backsideMap = new HashMap<>();
		BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("C:\\Scrape\\backChannels.txt")));
		String temp;
		while ((temp = bufferedReader.readLine()) != null) {
			backsideMap.put(temp.split("_")[0].trim(), temp.split("_")[1].trim());
		}
		return backsideMap;
	}

	public static Boolean checkBacksideFile() {
		File file = new File("C:\\Scrape\\backChannels.txt");
		return file.exists();
	}

	public void saveBacksideChannels() throws IOException {
		FileWriter fileWriter = new FileWriter(new File("C:\\Scrape\\backChannels.txt"));
		fileWriter.write("");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		HashMap<String, String> backsideMap = getBacksideMap();
		for (String s : backsideMap.keySet()) {
			bufferedWriter.append(s);
			bufferedWriter.append("_");
			bufferedWriter.append(backsideMap.get(s));
			bufferedWriter.newLine();
		}
		bufferedWriter.flush();
		bufferedWriter.close();
	}

	public void saveChannels() throws IOException {

		FileWriter channels = new FileWriter(new File("C:\\Scrape\\" + fileName));
		BufferedWriter chan = new BufferedWriter(channels);
		int i = 0;
		chan.write("");
		while (i < channelTable.getRowCount() && channelTable.getValueAt(i, 0) != null) {

			chan.append(channelTable.getValueAt(i, 0).toString());
			chan.append("_");
			if (channelTable.getValueAt(i, 1) == null || channelTable.getValueAt(i, 1).equals("")) {
				chan.append("null");
				channelTable.setValueAt("null", i, 1);
			} else {
				chan.append(channelTable.getValueAt(i, 1).toString());
			}
			chan.append("\n");
			i++;
		}
		chan.close();
	}

	public static HashMap<String, String> getChannelList(String well, String fileName) {

		HashMap<String, String> backsideMap = null;
		try {
			backsideMap = readBacksideMap();
		} catch (IOException e) {
			System.out.println("Exception caught getting backside map");
		}

		HashMap<String, String> channelMap = new HashMap<>();

		try {
			BufferedReader channelRead = new BufferedReader(new FileReader("C:\\Scrape\\" + fileName));
			String channel;
			while ((channel = channelRead.readLine()) != null) {
				if (channel.split("_").length < 2) {
					channelMap.put(channel.split("_")[0].trim(), "null");
				} else if (channel.split("_")[0].trim().equals("Backside")) {
					channelMap.put(channel.split("_")[0].trim(),
							backsideMap != null && backsideMap.containsKey(well) ? backsideMap.get(well)
									: channel.split("_")[1].trim());
				} else {
					channelMap.put(channel.split("_")[0].trim(), channel.split("_")[1].trim());
				}
			}

		} catch (IOException e) {
			System.out.println("JOptionPane in ChannelPane");
			JOptionPane.showMessageDialog(null,
					"The file 'channels.txt' has been moved or deleted; \n"
							+ "go to 'View' -> 'Channels', type in the correct channel names in the second "
							+ "column for the corresponding channels in the first column, then save the list");
		}

		return channelMap;
	}

	public static HashMap<String, String> getChannelList() {

		HashMap<String, String> channelMap = new HashMap<>();

		try {
			BufferedReader channelRead = new BufferedReader(new FileReader("C:\\Scrape\\channels.txt"));
			String channel;
			while ((channel = channelRead.readLine()) != null) {
				if (channel.split("_").length < 2) {
					channelMap.put(channel.split("_")[0].trim(), "null");
				} else {
					channelMap.put(channel.split("_")[0].trim(), channel.split("_")[1].trim());
				}
			}

		} catch (IOException e) {
			System.out.println("JOptionPane in ChannelPane");
			JOptionPane.showMessageDialog(null,
					"The file 'channels.txt' has been moved or deleted; \n"
							+ "go to 'View' -> 'Channels', type in the correct channel names in the second "
							+ "column for the corresponding channels in the first column, then save the list");
		}

		return channelMap;
	}

	public static HashMap<String, String> getChannelList(String fileName) {
		if (!checkFile("C:\\Scrape\\" + fileName)) {
			return null;
		}
		HashMap<String, String> channelMap = new HashMap<>();

		try {
			BufferedReader channelRead = new BufferedReader(new FileReader("C:\\Scrape\\" + fileName));
			String channel;
			while ((channel = channelRead.readLine()) != null) {
				if (channel.split("_").length < 2) {
					channelMap.put(channel.split("_")[0].trim(), "null");
				} else {
					channelMap.put(channel.split("_")[0].trim(), channel.split("_")[1].trim());
				}
			}

		} catch (IOException e) {
			System.out.println("JOptionPane in ChannelPane");
			JOptionPane.showMessageDialog(null,
					"The file 'channels.txt' has been moved or deleted; \n"
							+ "go to 'View' -> 'Channels', type in the correct channel names in the second "
							+ "column for the corresponding channels in the first column, then save the list");
		}

		return channelMap;
	}

	public static Boolean checkFile(String path) {
		File file = new File(path);
		return file.exists();
	}

	public boolean isVisible() {
		return channelFrame.isVisible();
	}

	public void setVisible(boolean visible) {
		channelFrame.setVisible(visible);
	}

	public class AdjustChannelsMenu extends JPopupMenu {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private int row = 0;

		AdjustChannelsMenu() {
			construct();
		}

		public void setRow(int row) {
			this.row = row;
		}

		public void construct() {
			add(getCombineChannelMenu());
			add(getAddChannelMenu());
		}

		public final static String COMBINE_MENU_NAME = "combine_channel";

		JMenuItem getCombineChannelMenu() {
			JMenuItem menuItem = new JMenuItem();
			menuItem.setName(COMBINE_MENU_NAME);
			menuItem.setText("Add Channel to Combine");
			menuItem.addActionListener(new AbstractAction() {
				/**
				 *
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					Executors.newSingleThreadExecutor().execute(() -> {
						String[] channels = getPrimArrayOfString(channelMnemonics);
						String selected = (String) JOptionPane.showInputDialog(channelTable,
								"Select the channel that you want to combine", "Combine Channel",
								JOptionPane.PLAIN_MESSAGE, null, channels, channels[0]);
						if (selected == null || selected.equals("")) {
							return;
						}
						String cellValue = channelTable.getValueAt(row, 1) != null
								? channelTable.getValueAt(row, 1).toString()
								: "";
						if (cellValue.equals("")) {
							return;
						}
						channelTable.setValueAt(cellValue + "&&" + selected, row, 1);
					});
				}
			});
			setVisible(true);
			return menuItem;
		}

		public final static String ADD_MENU_NAME = "add_name";

		JMenuItem getAddChannelMenu() {
			JMenuItem menuItem = new JMenuItem();
			menuItem.setText("Add Channel To List");
			menuItem.setName(ADD_MENU_NAME);
			menuItem.addActionListener(new AbstractAction() {
				/**
				 *
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					Executors.newSingleThreadExecutor().execute(() -> {
						String addChannel = JOptionPane
								.showInputDialog("Input the name of the channel you would like to add to the list");
						try {
							ArrayList<String> addArray = readAddMnemonics(ADD_CHANNELS_FILENAME);
							addArray.add(addChannel);
							addChannelsToMnemonics(addArray);
							writeAddMnemonics(addArray, ADD_CHANNELS_FILENAME);
						} catch (IOException | ClassNotFoundException e1) {
							e1.printStackTrace();
							return;
						}
					});
				}
			});
			menuItem.setVisible(true);
			return menuItem;
		}

		public void addChannelsToMnemonics(ArrayList<String> addChannels) {
			if (addChannels == null) {
				return;
			}
			for (String s : addChannels) {
				channelMnemonics.put(s, s);
			}
		}

		String[] getPrimArrayOfString(Map<String, String> map) {
			String[] array = new String[map.size()];
			int i = 0;
			for (String s : map.keySet()) {
				array[i] = s;
				i++;
			}
			return array;
		}
	}

	public class SaveChannels extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			mainFrame.stopEditingTables(channelTable, backChannelTable);
			try {
				if (fileName.equals("channels.txt")) {
					saveBacksideChannels();
					writeChemsToFile();
				}
				saveChannels();
				if (fileName.equals("channels.txt")) {
					channelFrame.setVisible(false);
				} else {
					channelFrame.dispose();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	private class SerSpringLayout extends SpringLayout implements Serializable {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		SerSpringLayout() {

		}
	}

	public static final String CHEMPATH = "C:\\Scrape\\chemMap.chm";
}