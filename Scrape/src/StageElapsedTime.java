import java.util.ArrayList;

public class StageElapsedTime extends Thread {
	private ArrayList<String> elapsedTime;
	private ArrayList<Integer> stageUp;

	public StageElapsedTime(ArrayList<String> elapsedTime, ArrayList<Integer> stageUp) {
		this.elapsedTime = elapsedTime;
		this.stageUp = stageUp;
	}

	@Override
	public void run() {
		Integer ii = 1;
		for (String s : elapsedTime) {
			if (!Integer.valueOf(s).equals(Integer.valueOf(Integer.valueOf(elapsedTime.get(ii)) - 1))) {
				System.out.println(
						"The time offset - " + (Integer.valueOf(s) - Integer.valueOf(elapsedTime.get(ii + 1))));

			}
			if (ii == elapsedTime.size() - 5) {
				break;
			}
			ii++;
		}
		for (Integer i : stageUp) {
			System.out.println(elapsedTime.get(i));
		}
	}

}
