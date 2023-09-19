import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JTable;

public class EmailBody implements Runnable {
	JTable table;
	JTable dT1;
	JTable dT3;
	String lastClose;
	Thread t;

	EmailBody(JTable table, JTable dT1, JTable dT3, String lastClose) {
		this.table = table;
		this.dT1 = dT1;
		this.dT3 = dT3;
		this.lastClose = lastClose;
		t = new Thread(this, "Email Body");
		t.start();
	}

	public void writeEmailBody() throws IOException {
		String emailBody = getEmailBody(table, dT1, dT3, lastClose);
		FileWriter fileWriter = new FileWriter("C:\\Scrape\\ScrapePython\\Email\\body.txt");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		Scanner scan = new Scanner(emailBody);
		scan.useDelimiter("\n");
		bufferedWriter.write("");
		while (scan.hasNext()) {
			bufferedWriter.append(scan.next());
			bufferedWriter.newLine();
		}
		bufferedWriter.flush();
		bufferedWriter.close();
	}

	public static String getEmailBody(JTable table, JTable dT1, JTable dT3, String lastClose) {
		StringBuilder emailBody = new StringBuilder();
		emailBody.append("Last Close: ");
		emailBody.append(lastClose);
		emailBody.append("\n");
		emailBody.append("Start Time: ");
		emailBody.append(String.valueOf(table.getValueAt(0, 0)));
		emailBody.append("\n");
		int i;
		for (i = 0; i < table.getRowCount(); i++) {
			if (String.valueOf(table.getValueAt(i + 1, 1)) == "null"
					|| String.valueOf(table.getValueAt(i + 1, 1)) == "") {
				emailBody.append("End Time: ");
				emailBody.append(String.valueOf(table.getValueAt(i, 1)));
				emailBody.append("\n");
				break;
			}
		}
		i = 0;
		while (String.valueOf(dT1.getValueAt(i, 0)) != "null" && String.valueOf(dT1.getValueAt(i, 0)) != "") {
			emailBody.append(String.valueOf(dT1.getValueAt(i, 0)));
			emailBody.append(": ");
			emailBody.append(String.valueOf(dT1.getValueAt(i, 1)));
			emailBody.append("\n");
			i++;
		}
		i = 0;
		while (String.valueOf(dT3.getValueAt(i, 0)) != "null" && String.valueOf(dT3.getValueAt(i, 0)) != "") {
			emailBody.append(String.valueOf(dT3.getValueAt(i, 0)));
			emailBody.append(": ");
			emailBody.append(String.valueOf(dT3.getValueAt(i, 1)));
			emailBody.append("\n");
			i++;
		}

		return emailBody.toString();
	}

	@Override
	public void run() {
		try {
			writeEmailBody();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
