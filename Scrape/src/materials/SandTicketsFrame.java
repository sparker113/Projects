package materials;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

public class SandTicketsFrame extends JFrame {
	Rectangle rectangle;
	private String padName;
	private SandTicketsObject sandTicketsObject;
	private TicketsTable ticketsTable;
	private final Color color = Color.getHSBColor(-.85f, .1f, .85f);

	public SandTicketsFrame(Rectangle rectangle, String padName) throws IOException, ClassNotFoundException {
		this.rectangle = rectangle;
		this.padName = padName;
		this.sandTicketsObject = getSandTicketsObject(padName);
		construct();
		nittyGritty();
	}

	public SandTicketsFrame(Rectangle rectangle, SandTicketsObject sandTicketsObject) {
		this.rectangle = rectangle;
		this.sandTicketsObject = sandTicketsObject;
		this.padName = sandTicketsObject.padName;
		construct();
		nittyGritty();
	}

	public SandTicketsFrame(int x, int y, int width, int height, String padName)
			throws IOException, ClassNotFoundException {
		this.rectangle = new Rectangle(x, y, width, height);
		this.padName = padName;
		this.sandTicketsObject = getSandTicketsObject(padName);
		construct();
		nittyGritty();
	}

	private void construct() {
		add(constructScrollPane());
		add(new Panels());
	}

	public static Rectangle getTicketsFrameBounds() {
		return new Rectangle(150, 150, 1200, 450);
	}

	private JScrollPane constructScrollPane() {
		ticketsTable = new TicketsTable(2000);
		JScrollPane scrollPane = new JScrollPane(ticketsTable);
		scrollPane.setName("scroll");
		scrollPane.setVisible(true);
		return scrollPane;
	}

	private SandTicketsObject getSandTicketsObject(String padName) throws IOException, ClassNotFoundException {
		SandTicketsObject sandTicketsObject = SandTicketsObject.readFromFile(padName);
		return sandTicketsObject;
	}

	private void nittyGritty() {
		setBounds(rectangle);
		setIconImage(new ImageIcon("C:\\Scrape\\Scrape.png").getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
		setTitle("Sand Tickets - " + padName);
		getContentPane().setBackground(color);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLayout(new TicketsFrameLayout());
		setVisible(true);
	}

	private Component breadthOfSearchComp(JComponent component, String name) {
		Component comp = null;
		for (Component c : component.getComponents()) {
			if (comp != null) {
				return comp;
			}
			if (c.getName() == null || !c.getName().equals(name)) {
				comp = breadthOfSearchComp((JComponent) c, name);
				continue;
			}
			return c;
		}
		return null;
	}

	public static HashMap<Integer, String> getTableNameColumns() {
		HashMap<Integer, String> map = new HashMap<>();
		map.put(SILO, SandTicketsObject.SILO);
		map.put(TYPE, SandTicketsObject.TYPE);
		map.put(TRUCK, SandTicketsObject.TRUCK);
		map.put(WEIGHT, SandTicketsObject.WEIGHT);
		map.put(BOL, SandTicketsObject.BOL);
		map.put(TRUCKING, SandTicketsObject.TRUCKING);
		map.put(PO, SandTicketsObject.PO);
		map.put(DATE, SandTicketsObject.DATE);
		map.put(TIME, SandTicketsObject.TIME);
		map.put(SHIPPER, SandTicketsObject.SHIPPER);
		return map;
	}

	public class TicketsTable extends JTable {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private static final int COLUMNS = 10;

		TicketsTable(int rows) {
			super(rows, COLUMNS);
			nittyGritty();
		}

		void nittyGritty() {
			setName("Table");
			new TableKeyPressed(this);
			this.setCellSelectionEnabled(true);
			formatColumns();
			setAutoResizeMode(AUTO_RESIZE_OFF);
		}

		void formatColumns() {
			HashMap<Integer, String> headers = getTableNameColumns();
			for (int i = 0; i < headers.size(); i++) {
				System.out.println(getPrefferedWidth(headers.size()));
				getColumnModel().getColumn(i).setPreferredWidth(getPrefferedWidth(headers.size()));
				getColumnModel().getColumn(i).setHeaderValue(headers.get(i));
				addComboBox(headers, i);
			}
		}

		void addComboBox(HashMap<Integer, String> headers, int i) {
			switch (i) {
			case 0, 1, 2, 4, 9:
				getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(new TicketsComboBox(headers.get(i))));
			}
		}

		void addComboBox() {
			HashMap<Integer, String> headers = getTableNameColumns();
			for (int i = 0; i < headers.size(); i++) {
				switch (i) {
				case 0, 1, 2, 4, 9:
					getColumnModel().getColumn(i)
							.setCellEditor(new DefaultCellEditor(new TicketsComboBox(headers.get(i))));
				}
			}
		}

		void removeComboBox() {
			for (int i = 0; i < getColumnCount(); i++) {
				getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(new JTextField()));
			}
		}

		String getCellValue(int row, int column) {
			if (this.getValueAt(row, column) == null) {
				return "";
			}
			return getValueAt(row, column).toString();
		}

		Boolean isComplete(int row) {
			for (int i = 0; i < getColumnCount(); i++) {
				if (getCellValue(row, i).equals("")) {
					return false;
				}
			}
			return true;
		}

		ArrayList<String> getRowOfData(int row) {
			ArrayList<String> array = new ArrayList<>();
			for (int i = 0; i < getColumnCount(); i++) {
				array.add(getCellValue(row, i));
			}
			return array;
		}

		void fillTable(LinkedHashMap<String, HashMap<String, String>> map) {
			int row = 0;
			for (String s : map.keySet()) {
				setValueAt(map.get(s).get(SandTicketsObject.SILO), row, SILO);
				setValueAt(map.get(s).get(SandTicketsObject.TYPE), row, TYPE);
				setValueAt(map.get(s).get(SandTicketsObject.TRUCKING), row, TRUCKING);
				setValueAt(map.get(s).get(SandTicketsObject.BOL), row, BOL);
				setValueAt(map.get(s).get(SandTicketsObject.WEIGHT), row, WEIGHT);
				setValueAt(map.get(s).get(SandTicketsObject.TRUCK), row, TRUCK);
				setValueAt(map.get(s).get(SandTicketsObject.PO), row, PO);
				setValueAt(map.get(s).get(SandTicketsObject.DATE), row, DATE);
				setValueAt(map.get(s).get(SandTicketsObject.TIME), row, TIME);
				setValueAt(map.get(s).get(SandTicketsObject.SHIPPER), row, SHIPPER);
				row++;
			}
		}

		void updateDate() {
			int row = getSelectedRow();
			setValueAt(LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")), row, DATE);
		}

		void updateDate(int row) {
			setValueAt(LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")), row, DATE);
		}

		void updateTime() {
			int row = getSelectedRow();
			setValueAt(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")), row, TIME);
		}

		void updateTime(int row) {
			setValueAt(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")), row, TIME);
		}

		void updateBOL() {
			int row = getSelectedRow();
			if (getCellValue(row, SHIPPER).equals("") | !getCellValue(row, BOL).equals("")) {
				return;
			}
			setValueAt(sandTicketsObject.getCommonInfoString(getCellValue(row, SHIPPER), BOL, true), row, BOL);
		}

		void updateBOL(String selected, int row) {
			setValueAt(sandTicketsObject.getCommonInfoString(selected, BOL, true), row, BOL);
		}

		void updateTrucking() {
			int row = getSelectedRow();
			if (getCellValue(row, SHIPPER).equals("") | !getCellValue(row, TRUCKING).equals("")) {
				return;
			}
			setValueAt(sandTicketsObject.getLastInfoValue(getCellValue(row, SHIPPER), TRUCKING), row, TRUCKING);
		}

		void updateTrucking(String selected, int row) {
			setValueAt(sandTicketsObject.getLastInfoValue(selected, TRUCKING), row, TRUCKING);
		}

		void updatePO(String selected) {
			int row = getSelectedRow();
			if (getCellValue(row, SHIPPER).equals("") | !getCellValue(row, PO).equals("")) {
				return;
			}
			setValueAt(sandTicketsObject.getCommonInfoString(getCellValue(row, SHIPPER), PO, true), row, PO);
		}

		void updatePO(String selected, int row) {
			setValueAt(sandTicketsObject.getCommonInfoString(getCellValue(row, SHIPPER), PO, true), row, PO);
		}

		void generateTickets(String type, String silo, String facility, int startRow, int numTickets) {
			for (int i = 0; i < numTickets; i++) {
				updateTable(type, silo, facility, startRow + i);
			}
		}

		int findFirstEmptyRow(int searchColumn) {
			if (searchColumn > ticketsTable.getColumnCount() - 1) {
				return -1;
			}
			for (int i = ticketsTable.getRowCount() - 1; i > -1; i--) {
				if (ticketsTable.getValueAt(i, searchColumn) != null
						&& !ticketsTable.getValueAt(i, searchColumn).toString().equals("")) {
					return i + 1;
				}
			}
			return 0;
		}

		void updateTable(String selected) {
			if (getSelectedColumn() == SHIPPER && !getCellValue(getSelectedRow(), SHIPPER).equals("")) {
				updateTime();
				updateDate();
				updateBOL();
				updateTrucking();
				updatePO(selected);
			}
		}

		void updateTable(String type, String silo, String selected, int row) {
			setValueAt(selected, row, SHIPPER);
			setValueAt(silo, row, SILO);
			setValueAt(type, row, TYPE);
			updateTime(row);
			updateDate(row);
			updateBOL(selected, row);
			updateTrucking(selected, row);
			updatePO(selected, row);
		}

		void addRowToMap(LinkedHashMap<String, HashMap<String, String>> map, int row, String bol) {
			ArrayList<String> array = getRowOfData(row);
			map.put(bol, new HashMap<String, String>());
			map.get(bol).put(SandTicketsObject.SILO, array.get(SILO));
			map.get(bol).put(SandTicketsObject.TYPE, array.get(TYPE));
			map.get(bol).put(SandTicketsObject.TRUCKING, array.get(TRUCKING));
			map.get(bol).put(SandTicketsObject.BOL, bol);
			map.get(bol).put(SandTicketsObject.WEIGHT, array.get(WEIGHT));
			map.get(bol).put(SandTicketsObject.TRUCK, array.get(TRUCK));
			map.get(bol).put(SandTicketsObject.PO, array.get(PO));
			map.get(bol).put(SandTicketsObject.DATE, array.get(DATE));
			map.get(bol).put(SandTicketsObject.TIME, array.get(TIME));
			map.get(bol).put(SandTicketsObject.SHIPPER, array.get(SHIPPER));
		}

		LinkedHashMap<String, HashMap<String, String>> getTicketsFromTable() {
			LinkedHashMap<String, HashMap<String, String>> map = new LinkedHashMap<>();
			for (int row = 0; row < ticketsTable.getRowCount(); row++) {
				String bol = getCellValue(row, BOL);
				if (bol.equals("")) {
					break;
				}
				if (!isComplete(row)) {
					JOptionPane.showMessageDialog(null, "Fill in all columns on row " + row);
					return null;
				}
				if (map.containsKey(bol)) {
					JOptionPane.showMessageDialog(null, "Fix the duplicate BOL " + bol + " before saving");
					return null;
				}
				addRowToMap(map, row, bol);
			}
			System.out.println(map);
			return map;
		}

		void clearTable() {
			for (int i = 0; i < getColumnCount(); i++) {
				for (int ii = 0; ii < getRowCount(); ii++) {
					setValueAt("", ii, i);
				}
			}
		}

		int getPrefferedWidth(int numColumns) {
			return rectangle.width / numColumns;
		}
	}

	class TicketsFrameLayout extends SpringLayout {
		TicketsFrameLayout() {
			construct();
		}

		void construct() {
			for (Component c : getContentPane().getComponents()) {
				String name;
				if ((name = c.getName()) == null) {
					continue;
				}
				switch (name) {
				case ("scroll"):
					scrollLayout((JScrollPane) c);
					break;
				case ("panels"):
					buttonPanelLayout((Panels) c);
					break;
				}
			}
		}

		void scrollLayout(JScrollPane scrollPane) {
			System.out.println("ScrollPane");
			putConstraint(NORTH, scrollPane, 25, NORTH, SandTicketsFrame.this);
			putConstraint(SOUTH, scrollPane, rectangle.height - 145, NORTH, SandTicketsFrame.this);
			putConstraint(WEST, scrollPane, 5, WEST, SandTicketsFrame.this);
			putConstraint(EAST, scrollPane, rectangle.width - 25, WEST, SandTicketsFrame.this);
		}

		void buttonPanelLayout(Panels panels) {
			System.out.println("Button Panel");
			putConstraint(NORTH, panels, panels.getPanelY(), NORTH, SandTicketsFrame.this);
			putConstraint(SOUTH, panels, panels.getPanelY() + 100, NORTH, SandTicketsFrame.this);
			putConstraint(WEST, panels, ButtonPanel.X, WEST, SandTicketsFrame.this);
			putConstraint(EAST, panels, panels.getPanelWidth(), WEST, SandTicketsFrame.this);
		}
	}

	class TicketsComboBox extends JComboBox<String> {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		String filter;

		TicketsComboBox(String filter) {
			this.filter = filter;
			nittyGritty();
		}

		void nittyGritty() {
			setName(filter);
			addOptions();
			addUpdateAction();
			setEditable(true);
			((JLabel) getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		}

		void addUpdateAction() {
			addActionListener(new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (filter.equals(SandTicketsObject.SHIPPER)) {
						ticketsTable.updateTable(getSelectedItem().toString());
					}
				}
			});
		}

		void addOptions() {
			ArrayList<String> optionsArray = sandTicketsObject.getPossibleFilters(filter);
			for (String s : optionsArray) {
				addItem(s);
			}
		}
	}

	class Panels extends JPanel {
		ButtonPanel buttonPanel;
		FilterPanel filterPanel;
		StatsPanel statsPanel;

		Panels() {
			construct();
		}

		void construct() {
			addComponents();
			nittyGritty();
		}

		int getPanelWidth() {
			return rectangle.width;
		}

		int getPanelY() {
			return rectangle.height - 120;
		}

		void nittyGritty() {
			setName("panels");
			setVisible(true);
			setBackground(color);
			setLayout(new PanelsLayout());
		}

		void addComponents() {
			statsPanel = new StatsPanel();
			filterPanel = new FilterPanel(statsPanel);
			buttonPanel = new ButtonPanel(filterPanel);
			add(statsPanel);
			add(buttonPanel);
			add(filterPanel);
		}

		class PanelsLayout extends SpringLayout {
			PanelsLayout() {
				construct();
			}

			void construct() {
				for (Component c : getComponents()) {
					if (c.getName() == null) {
						continue;
					}
					String name = c.getName();
					switch (name) {
					case ("buttonPanel"):
						constructButtonLayout((ButtonPanel) c);
						break;
					case ("filterPanel"):
						constructFilterLayout((FilterPanel) c);
						break;
					case ("stats"):
						constructStatsPanel((StatsPanel) c);
						break;
					}
				}
			}

			void constructButtonLayout(ButtonPanel buttonPanel) {
				System.out.println("Button Layout");
				putConstraint(NORTH, buttonPanel, 0, NORTH, Panels.this);
				putConstraint(SOUTH, buttonPanel, 40, NORTH, Panels.this);
				putConstraint(WEST, buttonPanel, 0, WEST, Panels.this);
				putConstraint(EAST, buttonPanel, rectangle.width - 200, WEST, Panels.this);
			}

			void constructFilterLayout(FilterPanel filterPanel) {
				putConstraint(NORTH, filterPanel, 40, NORTH, Panels.this);
				putConstraint(SOUTH, filterPanel, 80, NORTH, Panels.this);
				putConstraint(WEST, filterPanel, 0, WEST, Panels.this);
				putConstraint(EAST, filterPanel, rectangle.width - 200, WEST, Panels.this);
			}

			void constructStatsPanel(StatsPanel statsPanel) {
				putConstraint(NORTH, statsPanel, 0, NORTH, Panels.this);
				putConstraint(SOUTH, statsPanel, 80, NORTH, Panels.this);
				putConstraint(WEST, statsPanel, rectangle.width - 500, WEST, Panels.this);
				putConstraint(EAST, statsPanel, rectangle.width - 25, WEST, Panels.this);
			}
		}
	}

	class StatsPanel extends JPanel {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		Semaphore semaphore;

		StatsPanel() {
			nittyGritty();
			semaphore = new Semaphore(1);
		}

		void nittyGritty() {
			setName("stats");
			setBackground(color);
			setLayout(new FlowLayout());
			setVisible(true);
		}

		synchronized void removeLabels() {
			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
				System.out.println("StatsPanel::removeLabels");
			}
			SwingWorker<Void, Component> worker = new SwingWorker<>() {
				public Void doInBackground() {
					for (Component c : getComponents()) {
						publish(c);
					}
					return null;
				}

				public void process(List<Component> chunks) {
					for (Component c : chunks) {
						System.out.println(((JLabel) c).getText());
						remove(c);
					}
				}

				public void done() {
					validate();
					setVisible(false);
					semaphore.release();
				}

			};
			worker.execute();
		}

		synchronized void addLabels(LinkedHashMap<String, HashMap<String, String>> filteredMap) {
			LinkedHashMap<String, Integer> countMap = getLabelCounts(filteredMap);
			constructLabel("Total : " + filteredMap.size());
			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
				System.out.println("StatsPanel::addLabels");
			}
			for (String s : countMap.keySet()) {
				constructLabel(s + " : " + countMap.get(s));
			}
			setVisible(true);
			semaphore.release();
		}

		void constructLabel(String text) {
			JLabel label = new JLabel(text);
			label.setBackground(color);
			label.setBorder(BorderFactory.createEtchedBorder());
			label.setVisible(true);
			label.setOpaque(true);
			System.out.println(label.getText());
			add(label);
		}

		LinkedHashMap<String, Integer> getLabelCounts(LinkedHashMap<String, HashMap<String, String>> filteredMap) {
			LinkedHashMap<String, Integer> countMap = new LinkedHashMap<>();
			ArrayList<String> dontAdd = getDontAddArray();
			for (String s : filteredMap.keySet()) {
				for (String key : filteredMap.get(s).keySet()) {
					if (dontAdd.contains(key)) {
						continue;
					}
					if (countMap.containsKey(filteredMap.get(s).get(key))) {
						countMap.put(filteredMap.get(s).get(key), countMap.get(filteredMap.get(s).get(key)) + 1);
						continue;
					}
					countMap.put(filteredMap.get(s).get(key), 1);
				}
			}
			return countMap;
		}

		static ArrayList<String> getDontAddArray() {
			ArrayList<String> dontAdd = new ArrayList<>();
			dontAdd.add(SandTicketsObject.BOL);
			dontAdd.add(SandTicketsObject.TRUCK);
			dontAdd.add(SandTicketsObject.DATE);
			dontAdd.add(SandTicketsObject.TIME);
			dontAdd.add(SandTicketsObject.WEIGHT);
			return dontAdd;
		}
	}

	class ButtonPanel extends JPanel {
		static final int HEIGHT = 100;
		static final int X = 0;
		Boolean viewing = false;
		Boolean copyPaste = false;
		FilterPanel filterPanel;

		ButtonPanel(FilterPanel filterPanel) {
			this.filterPanel = filterPanel;
			construct();
		}

		void construct() {
			setLayout(new FlowLayout());
			nittyGritty();
			addButtons();
		}

		void nittyGritty() {
			setBounds(getPanelBounds());
			setBackground(color);
			setName("buttonPanel");
			setOpaque(true);
			setVisible(true);
		}

		Rectangle getPanelBounds() {
			return new Rectangle(0, rectangle.height - 125, rectangle.width, 100);
		}

		void setEditorToComboBox() {
			if (copyPaste) {
				copyPaste = false;
				ticketsTable.addComboBox();
				updateButtonText((JButton) getComponentByName("editor"), "Copy/Paste");
			}
		}

		void addButtons() {
			constructButton("Save", "save", new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (viewing) {
						JOptionPane.showMessageDialog(null, "Select 'Input Tickets', and input tickets before saving");
						return;
					}
					setEditorToComboBox();
					LinkedHashMap<String, HashMap<String, String>> tickets = ticketsTable.getTicketsFromTable();
					if (sandTicketsObject.checkTickets(tickets)) {
						JOptionPane.showMessageDialog(null,
								"The following BOL's are duplicates: " + sandTicketsObject.getDuplicateBOLs());
						return;
					}
					sandTicketsObject.addTickets(tickets);
					sandTicketsObject.clearDuplicatesMap();
					try {
						sandTicketsObject.writeToFile();
					} catch (IOException e1) {
						System.out.println("SandTicketsObject::writeToFile");
					}
					dispose();
				}
			});
			constructButton("View Tickets", "viewInput", new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (viewing) {
						viewing = false;
						filterPanel.setVisible(false);
						ticketsTable.setEnabled(true);
						updateButtonText((JButton) getComponentByName("viewInput"), "View Tickets");
						filterPanel.getComponentByName("filterBox").setEnabled(false);
						filterPanel.statsPanel.removeLabels();
						ticketsTable.clearTable();
					} else {
						viewing = true;
						filterPanel.setVisible(true);
						ticketsTable.setEnabled(false);
						updateButtonText((JButton) getComponentByName("viewInput"), "Input Tickets");
						filterPanel.getComponentByName("filterBox").setEnabled(true);
						ticketsTable.fillTable(sandTicketsObject.ticketsMap);
					}
				}
			});
			constructButton("Copy/Paste", "editor", new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (copyPaste) {
						copyPaste = false;
						ticketsTable.addComboBox();
						updateButtonText((JButton) getComponentByName("editor"), "Copy/Paste");
					} else {
						copyPaste = true;
						ticketsTable.removeComboBox();
						updateButtonText((JButton) getComponentByName("editor"), "Dropdowns");
					}
				}
			});
			constructButton("Generate Tickets", "generate", new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					new TicketTemplateFrame(sandTicketsObject.getPossibleFilters(SandTicketsObject.TYPE),
							sandTicketsObject.getPossibleFilters(SandTicketsObject.SILO),
							sandTicketsObject.getPossibleFilters(SandTicketsObject.SHIPPER), ticketsTable);
				}
			});

		}

		void updateButtonText(JButton button, String text) {
			SwingWorker<Void, String> worker = new SwingWorker<>() {
				public Void doInBackground() {
					publish(text);
					return null;
				}

				public void process(List<String> chunks) {
					button.setText(chunks.get(0));
				}
			};
			worker.execute();
		}

		Component getComponentByName(String name) {
			for (Component c : getComponents()) {
				String cName;
				if ((cName = c.getName()) == null) {
					continue;
				}
				if (cName.equals(name)) {
					return c;
				}
			}
			return null;
		}

		void constructButton(String text, String name, AbstractAction action) {
			JButton button = new JButton();
			button.setText(text);
			button.setName(name);
			button.addActionListener(action);
			button.setVisible(true);
			add(button);
		}
	}

	class FilterPanel extends JPanel {
		StatsPanel statsPanel;
		Boolean editting = false;
		LinkedHashMap<String, HashMap<String, String>> savedMap;

		FilterPanel(StatsPanel statsPanel) {
			this.statsPanel = statsPanel;
			construct();
		}

		void construct() {
			addComponents();
			nittyGritty();
		}

		void nittyGritty() {
			setName("filterPanel");
			setBackground(color);
			setVisible(false);
			setLayout(new FlowLayout(FlowLayout.CENTER));
		}

		Boolean checkForEdits() {
			LinkedHashMap<String, HashMap<String, String>> newSavedMap = ticketsTable.getTicketsFromTable();
			if (newSavedMap == null) {
				return false;
			}
			removeBOLs(findChangedBOLs(newSavedMap));
			for (String s : newSavedMap.keySet()) {
				savedMap.put(s, newSavedMap.get(s));
			}
			System.out.println(savedMap);
			sandTicketsObject.addEdittedTickets(savedMap);
			savedMap.clear();
			return true;
		}

		ArrayList<String> findChangedBOLs(LinkedHashMap<String, HashMap<String, String>> newSavedMap) {
			ArrayList<String> removeArray = new ArrayList<>();
			for (String s : savedMap.keySet()) {
				if (!newSavedMap.containsKey(s)) {
					removeArray.add(s);
				}
			}
			return removeArray;
		}

		void removeBOLs(ArrayList<String> removeArray) {
			for (String s : removeArray) {
				savedMap.remove(s);
				sandTicketsObject.removeTicket(s);
			}
		}

		void checkEditing() {
			System.out.println("Editing Row = " + ticketsTable.getEditingRow());
			if (ticketsTable.getEditingRow() > -1) {
				ticketsTable.getCellEditor().stopCellEditing();
			}
		}

		void addComponents() {
			constructButton("Edit", "edit", new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!editting) {
						editting = true;
						savedMap = ticketsTable.getTicketsFromTable();
						ticketsTable.setEnabled(true);
						((JButton) getComponentByName("edit")).setText("Save Edits");
					} else {
						checkEditing();
						if (!checkForEdits()) {
							return;
						}
						editting = false;
						ticketsTable.setEnabled(false);
						((JButton) getComponentByName("edit")).setText("Edit");
					}
				}
			});
			constructFilterTextField();
			constructFilterOptions();
			constructButton("Search", "search", getSearchAction());
		}

		void constructFilterTextField() {
			JTextField textField = new JTextField();
			textField.setEnabled(true);
			textField.setVisible(true);
			textField.setName("text");
			textField.setColumns(10);
			add(textField);
		}

		void constructFilterOptions() {
			JComboBox<String> comboBox = new JComboBox<>();
			comboBox.addItem(SandTicketsObject.BOL);
			comboBox.addItem(SandTicketsObject.PO);
			comboBox.addItem(SandTicketsObject.DATE);
			comboBox.addItem(SandTicketsObject.TRUCK);
			comboBox.addItem(SandTicketsObject.TRUCKING);
			comboBox.addItem(SandTicketsObject.TYPE);
			comboBox.addItem(SandTicketsObject.SILO);
			comboBox.addItem(SandTicketsObject.SHIPPER);
			comboBox.setEditable(false);
			comboBox.setName("filterBox");
			((JLabel) comboBox.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
			comboBox.setVisible(true);
			comboBox.setEnabled(false);
			add(comboBox);
		}

		void constructButton(String text, String name, AbstractAction action) {
			JButton button = new JButton();
			button.setText(text);
			button.setName(name);
			button.addActionListener(action);
			button.setVisible(true);
			add(button);
		}

		AbstractAction getSearchAction() {
			return new AbstractAction() {
				/**
				 *
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					Executors.newSingleThreadExecutor().execute(() -> {
						statsPanel.removeLabels();
						JComboBox<String> comboBox = (JComboBox<String>) getComponentByName("filterBox");
						JTextField textField = (JTextField) getComponentByName("text");
						if (comboBox.getSelectedItem().toString().equals("") || textField.getText().equals("")) {
							return;
						}
						String filter = comboBox.getSelectedItem().toString();
						String value = textField.getText();
						ticketsTable.clearTable();
						LinkedHashMap<String, HashMap<String, String>> filteredMap = sandTicketsObject
								.getFilteredMap(filter, value);
						ticketsTable.fillTable(sandTicketsObject.getFilteredMap(filter, value));
						statsPanel.addLabels(filteredMap);
						statsPanel.validate();
					});
				}
			};
		}

		Component getComponentByName(String name) {
			for (Component c : getComponents()) {
				String cName;
				if ((cName = c.getName()) == null) {
					continue;
				}
				if (cName.equals(name)) {
					return c;
				}
			}
			return null;
		}
	}

	static final int SILO = 0;
	static final int TYPE = 1;
	static final int TRUCKING = 2;
	static final int WEIGHT = 3;
	static final int BOL = 4;
	static final int TRUCK = 5;
	static final int PO = 6;
	static final int DATE = 7;
	static final int TIME = 8;
	static final int SHIPPER = 9;
}
