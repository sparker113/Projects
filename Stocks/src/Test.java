import java.time.LocalDate;
import java.util.Map;

import data.Nasdaq;

public class Test {
	public static void main(String[] args) throws Exception{
		Nasdaq nasdaq = Nasdaq.getAllStockData();
		nasdaq.getData();
		nasdaq.setHistoricData(30);
		Map<String,Map<LocalDate,Map<String,String>>> map = nasdaq.getHistoricMap();
		System.out.println(map.size());
		System.out.println(nasdaq.getData().size());
	}
	
	
}
