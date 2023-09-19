import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

public class TemplateSelectionFrame extends JFrame {
	private final Color COLOR = Color.getHSBColor(-.85f, .1f, .85f);
	private InputPanel operatorInput;

	TemplateSelectionFrame() {
		setBounds(getScreenCenterX() - 125, getScreenCenterY() - 125, 250, 250);
		constructLines();
		construct();
		doNittyGritty();
	}

	private void construct() {
		Integer wrapWidth = this.getBounds().width * 3 / 4;
		this.add(constructLabel(new Rectangle(0, 35, this.getBounds().width * 3 / 4, this.getBounds().height / 5),
				getStringFormatted(wrapWidth, "Single Stage Summary Sheet Template"), "Stage_Summary_Label"));
		this.add(constructLabel(
				new Rectangle(0, this.getBounds().height / 5 + 30, this.getBounds().width * 3 / 4,
						this.getBounds().height / 5),
				getStringFormatted(wrapWidth, "Conventional Stage-By-Row Template"), "Conventional_Template_Label"));

		this.add(constructLabel(
				new Rectangle(0, this.getBounds().height * 2 / 5 + 30, this.getBounds().width * 3 / 4,
						this.getBounds().height / 5),
				getStringFormatted(wrapWidth, "Treatment Summary Template"), "Treatment_Summary_Label"));

		this.add(constructCheckBox("Stage_Summary_Box"));
		this.add(constructCheckBox("Conventional_Template_Box"));
		this.add(constructCheckBox("Treatment_Summary_Box"));
		constructOperatorInput();
		constructButton();
	}

	private void constructLines() {
		Lines lines = new Lines();
		lines.setName("Lines");
		lines.addLine(0, 32, this.getBounds().width, 32, Color.black);
		lines.addLine(0, this.getBounds().height * 2 / 5 + 25 + this.getBounds().height / 5, this.getBounds().width,
				this.getBounds().height * 2 / 5 + 25 + this.getBounds().height / 5, Color.black);
		this.getContentPane().add(lines);
	}

	private void constructButton() {
		JButton button = new JButton();
		button.setName("Button");
		button.setText("SAVE");
		button.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TemplateSelectionFrame.this.dispose();
			}
		});
		button.setVisible(true);
		this.add(button);
	}

	public static HashMap<String, HashMap<String, ArrayList<Integer>>> readSavedTemplateMap(String operator,
			String fileName) throws IOException {
		String path = "C://Scrape/Operator_Templates/" + operator + "/" + fileName;
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		BufferedReader bufferedReader = new BufferedReader(
				new FileReader("C://Scrape/Operator_Templates/" + operator + "/" + fileName));

		HashMap<String, HashMap<String, ArrayList<Integer>>> templateMap = new HashMap<>();
		String temp;
		while ((temp = bufferedReader.readLine()) != null && temp != "") {
			if (!temp.contains(":") || temp.split(":").length < 2) {
				continue;
			}
			String key = temp.split(":")[0];
			String rowsColumns = temp.split(":")[1];
			if (key.equals("Workbook Suffix") || key.equals("Sheet Name")) {
				// templateMap.put(key, new HashMap<String,ArrayList<Integer>>());
				// templateMap.get(key).put(rowsColumns, null);
				continue;
			}
			templateMap.put(key, new HashMap<String, ArrayList<Integer>>());
			addRowsColumnsToMap(templateMap, key, rowsColumns);
		}
		bufferedReader.close();
		return templateMap;
	}

	public static HashMap<String, HashMap<String, ArrayList<Integer>>> readSavedTemplateMap(String path)
			throws IOException {
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		BufferedReader bufferedReader = new BufferedReader(new FileReader(path));

		HashMap<String, HashMap<String, ArrayList<Integer>>> templateMap = new HashMap<>();
		String temp;
		while ((temp = bufferedReader.readLine()) != null && temp != "") {
			if (!temp.contains(":")) {
				continue;
			}
			String key = temp.split(":")[0];
			String rowsColumns = temp.split(":")[1];
			if (key.equals("Workbook Suffix") || key.equals("Sheet Name")) {
				// templateMap.put(key, new HashMap<String,ArrayList<Integer>>());
				// templateMap.get(key).put(rowsColumns, null);
				continue;
			}
			templateMap.put(key, new HashMap<String, ArrayList<Integer>>());
			addRowsColumnsToMap(templateMap, key, rowsColumns);
		}
		bufferedReader.close();
		return templateMap;
	}

	private static void addRowsColumnsToMap(HashMap<String, HashMap<String, ArrayList<Integer>>> templateMap,
			String key, String rowsColumns) {
		templateMap.get(key).put("rows", new ArrayList<Integer>());
		templateMap.get(key).put("columns", new ArrayList<Integer>());
		for (String s : rowsColumns.split(";")) {
			if (!checkBadCellAddress(s)) {
				break;
			}
			templateMap.get(key).get("rows").add(Integer.valueOf(s.split(",")[0]));
			templateMap.get(key).get("columns").add(Integer.valueOf(s.split(",")[1]));
		}
	}

	private static Boolean checkBadCellAddress(String rowsColumns) {
		Matcher matcher = Pattern.compile("[,]").matcher(rowsColumns);
		return matcher.find();
	}

	private String getStringFormatted(Integer wrapWidth, String text) {
		return String.format("<html><div style=\"width\":%dpx>%s</div></html>", wrapWidth, text);
	}

	private JLabel constructLabel(Rectangle rectangle, String text, String name) {
		JLabel label = new JLabel();
		label.setBackground(COLOR);
		label.setText(text);
		label.setName(name);
		label.setBounds(rectangle);
		label.setVerticalTextPosition(SwingConstants.CENTER);
		label.setHorizontalTextPosition(SwingConstants.CENTER);
		label.setOpaque(true);
		return label;
	}

	private void constructOperatorInput() {
		this.operatorInput = new InputPanel(new Rectangle(0, 0, this.getBounds().width, 40), "Operator",
				"Operator_Input");
		operatorInput.setVisible(true);
		this.add(operatorInput);
	}

	private String getOperator() {
		return this.operatorInput.getInput();
	}

	private void addActionToBox(String name, CheckBox box) {
		switch (name) {
		case ("Stage_Summary_Box"):
			box.addMouseListener(new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent e) {

				}

				@Override
				public void mousePressed(MouseEvent e) {
				}

				@Override
				public void mouseReleased(MouseEvent e) {
				}

				@Override
				public void mouseEntered(MouseEvent e) {
				}

				@Override
				public void mouseExited(MouseEvent e) {
				}
			});
			break;
		case ("Conventional_Template_Box"):
			box.addMouseListener(new MouseListener() {

				@Override
				public void mouseClicked(MouseEvent e) {
					if (getOperator().equals("")) {
						JOptionPane.showMessageDialog(null, "Input the associated operator's name for the template");
						return;
					}
					new OperatorTemplate(getOperator());
				}

				@Override
				public void mousePressed(MouseEvent e) {
				}

				@Override
				public void mouseReleased(MouseEvent e) {
				}

				@Override
				public void mouseEntered(MouseEvent e) {
				}

				@Override
				public void mouseExited(MouseEvent e) {
				}
			});
			break;

		case ("Treatment_Summary_Box"):
			box.addMouseListener(new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent e) {

				}

				@Override
				public void mousePressed(MouseEvent e) {
				}

				@Override
				public void mouseReleased(MouseEvent e) {
				}

				@Override
				public void mouseEntered(MouseEvent e) {
				}

				@Override
				public void mouseExited(MouseEvent e) {
				}
			});
			break;
		}
	}

	private Rectangle getRectBounds() {
		int x = Toolkit.getDefaultToolkit().getScreenSize().width / 4;
		int y = 0;
		int width = Toolkit.getDefaultToolkit().getScreenSize().width / 2;
		int height = Toolkit.getDefaultToolkit().getScreenSize().height - 50;
		return new Rectangle(x, y, width, height);
	}

	private CheckBox constructCheckBox(String name) {
		CheckBox box = new CheckBox(name);
		addActionToBox(name, box);
		box.setName(name);
		box.setVisible(true);
		return box;
	}

	private void doNittyGritty() {
		System.out.println(getScreenCenterX() - 125);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setIconImage(
				new ImageIcon("C:\\Scrape\\Scrape.png").getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
		this.setTitle("Template Selection");
		this.getContentPane().setBackground(COLOR);
		this.setLayout(new TemplateSelectionLayout());
		this.setVisible(true);
	}

	private int getScreenCenterX() {
		GraphicsDevice gD = getGraphicsConfiguration().getDevice();
		return (int) gD.getDefaultConfiguration().getBounds().getCenterX();
	}

	private int getScreenCenterY() {

		GraphicsDevice gD = getGraphicsConfiguration().getDevice();

		return (int) gD.getDefaultConfiguration().getBounds().getCenterY();
	}

	private class TemplateSelectionLayout extends SpringLayout {
		int yLastComponent;

		TemplateSelectionLayout() {
			setSelectionLayout();
		}

		void setSelectionLayout() {
			int index = 0;
			for (Component c : getContentPane().getComponents()) {
				Matcher matcher = Pattern.compile("(Label|Box)").matcher(c.getName());
				String name = matcher.find() ? matcher.group() : "NULL";
				switch (name) {
				case ("Label"):
					labelLayout((JLabel) c);
					break;
				case ("Box"):
					boxLayout((CheckBox) c, index);
					index++;
					break;
				default:
					if (c.getName().equals("Button")) {
						buttonLayout((JButton) c);
					} else if (c.getName().equals("Lines")) {
						linesLayout((Lines) c);
					} else {
						inputPanelLayout((InputPanel) c);
					}
					break;
				}
			}
		}

		void linesLayout(Lines lines) {
			this.putConstraint(NORTH, lines, 0, NORTH, getContentPane());
			this.putConstraint(SOUTH, lines, getContentPaneBounds("h"), NORTH, getContentPane());
			this.putConstraint(WEST, lines, 0, WEST, getContentPane());
			this.putConstraint(EAST, lines, getContentPaneBounds("w"), WEST, getContentPane());
		}

		void buttonLayout(JButton button) {
			this.putConstraint(NORTH, button, getLastCompY(), NORTH, getContentPane());
			this.putConstraint(SOUTH, button, getLastCompY() + 20, NORTH, getContentPane());
			this.putConstraint(WEST, button, getContentPaneBounds("w") / 4, WEST, getContentPane());
			this.putConstraint(EAST, button, getContentPaneBounds("w") * 3 / 4, WEST, getContentPane());
		}

		void inputPanelLayout(InputPanel inputPanel) {
			this.putConstraint(NORTH, inputPanel, inputPanel.getBounds().y, NORTH, getContentPane());
			this.putConstraint(SOUTH, inputPanel, inputPanel.getBounds().height, NORTH, getContentPane());
			this.putConstraint(WEST, inputPanel, inputPanel.getBounds().x, WEST, getContentPane());
			this.putConstraint(EAST, inputPanel, inputPanel.getBounds().width, WEST, getContentPane());

		}

		void labelLayout(JLabel label) {
			this.putConstraint(NORTH, label, label.getBounds().y, NORTH, getContentPane());
			this.putConstraint(SOUTH, label, label.getBounds().y + label.getBounds().height, NORTH, getContentPane());
			this.putConstraint(WEST, label, 10, WEST, getContentPane());
			this.putConstraint(EAST, label, label.getBounds().width, WEST, getContentPane());
			setLastCompY(label.getBounds().y + label.getBounds().height);
		}

		void boxLayout(CheckBox box, int index) {
			this.putConstraint(NORTH, box, getBoxNorth(index, box), NORTH, getContentPane());
			this.putConstraint(SOUTH, box, getBoxSouth(getBoxNorth(index, box), box), NORTH, getContentPane());
			this.putConstraint(WEST, box, getBoxWest(box), WEST, getContentPane());
			this.putConstraint(EAST, box, getBoxEast(getBoxWest(box), box), WEST, getContentPane());
		}

		void setLastCompY(int yLastComponent) {
			this.yLastComponent = yLastComponent;
		}

		int getLastCompY() {
			return this.yLastComponent;
		}

		int getBoxNorth(int index, CheckBox box) {
			int north = (getContentPaneBounds("h") / 5 * index) + (getContentPaneBounds("h") / 10)
					- (box.getBounds().height / 2);

			return north + 45;
		}

		int getBoxSouth(int north, CheckBox box) {
			return north + box.getBounds().height;
		}

		int getBoxWest(CheckBox box) {
			int width = getContentPaneBounds("w");
			int widthBoxStart = width * 3 / 4;
			int boxWorkingWidth = width / 4;
			return widthBoxStart + boxWorkingWidth / 2 - box.getBounds().width / 2;
		}

		int getBoxEast(int west, CheckBox box) {
			return west + box.getBounds().width;
		}

		int getContentPaneBounds(String dim) {
			Rectangle rect = TemplateSelectionFrame.this.getBounds();
			switch (dim) {
			case ("h"):
				return rect.height;
			case ("w"):
				return rect.width;
			case ("x"):
				return rect.x;
			case ("y"):
				return rect.y;
			default:
				return -1;
			}
		}
	}

	private class Lines extends JLabel {
		ArrayList<Line> lineList;

		Lines() {
			lineList = new ArrayList<>();
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2D = (Graphics2D) g;
			for (Line line : lineList) {
				g2D.setColor(line.color);
				g2D.setStroke(new BasicStroke(2));
				g2D.drawLine(line.x1, line.y1, line.x2, line.y2);
			}
		}

		public void addLine(int x1, int y1, int x2, int y2, Color color) {
			lineList.add(new Line(x1, y1, x2, y2, color));
		}

		private class Line {
			int x1;
			int y1;
			int x2;
			int y2;
			Color color;

			Line(int x1, int y1, int x2, int y2, Color color) {
				this.x1 = x1;
				this.y1 = y1;
				this.x2 = x2;
				this.y2 = y2;
				this.color = color;
			}
		}
	}
}
