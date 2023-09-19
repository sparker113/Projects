import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import exceltransfer.DataNames;

public class OperatorTemplate extends JFrame {
	private static final long serialVersionUID = 1L;
	JPanel operatorPanel;
	OperatorTemplateTable operatorTable;
	TemplateIdentifiersTable identifiersTable;
	ArrayList<String> headers = new ArrayList<>();
	Double buttonX;
	Double buttonWidth;
	ArrayList<String> userDefinedArray;
	ArrayList<String> summaryUserDefinedArray;
	String operator;
	String path;
	Boolean addSummaryNames;

	OperatorTemplate(String operator) {
		this.addSummaryNames = false;
		this.operator = operator;
		this.userDefinedArray = new ArrayList<>();
		setPath();
		this.setBounds(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - 200,
				Toolkit.getDefaultToolkit().getScreenSize().height / 2 - 250, 400, 500);
		operatorPanel();
		this.setResizable(false);
		setButtonX();
		setButtonWidth();
		setHeaders();
		setTitle();
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		ImageIcon PP = new ImageIcon("C:\\Scrape\\Scrape.png");
		this.setIconImage(PP.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
		constructFrame();
		addRowNames(identifiersTable);
		this.setVisible(true);
	}

	OperatorTemplate() {
		setPath();
		setAddSummaryNames();
		this.userDefinedArray = new ArrayList<>();
		this.summaryUserDefinedArray = new ArrayList<>();
		this.setBounds(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - 200,
				Toolkit.getDefaultToolkit().getScreenSize().height / 2 - 250, 400, 500);
		operatorPanel();
		this.setResizable(false);
		setButtonX();
		setButtonWidth();
		setHeaders();
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle();
		ImageIcon PP = new ImageIcon("C:\\Scrape\\Scrape.png");
		this.setIconImage(PP.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
		constructFrame(true);
		this.setVisible(true);
	}

	public void setTitle() {
		this.setTitle("New Operator Template");
	}

	public void setAddSummaryNames() {
		this.addSummaryNames = false;
	}

	public void setPath() {
		this.path = "C://Scrape//Operator_Templates//" + operator + "//Sig_Values.txt";
	}

	public void addRowNames(JTable table) {

	}

	public void setButtonX() {
		this.buttonX = .6;
	}

	public Double getButtonX() {
		return this.buttonX;
	}

	public void setButtonWidth() {
		this.buttonWidth = .85;
	}

	public Double getButtonWidth() {
		return this.buttonWidth;
	}

	public void setHeaders() {

	}

	public void operatorPanel() {
		this.operatorPanel = new JPanel();
		operatorPanel.setBounds(this.getBounds());
		operatorPanel.setBackground(Color.DARK_GRAY);
		operatorPanel.setOpaque(true);
		operatorPanel.setVisible(true);
	}

	public String[] getRowNames() {
		String[] rowNames = { "Stage 1 Row", "Sheet Name", "Workbook Suffix" };
		return rowNames;
	}

	public void constructFrame() {
		// operatorPanel();
		// operatorField();
		operatorTable = new OperatorTemplateTable(50, 2);
		identifiersTable = new TemplateIdentifiersTable(10, 2);
		new TableKeyPressed(operatorTable);
		new TableKeyPressed(identifiersTable);
		JScrollPane operatorScroll = new JScrollPane(operatorTable);
		JScrollPane identifiersScroll = new JScrollPane(identifiersTable);
		OperatorTemplateButton operatorButton = new OperatorTemplateButton();
		operatorPanel.add(identifiersScroll);
		operatorPanel.add(operatorScroll);

		operatorPanel.add(operatorButton);

		OperatorSpringLayout operatorLayout = new OperatorSpringLayout(operatorScroll, operatorButton,
				identifiersScroll);
		operatorPanel.setLayout(operatorLayout);
		this.add(operatorPanel);
	}

	public void constructFrame(Boolean identifiers) {
		// operatorPanel();
		// operatorField();
		operatorTable = new OperatorTemplateTable(50, 2);
		new TableKeyPressed(operatorTable);
		JScrollPane operatorScroll = new JScrollPane(operatorTable);
		OperatorTemplateButton operatorButton = new OperatorTemplateButton();
		operatorPanel.add(operatorScroll);
		operatorPanel.add(operatorButton);
		OperatorSpringLayout operatorLayout = new OperatorSpringLayout(operatorScroll, operatorButton);
		operatorPanel.setLayout(operatorLayout);
		this.add(operatorPanel);
	}

	public class SigValuesComboBox extends JComboBox<String> {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		JTable table;

		SigValuesComboBox(JTable table) {
			this.table = table;
			this.addAll(getDefaultBoxList());
			this.setEditable(false);
			this.addPopupMenuListener(new UserDefinedActionListener(table));
		}

		public ArrayList<String> getDefaultBoxList() {
			String[] sigVals = DataNames.getDataNames();
			ArrayList<String> sigValsArray = new ArrayList<>();
			for (String s : sigVals) {
				sigValsArray.add(s);
			}
			addSummaryNames(sigValsArray);
			return sigValsArray;
		}

		public void addSummaryNames(ArrayList<String> sigValsArray) {
			if (addSummaryNames) {
				sigValsArray.addAll(DataNames.getDataNamesForSummary());
				sigValsArray.add("Summary User-Defined");
			}
		}

		public void addAll(ArrayList<String> defaultBoxList) {
			for (String s : defaultBoxList) {
				this.addItem(s);
			}
		}

		private class UserDefinedActionListener implements PopupMenuListener {
			JTable table;

			UserDefinedActionListener(JTable table) {
				this.table = table;
			}

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

				SwingWorker<Void, String> worker = new SwingWorker<>() {
					int i = 0;

					public Void doInBackground() {
						String selected = SigValuesComboBox.this.getSelectedItem().toString();
						if (selected.equals("User-Defined")) {
							String userDefined = JOptionPane.showInputDialog(OperatorTemplate.this,
									"Input the identifier for the data required for this operator");
							i = table.getSelectedRow();
							userDefinedArray.add(userDefined);
							publish(userDefined);
							publish(userDefined);
						} else if (selected.equals("Summary User-Defined")) {
							String userDefined = JOptionPane.showInputDialog(OperatorTemplate.this,
									"Input the name for the data you want to define");
							i = table.getSelectedRow();
							userDefinedArray.add("First" + userDefined + "Cell");
							publish(userDefined);
							publish(userDefined);
						}
						return null;
					}

					public void process(List<String> chunks) {
						boolean set = false;
						for (String s : chunks) {
							if (!set) {
								((SigValuesComboBox) e.getSource()).addItem(s);
								set = !set;
							} else {
								System.out.println(table.getSelectedRow() + " - " + i);
								table.setValueAt(s, i, 0);

							}

						}
					}

				};
				worker.execute();
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {

			}

		}
	}

	private class OperatorTemplateTable extends JTable {

		private static final long serialVersionUID = 1L;

		OperatorTemplateTable(int rows, int columns) {
			super(rows, columns);
			setStageValueNames();
			this.setCellSelectionEnabled(true);
		}

		private void setStageValueNames() {
			this.getColumnModel().getColumn(0).setHeaderValue("Data Name");
			this.getColumnModel().getColumn(1).setHeaderValue("Column");
			this.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new SigValuesComboBox(this)));
		}

	}

	class TemplateIdentifiersTable extends JTable {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		TemplateIdentifiersTable(int rows, int columns) {
			super(rows, columns);
			setRowNames();
			this.setCellSelectionEnabled(true);
		}

		private void setRowNames() {
			String[] rowNames = getRowNames();
			int i;
			this.getColumnModel().getColumn(0).setHeaderValue("Identifier (*=if applicable)");
			this.getColumnModel().getColumn(1).setHeaderValue("Input");

			for (i = 0; i < rowNames.length; i++) {
				this.setValueAt(rowNames[i], i, 0);
			}
		}
	}

	private class OperatorSpringLayout extends SpringLayout {
		OperatorSpringLayout(JScrollPane operatorScroll, JButton operatorButton, JScrollPane identifiersScroll) {
			this.putConstraint(NORTH, operatorScroll, 0, NORTH, operatorPanel);
			this.putConstraint(WEST, operatorScroll, 0, WEST, operatorPanel);
			this.putConstraint(EAST, operatorScroll, operatorPanel.getWidth() - 15, WEST, operatorPanel);
			this.putConstraint(SOUTH, operatorScroll, 300, NORTH, operatorPanel);

			this.putConstraint(NORTH, identifiersScroll, 300, NORTH, operatorPanel);
			this.putConstraint(WEST, identifiersScroll, 0, WEST, operatorPanel);
			this.putConstraint(EAST, identifiersScroll, operatorPanel.getWidth() - 15, WEST, operatorPanel);
			this.putConstraint(SOUTH, identifiersScroll, 400, NORTH, operatorPanel);

			this.putConstraint(NORTH, operatorButton, 420, NORTH, operatorPanel);
			this.putConstraint(WEST, operatorButton, (int) (operatorPanel.getWidth() * getButtonX()), WEST,
					operatorPanel);
			this.putConstraint(EAST, operatorButton, (int) (operatorPanel.getWidth() * getButtonWidth()), WEST,
					operatorPanel);
			this.putConstraint(SOUTH, operatorButton, 445, NORTH, operatorPanel);

		}

		OperatorSpringLayout(JScrollPane operatorScroll, JButton operatorButton) {
			this.putConstraint(NORTH, operatorScroll, 0, NORTH, operatorPanel);
			this.putConstraint(WEST, operatorScroll, 0, WEST, operatorPanel);
			this.putConstraint(EAST, operatorScroll, operatorPanel.getWidth() - 15, WEST, operatorPanel);
			this.putConstraint(SOUTH, operatorScroll, 400, NORTH, operatorPanel);

			this.putConstraint(NORTH, operatorButton, 420, NORTH, operatorPanel);
			this.putConstraint(WEST, operatorButton, (int) (operatorPanel.getWidth() * getButtonX()) - 75, WEST,
					operatorPanel);
			this.putConstraint(EAST, operatorButton, (int) (operatorPanel.getWidth() * getButtonWidth()) - 75, WEST,
					operatorPanel);
			this.putConstraint(SOUTH, operatorButton, 445, NORTH, operatorPanel);

		}
	}

	private class OperatorTemplateButton extends JButton {

		private static final long serialVersionUID = 1L;

		OperatorTemplateButton() {
			this.setSize(45, 25);
			this.setVisible(true);
			this.setText("Save");
			WriteOperatorTemplate writeTemplate = new WriteOperatorTemplate();
			this.addActionListener(writeTemplate);
		}
	}

	public static SortedMap<String, String> getTemplateMap(JTable table) {
		TreeMap<String, String> templateMap = new TreeMap<>();
		boolean perfsSet = false;
		boolean tvdSet = false;
		int nextRow = 0;
		int i;
		for (i = 0; i < table.getRowCount(); i++) {
			if (table.getValueAt(i, 0) != null && !String.valueOf(table.getValueAt(i, 0)).equals("")) {
				String inputString = table.getValueAt(i, 1) != null && !table.getValueAt(i, 1).equals("")
						? String.valueOf(table.getValueAt(i, 1))
						: "N0_DICE";
				// System.out.println(inputString);
				if (String.valueOf(table.getValueAt(i, 0)).equals("Perfs")) {
					perfsSet = true;
				} else if (String.valueOf(table.getValueAt(i, 0)).equals("TVD")) {
					tvdSet = true;
				}
				templateMap.put(String.valueOf(table.getValueAt(i, 0)), inputString);
			} else {
				nextRow = 0;
				break;
			}
		}
		return templateMap;
	}

	public String getLowerIntValue(String a) {
		Integer lowerInt;
		String[] valueString = a.toLowerCase().split(",");
		String returnString = "";
		StringBuilder stringBuilder = new StringBuilder();
		for (String s : valueString) {
			lowerInt = 0;
			IntStream value = s.chars();
			Iterator<Integer> valueIterator = value.iterator();
			int i = 0;
			while (valueIterator.hasNext()) {
				lowerInt = valueIterator.next() - 97 + (lowerInt + i) * 26;
				i++;
			}
			stringBuilder.append(lowerInt);
			stringBuilder.append(",");
		}
		returnString = stringBuilder.substring(0, stringBuilder.lastIndexOf(","));
		return returnString;
	}

	public TreeMap<String, String> checkSheets(SortedMap<String, String> templateMap) {
		String[] sheetNames = { "Chemicals Sheet Name*", "Sand Sheet Name*" };
		TreeMap<String, String> tempMap = new TreeMap<>();
		for (String s : sheetNames) {
			System.out.println(s);
			if (templateMap.get(s) == null || templateMap.get(s).equals("NO_DICE")) {
				templateMap.remove(s);
				tempMap.put(s, templateMap.get("Sheet Name"));
			}
		}
		return tempMap;
	}

	public void writeTemplate() throws IOException {
		SortedMap<String, String> templateMap = getTemplateMap(operatorTable);
		File file = new File(path);

		FileWriter fileWriter = new FileWriter(file);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		bufferedWriter.write("");
		String temp;
		int i = 0;
		for (String a : templateMap.keySet()) {
			if (templateMap.get(a) == null) {
				continue;
			}
			Matcher matcher = Pattern.compile("\\D").matcher(templateMap.get(a));
			Boolean matched = matcher.find();
			String value = templateMap.get(a);
			if ((matched && templateMap.get(a).length() < 3) || templateMap.get(a).contains(",")) {

				value = getLowerIntValue(templateMap.get(a));
			} else if (matched || a.toUpperCase().contains("OFFSET")) {
				value = String.valueOf(templateMap.get(a));
			} else {
				value = String.valueOf(Integer.valueOf(value) - Integer.valueOf(1));
			}
			temp = a + ":" + value;
			System.out.println(temp);
			bufferedWriter.append(temp);
			bufferedWriter.newLine();
			i++;
		}

		bufferedWriter.close();
	}

	public void writeTemplate(String operator) throws IOException {
		SortedMap<String, String> templateMap = getTemplateMap(operatorTable);
		if (!addSummaryNames) {
			templateMap.putAll(getTemplateMap(identifiersTable));
		}
		templateMap.putAll(checkSheets(templateMap));
		File directory = new File("C:\\Scrape\\Operator_Templates\\" + operator);
		if (!directory.exists()) {
			directory.mkdir();
		}
		FileWriter fileWriter = new FileWriter(new File(path));
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		bufferedWriter.write("");
		String temp;
		int i = 0;
		for (String a : templateMap.keySet()) {
			if (templateMap.get(a) == null) {
				continue;
			}
			Matcher matcher = Pattern.compile("\\D").matcher(templateMap.get(a));
			Boolean matched = matcher.find();
			String value = templateMap.get(a);
			if ((matched && templateMap.get(a).length() < 3) || templateMap.get(a).contains(",")) {

				value = getLowerIntValue(templateMap.get(a));
			} else if (matched || a.toUpperCase().contains("OFFSET")) {
				value = String.valueOf(templateMap.get(a));
			} else {
				value = String.valueOf(Integer.valueOf(value) - Integer.valueOf(1));
			}
			temp = a + ":" + value;
			System.out.println(temp);
			bufferedWriter.append(temp);
			bufferedWriter.newLine();
			i++;
		}

		bufferedWriter.close();
	}

	private class WriteOperatorTemplate extends AbstractAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			try {

				if (!addSummaryNames) {
					writeTemplate(operator);
					ArrayList<String> previousUserDefined = DataNames.readUserDefinedNames(operator);
					previousUserDefined.addAll(userDefinedArray);
					UserDefinedFrame.writeNamesToText(previousUserDefined, operator);
				} else {
					writeTemplate();
				}
			} catch (IOException e1) {
				System.out.println("IOException writing OperatorTemplate");
			}
			OperatorTemplate.this.dispose();
		}

	}

}
