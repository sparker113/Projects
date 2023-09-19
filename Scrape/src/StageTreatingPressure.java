import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class StageTreatingPressure implements Runnable {
	private ArrayList<String> treatPres;
	private ArrayList<Integer> stageUp;
	private TreatmentSummary constructTreatment;
	private Semaphore avgSem;
	ArrayList<Long> pressureArray = new ArrayList<>();

	public StageTreatingPressure(ArrayList<String> treatPres, ArrayList<Integer> stageUp,
			TreatmentSummary constructTreatment, Semaphore avgSem) {

		this.treatPres = treatPres;
		this.stageUp = stageUp;
		this.constructTreatment = constructTreatment;
		this.avgSem = avgSem;
	}

	@Override
	public void run() {
		System.out.println("Average Treating Pressure Index Array: " + stageUp);
		LocalDateTime a = LocalDateTime.now();
		System.out.println("Start Treating Pressure Thread");
		SplitAverage<String> treatingPressureAvg = null;
		try {
			treatingPressureAvg = new SplitAverage<>(stageUp, treatPres);
		} catch (InterruptedException e) {
			try {
				TextLog textLog = new TextLog("Treating Pressure InterruptedException " + LocalDateTime.now());
			} catch (IOException e1) {
			}
		}
		ArrayList<String> averagePressuresString = treatingPressureAvg.getAveragesString();

		System.out.println("End Treating Pressure Thread/Start ConstructTreatment");
		System.out.println("Average Pressures: " + averagePressuresString);
		constructTreatment.appendToMap(7, averagePressuresString);
		avgSem.release();
		System.out.println("End Treating Pressure Thread/End ConstructTreatment");

	}

}
