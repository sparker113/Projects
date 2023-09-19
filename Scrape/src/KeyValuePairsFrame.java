import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

public class KeyValuePairsFrame<T> extends JFrame {
	private static final int LEFT = 35;
	private static final int RIGHT = 50;
	private static final int TOP = 25;
	private static final int BOTTOM = 25;
	Map<String, T> map;
	String title;
	Color color = Color.getHSBColor(-.85f, .1f, .85f);

	public KeyValuePairsFrame(Map<String, T> map, String title) {
		this.map = map;
		this.title = title;
		nittyGritty();
	}

	void nittyGritty() {
		setTitle(title);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setBounds(getFrameBounds());
		add(new KeyValuePairsJPanel());
		setVisible(true);
	}

	Rectangle getFrameBounds() {
		int width = getReqWidth();
		int height = getReqHeight();
		return new Rectangle(getXForCenter(width), getYForCenter(height), width, height);
	}

	void setColor(Color color) {
		this.color = color;
	}

	int getReqWidth() {
		int width = getMaxWidth();
		return width + LEFT + RIGHT;
	}

	int getMaxWidth() {
		int width = 0;
		for (String s : map.keySet()) {
			int length = s.length() + String.valueOf(map.get(s)).length();
			width = width < length * 10 ? length * 10 : width;
		}
		return width;
	}

	int getReqHeight() {
		return map.size() * 25 + TOP + BOTTOM + 45;
	}

	int getXForCenter(int width) {
		int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
		return screenWidth / 2 - width / 2;
	}

	int getYForCenter(int height) {
		int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
		return screenHeight / 2 - height / 2;
	}

	class KeyValuePairsJPanel extends JPanel {
		AbstractAction buttonAction;
		String buttonText;

		KeyValuePairsJPanel() {
			this.buttonAction = getDefaultButtonAction();
			this.buttonText = "Copy Selected";
			nittyGritty();
		}

		KeyValuePairsJPanel(AbstractAction buttonAction, String buttonText) {
			this.buttonAction = buttonAction;
			this.buttonText = buttonText;
			nittyGritty();
		}

		AbstractAction getDefaultButtonAction() {
			return new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					copyLabelsToClipboard();
					KeyValuePairsFrame.this.dispose();
				}
			};
		}

		void nittyGritty() {
			setBounds(0, 0, getReqWidth(), getReqHeight());
			setBackground(color);
			addComps();
			setLayout(new PanelLayout());
			setVisible(true);
		}

		void addComps() {
			for (String s : map.keySet()) {
				add(constructLabel(s + "-->" + String.valueOf(map.get(s)), s));
			}
			add(constructButton());
		}

		JButton constructButton() {
			JButton button = new JButton();
			button.setText(buttonText);
			button.setName("button");
			button.addActionListener(buttonAction);
			return button;
		}

		private LinkedHashMap<String, String> getMapFromLabels() {
			LinkedHashMap<String, String> map = new LinkedHashMap<>();
			for (Component c : getComponents()) {
				if (c.getName() == null) {
					continue;
				}
				String name = c.getName();
				if (name.equals("button")) {
					continue;
				}
				JLabel label = (JLabel) c;
				if (label.getBackground().equals(Color.LIGHT_GRAY)) {
					String text = label.getText();
					map.put(getKeyFromText(text), getValueFromText(text));
				}
			}
			return map;
		}

		private static String getClipboardStringFromMap(Map<String, String> map) {
			String string = "";
			for (String s : map.keySet()) {
				string += "\n" + s + "\t" + map.get(s);
			}
			return string.equals("") ? "" : string.substring(1);
		}

		private void copyLabelsToClipboard() {
			LinkedHashMap<String, String> map = getMapFromLabels();
			String mapString = getClipboardStringFromMap(map);
			if (mapString.equals("")) {
				return;
			}
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(new StringSelection(mapString), null);
		}

		private String getKeyFromText(String text) {
			Matcher matcher = Pattern.compile("\\-\\-\\>").matcher(text);
			if (matcher.find()) {
				return text.substring(0, matcher.start());
			}
			return text;
		}

		private String getValueFromText(String text) {
			Matcher matcher = Pattern.compile("\\-\\-\\>").matcher(text);
			if (matcher.find()) {
				return text.substring(matcher.end());
			}
			return text;
		}

		JLabel constructLabel(String text, String name) {
			JLabel label = new JLabel();
			label.setText(text);
			label.setName(name);
			label.addMouseListener(new MouseListener() {

				@Override
				public void mouseClicked(MouseEvent e) {
					// TODO Auto-generated method stub

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
					if (e.getModifiersEx() == InputEvent.BUTTON1_DOWN_MASK) {
						JLabel label = ((JLabel) e.getSource());
						if (label.getBackground().equals(Color.LIGHT_GRAY)) {
							label.setBackground(color);
							return;
						}
						label.setBackground(Color.LIGHT_GRAY);
					}
				}

				@Override
				public void mouseExited(MouseEvent e) {
					// TODO Auto-generated method stub

				}

			});
			label.setBackground(color);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			label.setOpaque(true);
			label.setVisible(true);
			return label;
		}

		class PanelLayout extends SpringLayout {
			private static final int LABELHEIGHT = 25;
			private static final int BUTTONHEIGHT = 25;

			PanelLayout() {
				construct();
			}

			void construct() {
				int i = 0;
				for (Component c : getComponents()) {
					if (c.getName() == null) {
						continue;
					}
					String name = c.getName();
					switch (name) {
					case ("button"):
						buttonLayout((JButton) c);
						break;
					default:
						labelLayout((JLabel) c, i);
						i++;
						break;
					}
				}
			}

			void labelLayout(JLabel label, int i) {
				putConstraint(NORTH, label, getNorth(i), NORTH, KeyValuePairsJPanel.this);
				putConstraint(SOUTH, label, getNorth(i + 1), NORTH, KeyValuePairsJPanel.this);
				putConstraint(WEST, label, LEFT, WEST, KeyValuePairsJPanel.this);
				putConstraint(EAST, label, getEast(), WEST, KeyValuePairsJPanel.this);
			}

			void buttonLayout(JButton button) {
				putConstraint(NORTH, button, getButtonNorth(), NORTH, KeyValuePairsJPanel.this);
				putConstraint(SOUTH, button, getButtonSouth(), NORTH, KeyValuePairsJPanel.this);
				putConstraint(WEST, button, getButtonWest(), WEST, KeyValuePairsJPanel.this);
				putConstraint(EAST, button, getButtonEast(), WEST, KeyValuePairsJPanel.this);
			}

			int getEast() {
				return LEFT + getMaxWidth();
			}

			int getNorth(int i) {
				return (TOP / 2) + LABELHEIGHT * i;
			}

			int getButtonNorth() {
				return getReqHeight() - ((BOTTOM / 2) + 55) - (BUTTONHEIGHT / 2);
			}

			int getButtonSouth() {
				return getButtonNorth() + BUTTONHEIGHT;
			}

			int getButtonWidth() {
				return getReqWidth() * 2 / 3;
			}

			int getButtonWest() {
				return (getReqWidth() / 2) - (getButtonWidth() / 2);
			}

			int getButtonEast() {
				return getButtonWest() + getButtonWidth();
			}
		}
	}
}
