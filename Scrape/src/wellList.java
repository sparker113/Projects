import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class wellList {

	String avgPerfTVD;
	String completionDate;
	String completionDateLcl;
	String country;
	String county;
	String formation;
	String lateralLength;
	String operatorCompany;
	String operatorCompanyId;
	String padName;
	String serviceCompany;
	String serviceCompanyId;
	String state;
	ArrayList treatmentList;
	String wellName;
	String wellUid;
	String totalCount;
	String wellId;
	ArrayList id;
	ArrayList timeStart;

	Map<String, String> a1 = new HashMap<>();
	Map<String, String> a2 = new HashMap<>();
	Map<String, String> a3 = new HashMap<>();
	Map<String, String> a4 = new HashMap<>();
	Map<String, String> a5 = new HashMap<>();
	Map<String, String> a6 = new HashMap<>();
	Map<String, String> a7 = new HashMap<>();
	Map<String, String> a8 = new HashMap<>();
	Map<String, String> a9 = new HashMap<>();
	Map<String, String> a11 = new HashMap<>();
	Map<String, String> a12 = new HashMap<>();
	Map<String, String> a13 = new HashMap<>();
	Map<String, String> a14 = new HashMap<>();
	Map<String, ArrayList> a15 = new HashMap<>();
	Map<String, String> a16 = new HashMap<>();
	Map<String, String> a17 = new HashMap<>();
	Map<String, HashMap<String, String>> a18 = new HashMap<>();
	Map<String, HashMap<String, String>> a19 = new HashMap<>();

	public String getAvgPerfTVD(String wellName) {
		return a1.get(wellName);
	}

	public void setAvgPerfTVD(String avgPerfTVD, String wellName) {

		a1.put(wellName, avgPerfTVD);
	}

	public String getCompletionDate(String wellName) {
		return a2.get(wellName);
	}

	public void setCompletionDate(String completionDate, String wellName) {
		a2.put(wellName, completionDate);
	}

	public String getCompletionDateLcl(String wellName) {
		return a3.get(wellName);
	}

	public void setCompletionDateLcl(String completionDateLcl, String wellName) {
		a3.put(wellName, completionDateLcl);
	}

	public String getCountry(String wellName) {
		return a4.get(wellName);
	}

	public void setCountry(String country, String wellName) {
		a4.put(wellName, country);
	}

	public String getCounty(String wellName) {
		return a5.get(wellName);
	}

	public void setCounty(String county, String wellName) {
		a5.put(wellName, county);
	}

	public String getFormation(String wellName) {
		return a6.get(wellName);
	}

	public void setFormation(String formation, String wellName) {
		a6.put(wellName, formation);
	}

	public String getLateralLength(String wellName) {
		return a7.get(wellName);
	}

	public void setLateralLength(String lateralLength, String wellName) {
		a7.put(wellName, lateralLength);
	}

	public String getOperatorCompany(String wellName) {
		return a8.get(wellName);
	}

	public void setOperatorCompany(String operatorCompany, String wellName) {
		a8.put(wellName, operatorCompany);
	}

	public String getOperatorCompanyId(String wellName) {
		return a9.get(wellName);
	}

	public void setOperatorCompanyId(String operatorCompanyId, String wellName) {
		a9.put(wellName, operatorCompanyId);
	}

	public String getPadName(String wellName) {
		return a11.get(wellName);
	}

	public void setPadName(String padName, String wellName) {
		a11.put(wellName, padName);
	}

	public String getServiceCompany(String wellName) {
		return a12.get(wellName);
	}

	public void setServiceCompany(String serviceCompany, String wellName) {
		a12.put(wellName, serviceCompany);
	}

	public String getServiceCompanyId(String wellName) {
		return a13.get(wellName);
	}

	public void setServiceCompanyId(String serviceCompanyId, String wellName) {
		a13.put(wellName, serviceCompanyId);
	}

	public String getState(String wellName) {
		return a14.get(wellName);
	}

	public void setState(String state, String wellName) {
		a14.put(wellName, state);
	}

	public ArrayList<String> getTreatmentList(String wellName) {
		return a15.get(wellName);
	}

	public void setTreatmentList(ArrayList<String> treatmentList, String wellName) {
		if (a15.keySet().contains(wellName)) {
			a15.remove(wellName);
		}
		a15.put(wellName, treatmentList);
	}

	public String getWellUid(String wellName) {
		return a16.get(wellName);
	}

	public void setWellUid(String wellUid, String wellName) {
		a16.put(wellName, wellUid);
	}

	public String getWellId(String wellName) {
		return a17.get(wellName);
	}

	public void setWellId(String wellId, String wellName) {
		a17.put(wellName, wellId);
	}

	public HashMap<String, String> getid(String wellName) {
		return a18.get(wellName);
	}

	public void setid(HashMap<String, String> id, String wellName) {
		if (a18.keySet().contains(wellName)) {
			a18.remove(wellName);
		}
		a18.put(wellName, id);
	}

	public HashMap<String, String> getTimeStart(String wellName) {
		return a19.get(wellName);
	}

	public void setTimeStart(HashMap<String, String> timeStart, String wellName) {
		a19.put(wellName, timeStart);
	}

}
