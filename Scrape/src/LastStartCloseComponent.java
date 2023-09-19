import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class LastStartCloseComponent extends JPanel {
	private JTextField lastClose;
	private JTextField stageOpenTime;
	private JTextField stageCloseTime;
	private JTextField lastCloseDate;
	private JTextField stageOpenDate;
	private JTextField stageCloseDate;
	private JLabel close1;
	private JLabel close2;
	private JLabel close3;
	private int w;
	private int h;

	LastStartCloseComponent(int x, int y, int w, int h) {
		this.w = w;
		this.h = h;
		this.setBounds(x, y, w, h);
		nittyGritty();
	}

	private void nittyGritty() {
		constructComponents();
		addComponents();
		this.setLayout(new LastStartCloseLayout());
		this.setBackground(Color.black);
		this.setVisible(true);
	}

	public LastStartCloseComponent getThis() {
		return this;
	}

	public void constructComponents() {
		this.lastClose = constructTextField("last_close_time");
		this.stageOpenTime = constructTextField("open_time");
		this.stageCloseTime = constructTextField("close_time");
		this.lastCloseDate = constructTextField("last_close_date",
				LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
		this.stageOpenDate = constructTextField("open_date", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
		this.stageCloseDate = constructTextField("close_date",
				LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
		this.close1 = constructLabel("Last Close:", "last_close_label");
		this.close2 = constructLabel("Open Date/Time:", "open_label");
		this.close3 = constructLabel("Close Date/Time:", "close_label");
	}

	public static void updateDates(LastStartCloseComponent lastStartClose) {
		String dateNow = LocalDateTime.parse(LocalDateTime.now().toString())
				.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		for (Component c : lastStartClose.getComponents()) {
			if (c.getName() == null) {
				continue;
			}
			if (c.getName().contains("date")) {
				((JTextField) c).setText(dateNow);
			} else if (c.getName().contains("time")) {
				((JTextField) c).setText("");
			}
		}
	}

	public void addComponents() {
		this.add(close1);
		this.add(lastClose);
		this.add(close2);
		this.add(stageOpenTime);
		this.add(close3);
		this.add(stageCloseTime);
		this.add(lastCloseDate);
		this.add(stageOpenDate);
		this.add(stageCloseDate);
	}

	public JLabel constructLabel(String text, String name) {
		JLabel label = new JLabel();
		label.setName(name);
		label.setText(text);
		label.setBackground(Color.BLACK);
		label.setForeground(Color.WHITE);
		label.setVisible(true);
		label.setOpaque(true);
		return label;
	}

	public JTextField constructTextField(String name) {
		JTextField textField = new JTextField();
		textField.setName(name);
		textField.setOpaque(true);
		textField.setEditable(true);
		textField.setVisible(true);
		if (name.contains("time")) {
			textField.addKeyListener(new LastStartCloseKeyListener());
			textField.addFocusListener(new DateTimeFocusListener());
		} else {
			textField.addKeyListener(new DateTextKeyListener());
		}
		return textField;
	}

	public JTextField constructTextField(String name, String setText) {
		JTextField textField = new JTextField();
		textField.setName(name);
		textField.setText(setText);
		textField.setOpaque(true);
		textField.setToolTipText("yyyy-mm-dd");
		textField.setEditable(true);
		textField.setVisible(true);
		if (name.contains("time")) {
			textField.addKeyListener(new LastStartCloseKeyListener());
		} else {
			textField.addKeyListener(new DateTextKeyListener());
		}
		return textField;
	}

	public HashMap<String, String> getTimes() {
		HashMap<String, String> timesMap = new HashMap<>();
		timesMap.put("Last_Close", String.valueOf(lastClose.getText()));
		timesMap.put("Stage_Open", String.valueOf(stageOpenTime.getText()));
		timesMap.put("Stage_Close", String.valueOf(stageCloseTime.getText()));
		return timesMap;
	}

	public HashMap<String, String> getDateTimes() {
		HashMap<String, String> dateTimesMap = new HashMap<>();
		dateTimesMap.put("last", String.valueOf(lastCloseDate.getText() + " " + lastClose.getText()));
		dateTimesMap.put("open", String.valueOf(stageOpenDate.getText() + " " + stageOpenTime.getText()));
		dateTimesMap.put("close", String.valueOf(stageCloseDate.getText() + " " + stageCloseTime.getText()));
		return dateTimesMap;
	}

	public HashMap<String, String> getDateTimesWithT() {
		HashMap<String, String> dateTimesMap = new HashMap<>();
		dateTimesMap.put("last", String.valueOf(lastCloseDate.getText() + "T" + lastClose.getText()));
		dateTimesMap.put("open", String.valueOf(stageOpenDate.getText() + "T" + stageOpenTime.getText()));
		dateTimesMap.put("close", stageCloseDate.getText() + "T" + stageCloseTime.getText());
		return dateTimesMap;
	}

	public final static String CLOSE = "close";
	public final static String OPEN = "open";
	public final static String LAST = "last";

	public HashMap<String, String> getDateTimesPlusClose() {
		HashMap<String, String> dateTimesMap = new HashMap<>();
		dateTimesMap.put("last", String.valueOf(lastCloseDate.getText() + " " + lastClose.getText()));
		dateTimesMap.put("open", getAddTimeFromString(stageOpenDate.getText() + "T" + stageOpenTime.getText(), -1));
		dateTimesMap.put("close", getAddTimeFromString(stageCloseDate.getText() + "T" + stageCloseTime.getText(), 5));
		return dateTimesMap;
	}

	public HashMap<String, String> getDateTimes(int plusMinusOpen, int plusMinusClose) {
		HashMap<String, String> dateTimesMap = new HashMap<>();
		dateTimesMap.put("last", String.valueOf(lastCloseDate.getText() + " " + lastClose.getText()));
		dateTimesMap.put("open",
				getAddTimeFromString(stageOpenDate.getText() + "T" + stageOpenTime.getText(), plusMinusOpen));
		dateTimesMap.put("close",
				getAddTimeFromString(stageCloseDate.getText() + "T" + stageCloseTime.getText(), plusMinusClose));
		return dateTimesMap;
	}
	public String getDateTime(String dateTimeSelection,int plusMinus) {
		switch(dateTimeSelection) {
		case(OPEN):
			return getAddTimeFromString(stageOpenDate.getText() + "T" + stageOpenTime.getText(), plusMinus);
		case(CLOSE):
			return getAddTimeFromString(stageCloseDate.getText() + "T" + stageCloseTime.getText(), plusMinus);
		case(LAST):
			return getAddTimeFromString(lastCloseDate.getText() + "T"+lastClose.getText(),plusMinus);
		}
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss"));
	}
	public static HashMap<String, String> getDateTimes(String startDate, String startTime, String endDate,
			String endTime, int plusMinusOpen, int plusMinusClose) {
		HashMap<String, String> dateTimesMap = new HashMap<>();

		dateTimesMap.put("open", getAddTimeFromString(startDate + "T" + startTime, plusMinusOpen));
		dateTimesMap.put("close", getAddTimeFromString(endDate + "T" + endTime, plusMinusClose));
		return dateTimesMap;
	}

	public static HashMap<String, String> getDateTimes(HashMap<String, String> requestTimes, int plusMinusOpen,
			int plusMinusClose) {
		HashMap<String, String> newMap = new HashMap<>();
		newMap.put(OPEN, LocalDateTime.parse(requestTimes.get(OPEN).replace(" ", "T"))
				.format(DateTimeFormatter.ofPattern("YYYY-MM-dd'T'HH:mm")));
		newMap.put(CLOSE, LocalDateTime.parse(requestTimes.get(CLOSE).replace(" ", "T"))
				.format(DateTimeFormatter.ofPattern("YYYY-MM-dd'T'HH:mm")));
		return newMap;
	}

	private static String getAddTimeFromString(String dateTimeString, Integer plusMins) {
		String dateTime = "";
		try {
			dateTime = LocalDateTime.parse(dateTimeString).plusMinutes(Long.valueOf(plusMins))
					.format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm"));
		} catch (DateTimeParseException e) {
			return "CHECK TIMES";
		}
		return dateTime;
	}

	public void transferFocus(JTextField comp) {
		String name = comp.getName();
		if (name == null) {
			return;
		}
		JTextField field = null;
		switch (name) {
		case ("last_close_time"):
			field = (JTextField) GUIUtilities.getComponentByName(LastStartCloseComponent.this, "open_time");
			field.requestFocus();
			field.selectAll();
			break;
		case ("open_time"):
			field = (JTextField) GUIUtilities.getComponentByName(LastStartCloseComponent.this, "close_time");
			field.requestFocus();
			field.selectAll();
			break;
		case ("close_time"):
			field = (JTextField) GUIUtilities.getComponentByName(LastStartCloseComponent.this, "last_close_time");
			field.requestFocus();
			field.selectAll();
		}
	}

	public void fixTimes(JTextField textField) {
		String sourceText = textField.getText();
		Matcher match = Pattern.compile(":").matcher(sourceText);
		if (match.find()) {
			String newText = sourceText;
			int hourLength = match.end();
			if (hourLength == 2) {
				newText = "0" + sourceText;
			}
			sourceText = newText;
			if (hourLength == 3 & sourceText.toCharArray().length == 4 | newText.toCharArray().length == 4) {
				newText = sourceText + "0";
			}
			textField.setText(newText);
		} else {
			textField.setText("");
		}
	}

	class LastStartCloseLayout extends SpringLayout {
		LastStartCloseLayout() {
			construct();
		}

		public void construct() {
			int timeCount = 0;
			int dateCount = 0;
			int labelCount = 0;
			for (Component c : getThis().getComponents()) {
				String ident = c.getName().split("_")[c.getName().split("_").length - 1];
				switch (ident) {
				case ("time"):
					timeLayout((JTextField) c, timeCount);
					timeCount++;
					break;
				case ("date"):
					dateLayout((JTextField) c, dateCount);
					dateCount++;
					break;
				case ("label"):
					labelLayout((JLabel) c, labelCount);
					labelCount++;
					break;
				}
			}
		}

		public void dateLayout(JTextField textField, int row) {
			int height = h / 3;
			int y = height * (row);
			putConstraint(NORTH, textField, y + 1, NORTH, getThis());
			putConstraint(SOUTH, textField, y + height + 1, NORTH, getThis());
			putConstraint(WEST, textField, w / 3, WEST, getThis());
			putConstraint(EAST, textField, w * 2 / 3, WEST, getThis());
		}

		public void timeLayout(JTextField textField, int row) {
			int height = h / 3;
			int y = height * (row);
			putConstraint(NORTH, textField, y + 1, NORTH, getThis());
			putConstraint(SOUTH, textField, y + height + 1, NORTH, getThis());
			putConstraint(WEST, textField, w * 2 / 3, WEST, getThis());
			putConstraint(EAST, textField, w, WEST, getThis());
		}

		public void labelLayout(JLabel label, int row) {
			int height = h / 3;
			int y = height * (row);
			putConstraint(NORTH, label, y + 1, NORTH, getThis());
			putConstraint(SOUTH, label, y + height + 1, NORTH, getThis());
			putConstraint(WEST, label, 0, WEST, getThis());
			putConstraint(EAST, label, w / 3, WEST, getThis());
		}
	}

	public class DateTimeFocusListener implements FocusListener {

		@Override
		public void focusGained(FocusEvent e) {
			((JTextField) e.getComponent()).selectAll();
		}

		@Override
		public void focusLost(FocusEvent e) {
			fixTimes((JTextField) e.getComponent());

		}

	}

	public class LastStartCloseKeyListener implements KeyListener {

		@Override
		public void keyTyped(KeyEvent e) {
			Matcher match = Pattern.compile("[0-9]").matcher(String.valueOf(e.getKeyChar()));
			if (!match.find()) {
				e.consume();
				return;
			} else if (((JTextField) e.getComponent()).getCaretPosition() == 4) {
				transferFocus((JTextField) e.getComponent());
			}
		}

		@Override
		public void keyPressed(KeyEvent e) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_ENTER, KeyEvent.VK_DELETE, KeyEvent.VK_BACK_SPACE, KeyEvent.VK_TAB:

				if (e.getKeyCode() == KeyEvent.VK_ENTER | e.getKeyCode() == KeyEvent.VK_TAB) {
					transferFocus((JTextField) e.getComponent());
				}
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					((JTextField) e.getSource()).setText("");
				}
				break;
			default:
				String text = ((JTextField) e.getComponent()).getText() + e.getKeyChar();
				Matcher match = Pattern.compile("[0-9]").matcher(String.valueOf(e.getKeyChar()));
				if (!match.find()) {
					e.consume();
					return;
				}

				break;
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			Matcher match = Pattern.compile("[0-9\\d:]").matcher(String.valueOf(e.getKeyChar()));
			if (!match.find()) {
				e.consume();
				return;
			}
			if (((JTextField) e.getComponent()).getCaretPosition() == 2
					&& String.valueOf(e.getKeyChar()).matches("[0-9]")) {
				String text = ((JTextField) e.getComponent()).getText();
				((JTextField) e.getComponent()).setText(text + ":");
			}
		}

	}

	private class DateTextKeyListener implements KeyListener {

		Boolean consume;
		String text;

		@Override
		public void keyTyped(KeyEvent e) {
			if (consume == null || consume) {
				e.consume();
				return;
			}
		}

		@Override
		public void keyPressed(KeyEvent e) {
			Matcher matcher = Pattern.compile("\\d|\\t|\\s").matcher(String.valueOf(e.getKeyChar()));
			int code = e.getKeyCode();
			JTextField source = (JTextField) e.getSource();
			if ((code != KeyEvent.VK_ENTER & code != KeyEvent.VK_TAB & code != KeyEvent.VK_BACK_SPACE
					& code != KeyEvent.VK_DELETE & !matcher.find())) {
				if (source.getText().length() >= 10 & code != KeyEvent.VK_BACK_SPACE
						& (code < KeyEvent.VK_LEFT | code > KeyEvent.VK_DOWN)) {
					((JTextField) e.getSource()).setCaretPosition(source.getText().length());
				} else {
					consume = false;
					text = source.getText() + e.getKeyChar();
					return;
				}
				consume = true;
				e.consume();
			} else if (code == KeyEvent.VK_ENTER) {
				lastClose.transferFocus();
				consume = false;
			} else {
				consume = false;
				text = source.getText() + e.getKeyChar();
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {

			if (consume == null || consume) {
				e.consume();
				return;
			} else {
				JTextField source = (JTextField) e.getSource();
				if (e.getKeyCode() != KeyEvent.VK_BACK_SPACE & (text.length() == 4 | text.length() == 7)) {
					source.setText(text + "-");
				}
			}
		}

	}
}
