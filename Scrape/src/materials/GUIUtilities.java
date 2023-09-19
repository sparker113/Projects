package materials;

import java.awt.Component;
import java.awt.Container;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

public class GUIUtilities {
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

	private static Component getMenuItemByName(JMenu parentComp, String name) {
		Component comp = null;
		for (Component c : parentComp.getMenuComponents()) {
			comp = getComponentByNameBOS((JComponent) c, name);
			if (comp != null && comp.getName() != null && comp.getName().equals(name)) {
				return comp;
			}
		}
		return null;
	}

	public static Component getComponentByName(JComponent parentComp, String name) {
		Component comp = null;
		if (parentComp.getClass().getName().contains("JMenu")) {
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
		if (component.getName() != null && component.getName().equals("materials")) {
			System.out.println("Sam");
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

	public static void updateJMenuItemText(JMenuItem menuItem, String text) {
		SwingWorker<Void, String> worker = new SwingWorker<>() {
			public Void doInBackground() {
				publish(text);
				return null;
			}

			public void process(List<String> chunks) {
				menuItem.setText(chunks.get(0));
			}
		};
		worker.execute();
	}
}
