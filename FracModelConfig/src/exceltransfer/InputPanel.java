package exceltransfer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class InputPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final static String TEXT_FIELD = "Text Field";
	Rectangle rectangle;
	private String labelString;
	private String name;
	private Color color;

	private String setString;

	InputPanel(Rectangle rectangle, String labelString, String name) {
		this.rectangle = rectangle;
		this.labelString = labelString;
		this.name = name;
		this.color = Color.getHSBColor(-.85f, .1f, .85f);
		this.setString = "";
		make();
	}
	InputPanel(Rectangle rectangle, String labelString, String name,Color color) {
		this.rectangle = rectangle;
		this.labelString = labelString;
		this.name = name;
		this.color = color;
		this.setString = "";
		make();
	}

	InputPanel(Rectangle rectangle, String labelString, String name, String setString) {
		this.rectangle = rectangle;
		this.labelString = labelString;
		this.name = name;
		this.color = Color.getHSBColor(-.85f, .1f, .85f);
		this.setString = setString;
		make();
	}

	private JLabel constructLabel() {
		JLabel label = new JLabel();
		System.out.println("Y0 of label: " + rectangle.getY());
		label.setBounds(0, (int) (rectangle.getHeight() * .025), (int) (rectangle.getCenterX() - 15),
				(int) (rectangle.getHeight() * .95));
		label.setText(labelString);
		label.setBackground(color);
		label.setName("Label");
		label.setVerticalAlignment(SwingConstants.CENTER);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setOpaque(true);
		return label;
	}

	private void constructPanel() {
		this.setBounds(rectangle);
		this.setBackground(color);
		this.setName(name);
		setLayout(null);
		this.setVisible(true);
	}

	private JTextField constructTextField() {
		JTextField inputField = new JTextField();

		inputField.setBounds((int) (rectangle.getCenterX()), rectangle.height / 2 - 10,
				(int) (rectangle.getCenterX() - 20), 20);
		inputField.setName(TEXT_FIELD);
		inputField.setText(setString);
		inputField.setBackground(Color.white);
		return inputField;
	}
	private Component getComponentByName(String name) {
		for (Component comp : this.getComponents()) {
			if (comp.getName().equals(name)) {
				return comp;
			}
		}
		return null;
	}
	public String getInput() {
		JTextField inputField = ((JTextField) getComponentByName(TEXT_FIELD));
		return inputField.getText();
	}
	private void make() {
		constructPanel();
		this.add(constructTextField());
		this.add(constructLabel());
		this.setOpaque(true);
		this.setVisible(true);
	}
	public void setInput(String input) {
		JTextField inputField = ((JTextField) getComponentByName(TEXT_FIELD));
		inputField.setText(input);
	}
}
