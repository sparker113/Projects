import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class StagePropCon implements Runnable {

	private ArrayList<Integer> stageUp;
	private ArrayList<String> propCon;
	private ArrayList<Double> averageCon = new ArrayList<>();
	private Integer lIndex = 0;
	private TreatmentSummary constructTreatment;
	private Semaphore stageSandSem;
	private Semaphore avgSem;
	private ArrayList<Integer> excludeArray;
	private Double sandRound;

	public StagePropCon(ArrayList<String> propCon, ArrayList<Integer> stageUp, TreatmentSummary constructTreatment,
			Semaphore stageSandSem, Semaphore avgSem, ArrayList<Integer> excludeArray, Double sandRound) {
		this.avgSem = avgSem;
		this.propCon = propCon;
		this.stageUp = stageUp;
		this.constructTreatment = constructTreatment;
		this.stageSandSem = stageSandSem;
		this.excludeArray = excludeArray;
		this.sandRound = sandRound;
	}

	@Override
	public void run() {

		SplitAverage<String> averageCon = null;
		try {
			averageCon = new SplitAverage<>(stageUp, propCon, sandRound, true, excludeArray);
		} catch (InterruptedException e1) {
		}
		ArrayList<String> averageConString = averageCon.getAveragesString();
		System.out.println("End propCon Thread/Start constructTreatment");
		int i = 0;

		constructTreatment.appendToMap(5, averageConString);
		this.setAverageCon(averageCon.getAverages());
		try {
			setLastIndex(findLastIndex(averageConString));
		}catch(IOException|ClassNotFoundException e) {
			e.printStackTrace();
			stageSandSem.release();
			avgSem.release();
			return;
		}
		stageSandSem.release();
		avgSem.release();
		System.out.println("End propCon Thread/End constructTreatment - 2");

	}

	public void setAverageCon(ArrayList<Double> averageCon) {
		this.averageCon = averageCon;
	}

	public ArrayList<Double> getAverageCon() {
		return this.averageCon;
	}

	public void setLastIndex(Integer lIndex) {
		this.lIndex = lIndex;
	}

	public Integer getLastIndex() {
		return this.lIndex;
	}

	public Integer findLastIndex(ArrayList<String> averageConString) throws ClassNotFoundException, IOException {
		Integer i;
		Integer index = 0;
		HashMap<String,Boolean> optionsMap = mainFrame.getSavedOptions();
		for (i = averageConString.size() - 1; i > 0; i--) {
			if (averageConString.get(i) != "") {
				if (optionsMap.get(mainFrame.PREFLUSH_OPTION) && i == averageConString.size() - 2) {
					this.averageCon.remove(i.intValue());
					this.averageCon.add(i.intValue(), 0.0);
					continue;
				}
				index = i;
				break;
			}
		}

		return index;
	}
}
