import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class MetricsMap extends LinkedHashMap<String, LinkedHashMap<String, HashMap<String, String>>>
		implements Serializable {
	Boolean sentEmail;
	DayOfWeek dayOfWeek;

	MetricsMap() {
		this.sentEmail = false;
		this.dayOfWeek = LocalDateTime.now().getDayOfWeek();
	}

	MetricsMap(DayOfWeek dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public void addToMap(String well, String stage, String endTime, String crew)
			throws FileNotFoundException, IOException {
		String sendTime = getFormattedDateTime(LocalDateTime.now());
		String formatEnd = getFormattedDateTime(endTime);
		if (this.containsKey(well) && !this.get(well).containsKey(stage)) {
			this.get(well).put(stage, makeMetric(crew, formatEnd, sendTime, "1"));
		} else if (this.containsKey(well) && this.get(well).containsKey(stage)) {
			String count = String.valueOf(Integer.valueOf(this.get(well).get(stage).get("count")) + 1);
			this.get(well).put(stage, makeMetric(crew, formatEnd, sendTime, count));
		} else {
			this.put(well, new LinkedHashMap<>());
			this.get(well).put(stage, makeMetric(crew, formatEnd, sendTime, "1"));
		}
		setSentEmail();
		checkAndSend();
		writeToFile();
	}

	private String getFormattedDateTime(LocalDateTime localDateTime) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
		String formattedDate = LocalDateTime.parse(String.valueOf(localDateTime)).format(formatter);
		return formattedDate;
	}

	private String getFormattedDateTime(String dateTimeString) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
		String formattedDate = LocalDateTime.parse(String.valueOf(dateTimeString)).format(formatter);
		return formattedDate;
	}

	private HashMap<String, String> makeMetric(String crew, String endTime, String sendTime, String count) {
		HashMap<String, String> metric = new HashMap<>();
		metric.put("transmission", getTransmissionTime(endTime, sendTime));
		metric.put("endTime", endTime);
		metric.put("sendTime", sendTime);
		metric.put("count", count);
		metric.put("crew", crew);
		return metric;
	}

	private String getTransmissionTime(String endDateTime, String sendDateTime) {
		LocalDateTime end = LocalDateTime.parse(getFormattedDate(endDateTime));
		LocalDateTime send = LocalDateTime.parse(getFormattedDate(sendDateTime));
		String transmissionTime = String.valueOf(Duration.between(end, send).toMinutes());
		return transmissionTime;
	}

	private String getFormattedDate(String dateString) {
		String withSpace = checkForSpace(dateString);
		String date = dateString.split(" ")[0];
		String[] yearMonthDay = new String[] { date.split("/")[2], date.split("/")[0], date.split("/")[1] };
		return yearMonthDay[0] + "-" + yearMonthDay[1] + "-" + yearMonthDay[2] + "T" + dateString.split(" ")[1];
	}

	private String checkForSpace(String dateString) {
		if (!dateString.contains(" ") && dateString.length() > 10) {
			return dateString.substring(0, 10) + " " + dateString.substring(10, dateString.length());
		}
		return dateString;
	}

	public void writeToFile() throws FileNotFoundException, IOException {
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(
				new FileOutputStream(new File("C:\\Scrape\\MetricsMap.map")));
		objectOutputStream.writeObject(this);
		objectOutputStream.flush();
		objectOutputStream.close();
	}

	private static Boolean checkFile() {
		File file = new File("C:\\Scrape\\MetricsMap.map");
		return file.exists();
	}

	public static MetricsMap readFromFile() throws FileNotFoundException, IOException, ClassNotFoundException {
		if (!checkFile()) {
			return new MetricsMap();
		}
		ObjectInputStream objectInputStream = new ObjectInputStream(
				new FileInputStream(new File("C:\\Scrape\\MetricsMap.map")));
		MetricsMap metricsMap = (MetricsMap) objectInputStream.readObject();
		objectInputStream.close();
		return metricsMap;
	}

	public static void clearMap() {
		File file = new File("C:\\Scrape\\MetricsMap.map");
		System.out.println(file.delete());
	}

	private void setSentEmail() {
		if (LocalDateTime.now().getDayOfWeek() != dayOfWeek) {
			this.sentEmail = false;
			dayOfWeek = LocalDate.now().getDayOfWeek();
		}
	}

	private Boolean isDayOfWeek() {
		return LocalDateTime.now().getDayOfWeek() == dayOfWeek;
	}

	private void sendEmail(String path) throws IOException {
		Process process = Runtime.getRuntime().exec(new String[] { "C:\\Scrape\\ScrapePython\\ROC_Email.exe", path });
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String temp;
		while ((temp = bufferedReader.readLine()) != null) {
			System.out.println(temp);
		}
		process.destroy();
	}

	private Boolean checkAndSend() throws IOException {
		if (isDayOfWeek() && !this.sentEmail) {
			MetricsMapWorkbook metrics = new MetricsMapWorkbook(this);
			metrics.makeWorkbook();
			sendEmail("C:\\Scrape\\metrics.xlsm");
			this.clear();
			this.sentEmail = true;
		}
		return sentEmail;
	}
}
