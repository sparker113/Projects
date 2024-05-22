import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.sql.Wrapper;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.compress.utils.Lists;

import checkbox.CheckBox;
import checkbox.CheckBoxFrame;
import checkbox.CheckBoxPanel;
import exceltransfer.DataNames;
import frame.DataTablePanel;
import frame.DataTables;
import frame.GUIUtilities;
import frame.ListsPanel;
import frame.MainPanel;
import frame.OptionPanel;
import frame.PanelPos;
import frame.ParamPanel;
import graphpanel.PlotPanel;
import mrl.DataHandling;
import mrl.DataRequest;
import mrl.JobRequest;
import mrl.Login;

public class TestClass {
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {

		/*
		 * Executors.newSingleThreadExecutor().execute(()->{
		 * Map<String,ArrayList<String>> map = null; try { map =
		 * ImportData.readFileData(ImportData.selectFile(),new String());
		 * System.out.println(map); } catch (Exception e) { // TODO Auto-generated catch
		 * block e.printStackTrace(); } });
		 */
		// textDataTables();
		// 220,230,229
		// printColors();
		// StartUp startUp = new StartUp("s.parker@shearfrac.com", "FracShear.1");
		// MapChannels mapChannels = new MapChannels(getSetOfHeaders(),"Sam");
		// Map<String,Map<String,String>> jobInfoMap = startUp.getJobInfo();
		// hold();

		// LocalDateTime localDateTime = LocalDateTime.parse(string);
		// Well well1 = DataHandling.readObjFromFile(Well.class,new File("well1.obj"));
		// Well well2 = DataHandling.readObjFromFile(Well.class, new File("well2.obj"));



		/*
		 * Well well = DataHandling.readObjFromFile(Well.class, new File("well3.obj"));
		 * Well well2 = DataHandling.readObjFromFile(Well.class, new File("well1.obj"));
		 * Double x1 = well.getWellSurvey().getXCoord(); Double x2 =
		 * well.getWellSurvey().getXCoord(); System.out.println("x1: "+x1);
		 * System.out.println("x2: "+x2); Double y1 = well.getWellSurvey().getYCoord();
		 * Double y2 = well2.getWellSurvey().getYCoord();
		 * System.out.println("X-Distance: "+Math.abs(x1-x2));
		 * System.out.println("Y-Distance: "+Math.abs(y1-y2));
		 * System.out.println("Latitudes: "+well.getWellSurvey().getLatitude()+" - "
		 * +well2.getWellSurvey().getLatitude());
		 * System.out.println("Longitudes: "+well.getWellSurvey().getLongitude()+" - "
		 * +well2.getWellSurvey().getLongitude());
		 */
		/*
		 * for (int i = 2; i <=
		 * well1.getWellPerfs().getDepthList(Perfs.DEPTHS.PLUG_DEPTH).size(); i++) {
		 * System.out.println(well1.findVerticalDistFromWells(i,well2,well3));
		 * System.out.println(well1.findPerpDistFromWells(i, well2,well3)); }
		 */
		//saveWellTests();
		// System.out.println(survey.getSurveyData());
		// System.out.println("Sam");
		/*
		 * System.out.println(GUIUtilities.
		 * getDialog("Select jobs whose data you want to include in the new project",
		 * StartUp.getCachedJobsFromDir().keySet()).get());
		 */

		// MapChannels mapChannels = new MapChannels();
		// Project project = Project.createNewProject();
		/*
		 * String filePath = ImportData.selectFile(); Map<String, LinkedHashMap<String,
		 * ArrayList<String>>> fdiMap = ImportData.readFracBrainFDIs(filePath,
		 * "pressureUnits", "proppantUnits", "rateUnits");
		 */
		// textDataTables();
		// holdCode();
		// listsPanelTest();
		/*
		 * Map<LocalDate,Map<String,List<String>>> jobDataMap =
		 * DataRequest.readJobDataFromDir("4794");
		 * jobDataMap.keySet().iterator().forEachRemaining((LocalDate string)->{
		 * System.out.println(string+" - "+jobDataMap.get(string).size()); });
		 */
		// MapChannels mapChannels = new MapChannels(getSetOfHeaders(),"jobName");
		// holdCode();
		// JobRequest.makeJobListRequest(login.getCookieString());
	}
	public static void saveWellTests() throws Exception{
		Survey survey1 = Survey
				.getSurveyInst("C:\\Users\\swppa\\Documents\\ShearFrac\\Job_Files\\Arrington\\Bulldog H\\130");
		Perfs perfs1 = Perfs
				.getPerfsInst("C:\\Users\\swppa\\Documents\\ShearFrac\\Job_Files\\Arrington\\Bulldog H\\130");
		Well well1 = new Well("135WA", survey1, perfs1);
		Survey survey2 = Survey
				.getSurveyInst("C:\\Users\\swppa\\Documents\\ShearFrac\\Job_Files\\Arrington\\Bulldog H\\130");
		Perfs perfs2 = Perfs
				.getPerfsInst("C:\\Users\\swppa\\Documents\\ShearFrac\\Job_Files\\Arrington\\Bulldog H\\130");
		Well well2 = new Well("130WB", survey2, perfs2);
		Survey survey3 = Survey
				.getSurveyInst("C:\\Users\\swppa\\Documents\\ShearFrac\\Job_Files\\Arrington\\Bulldog H");
		Perfs perfs3 = Perfs.getPerfsInst("C:\\Users\\swppa\\Documents\\ShearFrac\\Job_Files\\Arrington\\Bulldog H");
		Well well3 = new Well("160LS", survey3, perfs3);

		DataHandling.writeObjToFile(well1, "well1.obj");
		DataHandling.writeObjToFile(well2, "well2.obj");
		DataHandling.writeObjToFile(well3, "well3.obj");
		List<Well> wells = new ArrayList<>();
		wells.add(well1);
		wells.add(well2);
		wells.add(well3);
		for (Well well : wells) {
			for (int i = 1; i <= well.getWellPerfs().getDepthList(Perfs.DEPTHS.BOTTOM_PERF).size(); i++) {
				System.out.println(well.findVerticalDistFromWells(i, wells));
				System.out.println(well.findPerpDistFromWells(i, wells));
			}
		}
	}
	public static int getListSize(Map<String, List<String>> map) {
		int min = -1;
		for (String s : map.keySet()) {
			if (min == -1) {
				min = map.get(s).size();
				continue;
			}
			min = map.get(s).size() < min ? map.get(s).size() : min;
		}
		return min;
	}

	public static String getCSVHeaders(Set<String> headers) {
		String headerString = "";
		for (String s : headers) {
			headerString += "," + s;
		}
		headerString = headerString.substring(1);
		headerString += "\n";
		return headerString;
	}

	public static void stageSplittingCode() throws InterruptedException, ExecutionException {
		Project project = null;
		try {
			project = Project.loadProject("Samuel Wayne Parker");
			FracData fracData = new FracData(project);
			fracData.structByWellAndStage();
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String projectName = "Samuel Wayne Parker";
		String filePath = Project.getFileDirForStages(projectName);
		File file = new File(filePath);
		Map<String, String> channelMap = project.getDataChannels()
				.getChannelMap(MapChannels.DataChannels.FRAC_CHANNELS);
		String[] keysToKeep = getKeysToKeep(channelMap);
		ExecutorService executor = Executors.newFixedThreadPool(7);
		for (File f : file.listFiles()) {
			executor.execute(() -> {
				try {
					Map<String, List<String>> dataMap = (Map<String, List<String>>) DataHandling
							.readObjFromFile(HashMap.class, f);
					removeAllBut(dataMap, keysToKeep);
					String plotPath = Project.getFilePathForStagePlots(projectName,
							f.getName().replace(Project.DATA_EXT, ""));
					plotArrays(dataMap, plotPath, channelMap);
				} catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
					return;
				}
			});
		}
	}

	public static String formatDateTimeForFile(String dateTime) {
		Matcher matcher = Pattern.compile("(\\d+)").matcher(dateTime);
		while (matcher.find()) {
			System.out.println(matcher.group());
		}
		return "1970-01-01 00:00:00";
	}

	private static String[] getKeysToKeep(Map<String, String> channelMap) {
		String[] str = new String[3];
		str[0] = channelMap.get(MapChannels.TREATING_PRESSURE);
		str[1] = channelMap.get(MapChannels.SLURRY_RATE);
		str[2] = channelMap.get(MapChannels.PROPPANT_CONC);
		return str;

	}

	private static Map<String, List<String>> removeAllBut(Map<String, List<String>> dataMap, String... exclude) {
		Set<String> set = Set.copyOf(dataMap.keySet());
		for (String s : set) {
			if (!arrContains(exclude, s)) {
				dataMap.remove(s);
			}
		}
		return dataMap;
	}

	private static boolean arrContains(String[] arr, String strComp) {
		for (String s : arr) {
			if (s.toLowerCase().equals(strComp.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	private static List<LocalDate> sortSetChrono(Set<LocalDate> set) {
		List<LocalDate> list = new ArrayList<>();
		list.addAll(set);
		list.sort(new Comparator<>() {

			@Override
			public int compare(LocalDate o1, LocalDate o2) {
				// TODO Auto-generated method stub
				return o1.compareTo(o2);
			}

		});
		return list;

	}

	public static HashMap<String, Integer> getPlotPanelMap() {
		HashMap<String, Integer> map = new HashMap<>();
		map.put("aa", PlotPanel.RED);
		map.put("aaa", PlotPanel.GREEN);
		map.put("aaaa", PlotPanel.BLUE);
		return map;
	}

	public static Integer getFirstUnusedColor(ArrayList<Integer> usedColors, ArrayList<Integer> decColors) {
		ArrayList<Integer> allColors = new ArrayList<>();
		allColors.addAll(usedColors);
		allColors.addAll(decColors);
		Collections.sort(allColors);
		Integer cInt = allColors.size() > 0 ? min(allColors) : 1;

		do {
			cInt++;
		} while (allColors.contains(cInt));
		return cInt;
	}

	private static Integer min(ArrayList<Integer> array) {
		Integer min = array.get(0);
		for (Integer i : array) {
			min = i < min ? i : min;
		}
		return min;
	}

	public static int getYAxisForPlot(String dataName) {
		switch (dataName) {
		case (MapChannels.TREATING_PRESSURE):
			return 10000;
		case (MapChannels.SLURRY_RATE):
			return 200;
		case (MapChannels.PROPPANT_CONC):
			return 20;
		default:
			return 10000;
		}
	}

	public static int getColorForPlot(String dataName) {
		switch (dataName) {
		case (MapChannels.TREATING_PRESSURE):
			return PlotPanel.RED;
		case (MapChannels.SLURRY_RATE):
			return PlotPanel.BLUE;
		case (MapChannels.PROPPANT_CONC):
			return PlotPanel.GREEN;
		default:
			return 10000;
		}
	}

	public static Map<String, String> flipKeyValues(Map<String, String> map) {
		Map<String, String> flipped = new HashMap<>();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			flipped.put(entry.getValue(), entry.getKey());
		}
		return flipped;
	}

	public static void plotArrays(Map<String, List<String>> arrayMap, String fileName, Map<String, String> channelMap)
			throws IOException {
		graphpanel.AxisPanel axisPanel = new graphpanel.AxisPanel(GUIUtilities.getScreenWidth() - 100,
				GUIUtilities.getScreenHeight() - 100);
		graphpanel.PlotPanel<String> plotPanel = new graphpanel.PlotPanel<>();
		HashMap<String, Integer> plotPanelMap = getPlotPanelMap();
		ArrayList<Integer> usedColors = new ArrayList<>();
		int[] yScale = new int[3];
		int i = 0;
		channelMap = flipKeyValues(channelMap);
		for (String s : arrayMap.keySet()) {
			if (plotPanelMap.keySet().contains(s)) {
				plotPanel.addDataArray(arrayMap.get(s), plotPanelMap.get(s), 1);
				continue;
			}
			yScale[i] = getYAxisForPlot(channelMap.get(s));
			Integer cInt = getFirstUnusedColor(usedColors, FracCalculations.getArrayOfTValues(plotPanelMap));
			usedColors.add(cInt);
			plotPanel.addDataArray(arrayMap.get(s), getColorForPlot(channelMap.get(s)), 1);
		}

		axisPanel.addPlotPanel(plotPanel);
		JFrame frame = new JFrame();
		frame.setBounds(GUIUtilities.getCenterRectangle(0f, 0f));
		frame.add(axisPanel);
		frame.setVisible(true);
		saveImage(new File(fileName), axisPanel, GUIUtilities.getScreenWidth(), GUIUtilities.getScreenHeight());
		frame.dispose();
	}

	public static void saveImage(File file, JPanel panel, int width, int height) throws IOException {
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bufferedImage.createGraphics();
		panel.paintAll(g);
		g.dispose();
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		ImageIO.write(bufferedImage, "png", file);
	}

	private static void hold() throws Exception {
		String fileName = "C:\\FracModelConfig\\data\\job_data\\4872\\";
		File file = new File(fileName);
		File file2 = new File(fileName + "\\csv\\");
		file2.mkdir();
		for (File f : file.listFiles()) {
			if (f.getAbsolutePath().matches(".+?\\.csv")) {
				writeFileToPath(f, file2.getAbsolutePath() + "\\" + f.getName() + ".csv");
				f.delete();
			}
		}
	}

	private static void writeFileToPath(File file, String path) throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(path);
		FileInputStream fileInputStream = new FileInputStream(file);
		byte[] bytes = new byte[2048];
		int i;
		while ((i = fileInputStream.read(bytes)) > -1) {
			fileOutputStream.write(bytes);
		}
		fileOutputStream.close();
		fileInputStream.close();
	}

	private static void setJobInfoAction(String cookies, Map<String, String> jobInfoMap, ListsPanel listsPanel) {
		listsPanel.addActionToButton((list) -> {
			return new AbstractAction() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					Executor executor = Executors.newCachedThreadPool();
					List<String> selectedJobs = list.getSelectedValuesList();
					List<String> updatedCachedJobs = getListFromJList(listsPanel.getList(MainPanel.SAVED_JOBS_NAME));
					executor.execute(() -> {
						for (String s : selectedJobs) {
							updatedCachedJobs.add(s);
							executor.execute(() -> {
								try {
									new DataRequest(cookies, jobInfoMap.get(s));
								} catch (IOException | InterruptedException e1) {
									e1.printStackTrace();
								}

							});
						}
						listsPanel.getList(MainPanel.SAVED_JOBS_NAME).setModel(getNewModel(updatedCachedJobs));
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

	private static ListModel<String> getNewModel(List<String> list) {
		DefaultListModel<String> model = new DefaultListModel<>();
		model.addAll(list);
		return model;
	}

	private static List<String> getListFromJList(JList<String> jlist) {
		List<String> list = new ArrayList<>();
		for (int i = 0; i < jlist.getModel().getSize(); i++) {
			list.add(jlist.getModel().getElementAt(i));
		}
		return list;
	}

	private static String[] lazy(String... strings) {
		return strings;
	}

	private static Map<String, List<String>> getListsMap(String[] names, List<String>... lists) {
		Map<String, List<String>> listsMap = new LinkedHashMap<>();
		int i = 0;
		for (String s : names) {
			listsMap.put(s, (i >= lists.length ? Lists.newArrayList() : lists[i]));
			i++;
		}
		return listsMap;
	}

	private static PanelPos listsPanelPos() {
		PanelPos panelPos = new PanelPos(0.05f, .8f, .4f, .4f);
		return panelPos;
	}

	private static ListsPanel getListsPanel(Map<String, List<String>> listsMap) {

		return null;
	}

	private static Map<String, JPanel> getPanelMap() {
		Map<String, JPanel> map = new LinkedHashMap<>();
		map.put("panel1", getAPanel2("panel1", Color.BLACK));
		map.put("panel2", getAPanel("panel2", Color.BLUE));
		map.put("panel3", getAPanel("panel3", Color.PINK));
		return map;
	}

	private static JPanel getAPanel(String name, Color color) {
		JPanel panel = new JPanel();
		panel.setName(name);
		panel.setBackground(color);
		return panel;
	}

	private static JPanel getAPanel2(String name, Color color) {
		JPanel panel = new JPanel();
		panel.setName(name);
		panel.setBackground(color);
		panel.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				System.out.println(name + " - Acknowledged Click");
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}

		});
		return panel;
	}

	private static void listsPanelTest() {
		JFrame frame = new JFrame();
		frame.setLayout(null);
		frame.setBounds(GUIUtilities.getCenterRectangle(0f));

		ListsPanel listPanel = new ListsPanel("Sam", new PanelPos(.05f, .75f, .25f, .2f), getListMap(), 1);

		listPanel.addActionToButton((list) -> {
			return new AbstractAction() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					ExecutorService executor = Executors.newSingleThreadExecutor();
					executor.execute(() -> {
						System.out.println("Button Pressed");
					});

				}
			};
		}, "List1", "Load Data");
		frame.add(listPanel);

		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	private static Map<String, List<String>> getListMap() {
		Map<String, List<String>> map = new LinkedHashMap<>();
		map.put("List1", getRandomList(50));
		map.put("List2", getRandomList(50));
		map.put("List3", getRandomList(25));
		map.put("List4", getRandomList(100));
		map.put("List5", getRandomList(200));
		return map;
	}

	private static List<String> getRandomList(int size) {
		List<String> list = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			int sec = LocalDateTime.now().getSecond();
			long factor = (sec == 0 ? 0 : System.currentTimeMillis() / sec);
			list.add(String.valueOf((System.nanoTime() - factor) % 10000l));
		}
		return list;
	}

	private static void printColors() {
		JColorChooser colorChooser = new JColorChooser();
		for (Component c : colorChooser.getComponents()) {

			c.addMouseListener(new MouseListener() {

				@Override
				public void mouseClicked(MouseEvent e) {
					System.out.println(colorChooser.getColor().getRed() + "," + colorChooser.getColor().getGreen() + ","
							+ colorChooser.getColor().getBlue() + "," + colorChooser.getColor().getAlpha());
					float[] rgb = new float[3];
					colorChooser.getColor().getRGBColorComponents(rgb);
					float[] hsb = new float[3];
					hsb = Color.RGBtoHSB((int) rgb[0], (int) rgb[1], (int) rgb[2], hsb);
					System.out.println("hue: " + hsb[0] + "\tsaturation: " + hsb[1] + "\tbrightness: " + hsb[2]);
				}

				@Override
				public void mousePressed(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseReleased(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseEntered(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseExited(MouseEvent e) {
					// TODO Auto-generated method stub

				}

			});
		}
		;
		colorChooser.setBounds(GUIUtilities.getCenterRectangle(.25f));
		colorChooser.setVisible(true);
		JFrame jFrame = new JFrame();
		jFrame.setBounds(GUIUtilities.getCenterRectangle(.1f));
		jFrame.add(colorChooser);
		jFrame.setVisible(true);

	}

	private static void holdCode1() throws Exception {
		Map<String, Map<String, String>> jobInfo = getJobInfo();
		JList<String> list = new JList(jobInfo.keySet().toArray());
		list.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				System.out.println(e.getFirstIndex());
				System.out.println(e.getLastIndex());
				System.out.println(list.getSelectedValue());
				for (String s : list.getSelectedValuesList()) {
					System.out.print(s + ",");
				}
			}
		});
		list.setBounds(GUIUtilities.getCenterRectangle(.25f));
		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		JFrame jFrame = new JFrame();
		jFrame.add(scrollPane);
		jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		jFrame.setBounds(GUIUtilities.getCenterRectangle(.1f));
		jFrame.setVisible(true);
	}

	private static Set<String> getSetOfHeaders() {
		Set<String> set = new LinkedHashSet<>();
		for (String s : DataNames.getDataNames()) {
			set.add(s);
		}
		return set;
	}

	private static List<LocalDate> getChronoDataDates(String[] dateFileNames) {
		List<LocalDate> dateList = new ArrayList<>();
		for (String fileName : dateFileNames) {
			dateList.add(LocalDate.parse(getFileWOExt(fileName)));
		}
		dateList.sort(new Comparator<LocalDate>() {

			@Override
			public int compare(LocalDate o1, LocalDate o2) {
				int bea = !o1.isBefore(o2) ? (!o1.isAfter(o2) ? 0 : 1) : -1;
				return bea;
			}
		});
		return dateList;
	}

	private static String getFileWOExt(String fileName) {
		Matcher matcher = Pattern.compile("(.+?)\\..+?$").matcher(fileName);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return fileName;
	}

	private static Map<String, Map<String, String>> getJobInfo() throws Exception {
		List<String> cookies = Login.makeSessionIDRequest(Login.MRL_LOGIN_URL, "s.parker@shearfrac.com", "FracShear.1");
		Login login = new Login(cookies);

		LocalDateTime start = LocalDateTime.now();

		String jobResponse = JobRequest.makeJobListRequestForString(login.getCookieString());
		JobRequest info = new JobRequest(login.getCookieString());
		info.addJobs(0);
		info.addJobs(50);
		info.requestJobInfo();
		Map<String, Map<String, String>> infoMap = info.getJobInfoMap();
		return infoMap;
	}

	private static void holdCode() throws Exception {
		List<String> cookies = Login.makeSessionIDRequest(Login.MRL_LOGIN_URL, "s.parker@shearfrac.com", "FracShear.1");
		Login login = new Login(cookies);

		LocalDateTime start = LocalDateTime.now();

		String jobResponse = JobRequest.makeJobListRequestForString(login.getCookieString());
		JobRequest info = new JobRequest(login.getCookieString());
		info.addJobs(0);
		info.addJobs(50);
		info.requestJobInfo();
		Map<String, Map<String, String>> infoMap = info.getJobInfoMap();
		LocalDateTime end = LocalDateTime.now();

		System.out.println("Execution Duration (InputStream): " + Duration.between(start, end).getNano());
		ExecutorService executor = Executors.newCachedThreadPool();

		List<String> selectedJobs = selectJobs(infoMap);

		Queue<DataRequest> dataRequests = new LinkedBlockingQueue<>();
		CountDownLatch latch = new CountDownLatch(selectedJobs.size());
		for (String s : selectedJobs) {
			executor.execute(() -> {
				try {
					DataRequest dataRequest = new DataRequest(login.getCookieString(),
							infoMap.get(s).get(JobRequest.JobInformation.JOB_INDEX));
					dataRequests.add(dataRequest);
					latch.countDown();
				} catch (IOException | InterruptedException e) {
					latch.countDown();
					e.printStackTrace();
				}
			});
		}

		latch.await();
		executor.shutdown();

	}

	private static List<String> selectJobs(Map<String, Map<String, String>> jobInfoMap) throws InterruptedException {
		CheckBoxPanel[] checkBoxPanels = new CheckBoxPanel[jobInfoMap.size()];
		int i = 0;
		for (Map.Entry<String, Map<String, String>> entry : jobInfoMap.entrySet()) {
			String label = entry.getKey() + " - " + entry.getValue().get(JobRequest.JobInformation.LOCATION) + " - "
					+ entry.getValue().get(JobRequest.JobInformation.STAGE);
			checkBoxPanels[i] = new CheckBoxPanel(label, entry.getKey(), new CheckBox(label), 500, 25);
			i++;
		}
		CheckBoxFrame checkBoxFrame = new CheckBoxFrame(checkBoxPanels);
		return checkBoxFrame.getSelections();
	}

	private static DataTablePanel getDataTablePanel(String panelName, PanelPos panelPos, Rectangle rectangle,
			String... dataTableNames) {
		DataTablePanel dataTablePanel = new DataTablePanel(panelName, panelPos, dataTableNames);
		dataTablePanel.setBounds(rectangle);
		dataTablePanel.setVisible(true);
		return dataTablePanel;
	}

	private static void textDataTables() throws Exception {
		JFrame jframe = new JFrame();
		jframe.setBounds(0, 0, GUIUtilities.getScreenWidth(), GUIUtilities.getScreenHeight() - 45);
		jframe.setLayout(null);
		DataTablePanel dataTablePanel = getDataTablePanel("Test_Panel", new PanelPos(.05f, .05f, .05f, .05f),
				new Rectangle(0, 50, GUIUtilities.getScreenWidth(), GUIUtilities.getScreenHeight() - 135), "FDI_Table",
				"Survey Table");
		jframe.add(dataTablePanel);

		/*
		 * DataTables<String, String> dataTable = new DataTables<>("FDI", new
		 * Rectangle(0, 50, (jframe.getBounds().width / 2) - 8,
		 * jframe.getBounds().height - 135)); dataTable.addHierarchyBoundsListener(new
		 * HierarchyBoundsListener() {
		 * 
		 * @Override public void ancestorMoved(HierarchyEvent e) { // TODO
		 * Auto-generated method stub
		 * 
		 * }
		 * 
		 * @Override public void ancestorResized(HierarchyEvent e) {
		 * Executors.newSingleThreadExecutor().execute(()->{
		 * dataTable.resetBounds(jframe, 2, 0, 50); });
		 * 
		 * }
		 * 
		 * }); DataTables<String, String> surveyTable = new DataTables<>("Survey", new
		 * Rectangle((GUIUtilities.getScreenWidth() / 2) - 8, 50,
		 * (jframe.getBounds().width / 2) - 8, jframe.getBounds().height - 135));
		 * surveyTable.addHierarchyBoundsListener(new HierarchyBoundsListener() {
		 * 
		 * @Override public void ancestorMoved(HierarchyEvent e) { // TODO
		 * Auto-generated method stub
		 * 
		 * }
		 * 
		 * @Override public void ancestorResized(HierarchyEvent e) {
		 * Executors.newSingleThreadExecutor().execute(()->{
		 * surveyTable.resetBounds(jframe, 2, 1, 50); });
		 * 
		 * }
		 * 
		 * });
		 */

		jframe.getContentPane().setBackground(Color.LIGHT_GRAY);
		// jframe.add(dataTable);
		// jframe.add(surveyTable);
		jframe.add(getParamPanel(1, "Hydr. FDI Pressure", "VFR Mult.", "Min Down", "Min Height"));

		jframe.add(getOptionPanel(dataTablePanel, "FDI Table", "Survey Table"));
		jframe.setVisible(true);
		jframe.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	public static Rectangle getMaxWindowBounds() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
	}

	private static ParamPanel getParamPanel(int rows, String... params) {
		ParamPanel panel = new ParamPanel(
				new ParamPanel.PanelPadding(0, GUIUtilities.getScreenHeight() - 100,
						((GUIUtilities.getScreenWidth() * 2) / 3), 50),
				GUIUtilities.getCenterRectangle(0f), rows, params);
		return panel;
	}

	@SuppressWarnings("unchecked")
	private static OptionPanel getOptionPanel(DataTablePanel dataTablePanel, String table1Name, String table2Name) {
		OptionPanel optionPanel = new OptionPanel(GUIUtilities.getScreenWidth() * 3 / 4, 50);

		DataTables<String, String> dataTable1 = (DataTables<String, String>) GUIUtilities
				.getComponentByName(dataTablePanel, table1Name);
		DataTables<String, String> dataTable2 = (DataTables<String, String>) GUIUtilities
				.getComponentByName(dataTablePanel, table2Name);
		optionPanel.addOption("Import FDI Data", new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				String filePath = ImportData.selectFile();
				try {
					Map<String, Map<String, List<String>>> fdiMap = ImportData.readFracBrainFDIs(filePath,
							"pressureUnits", "proppantUnits", "rateUnits");
					dataTable1.addTabs(fdiMap);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		optionPanel.addOption("Import Surveys", new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				Map<String, Map<String, List<String>>> map = new HashMap<>();
				String filePath = ImportData.selectFile();
				try {
					Map<String, List<String>> survey = ImportData.readFileData(filePath, String.class);
					map.put("SURVEY", survey);
					dataTable2.addTabs(map);
				} catch (Exception e1) {
					e1.printStackTrace();
					return;
				}

			}
		});
		optionPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		return optionPanel;
	}

	private static OptionPanel getOptionPanel(DataTables<String, String> dataTable,
			DataTables<String, String> surveyTable) {
		OptionPanel optionPanel = new OptionPanel(GUIUtilities.getScreenWidth() * 3 / 4, 50);
		optionPanel.addOption("Import FDI Data", new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				String filePath = ImportData.selectFile();
				try {
					Map<String, Map<String, List<String>>> fdiMap = ImportData.readFracBrainFDIs(filePath,
							"pressureUnits", "proppantUnits", "rateUnits");
					dataTable.addTabs(fdiMap);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		optionPanel.addOption("Import Surveys", new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				Map<String, Map<String, List<String>>> map = new HashMap<>();
				String filePath = ImportData.selectFile();
				try {
					Map<String, List<String>> survey = ImportData.readFileData(filePath, String.class);
					map.put("SURVEY", survey);
					dataTable.addTabs(map);
				} catch (Exception e1) {
					e1.printStackTrace();
					return;
				}

			}
		});
		optionPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		return optionPanel;
	}

	private static String getFileExt(String filePath) {
		Matcher matcher = Pattern.compile("\\.[A-Za-z0-9]+$").matcher(filePath);
		if (matcher.find()) {
			return matcher.group();
		}
		return "null_ext";
	}
}
