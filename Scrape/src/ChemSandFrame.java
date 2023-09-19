import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;

public class ChemSandFrame extends JFrame {
	Rectangle rectangle;
	HashMap<String, LinkedHashMap<String, String>> sandChemMap;
	Semaphore semaphore;
	JTable chemTable;
	JTable sandTable;
	Boolean mapSet;
	ExecutorService executor = Executors.newCachedThreadPool();

	ChemSandFrame(Rectangle rectangle) throws IOException {
		mapSet = false;
		this.rectangle = rectangle;
		semaphore = new Semaphore(0);
		construct();
	}

	ChemSandFrame(Rectangle rectangle, LinkedHashMap<String, String> sandMap, LinkedHashMap<String, String> chemMap) {
		mapSet = false;
		this.rectangle = rectangle;
		semaphore = new Semaphore(0);
		construct(sandMap, chemMap);
	}

	ChemSandFrame(Rectangle rectangle, HashMap<String, LinkedHashMap<String, String>> sandChemMap) {
		mapSet = false;
		this.rectangle = rectangle;
		semaphore = new Semaphore(0);
		construct(sandChemMap);
	}

	public final static String SAND_NAME = "sand";
	public final static String CHEM_NAME = "chemicals";

	private void construct(LinkedHashMap<String, String> sandMap, LinkedHashMap<String, String> chemMap) {
		chemTable = constructTable("chemicals", 3);
		sandTable = constructTable("sand", 3, '[', ']');
		nittyGritty();
		populateTables(sandMap, chemMap);
		constructSaveButton();
		this.setLayout(new ChemSandLayout());
		this.setVisible(true);
	}

	public final static String DUPLICATE_REGEX = "\\[\\#(\\d+)\\]";

	private void construct(HashMap<String, LinkedHashMap<String, String>> chemSandMap) {
		chemTable = constructTable("chemicals", 3);
		sandTable = constructTable("sand", 3, '[', ']');
		nittyGritty();
		populateTables(chemSandMap.get("sand"), chemSandMap.get("chemicals"));
		populateTable(sandTable, chemSandMap.get("design"), 2);
		populateTable(chemTable, chemSandMap.get("chemUnits"), 2);
		constructSaveButton();
		this.setLayout(new ChemSandLayout());
		this.setVisible(true);
	}

	private void construct() throws IOException {
		chemTable = constructTable("chemicals", 3);
		sandTable = constructTable("sand", 3, '[', ']');
		nittyGritty();
		populateTables();
		constructSaveButton();
		this.setLayout(new ChemSandLayout());
		this.setVisible(true);
	}

	private void populateTables(LinkedHashMap<String, String> sandMap, LinkedHashMap<String, String> chemMap) {
		populateTable(sandTable, sandMap);
		populateTable(chemTable, chemMap);
	}

	private void populateTable(JTable table, LinkedHashMap<String, String> map) {
		int i = 0;
		for (String s : map.keySet()) {
			if (s.toUpperCase().contains("TOTAL")) {
				continue;
			}
			Matcher matcher = Pattern.compile(DUPLICATE_REGEX).matcher(s);
			if (matcher.find()) {
				table.setValueAt(s.substring(0, matcher.start()).trim(), i, 0);
			} else {
				table.setValueAt(s, i, 0);
			}
			table.setValueAt(map.get(s), i, 1);
			i++;
		}
	}

	private void populateTable(JTable table, LinkedHashMap<String, String> map, int col) {
		int i = 0;
		for (String s : map.keySet()) {
			if (s.toUpperCase().contains("TOTAL")) {
				continue;
			}
			table.setValueAt(map.get(s), i, col);
			i++;
		}
	}

	public final static String CHEMICAL_UNITS_NAME = "chemUnits";
	public final static String SAND_DESIGN_NAME = "design";
	public final static String CHEMICALS = "C:\\Scrape\\chemicals.txt";
	public final static String SAND = "C:\\Scrape\\sand.txt";
	public final static String CHEMICAL_UNITS = "C:\\Scrape\\chemical_units.txt";
	public final static String SAND_DESIGN = "C:\\Scrape\\sand_design.txt";

	private void populateTables() throws IOException {
		ArrayList<String> chemArray = readSavedNamesFromFile(CHEMICALS);
		ArrayList<String> unitsArray = readSavedNamesFromFile(CHEMICAL_UNITS);
		ArrayList<String> sandArray = readSavedNamesFromFile(SAND);
		ArrayList<String> sandDesArray = readSavedNamesFromFile(SAND_DESIGN);
		populateTable(chemTable, chemArray);
		populateTable(chemTable, unitsArray, 2);
		populateTable(sandTable, sandArray);
		populateTable(sandTable, sandDesArray, 2);
	}

	private void nittyGritty() {
		setBounds(rectangle);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setIconImage(new ImageIcon("C:\\Scrape\\Scrape.png").getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
		setTitle("Chemical/Sand Usage");
		getContentPane().setBackground(Color.getHSBColor(-.85f, .1f, .85f));
	}

	private Runnable getButtonRunnable() {
		return () -> {
			mainFrame.stopEditingTables(chemTable, sandTable);
			setChemSandMap(getTableMap(chemTable, 2), getTableMap(chemTable, 1), getTableMap(sandTable, 1),
					getTableMap(sandTable, 2));
			try {
				writeSavedNamesToFile(getTableValues(chemTable), "C:\\Scrape\\chemicals.txt");
				writeSavedNamesToFile(getTableValues(sandTable), "C:\\Scrape\\sand.txt");
				writeSavedNamesToFile(getTableValues(sandTable, 2), "C:\\Scrape\\sand_design.txt");
				writeSavedNamesToFile(getTableValues(chemTable, 2), "C:\\Scrape\\chemical_units.txt");
			} catch (IOException e1) {
				System.out.println("ONONONONONO");
			}
			dispose();

		};

	}
	private static String capWords(String string) {
		Matcher matcher = Pattern.compile("(^[a-z])|(\\s[a-z])").matcher(string);
		while (matcher.find()) {
			string = (matcher.start()==0?"":string.substring(0, matcher.start()+1)) + String.valueOf(string.charAt(matcher.end()-1)).toUpperCase()
					+ string.substring(matcher.end()).toLowerCase();
			matcher.reset(string);
		}
		return string;
	}
	private void constructSaveButton() {
		JButton button = new JButton();
		button.setText("SAVE");
		button.setName("button");
		button.addActionListener(new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = -7571174979401327891L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					executor.execute(getButtonRunnable());
				} catch (RejectedExecutionException e1) {
					System.out.println("Rejected");
				}
			}
		});
		button.setVisible(true);
		this.add(button);
	}

	private void setChemSandMap(LinkedHashMap<String, String> units, LinkedHashMap<String, String> chemMap,
			LinkedHashMap<String, String> sandMap, LinkedHashMap<String, String> designSandMap) {
		this.sandChemMap = new HashMap<>();
		this.sandChemMap.put("chemicals", chemMap);
		this.sandChemMap.put("chemUnits", units);
		this.sandChemMap.put("sand", sandMap);
		this.sandChemMap.put("design", designSandMap);
		semaphore.release();
	}

	public HashMap<String, LinkedHashMap<String, String>> getChemSandMap() throws InterruptedException {
		semaphore.acquire();
		semaphore.release();
		return sandChemMap;
	}

	private LinkedHashMap<String, String> getTableMap(JTable table, int valueCol) {
		LinkedHashMap<String, String> values = new LinkedHashMap<>();
		ArrayList<String> typeArray = getValuesInColumn(table, 0);
		for (int i = 0; i < table.getRowCount(); i++) {
			if (table.getValueAt(i, 0) == null || String.valueOf(table.getValueAt(i, 0)).equals("")) {
				continue;
			}
			if (table.getValueAt(i, valueCol) == null || String.valueOf(table.getValueAt(i, valueCol)).equals("")) {
				values.put(String.valueOf(table.getValueAt(i, 0)), "0");
				continue;
			}
			String key = String.valueOf(table.getValueAt(i, 0));
			key = key.equals(key.toUpperCase())?key:capWords(key);
			typeArray.remove(key);
			if (typeArray.contains(key) || checkKeysAgainstOriginal(values, key)) {
				String duplicateString = getNextDuplicateString(values, key);
				values.put(duplicateString, String.valueOf(table.getValueAt(i, valueCol)));
				continue;
			}
			values.put(String.valueOf(table.getValueAt(i, 0)), String.valueOf(table.getValueAt(i, valueCol)));
		}
		return values;
	}

	private boolean checkKeysAgainstOriginal(Map<String, String> map, String original) {
		for (String s : map.keySet()) {
			if (original.matches(getOriginalType(s))) {
				return true;
			}
		}
		return false;
	}

	private String getOriginalType(String modType) {
		Matcher matcher = Pattern.compile(ChemSandFrame.DUPLICATE_REGEX).matcher(modType);
		if (matcher.find()) {
			return modType.substring(0, matcher.start()).trim();
		}
		return modType;
	}

	private ArrayList<String> getValuesInColumn(JTable table, int column) {
		ArrayList<String> array = new ArrayList<>();
		String value = "";
		int i = 0;
		while (table.getValueAt(i, 0) != null && !(value = String.valueOf(table.getValueAt(i, column))).equals("")) {
			array.add(value);
			i++;
		}
		return array;
	}

	private String getNextDuplicateString(LinkedHashMap<String, String> map, String key) {
		int i = 1;
		String duplicateString = key + "[#" + i + "]";
		while (map.containsKey(duplicateString)) {
			i++;
			duplicateString = key + "[#" + i + "]";
		}
		return duplicateString;
	}

	private ArrayList<String> getTableValues(JTable table) {
		ArrayList<String> values = new ArrayList<>();
		for (int i = 0; i < table.getRowCount(); i++) {
			if (table.getValueAt(i, 0) == null || String.valueOf(table.getValueAt(i, 0)).equals("")) {
				continue;
			}
			String tableValue = String.valueOf(table.getValueAt(i,0));
			if(tableValue.equals(tableValue.toUpperCase())){
				values.add(tableValue);
				continue;
			}
			values.add(capWords(tableValue));
		}
		return values;
	}

	private ArrayList<String> getTableValues(JTable table, int col) {
		ArrayList<String> values = new ArrayList<>();
		for (int i = 0; i < table.getRowCount(); i++) {
			if (table.getValueAt(i, col) == null || String.valueOf(table.getValueAt(i, col)).equals("")) {
				continue;
			}
			values.add(String.valueOf(table.getValueAt(i, col)));
		}
		return values;
	}
	private final static int NUM_ROWS = 40;
	private JTable constructTable(String name, int columns) {
		JTable table = new JTable(NUM_ROWS, columns);
		table.setCellSelectionEnabled(true);
		table.addKeyListener(new TableKeyPressed(table));
		setTableColumnWidth(table, 150);
		setTableHeaders(table, String.valueOf(name.charAt(0)).toUpperCase() + name.substring(1));
		table.setVisible(true);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setName(name);
		scrollPane.setVisible(true);
		this.add(scrollPane);
		return table;
	}

	private JTable constructTable(String name, int columns, char... restrictedChars) {
		JTable table = new JTable(NUM_ROWS, columns);
		table.setCellSelectionEnabled(true);
		addKeyListenerToTable(table, restrictedChars);
		setTableColumnWidth(table, 150);
		setTableHeaders(table, String.valueOf(name.charAt(0)).toUpperCase() + name.substring(1));
		table.setVisible(true);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setName(name);
		scrollPane.setVisible(true);
		this.add(scrollPane);
		return table;
	}

	private void addKeyListenerToTable(JTable table, char... restrictedChars) {
		table.addKeyListener(new TableKeyPressed(table, restrictedChars));
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumnModel().getColumn(i)
					.setCellEditor(new DefaultCellEditor(getCellEditorWithRestrictions(table, restrictedChars)));
		}
	}

	private JTextField getCellEditorWithRestrictions(JTable table, char... restrictedChars) {
		JTextField textField = new JTextField();
		textField.addKeyListener(new KeyListener() {
			private boolean checkRestricted(char typedChar) {
				if (restrictedChars == null) {
					return false;
				}
				for (char c : restrictedChars) {
					if (c == typedChar) {
						return true;
					}
				}
				return false;
			}

			@Override
			public void keyTyped(KeyEvent e) {
				if (checkRestricted(e.getKeyChar())) {
					e.consume();
					return;
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (checkRestricted(e.getKeyChar())) {
					e.consume();
					return;
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (checkRestricted(e.getKeyChar())) {
					e.consume();
					return;
				}

			}

		});
		return textField;
	}

	private void setTableHeaders(JTable table, String name) {
		table.getColumnModel().getColumn(0).setHeaderValue(name + " Names");
		table.getColumnModel().getColumn(1).setHeaderValue(name + " Usage");
		if (name.equals("Sand")) {
			table.getColumnModel().getColumn(2).setHeaderValue(name + " Design");
		} else {
			table.getColumnModel().getColumn(2).setHeaderValue("Unit");
		}
	}

	private void setTableColumnWidth(JTable table, int width) {
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setMinWidth(width);
		}
	}

	private void populateTable(JTable table, ArrayList<String> array) {
		if (array == null || array.isEmpty()) {
			return;
		}
		int i = 0;
		for (String s : array) {
			table.setValueAt(s, i, 0);
			i++;
		}
	}

	private void populateTable(JTable table, ArrayList<String> array, int col) {
		if (array == null || array.isEmpty()) {
			return;
		}
		int i = 0;
		for (String s : array) {
			table.setValueAt(s, i, col);
			i++;
		}
	}

	public static HashMap<String, ArrayList<String>> returnMapFromFiles(String chemPath, String sandPath)
			throws IOException {
		HashMap<String, ArrayList<String>> sandChemMap = new HashMap<>();
		sandChemMap.put("chemicals", readSavedNamesFromFile(chemPath));
		sandChemMap.put("sand", readSavedNamesFromFile(sandPath));
		return sandChemMap;
	}

	public static ArrayList<String> readSavedNamesFromFile(String filePath) throws IOException {
		File file = new File(filePath);
		if (!file.exists()) {
			return null;
		}
		ArrayList<String> savedNames = new ArrayList<>();
		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		String temp;
		while ((temp = bufferedReader.readLine()) != null) {
			savedNames.add(temp);
		}
		bufferedReader.close();
		return savedNames;
	}

	public static ArrayList<String> readSavedNamesFromFile(String filePath, String regex) throws IOException {
		File file = new File(filePath);
		if (!file.exists()) {
			return null;
		}
		ArrayList<String> array = new ArrayList<>();
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String temp;
		while ((temp = bufferedReader.readLine()) != null) {
			Matcher matcher = Pattern.compile(regex).matcher(temp);
			if (matcher.find()) {
				array.add(matcher.group());
			}
		}
		return array;
	}

	public static void writeSavedNamesToFile(ArrayList<String> names, String filePath) throws IOException {
		FileWriter fileWriter = new FileWriter(new File(filePath));
		fileWriter.write("");
		for (String s : names) {
			fileWriter.append(s);
			fileWriter.append("\n");
		}
		fileWriter.flush();
		fileWriter.close();
	}

	private class ChemSandLayout extends SpringLayout {

		ChemSandLayout() {
			construct();
		}

		void construct() {
			for (Component c : getContentPane().getComponents()) {
				String name = c.getName();
				switch (name) {
				case ("chemicals"):
					chemLayout((JScrollPane) c);
					break;
				case ("sand"):
					sandLayout((JScrollPane) c);
					break;
				case ("button"):
					buttonLayout((JButton) c);
					break;
				}
			}
		}

		void chemLayout(JScrollPane scrollPane) {
			putConstraint(NORTH, scrollPane, 5, NORTH, getContentPane());
			putConstraint(SOUTH, scrollPane, 5 + getTableHeight(), NORTH, getContentPane());
			putConstraint(WEST, scrollPane, 12, WEST, getContentPane());
			putConstraint(EAST, scrollPane, rectangle.width - 25, WEST, getContentPane());
		}

		void sandLayout(JScrollPane scrollPane) {
			putConstraint(NORTH, scrollPane, 10 + getTableHeight(), NORTH, getContentPane());
			putConstraint(SOUTH, scrollPane, 10 + getTableHeight() * 2, NORTH, getContentPane());
			putConstraint(WEST, scrollPane, 12, WEST, getContentPane());
			putConstraint(EAST, scrollPane, rectangle.width - 25, WEST, getContentPane());
		}

		void buttonLayout(JButton button) {
			putConstraint(NORTH, button, getButtonY(), NORTH, getContentPane());
			putConstraint(SOUTH, button, getButtonY() + 25, NORTH, getContentPane());
			putConstraint(WEST, button, getButtonX(), WEST, getContentPane());
			putConstraint(EAST, button, getButtonX() + getButtonWidth(), WEST, getContentPane());
		}

		int getButtonY() {
			return rectangle.height * 11 / 12 - 12;
		}

		int getButtonWidth() {
			return rectangle.width / 4;
		}

		int getButtonX() {
			int w = getButtonWidth();
			int width = rectangle.width;
			return width / 2 - w / 2;
		}

		int getTableHeight() {
			return rectangle.height * 5 / 12;
		}
	}
}
