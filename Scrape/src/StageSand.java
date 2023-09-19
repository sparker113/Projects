import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StageSand {
	
	private ArrayList<String> sandType;
	private ArrayList<Long> sandAmt;
	private ArrayList<Double> averageCon;
	private ArrayList<Double> cleanTotal;
	private ArrayList<Long> subStageSand = new ArrayList<>();
	private ArrayList<Double> shift;
	private ArrayList<String> stageName;
	private Integer lastIndex;
	private ArrayList<String> sandTypeString = new ArrayList<>();
	private TreatmentSummary constructSummary;

	public StageSand(ArrayList<String> sandType, ArrayList<Long> sandAmt, ArrayList<Double> averageCon,
			ArrayList<Double> cleanTotal, ArrayList<Double> shift, Integer lastIndex, ArrayList<String> stageName,
			TreatmentSummary constructSummary) {
		this.stageName = stageName;
		fixSandArrays(sandType, sandAmt);
		this.averageCon = averageCon;
		this.cleanTotal = cleanTotal;
		this.shift = shift;
		this.lastIndex = lastIndex;
		this.constructSummary = constructSummary;
	}
	
	public void setSandTypeArray(ArrayList<String> sandType) {
		this.sandType = sandType;
	}
	
	public void setSandAmtArray(ArrayList<Long> sandAmt) {
		this.sandAmt = sandAmt;
	}
	
	public Double getActualToCalcRatio() {
		Double calculatedTotal = getCalculatedTotalSand();
		return Double.valueOf(sandAmt.get(0)) / calculatedTotal;
	}

	public Double getCalculatedTotalSand() {
		double total = 0.0;
		int i = 0;
		for (Double d : averageCon) {
			if (d == 0.0) {
				i++;
				continue;
			}
			total += 42 * d * cleanTotal.get(i);
			i++;
		}
		return total;
	}
	
	public final static float TYPE_SAND_RANGE = .1f;
	public final static float TOTAL_SAND_RANGE = .80f;
	
	public void Evaluate() {
		int i = 0;
		int ii = 0;
		int count = 1;
		Long totalSand = Long.valueOf(0);
		double shiftPoint = 0.0;
		Long prevSand = Long.valueOf(0);
		double subShiftPoint = 0.0;
		Double adjFactor = getActualToCalcRatio();
		Long typeTotalSand = Long.valueOf(0);
		for (Double a : averageCon) {
			if (i < stageName.size() && a != 0.0 && stageName.get(i).equals("SWEEP")) {
				typeTotalSand = typeTotalSand + Math.round(a * cleanTotal.get(i) * 42 * adjFactor);
				totalSand = totalSand + Math.round(a * cleanTotal.get(i) * 42 * adjFactor);
				shiftPoint = Double.valueOf(totalSand) / Double.valueOf(sandAmt.get(0));
				subShiftPoint = Double.valueOf(typeTotalSand) / Double.valueOf(sandAmt.get(count));
				if (i < stageName.size()) {
					stageName.remove(i);
					stageName.add(i, "SLURRY");
				} else {
					stageName.add(i, "SLURRY");
				}
				if (count == sandAmt.size()) {
					count = sandAmt.size() - 1;
				}

				if (shiftPoint >= shift.get(count - 1) - TYPE_SAND_RANGE & subShiftPoint > TOTAL_SAND_RANGE & i != lastIndex
						& count != sandAmt.size() - 1) {

					subStageSand.add(Math.round(
							(sandAmt.get(count) + prevSand) - (totalSand - (a * cleanTotal.get(i) * 42 * adjFactor))));
					prevSand = prevSand + sandAmt.get(count);
					totalSand = prevSand;
					sandTypeString.add(sandType.get(count));
					typeTotalSand = Long.valueOf(0);
					count++;
				} else if (shiftPoint > shift.get(count - 1) & i != lastIndex & count != sandAmt.size() - 1) {
					count++;
					subStageSand.add(Math.round(
							sandAmt.get(count) + prevSand - (totalSand - (a * cleanTotal.get(i) * 42 * adjFactor))));
					prevSand = prevSand + sandAmt.get(count);
					totalSand = prevSand;
					typeTotalSand = Math.round(a * cleanTotal.get(i) * 42.0 * adjFactor);
					sandTypeString.add(sandType.get(count));
				} else if (i == lastIndex) {
					if (count != sandType.size() - 1) {
						count++;
					} else if (count >= sandType.size() - 1) {
						count = sandType.size() - 1;
					}
					sandTypeString.add(sandType.get(count));
					subStageSand.add(Math.round(sandAmt.get(0))
							- (totalSand - Long.valueOf(Math.round((a * cleanTotal.get(i) * 42 * adjFactor)))));
				} else if (i == lastIndex - 1 && count < sandType.size() - 1
						&& shiftPoint < shift.get(count - 1) - TYPE_SAND_RANGE) {
					sandTypeString.add(sandType.get(count));
					subStageSand
							.add(Math.round(sandAmt.get(count) - (typeTotalSand - a * cleanTotal.get(i) * 42 * adjFactor)));
					prevSand = prevSand + sandAmt.get(count);
					totalSand = prevSand;
					count++;
				} else {
					sandTypeString.add(sandType.get(count));
					// Main.yess.mTable.setValueAt(sandType.get(count), i, 10);
					subStageSand.add(Math.round(a * cleanTotal.get(i) * 42 * adjFactor));
				}
				if(count-1==shift.size()) {
					count--;
				}
			} else {
				sandTypeString.add("");
				subStageSand.add(Long.valueOf(0));
			}

			i++;
		}
		i = 0;
		ArrayList<String> subStageSandString = new ArrayList<>();

		for (Long sand : subStageSand) {
			if (sand != 0) {
				subStageSandString.add(String.valueOf(sand));
			} else {
				subStageSandString.add("");
			}
			i++;
		}
		constructSummary.appendToMap(10, fixSandTypeString(sandTypeString));
		constructSummary.appendToMap(11, subStageSandString);
	}
	
	private ArrayList<String> fixSandTypeString(ArrayList<String> sandTypeString){
		ArrayList<String> newSandTypeString = new ArrayList<>();
		for(String s:sandTypeString) {
			Matcher matcher = Pattern.compile(ChemSandFrame.DUPLICATE_REGEX).matcher(s);
			if(matcher.find()) {
				newSandTypeString.add(s.substring(0,matcher.start()));
				continue;
			}
			newSandTypeString.add(s);
		}
		return newSandTypeString;
	}
	
	public ArrayList<String> getNameArray() {
		return this.stageName;
	}

	public void fixSandArrays(ArrayList<String> sandType, ArrayList<Long> sandAmt) {
		Integer i = 0;
		ArrayList<Integer> removeArray = new ArrayList<>();
		for (Long l : sandAmt) {
			if (l == 0l) {
				removeArray.add(i);
			}
			i++;
		}
		if (!removeArray.isEmpty()) {
			int correction = 0;
			for (Integer index : removeArray) {
				sandAmt.remove(index.intValue() - correction);
				sandType.remove(index.intValue() - correction);
				correction++;
			}
		}
		
		setSandAmtArray(sandAmt);
		setSandTypeArray(sandType);
		
	}
	
}