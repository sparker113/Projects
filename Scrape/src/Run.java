import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

//("{\"wellId\":" + wellID + ",\"treatmentId\":" + TreatmentID + ",\"items\":[\"C19\",\"C1\",\"C26\"],\"flowPathType\":0,\"isSeparateRealTimeData\":true,\"baseTreatmentDataTime\":\"" + StartTime + "\"}"
public class Run {
	ArrayList<String> data = new ArrayList<>();
	ArrayList<String> chan = new ArrayList<>();
	String channelCall;

	// public ArrayList<String> data = new ArrayList<>();
	Run(String att, String wellID, String TreatmentID, String StartTime, channelData channels, String wellName,
			String operator) {
		channelCall = new String("\"");
		String abbrev;
		abbrev = wellName.split(" ")[wellName.split(" ").length - 1];
		for (String a : channels.getOriginalName()) {
			if (a.toUpperCase().contains("NUMBER") || a.toUpperCase().contains("STEP")
					|| a.toUpperCase().contains("BLENDER STAGE")) {
				channelCall = channelCall + channels.getoCName().get(a) + "\",\"";
			} else if (a.toUpperCase().contains("CLEAN") & a.toUpperCase().contains("RATE")) {
				channelCall = channelCall + channels.getoCName().get(a) + "\",\"";
			} else if (a.toUpperCase().contains("SLURRY") & a.toUpperCase().contains("RATE")) {
				channelCall = channelCall + channels.getoCName().get(a) + "\",\"";
			} else if (a.toUpperCase().contains("CLEAN") & a.toUpperCase().contains("TOTAL")) {
				if (a.toUpperCase().contains("STAGE") || a.toUpperCase().contains("CALC")) {
				} else {
					channelCall = channelCall + channels.getoCName().get(a) + "\",\"";
				}
			} else if (a.toUpperCase().contains("SLURRY") & a.toUpperCase().contains("TOTAL")) {
				if (a.toUpperCase().contains("STAGE")) {
				} else {
					channelCall = channelCall + channels.getoCName().get(a) + "\",\"";
				}
			} else if (a.toUpperCase().contains("WELLSIDE") || a.toUpperCase().contains("PRESSURE 1 ")) {
				channelCall = channelCall + channels.getoCName().get(a) + "\",\"";
			} else if (a.toUpperCase().contains("INT") & a.toUpperCase().contains(abbrev)
					|| a.toUpperCase().contains("BACKSIDE") & a.toUpperCase().contains(abbrev)) {
				channelCall = channelCall + channels.getoCName().get(a) + "\",\"";
			} else if (operator.toUpperCase().contentEquals("PIONEER NATURAL RESOURCES")
					& a.toUpperCase().contains("TN DENSITY")) {
				channelCall = channelCall + channels.getoCName().get(a) + "\",\"";
			}
		}
		Integer a = channelCall.lastIndexOf(",");
		channelCall = (String) channelCall.subSequence(0, a);
		System.out.println(channelCall);
		// String att = new String();
		String rl = new String("https://api.fracpro.ai:4000/api/v1/wells/" + wellID + "/treatments/" + TreatmentID
				+ "/flowPaths/0/getDataChart");
		// String rl = new
		// String("https://api.fracpro.ai:4000/api/v1/wells/3308/treatments/102341/flowPaths/0/getDataChart");
		System.out.println(rl);
		String reqBody = new String("{\"wellId\":" + wellID + ",\"treatmentId\":" + TreatmentID + ",\"items\":["
				+ channelCall + "],\"flowPathType\":0,\"isSeparateRealTimeData\":true,\"baseTreatmentDataTime\":\""
				+ StartTime + "\"}");
		// att = at.grantAccess();
		System.out.println(reqBody);
		while (att == "") {
		}
		// System.out.println(att);
		HttpClient client2 = HttpClient.newHttpClient();
		HttpRequest req2 = (HttpRequest.newBuilder().header("Content-type", "application/json; charset=utf-8")
				.header("accepet", "application/json").header("authorization", att)
				.POST(HttpRequest.BodyPublishers.ofString(reqBody)).uri(URI.create(rl)).build());
		HttpResponse<InputStream> resp2 = null;
		try {
			resp2 = client2.send(req2, HttpResponse.BodyHandlers.ofInputStream());
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Scanner someShit = new Scanner(resp2.body());
		// ArrayList<String> data = new ArrayList<>();
		String superLolz = new String();
		someShit.useDelimiter("\\[");
		int iii = 0;
		while (someShit.hasNext()) {
			superLolz = someShit.next();
			if (iii == 3) {
				for (String lolz : superLolz.split(",")) {
					if (lolz == superLolz.split(",")[superLolz.split(",").length - 2]) {

					} else if (lolz == superLolz.split(",")[superLolz.split(",").length - 3]) {

						this.chan.add(lolz.toString().replace("\\]", ""));
					} else {
						this.chan.add(lolz.replace("\"", ""));
					}
				}
			} else if (iii > 3) {
				try {
					this.data.add(superLolz);
				} catch (NoSuchElementException e) {
					System.out.println("BITCH");
				}
			}

			iii++;
		}
		System.out.println("FUCKKKKKKKK");
		// chan.forEach(System.out::println);
		this.chan.remove(this.chan.size() - 1);

		for (String hmmm : chan) {
			hmmm.replace("\\]", "");
		}
		// chan.forEach(System.out::println);
		// data.forEach(System.out::println);
		// that2.close();
	}

	public ArrayList<String> getData() {
		return this.data;
	}

	public ArrayList<String> getChans() {
		return this.chan;
	}

}