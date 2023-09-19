import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Scanner;

public class wellListRequest {
	String wellListRequest(String at) throws IOException, InterruptedException {

		HttpClient client = HttpClient.newHttpClient();
		HttpRequest req = (HttpRequest.newBuilder().header("Content-type", "application/json; charset=utf-8")
				.header("Accepet", "application/json").header("Authorization", at)).GET()
				.uri(URI.create("https://api.fracpro.ai:4000/api/v1/wells/GetListWellPaging?page=2&pageSize=8"))
				.build();
		HttpResponse<InputStream> resp = client.send(req, HttpResponse.BodyHandlers.ofInputStream());
		Scanner that = new Scanner(resp.body());
		Integer tCount;
		that.useDelimiter("]");
		// System.out.println(resp.body().toString());
		// while(that.hasNext()) {
		tCount = Integer.valueOf(that.next().split(",")[0].split(":")[2]);
		while (that.hasNext()) {
			// System.out.println(that.next());
			that.next();
		}
		// System.out.println(tCount);
		that.close();

		HttpClient client2 = HttpClient.newHttpClient();
		HttpRequest req2 = (HttpRequest.newBuilder().header("Content-type", "application/json; charset=utf-8")
				.header("accepet", "application/json").header("authorization", at).GET()
				.uri(URI.create("https://api.fracpro.ai:4000/api/v1/wells/GetListWellPaging?page=1&pageSize="
						+ String.valueOf(tCount - 1)))
				.build());
		HttpResponse<InputStream> resp2 = client2.send(req2, HttpResponse.BodyHandlers.ofInputStream());

		Scanner that2 = new Scanner(resp2.body());
		that2.useDelimiter("]");
		ArrayList<String> allWells = new ArrayList<>();
		while (that2.hasNext()) {
			allWells.add(that2.next());
		}
		allWells.forEach(System.out::println);
		that2.close();
		return "a";
	}
}
