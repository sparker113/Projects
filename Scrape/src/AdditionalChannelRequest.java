import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.zip.DataFormatException;

import intelie.DataRequest;
import intelie.RememberMe;

public class AdditionalChannelRequest extends LinkedHashMap<String, ArrayList<String>> {
	Semaphore semaphore;
	String csrfToken;
	RememberMe rememberMe;
	String sessionId;
	Boolean madeRequest;

	AdditionalChannelRequest(Semaphore semaphore) {
		this.madeRequest = false;
		this.semaphore = semaphore;
	}

	AdditionalChannelRequest(Semaphore semaphore, String csrfToken, String sessionId)
			throws IOException, ClassNotFoundException, InterruptedException {
		this.madeRequest = false;
		this.semaphore = semaphore;
		this.csrfToken = csrfToken;
		this.sessionId = sessionId;
		setRememberMe();
	}

	public void makeRequest(ArrayList<String> channels, String crew, String start, String end)
			throws IOException, InterruptedException, ExecutionException, DataFormatException {
		if (channels == null || channels.isEmpty()) {
			madeRequest = true;
			semaphore.release();
			return;
		}
		System.out.println("Channels for Additional Requests: " + channels);
		DataRequest dataRequest = new DataRequest(csrfToken, sessionId,
				DataRequest.getPostBody(crew, start, end, channels), channels.size(), rememberMe.getCookie());
		LinkedHashMap<String, ArrayList<String>> additionalMap = dataRequest.makeRequest();
		fixMapEntries(additionalMap, channels);
		setThisMap(additionalMap);
		this.madeRequest = true;
	}

	private void fixMapEntries(LinkedHashMap<String, ArrayList<String>> additionalMap, ArrayList<String> channels) {
		if(additionalMap ==null) {
			return;
		}
		String[] removeArray = new String[1];
		int i = 0;
		for (String s : additionalMap.keySet()) {
			if (!channels.contains(s)) {
				removeArray = checkArrayAllocateMemory(removeArray, i, s);
				i++;
			}
		}
		removeKeysFromMap(additionalMap, removeArray);
		removeFirstIndices(additionalMap);
	}
	///////TEMPORARY FIX TO ISSUE WHEN PARSING DATA RESPONSE, 'Anomalous' format of first data point
		//forces an entry of 0
	private void removeFirstIndices(LinkedHashMap<String,ArrayList<String>> additionalMap) {
		for(String s:additionalMap.keySet()) {
			if(additionalMap.get(s)!=null&&additionalMap.get(s).size()>0) {
				additionalMap.get(s).remove(0);
			}
		}
	}
	private void removeKeysFromMap(LinkedHashMap<String, ArrayList<String>> additionalMap, String[] removeArray) {
		for (String s : removeArray) {
			System.out.println("Removed: " + s);
			additionalMap.remove(s);
		}
	}

	private String[] checkArrayAllocateMemory(String[] removeArray, int i, String channelName) {
		if (i >= removeArray.length) {
			int count = 0;
			String[] tempArray = new String[i + 1];
			for (String s : removeArray) {
				tempArray[count] = s;
				count++;
			}
			tempArray[count] = channelName;
			removeArray = tempArray;
		} else {
			removeArray[i] = channelName;
		}
		return removeArray;
	}

	private void setRememberMe() throws IOException, ClassNotFoundException, InterruptedException {
		this.rememberMe = RememberMe.readCookie();
	}

	public void setThisMap(LinkedHashMap<String, ArrayList<String>> additionalDataMap) {
		if (additionalDataMap != null) {
			this.putAll(additionalDataMap);
		}
		semaphore.release();
	}

	public LinkedHashMap<String, ArrayList<String>> getThisMap() {
		if (madeRequest) {
			return this;
		}
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			System.out.println("InterruptedException>>AdditionalChannelRequest::getThisMap");
		}
		return this;
	}
}
