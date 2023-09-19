import java.util.ArrayList;

public class PumpedAcid {
	private ArrayList<String> slurryRate;
	private ArrayList<String> treatingPressure;
	private ArrayList<String> propConc;
	private String operator;

	public PumpedAcid(ArrayList<String> slurryRate, ArrayList<String> treatingPressure, ArrayList<String> propConc,
			String operator) {
		this.slurryRate = slurryRate;
		this.treatingPressure = treatingPressure;
		this.propConc = propConc;
		this.operator = operator;
	}

}
