package login;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SpringLayout;

public class UserNamePassword extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Rectangle rectangle;
	private String username;
	private String password;
	Semaphore semaphore = new Semaphore(0);
	public final static String IMAGE_PROPERTY = "PROGRAM_IMAGE_PATH";
	private final static int WIDTH = 450;
	private final static int HEIGHT = 150;
	public UserNamePassword(Rectangle rectangle) {
		this.rectangle = rectangle;
		constructFrame();
	}
	public UserNamePassword() {
		
	}
	public UserNamePassword(int x, int y, int width, int height) {
		this.rectangle = new Rectangle(x, y, width, height);
		constructFrame();
	}
	public static UserNamePassword getWindowWithTitle(String title) {
		UserNamePassword userNamePassword = new UserNamePassword();
		userNamePassword.rectangle = new Rectangle(getCenterX(WIDTH),getCenterY(HEIGHT),WIDTH,HEIGHT);
		userNamePassword.constructFrame(title);
		return userNamePassword;
	}
	public final static String USERNAME = "username";
	public final static String PASSWORD = "password";
	public UserNamePassword(Rectangle rectangle, String username) {
		this.rectangle = rectangle;
		this.username = username;
		constructFrame();
	}

	public HashMap<String, String> getCredentials() {
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			System.out.println("UserNamePassword::getCredentials");
		}
		if (password == null) {
			return null;
		}
		HashMap<String, String> map = new HashMap<>();
		map.put("username", username);
		map.put("password", password);
		return map;
	}

	private void setFrameIcon() {
		File file = new File(System.getProperty(IMAGE_PROPERTY));
		if (!file.exists()) {
			return;
		}
		setIconImage(new ImageIcon(System.getProperty(IMAGE_PROPERTY)).getImage().getScaledInstance(64, 64,
				Image.SCALE_SMOOTH));
	}

	public static int getCenterX(int width) {
		return Toolkit.getDefaultToolkit().getScreenSize().width / 2 - width / 2;
	}

	public static int getCenterY(int height) {
		return Toolkit.getDefaultToolkit().getScreenSize().height / 2 - height / 2;
	}

	public void setCredentials(String username, String password) {
		this.username = username;
		this.password = password;
	}

	private String getInputText(String name) {
		LabeledInput labeledInput = (LabeledInput) getComponentByNameRecursive(name);
		return labeledInput.getInputString();
	}

	private void constructFrame() {
		if (username == null) {
			LabeledInput usernameInput = constructInput(USERNAME, "Username",
					new Dimension(rectangle.width - 10, 20),false);
			getContentPane().add(usernameInput);
			System.out.println("height: " + rectangle.height / 3);
		} else {
			LabeledInput usernameInput = constructInput(USERNAME, "Username", new Dimension(rectangle.width - 10, 20),
					username);
			add(usernameInput);
			System.out.println("height: " + rectangle.height / 3);
		}
		
		LabeledInput passwordInput = constructInput(PASSWORD, "Password", new Dimension(rectangle.width - 10, 20),true);
		passwordInput.setVisible(true);
		add(passwordInput);
		add(constructButton());
		setLayout(new UsernamePasswordLayout());
		nittyGritty();
	}
	private void constructFrame(String title) {
		if (username == null) {
			LabeledInput usernameInput = constructInput(USERNAME, "Username",
					new Dimension(rectangle.width - 10, 20),false);
			getContentPane().add(usernameInput);
			System.out.println("height: " + rectangle.height / 3);
		} else {
			LabeledInput usernameInput = constructInput(USERNAME, "Username", new Dimension(rectangle.width - 10, 20),
					username);
			add(usernameInput);
			System.out.println("height: " + rectangle.height / 3);
		}
		
		LabeledInput passwordInput = constructInput(PASSWORD, "Password", new Dimension(rectangle.width - 10, 20),true);
		passwordInput.setVisible(true);
		add(passwordInput);
		add(constructButton());
		setLayout(new UsernamePasswordLayout());
		nittyGritty(title);
	}

	private JButton constructButton() {
		JButton button = new JButton();
		button.setText("Save");
		button.setName("button");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String username = getInputText("username");
				String password = getInputText("password");
				setCredentials(username, password);
				semaphore.release();
				dispose();
			}
		});
		button.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					button.doClick();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub

			}

		});
		return button;
	}

	private void nittyGritty() {
		getContentPane().setBackground(Color.getHSBColor(-.85f, .1f, .85f));
		setTitle("Login");
		setFrameIcon();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setWindowListener();
		setBounds(rectangle);
		setVisible(true);
	}
	private void nittyGritty(String title) {
		getContentPane().setBackground(Color.getHSBColor(-.85f, .1f, .85f));
		setTitle(title);
		setFrameIcon();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setWindowListener();
		setBounds(rectangle);
		setVisible(true);
	}

	private void setWindowListener() {
		addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowClosing(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowClosed(WindowEvent e) {
				semaphore.release();

			}

			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub

			}

		});
	}

	public JComponent getComponentByNameRecursive(String name) {
		String comp = null;
		for (Component c : getComponents()) {
			if (c.getName() == null) {
				continue;
			}
			if (c.getName().equals(name)) {
				return (JComponent) c;
			}
		}
		return depthOfSearch((JComponent) this.getContentPane(), name);
	}

	JComponent depthOfSearch(JComponent comp, String name) {
		JComponent jComp = null;
		for (Component c : comp.getComponents()) {
			if (c.getName() == null) {
				jComp = depthOfSearch((JComponent) c, name);
			}
			String compName = c.getName();
			if (compName.equals(name)) {
				return (JComponent) c;
			} else {
				jComp = depthOfSearch((JComponent) c, name);
			}
			if (jComp != null) {
				return jComp;
			}
		}
		return null;
	}

	private LabeledInput constructInput(String name, String labelText, Dimension dimension,boolean privacy) {
		LabeledInput labeledInput = new LabeledInput(labelText, dimension,privacy);
		labeledInput.setName(name);
		return labeledInput;
	}

	private LabeledInput constructInput(String name, String labelText, Dimension dimension, String setText) {
		LabeledInput labeledInput = new LabeledInput(labelText, dimension, setText);
		labeledInput.setName(name);
		return labeledInput;
	}

	private class UsernamePasswordLayout extends SpringLayout {
		final static int SPACING = 5;

		UsernamePasswordLayout() {
			constructLayout();
		}

		void constructLayout() {
			inputLayout((LabeledInput) getComponentByNameRecursive("username"), 0);
			inputLayout((LabeledInput) getComponentByNameRecursive("password"), 1);
			buttonLayout((JButton) getComponentByNameRecursive("button"));
		}

		void inputLayout(LabeledInput input, int index) {
			putConstraint(NORTH, input, getInputY(index), NORTH, getContentPane());
			putConstraint(SOUTH, input, getInputYBase(index), NORTH, getContentPane());
			putConstraint(WEST, input, 0, WEST, UserNamePassword.this);
			putConstraint(EAST, input, rectangle.width - 20, WEST, getContentPane());

		}

		void buttonLayout(JButton button) {
			putConstraint(NORTH, button, getButtonY(), NORTH, getContentPane());
			putConstraint(SOUTH, button, getButtonY() + 25, NORTH, getContentPane());
			putConstraint(WEST, button, getButtonX(), WEST, getContentPane());
			putConstraint(EAST, button, getButtonX() + 80, WEST, getContentPane());
		}

		int getButtonX() {
			return rectangle.width / 2 - 40;
		}

		int getButtonY() {
			return (rectangle.height / 3) + (rectangle.height / 6);
		}

		int getInputY(int index) {
			return (30 * index) + SPACING;
		}

		int getInputYBase(int index) {
			return getInputY(index) + 20;
		}
	}
}

