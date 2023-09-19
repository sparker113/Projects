import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.concurrent.Semaphore;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.MouseInputListener;

public class NoSand {

	private static final long serialVersionUID = 1L;
	private JDialog noSandDialog;
	JPanel dBox1 = new JPanel();
	JTextArea message = new JTextArea();
	JTextField sandField = new JTextField();
	JButton sandYes = new JButton();
	JButton sandNo = new JButton();
	String s;
	Long yayOrNay;
	String messageText;
	private Semaphore noSandSem;
	int finished;
	Thread t;
	private int x;
	private int y;
	final static int WIDTH = 400;
	final static int HEIGHT = 280;

	public NoSand(int x, int y, String s) {

		noSandDialog = new JDialog();
		noSandDialog.setModal(true);
		noSandDialog.setIconImage(
				new ImageIcon("C:\\Scrape\\Scrape.png").getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
		noSandDialog.setTitle("Correct Sand");
		noSandDialog.setAlwaysOnTop(true);
		this.x = x;
		this.y = y;
		noSandDialog.setBounds(x, y, WIDTH, HEIGHT);
		dBox1.setBounds(0, 0, WIDTH, HEIGHT);
		message = constructMessage();
		sandField = constructSandField();
		sandYes = constructYesButton();
		sandNo = constructNoButton();
		dBox1.add(this.message);
		dBox1.add(this.sandField);
		dBox1.add(this.sandYes);
		dBox1.add(this.sandNo);
		noSandDialog.add(constructPanel(dBox1));
		this.s = s;
		this.message.setText(s);
		noSandDialog.setResizable(false);
		noSandDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dBox1.setVisible(true);
		noSandDialog.paint(noSandDialog.getGraphics());
		noSandDialog.setVisible(true);
	}

	public void setParameters(String s) {

		// this.paint(super.getGraphics());
		// this.setEnabled(true);
	}

	public Long checkTotal() {
		while (this.finished == 0) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}
		Long temp = this.yayOrNay;
		return temp;
	}

	public JTextArea constructMessage() {

		message = new JTextArea();
		message.setWrapStyleWord(true);
		message.setLineWrap(true);
		message.setBackground(Color.LIGHT_GRAY);
		// message.setEditable(false);

		return message;
	}

	public JTextField constructSandField() {

		sandField = new JTextField();
		ClickIt click = new ClickIt();
		sandField.setText("<Input Correct Sand>");
		sandField.setHorizontalAlignment(SwingConstants.CENTER);
		sandField.addMouseListener(click);

		return sandField;
	}

	public JButton constructYesButton() {

		sandYes.setText("Yes");
		sandYes.addActionListener(new YesButtonAction());

		return sandYes;
	}

	public JButton constructNoButton() {

		sandNo.setText("No");
		sandNo.addActionListener(new NoButtonAction());

		return sandNo;
	}

	public JPanel constructPanel(JPanel panel) {

		panel.setBackground(Color.LIGHT_GRAY);
		SpringLayout layout = new SpringLayout();
		layout.putConstraint(SpringLayout.NORTH, message, 10, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.SOUTH, message, 110, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, message, 10, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.EAST, message, 390, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, sandYes, 10, SpringLayout.SOUTH, message);
		layout.putConstraint(SpringLayout.SOUTH, sandYes, 90, SpringLayout.SOUTH, message);
		layout.putConstraint(SpringLayout.WEST, sandYes, 10, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.EAST, sandYes, 190, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, sandNo, 10, SpringLayout.SOUTH, message);
		layout.putConstraint(SpringLayout.SOUTH, sandNo, 90, SpringLayout.SOUTH, message);
		layout.putConstraint(SpringLayout.WEST, sandNo, 210, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.EAST, sandNo, 375, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, sandField, 10, SpringLayout.SOUTH, sandYes);
		layout.putConstraint(SpringLayout.SOUTH, sandField, 30, SpringLayout.SOUTH, sandYes);
		layout.putConstraint(SpringLayout.WEST, sandField, 10, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.EAST, sandField, 190, SpringLayout.WEST, panel);
		panel.setLayout(layout);

		return panel;
	}

	public Long getCorrected() {
		return yayOrNay;
	}

	public int getFinished() {
		return finished;
	}

	public Boolean isVisible() {
		return noSandDialog.isVisible();
	}

	public void dispose() {
		noSandDialog.dispose();
	}

	public class YesButtonAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			// j.setVisible(true);
			yayOrNay = Long.valueOf(sandField.getText().toString());
			finished = 1;
			noSandDialog.setVisible(false);
		}
	}

	public class NoButtonAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			yayOrNay = Long.valueOf(0);
			finished = 0;
			noSandDialog.setVisible(false);
		}
	}

	public class ClickIt implements MouseInputListener {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getSource().equals(sandField) && sandField.getText().contains("<")) {
				sandField.setText("");
			}
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
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseDragged(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseMoved(MouseEvent e) {
			// TODO Auto-generated method stub

		}
	}

}