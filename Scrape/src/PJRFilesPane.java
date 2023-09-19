import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

public class PJRFilesPane extends JFrame {

	private LinkedList<CheckBox> checkList = new LinkedList<>();
	private CheckLabel label1;
	private CheckLabel label2;
	private CheckLabel label3;
	private CheckLabel label4;
	private CheckLabel label5;
	private String filePath;
	private JButton button;
	CheckBox check1;
	CheckBox check2;
	CheckBox check3;
	CheckBox check4;
	CheckBox check5;

	PJRFilesPane(String l1, String l2, String l3, String l4, String l5, String filePath) {
		this.label1 = new CheckLabel(l1);
		this.label2 = new CheckLabel(l2);
		this.label3 = new CheckLabel(l3);
		this.label4 = new CheckLabel(l4);
		this.label5 = new CheckLabel(l5);
		this.filePath = filePath;
		this.setBounds(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - 120,
				Toolkit.getDefaultToolkit().getScreenSize().height / 2 - 170, 240, 340);
		JPanel panel = constructPanel();
		ArrayList<Boolean> checks = null;
		try {
			checks = loadChecks();
		} catch (IOException e) {
			checks = new ArrayList<>();
		}

		if (checks != null && !checks.isEmpty()) {
			check1 = new CheckBox(checks.get(0));
			check2 = new CheckBox(checks.get(1));
			check3 = new CheckBox(checks.get(2));
			check4 = new CheckBox(checks.get(3));
			check5 = new CheckBox(checks.get(4));
		} else {
			check1 = new CheckBox(true);
			check2 = new CheckBox(true);
			check3 = new CheckBox(true);
			check4 = new CheckBox(true);
			check5 = new CheckBox(true);
		}
		checkList.add(check1);
		checkList.add(check2);
		checkList.add(check3);
		checkList.add(check4);
		checkList.add(check5);
		panel.add(check1);
		panel.add(check2);
		panel.add(check3);
		panel.add(check4);
		panel.add(check5);
		panel.add(label1);
		panel.add(label2);
		panel.add(label3);
		panel.add(label4);
		panel.add(label5);
		button = constructButton();
		panel.add(button);
		CheckSpringLayout checkLayout = new CheckSpringLayout(check1, check2, check3, check4, check5, label1, label2,
				label3, label4, label5, panel, button);
		panel.setLayout(checkLayout);
		this.add(panel);
		this.setTitle("PJR Files");
		this.setIconImage(
				new ImageIcon("C:\\Scrape\\Scrape.png").getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
		this.setAlwaysOnTop(true);
		this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		this.setVisible(false);
	}

	public LinkedList<CheckBox> getCheckList() {
		return this.checkList;
	}

	public ArrayList<Boolean> loadChecks() throws IOException {
		ArrayList<Boolean> checks = new ArrayList<>();
		FileReader fileReader = new FileReader(new File(filePath));
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String temp;
		while ((temp = bufferedReader.readLine()) != null) {
			checks.add(Boolean.valueOf(temp));
		}
		return checks;
	}

	public void saveChecks() throws IOException {
		FileWriter fileWriter = null;

		fileWriter = new FileWriter(new File(filePath));
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		bufferedWriter.write("");
		for (CheckBox a : checkList) {
			if (a.isChecked()) {
				bufferedWriter.append("true");
				bufferedWriter.append("\n");
			} else {
				bufferedWriter.append("false");
				bufferedWriter.append("\n");
			}
		}
		bufferedWriter.flush();
		bufferedWriter.close();
	}

	public JPanel constructPanel() {
		JPanel panel = new JPanel();
		panel.setBounds(this.getBounds());
		panel.setBackground(Color.DARK_GRAY);
		panel.setVisible(true);
		return panel;
	}

	public JButton constructButton() {
		JButton button = new JButton();
		button.setSize(75, 25);
		button.setText("Save");
		button.setVisible(true);
		button.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					saveChecks();
				} catch (IOException e1) {
					System.out.println("FileNotFoundException/IOException");
				}
				PJRFilesPane.this.setVisible(!PJRFilesPane.this.isVisible());
			}
		});
		return button;
	}

	public void setButtonAction(ActionListener e) {
		button.addActionListener(e);
	}

	private class CheckLabel extends JLabel {
		CheckLabel(String text) {
			this.setSize(85, 35);
			this.setHorizontalAlignment(SwingConstants.CENTER);
			this.setOpaque(true);

			this.setText(String.format("<html><div style=\"width:%dpx\">%s</div></html>", 50, text));
			this.setBackground(Color.DARK_GRAY);
			this.setForeground(Color.WHITE);
		}
	}

	public class CheckBox extends JLabel {
		private Image check = new ImageIcon("C:\\Scrape\\check.png").getImage().getScaledInstance(50, 50,
				Image.SCALE_SMOOTH);
		private ImageIcon checkIcon = new ImageIcon(check);

		CheckBox(Boolean checked) {
			this.setSize(35, 35);
			if (checked) {
				this.setIcon(checkIcon);
			} else {
				this.setIcon(null);
			}
			this.setOpaque(true);
			this.setBackground(Color.WHITE);
			this.addMouseListener(new HideUnhideCheck());
		}

		Boolean isChecked() {
			Boolean checked = null;
			if (this.getIcon() == null) {
				checked = false;
			} else {
				checked = true;
			}
			return checked;
		}

		class HideUnhideCheck implements MouseListener {
			@Override
			public void mouseClicked(MouseEvent e) {
				SwingWorker<Void, Void> worker = new SwingWorker<>() {
					public Void doInBackground() {
						if (((JLabel) e.getSource()).getIcon() == null) {
							((JLabel) e.getSource()).setIcon(checkIcon);
						} else {
							((JLabel) e.getSource()).setIcon(null);
						}
						return null;
					}
				};
				worker.execute();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}
		}
	}

	private class CheckSpringLayout extends SpringLayout {
		CheckSpringLayout(CheckBox cB1, CheckBox cB2, CheckBox cB3, CheckBox cB4, CheckBox cB5, JLabel l1, JLabel l2,
				JLabel l3, JLabel l4, JLabel l5, JPanel panel, JButton button) {
			this.putConstraint(WEST, cB1, 55, WEST, panel);
			this.putConstraint(EAST, cB1, 90, WEST, panel);
			this.putConstraint(NORTH, cB1, 15, NORTH, panel);
			this.putConstraint(SOUTH, cB1, 50, NORTH, panel);
			this.putConstraint(WEST, cB2, 55, WEST, panel);
			this.putConstraint(EAST, cB2, 90, WEST, panel);
			this.putConstraint(NORTH, cB2, 60, NORTH, panel);
			this.putConstraint(SOUTH, cB2, 95, NORTH, panel);
			this.putConstraint(WEST, cB3, 55, WEST, panel);
			this.putConstraint(EAST, cB3, 90, WEST, panel);
			this.putConstraint(NORTH, cB3, 105, NORTH, panel);
			this.putConstraint(SOUTH, cB3, 140, NORTH, panel);
			this.putConstraint(WEST, cB4, 55, WEST, panel);
			this.putConstraint(EAST, cB4, 90, WEST, panel);
			this.putConstraint(NORTH, cB4, 150, NORTH, panel);
			this.putConstraint(SOUTH, cB4, 185, NORTH, panel);
			this.putConstraint(WEST, cB5, 55, WEST, panel);
			this.putConstraint(EAST, cB5, 90, WEST, panel);
			this.putConstraint(NORTH, cB5, 195, NORTH, panel);
			this.putConstraint(SOUTH, cB5, 230, NORTH, panel);
			this.putConstraint(WEST, l1, 95, WEST, panel);
			this.putConstraint(EAST, l1, 200, WEST, panel);
			this.putConstraint(NORTH, l1, 15, NORTH, panel);
			this.putConstraint(SOUTH, l1, 50, NORTH, panel);
			this.putConstraint(WEST, l2, 95, WEST, panel);
			this.putConstraint(EAST, l2, 200, WEST, panel);
			this.putConstraint(NORTH, l2, 60, NORTH, panel);
			this.putConstraint(SOUTH, l2, 95, NORTH, panel);
			this.putConstraint(WEST, l3, 95, WEST, panel);
			this.putConstraint(EAST, l3, 200, WEST, panel);
			this.putConstraint(NORTH, l3, 105, NORTH, panel);
			this.putConstraint(SOUTH, l3, 140, NORTH, panel);
			this.putConstraint(WEST, l4, 95, WEST, panel);
			this.putConstraint(EAST, l4, 200, WEST, panel);
			this.putConstraint(NORTH, l4, 150, NORTH, panel);
			this.putConstraint(SOUTH, l4, 185, NORTH, panel);
			this.putConstraint(WEST, l5, 95, WEST, panel);
			this.putConstraint(EAST, l5, 200, WEST, panel);
			this.putConstraint(NORTH, l5, 195, NORTH, panel);
			this.putConstraint(SOUTH, l5, 230, NORTH, panel);
			this.putConstraint(WEST, button, 68, WEST, panel);
			this.putConstraint(EAST, button, 143, WEST, panel);
			this.putConstraint(NORTH, button, 255, NORTH, panel);
			this.putConstraint(SOUTH, button, 280, NORTH, panel);
		}
	}
}
