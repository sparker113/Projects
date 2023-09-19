import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class SpecificInputPane extends JFrame {
	ImageIcon image;
	private JPanel panel;
	private JButton button;
	private JTextArea area;
	private JTextField textField;
	private Boolean addKeyListener;

	SpecificInputPane(String inputText) {
		this.addKeyListener = true;
		this.image = new ImageIcon("C:\\Scrape\\scrape.png");
		this.setIconImage(image.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
		this.setBounds(200, 200, 220, 165);
		this.button = constructButton();
		this.area = constructTextArea(inputText);
		this.textField = constructTextField();
		this.panel = constructPanel();
		this.setResizable(false);
		this.add(panel);
		this.setAlwaysOnTop(true);
		this.setVisible(false);
	}

	SpecificInputPane(String inputText, Boolean addKeyListener) {
		this.addKeyListener = addKeyListener;
		this.image = new ImageIcon("C:\\Scrape\\scrape.png");
		this.setIconImage(image.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
		this.setBounds(200, 200, 220, 165);
		this.button = constructButton();
		this.area = constructTextArea(inputText);
		this.textField = constructTextField();
		this.panel = constructPanel();
		this.setResizable(false);
		this.add(panel);
		this.setAlwaysOnTop(true);
		this.setVisible(false);
	}

	public JPanel constructPanel() {
		JPanel panel = addComponents(new JPanel(), button, area, textField);
		panel.setBounds(this.getBounds());
		panel.setLayout(new InputPaneLayout(panel, button, area, textField));
		panel.setBackground(Color.getHSBColor((float) -.85, (float) .1, (float) .85));
		panel.setVisible(true);
		return panel;
	}

	public JButton constructButton() {
		JButton button = new JButton();
		button.setText("Save");
		button.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		button.setBackground(Color.white);
		button.setOpaque(true);
		button.setVisible(true);
		return button;
	}

	public JTextArea constructTextArea(String inputText) {
		JTextArea area = new JTextArea();
		area.setBackground(Color.getHSBColor((float) -.85, (float) .1, (float) .85));
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setText(inputText);
		area.setEditable(false);
		area.setOpaque(true);
		area.setVisible(true);
		return area;
	}

	public JTextField constructTextField() {
		JTextField textField = new JTextField();
		if (addKeyListener) {
			textField.addKeyListener(new InputKeyListener());
		}
		textField.setEditable(true);
		textField.setVisible(true);
		return textField;
	}

	public JPanel addComponents(JPanel panel, JComponent... a) {
		int i = 0;
		for (JComponent b : a) {
			panel.add(b);
			System.out.println(i++);
		}
		return panel;
	}

	public void writeInputToFile(String path) throws IOException {
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(path)));
		String input = getInputWithoutClearing();
		bufferedWriter.write(input);
		bufferedWriter.close();
	}

	public String getInputWithoutClearing() {
		String input = textField.getText();
		return input;
	}

	public String getInput() {
		String input = textField.getText();
		clearInput();
		return input;
	}

	public void clearInput() {
		textField.setText("");
	}

	public Boolean isEmpty() {
		Boolean empty = null;
		if (textField.getText() != null & !textField.getText().toString().equals("")) {
			empty = false;
		} else {
			empty = true;
		}
		return empty;
	}

	public void setButtonAction(ActionListener actionListener) {
		this.button.addActionListener(actionListener);
	}

	public void setColor(float h, float s, float b) {
		this.area.setBackground(Color.getHSBColor(h, s, b));
		this.panel.setBackground(Color.getHSBColor(h, s, b));
	}

	private class InputPaneLayout extends SpringLayout {
		InputPaneLayout(JPanel panel, JButton button, JTextArea area, JTextField textField) {
			this.putConstraint(SpringLayout.NORTH, area, 15, SpringLayout.NORTH, panel);
			this.putConstraint(SpringLayout.SOUTH, area, 80, SpringLayout.NORTH, panel);
			this.putConstraint(SpringLayout.WEST, area, 10, SpringLayout.WEST, panel);
			this.putConstraint(SpringLayout.EAST, area, 210, SpringLayout.WEST, panel);
			this.putConstraint(SpringLayout.NORTH, textField, 85, SpringLayout.NORTH, panel);
			this.putConstraint(SpringLayout.SOUTH, textField, 105, SpringLayout.NORTH, panel);
			this.putConstraint(SpringLayout.WEST, textField, 0, SpringLayout.WEST, area);
			this.putConstraint(SpringLayout.EAST, textField, panel.getWidth() - 35, SpringLayout.WEST, panel);
			this.putConstraint(SpringLayout.NORTH, button, 105, SpringLayout.NORTH, panel);
			this.putConstraint(SpringLayout.SOUTH, button, 125, SpringLayout.NORTH, panel);
			this.putConstraint(SpringLayout.WEST, button, 30, SpringLayout.WEST, area);
			this.putConstraint(SpringLayout.EAST, button, panel.getWidth() - 60, SpringLayout.WEST, panel);
		}

	}

	private class InputKeyListener implements KeyListener {
		private Boolean consume = false;

		@Override
		public void keyTyped(KeyEvent e) {

			if (consume) {
				e.consume();
			}

		}

		@Override
		public void keyPressed(KeyEvent e) {

			int i = e.getKeyCode();
			System.out.println(!(i > 47 & i < 58));
			System.out.println(!(i > 95 & i < 106));
			System.out.println(i != 44);
			System.out.println(i != 8);
			if (!(i > 47 & i < 58) && !(i > 95 & i < 106) && i != 44 && i != 8) {
				e.consume();
				System.out.println("consumed");
				consume = true;
			} else {
				consume = false;
			}

		}

		@Override
		public void keyReleased(KeyEvent e) {

			if (consume) {
				e.consume();
			}

		}

	}
}
