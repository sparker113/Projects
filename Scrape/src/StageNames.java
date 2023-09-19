import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JTable;

public class StageNames {
	private ArrayList<Integer> theIndArray;
	private HashMap<String, ArrayList<Integer>> shutdownResumes;
	private ArrayList<Integer> stEndAcid;
	private ArrayList<String> nameArray = new ArrayList<>();
	private String operator;
	private Boolean set;
	private HashMap<String, ArrayList<Integer>> stageInputs;
	private Boolean containShutdown = false;
	private HashMap<String,Boolean> savedOptions;
	public StageNames(ArrayList<Integer> theIndArray, HashMap<String, ArrayList<Integer>> shutdownResumes,
			ArrayList<Integer> stEndAcid, String operator, HashMap<String, ArrayList<Integer>> stageInputs,
			HashMap<String,Boolean> savedOptions) {
		this.theIndArray = theIndArray;
		this.shutdownResumes = shutdownResumes;
		this.stEndAcid = stEndAcid;
		this.operator = operator;
		this.set = false;
		this.stageInputs = stageInputs;
		this.savedOptions = savedOptions;
		execute();
	}

	private void execute() {
		int i;
		int acidProg = 0;
		String[] acidPred = new String[2];
		setCheckShutdowns();
		if (operator.contains("Pioneer Natural Resources")) {
			acidPred[0] = "LOAD WELL";
			acidPred[1] = "ACID SPEARHEAD";
		} else {
			acidPred[1] = "ACID";
			acidProg = 1;
		}
		checkAcid();
		for (i = 1; i < theIndArray.size() - 1; i++) {
			if (stEndAcid.contains(theIndArray.get(i))) {
				nameArray.add(acidPred[acidProg]);
				if (acidProg == 1) {
					nameArray.add("PAD");
				}
				acidProg++;
			} else {
				nameArray.add(getName(theIndArray.get(i), i));
			}
		}
		set = true;
	}

	private String stageInputsNames(Integer i) {
		for (String s : stageInputs.keySet()) {
			if (stageInputs.get(s).contains(i)) {
				switch (s) {
				case ("Stage_Up"):
					return "SWEEP";
				// case("Acid_Spearhead"):
				case ("Mid_Stage_Acid"):
					return getAcidName(operator);
				case ("Diverter"):
					return "DIVERTER";
				}
			}
		}
		return "";
	}

	private String endStageNames(Integer count) {
		if (count == theIndArray.size() - 2) {
			return "FLUSH";
		} else if (count == theIndArray.size() - 3 && savedOptions.get(mainFrame.PREFLUSH_OPTION)) {
			return "PRE-FLUSH";
		}
		return "";
	}

	private String getName(Integer i, Integer count) {
		String stageName = "";
		if (checkShutdowns()) {
			if (shutdownResumes.get("Shutdowns").contains(i)) {
				return "SHUTDOWN";
			} else if (shutdownResumes.get("Resumes").contains(i)) {
				return "RESUME";
			}
		}
		String endStageName = endStageNames(count);
		if (!endStageName.equals("")) {
			return endStageName;
		}
		String inputsName = stageInputsNames(i);
		if (!inputsName.equals("")) {
			return inputsName;
		}
		return "SWEEP";
	}

	private static String getAcidName(String operator) {
		switch (operator) {
		case ("Pioneer Natural Resources"):
			return "ACID SPEARHEAD";
		default:
			return "ACID";
		}
	}

	private void setCheckShutdowns() {
		if (!nullCheck(shutdownResumes.get("Shutdowns"))) {
			this.containShutdown = true;
		} else {
			this.containShutdown = false;
		}
	}

	private Boolean checkShutdowns() {
		return this.containShutdown;
	}

	private void checkAcid() {
		if (stEndAcid.isEmpty()) {
			nameArray.add(0, "PAD");
		}
	}

	private Boolean nullCheck(ArrayList<?> checkArray) {
		boolean check = false;
		if (checkArray.toString() == "null") {
			check = true;
		}
		return check;
	}

	public void printNames(JTable table, Integer column) {
		int row = 0;
		int i = 0;
		for (String name : nameArray) {
			table.setValueAt(name, row, column);
			row++;
		}
	}

	public synchronized ArrayList<String> getNameArray() {
		System.out.println(this.nameArray);
		return this.nameArray;
	}

	/*
	 * @Override public boolean cancel(boolean mayInterruptIfRunning) { // TODO
	 * Auto-generated method stub return false; }
	 *
	 * @Override public boolean isCancelled() { // TODO Auto-generated method stub
	 * return false; }
	 *
	 * @Override public boolean isDone() { return set; }
	 *
	 * @Override public StageNames get() throws InterruptedException,
	 * ExecutionException {
	 *
	 * return this; }
	 *
	 * @Override public StageNames get(long timeout, TimeUnit unit) throws
	 * InterruptedException, ExecutionException, TimeoutException {
	 * Thread.sleep(Duration.of(timeout, (TemporalUnit)
	 * unit.toChronoUnit()).get(TimeUnit.MILLISECONDS.toChronoUnit())); return null;
	 * }
	 */

}
