
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChemData implements Runnable {
	private String att;
	private String wellID;
	private String treatmentID;
	private String startTime;
	private channelData channels;
	private ArrayList<String> usedChannels;

	public ChemData(String att, String wellID, String treatmentID, String startTime, channelData channels,
			ArrayList<String> usedChannels) {
		this.att = att;
		this.wellID = wellID;
		this.treatmentID = treatmentID;
		this.startTime = startTime;
		this.channels = channels;
		this.usedChannels = usedChannels;
	}

	public ChemData(channelData channels, ArrayList<String> usedChannels) {
		this.channels = channels;
		this.usedChannels = usedChannels;
	}

	public TreeMap<String, ArrayList<String>> parseChannels(channelData channels, Scanner scan) {

		TreeMap<String, ArrayList<String>> chemChannelMap = new TreeMap<>();
		ArrayList<String> index = new ArrayList<>();
		Integer i2 = 0;
		boolean end = false;
		for (String s : usedChannels) {
			ArrayList<String> tempArray = new ArrayList<>();
			Matcher match = Pattern.compile("\\d").matcher(s);
			Integer x = 0;
			if (match.find()) {
				x = Integer.valueOf(match.group());
			} else {
				continue;
			}
			if (s.toUpperCase().contains("LA")) {
				chemChannelMap.put("LA" + x, tempArray);
			} else {
				chemChannelMap.put("DA" + x, tempArray);
			}
		}
		scan.useDelimiter("\\[");
		int i = 0;
		int ii = 0;
		ArrayList<String> cChannels = new ArrayList<>();
		String temp;
		while (scan.hasNext()) {
			temp = scan.next();
			if (i == 3) {
				for (String s : temp.split(",")) {
					if (ii == temp.split(",").length - 1) {
						break;
					} else {
						cChannels.add(s.replace("\"", "").replace("]", ""));
						String figureChannel = channels.getcOName(s.replace("\"", "").replace("]", ""));
						if (figureChannel != null && !figureChannel.contains("Default")) {
							Pattern pattern = Pattern.compile("\\d");
							Matcher match = pattern.matcher(figureChannel);
							Matcher matchTypeChem = Pattern.compile("^LA").matcher(figureChannel);
							Boolean liqAdd = matchTypeChem.find();
							Integer x2 = 0;
							if (match.find()) {
								x2 = Integer.valueOf(match.group());
							}

							if (liqAdd) {
								index.add("LA" + String.valueOf(x2));
							} else {
								index.add("DA" + String.valueOf(x2));
							}

						}
					}
					ii++;
				}
				ii = 0;
			} else if (i > 3) {
				temp = temp.split("]")[0];
				ii = 0;
				i2 = 0;
				for (String s : temp.split(",")) {

					if (s.contains("}")) {
						end = true;
						break;
					}
					if (ii > 1) {
						// System.out.println(s.replace("\"","").replace("]","") + " -- " +
						// index.get(i2));
						chemChannelMap.get(index.get(i2)).add(s.replace("\"", "").replace("]", "").replace("}", ""));
						i2++;
					}

					ii++;
				}
			}
			if (end) {
				break;
			}
			i++;
		}
		for (i = 0; i < 2; i++) {
			for (String k : chemChannelMap.keySet()) {
				if (chemChannelMap.get(k).size() > 0) {
					chemChannelMap.get(k).remove(chemChannelMap.get(k).size() - 1);
				}
			}
		}
		scan.close();
		return chemChannelMap;
	}

	public static void writeChemData(ArrayList<String> chemChannel, String liqAdd) throws IOException {

		// int i = 0;
		FileWriter fileWriter = null;
		fileWriter = new FileWriter(new File("C:\\Scrape\\ScrapePython\\Plot\\" + liqAdd + ".txt"));
		fileWriter.write("");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		for (String b : chemChannel) {
			bufferedWriter.append(b);
			bufferedWriter.newLine();
		}

		bufferedWriter.flush();
		bufferedWriter.close();

	}

	@Override
	public void run() {
		System.out.println("Start ChemData Thread");
		Scanner scan = StageDataRequest.getData(att, wellID, treatmentID, startTime, channels, usedChannels);
		TreeMap<String, ArrayList<String>> chemChannels = parseChannels(channels, scan);
		for (String a : chemChannels.keySet()) {
			try {
				writeChemData(chemChannels.get(a), a);
			} catch (IOException e) {
				try {
					new TextLog("Chem Plots - line 132");
				} catch (IOException e1) {

				}
			}
		}
		System.out.println("End ChemData Thread");
	}

}
