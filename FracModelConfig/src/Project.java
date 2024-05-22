import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import frame.GUIUtilities;
import mrl.DataHandling;

public class Project implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 111111111;
	private MapChannels.DataChannels dataChannels;
	private String name;
	private List<String> jobList;
	private List<String> wellNames;
	private List<String> offsetWells;
	private Map<String, List<LocalDateTime>> wellStageMap;

	private Project(String name, List<String> jobList, List<String> wellNames, List<String> offsetWells,
			MapChannels.DataChannels dataChannels) throws IOException {
		this.name = name;
		this.jobList = jobList;
		this.wellNames = wellNames;
		this.offsetWells = offsetWells;
		this.dataChannels = dataChannels;
		createWellStageMap(wellNames);
		saveProject();
	}

	private void createWellStageMap(List<String> wellNames) {
		wellStageMap = new HashMap<>();
		for (String s : wellNames) {
			wellStageMap.put(s, new ArrayList<>());
		}
	}

	public String getStageWell(Map<String, List<String>> dataMap) {
		Map<String, String> fracChannels = dataChannels.getChannelMap(MapChannels.DataChannels.FRAC_CHANNELS);
		Map<String, String> wellPressChannels = dataChannels
				.getChannelMap(MapChannels.DataChannels.WELL_PRESSURE_CHANNELS);
		Map<Double, String> avgDiffMap = new HashMap<>();
		Double min = 0.0;
		for (String s : wellPressChannels.keySet()) {
			Double avgDiff = getAvgPressDiff(dataMap.get(fracChannels.get(MapChannels.TREATING_PRESSURE)),
					dataMap.get(wellPressChannels.get(s)), dataMap.get(fracChannels.get(MapChannels.SLURRY_RATE)));
			avgDiffMap.put(avgDiff,s);
			min=avgDiff<min?avgDiff:min;
		}
		return avgDiffMap.get(min);
	}

	private final static int PRESSURE_SAMPLE_INT = 20;

	private Double getAvgPressDiff(List<String> treatingPressure, List<String> sfgPressure, List<String> slurryRate) {
		int startIndex = getIndexValueGreater(slurryRate, 30d);
		Double treatingAvg = FracCalculations.avgWithinBounds(treatingPressure, startIndex, PRESSURE_SAMPLE_INT);
		Double sfgAvg = FracCalculations.avgWithinBounds(sfgPressure, startIndex, PRESSURE_SAMPLE_INT);
		return Math.abs(treatingAvg - sfgAvg);
	}

	private int getIndexValueGreater(List<String> list, Double value) {
		int i = 0;
		for (String s : list) {
			if (Double.valueOf(s) > value) {
				return i;
			}
			i++;
		}
		return -1;
	}

	public synchronized void addToWellStageMap(String wellName, LocalDateTime openTime) {
		wellStageMap.get(wellName).add(openTime);
	}

	public final static String PROJECT_STAGE_PLOTS_DIR = "project_stage_plots\\";
	public final static String PLOT_EXT = ".png";
	public final static String PROJECT_STAGE_DIR = "project_stages\\";
	public final static String PROJECT_STAGE_FILE_PREFIX = "Stage_";

	public final static String PROJECT_FILENAME = "project.pro";
	public final static String PROJECT_DATA_DIR = "project_data\\";
	public final static String TRIMMED_PROJECT_DATA_DIR = "trimmed_project_data\\";

	public static String getFilePathForStage(String projectName, int stageNum) {
		return Project.getProjectFolderPath(projectName) + PROJECT_STAGE_DIR + PROJECT_STAGE_FILE_PREFIX + stageNum
				+ DATA_EXT;
	}

	public static String getFilePathForStage(String projectName, String fileName) {
		return Project.getProjectFolderPath(projectName) + PROJECT_STAGE_DIR + fileName + DATA_EXT;
	}

	public static String getFileDirForStages(String projectName) {
		return Project.getProjectFolderPath(projectName) + PROJECT_STAGE_DIR;
	}

	public static String getFilePathForStagePlots(String projectName, int stageNum) {
		return Project.getProjectFolderPath(projectName) + PROJECT_STAGE_PLOTS_DIR + PROJECT_STAGE_FILE_PREFIX
				+ stageNum + PLOT_EXT;
	}

	public static String getFilePathForStagePlots(String projectName, String fileName) {
		return Project.getProjectFolderPath(projectName) + PROJECT_STAGE_PLOTS_DIR + fileName + PLOT_EXT;
	}

	public static void saveStageMap(String projectName, int stageNum, Map<String, List<String>> map)
			throws IOException {
		String filePath = getFilePathForStage(projectName, stageNum);
		DataHandling.writeObjToFile(map, filePath);
	}

	public static void saveStageMap(String projectName, String openTime, Map<String, List<String>> map)
			throws IOException {
		String filePath = getFilePathForStage(projectName, openTime);
		DataHandling.writeObjToFile(map, filePath);
	}

	public static String formatDateTimeForFile(String dateTime) {
		Matcher matcher = Pattern.compile("(\\d+).(\\d+).(\\d+)(.+)").matcher(dateTime);
		if (matcher.find()) {
			String day = matcher.group(2);
			String month = matcher.group(1);
			String year = matcher.group(3);
			return year + "-" + month + "-" + day + matcher.group(4);
		}
		return "1970-01-01 00:00:00";
	}

	private void saveProject() throws IOException {
		String filePath = getProjectFolderPath(name) + PROJECT_FILENAME;
		DataHandling.writeObjToFile(this, filePath);
		System.gc();
	}

	public static Project getSavedProject(String projectName) throws IOException, ClassNotFoundException {
		String filePath = Project.getProjectFolderPath(projectName) + PROJECT_FILENAME;
		File file = new File(filePath);
		Project project = (Project) DataHandling.readObjFromFile(Project.class, file);
		return project;
	}

	private void setWellNames(List<String> wellNames) {
		this.wellNames = wellNames;
	}

	private void setOffsetWellNames(List<String> offsetWells) {
		this.offsetWells = offsetWells;
	}

	public void updateWellNames() throws InterruptedException, ExecutionException, IOException {
		List<String> wellNames = setFracWellNames();
		setWellNames(wellNames);
		saveProject();

	}

	public void updateOffsetWellNames() throws InterruptedException, ExecutionException, IOException {
		List<String> offsetWells = setOffsetWellNames();
		setOffsetWellNames(offsetWells);
		saveProject();
	}

	public void updateChannels() throws ClassNotFoundException, IOException, InterruptedException {
		Map<String, String> cachedJobs = StartUp.getCachedJobsFromDir();
		List<String> headers = getDataHeadersFromJobData(cachedJobs, jobList);
		MapChannels mapChannels = setChannels(name, wellNames, offsetWells, headers);
		FracData.trimProjectData(name, mapChannels.dataChannels);
		saveProject();
	}

	public List<String> getJobList() {
		return this.jobList;
	}

	public MapChannels.DataChannels getDataChannels() {
		return dataChannels;
	}

	public static Project loadProject(String projectName) throws ClassNotFoundException, IOException {
		String filePath = getProjectFolderPath(projectName) + PROJECT_FILENAME;
		Project project = DataHandling.readObjFromFile(Project.class, new File(filePath));
		return project;
	}

	public static List<String> getSavedProjects() {
		File file = new File(PROJECTS_DIR);
		List<String> list = new ArrayList<>();
		if (!file.exists() || file.list().length == 0) {
			file.mkdirs();
			list.add(CREATE_NEW_PROJECT);
			return list;
		}
		for (String s : file.list()) {
			list.add(s);
		}
		list.add(CREATE_NEW_PROJECT);
		return list;
	}

	public final static String DATA_EXT = ".map";

	public static String getTrimmedProjectDataDirForDate(LocalDate localDate, String projectName) {
		return getTrimmedProjectDataDir(projectName) + "\\" + localDate.toString() + DATA_EXT;
	}

	public static String getProjectDataDirForDate(LocalDate localDate, String projectName) {
		return getProjectDataDir(projectName) + "\\" + localDate.toString() + DATA_EXT;
	}

	public static void deleteProject(String projectName) {
		String filePath = getProjectFolderPath(projectName);
		File file = new File(filePath);
		deleteDirContents(file);
		file.delete();
	}

	private static void deleteDirContents(File file) {
		if (file.isDirectory() && file.list().length == 0) {
			return;
		}
		for (File f : file.listFiles()) {
			if (f.isDirectory()) {
				deleteDirContents(f);
			}
			f.delete();
		}

	}

	public final static String CREATE_NEW_PROJECT = "Create New Project";

	public static String getProjectFolderPath(String projectName) {
		return PROJECTS_DIR + projectName + "/";
	}

	public String getName() {
		return this.name;
	}
	/// In the future, handle issue
	// where user may have not downloaded a job from the MainFrame before trying to
	/// create a new Project

	/// Also, check for existing projects with same name that is input and prompt
	/// user to overwrite or change name
	public static Project createNewProject()
			throws ClassNotFoundException, InterruptedException, ExecutionException, IOException {
		String name = JOptionPane.showInputDialog("Input the name of this project");
		createDataDir(name);
		if (name == null || name.isEmpty()) {
			return null;
		}
		List<String> wellNames = setFracWellNames();
		List<String> offsetWells = setOffsetWellNames();
		Map<String, String> cachedJobs = StartUp.getCachedJobsFromDir();
		List<String> jobList = getJobIDs(cachedJobs,
				GUIUtilities.getListSelectionDialog("Select jobs whose data you want to include in the new project",
						cachedJobs.keySet()).get());
		List<String> headers = getDataHeadersFromJobData(cachedJobs, jobList);
		Semaphore semaphore = new Semaphore(0);
		Executors.newSingleThreadExecutor().execute(() -> {
			try {
				FracData.structProjectData(name, jobList);
				semaphore.release();
			} catch (ClassNotFoundException | IOException | InterruptedException e) {
				semaphore.release();
				e.printStackTrace();
			}
		});
		MapChannels mapChannels = setChannels(name, wellNames, offsetWells, headers);
		semaphore.acquire();
		FracData.trimProjectData(name, mapChannels.dataChannels);
		return new Project(name, jobList, wellNames, offsetWells, mapChannels.dataChannels);
	}

	private static List<String> getJobIDs(Map<String, String> cachedJobs, List<String> jobList) {
		List<String> ids = new ArrayList<>();
		for (String s : jobList) {
			ids.add(cachedJobs.get(s));
		}
		return ids;
	}

	public static String getTrimmedProjectDataDir(String projectName) {
		String projPath = getProjectFolderPath(projectName) + TRIMMED_PROJECT_DATA_DIR;
		return projPath;
	}

	public static String getProjectDataDir(String projectName) {
		String projPath = getProjectFolderPath(projectName) + PROJECT_DATA_DIR;
		return projPath;
	}

	private static void createDataDir(String projectName) {
		File file = new File(getProjectDataDir(projectName));
		file.mkdirs();
	}

	private static List<String> setFracWellNames() throws InterruptedException, ExecutionException {
		return GUIUtilities.getMultiInputDialog("Set Frac Well Names", "Well Name").get();
	}

	private static List<String> setOffsetWellNames() throws InterruptedException, ExecutionException {
		return GUIUtilities.getMultiInputDialog("Set Offset Well Names", "Offset Well").get();
	}

	private static MapChannels setChannels(String name, List<String> wellNames, List<String> offsetWells,
			List<String> headers) throws ClassNotFoundException, IOException, InterruptedException {

		headers.sort(getCompForHeaders());
		MapChannels mapChannels = new MapChannels(headers, wellNames, offsetWells, name);
		return mapChannels;
	}

	private static Comparator<String> getCompForHeaders() {
		Comparator<String> comp = new Comparator<>() {

			@Override
			public int compare(String o1, String o2) {
				int c1 = o1.toLowerCase().charAt(0);
				int c2 = o2.toLowerCase().charAt(0);
				return c1 - c2;
			}

		};
		return comp;
	}

	private static List<String> getDataHeadersFromJobData(Map<String, String> cachedJobs, List<String> jobIDs)
			throws ClassNotFoundException, IOException {
		Set<String> headerSet = new HashSet<>();

		for (String s : jobIDs) {
			String filePath = DataHandling.JOB_DATA_DIR + s;
			headerSet.addAll(getHeadersFromFile(filePath));
		}
		List<String> headers = new ArrayList<>();
		headers.addAll(headerSet);
		return headers;
	}

	private static List<String> getHeadersFromFile(String fileDir) throws ClassNotFoundException, IOException {
		List<String> array = new ArrayList<>();

		File file = new File(fileDir);
		File[] files = file.listFiles();

		if (files.length == 0) {
			return array;
		}

		Map<String, List<String>> dataMap = (Map<String, List<String>>) DataHandling.readObjFromFile(HashMap.class,
				files[0]);
		array.addAll(dataMap.keySet());
		return array;
	}

	public static List<File> selectFromSavedJobs()
			throws ClassNotFoundException, IOException, InterruptedException, ExecutionException {
		Map<String, String> cachedJobs = StartUp.getCachedJobsFromDir();
		CompletableFuture<List<String>> listToPopulate = new CompletableFuture<>();
		System.out.println(listToPopulate.get());

		return null;
	}

	public static JFrame getFrame() {
		JFrame frame = new JFrame();
		frame.setBounds(GUIUtilities.getCenterRectangle(0f));
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
		return frame;
	}

	public final static String PROJECTS_DIR = "Projects/";

}
