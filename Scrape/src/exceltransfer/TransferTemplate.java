package exceltransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.channels.Channel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class TransferTemplate implements Serializable, AutoCloseable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public final static String COLUMN = "column";
	public final static String ROW = "row";
	public final static String STAGE_NUMBER = "#";
	public final static String PARENT_FOLDER = "Operator_Templates";
	private final static String FILE_EXT = ".scp";
	public final static String WELL_SUMMARY_NAME = "well_summary";
	public final static String WELL_SUMMARY_OPERATOR = "well_summary";
	public final static String BASE_TEMPLATE_NAME = "Transfer_";
	public final static String TOTAL_SUMMARY_NAME = "total_summary";
	public final static String TOTAL_SUMMARY_OPERATOR = "all_possible";
	public final static int ROW_INDEX = 0;
	public final static int COLUMN_INDEX = 1;
	public final static String SAND_NAME = "sand";
	public final static String CHEM_NAME = "chemicals";
	public final static String NAME_RANGE = "Name Range";
	public final static String VOLUME_RANGE = "Volume Range";

	private final static int DEFAULT_RANGE_LENGTH = 15;

	private final static int VALUE_FLOAT_SIZE = 4;

	public final static String MATCH_FOUND = "found";

	private final static int MAX_MATCH_ITERATIONS = 777;

	private final static String MATCH_REGEX = "match\\(((.+)?)\\)";

	public final static String DOWN = "down";

	public final static String RIGHT = "right";

	private final static int MAX_INDEX = 7777;

	public final static String MATCH = "match";

	private final static int MATCH_VALUE_INDEX = 0;

	private final static int MATCH_ROW_INDEX = 1;
	private final static int MATCH_COLUMN_INDEX = 2;

	private final static int MATCH_DOWN_RIGHT_INDEX = 3;

	private final static String MATCH_VALUE = "value";

	private final static String MATCH_ROW = "row";

	private final static String MATCH_COLUMN = "column";

	private final static String MATCH_DOWN_RIGHT = "down_right";

	public static boolean arrayContainsValue(String[] array, String value) {
		for (String s : array) {
			if (s.equals(value)) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("resource")
	public static boolean checkFileOpen(File file) {
		if (file == null || !file.exists()) {
			return true;
		}
		boolean open = false;
		Channel channel = null;
		try {
			channel = new RandomAccessFile(file, "rw").getChannel();
		} catch (IOException e) {
			open = true;
		} finally {
			if (channel != null) {
				try {
					channel.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return open;
	}

	private static Float containsNumeric(String string) {
		Matcher matcher = Pattern.compile("\\d+").matcher(string);
		if (matcher.find()) {
			return Float.valueOf(matcher.group());
		}
		return null;
	}

	public static HashMap<String, Integer> getBaseRCMap(Integer row, Integer column) {
		HashMap<String, Integer> map = new HashMap<>();
		map.put(ROW, row);
		map.put(COLUMN, column);
		map.put(MATCH_FOUND, 0);
		return map;
	}

	public static String getCellStringValue(XSSFSheet sheet, int row, int col) {
		if (sheet.getRow(row) == null || sheet.getRow(row).getCell(col) == null) {
			return "";
		}
		if (sheet.getRow(row).getCell(col).getCellType() == CellType.NUMERIC) {
			double value;
			return (value = sheet.getRow(row).getCell(col).getNumericCellValue()) > 0 ? String.valueOf(value) : "";
		} else if (sheet.getRow(row).getCell(col).getCellType() == CellType.FORMULA) {
			return getCellValue(sheet.getRow(row).getCell(col),
					sheet.getRow(row).getCell(col).getCachedFormulaResultType());
		}
		return sheet.getRow(row).getCell(col).getStringCellValue();
	}

	public static String getCellValue(XSSFCell cell, CellType type) {

		if (type == CellType.NUMERIC) {
			double value;
			return (value = cell.getNumericCellValue()) > 0.0 ? String.valueOf(value) : "";
		}
		return cell.getStringCellValue();
	}

	public static String getExcelStringValue(XSSFSheet sheet, int row, int column) {
		if (sheet.getRow(row) == null || sheet.getRow(row).getCell(column) == null) {
			return "";
		}
		return sheet.getRow(row).getCell(column).getRawValue();
	}

	public static String getMatchAddress(String matchString, String downRight, int row, int column) {
		return row + "," + column + "->" + matchString;
	}

	public static HashMap<String, Integer> getMatchFuncLocation(String cellAddress) {
		String r1C1 = cellAddress.split("\\-\\>")[0];
		HashMap<String, Integer> map = new HashMap<>();
		map.put(ROW, Integer.valueOf(r1C1.split(",")[0]));
		map.put(COLUMN, Integer.valueOf(r1C1.split(",")[1]));
		return map;
	}

	public static LinkedHashSet<HashMap<String, Integer>> getMatchFuncLocation(String cellAddress, boolean forConfig) {
		LinkedHashSet<HashMap<String, Integer>> set = new LinkedHashSet<>();
		for (String s : cellAddress.split(";")) {
			set.add(getMatchFuncLocationForOne(s, forConfig));
		}
		return set;
	}

	public static HashMap<String, Integer> getMatchFuncLocationForOne(String cellAddress, boolean forConfig) {
		String r1C1 = cellAddress.split("\\-\\>")[0];
		HashMap<String, Integer> map = new HashMap<>();
		map.put(ROW, Integer.valueOf(r1C1.split(",")[0]));
		map.put(COLUMN, Integer.valueOf(r1C1.split(",")[1]) + (forConfig ? 1 : 0));
		return map;
	}

	public static String getMatchString(String searchValue, String row, String column, String downRight) {
		return MATCH + "([" + searchValue + "]," + row + "," + column + "," + downRight + ")";
	}

	public static TransferTemplate getNewTotalSummaryTemplateObject() {
		String filePath = getTotalSummaryTemplateFilePath();
		return new TransferTemplate(filePath);
	}

	public static String getNextTemplateName(String operator) {
		String[] templateNames = getOperatorTemplates(operator);
		if (templateNames == null) {
			return BASE_TEMPLATE_NAME + 1;
		}
		int count = 1;
		String name = BASE_TEMPLATE_NAME + count;
		while (arrayContainsValue(templateNames, name) && count < 100) {
			count++;
			name = BASE_TEMPLATE_NAME + count;
		}
		return name;
	}

	public static String[] getOperatorTemplates(String operator) {
		File file = new File(PARENT_FOLDER + "\\" + operator);
		if (!file.exists() || file.list().length == 0) {
			return null;
		}
		return file.list();
	}

	public static String[] getSavedOperators() {
		File file = new File(PARENT_FOLDER);
		if (!file.exists() || file.list().length == 0) {
			return null;
		}
		return file.list();
	}

	public static String getTemplatePath(String operator, String name) {
		Matcher matcher = Pattern.compile(FILE_EXT + "$").matcher(name);
		if (matcher.find()) {
			return PARENT_FOLDER + "\\" + operator + "\\" + name;
		}
		return PARENT_FOLDER + "\\" + operator + "\\" + name + FILE_EXT;
	}

	public static String getTotalSummaryTemplateFilePath() {
		return PARENT_FOLDER + "\\" + TOTAL_SUMMARY_NAME + FILE_EXT;
	}

	public static String getWellSummaryTemplatePath() {
		return PARENT_FOLDER + "\\" + WELL_SUMMARY_OPERATOR + "\\" + WELL_SUMMARY_NAME + FILE_EXT;
	}

	public static boolean isMatchFunc(String cellAddress) {
		return cellAddress.contains("->");
	}

	public static boolean isNumeric(String string) {
		Matcher matcher = Pattern.compile("(^(\\-?)(\\d+)(\\.(\\d*))?)").matcher(string);
		if(matcher.find()) {
			String found = matcher.group();
			System.out.println(found);
			return found.equals(string);
		}
		return false;
	}

	public static void makeDir(String path) {
		File file = new File(path);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
	}

	/*
	 * The return TransferTemplate object adopts the name and operator parameters
	 * from transferTemplate1
	 */
	public static TransferTemplate mergeTemplates(TransferTemplate transferTemplate1,
			TransferTemplate transferTemplate2) {
		transferTemplate1.getLocationMap().putAll(transferTemplate2.getLocationMap());
		return transferTemplate1;
	}

	public static TransferTemplate readFromFile(String path) throws IOException, ClassNotFoundException {
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		TransferTemplate transferTemplate;
		try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file.getAbsolutePath()));) {
			transferTemplate = (TransferTemplate) objectInputStream.readObject();
			return transferTemplate;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void writeToFile(TransferTemplate transferTemplate) throws IOException {
		String path = transferTemplate.getFilePath();
		makeDir(path);
		try(ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(new File(path)))){
			objectOutputStream.writeObject(transferTemplate);
		}
	}

	private String name;

	private String operator;

	private String path;

	private String sheetName;

	private String workbookPath;

	private HashMap<String, String> locationMap;

	TransferTemplate() {
		name = WELL_SUMMARY_NAME;
		operator = WELL_SUMMARY_OPERATOR;
		this.path = PARENT_FOLDER + "\\" + operator + "\\" + name + FILE_EXT;
		locationMap = new HashMap<>();
	}

	private TransferTemplate(String filePath) {
		name = TOTAL_SUMMARY_NAME;
		operator = TOTAL_SUMMARY_OPERATOR;
		this.path = filePath;
		locationMap = new HashMap<>();
	}

	TransferTemplate(String name, String operator) {
		this.name = name;
		this.operator = operator;
		this.path = PARENT_FOLDER + "\\" + operator + "\\" + name + FILE_EXT;
		locationMap = new HashMap<>();
	}

	public void addToMap(String dataName, String matchFunc) {
		locationMap.put(dataName, matchFunc);
	}

	public void addToMap(String dataName, String row, String column) {
		if (!locationMap.containsKey(dataName)) {
			locationMap.put(dataName, getRowColString(row, column));
			return;
		}
		removeAddressFromMap(row + "," + column);
		String locationsString = locationMap.get(dataName) + ";" + row + "," + column;
		locationMap.put(dataName, locationsString);
	}

	private boolean arrayContainsValue(String value, String[] array) {
		for (String s : array) {
			if (value.toLowerCase().equals(s.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	private boolean cellValueMatches(XSSFSheet sheet, int row, int column, String... values) {
		CellType cellType = sheet.getRow(row).getCell(column).getCellType();
		String cellValue;
		if (cellType == CellType.NUMERIC) {
			cellValue = String.valueOf(sheet.getRow(row).getCell(column).getNumericCellValue());
		} else {
			cellValue = sheet.getRow(row).getCell(column).getStringCellValue();
		}
		return arrayContainsValue(cellValue, values);
	}

	public boolean checkEqualSize(String[] array1, String[] array2) {
		return array1.length == array2.length;
	}

	private int determineIntArraySize(int start1, int end1, int start2, int end2) {
		int diff1 = Math.abs(end1 - start1);
		int diff2 = Math.abs(end2 - start2);
		return Math.max(diff1, diff2);
	}

	private String fillInLocationFunc(Map<String, String> sigValsMap, String locationFunc) {
		Matcher matcher = Pattern.compile("\\[(.+?)\\]").matcher(locationFunc);
		String corrected = new String(locationFunc);
		while (matcher.find()) {
			String valName = matcher.group().substring(1, matcher.group().length() - 1);
			if (sigValsMap.containsKey(valName)) {
				corrected.replace(matcher.group(), sigValsMap.get(valName));
			}
			matcher.reset(corrected);
		}
		return corrected;
	}

	private HashMap<String, Integer> findRowColumn(String dataValue, XSSFSheet sheet,
			HashMap<String, String> matchMap) {
		HashMap<String, Integer> map = getBaseRCMap(Integer.valueOf(matchMap.get(MATCH_ROW)),
				Integer.valueOf(matchMap.get(MATCH_COLUMN)));
		int addColumn = Boolean.compare(Boolean.valueOf(matchMap.get(MATCH_DOWN_RIGHT).equals(RIGHT)), true);
		int addRow = Boolean.compare(Boolean.valueOf(matchMap.get(MATCH_DOWN_RIGHT).equals(RIGHT)), true);

		int count = 0;
		for (int i = 0; i < MAX_MATCH_ITERATIONS; i++) {
			String cellValue = getCellStringValue(sheet, map.get(ROW), map.get(COLUMN));
			if (!dataValue.equals("") & cellValue.equals("")) {
				map.put(ROW, map.get(ROW) + addRow);
				map.put(COLUMN, map.get(COLUMN) + addColumn);
				continue;
			} else if (dataValue.equals("") & cellValue.equals("")) {
				map.put(MATCH_FOUND, 1);
				return map;
			}
			if (cellValue.matches("\\d+") & dataValue.equals(cellValue)) {
				map.put(MATCH_FOUND, 1);
				return map;
			}
			Float numValue;
			if (dataValue.equals(cellValue)
					|| ((numValue = containsNumeric(dataValue)) != null && numValue == Float.valueOf(cellValue))) {
				map.put(MATCH_FOUND, 1);
				return map;
			}
			map.put(ROW, map.get(ROW) + addRow);
			map.put(COLUMN, map.get(COLUMN) + addColumn);
		}
		return null;
	}

	public String getBaseMatchAddr(String matchFuncString) {
		Matcher matcher = Pattern.compile("^(\\d+),(\\d+)").matcher(matchFuncString);
		if (matcher.find()) {
			return matcher.group();
		}
		return "-1,-1";
	}

	public HashMap<String, Integer> getCellRowColumn(Map<String, String> sigValsMap, String dataName, XSSFSheet sheet) {
		if (isMatchFunc(locationMap.get(dataName))) {
			return matchFunc(sigValsMap, dataName, sheet);
		}
		return getRCMap(locationMap.get(dataName));
	}

	public HashMap<String, Integer> getCellRowColumn(String dataName) {
		if (isMatchFunc(locationMap.get(dataName))) {
			return getMatchFuncLocation(locationMap.get(dataName));
		}
		Matcher matcher = Pattern.compile("[^\\d\\,]").matcher(locationMap.get(dataName));
		if (matcher.find()) {
			return getRowColumnOfRawMatch(locationMap.get(dataName));
		}
		return getRCMap(locationMap.get(dataName));
	}

	public LinkedHashSet<HashMap<String, Integer>> getCellRowColumn(String dataName, boolean forConfig) {
		if (isMatchFunc(locationMap.get(dataName))) {
			return getMatchFuncLocation(locationMap.get(dataName), forConfig);
		}
		return getRCMap(locationMap.get(dataName), forConfig);
	}

	public String getFilePath() {
		return this.path;
	}

	private ArrayList<Integer> getIntArray(int start, int end) {
		ArrayList<Integer> array = new ArrayList<>();
		for (int i = start; i <= end; i++) {
			array.add(i);
		}
		return array;
	}

	private ArrayList<Integer> getIntArray(int start, int end, int size) {
		ArrayList<Integer> array = new ArrayList<>();
		int index = start;
		while (index <= end & array.size() < size) {
			array.add(index);
			index = index == end ? end : index + 1;
		}
		return array;
	}

	public HashMap<String, String> getLocationMap() {
		return this.locationMap;
	}

	public String getModifiedValue(String cellAddress, String existingValue) {
		Matcher matcher = Pattern.compile("(;" + cellAddress + ")|(^" + cellAddress + ";)").matcher(existingValue);
		String newValue = existingValue;
		if (matcher.find()) {
			existingValue = matcher.replaceAll("");
		}
		return existingValue;
	}

	public String getName() {
		return this.name;
	}

	public String getOperator() {
		return this.operator;
	}

	private String getParamString(String locationFunc) {
		Matcher matcher = Pattern.compile("\\(((.+)?)\\)").matcher(locationFunc);
		String paramString = locationFunc;
		if (matcher.find()) {
			paramString = matcher.group().substring(1, matcher.group().length() - 1);
		}
		return paramString;
	}

	public HashMap<String, Integer> getRCMap(String rCString) {
		HashMap<String, Integer> map = new HashMap<>();
		map.put(ROW, Integer.valueOf(rCString.split(",")[0]));
		map.put(COLUMN, Integer.valueOf(rCString.split(",")[1]));
		return map;
	}

	public LinkedHashSet<HashMap<String, Integer>> getRCMap(String rCString, boolean forConfig) {
		LinkedHashSet<HashMap<String, Integer>> set = new LinkedHashSet<>();
		HashMap<String, Integer> map = new HashMap<>();
		for (String s : rCString.split(";")) {
			set.add(getRCMapForOne(s, forConfig));
		}
		return set;
	}

	public HashMap<String, Integer> getRCMapForOne(String rCString, boolean forConfig) {
		HashMap<String, Integer> map = new HashMap<>();
		map.put(ROW, Integer.valueOf(rCString.split(",")[0]));
		map.put(COLUMN, Integer.valueOf(rCString.split(",")[1]) + (forConfig ? 1 : 0));
		return map;
	}

	private HashMap<String, LinkedHashMap<String, ArrayList<Integer>>> getRCMapForRange(String startKey[],
			String endKey[]) {
		HashMap<String, LinkedHashMap<String, ArrayList<Integer>>> array = new HashMap<>();
		int rangeCount = 0;
		for (String s : startKey) {
			String e = endKey[rangeCount];
			rangeCount++;
			for (String start : locationMap.get(s).split(";")) {
				int count = 0;
				int rowStart = Integer.valueOf(start.substring(0, start.indexOf(',')));
				int columnStart = Integer.valueOf(start.substring(start.indexOf(',') + 1));
				int rowEnd = locationMap.containsKey(e) && locationMap.get(e).split(";").length > count
						? Integer.valueOf(locationMap.get(e).split(";")[count].substring(0,
								locationMap.get(e).split(";")[count].indexOf(',')))
						: rowStart + DEFAULT_RANGE_LENGTH;
				int columnEnd = locationMap.containsKey(e) && locationMap.get(e).split(";").length > count
						? Integer.valueOf(locationMap.get(e).split(";")[count]
								.substring(locationMap.get(e).split(";")[count].indexOf(',') + 1))
						: columnStart;
				LinkedHashMap<String, ArrayList<Integer>> tempMap = new LinkedHashMap<>();
				int size = determineIntArraySize(rowStart, rowEnd, columnStart, columnEnd);
				tempMap.put(ROW, getIntArray(rowStart, rowEnd, size));
				tempMap.put(COLUMN, getIntArray(columnStart, columnEnd, size));
				array.put(s, tempMap);
				count++;
			}
		}
		return array;
	}

	private String getRowColString(String row, String column) {
		return row + "," + column;
	}

	private HashMap<String, Integer> getRowColumnMap(int row, int column) {
		HashMap<String, Integer> map = new HashMap<>();
		map.put(COLUMN, column);
		map.put(ROW, row);
		return map;
	}

	private HashMap<String, Integer> getRowColumnOfRawMatch(String rawMatchString) {
		Matcher matcher = Pattern.compile("\\(((.+)?)\\)").matcher(rawMatchString);
		if (matcher.find()) {
			HashMap<String, String> map = matchFuncMap(matcher.group().substring(1, matcher.group().length() - 1));
			return getRowColumnMap(Integer.valueOf(map.get(ROW)), Integer.valueOf(map.get(COLUMN)));
		}
		return getRowColumnMap(-1, -1);
	}

	public XSSFSheet getSheet(XSSFWorkbook workbook) {
		XSSFSheet sheet = workbook.getSheet(sheetName);
		return sheet;
	}

	public String getSheetName() {
		return this.sheetName;
	}

	public XSSFWorkbook getWorkbook() throws IOException, InvalidFormatException {
		File file = new File(workbookPath);
		if (checkFileOpen(file)) {
			return null;
		}
		XSSFWorkbook workbook = new XSSFWorkbook(new File(workbookPath));
		return workbook;
	}

	public String getWorkbookPath() {
		return this.workbookPath;
	}

	public HashMap<String, Integer> matchFunc(Map<String, String> sigValsMap, String dataName, XSSFSheet sheet) {
		String matchString = locationMap.get(dataName).substring(locationMap.get(dataName).indexOf('>') + 1);
		HashMap<String, String> matchMap = matchFuncMap(matchString);
		HashMap<String, Integer> baseRCMap = getRCMap(getBaseMatchAddr(matchString));
		String changeValue = matchMap.get(MATCH_DOWN_RIGHT).equals(RIGHT) ? COLUMN : ROW;
		HashMap<String, Integer> foundAddressMap = findRowColumn(
				dataName.equals(DataNames.EMPTY_CELL_VALUE) ? "" : sigValsMap.get(dataName), sheet, matchMap);
		if (foundAddressMap == null || foundAddressMap.get(MATCH_FOUND) == null) {
			return null;
		}
		baseRCMap.put(changeValue, foundAddressMap.get(changeValue));
		return baseRCMap;
	}

	private HashMap<String, String> matchFuncMap(String locationFunc) {
		HashMap<String, String> map = new HashMap<>();
		String[] paramStringArr = getParamString(locationFunc).split(",");
		map.put(MATCH_VALUE, paramStringArr[MATCH_VALUE_INDEX]);
		map.put(MATCH_ROW, paramStringArr[MATCH_ROW_INDEX]);
		map.put(MATCH_COLUMN, paramStringArr[MATCH_COLUMN_INDEX]);
		map.put(MATCH_DOWN_RIGHT, paramStringArr[MATCH_DOWN_RIGHT_INDEX]);
		return map;

	}

	public void populateTable(OperatorTemplateStageSummary.TemplateStageSummaryTable table) {
		for (Map.Entry<String, String> entry : getLocationMap().entrySet()) {
			LinkedHashSet<HashMap<String, Integer>> set = getCellRowColumn(entry.getKey(), true);
			String dataName = entry.getKey();
			for (HashMap<String, Integer> rcMap : set) {
				if (isMatchFunc(entry.getValue())) {
					dataName += entry.getValue().substring(entry.getValue().indexOf('>') + 1);
				}
				table.setValueAt(dataName, rcMap.get(ROW), rcMap.get(COLUMN));
			}
		}
	}

	public void removeAddressFromMap(String cellAddress) {
		HashSet<String> removeKeys = new HashSet<>();
		HashMap<String, String> modifyMap = new HashMap<>();
		for (Map.Entry<String, String> entry : locationMap.entrySet()) {
			if (entry.getValue().equals(cellAddress)) {
				removeKeys.add(entry.getKey());
			} else if (entry.getValue().contains(cellAddress)) {
				modifyMap.put(entry.getKey(), getModifiedValue(cellAddress, entry.getValue()));
			}
		}
		removeKeys(removeKeys);
		locationMap.putAll(modifyMap);
	}

	public void removeKeys(HashSet<String> removeSet) {
		for (String s : removeSet) {
			locationMap.remove(s);
		}
	}

	public void saveWorkbook(XSSFWorkbook workbook, File file) throws IOException, ExecutionException {
		try (FileOutputStream fileOutputStream = new FileOutputStream(file);) {
			workbook.write(fileOutputStream);
			workbook.close();
		}
	}
	public static String getCopyWorkbookPath(File file) {
		String dir = file.getParent();
		return dir+"\\"+COPY_FILE_NAME+"."+(file.getName().endsWith("xlsm")?"xlsm":"xlsx");
	}
	private final static String COPY_FILE_NAME = "workbook_copy";
	public boolean searchRow(HashMap<String, String> matchMap) {
		return matchMap.get(MATCH_DOWN_RIGHT).equals(RIGHT);
	}

	private HashMap<String, Integer> searchSheetForValue(HashMap<String, String> funcMap, XSSFSheet sheet) {
		int row = Integer.valueOf(funcMap.get(MATCH_ROW));
		int column = Integer.valueOf(funcMap.get(MATCH_COLUMN));

		for (int i = 0; i < MAX_INDEX; i++) {
			if (funcMap.get(MATCH_VALUE).toLowerCase().equals(getCellStringValue(sheet, row, column).toLowerCase())) {
				break;
			}
			row += funcMap.get(MATCH_DOWN_RIGHT).equals(DOWN) ? 1 : 0;
			column += funcMap.get(MATCH_DOWN_RIGHT).equals(RIGHT) ? 1 : 0;
		}
		return getRowColumnMap(row, column);
	}

	public static void setCellStringValue(XSSFSheet sheet, String stageValue, HashMap<String, Integer> rcMap) {
		ExcelTransfer.changeTypeToString(sheet, rcMap.get(ROW), rcMap.get(COLUMN));
		sheet.getRow(rcMap.get(ROW)).getCell(rcMap.get(COLUMN)).setCellValue(stageValue);

	}

	public static void setCellStringValue(XSSFSheet sheet, String stageValue, Integer row, Integer column) {
		ExcelTransfer.changeTypeToString(sheet, row, column);
		sheet.getRow(row).getCell(column).setCellValue(stageValue);
	}

	public static void setCellValueNumericAdd(XSSFSheet sheet, String stageValue, Integer row, Integer column) {
		Double value = Double.valueOf(stageValue);
		//ExcelTransfer.changeTypeToNumeric(sheet, row, column);
		String cellValueString = getStringNumber(getCellValue(sheet.getRow(row).getCell(column),CellType.NUMERIC));
		Double cellValue = Double.valueOf(cellValueString);
		sheet.getRow(row).getCell(column).setCellStyle(
				ExcelTransfer.getPatternStyle(sheet.getWorkbook(), sheet, row, column, getNumericPattern(value)));
		sheet.getRow(row).getCell(column).setCellValue(cellValue==0.0?Double.valueOf(stageValue):cellValue+value);
	}
	public static String getStringNumber(String string) {
		return isNumeric(string)?string:"0.0";
	}
	public static String getNumericPattern(Double value) {
		if (value < 10) {
			return "0.00";
		} else if (value < 100) {
			return "00.0";
		} else {
			return "##############";
		}
	}

	public static void setCellValueNumeric(XSSFSheet sheet, String stageValue, HashMap<String, Integer> rcMap) {
		if (stageValue.length() < 3) {
			ExcelTransfer.changeTypeToNumeric(sheet, rcMap.get(ROW), rcMap.get(COLUMN));
			sheet.getRow(rcMap.get(ROW)).getCell(rcMap.get(COLUMN)).setCellValue(Double.valueOf(stageValue));
			return;
		}
		ExcelTransfer.changeTypeToDouble(sheet.getWorkbook(), sheet, rcMap.get(ROW), rcMap.get(COLUMN),
				VALUE_FLOAT_SIZE - stageValue.length());
		sheet.getRow(rcMap.get(ROW)).getCell(rcMap.get(COLUMN)).setCellValue(Double.valueOf(stageValue));
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}

	private void setValueInCell(XSSFSheet sheet, String value, int row, int column) {
		Matcher matcher = Pattern.compile("[^\\d\\.\\-]").matcher(value);
		if (matcher.find()) {
			ExcelTransfer.changeTypeToString(sheet, row, column);
			sheet.getRow(row).getCell(column).setCellValue(value);
		} else {
			ExcelTransfer.changeTypeToNumeric(sheet, row, column);
			sheet.getRow(row).getCell(column).setCellValue(Float.valueOf(value));
		}
	}

	public void setWorkbookPath(String workbookPath) {
		this.workbookPath = workbookPath;
	}

	private void transferSetToSheet(XSSFSheet sheet,
			HashMap<String, LinkedHashMap<String, ArrayList<Integer>>> rangeMap, Map<String, String> valueMap,
			String[] startKey) {
		boolean sand = false;
		boolean chem = false;
		for (Map.Entry<String, LinkedHashMap<String, ArrayList<Integer>>> ent : rangeMap.entrySet()) {
			if (sand & chem) {
				return;
			}

			String key = ent.getKey();
			if (DataNames.getChemSandKey(key).equals(DataNames.SAND)) {
				sand = true;
			} else {
				chem = true;
			}
			ArrayList<Integer> rows = ent.getValue().get(ROW);
			ArrayList<Integer> columns = ent.getValue().get(COLUMN);
			HashMap<Integer, HashMap<String, Integer>> occMap = new HashMap<>();
			valueMap.entrySet().forEach((Map.Entry<String, String> entry) -> {
				int count = 0;
				Matcher matcher = Pattern.compile("[^\\d\\.\\-]").matcher(key);
				rows.forEach((Integer r) -> {
					int c = count >= columns.size() ? columns.get(columns.size() - 1) : columns.get(count);

					if (cellValueMatches(sheet, r, c, "", key)) {

					}
				});

			});
		}
	}

	public static boolean arrayContains(String[] values, String value) {
		for (String s : values) {
			if (s.equals(value)) {
				return true;
			}
		}
		return false;
	}

	public static Map<String, String> getFilteredContainingKey(HashMap<String, String> locationMap, String... value) {
		Map<String, String> filteredMap = new HashMap<>();
		locationMap.entrySet().stream().filter(entry -> arrayContains(value, entry.getKey()))
				.forEachOrdered(e -> filteredMap.put(e.getKey(), e.getValue()));
		return filteredMap;
	}

	public final static int DEFAULT_RANGE_SIZE = 10;

	public static void transferAllRangeValues(XSSFSheet sheet, HashMap<String, String> locationMap,
			Map<String, Map<String, String>> chemSandMap) {
		transferRangeValues(sheet, locationMap, chemSandMap.get(DataNames.CHEMICALS),
				new String[] { DataNames.CHEMICAL_NAME_START, DataNames.CHEMICAL_NAME_END },
				new String[] { DataNames.CHEMICAL_VOLUME_START, DataNames.CHEMICAL_VOLUME_END });
		transferRangeValues(sheet, locationMap, chemSandMap.get(DataNames.SAND),
				new String[] { DataNames.SAND_NAME_START, DataNames.SAND_NAME_END },
				new String[] { DataNames.SAND_VOLUME_START, DataNames.SAND_VOLUME_END });
	}

	private static ArrayList<Object> getSortedList(Object[] array) {
		Comparator<Object> comp = (Object s1, Object s2) -> {
			int row1 = getRowFromAddress(s1.toString());
			int col1 = getColumnFromAddress(s1.toString());
			int row2 = getRowFromAddress(s2.toString());
			int col2 = getColumnFromAddress(s2.toString());
			return col1 == col2 ? row1 - row2 : col1 - col2;
		};
		Arrays.sort(array, comp);
		return new ArrayList<>(List.of(array));
	}

	private static ArrayList<String> changeToArrayOfStrings(ArrayList<Object> array) {
		ArrayList<String> arrayString = new ArrayList<>();
		for (Object o : array) {
			arrayString.add(String.valueOf(o));
		}
		return arrayString;
	}

	private static void transferRangeValues(XSSFSheet sheet, HashMap<String, String> locationMap,
			Map<String, String> nameValueMap, String[] nameKeys, String[] volumeKeys) {
		ArrayList<String> nameRange = changeToArrayOfStrings(
				getSortedList(getFilteredContainingKey(locationMap, nameKeys).values().toArray()));
		ArrayList<String> volumeRange = changeToArrayOfStrings(
				getSortedList(getFilteredContainingKey(locationMap, volumeKeys).values().toArray()));
		ArrayList<String> nameRangeArray = getFullRangeArray(nameRange);
		ArrayList<String> volumeRangeArray = getFullRangeArray(volumeRange);
		transferNameVolumeRangePairs(sheet, nameRangeArray, volumeRangeArray, nameValueMap);
	}

	public static ArrayList<String> getFullRangeArray(ArrayList<String> startEndArray) {
		ArrayList<String> rangeArray = new ArrayList<>();
		if (startEndArray.size() == 0) {
			return null;
		}
		if (startEndArray.size() == 1) {
			int row1 = Integer.valueOf(startEndArray.get(0).split(",")[0]);
			String columnString = "," + startEndArray.get(0).split(",")[1];
			for (int i = 0; i < 10; i++) {
				rangeArray.add((row1 + i) + columnString);
			}
			return rangeArray;
		}

		int row1 = getRowFromAddress(startEndArray.get(0));
		int col1 = getColumnFromAddress(startEndArray.get(0));
		int row2 = getRowFromAddress(startEndArray.get(1));
		int col2 = getColumnFromAddress(startEndArray.get(1));
		int colDiff = Math.abs(col2 - col1);
		int rowDiff = Math.abs(row2 - row1);
		int rowAdd = row2 != row1 ? (row2 - row1) / rowDiff : 0;
		int colAdd = col2 != col1 ? (col2 - col1) / rowDiff : 0;
		int iter = Math.max(colDiff, rowDiff);

		for (int i = 0; i <= iter; i++) {
			rangeArray.add(row1 + "," + col1);
			row1 += rowAdd;
			col1 += colAdd;
		}
		return rangeArray;
	}

	public static void transferNameVolumeRangePairs(XSSFSheet sheet, ArrayList<String> nameAddress,
			ArrayList<String> volumeAddress, Map<String, String> nameVolumes) {
		Set<String> used = new HashSet<>();
		if (nameAddress == null | volumeAddress == null) {
			return;
		}
		nameAddress.forEach(address -> {
			if (used.size() == nameVolumes.size()) {
				return;
			}
			Integer nameRow = getRowFromAddress(address);
			Integer nameColumn = getColumnFromAddress(address);
			String cellValue = removeSpecialCharacters(getCellStringValue(sheet, nameRow, nameColumn));
			boolean[] found = {false};
			nameVolumes.entrySet().stream().filter(e -> !used.contains(e.getKey())).forEachOrdered(entry -> {
				if(found[0]) {
					return;
				}
				System.out.println("Key: " + entry.getKey() + " - Value: " + entry.getValue());
				String name = entry.getKey();
				String value = entry.getValue();
				if (cellValue.equals(removeSpecialCharacters(name)) | cellValue.equals("")) {
					used.add(name);
					System.out.println("Name Row: " + nameRow);
					System.out.println("Name Column: " + nameColumn);
					setCellStringValue(sheet, name, nameRow, nameColumn);
					setCellValueNumericAdd(sheet, value,
							getRowFromAddress(volumeAddress.get(nameAddress.indexOf(address))),
							getColumnFromAddress(volumeAddress.get(nameAddress.indexOf(address))));
					found[0] = true;
				}
			});
		});
	}

	public static String removeSpecialCharacters(String string) {
		Matcher matcher = Pattern.compile("[\\-\\{}\\(\\)\\!\\?\\,\\&\\%\\*\\$\\#]|(\\s\\s)").matcher(string);
		String newString = string;
		while (matcher.find()) {
			if (matcher.group().matches("\\s\\s")) {
				newString = newString.replace(matcher.group(), " ");
				matcher.reset(newString);
				continue;
			}
			newString = newString.replace(matcher.group(), "");
			matcher.reset(newString);
		}
		return newString.toLowerCase();
	}

	public static Integer getRowFromAddress(String address) {
		Integer row = Integer.valueOf(address.split(",")[0]);
		return row;
	}

	public static Integer getColumnFromAddress(String address) {
		Integer column = Integer.valueOf(address.split(",")[1]);
		return column;
	}

	public void transferValuesFromTemplate(XSSFSheet sheet, Map<String, String> sigValsMap,
			Map<String, Map<String, String>> chemSandMap) {
		transferAllRangeValues(sheet, locationMap, chemSandMap);
		if (sigValsMap == null) {
			return;
		}
		for (String s : locationMap.keySet()) {
			if ((s.contains(NAME_RANGE) | s.contains(VOLUME_RANGE)) || !sigValsMap.containsKey(s)) {
				continue;
			}
			HashMap<String, Integer> rcMap = getCellRowColumn(sigValsMap, s, sheet);
			if (rcMap == null) {
				continue;
			}
			String stageValue = sigValsMap.get(s);
			if (isNumeric(stageValue)) {
				setCellValueNumeric(sheet, sigValsMap.get(s), rcMap);
				continue;
			}
			setCellStringValue(sheet, s, rcMap);
		}
	}

	@Override
	public void close() throws Exception {
		try(ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(this.getFilePath()))){
			objectOutputStream.writeObject(this);
		}catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}
