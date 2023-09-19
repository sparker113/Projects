package intelie;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.JOptionPane;

import login.EncryptCredentials;
import login.UserNamePassword;

public class RememberMe implements Serializable {
	String cookie;
	private static final long serialVersionUID = 8540293410390577283L;

	public RememberMe(String username, String password) throws IOException, InterruptedException {
		setCookie(username, password);
		// setCookie();
	}
	public RememberMe(String username, String password,String fileName) throws IOException, InterruptedException {
		setCookie(username, password,fileName);
		// setCookie();
	}

	public RememberMe() throws IOException, InterruptedException {
		HashMap<String, String> map = null;
		try {
			map = EncryptCredentials.getUserCredentials();
		} catch (Exception e) {
			System.out.println();
		}
		setCookie(map.get("username"), map.get("password"));
		// setCookie();
	}

	private RememberMe(String filler) throws IOException {

	}

	public static RememberMe resetCookie() throws IOException, InterruptedException {
		HashMap<String, String> credentials = null;
		try {
			credentials = EncryptCredentials.getUserCredentials();
		} catch (Exception e) {
			return null;
		}
		return setCookie(credentials);
	}

	public static boolean deleteCookie() throws IOException {
		File file = new File(COOKIE_PATH);
		return file.delete();
	}

	/*
	 * public void setCookie() throws IOException, InterruptedException { Process
	 * process = Runtime.getRuntime().exec(new String[] { "powershell",
	 * "Start chrome '--new-window https://propetro.intelie.com/auth/plugin-samlv2/PropetroIntelieLive/SSO'"
	 * }); JOptionPane.showConfirmDialog(null, "Click yes after you have logged in",
	 * JOptionPane.MESSAGE_PROPERTY, JOptionPane.YES_OPTION); intelie.ChromeBrowser
	 * browser = new intelie.ChromeBrowser(); Set<Cookie> cookies =
	 * browser.getCookiesForDomain("remember-me", "propetro.intelie.com"); for
	 * (Cookie c : cookies) { this.cookie = c.getValue(); } saveCookie(); }
	 */

	public boolean setCookie(String username, String password) throws IOException, InterruptedException {
		System.out.println("System Account Login");
		CrewRequest crewRequest = new CrewRequest(true);
		crewRequest.sessionIDRequest(true);
		String sessionId = crewRequest.getSessionId();
		String csrfToken = crewRequest.getToken();
		boolean bool = rememberMeRequest(username, password, sessionId, csrfToken);
		if (!bool) {
			updateUserCredentials();
		}
		saveCookie();
		return bool;
	}
	public boolean setCookie(String username, String password,String fileName) throws IOException, InterruptedException {
		System.out.println("System Account Login");
		CrewRequest crewRequest = new CrewRequest(true);
		crewRequest.sessionIDRequest(true);
		String sessionId = crewRequest.getSessionId();
		String csrfToken = crewRequest.getToken();
		boolean bool = rememberMeRequest(username, password, sessionId, csrfToken);
		if (!bool) {
			updateUserCredentials();
		}
		saveCookie(fileName);
		return bool;
	}

	private static RememberMe setCookie(HashMap<String, String> map) throws IOException, InterruptedException {
		CrewRequest crewRequest = new CrewRequest(true);
		crewRequest.sessionIDRequest(true);
		String sessionId = crewRequest.getSessionId();
		String csrfToken = crewRequest.getToken();
		RememberMe rememberMe = new RememberMe("Sam");
		boolean bool = rememberMeRequest(map.get(UserNamePassword.USERNAME),
				map.get(UserNamePassword.PASSWORD),sessionId,csrfToken,rememberMe);
		return rememberMe;
	}

	private void updateUserCredentials() {
		try {
			EncryptCredentials.updateUserCredentials(
					(Function<HashMap<String, String>, Boolean>) (HashMap<String, String> map) -> {
						RememberMe rememberMe = null;
						try {
							rememberMe = new RememberMe(map.get(EncryptCredentials.USERNAME),
									map.get(EncryptCredentials.PASSWORD));
						} catch (Exception e) {
							setRememberMe("AUTH=FAILED");
							return false;
						}
						if (cookie.equals("AUTH=FAILED")) {
							return false;
						}
						return true;
					});
		} catch (InvalidKeyException | ClassNotFoundException | NoSuchAlgorithmException | NoSuchPaddingException
				| IllegalBlockSizeException | BadPaddingException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setRememberMe(String rememberMeCookie) {
		Matcher matcher = Pattern.compile("remember-me\\s?=\\s?").matcher(rememberMeCookie);
		if (matcher.find()) {
			this.cookie = rememberMeCookie.replace(matcher.group(), "");
			try {
				saveCookie();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		try {
			saveCookie();
		} catch (IOException e) {
			this.cookie = rememberMeCookie;
		}
		this.cookie = rememberMeCookie;
	}
	private void setRememberMe(String rememberMeCookie,String fileName) {
		Matcher matcher = Pattern.compile("remember-me\\s?=\\s?").matcher(rememberMeCookie);
		if (matcher.find()) {
			this.cookie = rememberMeCookie.replace(matcher.group(), "");
			try {
				saveCookie(fileName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		try {
			saveCookie(fileName);
		} catch (IOException e) {
			this.cookie = rememberMeCookie;
		}
		this.cookie = rememberMeCookie;
	}

	private static boolean rememberMeRequest(String username, String password, String sessionId, String csrfToken,
			RememberMe rememberMe) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newBuilder().cookieHandler(getCookieHandler()).build();
		HttpRequest request = HttpRequest.newBuilder().header("accept", "*/*")
				.header("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
				.header("accept-encoding", "gzip, deflate, br").header("cookie", sessionId)
				.header("X-CSRF-TOKEN", csrfToken)
				.uri(URI.create("https://propetro.intelie.com/j_spring_security_check"))
				.POST(HttpRequest.BodyPublishers
						.ofString("j_username=" + URLEncoder.encode(username, StandardCharsets.UTF_8) + "&j_password="
								+ URLEncoder.encode(password, StandardCharsets.UTF_8)))
				.build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		try {
			rememberMe.setRememberMe(client.cookieHandler().get()
					.get(URI.create("https://propetro.intelie.com"), response.headers().map()).get("Cookie").get(1));
		} catch (Exception e) {
			rememberMe.setRememberMe("AUTH=FALSE");
			rememberMe.saveCookie();
			return false;
		}
		rememberMe.saveCookie();
		return true;
	}

	private boolean rememberMeRequest(String username, String password, String sessionId, String csrfToken)
			throws IOException, InterruptedException {
		HttpClient client = HttpClient.newBuilder().cookieHandler(getCookieHandler()).build();
		HttpRequest request = HttpRequest.newBuilder().header("accept", "*/*")
				.header("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
				.header("accept-encoding", "gzip, deflate, br").header("cookie", sessionId)
				.header("X-CSRF-TOKEN", csrfToken)
				.uri(URI.create("https://propetro.intelie.com/j_spring_security_check"))
				.POST(HttpRequest.BodyPublishers
						.ofString("j_username=" + URLEncoder.encode(username, StandardCharsets.UTF_8) + "&j_password="
								+ URLEncoder.encode(password, StandardCharsets.UTF_8)))
				.build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		try {
			setRememberMe(client.cookieHandler().get()
					.get(URI.create("https://propetro.intelie.com"), response.headers().map()).get("Cookie").get(1));
		} catch (Exception e) {
			setRememberMe("AUTH=FALSE");
			return false;
		}
		return true;
	}

	public final static String COOKIE_PATH = "cookie.scp";

	private void saveCookie() throws IOException {
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(new File(COOKIE_PATH)));
		objectOutputStream.writeObject(this);
		objectOutputStream.flush();
		objectOutputStream.close();
	}
	private void saveCookie(String fileName) throws IOException {
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(new File(fileName)));
		objectOutputStream.writeObject(this);
		objectOutputStream.flush();
		objectOutputStream.close();
	}

	public static CookieHandler getCookieHandler() {
		CookieManager manager = new CookieManager();
		CookieHandler.setDefault(manager);
		CookieHandler cookieHandler = CookieHandler.getDefault();
		return cookieHandler;
	}

	public String getCookie() {
		return this.cookie;
	}

	public static RememberMe readCookie() throws IOException, InterruptedException, ClassNotFoundException {
		File file = new File("C:\\Scrape\\cookie.scp");
		if (!file.exists()) {
			HashMap<String, String> map = null;
			try {
				map = EncryptCredentials.getUserCredentials();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return new RememberMe(map.get("username"), map.get("password"));
		}
		ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
		RememberMe rememberMe = (RememberMe) objectInputStream.readObject();
		if (rememberMe == null || rememberMe.getCookie() == null) {
			HashMap<String, String> map = null;
			try {
				EncryptCredentials.getUserCredentials();
			} catch (Exception e) {
				e.printStackTrace();
			}
			rememberMe = new RememberMe();
		}
		objectInputStream.close();
		return rememberMe;
	}
	public static RememberMe readCookie(String fileName) throws IOException, InterruptedException, ClassNotFoundException {
		File file = new File(fileName);
		if (!file.exists()) {
			HashMap<String, String> map = null;
			try {
				map = EncryptCredentials.getUserCredentials();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return new RememberMe(map.get("username"), map.get("password"),fileName);
		}
		ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
		RememberMe rememberMe = (RememberMe) objectInputStream.readObject();
		if (rememberMe == null || rememberMe.getCookie() == null) {
			HashMap<String, String> map = null;
			try {
				EncryptCredentials.getUserCredentials();
			} catch (Exception e) {
				e.printStackTrace();
			}
			rememberMe = new RememberMe();
		}
		objectInputStream.close();
		return rememberMe;
	}
}
