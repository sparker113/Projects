package exceltransfer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelTransfer<T> implements Runnable {
	private XSSFWorkbook workbook;
	private XSSFSheet sheet;
	private ArrayList<T> array;
	private ArrayList<T> array1;
	private ArrayList<T> arrayOverlay;
	private Integer startRow;
	private Integer column;
	private Integer excludeRow;
	private Integer startRow2;
	Thread t;

	ExcelTransfer(XSSFWorkbook workbook, XSSFSheet sheet, ArrayList<T> array, Integer startRow, Integer column,
			Integer excludeRow, Integer startRow2) {
		this.workbook = workbook;
		this.sheet = sheet;
		this.array = array;
		this.startRow = startRow;
		this.column = column;
		this.excludeRow = excludeRow;
		this.startRow2 = startRow2;
		this.t = new Thread(this, "Excel Transfer - " + column);
		t.setDaemon(false);
		t.start();
	}

	ExcelTransfer(XSSFWorkbook workbook, XSSFSheet sheet, ArrayList<T> array, Integer startRow, Integer column) {
		this.workbook = workbook;
		this.sheet = sheet;
		this.array = array;
		this.startRow = startRow;
		this.column = column;
		this.excludeRow = 0;
		this.t = new Thread(this, "Excel Transfer - 1st Constructor");
		t.setDaemon(false);
		t.start();
	}

	ExcelTransfer(XSSFWorkbook workbook, XSSFSheet sheet, ArrayList<T> array1, ArrayList<T> arrayOverlay,
			Integer startRow, Integer column, Integer excludeRow, Integer startRow2) {
		this.workbook = workbook;
		this.sheet = sheet;
		this.array1 = array1;
		this.arrayOverlay = arrayOverlay;
		this.column = column;
		this.startRow = startRow;
		this.startRow2 = startRow2;
		this.excludeRow = excludeRow;
		this.array = getNameColumn();
		this.t = new Thread(this, "Excel Transfer - 2nd Constructor");
		t.setDaemon(false);
		t.start();
	}

	ExcelTransfer(XSSFWorkbook workbook, XSSFSheet sheet, ArrayList<T> array, String findString, Integer column) {
		this.workbook = workbook;
		this.sheet = sheet;
		this.array = array;
		this.column = column;
		this.startRow = findRow(findString, column);
		if (startRow == -1) {
			JOptionPane.showMessageDialog(null, "Unable to find " + findString + " in the specified column");
			return;
		}
		this.t = new Thread(this, "Excel Transfer - 3rd Constructor");
		t.setDaemon(false);
		t.start();
	}

	public static MirroredSpreadSheet getArrayOfDataFromWorkbook(int rowExtent, int colExtent)
			throws IOException, InterruptedException {
		File file = OperatorTemplateStageSummary.getSelectedFile();
		XSSFWorkbook workbook = getWorkbook(file);
		String[][] sheetValues = getSheetValues(workbook, rowExtent, colExtent);
		workbook.close();
		if (sheetValues == null) {
			return null;
		}
		MirroredSpreadSheet mirror = new MirroredSpreadSheet(sheetValues);
		return mirror;
	}

	public static String[][] getSheetValues(XSSFWorkbook workbook, int rowExtent, int colExtent) {
		XSSFSheet sheet = getSelectedSheet(workbook);
		if (sheet == null) {
			return null;
		}
		String[][] sheetValues = new String[rowExtent][colExtent];
		for (int row = 0; row < rowExtent; row++) {
			for (int column = 0; column < colExtent; column++) {
				sheetValues[row][column] = getCellValue(sheet, row, column);
			}
		}
		return sheetValues;
	}

	public static String getCellValue(XSSFSheet sheet, int row, int column) {
		if (sheet.getRow(row) == null) {
			return "";
		} else if (sheet.getRow(row).getCell(column) == null) {
			return "";
		}
		XSSFCell cell = sheet.getRow(row).getCell(column);
		System.out.println(cell.getCellStyle().getDataFormatString());
		if (cell.getCellType() == CellType.NUMERIC & checkForDateStyle(cell.getCellStyle().getDataFormatString())) {
			return cell.getLocalDateTimeCellValue().format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss"));
		} else if (cell.getCellType() == CellType.NUMERIC) {
			return String.valueOf(cell.getNumericCellValue());
		}
		return cell.getStringCellValue().toString();
	}

	public static boolean checkForDateStyle(String dataFormatString) {
		Matcher matcher = Pattern.compile("([mM][Mm]?/[Dd][Dd]?/[Yy][Yy][Yy]?[Yy]?)|([Yy]{4}\\-[Mm][Mm]?\\-[Dd][Dd]?)")
				.matcher(dataFormatString);
		return matcher.find();
	}

	public static XSSFSheet getSelectedSheet(XSSFWorkbook workbook) {
		String[] sheetNames = getSheetNames(workbook);
		String selectedSheet = (String) JOptionPane.showInputDialog(null,
				"Select the sheet from which you want to extract data", "Sheet Selection", JOptionPane.DEFAULT_OPTION,
				null, sheetNames, sheetNames[0]);
		if (selectedSheet == null) {
			return null;
		}
		return workbook.getSheet(selectedSheet);
	}

	public static String[] getSheetNames(XSSFWorkbook workbook) {
		String[] sheetNames = new String[workbook.getNumberOfSheets()];
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			sheetNames[i] = workbook.getSheetAt(i).getSheetName();
		}
		return sheetNames;
	}

	public static XSSFWorkbook getWorkbook(File file) throws IOException {
		XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(file));
		return workbook;
	}

	public static CellStyle getTimeStyle(XSSFWorkbook workbook, XSSFCell cell) {
		CellStyle cellStyle = workbook.createCellStyle();
		CreationHelper createHelper = workbook.getCreationHelper();
		cellStyle.cloneStyleFrom(cell.getCellStyle());
		cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("HH:mm"));
		return cellStyle;
	}

	public static void changeTypeToTime(XSSFWorkbook workbook, XSSFSheet sheet, XSSFCell cell) {
		int row = cell.getRowIndex();
		int column = cell.getColumnIndex();
		CellStyle cellStyle = getTimeStyle(workbook, cell);
		sheet.getRow(row).createCell(column);
		checkForFormulaAndRemove(cell);
		sheet.getRow(row).getCell(column).setCellStyle(cellStyle);
		sheet.getRow(row).getCell(column).setCellType(CellType.NUMERIC);
	}

	public static void changeTypeToTime(XSSFWorkbook workbook, XSSFSheet sheet, int row, int column) {
		XSSFCell cell = checkCreateCell(sheet, row, column);
		CellStyle cellStyle = getTimeStyle(workbook, cell);
		sheet.getRow(row).createCell(column);
		checkForFormulaAndRemove(cell);
		sheet.getRow(row).getCell(column).setCellStyle(cellStyle);
		sheet.getRow(row).getCell(column).setCellType(CellType.NUMERIC);
	}

	public static CellStyle getDateStyle(XSSFWorkbook workbook, XSSFCell cell) {
		CellStyle cellStyle = workbook.createCellStyle();
		CreationHelper createHelper = workbook.getCreationHelper();
		cellStyle.cloneStyleFrom(cell.getCellStyle());
		cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("MM/dd/yyyy"));
		return cellStyle;
	}
	public static XSSFCellStyle getPatternStyle(XSSFWorkbook workbook,XSSFSheet sheet,int row,int column,String pattern) {
		XSSFCell cell = checkCreateCell(sheet,row,column);
		CreationHelper createHelper = workbook.getCreationHelper();
		checkForFormulaAndRemove(cell);
		XSSFCellStyle cellStyle = cell.getCellStyle();
		cellStyle.setDataFormat(createHelper.createDataFormat().getFormat(pattern));
		return cellStyle;
	}
	public static void changeTypeToDate(XSSFWorkbook workbook, XSSFSheet sheet, XSSFCell cell) {
		int row = cell.getRowIndex();
		int column = cell.getColumnIndex();
		CellStyle cellStyle = getDateStyle(workbook, cell);
		sheet.getRow(row).createCell(column);
		checkForFormulaAndRemove(cell);
		sheet.getRow(row).getCell(column).setCellStyle(cellStyle);
		sheet.getRow(row).getCell(column).setCellType(CellType.NUMERIC);
	}

	public static String convertExcelAddressToRC(String address, Integer stage) {
		String row = findRowFromAddress(address.replace("+", ""));
		String column = findColumnFromAddress(address.replace("+", ""));
		if (address.contains("+")) {
			String addByStage = getAdditionByStage(address);
			switch (addByStage) {
			case ("0,1"):
				column = String.valueOf(Integer.valueOf(column) + stage - 1);
				break;
			case ("1,0"):
				row = String.valueOf(Integer.valueOf(row) + stage - 1);
				break;
			}
		}
		return row + "," + column;
	}

	private static Integer getIntegerFromString(String string) {
		Matcher matcher = Pattern.compile("\\D").matcher(string);
		if (matcher.find()) {
			return 0;
		}
		return Integer.valueOf(string);
	}

	public static String convertExcelAddressToRC(XSSFSheet sheet, String address, String stage) {
		Matcher matcher = Pattern.compile("match\\((\\d+)\\)").matcher(address);
		String row = "";
		String column = "";
		if (matcher.find() & matcher.start() == 0) {
			Integer searchRow = getSearchRowColumn(matcher.group());
			if (searchRow != -1) {
				column = findColumnByCellValue(sheet, searchRow, stage);
			} else {
				System.out.println("Search Row absent in match function");
				return null;
			}
			row = String.valueOf(getIntegerFromString(address.substring(matcher.end())) - 1);
			if (row.equals("-1")) {
				return "-1";
			}
		} else {
			Integer searchColumn = getSearchRowColumn(matcher.group());
			if (searchColumn != -1) {
				row = findRowByCellValue(sheet, searchColumn, stage);
			} else {
				System.out.println("Search Column absent in match function");
				return null;
			}
			column = String.valueOf(getIntegerFromString(address.substring(0, matcher.start())) - 1);
			if (column.equals("-1")) {
				return "-1";
			}
		}
		return row + "," + column;
	}

	private static Integer getSearchRowColumn(String matchFunc) {
		Matcher matcher = Pattern.compile("(\\d+)").matcher(matchFunc);
		return matcher.find() ? Integer.valueOf(matcher.group()) - 1 : -1;
	}

	private static String findColumnByCellValue(XSSFSheet sheet, Integer searchRow, String stage) {
		int i = -1;
		String matchString = "";
		do {
			i++;
			checkNullCreateCell(sheet, searchRow, i);
			// changeTypeToString(sheet,sheet.getRow(searchRow).getCell(i));
			System.out.println("Search Row: " + searchRow + " - Column: " + i);
			String cellValue = String.valueOf(sheet.getRow(searchRow).getCell(i).getRichStringCellValue());
			Matcher matcher = Pattern.compile("\\d+").matcher(cellValue);
			if (matcher.find()) {
				matchString = matcher.group();
			}
		} while (!matchString.equals(stage) & i < 999);
		return String.valueOf(i);
	}

	private static String findRowByCellValue(XSSFSheet sheet, Integer searchColumn, String stage) {
		int i = -1;
		String matchString = "";
		do {
			i++;
			checkNullCreateCell(sheet, i, searchColumn);
			changeTypeToString(sheet, sheet.getRow(i).getCell(i));
			String cellValue = String.valueOf(sheet.getRow(i).getCell(searchColumn).getRawValue());
			Matcher matcher = Pattern.compile("\\d+").matcher(cellValue);
			if (matcher.find()) {
				matchString = matcher.group();
			}
		} while (!matchString.equals(stage) & i < 999);
		return String.valueOf(i);
	}

	public static String getAdditionByStage(String address) {
		Matcher matcher = Pattern.compile("(\\w+)\\+").matcher(address);
		if (matcher.find()) {
			return "0,1";
		} else {
			return "1,0";
		}
	}

	public static String findRowFromAddress(String address) {
		Matcher matcher = Pattern.compile("\\d+").matcher(address);
		String column = "0";
		if (matcher.find()) {
			column = String.valueOf(Integer.valueOf(matcher.group()) - 1);
		}
		return column;
	}

	public static String findColumnFromAddress(String address) {
		Matcher matcher = Pattern.compile("\\D+").matcher(address);
		int column = 0;
		if (matcher.find()) {
			String rowLetters = matcher.group().toUpperCase();
			Iterator<Integer> iterator = rowLetters.chars().iterator();
			ArrayList<Integer> integerArray = new ArrayList<>();
			while (iterator.hasNext()) {
				integerArray.add(iterator.next() - 64);
			}
			int count = 0;
			for (int i = integerArray.size() - 1; i >= 0; i--) {
				column = column + integerArray.get(i)
						* Integer.valueOf(String.valueOf(Math.round(Math.pow(26.0, Double.valueOf(count)))));
				count++;
			}
		}
		System.out.println("Found The Column: " + column + " for " + address);
		return String.valueOf(column - 1);
	}

	public static int getColumnIndex(String column) {
		String rowLetters = column;
		int columnInt = 0;
		Iterator<Integer> iterator = rowLetters.chars().iterator();
		ArrayList<Integer> integerArray = new ArrayList<>();
		while (iterator.hasNext()) {
			integerArray.add(iterator.next() - 64);
		}
		int count = 0;
		for (int i = integerArray.size() - 1; i >= 0; i--) {
			columnInt = columnInt + integerArray.get(i)
					* Integer.valueOf(String.valueOf(Math.round(Math.pow(26.0, Double.valueOf(count)))));
			count++;
		}
		return columnInt - 1;
	}

	public static XSSFCell checkCreateCell(XSSFSheet sheet, int row, int column) {
		if (sheet.getRow(row) == null) {
			sheet.createRow(row);
			sheet.getRow(row).createCell(column);
			return sheet.getRow(row).getCell(column);
		} else if (sheet.getRow(row).getCell(column) == null) {
			sheet.getRow(row).createCell(column);
			return sheet.getRow(row).getCell(column);
		}
		return sheet.getRow(row).getCell(column);
	}

	public synchronized static void changeTypeToDouble(XSSFWorkbook workbook, XSSFSheet sheet, int row, int column,
			int decimalPlaces) {
		XSSFCell cell = checkCreateCell(sheet, row, column);
		CreationHelper createHelper = workbook.getCreationHelper();
		checkForFormulaAndRemove(cell);
		CellStyle cellStyle = cell.getCellStyle();
		if (decimalPlaces > 0) {
			cellStyle.setDataFormat(
					createHelper.createDataFormat().getFormat("####." + getRepeatedValue("#", decimalPlaces)));
		} else {
			cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("General"));
		}

		sheet.getRow(row).createCell(column);
		sheet.getRow(row).getCell(column).setCellStyle(cellStyle);
		sheet.getRow(row).getCell(column).setCellType(CellType.NUMERIC);
	}

	public static String getRepeatedValue(String value, int numTimes) {
		String valueIt = "";
		for (int i = 0; i < numTimes; i++) {
			valueIt += value;
		}
		return valueIt;
	}

	public synchronized static void changeTypeToDouble(XSSFWorkbook workbook, XSSFSheet sheet, XSSFCell cell,
			Boolean decimal) {
		int row = cell.getRowIndex();
		int column = cell.getColumnIndex();
		CreationHelper createHelper = workbook.getCreationHelper();
		checkForFormulaAndRemove(cell);
		CellStyle cellStyle = cell.getCellStyle();
		if (decimal) {
			cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("####.###"));
		} else {
			cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("General"));
		}

		sheet.getRow(row).createCell(column);
		sheet.getRow(row).getCell(column).setCellStyle(cellStyle);
		sheet.getRow(row).getCell(column).setCellType(CellType.NUMERIC);
	}

	public synchronized static void checkForFormulaAndRemove(XSSFCell cell) {
		if (cell.getCellType().equals(CellType.FORMULA) && cell.getCellFormula() != null) {
			try {
				cell.setCellFormula(null);
			} catch (org.apache.xmlbeans.impl.values.XmlValueDisconnectedException e) {
				checkForFormulaAndRemove(cell);
			}
		}
		return;
	}

	public static void changeTypeToNumeric(XSSFSheet sheet, int row, int column) {
		checkCreateCell(sheet, row, column);
		XSSFCell cell = sheet.getRow(row).getCell(column);
		checkForFormulaAndRemove(cell);
		CellStyle cellStyle = cell.getCellStyle();
		sheet.getRow(row).createCell(column);
		sheet.getRow(row).getCell(column).setCellStyle(cellStyle);
		sheet.getRow(row).getCell(column).setCellType(CellType.NUMERIC);
	}

	public static void changeTypeToNumeric(XSSFSheet sheet, XSSFCell cell) {
		int row = cell.getRowIndex();
		int column = cell.getColumnIndex();
		checkForFormulaAndRemove(cell);
		CellStyle cellStyle = cell.getCellStyle();
		sheet.getRow(row).createCell(column);
		sheet.getRow(row).getCell(column).setCellStyle(cellStyle);
		sheet.getRow(row).getCell(column).setCellType(CellType.NUMERIC);
	}

	public static void changeTypeToString(XSSFSheet sheet, XSSFCell cell) {
		int row = cell.getRowIndex();
		int column = cell.getColumnIndex();
		checkForFormulaAndRemove(cell);
		CellStyle cellStyle = cell.getCellStyle();
		sheet.getRow(row).createCell(column);
		sheet.getRow(row).getCell(column).setCellType(CellType.STRING);
		sheet.getRow(row).getCell(column).setCellStyle(cellStyle);

	}

	public static void changeTypeToString(XSSFSheet sheet, int row, int column) {
		XSSFCell cell = checkCreateCell(sheet, row, column);
		checkForFormulaAndRemove(cell);
		CellStyle cellStyle = cell.getCellStyle();
		sheet.getRow(row).createCell(column);
		sheet.getRow(row).getCell(column).setCellType(CellType.STRING);
		sheet.getRow(row).getCell(column).setCellStyle(cellStyle);

	}

	public ArrayList<T> getNameColumn() {
		ArrayList<T> newArray = new ArrayList<>();
		int i = 0;
		for (T a : arrayOverlay) {
			if (!a.equals("")) {
				newArray.add(a);
			} else {
				newArray.add(array1.get(i));
			}
			i++;
		}
		return newArray;
	}

	public static void checkNullCreateCell(XSSFSheet sheet, int row, int column) {
		if (sheet.getRow(row) == null) {
			sheet.createRow(row);
		}
		if (sheet.getRow(row).getCell(column) == null) {
			sheet.getRow(row).createCell(column);
		}
	}

	public void writeColumn() {
		for (int i = 0; i < array.size() - excludeRow; i++) {
			checkNullCreateCell(sheet, startRow + i, column);
			if (array.get(0).getClass().toString().contains("String")) {
				checkForFormulaAndRemove(sheet.getRow(startRow + i).getCell(column));
				changeTypeToString(sheet, sheet.getRow(i + startRow).getCell(column));
				sheet.getRow(i + startRow).getCell(column).setCellValue(String.valueOf(array.get(i)));

			} else {
				// removeFormula(sheet.getRow(i+startRow).getCell(column),sheet);
				if (!array.get(i).equals(0.0)) {
					checkForFormulaAndRemove(sheet.getRow(startRow + i).getCell(column));
					changeTypeToDouble(workbook, sheet, sheet.getRow(i + startRow).getCell(column), true);
					sheet.getRow(i + startRow).getCell(column)
							.setCellValue(Double.valueOf(String.valueOf(array.get(i))));
				}
			}
		}
		if (excludeRow != 0) {
			writeAddColumn();
		}
	}

	public synchronized void transformTypeCell(XSSFSheet sheet, Integer row, Integer column, CellType cellType) {
		XSSFCell cell = sheet.getRow(row).getCell(column);
		CellStyle cellStyle = cell.getCellStyle();
		/*
		 * sheet.getRow(row).removeCell(cell); if(sheet.getRow(row)==null) {
		 * sheet.createRow(row); System.out.println("Created a row"); }
		 */
		sheet.getRow(row).createCell(column);
		sheet.getRow(row).getCell(column).setCellStyle(cellStyle);
		sheet.getRow(row).getCell(column).setCellType(cellType);
	}

	public void writeAddColumn() {
		int i;
		int ii = 0;
		for (i = array.size() - excludeRow; i < array.size(); i++) {
			if (array.get(0).getClass().toString().contains("String")) {
				checkForFormulaAndRemove(sheet.getRow(startRow2 + ii).getCell(column));
				changeTypeToString(sheet, sheet.getRow(startRow2 + ii).getCell(column));
				sheet.getRow(ii + startRow2).getCell(column).setCellValue(String.valueOf(array.get(i)));
			} else {
				if (!array.get(i).equals(0.0)) {
					checkForFormulaAndRemove(sheet.getRow(startRow2 + ii).getCell(column));
					changeTypeToDouble(workbook, sheet, sheet.getRow(startRow2 + ii).getCell(column), true);
					sheet.getRow(ii + startRow2).getCell(column)
							.setCellValue(Double.valueOf(String.valueOf(array.get(i))));
				}
			}
			ii++;
		}
	}

	public Integer findRow(String findString, Integer col) {
		int i;
		Integer row = -1;
		for (i = 1; i < sheet.getLastRowNum(); i++) {
			if (sheet.getRow(i).getCell(col).getStringCellValue().contains(findString)) {
				row = i;
			}
		}
		return row;
	}

	@Override
	public void run() {
		writeColumn();
	}

	public static class MirroredSpreadSheet extends JFrame {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		String[][] sheetValues;
		ArrayList<ArrayList<String>> array;;
		private int rows;
		private int columns;
		public final static Color SELECTED_COLOR = getSelectedColor();
		public final static Color DESELECTED_COLOR = getDeselectedColor();
		private Semaphore semaphore = new Semaphore(0);

		MirroredSpreadSheet(String[][] sheetValues) {
			this.sheetValues = sheetValues;
			setRows(sheetValues);
			setColumns(sheetValues);
			setBounds(getDefaultBounds());
			constructScrollPane();
			constructGetButton();
			nittyGritty();
		}

		void nittyGritty() {
			setTitle(TITLE);
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			getContentPane().setBackground(Color.getHSBColor(-.85f, .1f, .85f));
			setLayout(new MirroredSheetLayout());
			setVisible(true);
		}

		private final static String TITLE = "Mirrored Spreadsheet";

		Rectangle getDefaultBounds() {
			return new Rectangle(0, 0, GUIUtilities.getScreenWidth(), GUIUtilities.getScreenHeight());
		}

		public final static String SCROLL_PANE = "scroll_pane";

		private void constructScrollPane() {
			JScrollPane scrollPane = new JScrollPane(constructTable());
			scrollPane.setName(SCROLL_PANE);
			scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			scrollPane.setVisible(true);
			add(scrollPane);
		}

		public final static String TABLE = "table";
		public final static int TABLE_MIN_WIDTH = 75;

		private void setTableMinWidth(JTable table) {
			for (int i = 0; i < columns; i++) {
				table.getColumnModel().getColumn(i).setMinWidth(TABLE_MIN_WIDTH);
			}
		}

		private JTable constructTable() {
			JTable table = new JTable(rows, columns);
			table.setName(TABLE);
			setTableMinWidth(table);
			table.setCellSelectionEnabled(true);
			setTableCellEditor(table, getCellEditor());
			setTableRenderer(table);
			// table.addMouseListener(getTableMouseListener());
			populateTable(table);
			// table.setVisible(true);
			return table;
		}

		void setTableCellEditor(JTable table, TableCellEditor cellEditor) {
			for (int i = 0; i < columns; i++) {
				table.getColumnModel().getColumn(i).setCellEditor(cellEditor);
			}
		}

		void setTableRenderer(JTable table) {
			for (int i = 0; i < columns; i++) {
				table.getColumnModel().getColumn(i).setCellRenderer(getCellRenderer());
			}
		}

		void populateTable(JTable table) {
			int row = 0;
			int column = 0;
			for (String[] strArray : sheetValues) {
				column = 0;
				for (String val : strArray) {
					table.setValueAt(val, row, column);
					column++;
				}
				row++;
			}
		}

		private void setRows(String[][] sheetValues) {
			rows = sheetValues.length;
		}

		private void setColumns(String[][] sheetValues) {
			if (sheetValues.length == 0) {
				columns = 0;
				return;
			}
			columns = sheetValues[0].length;
		}

		/*
		 * private MouseListener getTableMouseListener() {
		 * 
		 * return new MouseListener() {
		 * 
		 * @Override public void mouseClicked(MouseEvent e) {
		 * 
		 * }
		 * 
		 * @Override public void mousePressed(MouseEvent e) {
		 * 
		 * }
		 * 
		 * @Override public void mouseReleased(MouseEvent e) { JTable table = (JTable)
		 * e.getSource(); int[] rows = table.getSelectedRows(); int[] columns =
		 * table.getSelectedColumns(); Executors.newSingleThreadExecutor().execute(()->{
		 * 
		 * for (int row : rows) { for (int column : columns) { if (table.getValueAt(row,
		 * column) == null || table.getValueAt(row, column).toString().equals("")) {
		 * continue; } System.out.println("update cell color: "+table.getValueAt(row,
		 * column)); updateCellColor(table, row, column); } } }); }
		 * 
		 * @Override public void mouseEntered(MouseEvent e) {
		 * 
		 * }
		 * 
		 * @Override public void mouseExited(MouseEvent e) { // TODO Auto-generated
		 * method stub
		 * 
		 * }
		 * 
		 * 
		 * 
		 * }; }
		 */
		public void updateCellColor(JTable table, int row, int column) {
			if (((JLabel) table.getCellRenderer(row, column)).getBackground() == DESELECTED_COLOR) {
				System.out.println("UPDATE CELL COLOR TO WHITE");
				((JLabel) table.getCellRenderer(row, column)).setBackground(SELECTED_COLOR);
			} else {
				System.out.println("UPDATE CELL COLOR TO GREY");
				((JLabel) table.getCellRenderer(row, column)).setBackground(DESELECTED_COLOR);
			}
			((JLabel) table.getCellRenderer(row, column)).repaint();
		}

		public final static String BUTTON = "button";
		public final static String BUTTON_TEXT = "Get Selected Values";
		public final static float BUTTON_WIDTH_RATIO = .25f;
		public final static int BUTTON_HEIGHT = 25;

		void constructGetButton() {
			JButton button = new JButton();
			button.setName(BUTTON);
			button.setText(BUTTON_TEXT);
			button.addActionListener(getButtonAction());
			button.setVisible(true);
			add(button);
		}

		private AbstractAction getButtonAction() {
			return new AbstractAction() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					Executors.newSingleThreadExecutor().execute(()->{
						exited = false;
						JTable table = (JTable) GUIUtilities.getComponentByName(getContentPane(), TABLE);
						setSelectedValues(table);
						dispose();
					});
				}
			};
		}

		boolean exited = true;

		@Override
		public void dispose() {
			if (exited) {
				try {
					semaphore.acquire();
				}catch(InterruptedException e) {
					e.printStackTrace();
					super.dispose();
					return;
				}
			}
			super.dispose();
		}

		private void setSelectedValues(ArrayList<ArrayList<String>> array) {
			this.array = array;
			semaphore.release();
		}
		public ArrayList<ArrayList<String>> getSelectedValues() throws InterruptedException {
			semaphore.acquire();
			ArrayList<ArrayList<String>> selectedArray = new ArrayList<>();
			selectedArray.addAll(array);
			semaphore.release();
			return selectedArray;
		}

		public static boolean colorIsEqual(Color color1, Color color2) {
			boolean isEqual = true;
			isEqual = color1.getRed() != color2.getRed() ? false
					: (color1.getGreen() != color2.getGreen() ? false : (color1.getBlue() == color2.getBlue()));
			System.out.println(isEqual);
			return isEqual;
		}

		@SuppressWarnings("null")
		private void setSelectedValues(JTable table) {
			ArrayList<ArrayList<String>> selectedArray = new ArrayList<>();
		
			for (int row = 1; row < rows; row++) {
				selectedArray.add(new ArrayList<String>());
				for (int column = 0; column < columns; column++) {
					System.out.println(((JLabel) table.getCellRenderer(row, column)).getBackground());
					JLabel label = ((JLabel) table.getCellRenderer(row, column));
					if (label == null || label.getBackground() == null) {
						continue;
					}
					if (table.getValueAt(row, column) != null && !table.getValueAt(row, column).toString().equals("")
							&& colorIsEqual(label.getBackground(),SELECTED_COLOR)) {

						selectedArray.get(selectedArray.size()-1).add(table.getValueAt(row, column).toString());
					}
				}
				if (selectedArray.get(selectedArray.size()-1).isEmpty()) {
					selectedArray.remove(selectedArray.size()-1);
				}
			}
			setSelectedValues(selectedArray);
		}

		JTextField getCellEditorTextField() {
			JTextField textField = new JTextField();
			// textField.setEditable(false);
			textField.setEnabled(false);
			return textField;
		}

		private final static int D_RED = 184;
		private final static int D_GREEN = 207;
		private final static int D_BLUE = 229;

		private static Color getSelectedColor() {
			float[] buffer = new float[3];
			buffer = Color.RGBtoHSB(D_RED, D_GREEN, D_BLUE, buffer);
			return Color.getHSBColor(buffer[0], buffer[1], buffer[2]);
		}

		private static Color getDeselectedColor() {
			return Color.getHSBColor(1f, 1f, 1f);
		}

		void updateCellColor(Component comp) {

			if (comp.getBackground() == DESELECTED_COLOR) {
				comp.setBackground(SELECTED_COLOR);
				return;
			}
			comp.setBackground(DESELECTED_COLOR);
		}

		DefaultTableCellRenderer getCellRenderer() {
			DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
						boolean hasFocus, int row, int column) {
					Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
							column);
					if (table.isCellSelected(row, column) & value != null && !value.toString().equals("")) {
						updateCellColor(comp);
					}
					return comp;
				}
			};
			return cellRenderer;
		}

		TableCellEditor getCellEditor() {
			TableCellEditor cellEditor = new DefaultCellEditor(getCellEditorTextField());
			return cellEditor;
		}

		class MirroredSheetLayout extends SpringLayout {
			JPanel panel;
			public final static int BUTTON_AREA_BUFFER = 150;

			MirroredSheetLayout() {
				panel = ((JPanel) getContentPane());
				construct();
			}

			void construct() {
				for (Component c : panel.getComponents()) {
					String name = c.getName();
					switch (name) {
					case (BUTTON):
						buttonLayout((JButton) c);
						break;
					case (SCROLL_PANE):
						scrollLayout((JScrollPane) c);
						break;
					}
				}
			}

			private final static int BORDER_BUFFER = 50;

			void scrollLayout(JScrollPane scrollPane) {
				putConstraint(NORTH, scrollPane, BORDER_BUFFER, NORTH, panel);
				putConstraint(SOUTH, scrollPane, getScrollSouth(), NORTH, panel);
				putConstraint(WEST, scrollPane, BORDER_BUFFER, WEST, panel);
				putConstraint(EAST, scrollPane, getScrollEast(), WEST, panel);
			}

			int getScrollEast() {
				return getBounds().width - panel.getInsets().right - BORDER_BUFFER;
			}

			int getScrollSouth() {
				return getBounds().height - BUTTON_AREA_BUFFER;
			}

			int getButtonNorth() {
				int midArea = (BUTTON_AREA_BUFFER / 2) + getScrollSouth();
				int halfButton = BUTTON_HEIGHT / 2;
				return midArea - halfButton;
			}

			int getButtonSouth() {
				int buttonNorth = getButtonNorth();
				return buttonNorth + BUTTON_HEIGHT;
			}

			int getButtonWest() {
				int buttonWidth = getButtonWidth();
				int panelCenterX = GUIUtilities.getCenterX(panel.getWidth());
				int halfButton = buttonWidth / 2;
				return panelCenterX - halfButton;
			}

			int getButtonWidth() {
				return (int) (((float) getBounds().getWidth()) * BUTTON_WIDTH_RATIO);
			}

			int getButtonEast() {
				int buttonWest = getButtonWest();
				return buttonWest + getButtonWidth();
			}

			void buttonLayout(JButton button) {
				putConstraint(NORTH, button, getScrollSouth(), NORTH, panel);
				putConstraint(SOUTH, button, getButtonSouth(), NORTH, panel);
				putConstraint(WEST, button, getButtonWest(), WEST, panel);
				putConstraint(EAST, button, getButtonEast(), WEST, panel);
			}
		}
	}
}
