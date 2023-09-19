import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class StageCleanTotal implements Runnable {
	private ArrayList<String> clTotal;
	private ArrayList<Integer> stageUp;
	private ArrayList<Double> clean = new ArrayList<>();
	private ArrayList<String> stageNames;
	private Long cleanGrand;
	private ArrayList<Double> acidAmt;
	private ArrayList<String> slurryRate;
	private TreatmentSummary constructTreatment;
	private Semaphore stageSandSem;

	public StageCleanTotal(ArrayList<String> clTotal, ArrayList<Integer> stageUp, ArrayList<String> stageNames,
			ArrayList<Double> acidAmt, ArrayList<String> slurryRate, TreatmentSummary constructTreatment,
			Semaphore stageSandSem) {

		this.slurryRate = slurryRate;
		this.clTotal = clTotal;
		this.stageUp = stageUp;
		this.stageNames = stageNames;
		this.acidAmt = acidAmt;
		this.cleanGrand = Long.valueOf(0);
		this.constructTreatment = constructTreatment;
		this.stageSandSem = stageSandSem;
	}

	@Override
	public void run() {

		Double saveClean = 0.0;
		Integer count = 0;
		Integer acidCount = 0;
		for (Integer i : stageUp) {

			if (count == 1) {
				if (count - 1 < stageNames.size() && stageNames.get(count - 1).toUpperCase().contains("ACID")) {
					Double acid = FracCalculations.getDoubleRoundedDouble(acidAmt.get(acidCount) / 42.0, 2);
					if (UserDefinedFrame.avg(
							UserDefinedFrame.getArrayWithinIndeces(slurryRate, stageUp.get(0), stageUp.get(1))) > 5.0) {
						saveClean += Double.valueOf(clTotal.get(stageUp.get(count))) - acid;
					} else {
						saveClean += Double.valueOf(clTotal.get(stageUp.get(count)));
					}
					clean.add(acid);
					acidCount++;
				} else {
					clean.add(saveClean < 0.0 ? 0.0 : saveClean + Double.valueOf(clTotal.get(stageUp.get(count))));
					if (clean.get(clean.size() - 1) < Double.valueOf(1)) {
						clean.remove(clean.size() - 1);
						clean.add(Double.valueOf(1));
					}
				}

			} else if (count == 0) {

			} else {

				if (count - 1 < stageNames.size() && stageNames.get(count - 1).toUpperCase().contains("ACID")) {

					Double acid = FracCalculations.getDoubleRoundedDouble(acidAmt.get(acidCount) / 42.0, 2);
					if (UserDefinedFrame.avg(UserDefinedFrame.getArrayWithinIndeces(slurryRate, stageUp.get(count - 1),
							stageUp.get(count))) > 5.0) {
						saveClean = saveClean + Double.valueOf(clTotal.get(stageUp.get(count)))
								- Double.valueOf(clTotal.get(stageUp.get(count - 1))) - acid;
					} else {
						saveClean = saveClean + Double.valueOf(clTotal.get(stageUp.get(count)))
								- Double.valueOf(clTotal.get(stageUp.get(count - 1)));
					}
					clean.add(acid);
					count++;
					acidCount++;
					continue;
				}

				saveClean = checkCleanAdd(Double.valueOf(clTotal.get(stageUp.get(count)))
						- Double.valueOf(clTotal.get(stageUp.get(count - 1))), saveClean);

			}
			if (count == stageUp.size() - 1) {
				break;
			}
			count++;
		}
		count = 0;

		ArrayList<String> cleanString = new ArrayList<>();
		for (Double j : clean) {
			if (count < stageNames.size() && stageNames.get(count).toUpperCase().contains("ACID")) {
				System.out.println("The Name Array contains Acid");
				cleanString.add(String.valueOf(Math.round(j * Double.valueOf(100)) / Double.valueOf(100)));
				cleanGrand = cleanGrand + Math.round(j);
			} else {
				cleanString.add(String.valueOf(Math.round(j)));
				cleanGrand = cleanGrand + Math.round(j);
			}

			count++;
		}
		System.out.println("End cleanTotal Thread/Start constructTreatment");
		constructTreatment.appendToMap(6, cleanString);
		System.out.println("End cleanTotal Thread/End constructTreatment - 3");
		stageSandSem.release();

	}

	public Double checkCleanAdd(Double addDouble, Double saveClean) {
		if (addDouble + saveClean < 0.0) {
			clean.add(1.0);
			return saveClean -= 1.0;
		}
		clean.add(addDouble + saveClean);
		return 0.0;
	}

	public Queue<Integer> findResets() {
		Queue<Integer> queue = new LinkedList<>();
		for (int i = stageUp.get(0); i < clTotal.size() - 1; i++) {
			if (i + 1 == clTotal.size()) {
				break;
			}
			if (Double.valueOf(clTotal.get(i + 1)) + 5.0 < Double.valueOf(clTotal.get(i))
					& !checkSubsequentValues(i + 1, 5, Double.valueOf(clTotal.get(i))) & !checkNegative(i)) {
				queue.offer(i);
			}

		}
		System.out.println("Resets Queue: " + queue);
		return queue;
	}

	private boolean checkNegative(int i) {
		if (Double.valueOf(clTotal.get(i)) < 0.0) {
			return true;
		}
		return false;
	}

	private boolean checkSubsequentValues(int count, int numIndices, Double value) {
		for (int i = 1; i < numIndices; i++) {
			if (count + i >= clTotal.size() - 1) {
				return true;
			}
			if (Double.valueOf(clTotal.get(count + i)) > value) {
				return false;
			}
		}
		return true;
	}

	public ArrayList<Double> getClean() {
		return this.clean;
	}

	public Long getCleanGrand() {
		int i = 0;
		while (i < 10 & cleanGrand == 0) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
			i++;
		}
		return cleanGrand;
	}
}
