import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Perfs implements Serializable{
	private Map<String,List<String>> perfData;
	private Perfs(Map<String,List<String>> perfData) {
		this.perfData = perfData;
	}
	public static Perfs getPerfsInst() throws Exception {
		String filePath = ImportData.selectFile();
		Map<String,List<String>> perfData = ImportData.readFileData(filePath,String.class,STAGE_NUM,PLUG_DEPTH,TOP_PERF,BOTTOM_PERF);
		return new Perfs(perfData);
	}
	public static Perfs getPerfsInst(String projectDir) throws Exception {
		String filePath = ImportData.selectFile(projectDir);
		Map<String,List<String>> perfData = ImportData.readFileData(filePath,String.class,STAGE_NUM,PLUG_DEPTH,TOP_PERF,BOTTOM_PERF);
		return new Perfs(perfData);
	}
	public String getDepth(Integer stage,DEPTHS depths) {
		int index = perfData.get(STAGE_NUM).indexOf(String.valueOf(stage));
		return perfData.get(depths.getValue()).get(index==-1?0:index);
	}
	public List<String> getDepthList(DEPTHS depths){
		return this.perfData.get(depths.getValue());
	}
	
	public final static String STAGE_NUM = "stage";
	public final static String PLUG_DEPTH = "plug_depth";
	public final static String TOP_PERF = "top_perf";
	public final static String BOTTOM_PERF = "bottom_perf";
	
	public static enum DEPTHS{
		PLUG_DEPTH,TOP_PERF,BOTTOM_PERF;
		public String getValue() {
			switch(this) {
			case PLUG_DEPTH:
				return Perfs.PLUG_DEPTH;
			case TOP_PERF:
				return Perfs.TOP_PERF;
			case BOTTOM_PERF:
				return Perfs.BOTTOM_PERF;
			default:
				return Perfs.PLUG_DEPTH;
			}
		}
	}
}
