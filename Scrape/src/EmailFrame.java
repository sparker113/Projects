import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import org.apache.commons.math3.util.Pair;

public class EmailFrame extends JFrame {
	private int width;
	private int height;
	private JTextField myEmailField = new JTextField();
	JTable emailTable = new JTable(30, 2);
	private JButton button;
	private String fileName;

	EmailFrame(int width, int height) {
		this.width = width;
		this.height = height;
		this.setBounds(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - width / 2,
				Toolkit.getDefaultToolkit().getScreenSize().height / 2 - height / 2, width, height);
		this.setTitle("Email Settings");
		ImageIcon scrape = new ImageIcon("C:\\Scrape\\Scrape.png");
		Image scrape1 = scrape.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
		this.setIconImage(scrape1);
		JLabel myEmailLabel = constructLabel();
		try {
			constructMyEmail();
		} catch (IOException e) {
			System.out.println("Exception caught setting MyEmailField");
		}
		JScrollPane emailScroll = constructTable();
		button = constructButton();
		JPanel mainPane = constructPane(emailScroll, myEmailLabel, myEmailField, button);
		EmailPaneLayout emailPaneLayout = new EmailPaneLayout(emailScroll, myEmailLabel, myEmailField, mainPane,
				button);
		mainPane.setLayout(emailPaneLayout);
		this.add(mainPane);
		this.setVisible(false);
		this.setAlwaysOnTop(true);
		this.setResizable(false);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	EmailFrame(int width, int height, String fileName) {
		this.width = width;
		this.height = height;
		this.setBounds(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - width / 2,
				Toolkit.getDefaultToolkit().getScreenSize().height / 2 - height / 2, width, height);
		this.setTitle("Email Settings");
		ImageIcon scrape = new ImageIcon("C:\\Scrape\\Scrape.png");
		Image scrape1 = scrape.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
		this.setIconImage(scrape1);
		this.fileName = fileName;
		System.out.println(fileName);
		JLabel myEmailLabel = constructLabel();

		JScrollPane emailScroll = null;
		try {
			emailScroll = constructTable(fileName);
		} catch (IOException e) {
			System.out.println("NONONONONONO");
		}
		button = constructButton();
		JPanel mainPane = constructPane(emailScroll, myEmailLabel, myEmailField, button);
		EmailPaneLayout emailPaneLayout = new EmailPaneLayout(emailScroll, myEmailLabel, myEmailField, mainPane,
				button);
		mainPane.setLayout(emailPaneLayout);
		this.add(mainPane);
		this.setVisible(false);
		this.setAlwaysOnTop(true);
		this.setResizable(false);
	}

	private void constructMyEmail() throws IOException {
		File file = new File("C:\\Scrape\\My_Email.txt");
		if (!file.exists()) {
			myEmailField.setText("");
		} else {
			myEmailField.setText(ArgumentsToText.readStringFromFile("C:\\Scrape\\My_Email.txt"));
		}
	}

	private JLabel constructLabel() {
		JLabel myEmailLabel = new JLabel();
		myEmailLabel.setBackground(Color.LIGHT_GRAY);
		myEmailLabel.setHorizontalAlignment(SwingConstants.CENTER);
		myEmailLabel.setText("My Email");
		myEmailLabel.setOpaque(true);
		return myEmailLabel;
	}

	private HashMap<String, String> getPreviousEmails(ArrayList<String> arguments) throws IOException {
		HashMap<String, String> prevSavedMap = new HashMap<>();
		HashMap<String, String> map = CompanyManList.getCMList();
		for (String s : arguments) {
			for (Map.Entry<String, String> sEntry : map.entrySet()) {
				if (s.equals(sEntry.getValue())) {
					prevSavedMap.put(sEntry.getKey(), s);
				}
			}
		}
		return prevSavedMap;
	}

	private JScrollPane constructTable(String fileName) throws IOException {
		TableKeyPressed tableKeyPressed = new TableKeyPressed(emailTable);
		emailTable.setCellSelectionEnabled(true);

		ArrayList<String> arguments = new ArrayList<>();
		try {
			arguments = ArgumentsToText
					.readArguments("C:\\Scrape\\ScrapePython\\Email_Settings\\" + fileName + "\\email_address.txt");
		} catch (IOException e) {
			System.out.println("I/O Company Man Emails");
		}
		HashMap<String, String> prevSavedMap = getPreviousEmails(arguments);
		int i = 0;
		for (String s : prevSavedMap.keySet()) {
			emailTable.setValueAt(s, i, 0);
			emailTable.setValueAt(prevSavedMap.get(s), i, 1);
			i++;
		}
		emailTable.addKeyListener(new UpdateRepEmail());
		emailTable.getColumnModel().getColumn(0).setHeaderValue("Company Man Name");
		emailTable.getColumnModel().getColumn(1).setHeaderValue("Company Man Email");
		emailTable.setSize(width, height - 20);
		emailTable.getColumnModel().getColumn(0).setPreferredWidth(emailTable.getWidth() * 3 / 8 - 10);
		emailTable.getColumnModel().getColumn(1).setPreferredWidth(emailTable.getWidth() * 5 / 8);
		JScrollPane emailScroll = new JScrollPane(emailTable);

		return emailScroll;
	}

	private JScrollPane constructTable() {
		TableKeyPressed tableKeyPressed = new TableKeyPressed(emailTable);
		emailTable.setCellSelectionEnabled(true);

		ArgumentsToText emailsToText = new ArgumentsToText();
		ArrayList<String> arguments = new ArrayList<>();
		ArrayList<String> names = new ArrayList<>();
		try {
			arguments = ArgumentsToText.readArguments("C:\\Scrape\\CM_Emails.txt");
			names = ArgumentsToText.readArguments("C:\\Scrape\\CM_Names.txt");
		} catch (IOException e) {
			System.out.println("I/O Company Man Emails");

		}
		int i = 0;
		if (!arguments.isEmpty()) {
			for (String a : arguments) {
				emailTable.setValueAt(a, i, 1);
				i++;
			}
			i = 0;
			for (String b : names) {
				emailTable.setValueAt(b, i, 0);
				i++;
			}
		}
		emailTable.addKeyListener(new UpdateRepEmail());
		emailTable.getColumnModel().getColumn(0).setHeaderValue("Company Man Name");
		emailTable.getColumnModel().getColumn(1).setHeaderValue("Company Man Email");
		emailTable.setSize(width, height - 20);
		emailTable.getColumnModel().getColumn(0).setPreferredWidth(emailTable.getWidth() * 3 / 8 - 10);
		emailTable.getColumnModel().getColumn(1).setPreferredWidth(emailTable.getWidth() * 5 / 8);
		JScrollPane emailScroll = new JScrollPane(emailTable);

		return emailScroll;
	}

	private JPanel constructPane(JComponent... args) {
		JPanel mainPane = new JPanel();
		mainPane.setSize(width, height);
		mainPane.setBackground(Color.DARK_GRAY);
		for (JComponent j : args) {
			mainPane.add(j);
		}
		mainPane.setOpaque(true);
		return mainPane;
	}

	private JButton constructButton() {
		JButton button = new JButton();
		button.setSize(75, 25);
		button.setText("Save");
		return button;
	}

	private class EmailPaneLayout extends SpringLayout {
		EmailPaneLayout(JScrollPane emailScroll, JLabel myEmailLabel, JTextField myEmailField, JPanel mainPane,
				JButton button) {
			this.putConstraint(NORTH, myEmailLabel, 0, NORTH, mainPane);
			this.putConstraint(SOUTH, myEmailLabel, 20, NORTH, mainPane);
			this.putConstraint(WEST, myEmailLabel, 0, WEST, mainPane);
			this.putConstraint(EAST, myEmailLabel, width / 4, WEST, mainPane);
			this.putConstraint(NORTH, myEmailField, 0, NORTH, mainPane);
			this.putConstraint(SOUTH, myEmailField, 20, NORTH, mainPane);
			this.putConstraint(WEST, myEmailField, width / 4, WEST, mainPane);
			this.putConstraint(EAST, myEmailField, width, WEST, mainPane);
			this.putConstraint(NORTH, emailScroll, 20, NORTH, mainPane);
			this.putConstraint(SOUTH, emailScroll, 220, NORTH, mainPane);
			this.putConstraint(WEST, emailScroll, 0, WEST, mainPane);
			this.putConstraint(EAST, emailScroll, width - 10, WEST, mainPane);
			this.putConstraint(NORTH, button, 10, SOUTH, emailScroll);
			this.putConstraint(WEST, button, mainPane.getWidth() / 2 - 40, WEST, mainPane);
			this.putConstraint(EAST, button, mainPane.getWidth() / 2 + 35, WEST, mainPane);
			this.putConstraint(SOUTH, button, 35, SOUTH, emailScroll);
		}
	}

	public ArrayList<String> getEmails(int column) {
		ArrayList<String> arguments = new ArrayList<>();
		int i = 0;
		while (emailTable.getValueAt(i, column) != null && String.valueOf(emailTable.getValueAt(i, column)) != "") {
			arguments.add(String.valueOf(emailTable.getValueAt(i, column)));
			i++;
		}
		return arguments;
	}

	public String getMyEmail() {
		String myEmail = myEmailField.getText();
		return myEmail;
	}

	public void setButtonAction(ActionListener actionListener) {
		button.addActionListener(actionListener);
	}

	private class CompanyManList {
		CompanyManList() {
		}

		public static String checkCMList(String rep) throws IOException {
			HashMap<String, String> repsMap = getCMList();
			String repEmail = "";
			for (String a : repsMap.keySet()) {
				System.out.print(a);
				System.out.println(" - " + repsMap.get(a));
				if (a.toUpperCase().replace(" ", "").equals(rep.toUpperCase().replace(" ", ""))) {
					repEmail = repsMap.get(a);
					break;
				}
			}
			return repEmail;
		}

		public static HashMap<String, String> getCMList() throws IOException {
			FileReader fileReader = new FileReader("C:\\Scrape\\reps.txt");
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			HashMap<String, String> repsMap = new HashMap<>();
			String temp;
			while ((temp = bufferedReader.readLine()) != null) {
				if (temp.split("_").length > 1) {
					repsMap.put(temp.split("_")[0].trim(), temp.split("_")[1].trim());
				}
			}
			bufferedReader.close();
			return repsMap;
		}

		public static void appendList(Pair<String, String> newRep) throws IOException {
			HashMap<String, String> newMap = getCMList();
			File file = new File("C:\\Scrape\\reps.txt");
			FileWriter fileWriter = new FileWriter(file);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.append(newRep.getKey() + "_" + newRep.getValue());
			bufferedWriter.append("\n");

			for (String a : newMap.keySet()) {
				bufferedWriter.append(a);
				bufferedWriter.append("_");
				bufferedWriter.append(newMap.get(a));
				bufferedWriter.append("\n");
			}

			bufferedWriter.flush();
			bufferedWriter.close();
		}

		public static void writeList(HashMap<String, String> allReps) throws IOException {
			FileWriter fileWriter = new FileWriter(new File("C:\\Scrape\\reps.txt"));
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write("");
			for (String a : allReps.keySet()) {
				bufferedWriter.append(a);
				bufferedWriter.append("_");
				bufferedWriter.append(allReps.get(a));
				bufferedWriter.append("\n");
			}
			bufferedWriter.flush();
			bufferedWriter.close();
		}
	}

	public class UpdateRepEmail extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			JTable table = ((JTable) e.getSource());
			int row = ((JTable) e.getSource()).getSelectedRow();
			System.out.println(((JTable) e.getSource()).getSelectedRow());
			if (e.getKeyCode() == KeyEvent.VK_ENTER && ((JTable) e.getSource()).getSelectedRow() != -1) {

				Thread repsThread = new Thread(new Runnable() {
					@Override
					public void run() {
						if (table.getSelectedColumn() == 0) {
							String name = String.valueOf(((JTable) e.getSource()).getValueAt(row, 0));
							String email = "";
							try {
								email = CompanyManList.checkCMList(name).trim();
								System.out.println(email);
								System.out.println(name);
							} catch (IOException e1) {
								try {
									TextLog textLog = new TextLog(
											"IOException - UpdateRepEmail - " + LocalDateTime.now());
								} catch (IOException e) {
								}
							}
							((JTable) e.getSource()).setValueAt(email, row, 1);
						} else {
							String name1 = String.valueOf(table.getValueAt(row, 0));
							if (!name1.equals("")) {
								boolean check = false;
								try {
									for (String a : CompanyManList.getCMList().keySet()) {
										if (a.toUpperCase().replace(" ", "")
												.equals(name1.toUpperCase().replace(" ", ""))) {
											check = true;
											break;
										}

									}
								} catch (IOException e) {
								}
								if (check) {
									HashMap<String, String> newMap = null;
									try {
										newMap = CompanyManList.getCMList();
									} catch (IOException e) {
									}
									newMap.replace(name1, String.valueOf(table.getValueAt(row, 1)));
									try {
										CompanyManList.writeList(newMap);
									} catch (IOException e) {
									}

								} else {
									Pair<String, String> newPair = new Pair<>(name1,
											String.valueOf(table.getValueAt(row, 1)));
									try {
										CompanyManList.appendList(newPair);
									} catch (IOException e) {
									}
								}
							}
						}
					}
				});
				repsThread.start();
			}
		}
	}
}
