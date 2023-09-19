import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.swing.JTable;

public class EvaluatedDataObject implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 4926141571900515623L;
	SummaryMaps summaryMaps = new SummaryMaps();
	ChemSandMaps chemSandMaps = new ChemSandMaps();
	SigValsMaps sigValsMaps = new SigValsMaps();

	EvaluatedDataObject() {

	}

	public void addToMaps(EvaluatedDataObject evaluatedDataObject) {
		addToSummaryMaps(evaluatedDataObject.getSummaryMaps());
		addToChemSandMaps(evaluatedDataObject.getChemSandMaps());
		addToSigValsMaps(evaluatedDataObject.getSigValsMaps());
	}

	public void addToSummaryMaps(HashMap<String, HashMap<Integer, HashMap<Integer, ArrayList<String>>>> sumMapAppend) {
		for (String s : sumMapAppend.keySet()) {
			if (summaryMaps.containsKey(s)) {
				summaryMaps.get(s).putAll(sumMapAppend.get(s));
				continue;
			}
			summaryMaps.put(s, sumMapAppend.get(s));
		}
	}

	public void addToChemSandMaps(
			HashMap<String, HashMap<Integer, HashMap<String, LinkedHashMap<String, String>>>> chemSandAppend) {
		for (String s : chemSandAppend.keySet()) {
			if (chemSandMaps.containsKey(s)) {
				chemSandMaps.get(s).putAll(chemSandAppend.get(s));
				continue;
			}
			chemSandMaps.put(s, chemSandAppend.get(s));
		}
	}

	public void addToSigValsMaps(HashMap<String, HashMap<Integer, LinkedHashMap<String, String>>> sigValsAppend) {
		for (String s : sigValsAppend.keySet()) {
			if (sigValsMaps.containsKey(s)) {
				sigValsMaps.get(s).putAll(sigValsAppend.get(s));
				continue;
			}
			sigValsMaps.put(s, sigValsAppend.get(s));
		}
	}

	public void addToMaps(HashMap<Integer, ArrayList<String>> summaryMap,
			HashMap<String, LinkedHashMap<String, String>> chemSandMap, LinkedHashMap<String, String> sigValsMap,
			String wellName, Integer stage) throws IOException {
		summaryMaps.addMap(wellName, stage, summaryMap);
		chemSandMaps.addMap(wellName, stage, chemSandMap);
		sigValsMaps.addMap(wellName, stage, sigValsMap);
	}

	public HashMap<String, HashMap<Integer, HashMap<Integer, ArrayList<String>>>> getSummaryMaps() {
		return summaryMaps;
	}

	public HashMap<String, HashMap<Integer, HashMap<String, LinkedHashMap<String, String>>>> getChemSandMaps() {
		return chemSandMaps;
	}

	public HashMap<String, HashMap<Integer, LinkedHashMap<String, String>>> getSigValsMaps() {
		return sigValsMaps;
	}

	public HashMap<Integer, ArrayList<String>> getSummaryMap(String wellName, Integer stage) {
		return summaryMaps.get(wellName).get(stage);
	}

	public HashMap<String, LinkedHashMap<String, String>> getChemSandMap(String wellName, Integer stage) {
		return chemSandMaps.get(wellName).get(stage);
	}

	public LinkedHashMap<String, String> getSigValsMap(String wellName, Integer stage) {
		return sigValsMaps.get(wellName).get(stage);
	}

	private void writeSummary(JTable table, String wellName, Integer stage) {
		HashMap<Integer, ArrayList<String>> summaryMap = summaryMaps.get(wellName).get(stage);
		for (Integer i : summaryMap.keySet()) {
			int count = 0;
			for (String s : summaryMap.get(i)) {
				table.setValueAt(s, count, i);
				count++;
			}
		}
	}

	private void writeSandMap(JTable table, String wellName, Integer stage) {
		LinkedHashMap<String, String> sandMap = chemSandMaps.get(wellName).get(stage).get("sand");
		int i = 0;
		for (String s : sandMap.keySet()) {
			table.setValueAt(s, i, 0);
			table.setValueAt(sandMap.get(s), i, 1);
			i++;
		}
	}

	private void writeChemMap(JTable table, String wellName, Integer stage) {
		LinkedHashMap<String, String> chemMap = chemSandMaps.get(wellName).get(stage).get("chemicals");
		LinkedHashMap<String, String> chemUnitsMap = chemSandMaps.get(wellName).get(stage).get("chemUnits");
		int i = 0;
		for (String s : chemMap.keySet()) {
			table.setValueAt(s, i, 0);
			table.setValueAt(chemMap.get(s), i, 1);
			if (chemUnitsMap != null) {
				table.setValueAt(chemUnitsMap.get(s), i, 2);
			}
			i++;
		}
	}

	private void writeSigValsMap(JTable table, String wellName, Integer stage) {
		LinkedHashMap<String, String> sigValsMap = sigValsMaps.get(wellName).get(stage);
		int i = 0;
		for (String s : sigValsMap.keySet()) {
			table.setValueAt(s, i, 0);
			table.setValueAt(sigValsMap.get(s), i, 1);
			i++;
		}
	}

	private void writeFluidVolumes(JTable table, String wellName, Integer stage) {
		LinkedHashMap<String, String> sigValsMap = sigValsMaps.get(wellName).get(stage);
		table.setValueAt(sigValsMap.get("Clean Total"), 0, 0);
		table.setValueAt(sigValsMap.get("Slurry Total"), 0, 1);
	}

	public void retrieveStage(JTable mTable, JTable chemTable, JTable sigTable, JTable sandTable, JTable volumesTable,
			String wellName, Integer stage) {
		writeSummary(mTable, wellName, stage);
		writeSandMap(sandTable, wellName, stage);
		writeChemMap(chemTable, wellName, stage);
		writeSigValsMap(sigTable, wellName, stage);
		writeFluidVolumes(volumesTable, wellName, stage);
	}




	public static void writeToFile(EvaluatedDataObject evaluatedDataObject) throws IOException {
		FileOutputStream fileOutputStream = null;
		File file = new File("C:\\Scrape\\data.his");
		fileOutputStream = new FileOutputStream(file);
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
		objectOutputStream.writeObject(evaluatedDataObject);
		objectOutputStream.close();
	}

	public final static String BASE_PATH = "C:\\Scrape\\histDataObjects\\";

	public static EvaluatedDataObject getFromFile() throws IOException, ClassNotFoundException {
		if (!checkFile()) {
			System.out.println("New EvaluatedDataObject Returned");
			return new EvaluatedDataObject();
		}
		FileInputStream fileInputStream = new FileInputStream("C:\\Scrape\\data.his");
		ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
		EvaluatedDataObject evaluatedDataObject = (EvaluatedDataObject) objectInputStream.readObject();
		fileInputStream.close();
		objectInputStream.close();
		return evaluatedDataObject;
	}

	public static EvaluatedDataObject getCrewFile(String crew) throws IOException, ClassNotFoundException {
		String path = BASE_PATH + crew + "\\data.his";
		if (!checkFile(path)) {
			System.out.println("New EvaluatedDataObject Returned");
			return new EvaluatedDataObject();
		}
		FileInputStream fileInputStream = new FileInputStream(path);
		ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
		EvaluatedDataObject evaluatedDataObject = (EvaluatedDataObject) objectInputStream.readObject();
		fileInputStream.close();
		objectInputStream.close();
		return evaluatedDataObject;
	}

	private static Boolean checkFile(String path) {
		System.out.println(path);
		File file = new File(path);
		return file.exists();
	}

	public static EvaluatedDataObject getFromFile(String path) throws IOException, ClassNotFoundException {
		if (!checkFile(path)) {
			System.out.println("File does not exist");
			return null;
		}
		System.out.println(path);
		FileInputStream fileInputStream = new FileInputStream(path);
		ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
		EvaluatedDataObject evaluatedDataObject = (EvaluatedDataObject) objectInputStream.readObject();
		fileInputStream.close();
		objectInputStream.close();
		return evaluatedDataObject;
	}

	private static Boolean checkFile() {
		File file = new File("C:\\Scrape\\data.his");
		return file.exists();
	}

	private class SummaryMaps extends HashMap<String, HashMap<Integer, HashMap<Integer, ArrayList<String>>>>
			implements Serializable {
		/**
		 *
		 */
		private static final long serialVersionUID = 6726934859743382268L;

		public void addMap(String well, Integer stage, HashMap<Integer, ArrayList<String>> summaryMap) {
			if (this.containsKey(well)) {
				this.get(well).put(stage, summaryMap);
			} else {
				this.put(well, new HashMap<Integer, HashMap<Integer, ArrayList<String>>>());
				this.get(well).put(stage, summaryMap);
			}
		}
	}

	private class ChemSandMaps extends HashMap<String, HashMap<Integer, HashMap<String, LinkedHashMap<String, String>>>>
			implements Serializable {
		/**
		 *
		 */
		private static final long serialVersionUID = 8703255372132692049L;

		public void addMap(String well, Integer stage, HashMap<String, LinkedHashMap<String, String>> chemSandMap) {
			if (this.containsKey(well)) {
				this.get(well).put(stage, chemSandMap);
			} else {
				this.put(well, new HashMap<Integer, HashMap<String, LinkedHashMap<String, String>>>());
				this.get(well).put(stage, chemSandMap);
			}
		}
	}

	private class SigValsMaps extends HashMap<String, HashMap<Integer, LinkedHashMap<String, String>>>
			implements Serializable {
		/**
		 *
		 */
		private static final long serialVersionUID = -4691490448264494921L;

		public void addMap(String well, Integer stage, LinkedHashMap<String, String> sigValsMap) {
			if (this.containsKey(well)) {
				this.get(well).put(stage, sigValsMap);
			} else {
				this.put(well, new HashMap<Integer, LinkedHashMap<String, String>>());
				this.get(well).put(stage, sigValsMap);
			}
		}
	}

}
