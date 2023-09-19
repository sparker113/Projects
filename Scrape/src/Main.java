
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.Function;

import javax.swing.JOptionPane;

import intelie.RememberMe;
import login.EncryptCredentials;
import login.UserNamePassword;

public class Main {
	static mainFrame yess;
	// static ArrayList<ArrayList<String>> dataSet;
	// static baseTreatment bTreat;
	public static Main instance;

	public static void main(String[] args) throws Exception {
		if (!mainFrame.isConnectedToInternet()) {
			JOptionPane.showMessageDialog(null, "Check your internet connection");
			return;
		}
		initProperties();
		HashMap<String, String> map = EncryptCredentials.getUserCredentials(getIntelieLoginFunc(),
				intelie.CrewRequest.INTELIE_CRED_PATH, "Intelie Login");
		HashMap<String, String> iqLoginMap = EncryptCredentials.getUserCredentials(getPetroIQLoginFunc(),
				joblog.LoginRequest.PETRO_IQ_CRED_PATH, "PetroIQ Login");
		if (!checkCredentialMap(map) | !checkCredentialMap(iqLoginMap)) {
			JOptionPane.showMessageDialog(null, "Not Authorized");
			return;
		}
		try {
			yess = new mainFrame();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static Function<HashMap<String, String>, Boolean> getPetroIQLoginFunc() throws Exception {
		return (Function<HashMap<String, String>, Boolean>) (map) -> {
			joblog.LoginRequest loginRequest = null;
			try {
				loginRequest = new joblog.LoginRequest(map.get(EncryptCredentials.USERNAME),
						map.get(EncryptCredentials.PASSWORD));
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
				return false;
			}
			return loginRequest.getAuthorized();

		};
	}

	public static Function<HashMap<String, String>, Boolean> getIntelieLoginFunc() {
		return (Function<HashMap<String, String>, Boolean>) (map2) -> {
			RememberMe rememberMe = null;
			try {
				rememberMe = new RememberMe(map2.get(EncryptCredentials.USERNAME),
						map2.get(EncryptCredentials.PASSWORD));
			} catch (IOException | InterruptedException e) {
				try {
					RememberMe.deleteCookie();
				} catch (IOException e1) {
					System.out.println("Failed to delete cookie.scp file");
					return false;
				}
			}
			if (rememberMe.getCookie().equals("AUTH=FAILED")) {
				return false;
			}
			return true;
		};
	}

	// RETURNS TRUE IF EACH KEY HAS A VALID VALUE
	public static boolean checkCredentialMap(HashMap<String, String> map) {
		if (map == null) {
			return false;
		}
		for (String s : map.keySet()) {
			if (map.get(s) == null || map.get(s).isBlank()) {
				return false;
			}
		}
		return true;
	}

	public final static String IMAGE_PROPERTY = "PROGRAM_IMAGE_PATH";

	public static void initProperties() {
		try {
			System.setProperty(IMAGE_PROPERTY, "C:\\Scrape\\Scrape.png");
			System.getProperties().storeToXML(new FileOutputStream(new File("properties.xml")), "Scrape Properties");

			System.getProperties().loadFromXML(new FileInputStream(new File("properties.xml")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Main getInstance() {
		return instance;
	}

}
