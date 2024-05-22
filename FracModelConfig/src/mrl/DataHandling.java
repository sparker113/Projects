package mrl;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DataHandling {
	public final static String DATA_DIR = "data/";
	public final static String JOB_DATA_DIR = DATA_DIR+"job_data/";
	public static void writeDataToFiles(Map<LocalDate,Map<String,List<String>>> parsedMap,String jobName) throws IOException{
		String jobDir = DATA_DIR+jobName+"/";
		for(LocalDate localDate:parsedMap.keySet()) {
			writeObjToFile(parsedMap.get(localDate),jobDir+localDate.toString());
		}
	}
	public final static String DATA_EXT = ".map";
	public static String getJobDataDirForDate(LocalDate localDate,String jobID) {
		return JOB_DATA_DIR+jobID+"/"+localDate.toString()+DATA_EXT;
	}
	public static String getJobDataDir(String jobID) {
		return JOB_DATA_DIR+jobID+"/";
	}
	public static <T> void writeObjToFile(T t,String dir) throws IOException{
		File file = new File(dir);
		if(file.getParentFile()!=null&&!file.getParentFile().exists()){
			file.getParentFile().mkdirs();
		}
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(file));
		objectOutputStream.writeObject(t);
		objectOutputStream.close();
	}
	
	public static synchronized <T> T readObjFromFile(Class<T> tClass,File file) throws IOException,ClassNotFoundException{
		ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
		T t = tClass.cast(objectInputStream.readObject());
		objectInputStream.close();
		return t;
	}
	@SuppressWarnings("unchecked")
	public static Map<String,List<String>> getJobDataForDate(LocalDate date,String jobID) throws ClassNotFoundException, IOException{
		File file = new File(getJobDataDirForDate(date,jobID));
		if(!file.exists()) {
			return null;
		}
		return (Map<String,List<String>>)readObjFromFile(HashMap.class,file);
	}
	public static Map<LocalDate,Map<String,List<String>>> parseDataByDate(Map<String,List<String>> dataMap){
		Map<LocalDate,Integer> indexMap = getStartIndeces(dataMap.get(DataRequest.DataChannels.TIME.getValue()));
		Map<LocalDate,Map<String,List<String>>> parsedMap = parseData(dataMap,indexMap);
		return parsedMap;
	}
	
	private static Map<LocalDate,Map<String,List<String>>> parseData(Map<String,List<String>> dataMap,Map<LocalDate,Integer> indexMap){
		Map<LocalDate,Map<String,List<String>>> parsedMap = new HashMap<>();
		int lastInd = -1;
		LocalDate lastDate = null;
		for(Map.Entry<LocalDate,Integer> entry: indexMap.entrySet()) {
			if(lastInd==-1) {
				lastInd = 0;
				lastDate = entry.getKey();
				continue;
			}
			
			parsedMap.put(lastDate, getTrimmedMapArrays(dataMap,lastInd,entry.getValue()));
			lastInd = entry.getValue();
			lastDate = entry.getKey();
		}
		parsedMap.put(lastDate, getTrimmedMapArrays(dataMap,lastInd));
		return parsedMap;
	}
	
	private static Map<String,List<String>> getTrimmedMapArrays(Map<String,List<String>> dataMap,int start){
		Map<String,List<String>> trimmed = new LinkedHashMap<>();
		for(Map.Entry<String,List<String>> entry:dataMap.entrySet()) {
			List<String> reducArr = new ArrayList<>();
			for(int i = start;i<entry.getValue().size();i++) {
				reducArr.add(entry.getValue().get(i));
			}
			trimmed.put(entry.getKey(), reducArr);
		}
		return trimmed;
	}
	
	private static Map<String,List<String>> getTrimmedMapArrays(Map<String,List<String>> dataMap,int start,int end){
		Map<String,List<String>> trimmed = new LinkedHashMap<>();
		for(Map.Entry<String,List<String>> entry:dataMap.entrySet()) {
			List<String> reducArr = new ArrayList<>();
			for(int i = start;i<end;i++) {
				reducArr.add(entry.getValue().get(i));
			}
			trimmed.put(entry.getKey(), reducArr);
		}
		return trimmed;
	}
	
	private static Map<LocalDate,Integer> getStartIndeces(List<String> dateArr){
		LocalDate firstDate = LocalDateTime.parse(dateArr.get(0).replace(" ","T")).toLocalDate();
		Map<LocalDate,Integer> map = new LinkedHashMap<>();
		map.put(firstDate, 0);
		int endOfFirst = getEndOfFirstDay(dateArr,firstDate);
		//map.put(firstDate.plusDays(1l), endOfFirst);
		getRestOfDays(dateArr,endOfFirst,map);
		return map;
		
	}
	private static Map<LocalDate,Integer> getRestOfDays(List<String> dateArr,int startInd,Map<LocalDate,Integer> map){
		int countInt = 86400;
		for(int i = startInd;i<dateArr.size();i+=countInt) {
			LocalDate date = LocalDateTime.parse(dateArr.get(i).replace(" ","T")).toLocalDate();
			map.put(date, i);
		}
		return map;
	}
	private static int getEndOfFirstDay(List<String> dateArr,LocalDate firstDate) {
		int index = 0;
		for(int i = (dateArr.size()>600?600:dateArr.size()-1);i<dateArr.size();i+=600) {
			LocalDate nextDate = LocalDateTime.parse(dateArr.get(i).replace(" ", "T")).toLocalDate();
			if(!nextDate.equals(firstDate)) {
				return indexBack(dateArr,i,firstDate);
			}
			index = i;
			
		}
		return indexForward(dateArr,index,firstDate);
		
	}
	private static int indexForward(List<String> dateArr,int startIndex,LocalDate firstDate) {
		for(int i = startIndex;i<dateArr.size();i++) {
			LocalDate nextDate = LocalDateTime.parse(dateArr.get(i).replace(" ", "T")).toLocalDate();
			if(nextDate.equals(firstDate)) {
				return i;
			}
		}
		return dateArr.size();
	}
	private static int indexBack(List<String> dateArr,int startIndex,LocalDate firstDate) {
		for(int i = startIndex;i>0;i--) {
			LocalDate prevDate = LocalDateTime.parse(dateArr.get(i).replace(" ", "T")).toLocalDate();
			if(prevDate.equals(firstDate)) {
				return i;
			}
		}
		return 0;
	}
}
