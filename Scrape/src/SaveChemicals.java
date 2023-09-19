import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JTable;

public class SaveChemicals {
	private ArrayList<String> addType;

	public SaveChemicals(ArrayList<String> addType) {
		this.addType = addType;
	}

	public SaveChemicals() {
	}

	public void writeChemicals(ArrayList<String> addType) throws IOException {
		FileWriter fileWriter = new FileWriter("C:\\Scrape\\chemicals.txt");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		int i = 0;
		for (String chem : addType) {
			if (i == 0) {
				bufferedWriter.write(chem);
				bufferedWriter.append("\n");
			} else {
				bufferedWriter.append(chem + "\n");

			}
		}
		bufferedWriter.close();
	}

	public static ArrayList<String> readChemicals(JTable table) throws IOException {
		ArrayList<String> addType = new ArrayList<>();
		FileReader fileReader = new FileReader(new File("C:\\Scrape\\chemicals.txt"));
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String temp;
		while ((temp = bufferedReader.readLine()) != null) {
			addType.add(temp);
		}
		int i = 0;
		for (String s : addType) {
			table.setValueAt(s, i, 0);
			i++;
		}
		return addType;
	}
}
