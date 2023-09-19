import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSessionContext;
import javax.swing.JOptionPane;

public class GrantAccess {

	public static String readAccessToken() throws IOException {
		String token;
		File file = new File("C:\\Scrape\\bearer_token.txt");
		if (!file.exists()) {
			return makeRequestForToken();
		}
		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		token = bufferedReader.readLine();
		bufferedReader.close();
		return token;
	}

	private static void writeAccessToken(String accessToken) throws IOException {
		FileWriter fileWriter = new FileWriter(new File("C:\\Scrape\\bearer_token.txt"));
		fileWriter.write(accessToken);
		fileWriter.flush();
		fileWriter.close();
	}

	public static String makeRequestForToken() {
		int i = 0;
		List<NameValuePair> values = new ArrayList<>();
		values.add(new NameValuePair("userNameOrEmailAddress", "PPS6dv@propetroservices.com"));
		values.add(new NameValuePair("password", "Propetro62022!"));

		String reqBody = new String();
		reqBody = "{";
		for (NameValuePair q : values) {
			if (i == 0) {
				reqBody = reqBody + q.c + ",";
				i++;
			} else if (i == 1) {
				reqBody = reqBody + q.c;
			}
		}

		reqBody = reqBody + ",\"rememberClient\":true}";
		// System.out.println(reqBody);
		Integer respCode = 0;
		HttpClient obj = HttpClient.newHttpClient();
		HttpRequest req = (HttpRequest.newBuilder().header("content-type", "application/json"))
				.header("accepet", "application/json")
				.uri(URI.create("https://api.fracpro.ai:4000/api/TokenAuth/AuthenticateNoTenant"))
				.POST(HttpRequest.BodyPublishers.ofString(reqBody)).build();
		HttpResponse<String> resp = null;
		try {
			resp = obj.send(req, HttpResponse.BodyHandlers.ofString());
			respCode = resp.statusCode();
		} catch (IOException | InterruptedException e) {
			System.out.println("Exception Caught getting Bearer Token");
			return null;
		}
		if (respCode == 0) {
			JOptionPane.showMessageDialog(Main.yess, "Check your internet connection, then go to File > Refresh");
		}
		// Iterator<String> respBody;
		// respBody = resp.body().lines().iterator();
		// Scanner it = new Scanner((Readable) respBody);
		// it.
		// ArrayList<String> dataArray = new ArrayList<>();
		String justWork = resp.body();

		String at = resp.body().split(":")[2].split(",")[0].split("\"")[1];
		// System.out.println(at);
		clearSSLCache(obj);
		try {
			writeAccessToken("Bearer " + at);
		} catch (IOException e) {
			System.out.println("Exception caught writing token");
		}
		return "Bearer " + at;
	}

	public static void clearSSLCache(HttpClient client) {
		SSLContext context = client.sslContext();
		SSLSessionContext sslSession = context.getClientSessionContext();
		Collections.list(context.getClientSessionContext().getIds()).stream().map(sslSession::getSession)
				.forEach(System.out::println);
	}

	public static class NameValuePair {

		private String c;

		public NameValuePair(String a, String b) {
			this.c = "\"" + a + "\":\"" + b + "\"";
		}
	}

	public class dataR {

		dataR(String p) {

		}
	}
}
