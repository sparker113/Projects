package materials;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

public class SandSilo extends JPanel {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String silo;
	private Double percent;
	private int height;
	private Integer order;
	private int width;
	private String sandType;
	private Double fillWeight;
	private final Double RATIO = 0.5;

	SandSilo(String silo, Double fillWeight, Double percent, Integer order, String sandType, int height) {
		this.silo = silo;
		this.fillWeight = fillWeight;
		this.percent = percent;
		this.order = order;
		this.sandType = sandType;
		this.height = height;
		this.width = (int) Math.round(Double.valueOf(height) * RATIO);
		nittyGritty();
	}

	SandSilo(String silo, Double fillWeight, Double percent, Integer order, int height) {
		this.silo = silo;
		this.fillWeight = fillWeight;
		this.percent = percent;
		this.height = height;
		this.width = (int) Math.round(Double.valueOf(height) * RATIO);
	}

	public void construct() {
		nittyGritty();
	}

	public void setSandType(String sandType) {
		this.sandType = sandType;
	}

	public String getSandType() {
		return this.sandType;
	}

	private void nittyGritty() {
		setName(silo);
		setBounds(0, 0, width, height);
		setBackground(Color.getHSBColor(-.85f, .1f, .85f));
		constructTextField();
		constructLabel();
		constructSilo();
		constructOrderField();
		setLayout(new SiloLayout());
		setVisible(true);
	}

	public static <T> String getCommaFormattedNumber(T t, Boolean rounded) {
		String value = String.valueOf(t);
		if (checkNonNumeric(value)) {
			return "0";
		}
		Matcher matcher = Pattern.compile("\\d+").matcher(value);
		if (rounded && matcher.find()) {
			return addCommas(matcher.group());
		} else if (matcher.find()) {
			return matcher.groupCount() > 1 ? addCommas(matcher.group()) + "." + matcher.group(1)
					: addCommas(matcher.group());
		}
		return null;
	}

	private static String addCommas(String value) {
		StringBuilder stringBuilder = new StringBuilder();
		int count = 0;
		for (int i = value.length() - 1; i > -1; i--) {
			if (count % 3 == 0 & count > 0) {
				stringBuilder.insert(0, ",");
				stringBuilder.insert(0, value.charAt(i));
				count++;
				continue;
			}
			stringBuilder.insert(0, value.charAt(i));
			count++;
		}
		return stringBuilder.toString();
	}

	private static Boolean checkNonNumeric(String value) {
		Matcher matcher = Pattern.compile("[^\\d\\.]").matcher(value);
		if (matcher.find()) {
			return true;
		}
		return false;
	}

	public Component getComponentByName(String name) {
		for (Component c : getComponents()) {
			if (c.getName() == null) {
				continue;
			}
			String cName = c.getName();
			if (cName.equals(name)) {
				return c;
			}
		}
		return null;
	}

	private void constructSilo() {
		Silo silo = new Silo(getColor(sandType), percent);
		silo.setName("silo");
		silo.setVisible(true);
		add(silo);
	}

	private void constructLabel() {
		JLabel label = new JLabel();
		label.setHorizontalTextPosition(SwingConstants.CENTER);
		label.setBackground(Color.getHSBColor(-.85f, .1f, .85f));
		label.setName("siloLabel");
		label.setText(silo + "(%) - ");
		label.setOpaque(true);
		label.setVisible(true);
		add(label);
	}

	private void constructTextField() {
		JTextField textField = new JTextField();
		textField.setText(roundPercent());
		textField.setName("siloPercent");
		textField.setEditable(true);
		textField.setVisible(true);
		add(textField);
	}

	private void constructOrderField() {
		JTextField textField = new JTextField();
		textField.setName("order");
		textField.setText(order.toString());
		textField.setEditable(true);
		textField.setVisible(true);
		textField.addKeyListener(new NumberKeyListener());
		add(textField);
	}

	private int getSiloSouth() {
		return (int) Math.round(Double.valueOf(height) * 0.8);
	}

	private String roundPercent() {
		if (percent == null || percent == 0.0) {
			return "0.0";
		}
		Double temp = Double.valueOf(Math.round(percent * 100) / 100.0);
		return String.valueOf(temp);
	}

	private Color getColor(String sandType) {
		switch (sandType) {
		case (R200):
			return Color.blue;
		case (R100):
			return Color.cyan;
		case (R40):
			return Color.green;
		case (R30):
			return Color.magenta;
		case (R20):
			return Color.orange;
		default:
			return Color.pink;
		}
	}

	private class NumberKeyListener implements KeyListener {

		@Override
		public void keyTyped(KeyEvent e) {
			Matcher matcher = Pattern.compile("\\D|0").matcher(String.valueOf(e.getKeyChar()));
			if (matcher.find()) {
				e.consume();
				return;
			}
			e.consume();
			GUIUtilities.updateTextField(((JTextField) e.getSource()), String.valueOf(e.getKeyChar()));
		}

		@Override
		public void keyPressed(KeyEvent e) {
			e.consume();

		}

		@Override
		public void keyReleased(KeyEvent e) {
			e.consume();

		}

	}

	private class SiloLayout extends SpringLayout {
		SiloLayout() {
			construct();
		}

		void construct() {
			int siloSouth = getSiloSouth();
			for (Component c : getComponents()) {
				if (c.getName() == null) {
					continue;
				}
				String name = c.getName();
				switch (name) {
				case ("silo"):
					constructSilo((Silo) c);
					break;
				case ("siloLabel"):
					constructLabel((JLabel) c, siloSouth);
					break;
				case ("siloPercent"):
					constructTextField((JTextField) c, siloSouth);
					break;
				case ("order"):
					constructOrderField((JTextField) c, siloSouth + 25);
					break;
				}
			}
		}

		void constructSilo(Silo silo) {
			putConstraint(NORTH, silo, 0, NORTH, SandSilo.this);
			putConstraint(SOUTH, silo, silo.getSiloHeight(), NORTH, SandSilo.this);
			putConstraint(WEST, silo, 0, WEST, SandSilo.this);
			putConstraint(EAST, silo, width, WEST, SandSilo.this);
		}

		void constructLabel(JLabel label, int siloSouth) {
			putConstraint(NORTH, label, siloSouth, NORTH, SandSilo.this);
			putConstraint(SOUTH, label, siloSouth + 25, NORTH, SandSilo.this);
			putConstraint(WEST, label, 10, WEST, SandSilo.this);
			putConstraint(EAST, label, width / 2, WEST, SandSilo.this);
		}

		void constructTextField(JTextField textField, int siloSouth) {
			putConstraint(NORTH, textField, siloSouth, NORTH, SandSilo.this);
			putConstraint(SOUTH, textField, siloSouth + 25, NORTH, SandSilo.this);
			putConstraint(WEST, textField, width / 2, WEST, SandSilo.this);
			putConstraint(EAST, textField, width - 10, WEST, SandSilo.this);
		}

		void constructOrderField(JTextField textField, int northMin) {
			putConstraint(NORTH, textField, northMin + 5, NORTH, SandSilo.this);
			putConstraint(SOUTH, textField, northMin + 25, NORTH, SandSilo.this);
			putConstraint(WEST, textField, width / 2 - 15, WEST, SandSilo.this);
			putConstraint(EAST, textField, width / 2 + 15, WEST, SandSilo.this);
		}
	}

	private class Silo extends JPanel {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		Color color;
		Double percent;
		final int CONEHEIGHT = 15;
		final int FILLPAD = 1;

		Silo(Color color, Double percent) {
			this.color = color;
			this.percent = percent;
			nittyGritty();
		}

		private void nittyGritty() {
			setVisible(true);
			setOpaque(true);
			setBackground(Color.getHSBColor(-.85f, .1f, .85f));
		}

		@Override
		public void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			drawBackground(g2d);
			drawSiloEmpty(g2d, Color.white);
			drawOutline(g2d);
			drawSiloFill(g2d, color);
			drawType(g2d);
		}

		private void drawBackground(Graphics2D g2d) {
			g2d.setColor(Color.getHSBColor(-.85f, .1f, .85f));
			g2d.fillRect(0, 0, width, height);
		}

		private void drawOutline(Graphics2D g2d) {
			ArrayList<Line> lines = getOutlineLines();
			g2d.setStroke(new BasicStroke(2));
			g2d.setColor(Color.black);
			for (Line l : lines) {
				g2d.drawLine(l.x1, l.y1, l.x2, l.y2);
			}
		}

		private void drawSiloFill(Graphics2D g2d, Color color) {
			Polygon polygon = getSiloFillPolygon();
			g2d.setColor(color);

			g2d.fillPolygon(polygon);
		}

		private void drawSiloEmpty(Graphics2D g2d, Color color) {
			g2d.setColor(color);
			g2d.fill(getSiloEmptyRect());
		}

		private void drawType(Graphics2D g2d) {
			g2d.setColor(Color.black);
			g2d.drawString(sandType, 2.0f, 15.0f);
			g2d.drawString(getCommaFormattedNumber(fillWeight, true) + " lbs.", 2.0f, 30.0f);
		}

		private Polygon getSiloFillPolygon() {
			Polygon polygon = new Polygon();
			Point coneTip = getConeTip();
			polygon.addPoint(coneTip.x, coneTip.y);
			for (Line l : getSiloFillPolygonLines()) {
				polygon.addPoint(l.x1, l.y1);
				polygon.addPoint(l.x2, l.y2);
			}
			return polygon;
		}

		private Rectangle getSiloEmptyRect() {
			Rectangle rectangle = new Rectangle(FILLPAD, FILLPAD, getSiloWidth() - FILLPAD,
					getSiloHeight() - CONEHEIGHT - FILLPAD);
			return rectangle;
		}

		private ArrayList<Line> getSiloFillPolygonLines() {
			ArrayList<Line> array = new ArrayList<>();
			int fillPixels = getFillPixels(getSiloHeight());
			array.add(new Line(getSiloWidth() - FILLPAD, getY1Fill(fillPixels) + fillPixels, getSiloWidth() - FILLPAD,
					getY1Fill(fillPixels)));
			array.add(new Line(FILLPAD, getY1Fill(fillPixels), FILLPAD, getY1Fill(fillPixels) + fillPixels));
			return array;
		}

		private int getY1Fill(int fillPixels) {
			return getSiloHeight() - CONEHEIGHT - fillPixels;
		}

		private int getFillPixels(int height) {
			if (percent > 0.0) {
				return (int) Math.round((Double.valueOf(height) - Double.valueOf(CONEHEIGHT)) * (percent / 100.0));
			}
			return 0;
		}

		private int getSiloHeight() {
			return (int) Math.round(Double.valueOf(height) * 0.8);
		}

		private int getSiloWidth() {
			return width - 20;
		}

		private Point getConeTip() {
			return new Point(getSiloWidth() / 2, getSiloHeight() - FILLPAD);
		}

		private ArrayList<Line> getOutlineLines() {
			ArrayList<Line> array = new ArrayList<>();
			array.add(new Line(0, 0, 0, getSiloHeight() - CONEHEIGHT));
			array.add(new Line(0, 0, getSiloWidth(), 0));
			array.add(new Line(getSiloWidth(), 0, getSiloWidth(), getSiloHeight() - CONEHEIGHT));
			array.add(new Line(0, getSiloHeight() - CONEHEIGHT, getSiloWidth() / 2, getSiloHeight()));
			array.add(new Line(getSiloWidth(), getSiloHeight() - CONEHEIGHT, getSiloWidth() / 2, getSiloHeight()));
			return array;
		}

		private class Line {
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

	public static final String R200 = "200 Mesh";
	public static final String R100 = "100 Mesh";
	public static final String R40 = "40/70";
	public static final String R30 = "30/50";
	public static final String R20 = "20/40";

}
