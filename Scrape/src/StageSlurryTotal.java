import java.util.ArrayList;

public class StageSlurryTotal extends Thread {

	private ArrayList<String> slTotal;
	private ArrayList<Integer> stageUp;
	private ArrayList<String> stageNames;
	private Long slurryGrand;
	private ArrayList<Double> acidAmt;
	private ArrayList<String> slurryRate;
	private Boolean finished;
	private TreatmentSummary constructTreatment;
	ArrayList<Double> slurr;

	public StageSlurryTotal(ArrayList<String> slTotal, ArrayList<Integer> stageUp, ArrayList<String> stageNames,
			ArrayList<Double> acidAmt, ArrayList<String> slurryRate, TreatmentSummary constructTreatment) {

		this.slurryRate = slurryRate;
		this.slTotal = slTotal;
		this.stageUp = stageUp;
		this.stageNames = stageNames;
		this.acidAmt = acidAmt;
		this.slurryGrand = Long.valueOf(0);
		this.constructTreatment = constructTreatment;
		this.slurr = new ArrayList<>();
	}

	@Override
	public synchronized void run() {
		System.out.println("Start slurryTotal Thread");
		finished = false;

		Double saveSlurr = 0.0;
		Integer count = 0;
		Integer acidCount = 0;
		for (Integer i : stageUp) {
			if (count == 0) {
				count++;
				continue;
			} else if (count == 1) {
				if (count - 1 < stageNames.size() && stageNames.get(count - 1).toUpperCase().contains("ACID")) {
					Double acid = FracCalculations.getDoubleRoundedDouble(acidAmt.get(acidCount) / 42.0, 2);
					if (UserDefinedFrame.avg(UserDefinedFrame.getArrayWithinIndeces(slurryRate, stageUp.get(count - 1),
							stageUp.get(count))) > 5.0) {
						saveSlurr = saveSlurr + Double.valueOf(slTotal.get(stageUp.get(count))) - acid;
					} else {
						saveSlurr = saveSlurr + Double.valueOf(slTotal.get(stageUp.get(count)));
					}
					slurr.add(acid);
					acidCount++;
				} else {
					slurr.add(Double.valueOf(slTotal.get(stageUp.get(count))));
					if (slurr.get(slurr.size() - 1) == Double.valueOf(0)) {
						slurr.remove(slurr.size() - 1);
						slurr.add(Double.valueOf(1));
					}
				}

			} else {
				if (count - 1 < stageNames.size() && stageNames.get(count - 1).toUpperCase().contains("ACID")) {
					Double acid = FracCalculations.getDoubleRoundedDouble(acidAmt.get(acidCount) / 42.0, 2);
					if (UserDefinedFrame.avg(UserDefinedFrame.getArrayWithinIndeces(slurryRate, stageUp.get(count - 1),
							stageUp.get(count))) > 5.0) {
						saveSlurr = saveSlurr + Double.valueOf(slTotal.get(stageUp.get(count)))
								- Double.valueOf(slTotal.get(stageUp.get(count - 1))) - acid;

					} else {
						saveSlurr = saveSlurr + Double.valueOf(slTotal.get(stageUp.get(count)))
								- Double.valueOf(slTotal.get(stageUp.get(count - 1)));
					}
					slurr.add(acid);
					count++;
					acidCount++;
					continue;
				}
				saveSlurr = checkSlurryAdd(Double.valueOf(slTotal.get(stageUp.get(count)))
						- Double.valueOf(slTotal.get(stageUp.get(count - 1))), saveSlurr);
			}
			if (count == stageUp.size() - 1) {
				break;
			}
			count++;
		}
		count = 0;

		ArrayList<String> slurrString = new ArrayList<>();
		for (Double j : slurr) {
			if (count < stageNames.size() && stageNames.get(count).toUpperCase().contains("ACID")) {
				slurrString.add(String.valueOf(Math.round(j * Double.valueOf(100)) / Double.valueOf(100)));
				// Main.yess.mTable.setValueAt(Math.rint(j), count, 9);
				slurryGrand = slurryGrand + Math.round(j);
			} else {
				slurrString.add(String.valueOf(Math.round(j)));
				// Main.yess.mTable.setValueAt(j, count, 9);
				slurryGrand = slurryGrand + Math.round(j);
			}
			count++;
		}
		System.out.println("End slurryTotal Thread/Start constructTreatment");
		constructTreatment.appendToMap(9, slurrString);
		System.out.println("End slurryTotal Thread/End constructTreatment");
		finished = true;
	}

	public Double checkSlurryAdd(Double slurryAdd, Double saveSlurr) {
		if (slurryAdd + saveSlurr < 0.0) {
			slurr.add(1.0);
			return saveSlurr -= 1.0;
		}
		slurr.add(slurryAdd + saveSlurr);
		return 0.0;
	}

	public synchronized Long getSlurryGrand() {
		int i = 0;
		while (i < 10 & slurryGrand == 0) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
			i++;
		}
		return slurryGrand;
	}
}
