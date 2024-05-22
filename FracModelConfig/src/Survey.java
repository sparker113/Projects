import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import javax.swing.JOptionPane;

public class Survey implements Serializable {
	private Map<String,List<String>> surveyData;
	private Double latitude;
	private Double longitude;
	private Double avgAzimuth;
	private Double landMeasuredDepth;
	private Double xCoord;
	private Double yCoord;
	private int landingIndex;
	private Survey(Map<String,List<String>> surveyData,Double latitude,Double longitude,Double xCoord,Double yCoord) {
		
		this.surveyData = surveyData;
		this.latitude = latitude;
		this.longitude = longitude;
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.landingIndex = setLandingDepths(surveyData.get(INCLINATION),surveyData.get(MEASURED_DEPTH),surveyData.get(TRUE_VERTICAL_DEPTH));
		setAvgAzimuth(surveyData.get(AZIMUTH));
	}
	private final static String SELECT_DATA_TITLE = "Match Survey Data with Headers";
	public static Survey getSurveyInst() throws Exception {
		String filePath = ImportData.selectFile();
		Map<String,List<String>> surveyData = ImportData.readFileData(filePath,String.class,MEASURED_DEPTH,AZIMUTH,INCLINATION,TRUE_VERTICAL_DEPTH,NORTH_SOUTH,EAST_WEST);
		Map<String,Double> latLong = getLatLong(inputLatLong());
		surveyData = fillMissingData(surveyData);
		return new Survey(surveyData,latLong.get(LATITUDE),latLong.get(LONGITUDE),calcXCoord(latLong.get(LONGITUDE),latLong.get(LATITUDE)),calcYCoord(latLong.get(LATITUDE)));
	}
	public static Survey getSurveyInst(String projectDir) throws Exception {
		String filePath = ImportData.selectFile(projectDir);
		Map<String,List<String>> surveyData = ImportData.readFileData(filePath,String.class,MEASURED_DEPTH,AZIMUTH,INCLINATION,TRUE_VERTICAL_DEPTH,NORTH_SOUTH,EAST_WEST);
		Map<String,Double> latLong = getLatLong(inputLatLong());
		surveyData.putAll(fillMissingData(surveyData));
		return new Survey(surveyData,latLong.get(LATITUDE),latLong.get(LONGITUDE),calcXCoord(latLong.get(LONGITUDE),latLong.get(LATITUDE)),calcYCoord(latLong.get(LATITUDE)));
	}
	public static Survey getSurveyInst(String projectDir,String frameTitles) throws Exception {
		String filePath = ImportData.selectFile(projectDir,frameTitles);
		Map<String,List<String>> surveyData = ImportData.readFileData(filePath,String.class,MEASURED_DEPTH,AZIMUTH,INCLINATION,TRUE_VERTICAL_DEPTH,NORTH_SOUTH,EAST_WEST);
		Map<String,Double> latLong = getLatLong(inputLatLong());
		surveyData.putAll(fillMissingData(surveyData));
		return new Survey(surveyData,latLong.get(LATITUDE),latLong.get(LONGITUDE),calcXCoord(latLong.get(LONGITUDE),latLong.get(LATITUDE)),calcYCoord(latLong.get(LATITUDE)));
	}
	public void updateCoordinates(Double latitude,Double longitude) {
		setLatitude(latitude);
		setLongitude(longitude);
		calcXCoord(longitude,latitude);
		calcYCoord(latitude);
	}
	private void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	private void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	public Double getXCoord() {
		return this.xCoord;
	}
	public Double getYCoord() {
		return this.yCoord;
	}
	public String getValueAtMD(Double mD,String dataName) {
		for(int i = landingIndex;i<surveyData.get(MEASURED_DEPTH).size();i++) {
			if(Double.valueOf(surveyData.get(MEASURED_DEPTH).get(i))>=mD) {
				return surveyData.get(dataName).get(i);
			}
		}
		return "0";
	}
	public String findXAtY(Double yCoord) {
		for(int i = landingIndex;i<surveyData.get(NORTH_SOUTH).size();i++) {
			Double yAtPoint = this.yCoord+Double.valueOf(surveyData.get(NORTH_SOUTH).get(i));
			if(yAtPoint>=yCoord) {
				return String.valueOf(Double.valueOf(surveyData.get(EAST_WEST).get(i))+xCoord);
			}
		}
		return "0";
	}
	public String getValueAtY(Double yCoord,String dataName) {
		for(int i = landingIndex;i<surveyData.get(NORTH_SOUTH).size();i++) {
			Double yAtPoint = this.yCoord+Double.valueOf(surveyData.get(NORTH_SOUTH).get(i));
			if(yAtPoint>=yCoord) {
				if(!surveyData.containsKey(dataName)) {
					System.out.println(dataName+": not a value within the survey data");
					return "0";
				}
				return surveyData.get(dataName).get(i);
			}
		}
		return "0";
	}
	public Double getXCoordAtLand() {
		Double eWAtLand = Double.valueOf(surveyData.get(EAST_WEST).get(landingIndex));
		Double xAtLand = xCoord+eWAtLand;
		return xAtLand;
	}
	public Double getYCoordAtLand() {
		Double nSAtLand = Double.valueOf(surveyData.get(NORTH_SOUTH).get(landingIndex));
		Double yAtLand = yCoord+nSAtLand;
		return yAtLand;
	}
	private static Double calcXCoord(Double longitude,Double latitude ) {
		Double relLong = longitude - LONG_DATUM;
		Double xCoord = getRadius(latitude)*getRadians(relLong);
		return xCoord;
	}
	public static Double calcYCoord(Double latitude) {
		Double relLat = latitude - LAT_DATUM;
		Double yCoord = getRadians(relLat)*Math.sqrt((Math.pow(EQ_EARTH_RADIUS,2.0)+Math.pow(PO_EARTH_RADIUS, 2.0))/2.0);
		return yCoord;
	}
	private final static Double LONG_DATUM = -130d;
	private final static Double LAT_DATUM = 25d;
	private static Map<String,Double> getLatLong(String latLongString){
		String[] latLong = latLongString.split(",");
		Map<String,Double> latLongMap = new HashMap<>();
		latLongMap.put(LATITUDE, Double.valueOf(latLong[0]));
		latLongMap.put(LONGITUDE, Double.valueOf(latLong[1]));
		return latLongMap;
	}
	private void setAvgAzimuth(List<String> azimuth) {
		this.avgAzimuth = FracCalculations.avgWithinBounds(azimuth, landingIndex);
		System.out.println("Average Azimuth: "+avgAzimuth);
	}
	private final static Double LANDING_AZIMUTH = 85d;
	private int setLandingDepths(List<String> inclination,List<String> measuredDepth,List<String> tvd) {
		for(int i = 0;i<inclination.size();i++) {
			Double value = Double.valueOf(inclination.get(i));
			if(value>LANDING_AZIMUTH) {
				setLandingMeasuredDepth(Double.valueOf(measuredDepth.get(i)));
				setLandingTVD(Double.valueOf(tvd.get(i)));
				return i;
			}
		}
		setLandingMeasuredDepth(0d);
		setLandingTVD(0d);
		System.out.println("Didn't find landing");
		return 0;
	}
	public Double getLandingMeasuredDepth() {
		return this.landMeasuredDepth;
	}
	public Double getLandingTVD() {
		return this.landTVD;
	}
	public Double getAvgAzimuth() {
		return this.avgAzimuth;
	}
	public Map<String,List<String>> getSurveyData(){
		return this.surveyData;
	}
	private void setLandingMeasuredDepth(Double landMeasuredDepth) {
		this.landMeasuredDepth = landMeasuredDepth;
	}
	private Double landTVD;
	private void setLandingTVD(Double landTVD) {
		this.landTVD = landTVD;
	}
	private static Map<String,List<String>> fillMissingData(Map<String,List<String>> surveyData) throws InterruptedException{
		ExecutorService executor = Executors.newCachedThreadPool();
		Semaphore semaphore = new Semaphore(0);
		int numEntries = surveyData.size();
		for(Map.Entry<String, List<String>> entry:surveyData.entrySet()) {
			executor.execute(()->{
				if(entry.getValue().isEmpty()) {
					System.out.println("Entry is Empty");
					List<String> list = getMissingData(surveyData,entry.getKey());
					System.out.println(entry.getKey()+" : "+list);
					addToMap(surveyData,entry.getKey(),list);
				}
				semaphore.release();
			});
		}
		semaphore.acquire(numEntries);
		executor.shutdownNow();
		return surveyData;
		
	}
	private static List<String> getMissingData(Map<String,List<String>> surveyData,String missing){
		switch(missing) {
		case(NORTH_SOUTH):
			return calcNorthSouth(surveyData.get(MEASURED_DEPTH),surveyData.get(AZIMUTH),surveyData.get(INCLINATION));
		case(EAST_WEST):
			return calcEastWest(surveyData.get(MEASURED_DEPTH),surveyData.get(AZIMUTH),surveyData.get(INCLINATION));
		case(TRUE_VERTICAL_DEPTH):
			return calcTVD(surveyData.get(MEASURED_DEPTH),surveyData.get(INCLINATION));
		}
		return calcTVD(surveyData.get(MEASURED_DEPTH),surveyData.get(INCLINATION));
	}
	private static synchronized void addToMap(Map<String,List<String>> surveyData,String key,List<String> data) {
		surveyData.put(key, data);
	}
	private static List<String> calcTVD(List<String> md,List<String> inclination){
		List<String> calcTVD = new ArrayList<>();
		if((md==null||md.isEmpty())|(inclination==null||inclination.isEmpty())) {
			return calcTVD;
		}
		for(int i = 1;i<getMinSize(md,inclination);i++) {
			String curTVD = calcTVD.isEmpty()?"0.0":calcTVD.get(calcTVD.size()-1);
			calcTVD.add(calcTVDForStep(md.get(i),md.get(i-1),inclination.get(i),curTVD));
		}
		return calcTVD;
	}
	private static String calcTVDForStep(String md2,String md1,String inc,String curTVD) {
		Double depth = Double.valueOf(md2)-Double.valueOf(md1);
		Double incline = Double.valueOf(inc);
		Double stepTVD = Math.cos(getRadians(incline))*depth;
		Double tvd = stepTVD + Double.valueOf(curTVD);
		return String.valueOf(tvd);
	}
	private static List<String> calcNorthSouth(List<String> md,List<String> azimuth,List<String> inclination){
		List<String> northSouth = new ArrayList<>();
		if(md.isEmpty()|azimuth.isEmpty()|inclination.isEmpty()) {
			return northSouth;
		}
		for(int i = 1;i<getMinSize(md,azimuth,inclination);i++) {
			String curNS = northSouth.isEmpty()?"0.0":northSouth.get(northSouth.size()-1);
			northSouth.add(calcNorthSouthStep(md.get(i),md.get(i-1),azimuth.get(i),inclination.get(i),curNS));
		}
		return northSouth;
	}
	private static String calcNorthSouthStep(String md2,String md1,String azimuth,String inc,String curNS) {
		Double netOut = calcNetLengthOut(md2,md1,inc);
		Double netNS = netOut*Math.cos(getRadians(Double.valueOf(azimuth)));
		Double totalNS = Double.valueOf(curNS)+netNS;
		return String.valueOf(totalNS);
	}
	private static List<String> calcEastWest(List<String> md,List<String> azimuth,List<String> inclination){
		List<String> eastWest = new ArrayList<>();
		if(md.isEmpty()|azimuth.isEmpty()|inclination.isEmpty()) {
			return eastWest;
		}
		for(int i = 1;i<getMinSize(md,azimuth,inclination);i++) {
			String curEW = eastWest.isEmpty()?"0.0":eastWest.get(eastWest.size()-1);
			eastWest.add(calcEastWestStep(md.get(i),md.get(i-1),azimuth.get(i),inclination.get(i),curEW));
		}
		return eastWest;
	}
	private static String calcEastWestStep(String md2,String md1,String azimuth,String inc,String curEW) {
		double netOut = calcNetLengthOut(md2,md1,inc);
		Double netEW = netOut*Math.sin(getRadians(Double.valueOf(azimuth)));
		Double totalEW = Double.valueOf(curEW)+netEW;
		return String.valueOf(totalEW);
	}
	private static Double calcNetLengthOut(String md2,String md1,String inc) {
		Double netMD = Double.valueOf(md2)-Double.valueOf(md1);
		Double lOut = Math.sin(getRadians(Double.valueOf(inc)))*netMD;
		return lOut;
	}
	public Double getLatitude() {
		return this.latitude;
	}
	public Double getLongitude() {
		return this.longitude;
	}
	public final static Double DEFAULT_LATITUDE = 31.0d;
	public final static Double DEFAULT_LONGITUDE = -103.0d;
	private final static String DEFAULT_LAT_LON = "31.0,-103.0";
	private static String inputLatLong() {
		String latLong = JOptionPane.showInputDialog("Input the latitude,longitude of the wellhead");
		int count = 0;
		while(!latLong.contains(",")&count<3) {
			latLong = JOptionPane.showInputDialog("Separate latitude and longitude with a comma (latitude,longitude");
			count++;
		}
		if(!latLong.contains(",")) {
			return DEFAULT_LAT_LON;
		}
		return latLong;
	}
	public static Double getRadians(Double degrees) {
		//System.out.println("Degrees: "+degrees);
		Double radians = (degrees/(180d))*Math.PI; 
		//System.out.println("Radians: "+radians);
		return radians;
	}
	public static int getMinSize(List<?>...lists) {
		int min = lists[0].size();
		for(List<?> l:lists) {
			min=l.size()<min?l.size():min;
		}

		return min;
	}
	public static double getRadius(double latitude) {
		double rad = getRadians(latitude);
		double r = (EQ_EARTH_RADIUS*Math.pow(Math.cos(rad),2.0))+(PO_EARTH_RADIUS*Math.pow(Math.sin(rad),2.0));
		double r2 = r*Math.sin((Math.PI/2)-rad);
		return r2;
	}
	public final static Double EARTH_RADIUS = 20924640d;
	public final static Double EQ_EARTH_RADIUS = 20924640d;
	public final static Double PO_EARTH_RADIUS = 20856000d;
	public final static String NORTH_SOUTH = "north/south";
	public final static String EAST_WEST = "east/west";
	
	public final static String LATITUDE = "latitude";
	public final static String LONGITUDE = "longitude";
	

	public final static String MEASURED_DEPTH = "measuredDepth";
	public final static String TRUE_VERTICAL_DEPTH = "tvd";
	
	public final static String AZIMUTH = "azimuth";
	public final static String INCLINATION = "inclination";
}
