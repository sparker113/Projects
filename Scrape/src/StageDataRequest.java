import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StageDataRequest {

	public StageDataRequest() {
	}

	public static Scanner getData(String att, String wellID, String treatmentID, String startTime, channelData channels,
			ArrayList<String> usedChannels) {
		String rl = new String("https://api.fracpro.ai:4000/api/v1/wells/" + wellID + "/treatments/" + treatmentID
				+ "/flowPaths/0/getDataChart");

		String requestBody = reqBody(wellID, treatmentID, startTime, channels, usedChannels);
		// System.out.println(requestBody);
		HttpClient client2 = HttpClient.newHttpClient();
		HttpRequest req2 = (HttpRequest.newBuilder()
				// .expectContinue(false)
				.header("content-type", "application/json").header("accept", "application/json")
				.header("authorization", att).header("accept-encoding", "gzip, deflate, br")
				.timeout(Duration.ofMillis(10000)).POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.uri(URI.create(rl)).build());
		HttpResponse<InputStream> resp2 = null;
		try {
			resp2 = client2.send(req2, HttpResponse.BodyHandlers.ofInputStream());
		} catch (IOException | InterruptedException e) {

		}
		Scanner scan = new Scanner(resp2.body());
		return scan;
	}

	public static String reqBody(String wellID, String treatmentID, String startTime, channelData channels,
			ArrayList<String> usedChannels) {
		String request = "\"";
		for (String a : usedChannels) {
			Matcher matcher2 = Pattern.compile("[-\s_]+").matcher(a);
			String newA = "";
			if (matcher2.find()) {
				newA = a.replace(matcher2.group(), "").toUpperCase();
			} else {
				newA = a.toUpperCase();
			}
			for (String key : channels.getoCName().keySet()) {
				Matcher matcher = Pattern.compile("[-\s_]+").matcher(key);
				String fixedKey = "";
				if (matcher.find()) {
					fixedKey = key.replace(matcher.group(), "").toUpperCase();
					System.out.print(matcher.group());
					System.out.println(" - " + fixedKey + " - " + key);
				} else {
					fixedKey = key;
				}
				if (fixedKey.contains(newA)) {
					request = request + channels.getoCName().get(key) + "\",\"";
					break;
				}
			}

		}
		System.out.println(request);
		request = String.valueOf(request.subSequence(0, request.length() - 2));
		String requestBody = new String("{\"wellId\":" + wellID + ",\"treatmentId\":" + treatmentID + ",\"items\":["
				+ request + "],\"flowPathType\":0,\"isSeparateRealTimeData\":true,\"baseTreatmentDataTime\":\""
				+ startTime + "\"}");

		return requestBody;
	}

	public static StringBuilder getChannelNames(channelData channels, ArrayList<String> usedChannels) {
		StringBuilder chemBuilder = new StringBuilder();
		for (String a : usedChannels) {
			for (String b : channels.getoCName().keySet()) {
				if (b.toUpperCase().replace(" ", "").contains(a.toUpperCase().replace(" ", ""))
						&& !b.toUpperCase().contains("RATE") & !b.toUpperCase().contains("TOTAL")) {
					chemBuilder.append(b);
					chemBuilder.append("_gal");
					chemBuilder.append("\n");
				}
			}
		}
		return chemBuilder;
	}
}
