import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//("{\"wellId\":" + wellID + ",\"treatmentId\":" + TreatmentID + ",\"items\":[\"C19\",\"C1\",\"C26\"],\"flowPathType\":0,\"isSeparateRealTimeData\":true,\"baseTreatmentDataTime\":\"" + StartTime + "\"}"
public class Runn {
	ArrayList<String> data = new ArrayList<>();
	ArrayList<String> chan = new ArrayList<>();
	private String wellID;
	private String TreatmentID;
	private String StartTime;
	private Scanner scan;
	String channelCall;
	private channelData channels;
	private String att;
	Long time;

	// public ArrayList<String> data = new ArrayList<>();
	Runn(String att, String wellID, String TreatmentID, String StartTime, channelData channels, String wellName,
			String operator) throws IOException {
		channelCall = new String("\"");
		this.wellID = wellID;
		this.TreatmentID = TreatmentID;
		this.StartTime = StartTime;
		this.channels = channels;
		this.att = att;
		makeRequest(wellName);
	}

	public void makeRequest(String wellName) {
		LocalDateTime start = LocalDateTime.now();
		String abbrev = wellName.split(" ")[wellName.split(" ").length - 1];
		HashMap<String, String> channelList = new HashMap<>();
		channelList = ChannelPane.getChannelList();

		String wellNum = wellName.split(" ")[wellName.split(" ").length - 1];
		Integer a = channelCall.lastIndexOf(",");
		// channelCall = (String) channelCall.subSequence(0, a);

		String rl = new String("https://api.fracpro.ai:4000/api/v1/wells/" + wellID + "/treatments/" + TreatmentID
				+ "/flowPaths/0/getDataChart");

		// System.out.println(rl);
		String chanJSon = constructChannelsReqBody(channels, wellNum);
		if (chanJSon == null) {
			return;
		}
		String reqBody = new String("{\"wellId\":" + wellID + ",\"treatmentId\":" + TreatmentID + ",\"items\":["
				+ chanJSon + "],\"flowPathType\":0,\"isSeparateRealTimeData\":true,\"baseTreatmentDataTime\":\""
				+ StartTime + "\"}");

		HttpClient client2 = HttpClient.newHttpClient();
		HttpRequest req2 = (HttpRequest.newBuilder()
				// .expectContinue(false)
				.header("content-type", "application/json").header("accept", "application/json")
				.header("authorization", att).header("accept-encoding", "gzip, deflate, br")
				.timeout(Duration.ofMillis(10000)).POST(HttpRequest.BodyPublishers.ofString(reqBody))
				.uri(URI.create(rl)).build());

		HttpResponse<InputStream> resp2 = null;
		try {
			resp2 = client2.send(req2, HttpResponse.BodyHandlers.ofInputStream());
		} catch (IOException | InterruptedException e) {

		}
		this.scan = new Scanner(resp2.body());
		this.time = Duration.between(start, LocalDateTime.now()).toMillis();
		GrantAccess.clearSSLCache(client2);
	}

	Long getTime() {
		return this.time;
	}

	static void removeKeys(Map<String, String> map, String... keys) {
		for (String s : keys) {
			map.remove(s);
		}
	}

	static void removeWhiteSpaceKeys(Map<String, String> map) {
		for (String s : map.keySet()) {
			System.out.println("key: " + s);
			if (s.trim().equals("")) {
				map.remove(s);
			}
		}
	}

	static void removeValues(Map<String, String> map, String... values) {
		for (String s : values) {
			map.values().remove(s);
		}
	}

	public String constructChannelsReqBody(channelData channels, String wellNum) {
		String channelCall = "";
		int count = 0;
		Stack<String> checkStack = new Stack<>();
		HashMap<String, String> channelListMap = getChannelList();
		removeWhiteSpaceKeys(channelListMap);
		removeValues(channelListMap, "null");
		for (String ss : channelListMap.keySet()) {
			for (String s : channels.getOriginalName()) {
				if (ss.equals("Treating Pressure") && s.toUpperCase().contains(channelListMap.get(ss).toUpperCase())) {
					Matcher matcher = Pattern.compile("\\d+").matcher(channelListMap.get(ss));
					Matcher allMatcher = Pattern.compile("\\d+").matcher(s);
					if (matcher.find() & allMatcher.find() && matcher.group().equals(allMatcher.group())) {
						String cNamePres = channels.getoCName().get(s);
						channelCall += ",\"" + cNamePres + "\"";
						checkStack.push(s);
						count++;
						break;
					} else if (!matcher.find() & s.toUpperCase().contains(channelListMap.get(ss).toUpperCase())) {
						String cNamePres = channels.getoCName().get(s);
						channelCall += ",\"" + cNamePres + "\"";
						checkStack.push(s);
						count++;
						break;
					}
				} else if (ss.equals("Backside") && s.toUpperCase().contains(channelListMap.get(ss).toUpperCase())
						&& s.toUpperCase().contains(wellNum.toUpperCase())) {
					String cNamePres = channels.getoCName().get(s);
					channelCall += ",\"" + cNamePres + "\"";
					checkStack.push(s);
					count++;
					break;
				} else if (s.toUpperCase().contains(channelListMap.get(ss).toUpperCase()) && !checkStack.contains(s)) {
					String cName = channels.getoCName().get(s);
					channelCall += ",\"" + cName + "\"";
					checkStack.push(s);
					count++;
					break;
				}
			}
			if (count == channelListMap.size()) {
				break;
			}
		}
		if (count < channelListMap.size()) {
			return null;
		}
		return channelCall.substring(1);
	}

	public HashMap<String, String> getChannelList() {
		return ChannelPane.getChannelList();
	}

	public String getRequestBody() {
		String reqBody1 = new String("{\"wellId\":" + wellID + ",\"treatmentId\":" + TreatmentID + ",\"items\":["
				+ channelCall + "],\"flowPathType\":0,\"isSeparateRealTimeData\":true,\"baseTreatmentDataTime\":\"");
		return reqBody1;
	}

	public ArrayList<String> getData() {
		return this.data;
	}

	public ArrayList<String> getChans() {
		return this.chan;
	}

	public Scanner getScanner() {
		return this.scan;
	}

}
