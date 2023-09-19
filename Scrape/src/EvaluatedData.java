import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import exceltransfer.DataNames;

public class EvaluatedData implements Runnable {
	HashMap<Integer, ArrayList<String>> summaryMap;
	HashMap<String, LinkedHashMap<String, String>> chemSandMap;
	Map<String, String> sigValsMap;
	LinkedHashMap<String, ArrayList<String>> dataMap;
	String crew;

	EvaluatedData(HashMap<Integer, ArrayList<String>> summaryMap,
			HashMap<String, LinkedHashMap<String, String>> chemSandMap, Map<String, String> sigValsMap, String crew,
			LinkedHashMap<String, ArrayList<String>> dataMap) throws IOException {
		this.summaryMap = summaryMap;
		this.chemSandMap = chemSandMap;
		this.sigValsMap = sigValsMap;
		this.dataMap = dataMap;
		this.crew = crew;
	}

	@Override
	public void run() {
		String dir = getDirectory();
		configDir(dir);
		File file = getFile(dir);
		try {
			FileWriter fileWriter = getFileWriter(file);
			writeSigVals(fileWriter);
			writeChemSand(fileWriter);
			writeTreatmentSummary(fileWriter);
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			System.out.println("Exception caught EvaluatedData");
		}
	}

	private FileWriter getFileWriter(File file) throws IOException {
		FileWriter fileWriter = new FileWriter(file);
		fileWriter.write("");
		return fileWriter;
	}

	private File getFile(String dir) {
		String wellName = sigValsMap.get("Well Name");
		String stageNumber = "00" + sigValsMap.get("Stage Number");
		File file = new File(dir + "\\" + wellName + " - " + stageNumber.substring(stageNumber.length() - 3) + ".txt");
		return file;
	}

	private void writeTreatmentSummary(FileWriter fileWriter) throws IOException {
		ArrayList<String> names = DataNames.getSummaryColumnNames();
		for (Integer i : summaryMap.keySet()) {
			fileWriter.append(getCamelCase(names.get(i)));
			fileWriter.append(":{");
			String writeString = "";
			for (String s : summaryMap.get(i)) {
				writeString += "," + s;
			}
			writeString += "}\n";
			fileWriter.append(writeString.substring(1));
		}
	}

	private String getCamelCase(String s) {
		Matcher matcher = Pattern.compile("\\s[A-Za-z]").matcher(s);
		String camelCase = s.toLowerCase();

		while (matcher.find()) {
			String found = matcher.group();
			int end = matcher.end();
			int start = matcher.start();
			camelCase = camelCase.substring(0, start).toLowerCase() + found.trim().toUpperCase()
					+ camelCase.substring(end).toLowerCase();
			matcher = Pattern.compile("\\s[A-Za-z]").matcher(camelCase);
		}
		return camelCase;
	}

	private void writeChemSand(FileWriter fileWriter) throws IOException {
		for (String s : chemSandMap.keySet()) {
			fileWriter.append(s);
			fileWriter.append(":{");
			String writeString = "";
			for (String ss : chemSandMap.get(s).keySet()) {
				writeString += "," + ss + ":" + chemSandMap.get(s).get(ss);
			}
			writeString += "}";
			fileWriter.append(writeString.substring(1));
			fileWriter.append("\n");
		}
	}

	private void writeSigVals(FileWriter fileWriter) throws IOException {
		fileWriter.append("sigValues:{");
		for (String s : sigValsMap.keySet()) {
			fileWriter.append(s);
			fileWriter.append(":");
			fileWriter.append(sigValsMap.get(s));
			fileWriter.append(",");
		}
		fileWriter.append("}");
		fileWriter.append("\n");
	}

	private String getDirectory() {
		String wellName = sigValsMap.get("Well Name");
		return "C:\\Scrape\\HistoricData\\" + crew + "\\" + wellName;
	}

	private void configDir(String path) {
		Stack<String> stack = new Stack<>();
		File file = new File(path);
		while (!file.exists()) {
			stack.push(file.getAbsolutePath());
			file = file.getParentFile();
		}
		if (stack.size() > 0) {
			createPath(stack);
		}
	}

	private void createPath(Stack<String> stack) {
		while (stack.size() > 0) {
			File file = new File(stack.pop());
			file.mkdir();
		}
	}
}
