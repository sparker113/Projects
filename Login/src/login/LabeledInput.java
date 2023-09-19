package login;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.JTextField;

public class LabeledInput extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Dimension dimension;
	String labelText;
	Color color;
	String setText;
	boolean privacy = false;
	PasswordKeyListener passwordKeyListener;
	public LabeledInput(String labelText,Dimension dimension,String setText){
		this.labelText = labelText;
		this.dimension = dimension;
		this.setText = setText;
		color = Color.getHSBColor(-.85f, .1f, .85f);
		construct();
	}
	public LabeledInput(String labelText,Dimension dimension){
		this.labelText = labelText;
		this.dimension = dimension;
		color = Color.getHSBColor(-.85f, .1f, .85f);
		construct();
	}
	public LabeledInput(String labelText,Dimension dimension,boolean privacy){
		this.labelText = labelText;
		this.dimension = dimension;
		color = Color.getHSBColor(-.85f, .1f, .85f);
		this.privacy = privacy;
		construct();
	}
	public LabeledInput(String labelText,Dimension dimension,Color color){
		this.labelText = labelText;
		this.dimension = dimension;
		this.color = color;
		construct();
	}
	public String getInputString() {
		if(privacy) {
			return passwordKeyListener.getPrivateString();
		}
		return getTextField().getText();
	}
	private void construct() {
		add(constructLabel());
		if(setText==null) {
			add(constructTextField());
		}else {
			add(constructTextField(setText));
		}
		
		setLayout(new InputLayout());
		nittyGritty();
	}
	private void nittyGritty() {
		setBackground(color);
		setOpaque(true);
		setVisible(true);
	}
	private JTextField getTextField() {
		for(Component c:getComponents()) {
			if(c.getName()==null) {
				continue;
			}
			if(c.getName().equals("input")) {
				return (JTextField)c;
			}
		}
		return null;
	}
	private JTextField constructTextField() {
		JTextField textField = new JTextField();
		if(privacy) {
			passwordKeyListener = new PasswordKeyListener(textField);
		}
		textField.setName("input");
		textField.setEditable(true);
		textField.setVisible(true);
		return textField;
	}
	private JTextField constructTextField(String setText) {
		JTextField textField = new JTextField();
		textField.setName("input");
		textField.setText(setText);
		textField.setEditable(true);
		textField.setVisible(true);
		return textField;
	}
	private JLabel constructLabel() {
		JLabel label = new JLabel();
		label.setName("label");
		label.setBackground(color);
		label.setHorizontalAlignment(SwingConstants.CENTER);		
		label.setText(labelText);
		label.setOpaque(true);
		label.setVisible(true);
		return label;
	}
	private class InputLayout extends SpringLayout{
		InputLayout(){
			constructLayout();
		}
		void constructLayout() {
			for(Component c:LabeledInput.this.getComponents()) {
				if(c.getName()==null) {
					continue;
				}
				String name = c.getName();
				switch(name) {
				case("label"):
					labelLayout((JLabel)c);
					break;
				case("input"):
					inputLayout((JTextField)c);
					break;
				}
			}
		}
		
		void labelLayout(JLabel label) {
			putConstraint(NORTH,label,0,NORTH,LabeledInput.this);
			putConstraint(SOUTH,label,dimension.height,NORTH,LabeledInput.this);
			putConstraint(WEST,label,0,WEST,LabeledInput.this);
			putConstraint(EAST,label,dimension.width/2,WEST,LabeledInput.this);
		}
		void inputLayout(JTextField input) {
			putConstraint(NORTH,input,0,NORTH,LabeledInput.this);
			putConstraint(SOUTH,input,dimension.height,NORTH,LabeledInput.this);
			putConstraint(WEST,input,dimension.width/2,WEST,LabeledInput.this);
			putConstraint(EAST,input,dimension.width,WEST,LabeledInput.this);
		}
	}
}
