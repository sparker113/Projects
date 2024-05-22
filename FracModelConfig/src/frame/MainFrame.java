package frame;

import java.awt.Color;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;

import mrl.DataHandling;
import mrl.JobRequest;

public class MainFrame extends JFrame {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	Color color = Color.LIGHT_GRAY;
	DataTables<String, String>[] dataTables;
	Map<String, JPanel> panelMap;
	ExecutorService executor;
	MainPanel mainPanel;
	String cookies;

	@Deprecated
	@SafeVarargs
	public MainFrame(OptionPanel optionPanel, ParamPanel paramPanel, DataTables<String, String>... dataTables) {
		nittyGritty();
		addDataTables(getDataTableBounds(optionPanel), dataTables);
		add(optionPanel);
		add(paramPanel);
	}

	public MainFrame(MainPanel mainPanel,String cookies, Map<String, JPanel> panelMap) {
		this.panelMap = panelMap;
		this.mainPanel = mainPanel;
		this.cookies = cookies;
		executor = Executors.newCachedThreadPool();
		nittyGritty();
	}

	public MainFrame(MainPanel mainPanel,String cookies, String[] panelNames, JPanel... panels) {
		this.panelMap = constrPanelMap(panelNames, panels);
		this.mainPanel = mainPanel;
		this.cookies = cookies;
		executor = Executors.newCachedThreadPool();
		nittyGritty();
	}

	void nittyGritty() {
		setBounds(getMaxWindowBounds());
		setLayeredContentPane();
		// setLayout(null);
		addMainPanel();
		addPanels();
		setBackground(getFrameColor());
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		constrMenuBar();
		setVisible(true);
	}

	void addMainPanel() {
		mainPanel.setBounds(getBounds());
		getContentPane().add(mainPanel);
		mainPanel.setEnabled(true);
		mainPanel.setVisible(true);
	}

	void addPanels() {

		for (JPanel panel : panelMap.values()) {
			panel.setBounds(getBounds());
			getContentPane().add(panel);
			panel.setEnabled(false);
			panel.setVisible(false);
		}
	}

	Color getFrameColor() {
		float[] hsb = new float[3];
		hsb = Color.RGBtoHSB(220, 230, 229, hsb);
		return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
	}

	private final static String MENU_BAR_NAME = "menu_bar";

	private void constrMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.setName(MENU_BAR_NAME);
		menuBar.setBorderPainted(true);
		menuBar.setBackground(Color.LIGHT_GRAY);
		menuBar.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		menuBar.add(getPanelMenu());
		menuBar.add(getImportOptionMenu());
		setJMenuBar(menuBar);
	}

	public final static String IMPORT_MENU_NAME = "import_menu";

	private JMenu getImportOptionMenu() {
		JMenu menu = getTemplateMenu("Import", IMPORT_MENU_NAME);
		menu.add(getImportJobItem());
		return menu;
	}

	private final static String IMPORT_JOBS_ITEM_NAME = "import_jobs_menu_item";
	public void addSaveCSVItem(String menuName,String dataDir,Map<String,String> jobIDMap) {
		JMenuItem menuItem = new JMenuItem("Save Job CSV");
		menuItem.setName(IMPORT_JOBS_ITEM_NAME);
		menuItem.setHorizontalAlignment(JMenuItem.LEFT);
		menuItem.setVerticalAlignment(JMenuItem.CENTER);
		menuItem.addActionListener(new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				executor.execute(()->{
					@SuppressWarnings("unchecked")
					JList<String> list = mainPanel.getListsPanel().getList(MainPanel.SAVED_JOBS_NAME);
					String jobDisplayName = list.getSelectedValue();
					String id = jobIDMap.get(jobDisplayName);
					File[] dataFiles = getDataFiles(dataDir,id);
					try {
						writeDataFilesToCSV(dataFiles);
					} catch (ClassNotFoundException | IOException e1) {
						e1.printStackTrace();
					}
				});
			}
		});
		menuItem.setEnabled(true);
		menuItem.setVisible(true);
		JMenu menu = (JMenu)GUIUtilities.getMenuInMenuBar(getJMenuBar(), menuName);
		menu.add(menuItem);
	}
	@SuppressWarnings("unchecked")
	public void writeDataFilesToCSV(File[] dataFiles) throws ClassNotFoundException, IOException {
		for(File file:dataFiles) {
			executor.execute(()->{
				Map<String, List<String>> dataMap = null;
				try {
					
					dataMap = DataHandling.readObjFromFile(HashMap.class, file);
					saveDataToCSV(dataMap,file.getAbsolutePath()+".csv");
				} catch (ClassNotFoundException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		}
	}
	private File[] getDataFiles(String dataDir,String id) {
		File file = new File(getDataFileDir(dataDir,id));
		return file.listFiles();
	}
	private String getDataFileDir(String dataDir,String id) {
		return dataDir+"/"+id+"/";
	}
	private static int getListSize(Map<String,List<String>> map) {
		for(String s:map.keySet()) {
			return map.get(s).size();
		}
		return 0;
	}
	public static void saveDataToCSV(Map<String,List<String>> dataMap,String filePath) throws IOException{
		FileOutputStream fileOutputStream = new FileOutputStream(new File(filePath));
		writeHeadersToCSV(dataMap.keySet(),fileOutputStream);
		for(int i = 0;i<getListSize(dataMap);i++) {
			fileOutputStream.write(getCSVRow(dataMap,i).getBytes());
		}
		fileOutputStream.close();
	}

	private static String getCSVRow(Map<String,List<String>> map,int index) {
		String row = "";
		for(Map.Entry<String,List<String>> entry:map.entrySet()) {
			String value = (index>=entry.getValue().size()?"0":entry.getValue().get(index));
			row+=(","+value);
		}
		row=row.substring(1)+"\n";
		return row;
	}
	private static void writeHeadersToCSV(Set<String> headers,FileOutputStream fileOutputStream) throws IOException {
		String row = "";
		for(String s:headers) {
			row+=(","+s);
		}
		row=row.substring(1)+"\n";
		fileOutputStream.write(row.getBytes());
	}
	private JMenuItem getImportJobItem() {
		JMenuItem menuItem = new JMenuItem("View Older Jobs");
		menuItem.setName(IMPORT_JOBS_ITEM_NAME);
		menuItem.setHorizontalAlignment(JMenuItem.LEFT);
		menuItem.setVerticalAlignment(JMenuItem.CENTER);
		menuItem.addActionListener(new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				executor.execute(()->{
					Integer[] options = getNumJobsOptArr();
					Integer startIndex = Integer.valueOf(String.valueOf(JOptionPane.showInputDialog(null, "Job Import",
							"Select Starting Index of MRL Job List for Import", JOptionPane.DEFAULT_OPTION, null, options,
							options[0])));
					ListsPanel listsPanel = mainPanel.getListsPanel();
					
					try {
						addJobsToSavedList(listsPanel,startIndex);
					} catch (IOException | InterruptedException | ExecutionException e1) {
						e1.printStackTrace();
					}
				});
			}
		});
		menuItem.setEnabled(true);
		menuItem.setVisible(true);
		return menuItem;
	}
	private void addJobsToSavedList(ListsPanel listsPanel,Integer jobStartIndex) throws IOException, InterruptedException, ExecutionException {
		JobRequest jobRequest = new JobRequest(cookies);
		jobRequest.addJobs(jobStartIndex);
		jobRequest.requestJobInfo(false);
		listsPanel.addToList(MainPanel.MRL_JOB_INFO_NAME, jobRequest.getJobInfoList());
	}
	private final static int MAX_NUM_JOBS = 600;

	private static Integer[] getNumJobsOptArr() {
		Integer[] numJobOptions = new Integer[11];
		int index = 0;
		for (int i = JobRequest.DEFAULT_NUM_JOBS; i <= MAX_NUM_JOBS; i += JobRequest.JOB_REQUEST_INTERVAL) {
			numJobOptions[index] = i;
			index++;
		}
		return numJobOptions;
	}

	private void addImportMenuItems(JMenu importMenu) {

	}

	private JMenu getTemplateMenu(String text, String name) {
		JMenu menu = new JMenu(text);
		menu.setName(name);
		menu.setHorizontalAlignment(JMenu.LEFT);
		menu.setVerticalAlignment(JMenu.CENTER);
		menu.setBorder(BorderFactory.createEtchedBorder());
		return menu;
	}

	private final static String PANEL_MENU_NAME = "panel_menu";

	private JMenu getPanelMenu() {
		JMenu menu = getTemplateMenu("View", PANEL_MENU_NAME);
		addMenuItemsToView(menu);
		menu.setVisible(true);
		return menu;
	}

	private void setLayeredContentPane() {
		JLayeredPane layeredPane = new JLayeredPane();

		setContentPane(layeredPane);
	}

	private void addMenuItemsToView(JMenu menu) {
		menu.add(getViewMenuItem(mainPanel.getName(), mainPanel));
		for (Map.Entry<String, JPanel> entry : panelMap.entrySet()) {
			menu.add(getViewMenuItem(entry.getKey(), entry.getValue()));
		}
	}

	private JMenuItem getViewMenuItem(String panelName, JPanel panel) {
		JMenuItem menuItem = new JMenuItem(panelName);
		menuItem.setName(panelName);
		menuItem.addActionListener(getSwitchPanelAction(panelName, panel));
		menuItem.setHorizontalTextPosition(JMenuItem.CENTER);
		menuItem.setVerticalTextPosition(JMenuItem.CENTER);
		menuItem.setEnabled(true);
		menuItem.setVisible(true);
		return menuItem;
	}

	private void hideCurrentPanel() {
		JLayeredPane contentPane = (JLayeredPane) getContentPane();
		System.out.println(contentPane.getComponentCount());
		for (int i = 0; i < contentPane.getComponentCount(); i++) {
			Component c = contentPane.getComponent(i);
			String name = c.getName();
			if (name == null) {
				continue;
			} else if (panelMap.containsKey(name) && contentPane.getLayer(c) == JLayeredPane.PALETTE_LAYER) {
				contentPane.setLayer(c, JLayeredPane.DEFAULT_LAYER);
				c.setVisible(false);
				c.setEnabled(false);
				break;
			}

		}
	}

	private AbstractAction getSwitchPanelAction(String panelName, JPanel panel) {
		return new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				executor.execute(() -> {
					hideCurrentPanel();
					JLayeredPane contentPane = (JLayeredPane) getContentPane();
					contentPane.setLayer(panel, JLayeredPane.PALETTE_LAYER);
					panel.setVisible(true);
					panel.setEnabled(true);
					System.out.println(panel.getName() + " - " + panel.isVisible() + " - " + panel.getBounds());
					System.out.println("Content Pane Bounds: " + contentPane.getBounds());
					System.out.println("Panel Layer: " + contentPane.getLayer(panel));
				});
			}
		};
	}

	public static Map<String, JPanel> constrPanelMap(String[] panelNames, JPanel... panels) {
		Map<String, JPanel> map = new LinkedHashMap<>();
		if (panelNames.length != panels.length) {
			return null;
		}
		for (int i = 0; i < panelNames.length; i++) {
			map.put(panelNames[i], panels[i]);
		}
		return map;
	}

	public DataTables<String, String>[] getDataTables() {
		return this.dataTables;
	}

	public DataTables<String, String> getDataTable(String name) {
		for (DataTables<String, String> table : getDataTables()) {
			if (name.equals(table.getDataName())) {
				return table;
			}
		}
		return null;
	}

	public boolean checkForDataTable(String name) {
		for (DataTables<String, String> table : getDataTables()) {
			if (name.equals(table.getDataName())) {
				return true;
			}
		}
		return false;
	}

	public Map<String, Map<String, List<String>>> getTableData(String name) {
		DataTables<String, String> table = getDataTable(name);
		if (table == null) {
			return null;
		}
		return table.getDataMap();
	}

	Rectangle getDataTableBounds(OptionPanel optionPanel) {
		Rectangle rectangle = optionPanel.getBounds();
		Rectangle dataTableRect = new Rectangle(0, rectangle.height, getBounds().width,
				getBounds().height - rectangle.height);
		return dataTableRect;
	}

	public final static String DATA_PANEL_NAME = "data_panel";

	@SuppressWarnings("unchecked")
	void addDataTables(Rectangle rectangle, DataTables<String, String>... dataTables) {
		JPanel panel = getDataPanel(rectangle, color);
		for (DataTables<String, String> tables : dataTables) {
			panel.add(tables);
		}
		panel.setVisible(true);
		add(panel);
	}

	JPanel getDataPanel(Rectangle rectangle, Color color) {
		JPanel panel = new JPanel();
		panel.setName(DATA_PANEL_NAME);
		panel.setBounds(rectangle);
		panel.setBackground(color);
		panel.setLayout(new GridLayout());
		return panel;
	}

	public static Rectangle getMaxWindowBounds() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
	}

}
