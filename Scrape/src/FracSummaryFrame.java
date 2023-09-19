import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

public class FracSummaryFrame extends JFrame {
	/*
	 *
	 */
	private static final long serialVersionUID = 1L;
	Rectangle rectangle;
	ArrayList<String> crews;
	String selectedCrew = ALL_CREWS;
	HashMap<String, String> configMap = new HashMap<>();
	Semaphore semaphore = new Semaphore(0);

	FracSummaryFrame(Rectangle rectangle, ArrayList<String> crews) {
		this.rectangle = rectangle;
		this.crews = crews;
		constructFrame();
	}

	final static String TITLE = "Summary Parameters";

	void constructFrame() {
		setBounds(rectangle);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setResizable(false);
		setTitle(TITLE);
		setContentPane(gradientContentPane(Color.getHSBColor(-.85f, .1f, .85f),Color.black));
		setIconImage(new ImageIcon(System.getProperty(Main.IMAGE_PROPERTY)).getImage().getScaledInstance(64, 64,
				Image.SCALE_SMOOTH));
		setLayout(null);
		add(new TimeFramePanel(rectangle.width - 15));
		add(getCrewComboPanel(1));
		add(getButtonPanel(2));
		setVisible(true);
	}
	@Override
	public void dispose() {
		setConfigMap();
		super.dispose();
	}
	JPanel getButtonPanel(int row) {
		JPanel panel = getComponentRowPanel(row);
		panel.add(getButton());
		panel.setVisible(true);
		return panel;
	}

	int getButtonWidth() {
		return rectangle.width * 3 / 4;
	}

	public final static int BUTTON_HEIGHT = 25;

	JButton getButton() {
		JButton button = new JButton();
		button.setText("Get Summary");
		button.setSize(new Dimension(getButtonWidth(), BUTTON_HEIGHT));
		button.addActionListener(getButtonAction());
		button.setVisible(true);
		return button;
	}

	public final static String CREW = "crew";
	public final static String START_DATE = "start_date";
	public final static String END_DATE = "end_date";
	void setNullMap() {
		configMap=null;
		semaphore.release();
	}
	void setConfigMap() {
		configMap.put(CREW, selectedCrew);
		configMap.put(START_DATE, getInputText(TimeFramePanel.START_DATE_FIELD));
		configMap.put(END_DATE, getInputText(TimeFramePanel.END_DATE_FIELD));
		semaphore.release();
	}
	boolean validateInput(String inputDate) {
		return inputDate.matches("\\d{4}\\-\\d{2}\\-\\d{2}");
	}
	private boolean mapSet = false;
	public HashMap<String, String> getConfigMap() throws InterruptedException {
		if(!mapSet) {
			semaphore.acquire();
			mapSet = true;
		}
		return configMap;
	}
	public final static String INVALID_INPUT = "INVALID";
	String getInputText(String compName) {
		JTextField textField = ((JTextField) GUIUtilities.getComponentByName(this, compName));
		String input = textField.getText();
		if(!validateInput(input)) {
			return INVALID_INPUT;
		}
		return input;
	}

	AbstractAction getButtonAction() {
		return new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				Executors.newSingleThreadExecutor().execute(() -> {
					setConfigMap();
					dispose();
				});
			}
		};
	}

	ArrayList<String> getCrews() {
		if (!crews.contains(ALL_CREWS)) {
			crews.add(0, ALL_CREWS);
		}
		return crews;
	}

	AbstractAction getCrewComboAction() {
		return new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent e) {
				selectedCrew = ((JComboBox<String>) e.getSource()).getSelectedItem().toString();
			}
		};
	}

	JPanel getCrewComboPanel(int row) {
		JComboBox<String> comboBox = constructComboBox(CREW_COMBO_BOX, getCrews(), getCrewComboAction());
		comboBox.setVisible(true);
		JPanel panel = getComponentRowPanel(row);
		panel.add(comboBox);
		//panel.setVisible(true);
		return panel;
	}

	public final static int COMPONENT_ROW_HEIGHT = 50;

	JPanel getComponentRowPanel(int row) {
		JPanel panel = new JPanel();
		panel.setBounds(0, row * COMPONENT_ROW_HEIGHT, rectangle.width - 15, COMPONENT_ROW_HEIGHT);
		//panel.setBackground(Color.getHSBColor(-.85f, .1f, .85f));
		panel.setVisible(true);
		panel.setOpaque(false);
		return panel;
	}
	JPanel gradientContentPane(Color color1,Color color2) {
		JPanel panel = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);

				Graphics2D g2d = (Graphics2D)g.create();
				try {
					g2d.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_DEFAULT);
					GradientPaint gradientPaint = new GradientPaint(rectangle.width,0f,color1,rectangle.width,rectangle.height,color2);
					g2d.setPaint(gradientPaint);
					g2d.fillRect(0, 0, rectangle.width, rectangle.height);
				}finally {
					g2d.dispose();
				}
			}
		};

		return panel;
	}
	public final static String ALL_CREWS = "All Crews";
	public final static String CREW_COMBO_BOX = "crew_combo_box";

	JComboBox<String> constructComboBox(String name, ArrayList<String> itemsToAdd, AbstractAction action) {
		JComboBox<String> comboBox = new JComboBox<>();
		comboBox.setName(name);
		if (action != null) {
			comboBox.addActionListener(action);
		}
		((JLabel) comboBox.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		addItemsToComboBox(comboBox, itemsToAdd);
		comboBox.setVisible(true);
		return comboBox;
	}

	void addItemsToComboBox(JComboBox<String> comboBox, ArrayList<String> array) {
		for (String s : array) {
			comboBox.addItem(s);
		}
	}

	class TimeFramePanel extends JPanel {

		public final static int PANEL_HEIGHT = 50;
		public final static int TEXT_FIELD_HEIGHT = 20;
		public final static int INPUT_BOX_BUFFER = 10;
		public final static int PANEL_INPUT_BUFFER = 15;
		int width;

		TimeFramePanel(int width) {
			this.width = width;
			nittyGritty();
		}

		public final static String START_INIT_TEXT = "<START DATE>";
		public final static String END_INIT_TEXT = "<END DATE>";

		void nittyGritty() {
			setBounds(0, 0, width, PANEL_HEIGHT);
			setBackground(Color.getHSBColor(-.85f, .1f, .85f));
			setOpaque(false);
			constructTextField(START_DATE_FIELD, START_INIT_TEXT);
			constructTextField(END_DATE_FIELD, END_INIT_TEXT);
			setLayout(new TimeLayout(this));
			setVisible(true);
		}

		final static String START_DATE_FIELD = "start_date_field";
		final static String END_DATE_FIELD = "field_date_field";

		void constructTextField(String name, String initText) {
			JTextField textField = new JTextField();
			textField.setHorizontalAlignment(SwingConstants.CENTER);
			textField.setName(name);
			textField.setText(initText);
			textField.addKeyListener(new TimeKeyListener(textField));
			textField.setVisible(true);
			add(textField);
		}

		int getTextFieldWidth() {
			return (width - INPUT_BOX_BUFFER - (PANEL_INPUT_BUFFER * 2)) / 2;
		}

		void giveNextFocus(JTextField textField) {
			if (textField.getName().equals(START_DATE_FIELD)) {
				((JTextField) GUIUtilities.getComponentByName(this, END_DATE_FIELD)).requestFocus();
				return;
			}
			((JTextField) GUIUtilities.getComponentByName(this, START_DATE_FIELD)).requestFocus();
		}

		public final static int START_INDEX = 0;
		public final static int END_INDEX = 1;

		class TimeLayout extends SpringLayout {
			TimeFramePanel panel;

			TimeLayout(TimeFramePanel panel) {
				this.panel = panel;
				constructLayout();
			}

			void constructLayout() {
				for (Component c : getComponents()) {
					String name = c.getName();
					if (name == null) {
						continue;
					}
					switch (name) {
					case (START_DATE_FIELD):
						textFieldLayout((JTextField) c, START_INDEX);
						break;
					case (END_DATE_FIELD):
						textFieldLayout((JTextField) c, END_INDEX);
						break;
					}
				}
			}

			void textFieldLayout(JTextField textField, int index) {
				putConstraint(NORTH, textField, TEXT_FIELD_Y, NORTH, panel);
				putConstraint(SOUTH, textField, TEXT_FIELD_Y + TEXT_FIELD_HEIGHT, NORTH, panel);
				putConstraint(WEST, textField, getTextFieldX(index), WEST, panel);
				putConstraint(EAST, textField, getTextFieldEast(index), WEST, panel);
			}

			int getTextFieldX(int index) {
				return PANEL_INPUT_BUFFER + (getTextFieldWidth() + INPUT_BOX_BUFFER) * index;
			}

			int getTextFieldEast(int index) {
				return getTextFieldX(index) + getTextFieldWidth();
			}

			final static int TEXT_FIELD_Y = (PANEL_HEIGHT - TEXT_FIELD_HEIGHT) / 2;

		}

		class TimeKeyListener implements KeyListener {
			JTextField textField;
			Queue<String> queue = new LinkedBlockingQueue<>();
			public final static int MAX_STRING_LENGTH = 10;

			TimeKeyListener(JTextField textField) {
				this.textField = textField;
			}

			public HashSet<Integer> getAllowedKeyCodes() {
				HashSet<Integer> allowedCodes = new HashSet<>();
				allowedCodes.add(KeyEvent.VK_ENTER);
				allowedCodes.add(KeyEvent.VK_DELETE);
				allowedCodes.add(KeyEvent.VK_TAB);
				allowedCodes.add(KeyEvent.VK_END);
				allowedCodes.add(KeyEvent.VK_HOME);
				allowedCodes.add(KeyEvent.VK_LEFT);
				allowedCodes.add(KeyEvent.VK_RIGHT);
				allowedCodes.add(KeyEvent.VK_UP);
				allowedCodes.add(KeyEvent.VK_DOWN);
				allowedCodes.add(KeyEvent.VK_BACK_SPACE);
				return allowedCodes;
			}

			void addToQueue(char keyChar) {
				if (textField.getText().matches("((\\<)?)([A-Za-z\\s]+)((\\>)?)")) {
					textField.setText("");
				}
				if (textField.getText().length() == MAX_STRING_LENGTH) {
					giveNextFocus(textField);
					queue.clear();
					return;
				}
				queue.offer(String.valueOf(keyChar));
				writeString();
			}

			synchronized void writeString() {

				while (queue.size() > 0) {
					addHyphen(textField.getText());
					String setText = textField.getText() + queue.poll();
					textField.setText(setText);
					addHyphen(setText);
				}
				if (textField.getText().length() == MAX_STRING_LENGTH) {
					giveNextFocus(textField);
				}
			}

			void addHyphen(String currentText) {
				if (textField.getText().length() == 4 | textField.getText().length() == 7) {
					textField.setText(currentText + "-");
				}
			}

			public boolean allowKey(int keyCode) {
				HashSet<Integer> allowedCodes = getAllowedKeyCodes();
				return allowedCodes.contains(keyCode);
			}

			@Override
			public void keyTyped(KeyEvent e) {
				e.consume();

			}

			@Override
			public void keyPressed(KeyEvent e) {
				String keyString = String.valueOf(e.getKeyChar());
				if (!allowKey(e.getKeyCode()) & !keyString.matches("\\d")) {
					System.out.println(
							"Not Allowed: " + e.getKeyCode() + " - > Backspace KeyCode: " + KeyEvent.VK_BACK_SPACE);
					e.consume();
				} else if (keyString.matches("\\d")) {
					addToQueue(e.getKeyChar());
					e.consume();
				}
				System.out.println("Pressed KeyCode: " + e.getKeyCode());

			}

			@Override
			public void keyReleased(KeyEvent e) {
				System.out.println("Released KeyCode: " + e.getKeyCode());
				e.consume();
			}

		}
	}
}
