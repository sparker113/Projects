import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class StageTimeOffset {
	private String at = new String();
	private String wellId = new String();
	private String treatmentId = new String();
	private boolean done;
	private Long offset;
	private String topPerf;
	private String bottomPerf;
	private Long tvd;

	public StageTimeOffset() {
		done = false;
	}

	public void StageTimeOffset(String at, String wellId, String treatmentId) throws IOException, InterruptedException {
		this.at = at;
		this.wellId = wellId;
		this.treatmentId = treatmentId;
		offset = getOffset(request());

	}

	public Scanner request() throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = (HttpRequest.newBuilder().header("Content-type", "application/json; charset=utf-8")
				.header("Accepet", "application/json").header("Authorization", at)).GET()
				.uri(URI.create("https://api.fracpro.ai:4000/api/v1/wells/" + wellId + "/treatments/" + treatmentId))
				.build();
		HttpResponse<InputStream> resp = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		Scanner response = new Scanner(resp.body());
		return response;
	}

	public Long getOffset(Scanner response) {
		response.useDelimiter(",");
		String temp;
		Long offset = Long.valueOf(0);
		while (response.hasNext()) {
			temp = response.next();
			if (temp.contains("startTreatmentTimeOffSet")) {
				offset = Long.valueOf(temp.split(":")[1].trim());
			} else if (temp.contains("mdFormationTop")) {
				topPerf = temp.split(":")[1].trim();
			} else if (temp.contains("mdFormationBottom")) {
				bottomPerf = temp.split(":")[1].trim();
			} else if (temp.contains("tvdFormationTop")) {
				tvd = Long.valueOf(Math.round(Double.valueOf(temp.split(":")[1].trim())));
			}
		}
		done = true;
		return offset;
	}

	public Long getTimeOffset() {
		return offset;
	}

	public String getPerfs() {
		return topPerf + " - " + bottomPerf;
	}

	public Long getTVD() {
		return tvd;
	}

	public boolean finished() {
		return done;
	}
}
