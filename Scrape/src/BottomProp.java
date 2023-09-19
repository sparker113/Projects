import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class BottomProp implements Runnable {
	private ArrayList<String> propCon;
	private ArrayList<String> slurryTotal;
	private ArrayList<String> bottomProp = new ArrayList<>();
	private Double bottomPerf;
	private boolean bottomPropSet = false;
	private Double casingFactor;
	private Semaphore perfsSemaphore;
	private Thread t;

	BottomProp(ArrayList<String> propCon, ArrayList<String> slurryTotal, Double bottomPerf, Double casingFactor,
			Semaphore perfsSemaphore) {
		this.propCon = propCon;
		this.slurryTotal = slurryTotal;
		this.bottomPerf = bottomPerf;
		this.casingFactor = casingFactor;
		this.perfsSemaphore = perfsSemaphore;
	}

	BottomProp(ArrayList<String> propCon, ArrayList<String> slurryTotal, Double bottomPerf, Double casingFactor) {
		this.propCon = propCon;
		this.slurryTotal = slurryTotal;
		this.bottomPerf = bottomPerf;
		this.casingFactor = casingFactor;
		this.perfsSemaphore = new Semaphore(0);
		this.t = new Thread(this, "Bottom_Prop_Thread");
		t.start();
	}

	public static Double wellboreVolume(Double bottomPerf, Double casingFactor) {
		double volume = casingFactor * Double.valueOf(bottomPerf);
		return volume;
	}
	public ArrayList<String> getBottomPropSynced() {
		try {
			perfsSemaphore.tryAcquire(2500,TimeUnit.MILLISECONDS);
		}catch(InterruptedException e) {
			e.printStackTrace();
			return null;
		}
		return bottomProp;

	}
	public synchronized void constructBottomProp() {

		System.out.println("Bottom Prop Acquired Semaphore");
		Double wellboreVolume = wellboreVolume(bottomPerf, casingFactor);

		int elapTime = propCon.size();
		int startAtRestart = getStartAtRestart();
		int startAtVolume = getStartAtVolume(wellboreVolume);

		int i;
		int ii;
		bottomProp.add(0, "Bottom Prop Conc");
		for (i = 0; i < startAtVolume; i++) {
			bottomProp.add("0");
		}
		one: for (i = startAtVolume; i < elapTime; i++) {
			for (ii = startAtRestart; ii < elapTime; ii++) {
				try {
					if (Double.valueOf(slurryTotal.get(ii)) >= (Double.valueOf(slurryTotal.get(i)) - wellboreVolume)) {
						bottomProp.add(propCon.get(ii));
						break;
					}
				} catch (IndexOutOfBoundsException e) {
					break one;
				}
				if (Integer.valueOf(ii) == slurryTotal.size() - 1) {
					break;
				}
			}
		}
		bottomPropSet = true;
		System.out.println("Bottom Prop Notified ------------");

		notify();
	}

	public int getStartAtVolume(Double wellboreVolume) {
		int elapTime = propCon.size();
		int start;
		int startInd = 0;
		for (start = 0; start < elapTime; start++) {
			if (Double.valueOf(slurryTotal.get(start)) < wellboreVolume) {
				startInd = start;
				break;
			}
		}
		return startInd;
	}

	public int getStartAtRestart() {
		int elapTime = propCon.size();
		int start;
		int startInd = 0;
		for (start = 0; start < elapTime; start++) {
			if (Double.valueOf(slurryTotal.get(start)) < Double.valueOf(50)) {
				startInd = start;
				break;
			}
		}
		return startInd;
	}

	public synchronized ArrayList<String> getBottomProp() {
		while (!bottomPropSet) {
			try {
				System.out.println("Bottom Prop Waiting to be hadddddddddddddddddddddd");
				wait();
			} catch (InterruptedException e) {
				try {
					new TextLog("Bottom Prop Interrupted");
				} catch (IOException e1) {
				}
			}
		}
		return this.bottomProp;
	}

	@Override
	public void run() {
		System.out.println("Start Bottom Prop Thread");
		constructBottomProp();
		System.out.println("End Bottom Prop Thread");
	}
}
