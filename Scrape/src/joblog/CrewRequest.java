package joblog;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Scanner;

public class CrewRequest {
	String token;
	HashMap<String, String> crewMap;

	public CrewRequest(String token) {
		this.token = token;
		parseJSon(makeRequest());
	}

	private void parseJSon(Scanner scanner) {
		if (scanner == null) {
			return;
		}
		HashMap<String, String> crewMap = new HashMap<>();
		scanner.useDelimiter("\\}");
		while (scanner.hasNext()) {
			String crew = "";
			String id = "";
			for (String s : scanner.next().split(",")) {
				if (s.contains("id")) {
					id = s.split(":")[1];
				} else if (s.contains("name")) {
					crew = s.split(":")[1].replace("\"", "");
				}
			}
			crewMap.put(id, crew);
		}
		setCrewMap(crewMap);
	}

	private void setCrewMap(HashMap<String, String> crewMap) {
		this.crewMap = crewMap;
	}

	public HashMap<String, String> getCrewMap() {
		return this.crewMap;
	}

	private Scanner makeRequest() {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().header("content-type", "application/json;charset=UTF-8")
				.header("x-access-token", token).uri(URI.create("https://propetro.petroiq.com/crews/")).GET().build();
		HttpResponse<InputStream> response = null;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		} catch (InterruptedException | IOException e) {
			System.out.println("Exception caught sending request for crews");
		}
		if (response == null) {
			return null;
		}
		Scanner scanner = new Scanner(response.body());
		return scanner;
	}
}
