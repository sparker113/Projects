import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class StageSlurryRate extends Thread {
	private ArrayList<String> slRate;
	private ArrayList<Integer> stageUp;
	private ArrayList<Double> avgRate = new ArrayList<>();
	private ArrayList<String> stageNames;
	private TreatmentSummary constructTreatment;
	private Semaphore avgSem;
	private String acidRate;

	public StageSlurryRate(ArrayList<String> slRate, ArrayList<Integer> stageUp, ArrayList<String> stageNames,
			TreatmentSummary constructTreatment, Semaphore avgSem) {
		this.avgSem = avgSem;
		this.slRate = slRate;
		this.stageUp = stageUp;
		this.stageNames = stageNames;
		this.constructTreatment = constructTreatment;
		this.acidRate = "0.0";
	}

	@Override
	public void run() {
		System.out.println("Average Slurry Rate Index Array: " + stageUp);
		System.out.println("Start slurryRate Thread");
		SplitAverage<Double> avgRate = null;
		try {
			avgRate = new SplitAverage(stageUp, slRate, "Slurry Rate");
		} catch (InterruptedException e) {
			System.out.println("Average Rates Interrupted Exception");
			try {
				TextLog textLog = new TextLog("Average Rates Interruped Exception " + LocalDateTime.now());
			} catch (IOException e1) {
			}
		}

		ArrayList<String> avgRateString = avgRate.getAveragesString();
		setAcidRate(avgRateString);
		if (stageNames.contains("LOAD WELL") || stageNames.contains("ACID")) {
			avgRateString = correctAcidRates(avgRateString);
		}
		System.out.println("End slurryRate Thread/Start constructTreatment");
		constructTreatment.appendToMap(8, avgRateString);
		avgSem.release();
		System.out.println("End slurryRate Thread/End constructTreatment: " + avgRateString.size());

	}

	public ArrayList<Integer> acidRateInd() {
		ArrayList<Integer> indeces = new ArrayList<>();
		ArrayList<String> names = new ArrayList<>(stageNames);
		int i = 0;
		for (String a : names) {
			switch (a) {
			case "LOAD WELL", "ACID SPEARHEAD":
				indeces.add(i);
				System.out.println("LOAD WELL AND ACID INDECES " + i);
				i++;
				break;
			default:
				i++;
				break;
			}
		}
		return indeces;
	}

	public ArrayList<String> correctAcidRates(ArrayList<String> avgRateString) {
		ArrayList<Integer> indeces = acidRateInd();
		for (int i : indeces) {
			avgRateString.remove(i);
			avgRateString.add(i, "5.0");
		}
		return avgRateString;
	}

	private void setAcidRate(ArrayList<String> avgRates) {
		if (!stageNames.contains("ACID") && !stageNames.contains("ACID SPEARHEAD")) {
			this.acidRate = "0.0";
			return;
		}
		int count = 0;
		for (String s : stageNames) {
			if (s.toUpperCase().contains("ACID")) {
				this.acidRate = avgRates.get(count);
				return;
			}
			count++;
		}
	}

	public String getAcidRate() {
		return this.acidRate;
	}

}
