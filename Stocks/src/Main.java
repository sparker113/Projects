import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.awt.Color;
import data.Nasdaq;
import graphpanel.PlotFrame;

public class Main {

	
	public static void main(String... args) throws Exception {
		File stockFile = getStockFile("ABCL");
		System.out.println(stockFile.getAbsolutePath());
		@SuppressWarnings("unchecked")
		Map<LocalDate,Map<String,String>> map = (Map<LocalDate,Map<String,String>>)readObjFromFile(stockFile.getAbsolutePath());
		ArrayList<Float> array = getNumericArrayFromMap(map,Nasdaq.Historic.CLOSE.getValue(),true);
		System.out.println(array);
		PlotFrame plotFrame = new PlotFrame(Set.of(array),getArrayOfSet(map.keySet(),true));
		plotFrame.setBackground(Color.GRAY);
	}
	public static LocalDate getFirstDate(Map<LocalDate,?> map){
		Object[] localDates = map.keySet().toArray();
		return (LocalDate)localDates[localDates.length-1];
	}
	public static <T> ArrayList<T> getArrayOfSet(Set<T> set,boolean reversed){
		ArrayList<T> array = new ArrayList<>();
		set.forEach(t->{
			array.add(reversed?0:array.size(),t);
		});
		return array;
	}
	public final static String STOCK_FILE_EXT = ".map";
	public static File getStockFile(String ticker){
		File file = Paths.get(DATA_DIR,ticker,ticker+STOCK_FILE_EXT).toFile();
		return file;
	}
	public static File getAnyStockFile() throws IOException{
		File file = Paths.get(DATA_DIR).toFile();
		return getAnyStockFile(file);
	}
	public static File getAnyStockFile(File file){
		if(file.isFile()){
			return file;
		}else if(file.listFiles().length==0){
			return null;
		}
		File stockFile = checkListFiles(file.listFiles());
		return stockFile;
	}
	public static File checkListFiles(File[] files){
		if(files.length==0){
			return null;
		}
		for(File file:files){
			if(file.isFile()){
				return file;
			}else{
				return checkListFiles(file.listFiles());
			}
		}
		return null;
	}
	public static <K,T> ArrayList<Float> getNumericArrayFromMap(Map<K,Map<T,String>> map,T key,boolean reversed){
		ArrayList<Float> array = new ArrayList<>();
		map.entrySet().forEach(entry->{
			if(!entry.getValue().containsKey(key)){
				return;
			}
			String value = entry.getValue().get(key);
			array.add(reversed?0:array.size(),getNumericValue(value));
		});
		return array;
	}
	public static Float getNumericValue(String string){
		Matcher matcher = Pattern.compile("\\-?[0-9]+(\\.[0-9]+)?").matcher(string);
		if(matcher.find()){
			return Float.valueOf(matcher.group());
		}
		return 0f;
	}
	public static boolean isNumeric(String string){
		Matcher matcher = Pattern.compile("[^0-9\\-\\.]").matcher(string);
		return !matcher.find();
	}
	public static void seperateMapFile(Map<String,Map<LocalDate,Map<String,String>>> map){
		makeDataDir(DATA_DIR);
		map.forEach((String ticker,Map<LocalDate,Map<String,String>> dataMap) ->{
			writeObjToFile(dataMap,getStockDataFilePath(ticker));
		});
	}
	public static String getStockDataFilePath(String ticker){
		Path path = Paths.get(DATA_DIR,ticker,ticker+".map");
		File file = path.toFile();
		if(!file.exists()){
			file.getParentFile().mkdirs();
		}
		return path.toString();
	}
	public final static String DATA_DIR = "data";
	public static void makeDataDir(String dirPath){
		Path path = Paths.get(dirPath);
		File file = path.toFile();
		file.mkdirs();
	}
	public final static String HISTORIC_MAP_FILE = "historicMap.map";
	public static <T> void writeObjToFile(T obj,String fileName){
		try(ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(new File(fileName)))){
			objectOutputStream.writeObject(obj);
		}catch(IOException e){
			e.printStackTrace();
			return;
		}
		
	}
	
	
	public static Object readObjFromFile(String fileName){
		try(ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(fileName))){
			Object object = objectInputStream.readObject();
			return object;
		}catch(IOException | ClassNotFoundException e){
			e.printStackTrace();
		}
		return null;
	}
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
	public static String getStringBetweenChars(String string, char char1, char char2) {
		int first = string.indexOf(char1);
		int second = string.indexOf(char2, first);
		return string.substring(first + 1, second);
	}

	public final static String RESPONSE_FILE_PATH = "response.txt";

	public static void appendTextToFile(String text, String absFilePath) {
		try (FileChannel fileChannel = getFileChannel(absFilePath)) {
			ByteBuffer byteBuffer = ByteBuffer.wrap(text.getBytes());
			fileChannel.write(byteBuffer);
			fileChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static String getRuntimePath() {
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
        scanner.close();
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
