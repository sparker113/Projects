import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

public class StageInputsFrame extends JFrame {
	private int w;
	private int h;
	private Color color;
	private HashMap<String, ArrayList<Double>> values;
	private Semaphore semaphore;
	private Long totalAcid;

	StageInputsFrame(int w, int h) {
		this.w = w;
		this.h = h;
		this.values = getDummyMap();
		this.setBounds(getXByDisplay(), getYByDisplay(), w, h);
		this.color = Color.getHSBColor(-.85f, .1f, .85f);
		this.semaphore = new Semaphore(0);

		drawSectionLines();
		constructInputComponents();

		this.setLayout(new InputPanelLayout((JPanel) this.getContentPane()));
		this.setBackground(color);
		this.setTitle("Stage Inputs");
		this.setIconImage(
				new ImageIcon("C:\\Scrape\\Scrape.png").getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
		this.setDefaultCloseOperation(1);
		this.setVisible(true);

	}

	StageInputsFrame(int w, int h, HashMap<String, ArrayList<Double>> values) {
		this.w = w;
		this.h = h;
		this.values = values;
		this.setBounds(getXByDisplay(), getYByDisplay(), w, h);
		this.color = Color.getHSBColor(-.85f, .1f, .85f);
		this.semaphore = new Semaphore(0);

		drawSectionLines();
		constructInputComponents();

		this.setLayout(new InputPanelLayout((JPanel) this.getContentPane()));
		this.setBackground(color);
		this.setTitle("Stage Inputs");
		this.setIconImage(
				new ImageIcon("C:\\Scrape\\Scrape.png").getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
		this.setDefaultCloseOperation(1);
		this.setVisible(true);

	}

	private HashMap<String, ArrayList<Double>> getDummyMap() {
		HashMap<String, ArrayList<Double>> dummyMap = new HashMap<>();
		dummyMap.put("Stage_Up", new ArrayList<Double>());
		dummyMap.put("Acid_Spearhead", new ArrayList<Double>());
		dummyMap.put("Diverter", new ArrayList<Double>());
		dummyMap.put("Mid_Stage_Acid", new ArrayList<Double>());
		dummyMap.put("Acid_Input", new ArrayList<Double>());
		return dummyMap;
	}

	private void constructAcidInput() {
		JTextField acidInputs = new JTextField();
		acidInputs.setName("Acid_Inputs");
		acidInputs.setOpaque(true);
		acidInputs.setVisible(true);
		acidInputs.setEditable(true);
		this.getContentPane().add(acidInputs);
	}

	private int getXByDisplay() {
		int x;
		getToolkit();
		int width = Toolkit.getDefaultToolkit().getScreenSize().width;
		x = width / 2 - w / 2;
		return x;
	}

	private int getYByDisplay() {
		int y;
		getToolkit();
		int height = Toolkit.getDefaultToolkit().getScreenSize().height;
		y = height / 2 - h / 2;
		return y;
	}

	private void setMap(HashMap<String, ArrayList<Double>> values) {
		this.values = values;
		semaphore.release();
	}

	public HashMap<String, ArrayList<Double>> getMap() {
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			System.out.println("Interrupted waiting for semaphore - StageInputsFrame");
		}
		return this.values;
	}

	public ArrayList<Double> getAcidInputGal() {
		InputPanel acidInput = getComponentByName("Acid_Input");
		return acidInput.getInput();
	}

	public ArrayList<Double> getAcidInput() {
		InputPanel acidInput = getComponentByName("Acid_Input");
		ArrayList<Double> acidInputBBL = new ArrayList<>();

		if (acidInput.getInput().isEmpty()) {
			acidInputBBL.add(0.0);
			setTotalAcid(Long.valueOf(0));
		} else {
			Long totalAcid = Long.valueOf(0);
			for (Double d : acidInput.getInput()) {
				acidInputBBL.add(d);
				totalAcid += Math.round(d);
			}
			setTotalAcid(totalAcid);
		}
		return acidInputBBL;
	}

	public Long getTotalAcid() {
		return this.totalAcid;
	}

	private void setTotalAcid(Long totalAcid) {
		this.totalAcid = totalAcid;
	}

	private void constructInputComponents() {
		this.add(new InputPanel(new Rectangle(0, 0, w, h / 6), "Add Stage", "Stage_Up", values.get("Stage_Up")));
		this.add(new InputPanel(new Rectangle(0, h / 6, w, h / 6), "Add Acid Spearhead", "Acid_Spearhead",
				values.get("Acid_Spearhead")));
		this.add(new InputPanel(new Rectangle(0, 2 * h / 6, w, h / 6), "Add Mid-Stage Acid", "Mid_Stage_Acid",
				values.get("Mid_Stage_Acid")));
		this.add(new InputPanel(new Rectangle(0, 3 * h / 6, w, h / 6), "Add Diverter", "Diverter",
				values.get("Diverter")));
		this.add(new InputPanel(new Rectangle(0, h * 2 / 3 - 15, w * 3 / 4 - 5, 45), "Acid Amount", "Acid_Input",
				values.get("Acid_Input")));
		this.add(constructSaveButton());

	}

	private void drawSectionLines() {
		SectionLines sectionLines = new SectionLines();
		sectionLines.addLine(0, h / 6 + 5, w, h / 6 + 5);
		sectionLines.addLine(0, 2 * h / 6 + 5, w, 2 * h / 6 + 5);
		sectionLines.addLine(0, 3 * h / 6 + 5, w, 3 * h / 6 + 5);
		sectionLines.addLine(0, 4 * h / 6 + 5, w, 4 * h / 6 + 5);
		sectionLines.setVisible(true);

		this.getContentPane().add(sectionLines);
	}

	private Boolean checkComponentName(String[] keys, Component comp) {
		for (String s : keys) {
			if (comp.getName() != null && comp.getName().equals(s)) {
				return true;
			}
		}
		return false;
	}

	private JButton constructSaveButton() {
		JButton button = new JButton();
		button.setText("Save");
		button.setName("Save");
		button.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setMap(getValueMap());
				StageInputsFrame.this.dispose();
			}
		});
		button.setVisible(true);
		return button;
	}

	private HashMap<String, ArrayList<Double>> getValueMap() {
		HashMap<String, ArrayList<Double>> valueMap = new HashMap<>();
		String[] keys = { "Stage_Up", "Acid_Spearhead", "Mid_Stage_Acid", "Diverter" };
		for (String s : keys) {
			valueMap.put(s, getComponentByName(s).getInput());
		}
		return valueMap;
	}

	private InputPanel getComponentByName(String name) {
		for (Component comp : this.getContentPane().getComponents()) {

			if (comp.getName() != null && comp.getName().equals(name)) {
				return ((InputPanel) comp);
			}
		}
		return null;
	}

	@Override
	public void setDefaultCloseOperation(int i) {
		// setMap(getValueMap());
		this.dispose();
	}

	private class InputPanel extends JPanel {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private Rectangle rectangle;
		private String labelString;
		private String name;
		private ArrayList<Double> previousInputs;

		InputPanel(Rectangle rectangle, String labelString, String name, ArrayList<Double> previousInputs) {
			this.rectangle = rectangle;
			this.labelString = labelString;
			this.name = name;
			this.previousInputs = previousInputs;
			make();
		}

		private void make() {
			constructPanel();
			this.add(constructTextField());
			this.add(constructLabel());
			this.setVisible(true);
		}

		private void constructPanel() {
			this.setBounds(rectangle);
			this.setBackground(color);
			this.setName(name);
			this.setLayout(null);
			this.setVisible(true);
		}

		private JTextField constructTextField() {
			JTextField inputField = new JTextField();
			inputField.setBounds((int) (rectangle.getCenterX()), (int) (rectangle.getHeight() * .3),
					(int) (rectangle.getCenterX() - 20), (int) (rectangle.getHeight() - rectangle.getHeight() * .3));
			inputField.setName("Text Field");
			inputField.setText(getInputString());
			inputField.setVisible(true);
			return inputField;
		}

		private String getInputString() {
			String inputString = "";
			if (previousInputs == null || previousInputs.isEmpty() || previousInputs.get(0).equals(0.0)) {
				return "";
			}
			for (Double d : previousInputs) {
				inputString += String.valueOf(d) + ",";
			}
			return inputString.substring(0, inputString.length() - 1);

		}

		private JLabel constructLabel() {
			JLabel label = new JLabel();
			label.setBounds(10, (int) (rectangle.getHeight() * .25), (int) (rectangle.getCenterX() - 15),
					(int) (rectangle.getHeight() - rectangle.getHeight() * .3));
			label.setText(labelString);
			label.setBackground(null);
			label.setName("Label");
			label.setVerticalTextPosition(SwingConstants.CENTER);
			label.setVerticalAlignment(SwingConstants.CENTER);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			label.setHorizontalTextPosition(SwingConstants.CENTER);
			label.setOpaque(true);
			return label;
		}

		private ArrayList<Double> parseInput(String input) {
			ArrayList<Double> inputArray = new ArrayList<>();
			if (input.contains(",")) {
				for (String s : input.split(",")) {
					inputArray.add(Double.valueOf(s));
				}
			} else {
				inputArray.add(Double.valueOf(input));
			}
			return inputArray;
		}

		private ArrayList<Double> getInput() {
			JTextField inputField = ((JTextField) getComponentByName("Text Field"));
			if (inputField.getText() != null && !inputField.getText().equals("")) {
				return parseInput(inputField.getText());
			} else {
				ArrayList<Double> array = new ArrayList<>();
				array.add(0.0);
				return array;
			}
		}

		private Component getComponentByName(String name) {
			for (Component comp : this.getComponents()) {
				if (comp.getName().equals(name)) {
					return comp;
				}
			}
			return null;
		}

	}

	private class InputPanelLayout extends SpringLayout {
		JPanel contentPane;

		InputPanelLayout(JPanel contentPane) {
			this.contentPane = contentPane;
			configLayout();
		}

		private void configLayout() {
			String[] keys = { "Stage_Up", "Acid_Spearhead", "Mid_Stage_Acid", "Diverter" };
			for (Component comp : contentPane.getComponents()) {
				if (checkComponentName(keys, comp)) {
					singlePanelLayout(comp);
				} else if (comp.getName() != null && comp.getName().equals("Acid_Input")) {
					positionAcidInput(comp);
				} else if (comp.getName() != null) {
					positionButton((JButton) comp, 15);
				} else {
					positionLines((JPanel) comp);
				}
			}
		}

		private void positionAcidInput(Component comp) {
			int eastInput = w * 5 / 8 - 5;
			this.putConstraint(NORTH, comp, h * 2 / 3 + 3, NORTH, contentPane);
			this.putConstraint(SOUTH, comp, h * 2 / 3 + 40, NORTH, contentPane);
			this.putConstraint(WEST, comp, 0, WEST, contentPane);
			this.putConstraint(EAST, comp, eastInput, WEST, contentPane);
		}

		private void positionLines(JPanel panel) {
			this.putConstraint(NORTH, panel, 0, NORTH, contentPane);
			this.putConstraint(SOUTH, panel, h, NORTH, contentPane);
			this.putConstraint(WEST, panel, 0, WEST, contentPane);
			this.putConstraint(EAST, panel, w, WEST, contentPane);
		}

		private void singlePanelLayout(Component comp) {
			this.putConstraint(NORTH, comp, comp.getY(), NORTH, contentPane);
			this.putConstraint(SOUTH, comp, comp.getY() + comp.getHeight(), NORTH, contentPane);
			this.putConstraint(WEST, comp, comp.getX(), WEST, contentPane);
			this.putConstraint(EAST, comp, comp.getX() + comp.getWidth(), WEST, contentPane);
		}

		private void positionButton(JButton button, int vertSpacing) {
			int buttonWidth = w / 4;
			int buttonX = buttonCenterX(buttonWidth) + buttonWidth;
			int buttonY = buttonY(h, vertSpacing);
			int buttonBottom = h - vertSpacing - 10;

			this.putConstraint(NORTH, button, buttonY, NORTH, contentPane);
			this.putConstraint(SOUTH, button, buttonBottom - 20, NORTH, contentPane);
			this.putConstraint(WEST, button, buttonX, WEST, contentPane);
			this.putConstraint(EAST, button, buttonX + buttonWidth, WEST, contentPane);
		}

		private int buttonY(int totalHeight, int spacing) {
			int spacingFrom = totalHeight * 2 / 3;
			return spacingFrom + spacing;
		}

		private int buttonCenterX(int forWidth) {
			int center = w / 2;
			return center - (forWidth / 2);
		}
	}

	private class SectionLines extends JPanel {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		LinkedList<Lines> lines;

		SectionLines() {
			this.lines = new LinkedList<>();
			this.setBackground(Color.BLACK);
			this.setSize(w, h);
			this.setOpaque(true);
			this.setVisible(true);
		}

		void addLine(int x1, int y1, int x2, int y2) {
			this.lines.add(new Lines(x1, y1, x2, y2));
		}

		@Override
		public void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			for (Lines line : lines) {
				g2d.setColor(Color.BLACK);
				g2d.setStroke(new BasicStroke(2));
				g2d.drawLine(line.x1, line.y1, line.x2, line.y2);
			}
		}

	}

	private static class Lines {
		int x1;
		int y1;
		int x2;
		int y2;

		Lines(int x1, int y1, int x2, int y2) {
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}

	}
}
