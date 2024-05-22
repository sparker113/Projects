package frame;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JPanel;

public class DataTablePanel extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String panelName;
	private ExecutorService executor;
	//private PanelPadding panelPadding;
	private PanelPos panelPos;
	private String[] dataTableNames;
	
//	@Deprecated
//	public DataTablePanel(String panelName,PanelPadding panelPadding,String...dataTableNames) {
//		this.panelName = panelName;
	//	this.panelPadding = panelPadding;
//		this.dataTableNames = dataTableNames;
//		nittyGritty(dataTableNames);
//	}
	
	public DataTablePanel(String panelName,PanelPos panelPos,String...dataTableNames) {
		this.panelName = panelName;
		this.panelPos = panelPos;
		this.dataTableNames = dataTableNames;
		nittyGritty(dataTableNames);
	}
	
	private void addDataTables(String[] dataTableNames) {
		for(String s:dataTableNames) {
			add(new DataTables<String,String>(s));
		}
	}

	/*
	 * @Deprecated public void setPadding(int top,int bottom,int left,int right) {
	 * panelPadding = new PanelPadding(top,bottom,left,right); }
	 * 
	 * @Deprecated public void setPadding(PanelPadding panelPadding) {
	 * this.panelPadding = panelPadding; }
	 */
	private void nittyGritty(String[] dataTableNames) {
		setName(panelName);
		executor = Executors.newSingleThreadExecutor();
		addBoundsChangeListener();
		setPanelLayout();
		addDataTables(dataTableNames);
		setBackground(Color.LIGHT_GRAY);
		setVisible(true);

	}
	private void setPanelLayout() {
		GridLayout gridLayout = new GridLayout(1,0);
		gridLayout.setHgap(10);
		setLayout(gridLayout);
	}
	@SuppressWarnings("unchecked")
	private void resetBounds(Rectangle parentBounds) {
		
		setBounds(getPanelPos(parentBounds));
		int i = 0;
		for(Component c:getComponents()) {
			if(isDataTable(c.getName(),dataTableNames)) {
				((DataTables<String,String>)c).resetBounds(getBounds(), dataTableNames.length, i);
				i++;
			}
		}
	}
	private boolean isDataTable(String name,String[] dataTableNames) {
		if(name==null) {
			return false;
		}
		for(String s:dataTableNames) {
			if(name.equals(s)) {
				return true;
			}
		}
		return false;
	}
//	@Deprecated
//	private Rectangle getPaddedBounds(Rectangle parentBounds) {
//		int x = panelPadding.left;
//		int y = panelPadding.top;
//		int width = parentBounds.width-x-panelPadding.right;
//		int height = parentBounds.height-y-panelPadding.bottom;
//		return new Rectangle(x,y,width,height);
//	}
	private Rectangle getPanelPos(Rectangle parentBounds) {
		return panelPos.getPanelRect(parentBounds);
	}
	public final static int JFRAME_BOTTOM_INSET = 35;
	private void addBoundsChangeListener() {

		HierarchyBoundsListener listener = new HierarchyBoundsListener() {

			@Override
			public void ancestorMoved(HierarchyEvent e) {
				
				
			}

			@Override
			public void ancestorResized(HierarchyEvent e) {
				executor.execute(()->{
					Component c = e.getChanged();
					String className = c.getClass().getSimpleName();
					if(getParent().equals(c)) {
						System.out.println("DataTablePanel parent: "+c.getName()+" - "+c.getClass().getSimpleName());
						resetBounds(c.getBounds());
					}
					/*
					 * Component component = e.getChanged();
					 * if(!component.getClass().getSimpleName().equals("JFrame")) { return; }
					 * System.out.println("Reset Bounds"); resetBounds(component.getBounds());
					 */
				});
				
			}
			
		};
		
		addHierarchyBoundsListener(listener);
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
	/*
	 * public static class PanelPadding{ private int top; private int bottom;
	 * private int left; private int right; public PanelPadding(int top,int
	 * bottom,int left,int right) { this.top = top; this.left = left; this.right =
	 * right; this.bottom = bottom; } PanelPadding getPadding() { return this; } int
	 * getTop() { return this.top; } int getBottom() { return this.bottom; } int
	 * getLeft() { return this.left; } int getRight() { return this.right; } }
	 */

}
