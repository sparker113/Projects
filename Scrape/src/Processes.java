import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class Processes {

	public static InputStream getStream(Process process) {
		return process.getInputStream();
	}

	public static void waitAndDestroy(Process process) throws IOException {

		BufferedReader bufferedReader = process.errorReader();
		String temp;
		while ((temp = bufferedReader.readLine()) != null) {
			System.out.println(temp);
		}
		System.out.println("Process Destroyed");
	}

	public static void treatmentPDF() {
		try {
			Process process = Runtime.getRuntime().exec(new String[]{"cmd","/c","start C:\\Scrape\\ScrapePython\\TRpdf.exe"});
			InputStream stream = process.getInputStream();
			Scanner scanner = new Scanner(stream);
			while (scanner.hasNext()) {
				System.out.println(scanner.next());
			}
			process.destroy();
		} catch (IOException e) {
			System.out.println("Son of a bitch");
		}
	}

	public static void getBridgeStream() throws IOException {
		Executors.newSingleThreadExecutor().execute(() -> {
			Process process;
			try {
				process = Runtime.getRuntime()
						.exec(new String[] { "cmd.exe", "/c", "java", "-jar", "C:\\Scrape\\scrape_v.1.3.jar" });// ScrapePython\\jvm\\lib\\JavaClassBridge69.jar"});
				InputStream stream = process.getInputStream();
				byte[] bytes = new byte[128];
				int i;
				while ((i = stream.read(bytes)) > -1) {
					System.out.println(getStringFromBytes(bytes));

				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	public static String getStringFromBytes(byte[] bytes) {
		StringBuilder stringBuilder = new StringBuilder();
		for (byte b : bytes) {
			stringBuilder.append((char) b);
		}
		return stringBuilder.toString();
	}

	public static Runnable plotSettings() {
		Runnable runnable = () -> {
			try {
				Process process = Runtime.getRuntime().exec(new String[]{"cmd","/c","start C:\\Scrape\\ScrapePython\\PlotSettings.exe"});
				InputStream stream = getStream(process);
				waitAndDestroy(process);
			} catch (IOException e) {
				System.out.println("Exception caught executing PlotSettings.exe");
			}
		};
		return runnable;
	}

	public static Runnable jobSetup() throws IOException {
		Runnable runnable = () -> {
			try {
				Process process = Runtime.getRuntime().exec(new String[]{"cmd","/c","start C:\\Scrape\\ScrapePython\\JobSetup.exe"});
				InputStream stream = getStream(process);
				waitAndDestroy(process);
			} catch (IOException e) {
			}
		};
		return runnable;
	}

	public static Runnable wellDrive() throws IOException {
		Runnable runnable = () -> {
			try {
				Process process = Runtime.getRuntime().exec(new String[]{"cmd","/c","start C:\\Scrape\\ScrapePython\\Welldrive_mainframe.exe"});
				InputStream stream = getStream(process);
				getFilePane();
				waitAndDestroy(process);
			} catch (IOException e) {
			}
		};
		return runnable;
	}
	public static Runnable engineLoad() throws IOException {
		Runnable runnable = () -> {
			try {
				Process process = Runtime.getRuntime().exec(new String[]{"cmd","/c","C:\\Scrape\\ScrapePython\\engineload.exe"});
				InputStream stream = getStream(process);
				waitAndDestroy(process);
			} catch (IOException e) {
				e.printStackTrace();
			}
		};
		return runnable;
	}

	public static Runnable taskMaker() throws IOException {
		Runnable runnable = () -> {
			try {
				Process process = Runtime.getRuntime().exec(new String[]{"cmd","/c","start C:\\Scrape\\ScrapePython\\TaskMaker.exe"});
				InputStream stream = getStream(process);
				waitAndDestroy(process);
			} catch (IOException e) {
			}
		};
		return runnable;
	}

	public static Runnable completedTasks() throws IOException {
		Runnable runnable = () -> {
			try {
				Process process = Runtime.getRuntime().exec(new String[]{"cmd","/c", "start C:\\Scrape\\ScrapePython\\CompleteTasksTK.exe"});
				InputStream stream = getStream(process);
				waitAndDestroy(process);
			} catch (IOException e) {
			}
		};
		return runnable;
	}

	public static Runnable intelieCSV() throws IOException {
		Runnable runnable = () -> {
			try {
				Process process = Runtime.getRuntime().exec(new String[]{"cmd","/c","start C:\\Scrape\\ScrapePython\\InteliePullMF.exe"});
				InputStream stream = getStream(process);
				waitAndDestroy(process);
			} catch (IOException e) {
			}
		};
		return runnable;
	}

	public static Runnable waterAnalysis() throws IOException {
		Runnable runnable = () -> {
			try {
				Process process = Runtime.getRuntime().exec(new String[]{"cmd","/c","start C:\\Scrape\\ScrapePython\\WAoutlookScrape.exe"});
				InputStream stream = getStream(process);
				waitAndDestroy(process);
			} catch (IOException e) {
			}
		};
		return runnable;
	}

	public static Runnable email() throws IOException {
		Runnable runnable = () -> {
			try {
				Process process = Runtime.getRuntime().exec(new String[]{"cmd","/c","start C:\\Scrape\\ScrapePython\\CMemail.exe"});
				InputStream stream = getStream(process);
				waitAndDestroy(process);
				JOptionPane.showMessageDialog(Main.yess, "Company Man email sent");
			} catch (IOException e) {
			}
		};
		return runnable;
	}

	public static Runnable processCSV() {
		Runnable runnable = () -> {
			try {
				Process process = Runtime.getRuntime().exec(new String[]{"cmd","/c","start C:\\Scrape\\ScrapePython\\CSV.exe"});
				waitAndDestroy(process);
			} catch (IOException e) {
				System.out.println("Exception caught while saving CSV");
			}
		};
		return runnable;
	}

	public static AbstractAction getProcessAction(Runnable runnable) {
		AbstractAction action = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ExecutorService executor = Executors.newCachedThreadPool();
				executor.execute(runnable);
			}
		};
		return action;
	}

	public static JFileChooser getFilePane() throws IOException {
		JFileChooser fileChooser = new JFileChooser();
		String directory = ReadDirectory.readDirect();
		Image imageIcon = new ImageIcon("C:\\Scrape\\Scrape.png").getImage().getScaledInstance(64, 64,
				Image.SCALE_SMOOTH);

		fileChooser.setDialogTitle("Drag and Drop Files");
		fileChooser.setCurrentDirectory(new File(directory));
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setVisible(true);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setDragEnabled(true);
		fileChooser.showDialog(null, "Close");

		return fileChooser;
	}

}
