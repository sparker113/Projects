package exceltransfer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import login.UserNamePassword;

public class GUIUtilities {
	public class SimpleJFrame extends JFrame{
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		Rectangle rectangle;
		String title;
		Color color;
		JPanel[] panels;
		SimpleJFrame(Rectangle rectangle,String title,Color color,JPanel...panels){
			this.rectangle = rectangle;
			this.title = title;
			this.color = color;
			this.panels = panels;
			getSimpleFrame(rectangle,title,color,panels);
		}
		public void applyAction() {

		}
		public void getSimpleFrame(Rectangle rectangle, String title, Color color, JPanel... panels) {
			setBounds(rectangle);
			setTitle(title);
			setBackground(color);
			setIconImage(new ImageIcon(System.getProperty(UserNamePassword.IMAGE_PROPERTY)).getImage()
					.getScaledInstance(64, 64, Image.SCALE_SMOOTH));
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			int y = 0;
			setLayout(null);
			for (JPanel panel : panels) {
				panel.setBounds(0,y,panel.getBounds().width,panel.getBounds().height);
				add(panel);
				y+=panel.getBounds().height;
			}
			setVisible(true);
		}

	}
	public static int getCenterX(int width) {
		return Toolkit.getDefaultToolkit().getScreenSize().width / 2 - width / 2;
	}
	public static int getCenterY(int height) {
		return Toolkit.getDefaultToolkit().getScreenSize().height / 2 - height / 2;
	}
	public static Component getComponentByName(Container parentComp, String name) {
		Component comp = null;

		for (Component c : parentComp.getComponents()) {
			comp = getComponentByNameBOS((JComponent) c, name);
			if (comp != null && comp.getName() != null && comp.getName().equals(name)) {
				return comp;
			}
			/*
			 * if((comp=getComponentByNameBOS((JComponent)c,name))!=null) { return comp; }
			 */
		}
		return null;
	}
	public static Component getComponentByName(JComponent parentComp, String name) {
		Component comp = null;
		if (parentComp.getClass().getName().equals("javax.swing.JMenu")) {
			System.out.println(parentComp.getClass().getName());
			return getMenuItemByName((JMenu) parentComp, name);
		}
		for (Component c : parentComp.getComponents()) {
			comp = getComponentByNameBOS((JComponent) c, name);
			if (comp != null && comp.getName() != null && comp.getName().equals(name)) {
				return comp;
			}
			/*
			 * if((comp=getComponentByNameBOS((JComponent)c,name))!=null) { return comp; }
			 */
		}
		return null;
	}
	private static Component getComponentByNameBOS(JComponent component, String name) {
		Component comp = null;
		if (component.getName() != null && component.getName().equals(name)) {
			return component;
		}

		for (Component c : component.getComponents()) {
			if (c.getName() != null && c.getName().equals(name)) {
				return c;
			}
			if (c.getClass().getSimpleName().equals("CellRendererPane")) {
				comp = getComponentByName((Container) c, name);
			} else {
				comp = getComponentByNameBOS((JComponent) c, name);
			}
			if (comp != null) {
				return comp;
			}
		}
		return comp;
	}

	public static JMenu getMenuInMenuBar(JMenuBar menuBar, String name) {
		for (int i = 0; i < menuBar.getMenuCount(); i++) {
			if (menuBar.getMenu(i)==null||menuBar.getMenu(i).getName() == null) {
				continue;
			}

			if (menuBar.getMenu(i).getName().equals(name)) {
				return menuBar.getMenu(i);
			}
		}
		return null;
	}
	private static Component getMenuItemByName(JMenu parentComp, String name) {
		if (parentComp.getName() != null && parentComp.getName().equals(name)) {
			return parentComp;
		}
		Component comp = null;
		for (Component c : parentComp.getMenuComponents()) {
			if (c.getClass().getName().equals("javax.swing.JMenu")) {
				comp = getMenuItemByName((JMenu) c, name);
				if (comp != null) {
					return comp;
				}
			}
			comp = getComponentByNameBOS((JComponent) c, name);
			if (comp != null && comp.getName() != null && comp.getName().equals(name)) {
				return comp;
			}
		}
		return comp;
	}


	public static int getScreenHeight() {
		return Toolkit.getDefaultToolkit().getScreenSize().height;
	}

	public static int getScreenWidth() {
		return Toolkit.getDefaultToolkit().getScreenSize().width;
	}

	public static JPanel getSimpleButtonPanel(Dimension dim,Color color,String text,String name) {
		JPanel panel = new JPanel();
		panel.setBackground(color);
		JButton button = new JButton();
		button.setText(text);
		button.setName(name);
		button.setSize(dim);
		button.setVisible(true);
		panel.add(button);
		panel.setVisible(true);
		return panel;
	}

	public static JPanel getSimpleButtonPanel(Dimension dim,Color color,String text,String name,AbstractAction action) {
		JPanel panel = new JPanel();
		panel.setBackground(color);
		JButton button = new JButton();
		button.setText(text);
		button.setName(name);
		button.addActionListener(action);
		button.setSize(dim);
		button.setVisible(true);
		panel.add(button);
		panel.setVisible(true);
		return panel;
	}

	public static JPanel getSimpleFramePane(Rectangle rectangle, Color color) {
		JPanel panel = new JPanel();
		panel.setBackground(color);
		panel.setBounds(rectangle);
		panel.setVisible(true);
		return panel;
	}

	public static void updateJMenuItemText(JMenuItem menuItem, String text) {
		SwingWorker<Void, String> worker = new SwingWorker<>() {
			public Void doInBackground() {
				publish(text);
				return null;
			}

			public void done() {
				System.out.println("Done");
			}

			public void process(List<String> chunks) {
				for (String s : chunks) {
					System.out.println(s);
					menuItem.setText(s);
				}
			}
		};
		worker.execute();
	}

	public static void updateTextField(JTextField textField, String text) {
		SwingWorker<Void, String> worker = new SwingWorker<>() {
			public Void doInBackground() {
				publish(text);
				return null;
			}

			public void process(List<String> chunks) {
				textField.setText(chunks.get(0));
			}
		};
		worker.execute();
	}
}
