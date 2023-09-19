package intelie;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChannelMnemonics {
	private String csrfToken;
	private String cookie;
	private String crewId;
	private String remCookie;
	private TreeMap<String, String> mnemonicMap = new TreeMap<>();

	public ChannelMnemonics(String csrfToken, String cookie, String crewId, String remCookie) {
		this.csrfToken = csrfToken;
		this.cookie = cookie;
		this.crewId = crewId;
		this.remCookie = remCookie;
		setMnemonicMap();
	}

	public Scanner makeRequest() {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().header("accept", "*/*")
				.header("content-type", "application/json; charset=UTF-8").header("live-timezone", "America/Chicago")
				.header("cookie", "remember-me=" + remCookie + ";" + cookie).header("X-CSRF-TOKEN", csrfToken)
				.uri(URI.create("https://propetro.intelie.com/rest/lookup/treater_mnemonics/search?")).GET().build();
		HttpResponse<InputStream> response = null;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		} catch (IOException | InterruptedException e) {
			System.out.println("NONONONONONONONO");
		}
		return new Scanner(response.body());
	}

	public void setMnemonicMap() {
		Scanner scanner = makeRequest();
		scanner.useDelimiter("\n");
		scanner.useLocale(Locale.US);

		while (scanner.hasNext()) {
			String temp = scanner.next();
			Matcher channelMatcher = Pattern.compile("\"key\":\"([\\w|-|\\s]+)\",\"value\":\"([\\w|\\s]+)\"")
					.matcher(temp);
			while (channelMatcher.find()) {
				String matched = channelMatcher.group();
				String channel = matched.split(",")[0].replace("\"", "").split(":")[1];
				String mnemonic = matched.split(",")[1].replace("\"", "").split(":")[1];
				mnemonicMap.put(channel, mnemonic);
			}
		}

	}

	public TreeMap<String, String> getMnemonicMap() {
		return this.mnemonicMap;
	}

	public Set<String> getMnemonicMapKeys() {
		return mnemonicMap.keySet();
	}

	public ArrayList<String> getMnemonicMapValues() {
		ArrayList<String> valueArray = new ArrayList<>();
		mnemonicMap.keySet().iterator().forEachRemaining(new Consumer<String>() {
			@Override
			public void accept(String t) {
				valueArray.add(mnemonicMap.get(t));
			}
		});
		return valueArray;
	}
}
