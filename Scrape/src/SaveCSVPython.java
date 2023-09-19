import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class SaveCSVPython {
	SaveCSVPython() {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec("C:\\Scrape\\ScrapePython\\CSV.exe");
		} catch (IOException e) {
			try {
				TextLog textLog = new TextLog("SaveCSVPython IOException");
			} catch (IOException e1) {
			}
		}
		InputStream stdout = process.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(stdout, StandardCharsets.UTF_8));
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				System.out.println("stdout: " + line);
			}
		} catch (IOException e) {
			System.out.println("Exception in reading output" + e.toString());
		}
	}
}
