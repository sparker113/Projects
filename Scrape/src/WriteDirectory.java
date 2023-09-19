import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;

public class WriteDirectory {
	private String chosenDir;

	public WriteDirectory() throws HeadlessException, IOException {
		String directory = "";
		JFileChooser fileDialog = new JFileChooser();
		try {
			String readDir = ReadDirectory.readDirect();
			directory = setDefaultDir(readDir);
		} catch (IOException e) {
			directory = "C:\\";
		}
		fileDialog.setCurrentDirectory(new File(directory));
		fileDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int selected = fileDialog.showOpenDialog(null);
		if (selected == JFileChooser.APPROVE_OPTION) {
			chosenDir = fileDialog.getSelectedFile().getAbsolutePath();
		} else {
			chosenDir = ReadDirectory.readDirect();
		}
		System.out.println(chosenDir);
	}

	public String getDirectory() {
		return this.chosenDir;
	}

	public void sendDirectory() throws IOException {
		DirToText textFileDirectory = new DirToText(chosenDir);
		textFileDirectory.sendIt();
	}

	public String setDefaultDir(String directory) {
		String reducedDir = directory.substring(0, directory.lastIndexOf("\\"));
		return reducedDir;
	}

}
