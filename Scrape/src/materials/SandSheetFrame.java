package materials;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class SandSheetFrame extends JFrame {
	private SandTicketsObject sandTicketsObject;
	private ArrayList<String> wells;
	private String wellName;
	private String stage = "<#>";
	private String totalStage = "<#>";
	private String wellTotalStage = "<#>";
	private String padTotalStages = "<#>";
	private static int WIDTH = 900;
	private static int HEIGHT = 650;

	public SandSheetFrame(SandTicketsObject sandTicketsObject, ArrayList<String> wells) {
		this.sandTicketsObject = sandTicketsObject;
		this.wells = wells;
		nittyGritty();
	}

	public SandSheetFrame(SandTicketsObject sandTicketsObject, ArrayList<String> wells, String wellName) {
		this.sandTicketsObject = sandTicketsObject;
		this.wells = wells;
		this.wellName = wellName;
		nittyGritty();
	}

	void nittyGritty() {
		setBounds(200, 200, WIDTH, HEIGHT);
		setTitle("Sand Sheet/Designs");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setIconImage(
				new ImageIcon("C:\\Scrape\\Scrape.png").getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
		addTabbedPane();
		setVisible(true);
	}

	public void setStage(String stage) {
		this.stage = stage;
	}

	public void setTotalStage(String totalStage) {
		this.totalStage = totalStage;
	}

	public void setWellTotalStage(String wellTotalStage) {
		this.wellTotalStage = wellTotalStage;
	}

	public void setPadTotalStages(String padTotalStages) {
		this.padTotalStages = padTotalStages;
	}

	public void addTabbedPane() {
		add(new SandSheetPane(3));
	}

	public class SandSheetPane extends JTabbedPane {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private int numTypes;

		public SandSheetPane(int numTypes) {
			this.numTypes = numTypes;
			addTabs();
			nittyGritty();
		}

		public SandSheetPane() {
			this.numTypes = 2;
			addTabs();
			nittyGritty();
		}

		void nittyGritty() {
			setVisible(true);
			setName(SANDSHEETPANEL);
		}

		void addTabs() {
			if (wellName != null) {
				addTab("Sand Sheet", new SandSheet());
			}
			addTab("Sand Designs", new SandDesignPanel());
		}

		class SandSheet extends JPanel {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;
			private static final Float STX = 25f;
			private static final Float STY = 25f;
			private int possibleSilos = 8;
			private static final Float STAGEEAST = SandSheetFrame.WIDTH - 48f;
			private static final Float STAGESOUTH = SandSheetFrame.HEIGHT / 3f - 15f;
			private static final Float SCX = STX;
			private static final Float SCY = SandSheetFrame.HEIGHT / 3f + 15f;
			private static final Float SCHEDULEEAST = SandSheetFrame.WIDTH / 2f;
			private static final Float SCHEDULESOUTH = SandSheetFrame.HEIGHT - 75f;
			private static final int HEADERY = STY.intValue() - 20;
			private static final int SILO = 0;
			private static final int LBS_START = 1;
			private static final int LBS_END = 2;
			private static final int PERC_START = 3;
			private static final int PERC_END = 4;
			private Color wellColor = Color.BLUE;

			SandSheet() {
				nittyGritty();
			}

			SandSheet(Color wellColor) {
				this.wellColor = wellColor;
				nittyGritty();
			}

			SandSheet(int possibleSilos) {
				this.possibleSilos = possibleSilos;
				nittyGritty();
			}

			SandSheet(Color wellColor, int possibleSilos) {
				this.wellColor = wellColor;
				this.possibleSilos = possibleSilos / 2 + possibleSilos % 2;
				nittyGritty();
			}

			void nittyGritty() {
				// setBackground(Color.cyan);
				setName(SANDSHEETPANEL);
				setVisible(true);
			}

			ArrayList<Line> getStageOutlineBox() {
				ArrayList<Line> array = new ArrayList<>();
				array.add(new Line(STX.intValue(), STY.intValue(), STAGEEAST.intValue(), STY.intValue()));
				array.add(new Line(STAGEEAST.intValue(), STY.intValue(), STAGEEAST.intValue(), STAGESOUTH.intValue()));
				array.add(new Line(STAGEEAST.intValue(), STAGESOUTH.intValue(), STX.intValue(), STAGESOUTH.intValue()));
				array.add(new Line(STX.intValue(), STAGESOUTH.intValue(), STX.intValue(), STY.intValue()));
				return array;
			}

			ArrayList<Line> getHeaderLines() {
				ArrayList<Line> array = new ArrayList<>();
				array.add(new Line(STX.intValue(), STY.intValue(), STX.intValue(), HEADERY));
				array.add(new Line(STX.intValue(), HEADERY, STAGEEAST.intValue(), HEADERY));
				array.add(new Line(STAGEEAST.intValue(), HEADERY, STAGEEAST.intValue(), STY.intValue()));
				return array;
			}

			int getVerticalLineX(Float stageSchedule, int index, Float partitions) {
				if (index > partitions | index < 0) {
					return stageSchedule.intValue();
				}
				Float boxWidth = getBoxWidth();
				float partWidth = (boxWidth / partitions);
				return stageSchedule.intValue() + Math.round(partWidth * index);
			}

			Line getVerticalLine(Float stageScheduleX, int index, Float partitions) {
				int lineX = getVerticalLineX(stageScheduleX, index, partitions);
				Float south = stageScheduleX == STX ? STAGESOUTH : SCHEDULESOUTH;
				return new Line(lineX, stageScheduleX.intValue(), lineX, south.intValue());
			}

			int getHorizontalLineY(Float stageScheduleY, int index, Float partitions) {
				float boxHeight = (stageScheduleY == STY ? STAGESOUTH : SCHEDULESOUTH) - stageScheduleY;
				float partHeight = boxHeight / partitions;
				return Math.round(partHeight * index) + stageScheduleY.intValue();
			}

			Line getHorizontalLine(Float stageScheduleY, int index, Float partitions) {
				int lineY = getHorizontalLineY(stageScheduleY, index, partitions);
				return new Line((stageScheduleY == STY ? STX : SCX).intValue(), lineY,
						(stageScheduleY == STY ? STAGEEAST : SCHEDULEEAST).intValue(), lineY);
			}

			Float getBoxWidth() {
				return STAGEEAST - STX;
			}

			private void drawVerticalLines(Graphics2D g2d) {
				g2d.setStroke(new BasicStroke(1));
				g2d.setColor(Color.black);
				for (int i = 1; i < 5; i++) {
					drawALine(g2d, getVerticalLine(STX, i, 5f));
				}
			}

			private void drawALine(Graphics2D g2d, Line line) {
				g2d.drawLine(line.x1, line.y1, line.x2, line.y2);
			}

			private void drawHorizontalLines(Graphics2D g2d, int partitions, int stroke) {
				g2d.setColor(Color.black);
				for (int i = 1; i < partitions; i++) {
					if (i % (partitions / numTypes) == 0) {
						g2d.setStroke(new BasicStroke(stroke + 2));
					} else {
						g2d.setStroke(new BasicStroke(stroke));
					}
					drawALine(g2d, getHorizontalLine(STY, i, Float.valueOf(partitions)));
				}

			}

			private void drawHeader(Graphics2D g2d) {
				g2d.setStroke(new BasicStroke(3));
				for (Line line : getHeaderLines()) {
					drawALine(g2d, line);
				}

			}

			private int getNumLines(int partitions) {
				while (partitions % numTypes != 0) {
					partitions++;
				}
				return partitions;
			}

			private Polygon getHeaderPolygon() {
				Polygon polygon = new Polygon();
				ArrayList<Line> headerLines = getHeaderLines();
				for (Line line : headerLines) {
					polygon.addPoint(line.x2, line.y2);
				}
				polygon.addPoint(headerLines.get(0).x1, headerLines.get(0).y1);
				return polygon;
			}

			private void drawHeaderFill(Graphics2D g2d) {
				g2d.setColor(wellColor);
				g2d.setStroke(new BasicStroke(1));
				g2d.fillPolygon(getHeaderPolygon());
			}

			private void drawStageOutline(Graphics2D g2d) {
				g2d.setStroke(new BasicStroke(3));
				g2d.setColor(Color.black);
				for (Line line : getStageOutlineBox()) {
					g2d.drawLine(line.x1, line.y1, line.x2, line.y2);
				}
				// drawALine(g2d,getHorizontalLine(STY,1,2f));
			}

			private void drawHeaderString(Graphics2D g2d) {
				g2d.setColor(Color.black);
				g2d.setFont(g2d.getFont().deriveFont(Font.BOLD));
				String headerString = getHeaderString();
				g2d.drawString(headerString, getHeaderStringX(headerString), getHeaderStringY());
			}

			private String getHeaderString() {
				return wellName + " - (" + stage + "/" + wellTotalStage + "; " + totalStage + "/" + padTotalStages
						+ ")";
			}

			private int getHeaderStringY() {
				return HEADERY + 15;
			}

			private int getHeaderStringX(String headerString) {
				float stringWidth = headerString.length() * 5f;
				Float width = getBoxWidth();
				return Math.round((width - stringWidth) / 2f);
			}

			@Override
			public void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D) g;
				drawHeaderFill(g2d);
				drawHeaderString(g2d);
				drawStageOutline(g2d);
				drawHeader(g2d);
				drawHorizontalLines(g2d, getNumLines(possibleSilos + numTypes), 1);
				drawVerticalLines(g2d);
			}

			class Line {
				int x1;
				int y1;
				int x2;
				int y2;

				Line(int x1, int y1, int x2, int y2) {
					this.x1 = x1;
					this.y1 = y1;
					this.x2 = x2;
					this.y2 = y2;
				}
			}
		}

		class SandDesignPanel extends JPanel {
			SandDesignPanel() {
				nittyGritty();
			}

			void nittyGritty() {
				setBackground(Color.getHSBColor(-.85f, .1f, .85f));
				setName(DESIGNPANEL);
				addComps();
				setLayout(new SandDesignLayout());
				setVisible(true);
			}

			void addComps() {
				add(getSandDesignTable());
				constructComboBox();
				constructButton();
				constructSaveCloseButton();
			}

			JScrollPane getSandDesignTable() {
				JScrollPane scrollPane = new JScrollPane(new SandDesignTable());
				scrollPane.setName(SCROLLPANE);
				scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
				scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				return scrollPane;
			}

			void setWellSandDesign(String wellName, LinkedHashMap<String, String> designMap) {
				if (designMap == null || designMap.isEmpty()) {
					return;
				}
				sandTicketsObject.addToWellDesigns(wellName, designMap);
			}

			private void addPopupListener(JComboBox<String> comboBox) {
				comboBox.addPopupMenuListener(new PopupMenuListener() {

					@Override
					public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
						SandDesignTable sandDesignTable = (SandDesignTable) GUIUtilities
								.getComponentByName(SandDesignPanel.this, DESIGNTABLE);
						LinkedHashMap<String, String> designMap = sandDesignTable.getDesignAmts();
						setWellSandDesign(comboBox.getSelectedItem().toString(), designMap);
					}

					@Override
					public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
					}

					@Override
					public void popupMenuCanceled(PopupMenuEvent e) {
					}

				});
			}

			private void addFillActionListener(JComboBox<String> comboBox) {
				comboBox.addActionListener(new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						SandDesignTable sandDesignTable = (SandDesignTable) GUIUtilities
								.getComponentByName(SandDesignPanel.this, DESIGNTABLE);
						LinkedHashMap<String, String> designMap = sandTicketsObject
								.getWellSandDesign(comboBox.getSelectedItem().toString());
						sandDesignTable.fillTable(designMap);
					}
				});
			}

			void constructComboBox() {
				JComboBox<String> comboBox = new JComboBox<>();
				comboBox.setName(WELLLIST);
				wells.forEach(comboBox::addItem);
				addPopupListener(comboBox);
				addFillActionListener(comboBox);
				((JLabel) comboBox.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
				comboBox.setEditable(false);
				comboBox.setEnabled(true);
				add(comboBox);
			}

			@SuppressWarnings("unchecked")
			void saveInfoInTable() {
				SandDesignTable sandDesignTable = (SandDesignTable) GUIUtilities
						.getComponentByName(SandDesignPanel.this, DESIGNTABLE);
				setWellSandDesign(((JComboBox<String>) GUIUtilities.getComponentByName(SandDesignPanel.this, WELLLIST))
						.getSelectedItem().toString(), sandDesignTable.getDesignAmts());
				try {
					sandTicketsObject.writeToFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

			void constructButton() {
				JButton button = new JButton();
				button.setName(SAVEBUTTON);
				button.setText("Save");
				button.addActionListener(new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						saveInfoInTable();
					}
				});
				button.setVisible(true);
				add(button);
			}

			void constructSaveCloseButton() {
				JButton button = new JButton();
				button.setName(SAVECLOSE);
				button.setText("Save & Close");
				button.addActionListener(new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						saveInfoInTable();
						SandSheetFrame.this.dispose();
					}
				});
				button.setVisible(true);
				add(button);
			}

			class SandDesignTable extends JTable {
				final static int ROWS = 5;
				final static int COLUMNS = 2;
				final static int ROWHEIGHT = 16;

				SandDesignTable() {
					super(ROWS, COLUMNS);
					nittyGritty();
				}

				void nittyGritty() {
					setName(DESIGNTABLE);
					setCellSelectionEnabled(true);
					setColumnWidths();
					setRowHeight(ROWHEIGHT);
					new CellComboBox(this, 0, sandTicketsObject.getSandTypes());
					setTableHeaders();
					addKeyListener(new TableKeyPressed(this));
					addKeyListener(new NumericKeyListener());
				}

				void setColumnWidths() {
					this.setAutoResizeMode(AUTO_RESIZE_OFF);
					for (int i = 0; i < COLUMNS; i++) {
						getColumnModel().getColumn(i).setMinWidth(
								(SandSheetFrame.WIDTH - SandDesignLayout.WESTPAD - SandDesignLayout.EASTPAD) / 2);
					}
				}

				void setTableHeaders() {
					getColumnModel().getColumn(0).setHeaderValue("Sand Type");
					getColumnModel().getColumn(1).setHeaderValue("Design Amount");
				}

				void fillTable(LinkedHashMap<String, String> map) {
					clearTable();
					if (map == null) {
						return;
					}
					int i = 0;
					for (String s : map.keySet()) {
						setValueAt(s, i, 0);
						setValueAt(map.get(s), i, 1);
						i++;
					}
				}

				void clearTable() {
					for (int i = 0; i < getColumnCount(); i++) {
						for (int ii = 0; ii < getRowCount(); ii++) {
							setValueAt("", ii, i);
						}
					}
				}

				LinkedHashMap<String, String> getDesignAmts() {
					LinkedHashMap<String, String> map = new LinkedHashMap<>();
					int i = 0;
					while (i < getRowCount()) {
						if (getCellValue(i, 0).equals("0")) {
							break;
						}
						map.put(getCellValue(i, 0), getCellValue(i, 1));
						i++;
					}
					return map;
				}

				String checkEmptyDesignAmts() {
					int i = 0;
					while (i < getRowCount()) {
						if (!getCellValue(i, 0).equals("0") & getCellValue(i, 1).equals("0")) {
							return getCellValue(i, 0);
						}
						i++;
					}
					return "";
				}

				String getCellValue(int row, int column) {
					if (getValueAt(row, column) == null || getValueAt(row, column).toString().equals("")) {
						return "0";
					}
					return getValueAt(row, column).toString();
				}

				class NumericKeyListener implements KeyListener {

					@Override
					public void keyTyped(KeyEvent e) {
						Matcher matcher = Pattern.compile("[a-zA-Z ]").matcher(String.valueOf(e.getKeyChar()));
						if (matcher.find()) {
							e.consume();
							return;
						}
						SandDesignTable table = (SandDesignTable) e.getSource();
						table.editCellAt(table.getSelectedRow(), table.getSelectedColumn());

					}

					@Override
					public void keyPressed(KeyEvent e) {
						Matcher matcher = Pattern.compile("[a-zA-Z]").matcher(String.valueOf(e.getKeyChar()));
						if (matcher.find() & !e.isActionKey()) {
							e.consume();
							return;
						}

					}

					@Override
					public void keyReleased(KeyEvent e) {
						Matcher matcher = Pattern.compile("[a-zA-Z]").matcher(String.valueOf(e.getKeyChar()));
						if (matcher.find() & !e.isActionKey()) {
							e.consume();
							return;
						}

					}
				}
			}

			class SandDesignLayout extends SpringLayout {
				final static int NORTHPAD = 10;
				final static int WESTPAD = 10;
				final static int EASTPAD = 40;
				final static int COMBOBOXHEIGHT = 30;
				final static int BUTTONHEIGHT = 25;
				final static int BUTTONWIDTH = 150;
				final static int SAVECLOSEWIDTH = 150;
				int tableSouth = 0;

				SandDesignLayout() {
					construct();
				}

				@SuppressWarnings("unchecked")
				void construct() {
					for (Component c : getComponents()) {
						if (c.getName() == null) {
							continue;
						}
						String name = c.getName();
						switch (name) {
						case (WELLLIST):
							comboBoxLayout((JComboBox<String>) c);
							break;
						case (SCROLLPANE):
							designTableLayout((JScrollPane) c);
							break;
						case (SAVEBUTTON):
							saveButtonLayout((JButton) c);
							break;
						case (SAVECLOSE):
							saveCloseButtonLayout((JButton) c);
							break;
						}
					}
				}

				void comboBoxLayout(JComboBox<String> comboBox) {
					putConstraint(NORTH, comboBox, NORTHPAD, NORTH, SandDesignPanel.this);
					putConstraint(SOUTH, comboBox, NORTHPAD + COMBOBOXHEIGHT, NORTH, SandDesignPanel.this);
					putConstraint(WEST, comboBox, WESTPAD, WEST, SandDesignPanel.this);
					putConstraint(EAST, comboBox, SandSheetFrame.WIDTH - EASTPAD, WEST, SandDesignPanel.this);
				}

				void designTableLayout(JScrollPane scrollPane) {
					SandDesignTable sandDesignTable = (SandDesignTable) GUIUtilities.getComponentByName(scrollPane,
							DESIGNTABLE);
					tableSouth = getTableSouth(sandDesignTable);
					putConstraint(NORTH, scrollPane, getTableX(sandDesignTable), NORTH, SandDesignPanel.this);
					putConstraint(SOUTH, scrollPane, getTableSouth(sandDesignTable), NORTH, SandDesignPanel.this);
					putConstraint(WEST, scrollPane, WESTPAD, WEST, SandDesignPanel.this);
					putConstraint(EAST, scrollPane, SandSheetFrame.WIDTH - EASTPAD, WEST, SandDesignPanel.this);
				}

				void saveButtonLayout(JButton button) {
					putConstraint(NORTH, button, getButtonY(), NORTH, SandDesignPanel.this);
					putConstraint(SOUTH, button, getButtonY() + BUTTONHEIGHT, NORTH, SandDesignPanel.this);
					putConstraint(WEST, button, getButtonX(), WEST, SandDesignPanel.this);
					putConstraint(EAST, button, getButtonX() + BUTTONWIDTH, WEST, SandDesignPanel.this);
				}

				void saveCloseButtonLayout(JButton button) {
					putConstraint(NORTH, button, getButtonY(), NORTH, SandDesignPanel.this);
					putConstraint(SOUTH, button, getButtonY() + BUTTONHEIGHT, NORTH, SandDesignPanel.this);
					putConstraint(WEST, button, getSaveCloseX(), WEST, SandDesignPanel.this);
					putConstraint(EAST, button, getSaveCloseX() + SAVECLOSEWIDTH, WEST, SandDesignPanel.this);
				}

				int getTableX(SandDesignTable sandDesignTable) {
					return NORTHPAD * 2 + COMBOBOXHEIGHT;
				}

				int getTableSouth(SandDesignTable sandDesignTable) {
					int north = getTableX(sandDesignTable);
					return north + SandDesignTable.ROWHEIGHT * (SandDesignTable.ROWS + 1) + 5;

				}

				int getButtonY() {
					return tableSouth + NORTHPAD;
				}

				int getButtonX() {
					return getCenterX() - BUTTONWIDTH - WESTPAD;
				}

				int getSaveCloseX() {
					return getCenterX() + WESTPAD;
				}

				int getCenterX() {
					return SandSheetFrame.WIDTH / 2 + ((WESTPAD - EASTPAD) / 2);
				}

			}
		}

		public final static String SAVECLOSE = "saveCloseButton";
		public final static String SCROLLPANE = "scrollPane";
		public final static String SANDSHEETPANEL = "sandSheet";
		public final static String SANDSHEETTAB = "sandSheetTab";
		public final static String DESIGNTAB = "designTab";
		public final static String DESIGNPANEL = "designPanel";
		public final static String SAVEBUTTON = "saveButton";
		public final static String WELLLIST = "wellList";
		public final static String DESIGNTABLE = "designTable";

	}
}
