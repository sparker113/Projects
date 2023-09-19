import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import exceltransfer.OperatorTemplateStageSummary;

public class WriteToOperatorSummary implements Runnable {
	private String operator;
	final String[] STRINGNAMES = { "STRING", "Sand Type", "Substage Name", "Stage Number" };
	final String[] DOUBLENAMES = { "DOUBLE", "Prop Con", "Clean Total", "Average Pressure", "Average Rate",
			"Slurry Total", "Sand Volume" };
	final String[] DATENAMES = { "DATE", "Start Date", "End Date" };
	final String[] TIMENAMES = { "TIME", "Start Time", "End Time" };
	final String[] RANGEKEYS = { "Sand Volume Range", "Sand Name Range", "Chemical Volume Range",
			"Chemical Name Range" };
	final int STRING = 0;
	final int TIME = 1;
	final int DATE = 2;
	final int DOUBLE = 3;
	private Integer stage;
	private Integer offset;

	WriteToOperatorSummary(String operator, Integer stage) {
		this.operator = operator;
		this.stage = stage;
	}

	private void setOffset(HashMap<String, String> templateMap) {
		if (stage > 1 || !templateMap.containsKey("Offset")) {
			this.offset = 0;
		} else if (templateMap.containsKey("Offset")) {
			this.offset = Integer.valueOf(templateMap.get("Offset"));
		}
	}

	public HashMap<String, String> acquireTemplateMap() {
		HashMap<String, String> templateMap = null;
		try {
			templateMap = OperatorTemplateStageSummary.readOperatorStageSummary(operator, "Summary.txt");
		} catch (IOException e) {
			return null;
		}
		return templateMap;
	}

	private void saveWorkbook(XSSFWorkbook workbook, String path) throws FileNotFoundException, IOException {
		workbook.write(new FileOutputStream(path));
	}

	private String getWorkbookPath(LinkedHashMap<String, String> sigValsMap, HashMap<String, String> templateMap)
			throws IOException {
		String path = "";
		try {
			path = ReadDirectory.readDirect() + "\\" + sigValsMap.get("Well Name") + "\\" + sigValsMap.get("Well Name")
					+ templateMap.get("Workbook Suffix");
		} catch (IOException e) {
			System.out.println("Template not made for this operator");
			return "";
		}
		return path;
	}

	private XSSFWorkbook getWorkbook(String path) throws IOException {
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(path));
		return workbook;
	}

	private String getSheetName(XSSFWorkbook workbook, String sheetTemplate, String stage) {
		String templateName = sheetTemplate.replace("#", stage);
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			String sheetName = workbook.getSheetAt(i).getSheetName();
			Matcher matcher = Pattern.compile("\\d+").matcher(sheetName);
			matcher.find();
			if (sheetName.toUpperCase().contains(templateName.toUpperCase()) && matcher.group().equals(stage)) {
				return sheetName;
			}
		}
		return templateName;
	}

	private void writeDataColumn(ArrayList<String> dataColumn, String rC, String dataType, XSSFSheet sheet,
			XSSFWorkbook workbook) {
		if (rC.contains(";")) {
			rC = rC.split(";")[0];
		}

		int firstRow = Integer.valueOf(rC.split(",")[0]).intValue() + offset;
		int column = Integer.valueOf(rC.split(",")[1]).intValue();
		int i = 0;
		switch (dataType) {
		case ("STRING"):
			new ExcelTransfer<>(workbook, sheet, dataColumn, firstRow, column);
			break;
		case ("DOUBLE"):
			new ExcelTransfer<>(workbook, sheet, stringArrayToDouble(dataColumn), firstRow, column);
			break;
		case ("DATE"):
			for (String s : dataColumn) {
				java.sql.Date date = parseStringToDate(s);
				ExcelTransfer.checkNullCreateCell(sheet, firstRow + i, column);
				ExcelTransfer.changeTypeToDate(workbook, sheet, sheet.getRow(firstRow + i).getCell(column));
				sheet.getRow(firstRow + i).getCell(column).setCellValue(date);
				i++;
			}
			break;
		case ("TIME"):
			for (String s : dataColumn) {
				java.sql.Time time = parseStringToTime(s);
				ExcelTransfer.checkNullCreateCell(sheet, firstRow + i, column);
				ExcelTransfer.changeTypeToTime(workbook, sheet, sheet.getRow(firstRow + i).getCell(column));
				sheet.getRow(firstRow + i).getCell(column).setCellValue(time);
				i++;
			}
			break;
		}
	}

	private java.sql.Date parseStringToDate(String stringDate) {
		int year = Integer.valueOf(stringDate.split("-")[0]);
		int month = Integer.valueOf(stringDate.split("-")[1]);
		int day = Integer.valueOf(stringDate.split("-")[2]);
		java.sql.Date date = java.sql.Date.valueOf(LocalDate.of(year, month, day));
		return date;
	}

	private java.sql.Time parseStringToTime(String stringTime) {
		int hour = Integer.valueOf(stringTime.split(":")[0]).intValue();
		int minute = Integer.valueOf(stringTime.split(":")[1]).intValue();
		java.sql.Time time = java.sql.Time.valueOf(LocalTime.of(hour, minute));
		return time;
	}

	private ArrayList<Double> stringArrayToDouble(ArrayList<String> array) {
		ArrayList<Double> doubleArray = new ArrayList<>();
		for (String s : array) {
			if (s.equals("")) {
				doubleArray.add(0.0);
			} else {
				doubleArray.add(Double.valueOf(s));
			}
		}
		return doubleArray;
	}

	private String getDataTypeFromString(String columnName) {
		ArrayList<String[]> arrays = getArrayOfStringArrays();
		for (String[] array : arrays) {
			for (String s : array) {
				if (s.equals(columnName)) {
					return array[0];
				}
			}
		}
		return "";
	}

	private ArrayList<String[]> getArrayOfStringArrays() {
		ArrayList<String[]> arrayOfStringArrays = new ArrayList<>();
		arrayOfStringArrays.add(STRINGNAMES);
		arrayOfStringArrays.add(DOUBLENAMES);
		arrayOfStringArrays.add(DATENAMES);
		arrayOfStringArrays.add(TIMENAMES);
		return arrayOfStringArrays;
	}

	private void writeSummary(HashMap<String, String> templateMap, XSSFSheet sheet, XSSFWorkbook workbook) {
		HashMap<String, ArrayList<String>> mainTableMap = SheetData.getMainTableData(Main.yess.mTable);
		for (String s : templateMap.keySet()) {
			for (String ss : mainTableMap.keySet()) {
				if (s.equals("First " + ss + " Cell")) {
					writeDataColumn(mainTableMap.get(ss), templateMap.get(s), getDataTypeFromString(ss), sheet,
							workbook);

				}
			}
		}
	}

	private void writeSigValues(XSSFWorkbook workbook, XSSFSheet sheet, HashMap<String, String> templateMap,
			LinkedHashMap<String, String> sigValsMap) {
		for (String s : templateMap.keySet()) {
			if (!sigValsMap.keySet().contains(s)) {
				continue;
			}
			for (String rowColumn : templateMap.get(s).split(";")) {
				Matcher matcher = Pattern.compile("[^\\d\\.]").matcher(sigValsMap.get(s));
				Boolean nonNumeric = matcher.find();
				if (nonNumeric && !s.contains(":") && !s.contains("-")) {
					writeSigVal(workbook, sheet, sigValsMap.get(s), rowColumn, STRING);
				} else if (nonNumeric && s.contains(":")) {
					writeSigVal(workbook, sheet, sigValsMap.get(s), rowColumn, TIME);
				} else if (nonNumeric && s.contains("-")) {
					writeSigVal(workbook, sheet, sigValsMap.get(s), rowColumn, DATE);
				} else {
					writeSigVal(workbook, sheet, sigValsMap.get(s), rowColumn, DOUBLE);
				}
			}
		}
	}

	private void writeSigVal(XSSFWorkbook workbook, XSSFSheet sheet, String value, String address, int dataType) {
		Integer row = getRowFromString(address) + offset;
		Integer column = getColumnFromString(address);
		switch (dataType) {
		case (0):
			ExcelTransfer.checkNullCreateCell(sheet, row, column);
			ExcelTransfer.changeTypeToString(sheet, sheet.getRow(row).getCell(column));
			sheet.getRow(row).getCell(column).setCellValue(value);
			break;
		case (1):
			ExcelTransfer.checkNullCreateCell(sheet, row, column);
			ExcelTransfer.changeTypeToTime(workbook, sheet, sheet.getRow(row).getCell(column));
			sheet.getRow(row).getCell(column).setCellValue(value);
			break;
		case (2):
			ExcelTransfer.checkNullCreateCell(sheet, row, column);
			ExcelTransfer.changeTypeToDate(workbook, sheet, sheet.getRow(row).getCell(column));
			sheet.getRow(row).getCell(column).setCellValue(value);
			break;
		case (3):
			ExcelTransfer.checkNullCreateCell(sheet, row, column);
			ExcelTransfer.changeTypeToDouble(workbook, sheet, sheet.getRow(row).getCell(column), false);
			sheet.getRow(row).getCell(column).setCellValue(Double.valueOf(value));
			break;
		}
	}

	private Integer getRowFromString(String input) {
		return Integer.valueOf(input.split(",")[0]);
	}

	private Integer getColumnFromString(String input) {
		return Integer.valueOf(input.split(",")[1]);
	}

	private ArrayList<String> getUsedRangeKeys(HashMap<String, String> templateMap) {
		ArrayList<String> usedKeys = new ArrayList<>();
		for (String s : RANGEKEYS) {
			if (templateMap.containsKey(s)) {
				usedKeys.add(s);
			}
		}
		return usedKeys;
	}

	private void writeRangeValues(XSSFWorkbook workbook, XSSFSheet sheet, HashMap<String, String> templateMap,
			ArrayList<String> keys) {
		LinkedHashMap<String, HashMap<String, String>> sandMap = null;
		LinkedHashMap<String, String> chemMap = null;
		for (String s : keys) {
			switch (s) {
			case ("Sand Volume Range"):
				sandMap = SheetData.getSigTableData(Main.yess.diagTable3, 1);
				writeSandTotals(workbook, sheet, sandMap, getRangeArray(templateMap.get(s)));
				break;
			case ("Sand Name Range"):
				sandMap = SheetData.getSigTableData(Main.yess.diagTable3, 1);
				writeSandNames(sheet, sandMap, getRangeArray(templateMap.get(s)));
				break;
			case ("Chemical Volume Range"):
				chemMap = SheetData.getSigTableData(Main.yess.diagTable1);
				writeChemicalTotals(workbook, sheet, chemMap, getRangeArray(templateMap.get(s)));
				break;
			case ("Chemical Name Range"):
				chemMap = SheetData.getSigTableData(Main.yess.diagTable1);
				writeChemicalNames(sheet, chemMap, getRangeArray(templateMap.get(s)));
				break;
			}
		}
	}

	private void writeChemicalNames(XSSFSheet sheet, LinkedHashMap<String, String> chemMap,
			ArrayList<String> addressArray) {
		int i = 0;

		for (String s : chemMap.keySet()) {
			if (s.toUpperCase().contains("HCL") || s.toUpperCase().contains("ACID")) {
				continue;
			}
			Integer row = getRowFromString(addressArray.get(i)) + offset;
			Integer column = getColumnFromString(addressArray.get(i));
			ExcelTransfer.checkNullCreateCell(sheet, row, column);
			ExcelTransfer.changeTypeToString(sheet, sheet.getRow(row).getCell(column));
			sheet.getRow(row).getCell(column).setCellValue(s);
			i++;
		}
		if (i < addressArray.size()) {
			int newStart = checkForAddValidRange(addressArray, i - 1);
			System.out.println(newStart);
			System.out.println("--------------!!!!!!!!!!!!!!!!!Found next range for chemical names");
			if (newStart != 0) {
				writeChemicalNames(sheet, chemMap, addressArray, newStart);
			}
		}

	}

	private int checkForAddValidRange(ArrayList<String> addressArray, int lastIndex) {
		Integer row = getRowFromString(addressArray.get(lastIndex));
		Integer column = getColumnFromString(addressArray.get(lastIndex));
		Integer anotherRow = row;
		Integer anotherColumn = column;
		int i = 0;
		while (lastIndex < addressArray.size() && ((anotherRow - row == 0 & anotherColumn - column != 0)
				|| (column - anotherColumn == 0 & anotherRow - row != 0)) | i == 0) {
			lastIndex++;
			anotherRow = getRowFromString(addressArray.get(lastIndex));
			anotherColumn = getColumnFromString(addressArray.get(lastIndex));
			if (anotherRow != row & anotherColumn != column) {
				System.out.println("--------------!!!!!!!!!!!!!!!!!Found next range for chemical names");
				return lastIndex;
			}
			i++;
		}
		return 0;
	}

	private void writeChemicalNames(XSSFSheet sheet, LinkedHashMap<String, String> chemMap,
			ArrayList<String> addressArray, int startIndex) {
		int i = startIndex;

		for (String s : chemMap.keySet()) {
			if (s.toUpperCase().contains("HCL") || s.toUpperCase().contains("ACID")) {
				continue;
			}
			Integer row = getRowFromString(addressArray.get(i)) + offset;
			Integer column = getColumnFromString(addressArray.get(i));
			ExcelTransfer.checkNullCreateCell(sheet, row, column);
			ExcelTransfer.changeTypeToString(sheet, sheet.getRow(row).getCell(column));
			sheet.getRow(row).getCell(column).setCellValue(s);
			i++;
		}
		if (i < addressArray.size()) {
			int newStart = checkForAddValidRange(addressArray, i - 1);
			if (newStart != 0) {
				writeChemicalNames(sheet, chemMap, addressArray, newStart);
			}
		}
	}

	private void writeChemicalTotals(XSSFWorkbook workbook, XSSFSheet sheet, LinkedHashMap<String, String> chemMap,
			ArrayList<String> addressArray) {
		int i = 0;
		for (String s : chemMap.keySet()) {
			if (s.toUpperCase().contains("HCL") || s.toUpperCase().contains("ACID")) {
				continue;
			}
			Integer row = getRowFromString(addressArray.get(i)) + offset;
			Integer column = getColumnFromString(addressArray.get(i));
			ExcelTransfer.checkNullCreateCell(sheet, row, column);
			ExcelTransfer.changeTypeToDouble(workbook, sheet, sheet.getRow(row).getCell(column), false);
			sheet.getRow(row).getCell(column).setCellValue(Double.valueOf(chemMap.get(s)));
			i++;
		}
	}

	private void writeSandNames(XSSFSheet sheet, LinkedHashMap<String, HashMap<String, String>> sandMap,
			ArrayList<String> addressArray) {
		int i = 0;
		for (String s : sandMap.keySet()) {
			if (s.toUpperCase().contains("TOTAL")) {
				continue;
			}
			Integer row = getRowFromString(addressArray.get(i)) + offset;
			Integer column = getColumnFromString(addressArray.get(i));
			ExcelTransfer.checkNullCreateCell(sheet, row, column);
			ExcelTransfer.changeTypeToString(sheet, sheet.getRow(row).getCell(column));
			sheet.getRow(row).getCell(column).setCellValue(s);
			i++;
		}
		if (i < addressArray.size()) {
			i--;
			Integer lastIndex = checkForAddValidRange(addressArray, i);
			if (!lastIndex.equals(0)) {
				writeSandNames(sheet, sandMap, addressArray, lastIndex);
			}
		}
	}

	private void writeSandNames(XSSFSheet sheet, LinkedHashMap<String, HashMap<String, String>> sandMap,
			ArrayList<String> addressArray, Integer lastIndex) {
		int i = lastIndex;
		for (String s : sandMap.keySet()) {
			if (s.toUpperCase().contains("TOTAL")) {
				continue;
			}
			Integer row = getRowFromString(addressArray.get(i)) + offset;
			Integer column = getColumnFromString(addressArray.get(i));
			ExcelTransfer.checkNullCreateCell(sheet, row, column);
			ExcelTransfer.changeTypeToString(sheet, sheet.getRow(row).getCell(column));
			sheet.getRow(row).getCell(column).setCellValue(s);
			i++;
		}
		if (i < addressArray.size()) {
			i--;
			lastIndex = checkForAddValidRange(addressArray, i);
			if (!lastIndex.equals(0)) {
				writeSandNames(sheet, sandMap, addressArray, lastIndex);
			}
		}
	}

	private void hideStageSheets(XSSFWorkbook workbook, HashMap<String, String> templateMap) {

		String stageSheetTemplate = templateMap.get("Sheet Name");
		String firstStageSheetName = getFirstStageSheetName(workbook, stageSheetTemplate);
		String thisStageSheetName = stageSheetTemplate.replace("#", String.valueOf(stage));
		String stageSheetPrefix = stageSheetTemplate.substring(0, stageSheetTemplate.length() - 2);
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			String sheetName = workbook.getSheetName(i);
			if (sheetName.contains(stageSheetPrefix) && !sheetName.equals(firstStageSheetName)
					&& !sheetName.equals(thisStageSheetName)) {
				workbook.setSheetHidden(i, true);
			} else if (sheetName.equals(thisStageSheetName)) {
				workbook.setSheetHidden(i, false);
			}
		}
	}

	private String getFirstStageSheetName(XSSFWorkbook workbook, String stageTemplate) {
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			if (workbook.getSheetName(i).toUpperCase().contains(stageTemplate.toUpperCase().replace("#", "1"))) {
				return workbook.getSheetName(i);
			}
		}
		System.out.println("Unable to find stage 1 sheet name");
		return "";
	}

	private void writeSandTotals(XSSFWorkbook workbook, XSSFSheet sheet,
			LinkedHashMap<String, HashMap<String, String>> sandMap, ArrayList<String> addressArray) {
		int i = 0;
		for (String s : sandMap.keySet()) {

			if (s.toUpperCase().contains("TOTAL")) {
				continue;
			} else if (i == addressArray.size()) {
				break;
			}
			Integer row = getRowFromString(addressArray.get(i)) + offset;
			Integer column = getColumnFromString(addressArray.get(i));
			ExcelTransfer.checkNullCreateCell(sheet, row, column);
			ExcelTransfer.changeTypeToDouble(workbook, sheet, sheet.getRow(row).getCell(column), false);
			sheet.getRow(row).getCell(column).setCellValue(Double.valueOf(sandMap.get(s).get("Volume Pumped")));
			i++;
		}
	}

	private ArrayList<String> getRangeArray(String templateLocations) {
		ArrayList<String> addressArray = new ArrayList<>();
		for (String s : templateLocations.split(";")) {
			addressArray.add(s);
		}
		return addressArray;
	}

	@Override
	public void run() {
		LinkedHashMap<String, String> sigValsMap = SheetData.getSigTableData(Main.yess.diagTable2);
		HashMap<String, String> templateMap = acquireTemplateMap();
		if (templateMap == null) {
			return;
		}
		XSSFWorkbook workbook = null;
		String workbookPath = "";
		try {
			workbookPath = getWorkbookPath(sigValsMap, templateMap);
			workbook = getWorkbook(workbookPath);
		} catch (IOException e) {
			System.out.println("Exception caught reading path");
			return;
		}
		if (workbook == null) {
			return;
		}
		hideStageSheets(workbook, templateMap);
		setOffset(templateMap);
		XSSFSheet sheet = workbook
				.getSheet(getSheetName(workbook, templateMap.get("Sheet Name"), sigValsMap.get("Stage Number")));
		writeSummary(templateMap, sheet, workbook);
		writeSigValues(workbook, sheet, templateMap, sigValsMap);
		writeRangeValues(workbook, sheet, templateMap, getUsedRangeKeys(templateMap));
		try {
			saveWorkbook(workbook, getWorkbookPath(sigValsMap, templateMap));
		} catch (IOException e) {
			System.out.println("Issue saving operator summary workbook");
		}

	}
}
