import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.JTable;

public class SheetData implements Runnable {
	private JTable table;
	private ArrayList<String> dataColumn = new ArrayList<>();
	private Integer column;
	private Integer lastRow;
	private Boolean setData;
	Thread t;

	public SheetData(Integer lastRow, Integer column, JTable table) {
		this.table = table;
		this.column = column;
		this.lastRow = lastRow;
		this.setData = false;
		this.t = new Thread(this, "Sheet Data - Column " + column);
		t.start();
	}

	public SheetData() {

	}

	public static ArrayList<String> getTableArrayByMatch(JTable table, int columnToAdd, int columnToMatch,
			String match) {
		int lastRow = findLastRowOfData(table, columnToMatch, 10).intValue();
		ArrayList<String> array = new ArrayList<>();
		for (int i = 0; i <= lastRow; i++) {
			String matchedValue = matchedValue(table, columnToAdd, columnToMatch, i, match);
			if (matchedValue != null) {
				array.add(matchedValue);
			}
		}
		System.out.println("array = " + lastRow);
		return array;
	}

	public static Double sumStringArray(ArrayList<String> array) {
		double sum = 0.0;
		for (String s : array) {
			sum += Double.valueOf(s==null||s.equals("")?"0.0":s);
		}
		return sum;
	}

	public static String matchedValue(JTable table, int columnToAdd, int columnToMatch, int row, String match) {
		String cellString = table.getValueAt(row, columnToMatch) != null
				? table.getValueAt(row, columnToMatch).toString()
				: null;
		return cellString != null && cellString.toUpperCase().equals(match.toUpperCase())
				? (table.getValueAt(row, columnToAdd) != null ? table.getValueAt(row, columnToAdd).toString() : "")
				: null;
	}

	public static Double getColumnSum(JTable table, int column) {

		int lastRow = findLastRowOfData(table, column, 10).intValue();
		if (!checkValidNumeric(table, column, 0, lastRow)) {
			return 0.0;
		}

		return sumColumnRange(table, column, 0, lastRow);
	}

	public static Double getColumnSum(JTable table, String columnHeader) {
		int column = checkTableForHeader(table, columnHeader);
		if (column == -1) {
			return 0.0;
		}
		return getColumnSum(table, column);
	}

	private static Double sumColumnRange(JTable table, int column, int r1, int r2) {
		double sum = 0.0;
		for (int i = r1; i <= r2; i++) {
			sum += Double.valueOf(table.getValueAt(i, column).toString());
		}
		return sum;
	}

	public static Boolean checkValidNumeric(JTable table, int column, int r1, int r2) {
		for (int i = r1; i <= r2; i++) {
			String cellString = table.getValueAt(i, column) != null ? table.getValueAt(i, column).toString() : "";
			Matcher matcher = Pattern.compile("([^\\d\\.]+)").matcher(cellString);
			if (matcher.find()) {
				return false;
			}
		}
		return true;
	}

	public static int checkTableForHeader(JTable table, String columnHeader) {
		if (table.getColumnCount() > 0 && table.getColumnModel().getColumn(0).getHeaderValue() == null) {
			return -1;
		}
		ArrayList<String> array = getTableHeaders(table);
		return array.indexOf(columnHeader);
	}

	private static ArrayList<String> getTableHeaders(JTable table) {
		ArrayList<String> headers = new ArrayList<>();
		for (int i = 0; i < table.getColumnCount(); i++) {
			headers.add((String) table.getColumnModel().getColumn(i).getHeaderValue());
		}
		return headers;
	}

	public static Integer findLastRowOfData(JTable table, int column) {
		int i = 0;
		while (table.getValueAt(i, column) != null && !table.getValueAt(i, column).toString().equals("")
				&& i < table.getRowCount()) {
			i++;
		}
		return i;
	}

	public static Integer findLastRowOfData(JTable table, int column, int maxRowSpacing) {
		int i = checkNumRowsForData(table, 0, column, maxRowSpacing);

		while (table.getValueAt(i, column) != null && !table.getValueAt(i, column).toString().equals("")
				&& i < table.getRowCount()) {
			i = checkNumRowsForData(table, i + 1, column, maxRowSpacing);
		}
		System.out.println("Last Row of Data Found in JTable = " + i);
		return i;
	}

	public static int checkNumRowsForData(JTable table, int rowStart, int column, int numRows) {
		for (int i = rowStart; i < rowStart + numRows; i++) {
			if (table.getValueAt(i, column) != null && !table.getValueAt(i, column).toString().equals("")) {
				return i;
			}
		}
		return rowStart += 1;
	}

	public static HashMap<String, LinkedHashMap<String, String>> getMapOfTables(List<String> keys, JTable... tables) {
		HashMap<String, LinkedHashMap<String, String>> tablesMap = new HashMap<>();
		int i = 0;
		for (String s : keys) {
			tablesMap.put(s, getSigTableData(tables[i]));
			i++;
		}
		return tablesMap;
	}

	@Override
	public synchronized void run() {
		int i;
		for (i = 0; i <= lastRow; i++) {
			dataColumn.add(String.valueOf(table.getValueAt(i, column)));
		}
		setData = true;
		notify();
	}

	public static HashMap<Integer, ArrayList<String>> getMainTableDataIntKeys(JTable mTable) {
		HashMap<Integer, ArrayList<String>> mainTableMap = new HashMap<>();
		String[] columnNames = getColumnNames();
		for (int i = 0; i < mTable.getColumnCount(); i++) {
			mainTableMap.put(i, new ArrayList<>());
			for (int ii = 0; ii <= getLastDataRow(mTable); ii++) {
				mainTableMap.get(i).add(String.valueOf(mTable.getValueAt(ii, i)));
			}
		}
		return mainTableMap;
	}

	public static HashMap<String, ArrayList<String>> getMainTableData(JTable mTable) {
		HashMap<String, ArrayList<String>> mainTableMap = new HashMap<>();
		String[] columnNames = getColumnNames();
		for (int i = 0; i < mTable.getColumnCount(); i++) {
			mainTableMap.put(columnNames[i], new ArrayList<>());
			for (int ii = 0; ii <= getLastDataRow(mTable); ii++) {
				mainTableMap.get(columnNames[i]).add(String.valueOf(mTable.getValueAt(ii, i)));
			}
		}
		return mainTableMap;
	}

	public static String[] getColumnNames() {
		String[] columnNames = { "Start Time", "End Time", "Stage Number", "Start Date", "End Date", "Prop Con",
				"Clean Total", "Average Pressure", "Average Rate", "Slurry Total", "Sand Type", "Sand Volume",
				"Substage Name" };

		return columnNames;
	}

	public static int getLastDataRow(JTable table) {
		int lRow = -1;
		int i;

		for (i = 0; i < table.getRowCount(); i++) {
			if (table.getValueAt(i, 0).equals(String.valueOf(""))) {
				lRow = i - 1;
				break;
			}
		}
		if (lRow == -1) {
			JOptionPane.showInputDialog(null,
					"There's no data to transfer; run the program" + " before trying to transfer data");
		}

		return lRow;
	}

	public synchronized ArrayList<String> getDataColumn() {
		while (!setData) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println("InterruptedException SheetData");
			}
		}

		return this.dataColumn;
	}

	public static LinkedHashMap<String, String> getSigTableData(JTable... table) {
		int i;
		LinkedHashMap<String, String> sigMap = new LinkedHashMap<>();
		for (JTable tab : table) {
			if (tab.getRowCount() == 1) {
				sigMap.put("Clean Total", String.valueOf(tab.getValueAt(0, 0)));
				sigMap.put("Slurry Total", String.valueOf(tab.getValueAt(0, 1)));
			} else {
				for (i = 0; i < tab.getRowCount(); i++) {

					if (tab.getValueAt(i, 0) == null || tab.getValueAt(i, 0).equals(String.valueOf(""))) {
						break;
					}
					if (tab.getValueAt(i, 1) != null && !tab.getValueAt(i, 1).equals(String.valueOf(""))) {
						sigMap.put(String.valueOf(tab.getValueAt(i, 0)), String.valueOf(tab.getValueAt(i, 1)));
					} else {
						sigMap.put(String.valueOf(tab.getValueAt(i, 0)), "0");
					}
				}
			}
		}
		return sigMap;
	}

	private static String getCellValue(JTable table, int row, int column) {
		if (table.getValueAt(row, column) == null || table.getValueAt(row, column).toString().equals("")) {
			return "0";
		}
		return table.getValueAt(row, column).toString();
	}

	public static LinkedHashMap<String, LinkedHashMap<String, String>> getTableData(JTable table) {
		LinkedHashMap<String, LinkedHashMap<String, String>> map = new LinkedHashMap<>();
		for (int row = 0; row < table.getRowCount(); row++) {
			if (getCellValue(table, row, 0).equals("0")) {
				break;
			}
			String value = getCellValue(table, row, 0);
			map.put(value, new LinkedHashMap<String, String>());
			for (int column = 0; column < table.getColumnCount(); column++) {
				map.get(value).put(table.getColumnModel().getColumn(column).getHeaderValue().toString(),
						getCellValue(table, row, column));
			}
		}
		return map;
	}

	//////// REVISIT THIS AND MAKE IT A RECURSIVE METHOD
	public static LinkedHashMap<String, HashMap<String, String>> getSigTableData(JTable table, Integer startColumn) {
		int i;
		int ii;
		LinkedHashMap<String, HashMap<String, String>> sigMap = new LinkedHashMap<>();

		for (i = 0; i < table.getRowCount(); i++) {
			if (table.getValueAt(i, 0) == null || table.getValueAt(i, 0).equals(String.valueOf(""))) {
				break;
			}
			HashMap<String, String> tempMap = new HashMap<>();
			for (ii = startColumn; ii < table.getColumnCount(); ii++) {
				String colHeader = String.valueOf(table.getColumnModel().getColumn(ii).getHeaderValue());

				if (table.getValueAt(i, ii) != null && !String.valueOf(table.getValueAt(i, ii)).equals("")) {
					tempMap.put(colHeader, String.valueOf(table.getValueAt(i, ii)));
				} else {
					tempMap.put(colHeader, "0");
				}
			}
			sigMap.put(String.valueOf(table.getValueAt(i, 0)), tempMap);

		}

		return sigMap;
	}

	public static String getEndDate(JTable table) {
		Integer lDataRow = getLastDataRow(table);
		String stageEndDate = String.valueOf(table.getValueAt(lDataRow, 4));
		return stageEndDate;
	}

	public static String getEndTime(JTable table) {
		Integer lDataRow = getLastDataRow(table);
		String stageEndTime = String.valueOf(table.getValueAt(lDataRow, 1));
		return stageEndTime;
	}

}
