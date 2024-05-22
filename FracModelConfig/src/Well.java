import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Well implements Serializable {
	private Survey survey;
	private Perfs perfs;
	private String name;
	public Well(String name,Survey survey, Perfs perfs) {
		this.name = name;
		this.survey = survey;
		this.perfs = perfs;
	}
	public Well(String name,Survey survey) {
		this.name = name;
		this.survey = survey;
	}
	public String getName() {
		return this.name;
	}
	public Map<String, Double> findDistFromWell(Well well2) {
		Survey survey2 = well2.getWellSurvey();
		Map<String, Double> xYMap = new HashMap<>();
		xYMap.put(X_DIR, (survey.getXCoord() - survey2.getXCoord()));
		xYMap.put(Y_DIR, (survey.getYCoord() - survey2.getYCoord()));
		return xYMap;
	}
	public void updateCoordinates(Double lat,Double lon) {
		survey.updateCoordinates(lat,lon);
	}
	public Map<String,Double> findPerpDistFromWells(int stage,Well...wells){
		Map<String,Double> distMap = new HashMap<>();
		for(Well well2:wells) {
			distMap.put(well2.getName(), findPerpDistFromWell(well2,stage));
		}
		return distMap;
	}
	public Map<String,Double> findVerticalDistFromWells(int stage,Well...wells){
		if(perfs==null) {
			System.out.println("Can't find distance from Offset Well");
			return null;
		}
		Map<String,Double> distMap = new HashMap<>();
		for(Well well2:wells) {
			distMap.put(well2.getName(), findVerticalDistFromWell(well2,stage));
		}
		return distMap;
	}
	public Map<String,Double> findPerpDistFromWells(int stage,Collection<Well> wells){
		Map<String,Double> distMap = new HashMap<>();
		for(Well well2:wells) {
			distMap.put(well2.getName(), findPerpDistFromWell(well2,stage));
		}
		return distMap;
	}
	public Map<String,Double> findVerticalDistFromWells(int stage,Collection<Well> wells){
		Map<String,Double> distMap = new HashMap<>();
		for(Well well2:wells) {
			distMap.put(well2.getName(), findVerticalDistFromWell(well2,stage));
		}
		return distMap;
	}
	public Double findPerpDistFromWell(Well well2, int stage) {
		/*
		 * Double thisLandX = survey.getXCoordAtLand(); Double landX2 =
		 * well2.getWellSurvey().getXCoordAtLand();
		 * 
		 * if(thisLandX>landX2) { return well2.findPerpDistFromWell(this,stage); }
		 */
		Double yCoordAtStage = findYCoordAtStage(stage);

		Double cardXDist = findXCoordAtStage(stage) - Double.valueOf(well2.getWellSurvey().findXAtY(yCoordAtStage));
		return Math.abs((cardXDist * Math.cos(survey.getAvgAzimuth())));
	}

	public Double findVerticalDistFromWell(Well well2, int stage) {
		Double tvdAtStage = Double.valueOf(survey.getValueAtMD(
				Double.valueOf(perfs.getDepth(stage, Perfs.DEPTHS.BOTTOM_PERF)), Survey.TRUE_VERTICAL_DEPTH));
		Double yCoordAtStage = findYCoordAtStage(stage);
		Double tvdAtStage2 = Double.valueOf(well2.getWellSurvey().getValueAtY(yCoordAtStage,Survey.TRUE_VERTICAL_DEPTH));
		return tvdAtStage - tvdAtStage2;
	}

	public Double findVerticalDistFromWell(Well well2) {
		Double tvd = Double.valueOf(survey.getLandingTVD());
		Double tvd2 = Double.valueOf(well2.getWellSurvey().getLandingTVD());
		return tvd-tvd2;
	}

	public Double findXCoordAtStage(int stage) {
		Double mDAtStage = Double.valueOf(perfs.getDepth(stage, Perfs.DEPTHS.PLUG_DEPTH));
		Double stgEW = Double.valueOf(survey.getValueAtMD(mDAtStage, Survey.EAST_WEST));
		return survey.getXCoord() + stgEW;
	}

	public Double findYCoordAtStage(int stage) {
		Double mDAtStage = Double.valueOf(perfs.getDepth(stage, Perfs.DEPTHS.PLUG_DEPTH));
		Double stgNS = Double.valueOf(survey.getValueAtMD(mDAtStage, Survey.NORTH_SOUTH));
		return survey.getYCoord() + stgNS;
	}

	public Survey getWellSurvey() {
		return this.survey;
	}

	public Perfs getWellPerfs() {
		return this.perfs;
	}

	// positive to the east
	public final static String X_DIR = "x";
	// positive to the north
	public final static String Y_DIR = "y";

}
