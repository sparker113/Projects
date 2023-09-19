import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

public class InfoPanel extends JPanel {
	Rectangle rectangle;
	JLabel[] labels;
	int labelCount;

	InfoPanel(Rectangle rectangle, JLabel... labels) {
		this.rectangle = rectangle;
		this.labels = labels;
		labelCount = labels.length;
		System.out.println(rectangle);
		construct();
	}

	public void construct() {
		// this.setBounds(rectangle);
		this.setLayout(new FlowLayout());
		addLabels();
		this.setOpaque(true);
		this.setVisible(true);
		this.setName("info_panel");
	}

	public void addLabels() {
		for (JLabel label : labels) {
			label.setHorizontalTextPosition(SwingConstants.CENTER);
			label.setVerticalTextPosition(SwingConstants.CENTER);
			label.setOpaque(true);
			label.setVisible(true);
			this.add(label);
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		int w = rectangle.width;
		int h = rectangle.height;
		Color color1 = Color.getHSBColor(0f, 0f, .6f);
		// Color color1 = Color.black;
		Color color2 = Color.white;
		GradientPaint gp = new GradientPaint(w, 0, color1, w, h - 60, color2);
		g2d.setPaint(gp);
		g2d.fillRect(0, 0, w, h);
	}

	private class InfoPanelLayout extends SpringLayout {
		JPanel panel;

		InfoPanelLayout(JPanel panel) {
			this.panel = panel;
		}

		void construct() {
			int count = 0;
			for (JLabel label : labels) {
				labelLayout(label, count);
				count++;
			}
		}

		void labelLayout(JLabel label, int count) {
			int width = getLabelWidth();
			int x = getLabelX(count);
			int height = getLabelHeight();
			putConstraint(NORTH, label, 0, NORTH, panel);
			putConstraint(SOUTH, label, height, NORTH, panel);
			putConstraint(WEST, label, x, WEST, panel);
			putConstraint(EAST, label, x + width, WEST, panel);
		}

		int getLabelWidth() {
			return rectangle.width / labelCount;
		}

		int getLabelX(int count) {
			return getLabelWidth() * count;
		}

		int getLabelHeight() {
			return rectangle.height;
		}
	}
}
