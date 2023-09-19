import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class EmailSettings extends JFrame {
	public int x;
	public int y;
	public int width;
	public int height;
	private final Color COLOR = Color.getHSBColor(-.85f, .1f, .85f);
	JTable table;
	HashMap<String, ArrayList<String>> filesMap;

	EmailSettings() {
		nittyGritty();
		constructListBox();
		constructAddButton();
		constructDeleteButton();
		constructReconfigButton();
		constructSaveButton();
		constructMyEmailTextBox();
		constructMyEmailLabel();
		constructFileTable();
		this.setLayout(new EmailLayout());
		this.setVisible(true);
	}

	public Component getComponentByName(String name) {
		for (Component c : getContentPane().getComponents()) {
			String cName = c.getName();
			if (cName != null && cName.equals(name)) {
				return c;
			}
		}
		return null;
	}

	private void nittyGritty() {
		filesMap = new HashMap<>();
		defineBounds();
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setBounds(x, y, width, height);
		this.getContentPane().setBackground(COLOR);
		this.setTitle("Email Settings");
		this.setIconImage(
				new ImageIcon("C:\\Scrape\\Scrape.png").getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
		constructDividerLines();
		this.setVisible(true);
	}

	public void constructDividerLines() {
		DividerLines dividerLines = new DividerLines(new Rectangle(x, y, width, height));
		dividerLines.setName("lines");
		dividerLines.addLine(0, 35, width, 35);
		dividerLines.setVisible(true);
		getContentPane().add(dividerLines);
	}

	private void defineBounds() {
		this.width = Toolkit.getDefaultToolkit().getScreenSize().width / 3;
		this.height = Toolkit.getDefaultToolkit().getScreenSize().height / 4;
		this.x = Toolkit.getDefaultToolkit().getScreenSize().width / 2 - width / 2;
		this.y = Toolkit.getDefaultToolkit().getScreenSize().height / 2 - height / 2;
	}

	private void constructMyEmailTextBox() {
		JTextField textField = new JTextField();
		textField.setName("my_email_text");
		String myEmail = "";
		try {
			myEmail = ArgumentsToText.readStringFromFile("C:\\Scrape\\My_Email.txt");
		} catch (IOException e) {
			myEmail = "";
		}
		textField.setText(myEmail);
		textField.setVisible(true);
		this.add(textField);
	}

	private void constructMyEmailLabel() {
		JLabel label = new JLabel();
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setName("my_email_label");
		label.setText("My Email:");
		label.setBackground(COLOR);
		label.setOpaque(true);
		this.add(label);
	}

	private void constructSaveButton() {
		JButton button = new JButton();
		button.setName("save_button");
		button.setText("Save");
		button.addActionListener(new AbstractAction() {
			@Override
			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> savedItems = getComboBoxItems((JComboBox<String>) getComponentByName("combo_box"));
				try {
					deleteItems(savedItems);
				} catch (IOException e1) {
					System.out.println("IOException caught deleting directories");
				}
				String myEmail = ((JTextField) getComponentByName("my_email_text")).getText();
				try {
					Matcher matcher = Pattern.compile("^(.[^\\s]+)@(\\D+)\\.\\w\\w\\w$").matcher(myEmail);
					if (!matcher.find()) {
						JOptionPane.showMessageDialog(null,
								"Input a valid email in the 'My Email' field before saving");
						return;
					}
					ArgumentsToText.writeSingleLineToText(myEmail, "C://Scrape//My_Email.txt");
					writeMapToText();
				} catch (IOException e2) {
					System.out.println("Exception caught saving my email");
				}
				dispose();
			}
		});
		this.add(button);
	}

	private void writeMapToText() throws IOException {
		String parentPath = "C:\\Scrape\\ScrapePython\\Email_Settings\\";
		for (String s : filesMap.keySet()) {
			FileWriter fileWriter = new FileWriter(parentPath + s + "\\files.txt");
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write("");
			for (String ss : filesMap.get(s)) {
				bufferedWriter.append(ss);
				bufferedWriter.newLine();
			}
			bufferedWriter.flush();
			bufferedWriter.close();
		}
	}

	private ArrayList<String> getComboBoxItems(JComboBox<String> comboBox) {
		ArrayList<String> savedItems = new ArrayList<>();
		for (int i = 1; i < comboBox.getItemCount(); i++) {
			System.out.println(comboBox.getItemAt(i).toString());
			savedItems.add(comboBox.getItemAt(i).toString());
		}
		return savedItems;
	}

	private void deleteItems(ArrayList<String> savedItems) throws IOException {
		for (String s : readConfigurationDirs()) {
			if (!savedItems.contains(s)) {
				File file = new File("C:\\Scrape\\ScrapePython\\Email_Settings\\" + s);
				if (!file.delete()) {
					System.out.println("Directory could not be deleted");
				}
			}
		}
	}

	private void constructListBox() {
		JComboBox<String> comboBox = new JComboBox<>();
		try {
			addComboBoxItems(comboBox);
		} catch (IOException e) {
			System.out.println("IOException caught adding items to email combo box");
		}
		comboBox.addPopupMenuListener(new PopupMenuListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				JComboBox<String> comboBox = (JComboBox<String>) getComponentByName("combo_box");
				String selectedItem = comboBox.getSelectedItem().toString();
				JButton configButton = (JButton) getComponentByName("reconfig_button");
				SwingWorker<Void, String> worker = new SwingWorker<>() {
					public Void doInBackground() {
						publish(selectedItem);
						return null;
					}

					public void process(List<String> chunks) {
						if (chunks.get(0).equals("-")) {
							configButton.setEnabled(false);
							clearTable(table);
						} else {
							populateTable(filesMap.get(selectedItem), table);
							configButton.setEnabled(true);
							EmailFrame emailFrame = new EmailFrame(350, 300, chunks.get(0));
							emailFrame.setVisible(true);
							emailFrame.setButtonAction(new EmailFrameAction(chunks.get(0), emailFrame));
						}
					}
				};
				worker.execute();
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			}

		});
		comboBox.setName("combo_box");
		((JLabel) comboBox.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		this.add(comboBox);
	}

	private void addComboBoxItems(JComboBox<String> comboBox) throws IOException {
		comboBox.addItem("-");

		for (String s : readConfigurationDirs()) {
			comboBox.addItem(s);
			filesMap.put(s, new ArrayList<String>());
			addFilesToMap(s);
		}
	}

	private void addFilesToMap(String configName) throws IOException {
		filesMap.get(configName).addAll(ArgumentsToText
				.readArguments("C:\\Scrape\\ScrapePython\\Email_Settings\\" + configName + "\\files.txt"));
	}

	private void populateTable(ArrayList<String> files, JTable table) {
		clearTable(table);
		int i = 0;
		for (String s : files) {
			table.setValueAt(s, i, 0);
			i++;
		}
	}

	private void clearTable(JTable table) {
		for (int i = 0; i < table.getRowCount(); i++) {
			table.setValueAt("", i, 0);
		}
	}

	private void constructAddButton() {
		JButton button = new JButton();
		button.setName("add_button");
		button.setText("Add Email");
		button.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String configName = JOptionPane.showInputDialog("Input what you want to name this configuration");
				File file = new File("C:\\Scrape\\ScrapePython\\Email_Settings\\" + configName);
				SwingWorker<Void, JComboBox<String>> worker = new SwingWorker<>() {
					public Void doInBackground() {
						JComboBox<String> comboBox = (JComboBox<String>) getComponentByName("combo_box");
						comboBox.addItem(configName);
						filesMap.put(configName, new ArrayList<String>());
						publish(comboBox);
						return null;
					}

					public void process(List<JComboBox<String>> chunks) {
						chunks.get(0).repaint();
					}
				};
				worker.execute();
				file.mkdir();
				EmailFrame emailFrame = new EmailFrame(350, 300);
				emailFrame.setVisible(true);
				emailFrame.setButtonAction(new EmailFrameAction(configName, emailFrame));
			}
		});
		this.add(button);
	}

	private void constructReconfigButton() {
		JButton button = new JButton();
		button.setName("reconfig_button");
		button.setText("Add File");
		button.setEnabled(false);
		button.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String mainDirectory = "C:\\";
				try {
					mainDirectory = ReadDirectory.readDirect();
				} catch (IOException e1) {
					System.out.println("Exception caught reading directory");
				}
				JFileChooser fileChooser = new JFileChooser(mainDirectory);
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int selected = fileChooser.showOpenDialog(null);
				if (selected == JFileChooser.APPROVE_OPTION) {
					setFileInTable(fileChooser.getSelectedFile().getAbsolutePath());
					JComboBox<String> comboBox = (JComboBox<String>) getComponentByName("combo_box");
					filesMap.get(comboBox.getSelectedItem().toString())
							.add(fileChooser.getSelectedFile().getAbsolutePath());
				}
				// if(selected == fileChooser.)
			}
		});
		button.setVisible(true);
		this.add(button);
	}

	private void constructFileTable() {
		this.table = new JTable(10, 1);
		table.setName("table");
		table.getColumnModel().getColumn(0).setHeaderValue("Files");
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setName("scroll");
		scrollPane.setVisible(true);
		this.add(scrollPane);
	}

	private JTable getFileTable() {
		for (Component c : ((JScrollPane) getComponentByName("scroll")).getComponents()) {
			System.out.println(c.getName());
			if (c.getName() != null && c.getName().equals("table")) {
				return (JTable) c;
			}
		}
		return null;
	}

	private int findNextEmptyRow(JTable table) {
		for (int i = 0; i < table.getRowCount(); i++) {
			if (table.getValueAt(i, 0) == null || table.getValueAt(i, 0).toString().equals("")) {
				return i;
			}
		}
		return 0;

	}

	private void setFileInTable(String path) {
		int row = findNextEmptyRow(table);
		table.setValueAt(path, row, 0);
	}

	private void constructDeleteButton() {
		JButton button = new JButton();
		button.setName("delete_button");
		button.setText("Delete Email");
		button.setEnabled(true);
		button.addActionListener(new AbstractAction() {
			@Override
			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent e) {
				JComboBox<String> comboBox = (JComboBox<String>) getComponentByName("combo_box");
				JButton configButton = (JButton) getComponentByName("reconfig_button");
				SwingWorker<Void, String> worker = new SwingWorker<>() {
					public Void doInBackground() {
						if (!comboBox.getSelectedItem().toString().equals("-")) {
							publish(comboBox.getSelectedItem().toString());
						}
						return null;
					}

					public void process(List<String> chunks) {
						comboBox.removeItem(chunks.get(0));
						comboBox.setSelectedIndex(0);
						configButton.setEnabled(false);
						filesMap.remove(chunks.get(0));
					}
				};
				worker.execute();
			}
		});
		this.add(button);
	}

	public ArrayList<String> readConfigurationDirs() throws IOException {
		String path = "C:\\Scrape\\ScrapePython\\Email_Settings\\";
		ArrayList<String> configDirs = new ArrayList<>();
		File file = new File(path);
		if (!file.exists()) {
			file.mkdir();
		}
		for (String s : file.list()) {
			System.out.println(s);
			configDirs.add(s);
		}
		return configDirs;
	}

	private class EmailLayout extends SpringLayout {
		int BUTTONSPACING = 10;
		int BUTTONHEIGHT = 25;
		int topInset = getInsets().top;

		EmailLayout() {
			constructLayout();
		}

		@SuppressWarnings("unchecked")
		public void constructLayout() {
			for (Component c : getContentPane().getComponents()) {
				String name = c.getName();
				switch (name) {
				case ("my_email_label"):
					myEmailLabelLayout((JLabel) c);
					break;
				case ("my_email_text"):
					myEmailTextLayout((JTextField) c);
					break;
				case ("combo_box"):
					comboBoxLayout((JComboBox<String>) c);
					break;
				case ("add_button"):
					buttonLayout((JButton) c, 1);
					break;
				case ("reconfig_button"):
					buttonLayout((JButton) c, 2);
					break;
				case ("delete_button"):
					buttonLayout((JButton) c, 3);
					break;
				case ("save_button"):
					buttonLayout((JButton) c, 4);
					break;
				case ("lines"):
					linesLayout((DividerLines) c);
					break;
				case ("scroll"):
					scrollLayout((JScrollPane) c);
					break;
				}
			}
		}

		public void scrollLayout(JScrollPane scrollPane) {
			putConstraint(NORTH, scrollPane, getButtonY(1) + 35, NORTH, getContentPane());
			putConstraint(SOUTH, scrollPane, height - topInset - 10, NORTH, getContentPane());
			putConstraint(WEST, scrollPane, 15, WEST, getContentPane());
			putConstraint(EAST, scrollPane, width * 3 / 5, WEST, getContentPane());
		}

		public void linesLayout(DividerLines lines) {
			putConstraint(NORTH, lines, 0, NORTH, getContentPane());
			putConstraint(SOUTH, lines, height, NORTH, getContentPane());
			putConstraint(WEST, lines, 0, WEST, getContentPane());
			putConstraint(EAST, lines, width, WEST, getContentPane());
		}

		public void buttonLayout(JButton button, int orderOfButton) {
			putConstraint(NORTH, button, getButtonY(orderOfButton) + topInset, NORTH, getContentPane());
			putConstraint(SOUTH, button, getButtonY(orderOfButton) + BUTTONHEIGHT + topInset, NORTH, getContentPane());
			putConstraint(WEST, button, width * 3 / 5 + 25, WEST, getContentPane());
			putConstraint(EAST, button, width - 25, WEST, getContentPane());
		}

		public void comboBoxLayout(JComboBox<String> comboBox) {
			putConstraint(NORTH, comboBox, getButtonY(1), NORTH, getContentPane());
			putConstraint(SOUTH, comboBox, getButtonY(1) + 25, NORTH, getContentPane());
			putConstraint(WEST, comboBox, 15, WEST, getContentPane());
			putConstraint(EAST, comboBox, width * 3 / 5, WEST, getContentPane());
		}

		public void myEmailLabelLayout(JLabel label) {
			putConstraint(NORTH, label, 10, NORTH, getContentPane());
			putConstraint(SOUTH, label, 30, NORTH, getContentPane());
			putConstraint(WEST, label, 15, WEST, getContentPane());
			putConstraint(EAST, label, width / 4, WEST, getContentPane());
		}

		public void myEmailTextLayout(JTextField textField) {
			putConstraint(NORTH, textField, 10, NORTH, getContentPane());
			putConstraint(SOUTH, textField, 30, NORTH, getContentPane());
			putConstraint(WEST, textField, width / 4 + 5, WEST, getContentPane());
			putConstraint(EAST, textField, width - 25, WEST, getContentPane());
		}

		int getComboBoxY(int textBottom) {
			return ((height - textBottom) / 2) + textBottom - 13 - topInset;
		}

		int getButtonY(int orderOfButton) {
			return getFirstButtonY() + (orderOfButton - 1) * (BUTTONHEIGHT + BUTTONSPACING) - topInset;
		}

		int getFirstButtonY() {
			int totalFromTopButton = BUTTONHEIGHT * 2 + BUTTONSPACING * 3 / 2 - topInset;
			int topTwoBottom = getComboBoxY(30) + 13;
			return topTwoBottom - totalFromTopButton;
		}
	}

	class EmailFrameAction extends AbstractAction {
		String directory;
		EmailFrame frame;

		EmailFrameAction(String directory, EmailFrame frame) {
			this.directory = directory;
			this.frame = frame;
		}

		public String getPath() {
			return "C:\\Scrape\\ScrapePython\\Email_Settings\\" + directory;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			ArrayList<String> emailList = new ArrayList<>();
			for (int i = 0; i < frame.emailTable.getRowCount(); i++) {
				if (frame.emailTable.getValueAt(i, 1) == null
						|| frame.emailTable.getValueAt(i, 1).toString().equals("")) {
					continue;
				}
				emailList.add(frame.emailTable.getValueAt(i, 1).toString());
			}
			String fileName = getPath() + "\\email_address.txt";
			try {
				ArgumentsToText.writeArguments(emailList, fileName, "\n");
			} catch (IOException e1) {
				System.out.println("IOException caught writing emails to email_address.txt");
			}
			frame.dispose();
		}
	}

	class DividerLines extends JLabel {
		ArrayList<Line> lineArray;
		Rectangle rectangle;

		DividerLines(Rectangle rectangle) {
			this.rectangle = rectangle;
			nittyGritty();
			lineArray = new ArrayList<>();
		}

		public void nittyGritty() {
			this.setBounds(rectangle);
			this.setBackground(null);
			this.setVisible(true);
		}

		void addLine(int x1, int y1, int x2, int y2) {
			lineArray.add(new Line(x1, y1, x2, y2));
		}

		@Override
		public void paintComponent(Graphics g) {
			for (Line line : lineArray) {
				g.setColor(Color.black);
				g.drawLine(line.x1, line.y1, line.x2, line.y2);
			}
		}

		class Line {
			int x1;
			int y1;
			int x2;
			int y2;

			Line(int x1, int y1, int x2, int y2) {
				this.x1 = x1;
				this.y1 = y1;
				this.x2 = x2;
				this.y2 = y2;
			}
		}
	}

}
