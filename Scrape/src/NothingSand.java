import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

public class NothingSand {
	private JDialog sandFrame;
	private JPanel transferPanel = new JPanel();
	private JTable sandTable = new JTable(10, 2);
	private JButton transferButton = new JButton();
	private TreeMap<String, String> sandMap;

	public NothingSand() {
		sandFrame = new JDialog();
		sandFrame.setModal(true);
		sandFrame.setTitle("Sand Volume Correction");
		System.out.println(3);
		SpringLayout channelLayout = new SpringLayout();
		sandFrame.setSize(400, 260);
		TableKeyPressed tableKeyPressed = new TableKeyPressed(sandTable);
		sandTable.setCellSelectionEnabled(true);
		sandTable.getColumnModel().getColumn(0).setHeaderValue("Sand Type");
		sandTable.getColumnModel().getColumn(1).setHeaderValue("Sand Amount");
		transferButton.setSize(100, 35);
		transferButton.setHorizontalAlignment(SwingConstants.CENTER);
		transferButton.setVerticalAlignment(SwingConstants.CENTER);

		transferButton.setText("DONE");
		TransferButton tButton = new TransferButton();
		transferButton.addActionListener(tButton);
		transferButton.setOpaque(true);
		transferPanel.add(transferButton);
		transferPanel.setLayout(new FlowLayout());
		transferPanel.setBackground(Color.DARK_GRAY);
		transferPanel.setOpaque(true);

		sandTable.setValueAt("100 MESH BROWN", 0, 0);
		sandTable.setValueAt("40/70 BROWN", 1, 0);
		// sandTable.setBounds(0, 0, sandFrame.getWidth(),(int)
		// Math.round(sandFrame.getHeight() * .9));
		JScrollPane sandScroll = new JScrollPane(sandTable);
		sandScroll.setSize(sandFrame.getWidth(), (int) Math.round(sandFrame.getHeight() * .9));

		sandScroll.setOpaque(true);
		sandFrame.add(sandScroll);
		sandFrame.add(transferPanel);
		channelLayout.putConstraint(SpringLayout.NORTH, sandScroll, 0, SpringLayout.NORTH, sandFrame);
		channelLayout.putConstraint(SpringLayout.SOUTH, sandScroll, 180, SpringLayout.NORTH, sandFrame);
		channelLayout.putConstraint(SpringLayout.WEST, sandScroll, 0, SpringLayout.WEST, sandFrame);
		channelLayout.putConstraint(SpringLayout.EAST, sandScroll, 400, SpringLayout.WEST, sandFrame);
		channelLayout.putConstraint(SpringLayout.NORTH, transferPanel, 180, SpringLayout.NORTH, sandFrame);
		channelLayout.putConstraint(SpringLayout.SOUTH, transferPanel, 220, SpringLayout.NORTH, sandFrame);
		channelLayout.putConstraint(SpringLayout.WEST, transferPanel, 0, SpringLayout.WEST, sandFrame);
		channelLayout.putConstraint(SpringLayout.EAST, transferPanel, 400, SpringLayout.WEST, sandFrame);
		transferButton.setVisible(true);
		transferPanel.setVisible(true);
		sandScroll.setVisible(true);
		sandFrame.setLayout(channelLayout);
		sandFrame.setResizable(false);
		sandFrame.setVisible(true);
		sandFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		System.out.println(4);

	}

	public TreeMap<String, String> getSand() {
		TreeMap<String, String> sandMap2 = new TreeMap<>();
		int i = 0;
		Long sandTotal = Long.valueOf(0);

		while (sandTable.getValueAt(i, 0) != null && !sandTable.getValueAt(i, 0).toString().equals("")) {
			System.out.println(i);
			sandMap2.put(String.valueOf(sandTable.getValueAt(i, 0)), String.valueOf(sandTable.getValueAt(i, 1)));
			sandTotal = sandTotal + Long.valueOf(String.valueOf(sandTable.getValueAt(i, 1)));
			i++;
		}

		sandMap2.put("Total Proppant (lbm)", String.valueOf(sandTotal));
		this.sandMap = sandMap2;
		return sandMap2;
	}

	public TreeMap<String, String> getSandArray() {
		return this.sandMap;
	}

	public void dispose() {
		sandFrame.dispose();
	}

	public boolean isVisible() {
		return sandFrame.isVisible();
	}

	public void setVisible(boolean visible) {
		sandFrame.setVisible(visible);
	}

	public JTable getTable() {
		return this.sandTable;
	}

	public class TransferButton extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {

			TreeMap<String, String> sandMap2 = getSand();
			int i = 1;
			for (String key : sandMap2.keySet()) {
				if (key == "Total Proppant (lbm)") {
					Main.yess.diagTable3.setValueAt(key, 0, 0);
					Main.yess.diagTable3.setValueAt(sandMap2.get(key), 0, 1);
				} else {
					Main.yess.diagTable3.setValueAt(key, i, 0);
					Main.yess.diagTable3.setValueAt(sandMap2.get(key), i, 1);
					i++;
				}

			}

			sandFrame.setVisible(false);
		}
	}

}
