package materials;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

public class TicketTemplateFrame extends JFrame {
	ArrayList<String> facilities;
	ArrayList<String> silos;
	ArrayList<String> sandTypes;
	SandTicketsFrame.TicketsTable ticketsTable;
	private static final int WIDTH = 500;
	private static final int HEIGHT = 150;

	TicketTemplateFrame(ArrayList<String> sandTypes, ArrayList<String> silos, ArrayList<String> facilities,
			SandTicketsFrame.TicketsTable ticketsTable) {
		this.sandTypes = sandTypes;
		this.silos = silos;
		this.facilities = facilities;
		this.ticketsTable = ticketsTable;
		nittyGritty();
	}

	void nittyGritty() {
		getContentPane().setBackground(Color.getHSBColor(-.85f, .1f, .85f));
		setIconImage(new ImageIcon("C:\\Scrape\\Scrape.png").getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
		setTitle("Generate Tickets");
		setBounds(getCenterX(), getCenterY(), WIDTH, HEIGHT);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		addComps();
		setLayout(new FlowLayout());
		setVisible(true);
	}

	void addComps() {
		constructSiloLabel();
		constructSiloComboBox();
		constructSandTypeLabel();
		constructSandTypeComboBox();
		constructFacilityLabel();
		constructFacilityComboBox();
		constructTicketsLabel();
		constructTicketsComboBox();
		constructGenerateButton();
	}

	final static String TYPE_LABEL = "typeLabel";

	void constructSandTypeLabel() {
		JLabel label = new JLabel();
		label.setName(TYPE_LABEL);
		label.setBackground(Color.getHSBColor(-.85f, .1f, .85f));
		label.setText("Sand Type: ");
		label.setOpaque(true);
		label.setVisible(true);
		add(label);
	}

	final static String TYPE_COMBO_BOX = "typComboBox";

	void constructSandTypeComboBox() {
		JComboBox<String> comboBox = new JComboBox<>();
		comboBox.setName(TYPE_COMBO_BOX);
		addSandTypes(comboBox);
		comboBox.setEditable(false);
		add(comboBox);
	}

	private void addSandTypes(JComboBox<String> comboBox) {
		for (String s : sandTypes) {
			comboBox.addItem(s);
		}
	}

	final static String SILO_LABEL = "siloLabel";

	void constructSiloLabel() {
		JLabel label = new JLabel();
		label.setName(SILO_LABEL);
		label.setText("Silo: ");
		label.setBackground(Color.getHSBColor(-.85f, .1f, .85f));
		label.setOpaque(true);
		label.setVisible(true);
		add(label);
	}

	final static String SILO_COMBO_BOX = "siloComboBox";

	void constructSiloComboBox() {
		JComboBox<String> comboBox = new JComboBox<>();
		comboBox.setName(SILO_COMBO_BOX);
		((JLabel) comboBox.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		addSilos(comboBox);
		comboBox.setEditable(false);
		comboBox.setVisible(true);
		add(comboBox);
	}

	private void addSilos(JComboBox<String> comboBox) {
		for (String s : silos) {
			comboBox.addItem(s);
		}
	}

	final static String FACILITY_LABEL = "facilityLabel";

	void constructFacilityLabel() {
		JLabel label = new JLabel();
		label.setName(FACILITY_LABEL);
		label.setBackground(Color.getHSBColor(-.85f, .1f, .85f));
		label.setText("Facility: ");
		label.setOpaque(true);
		label.setVisible(true);
		add(label);
	}

	final static String FACILITY_COMBO_BOX = "facilityComboBox";

	void constructFacilityComboBox() {
		JComboBox<String> comboBox = new JComboBox<>();
		comboBox.setName(FACILITY_COMBO_BOX);
		((JLabel) comboBox.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		addFacilityItems(comboBox);
		comboBox.setVisible(true);
		add(comboBox);
	}

	void addFacilityItems(JComboBox<String> comboBox) {
		comboBox.addItem("-");
		for (String s : facilities) {
			comboBox.addItem(s);
		}
	}

	final static String TICKETS_LABEL = "ticketsLabel";

	void constructTicketsLabel() {
		JLabel label = new JLabel();
		label.setBackground(Color.getHSBColor(-.85f, .1f, .85f));
		label.setText("# of Tickets: ");
		label.setName(TICKETS_LABEL);
		label.setOpaque(true);
		label.setVisible(true);
		add(label);
	}

	final static String NUM_TICKETS_COMBO_BOX = "numTicketsComboBox";

	void constructTicketsComboBox() {
		JComboBox<String> comboBox = new JComboBox<>();
		comboBox.setName(NUM_TICKETS_COMBO_BOX);
		((JLabel) comboBox.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		addTicketItems(comboBox);
		comboBox.setVisible(true);
		comboBox.setEditable(true);
		add(comboBox);
	}

	private void addTicketItems(JComboBox<String> comboBox) {
		ArrayList<Integer> numOptionsArray = getNumOptionsArray();
		for (Integer i : numOptionsArray) {
			comboBox.addItem(String.valueOf(i));
		}
	}

	private ArrayList<Integer> getNumOptionsArray() {
		ArrayList<Integer> array = new ArrayList<>();
		for (int i = 1; i <= 10; i++) {
			array.add(i);
		}
		return array;
	}

	final static String GENERATE_BUTTON = "generateButton";

	private void constructGenerateButton() {
		JButton button = new JButton();
		button.setName(GENERATE_BUTTON);
		button.setText("Generate");
		button.addActionListener(new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				generateTickets();
				dispose();
			}
		});
		add(button);
	}

	private String getSilo() {
		@SuppressWarnings("unchecked")
		JComboBox<String> siloComboBox = (JComboBox<String>) GUIUtilities.getComponentByName(getContentPane(),
				SILO_COMBO_BOX);
		return siloComboBox.getSelectedItem().toString();
	}

	private void generateTickets() {
		String facility = getFacility();
		if (!facilities.contains(facility)) {
			return;
		}
		Integer numTickets = getNumTickets();
		String silo = getSilo();
		String type = getSandType();
		int startRow = ticketsTable.findFirstEmptyRow(SandTicketsFrame.SHIPPER);
		ticketsTable.generateTickets(type, silo, facility, startRow, numTickets);
	}

	private String getSandType() {
		@SuppressWarnings("unchecked")
		JComboBox<String> typeComboBox = (JComboBox<String>) GUIUtilities.getComponentByName(getContentPane(),
				TYPE_COMBO_BOX);
		return typeComboBox.getSelectedItem().toString();
	}

	private String getFacility() {
		@SuppressWarnings("unchecked")
		JComboBox<String> facilityComboBox = (JComboBox<String>) GUIUtilities.getComponentByName(getContentPane(),
				FACILITY_COMBO_BOX);
		return facilityComboBox.getSelectedItem().toString();
	}

	private Integer getNumTickets() {
		@SuppressWarnings("unchecked")
		JComboBox<String> ticketsComboBox = (JComboBox<String>) GUIUtilities.getComponentByName(getRootPane(),
				NUM_TICKETS_COMBO_BOX);
		return Integer.valueOf(ticketsComboBox.getSelectedItem().toString());
	}

	int getCenterX() {
		return Toolkit.getDefaultToolkit().getScreenSize().width / 2 - (WIDTH / 2);
	}

	int getCenterY() {
		return Toolkit.getDefaultToolkit().getScreenSize().height / 2 - (HEIGHT / 2);
	}
}
