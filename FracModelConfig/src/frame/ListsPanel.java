package frame;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;

public class ListsPanel extends JPanel {
	// key is used as name of JList, value is the list used in the jlist
	private Map<String, List<String>> listMap;
	private PanelPos panelPos;
	private int rows = 1;
	private int columns;
	private List<String> labels;

	
	public ListsPanel(String name, PanelPos panelPos, Map<String, List<String>> listMap) {
		setName(name);
		this.panelPos = panelPos;
		this.listMap = listMap;
		this.labels = getListFromSet(listMap.keySet());
		nittyGritty();
	}
	
	@SuppressWarnings("unused")
	private Rectangle initialBounds;
	
	public ListsPanel(String name, PanelPos panelPos, Map<String, List<String>> listMap, List<String> labels) {
		setName(name);
		this.panelPos = panelPos;
		this.listMap = listMap;
		this.labels = labels;
		nittyGritty();
	}

	public ListsPanel(String name, PanelPos panelPos, Map<String, List<String>> listMap, int rows) {
		setName(name);
		this.panelPos = panelPos;
		this.listMap = listMap;
		this.labels = getListFromSet(listMap.keySet());
		this.rows = rows;
		nittyGritty();
	}

	public ListsPanel(String name, PanelPos panelPos, Map<String, List<String>> listMap, List<String> labels,
			int rows) {
		setName(name);
		this.panelPos = panelPos;
		this.listMap = listMap;
		this.rows = rows;
		this.labels = labels;
		nittyGritty();
	}

	private void nittyGritty() {
		setColumns();
		setInitialBounds();
		addBoundsListener();
		constrLists(listMap);
		setLayout(getGridLayout());
		setVisible(true);
	}
	
	private void setInitialBounds() {
		Rectangle screenBounds = GUIUtilities.getCenterRectangle(0f);
		initialBounds = panelPos.getPanelRect(screenBounds);
	}

	private GridLayout getGridLayout() {
		GridLayout gridLayout = new GridLayout(rows,columns);
		return gridLayout;
	} 
	private void setColumns() {
		int cols = listMap.size() / rows;
		cols += ((listMap.size() % rows) > 0 ? 1 : 0);
		this.columns = cols;
	}

	private List<String> getListFromSet(Set<String> set) {
		List<String> list = new ArrayList<>();
		set.forEach((String string) -> {
			list.add(string);
		});
		return list;
	}

	private void constrLists(Map<String, List<String>> listMap) {
		int i = 0;
		for (String s : listMap.keySet()) {
			constrList(s, listMap.get(s), "import", labels.get(i),listMap.size(),i);
			i++;
		}
	}
	public void addKeyListenerToList(Function<JList<String>,KeyListener> function,String listName) {
		JList<String> list = getList(listName);
		list.addKeyListener(function.apply(list));
	}
	private final static Color DEFAULT_BACKGROUND = Color.LIGHT_GRAY;
	
	@SuppressWarnings("unused")
	@Deprecated
	private void constrList(String name, List<String> list, String buttonText, String label) {
		JList<String> jList = new JList<>(getStringArr(list.toArray()));
		jList.setName(name);
		add(new ListSelectPanel(jList, label, buttonText));
	}
	private void constrList(String name, List<String> list, String buttonText, String label,int totalPanels,int index) {
		JList<String> jList = new JList<>(getStringArr(list.toArray()));
		jList.setName(name);
		((DefaultListCellRenderer)(jList.getCellRenderer())).setHorizontalAlignment(JLabel.LEFT);
		add(new ListSelectPanel(jList, label, buttonText,totalPanels,index));
	}
	private float getIndPanelWidthPerc(int numPanels) {
		return ((float)(1f/((float)numPanels)));
	}
	private int getScrollWidth() {
		return ((int)(getBounds().getWidth()/columns)*3/4);
	}
	private int getScrollHeight() {
		return ((int)(getBounds().getHeight()/rows));
	}

	private Dimension getViewportDim() {
		Dimension dim = new Dimension(getScrollWidth(),getScrollHeight());
		return dim;
	}
	
	@SuppressWarnings("unused")
	private void setScrollMaxes(JScrollPane scrollPane) {
		int width = getScrollWidth();
		int height = getScrollHeight();
		scrollPane.setMaximumSize(new Dimension(width,height));
	}
	
	@SuppressWarnings("unused")
	private Rectangle getViewportRect() {
		int width = getScrollWidth()*2/3;
		int height = getScrollHeight();
		return new Rectangle(0,0,width,height);
	}
	private final static String SCROLL_PANE_NAME = ListSelectPanel.SCROLL_PANE_NAME;
	
	/*
	 * private void constrList(String name, List<String> list) { JList<String> jList
	 * = new JList<>(getStringArr(list.toArray())); jList.setName(name);
	 * ((DefaultListCellRenderer)(jList.getCellRenderer())).setHorizontalAlignment(
	 * JLabel.CENTER); //((DefaultListCellRenderer)(jList.getCellRenderer())).
	 * setHorizontalTextPosition(DefaultListCellRenderer.CENTER); JScrollPane
	 * scrollPane = new JScrollPane(jList); scrollPane.setName(SCROLL_PANE_NAME);
	 * scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	 * scrollPane.setHorizontalScrollBarPolicy(JScrollPane.
	 * HORIZONTAL_SCROLLBAR_ALWAYS); add(scrollPane); }
	 */

	public static String[] getStringArr(Object[] obj) {
		String[] arr = new String[obj.length];
		int i = 0;
		for (Object o : obj) {
			arr[i] = (String) o;
			i++;
		}
		return arr;
	}

	private void resetPanelBounds(JPanel parent) {
		Rectangle rect = parent.getBounds();
		setBounds(panelPos.getPanelRect(rect));
	}
	
	private void resetListBounds() {
		
		for(String s:listMap.keySet()) {
			@SuppressWarnings("unchecked")
			JList<String> list = (JList<String>)GUIUtilities.getComponentByName(this, s);
			
		}
	}	
	private JPanel findParentPanel(JPanel panel) {
		if (panel.equals(getParent())) {
			return panel;
		}
		for (Component c : panel.getComponents()) {
			String cName = c.getClass().getSimpleName();
			if (cName.equals("JPanel") && getParent().equals((JPanel) c)) {
				return (JPanel) c;
			}
		}
		return null;
	}



	public JList<String> getList(String listName){
		ListSelectPanel selectPanel = (ListSelectPanel) GUIUtilities.getComponentByName(this,
				ListSelectPanel.getPrefixedName(listName));
		JList<String> list = selectPanel.getList();
		return list;
	}
	
	public void removeFromList(String listName,String itemToRemove) {
		JList<String> jList = getList(listName);
		List<String> list = getListedItems(jList);
		list.remove(itemToRemove);
		jList.setModel(getNewModel(list));
	}
	
	public void addToList(String listName,List<String> addList) {
		JList<String> list = getList(listName);
		Set<String> updated = getUpdatedList(list,addList);
		list.setModel(getNewModel(updated));
		
	}
	public void addToList(String listName,Comparator<String> comp,String...addString) {
		JList<String> list = getList(listName);
		Set<String> updated = getUpdatedList(list,addString);
		updated = sortItemsInSet(updated,comp);
		list.setModel(getNewModel(updated));
		
	}
	public void addToList(String listName,String...addString) {
		JList<String> list = getList(listName);
		Set<String> updated = getUpdatedList(list,addString);
		list.setModel(getNewModel(updated));
		
	}
	private Set<String> sortItemsInSet(Set<String> set,Comparator<String> comp){
		List<String> list = new ArrayList<>();
		list.addAll(set);
		list.sort(comp);
		Set<String> set2 = new LinkedHashSet<>();
		set2.addAll(list);
		return set2;
	}
	public void addToList(String listName,Set<String> addList) {
		JList<String> list = getList(listName);
		Set<String> updated = getUpdatedList(list,addList);
		list.setModel(getNewModel(updated));
	}
	
	private Set<String> getUpdatedList(JList<String> list,String...addString){
		Set<String> updated = new LinkedHashSet<>();
		for(int i = 0;i<list.getModel().getSize();i++) {
			updated.add(list.getModel().getElementAt(i));
		}
		for(String s:addString) {
			updated.add(s);
		}
		return updated;
	}
	private List<String> getListedItems(JList<String> jList){
		List<String> list = new ArrayList<>();
		ListModel<String> model = jList.getModel();
		for(int i = 0;i<model.getSize();i++) {
			list.add(model.getElementAt(i));
		}
		return list;
	}
	private Set<String> getUpdatedList(JList<String> list,List<String> addList){
		Set<String> updated = new LinkedHashSet<>();
		for(int i = 0;i<list.getModel().getSize();i++) {
			updated.add(list.getModel().getElementAt(i));
		}
		updated.addAll(addList);
		return updated;
	}
	private Set<String> getUpdatedList(JList<String> list,Set<String> addList){
		Set<String> updated = new LinkedHashSet<>();
		for(int i = 0;i<list.getModel().getSize();i++) {
			updated.add(list.getModel().getElementAt(i));
		}
		updated.addAll(addList);
		return updated;
	}
	
	private static ListModel<String> getNewModel(Collection<String> collection){
		DefaultListModel<String> model = new DefaultListModel<>();
		model.addAll(collection);
		return model;
	}
	public void addActionToButton(Function<JList<String>, AbstractAction> function, String listName) {
		ListSelectPanel selectPanel = (ListSelectPanel) GUIUtilities.getComponentByName(this,
				ListSelectPanel.getPrefixedName(listName));
		JButton button = (JButton) GUIUtilities.getComponentByName(selectPanel, ListSelectPanel.BUTTON_NAME);
		JList<String> list = selectPanel.getList();
		AbstractAction action = function.apply(list);
		button.addActionListener(action);
	}

	public void addActionToButton(Function<JList<String>, AbstractAction> function, String listName,
			String buttonText) {
		ListSelectPanel selectPanel = (ListSelectPanel) GUIUtilities.getComponentByName(this,
				ListSelectPanel.getPrefixedName(listName));
		JButton button = (JButton) GUIUtilities.getComponentByName(selectPanel, ListSelectPanel.BUTTON_NAME);
		button.setText(buttonText);
		JList<String> list = selectPanel.getList();
		AbstractAction action = function.apply(list);
		button.addActionListener(action);
	}
	
	private final static String JPANEL_CLASS_DIR = "javax.swing.JPanel";
	private void addBoundsListener() {
		addHierarchyBoundsListener(new HierarchyBoundsListener() {

			@Override
			public void ancestorMoved(HierarchyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void ancestorResized(HierarchyEvent e) {
				Component c = e.getChanged();
				String className = c.getClass().getGenericSuperclass().getTypeName();
				if (className.equals(JPANEL_CLASS_DIR)) {
					JPanel parent = findParentPanel((JPanel) c);
					if (parent == null) {
						return;
					}
					System.out.println("Parent Bounds: "+parent.getBounds());
					resetPanelBounds(parent);
					System.out.println("Panel Bounds: "+getBounds());
				}
			}

		});
	}
	
	private void resetBoundsOfLayout() {
		GridLayout gridLayout = (GridLayout)getLayout();		
		System.out.println(gridLayout.preferredLayoutSize(this));
	}
	private class ListSelectPanel extends JPanel {
		private final static String NAME_PREFIX = "ListSelection_";
		private JList<String> list;
		private String labelText;
		private String buttonText;
		private List<String> selected;

		ListSelectPanel(JList<String> list, String labelText, String buttonText) {
			this.labelText = labelText;
			this.buttonText = buttonText;
			this.list = list;
			nittyGritty();
		}

		private PanelPos panelPos;
		
		private int totalPanels;
		private int index;
		ListSelectPanel(JList<String> list, String labelText, String buttonText,int totalPanels,int index) {
			this.labelText = labelText;
			this.buttonText = buttonText;
			this.list = list;
			this.totalPanels = totalPanels;
			this.index = index;
			setPanelPos(getIndPanelWidthPerc(totalPanels),index);
			nittyGritty();
		}
		
		private void setPanelPos(float percOfWidth,int index) {
			PanelPos panelPos = new PanelPos(0f,0f,getLeftPerc(percOfWidth,index),getRightPerc(percOfWidth,index));
			this.panelPos = panelPos;
		}
		
		//zero indexed
		private float getLeftPerc(float percOfWidth,int index) {
			return percOfWidth*index;
		}
		
		private float getRightPerc(float percOfWidth,int index) {
			float left = getLeftPerc(percOfWidth,index);
			return (1f-(left+percOfWidth));
		}
		
		private void nittyGritty() {
			setName(NAME_PREFIX + list.getName());
			setBackground(DEFAULT_BACKGROUND);
			setBorder(BorderFactory.createEtchedBorder());
			addHierarchyBoundsListener(new HierarchyBoundsListener() {

				@Override
				public void ancestorMoved(HierarchyEvent e) {
					
				}

				@Override
				public void ancestorResized(HierarchyEvent e) {
					//Executors.newSingleThreadExecutor().execute(()->{
						setBounds(panelPos.getPanelRect(ListsPanel.this.getBounds()));
						System.out.println("Calculated Rectangle: "+panelPos.getPanelRect(ListsPanel.this.getBounds()));
					//});
				}
			});
			//setLayout(null);
			setLayout(new BoxLayout(this, ((rows > columns ? BoxLayout.X_AXIS : BoxLayout.Y_AXIS))));
			addLabel();
			addList();
			addButton();
		}

		/*
		 * private BorderLayout getBorderLayout() { BorderLayout borderLayout = new
		 * BorderLayout(); }
		 */
		public JList<String> getList() {
			return this.list;
		}

		public static String getPrefixedName(String listName) {
			return NAME_PREFIX + listName;
		}

		private final static String SCROLL_PANE_NAME = "select_scroll_pane";

		private void addList() {
			JScrollPane scrollPane = new JScrollPane(list);
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			scrollPane.setName(SCROLL_PANE_NAME);
			scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
			scrollPane.setAlignmentY(Component.CENTER_ALIGNMENT);
			//scrollPane.setMaximumSize(getMaximumSize());
			add(scrollPane);
		}

		private final static String LABEL_NAME = "select_label";

		private void addLabel() {
			JLabel label = new JLabel();
			label.setBackground(DEFAULT_BACKGROUND);
			label.setName(LABEL_NAME);
			label.setAlignmentX(Component.CENTER_ALIGNMENT);
			label.setAlignmentY(Component.CENTER_ALIGNMENT);
			label.setText(labelText);
			add(label);
		}

		private void setSelected(List<String> selected) {
			this.selected = selected;
		}

		private final static String BUTTON_NAME = "select_button";

		private void addButton() {
			JButton button = new JButton();
			button.setName(BUTTON_NAME);
			button.setText(buttonText);
			button.addActionListener(new AbstractAction() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					setSelected(list.getSelectedValuesList());
				}
			});
			button.setAlignmentX(Component.CENTER_ALIGNMENT);
			button.setAlignmentY(Component.CENTER_ALIGNMENT);
			add(button);
		}
	}

}
