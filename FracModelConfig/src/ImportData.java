import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.Channel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import frame.DataTables;
import frame.GUIUtilities;

public class ImportData {
	ImportData(){

	}
	public final static String NONE_SELECTION = "no_file";
	public static String selectFile(String currentDir,String title) {
		JFileChooser fileChooser = new JFileChooser(currentDir);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setDialogTitle(title);
		int selected = fileChooser.showOpenDialog(null);
		if(selected!=JFileChooser.APPROVE_OPTION) {
			return NONE_SELECTION;
		}
		return fileChooser.getSelectedFile().getAbsolutePath();
	}
	public static String selectFile(String currentDir) {
		JFileChooser fileChooser = new JFileChooser(currentDir);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		int selected = fileChooser.showOpenDialog(null);
		if(selected!=JFileChooser.APPROVE_OPTION) {
			return NONE_SELECTION;
		}
		return fileChooser.getSelectedFile().getAbsolutePath();
	}
	public static String selectFile() {
		JFileChooser fileChooser = new JFileChooser("C://");
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int selected = fileChooser.showOpenDialog(null);
		if(selected!=JFileChooser.APPROVE_OPTION) {
			return NONE_SELECTION;
		}
		return fileChooser.getSelectedFile().getAbsolutePath();
	}
	public final static String WELLNAME_HEADER = "wellNumber";
	public static Map<String,Map<String,List<String>>> readFracBrainFDIs(String filePath,String...excludeHeaders) throws Exception{
		Map<String,List<String>> fdis = readFileData(filePath,String.class);
		removeKeys(fdis,excludeHeaders);
		return orgByWell(fdis,WELLNAME_HEADER);
	}
	public static Map<String,Map<String,List<String>>> orgByWell(Map<String,List<String>> fdis,String wellNameHeader){
		Map<String,Integer> wellRows = getWellRows(fdis.get(wellNameHeader));
		return splitFDIMap(fdis,wellRows);
	}
	private static Map<String,Map<String,List<String>>> splitFDIMap(Map<String,List<String>> fdis,Map<String,Integer> wellRows){
		int start = 0;
		Map<String,Map<String,List<String>>> orgMap = getFDIMapShell(wellRows);
		for(String s:wellRows.keySet()) {
			orgMap.get(s).putAll(getTrimmedArrays(fdis,start,wellRows.get(s)));
			start = wellRows.get(s);
		}
		return orgMap;
	}
	private static Map<String,ArrayList<String>> getTrimmedArrays(Map<String,List<String>> fdis,int start,int end){
		Map<String,ArrayList<String>> trimmed = new LinkedHashMap<>();
		for(Map.Entry<String, List<String>> entry:fdis.entrySet()) {
			trimmed.put(entry.getKey(),trimArray(entry.getValue(),start,end));
		}
		return trimmed;
	}
	private static <T> ArrayList<T> trimArray(List<T> array,int start,int end){
		ArrayList<T> newArray = new ArrayList<>();
		for(int i = start;i<end;i++) {
			newArray.add(array.get(i));
		}
		return newArray;
	}
	private static Map<String,Map<String,List<String>>> getFDIMapShell(Map<String,Integer> wellRows){
		Map<String,Map<String,List<String>>> mapShell = new LinkedHashMap<>();
		for(String s:wellRows.keySet()) {
			mapShell.put(s, new LinkedHashMap<>());
		}
		return mapShell;
	}
	private static Map<String,Integer> getWellRows(List<String> wellNameCol){
		Map<String,Integer> wellRows = new LinkedHashMap<>();
		int i = 0;
		String well = "";
		for(String s:wellNameCol) {
			if(!wellRows.containsKey(s)&!well.equals("")) {
				wellRows.put(well, i);
			}
			well = s;
			i++;
		}
		wellRows.put(well, i);
		return wellRows;
	}
	private static void removeKeys(Map<String,?> map,String...excludeKeys) {
		for(String s:excludeKeys) {
			if(!map.containsKey(s)) {
				continue;
			}
			map.remove(s);
		}
	}
	public static <T> Map<String, List<T>> readFileData(String filePath,Class<T> t) throws Exception {

		final String fileExt = getFileExt(filePath);
		if (fileExt.equals(NULL_EXT)) {
			throw new Exception("Incorrect File Type (.xlsm,.xlsx,.csv)");
		}
		switch (fileExt) {
		case XLSM:
		case XLSX:
			return readWellSurveyXLSM(filePath,t);
		case CSV:
			return readWellSurveyCSV(filePath,t);
		default:
			return readWellSurveyCSV(filePath,t);
		}


	}
	public static <T> Map<String, List<T>> readFileData(String filePath,Class<T> t,String...constHeaders) throws Exception {

		final String fileExt = getFileExt(filePath);
		if (fileExt.equals(NULL_EXT)) {
			throw new Exception("Incorrect File Type (.xlsm,.xlsx,.csv)");
		}
		switch (fileExt) {
		case XLSM:
		case XLSX:
			return readWellSurveyXLSM(filePath,t);
		case CSV:
			return readWellSurveyCSV(filePath,t,constHeaders);
		default:
			return readWellSurveyCSV(filePath,t,constHeaders);
		}


	}
	public static <T> Map<String, List<T>> readFileData(String filePath,String frameTitle,Class<T> t,String...constHeaders) throws Exception {

		final String fileExt = getFileExt(filePath);
		if (fileExt.equals(NULL_EXT)) {
			throw new Exception("Incorrect File Type (.xlsm,.xlsx,.csv)");
		}
		switch (fileExt) {
		case XLSM:
		case XLSX:
			return readWellSurveyXLSM(filePath,t);
		case CSV:
			return readWellSurveyCSV(filePath,frameTitle,t,constHeaders);
		default:
			return readWellSurveyCSV(filePath,frameTitle,t,constHeaders);
		}


	}
	private static <T> Map<String,List<T>> readWellSurveyCSV(String filePath,Class<T> t) throws Exception {
			Map<String,List<T>> csvData = readWellSurveyCSV(new File(filePath),t);
			String tablesName = "select_data";
			Rectangle rectangle = GUIUtilities.getCenterRectangle(.125f);
			DataTables<T,String> dataTables = getDataTablesForCSV(t,tablesName,new Rectangle(0,0,rectangle.width,rectangle.height),csvData);
			
			DataTables.SelectDataFrame<T,String> selectDataFrame = new DataTables.SelectDataFrame<T,String>(dataTables,rectangle,t);
			Map<String, List<T>> selectedData = selectDataFrame.getSelectedData();
			return selectedData;
	}
	private static <T> Map<String,List<T>> readWellSurveyCSV(String filePath,String frameTitle,Class<T> t,String...constHeaders) throws Exception {
			Map<String,List<T>> csvData = readWellSurveyCSV(new File(filePath),t);
			String tablesName = "select_data";
			Rectangle rectangle = GUIUtilities.getCenterRectangle(.125f);
			DataTables<T,String> dataTables = getDataTablesForCSV(t,tablesName,new Rectangle(0,0,rectangle.width,rectangle.height),csvData,constHeaders);
			
			DataTables.SelectDataFrame<T,String> selectDataFrame = new DataTables.SelectDataFrame<T,String>(dataTables,rectangle,t);
			selectDataFrame.setTitle(frameTitle);
			Map<String, List<T>> selectedData = selectDataFrame.getTableData();
			return selectedData;
	}
	private static <T> Map<String,List<T>> readWellSurveyCSV(String filePath,Class<T> t,String...constHeaders) throws Exception {
			Map<String,List<T>> csvData = readWellSurveyCSV(new File(filePath),t);
			String tablesName = "select_data";
			Rectangle rectangle = GUIUtilities.getCenterRectangle(.125f);
			DataTables<T,String> dataTables = getDataTablesForCSV(t,tablesName,new Rectangle(0,0,rectangle.width,rectangle.height),csvData,constHeaders);
			
			DataTables.SelectDataFrame<T,String> selectDataFrame = new DataTables.SelectDataFrame<T,String>(dataTables,rectangle,t);
			Map<String, List<T>> selectedData = selectDataFrame.getTableData();
			int x = 0;
			if(selectedData==null){
				System.out.println("Selected Data is = null");
				String newFilePath = selectFile(new File(filePath).getParent());
				return readWellSurveyCSV(newFilePath,t,constHeaders);
			}
			return selectedData;
	}
	private static <T> Map<String,List<T>> readWellSurveyXLSM(String filePath,Class<T> t) throws IOException, InvalidFormatException, InterruptedException {
			XSSFWorkbook workbook = getWorkbook(filePath);
			String tablesName = "select_data";
			Rectangle rectangle = GUIUtilities.getCenterRectangle(.125f);
			DataTables<T,Integer> dataTables = getDataTables(t,tablesName,new Rectangle(0,0,rectangle.width,rectangle.height),workbook);
			workbook.close();
			DataTables.SelectDataFrame<T,Integer> selectDataFrame = new DataTables.SelectDataFrame<T,Integer>(dataTables,rectangle,t);
			Map<String, List<T>> selectedData = selectDataFrame.getSelectedData();
			return selectedData;
	}
	private static XSSFWorkbook getWorkbook(String workbookPath) throws IOException, InvalidFormatException {
		File file = new File(workbookPath);
		if (checkFileOpen(file)) {
			return null;
		}
		XSSFWorkbook workbook = new XSSFWorkbook(new File(workbookPath));
		return workbook;
	}
	private static <T> Map<Integer,List<T>> getSheetData(XSSFSheet sheet,Class<T> t){
		int lastRow = sheet.getLastRowNum();
		Map<Integer,List<T>> map = new LinkedHashMap<>();
		for(int i = 0;i<=lastRow;i++) {
			map.put(i,getRowData(t,sheet,i));
		}
		return map;
	}
	@SuppressWarnings("unchecked")
	private static <T> DataTables<T,String> getDataTablesForCSV(Class<T> t,String name,Rectangle rectangle,Map<String,List<T>> csvData) {
		DataTables<T,String> dataTables = new DataTables<>(name,rectangle);
		dataTables.addTabs(new String[]{"survey"},csvData);
		return dataTables;
	}
	@SuppressWarnings("unchecked")
	private static <T> DataTables<T,String> getDataTablesForCSV(Class<T> t,String name,Rectangle rectangle,Map<String,List<T>> csvData,String...constHeaders) {
		DataTables<T,String> dataTables = new DataTables<>(name,rectangle);
		dataTables.addTab("Survey",csvData,constHeaders);
		return dataTables;
	}
	private static <T> DataTables<T,Integer> getDataTables(Class<T> t,String name,Rectangle rectangle,XSSFWorkbook workbook) {
		Map<String,Map<Integer,List<T>>> workbookData = getWorkbookData(workbook,t);
		DataTables<T,Integer> dataTables = new DataTables<>(name,rectangle);
		dataTables.addTabs(workbookData);
		return dataTables;
	}
	private static <T> Map<String,Map<Integer,List<T>>> getWorkbookData(XSSFWorkbook workbook,Class<T> t){
		Map<String,Map<Integer,List<T>>> workbookData = new LinkedHashMap<>();
		for(int i = 0;i<workbook.getNumberOfSheets();i++) {
			XSSFSheet sheet = workbook.getSheetAt(i);
			workbookData.put(sheet.getSheetName(), getSheetData(sheet,t));
		}
		return workbookData;
	}
	private static <T> List<T> getRowData(Class<T> t,XSSFSheet sheet,int row){
		List<T> array = new ArrayList<>();
		int lastCol = sheet.getRow(row)==null?0:sheet.getRow(row).getLastCellNum();
		for(int i = 0;i<=lastCol;i++) {
			array.add(getCellStringValue(t,sheet,row,i));
		}
		return array;
	}
	public static <T> T getCellStringValue(Class<T> t,XSSFSheet sheet, int row, int col) {
		if (sheet.getRow(row) == null || sheet.getRow(row).getCell(col) == null) {
			return t.cast(" ");
		}
		if (sheet.getRow(row).getCell(col).getCellType() == CellType.NUMERIC) {
			double value = sheet.getRow(row).getCell(col).getNumericCellValue();
			return t.cast(value);
		} else if (sheet.getRow(row).getCell(col).getCellType() == CellType.FORMULA) {
			return getCellValue(t,sheet.getRow(row).getCell(col),
					sheet.getRow(row).getCell(col).getCachedFormulaResultType());
		}
		return t.cast(sheet.getRow(row).getCell(col).getStringCellValue());
	}


	public static <T> T getCellValue(Class<T> t,XSSFCell cell, CellType type) {

		if (type == CellType.NUMERIC) {
			double value;
			return (value = cell.getNumericCellValue()) > 0.0 ? t.cast(value) : t.cast("");
		}
		return t.cast(cell.getStringCellValue());
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
	private static String getFileExt(String filePath) {
		Matcher matcher = Pattern.compile("\\.[A-Za-z0-9]+$").matcher(filePath);
		if (matcher.find()) {
			String fileExt = matcher.group().toLowerCase();
			System.out.println(fileExt);
			return fileExt;
		}
		System.out.println("Didn't find file exxtension");
		return NULL_EXT;
	}

	private final static String XLSM = ".xlsm";
	private final static String XLSX = ".xlsx";
	private final static String CSV = ".csv";

	private final static String NULL_EXT = "null_ext";

	@SuppressWarnings("resource")
	static <T> Map<String, List<T>> readWellSurveyCSV(File file,Class<T> t) throws Exception {
		// LinkedHashMap<String,ArrayList<Float>> surveyData = new LinkedHashMap<>();
		FileInputStream fileInputStream = new FileInputStream(file);
		Scanner scanner = new Scanner(fileInputStream);
		scanner.useDelimiter("\n");
		if (!scanner.hasNext()) {
			throw new Exception("No Data in File");
		}
		String headers = scanner.next();
		String[] headersArr = headers.split(",");
		Map<String, List<T>> surveyData = getShellSurveyMap(headers,t);
		while (scanner.hasNext()) {
			String[] data = correctNull(scanner.next()).split(",");
			addToMap(surveyData,t,data,headersArr);
		}
		scanner.close();
		return surveyData;
	}
	private static String WELL_FDI_STATE_PATTERN = "(.+?)\\sFDI State";
	public static List<String> getWellsFromFDIData(Map<String,List<String>> fdiData){
		List<String> array = new ArrayList<>();
		for(String s:fdiData.keySet()) {
			Matcher matcher = Pattern.compile(WELL_FDI_STATE_PATTERN).matcher(s);
			if(matcher.find()) {
				array.add(matcher.group(1));
			}
		}
		return array;
	}
	@SuppressWarnings("unchecked")
	private static <T> void addToMap(Map<String,List<T>> map,Class<T> t,String[] dataRow,String[] headers) {
		for(int i = 0;i<headers.length;i++) {
			map.get(headers[i]).add((t.getSimpleName().equals("String")?(T)dataRow[i]:(T) Float.valueOf(dataRow[i])));
		}
	}
	private static String correctNull(String dataRow) {
		Matcher matcher = Pattern.compile("(,,)|(,$)").matcher(dataRow);
		while (matcher.find()) {
			dataRow = matcher.group(1)!=null?dataRow.substring(0, matcher.start()) + ",0"
					+ dataRow.substring(matcher.end() - 1, dataRow.length()):dataRow+"0";
			matcher.reset(dataRow);

		}
		return dataRow;
	}

	private static <T> Map<String, List<T>> getShellSurveyMap(String headers,Class<T> t) {
		Map<String, List<T>> surveyData = new LinkedHashMap<>();
		for (String s : headers.split(",")) {
			surveyData.put(s, new ArrayList<>());
		}
		return surveyData;
	}

}
