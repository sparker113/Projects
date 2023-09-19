import java.util.ArrayList;

import javax.swing.JOptionPane;

public class baseTreatment {
	ArrayList<String> startDate = new ArrayList<>();
	ArrayList<String> startTime = new ArrayList<>();
	ArrayList<String> endDate = new ArrayList<>();
	ArrayList<String> endTime = new ArrayList<>();
	ArrayList<Double> sandCon = new ArrayList<>();
	ArrayList<Double> cleanTotal = new ArrayList<>();
	ArrayList<Double> tPressure = new ArrayList<>();
	ArrayList<Double> slurryRate = new ArrayList<>();
	ArrayList<Double> slurryTotal = new ArrayList<>();
	ArrayList<Double> subSandLbs = new ArrayList<>();

	public baseTreatment(ArrayList<ArrayList<String>> masArray, ArrayList<Integer> dataValue,
			ArrayList<Integer> dataIndexValue) {
		Integer i = 0;
		Integer ii = 0;
		Integer stageNumIndex = 0;
		Integer sandConIndex = 0;
		Integer cleanTotalIndex = 0;
		Integer treatPresIndex = 0;
		Integer avgRateIndex = 0;
		Integer slurryTotalIndex = 0;
		int headers = 0;
		int count = 0;
		Double sc = 0.00;
		double ct = 0.00;
		Double tp = 0.00;
		Double sr = 0.00;
		double st = 0.00;
		double sst = 0.00;
		for (ArrayList<String> al : masArray) {
			if (al.get(0).toUpperCase().contains("STAGE")) {
				stageNumIndex = count;
				count++;
			} else if (al.get(0).toUpperCase().contains("BLENDER DENS")) {
				sandConIndex = count;
				count++;
			} else if (al.get(0).toUpperCase().contains("CLEAN") & al.get(0).toUpperCase().contains("TOTAL")) {
				cleanTotalIndex = count;
				count++;
			} else if (al.get(0).toUpperCase().contains("TREATING PRESSURE")
					|| al.get(0).toUpperCase().trim().contains("WELLSIDE")
					|| al.get(0).toUpperCase().contains("PRESSURE 1")) {
				treatPresIndex = count;
				count++;
			} else if (al.get(0).toUpperCase().contains("RATE")) {
				avgRateIndex = count;
				count++;
			} else if (al.get(0).toUpperCase().contains("SLURRY") & al.get(0).toUpperCase().contains("TOTAL")) {
				slurryTotalIndex = count;
				count++;
			} else {
				count++;
			}
		}
		if (sandConIndex == 0) {
			JOptionPane.showMessageDialog(null,
					"Figure out your fucking blender prop channel and then re-run the stage!", "GET YOUR SHIT TOGETHER",
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);

		}
		/*
		 * ArrayList<Double> sandCon; Double sc = 0.00; ArrayList<Double> cleanTotal;
		 * Double ct = 0.00; ArrayList<Double> tPressure; Double tp = 0.00;
		 * ArrayList<Double> slurryRate; Double sr = 0.00; ArrayList<Double>
		 * slurryTotal; Double st = 0.00; ArrayList<Double> subSandLbs; Double sst =
		 * 0.00;
		 */
		for (i = 1; i < dataValue.size() + 1; i++) {
			startDate.add(masArray.get(0).get(dataIndexValue.get(i - 1)).split("T")[0]);
			endDate.add(masArray.get(0).get(dataIndexValue.get(i)).split("T")[0]);
			startTime.add((masArray.get(0).get(dataIndexValue.get(i - 1)).split("T")[1]).split("Z")[0]);
			endTime.add((masArray.get(0).get(dataIndexValue.get(i)).split("T")[1]).split("Z")[0]);
			for (ii = dataIndexValue.get(i - 1); ii < dataIndexValue.get(i); ii++) {
				// Start Time0/Date3 - End Time1/Date4 // put that shit somewhere else

				// Stage Number2 and count this shit after you run through the data

				// Sand Concentration5
				if (sc == 0.00) {
					sc = Double.valueOf(masArray.get(sandConIndex).get(ii));
				}
				sc = (Double.valueOf(masArray.get(sandConIndex).get(ii)) + sc) / 2;
				// Clean Barrels6
				// Average Pressure7
				if (tp == 0.00) {
					tp = Double.valueOf(masArray.get(treatPresIndex).get(ii));
				}
				tp = (Double.valueOf(masArray.get(treatPresIndex).get(ii)) + tp) / 2;

				// Average Slurry Rate 9
				if (sr == 0.00) {
					sr = Double.valueOf(masArray.get(avgRateIndex).get(ii));
				}
				sr = (Double.valueOf(masArray.get(avgRateIndex).get(ii)) + sr) / 2;
				// Slurry Barrels 10

			}
			if (cleanTotal.size() == 0) {
				cleanTotal.add(Double.valueOf(masArray.get(cleanTotalIndex).get(ii)));
				slurryTotal.add(Double.valueOf(masArray.get(slurryTotalIndex).get(ii)));
			} else {
				cleanTotal.add(Double.valueOf(masArray.get(cleanTotalIndex).get(ii)) - cleanTotal.get(i - 2));
				slurryTotal.add(Double.valueOf(masArray.get(slurryTotalIndex).get(ii)) - slurryTotal.get(i - 2));
			}

			sandCon.add(sc);
			sc = 0.00;
			tPressure.add(tp);
			tp = 0.00;
			slurryRate.add(sr);

		}
		for (Double a : tPressure) {
			// System.out.println(a);
		}
	}

	public ArrayList<Double> getSandCon() {
		return sandCon;
	}

	public void setSandCon(ArrayList<Double> sandCon) {
		this.sandCon = sandCon;
	}

	public ArrayList<Double> getCleanTotal() {
		return cleanTotal;
	}

	public void setCleanTotal(ArrayList<Double> cleanTotal) {
		this.cleanTotal = cleanTotal;
	}

	public ArrayList<Double> getPressure() {
		return tPressure;
	}

	public void settPressure(ArrayList<Double> tPressure) {
		this.tPressure = tPressure;
	}

	public ArrayList<Double> getSlurryRate() {
		return slurryRate;
	}

	public void setSlurryRate(ArrayList<Double> slurryRate) {
		this.slurryRate = slurryRate;
	}

	public ArrayList<Double> getSlurryTotal() {
		return slurryTotal;
	}

	public void setSlurryTotal(ArrayList<Double> slurryTotal) {
		this.slurryTotal = slurryTotal;
	}

	public ArrayList<Double> getSubSandLbs() {
		return subSandLbs;
	}

	public void setSubSandLbs(ArrayList<Double> subSandLbs) {
		this.subSandLbs = subSandLbs;
	}

	public void setStartDate(ArrayList<String> startDate) {
		this.startDate = startDate;
	}

	public ArrayList<String> getStartDate() {
		return this.startDate;
	}

	public void setEndDate(ArrayList<String> endDate) {
		this.endDate = endDate;
	}

	public ArrayList<String> getEndDate() {
		return this.endDate;
	}

	public void setStartTime(ArrayList<String> startTime) {
		this.startTime = startTime;
	}

	public ArrayList<String> getStartTime() {
		return this.startTime;
	}

	public void setEndTime(ArrayList<String> endTime) {
		this.endTime = endTime;
	}

	public ArrayList<String> getEndTime() {
		return this.endTime;
	}

	public baseTreatment() {
		// TODO Auto-generated constructor stub
	}
}
