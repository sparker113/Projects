import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TextLog {
	TextLog(String log) throws IOException {
		String previous = readFile();
		FileWriter fileWriter = new FileWriter("C:\\Scrape\\log.txt");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		DateTimeFormatter dateTime = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
		LocalDateTime now = LocalDateTime.now();
		bufferedWriter.append("----------------------------");
		bufferedWriter.append(dateTime.format(now));
		bufferedWriter.append("----------------------------");
		bufferedWriter.append("\n");
		for (String a : log.split("\n")) {
			bufferedWriter.append(a);
			bufferedWriter.append("\n");
		}
		bufferedWriter.append(previous);
		bufferedWriter.flush();
		bufferedWriter.close();

	}

	private String readFile() throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		FileReader fileReader = new FileReader("C:\\Scrape\\log.txt");
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String temp;
		while ((temp = bufferedReader.readLine()) != null) {
			stringBuilder.append(temp);
			stringBuilder.append("\n");
		}
		return stringBuilder.substring(0);
	}
}
