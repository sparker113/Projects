import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import exceltransfer.DataNames;

public class FracSummary {
	EvaluatedDataObject evaluatedDataObject;
	HashMap<String, String> configMap;
	int invalidDateCount = 0;
	Exception exception = new Exception();
	FracSummary(HashMap<String, String> configMap) throws Exception {
		this.configMap = configMap;
		checkConfigMap();
		setDataObject();
	}
	void checkConfigMap() throws Exception{
		for(String s:configMap.values()) {
			if(s.equals(FracSummaryFrame.INVALID_INPUT)) {
				throw exception;
			}
		}
	}
	void setDataObject() throws IOException, ClassNotFoundException {
		String crew = configMap.get(FracSummaryFrame.CREW);
		if (crew.equals(FracSummaryFrame.ALL_CREWS)) {
			evaluatedDataObject = filterDataObject(EvaluatedDataObject.getFromFile());
			return;
		}
		evaluatedDataObject = filterDataObject(EvaluatedDataObject.getCrewFile(crew));
	}
	EvaluatedDataObject getFilteredDataObject() {
		return evaluatedDataObject;
	}
	EvaluatedDataObject getStageDataObject(EvaluatedDataObject evaluatedDataObject, String wellName, Integer stage)
			throws IOException {
		EvaluatedDataObject stageDataObject = new EvaluatedDataObject();
		stageDataObject.addToMaps(evaluatedDataObject.getSummaryMap(wellName, stage),
				evaluatedDataObject.getChemSandMap(wellName, stage), evaluatedDataObject.getSigValsMap(wellName, stage),
				wellName, stage);
		return stageDataObject;
	}

	EvaluatedDataObject filterDataObject(EvaluatedDataObject evaluatedDataObject){
		HashMap<String,ArrayList<Integer>> wellStageMap = getWellStageMap(evaluatedDataObject);
		EvaluatedDataObject filterObject = new EvaluatedDataObject();
		for(String s:wellStageMap.keySet()) {
			wellStageMap.get(s).forEach((Integer stage)->{
				try {
					filterObject.addToMaps(getStageDataObject(evaluatedDataObject,s,stage));
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}
		return filterObject;
	}

	boolean validDate(String date) {
		return date.matches("\\d{4}\\-\\d{2}\\-\\d{2}");
	}

	void addToWellStageMap(HashMap<String, ArrayList<Integer>> wellStageMap, String wellName, Integer stage) {
		if (!wellStageMap.containsKey(wellName)) {
			wellStageMap.put(wellName, new ArrayList<Integer>());
		}
		wellStageMap.get(wellName).add(stage);
	}

	HashMap<String, ArrayList<Integer>> getWellStageMap(EvaluatedDataObject evaluatedDataObject) {
		LocalDate startDate = LocalDate.parse(configMap.get(FracSummaryFrame.START_DATE));
		LocalDate endDate = LocalDate.parse(configMap.get(FracSummaryFrame.END_DATE));

		HashMap<String, ArrayList<Integer>> wellStageMap = new HashMap<>();
		evaluatedDataObject.getSigValsMaps().entrySet()
				.forEach((Map.Entry<String, HashMap<Integer, LinkedHashMap<String, String>>> wellEntry) -> {
					HashMap<Integer, LinkedHashMap<String, String>> map = wellEntry.getValue();
					for (Map.Entry<Integer, LinkedHashMap<String, String>> entry : map.entrySet()) {
						String stageEnd = entry.getValue().get(DataNames.END_DATE);
						if (!validDate(stageEnd)) {
							invalidDateCount++;
							continue;
						}
						LocalDate stageDate = LocalDate.parse(stageEnd);
						if (stageDate.isAfter(startDate) & stageDate.isBefore(endDate)) {
							addToWellStageMap(wellStageMap, wellEntry.getKey(), entry.getKey());
						}
					}
				});
		return wellStageMap;
	}

}
