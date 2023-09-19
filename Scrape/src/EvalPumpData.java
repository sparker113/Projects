import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.http.HttpTimeoutException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

import intelie.CrewRequest;
import intelie.DataRequest;
import intelie.RememberMe;
import joblog.JobLogWells;
import joblog.LoginRequest;
import joblog.WellsRequest;
import login.EncryptCredentials;
import login.UserNamePassword;

public class EvalPumpData implements Serializable {

	EvalPumpData() {

	}

	public static Integer getIndex() {
		Integer index = 0;
		try {
			ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(new File("index.txt")));
			index = (Integer) objectInputStream.readObject();
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("NONOONO");
			return index;
		}
		return index;
	}

	public static void writeIndex(Integer index) {
		try {
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(new File("index.txt")));
			objectOutputStream.writeObject(Integer.valueOf(index));
			objectOutputStream.close();
		} catch (IOException e) {
			System.out.println("NONONON");
		}
	}

	public static Integer countTotalObjects(Map<String, LinkedHashMap<String, ArrayList<String>>> mainPumpData) {
		int count = 0;
		for (String s : mainPumpData.keySet()) {
			count += mainPumpData.get(s).size();
		}
		return count;
	}

	public final static String MAIN_PUMP_AVERAGES_FILENAME = "main_pump_averages.map";

	public static void writeAveragesToFile(HashMap<String, HashMap<String, String>> map, String fileName)
			throws IOException {
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(
				new FileOutputStream(new File(MAIN_PUMP_AVERAGES_FILENAME)));
		objectOutputStream.writeObject(map);
		objectOutputStream.flush();
		objectOutputStream.close();
	}

	@SuppressWarnings("unchecked")
	public static HashMap<String, LinkedHashMap<String, ArrayList<String>>> getMainPumpDataMap()
			throws IOException, ClassNotFoundException {
		ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(new File(MAIN_DATA_FILENAME)));
		HashMap<String, LinkedHashMap<String, ArrayList<String>>> mainDataMap = (HashMap<String, LinkedHashMap<String, ArrayList<String>>>) objectInputStream
				.readObject();
		return mainDataMap;
	}

	public static void setPumpAveragesAndSave() throws IOException, ClassNotFoundException {
		HashMap<String, LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>>> pumpDataMap = getPumpDataMap(
				PUMP_DATA_MAP_FILENAME);
		HashMap<String, HashMap<String, HashMap<String, String>>> pumpDataAveragesMap = new HashMap<>();
		for (String s : pumpDataMap.keySet()) {
			pumpDataAveragesMap.put(s, setAveragesMap(pumpDataMap.get(s)));
		}
		savePumpAveragesMap(pumpDataAveragesMap, PUMP_DATA_AVERAGES_FILENAME);
	}

	@SuppressWarnings("unchecked")
	public static void getPumpAverages(String fileName) throws IOException, ClassNotFoundException {
		ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(new File(fileName)));
		HashMap<String, HashMap<String, HashMap<String, String>>> pumpDataAveragesMap = (HashMap<String, HashMap<String, HashMap<String, String>>>) objectInputStream
				.readObject();
		objectInputStream.close();
	}

	public final static String PUMP_DATA_AVERAGES_FILENAME = "pump_data_averages.map";

	public static void savePumpAveragesMap(
			HashMap<String, HashMap<String, HashMap<String, String>>> pumpDataAveragesMap, String fileName)
			throws IOException {
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(new File(fileName)));
		objectOutputStream.writeObject(pumpDataAveragesMap);
		objectOutputStream.flush();
		objectOutputStream.close();
	}

	@SuppressWarnings("unchecked")
	public static HashMap<String, LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>>> getPumpDataMap(
			String fileName) throws IOException, ClassNotFoundException {
		ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(new File(fileName)));
		HashMap<String, LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>>> pumpDataMap = (HashMap<String, LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>>>) objectInputStream
				.readObject();
		objectInputStream.close();
		return pumpDataMap;
	}

	public static void setAveragesAndSave() throws IOException, ClassNotFoundException {
		HashMap<String, LinkedHashMap<String, ArrayList<String>>> mainDataMap = getMainPumpDataMap();
		HashMap<String, HashMap<String, String>> averagesMap = setAveragesMap(mainDataMap);
		writeAveragesToFile(averagesMap, MAIN_PUMP_AVERAGES_FILENAME);
	}

	public static HashMap<String, HashMap<String, String>> setAveragesMap(
			Map<String, LinkedHashMap<String, ArrayList<String>>> mainPumpData) {
		ExecutorService executor = Executors.newCachedThreadPool();
		Integer count = countTotalObjects(mainPumpData);
		Semaphore semaphore = new Semaphore(0);
		HashMap<String, HashMap<String, String>> averagesMap = new HashMap<>();
		for (String s : mainPumpData.keySet()) {
			for (String chan : mainPumpData.get(s).keySet()) {
				Matcher matcher = Pattern.compile("date|timestamp|crew").matcher(chan.toLowerCase());
				if (matcher.find() || mainPumpData.get(s).get(chan) == null || mainPumpData.get(s).get(chan).isEmpty()) {
					semaphore.release();
					continue;
				}
				executor.execute(() -> {
					try {
						Double dAverage = UserDefinedFrame.avg(mainPumpData.get(s).get(chan));
						addToAveragesMap(averagesMap, s, chan, String.valueOf(dAverage));
					} catch (Exception e1) {
						e1.printStackTrace();
						semaphore.release();
						return;
					}
					semaphore.release();
				});
			}
		}
		System.out.println("Waiting on: " + count + " Permits - " + semaphore.availablePermits() + " Available");
		try {
			semaphore.acquire(count);
		} catch (InterruptedException e) {
			e.printStackTrace();
			return averagesMap;
		}
		return averagesMap;
	}

	public synchronized static void addToAveragesMap(HashMap<String, HashMap<String, String>> averagesMap, String key1,
			String key2, String average) {
		averagesMap.put(key1, new HashMap<String, String>());
		averagesMap.get(key1).put(key2, average);
	}

	public final static String MAIN_DATA_FILENAME = "blender_disch_manifold.map";

	public static void aggregateMainData(HashMap<String, HashMap<Integer, LinkedHashMap<String, String>>> sigValsMap,
			String fileName, String... channels) throws IOException, InterruptedException, ClassNotFoundException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ShortBufferException {
		CrewRequest crewRequest = new CrewRequest();
		RememberMe rememberMe = new RememberMe();
		// String cookies = "remember-me=" + rememberMe.getCookie() + "; " +
		// crewRequest.getSessionId();
		String token = crewRequest.getToken();
		HashMap<String, String> crewMap = petroIQCrews();
		HashMap<String, LinkedHashMap<String, ArrayList<String>>> mainDataMap = new HashMap<>();
		DataRequest dataRequest = null;
		ExecutorService executor = Executors.newFixedThreadPool(10);
		int index = getIndex();
		for (String well : sigValsMap.keySet()) {
			String crew = crewMap.get(findWell(crewMap, well));

			if (crew.equals("")) {
				System.out.println("Well Not Found: " + well);
				continue;
			}
			for (Integer i : sigValsMap.get(well).keySet()) {
				String key = well + " - Stage " + i;

				try {
					String postBody = DataRequest.getPostBody(crewRequest.getNormCrew(crew.replace(" ", "")),
							getFormattedTime(sigValsMap.get(well).get(i), START),
							getFormattedTime(sigValsMap.get(well).get(i), END), toArrayObject(channels));
					dataRequest = new DataRequest(token, crewRequest.getSessionId(), postBody, channels.length,
							rememberMe.getCookie());
					dataRequest.setRequestString(postBody);
					LinkedHashMap<String, ArrayList<String>> temp = dataRequest.makeRequest(executor).get(60,
							TimeUnit.SECONDS);
					System.out.println(temp);
					mainDataMap.put(key, temp);
					System.out.println("Done Making Request");
				} catch (HttpTimeoutException e1) {
					System.out.println("aggregateData::HttpTimeoutException->line 80");
					writeMapToFile(mainDataMap, fileName);
					continue;
				} catch (Exception e) {
					writeMapToFile(mainDataMap, fileName);
					continue;
				}

			}
		}
		writeMapToFile(mainDataMap, fileName);
	}

	public static ArrayList<String> toArrayObject(String[] primArray) {
		ArrayList<String> array = new ArrayList<>();
		for (String s : primArray) {
			array.add(s);
		}
		return array;
	}

	public static void aggregateData(HashMap<String, HashMap<Integer, LinkedHashMap<String, String>>> sigValsMap,
			String... channels) throws IOException, InterruptedException, ClassNotFoundException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ShortBufferException {
		CrewRequest crewRequest = new CrewRequest();
		RememberMe rememberMe = new RememberMe();
		String cookies = "remember-me=" + rememberMe.getCookie() + "; " + crewRequest.getSessionId();
		String token = crewRequest.getToken();
		HashMap<String, String> crewMap = petroIQCrews();
		HashMap<String, LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>>> pumpDataMap = new HashMap<>();
		ExecutorService executor = Executors.newCachedThreadPool();
		int index = getIndex();
		for (String well : sigValsMap.keySet()) {
			String crew = crewMap.get(findWell(crewMap, well));

			if (crew.equals("")) {
				System.out.println("Well Not Found: " + well);
				continue;
			}
			for (Integer i : sigValsMap.get(well).keySet()) {
				String key = well + " - Stage " + i;

				System.out.println("Making Request For Pumps");
				HashSet<String> pumps = DataRequest.getPumpsRequest(crewRequest.getNormMap(), crew.replace(" ", "-"),
						cookies, token, getFormattedTime(sigValsMap.get(well).get(i), START),
						getFormattedTime(sigValsMap.get(well).get(i), END));
				if (pumps.isEmpty()) {
					continue;
				}
				System.out.println("Pumps Acquired");
				try {

					System.out.println("Making Request");
					pumpDataMap.put(key,
							DataRequest.pumpDataRequestFuture(crew.replace(" ", "-"), cookies, token,
									getFormattedTime(sigValsMap.get(well).get(i), START),
									getFormattedTime(sigValsMap.get(well).get(i), END), pumps, executor, index,
									channels).get(20l, TimeUnit.SECONDS));
					System.out.println("Done Making Request");
				} catch (HttpTimeoutException e1) {
					System.out.println("aggregateData::HttpTimeoutException->line 80");
					writeMapToFile(pumpDataMap, "pump_data_map.map");

					continue;
				} catch (Exception e) {
					writeMapToFile(pumpDataMap, "pump_data_map.map");
					index = DataRequest.getRightIndex(crew.replace(" ", "-"), cookies, token,
							getFormattedTime(sigValsMap.get(well).get(i), START),
							getFormattedTime(sigValsMap.get(well).get(i), END), pumps, executor, 0, channels);
					writeIndex(index);
					System.out.println("THE CORRECT INDEX IS: " + index);
					continue;
				}

			}
		}
		try {
			writeMapToFile(pumpDataMap, "pump_data_map.map");

		} catch (IOException e) {
			System.out.println("Could not write map to file");
		}
	}

	public final static String PUMP_DATA_MAP_FILENAME = "pump_data_map.map";

	public static void writeMapToFile(HashMap<String, ?> map, String fileName)
			throws FileNotFoundException, IOException {
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(new File(fileName)));
		objectOutputStream.writeObject(map);
		objectOutputStream.flush();
		objectOutputStream.close();
	}

	public static String findWell(HashMap<String, String> jobLogWellNames, String savedWellName) {
		for (String s : jobLogWellNames.keySet()) {
			String sFixed = mainFrame.removeSpecialCharacters(s).toLowerCase().replace(" ", "");
			String savedFixed = mainFrame.removeSpecialCharacters(savedWellName).toLowerCase().replace(" ", "");
			System.out.println(sFixed + " - " + savedFixed);
			if (sFixed.equals(savedFixed)) {
				return s;
			}
		}
		return "";
	}

	private final static String START = "Start";
	private final static String END = "End";

	public static String getFormattedTime(LinkedHashMap<String, String> valsMap, String startEnd) {
		return valsMap.get(startEnd + " Date") + " " + valsMap.get(startEnd + " Time");
	}

	public static HashMap<String, String> petroIQCrews() throws IOException, InterruptedException, InvalidKeyException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ShortBufferException {
		HashMap<String,String> creds = EncryptCredentials.getUserCredentials(LoginRequest.PETRO_IQ_CRED_PATH,"PetroIQ Login");
		String token = new LoginRequest(creds.get(UserNamePassword.USERNAME),creds.get(UserNamePassword.PASSWORD)).getToken();
		joblog.CrewRequest crewRequest = new joblog.CrewRequest(token);
		JobLogWells jobLogWells = WellsRequest.setWellMap(token);
		HashMap<String, String> crewMap = new HashMap<>();
		HashMap<String, HashMap<String, String>> jobLogCrewMap = jobLogWells.getCrewMap();
		for (String wellID : jobLogCrewMap.keySet()) {
			System.out.println(jobLogCrewMap.get(wellID));
			crewMap.put(jobLogWells.getWellName(wellID), jobLogCrewMap.get(wellID).get("name"));
		}

		return crewMap;
	}
}
