import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.Socket;
import java.net.URI;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.imageio.ImageIO;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookType;

import exceltransfer.DataNames;
import graphpanel.PlotPanel;
import intelie.CrewRequest;
import intelie.DataRequest;
import intelie.RememberMe;
import joblog.JobLogWells;
import joblog.LoginRequest;
import joblog.WellsRequest;
import login.EncryptCredentials;
import login.GenerateEncryption;
import login.UserNamePassword;
import materials.SandSilosFrame;
import materials.SandTicketsObject;

public class TestClass {
	static mainFrame mf;

	public static void main(String[] args) throws Exception {

		// System.getProperties().loadFromXML(new FileInputStream(new
		// File("c:\\scrape\\properties.xml")));
		// System.setProperty("PROGRAM_IMAGE_PATH", "C:\\Scrape\\Scrape.png");
		// System.out.println(System.getProperty("PROGRAM_IMAGE_PATH"));
		// System.getProperties().storeToXML(new FileOutputStream(new
		// File("c:\\scrape\\properties.xml")),"Scrape Properties");
		// LinkedHashMap<String,LinkedHashMap<String,Double>> map = isipAverages();
		// saveISIPAvgToWorkbook("saved_isip.xlsm",map);

		/*
		 * createDataInWorkbook(false, new ArrayList<Double>(), DataNames.AVERAGE_RATE,
		 * DataNames.AVERAGE_PRESSURE, DataNames.ISIP, DataNames.TVD, BOTTOM_PERF,
		 * TOP_PERF, "CALC:((([" + DataNames.AVERAGE_PRESSURE + "]-[" + DataNames.ISIP +
		 * "])/[" + BOTTOM_PERF + "])*(2/" + WATER_RO_FT3 + "))*(" +
		 * P110_5_HALF_INNER_DIAM_FT + "/([" + DataNames.AVERAGE_RATE + "]*" +
		 * BBL_FT_RATE + ")^2)");
		 */

		// System.out.println(getAlphabetIndex(52));
		/*
		 * Main.initProperties(); CrewRequest crewRequest = new CrewRequest();
		 * ArrayList<String> crews =
		 * FracCalculations.getArrayOfStringKeys(crewRequest.getCrewMap());
		 *
		 * FracSummaryFrame fracSummaryFrame = new FracSummaryFrame(new
		 * Rectangle(200,200,300,175),crews); FracSummary fracSummary = null; try {
		 * fracSummary = new FracSummary(fracSummaryFrame.getConfigMap());
		 * }catch(Exception e) { e.printStackTrace(); }
		 * System.out.println(mainFrame.getFracSummaryTotals(fracSummary.
		 * getFilteredDataObject()));
		 */
		/*
		 * CheckBoxPanel panel1 = new CheckBoxPanel("Sam Parker",new
		 * CheckBox("sam_parker"),400,40); CheckBoxPanel panel2 = new
		 * CheckBoxPanel("Samuel Wayne Parker",new
		 * CheckBox("samuel_wayne_parker"),400,40); CheckBoxPanel panel3 = new
		 * CheckBoxPanel("Sam Wayne Parker",new CheckBox("sam_wayne_parker"),400,40);
		 * JFrame frame = GUIUtilities.getSimpleFrame(new Rectangle(200,200,400,250),
		 * "Test", Color.getHSBColor(-.85f, .1f, .85f), panel1,panel2,panel3);
		 * frame.setVisible(true);
		 */

		// System.out.println(URLDecoder.decode(getString(),StandardCharsets.UTF_8));
		// System.out.println(System.nanoTime()/1000);

		/*
		 * String string =
		 * "\"id\":3985,\"name\":\"7G 7H\",\"padId\":1141,\"apiNumber\":\"42461422000000\",\"afeNumber\":\"9022794\",\"section\":null,\"block\":null,\"township\":null,\"range\":null,\"trueVerticalDepth\":8537,\"measuredDepth\":19172,\"latitude\":31.3032,\"longitude\":-101.882,\"totalShotsPerStage\":30,\"totalStages\":52,\"stageLength\":198,\"designFluidVolume\":9293,\"fracRate\":75,\"active\":true,\"createdAt\":\"2023-05-02T11:05:23.000Z\",\"updatedAt\":\"2023-05-02T11:35:52.000Z\",\"deletedAt\":null,\"fracFluidTypeId\":2,\"countyId\":231,\"wellCompletionTypeId\":12,\"wellboreOrientationId\":2,\"Pad\":{\"id\":1141,\"customerId\":11,\"name\":\"University Ratliff E58\",\"createdAt\":\"2023-05-02T10:42:43.000Z\",\"updatedAt\":\"2023-05-02T11:21:50.000Z\",\"deletedAt\":null,\"Customer\":{\"id\":11,\"name\":\"Pioneer Natural Resources\",\"createdAt\":null,\"updatedAt\":null,\"deletedAt\":null}},\"crews\":[{\"id\":26,\"name\":\"PPS6\",\"createdAt\":null,\"updatedAt\":null,\"deletedAt\":null,\"WellCrew\":{\"wellId\":3985,\"crewId\":26}}"
		 * ; Matcher crewMatcher =
		 * Pattern.compile("\"crews\":(\\[?)\\{((.+)?)\\}(\\]?)").matcher(string);
		 * if(crewMatcher.find()) { System.out.println(crewMatcher.group()); }
		 */

		/*
		 * HashMap<String, ArrayList<String>> dataMap = makeStaticRequest("White",
		 * "2023-05-30 13:40", "2023-05-30 14:40", getMainChannels());
		 * 
		 * BottomProp bottomProp = new
		 * BottomProp(dataMap.get("BLENDER CALCULATED DENSITY"),
		 * dataMap.get("Blender Discharge Grand Total"), 9305d, .0217);
		 * 
		 * 
		 * dataMap.put(BOTTOM_PROP_NAME, bottomProp.getBottomProp()); PlotPanel<String>
		 * plotPanel = new PlotPanel<>(); plotArrays(dataMap,
		 * "C://Test//test_plot.png");
		 */

		/*
		 * exceltransfer.ExcelTransfer.MirroredSpreadSheet mirroredSpreadSheet =
		 * exceltransfer.ExcelTransfer .getArrayOfDataFromWorkbook(200, 100);
		 * ArrayList<ArrayList<String>> array = mirroredSpreadSheet.getSelectedValues();
		 * ArrayList<String> crews = getCrews(); HashMap<String, ArrayList<String>>
		 * assetCrewMap = new HashMap<>(); for (ArrayList<String> in : array) {
		 * ArrayList<String> parsedTimeFrame = getParsedTimeFrame(in.get(0), in.get(2),
		 * 30l); Matcher matcher = Pattern.compile("\\d+").matcher(in.get(1)); String id
		 * = matcher.find() ? matcher.group() : in.get(1); for (String timeFrame :
		 * parsedTimeFrame) { addToAssetCrewMap(assetCrewMap, assetHistoryRequest(crews,
		 * timeFrame, id)); } } System.out.println(assetCrewMap);
		 * writeMapToFile(assetCrewMap,ASSET_CREW_MAP_FILENAME);
		 */

		/*
		 * for(CellType s:CellType.values()) { System.out.println(s); }
		 *
		 * exceltransfer.OperatorTemplateStageSummary.getExeDir();
		 * exceltransfer.OperatorTemplateStageSummary operatorTemplateStageSummary = new
		 * exceltransfer.OperatorTemplateStageSummary( new
		 * Rectangle(Toolkit.getDefaultToolkit().getScreenSize().width / 3, 0,
		 * Toolkit.getDefaultToolkit().getScreenSize().width / 2,
		 * Toolkit.getDefaultToolkit().getScreenSize().height - 40));
		 */

		// importTRData("D:\\Projects2\\Test\\import");
		// staticRequestPlot("red", "2023-05-12 08:35:00", "2023-05-12 11:20:00",
		// getChannels());
		// System.out.println(upperCaseBeginning("SAM PARKER 45h"));
		// remoteFleetVisionLogin();
		LoginRequest login = null;
		HashMap<String, String> creds = EncryptCredentials.getUserCredentials(LoginRequest.PETRO_IQ_CRED_PATH,
				"PetroIQ Login");
		try {
			login = new LoginRequest(creds.get(UserNamePassword.USERNAME), creds.get(UserNamePassword.PASSWORD));
		} catch (IOException | InterruptedException e) {
			System.out.println("Exception caught logging in to PETROIQ");
		}
		String token = login.getToken();
		JobLogWells jobLogWells = WellsRequest.setWellMap(token);
		System.out.println("sam");
		Map<String, HashMap<String, HashMap<String, String>>> activeMap = jobLogWells.getActiveMap();
		HashMap<String, HashMap<String, String>> pads = new HashMap<>();
		activeMap.entrySet().forEach((Map.Entry<String, HashMap<String, HashMap<String, String>>> entry) -> {
			String startDate = formatStartDate(entry.getValue().get(PAD).get(START_DATE));
			if(isBefore(startDate,60l,ChronoUnit.DAYS)) {
				return;
			}
			String key = entry.getValue().get(PAD).get(OBJECT_NAME);
			pads.put(key, new HashMap<>());
			pads.get(key).put(START_DATE,startDate);
			pads.get(key).put(OPERATOR, entry.getValue().get(OPERATOR).get(OBJECT_NAME));
			pads.get(key).put(CREW, entry.getValue().get(CREW).get(OBJECT_NAME));
		});
		pads.entrySet().forEach((Map.Entry<String, HashMap<String, String>> entry) -> {
			System.out.println(entry.getKey() + "\t-\tCrew: " + entry.getValue().get(CREW) + "\t-\t Start_Date: "
					+ entry.getValue().get(START_DATE) + "\t-\tOperator: " + entry.getValue().get(OPERATOR));
		});
	}

	public final static String OPERATOR = "operator";
	public final static String CREW = "crew";
	public final static String START_DATE = "createdAt";
	public final static String PAD = "pad";
	public final static String OBJECT_NAME = "name";
	public final static String ASSET_CREW_MAP_FILENAME = "asset_crew_map.map";
	public static boolean isBefore(String localDate,long numUnits,ChronoUnit unit) {
		LocalDateTime dateTime = LocalDateTime.parse(localDate);
		LocalDateTime cutOffDate = LocalDateTime.now().minus(numUnits,unit);
		return Duration.between(dateTime, cutOffDate).getSeconds()>0l;
	}
	public static String formatStartDate(String startDate) {
		return startDate+":00:00";
	}
	public static boolean isNumeric1(String string) {
		Matcher matcher = Pattern.compile("(^(\\-?)(\\d+)(\\.(\\d*))?)").matcher(string);
		if (matcher.find()) {
			String found = matcher.group();
			System.out.println(found);
			return found.equals(string);
		}
		return false;
	}

	public static int[] getAnotherArray() {
		int[] array = { 1, 2 };
		return array;
	}

	public static LinkedHashMap<String, ArrayList<String>> makeStaticRequest(String crew, String start, String end,
			ArrayList<String> channels) throws Exception {
		CrewRequest crewRequest = new CrewRequest();
		DataRequest dataRequest = new DataRequest(crewRequest.getToken(), crewRequest.getSessionId(),
				DataRequest.getPostBody(crew, start, end, channels), 2, crewRequest.getCookie());
		LinkedHashMap<String, ArrayList<String>> rateMap = dataRequest.makeRequest();
		return rateMap;
	}

	public static HashMap<String, ArrayList<String>> readMapFromFile(String fileName)
			throws IOException, ClassNotFoundException {
		File file = new File(fileName);
		if (!file.exists()) {
			return null;
		}
		ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
		@SuppressWarnings("unchecked")
		HashMap<String, ArrayList<String>> map = (HashMap<String, ArrayList<String>>) objectInputStream.readObject();
		objectInputStream.close();
		return map;
	}

	public static void writeMapToFile(HashMap<String, ArrayList<String>> assetCrewMap, String fileName)
			throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(new File(fileName));
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
		objectOutputStream.writeObject(assetCrewMap);
		objectOutputStream.close();
	}

	public static void addToAssetCrewMap(HashMap<String, ArrayList<String>> grossAssetHistory,
			HashMap<String, ArrayList<String>> frameAssetHistory) {
		for (Map.Entry<String, ArrayList<String>> entry : frameAssetHistory.entrySet()) {
			if (grossAssetHistory.containsKey(entry.getKey())) {
				grossAssetHistory.get(entry.getKey()).addAll(frameAssetHistory.get(entry.getKey()));
				continue;
			}
			grossAssetHistory.putAll(frameAssetHistory);
		}
	}

	public static ArrayList<String> getParsedTimeFrame(String start, String end, long maxDays) {
		LocalDateTime startDateTime = LocalDateTime.parse(start.replace(" ", "T"));
		LocalDateTime endDateTime = LocalDateTime.parse(end.replace(" ", "T"));
		long numDays;
		if ((numDays = Duration.between(startDateTime, endDateTime).toDays()) > maxDays) {
			long accumDays = 0l;
			return parseTimeFrame(startDateTime, endDateTime, maxDays, numDays);
		}
		ArrayList<String> parsedTimeFrame = new ArrayList<>();
		parsedTimeFrame.add(getTimeFrame(start, end));
		return parsedTimeFrame;
	}

	public final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss");

	public static ArrayList<String> parseTimeFrame(LocalDateTime startDateTime, LocalDateTime endDateTime, long maxDays,
			long totalDays) {
		long accumDays = 0l;
		boolean done = false;
		ArrayList<String> parsedTimeFrame = new ArrayList<>();
		LocalDateTime currentStart = startDateTime;
		while (!done) {
			LocalDateTime currentEnd = currentStart.plusDays(maxDays);
			if (Duration.between(currentEnd, endDateTime).toDays() < 0) {
				currentEnd = endDateTime;
				done = true;
			}
			parsedTimeFrame.add(
					getTimeFrame(currentStart.format(DATE_TIME_FORMATTER), currentEnd.format(DATE_TIME_FORMATTER)));
			currentStart = currentEnd;
		}
		return parsedTimeFrame;
	}

	public static String getTimeFrame(String start, String end) {
		return start + " to " + end;
	}

	public static ArrayList<String> getCrews() {
		ArrayList<String> array = new ArrayList<>();
		array.add("Black");
		array.add("Red");
		array.add("Orange");
		array.add("Green");
		array.add("Silver");
		array.add("Navy");
		array.add("Navy B");
		array.add("White");
		array.add("Purple");
		array.add("Platinum");
		array.add("Gold");
		array.add("Grey");
		array.add("PPS6");
		array.add("PPS6 B");
		return array;
	}

	public final static String BOTTOM_PROP_NAME = "bottom_prop_con";

	public static Integer getColor(String channelName) {
		switch (channelName) {
		case ("BLENDER CALCULATED DENSITY"):
			return PlotPanel.GREEN;
		case ("Pressure 1"):
			return PlotPanel.RED;
		case ("Blender Selected Discharge Rate"):
			return PlotPanel.BLUE;
		case ("Blender Discharge Grand Total"):
			return PlotPanel.PURPLE;
		case ("BLENDER CURRENT STAGE"):
			return PlotPanel.BLACK;
		case (BOTTOM_PROP_NAME):
			return PlotPanel.YELLOW;
		default:
			return PlotPanel.BLACK;
		}
	}

	public static <T> void plotArrays(HashMap<String, ArrayList<String>> dataMap, String fileName) throws IOException {
		graphpanel.AxisPanel axisPanel = new graphpanel.AxisPanel(GUIUtilities.getScreenWidth() - 50,
				GUIUtilities.getScreenHeight() - 50);
		graphpanel.PlotPanel<String> plotPanel = new graphpanel.PlotPanel<>();
		ArrayList<String> channels = getMainChannels();
		dataMap.entrySet().forEach((Map.Entry<String, ArrayList<String>> entry) -> {
			if (!channels.contains(entry.getKey()) & !entry.getKey().equals(BOTTOM_PROP_NAME)) {
				return;
			}
			Integer colorIndex = getColor(entry.getKey());
			Integer stroke = 2;
			plotPanel.addDataArray(entry.getValue(), colorIndex, stroke);

		});
		axisPanel.addPlotPanel(plotPanel);
		JFrame frame = new JFrame();
		frame.setBounds(0, 0, GUIUtilities.getScreenWidth(), GUIUtilities.getScreenHeight());
		frame.add(axisPanel);
		frame.setVisible(true);
		saveImage(new File(fileName), axisPanel, GUIUtilities.getScreenWidth() - 50,
				GUIUtilities.getScreenHeight() - 50);
		frame.dispose();
	}

	public static void saveImage(File file, JPanel panel, int width, int height) throws IOException {
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bufferedImage.createGraphics();
		panel.paintAll(g);
		g.dispose();
		ImageIO.write(bufferedImage, "png", file);

	}

	public static ArrayList<String> getMainChannels() {
		ArrayList<String> array = new ArrayList<>();
		array.add("BLENDER CALCULATED DENSITY");
		array.add("Pressure 1");
		array.add("Blender Selected Discharge Rate");
		array.add("Blender Discharge Grand Total");
		array.add("BLENDER CURRENT STAGE");
		return array;
	}

	/*
	 * USED FOR DATES THAT HAVE THE FORMAT MM/DD/YYYY HH:mm:(ss?)
	 */
	public final static LocalDateTime parseDateTime(String dateTime) {
		if (dateTime.regionMatches(10, " ", 0, 1)) {
			return LocalDateTime.parse(dateTime.replace(" ", "T"));
		}
		return LocalDateTime.parse(dateTime);
	}

	private static String getTwoDigits(String intString) {
		String fullString = "0" + intString;
		return fullString.substring(fullString.length() - 2, fullString.length());
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

	public static String reOrgDateTime(String dateTime) {
		String dateString = getDateFromDateTime(dateTime);
		String timeString = getTimeFromDateTime(dateTime);
		return dateString + " " + timeString;
	}

	/*
	 * USED FOR DATES THAT HAVE THE FORMAT MM/DD/YYYY HH:mm:(ss?)
	 */
	public static String getTimeFromDateTime(String dateTime) {
		Matcher timeMatcher = Pattern.compile("\\d{2}\\:\\d{2}((\\:\\d{2})?)").matcher(dateTime);
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
		Matcher matcher = Pattern.compile("\\d{2}/").matcher(dateTime);
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
		return year + "-" + monthDayArray[MONTH_INDEX] + "-" + monthDayArray[DAY_INDEX];
	}

	private static String getYearFromDateTime(String dateTime) {
		Matcher matcher = Pattern.compile("\\d{4}").matcher(dateTime);
		if (matcher.find()) {
			return matcher.group();
		}
		return LocalDate.now().format(DateTimeFormatter.ofPattern("YYYY"));
	}

	private static String capWords(String string) {
		Matcher matcher = Pattern.compile("(^[a-z])|(\\s[a-z])").matcher(string);
		while (matcher.find()) {
			string = (matcher.start() == 0 ? "" : string.substring(0, matcher.start() + 1))
					+ String.valueOf(string.charAt(matcher.end() - 1)).toUpperCase()
					+ string.substring(matcher.end()).toLowerCase();
			matcher.reset(string);
		}
		return string;
	}

	public static void staticRequestPlot(String crew, String start, String end, ArrayList<String> channels)
			throws Exception {

		LinkedHashMap<String, ArrayList<String>> rateMap = makeStaticRequest(crew, start, end, channels);
		LinkedHashMap<String, ArrayList<String>> newMap = new LinkedHashMap<>();

		channels.forEach((String string) -> {
			newMap.put(string, rateMap.get(string));
		});
		for (Map.Entry<String, ArrayList<String>> entry : newMap.entrySet()) {
			if (!channels.contains(entry.getKey())) {
				continue;
			}

			for (int i = 1; i < entry.getValue().size(); i++) {
				Float delta = Float.valueOf(entry.getValue().get(i)) - Float.valueOf(entry.getValue().get(i - 1));
				if (delta > 3f) {
					System.out.println(delta);
				}
			}
			mainFrame.plotArray(entry.getValue(), entry.getKey() + ".png");
			System.out.println(entry.getKey() + " - " + UserDefinedFrame.max(entry.getValue()));
		}
	}

	public final static String LAST_SIX_HOURS = "last 6 hours";
	public final static String LAST_SEVEN_DAYS = "last 7 days";
	public final static String LAST_THIRTY_DAYS = "last 30 days";

	public static String getSpecificTimeFrame(String start, String end) {
		return start + " to " + end;
	}

	public static HashMap<String, ArrayList<String>> assetHistoryRequest(ArrayList<String> crews, String timeFrame,
			String id) throws IOException, InterruptedException {
		CrewRequest crewRequest = new CrewRequest();
		String cookies = crewRequest.getSessionId() + "; remember-me=" + crewRequest.getCookie();
		return DataRequest.makeAssetHistoryRequest(timeFrame, cookies, crewRequest.getToken(), crews, id);
	}

	public static LinkedHashMap<String, ArrayList<String>> calcTotalChannels(
			LinkedHashMap<String, ArrayList<String>> rateMap) {
		LinkedHashMap<String, ArrayList<String>> integArray = new LinkedHashMap<>();
		ArrayList<String> channels = getChannels();
		for (Map.Entry<String, ArrayList<String>> entry : rateMap.entrySet()) {
			if (!channels.contains(entry.getKey())) {
				continue;
			}
			integArray.put(entry.getKey() + "_Total",
					getIntegralCurve(strArrayToFloatArr(entry.getValue()), (1f / 60f)));
		}
		return integArray;
	}

	public static ArrayList<String> getIntegralCurve(ArrayList<? extends Number> array, Float convUnit) {
		ArrayList<String> integArray = new ArrayList<>();
		Float runningValue = 0f;
		for (Number v : array) {
			runningValue += (((float) v) * convUnit);
			integArray.add(String.valueOf(Math.round(runningValue)));
		}
		return integArray;
	}

	public static ArrayList<Float> strArrayToFloatArr(ArrayList<String> array) {
		ArrayList<Float> newArray = new ArrayList<>();
		for (String s : array) {
			newArray.add(s.matches("(\\-?)(([\\d]*)?)((\\.(\\d+))?)") ? Float.valueOf(s) : 0f);
		}
		return newArray;
	}

	public static ArrayList<String> getChannels() {
		ArrayList<String> array = new ArrayList<>();
		array.add("Blender Suction Grand Total");
		array.add("Blender Discharge Grand Total");
		return array;
	}

	public static LinkedHashMap<LocalDate, HashSet<String>> eventLogRequest() throws Exception {
		ExecutorService executor = Executors.newCachedThreadPool();
		HashMap<String, String> creds = EncryptCredentials.getUserCredentials(LoginRequest.PETRO_IQ_CRED_PATH,
				"PetroIQ Login");
		joblog.LoginRequest login = new joblog.LoginRequest(creds.get(UserNamePassword.USERNAME),
				creds.get(UserNamePassword.PASSWORD));
		String token = login.getToken();
		joblog.CrewRequest crewRequest = new joblog.CrewRequest(token);
		HashMap<String, String> crewMap = crewRequest.getCrewMap();
		LinkedHashMap<LocalDate, HashSet<String>> engineerDates = new LinkedHashMap<>();
		Semaphore semaphore = new Semaphore(1);
		for (String s : crewMap.keySet()) {
			executor.execute(() -> {
				LinkedHashMap<LocalDate, HashSet<String>> crewEngrMap = null;
				try {
					crewEngrMap = loggedDaysRequest(s, getNowFormatted(), token);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
				try {
					semaphore.acquire();
				} catch (Exception e1) {
					e1.printStackTrace();
					semaphore.release();
				}
				engineerDates.putAll(addToMap(engineerDates, crewEngrMap));
				semaphore.release();
			});
		}

		return engineerDates;
	}

	public synchronized static LinkedHashMap<LocalDate, HashSet<String>> addToMap(
			LinkedHashMap<LocalDate, HashSet<String>> engineerDates,
			LinkedHashMap<LocalDate, HashSet<String>> crewEngrDates) {
		engineerDates.putAll(crewEngrDates);
		return engineerDates;
	}

	public static String getNowFormatted() {
		String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY-MM-DD hh:mm:ss")) + ".000";
		return now;
	}

	public static LinkedHashMap<LocalDate, HashSet<String>> loggedDaysRequest(String crew, String time, String token)
			throws Exception {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().header("accept", "application/json, text/plain, */*")
				.header("accept-encoding", "gzip, deflate, br").header("content-type", "application/json;charset=UTF-8")
				.header("x-access-token", token).uri(URI.create("https://propetro.petroiq.com/graphql"))

				.POST(HttpRequest.BodyPublishers.ofString(getRequestString(crew, time, "200"))).build();
		HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		LinkedHashMap<LocalDate, HashSet<String>> map = parseLogResponse(response.body());
		return map;
	}

	public static String getStringFromBytes(byte[] bytes) {
		StringBuilder stringBuilder = new StringBuilder();
		for (byte b : bytes) {
			if (b == 0) {
				return stringBuilder.toString();
			}
			stringBuilder.append((char) b);
		}
		return stringBuilder.toString();
	}

	public static void addEventToDateMap(LinkedHashMap<LocalDate, HashSet<String>> engineerDate,
			HashMap<String, String> eventMap) {
		LocalDate date = getLocalDate(eventMap.get("date"));
		if (engineerDate.containsKey(date)) {
			engineerDate.get(date).add(eventMap.get("engineer"));
			return;
		}
		engineerDate.put(date, new HashSet<>());
		engineerDate.get(date).add(eventMap.get("engineer"));
	}

	public static LinkedHashMap<LocalDate, HashSet<String>> parseLogResponse(InputStream inputStream) throws Exception {
		byte[] bytes = new byte[1024];
		int i;
		String response = "";
		LinkedHashMap<LocalDate, HashSet<String>> engineerDateMap = new LinkedHashMap<>();
		while ((i = inputStream.read(bytes)) > -1) {
			response += getStringFromBytes(bytes);
			if (response.contains("\"events\":[")) {
				response = response.substring(response.indexOf('[') + 1);
			} else if (response.charAt(0) != '{') {
				response = response.substring(response.indexOf('{'));
			}
			/*
			 * else if (response.charAt(0) == '{') { response = response.substring(1); }
			 */

			int end = getEndIndexOfNextEvent(response);
			if (end == -1) {
				continue;
			}
			System.out.println(response.substring(0, end));
			HashMap<String, String> map = getEventMap(getPrimArray("date", "engineer"),
					getPrimArray("startDate", "Engineer"), getPrimArray("startDate", "fullName"),
					response.substring(0, end));
			if (hasNullValues(map)) {
				continue;
			}

			addEventToDateMap(engineerDateMap, map);
			response = response.substring(end);
		}
		return engineerDateMap;
	}

	public static boolean hasNullValues(HashMap<String, String> map) {
		for (String s : map.keySet()) {
			if (map.get(s) == null || map.get(s).isEmpty() || map.get(s).equals("")) {
				return true;
			}
		}
		return false;
	}

	public static LocalDate getLocalDate(String startDate) {
		LocalDate localDate = LocalDate.parse(startDate.split("T")[0]);
		return localDate;
	}

	public static String[] getPrimArray(String... strings) {
		return strings;
	}

	public static HashMap<String, String> getEventMap(String[] keys, String objNames[], String propNames[],
			String json) {
		int i = 0;
		HashMap<String, String> eventMap = new HashMap<>();
		for (String obj : objNames) {
			eventMap.put(keys[i], getPropValue(obj, propNames[i], json));
			i++;
		}
		return eventMap;
	}

	public static String getPropValue(String propName, String json) {
		Matcher matcher = Pattern.compile("\"" + propName + "\"\\:(\"?)(([A-Za-z0-9\\-\\.\\:\\s]+)?)([\",\\}]?)")
				.matcher(json);
		if (matcher.find()) {
			String found = matcher.group();
			return found.substring(found.indexOf(':') + 1);
		}
		return "";
	}

	public static String getPropValue(String objName, String propName, String json) {
		String objString = objName.equals(propName) ? json : getObjectString(objName, json);
		Matcher matcher = Pattern.compile("\"" + propName + "\"\\:(\"?)(([A-Za-z0-9\\-\\.\\:\\s]+)?)([\",\\}]?)")
				.matcher(objString);
		if (matcher.find()) {
			String found = matcher.group();
			return found.substring(found.indexOf(":") + 1).replace("\"", "");
		}
		return "";
	}

	public static String getObjectString(String objName, String json) {
		Matcher matcher = Pattern.compile("\"" + objName + "\":\\{").matcher(json);
		if (matcher.find()) {
			int endObjString = findEndIndexOfObject(json, matcher.end() - 1);
			return json.substring(matcher.end(), endObjString);
		}
		return "";
	}

	public static int findEndIndexOfObject(String json, int start) {
		int bracketCount = 0;
		char[] chars = new char[json.length()];
		json.getChars(start, json.length(), chars, 0);
		int index = json.substring(start).indexOf('{', start);
		for (int i = start; i < json.length(); i++) {
			char c = json.charAt(i);
			if (c == '{') {
				bracketCount++;
			} else if (c == '}') {
				bracketCount--;
				System.out.println("Bracket Count: " + bracketCount);
				if (bracketCount == 0) {
					return i;
				}
			}

		}
		return -1;
	}

	public static int getEndIndexOfNextEvent(String response) {
		int end = findEndIndexOfObject(response, 0);

		return end;
	}

	public static String getRequestString(String crew, String time, String pageSize) {
		return "{\"query\":\"{ getEvents(_pageNumber: 1, _pageSize: " + pageSize + ", _echo: \\\"" + time
				+ "\\\",\\n                        id: null, crew: " + crew
				+ ", approvalStatuses: [0,1,2,3,4], pad: null, wellFilter: null, stageFilter: null, \\n                        startDateAfter: null, startDateBefore: null, \\n                        endDateAfter: null, endDateBefore: null, durationFilter: null,\\n                        pumpTimeFilter: null, nptTimeFilter: null, engineerFilter: null, treaterFilter: null,\\n                        consultantFilter: null, downtimeCategoryFilter: null, padSort: null, wellSort: null,\\n                        stageSort: null, startDateSort: \\\"DESC\\\", endDateSort: null, durationSort: null, \\n                        pumpTimeSort: null, nptTimeSort: null,\\n                        engineerSort: null, treaterSort: null, consultantSort: null, \\n                        downtimeCategorySort: null, validationSort: null) { \\n                        _totalCount\\n                        _totalPages\\n                        _pageSize\\n                        _echo\\n                        events {\\n                            id\\n                            wellId\\n                            Well { id name Pad { name Customer { name } } }\\n                            crewId\\n                            Crew { name }\\n                            startDate\\n                            endDate\\n                            engineerId\\n                            Engineer { fullName }\\n                            treaterId\\n                            Treater { id fullName }\\n                            consultantId\\n                            Consultant { id fullName }\\n                            EventCalculation {\\n                                eventDuration\\n                                ptDuration\\n                                nptDuration\\n                                errorCount\\n                                warningCount\\n                                validationSnapshot\\n                            }                  \\n                            details\\n                            approvalStatus\\n                            Stage {\\n                                id\\n                                stageNumber\\n                                designSandVolume\\n                                actualSandVolume\\n                                designFluidVolume\\n                                actualFluidVolume\\n                                Npts {\\n                                    id\\n                                    startDate\\n                                    endDate\\n                                    details\\n                                    Npt {\\n                                        id\\n                                        downtimeCategoryId\\n                                        DowntimeCategory {\\n                                            id\\n                                            name\\n                                            internal\\n                                        }\\n                                        thirdPartyId\\n                                        ThirdParty {\\n                                            id\\n                                            name\\n                                        }\\n                                    }\\n                                }\\n                            }\\n                            Npt {\\n                                id\\n                                downtimeCategoryId\\n                                DowntimeCategory { id name internal }\\n                                thirdPartyId\\n                                ThirdParty { id name }\\n                            }\\n                        }\\n                } }\"}";
	}

	public static String getWellNameFromTR(String fileName) {
		Matcher matcher = Pattern.compile("(\\s?)(\\-?)(\\s?)[Tt][Rr]((\\.xlsm)?)").matcher(fileName);
		if (matcher.find()) {
			return fileName.substring(0, matcher.start());
		}
		return fileName.split("\\.")[0];
	}

	public static void importTRData(String directory) throws Exception {
		File file = new File(directory);

		if (!file.exists()) {
			System.out.println("Zero TR's in directory");
			return;
		}

		ExecutorService executor = Executors.newCachedThreadPool();
		JobLogWells jobLogWells = mainFrame.retrievePetroIQInfo();
		CrewRequest crewRequest = new CrewRequest();
		Semaphore requestSemaphore = new Semaphore(1);
		Semaphore workbookSemaphore = new Semaphore(3);
		for (File tr : file.listFiles()) {
			executor.execute(() -> {
				String wellName = upperCaseBeginning(getWellNameFromTR(tr.getName()));
				XSSFWorkbook workbook = null;
				try {
					workbookSemaphore.acquire();
					System.out.println("Workbook Semaphore Acquired");
					workbook = new XSSFWorkbook(new FileInputStream(tr));
				} catch (IOException | InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return;
				}
				XSSFSheet totalsSheet = workbook.getSheet("Totals");
				Integer numStages = findLastStage(totalsSheet) - 1;
				try {
					importWorkbookData(jobLogWells, crewRequest, workbook, totalsSheet, numStages, wellName,
							requestSemaphore);
				} catch (Exception e) {
					e.printStackTrace();
					try {
						workbook.close();
						workbookSemaphore.release();
					} catch (IOException e1) {
						e1.printStackTrace();
						return;
					}
				}
				try {
					workbook.close();
					workbookSemaphore.release();
					System.out.println("Workbook Semaphore Released");
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			});
		}
	}

	public static HashMap<String, String> getValueLocationMap() {
		HashMap<String, String> map = new HashMap<>();
		map.put(DataNames.START_DATE, "G33");
		map.put(DataNames.START_TIME, "H33");
		map.put(DataNames.END_DATE, "I33");
		map.put(DataNames.END_TIME, "J33");
		map.put(DataNames.SAND_NAME_START, "B80");
		map.put(DataNames.SAND_VOLUME_START, "F80");
		return map;
	}

	public static void importTRData(String directory, String sheetName, HashMap<String, String> locationMap)
			throws Exception {
		File file = new File(directory);

		if (!file.exists()) {
			System.out.println("Zero TR's in directory");
			return;
		}

		ExecutorService executor = Executors.newCachedThreadPool();
		JobLogWells jobLogWells = mainFrame.retrievePetroIQInfo();
		CrewRequest crewRequest = new CrewRequest();
		Semaphore requestSemaphore = new Semaphore(1);
		Semaphore workbookSemaphore = new Semaphore(3);
		for (File tr : file.listFiles()) {
			executor.execute(() -> {
				String wellName = upperCaseBeginning(tr.getName().split("-")[0].trim());
				XSSFWorkbook workbook = null;
				try {
					workbookSemaphore.acquire();
					System.out.println("Workbook Semaphore Acquired");
					workbook = new XSSFWorkbook(new FileInputStream(tr));
				} catch (IOException | InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return;
				}
				XSSFSheet totalsSheet = workbook.getSheet(sheetName);
				Integer numStages = findLastStage(totalsSheet) - 1;
				try {
					importWorkbookData(jobLogWells, crewRequest, workbook, totalsSheet, numStages, wellName,
							requestSemaphore);
				} catch (Exception e) {
					e.printStackTrace();
					try {
						workbook.close();
						workbookSemaphore.release();
					} catch (IOException e1) {
						e1.printStackTrace();
						return;
					}
				}
				try {
					workbook.close();
					workbookSemaphore.release();
					System.out.println("Workbook Semaphore Released");
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			});
		}
	}

	public static HashMap<String, String> getInputTimes(LinkedHashMap<String, String> sigVals) {
		return LastStartCloseComponent.getDateTimes(sigVals.get(DataNames.START_DATE),
				sigVals.get(DataNames.START_TIME), sigVals.get(DataNames.END_DATE), sigVals.get(DataNames.END_TIME), -5,
				1);
	}

	public static HashMap<String, String> getTrueTimes(HashMap<String, String> stageTimes) {
		return LastStartCloseComponent.getDateTimes(stageTimes, 5, -1);
	}

	public static HashMap<String, String> getChannelMap() throws IOException {
		HashMap<String, String> channelMap = ChannelPane.getChannelList();
		return channelMap;
	}

	public static void importWorkbookData(JobLogWells jobLogWells, CrewRequest crewRequest, XSSFWorkbook workbook,
			XSSFSheet totalsSheet, Integer numStages, String wellName, Semaphore requestSemaphore) throws Exception {
		for (int i = 1; i <= numStages; i++) {
			Integer stageColumn = findStageColumn(totalsSheet, "STG " + i, 7);
			if (stageColumn == -1) {
				continue;
			}
			XSSFSheet stageSheet = workbook.getSheet("Stage " + i);
			LinkedHashMap<String, String> sigVals = getSigValsMap(totalsSheet, stageSheet, stageColumn, i, wellName);
			HashMap<String, LinkedHashMap<String, String>> chemSandMap = getChemSandMap(stageSheet);
			HashMap<String, String> inputTimes = getInputTimes(sigVals);
			try {
				mainFrame.butClick.mainEvaluate(crewRequest, jobLogWells, wellName,
						new SetDiagnostics(ReadOperatorTemplate.readTemplateValueNames("sam")), sigVals, chemSandMap,
						inputTimes, getChannelMap(), getTrueTimes(inputTimes), requestSemaphore);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}

	}

	public static void importWorkbookData(JobLogWells jobLogWells, CrewRequest crewRequest, XSSFWorkbook workbook,
			HashMap<String, String> locationMap, String sheetNamePattern, String stageOneName, Integer numStages,
			String wellName, Semaphore requestSemaphore) throws Exception {
		for (int i = 1; i <= numStages; i++) {
			XSSFSheet sheet = null;
			if (i == 1) {
				sheet = workbook.getSheet(stageOneName);
			} else {
				sheet = workbook.getSheet(sheetNamePattern.replace("#", String.valueOf(i)));
			}
			Integer stageColumn = findStageColumn(sheet, "STG " + i, 7);
			if (stageColumn == -1) {
				continue;
			}
			XSSFSheet stageSheet = workbook.getSheet("Stage " + i);
			LinkedHashMap<String, String> sigVals = getSigValsMap(sheet, stageSheet, stageColumn, i, wellName);
			HashMap<String, LinkedHashMap<String, String>> chemSandMap = getChemSandMap(stageSheet);
			HashMap<String, String> inputTimes = getInputTimes(sigVals);

			try {
				mainFrame.butClick.mainEvaluate(crewRequest, jobLogWells, wellName,
						new SetDiagnostics(ReadOperatorTemplate.readTemplateValueNames("sam")), sigVals, chemSandMap,
						inputTimes, getChannelMap(), getTrueTimes(inputTimes), requestSemaphore);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}

	}

	public static HashMap<String, LinkedHashMap<String, String>> getChemSandMap(XSSFSheet stageSheet) {
		HashMap<String, LinkedHashMap<String, String>> chemSandMap = new HashMap<>();
		chemSandMap.put(ChemSandFrame.SAND_NAME, getTableMap(stageSheet, 137, 1, 15));
		chemSandMap.put(ChemSandFrame.CHEM_NAME, getTableMap(stageSheet, 185, 1, 16));
		return chemSandMap;
	}

	public static LinkedHashMap<String, String> getTableMap(XSSFSheet stageSheet, int startRow, int nameCol,
			int valCol) {
		LinkedHashMap<String, String> tableMap = new LinkedHashMap<>();

		int i = 0;
		String cellValue;
		while (stageSheet.getRow(startRow + i) != null
				&& !(cellValue = stageSheet.getRow(startRow + i).getCell(nameCol).getStringCellValue()).equals("")) {
			tableMap.put(cellValue,
					String.valueOf(stageSheet.getRow(startRow + i).getCell(valCol).getNumericCellValue()));
			i++;
		}
		return tableMap;
	}

	public static LinkedHashMap<String, String> getSigValsMap(XSSFSheet totalsSheet, XSSFSheet stageSheet,
			Integer column, Integer stage, String wellName) {
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		map.put(DataNames.WELL_NAME, wellName);
		map.put(DataNames.STAGE_NUMBER, String.valueOf(stage));
		map.put(DataNames.OPEN_PRESSURE, getCellStringValue(totalsSheet, 44, column));
		map.put(DataNames.BREAK_PRESSURE, getCellStringValue(totalsSheet, 45, column));
		map.put(DataNames.ISIP, getCellStringValue(totalsSheet, 46, column));
		map.put(DataNames.MAX_PRESSURE, getCellStringValue(totalsSheet, 48, column));
		map.put(DataNames.AVERAGE_PRESSURE, getCellStringValue(totalsSheet, 49, column));
		map.put(DataNames.MAX_RATE, getCellStringValue(totalsSheet, 51, column));
		map.put(DataNames.AVERAGE_RATE, getCellStringValue(totalsSheet, 52, column));
		map.put(DataNames.CLEAN_GRAND, getCellStringValue(totalsSheet, 27, column));
		map.put(DataNames.SLURRY_GRAND, getCellStringValue(totalsSheet, 28, column));
		map.put(DataNames.PERFS,
				getCellStringValue(totalsSheet, 60, column) + "-" + getCellStringValue(totalsSheet, 61, column));
		map.put(DataNames.TVD, getCellStringValue(totalsSheet, 62, column));
		map.put(DataNames.START_TIME, getCellDateTime(stageSheet, 212, 0, DateTimeFormatter.ofPattern("HH:mm")));
		map.put(DataNames.END_TIME, getCellDateTime(stageSheet, 264, 0, DateTimeFormatter.ofPattern("HH:mm")));
		map.put(DataNames.END_DATE, getCellDateTime(stageSheet, 31, 0, DateTimeFormatter.ofPattern("YYYY-MM-dd")));
		map.put(DataNames.START_DATE,
				getStartDate(map.get(DataNames.START_TIME), map.get(DataNames.END_TIME), map.get(DataNames.END_DATE)));
		return map;
	}

	public static String upperCaseBeginning(String string) {
		Matcher matcher = Pattern.compile("(^|[\\d\\s])\\w").matcher(string);
		String newString = "";
		int last = 0;
		while (matcher.find()) {
			if (matcher.end() == string.length()) {
				newString += newString.substring(0, newString.length() - 1) + matcher.group().toUpperCase();
				last = newString.length();
				break;
			}
			newString += string.substring(last, matcher.start()).toLowerCase() + matcher.group().toUpperCase();
			last = matcher.end();
		}
		if (last < string.length()) {
			newString += string.substring(last).toLowerCase();
		}
		return newString;
	}

	public static String getStartDate(String startTime, String endTime, String endDate) {
		LocalDateTime endDateTime = LocalDateTime.of(LocalDate.parse(endDate), LocalTime.parse(endTime));
		LocalDateTime startDateTime = LocalDateTime.of(LocalDate.parse(endDate), LocalTime.parse(startTime));
		if (Duration.between(startDateTime, endDateTime).isNegative()) {
			return LocalDate.parse(endDate).minusDays(1).format(DateTimeFormatter.ofPattern("YYYY-MM-dd"));
		}
		return endDate;
	}

	public static Integer findStageColumn(XSSFSheet totalsSheet, String findString, int searchRow) {
		int i = 0;
		while (i < 500) {
			String cellValue = totalsSheet.getRow(searchRow).getCell(i) != null
					? totalsSheet.getRow(searchRow).getCell(i).getStringCellValue()
					: "";
			System.out.println(cellValue);
			if (cellValue.equals(findString)) {
				return i;
			}
			i++;
		}
		return -1;
	}

	public static String getCellDateTime(XSSFSheet sheet, int row, int col, DateTimeFormatter formatter) {
		LocalDateTime cellDateTime = null;
		try {
			cellDateTime = sheet.getRow(row).getCell(col).getLocalDateTimeCellValue();
		} catch (IllegalStateException e) {
			String cellValue = sheet.getRow(row).getCell(col).getStringCellValue();
			Matcher matcher = Pattern.compile("\\d{2}\\:\\d{2}").matcher(cellValue);
			if (matcher.find()) {
				return LocalTime.parse(cellValue).format(formatter);
			}
			return LocalDate.parse(cellValue).format(formatter);
		}
		return cellDateTime != null ? cellDateTime.format(formatter) : "00:00";
	}

	public static String getCellValue(XSSFCell cell, CellType type) {

		if (type == CellType.NUMERIC) {
			double value;
			return (value = cell.getNumericCellValue()) > 0.0 ? String.valueOf(value) : "";
		}
		return cell.getStringCellValue();
	}

	public static String getCellStringValue(XSSFSheet sheet, int row, int col) {
		if (sheet.getRow(row) == null || sheet.getRow(row).getCell(col) == null) {
			return "";
		}
		if (sheet.getRow(row).getCell(col).getCellType() == CellType.NUMERIC) {
			double value;
			return (value = sheet.getRow(row).getCell(col).getNumericCellValue()) > 0 ? String.valueOf(value) : "";
		} else if (sheet.getRow(row).getCell(col).getCellType() == CellType.FORMULA) {
			return getCellValue(sheet.getRow(row).getCell(col),
					sheet.getRow(row).getCell(col).getCachedFormulaResultType());
		}
		return sheet.getRow(row).getCell(col).getStringCellValue();
	}

	public final static int LABEL_ROW = 26;

	public static Integer findLastStage(XSSFSheet totalsSheet) {
		int lastStage = -1;
		int labelRow = LABEL_ROW;
		for (int i = 0; i < 500; i++) {
			if (totalsSheet.getRow(labelRow).getCell(i) == null) {
				continue;
			}
			if (!getCellStringValue(totalsSheet, labelRow, i).equals("")
					& getCellStringValue(totalsSheet, labelRow + 1, i).equals("")) {
				return i;
			}
		}
		return lastStage;
	}

	public static LinkedHashMap<String, String> getSandMap(XSSFSheet sheet) {
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		return map;
	}

	public static String getEpochMilli() {
		LocalDateTime localDateTime = LocalDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.of(0, 0));
		LocalDateTime now = LocalDateTime.now();
		return String.valueOf(Duration.between(localDateTime, now).toMillis());
	}

	public static String getString() {
		return "https://cwslogin.b2clogin.com/4f0f19d0-f44c-4a03-b8cb-ab327bd2b12b/B2C_1A_P2_V1_SignIn_Prod/api/CombinedSigninAndSignup/confirmed?rememberMe=false&csrf_token=bTJtT0xHOFhPVGY2UTBYNGVkSmxGYWc3T0FwYS92ZHhSRG1mb0tsV0wvWlk5bHZzVjhKVWgzb0RCRk9yZmRmb0hQWDF6dWEvNVRaMi9IZW81UkE3amc9PTsyMDIzLTA0LTExVDIyOjQ4OjUzLjM1MTM5MjNaO1BoY0Y4aFVIUzhtaFUrUW0yRTVTMUE9PTt7Ik9yY2hlc3RyYXRpb25TdGVwIjo2fQ==&tx=StateProperties=eyJUSUQiOiJiNDJhM2Y3ZS04ZDUyLTRmNGItYjg4ZS0xZGI1NjZiMDM0YzQifQ&p=B2C_1A_P2_V1_SignIn_Prod&diags=%7B%22pageViewId%22%3A%22e4a0bac5-6ad8-48bf-81ff-07f40ee70dfb%22%2C%22pageId%22%3A%22CombinedSigninAndSignup%22%2C%22trace%22%3A%5B%7B%22ac%22%3A%22T005%22%2C%22acST%22%3A1681253333%2C%22acD%22%3A1%7D%2C%7B%22ac%22%3A%22T021%20-%20URL%3Ahttps%3A%2F%2Fb2cstorage-cdnendpoint-prod.azureedge.net%2Fb2ccardsprod%2Frfv%2Fen%2Fhrd-collect-username.html%22%2C%22acST%22%3A1681253333%2C%22acD%22%3A4%7D%2C%7B%22ac%22%3A%22T019%22%2C%22acST%22%3A1681253333%2C%22acD%22%3A4%7D%2C%7B%22ac%22%3A%22T004%22%2C%22acST%22%3A1681253333%2C%22acD%22%3A2%7D%2C%7B%22ac%22%3A%22T003%22%2C%22acST%22%3A1681253333%2C%22acD%22%3A2%7D%2C%7B%22ac%22%3A%22T035%22%2C%22acST%22%3A1681253333%2C%22acD%22%3A0%7D%2C%7B%22ac%22%3A%22T030Online%22%2C%22acST%22%3A1681253333%2C%22acD%22%3A0%7D%2C%7B%22ac%22%3A%22T002%22%2C%22acST%22%3A1681253360%2C%22acD%22%3A0%7D%2C%7B%22ac%22%3A%22T018T010%22%2C%22acST%22%3A1681253360%2C%22acD%22%3A286%7D%5D%7D";
	}

	public static void remoteFleetVisionLogin() throws InterruptedException, IOException {
		HttpClient client = HttpClient.newBuilder().cookieHandler(getCookieHandler()).followRedirects(Redirect.ALWAYS)
				.build();
		HttpRequest request = HttpRequest.newBuilder().header("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
				.header("Accept-Encoding", "gzip, deflate, br")

				.uri(URI.create(
						"https://cwslogin.b2clogin.com/4f0f19d0-f44c-4a03-b8cb-ab327bd2b12b/b2c_1a_p2_v1_signin_prod/oauth2/v2.0/authorize?scope=https://cwslogin.onmicrosoft.com/745fa34b-93d9-4160-86e1-c87b01d401e8/read&application=/rfv&response_type=code&state=eyJyZWRpcmVjdFVybCI6Ii8iLCJjb2RlVmVyaWZpZXIiOiJyRTBmR2p4V1gwLzA4cndCUE1ZT0pGV3N1MFh6RW9VdVRXV0pkakhxV0xRa241WWlkbkpOZ2lVMVQ1eWcyK0tlSlpQdDkxRFpPQ0poQVgvdVp3cmtIdz09Iiwic2VlZCI6InRtQWhVeWd1NTBwS2NTMmM0TmpkMHc9PSJ9&nonce=defaultNonce&client_id=83321aa9-416e-49f9-8475-20b1fa5e57c9&dc=us-all&code_challenge=MuZqx71YM1bOt_ldZ2gf0rqcop61rwzXVT0co0Fyd54&code_challenge_method=S256&redirect_uri=https://www.remotefleetvision.com"))
				.GET().build();
		HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

		InputStream inputStream = response.body();
		String[] cookies = { "" };
		client.cookieHandler().get().get(request.uri(), request.headers().map())
				.forEach((String string, List<String> list) -> {
					System.out.println("key = " + string);
					list.forEach((String value) -> {
						cookies[0] += value;
					});
				});

		for (Certificate cert : response.sslSession().get().getPeerCertificates()) {
			PublicKey publicKey = cert.getPublicKey();
			System.out.println(publicKey);
			System.out.println(publicKey.getAlgorithm());
			System.out.println(publicKey.getFormat());
		}
		diskCacheRequest(client);
		System.out.println("Sam");
		perfTraceRequest(client, cookies[0], getCSRFToken(response.headers().map().get("set-cookie")));
		System.out.println("Sam");

		// request2(client,cookies[0],response.headers().map().get("set-cookie"));
		/*
		 * while ((i = inputStream.read(bytes)) > -1) { String string =
		 * getStringFromBytes(bytes); System.out.println(string); bytes = new
		 * byte[1024]; }
		 */

	}

	public static void diskCacheRequest(HttpClient client) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(
				"https://b2cstorage-cdnendpoint-prod.azureedge.net/b2ccardsprod/rfv/en/hrd-collect-username.html"))
				.build();
		HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		response.headers().map().forEach((String string, List<String> list) -> {
			System.out.println("HEADER NAME: " + string);
			System.out.println("HEADER VALUE: " + list);
		});
	}

	public static void perfTraceRequest(HttpClient client, String cookies, String csrf)
			throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder().header("Accept", "application/json, text/javasript, */*; q=0.01")
				.header("Accept-Encoding", "gzip, deflate, br")
				.header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
				.header("Cookie", "x-ms-cpim-dc=us-all; " + cookies).header("X-CSRF-TOKEN", csrf)
				.uri(URI.create(
						"https://cwslogin.b2clogin.com/4f0f19d0-f44c-4a03-b8cb-ab327bd2b12b/B2C_1A_P2_V1_SignIn_Prod/SelfAsserted?tx=StateProperties=eyJUSUQiOiI4MGFiOGQ4ZS1kODI4LTQxNWYtOGM0NC1kYTIwMjg3MzMzOTgifQ&p=B2C_1A_P2_V1_SignIn_Prod"))
				.POST(HttpRequest.BodyPublishers.ofString(getUserNamePayload())).build();
		HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

	}

	public static String getUserNamePayload() {
		return "request_type=RESPONSE&signInName=samuel.parker%40propetroservices.com";
	}

	public static void request2(HttpClient client, String cookies, List<String> headers)
			throws InterruptedException, IOException {
		System.out.println("The illegal character = " + getURI(headers).charAt(498));
		HttpRequest request = HttpRequest.newBuilder()
				.header("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
				.header("accept", "application/json, text/javascript, */*; q=0.01")
				.header("Accept-Encoding", "gzip, deflate, br").header("cookie", cookies)
				.header("X-CSRF-TOKEN", getCSRFToken(headers)).uri(URI.create(getURI(headers)))

				.POST(HttpRequest.BodyPublishers.ofString(getPayload())).build();
		HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		List<String> headers2 = response.headers().map().get("set-cookie");
		String[] cookies1 = { "" };
		response.headers().map().get("set-cookie").forEach((String string) -> {
			cookies1[0] += "; " + string;
		});
		request3(client, cookies1[0], headers2);
	}

	public static void request3(HttpClient client, String cookies, List<String> headers)
			throws InterruptedException, IOException {

		HttpRequest request = HttpRequest.newBuilder().header("accept", "application/json, text/plain, */*")
				.header("accept-encoding", "gzip, deflate, br").header("content-type", "application/json")
				.header("cookie", cookies).uri(URI.create("https://www.remotefleetvision.com/auth/GetAccessToken"))
				.POST(HttpRequest.BodyPublishers.ofString(getOtherPayload())).build();
		HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		Scanner scanner = new Scanner(response.body());
		while (scanner.hasNext()) {
			System.out.println(scanner.next());
		}
	}

	public static String getCSRFToken(List<String> headers) {
		String csrfToken = "";
		for (String s : headers) {
			Matcher matcher = Pattern.compile("x-ms-cpim-csrf\\=((.)+?)\\=").matcher(s);
			if (matcher.find()) {
				csrfToken = matcher.group().substring(15);
				break;
			}
		}
		return csrfToken;
	}

	public static String getURI(List<String> headers) {
		String csrfToken = "";
		String uri = "https://cwslogin.b2clogin.com/4f0f19d0-f44c-4a03-b8cb-ab327bd2b12b/B2C_1A_P2_V1_SignIn_Prod/api/CombinedSigninAndSignup/confirmed?rememberMe=false&csrf_token=";
		for (String s : headers) {
			Matcher matcher = Pattern.compile("x-ms-cpim-csrf\\=((.)+?)\\=").matcher(s);
			if (matcher.find()) {
				csrfToken = matcher.group().substring(15);
				break;
			}
		}
		System.out.println(csrfToken);
		uri += csrfToken;
		uri += URLEncoder.encode(
				"=&tx=StateProperties=eyJUSUQiOiJiNDJhM2Y3ZS04ZDUyLTRmNGItYjg4ZS0xZGI1NjZiMDM0YzQifQ&p=B2C_1A_P2_V1_SignIn_Prod&diags={\"pageViewId\":\"e4a0bac5-6ad8-48bf-81ff-07f40ee70dfb\",\"pageId\":\"CombinedSigninAndSignup\",\"trace\":[{\"ac\":\"T005\",\"acST\":1681253333,\"acD\":1},{\"ac\":\"T021 - URL:https://b2cstorage-cdnendpoint-prod.azureedge.net/b2ccardsprod/rfv/en/hrd-collect-username.html\",\"acST\":1681253333,\"acD\":4},{\"ac\":\"T019\",\"acST\":1681253333,\"acD\":4},{\"ac\":\"T004\",\"acST\":1681253333,\"acD\":2},{\"ac\":\"T003\",\"acST\":1681253333,\"acD\":2},{\"ac\":\"T035\",\"acST\":1681253333,\"acD\":0},{\"ac\":\"T030Online\",\"acST\":1681253333,\"acD\":0},{\"ac\":\"T002\",\"acST\":1681253360,\"acD\":0},{\"ac\":\"T018T010\",\"acST\":1681253360,\"acD\":286}]}",
				StandardCharsets.UTF_8);
		return uri;
	}

	public static String getOtherPayload() {
		return "{\"accessCode\":\"eyJraWQiOiJqYVVPM3VweUljVkc0cmY1blpEOVhQd2Q4c2FhcHhTXzFaelBvcEJqT3FZIiwidmVyIjoiMS4wIiwiemlwIjoiRGVmbGF0ZSIsInNlciI6IjEuMCJ9.bS7lANL4bYi6qZ-ifWxv5BhK2zWQ9EcsQ7uKBLr30cPP_RUBGef_ACv7sdTN4gFv8bScPnuTyxvoTf5n4BSVDehAn9NydIumyBo0HtMLlb9FQYrGhhqX7n2zTTlneIuqb74UnukB9o7TUYnDZHLKPEXvbsvTx24QgosSwqdRoRz4yGSNrf6HTEkTP3I8vRijrNVM1OeTWNeH_gUhM04MbgAmUNkIW3qWhSWhckqSJoNi0heSutNm19ShT4HOzse93tKh_L44CcawcJNT8B9HJ5huF5zSnMjF-fv20UJt26haQtQ7qcPAHwppPY-Rk_stO1qtR3c4sK2rbG_j7cXQRg.DNTbPd7UznAIDYct.qjE7Jd0-u4phrQFqzP9NNmh0ZrNMVT6agZTfyW8aCDhCBlE5y8GQG6DIuXVW8fXInO68oXXP_pz_xR20ccu5nvIvVaVubVbR5APKpM5A7L9oih1vFk_Q1OxQcri7xwDram1pVZYfbNrHNNvMv-ZiWeBfwv4-8gZejQZr6B9VrqYnZOF_Rg3G7GPNHZxxRu85S5JzvDaNcLFWaE6Do8QrX5_rt9hmaBPCIbMCKR4S9B2oaC13IUlclodHCwrO0PIAFtayZvyUM1a9SzqBcgtiMRTKtW-GkfthOiU3U3nhNFy98IHMlosc-LGOarkW-FnuYGy2OIzls9oAk18hpO9lPVaUP7t74Dr8AncRhKnc6xpcQhXbYJJJcEuTwrardNNMSMaSmmEtvTSPtHqHb4mR0UxlEeXmq-_jGLXM6f6axUzCw6ZGfiu-ohBx4indLUpBGlJ-bHXq5dONNTk8tM4SOIfTvcUcXISy8CGH2mug1BzHmyuiuNRa1u72eO2wDqn3UtGCLTERbnVOeD_OXccnLn_9sAHyyWXXHqbnhjPxKFFSeRtNDeDyMdGijNa0AoXAACGFDKIbv0z7NDSZF3f2R6boe31rxkexBJHOkMH2DSZJ4XfscZexTxreA6zyR4DKQAgRYIRwdVz9_mMt_P7PCThwj1HBykGBOEbU2LK8L60M0g07M3qYe2oE14PmvzNM13uLYLsgVDlMqvI2nDEwiRdalE3h4IIMCaLqSUFW4JQ_DMoF0OIfM33ME9jxqPjjXFi4IB9ya3rj3MsUc6zBfxnlmjob3FF4LnIrQrfFbReQqglu15_3zxWh8EefgmmTdI12zbP_vvI2Q4wZAUm-l1NKskTnLLqC_cjUCYrt1s5hvn7X0oYw_-Qf9Rjpql5kQe-kJm_yvO8jIJiJvpOaOwLkppxq0hZuIxofHuHyRCOHVK93di7U_VugclpJk-a2odud-kNonz_umuRib-467SMDozkfSVEG8n7z9y5ntC0fGFvnpQG5LS_6m48bgXkLbDCuGkz5oikp0sIJQY_JoRWbXOMS3xBdDeIAPextH92sH8Ucm-W-PIKLc1DnH2rZPgbB0aRJoZ1F_vY37WsqmX6n4g3svb-RAKXlWaBzEwvsdI6rcJdefZiCx0mUJcMm2sQE3uSYevA1U8F4uwuXGzKHZohvuAU.FE5iQvM1nahEN7EJlReHVw\",\"codeVerifier\":\"zbROFKv6LjL232EJzYaPgngJ+SIQASO1SqjWa95XAKmnAveazE5l/TC78jMmF/wECQLnJ73sq15nuA2ic3b9lQ==\",\"seed\":\"f3evJ51/lIquHpyo6vAgYw==\"}";
	}

	public static String getPayload() {
		return "request_type=REQUEST&signInName=samuel.parker%40propetroservices.com&password=Ranchbronc.311";
	}

	public static CookieHandler getCookieHandler() {
		CookieManager cookieManager = new CookieManager();
		CookieHandler.setDefault(cookieManager);
		CookieHandler cookieHandler = CookieHandler.getDefault();
		return cookieHandler;
	}

	public static ArrayList<String> getArrayOfString() {
		ArrayList<String> array = new ArrayList<>();
		array.add("Sam Parker");
		array.add("Carlos Barba");
		array.add("Victor Espinoza");
		array.add("Zack Hernandez&&Frank Martinez");
		return array;
	}

	public static int getRealNumChannels(ArrayList<String> channels) {
		int addChannels = 0;
		for (String s : channels) {
			Matcher matcher = Pattern.compile("\\&\\&").matcher(s);
			if (matcher.find()) {
				addChannels++;
			}
		}
		return channels.size() + addChannels;
	}

	public static ArrayList<String> getArrayOfValues(HashMap<String, String> map) {
		ArrayList<String> array = new ArrayList<>();
		for (String s : map.keySet()) {
			array.add(map.get(s));
		}
		return array;
	}

	public static void saveISIPAvgToWorkbook(String fileName, LinkedHashMap<String, LinkedHashMap<String, Double>> map)
			throws IOException {
		XSSFWorkbook workbook = new XSSFWorkbook(XSSFWorkbookType.XLSM);
		createSheetsFromSet(map.keySet(), workbook);
		writeMapToWorkbook(workbook, map);
		saveWorkbook(workbook, fileName);
	}

	public static void writeMapToWorkbook(XSSFWorkbook workbook,
			LinkedHashMap<String, LinkedHashMap<String, Double>> map) {
		int i = 0;
		for (String s : map.keySet()) {
			int[] count = { 0 };
			XSSFSheet sheet = createHeadersInSheet(map.get(s).keySet(), workbook.getSheetAt(i));
			map.get(s).forEach((String formation, Double isip) -> {
				int col = count[0];
				checkCreateRowCell(sheet, 1, col, CellType.NUMERIC);
				sheet.getRow(1).getCell(col).setCellValue(isip);
				checkCreateRowCell(sheet, 2, col, CellType.STRING);
				sheet.getRow(2).getCell(col).setCellValue(formation);
				count[0] = count[0] + 1;
			});
			i++;
		}
	}

	public static void checkCreateRowCell(XSSFSheet sheet, int row, int column, CellType cellType) {
		if (sheet.getRow(row) == null) {
			sheet.createRow(row);
		}
		sheet.getRow(row).createCell(column).setCellType(cellType);
	}

	public static XSSFSheet createHeadersInSheet(Collection<String> collection, XSSFSheet sheet) {
		int col = 0;
		sheet.createRow(0);
		for (String s : collection) {
			sheet.getRow(0).createCell(col).setCellType(CellType.STRING);
			sheet.getRow(0).createCell(col).setCellValue(s);
			col++;
		}
		return sheet;
	}

	public static void createSheetsFromSet(Collection<String> collection, XSSFWorkbook workbook) {
		for (String s : collection) {

			workbook.createSheet(s != null ? s : "NO NAME");
		}
	}

	public static ArrayList<HashMap<String, String>> filterAlerts(ArrayList<HashMap<String, String>> alerts) {
		ArrayList<HashMap<String, String>> newArray = new ArrayList<>();
		alerts.forEach((HashMap<String, String> map) -> {
			if (map.containsKey("Parameter") && map.get("Parameter").contains("PE Vibration")) {
				System.out.println("sam");
			}
			if (map.containsKey("Parameter")
					&& map.get("Parameter").substring(3, map.get("Parameter").length() - 4).equals("PE Vibration")
							& map.get("Value").contains("-25")) {
				newArray.add(map);
			}
		});
		return newArray;
	}

	// "SPLIT([" + DataNames.PERFS + "],-);NAMES:{\"TOP PERF\",\"BOTTOM
	// PERF\"};length==2",
	public final static Double BBL_FT_RATE = .021639303;
	public final static Double WATER_RO_FT3 = 62.2;
	public final static Double P110_5_HALF_INNER_DIAM_FT = .39367;
	public final static String BOTTOM_PERF = "Bottom Perf";
	public final static String TOP_PERF = "Top Perf";

	public final static String LESS_THAN = "LESS_THAN\\(([\\d\\.]+)\\)";
	public final static String GREATER_THAN = "GREATER_THAN\\([\\d\\.]+)\\)";
	public final static String EQUAL_TO = "EQUAL_TO\\([\\d\\.]+)\\)";

	public static LinkedHashMap<String, LinkedHashMap<String, Double>> isipAverages() throws IOException,
			InterruptedException, ClassNotFoundException, InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ShortBufferException {
		EvaluatedDataObject evaluatedDataObject = EvaluatedDataObject.getFromFile();
		LinkedHashMap<String, LinkedHashMap<String, Double>> map = getAverageISIPs(evaluatedDataObject);
		for (String s : map.keySet()) {
			System.out.println(s);
			for (String ss : map.get(s).keySet()) {
				System.out.println(ss);
				System.out.println(map.get(s).get(ss));
			}
		}
		return map;
	}

	public static void getAlertsInTimeFrame(String timeframe, ArrayList<String> filters, String valueCondition) {

	}

	@SuppressWarnings("unchecked")
	public static <T> void createDataInWorkbook(Boolean organized, T t, String... dataNames) throws IOException,
			ClassNotFoundException, InterruptedException, InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ShortBufferException {
		EvaluatedDataObject evaluatedDataObject = EvaluatedDataObject.getFromFile();

		if (organized) {
			LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, ArrayList<Double>>>> map = getOrganizedData(
					evaluatedDataObject, true, dataNames);
			saveMapToWorkbook(map);
		} else {
			LinkedHashMap<String, ArrayList<Double>> map = getAllDataNoOrg(evaluatedDataObject, dataNames);
			saveMapToWorkbook(map);
		}
	}

	public final static String SPLIT_VALUE = "value";
	public final static String SPLIT_DELIMITER = "delimiter";
	public final static String SPLIT_LENGTH = "length";

	public static LinkedHashMap<String, LinkedHashMap<String, Double>> getAverageISIPs(
			EvaluatedDataObject evaluatedDataObject) throws IOException, InterruptedException, InvalidKeyException,
			ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException, ShortBufferException {

		LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, ArrayList<Double>>>> map = getOrganizedData(
				evaluatedDataObject, false, DataNames.ISIP);

		return evalMapArrayAverages(map);
	}

	public static LinkedHashMap<String, LinkedHashMap<String, Double>> evalMapArrayAverages(
			LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, ArrayList<Double>>>> map) {

		LinkedHashMap<String, LinkedHashMap<String, Double>> averagesMap = new LinkedHashMap<>();

		for (String s : map.keySet()) {
			int[] count = { 0 };
			map.get(s).forEach((String string, LinkedHashMap<String, ArrayList<Double>> valueMap) -> {
				if (count[0] == 0) {
					averagesMap.put(s, new LinkedHashMap<>());
					count[0] = 1;
				}
				averagesMap.get(s).put(string, averageArray(valueMap.get(DataNames.ISIP)));
			});
		}
		return averagesMap;

	}

	public static Double averageArray(ArrayList<Double> array) {
		if (array == null) {
			return 0.0;
		}
		double addedValues = 0.0;
		for (Double d : array) {
			addedValues += d;
		}
		return addedValues / Double.valueOf(array.size());
	}

	public static String getSplitStringName(String splitFuncString, int index) {
		Matcher matcher = Pattern.compile("NAMES\\:\\{([^\\}]+?)\\}").matcher(splitFuncString);
		String[] namesArray = getNamesArray(matcher.group());
		return namesArray[index];
	}

	public static String[] getNamesArray(String namesParam) {
		String[] namesArray = new String[0];
		Matcher matcher = Pattern.compile("\"((.)+?)\"").matcher(namesParam);
		while (matcher.find()) {
			String found = matcher.group();
			namesArray = pushBackArray(namesArray, found.substring(1, found.length() - 1));
		}
		return namesArray;
	}

	public static LinkedHashMap<String, String> getSplitMap(String splitFuncString) {
		String params = getParenthetical(splitFuncString);
		String delim = params.split(",")[1];
		String value = params.split(",")[0].substring(1, params.split(",")[0].length() - 1);
		Integer length = getSplitLength(splitFuncString);
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		map.put(SPLIT_VALUE, value);
		map.put(SPLIT_DELIMITER, delim);
		map.put(SPLIT_LENGTH, String.valueOf(length));
		for (int i = 0; i < length; i++) {
			map.put(getSplitFormattedString(map, i), getSplitStringName(splitFuncString, i));
		}
		return map;
	}

	public static <T> boolean areValuesArrayType(LinkedHashMap<String, T> map) {
		for (Entry<String, T> entry : map.entrySet()) {
			return entry.getValue().getClass().getName().equals("java.util.ArrayList");
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public static <T> void saveMapToWorkbook(LinkedHashMap<String, T> map) throws IOException {
		if (areValuesArrayType(map)) {
			saveMapToSingleSheet((LinkedHashMap<String, ArrayList<Double>>) map);
			return;
		}
		XSSFWorkbook workbook = new XSSFWorkbook();
		workbook.setWorkbookType(XSSFWorkbookType.XLSM);
		createSheetSetHeaders(workbook,
				(LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, ArrayList<Double>>>>) map);
	}

	public static void saveMapToSingleSheet(LinkedHashMap<String, ArrayList<Double>> map) throws IOException {
		XSSFWorkbook workbook = new XSSFWorkbook(XSSFWorkbookType.XLSM);
		XSSFSheet sheet = createSheetSetHeaders(workbook, "All_Data", map);
		writeArraysToColumns(map, sheet, 0, 0);
		saveWorkbook(workbook, "All_Data.xlsm");
	}

	public static XSSFSheet createSheetSetHeaders(XSSFWorkbook workbook, String sheetName,
			LinkedHashMap<String, ArrayList<Double>> map) {
		XSSFSheet sheet = workbook.createSheet(sheetName);
		int numHeaders = map.size();
		createStringRowCells(sheet, 0, 0, numHeaders);
		writeHeadersRow(sheet, 0, 0, map.keySet());
		return sheet;
	}

	public static void writeArraysToColumns(LinkedHashMap<String, ArrayList<Double>> map, XSSFSheet sheet,
			int headersRow, int headersCol0) {
		for (int i = headersCol0; i <= (headersCol0 + map.size()); i++) {
			String header = sheet.getRow(headersRow).getCell(i).getStringCellValue();
			if (map.containsKey(header)) {
				createNumCellsColumn(sheet, 1, i, map.get(header).size());
				writeArrayToColumn(sheet, 1, i, map.get(header));
				continue;
			}
			System.out.println("Map does not contain the key: " + header);
			System.out.println("Map Key Set = " + map.keySet());
		}
	}

	public static void writeHeadersRow(XSSFSheet sheet, int row, int startCol, Set<String> headerNames) {
		int count = 0;
		for (String s : headerNames) {
			sheet.getRow(row).getCell(startCol + count).setCellValue(s);
			count++;
		}
	}

	private static void createRowCell(XSSFSheet sheet, int row, int col, CellType cellType) {
		sheet.createRow(row);
		sheet.getRow(row).createCell(col);
		sheet.getRow(row).getCell(col).setCellType(cellType);
	}

	private static void createCell(XSSFRow row, int col, CellType cellType) {
		row.createCell(col);
		row.getCell(col).setCellType(cellType);
	}

	public static void checkCreateCell(XSSFSheet sheet, int row, int col, CellType cellType) {
		if (sheet.getRow(row) == null) {
			createRowCell(sheet, row, col, cellType);
			return;
		}
		createCell(sheet.getRow(row), col, cellType);
	}

	public static void createStringRowCells(XSSFSheet sheet, int row, int col1, int col2) {
		if (col1 == col2) {
			checkCreateCell(sheet, row, col1, CellType.STRING);
			return;
		}
		int iterDir = (col2 - col1) / (Math.abs(col2 - col1));
		for (int i = col1; i != col2 + iterDir; i = i + iterDir) {
			checkCreateCell(sheet, row, i, CellType.STRING);
		}
	}

	public static void saveWorkbook(XSSFWorkbook workbook) throws IOException {
		while (RedTreatmentReport.checkFileOpen(new File("data.xlsm"))) {
			try {
				Thread.sleep(1000);
				System.out.println("Waiting");
			} catch (InterruptedException e) {
				continue;
			}
		}
		FileOutputStream fileOutputStream = new FileOutputStream(new File("data.xlsm"));
		workbook.write(fileOutputStream);
		fileOutputStream.flush();
		fileOutputStream.close();
		workbook.close();
	}

	public static void saveWorkbook(XSSFWorkbook workbook, String fileName) throws IOException {
		while (RedTreatmentReport.checkFileOpen(new File(fileName))) {
			try {
				Thread.sleep(1000);
				System.out.println("Waiting");
			} catch (InterruptedException e) {
				continue;
			}
		}
		FileOutputStream fileOutputStream = new FileOutputStream(new File(fileName));
		workbook.write(fileOutputStream);
		workbook.close();
		fileOutputStream.close();
	}

	public static String getParenthetical(String string) {
		Matcher matcher = Pattern.compile("\\((.+?)\\)").matcher(string);
		if (matcher.find()) {
			return string.substring(matcher.start() + 1, matcher.end() - 1);
		}
		return string;
	}

	public static void createSheetSetHeaders(XSSFWorkbook workbook,
			LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, ArrayList<Double>>>> map)
			throws IOException {
		for (String county : map.keySet()) {
			XSSFSheet sheet = workbook.createSheet(county == null ? "nothing" : county);
			int column = 0;
			int mergedSize = 0;
			for (String formation : map.get(county).keySet()) {
				if (column == 0) {
					createCells(sheet, 0, 0, 0, map.get(county).size() * map.get(county).get(formation).size());
					mergedSize = map.get(county).get(formation).size() - 1;
				}
				ExcelTransfer.changeTypeToString(sheet, 0, column);
				sheet.getRow(0).getCell(column).setCellValue(formation);
				for (String name : map.get(county).get(formation).keySet()) {
					ExcelTransfer.changeTypeToNumeric(sheet, 1, column);
					sheet.getRow(1).getCell(column).setCellValue(name);
					createNumCellsColumn(sheet, 2, column, map.get(county).get(formation).get(name).size() + 2);
					writeArrayToColumn(sheet, 2, column, map.get(county).get(formation).get(name));
					column++;
				}
			}
		}
		saveWorkbook(workbook);
	}

	public static void writeArrayToColumn(XSSFSheet sheet, int startRow, int column, ArrayList<Double> array) {
		int count = 0;
		for (Double d : array) {
			sheet.getRow(startRow + count).getCell(column).setCellValue(d);
			count++;
		}
	}

	public static void createNumCellsColumn(XSSFSheet sheet, int startRow, int column, int numCells) {
		for (int i = 0; i < numCells; i++) {
			if (sheet.getRow(startRow + i) == null) {
				sheet.createRow(startRow + i).createCell(column);
				sheet.getRow(startRow + i).createCell(column);
				sheet.getRow(startRow + i).getCell(column).setCellType(CellType.NUMERIC);
				continue;
			} else if (sheet.getRow(startRow + i).getCell(column) == null) {
				sheet.getRow(startRow + i).createCell(column);
				sheet.getRow(startRow + i).getCell(column).setCellType(CellType.NUMERIC);
				continue;
			}
			sheet.getRow(startRow + i).getCell(column).setCellType(CellType.NUMERIC);
		}
	}

	public static String getAlphabetIndex(int index) {
		int[] iArray = getHexArray(Float.valueOf(index), 26);
		return getAlphIndexString(iArray);
	}

	public static String getAlphIndexString(int[] iArray) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i : iArray) {
			int charEquiv = i + 65;
			System.out.println(charEquiv);
			stringBuilder.append((char) charEquiv);
		}
		return stringBuilder.toString().toUpperCase();
	}

	public static int getHexLength(Float num, int base) {
		int count = 1;
		while (Math.pow(base, count) <= num) {
			count++;
		}
		return count;
	}

	public static int[] getHexArray(Float num, int base) {
		if (num < base) {
			return new int[] { num.intValue() };
		}
		int[] iArray = new int[getHexLength(num, base)];
		int count = 0;
		for (int i = iArray.length - 1; i >= 0; i--) {
			Float power = Float.valueOf(String.valueOf(Math.pow(base, i)));
			Float chunk = power == 0f ? num : ((num.intValue() / power.intValue()));
			num = num - (chunk.intValue() * power.intValue());
			iArray[count] = chunk.intValue() - i;
			count++;
		}
		return iArray;
	}

	public static void createCells(XSSFSheet sheet, CellType cellType, int r1, int c1, int r2, int c2) {
		final int direction = r2 != r1 ? (r2 - r1) / (Math.abs(r2 - r1)) : 1;
		r2 = r2 == r1 ? r1 + 1 : r2;
		final int direction2 = c2 != c1 ? (c2 - c1) / (Math.abs(c2 - c1)) : 1;
		c2 = c2 == c1 ? c1 + 1 : c2;
		int i;
		for (i = r1; i != r2; i += direction) {
			sheet.createRow(i);
			for (int ii = c1; ii != c2; ii += direction2) {
				sheet.getRow(i).createCell(ii);
				sheet.getRow(i).getCell(ii).setCellType(cellType);
			}
		}
	}

	public static void createCells(XSSFSheet sheet, int r1, int c1, int r2, int c2) {
		final int direction = r2 != r1 ? (r2 - r1) / (Math.abs(r2 - r1)) : 1;
		r2 = r2 == r1 ? r1 + 1 : r2;
		final int direction2 = c2 != c1 ? (c2 - c1) / (Math.abs(c2 - c1)) : 1;
		c2 = c2 == c1 ? c1 + 1 : c2;
		int i;
		for (i = r1; i != r2; i += direction) {
			sheet.createRow(i);
			for (int ii = c1; ii != c2; ii += direction2) {
				sheet.getRow(i).createCell(ii);
				sheet.getRow(i).getCell(ii).setCellType(CellType.STRING);
			}
		}
	}

	public static void createMergedCells(XSSFSheet sheet, int r1, int c1, int r2, int c2) {
		CellRangeAddress cellRangeAddress = new CellRangeAddress(r1, c1, r2, c2);
		sheet.addMergedRegion(cellRangeAddress);

	}

	public static JobLogWells getJobLogWells() throws IOException, InterruptedException, InvalidKeyException,
			ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException, ShortBufferException {
		HashMap<String, String> creds = EncryptCredentials.getUserCredentials(LoginRequest.PETRO_IQ_CRED_PATH,
				"PetroIQ Login");
		LoginRequest loginRequest = new LoginRequest(creds.get(UserNamePassword.USERNAME),
				creds.get(UserNamePassword.PASSWORD));
		String token = loginRequest.getToken();
		JobLogWells jobLogWells = WellsRequest.setWellMap(token);
		jobLogWells.setToken(token);
		jobLogWells.setFormationMap(token);
		return jobLogWells;
	}

	public static String formatCalcString(String calcString, String[] values, LinkedHashMap<String, String> map) {
		Matcher matcher = Pattern.compile("\\[\\d\\]").matcher(calcString);
		ArrayList<Integer> array = new ArrayList<>();
		while (matcher.find()) {
			Integer i = Integer.valueOf(String.valueOf(matcher.group().charAt(1)));
			array.add(i);
			System.out.println(matcher.group().charAt(1));
		}
		Matcher matcher2 = Pattern.compile("[\\*/\\+\\-]").matcher(calcString);
		String formatted = "";
		for (Integer i : array) {

			formatted += map.get(values[i]);
			if (matcher2.find()) {
				formatted += matcher2.group();
			}
		}
		matcher.reset(formatted);

		Matcher matcher3 = Pattern.compile("([\\d\\.]+)[\\*/\\+\\-]([\\d\\.]+)|([\\d\\.]+)").matcher(formatted);
		String calculated = new String(formatted);
		while (matcher3.find()) {
			String operation = matcher3.group();
			matcher2.reset(operation);
			if (!matcher2.find()) {
				return operation;
			}
			String operand = matcher2.group();
			String term1 = operation.split("\\" + operand)[0];
			String term2 = operation.split("\\" + operand)[1];
			formatted = formatted.replace(operation, UserDefinedFrame.calculateString(term1, term2, operand));
			matcher3.reset(formatted);
		}
		return "0.0";
	}

	public static String[] addChemNamesToArray(String[] dataNames, LinkedHashMap<String, String> chemMap) {
		String[] newDataNames = new String[dataNames.length + chemMap.size()];
		int i = 0;
		for (String s : dataNames) {
			newDataNames[i] = s;
			i++;
		}
		for (String s : chemMap.keySet()) {
			String fixed = s.toUpperCase().replace(" ", "");
			newDataNames[i] = fixed;
			i++;
		}
		return newDataNames;
	}

	public static HashMap<String, String> getChemNameMap(LinkedHashMap<String, String> chemMap) {
		HashMap<String, String> map = new HashMap<>();
		for (String s : chemMap.keySet()) {
			map.put(s.toUpperCase().replace(" ", ""), s);
		}
		return map;
	}

	public static String getSplitFormattedString(HashMap<String, String> splitMap, Integer index) {
		return splitMap.get(SPLIT_VALUE) + "(" + splitMap.get(SPLIT_DELIMITER) + ")" + "@" + index;
	}

	public static String[] getFormattedDataNames(String[] dataValueNames) {
		String[] fixedArray = new String[0];
		for (String s : dataValueNames) {
			if (s.contains("SPLIT")) {
				HashMap<String, String> map = getSplitMap(s);
				int length = Integer.parseInt(map.get(SPLIT_LENGTH));
				for (int i = 0; i < length; i++) {
					fixedArray = pushBackArray(fixedArray, getSplitFormattedString(map, i));
				}
				continue;
			}
			fixedArray = pushBackArray(fixedArray, s);
		}
		return fixedArray;
	}

	public static boolean checkForPerfRequest(String... dataValueNames) {
		for (String s : dataValueNames) {
			if (s.equals(BOTTOM_PERF) | s.equals(TOP_PERF)) {
				return true;
			}
		}
		return false;
	}

	public static HashMap<String, String> getIndPerfs(String perfs) {
		if (perfs == null) {
			return null;
		}
		Matcher matcher = Pattern.compile("[\\d\\.]+").matcher(perfs);
		HashMap<String, String> map = new HashMap<>();
		int count = 0;
		boolean found;
		while ((found = matcher.find()) | count < 2) {
			String name = count == 0 ? TOP_PERF : BOTTOM_PERF;
			String value = found ? matcher.group() : "-1.0";
			map.put(name, value);
			count++;
		}
		return map;
	}

	// {COUNTY,{FORMATION,{DATAVALUE,VALUES}}}
	public static LinkedHashMap<String, ArrayList<Double>> getAllDataNoOrg(EvaluatedDataObject evaluatedDataObject,
			String... dataValueNames) {
		LinkedHashMap<String, ArrayList<Double>> map = getMapShell(dataValueNames);
		HashMap<String, HashMap<Integer, LinkedHashMap<String, String>>> sigValsMap = evaluatedDataObject
				.getSigValsMaps();
		boolean addIndPerfs = checkForPerfRequest(dataValueNames);
		for (String wellName : sigValsMap.keySet()) {
			sigValsMap.get(wellName).forEach((Integer i, HashMap<String, String> stageMap) -> {
				for (String dataName : dataValueNames) {
					if (addIndPerfs) {
						String perfs = stageMap.get(DataNames.PERFS);
						HashMap<String, String> perfsMap = getIndPerfs(perfs);
						if (perfsMap == null || Double.valueOf(perfsMap.get(BOTTOM_PERF)) == 0.0) {
							break;
						}
						stageMap.putAll(getIndPerfs(perfs));
					}
					if (dataName.matches("^CALC:(.+)")) {
						map.get(dataName).add(evaluateCalc(dataName.substring(5), stageMap));
						continue;
					} else if (dataName.contains("@")) {
						try {
							map.get(dataName).add(Double.valueOf(stageMap.get(dataName.split("\\(")[0])
									.split(getParenthetical(dataName))[Integer.valueOf(dataName.split("\\@")[1])]));
						} catch (Exception e) {
							map.get(dataName).add(0.0);
							continue;
						}
						continue;
					}
					String value = stageMap.containsKey(dataName) ? stageMap.get(dataName) : "0.0";
					map.get(dataName).add(isNumeric(value) ? Double.valueOf(value) : 0.0);
				}
			});
		}
		return map;
	}

	public static String simplifyCalcString(String valueCalcString) {
		Matcher matcher = Pattern.compile("\\-\\-").matcher(valueCalcString);
		while (matcher.find()) {
			valueCalcString = matcher.replaceAll("+");
		}
		return valueCalcString;
	}

	public static Double evaluateCalc(String holderCalcString, HashMap<String, String> stageMap) {
		String valueCalcString = addValuesToCalcString(holderCalcString, stageMap);
		valueCalcString = simplifyCalcString(valueCalcString);
		String answer = UserDefinedFrame.calculateExpression(valueCalcString);
		Double dubAnswer = -1.0;
		try {
			dubAnswer = Double.valueOf(answer);
		} catch (NumberFormatException e) {
			return -1.0;
		}
		return dubAnswer;
	}

	public static String addValuesToCalcString(String holderCalcString, HashMap<String, String> map) {
		Matcher matcher = Pattern.compile("\\[([\\w\\s]+)\\]").matcher(holderCalcString);
		String valueCalcString = new String(holderCalcString);
		while (matcher.find()) {
			String holderValue = matcher.group();
			String value = map.containsKey(holderValue.substring(1, holderValue.length() - 1))
					? map.get(holderValue.substring(1, holderValue.length() - 1))
					: "0.0";
			valueCalcString = valueCalcString.replace(holderValue, value);
		}
		System.out.println(valueCalcString);
		return valueCalcString;
	}

	public static boolean isNumeric(String value) {
		return value.matches("[\\d\\.\\-]+") & !value.matches("\\.");
	}

	public static LinkedHashMap<String, ArrayList<Double>> getMapShell(String... dataValueNames) {
		LinkedHashMap<String, ArrayList<Double>> map = new LinkedHashMap<>();
		for (String s : dataValueNames) {
			map.put(s, new ArrayList<>());
		}
		return map;
	}

	@SuppressWarnings("unlikely-arg-type")
	public static LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, ArrayList<Double>>>> getOrganizedData(
			EvaluatedDataObject evaluatedDataObject, boolean chems, String... dataValueNames) throws IOException,
			InterruptedException, InvalidKeyException, ClassNotFoundException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ShortBufferException {
		JobLogWells jobLogWells = getJobLogWells();
		HashMap<String, String> formationMap = jobLogWells.getFormationMap();
		LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, ArrayList<Double>>>> map = new LinkedHashMap<>();
		HashMap<String, HashMap<Integer, LinkedHashMap<String, String>>> sigValsMap = evaluatedDataObject
				.getSigValsMaps();

		for (String s : sigValsMap.keySet()) {

			jobLogWells.setSelectedWellMap(jobLogWells.getToken(), jobLogWells.getIdFromWellTest(s));

			for (Integer i : sigValsMap.get(s).keySet()) {
				String formation = formationMap.get(jobLogWells.getSelectedWellMap().get("formationId"));
				String county = sigValsMap.get(s).get(i).get("County");
				LinkedHashMap<String, String> chemMap = evaluatedDataObject.getChemSandMap(s, i).get("chemicals");
				String[] stringArray;
				HashMap<String, String> chemNameMap = getChemNameMap(chemMap);
				checkAddObjToMap(map, county, formation, dataValueNames);
				if (chems) {
					stringArray = addChemNamesToArray(dataValueNames, chemMap);
					sigValsMap.get(s).get(i).putAll(chemMap);
					checkAddChemsToMap(map, county, formation, stringArray, chemMap);
					stringArray = getFormattedDataNames(stringArray);
				} else {
					stringArray = getFormattedDataNames(dataValueNames);
				}
				HashSet<String> names = new HashSet<>();
				for (String name : map.get(county).get(formation).keySet()) {
					names.add(name);
					try {
						if (name.contains("CALC:")) {
							map.get(county).get(formation).get(name).add(
									Double.valueOf(formatCalcString(name, dataValueNames, sigValsMap.get(s).get(i))));
							continue;
						} else if (name.contains("@")) {
							try {
								map.get(county).get(formation).get(name)
										.add(Double.valueOf(sigValsMap.get(s).get(i).get(name.split("\\(")[0])
												.split(getParenthetical(name))[Integer.valueOf(name.split("\\@")[1])]));
							} catch (Exception e) {
								map.get(county).get(formation).get(name).add(0.0);
								continue;
							}
							continue;
						}
						if (!sigValsMap.get(s).get(i).containsKey(name)) {
							map.get(county).get(formation).get(name).add(0.0);
							continue;
						}
						if (chemNameMap.containsKey(name)) {
							map.get(county).get(formation).get(name)
									.add(Double.valueOf(
											sigValsMap.get(s).get(i).get(chemNameMap.get(name)).matches("[\\d\\.]+")
													? sigValsMap.get(s).get(i).get(chemNameMap.get(name))
													: "0.0"));
							continue;
						}
						map.get(county).get(formation).get(name)
								.add(Double.valueOf(sigValsMap.get(s).get(i).get(name).matches("[\\d\\.]+")
										? sigValsMap.get(s).get(i).get(name)
										: "0.0"));
					} catch (NullPointerException e) {
						System.out.println("Exception -> ");
						System.out.println(name);
						map.get(county).get(formation).get(chemNameMap.containsKey(name) ? chemNameMap.get(name) : name)
								.add(0.0);
						continue;
					}
				}
				for (String dataName : map.get(county).get(formation).keySet()) {
					if (!names.contains(dataName)) {
						map.get(county).get(formation).get(dataName).add(0.0);
					}
				}
			}
		}

		return map;
	}

	public static String[] pushBackArray(String[] stringArr, String addValue) {
		String[] newStringArray = new String[stringArr.length + 1];
		int count = 0;
		for (String s : stringArr) {
			newStringArray[count] = s;
			count++;
		}
		newStringArray[count] = addValue;
		return newStringArray;
	}

	public static void checkAddObjToMap(
			LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, ArrayList<Double>>>> map, String county,
			String formation, String[] dataValueNames) {
		if (map.containsKey(county) && map.get(county).containsKey(formation)) {
			return;
		}
		if (!map.containsKey(county)) {
			map.put(county, new LinkedHashMap<>());
			map.get(county).put(formation, new LinkedHashMap<>());
		} else if (!map.get(county).containsKey(formation)) {
			map.get(county).put(formation, new LinkedHashMap<>());
		}

		for (String s : dataValueNames) {
			if (s.contains("SPLIT")) {
				HashMap<String, String> splitMap = getSplitMap(s);
				int length = Integer.parseInt(splitMap.get(SPLIT_LENGTH));
				for (int i = 0; i < length; i++) {
					map.get(county).get(formation).put(
							splitMap.get(SPLIT_VALUE) + "(" + splitMap.get(SPLIT_DELIMITER) + ")@" + i,
							new ArrayList<>());
				}
				continue;
			}
			map.get(county).get(formation).put(s, new ArrayList<>());
		}
	}

	public static Integer getSplitLength(String splitFuncString) {
		Matcher matcher = Pattern.compile("(\\d*?)$").matcher(splitFuncString);
		if (matcher.find()) {
			return Integer.valueOf(matcher.group());
		}
		return 0;
	}

	public static void checkAddChemsToMap(
			LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, ArrayList<Double>>>> map, String county,
			String formation, String[] dataNames, LinkedHashMap<String, String> chemMap) {
		int size = map.get(county).get(formation).get(dataNames[0]).size();
		for (String s : chemMap.keySet()) {
			String fixed = s.toUpperCase().replace(" ", "");
			if (!map.get(county).get(formation).containsKey(fixed)) {
				map.get(county).get(formation).put(fixed, getZerosArray(size));
			}
		}
	}

	public static ArrayList<Double> getZerosArray(int size) {
		ArrayList<Double> array = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			array.add(0.0);
		}
		return array;
	}

	@SuppressWarnings("unused")
	private static void holddd()
			throws IOException, InvalidKeyException, ClassNotFoundException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ShortBufferException {
		EncryptCredentials.updateHostStoredPassword();
		// System.out.println(EncryptCredentials.getRuntimeDir());
		EncryptCredentials.updateClientStoredPassword();
		byte[] challenge = EncryptCredentials.getFileByteBuffer(EncryptCredentials.PASSWORD_PATH).array();
		GenerateEncryption generateEncryption = GenerateEncryption.readObjectFromFile();
		if (EncryptCredentials.challengeCreds(challenge, generateEncryption)) {
			System.out.println("ACCESS GRANTED");
		} else {
			System.out.println("ACCESS DENIED");
		}
		// System.out.println(EncryptCredentials.getDecryptedPassword());
	}

	private static LocalDateTime getDatumDate() {
		LocalDateTime localDateTime = LocalDateTime.of(LocalDate.of(2022, 1, 1), LocalTime.of(0, 0));
		return localDateTime;
	}

	private static String decodeUnicode(String unicode, int base) {
		char[] reverseCharArray = getReverseStringArray(unicode);
		long numVal = getUnicodeNum(reverseCharArray, base);
		return String.valueOf(numVal);
	}

	private static long getUnicodeNum(char[] charArray, int base) {
		int count = 0;
		long totalValue = 0;
		for (char c : charArray) {
			int intValue = getIntEquiv(c, 10);
			long addVal = (long) Math.pow(base, count) * intValue;
			totalValue += addVal;
			count++;
		}
		return totalValue;
	}

	private static int getIntEquiv(char c, int start) {
		String cString = String.valueOf(c).toLowerCase();
		if (cString.matches("\\d")) {
			return c;
		}
		char[] cLower = cString.toCharArray();
		int sub = 97 - start;
		return cLower[0] - sub;
	}

	private static char[] getReverseStringArray(String string) {
		char[] charArray = new char[string.length()];
		for (int i = string.length(); i > 0; i--) {
			charArray[string.length() - i] = string.charAt(i - 1);
		}
		return charArray;
	}

	private static HashMap<String, String> parseCrewJSonArray(String id, String json) {
		HashMap<String, String> tempMap = new HashMap<>();
		Matcher matcher = Pattern.compile("\"(\\w+)\":(\"?)\\b(.+?)\\b(\"?)").matcher(json);
		while (matcher.find()) {
			String found = matcher.group();
			System.out.println(found);
			tempMap.put(found.split(":")[0].replace("\"", ""), found.split(":")[1].replace("\"", ""));
		}
		return tempMap;
	}

	public static String getResponseString(String wellResponse, String objectName) {
		Matcher matcher = Pattern.compile("\"" + objectName + "\":(\\[?)\\{(.+?)\\}(\\]?)").matcher(wellResponse);
		if (matcher.find()) {
			String found = matcher.group();
			Matcher matcher2 = Pattern.compile("\"" + objectName + "\":(\\[?)\\{").matcher(found);
			return found.substring(matcher2.find() ? matcher2.end() : 0);
		}
		return "nope";
	}

	public static int getFactorial(int num) {
		int answer = 1;
		for (int i = num; i > 0; i--) {
			answer *= i;
		}
		return answer;
	}

	public static String getStartFromRequestString(String requestString) {
		Matcher matcher = Pattern.compile("\\d{4}\\-\\d{2}\\-\\d{2}\\s\\d{2}\\:\\d{2}").matcher(requestString);
		if (matcher.find()) {
			return matcher.group() + ":00";
		}
		return "0";
	}

	public static void testSandSilos() throws ClassNotFoundException, IOException, InterruptedException {
		new SandSilosFrame(SandTicketsObject.readFromFile("Port Hudson F"));
	}

	public static void testKeyValuesFrame() {
		new KeyValuePairsFrame<>(getHashMap(), "Sam");
	}

	public static String capitalizeWord(String word) {
		Matcher matcher = Pattern.compile("\\-").matcher(word);
		if (matcher.find()) {
			return String.valueOf(word.charAt(0)).toUpperCase() + word.substring(1, matcher.start()).toLowerCase() + "-"
					+ String.valueOf(word.charAt(word.length() - 1)).toUpperCase();
		}
		return String.valueOf(word.charAt(0)).toUpperCase() + word.substring(1).toLowerCase();
	}

	public static File testFileFind() {
		File file = new File("C:\\Users\\swppa\\Documents\\Job Files\\Test");
		String fileName = mainFrame.removeSpecialCharacters("MABEE O 1 2 2415H - TR.xlsm");
		System.out.println(fileName);
		File findFile = RedTreatmentReport.findDir(file, fileName);
		return findFile;
	}

	public static HashMap<String, String> getHashMap() {
		LinkedHashMap<String, String> map = new LinkedHashMap<>();

		map.put("Sam", "Parker");
		map.put("what", "it");
		map.put("do", "doe");
		map.put("Pro", "Petro");
		map.put("Mississippi", "Missouri");

		map.put("DAAAAAAAAA", "BEEEAAAAARSSSSS");
		map.put("What We", "Do In Life");
		map.put("ECHOS", "IN ETERNITY");
		map.put("See", "You");
		map.put("In", "Elisium");
		map.put("this", "that");
		map.put("hello", "world");
		map.put("Java", "Scrape");

		return map;
	}

	public static void testPumpRequest()
			throws IOException, ClassNotFoundException, InterruptedException, DataFormatException {
		RememberMe rememberMe = RememberMe.readCookie();
		CrewRequest crewRequest = new CrewRequest();
		String session = crewRequest.getSessionId();
		String csrf = crewRequest.getToken();
		String crew = "Red";
		String start1 = getTime(Long.valueOf(60));
		String end1 = getTime(Long.valueOf(30));
		String start2 = getTime(Long.valueOf(400));
		String end2 = getTime(Long.valueOf(300));
		LocalDateTime now1 = LocalDateTime.now();
	}

	public static String getTime(Long minusMinutes) {
		return LocalDateTime.now().minusMinutes(minusMinutes).format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm"));
	}

	public static void testTableColor() {
		JFrame frame = new JFrame();
		frame.setBounds(200, 200, 400, 400);
		JTable table = new JTable(20, 4);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setMinWidth(100);
			table.getColumnModel().getColumn(i).setCellRenderer(getTheThing());
		}
		table.setVisible(true);
		frame.add(table);
		frame.setVisible(true);
	}

	public static DefaultTableCellRenderer getTheThing() {
		DefaultTableCellRenderer theThing = new DefaultTableCellRenderer();
		theThing.setBackground(Color.black);
		theThing.setForeground(Color.white);
		return theThing;
	}

	public static ArrayList<String> getWells() {
		ArrayList<String> array = new ArrayList<>();
		array.add("They");
		array.add("Call");
		array.add("Me");
		array.add("Big");
		array.add("Daddy");
		return array;
	}

	public static void reset() throws IOException, ClassNotFoundException {
		SandTicketsObject sandTicketsObject = SandTicketsObject.readFromFile("Port Hudson F");
		SandTicketsObject.resetPumpedSiloMap(sandTicketsObject);
		System.out.println("Sam");

		sandTicketsObject.writeToFile();
	}

	public static void pumpSand() throws IOException, ClassNotFoundException {
		SandTicketsObject sandTicketsObject = SandTicketsObject.readFromFile("Sams Pad");
		sandTicketsObject.setSiloOrder(getSiloOrders());
		sandTicketsObject.pumpSand(getPumpedSand(), "Sams Pad 1H", 1);
	}

	public static LinkedHashMap<String, Double> getPumpedSand() {
		LinkedHashMap<String, Double> map = new LinkedHashMap<>();
		map.put("100 Mesh Regional", 320000.0);
		map.put("40/70 Regional", 80000.0);
		return map;
	}

	public static HashMap<String, ArrayList<String>> getSiloOrders() {
		ArrayList<String> order1 = new ArrayList<>();
		order1.add("Silo 1");
		order1.add("Silo 2");
		order1.add("Silo 3");
		order1.add("Silo 5");
		ArrayList<String> order2 = new ArrayList<>();
		order2.add("Silo 4");
		order2.add("Silo 6");
		HashMap<String, ArrayList<String>> map = new HashMap<>();
		map.put("100 Mesh Regional", order1);
		map.put("40/70 Regional", order2);
		return map;
	}

	public static <T> String getCommaFormattedNumber(T t, Boolean rounded) {
		String value = String.valueOf(t);
		System.out.println(value);
		if (checkNonNumeric(value)) {
			return "0";
		}
		Matcher matcher = Pattern.compile("\\d+").matcher(value);
		if (rounded && matcher.find()) {
			return addCommas(matcher.group());
		}
		return matcher.find() && matcher.end() + 1 < value.length()
				? addCommas(matcher.group()) + "." + value.substring(matcher.end() + 1)
				: addCommas(matcher.group());

	}

	private static Boolean checkNonNumeric(String value) {
		Matcher matcher = Pattern.compile("[^\\d\\.]").matcher(value);
		if (matcher.find()) {
			return true;
		}
		return false;
	}

	public static String addCommas(String value) {
		StringBuilder stringBuilder = new StringBuilder();
		int count = 0;
		for (int i = value.length() - 1; i > -1; i--) {
			if (count % 3 == 0 & count > 0) {
				stringBuilder.insert(0, ",");
				stringBuilder.insert(0, value.charAt(i));
				count++;
				continue;
			}
			stringBuilder.insert(0, value.charAt(i));
			count++;
		}
		return stringBuilder.toString();
	}

	public static JFrame getBlankJFrame() {
		JFrame frame = new JFrame();
		frame.getContentPane().setBackground(Color.getHSBColor(-.85f, .1f, .85f));
		frame.setBounds(200, 200, 600, 450);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		return frame;
	}

	public static Rectangle getTicketsFrameBounds() {
		return new Rectangle(150, 150, 1200, 450);
	}

	public static void checkForDoneMessage(Socket socket) throws IOException {
		/*
		 * if(socket.isInputShutdown()) { System.out.println("isInputShutdown: true");
		 * return; }
		 */
		DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
		int done;
		while ((done = dataInputStream.read()) != -1) {
			System.out.println("Waiting");
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				System.out.println("Interrupted while waiting for stream to to finish");
			}
		}
	}

	public static void sendDataFileSocketRefined() throws UnknownHostException, IOException, ClassNotFoundException {
		String crewName = "Test";
		byte[] crewBytes = crewName.getBytes();
		Socket socket = new Socket("10.119.224.55", 80);
		socket.setSoTimeout(60000);
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
		EvaluatedDataObject evaluatedDataObject = EvaluatedDataObject.getFromFile();
		objectOutputStream.writeInt(crewBytes.length);
		objectOutputStream.write(crewBytes);
		objectOutputStream.writeObject(evaluatedDataObject);
		checkForDoneMessage(socket);
		System.out.println("socket disconnected");
		objectOutputStream.close();
	}

	public static Long getEpochSecondNow() {
		return Duration.between(LocalDateTime.of(1970, 1, 1, 0, 0, 0), LocalDateTime.now()).toMillis();
	}

	public static void sendDataFileSocket1() throws UnknownHostException, IOException, ClassNotFoundException {
		String crewName = "GREEN";
		byte[] crewBytes = crewName.getBytes();
		Socket socket = new Socket("10.119.224.55", 80);
		socket.setSoLinger(true, 60);
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
		File file = new File("C:\\Scrape\\data.his");
		EvaluatedDataObject evaluatedDataObject = EvaluatedDataObject.getFromFile();
		objectOutputStream.writeInt(crewBytes.length);
		objectOutputStream.write(crewBytes);
		objectOutputStream.writeObject(evaluatedDataObject);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			System.out.println("InterruptedException sendDataFileSocket");
		}
		objectOutputStream.close();
	}

	public static void printMatrix(int[][] intArray) {
		int n = intArray.length;
		for (int i = 0; i < n; i++) {
			for (int ii = 0; ii < n; ii++) {
				System.out.print(intArray[i][ii] + ",");
			}
			System.out.println("");
		}
	}

	public static int hold(int[][] img1, int[][] img2) {
		int max = 0;
		int temp = 0;
		temp = calculateRightUp(img1, img2);
		if (temp > max) {
			max = temp;
		}
		temp = calculateRightDown(img1, img2);
		if (temp > max) {
			max = temp;
		}
		temp = calculateLeftUp(img1, img2);
		if (temp > max) {
			max = temp;
		}
		temp = calculateLeftDown(img1, img2);
		if (temp > max) {
			max = temp;
		}
		return max;
	}

	public static int calculateRightDown(int[][] i1, int[][] i2) {

		int[][] img1 = i1;
		int[][] img2 = i2;
		int max = overlapMatrices(img1, img2);
		int tempCount = 0;
		int n = img1.length;
		if (max == n * n) {
			return max;
		}
		int[][] copy = img1;
		for (int i = 0; i < n; i++) {

			copy = img1;
			for (int ii = 0; ii < n; ii++) {
				copy = getTranslatedLeftRight(img1, -1);
				tempCount = overlapMatrices(copy, img2);
				if (tempCount > max) {
					max = tempCount;
				}
			}
			img1 = getTranslatedUpDown(img1, -1);
			tempCount = overlapMatrices(img1, img2);
			if (tempCount > max) {
				max = tempCount;
			}

		}
		return max;
	}

	public static int calculateRightUp(int[][] i1, int[][] i2) {

		int[][] img1 = i1;
		int[][] img2 = i2;
		int max = overlapMatrices(img1, img2);
		int tempCount = 0;
		int n = img1.length;
		if (max == n * n) {
			return max;
		}
		int[][] copy = img1;
		for (int i = 0; i < n; i++) {

			copy = img1;
			for (int ii = 0; ii < n; ii++) {
				copy = getTranslatedLeftRight(img1, -1);
				tempCount = overlapMatrices(copy, img2);
				if (tempCount > max) {
					max = tempCount;
				}
			}
			img1 = getTranslatedUpDown(img1, 1);
			tempCount = overlapMatrices(img1, img2);
			if (tempCount > max) {
				max = tempCount;
			}
		}
		return max;
	}

	public static int calculateLeftUp(int[][] i1, int[][] i2) {

		int[][] img1 = i1;
		int[][] img2 = i2;
		int max = overlapMatrices(img1, img2);
		int tempCount = 0;
		int n = img1.length;
		if (max == n * n) {
			return max;
		}
		int[][] copy = img1;
		for (int i = 0; i < n; i++) {
			copy = img1;
			for (int ii = 0; ii < i; ii++) {
				copy = getTranslatedLeftRight(img1, 1);
				tempCount = overlapMatrices(copy, img2);
				if (tempCount > max) {
					max = tempCount;
				}
			}
			img1 = getTranslatedUpDown(img1, 1);
			tempCount = overlapMatrices(img1, img2);
			if (tempCount > max) {
				max = tempCount;
			}

		}
		return max;
	}

	public static int calculateLeftDown(int[][] i1, int[][] i2) {

		int[][] img1 = i1;
		int[][] img2 = i2;
		int max = overlapMatrices(img1, img2);
		int tempCount = 0;
		int n = img1.length;
		if (max == n * n) {
			return max;
		}
		int[][] copy = img1;
		for (int i = 0; i < n; i++) {

			copy = img1;
			for (int ii = 0; ii < i; ii++) {
				copy = getTranslatedLeftRight(copy, 1);
				tempCount = overlapMatrices(copy, img2);
				if (tempCount > max) {
					max = tempCount;
				}
			}
			img1 = getTranslatedUpDown(img1, -1);
			tempCount = overlapMatrices(img1, img2);
			if (tempCount > max) {
				max = tempCount;
			}

		}
		return max;
	}

	public static int[][] getTranslatedLeftRight(int[][] i1, int leftRight) {
		int[][] img1 = i1;
		int n = img1.length;
		int[][] translated = new int[n][n];
		int start = 1;
		int end = 0;
		if (leftRight > 0) {
			start = 0;
			end = 1;
		}
		for (int i = 0; i < img1.length; i++) {
			int[] img = new int[n];
			if (leftRight < 0) {
				img[0] = 0;
			} else {
				img[n - 1] = 0;
			}
			for (int ii = start; ii < img.length - end; ii++) {
				img[ii] = img1[i][ii + leftRight];
			}
			translated[i] = img;
		}
		return translated;
	}

	public static int[][] getTranslatedUpDown(int[][] i1, int downUp) {
		int img1[][] = i1;
		int n = img1.length;
		int[][] translated = new int[n][n];
		int start = 1;
		int end = 0;
		if (downUp < 0) {
			translated[0] = getZeros(n);

		} else {
			translated[n - 1] = getZeros(n);
			start = 0;
			end = 1;
		}
		for (int i = start; i < img1.length - end; i++) {

			translated[i] = img1[i + downUp];
		}
		return translated;
	}

	public static int[] getZeros(int n) {
		int[] zeros = new int[n];
		for (int i = 0; i < n; i++) {
			zeros[i] = 0;
		}
		return zeros;
	}

	public static int overlapMatrices(int[][] img1, int[][] img2) {
		int n = img1.length - 1;
		int count = 0;
		for (int i = 0; i <= n; i++) {
			for (int ii = 0; ii <= n; ii++) {
				if ((img1[i][ii] == 0) || (img2[i][ii] == 0)) {
					continue;
				}
				count++;
			}
		}
		return count;
	}

	public static File getDataFile() throws FileNotFoundException {
		File file = new File("C:\\Scrape\\data.his");

		return file;
	}

	////////////////////////////////////
	public static void sendDataFileSocket() throws UnknownHostException, IOException {
		Socket socket = new Socket("10.119.224.55", 80);
		String crewName = "PPS6";
		byte[] crewBytes = crewName.getBytes();
		DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
		byte[] fileBytes = new byte[(int) (new File("C:\\Scrape\\data.his")).length()];
		FileInputStream fileInputStream = new FileInputStream(new File("C:\\Scrape\\data.his"));
		fileInputStream.read(fileBytes);
		dataOutputStream.writeInt(crewBytes.length);
		dataOutputStream.write(crewBytes);

		dataOutputStream.writeInt(fileBytes.length);
		dataOutputStream.write(fileBytes);
		dataOutputStream.close();
		fileInputStream.close();
	}

	/////////////////
	public static void getDataFileSocket() throws UnknownHostException, IOException {
		Socket socket = new Socket("10.119.224.55", 443);
		DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
		writeFilesFromInputStream(dataInputStream);
	}

	public static void writeFilesFromInputStream(DataInputStream dataInputStream) throws IOException {
		File file;
		while ((file = readDirName(dataInputStream)) != null) {
			int fileSize = dataInputStream.readInt();
			byte[] fileBytes = new byte[fileSize];
			dataInputStream.read(fileBytes);
			writeFile(fileBytes, file);
		}
	}

	public static File readDirName(DataInputStream dataInputStream) throws IOException {
		int dirLen = checkEndOfFile(dataInputStream);
		System.out.println(dirLen);
		if (dirLen == -1) {
			return null;
		}
		byte[] dirBytes = new byte[dirLen];
		dataInputStream.read(dirBytes);
		String dirName = getStringFromBytes(dirBytes);
		File file = new File("C:\\Scrape\\histDataObjects\\" + dirName + "\\data.his");
		makeDirectories(file);
		return file;
	}

	public static int checkEndOfFile(DataInputStream dataInputStream) throws IOException {
		int dirLen = 0;
		try {
			while (dataInputStream.available() == 0) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					System.out.println("InterruptedException checkEndOfFile()");
				}
			}
			dirLen = dataInputStream.readInt();
		} catch (EOFException e) {
			return -1;
		}
		return dirLen;
	}

	public static void makeDirectories(File file) {
		File newFile = file.getParentFile();
		if (!newFile.exists()) {
			newFile.mkdirs();
		}
	}

	public static void writeFile(byte[] fileBytes, File scrapeDir) throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(scrapeDir + "\\");
		fileOutputStream.write(fileBytes);
	}

	public static File getScrapeDir() {
		File file = new File("C:\\TestDataDir");
		if (!file.exists()) {
			file.mkdir();
		}
		return file;
	}
	//////////////

	public static StringBuilder nextStringBuilder(StringBuilder lastStringBuilder) {
		StringBuilder nextStringBuilder = new StringBuilder();
		int length = lastStringBuilder.length();
		int i = 0;
		int count = 0;
		while (i < length) {
			char c = lastStringBuilder.charAt(i++);
			count = 1;
			while (i < length && c == lastStringBuilder.charAt(i)) {
				count++;
				i++;
			}
			nextStringBuilder.append(count).append(c);
		}
		return nextStringBuilder;
	}

	public static ArrayList<String> getStartEnd(String source) {
		Matcher matcher = Pattern.compile("<script>((.^(/script))+)</script>").matcher(source);
		ArrayList<String> stringArray = new ArrayList<>();
		while (matcher.find()) {
			stringArray.add(source.substring(matcher.start(), matcher.end()));
		}

		return stringArray;
	}

	public static void makeLoginRequest(String url) {

	}

	public static JComboBox<String> getRandomComboBox() {
		String[] something = getRandomArray();
		JComboBox<String> comboBox = new JComboBox<>();
		for (String s : something) {
			comboBox.addItem(s);
		}
		return comboBox;
	}

	public static String[] getRandomArray() {
		String[] something = new String[26];
		for (int i = 0; i < 26; i++) {
			String string = "";
			for (int ii = 0; ii < 5; ii++) {
				string += (char) (i + 64);
			}
			something[i] = string;
		}
		return something;
	}

	public static void checkFileProperties(File file) throws IllegalArgumentException, IllegalAccessException {
		Class cls = file.getClass();
		for (Method method : cls.getMethods()) {
			if (!method.getName().equals("delete")
					&& method.getParameterCount() == 0 & method.getReturnType() != null) {
				try {
					System.out.println(method.getName() + " - " + method.invoke(file, null));
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				}
			}
		}

	}

	public static HashMap<String, HashMap<String, String>> getActiveWells(
			HashMap<String, HashMap<String, String>> wellMap) {
		HashMap<String, HashMap<String, String>> activeMap = new HashMap<>();

		for (String s : wellMap.keySet()) {
			try {
				if (wellMap.get(s).get("active").equals("true")) {
					activeMap.put(s, wellMap.get(s));
				}
			} catch (NullPointerException e) {
				System.out.println("-----------NullPointerException-----------------");
				System.out.println(s);
				System.out.println(wellMap.get(s));
				System.out.println("------------------------------------------------");
			}
		}
		return activeMap;
	}

	public static Rectangle getBoundsAgain() {
		int x = Toolkit.getDefaultToolkit().getScreenSize().width / 3;
		int y = 0;
		int width = Toolkit.getDefaultToolkit().getScreenSize().width / 3;
		int height = Toolkit.getDefaultToolkit().getScreenSize().height * 3 / 5;
		return new Rectangle(x, y, width, height);
	}

	public static Rectangle getBounds() {
		int x = Toolkit.getDefaultToolkit().getScreenSize().width / 4;
		int y = 0;
		int width = Toolkit.getDefaultToolkit().getScreenSize().width / 2;
		int height = Toolkit.getDefaultToolkit().getScreenSize().height - 50;
		return new Rectangle(x, y, width, height);
	}

	private static String reverseString(String statement) {
		StringBuilder reverseBuilder = new StringBuilder();
		for (int i = 0; i < statement.length(); i++) {
			reverseBuilder.insert(0, statement.charAt(i));
		}
		return reverseBuilder.substring(0);
	}

	private static Rectangle getRectBounds() {
		int x = Toolkit.getDefaultToolkit().getScreenSize().width / 4;
		int y = 0;
		int width = Toolkit.getDefaultToolkit().getScreenSize().width / 2;
		int height = Toolkit.getDefaultToolkit().getScreenSize().height - 50;
		return new Rectangle(x, y, width, height);
	}
}
//addAddressToRange
//removeValueFromMap