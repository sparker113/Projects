package joblog;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WellsRequest {

	public static Scanner makeRequest(String token) {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().header("accept", "application/json")
				.header("content-type", "application/json; charset=utf-8").header("x-access-token", token)
				.uri(URI.create("https://propetro.petroiq.com/wells")).GET().build();
		HttpResponse<InputStream> response = null;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		} catch (IOException | InterruptedException e) {
			System.out.println("WELLREQUEST NONOOONONONOOO");
		}
		Scanner scanner = new Scanner(response.body());
		return scanner;
	}

	public static InputStream makeRequestForStream(String token) {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().header("accept", "application/json")
				.header("content-type", "application/json; charset=utf-8").header("x-access-token", token)
				.uri(URI.create("https://propetro.petroiq.com/wells")).GET().build();
		HttpResponse<InputStream> response = null;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		} catch (IOException | InterruptedException e) {
			System.out.println("WELLREQUEST NONOOONONONOOO");
		}

		return response.body();
	}

	public static Scanner makePerfsRequest(String token, String id) {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().header("accept", "application/json")
				.header("content-type", "application/json; charset=utf-8").header("x-access-token", token)
				.uri(URI.create("https://propetro.petroiq.com/wells/" + id)).GET().build();
		HttpResponse<InputStream> response = null;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		} catch (IOException | InterruptedException e) {
			System.out.println("WELLREQUEST NONOOONONONOOO");
		}
		Scanner scanner = new Scanner(response.body());
		return scanner;
	}

	public static void setActivePerfsMap(Scanner scanner, String id, JobLogWells jobLogWells) {
		while (scanner.hasNext()) {
			String temp = scanner.next();
			ArrayList<ArrayList<Integer>> stageIndeces = getSearchIndeces(
					Pattern.compile("\"stage\":(\\d+)").matcher(temp));
			ArrayList<ArrayList<Integer>> topIndeces = getSearchIndeces(
					Pattern.compile("\"topMeasuredDepth\":(\\d+)").matcher(temp));
			ArrayList<ArrayList<Integer>> bottomIndeces = getSearchIndeces(
					Pattern.compile("\"bottomMeasuredDepth\":(\\d+)").matcher(temp));
			jobLogWells.addToActivePerfsMap(stageIndeces, topIndeces, bottomIndeces, id, temp);
		}
	}

	private static ArrayList<ArrayList<Integer>> getSearchIndeces(Matcher matcher) {
		ArrayList<Integer> startIndeces = new ArrayList<>();
		ArrayList<Integer> endIndeces = new ArrayList<>();
		while (matcher.find()) {
			startIndeces.add(matcher.start());
			endIndeces.add(matcher.end());
		}
		ArrayList<ArrayList<Integer>> startEndIndeces = new ArrayList<>();
		startEndIndeces.add(0, startIndeces);
		startEndIndeces.add(1, endIndeces);
		return startEndIndeces;
	}

	public static JobLogWells setWellMap(Scanner scanner) {
		JobLogWells jobLogWells = new JobLogWells();

		scanner.useDelimiter("\\]\\}\\}");
		while (scanner.hasNext()) {
			String temp = scanner.next();
			System.out.println(temp);
			if (temp.length() < 5) {
				continue;
			}
			ArrayList<Integer> beginObject = new ArrayList<>();
			ArrayList<Integer> endObject = new ArrayList<>();
			Matcher matcher = Pattern.compile(",\"(\\w+)\":\\{").matcher(temp);
			while (matcher.find()) {
				beginObject.add(matcher.start());
				endObject.add(matcher.end());
			}
			Matcher crewMatcher = Pattern.compile("\"crews\":\\[\\{(.*?)\\}\\]").matcher(temp);
			String id = temp.split(",")[1].split(":")[1];
			jobLogWells.addToWellMap(id, temp.substring(0, beginObject.get(0)));
			jobLogWells.addToPadMap(id, temp.substring(endObject.get(0), beginObject.get(1)));
			jobLogWells.addToOperatorMap(id, temp.substring(endObject.get(1), beginObject.get(2)));
			if (crewMatcher.find()) {
				jobLogWells.addToCrewMap(id, crewMatcher.group());
			}
		}
		return jobLogWells;
	}

	public static void addToJobLogWells(JobLogWells jobLogWells, String wellResponse) {
		ArrayList<Integer> beginObject = new ArrayList<>();
		ArrayList<Integer> endObject = new ArrayList<>();
		Matcher matcher = Pattern.compile(",\"(\\w+)\":\\{").matcher(wellResponse);
		int start = 0;
		if (matcher.find()) {
			start = matcher.start();
		}

		Matcher crewMatcher = Pattern.compile("\"crews\":\\[\\{(.+?)\\}\\]").matcher(wellResponse);
		String id = "";
		try {
			id = wellResponse.split(",")[0].split(":")[1];
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println(wellResponse);
			System.out.println("WellsRequest::addToJobLogWells");
			return;
		}

		jobLogWells.addToWellMap(id, wellResponse.substring(0, start));
		String padString = getResponseString(wellResponse, "Pad");

		jobLogWells.addToPadMap(id, padString.substring(0, getStartPositionOfString(padString, "\"Customer")));
		jobLogWells.addToOperatorMap(id, getResponseString(wellResponse, "Customer"));
		jobLogWells.addToCrewMap(id, crewMatcher.find()?crewMatcher.group():getResponseString(wellResponse, "crews"));
	}

	public static int getStartPositionOfString(String string, String find) {
		Matcher matcher = Pattern.compile(find).matcher(string);
		if (matcher.find()) {
			return matcher.start();
		}
		return string.length();
	}

	public static int findClosingBracket(String string) {
		if (!string.matches("(.*?)\\{(.*?)\\}(.*?)")) {
			return string.length();
		}
		int open = -1;
		int count = 0;
		for (char c : string.toCharArray()) {
			if (c == '{') {
				open = open == -1 ? 1 : open++;
				count++;
				continue;
			}
			if (c == '}') {
				open--;
			}
			if (open == 0) {
				return count + 1;
			}
			count++;
		}
		return string.length();
	}

	public static int findClosingBracket(String string, int start,char closeBracket) {
		char openBracket = closeBracket==']'?'[':'{';
		if (!string.contains(String.valueOf(openBracket))) {
			return -1;
		}
		int open = -1;
		int count = 0;
		for (char c : string.toCharArray()) {
			if (c == openBracket) {
				open = open == -1 ? 1 : open++;
				count++;
				continue;
			}
			if (c == closeBracket) {
				open--;
			}
			if (open == 0) {
				return count + 1;
			}
			count++;
		}
		return -1;
	}

	public static int findFirstInstance(String string, char find) {
		int i = 0;
		for (char c : string.toCharArray()) {
			if (c == find) {
				return i;
			}
			i++;
		}
		return i;
	}

	public static int findFirstInstance(String string, char find, int start) {

		for (int i = start; i < string.length(); i++) {
			if (string.charAt(i) == find) {
				return i;
			}

		}
		return string.length();
	}

	public static String getResponseString(String wellResponse, String objectName) {
		// Matcher matcher =
		// Pattern.compile("\""+objectName+"\":(\\[?)\\{(.+?)\\}(\\]?)").matcher(wellResponse);
		Matcher matcher = Pattern.compile("\"" + objectName + "\":([\\[\\{])").matcher(wellResponse);
		if (!matcher.find()) {
			return getObjectValue(wellResponse, objectName);
		}
		char bracket = wellResponse.charAt(matcher.end()-1);
		int lastInd = findClosingBracket(wellResponse, matcher.end() - 1,bracket);
		lastInd = lastInd == -1 ? findFirstInstance(wellResponse, '}', matcher.end() - 1) : lastInd;
		// if(matcher.find()){
		String found = wellResponse.substring(matcher.start(), lastInd);// matcher.group();
		Matcher matcher2 = Pattern.compile("\"" + objectName + "\":(\\[?)\\{").matcher(found);
		return found.substring(matcher2.find() ? matcher2.end() : 0);
		// }
		// return getObjectValue(wellResponse,objectName);
	}

	public static String getObjectValue(String wellResponse, String key) {
		Matcher matcher = Pattern.compile("\"" + key + "\":(\"?)(.+?)\\b").matcher(wellResponse);
		if (matcher.find()) {
			return matcher.group();
		}
		return key + ":null";
	}

	public static byte[] getNewBytes(int i, byte[] bytes) {
		bytes = i == bytes.length ? bytes : new byte[bytes.length];
		return bytes;
	}

	public static JobLogWells setWellMap(String token) throws IOException {
		JobLogWells jobLogWells = new JobLogWells();
		int length = 1024;

		int i = 0;
		byte[] bytes = new byte[length];
		String wellResponse = "";
		InputStream inputStream = makeRequestForStream(token);
		while ((i = inputStream.read(bytes)) > -1) {

			wellResponse += getStringFromBytes(bytes);
			bytes = new byte[length];

			Matcher matcher = Pattern
					.compile("(\"id\":(\\d+),\"name\":\"([^\"]+)\",\"padId\"(.*?)\"crews\":\\[\\{(.*?)\\})+?\\}\\]")
					.matcher(wellResponse);
			while (matcher.find()) {
				String found = matcher.group();
				addToJobLogWells(jobLogWells, found);
				wellResponse = wellResponse.substring(matcher.end());
				matcher.reset(wellResponse);
			}
		}

		System.out.println("InputStream closed - " + i);
		return jobLogWells;
	}

	@Deprecated
	public static JobLogWells setWellMap(InputStream inputStream) throws IOException {
		JobLogWells jobLogWells = new JobLogWells();
		int i = 0;
		byte[] bytes = new byte[1024];
		String wellResponse = "";
		// int idNum = 1;
		while ((i = inputStream.read(bytes)) > -1) {
			wellResponse += getStringFromBytes(bytes);
			bytes = new byte[1024];
			Matcher matcher = Pattern.compile("(\"id\":(\\d+),\"name\":\"([^\"]+)\",\"padId\""
					+ "(.*?)((\"Pad\":\\{(.*?)\\})*?)(.*?)((\"Customer\":\\{(.*?)\\})*?)(.*?)\"crews\":\\[\\{(.*?)\\})+?\\}")
					.matcher(wellResponse);
			while (matcher.find()) {
				String found = matcher.group();
				addToJobLogWells(jobLogWells, found);
				wellResponse = wellResponse.substring(matcher.end());
				matcher.reset(wellResponse);
			}
		}
		return jobLogWells;
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

	public static String formationRequest(String token) throws InterruptedException, IOException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().header("accept", "application/json")
				.header("content-type", "application/json; charset=UTF-8").header("authorization", token)
				.uri(URI.create("https://propetro.petroiq.com/formations")).GET().build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		return response.body();
	}

	public static String singleWellRequest(String token, String wellId) throws InterruptedException, IOException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().header("accept", "application/json")
				.header("content-type", "application/json; charset=UTF-8").header("authorization", token)
				.uri(URI.create("https://propetro.petroiq.com/wells/" + wellId)).GET().build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		return response.body();
	}

	public static Scanner customersRequest(String token) {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().header("accept", "application/json")
				.header("content-type", "application/json; charset=UTF-8").header("authorization", token)
				.uri(URI.create("https://propetro.petroiq.com/customers/")).GET().build();
		HttpResponse<InputStream> response = null;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		} catch (IOException | InterruptedException e) {
			System.out.println("Bad Request");
		}
		Scanner scanner = new Scanner(response.body());
		return scanner;
	}

	public static HashMap<String, String> getCustomersMap(Scanner scanner) {
		scanner.useDelimiter("\\}");
		HashMap<String, String> customersMap = new HashMap<>();
		while (scanner.hasNext()) {
			String temp = scanner.next();
			String id = "";
			String name = "";
			for (String s : temp.split(",")) {
				if (s.contains("id")) {
					id = s.split(":")[1];
				} else if (s.contains("name")) {
					name = s.split(":")[1].replace("\"", "");
				}
			}
			customersMap.put(id, name);
		}
		return customersMap;
	}

	public static void printScanner(ArrayList<String> scanner) throws IOException {
		FileWriter fileWriter = new FileWriter("C:\\Scrape\\wells.txt");
		fileWriter.write("");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		for (String s : scanner) {
			bufferedWriter.append(s);
			bufferedWriter.newLine();
		}
		bufferedWriter.flush();
		bufferedWriter.close();
	}
}
