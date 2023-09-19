import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class CellComboBox extends JComboBox<String> {
	JTable table;
	ArrayList<String> values;
	int column;

	CellComboBox(JTable table, ArrayList<String> values) {
		this.table = table;
		this.values = values;
		executeAll();
	}

	CellComboBox(JTable table, int column, ArrayList<String> values) {
		this.table = table;
		this.values = values;
		this.column = column;
		executeColumn();
	}

	CellComboBox(JTable table, int column, TreeMap<String, String> keyValues) {
		this.table = table;
		this.column = column;
		executeColumn(keyValues);
	}

	private void addItemsFromArray() {
		values.forEach(this::addItem);
	}

	private void setAllCells() {
		for (int col = 0; col < table.getColumnCount(); col++) {
			table.getColumnModel().getColumn(col).setCellEditor(new DefaultCellEditor(this));
		}
	}

	public void resetKeyValues(TreeMap<String, String> channelMnemonics) {
		addItemsFromMap(channelMnemonics);
	}

	private void setColumnCells() {
		table.getColumnModel().getColumn(column).setCellEditor(new DefaultCellEditor(this));
	}

	private void addItemsFromMap(Map<String, String> keyValues) {
		for (String s : keyValues.keySet()) {
			this.addItem(s);
		}
	}

	private void executeColumn(TreeMap<String, String> keyValues) {
		addItemsFromMap(keyValues);
		setColumnCells();
	}

	private void executeColumn() {
		addItemsFromArray();
		setColumnCells();
	}

	private void executeAll() {
		addItemsFromArray();
		setAllCells();
	}

	public void addPopupMenuListenerTask(Runnable task) {
		this.addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				Executors.newSingleThreadExecutor().execute(task);
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				// TODO Auto-generated method stub

			}

		});
	}
}
