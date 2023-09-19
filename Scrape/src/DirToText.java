import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DirToText {
	private String dir;

	public DirToText(String dir) {
		this.dir = dir;
	}

	public void sendIt() throws IOException {
		File file = new File("C:\\Scrape\\directory.txt");
		FileWriter fileWriter = new FileWriter(file);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		bufferedWriter.append(dir);
		bufferedWriter.flush();
		bufferedWriter.close();
	}
}
