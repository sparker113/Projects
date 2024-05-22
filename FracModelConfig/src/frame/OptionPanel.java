package frame;

import java.awt.Color;
import java.awt.Rectangle;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;

public class OptionPanel extends JPanel{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static int DEFAULT_X = 0;
	private static int DEFAULT_Y = 0;
	public static String PANEL_NAME = "option_panel";
	private Color color = Color.lightGray;

	public OptionPanel(int width,int height){
		nittyGritty(new Rectangle(DEFAULT_X,DEFAULT_Y,width,height));
	}
	public OptionPanel(Rectangle rectangle){
		nittyGritty(rectangle);

	}
	private void nittyGritty(Rectangle rectangle) {
		setBounds(rectangle);
		setBackground(color);
	}

	public void addOption(String buttonText,AbstractAction action) {
		JButton button = new JButton();
		button.setText(buttonText);
		button.setName(buttonText);
		button.setBackground(Color.white);
		button.setForeground(Color.black);
		button.addActionListener(action);
		add(button);
	}


}
