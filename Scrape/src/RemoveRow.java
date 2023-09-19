import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTable;

public class RemoveRow extends TreatmentSummary {
	final int START = 0;
	final int END = 1;

	RemoveRow(HashMap<Integer, ArrayList<String>> treatmentSummary) {
		super(treatmentSummary);

	}

	public RemoveRow() {
	}

	public void deleteRow(Integer row) {
		this.remove(row);
	}

	public void addColumn(Integer row, Integer... column) {
		for (Integer c : column) {
			double temp1 = Double.parseDouble(treatmentSummary.get(c).get(row).toString().equals("") ? "0"
					: treatmentSummary.get(c).get(row).toString());
			double temp2 = Double.parseDouble(treatmentSummary.get(c).get(row + 1).toString().equals("") ? "0"
					: treatmentSummary.get(c).get(row + 1).toString());
			this.ratifyTreatmentSummary(c, row + 1, String.valueOf(temp1 + temp2));
		}
	}

	public void addColumnUp(Integer row, Integer... column) {
		if(row==0) {
			return;
		}
		for (Integer c : column) {
			double temp1 = Double.parseDouble(treatmentSummary.get(c).get(row).toString().equals("") ? "0"
					: treatmentSummary.get(c).get(row).toString());
			double temp2 = Double.parseDouble(treatmentSummary.get(c).get(row - 1).toString().equals("") ? "0"
					: treatmentSummary.get(c).get(row - 1).toString());
			this.ratifyTreatmentSummary(c, row - 1, String.valueOf(temp1 + temp2));
		}
	}

	@Deprecated
	public void moveRows(Integer row, Integer lastRow) {
		ArrayList<Integer> rows = new ArrayList<>();
	}

	@Deprecated
	public static Integer findLastRow(JTable table) {
		Integer i = 0;
		while (table.getValueAt(i, 0) != null && String.valueOf(table.getValueAt(i, 0)) != "") {
			i++;
		}
		Integer lastRow = i;
		return lastRow;
	}

	public ArrayList<Integer> findShutdownRows() {
		ArrayList<Integer> shutRows = new ArrayList<>();
		int i = 0;
		for (String a : treatmentSummary.get(12)) {
			if (a == "SHUTDOWN") {
				shutRows.add(i);
			}
			i++;
		}
		return shutRows;
	}

	public void checkFlushVolume() {
		if (Double.valueOf(treatmentSummary.get(6).get(treatmentSummary.get(6).size() - 1)) < 5.0) {
			fixFlushSubStage();
		}
	}

	private void fixFlushSubStage() {
		String endTime = getArray(1).get(getArray(1).size() - 1);
		String endDate = getArray(4).get(getArray(4).size() - 1);
		removeRow(getArray(6).size() - 1, "LASTROW");
		checkPreFlushAdd();
		getArray(12).remove(getArray(12).size() - 1);
		getArray(12).add("FLUSH");
		fixValue(1, getArray(1).size() - 1, endTime);
		fixValue(4, getArray(4).size() - 1, endDate);
	}

	private void checkPreFlushAdd() {
		if (getArray(12).contains("PRE-FLUSH")) {
			getArray(12).remove(getArray(12).size() - 2);
			getArray(12).add(getArray(12).size() - 1, "PRE-FLUSH");
		}
	}

	private void findResumeIndeces() {
		ArrayList<Integer> resumeIndeces = new ArrayList<>();
		Integer index = 0;
		for (String s : treatmentSummary.get(12)) {
			if (s.equals("RESUME")) {
				resumeIndeces.add(index);
			}
			index++;
		}
		removePropConc(resumeIndeces);
	}

	private ArrayList<Integer> findResumeIndecesReturn() {
		ArrayList<Integer> resumeIndeces = new ArrayList<>();
		Integer index = 0;
		for (String s : treatmentSummary.get(12)) {
			if (s.equals("RESUME")) {
				resumeIndeces.add(index);
			}
			index++;
		}
		return resumeIndeces;
	}

	public void addSweepAfterResume() {
		ArrayList<Integer> resumeIndeces = findResumeIndecesReturn();
		int correction = 0;
		if (!resumeIndeces.isEmpty()) {

			for (Integer i : resumeIndeces) {
				if (getArray(12).get(i.intValue() + 1).equals("SWEEP")) {
					distributeShutdownValues(i.intValue() + correction, false);
					continue;
				}
				for (int ii = 0; ii <= 12; ii++) {
					if (ii == 12) {
						this.getArray(ii).add(i.intValue() + correction + 1, "SWEEP");
					} else {
						this.getArray(ii).add(i.intValue() + correction + 1,
								this.getArray(ii).get(i.intValue() + correction));
					}
				}
				distributeShutdownValues(i.intValue() + correction, true);
				correction++;
			}
		}
	}

	private void distributeShutdownValues(int resumeIndex, Boolean createSweep) {
		if(resumeIndex==0) {
			return;
		}
		addColumnUp(resumeIndex - 1, 6, 9);
		if (!createSweep) {
			addColumn(resumeIndex, 6, 9);
		}
		clearColumns(resumeIndex, 5, 6, 7, 8, 9);
		clearColumns(resumeIndex - 1, 5, 6, 7, 8, 9);
		fixShutdownTime(resumeIndex - 1);
		fixShutdownTime(resumeIndex);
	}

	private void fixShutdownTime(int row) {
		getArray(1).remove(row);
		getArray(1).add(row, getArray(0).get(row));
	}

	private void clearColumns(int row, int... columns) {
		for (int c : columns) {
			this.getArray(c).remove(row);
			this.getArray(c).add(row, "");
		}
	}

	public synchronized void fixSandConc(String... names) {
		for (String s : names) {
			int i = 0;
			for (String s2 : this.getArray(12)) {
				if (s2.equals(s) && !this.getArray(5).get(i).equals("")) {
					this.getArray(5).remove(i);
					this.getArray(5).add(i, "");
					break;
				} else if (s2.equals(s)) {
					break;
				}
				i++;
			}
		}
	}

	public static String fixUserDefinedSand(ArrayList<String> namesArray, String commaArray) {
		int subsBeforeSlurry = getNumStagesBeforeSand(namesArray);
		int subsBeforeSand = getNumStagesBeforeSand(commaArray);
		int start = subsBeforeSand - subsBeforeSlurry;
		String newCommaArray = "";
		commaArray = addCleanStagesToCommaArray(commaArray, start);
		start = start < 0 ? 0 : start;
		for (int i = start; i < commaArray.split(",").length; i++) {
			newCommaArray += "," + commaArray.split(",")[i];
		}
		System.out.println("NEW COMMA ARRAY: " + newCommaArray);
		return newCommaArray.substring(1);
	}
	public static String fixUserDefinedSandNumStages(ArrayList<String> namesArray, String commaArray) {
		int commaArrSize = getCommaArraySize(commaArray);
		int arrSize = namesArray.size();
		int start = arrSize-commaArrSize;
		String newCommaArray = "";
		commaArray = addCleanStagesToCommaArray(commaArray, start);
		start = start < 0 ? 0 : start;
		for (int i = start; i < commaArray.split(",").length; i++) {
			newCommaArray += "," + commaArray.split(",")[i];
		}
		System.out.println("NEW COMMA ARRAY: " + newCommaArray);
		return newCommaArray.substring(1);
	}
	private static int getCommaArraySize(String commaArray) {
		return commaArray.split(",").length;
	}

	private static String addCleanStagesToCommaArray(String commaArray, int start) {
		String addString = "";
		while (start < 0) {
			addString += "0.0,";
			start++;
		}
		return addString + commaArray;
	}

	public static int getNumStagesBeforeSand(ArrayList<String> namesArray) {
		int i = 0;
		for (String s : namesArray) {
			if (s.equals("SLURRY")) {
				break;
			}
			i++;
		}
		return i;
	}

	public static int getNumStagesBeforeSand(String commaArray) {
		int i = 0;
		for (String s : commaArray.split(",")) {
			Matcher matcher = Pattern.compile("[\\d\\.]+").matcher(s);
			if (matcher.find() && Double.valueOf(matcher.group()) > 0.0) {
				return i;
			}
			i++;
		}
		return i;
	}

	public void checkForPadAndAdd() {
		if (treatmentSummary.get(12).contains("PAD")) {
			return;
		}
		int firstSweep = findFirstSweep();
		int i = 0;
		while (firstSweep + i < treatmentSummary.get(12).size()
				&& treatmentSummary.get(12).get(firstSweep + i).toUpperCase().equals("SWEEP")) {
			i++;
		}
		int pad = firstSweep + i;
		treatmentSummary.get(12).remove(pad);
		treatmentSummary.get(12).add(pad, "PAD");
	}

	private int findFirstSweep() {
		int i = 0;
		for (String s : treatmentSummary.get(12)) {
			if (s.toUpperCase().equals("SWEEP")) {
				return i;
			}
			i++;
		}
		return 0;
	}

	public void fixPreSandStageProgression(Boolean pumpedAcid, ArrayList<Integer> stageInputs) {
		checkForPadAndAdd();
		boolean corrected = false;
		Boolean atPad = false;
		Double carrySand = 0.0;
		System.out.println(treatmentSummary);
		int i = 0;

		while ((i < treatmentSummary.get(12).size()) && (!atPad || !treatmentSummary.get(12).get(i).equals("SLURRY"))) {

			if (stageInputs.contains(i)) {
				atPad = !atPad ? getArray(12).get(i).equals("PAD") : atPad;
				i++;
				continue;
			}
			if (getArray(12).get(i).equals("SWEEP") && getArray(12).get(i + 1).toUpperCase().contains("ACID")
					&& i != 0) {
				removeRow(i, "UP");
				fixIndexArray(stageInputs);
				corrected = true;
				System.out.println(treatmentSummary);
				continue;
			} else if (i > 0 && getArray(12).get(i).equals("RESUME") & !getArray(12).get(i - 1).equals("SHUTDOWN")) {
				removeRow(i, "DOWN");
				fixIndexArray(stageInputs);
				continue;
			} else if (getArray(12).get(i).equals("SWEEP")) {
				if (i != 0 && getArray(12).get(i - 1).equals("PAD")) {
					averageRows(6, 7, i, "UP");
					averageRows(6, 8, i, "UP");
					removeRow(i, "UP");
				} else if (i != 0 && !getArray(12).get(i - 1).equals("RESUME")) {
					removeRow(i, "DOWN");
				} else if (i == 0) {
					removeRow(i, "DOWN");
				} else {
					i++;
				}
				fixIndexArray(stageInputs);
				corrected = true;
				continue;
			} else if (getArray(12).get(i).equals("SLURRY")) {
				carrySand += !getArray(11).get(i).equals("") ? Double.valueOf(getArray(11).get(i)) : 0;
				if (this.getArray(12).get(i + 1).toUpperCase().contains("ACID") && i != 0) {
					removeRow(i, "UP");
				} else {
					removeRow(i, "DOWN");
				}
				fixIndexArray(stageInputs);
				corrected = true;
				continue;
			}
			if (getArray(12).get(i).equals("PAD")) {
				atPad = true;
			}
			i++;

		}
		if (corrected) {
			System.out.println(i);
			correctionsAfterCorrected(i, carrySand);
		}
		System.out.println("End of 'Fix Sand Progression': " + treatmentSummary);

	}

	private void averageRows(Integer weightCol, Integer col, Integer row, String upDown) {
		switch (upDown) {
		case ("UP"):
			double keepRowValue = Double.parseDouble(
					treatmentSummary.get(weightCol).get(row - 1) != "" ? treatmentSummary.get(weightCol).get(row - 1)
							: "0.0");
			double removeRowValue = Double.parseDouble(
					treatmentSummary.get(weightCol).get(row) != "" ? treatmentSummary.get(weightCol).get(row) : "0.0");
			double keepRowWeight = keepRowValue / (keepRowValue + removeRowValue);
			if (!treatmentSummary.get(col).get(row - 1).equals("") && !treatmentSummary.get(col).get(row).equals("")) {
				Double weightedAverageValue = (Double.valueOf(treatmentSummary.get(col).get(row - 1)) * keepRowWeight)
						+ (Double.valueOf(treatmentSummary.get(col).get(row)) * (1 - keepRowWeight));
				treatmentSummary.get(col).remove(row - 1);
				treatmentSummary.get(col).add(row - 1, String.valueOf(Math.rint(weightedAverageValue)));
			}

			break;
		case ("DOWN"):
			double keepRowValue1 = Double.parseDouble(treatmentSummary.get(weightCol).get(row + 1));
			double removeRowValue1 = Double.parseDouble(treatmentSummary.get(weightCol).get(row));
			double keepRowWeight1 = keepRowValue1 / (keepRowValue1 + removeRowValue1);
			Double weightedAverageValue1 = (Double.valueOf(treatmentSummary.get(col).get(row + 1)) * keepRowWeight1)
					+ (Double.valueOf(treatmentSummary.get(col).get(row)) * (1 - keepRowWeight1));
			treatmentSummary.get(col).remove(row + 1);
			treatmentSummary.get(col).add(row + 1, String.valueOf(Math.rint(weightedAverageValue1)));
			break;
		}
	}

	private void correctionsAfterCorrected(int firstSandSubStage, Double carrySand) {
		if(firstSandSubStage==treatmentSummary.get(11).size()) {
			return;
		}
		ArrayList<Integer> subBeforeSand = new ArrayList<>();
		for (int i = 0; i < firstSandSubStage; i++) {
			subBeforeSand.add(i);
		}
		removePropConc(subBeforeSand);

		Double firstSand = carrySand + checkEmptyString(11, firstSandSubStage);
		treatmentSummary.get(11).remove(firstSandSubStage);
		treatmentSummary.get(11).add(firstSandSubStage, String.valueOf(firstSand));
		correctSubStageIndex(2);
	}

	private Double checkEmptyString(int column, int row) {
		System.out.println("column in map: " + column + " - row in map: " + row);
		System.out.println("The Array in Column " + column + "=" + treatmentSummary.get(column));
		if (row >= treatmentSummary.get(column).size() || treatmentSummary.get(column).get(row).equals("")) {
			return 0.0;
		} else {
			return Double.valueOf(treatmentSummary.get(column).get(row));
		}
	}

	private void fixIndexArray(ArrayList<Integer> stageInputs) {
		ArrayList<Integer> copyStageInputs = new ArrayList<>();
		copyStageInputs.addAll(stageInputs);
		for (Integer integer : copyStageInputs) {
			stageInputs.remove(integer);
			stageInputs.add(integer - 1);
		}
	}

	private void fixTimes(int rowToRemove) {
		this.getArray(0).remove(rowToRemove + 1);
		this.getArray(0).add(rowToRemove + 1, this.getArray(0).get(rowToRemove));
	}

	private void fixTimes(int rowToRemove, Boolean lastRow) {
		this.getArray(3).remove(rowToRemove + 1);
		this.getArray(3).add(rowToRemove + 1, this.getArray(3).get(rowToRemove));
	}

	private Boolean checkForSand() {
		for (String s : getArray(12)) {
			if (!s.equals("")) {
				return true;
			}
		}
		return false;
	}

	public void removeRow(int rowToRemove, String upDown) {
		switch (upDown) {
		case ("UP"):
			addColumnUp(rowToRemove, 6, 9);
			fixTimes(rowToRemove);
			deleteRow(rowToRemove);
			break;
		case ("DOWN"):
			addColumn(rowToRemove, 6, 9);
			fixTimes(rowToRemove);
			deleteRow(rowToRemove);
			break;
		case ("LASTROW"):
			addColumn(rowToRemove - 1, 6, 9);
			fixTimes(rowToRemove - 1, true);
			deleteRow(rowToRemove);
		}
	}

	private void removePropConc(ArrayList<Integer> resumeIndeces) {
		if (resumeIndeces.isEmpty()) {
			return;
		}
		for (Integer i : resumeIndeces) {
			treatmentSummary.get(5).remove(i.intValue());
			treatmentSummary.get(5).add(i, "");
		}
	}

	public void findResumes() {
		findResumeIndeces();
		Collections.replaceAll(this.treatmentSummary.get(12), "RESUME", "SWEEP");
	}

}
