import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Scanner;

public class LiveWellList implements Runnable {
	private Boolean setMap;
	private String at;
	private Thread t;
	private HashMap<String, HashMap<String, String>> wellMap;

	LiveWellList(String at) {
		this.at = at;
		this.setMap = false;
		this.t = new Thread(this, "Live_Well_List_Thread");
		this.t.start();
	}

	public synchronized void wellRequest(String at) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().header("content-type", "application/json")
				.header("accept", "application/json").header("authorization", at)
				.uri(URI.create("https://api.fracpro.ai:4000/api/v1/wells/treatments/inProgressData")).build();
		HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		Scanner scan = new Scanner(response.body());
		String[] identifiers = { "wellId", "treatmentId", "treatmentNumber", "baseTreatmentDateTime",
				"operatorCompanyName" };
		this.wellMap = DataMapJSon.wellPropertiesMap(scan, identifiers);
		setMap = true;
		notify();
	}

	public synchronized HashMap<String, HashMap<String, String>> getWellMap() {
		while (!setMap)
			try {
				wait();
			} catch (InterruptedException e) {
			}
		return this.wellMap;
	}

	@Override
	public synchronized void run() {
		try {
			wellRequest(at);
		} catch (IOException | InterruptedException e1) {
		}
	}
}
