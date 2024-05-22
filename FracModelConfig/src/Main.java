import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.ListModel;

import org.apache.commons.compress.utils.Lists;

import frame.DataTablePanel;
import frame.ListsPanel;
import frame.MainFrame;
import frame.MainPanel;
import frame.PanelPos;
import login.EncryptCredentials;
import login.UserNamePassword;
import mrl.DataHandling;
import mrl.DataRequest;
import mrl.Login;

public class Main {
	public static StartUp startUp;
	static MainFrame mainFrame;
	static Project project;
	static ExecutorService executor;
	public static void main(String...args) throws Exception {
		initProperties();
		executor = Executors.newCachedThreadPool();
		
		boolean shutdown =  startUp();
		if(shutdown) {
			return;
		}
		setMainFrame();
	}
	
	
	
	////UPDATE WITH OTHER PANELS
	///NEED TO ADD OTHER PANELS FOR FDI's/Surveys, FracModeling Configurations, Visuals
	static void setMainFrame() throws InterruptedException, ExecutionException, ClassNotFoundException, IOException {
		MainPanel mainPanel = getMainPanel();
		mainFrame = new MainFrame(mainPanel,startUp.getCookies(), Map.of()) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void dispose(){
				System.out.println(executor.shutdownNow());
				super.dispose();
				System.exit(0);
			}
		};
		mainFrame.addSaveCSVItem(MainFrame.IMPORT_MENU_NAME, DataHandling.JOB_DATA_DIR, startUp.getCachedMap());
	}
	
	
	
	static MainPanel getMainPanel() throws InterruptedException, ExecutionException, ClassNotFoundException, IOException{
		ListsPanel listsPanel = getMainListsPanel();
		DataTablePanel dataTablePanel = getMainDataTablePanel();
		return new MainPanel(listsPanel,dataTablePanel);
	}
	
	final static PanelPos MAIN_DATA_TABLE_PANEL_POS = new PanelPos(.25f,.05f,0.02f,0.25f);
	static DataTablePanel getMainDataTablePanel() {
		DataTablePanel dataTablePanel = new DataTablePanel(Main.MAIN_DATA_TABLE_NAME,MAIN_DATA_TABLE_PANEL_POS,"Loaded Data");
		dataTablePanel.setVisible(true);
		return dataTablePanel;
	}
	private static void setJobInfoAction(String cookies,Map<String,String> jobInfoMap, ListsPanel listsPanel) {
		listsPanel.addActionToButton((list)->{
			return new AbstractAction() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					List<String> selectedJobs = list.getSelectedValuesList();
					Set<String> updatedCachedJobs = getSetFromJList(listsPanel.getList(MainPanel.SAVED_JOBS_NAME));
					executor.execute(() -> {
						for(String s:selectedJobs) {
							updatedCachedJobs.add(s);
							executor.execute(()->{
								try {
									new DataRequest(cookies,
											jobInfoMap.get(s));
								} catch (IOException | InterruptedException e1) {
									e1.printStackTrace();
								}
							
							});
						}
						//listsPanel.getList(MainPanel.SAVED_JOBS_NAME).setModel(getNewModel(updatedCachedJobs));
						listsPanel.addToList(MainPanel.SAVED_JOBS_NAME, updatedCachedJobs);
					});
					/*
					 * executor.execute(()->{ while(!done[0]) { while(!dataRequestQueue.isEmpty()) {
					 * 
					 * DataRequest dataRequest = dataRequestQueue.poll(); try {
					 * Map<String,List<String>> dataMap = dataRequest.getDataMap();
					 * Map<LocalDate,Map<String,List<String>>> } catch (InterruptedException |
					 * IOException e1) { // TODO Auto-generated catch block e1.printStackTrace(); }
					 * } } });
					 */
				
				}
			};
		}, MainPanel.MRL_JOB_INFO_NAME);
	}
	private static ListModel<String> getNewModel(Set<String> set){
		DefaultListModel<String> model = new DefaultListModel<>();
		model.addAll(set);
		return model;
	}
	public static List<String> getListFromJList(JList<String> jlist){
		List<String> list = new ArrayList<>();
		for(int i = 0;i<jlist.getModel().getSize();i++) {
			list.add(jlist.getModel().getElementAt(i));
		}
		return list;
	}
	public static Set<String> getSetFromJList(JList<String> jlist){
		Set<String> set = new LinkedHashSet<>();
		for(int i = 0;i<jlist.getModel().getSize();i++) {
			set.add(jlist.getModel().getElementAt(i));
		}
		return set;
	}
	
	static void setLoadProjectAction(ListsPanel listsPanel) {
		listsPanel.addActionToButton((list)->{
			return new AbstractAction() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					executor.execute(()->{
						if(list.getSelectedValue().equals(Project.CREATE_NEW_PROJECT)) {
							try {
								project = Project.createNewProject();
								listsPanel.addToList(list.getName(), new Comparator<>() {

									@Override
									public int compare(String o1, String o2) {
										int i = (o2.equals(Project.CREATE_NEW_PROJECT)?-1:0);
										return i;
									}
									
								},project.getName());
								return;
							} catch (ClassNotFoundException | InterruptedException | ExecutionException
									| IOException e1) {
								e1.printStackTrace();
								return;
							}
						}
						try {
							project = Project.loadProject(list.getSelectedValue());
						} catch (ClassNotFoundException | IOException e1) {
							e1.printStackTrace();
							return;
						}
					});
				}
			};
		}, MainPanel.SAVED_PROJECTS_NAME);
	}
	
	final static PanelPos MAIN_LISTS_PANEL_POS = new PanelPos(0f,.75f,0f,0f);
	static void setProjectListKeyListener(ListsPanel listsPanel) {
		listsPanel.addKeyListenerToList((list)->{
			return new KeyListener() {

				@Override
				public void keyTyped(KeyEvent e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==KeyEvent.VK_DELETE){
						executor.execute(()->{
							String selected = list.getSelectedValue();
							int delete = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete project: "+selected);
							if(delete==JOptionPane.YES_OPTION) {
								Project.deleteProject(selected);
								listsPanel.removeFromList(MainPanel.SAVED_PROJECTS_NAME, selected);
								JOptionPane.showMessageDialog(null, "Project Deleted");
							}
						});
					}
					
				}

				@Override
				public void keyReleased(KeyEvent e) {
					// TODO Auto-generated method stub
					
				}
				
			};
		},MainPanel.SAVED_PROJECTS_NAME);
	}
	static ListsPanel getMainListsPanel() throws InterruptedException, ExecutionException {
		List<String> jobInfoList = startUp.getJobInfoList();
		List<String> cachedJobs = null;
		try {
			cachedJobs = startUp.getCachedList();
			
		}catch(ClassNotFoundException | IOException e) {
			System.out.println("Exception: Main.getMainListsPanel()");
			cachedJobs = new ArrayList<>();
		}
		//System.out.println("Past Exception");
		List<String> projectList = Project.getSavedProjects();
		Map<String, List<String>> listsMap = getListsMap(
				lazy(MainPanel.MRL_JOB_INFO_NAME, MainPanel.SAVED_JOBS_NAME, MainPanel.SAVED_PROJECTS_NAME),
				jobInfoList, cachedJobs, projectList);
		ListsPanel listsPanel = new ListsPanel(Main.MAIN_LISTS_PANEL_NAME,MAIN_LISTS_PANEL_POS,listsMap,1);
		setJobInfoAction(startUp.getCookies(),startUp.getJobInfoMap(),listsPanel);
		setLoadProjectAction(listsPanel);
		setProjectListKeyListener(listsPanel);
		setProjectPopupMenu(listsPanel);
		return listsPanel;
	}
	private static void setProjectPopupMenu(ListsPanel listsPanel) {
		JList<String> list = listsPanel.getList(MainPanel.SAVED_PROJECTS_NAME);
		addProjectPopupMenu(list);
	}
	private static void addProjectPopupMenu(JList<String> list) {
		JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.add(updateProjChannelsItem(list));
		popupMenu.add(updateProjWellNamesItem(list));
		popupMenu.add(updateProjOffsetWellsItem(list));
		list.setComponentPopupMenu(popupMenu);
	}
	private final static String UPDATE_OFFSETS_ITEM = "update_offsets_item";
	private static JMenuItem updateProjOffsetWellsItem(JList<String> list) {
		JMenuItem menuItem = new JMenuItem();
		menuItem.setName(UPDATE_OFFSETS_ITEM);
		menuItem.setText("Update Offset Well Names");
		menuItem.setVisible(true);
		menuItem.setEnabled(true);
		menuItem.addActionListener(new AbstractAction() {
			/**
			 * 
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				executor.execute(()->{
					String projName = list.getSelectedValue();
					int confirm = JOptionPane.showConfirmDialog(null, "Do you want to update the offset well names for project: "+projName);
					if(confirm==JOptionPane.NO_OPTION) {
						return;
					}
					Project project = null;
					try {
						project = Project.getSavedProject(projName);
						project.updateOffsetWellNames();
					} catch (ClassNotFoundException | IOException | InterruptedException | ExecutionException e1) {
						e1.printStackTrace();
						return;
					}
				});
			}
		});
		return menuItem;
	}
	private final static String UPDATE_WELLS_ITEM = "update_wells_item";
	private final static String UPDATE_CHANNELS_ITEM = "update_channels_menu_item";
	private static JMenuItem updateProjWellNamesItem(JList<String> list) {
		JMenuItem menuItem = new JMenuItem();
		menuItem.setName(UPDATE_WELLS_ITEM);
		menuItem.setText("Update Well Names");
		menuItem.setVisible(true);
		menuItem.setEnabled(true);
		menuItem.addActionListener(new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				executor.execute(()->{
					String projName = list.getSelectedValue();
					int confirm = JOptionPane.showConfirmDialog(null, "Do you want to update the well names for project: "+projName);
					if(confirm==JOptionPane.NO_OPTION) {
						return;
					}
					Project project = null;
					try {
						project = Project.getSavedProject(projName);
						project.updateWellNames();
					} catch (ClassNotFoundException | IOException | InterruptedException | ExecutionException e1) {
						e1.printStackTrace();
						return;
					}
				});
			}
		});
		return menuItem;
	}
	private static JMenuItem updateProjChannelsItem(JList<String> list) {
		JMenuItem menuItem = new JMenuItem();
		menuItem.setName(UPDATE_CHANNELS_ITEM);
		menuItem.setText("Update Project Channels");
		menuItem.setVisible(true);
		menuItem.setEnabled(true);
		menuItem.addActionListener(new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				executor.execute(()->{
					String projName = list.getSelectedValue();
					int confirm = JOptionPane.showConfirmDialog(null, "Do you want to update the channels for project: "+projName);
					if(confirm==JOptionPane.NO_OPTION) {
						return;
					}
					Project project = null;
					try {
						project = Project.getSavedProject(projName);
						project.updateChannels();
					} catch (ClassNotFoundException | IOException | InterruptedException e1) {
						e1.printStackTrace();
						return;
					}
				});
			}
		});
		return menuItem;
	}
	@SafeVarargs
	private static Map<String, List<String>> getListsMap(String[] names, List<String>... lists) {
		Map<String, List<String>> listsMap = new LinkedHashMap<>();
		int i = 0;
		for (String s : names) {
			listsMap.put(s, (i >= lists.length ? Lists.newArrayList() : lists[i]));
			i++;
		}
		return listsMap;
	}
	private static String[] lazy(String... strings) {
		return strings;
	}
	//Returns true to shutdown if not authenticated multiple times
	private static boolean startUp() throws InvalidKeyException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ShortBufferException, IOException, Exception {
		HashMap<String, String> map = EncryptCredentials.getUserCredentials(getMRLLoginFunc(),
				MRL_CREDENTIAL_PATH, MRL_CREDS_NAME);
		if(map==null) {
			return true;
		}
		System.out.println(map);
		startUp = new StartUp(map.get(EncryptCredentials.USERNAME),map.get(EncryptCredentials.PASSWORD));
		return false;
	}
	
	
	final static String IMAGE_PROPERTY = UserNamePassword.IMAGE_PROPERTY;
	
	public static void initProperties() {
		try {
			System.setProperty(IMAGE_PROPERTY, "shearfrac.png");
			System.getProperties().storeToXML(new FileOutputStream(new File("properties.xml")), "FracModel Properties");

			System.getProperties().loadFromXML(new FileInputStream(new File("properties.xml")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Function<HashMap<String, String>, Boolean> getMRLLoginFunc() throws Exception {
		return (Function<HashMap<String, String>, Boolean>) (map) -> {
			List<String> cookies;
			try {
				cookies = Login.makeSessionIDRequest(Login.MRL_LOGIN_URL,map.get(EncryptCredentials.USERNAME),map.get(EncryptCredentials.PASSWORD));
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
				return false;
			}
			return (cookies!=null);
		};
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
			row+=(","+entry.getValue().get(index));
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
	final static String MRL_CREDS_NAME = "mrl";
	
	final static String CREDENTIALS_PATH = "credentials/";
	final static String MRL_CREDENTIAL_PATH = "mrl/";
	
	final static String MAIN_LISTS_PANEL_NAME = "Main_Lists_Panel";
	final static String MAIN_DATA_TABLE_NAME = "Main_Data_Table";
	final static String FDI_TABLE = "fdi";
	final static String SURVEY_TABLE = "survey";



	final static String MIN_XF = "Minimum Xf";
	final static String XF = "Xf";
	final static String MIN_YF = "Minimum Yf";
	final static String YF = "Yf";
	final static String MIN_VFR = "Minimum VFR %";
	final static String VFR_HIGH = "High VFR %";
	final static String VFR_HIGH_MULT = "High VFR Multiplier";
	final static String VFR_LOW = "Low VFR %";
	final static String VFR_LOW_MULT = "Low VFR Multiplier";
	
	
	

}
