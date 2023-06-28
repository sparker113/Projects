package data;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class Nasdaq {
	private boolean isWaiting = false;
	private Semaphore waiting = new Semaphore(0);
	private boolean reading = true;
	private Map<String, Map<LocalDate, Map<String, String>>> historicMap;
	ExecutorService executor = Executors.newCachedThreadPool();
	private CompletableFuture<Map<String, Map<String, String>>> dataMapFuture = new CompletableFuture<>();

	Nasdaq() {
		historicMap = new LinkedHashMap<>();
	}

	Nasdaq(String filter) {
		historicMap = new LinkedHashMap<>();
	}

	void setDataMap(CompletableFuture<Map<String, Map<String, String>>> dataMap) {
		this.dataMapFuture = dataMap;
	}

	public CompletableFuture<Map<String, Map<String, String>>> getDataFuture() {
		return this.dataMapFuture;
	}

	public Map<String, Map<String, String>> getData() throws InterruptedException, ExecutionException {
		return dataMapFuture.get();
	}

	public ExecutorService getExecutor() {
		if (executor.isTerminated()) {
			executor = Executors.newCachedThreadPool();
		}
		return executor;
	}

	private synchronized void addToHistoricMap(String ticker, Map<LocalDate, Map<String, String>> singleHistoricMap) {
		System.out.println(ticker);
		System.out.println(singleHistoricMap);
		historicMap.put(ticker, singleHistoricMap);
	}

	public Map<String, Map<LocalDate, Map<String, String>>> getHistoricMap() {
		return this.historicMap;
	}

	public void setHistoricData(int days)
			throws InterruptedException, ExecutionException {
		Map<String, Map<String, String>> currentData = getData();
		ExecutorService executor = Executors.newFixedThreadPool(3);
		CountDownLatch latch = new CountDownLatch(currentData.size());
		for (String s : currentData.keySet()) {
			executor.execute(() -> {
				try {
					addToHistoricMap(s, getStockHistory(s, days).get());
					latch.countDown();
				} catch (IOException | InterruptedException | ExecutionException e) {
					e.printStackTrace();
					latch.countDown();
					return;
				}
			});
		}
		latch.await();
		executor.shutdown();
	}

	public CompletableFuture<Map<LocalDate, Map<String, String>>> getStockHistory(String ticker, int days)
			throws IOException, InterruptedException {
		LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
		LocalDate firstDate = getDay(days, LocalDate.now());
		InputStream inputStream = DataRequest.makeConfiguredRequest(getHistoricURI(ticker, firstDate, LocalDate.now()));
		System.out.println("Received Response");
		SyncControls syncControls = new SyncControls();
		Executors.newSingleThreadExecutor().execute(() -> {
			try {
				parseDayResponse(new GZIPInputStream(inputStream), queue, syncControls);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
				syncControls.reading = false;
				syncControls.continueFromWait();
			}
		});
		CompletableFuture<Map<LocalDate, Map<String, String>>> historicFuture = parseDataFromQueue(queue, syncControls);

		return historicFuture;
	}

	private CompletableFuture<Map<LocalDate, Map<String, String>>> parseDataFromQueue(
			LinkedBlockingQueue<String> queue, SyncControls syncControls) {
		CompletableFuture<Map<LocalDate, Map<String, String>>> historicMapFuture = new CompletableFuture<>();
		Executors.newSingleThreadExecutor().execute(() -> {
			Map<LocalDate, Map<String, String>> historicMap = new LinkedHashMap<>();
			while (queue.size() > 0 | syncControls.reading == true) {
				if (queue.size() == 0) {
					syncControls.isWaiting = true;
					try {
						syncControls.waiting.acquire();
					} catch (InterruptedException e) {
						e.printStackTrace();
						continue;
					}
					syncControls.isWaiting = false;
					continue;
				}
				String dayData = queue.poll();
				Map<String, String> dayMap = parseSimpleObject(dayData);
				if (!dayMap.containsKey(Historic.DATE.getValue())) {
					continue;
				}
				historicMap.put(getLocalDate(dayMap.get(Historic.DATE.getValue())), dayMap);
			}
			historicMapFuture.complete(historicMap);
		});
		return historicMapFuture;
	}

	private static LocalDate getLocalDate(String date) {
		Matcher matcher = Pattern.compile("\\d+").matcher(date);
		ArrayList<String> array = new ArrayList<>();
		while (matcher.find()) {
			array.add(matcher.group());
		}
		if (array.size() < 3) {
			return LocalDate.now();
		}
		return getDateFromArray(array);
	}

	private static LocalDate getDateFromArray(ArrayList<String> array) {
		if (array.get(0).length() >= 4) {
			System.out.println("Sam");
		}
		String year = array.get(0).length() < 4 ? array.get(2) : array.get(0);
		String month = year.equals(array.get(0)) ? array.get(1) : array.get(0);
		String day = year.equals(array.get(0)) ? array.get(2) : array.get(1);
		if (day.equals("2023")) {
			System.out.println("Sam");
		}
		return LocalDate.of(Integer.valueOf(year), Integer.valueOf(month), Integer.valueOf(day));
	}

	public static LocalDate getDay(int minusDays, LocalDate fromDay) {
		return fromDay.minusDays((long) minusDays);
	}

	private static URI getHistoricURI(String ticker, LocalDate date1, LocalDate date2) {
		String url = "https://api.nasdaq.com/api/quote/" + ticker + "/historical?assetclass=stocks&fromdate="
				+ date1.format(DateTimeFormatter.ofPattern("YYYY-MM-dd")) + "&limit=9999&todate="
				+ date2.format(DateTimeFormatter.ofPattern("YYYY-MM-dd"));
		return URI.create(url);
	}

	private static URI getHistoricURI() {
		return URI.create(
				"https://charting.nasdaq.com/data/charting/historical?symbol=AAPL&date=2021-10-19~2024-03-02&includeLatestIntradayData=1&");
	}

	public static Nasdaq getAllStockData() {
		Nasdaq nasdaq = null;
		try {
			InputStream inputStream = DataRequest.makeConfiguredRequest();// DataRequest.makeRequest();
			System.out.println("Connection Established");
			GZIPInputStream gzipStream = new GZIPInputStream(inputStream);
			nasdaq = new Nasdaq();
			nasdaq.setDataMap(nasdaq.parseNasdaqResponse(gzipStream));
		} catch (Exception e) {
			e.printStackTrace();
			nasdaq = new Nasdaq();
		}
		return nasdaq;
	}

	static class SyncControls {
		boolean isWaiting = false;
		boolean reading = true;
		Semaphore waiting = new Semaphore(0);

		void continueFromWait() {
			if (isWaiting) {
				waiting.release();
			}
		}
	}

	public class DataRequest {
		URI uri;
		InputStream inputStream;
		Semaphore waitForConnection = new Semaphore(0);
		Exception exception = null;

		DataRequest(URI uri) {
			this.uri = uri;
			request();
		}

		DataRequest(String uriString) {
			this.uri = URI.create(uriString);
			request();
		}

		private void request() {
			try {
				instanceRequest();
			} catch (Exception e) {
				exception = e;
				setInputStream(null);
			}
		}

		public InputStream getInputStream() throws Exception {
			boolean acquired = false;
			while (exception == null & !acquired) {
				waitForConnection.acquire();
				acquired = true;
			}
			if (exception != null) {
				throw exception;
			}
			return inputStream;
		}

		private void setInputStream(InputStream inputStream) {
			waitForConnection.release();
			this.inputStream = inputStream;
		}

		void instanceRequest() throws IOException {
			HttpURLConnection.setFollowRedirects(true);
			HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
			connection.setRequestProperty("Accept", "application/json, text/plain, */*");
			connection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
			connection.setRequestProperty("User-Agent", "Mozilla/5.0");
			connection.setRequestMethod("GET");
			connection.connect();

			setInputStream(connection.getInputStream());
		}

		static InputStream makeRequest() throws IOException, InterruptedException {
			URL url = getAllNasdaqURI().toURL();
			HttpURLConnection.setFollowRedirects(true);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("Accept", "application/json, text/plain, */*");
			connection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
			// connection.setRequestProperty("User-Agent", "Mozilla/5.0");
			connection.setRequestMethod("GET");
			try {
				connection.connect();
			} catch (ConnectException e) {
				// disconnectReconnect();
				e.printStackTrace();
				return InputStream.nullInputStream();
			}

			return connection.getInputStream();
		}

		static InputStream makeRequest(URI uri) throws IOException, InterruptedException {
			URL url = uri.toURL();
			HttpURLConnection.setFollowRedirects(true);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("Accept", "*/*");
			connection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
			connection.setRequestProperty("User-Agent", "Mozilla/5.0");
			connection.setRequestMethod("GET");
			try {
				connection.connect();
			} catch (ConnectException e) {
				disconnectReconnect();
				return makeRequest();
			}

			return connection.getInputStream();
		}

		static InputStream makeConfiguredRequest(URI uri) throws IOException, InterruptedException {
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
					.header("Accept", "*/*")
					.header("Accept-Encoding", "gzip, deflate, br")
					.header("User-Agent", "Mozilla/5.0")
					.GET()
					.uri(uri)
					.build();
			HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
			System.out.println(response.statusCode());
			return response.body();
		}

		static InputStream makeConfiguredRequest() throws IOException, InterruptedException {
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().header("Accept", "application/json, text/plain, */*")
					.header("Accept-Encoding", "gzip, defalte, br").GET().uri(getAllNasdaqURI()).build();
			HttpResponse<InputStream> response = null;
			try {
				response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
			} catch (ConnectException e) {
				e.printStackTrace();
				return InputStream.nullInputStream();
			}

			System.out.println("Status Code: " + response.statusCode());
			return response.body();
		}

		private static URI getAllNasdaqURI() {
			return URI.create(
					"https://api.nasdaq.com/api/screener/stocks?tableonly=true&limit=25&offset=0&download=true");
		}

	}

	public static void disconnectReconnect() throws IOException, InterruptedException {
		System.out.println("------------------Reconnecting to Wifi-------------------");
		String wifiName = SystemCommands.getWifiName();
		SystemCommands.disconnectFromInternet();
		Thread.sleep(2500);
		SystemCommands.connectToWifi(wifiName);
	}

	private void waitForData() throws InterruptedException {
		isWaiting = true;
		waiting.acquire();
		isWaiting = false;
	}

	void continueFromWait() {
		if (isWaiting) {
			waiting.release();
		}
	}

	synchronized void addToMap(Map<String, Map<String, String>> map, Map<String, String> objMap) {
		if (objMap == null || objMap.isEmpty()) {
			return;
		}
		map.put(objMap.get(HIGH_LEVEL_KEY_NAME), objMap);
	}

	CompletableFuture<Map<String, Map<String, String>>> parseJSON(LinkedBlockingQueue<String> queue)
			throws IOException, InterruptedException {
		CompletableFuture<Map<String, Map<String, String>>> mapFuture = new CompletableFuture<>();
		executor.execute(() -> {
			Map<String, Map<String, String>> map = new LinkedHashMap<>();
			while (queue.size() > 0 | reading) {
				if (queue.size() == 0) {
					try {
						waitForData();
					} catch (InterruptedException e) {
						continueFromWait();
						e.printStackTrace();
						break;
					}
					continue;
				}
				String obj = queue.poll();
				addToMap(map, parseSimpleObject(obj));
			}
			mapFuture.complete(map);
		});
		return mapFuture;
	}

	final String HIGH_LEVEL_KEY_NAME = "symbol";

	static Map<String, String> parseSimpleObject(String objString) {
		Map<String, String> map = new HashMap<>();
		Matcher matcher = Pattern.compile("\"[^\\{\\}]+?\":\"[^\\{\\}]*?\"").matcher(objString);
		while (matcher.find()) {
			String fieldString = matcher.group();
			map.put(fieldString.split(":")[0].replace("\"", ""), fieldString.split(":")[1].replace("\"", ""));
		}
		return map;
	}

	void deleteFile(String filePath) throws IOException {
		File file = new File(filePath);
		if (file.exists()) {
			file.delete();
		}
	}

	void addObjectsToQueue(InputStream inputStream, LinkedBlockingQueue<String> queue, String key)
			throws IOException, InterruptedException {
		StringBuilder stringBuilder = getStringBuilder(inputStream, key);
		byte[] bytes = new byte[BUFFER_SIZE];
		int i;
		while ((i = inputStream.read(bytes)) > 0) {
			bytes = i == BUFFER_SIZE ? bytes : getNonNullBytes(bytes, i);
			String newString = new String(bytes);
			stringBuilder.append(newString);
			int end = addObjects(stringBuilder.toString(), queue);
			if (end == -1) {
				continue;
			}
			stringBuilder.delete(0, end - 1);
		}
		reading = false;
		continueFromWait();
	}

	int addObjects(String string, LinkedBlockingQueue<String> queue) throws IOException, InterruptedException {
		Matcher matcher = Pattern.compile("\\{(.+?)\\}").matcher(string);
		int end = -1;
		while (matcher.find()) {
			queue.put(matcher.group());
			end = matcher.end();
			continueFromWait();
		}
		return end;
	}

	public final static String DATA_RESPONSE_OBJ_NAME = "rows";

	CompletableFuture<Map<String, Map<String, String>>> parseNasdaqResponse(InputStream inputStream)
			throws IOException, InterruptedException {
		LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
		executor.execute(() -> {
			try {
				addObjectsToQueue(inputStream, queue, DATA_RESPONSE_OBJ_NAME);
			} catch (IOException | InterruptedException e) {
				reading = false;
				continueFromWait();
				e.printStackTrace();
			}
		});
		return parseJSON(queue);

	}

	static void parseDayResponse(InputStream inputStream, LinkedBlockingQueue<String> queue, SyncControls syncControls)
			throws IOException, InterruptedException {
		StringBuilder stringBuilder = getStringBuilder(inputStream, HISTORIC_DATA_OBJ_NAME);
		byte[] bytes = new byte[BUFFER_SIZE];
		int i;
		while ((i = inputStream.read(bytes)) > 0) {
			bytes = getNonNullBytes(bytes, i);
			String readString = new String(bytes);
			stringBuilder.append(readString);
			int lastAdded = addDayData(stringBuilder.toString(), queue, syncControls);
			if (lastAdded > -1) {
				stringBuilder = new StringBuilder(stringBuilder.toString().substring(lastAdded));
			}
		}
		syncControls.reading = false;
		syncControls.continueFromWait();
	}

	private static int addDayData(String dayData, LinkedBlockingQueue<String> queue, SyncControls syncControls)
			throws InterruptedException {
		Matcher matcher = Pattern.compile("\\{.+?\\}").matcher(dayData);
		int end = -1;
		while (matcher.find()) {
			String found = matcher.group();
			queue.put(found.substring(1, found.length() - 1));
			syncControls.continueFromWait();
			end = matcher.end();
		}
		return end;
	}

	public final static String HISTORIC_DATA_OBJ_NAME = "rows";

	static byte[] getNonNullBytes(byte[] byteArray, int length) {
		byte[] bytes = new byte[length];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = byteArray[i];
		}
		return bytes;
	}

	final static int BUFFER_SIZE = 1024;

	static StringBuilder getStringBuilder(InputStream inputStream, String key) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		byte[] bytes = new byte[BUFFER_SIZE];
		int i;
		while ((i = inputStream.read(bytes)) > 0) {
			bytes = getNonNullBytes(bytes, i);
			String readString = new String(bytes);
			stringBuilder.append(readString);
			int startPos = findKeyInString(stringBuilder, key);
			if (startPos > -1) {
				return new StringBuilder(stringBuilder.substring(startPos));
			}
		}
		return null;
	}

	static int findKeyInString(StringBuilder stringBuilder, String key) {
		return stringBuilder.toString().indexOf(key);
	}

	public static enum Historic {
		DATE("date"), CLOSE("close"), VOLUME("volume"), OPEN("open"), HIGH("high"), LOW("low");

		private String fieldName;

		Historic(String fieldName) {
			this.fieldName = fieldName;
		}

		public String getValue() {
			return fieldName;
		}
	}
}
