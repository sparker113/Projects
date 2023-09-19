package joblog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JobLogWells {
	private HashMap<String, HashMap<String, String>> wellMap = new HashMap<>();
	private HashMap<String, HashMap<String, String>> padMap = new HashMap<>();
	private HashMap<String, HashMap<String, String>> operatorMap = new HashMap<>();
	private HashMap<String, HashMap<String, String>> crewMap = new HashMap<>();
	private HashMap<String, String> selectedWellMap = new HashMap<>();
	private HashMap<String, String> formationMap = new HashMap<>();
	private HashMap<String, String> countiesMap = new HashMap<>();
	private ArrayList<String> idArray = new ArrayList<>();
	private HashMap<String, String> wellNames = new HashMap<>();
	private HashMap<String, HashMap<String, HashMap<String, String>>> activeMap = new HashMap<>();
	private HashMap<String, HashMap<String, ArrayList<String>>> activePerfsMap = new HashMap<>();
	private String token;
	private Semaphore countySem = new Semaphore(0);

	public void addToWellMap(String id, String json) {
		idArray.add(id);
		HashMap<String, String> tempMap = new HashMap<>();
		for (String s : json.split(",")) {
			if (s.split(":").length < 2) {
				continue;
			}
			tempMap.put(s.split(":")[0].replace("\"", "").replace("\\{", ""), s.split(":")[1].replace("\"", ""));
		}
		if (id.equals("3985")) {
			System.out.println("sam");
		}
		wellMap.put(id, tempMap);
	}

	public String getToken() {
		return this.token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setCountiesMap() {
		this.countiesMap = CountiesRequest.getCountiesMap(getToken());
	}

	public String getCountyById(String wellId) {
		return countiesMap.get(wellMap.get(wellId).get("countyId"));
	}

	public String getCounty(String wellName) {
		return countiesMap.get(wellMap.get(getIdFromWell(wellName)).get("countyId"));
	}

	public void addToPadMap(String id, String json) {
		HashMap<String, String> tempMap = new HashMap<>();
		for (String s : json.split(",")) {
			tempMap.put(s.split(":")[0].replace("\"", "").replace("\\{", ""), s.split(":")[1].replace("\"", ""));
		}
		if (padMap.containsKey(id)) {
			padMap.put(id + "::2", tempMap);
			// addToWellNames(id+"::2");
			return;
		}
		padMap.put(id, tempMap);
		addToWellNames(id);
	}

	public void addToOperatorMap(String id, String json) {
		if (json.isEmpty()) {
			return;
		}
		HashMap<String, String> tempMap = new HashMap<>();
		for (String s : json.split(",")) {
			if (s.matches("((.+?):$)|([^\\:]+)")) {
				continue;
			}
			if (json.contains("Customer\":null")) {
				System.out.println("sam");
			}
			if (!tempMap.containsKey(s.split(":")[0].replace("\"", ""))) {
				tempMap.put(s.split(":")[0].replace("\"", "").replace("\\{", ""), s.split(":")[1].replace("\"", ""));
			}
		}
		if (operatorMap.containsKey(id)) {
			operatorMap.put(id + "::2", tempMap);
			return;
		}
		operatorMap.put(id, tempMap);
	}

	public HashMap<String, String> getFormationMap() {
		return this.formationMap;
	}

	public void setFormationMap(String token) {
		String json = "";
		try {
			json = WellsRequest.formationRequest(token);
		} catch (IOException | InterruptedException e) {
			System.out.println("NONONOONONONOON");
		}
		Matcher matcher = Pattern.compile("\"id\":(\\d+),\"name\":\"((\\w|\\s|\\d)+)\"").matcher(json);
		while (matcher.find()) {
			String found = matcher.group();
			formationMap.put(found.split(",")[0].split(":")[1], found.split(",")[1].split(":")[1].replace("\"", ""));
		}
	}

	public void addToCrewMap(String id, String json) {
		HashMap<String, String> tempMap = new HashMap<>();
		ArrayList<String> crewArray = getCrews("name", json);
		tempMap = parseCrewJSonArray(id, json);

		addArrayValuesToMap("name", crewArray, tempMap);
		crewMap.put(id, tempMap);
		checkActiveStatus(id);
	}

	private ArrayList<String> getCrews(String key, String json) {
		ArrayList<String> array = new ArrayList<>();
		Matcher matcher = Pattern.compile("\"" + key + "\":\"((.*?))\"").matcher(json);
		while (matcher.find()) {
			String found = matcher.group();
			array.add(found.substring(found.indexOf(':') + 1).replace("\"", ""));
		}
		return array;
	}

	private void addArrayValuesToMap(String key, ArrayList<String> values, HashMap<String, String> map) {
		String value = "";
		for (String s : values) {
			value += "," + s;
		}
		map.put(key, value.substring(1));
	}

	private HashMap<String, String> parseCrewJSonArray(String id, String json) {
		HashMap<String, String> tempMap = new HashMap<>();
		Matcher matcher = Pattern.compile("\"(\\w+)\":(\"?)\\b(.+?)[^\\w\\s]").matcher(json);
		while (matcher.find()) {
			String found = matcher.group();
			String key = found.split(":")[0].replace("\"", "");
			String value = found.split(":")[1].replace("\"", "");

			tempMap.put(found.split(":")[0].replace("\"", ""), found.split(":")[1].replace("\"", ""));
		}
		return tempMap;
	}

	public void setSelectedWellMap(String token, String wellId) throws InterruptedException, IOException {
		String json = WellsRequest.singleWellRequest(token, wellId);
		selectedWellMap = new HashMap<>();
		Matcher matcher = Pattern.compile("\"(\\w+)\":((\\w|\\d\\|\"|\\s)+)").matcher(json);
		while (matcher.find()) {
			String found = matcher.group();
			selectedWellMap.put(found.split(":")[0].replace("\"", ""), found.split(":")[1].replace("\"", ""));
		}
	}

	public HashMap<String, String> getSelectedWellMap() {
		return this.selectedWellMap;
	}

	private synchronized void addToActiveMap(String id) {
		HashMap<String, HashMap<String, String>> tempMap = new HashMap<>();
		tempMap.put("well", wellMap.get(id));
		tempMap.put("pad", padMap.get(id));
		tempMap.put("operator", operatorMap.get(id));
		tempMap.put("crew", crewMap.get(id));
		activeMap.put(getWellName(id), tempMap);
	}

	public synchronized void addToActivePerfsMap(ArrayList<ArrayList<Integer>> stageIndeces,
			ArrayList<ArrayList<Integer>> topIndeces, ArrayList<ArrayList<Integer>> bottomIndeces, String id,
			String json) {
		ArrayList<String> stageValues = getValuesFromIndeces(json, stageIndeces);
		ArrayList<String> topValues = getValuesFromIndeces(json, topIndeces);
		ArrayList<String> bottomValues = getValuesFromIndeces(json, bottomIndeces);
		HashMap<String, ArrayList<String>> perfsMap = new HashMap<>();
		perfsMap.put("stages", stageValues);
		perfsMap.put("topPerfs", topValues);
		perfsMap.put("bottomPerfs", bottomValues);
		activePerfsMap.put(id, perfsMap);
	}

	public synchronized HashMap<String, HashMap<String, ArrayList<String>>> getActivePerfsMap() {
		return this.activePerfsMap;
	}

	public ArrayList<String> getValuesFromIndeces(String json, ArrayList<ArrayList<Integer>> indexArray) {
		ArrayList<String> valuesArray = new ArrayList<>();
		for (int i = 0; i < indexArray.get(0).size(); i++) {
			valuesArray.add(json.substring(indexArray.get(0).get(i), indexArray.get(1).get(i)).split(":")[1]);
		}
		return valuesArray;
	}

	private Boolean checkActiveStatus(String id) {
		if (wellMap.get(id).get("active").equals("true")) {
			addToActiveMap(id);
			return true;
		}
		return false;
	}

	public static ArrayList<String> getArrayOfKeys(Map<String, ?> map) {
		ArrayList<String> keys = new ArrayList<>();
		for (String s : map.keySet()) {
			keys.add(s);
		}
		return keys;
	}

	public synchronized HashMap<String, HashMap<String, HashMap<String, String>>> getActiveMap() {
		return this.activeMap;
	}

	public ArrayList<String> getActiveWellsByCrew(String crew) {
		ArrayList<String> wells = new ArrayList<>();
		for (String s : crewMap.keySet()) {
			if (crewMap.get(s).get("name").toUpperCase().equals(crew.toUpperCase())) {
				wells.add(getWellName(s));
			}
		}
		return wells;
	}

	public static ArrayList<String> getActiveWellsByCrew(HashMap<String, HashMap<String, HashMap<String, String>>> map,
			String crew) {
		ArrayList<String> wells = new ArrayList<>();
		for (String s : map.keySet()) {

			if (map.get(s).get("crew").get("name").toUpperCase().replace("-", "").replace("_", "").replace(" ", "")
					.contains(crew.toUpperCase())) {
				wells.add(s);
			}
		}
		return wells;
	}

	private void addToWellNames(String id) {
		wellNames.put(id, getWellName(id));
	}

	public synchronized String getWellName(String id) {
		String wellName = wellMap.get(id).get("name").trim();
		String padName = padMap.get(id).get("name");

		Matcher matcher = Pattern.compile("\\D$").matcher(padName);
		Matcher wellMatcher = Pattern.compile("^\\d").matcher(wellName);
		Boolean pad = matcher.find();
		Boolean well = wellMatcher.find();
		if (pad | (!pad & well)) {
			return padName + " " + wellName;
		} else {
			return padName + wellName;
		}
	}

	public synchronized HashMap<String, String> getWellNames() {
		return this.wellNames;
	}

	public synchronized String getOperator(String wellName) {
		if (!operatorMap.containsKey(getIdFromWell(wellName))) {
			return "EOG Resources";
		}
		return operatorMap.get(getIdFromWell(wellName)).get("name");
	}

	public synchronized String getIdFromWell(String wellName) {
		for (Map.Entry<String, String> entry : wellNames.entrySet()) {
			if (removeSpecialCharacters(entry.getValue()).toLowerCase().trim()
					.equals(removeSpecialCharacters(wellName).toLowerCase().trim())) {
				return entry.getKey();
			}
		}
		return "";
	}

	public static String removeSpecialCharacters(String string) {
		Matcher matcher = Pattern.compile("[^\\w\\d]").matcher(string);
		while (matcher.find()) {
			string = string.replace(matcher.group(), "");
			matcher.reset(string);
		}
		return string.toLowerCase();
	}

	public synchronized String getIdFromWellTest(String savedWellName) {
		String reducedString = removeSpecialCharacters(savedWellName);
		for (Map.Entry<String, String> entry : wellNames.entrySet()) {
			if (removeSpecialCharacters(entry.getValue()).equals(reducedString)) {
				return entry.getKey();
			}
		}
		return "";
	}

	public static ArrayList<String> getArrayOfValues(HashMap<String, HashMap<String, String>> map, String key) {
		ArrayList<String> array = new ArrayList<>();
		for (String s : map.keySet()) {
			if (array.contains(map.get(s).get(key))) {
				continue;
			}
			array.add(map.get(s).get(key));
		}
		return array;
	}

	public static ArrayList<String> getArrayOfValues(HashMap<String, String> map) {
		ArrayList<String> array = new ArrayList<>();
		for (String s : map.keySet()) {
			if (array.contains(map.get(s))) {
				continue;
			}
			array.add(map.get(s));
		}
		return array;
	}

	public synchronized HashMap<String, HashMap<String, String>> getWellMap() {
		return this.wellMap;
	}

	public synchronized HashMap<String, HashMap<String, String>> getPadMap() {
		return this.padMap;
	}

	public synchronized HashMap<String, HashMap<String, String>> getOperatorMap() {
		return this.operatorMap;
	}

	public synchronized HashMap<String, HashMap<String, String>> getCrewMap() {
		return this.crewMap;
	}

}
