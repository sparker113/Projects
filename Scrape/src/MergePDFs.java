import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MergePDFs {
	Process mProcess;
	String arguments;

	MergePDFs(String arguments) {
		this.arguments = arguments;
		Process process = null;
		try {
			System.out.println(arguments);
			process = Runtime.getRuntime().exec("C:\\Scrape\\ScrapePython\\MergePDFs.exe " + arguments);
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
 * class PJR{ public static void main(String[] args){ MergePDFs scriptPython =
 * new MergePDFs(); scriptPython.CreateReports(); } }
 */