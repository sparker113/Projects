import java.io.IOException;
import java.util.ArrayList;

public class StageArguments implements Runnable {
	Thread t;
	private ArrayList<String> stageArguments = new ArrayList<>();

	StageArguments(String filePath, String well, String stage, String coState, String formation, String myEmail,
			String toEmail, String wellDriveUser, String wellDrivePW) {
		t = new Thread(this, "Stage Arguments");
		stageArguments.add(filePath);
		stageArguments.add(well);
		stageArguments.add(stage);
		stageArguments.add(coState);
		stageArguments.add(formation);
		stageArguments.add(myEmail);
		stageArguments.add(toEmail);
		stageArguments.add(wellDriveUser);
		stageArguments.add(wellDrivePW);
		t.start();
	}

	public String additionalEmails(String toEmail, ArrayList<String> additionalEmails) {
		String addEmails = "";
		for (String s : additionalEmails) {
			addEmails += ";" + s;
		}
		if (toEmail.charAt(toEmail.length()) == ';') {
			return toEmail + addEmails.substring(1);
		}
		return toEmail + addEmails;
	}

	@Override
	public void run() {
		ArgumentsToText pythonArgs = new ArgumentsToText();
		try {
			ArgumentsToText.writeArguments(stageArguments, "C:\\Scrape\\ScrapePython\\Stage Arguments.txt", "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
