import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookType;

public class MetricsMapWorkbook extends XSSFWorkbook implements Runnable {
	MetricsMap metricsMap;
	Double totalTransmissionTime = 0.0;
	Integer totalStages = 0;
	Integer totalDoubleEmails = 0;

	MetricsMapWorkbook(MetricsMap metricsMap) {
		super(XSSFWorkbookType.XLSM);
		this.metricsMap = metricsMap;
	}

	public void writeMapToWorkbook() {
		for (String wellName : metricsMap.keySet()) {
			this.createSheet(wellName);
			XSSFSheet sheet = this.getSheet(wellName);
			writeHeaders(sheet);
			writeWellValuesToSheet(sheet, metricsMap.get(wellName));
		}
	}

	private void writeMetrics(XSSFSheet sheet, int column, int startRow) {
		ExcelTransfer.changeTypeToString(sheet, startRow, column);
		sheet.getRow(startRow).getCell(column).setCellValue("Total Stages");
		writeGeneralNumeric(sheet, startRow, column + 1, totalStages);

		ExcelTransfer.changeTypeToString(sheet, startRow + 1, column);
		sheet.getRow(startRow + 1).getCell(column).setCellValue("Total Double Emails");
		writeGeneralNumeric(sheet, startRow + 1, column + 1, totalDoubleEmails);

		ExcelTransfer.changeTypeToString(sheet, startRow + 2, column);
		sheet.getRow(startRow + 2).getCell(column).setCellValue("Average Transmission Time");
		writeGeneralNumeric(sheet, startRow + 2, column + 1, getAverageTransmission());

	}

	private Double getAverageTransmission() {
		return FracCalculations.getDoubleRoundedDouble(totalTransmissionTime / Double.valueOf(totalStages), 1);
	}

	private void writeGeneralNumeric(XSSFSheet sheet, int row, int column, Integer value) {
		ExcelTransfer.changeTypeToDouble(this, sheet, row, column, false);
		XSSFCell cell = sheet.getRow(row).getCell(column);
		cell.setCellValue(value);
	}

	private void writeGeneralNumeric(XSSFSheet sheet, int row, int column, Double value) {
		ExcelTransfer.changeTypeToDouble(this, sheet, row, column, false);
		XSSFCell cell = sheet.getRow(row).getCell(column);
		cell.setCellValue(value);
	}

	private void writeHeaders(XSSFSheet sheet) {
		createCells(sheet, 0, 10);
		sheet.getRow(0).getCell(0).setCellValue("Stage");
		sheet.getRow(0).getCell(1).setCellValue("# of Transfers");
		sheet.getRow(0).getCell(2).setCellValue("Stage End");
		sheet.getRow(0).getCell(3).setCellValue("Time Transfered");
		sheet.getRow(0).getCell(4).setCellValue("Time to Send Email");
		sheet.getRow(0).getCell(5).setCellValue("Crew");
	}

	private void addToGrossMetrics(HashMap<String, String> stageMap) {
		addDoubleEmails(stageMap.get("count"));
		addTransmissionTime(stageMap.get("transmission"));
		totalStages++;
	}

	private void addDoubleEmails(String count) {
		if (!count.equals("1")) {
			totalDoubleEmails++;
		}
	}

	private void addTransmissionTime(String transmission) {
		double tranDouble = Double.parseDouble(transmission);
		totalTransmissionTime += tranDouble;
	}

	private void createCells(XSSFSheet sheet, int row, int lastCell) {
		sheet.createRow(row);
		for (int i = 0; i <= lastCell; i++) {
			sheet.getRow(row).createCell(i);
			if (i == 1 || i == 3) {
				ExcelTransfer.changeTypeToDouble(this, sheet, sheet.getRow(row).getCell(i), false);
				continue;
			}
			ExcelTransfer.changeTypeToString(sheet, sheet.getRow(row).getCell(i));
		}
	}

	private void writeWellValuesToSheet(XSSFSheet sheet, LinkedHashMap<String, HashMap<String, String>> wellMap) {
		int i = 1;
		int count = 0;
		while (count < wellMap.size()) {
			String stage = String.valueOf(i);
			if (!wellMap.containsKey(stage)) {
				i++;
				continue;
			}
			count++;
			createCells(sheet, count, 20);
			writeStageValuesToRow(sheet, wellMap.get(stage), stage, count);
			i++;
		}
	}

	private void writeStageValuesToRow(XSSFSheet sheet, HashMap<String, String> map, String stage, int row) {
		XSSFRow sheetRow = sheet.getRow(row);
		sheetRow.getCell(0).setCellValue(stage);
		sheetRow.getCell(1).setCellValue(Integer.valueOf(map.get("count")));
		sheetRow.getCell(2).setCellValue(map.get("endTime"));
		sheetRow.getCell(3).setCellValue(map.get("sendTime"));
		sheetRow.getCell(4).setCellValue(Double.valueOf(map.get("transmission")));
		sheetRow.getCell(5).setCellValue(map.get("crew"));
		addToGrossMetrics(map);
	}

	private void saveWorkbook() throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(new File("C:\\Scrape\\metrics.xlsm"));
		this.write(fileOutputStream);
		fileOutputStream.flush();
		fileOutputStream.close();
		this.close();
	}

	public void makeWorkbook() {
		writeMapToWorkbook();
		writeMetrics(this.getSheetAt(0), 7, 1);
		try {
			saveWorkbook();
		} catch (IOException e) {
			System.out.println("Exception caught MetricsMapWorkbook::saveWorkbook");
		}
	}

	@Override
	public void run() {
		writeMapToWorkbook();
		writeMetrics(this.getSheetAt(0), 1, 7);
		try {
			saveWorkbook();
		} catch (IOException e) {
			System.out.println("Exception caught MetricsMapWorkbook::saveWorkbook");
		}
	}
}
