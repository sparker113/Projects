package mrl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.FileChannel;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JobRequest {
	String cookies;
	JobInformation jobInformation;
	public JobRequest(String cookies) {
		this.cookies = cookies;
		jobInformation = new JobInformation(cookies,false);
	}
	
	public final static int DEFAULT_NUM_JOBS = 50;
	public final static int JOB_REQUEST_INTERVAL = 50;
	public void addJobs(int startIndex) throws InterruptedException, IOException {
		jobInformation.addJobs(startIndex);
	}
	public final static String JOB_INFO_PARENT = "job_info/";
	public final static String JOB_INFO_FILE = "job_info.map";
	public final static String JOB_INFO_DIR = DataHandling.DATA_DIR+JOB_INFO_PARENT;
	public void requestJobInfo() throws InterruptedException {
		List<String> jobIDs = jobInformation.getJobIDList();
		if(jobIDs.size()==0) {
			return;
		}
		CountDownLatch latch = new CountDownLatch(jobIDs.size());
		System.out.println("jobIDs size: "+jobIDs.size());
		ExecutorService executor = Executors.newCachedThreadPool();
		LocalDateTime time1 = LocalDateTime.now();
		for(String s:jobIDs) {
			executor.execute(()->{
				try {
					jobInformation.parseSingleJobInfo(s,JobRequest.makeJobInfoRequest(cookies,s));
					System.out.println(latch.getCount());
					latch.countDown();
				}catch(IOException|InterruptedException e) {
					latch.countDown();
					e.printStackTrace();
				}
			});
		}
		//System.out.println(latch.getCount());
		latch.await();
		executor.shutdown();
		jobInformation.releasePermit();
		System.out.println("jobInformation map saved");
		saveJobInfoMap(jobInformation.getInstJobInfoMap(), JOB_INFO_DIR+JOB_INFO_FILE);
		LocalDateTime time2 = LocalDateTime.now();
		System.out.println(Duration.between(time1,time2).toMillis());
	}
	public void requestJobInfo(boolean saveJobs) throws InterruptedException {
		List<String> jobIDs = jobInformation.getJobIDList();
		if(jobIDs.size()==0) {
			return;
		}
		CountDownLatch latch = new CountDownLatch(jobIDs.size());
		System.out.println("jobIDs size: "+jobIDs.size());
		ExecutorService executor = Executors.newCachedThreadPool();
		LocalDateTime time1 = LocalDateTime.now();
		for(String s:jobIDs) {
			executor.execute(()->{
				try {
					jobInformation.parseSingleJobInfo(s,JobRequest.makeJobInfoRequest(cookies,s));
					System.out.println(latch.getCount());
					latch.countDown();
				}catch(IOException|InterruptedException e) {
					latch.countDown();
					e.printStackTrace();
				}
			});
		}
		//System.out.println(latch.getCount());
		latch.await();
		executor.shutdown();
		jobInformation.releasePermit();
		System.out.println("jobInformation map saved");
		if(saveJobs) {
			saveJobInfoMap(jobInformation.getInstJobInfoMap(), JOB_INFO_DIR+JOB_INFO_FILE);
		}
		LocalDateTime time2 = LocalDateTime.now();
		System.out.println(Duration.between(time1,time2).toMillis());
	}
	public List<String> getJobInfoList() throws InterruptedException, ExecutionException{
		Map<String,Map<String,String>> jobInfo = getJobInfoMap();
		List<String> jobInfoList = new ArrayList<>();
		for(Map.Entry<String,Map<String,String>> entry:jobInfo.entrySet()) {
			jobInfoList.add(getJobIdentString(entry.getValue()));
		}
		return jobInfoList;
	}
	private String getJobIdentString(Map<String,String> singleJob) {
		return singleJob.get(JobRequest.JobInformation.PROJECT_ID)+"_"+singleJob.get(JobRequest.JobInformation.JOB_INDEX);
	}
	public static void saveJobInfoMap(Map<String,Map<String,String>> jobInfo,String fileDir) {
		Executors.newSingleThreadExecutor().execute(()->{
			try {
				
				
				DataHandling.writeObjToFile(jobInfo,fileDir);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println(fileDir);
			}
		});
	}
	
	public Map<String,Map<String,String>> getJobInfoMap() throws InterruptedException{
		return jobInformation.getInstJobInfoMap();
	}
	public List<String> getJobIDList(){
		return jobInformation.getJobIDList();
	}
	public final static String JOB_LIST_URL = "https://shear.mrlsolutions.com/JobManager/index.php";

	public static InputStream makeJobListRequest(String cookies) throws InterruptedException, IOException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().headers(getRequestHeaderString(cookies))
				.uri(URI.create(JOB_LIST_URL)).GET().build();
		HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		return response.body();
	}

	public static String makeJobListRequestForString(String cookies) throws InterruptedException, IOException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().headers(getRequestHeaderString(cookies))
				.uri(URI.create(JOB_LIST_URL)).GET().build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		return response.body();
	}

	public final static String INDEXED_JOB_LIST_URL = "https://shear.mrlsolutions.com/index.php?menuaction=JobManager.ui.admin";//;&action=show&type=jobs&Department=4&Division=7&query=&Status=&sort=DESC&order=id&ViewType=";

	public static InputStream makeJobListRequest(String cookies, int jobStartIndex)
			throws InterruptedException, IOException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().headers(getRequestHeaderString(cookies))
				.uri(URI.create(INDEXED_JOB_LIST_URL)).POST(HttpRequest.BodyPublishers.ofString(getIndexedJobListForm(jobStartIndex))).build();
		HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		return response.body();
	}
	public static String makeJobListRequestForString(String cookies, int jobStartIndex)
			throws InterruptedException, IOException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().headers(getRequestHeaderString(cookies))
				.uri(URI.create(INDEXED_JOB_LIST_URL)).POST(HttpRequest.BodyPublishers.ofString(getIndexedJobListForm(jobStartIndex))).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		return response.body();
	}
	
	public final static String JOB_ID_PLACEHOLDER = "@JOB ID PLACEHOLDER@";
	public final static String JOB_INFO_URL_TEMP = "https://shear.mrlsolutions.com/index.php?menuaction=JobManager.ui.admin&type=job&job_id="+JOB_ID_PLACEHOLDER+"&Department=4&Division=7&query=&Status=&GroupingSelect=&sort=DESC&order=id&action=view";
	public static String makeJobInfoRequest(String cookies,String jobID) throws InterruptedException,IOException{
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().headers(getRequestHeaderString(cookies))
				.uri(getJobInfoURI(jobID)).GET().build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		return response.body();
	}
	private static URI getJobInfoURI(String jobID) {
		String jobInfoURL = JOB_INFO_URL_TEMP.replace(JOB_ID_PLACEHOLDER, jobID);
		return URI.create(jobInfoURL);
	}
	private final static String JOB_INDEX_PLACEHOLDER = "@JOB INDEX@";
	private final static String INDEXED_FORM_TEMP = "order=DateTimeStart&sort=DESC&action=show&type=jobs&Department=4&Division=7&start="+JOB_INDEX_PLACEHOLDER+"&start.x=7&start.y=11";
	private static String getIndexedJobListForm(int startIndex) {
		return INDEXED_FORM_TEMP.replace(JOB_INDEX_PLACEHOLDER, String.valueOf(startIndex));
	}
	private static void printStream(InputStream inputStream) {
		Scanner scanner = new Scanner(inputStream);
		scanner.useDelimiter("\n");
		while (scanner.hasNext()) {
			System.out.println(scanner.next());
		}
		scanner.close();
	}

	private static Map<String, String> getInitRequestHeaders(String cookieString) {
		Map<String, String> map = new HashMap<>();
		map.put("Accept-Encoding", "gzip, deflate, br, zstd");
		map.put("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
		map.put("Content-Type", "application/x-www-form-urlencoded");
		map.put("Cookie", cookieString);
		map.put("Upgrade-Insecure-Requests", "1");
		map.put("User-Agent",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");
		return map;
	}

	public static String[] getRequestHeaderString(String cookieString) {
		Map<String, String> map = getInitRequestHeaders(cookieString);
		String[] headers = new String[map.size() * 2];
		int i = 0;
		for (Map.Entry<String, String> entry : map.entrySet()) {
			headers[i] = entry.getKey();
			i++;
			headers[i] = entry.getValue();
			i++;
		}
		return headers;
	}

	public class JobInformation {
		ExecutorService executor;
		Queue<String> jobInfoQueue;
		List<String> jobIDList;
		private Map<String, Map<String, String>> jobInfo;
		boolean async = false;
		String cookies;
		Semaphore mapSetSem;
		public JobInformation(String cookies,boolean async) {
			this.async = async;
			this.cookies = cookies;
			this.mapSetSem = new Semaphore(0);
			if(async) {
				instAsyncObjs();
			}else {
				instObjs();
			}
		}

		public JobInformation(InputStream jobReqResponse) throws InterruptedException {
			instAsyncObjs();
			parseJobInfoResponse(jobReqResponse);
		}

		public JobInformation(String jobReqResponse) {
			instObjs();
			parseJobInfoResponse(jobReqResponse);
		}

		public void addJobsAsync(int jobStartIndex) {
			reInstAsyncRes();
			executor.execute(() -> {
					
			});
		}
		public void addJobs(int jobStartIndex) throws InterruptedException, IOException {
			parseJobInfoResponse(makeJobListRequestForString(cookies,jobStartIndex));
		}
		protected void releasePermit() {
			mapSetSem.release();
		}
		public Map<String,Map<String,String>> getInstJobInfoMap() throws InterruptedException{
			mapSetSem.acquire();
			releasePermit();
			return jobInfo;
		}
		public static String getJobConfigTable(String response) {
			Matcher matcher = Pattern.compile("<table id=\"JobConfiguration\".+?>((.+|[\\r\\n\\s]+)+?)</table>").matcher(response);
			if(matcher.find()) {
				return matcher.group(1);
			}
			return "";
		}
		
		public void parseSingleJobInfo(String jobID,String response) {
			String jobConfigTable = getJobConfigTable(response);
			Matcher matcher = Pattern.compile("<tr.*?>((.*|[\\r\\n\\s]*)*?)</tr>").matcher(jobConfigTable);
			List<String> list = new ArrayList<>();
			while(matcher.find()) {
				Matcher matcher2 = Pattern.compile("<td.*?>(.*?)</td>").matcher(matcher.group(1));
				while(matcher2.find()) {
					list.add(matcher2.group(1));
				}
			}
			addJobInfoToMap(jobID,list);
		}
		
		private synchronized void addJobInfoToMap(String jobID,List<String> jobInfoArr) {
			Map<String,String> map = getJobInfoMap(jobInfoArr);
			map.put(JobInformation.JOB_INDEX, jobID);
			jobInfo.put(jobID,map);
		}

		private Map<String,String> getJobInfoMap(List<String> jobInfo){
			Map<String,String> map = new HashMap<>();
			for(int i = 0;i<jobInfo.size();i+=2) {
				map.put(jobInfo.get(i), jobInfo.get(i+1));
			}
			return map;
		}
		private void instAsyncObjs() {
			executor = Executors.newCachedThreadPool();
			jobInfoQueue = new LinkedBlockingQueue<>();
			headerIndexMap = new HashMap<>();
			jobIDList = new ArrayList<>();
			jobInfo = new LinkedHashMap<>();
		}
		private void instObjs() {
			headerIndexMap = new HashMap<>();
			jobIDList = new ArrayList<>();
			jobInfo = new LinkedHashMap<>();
		}
		public List<String> getJobIDList(){
			return this.jobIDList;
		}
		private void addJobIDs(String jobIDString) {
			for(String id:jobIDString.split(",")) {
				jobIDList.add(id);
			}
		}
		private final static int JOB_TABLE_INDEX = 2;

		private Map<String, Map<String, String>> parseJobInfoResponse(String jobReqResponse) {
			String[] tables = jobReqResponse.split("<table class=\"maintable\"");
			addJobIDs(checkForJobList(tables[0]));
			String jobTable = tables[JOB_TABLE_INDEX];
			List<String> jobResponseList = splitResponseByJob(jobTable);
			headerIndexMap.putAll(getIndexMap(jobResponseList.remove(0)));
			for (String s : jobResponseList) {
				Map<String, String> singleJobMap = getSingleJobMap(headerIndexMap, s);
				if (singleJobMap == null) {
					continue;
				}
				addJobToMap(singleJobMap);
			}
			return jobInfo;
		}

		private void addJobToMap(Map<String, String> singleJobInfo) {
			String projID = getNextIndexedID(jobInfo.keySet(), singleJobInfo.get(PROJECT_ID));
			jobInfo.put(projID, singleJobInfo);
		}

		private String getNextIndexedID(Set<String> ids, String thisID) {
			String indexID = thisID;
			int i = 1;
			while (ids.contains(indexID)) {
				indexID = thisID + "_" + i;
				i++;
			}
			return indexID;
		}

		public Map<String, Map<String, String>> getJobInfoMap() {
			return this.jobInfo;
		}

		public final static String GROUP = "Group";
		public final static String LOCATION = "Location";
		public final static String PAD_NAME = "Pad Name";
		public final static String STAGE = "Stage";
		public final static String PROJECT_ID = "Project ID";
		public final static String START_DATE = "Start Date";
		public final static String UNIT = "Unit";
		public final static String JOB_INDEX = "Job Index";

		private Map<Integer, String> getIndexMap(String jobHeaderString) {
			List<String> headers = getJobInfoList(jobHeaderString);
			Map<Integer, String> indexMap = new HashMap<>();
			int i = 0;
			for (String s : headers) {
				indexMap.put(i, s);
				i++;
			}
			return indexMap;
		}

		private Map<String, String> getSingleJobMap(Map<Integer, String> headerIndexMap, String jobString) {
			String jobIndex = "";
			Matcher matcher = Pattern.compile("id=\"row(\\d+?)\"").matcher(jobString);
			if (matcher.find()) {
				jobIndex = removeLeadingZeros(matcher.group(1));
			} else {
				return null;
			}
			List<String> jobInfo = getJobInfoList(jobString);
			Map<String, String> infoMap = new HashMap<>();
			infoMap.put(JOB_INDEX, jobIndex);
			for (int i = 0; i < jobInfo.size(); i++) {
				if (!headerIndexMap.containsKey(i)) {
					break;
				}
				infoMap.put(headerIndexMap.get(i), jobInfo.get(i).trim());
			}
			if (!checkStartDate(infoMap.get(START_DATE))) {
				return null;
			}
			return infoMap;
		}

		private static boolean checkStartDate(String startDateString) {
			return startDateString.matches("\\d{4}\\-\\d{2}\\-\\d{2}.+");
		}

		public static List<String> getJobInfoList(String jobString) {
			List<String> list = new ArrayList<>();
			Matcher matcher = Pattern.compile(".+?>([^<>]*?)</.+?>").matcher(jobString);
			while (matcher.find()) {
				String found = matcher.group(1);
				found = (found.equals("") ? " " : found);
				list.add(found);
			}
			return list;
		}

		public static String removeLeadingZeros(String num) {
			Matcher matcher = Pattern.compile("^(0+)").matcher(num);
			if (matcher.find()) {
				return matcher.replaceFirst("");
			}
			return num;
		}

		private List<String> splitResponseByJob(String jobTable) {
			List<String> jobResponseList = new ArrayList<>();
			Matcher matcher = Pattern.compile("<tr[.\\n\\r\\t\\w\\s\\S]+?</tr>").matcher(jobTable);
			while (matcher.find()) {
				jobResponseList.add(matcher.group());
			}
			jobResponseList.remove(0);
			return jobResponseList;

		}

		private boolean waiting = true;
		private boolean done = false;
		private final static int HEADERS_ENTRY = 1;
		private int countEntries = 0;

		private int addCompJobInfoToQueue(String jobInfo, Semaphore semaphore) {
			Matcher matcher = Pattern.compile("<tr[.\\n\\r\\t\\w\\s\\S]+?</tr>").matcher(jobInfo);
			int end = 0;
			while (matcher.find()) {
				jobInfoQueue.add(matcher.group());
				if (waiting == true) {
					semaphore.release();
				}
				end = matcher.end();
			}
			return end;
		}

		private Map<Integer, String> headerIndexMap;

		private void addInfoToMapFromQueue(Semaphore semaphore) throws InterruptedException {

			while (!jobInfoQueue.isEmpty()) {
				String jobInfoString = jobInfoQueue.poll();
				if (countEntries == HEADERS_ENTRY) {
					headerIndexMap = getIndexMap(jobInfoString);
					countEntries++;
					continue;
				} else if (countEntries == 0) {
					countEntries++;
					continue;
				}

				Map<String, String> singleJobMap = getSingleJobMap(headerIndexMap, jobInfoString);
				if (singleJobMap == null) {
					System.out.println("NULL MAP FROM STRING: " + jobInfoString);
					continue;
				}
				addJobToMap(singleJobMap);
				countEntries++;

			}
			waiting = true;
			if (!done) {
				semaphore.acquire();
				waiting = false;
			}

		}

		private void reInstAsyncRes() {
			if (executor.isShutdown()) {
				executor = Executors.newCachedThreadPool();
			}
			;
		}

		private Map<String, Map<String, String>> parseJobInfoResponse(InputStream jobReqResponse)
				throws InterruptedException {
			Semaphore semaphore = new Semaphore(0);
			executor.execute(() -> {
				try {
					readJobInfoResponse(jobReqResponse, semaphore);
					done = true;
					if (waiting) {
						semaphore.release();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			executor.execute(() -> {
				while (!done) {
					try {
						addInfoToMapFromQueue(semaphore);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				semaphore.release();
			});
			semaphore.acquire();
			executor.shutdown();
			return getJobInfoMap();
		}

		private int mainTableCount = 0;

		private int checkForTable(String responseString) {
			int patternLength = 23;
			Matcher matcher = Pattern.compile("<table class=\"maintable\"").matcher(responseString);
			if (matcher.find()) {
				mainTableCount++;
				return matcher.end();
			}
			if (responseString.length() > patternLength) {
				return responseString.length() - patternLength;
			}
			return 0;
		}
		private String checkForJobList(String responseString) {
			Matcher matcher = Pattern.compile("\"job_list\".value=\"(.+?)\"").matcher(responseString);
			if(matcher.find()) {
				return matcher.group(1);
			}
			return "";
		}

		private void readJobInfoResponse(InputStream inputStream, Semaphore semaphore) throws IOException {
			int chunk = 2048;
			byte[] bytes = new byte[chunk];
			StringBuilder stringBuilder = new StringBuilder();
			String jobList = "";
			int i;
			while ((i = inputStream.read(bytes)) > -1 & mainTableCount < JOB_TABLE_INDEX) {
				String readBytes = getStringFromBytes(bytes);
				stringBuilder.append(readBytes);
				if(jobList.equals("")) {
					jobList = checkForJobList(readBytes);
				}else {
					addJobIDs(jobList);
				}
				int startIndex = checkForTable(stringBuilder.toString());
				stringBuilder.delete(0, startIndex);
				bytes = new byte[chunk];
			}
			while ((i = inputStream.read(bytes)) > -1 & mainTableCount == JOB_TABLE_INDEX) {
				stringBuilder.append(getStringFromBytes(bytes));
				checkForTable(stringBuilder.toString());
				stringBuilder.delete(0, addCompJobInfoToQueue(stringBuilder.toString(), semaphore));
				bytes = new byte[chunk];
			}

		}
		private String getStringFromBytes(byte[] bytes) {
			int i = 0;
			String string = "";
			for(byte b:bytes) {
				if(b==0) {
					return string;
				}
				string+=(char)b;
			}
			return string;
		}

		// base 0
		public final static Integer GROUP_INDEX = 0;
		public final static Integer LOCATION_INDEX = 1;
		public final static Integer PAD_NAME_INDEX = 2;
		public final static Integer STAGE_INDEX = 3;
		public final static Integer PROJECT_ID_INDEX = 4;
		public final static Integer START_DATE_INDEX = 5;
		public final static Integer UNIT_INDEX = 6;
	}

}
