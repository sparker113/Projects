package frame;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class InfoObject implements Serializable {
	/**
	 *
	 */
	// v.1 serioalVersionUID = 2985781571039457516L;
	private static final long serialVersionUID = 2985781571039457516L;
	private ArrayList<String> infillWellNames;
	private ArrayList<String> offsetWellNames;
	private Map<String, Map<String, ArrayList<String>>> surveyDataMap;
	private Map<String, Map<String, ArrayList<String>>> fdiDataMap;

	public InfoObject(ArrayList<String> infillWellNames, ArrayList<String> offsetWellNames,
			Map<String, Map<String, ArrayList<String>>> surveyDataMap, Map<String,Map<String,ArrayList<String>>> fdiDataMap) {

	}
}
