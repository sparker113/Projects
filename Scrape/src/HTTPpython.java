import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class HTTPpython {
	public HTTPpython(String type) {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(new String[] { "C:\\Scrape\\ScrapePython\\HTTPpython.exe", type });

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
 * class WellDrive{ public static void main(String[] args){ HTTPpython
 * scriptPython = new HTTPpython(); scriptPython.UploadWD(); } }
 */
