package joblog;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CountiesRequest extends HashMap<String, String> implements Runnable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String token;

	public CountiesRequest(String token) {
		this.token = token;
	}

	private String makeRequest() throws InterruptedException, IOException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().header("accept", "*/*")
				.header("content-type", "application/json; charset=UTF-8").header("x-access-token", token)
				.uri(URI.create("https://propetro.petroiq.com/counties/")).GET().build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		return response.body();
	}

	private static String makeRequest(String token) throws InterruptedException, IOException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().header("accept", "*/*")
				.header("content-type", "application/json; charset=UTF-8").header("x-access-token", token)
				.uri(URI.create("https://propetro.petroiq.com/counties/")).GET().build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		return response.body();
	}

	public static HashMap<String, String> getCountiesMap(String token) {
		String response = "";
		try {
			response = makeRequest(token);
		} catch (IOException | InterruptedException e) {
			System.out.println("Exception caught CountiesRequest::makeRequest");
		}
		HashMap<String, String> countiesMap = new HashMap<>();
		Matcher matcher = Pattern.compile("\"id\":((\\d|\\s)+),\"name\":((\"|\\s|\\w)+)").matcher(response);
		while (matcher.find()) {
			String match = matcher.group();
			countiesMap.put(match.split(",")[0].split(":")[1], match.split(",")[1].split(":")[1].replace("\"", ""));
		}
		return countiesMap;
	}

	private void parseAndAppendToMap(String response) {
		Matcher matcher = Pattern.compile("\"id\":((\\d|\\s)+),\"name\":((\"|\\s|\\w)+)").matcher(response);
		while (matcher.find()) {
			String match = matcher.group();
			this.put(match.split(",")[0].split(":")[1], match.split(",")[1].split(":")[1].replace("\"", ""));
		}
	}

	public void setCounties() throws InterruptedException, IOException {
		parseAndAppendToMap(makeRequest());
	}

	@Override
	public void run() {
		try {
			setCounties();
		} catch (IOException | InterruptedException e) {
			System.out.println("Exception caught CountiesRequest::setCounties");
		}
	}
}
