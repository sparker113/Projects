import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ChannelToTxt {
	private ArrayList<String> channelData;
	private String fileName;

	public ChannelToTxt(ArrayList<String> channelData, String fileName) {
		this.channelData = channelData;
		this.fileName = fileName;
	}

	public synchronized void execute() {
		FileWriter fileWriter = null;
		String temp;
		BufferedWriter bufferedWriter = null;
		try {
			fileWriter = new FileWriter(new File(fileName));
			bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write("");
			int i = 0;
			for (String a : channelData) {
				if (i > 0) {
					bufferedWriter.append(a);
					bufferedWriter.newLine();
				} else {
					i = 1;
				}
			}
			fileWriter.flush();
			bufferedWriter.flush();
			fileWriter.close();
			bufferedWriter.close();
		} catch (IOException e) {
			return;
		}

	}
}
