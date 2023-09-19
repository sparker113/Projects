import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class WriteToOperator implements Runnable {
	XSSFWorkbook sigWorkbook;
	HashMap<Integer, ArrayList<String>> summaryMap;
	ExecutorService executor;
	String operator;
	Integer stageNumber;
	String wellName;
	Thread t;
	ArrayList<String> paths;
	Boolean hasSummary;
	final String fileName;

	WriteToOperator(String wellName, String operator, Integer stageNumber, String fileName) throws IOException {
		this.operator = operator;
		this.executor = Executors.newCachedThreadPool();
		this.stageNumber = stageNumber;
		this.summaryMap = new HashMap<>();
		this.wellName = wellName;
		this.fileName = fileName;
		this.paths = new ArrayList<>();
	}

	public void readWriteSigWorkbook(HashMap<String, String> template) {
		Integer lastRow = SheetData.getLastDataRow(Main.yess.getmTable());
		Integer column;
		for (column = 0; column < Main.yess.getmTable().getColumnCount(); column++) {
			SheetData tempData = new SheetData(lastRow, column, Main.yess.getmTable());
			ArrayList<String> tempArray = tempData.getDataColumn();
			summaryMap.put(column, tempArray);
		}

		this.sigWorkbook = getOperatorWorkbook(template, wellName);
		if (sigWorkbook == null) {
			return;
		}
		XSSFSheet sigSheet = sigWorkbook.getSheet(template.get("Sheet Name"));
		ArrayList<Integer> headerRows = getMergedFirstRow(sigSheet, Integer.valueOf(template.get("Stage 1 Row")),
				getFirstDataColumnIndex(template));
		writeChemicals(sigSheet, template, headerRows);
		writeSand(sigSheet, template, headerRows);
		writeSigToOperator(sigSheet, stageNumber, template, getSigValuesMap());

		try {
			String sigPath = ReadDirectory.readDirect() + "\\" + wellName + "\\" + wellName
					+ template.get("Workbook Suffix");
			sigWorkbook.write(new FileOutputStream(sigPath));
			sigWorkbook.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Unable to save Operator Workbook");
			return;
		}
	}

	public Integer getFirstDataColumnIndex(HashMap<String, String> template) {
		Integer firstColumn = 10;
		for (String s : template.values()) {
			Matcher matcher = Pattern.compile("[\\D|,]").matcher(s);
			if (matcher.find()) {
				System.out.println("Matcher found: " + matcher.group());
				continue;
			}
			if (Integer.valueOf(s) < firstColumn) {
				firstColumn = Integer.valueOf(s);
			}
		}
		return firstColumn;
	}

	public ArrayList<String> getSandNames() {
		ArrayList<String> sandNames = new ArrayList<>();
		sandNames.add("BROWN");
		sandNames.add("WHITE");
		sandNames.add("REGIONAL");
		return sandNames;
	}

	@Override
	public void run() {

		HashMap<String, String> template = null;
		try {
			template = ReadOperatorTemplate.readOperatorTemplate(operator, fileName);
		} catch (IOException e) {
			System.out.println("Exception caught reading operator template");
			return;
		}
		// GET AND WRITE TO SIG WORKBOOK
		readWriteSigWorkbook(template);
	}

	public HashMap<String, Integer> getChemColumns(XSSFSheet sheet, Integer chemRow) {
		HashMap<String, Integer> chemColumnMap = new HashMap<>();
		chemColumnMap.put("Fluid Name", findColumn(sheet, chemRow, "Fluid Name"));
		chemColumnMap.put("Additive Name", findColumn(sheet, chemRow, "Additive Name"));
		chemColumnMap.put("Max Concentration", findColumn(sheet, chemRow, "Max"));
		chemColumnMap.put("Min Concentration", findColumn(sheet, chemRow, "Min"));
		return chemColumnMap;
	}

	public HashMap<String, Integer> getSandColumns(XSSFSheet sheet, Integer sandRow) {
		HashMap<String, Integer> sandColumnMap = new HashMap<>();
		sandColumnMap.put("Name", findColumn(sheet, sandRow, "Name"));
		sandColumnMap.put("Type", findColumn(sheet, sandRow, "Type"));
		sandColumnMap.put("Design", findColumn(sheet, sandRow, "Amount", "Design"));
		sandColumnMap.put("Actual", findColumnExclude(sheet, sandRow, "Design", "Amount"));
		sandColumnMap.put("Size", findColumn(sheet, sandRow, "Size"));
		for (String s : sandColumnMap.keySet()) {
			System.out.println(s + " - " + sandColumnMap.get(s));
		}
		return sandColumnMap;
	}

	public String[] getValidationValues(XSSFSheet sheet, Integer row, Integer column) {
		String[] validationValues = null;
		found: for (XSSFDataValidation v : sheet.getDataValidations()) {
			System.out.println(v.getValidationConstraint().getExplicitListValues());
			for (CellRangeAddress cRA : v.getRegions().getCellRangeAddresses()) {
				if (cRA.containsColumn(column) && cRA.containsRow(row)) {
					validationValues = new String[cRA.getNumberOfCells()];

					int i = 0;
					for (String s : v.getValidationConstraint().getExplicitListValues()) {
						validationValues[i] = s;
						i++;
					}
					break found;
				}
			}
		}
		return validationValues;
	}

	public HashMap<String, Integer> getValueColumns(XSSFSheet sheet, Integer sigRow) {
		HashMap<String, Integer> summarySigVals = new HashMap<>();
		summarySigVals.put("Start Date", findColumn(sheet, sigRow, "Start Date"));
		summarySigVals.put("Start Time", findColumn(sheet, sigRow, "Start Time"));
		summarySigVals.put("End Date", findColumn(sheet, sigRow, "End Date"));
		summarySigVals.put("End Time", findColumn(sheet, sigRow, "End Time"));
		summarySigVals.put("Pump Time", findColumn(sheet, sigRow, "Duration"));
		summarySigVals.put("Top Perf", findColumn(sheet, sigRow, "Top Depth"));
		summarySigVals.put("Bottom Perf", findColumn(sheet, sigRow, "Bottom Depth"));
		summarySigVals.put("Open Pressure",
				findColumn(sheet, sigRow, "Open Pressure", "Pre-Treatment Shut-In Pressure"));
		summarySigVals.put("ISIP", findColumn(sheet, sigRow, "ISIP", "Instantaneous Shut-In Pressure"));
		summarySigVals.put("Close Pressure", findColumn(sheet, sigRow, "Close Pressure", "SICP"));
		summarySigVals.put("Design Total Proppant", findColumn(sheet, sigRow, "Proppant Designed"));
		summarySigVals.put("Total Proppant", findColumn(sheet, sigRow, "Proppant In Formation", "Total Proppant"));
		summarySigVals.put("Average Rate", findColumn(sheet, sigRow, "AIR", "Average Rate"));
		summarySigVals.put("Average Pressure", findColumn(sheet, sigRow, "ATP", "Average Pressure"));
		summarySigVals.put("Max Rate", findColumn(sheet, sigRow, "MIR", "Max Treating Rate"));
		summarySigVals.put("Max Pressure", findColumn(sheet, sigRow, "MTP", "Max Treating Pressure"));
		summarySigVals.put("Breakdown", findColumn(sheet, sigRow, "Breakdown"));
		summarySigVals.put("Frac Gradient", findColumn(sheet, sigRow, "Frac Gradient", "FG"));

		return summarySigVals;
	}

	public String calcFracGradient(TreeMap<String, String> sigMap) {
		double isip = Double.parseDouble(sigMap.get("ISIP"));
		double tvd = Double.parseDouble(sigMap.get("TVD"));

		Double fracGradient = ((Double.valueOf(.052) * Double.valueOf(8.33) * tvd) + isip) / tvd;
		String fGString = String.valueOf(fracGradient);
		return fGString;
	}

	public TreeMap<String, String> addTotalSandMap() {
		TreeMap<String, String> totalSandMap = new TreeMap<>();
		LinkedHashMap<String, HashMap<String, String>> sandMap = SheetData.getSigTableData(Main.yess.diagTable3, 1);
		totalSandMap.put("Design Total Proppant", sandMap.get("Total Proppant (lbm)").get("Design Volume"));
		totalSandMap.put("Total Proppant", sandMap.get("Total Proppant (lbm)").get("Volume Pumped"));
		return totalSandMap;
	}

	public Integer findColumn(XSSFSheet sheet, Integer sigRow, String... findString) {
		Integer foundColumn = -1;
		Integer i;
		int firstCell = sheet.getRow(sigRow).getFirstCellNum();
		int lastCell = sheet.getRow(sigRow).getLastCellNum();
		found: for (String s : findString) {
			for (i = firstCell; i < lastCell; i++) {
				if (sheet.getRow(sigRow).getCell(i).getStringCellValue().toUpperCase().contains(s.toUpperCase())) {
					foundColumn = i;
					break found;
				}
			}
		}
		return foundColumn;
	}

	public Integer findColumnExclude(XSSFSheet sheet, Integer sigRow, String exclude, String... findString) {
		Integer foundColumn = -1;
		Integer i;
		int firstCell = sheet.getRow(sigRow).getFirstCellNum();
		int lastCell = sheet.getRow(sigRow).getLastCellNum();
		found: for (String s : findString) {
			for (i = firstCell; i < lastCell; i++) {
				if (sheet.getRow(sigRow).getCell(i).getStringCellValue().toUpperCase().contains(s.toUpperCase())
						&& !sheet.getRow(sigRow).getCell(i).getStringCellValue().toUpperCase().contains(exclude)) {
					foundColumn = i;
					break found;
				}
			}
		}
		return foundColumn;
	}

	public void setSigWorkbook(XSSFWorkbook sigWorkbook) {
		this.sigWorkbook = sigWorkbook;
	}

	public XSSFWorkbook getSigWorkbook() {
		return this.sigWorkbook;
	}

	public void writeToSummaryWorkbook(XSSFSheet sheet, HashMap<String, String> summaryTemplate) {

	}

	public ArrayList<Double> convertToDouble(ArrayList<String> stringArray) {
		ArrayList<Double> array = new ArrayList<>();
		for (String s : stringArray) {
			if (!s.isEmpty()) {
				array.add(Double.valueOf(s));
			} else {
				array.add(0.0);
			}
		}
		return array;
	}

	public ArrayList<Integer> convertToInteger(ArrayList<String> stringArray) {
		ArrayList<Integer> array = new ArrayList<>();
		for (String s : stringArray) {
			array.add(Integer.valueOf(s));
		}
		return array;
	}

	public synchronized XSSFSheet getStageSheet(XSSFWorkbook workbook, Integer stage) {
		XSSFSheet sheet = null;
		int i;
		for (i = 0; i < workbook.getNumberOfSheets(); i++) {
			if (workbook.getSheetAt(i).getSheetName().toUpperCase().contains("STAGE " + String.valueOf(stage))) {
				sheet = workbook.getSheetAt(i);
				break;
			}
		}
		return sheet;
	}

	public void writeSand(XSSFSheet sheet, HashMap<String, String> template, ArrayList<Integer> headerRows) {
		Integer stageRow = headerRows.get(headerRows.size() - 1) + stageNumber;
		LinkedHashMap<String, String> sandMap = SheetData.getSigTableData(Main.yess.diagTable3);
		ArrayList<Integer> sandRange = getChemicalRange(template.get("Sand Volume Range"));

		for (Integer i : sandRange) {
			sandBreak: for (String s : sandMap.keySet()) {
				for (Integer row : headerRows) {

					String workbookChem = sheet.getRow(row).getCell(i).getStringCellValue();
					if (workbookChem.toUpperCase().contains(s.toUpperCase())) {

						sheet.getRow(stageRow).getCell(i).setCellValue(sandMap.get(s));
						break sandBreak;
					}
				}
			}
		}
	}

	public void writeChemicals(XSSFSheet sheet, HashMap<String, String> template, ArrayList<Integer> headerRows) {

		Integer stageRow = headerRows.get(headerRows.size() - 1) + stageNumber;
		LinkedHashMap<String, String> chemMap = SheetData.getSigTableData(Main.yess.diagTable1);
		ArrayList<Integer> chemicalRange = getChemicalRange(template.get("Chemical Range"));

		for (Integer i : chemicalRange) {
			chemBreak: for (String s : chemMap.keySet()) {
				for (Integer row : headerRows) {
					if (sheet.getRow(row).getCell(i) == null) {
						continue;
					}
					String workbookChem = sheet.getRow(row).getCell(i).getStringCellValue();
					if (workbookChem.toUpperCase().contains(s.toUpperCase())) {
						ExcelTransfer.changeTypeToDouble(sigWorkbook, sheet, sheet.getRow(stageRow).getCell(i), true);
						sheet.getRow(stageRow).getCell(i).setCellValue(chemMap.get(s));
						break chemBreak;
					}
				}
			}
		}
	}

	public static ArrayList<Integer> getMergedFirstRow(XSSFSheet sheet, int headersRow, int stageColumn) {
		ArrayList<Integer> mergedRowRange = new ArrayList<>();
		for (int i = 1; i < 4; i++) {
			if (headersRow - 1 < 0) {
				break;
			}
			mergedRowRange.add(headersRow - i);
		}

		return mergedRowRange;
	}

	public ArrayList<Integer> getChemicalRange(String chemRangeString) {
		ArrayList<Integer> chemicalRange = new ArrayList<>();
		for (String s : chemRangeString.split(",")) {
			chemicalRange.add(Integer.valueOf(s));
		}
		return chemicalRange;
	}

	public static XSSFSheet getChemSheet(XSSFWorkbook sigWorkbook, HashMap<String, HashMap<String, String>> template) {
		XSSFSheet sheet = null;
		if (template.get("SigMap").get("Chemicals Sheet Name*").equals("NO_DICE")) {
			sheet = sigWorkbook.getSheet(template.get("SigMap").get("Sheet Name"));
		} else {
			sheet = sigWorkbook.getSheet(template.get("SigMap").get("Chemicals Sheet Name*"));
		}
		return sheet;
	}

	public static XSSFSheet getSandSheet(XSSFWorkbook sigWorkbook, HashMap<String, HashMap<String, String>> template) {
		XSSFSheet sheet = null;
		if (template.get("SigMap").get("Sand Sheet Name*").equals("NO_DICE")) {
			sheet = sigWorkbook.getSheet(template.get("SigMap").get("Sheet Name"));
		} else {
			sheet = sigWorkbook.getSheet(template.get("SigMap").get("Sand Sheet Name*"));
		}
		return sheet;
	}

	public ArrayList<String> getPaths() {
		return this.paths;
	}

	public synchronized XSSFWorkbook getOperatorWorkbook(HashMap<String, String> template, String wellName) {
		XSSFWorkbook workbook = null;
		File f = null;
		// System.out.println(ReadDirectory.readDirect() + "\\" + well + "\\" + well +
		// template.get("Workbook Suffix"));

		try {
			this.paths.add(
					ReadDirectory.readDirect() + "\\" + wellName + "\\" + wellName + template.get("Workbook Suffix"));
			File file = RedTreatmentReport.findDir(new File(ReadDirectory.readDirect()),
					wellName + template.get("Workbook Suffix"));
			if (file == null) {
				return null;
			}
			FileInputStream fileInputStream = new FileInputStream(new File(
					ReadDirectory.readDirect() + "\\" + wellName + "\\" + wellName + template.get("Workbook Suffix")));
			workbook = new XSSFWorkbook(fileInputStream);
		} catch (IOException e) {

			String message = "Double check the operators workbook name; the format should be 'Well Name' \" - \""
					+ " 'Workbook Suffix'; ensure the suffix is correct in the template submitted for this operator."
					+ "The hyphen is not automatically included, nor is it required to be in the filename.";
			String formattedMessage = String.format("<html><div style=\"width:%dpx\">%s</div></html>", 400, message);
			JOptionPane.showMessageDialog(null, formattedMessage);
			return null;
		}
		return workbook;
	}

	public synchronized static void writeSigToOperator(XSSFSheet sheet, Integer stage, HashMap<String, String> template,
			Map<String, String> values) {

		HashMap<String, String> template1 = template;
		Integer row = Integer.valueOf(template1.get("Stage 1 Row")) + stage - 1;
		String dateTimeStart = "";
		String dateTimeEnd = "";
		if (template.keySet().contains("Start Time Date")) {
			dateTimeStart = values.get("Start Date") + " " + values.get("Start Time");
			dateTimeEnd = values.get("End Date") + " " + values.get("End Time");
			values.put("Start Time Date", dateTimeStart);
			values.put("End Time Date", dateTimeEnd);
		}
		for (String a : values.keySet()) {
			System.out.println(a);
			System.out.println(template1.get(a));
			if (!template1.keySet().contains(a) || template1.get(a).equals("NO_DICE")) {
				continue;
			}
			if (String.valueOf(values.get(a)) != "null" && !template1.get(a).equals("NO_DICE")) {
				if (sheet.getRow(row) == null) {
					sheet.createRow(row);
				}
				try {
					Integer.valueOf(template1.get(a));
				} catch (NumberFormatException e) {
					continue;
				}
				if (sheet.getRow(row).getCell(Integer.valueOf(template1.get(a))) == null) {
					sheet.getRow(row).createCell(Integer.valueOf(template1.get(a)));
				}
				XSSFCell cell = sheet.getRow(row).getCell(Integer.valueOf(template1.get(a)));
				Matcher matcher = Pattern.compile("\\D").matcher(values.get(a));
				if (values.get(a) == "") {
					cell.setCellValue(0);
				} else if (matcher.find()) {
					cell.setCellValue(values.get(a));
				} else {
					cell.setCellValue(Double.valueOf(values.get(a)));
				}
			}
		}
	}

	public LinkedHashMap<String, String> getSigValuesMap() {
		LinkedHashMap<String, String> sigValuesMap = SheetData.getSigTableData(Main.yess.diagTable2);
		sigValuesMap.put("Start Time", String.valueOf(Main.yess.getmTable().getValueAt(0, 0)));
		sigValuesMap.put("Start Date", String.valueOf(Main.yess.getmTable().getValueAt(0, 3)));
		sigValuesMap.put("End Time", SheetData.getEndTime(Main.yess.getmTable()));
		sigValuesMap.put("End Date", SheetData.getEndDate(Main.yess.getmTable()));
		sigValuesMap.put("Top Perf", sigValuesMap.get("Perfs").split("-")[0].trim());
		sigValuesMap.put("Bottom Perf", sigValuesMap.get("Perfs").split("-")[1].trim());
		return sigValuesMap;
	}

	public synchronized static void writeSandToOperator(XSSFSheet sheet, String stage, HashMap<String, String> template,
			Map<String, String> sandValues) {
		ArrayList<Integer> sandCells = new ArrayList<>();
		int stageInt = Integer.parseInt(stage);
		Integer row = Integer.valueOf(template.get("Stage 1 Row")) + stageInt - 1;
		Integer rowHeaders = Integer.valueOf(template.get("Stage 1 Row")) - 1;
		for (String a : template.get("Sand Volume Range").split(",")) {
			sandCells.add(Integer.valueOf(a));
		}
		int i = 0;
		if (sheet.getRow(row) == null) {
			sheet.createRow(row);
		}
		for (String a : sandValues.keySet()) {
			for (Integer b : sandCells) {

				if (sheet.getRow(row).getCell(b) == null) {
					sheet.getRow(row).createCell(b);
				}
				XSSFCell cell = sheet.getRow(rowHeaders).getCell(b);
				if (a.toUpperCase().contains(cell.getStringCellValue().toUpperCase())) {
					XSSFCell cellValue = sheet.getRow(row).getCell(b);
					cellValue.setCellValue(Double.valueOf(sandValues.get(a)));
				}
			}
		}
	}

	public synchronized static void writeChemsToOperator(XSSFSheet sheet, String stage,
			HashMap<String, String> template, Map<String, String> chemValues) {
		ArrayList<Integer> chemCells = new ArrayList<>();
		int stageInt = Integer.parseInt(stage);
		Integer row = Integer.valueOf(template.get("Stage 1 Row")) + stageInt - 1;
		Integer rowHeaders = Integer.valueOf(template.get("Stage 1 Row")) - 1;
		for (String a : template.get("Chemical Range").split(",")) {
			chemCells.add(Integer.valueOf(a));
		}
		if (sheet.getRow(row) == null) {
			sheet.createRow(row);
		}
		for (Integer c : chemCells) {
			if (sheet.getRow(row).getCell(c) == null) {
				sheet.getRow(row).createCell(c);
			}
		}
		for (String a : chemValues.keySet()) {
			System.out.println(a);
			for (Integer b : chemCells) {

				XSSFCell cell = sheet.getRow(rowHeaders).getCell(b);
				if (a.toUpperCase().contains(cell.getStringCellValue().toUpperCase())) {
					XSSFCell cellValue = sheet.getRow(row).getCell(b);
					cellValue.setCellValue(Double.valueOf(chemValues.get(a)));
				}
			}
		}
	}

	public synchronized static void saveWorkbook(XSSFWorkbook workbook, String path)
			throws FileNotFoundException, IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(new File(path));

		try {
			workbook.write(fileOutputStream);
			workbook.close();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

}

/////////////////////get xml text, write the data with xlsx and xlsm file extension
