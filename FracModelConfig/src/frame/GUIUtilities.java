package frame;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;

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

	public static int getScreenWidth() {
		return Toolkit.getDefaultToolkit().getScreenSize().width;
	}

	public static int getScreenHeight() {
		return Toolkit.getDefaultToolkit().getScreenSize().height;
	}

	public static Container getHighestParent(Component c) {
		Container parent = c.getParent();
		Container temp = parent;
		while (temp != null) {
			parent = temp;
		}
		return parent;
	}

	// Get rectangle centered in screen with padding specified by percPad for
	// percent of screen width/height on each side
	public static Rectangle getCenterRectangle(float percPad) {
		int x = (int) ((getScreenWidth()) * percPad);
		int width = getScreenWidth() - (x * 2);
		int y = (int) ((getScreenHeight()) * percPad);
		int height = getScreenHeight() - (y * 2);
		return new Rectangle(x, y, width, height);
	}

	public static final String BUTTON_PANEL = "button_panel";

	public static JPanel getSimpleButtonPanel(Dimension dim, Color color, String text, String name,
			AbstractAction action) {
		JPanel panel = new JPanel();
		panel.setBackground(color);
		JButton button = new JButton();
		button.setText(text);
		button.setName(BUTTON_NAME);
		button.addActionListener(action);
		// button.setBounds(dim.width/4,0,dim.width/2,dim.height);
		// button.setVisible(true);
		panel.setName(name);
		panel.add(button);
		panel.setVisible(true);
		return panel;
	}

	public static JPanel getSimpleButtonPanel(Color color, String text, String name, AbstractAction action) {
		JPanel panel = new JPanel();
		panel.setBackground(color);
		JButton button = new JButton();
		button.setText(text);
		button.setName(BUTTON_NAME);
		button.addActionListener(action);
		// button.setBounds(dim.width/4,0,dim.width/2,dim.height);
		// button.setVisible(true);
		panel.setName(name);
		panel.add(button);
		panel.setVisible(true);
		return panel;
	}

	public static void setAllCompsVisible(JFrame frame) {
		for (Component c : frame.getComponents()) {
			c.setVisible(true);
		}
	}

	public static JPanel getSimpleButtonPanel(Dimension dim, Color color, String text, String name) {
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

	public static JPanel getSimpleTextPanel(Dimension dim, Color color, String text, String name) {
		JPanel panel = new JPanel();
		panel.setBackground(color);
		JLabel label = new JLabel();
		label.setBackground(color);
		label.setText(text);
		label.setEnabled(true);
		label.setVisible(true);
		panel.add(label);
		panel.setVisible(true);
		return panel;
	}

	public final static String BUTTON_NAME = "button";

	public static int getCenterX(int width) {
		return Toolkit.getDefaultToolkit().getScreenSize().width / 2 - width / 2;
	}

	public static int getCenterY(int height) {
		return Toolkit.getDefaultToolkit().getScreenSize().height / 2 - height / 2;
	}

	public static class SimpleJFrame extends JFrame {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		Rectangle rectangle;
		String title;
		Color color;
		JPanel[] panels;

		SimpleJFrame(Rectangle rectangle, String title, Color color, JPanel... panels) {
			this.rectangle = rectangle;
			this.title = title;
			this.color = color;
			this.panels = panels;
			getSimpleFrame(rectangle, title, color, panels);
		}

		public void applyAction() {

		}

		public void getSimpleFrame(Rectangle rectangle, String title, Color color, JPanel... panels) {
			setBounds(rectangle);
			setTitle(title);
			setBackground(color);
			/*
			 * setIconImage(new
			 * ImageIcon(System.getProperty(UserNamePassword.IMAGE_PROPERTY)).getImage()
			 * .getScaledInstance(64, 64, Image.SCALE_SMOOTH));
			 */
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			int y = 0;
			setLayout(null);
			for (JPanel panel : panels) {
				panel.setBounds(0, y, panel.getBounds().width, panel.getBounds().height);
				add(panel);
				y += panel.getBounds().height;
			}
			setAllCompsVisible(this);
			setVisible(true);
		}

		public void getSimpleFrame(Rectangle rectangle, String title, Color color, Object... panels) {
			setBounds(rectangle);
			setTitle(title);
			setBackground(color);
			/*
			 * setIconImage(new
			 * ImageIcon(System.getProperty(UserNamePassword.IMAGE_PROPERTY)).getImage()
			 * .getScaledInstance(64, 64, Image.SCALE_SMOOTH));
			 */
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			int y = 0;
			setLayout(null);
			for (Object p : panels) {
				JPanel panel = (JPanel) p;
				panel.setBounds(0, y, panel.getBounds().width, panel.getBounds().height);
				add(panel);
				y += panel.getBounds().height;
			}

		}

		public void setPanels(Object[] panelObjs) {
			JPanel[] panels = new JPanel[panelObjs.length];
			int i = 0;
			for (Object o : panelObjs) {
				panels[i] = (JPanel) o;
			}
			this.panels = panels;
		}

	}

	public static JPanel getSimpleFramePane(Rectangle rectangle, Color color) {
		JPanel panel = new JPanel();
		panel.setBackground(color);
		panel.setBounds(rectangle);
		panel.setVisible(true);
		return panel;
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

	public static JMenu getMenuInMenuBar(JMenuBar menuBar, String name) {
		for (int i = 0; i < menuBar.getMenuCount(); i++) {
			if (menuBar.getMenu(i) == null || menuBar.getMenu(i).getName() == null) {
				continue;
			}

			if (menuBar.getMenu(i).getName().equals(name)) {
				return menuBar.getMenu(i);
			}
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

	public static void updateTextField(JTextField textField, String text) {
		SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
			@Override
			public Void doInBackground() {
				publish(text);
				return null;
			}

			@Override
			public void process(List<String> chunks) {
				textField.setText(chunks.get(0));
			}
		};
		worker.execute();
	}

	public static void updateJMenuItemText(JMenuItem menuItem, String text) {
		SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
			@Override
			public Void doInBackground() {
				publish(text);
				return null;
			}

			@Override
			public void process(List<String> chunks) {
				for (String s : chunks) {
					System.out.println(s);
					menuItem.setText(s);
				}
			}

			@Override
			public void done() {
				System.out.println("Done");
			}
		};
		worker.execute();
	}

	public static Rectangle getCenterRectangle(float hPad, float vPad) {
		return new Rectangle(getCenterX(hPad), getCenterY(vPad), getCenterWidth(hPad), getCenterHeight(vPad));
	}

	private static int getCenterX(float hPad) {
		int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
		int width = (int) ((1f - (2f * hPad)) * screenWidth);
		int x = (screenWidth - width) / 2;
		return x;
	}

	private static int getCenterY(float vPad) {
		int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
		int height = (int) ((1f - (2f * vPad)) * screenHeight);
		int y = (screenHeight - height) / 2;
		return y;
	}

	private static int getCenterWidth(float hPad) {
		int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
		int width = (int) ((1f - (2f * hPad)) * screenWidth);
		return width;
	}

	private static int getCenterHeight(float vPad) {
		int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
		int height = (int) ((1f - (2f * vPad)) * screenHeight);
		return height;
	}

	public static JDialog getDialog(String buttonText, String labelText, Set<String> set,
			CompletableFuture<List<String>> selectedValues) {
		JDialog dialog = new JDialog();

		dialog.setLayout(getDialogLayout(dialog));
		dialog.setBounds(getCenterRectangle(.4f, .35f));
		dialog.getContentPane().setBackground(DEFAULT_COLOR);

		dialog.add(getDialogMessageLabel(labelText));
		dialog.add(getDialogListScrollPane(set));
		dialog.add(getDialogButtonPanel(dialog, selectedValues));
		dialog.setUndecorated(true);
		dialog.setVisible(true);
		return dialog;
	}

	public final static float DIALOG_HORIZ_PAD = .4f;
	public final static float DIALOG_VERT_PAD = .35f;

	public static CompletableFuture<List<String>> getListSelectionDialog(String labelText, Set<String> set) {
		JDialog dialog = new JDialog();

		dialog.setLayout(getDialogLayout(dialog));
		dialog.setBounds(getDialogBounds());
		dialog.getContentPane().setBackground(DEFAULT_COLOR);

		dialog.add(getDialogMessageLabel(labelText));
		dialog.add(getDialogListScrollPane(set));

		CompletableFuture<List<String>> selectedValues = new CompletableFuture<>();

		dialog.add(getDialogButtonPanel(dialog, selectedValues));
		dialog.setUndecorated(true);
		dialog.setVisible(true);
		return selectedValues;
	}

	private static Rectangle getDialogBounds() {
		return getCenterRectangle(DIALOG_HORIZ_PAD, DIALOG_VERT_PAD);
	}

	private static LayoutManager getDialogLayout(JDialog dialog) {
		LayoutManager layout = new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS);
		return layout;
	}

	public final static Color DEFAULT_COLOR = Color.BLACK;

	public final static String DIALOG_MESSAGE_LABEL = "dialog_message";

	private final static float LABEL_VERT = .15f;

	private static JLabel getDialogMessageLabel(String text) {
		JLabel label = new JLabel();
		label.setName(DIALOG_MESSAGE_LABEL);
		label.setText(text);
		label.setBackground(Color.black);
		label.setForeground(Color.white);

		Rectangle dialogBounds = getDialogBounds();

		label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		label.setSize(new Dimension(dialogBounds.width, (int) (dialogBounds.height * LABEL_VERT)));
		label.setOpaque(true);
		label.setVisible(true);
		return label;
	}

	public final static String DIALOG_LIST_SCROLL_PANE = "dialog_scroll_pane";
	public final static float DIALOG_SCROLL_PANE_VERT = .7f;

	private static JScrollPane getDialogListScrollPane(Collection<String> selectionList) {
		JScrollPane scrollPane = new JScrollPane(getList(selectionList));
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setName(DIALOG_LIST_SCROLL_PANE);

		Rectangle dialogBounds = getDialogBounds();
		scrollPane.setSize(new Dimension(dialogBounds.width, (int) (dialogBounds.height * DIALOG_SCROLL_PANE_VERT)));

		scrollPane.setVisible(true);
		return scrollPane;
	}

	public final static String DIALOG_LIST_COMP = "dialog_j_list";

	private static JList<String> getList(Collection<String> options) {
		JList<String> list = new JList<>(getStringArr(options.toArray()));
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.setDragEnabled(true);
		list.setName(DIALOG_LIST_COMP);
		((JLabel) list.getCellRenderer()).setAlignmentX(JLabel.CENTER_ALIGNMENT);

		Rectangle dialogBounds = getDialogBounds();
		list.setSize(dialogBounds.width, (int) (dialogBounds.height * DIALOG_SCROLL_PANE_VERT));
		return list;
	}

	public final static String DIALOG_BUTTON_PANEL = "dialog_button_panel";
	private final static float DIALOG_BUTTON_PANEL_VERT = .15f;

	private static JPanel getDialogButtonPanel(JDialog dialog, CompletableFuture<List<String>> selectedValues) {
		JPanel panel = new JPanel();
		panel.setName(DIALOG_BUTTON_PANEL);

		Rectangle dialogBounds = getDialogBounds();
		panel.setSize(new Dimension(dialogBounds.width, (int) (dialogBounds.height * DIALOG_BUTTON_PANEL_VERT)));
		panel.add(getDialogButton(dialog, panel.getSize(), selectedValues));
		panel.setBackground(DEFAULT_COLOR);
		return panel;
	}

	public final static String DIALOG_BUTTON = "dialog_button";

	// Percent of button panel's height
	private final static float DIALOG_BUTTON_VERT = .5f;
	private final static float DIALOG_BUTTON_HORIZ = .5f;

	private static JButton getDialogButton(JDialog dialog, Dimension panelDim,
			CompletableFuture<List<String>> selectedValues) {
		JButton button = new JButton();
		button.setText("Add Selections");
		button.setName(DIALOG_BUTTON);
		button.setBounds(getCenterRectangle(DIALOG_BUTTON_HORIZ / 2, DIALOG_BUTTON_VERT / 2));
		button.setAlignmentX(JButton.CENTER_ALIGNMENT);
		button.addActionListener(new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent e) {
				selectedValues.complete(
						((JList<String>) getComponentByName(dialog, DIALOG_LIST_COMP)).getSelectedValuesList());
				dialog.dispose();
			}
		});
		button.setVisible(true);
		return button;
	}

	public static String[] getStringArr(Object[] obj) {
		String[] arr = new String[obj.length];
		int i = 0;
		for (Object o : obj) {
			arr[i] = (String) o;
			i++;
		}
		return arr;
	}

	private final static float MULTI_INPUT_VERT = .3f;
	private final static float MULTI_INPUT_HORIZ = .4f;

	public static CompletableFuture<List<String>> getMultiInputDialog(String title,String labelDescrText) {
		JDialog dialog = new JDialog();
		Rectangle dialogBounds = getCenterRectangle(MULTI_INPUT_HORIZ, MULTI_INPUT_VERT);
		dialog.setTitle(title);
		dialog.setLayout(new BoxLayout(dialog.getContentPane(),BoxLayout.Y_AXIS));
		dialog.setResizable(false);
		dialog.setBounds(dialogBounds);
		CompletableFuture<List<String>> inputFuture = new CompletableFuture<>();
		dialog.add(getMultiInputButtonPanel(dialog,dialogBounds,labelDescrText,inputFuture));
		dialog.add(getMultiInputScrollPane(dialogBounds));
		dialog.setVisible(true);
		return inputFuture;
	}

	public final static String MULTI_INPUT_SCROLL_PANE = "multi_input_scroll_pane";
	private final static float MULTI_INPUT_SCROLL_VERT = 0.2f;

	private static JScrollPane getMultiInputScrollPane(Rectangle dialogBounds) {
		JScrollPane scrollPane = new JScrollPane(getInsetScrollPanel(dialogBounds));
		scrollPane.setName(MULTI_INPUT_SCROLL_PANE);
		//scrollPane.setLayout(new ScrollPaneLayout());
		//scrollPane.setBounds(getMultiInputScrollBounds(dialogBounds));
		scrollPane.setPreferredSize(getMultiInputScrollSize(dialogBounds));
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setWheelScrollingEnabled(true);
		scrollPane.setVisible(true);
		return scrollPane;
	}
	private static Dimension getMultiInputScrollSize(Rectangle dialogBounds) {
		return new Dimension(dialogBounds.width,
				(int) (dialogBounds.height * (1f - MULTI_INPUT_SCROLL_VERT)));
	}

	public final static String MULTI_INPUT_PANEL = "inset_scroll_panel";

	private static JPanel getInsetScrollPanel(Rectangle dialogBounds) {
		JPanel panel = new JPanel();
		panel.setName(MULTI_INPUT_PANEL);
		panel.setBounds(0, 0, dialogBounds.width, dialogBounds.height * 4);
		panel.setBackground(Color.black);
		panel.setLayout(null);
		return panel;
	}
	private final static String MULTI_INPUT_BUTTON_PANEL = "multi_input_button_panel";
	private static JPanel getMultiInputButtonPanel(JDialog dialog,Rectangle dialogBounds,String labelDescrText,CompletableFuture<List<String>> inputFuture) {
		JPanel panel = new JPanel();
		panel.setName(MULTI_INPUT_BUTTON_PANEL);
		panel.setBounds(0,0,dialogBounds.width,(int)(dialogBounds.height*MULTI_INPUT_SCROLL_VERT));
		panel.add(getMultiInputAddButton(dialog,labelDescrText));
		panel.add(getMultiInputSaveButton(dialog,inputFuture));
		panel.setBackground(Color.LIGHT_GRAY);
		panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		panel.setVisible(true);
		return panel;
	}
	
	
	private final static String MULTI_INPUT_ADD_BUTTON = "multi_input_add_panel";
	
	private final static int INPUT_PANEL_HEIGHT = 35;
	private static JButton getMultiInputAddButton(JDialog dialog,String labelDescrText) {
		JButton button = new JButton();
		button.setName(MULTI_INPUT_ADD_BUTTON);
		button.addActionListener(new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				Executors.newSingleThreadExecutor().execute(()->{
					JPanel panel = (JPanel)getComponentByName(dialog,MULTI_INPUT_PANEL);
					Rectangle bounds = panel.getBounds();
					String labelText = getNextInputLabelString(panel,labelDescrText);
					int labelIndex = getIndexOfNextInputLabel(panel);
					panel.add(new InputPanel(new Rectangle(0,getYPosOfInputPanel(labelIndex),bounds.width,INPUT_PANEL_HEIGHT),labelText,labelText));
					panel.setPreferredSize(new Dimension(bounds.width,bounds.height+INPUT_PANEL_HEIGHT));
					System.out.println(panel.getBounds());
					panel.revalidate();
					panel.repaint();
					/*
					 * if(labelIndex%2==0) { updateMultiInputScrollPane(dialog); } panel.repaint();
					 */
					//panel.setLayout(new GridLayout(0,1));
				});
			}
		});
		button.setAlignmentX(JButton.CENTER_ALIGNMENT);
		button.setAlignmentY(JButton.CENTER_ALIGNMENT);
		button.setText("Add");
		button.setVisible(true);
		return button;
	}
	private final static String MULTI_INPUT_SAVE_BUTTON = "multi_input_save_button";
	
	private static JButton getMultiInputSaveButton(JDialog dialog,CompletableFuture<List<String>> inputFuture) {
		JButton button = new JButton();
		button.setName(MULTI_INPUT_SAVE_BUTTON);
		button.addActionListener(new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				Executors.newSingleThreadExecutor().execute(()->{
					JPanel panel = (JPanel)getComponentByName(dialog,MULTI_INPUT_PANEL);
					completeMultiInputFuture(panel,inputFuture);
					dialog.dispose();
				});
			}
		});
		button.setAlignmentX(JButton.CENTER_ALIGNMENT);
		button.setAlignmentY(JButton.CENTER_ALIGNMENT);
		button.setText("Save");
		button.setVisible(true);
		
		
		return button;
	}
	private static void completeMultiInputFuture(JPanel panel,CompletableFuture<List<String>> inputFuture) {
		List<String> list = new ArrayList<>();
		for(Component c:panel.getComponents()) {
			Class<?> clazz = c.getClass();
			if(clazz.getSimpleName().equals("InputPanel")) {
				String input = ((InputPanel)c).getInput();
				list.add(input);
			}
		}
		inputFuture.complete(list);
	}
	private static int getYPosOfInputPanel(int labelIndex) {
		return INPUT_PANEL_HEIGHT*(labelIndex-1);
	}
	private static String getNextInputLabelString(JPanel insetPanel,String labelDescrText) {
		int numLabels = getIndexOfNextInputLabel(insetPanel);
		return (labelDescrText+"_"+numLabels);
	}
	private static int getIndexOfNextInputLabel(JPanel insetPanel) {
		int numLabels = 0;
		for(Component c:insetPanel.getComponents()) {
			Class<?> clazz = c.getClass();
			if(clazz.getSimpleName().equals("InputPanel")) {
				numLabels++;
			}
		}
		numLabels++;
		return numLabels;
	}


}
