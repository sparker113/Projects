package intelie;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DataRequest {
	String csrfToken;
	String cookie;
	String remCookie;
	String requestString;
	File dataFile;
	Integer numChannels;
	private int count = 0;
	private Boolean reading = true;

	public DataRequest(String csrfToken, String cookie, String requestString, Integer numChannels, String remCookie) {
		this.csrfToken = csrfToken;
		this.cookie = cookie;
		this.requestString = requestString;
		this.numChannels = numChannels;
		this.remCookie = remCookie;
	}

	public void setRequestString(String requestString) {
		this.requestString = requestString;
	}

	public LinkedHashMap<String, ArrayList<String>> makeRequest()
			throws IOException, InterruptedException, ExecutionException, DataFormatException {
		// int filesInDownloads = getDownloadsDir().list().length;
		System.out.println(requestString);
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().header("accept", "*/*")
				.header("content-type", "application/json").header("accept-encoding", "gzip, deflate, br")
				.header("cookie", "remember-me=" + remCookie + "; " + cookie).header("X-CSRF-TOKEN", csrfToken)
				.uri(URI.create(
						"https://propetro.intelie.com/services/plugin-propetro-custom-download/propetro/custom-download/mnemonic-values-by-span/"))
				.POST(HttpRequest.BodyPublishers.ofString(requestString)).build();

		HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		if (response.statusCode() != 200 && getRequestCount() < 2) {
			System.out.println(response.statusCode());
			System.out.println(response.headers());
			RememberMe rememberMe = new RememberMe();
			remCookie = rememberMe.getCookie();
			return null;
		}
		ZipInputStream inputStream = new ZipInputStream(response.body());
		DataMap dataMapObj = unZipStream(inputStream);
		return removeFirstIndices(fixTimes(dataMapObj.getDataMap()));
	}

	public LinkedHashMap<String, ArrayList<String>> fixTimes(LinkedHashMap<String, ArrayList<String>> dataMap) {
		int offset = getOffset();
		if (offset == 0) {
			return dataMap;
		}
		ArrayList<String> newTimes = new ArrayList<>();
		for (String s : dataMap.get("timestamp")) {
			newTimes.add(String.valueOf(LocalDateTime.parse(getParsableTime(s)).plusSeconds(offset)));
		}
		dataMap.put("timestamp", newTimes);
		return dataMap;
	}

	public static LinkedHashMap<String, ArrayList<String>> removeFirstIndices(
			LinkedHashMap<String, ArrayList<String>> dataMap) {
		for (String s : dataMap.keySet()) {
			if (dataMap.get(s) != null && dataMap.get(s).size() > 0) {
				dataMap.get(s).remove(0);
			}
		}
		return dataMap;
	}

	public static String getParsableTime(String dateTime) {
		if (dateTime.contains(".")) {
			return dateTime.split("\\.")[0].replace(" ", "T");
		}
		return dateTime.replace(" ", "T");
	}

	public int getOffset() {
		int offset = TimeZone.getDefault().getOffset(System.currentTimeMillis())
				- TimeZone.getTimeZone("America/Chicago").getOffset(System.currentTimeMillis());
		return offset / 1000;
	}

	public synchronized CompletableFuture<LinkedHashMap<String, ArrayList<String>>> makeRequest(
			ExecutorService executor)
			throws IOException, InterruptedException, ExecutionException, DataFormatException {
		// int filesInDownloads = getDownloadsDir().list().length;
		final String copy = new String(requestString);
		CompletableFuture<LinkedHashMap<String, ArrayList<String>>> futureDataMapObj = new CompletableFuture<>();
		executor.execute(() -> {
			System.out.println(copy);
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().header("accept", "*/*")
					.header("content-type", "application/json; charset=UTF-8")
					.header("cookie", "remember-me=" + remCookie + "; " + cookie).header("X-CSRF-TOKEN", csrfToken)
					.uri(URI.create(
							"https://propetro.intelie.com/services/plugin-propetro-custom-download/propetro/custom-download/mnemonic-values-by-span/"))
					.POST(HttpRequest.BodyPublishers.ofString(copy)).build();

			HttpResponse<InputStream> response = null;
			try {
				response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
				futureDataMapObj.complete(null);
			}
			if (response.statusCode() != 200 && getRequestCount() < 2) {
				System.out.println(response.statusCode());
				System.out.println(response.headers());
				RememberMe rememberMe = null;
				try {
					rememberMe = new RememberMe();
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
			ZipInputStream inputStream = new ZipInputStream(response.body());
			DataMap dataMapObj = null;
			try {
				dataMapObj = unZipStream(inputStream);
			} catch (InterruptedException | IOException | DataFormatException e) {
				e.printStackTrace();
				futureDataMapObj.complete(null);
			}
			try {
				futureDataMapObj.complete(fixTimes(dataMapObj.getDataMap()));
			} catch (InterruptedException e) {
				e.printStackTrace();
				futureDataMapObj.complete(null);
			}
		});
		return futureDataMapObj;
	}

	private int getRequestCount() {
		return count++;
	}

	public DataMap unZipStream(ZipInputStream responseStream)
			throws InterruptedException, IOException, DataFormatException {
		LocalDateTime start = LocalDateTime.now();
		DataMap dataMap = new DataMap(new LinkedBlockingQueue<String>(), new LinkedBlockingQueue<String>());

		Executors.newSingleThreadExecutor().execute(dataMap);
		ZipEntry entry = responseStream.getNextEntry();
		int i;
		byte[] buffer = new byte[64];
		while (entry != null) {
			while ((i = responseStream.read(buffer)) >= 0) {
				String data = removeEmptyBytes(new String(buffer, StandardCharsets.UTF_8));
				addDataToQueue(dataMap, data);
				buffer = new byte[64];
			}
			entry = responseStream.getNextEntry();
		}
		System.out.println(Duration.between(start, LocalDateTime.now()).toMillis());
		reading = false;
		return dataMap;
	}

	private Boolean isEmptyData(String data) {
		Matcher matcher = Pattern.compile(",,|,$").matcher(data);
		Integer count = 0;
		while (matcher.find()) {
			count++;
		}

		if (count.equals(numChannels / 2 + numChannels % 2)) {
			return true;
		}
		return false;
	}

	public static String removeEmptyBytes(String data) {
		String temp = "";
		for (byte b : data.getBytes()) {
			if (b == 0) {
				return temp;
			}
			temp += String.valueOf((char) b);
		}
		return temp;
	}

	private void addDataToQueue(DataMap dataMap, String data) {
		Matcher matcher = Pattern.compile("[\n\r]").matcher(data);
		int start = 0;
		while (matcher.find()) {
			int end = matcher.end();
			if (dataMap.incompleteQueue.size() > 0) {
				String lastString = dataMap.incompleteQueue.poll();
				lastString += data.substring(start, end);
				dataMap.dataQueue.offer(lastString);
				data = data.substring(end);
			} else {
				dataMap.dataQueue.offer(data.substring(start, end));
				data = data.substring(end);
			}
			matcher.reset(data);
		}
		addToIncompleteQueue(dataMap, data);
	}

	private void addToIncompleteQueue(DataMap dataMap, String data) {
		if (!data.equals("") & dataMap.incompleteQueue.size() > 0) {
			String lastIncomplete = dataMap.incompleteQueue.poll();
			dataMap.incompleteQueue.offer(lastIncomplete + data);
		} else if (!data.equals("")) {
			dataMap.incompleteQueue.offer(data);
		}
	}

	private void addDataToArray(ArrayList<String> dataArray, String data) {
		Matcher matcher = Pattern.compile("(\n)").matcher(data);

		if (matcher.find() && !lastIsNewLine(dataArray.get(dataArray.size() - 1))) {
			String lastString = dataArray.get(dataArray.size() - 1);
			dataArray.remove(dataArray.size() - 1);
			lastString += data.substring(0, matcher.end());
			Boolean added = isEmptyData(lastString) ? false : dataArray.add(lastString);
			dataArray.add(data.substring(matcher.end()));
		} else if (matcher.find() && lastIsNewLine(dataArray.get(dataArray.size() - 1))) {
			String next = data.substring(0, matcher.end());
			Boolean added = isEmptyData(next) ? false : dataArray.add(next);
			dataArray.add(data.substring(matcher.end()));
		} else if (!matcher.find() && lastIsNewLine(dataArray.get(dataArray.size() - 1))) {
			dataArray.add(data);
		} else if (!matcher.find() && !lastIsNewLine(dataArray.get(dataArray.size() - 1))) {
			String lastString = dataArray.get(dataArray.size() - 1);
			dataArray.remove(dataArray.size() - 1);
			lastString += data;
			dataArray.add(lastString);
		}
	}

	private static Boolean lastIsNewLine(String string) {
		Matcher matcher = Pattern.compile("\n").matcher(string);
		if (matcher.find() && matcher.end() == string.length()) {
			return true;
		}
		return false;
	}

	public static void unZipFile(String zipPath, String destPath) throws IOException {
		removeFile(destPath);
		Process process = Runtime.getRuntime().exec(new String[] { "powershell", "/c",
				"Expand-Archive -path \"" + zipPath + "\" -DestinationPath \"" + destPath + "\" -Force" });
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		String temp;
		while ((temp = reader.readLine()) != null) {
			System.out.println(temp);
		}
		process.destroy();
	}

	public static void removeFile(String path) {
		File file = new File(path);
		if (file.exists()) {
			file.delete();
		}
	}

	public static void readWriteCompressedData(File compressedFile, File destFile) throws IOException {
		InputStream inputFile = new FileInputStream(compressedFile);
		// InputStream inflaterInput = new InflaterInputStream(inputFile, new
		// Inflater(true));
		// unZipStream(inputFile,destFile);
		InputStream inputFile2 = new FileInputStream(new File(destFile.getAbsolutePath()));
		InflaterInputStream inflaterStream = new InflaterInputStream(inputFile2, new Inflater(true));
		transferToOutputStream(inflaterStream, new FileOutputStream("C:\\Scrape\\stagedata.csv"));
	}

	public static void checkFileProperties(File file) throws IllegalArgumentException, IllegalAccessException {
		Class cls = file.getClass();
		for (Method method : cls.getMethods()) {
			if (!method.getName().toUpperCase().contains("DELETE")
					&& method.getParameterCount() == 0 & method.getReturnType() != null) {
				try {
					System.out.println(method.getName() + " - " + method.invoke(file, null));
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				}
				if (file.exists() && method.getName().equals("list")) {
					try {
						for (String s : (String[]) (method.invoke(file, null))) {
							System.out.println(s);
						}
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {

					}
				}
			}
		}

	}

	public static void unBlockFile(String path) throws IOException {
		Process process = Runtime.getRuntime()
				.exec(new String[] { "powershell", "/c", "unblock-file -path \"" + path + "\"" });
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String temp;
		while ((temp = reader.readLine()) != null) {
			System.out.println(temp);
		}
		process.destroy();
	}

	public static void checkDataDir() {
		File file = new File("C:\\Scrape\\stagedata");
		if (!file.exists()) {
			file.mkdir();
		}
	}

	private Inflater getInflater(InputStream response) throws IOException {
		Inflater inflater = new Inflater(true);
		inflater.setInput(response.readAllBytes());
		return inflater;
	}

	public static void transferToOutputStream(InputStream inputStream, FileOutputStream outputStream)
			throws IOException, DataFormatException {
		byte[] bytes = new byte[1024];
		int i = 0;
		while ((i = inputStream.read(bytes, 0, 1024)) != -1) {
			byte[] inflateBytes = new byte[1024];
			Inflater inflater = new Inflater(true);
			inflater.setInput(bytes);
			inflater.inflate(inflateBytes);
			outputStream.write(inflateBytes);
		}
		outputStream.flush();
		outputStream.close();
	}

	public static void transferToOutputStream(InflaterInputStream inputStream, FileOutputStream outputStream)
			throws IOException {
		byte[] bytes = new byte[1024];
		int i = 0;
		while ((i = inputStream.read(bytes, 0, 1024)) != -1) {
			outputStream.write(bytes);
		}
		outputStream.flush();
		outputStream.close();
	}

	public static File getMostRecentFileInDir(File dir) throws IOException {
		Long lastModified = Long.valueOf(0);
		File recentFile = null;
		for (File f : dir.listFiles()) {
			if (lastModified < f.lastModified()) {
				lastModified = f.lastModified();
				recentFile = f;
			}
		}
		return recentFile;
	}

	public static File getMostRecentZipInDir(File dir) throws IOException {
		Long lastModified = Long.valueOf(0);
		File recentFile = null;
		for (File f : dir.listFiles()) {
			if (lastModified < f.lastModified() & f.getName().toLowerCase().contains(".zip")) {
				lastModified = f.lastModified();
				recentFile = f;
			}
		}
		System.out.println(recentFile.getName());
		return recentFile.listFiles()[0];
	}

	public static File getDownloadsDir() throws IOException {
		Process process = Runtime.getRuntime().exec(new String[] { "cmd.exe", "/c", "whoami" });
		String path = "C:\\Users\\"
				+ (new BufferedReader(new InputStreamReader(process.getInputStream()))).readLine().split("\\\\")[1]
				+ "\\downloads";
		System.out.println("'downloads' folder path=" + path);
		process.destroy();
		return new File(path);
	}

	public static String getPostBody(String crew, String start, String end, ArrayList<String> channels) {
		String postBody = "{\"eventType\":\"" + crew + "\",\"crew\":\"" + crew + "\",\"span\":\"tz '"
				+ TimeZone.getDefault().getID() + "' (" + start + " to " + end + ")\",\"outputMnemonics\":["
				+ getArrayAsString(channels) + "],\"includeCalculatedChannels\":false,\"outputFrequency\":\"1 sec\"}";
		return postBody;
	}

	private static String getArrayAsString(ArrayList<String> array) {
		String string = "";
		for (String s : array) {
			Matcher matcher = Pattern.compile("([^\\&]+)").matcher(s);
			if (matcher.find()) {
				for (String ss : s.split("&&")) {
					string += ",\"" + ss.trim() + "\"";
				}
				continue;
			}
			string += ",\"" + s + "\"";
		}
		return string.substring(1);
	}

	public String readRememberMeCookie() {

		return "CoOkIe";
	}

	private class DataMap implements Runnable {
		LinkedBlockingQueue<String> dataQueue;
		LinkedHashMap<String, ArrayList<String>> dataMap;
		LinkedBlockingQueue<String> incompleteQueue;
		private Semaphore semaphore = new Semaphore(0);
		private Boolean first = true;

		public DataMap(LinkedBlockingQueue<String> dataQueue, LinkedBlockingQueue<String> incompleteQueue) {
			this.dataQueue = dataQueue;
			this.dataMap = new LinkedHashMap<>();
			this.incompleteQueue = incompleteQueue;
		}

		private void addArraysToMap() throws InterruptedException {
			String[] channelArray = dataQueue.take().split(",");
			for (String s : channelArray) {
				this.dataMap.put(s.trim(), new ArrayList<String>());
			}
		}

		private void addDataToArrays() throws InterruptedException {
			String data = dataQueue.take();
			data = data.substring(0, data.length() - 1);
			if (isEmptyData(data)) {
				generateValues();
				return;
			}
			int i = 0;
			for (String s : dataMap.keySet()) {
				String value = "";
				if (i >= data.split(",").length) {
					value = "";
				} else {
					value = data.split(",")[i];
				}
				dataMap.get(s).add(value.equals("") ? getGeneratedValue(s) : value);
				i++;
			}
		}

		private void generateValues() {
			for (String s : dataMap.keySet()) {
				dataMap.get(s).add(getGeneratedValue(s));
			}
		}

		private String getGeneratedValue(String key) {
			if (key.equals("timestamp") | key.equals("Date")) {
				return getGeneratedTime(key);
			} else if (key.toLowerCase().equals("crew")) {
				return "crew";
			}
			if (dataMap.get(key).size() == 1) {
				return dataMap.get(key).get(0);
			} else if (dataMap.get(key).isEmpty()) {
				return "0";
			}
			double lastValue = Double.parseDouble(dataMap.get(key).get(dataMap.get(key).size() - 1));
			return String.valueOf(lastValue + getAverageSlope(key, 3.0f));
		}

		private Double getAverageSlope(String key, Float seconds) {
			Float indices = seconds;
			if (dataMap.get(key).size() < seconds) {
				indices = Float.valueOf(dataMap.get(key).size());
			}
			double slope = (Double.valueOf(dataMap.get(key).get(dataMap.get(key).size() - 1))
					- Double.valueOf(dataMap.get(key).get(dataMap.get(key).size() - indices.intValue())))
					/ (indices.doubleValue() - 1);
			return slope;
		}

		private String getStartFromRequestString() {
			Matcher matcher = Pattern.compile("\\d{4}\\-\\d{2}\\-\\d{2}\\s\\d{2}\\:\\d{2}").matcher(requestString);
			if (matcher.find()) {
				return matcher.group() + ":00";
			}
			return "0";
		}

		private String getGeneratedTime(String key) {
			if (dataMap.get(key).size() == 0) {
				return getStartFromRequestString();
			}
			String lastDateTime = checkFormatDate(dataMap.get(key).get(dataMap.get(key).size() - 1));

			LocalDateTime dateTime = LocalDateTime.parse(lastDateTime);
			return dateTime.plusSeconds(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		}

		private String checkFormatDate(String dateTimeString) {
			Matcher matcher = Pattern.compile("\\.").matcher(dateTimeString);
			if (matcher.find()) {
				return dateTimeString.split("\\.")[0];
			}
			return formatDate(dateTimeString);
		}

		private String formatDate(String dateTimeString) {
			Matcher dateMatcher = Pattern.compile("\\d+").matcher(dateTimeString);
			ArrayList<String> compArray = new ArrayList<>();
			int i = 0;
			while (dateMatcher.find() & i < 3) {
				compArray.add(dateMatcher.group());
			}
			return getDateFromArray(compArray) + "T" + getTimeString(dateTimeString);
		}

		private String getDateFromArray(ArrayList<String> compArray) {
			if (compArray.get(0).length() == 4) {
				return compArray.get(0) + "-" + compArray.get(1) + "-" + compArray.get(2);
			}
			return compArray.get(2) + "-" + compArray.get(0) + "-" + compArray.get(1);
		}

		private String getTimeString(String dateTimeString) {
			Matcher matcher = Pattern.compile("\\d{2}\\:\\d{2}((\\:\\d{2})?)").matcher(dateTimeString);
			if (matcher.find()) {
				return matcher.group();
			}
			return "0";
		}

		private boolean mapSet = false;

		LinkedHashMap<String, ArrayList<String>> getDataMap() throws InterruptedException {
			if (!mapSet) {
				semaphore.acquire();
				mapSet = true;
			}
			return this.dataMap;
		}

		@Override
		public void run() {
			try {
				addArraysToMap();
			} catch (InterruptedException e) {
				System.out.println("ONONONONONOON");
			}
			while (reading) {
				while (dataQueue.size() > 0) {
					try {
						addDataToArrays();
					} catch (InterruptedException e) {
						System.out.println("NONONONONONO");
					}
				}
			}
			semaphore.release();
		}
	}

	public static String capitalizeWord(String word) {
		Matcher matcher = Pattern.compile("\\-").matcher(word);
		if (matcher.find()) {
			return String.valueOf(word.charAt(0)).toUpperCase() + word.substring(1, matcher.start()).toLowerCase() + "-"
					+ String.valueOf(word.charAt(word.length() - 1)).toUpperCase();
		}
		return String.valueOf(word.charAt(0)).toUpperCase() + word.substring(1).toLowerCase();
	}

	public static String getPumpsRequestBody(HashMap<String, String> normCrewMap, String crew, String start,
			String end) {
		return "{\"format\":\"csv\",\"config\":{\"widgetId\":3034,\"span\":\"tz '" + TimeZone.getDefault().getID()
				+ "' (" + start + " to " + end + ")\",\"originalSpan\":\"" + start + " to " + end
				+ "\",\"spanType\":\"time\",\"prefilter\":null,\"lookupValues\":{\"crew_event_type\":[\""
				+ capAllIfNumber(crew) + "\"],\"asset_name_event_type\":[\"" + capAllIfNumber(crew)
				+ "\"],\"event_type_crew\":[\""
				+ normCrewMap.get(CrewRequest.removeSpecialCharacters(crew.replace(" ", "")))
				+ "\"],\"event_type_asset_name\":[\""
				+ normCrewMap.get(CrewRequest.removeSpecialCharacters(crew.replace(" ", "")))
				+ "\"]},\"omitReducer\":true,\"kind\":\"WIDGET\",\"userConfig\":{}}}";
	}

	public static String capAllIfNumber(String crew) {
		Matcher matcher = Pattern.compile("[aeiouAEIOU]").matcher(crew);
		if (matcher.find()) {
			return capitalizeWord(crew);
		}
		return crew.toUpperCase();
	}

	public static HashSet<String> unZipGzipToSet(GZIPInputStream inputStream) throws IOException {
		int i;
		byte[] bytes = new byte[64];
		HashSet<String> set = new HashSet<>();
		String temp = "";
		while ((i = inputStream.read(bytes)) != -1) {
			bytes = removeEmptyBytes(bytes);
			temp += getStringFromBytes(bytes);
			temp = addToRawSet(set, temp);
			bytes = new byte[64];
		}
		inputStream.close();
		return set;
	}

	public static boolean unZipFindGreaterThanValue(GZIPInputStream inputStream, String header, Double value)
			throws IOException {
		int i;
		byte[] bytes = new byte[1024];
		String temp = "";
		int index = -1;
		boolean found = false;
		ArrayList<String> array = new ArrayList<>();
		while ((i = inputStream.read(bytes)) != -1 & !found) {
			bytes = removeEmptyBytes(bytes);
			temp += getStringFromBytes(bytes);
			if (index == -1 && Pattern.compile("\\n").matcher(temp).find()) {
				index = findPosition(getStringByIndexOfPattern(temp, "(.*?)[\\n\\r]", 0), header)[0];
				temp = temp.substring(temp.indexOf(10) + 1, temp.length());
			} else if (index == -1) {
				bytes = new byte[1024];
				continue;
			}
			temp = addValuesToArray(array, index, temp);
			found = checkArrayForValuesGreaterThan(array, value);
			bytes = new byte[1024];
		}
		return found;
	}

	public static boolean checkNull(int[] intArray) {
		for (int i : intArray) {
			if (i == 0) {
				return true;
			}
		}
		return false;
	}

	public static LinkedHashMap<Integer, ArrayList<String>> addArraysToMap(int[] indices) {
		LinkedHashMap<Integer, ArrayList<String>> map = new LinkedHashMap<>();
		for (int i : indices) {
			map.put(i, new ArrayList<>());
		}
		return map;
	}

	public static LinkedHashMap<String, ArrayList<String>> getMapWithNames(
			LinkedHashMap<Integer, ArrayList<String>> map, int[] indices, String[] headers) {
		LinkedHashMap<String, ArrayList<String>> mapWithNames = new LinkedHashMap<>();
		int index = 0;
		for (int i : indices) {
			mapWithNames.put(headers[index], map.get(i));
			index++;
		}
		return mapWithNames;
	}

	public static LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>> getBigMapWithNames(
			LinkedHashMap<String, LinkedHashMap<Integer, ArrayList<String>>> map, int[] indices, String[] headers) {
		LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>> mapWithNames = new LinkedHashMap<>();
		for (String s : map.keySet()) {
			mapWithNames.put(s, getMapWithNames(map.get(s), indices, headers));
		}
		return mapWithNames;
	}

	public static LinkedHashMap<String, ArrayList<String>> unZipGzipStream(GZIPInputStream inputStream,
			String... headers) throws IOException {
		int i;
		byte[] bytes = new byte[1024];
		String temp = "";
		int[] indices = new int[headers.length];
		LinkedHashMap<Integer, ArrayList<String>> map = null;
		while ((i = inputStream.read(bytes)) > -1) {
			bytes = removeEmptyBytes(bytes);
			temp += getStringFromBytes(bytes);
			if (checkNull(indices) && Pattern.compile("\\n").matcher(temp).find()) {
				indices = findPosition(getStringByIndexOfPattern(temp, "(.*?)[\\n\\r]", 0), headers);
				map = addArraysToMap(indices);
				temp = temp.substring(temp.indexOf(10) + 1, temp.length());
			} else if (checkNull(indices)) {
				bytes = new byte[1024];
				continue;
			}

			temp = addValuesToMap(map, indices, temp);
			bytes = new byte[1024];
		}
		return getMapWithNames(map, indices, headers);
	}

	public static LinkedHashMap<String, LinkedHashMap<Integer, ArrayList<String>>> addMapsToMap(int[] indices,
			HashSet<String> pumpIDs) {
		LinkedHashMap<String, LinkedHashMap<Integer, ArrayList<String>>> map = new LinkedHashMap<>();
		int count = 0;
		for (String s : pumpIDs) {
			map.put(s, new LinkedHashMap<>());
			for (int i : indices) {
				map.get(s).put(i, new ArrayList<>());
			}
		}
		return map;
	}

	private final static String COMPLETE = "complete";
	private final static String RESIDUAL = "residual";

	public static int findStartOfIncomplete(String byteString) {
		Matcher matcher = Pattern.compile("[\\r\\n]([^\\r\\n]+)$").matcher(byteString);
		int lastStartIndex = 0;
		while (matcher.find()) {
			lastStartIndex = matcher.start();
		}

		return lastStartIndex;
	}

	public static HashMap<String, String> identifyCompleteValue(String byteString) {
		HashMap<String, String> map = new HashMap<>();
		int lastStart = findStartOfIncomplete(byteString);
		map.put(COMPLETE, byteString.substring(0, lastStart == 0 ? 0 : lastStart - 1));
		map.put(RESIDUAL, byteString.substring(lastStart < byteString.length() ? lastStart + 1 : byteString.length()));
		return map;
	}

	public static ArrayList<HashMap<Integer, String>> unZipGzipStream(GZIPInputStream inputStream) throws IOException {
		byte[] bytes = new byte[512];
		int i;
		String residString = "";
		System.out.println("Recieved Response");
		ArrayList<HashMap<Integer, String>> mapArray = new ArrayList<>();

		while ((i = inputStream.read(bytes)) > -1) {
			String byteString = getStringFromBytes(removeEmptyBytes(bytes));
			HashMap<String, String> partitioned = identifyCompleteValue(residString + byteString);
			System.out.println(partitioned);
			String completeAlerts = partitioned.get(COMPLETE);
			residString = partitioned.get(RESIDUAL);
			HashSet<String> alertsSet = getCompleteAlerts(completeAlerts);
			addAlertsToArray(alertsSet, mapArray);
			bytes = new byte[512];
		}
		return mapArray;
	}

	public static Integer getAvailableKey(Map<Integer, ?> map) {
		Integer i = 0;
		while (map.containsKey(i)) {
			i++;
		}
		return i;
	}

	public static void addAlertsToArray(HashSet<String> alertSet, ArrayList<HashMap<Integer, String>> array) {

		alertSet.forEach((String string) -> {
			array.add(parseSingleAlert(string));
		});
	}

	private final static String DATE_REGEX = "\\d{4}\\-\\d{2}\\-\\d{2}T\\d{2}\\:\\d{2}\\:\\d{2}\\.\\d{3}\\-\\d{4}";

	public static HashSet<String> getCompleteAlerts(String completeAlerts) {
		HashSet<String> set = new HashSet<>();
		for (String s : completeAlerts.split("\\n")) {
			set.add(s);
		}
		return set;
	}

	public static HashMap<Integer, String> parseSingleAlert(String alertJson) {
		HashMap<Integer, String> map = new HashMap<>();
		int count = 0;
		for (String s : alertJson.split(",")) {
			map.put(count, s);
			count++;
		}
		return map;
	}

	public static LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>> unZipGzipStream(
			GZIPInputStream inputStream, HashSet<String> pumpIDs, String... headers) throws IOException {
		int i;
		byte[] bytes = inputStream.readAllBytes();// new byte[512];
		String temp = "";
		int[] indices = new int[headers.length];
		int pumpNameIndex = 0;
		LinkedHashMap<String, LinkedHashMap<Integer, ArrayList<String>>> map = null;
		// while ((i = inputStream.read(bytes,0,bytes.length)) > 0) {
		bytes = removeEmptyBytes(bytes);
		temp += getStringFromBytes(bytes);
		if (checkNull(indices) && Pattern.compile("\\n").matcher(temp).find()) {
			pumpNameIndex = findPosition(getStringByIndexOfPattern(temp, "(.*?)[\\n\\r]", 0), "Pump Name")[0];
			indices = findPosition(getStringByIndexOfPattern(temp, "(.*?)[\\n\\r]", 0), headers);
			map = addMapsToMap(indices, pumpIDs);
			temp = temp.substring(temp.indexOf(10) + 1, temp.length());
		} else if (checkNull(indices)) {
			bytes = new byte[512];
			// continue;
		}

		temp = addValuesToMap(map, indices, temp, pumpNameIndex);
		bytes = new byte[512];
		// }
		return getBigMapWithNames(map, indices, headers);
	}

	public static boolean checkArrayForValuesGreaterThan(ArrayList<String> array, Double value) {
		for (String s : array) {
			Matcher matcher = Pattern.compile("[^\\d\\.]").matcher(s);
			if (matcher.find() | s.isEmpty()) {
				continue;
			}
			if (Double.valueOf(s) > value) {
				return true;
			}
		}
		array.removeAll(array);
		return false;
	}

	public static String addValuesToArray(ArrayList<String> array, int index, String resp) {
		Matcher matcher = Pattern.compile("[^\\n]+").matcher(resp);
		while (matcher.find()) {
			Matcher matcher2 = Pattern.compile("(^|,)(.*?)($|,)").matcher(matcher.group());
			int i = 0;
			String response = resp;
			System.out.println(response);
			while (matcher2.find()) {
				if (i < index) {
					response = response.substring(matcher2.end());
					matcher2.reset(response);
					i++;
					continue;
				}
				if (matcher2.group().length() > 0) {
					array.add(matcher2.group().substring(0, matcher2.group().length() - 1));
				}
				break;
			}
			if (matcher.end() == resp.length()) {
				break;
			}
			resp = resp.substring(matcher.end() + 1);
			matcher.reset(resp);
		}
		return resp;

	}

	public static boolean checkArrayForInt(int[] array, int i) {
		for (int ind : array) {
			if (ind == i) {
				return true;
			}
		}
		return false;
	}

	private static String getPumpName(String resp, int pumpNameIndex) {
		String response = resp;
		Matcher matcher = Pattern.compile("(^|,)(.*?)($|,)").matcher(response);
		int count = 0;
		while (matcher.find() & count <= pumpNameIndex) {
			if (count == pumpNameIndex) {
				Matcher matcher2 = Pattern.compile("\\d+").matcher(matcher.group());
				return matcher2.find() ? matcher2.group() : "0";
			}
			response = response.substring(matcher.end());
			matcher.reset(response);
			count++;
		}
		return "0";
	}

	public static int[] orderIntArray(int[] array) {
		int[] newArray = new int[array.length];
		newArray[0] = findMinValue(array);
		int lastMin = newArray[0];
		int count = 1;
		for (int i = 0; i < array.length - 1; i++) {
			int nextSmallNum = findNextSmallNum(lastMin, array);
			int numInstances = countInstances(nextSmallNum, array);
			numInstances = nextSmallNum == newArray[0] ? numInstances - 1 : numInstances;
			for (int ii = count; ii < count + numInstances; ii++) {
				newArray[count] = nextSmallNum;
				count++;
			}
		}
		return newArray;
	}

	public static int countInstances(int num, int[] array) {
		int count = 0;
		for (int i : array) {
			if (i == num) {
				count++;
			}
		}
		return count;
	}

	public static int findNextSmallNum(int greaterThan, int[] array) {
		int nextMin = greaterThan;
		for (int i : array) {
			nextMin = i > greaterThan & i < nextMin ? i : nextMin;
		}
		return nextMin;
	}

	public static int findMinValue(int[] array) {
		int min = array[0];
		for (int i : array) {
			min = min > i ? i : min;
		}
		return min;
	}

	public static String addValuesToMap(LinkedHashMap<String, LinkedHashMap<Integer, ArrayList<String>>> map,
			int[] indices, String resp, int pumpNameIndex) {
		if (map == null) {
			return resp;
		}
		Matcher matcher = Pattern.compile("[^\\n]+").matcher(resp);
		int end = 0;
		while (matcher.find()) {
			Matcher matcher2 = Pattern.compile("(^|,)(.*?)($|,)").matcher(matcher.group());
			int i = 0;
			String pumpName = getPumpName(matcher.group(), pumpNameIndex);
			if (pumpName.equals("0")) {
				return resp;
			}
			String response = resp;
			String[] splitIndex = matcher.group().split(",");
			for (int valIndex : indices) {
				if (splitIndex.length <= valIndex || splitIndex[valIndex].equals("")) {
					continue;
				}
				map.get(pumpName).get(valIndex).add(splitIndex[valIndex]);
			}
			/*
			 * while (matcher2.find()) { if (!checkArrayForInt(indices, i)) { response =
			 * response.substring(matcher2.end()); matcher2.reset(response); i++; continue;
			 * } Matcher matcher3 = Pattern.compile("[\\d\\.]+").matcher(matcher2.group());
			 * if (matcher3.find()) { map.get(pumpName).get(i).add(matcher3.group()); break;
			 * } else if (matcher.end() != resp.length()) { response =
			 * response.substring(matcher2.end()); matcher2.reset(response); i++; continue;
			 * } }
			 */
			if (matcher.end() == resp.length()) {
				break;
			}
			resp = resp.substring(matcher.end() + 1);
			matcher.reset(resp);
		}
		return resp;

	}

	public static String addValuesToMap(LinkedHashMap<Integer, ArrayList<String>> map, int[] indices, String resp) {
		if (map == null) {
			return resp;
		}
		Matcher matcher = Pattern.compile("[^\\n]+").matcher(resp);
		int end = 0;
		while (matcher.find()) {
			Matcher matcher2 = Pattern.compile("(^|,)(.*?)($|,)").matcher(matcher.group());
			int i = 0;
			String response = resp;
			while (matcher2.find()) {
				if (!checkArrayForInt(indices, i)) {
					response = response.substring(matcher2.end());
					matcher2.reset(response);
					i++;
					continue;
				}
				Matcher matcher3 = Pattern.compile("[\\d\\.]+").matcher(matcher2.group());
				if (matcher3.find()) {
					map.get(i).add(matcher3.group());
					break;
				} else if (matcher.end() != resp.length()) {
					response = response.substring(matcher2.end());
					matcher2.reset(response);
					i++;
					continue;
				}
			}
			if (matcher.end() == resp.length()) {
				break;
			}
			resp = resp.substring(matcher.end() + 1);
			matcher.reset(resp);
		}
		return resp;

	}

	public static String getStringByIndexOfPattern(String string, String regex, int index) {
		Matcher matcher = Pattern.compile(regex).matcher(string);
		int count = 0;
		while (matcher.find()) {
			if (count < index) {
				count++;
			}
			return matcher.group();
		}
		return string;
	}

	public static boolean checkArrayForString(String[] array, String search) {
		for (String s : array) {
			if (s.equals(search)) {
				return true;
			}
		}
		return false;
	}

	public static int[] findPosition(String headers, String... allowedHeaders) {
		LinkedHashMap<String, ArrayList<String>> map = new LinkedHashMap<>();
		int[] allowedIndeces = new int[allowedHeaders.length];
		int i = 0;
		int count = 0;
		for (String s : headers.split(",")) {
			if (checkArrayForString(allowedHeaders, s)) {
				allowedIndeces[i] = count;
				i++;
			} else if (i == allowedHeaders.length) {
				break;
			}
			count++;
		}
		return allowedIndeces;
	}

	public static String addToRawSet(HashSet<String> set, String temp) {
		Matcher matcher = Pattern.compile(">\\d+<").matcher(temp);
		while (matcher.find()) {
			set.add(matcher.group().substring(1, matcher.group().length() - 1));
			temp = temp.substring(matcher.end());
			matcher.reset(temp);
		}
		return temp;
	}

	public static String getStringFromBytes(byte[] bytes) {
		StringBuilder builder = new StringBuilder();
		for (byte b : bytes) {
			builder.append((char) b);
		}
		return builder.toString();
	}

	private static int getFirstEmptyByte(byte[] bytes) {
		for (int i = 0; i < bytes.length; i++) {
			if (bytes[i] == 0) {
				return i;
			}
		}
		return bytes.length;
	}

	public static byte[] removeEmptyBytes(byte[] bytes) {
		int size = getFirstEmptyByte(bytes);
		if (size == bytes.length) {
			return bytes;
		}
		byte[] newBytes = new byte[size];
		for (int i = 0; i < size; i++) {
			newBytes[i] = bytes[i];
		}
		return newBytes;
	}

	public static HashSet<String> getPumpsRequest(HashMap<String, String> normCrewMap, String crew, String cookies,
			String csrfToken, String start, String end) throws InterruptedException, IOException {
		System.out.println(getPumpsRequestBody(normCrewMap, crew, start, end));
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().header("accept", "*/*")
				.header("accept-encoding", "gzip, deflate, br").header("content-type", "application/json")
				.header("cookie", cookies).header("X-CSRF-TOKEN", csrfToken)
				.uri(URI.create("https://propetro.intelie.com/rest/download/widget"))
				.POST(HttpRequest.BodyPublishers.ofString(getPumpsRequestBody(normCrewMap, crew, start, end))).build();
		HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		HashSet<String> set = unZipGzipToSet(new GZIPInputStream(response.body()));
		System.out.println(set);
		return set;
	}

	public static String getPumpDataPostBody(String crew, String start, String end, String pumpID) {
		return "{\"format\":\"csv\",\"config\":{\"widgetId\":3046,\"span\":\"tz '" + TimeZone.getDefault().getID()
				+ "' (" + start + " to " + end + ")\",\"originalSpan\":\"" + start + " to " + end
				+ "\",\"spanType\":\"time\",\"prefilter\":null,\"lookupValues\":{\"crew_event_type\":[\""
				+ capAllIfNumber(crew) + "\"],\"pump_list\":[\"" + pumpID
				+ "\"],\"pump_abbreviated_mnemonics\":[\"Trans Gear\"]},\"omitReducer\":true,\"kind\":\"WIDGET\",\"userConfig\":{}}}";
	}

	public static String getPumpDataPostBody(String crew, String start, String end, String pumpID, String... channels) {
		return "{\"format\":\"csv\",\"config\":{\"widgetId\":3046,\"span\":\"tz '" + TimeZone.getDefault().getID()
				+ "' (" + start + " to " + end + ")\",\"originalSpan\":\"" + start + " to " + end
				+ "\",\"spanType\":\"time\",\"prefilter\":null,\"lookupValues\":{\"crew_event_type\":[\""
				+ capAllIfNumber(crew) + "\"],\"pump_list\":[" + pumpID + "],\"pump_abbreviated_mnemonics\":["
				+ getJSonPumpChannels(channels) + "]},\"omitReducer\":true,\"kind\":\"WIDGET\",\"userConfig\":{}}}";
	}

	public static String getPumpDataPostBodyWithOrder(String crew, String start, String end, String pumpID,
			ArrayList<Integer> order, String... channels) {
		return "{\"format\":\"csv\",\"config\":{\"widgetId\":3046,\"span\":\"tz '" + TimeZone.getDefault().getID()
				+ "' (" + start + " to " + end + ")\",\"originalSpan\":\"" + start + " to " + end
				+ "\",\"spanType\":\"time\",\"prefilter\":null,\"lookupValues\":"
				+ getLookupValueString(order, getLookupValuesArray(crew, pumpID, channels))
				+ ",\"omitReducer\":true,\"kind\":\"WIDGET\",\"userConfig\":{}}}";
	}

	public static String getLookupValueString(ArrayList<Integer> orderArray, ArrayList<String> lookupValues) {
		StringBuilder string = new StringBuilder();
		orderArray.forEach((Integer integer) -> {
			string.append("," + lookupValues.get(integer.intValue()));
		});
		return string.substring(1);
	}

	public static ArrayList<String> getLookupValuesArray(String crew, String pumpID, String[] channels) {
		ArrayList<String> array = new ArrayList<>();
		array.add("{\"crew_event_type\":[\"" + capAllIfNumber(crew) + "\"]");
		array.add("\"pump_list\":[" + pumpID + "]");
		array.add("\"pump_abbreviated_mnemonics\":[" + getJSonPumpChannels(channels) + "]}");
		return array;
	}

	public static int reverseFact(Collection<?> collection) {
		int size = collection.size();
		if (size == 1) {
			return 1;
		}
		for (int i = 2; i < size; i++) {
			if (size / getFactorial(i - 1) == i) {
				return i + 1;
			}
		}
		return 0;
	}

	public static ArrayList<ArrayList<Integer>> getMorePossibleOrders(ArrayList<ArrayList<Integer>> set) {
		if (set.isEmpty()) {
			return set;
		}
		ArrayList<Integer> array = new ArrayList<>();
		int numParams = set.get(0).size() + 1;
		ArrayList<ArrayList<Integer>> agg = new ArrayList<>();
		for (int i = 0; i < numParams; i++) {
			ArrayList<ArrayList<Integer>> newSet = new ArrayList<>();
			newSet.addAll(set);
			agg.addAll(addReplace(newSet, i, numParams - 1));
		}
		return agg;
	}

	public static ArrayList<ArrayList<Integer>> addReplace(ArrayList<ArrayList<Integer>> addedSet, int add,
			int replace) {
		ArrayList<ArrayList<Integer>> newSet = new ArrayList<>();
		addedSet.forEach((ArrayList<Integer> set2) -> {
			ArrayList<Integer> set = new ArrayList<>();
			set.add(add);
			if (add == replace) {
				set2.forEach((Integer integer) -> {
					set.add(integer);
				});
			} else {
				set2.forEach((Integer integer) -> {
					if (set.contains(integer)) {
						set.add(replace);
					} else {
						set.add(integer);
					}
				});
			}
			newSet.add(set);
		});
		return newSet;
	}

	public static ArrayList<ArrayList<Integer>> getPossibleOrders(int numParams) {
		ArrayList<ArrayList<Integer>> array = getPrimer(numParams < 3 ? numParams : 3);
		for (int i = 3; i < numParams; i++) {
			array = getMorePossibleOrders(array);
		}
		return array;
	}

	public static ArrayList<Integer> getSingleValueArray(Integer value) {
		ArrayList<Integer> array = new ArrayList<>();
		array.add(value);
		return array;
	}

	public static ArrayList<ArrayList<Integer>> getPrimer(int numParams) {
		ArrayList<ArrayList<Integer>> array = new ArrayList<>();
		if (numParams == 1) {
			ArrayList<Integer> single = getSingleValueArray(0);
			array.add(single);
			return array;
		}
		for (int i = 1; i < numParams; i++) {
			int start = i;
			for (int ii = 0; ii < numParams; ii++) {
				ArrayList<Integer> temp = new ArrayList<>();
				temp.add(ii);
				start = getPossibleOrders(numParams, temp, start);
				array.add(temp);
			}
		}
		return array;
	}

	public static int getPossibleOrders(int numParams, ArrayList<Integer> array, int start) {
		int add = 0;
		for (int i = start; i < numParams + start; i++) {
			add = i % (numParams);

			if (array.contains(add)) {
				continue;
			}
			array.add(add);
			break;
		}
		if (array.size() == numParams) {
			return add + 1;
		}
		int shift = getPossibleOrders(numParams, array, start);
		return shift;
	}

	public static int getFactorial(int num) {
		int answer = 1;
		for (int i = num; i > 0; i--) {
			answer *= i;
		}
		return answer;
	}

	public static String getJSonPumpChannels(String... channels) {
		String json = "";
		for (String s : channels) {
			json += ",\"" + s + "\"";
		}
		return json.substring(1);
	}

	public static boolean isPumpOnline(String crew, String cookies, String csrfToken, String start, String end,
			String pumpID) throws InterruptedException, IOException, DataFormatException {
		System.out.println(cookies);
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().header("accept", "*/*")
				.header("content-type", "application/json").header("accept-encoding", "gzip, deflate, br")
				.header("cookie", cookies).header("X-CSRF-TOKEN", csrfToken)
				.uri(URI.create("https://propetro.intelie.com/rest/download/widget"))
				.POST(HttpRequest.BodyPublishers.ofString(getPumpDataPostBody(crew, start, end, pumpID))).build();

		HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		boolean online = false;
		try {
			online = unZipFindGreaterThanValue(new GZIPInputStream(response.body()), "Trans Gear", 0.0);
		} catch (NullPointerException e) {
			return online;
		}
		return online;
	}

	public static LinkedHashMap<String, ArrayList<String>> pumpDataRequest(String crew, String cookies,
			String csrfToken, String start, String end, String pumpID, String... channels)
			throws InterruptedException, IOException, DataFormatException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().header("accept", "*/*")
				.header("content-type", "application/json").header("accept-encoding", "gzip, deflate, br")
				.header("cookie", cookies).header("X-CSRF-TOKEN", csrfToken).timeout(Duration.ofSeconds(10))
				.uri(URI.create("https://propetro.intelie.com/rest/download/widget"))
				.POST(HttpRequest.BodyPublishers.ofString(getPumpDataPostBody(crew, start, end, pumpID, channels)))
				.build();

		HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		LinkedHashMap<String, ArrayList<String>> map = null;

		try {
			map = unZipGzipStream(new GZIPInputStream(response.body()), channels);
		} catch (NullPointerException e) {
			System.out.println("DataRequest::pumpDataRequest Exception");
			return null;
		}
		System.out.println(map);
		return map;
	}

	public static String getStringFromSet(HashSet<String> set) {
		String string = "";
		for (String s : set) {
			string += ",\"" + s + "\"";
		}
		return string.substring(1);
	}

	public static LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>> pumpDataRequest(String crew,
			String cookies, String csrfToken, String start, String end, HashSet<String> pumpIDs, String... channels)
			throws HttpTimeoutException, InterruptedException, IOException, DataFormatException {

		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().header("accept", "*/*")
				.header("content-type", "application/json").header("accept-encoding", "gzip, deflate, br")
				.header("cookie", cookies).header("X-CSRF-TOKEN", csrfToken).timeout(Duration.ofSeconds(10l))
				.uri(URI.create("https://propetro.intelie.com/rest/download/widget")).POST(HttpRequest.BodyPublishers
						.ofString(getPumpDataPostBody(crew, start, end, getStringFromSet(pumpIDs), channels)))
				.build();
		System.out.println("Waiting on response");
		HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		System.out.println("Received Response");
		LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>> map = null;

		try {
			map = unZipGzipStream(new GZIPInputStream(response.body()), pumpIDs, channels);
		} catch (NullPointerException e) {
			System.out.println("DataRequest::pumpDataRequest Exception");
			return null;
		}

		System.out.println(map);
		return map;
	}

	public static CompletableFuture<LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>>> pumpDataRequestFuture(
			String crew, String cookies, String csrfToken, String start, String end, HashSet<String> pumpIDs,
			Executor executor, String... channels)
			throws HttpTimeoutException, InterruptedException, IOException, DataFormatException {
		CompletableFuture<LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>>> mapFuture = new CompletableFuture<>();
		ArrayList<ArrayList<Integer>> orderArray = getPossibleOrders(3);
		int index = readIndex();
		executor.execute(() -> {
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().header("accept", "*/*")
					.header("content-type", "application/json").header("accept-encoding", "gzip, deflate, br")
					.header("cookie", cookies).header("X-CSRF-TOKEN", csrfToken)
					.uri(URI.create("https://propetro.intelie.com/rest/download/widget"))
					.POST(HttpRequest.BodyPublishers
							.ofString(getPumpDataPostBody(crew, start, end, getStringFromSet(pumpIDs), channels)))
					.build();
			System.out.println("Waiting on response");
			HttpResponse<InputStream> response = null;
			try {
				response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
			} catch (IOException | InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			System.out.println("Received Response");
			try {
				mapFuture.complete(unZipGzipStream(new GZIPInputStream(response.body()), pumpIDs, channels));
			} catch (NullPointerException | IOException e) {
				e.printStackTrace();
				try {
					int newIndex = getRightIndex(crew, cookies, csrfToken, start, end, pumpIDs, executor, 0, channels);
					mapFuture.complete(
							pumpDataRequestFuture(crew, cookies, csrfToken, start, end, pumpIDs, executor, 0, channels)
									.get());
				} catch (InterruptedException | ExecutionException | IOException | DataFormatException e1) {
					mapFuture.complete(null);
					System.out.println("DataRequest::pumpDataRequest Exception");
				}
			}
		});

		return mapFuture;
	}

	public static void writeIndex(Integer index) {
		try {
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(new File(INDEX_PATH)));
			objectOutputStream.writeObject(index);
			objectOutputStream.flush();
			objectOutputStream.close();
		} catch (IOException e) {
			return;
		}
	}

	public final static String INDEX_PATH = "index.scp";

	public static Integer readIndex() {
		Integer index = 0;
		try {
			ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(new File(INDEX_PATH)));
			index = (Integer) objectInputStream.readObject();
		} catch (IOException | ClassNotFoundException e) {
			return index;
		}
		return index;
	}

	public static int getRightIndex(String crew, String cookies, String csrfToken, String start, String end,
			HashSet<String> pumpIDs, Executor executor, int index, String... channels) {
		ArrayList<ArrayList<Integer>> order = getPossibleOrders(3);
		for (int i = 0; i < order.size(); i++) {
			try {
				CompletableFuture<LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>>> completable = pumpDataRequestFuture(
						crew, cookies, csrfToken, start, end, pumpIDs, executor, i, channels);
				completable.get(15, TimeUnit.SECONDS);
			} catch (Exception e) {
				continue;
			}
			System.out.println("AND THE RIGHT ORDER IS........" + i);
			return i;
		}
		System.out.println("NO DICE");
		return -1;
	}

	public static Set<Integer> getIndexOfSet(Set<Set<Integer>> set, int index) {
		if (index < 0) {
			return null;
		}
		int count = 0;
		Set<Integer> set2;
		for (Set<Integer> element : set) {
			if (count == index) {
				return element;
			}
			count++;
		}
		return null;
	}

	private static String getAlertsPostBody(String timeframe) {
		return "{\"format\":\"csv\",\"config\":{\"widgetId\":3014,\"span\":\"tz 'America/Chicago' (" + timeframe
				+ ")\",\"originalSpan\":\"" + timeframe
				+ "\",\"spanType\":\"time\",\"prefilter\":null,\"lookupValues\":{},\"omitReducer\":true,\"kind\":\"WIDGET\",\"userConfig\":{}}}";
	}

	public static ArrayList<HashMap<String, String>> structureData(ArrayList<HashMap<Integer, String>> map,
			HashMap<Integer, String> indexMap) {
		ArrayList<HashMap<String, String>> structMap = new ArrayList<>();
		map.forEach((HashMap<Integer, String> singleMap) -> {
			HashMap<String, String> tempMap = new HashMap<>();
			singleMap.keySet().forEach((Integer i) -> {
				tempMap.put(indexMap.get(i), singleMap.get(i));
			});
			structMap.add(tempMap);
		});
		return structMap;
	}

	public static String getCrewsJSonString(ArrayList<String> crews) {
		String crewString = "";
		for (String s : crews) {
			crewString += ",\"" + s + "\"";
		}
		return crewString.substring(1);
	}

	public static String getAssetHistoryPostBody(ArrayList<String> crews, String timeFrame) {
		String body = "{\"format\":\"json\",\"config\":{\"widgetId\":3042,\"span\":\"tz 'America/Chicago' (" + timeFrame
				+ ")\",\"originalSpan\":\"" + timeFrame
				+ "\",\"spanType\":\"time\",\"prefilter\":null,\"lookupValues\":{\"crew_event_type\":["
				+ getCrewsJSonString(crews) + "]},\"omitReducer\":true,\"kind\":\"WIDGET\",\"userConfig\":{}}}";
		System.out.println(body);
		return body;
	}

	public static HashMap<String,ArrayList<String>> makeAssetHistoryRequest(String timeFrame, String cookies, String csrfToken,
			ArrayList<String> crews,String assetID) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().header("accept", "*/*")
				.header("accept-encoding", "gzip, deflate, br").header("content-type", "application/json")
				.header("cookie", cookies).header("x-csrf-token", csrfToken)
				.uri(URI.create("https://propetro.intelie.com/rest/download/widget"))
				.POST(HttpRequest.BodyPublishers.ofString(getAssetHistoryPostBody(crews, timeFrame))).build();
		HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		HashMap<String, ArrayList<String>> assetCrewMap = getAssetCrewMap(response.body(),assetID);
		return assetCrewMap;

	}

	public static void printGZipStream(InputStream inputStream) throws IOException {
		GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
		byte[] bytes = new byte[2048];
		int i;
		while ((i = gzipInputStream.read(bytes)) > 0) {
			System.out.println(bytesToString(bytes));
		}
	}

	public static HashMap<String, ArrayList<String>> getAssetCrewMap(InputStream inputStream, ArrayList<String> idArray)
			throws IOException {
		GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
		byte[] bytes = new byte[2048];
		int i;
		HashMap<String, ArrayList<String>> assetCrewMap = getAssetCrewMapStruct(idArray);
		String responseString = "";
		while ((i = gzipInputStream.read(bytes)) > -1) {
			responseString += bytesToString(bytes);
			responseString = assessCurrentResponse(assetCrewMap, responseString, idArray);
		}
		return assetCrewMap;
	}
	
	public static HashMap<String, ArrayList<String>> getAssetCrewMap(InputStream inputStream, String id)
			throws IOException {
		HashMap<String, ArrayList<String>> assetCrewMap = getAssetCrewMapStruct(id);
		try(
			GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
		){
			byte[] bytes = new byte[2048];
			int i;
			String responseString = "";
			while ((i = gzipInputStream.read(bytes)) > -1) {
				responseString += bytesToString(bytes);
				responseString = assessCurrentResponse(assetCrewMap, responseString, id);
			}
			return assetCrewMap;
		}catch(IOException e) {
			e.printStackTrace();
			return assetCrewMap;
		}
	}

	public static String getAssetCrewPattern(ArrayList<String> idArray) {
		String pattern = "\"id\"\\:(";
		for (String s : idArray) {
			pattern += s + "|";
		}
		pattern = pattern.substring(0, pattern.length() - 1) + "),\"lastCrew\"\\:\"([\\w\\s]+?)\"";
		return pattern;
	}
	
	public static String getAssetCrewPattern(String...idArray) {
		String pattern = "\"id\"\\:(";
		for (String s : idArray) {
			pattern += "\"?"+s +"\"?"+ "|";
		}
		pattern = pattern.substring(0, pattern.length() - 1) + "),\"lastCrew\"\\:\"([\\w\\s]+?)\"";
		System.out.println(pattern);
		return pattern;
	}
	
	public static String assessCurrentResponse(HashMap<String, ArrayList<String>> assetCrewMap, String responseString,
			ArrayList<String> idArray) {
		String pattern = getAssetCrewPattern(idArray);
		System.out.println(pattern);
		Matcher matcher = Pattern.compile(pattern).matcher(responseString);
		int end = 0;
		while (matcher.find()) {
			String found = matcher.group();
			addToAssetCrewMap(found, assetCrewMap);
			end = matcher.end();
		}
		return responseString.substring(end);
	}
	
	public static String assessCurrentResponse(HashMap<String, ArrayList<String>> assetCrewMap, String responseString,
			String id) {
		Matcher matcher = Pattern.compile(getAssetCrewPattern(id)).matcher(responseString);
		int end = 0;
		while (matcher.find()) {
			String found = matcher.group();
			addToAssetCrewMap(found, assetCrewMap);
			end = matcher.end();
		}
		return responseString.substring(end);
	}
	
	public static void addToAssetCrewMap(String found, HashMap<String, ArrayList<String>> assetCrewMap) {
		String id = getJsonFieldValue(found.split(",")[0]);
		String lastCrew = getJsonFieldValue(found.split(",")[1]);
		
		if(assetCrewMap.containsKey(id)) {
			assetCrewMap.get(id).add(lastCrew);
			return;
		}
		
		assetCrewMap.put(id, new ArrayList<>());
		assetCrewMap.get(id).add(found.split(":")[1].replace("\"", ""));
	}
	
	public static String getJsonFieldValue(String string) {
		String value = string.split(":")[1].replace("\"", "");
		return value;
	}
	
	public static HashMap<String, ArrayList<String>> getAssetCrewMapStruct(ArrayList<String> idArray) {
		HashMap<String, ArrayList<String>> map = new HashMap<>();
		for (String s : idArray) {
			map.put(s, new ArrayList<>());
		}
		return map;
	}
	
	public static HashMap<String, ArrayList<String>> getAssetCrewMapStruct(String id) {
		HashMap<String, ArrayList<String>> map = new HashMap<>();
		map.put(id, new ArrayList<>());
		
		return map;
	}

	public static String bytesToString(byte[] bytes) {
		StringBuilder stringBuilder = new StringBuilder();
		for (byte b : bytes) {
			if (b == 0) {
				return stringBuilder.toString();
			}
			stringBuilder.append((char) b);
		}
		return stringBuilder.toString();
	}

	public static ArrayList<HashMap<Integer, String>> makeAlertsRequest(String timeframe, String cookies,
			String csrfToken) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().header("accept", "*/*")
				.header("accept-encoding", "gzip, deflate, br").header("content-type", "application/json")
				.header("cookie", cookies).header("x-csrf-token", csrfToken)
				.uri(URI.create("https://propetro.intelie.com/rest/download/widget"))
				.POST(HttpRequest.BodyPublishers.ofString(getAlertsPostBody(timeframe))).build();
		HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		return unZipGzipStream(new GZIPInputStream(response.body()));
	}

	public static CompletableFuture<LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>>> pumpDataRequestFuture(
			String crew, String cookies, String csrfToken, String start, String end, HashSet<String> pumpIDs,
			Executor executor, int index, String... channels)
			throws HttpTimeoutException, InterruptedException, IOException, DataFormatException {

		CompletableFuture<LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>>> mapFuture = new CompletableFuture<>();
		executor.execute(() -> {
			ArrayList<ArrayList<Integer>> orderArray = getPossibleOrders(3);
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().header("accept", "*/*")
					.header("content-type", "application/json").header("accept-encoding", "gzip, deflate, br")
					.header("cookie", cookies).header("X-CSRF-TOKEN", csrfToken).timeout(Duration.ofSeconds(10l))
					.uri(URI.create("https://propetro.intelie.com/rest/download/widget"))
					.POST(HttpRequest.BodyPublishers.ofString(getPumpDataPostBodyWithOrder(crew, start, end,
							getStringFromSet(pumpIDs), orderArray.get(index), channels)))
					.build();
			HttpResponse<InputStream> response = null;
			try {
				response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
			} catch (IOException | InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println("Received Response");
			try {
				mapFuture.complete(unZipGzipStream(new GZIPInputStream(response.body()), pumpIDs, channels));
			} catch (NullPointerException | IOException e) {
				System.out.println("DataRequest::pumpDataRequest Exception");
			}
		});
		return mapFuture;
	}
}

