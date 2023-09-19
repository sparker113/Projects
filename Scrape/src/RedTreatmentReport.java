
import java.awt.HeadlessException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.Channel;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFName;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class RedTreatmentReport {
	private SheetData startTime;
	private SheetData endTimes;
	private SheetData stageNumber;
	private SheetData startDate;
	private SheetData endDate;
	private SheetData sandConc;
	private SheetData cleanTotal;
	private SheetData avgPressure;
	private SheetData avgRate;
	private SheetData slurryTotal;
	private SheetData sandType;
	private SheetData sandVol;
	private SheetData subStageName;
	private String well;
	private Integer stage;
	private static XSSFWorkbook wellWorkbook;
	private XSSFSheet sheet;
	private XSSFSheet totalsSheet;
	private FormulaEvaluator evaluate;
	private Boolean retrieve = false;
	private Boolean done;
	private ExecutorService executor;
	private HashMap<String, HashMap<String, String>> userDefinedMap;

	RedTreatmentReport(String well, Integer stage, XSSFWorkbook wellWorkbook,
			HashMap<String, HashMap<String, String>> userDefinedMap) {
		this.executor = Executors.newCachedThreadPool();
		this.well = well;
		this.stage = stage;
		RedTreatmentReport.wellWorkbook = wellWorkbook;
		this.userDefinedMap = userDefinedMap;
		this.done = false;
	}

	public CellStyle getCellStyle() {
		CellStyle cellStyle = wellWorkbook.createCellStyle();
		cellStyle.setFillForegroundColor((short) 1);
		return cellStyle;
	}

	public void clearRange(XSSFSheet sheet, int row1, int column1, int row2, int column2) {
		for (int row = row1; row <= row2; row++) {
			if (sheet.getRow(row) == null) {
				continue;
			}
			for (int column = column1; column <= column2; column++) {
				if (sheet.getRow(row).getCell(column) == null) {
					continue;
				}
				sheet.getRow(row).getCell(column).setBlank();
			}
		}
	}

	public void unhideRows(XSSFSheet sheet, int row1, int row2) {
		for (int i = row1; i <= row2; i++) {
			sheet.getRow(i).setZeroHeight(false);
		}
	}

	/*
	 *
	 * TRANSFER COMMENT F96
	 *
	 *
	 */
	public final static String DEFAULT_COMMENT = "Stage was pumped to completion with no fluid or mechanical issues.";

	public XSSFWorkbook transferData() throws IOException, ClassNotFoundException {
		CountDownLatch latch1 = new CountDownLatch(2);
		executor.execute(() -> {
			try {
				System.out.println("Workbook Thread initiated");
				getSheet();
				clearRange(sheet, 212, 0, 262, Integer.valueOf(ExcelTransfer.findColumnFromAddress("J")));
				clearRange(sheet, 212, Integer.valueOf(ExcelTransfer.findColumnFromAddress("P")), 262,
						Integer.valueOf(ExcelTransfer.findColumnFromAddress("P")));
				clearRange(sheet, 212, Integer.valueOf(ExcelTransfer.findColumnFromAddress("V")), 262,
						Integer.valueOf(ExcelTransfer.findColumnFromAddress("V")));
				clearRange(sheet, 212, Integer.valueOf(ExcelTransfer.findColumnFromAddress("Y")), 262,
						Integer.valueOf(ExcelTransfer.findColumnFromAddress("Y")));
				clearRange(sheet, 212, Integer.valueOf(ExcelTransfer.findColumnFromAddress("AE")), 262,
						Integer.valueOf(ExcelTransfer.findColumnFromAddress("AE")));
				unhideRows(sheet, 212, 262);
				hideIrrelevantSheets(wellWorkbook);
				latch1.countDown();
				System.out.println("Workbook Thread Complete");
			} catch (HeadlessException e) {
				e.printStackTrace();
			}
		});
		executor.execute(() -> {
			System.out.println("Retrieve Data initiated");
			retrieveData();
			latch1.countDown();
			System.out.println("Retrieve Data Complete");
		});
		try {
			latch1.await();
		} catch (InterruptedException e) {
			System.out.println("Latch1 - RedTreatmentReport Interrupted");
		}

		LinkedHashMap<String, String> sigValsMap = SheetData.getSigTableData(Main.yess.diagTable2);
		// FileOutputStream fileOutputStream = new FileOutputStream(new
		// File("calcchain.txt"));
		writeToWorkbook(wellWorkbook, sheet, sigValsMap);
		InvoiceEvaluate.evaluateNoReturn(wellWorkbook, sheet);

		wellWorkbook.setForceFormulaRecalculation(true);
		return wellWorkbook;
	}

	public Boolean isDone() {
		return this.done;
	}

	private synchronized void retrieveData() {

		int lastRow = SheetData.getLastDataRow(Main.yess.getmTable());
		endTimes = new SheetData(lastRow, 1, Main.yess.getmTable());
		sandConc = new SheetData(lastRow, 5, Main.yess.getmTable());
		cleanTotal = new SheetData(lastRow, 6, Main.yess.getmTable());
		avgPressure = new SheetData(lastRow, 7, Main.yess.getmTable());
		avgRate = new SheetData(lastRow, 8, Main.yess.getmTable());
		slurryTotal = new SheetData(lastRow, 9, Main.yess.getmTable());
		sandType = new SheetData(lastRow, 10, Main.yess.getmTable());
		sandVol = new SheetData(lastRow, 11, Main.yess.getmTable());
		subStageName = new SheetData(lastRow, 12, Main.yess.getmTable());

	}

	public void hideRows(XSSFSheet sheet, Integer beginRow, Integer endRow) {
		Integer i;
		for (i = beginRow; i <= endRow; i++) {
			sheet.getRow(i).setZeroHeight(true);
		}
	}

	public void hideIrrelevantSheets(XSSFWorkbook workbook) {
		int i;
		for (i = 0; i < workbook.getNumberOfSheets(); i++) {
			if (workbook.getSheetAt(i).getSheetName().toUpperCase().contains("STAGE")
					&& !workbook.getSheetAt(i).getSheetName().toUpperCase().equals("STAGE " + stage)) {
				workbook.setSheetHidden(i, true);
			} else {
				workbook.setSheetHidden(i, false);
			}
		}
	}

	public static File findDir(File file, String fileName) {
		if (fileName.equals(mainFrame.removeSpecialCharacters(file.getName()))) {
			return file;
		}
		File newFile = null;
		for (File inDir : file.listFiles()) {
			newFile = findDirRec(inDir, fileName);
			if (newFile != null) {
				return newFile;
			}
		}
		return newFile;
	}

	public static File findDirRec(File file, String fileName) {
		if (!file.isDirectory() & !fileName.equals(mainFrame.removeSpecialCharacters(file.getName()))) {
			return null;
		} else if (fileName.equals(mainFrame.removeSpecialCharacters(file.getName()))) {
			return file;
		}
		if (file.isDirectory() && file.list() == null) {
			return null;
		}
		File newFile = null;
		for (File inDir : file.listFiles()) {
			newFile = findDirRec(inDir, fileName);
			if (newFile != null) {
				return newFile;
			}
		}
		return null;
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

	public static XSSFWorkbook getWorkbook(String well) throws IOException, InterruptedException {
		File f = findDir(new File(ReadDirectory.readDirect()), mainFrame.removeSpecialCharacters(well + " - TR.xlsm"));
		if (checkFileOpen(f)) {
			return null;
		}
		FileInputStream fileInputStream = new FileInputStream(f);
		ZipSecureFile.setMinInflateRatio(.0010);
		if (!f.exists()) {
			JOptionPane.showMessageDialog(null, "Double check the name of the ProPetro TR \n"
					+ ReadDirectory.readDirect() + "\\" + well + "\\" + well + " - TR.xlsm");
			return null;
		}

		XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
		// evaluate = workbook.getCreationHelper().createFormulaEvaluator();
		fileInputStream.close();
		ZipSecureFile.setMinInflateRatio(.01);
		return workbook;
	}

	public void getSheet() {
		String sheetName = "Stage " + String.valueOf(stage);
		XSSFSheet tempSheet = wellWorkbook.getSheet(sheetName);
		wellWorkbook.setSheetVisibility(wellWorkbook.getSheetIndex(tempSheet), SheetVisibility.VISIBLE);
		this.sheet = tempSheet;
	}

	public void writeToWorkbook(XSSFWorkbook wellWorkbook, XSSFSheet sheet, LinkedHashMap<String, String> sigValsMap)
			throws IOException, ClassNotFoundException {

		System.out.println("Write to Workbook Initiated");
		HashMap<String, Integer> configMap = mainFrame.RedTRConfigureFrame.readConfigMapFromFile();
		ExcelTransfer<String> endTimeTransfer = new ExcelTransfer<>(
				wellWorkbook, sheet, endTimes.getDataColumn(), configMap.get(mainFrame.RedTRConfigureFrame.SUMMARY_ROW),
				0, 2, configMap.get(mainFrame.RedTRConfigureFrame.PREFLUSH_ROW) - 1, ExcelTransfer
						.getTimeStyle(wellWorkbook, sheet, configMap.get(mainFrame.RedTRConfigureFrame.SUMMARY_ROW), 0),
				CellType.NUMERIC);
		ExcelTransfer<Double> sandConcTransfer = new ExcelTransfer<>(wellWorkbook, sheet,
				arrayStringToDouble(sandConc.getDataColumn()), configMap.get(mainFrame.RedTRConfigureFrame.SUMMARY_ROW),
				21, 2, configMap.get(mainFrame.RedTRConfigureFrame.PREFLUSH_ROW) - 1,
				ExcelTransfer.getPatternStyle(wellWorkbook, sheet,
						configMap.get(mainFrame.RedTRConfigureFrame.SUMMARY_ROW), 21, "##.##"),
				CellType.NUMERIC);
		ExcelTransfer<Double> cleanTotalTransfer = new ExcelTransfer<>(wellWorkbook, sheet,
				arrayStringToDouble(cleanTotal.getDataColumn()),
				configMap.get(mainFrame.RedTRConfigureFrame.SUMMARY_ROW), 15, 2,
				configMap.get(mainFrame.RedTRConfigureFrame.PREFLUSH_ROW) - 1,
				ExcelTransfer.getPatternStyle(wellWorkbook, sheet,
						configMap.get(mainFrame.RedTRConfigureFrame.SUMMARY_ROW), 15, "#####"),
				CellType.NUMERIC);
		ExcelTransfer<Double> avgPressureTransfer = new ExcelTransfer<>(wellWorkbook, sheet,
				arrayStringToDouble(avgPressure.getDataColumn()),
				configMap.get(mainFrame.RedTRConfigureFrame.SUMMARY_ROW), 3, 2,
				configMap.get(mainFrame.RedTRConfigureFrame.PREFLUSH_ROW) - 1,
				ExcelTransfer.getPatternStyle(wellWorkbook, sheet,
						configMap.get(mainFrame.RedTRConfigureFrame.SUMMARY_ROW), 3, "#####"),
				CellType.NUMERIC);
		ExcelTransfer<Double> avgRateTransfer = new ExcelTransfer<>(wellWorkbook, sheet,
				arrayStringToDouble(avgRate.getDataColumn()), configMap.get(mainFrame.RedTRConfigureFrame.SUMMARY_ROW),
				6, 2, configMap.get(mainFrame.RedTRConfigureFrame.PREFLUSH_ROW) - 1,
				ExcelTransfer.getPatternStyle(wellWorkbook, sheet,
						configMap.get(mainFrame.RedTRConfigureFrame.SUMMARY_ROW), 6, "#####"),
				CellType.NUMERIC);
		ExcelTransfer<Double> slurryTotalTransfer = new ExcelTransfer<>(wellWorkbook, sheet,
				arrayStringToDouble(slurryTotal.getDataColumn()),
				configMap.get(mainFrame.RedTRConfigureFrame.SUMMARY_ROW), 9, 2,
				configMap.get(mainFrame.RedTRConfigureFrame.PREFLUSH_ROW) - 1,
				ExcelTransfer.getPatternStyle(wellWorkbook, sheet,
						configMap.get(mainFrame.RedTRConfigureFrame.SUMMARY_ROW), 9, "#####"),
				CellType.NUMERIC);
		// ExcelTransfer<String> sandTypeTransfer = new
		// ExcelTransfer(wellWorkbook,sheet,sandType.getDataColumn(),213,37,2,262);
		ExcelTransfer<Double> sandVolTransfer = new ExcelTransfer<>(wellWorkbook, sheet,
				arrayStringToDouble(sandVol.getDataColumn()), configMap.get(mainFrame.RedTRConfigureFrame.SUMMARY_ROW),
				24, 2, configMap.get(mainFrame.RedTRConfigureFrame.PREFLUSH_ROW) - 1,
				ExcelTransfer.getPatternStyle(wellWorkbook, sheet,
						configMap.get(mainFrame.RedTRConfigureFrame.SUMMARY_ROW), 24, "######"),
				CellType.NUMERIC);
		ExcelTransfer<String> subStageNameTransfer = new ExcelTransfer<>(wellWorkbook, sheet,
				subStageName.getDataColumn(), sandType.getDataColumn(),
				configMap.get(mainFrame.RedTRConfigureFrame.SUMMARY_ROW), 30, 2,
				configMap.get(mainFrame.RedTRConfigureFrame.PREFLUSH_ROW) - 1);

		Integer firstHideRow = subStageName.getDataColumn().size()
				+ configMap.get(mainFrame.RedTRConfigureFrame.SUMMARY_ROW) - 2;

		writeValueToCell(sheet, 31, 0, sigValsMap.get("End Date"));
		hideRows(sheet, firstHideRow, configMap.get(mainFrame.RedTRConfigureFrame.PREFLUSH_ROW) - 2);
		writeISIP(sheet, configMap.get(mainFrame.RedTRConfigureFrame.PREFLUSH_ROW) + 1);
		writeSICP(sheet, configMap.get(mainFrame.RedTRConfigureFrame.PREFLUSH_ROW) + 2);
		writeStartJob(sheet, configMap.get(mainFrame.RedTRConfigureFrame.SUMMARY_ROW) - 1);
		writeBreakdown(sheet, configMap.get(mainFrame.RedTRConfigureFrame.BREAKDOWN_ROW) - 1,
				configMap.get(mainFrame.RedTRConfigureFrame.BREAKDOWN_COLUMN));
		writeMaxAverages(sheet, 155, 15, 22);
		writeChemicals(sheet, 185, 1, 16, ExcelTransfer.getColumnIndex("Y"));
		writeEndTimes(sheet, getFormattedTime(sigValsMap.get("ISIP Time"), DateTimeFormatter.ofPattern("HH:mm")),
				configMap.get(mainFrame.RedTRConfigureFrame.PREFLUSH_ROW) + 1, 0);
		writeEndTimes(sheet, getFormattedTime(sigValsMap.get("End Time"), DateTimeFormatter.ofPattern("HH:mm")),
				configMap.get(mainFrame.RedTRConfigureFrame.PREFLUSH_ROW) + 2, 0);

		try {
			endTimeTransfer.t.join();
			sandConcTransfer.t.join();
			cleanTotalTransfer.t.join();
			avgPressureTransfer.t.join();
			avgRateTransfer.t.join();
			slurryTotalTransfer.t.join();
			sandVolTransfer.t.join();
			subStageNameTransfer.t.join();
		} catch (InterruptedException e) {
			System.out.println("interrupted");
		}
		writeUserDefinedValues(wellWorkbook, sheet);
		InvoiceEvaluate.evaluateNoReturn(wellWorkbook, sheet);
		// evaluate.evaluateNoReturn();
		System.out.println("Write to Workbook Complete");
	}

	public static String getFormattedTime(String time, DateTimeFormatter dateTimeFormatter) {
		Matcher matcher = Pattern.compile("\\d\\d:\\d\\d((:\\d\\d)?)").matcher(time);
		if (matcher.find()) {
			return LocalTime.parse(matcher.group()).format(dateTimeFormatter);
		}
		return time;
	}

	public void writeEndTimes(XSSFSheet sheet, String time, int row, int column) {
		CellStyle cellStyle = ExcelTransfer.getTimeStyle(sheet.getWorkbook(), sheet, row, column);
		ExcelTransfer.changeTypeToDate(sheet.getWorkbook(), sheet, row, column);
		sheet.getRow(row).getCell(column).setCellType(CellType.NUMERIC);
		sheet.getRow(row).getCell(column).setCellStyle(cellStyle);
		sheet.getRow(row).getCell(column).setCellValue(DateUtil.convertTime(time.equals("0") ? "00:00" : time));
	}

	public HashMap<String, String> getSheetLocationMap(String locationString, String stageSheetName) {
		HashMap<String, String> map = new HashMap<>();
		map.put(LOCATION, locationString.contains(":") ? locationString.split(":")[1] : locationString);
		map.put(SHEET_NAME, locationString.contains(":") ? locationString.split(":")[0] : stageSheetName);
		return map;
	}

	private final static String LOCATION = "location";
	private final static String SHEET_NAME = "sheet_name";

	public void writeUserDefinedValues(XSSFWorkbook workbook, XSSFSheet sheet) {

		if (userDefinedMap == null) {
			return;
		}
		LinkedHashMap<String, String> sigVals = SheetData.getSigTableData(Main.yess.diagTable2);
		for (String s : userDefinedMap.keySet()) {
			if (!sigVals.containsKey(s)) {
				continue;
			}
			HashMap<String, String> sheetLocationMap = getSheetLocationMap(
					userDefinedMap.get(s).get(UserDefinedFrame.LOCATION), sheet.getSheetName());
			XSSFSheet sheetCopy = workbook.getSheet(sheetLocationMap.get(SHEET_NAME));
			String address = sheetLocationMap.get(LOCATION);
			if(sheetCopy==null||address.equals("0"))continue;
			if (s.contains("@") && !address.equals("0")) {
				writeUserDefinedDoubleArray(workbook, sheetCopy, getListFromString(sigVals.get(s), "DOUBLE"),
						ExcelTransfer.convertExcelAddressToRC(address, stage));
				continue;
			}

			Matcher matcher = Pattern.compile("[^\\-\\d\\.\\:]+").matcher(sigVals.get(s));
			if (!matcher.find()) {
				String rowColumn = getExcelRC(sheetCopy, address, stage);
				if (rowColumn.equals("-1")) {
					continue;
				}
				if (sigVals.get(s).contains(":")) {
					ExcelTransfer.changeTypeToTime(workbook, sheet, Integer.valueOf(rowColumn.split(",")[0]),
							Integer.valueOf(rowColumn.split(",")[1]));
					sheetCopy.getRow(Integer.valueOf(rowColumn.split(",")[0]))
							.getCell(Integer.valueOf(rowColumn.split(",")[1])).setCellValue(sigVals.get(s));
				} else {
					Double value = Double.valueOf(sigVals.get(s));
					ExcelTransfer.changeTypeToDouble(workbook, sheetCopy,
							Integer.valueOf(rowColumn.split(",")[0]).intValue(),
							Integer.valueOf(rowColumn.split(",")[1]).intValue(), true);
					sheetCopy.getRow(Integer.valueOf(rowColumn.split(",")[0]).intValue())
							.getCell(Integer.valueOf(rowColumn.split(",")[1]).intValue())
							.setCellStyle(ExcelTransfer.getPatternStyle(workbook, sheetCopy,
									Integer.valueOf(rowColumn.split(",")[0]).intValue(),
									Integer.valueOf(rowColumn.split(",")[1]).intValue(),
									value < 10 ? "#.00" : (value < 100 ? "##.0" : "######")));
					sheetCopy.getRow(Integer.valueOf(rowColumn.split(",")[0]).intValue())
							.getCell(Integer.valueOf(rowColumn.split(",")[1]).intValue())
							.setCellValue(Double.valueOf(sigVals.get(s)));
				}
			} else {
				String rowColumn = getExcelRC(sheetCopy, address, stage);
				ExcelTransfer.changeTypeToString(sheetCopy, Integer.valueOf(rowColumn.split(",")[0]).intValue(),
						Integer.valueOf(rowColumn.split(",")[1]).intValue());
				sheetCopy.getRow(Integer.valueOf(rowColumn.split(",")[0]).intValue())
						.getCell(Integer.valueOf(rowColumn.split(",")[1]).intValue()).setCellValue(sigVals.get(s));
			}
		}
	}

	public String getExcelRC(XSSFSheet sheet, String address, Integer stage) {
		String rC = "";
		if (address.contains("match")) {
			rC = ExcelTransfer.convertExcelAddressToRC(sheet, address, String.valueOf(stage));
		} else {
			rC = ExcelTransfer.convertExcelAddressToRC(address, stage);
		}
		return rC;
	}

	public void writeUserDefinedDoubleArray(XSSFWorkbook workbook, XSSFSheet sheet, ArrayList<Double> doubleArray,
			String rowColumn) {
		Integer row = Integer.valueOf(rowColumn.split(",")[0]);
		Integer column = Integer.valueOf(rowColumn.split(",")[1]);
		ExcelTransfer<Double> transfer = new ExcelTransfer<>(workbook, sheet, doubleArray, row, column);
		try {
			transfer.t.join();
		} catch (InterruptedException e) {
			return;
		}
	}

	public ArrayList<Double> getListFromString(String commaArray, String type) {
		switch (type) {
		case ("STRING"):
			ArrayList<String> stringArray = new ArrayList<>();
			for (String s : commaArray.split(",")) {
				stringArray.add(s);
			}
		case ("DOUBLE"):
			ArrayList<Double> doubleArray = new ArrayList<>();
			for (String s : commaArray.split(",")) {
				doubleArray.add(Double.valueOf(s));
			}
			return doubleArray;
		default:
			return new ArrayList<>();
		}
	}

	public void writeValueToCell(XSSFSheet sheet, int row, int column, String value) {
		ExcelTransfer.changeTypeToString(sheet, sheet.getRow(row).getCell(column));
		sheet.getRow(row).getCell(column).setCellValue(value);
	}

	public void writeStageNumber(XSSFSheet sheet, int row, int column) {
		ExcelTransfer.changeTypeToString(sheet, sheet.getRow(row).getCell(column));
		sheet.getRow(row).getCell(column).setCellValue(new String("Stage " + stage + " -"));
	}

	public void writeSigTableDatumDouble(XSSFWorkbook workbook, XSSFSheet sheet, String dataName, int row, int column) {
		LinkedHashMap<String, String> sigVals = SheetData.getSigTableData(Main.yess.diagTable2);
		ExcelTransfer.changeTypeToDouble(workbook, sheet, sheet.getRow(row).getCell(column), false);
		sheet.getRow(row).getCell(column).setCellValue(checkStringMapValue(dataName, sigVals));
	}

	public Double checkStringMapValue(String key, Map<String, String> map) {
		if (map.get(key) == null || map.get(key).equals("")) {
			return 0.0;
		} else {
			return Double.valueOf(map.get(key));
		}
	}

	public void writeSandTypes(XSSFSheet sheet, Integer startRow, Integer column, ArrayList<String> sandTypeArray) {
		int i = 0;
		for (String s : sandTypeArray) {
			if (!s.contains("Total Proppant")) {
				ExcelTransfer.changeTypeToString(sheet, sheet.getRow(startRow + i).getCell(column));
				sheet.getRow(startRow + i).getCell(column).setCellValue(s);
				i++;
			}
		}
		hideRowsFindLast(sheet, startRow + i, column, "TOP");

	}

	public void writeSandTypes(XSSFSheet sheet, Integer startRow, Integer column, Boolean hide) {
		LinkedHashMap<String, String> sandTypes = SheetData.getSigTableData(Main.yess.diagTable3);
		int i = 0;
		for (String s : sandTypes.keySet()) {
			if (!s.contains("Total Proppant")) {
				ExcelTransfer.changeTypeToString(sheet, sheet.getRow(startRow + i).getCell(column));
				sheet.getRow(startRow + i).getCell(column).setCellValue(s);
				i++;
			}
		}
		if (hide) {
			hideRowsFindLast(sheet, startRow + i, column, "TOP");
		}
	}

	private void writeSandVolumes(XSSFSheet sheet, Integer startRow, Integer designColumn, Integer actualColumn) {
		LinkedHashMap<String, HashMap<String, String>> sandMap = SheetData.getSigTableData(Main.yess.diagTable3, 1);
		int i = 0;
		Double proposedTotal = Double.valueOf(0);
		Double actualTotal = 0.0;

		for (String s : sandMap.keySet()) {
			if (s.toUpperCase().contains("TOTAL")) {
				continue;
			}
			for (String ss : sandMap.get(s).keySet()) {
				switch (ss) {
				case ("Volume Pumped"):
					ExcelTransfer.changeTypeToDouble(sheet.getWorkbook(), sheet,
							sheet.getRow(startRow + i).getCell(actualColumn), false);
					sheet.getRow(startRow + i).getCell(actualColumn).setCellValue(Long.valueOf(sandMap.get(s).get(ss)));
					actualTotal = actualTotal + Double.valueOf(sandMap.get(s).get(ss));
					break;

				case ("Design Volume"):
					ExcelTransfer.changeTypeToDouble(sheet.getWorkbook(), sheet,
							sheet.getRow(startRow + i).getCell(actualColumn), false);
					sheet.getRow(startRow + i).getCell(designColumn).setCellValue(Long.valueOf(sandMap.get(s).get(ss)));
					proposedTotal = proposedTotal + Double.valueOf(sandMap.get(s).get(ss));
					break;
				}
			}
			i++;
		}
		hideRowsFindLast(sheet, startRow + i, designColumn, "TOP");

		do {
			i++;
		} while (findEndOfTable(sheet, startRow + i, actualColumn, "TOP"));

		// System.out.println(startRow+i);
		ExcelTransfer.changeTypeToDouble(sheet.getWorkbook(), sheet, sheet.getRow(startRow + i).getCell(actualColumn),
				false);
		ExcelTransfer.changeTypeToDouble(sheet.getWorkbook(), sheet, sheet.getRow(startRow + i).getCell(designColumn),
				false);
		sheet.getRow(startRow + i).getCell(actualColumn).setCellValue(Math.rint(actualTotal));
		sheet.getRow(startRow + i).getCell(designColumn).setCellValue(Math.rint(proposedTotal));
	}

	private void writeSandVolumes(XSSFSheet sheet, Integer startRow, Integer actualColumn) {
		LinkedHashMap<String, HashMap<String, String>> sandMap = SheetData.getSigTableData(Main.yess.diagTable3, 1);
		int i = 0;
		Double actualTotal = 0.0;

		for (String s : sandMap.keySet()) {
			if (s.toUpperCase().contains("TOTAL")) {
				continue;
			}
			for (String ss : sandMap.get(s).keySet()) {
				switch (ss) {
				case ("Volume Pumped"):
					ExcelTransfer.changeTypeToDouble(sheet.getWorkbook(), sheet,
							sheet.getRow(startRow + i).getCell(actualColumn), false);
					sheet.getRow(startRow + i).getCell(actualColumn).setCellValue(Long.valueOf(sandMap.get(s).get(ss)));
					actualTotal = actualTotal + Double.valueOf(sandMap.get(s).get(ss));
					break;
				}
			}
			i++;
		}
		hideRowsFindLast(sheet, startRow + i, actualColumn, "BOTTOM");

		while (findEndOfTable(sheet, startRow + i, actualColumn, "LEFT")) {
			i++;
		}
		i--;
		sheet.getRow(startRow + i).setZeroHeight(false);
		ExcelTransfer.changeTypeToDouble(sheet.getWorkbook(), sheet, sheet.getRow(startRow + i).getCell(actualColumn),
				false);
		sheet.getRow(startRow + i).getCell(actualColumn).setCellValue(actualTotal);

	}

	private void writeSandVolumes(XSSFSheet sheet, Integer startRow, Integer actualColumn,
			ArrayList<String> sandOrder) {
		LinkedHashMap<String, HashMap<String, String>> sandMap = SheetData.getSigTableData(Main.yess.diagTable3, 1);
		int i = 0;
		Double actualTotal = 0.0;

		for (String s : sandOrder) {
			if (!sandMap.containsKey(s)) {
				i++;
				continue;
			}
			ExcelTransfer.changeTypeToDouble(sheet.getWorkbook(), sheet,
					sheet.getRow(startRow + i).getCell(actualColumn), false);
			sheet.getRow(startRow + i).getCell(actualColumn)
					.setCellValue(Long.valueOf(sandMap.get(s).get("Volume Pumped")));
			actualTotal = actualTotal + Double.valueOf(sandMap.get(s).get("Volume Pumped"));
			i++;
		}
		hideRowsFindLast(sheet, startRow + i, actualColumn, "BOTTOM");

		while (findEndOfTable(sheet, startRow + i, actualColumn, "LEFT")) {
			i++;
		}
		i--;
		sheet.getRow(startRow + i).setZeroHeight(false);
		ExcelTransfer.changeTypeToDouble(sheet.getWorkbook(), sheet, sheet.getRow(startRow + i).getCell(actualColumn),
				false);
		sheet.getRow(startRow + i).getCell(actualColumn).setCellValue(actualTotal);

	}

	public void hideRowsFindLast(XSSFSheet sheet, Integer startRow, Integer column, String border) {
		int count = 0;
		do {
			if (sheet.getRow(startRow + count) == null) {
				break;
			}
			sheet.getRow(startRow + count).setZeroHeight(true);
			count++;
		} while (findEndOfTable(sheet, startRow + count, column, border));
		System.out.println("The Last Row Found: " + (startRow + count - 1));
	}

	private static Boolean findEndOfTable(XSSFSheet sheet, Integer row, Integer column, String border) {
		Boolean noEnd = false;
		Integer comparedRow = row - 1;
		if (sheet.getRow(row).getCell(column) == null) {
			return false;
		} else if (sheet.getRow(row - 1).getCell(column) == null) {
			comparedRow = row + 1;
		}
		switch (border) {
		case ("LEFT"):
			noEnd = sheet.getRow(row).getCell(column).getCellStyle().getBorderLeft().toString()
					.equals(sheet.getRow(comparedRow).getCell(column).getCellStyle().getBorderLeft().toString());
			break;
		case ("RIGHT"):
			noEnd = sheet.getRow(row).getCell(column).getCellStyle().getBorderRight().toString()
					.equals(sheet.getRow(comparedRow).getCell(column).getCellStyle().getBorderRight().toString());
			break;
		case ("TOP"):
			noEnd = sheet.getRow(row).getCell(column).getCellStyle().getBorderTop().toString()
					.equals(sheet.getRow(comparedRow).getCell(column).getCellStyle().getBorderTop().toString());
			break;
		case ("BOTTOM"):
			noEnd = sheet.getRow(row).getCell(column).getCellStyle().getBorderBottom().toString()
					.equals(sheet.getRow(comparedRow).getCell(column).getCellStyle().getBorderBottom().toString());
			break;
		}
		return noEnd;
	}

	public final static String STRAP = "Strap";
	public final static String UNIT = "Units";

	public void writeChemicals(XSSFSheet sheet, Integer startRow, Integer nameColumn, Integer valueColumn,
			Integer unitColumn) {
		LinkedHashMap<String, LinkedHashMap<String, String>> chemMap = SheetData.getTableData(Main.yess.diagTable1);
		int count = 0;
		LinkedHashMap<String, LinkedHashMap<String, String>> acidMap = new LinkedHashMap<>();
		sheet.disableLocking();
		for (String s : chemMap.keySet()) {
			if ((s.toUpperCase().contains("ACID") | s.toUpperCase().contains("HCL"))) {
				acidMap.put(s, chemMap.get(s));
				continue;
			}
			ExcelTransfer.changeTypeToDouble(wellWorkbook, sheet, sheet.getRow(startRow + count).getCell(valueColumn),
					false);
			ExcelTransfer.changeTypeToString(sheet, sheet.getRow(startRow + count).getCell(nameColumn));
			ExcelTransfer.changeTypeToString(sheet, sheet.getRow(startRow + count).getCell(unitColumn));
			sheet.getRow(startRow + count).getCell(nameColumn).setCellValue(s);
			sheet.getRow(startRow + count).getCell(valueColumn).setCellValue(Double.valueOf(chemMap.get(s).get(STRAP)));
			sheet.getRow(startRow + count).getCell(unitColumn).setCellValue(chemMap.get(s).get(UNIT));
			count++;
		}
		if (!acidMap.isEmpty()) {
			inputAcidInSummaryTab(acidMap, startRow + count, valueColumn, nameColumn, unitColumn);
		}
		hideRowsFindLast(sheet, count + startRow, nameColumn, "LEFT");
	}

	private void inputAcidInSummaryTab(LinkedHashMap<String, LinkedHashMap<String, String>> acidMap, int row,
			int valueColumn, int nameColumn, int unitColumn) {
		int count = 0;
		for (String s : acidMap.keySet()) {
			ExcelTransfer.changeTypeToDouble(wellWorkbook, sheet, row + count, valueColumn, false);
			ExcelTransfer.changeTypeToString(sheet, row + count, nameColumn);
			ExcelTransfer.changeTypeToString(sheet, row + count, unitColumn);
			sheet.getRow(row + count).getCell(valueColumn).setCellValue(Double.valueOf(acidMap.get(s).get(STRAP)));
			sheet.getRow(row + count).getCell(nameColumn).setCellValue(s);
			sheet.getRow(row + count).getCell(unitColumn).setCellValue(acidMap.get(s).get(UNIT));
			count++;
		}
	}

	public void writeMaxAverages(XSSFSheet sheet, Integer row, Integer rateColumn, Integer pressColumn) {
		Double avgPressure = Double.valueOf(SheetData.getSigTableData(Main.yess.diagTable2).get("Average Pressure"));
		Double avgRate = Double.valueOf(SheetData.getSigTableData(Main.yess.diagTable2).get("Average Rate"));
		Double maxPressure = Double.valueOf(SheetData.getSigTableData(Main.yess.diagTable2).get("Max Pressure"));
		Double maxRate = Double.valueOf(SheetData.getSigTableData(Main.yess.diagTable2).get("Max Rate"));
		ExcelTransfer.changeTypeToDouble(sheet.getWorkbook(), sheet, sheet.getRow(row).getCell(pressColumn), false);
		ExcelTransfer.changeTypeToDouble(sheet.getWorkbook(), sheet, sheet.getRow(row).getCell(rateColumn), false);
		ExcelTransfer.changeTypeToDouble(sheet.getWorkbook(), sheet, sheet.getRow(row + 1).getCell(pressColumn), false);
		ExcelTransfer.changeTypeToDouble(sheet.getWorkbook(), sheet, sheet.getRow(row + 1).getCell(rateColumn), false);
		sheet.getRow(row).getCell(pressColumn).setCellValue(avgPressure);
		sheet.getRow(row).getCell(rateColumn).setCellValue(avgRate);
		sheet.getRow(row + 1).getCell(pressColumn).setCellValue(maxPressure);
		sheet.getRow(row + 1).getCell(rateColumn).setCellValue(maxRate);
	}

	public static CellStyle getDateStyle(XSSFWorkbook workbook, XSSFCell cell) {
		CellStyle cellStyle = workbook.createCellStyle();
		CreationHelper createHelper = workbook.getCreationHelper();
		cellStyle.cloneStyleFrom(cell.getCellStyle());
		cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("hh:mm"));
		return cellStyle;
	}

	public static void changeTypeToDate(XSSFSheet sheet, XSSFCell cell) {
		int row = cell.getRowIndex();
		int column = cell.getColumnIndex();
		CellStyle cellStyle = getDateStyle(wellWorkbook, cell);
		ExcelTransfer.checkForFormulaAndRemove(sheet, row, column);
		sheet.getRow(row).createCell(column);
		sheet.getRow(row).getCell(column).setCellStyle(cellStyle);
		sheet.getRow(row).getCell(column).setCellType(CellType.NUMERIC);
	}

	public void writePumpTimes(LinkedHashMap<String, String> sigValsMap, XSSFSheet sheet) {
		ExcelTransfer.changeTypeToTime(wellWorkbook, sheet, 271, 5);
		ExcelTransfer.changeTypeToTime(wellWorkbook, sheet, 272, 5);
		ExcelTransfer.changeTypeToTime(wellWorkbook, sheet, 273, 5);
		String pumpTimeString = sigValsMap.get("Pump Time");
		String downTimeString = sigValsMap.get("Downtime");
		sheet.getRow(271).getCell(5)
				.setCellValue(LocalDateTime.of(LocalDate.of(1900, 1, 1), LocalTime.of(
						Integer.valueOf((int) Math.floor(Double.valueOf(pumpTimeString))) / Integer.valueOf(60),
						Integer.valueOf((int) Math.floor(Double.valueOf(pumpTimeString))) % Integer.valueOf(60))));
		Integer pumpTimeMinutes = getBreakToClose(
				Integer.valueOf((int) Math.round(Double.valueOf(sigValsMap.get("Pump Time")))), getHPTime(
						sigValsMap.get("Start Time"), sigValsMap.get("Start Date"), sigValsMap.get("Breakdown Time")));
		sheet.getRow(272).getCell(5).setCellValue(
				LocalDateTime.of(LocalDate.of(1900, 1, 1), LocalTime.of(pumpTimeMinutes / 60, pumpTimeMinutes % 60)));
		sheet.getRow(273).getCell(5).setCellValue(LocalDateTime.of(LocalDate.of(1900, 1, 1),
				LocalTime.of(getDownTimeHours(downTimeString), getDownTimeMinutes(downTimeString))));
	}

	public static Integer getDownTimeHours(String downTimeString) {
		return Integer.valueOf((int) Math.floor(Double.valueOf(downTimeString))) / Integer.valueOf(60);
	}

	public static Integer getDownTimeMinutes(String downTimeString) {
		return Integer.valueOf((int) Math.floor(Double.valueOf(downTimeString))) % 60;
	}

	public static void changePumpTimeToTimeFormat(LinkedHashMap<String, String> sigValsMap) {
		Integer pumpTimeMinutes = getBreakToClose(
				Integer.valueOf((int) Math.round(Double.valueOf(sigValsMap.get("Pump Time")))), getHPTime(
						sigValsMap.get("Start Time"), sigValsMap.get("Start Date"), sigValsMap.get("Breakdown Time")));
		java.sql.Time pumpTime = java.sql.Time
				.valueOf(LocalTime.of(pumpTimeMinutes / Integer.valueOf(60), pumpTimeMinutes % Integer.valueOf(60)));
		sigValsMap.remove("Pump Time");
		sigValsMap.put("Pump Time", String.valueOf(pumpTime));
	}

	private static Integer getHPTime(String startTime, String startDate, String breakTime) {
		if (!startTime.contains(":") || !breakTime.contains(":")) {
			return 0;
		}
		LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T" + startTime);
		LocalDateTime breakDateTime = LocalDateTime.of(startDateTime.toLocalDate(),
				LocalTime.of(Integer.valueOf(breakTime.split(":")[0]), Integer.valueOf(breakTime.split(":")[1])));
		int minutesHPTime = (int) Duration.between(startDateTime, breakDateTime).toMinutes();
		System.out.println("HorsePower Time: " + minutesHPTime);
		return minutesHPTime;
	}

	private static Integer getBreakToClose(Integer pumpTime, Integer hPTime) {
		return pumpTime - hPTime;
	}

	public void writeBreakdown(XSSFSheet sheet, Integer firstRow, Integer column) {
		Double breakPressure = Double
				.valueOf(SheetData.getSigTableData(Main.yess.diagTable2).get("Breakdown Pressure"));
		String breakTime = SheetData.getSigTableData(Main.yess.diagTable2).get("Breakdown Time");
		Double breakRate = Double.valueOf(SheetData.getSigTableData(Main.yess.diagTable2).get("Breakdown Rate"));
		Double breakVol = Double.valueOf(SheetData.getSigTableData(Main.yess.diagTable2).get("Breakdown Volume"));
		if (breakPressure != null) {
			ExcelTransfer.changeTypeToTime(sheet.getWorkbook(), sheet, firstRow, column);
			ExcelTransfer.changeTypeToDouble(sheet.getWorkbook(), sheet, firstRow + 1, column, false);
			ExcelTransfer.changeTypeToDouble(sheet.getWorkbook(), sheet, firstRow + 2, column, false);
			ExcelTransfer.changeTypeToDouble(sheet.getWorkbook(), sheet, firstRow + 3, column, false);
			sheet.getRow(firstRow).getCell(column).setCellValue(breakTime);
			sheet.getRow(firstRow + 1).getCell(column).setCellValue(breakPressure);
			sheet.getRow(firstRow + 2).getCell(column).setCellValue(breakRate);
			sheet.getRow(firstRow + 3).getCell(column).setCellValue(breakVol);
		}
	}

	public void writeStartJob(XSSFSheet sheet, Integer row) {
		String startTime = GetDatesTimes.getStartTime(Main.yess.mTable);
		Double openPressure = Double.valueOf(SheetData.getSigTableData(Main.yess.diagTable2).get("Open Pressure"));
		ExcelTransfer.changeTypeToString(sheet, sheet.getRow(row).getCell(0));
		ExcelTransfer.changeTypeToDouble(sheet.getWorkbook(), sheet, sheet.getRow(row).getCell(3), false);
		ExcelTransfer.changeTypeToString(sheet, sheet.getRow(row).getCell(30));
		sheet.getRow(row).getCell(0).setCellValue(startTime);
		sheet.getRow(row).getCell(3).setCellValue(openPressure);
		sheet.getRow(row).getCell(30).setCellValue("Start Job");

	}

	public static void writeISIP(XSSFSheet sheet, Integer row) {
		Double isip = Double.valueOf(SheetData.getSigTableData(Main.yess.diagTable2).get("ISIP"));
		if (isip != null) {
			ExcelTransfer.changeTypeToDouble(wellWorkbook, sheet, sheet.getRow(row).getCell(3), false);
			sheet.getRow(row).getCell(3).setCellValue(isip);
		}
		int column = 0;
		ExcelTransfer.changeTypeToString(sheet, row, column);
		sheet.getRow(row).getCell(0).setCellValue(SheetData.getEndTime(Main.yess.mTable));
	}

	public static void writeSICP(XSSFSheet sheet, Integer row) {
		Double sicp = Double.valueOf(SheetData.getSigTableData(Main.yess.diagTable2).get("Close Pressure"));
		int column = 3;
		if (sicp != null) {
			ExcelTransfer.changeTypeToDouble(wellWorkbook, sheet, row, column, false);
			sheet.getRow(row).getCell(3).setCellValue(sicp);
		}
	}

	public static synchronized void changeDowntimeTo(LinkedHashMap<String, String> sigValsMap, String hoursDays) {
		Double divisor;
		switch (hoursDays) {
		case ("HOURS"):
			divisor = 60.0;
			break;
		case ("DAYS"):
			divisor = 1440.0;
			break;
		default:
			divisor = 60.0;
		}
		Double downtimeHours = Double.valueOf(sigValsMap.get("Downtime")) / divisor;
		sigValsMap.remove("Downtime");
		sigValsMap.put("Downtime", FracCalculations.getDoubleRoundedString(downtimeHours, 2));
	}

	public ArrayList<Double> arrayStringToDouble(ArrayList<String> arrayString) {
		ArrayList<Double> arrayDouble = new ArrayList<>();
		for (String a : arrayString) {
			if (!a.isEmpty()) {
				arrayDouble.add(Double.valueOf(a));
			} else {
				arrayDouble.add(Double.valueOf(0));
			}
		}
		return arrayDouble;
	}

	private class TotalsSheet implements Runnable {
		private XSSFSheet totalsSheet;
		private Integer totalsColumn;
		private CountDownLatch latch2;
		private Thread t;

		TotalsSheet(XSSFWorkbook wellWorkbook, Integer stage, CountDownLatch latch2) {
			this.latch2 = latch2;
			this.totalsSheet = getTotalsSheet(wellWorkbook);
			this.totalsColumn = findTotalsColumn(totalsSheet, 7, stage);
			t = new Thread(this, "Totals Sheet");
			t.start();
		}

		static synchronized XSSFSheet getTotalsSheet(XSSFWorkbook wellWorkbook) {
			String sheetName = "Totals";
			XSSFSheet totalsSheet = wellWorkbook.getSheet(sheetName);
			wellWorkbook.setSheetVisibility(wellWorkbook.getSheetIndex(sheetName), SheetVisibility.VISIBLE);
			return totalsSheet;
		}

		static synchronized void updateChemNames(XSSFWorkbook wellWorkbook, Integer beginRow, Integer endRow) {
			FormulaEvaluator evaluator = wellWorkbook.getCreationHelper().createFormulaEvaluator();
			XSSFSheet sheet = getTotalsSheet(wellWorkbook);
			Integer i;
			for (i = beginRow; i < endRow; i++) {
				evaluator.evaluateInCell(sheet.getRow(i).getCell(0));
			}
		}

		static void updateTotalStage(XSSFSheet totalsSheet, XSSFCell stageTotalCell) {
			FormulaEvaluator evaluator = wellWorkbook.getCreationHelper().createFormulaEvaluator();
			evaluator.evaluate(stageTotalCell);
		}

		static synchronized Integer findTotalsColumn(XSSFSheet totalsSheet, Integer stageRow, Integer stage) {
			int i;
			Integer totalsColumn = -1;
			for (i = 1; i < totalsSheet.getRow(stageRow).getLastCellNum(); i++) {
				if (totalsSheet.getRow(stageRow).getCell(i).getStringCellValue().contains("STG " + stage)) {
					// System.out.println("The Totals Column: " + i);
					totalsColumn = i;
					break;
				}
			}
			return totalsColumn;
		}

		static synchronized HashMap<String, String> getNameConventionMap(String[] rowLabels) {
			HashMap<String, String> nameConventionMap = new HashMap<>();
			for (String s : rowLabels) {
				switch (s) {
				case ("OPEN"):
					nameConventionMap.put(s, "Open Pressure");
					break;
				case ("BREAK"):
					nameConventionMap.put(s, "Breakdown Pressure");
					break;
				case ("ISIP"):
					nameConventionMap.put(s, s);
					break;
				case ("PSI MAX"):
					nameConventionMap.put(s, "Max Pressure");
					break;
				case ("PSI AVG"):
					nameConventionMap.put(s, "Average Pressure");
					break;
				case ("RATE MAX"):
					nameConventionMap.put(s, "Max Rate");
					break;
				case ("RATE AVG"):
					nameConventionMap.put(s, "Average Rate");
					break;
				case ("Stage Start"):
					nameConventionMap.put(s, "Start Time");
					break;
				case ("Stage End"):
					nameConventionMap.put(s, "End Time");
					break;
				case ("Stage Time"):
					nameConventionMap.put(s, "Pump Time");
					break;
				case ("TOP PERF"):
					nameConventionMap.put(s, s);
					break;
				case ("BOTTOM PERF"):
					nameConventionMap.put(s, s);
					break;
				case ("TVD"):
					nameConventionMap.put(s, s);
					break;
				case ("F.G."):
					nameConventionMap.put(s, s);
					break;
				case ("Stage NPT"):
					nameConventionMap.put(s, "Downtime");
					break;
				}
			}
			return nameConventionMap;

		}

		static synchronized void writeSigValuesTotals(XSSFSheet sheet, Integer beginRow, Integer column)
				throws IOException {
			LinkedHashMap<String, String> sigValsMap = ReadOperatorTemplate.readInSigValueMap(Main.yess.diagTable2);
			Integer breakToClose = getBreakToClose(
					Integer.valueOf((int) Math.round(Double.valueOf(sigValsMap.get("Pump Time")))),
					getHPTime(sigValsMap.get("Start Time"), sigValsMap.get("Start Date"),
							sigValsMap.get("Breakdown Time")));
			sigValsMap.put("Pump Time", String.valueOf(breakToClose));
			changeDowntimeTo(sigValsMap, "DAYS");
			sigValsMap.put("BOTTOM PERF", sigValsMap.get("Perfs").split("-")[1].replace(" ", ""));
			sigValsMap.put("TOP PERF", sigValsMap.get("Perfs").split("-")[0].replace(" ", ""));
			sigValsMap.put("F.G.", FracCalculations.getDoubleRoundedString(FracCalculations.calculateFracGradient(
					Double.valueOf(sigValsMap.get("TVD")), Double.valueOf(sigValsMap.get("ISIP"))), 2));
			Double pumpTimeDouble = changePumpTimeTo(sigValsMap.get("Pump Time"), "DAYS");
			changeSigValsMap(sigValsMap, "Pump Time", String.valueOf(pumpTimeDouble));
			System.out.println("-------------------------------------------------"
					+ FracCalculations.getDoubleRoundedString(FracCalculations.calculateFracGradient(
							Double.valueOf(sigValsMap.get("TVD")), Double.valueOf(sigValsMap.get("ISIP"))), 2));

			String[] rowLabels = { "OPEN", "BREAK", "ISIP", "PSI MAX", "PSI AVG", "RATE MAX", "RATE AVG", "Stage Start",
					"Stage End", "Stage Time", "TOP PERF", "BOTTOM PERF", "Stage NPT", "TVD", "F.G." };
			HashMap<String, String> nameConventionMap = getNameConventionMap(rowLabels);

			int i;

			writeEndDateToTotals(sheet, sigValsMap, column);
			Integer lastRow = findTotalsLastRow(sheet, beginRow);
			for (i = beginRow; i < lastRow; i++) {
				if (sheet.getRow(i) == null) {
					sheet.createRow(i);
					sheet.getRow(i).createCell(0);
				} else if (sheet.getRow(i).getCell(0) == null) {
					sheet.getRow(i).createCell(0);
				}
				for (String s : nameConventionMap.keySet()) {
					if (String.valueOf(sheet.getRow(i).getCell(0).getRichStringCellValue()).toUpperCase()
							.equals(s.toUpperCase())) {

						if (nameConventionMap.get(s) != null
								&& sigValsMap.get(nameConventionMap.get(s)).contains(":")) {
							createCell(sheet, i, column);
							ExcelTransfer.changeTypeToTime(wellWorkbook, sheet, i, column);
							String hour = sigValsMap.get(nameConventionMap.get(s)).split(":")[0];
							String minute = sigValsMap.get(nameConventionMap.get(s)).split(":")[1];
							sheet.getRow(i).getCell(column).setCellValue(java.sql.Time
									.valueOf(LocalTime.of(Integer.valueOf(hour), Integer.valueOf(minute))));
							break;
						} else {
							ExcelTransfer.changeTypeToDouble(wellWorkbook, sheet, sheet.getRow(i).getCell(column),
									true);
							sheet.getRow(i).getCell(column)
									.setCellValue(Double.valueOf(sigValsMap.get(nameConventionMap.get(s))));
							break;
						}
					}
				}
			}

		}

		private static void createCell(XSSFSheet sheet, int row, int column) {
			if (sheet.getRow(row) == null) {
				sheet.createRow(row);
			}
			if (sheet.getRow(row).getCell(column) == null) {
				sheet.getRow(row).createCell(column);
			}
		}

		private static void changeSigValsMap(LinkedHashMap<String, String> sigValsMap, String key, String newValue) {
			sigValsMap.remove(key);
			sigValsMap.put(key, newValue);
		}

		private static void writeEndDateToTotals(XSSFSheet sheet, LinkedHashMap<String, String> sigValsMap,
				Integer column) {
			ExcelTransfer.changeTypeToDate(wellWorkbook, sheet, sheet.getRow(6).getCell(column));
			DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");
			sheet.getRow(6).getCell(column).setCellValue(java.sql.Date.valueOf(sigValsMap.get("End Date")));
		}

		public static Double changePumpTimeTo(String pumpTimeMin, String hoursDays) {
			Double divisor;
			switch (hoursDays) {
			case ("HOURS"):
				divisor = 60.0;
				break;
			case ("DAYS"):
				divisor = 1440.0;
				break;
			default:
				divisor = 60.0;
			}
			double pumpTimeDouble = Double.valueOf(pumpTimeMin) / divisor;
			return pumpTimeDouble;
		}

		public static synchronized Integer findTotalsLastRow(XSSFSheet sheet, Integer beginRow) {
			Integer row = beginRow;
			checkCreateRowCell(sheet, row, 0);
			while (findEndOfTable(sheet, row, 0, "LEFT")) {
				row++;
				checkCreateRowCell(sheet, row, 0);
			}
			return row++;
		}

		public static synchronized void checkCreateRowCell(XSSFSheet sheet, Integer row, Integer column) {
			if (sheet.getRow(row) == null) {
				sheet.createRow(row);
				sheet.getRow(row).createCell(column);
			} else if (sheet.getRow(row).getCell(column) == null) {
				sheet.getRow(row).createCell(column);
			}
		}

		public static synchronized void writeCleanDirty(XSSFSheet sheet, Integer column) {
			LinkedHashMap<String, String> cleanDirty = SheetData.getSigTableData(Main.yess.diagTable4);
			sheet.getRow(27).getCell(column).setCellValue(Double.valueOf(cleanDirty.get("Clean Total")));
			sheet.getRow(28).getCell(column).setCellValue(Double.valueOf(cleanDirty.get("Slurry Total")));
		}

		static synchronized void writeTotals(XSSFSheet totalsSheet, Integer column, Integer beginChems) {
			Stack<String> stackNotUsed = new Stack<>();
			Stack<String> stackUsed = new Stack<>();
			String cellString = "";
			LinkedHashMap<String, String> chemMap = SheetData.getSigTableData(Main.yess.diagTable1);
			Integer lastRow = beginChems;
			while (findEndOfTable(totalsSheet, lastRow, column, "LEFT")) {
				lastRow++;
			}
			chemMap = getOrderedChemMap(totalsSheet, chemMap, beginChems, lastRow);
			int i;
			for (String a : chemMap.keySet()) {
				if (!a.toUpperCase().contains("ACID") && !a.toUpperCase().contains("HCL")) {
					stackNotUsed.push(a);
				}
				for (i = beginChems; i < lastRow; i++) {
					// changeFontToBlack(totalsSheet,totalsSheet.getRow(i).getCell(column));
					if ((a.toUpperCase().contains("ACID") | a.toUpperCase().contains("HCL"))
							&& !totalsSheet.getSheetName().equals("Straps")) {
						/*
						 * changeFontToBlack(totalsSheet,totalsSheet.getRow(24).getCell(column));
						 * changeFontToBlack(totalsSheet,totalsSheet.getRow(29).getCell(column));
						 */
						ExcelTransfer.changeTypeToDouble(wellWorkbook, totalsSheet,
								totalsSheet.getRow(24).getCell(column), true);
						ExcelTransfer.changeTypeToDouble(wellWorkbook, totalsSheet,
								totalsSheet.getRow(29).getCell(column), true);
						totalsSheet.getRow(24).getCell(column).setCellValue(Double.valueOf(chemMap.get(a)));
						totalsSheet.getRow(29).getCell(column).setCellValue(Double.valueOf(chemMap.get(a)));
						break;
					} else if ((a.toUpperCase().contains("ACID") | a.toUpperCase().contains("HCL"))
							&& totalsSheet.getSheetName().equals("Straps")) {
						// changeFontToBlack(totalsSheet,totalsSheet.getRow(19).getCell(column));
						ExcelTransfer.changeTypeToDouble(wellWorkbook, totalsSheet,
								totalsSheet.getRow(19).getCell(column), true);
						totalsSheet.getRow(19).getCell(column).setCellValue(Double.valueOf(chemMap.get(a)));
						break;
					}
					try {
						cellString = String.valueOf(totalsSheet.getRow(i).getCell(0).getRichStringCellValue());
					} catch (IllegalStateException e) {
						break;
					}
					if (String.valueOf(totalsSheet.getRow(i).getCell(0).getRichStringCellValue()).toUpperCase()
							.contains(a.toUpperCase())) {
						stackUsed.push(stackNotUsed.pop());
						ExcelTransfer.changeTypeToDouble(wellWorkbook, totalsSheet,
								totalsSheet.getRow(i).getCell(column), true);
						totalsSheet.getRow(i).getCell(column).setCellValue(Double.valueOf(chemMap.get(a)));
						break;
					}
				}

			}
			if (!stackNotUsed.isEmpty()) {
				Integer firstRow = beginChems + stackUsed.size();
				writeChemsUnfound(totalsSheet, column, firstRow, stackNotUsed);
			}
		}

		public static LinkedHashMap<String, String> getOrderedChemMap(XSSFSheet totalsSheet,
				LinkedHashMap<String, String> chemMap, int beginChems, int lastRow) {
			LinkedHashMap<String, String> orderedChemMap = new LinkedHashMap<>();
			for (int i = beginChems; i < lastRow; i++) {
				String chem = "";
				try {
					chem = String.valueOf(totalsSheet.getRow(i).getCell(0).getRichStringCellValue());
				} catch (IllegalStateException e) {
					writeNewChemNames(totalsSheet, orderedChemMap, chemMap, i);
					break;
				}
				if (chem == "" || chem == "0") {
					writeNewChemNames(totalsSheet, orderedChemMap, chemMap, i);
					break;
				}
				if (chemMap.containsKey(chem) || chemMap.containsKey(chem.toUpperCase())) {
					orderedChemMap.put(chem, chemMap.get(chem));
					continue;
				}
				orderedChemMap.put(chem, "0");
			}
			return orderedChemMap;
		}

		public static void writeNewChemNames(XSSFSheet totalsSheet, LinkedHashMap<String, String> orderedChemMap,
				LinkedHashMap<String, String> chemMap, int nextRow) {
			for (String s : chemMap.keySet()) {
				if (!orderedChemMap.containsKey(s) && !s.toUpperCase().contains("ACID")
						&& !s.toUpperCase().contains("HCL")) {
					orderedChemMap.put(s, chemMap.get(s));
					ExcelTransfer.changeTypeToString(totalsSheet, totalsSheet.getRow(nextRow).getCell(0));
					totalsSheet.getRow(nextRow).getCell(0).setCellValue(s);
					nextRow++;
				} else if (!orderedChemMap.containsKey(s)) {
					orderedChemMap.put(s, chemMap.get(s));
				}
			}
		}

		public static void writeChemsUnfound(XSSFSheet totalsSheet, Integer column, Integer firstRow,
				Stack stackNotUsed) {
			int i = 0;
			Stack newStack = reOrderStack(stackNotUsed);
			LinkedHashMap<String, String> chemMap = SheetData.getSigTableData(Main.yess.diagTable1);
			while (!newStack.isEmpty()) {
				String name = String.valueOf(newStack.peek());

				changeTypeToString(totalsSheet, totalsSheet.getRow(firstRow + i).getCell(0));
				totalsSheet.getRow(firstRow + i).getCell(0).setCellValue(name);
				totalsSheet.getRow(firstRow + i).getCell(column)
						.setCellValue(Double.valueOf(chemMap.get(String.valueOf(newStack.pop()))));
				i++;
			}
		}

		public static Stack<String> reOrderStack(Stack<String> stack) {
			Stack<String> newStack = new Stack<>();
			while (!stack.isEmpty()) {
				newStack.push(stack.pop());
			}
			return newStack;
		}

		public static void changeTypeToString(XSSFSheet sheet, XSSFCell cell) {
			int row = cell.getRowIndex();
			int column = cell.getColumnIndex();
			CellStyle cellStyle = cell.getCellStyle();
			ExcelTransfer.checkForFormulaAndRemove(sheet, row, column);
			sheet.getRow(row).createCell(column);
			sheet.getRow(row).getCell(column).setCellType(CellType.STRING);
			sheet.getRow(row).getCell(column).setCellStyle(cellStyle);

		}

		public static void changeTypeToDouble(XSSFSheet sheet, XSSFCell cell) {
			int row = cell.getRowIndex();
			int column = cell.getColumnIndex();
			CreationHelper createHelper = wellWorkbook.getCreationHelper();
			CellStyle cellStyle = cell.getCellStyle();
			cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("#####.##"));
			ExcelTransfer.checkForFormulaAndRemove(sheet, row, column);
			sheet.getRow(row).createCell(column);
			sheet.getRow(row).getCell(column).setCellStyle(cellStyle);
			sheet.getRow(row).getCell(column).setCellType(CellType.NUMERIC);
		}

		public static void changeFontToBlack(XSSFSheet sheet, XSSFCell cell) {
			int row = cell.getRowIndex();
			int column = cell.getColumnIndex();
			XSSFFont font = cell.getCellStyle().getFont();
			font.setColor((short) 1);
			CellStyle cellStyle = cell.getCellStyle();
			cellStyle.setFont(font);
			cell.setCellStyle(cellStyle);
		}

		public static void changeTypeToDate(XSSFSheet sheet, XSSFCell cell) {
			int row = cell.getRowIndex();
			int column = cell.getColumnIndex();
			CellStyle cellStyle = getDateStyle(wellWorkbook, cell);
			ExcelTransfer.checkForFormulaAndRemove(sheet, row, column);
			sheet.getRow(row).createCell(column);
			sheet.getRow(row).getCell(column).setCellStyle(cellStyle);
			sheet.getRow(row).getCell(column).setCellType(CellType.NUMERIC);
		}

		public static void changeTypeToTime(XSSFSheet sheet, XSSFCell cell) {
			int row = cell.getRowIndex();
			int column = cell.getColumnIndex();
			sheet.getRow(row).createCell(column);

		}

		public static CellStyle getTimeStyle(XSSFWorkbook workbook, XSSFCell cell) {
			CellStyle cellStyle = workbook.createCellStyle();
			CreationHelper creationHelper = workbook.getCreationHelper();
			cellStyle.cloneStyleFrom(cell.getCellStyle());
			cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("hh:mm"));
			return cellStyle;
		}

		@Override
		public void run() {
			LinkedHashMap<String, String> sigTableData = SheetData.getSigTableData(Main.yess.diagTable2);
			writeTotals(totalsSheet, totalsColumn, 8);
			// InvoiceEvaluate totalsEvaluate = new
			// InvoiceEvaluate(totalsSheet,wellWorkbook);
			// totalsEvaluate.evaluateNoReturn();
			try {
				writeSigValuesTotals(totalsSheet, 44, totalsColumn);
			} catch (IOException e) {
			}
			ArrayList<String> sandOrder = checkSandOrder(totalsSheet, 32, 0);
			if (stage.equals(1)) {
				writeSandTypes(totalsSheet, 32, 0, true);
			} else {
				writeSandTypes(totalsSheet, 32, 0, sandOrder);
			}
			writeSandVolumes(totalsSheet, 32, totalsColumn, sandOrder);
			writeCleanDirty(totalsSheet, totalsColumn);
			updateTotalStage(totalsSheet, totalsSheet.getRow(63).getCell(getIntFromChar("FY")));
			latch2.countDown();
		}

		public void writeSandTypes(XSSFSheet sheet, Integer startRow, Integer column, Boolean hide) {
			LinkedHashMap<String, String> sandTypes = SheetData.getSigTableData(Main.yess.diagTable3);
			int i = 0;
			for (String s : sandTypes.keySet()) {
				if (!s.contains("Total Proppant")) {
					ExcelTransfer.changeTypeToString(sheet, sheet.getRow(startRow + i).getCell(column));
					sheet.getRow(startRow + i).getCell(column).setCellValue(s);
					i++;
				}
			}
			if (hide) {
				hideRowsFindLast(sheet, startRow + i, column, "TOP");
			}
		}

		private void writeSandTypes(XSSFSheet sheet, Integer startRow, Integer column, ArrayList<String> sandTypes) {
			int i = 0;
			for (String s : sandTypes) {
				ExcelTransfer.changeTypeToString(sheet, sheet.getRow(startRow + i).getCell(column));
				sheet.getRow(startRow + i).getCell(column).setCellValue(s);
				i++;
			}
		}

		private ArrayList<String> checkSandOrder(XSSFSheet sheet, Integer startRow, Integer column) {
			int i = 0;
			ArrayList<String> sandOrder = new ArrayList<>();
			while (sheet.getRow(startRow + i).getCell(column) != null
					&& !sheet.getRow(startRow + i).getCell(column).getStringCellValue().equals("")) {
				sandOrder.add(sheet.getRow(startRow + i).getCell(column).getStringCellValue());
				i++;
			}
			addNewSandTypes(sandOrder);
			return sandOrder;
		}

		private void addNewSandTypes(ArrayList<String> sandOrder) {
			LinkedHashMap<String, String> stageSandTypes = SheetData.getSigTableData(Main.yess.diagTable3);
			for (String s : stageSandTypes.keySet()) {
				if (!sandOrder.contains(s) && !s.toUpperCase().contains("TOTAL")) {
					sandOrder.add(s);
				}
			}
		}

		public static int getIntFromChar(String col) {
			col = col.toUpperCase();
			int count = 0;
			int totalCol = 0;
			for (int i = col.length(); i > 0; i--) {
				int temp = (col.charAt(i - 1)) - 64;
				totalCol = totalCol + temp * count * 26;
				count++;
			}
			System.out.println("The numeric equivalent for the alphabetically expressed Column: " + totalCol);
			return totalCol;
		}
	}

	private class StrapsSheet implements Runnable {
		private XSSFWorkbook wellWorkbook;
		private Integer stage;
		private CountDownLatch latch2;
		private Thread t;

		StrapsSheet(XSSFWorkbook wellWorkbook, Integer stage, CountDownLatch latch2) {
			this.latch2 = latch2;
			this.wellWorkbook = wellWorkbook;
			this.stage = stage;
			t = new Thread(this, "Straps Sheet");
			t.start();
		}

		static XSSFSheet getStrapSheet(XSSFWorkbook wellWorkbook) {
			XSSFSheet strapSheet = wellWorkbook.getSheet("Straps");
			return strapSheet;
		}

		static void writeChemNames(XSSFWorkbook wellWorkbook, Integer beginRow) throws IOException {
			LinkedHashMap<String, String> chemMap = SheetData.getSigTableData(Main.yess.diagTable1);
			int i = 0;
			// chemMap.keySet().forEach(System.out::println);
			XSSFSheet sheet = getStrapSheet(wellWorkbook);
			for (String a : chemMap.keySet()) {
				if (!a.toUpperCase().contains("ACID") && !a.toUpperCase().contains("HCL")) {
					sheet.getRow(beginRow + i).getCell(0).setBlank();
					sheet.getRow(beginRow + i).getCell(0).setCellValue(a);
				}
				i++;
			}
			TotalsSheet.updateChemNames(wellWorkbook, 8, 23);
		}

		static void writeStraps(XSSFSheet strapSheet, Integer beginRow, Integer endRow, Integer strapColumn) {
			LinkedHashMap<String, String> chemMap = SheetData.getSigTableData(Main.yess.diagTable1);
			int i;
			for (String a : chemMap.keySet()) {
				for (i = beginRow; i < endRow; i++) {
					if (a.toUpperCase().contains("ACID") || a.toUpperCase().contains("HCL")) {
						strapSheet.getRow(19).getCell(strapColumn).setCellValue(Double.valueOf(chemMap.get(a)));
						break;
					}
					if (String.valueOf(strapSheet.getRow(i).getCell(0).getRichStringCellValue()).toUpperCase()
							.contains(a.toUpperCase())) {
						strapSheet.getRow(i).getCell(strapColumn).setBlank();
						strapSheet.getRow(i).getCell(strapColumn).setCellValue(Double.valueOf(chemMap.get(a)));
						break;
					}
				}
			}
		}

		@Override
		public void run() {
			System.out.println("Strap Sheet Initiated");
			XSSFSheet strapSheet = getStrapSheet(wellWorkbook);
			// writeStraps(strapSheet,3,15,4);
			TotalsSheet.writeTotals(strapSheet, 4, 3);
			strapSheet.getRow(1).getCell(0).setCellValue("Stage " + stage);
			System.out.println("Strap Sheet Completed");
			latch2.countDown();
		}
	}

	public class SaveWorkbook {
		private String path;
		private final static String TEMP_FILE_PATH = "Temp_Workbook.xlsm";

		SaveWorkbook(String path) {
			this.path = path;
		}

		private static void saveTempWorkbook(XSSFWorkbook wellWorkbook) throws IOException {
			FileOutputStream fileOutputStream = new FileOutputStream(new File(TEMP_FILE_PATH));
			wellWorkbook.write(fileOutputStream);
			wellWorkbook.close();
		}

		private static XSSFWorkbook getTempWorkbook() throws IOException {
			ZipSecureFile.setMinInflateRatio(.001);
			FileInputStream fileInputStream = new FileInputStream(new File(TEMP_FILE_PATH));
			XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
			fileInputStream.close();
			ZipSecureFile.setMinInflateRatio(.01);
			return workbook;
		}

		public static void saveWorkbook(XSSFWorkbook wellWorkbook, String path) throws IOException {
			System.out.println("Save Workbook initiated");
			FileOutputStream fileOutputStream = null;
			// removeNames(wellWorkbook);
			wellWorkbook.setMissingCellPolicy(MissingCellPolicy.CREATE_NULL_AS_BLANK);
			wellWorkbook.setCellFormulaValidation(false);
			saveTempWorkbook(wellWorkbook);
			wellWorkbook = getTempWorkbook();
			fileOutputStream = new FileOutputStream(path);
			System.out.println(path + " - The Path");
			try {
				wellWorkbook.write(fileOutputStream);
			} catch (NullPointerException e) {
				System.out.println(fileOutputStream.getChannel());
				System.out.println(fileOutputStream.getFD());
				wellWorkbook.close();
				return;
			}
			fileOutputStream.flush();
			wellWorkbook.close();
			fileOutputStream.close();

			System.out.println("Save Workbook Complete");
		}

		private static void removeNames(XSSFWorkbook workbook) {
			workbook.getAllNames();
			ArrayList<XSSFName> nameArray = new ArrayList<>();
			for (XSSFName name : workbook.getAllNames()) {
				if (name.isHidden()) {
					nameArray.add(name);
				}
			}
			if (!nameArray.isEmpty()) {
				for (XSSFName n : nameArray) {
					System.out.println(n.getNameName());
					workbook.removeName(n);
				}
			}
		}
	}

}
