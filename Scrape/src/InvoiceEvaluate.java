import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class InvoiceEvaluate {
	XSSFSheet sheet;
	XSSFWorkbook workbook;
	FormulaEvaluator evaluator;

	InvoiceEvaluate(XSSFSheet sheet, XSSFWorkbook workbook) {
		this.sheet = sheet;
		this.workbook = workbook;
		evaluator = workbook.getCreationHelper().createFormulaEvaluator();
	}

	public XSSFWorkbook evaluate() {

		Integer lRow = sheet.getLastRowNum();
		int lCol = sheet.getRow(lRow).getLastCellNum();
		int i;
		int ii;
		for (i = 0; i < lRow; i++) {
			for (ii = 0; ii < lCol; ii++) {
				if (sheet.getRow(i) != null && sheet.getRow(i).getCell(ii) != null) {
					evaluator.evaluateFormulaCell(sheet.getRow(i).getCell(ii));
				}
			}
		}
		return workbook;
	}

	public void evaluateNoReturn() {
		Integer lRow = sheet.getLastRowNum();
		int lCol = sheet.getRow(lRow).getLastCellNum();
		int i;
		int ii;
		for (i = 0; i < lRow; i++) {
			for (ii = 0; ii < lCol; ii++) {
				if (sheet.getRow(i) != null && sheet.getRow(i).getCell(ii) != null
						&& sheet.getRow(i).getCell(ii).getCellType().equals(CellType.FORMULA)) {
					evaluator.evaluateFormulaCell(sheet.getRow(i).getCell(ii));
				}
			}
		}
	}

	public static void removeFormulaInputDouble(XSSFWorkbook workbook, XSSFSheet sheet, XSSFCell cell, Double value) {
		cell.setCellFormula(null);
		ExcelTransfer.changeTypeToDouble(workbook, sheet, cell, false);
		sheet.getRow(cell.getRowIndex()).getCell(cell.getColumnIndex()).setCellValue(value);
	}

	public static void evaluateNoReturn(XSSFWorkbook workbook, XSSFSheet sheet) {
		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
		Integer lRow = sheet.getLastRowNum();
		int lCol = sheet.getRow(lRow).getLastCellNum();
		System.out.println("Last Row: " + lRow);
		System.out.println("Last Column: " + lCol);
		int i;
		int ii;
		for (i = 0; i < lRow; i++) {
			for (ii = 0; ii < lCol; ii++) {
				if (sheet.getRow(i) != null && sheet.getRow(i).getCell(ii) != null) {

					if (sheet.getRow(i).getCell(ii).getCellType() == CellType.FORMULA) {
						// !sheet.getRow(i).getCell(ii).getCTCell().getF().getT().toString().equals("shared"))
						// {
						try {
							evaluator.evaluateFormulaCell(sheet.getRow(i).getCell(ii));
						} catch (Exception e) {

						}
					}
				}
			}
		}
	}

	public void transformTypeCellBlank(XSSFSheet sheet, XSSFCell cell, CellStyle cellStyle) {
		int row = cell.getRowIndex();
		int column = cell.getColumnIndex();
		sheet.getRow(row).removeCell(cell);
		sheet.getRow(row).createCell(column);
		sheet.getRow(row).getCell(column).setCellStyle(cellStyle);
		sheet.getRow(row).getCell(column).setCellType(CellType.BLANK);
	}

	public static void evaluateInvoice() throws IOException {
		String invoicePath = getInvoicePath();
		XSSFWorkbook workbook = getInvoiceWorkbook(invoicePath);
		XSSFSheet sheet = workbook.getSheet(String.valueOf(Main.yess.textCombo1.getSelectedItem().toString()));
		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
		evaluator.clearAllCachedResultValues();
		Integer lRow = sheet.getLastRowNum();
		int lCol = sheet.getRow(lRow).getLastCellNum();
		int i;
		int ii;
		evaluateNoReturn(workbook, sheet);
		saveWorkbook(workbook, invoicePath);
		JOptionPane.showMessageDialog(null, "Invoice Updated");
	}

	public static void saveWorkbook(XSSFWorkbook workbook, String invoicePath) throws IOException {
		FileOutputStream fileOutputStream = null;
		fileOutputStream = new FileOutputStream(invoicePath);
		workbook.write(fileOutputStream);
		workbook.close();
	}

	public static String getInvoicePath() {
		String pathString = "";
		String invoicePath = "";
		try {
			pathString = ReadDirectory.readDirect();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Configure the path for this pad");
		}
		ArrayList<Path> directories = new ArrayList<>();
		File file = new File(pathString);
		int i;
		for (String sub : file.list()) {
			if (sub.toUpperCase().contains("INVOICE")) {

				invoicePath = pathString + "\\" + sub;
				break;
			}
		}
		return invoicePath;
	}

	public static XSSFWorkbook getInvoiceWorkbook(String invoicePath) {
		XSSFWorkbook workbook = null;
		try {
			workbook = new XSSFWorkbook(new FileInputStream(invoicePath));
		} catch (IOException e) {
		}

		return workbook;
	}

	public class GetSpreadSheet {
		public static XSSFWorkbook getWorkbook(String suffix) throws IOException {
			String directory = ReadDirectory.readDirect() + suffix;
			XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(directory));
			return workbook;
		}
	}

}
