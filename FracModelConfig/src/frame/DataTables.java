package frame;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.function.Function;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;

public class DataTables<T, V> extends JTabbedPane {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private final static int D_WIDTH = 600;
	private final static int D_HEIGHT = 450;
	private final static int D_X = 0;
	private final static int D_Y = 0;
	private Map<String, Map<V, List<T>>> dataMap;
	private String dataName;
	private boolean containsHeaders = false;
	public DataTables(String name) {
		setDataName(name);
		nittyGritty(name, new Rectangle(D_X, D_Y, D_WIDTH, D_HEIGHT));
		addBlankTab();
	}

	public DataTables(String name, Rectangle rectangle) {
		setDataName(name);
		nittyGritty(name, rectangle);
		addBlankTab();
	}


	public DataTables(Map<String, Map<V, List<T>>> importedData, String name) {
		setDataName(name);
		nittyGritty(name, new Rectangle(D_X, D_Y, D_WIDTH, D_HEIGHT));
		addTabs(importedData);
	}

	public DataTables(Map<String, Map<V, List<T>>> importedData, String name, int x, int y, int width, int height) {
		setDataName(name);
		nittyGritty(name, new Rectangle(x, y, width, height));
		addTabs(importedData);
	}

	public DataTables(Map<String, Map<V, List<T>>> importedData, String name, Rectangle rectangle) {
		setDataName(name);
		nittyGritty(name, rectangle);
		addTabs(importedData);
	}
	private void setContainsHeaders(boolean containsHeaders) {
		this.containsHeaders = containsHeaders;
	}
	private boolean containsHeaders() {
		return this.containsHeaders;
	}
	public void resetBounds(JFrame frame, int numTables, int index, int y) {
		Rectangle rect = frame.getBounds();
		int w = rect.width / numTables;
		int x = w * index;
		int height = rect.height - y - 45;
		setBounds(x, y, w, height);

	}

	public void resetBounds(Rectangle containRect, int numTables, int index) {
		int w = (containRect.width) / numTables;
		int x = w * index;
		int height = containRect.height;
		setBounds(x, 0, w, height);
	}

	public Map<String, Map<V, List<T>>> getDataMap() {
		return this.dataMap;
	}

	public String getDataName() {
		return this.dataName;
	}

	private void setDataName(String name) {
		this.dataName = name;
	}

	public final static String BLANK_TAB_NAME = "blank";
	public final static String BLANK_TAB_TEXT = "Tab_1";

	private void addBlankTab() {
		addTab(BLANK_TAB_TEXT, getScrollPane(BLANK_TAB_NAME, new LinkedHashMap<>(), new String().getClass()));
	}

	private void removeBlankTab() {
		if (this.getTitleAt(0).equals(BLANK_TAB_TEXT)) {
			removeTabAt(0);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addTabs(Map<String, Map<V, List<T>>> newData) {
		dataMap.putAll(newData);
		Class<?> vClass = getTypeClass(((LinkedHashMap) newData.values().toArray()[0]).keySet());
		for (Map.Entry<String, Map<V, List<T>>> entry : newData.entrySet()) {
			dataMap.put(entry.getKey(), entry.getValue());
			addTab(entry.getKey(), getScrollPane(entry.getKey(), entry.getValue(), vClass));
		}
		removeBlankTab();
	}

	@SuppressWarnings("unchecked")
	public void addTabs(String[] names, Map<V, List<T>>... maps) {
		int i = 0;
		Class<?> vClass = getTypeClass((maps[0]).keySet());
		for (String s : names) {
			dataMap.put(s, maps[i]);
			addTab(s, getScrollPane(s, maps[i], vClass));
			i++;
		}
		removeBlankTab();
	}
	private void setConstHeaders(String[] constHeaders) {
		this.constHeaders = constHeaders;
	}
	private String[] getConstHeaders() {
		return this.constHeaders;
	}
	private String[] constHeaders;
	@SuppressWarnings("unchecked")
	public void addTab(String name, Map<V, List<T>> map,String...constHeaders) {
		setContainsHeaders(true);
		setConstHeaders(constHeaders);
		int i = 0;
		Class<?> vClass = getTypeClass((map).keySet());
		dataMap.put(name, map);
		JPanel panel = getConstHeadersPanel(name,map,vClass,constHeaders);
		addTab(name, panel);
		i++;
		
		removeBlankTab();
	}

	public static <V> Class<?> getTypeClass(Set<V> set) {
		for (V v : set) {
			return v.getClass();
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T, V> void addTabs(Map<String, Map<V, List<T>>> newData, DataTables<T, V> dataTables) {
		Class<?> vClass = getTypeClass(((LinkedHashMap) newData.values().toArray()[0]).keySet());
		for (Map.Entry<String, Map<V, List<T>>> entry : newData.entrySet()) {
			dataTables.addTab(entry.getKey(), dataTables.getScrollPane(entry.getKey(), entry.getValue(), vClass));
		}
	}

	JScrollPane getScrollPane(String tabName, Map<V, List<T>> tableData, Class<?> vClass) {
		ImportTable importTable = new ImportTable(tableData, vClass);
		JScrollPane scrollPane = new JScrollPane(importTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		importTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		scrollPane.setName(getScrollPaneName(tabName));
		return scrollPane;
	}
	final static String CONST_HEADER_TABLE_PANEL = "constant_header_table_panel";
	JPanel getConstHeaderPanel(ImportTable importTable,JScrollPane scrollPane,String...constHeaders) {
		JPanel panel = new JPanel();
		panel.setName(CONST_HEADER_TABLE_PANEL);
		setBoundsListener(panel,(component)->{
			return component.getBounds();
		});
		ConstHeaderLabels headerLabels = new ConstHeaderLabels(importTable,constHeaders);
		headerLabels.setAlignmentX(JPanel.CENTER_ALIGNMENT);
		panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
		panel.add(headerLabels);
		panel.add(scrollPane);
		panel.setVisible(true);
		return panel;
	}
	JPanel getConstHeadersPanel(String tabName, Map<V, List<T>> tableData, Class<?> vClass,String...constHeaders) {
		ImportTable importTable = new ImportTable(tableData, vClass);
		JScrollPane scrollPane = new JScrollPane(importTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		setBoundsListener(scrollPane,(component)->{
			Rectangle rectangle = component.getBounds();
			return new Rectangle(0,ConstHeaderLabels.PANEL_HEIGHT,rectangle.width,rectangle.height-ConstHeaderLabels.PANEL_HEIGHT);
		});
		scrollPane.setVisible(true);
		scrollPane.setOpaque(true);
		scrollPane.setEnabled(true);
		importTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		scrollPane.setName(getScrollPaneName(tabName));
		scrollPane.setAlignmentY(JScrollPane.TOP_ALIGNMENT);
		JPanel panel  = getConstHeaderPanel(importTable,scrollPane,constHeaders);
		panel.setBackground(Color.black);
		panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
		return panel;
	}
	
	void setScrollPaneBoundsListener(JScrollPane scrollPane) {
		scrollPane.addHierarchyBoundsListener(new HierarchyBoundsListener() {

			@Override
			public void ancestorMoved(HierarchyEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void ancestorResized(HierarchyEvent e) {

				if(e.getComponent().equals(scrollPane.getParent())) {
					Component parent = e.getComponent();
					scrollPane.setPreferredSize(new Dimension(parent.getBounds().width,parent.getBounds().height-ConstHeaderLabels.PANEL_HEIGHT));
					//scrollPane.revalidate();
					System.out.println("Updated Bounds");
				}
				System.out.println("End of ancestorResized() method");
			}
				
		});
	}
	
	void setThisBoundsListener(JComponent component,Function<Container,Rectangle> function) {
		component.addHierarchyBoundsListener(new HierarchyBoundsListener() {

			@Override
			public void ancestorMoved(HierarchyEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void ancestorResized(HierarchyEvent e) {
				Container container = e.getChangedParent();
				if(container.equals(DataTables.this.getParent())) {
					Rectangle rectangle = function.apply(container);
					component.setPreferredSize(new Dimension(rectangle.width,rectangle.height));
					component.setBounds(rectangle);
					component.revalidate();
				}
			}
				
		});
	}
	
	void setBoundsListener(JComponent component,Function<Component,Rectangle> function) {
		component.addHierarchyBoundsListener(new HierarchyBoundsListener() {

			@Override
			public void ancestorMoved(HierarchyEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void ancestorResized(HierarchyEvent e) {
				if(e.getChanged().equals(DataTables.this)) {
					Component parent = e.getComponent();
					Rectangle rectangle = function.apply(parent);
					component.setPreferredSize(new Dimension(rectangle.width,rectangle.height-ConstHeaderLabels.PANEL_HEIGHT));
					component.setBounds(rectangle);
					component.revalidate();
				}
			}
				
		});
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Class<?> getTypeClass() {
		return getTypeClass(((Map<T, V>) dataMap.values().toArray()[0]).keySet());
	}

	public Map<String, List<T>> getSelectedData() {
		JComponent c = (JComponent) getSelectedComponent();
		JTable table = (JTable) GUIUtilities.getComponentByName(c, ImportTable.TABLE_NAME);
		return getSelectedTableData(table, getTypeClass());
	}
	
	private Map<String, List<T>> getSelectedTableData(JTable table, Class<?> vClass) {
		Map<String, List<T>> map = new LinkedHashMap<>();
		for (int c : table.getSelectedColumns()) {
			String header = vClass.getSimpleName().equals("String")
					? (String) table.getColumnModel().getColumn(c).getHeaderValue()
					: getCellValue(table, table.getSelectedRow(), c);
			map.put(header, getColumnData(table, c));
		}
		return map;
	}
	
	public Map<String,List<T>> getTableData(){
		JComponent c = (JComponent) getSelectedComponent();
		JTable table = (JTable) GUIUtilities.getComponentByName(c, ImportTable.TABLE_NAME);
		return getTableData(table,getTypeClass());
	}
	private Map<String,List<T>> getTableData(JTable table,Class<?> vClass){
		Map<String,List<T>> map = new LinkedHashMap<>();
		String[] headers = getConstHeaders();
		int startRow = 1;
		for(int i = 0;i<headers.length;i++) {
			String header = headers[i];
			map.put(header, getColumnData(table,i,startRow));
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	private List<T> getColumnData(JTable table, int column) {
		List<T> array = new ArrayList<>();

		for (int i : table.getSelectedRows()) {
			if (i == table.getSelectedRow()) {
				continue;
			}
			array.add((T) getCellValue(table, i, column));
		}
		return array;
	}
	@SuppressWarnings("unchecked")
	private List<T> getColumnData(JTable table, int column, int startRow) {
		List<T> array = new ArrayList<>();

		for (int i = startRow;i<table.getRowCount();i++) {
			String value = getCellValue(table,i,column);
			if (value.equals("")|value.equals(" ")) {
				break;
			}
			array.add((T)String.valueOf(value).trim());
		}
		return array;
	}
	private String getCellValue(JTable table, int row, int column) {
		String cellValue = table.getValueAt(row, column) == null ? "" : String.valueOf(table.getValueAt(row, column));
		return cellValue;
	}

	private Map<String, List<String>> getSelectionHeaders(JTable table) {
		Map<String, List<String>> map = new LinkedHashMap<>();
		int headerRow = table.getSelectedRow();
		for (int c : table.getSelectedColumns()) {
			String cellValue = table.getValueAt(headerRow, c) == null ? ""
					: String.valueOf(table.getValueAt(headerRow, c));
			map.put(cellValue, new ArrayList<>());
		}
		return map;
	}

	private void nittyGritty(String name, Rectangle rectangle) {
		setName(name);
		dataMap = new LinkedHashMap<>();
		setBounds(rectangle);
		setBoundsListener(this,(component)->{
			return component.getBounds();
		});
		setVisible(true);
	}

	private void nittyGritty(String name, Class<V> vClass) {
		setName(name);
		setBoundsListener(this,(component)->{
			return component.getBounds();
		});
		setVisible(true);
	}

	public static String getScrollPaneName(String tabText) {
		return tabText + SCROLL_PANE_SUFFIX;
	}

	public final static String SCROLL_PANE_SUFFIX = "_scroll";

	private final static int MIN_ROWS = 100;
	private final static int MIN_COLUMNS = 15;
	class ConstHeaderLabels extends JPanel{
		final static int PANEL_HEIGHT = 25;
		final static int LABEL_WIDTH = 150;
		String[] constHeaders;
		ConstHeaderLabels(ImportTable importTable,String...constHeaders){
			this.constHeaders = constHeaders;
			nittyGritty(importTable,constHeaders);
		}
		final static String HEADER_LABEL_PANEL = "header_label_panel";
		void nittyGritty(ImportTable importTable,String...constHeaders) {
			setAlignmentX(JPanel.LEFT_ALIGNMENT);
			setPreferredSize(new Dimension(DataTables.this.getBounds().width,PANEL_HEIGHT));
			setMaximumSize(new Dimension(DataTables.this.getBounds().width,PANEL_HEIGHT));
			this.setAlignmentX(JPanel.LEFT_ALIGNMENT);
			setLayout(null);
			setBounds(0,0,DataTables.this.getBounds().width,PANEL_HEIGHT);
			setName(HEADER_LABEL_PANEL);
			addLabels(constHeaders);
			setBackground(Color.LIGHT_GRAY);
			setBoundsListener();
		}

		void addLabels(String...constHeaders) {
			int i = 0;
			for(String s:constHeaders) {
				add(getLabel(s,i));
				i++;
			}
		}
		JLabel getLabel(String labelText,int index) {
			JLabel label = new JLabel();
			label.setName(labelText);
			label.setText(labelText);
			label.setBackground(Color.LIGHT_GRAY);
			label.setBounds(getLabelBounds(index));
			label.setMinimumSize(new Dimension(LABEL_WIDTH,PANEL_HEIGHT));
			//label.setAlignmentX(JLabel.LEFT_ALIGNMENT);
			//label.setPreferredSize(new Dimension(LABEL_WIDTH,PANEL_HEIGHT));
			//label.setHorizontalAlignment(SwingConstants.LEFT);
			label.setHorizontalTextPosition(JLabel.CENTER);
			//label.setVerticalTextPosition(JLabel.CENTER);
			label.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			label.setOpaque(true);
			label.setVisible(true);
			return label;
		}
		String getCenteredLabelString(String labelText) {
			int charWidth = 4;
			int curWidth = labelText.length()*charWidth;
			int buffWidth = (LABEL_WIDTH-(curWidth))/2;
			String buffString = getBuffString(buffWidth,charWidth);
			return buffString+labelText+buffString;
		}
		String getBuffString(int buffWidth,int charWidth) {
			String buffString = "";
			for(int i = 0;i<buffWidth;i+=charWidth) {
				buffString+=" ";
			}
			return buffString;
		}
		Rectangle getLabelBounds(int index) {
			int x = LABEL_WIDTH*index;
			return new Rectangle(x,0,LABEL_WIDTH,PANEL_HEIGHT);
		}
		void resetBounds(Rectangle parentBounds) {
			Rectangle rectangle = new Rectangle(0,0,parentBounds.width,PANEL_HEIGHT);
			setPreferredSize(new Dimension(rectangle.width,rectangle.height));
			setBounds(rectangle);
			//revalidate();
		}
		void setBoundsListener() {
			addHierarchyBoundsListener(new HierarchyBoundsListener() {

				@Override
				public void ancestorMoved(HierarchyEvent e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void ancestorResized(HierarchyEvent e) {
					Component component = e.getChanged();
					String name = component.getClass().getSimpleName();
					if(name!=null&&name.equals("SelectDataFrame")) {
						resetBounds(component.getBounds());
						System.out.println(getBounds());
						System.out.println("Resized ConstHeaderLabels Panel");
					}
				}
				
			});
		}
	}
	class ImportTable extends JTable {
		private final static int MIN_WIDTH = 150;
		private final static int MIN_HEIGHT = 25;
		public final static String TABLE_NAME = "table";

		ImportTable(Map<V, List<T>> data, Class<?> vClass) {
			super(getNumRows(data, vClass), getNumColumns(data, vClass));
			nittyGritty(data, vClass);
		}

		void nittyGritty(Map<V, List<T>> data, Class<?> vClass) {
			setColumnWidth();
			setCellSelectionEnabled(true);
			this.setColumnSelectionAllowed(true);
			setRowHeight(MIN_HEIGHT);
			setName(TABLE_NAME);
			if (vClass.getSimpleName().equals(new String().getClass().getSimpleName())) {
				setHeaders(data);
			}
			inputData(data, vClass);
		}

		void setColumnWidth(int columnWidth) {
			for (int i = 0; i < getColumnCount(); i++) {
				getColumnModel().getColumn(i).setMinWidth(columnWidth);
			}
		}

		void setColumnWidth() {
			for (int i = 0; i < getColumnCount(); i++) {
				getColumnModel().getColumn(i).setMinWidth(MIN_WIDTH);
			}
		}

		void inputData(Map<V, List<T>> data, Class<?> vClass) {
			int i = 0;
			for (List<T> array : data.values()) {
				if (vClass.getSimpleName().equals(new String().getClass().getSimpleName())) {
					inputDataCol(i, array);
				} else {
					inputDataRow(i, array);
				}
				i++;
			}
		}

		// Integer as key correlating to row in Table
		void inputData(Map<V, List<T>> data) {

		}

		private void inputDataCol(int col, List<T> dataCol) {
			int i = 0;
			for (T t : dataCol) {
				setValueAt(t, i, col);
				i++;
			}
		}

		private void inputDataRow(int row, List<T> dataRow) {
			int i = 0;
			for (T t : dataRow) {

				setValueAt(t, row, i);
				i++;
			}
		}

		void setHeaders(Map<V, List<T>> data) {
			int i = 0;
			for (V v : data.keySet()) {
				getColumnModel().getColumn(i).setHeaderValue(String.valueOf(v));
				i++;
			}
		}

	}

	private int getNumColumns(Map<V, List<T>> data, Class<?> vClass) {
		if (vClass.getSimpleName().equals(String.class.getSimpleName())) {
			return data.size() < MIN_COLUMNS ? MIN_COLUMNS : data.size();
		}
		int max = MIN_COLUMNS;
		for (List<T> array : data.values()) {
			max = array.size() > max ? array.size() : max;
		}
		return max;
	}

	private int getNumRows(Map<V, List<T>> data, Class<?> vClass) {
		if (vClass.getSimpleName().equals(Integer.class.getSimpleName())) {
			return data.size() < MIN_ROWS ? MIN_ROWS : data.size();
		}
		int max = MIN_ROWS;
		for (List<T> array : data.values()) {
			max = array.size() > max ? array.size() : max;
		}
		return max;
	}

	public static class SelectDataFrame<T, V> extends JFrame {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		DataTables<T, V> dataTables;
		Map<String, List<T>> selectedData;
		Map<String,List<T>> tableData;
		Semaphore semaphore = new Semaphore(0);
		private boolean done = false;
		public SelectDataFrame(DataTables<T, V> dataTables, Rectangle rectangle, Class<T> t) {
			this.dataTables = dataTables;
			nittyGritty(rectangle);
		}

		private final static int SAVE_PANEL_HEIGHT = 100;

		void nittyGritty(Rectangle rectangle) {
			setBounds(rectangle.x, rectangle.y, rectangle.width, rectangle.height + SAVE_PANEL_HEIGHT);
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
			setTitle("Select Survey Data");
			add(dataTables);
			setAlwaysOnTop(true);
			constrSavePanel(rectangle);
			setVisible(true);
		}
		
		void constrSavePanel(Rectangle rectangle) {
			OptionPanel optionPanel = new OptionPanel(
					new Rectangle(0, rectangle.height, rectangle.width, SAVE_PANEL_HEIGHT));
			optionPanel.addOption("Save", new AbstractAction() {
				/**
				 *
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					setTableData(dataTables.getTableData());
					setSelectedData(dataTables.getSelectedData());
					done = true;
					dispose();
				}
			});
			optionPanel.addOption("Select Different File", new AbstractAction() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					done = true;
					dispose();
				}
			});
			optionPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			add(optionPanel);
		}
		private void setTableData(Map<String,List<T>> tableData){
			if(!dataTables.containsHeaders()) {
				return;
			}
			this.tableData = tableData;
			
		}
		public Map<String,List<T>> getTableData() throws InterruptedException{
			semaphore.acquire();
			semaphore.release();
			return this.tableData;
		}
		public Map<String, List<T>> getSelectedData() throws InterruptedException {
			semaphore.acquire();
			semaphore.release();
			return this.selectedData;
		}

		void setSelectedData(Map<String, List<T>> map) {
			this.selectedData = map;
			semaphore.release();
		}

		@Override
		protected void processWindowEvent(final WindowEvent e) {
			super.processWindowEvent(e);
			System.out.println(e.getID());
			if (e.getID() == WindowEvent.WINDOW_CLOSING|e.getID()==WindowEvent.WINDOW_CLOSED) {
			
				if(!done) {
					System.out.println("goBack = "+done);
					setTableData(new LinkedHashMap<>());
				}
				semaphore.release();
			}
		}
	}
}
