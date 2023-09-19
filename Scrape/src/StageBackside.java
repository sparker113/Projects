import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class StageBackside implements Runnable {
	private ArrayList<String> backPres;
	private CompletableFuture<Double> average;
	private Thread t;

	public StageBackside(ArrayList<String> backPres) {
		this.backPres = backPres;
		this.average = new CompletableFuture<>();
		this.t = new Thread(this, "Backside Thread");
		t.start();
	}

	@Override
	public void run() {
		if (backPres != null) {
			averageBackside();
		} else {
			return;
		}
	}

	public Double averageBackside() {
		double temp = 0.0;
		for (String s : backPres) {
			temp = temp + Double.valueOf(s);
		}
		Double averageBackside = temp / Double.valueOf(backPres.size());
		this.average.complete(averageBackside);
		return averageBackside;
	}

	public Double getAverage() throws InterruptedException, ExecutionException {
		return this.average.get();
	}

}
