import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PlotData implements Runnable {
	int i = 1;
	private ArrayList<String>[] args;
	private Thread t;

	@SafeVarargs
	public PlotData(ArrayList<String>... args) {
		this.args = args;
		this.t = new Thread(this, "Plot_Data_Thread");
		t.start();
	}

	int getI() {
		return i++;
	}

	@Override
	public void run() {
		ExecutorService executor = Executors.newCachedThreadPool();

		for (ArrayList<String> a : args) {

			executor.execute(() -> {
				System.out.println("PlotData executed - " + getI());
				ChannelToTxt chanToTxt = new ChannelToTxt(a, "C:\\Scrape\\ScrapePython\\Plot\\" + a.get(0) + ".txt");
				chanToTxt.execute();
			});
		}
		try {
			executor.shutdown();
			executor.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
		}
	}

}
