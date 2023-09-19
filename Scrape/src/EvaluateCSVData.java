import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import exceltransfer.DataNames;

public class EvaluateCSVData implements Runnable {
	Analyze analyze;
	HashMap<String, String> lastStartClose;
	LinkedHashMap<String, ArrayList<String>> map;
	StageDate stageDate;
	ExecutorService executor;
	String well;
	String stageNumber;
	ArrayList<String> sandType;
	ArrayList<Long> sandAmt;
	private ArrayList<String> nameArray;
	private ArrayList<Double> acidAmt;
	private Double roundSand;
	private String operator;
	private HashMap<String, LinkedHashMap<String, String>> chemSandMap;

	EvaluateCSVData(String well, String stageNumber, ArrayList<Double> acidAmt,
			HashMap<String, LinkedHashMap<String, String>> chemSandMap, Double roundSand, String operator) {
		this.well = well;
		this.stageNumber = stageNumber;
		this.executor = Executors.newCachedThreadPool();
		this.sandType = new ArrayList<>();
		this.sandAmt = new ArrayList<>();
		this.acidAmt = acidAmt;
		this.roundSand = roundSand;
		this.operator = operator;
		this.chemSandMap = chemSandMap;
	}

	public void addSandToArrays() {
		for (String type : chemSandMap.get(ChemSandFrame.SAND_NAME).keySet()) {
			sandAmt.add(Long.valueOf(chemSandMap.get(ChemSandFrame.SAND_NAME).get(type)));
			sandType.add(type);
		}
	}

	public void execute() {
		new ClearTable(Main.yess.getmTable());
		this.map = readData(well, stageNumber);
		if (!map.containsKey("Clean Rate")) {
			map.put("Clean Rate", Analyze.constructCleanRate(map.get("Clean Grand Total")));
		}
		map = mainFrame.butClick.smoothChannels(map);
		if (this.map == null) {
			return;
		}
		this.lastStartClose = Main.yess.getLastStartClose();
		HashMap<String, Integer> diagnostics = new HashMap<>();
		diagnostics = dummyDiagnostics();
		addSandToArrays();
		mainFrame.removeZeroValues(Main.yess.stageInputs);
		Main.yess.stageInputs.remove("Acid_Spearhead");
		try {
			this.analyze = new Analyze(map, diagnostics, Main.yess.stageInputs, pumpedAcid(),
					ReadDirectory.readCSVChannels().get("Stage Number"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		execute();
		if (analyze == null) {
			JOptionPane.showMessageDialog(Main.yess, "Check your csv channels");
			return;
		}
		try {
			HashMap<String, Boolean> savedOptions = mainFrame.getSavedOptions();
			this.analyze.analyzeMethod(savedOptions);
			this.nameArray = analyze.getNameArray();
		} catch (InterruptedException | ExecutionException | NumberFormatException | IOException
				| ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		SetDiagnostics setDiagnostics = null;
		try {
			setDiagnostics = new SetDiagnostics(ReadOperatorTemplate.readTemplateValueNames(Main.yess.operator));
		} catch (IOException e) {
			System.out.println("Read Operator Template Exception readingValueNames");
		}
		String wellName = Main.yess.textCombo1.getSelectedItem().toString();
		TreatmentSummary treatmentSummary = evaluateData(setDiagnostics);
		setDiagnostics.putAll(analyze.getDiagnosticValues());
		setDiagnostics.put(DataNames.ACID_VOLUME, String.valueOf(Math.round(getAcidSpearhead())));
		setDiagnostics.put(DataNames.PAD_NAME,
				Main.yess.jobLogWells.getPadMap().get(Main.yess.jobLogWells.getIdFromWell(wellName)).get("name"));
		setDiagnostics.put(DataNames.WELL_NAME, wellName);
		setDiagnostics.put(DataNames.STAGE_NUMBER, stageNumber);
		setDiagnostics.put(DataNames.PERFS, Main.yess.getPerfs());
		Semaphore diagnosticsSem = new Semaphore(0);
		ArrayList<HashMap<String, String>> mapArray = new ArrayList<>();
		executor.execute(() -> {
			CSVMarkers csvMarkers = null;
			try {
				csvMarkers = new CSVMarkers(map);
			} catch (IOException e) {
				System.out.println("Exception caught EvaluateCSVData::CSVMarkers");
				diagnosticsSem.release();
				return;
			}
			mapArray.add(csvMarkers.getDiagnosticMarkers());
			diagnosticsSem.release();
		});
		executor.shutdown();
		try {
			executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			diagnosticsSem.release();
		}
		treatmentSummary.appendToMap(0, stageDate.getStartTimes());
		treatmentSummary.appendToMap(1, stageDate.getEndTimes());
		treatmentSummary.appendToMap(2, constructStageNum(stageDate.getStartTimes().size()));
		treatmentSummary.appendToMap(3, stageDate.getStartDates());
		treatmentSummary.appendToMap(4, stageDate.getEndDates());
		stageDate.addSigDateTimeValues(setDiagnostics, nameArray);
		try {
			this.nameArray = analyze.getNameArray();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		treatmentSummary.appendToMap(12, nameArray);
		RemoveRow removeRow = new RemoveRow(treatmentSummary.getMap());
		if (operator.equals("Pioneer Natural Resources")) {
			ArrayList<Integer> shutdownRows = removeRow.findShutdownRows();
			if (shutdownRows.size() > 0) {

				int correction = 0;
				PumpTime pumpTime = stageDate.getPumptime();
				for (Integer r : shutdownRows) {
					removeRow.addColumn(r - correction, 6, 9);
					removeRow.deleteRow(r - correction);
					pumpTime.removeIndex(r - correction);
					correction++;
				}
				removeRow.findResumes();
				removeRow.correctSubStageIndex(2);
			}
		} else {
			removeRow.addSweepAfterResume();
		}
		removeRow.fixPreSandStageProgression(pumpedAcid(), analyze.getInputIndecesWithSand(analyze.theIndArray));
		removeRow.fixSandConc("PAD", "FLUSH", "PRE-FLUSH", "ACID");
		try {
			diagnosticsSem.tryAcquire(2000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			System.out.println("InterruptedException EvaluateCSVData::run");
		}
		if (!mapArray.isEmpty()) {
			setDiagnostics.putAll(mapArray.get(0));
		}
		try {
			Main.yess.setUserDefinedInDiagnostics(setDiagnostics, Main.yess
					.userDefinedCalculations(map, setDiagnostics, null, Main.yess.getSummaryMapWithNamedKeys(removeRow))
					.get());
		} catch (InterruptedException | ExecutionException e) {
			removeRow.writeToTable(Main.yess.mTable);
			setDiagnostics.writeMapToTable(Main.yess.diagTable2);
			e.printStackTrace();
			return;
		}
		removeRow.writeToTable(Main.yess.mTable);
		setDiagnostics.writeMapToTable(Main.yess.diagTable2);
		HashMap<String, LinkedHashMap<String, String>> chemSandCopy = new HashMap<>();
		chemSandCopy.putAll(chemSandMap);
		ArrayList<SetDiagnostics> array = new ArrayList<>();
		array.add(setDiagnostics);
		Executors.newSingleThreadExecutor().execute(() -> {
			try {
				EvaluatedDataObject evaluatedDataObject = EvaluatedDataObject.getFromFile();
				evaluatedDataObject.addToMaps(removeRow.getMap(), chemSandCopy, array.get(0), well,
						Integer.valueOf(stageNumber));
				EvaluatedDataObject.writeToFile(evaluatedDataObject);
				Main.yess.sendDataFileSocket(evaluatedDataObject);
			} catch (ClassNotFoundException | IOException e1) {
				System.out.println("Exception caught EvaluatedDataObject");
				return;
			}
		});
	}

	public Double getAcidSpearhead() {
		Double acidVol = 0.0;
		Double percent = 0.0;
		for (String s : chemSandMap.get("chemicals").keySet()) {
			Matcher matcher = Pattern.compile("((\\d|\\.)+)%").matcher(s.toUpperCase());
			Double found = 0.0;
			if (matcher.find()) {
				acidVol = (found = Double.valueOf(matcher.group().replace("%", ""))) > percent
						? Double.valueOf(chemSandMap.get("chemicals").get(s))
						: acidVol;
				percent = (acidVol == Double.valueOf(chemSandMap.get("chemicals").get(s))) ? found : percent;
			} else if (s.toUpperCase().contains("HCL") || s.toUpperCase().contains("ACID")) {
				acidVol = Double.valueOf(chemSandMap.get("chemicals").get(s));
			}
		}
		return acidVol;
	}

	public Boolean pumpedAcid() {
		if (getAcidSpearhead() > 0.0) {
			return true;
		}
		return false;
	}

	public void inputMaxAverages(HashMap<String, Double> maxAverage, JTable table) {
		for (String k : maxAverage.keySet()) {

			switch (k) {
			case ("avgPressure"):
				table.setValueAt(Math.round(maxAverage.get(k)), 4, 1);
				break;
			case ("maxPressure"):
				table.setValueAt(Math.round(maxAverage.get(k)), 5, 1);
				break;
			case ("avgRate"):
				table.setValueAt(Math.round(maxAverage.get(k)), 6, 1);
				break;
			case ("maxRate"):
				table.setValueAt(Math.round(maxAverage.get(k)), 7, 1);
				break;
			}

		}
	}

	public static LinkedHashMap<String, ArrayList<String>> readData(String well, String stageNumber) {
		LinkedHashMap<String, ArrayList<String>> data = null;
		try {
			data = DataMapJSon.parseCSVData(ReadDirectory.readCSVDirect(well, stageNumber));
			if (data == null) {
				return null;
			}
		} catch (IOException e1) {
		}
		return data;
	}

	public void inputSand() {
		LinkedHashMap<String, String> sandMap = new LinkedHashMap<>();
		sandMap.put("Total Proppant(lbs)", String.valueOf(getTotalSand(chemSandMap.get("sand"))));
		sandMap.putAll(chemSandMap.get("sand"));
		setTotalSand(sandMap);
	}

	private Long getTotalSand(LinkedHashMap<String, String> sandMap) {
		long totalSand = 0;
		for (String s : sandMap.keySet()) {
			totalSand += Long.valueOf(sandMap.get(s));
		}
		return totalSand;
	}

	public void writeChemicals(JTable table) {
		int i = 0;
		for (String s : chemSandMap.get("chemicals").keySet()) {
			table.setValueAt(s, i, 0);
			table.setValueAt(chemSandMap.get("chemicals").get(s), i, 1);
			i++;
		}
	}

	private void setTotalSand(Map<String, String> yesSand) {
		for (String s : yesSand.keySet()) {
			if (s.contains("Total")) {
				sandType.add(0, s);
				sandAmt.add(0, Long.valueOf(yesSand.get(s)));
				continue;
			}
		}
		setSandTypeAmtInTable(Main.yess.diagTable3);
	}

	private void setSandTypeAmtInTable(JTable table) {
		ArrayList<String> newSandType = new ArrayList<>();
		HashMap<String, Long> sandTotalMap = new HashMap<>();
		int i = 0;
		for (String s : sandType) {
			String type = getBaseSandType(s);
			long typeSandAmt = sandAmt.get(i);
			addToNewSandTypeArray(sandTotalMap, newSandType, type, typeSandAmt);
		}
		setSandInTable(table, newSandType, sandTotalMap);
	}

	private void setSandInTable(JTable table, ArrayList<String> newSandType, HashMap<String, Long> sandTotalMap) {
		int i = 0;
		for (String s : newSandType) {
			table.setValueAt(s, i, 0);
			table.setValueAt(sandTotalMap.get(s), i, 1);
			i++;
		}
	}

	private void addToNewSandTypeArray(HashMap<String, Long> sandTotalMap, ArrayList<String> newSandType, String type,
			long typeAmt) {
		if (newSandType.contains(type)) {
			sandTotalMap.put(type, sandTotalMap.containsKey(type) ? (typeAmt + sandTotalMap.get(type)) : typeAmt);
			return;
		}
		sandTotalMap.put(type, typeAmt);
		newSandType.add(type);
	}

	private String getBaseSandType(String sandType) {
		Matcher matcher = Pattern.compile(ChemSandFrame.DUPLICATE_REGEX).matcher(sandType);
		if (matcher.find()) {
			String type = sandType.substring(0, matcher.start()).trim();
			return type.toUpperCase().equals(type) ? type : capWords(type);
		}
		return sandType.toUpperCase().equals(sandType) ? sandType : capWords(sandType);
	}

	private static String capWords(String string) {
		Matcher matcher = Pattern.compile("(^[a-z])|(\\s[a-z])").matcher(string);
		while (matcher.find()) {
			string = (matcher.start()==0?"":string.substring(0, matcher.start()+1)) + String.valueOf(string.charAt(matcher.end()-1)).toUpperCase()
					+ string.substring(matcher.end()).toLowerCase();
			matcher.reset(string);
		}
		return string;
	}

	private void setSandInTable(JTable table, ArrayList<String> sandType, ArrayList<Long> sandAmt) {
		if (sandType.size() == 0) {
			return;
		}
		int count = 0;
		for (String s : sandType) {
			table.setValueAt(s, count, 0);
			table.setValueAt(sandAmt.get(count), count, 1);
			count++;
		}
	}

	private ArrayList<Double> getShift(ArrayList<String> sandType, ArrayList<Long> sandAmt) {
		ArrayList<Double> shift = new ArrayList<>();
		double accruedSand = 0.0;
		int i = 0;
		double totalSand = sandAmt.get(0);

		for (String s : sandType) {
			if (s.contains("Total")) {
				i++;
				continue;
			}
			accruedSand = accruedSand + Double.valueOf(String.valueOf(sandAmt.get(i)));
			shift.add(accruedSand / totalSand);

			i++;
		}
		return shift;
	}

	private ArrayList<String> constructStageNum(Integer size) {
		int i;
		ArrayList<String> stageNum = new ArrayList<>();
		for (i = 1; i <= size; i++) {
			stageNum.add(String.valueOf(i));
		}
		return stageNum;
	}

	private TreatmentSummary evaluateData(SetDiagnostics setDiagnostics) {

		writeChemicals(Main.yess.diagTable1);
		TreatmentSummary treatmentSummary = new TreatmentSummary(13);
		ArrayList<Integer> stageUp = analyze.getDataValueIndex();
		Semaphore stageSandSem = new Semaphore(0);
		Semaphore avgSem = new Semaphore(0);

		this.stageDate = new StageDate(map.get("Job Time"), analyze.getDataValueIndex(), lastStartClose, false);
		StageCleanTotal stageCleanTotal = new StageCleanTotal(map.get("Clean Grand Total"), stageUp, this.nameArray,
				acidAmt, map.get("Slurry Rate"), treatmentSummary, stageSandSem);
		StageSlurryRate slurryRate = new StageSlurryRate(map.get("Slurry Rate"), stageUp, this.nameArray,
				treatmentSummary, avgSem);
		StageSlurryTotal slurryTotal = new StageSlurryTotal(map.get("Slurry Grand Total"), stageUp, this.nameArray,
				acidAmt, map.get("Slurry Rate"), treatmentSummary);
		StageTreatingPressure treatingPressure = new StageTreatingPressure(map.get("Treating Pressure"), stageUp,
				treatmentSummary, avgSem);
		StagePropCon stagePropCon = new StagePropCon(map.get("Prop. Concentration"), stageUp, treatmentSummary,
				stageSandSem, avgSem, analyze.getInputIndeces(stageUp), roundSand);

		executor.execute(stageDate);
		executor.execute(stageCleanTotal);
		executor.execute(slurryRate);
		executor.execute(slurryTotal);
		executor.execute(treatingPressure);
		executor.execute(stagePropCon);
		Semaphore compSem = new Semaphore(0);
		executor.execute(() -> {
			try {
				stageSandSem.acquire(2);
			} catch (InterruptedException e) {

			}
			inputSand();
			StageSand stageSand = new StageSand(sandType, sandAmt, stagePropCon.getAverageCon(),
					stageCleanTotal.getClean(), getShift(sandType, sandAmt), stagePropCon.getLastIndex(),
					this.nameArray, treatmentSummary);
			stageSand.Evaluate();
			fluidTotalInput(Double.valueOf(String.valueOf(stageCleanTotal.getCleanGrand())),
					Double.valueOf(String.valueOf(slurryTotal.getSlurryGrand())), setDiagnostics);
			compSem.release();
		});

		try {
			compSem.acquire();
			SaveChemicals.readChemicals(Main.yess.diagTable1);
		} catch (IOException | InterruptedException e) {
			return treatmentSummary;
		}
		return treatmentSummary;
	}

	private HashMap<String, Integer> dummyDiagnostics() {
		HashMap<String, Integer> diagnostics = new HashMap<>();
		String[] diagNames = { "WellOpenTime", "WellClose", "Equalize", "StartStage", "MinMaxStart", "MinMaxEnd" };
		for (String s : diagNames) {
			diagnostics.put(s, 0);
		}
		return diagnostics;
	}

	private void fluidTotalInput(Double cleanTotal, Double slurryTotal, SetDiagnostics setDiagnostics) {
		Main.yess.diagTable4.setValueAt(cleanTotal, 0, 0);
		Main.yess.diagTable4.setValueAt(slurryTotal, 0, 1);
		setDiagnostics.put(DataNames.CLEAN_TOTAL, String.valueOf(cleanTotal));
		setDiagnostics.put(DataNames.SLURRY_TOTAL, String.valueOf(slurryTotal));
	}

	public void setWell(String well) {
		this.well = well;
	}

	public void setStage(String stageNumber) {
		this.stageNumber = stageNumber;
	}

	public String getWell() {
		return this.well;
	}

	public String getStage() {
		return this.stageNumber;
	}

	private class CSVMarkers {
		HashMap<String, ArrayList<String>> dataMap;
		HashMap<String, String> diagnostics = new HashMap<>();

		CSVMarkers(HashMap<String, ArrayList<String>> dataMap) throws IOException {
			this.dataMap = dataMap;
			setDiagnosticMarkers(this.dataMap);
		}

		public static String readTimeStamp(String filePath) throws IOException {
			String timeStamp = "";
			File file = new File(filePath);
			if (!file.exists()) {
				return null;
			}
			BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
			String temp;
			while ((temp = bufferedReader.readLine()) != null) {
				timeStamp += temp;
			}
			bufferedReader.close();
			return timeStamp;
		}

		public static int getIndexOfStamp(ArrayList<String> timesArray, String timeStamp) {
			int index = timesArray.indexOf(timeStamp);
			return index;
		}

		public final static String DIAGNOSTICS_PATH = "C:\\Scrape\\ScrapePython\\Telo\\Diagnostics\\";
		public final static String OPEN_TIME_FILENAME = "open.txt";
		public final static String CLOSE_TIME_FILENAME = "close.txt";
		public final static String BREAK_TIME_FILENAME = "break.txt";
		public final static String ISIP_TIME_FILENAME = "isip.txt";

		public final static String OPEN = "open";
		public final static String CLOSE = "close";
		public final static String BREAK = "break";
		public final static String ISIP = "isip";

		private static HashMap<String, Integer> getTimeStamps(ArrayList<String> timesArray) throws IOException {
			HashMap<String, Integer> timeStamps = new HashMap<>();
			String parentDir = DIAGNOSTICS_PATH;
			timeStamps.put(OPEN,
					getIndexOfStamp(timesArray,
							readTimeStamp(parentDir + OPEN_TIME_FILENAME) != null
									? readTimeStamp(parentDir + OPEN_TIME_FILENAME).replace(" ", "")
									: "-1"));
			timeStamps.put(CLOSE,
					getIndexOfStamp(timesArray,
							readTimeStamp(parentDir + CLOSE_TIME_FILENAME) != null
									? readTimeStamp(parentDir + CLOSE_TIME_FILENAME).replace(" ", "")
									: "-1"));
			timeStamps.put(BREAK,
					getIndexOfStamp(timesArray,
							readTimeStamp(parentDir + BREAK_TIME_FILENAME) != null
									? readTimeStamp(parentDir + BREAK_TIME_FILENAME).replace(" ", "")
									: "-1"));
			timeStamps.put(ISIP,
					getIndexOfStamp(timesArray,
							readTimeStamp(parentDir + ISIP_TIME_FILENAME) != null
									? readTimeStamp(parentDir + ISIP_TIME_FILENAME).replace(" ", "")
									: "-1"));
			return timeStamps;
		}

		private static ArrayList<String> removeSpacesFromTimes(ArrayList<String> jobTimes) {
			ArrayList<String> fixedTimes = new ArrayList<>();
			for (String s : jobTimes) {
				fixedTimes.add(s.replace(" ", ""));
			}
			return fixedTimes;
		}

		public final static String JOB_TIME_ARRAY = "Job Time";

		private void setDiagnosticMarkers(HashMap<String, ArrayList<String>> dataMap) throws IOException {
			HashMap<String, Integer> timeStamps = getTimeStamps(removeSpacesFromTimes(dataMap.get(JOB_TIME_ARRAY)));
			diagnostics.put(DataNames.OPEN_PRESSURE,
					timeStamps.get(OPEN) != -1 ? dataMap.get(ChannelPane.TREATING_PRESSURE).get(timeStamps.get(OPEN))
							: "0");
			diagnostics.put(DataNames.BREAK_PRESSURE,
					timeStamps.get(BREAK) != -1 ? dataMap.get(ChannelPane.TREATING_PRESSURE).get(timeStamps.get(BREAK))
							: "0");
			diagnostics.put(DataNames.BREAK_TIME,
					timeStamps.get(BREAK) != -1 ? dataMap.get(JOB_TIME_ARRAY).get(timeStamps.get(BREAK)) : "0");
			diagnostics.put(DataNames.BREAK_RATE,
					timeStamps.get(BREAK) != -1 ? dataMap.get(ChannelPane.SLURRY_RATE).get(timeStamps.get(BREAK))
							: "0");
			diagnostics.put(DataNames.ISIP,
					timeStamps.get(ISIP) != -1 ? dataMap.get(ChannelPane.TREATING_PRESSURE).get(timeStamps.get(ISIP))
							: "0");
			diagnostics.put(DataNames.CLOSE_PRESSURE,
					timeStamps.get(CLOSE) != -1 ? dataMap.get(ChannelPane.TREATING_PRESSURE).get(timeStamps.get(CLOSE))
							: "0");
			diagnostics.put(DataNames.BREAK_VOLUME,
					timeStamps.get(BREAK) != -1 ? dataMap.get(ChannelPane.CLEAN_GRAND).get(timeStamps.get(BREAK))
							: "0");
			diagnostics.put(DataNames.ISIP_TIME,
					timeStamps.get(ISIP) != -1 ? dataMap.get(JOB_TIME_ARRAY).get(timeStamps.get(ISIP)) : "0");
		}

		public HashMap<String, String> getDiagnosticMarkers() {
			return this.diagnostics;
		}

	}

}
