import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

public class SumPanel extends JPanel {
	JTable table;
	JLabel label;

	SumPanel(JTable table) {
		this.table = table;
		this.label = new JLabel();
		construction();
	}

	private void construction() {
		// this.setBackground(Color.WHITE);
		this.setLayout(new FlowLayout(FlowLayout.LEADING));
		this.label.setBackground(null);
		this.label.setFont(Font.getFont("Ariel"));
		this.label.setText("Sum:");
		this.label.setOpaque(true);
		this.label.setVisible(true);
		this.add(this.label);
		this.setOpaque(true);
		this.setVisible(true);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		int w = getWidth();
		int h = getHeight();
		float color1HSB[] = { 0, 0, 0 };
		Color.RGBtoHSB(247, 253, 252, color1HSB);
		Color color1 = Color.getHSBColor(0, 0, (float) (.85));
		Color color2 = Color.white;
		GradientPaint gp = new GradientPaint(w, h, color1, w, 0, color2);
		g2d.setPaint(gp);
		g2d.fillRect(0, 0, w, h);
	}

	private Double getTableValues() {
		int[] rows = table.getSelectedRows();
		int[] columns = table.getSelectedColumns();
		double sum = 0.0;
		for (int r : rows) {
			for (int c : columns) {
				sum = sum + Double.valueOf(String.valueOf(table.getValueAt(r, c)));
			}
		}
		return sum;
	}

	public SumPanel updateText() {
		Double sum = getTableValues();
		this.label.setText("SUM: " + String.valueOf(sum));
		this.label.setHorizontalAlignment(SwingConstants.LEFT);
		this.label.setHorizontalTextPosition(SwingConstants.LEFT);
		return this;
	}
}
