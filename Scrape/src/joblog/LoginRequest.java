package joblog;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Scanner;

public class LoginRequest {
	String email;
	String password;
	String token;
	public final static String PETRO_IQ_CRED_PATH = "petro_iq\\";
	public LoginRequest(String email, String password) throws IOException, InterruptedException {
		this.email = email;
		this.password = password;
		setToken(parseJSon(makeRequest()));
	}

	private void setToken(String token) {
		this.token = token;
	}

	public String getToken() {
		return this.token;
	}

	public String parseJSon(Scanner scanner) {
		scanner.useDelimiter("\\}");
		String token = "";
		while (scanner.hasNext()) {
			String temp = scanner.next();
			if (temp.contains("token")) {
				token = temp.split(":")[1].replace("\"", "");
			}
		}
		return token;

	}

	public Scanner makeRequest() throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().header("content-type", "application/json;charset=UTF-8")
				.uri(URI.create("https://propetro.petroiq.com/login"))
				.POST(HttpRequest.BodyPublishers.ofString(getPayload())).build();
		HttpResponse<InputStream> response = client.send(request, BodyHandlers.ofInputStream());
		setAuthorized(response.statusCode()==200);
		Scanner scanner = new Scanner(response.body());
		return scanner;
	}
	private boolean authorized = false;
	private void setAuthorized(boolean authorized) {
		this.authorized = authorized;
	}
	public boolean getAuthorized() {
		return authorized;
	}
	private String getPayload() {
		return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
	}
}
