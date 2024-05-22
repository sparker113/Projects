package mrl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.auth0.net.client.Auth0FormRequestBody;

import login.EncryptCredentials;

public class Login {
	public Login(List<String> cookies) throws IOException, InterruptedException {
		setSessionCookies(cookies);
		setBearerToken();
	}


	public final static String USERNAME_KEY = EncryptCredentials.USERNAME;
	public final static String PASSWORD_KEY = EncryptCredentials.PASSWORD;


	private String sessionID;
	
	private String kp3;
	
	private String cookieString;
	
	private Map<String,String> tokenMap;
	
	private final static String INITIAL_COOKIES = "MRLMobile=0; last_loginid=" + USERNAME_KEY + "; last_domain=shear";

	public final static String MRL_LOGIN_URL = "https://shear.mrlsolutions.com//login.php";
	public final static String TOKEN_URL = "https://shear.mrlsolutions.com/oauth/token";
	
	private void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}
	
	private void setKP3(String kp3) {
		this.kp3 = kp3;
	}
	
	private void setCookieString(List<String> cookies) {
		this.cookieString = constrCookieString(cookies);
	}
	private String constrCookieString(List<String> cookies) {
		String cString = "";
		for(String s:cookies) {
			cString+="; "+s;
		}
		cString=cString.substring(2);
		return cString;
	}
	public String getSessionID() {
		return this.sessionID;
	}
	
	public String getKP3() {
		return this.kp3;
	}
	
	public String getCookieString() {
		return this.cookieString;
	}
	public Map<String,String> getTokenMap(){
		return this.tokenMap;
	}
	public final static String TOKEN_KEY = "access_token";
	public final static String TYPE_KEY = "token_type";
	
	public final static String SESSION_ID = "sessionid";
	public final static String KP3 = "kp3";
	
	public void setSessionCookies(List<String> cookies) {
		Map<String,String> cookieMap = getCookieMap(getStringList(cookies));
		setSessionID(cookieMap.get(SESSION_ID));
		setKP3(cookieMap.get(KP3));
		setCookieString(cookies);
	}
	
	public static Map<String,String> getCookieMap(String cookies){
		Map<String,String> map = new HashMap<>();
		
		Matcher matcher = Pattern.compile(".+?=.+?;").matcher(cookies);
		while(matcher.find()) {
			String found = matcher.group();
			found = found.substring(0,found.length()-1);
			if(found.matches(".+?=.+?,/s.+?=.+?$")) {
				String[] splitCookies = found.split(",");
				map.put(splitCookies[0].split("=")[0].trim(), splitCookies[0].split("=")[1].trim());
				map.put(splitCookies[1].split("=")[0].trim(), splitCookies[1].split("=")[1].trim());
			}else {
				map.put(found.split("=")[0].trim(), found.split("=")[1].trim());
			}
		}
		return map;
	}
	
	public static List<String> makeSessionIDRequest(String url, String username, String password)
			throws IOException, InterruptedException {
		CookieHandler cookieHandler = getCookieHandler();
		HttpClient client = HttpClient.newBuilder().cookieHandler(cookieHandler).build();

		HttpRequest request = HttpRequest.newBuilder().headers(getInitRequestHeadersString())
				.uri(URI.create(url))
				.POST(HttpRequest.BodyPublishers.ofString(getLoginFormString(username,password)))
				.build();
		
		HttpResponse<InputStream> response = client.send(request, BodyHandlers.ofInputStream());
		System.out.println("Status Code: " + response.statusCode());
		System.out.println(response.headers().map());
		List<String> cookieList = client.cookieHandler().get().get(URI.create(url),response.headers().map()).get("Cookie");
		if(cookieList.size()==0) {
			return null;
		}
		return cookieList;
	}
	private final static String TOKEN_GRANT_TYPE = "client_credentials";
	private final static String GRANT_TYPE_KEY = "grant_type";
	private void setBearerToken() throws IOException,InterruptedException{
		HttpClient client = HttpClient.newBuilder().build();
		HttpRequest request = HttpRequest.newBuilder().headers(getTokenRequestHeadersString(getCookieString()))
				.uri(URI.create(TOKEN_URL))
				.POST(HttpRequest.BodyPublishers.ofString(getTokenFormString()))
				.build();
		HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
		setTokenMap(response.body());
	}

	private void setTokenMap(String tokenResponse) {
		this.tokenMap = parseTokenResponse(tokenResponse);
	}
	private Map<String,String> parseTokenResponse(String tokenResponse){
		tokenResponse = tokenResponse.replace("{", "").replace("}", "").replace("\"", "");
		String[] keyValues = tokenResponse.split(",");
		Map<String,String> tokenMap = new HashMap<>();
		for(String s:keyValues) {
			tokenMap.put(s.split(":")[0], s.split(":")[1]);
		}
		return tokenMap;
	}


	public static String getStringList(List<String> list) {
		String stringList = "";
		
		for(String s:list) {
			stringList+=";"+s;
		}
		stringList=stringList.substring(1);
		return stringList;
	}
	public static Map<String, String> getLoginHeaders() {
		Map<String, String> map = new HashMap<>();
		map.put("Accept-Encoding", "gzip, deflate, br, zstd");
		map.put("Authorization", "form");
		map.put("Accept",
				"application/json, text/javascript, */*;q=0.01");
		map.put("User-Agent",
				"\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");
		return map;
	}
	public static Map<String,String> getInitRequestHeaders(){
		Map<String, String> map = new HashMap<>();
		map.put("Accept-Encoding", "gzip, deflate, br, zstd");
		map.put("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
		map.put("Content-Type", "application/x-www-form-urlencoded");
		map.put("Cookie", "MRLMobile=0; last_loginid=s.parker%40shearfrac.com; last_domain=shear");
		map.put("Upgrade-Insecure-Requests", "1");
		map.put("User-Agent",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");
		return map;
	}
	public static Map<String,String> getTokenRequestHeaders(String cookieString){
		Map<String, String> map = new HashMap<>();
		map.put("Accept-Encoding", "gzip, deflate, br, zstd");
		map.put("Accept",
				"application/json, text/javascript, */*; q=0.01");
		map.put("Authorization", "Cookie");
		map.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		map.put("Cookie", cookieString);
		map.put("User-Agent",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");
		return map;
	}
	public static String[] getTokenRequestHeadersString(String cookieString){
		Map<String, String> map = getTokenRequestHeaders(cookieString);
		String[] headers = new String[map.size()*2];
		int i = 0;
		for(Map.Entry<String,String> entry:map.entrySet()) {
			headers[i] = entry.getKey();
			i++;
			headers[i] = entry.getValue();
			i++;
		}
		return headers;
	}
	public static String[] getInitRequestHeadersString(){
		Map<String, String> map = getInitRequestHeaders();
		String[] headers = new String[map.size()*2];
		int i = 0;
		for(Map.Entry<String,String> entry:map.entrySet()) {
			headers[i] = entry.getKey();
			i++;
			headers[i] = entry.getValue();
			i++;
		}
		return headers;
	}
	public static Map<String,String> getInitRequestHeaders2(){
		Map<String, String> map = new HashMap<>();
		map.put("Accept-Encoding", "gzip, deflate, br, zstd");
		map.put("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
		map.put("Content-Type", "application/x-www-form-urlencoded");
		map.put("Cookie", "MRLMobile=0; last_loginid=s.parker%40shearfrac.com; last_domain=shear; EGW_PHPSESSID=deleted");
		map.put("Upgrade-Insecure-Requests", "1");
		map.put("User-Agent",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");
		return map;
	}
	

	@SuppressWarnings("unused")
	public static void checkResponse(HttpResponse<InputStream> response) {
		System.out.println("Status Code: " + response.statusCode());
		Scanner scanner = new Scanner(new InputStreamReader(response.body()));
		while (scanner.hasNext()) {
			System.out.println(scanner.next());
		}
	}

	public static Auth0FormRequestBody getFormData(String username, String password) {
		Map<String, Object> map = getLoginForm(username, password);
		Auth0FormRequestBody body = new Auth0FormRequestBody(map);
	
		return body;
	}

	private static Map<String, Object> getLoginForm(String username, String password) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("passwd_type", "text");
		map.put("account_type", "u");
		map.put("login", URLEncoder.encode(username, StandardCharsets.UTF_8));
		map.put("passwd", password);
		map.put("submitit", "Login");
		return map;
	}

	private static String getLoginFormString(String username, String password) {
		Map<String,Object> map = getLoginForm(username,password);
		String formString = "";
		for(Map.Entry<String,Object> entry:map.entrySet()) {
			formString+="&"+entry.getKey()+"="+entry.getValue();
		}
		formString = formString.substring(1);
		System.out.println(formString);
		return formString;
	}
	
	private static String getTokenFormString() {
		return GRANT_TYPE_KEY+"="+TOKEN_GRANT_TYPE;
	}

	private static String getInitialCookieString(String username) {
		return INITIAL_COOKIES.replace(USERNAME_KEY, URLEncoder.encode(username, StandardCharsets.UTF_8));
	}

	private static CookieHandler getCookieHandler() {
		CookieManager cookieManager = new CookieManager();
		CookieHandler.setDefault(cookieManager);
		CookieHandler cookieHandler = CookieHandler.getDefault();
		return cookieHandler;
	}

}
