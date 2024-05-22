package mrl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DataRequest {

	private ExecutorService executor;
	private String cookies;
	private String jobID;
	private Queue<String> dataQueue;
	private Map<String, List<String>> dataMap;
	private LocalDate dataDate;
	private Semaphore aggDataSem;
	private Semaphore getDataSem;
	private boolean waiting;
	private boolean done;

	public DataRequest(String cookies, String jobID) throws IOException, InterruptedException {
		instObj(cookies, jobID);
		aggData();
	}

	private final static String CONTENT_TYPE_HEADER = "application/octet-stream";
	private final static String ACCEPT_HEADER = "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7";

	private void instObj(String cookies, String jobID) {
		executor = Executors.newCachedThreadPool();
		this.cookies = cookies;
		this.jobID = jobID;
		dataQueue = new LinkedBlockingQueue<>();
		dataMap = new HashMap<>();
		aggDataSem = new Semaphore(0);
		getDataSem = new Semaphore(0);
		setStartBooleans();
	}

	private void setStartBooleans() {
		waiting = true;
		done = false;
	}

	private void aggData() throws IOException, InterruptedException {
		InputStream dataStream = makeDataRequest(cookies, jobID);
		executor.execute(() -> {
			try {
				readData(dataStream);
			} catch (IOException e) {
				aggDataSem.release();
				e.printStackTrace();
			}
		});
		executor.execute(() -> {
			try {
				addQueueData();
			} catch (InterruptedException | IOException e) {
				getDataSem.release();
				e.printStackTrace();
			}
		});
		getDataSem.acquire();
		executor.shutdown();
		getDataSem.release();
	}

	public synchronized Map<String, List<String>> getDataMap() throws InterruptedException {
		getDataSem.acquire();
		getDataSem.release();
		return dataMap;
	}

	private Map<String, List<String>> readJobDataFromDir() throws ClassNotFoundException, IOException {
		File jobDir = new File(DataHandling.getJobDataDir(jobID));
		String[] dateData = jobDir.list();
		if (dateData.length == 0) {
			return dataMap;
		}
		Map<String, List<String>> jobDataMaps = readJobDataMaps(getChronoDataDates(dateData));
		addMapToMapArr(jobDataMaps, dataMap);
		return jobDataMaps;
	}

	public static Map<LocalDate,Map<String, List<String>>> readJobDataFromDir(String jobID)
			throws ClassNotFoundException, IOException, InterruptedException {
		File jobDir = new File(DataHandling.getJobDataDir(jobID));
		String[] dateData = jobDir.list();
		if (dateData.length == 0) {
			return null;
		}
		Map<LocalDate,Map<String, List<String>>> jobDataMaps = readJobDataMaps(getChronoDataDates(dateData), jobID);
		return jobDataMaps;
	}

	@SuppressWarnings("unchecked")
	private Map<String, List<String>> readJobDataMaps(List<LocalDate> dateFileList)
			throws ClassNotFoundException, IOException {
		Map<String, List<String>> combinedData = new HashMap<>();

		for (LocalDate date : dateFileList) {
			File file = new File(DataHandling.getJobDataDirForDate(date, jobID));

			Map<String, List<String>> dateData = DataHandling.readObjFromFile(combinedData.getClass(), file);
			addMapToMapArr(combinedData, dateData);
		}
		return combinedData;
	}

	@SuppressWarnings("unchecked")
	private static Map<LocalDate, Map<String, List<String>>> readJobDataMaps(List<LocalDate> dateFileList, String jobID)
			throws ClassNotFoundException, IOException, InterruptedException {
		Map<LocalDate, Map<String, List<String>>> combinedData = new HashMap<>();
		ExecutorService executor = Executors.newCachedThreadPool();
		CountDownLatch latch = new CountDownLatch(dateFileList.size());
		for (LocalDate date : dateFileList) {
			executor.execute(()->{
				File file = new File(DataHandling.getJobDataDirForDate(date, jobID));
				System.out.println(file.getAbsolutePath());
				Map<String, List<String>> dateData = null;
				try {
					dateData = DataHandling.readObjFromFile(combinedData.getClass(), file);
				} catch (ClassNotFoundException | IOException e) {
					latch.countDown();
					e.printStackTrace();
				}
				addMapToDateMap(combinedData, dateData,date);
				latch.countDown();
			});
		}
		latch.await();
		executor.shutdown();
		return combinedData;
	}

	private synchronized static void addMapToDateMap(Map<LocalDate, Map<String, List<String>>> combinedDateMap,
			Map<String, List<String>> addMap, LocalDate date) {
		combinedDateMap.put(date, addMap);
	}

	private static void addMapToMapArr(Map<String, List<String>> arr1, Map<String, List<String>> arr2) {
		for (String s : arr2.keySet()) {
			if (arr1.containsKey(s)) {
				arr1.get(s).addAll(arr2.get(s));
				continue;
			}
			arr1.put(s, arr2.get(s));
		}
	}

	private static List<LocalDate> getChronoDataDates(String[] dateFileNames) {
		List<LocalDate> dateList = new ArrayList<>();
		for (String fileName : dateFileNames) {
			dateList.add(LocalDate.parse(getFileWOExt(fileName)));
		}
		dateList.sort(new Comparator<LocalDate>() {

			@Override
			public int compare(LocalDate o1, LocalDate o2) {
				int bea = !o1.isBefore(o2) ? (!o1.isAfter(o2) ? 0 : 1) : -1;
				return bea;
			}
		});
		return dateList;
	}

	private static String getFileWOExt(String fileName) {
		Matcher matcher = Pattern.compile("(.+?)\\..+?$").matcher(fileName);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return fileName;
	}

	public static InputStream makeDataRequest(String cookies, String jobID) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().header("Content-Type", CONTENT_TYPE_HEADER)
				.header("Accept", ACCEPT_HEADER).header("Cookie", cookies).uri(getDataReqURI(jobID)).GET().build();
		HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		System.out.println(response.statusCode());
		System.out.println(response.headers());
		return response.body();
	}

	private final static int CHUNK_SIZE = 2048;

	@SuppressWarnings("unlikely-arg-type")
	private void addDataToMap(Map<Integer, String> headersIndex, String dataRow) throws IOException {
		int i = 0;
		Map<String, String> rowMap = new LinkedHashMap<>();

		for (String s : dataRow.split(",")) {
			if (!headersIndex.containsKey(i)) {
				break;
			}
			rowMap.put(headersIndex.get(i), s);
			i++;
		}

		LocalDate newDate = getRowDateTime(rowMap);
		if(newDate==null) {
			return;
		}
		if(newDate.equals(getEpochDatum())) {
			System.out.println("1");
			return;
		}
		
		if (dataDate != null && !newDate.equals(dataDate)) {
			cacheDataByDate(headersIndex);
		}
		
		dataDate = newDate;
		addRowMapToDataMap(rowMap);

	}

	private void addRowMapToDataMap(Map<String, String> rowMap) {
		for (Map.Entry<String, String> entry : rowMap.entrySet()) {
			dataMap.get(entry.getKey()).add(entry.getValue());
		}
	}

	private void cacheDataByDate(Map<Integer, String> headersIndex) throws IOException {
		DataHandling.writeObjToFile(dataMap, DataHandling.getJobDataDirForDate(dataDate, jobID));
		resetDataMap(headersIndex);
	}

	private static LocalDate getRowDateTime(Map<String, String> rowMap) {
		String dateTimeStr = rowMap.get(DataRequest.DataChannels.TIME.getValue());
		if (rowMap.containsKey(DataChannels.DATE.getValue())) {
			dateTimeStr = formatDMYDate(rowMap.get(DataChannels.DATE.getValue())) + "T" + dateTimeStr;
			rowMap.put(DataChannels.TIME.getValue(), dateTimeStr);
		}
		LocalDateTime dateTime = formatDateTimeString(dateTimeStr);
	
		return dateTime==null?null:dateTime.toLocalDate();
	}

	public static String getTimeFromDateTime(String dateTimeStr) {
		Matcher matcher = Pattern.compile("\\d{2}:\\d{2}(:\\d{2})?").matcher(dateTimeStr);
		if(matcher.find()) {
			return matcher.group();
		}
		return "";
	}

	public static LocalDateTime formatDateTimeString(String dateTimeString) {
		Matcher matcher = Pattern.compile("\\d{4}-\\d{2}-\\d{2}.\\d{2}:\\d{2}:\\d{2}").matcher(dateTimeString);
		if (matcher.find()) {
			return LocalDateTime.parse(dateTimeString.replace(" ", "T"));
		}
		return formatMDYDateTime(dateTimeString);
	}

	
	
	public static LocalDateTime getEpochDatum() {
		return LocalDateTime.of(1970, 1,1,0,0,0);
	}
	
	private static String formatDMYDate(String date) {
		Matcher matcher = Pattern.compile("(\\d+)/(\\d+)/(\\d+).(\\d{2}:\\d{2}:\\d{2})").matcher(date);
		String day = "01";
		String month = "01";
		String year = "1970";
		if (matcher.find()) {
			day = getLeadingZeros("0" + matcher.group(1));
			month = getLeadingZeros("0" + matcher.group(2));
			year = matcher.group(3);
		}
		return (year + "-" + month + "-" + day);
		
	}

	private static LocalDateTime formatMDYDateTime(String dateTimeString) {
		Matcher matcher = Pattern.compile("(\\d+)/(\\d+)/(\\d+).(.+)").matcher(dateTimeString);
		String month = "01";
		String day = "01";
		String year = "1970";
		String time = "00:00:00";
		if (matcher.find()) {
			month = getLeadingZeros(matcher.group(1));
			day = getLeadingZeros(matcher.group(2));
			year = matcher.group(3);
			time = matcher.group(4);
		} else if(dateTimeString.trim().matches("\\w+")) {
			return null;
		}else {
			System.out.println("Fucked up dateTimeString: "+dateTimeString);
		}
		return LocalDateTime.parse((year + "-" + month + "-" + day + "T" + time));
	}

	private static String getLeadingZeros(String intString) {
		String wZero = "0" + intString;
		wZero = wZero.substring(wZero.length() - 2);
		return wZero;
	}

	private void addQueueData() throws InterruptedException, IOException {
		aggDataSem.acquire();
		Map<Integer, String> headersIndex = addListsToDataMap(dataQueue.poll());
		if (headersIndex == null) {
			getDataSem.release();
			return;
		}
		while (!done | !dataQueue.isEmpty()) {
			while (!dataQueue.isEmpty()) {
				String dataRow = dataQueue.poll();
				addDataToMap(headersIndex, dataRow);
			}
			waiting = true;
			if(!done) {
				aggDataSem.acquire();
			}
		}
		getDataSem.release();
	}

	private void resetDataMap(Map<Integer, String> headerIndex) {
		dataMap = new HashMap<>();
		for (String s : headerIndex.values()) {
			dataMap.put(s, new ArrayList<>());
		}
	}

	private Map<Integer, String> addListsToDataMap(String headerString) {
		if (headerString == null) {
			return null;
		}
		String[] headers = headerString.split(",");
		Map<Integer, String> headersIndex = new HashMap<>();
		for (int i = 0; i < headers.length; i++) {
			headersIndex.put(i, headers[i]);
			dataMap.put(headers[i], new ArrayList<>());
		}
		return headersIndex;
	}

	private int addDataToQueue(String data) {
		Matcher matcher = Pattern.compile(".+?[\\n\\r]").matcher(data);
		int end = 0;
		while (matcher.find()) {
			dataQueue.add(matcher.group().trim());
			if (waiting) {
				aggDataSem.release();
			}
			end = matcher.end();
		}
		return end;

	}

	private final static String ENTRY_EXT = "csv";

	private boolean checkZipEntryExt(String zipEntryName) {
		return zipEntryName.matches(".+?\\." + ENTRY_EXT);
	}

	public void readData(InputStream inputStream) throws IOException {
		ZipInputStream stream = new ZipInputStream(inputStream);
		ZipEntry zipEntry;
		while ((zipEntry = stream.getNextEntry()) != null) {
			if (!checkZipEntryExt(zipEntry.getName())) {
				System.out.println(zipEntry.getName());
				continue;
			}

			int i;
			byte[] bytes = new byte[CHUNK_SIZE];
			StringBuilder stringBuilder = new StringBuilder();
			while ((i = stream.read(bytes)) > -1) {
				System.out.println(getStringFromBytes(bytes));
				stringBuilder.append(getStringFromBytes(bytes));
				stringBuilder.delete(0, addDataToQueue(stringBuilder.toString()));
				bytes = new byte[CHUNK_SIZE];
			}
		}
		if (waiting) {
			aggDataSem.release();
		}
		done = true;
	}

	private static String getStringFromBytes(byte[] bytes) {
		String string = "";
		for (byte b : bytes) {
			if (b == 0) {
				return string;
			}
			string += (char) b;
		}
		return string;
	}

	private static URI getDataReqURI(String jobID) {
		String dataReqURI = "https://shear.mrlsolutions.com/index.php?menuaction=JobManager.ui.admin&type=job&job_id="
				+ jobID + "&Department=4&Division=7&query=&Status=&GroupingSelect=&sort=DESC&order=id&action=download";
		URI uri = URI.create(dataReqURI);
		return uri;
	}

	public static enum DataChannels {
		TIME, DATE, CLEAN_RATE, SLURRY_RATE, PROPPANT_CONC, CLEAN_TOTAL, SLURRY_TOTAL;

		public String getValue() {
			switch (this) {
			case TIME:
				return "Time";
			case DATE:
				return "Date";
			case CLEAN_RATE:
				return "Clean Rate";
			case SLURRY_RATE:
				return "Slurry Rate";
			case PROPPANT_CONC:
				return "Proppant Concentration";
			case CLEAN_TOTAL:
				return "Clean Total";
			case SLURRY_TOTAL:
				return "Slurry Total";
			}
			return "Time";
		}
	}
}
