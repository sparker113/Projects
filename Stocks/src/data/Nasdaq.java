package data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
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
	private boolean parsing = true;
	private String filter;
	ExecutorService executor = Executors.newCachedThreadPool();
	private CompletableFuture<Map<String, Map<String, String>>> dataMapFuture = new CompletableFuture<>();

	Nasdaq() {
		filter = "all";
	}

	Nasdaq(String filter) {
		this.filter = filter;
	}
	void setDataMap(CompletableFuture<Map<String,Map<String,String>>> dataMap) {
		this.dataMapFuture = dataMap;
	}
	public CompletableFuture<Map<String,Map<String,String>>> getDataFuture(){
		return this.dataMapFuture;
	}
	public Map<String,Map<String,String>> getData() throws InterruptedException, ExecutionException{
		return dataMapFuture.get();
	}
	public static Nasdaq getAllStockData() {
		Nasdaq nasdaq = null;
		try{
			InputStream inputStream = DataRequest.makeRequest();
			System.out.println("Connection Established");
			GZIPInputStream gzipStream = new GZIPInputStream(inputStream);
			nasdaq = new Nasdaq();
			nasdaq.setDataMap(nasdaq.parseNasdaqResponse(gzipStream));
		}catch(Exception e) {
			e.printStackTrace();
			nasdaq = new Nasdaq();
		}finally{
			nasdaq.executor.shutdown();
		}
		return nasdaq;
	}
	private class DataRequest{
		URI uri;
		InputStream inputStream;
		Semaphore waitForConnection = new Semaphore(0);
		Exception exception = null;
		DataRequest(URI uri){
			this.uri = uri;
			request();
		}
		DataRequest(String uriString){
			this.uri = URI.create(uriString);
			request();
		}
		private void request(){
			try {
				instanceRequest();
			}catch(Exception e) {
				exception = e;
				setInputStream(null);
			}
		}
		public InputStream getInputStream() throws Exception{
			boolean acquired = false;
			while(exception==null&!acquired) {
				waitForConnection.acquire();
				acquired=true;
			}
			if(exception!=null) {
				throw exception;
			}
			return inputStream;
		}
		private void setInputStream(InputStream inputStream) {
			waitForConnection.release();
			this.inputStream = inputStream;
		}
		void instanceRequest() throws IOException{
			HttpURLConnection.setFollowRedirects(true);
			HttpURLConnection connection = (HttpURLConnection)uri.toURL().openConnection();
			connection.setRequestProperty("Accept","application/json, text/plain, */*");
			connection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
			connection.setRequestProperty("User-Agent", "Mozilla/5.0");
			connection.setRequestMethod("GET");
			connection.connect();
			
			setInputStream(connection.getInputStream());
		}
		static InputStream makeRequest() throws IOException{
			URL url = getAllNasdaqURI().toURL();
			HttpURLConnection.setFollowRedirects(true);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setRequestProperty("Accept","application/json, text/plain, */*");
			connection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
			connection.setRequestProperty("User-Agent", "Mozilla/5.0");
			connection.setRequestMethod("GET");
			connection.connect();
			
			return connection.getInputStream();
		}
		private static URI getAllNasdaqURI() {
			return URI.create("https://api.nasdaq.com/api/screener/stocks?tableonly=true&limit=25&offset=0&download=true");
		}
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
			parsing = false;
			mapFuture.complete(map);
		});
		return mapFuture;
	}

	final String HIGH_LEVEL_KEY_NAME = "symbol";

	Map<String, String> parseSimpleObject(String objString) {
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

	byte[] getNonNullBytes(byte[] byteArray, int length) {
		byte[] bytes = new byte[length];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = byteArray[i];
		}
		return bytes;
	}

	final static int BUFFER_SIZE = 1024;

	StringBuilder getStringBuilder(InputStream inputStream, String key) throws IOException {
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

	int findKeyInString(StringBuilder stringBuilder, String key) {
		return stringBuilder.toString().indexOf(key);
	}

}
