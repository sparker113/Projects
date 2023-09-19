package materials;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;

public class SandSilosFrame extends JFrame {
	SandTicketsObject sandTicketsObject;
	private static final int PADDING = 20;
	private static final int HEIGHT = 600 + PADDING * 3;
	private static final int WIDTH = 450 + PADDING * 3;
	private final int SILOHEIGHT = 300;
	private final int SILOWIDTH = 132;

	public SandSilosFrame(SandTicketsObject sandTicketsObject) {
		this.sandTicketsObject = sandTicketsObject;
		nittyGritty();
	}

	private void nittyGritty() {
		setBounds(getXForCenter(), getYForCenter(75), WIDTH, HEIGHT + 75);
		getContentPane().setBackground(Color.DARK_GRAY);
		constructButton();
		constructScrollPane();
		setLayout(new SilosFrameLayout());
		setTitle("Silos - " + sandTicketsObject.padName);
		this.setIconImage(
				new ImageIcon("C:\\Scrape\\Scrape.png").getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	private void constructScrollPane() {
		JScrollPane scrollPane = new JScrollPane(new SilosPanel());
		scrollPane.setName("scrollPane");
		getContentPane().add(scrollPane);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	}

	private void addOrderNumberToMap(SandSilo sandSilo, HashMap<String, Integer> orderMap) {
		String orderText = ((JTextField) GUIUtilities.getComponentByName(sandSilo, "order")).getText();
		if (orderText.equals("")) {
			orderText = "1";
		}
		orderMap.put(sandSilo.getName(), Integer.valueOf(orderText));
	}

	private String addSandTypeToMap(String silo, HashMap<String, HashMap<String, Integer>> orderMap) {
		String sandType = sandTicketsObject.getSiloSandType(silo);
		if (orderMap.containsKey(sandType)) {
			return sandType;
		}
		orderMap.put(sandType, new HashMap<>());
		return sandType;
	}

	private HashMap<String, HashMap<String, Integer>> getSiloOrderMap() {
		HashMap<String, HashMap<String, Integer>> siloOrder = new HashMap<>();
		for (String s : sandTicketsObject.silosCurrentMap.keySet()) {
			SandSilo sandSilo = (SandSilo) GUIUtilities.getComponentByName(this, s);
			String sandType = addSandTypeToMap(s, siloOrder);
			addOrderNumberToMap(sandSilo, siloOrder.get(sandType));
		}
		return siloOrder;
	}

	public HashMap<String, ArrayList<String>> getSiloOrder() {
		HashMap<String, HashMap<String, Integer>> siloOrderMap = getSiloOrderMap();
		HashMap<String, ArrayList<String>> siloOrders = new HashMap<>();
		for (String s : siloOrderMap.keySet()) {
			siloOrders.put(s, new ArrayList<>());
			for (String silo : siloOrderMap.get(s).keySet()) {
				if (siloOrderMap.get(s).get(silo) >= siloOrders.get(s).size()) {
					siloOrders.get(s).add(silo);
					continue;
				}
				siloOrders.get(s).add(siloOrderMap.get(s).get(silo) - 1, silo);
			}
		}
		return siloOrders;
	}

	private void constructButton() {
		JButton button = new JButton();
		button.setName("saveButton");
		button.setText("Save");
		button.setVisible(true);
		button.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (String s : sandTicketsObject.silosCurrentMap.keySet()) {
					SandSilo sandSilo = (SandSilo) getComponentByName(s);
					String percent = ((JTextField) sandSilo.getComponentByName("siloPercent")).getText();
					sandTicketsObject.silosPercentsMap.put(s, checkTextField(percent));
				}
				sandTicketsObject.setSiloOrder(getSiloOrder());
				try {
					sandTicketsObject.writeToFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				dispose();
			}
		});
		getContentPane().add(button);
	}

	private Component getComponentByName(String name) {
		Component comp = null;
		for (Component c : getComponents()) {
			comp = getComponentByNameBOS((JComponent) c, name);
			if ((comp != null && comp.getName() != null && comp.getName().equals(name)) || ((comp = getComponentByNameBOS((JComponent) c, name)) != null)) {
				return comp;
			}
		}
		return null;
	}

	private Component getComponentByNameBOS(JComponent component, String name) {
		Component comp = null;
		if (component.getName() != null && component.getName().equals(name)) {
			return component;
		}
		for (Component c : component.getComponents()) {
			if (c.getName() != null && c.getName().equals(name)) {
				return c;
			}
			comp = getComponentByNameBOS((JComponent) c, name);
			if (comp != null) {
				return comp;
			}
		}
		return null;
	}

	private Double checkTextField(String percent) {
		Matcher matcher = Pattern.compile("[\\d\\.]+").matcher(percent);
		if (matcher.find()) {
			return Double.valueOf(matcher.group());
		}
		return 0.0;
	}

	private String getSandSiloType(String sandType) {
		Matcher matcher = Pattern.compile("\\d+").matcher(sandType);
		if (matcher.find()) {
			String mesh = matcher.group();
			return getTypeFromMesh(mesh);
		}
		return sandType;
	}

	private String getTypeFromMesh(String mesh) {
		switch (mesh) {
		case ("100"):
			return SandSilo.R100;
		case ("40"):
			return SandSilo.R40;
		case ("30"):
			return SandSilo.R30;
		case ("20"):
			return SandSilo.R20;
		case ("200"):
			return SandSilo.R200;
		default:
			return mesh;
		}
	}

	private int getXForCenter() {
		int centerScreen = Toolkit.getDefaultToolkit().getScreenSize().width / 2;
		return centerScreen - (WIDTH / 2);
	}

	private int getYForCenter(int add) {
		int centerScreenY = Toolkit.getDefaultToolkit().getScreenSize().height / 2;
		return centerScreenY - ((HEIGHT + add) / 2);
	}

	private class SilosFrameLayout extends SpringLayout {
		static final int BUTTONWIDTH = 100;
		static final int BUTTONHEIGHT = 25;

		SilosFrameLayout() {
			construct();
		}

		void construct() {
			for (Component c : getContentPane().getComponents()) {
				if (c.getName() == null) {
					continue;
				}
				String name = c.getName();
				switch (name) {
				case ("scrollPane"):
					scrollPanelLayout((JScrollPane) c);
					break;
				case ("saveButton"):
					buttonLayout((JButton) c);
					break;
				}
			}
		}

		void scrollPanelLayout(JScrollPane scrollPane) {
			putConstraint(NORTH, scrollPane, 0, NORTH, SandSilosFrame.this);
			putConstraint(SOUTH, scrollPane, SandSilosFrame.HEIGHT, NORTH, SandSilosFrame.this);
			putConstraint(WEST, scrollPane, 0, WEST, SandSilosFrame.this);
			putConstraint(EAST, scrollPane, SandSilosFrame.WIDTH, WEST, SandSilosFrame.this);
		}

		void buttonLayout(JButton button) {
			putConstraint(NORTH, button, getButtonNorth(), NORTH, SandSilosFrame.this);
			putConstraint(SOUTH, button, getButtonNorth() + BUTTONHEIGHT, NORTH, SandSilosFrame.this);
			putConstraint(WEST, button, getButtonWest(), WEST, SandSilosFrame.this);
			putConstraint(EAST, button, getButtonWest() + BUTTONWIDTH, WEST, SandSilosFrame.this);
		}

		int getButtonWest() {
			return (SandSilosFrame.WIDTH / 2) - (BUTTONWIDTH / 2);
		}

		int getButtonNorth() {
			return SandSilosFrame.HEIGHT + 10;
		}
	}

	private class SilosPanel extends JPanel {
		SilosPanel() {
			addSilos();
			nittyGritty();
		}

		void nittyGritty() {
			setName("silosPanel");
			setBackground(Color.getHSBColor(-.85f, .1f, .85f));
			setBounds(getSilosBounds());
			setLayout(new SilosPanelLayout(this));
			setVisible(true);
		}

		void addSilos() {
			for (String s : sandTicketsObject.silosCurrentMap.keySet()) {
				add(new SandSilo(s, sandTicketsObject.silosCurrentMap.get(s),
						sandTicketsObject.silosPercentsMap.containsKey(s) ? sandTicketsObject.silosPercentsMap.get(s)
								: 0.0,
						sandTicketsObject.getSiloPriority(s), getSandSiloType(sandTicketsObject.getSandType(s)),
						SILOHEIGHT));
			}
		}

		Rectangle getSilosBounds() {
			int width = (sandTicketsObject.silosCurrentMap.size() / 2) * 100
					+ PADDING * ((sandTicketsObject.silosCurrentMap.size() / 2) + 1);
			return new Rectangle(0, 0, width, HEIGHT);
		}

		int getSilosPerRow() {
			return sandTicketsObject.silosCurrentMap.size() % 2 == 0 ? sandTicketsObject.silosCurrentMap.size() / 2
					: sandTicketsObject.silosCurrentMap.size() / 2 + 1;
		}

		private class SilosPanelLayout extends SpringLayout {
			SilosPanel silosPanel;

			SilosPanelLayout(SilosPanel silosPanel) {
				this.silosPanel = silosPanel;
				construct();
			}

			void construct() {
				int silosPerRow = getSilosPerRow();
				int column = 0;
				int row = 0;
				int count = 0;
				for (Component c : silosPanel.getComponents()) {
					putConstraint(NORTH, c, getNorth(row), NORTH, silosPanel);
					putConstraint(SOUTH, c, getNorth(row) + SILOHEIGHT, NORTH, silosPanel);
					putConstraint(WEST, c, getWest(column), WEST, silosPanel);
					putConstraint(EAST, c, getWest(column) + SILOWIDTH, WEST, silosPanel);
					count++;
					row = count / silosPerRow;
					column = count % silosPerRow;
				}
			}

			int getNorth(int row) {
				return PADDING + row * (PADDING + SILOHEIGHT);
			}

			int getWest(int column) {
				return PADDING + column * (PADDING + SILOWIDTH);
			}
		}
	}
}
