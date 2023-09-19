import java.awt.HeadlessException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.xssf.model.ExternalLinksTable;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class PioneerWorkbook {
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
	private ArrayList<String> slick;
	private Integer lastRow;
	private Integer stage;
	private String well;
	private XSSFWorkbook wellWorkbook;
	private XSSFSheet sheet;
	private LinkedHashMap<String, String> sandMap;
	private LinkedHashMap<String, String> prcMap;
	private LinkedHashMap<String, String> chemMap;
	private final String thisSheet;
	private Integer startRowOffset;
	private Boolean transferred = true;
	private FormulaEvaluator evaluate;

	public PioneerWorkbook(Integer stage, String well) throws IOException {
		this.stage = stage;
		this.well = well;
		this.startRowOffset = getStartRowOffset(stage);
		this.thisSheet = "Stage " + stage;
	}

	public void transferData() throws InterruptedException {

		CountDownLatch latch1 = new CountDownLatch(3);
		Thread readThread = new Thread(new Runnable() {
			@Override
			public void run() {
				setLastRow();
				System.out.println("readThread Started");

				dataTable(getLastRow());
				System.out.println("readThread Finished");
				latch1.countDown();
			}
		});

		Thread workbookThread = new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("writeData Started");
				XSSFWorkbook pioneerWorkbook = null;
				XSSFSheet pioneerSheet = null;

				try {
					pioneerWorkbook = getWorkbook();
					if (pioneerWorkbook != null) {
						pioneerSheet = getSheet();
					}
				} catch (HeadlessException | IOException e) {

				}
				System.out.println("writeData Finished");
				latch1.countDown();
			}

		});

		Thread sigValsThread = new Thread(new Runnable() {
			@Override
			public void run() {

				setSandMap();
				setPRCMap();
				setChemMap();
				latch1.countDown();
			}
		});

		readThread.start();
		workbookThread.start();
		sigValsThread.start();
		latch1.await();

		if (wellWorkbook == null) {
			JOptionPane.showMessageDialog(null,
					"Go to File->Configure Path; if the path is correct," + " double check your file names.");
			Thread.currentThread().interrupt();
		}

		CountDownLatch latch2 = new CountDownLatch(4);

		Thread writePumpSum = new Thread(new Runnable() {
			@Override
			public void run() {
				mainSummary();
				latch2.countDown();
			}
		});
		Thread writeSigVals = new Thread(new Runnable() {
			@Override
			public void run() {
				prcWriteToWorkbook();
				latch2.countDown();
			}
		});
		Thread writeChems = new Thread(new Runnable() {
			@Override
			public void run() {
				chemWriteToWorkbook();
				latch2.countDown();
			}
		});
		Thread writeSand = new Thread(new Runnable() {
			@Override
			public void run() {
				sandWriteToWorkbook();
				latch2.countDown();
			}
		});
		writePumpSum.start();
		writeSigVals.start();
		writeChems.start();
		writeSand.start();
		latch2.await();

		CountDownLatch latch3 = new CountDownLatch(4);

		Thread timesThread = new Thread(new Runnable() {
			@Override
			public void run() {
				updateTimes();
				latch3.countDown();
			}
		});
		Thread sandThread = new Thread(new Runnable() {
			@Override
			public void run() {
				updateSand();
				latch3.countDown();
			}
		});
		Thread fluidThread = new Thread(new Runnable() {
			@Override
			public void run() {
				updateFluids();
				latch3.countDown();
			}
		});
		Thread chemsThread = new Thread(new Runnable() {
			@Override
			public void run() {
				updateChems();
				latch3.countDown();
			}
		});
		timesThread.start();
		sandThread.start();
		fluidThread.start();
		chemsThread.start();
		latch3.await();
		writeUserDefinedData(wellWorkbook, sheet, prcMap, stage);
		testUpdateLinks(wellWorkbook);
		InvoiceEvaluate invoiceEvaluate = new InvoiceEvaluate(wellWorkbook.getSheet("Treatment Summary"), wellWorkbook);
		InvoiceEvaluate.removeFormulaInputDouble(wellWorkbook, wellWorkbook.getSheet("Treatment Summary"),
				wellWorkbook.getSheet("Treatment Summary").getRow(stage + 6).getCell(23),
				Double.valueOf(prcMap.get("Acid Volume")));
		invoiceEvaluate.evaluateNoReturn();
		System.out.println("Transfer Finished!");

		try {
			// printWorkbook();
			saveWorkbook(wellWorkbook);
		} catch (IOException e) {
			try {
				transferred = false;
				JOptionPane.showMessageDialog(null,
						"Close the treatment report workbook " + "and export the data again.");
				wellWorkbook.close();
				return;
			} catch (IOException e1) {
				System.out.println("Could not close workbook");
				return;
			}
		}
	}

	public static void writeUserDefinedData(XSSFWorkbook workbook, XSSFSheet sheet,
			LinkedHashMap<String, String> sigValsMap, Integer stage) {
		Executors.newSingleThreadExecutor().execute(() -> {
			HashMap<String, HashMap<String, String>> userDefined = null;
			try {
				userDefined = UserDefinedFrame.readUserDefinedDefinitions("Pioneer Natural Resources");
			} catch (IOException e) {
				System.out.println("Exception caught reading userDefined Definitions");
			}
			int offset = 0;
			if (sigValsMap.get("Stage Number").equals("1")) {
				offset = 20;
			}
			for (String s : userDefined.keySet()) {
				if (userDefined.get(s).get("Location").equals("0")) {
					continue;
				}
				String location = ExcelTransfer.convertExcelAddressToRC(userDefined.get(s).get("Location"), stage);
				Integer row = Integer.valueOf(location.split(",")[0]) + offset;
				Integer column = Integer.valueOf(location.split(",")[1]);
				Matcher matcher = Pattern.compile("\\D").matcher(sigValsMap.get(s));
				if (matcher.find()) {
					ExcelTransfer.changeTypeToString(sheet, sheet.getRow(row).getCell(column));
					sheet.getRow(row).getCell(column).setCellValue(sigValsMap.get(s));
				} else {
					ExcelTransfer.changeTypeToDouble(workbook, sheet, sheet.getRow(row).getCell(column), false);
					sheet.getRow(row).getCell(column).setCellValue(Double.valueOf(sigValsMap.get(s)));
				}
			}
		});
	}

	public Boolean getTransferred() {
		return this.transferred;
	}

	public static void breakLinks() {
		XSSFWorkbook workbook = InvoiceEvaluate.getInvoiceWorkbook(InvoiceEvaluate.getInvoicePath());
		for (ExternalLinksTable link : workbook.getExternalLinksTable()) {
			link.getDefinedNames().forEach(System.out::println);
		}
	}

	@SuppressWarnings("resource")
	public XSSFWorkbook getWorkbook() throws HeadlessException, IOException {
		File f = new File(ReadDirectory.readDirect() + "\\" + well + "\\" + well + " - TR.xlsm");
		FileInputStream fileInputStream = new FileInputStream(f);
		if (!f.exists()) {
			return null;
		}

		this.wellWorkbook = new XSSFWorkbook(fileInputStream);
		// if(this.wellWorkbook.)
		evaluate = wellWorkbook.getCreationHelper().createFormulaEvaluator();
		return this.wellWorkbook;
	}

	public XSSFSheet getSheet() {
		String sheetName;
		if (stage == 1) {
			sheetName = "General - Stage 1";
		} else {
			sheetName = "Stage " + String.valueOf(stage);
		}
		hideSheets();
		this.sheet = wellWorkbook.getSheet(sheetName);
		wellWorkbook.setSheetVisibility(wellWorkbook.getSheetIndex(sheetName), SheetVisibility.VISIBLE);
		return sheet;
	}

	public void hideSheets() {
		Integer i;
		for (i = 0; i < wellWorkbook.getNumberOfSheets(); i++) {
			String iSheet = wellWorkbook.getSheetName(i);
			switch (iSheet) {
			case "General - Stage 1", "Treatment Summary":
				wellWorkbook.setSheetVisibility(i, SheetVisibility.VISIBLE);
				break;
			default:
				wellWorkbook.setSheetVisibility(i, SheetVisibility.HIDDEN);
				break;
			}
		}
	}

	public void saveWorkbook(XSSFWorkbook workbook) throws IOException {
		File f = new File(ReadDirectory.readDirect() + "\\" + well + "\\" + well + " - TR.xlsm");

		FileOutputStream fileOutput = new FileOutputStream(f);
		workbook.write(fileOutput);

		fileOutput.flush();
		fileOutput.close();
		workbook.close();
	}

	public Integer getStartRowOffset(Integer stage) {
		int offset = 0;
		if (stage == 1) {
			offset = 20;
		} else {
			offset = 0;
		}
		return offset;
	}

	public void setLastRow() {
		this.lastRow = SheetData.getLastDataRow(Main.yess.getmTable());
	}

	public Integer getLastRow() {
		return this.lastRow;
	}

	public void setSandMap() {
		this.sandMap = SheetData.getSigTableData(Main.yess.diagTable3);
	}

	public void setPRCMap() {
		this.prcMap = SheetData.getSigTableData(Main.yess.diagTable2);
		this.prcMap.put("Clean Total", String.valueOf(Main.yess.diagTable4.getValueAt(0, 0)));
		this.prcMap.put("Slurry Total", String.valueOf(Main.yess.diagTable4.getValueAt(0, 1)));
	}

	public void setChemMap() {
		this.chemMap = SheetData.getSigTableData(Main.yess.diagTable1);
	}

	public LinkedHashMap<String, String> getSandMap() {
		return this.sandMap;
	}

	public LinkedHashMap<String, String> getPRCMap() {
		return this.prcMap;
	}

	public LinkedHashMap<String, String> getChemMap() {
		return this.chemMap;
	}

	public void dataTable(Integer lastRow) {

		startTime = new SheetData(lastRow, 0, Main.yess.getmTable());
		endTimes = new SheetData(lastRow, 1, Main.yess.getmTable());
		stageNumber = new SheetData(lastRow, 2, Main.yess.getmTable());
		startDate = new SheetData(lastRow, 3, Main.yess.getmTable());
		endDate = new SheetData(lastRow, 4, Main.yess.getmTable());
		sandConc = new SheetData(lastRow, 5, Main.yess.getmTable());
		cleanTotal = new SheetData(lastRow, 6, Main.yess.getmTable());
		avgPressure = new SheetData(lastRow, 7, Main.yess.getmTable());
		avgRate = new SheetData(lastRow, 8, Main.yess.getmTable());
		slurryTotal = new SheetData(lastRow, 9, Main.yess.getmTable());
		sandType = new SheetData(lastRow, 10, Main.yess.getmTable());
		sandVol = new SheetData(lastRow, 11, Main.yess.getmTable());
		subStageName = new SheetData(lastRow, 12, Main.yess.getmTable());

	}

	public void writeToWorkbook(ArrayList<String> data, Integer column) {
		String sheetName = sheet.getSheetName();
		int i = 0;
		Integer ii;

		if (sheetName.contains("General")) {
			this.startRowOffset = 20;
		} else {
			this.startRowOffset = 0;
		}
		for (String a : data) {
			XSSFCell cell = sheet.getRow(16 + startRowOffset + i).getCell(column);

			switch (column) {
			case 1:
				cell.setAsActiveCell();
				cell.setCellValue(Integer.valueOf(a));
				evaluate.evaluateFormulaCell(cell);
				break;
			case 9, 11, 12, 17, 20:
				cell.setAsActiveCell();
				if (!a.equals("")) {
					cell.setCellValue(Double.valueOf(a));
				} else {
					cell.setCellValue("");
				}
				evaluate.evaluateFormulaCell(cell);
				break;
			case 2, 8, 10:
				cell.setAsActiveCell();
				cell.setCellValue(a);
				evaluate.evaluateFormulaCell(cell);
				break;
			case 4, 6:

				cell.setAsActiveCell();
				cell.setCellValue(Date.valueOf(a));
				evaluate.evaluateFormulaCell(cell);
				break;
			case 5, 7:
				cell.setAsActiveCell();
				cell.setCellValue(a);
				evaluate.evaluateFormulaCell(cell);
				break;
			}
			i++;
		}

	}

	public void sandWriteToWorkbook() {
		int i = 0;
		for (String a : sandMap.keySet()) {
			if (a.contains("Total Proppant")) {
				XSSFCell cellVol = sheet.getRow(12 + startRowOffset).getCell(19);
				cellVol.setCellValue(sandMap.get(a));
			} else {
				XSSFCell cellType = sheet.getRow(59 + i + startRowOffset).getCell(1);
				XSSFCell cellVol = sheet.getRow(59 + i + startRowOffset).getCell(5);
				cellType.setAsActiveCell();
				cellType.setCellType(CellType.STRING);
				cellType.setCellValue(a);
				cellVol.setAsActiveCell();
				cellVol.setCellType(CellType.NUMERIC);
				cellVol.setCellValue(Double.valueOf(sandMap.get(a)));

				i++;
			}
		}
	}

	public void chemWriteToWorkbook() {
		int i = 0;
		int ii = 0;
		ArrayList<String> chemOrder = getOrderedChemMap(wellWorkbook.getSheet("General - Stage 1"), stage);
		for (String a : chemOrder) {
			if (a.toUpperCase().contains("ACID") | a.toUpperCase().contains("HCL")) {
				XSSFCell cellType = sheet.getRow(70 + startRowOffset).getCell(1);
				XSSFCell cellVol = sheet.getRow(70 + startRowOffset).getCell(5);
				Double acidVol = FracCalculations
						.getDoubleRoundedDouble(Double.valueOf(chemMap.get(a)) / Double.valueOf(42), 2);
				String acidVolStr = String.valueOf(acidVol);
				cellType.setAsActiveCell();
				cellType.setCellType(CellType.STRING);
				cellType.setCellValue(a);

				ExcelTransfer.changeTypeToDouble(wellWorkbook, sheet, cellVol, true);
				cellVol.setCellValue(acidVol);

			} else {
				XSSFCell slickType = sheet.getRow(79 + i + startRowOffset).getCell(1);
				XSSFCell cellType = sheet.getRow(79 + i + startRowOffset).getCell(3);
				XSSFCell cellVol = sheet.getRow(16 + startRowOffset).getCell(34 + ii);
				XSSFCell cellLabel = sheet.getRow(16 + startRowOffset).getCell(33 + ii);
				cellType.setAsActiveCell();
				cellType.setCellType(CellType.STRING);
				cellType.setCellValue(a);
				slickType.setAsActiveCell();
				slickType.setCellType(CellType.STRING);
				slickType.setCellValue("SLICKWATER");
				evaluate.evaluateFormulaCell(cellLabel);
				cellVol.setAsActiveCell();
				cellVol.setCellType(CellType.NUMERIC);
				cellVol.setCellValue(Double.valueOf(chemMap.get(a)));

				i++;
				ii += 2;

			}
		}
		XSSFCell cellEndDate = sheet.getRow(12 + startRowOffset).getCell(8);
		XSSFCell cellEndTime = sheet.getRow(12 + startRowOffset).getCell(9);

		cellEndDate.setCellValue(java.sql.Date.valueOf(SheetData.getEndDate(Main.yess.getmTable())));
		cellEndTime.setCellValue(SheetData.getEndTime(Main.yess.getmTable()));
	}

	public ArrayList<String> getOrderedChemMap(XSSFSheet firstStageSheet, Integer stage) {
		if (stage.equals(1)) {
			return orderedChemMapStageOne();
		}
		ArrayList<String> chemOrder = new ArrayList<>();
		int i = 0;
		while (firstStageSheet.getRow(99 + i) != null && firstStageSheet.getRow(99 + i).getCell(3) != null
				&& firstStageSheet.getRow(99 + i).getCell(3).getStringCellValue() != "") {
			String chem = firstStageSheet.getRow(99 + i).getCell(3).getStringCellValue();
			if (!chemMap.keySet().contains(chem)) {
				chemMap.put(chem, "0");
			}
			chemOrder.add(firstStageSheet.getRow(99 + i).getCell(3).getStringCellValue());
			i++;
		}
		ArrayList<String> newChems = findNewChems(chemOrder);
		addNewChemsToStageOne(newChems, firstStageSheet, 99 + i);
		addNewChemsToOrderedArray(newChems, chemOrder);
		addAcidToOrder(chemOrder);
		return chemOrder;
	}

	private void addAcidToOrder(ArrayList<String> chemOrder) {
		for (String s : chemMap.keySet()) {
			if (s.toUpperCase().contains("ACID") | s.toUpperCase().contains("HCL")) {
				chemOrder.add(s);
			}
		}
	}

	private ArrayList<String> orderedChemMapStageOne() {
		ArrayList<String> chemOrder = new ArrayList<>();
		for (String s : chemMap.keySet()) {
			chemOrder.add(s);
		}
		return chemOrder;
	}

	private void addNewChemsToOrderedArray(ArrayList<String> newChems, ArrayList<String> chemOrder) {
		if (!newChems.isEmpty()) {
			chemOrder.addAll(newChems);
		}
	}

	private ArrayList<String> findNewChems(ArrayList<String> chemOrder) {
		ArrayList<String> newChems = new ArrayList<>();
		for (String s : chemMap.keySet()) {
			if (s.toUpperCase().contains("ACID") || s.toUpperCase().contains("HCL")) {
				continue;
			}
			if (!chemOrder.contains(s)) {
				newChems.add(s);
			}
		}
		return newChems;
	}

	private void addNewChemsToStageOne(ArrayList<String> newChems, XSSFSheet firstStageSheet, int firstRow) {
		if (newChems.isEmpty()) {
			return;
		}
		int i = 0;
		for (String s : newChems) {
			firstStageSheet.getRow(firstRow + i).getCell(3).setCellType(CellType.STRING);
			firstStageSheet.getRow(firstRow + i).getCell(3).setCellValue(s);
			firstStageSheet.getRow(firstRow + i).getCell(1).setCellType(CellType.STRING);
			firstStageSheet.getRow(firstRow + i).getCell(1).setCellValue("SLICKWATER");
		}
	}

	public void prcWriteToWorkbook() {
		Integer mainRow = startRowOffset + 12;
		XSSFCell cellTreatment = sheet.getRow(12 + startRowOffset).getCell(1);
		cellTreatment.setAsActiveCell();
		cellTreatment.setCellType(CellType.NUMERIC);
		cellTreatment.setCellValue(Integer.valueOf(stage));
		Double aPressure = 0.0;
		Double aRate = 0.0;
		Double tvd = 0.0;
		for (String a : prcMap.keySet()) {

			switch (a) {
			case ("Perfs"):
				String topPerf = prcMap.get(a).split("-")[0].trim();
				String botPerf = prcMap.get(a).split("-")[1].trim();
				XSSFCell cellTop = sheet.getRow(mainRow).getCell(4);
				XSSFCell cellBot = sheet.getRow(mainRow).getCell(5);

				cellTop.setAsActiveCell();
				cellTop.setCellType(CellType.NUMERIC);
				cellTop.setCellValue(Double.valueOf(topPerf));

				cellBot.setAsActiveCell();
				cellBot.setCellType(CellType.NUMERIC);
				cellBot.setCellValue(Double.valueOf(botPerf));

				break;
			case ("Open Pressure"):
				XSSFCell openCell = sheet.getRow(mainRow).getCell(11);
				openCell.setAsActiveCell();
				openCell.setCellType(CellType.NUMERIC);
				openCell.setCellValue(Math.round(Double.valueOf(prcMap.get(a))));
				break;
			case ("Close Pressure"):
				XSSFCell closeCell = sheet.getRow(mainRow).getCell(16);
				closeCell.setAsActiveCell();
				closeCell.setCellType(CellType.NUMERIC);
				closeCell.setCellValue(Math.round(Double.valueOf(prcMap.get(a))));
				break;
			case ("ISIP"):
				XSSFCell isipCell = sheet.getRow(mainRow).getCell(12);
				isipCell.setAsActiveCell();
				isipCell.setCellType(CellType.NUMERIC);
				isipCell.setCellValue(Math.round(Double.valueOf(prcMap.get(a))));
				break;
			case ("Average Pressure"):
				XSSFCell avgPresCell = sheet.getRow(mainRow).getCell(29);
				avgPresCell.setAsActiveCell();
				avgPresCell.setCellType(CellType.NUMERIC);
				avgPresCell.setCellValue(Math.round(Double.valueOf(prcMap.get(a))));
				aPressure = Double.valueOf(prcMap.get(a));
				break;
			case ("Max Pressure"):
				XSSFCell maxPresCell = sheet.getRow(mainRow).getCell(30);
				maxPresCell.setAsActiveCell();
				maxPresCell.setCellType(CellType.NUMERIC);
				maxPresCell.setCellValue(Math.round(Double.valueOf(prcMap.get(a))));
				break;
			case ("Average Rate"):
				XSSFCell avgRateCell = sheet.getRow(mainRow).getCell(32);
				avgRateCell.setAsActiveCell();
				avgRateCell.setCellType(CellType.NUMERIC);
				avgRateCell.setCellValue(Math.round(Double.valueOf(prcMap.get(a))));
				aRate = Double.valueOf(prcMap.get(a));
				break;
			case ("Max Rate"):
				XSSFCell maxRateCell = sheet.getRow(mainRow).getCell(33);
				maxRateCell.setAsActiveCell();
				maxRateCell.setCellType(CellType.NUMERIC);
				maxRateCell.setCellValue(Math.round(Double.valueOf(prcMap.get(a))));
				break;
			case ("Breakdown Pressure"):
				XSSFCell breakCell = sheet.getRow(mainRow).getCell(27);
				breakCell.setAsActiveCell();
				breakCell.setCellType(CellType.NUMERIC);
				breakCell.setCellValue(Math.round(Double.valueOf(prcMap.get(a))));
				break;
			case ("Slurry Total"):
				XSSFCell slurryCell = sheet.getRow(mainRow).getCell(34);
				slurryCell.setAsActiveCell();
				slurryCell.setCellType(CellType.NUMERIC);
				slurryCell.setCellValue(Math.round(Double.valueOf(prcMap.get(a))));
				break;
			case ("Clean Total"):
				XSSFCell cleanCell = sheet.getRow(69 + startRowOffset).getCell(5);
				XSSFCell slickCell = sheet.getRow(69 + startRowOffset).getCell(1);
				XSSFCell cleanLock = sheet.getRow(56 + startRowOffset).getCell(9);

				slickCell.setCellType(CellType.STRING);
				cleanCell.setCellType(CellType.NUMERIC);
				slickCell.setAsActiveCell();
				slickCell.setCellValue("SLICKWATER");
				cleanCell.setAsActiveCell();
				cleanCell.setCellValue(Double.valueOf(prcMap.get(a)));

				// evaluate.notifyUpdateCell(cleanCell);
				// evaluate.notifyUpdateCell(cleanLock);
				break;
			case ("Comments"):
				XSSFCell commentCell = sheet.getRow(12 + startRowOffset).getCell(43);
				commentCell.setCellType(CellType.STRING);
				commentCell.setCellValue(prcMap.get(a));
				break;
			case ("TVD"):
				tvd = Double.valueOf(prcMap.get(a));
			case ("Pump Time"):
				break;
			}
		}

	}

	public void mainSummary() {
		writeToWorkbook(stageNumber.getDataColumn(), 1);
		writeToWorkbook(subStageName.getDataColumn(), 2);
		writeToWorkbook(startDate.getDataColumn(), 4);
		writeToWorkbook(startTime.getDataColumn(), 5);
		writeToWorkbook(endDate.getDataColumn(), 6);
		writeToWorkbook(endTimes.getDataColumn(), 7);
		writeToWorkbook(slickwater(stageNumber.getDataColumn().size(), subStageName.getDataColumn()), 8);
		writeToWorkbook(cleanTotal.getDataColumn(), 9);
		writeToWorkbook(sandType.getDataColumn(), 10);
		writeToWorkbook(sandVol.getDataColumn(), 11);
		writeToWorkbook(sandConc.getDataColumn(), 12);
		writeToWorkbook(avgRate.getDataColumn(), 20);
		writeToWorkbook(avgPressure.getDataColumn(), 17);
	}

	public static ArrayList<String> slickwater(Integer size, ArrayList<String> subStageName) {
		int i;
		ArrayList<String> slick = new ArrayList<>();
		for (i = 0; i < size; i++) {
			if (subStageName.get(i).toUpperCase().contains("ACID")) {
				slick.add("15% HCL ACID");
			} else {
				slick.add("SLICKWATER");
			}
		}
		return slick;
	}

	public void updateTimes() {
		int i;
		for (i = 16 + startRowOffset; i < 57 + startRowOffset; i++) {
			XSSFCell cell = sheet.getRow(i).getCell(29);
			evaluate.evaluateFormulaCell(cell);
		}
		for (i = 0; i < 2; i++) {
			XSSFCell cell = sheet.getRow(12 + startRowOffset).getCell(i + 6);
			evaluate.evaluateFormulaCell(cell);
		}
	}

	public void updateSand() {
		int i;
		int ii;
		for (i = 59 + startRowOffset; i < 66 + startRowOffset; i++) {
			for (ii = 1; ii <= 5; ii++) {
				XSSFCell cell = sheet.getRow(i).getCell(ii);
				evaluate.evaluateFormulaCell(cell);
			}
		}
		for (i = 16 + startRowOffset; i < 41 + startRowOffset; i++) {
			XSSFCell cell = sheet.getRow(16 + startRowOffset + i).getCell(11);
			if (cell != null) {
				evaluate.notifyUpdateCell(cell);
				evaluate.evaluateFormulaCell(cell);
				evaluate.notifySetFormula(cell);
			}
		}
		XSSFCell cell = sheet.getRow(12 + startRowOffset).getCell(19);

		evaluate.evaluateFormulaCell(cell);
		// evaluate.evaluateAll();

	}

	public void updateFluids() {
		int i;
		int ii;
		for (i = 69 + startRowOffset; i < 76 + startRowOffset; i++) {
			for (ii = 1; ii <= 5; ii++) {
				XSSFCell cell = sheet.getRow(i).getCell(ii);
				evaluate.evaluateFormulaCell(cell);
			}
		}
		XSSFCell cellHP = sheet.getRow(12 + startRowOffset).getCell(38);
		XSSFCell cellFG = sheet.getRow(12 + startRowOffset).getCell(39);
		XSSFCell cellPumpTime = sheet.getRow(12 + startRowOffset).getCell(37);
		XSSFCell cellClean = sheet.getRow(56 + startRowOffset).getCell(9);

		evaluate.evaluateInCell(cellClean);
		evaluate.evaluateFormulaCell(cellPumpTime);
		evaluate.evaluateFormulaCell(cellHP);
		evaluate.evaluateFormulaCell(cellFG);
	}

	public void updateChems() {
		int i;
		int ii;
		for (i = 79 + startRowOffset; i < 89 + startRowOffset; i++) {
			for (ii = 1; ii <= 8; ii++) {
				XSSFCell cell = sheet.getRow(i).getCell(ii);
				evaluate.evaluateFormulaCell(cell);
			}
		}
	}

	public void testUpdateLinks(XSSFWorkbook workbook) {
		workbook.setForceFormulaRecalculation(true);
	}

	public void printWorkbook() {

	}

}
