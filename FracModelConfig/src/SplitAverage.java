import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class SplitAverage<T> {
	private ArrayList<Integer> indArray;
	private ArrayList<Double> doubleData;
	private ArrayList<T> data;
	private ArrayList<Double> averages;
	private ArrayList<Double> subAverages;
	private HashMap<Integer, Double> averagesMap;
	private ExecutorService executor;
	private int index = 0;
	private Double roundMult;
	private int reduce;
	private Boolean sand;
	private Semaphore semaphore;
	private String name;
	private ArrayList<Integer> excludeArray;

	SplitAverage(ArrayList<Integer> indArray, ArrayList<T> data) throws InterruptedException {
		this.semaphore = new Semaphore(0);
		this.indArray = indArray;
		this.data = data;
		this.subAverages = new ArrayList<>();
		this.averagesMap = new HashMap<>();
		this.executor = Executors.newCachedThreadPool();
		this.roundMult = Double.valueOf(1);
		this.sand = false;
		this.reduce = 0;
		this.name = "";
		allAverages();
	}

	SplitAverage(ArrayList<Integer> indArray, ArrayList<T> data, String name) throws InterruptedException {
		this.semaphore = new Semaphore(0);
		this.indArray = indArray;
		this.data = data;
		this.subAverages = new ArrayList<>();
		this.averagesMap = new HashMap<>();
		this.executor = Executors.newCachedThreadPool();
		this.roundMult = Double.valueOf(1);
		this.sand = false;
		this.reduce = 0;
		this.name = name;
		allAverages();
	}

	SplitAverage(ArrayList<Integer> indArray, ArrayList<T> data, Double roundMult, Boolean sand)
			throws InterruptedException {
		this.semaphore = new Semaphore(0);
		this.indArray = indArray;
		this.data = data;
		this.subAverages = new ArrayList<>();
		this.averagesMap = new HashMap<>();
		this.executor = Executors.newCachedThreadPool();
		this.roundMult = roundMult;
		this.sand = sand;
		this.reduce = 10 * sand.compareTo(false);
		this.name = "";
		allAverages();
	}

	SplitAverage(ArrayList<Integer> indArray, ArrayList<T> data, Double roundMult, Boolean sand,
			ArrayList<Integer> excludeArray) throws InterruptedException {
		this.semaphore = new Semaphore(0);
		this.indArray = indArray;
		this.data = data;
		this.subAverages = new ArrayList<>();
		this.averagesMap = new HashMap<>();
		this.executor = Executors.newCachedThreadPool();
		this.roundMult = roundMult;
		this.sand = sand;
		this.reduce = 25 * sand.compareTo(false);
		this.name = "";
		this.excludeArray = excludeArray;
		allAverages();
	}

	public void allAverages() throws InterruptedException {

		ArrayList<ArrayList<Double>> subSets = splitData();
		System.out.println("Subsets size: " + subSets.size());
		Semaphore semaphore = new Semaphore(0);
		HashMap<Integer, Double> averagesMap = new HashMap<>();
		for (ArrayList<Double> aLD : subSets) {
			Integer i = getIndex();
			executor.execute(() -> {
				Double subAverage = calcAverage(aLD);
				addToMap(averagesMap,i,subAverage);
				semaphore.release();
			});
		}
		System.out.println("Semaphore waiting on " + subSets.size() + " Permits");
		semaphore.acquire(subSets.size());
		executor.shutdown();
		executor.awaitTermination(10000, TimeUnit.MILLISECONDS);
		setAverages(averagesMap);
	}
	private synchronized void addToMap(HashMap<Integer,Double> averagesMap,Integer key,Double value) {
		averagesMap.put(key, value);
	}
	private synchronized int getIndex() {
		return index++;
	}

	public Double calcAverage(ArrayList<Double> subSetData) {
		double tempDouble = 0.0;
		for (Double d : subSetData) {
			tempDouble = tempDouble + d;
		}
		double average = tempDouble / Double.valueOf(subSetData.size());
		System.out.println("Average: "+average);
		return average;
	}

	public synchronized ArrayList<ArrayList<Double>> splitData() {
		ArrayList<ArrayList<Double>> dataArrays = new ArrayList<>();
		int i;
		int ii;

		for (i = 0; i < indArray.size() - 1; i++) {
			ArrayList<Double> temp = new ArrayList<>();
			if (data.size() == 0) {
				temp.add(0.0);
				dataArrays.add(temp);
				continue;
			}
			for (ii = indArray.get(i); ii < indArray.get(i + 1) - (reduce>(indArray.get(i+1)-indArray.get(i))?0:reduce); ii++) {

				if (data.get(ii).equals("") || Double.valueOf(data.get(ii).toString()) == null) {
					temp.add(0.0);
				} else {
					temp.add(Double.valueOf(data.get(ii).toString()));
				}
			}
			dataArrays.add(temp);
		}
		return dataArrays;
	}

	public void setAverages(HashMap<Integer, Double> averagesMap) {
		ArrayList<Double> temp = new ArrayList<>();
		System.out.println("Averages Map: " + averagesMap);
		int i = 0;
		for (Integer index : averagesMap.keySet()) {
			int addAt = index > temp.size() ? temp.size() : index;
			if (sand && averagesMap.get(index) <= 0.0
					|| excludeArray != null && !excludeArray.isEmpty() & excludeArray.contains(index)) {
				temp.add(addAt, 0.0);
			} else {
				Double addToMap = FracCalculations.getDoubleRoundedDouble(
						roundMult != 1.0 ? Double.valueOf(Math.round(averagesMap.get(index) / roundMult)) * roundMult
								: averagesMap.get(index),
						2);
				temp.add(addAt, addToMap);
			}
		}
		this.averages = temp;
		semaphore.release();
	}

	public ArrayList<Double> getAverages() {
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
		}
		semaphore.release();
		return this.averages;
	}

	public ArrayList<String> getAveragesString() {
		ArrayList<String> temp = new ArrayList<>();
		for (Double d : getAverages()) {
			if (Double.valueOf(Math.round(d / roundMult)) * roundMult <= 0.0) {
				temp.add("");
			} else {
				temp.add(FracCalculations.getDoubleRoundedString(Double.valueOf(Math.round(d / roundMult) * roundMult),
						2));
			}
		}
		return temp;
	}
}
