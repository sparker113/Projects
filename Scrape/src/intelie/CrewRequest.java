package intelie;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

import login.EncryptCredentials;

public class CrewRequest {
	private String csrfToken;
	private String sessionId;
	private HashMap<String, String> crewMap = new HashMap<>();
	private HashMap<String, String> normCrewMap = new HashMap<>();
	private RememberMe rememberMe;
	private Semaphore semaphore = new Semaphore(0);
	private boolean normSet = false;
	private int count = 0;

	public CrewRequest() {
		try {
			this.rememberMe = RememberMe.readCookie();
		} catch (InterruptedException | IOException | ClassNotFoundException e) {
			System.out.println("Exception CrewRequest");
			try {
				HashMap<String, String> map = EncryptCredentials.getUserCredentials();
				this.rememberMe = new RememberMe(map.get("username"), map.get("password"));
			} catch (IOException | InterruptedException | InvalidKeyException | ClassNotFoundException
					| NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
					| BadPaddingException | ShortBufferException e1) {
				try {
					RememberMe.deleteCookie();
				} catch (IOException e2) {
					System.out.println("Failed to delete cookie.scp");
					return;
				}
			}
		}
		if (rememberMe.getCookie().equals("AUTH=FAILED")) {
			return;
		}
		setCrewMap();
		try {
			setNormCrewMap();
			System.out.println(getNormMap());
		} catch (IOException | InterruptedException e2) {
			e2.printStackTrace();
		}
	}

	public CrewRequest(boolean noRememberMe) {

	}

	public static String removeSpecialCharacters(String string) {
		Matcher matcher = Pattern.compile("[\\-\\{}\\(\\)\\!\\?\\,\\&\\%\\*\\$\\#]|(\\s\\s)").matcher(string);
		String newString = string;
		while (matcher.find()) {
			if (matcher.group().matches("\\s\\s")) {
				newString = newString.replace(matcher.group(), " ");
				matcher.reset(newString);
				continue;
			}
			newString = newString.replace(matcher.group(), "");
			matcher.reset(newString);
		}
		return newString.toLowerCase();
	}

	public void setNormCrewMap() throws IOException, InterruptedException {
		ExecutorService executor = Executors.newCachedThreadPool();
		for (String s : crewMap.keySet()) {
			executor.execute(() -> {
				try {
					addToNormMap(removeSpecialCharacters(s), makeNormRequest(crewMap.get(s)));
					semaphore.release();
				} catch (IOException | InterruptedException e) {
					System.out.println("NONONONON");
					semaphore.release();
				}
			});
		}
	}

	public synchronized String getNormCrew(String s) throws InterruptedException {
		if (!normSet) {
			semaphore.acquire(crewMap.size());
			normSet = true;
		}
		return normCrewMap.get(removeSpecialCharacters(s));
	}

	public HashMap<String, String> getNormMap() throws InterruptedException {
		if (!normSet) {
			semaphore.acquire(crewMap.size());
			normSet = true;
		}
		return normCrewMap;
	}

	public synchronized void addToNormMap(String key, String value) {
		normCrewMap.put(key, value);
	}

	private String unZipGzip(GZIPInputStream inputStream) throws IOException {
		byte[] bytes = new byte[1024];
		String response = "";
		Matcher matcher = Pattern.compile("\"event_type\"\\:\"(\\w+)\"").matcher(response);
		while (!matcher.find() & inputStream.read(bytes) > -1) {
			bytes = removeEmptyBytes(bytes);
			response += getStringFromBytes(bytes);
			matcher.reset(response);
		}
		try {
			String normName = matcher.group();
			return normName.split(":")[1].replace("\"", "");
		} catch (IllegalStateException e) {
			return response;
		}
	}

	private byte[] removeEmptyBytes(byte[] bytes) {
		int count = 0;
		for (byte b : bytes) {
			if (b == 0) {
				count++;
				break;
			}
			count++;
		}
		byte[] newBytes = new byte[count];
		for (int i = 0; i < count; i++) {
			newBytes[i] = bytes[i];
		}
		return newBytes;
	}

	private String getStringFromBytes(byte[] bytes) {
		StringBuilder builder = new StringBuilder();
		for (byte b : bytes) {
			builder.append((char) b);
		}
		return builder.toString();
	}

	public String makeNormRequest(String crewID) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().header("accept", "application/json")
				.header("content-type", "application/json;charset=utf-8").header("accept-encoding", "gzip, deflate, br")
				.header("cookie", "remember-me=" + rememberMe.getCookie() + "; " + getSessionId())
				.header("X-CSRF-TOKEN", getToken())
				.uri(URI.create(
						"https://propetro.intelie.com/services/plugin-liverig/assets/crew/" + crewID + "/normalizer"))
				.GET().build();
		HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		return unZipGzip(new GZIPInputStream(response.body()));
	}

	private String parseNormResponse(String response) {
		Matcher matcher = Pattern.compile("\"event_type\"\\:\"(\\w+)\"").matcher(response);
		if (matcher.find()) {
			return matcher.group().split(":")[1].replace("\"", "");
		}
		return response;
	}

	public HashMap<String, String> getCrewMap() {
		System.out.println(this.crewMap);
		return this.crewMap;
	}

	private ArrayList<ArrayList<Integer>> getSearchIndeces(Matcher matcher) {
		ArrayList<Integer> startIndeces = new ArrayList<>();
		ArrayList<Integer> endIndeces = new ArrayList<>();
		while (matcher.find()) {
			startIndeces.add(matcher.start());
			endIndeces.add(matcher.end());
		}
		ArrayList<ArrayList<Integer>> searchIndecesArray = new ArrayList<>();
		searchIndecesArray.add(startIndeces);
		searchIndecesArray.add(endIndeces);
		return searchIndecesArray;
	}

	private void setCrewMap() {
		sessionIDRequest();
		String json = null;
		try {
			json = makeRequest();
		} catch (IOException | InterruptedException e) {
			System.out.println("Exception caught intelie::CrewRequest::setCrewMap");
		}
		ArrayList<ArrayList<Integer>> idNameIndexArray = getSearchIndeces(
				Pattern.compile("\"id\":\"(\\d+)\",\"name\\\":\"([[a-zA-Z\\-]|\\d]+)\"").matcher(json));
		for (int i = 0; i < idNameIndexArray.get(0).size(); i++) {
			String name = json.substring(idNameIndexArray.get(0).get(i), idNameIndexArray.get(1).get(i)).split(":")[2]
					.replace("\"", "");
			String id = json.substring(idNameIndexArray.get(0).get(i), idNameIndexArray.get(1).get(i)).split(":")[1]
					.split(",")[0].replace("\"", "");
			crewMap.put(name, id);
		}
	}

	private String makeRequest() throws IOException, InterruptedException {

		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().header("accept", "application/json")
				.header("content-type", "application/json; charset=UTF-8")
				.header("cookie", "remember-me=" + rememberMe.getCookie() + ";" + getSessionId())
				.header("X-CSRF-TOKEN", getToken())
				.uri(URI.create("https://propetro.intelie.com/services/plugin-liverig/assets/crew/search?"))
				.POST(HttpRequest.BodyPublishers.ofString(
						"{\"terms\":[[\"\"]],\"filters\":{\"list.active.filter_crew\":[\"ACTIVE\",\"IDLE\"]}}"))
				.build();

		HttpResponse<String> response = null;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (InterruptedException | IOException e) {
			System.out.println("Exception caught intelie::CrewRequest::makeRequest");
		}
		if (response.statusCode() != 200 && getRequestCount() < 2) {
			HashMap<String, String> map = null;
			try {
				map = EncryptCredentials.getUserCredentials();
			} catch (InvalidKeyException | ClassNotFoundException | NoSuchAlgorithmException | NoSuchPaddingException
					| IllegalBlockSizeException | BadPaddingException | ShortBufferException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			rememberMe = new RememberMe(map.get("username"), map.get("password"));
			makeRequest();
		}else if(response.statusCode() == 401 && getRequestCount() < 3) {
			try {
				EncryptCredentials.updateUserCredentials(INTELIE_CRED_PATH,"Intelie Login");
			} catch (InvalidKeyException | ClassNotFoundException | NoSuchAlgorithmException | NoSuchPaddingException
					| IllegalBlockSizeException | BadPaddingException | IOException e) {
				e.printStackTrace();
				return null;
			}
			makeRequest();
		}
		return response.body();
	}
	public final static String INTELIE_CRED_PATH = "intelie\\";
	private int getRequestCount() {
		return count++;
	}

	public String getCookie() {
		return this.rememberMe.getCookie();
	}

	private void sessionIDRequest() {
		System.out.println(rememberMe.getCookie());
		HttpClient client = HttpClient.newBuilder().cookieHandler(getCookieHandler()).build();
		HttpRequest request = HttpRequest.newBuilder().header("accept", "application/json")
				.header("content-type", "application/json; charset=UTF-8")
				.header("cookie", "remember-me=" + rememberMe.getCookie())
				.uri(URI.create("https://propetro.intelie.com/")).GET().build();
		HttpResponse<InputStream> response = null;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		} catch (IOException | InterruptedException e) {
			System.out.println("NOOOOOOO");
		}
		Scanner scanner = new Scanner(response.body());
		try {
			setSessionId(
					client.cookieHandler().get().get(response.uri(), request.headers().map()).get("Cookie").get(0));
			setToken(getCSRFToken(scanner));
		} catch (IOException e) {
			System.out.println("Exception caught intelie::CrewRequest::sessionIdRequest");
		}
	}

	public void sessionIDRequest(boolean noRememberMe) {

		HttpClient client = HttpClient.newBuilder().cookieHandler(getCookieHandler()).build();
		HttpRequest request = HttpRequest.newBuilder().header("accept", "application/json")
				.header("content-type", "application/json; charset=UTF-8").header("cookie", "remember-me=")
				.uri(URI.create("https://propetro.intelie.com/")).GET().build();
		HttpResponse<InputStream> response = null;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		} catch (IOException | InterruptedException e) {
			System.out.println("NOOOOOOO");
		}
		try {
			setSessionId(
					client.cookieHandler().get().get(response.uri(), request.headers().map()).get("Cookie").get(0));
		} catch (IOException e) {
			System.out.println("Exception caught intelie::CrewRequest::sessionIdRequest");
		}
		tokenRequest(getSessionId());
	}

	public void tokenRequest(String sessionId) {
		HttpClient client = HttpClient.newBuilder().cookieHandler(getCookieHandler()).build();
		HttpRequest request = HttpRequest.newBuilder().header("accept", "application/json")
				.header("content-type", "application/json; charset=UTF-8").header("cookie", sessionId)
				.uri(URI.create("https://propetro.intelie.com/login")).GET().build();
		HttpResponse<InputStream> response = null;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		} catch (IOException | InterruptedException e) {
			System.out.println("NOOOOOOO");
		}
		Scanner scanner = new Scanner(response.body());
		setToken(getCSRFToken(scanner));
	}

	private String getCSRFToken(Scanner scanner) {
		boolean getNext = false;
		while (scanner.hasNext()) {
			String temp = scanner.next();
			if (getNext) {
				return temp.split("=")[1].replace("\"", "");
			}
			if (temp.contains("name=\"_csrf\"")) {
				getNext = true;
			}
		}
		return "";
	}

	private void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getSessionId() {
		return this.sessionId;
	}

	private void setToken(String csrfToken) {
		this.csrfToken = csrfToken;
	}

	public String getToken() {
		return this.csrfToken;
	}

	public static CookieHandler getCookieHandler() {
		CookieManager cookieHandler = new CookieManager();
		CookieHandler.setDefault(cookieHandler);
		CookieHandler cookieHandle = CookieHandler.getDefault();
		return cookieHandle;
	}
}
