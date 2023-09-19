import java.util.ArrayList;
import java.util.HashMap;

public class channelData {
	ArrayList<String> originalName = new ArrayList<>();
	ArrayList<String> name = new ArrayList<>();
	ArrayList<String> cName = new ArrayList<>();
	HashMap<String, String> cOName = new HashMap<>();
	HashMap<String, String> oCName = new HashMap<>();

	public ArrayList<String> getOriginalName() {
		return this.originalName;
	}

	public void setOriginalName(ArrayList<String> originalName) {
		this.originalName = originalName;
	}

	public ArrayList<String> getName() {
		return name;
	}

	public void setName(ArrayList<String> name) {
		this.name = name;
	}

	public ArrayList<String> getcName() {
		return cName;
	}

	public void setcName(ArrayList<String> cName) {
		this.cName = cName;
	}

	public String getcOName(String cname) {

		return this.cOName.get(cname);
	}

	public void setcOName(ArrayList<String> cName, ArrayList<String> oName) {
		Integer i = 0;
		for (String a : cName) {
			this.cOName.put(cName.get(i), oName.get(i));
			i++;
		}
	}

	public HashMap<String, String> getoCName() {
		Integer i = 0;
		// ArrayList<String> some = new ArrayList<>();
		for (String a : this.originalName) {
			this.oCName.put(a, cName.get(i));
			i++;
		}
		return this.oCName;
	}
}
