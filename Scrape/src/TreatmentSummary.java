import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import javax.swing.JTable;

public class TreatmentSummary {
	protected HashMap<Integer, ArrayList<String>> treatmentSummary;
	Semaphore semaphore;
	Integer countdown;

	TreatmentSummary() {
		this.countdown = 13;
		this.semaphore = new Semaphore(0);
		this.treatmentSummary = new HashMap<>();
	}

	TreatmentSummary(Integer countdown) {
		this.treatmentSummary = new HashMap<>();
		this.semaphore = new Semaphore(0);
		this.countdown = countdown;
	}

	TreatmentSummary(HashMap<Integer, ArrayList<String>> treatmentSummary) {
		this.treatmentSummary = treatmentSummary;
	}

	public synchronized void appendToMap(int index, ArrayList<String> array) {
		this.treatmentSummary.put(index, array);
		wildToStringArray(index, array);
		if (semaphore != null) {
			semaphore.release();
		}
	}

	public synchronized ArrayList<String> wildToStringArray(Integer index, ArrayList<String> array) {
		System.out.println(index + " - Array: " + array);
		System.out.println("The size of this array (" + index + "): " + array.size() + " - With the last value of: "
				+ array.get(array.size() - 1));
		return array;
	}

	public synchronized String getSlurryAverage(Integer dataColumn) {
		Integer i = 0;
		Long totalSeconds = Long.valueOf(0);
		ArrayList<Long> weights = new ArrayList<>();
		ArrayList<Double> values = new ArrayList<>();
		for (String s : treatmentSummary.get(12)) {
			if (s.equals("SLURRY")) {
				Long tempDuration = getSubStageSeconds(i);
				totalSeconds += tempDuration;
				weights.add(tempDuration);
				values.add(Double.valueOf((i >= treatmentSummary.get(dataColumn).size()
						|| treatmentSummary.get(dataColumn).get(i).equals("")) ? "0.0"
								: treatmentSummary.get(dataColumn).get(i)));
			}
			i++;
		}
		return FracCalculations.getDoubleRoundedString(getWeightedAverage(totalSeconds, weights, values), 0);
	}

	public void checkFixTimes() {
		ArrayList<String> startTimes = treatmentSummary.get(0);
		ArrayList<String> endTimes = treatmentSummary.get(1);
		if (startTimes.size() != endTimes.size()) {
			return;
		}
		for (int i = 0; i < startTimes.size(); i++) {
			if (treatmentSummary.get(12).get(i).equals("SHUTDOWN") | treatmentSummary.get(12).get(i).equals("RESUME")) {
				continue;
			}
			if (checkTimeEquity(treatmentSummary.get(0).get(i), treatmentSummary.get(1).get(i))) {
				System.out.println("Fixed Time at: " + i);
				fixStartEndTime(i);
			}
		}
	}

	private void fixStartEndTime(int endTimeInd) {
		if (endTimeInd + 1 >= treatmentSummary.get(0).size()) {
			return;
		}
		LocalDateTime endTime = getDateTimeFromIndex(endTimeInd, ArrayIndeces.END).plusMinutes(1);
		String time = endTime.format(DateTimeFormatter.ofPattern("HH:mm"));
		String date = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

		/// ADJUSTING END TIME AND END DATE
		fixValue(1, endTimeInd, time);
		fixValue(4, endTimeInd, date);

		// ADJUSTING THE PROCEDING START TIME AND START DATE
		fixValue(0, endTimeInd + 1, time);
		fixValue(3, endTimeInd + 1, date);
	}

	private LocalDateTime getDateTimeFromIndex(int index, ArrayIndeces startEnd) {
		switch (startEnd) {
		case START:
			return constructStartDateTimeFromIndex(index);
		case END:
			return constructEndDateTimeFromIndex(index);
		default:
			return null;
		}
	}

	private LocalDateTime constructStartDateTimeFromIndex(int i) {
		if (i >= treatmentSummary.get(0).size()) {
			return null;
		}
		LocalDateTime dateTime = LocalDateTime
				.parse(treatmentSummary.get(3).get(i) + "T" + treatmentSummary.get(0).get(i));
		return dateTime;
	}

	private LocalDateTime constructEndDateTimeFromIndex(int i) {
		if (i >= treatmentSummary.get(0).size()) {
			return null;
		}
		LocalDateTime dateTime = LocalDateTime
				.parse(treatmentSummary.get(4).get(i) + "T" + treatmentSummary.get(1).get(i));
		return dateTime;
	}

	public void fixValue(int arrayIndex, int index, String value) {
		if (index >= treatmentSummary.get(0).size()) {
			return;
		}
		treatmentSummary.get(arrayIndex).remove(index);
		treatmentSummary.get(arrayIndex).add(index, value);
	}

	private Boolean checkTimeEquity(String startTime, String endTime) {
		return startTime.equals(endTime);
	}

	private Double getWeightedAverage(Long totalSeconds, ArrayList<Long> weights, ArrayList<Double> values) {
		double average = 0.0;
		int i = 0;
		for (Long l : weights) {
			double weight = Double.valueOf(l) / Double.valueOf(totalSeconds);
			average += weight * values.get(i);
			i++;
		}
		return average;
	}

	private Long getSubStageSeconds(Integer index) {
		LocalDateTime start = LocalDateTime
				.parse(treatmentSummary.get(3).get(index) + "T" + treatmentSummary.get(0).get(index));
		LocalDateTime end = LocalDateTime
				.parse(treatmentSummary.get(4).get(index) + "T" + treatmentSummary.get(1).get(index));
		Long seconds = Duration.between(start, end).getSeconds();
		return seconds;
	}

	public synchronized void writeToTable(JTable table) {
		int i;
		acquire();
		// CHECK AND ADJUST DATES/TIMES FOR SUBSTAGES WITH SAME START AND END TIME
		checkFixTimes();

		// WRITE MAP TO TABLE
		for (i = 0; i < this.treatmentSummary.get(0).size(); i++) {
			for (Integer col : treatmentSummary.keySet()) {
				table.setValueAt(treatmentSummary.get(col).get(i), i, col);
			}
		}
	}


	public void acquire() {
		if (semaphore != null) {
			try {
				System.out.print("The number to countdown from: " + countdown + " - ");
				System.out.println("The number of available permits before: " + semaphore.availablePermits());
				semaphore.acquire(countdown);

			} catch (InterruptedException e) {
				System.out.println("TreatmentSummary acquire() Interrupted");
			}
		}
	}

	public void removeAll() {
		ArrayList<Integer> keys = new ArrayList<>();
		keys.addAll(this.treatmentSummary.keySet());
		for (Integer a : keys) {
			this.treatmentSummary.remove(a);
		}
	}

	public synchronized void addAll(TreatmentSummary newTreatmentSummary) {
		treatmentSummary = getMap();
		this.treatmentSummary.putAll(newTreatmentSummary.getMap());
		setMap(treatmentSummary);
	}

	public synchronized void correctSubStageIndex(Integer key) {
		treatmentSummary.remove(key);
		ArrayList<String> tempArray = new ArrayList<>();
		int i;
		for (i = 1; i <= componentSize(); i++) {
			tempArray.add(String.valueOf(i));
		}
		this.appendToMap(key, tempArray);
	}

	public synchronized void constructSubStageIndex(Integer key) {
		ArrayList<String> tempArray = new ArrayList<>();
		int i;
		for (i = 1; i <= componentSize(); i++) {
			tempArray.add(String.valueOf(i));
		}
		this.appendToMap(key, tempArray);
	}

	public synchronized ArrayList<String> getArray(Integer key) {
		return this.getMap().get(key);
	}

	public synchronized void ratifyTreatmentSummary(Integer key, int index, String newValue) {
		treatmentSummary = getMap();
		treatmentSummary.get(key).remove(index);
		treatmentSummary.get(key).add(index, newValue);
		this.setMap(treatmentSummary);
	}

	public synchronized Integer size() {
		return this.getMap().size();
	}

	public synchronized Integer componentSize() {
		return this.getMap().get(0).size();
	}

	public synchronized void remove(int index) {
		treatmentSummary = getMap();
		// System.out.println(treatmentSummary.get(6).size());
		for (Integer a : getMap().keySet()) {
			if (treatmentSummary.get(a).size() - 1 < index) {
				continue;
			}
			treatmentSummary.get(a).remove(index);
		}

		this.setMap(treatmentSummary);
	}

	public synchronized HashMap<Integer, ArrayList<String>> getMap() {
		return this.treatmentSummary;
	}

	public synchronized void setMap(HashMap<Integer, ArrayList<String>> newMap) {
		this.treatmentSummary = newMap;
	}

	public static enum ArrayIndeces {
		START_T(0), END_T(1), SUBSTAGE(2), START_D(3), END_D(4), PROP_CON(5), CLEAN(6), PRESSURE(7), RATE(8), SLURRY(9),
		SAND_TYPE(10), SAND_VOL(11), STAGE_NAME(12), START(100), END(200);

		final int id;

		ArrayIndeces(int id) {
			this.id = id;
		}

	}
}
