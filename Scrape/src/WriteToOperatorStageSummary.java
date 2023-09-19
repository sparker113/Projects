import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class WriteToOperatorStageSummary implements Runnable {
	private String wellName;
	private String stage;
	private String fileName;
	private String operator;
	private Semaphore semaphore;
	private Boolean waiting = false;

	WriteToOperatorStageSummary(String wellName, String operator, String stage, String fileName) {
		this.wellName = wellName;
		this.stage = stage;
		this.fileName = fileName;
		this.operator = operator;
		this.semaphore = new Semaphore(0);
	}

	public static HashMap<String, String> getTemplate(String operator, String fileName) throws IOException {
		String path = "C:\\Scrape\\Operator_Templates\\" + operator + "\\" + fileName;

		if (!checkFile(path)) {
			return null;
		}
		HashMap<String, String> template = new HashMap<>();
		String temp;
		BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
		while ((temp = bufferedReader.readLine()) != null) {
			if (!temp.contains(":") || temp.split(":").length < 2) {
				continue;
			}
			template.put(temp.split(":")[0], temp.split(":")[1]);
		}
		bufferedReader.close();
		return template;
	}

	private static Boolean checkFile(String filePath) throws IOException {
		File file = new File(filePath);
		return file.exists();
	}

	public static XSSFWorkbook getWorkbook(HashMap<String, String> template, String wellName) throws IOException {
		String fileSuffix = template.get("Workbook Suffix");
		if (fileSuffix == null) {
			return null;
		}

		String filePath = getPath(wellName, fileSuffix);
		if (!checkFile(filePath)) {
			return null;
		}
		FileInputStream fileInputStream = new FileInputStream(new File(filePath));
		XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
		return workbook;
	}

	private static String getPath(String wellName, String fileSuffix) throws IOException {
		String padPath = ReadDirectory.readDirect();
		String filePath = padPath + "\\" + wellName + "\\" + wellName + fileSuffix;
		return filePath;
	}

	public static XSSFSheet getSheet(XSSFWorkbook workbook, String templateSheet, String stage) {
		if (templateSheet.contains("#")) {
			return getSheetForStage(workbook, stage, templateSheet);
		}
		return workbook.getSheet(templateSheet);
	}

	public Boolean isWaiting() {
		return this.waiting;
	}

	public void releaseLock() {
		if (waiting) {
			semaphore.release();
		}
	}

	public void checkForLock(String workbookSuffix) throws InterruptedException {
		if (workbookSuffix.equals(" - TR.xlsm")) {
			waiting = true;
			semaphore.tryAcquire(10000, TimeUnit.MILLISECONDS);
		}
	}

	private static XSSFSheet getSheetForStage(XSSFWorkbook workbook, String stage, String templateSheet) {
		String sheetWithStage = templateSheet.replace("#", stage);
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			String tempSheet = workbook.getSheetName(i);
			Matcher matcher = Pattern.compile("\\d+").matcher(tempSheet);
			String numString;
			if (matcher.find() && (numString = matcher.group()).equals(stage)
					&& tempSheet.toUpperCase().contains(sheetWithStage.toUpperCase())) {
				return workbook.getSheet(tempSheet);
			}
		}
		return null;
	}

	private static void saveWorkbook(XSSFWorkbook workbook, String filePath) {
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(new File(filePath));
			workbook.write(fileOutputStream);
			fileOutputStream.flush();
			fileOutputStream.close();
			workbook.close();
		} catch (IOException e) {
			System.out.println("WriteToOperatorStageSummary::saveWorkbook");
		}
	}

	public static void writeValuesToSheet(XSSFWorkbook workbook, XSSFSheet sheet, LinkedHashMap<String, String> sigVals,
			HashMap<String, String> template) {
		for (String s : template.keySet()) {
			if (sigVals.keySet().contains(s) && !template.get(s).contains(";")) {
				Integer row = Integer.valueOf(template.get(s).split(",")[0]);
				Integer column = Integer.valueOf(template.get(s).split(",")[1]);
				String value = sigVals.get(s);
				ExcelTransfer.checkNullCreateCell(sheet, row, column);
				Matcher matcher = Pattern.compile("[\\D^[\\.]]").matcher(value);
				if (matcher.find()) {
					ExcelTransfer.changeTypeToString(sheet, sheet.getRow(row).getCell(column));
					sheet.getRow(row).getCell(column).setCellValue(value);
				} else {
					ExcelTransfer.changeTypeToDouble(workbook, sheet, sheet.getRow(row).getCell(column), false);
					sheet.getRow(row).getCell(column).setCellValue(Double.valueOf(value));
				}
			}
		}
	}

	@Override
	public void run() {

		CompletableFuture<LinkedHashMap<String, String>> sigValsFuture = new CompletableFuture<>();
		Executors.newSingleThreadExecutor().execute(() -> {
			sigValsFuture.complete(SheetData.getSigTableData(Main.yess.diagTable2));
		});
		HashMap<String, String> template = null;
		XSSFWorkbook workbook = null;
		try {
			template = getTemplate(operator, fileName);
			if (template == null) {
				return;
			}
			checkForLock(template.get("Workbook Suffix"));
			workbook = getWorkbook(template, wellName);
		} catch (IOException | InterruptedException e) {
			System.out.println("Exception caught WriteToOperatorStageSummary::run");
		}
		if (workbook == null) {
			return;
		}
		XSSFSheet sheet = getSheet(workbook, template.get("Sheet Name"), stage);
		LinkedHashMap<String, String> sigVals = null;
		try {
			sigVals = sigValsFuture.get();
		} catch (ExecutionException | InterruptedException e) {
			System.out.println("Exception caught WriteToOperatorStageSummary::run");
		}
		String workbookSuffix = template.get("Workbook Suffix");
		String path = "";
		try {
			path = getPath(wellName, workbookSuffix);
		} catch (IOException e) {
			System.out.println("Exception WriteToOperatorStageSummary::run");
		}
		writeValuesToSheet(workbook, sheet, sigVals, template);
		saveWorkbook(workbook, path);
	}
}
