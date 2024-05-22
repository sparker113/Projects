import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import mrl.DataHandling;
import mrl.JobRequest;
import mrl.Login;

public class StartUp {

	private ExecutorService executor = Executors.newCachedThreadPool();

	private Map<String, Map<String, String>> jobInfoMap;
	private CompletableFuture<Map<String, Map<String, String>>> jobInfoFuture = new CompletableFuture<>();
	private Map<String, String> cachedJobMap;

	public StartUp(String username, String password) throws InterruptedException, ExecutionException {
		setCreds(username, password);
		startGettingCookies();
		startSettingJobInfo();
		setCachedJobMap();
	}

	public Map<String, String> getCachedMap() {
		return cachedJobMap;
	}

	public List<String> getCachedList() throws ClassNotFoundException, IOException, InterruptedException {
		List<String> cachedList = new ArrayList<>();
		cachedList.addAll(getCachedJobsFromDir().keySet());
		return cachedList;
	}

	private void setCachedJobMap() throws InterruptedException, ExecutionException {
		Map<String, Map<String, String>> jobInfo = getJobInfo();
		String[] jobIDs = getJobIDList(jobInfo.keySet());
		this.cachedJobMap = constrCachedJobs(jobInfo, jobIDs);
	}
	private String[] getJobIDList(Set<String> jobIDs) {
		String[] jobIDList = new String[jobIDs.size()];
		int i = 0;
		for(String s:jobIDs) {
			jobIDList[i] = s;
		}
		return jobIDList;
	}
	public static Map<String,String> getCachedJobsFromDir() throws ClassNotFoundException, IOException, InterruptedException{
		Map<String,Map<String,String>> jobInfo = loadJobInfoFile();
		File jobDataDir = new File(DataHandling.JOB_DATA_DIR);
		String[] jobIDs = jobDataDir.list();
		return constrCachedJobs(jobInfo, jobIDs);
	}

	private static Map<String, String> constrCachedJobs(Map<String, Map<String, String>> jobInfo, String[] jobIDs) {
		Map<String, String> cachedMap = new LinkedHashMap<>();
		for (String s : jobIDs) {
			System.out.println(s);
			System.out.println(jobInfo.get(s));
			if(!jobInfo.containsKey(JobRequest.JobInformation.PROJECT_ID)) {
				continue;
			}
			cachedMap.put(jobInfo.get(s).get(JobRequest.JobInformation.PROJECT_ID) + "_" + s, s);
		}
		return cachedMap;
	}

	public List<String> getJobInfoList() throws InterruptedException, ExecutionException {
		Map<String, Map<String, String>> jobInfo = getJobInfo();
		List<String> jobInfoList = new ArrayList<>();
		for (Map.Entry<String, Map<String, String>> entry : jobInfo.entrySet()) {
			jobInfoList.add(getJobIdentString(entry.getValue()));
		}
		return jobInfoList;
	}

	public Map<String, String> getJobInfoMap() throws InterruptedException, ExecutionException {
		Map<String, Map<String, String>> jobInfo = getJobInfo();
		Map<String, String> jobInfoMap = new LinkedHashMap<>();
		for (Map.Entry<String, Map<String, String>> entry : jobInfo.entrySet()) {
			jobInfoMap.put(getJobIdentString(entry.getValue()),
					entry.getValue().get(JobRequest.JobInformation.JOB_INDEX));
		}
		return jobInfoMap;
	}
	public String getJobID(String displayName) {
		return cachedJobMap.get(displayName);
	}
	private String getJobIdentString(Map<String, String> singleJob) {
		return singleJob.get(JobRequest.JobInformation.PROJECT_ID) + "_"
				+ singleJob.get(JobRequest.JobInformation.JOB_INDEX);
	}

	private void startSettingJobInfo() {
		executor.execute(() -> {
			try {
				loadJobInfo();
			} catch (ClassNotFoundException | IOException | InterruptedException e) {
				e.printStackTrace();
				jobInfoFuture.complete(new LinkedHashMap<>());
				return;
			}
		});
	}

	public final static String JOB_INFO_PARENT = "job_info/";
	public final static String JOB_INFO_FILE = "job_info.map";
	public final static String JOB_INFO_DIR = DataHandling.DATA_DIR + JOB_INFO_PARENT;

	@SuppressWarnings("unchecked")
	private void loadJobInfo() throws ClassNotFoundException, IOException, InterruptedException {
		File file = new File(JOB_INFO_DIR + JOB_INFO_FILE);
		setJobInfo((Map<String, Map<String, String>>) DataHandling.readObjFromFile(new LinkedHashMap<>().getClass(),file));
		importJobInfo();

	}
	@SuppressWarnings("unchecked")
	public static Map<String,Map<String,String>> loadJobInfoFile() throws ClassNotFoundException, IOException, InterruptedException {
		File file = new File(JOB_INFO_DIR + JOB_INFO_FILE);
		
		return (Map<String, Map<String, String>>) DataHandling.readObjFromFile(new LinkedHashMap<>().getClass(),file);
	}

	@SuppressWarnings("unchecked")
	public void addToSavedJobInfo(Map<String, Map<String, String>> addJobInfo)
			throws ClassNotFoundException, IOException {
		File file = new File(JOB_INFO_DIR + JOB_INFO_FILE);
		if (!file.exists()) {
			setJobInfo(addJobInfo);
			return;
		}
		Map<String, Map<String, String>> savedJobInfo = (LinkedHashMap<String, Map<String, String>>) DataHandling
				.readObjFromFile(addJobInfo.getClass(), file);
		savedJobInfo.putAll(addJobInfo);
		setJobInfo(savedJobInfo);
	}

	private final static int DEFAULT_NUM_JOBS = 50;

	private void importJobInfo() throws InterruptedException, IOException {
		importJobInfo(DEFAULT_NUM_JOBS);
	}

	public final static int JOB_INFO_INCR = 50;

	private void importJobInfo(int numJobs) throws InterruptedException, IOException {
		String cookies = getCookies();
		JobRequest info = new JobRequest(cookies);
		for (int i = 0; i < numJobs; i += JOB_INFO_INCR) {
			System.out.println(i);
			info.addJobs(i);
		}
		info.requestJobInfo();
		setJobInfo(info.getJobInfoMap());
	}

	private void setJobInfo(Map<String, Map<String, String>> jobInfo) {
		jobInfoFuture.complete(jobInfo);
	}

	public Map<String, Map<String, String>> getJobInfo() throws InterruptedException, ExecutionException {
		if (jobInfoMap == null) {
			this.jobInfoMap = jobInfoFuture.get();
		}
		return this.jobInfoMap;
	}

	private String username;
	private String password;

	private void setCreds(String username, String password) {
		this.username = username;
		this.password = password;
	}

	private String cookieString;
	private Semaphore cookieSem = new Semaphore(0);

	private void startGettingCookies() {
		executor.execute(() -> {
			try {
				setCookies();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
				cookieSem.release();
				return;
			}
		});
	}

	public String getCookies() throws InterruptedException {
		if (cookieString == null) {
			cookieSem.acquire();
		}
		return cookieString;
	}

	private void setCookies() throws IOException, InterruptedException {
		List<String> cookies = Login.makeSessionIDRequest(Login.MRL_LOGIN_URL, username, password);
		Login login = new Login(cookies);
		this.cookieString = login.getCookieString();
		cookieSem.release();
	}

	public void requestJobInfo() {

	}

	public void requestJobInfo(int startIndex) {

	}
}
