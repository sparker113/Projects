import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import javax.swing.JTable;

public class SandTotal {
	private String at;
	private String wellID;
	private String treatmentID;
	private ArrayList<String> sandType = new ArrayList<>();
	private ArrayList<Long> sandAmt = new ArrayList<>();
	private ArrayList<Long> sandDesign = new ArrayList<>();
	private ArrayList<String> addType = new ArrayList<>();
	private ArrayList<Long> addStrap = new ArrayList<>();
	private ArrayList<Double> shift = new ArrayList<>();
	private Double acidAmtGal;
	private Double acidAmt;
	private String acidInput;
	private boolean pumpedAcid;
	private NothingSand nothingSand;

	public SandTotal(String at, String wellID, String treatmentID, String acidInput) {
		this.at = at;
		this.wellID = wellID;
		this.treatmentID = treatmentID;
		this.acidInput = acidInput;
	}

	public SandTotal(String acidInput) {
		this.acidInput = acidInput;
	}

	public Boolean inputAcid() {
		boolean tempBool = false;
		if (acidInput != "0") {
			tempBool = true;
		}
		return tempBool;
	}

	public void evaluate() {

		SaveChemicals saveChems = new SaveChemicals();
		if (addType.isEmpty()) {

			try {
				addType.addAll(SaveChemicals.readChemicals(Main.yess.diagTable1));
			} catch (IOException e) {
			}

		} else {
			try {
				saveChems.writeChemicals(addType);
			} catch (IOException e) {
			}

		}
		if (sandType.isEmpty()) {
			doNothingSand();
		}
		setValues();

	}

	public void setValues() {
		Long totalSand = Long.valueOf(0);
		Integer count1 = 0;
		for (String a : sandType) {
			Main.yess.diagTable3.setValueAt(a, count1, 0);
			count1++;
		}
		count1 = 0;
		for (Long b : sandAmt) {
			if (b == null) {
				b = Long.valueOf(0);
			}
			Main.yess.diagTable3.setValueAt(b, count1, 1);
			if (count1 > 0) {
				totalSand = totalSand + b;
				shift.add(Double.valueOf(totalSand) / Double.valueOf(sandAmt.get(0)));
			}
			count1++;
		}
		count1 = 0;
		for (Long c : sandDesign) {
			Main.yess.diagTable3.setValueAt(c, count1, 2);
			count1++;
		}
		count1 = 0;
		pumpedAcid = false;
		Long totalAcid = Main.yess.getAcidInputTotal();
		Integer acidCount = -1;
		for (String a : addType) {
			Main.yess.diagTable1.setValueAt(a, count1, 0);

			if ((a.toUpperCase().contains("ACID") | a.toUpperCase().contains("HCL"))
					&& (!a.toUpperCase().contains("SAFE") | !a.toUpperCase().contains("SCL"))) {
				if (!addStrap.isEmpty() && addStrap.get(count1) > Long.valueOf(0)) {
					pumpedAcid = true;
					totalAcid += Long.valueOf(addStrap.get(count1));
					System.out.println("The total gallons of Acid pumped was: " + totalAcid);
					acidAmt = FracCalculations
							.getDoubleRoundedDouble(Double.valueOf(addStrap.get(count1)) / Double.valueOf(42), 3);
					setAcidAmtGal(Double.valueOf(totalAcid));
					setAcidAmt(acidAmt);

				}
				acidCount = count1;
			}
			count1++;
		}
		if (!pumpedAcid && totalAcid == 0.0) {
			acidAmt = 0.0;
		}
		count1 = 0;
		for (Long b : addStrap) {
			if (count1 != acidCount) {
				Main.yess.diagTable1.setValueAt(b, count1, 1);
			} else {
				totalAcid = String.valueOf(totalAcid).contains(".")
						? Long.valueOf(String.valueOf(totalAcid).split(".")[0])
						: totalAcid;
				Main.yess.diagTable1.setValueAt(totalAcid, count1, 1);
			}
			count1++;
		}
		setSandType(sandType);
		setSandAmt(sandAmt);
		setShift(shift);

	}

	public void doNothingSand() {
		NothingSand nothingSand = new NothingSand();

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (nothingSand.isVisible()) {
		}
		TreeMap<String, String> yesSand = nothingSand.getSand();

		for (String key : yesSand.keySet()) {
			if (key == "Total Proppant (lbm)") {
				sandType.add(0, key);
				sandAmt.add(0, Long.valueOf(yesSand.get(key)));
			} else {
				sandType.add(key);
				sandAmt.add(Long.valueOf(yesSand.get(key)));
			}
		}
		int i;
		for (i = 0; i < 10; i++) {
			nothingSand.getTable().setValueAt("", i, 1);
		}
	}

	public void writeToTable(JTable table, ArrayList<String> sandType, ArrayList<Long> sandAmt) {

	}

	public void setSandType(ArrayList<String> sandType) {
		this.sandType = sandType;
	}

	public ArrayList<String> getSandType() {
		return this.sandType;
	}

	public void setSandAmt(ArrayList<Long> sandAmt) {
		this.sandAmt = sandAmt;
	}

	public ArrayList<Long> getSandAmt() {
		return this.sandAmt;
	}

	public void setShift(ArrayList<Double> shift) {
		this.shift = shift;
	}

	public ArrayList<Double> getShift() {
		return this.shift;
	}

	public Boolean pumpedAcid() {
		return this.pumpedAcid;
	}

	private void setAcidAmtGal(Double acidAmtGal) {
		this.acidAmtGal = acidAmtGal;
	}

	public Double getAcidAmtGal() {
		return this.acidAmtGal == null ? 0.0 : this.acidAmtGal;
	}

	public void setAcidAmt(Double acidAmt) {
		this.acidAmt = acidAmt;
	}

	public Double getAcidAmt() {
		return this.acidAmt == null ? 0.0 : this.acidAmt;
	}

	public HashMap<String, Long> getChemStraps() {
		HashMap<String, Long> chemStraps = new HashMap<>();
		int i = 0;
		for (String s : addType) {
			chemStraps.put(s, addStrap.get(i));
			i++;
		}
		return chemStraps;
	}

	public void setChemStrap(String key, Long value) {

	}
}
