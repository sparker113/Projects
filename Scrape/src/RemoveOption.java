import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker;

public class RemoveOption extends JFrame {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	JTable table;
	CheckBox addRowUp;
	CheckBox addRowDown;
	CheckBox noAdd;
	CheckBox doNothing;
	LinkedList<CheckBox> checkBoxes = new LinkedList<>();
	JTextField textField;
	JButton button;
	int x;
	int y;

	RemoveOption(JTable table, int x, int y) {
		this.table = table;
		this.x = x;
		this.y = y;
		this.checkBoxes.add(this.addRowUp = new CheckBox(false, "Add_Up"));
		this.checkBoxes.add(this.addRowDown = new CheckBox(true, "Add_Down"));
		this.checkBoxes.add(this.noAdd = new CheckBox(false, "No_Add"));
		this.checkBoxes.add(this.doNothing = new CheckBox(false, "Do_Nothing"));
		this.getContentPane().setBackground(Color.getHSBColor((float) -.85, (float) .1, (float) .85));
		constructFrame();
	}

	private void constructFrame() {
		// this.textField = makeInput();
		JLabel label1 = makeLabel("ADD UP");
		JLabel label2 = makeLabel("ADD DOWN");
		JLabel label3 = makeLabel("DON'T ADD");
		this.add(addRowUp);
		this.add(addRowDown);
		this.add(noAdd);
		this.button = makeButton();
		this.add(button);
		this.add(label1);
		this.add(label2);
		this.add(label3);
		this.setBounds(x + Main.yess.getScrollPane().getX(),
				y + Main.yess.getScrollPane().getY() + Main.yess.getJMenuBar().getHeight() * 3, 200, 200);
		this.setLayout(new RemoveLayout(addRowUp, addRowDown, noAdd, button, (JPanel) this.getContentPane(), label1,
				label2, label3));
		this.setVisible(true);
		this.setAlwaysOnTop(true);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	private JTextField makeInput() {
		JTextField textField = new JTextField();
		textField.setEditable(true);
		textField.setVisible(true);
		textField.setOpaque(true);
		return textField;
	}

	private JLabel makeLabel(String text) {
		JLabel label = new JLabel();
		label.setText(text);
		label.setSize(75, 15);
		label.setForeground(Color.BLACK);
		label.setBackground(Color.getHSBColor((float) -.85, (float) .1, (float) .85));
		label.setOpaque(true);
		label.setVisible(true);
		return label;
	}

	private JButton makeButton() {
		JButton button = new JButton();
		button.setText("Remove Row");
		button.setOpaque(true);
		button.setVisible(true);
		button.addActionListener(new ButtonAction());
		return button;
	}

	private Integer getRowToRemove() {
		int unitHeight = table.getRowHeight();
		Integer rowToRemove = (y - (y % unitHeight)) / unitHeight;
		System.out.println(rowToRemove);
		return rowToRemove;
	}

	private void fixDateTime(Integer rowToRemove) {
		table.setValueAt(table.getValueAt(rowToRemove, 0), rowToRemove + 1, 0);
		table.setValueAt(table.getValueAt(rowToRemove, 3), rowToRemove + 1, 3);
	}

	private Double addRowValues(Integer rowToRemove, Integer column, Boolean down) {
		Double removedValue = 0.0;
		Double valueToAdd = 0.0;
		if (down) {
			if (!String.valueOf(table.getValueAt(rowToRemove, column)).equals("")) {
				removedValue = Double.valueOf(String.valueOf(table.getValueAt(rowToRemove, column)));
			}
			if (!String.valueOf(table.getValueAt(rowToRemove + 1, column)).equals("")) {
				valueToAdd = Double.valueOf(String.valueOf(table.getValueAt(rowToRemove + 1, column)));
			}
			return removedValue + valueToAdd;
		} else {
			if (!String.valueOf(table.getValueAt(rowToRemove, column)).equals("")) {
				removedValue = Double.valueOf(String.valueOf(table.getValueAt(rowToRemove, column)));
			}
			if (!String.valueOf(table.getValueAt(rowToRemove - 1, column)).equals("")) {
				valueToAdd = Double.valueOf(String.valueOf(table.getValueAt(rowToRemove - 1, column)));
			}
			return removedValue + valueToAdd;
		}
	}

	private void removeRow(Integer rowToRemove) {
		Integer lastRow = findLastRow();
		System.out.println("The Last Row: " + lastRow);
		for (int i = rowToRemove; i <= lastRow; i++) {
			for (int ii = 0; ii < table.getColumnCount(); ii++) {
				table.setValueAt(table.getValueAt(i + 1, ii), i, ii);
			}
		}
	}

	public static void fixStageNumbers(Integer lastRow, JTable table) {
		for (int i = 0; i <= lastRow; i++) {
			table.setValueAt(i + 1, i, 2);
		}
	}

	private void noAdd(Integer rowToRemove) {
		Integer lastRow = findLastRow();
		int fChunkRow = rowToRemove + 1;
		Integer columnCount = table.getColumnCount();
		for (int i = fChunkRow; i <= lastRow; i++) {
			for (int col = 0; col < columnCount; col++) {
				if (String.valueOf(table.getValueAt(i, col)) == "" || table.getValueAt(i, col) == null) {
					table.setValueAt("", i - 1, col);
				} else {
					table.setValueAt(table.getValueAt(i, col), i - 1, col);
				}
			}
		}
		for (int col = 0; col < columnCount; col++) {
			table.setValueAt("", lastRow, col);
		}
	}

	/////////////////////////////////////////////////// This is where you left off
	private Integer findLastRow() {
		int lastRow = 0;
		for (int i = 0; i < table.getRowCount(); i++) {
			if (table.getValueAt(i, 0) != null && !String.valueOf(table.getValueAt(i, 0)).equals("")) {
				continue;
			} else {
				lastRow = i - 1;
				break;
			}
		}
		return lastRow;
	}

	private void setNewValues(Double clean, Double slurry, Double sand, Integer row) {
		String sandString = String.valueOf(sand);
		if (sandString.equals("0.0")) {
			sandString = "";
		}
		table.setValueAt(clean, row, 6);
		table.setValueAt(slurry, row, 9);
		table.setValueAt(sandString, row, 11);
		fixStageNumbers(findLastRow(), Main.yess.mTable);
	}
	/*
	 *
	 * Action set to the button that will remove the row and handle the information
	 * as specified by
	 *
	 */

	private class ButtonAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			String boxName = "Do_Nothing";
			Double clean = 0.0;
			Double slurry = 0.0;
			Double sand = 0.0;
			for (CheckBox box : checkBoxes) {
				if (box.isChecked()) {
					boxName = box.getName();
				}
			}
			Integer rowToRemove = getRowToRemove();
			System.out.println(rowToRemove + " - " + boxName);
			switch (boxName) {
			case ("Add_Up"):
				System.out.println("Add_Up");
				fixDateTime(rowToRemove);
				clean = addRowValues(rowToRemove, 6, false);
				slurry = addRowValues(rowToRemove, 9, false);
				sand = addRowValues(rowToRemove, 11, false);
				removeRow(rowToRemove);
				setNewValues(clean, slurry, sand, rowToRemove - 1);
				break;
			case ("Add_Down"):
				fixDateTime(rowToRemove);
				clean = addRowValues(rowToRemove, 6, true);
				slurry = addRowValues(rowToRemove, 9, true);
				sand = addRowValues(rowToRemove, 11, true);
				removeRow(rowToRemove);
				setNewValues(clean, slurry, sand, rowToRemove);
				break;
			case ("No_Add"):
				noAdd(rowToRemove);
				removeRow(rowToRemove);
				break;
			case ("Do_Nothing"):

				break;
			}
			RemoveOption.this.dispose();
		}
	}
	/////////////////////////////////////////////////////////////////// Left Off
	/*
	 *
	 *
	 * Layout for the Frame
	 *
	 *
	 *
	 */

	private class RemoveLayout extends SpringLayout {
		CheckBox box1;
		CheckBox box2;
		CheckBox box3;
		JLabel label1;
		JLabel label2;
		JLabel label3;
		JButton button;
		JPanel panel;

		RemoveLayout(CheckBox box1, CheckBox box2, CheckBox box3, JButton button, JPanel panel, JLabel label1,
				JLabel label2, JLabel label3) {
			this.box1 = box1;
			this.box2 = box2;
			this.box3 = box3;
			this.label1 = label1;
			this.label2 = label2;
			this.label3 = label3;
			this.button = button;
			this.panel = panel;
			layout();
		}

		public void layout() {
			boxLayout(box1, 20, 20);
			boxLayout(box2, 55, 20);
			boxLayout(box3, 90, 20);
			labelLayout(label1, 20, 40);
			labelLayout(label2, 55, 40);
			labelLayout(label3, 90, 40);
			buttonLayout(button, 125, 20);
		}

		public void boxLayout(CheckBox box, int topStart, int leftStart) {
			this.putConstraint(NORTH, box, topStart, NORTH, panel);
			this.putConstraint(SOUTH, box, topStart + 15, NORTH, panel);
			this.putConstraint(WEST, box, leftStart, WEST, panel);
			this.putConstraint(EAST, box, leftStart + 15, WEST, panel);
		}

		public void buttonLayout(JButton button, int topStart, int leftStart) {
			this.putConstraint(NORTH, button, topStart, NORTH, panel);
			this.putConstraint(SOUTH, button, topStart + 15, NORTH, panel);
			this.putConstraint(WEST, button, leftStart, WEST, panel);
			this.putConstraint(EAST, button, leftStart + 115, WEST, panel);
		}

		public void labelLayout(JLabel label, int topStart, int leftStart) {
			this.putConstraint(NORTH, label, topStart, NORTH, panel);
			this.putConstraint(SOUTH, label, topStart + 15, NORTH, panel);
			this.putConstraint(WEST, label, leftStart, WEST, panel);
			this.putConstraint(EAST, label, leftStart + 75, BASELINE, label);
		}
	}

	/*
	 *
	 * Class that constructs the check boxes and sets the action of checking and
	 * clearing the boxes
	 *
	 *
	 *
	 */
	public class CheckBox extends JLabel {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private Image check = new ImageIcon("C:\\Scrape\\check.png").getImage().getScaledInstance(15, 15,
				Image.SCALE_SMOOTH);
		private ImageIcon checkIcon = new ImageIcon(check);

		CheckBox(Boolean checked, String name) {
			this.setSize(15, 15);
			if (checked) {
				this.setIcon(checkIcon);
			} else {
				this.setIcon(null);
			}
			this.setName(name);
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
						for (CheckBox box : checkBoxes) {
							if (((JLabel) e.getSource()).getName().equals(box.getName())) {
								box.setIcon(checkIcon);
							} else {
								box.setIcon(null);
							}
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
}
