import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CSVJobLog {
	String wellStage;

	CSVJobLog(String wellStage) throws IOException {
		this.wellStage = wellStage;
		writeNewBody();
	}

	public void writeNewBody() throws IOException {
		String previous = readFile();
		FileWriter fileWriter = new FileWriter("C:\\Scrape\\job_log.csv");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		DateTimeFormatter dateTime = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
		LocalDateTime now = LocalDateTime.now();
		writeHeaders(bufferedWriter);
		bufferedWriter.append(wellStage);
		bufferedWriter.append(",");
		bufferedWriter.append(String.valueOf(dateTime.format(now)));
		bufferedWriter.newLine();
		bufferedWriter.append(previous);
		bufferedWriter.flush();
		bufferedWriter.close();
	}

	public void appendNewLine() throws IOException {
		FileWriter fileWriter = new FileWriter("C:\\Scrape\\job_log.csv");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		bufferedWriter.append("/n");
		bufferedWriter.close();
	}

	public void writeHeaders(BufferedWriter bufferedWriter) throws IOException {
		bufferedWriter.append("Well,Stage,Close Time,Time Email Was Sent");
		bufferedWriter.newLine();
	}

	private String readFile() throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		if (!new File("C:\\Scrape\\job_log.csv").exists()) {
			return "";
		}
		FileReader fileReader = new FileReader("C:\\Scrape\\job_log.csv");
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String temp;
		bufferedReader.readLine();
		while ((temp = bufferedReader.readLine()) != null) {
			stringBuilder.append(temp);
			stringBuilder.append("\n");
		}
		return stringBuilder.substring(0);
	}

	private class MetricWorkbook extends XSSFWorkbook {
		MetricWorkbook() {

		}
	}

}
