import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

public class DataMapJSon {

	private static final long serialVersionUID = 1L;
	private TreeMap<String, ArrayList<String>> dataMap = new TreeMap<>();
	private HashMap<Integer, String> channelIndeces = new HashMap<>();
	private channelData channels;
	private HashMap<String, String> channelList;
	private String lastIndex;
	private Semaphore writeQueue = new Semaphore(1);
	private Semaphore readQueue = new Semaphore(0);
	private HashMap<Integer, String> lastValues;
	private Queue<String> dataQueue;
	private Boolean locked;
	private HashMap<String, String> chemNameMap;
	private Boolean stageRequest;

	DataMapJSon() {
		this.stageRequest = true;
		this.chemNameMap = new HashMap<>();
	}

	DataMapJSon(channelData channels, HashMap<String, String> channelList) {
		this.channels = channels;
		this.channelList = channelList;
		this.dataQueue = new LinkedList<>();
		this.stageRequest = true;
		this.locked = false;
	}

	DataMapJSon(HashMap<String, String> channelList) {
		this.stageRequest = false;
		this.channelList = channelList;
		this.dataQueue = new LinkedList<>();
	}

	public void populateQueue(String data) {
		dataQueue.offer(data);
	}

	public Queue<String> getQueue() {
		return this.dataQueue;
	}

	public CompletableFuture<String> emptyQueue() {
		CompletableFuture<String> newData = new CompletableFuture<>();
		while (dataQueue.peek() == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		return newData.completeAsync(() -> dataQueue.poll());
	}

	public void initialJSon(Scanner scan) {

		scan.useDelimiter("\\[");
		String temp;
		int i = 0;
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File("tempData.txt")));
			while (scan.hasNext()) {
				temp = scan.next();
				bufferedWriter.append(temp);
				bufferedWriter.append("\n");
				if (temp.contains("equipment")) {
					break;
				}
				if (i == 3) {
					temp.replace("\"", "");
					temp = temp.split("]")[0];
					populateQueue(temp);
					int channelCount = 0;
					mapChannelArrays(temp);

				} else if (i > 3 && !temp.equals("")) {
					populateQueue(temp);
					populateArrays(temp);
				}

				i++;
			}
		} catch (IOException e) {
		}
		try {
			setValues();
		} catch (NullPointerException e) {
			System.out.println("JOptionPane DataMapJSon");
			// JOptionPane.showMessageDialog(null, "Data failed to upload to FracPro");
			return;
		}
		setLastIndex(dataMap.get("elapsedTime").get(dataMap.get("elapsedTime").size() - 1));
		scan.close();
	}

	public void addJSon(Scanner scan) {
		scan.useDelimiter("\\[");
		String temp;
		int i = 0;

		while (scan.hasNext()) {
			temp = scan.next();

			if (temp.contains("equipment")) {
				break;
			}

			if (i > 3 && !temp.equals("")) {
				temp.replace("\"", "");
				temp = temp.split("]")[0];
				populateQueue(temp);
				populateArrays(temp);
			}
			i++;
		}

		setValues();
		setLastIndex(dataMap.get("elapsedTime").get(dataMap.get("elapsedTime").size() - 1));
		scan.close();
	}

	public void setValues() {
		HashMap<Integer, String> temp = new HashMap<>();
		int lastIndex = dataMap.get("time").size() - 1;
		for (Integer i : channelIndeces.keySet()) {
			temp.put(i, dataMap.get(channelIndeces.get(i)).get(lastIndex));
		}
		setLastValues(temp);
	}

	public void mapChannelArrays(String temp) {
		channelIndeces.put(0, "time");
		channelIndeces.put(1, "elapsedTime");
		dataMap.put("time", new ArrayList<String>());
		dataMap.put("elapsedTime", new ArrayList<String>());
		if (stageRequest) {
			setStructure(temp);
		} else {
			setStructureAddRequest(temp);
		}
	}

	private void setStructureAddRequest(String temp) {
		int channelCount = 0;
		for (String a : temp.split(",")) {

			if (channelCount > 1) {
				for (String b : channelList.keySet()) {
					// Matcher matcher = Pattern.compile("\\d\\d").matcher(a);
					if (channelList.get(b).equals(a.replace("\"", ""))) {
						dataMap.put(b, new ArrayList<String>());
						channelIndeces.put(channelCount, b);
						break;
					}
				}
			}
			channelCount++;
		}
	}

	private void setStructure(String temp) {
		int channelCount = 0;

		for (String a : temp.split(",")) {

			if (channelCount > 1) {
				for (String b : channelList.keySet()) {
					// Matcher matcher = Pattern.compile("\\d\\d").matcher(a);
					if (channels.getcOName(a.replace("\"", "")).toUpperCase()
							.contains(channelList.get(b).toUpperCase())) {
						dataMap.put(b, new ArrayList<String>());
						if (channelIndeces.keySet().contains(channelCount)) {
							channelIndeces.put(-channelCount, b);
						} else {
							channelIndeces.put(channelCount, b);
						}
					}
				}
			}
			channelCount++;
		}
		if (stageRequest && !dataMap.keySet().contains("Stage Number")) {
			System.out.println("JOptionPane DataMapJSon - DataMapJSon::setStructure");
			JOptionPane.showMessageDialog(null, "Double check your stage number channel name", "Channel Not Found",
					JOptionPane.WARNING_MESSAGE);
			locked = true;
			return;
		}
	}

	public Boolean isLocked() {
		return this.locked;
	}

	public static HashMap<String, HashMap<String, String>> wellPropertiesMap(Scanner scan, String[] identifiers) {
		HashMap<String, HashMap<String, String>> wellMap = new HashMap<>();
		HashMap<String, String> tempMap = new HashMap<>();
		scan.useDelimiter(",");
		String temp;
		boolean open = false;
		String wellName = "errorWell";
		while (scan.hasNext()) {
			temp = scan.next();
			if (temp.contains("{")) {
				open = true;
			}
			if (open) {

				if (temp.contains("}")) {
					open = false;
					wellMap.put(wellName, tempMap);
					tempMap = new HashMap<>();
					open = false;
				}
				for (String a : identifiers) {
					if (temp.split(":")[0].replace("\"", "").equals(a)) {
						tempMap.put(a, temp.split(":")[1].replace("\"", ""));
						break;
					}
				}
				if (temp.contains("wellName")) {
					wellName = temp.split(":")[1].replace("\"", "");

				}
			}
		}

		return wellMap;
	}

	public void populateArrays(String temp) {
		Matcher match = Pattern.compile("\\]").matcher(temp);
		while (match.find()) {
			temp = temp.replaceAll(match.pattern().toString(), "");
		}
		int i = 0;
		String lIndex = getLastIndex();
		if (lIndex == null) {
			lIndex = "1";
		}

		for (String a : temp.split(",")) {

			if (Integer.valueOf(lIndex) < Integer.valueOf(temp.split(",")[1])) {

				dataMap.get(channelIndeces.get(i)).add(a);
				if (channelIndeces.keySet().contains(-i) && i != 0) {
					dataMap.get(channelIndeces.get(-i)).add(a);
				}
				i++;
			} else {
				break;
			}
		}

	}

	public final static String JOB_TIME = "Job Time";
	public final static String REDUCED_JOB_TIME = "jobtime";

	@SuppressWarnings("resource")
	public static LinkedHashMap<String, ArrayList<String>> parseCSVData(String path) throws IOException {
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		try {
			fileReader = new FileReader(path);
			bufferedReader = new BufferedReader(fileReader);
		} catch (IOException e) {
			System.out.println("parseCSVData IOException");
			return null;
		}
		LinkedHashMap<String, ArrayList<String>> map = new LinkedHashMap<>();
		HashMap<String, String> channels = ChannelPane.getChannelList("csvchannels.txt");
		String temp;
		Integer count;
		HashMap<Integer, String> indeces = new HashMap<>();
		Boolean checkChannel;
		channels.put("Job Time", "Job Time");
		map.put("elapsedTime", new ArrayList<String>());
		temp = bufferedReader.readLine();
		for (String a : channels.keySet()) {
			count = 0;
			checkChannel = false;
			if (channels.get(a).equals("null") || channels.get(a).equals("")) {
				continue;
			}
			for (String b : temp.split(",")) {

				if (b.toUpperCase().replace(" ", "").equals(channels.get(a).toUpperCase().replace(" ", ""))) {
					indeces.put(count, a);
					map.put(a, new ArrayList<String>());
					checkChannel = true;
					break;
				}
				count++;
			}
			if (!checkChannel) {

				JOptionPane.showMessageDialog(Main.yess, "Check your channel Names: " + a);
				return null;
			}
		}
		bufferedReader.readLine();
		Integer index = 1;
		map.get("elapsedTime").add("Elapsed Time");
		int maxIndex = getMaxIndex(indeces.keySet());
		while ((temp = bufferedReader.readLine()) != null) {
			if (temp.split(",").length < maxIndex) {
				continue;
			}
			for (Integer i : indeces.keySet()) {
				if (indeces.get(i).equals(JOB_TIME)) {
					addToJobTime(temp.split(",")[i], map);
					continue;
				}
				map.get(indeces.get(i)).add(temp.split(",")[i]);
			}
			map.get("elapsedTime").add(String.valueOf(index));
			index++;
		}
		for (Integer i : indeces.keySet()) {
			System.out.println(i + " - " + indeces.get(i));
		}
		bufferedReader.close();
		return map;
	}

	public static void addToJobTime(String dateTime, HashMap<String, ArrayList<String>> map) {
		if (map.get(JOB_TIME).isEmpty()) {
			map.get(JOB_TIME).add(getFormattedDateTime(dateTime));
			return;
		}
		String lastDateTime = map.get(JOB_TIME).get(map.get(JOB_TIME).size() - 1);
		map.get(JOB_TIME).add(parseDateTime(lastDateTime).plusSeconds(1l).format(DATE_TIME_FORMAT));
	}
	public final static LocalDateTime parseDateTime(String dateTime) {
		if(dateTime.regionMatches(10, " ", 0, 1)) {
			return LocalDateTime.parse(dateTime.replace(" ", "T"));
		}
		return LocalDateTime.parse(dateTime);
	}
	public final static DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss");

	public static String getProperDate(String dateTime) {
		Matcher matcher = Pattern.compile(PROPER_DATE_PATTERN).matcher(dateTime);
		if (matcher.find()) {
			return addFullDigitsToDate(matcher.group());
		}
		return NOT_PROPER_DATE;
	}

	public static String addFullDigitsToDate(String dateString) {
		Matcher matcher = Pattern.compile("\\d+").matcher(dateString);
		while (matcher.find()) {
			String found = matcher.group();
			if (found.length() == 1) {
				found = "0" + found;
				dateString = dateString.substring(0, matcher.start()) + found
						+ dateString.substring(matcher.end(), dateString.length());
			}
		}
		return dateString;
	}

	public static String IMPROPER_DATE_PATTERN = "\\d\\d?/\\d\\d?/\\d{4}";
	public static String PROPER_DATE_PATTERN = "\\d{4}\\-\\d\\d?\\-\\d\\d?";
	public static int PROPER_DATE_INDEX = 1;
	public static int IMPROPER_DATE_INDEX = 2;
	public static String NO_DATE_FOUND = "es no en biblioteca";

	public static String getDatePattern(String dateTime) {
		Matcher matcher = Pattern.compile("(" + PROPER_DATE_PATTERN + ")|(" + IMPROPER_DATE_PATTERN + ")")
				.matcher(dateTime);
		if (matcher.find()) {
			return matcher.group(PROPER_DATE_INDEX) == null ? IMPROPER_DATE_PATTERN : PROPER_DATE_PATTERN;
		}
		return NO_DATE_FOUND;
	}

	public final static String NOT_PROPER_DATE = "NO NO NOO";

	public static String getDateTimeNowFormatted() {
		return LocalDateTime.now().format(DATE_TIME_FORMAT);
	}

	public static String getFormattedDateTime(String dateTime) {
		String datePattern = getDatePattern(dateTime);
		String dateString = datePattern.equals(PROPER_DATE_PATTERN) ? getProperDate(dateTime)
				: (datePattern.equals(IMPROPER_DATE_PATTERN) ? getDateFromDateTime(dateTime)
						: getDateTimeNowFormatted());
		String timeString = addFullDigitsToDate(getTimeFromDateTime(dateTime));
		return dateString + " " + timeString;
	}

	public static String reOrgDateTime(String dateTime) {
		String dateString = getDateFromDateTime(dateTime);
		String timeString = getTimeFromDateTime(dateTime);
		return dateString + " " + timeString;
	}

	/*
	 * USED FOR DATES THAT HAVE THE FORMAT MM/DD/YYYY HH:mm:(ss?)
	 */
	public static String getTimeFromDateTime(String dateTime) {
		Matcher timeMatcher = Pattern.compile("\\d\\d?\\:\\d\\d?((\\:\\d\\d?)?)").matcher(dateTime);
		String timeString = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
		if (timeMatcher.find()) {
			String found = timeMatcher.group();
			timeString = found.matches("\\d{2}\\:\\d{2}\\:\\d{2}") ? found : found + ":00";
		}
		return timeString;
	}

	private final static int MONTH_INDEX = 0;
	private final static int DAY_INDEX = 1;

	/*
	 * USED FOR DATES THAT HAVE THE FORMAT MM/DD/YYYY HH:mm:(ss?)
	 */
	private static String[] getMonthDayArray(String dateTime) {
		String[] monthDayArray = new String[2];
		Matcher matcher = Pattern.compile("\\d\\d?/").matcher(dateTime);
		int i = 0;
		while (i < monthDayArray.length && matcher.find()) {
			String found = matcher.group();
			monthDayArray[i] = found.substring(0, found.length() - 1);
			i++;
		}
		return monthDayArray;
	}

	private static String getDateFromDateTime(String dateTime) {
		String[] monthDayArray = getMonthDayArray(dateTime);
		String year = getYearFromDateTime(dateTime);
		return year + "-" + getTwoDigits(monthDayArray[MONTH_INDEX]) + "-" + getTwoDigits(monthDayArray[DAY_INDEX]);
	}

	private static String getTwoDigits(String intString) {
		String fullString = "0" + intString;
		return fullString.substring(fullString.length() - 2, fullString.length());
	}

	private static String getYearFromDateTime(String dateTime) {
		Matcher matcher = Pattern.compile("\\d{4}").matcher(dateTime);
		if (matcher.find()) {
			return matcher.group();
		}
		return LocalDate.now().format(DateTimeFormatter.ofPattern("YYYY"));
	}

	public static int getMaxIndex(Set<Integer> indecesKeys) {
		int[] max = new int[1];
		indecesKeys.forEach((Integer index) -> {
			if (max[0] == 0) {
				max[0] = index;
				return;
			}
			max[0] = max[0] < index ? index : max[0];
		});
		return max[0];
	}

	public synchronized HashMap<String, ArrayList<String>> parseCSVChemData(String path, ArrayList<String> chemChannels)
			throws IOException {
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;

		try {
			fileReader = new FileReader(path);
			bufferedReader = new BufferedReader(fileReader);
		} catch (IOException e) {
			System.out.println("parseCSVData IOException");
			return null;
		}
		HashMap<String, ArrayList<String>> map = new HashMap<>();
		HashMap<String, String> channels = ChannelPane.getChannelList();
		String temp;
		Integer count;
		HashMap<Integer, String> indeces = new HashMap<>();
		temp = bufferedReader.readLine();

		for (String a : chemChannels) {
			count = 0;
			for (String b : temp.split(",")) {

				if (b.toUpperCase().replace(" ", "").replace("-", "")
						.contains(a.toUpperCase().replace(" ", "").replace("-", ""))) {
					indeces.put(count, b);
					map.put(b, new ArrayList<String>());
					break;
				}
				count++;
			}
		}
		bufferedReader.readLine();
		while ((temp = bufferedReader.readLine()) != null) {
			for (Integer i : indeces.keySet()) {
				map.get(indeces.get(i)).add(temp.split(",")[i]);
			}
		}
		bufferedReader.close();
		return map;
	}

	public synchronized void setChannelNameMap(HashMap<String, String> chemNameMap) {
		this.chemNameMap = chemNameMap;
	}

	public synchronized HashMap<String, String> getChannelNameMap() {
		return this.chemNameMap;
	}

	public void setLastIndex(String lastIndex) {
		this.lastIndex = lastIndex;
	}

	public String getLastIndex() {
		return this.lastIndex;
	}

	public void setLastValues(HashMap<Integer, String> lastValues) {
		this.lastValues = lastValues;
	}

	public HashMap<Integer, String> getChannelIndeces() {
		return this.channelIndeces;
	}

	public HashMap<Integer, String> getLastValues() {
		return this.lastValues;
	}

	public TreeMap<String, ArrayList<String>> getDataMap() {
		return this.dataMap;
	}
}
