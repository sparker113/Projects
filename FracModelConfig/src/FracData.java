
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mrl.DataHandling;
import mrl.DataRequest;

public class FracData {
	Project project;

	public FracData(Project project) throws ClassNotFoundException, IOException {
		this.project = project;
		// structProjectData(project.getName(),project.getJobList());
	}

	public void structByWellAndStage() throws ClassNotFoundException, IOException, InterruptedException, ExecutionException {
		String projectName = project.getName();
		File dataDir = new File(Project.getTrimmedProjectDataDir(projectName));
		long t1 = System.currentTimeMillis();
		Map<LocalDate, Map<String, List<Integer>>> datedUpDownMap = getRateUpDownIndeces(dataDir,
				project.getDataChannels().getChannelMap(MapChannels.DataChannels.FRAC_CHANNELS),true);
		//saveStages(datedUpDownMap,dataDir,projectName);
		long t2 = System.currentTimeMillis();
		System.out.println("Computing Time: "+(t2-t1));
	}

	@SuppressWarnings("unchecked")
	private void saveStages(Map<LocalDate, Map<String, List<Integer>>> datedUpDownMap, File dataDir, String projectName)
			throws ClassNotFoundException, IOException {
		List<LocalDate> sortedDates = sortSetChrono(datedUpDownMap.keySet());
		Map<String,List<String>> excessData = null;
		int i = 0;

		for (LocalDate date : sortedDates) {
			Map<String, List<Integer>> upDownMap = datedUpDownMap.get(date);
			if (i == 0) {
				upDownMap = checkFirstDown(upDownMap);
			}
			System.out.println(upDownMap);
			File dataFile = new File(Project.getTrimmedProjectDataDirForDate(date,projectName));
			Map<String, List<String>> dataMap = (Map<String, List<String>>) DataHandling.readObjFromFile(HashMap.class,
					dataFile);
			if(excessData!=null) {
				int addSize = getSizeOfLists(excessData);
				dataMap = combineMaps(excessData,dataMap);
				upDownMap = addToUpDownMap(upDownMap,addSize);
				System.out.println("After Adding To UpDownMap: "+upDownMap);
				upDownMap.get(RATE_UP_KEY).add(0,0);
			}
			excessData = constrStages(dataMap,upDownMap,projectName,i);//,lastUp,lastDown);
			i+=upDownMap.get(RATE_UP_KEY).size();
		}
	}
	private int getSizeOfLists(Map<String,List<String>> map) {
		for(Map.Entry<String,List<String>> entry:map.entrySet()) {
			return entry.getValue().size()-1;
		}
		return 0;
	}
	private Map<String,List<Integer>> addToUpDownMap(Map<String,List<Integer>> upDownMap,int addToAll) {
		System.out.println(upDownMap);
		Map<String,List<Integer>> newMap = new HashMap<>();
		for(Map.Entry<String, List<Integer>> entry:upDownMap.entrySet()) {
			newMap.put(entry.getKey(), addToAllInList(entry.getValue(),addToAll));
		}
		return newMap;
	}
	private List<Integer> addToAllInList(List<Integer> list,int add){
		List<Integer> newList = new ArrayList<>();
		for(Integer i:list) {
			newList.add((i+add));
		}
		return newList;
	}
	private final static Double MAX_SLOPE_NO_RATE = 10d;
	private final static int DERIV_AVG_INTERVAL = 5;
	private Map<String,List<String>> combineMaps(Map<String,List<String>> addMap,Map<String,List<String>> map){
		for(String s:addMap.keySet()) {
			addMap.get(s).addAll(0,map.get(s));
		}
		return addMap;
	}
	private Map<String, List<String>> constrStages(Map<String, List<String>> dataMap,
			Map<String, List<Integer>> upDownMap, Map<LocalDate,CompletableFuture<Map<String,List<String>>>> excessMap,
			String projectName,int totalStage,LocalDate date) throws IOException, InterruptedException, ExecutionException {
		List<Integer> rateUp = upDownMap.get(RATE_UP_KEY);
		List<Integer> rateDown = upDownMap.get(RATE_DOWN_KEY);
		List<Map<String,List<String>>> stageList = new ArrayList<>();
		int downOffset = rateUp.get(0)>rateDown.get(0)?1:0;
		for (int i = 0; i < rateDown.size()-downOffset; i++) {
			Map<String, List<String>> stage = getStage(dataMap, rateUp.get(i), rateDown.get(i+downOffset));
			stageList.add(stage);
		}
		if(rateUp.get(rateUp.size()-1)>rateDown.get(rateDown.size()-1)) {
			excessMap.get(date).complete(getSubsetMap(dataMap,rateUp.get(rateUp.size()-1)));
			System.out.println("CompletableFuture completed");
		}else {
			excessMap.get(date).complete(new HashMap<>());
			System.out.println("CompletableFuture completed");
		}
		if(downOffset==1&excessMap.containsKey(date.minusDays(1))) {
			System.out.println("Waiting on: "+date.minusDays(1));
			Map<String,List<String>> excess = excessMap.get(date.minusDays(1)).get();
			System.out.println("Excess Map - "+date.minusDays(1)+": "+excess);
			Map<String,List<String>> stage = getStage(dataMap,excess,rateDown.get(0));
			stageList.add(0,stage);
		}
		saveStagesFromList(stageList,projectName,totalStage);
		return null;
	}
	private void saveStagesFromList(List<Map<String,List<String>>> stageList,String projectName,int totalStage) throws IOException {
		int i = 0;
		for(Map<String,List<String>> stage:stageList) {
			Project.saveStageMap(projectName, stage.get(DataRequest.DataChannels.TIME.getValue()).get(0).replaceAll("/","_").replaceAll(":", "_"), stage);
			i++;
		}
	}
	//// Return data for stages that carry over past midnight
	private Map<String, List<String>> constrStages(Map<String, List<String>> dataMap,
			Map<String, List<Integer>> upDownMap,String projectName, int totalStage) throws IOException {
		List<Integer> rateUp = upDownMap.get(RATE_UP_KEY);
		List<Integer> rateDown = upDownMap.get(RATE_DOWN_KEY);
		Map<String,String> channelMap = project.getDataChannels().getChannelMap(MapChannels.DataChannels.FRAC_CHANNELS);

		
		for (int i = 0; i < rateDown.size(); i++) {
			if(!dataMap.containsKey(channelMap.get(MapChannels.TREATING_PRESSURE))) {
				return null;
			}
			int open = findWellOpen(dataMap.get(channelMap.get(MapChannels.TREATING_PRESSURE)),rateUp.get(i),DERIV_AVG_INTERVAL,MAX_SLOPE_NO_RATE);
			int close = findWellClose(dataMap.get(channelMap.get(MapChannels.TREATING_PRESSURE)),rateDown.get(i),DERIV_AVG_INTERVAL,MAX_SLOPE_NO_RATE);
			
			System.out.println("Open Index: "+open+"\tClose Index: "+close);
			Map<String, List<String>> stage = getStage(dataMap, open, close);
			Project.saveStageMap(projectName,totalStage+i,stage);
		}
		if(rateUp.size()>rateDown.size()) {
			return getSubsetMap(dataMap,findWellOpen(dataMap.get(channelMap.get(MapChannels.TREATING_PRESSURE)),rateUp.get(rateUp.size()-1),DERIV_AVG_INTERVAL,MAX_SLOPE_NO_RATE));
		}
		return null;
	}
	private static int findWellClose(List<String> treatingPressure, int startIndex, int interval, Double maxSlopeAtClose) {
		for (int i = startIndex; i < treatingPressure.size(); i++) {
			interval = (i + interval >= treatingPressure.size()?treatingPressure.size()-i-1: interval);
			if (isAverageSlopeWithin(treatingPressure, i, interval, maxSlopeAtClose)) {
				return i;
			}
		}
		return treatingPressure.size()-1;
	}
	public final static Double PRESSURE_LIMIT = 250d;
	private static int findWellClose(List<String> treatingPressure, List<String> slurryRate,int startIndex, int interval, Double averageSlopeMag) {
		if(startIndex==END_OF_LIST) {
			return END_OF_LIST;
		}
		for (int i = startIndex; i < treatingPressure.size(); i++) {
			interval = (i + interval >= treatingPressure.size()?treatingPressure.size()-i-1: interval);
			if (Double.valueOf(treatingPressure.get(i))<PRESSURE_LIMIT|(Double.valueOf(slurryRate.get(i))<FracData.RATE_LOWER_LIMIT)) {
				return i;
			}
		}
		return treatingPressure.size()-1;
	}
	private static int findWellOpen(List<String> treatingPressure, int startIndex, int interval, Double maxSlopeAtOpen) {
		for (int i = startIndex; i >= 0; i--) {
			int p0 = (i - interval < 0 ? 0 : i - interval);
			if (isAverageSlopeWithin(treatingPressure, p0, interval, maxSlopeAtOpen)) {
				return p0;
			}
		}
		return 0;
	}
	private static int findWellOpen(List<String> treatingPressure, List<String> slurryRate,int startIndex, int interval, Double averageSlopeMag) {
		if(startIndex==END_OF_LIST) {
			return END_OF_LIST;
		}
		for (int i = startIndex; i >= 0; i--) {
			int p0 = (i - interval < 0 ? 0 : i - interval);
			if (Double.valueOf(slurryRate.get(p0))<FracData.RATE_LOWER_LIMIT&&isAverageSlopeWithin(treatingPressure, p0, interval, averageSlopeMag)) {
				return p0;
			}
		}
		return 0;
	}
	
	private static boolean isAverageSlopeWithin(List<String> data, int startIndex, int interval, Double slope) {
		Double addedDeriv = 0d;
		for (int i = startIndex; i < startIndex + interval; i++) {
			addedDeriv += (Double.valueOf(data.get(i + 1)) - Double.valueOf(data.get(i)));
		}
		Double avgDeriv = addedDeriv / interval;
		return (slope > Math.abs(avgDeriv));
	}

	private Map<String, List<String>> getStage(Map<String, List<String>> dataMap, int start, int end) {
		Map<String, List<String>> stageData = new HashMap<>();
		for (Map.Entry<String, List<String>> entry : dataMap.entrySet()) {
			stageData.put(entry.getKey(), getSubsetList(entry.getValue(), start, end));
		}
		return stageData;
	}
	private Map<String, List<String>> getStage(Map<String, List<String>> dataMap,Map<String,List<String>> excessMap,int end) {
		Map<String, List<String>> stageData = new HashMap<>();
		stageData.putAll(excessMap);
		for (Map.Entry<String, List<String>> entry : dataMap.entrySet()) {
			stageData.put(entry.getKey(), getSubsetList(entry.getValue(), 0, end));
		}
		return stageData;
	}
	private Map<String,List<String>> getSubsetMap(Map<String,List<String>> map,int start){
		Map<String,List<String>> subMap = new HashMap<>();
		for(Map.Entry<String, List<String>> entry:map.entrySet()) {
			subMap.put(entry.getKey(), getSubsetList(entry.getValue(),start));
		}
		return subMap;
	}
	private List<String> getSubsetList(List<String> list, int start, int end) {
		List<String> truncList = new ArrayList<>();
		for (int i = start; i <= end; i++) {
			truncList.add(i>=list.size()?"0":list.get(i));
		}
		return truncList;
	}
	private List<String> getSubsetList(List<String> list, int start) {
		List<String> truncList = new ArrayList<>();
		for (int i = start; i < list.size(); i++) {
			truncList.add(list.get(i));
		}
		return truncList;
	}

	private Map<String, List<Integer>> checkFirstDown(Map<String, List<Integer>> upDownMap) {
		if (upDownMap.get(RATE_UP_KEY).get(0) > upDownMap.get(RATE_DOWN_KEY).get(0)) {
			upDownMap.get(RATE_DOWN_KEY).remove(0);
		}
		return upDownMap;
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

	public final static String RATE_UP_KEY = "rate_up";
	public final static String RATE_DOWN_KEY = "rate_down";

	@SuppressWarnings("unchecked")
	public Map<LocalDate, Map<String, List<Integer>>> getRateUpDownIndeces(File dataDir,
			Map<String, String> fracChannels) throws ClassNotFoundException, IOException, InterruptedException {
		Map<LocalDate, Map<String, List<Integer>>> datedUpDownMap = new HashMap<>();
		if (!dataDir.exists() || dataDir.list().length == 0) {
			return datedUpDownMap;
		}
		ExecutorService executor = Executors.newFixedThreadPool(5);
		Semaphore semaphore = new Semaphore(0);
		int numFiles = dataDir.list().length;
		for (File f : dataDir.listFiles()) {
			executor.execute(()->{
				long t1 = System.currentTimeMillis();
				
				Map<String, List<String>> dataMap = null;
				try {
					dataMap = (Map<String, List<String>>) DataHandling.readObjFromFile(HashMap.class,
							f);
				} catch (ClassNotFoundException | IOException e) {
				semaphore.release();
					e.printStackTrace();
					return;
				}
				if (!dataMap.containsKey(fracChannels.get(MapChannels.SLURRY_RATE))|!dataMap.containsKey(fracChannels.get(MapChannels.TREATING_PRESSURE))) {
					semaphore.release();
					return;
				}
				Map<String, List<Integer>> upDownMap = getRateUpDownIndeces(
						dataMap.get(fracChannels.get(MapChannels.SLURRY_RATE)),dataMap.get(fracChannels.get(MapChannels.TREATING_PRESSURE)));
				addToDatedUpDownMap(LocalDate.parse(f.getName().replace(DataHandling.DATA_EXT, "")), upDownMap,datedUpDownMap);
				
				long t2 = System.currentTimeMillis();
				
				System.out.println("Computing Time (getRateUpDownIndeces) - 1 File: "+(t2-t1));
				semaphore.release();
			});
		}
		semaphore.acquire(numFiles);
		executor.shutdownNow();
		return datedUpDownMap;
	}
	
	/////Need to synchronize stage numbering; currently using count
	@SuppressWarnings("unchecked")
	public Map<LocalDate, Map<String, List<Integer>>> getRateUpDownIndeces(File dataDir,
			Map<String, String> fracChannels,boolean change) throws ClassNotFoundException, IOException, InterruptedException, ExecutionException {
		Map<LocalDate, Map<String, List<Integer>>> datedUpDownMap = new HashMap<>();
		if (!dataDir.exists() || dataDir.list().length == 0) {
			return datedUpDownMap;
		}
		Map<LocalDate,CompletableFuture<Map<String,List<String>>>> excessData = getExcessFutureMap(dataDir.list());
		Semaphore semaphore = new Semaphore(0);
		int count[] = {0};
		int numFiles = dataDir.list().length;
		ExecutorService executor = Executors.newFixedThreadPool(10);
		for (File f : getChronoSortedList(dataDir)) {
			executor.execute(()->{
					long t1 = System.currentTimeMillis();
				
					Map<String, List<String>> dataMap = null;
					try {
						dataMap = (Map<String, List<String>>) DataHandling.readObjFromFile(HashMap.class,
								f);
					} catch (ClassNotFoundException | IOException e) {
						e.printStackTrace();
						semaphore.release();
						return;
					}
					LocalDate date = LocalDate.parse(f.getName().replace(DataHandling.DATA_EXT, ""));
					if (!dataMap.containsKey(fracChannels.get(MapChannels.SLURRY_RATE))|!dataMap.containsKey(fracChannels.get(MapChannels.TREATING_PRESSURE))) {
						System.out.println("Map doesn't contain pressure or slurry rate data channels");
						excessData.get(date).complete(new HashMap<>());
						System.out.println("CompletableFuture Completed "+date);
						semaphore.release();
						return;
					}
					Map<String, List<Integer>> upDownMap = getRateUpDownIndeces(
							dataMap.get(fracChannels.get(MapChannels.SLURRY_RATE)),dataMap.get(fracChannels.get(MapChannels.TREATING_PRESSURE)));
					
		
					//addToDatedUpDownMap(date, upDownMap,datedUpDownMap);
					try {
						constrStages(dataMap,upDownMap,excessData,project.getName(),count[0],date);
						//saveStages(date,upDownMap,dataMap,excessData,getProject().getName(),count[0]);
					} catch (IOException | InterruptedException | ExecutionException e) {
						semaphore.release();
						e.printStackTrace();
						return;
					}
					count[0]+=upDownMap.get(RATE_UP_KEY).size();
					long t2 = System.currentTimeMillis();
					
					System.out.println("Computing Time (getRateUpDownIndeces) - "+f.getName()+": "+(t2-t1));
					
					semaphore.release();
			});
		}
		semaphore.acquire(numFiles);
		executor.shutdownNow();
		return datedUpDownMap;
	}
	private List<File> getChronoSortedList(File dataDir){
		List<File> list = new ArrayList<>();
		for(File f:dataDir.listFiles()) {
			list.add(f);
		}
		list.sort(new Comparator<File>() {

			@Override
			public int compare(File o1, File o2) {
				LocalDate d1 = LocalDate.parse(o1.getName().replace(Project.DATA_EXT, ""));
				LocalDate d2 = LocalDate.parse(o2.getName().replace(Project.DATA_EXT, ""));
				return d1.compareTo(d2);
			}
		
		});
		System.out.println(list);
		return list;
	}
	private synchronized CompletableFuture<Map<String,List<String>>> getFutureForDate(Map<LocalDate,CompletableFuture<Map<String,List<String>>>> excessMap,LocalDate date){
		return excessMap.get(date);
	}
	private synchronized void completeExcessFuture(Map<LocalDate,CompletableFuture<Map<String,List<String>>>> excessMap,LocalDate date,Map<String,List<String>> map) {
		excessMap.get(date).complete(map);
	}
	private Map<LocalDate,CompletableFuture<Map<String,List<String>>>> getExcessFutureMap(String[] fileNames){
		Map<LocalDate,CompletableFuture<Map<String,List<String>>>> futureMap = new HashMap<>();
		for(String s:fileNames) {
			futureMap.put(LocalDate.parse(s.replace(Project.DATA_EXT, "")), new CompletableFuture<>());
		}
		return futureMap;
	}
	public Project getProject() {
		return this.project;
	}
	private synchronized void addToDatedUpDownMap(LocalDate date,Map<String,List<Integer>> upDownMap,Map<LocalDate,Map<String,List<Integer>>> datedUpDownMap) {
		datedUpDownMap.put(date, upDownMap);
	}
	private Map<String, List<Integer>> getEmptyMapWithKeys(String... keys) {
		Map<String, List<Integer>> map = new HashMap<>();
		for (String s : keys) {
			map.put(s, new ArrayList<>());
		}
		return map;
	}

	private Map<String, List<Integer>> getRateUpDownIndeces(List<String> slurryRate,List<String> treatingPressure) {
		Map<String, List<Integer>> upDownMap = getEmptyMapWithKeys(RATE_UP_KEY, RATE_DOWN_KEY);
		int firstZeros = findNextZeroRate(slurryRate, 0);
		int firstRate = findNextRateUp(slurryRate,0);
		if (firstZeros == END_OF_LIST|firstRate==END_OF_LIST) {
			return upDownMap;
		}
		String firstKey = firstRate>firstZeros?RATE_UP_KEY:RATE_DOWN_KEY;
		//upDownMap.get(firstKey).add(firstKey.equals(RATE_UP_KEY)?firstRate:firstZeros);
		int zeros = 0;
		int i = 0;
		int rateUp = 0;
		while (zeros != END_OF_LIST&rateUp!=END_OF_LIST) {
			//System.out.println("rateUp:\t"+rateUp);
			if(i==0&firstKey.equals(RATE_UP_KEY)){
				zeros = findWellClose(treatingPressure,slurryRate,findNextZeroRate(slurryRate, firstRate),DERIV_AVG_INTERVAL,MAX_SLOPE_NO_RATE);
				upDownMap.get(RATE_DOWN_KEY).add(zeros);
				i++;
				continue;
			}else{
				rateUp = findWellOpen(treatingPressure,slurryRate,findNextRateUp(slurryRate, zeros),DERIV_AVG_INTERVAL,MAX_SLOPE_NO_RATE);
				upDownMap.get(RATE_UP_KEY).add(rateUp);
				i++;
				
			}
			System.out.println("Rate Up: "+rateUp+"\tZeros: "+zeros);
			zeros = findWellClose(treatingPressure,slurryRate,findNextZeroRate(slurryRate, rateUp),DERIV_AVG_INTERVAL,MAX_SLOPE_NO_RATE);
			if(rateUp==zeros) {
				zeros = END_OF_LIST;
			}
			upDownMap.get(RATE_DOWN_KEY).add(zeros);
					
		}
		upDownMap.get(RATE_UP_KEY).remove(Integer.valueOf(END_OF_LIST));
		upDownMap.get(RATE_DOWN_KEY).remove(Integer.valueOf(END_OF_LIST));
		return upDownMap;
	}

	private final static double RATE_UPPER_LIMIT = 20d;
	private final static double RATE_LOWER_LIMIT = 1d;
	private final static int FUTURE_INDECES_TO_CHECK = 20;
	private Integer findNextRateUp(List<String> slurryRate, int startInd) {

		for (int i = startInd; i < slurryRate.size(); i++) {
			Double rate = Double.valueOf(slurryRate.get(i));
			if (rate > RATE_UPPER_LIMIT & checkNextValuesGreaterThan(slurryRate, RATE_UPPER_LIMIT, i, FUTURE_INDECES_TO_CHECK)) {
				return i;
			}
		}
		return END_OF_LIST;
	}

	private final static int END_OF_LIST = -1;


	private Integer findNextZeroRate(List<String> slurryRate, int startInd) {
		if (startInd == END_OF_LIST) {
			return END_OF_LIST;
		}
		for (int i = startInd; i < slurryRate.size(); i++) {
			Double rate = Double.valueOf(slurryRate.get(i));
			if (rate < RATE_LOWER_LIMIT & checkNextValuesLessThan(slurryRate, RATE_LOWER_LIMIT, i, FUTURE_INDECES_TO_CHECK)) {
				return i;
			}
		}
		return END_OF_LIST;
	}

	private boolean checkNextValuesGreaterThan(List<String> slurryRate, Double greaterThan, int startInd, int numVals) {
		for (int i = startInd; i < startInd + numVals; i++) {
			if (i >= slurryRate.size() & i > startInd) {
				return false;
			}
			Double rate = Double.valueOf(slurryRate.get(i));
			if (rate < greaterThan) {
				return false;
			}
		}
		return true;
	}

	private boolean checkNextValuesLessThan(List<String> slurryRate, Double lessThan, int startInd, int numVals) {
		for (int i = startInd; i < startInd + numVals; i++) {
			if (i >= slurryRate.size() & i > startInd) {
				return true;
			}
			Double rate = Double.valueOf(slurryRate.get(i));
			if (rate > lessThan) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public static void trimProjectData(String projectName, MapChannels.DataChannels dataChannels)
			throws InterruptedException {
		File fileDir = new File(Project.getProjectDataDir(projectName));
		if (!fileDir.exists() || fileDir.list().length == 0) {
			return;
		}
		List<String> channels = dataChannels.getListOfChannelsUsed();
		Semaphore semaphore = new Semaphore(0);
		ExecutorService executor = Executors.newFixedThreadPool(5);
		int i = fileDir.list().length;
		for (File f : fileDir.listFiles()) {
			executor.execute(() -> {
				Map<String, List<String>> trimmedMap;
				try {
					trimmedMap = trimDataMap(channels,
							(Map<String, List<String>>) DataHandling.readObjFromFile(HashMap.class, f));
					DataHandling.writeObjToFile(trimmedMap, Project.getTrimmedProjectDataDirForDate(
							LocalDate.parse(f.getName().replace(DataHandling.DATA_EXT, "")), projectName));
					semaphore.release();
				} catch (ClassNotFoundException | IOException e) {
					semaphore.release();
					e.printStackTrace();
				}
			});
		}
		semaphore.acquire(i);
		executor.shutdown();
	}

	private static Map<String, List<String>> trimDataMap(List<String> channels, Map<String, List<String>> dataMap) {
		Set<String> keys = new HashSet<>(dataMap.keySet());
		for (String s : keys) {
			if (!channels.contains(s)) {
				dataMap.remove(s);
			}
		}
		return dataMap;
	}

	public static void structProjectData(String projectName, List<String> jobList)
			throws ClassNotFoundException, IOException, InterruptedException {
		Map<LocalDate, List<String>> structMap = getProjectDataStructMap(jobList);
		ExecutorService executor = Executors.newFixedThreadPool(5);
		Semaphore semaphore = new Semaphore(0);
		for (Map.Entry<LocalDate, List<String>> entry : structMap.entrySet()) {
			executor.execute(() -> {
				try {
					aggJobDataForDate(projectName, entry.getKey(), entry.getValue());
					semaphore.release();
				} catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
					semaphore.release();
					return;
				}
			});
		}
		semaphore.acquire(structMap.size());
	}

	private static void aggJobDataForDate(String projectName, LocalDate date, List<String> jobListForDate)
			throws ClassNotFoundException, IOException {
		Map<String, List<String>> combinedData = new HashMap<>();
		for (String s : jobListForDate) {
			Map<String, List<String>> dataMap = DataHandling.getJobDataForDate(date, s);
			addNewDataToMap(combinedData, dataMap);
		}
		saveProjectDataByDate(combinedData, date, projectName);
		System.out.println("Saved Data for Date: " + date + "\tcombinedData size: " + combinedData.size());

	}

	private static void saveProjectDataByDate(Map<String, List<String>> dataMap, LocalDate date, String projectName)
			throws IOException {
		String filePath = Project.getProjectDataDirForDate(date, projectName);
		DataHandling.writeObjToFile(dataMap, filePath);
	}

	private static Map<LocalDate, List<String>> getProjectDataStructMap(List<String> jobList) {
		Map<LocalDate, List<String>> structMap = new TreeMap<>();
		for (String s : jobList) {
			addJobsToStructMap(structMap, s);
		}
		return structMap;
	}

	private static void addJobsToStructMap(Map<LocalDate, List<String>> structMap, String jobID) {
		List<LocalDate> list = getListOfJobDates(jobID);
		for (LocalDate d : list) {
			if (!structMap.containsKey(d)) {
				structMap.put(d, new ArrayList<>());
				structMap.get(d).add(jobID);
				continue;
			}
			structMap.get(d).add(jobID);
		}
	}

	public static List<LocalDate> getListOfJobDates(String jobID) {
		File file = new File(DataHandling.getJobDataDir(jobID));
		List<LocalDate> list = new ArrayList<>();
		if (file.list() == null || file.list().length == 0) {
			return list;
		}
		for (String s : file.list()) {
			if (!s.matches("\\d{4}-\\d{2}-\\d{2}.map")) {
				continue;
			}
			LocalDate date = LocalDate.parse(s.replace(DataHandling.DATA_EXT, ""));
			list.add(date);
		}
		return list;
	}

	public static LocalDate getFirstDateOfJobs(List<String> jobList) {
		LocalDate firstDate = LocalDate.now();
		for (String s : jobList) {
			LocalDate dataDate = getJobsFirstDate(s);
			firstDate = (firstDate.isBefore(dataDate) ? firstDate : dataDate);
		}
		return firstDate;
	}

	public static LocalDate getJobsFirstDate(String jobID) {
		File file = new File(DataHandling.getJobDataDir(jobID));
		LocalDate firstDate = LocalDate.now();
		for (String s : file.list()) {
			LocalDate dataDate = LocalDate.parse(s);
			firstDate = (firstDate.isBefore(dataDate) ? firstDate : dataDate);
		}
		return firstDate;
	}

	public static LocalDate getJobsLastDate(String jobID) {
		File file = new File(DataHandling.getJobDataDir(jobID));
		LocalDate lastDate = LocalDate.EPOCH;
		for (String s : file.list()) {
			LocalDate dataDate = LocalDate.parse(s);
			lastDate = (lastDate.isAfter(dataDate) ? lastDate : dataDate);
		}
		return lastDate;
	}

	private static Map<String, List<String>> addNewDataToMap(Map<String, List<String>> combinedMap,
			Map<String, List<String>> addMap) {

		if (combinedMap.isEmpty()) {
			addMap.put(DataRequest.DataChannels.TIME.getValue(), constrDateTimeArr(addMap, LocalDate.of(1970, 1, 1)));
			combinedMap.putAll(addMap);
			return combinedMap;
		}
		addMap.put(DataRequest.DataChannels.TIME.getValue(), constrDateTimeArr(addMap, getFirstDate(combinedMap)));
		return syncDataByTimeStamps(combinedMap, addMap);
	}

	private static List<String> constrDateTimeArr(Map<String, List<String>> dataMap, LocalDate startDate) {
		List<String> timeArr = dataMap.get(DataRequest.DataChannels.TIME.getValue());
		String timeValue = timeArr.get(0);
		if (timeValue.matches("\\d+:\\d+:\\d+") && dataMap.containsKey(DataRequest.DataChannels.DATE.getValue())) {
			return combineDateTime(dataMap.get(DataRequest.DataChannels.DATE.getValue()), timeArr);
		} else if (timeValue.matches("\\d+:\\d+:\\d")) {
			return combineDateTime(timeArr, startDate);
		}
		return timeArr;
	}

	private static LocalDate getFirstDate(Map<String, List<String>> dataMap) {
		String firstDateTime = dataMap.get(DataRequest.DataChannels.TIME.getValue()).get(0);
		LocalDateTime dateTime = formatDateTimeString(firstDateTime);
		return dateTime.toLocalDate();
	}

	private static List<String> combineDateTime(List<String> time, LocalDate startDate) {
		List<String> dateTime = new ArrayList<>();
		LocalDateTime startTime = LocalDateTime.of(startDate, LocalTime.parse(time.get(0)));
		for (int i = 0; i < time.size(); i++) {
			dateTime.add(startTime.plusSeconds((long) i).format(DateTimeFormatter.ofPattern("YYYY-MM-dd hh:mm:ss"))
					.toString());
		}
		return dateTime;
	}

	private static List<String> combineDateTime(List<String> date, List<String> time) {
		List<String> dateTime = new ArrayList<>();
		for (int i = 0; i < date.size(); i++) {
			String dateTimeString = formatDMYDate(date.get(i)) + "T" + time.get(i);
			dateTime.add(dateTimeString);
		}
		return dateTime;
	}

	private static String formatDMYDate(String date) {
		Matcher matcher = Pattern.compile("(\\d+)/(\\d+)/(\\d+)").matcher(date);
		String day = "01";
		String month = "01";
		String year = "1970";
		if (matcher.find()) {
			day = getLeadingZeros("0" + matcher.group(1));
			month = getLeadingZeros("0" + matcher.group(2));
			year = matcher.group(3);
		}
		return (year + "-" + month + "-" + day);
	}

	private static Map<String, List<String>> syncDataByTimeStamps(Map<String, List<String>> combinedMap,
			Map<String, List<String>> addMap) {
		int startInd = getEntryIndexForLists(combinedMap.get(DataRequest.DataChannels.TIME.getValue()),
				addMap.get(DataRequest.DataChannels.TIME.getValue()));
		appendToMapLists(combinedMap, addMap, startInd);
		/*
		 * 
		 * int size = getCombinedSize(startInd,
		 * combinedMap.get(DataRequest.DataChannels.TIME.getValue()).size(),
		 * addMap.get(DataRequest.DataChannels.TIME.getValue()).size()); if (startInd >
		 * 0) { return addListsToMap(combinedMap, addMap, startInd, size); }
		 * 
		 * return addListsToMap(addMap, combinedMap, Math.abs(startInd), size);
		 */
		return null;
	}

	// make private
	public static int getCombinedSize(int startInd, int size1, int size2) {
		return (size1 + size2) - startInd;
	}

	// make private
	public static Map<String, List<String>> addListsToMap(Map<String, List<String>> combinedMap,
			Map<String, List<String>> addMap, int startInd, int size) {
		for (Map.Entry<String, List<String>> entry : addMap.entrySet()) {
			if (combinedMap.containsKey(entry.getKey())) {
				continue;
			}
			combinedMap.put(entry.getKey(), getSizedArray(startInd, size, entry.getValue()));
		}
		return combinedMap;
	}

	private static List<String> getSizedArray(int startInd, int size, List<String> data) {
		List<String> list = constrArray(startInd, data);
		for (int i = (size - list.size()); i < size; i++) {
			list.add("0");
		}
		return list;
	}

	private static Map<String, List<String>> appendToMapLists(Map<String, List<String>> combinedMap,
			Map<String, List<String>> addMap, int addIndex) {
		for (Map.Entry<String, List<String>> entry : addMap.entrySet()) {
			if (combinedMap.containsKey(entry.getKey())) {
				combinedMap.get(entry.getKey()).addAll(addIndex, entry.getValue());
				continue;
			}

			combinedMap.put(entry.getKey(), constrArray(addIndex, entry.getValue()));
		}
		return combinedMap;
	}

	private static int getEntryIndexForLists(List<String> assemTime, List<String> addTime) {
		LocalDateTime dT1Start = formatDateTimeString(assemTime.get(0));
		LocalDateTime dT2Start = formatDateTimeString(addTime.get(0));
		int interval = (int) Duration.between(dT1Start, dT2Start).getSeconds();
		interval = (interval <= 0 ? 0 : (interval > assemTime.size() ? assemTime.size() : interval));
		return interval;
	}

	private static LocalDateTime formatDateTimeString(String dateTimeString) {
		Matcher matcher = Pattern.compile("\\d{4}-\\d{2}-\\d{2}[\\sT]\\d{2}:\\d{2}:\\d{2}").matcher(dateTimeString);
		if (matcher.find()) {
			return LocalDateTime.parse(dateTimeString.replace(" ", "T"));
		}
		return formatMDYDateTime(dateTimeString);
	}

	private static LocalDateTime formatMDYDateTime(String dateTimeString) {
		Matcher matcher = Pattern.compile("(\\d{2})/(\\d{2})/(\\d{4}).?(.+)").matcher(dateTimeString);
		String month = "01";
		String day = "01";
		String year = "1970";
		String time = "00:00:00";
		if (matcher.find()) {
			month = getLeadingZeros(matcher.group(1));
			day = getLeadingZeros(matcher.group(2));
			year = matcher.group(3);
			time = matcher.group(4);
		} else {
			System.out.println("Fucked up dateTimeString: " + dateTimeString);
		}
		return LocalDateTime.parse((year + "-" + month + "-" + day + "T" + time));
	}

	private static String getLeadingZeros(String intString) {
		String wZero = "0" + intString;
		wZero = wZero.substring(wZero.length() - 2);
		return wZero;
	}

	private static List<String> constrArray(int size, List<String> appendValues) {
		List<String> list = getArrayOfZeros(size);
		list.addAll(appendValues);
		return list;
	}

	private static List<String> getArrayOfZeros(int size) {
		List<String> list = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			list.add("0");
		}
		return list;
	}

}
