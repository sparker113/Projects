import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

public class WellDrivePane extends JFrame {
	private JPanel panel;
	private JTable table;
	private JLabel label1;
	private JLabel label2;
	private JButton button;
	private String path;

	WellDrivePane() {
		int x = Toolkit.getDefaultToolkit().getScreenSize().width / 2 - 150;
		int y = Toolkit.getDefaultToolkit().getScreenSize().height / 2 - 65;
		this.setBounds(x, y, 320, 150);
		this.setIconImage(
				new ImageIcon("C:\\Scrape\\Scrape.png").getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
		this.setTitle("WellDrive Credentials");
		this.setResizable(false);
		panel = constructPanel();
		table = constructTable();
		label1 = constructLabel("Username");
		label2 = constructLabel("Password");
		button = constructButton();
		panel.add(table);
		panel.add(label1);
		panel.add(label2);
		panel.add(button);
		WellDriveLayout wellDriveLayout = new WellDriveLayout(panel, table, label1, label2, button);
		panel.setLayout(wellDriveLayout);
		this.add(panel);
		this.setVisible(false);
		this.setAlwaysOnTop(true);
	}

	public JPanel constructPanel() {
		JPanel panel = new JPanel();
		panel.setBounds(this.getBounds());
		panel.setBackground(Color.DARK_GRAY);
		panel.setOpaque(true);
		panel.setVisible(true);
		return panel;
	}

	public JTable constructTable() {
		JTable table = new JTable(2, 1);
		table.setRowHeight(25);
		table.getColumnModel().getColumn(0).setPreferredWidth(135);
		table.setIntercellSpacing(new Dimension(3, 3));
		try {
			loadInfo(table);
		} catch (IOException e) {
			System.out.println("WellDrivePane");
		}
		table.setVisible(true);
		return table;
	}

	public JLabel constructLabel(String text) {
		JLabel label = new JLabel();
		label.setBackground(Color.white);
		label.setSize(125, 25);
		label.setText(text);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setOpaque(true);
		return label;
	}

	public JButton constructButton() {
		JButton button = new JButton();
		button.setText("Save");
		button.setVisible(true);
		/*
		 * button.addActionListener(new AbstractAction() { public void
		 * actionPerformed(ActionEvent e) { try { saveInfo(); } catch (IOException e1) {
		 * System.out.println("WellDrivePane.constructButton"); }
		 * WellDrivePane.this.setVisible(false); } });
		 */
		return button;
	}

	public void setButtonAction(ActionListener action) {
		button.addActionListener(action);
	}

	public String getUsername() {
		String userName = String.valueOf(table.getValueAt(0, 0));
		return userName;
	}

	public String getPassword() {
		String password = String.valueOf(table.getValueAt(1, 0));
		return password;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return this.path;
	}

	public void saveInfo(String path) throws IOException {
		FileWriter fileWriter = new FileWriter(new File(path));
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(String.valueOf(table.getValueAt(0, 0)));
		stringBuilder.append("\n");
		stringBuilder.append(String.valueOf(table.getValueAt(1, 0)));
		String wellDrive = stringBuilder.substring(0);
		bufferedWriter.write(wellDrive);
		bufferedWriter.flush();
		bufferedWriter.close();
	}

	public void loadInfo(JTable table) throws IOException {
		FileReader fileReader = new FileReader("C:\\Scrape\\welldrive.txt");
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String temp;
		int i = 0;
		while ((temp = bufferedReader.readLine()) != null) {
			table.setValueAt(temp, i, 0);
			i++;
		}
		bufferedReader.close();
	}

	public class WellDriveLayout extends SpringLayout {
		WellDriveLayout(JPanel panel, JTable table, JLabel label1, JLabel label2, JButton button) {
			this.putConstraint(NORTH, table, 10, NORTH, panel);
			this.putConstraint(SOUTH, table, 60, NORTH, panel);
			this.putConstraint(WEST, table, 150, WEST, panel);
			this.putConstraint(EAST, table, 285, WEST, panel);
			this.putConstraint(NORTH, label1, 10, NORTH, panel);
			this.putConstraint(SOUTH, label1, 34, NORTH, panel);
			this.putConstraint(WEST, label1, 15, WEST, panel);
			this.putConstraint(EAST, label1, 140, WEST, panel);
			this.putConstraint(NORTH, label2, 36, NORTH, panel);
			this.putConstraint(SOUTH, label2, 60, NORTH, panel);
			this.putConstraint(WEST, label2, 15, WEST, panel);
			this.putConstraint(EAST, label2, 140, WEST, panel);
			this.putConstraint(NORTH, button, 75, NORTH, panel);
			this.putConstraint(SOUTH, button, 100, NORTH, panel);
			this.putConstraint(WEST, button, 110, WEST, panel);
			this.putConstraint(EAST, button, 190, WEST, panel);

		}
	}

}
