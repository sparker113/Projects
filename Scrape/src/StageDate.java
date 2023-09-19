import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StageDate extends Thread {
	private ArrayList<String> date;
	private ArrayList<Integer> stageUp;
	private TreatmentSummary constructSummary;
	private ArrayList<String> stTimes = new ArrayList<>();
	private ArrayList<String> stDates = new ArrayList<>();
	private ArrayList<String> endTimes = new ArrayList<>();
	private ArrayList<String> endDates = new ArrayList<>();
	private HashMap<String, String> stageTimes = new HashMap<>();
	private HashMap<String,String> trueTimes;
	private CompletableFuture<Double> time;
	private PumpTime pumpTime;
	private Boolean timeSet;
	private Boolean setOffset;

	public StageDate(ArrayList<String> date, ArrayList<Integer> stageUp, HashMap<String, String> stageTimes) {
		this.date = date;
		this.stageUp = stageUp;
		this.stageTimes = stageTimes;
		this.timeSet = false;
		this.setOffset = true;
	}

	public StageDate(ArrayList<String> date, ArrayList<Integer> stageUp, HashMap<String, String> stageTimes,
			Boolean offset) {
		this.date = date;
		this.stageUp = stageUp;
		this.stageTimes = stageTimes;
		this.timeSet = false;
		this.setOffset = offset;
	}
	public StageDate(ArrayList<String> date, ArrayList<Integer> stageUp, HashMap<String, String> stageTimes,
			HashMap<String,String> trueTimes) {
		this.date = date;
		this.stageUp = stageUp;
		this.stageTimes = stageTimes;
		this.timeSet = false;
		this.setOffset = true;
		this.trueTimes = trueTimes;
	}

	@Override
	public synchronized void run() {
		final Long offset = Long.valueOf(TimeZone.getDefault().getOffset(System.currentTimeMillis()))
				/ Long.valueOf(1000);
		Long timeOffset = offset;
		DateTimeFormatter dateTimes = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		DateTimeFormatter dates = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		DateTimeFormatter times = DateTimeFormatter.ofPattern("HH:mm");
		ArrayList<LocalDateTime> startDateArray = new ArrayList<>();
		ArrayList<LocalDateTime> endDateArray = new ArrayList<>();
		HashMap<String, String> lastStartClose = trueTimes==null?Main.yess.lastStartClose.getDateTimesWithT():trueTimes;
		correctTimes();
		if (stageTimes.containsKey("Stage_Open")&&!stageTimes.get("Stage_Open").equals("")) {
			timeOffset = getTimeOffset();
		}
		Integer count = 0;
		for (Integer i : stageUp) {
			if (count == stageUp.size() - 1) {
				break;
			}
			LocalDateTime startDate;
			LocalDateTime endDate;
			if (count == 0) {
				startDate = LocalDateTime.parse(lastStartClose.get("open"));
				endDate = formatDateTime(date.get(stageUp.get(1)));
			} else {
				startDate = formatDateTime(checkTimes(date.get(stageUp.get(count))));
				endDate = formatDateTime(checkTimes(date.get(stageUp.get(count + 1))));
			}
			startDateArray.add(startDate);
			endDateArray.add(endDate);
			this.stDates.add(startDate.format(dates));
			this.stTimes.add(startDate.format(times));
			this.endDates.add(endDate.format(dates));
			this.endTimes.add(endDate.format(times));
			count++;
		}
		if (timeOffset != offset) {
			this.endTimes.remove(this.endTimes.size() - 1);
			this.endTimes.add(stageTimes.get("Stage_Close"));
		}
		this.pumpTime = new PumpTime(startDateArray, endDateArray);
		Executors.newSingleThreadExecutor().execute(() -> this.pumpTime.calcPumptime());

	}

	public synchronized PumpTime getPumptime() {
		return this.pumpTime;
	}

	private void correctTimes() {
		if (!date.get(0).contains("  ")) {
			return;
		}
		ArrayList<String> newDate = new ArrayList<>();
		for (String s : date) {
			Matcher matcher = Pattern.compile("\\w+").matcher(s);
			if (matcher.find()) {
				String newString = s.replace(matcher.group(), " ");
				newDate.add(newString);
				continue;
			}
			newDate.add(s);
		}
		date = newDate;
	}

	private String getDowntime() {
		LocalDateTime startDateTime = LocalDateTime.parse(getStartDate() + "T" + getStartTime());
		LocalDateTime endDateTime = LocalDateTime.parse(getEndDate() + "T" + getEndTime());
		double totalStageTime = Double.valueOf(Duration.between(startDateTime, endDateTime).getSeconds())
				/ Double.valueOf(60);
		Double downtimeDouble = Math.round((totalStageTime - this.pumpTime.getPumptime()) * Double.valueOf(100))
				/ Double.valueOf(100);
		String downtime;
		if (downtimeDouble < Double.valueOf(1)) {
			downtime = String.valueOf(0.0);
		} else {
			downtime = String.valueOf(downtimeDouble);
		}
		return downtime;
	}

	private String checkTimes(String dateTime) {
		Matcher matcher = Pattern.compile("\\d{2}:\\d{2}:\\d{2}").matcher(dateTime);
		if (matcher.find()) {
			return dateTime;
		}
		return dateTime + ":00";
	}

	public synchronized Double getDowntime(ArrayList<String> nameArray) {
		int count = 0;
		double downtimeSec = 0.0;
		for (String s : nameArray) {
			if (s.equals("SHUTDOWN")) {
				LocalDateTime startShutdown = LocalDateTime.parse(stDates.get(count) + "T" + stTimes.get(count));
				LocalDateTime endShutdown = LocalDateTime.parse(endDates.get(count) + "T" + endTimes.get(count));
				downtimeSec += Double.valueOf(Duration.between(startShutdown, endShutdown).getSeconds());
			}
			count++;
		}
		return downtimeSec / Double.valueOf(60);
	}

	public synchronized void addSigDateTimeValues(SetDiagnostics setDiagnostics, ArrayList<String> nameArray) {
		System.out.println(nameArray);
		setDiagnostics.put("Start Time", getStartTime());
		setDiagnostics.put("Start Date", getStartDate());
		setDiagnostics.put("End Time", getEndTime());
		setDiagnostics.put("End Date", getEndDate());
		setDiagnostics.put("Pump Time", String.valueOf(getPumptime().getPumptime() - getDowntime(nameArray)));
		setDiagnostics.put("Downtime", FracCalculations.getDoubleRoundedString(getDowntime(nameArray), 2));
	}

	public synchronized String getStartDate() {
		return this.stDates.get(0);
	}

	public synchronized String getStartTime() {
		return this.stTimes.get(0);
	}

	public synchronized String getEndDate() {
		return this.endDates.get(endDates.size() - 1);
	}

	public synchronized String getEndTime() {
		return this.endTimes.get(endTimes.size() - 1);
	}

	public LocalDateTime formatDateTime(String time) {
		LocalDateTime startDate = null;
		DateTimeFormatter dateTimes = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		if (time.contains("T")) {
			startDate = LocalDateTime.parse(
					time.split("\\.")[0].split("T")[0].replace("\"", "") + " " + time.split("\\.")[0].split("T")[1],
					dateTimes);
		} else {
			try {
				System.out.println(time);
				String newTime = getFormattedTime(time);
				startDate = LocalDateTime.parse(newTime);
			} catch (ArrayIndexOutOfBoundsException | DateTimeParseException e) {
				e.printStackTrace();
				try {
					startDate = LocalDateTime.parse(time, dateTimes);
				}catch(DateTimeParseException e1) {
					return LocalDateTime.of(getDateFromString(time),LocalTime.of(0, 0));
				}
				return startDate;
			}
		}
		return startDate;
	}
	public static LocalDate getDateFromString(String time) {
		Matcher matcher = Pattern.compile("\\d{4}\\-\\d{2}\\-\\d{2}").matcher(time);
		if(matcher.find()) {
			return LocalDate.parse(matcher.group());
		}
		return LocalDate.now();
	}
	public static String getFormattedTime(String time) throws ArrayIndexOutOfBoundsException {
		return getFormatDate(time) + "T" + getTime(time);
	}

	private static String getTime(String dateTime) {
		Matcher matcher = Pattern.compile("\\d{2}\\:\\d{2}((\\:\\d{2})?)").matcher(dateTime);
		if (matcher.find()) {
			return matcher.group().matches("\\d{2}\\:\\d{2}\\:\\d{2}") ? matcher.group() : matcher.group() + ":00";
		}
		return dateTime;
	}

	private static String getFormatDate(String dateTime) {
		HashMap<String,String> map = getDateMap(dateTime);
		if(map.isEmpty()) {
			return LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY-MM-dd"));
		}
		return map.get(YEAR)+"-"+map.get(MONTH)+"-"+map.get(DAY);
	}
	private final static int YEAR_MDY = 2;
	private final static int MONTH_MDY = 0;
	private final static int DAY_MDY = 1;

	private final static int YEAR_YMD = 0;
	private final static int DAY_YMD = 2;
	private final static int MONTH_YMD = 1;

	private final static String YEAR = "year";
	private final static String DAY = "day";
	private final static String MONTH = "month";
	private static HashMap<String,String> getDateMap(String dateTime){
		Matcher matcher = Pattern.compile("\\d{2}\\/\\d{2}\\/\\d{4}").matcher(dateTime);
		if(matcher.find()) {
			String date = matcher.group();
			return getMDYMap(date);
		}
		return getYMDMap(dateTime);
	}
	private static HashMap<String,String> getYMDMap(String dateTime){
		Matcher matcher = Pattern.compile("\\d{4}\\-\\d{2}-\\d{2}").matcher(dateTime);
		HashMap<String,String> map = new HashMap<>();
		if(matcher.find()) {
			String date = matcher.group();
			matcher = Pattern.compile("\\d+").matcher(date);
			int i = 0;
			while(matcher.find()&&i<3) {
				switch(i) {
				case YEAR_YMD:
					map.put(YEAR, matcher.group());
					i++;
					break;
				case MONTH_YMD:
					map.put(MONTH, matcher.group());
					i++;
					break;
				case DAY_YMD:
					map.put(DAY, matcher.group());
					i++;
					break;
				}
			}
		}
		return map;
	}
	private static HashMap<String,String> getMDYMap(String date){
		HashMap<String,String> map = new HashMap<>();
		Matcher matcher = Pattern.compile("\\d+").matcher(date);
		int i = 0;
		while(matcher.find()&&i<3) {
			switch(i) {
			case YEAR_MDY:
				map.put(YEAR, matcher.group());
				i++;
				break;
			case MONTH_MDY:
				map.put(MONTH, matcher.group());
				i++;
				break;
			case DAY_MDY:
				map.put(DAY, matcher.group());
				i++;
				break;
			}
		}
		return map;
	}
	@Deprecated
	private static ArrayList<String> getDateComps(String date) {
		ArrayList<String> array = new ArrayList<>();
		Matcher matcher = Pattern.compile("\\d+").matcher(date);
		while (matcher.find()) {
			array.add(matcher.group());
		}
		return array;
	}
	@Deprecated
	private static String getDate(String dateTime) {
		Matcher matcher = Pattern.compile("\\d{2}/\\d{2}/\\d{4}").matcher(dateTime);
		if (matcher.find()) {
			return matcher.group();
		}
		return dateTime;
	}

	public Long getTimeOffset() {
		DateTimeFormatter dateTimes = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime reportedOpenDate = formatDateTime(date.get(stageUp.get(0)));
		LocalDateTime temp = reportedOpenDate
				.plusSeconds((TimeZone.getDefault().getOffset(System.currentTimeMillis()) / Long.valueOf(1000)));
		String theDay = getDayFormatted(temp);
		String theMonth = getMonthFormatted(temp);

		LocalDateTime openDate = LocalDateTime.parse(Main.yess.lastStartClose.getDateTimes().get("open").split(" ")[0]
				+ " " + stageTimes.get("Stage_Open") + ":00", dateTimes);
		Duration duration = Duration.between(reportedOpenDate, openDate);
		Long timeOffset = duration.getSeconds();
		/*
		 * if(timeOffset < 0) { timeOffset =
		 * checkNegativeOffset(reportedOpenDate,stageTimes.get("Stage_Open")+":00"); }
		 */
		return timeOffset;
	}

	public String fixEndTime() {
		DateTimeFormatter dateTimes = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		DateTimeFormatter dates = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		Long timeOffset = Long.valueOf(0);

		LocalDateTime reportedStartFlush = LocalDateTime
				.parse(stDates.get(stDates.size() - 1) + "T" + stTimes.get(stTimes.size() - 1) + ":00");
		String theDay = getDayFormatted(reportedStartFlush);
		String theMonth = getMonthFormatted(reportedStartFlush);
		LocalDateTime closeDate = LocalDateTime.parse(String.valueOf(reportedStartFlush.getYear()) + "-" + theMonth
				+ "-" + theDay + " " + stageTimes.get("Stage_Open") + ":00", dateTimes);
		Duration duration = Duration.between(reportedStartFlush, closeDate);
		if (duration.getSeconds() < 0) {
			timeOffset = checkNegativeOffset(reportedStartFlush, stageTimes.get("Stage_Close") + ":00");
		} else {
			timeOffset = duration.getSeconds();
		}
		DateTimeFormatter dates2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		return reportedStartFlush.plusSeconds(timeOffset).format(dates2);
	}

	public String getMonthFormatted(LocalDateTime localDateTime) {
		String theMonth = "";
		if (localDateTime.getMonthValue() < 10) {
			theMonth = "0" + String.valueOf(localDateTime.getMonthValue());
		} else {
			theMonth = String.valueOf(localDateTime.getMonthValue());
		}
		return theMonth;
	}

	public String getDayFormatted(LocalDateTime localDateTime) {
		String addZero = String.valueOf("0" + String.valueOf(localDateTime.getDayOfMonth()));
		String theDay = addZero.substring(addZero.length() - 2);
		return theDay;
	}

	public Long checkNegativeOffset(LocalDateTime date1, String time) {
		DateTimeFormatter dateTimes = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		Long timeOffset = Long.valueOf(0);
		String theMonth = getMonthFormatted(date1);
		Integer theMonthInt = date1.getMonthValue() + 1;
		String theMonth2 = "";
		if (theMonthInt < 10) {
			theMonth2 = "0" + String.valueOf(theMonthInt);
		} else if (theMonthInt == 13) {
			theMonth2 = "01";
		} else {
			theMonth2 = String.valueOf(theMonthInt);
		}
		int theDayInt = date1.getDayOfMonth() + 1;

		String[] monthArray = { theMonth, theMonth2, theMonth2 };

		String[][] add = { { String.valueOf(date1.getDayOfMonth() + 1), "0", String.valueOf(date1.getYear()) },
				{ "01", String.valueOf(date1.getMonthValue() + 1), String.valueOf(date1.getYear()) },
				{ "01", "01", String.valueOf(date1.getYear() + 1) } };
		int i = 1;
		int ii = 0;
		while (timeOffset < Long.valueOf(0) && ii < 3) {
			LocalDateTime openDate = LocalDateTime
					.parse(add[ii][2] + "-" + monthArray[ii] + "-" + add[ii][0] + " " + time, dateTimes);
			Duration duration = Duration.between(openDate, date1);
			timeOffset = duration.getSeconds();
			ii++;
		}
		return timeOffset;
	}

	public ArrayList<String> getStartTimes() {
		return this.stTimes;
	}

	public ArrayList<String> getStartDates() {
		return this.stDates;
	}

	public ArrayList<String> getEndTimes() {
		return this.endTimes;
	}

	public ArrayList<String> getEndDates() {
		return this.endDates;
	}
}
