import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
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
	private XSSFCellStyle cellStyle;
	private CellType cellType;
	Thread t;

	public ExcelTransfer(XSSFWorkbook workbook, XSSFSheet sheet, ArrayList<T> array, Integer startRow, Integer column,
			Integer excludeRow, Integer startRow2,XSSFCellStyle cellStyle,CellType cellType) {
		this.workbook = workbook;
		this.sheet = sheet;
		this.array = array;
		this.startRow = startRow;
		this.column = column;
		this.excludeRow = excludeRow;
		this.startRow2 = startRow2;
		this.cellStyle = cellStyle;
		this.cellType = cellType;
		this.t = new Thread(this, "Excel Transfer - " + column);
		t.setDaemon(false);
		t.start();
	}
	public ExcelTransfer(XSSFWorkbook workbook, XSSFSheet sheet, ArrayList<T> array, Integer startRow, Integer column,
			Integer excludeRow, Integer startRow2) {
		this.workbook = workbook;
		this.sheet = sheet;
		this.array = array;
		this.startRow = startRow;
		this.column = column;
		this.excludeRow = excludeRow;
		this.startRow2 = startRow2;
		this.cellStyle = getStringStyle(sheet,startRow,column);
		this.cellType = CellType.STRING;
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
		this.cellStyle = getStringStyle(sheet,startRow,column);
		this.cellType = CellType.STRING;
		this.t = new Thread(this, "Excel Transfer - 1st Constructor");
		t.setDaemon(false);
		t.start();
	}

	ExcelTransfer(XSSFWorkbook workbook, XSSFSheet sheet, ArrayList<T> array1, ArrayList<T> arrayOverlay,
			Integer startRow, Integer column, Integer excludeRow, Integer startRow2,XSSFCellStyle cellStyle) {
		this.workbook = workbook;
		this.sheet = sheet;
		this.array1 = array1;
		this.arrayOverlay = arrayOverlay;
		this.column = column;
		this.startRow = startRow;
		this.startRow2 = startRow2;
		this.excludeRow = excludeRow;
		this.cellStyle = cellStyle;
		this.cellType = CellType.STRING;
		this.array = getNameColumn();
		this.t = new Thread(this, "Excel Transfer - 2nd Constructor");
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
		this.cellStyle = getStringStyle(sheet,startRow,column);
		this.cellType = CellType.STRING;
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
		this.cellStyle = getStringStyle(sheet,startRow,column);
		this.cellType = CellType.STRING;
		if (startRow == -1) {
			JOptionPane.showMessageDialog(null, "Unable to find " + findString + " in the specified column");
			return;
		}
		this.t = new Thread(this, "Excel Transfer - 3rd Constructor");
		t.setDaemon(false);
		t.start();
	}

	public static XSSFCellStyle getTimeStyle(XSSFWorkbook workbook, XSSFCell cell) {
		XSSFCellStyle cellStyle = workbook.createCellStyle();
		CreationHelper createHelper = workbook.getCreationHelper();
		cellStyle.cloneStyleFrom(cell.getCellStyle());
		cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("HH:mm"));
		return cellStyle;
	}
	public static XSSFCellStyle getTimeStyle(XSSFWorkbook workbook, XSSFSheet sheet,int row,int column) {
		XSSFCellStyle cellStyle = workbook.createCellStyle();
		CreationHelper createHelper = workbook.getCreationHelper();
		XSSFCell cell = checkCreateCell(sheet,row,column);
		cellStyle.cloneStyleFrom(cell.getCellStyle());
		cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("HH:mm;@"));
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

	public static XSSFCellStyle getDateStyle(XSSFWorkbook workbook, XSSFCell cell) {
		XSSFCellStyle cellStyle = workbook.createCellStyle();
		CreationHelper createHelper = workbook.getCreationHelper();
		cellStyle.cloneStyleFrom(cell.getCellStyle());
		cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("MM/dd/yyyy"));
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
	public static void changeTypeToDate(XSSFWorkbook workbook, XSSFSheet sheet,int row,int column) {
		XSSFCell cell = checkCreateCell(sheet,row,column);
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
		if(matcher.find()) {
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
			if(row.equals("-1")) {
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
			if(column.equals("-1")) {
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
			Boolean decimal) {
		XSSFCell cell = checkCreateCell(sheet, row, column);
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
	public synchronized static void changeTypeToDouble(XSSFWorkbook workbook, XSSFSheet sheet, int row, int column,
			int decimalPlaces) {
		XSSFCell cell = checkCreateCell(sheet, row, column);
		CreationHelper createHelper = workbook.getCreationHelper();
		checkForFormulaAndRemove(cell);
		CellStyle cellStyle = cell.getCellStyle();
		if (decimalPlaces>0) {
			cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("####."+getRepeatedValue("#",decimalPlaces)));
		} else {
			cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("####"));
		}

		sheet.getRow(row).createCell(column);
		sheet.getRow(row).getCell(column).setCellStyle(cellStyle);
		sheet.getRow(row).getCell(column).setCellType(CellType.NUMERIC);
	}
	public static XSSFCellStyle getDoubleStyle(XSSFWorkbook workbook,XSSFSheet sheet,int row,int column,int decimalPlaces) {
		XSSFCell cell = checkCreateCell(sheet, row, column);
		CreationHelper createHelper = workbook.getCreationHelper();
		checkForFormulaAndRemove(cell);
		XSSFCellStyle cellStyle = cell.getCellStyle();
		if (decimalPlaces>0) {
			cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("####."+getRepeatedValue("#",decimalPlaces)));
		} else {
			cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("####"));
		}
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
	public static String getRepeatedValue(String value,int numTimes) {
		String valueIt = "";
		for(int i=0;i<numTimes;i++) {
			valueIt+=value;
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
	public synchronized static void checkForFormulaAndRemove(XSSFSheet sheet,int row,int column) {
		XSSFCell cell = sheet.getRow(row).getCell(column);
		if(cell==null) {
			cell = checkCreateCell(sheet, row, column);
		}
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
	public static XSSFCellStyle getStringStyle(XSSFSheet sheet,int row,int column) {
		XSSFCellStyle cellStyle = checkCreateCell(sheet,row,column).getCellStyle();
		CreationHelper creationHelper = sheet.getWorkbook().getCreationHelper();
		cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("Text"));
		return cellStyle;
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

	public static XSSFCell checkNullCreateCell(XSSFSheet sheet, int row, int column) {
		if (sheet.getRow(row) == null) {
			sheet.createRow(row);
		}
		if (sheet.getRow(row).getCell(column) == null) {
			return sheet.getRow(row).createCell(column);
		}
		return sheet.getRow(row).getCell(column);
	}

	public void writeColumn() {
		for (int i = 0; i < array.size() - excludeRow; i++) {
			XSSFCell cell = checkCreateCell(sheet, startRow + i, column);
			checkForFormulaAndRemove(cell);
			cell.setCellStyle(cellStyle);
			if(cellStyle.getDataFormatString().equals("HH:mm;@")) {
				cell.setCellValue(DateUtil.convertTime((String)array.get(i)));
			}else if (array.get(0).getClass().toString().contains("String")) {
				cell.setCellValue(String.valueOf(array.get(i)));
			} else {
				// removeFormula(sheet.getRow(i+startRow).getCell(column),sheet);
				if (!array.get(i).equals(0.0)) {
					cell.setCellValue(Double.valueOf(String.valueOf(array.get(i))));
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
			XSSFCell cell = checkCreateCell(sheet, startRow2 + ii, column);
			checkForFormulaAndRemove(cell);
			cell.setCellType(cellType);
			if(cellStyle.getDataFormatString().equals("HH:mm;@")) {
				cell.setCellValue(DateUtil.convertTime((String)array.get(i)));
			}else if (array.get(0).getClass().toString().contains("String")) {
				cell.setCellValue(String.valueOf(array.get(i)));
			} else {
				if (!array.get(i).equals(0.0)) {
					cell.setCellValue(Double.valueOf(String.valueOf(array.get(i))));
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
}
