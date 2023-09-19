import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.poi.xssf.usermodel.XSSFSheet;

public class ExcelSigValueTransfer implements Runnable {
	HashMap<String, String> summaryTemplate;
	XSSFSheet sheet;
	Integer offset;
	ArrayList<String> quantityString;
	ArrayList<String> designString;
	Double acidAmt;

	ExcelSigValueTransfer(HashMap<String, String> summaryTemplate, XSSFSheet sheet, Integer offset) {
		this.summaryTemplate = summaryTemplate;
		this.sheet = sheet;
		this.offset = offset;
		fillQuantityString();
		fillDesignString();
	}

	public void fillQuantityString() {
		this.quantityString.add("VOLUME");
		this.quantityString.add("AMOUNT");
		this.quantityString.add("GAL");
		this.quantityString.add("LBS");
		this.quantityString.add("WEIGHT");
	}

	public void fillDesignString() {
		this.designString.add("DESIGN");
		this.designString.add("PROPOSED");
	}

	public void setAcid(Double acidAmt) {
		this.acidAmt = acidAmt;
	}

	public Double getAcid() {
		return this.acidAmt;
	}

	public int findColumn(Integer row, String identifier) {
		int i;
		int column = -1;
		for (i = 0; i < sheet.getRow(row).getLastCellNum(); i++) {
			fixCell(row, i);
			if (String.valueOf(sheet.getRow(row).getCell(i).getRichStringCellValue()).toUpperCase()
					.contains(identifier)) {
				column = i;
				break;
			}
			i++;
		}
		return column;
	}

	public void fixCell(Integer row, Integer column) {
		if (sheet.getRow(row).getCell(column) == null) {
			sheet.getRow(row).createCell(column);
		}
	}

	public void writeProppant() {
		Integer firstRow = Integer.valueOf(summaryTemplate.get("Proppant Table Headers Row"));
		Integer nameColumn = findColumn(firstRow, "NAME");
		Integer designColumn = -1;
		Integer valueColumn = -1;
		int i = 0;
		while (valueColumn == -1 & i < quantityString.size()) {
			valueColumn = findColumn(firstRow, quantityString.get(i));
			i++;
		}
		i = 0;
		while (designColumn == -1 & i < designString.size()) {
			designColumn = findColumn(firstRow, designString.get(i));
			i++;
		}
		LinkedHashMap<String, HashMap<String, String>> sandMap = SheetData.getSigTableData(Main.yess.diagTable3, 1);
		int count = 1;
		for (String s : sandMap.keySet()) {
			sheet.getRow(firstRow + count).getCell(nameColumn).setCellValue(s);
			sheet.getRow(firstRow + count).getCell(designColumn)
					.setCellValue(Double.valueOf(sandMap.get(s).get("Design Volume")));
			sheet.getRow(firstRow + count).getCell(valueColumn)
					.setCellValue(Double.valueOf(sandMap.get(s).get("Volume Pumped")));
		}

	}

	public void writeChems() {
		final Integer firstRow = Integer.valueOf(summaryTemplate.get("Chemical Headers Row"));
		Integer nameColumn = findColumn(firstRow, "NAME");
		Integer valueColumn = -1;
		int i = 0;
		while (valueColumn == -1 & i < quantityString.size()) {
			valueColumn = findColumn(firstRow, quantityString.get(i));
			i++;
		}
		int count = 1;
		LinkedHashMap<String, String> chemMap = SheetData.getSigTableData(Main.yess.diagTable1);
		for (String s : chemMap.keySet()) {
			if (s.contains("ACID")) {
				setAcid(Double.valueOf(chemMap.get(s)));
				continue;
			}
			sheet.getRow(firstRow + count).getCell(nameColumn).setCellValue(s);
			sheet.getRow(firstRow + count).getCell(valueColumn).setCellValue(Double.valueOf(chemMap.get(s)));
		}
	}

	public void writeFluids() {
		int firstRow = Integer.parseInt(summaryTemplate.get("Fluids Table Headers Row"));

	}

	@Override
	public void run() {

	}
}
