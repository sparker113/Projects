import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import joblog.JobLogWells;

public class WellListSelection extends JFrame {
	private CheckBox allWells;
	private CheckBox tableWells;
	private CheckBox selectedWells;
	private CheckBox crewWells;
	private JTable table;
	private JButton button;
	private LinkedList<CheckBox> linkedBox;
	ArrayList<String> wellList = new ArrayList<>();
	ArrayList<String> crews;
	HashMap<String, HashMap<String, HashMap<String, String>>> activeWellMap;
	boolean historic;

	WellListSelection(HashMap<String, HashMap<String, HashMap<String, String>>> activeWellMap, ArrayList<String> crews,
			boolean historic) {
		this.crews = crews;
		this.activeWellMap = activeWellMap;
		this.historic = historic;
		construct();
	}

	private void construct() {
		this.setBounds(250, 250, 330, 315);
		Image image = new ImageIcon("C:\\Scrape\\Scrape.png").getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
		this.setIconImage(image);
		this.setTitle("Well Selection");
		this.getContentPane().setBackground(Color.getHSBColor((float) -.85, (float) .1, (float) .85));
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		JLabel label1 = constructBoxLabels("All Wells");
		JLabel label2 = constructBoxLabels("Crew");
		JLabel label3 = constructBoxLabels("List");
		JLabel label4 = constructBoxLabels("Directory");
		this.add(label1);
		this.add(label2);
		this.add(label3);
		this.add(label4);
		constructCrewInput();
		constructCrewLabel();
		this.linkedBox = checkBoxList();
		this.button = constructButton();

		this.add(button);
		SelectionLayout selectionLayout = new SelectionLayout(linkedBox, constructTable(), button, label1, label2,
				label3, label4);
		this.setLayout(selectionLayout);
		this.setVisible(true);
		readWellList();
	}

	public ArrayList<String> getFullList() {
		if (historic) {
			return mainFrame.getHistoricWellNames();
		}
		return JobLogWells.getArrayOfKeys(activeWellMap);
	}

	private void constructCrewInput() {
		JComboBox<String> combo = new JComboBox<>();
		combo.setEditable(false);
		combo.setName("crew_input");
		addCrewsToComboBox(combo);
		combo.setVisible(true);
		this.add(combo);
	}

	private void addCrewsToComboBox(JComboBox<String> combo) {
		for (String s : crews) {
			combo.addItem(s);
		}
	}

	private void constructCrewLabel() {
		JLabel label = new JLabel();
		label.setBackground(Color.getHSBColor(-.85f, .1f, .85f));
		label.setName("crew_label");
		label.setOpaque(true);
		label.setText("Crew:");
		add(label);
	}

	private LinkedList<CheckBox> checkBoxList() {
		LinkedList<CheckBox> linkedBox = new LinkedList<>();
		this.allWells = new CheckBox("All_Wells");
		this.crewWells = new CheckBox("Crew_Wells");
		this.tableWells = new CheckBox("Table_Wells");
		this.selectedWells = new CheckBox("Selected_Wells");
		linkedBox.add(allWells);
		linkedBox.add(crewWells);
		linkedBox.add(tableWells);
		linkedBox.add(selectedWells);
		for (CheckBox box : linkedBox) {
			this.add(box);
		}
		return linkedBox;
	}

	private JLabel constructBoxLabels(String text) {
		JLabel label = new JLabel();
		label.setBackground(Color.getHSBColor((float) -.85, (float) .1, (float) .85));

		label.setText(text);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setOpaque(true);
		label.setVisible(true);
		return label;
	}

	private JScrollPane constructTable() {
		this.table = new JTable(50, 1);
		table.getColumnModel().getColumn(0).setPreferredWidth(200);
		table.getColumnModel().getColumn(0).setHeaderValue("Wells");
		table.setVisible(true);
		JScrollPane scrollPane = new JScrollPane(table);
		this.add(scrollPane);
		return scrollPane;

	}

	private JButton constructButton() {
		JButton button = new JButton();
		button.setSize(75, 20);
		button.setBackground(Color.white);
		button.setText("Apply");
		button.setVisible(true);

		this.add(button);
		return button;
	}

	public void setButtonAction(ActionListener e) {
		this.button.addActionListener(e);
	}

	public void writeWellList() throws IOException {
		FileWriter fileWriter = new FileWriter("C:\\Scrape\\welllist.txt");
		fileWriter.write("");
		for (String s : getWells()) {
			fileWriter.append(s);
			fileWriter.append("\n");
		}
		fileWriter.flush();
		fileWriter.close();
	}

	private String getComboBoxText() {
		JComboBox<String> comboBox = (JComboBox<String>) getComponentByName("crew_input");
		return String.valueOf(comboBox.getSelectedItem());
	}

	private Component getComponentByName(String name) {
		for (Component c : getContentPane().getComponents()) {
			if (c.getName() == null) {
				continue;
			}
			if (c.getName().equals(name)) {
				return c;
			}
		}
		return null;
	}

	public ArrayList<String> getCrewWells() {
		String crew = getComboBoxText();
		if (historic) {
			ArrayList<String> array = getHistoricCrewWells(crew);
			for (String s : array) {
				System.out.println("In WellListSelection: " + s);
			}
			return array;
		}
		ArrayList<String> crewWells = JobLogWells.getActiveWellsByCrew(activeWellMap,
				crew.replace("-", "").replace("_", ""));
		System.out.println(crewWells);
		return crewWells;
	}

	public ArrayList<String> getHistoricCrewWells(String crew) {
		EvaluatedDataObject evaluatedDataObject = null;
		try {
			evaluatedDataObject = EvaluatedDataObject.getCrewFile(crew);
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		return FracCalculations.getArrayOfStringKeys(evaluatedDataObject.getSigValsMaps());
	}

	public void readWellList() {
		Executors.newSingleThreadExecutor().execute(() -> {
			FileReader fileReader = null;
			BufferedReader bufferedReader = null;
			try {
				fileReader = new FileReader("C:\\Scrape\\welllist.txt");
				bufferedReader = new BufferedReader(fileReader);
			} catch (IOException e) {
			}
			String temp;
			Integer row = 0;
			try {
				while ((temp = bufferedReader.readLine()) != null) {
					table.setValueAt(temp, row, 0);
					row++;
				}
			} catch (IOException e) {
			}
		});
	}

	public ArrayList<String> getWells() {
		ArrayList<String> wellList = new ArrayList<>();
		for (CheckBox box : linkedBox) {
			if (box.isChecked()) {
				String boxName = box.getName();
				switch (boxName) {
				case ("Selected_Wells"):
					try {
						wellList.addAll(getWellsInPath(getFullList()));
					} catch (IOException e) {
						System.out.println("Bad path");
						return null;
					}
					break;
				case ("Table_Wells"):
					wellList.addAll(getWellsInList());
					break;
				case ("Crew_Wells"):
					wellList.addAll(getCrewWells());
					break;
				default:
					wellList.addAll(getFullList());
				}
			}

		}
		return wellList;
	}

	public static ArrayList<String> getWellsInPath(ArrayList<String> allWells) throws IOException {
		ArrayList<String> pathWells = new ArrayList<>();
		File filePath = new File(ReadDirectory.readDirect());
		for (String s : filePath.list()) {
			if(checkForStringInList(allWells,s)) {
				pathWells.add(s);
			}
		}
		return pathWells;
	}

	public static boolean checkForStringInList(ArrayList<String> array, String checkString) {
		for (String s : array) {
			if (mainFrame.removeSpecialCharacters(s).toLowerCase()
					.equals(mainFrame.removeSpecialCharacters(checkString))) {
				return true;
			}
		}
		return false;
	}

	public static ArrayList<String> getWellsInPathWithFilter(String regex) throws IOException {
		ArrayList<String> pathWells = new ArrayList<>();
		File filePath = new File(ReadDirectory.readDirect());
		for (File s : filePath.listFiles()) {
			if (!s.isDirectory()) {
				continue;
			}
			Matcher matcher = Pattern.compile(regex).matcher(s.getName());
			if (matcher.find()) {
				pathWells.add(s.getName());
			}
		}
		return pathWells;
	}

	private ArrayList<String> getWellsInList() {
		ArrayList<String> wellList = new ArrayList<>();
		int row = 0;
		while (table.getValueAt(row, 0) != null && !String.valueOf(table.getValueAt(row, 0)).equals("")) {
			wellList.add(String.valueOf(table.getValueAt(row, 0)));
			row++;
		}
		return wellList;
	}

	public class CheckBox extends JLabel {
		String name;
		JLabel label;

		CheckBox(String name) {
			this.name = name;
			construct();
		}

		public void construct() {
			this.setName(name);
			this.setSize(45, 45);
			this.setBackground(Color.getHSBColor((float) -.85, (float) .1, (float) .85));
			this.label = insetBox();
			label.setHorizontalAlignment(SwingConstants.CENTER);
			this.add(label);
			this.addMouseListener(new CheckLabel());
			this.setOpaque(true);
			this.setVisible(true);
		}

		public JLabel insetBox() {
			JLabel label = new JLabel();
			label.setSize(15, 15);
			label.setOpaque(true);
			label.setVisible(true);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			label.setVerticalAlignment(SwingConstants.CENTER);
			return label;
		}

		public Boolean isChecked() {
			boolean checked = false;
			if (((JLabel) this.getComponent(0)).getIcon() != null) {
				checked = true;
			}
			return checked;
		}

		public void check() {
			if (!isChecked()) {
				ImageIcon image = new ImageIcon("C:\\Scrape\\check.png");
				ImageIcon image1 = new ImageIcon();
				image1.setImage(image.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH));
				((JLabel) this.getComponent(0)).setIcon(image1);
			}

		}
	}

	public class CheckLabel extends SwingWorker implements MouseListener {
		Executor executor = Executors.newCachedThreadPool();
		SwingWorker<Void, Void> worker;
		MouseEvent e;

		@Override
		public void mouseClicked(MouseEvent e) {
			this.e = e;
			executor.execute(() -> {
				try {
					this.doInBackground();
				} catch (Exception e1) {
				}
			});
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

		@Override
		protected Object doInBackground() throws Exception {

			for (CheckBox box : linkedBox) {

				if (e.getSource().equals(box) || e.getSource().equals(box.getComponent(0))) {
					System.out.println(box.getName());
					box.check();
				} else {
					((JLabel) box.getComponent(0)).setIcon(null);
				}
			}

			return null;
		}

	}

	public class SelectionLayout extends SpringLayout {
		@SuppressWarnings("unchecked")
		SelectionLayout(LinkedList<CheckBox> boxs, JScrollPane scrollPane, JButton button, JLabel label1, JLabel label2,
				JLabel label3, JLabel label4) {
			for (CheckBox box : boxs) {
				String name = box.getName();
				switch (name) {
				case ("All_Wells"):
					putConstraint(NORTH, box, 50, NORTH, WellListSelection.this);
					putConstraint(SOUTH, box, 80, NORTH, WellListSelection.this);
					putConstraint(WEST, box, 53, WEST, WellListSelection.this);
					putConstraint(EAST, box, 98, WEST, WellListSelection.this);

					putConstraint(NORTH, label1, 20, NORTH, WellListSelection.this);
					putConstraint(SOUTH, label1, 50, NORTH, WellListSelection.this);
					putConstraint(WEST, label1, 15, WEST, WellListSelection.this);
					putConstraint(EAST, label1, 105, WEST, WellListSelection.this);
					break;
				case ("Crew_Wells"):
					putConstraint(NORTH, box, 100, NORTH, WellListSelection.this);
					putConstraint(SOUTH, box, 130, NORTH, WellListSelection.this);
					putConstraint(WEST, box, 53, WEST, WellListSelection.this);
					putConstraint(EAST, box, 98, WEST, WellListSelection.this);

					putConstraint(NORTH, label2, 65, NORTH, WellListSelection.this);
					putConstraint(SOUTH, label2, 100, NORTH, WellListSelection.this);
					putConstraint(WEST, label2, 15, WEST, WellListSelection.this);
					putConstraint(EAST, label2, 105, WEST, WellListSelection.this);
					break;
				case ("Table_Wells"):
					putConstraint(NORTH, box, 150, NORTH, WellListSelection.this);
					putConstraint(SOUTH, box, 180, NORTH, WellListSelection.this);
					putConstraint(WEST, box, 53, WEST, WellListSelection.this);
					putConstraint(EAST, box, 98, WEST, WellListSelection.this);

					putConstraint(NORTH, label3, 115, NORTH, WellListSelection.this);
					putConstraint(SOUTH, label3, 150, NORTH, WellListSelection.this);
					putConstraint(WEST, label3, 15, WEST, WellListSelection.this);
					putConstraint(EAST, label3, 105, WEST, WellListSelection.this);
					break;
				case ("Selected_Wells"):
					putConstraint(NORTH, box, 200, NORTH, WellListSelection.this);
					putConstraint(SOUTH, box, 230, NORTH, WellListSelection.this);
					putConstraint(WEST, box, 53, WEST, WellListSelection.this);
					putConstraint(EAST, box, 98, WEST, WellListSelection.this);

					putConstraint(NORTH, label4, 165, NORTH, WellListSelection.this);
					putConstraint(SOUTH, label4, 200, NORTH, WellListSelection.this);
					putConstraint(WEST, label4, 15, WEST, WellListSelection.this);
					putConstraint(EAST, label4, 105, WEST, WellListSelection.this);
					break;
				}
				for (Component c : getContentPane().getComponents()) {
					if (c.getName() == null) {
						continue;
					}
					String compName = c.getName();
					switch (compName) {
					case ("crew_input"):
						crewInputLayout((JComboBox<String>) c);
						break;
					case ("crew_label"):
						crewInputLabelLayout((JLabel) c);
						break;
					}
				}
				putConstraint(NORTH, scrollPane, 125, NORTH, WellListSelection.this);
				putConstraint(SOUTH, scrollPane, 200, NORTH, WellListSelection.this);
				putConstraint(WEST, scrollPane, 130, WEST, WellListSelection.this);
				putConstraint(EAST, scrollPane, 305, WEST, WellListSelection.this);

				putConstraint(NORTH, button, 245, NORTH, WellListSelection.this);
				putConstraint(SOUTH, button, 270, NORTH, WellListSelection.this);
				putConstraint(WEST, button, 136, WEST, WellListSelection.this);
				putConstraint(EAST, button, 211, WEST, WellListSelection.this);
			}
		}

		void crewInputLayout(JComboBox<String> combo) {
			putConstraint(NORTH, combo, 85, NORTH, WellListSelection.this);
			putConstraint(SOUTH, combo, 110, NORTH, WellListSelection.this);
			putConstraint(WEST, combo, 170, WEST, WellListSelection.this);
			putConstraint(EAST, combo, 305, WEST, WellListSelection.this);
		}

		void crewInputLabelLayout(JLabel label) {
			putConstraint(NORTH, label, 85, NORTH, WellListSelection.this);
			putConstraint(SOUTH, label, 110, NORTH, WellListSelection.this);
			putConstraint(WEST, label, 130, WEST, WellListSelection.this);
			putConstraint(EAST, label, 170, WEST, WellListSelection.this);
		}
	}
}
