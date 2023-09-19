import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class PumpTime {
	private ArrayList<LocalDateTime> startDate;
	private ArrayList<LocalDateTime> endDate;
	private ArrayList<Long> subPumpTime;
	private Double pumptime;
	private Semaphore semaphore;

	PumpTime(ArrayList<LocalDateTime> startDate, ArrayList<LocalDateTime> endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.subPumpTime = new ArrayList<>();
		this.semaphore = new Semaphore(0);
	}

	public void calcPumptime() {
		Long pumpT = Long.valueOf(0);
		int i;
		for (i = 0; i < startDate.size(); i++) {
			subPumpTime.add(Duration.between(startDate.get(i), endDate.get(i)).toSeconds());
			pumpT = pumpT + subPumpTime.get(subPumpTime.size() - 1);
		}
		setPumptime(Double.valueOf(pumpT) / Double.valueOf(60));
		semaphore.release();
		System.out.println("Pumptime #1: " + pumpT);
	}

	public void setPumptime(Double pumptime) {
		double newPumpTime = Math.round(pumptime * 100) / Double.valueOf(100);
		this.pumptime = newPumpTime;
	}

	public Double getPumptime() {
		try {
			semaphore.acquire();
		} catch (InterruptedException e1) {
		}
		System.out.println("Pumptime: " + this.pumptime);
		semaphore.release();
		return this.pumptime;
	}

	public Double removeIndex(int i) {
		subPumpTime.remove(i);
		setPumptime(reCalcPumpTime(subPumpTime));
		return this.pumptime;
	}

	private Double reCalcPumpTime(ArrayList<Long> subPumpTime) {
		Long newPumpTime = Long.valueOf(0);
		for (Long a : subPumpTime) {
			newPumpTime = newPumpTime + a;
		}

		return Double.valueOf(newPumpTime) / Double.valueOf(60);
	}

}
