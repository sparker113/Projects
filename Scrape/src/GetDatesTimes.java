import javax.swing.JTable;

public class GetDatesTimes {

	public static String getStartDate(JTable table) {
		String startDate = String.valueOf(table.getValueAt(0, 3));
		return startDate;
	}

	public static String getStartTime(JTable table) {
		String startTime = String.valueOf(table.getValueAt(0, 0));
		return startTime;
	}

	public static String getEndDate(JTable table) {
		int i = getLastRow(table);
		String endDate = String.valueOf(table.getValueAt(i, 4));
		return endDate;
	}

	public static String getEndTime(JTable table) {
		int i = getLastRow(table);
		String endTime = String.valueOf(table.getValueAt(i, 1));
		return endTime;
	}

	private static int getLastRow(JTable table) {
		int i = 0;
		while (String.valueOf(table.getValueAt(i, 0)) != "null" && String.valueOf(table.getValueAt(i, 0)) != "") {
			// System.out.println(String.valueOf(table.getValueAt(i, 0)));
			i++;
		}

		return i - 1;
	}
}
