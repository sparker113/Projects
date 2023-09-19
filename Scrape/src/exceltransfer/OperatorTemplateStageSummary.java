package exceltransfer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.CellEditorListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import materials.GUIUtilities;

public class OperatorTemplateStageSummary extends JFrame {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	protected Rectangle rectangle;
	protected HashMap<String, String> dataLocationMap;
	private ArrayList<String> userDefinedData;
	protected String operator;
	public final static String REQ_FILE_EXT = ".scp";
	public String path;
	private String templateName;
	public TransferTemplate transferTemplate;
	public final static String BASE_PATH = "Operator_Templates\\";
	private Semaphore semaphore = new Semaphore(0);

	public OperatorTemplateStageSummary(Rectangle rectangle, String operator, TransferTemplate transferTemplate)
			throws IOException, ClassNotFoundException {
		this.rectangle = rectangle;
		this.userDefinedData = new ArrayList<>();
		this.operator = operator;
		this.transferTemplate = transferTemplate;
		setNittyGritty();
		setPath(operator, transferTemplate.getName());
		constructInputPanels();
		constructTableScrollPane();
		addButtons();
		this.setLayout(new FrameLayout());
		this.setVisible(true);
	}

	public OperatorTemplateStageSummary(Rectangle rectangle, String operator, String templateName)
			throws IOException, ClassNotFoundException {
		this.rectangle = rectangle;
		this.userDefinedData = new ArrayList<>();
		this.operator = operator;
		this.templateName = templateName;
		setPath(operator, templateName);
		setTransferTemplate();
		setNittyGritty();
		constructInputPanels();
		constructTableScrollPane();
		addButtons();
		this.setLayout(new FrameLayout());
		this.setVisible(true);
	}

	public OperatorTemplateStageSummary(Rectangle rectangle) throws IOException, ClassNotFoundException {
		this.rectangle = rectangle;
		this.rectangle = rectangle;
		this.userDefinedData = new ArrayList<>();
		this.transferTemplate = new TransferTemplate();
		this.templateName = TransferTemplate.WELL_SUMMARY_NAME;
		setPath(TransferTemplate.WELL_SUMMARY_OPERATOR, TransferTemplate.WELL_SUMMARY_NAME);
		setNittyGritty();
		constructInputPanels();
		constructTableScrollPane();
		addButtons();
		this.setLayout(new FrameLayout());
		this.setVisible(true);
	}

	public final static String DEFAULT_EXE_DIR = "C:\\Scrape";

	public void setTransferTemplate() throws IOException, ClassNotFoundException {
		transferTemplate = TransferTemplate.readFromFile(getPath());
		if (transferTemplate == null) {
			transferTemplate = new TransferTemplate(templateName, operator);
		}
	}

	public static String getExeDir() {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(new String[] { "cmd", "/c", "cd" });
		} catch (IOException e) {
			e.printStackTrace();
			return DEFAULT_EXE_DIR;
		}
		InputStream inputStream = process.getInputStream();
		Scanner scanner = new Scanner(inputStream);
		String exeDir = "";
		while (scanner.hasNext()) {
			exeDir = scanner.next();
		}
		System.out.println(exeDir);
		scanner.close();
		return exeDir;
	}

	private void addButtons() throws IOException {
		constructButton();
		constructImportButton();
		constructPathButton();
	}

	public void populateTable(TemplateStageSummaryTable table, TransferTemplate transferTemplate) {
		if (transferTemplate.getLocationMap() == null) {
			return;
		}
		for (Map.Entry<String, String> entry : transferTemplate.getLocationMap().entrySet()) {
			LinkedHashSet<HashMap<String, Integer>> set = transferTemplate.getCellRowColumn(entry.getKey(), true);
			String dataName = entry.getKey();
			for (HashMap<String, Integer> rcMap : set) {
				if (TransferTemplate.isMatchFunc(entry.getValue())) {
					dataName += "@" + entry.getValue().substring(entry.getValue().indexOf('>') + 1);
				}
				table.setValueAt(dataName, rcMap.get(ROW), rcMap.get(COLUMN));
			}
		}
	}

	public final static String ROW = "row";
	public final static String COLUMN = "column";
	private boolean waitForSelection = false;
	private final static String IMPORT_BUTTON = "import";

	private void constructImportButton() throws IOException {
		JButton button = new JButton();
		button.setText("Import Template");
		button.setName(IMPORT_BUTTON);
		button.addActionListener(new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				importAction();
			}
		});
		button.setVisible(true);
		add(button);
	}

	public final static int ROW_HEADER_WIDTH = 35;
	public final static String REQ_EXT = ".scp";

	public static File getSelectedFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setCurrentDirectory(getBaseFile());
		int selected = fileChooser.showOpenDialog(null);
		if (selected != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		File file = fileChooser.getSelectedFile();
		return file;
	}

	private boolean checkForFileExt(String fileName) {
		String fileExt = fileName.substring(fileName.length() - 3);
		System.out.println(fileExt);
		return fileExt.equals(REQ_FILE_EXT);
	}

	private static File getBaseFile() {
		File file = new File(BASE_PATH);
		if (!file.exists()) {
			file.mkdirs();
		}
		return file;
	}

	private void importAction() {
		Executors.newSingleThreadExecutor().execute(() -> {
			File selectedFile = getSelectedFile();
			if (selectedFile == null || !checkForFileExt(selectedFile.getName())) {
				return;
			}
			TransferTemplate transferTemplate2 = null;
			try {
				transferTemplate2 = readTransferTemplate(selectedFile);
			} catch (IOException e) {
				return;
			}
			transferTemplate = TransferTemplate.mergeTemplates(transferTemplate, transferTemplate2);
			populateTable(getTemplateTable(), transferTemplate);
		});
	}

	public TemplateStageSummaryTable getTemplateTable() {
		TemplateStageSummaryTable table = (TemplateStageSummaryTable) GUIUtilities.getComponentByName(getContentPane(),
				TemplateStageSummaryTable.TABLE_NAME);
		return table;
	}

	public static TransferTemplate readTransferTemplate(File file) throws IOException {
		if (file == null) {
			return null;
		}
		ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
		TransferTemplate transferTemplate = null;
		try {
			transferTemplate = (TransferTemplate) objectInputStream.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			objectInputStream.close();
			return null;
		}

		objectInputStream.close();
		return transferTemplate;
	}

	private Optional<HashMap<String, String>> getPrevLocationMap() throws IOException {
		Optional<HashMap<String, String>> optionMap = Optional
				.ofNullable(readOperatorStageSummary(operator, REQ_FILE_EXT));
		return optionMap;
	}

	public HashMap<String, String> readTemplateFromPath(String path) throws IOException {
		HashMap<String, String> locationMap = new HashMap<>();
		File file = new File(path);
		if (!file.exists()) {
			return locationMap;
		}
		BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
		String temp;
		while ((temp = bufferedReader.readLine()) != null && temp != "") {
			locationMap.put(temp.split(":")[0], temp.split(":")[1]);
		}
		return locationMap;
	}

	private void writeUserDefinedToText() throws IOException {
		if (userDefinedData.isEmpty()) {
			return;
		}
		userDefinedData.addAll(readOperatorUserDefined(operator));
		String path = getPath();
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(path));
		for (String s : userDefinedData) {
			bufferedWriter.append(s);
			bufferedWriter.newLine();
		}
		bufferedWriter.flush();
		bufferedWriter.close();
	}

	public String getPath() {
		return this.path;
	}

	public void setPath(String Operator, String templateName) {
		this.path = BASE_PATH + Operator + "\\" + templateName + REQ_FILE_EXT;
	}

	public void addDefaultBoxList(SigValuesComboBox box) throws IOException {
		box.addAll(getDefaultBoxList());
	}

	public final static String MATCH = "match";

	public ArrayList<String> getDefaultBoxList() throws IOException {
		String[] sigVals;
		if (transferTemplate.getName().equals(TransferTemplate.WELL_SUMMARY_NAME)) {
			sigVals = DataNames.getDataNames();
			return DataNames.getArrayObject(sigVals);
		} else {
			sigVals = DataNames.getDataNames();
		}
		ArrayList<String> sigValsArray = new ArrayList<>();
		for (String s : sigVals) {
			sigValsArray.add(s);
		}
		sigValsArray.addAll(DataNames.readUserDefinedNames(operator));
		sigValsArray.add(MATCH);
		return sigValsArray;
	}

	@Deprecated
	public void integrateOldNewMap() throws IOException {
		HashMap<String, String> oldMap = readOperatorStageSummary(operator, REQ_FILE_EXT);
		dataLocationMap.putAll(oldMap);
	}

	@Deprecated
	public void writeMapToText() throws IOException {

		String path = getPath();
		File dir = new File(path);
		System.out.println(REQ_FILE_EXT);
		Stack<String> directories = new Stack<>();
		while (!dir.getParentFile().exists()) {
			directories.push(dir.getParentFile().getAbsolutePath());
			dir = new File(dir.getParent());
		}
		makeDir(directories);
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(dir));
		bufferedWriter.append("Workbook Suffix:");
		bufferedWriter.append(getInputPanelText("Workbook"));
		bufferedWriter.newLine();
		bufferedWriter.append("Sheet Name:");
		bufferedWriter.append(getInputPanelText("Worksheet"));
		bufferedWriter.newLine();
		bufferedWriter.append("Offset:");
		bufferedWriter.append(getInputPanelText("Offset"));
		bufferedWriter.newLine();
		for (String s : dataLocationMap.keySet()) {
			bufferedWriter.append(s);
			bufferedWriter.append(":");
			bufferedWriter.append(dataLocationMap.get(s));
			bufferedWriter.newLine();
		}
		bufferedWriter.flush();
		bufferedWriter.close();
	}

	private void makeDir(Stack<String> directories) {
		while (directories.size() > 0) {
			File file = new File(directories.pop());
			file.mkdir();
		}
	}

	private String getInputPanelText(String panelName) {
		for (Component c : this.getContentPane().getComponents()) {
			String name = c.getName();
			if (name == null) {
				continue;
			}
			if (name.equals(panelName)) {
				return ((InputPanel) c).getInput();
			}
		}

		return "";
	}

	public static HashMap<String, String> readOperatorStageSummary(String operator, String fileName)
			throws IOException {
		if (operator == null) {
			return null;
		}
		String path = BASE_PATH + operator + "\\" + fileName;
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		HashMap<String, String> stageSummaryMap = new HashMap<>();
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String temp;
		while ((temp = bufferedReader.readLine()) != null) {
			if (!temp.contains(":") || temp.split(":").length < 2) {
				continue;
			}
			stageSummaryMap.put(temp.split(":")[0], temp.split(":")[1]);
		}
		bufferedReader.close();
		return stageSummaryMap;
	}

	public static ArrayList<String> readOperatorUserDefined(String operator) throws IOException {
		if (operator == null) {
			return null;
		}
		String path = BASE_PATH + operator + "\\User_Defined.txt";
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		ArrayList<String> userDefinedArray = new ArrayList<>();
		BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
		String temp;
		while ((temp = bufferedReader.readLine()) != null) {
			userDefinedArray.add(temp);
		}
		bufferedReader.close();
		return userDefinedArray;
	}

	public static void writeSummaryToWorkbook(XSSFWorkbook workbook, XSSFSheet sheet, Map<String, String> valueMap,
			Map<String, String> locationMap) {
		for (String s : locationMap.keySet()) {
			int row = Integer.valueOf(locationMap.get(s).split(",")[0]).intValue();
			int column = Integer.valueOf(locationMap.get(s).split(",")[1]).intValue();
			try {
				Double doubleValue = Double.valueOf(valueMap.get(s));
				setValueToWorkbook(row, column, sheet, doubleValue);
			} catch (NumberFormatException e) {
				setValueToWorkbook(row, column, sheet, valueMap.get(s));
			}
		}
	}

	private static void checkNullRowCell(int row, int column, XSSFSheet sheet) {
		if (sheet.getRow(row) == null) {
			sheet.createRow(row);
			sheet.getRow(row).createCell(column);
		} else if (sheet.getRow(row).getCell(column) == null) {
			sheet.getRow(row).createCell(column);
		}
	}

	private static <T> void setValueToWorkbook(int row, int column, XSSFSheet sheet, T value) {
		checkNullRowCell(row, column, sheet);
		if (value.getClass().getSimpleName().equals("String")) {
			sheet.getRow(row).getCell(column).setCellType(CellType.STRING);
			sheet.getRow(row).getCell(column).setCellValue((String) value);
		} else {
			sheet.getRow(row).getCell(column).setCellType(CellType.NUMERIC);
			sheet.getRow(row).getCell(column).setCellValue((Double) value);
		}

	}

	private void setNittyGritty() {

		this.setBounds(rectangle);
		setThisTitle();
		setFrameIconImage();
		createGridLines();
		this.getContentPane().setBackground(Color.getHSBColor(-.85f, .1f, .85f));
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	public void setThisTitle() {
		this.setTitle("Stage Summary Template");
	}

	public void createGridLines() {
		int xEnd = (int) rectangle.getWidth() * 3 / 4;
		DividerLines dividerLines = new DividerLines(new Dimension(rectangle.x, rectangle.height - 280));
		dividerLines.setName("Lines");
		dividerLines.addLine(0, 60, xEnd, 60, Color.black);
		dividerLines.addLine(0, 120, xEnd, 120, Color.black);
		dividerLines.addLine(xEnd, 0, xEnd, 280, Color.black);
		this.add(dividerLines);
	}

	private void setFrameIconImage() {
		this.setIconImage(
				new ImageIcon("C:\\Scrape\\Scrape.png").getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
	}

	public final static String SCROLL_PANE = "Scroll_Pane";

	private void constructTableScrollPane() throws IOException, ClassNotFoundException {
		TemplateStageSummaryTable summaryTable = new TemplateStageSummaryTable(120, 100);
		populateTable(summaryTable, transferTemplate);
		JScrollPane scrollPane = new JScrollPane(summaryTable);
		scrollPane.setName(SCROLL_PANE);
		scrollPane.setVisible(true);
		scrollPane.setOpaque(true);
		this.add(scrollPane);
	}

	private TransferTemplate readSavedTemplate() throws IOException, ClassNotFoundException {
		return TransferTemplate.readFromFile(path);
	}

	public void constructInputPanels() {

		this.add(new InputPanel(getPanelRectangle(2),
				String.format("<html><div style=\"width\":%dpx>%s</div></html>", getPanelWidth() / 2,
						getWorkbookSuffixPrompt()),
				"Workbook", transferTemplate.getWorkbookPath() != null ? transferTemplate.getWorkbookPath() : ""));

		this.add(new InputPanel(getPanelRectangle(3),
				String.format("<html><div style=\"width\":%dpx>%s</div></html>", getPanelWidth() / 2,
						getStageSummaryPrompt()),
				"Worksheet", transferTemplate.getSheetName() != null ? transferTemplate.getSheetName() : ""));
	}

	private void constructButton() {
		JButton button = new JButton();
		button.setName("button");
		button.addActionListener(buttonAction());
		button.setText("SAVE");
		button.setVisible(true);
		this.add(button);
	}

	private final static String PATH_BUTTON = "path_button";

	private void constructPathButton() {
		JButton button = new JButton();
		button.setText("Select File");
		button.addActionListener(pathAction());
		button.setName("path_button");
		button.setVisible(true);
		add(button);
	}

	public void updateWorkbookPath(String path) {
		InputPanel inputPanel = (InputPanel) GUIUtilities.getComponentByName(this, "Workbook");
		inputPanel.setInput(path);
	}

	public void updateSheetName(String sheetName) {
		InputPanel inputPanel = (InputPanel) GUIUtilities.getComponentByName(this, "Worksheet");
		inputPanel.setInput(sheetName);
	}

	public String getSheetName(String path) throws IOException {
		ZipSecureFile.setMinInflateRatio(.001);
		XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(new File(path)));
		String[] sheets = getSheets(workbook);
		workbook.close();
		ZipSecureFile.setMinInflateRatio(.01);
		String selectedSheet = (String) JOptionPane.showInputDialog(null, "Select The Sheet", "Sheet Selection",
				JOptionPane.PLAIN_MESSAGE, null, sheets, sheets[0]);
		return selectedSheet == null ? "" : selectedSheet;
	}

	public String[] getSheets(XSSFWorkbook workbook) {
		String[] sheets = new String[workbook.getNumberOfSheets()];
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			sheets[i] = workbook.getSheetAt(i).getSheetName();
		}
		return sheets;
	}

	public void setWorkbookAndSheet(String path) throws IOException {
		updateWorkbookPath(path);
		transferTemplate.setWorkbookPath(path);
		String sheetName = getSheetName(path);
		transferTemplate.setSheetName(sheetName);
		updateSheetName(sheetName);
	}

	private ActionListener pathAction() {
		AbstractAction action = new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				Executors.newSingleThreadExecutor().execute(() -> {
					JFileChooser fileChooser = new JFileChooser();
					int selected = fileChooser.showDialog(null, "Select");
					if (selected == JFileChooser.APPROVE_OPTION) {
						String path = fileChooser.getSelectedFile().getAbsolutePath();
						try {
							setWorkbookAndSheet(path);
						} catch (IOException e1) {
							e1.printStackTrace();
							return;
						}

					}
				});
			}
		};
		return action;
	}

	private ActionListener buttonAction() {
		AbstractAction action = new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					TransferTemplate.writeToFile(transferTemplate);
					writeUserDefinedToText();
					System.out.println(userDefinedData);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				OperatorTemplateStageSummary.this.dispose();
			}
		};
		return action;
	}

	public int getPanelWidth() {
		return (int) rectangle.getWidth() * 3 / 4;
	}

	private int getBottomPanelTop() {
		return rectangle.height - 280;
	}

	public Rectangle getPanelRectangle(int numPanel) {
		int x = 0;
		int y = getBottomPanelTop() + 60 * (numPanel - 1) + 10;
		int width = getPanelWidth();
		int height = 50;
		Rectangle rectangle = new Rectangle(x, y, width, height);
		return rectangle;
	}

	public String getWorkbookSuffixPrompt() {
		String prompt = "Selected Path";
		return prompt;
	}

	public String getStageSummaryPrompt() {
		String sheetPrompt = "Input the name of the sheet";
		return sheetPrompt;
	}

	private void addAddressToRange(String key, String cellAddress) {
		transferTemplate.addToMap(key, cellAddress);
	}

	private void removeValueFromMap(String cellAddress) {
		transferTemplate.removeAddressFromMap(cellAddress);
	}

	private class FrameLayout extends SpringLayout {

		FrameLayout() {
			setFrameLayout();
		}

		int getNumPanels() {
			int count = 0;
			for (Component c : getContentPane().getComponents()) {
				if (c.getName() != null && c.getName().toUpperCase().contains("WORK")
						|| c.getName().toUpperCase().contains("OFFSET")) {
					count++;
				}
			}
			return count;
		}

		final static int BUTTON_SPACING = 20;
		final static int PANEL_HEIGHT = 70;

		void tableLayout(JScrollPane scrollPane) {
			this.putConstraint(WEST, scrollPane, 0, WEST, getContentPane());
			this.putConstraint(EAST, scrollPane, rectangle.width - 15, WEST, getContentPane());
			this.putConstraint(NORTH, scrollPane, 0, NORTH, getContentPane());
			this.putConstraint(SOUTH, scrollPane, rectangle.height - (getNumPanels()) * 70 - 80, NORTH,
					getContentPane());
		}

		void panelLayouts(InputPanel panel) {
			this.putConstraint(NORTH, panel, panel.rectangle.y, NORTH, getContentPane());
			this.putConstraint(SOUTH, panel, panel.rectangle.y + panel.rectangle.height, NORTH, getContentPane());
			this.putConstraint(WEST, panel, 0, WEST, getContentPane());
			this.putConstraint(EAST, panel, panel.rectangle.width, WEST, getContentPane());
		}

		void importButtonLayout(JButton button, int width, int height, int index) {
			int buttonY = getButtonY(index);
			int buttonX = getButtonX(width);
			this.putConstraint(NORTH, button, buttonY, NORTH, getContentPane());
			this.putConstraint(SOUTH, button, buttonY + height, NORTH, getContentPane());
			this.putConstraint(WEST, button, buttonX, WEST, getContentPane());
			this.putConstraint(EAST, button, buttonX + width, WEST, getContentPane());
		}

		void buttonLayout(JButton button, int width, int height, int index) {
			int buttonY = getButtonY(index);
			int buttonX = getButtonX(width);
			this.putConstraint(NORTH, button, buttonY, NORTH, getContentPane());
			this.putConstraint(SOUTH, button, buttonY + height, NORTH, getContentPane());
			this.putConstraint(WEST, button, buttonX, WEST, getContentPane());
			this.putConstraint(EAST, button, buttonX + width, WEST, getContentPane());
		}

		int getButtonX(int width) {
			return (rectangle.width * 7 / 8) - width / 2;
		}

		int getButtonY(int index) {
			int distFromTop = index * (BUTTON_HEIGHT + BUTTON_SPACING);
			int startY = getButtonAreaY();
			return distFromTop + startY;
		}

		int getButtonAreaY() {
			return rectangle.height - ((getNumPanels() + 1) * PANEL_HEIGHT) + BUTTON_SPACING;
		}

		void pathButtonLayout(JButton button, int width, int height, int index) {
			int buttonY = getButtonY(index);
			int buttonX = getButtonX(width);
			this.putConstraint(NORTH, button, buttonY, NORTH, getContentPane());
			this.putConstraint(SOUTH, button, buttonY + height, NORTH, getContentPane());
			this.putConstraint(WEST, button, buttonX, WEST, getContentPane());
			this.putConstraint(EAST, button, buttonX + width, WEST, getContentPane());
		}

		void linesLayout(DividerLines lines) {
			this.putConstraint(NORTH, lines, rectangle.height - (getNumPanels()) * 70 - 80, NORTH, getContentPane());
			this.putConstraint(SOUTH, lines, rectangle.height, NORTH, getContentPane());
			this.putConstraint(WEST, lines, 0, WEST, getContentPane());
			this.putConstraint(EAST, lines, rectangle.width, WEST, getContentPane());
		}

		private final static int BUTTON_WIDTH = 150;
		private final static int BUTTON_HEIGHT = 25;

		void setFrameLayout() {
			for (Component c : getContentPane().getComponents()) {
				String name = c.getName();
				switch (name) {
				case ("Scroll_Pane"):
					tableLayout((JScrollPane) c);
					break;
				case ("Offset"):
				case ("Workbook"):
				case ("Worksheet"):
					panelLayouts((InputPanel) c);
					break;
				case ("button"):
					buttonLayout((JButton) c, BUTTON_WIDTH, BUTTON_HEIGHT, 2);
					break;
				case ("path_button"):
					pathButtonLayout((JButton) c, BUTTON_WIDTH, BUTTON_HEIGHT, 1);
					break;
				case (IMPORT_BUTTON):
					importButtonLayout((JButton) c, BUTTON_WIDTH, BUTTON_HEIGHT, 0);
					break;
				case ("Lines"):
					linesLayout((DividerLines) c);
					break;
				}
			}
		}
	}

	public class TemplateStageSummaryTable extends JTable {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private int rows;
		private int columns;
		final static String TABLE_NAME = "template_table";
		boolean editing = false;
		int specifiedRow = 0;
		int specifiedColumn = 0;

		TemplateStageSummaryTable(int rows, int columns) throws IOException {
			super(rows, columns);
			this.rows = rows;
			this.columns = columns;
			setTableNittyGritty();
			setRowNumbers();
			setColumnEditor();
			this.setVisible(true);

		}

		public int getSpecifiedRow() {
			return specifiedRow;
		}

		public int getSpecifiedColumn() {
			return specifiedColumn;
		}

		private void setSpecifiedCoordinates() {
			specifiedRow = getSelectedRow();
			specifiedColumn = getSelectedColumn();
		}

		private ExecutorService executor = Executors.newCachedThreadPool();

		private String[] getColumnValues(int column) {
			String[] values = new String[getRowCount()];
			for (int i = 0; i < getRowCount(); i++) {
				values[i] = getValueAt(i, column) != null ? getValueAt(i, column).toString() : "";
			}
			return values;
		}

		private void setValuesInTable(String[] values, int column) {
			for (int i = 0; i < values.length; i++) {
				setValueAt(values[i], i, column);
			}
		}

		private void addStaticCellEditor() {
			this.removeEditor();
			for (int i = 0; i < getColumnCount(); i++) {
				String[] values = getColumnValues(i);
				setCellEditor(new DefaultCellEditor(getStaticEditor()));
				setValuesInTable(values, i);
			}
		}

		private JTextField getStaticEditor() {
			JTextField textField = new JTextField();
			textField.setText("");
			textField.setEditable(false);
			textField.setVisible(true);
			return textField;
		}

		private void setTableNittyGritty() {
			setName(TABLE_NAME);
			setAutoResizeMode(AUTO_RESIZE_OFF);
			setCellSelectionEnabled(true);
			addKeyListener(new KeyListener() {

				@Override
				public void keyTyped(KeyEvent e) {
					if (TemplateStageSummaryTable.this.getSelectedColumn() == 0) {
						e.consume();
					}
				}

				@Override
				public void keyPressed(KeyEvent e) {
					if (TemplateStageSummaryTable.this.getSelectedColumn() == 0) {
						e.consume();
					}
				}

				@Override
				public void keyReleased(KeyEvent e) {
					if (TemplateStageSummaryTable.this.getSelectedColumn() == 0) {
						e.consume();
					}
				}

			});
		}

		private int getTableX() {
			int screenWidth = rectangle.width;
			return screenWidth / 4;
		}

		private int getTableWidth() {
			int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
			return screenWidth / 2;
		}

		private int getTableHeight() {
			return Toolkit.getDefaultToolkit().getScreenSize().height - 150;
		}

		public DefaultTableCellRenderer getColumnOneTextField() {
			DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
			cellRenderer.setBackground(Color.LIGHT_GRAY);
			cellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
			cellRenderer.setVisible(true);
			return cellRenderer;
		}

		public void setColumnOneEditor() {
			DefaultTableCellRenderer cellRenderer = getColumnOneTextField();
			getColumnModel().getColumn(0).setHeaderValue("Row");
			getColumnModel().getColumn(0).setPreferredWidth(ROW_HEADER_WIDTH);
			getColumnModel().getColumn(0).setCellRenderer(cellRenderer);
			getColumnModel().getColumn(0).setCellEditor(new TableCellEditor() {

				@Override
				public Object getCellEditorValue() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public boolean isCellEditable(EventObject anEvent) {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public boolean shouldSelectCell(EventObject anEvent) {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public boolean stopCellEditing() {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public void cancelCellEditing() {
					// TODO Auto-generated method stub

				}

				@Override
				public void addCellEditorListener(CellEditorListener l) {
					// TODO Auto-generated method stub

				}

				@Override
				public void removeCellEditorListener(CellEditorListener l) {
					// TODO Auto-generated method stub

				}

				@Override
				public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
						int column) {
					// TODO Auto-generated method stub
					return null;
				}

			});
		}

		public String[][] getTableValues() {
			String[][] array = new String[getColumnCount()][getRowCount()];
			for (int i = 0; i < getColumnCount(); i++) {
				String[] values = getColumnValues(i);
				array[i] = values;
			}
			return array;
		}

		public void setValuesInTable(String[][] values) {
			int column = 0;
			for (String[] sArr : values) {
				int row = 0;
				for (String s : sArr) {
					setValueAt(s, row, column);
					row++;
				}
				column++;
			}
		}

		public void setColumnEditor(boolean original) throws IOException {
			String[][] values = null;
			if (original) {
				setColumnOneEditor();
				setColumnHeaders();
			} else {
				values = getTableValues();
				removeEditor();
			}
			for (int i = 1; i < columns; i++) {
				if (original) {
					this.getColumnModel().getColumn(i).setPreferredWidth(150);
				}

				removeEditor();
				this.getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(new SigValuesComboBox(this)));
			}
			if (!original) {
				setValuesInTable(values);
			}
		}

		public void setColumnEditor() throws IOException {
			setColumnOneEditor();
			setColumnHeaders();
			for (int i = 1; i < columns; i++) {
				this.getColumnModel().getColumn(i).setPreferredWidth(150);
				this.getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(new SigValuesComboBox(this)));
			}
		}

		private void setRowNumbers() {
			for (int i = 0; i < rows; i++) {
				this.setValueAt(i + 1, i, 0);
			}
		}

		private void setColumnHeaders() {
			for (int i = 1; i < columns; i++) {
				this.getColumnModel().getColumn(i).setHeaderValue(getHeaderNameForTable(i - 1));
			}
		}

		private String getHeaderNameForTable(int i) {
			StringBuilder builder = new StringBuilder();
			int letterCount = 0;
			int count = 0;
			do {
				letterCount++;
				count = i / (int) Math.pow(26, letterCount);
			} while (count >= 1);
			letterCount--;
			Integer colNum = i;
			for (int ii = letterCount; ii >= 0; ii--) {
				int charNum = colNum.intValue() / ((int) Math.pow(26, ii)) - ii;
				builder.append((char) (charNum + 65));
				colNum -= ((int) Math.pow(26, ii) * (charNum + ii));
			}
			return builder.substring(0);
		}

	}

	public Rectangle getFrameRectangle() {
		return getBounds();
	}

	public int getTableRelativeX(int xOnScreen) {
		Rectangle frameBounds = getFrameRectangle();
		int scrollX = ((JScrollPane) GUIUtilities.getComponentByName(getContentPane(),
				OperatorTemplateStageSummary.SCROLL_PANE)).getBounds().x;
		return xOnScreen - frameBounds.x + scrollX;
	}

	public int getTableRelativeY(int yOnScreen) {
		Insets insets = getInsets();
		Rectangle frameBounds = getFrameRectangle();
		JScrollPane scrollPane = ((JScrollPane) GUIUtilities.getComponentByName(getContentPane(),
				OperatorTemplateStageSummary.SCROLL_PANE));
		int tableY = scrollPane.getBounds().y + scrollPane.getInsets().top + scrollPane.getColumnHeader().getHeight();
		return yOnScreen - tableY;
	}

	public class SigValuesComboBox extends JComboBox<String> {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private Boolean onOff = true;
		private Boolean editing = false;
		JTable table;
		ExecutorService executor = Executors.newCachedThreadPool();
		boolean mouseEvent = false;
		Semaphore eventSem = new Semaphore(0);

		SigValuesComboBox(JTable table) throws IOException {
			this.table = table;
			addDefaultBoxList(this);
			setEditable(false);
			addPopupMenuListener(new UserDefinedActionListener(table));
			addPropertyChangeListener(new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if (waitForSelection) {
						TemplateStageSummaryTable templateStageSummaryTable = getTemplateTable();
						templateStageSummaryTable.setSpecifiedCoordinates();
						semaphore.release();
						setPopupVisible(false);
						waitForSelection = false;
						// listenerMethod(table,false,false);
						return;
					}
					if (evt.getPropertyName().equals("ancestor")) {
						setEditable(false);
					}
				}

			});
			addMouseListener(new MouseListener() {

				@Override
				public void mouseClicked(MouseEvent e) {
					executor.execute(() -> {
						System.out.println("The Mouse Button Pressed: " + e.getButton());
						if (e.getButton() == MouseEvent.BUTTON3) {

							editing = true;
							setEditable(true);
							getTemplateTable()
									.setEditingColumn(getColumnFromClick(getTableRelativeX(e.getLocationOnScreen().x)));
							getTemplateTable()
									.setEditingRow(getRowFromClick(getTableRelativeY(e.getLocationOnScreen().y)));
							return;
						}
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
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseExited(MouseEvent e) {
					// TODO Auto-generated method stub

				}

			});
		}

		public void listenerMethod(JTable table, boolean popup) {
			executor.execute(() -> {
				setEditable(popup);
				setLightWeightPopupEnabled(popup);
			});
		}

		int getColumnFromClick(int tableY) {
			float y = (float) tableY - ROW_HEADER_WIDTH;
			float columnWidth = getTemplateTable().getColumnModel().getColumn(1).getWidth();
			System.out.println("The column determined is column: " + ((int) (y / columnWidth) + 1));
			return (((int) (y / columnWidth)) + 1);
		}

		int getRowFromClick(int tableY) {
			float x = (float) tableY - (float) ROW_HEADER_WIDTH;
			float rowHeight = getTemplateTable().getRowHeight();
			System.out.println("The Determined Row: " + ((int) x / (int) rowHeight));
			return ((int) (x / rowHeight));

		}

		public void addAll(ArrayList<String> defaultBoxList) {
			for (String s : defaultBoxList) {
				this.addItem(s);
			}
		}

		public final static String ROW = "row";
		public final static String COLUMN = "column";

		public HashMap<String, Integer> selectStartCell() {

			JOptionPane.showMessageDialog(GUIUtilities.getComponentByName(getContentPane(), SCROLL_PANE),
					"Select the cell in which to begin the value search");
			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return null;

			}
			TemplateStageSummaryTable templateStageSummaryTable = getTemplateTable();
			HashMap<String, Integer> map = new HashMap<>();
			map.put(ROW, templateStageSummaryTable.getSpecifiedRow());
			map.put(COLUMN, templateStageSummaryTable.getSpecifiedColumn());
			return map;
		}

		private class UserDefinedActionListener implements PopupMenuListener {
			JTable table;

			UserDefinedActionListener(JTable table) {
				this.table = table;
			}

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

			}

			private final static String USER_DEFINED = "User-Defined";

			public void matchValueAction(int row, int column) {
				executor.execute(() -> {
					String[] dataNames = DataNames.getDataNames(userDefinedData);
					String value = (String) JOptionPane.showInputDialog(
							GUIUtilities.getComponentByName(getContentPane(), SCROLL_PANE),
							"Select the name of the data that will be input into the sheet", "Data Value Selection",
							JOptionPane.QUESTION_MESSAGE, null, dataNames, dataNames[0]);
					if (value == null) {
						return;
					} else if (value.equals(USER_DEFINED)) {
						value = addUserDefinedName();
					}
					dataNames = DataNames.putValueAtIndexInArray(dataNames, DataNames.EMPTY_CELL_VALUE, 0);
					String matchValue = (String) JOptionPane.showInputDialog(
							GUIUtilities.getComponentByName(getContentPane(), SCROLL_PANE),
							"Select the data name to match", "Search Value Selection", JOptionPane.QUESTION_MESSAGE,
							null, dataNames, dataNames[0]);
					if (matchValue == null) {
						return;
					}
					waitForSelection = true;
					HashMap<String, Integer> specsMap = selectStartCell();
					String downRight = (String) JOptionPane.showInputDialog(
							GUIUtilities.getComponentByName(getContentPane(), SCROLL_PANE), "Search row or column?",
							"Search Direction", JOptionPane.QUESTION_MESSAGE, null,
							new String[] { SEARCH_ROW, SEARCH_COLUMN }, SEARCH_ROW);
					if (downRight == null) {
						return;
					}
					downRight = downRight.equals(SEARCH_ROW) ? TransferTemplate.RIGHT : TransferTemplate.DOWN;
					String matchString = TransferTemplate.getMatchString(matchValue, String.valueOf(specsMap.get(ROW)),
							String.valueOf(specsMap.get(COLUMN)), downRight);
					addItem(value + "@" + matchString);
					table.setValueAt(value + "@" + matchString, row, column + 1);
					transferTemplate.addToMap(value,
							TransferTemplate.getMatchAddress(matchString, downRight, row, column));
				});
			}

			public void updateMaps(String definedName, int row, int column) {
				transferTemplate.addToMap(definedName, String.valueOf(row), String.valueOf(column));
				userDefinedData.add(definedName);
				table.setValueAt(definedName, row, column + 1);
			}

			public final static String SEARCH_ROW = "SEARCH IN ROW";
			public final static String SEARCH_COLUMN = "SEARCH IN COLUMN";

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				if (getSelectedItem() == null) {
					return;
				}
				String selected = SigValuesComboBox.this.getSelectedItem().toString();
				int row = table.getSelectedRow();
				int column = table.getSelectedColumn() - 1;
				System.out.println(row + "," + column);
				if (selected.equals("User-Defined")) {
					addUserDefined(row, column);
				} else if (selected.equals(MATCH)) {
					matchValueAction(row, column);
				} else if (!selected.equals("")) {
					transferTemplate.addToMap(selected, String.valueOf(row), String.valueOf(column));
				} else {
					removeValueFromMap(row + "," + column);
				}
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {

			}

			public void addUserDefined(int row, int column) {
				String definedName = JOptionPane.showInputDialog(OperatorTemplateStageSummary.this,
						"Input the identifier for the data required for this operator");
				updateMaps(definedName, row, column);
				addItem(definedName);
			}

			public String addUserDefinedName() {
				String definedName = JOptionPane.showInputDialog(OperatorTemplateStageSummary.this,
						"Input the identifier for the data required for this operator");
				addItem(definedName);
				return definedName;
			}

		}

	}

	private class DividerLines extends JPanel {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		LinkedList<Lines> lines;
		Dimension dim;

		DividerLines(Dimension dim) {
			this.dim = dim;
			this.lines = new LinkedList<>();
			this.setSize(dim);
			this.setBackground(null);
			this.setOpaque(true);
		}

		public void addLine(int x1, int y1, int x2, int y2, Color color) {
			this.lines.add(new Lines(x1, y1, x2, y2, color));
		}

		@Override
		public void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			for (Lines line : lines) {
				g2d.setColor(line.color);
				g2d.setStroke(new BasicStroke(2));
				g2d.drawLine(line.x1, line.y1, line.x2, line.y2);
			}
		}

		private static class Lines {
			final int x1;
			final int y1;
			final int x2;
			final int y2;
			final Color color;

			Lines(int x1, int y1, int x2, int y2, Color color) {
				this.x1 = x1;
				this.y1 = y1;
				this.x2 = x2;
				this.y2 = y2;
				this.color = color;
			}

		}
	}

}
