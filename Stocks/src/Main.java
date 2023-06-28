import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.aayushatharva.brotli4j.decoder.Decoder;
import com.aayushatharva.brotli4j.decoder.DirectDecompress;

import data.Nasdaq;

public class Main {


	public static void main(String... args) throws Exception {
		Nasdaq nasdaq = Nasdaq.getAllStockData();
		nasdaq.setHistoricData(10);
	}

	public static String getStringBetweenChars(String string, char char1, char char2) {
		int first = string.indexOf(char1);
		int second = string.indexOf(char2, first);
		return string.substring(first + 1, second);
	}

	public final static String RESPONSE_FILE_PATH = "response.txt";

	private static void appendTextToFile(String text, String absFilePath) {
		try (FileChannel fileChannel = getFileChannel(absFilePath)) {
			ByteBuffer byteBuffer = ByteBuffer.wrap(text.getBytes());
			fileChannel.write(byteBuffer);
			fileChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	public final static String HISTORIC_MAP_FILE = "historicMap.map";
	@SuppressWarnings("unchecked")
	public static Map<String,Map<LocalDate,Map<String,String>>> readMapFromFile(String fileName) {
		Map<String,Map<LocalDate,Map<String,String>>> map = null;
		try(ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(HISTORIC_MAP_FILE))){
			map = (Map<String,Map<LocalDate,Map<String,String>>>)objectInputStream.readObject();
		}catch(IOException | ClassNotFoundException e){
			e.printStackTrace();
		}
		return map;
	}
	private static String getRuntimePath() {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(new String[] { "cmd", "/c", "cd" });
		} catch (IOException e) {
			return "C:\\";
		}
		InputStream inputStream = process.getInputStream();
		Scanner scanner = new Scanner(inputStream);
		String runtimePath = "";
		while (scanner.hasNext()) {
			runtimePath += scanner.next();
		}

		return "file:/" + runtimePath.replace("\\", "/");
	}

	private static FileChannel getFileChannel(String absFilePath) throws IOException {
		Path path = Path.of(URI.create(absFilePath));
		FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
				StandardOpenOption.APPEND);
		return fileChannel;
	}



		public static byte[] getNonNullBytes(byte[] byteArray, int length) {
		byte[] bytes = new byte[length];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = byteArray[i];
		}
		return bytes;
	}

	public static int getNonNullSize(byte[] bytes) {
		int count = 0;
		for (byte b : bytes) {
			count += b == 0 ? 0 : 1;
		}
		return count;
	}

	public static LocalDateTime getEpochTime(long numUnits, TemporalUnit unit) {
		LocalDateTime datumDate = LocalDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.of(0, 0));
		LocalDateTime dateTime = datumDate.plus(numUnits, unit);
		return dateTime;
	}

	public static LocalDateTime getNowPlus(long numUnits, TemporalUnit unit) {
		return LocalDateTime.now().plus(numUnits, unit);
	}

	public static long getEpochSecondsForDateTime(LocalDateTime localDateTime) {
		return Duration.between(getDatumDateTime(), localDateTime).toSeconds();
	}

	public static LocalDateTime getDatumDateTime() {
		return LocalDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.of(0, 0));
	}

	public static CookieManager getCookieHandler() {
		CookieManager.setDefault(CookieHandler.getDefault());
		CookieManager cookieManager = new CookieManager();
		return cookieManager;
	}

		public static String getStringFromBytes(byte[] bytes) {

		StringBuilder stringBuilder = new StringBuilder();
		for (byte b : bytes) {
			if (b == 0) {
				return stringBuilder.toString();
			}
			stringBuilder.append((char) b);
		}
		return stringBuilder.toString();
	}

	public static ArrayList<ZipEntry> getZipEntries(ZipInputStream zipInputStream) throws IOException {
		ArrayList<ZipEntry> array = new ArrayList<>();
		ZipEntry zipEntry;
		while ((zipEntry = zipInputStream.getNextEntry()) != null) {
			zipEntry.setMethod(ZipEntry.DEFLATED);
			array.add(zipEntry);
		}
		return array;
	}

	public static ZipInputStream decompressZip(InputStream inputStream) throws IOException {
		return new ZipInputStream(inputStream);
	}

		public static void printStream(InputStream inputStream) throws IOException {
		byte[] bytes = new byte[2048];
		int i;
		while ((i = inputStream.read(bytes)) > -1) {
			DirectDecompress directDecompress = Decoder.decompress(bytes);

			System.out.println(getStringFromBytes(directDecompress.getDecompressedData()));
		}
	}

	public static String[] getHeadersString(Map<String, List<String>> headers) {
		String[] strings = new String[headers.size() * 2];
		int[] count = { 0 };
		headers.entrySet().forEach(entry -> {
			if (entry.getKey() == null || entry.getKey().equals("Connection")) {
				return;
			}
			strings[count[0]] = entry.getKey() == "Set-Cookie" ? "Cookie" : entry.getKey();
			count[0]++;
			strings[count[0]] = getListString(entry.getValue());
			count[0]++;
		});

		return trimArray(strings);
	}

	public static String[] trimArray(String[] strings) {
		String[] newArray = new String[strings.length - countNull(strings)];
		var count = 0;
		for (String s : strings) {
			if (s == null) {
				return newArray;
			}
			newArray[count] = s;
			count++;
		}
		return newArray;
	}

	public static int countNull(Object[] objects) {
		int count = 0;
		for (Object o : objects) {
			count = o == null ? count + 1 : count;
		}
		return count;
	}

	public static void printArray(String[] strings) {
		for (int i = 0; i < strings.length; i += 2) {
			System.out.print(strings[i]);
			System.out.println(" : " + strings[i + 1]);
		}
	}

	public static String getListString(List<String> list) {
		String string = "";
		for (String s : list) {
			string += "," + s;
		}
		return string.substring(1);
	}
}
