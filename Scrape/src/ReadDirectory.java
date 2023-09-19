import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;

public class ReadDirectory {
	public ReadDirectory() {

	}

	public static String readDirect() throws IOException {
		String directory = new String();
		int ind = 0;
		int ind2 = 0;
		ArrayList<File> jobFiles = new ArrayList<>();
		File cDrive;
		BufferedReader bufferedReader = new BufferedReader(new FileReader("C:\\Scrape\\directory.txt"));
		directory = bufferedReader.readLine();
		if (directory == null) {
			JOptionPane.showMessageDialog(null, "Configure the path for the current" + " Pad's folder");

		} else if (!Files.exists(Path.of(directory))) {
			File file = new File(directory);

			if (!file.exists()) {
				JOptionPane.showMessageDialog(null, "Configure the path for the current" + " Pad's folder");
				directory = "C:\\";
			}
		}
		bufferedReader.close();
		return directory;
	}

	public static String readTRDirect(String wellName) throws IOException {
		String wellDirectory = readDirect() + "\\" + wellName + " - TR.xlsm";
		return wellDirectory;
	}

	public static String readCSVDirect(String wellName, String stage) throws IOException {
		String stageString = "00" + stage;
		stageString = stageString.substring(stageString.length() - 3);
		File file = RedTreatmentReport.findDir(new File(ReadDirectory.readDirect()),
				mainFrame.removeSpecialCharacters(wellName + " - Stage " + stageString + ".csv"));
		return file.getAbsolutePath();
	}

	public static HashMap<String, String> readCSVChannels() throws IOException {
		HashMap<String, String> map = new HashMap<>();
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(new FileInputStream(new File("C:\\Scrape\\csvchannels.txt"))));
		String temp;
		while ((temp = bufferedReader.readLine()) != null) {
			map.put(temp.split("_")[0], temp.split("_")[1]);
		}
		return map;
	}
}
