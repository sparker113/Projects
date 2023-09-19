import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JOptionPane;

public class Channels {
	private Boolean channelsSet;

	Channels() {
		this.channelsSet = false;
	}

	channelData channels = new channelData();

	public synchronized void setChannels(String at, String wellID, String treatmentID) {

		HttpClient client = HttpClient.newHttpClient();
		HttpRequest req = (HttpRequest.newBuilder().header("Content-type", "application/json; charset=utf-8")
				.header("Accept", "application/json").header("Authorization", at)).GET()
				.uri(URI.create("https://api.fracpro.ai:4000/api/v1/wells/" + wellID + "/treatments/" + treatmentID
						+ "/channels"))
				.build();
		HttpResponse<InputStream> resp = null;
		try {
			resp = client.send(req, HttpResponse.BodyHandlers.ofInputStream());
		} catch (IOException | InterruptedException e) {
			try {
				new TextLog("Channels - line 27");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		int ii = 0;
		Scanner hm = null;
		try {
			hm = new Scanner(resp.body());
		} catch (NullPointerException e) {
			JOptionPane.showMessageDialog(null, "Re-select a stage");
		}

		ArrayList<String> oName = new ArrayList<>();
		ArrayList<String> name = new ArrayList<>();
		ArrayList<String> cName = new ArrayList<>();
		hm.useDelimiter("\\{");
		while (hm.hasNext()) {
			for (String a : hm.next().split(",")) {
				if (a.contains("originalName")) {
					oName.add(a.split(":")[1].replace("\"", ""));
				} else if (a.contains("name")) {
					name.add(a.split(":")[1].replace("\"", ""));
				} else if (a.contains("cName")) {
					cName.add(a.split(":")[1].replace("\"", ""));
				}
			}
		}
		hm.close();
		channels.setOriginalName(oName);
		channels.setcName(cName);
		channels.setcOName(cName, oName);
		channels.setName(name);
		channelsSet = true;
		notify();
		GrantAccess.clearSSLCache(client);

	}

	public synchronized channelData getChannels() {
		while (!channelsSet)
			try {
				wait();
			} catch (InterruptedException e) {
			}
		return channels;
	}
}
