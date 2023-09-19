import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ReportPlots {

	public ReportPlots() {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(new String[] { "C:\\Scrape\\ScrapePython\\ReportPlots.exe" });
		} catch (Exception e) {
			System.out.println("Exception Raised" + e.toString());
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
		process.destroy();
	}
}

/*
 * class PointTwo{ public static void main(String[] args){ ReportPlots
 * scriptPython = new ReportPlots(); scriptPython.runPlots(); } }
 */
