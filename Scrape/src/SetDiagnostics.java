import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.swing.JTable;

public class SetDiagnostics extends LinkedHashMap<String, String> {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private Map<String, Integer> sigValPosMap;
	transient Semaphore semaphore = new Semaphore(0);
	transient ExecutorService executor;

	SetDiagnostics(Map<String, Integer> sigValPosMap) {
		this.sigValPosMap = sigValPosMap;
		this.executor = Executors.newCachedThreadPool();
	}

	public synchronized void writeMapToTable(JTable table) {
		try {
			semaphore.tryAcquire(2500, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			System.out.println("InterruptedException SetDiagnostics::writeMapToTable");
		}
		Executors.newSingleThreadExecutor().execute(() -> {
			for (String s : sigValPosMap.keySet()) {
				if (keySet().contains(s) & getCellValue(table, sigValPosMap.get(s), 1).equals("")) {
					table.setValueAt(this.get(s), sigValPosMap.get(s), 1);
				}
			}
		});
	}

	public static String getCellValue(JTable table, int row, int column) {
		if (table.getValueAt(row, column) == null || table.getValueAt(row, column).toString().equals("")) {
			return "";
		}
		return table.getValueAt(row, column).toString();
	}

	public synchronized void setUserDefinedResults(HashMap<String, HashMap<String, String>> userDefinedMap) {
		if (userDefinedMap == null || userDefinedMap.isEmpty()) {
			return;
		}
		for (String s : userDefinedMap.keySet()) {
			this.put(s, userDefinedMap.get(s).get("result"));
		}
		System.out.println(userDefinedMap);
	}

	public synchronized void setGraphDiagnostics(HashMap<String, String> markers) {
		if (markers == null) {
			return;
		}
		executor.execute(() -> {
			this.put("Open Pressure", markers.get("open_pressure"));
			this.put("Close Pressure", markers.get("close_pressure"));
			this.put("Breakdown Pressure", markers.get("break_pressure"));
			this.put("Breakdown Rate", markers.get("break_rate"));
			this.put("Breakdown Time", markers.get("break_time"));
			this.put("Breakdown Volume", markers.get("break_volume"));
			this.put("ISIP", markers.get("isip"));
			this.put("ISIP Time", markers.get("isip_time"));
			semaphore.release();
		});
	}

}
