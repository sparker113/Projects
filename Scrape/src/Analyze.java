
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.swing.JOptionPane;

import exceltransfer.DataNames;

public class Analyze {

	private ArrayList<Integer> dataValue;
	private int wellClose1;
	private Semaphore semaphore = new Semaphore(0);
	private HashMap<String, Integer> diagnostics;
	private HashMap<String, String> diagnosticValues;
	private CompletableFuture<HashMap<String, Double>> maxAverage = new CompletableFuture<>();
	private Integer stMinMax = 0;
	private HashMap<String, ArrayList<Double>> inputs;
	private HashMap<String, ArrayList<Integer>> stageInputs;
	private int didIt = 0;
	private Boolean pumpedAcid;
	private boolean lastIndSet = false;
	private ArrayList<Integer> stEndAcid = new ArrayList<>();
	private HashMap<String, ArrayList<Integer>> shutdowns = new HashMap<>();
	protected CompletableFuture<StageNames> stageNames;
	private String operator;
	private String well;
	protected ArrayList<Integer> theIndArray = new ArrayList<>();
	private Map<String, ArrayList<String>> dataMap;
	private Integer open;
	private Semaphore copySem = new Semaphore(0);
	private Semaphore doneSem = new Semaphore(0);
	private Double acidInputAmt;
	private Integer lastIndex;
	private String stageChannel;
	private static int endTimeIndex;

	@SuppressWarnings("unlikely-arg-type")
	public Analyze(Map<String, ArrayList<String>> dataMap, String operator, String well,
			HashMap<String, Integer> diagnostics2, HashMap<String, ArrayList<Double>> inputs, Boolean pumpedAcid,
			String stageChannel, int endIndex) throws IOException {
		this.inputs = inputs;
		this.pumpedAcid = pumpedAcid;
		this.diagnostics = diagnostics2;
		this.operator = operator;
		this.well = well;
		this.dataMap = dataMap;
		this.stageNames = new CompletableFuture<>();
		this.acidInputAmt = 0.0;
		this.stageChannel = stageChannel;
		this.diagnosticValues = new HashMap<>();
		endTimeIndex = endIndex;
	}

	Analyze(HashMap<String, ArrayList<String>> dataMap, HashMap<String, Integer> diagnostics,
			HashMap<String, ArrayList<Double>> inputs, Boolean pumpedAcid, String stageChannel) {
		this.inputs = inputs;
		this.well = Main.yess.textCombo1.getItemAt(Main.yess.textCombo1.getSelectedIndex());
		this.pumpedAcid = pumpedAcid;
		this.operator = Main.yess.textField2.getText();
		this.dataMap = dataMap;
		this.stageNames = new CompletableFuture<>();
		this.diagnostics = diagnostics;
		this.stageChannel = stageChannel;
		this.diagnosticValues = new HashMap<>();
		endTimeIndex = dataMap.get("Slurry Rate").size() - 1;
	}

	public final static int MAX_RATE = 150;
	public final static Double MAX_PRESSURE = 13500.0;
	public final static Double MAX_RATE_DOUBLE = 150.0;

	public void calcMaxAverages(ArrayList<String> pressure, ArrayList<String> rate, ArrayList<String> propConc,
			ArrayList<String> slurryGrand) {
		Executors.newSingleThreadExecutor().execute(() -> {
			HashMap<String, Double> maxAverage = new HashMap<>();
			maxAverage.put("Max Pressure",
					UserDefinedFrame.max(UserDefinedFrame.getArrayWithBoundaryCondition(
							UserDefinedFrame.getArrayWithGreaterThanCondition(pressure, rate, 10.0), 0.0,
							MAX_PRESSURE)));
			maxAverage.put("Max Rate",
					UserDefinedFrame.max(UserDefinedFrame.getArrayWithBoundaryCondition(rate, 0.0, MAX_RATE_DOUBLE)));
			maxAverage.put("Average Pressure",
					UserDefinedFrame.avg(UserDefinedFrame.getArrayWithGreaterThanCondition(pressure, propConc, .25)));
			maxAverage.put("Average Rate", UserDefinedFrame.avg(UserDefinedFrame.getArrayWithGreaterThanCondition(rate,
					propConc, .25, maxAverage.get("Max Rate"))));
			maxAverage.put("Pres. At Max Rate", getValueFromValue(pressure, rate, maxAverage.get("Max Rate")));
			maxAverage.put("Sand Start Rate", getValueFromValue(rate, propConc, 0.1));
			maxAverage.put("Vol. To Design Rate", getValueFromValue(slurryGrand, rate, maxAverage.get("Average Rate")));
			this.maxAverage.complete(maxAverage);
		});
	}

	public static Double getValueFromValue(ArrayList<String> valueArray, ArrayList<String> fromValueArray,
			Double value) {
		int index = getIndexOfValue(fromValueArray, value);
		if (index == -1) {
			return 0.0;
		}
		return Double.valueOf(valueArray.get(index));
	}

	public static int getIndexOfValue(ArrayList<String> array, Double maxValue) {
		int i = 0;
		for (String s : array) {
			if (Double.valueOf(s) >= maxValue) {
				return i;
			}
			i++;
		}
		return -1;
	}

	public void setDiagnosticValues(HashMap<String, String> diagnosticValues) {
		this.diagnosticValues = diagnosticValues;
	}

	public HashMap<String, String> getDiagnosticValues() {
		return this.diagnosticValues;
	}

	private void addMaxAveragesToMap(HashMap<String, Double> maxAverage) {
		for (String s : maxAverage.keySet()) {
			diagnosticValues.put(s, String.valueOf(maxAverage.get(s)));
		}
	}

	private void setLastIndex(Integer lastIndex) {
		lastIndSet = true;
		semaphore.release();
		System.out.println("Last Index Set");
		this.lastIndex = lastIndex;
	}

	public Integer getLastIndex() throws InterruptedException {
		if (!lastIndSet) {
			System.out.println("Waiting on Last Index");
			semaphore.acquire();
		}
		System.out.println("Last Index Returned");
		return this.lastIndex;
	}

	public static ArrayList<Double> getCriticallyDampedCurve(Double y00, Double y10, int interval) {
		double y1 = y00 - y10;

		ArrayList<Double> curveArray = new ArrayList<>();
		curveArray.add(y00);
		for (int i = 1; i <= interval; i++) {
			Double value = (y1 + i) * Math.exp(-i) + y10;
			curveArray.add(value);
		}
		return curveArray;///////////////////////////////////////////////////////////////////////////////
	}

	public static Double calcISIP(int zeros, Double y00, Double y10, int interval) {
		double y1 = y00 - y10;

		Double yCalc = (y1 + (zeros + 1)) * Math.exp(-(zeros + 1));
		Double value = getLinearValue(Double.valueOf(zeros + 1), yCalc, Double.valueOf(zeros), yCalc,
				(Function<Double, Double>) (Double yInt) -> {
					return yCalc * (Double.valueOf(zeros + 1)) + yInt;
				});
		value += y10;
		return value;
	}

	public static int findBalancedIndex(ArrayList<String> pressure, int x0) {
		int balancedIndex = FracCalculations.findNextIndexWithSlope(pressure, x0, 5.0, -5.0, 2);
		return balancedIndex;
	}

	public static HashMap<String, Double> identifyEndSigVals(ArrayList<String> pressure, int x0, int zerosIndex)
			throws IOException {
		HashMap<String, Double> isipClosePress = new HashMap<>();
		int balancedIndex = findBalancedIndex(pressure, zerosIndex);
		if (balancedIndex == -1) {
			isipClosePress.put(DataNames.ISIP, 0.0);
			isipClosePress.put(DataNames.CLOSE_PRESSURE, 0.0);
			return isipClosePress;
		}

		int interval = balancedIndex - x0;
		Double y1 = Double.valueOf(pressure.get(balancedIndex));
		Double y0 = Double.valueOf(UserDefinedFrame.max(getTrimmedArray(pressure, zerosIndex + 1, balancedIndex)));

		// mainFrame.plotArray(idealHammer,"ideal_isip.png");
		int hammerZerosX = x0 > zerosIndex ? 0 : zerosIndex - x0;
		Double value = calcISIP(hammerZerosX, y0, y1, interval);
		isipClosePress.put(DataNames.ISIP, value);
		isipClosePress.put(DataNames.CLOSE_PRESSURE, y1);
		return isipClosePress;
	}

	public static ArrayList<String> getTrimmedArray(ArrayList<String> array, int startIndex, int endIndex) {
		ArrayList<String> trimmedArray = new ArrayList<>();
		for (int i = startIndex; i <= endIndex; i++) {
			trimmedArray.add(array.get(i));
		}
		return trimmedArray;
	}

	public static Double getLinearValue(Double x1, Double y1, Double x2, Double slope, Function<Double, Double> func) {
		Double yInt = y1 - (y1 / (slope));
		return func.apply(yInt);

	}

	public static ArrayList<String> getPressureWithRate(ArrayList<String> pressure, ArrayList<String> rate,
			Double minRate) {
		int i = 0;
		ArrayList<String> trimmedPressure = new ArrayList<>();
		while (i < pressure.size() && i < rate.size()) {
			if (Double.valueOf(rate.get(i)) >= minRate) {
				trimmedPressure.add(pressure.get(i));
			}
			i++;
		}
		return trimmedPressure;
	}
	public final static String AUTO_ISIP = "auto_isip";
	public final static String AUTO_ISIP_TIME = "auto_isip_time";
	public final static String AUTO_CLOSE = "auto_close";
	public void automateCloseISIP(Integer zeros) {
		try {
			HashMap<String, Double> isipClosePress = identifyEndSigVals(dataMap.get("Treating Pressure"),
					GetZeros.findIndexOfRateBelow(zeros - 300, dataMap.get("Slurry Rate"), 10.0), zeros);
			diagnosticValues.put(AUTO_ISIP_TIME, FracCalculations.getTimeFromString(dataMap.get(ChannelPane.TIMESTAMP)
					.get(GetZeros.getZerosStatic(dataMap.get(ChannelPane.SLURRY_RATE))),FracCalculations.HH_MM));
			diagnosticValues.put(AUTO_ISIP, String.valueOf(Math.round(isipClosePress.get(DataNames.ISIP))));
			diagnosticValues.put(AUTO_CLOSE,
					String.valueOf(Math.round(isipClosePress.get(DataNames.CLOSE_PRESSURE))));
		} catch (Exception e) {
			return;
		}
	}

	public void analyzeMethod(HashMap<String, Boolean> savedOptions)
			throws InterruptedException, NumberFormatException, IOException {

		calcMaxAverages(dataMap.get("Treating Pressure"), dataMap.get("Slurry Rate"),
				dataMap.get("Prop. Concentration"), dataMap.get("Slurry Grand Total"));
		String maB;
		for (String k : diagnostics.keySet()) {
			if (diagnostics.get(k) != 0) {
				if (k.equals("WellClose")) {
					maB = String.valueOf(diagnostics.get(k));
					diagnostics.replace(k, Integer.valueOf(dataMap.get("elapsedTime").indexOf(maB)));
				} else {
					diagnostics.replace(k,
							Integer.valueOf(dataMap.get("elapsedTime").indexOf(String.valueOf(diagnostics.get(k)))));
				}
			}
		}

		ArrayList<Integer> theArray = new ArrayList<>();
		CountDownLatch latch = new CountDownLatch(3);
		Thread acidThread = new Thread(new Runnable() {
			@Override
			public void run() {
				ArrayList<Integer> acidArray = new ArrayList<>();
				if (operator.contains("Pioneer Natural Resources")) {
					acidArray.addAll(findAcidGreen(dataMap.get("Slurry Rate"), dataMap.get("Treating Pressure")));
				}
				int acid = 0;
				if (!acidArray.isEmpty()) {
					stEndAcid.addAll(acidArray);
				}
				System.out.println("acid thread done");
				latch.countDown();
			}
		});

		Thread holdThread = new Thread(new Runnable() {
			@Override
			public void run() {
				stageInputs = getStageInputs();
				for (String s : stageInputs.keySet()) {
					theIndArray.addAll(stageInputs.get(s));
				}

				System.out.println("hold thread done");
				latch.countDown();
			}
		});

		PerfectShutdown perfectShutdown = new PerfectShutdown(savedOptions);

		Thread threadShutdown = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					setShutdowns(perfectShutdown.FindShutdown());
				} catch (NullPointerException | InterruptedException e) {
					return;
				}
				System.out.println("Shutdown Thread done");
				latch.countDown();
			}
		});

		perfectShutdown.t.start();
		threadShutdown.start();
		acidThread.start();
		holdThread.start();
		try {
			latch.await();
		} catch (InterruptedException e) {
			System.out.println("Exception waiting on latch");
		}

		System.out.println("start this");
		GetZeros findZeros = new GetZeros(dataMap.get("Slurry Rate"), dataMap.get("Treating Pressure"));
		Integer zeros = findZeros.getZeros();
		automateCloseISIP(zeros);
		try {
			doneSem.acquire();
		} catch (InterruptedException e) {
		}

		// checkMinMaxSpread();

		System.out.println("Zeros: " + zeros);
		checkZeros(zeros);
		theIndArray.add(zeros);
		checkForAcid();

		System.out.println("Before Sort: " + theIndArray);
		Collections.sort(theIndArray);

		Integer count1 = 0;
		ArrayList<Integer> sameIndeces = new ArrayList<>();
		for (Integer checkInd : theIndArray) {

			if (count1 == theIndArray.size() - 1) {
				break;
			}
			if (theIndArray.get(count1 + 1).equals(checkInd)) {
				sameIndeces.add(count1);
			}
			count1++;
		}
		if (!sameIndeces.isEmpty()) {
			for (Integer i : sameIndeces) {
				theIndArray.remove(i.intValue());
			}
		}

		fixAddShutdowns();

		correctAcid();
		checkDoubleStages();
		int ii;
		for (ii = 0; ii < theIndArray.size(); ii++) {
			if (theIndArray.get(ii) < 0) {
				theIndArray.remove(ii);
				theIndArray.add(ii, 1);
			}
		}

		System.out.println(stageInputs);
		stageNames.complete(new StageNames(theIndArray, shutdowns, stEndAcid, operator, stageInputs, savedOptions));

		this.setDataValue(theArray);
		diagnostics.put("WellClose", findClose(zeros));
		try {
			addMaxAveragesToMap(maxAverage.get());
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void fixAddShutdowns() {
		ArrayList<Integer> removeIndex = null;
		if (shutdowns != null && !shutdowns.isEmpty() && !shutdowns.get("Shutdowns").isEmpty()) {
			removeShutdownBeforeOpen(shutdowns, diagnostics);
			removeIndex = correctShutdown(theIndArray, getShutdowns());
		}
		if (shutdowns != null && !shutdowns.isEmpty()) {
			for (String s : shutdowns.keySet()) {
				theIndArray.addAll(shutdowns.get(s));
			}
		}
		if (removeIndex != null) {
			for (Integer r : removeIndex) {
				theIndArray.remove(r);
			}
		}
	}

	public void checkDoubleStages() {
		ArrayList<Integer> temp = correctDoubleStage(theIndArray);
		if (temp != null) {
			theIndArray.removeAll(temp);
		}
	}

	public void checkForAcid() {
		if (stEndAcid.isEmpty() & pumpedAcid) {
			Collections.sort(theIndArray);
			if (operator.contains("Pioneer Natural Resources")) {
				stEndAcid.add(theIndArray.get(0) + 60);
				stEndAcid.add(findPumpsUpReturn(25));
				System.out.println("End Acid Array (Empty): " + stEndAcid);
				theIndArray.addAll(stEndAcid);
			} else {
				stEndAcid.add(theIndArray.get(1));
			}

		} else if (!stEndAcid.isEmpty() & pumpedAcid) {
			System.out.println("End Acid Array: " + stEndAcid);
			fixAcidArray(stEndAcid, theIndArray);
			theIndArray.addAll(stEndAcid);
		}

		if (!stEndAcid.isEmpty() && shutdowns.get("Shutdowns") != null && !shutdowns.get("Shutdowns").isEmpty()) {
			acidShutdownCorrection(shutdowns, stEndAcid);
		}
	}

	public void fixAcidArray(ArrayList<Integer> stEndAcid, ArrayList<Integer> theIndArray) {
		Collections.sort(theIndArray);

		if (stEndAcid.get(0) < theIndArray.get(0)) {
			stEndAcid.remove(0);
			stEndAcid.add(0, (theIndArray.get(1) - theIndArray.get(0)) / 4 + theIndArray.get(0));
		}
		if (stEndAcid.get(stEndAcid.size() - 1) > theIndArray.get(1)) {
			stEndAcid.remove(stEndAcid.size() - 1);
			stEndAcid.add((theIndArray.get(1) - stEndAcid.get(0)) / 2 + stEndAcid.get(0));
		}
	}

	public void checkZeros(Integer zeros) throws InterruptedException {
		Integer lastInd = getLastIndex();
		if (zeros < lastInd) {
			theIndArray.remove(lastInd);
		}
	}

	public void checkMinMaxSpread() {
		Integer start = diagnostics.get("MinMaxStart");
		Integer end = diagnostics.get("MinMaxEnd");
		if (start == null || end - start < 500) {
			diagnostics.remove("MinMaxStart");
			diagnostics.put("MinMaxStart", stMinMax);
		} else if (diagnostics.get("MinMaxStart") < 1) {
			diagnostics.remove("MinMaxStart");
			diagnostics.put("MinMaxStart", stMinMax);
		}
	}

	public ArrayList<Integer> getInputIndeces(ArrayList<Integer> theIndArray) {
		ArrayList<Integer> matchingArray = new ArrayList<>();
		HashMap<String, ArrayList<Integer>> inputArray = getStageInputs();
		for (String s : inputArray.keySet()) {
			if (inputArray.get(s).isEmpty() || s.equals("Stage_Up")) {
				continue;
			}
			for (Integer i : theIndArray) {
				if (inputArray.get(s).contains(i)) {
					matchingArray.add(theIndArray.indexOf(i));
				}
			}
		}
		return matchingArray;
	}

	public ArrayList<Integer> getInputIndecesWithSand(ArrayList<Integer> theIndArray) {
		ArrayList<Integer> matchingArray = new ArrayList<>();
		HashMap<String, ArrayList<Integer>> inputArray = getStageInputs();
		for (String s : inputArray.keySet()) {
			if (inputArray.get(s).isEmpty()) {
				continue;
			}
			for (Integer i : theIndArray) {
				if (inputArray.get(s).contains(i)) {
					matchingArray.add(theIndArray.indexOf(i));
				}
			}
		}
		return matchingArray;
	}

	public void clearInputs() {
		this.inputs.clear();
	}

	public Double getAcidAmt() {
		return this.acidInputAmt;
	}

	public StageNames getStageNames() throws InterruptedException, ExecutionException {
		return stageNames.get();
	}

	public ArrayList<String> getNameArray() throws InterruptedException, ExecutionException {
		return stageNames.get().getNameArray();
	}

	public ArrayList<String> getDate() {
		return this.dataMap.get("timestamp");
	}

	public ArrayList<String> getElapsedTime() {
		return this.dataMap.get("elapsedTime");
	}

	public ArrayList<String> getTreatingPressure() {
		return this.dataMap.get("Treating Pressure");
	}

	public ArrayList<String> getTreatingPressurePlots() {
		ArrayList<String> tPPlots = new ArrayList<>();
		tPPlots.add(0, "Treating Pressure");
		tPPlots.addAll(this.dataMap.get("Treating Pressure"));
		return tPPlots;
	}

	public ArrayList<String> getCleanTotal() {
		return this.dataMap.get("Clean Grand Total");
	}

	public ArrayList<String> getSlurryRate() {
		return this.dataMap.get("Slurry Rate");
	}

	public ArrayList<String> getSlurryRatePlots() {
		ArrayList<String> slRatePlots = new ArrayList<>();
		slRatePlots.add(0, "Slurry Rate");
		slRatePlots.addAll(this.dataMap.get("Slurry Rate"));
		return slRatePlots;
	}

	public ArrayList<String> getSlurryTotal() {
		return this.dataMap.get("Slurry Grand Total");
	}

	public ArrayList<String> getPropConc() {
		return this.dataMap.get("Prop. Concentration");
	}

	public synchronized ArrayList<String> getBackside() {
		if (this.dataMap.get("Backside") == null) {
			ArrayList<String> tempBackside = new ArrayList<>();
			for (int i = 0; i < endTimeIndex; i++) {
				tempBackside.add("0");
			}
			this.dataMap.put("Backside", tempBackside);
		}
		return this.dataMap.get("Backside");
	}

	public ArrayList<String> getBacksidePlots() {
		ArrayList<String> backsidePlots = new ArrayList<>();
		backsidePlots.add(0, "Backside");
		if (dataMap.get("Backside") != null) {
			backsidePlots.addAll(getBackside());
		} else {
			String message = "<html><div style=\"width:%dpx\">Double-check your backside pressure channel;"
					+ " if you are plotting the backside pressure, correct the channel and re-run the stage before "
					+ "making the plots</div></html>";
			System.out.println(message);
			JOptionPane.showMessageDialog(null, String.format(message, 450));
		}
		return backsidePlots;
	}

	public ArrayList<Integer> getDataValueIndex() {
		return this.theIndArray;
	}

	public void setDataValue(ArrayList<Integer> theArray) {
		this.dataValue = theArray;
	}

	public ArrayList<Integer> getDataValue() {
		return this.dataValue;
	}

	public void setWellClose1(int wellClose1) {
		this.wellClose1 = wellClose1;
	}

	public int getWellClose1() {
		return this.wellClose1;
	}

	public HashMap<String, Double> getMaxAverage() throws InterruptedException, ExecutionException {
		return this.maxAverage.get();
	}

	public ArrayList<Integer> getAcidArray() {
		return this.stEndAcid;
	}

	public void setShutdowns(HashMap<String, ArrayList<Integer>> somethingElseShutdowns) {
		this.shutdowns = somethingElseShutdowns;
	}

	public HashMap<String, ArrayList<Integer>> getShutdowns() {
		return this.shutdowns;
	}

	// Finds the index within the data arrays where rate reached less than 5 bpm
	// when finishing the stage
	public class GetZeros {
		ArrayList<String> slurryRate;
		ArrayList<String> treatingPressure;

		private GetZeros(ArrayList<String> slurryRate, ArrayList<String> treatingPressure) {
			this.slurryRate = slurryRate;
			this.treatingPressure = treatingPressure;
		}

		public Integer getZeros() {

			Integer zeros = getZerosStatic(slurryRate, 15.0);
			return zeros;

		}

		public static Integer getZerosStatic(ArrayList<String> slurryRate, Double rateAtZero) {
			int startIndex = postStageRate(slurryRate, rateAtZero / 2.0);
			for (int i = startIndex; i > 3; i--) {
				if (Double.valueOf(slurryRate.get(i)) > rateAtZero) {
					return i;
				}
			}
			return endTimeIndex;
		}
		public static Integer getZerosStatic(ArrayList<String> slurryRate) {
			Double rateAtZero = Double.valueOf(slurryRate.get(endTimeIndex));
			int startIndex = postStageRate(slurryRate, rateAtZero / 2.0);
			for (int i = startIndex; i > 3; i--) {
				if (Double.valueOf(slurryRate.get(i)) > rateAtZero) {
					return i;
				}
			}
			return endTimeIndex;
		}
		public static Integer postStageRate(ArrayList<String> slurryRate, Double rateAtZero) {
			endTimeIndex = endTimeIndex >= slurryRate.size() ? slurryRate.size() - 1 : endTimeIndex;
			for (int i = endTimeIndex; i > (endTimeIndex > 300 ? endTimeIndex - 300 : 1); i--) {
				if (Double.valueOf(slurryRate.get(i)) < rateAtZero) {
					return i;
				}
			}
			return endTimeIndex;
		}

		public static Integer findIndexOfRateBelow(int startIndex, ArrayList<String> slurryRate, Double rate) {
			for (int i = startIndex; i < slurryRate.size(); i++) {
				if (Double.valueOf(slurryRate.get(i)) < rate) {
					return i;
				}
			}
			return startIndex;
		}
	}

	public Integer findClose(Integer zeros) {
		Integer close = 0;
		int i;
		for (i = zeros; i < endTimeIndex; i++) {
			if (Double.valueOf(dataMap.get("Treating Pressure").get(i)) < 250) {
				close = i - 2;
				break;
			}
		}
		return close == 0 ? zeros : close;
	}

	public HashMap<String, ArrayList<Integer>> getStageInputs() {
		HashMap<String, ArrayList<Integer>> inputsIndeces = new HashMap<>();
		int i;
		int ii = 0;

		System.out.println(inputs);
		for (String s : inputs.keySet()) {
			int start = getJobReset(10);
			ArrayList<Integer> c = new ArrayList<>();
			ii = 0;
			for (Double d : inputs.get(s)) {
				if (d < 0) {
					break;
				}
				if (ii > 0 && d < inputs.get(s).get(ii - 1)) {
					start = getJobReset(start);
				}
				for (i = start; i < endTimeIndex; i++) {
					if (Double.valueOf(this.dataMap.get("Clean Grand Total").get(i)) >= d) {
						c.add(i);
						start = i;
						break;
					}
				}
				ii++;
			}
			System.out.println("C-" + s + " - " + c);
			inputsIndeces.put(s, c);
		}
		return inputsIndeces;
	}

	public Integer getJobReset(Integer start) {
		Integer i;
		Integer resetIndex = 0;
		for (i = start; i < endTimeIndex; i++) {
			if (Double.valueOf(dataMap.get("Clean Grand Total").get(i)) < 100) {
				resetIndex = i;
				break;
			}
		}
		return resetIndex;
	}

	public ArrayList<Integer> findAcidGreen(ArrayList<String> slurryRate, ArrayList<String> treatingPressure) {
		boolean thisAcid = false;
		Boolean pressTest = false;
		ArrayList<Integer> stEndAcid = new ArrayList<>();
		HashMap<Boolean, Integer> pT = new HashMap<>();
		Double dP;
		double sumPressure = 0.0;
		int i;
		int ii;
		pT = findPressureTest(slurryRate, treatingPressure);
		for (Boolean p : pT.keySet()) {
			pressTest = p;
		}

		for (i = pT.get(pressTest); i < endTimeIndex; i++) {

			for (ii = i; ii < i + 5; ii++) {
				sumPressure = (Double.valueOf(treatingPressure.get(i + 1)) - Double.valueOf(treatingPressure.get(i)))
						+ sumPressure;
			}
			dP = sumPressure / Double.valueOf(6);
			if (Double.valueOf(slurryRate.get(i + 5)) < Double.valueOf(2) && dP > Double.valueOf(25)) {
				thisAcid = true;
				stEndAcid.add(i);
				break;
			} else if (Double.valueOf(slurryRate.get(i)) > Double.valueOf(10)) {
				thisAcid = false;
				break;
			}
			sumPressure = 0.0;
		}

		if (thisAcid) {
			for (i = stEndAcid.get(0); i < endTimeIndex; i++) {
				if (Double.valueOf(slurryRate.get(i)) >= Double.valueOf(20)) {
					stEndAcid.add(i);
					break;
				}
			}
		}
		return stEndAcid;
	}

	public ArrayList<Integer> findAcidRed(ArrayList<String> slurryRate, ArrayList<String> treatingPressure,
			ArrayList<String> propConc) {
		HashMap<Boolean, Integer> pT = findPressureTest(slurryRate, treatingPressure);
		Boolean pressTest = false;
		boolean thisAcid = false;
		Double slope = null;
		ArrayList<Integer> endAcid = new ArrayList<>();
		for (Boolean b : pT.keySet()) {
			pressTest = b;
		}
		int i;
		for (i = pT.get(pressTest); i < endTimeIndex; i++) {
			if (!thisAcid) {
				slope = getRateSlope(slurryRate, i);
			}
			if (slope < Double.valueOf(1) && Double.valueOf(slurryRate.get(i)) < Double.valueOf(25)
					&& Double.valueOf(propConc.get(i)) > Double.valueOf(.05) && !thisAcid) {
				thisAcid = true;
			}
			if (Double.valueOf(slurryRate.get(i)) > Double.valueOf(25) && thisAcid) {
				endAcid.add(i);
				break;
			} else if (Double.valueOf(slurryRate.get(i)) > Double.valueOf(25) && !thisAcid) {
				break;
			}
		}
		return endAcid;
	}

	public Double getRateSlope(ArrayList<String> slurryRate, Integer currentIndex) {
		Double slope = null;
		double sumSlopeRate = 0.0;
		int i;
		for (i = currentIndex; i < currentIndex + 10; i++) {
			sumSlopeRate = sumSlopeRate + (Double.valueOf(slurryRate.get(i + 1)) - Double.valueOf(slurryRate.get(i)));
		}
		slope = sumSlopeRate / Double.valueOf(10);
		return slope;
	}

	public void correctAcid() {
		int i = 0;
		if (!pumpedAcid & !stEndAcid.isEmpty()) {
			for (i = 0; i <= stEndAcid.size(); i++) {
				System.out.println("Removing End Acid: " + stEndAcid.get(0));
				stEndAcid.remove(0);
			}
		}
	}

	public HashMap<Boolean, Integer> findPressureTest(ArrayList<String> slurryRate,
			ArrayList<String> treatingPressure) {
		Boolean pressTest = false;
		Integer rateUp = 0;
		Integer start = 0;
		Double deltaPStart;
		HashMap<Boolean, Integer> pT = new HashMap<>();
		Integer i;

		for (i = 3; i < endTimeIndex; i++) {
			if (Double.valueOf(slurryRate.get(i)) < Double.valueOf(1) && Double.valueOf(treatingPressure.get(i)) > 7500
					&& !pressTest) {
				pressTest = true;
			} else if (Double.valueOf(slurryRate.get(i)) > Double.valueOf(10) && !pressTest) {
				if (Double.valueOf(slurryRate.get(i + 5)) < Double.valueOf(10)) {
					continue;
				}
				pressTest = false;
				rateUp = i;
				break;
			} else if (pressTest && Double.valueOf(slurryRate.get(i)) > Double.valueOf(10)) {
				rateUp = i;
				break;
			}
		}
		if (rateUp < 4) {
			pT.put(false, 4);
			return pT;
		}
		for (i = rateUp; i > 3; i--) {
			deltaPStart = (Double.valueOf(treatingPressure.get(i)) - Double.valueOf(treatingPressure.get(i - 1)))
					/ Double.valueOf(2);
			if (deltaPStart < 10 & deltaPStart > -10 && Double.valueOf(slurryRate.get(i)) < 1) {
				start = i;
				break;
			}
		}
		if (start == 0) {
			start = 2;
		}
		pT.put(pressTest, start);

		return pT;
	}

	public int findPumpsUpReturn(Integer aboveRate) {

		System.out.println("Pumps Up Start");
		int i;
		for (i = 1; i < endTimeIndex; i++) {

			if (Double.valueOf(dataMap.get(ChannelPane.SLURRY_RATE).get(i)) > Double.valueOf(aboveRate)
					&& Double.valueOf(dataMap.get(ChannelPane.TREATING_PRESSURE).get(i)) > Double.valueOf(2500)) {
				return i;
			}

		}
		return 1;

	}

	public ArrayList<Integer> correctShutdown(ArrayList<Integer> theIndArray,
			HashMap<String, ArrayList<Integer>> shutdowns) {
		int i;
		int ii = 0;
		ArrayList<Integer> removeIndex = new ArrayList<>();
		for (i = 0; i < theIndArray.size(); i++) {
			ii = 0;
			for (Integer shut : shutdowns.get("Shutdowns")) {
				if (Integer.valueOf(theIndArray.get(i)) > shut
						&& Integer.valueOf(theIndArray.get(i)) < Integer.valueOf(shutdowns.get("Resumes").get(ii))) {
					removeIndex.add(theIndArray.get(i));
				}
				ii++;
			}
		}
		return removeIndex;
	}

	private void removeShutdownBeforeOpen(HashMap<String, ArrayList<Integer>> shutdowns,
			HashMap<String, Integer> diagnosticMarkers) {
		ArrayList<Integer> removeIndex = new ArrayList<>();
		int i = 0;
		for (Integer shutdown : shutdowns.get("Shutdowns")) {
			if (shutdown < diagnosticMarkers.get("WellOpenTime")) {
				System.out.println("Removed: " + i);
				removeIndex.add(i);
			}
			i++;
		}
		int correct = 0;
		if (!removeIndex.isEmpty()) {
			for (Integer remove : removeIndex) {
				shutdowns.get("Shutdowns").remove(remove.intValue() - correct);
				shutdowns.get("Resumes").remove(remove.intValue() - correct);
				correct++;
			}
		}
		shutdowns.put("resumeSand",
				checkSandResumeArray(shutdowns.get("resumeSand"), diagnosticMarkers.get("WellOpenTime")));
		this.setShutdowns(shutdowns);
	}

	private ArrayList<Integer> checkSandResumeArray(ArrayList<Integer> sandResume, int wellOpen) {
		ArrayList<Integer> removeArray = new ArrayList<>();
		for (Integer i : sandResume) {
			if (i.intValue() < wellOpen) {
				removeArray.add(i);
			}
		}
		sandResume.removeAll(removeArray);
		return sandResume;
	}

	public synchronized void acidShutdownCorrection(HashMap<String, ArrayList<Integer>> shutdowns,
			ArrayList<Integer> stEndAcid) {
		int i = 0;
		int corrected = 0;
		ArrayList<Integer> removeIndex = new ArrayList<>();
		for (Integer a : shutdowns.get("Shutdowns")) {
			for (Integer b : stEndAcid) {
				if (a < b) {
					removeIndex.add(i - corrected);
					corrected++;
					break;
				}
			}
			i++;
		}
		for (Integer a : removeIndex) {
			shutdowns.get("Shutdowns").remove(a.intValue());
			shutdowns.get("Resumes").remove(a.intValue());
		}
		setShutdowns(shutdowns);
	}

	public Integer findPreFlush(ArrayList<String> targetPropCon, Integer lastIndex) {
		Collections.sort(theIndArray);
		int i;
		Integer preFlush = 0;
		for (i = lastIndex; i > 1; i--) {
			if (Double.valueOf(targetPropCon.get(i)) > Double.valueOf(0)) {
				preFlush = Integer.valueOf(i);
				break;
			}
		}
		return preFlushCleanDirty(preFlush, lastIndex);
	}

	public Integer preFlushCleanDirty(Integer prevFound, Integer lastIndex) {
		if (lastIndex - prevFound > 20) {
			return prevFound;
		}

		if (!dataMap.containsKey("Clean Rate")) {
			dataMap.put("Clean Rate", constructCleanRate(dataMap.get("Clean Grand Total")));
		}
		Integer preFlush = findPreFlush(dataMap.get("Slurry Rate"), dataMap.get("Clean Rate"), prevFound,
				theIndArray.size() < 3 ? theIndArray.size() - 1 : theIndArray.get(theIndArray.size() - 3));
		return lastIndex - preFlush > 20 ? preFlush : lastIndex - 20;
	}

	public static ArrayList<String> constructCleanRate(ArrayList<String> cleanTotal) {
		ArrayList<String> cleanRate = new ArrayList<>();
		int i = 0;
		for (String s : cleanTotal) {
			if (i == 0) {
				cleanRate.add(String.valueOf(Double.valueOf(s) * 60.0));
				i++;
				continue;
			}
			cleanRate.add(String.valueOf((Double.valueOf(s) - Double.valueOf(cleanTotal.get(i - 1))) * 60.0));
			i++;
		}
		return cleanRate;
	}

	public Integer findPreFlush(ArrayList<String> slurryRate, ArrayList<String> cleanRate, int start, int end) {
		Integer preFlush = start;
		if (checkIfCalcCleanRate(start)) {
			preFlush = (preFlush = findPreFlushCalcSlurry(slurryRate, start, end)) == start
					? findPreFlushCalcClean(cleanRate, start, end)
					: preFlush;
			return preFlush;
		}
		return findPreFlushCleanDirty(slurryRate, cleanRate, start, end);
	}

	private Integer findPreFlushCleanDirty(ArrayList<String> slurryRate, ArrayList<String> cleanRate, int start,
			int end) {
		for (int i = start; i > end; i--) {
			if (calculateCleanDirty(Double.valueOf(slurryRate.get(i)), Double.valueOf(cleanRate.get(i))) > 0.1) {
				System.out.println("Calc based on Clean Dirty -------------------------- " + i);
				return i;
			}
		}
		return start - 50;
	}

	private Double getLastRealSandConc(int start, int end) {
		return UserDefinedFrame
				.avg(UserDefinedFrame.getArrayWithinIndeces(dataMap.get("Prop. Concentration"), start, end));
	}

	private Integer findPreFlushCalcSlurry(ArrayList<String> slurryRate, int start, int end) {
		Double rateAtFlush = Double.valueOf(slurryRate.get(start));
		Double corrRate = getCalcSlurryRateCorr(rateAtFlush, start, end);
		for (int i = start; i > end; i--) {
			if (Double.valueOf(slurryRate.get(i)) <= corrRate) {
				System.out.println("Calc based on slurry ---------------------------- " + i);
				return i;
			}
		}
		return start;
	}

	private Double getCalcSlurryRateCorr(Double rateAtFlush, int start, int end) {
		Double propAtFlush = getLastRealSandConc(start, end);
		double propPerBarrel = propAtFlush * 42;
		return rateAtFlush - propPerBarrel / (2.65 * 8.33);
	}

	private Integer findPreFlushCalcClean(ArrayList<String> cleanRate, int start, int end) {
		System.out.println("Calc based on Clean ---------------------");
		return start - 50;
	}

	public Boolean checkIfCalcCleanRate(int start) {
		Double sandConcAtFlush = calculateCleanDirty(Double.valueOf(dataMap.get("Slurry Rate").get(start)),
				Double.valueOf(dataMap.get("Clean Rate").get(start)));
		return sandConcAtFlush > 0.1;
	}

	public Double calculateCleanDirty(Double slurryRate, Double cleanRate) {
		// BBL/MIN
		double diff = slurryRate - cleanRate;
		double density = 2.65 * 8.33;
		double sandConc = density * diff / 42;
		return sandConc;
	}

	public class PerfectShutdown implements Runnable {
		Boolean stCopyFound = false;
		private ExecutorService executor;
		private CompletableFuture<Integer> wellOpenCopy;
		private HashMap<String, Boolean> savedOptions;
		Thread t;

		PerfectShutdown(HashMap<String, Boolean> savedOptions) {
			this.executor = Executors.newCachedThreadPool();
			this.t = new Thread(this, "PerfectShutdown");
			this.wellOpenCopy = new CompletableFuture<>();
			this.savedOptions = savedOptions;
		}

		@Override
		public void run() {
			executor.execute(() -> getSandStartRun());
			if (!isWellOpenSet()) {
				executor.execute(() -> findWellOpen());
			} else {
				wellOpenCopy.completeAsync(() -> diagnostics.get("WellOpenTime"));
			}

			executor.execute(() -> {

				try {
					findPumpsUp(30);
				} catch (NumberFormatException | InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			});
			try {
				perfectMethod(savedOptions);
			} catch (InterruptedException | ExecutionException e) {
				try {
					TextLog textLog = new TextLog("Not so perfect..." + LocalDateTime.now());
				} catch (IOException e1) {
				}
			}
			executor.shutdown();
			try {
				executor.awaitTermination(2500, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public void getSandStartRun() {

			System.out.println("Get Sand Start Start");
			Integer count = 0;
			for (String a : dataMap.get("Prop. Concentration")) {
				if (Double.valueOf(a) > Double.valueOf(0.10)) {
					diagnostics.put("SandStartRate", count);
					System.out.println("Get Sand Start End");
					break;
				}
				count++;
			}
			System.out.println("Get Sand Start End 2");

		}

		public Boolean isWellOpenSet() {
			return false;
		}

		public void findWellOpen() {
			int i;
			int start = findPumpsUpReturn(15);
			System.out.println("Well Open Start");
			for (i = start; i > 0; i--) {
				if ((Double.valueOf(dataMap.get("Treating Pressure").get(i))
						- Double.valueOf(dataMap.get("Treating Pressure").get(i + 4))) / Double.valueOf(4) > -15
						& (Double.valueOf(dataMap.get("Treating Pressure").get(i))
								- Double.valueOf(dataMap.get("Treating Pressure").get(i + 4)))
								/ Double.valueOf(4) < 15) {
					diagnostics.remove("WellOpenTime");
					diagnostics.put("WellOpenTime", i);
					Integer temp = i;
					wellOpenCopy.completeAsync(() -> temp);
					System.out.println("Well Open End");
					break;
				}
			}
			if (!wellOpenCopy.isDone()) {
				diagnostics.put("WellOpenTime", start);
				wellOpenCopy.completeAsync(() -> start);
			}
			System.out.println("Well Open End 2");

		}

		public int findRateBelowBackwards(Double belowRate, int startInd) {
			for (int i = startInd; i > 0; i--) {
				if (Double.valueOf(dataMap.get("Slurry Rate").get(i)) < belowRate) {
					return i;
				}
			}
			return 1;
		}

		public Boolean checkSubsequentValues(int numValues, int start, Double floor, String mapKey) {
			for (int i = start; i < start + numValues; i++) {
				if (Double.valueOf(dataMap.get(mapKey).get(i)) < floor) {
					return false;
				}
			}
			return true;
		}

		public void findPumpsUp(Integer aboveRate)
				throws NumberFormatException, InterruptedException, ExecutionException {

			System.out.println("Pumps Up Start");
			int i;
			for (i = 1; i < endTimeIndex; i++) {
				if (Double.valueOf(dataMap.get("Slurry Rate").get(i)) > Double.valueOf(aboveRate)) {
					if (!checkSubsequentValues(10, i, Double.valueOf(aboveRate), "Slurry Rate")) {
						continue;
					}
					stMinMax = i;
					didIt = i;
					System.out.println("Pumps Up at Index: " + didIt);
					System.out.println("Pumps Up End");
					break;
				}

			}
			System.out.println("Pumps Up End 2");

		}

		/*
		 * public void executeQueue() {
		 *
		 * while(runQueue.peek()!=null) { if(runQueue.peek().keySet().contains(true)) {
		 * executor.execute(runQueue.poll().get(true)); } } }
		 */
		public void perfectMethod(HashMap<String, Boolean> savedOptions)
				throws InterruptedException, ExecutionException {
			int i;
			Integer openWellCopy = wellOpenCopy.get();
			open = openWellCopy;
			theIndArray.add(0, openWellCopy);
			System.out.println("Open Well Copy: " + openWellCopy);
			int lastIndex = 0;
			int rateUp = findPumpsUpReturn(5);
			for (i = rateUp; i < findClose(GetZeros.getZerosStatic(dataMap.get("Slurry Rate"), 15.0)); i++) {
				try {
					if (stageChannel.toUpperCase().contains("CLEAN") || stageChannel.toUpperCase().contains("TOTAL")) {
						if (Double.valueOf(dataMap.get("Stage Number").get(i)) < Double
								.valueOf(dataMap.get("Stage Number").get(i - 1))
								&& Float.valueOf(dataMap.get("Stage Number").get(i)) < 10f
								&& FracCalculations.checkSubsequentValues(dataMap.get("Stage Number"),
										Float.valueOf(dataMap.get("Stage Number").get(i - 1)), 20, i,
										FracCalculations.LESSER)) {
							theIndArray.add(i);
							lastIndex = i;
						}
					} else {
						if (Math.rint(Double.valueOf(dataMap.get("Stage Number").get(i))) != Math
								.rint(Double.valueOf(dataMap.get("Stage Number").get(i - 1)))
								&& Math.rint(Double.valueOf(dataMap.get("Stage Number").get(i + 2))) != Math
										.rint(Double.valueOf(dataMap.get("Stage Number").get(i - 1)))) {
							System.out.println(i);
							theIndArray.add(i);
							lastIndex = i;
						}
					}
				} catch (IndexOutOfBoundsException e) {

					break;
				}

			}

			if (savedOptions.get(mainFrame.PREFLUSH_OPTION)) {
				theIndArray.add(findPreFlush(dataMap.get("Prop. Concentration"), lastIndex));
			}
			setLastIndex(lastIndex);
			System.out.println("The Schedule Index Array: " + theIndArray);
			System.out.println("perfect thread done");
			doneSem.release();
		}

		public HashMap<String, ArrayList<Integer>> FindShutdown() throws InterruptedException {// ArrayList<ArrayList<String>>
																								// masArray, Integer
																								// slurryRate,Integer
																								// stMinMax,Integer
																								// treatPressure
			int i;
			int ii;
			Integer zeros = 0;
			Double shutdownPres = 0.0;
			ArrayList<Integer> shutdownArray = new ArrayList<>();
			ArrayList<Integer> resumeArray = new ArrayList<>();
			HashMap<String, ArrayList<Integer>> shutdownsMap = new HashMap<>();

			if (stMinMax < 2) {
				stMinMax = 2;
			}
			zeros = GetZeros.getZerosStatic(dataMap.get("Slurry Rate"), 25.0);

			for (i = findPumpsUpReturn(25); i < zeros; i++) {

				if (Long.valueOf((long) Math.floor(Double.valueOf(dataMap.get("Slurry Rate").get(i)))) < Long
						.valueOf(2)) {
					shutdownArray.add(i);
					shutdownPres = Double.valueOf(dataMap.get("Treating Pressure").get(i));
					System.out.println("Shutdown - " + i);
					System.out.println("Shutdown Pressure - " + shutdownPres);
					if (shutdownPres < Double.valueOf(1500.0) | shutdownPres > 7500) {
						shutdownPres = 1500.0;
					}
					System.out.println(zeros);
					for (ii = i; ii < zeros; ii++) {
						if (Long.valueOf(Math.round(Double.valueOf(dataMap.get("Slurry Rate").get(ii)))) > Long
								.valueOf(3)
								&& Double.valueOf(dataMap.get("Treating Pressure").get(ii)) > shutdownPres) {
							resumeArray.add(ii);
							System.out.println("Resume - " + ii);
							i = ii;
							break;
						} else if (ii == zeros - 1) {
							i = ii;
						}
					}
					if (resumeArray.size() == 0 || (resumeArray.get(resumeArray.size() - 1)
							- shutdownArray.get(shutdownArray.size() - 1)) < 120) {
						System.out.println("Removed - " + i + " - " + ii);
						if (resumeArray.size() != shutdownArray.size()) {
							shutdownArray.remove(shutdownArray.size() - 1);
						} else {
							resumeArray.remove(resumeArray.size() - 1);
							shutdownArray.remove(shutdownArray.size() - 1);
						}
					}
				}
			}
			shutdownsMap.put("Shutdowns", shutdownArray);
			shutdownsMap.put("Resumes", resumeArray);
			shutdownsMap.put("resumeSand", getResumeSandArray(shutdownsMap));
			return shutdownsMap;
		}

		private ArrayList<Integer> getResumeSandArray(HashMap<String, ArrayList<Integer>> shutdownResumes) {
			ArrayList<Integer> array = new ArrayList<>();
			int count = 1;
			Integer nextSand;
			for (Integer i : shutdownResumes.get("Resumes")) {
				if (count >= shutdownResumes.get("Shutdowns").size()) {
					nextSand = findResumeSand(i, endTimeIndex);
					if (nextSand != null) {
						array.add(nextSand);
					}
					return array;
				}
				nextSand = findResumeSand(i, shutdownResumes.get("Shutdowns").get(count));
				if (nextSand != null) {
					array.add(nextSand);
				}
				count++;
			}
			return array;
		}

		private Integer findResumeSand(Integer resumeIndex, Integer nextShutdown) {
			for (int i = resumeIndex; i < nextShutdown; i++) {
				if (Float.valueOf(dataMap.get("Prop. Concentration").get(i)) > 0.1f
						&& FracCalculations.checkSubsequentValues(dataMap.get("Prop. Concentration"), 0.1f, 5, i,
								FracCalculations.GREATER)) {
					return i;
				}
			}
			return null;
		}
	}

	public ArrayList<Integer> correctDoubleStage(ArrayList<Integer> array) {
		ArrayList<Integer> temp = new ArrayList<>();
		Integer count = 0;
		Collections.sort(array);
		ArrayList<Integer> inputIndeces = getInputIndeces(theIndArray);
		for (Integer i : array) {

			if (i > array.get(0) && i - array.get(count - 1) < 10 && !(stEndAcid.contains(i) | i == open)) {
				if (!inputIndeces.contains(count) && !inputIndeces.contains(count - 1) && !stEndAcid.contains(i)) {
					temp.add(i);
				} else if (!inputIndeces.contains(count - 1) & !stEndAcid.contains(array.get(count - 1))) {
					temp.add(array.get(count - 1));
				}
			}
			count++;
		}
		return temp;
	}

	public Integer findJobReset(ArrayList<String> cleanTotal) {
		int count = 1;
		int newCount = 1;
		for (String a : cleanTotal) {
			if (Double.valueOf(a) < Double.valueOf(250.0)) {
				newCount = count + 1;
				break;
			}
			count++;
		}
		return newCount;
	}

	public ArrayList<Integer> badReset(Integer jobReset, ArrayList<Integer> indArray, ArrayList<Integer> acidArray) {
		boolean badReset = false;
		ArrayList<Integer> indeces = new ArrayList<>();
		ArrayList<Integer> badResetArray = null;
		int i;
		for (i = 1; i < indArray.size(); i++) {
			if (jobReset > indArray.get(i) && !acidArray.contains(i)) {
				badReset = true;
				indeces.add(i);
			}
		}

		if (badReset) {
			badResetArray = getBadResetArray(jobReset, indArray, indeces);
			return badResetArray;
		} else {
			return indArray;
		}
	}

	public ArrayList<Integer> getBadResetArray(Integer jobReset, ArrayList<Integer> indArray,
			ArrayList<Integer> indeces) {
		ArrayList<Integer> badReset = new ArrayList<>();
		badReset.addAll(indArray);
		for (Integer i : indeces) {
			badReset.remove(i.intValue());
			badReset.add(i.intValue(), jobReset);
		}
		return badReset;
	}
}
