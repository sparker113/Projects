package login;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextField;

public class PasswordKeyListener implements KeyListener {
	StringBuilder stringBuilder;
	JTextField textField;
	final static String REGEX_ACCEPTED = "[a-zA-Z0-9\\.\\(\\)\\[\\]\\{\\}\\|\\*/\\\\\\+\\-]";
	public PasswordKeyListener(JTextField textField) {
		this.textField = textField;
		stringBuilder = new StringBuilder();
		addThisToInput();
	}
	public String getPrivateString() {
		return stringBuilder.toString();
	}
	private void addThisToInput() {
		textField.addKeyListener(this);
	}
	private char privChar = '*';

	@Override
	public void keyTyped(KeyEvent e) {
		char pressed = e.getKeyChar();
		Matcher matcher = Pattern.compile(REGEX_ACCEPTED).matcher(String.valueOf(pressed));
		if(matcher.find()) {
			stringBuilder.append(pressed);
			e.setKeyChar(privChar);
		}else if(e.getKeyCode()==KeyEvent.VK_BACK_SPACE) {
			stringBuilder.delete(stringBuilder.length()-1, stringBuilder.length());
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

}
