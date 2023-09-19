import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Diagnostics {

	private HashMap<String, Integer> diagnostics = new HashMap<>();

	public Diagnostics(String tID, String token) {
		String b;
		ArrayList<String> theKey = new ArrayList<>();
		ArrayList<Integer> theValue = new ArrayList<>();
		int i = 0;
		String url = "https://api.fracpro.ai:4000/api/v1/wells/treatments/getDataForPivotPoints";
		Scanner a = null;
		String reqBody = "{\"treatmentIds\":[" + tID + "],\"paramNames\":[\"Equalize\",\"WellOpenTime\",\"StartStage\""
				+ ",\"BallSeatTime\",\"BreakDownTime\",\"AcidStart\",\"AcidEnd\",\"DesignRate\",\"DiverterStart\","
				+ "\"DiverterEnd\",\"EndStage\",\"InitialShutinPresTime\",\"WellClose\",\"MinMax\"]}";

		HttpClient obj = HttpClient.newHttpClient();
		HttpRequest req = (HttpRequest.newBuilder().header("content-type", "application/json"))
				.header("accepet", "application/json").header("Authorization", token).uri(URI.create(url))
				.POST(HttpRequest.BodyPublishers.ofString(reqBody)).build();

		try {
			HttpResponse<String> resp = obj.send(req, HttpResponse.BodyHandlers.ofString());
			a = new Scanner(resp.body());
		} catch (IOException e) {
			try {
				new TextLog("Diagnostics - line 38");
			} catch (IOException e1) {

			}
		} catch (InterruptedException e) {
			try {
				new TextLog("Diagnostics - line 44");
			} catch (IOException e1) {

			}
		}
		a.useDelimiter(",");
		while (a.hasNext()) {
			b = a.next();
			if (b.contains("paramName")) {
				theKey.add(b.split(":")[1].replace("\"", ""));
			} else if (b.contains("paramValue")) {
				theValue.add(Integer.valueOf(b.split(":")[1]));
			}
		}
		ArrayList<String> diagnosticsList = new ArrayList<>();
		for (String c : theKey) {
			diagnostics.put(c, theValue.get(i));
			diagnosticsList.add(c + " - " + theValue.get(i));
			i++;
		}
		ArgumentsToText diagnosticsToText = new ArgumentsToText(diagnosticsList, "C:\\Scrape\\diagnostics.txt", "\n");
		setDiagnostics(diagnostics);
	}

	Diagnostics() {

	}

	public void setDiagnostics(HashMap<String, Integer> diagnostics) {
		this.diagnostics = diagnostics;
	}

	public HashMap<String, Integer> getDiagnostics() {
		return this.diagnostics;
	}
}
