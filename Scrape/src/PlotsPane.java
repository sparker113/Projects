import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

public class PlotsPane {

	private JFrame plotsFrame;
	private JPanel tablesPane;
	private JPanel buttonPane;
	JTable primaryTable;
	JTable chemsTable;
	private JLabel tempLabel;
	private JButton mainButton;
	SelectColorPane selectColorPane;
	LinkedList<JLabel> primaryLabels = new LinkedList<>();
	LinkedList<JLabel> chemLabels = new LinkedList<>();
	private ArrayList<String> chemChannels = new ArrayList<>();
	private JLabel factorLabel;
	private LinkedList<CheckBox> boxListPrimary;
	private LinkedList<CheckBox> boxListChem;
	JTextField casingFactor;

	public PlotsPane() throws IOException {
		mainButton = new JButton();
		plotsFrame = new JFrame();
		tablesPane = new JPanel();
		buttonPane = new JPanel();
		factorLabel = new JLabel();
		casingFactor = new JTextField();
		primaryTable = new JTable(20, 2);
		chemsTable = new JTable(20, 2);
		primaryTable.setCellSelectionEnabled(true);
		chemsTable.setCellSelectionEnabled(true);
		TableKeyPressed tableKeyPressedPrimary = new TableKeyPressed(primaryTable);
		TableKeyPressed tableKeyPressedChems = new TableKeyPressed(chemsTable);

		mainButton.setSize(50, 25);
		mainButton.setText("Save");
		mainButton.setHorizontalAlignment(SwingConstants.CENTER);
		mainButton.setVerticalAlignment(SwingConstants.TOP);
		mainButton.setOpaque(true);

		// factorLabel.setSize((int).15*plotsFrame.getWidth(),25);
		factorLabel.setBackground(Color.lightGray);
		factorLabel.setText("Casing Factor");
		factorLabel.setOpaque(true);
		factorLabel.setHorizontalAlignment(SwingConstants.CENTER);
		// casingFactor.setSize(factorLabel.getSize());
		casingFactor.setBackground(Color.WHITE);
		casingFactor.setEditable(true);
		LoadInformation.loadCasingFactor(casingFactor);
		casingFactor.setOpaque(true);
		casingFactor.setHorizontalAlignment(SwingConstants.CENTER);
		buttonPane.add(factorLabel);
		buttonPane.add(casingFactor);

		plotsFrame.setTitle("Plots Configuration");
		ImageIcon scrape = new ImageIcon("C:\\Scrape\\Scrape.png");
		Image scrape1 = scrape.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
		plotsFrame.setIconImage(scrape1);
		plotsFrame.setBounds(250, 250, 350, 400);
		plotsFrame.setResizable(true);
		tablesPane.setBounds(0, 0, (int) (plotsFrame.getBounds().getWidth()), plotsFrame.getBounds().height - 80);

		buttonPane.setBounds(0, tablesPane.getHeight(), plotsFrame.getWidth(), 80);
		buttonPane.setBackground(Color.DARK_GRAY);

		buttonPane.add(mainButton);
		buttonPane.add(factorLabel);
		buttonPane.add(casingFactor);
		ButtonPaneLayout buttonPaneLayout = new ButtonPaneLayout(factorLabel, casingFactor, buttonPane, mainButton);
		buttonPane.setLayout(buttonPaneLayout);
		buttonPane.setOpaque(true);
		tablesPane.setBackground(Color.DARK_GRAY);
		tablesPane.setOpaque(true);
		tablesPane.setVisible(true);
		buttonPane.setVisible(true);

		// System.out.println(primaryTable.getRowHeight());
		primaryTable.getColumnModel().getColumn(0).setHeaderValue("Primary Channels");
		primaryTable.getColumnModel().getColumn(1).setHeaderValue("Y-Axis Scale");

		chemsTable.getColumnModel().getColumn(0).setHeaderValue("Chemical Channel");
		chemsTable.getColumnModel().getColumn(1).setHeaderValue("Y-Axis Scale");

		JScrollPane primaryScroll = new JScrollPane(primaryTable);
		JScrollPane chemsScroll = new JScrollPane(chemsTable);
		primaryScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		chemsScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

		SpringLayout plotsLayout = new SpringLayout();
		tablesPane.add(primaryScroll);
		tablesPane.add(chemsScroll);
		plotsLayout.putConstraint(SpringLayout.NORTH, primaryScroll, 10, SpringLayout.NORTH, tablesPane);
		plotsLayout.putConstraint(SpringLayout.SOUTH, primaryScroll, tablesPane.getHeight() / 2 - 5, SpringLayout.NORTH,
				tablesPane);
		plotsLayout.putConstraint(SpringLayout.NORTH, chemsScroll, tablesPane.getHeight() / 2 + 5, SpringLayout.NORTH,
				tablesPane);
		plotsLayout.putConstraint(SpringLayout.SOUTH, chemsScroll, tablesPane.getHeight() - 10, SpringLayout.NORTH,
				tablesPane);
		plotsLayout.putConstraint(SpringLayout.WEST, primaryScroll, 15, SpringLayout.WEST, tablesPane);
		plotsLayout.putConstraint(SpringLayout.EAST, primaryScroll, (int) (tablesPane.getWidth() * .65),
				SpringLayout.WEST, tablesPane);
		plotsLayout.putConstraint(SpringLayout.WEST, chemsScroll, 15, SpringLayout.WEST, tablesPane);
		plotsLayout.putConstraint(SpringLayout.EAST, chemsScroll, (int) (tablesPane.getWidth() * .65),
				SpringLayout.WEST, tablesPane);

		int i;
		for (i = 0; i < 8; i++) {
			primaryLabels.add(new JLabel());
			chemLabels.add(new JLabel());
		}
		int primY = 28;
		int chemY = tablesPane.getHeight() / 2 + 25;
		// colorsPane.setVisible(true);
		// SpringLayout colorsLayout = new SpringLayout();
		Integer Ind = 0;
		LabelListen labelListen = new LabelListen();
		for (JLabel p : primaryLabels) {
			p.setBackground(Color.WHITE);
			p.setForeground(Color.WHITE);
			p.setOpaque(true);
			p.setSize(50, 14);
			p.setName(String.valueOf(Ind));
			p.addMouseListener(labelListen);
			p.setVisible(true);
			tablesPane.add(p);
			plotsLayout.putConstraint(SpringLayout.NORTH, p, primY + 2, SpringLayout.NORTH, plotsFrame);
			plotsLayout.putConstraint(SpringLayout.SOUTH, p, primY + 14, SpringLayout.NORTH, plotsFrame);
			plotsLayout.putConstraint(SpringLayout.WEST, p, (int) (tablesPane.getWidth() * .70), SpringLayout.WEST,
					plotsFrame);
			plotsLayout.putConstraint(SpringLayout.EAST, p, (int) (tablesPane.getWidth() * .80), SpringLayout.WEST,
					tablesPane);
			Ind++;
			primY = primY + 16;
		}
		boxListPrimary = addCheckBoxes("Primary", 8, 12, 4, plotsLayout);
		int numBox = 0;
		addBoxLayoutToList(boxListPrimary, 30, 12, 4, plotsLayout);
		Ind = 0;
		for (JLabel c : chemLabels) {
			c.setVisible(true);
			c.setOpaque(true);
			c.setBackground(Color.BLUE);
			c.setForeground(Color.WHITE);
			c.setSize(50, 14);
			c.setName(String.valueOf(Ind));
			c.addMouseListener(labelListen);
			tablesPane.add(c);
			plotsLayout.putConstraint(SpringLayout.NORTH, c, chemY + 2, SpringLayout.NORTH, plotsFrame);
			plotsLayout.putConstraint(SpringLayout.SOUTH, c, chemY + 14, SpringLayout.NORTH, plotsFrame);
			plotsLayout.putConstraint(SpringLayout.WEST, c, (int) (tablesPane.getWidth() * .70), SpringLayout.WEST,
					plotsFrame);
			plotsLayout.putConstraint(SpringLayout.EAST, c, (int) (tablesPane.getWidth() * .80), SpringLayout.WEST,
					plotsFrame);
			chemY = chemY + 16;
			Ind++;
		}
		boxListChem = addCheckBoxes("Chemical", 8, 12, 4, plotsLayout);
		LoadInformation.loadChecks(boxListChem, "C:\\Scrape\\smooth_chem.txt");

		addBoxLayoutToList(boxListChem, tablesPane.getHeight() / 2 + 27, 12, 4, plotsLayout);

		// Drawing Dividing lines in the frame
		ArrayList<HashMap<String, Integer>> lineList = new ArrayList<>();
		lineList.add(makeLineMap((int) (tablesPane.getWidth() * .835), 0, (int) (tablesPane.getWidth() * .835),
				tablesPane.getHeight()));
		lineList.add(makeLineMap(0, primY + 5, plotsFrame.getWidth(), primY + 5));
		lineList.add(makeLineMap(0, tablesPane.getHeight(), plotsFrame.getWidth(), tablesPane.getHeight()));
		plotsFrame.add(drawLines(lineList));

		// Constructing Text Labels for the Color Selection Labels and Smoothing Check
		// Boxes
		String colorText = "Plot Color";
		String smoothText = "Smooth Curve";
		int wid = 50;
		Dimension d = new Dimension((int) (tablesPane.getWidth() * .1), 50);
		JLabel colorLabel = actionColumnLabel(colorText, d);
		JLabel smoothLabel = actionColumnLabel(smoothText, d);
		tablesPane.add(colorLabel);
		tablesPane.add(smoothLabel);
		columnLabelLayout(0, 28, (tablesPane.getWidth()), (int) (tablesPane.getWidth() * .85), smoothLabel,
				plotsLayout);
		columnLabelLayout(0, 28, (int) (tablesPane.getWidth() * .8), (int) (tablesPane.getWidth() * .72), colorLabel,
				plotsLayout);
		buttonPane.setVisible(true);
		plotsFrame.add(buttonPane);
		tablesPane.setLayout(plotsLayout);
		plotsFrame.add(tablesPane);

		LoadInformation.loadPrimaryInfo(primaryTable, primaryLabels);
		chemChannels = LoadInformation.loadChemInfo(chemsTable, chemLabels);

		plotsFrame.setVisible(false);
		plotsFrame.setAlwaysOnTop(true);
		plotsFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
	}

	public LinkedList<CheckBox> getBoxListChem() {
		return this.boxListChem;
	}

	public static ArrayList<String> getInputArray(JTable table) {
		ArrayList<String> inputArray = new ArrayList<>();
		int i = 0;
		while (i < table.getRowCount() && table.getValueAt(i, 0) != null && !table.getValueAt(i, 0).equals("")) {
			inputArray.add(String.valueOf(table.getValueAt(i, 0)));
			i++;
		}
		return inputArray;
	}

	public void columnLabelLayout(int n, int s, int e, int w, JLabel label, SpringLayout layout) {
		layout.putConstraint(SpringLayout.NORTH, label, n, SpringLayout.NORTH, tablesPane);
		layout.putConstraint(SpringLayout.SOUTH, label, s, SpringLayout.NORTH, tablesPane);
		layout.putConstraint(SpringLayout.WEST, label, w, SpringLayout.WEST, tablesPane);
		layout.putConstraint(SpringLayout.EAST, label, e, SpringLayout.WEST, tablesPane);
	}

	public DividerLines drawLines(ArrayList<HashMap<String, Integer>> lineList) {
		DividerLines dividerLines = new DividerLines(plotsFrame.getSize());
		int lineX = (int) (tablesPane.getWidth() * .835);
		for (HashMap<String, Integer> map : lineList) {
			dividerLines.addLine(map.get("x1"), map.get("y1"), map.get("x2"), map.get("y2"), Color.black);
		}
		return dividerLines;
	}

	public HashMap<String, Integer> makeLineMap(int x1, int y1, int x2, int y2) {
		HashMap<String, Integer> map = new HashMap<>();
		map.put("x1", x1);
		map.put("y1", y1);
		map.put("x2", x2);
		map.put("y2", y2);
		return map;
	}

	public JLabel actionColumnLabel(String text, Dimension dim) {
		JLabel label = new JLabel();
		label.setSize(dim);
		label.setBackground(Color.DARK_GRAY);
		label.setOpaque(true);
		label.setForeground(Color.white);
		label.setFont(new Font("Serif", Font.PLAIN, 12));
		label.setText(String.format("<html><div style=\"width:%dpx\" m>%s</div></html>",
				(int) (tablesPane.getWidth() * .1), text));
		return label;
	}

	public void setButtonAction(ActionListener e) {
		mainButton.addActionListener(e);
	}

	public Double getCasingFactor() {
		double dCasingFactor = Double.parseDouble(casingFactor.getText());
		return dCasingFactor;
	}

	public void setChemChannels(ArrayList<String> chemChannels) {
		this.chemChannels = chemChannels;
	}

	public ArrayList<String> getChemChannels() {
		return this.chemChannels;
	}

	public void setColorPane() {
		this.selectColorPane = new SelectColorPane();
	}

	public SelectColorPane getColorPane() {
		return this.selectColorPane;
	}

	public void setVisible(Boolean visible) {
		plotsFrame.setVisible(visible);
	}

	public Boolean isVisible() {
		return plotsFrame.isVisible();
	}

	public void setTempLabel(JLabel label) {
		this.tempLabel = label;
	}

	public JLabel getTempLabel() {
		return this.tempLabel;
	}

	public class LabelListen implements MouseListener {
		@Override
		public void mouseClicked(MouseEvent e) {
			setTempLabel((JLabel) e.getComponent());
			if (selectColorPane.equals(null)) {
				setColorPane();
			} else {
				selectColorPane.setVisible(true);
			}
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

	public class ButtonPaneLayout extends SpringLayout {
		ButtonPaneLayout(JLabel factorLabel, JTextField casingFactor, JPanel buttonPane, JButton button) {
			this.putConstraint(SpringLayout.NORTH, factorLabel, 5, SpringLayout.NORTH, buttonPane);
			this.putConstraint(SpringLayout.SOUTH, factorLabel, 20, SpringLayout.NORTH, buttonPane);
			this.putConstraint(SpringLayout.WEST, factorLabel, (int) (plotsFrame.getWidth() * .125), SpringLayout.WEST,
					buttonPane);
			this.putConstraint(SpringLayout.EAST, factorLabel, (int) (plotsFrame.getWidth() * .45), SpringLayout.WEST,
					buttonPane);
			this.putConstraint(SpringLayout.NORTH, casingFactor, 20, SpringLayout.NORTH, buttonPane);
			this.putConstraint(SpringLayout.SOUTH, casingFactor, 35, SpringLayout.NORTH, buttonPane);
			this.putConstraint(SpringLayout.WEST, casingFactor, (int) (plotsFrame.getWidth() * .125), SpringLayout.WEST,
					buttonPane);
			this.putConstraint(SpringLayout.EAST, casingFactor, (int) (plotsFrame.getWidth() * .45), SpringLayout.WEST,
					buttonPane);
			this.putConstraint(SpringLayout.NORTH, button, 10, SpringLayout.NORTH, buttonPane);
			this.putConstraint(SpringLayout.WEST, button, (int) (plotsFrame.getWidth() * .55), SpringLayout.WEST,
					buttonPane);
			this.putConstraint(SpringLayout.EAST, button, (int) (plotsFrame.getWidth() * .8), SpringLayout.WEST,
					buttonPane);
		}
	}

	public class SelectColorPane {
		private JButton selectButton;
		private JPanel buttonPane;
		private JDialog colorsFrame;
		private JPanel colorsPane;
		private JColorChooser colorChooser;

		public SelectColorPane() {
			selectButton = new JButton();
			buttonPane = new JPanel();
			colorsFrame = new JDialog();
			colorsPane = new JPanel();
			colorChooser = new JColorChooser();
			colorChooser.setAlignmentY(SwingConstants.BOTTOM);
			colorsFrame.setBounds(250, 250, 650, 400);

			colorsPane.setBounds(0, 0, 650, 315);
			colorsPane.add(colorChooser);
			colorsPane.setBackground(Color.CYAN);
			colorsPane.setLayout(new FlowLayout());

			selectButton.setHorizontalAlignment(SwingConstants.CENTER);
			selectButton.setVerticalAlignment(SwingConstants.CENTER);
			selectButton.setText("Select Color");
			selectButton.setOpaque(true);
			selectButton.setSize(50, 25);
			selectButton.setText("Select");

			buttonPane.setOpaque(true);
			buttonPane.setBounds(0, 315, 650, 85);
			buttonPane.add(selectButton);
			buttonPane.setBackground(Color.DARK_GRAY);
			buttonPane.setLayout(new FlowLayout());

			colorsFrame.add(buttonPane);
			colorsFrame.add(colorsPane);
			colorsFrame.setVisible(false);

			ColorSelectAction colorSelectAction = new ColorSelectAction();
			selectButton.addActionListener(colorSelectAction);

		}

		public void setVisible(Boolean visible) {
			colorsFrame.setVisible(visible);
		}

		public void dispose() {
			colorsFrame.dispose();
		}

		public Boolean isVisible() {
			return colorsFrame.isVisible();
		}

		public JDialog getColorsFrame() {
			return colorsFrame;
		}

		public class ColorSelectAction extends AbstractAction {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				int red = colorChooser.getSelectionModel().getSelectedColor().getRed();
				int green = colorChooser.getSelectionModel().getSelectedColor().getGreen();
				int blue = colorChooser.getSelectionModel().getSelectedColor().getBlue();
				tempLabel.setText(red + "," + green + "," + blue);
				tempLabel.setForeground(colorChooser.getSelectionModel().getSelectedColor());
				tempLabel.setBackground(colorChooser.getSelectionModel().getSelectedColor());
				colorsFrame.setAlwaysOnTop(true);
				colorsFrame.setVisible(false);
			}
		}
	}

	public ArrayList<String> loadSmoothChecks(String path) throws IOException {
		ArrayList<String> checkedArray = new ArrayList<>();
		FileReader reader = new FileReader(new File(path));
		BufferedReader bufferedReader = new BufferedReader(reader);
		String temp;
		while ((temp = bufferedReader.readLine()) != null) {
			checkedArray.add(temp);
		}
		return checkedArray;
	}

	public CheckBox constructCheckBox(String prefix, Integer indexBox) {
		Boolean checked = false;
		ArrayList<String> checkedArray = null;
		try {
			checkedArray = loadSmoothChecks("C:\\Scrape\\smooth_checks.txt");
			if (checkedArray.contains(prefix + String.valueOf(indexBox))) {
				checked = true;
			}
		} catch (IOException e) {
			checkedArray = null;
		}

		CheckBox box = new CheckBox(checked, 12);
		box.setName(prefix + String.valueOf(indexBox));
		return box;
	}

	public LinkedList<CheckBox> addCheckBoxes(String prefix, int boxCount, int boxDim, int vertSpacing,
			SpringLayout layout) {

		LinkedList<CheckBox> boxList = new LinkedList<>();
		for (Integer i = 1; i <= 8; i++) {
			CheckBox box = constructCheckBox(prefix, i);
			box.setName(prefix + String.valueOf(i));
			box.setOpaque(true);
			box.setVisible(true);
			boxList.add(box);
		}
		return boxList;
	}

	public void addBoxLayoutToList(LinkedList<CheckBox> list, int top, int dim, int space, SpringLayout layout) {
		int newTop = top;
		int i = 0;
		for (CheckBox box : list) {
			newTop = top + (dim + space) * i;
			tablesPane.add(box);
			boxLayout(newTop, (int) (plotsFrame.getWidth() * .87 + dim), newTop + dim,
					(int) (plotsFrame.getWidth() * .87), box, layout);
			i++;
		}
	}

	public void boxLayout(int n, int e, int s, int w, CheckBox box, SpringLayout layout) {
		layout.putConstraint(SpringLayout.NORTH, box, n, SpringLayout.NORTH, tablesPane);
		layout.putConstraint(SpringLayout.EAST, box, e, SpringLayout.WEST, tablesPane);
		layout.putConstraint(SpringLayout.SOUTH, box, s, SpringLayout.NORTH, tablesPane);
		layout.putConstraint(SpringLayout.WEST, box, w, SpringLayout.WEST, tablesPane);
	}

	public static class SaveInformation {
		public static void savePrimaryInfo(JTable primaryTable, LinkedList<JLabel> primaryColors) throws IOException {
			int i = 0;
			String colorString;
			FileWriter fileWriter = new FileWriter(new File("C:\\Scrape\\Primary_Plots_Info.txt"));
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write("");
			while (primaryTable.getValueAt(i, 0) != null && !String.valueOf(primaryTable.getValueAt(i, 0)).equals("")
					&& i < primaryTable.getRowCount()) {
				colorString = String.valueOf(primaryColors.get(i).getBackground().getRed()) + ","
						+ String.valueOf(primaryColors.get(i).getBackground().getGreen()) + ","
						+ String.valueOf(primaryColors.get(i).getBackground().getBlue());
				bufferedWriter.append(String.valueOf(primaryTable.getValueAt(i, 0)) + ":"
						+ String.valueOf(primaryTable.getValueAt(i, 1)) + ":" + colorString);
				bufferedWriter.append("\n");
				i++;
			}
			bufferedWriter.flush();
			bufferedWriter.close();
		}

		public static void saveChemInfo(JTable chemTable, LinkedList<JLabel> chemColors) throws IOException {
			int i = 0;
			String colorString;
			FileWriter fileWriter = new FileWriter(new File("C:\\Scrape\\Chemical_Plots_Info.txt"));
			fileWriter.write("");
			while (chemTable.getValueAt(i, 0) != null && !String.valueOf(chemTable.getValueAt(i, 0)).equals("")
					&& i < chemTable.getRowCount()) {
				colorString = String.valueOf(chemColors.get(i).getBackground().getRed()) + ","
						+ String.valueOf(chemColors.get(i).getBackground().getGreen()) + ","
						+ String.valueOf(chemColors.get(i).getBackground().getBlue());
				fileWriter.append(String.valueOf(chemTable.getValueAt(i, 0)) + ":"
						+ String.valueOf(chemTable.getValueAt(i, 1)) + ":" + colorString);
				fileWriter.append("\n");
				i++;

			}
			fileWriter.flush();
			fileWriter.close();
		}

		public static void saveCasingFactor(String casingFactor) throws IOException {
			FileWriter fileWriter = new FileWriter(new File("C:\\Scrape\\Casing_Factor.txt"));
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(casingFactor);
			bufferedWriter.flush();
			bufferedWriter.close();
		}

		public static void saveDictOnUpdate(String chemsInfoPath, String primaryInfoPath, channelData channels)
				throws NumberFormatException, IOException {
			FileReader primaryReader = new FileReader(new File(primaryInfoPath));
			BufferedReader primaryBuffer = new BufferedReader(primaryReader);
			StringBuilder colorDict = new StringBuilder();
			StringBuilder primaryDict = new StringBuilder();
			StringBuilder chemDict = new StringBuilder();
			StringBuilder scaleDict = new StringBuilder();
			String temp;
			Float tempFloat;
			int i = 0;
			while ((temp = primaryBuffer.readLine()) != null) {
				primaryDict.append(temp.split(":")[0]);
				if (temp.split(":")[0].toUpperCase().contains("PRESSURE")
						|| temp.split(":")[0].toUpperCase().contains("BACKSIDE")) {
					primaryDict.append("_psi");
				} else if (temp.split(":")[0].toUpperCase().contains("RATE")) {
					primaryDict.append("_bpm");
				} else if (temp.split(":")[0].toUpperCase().contains("DENSITY")
						|| temp.split(":")[0].toUpperCase().contains("PROP")) {
					primaryDict.append("_ppg");
				}

				primaryDict.append("\n");
				colorDict.append(temp.split(":")[0]);
				colorDict.append(":");
				for (String rgb : temp.split(":")[2].split(",")) {
					tempFloat = Float.valueOf(rgb) / Float.valueOf(255);
					colorDict.append(String.format("%.1f", tempFloat));
					if (i < 2) {
						colorDict.append(",");
						i++;
					} else {
						i = 0;
					}
				}
				colorDict.append("\n");
				scaleDict.append(temp.split(":")[0]);
				scaleDict.append(":");
				scaleDict.append(temp.split(":")[1]);
				scaleDict.append("\n");
			}
			primaryBuffer.close();
			FileReader chemsReader = new FileReader(new File(chemsInfoPath));
			BufferedReader chemsBuffer = new BufferedReader(chemsReader);

			while ((temp = chemsBuffer.readLine()) != null) {
				for (String a : channels.getOriginalName()) {
					Matcher matcher = Pattern.compile("LA(\\.?)\\d(\\.*)").matcher(a);
					if (matcher.find()) {
						System.out.println(a);
					}
					if (a.toUpperCase().replace(" ", "").replace("-", "")
							.contains(temp.split(":")[0].toUpperCase().replace(" ", ""))
							&& !a.toUpperCase().contains("RATE") && !a.toUpperCase().contains("TOTAL")) {
						chemDict.append(a);
						chemDict.append("_gpt");
						chemDict.append("\n");
						colorDict.append(a);
						colorDict.append(":");
						scaleDict.append(a);
						scaleDict.append(":");
						scaleDict.append(temp.split(":")[1]);
						scaleDict.append("\n");
						break;
					}
				}

				for (String rgb : temp.split(":")[2].split(",")) {
					tempFloat = Float.valueOf(rgb) / Float.valueOf(255);
					colorDict.append(String.format("%.1f", tempFloat));
					if (i < 2) {
						colorDict.append(",");
						i++;
					} else {
						i = 0;
					}
				}
				colorDict.append("\n");
			}
			chemsBuffer.close();
			writeColorDict(colorDict);
			writeDict(scaleDict, "C:\\Scrape\\ScrapePython\\Plot\\ScaleDict.txt");
			writeDict(chemDict, "C:\\Scrape\\ScrapePython\\Plot\\CPchannels.txt");
		}

		public static void saveColorDict(String chems, String primary) throws IOException {
			FileReader primaryReader = new FileReader(new File(primary));
			BufferedReader primaryBuffer = new BufferedReader(primaryReader);
			StringBuilder colorDict = new StringBuilder();
			StringBuilder primaryDict = new StringBuilder();
			StringBuilder chemDict = new StringBuilder();
			StringBuilder scaleDict = new StringBuilder();
			String temp;
			Float tempFloat;
			int i = 0;
			while ((temp = primaryBuffer.readLine()) != null) {
				primaryDict.append(temp.split(":")[0]);
				if (temp.split(":")[0].toUpperCase().contains("PRESSURE")
						|| temp.split(":")[0].toUpperCase().contains("BACKSIDE")) {
					primaryDict.append("_psi");
				} else if (temp.split(":")[0].toUpperCase().contains("RATE")) {
					primaryDict.append("_bpm");
				} else if (temp.split(":")[0].toUpperCase().contains("DENSITY")
						|| temp.split(":")[0].toUpperCase().contains("PROP")) {
					primaryDict.append("_ppg");
				}

				primaryDict.append("\n");
				colorDict.append(temp.split(":")[0]);
				colorDict.append(":");
				for (String rgb : temp.split(":")[2].split(",")) {
					tempFloat = Float.valueOf(rgb) / Float.valueOf(255);
					colorDict.append(String.format("%.1f", tempFloat));
					if (i < 2) {
						colorDict.append(",");
						i++;
					} else {
						i = 0;
					}
				}
				colorDict.append("\n");
				scaleDict.append(temp.split(":")[0]);
				scaleDict.append(":");
				scaleDict.append(temp.split(":")[1]);
				scaleDict.append("\n");
			}
			FileReader chemsReader = new FileReader(new File(chems));
			BufferedReader chemsBuffer = new BufferedReader(chemsReader);

			while ((temp = chemsBuffer.readLine()) != null) {
				chemDict.append(temp.split(":")[0]);
				chemDict.append("\n");
				colorDict.append(temp.split(":")[0]);
				colorDict.append(":");
				for (String rgb : temp.split(":")[2].split(",")) {
					tempFloat = Float.valueOf(rgb) / Float.valueOf(255);
					colorDict.append(String.format("%.1f", tempFloat));
					if (i < 2) {
						colorDict.append(",");
						i++;
					} else {
						i = 0;
					}
				}
				colorDict.append("\n");

				scaleDict.append(temp.split(":")[0]);
				scaleDict.append(":");
				scaleDict.append(temp.split(":")[1]);
				scaleDict.append("\n");
			}
			chemsReader.close();
			chemsBuffer.close();
			// writeColorDict(colorDict);
			writeDict(primaryDict, "C:\\Scrape\\ScrapePython\\Plot\\PPchannels.txt");
			// writeDict(chemDict, "C:\\Scrape\\ScrapePython\\Plot\\CPchannels.txt");
			// writeDict(scaleDict, "C:\\Scrape\\ScrapePython\\Plot\\ScaleDict.txt");

		}

		public static void writeColorDict(StringBuilder colorDict) throws IOException {
			FileWriter colorDictWriter = new FileWriter(new File("C:\\Scrape\\ScrapePython\\Plot\\ColorDict.txt"));
			BufferedWriter colorDictBuffer = new BufferedWriter(colorDictWriter);
			Scanner colorDictScan = new Scanner(colorDict.toString());
			colorDictScan.useDelimiter("\n");
			colorDictBuffer.write("");
			while (colorDictScan.hasNext()) {
				colorDictBuffer.append(colorDictScan.next());
				colorDictBuffer.append("\n");
			}

			colorDictBuffer.flush();
			colorDictBuffer.close();
		}

		public static void writeDict(StringBuilder primaryDict, String fileName) throws IOException {
			FileWriter primaryWriter = new FileWriter(new File(fileName));
			Scanner primaryScan = new Scanner(primaryDict.toString());
			primaryScan.useDelimiter("\n");
			primaryWriter.write("");
			while (primaryScan.hasNext()) {
				primaryWriter.append(primaryScan.next());
				primaryWriter.append("\n");
			}
			primaryWriter.flush();
			primaryWriter.close();

		}

		public static void saveSmoothChemChecks(LinkedList<CheckBox> boxList, JTable table, Channels channels,
				String path) throws IOException {
			ArrayList<String> chemChecks = getInputChecks(boxList, PlotsPane.getInputArray(table));
			if (!chemChecks.isEmpty()) {
				ArrayList<String> checkChemNames = new ArrayList<>();

				for (String s : chemChecks) {
					String trimmedString = s.replaceAll(" ", "").replaceAll("-", "").toUpperCase();
					for (String chan : channels.getChannels().getOriginalName()) {
						Matcher matcher = Pattern.compile("[\\d]").matcher(chan);
						if (matcher.find()
								&& chan.replaceAll(" ", "").replaceAll("-", "").toUpperCase().contains(trimmedString)) {
							checkChemNames.add(chan);
						}
					}
				}
				writeArrayToTxt(checkChemNames, path);
			} else {
				FileWriter fileWriter = new FileWriter(new File(path));
				fileWriter.write("");
				fileWriter.close();
			}
		}

		public static void saveInternalChemChecks(LinkedList<CheckBox> boxList, String path) throws IOException {
			FileWriter fileWriter = new FileWriter(new File(path));
			for (CheckBox box : boxList) {
				fileWriter.append(String.valueOf(box.isChecked()));
				fileWriter.append("\n");
			}
			fileWriter.flush();
			fileWriter.close();

		}

		private static ArrayList<String> getInputChecks(LinkedList<CheckBox> boxList, ArrayList<String> inputs) {
			ArrayList<String> chemChecks = new ArrayList<>();
			int i = 0;
			for (CheckBox box : boxList) {
				if (box.isChecked()) {
					chemChecks.add(inputs.get(i));
				}
				i++;
			}
			return chemChecks;
		}

		private static void writeArrayToTxt(ArrayList<String> array, String path) throws IOException {
			FileWriter fileWriter = new FileWriter(new File(path));
			fileWriter.write("");
			for (String s : array) {
				fileWriter.append(s);
				fileWriter.append("\n");
			}
			fileWriter.flush();
			fileWriter.close();
		}

	}

	public static class LoadInformation {
		public static void loadPrimaryInfo(JTable primaryTable, LinkedList<JLabel> primaryColors) throws IOException {
			int i = 0;
			Integer[] colorVals;
			float[] hSBColor;
			String tempNext;
			FileReader fileReader = new FileReader(new File("C:\\Scrape\\Primary_Plots_Info.txt"));
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			Scanner primInfo = new Scanner(bufferedReader);
			primInfo.useDelimiter("\n");
			while (primInfo.hasNext()) {
				tempNext = primInfo.next();
				primaryTable.setValueAt(tempNext.split(":")[0], i, 0);
				primaryTable.setValueAt(tempNext.split(":")[1], i, 1);
				colorVals = new Integer[] { Integer.valueOf(tempNext.split(":")[2].split(",")[0]),
						Integer.valueOf(tempNext.split(":")[2].split(",")[1]),
						Integer.valueOf(tempNext.split(":")[2].split(",")[2]) };
				hSBColor = Color.RGBtoHSB(colorVals[0], colorVals[1], colorVals[2], null);
				primaryColors.get(i).setBackground(Color.getHSBColor(hSBColor[0], hSBColor[1], hSBColor[2]));
				i++;
			}
			fileReader.close();
			bufferedReader.close();
		}

		public static ArrayList<String> loadChemInfo(JTable chemTable, LinkedList<JLabel> chemColors)
				throws IOException {
			int i = 0;
			Integer[] colorVals;
			float[] hSBColor;
			ArrayList<String> chemChannels = new ArrayList<>();
			String tempNext;
			FileReader fileReader = new FileReader(new File("C:\\Scrape\\Chemical_Plots_Info.txt"));
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			Scanner primInfo = new Scanner(bufferedReader);
			primInfo.useDelimiter("\n");
			while (primInfo.hasNext()) {
				tempNext = primInfo.next();
				chemChannels.add(tempNext.split(":")[0]);
				chemTable.setValueAt(tempNext.split(":")[0], i, 0);
				chemTable.setValueAt(tempNext.split(":")[1], i, 1);
				colorVals = new Integer[] { Integer.valueOf(tempNext.split(":")[2].split(",")[0]),
						Integer.valueOf(tempNext.split(":")[2].split(",")[1]),
						Integer.valueOf(tempNext.split(":")[2].split(",")[2]) };
				hSBColor = Color.RGBtoHSB(colorVals[0], colorVals[1], colorVals[2], null);
				chemColors.get(i).setBackground(Color.getHSBColor(hSBColor[0], hSBColor[1], hSBColor[2]));
				i++;
			}

			fileReader.close();
			bufferedReader.close();
			return chemChannels;

		}

		public static void loadCasingFactor(JTextField casingFactor) throws IOException {
			FileReader fileReader = new FileReader("C:\\Scrape\\Casing_Factor.txt");
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			casingFactor.setText(bufferedReader.readLine());
			fileReader.close();
			bufferedReader.close();

		}

		public static void loadChecks(LinkedList<CheckBox> boxList, String path) throws IOException {
			if (!(new File(path).exists())) {
				return;
			}
			FileReader fileReader = new FileReader(path);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String temp;
			int i = 0;
			while ((temp = bufferedReader.readLine()) != null) {
				if (Boolean.valueOf(temp)) {
					boxList.get(i).setCheck();
				}
			}
		}
	}

	public class CheckBox extends JLabel {
		private Image check = new ImageIcon("C:\\Scrape\\check.png").getImage().getScaledInstance(15, 15,
				Image.SCALE_SMOOTH);
		private ImageIcon checkIcon = new ImageIcon(check);

		CheckBox(Boolean checked, int dim) {
			// this.setSize(dim,dim);
			if (checked) {
				this.setIcon(checkIcon);
			} else {
				this.setIcon(null);
			}
			// this.setOpaque(true);
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

		void setCheck() {
			this.setIcon(checkIcon);
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

	private class DividerLines extends JPanel {
		LinkedList<Lines> lines;
		Dimension dim;

		DividerLines(Dimension dim) {
			this.dim = dim;
			this.lines = new LinkedList<>();
			this.setSize(1000, 1000);
			this.setBackground(null);
			this.setOpaque(true);
		}

		public void addLine(int x1, int y1, int x2, int y2, Color color) {
			this.lines.add(new Lines(x1, y1, x2, y2, color));
		}

		@Override
		public void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			for (Lines line : lines) {
				g2d.setColor(line.color);
				g2d.setStroke(new BasicStroke(2));
				g2d.drawLine(line.x1, line.y1, line.x2, line.y2);
			}
		}
	}

	private static class Lines {
		final int x1;
		final int y1;
		final int x2;
		final int y2;
		final Color color;

		Lines(int x1, int y1, int x2, int y2, Color color) {
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
			this.color = color;
		}

	}

}
