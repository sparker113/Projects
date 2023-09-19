import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FracCalculations {
	public static int findNextIndexWithSlope(ArrayList<String> array, int startIndex, Double lessThan,
			Double greaterThan, int avgInterval) {
		int index = array.size() - 1;
		int lastTrueIndex = -1;
		for (int i = startIndex; i < array.size(); i++) {
			if (i + avgInterval >= array.size()) {
				break;
			}
			Double y2 = getDoubleValue(array.get(i + avgInterval));
			Double y1 = getDoubleValue(array.get(i));
			double slope = (y2 - y1) / avgInterval;
			if (slope > greaterThan & slope < lessThan) {
				if (!checkSlopeBounds(getDoubleValue(array.get(i + 1 + avgInterval)), getDoubleValue(array.get(i + 1)),
						avgInterval, lessThan, greaterThan)) {
					lastTrueIndex = i;
					continue;
				}
				return i;
			}
		}
		return lastTrueIndex == -1
				? findNextIndexWithSlope(array, startIndex, lessThan+(2*(lessThan / Math.abs(lessThan))),
						greaterThan+(2*(greaterThan / Math.abs(greaterThan))), avgInterval,2)
				: lastTrueIndex;
	}
	public final static String HH_MM_SS = "HH:mm:ss";
	public final static String HH_MM = "HH:mm";
	public static String getTimeFromString(String timestamp,String pattern) {
		Matcher matcher = Pattern.compile(getTimeRegex(pattern)).matcher(timestamp);
		if(matcher.find()) {
			return matcher.group();
		}
		return "00:00";
	}
	private static String getTimeRegex(String pattern) {
		switch(pattern) {
		case(HH_MM_SS):
			return "\\d\\d?\\:\\d\\d?\\:\\d\\d?";
		case(HH_MM):
			return "\\d\\d?\\:\\d\\d?";
		}
		return "\\d\\d?\\:\\d\\d?";
	}
	public static int findNextIndexWithSlope(ArrayList<String> array, int startIndex, Double lessThan,
			Double greaterThan, int avgInterval,int iterations) {
		int index = array.size() - 1;
		int lastTrueIndex = -1;
		for (int i = startIndex; i < array.size(); i=i+avgInterval) {
			if (i + avgInterval >= array.size()) {
				break;
			}
			Double y2 = getDoubleValue(array.get(i + avgInterval));
			Double y1 = getDoubleValue(array.get(i));
			double slope = (y2 - y1) / avgInterval;
			if (slope > greaterThan & slope < lessThan) {
				if (!checkSlopeBounds(getDoubleValue(array.get(i + 1 + avgInterval)), getDoubleValue(array.get(i + 1)),
						avgInterval, lessThan, greaterThan)) {
					lastTrueIndex = i;
					continue;
				}
				System.out.println("RETURNED THE BALANCED INDEX OF: "+i+" At Value: "+y1);
				return i;
			}
		}
		System.out.println("iterations: "+iterations);
		return lastTrueIndex == -1&&iterations<5
				? findNextIndexWithSlope(array, startIndex, lessThan+(2*(lessThan / Math.abs(lessThan))),
						greaterThan+(2*(greaterThan / Math.abs(greaterThan))), avgInterval,iterations+1)
				: lastTrueIndex;
	}

	public static boolean checkSlopeBounds(Double y2, Double y1, int avgInterval, Double lessThan, Double greaterThan) {
		double slope = (y2 - y1) / avgInterval;
		if (slope > greaterThan & slope < lessThan) {
			return true;
		}
		return false;
	}

	public static Double getDoubleValue(String numString) {
		Matcher matcher = Pattern.compile("\\d+").matcher(numString);
		if (matcher.find()) {
			return Double.valueOf(matcher.group());
		}
		return 0.0;
	}

	public static Double calculateHorsePower(Double pressure, Double rate) {
		return (pressure * rate) / 40.8;
	}

	public static Double calculateFracGradient(Double tvd, Double isip) {
		System.out.println("TVD: " + tvd + "\n ISIP: " + isip);
		double fracPressure = 0.052 * 8.33 * tvd + isip;

		return fracPressure / tvd;
	}

	public static Double calculateStdDev(ArrayList<String> array) {
		Double variance = calculateVariance(array);
		System.out.println("Standard Deviation = " + Math.sqrt(variance));
		return Math.sqrt(variance);
	}

	public static Double calculateStdDev(ArrayList<String> array, Double average) {
		Double variance = calculateVariance(array);
		System.out.println("Standard Deviation = " + Math.sqrt(variance));
		return Math.sqrt(variance);
	}

	public static Double calculateVariance(ArrayList<String> array) {
		Double avg = UserDefinedFrame.avg(array);
		double sum = 0.0;
		for (String s : array) {
			if(s.matches("([\\w\\s]+)")) {
				continue;
			}
			sum += Math.pow(Math.abs((avg - Double.valueOf(s))), 2.0);
		}
		System.out.println("Variance = " + (sum / Double.valueOf(array.size())));
		return sum / Double.valueOf(array.size());
	}

	public static Double calculateVariance(ArrayList<String> array, Double average) {
		double sum = 0.0;
		for (String s : array) {
			sum += Math.pow(Math.abs((average - Double.valueOf(s))), 2.0);
		}
		System.out.println("Variance = " + (sum / Double.valueOf(array.size())));
		return sum / Double.valueOf(array.size());
	}

	public static CompletableFuture<Double> getMaxCalculation(ArrayList<String> propCon, Double roundMult) {
		CompletableFuture<Double> max = new CompletableFuture<>();
		Executors.newSingleThreadExecutor().execute(() -> {
			Double maximum = 0.0;
			for (String s : propCon) {
				maximum = Double.valueOf(s) > maximum ? Double.valueOf(s) : maximum;
			}
			maximum = Double.valueOf(Math.round(maximum / roundMult)) * roundMult;
			max.complete(maximum);
		});
		return max;
	}

	public static <T> ArrayList<T> getArrayFromSet(HashMap<String, HashSet<T>> mapSet) {
		ArrayList<T> array = new ArrayList<>();
		for (String s : mapSet.keySet()) {
			for (T t : mapSet.get(s)) {
				array.add(t);
			}
		}
		return array;
	}

	public final static int GREATER = 1;
	public final static int LESSER = -1;

	public static <T> boolean checkSubsequentValues(ArrayList<T> array, Float value, int numIndeces, int start,
			int greaterLesser) {
		if (numIndeces + start >= array.size()) {
			numIndeces = array.size() - start - 1;
		}
		for (int i = start; i < start + numIndeces; i++) {
			Matcher matcher = Pattern.compile("[^\\d\\.\\-]").matcher(String.valueOf(array.get(i)));
			if (matcher.find() || Float.valueOf(String.valueOf(array.get(i))) * (greaterLesser) <= value
					* (greaterLesser)) {
				System.out.print(Float.valueOf(String.valueOf(array.get(i))) * (greaterLesser));
				System.out.print(" - ");
				System.out.println(value * (greaterLesser));

				return false;
			}
		}
		return true;
	}

	public static <T, V> LinkedHashMap<T, V> getMapWithExchangedKeys(Map<T, HashSet<T>> mapWithKey,
			Map<T, V> mapWithValue) {
		LinkedHashMap<T, V> newMap = new LinkedHashMap<>();
		for (T t : mapWithKey.keySet()) {
			for (T tt : mapWithKey.get(t)) {
				if (!mapWithValue.containsKey(tt)) {
					continue;
				}
				newMap.put(t, mapWithValue.get(tt));
			}
		}
		return newMap;
	}

	public static Double calcTotalFromSetPoint(ArrayList<String> setPointData, Double cleanTotal) {
		Double average = UserDefinedFrame.avg(setPointData);
		double mGal = cleanTotal * 42.0 / 1000.0;
		return average * mGal;
	}

	public static Double calcTotalFromSetPoint(ArrayList<String> setPointData, Double cleanTotal,
			ArrayList<String> cleanRate, Double greaterThan) {
		Double average = UserDefinedFrame
				.avg(UserDefinedFrame.getArrayWithGreaterThanCondition(setPointData, cleanRate, 2.0));
		double mGal = cleanTotal * 42.0 / 1000.0;
		return average * mGal;
	}

	public static Double getTotal(ArrayList<String> totalChannelData) {
		Double total = 0.0;
		for (int i = findWhenTotalsReset(totalChannelData) + 1; i < totalChannelData.size() - 1; i++) {
			if (Double.valueOf(totalChannelData.get(i)) < Double.valueOf(totalChannelData.get(i - 1))) {
				total += Double.valueOf(totalChannelData.get(i - 1));
			}
		}
		total += Double.valueOf(totalChannelData.get(totalChannelData.size() - 1));
		total = getDoubleRoundedDouble(total, 0);
		return total;
	}

	public static int findWhenTotalsReset(ArrayList<String> totalChannelData) {
		int i = 0;
		for (String s : totalChannelData) {
			if (Double.valueOf(s) < 250.0) {
				return i;
			}
			i++;
		}
		return 1;
	}

	public static String getDoubleRoundedString(Double value, int places) {
		Long valueLong = Math.round(value * (Math.pow(10.0, Double.valueOf(places))));
		return String.valueOf(Double.valueOf(valueLong / (Math.pow(10.0, Double.valueOf(places)))));
	}

	public static Double getDoubleRoundedDouble(Double value, int places) {
		Long valueLong = Math.round(value * (Math.pow(10.0, Double.valueOf(places))));
		return Double.valueOf(valueLong / (Math.pow(10.0, Double.valueOf(places))));
	}

	public static ArrayList<Long> getArrayOfLongFromMap(Map<String, String> map) {
		ArrayList<Long> array = new ArrayList<>();
		for (Object s : map.keySet()) {
			Matcher matcher = Pattern.compile("[\\d\\.]+").matcher(String.valueOf(map.get(s)));
			if (matcher.find()) {
				String found = matcher.group();
				array.add(Math.round(Double.valueOf(matcher.group())));
				System.out.println(found);
			}
		}
		return array;
	}
	public static ArrayList<String> getArrayOfStringValues(Map<String,String> map){
		ArrayList<String> array = new ArrayList<>();
		for(String s:map.keySet()) {
			if(map.get(s).equals("null")) {
				continue;
			}
			array.add(map.get(s));
		}
		return array;
	}
	public static ArrayList<String> getArrayOfStringKeys(Map<String, ?> map) {
		ArrayList<String> array = new ArrayList<>();
		for (String s : map.keySet()) {
			array.add(s);
		}
		return array;
	}

	public static ArrayList<String> getArrayOfStringKeys(Map<String, String> map, String escDuplicateRegex) {
		ArrayList<String> array = new ArrayList<>();
		for (String s : map.keySet()) {
			Matcher matcher = Pattern.compile(escDuplicateRegex).matcher(s);
			if (matcher.find()) {
				array.add(s.substring(0, matcher.start()).trim());
				continue;
			}
			array.add(s);
		}
		return array;
	}

	public static Double roundDoubleToMult(Double dub, Double mult) {
		return Double.valueOf(Math.round(dub / mult)) * mult;
	}
}
