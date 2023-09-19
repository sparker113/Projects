import javax.swing.JTable;

public class ClearTable {
	int row;
	int col;
	JTable table;

	ClearTable(int row, JTable table) {
		this.row = row;
		this.table = table;
		clearRow();
	}

	ClearTable(JTable table) {
		this.table = table;
		clearTable();
	}

	ClearTable(Integer col, JTable table) {
		this.col = col;
		this.table = table;
		clearColumn(col, table);
	}

	public static void clearColumn(int col, JTable table) {
		int numRows = table.getRowCount();
		int i;
		for (i = 0; i < numRows; i++) {
			table.setValueAt("", i, col);
		}
	}

	private void clearTable() {
		int numRows = table.getRowCount();
		int numCols = table.getColumnCount();
		int i;
		int ii;
		for (i = 0; i < numRows; i++) {
			for (ii = 0; ii < numCols; ii++) {
				table.setValueAt("", i, ii);
			}
		}
	}

	public static void clearTables(JTable... tables) {
		for (JTable table : tables) {
			clearTable(table);
		}
	}

	public static void clearTable(JTable table) {
		int numRows = table.getRowCount();
		int numCols = table.getColumnCount();
		int i;
		int ii;
		for (i = 0; i < numRows; i++) {
			for (ii = 0; ii < numCols; ii++) {
				table.setValueAt("", i, ii);
			}
		}
	}

	private void clearRow() {
		int numCols = table.getColumnCount();
		int i;
		for (i = 0; i < numCols; i++) {
			table.setValueAt("", row, i);
		}
	}
}
