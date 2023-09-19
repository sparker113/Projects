package materials;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SandTicketsObject implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	String padName;
	LinkedHashMap<String, HashMap<String, String>> ticketsMap;
	LinkedHashMap<String, HashMap<String, String>> duplicatesMap;
	Double deliveredSand;
	Double pumpedSand;
	Double coneVolume;
	HashMap<String, LinkedHashMap<String, String>> sandDesigns;
	HashMap<Integer, HashMap<String, Stack<String>>> stackMap;
	HashMap<String, HashMap<Integer, HashMap<String, Double>>> sandPumpedByStageAndSilo;
	LinkedHashMap<String, Double> silosDeliveredMap;
	LinkedHashMap<String, Double> silosPumpedMap;
	LinkedHashMap<String, Double> silosCurrentMap;
	LinkedHashMap<String, Double> silosPercentsMap;
	HashMap<String, Queue<String>> siloOrderQueues;
	HashMap<String, Integer> siloPriority;
	HashMap<String, Color> wellColors;
	private HashMap<String, String> sandTypeMap;
	Boolean duplicates;
	private static final String EXT = ".sto";
	private static final String PATH = "C:\\Scrape\\materials\\sandTickets\\";

	public SandTicketsObject(String padName) {
		instantiateDataObjects(padName);
		instantiateStacks();
	}

	public void instantiateDataObjects(String padName) {
		this.padName = padName;
		this.ticketsMap = new LinkedHashMap<>();
		this.duplicatesMap = new LinkedHashMap<>();
		this.duplicates = false;
		this.siloOrderQueues = new HashMap<>();
		this.sandDesigns = new HashMap<>();
		this.sandPumpedByStageAndSilo = new HashMap<>();
		this.silosDeliveredMap = new LinkedHashMap<>();
		this.silosPumpedMap = new LinkedHashMap<>();
		this.silosCurrentMap = new LinkedHashMap<>();
		this.silosPercentsMap = new LinkedHashMap<>();
		this.siloPriority = new HashMap<>();
		this.sandTypeMap = new HashMap<>();
		this.wellColors = new HashMap<>();
		this.coneVolume = 14000.0;
	}

	public Integer getSiloPriority(String silo) {
		if (siloPriority == null || !siloPriority.containsKey(silo)) {
			return 1;
		}
		return siloPriority.get(silo);
	}

	public LinkedHashMap<String, String> getWellSandDesign(String wellName) {
		if (!sandDesigns.containsKey(wellName)) {
			return null;
		}
		return sandDesigns.get(wellName);
	}

	public void addWellColor(String wellName, Color color) {
		wellColors.put(wellName, color);
	}

	public void addWellColor(Map<String, Color> wellColors) {
		this.wellColors.putAll(wellColors);
	}

	public void addToWellDesigns(String wellName, LinkedHashMap<String, String> designMap) {
		sandDesigns.put(wellName, designMap);
	}

	public void setSiloOrder(HashMap<String, ArrayList<String>> siloOrderMap) {
		siloOrderQueues = new HashMap<>();
		for (String sandType : siloOrderMap.keySet()) {
			Queue<String> queue = new LinkedList<>();
			Integer count = 1;
			for (String silo : siloOrderMap.get(sandType)) {
				siloPriority.put(silo, count);
				queue.offer(silo);
				count++;
			}
			siloOrderQueues.put(sandType, queue);
		}
	}

	public static String[] getObjectFileNames() {
		File file = new File(PATH);
		if (!file.exists()) {
			return null;
		}
		String[] fileNames = new String[file.list().length];
		int i = 0;
		for (String s : file.list()) {
			fileNames[i] = getFileWOExt(s);
		}
		return fileNames;
	}

	public static String getFileWOExt(String fileName) {
		Matcher matcher = Pattern.compile("\\.\\w+$").matcher(fileName);
		if (matcher.find()) {
			return fileName.substring(0, matcher.start());
		}
		return fileName;
	}

	public String getSandType(String silo) {
		return sandTypeMap.get(silo);
	}

	public Double getConeVolume() {
		return this.coneVolume;
	}

	public void setConeVolume(Double coneVolume) {
		this.coneVolume = coneVolume;
	}

	public static SandTicketsObject readFromFile(String padName) throws IOException, ClassNotFoundException {
		if (!checkFile(padName)) {
			return new SandTicketsObject(padName);
		}
		ObjectInputStream objectInputStream = new ObjectInputStream(
				new FileInputStream(new File(PATH + padName + EXT)));
		SandTicketsObject sandTicketsObject = (SandTicketsObject) objectInputStream.readObject();
		instantiateNullObjects(sandTicketsObject);
		return sandTicketsObject;
	}

	public static void instantiateNullObjects(SandTicketsObject sandTicketsObject) {
		if (sandTicketsObject.siloPriority == null) {
			sandTicketsObject.siloPriority = new HashMap<>();
		}
		if (sandTicketsObject.sandDesigns == null) {
			sandTicketsObject.sandDesigns = new HashMap<>();
		}
	}

	public Boolean checkTickets(LinkedHashMap<String, HashMap<String, String>> tickets) {
		if (tickets == null) {
			return false;
		}
		for (String s : tickets.keySet()) {
			if (ticketsMap.containsKey(s)) {
				duplicates = true;
				duplicatesMap.put(getDuplicateKey(s), ticketsMap.get(s));
				return true;
			}
		}
		return false;
	}

	public void clearDuplicatesMap() {
		duplicatesMap.clear();
	}

	public String getDuplicateBOLs() {
		String duplicateString = "";
		for (String s : duplicatesMap.keySet()) {
			duplicateString += "," + s;
		}
		return duplicateString.substring(1);
	}

	public LinkedHashMap<String, HashMap<String, String>> getFilteredMap(String filter, String value) {
		LinkedHashMap<String, HashMap<String, String>> map = new LinkedHashMap<>();
		if (filter.equals(SandTicketsObject.BOL) && ticketsMap.containsKey(value)) {
			map.put(value, ticketsMap.get(value));
			return map;
		} else if (filter.equals(SandTicketsObject.BOL)) {
			return map;
		}
		map.putAll(getFilteredMapNotBOL(filter, value));
		return map;
	}

	private LinkedHashMap<String, HashMap<String, String>> getFilteredMapNotBOL(String filter, String value) {
		LinkedHashMap<String, HashMap<String, String>> map = new LinkedHashMap<>();
		for (String s : ticketsMap.keySet()) {
			if (!ticketsMap.get(s).get(filter).equals(value)) {
				continue;
			}
			map.put(s, ticketsMap.get(s));
		}
		return map;
	}

	public ArrayList<String> getPossibleFilters(String filter) {
		HashMap<String, Integer> filterValueMap = new HashMap<>();
		for (String s : ticketsMap.keySet()) {
			filterValueMap.put(ticketsMap.get(s).get(filter), 1);
		}
		return getArrayOfKeys(filterValueMap);
	}

	public void addTicketsToStacks(LinkedHashMap<String, HashMap<String, String>> ticket) {
		for (String s : ticket.keySet()) {
			checkAddToStack(SandTicketsFrame.BOL, ticket.get(s).get(SHIPPER), s);
			checkAddToStack(SandTicketsFrame.PO, ticket.get(s).get(SHIPPER), ticket.get(s).get(PO));
			checkAddToStack(SandTicketsFrame.TRUCKING, ticket.get(s).get(SHIPPER), ticket.get(s).get(TRUCKING));
		}
	}

	private void checkAddToStack(Integer stackNum, String key, String value) {
		if (stackMap.get(stackNum).containsKey(key)) {
			if (stackMap.get(stackNum).get(key).elementAt(4) != null) {
				stackMap.get(stackNum).get(key).pop();
			}
			stackMap.get(stackNum).get(key).push(value);
			return;
		}
		stackMap.get(stackNum).put(key, getNewStack());
		stackMap.get(stackNum).get(key).push(value);

	}

	private Stack<String> getNewStack() {
		Stack<String> stack = new Stack<>();
		stack.setSize(5);
		return stack;
	}

	private void instantiateStacks() {
		stackMap = new HashMap<>();
		stackMap.put(SandTicketsFrame.BOL, new HashMap<String, Stack<String>>());
		stackMap.put(SandTicketsFrame.TRUCKING, new HashMap<String, Stack<String>>());
		stackMap.put(SandTicketsFrame.PO, new HashMap<String, Stack<String>>());
	}

	private ArrayList<String> getArrayOfKeys(HashMap<String, Integer> filterValueMap) {
		ArrayList<String> array = new ArrayList<>();
		for (String s : filterValueMap.keySet()) {
			array.add(s);
		}
		return array;
	}

	public String getCommonInfoString(String shipper, int jobInfoNum, Boolean withX) {
		if (shipper.isEmpty() || !stackMap.get(jobInfoNum).containsKey(shipper)) {
			return "";
		}
		String lastValue = getLastInfoValue(shipper, jobInfoNum);
		int len = lastValue.length();
		for (int i = 0; i < stackMap.get(jobInfoNum).get(shipper).size() - 1; i++) {
			if (stackMap.get(jobInfoNum).get(shipper).elementAt(i + 1) == null) {
				continue;
			}
			lastValue = getCommonString(lastValue, stackMap.get(jobInfoNum).get(shipper).get(i + 1));
		}
		if (withX) {
			return lastValue + getXs(len - lastValue.length());
		}
		return lastValue;
	}

	public String getSiloSandType(String silo) {
		String sandType = "";
		for (Map.Entry<String, HashMap<String, String>> entry : ticketsMap.entrySet()) {
			if (entry.getValue().get(SILO).equals(silo)) {
				sandType = entry.getValue().get(TYPE);
			}
		}
		return sandType;
	}

	public String getLastInfoValue(String shipper, int jobInfoNum) {
		if (!stackMap.get(jobInfoNum).containsKey(shipper) || stackMap.get(jobInfoNum).get(shipper).isEmpty()) {
			return "";
		}
		return stackMap.get(jobInfoNum).get(shipper).peek();
	}

	private String getXs(int length) {
		String xxx = "";
		for (int i = 0; i < length; i++) {
			xxx += "x";
		}
		return xxx;
	}

	private String getCommonString(String s1, String s2) {
		int len = Math.min(s1.length(), s2.length());
		StringBuilder common = new StringBuilder();
		for (int i = 0; i < len; i++) {
			char c1 = s1.charAt(i);
			char c2 = s2.charAt(i);
			if (c1 != c2) {
				break;
			}
			common.append(c1);
		}
		System.out.println(common.toString());
		return common.toString();
	}

	public LinkedHashMap<String, HashMap<String, String>> filterTickets(String filter, String key) {
		LinkedHashMap<String, HashMap<String, String>> filteredMap = new LinkedHashMap<>();
		for (String s : ticketsMap.keySet()) {
			if (ticketsMap.get(s).get(filter).toLowerCase().equals(key.toLowerCase())) {
				filteredMap.put(s, ticketsMap.get(s));
			}
		}
		return filteredMap;
	}

	public void removeTicket(String bol) {
		ticketsMap.remove(bol);
	}

	public String getDuplicateKey(String key) {
		int i = 1;
		String duplicateKey = key + "." + i;
		while (duplicatesMap.containsKey(duplicateKey)) {
			i++;
			duplicateKey = key + "." + i;
		}
		return duplicateKey;
	}

	public HashMap<String, Double> getTotalSandByKey(String key) {
		HashMap<String, Double> sandMap = new HashMap<>();
		for (String s : ticketsMap.keySet()) {
			addToSandMapByKey(key, sandMap, ticketsMap.get(s));
		}
		return sandMap;
	}

	private void addToSandMapByKey(String key, HashMap<String, Double> sandMap, HashMap<String, String> ticket) {
		if (sandMap.containsKey(ticket.get(key))) {
			sandMap.put(ticket.get(key), sandMap.get(ticket.get(key)) + Double.valueOf(ticket.get(WEIGHT)));
			return;
		}
		sandMap.put(ticket.get(key), Double.valueOf(ticket.get(WEIGHT)));
	}

	public void addTickets(LinkedHashMap<String, HashMap<String, String>> tickets) {
		addTicketsToStacks(tickets);
		ticketsMap.putAll(tickets);
		addToDeliveredSand(tickets);
		updateSiloSandTypes(tickets);
	}

	public void configNewSilosMaps() {
		silosDeliveredMap = getNewSilosDeliveredMap();
		LinkedHashMap<String, Double> newCurrentMap = new LinkedHashMap<>();
		for (String s : silosDeliveredMap.keySet()) {
			if (!silosPumpedMap.containsKey(s)) {
				newCurrentMap.put(s, silosDeliveredMap.get(s));
				continue;
			}
			newCurrentMap.put(s, silosDeliveredMap.get(s) - silosPumpedMap.get(s));
		}
		silosCurrentMap = newCurrentMap;
	}

	public LinkedHashMap<String, Double> getNewSilosDeliveredMap() {
		LinkedHashMap<String, Double> map = new LinkedHashMap<>();
		for (String s : ticketsMap.keySet()) {
			addToSandMapByKey(SILO, map, ticketsMap.get(s));
		}
		return map;
	}

	public void updateSiloSandTypes(LinkedHashMap<String, HashMap<String, String>> tickets) {
		for (String s : tickets.keySet()) {
			sandTypeMap.put(tickets.get(s).get(SILO), tickets.get(s).get(TYPE));
		}
	}

	public void addToDeliveredSand(LinkedHashMap<String, HashMap<String, String>> tickets) {
		for (String s : tickets.keySet()) {
			addToSandMapByKey(SILO, silosDeliveredMap, tickets.get(s));
			addToSandMapByKey(SILO, silosCurrentMap, tickets.get(s));
		}
	}

	@SuppressWarnings("unused")
	@Deprecated
	private Boolean removeFromSiloMap(String key, Double value, Double coneVolume) {
		if (silosCurrentMap.get(key) - value > coneVolume) {
			silosCurrentMap.put(key, silosCurrentMap.get(key) - value);
			return false;
		}
		silosCurrentMap.put(key, silosCurrentMap.get(key) - value);
		return true;
	}

	public static HashSet<String> getSandTypes(LinkedHashMap<String, HashMap<String, String>> ticketsMap) {
		HashSet<String> set = new HashSet<>();
		for (String s : ticketsMap.keySet()) {
			set.add(ticketsMap.get(s).get(TYPE));
		}
		return set;
	}

	public HashSet<String> getSandTypes() {
		HashSet<String> set = new HashSet<>();
		for (String s : ticketsMap.keySet()) {
			set.add(ticketsMap.get(s).get(TYPE));
		}
		return set;
	}

	private Double getSiloPercent(String silo, Double sandFactor, Double coneVolume) {
		double sandAboveCone = silosCurrentMap.get(silo) - coneVolume;
		return sandAboveCone > 0.0 ? sandAboveCone / sandFactor : 0.0;
	}

	private void addToSilosPumpedMap(String silo, Double pumpedSand, String wellName, Integer stage) {
		sandPumpedByStageAndSilo.get(wellName).get(stage).put(silo, pumpedSand);
		if (silosPumpedMap.containsKey(silo)) {
			silosPumpedMap.put(silo, silosPumpedMap.get(silo) + pumpedSand);
			return;
		}
		silosPumpedMap.put(silo, pumpedSand);
	}

	public void substractSandFromSilos(String sandType, Double sandUsed, String wellName, Integer stage) {
		Double siloAmt = -1.0;
		HashMap<String, Double> sandFactorMap = getSandFactorMap();
		do {
			if (siloAmt > -1.0) {
				String silo = siloOrderQueues.get(sandType).poll();
				addToSilosPumpedMap(silo, siloAmt, wellName, stage);
				wasteSilo(silo, sandType);
				if (siloAmt == 0.0) {
					break;
				}
			}
			siloAmt = silosCurrentMap.get(siloOrderQueues.get(sandType).peek());
			sandUsed -= siloAmt;
		} while (sandUsed > 0.0);
		if (siloOrderQueues.get(sandType) == null) {
			return;
		}
		String silo = siloOrderQueues.get(sandType).peek();
		addToSilosPumpedMap(silo, siloAmt + sandUsed, wellName, stage);
		updateSiloCurrentStatus(silo, sandFactorMap.get(silo));
	}

	public Double getSandInSilo(String silo) {
		return silosDeliveredMap.get(silo) - (silosPumpedMap.containsKey(silo) ? silosPumpedMap.get(silo) : 0.0);
	}

	public void updateSiloCurrentStatus(String silo, Double sandFactor) {
		silosCurrentMap.put(silo, getSandInSilo(silo));
		silosPercentsMap.put(silo, getSiloPercent(silo, sandFactor, coneVolume));
	}

	public static void resetPumpedSiloMap(SandTicketsObject sandTicketsObject) {
		sandTicketsObject.silosPumpedMap = new LinkedHashMap<>();
		for (String s : sandTicketsObject.silosCurrentMap.keySet()) {
			sandTicketsObject.updateSiloCurrentStatus(s, 3650.0);
		}
	}

	private void wasteSilo(String silo, String sandType) {
		silosPercentsMap.put(silo, 0.0);
		silosCurrentMap.put(silo, 0.0);
		siloOrderQueues.get(sandType).offer(silo);
	}

	public void addMapsToStageAndSiloMap(String wellName, Integer stage) {
		if (!sandPumpedByStageAndSilo.containsKey(wellName)) {
			sandPumpedByStageAndSilo.put(wellName, new HashMap<>());
		} else if (sandPumpedByStageAndSilo.get(wellName).containsKey(stage)) {
			updatePumpedSandMap(sandPumpedByStageAndSilo.get(wellName).get(stage));
		}
		sandPumpedByStageAndSilo.get(wellName).put(stage, new HashMap<>());
	}

	private void updatePumpedSandMap(HashMap<String, Double> stageSiloMap) {
		HashMap<String, Double> sandFactorMap = getSandFactorMap();
		for (String s : stageSiloMap.keySet()) {
			silosPumpedMap.put(s, silosPumpedMap.get(s) - stageSiloMap.get(s));
			updateSiloCurrentStatus(s, sandFactorMap.get(s));
		}
	}

	public void pumpSand(Map<String, Double> sandPumped, String wellName, Integer stage) {
		addMapsToStageAndSiloMap(wellName, stage);
		for (String s : sandPumped.keySet()) {
			substractSandFromSilos(s, sandPumped.get(s), wellName, stage);
		}
		try {
			writeToFile();
		} catch (IOException e) {
			System.out.println("NONOONOON");
		}
	}

	public void setSiloPercent(String silo, Double percent) {
		silosPercentsMap.put(silo, percent);
	}

	public LinkedHashMap<String, Double> getSiloPercentMap() {
		return this.silosPercentsMap;
	}

	private HashMap<String, Double> getSandFactorMap() {
		HashMap<String, Double> factorMap = new HashMap<>();
		for (String s : silosCurrentMap.keySet()) {
			if (silosCurrentMap.get(s) < coneVolume) {
				factorMap.put(s, 0.0);
				continue;
			}
			factorMap.put(s, (silosCurrentMap.get(s) - coneVolume) / silosPercentsMap.get(s));
		}
		return factorMap;
	}

	public void addEdittedTickets(LinkedHashMap<String, HashMap<String, String>> tickets) {
		ticketsMap.putAll(tickets);
		configNewSilosMaps();
		updateSiloSandTypes(tickets);
	}

	public HashSet<String> getSilos() {
		HashSet<String> silos = new HashSet<>();
		for (String s : ticketsMap.keySet()) {
			silos.add(ticketsMap.get(s).get(SILO));
		}
		return silos;
	}

	public void writeToFile() throws IOException {
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(checkBuildPath()));
		objectOutputStream.writeObject(this);
		objectOutputStream.close();
	}

	public File checkBuildPath() {
		File file = new File(checkAddExt(PATH + padName));
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		return file;
	}

	private static Boolean checkFile(String padName) {
		File file = new File(checkAddExt(PATH + padName));
		return file.exists();
	}

	private static String checkAddExt(String padName) {
		Matcher matcher = Pattern.compile(EXT + "$").matcher(padName);
		if (matcher.find()) {
			return padName;
		}
		return padName + EXT;
	}

	static final String SILO = "COMPARTMENT";
	static final String TYPE = "SAND TYPE";
	static final String TRUCKING = "TRUCKING COMPANY";
	static final String WEIGHT = "WEIGHT";
	static final String BOL = "B.O.L";
	static final String TRUCK = "TRUCK";
	static final String PO = "P.O.";
	static final String DATE = "DATE";
	static final String TIME = "TIME";
	static final String SHIPPER = "SHIPPER";
}
