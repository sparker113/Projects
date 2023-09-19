import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import exceltransfer.DataNames;

public class ReadOperatorTemplate {
	@SuppressWarnings("resource")
	public static HashMap<String, String> readOperatorTemplate(String operator, String fileName) throws IOException {
		HashMap<String, String> templateMap = new HashMap<>();
		BufferedReader bufferedReader;
		bufferedReader = new BufferedReader(
				new FileReader("C:\\Scrape\\Operator_Templates\\" + operator + "\\" + fileName));
		String temp;
		while ((temp = bufferedReader.readLine()) != null) {
			templateMap.put(temp.split(":")[0], temp.split(":")[1]);
		}
		return templateMap;
	}

	public static ArrayList<String> readUserDefined(String operator) throws IOException {
		String path = "C:\\Scrape\\Operator_Templates\\" + operator + "\\User_Defined.txt";
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		ArrayList<String> userDefinedArray = new ArrayList<>();
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String temp;
		while ((temp = bufferedReader.readLine()) != null) {
			userDefinedArray.add(temp);
		}
		return userDefinedArray;
	}

	public static LinkedHashMap<String, Integer> readTemplateValueNames(String operator) throws IOException {
		String path = "C:\\Scrape\\Operator_Templates\\" + operator + "\\User_Defined.txt";
		File file = new File("C:\\Scrape\\Operator_Templates\\" + operator + "\\User_Defined.txt");
		LinkedHashMap<String, Integer> sigValueNamesMap = new LinkedHashMap<>();
		Integer count = 0;
		String[] sigVals = DataNames.getDataNamesForTable();
		for (String s : sigVals) {
			sigValueNamesMap.put(s, count);
			count++;
		}

		if (file.exists()) {
			FileReader fileReader = new FileReader(path);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String temp;
			while ((temp = bufferedReader.readLine()) != null && !temp.equals("")) {
				if (!temp.toUpperCase().contains("RANGE")) {
					if (sigValueNamesMap.keySet().contains(temp.split(":")[0])) {
						continue;
					}
					sigValueNamesMap.put(temp.split(":")[0], count);
					System.out.println(count);
					count++;
				}
			}
		}
		return sigValueNamesMap;
	}

	public static LinkedHashMap<String, String> readInSigValueMap(JTable table) throws IOException {
		LinkedHashMap<String, String> sigValueMap = new LinkedHashMap<>();
		for (int i = 0; i < table.getRowCount(); i++) {
			if (table.getValueAt(i, 0) != null && String.valueOf(table.getValueAt(i, 0)) != "") {
				try {
					sigValueMap.put(table.getValueAt(i, 0).toString(), String.valueOf(table.getValueAt(i, 1)));

				} catch (Exception e) {
					JOptionPane.showMessageDialog(Main.yess,
							"Input a value for: " + String.valueOf(table.getValueAt(i, 0)));
				}
			}
		}
		return sigValueMap;
	}
}
