package frame;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.io.File;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;

public class MainPanel extends JPanel{
	private File dataDir;
	private ListsPanel listsPanel;
	private DataTablePanel dataTablePanel;
	private PanelPos panelPos = new PanelPos(0f,0f,0f,0f);
	public MainPanel(ListsPanel listsPanel,DataTablePanel dataTablePanel) {
		//setDataDir(dataDir);
		this.listsPanel = listsPanel;
		this.dataTablePanel = dataTablePanel;
		nittyGritty();
	}
	private void nittyGritty() {
		add(listsPanel);
		add(dataTablePanel);
		setBackground(Color.LIGHT_GRAY);
		addHierarchyBoundsListener(new HierarchyBoundsListener() {

			@Override
			public void ancestorMoved(HierarchyEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void ancestorResized(HierarchyEvent e) {
				Component c = e.getChanged();
				String className = c.getClass().getSimpleName();
				if (className.equals("JLayeredPane")) {
					JLayeredPane parent = findParentPanel((JLayeredPane) c);
					if (parent == null) {
						return;
					}
					setBounds(panelPos.getPanelRect(parent.getBounds()));
				}
			}
			
		});
		setLayout(null);
	}
	
	public ListsPanel getListsPanel() {
		return this.listsPanel;
	}
	public DataTablePanel getDataTablePanel() {
		return this.dataTablePanel;
	}

	private JLayeredPane findParentPanel(JLayeredPane panel) {
		if (panel.equals(getParent())) {
			return panel;
		}
		for (Component c : panel.getComponents()) {
			String cName = c.getClass().getSimpleName();
			if (cName.equals("JPanel") && getParent().equals((JLayeredPane) c)) {
				return (JLayeredPane) c;
			}
		}
		return null;
	}
	
	
	public final static String MRL_JOB_INFO_NAME = "MRL Jobs";
	public final static String SAVED_JOBS_NAME = "Saved Jobs";
	public final static String SAVED_PROJECTS_NAME = "Saved Projects";
	
	
	
	
	
	
	
	

	private void setDataDir(File dataDir) {
		if(!dataDir.isDirectory()) {
			this.dataDir = dataDir.getParentFile();
		}else {
			this.dataDir = dataDir;
		}
	}
}
