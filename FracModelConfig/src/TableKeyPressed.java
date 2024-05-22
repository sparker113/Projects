import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JTable;

public class TableKeyPressed implements KeyListener, ClipboardOwner {
	JTable table;
	int row;
	int column;
	char[] restrictedChars;
	TableKeyPressed(JTable table) {
		this.table = table;
		this.table.addKeyListener(this);
	}
	TableKeyPressed(JTable table,char...restrictedChars) {
		this.table = table;
		this.table.addKeyListener(this);
		this.restrictedChars = restrictedChars;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if(checkRestricted(e.getKeyChar())) {
			e.consume();
			return;
		}
	}
	public boolean checkRestricted(char typedChar) {
		if(restrictedChars == null) {
			return false;
		}
		for(char c:restrictedChars) {
			if(c==typedChar) {
				return true;
			}
		}
		return false;
	}
	@Override
	public void keyPressed(KeyEvent e) {

		if(checkRestricted(e.getKeyChar())) {
			e.consume();
			return;
		}
		this.row = table.getSelectedRow();
		this.column = table.getSelectedColumn();
		if (String.valueOf(e.getKeyChar()) != "?" && e.getKeyCode() != KeyEvent.VK_ENTER && e.getKeyCode() != 17
				&& e.getKeyCode() != 16 && e.getKeyCode() != 20 && e.getKeyCode() != 9 && !e.isControlDown()
				&& !(e.getKeyCode() > 36 & e.getKeyCode() < 41) & e.getKeyCode() != KeyEvent.VK_SHIFT) {
			table.setValueAt("", table.getSelectedRow(), table.getSelectedColumn());
		}
		if (e.getKeyCode() == KeyEvent.VK_DELETE) {
			int dRows[];
			int dCols[];
			if (table.getSelectedColumn() != -1) {
				dRows = table.getSelectedRows();
				dCols = table.getSelectedColumns();
				for (int r : dRows) {
					for (int c : dCols) {
						table.setValueAt("", r, c);
					}
				}
				table.setValueAt("", table.getSelectedRow(), table.getSelectedColumn());
			}

		}

		if (e.getKeyCode() == KeyEvent.VK_V & e.isControlDown()) {
			Scanner a;
			int i = 0;
			String b;
			int ii = 0;
			try {
				a = new Scanner(
						Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor).toString());
				a.useDelimiter("\n");
				do {
					b = a.next();
					for (String col : b.split("\t")) {
						try {
							table.setValueAt(col, table.getSelectedRow() + i, table.getSelectedColumn() + ii);
						} catch (ArrayIndexOutOfBoundsException e1) {
							break;
						}
						ii++;
					}
					ii = 0;
					i++;
				} while (a.hasNext());
			} catch (HeadlessException el) {
				System.out.println("nope");
			} catch (UnsupportedFlavorException e1) {
				System.out.println("nope");
			} catch (IOException e1) {
				System.out.println("noper");
			}

		}

		if (e.getKeyCode() == KeyEvent.VK_C & e.isControlDown()) {
			StringBuilder c = new StringBuilder();
			String toClip = new String();
			boolean first = false;
			for (int col : ((JTable) e.getSource()).getSelectedColumns()) {
				// System.out.println(col);
				if (!first) {
					c.append("\n");
				} else {
					first = true;
				}
				for (int row : ((JTable) e.getSource()).getSelectedRows()) {
					// System.out.println(row);
					c.append(((JTable) e.getSource()).getValueAt(row, col));
					c.append("\t");
				}

			}

			toClip = c.toString();
			StringSelection d = new StringSelection(toClip);
			Clipboard thisClip = Toolkit.getDefaultToolkit().getSystemClipboard();
			thisClip.setContents(d, this);

		}

	}

	@Override
	public void keyReleased(KeyEvent e) {
		if(checkRestricted(e.getKeyChar())) {

			e.consume();
			return;
		}
		Integer code = e.getKeyCode();
		switch (code) {
		case KeyEvent.VK_ENTER, KeyEvent.VK_DOWN:
			table.changeSelection(row + 1, column, false, false);
		}

	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		// TODO Auto-generated method stub

	}

}
