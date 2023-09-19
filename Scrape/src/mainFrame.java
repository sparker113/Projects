
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.DataFormatException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.event.MouseInputListener;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import exceltransfer.DataNames;
import exceltransfer.OperatorTemplateStageSummary;
import exceltransfer.TransferTemplate;
import intelie.ChannelMnemonics;
import intelie.CrewRequest;
import intelie.DataRequest;
import intelie.RememberMe;
import joblog.JobLogWells;
import joblog.LoginRequest;
import joblog.WellsRequest;
import login.EncryptCredentials;
import login.UserNamePassword;
import materials.SandSilosFrame;
import materials.SandTicketsFrame;
import materials.SandTicketsObject;

public class mainFrame extends JFrame implements MouseInputListener, Runnable, Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 5279129035304371243L;
	JButton button = new JButton();
	JPanel topPanel = new JPanel();
	JPanel topPanel2 = new JPanel(new FlowLayout());
	JPanel botPanel = new JPanel();
	JPanel topPanel3 = new JPanel();
	JPanel botPanel2 = new JPanel();
	JPanel botPanel3 = new JPanel();
	JLabel crewLabel;

	JTable mTable = new JTable(100, 13);
	JTable diagTable1 = new JTable(150, 3);
	JTable diagTable2 = new JTable(150, 2);
	JTable diagTable3 = new JTable(150, 3);
	JTable diagTable4 = new JTable(1, 2);
	// ArrayList<JTextField> a = new ArrayList<>();
	JFrame thisFrame = this;
	JFrame holdStage = new JFrame();
	JLayeredPane pane = new JLayeredPane();

	JLabel sledge = new JLabel();
	JLabel prop1 = new JLabel();

	JComboBox<String> textCombo1 = new JComboBox<>();
	JComboBox<String> textField1 = new JComboBox<>();
	JTextField textField2 = new JTextField();
	JTextField textField3 = new JTextField();
	JLabel pathLabel = new JLabel();
	// wellList well = new wellList();
	String tId = new String();
	String wellId = new String();
	String sttTime = new String();
	String at;
	String operator;
	channelData channels = new channelData();
	Analyze analyzeData;
	ArrayList<ArrayList<String>> dataSet;
	ArrayList<Integer> stageUp = new ArrayList<>();
	HashMap<String, ArrayList<Double>> stageInputs;
	ArrayList<Double> acidInputs = new ArrayList<>();
	Long inputTotalAcid = Long.valueOf(0);
	LinkedHashMap<String, Integer> sigMap;
	LinkedHashMap<String, Double> sigValueMap;
	Double sandRound;
	String sessionID;
	String csrfToken;
	String remCookie;
	SandTicketsObject sandTicketsObject;
	Boolean trackSand = true;
	JobLogWells jobLogWells;
	private intelie.CrewRequest crewRequest;
	private TransfersQueue transfersQueue;
	private Boolean clickedRun = false;
	private Boolean historic = false;
	private Boolean wellSummary = false;
	private EvaluatedDataObject evaluatedDataObject;
	private LinkedHashMap<String, LinkedHashMap<String, String>> chemSandMap = new LinkedHashMap<>();
	private HashMap<String, HashMap<String, HashMap<String, String>>> activeMap;
	private HashMap<String, HashMap<String, ArrayList<String>>> activePerfsMap;
	private HashMap<String, String> crewMap;
	private TreeMap<String, String> channelMnemonics;
	private JPanel progressPanel;
	private HashMap<String, String> additionalChannels = new HashMap<>();
	private LinkedHashMap<String, HashMap<String, String>> userDefinedMap;
	private ArrayList<Double> acidInputGal = new ArrayList<>();
	private String tvd;
	private JScrollPane sPane;
	private HashMap<String, Double> maxAverage;
	private HashMap<String, Integer> diagnosticsSet;
	private JPanel holdPanel = new JPanel();
	private JTextArea holdArea = new JTextArea();
	private JTextField holdText = new JTextField();

	private String wellName;
	private PlotsPane plotsPane;
	private Long sand;
	ChannelPane channelPane;
	private EmailFrame emailFrame = new EmailFrame(350, 300);
	private PJRFilesPane pjrFilesPane = new PJRFilesPane(".1 TR,", ".2 Plots", ".3 WA", ".4 Invoice", ".5 cs",
			"C:\\Scrape\\checklist.txt");
	private PJRFilesPane emailComponents = new PJRFilesPane("TR", "PJR", "Invoice", "CSV", "Operator Workbook",
			"C:\\Scrape\\email_comps.txt");
	private Channels ch = new Channels();
	private WellDrivePane wellDrivePane = new WellDrivePane();
	private SumPanel sumPanel;
	LastStartCloseComponent lastStartClose;
	private String perfs;
	private ExecutorService executor;
	private JMenuItem xPort = new JMenuItem("Export Treatment");
	public final static String VERSION = "Scrape v.2.9";

	// Updated setDiagnostics in the EvaluateCSV class
	// Fixed bug in EvaluateCSVData that overwrites the perfs as "0"
	// Removed deprecated code and synchronizing tools that were no longer being
	// used from Analyze
	// Fixed bug in Analyze when removing indices from stEndAcid array
	// Fixed bug when pulling in the start/end/dgb pump info
	// Added EvalPumpData class
	// Fixed issue with crews operating in different time zones
	// Parsed petroIQ wells correctly to fix the uncommon occurrence of a well
	// not populating in the program
	// Fixed the issue where B-side crew names were not being saved correctly to
	// JobLogWells.crewMap;
	// Fixed the issue where the rememberMe cookie was being saved from the
	// local cookieJar
	// Switched the filePath for the serialized rememberMe object to the correct
	// file :)))))
	// Added a login screen that hides the password and encrypts the credentials
	// for intelie and ties system identifiers to the credentials to avoid
	// risk associated with sharing files
	// Added the ability to configure multiple sand types run in tandem and balanced
	// to reflect to the total sand that is calculated for the stage
	// Added functionality for sand schedules that alternate sand types
	// Added the acid strap to the chemical table within the red treatment report
	// stage tab
	// Added an overloaded
	// getArrayWithGreaterThanCondition(Array,Array,conditionVal,maxValue)
	// To make sure that any values that are omitted in the process of finding the
	// max aren't so big that it causes a significant error in the average
	// calculated
	// Added the pad name to the setDiagnostics map for when it is sent
	// to the server, per structure requested by Blake 02/25/2023 - v.2.1
	// Fixed issues with the times when evaluating a CSV
	// Added Well Specific functionality to UserDefinedFrame class
	// Fixed Issue that was occuring where the program would input
	// the 'Total Proppant (lbm)' in the treatment summary as a
	// sand type
	// Fixed the issue that was occuring when the user was evaluating
	// a csv where the 'Treating Pressure' channel had to be
	// prefixed with 'Pressure 1'
	// Changed 'Josh Mode' to allow user to select any color of background
	// for the tables in the main user interface
	// Fixed issue when user inputs multiple acid types and the program only
	// transfers one type to the Red TR
	// Added well_specific_data in UserDefinedFrame
	// Added multi_sand_stage function in UserDefinedFrame
	// Added functionality to configure the server address that the data is sent
	// Updated ChemSandFrame and ChannelPane classes to cancel editing before
	// Input values are saved
	// Added configuration functionality for transferring data to the ProPetro
	// Treatment Report
	// Added notification for when the data object completes transmission to server
	// when updating the IP of the server
	// Added automated isip and close pressure for testing
	// Added the ability to add channels when running two blenders
	// Added the option to execute Carlos' engineload.exe file
	// Added to toggle on/off shutdowns and preflush in the treatment summary
	// Updated SplitAverage class for instances that the array that is being
	// averaged for sand wont reduce the array if the reduce amount
	// is greater than the size of the array it is averaging

	// Fixed recent issue with total channels flat lining and then Scrape smoothing
	// out the jump when raw data is present in the array again

	// Fixed Issue with AdditionalChannelRequest blocking; passed a semaphore in
	// construct
	// that I release and then tried to acquire a permit from a different semaphore
	// Updated the monitoringDGBAction() method to only input the number of pumps
	// blending
	// if the previous number is less than or there isn't an input

	// Updated wellListSelection to check the fileNames in the configured path
	// against the
	// list of wells in the well list from PetroIQ
	// Functionality that was previously implemented and taken out by accident
	// cleaning up code

	// Updated DataNames class with all names as final static String objects
	// Fixed issue when evaluating a csv where the sand types and sand volumes
	// for each sand type would get added to their respective arrays twice
	// and mess up the treatment summary
	// Fixed issue when getting the Diagnostic marker time stamps, where it was
	// checking for the open.txt file regardless of the target file

	// Fixed issues when evaluating a csv
	// I take the very first timestamp and then construct the 'Job Time'
	// array by adding a second to each subsequent index to overcome issues
	// where it is saved without the second precision on the times
	// Changed the sand to fix sand name inputs that are not capitalized to minimize
	// confusion when a sand type is alternated throughout a schedule and is
	// capitalized
	// in one row input and not the others
	// Updated the error message when a channel is not found in a csv to notify the
	// user
	// of the specific channel it was unable to find
	// Notify the user that sand/chems have yet to be input if he/she tries to
	// evaluate a stage
	// through a csv
	// Fixed bug that would crash the program if a stage is attempted to be
	// evaluated through a csv
	// and there aren't any user-defined data types for that specific operator
	// Added null checks throughout the EvaluateCSVData class when handling the data
	// map object
	// and when parsing the csv to ensure legitimate values are available before
	// attempting to
	// retrieve the resources
	// Added fifty rows to the UserDefinedFrame class' table, for a total of 100
	// rows to input user defined
	// data values

	// Increased allowable slope within the smoothTotalChannel(ArrayList<String>
	// array,Float slope) method
	// to 30
	// Fixed the issue where Mid-Stage Acid input volumes weren't combining with the
	// spearhead strap
	// when writing the chemical values to the chemical table
	
	// Fixed Time formatting when transferring data to the Red TR
	// Added login window to gather credentials for petroIQ
	// Added makeBlenderIDRequest() method to mainFrame class, implemented in mainEvaluate() method
		//to get the blender asset id from intelie and write the id to diagTable2
	mainFrame() throws Exception {
		Thread.currentThread().getContextClassLoader().clearAssertionStatus();
		transfersQueue = TransfersQueue.readFromFile();
		retrieveCrewMap();
		getRoundSand();
		stageInputs = new HashMap<>();

		emailComponents.setTitle("Email Components");

		emailComponents.setButtonAction(getEmailCompAction());
		executor = Executors.newCachedThreadPool();
		sumPanel = new SumPanel(mTable);
		wellDrivePane.setButtonAction(new WellDriveAction());
		emailFrame.setButtonAction(new EmailButton());
		plotsPane = new PlotsPane();
		plotsPane.setButtonAction(getPlotsPaneAction());
		JMenuBar menu = new JMenuBar();
		JMenu mFile = new JMenu("File");
		JMenu mReports = new JMenu("Reports");
		JMenu mView = new JMenu("View");
		JMenu mEdit = new JMenu("Edit");

		JMenuItem emailComp = new JMenuItem("Email Attachments");
		emailComp.addActionListener(new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				emailComponents.setVisible(!emailComponents.isVisible());
			}
		});
		JMenuItem pjrFiles = new JMenuItem("PJR Files");
		pjrFiles.addActionListener(new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				pjrFilesPane.setVisible(!pjrFilesPane.isVisible());
			}
		});
		int ro;
		int co;
		for (ro = 0; ro < mTable.getRowCount(); ro++) {
			for (co = 0; co < mTable.getColumnCount(); co++) {
				mTable.setValueAt("", ro, co);
			}
		}
		altScreenUpdate();
		JMenuItem selectWellList = new JMenuItem("Select Wells to View");
		JMenuItem invoice = new JMenuItem("Save Invoice");
		JMenuItem mergeFiles = new JMenuItem("Merge PDF's");
		JMenuItem wellDrive = new JMenuItem("WellDrive Credentials");
		JMenuItem operatorTemplate = new JMenuItem("New Operator Template");

		JMenuItem wA = new JMenuItem("Water Analysis");
		JMenuItem postStage = new JMenuItem("CM Email");
		JMenuItem uploads = new JMenuItem("Uploads");
		// new JMenuItem("Plots");
		// JMenuItem holdItem = new JMenuItem("Hold Stage Input");
		JMenuItem stageInclusions = new JMenuItem("Sub-Stage Inputs");

		JMenuItem configPath = new JMenuItem("Configure Path");
		JMenu csvMenu = new JMenu("CSV");
		JMenuItem csv = new JMenuItem("CSV Channels");
		csv.addActionListener(new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				ChannelPane channelPane = new ChannelPane("csvchannels.txt");
				channelPane.setVisible(true);
			}
		});
		JMenuItem runCSV = new JMenuItem("Evaluate CSV");
		runCSV.addActionListener(new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (chemSandIsNull()) {
					return;
				}
				String well = getWellName();
				String stage = String.valueOf(getTreatmentNumber());
				ArrayList<Double> acidSpearheadInput = new ArrayList<>();
				acidSpearheadInput.add(getAcidSpearhead());
				System.out.println(getOperator());
				if (chemSandMap == null || chemSandMap.isEmpty() || chemSandMap.get("sand").isEmpty()) {
					JOptionPane.showMessageDialog(Main.yess, "Input sand and chemical straps");
					return;
				}
				EvaluateCSVData evaluateCSV = new EvaluateCSVData(well, stage, acidSpearheadInput, chemSandMap,
						sandRound, getOperator());
				executor.execute(evaluateCSV);
			}
		});
		csvMenu.add(csv);
		csvMenu.add(runCSV);
		JMenuItem jobSetup = new JMenuItem("Job Setup");
		jobSetup.addActionListener(Processes.getProcessAction(Processes.jobSetup()));
		JMenu taskMenu = new JMenu("Completed Tasks");
		JMenuItem taskSettings = new JMenuItem("Completed Tasks Settings");
		taskSettings.addActionListener(Processes.getProcessAction(Processes.taskMaker()));
		JMenuItem tasksCompleted = new JMenuItem("Completed Tasks");
		tasksCompleted.addActionListener(Processes.getProcessAction(Processes.completedTasks()));
		taskMenu.add(taskSettings);
		taskMenu.add(tasksCompleted);

		selectWellList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				WellListSelection wellListSelection = new WellListSelection(activeMap,
						JobLogWells.getArrayOfKeys(crewMap), historic);
				wellListSelection.setButtonAction(getWellSelectionAction(wellListSelection));
			}
		});

		JMenuItem channels = new JMenuItem("Channels");
		JMenuItem refresh = new JMenuItem("Refresh Wells/Treatments");
		JMenuItem email = new JMenuItem("Email Settings");

		OpTemplate opTemplate = new OpTemplate();
		PostStageInvoice postStageInvoice = new PostStageInvoice();
		invoice.addActionListener(postStageInvoice);
		operatorTemplate.addActionListener(opTemplate);
		wA.addActionListener(Processes.getProcessAction(Processes.waterAnalysis()));
		postStage.addActionListener(Processes.getProcessAction(Processes.email()));
		postStage.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				executor.execute(() -> {
					String well = textCombo1.getSelectedItem().toString();
					String stage = textField1.getSelectedItem().toString();
					String endTime = GetDatesTimes.getEndDate(mTable) + "T" + GetDatesTimes.getEndTime(mTable);
					MetricsMap metricsMap = null;
					try {
						metricsMap = MetricsMap.readFromFile();
						metricsMap.addToMap(well, stage, endTime, crewLabel.getText());
					} catch (IOException | ClassNotFoundException e1) {
						e1.printStackTrace();
					}
					System.out.println(metricsMap);
				});
			}
		});
		uploads.addActionListener(Processes.getProcessAction(Processes.wellDrive()));
		Refresh ref = new Refresh();
		HideUnhide hUnhide = new HideUnhide();
		HideUnhideEmail unhideEmail = new HideUnhideEmail();
		viewChannels cUnhide = new viewChannels();
		ConfigurePath conPath = new ConfigurePath();
		PostStageMerge postStageMerge = new PostStageMerge();

		stageInclusions.addActionListener(getStageInclusionsAction());
		mergeFiles.addActionListener(postStageMerge);
		TransferToWorkbook actionTransfer = new TransferToWorkbook(mergeFiles, xPort);

		wellDrive.addActionListener(new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				wellDrivePane.setVisible(!wellDrivePane.isVisible());
			}
		});

		refresh.addActionListener(ref);
		// holdItem.addActionListener(hUnhide);
		channels.addActionListener(cUnhide);
		configPath.addActionListener(conPath);
		xPort.addActionListener(actionTransfer);
		email.addActionListener(unhideEmail);
		// JMenuItem fonts = new JMenuItem("Choose Font");
		JMenu emailMenu = new JMenu("Email");
		mFile.add(jobSetup);
		mFile.add(configPath);
		mFile.add(selectWellList);
		mFile.add(pjrFiles);
		mFile.add(wellDrive);
		mFile.add(taskMenu);
		mFile.add(constructSaveTreatmentItem());
		mFile.add(constructCombineHistoricItem());
		mFile.add(getLoginItem());
		mFile.add(refresh);
		mReports.add(wA);
		mReports.add(xPort);
		mReports.add(invoice);
		mReports.add(postStage);
		mReports.add(mergeFiles);
		mReports.add(uploads);
		mReports.add(
				getMenuItemShell("engine_load", "Engine Loads", Processes.getProcessAction(Processes.engineLoad())));

		// mView.add(holdItem);
		mView.setName("view");
		mView.add(stageInclusions);
		mView.add(csvMenu);
		mView.add(constructPumpMenu());
		mView.add(roundSandMenuItem());
		mView.add(constructTeloItem());
		mView.add(constructHistoricMenu());
		emailMenu.add(email);
		emailMenu.add(emailComp);
		mEdit.add(constructChemSandMenu());
		mEdit.add(emailMenu);
		mEdit.add(channels);
		mEdit.add(operatorTemplate);
		mEdit.add(getConfigureMenu());
		mEdit.add(getJoshModeItem());
		mEdit.add(getServerAddressItem());
		mEdit.add(getSummaryOptionsItem());

		// mEdit.add(fonts);
		// mLive.add(liveRefresh);
		// mLive.add(liveDashboard);
		menu.add(mFile);
		menu.add(mReports);
		menu.add(mView);
		menu.add(mEdit);
		menu.add(getMaterialsMenu());
		menu.add(sumPanel);
		// Setting application icon image
		ImageIcon PP = new ImageIcon("C:\\Scrape\\Scrape.png");
		this.setTitle(VERSION); // Set Frame Title
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); // Close the Frame with "X"
		this.setIconImage(PP.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
		this.setSize((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth(),
				Toolkit.getDefaultToolkit().getScreenSize().height
						- Toolkit.getDefaultToolkit().getScreenInsets(GraphicsEnvironment.getLocalGraphicsEnvironment()
								.getDefaultScreenDevice().getDefaultConfiguration()).bottom); // Set x and y dimensions
		this.addComponentListener(new WindowAdapter());
		// System.out.println(menu.getBaseline((int)
		// Toolkit.getDefaultToolkit().getScreenSize().getWidth(),
		// (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight()));
		pane.setBounds(0,
				menu.getBaseline((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth(),
						(int) Toolkit.getDefaultToolkit().getScreenSize().getHeight()),
				(int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth()),
				(int) Toolkit.getDefaultToolkit().getScreenSize().getHeight());

		ImageIcon holdIcon = new ImageIcon("C:\\Scrape\\Scrape.png");
		ImageIcon PPP = new ImageIcon("images.png");
		Image image = PPP.getImage().getScaledInstance((int) ((Toolkit.getDefaultToolkit().getScreenSize().getWidth())),
				(int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 240, java.awt.Image.SCALE_SMOOTH);

		ImageIcon image2 = new ImageIcon(image);
		sledge.setIcon(image2);
		sledge.setHorizontalAlignment(SwingConstants.LEFT);
		sledge.setVerticalAlignment(SwingConstants.TOP);
		prop1.setIcon(image2);
		prop1.setHorizontalAlignment(SwingConstants.RIGHT);
		prop1.setVerticalAlignment(SwingConstants.TOP);

		topPanel2.setOpaque(true);
		topPanel.setOpaque(true);
		topPanel.setBackground(Color.GRAY);
		topPanel2.setBackground(Color.black);
		topPanel2.setBounds(0, 0, (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() * .25), 200);
		topPanel3.setBounds(topPanel2.getWidth(), 0,
				(int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() * .7), 200);
		topPanel3.setBackground(Color.GRAY);
		topPanel3.setOpaque(true);
		topPanel3.setVisible(true);
		topPanel.setBounds(0, 0, thisFrame.getWidth(), 200);
		botPanel.setBounds((int) (thisFrame.getWidth() * .15), (int) topPanel.getBounds().getY() + 200,
				(int) (topPanel.getWidth() * .7), thisFrame.getHeight() - topPanel.getHeight() - 70);
		botPanel2.setBounds(0, 200, (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() * .15),
				thisFrame.getHeight() - topPanel.getHeight() - 70);
		botPanel3.setBounds((int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() * .85), 200,
				(int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() * .15),
				thisFrame.getHeight() - topPanel.getHeight() - 70);
		botPanel3.add(prop1);
		botPanel2.add(sledge);
		botPanel.setOpaque(true);
		mTable.getColumnModel().getColumn(0).setHeaderValue("Start Time");
		mTable.getColumnModel().getColumn(1).setHeaderValue("End Time");
		mTable.getColumnModel().getColumn(2).setHeaderValue("Stage Number");
		mTable.getColumnModel().getColumn(3).setHeaderValue("Start Date");
		mTable.getColumnModel().getColumn(4).setHeaderValue("End Date");
		mTable.getColumnModel().getColumn(5).setHeaderValue("Proppant Con");
		mTable.getColumnModel().getColumn(6).setHeaderValue("Clean BBLS");
		mTable.getColumnModel().getColumn(7).setHeaderValue("Average Pressure");
		mTable.getColumnModel().getColumn(8).setHeaderValue("Average Rate");
		mTable.getColumnModel().getColumn(9).setHeaderValue("Slurry BBLS");
		mTable.getColumnModel().getColumn(10).setHeaderValue("Sand Type");
		mTable.getColumnModel().getColumn(11).setHeaderValue("Sand (lbs)");
		mTable.getColumnModel().getColumn(12).setHeaderValue("SubStage");
		mTable.setSurrendersFocusOnKeystroke(true);
		int i;
		diagTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		for (i = 0; i < 13; i++) {
			mTable.getColumnModel().getColumn(i).setPreferredWidth(90);
			if (i < 2) {
				diagTable1.getColumnModel().getColumn(i).setPreferredWidth(150);
				diagTable2.getColumnModel().getColumn(i).setPreferredWidth(150);
			} else if (i < 3) {
				diagTable3.getColumnModel().getColumn(i).setPreferredWidth(50);
			}
		}
		diagTable1.getColumnModel().getColumn(0).setHeaderValue("Chemical");
		diagTable1.getColumnModel().getColumn(1).setHeaderValue("Strap");
		diagTable1.getColumnModel().getColumn(2).setHeaderValue("Units");
		diagTable2.getColumnModel().getColumn(0).setHeaderValue("Sig. Values");
		diagTable2.getColumnModel().getColumn(1).setHeaderValue("Values");

		diagTable3.getColumnModel().getColumn(0).setHeaderValue("Sand Type");
		diagTable3.getColumnModel().getColumn(1).setHeaderValue("Volume Pumped");
		diagTable3.getColumnModel().getColumn(2).setHeaderValue("Design Volume");

		diagTable4.getColumnModel().getColumn(0).setHeaderValue("Clean Total");
		diagTable4.getColumnModel().getColumn(1).setHeaderValue("Slurry Total");
		diagTable4.setRowHeight(20);

		diagTable1.setCellSelectionEnabled(true);
		diagTable2.setCellSelectionEnabled(true);
		diagTable3.setCellSelectionEnabled(true);
		diagTable4.setCellSelectionEnabled(true);
		addKeyListenerToTables(mTable, diagTable1, diagTable2, diagTable3, diagTable4);
		JScrollPane dT1 = new JScrollPane(diagTable1);
		JScrollPane dT2 = new JScrollPane(diagTable2);
		JScrollPane dT3 = new JScrollPane(diagTable3);
		JScrollPane dT4 = new JScrollPane(diagTable4);

		dT4.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		topPanel3.add(dT1);
		topPanel3.add(dT2);
		topPanel3.add(dT3);
		topPanel3.add(dT4);
		TopPanelLayout tPLayout = new TopPanelLayout(dT1, dT2, dT3, dT4, topPanel3);

		topPanel3.setLayout(tPLayout);

		botPanel.addMouseListener(this);
		topPanel.addMouseListener(this);

		mTable.addMouseListener(new SumPanelMouseListener());
		butClick runIt = new butClick();
		button.addActionListener(runIt);
		button.setBackground(Color.white);
		button.setText("Run");
		button.setOpaque(true);
		button.setBackground(Color.white);

		mTable.setCellSelectionEnabled(true);

		update Update = new update();
		update1 Update1 = new update1();
		// botPanel.add(mTable);
		mTable.setVisible(true);
		// botPanel.setVisible(true);
		textCombo1.setBounds((int) (topPanel2.getWidth() * .1), 25, (int) (topPanel2.getWidth() * .8), 20);
		textCombo1.addItem("-");
		textCombo1.setOpaque(true);
		textCombo1.setAction(Update);
		textCombo1.setBackground(Color.WHITE);
		((JLabel) textCombo1.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		textCombo1.setEnabled(false);
		button.setBounds(textCombo1.getX(), 130, textCombo1.getWidth() / 3, topPanel2.getHeight() - 150);
		button.setVisible(true);

		textCombo1.setBackground(Color.WHITE);
		textField1.setBounds((int) (topPanel2.getWidth() * .1), 50, (int) (topPanel2.getWidth() * .8), 20);
		textField1.addItem("<Stage Number>");
		textField1.setBackground(Color.WHITE);
		textField1.setAction(Update1);

		((JLabel) textField1.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

		textField2.setHorizontalAlignment(SwingConstants.CENTER);
		textField2.setBounds((int) (topPanel2.getWidth() * .1), 75, (int) (topPanel2.getWidth() * .8), 20);
		textField2.setText(OPERATOR_PLACE_HOLDER);
		textField2.setBackground(Color.WHITE);

		textField3.setBounds((int) (topPanel2.getWidth() * .1), 100, (int) (topPanel2.getWidth() * .8), 20);
		textField3.setText("<Formation>");
		textField3.setBackground(Color.WHITE);
		textField3.setHorizontalAlignment(SwingConstants.CENTER);

		pathLabel.setBounds((int) (topPanel2.getWidth() * .025), 180, (int) (topPanel2.getWidth() * .95), 20);
		pathLabel.setBackground(Color.BLACK);
		pathLabel.setForeground(Color.WHITE);
		pathLabel.setOpaque(true);

		pathLabel.setFont(new Font("Times New Roman", Font.PLAIN, 12));
		pathLabel.setLayout(null);
		pathLabel.setHorizontalAlignment(SwingConstants.CENTER);
		pathLabel.setVisible(true);

		pathLabel.setText(ReadDirectory.readDirect());

		int lastStartCloseX = button.getBounds().x + button.getWidth() + 5;
		this.lastStartClose = new LastStartCloseComponent(lastStartCloseX, (int) button.getBounds().getY(),
				textField3.getWidth() * 2 / 3, button.getHeight());

		topPanel2.add(button);
		topPanel2.add(textField1);
		topPanel2.add(textField2);
		topPanel2.add(textCombo1);
		topPanel2.add(textField3);
		topPanel2.add(pathLabel);
		topPanel2.add(lastStartClose);
		topPanel2.setLayout(null);

		sPane = new JScrollPane(mTable);
		setScrollPane(sPane);

		sPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		sPane.setBounds((int) (this.getBounds().getWidth() * .15), topPanel.getHeight(),
				(int) (thisFrame.getWidth() * .7), botPanel3.getHeight() - 17);
		sPane.setVisible(true);

		pane.add(sPane, JLayeredPane.PALETTE_LAYER);
		// pane.add(frameScrollPane, JLayeredPane.POPUP_LAYER);
		pane.add(topPanel2, JLayeredPane.PALETTE_LAYER);
		pane.add(topPanel3, JLayeredPane.PALETTE_LAYER);
		pane.add(topPanel, JLayeredPane.DEFAULT_LAYER);
		pane.add(botPanel2, JLayeredPane.DEFAULT_LAYER);
		pane.add(botPanel3, JLayeredPane.DEFAULT_LAYER);
		pane.add(constructInfoPanel(), JLayeredPane.PALETTE_LAYER);
		this.setJMenuBar(menu);
		this.getContentPane().add(pane);
		this.setVisible(true); // Make this visible
		// Frame construction for "Job on Hold" frame
		Image holdImage = holdIcon.getImage();
		JButton hideHold = new JButton();
		hideHold.setText("Hide");
		hideHold.addActionListener(hUnhide);
		holdStage.setIconImage(holdImage.getScaledInstance(64, 64, Image.SCALE_SMOOTH));
		holdStage.setBounds(0, 75, 215, 165);
		holdPanel.setBounds(holdStage.getBounds());
		SpringLayout holdLayout = new SpringLayout();
		holdArea.setText("Input the total clean amount at any point the "
				+ "job should have been staged but was not; separate each value by ','");
		holdArea.setWrapStyleWord(true);
		holdArea.setLineWrap(true);
		holdPanel.setBackground(Color.getHSBColor((float) -.85, (float) .1, (float) .85));
		holdArea.setBackground(Color.getHSBColor((float) -.85, (float) .1, (float) .85));
		holdPanel.add(holdArea);
		holdPanel.add(holdText);
		holdPanel.add(hideHold);
		holdLayout.putConstraint(SpringLayout.NORTH, holdArea, 15, SpringLayout.NORTH, holdPanel);
		holdLayout.putConstraint(SpringLayout.SOUTH, holdArea, 80, SpringLayout.NORTH, holdPanel);
		holdLayout.putConstraint(SpringLayout.WEST, holdArea, 10, SpringLayout.WEST, holdPanel);
		holdLayout.putConstraint(SpringLayout.EAST, holdArea, 10, SpringLayout.EAST, holdPanel);
		holdLayout.putConstraint(SpringLayout.NORTH, holdText, 85, SpringLayout.NORTH, holdPanel);
		holdLayout.putConstraint(SpringLayout.SOUTH, holdText, 105, SpringLayout.NORTH, holdPanel);
		holdLayout.putConstraint(SpringLayout.WEST, holdText, 0, SpringLayout.WEST, holdArea);
		holdLayout.putConstraint(SpringLayout.EAST, holdText, holdPanel.getWidth() - 30, SpringLayout.WEST, holdPanel);
		holdLayout.putConstraint(SpringLayout.NORTH, hideHold, 105, SpringLayout.NORTH, holdPanel);
		holdLayout.putConstraint(SpringLayout.SOUTH, hideHold, 125, SpringLayout.NORTH, holdPanel);
		holdLayout.putConstraint(SpringLayout.WEST, hideHold, 30, SpringLayout.WEST, holdArea);
		holdLayout.putConstraint(SpringLayout.EAST, hideHold, holdPanel.getWidth() - 60, SpringLayout.WEST, holdPanel);
		holdPanel.setLayout(holdLayout);
		holdStage.add(holdPanel);
		holdStage.setResizable(false);
		holdStage.setAlwaysOnTop(true);
		holdStage.setVisible(false);
		holdStage.setDefaultCloseOperation(HIDE_ON_CLOSE);

		setMouseListener(mTable);
		// Obtain bearer token
		// at = GrantAccess.readAccessToken();
		JProgressBar progressBar = getProgressBar();
		progressPanel = getProgressPanel();
		progressPanel.add(progressBar);
		// wellRequestQueue.add(new GetListWellPaging(at,false,progressBar));
		petroIQInformation();
		this.setAt(at);
		// this.setWellId(wellId);
		this.setJobTime(sttTime);
		this.setTreatmentId(tId);
	}

	public final static String LOGIN_ITEM = "login";
	public final static String ADDRESS_ITEM = "server_address";
	public final static String SERVER_ADDRESS_FILENAME = "C:\\Scrape\\server_address.txt";
	public final static String SERVER_ADDRESS_DEFAULT = "10.119.224.218";

	public final static Boolean PREFLUSH_DEFAULT = true;
	public final static Boolean SHUTDOWN_DEFAULT = true;

	private void addKeyListenerToTables(JTable... tables) {
		for (JTable table : tables) {
			new TableKeyPressed(table);
		}
	}

	public final static String DIESEL_TRACKING = "diesel_tracking";
	public final static String NOT_TRACKING_DIESEL_TEXT = "Track Diesel Usage";
	public final static String TRACKING_DIESEL_TEXT = "Stop Tracking Diesel";

	private AbstractAction getStageInclusionsAction() {
		return new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				StageInputsFrame inputsFrame;
				if (stageInputs.isEmpty()) {
					inputsFrame = new StageInputsFrame(300, 250);
				} else {
					HashMap<String, ArrayList<Double>> copyMap = new HashMap<>();
					copyMap.putAll(stageInputs);
					copyMap.put("Acid_Input", acidInputGal);
					inputsFrame = new StageInputsFrame(300, 250, copyMap);
				}
				executor.execute(() -> {
					removeInputs();
					Main.yess.stageInputs.putAll(inputsFrame.getMap());
					Main.yess.acidInputs.addAll(inputsFrame.getAcidInput());
					Main.yess.acidInputGal.addAll(inputsFrame.getAcidInputGal());
					Main.yess.inputTotalAcid = inputsFrame.getTotalAcid();
					System.out.println(Main.yess.stageInputs);
				});
			}
		};
	}

	private AbstractAction getPlotsPaneAction() {
		return new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (ch.getChannels().getOriginalName() == null || ch.getChannels().getOriginalName().size() == 0) {
					JOptionPane.showMessageDialog(null, "Select a Well/Stage and then configure the plots");
					return;
				}
				try {
					PlotsPane.SaveInformation.savePrimaryInfo(plotsPane.primaryTable, plotsPane.primaryLabels);
					PlotsPane.SaveInformation.saveChemInfo(plotsPane.chemsTable, plotsPane.chemLabels);
					PlotsPane.SaveInformation.saveColorDict("C:\\Scrape\\Chemical_Plots_Info.txt",
							"C:\\Scrape\\Primary_Plots_Info.txt");
					PlotsPane.SaveInformation.saveCasingFactor(plotsPane.casingFactor.getText());
					plotsPane.setChemChannels(
							PlotsPane.LoadInformation.loadChemInfo(plotsPane.chemsTable, plotsPane.chemLabels));
					PlotsPane.SaveInformation.saveSmoothChemChecks(plotsPane.getBoxListChem(), plotsPane.chemsTable, ch,
							"C:\\Scrape\\ScrapePython\\Plot\\smooth.txt");
					PlotsPane.SaveInformation.saveInternalChemChecks(plotsPane.getBoxListChem(),
							"C:\\Scrape\\smooth_chem.txt");

				} catch (IOException e1) {
					JOptionPane.showMessageDialog(mainFrame.this, "Error saving plot information: " + e1.getCause());
				}
				try {
					PlotsPane.SaveInformation.saveDictOnUpdate("C:\\Scrape\\Chemical_Plots_Info.txt",
							"C:\\Scrape\\Primary_Plots_Info.txt", ch.getChannels());
				} catch (IOException e1) {
					System.out.println("Chemical configuration files not found");
				}
				plotsPane.selectColorPane.dispose();
				plotsPane.setVisible(false);
			}
		};
	}

	private AbstractAction getEmailCompAction() {
		return new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				SwingWorker<Void, Void> worker = new SwingWorker<>() {
					public Void doInBackground() {
						try {
							emailComponents.saveChecks();
						} catch (IOException e2) {
							e2.printStackTrace();
							return null;
						}
						String[] comps = { "TR", "PJR", "Invoice", "CSV", "Operator Workbook" };
						int eInd = 0;
						ArrayList<String> usedComps = new ArrayList<>();
						ArrayList<Boolean> loadChecks = null;
						try {
							loadChecks = emailComponents.loadChecks();
						} catch (IOException e1) {
							e1.printStackTrace();
							return null;
						}
						for (Boolean b : loadChecks) {
							System.out.println(comps[eInd]);
							if (b) {
								usedComps.add(comps[eInd]);
							}
							eInd++;
						}
						new ArgumentsToText(usedComps, "C:\\Scrape\\ScrapePython\\emailcomponents.txt", "\n");
						return null;
					}
				};
				worker.execute();
			}
		};
	}

	private JMenuItem getDieselTrackingItem() {
		JMenuItem menuItem = getMenuItemShell(DIESEL_TRACKING, "Track Diesel Usage");
		menuItem.addActionListener(new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				executor.execute(() -> {
					if (menuItem.getText().equals(NOT_TRACKING_DIESEL_TEXT)) {
						menuItem.setText(TRACKING_DIESEL_TEXT);
					} else {
						menuItem.setText(NOT_TRACKING_DIESEL_TEXT);
					}
				});
			}
		});
		return menuItem;
	}

	private boolean isTrackingDiesel() {
		String text = ((JMenuItem) GUIUtilities
				.getComponentByName(GUIUtilities.getMenuInMenuBar(this.getJMenuBar(), "view"), DIESEL_TRACKING))
				.getText();
		return text.equals(TRACKING_DIESEL_TEXT);
	}

	private GUIUtilities.SimpleJFrame getCheckBoxFrame() throws IOException, ClassNotFoundException {
		CheckBoxPanel preFlushBox = new CheckBoxPanel("Add PreFlush", PREFLUSH_OPTION, new CheckBox(PREFLUSH_OPTION),
				300, 40);
		CheckBoxPanel shutdownBox = new CheckBoxPanel("Add Shutdowns", SHUTDOWN_OPTION, new CheckBox(SHUTDOWN_OPTION),
				300, 40);
		HashMap<String, Boolean> map = CheckBoxPanel.readOptionsFromFiles();
		if (map.isEmpty()) {
			map.put(PREFLUSH_OPTION, PREFLUSH_DEFAULT);
			map.put(SHUTDOWN_OPTION, SHUTDOWN_DEFAULT);
		} else {
			preFlushBox.setChecked(map.get(PREFLUSH_OPTION) != null ? map.get(PREFLUSH_OPTION) : true);
			shutdownBox.setChecked(map.get(SHUTDOWN_OPTION) != null ? map.get(SHUTDOWN_OPTION) : true);
		}
		GUIUtilities utilities = new GUIUtilities();
		GUIUtilities.SimpleJFrame simpleJFrame = utilities.new SimpleJFrame(
				new Rectangle(GUIUtilities.getCenterX(300), GUIUtilities.getCenterY(120), 300, 120), "Summary Options",
				Color.getHSBColor(-.85f, .1f, .85f), preFlushBox, shutdownBox);

		return simpleJFrame;
	}

	public final static String SUMMARY_OPTIONS_NAME = "summary_options";
	public final static String PREFLUSH_OPTION = "preflush";
	public final static String SHUTDOWN_OPTION = "shutdowns";

	public static HashMap<String, Boolean> getSavedOptions() throws IOException, ClassNotFoundException {
		HashMap<String, Boolean> optionsMap = CheckBoxPanel.readOptionsFromFiles();
		if (!optionsMap.containsKey(PREFLUSH_OPTION)) {
			optionsMap.put(PREFLUSH_OPTION, true);
		}
		if (!optionsMap.containsKey(SHUTDOWN_OPTION)) {
			optionsMap.put(SHUTDOWN_OPTION, true);
		}
		return optionsMap;
	}

	public final static String WELL_SUMMARY_MENU_ITEM = "well_summary_menu_item";
	public final static String STAGE_SUMMARY_TEXT = "Get Stage Summary";
	public final static String WELL_SUMMARY_TEXT = "Get Well Summary";

	private AbstractAction getWellSelectionAction(WellListSelection wellListSelection) {
		return new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				executor.execute(() -> {
					((JProgressBar) progressPanel.getComponent(0)).setString("LOADING");

					while (textCombo1.getItemCount() > 1) {
						textCombo1.removeItemAt(1);
					}
					wellListSelection.dispose();
					ArrayList<String> wellList = wellListSelection.getWells();
					for (String s : wellList) {
						textCombo1.addItem(s);
					}
					((JProgressBar) progressPanel.getComponent(0)).setString("");
					wellListSelection.dispose();
				});

			}
		};

	}

	public JMenuItem getWellSummaryMenuItem() {
		JMenuItem menuItem = getMenuItemShell(WELL_SUMMARY_MENU_ITEM, WELL_SUMMARY_TEXT);
		menuItem.setEnabled(false);
		menuItem.addActionListener(new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (wellSummary) {
					GUIUtilities.updateJMenuItemText(menuItem, WELL_SUMMARY_TEXT);
				} else {
					GUIUtilities.updateJMenuItemText(menuItem, STAGE_SUMMARY_TEXT);
				}
				wellSummary = !wellSummary;
			}
		});
		return menuItem;

	}

	public void populateTablesWithWellSummary(String wellName, EvaluatedDataObject evaluatedDataObject,
			JTable chemTable, JTable sigValsTable, JTable sandTable) {
		executor.execute(() -> {
			HashMap<String, LinkedHashMap<String, String>> wellSummary = getWellSummary(wellName, evaluatedDataObject);
			writeMapToTables(wellSummary, chemTable, sigValsTable, sandTable);
		});
	}

	public final static String SIG_VAL_SUMMARY = "sig_val_well_summary";
	public final static String CHEM_NAME = ChemSandFrame.CHEM_NAME;
	public final static String SAND_NAME = ChemSandFrame.SAND_NAME;

	private static LinkedHashMap<String, String> returnTotalsToString(LinkedHashMap<String, Float> totalsMap) {
		LinkedHashMap<String, String> newMap = new LinkedHashMap<>();
		for (String s : totalsMap.keySet()) {
			newMap.put(s, String.valueOf(totalsMap.get(s)));
		}
		return newMap;
	}

	private static LinkedHashMap<String, Float> addToTotalMap(Map<String, String> stageMap,
			LinkedHashMap<String, Float> totalMap) {
		for (String s : stageMap.keySet()) {
			if (totalMap.containsKey(s)) {
				totalMap.put(s, Float.valueOf(stageMap.get(s)) + totalMap.get(s));
				continue;
			}
			totalMap.put(s, Float.valueOf(stageMap.get(s)));
		}
		return totalMap;
	}

	private static HashSet<String> getConstantSigVals() {
		HashSet<String> staticSet = new HashSet<>();
		staticSet.add(DataNames.CREW);
		staticSet.add(DataNames.WELL_NAME);
		staticSet.add(DataNames.PAD_NAME);
		return staticSet;
	}

	public CompletableFuture<String> getBlenderAssetID(String crew, String start, String end) {
		ArrayList<String> assetIDChannel = new ArrayList<>(List.of(DataNames.BLENDER_ASSET_ID));
		CompletableFuture<String> idFuture = new CompletableFuture<>();
		executor.execute(() -> {
			DataRequest dataRequest = new DataRequest(crewRequest.getToken(), crewRequest.getSessionId(),
					DataRequest.getPostBody(crew, start, end, assetIDChannel), 10, crewRequest.getCookie());
			String[] blender = new String[]{""};
			try {
				dataRequest.makeRequest().entrySet().parallelStream()
						.filter(entry -> assetIDChannel.contains(entry.getKey()))
						.forEach(e -> blender[0] = e.getValue().size()>0?e.getValue().get(0):"0");
			} catch (Exception e) {
				idFuture.complete("");
			}
			idFuture.complete(blender[0]);
		});
		return idFuture;
	}
	
	public CompletableFuture<String> makeBlenderIDRequest(String crew,LastStartCloseComponent lastStartClose){
		String start = lastStartClose.getDateTime(LastStartCloseComponent.OPEN, 0);
		String end = lastStartClose.getDateTime(LastStartCloseComponent.OPEN, 15);
		return getBlenderAssetID(crew,start,end);
	}
	
	public static LinkedHashMap<String, ArrayList<String>> makeStaticRequest(CrewRequest crewRequest, String crew,
			String start, String end, ArrayList<String> channels) throws Exception {
		DataRequest dataRequest = new DataRequest(crewRequest.getToken(), crewRequest.getSessionId(),
				DataRequest.getPostBody(crew, start, end, channels), 2, crewRequest.getCookie());
		LinkedHashMap<String, ArrayList<String>> map = dataRequest.makeRequest();
		return map;
	}

	public static void writeMapToTables(HashMap<String, LinkedHashMap<String, String>> wellSummaryMap, JTable chemTable,
			JTable sigTable, JTable sandTable) {
		writeMapToTable(wellSummaryMap.get(SIG_VAL_SUMMARY), sigTable);
		writeMapToTable(wellSummaryMap.get(CHEM_NAME), chemTable);
		writeMapToTable(wellSummaryMap.get(SAND_NAME), sandTable);
	}

	public static void writeMapToTable(LinkedHashMap<String, String> map, JTable table) {
		int count = 0;
		for (Map.Entry<String, String> entry : map.entrySet()) {
			table.setValueAt(entry.getKey(), count, 0);
			table.setValueAt(entry.getValue(), count, 1);
			count++;
		}
	}

	public static void writeMapToTable(LinkedHashMap<String, String> map, JTable table, int keyColumn,
			int valueColumn) {
		int count = 0;
		for (Map.Entry<String, String> entry : map.entrySet()) {
			table.setValueAt(entry.getKey(), count, keyColumn);
			table.setValueAt(entry.getValue(), count, valueColumn);
			count++;
		}
	}

	private static HashMap<String, LinkedHashMap<String, String>> getChemSandTotals(
			HashMap<Integer, HashMap<String, LinkedHashMap<String, String>>> chemSandMaps) {
		LinkedHashMap<String, Float> chemTotals = new LinkedHashMap<>();
		LinkedHashMap<String, Float> sandTotals = new LinkedHashMap<>();

		for (HashMap<String, LinkedHashMap<String, String>> stageMap : chemSandMaps.values()) {
			addToTotalMap(stageMap.get(ChemSandFrame.CHEM_NAME), chemTotals);
			addToTotalMap(stageMap.get(ChemSandFrame.SAND_NAME), sandTotals);
		}
		HashMap<String, LinkedHashMap<String, String>> totalChemSandMap = new HashMap<>();
		totalChemSandMap.put(ChemSandFrame.CHEM_NAME, returnTotalsToString(chemTotals));
		totalChemSandMap.put(ChemSandFrame.SAND_NAME, returnTotalsToString(sandTotals));
		return totalChemSandMap;
	}

	public static ArrayList<String> getHistoricWellNames() {
		EvaluatedDataObject evaluatedDataObject = null;
		try {
			evaluatedDataObject = EvaluatedDataObject.getFromFile();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		if (evaluatedDataObject == null) {
			return null;
		}
		return FracCalculations.getArrayOfStringKeys(evaluatedDataObject.getSummaryMaps());

	}

	public final static String TOTAL_SUFFIX = DataNames.TOTAL_SUFFIX;
	public final static String AVERAGE_SUFFIX = DataNames.AVERAGE_SUFFIX;

	private static LinkedHashMap<String, String> getAverageOfMap(LinkedHashMap<String, Float> addedMap, Float numStages,
			String... maintainAdded) {
		LinkedHashMap<String, String> averageMap = new LinkedHashMap<>();
		addedMap.entrySet().forEach((Map.Entry<String, Float> entry) -> {
			if (containsString(maintainAdded, entry.getKey(), false)) {
				averageMap.put(entry.getKey() + TOTAL_SUFFIX, String.valueOf(entry.getValue()));
			}
			averageMap.put(entry.getKey() + AVERAGE_SUFFIX, String.valueOf((entry.getValue() / numStages)));
		});
		return averageMap;
	}

	private static boolean containsString(String[] strings, String string, boolean matchCase) {
		String inArray = "";
		if (!matchCase) {
			string = string.toLowerCase();
		}
		for (String s : strings) {
			if (!matchCase) {
				inArray = s.toLowerCase();
			} else {
				inArray = s;
			}
			if (inArray.equals(string)) {
				return true;
			}
		}
		return false;
	}

	private static Integer getMaxInSet(Set<Integer> set) {
		Integer[] max = { 0 };
		boolean[] first = { true };
		set.forEach((Integer i) -> {
			if (first[0]) {
				max[0] = i;
				first[0] = false;
				return;
			}
			max[0] = i > max[0] ? i : max[0];
		});
		return max[0];
	}

	private static Integer getMinInSet(Set<Integer> set) {
		Integer[] min = { 0 };
		boolean[] first = { true };
		set.forEach((Integer i) -> {
			if (first[0]) {
				min[0] = i;
				first[0] = false;
				return;
			}
			min[0] = i < min[0] ? i : min[0];
		});
		return min[0];
	}

	public static HashMap<String, LinkedHashMap<String, String>> getWellSummary(String wellName,
			EvaluatedDataObject evaluatedDataObject) {
		if (!evaluatedDataObject.getSigValsMaps().containsKey(wellName)) {
			return null;
		}
		HashMap<Integer, LinkedHashMap<String, String>> wellSigVals = evaluatedDataObject.getSigValsMaps()
				.get(wellName);
		LinkedHashMap<String, String> sigValsAverages = getSummaryOfMaps(wellSigVals);
		HashMap<String, LinkedHashMap<String, String>> wellSummaryMap = new HashMap<>();
		wellSummaryMap.put(SIG_VAL_SUMMARY, sigValsAverages);
		wellSummaryMap.putAll(getChemSandTotals(evaluatedDataObject.getChemSandMaps().get(wellName)));
		return wellSummaryMap;
	}

	public static HashMap<String, LinkedHashMap<String, String>> getFracSummaryTotals(
			EvaluatedDataObject evaluatedDataObject) {
		HashMap<String, LinkedHashMap<String, Float>> totalsMap = getBlankTotalsMap(ChemSandFrame.CHEM_NAME,
				ChemSandFrame.SAND_NAME);

		for (Map.Entry<String, HashMap<Integer, HashMap<String, LinkedHashMap<String, String>>>> entry : evaluatedDataObject
				.getChemSandMaps().entrySet()) {
			totalsMap = getChemSandTotals(entry.getValue(), totalsMap);
		}
		totalsMap = combineDuplicates(totalsMap);
		return formatTotalsMap(totalsMap);
	}

	/*
	 *
	 *
	 *
	 * REVISIT TO COMBINE DUPLICATES IN TOTALS MAPS THAT HAVE SLIGHTLY DIFFERENT
	 * NAMES
	 *
	 *
	 *
	 *
	 *
	 *
	 */
	public static String removeDupSandSyntax(String sandName) {
		Matcher matcher = Pattern.compile(ChemSandFrame.DUPLICATE_REGEX + "|regional|brown|(\\(((.+)?)\\))")
				.matcher(sandName.toLowerCase());
		while (matcher.find()) {
			sandName = sandName.substring(0, matcher.start()).trim();
			matcher.reset(sandName.toLowerCase());
		}
		return sandName;
	}

	public static HashMap<String, LinkedHashMap<String, Float>> combineDuplicates(
			HashMap<String, LinkedHashMap<String, Float>> totalsMap) {

		HashMap<String, LinkedHashMap<String, Float>> combinedMap = new HashMap<>();
		HashMap<String, HashMap<String, String>> nameMap = new HashMap<>();
		for (String s : totalsMap.keySet()) {
			combinedMap.put(s, new LinkedHashMap<>());
			nameMap.put(s, new HashMap<>());
			for (String ss : totalsMap.get(s).keySet()) {
				String normName = removeDupSandSyntax(ss);
				String reducedName = removeSpecialCharacters(normName, "\\s").toLowerCase();
				addToNameMap(nameMap.get(s), normName, reducedName);
				addToCombinedTotalsMap(combinedMap.get(s), reducedName, totalsMap.get(s).get(ss));
			}
		}
		return getNormNameCombinedMap(combinedMap, nameMap);
	}

	private static HashMap<String, LinkedHashMap<String, Float>> getNormNameCombinedMap(
			HashMap<String, LinkedHashMap<String, Float>> combinedMap,
			HashMap<String, HashMap<String, String>> nameMap) {
		HashMap<String, LinkedHashMap<String, Float>> normCombinedMap = new HashMap<>();
		for (Map.Entry<String, LinkedHashMap<String, Float>> entry : combinedMap.entrySet()) {
			String mapName = entry.getKey();
			normCombinedMap.put(mapName, new LinkedHashMap<>());
			for (String s : entry.getValue().keySet()) {
				normCombinedMap.get(mapName).put(nameMap.get(mapName).get(s), entry.getValue().get(s));
			}
		}
		return normCombinedMap;
	}

	private static void addToCombinedTotalsMap(LinkedHashMap<String, Float> combinedInnerMap, String reducedName,
			Float value) {

		if (combinedInnerMap.containsKey(reducedName)) {
			combinedInnerMap.put(reducedName, combinedInnerMap.get(reducedName) + value);
			return;
		}
		combinedInnerMap.put(reducedName, value);
	}

	private static void addToNameMap(HashMap<String, String> nameMap, String normName, String reducedName) {
		if (nameMap.containsKey(reducedName)) {
			return;
		}
		nameMap.put(reducedName, normName);
	}

	public static HashMap<String, LinkedHashMap<String, String>> formatTotalsMap(
			HashMap<String, LinkedHashMap<String, Float>> totalsMap) {
		HashMap<String, LinkedHashMap<String, String>> formattedMap = new HashMap<>();
		for (Map.Entry<String, LinkedHashMap<String, Float>> entry : totalsMap.entrySet()) {
			formattedMap.put(entry.getKey(), new LinkedHashMap<String, String>());
			for (String s : entry.getValue().keySet()) {
				formattedMap.get(entry.getKey()).put(s, String.format("%.0f", entry.getValue().get(s)));
			}
		}
		return formattedMap;

	}

	public static void writeMapToTable(JTable table, LinkedHashMap<String, String> map) {
		ClearTable.clearTable(table);
		int count = 0;
		for (String s : map.keySet()) {
			table.setValueAt(s, count, 0);
			table.setValueAt(map.get(s), count, 1);
			count++;
		}
	}

	public static HashMap<String, LinkedHashMap<String, Float>> getChemSandTotals(
			HashMap<Integer, HashMap<String, LinkedHashMap<String, String>>> wellMap,
			HashMap<String, LinkedHashMap<String, Float>> totalsMap) {
		for (Integer stage : wellMap.keySet()) {
			addToTotalsMap(wellMap.get(stage), totalsMap);
		}
		return totalsMap;
	}

	public static HashMap<String, LinkedHashMap<String, Float>> addToTotalsMap(
			HashMap<String, LinkedHashMap<String, String>> stageChemSandMap,
			HashMap<String, LinkedHashMap<String, Float>> totalsMap) {

		stageChemSandMap.keySet().forEach((String key) -> {
			if (key.equals(ChemSandFrame.CHEMICAL_UNITS_NAME) | key.equals(ChemSandFrame.SAND_DESIGN_NAME)) {
				return;
			}
			for (String name : stageChemSandMap.get(key).keySet()) {
				Float stageValue = Float.valueOf(isNumeric(stageChemSandMap.get(key).get(name))?stageChemSandMap.get(key).get(name):"0");
				if (totalsMap.containsKey(key) && totalsMap.get(key).containsKey(name)) {
					totalsMap.get(key).put(name, Float.valueOf(totalsMap.get(key).get(name)) + stageValue);
				} else if (totalsMap.containsKey(key)) {
					totalsMap.get(key).put(name, stageValue);
				}
			}
		});

		return totalsMap;
	}
	public static boolean isNumeric(String string) {
		Matcher matcher = Pattern.compile("(^(\\-?)((\\d*)?)(\\.(\\d*))?)").matcher(string);
		if(matcher.find()) {
			String found = matcher.group();
			return found.equals(string);
		}
		return false;
	}
	public static HashMap<String, LinkedHashMap<String, Float>> getBlankTotalsMap(String... keys) {
		HashMap<String, LinkedHashMap<String, Float>> blankMap = new HashMap<>();
		for (String s : keys) {
			blankMap.put(s, new LinkedHashMap<>());
		}
		return blankMap;
	}

	private static LinkedHashMap<String, String> getSummaryOfMaps(
			HashMap<Integer, LinkedHashMap<String, String>> wellSigVals) {
		LinkedHashMap<String, Float> addedMap = new LinkedHashMap<>();
		LinkedHashMap<String, String> nonNumericEntries = new LinkedHashMap<>();
		HashSet<String> staticSet = getConstantSigVals();
		Integer min = getMinInSet(wellSigVals.keySet());
		Integer max = getMaxInSet(wellSigVals.keySet());
		for (Map.Entry<Integer, LinkedHashMap<String, String>> stageMapEntry : wellSigVals.entrySet()) {

			stageMapEntry.getValue().entrySet().forEach((Map.Entry<String, String> entry) -> {
				if (entry.getValue() == null || entry.getValue().equals("")) {
					return;
				}
				if (staticSet.contains(entry.getKey())
						|| !entry.getValue().matches("^((\\-)?)(\\d*)(((\\.)(\\d*))?)")) {
					if (entry.getKey().contains("Start") & stageMapEntry.getKey() == min) {
						nonNumericEntries.put(entry.getKey(), entry.getValue());
					} else if (entry.getKey().contains("End") & stageMapEntry.getKey() == max) {
						nonNumericEntries.put(entry.getKey(), entry.getValue());
					}
					return;
				}
				Float value = Float.valueOf(entry.getValue());
				if (addedMap.containsKey(entry.getKey())) {
					addedMap.put(entry.getKey(), addedMap.get(entry.getKey()) + value);
					return;
				}
				addedMap.put(entry.getKey(), value);
			});
		}
		LinkedHashMap<String, String> averageSigVals = getAverageOfMap(addedMap, Float.valueOf(wellSigVals.size()),
				DataNames.PUMP_TIME, DataNames.CLEAN_GRAND, DataNames.SLURRY_GRAND);
		nonNumericEntries.putAll(averageSigVals);
		return nonNumericEntries;
	}

	private JMenuItem getSummaryOptionsItem() {
		JMenuItem menuItem = getMenuItemShell(SUMMARY_OPTIONS_NAME, "Summary Options");
		menuItem.addActionListener(new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					getCheckBoxFrame();
				} catch (ClassNotFoundException | IOException e1) {
					e1.printStackTrace();
					return;
				}
			}
		});

		return menuItem;
	}

	private JMenuItem getServerAddressItem() {
		JMenuItem menuItem = getMenuItemShell(ADDRESS_ITEM, "Update Address");
		menuItem.addActionListener(new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				executor.execute(() -> {
					String address = JOptionPane.showInputDialog("Input the updated server address");
					Matcher matcher = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)").matcher(address);
					if (matcher.find()) {
						try {
							updateServerAddAction(address);
						} catch (Exception e1) {
							e1.printStackTrace();
							return;
						}
					}
				});
			}
		});
		return menuItem;
	}

	private void updateServerAddAction(String address) throws IOException {
		executor.execute(() -> {
			try {
				writeStringToFile(SERVER_ADDRESS_FILENAME, address);
				sendDataFileToServer(address);
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
				return;
			}
			JOptionPane.showMessageDialog(Main.yess, "Data object sent to server");
		});
	}

	private static String readStringObjFromFile(String fileName, String defaultValue)
			throws IOException, ClassNotFoundException {
		File file = new File(fileName);
		if (!file.exists()) {
			return defaultValue;
		}
		ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
		String string = (String) objectInputStream.readObject();
		objectInputStream.close();
		return string;
	}

	private void writeStringToFile(String fileName, String string) throws IOException {
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(new File(fileName)));
		objectOutputStream.writeObject(string);
		objectOutputStream.close();
	}

	private JMenuItem getLoginItem() {
		JMenuItem menuItem = getMenuItemShell(LOGIN_ITEM, "Update Login");
		menuItem.addActionListener(new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				executor.execute(() -> {
					try {
						EncryptCredentials.updateUserCredentials(
								(Function<HashMap<String, String>, Boolean>) (HashMap<String, String> map) -> {
									RememberMe rememberMe = null;
									try {
										rememberMe = new RememberMe(map.get(EncryptCredentials.USERNAME),
												map.get(EncryptCredentials.PASSWORD));
									} catch (Exception e1) {
										try {
											RememberMe.deleteCookie();
										} catch (IOException e2) {
											System.out.println("Failed to delete cookie.scp");
											return false;
										}
									}
									if (rememberMe.getCookie().equals("AUTH=FAILED")) {
										return false;
									}
									return true;
								});
					} catch (InvalidKeyException | ClassNotFoundException | NoSuchAlgorithmException
							| NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException
							| IOException e1) {
						System.out.println("Unable to save credentials");
						e1.printStackTrace();
					}
				});
			}
		});
		return menuItem;
	}

	private JMenu getConfigureMenu() {
		JMenu menu = new JMenu();
		menu.setText("Config Transfer");
		menu.setVisible(true);
		menu.add(getUserDefinedItem());
		menu.add(getRedTRConfigItem());
		return menu;
	}

	final static String RED_TR_CONFIG_ITEM = "red_tr_config";

	private JMenuItem getRedTRConfigItem() {
		JMenuItem menuItem = getMenuItemShell(RED_TR_CONFIG_ITEM, "Configure Red TR");
		menuItem.addActionListener(new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				RedTRConfigureFrame configFrame = new RedTRConfigureFrame();
				configFrame.displayFrame();
			}
		});
		return menuItem;
	}

	public static void stopEditingTables(JTable... tables) {
		for (JTable table : tables) {
			stopEditingTable(table);
		}
	}

	public static void stopEditingTable(JTable table) {
		if (table.getEditingRow() > -1) {
			table.getCellEditor().stopCellEditing();
		}
	}

	private void removeInputs() {
		Main.yess.stageInputs.clear();
		Main.yess.acidInputs.clear();
		Main.yess.acidInputGal.clear();
	}

	private JMenuItem getJoshModeItem() {
		JMenuItem menuItem = new JMenuItem();
		menuItem.setText("Josh Mode");
		menuItem.setName("joshMode");
		menuItem.addActionListener(new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (menuItem.getText().equals("Josh Mode")) {
					Color color = JColorChooser.showDialog(Main.yess, "Select Color", Color.black);

					setTablesBackground(color, Color.LIGHT_GRAY);
					updatePanelImages(new ImageIcon(new ImageIcon("C:\\Scrape\\imageDark.png").getImage()
							.getScaledInstance((int) ((Toolkit.getDefaultToolkit().getScreenSize().getWidth())),
									(int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 240,
									java.awt.Image.SCALE_SMOOTH)));
					GUIUtilities.updateJMenuItemText(menuItem, "Normal Mode");
					Main.yess.repaint();
					return;
				}
				setTablesNormalMode();
				updatePanelImages(new ImageIcon(new ImageIcon("C:\\Scrape\\images.png").getImage().getScaledInstance(
						(int) ((Toolkit.getDefaultToolkit().getScreenSize().getWidth())),
						(int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 240,
						java.awt.Image.SCALE_SMOOTH)));
				GUIUtilities.updateJMenuItemText(menuItem, "Josh Mode");
				Main.yess.repaint();
			}
		});
		return menuItem;
	}

	private void setTablesNormalMode() {
		removeSelections(mTable, diagTable1, diagTable2, diagTable3, diagTable4);
		normalModeTableRenderer(mTable, diagTable1, diagTable2, diagTable3, diagTable4);
		normalModeCellEditor(mTable, diagTable1, diagTable2, diagTable3, diagTable4);
	}

	public boolean isMonitoringDGB() {
		return ((JMenuItem) GUIUtilities.getComponentByName(GUIUtilities.getMenuInMenuBar(getJMenuBar(), "view"),
				"dgbItem")).getText().equals("Stop Monitoring");
	}

	private static final String DGB = "Monitor DGB Pumps";

	private JMenuItem constructDGBItem() {
		JMenuItem item = getMenuItemShell("dgbItem", DGB);
		item.addActionListener(new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (item.getText().equals(DGB)) {
					GUIUtilities.updateJMenuItemText(item, "Stop Monitoring");
					return;
				}
				GUIUtilities.updateJMenuItemText(item, DGB);
			}
		});
		return item;
	}

	private void removeSelections(JTable... tables) {
		for (JTable table : tables) {
			table.clearSelection();
		}
	}

	private void normalModeTableRenderer(JTable... tables) {
		for (JTable table : tables) {
			for (int i = 0; i < table.getColumnCount(); i++) {
				table.getColumnModel().getColumn(i).setCellRenderer(new DefaultTableCellRenderer());
			}
		}
	}

	private void normalModeCellEditor(JTable... tables) {
		for (JTable table : tables) {
			for (int i = 0; i < table.getColumnCount(); i++) {
				table.getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(new JTextField()));
			}
		}
	}

	public static void saveImage(File file, JPanel panel, int width, int height) throws IOException {
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bufferedImage.createGraphics();
		panel.paintAll(g);
		g.dispose();
		ImageIO.write(bufferedImage, "png", file);
	}

	private void updatePanelImages(ImageIcon imageIcon) {
		SwingWorker<Void, ImageIcon> worker = new SwingWorker<>() {
			public Void doInBackground() {
				publish(imageIcon);
				return null;
			}

			public void process(List<ImageIcon> chunks) {
				sledge.setIcon(chunks.get(0));
				prop1.setIcon(chunks.get(0));
			}
		};
		worker.execute();
	}

	private void setTablesBackground(Color renderColor, Color editColor) {
		removeSelections(mTable, diagTable1, diagTable2, diagTable3, diagTable4);
		changeTableRendererColor(renderColor, mTable, diagTable1, diagTable2, diagTable3, diagTable4);
		changeCellEditorColor(editColor, mTable, diagTable1, diagTable2, diagTable3, diagTable4);
	}

	private void changeTableRendererColor(Color color, JTable... tables) {
		for (JTable table : tables) {
			for (int i = 0; i < table.getColumnCount(); i++) {
				table.getColumnModel().getColumn(i).setCellRenderer(getColoredCellRenderer(color));
			}
		}
	}

	private void changeCellEditorColor(Color color, JTable... tables) {
		for (JTable table : tables) {
			for (int i = 0; i < table.getColumnCount(); i++) {
				table.getColumnModel().getColumn(i).setCellEditor(getBlackCellEditor(color));
			}
		}
	}

	private DefaultTableCellRenderer getColoredCellRenderer(Color color) {
		DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
		cellRenderer.setBackground(color);
		cellRenderer.setForeground(Color.white);
		return cellRenderer;
	}

	public DefaultTableCellRenderer getColoredCellRenderer(Color backColor, Color foreColor) {
		DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
		cellRenderer.setBackground(backColor);
		cellRenderer.setForeground(foreColor);
		return cellRenderer;
	}

	private DefaultCellEditor getBlackCellEditor(Color color) {
		JTextField textField = new JTextField();
		textField.setBackground(color);
		textField.setForeground(Color.white);
		return new DefaultCellEditor(textField);
	}

	private JMenu getMaterialsMenu() {
		JMenu materials = new JMenu();
		materials.setText("Materials Tracking");
		materials.setVisible(true);
		materials.setName("materials");
		materials.add(constructMenuItem("Sand Tickets", getSandTicketsActionListener()));
		materials.add(constructMenuItem("View Past Job Tickets", new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				setSandTicketsObject(false);
			}
		}));
		materials.add(constructMenuItem("Silos", new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				new SandSilosFrame(sandTicketsObject);
			}
		}));
		materials.add(constructMenuItem("Stop Tracking", "track", new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {

				JMenuItem menuItem = (JMenuItem) GUIUtilities.getComponentByName(materials, "track");
				if (trackSand) {
					GUIUtilities.updateJMenuItemText(menuItem, "Track Sand");
				} else {
					GUIUtilities.updateJMenuItemText(menuItem, "Stop Tracking");
				}
				trackSand = !trackSand;
			}
		}));

		return materials;
	}

	private JMenuItem constructMenuItem(String text, ActionListener actionListener) {
		JMenuItem menuItem = new JMenuItem();
		menuItem.setText(text);
		menuItem.addActionListener(actionListener);
		menuItem.setVisible(true);
		return menuItem;
	}

	private JMenuItem constructMenuItem(String text, String name, ActionListener actionListener) {
		JMenuItem menuItem = new JMenuItem();
		menuItem.setText(text);
		menuItem.setName(name);
		menuItem.addActionListener(actionListener);
		menuItem.setVisible(true);
		return menuItem;
	}

	private ActionListener getSandTicketsActionListener() {
		AbstractAction action = new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				int count = 0;
				while (sandTicketsObject == null && count <= 2) {
					if (count == 2) {
						JOptionPane.showMessageDialog(Main.yess, "No Sand Ticket file was selected to display tickets");
						return;
					}
					setSandTicketsObject(true);
					count++;
				}
				new SandTicketsFrame(SandTicketsFrame.getTicketsFrameBounds(), sandTicketsObject);
			}
		};
		return action;
	}

	public Boolean setSandTicketsObject(Boolean check) {
		if (check) {
			return true;
		}
		String[] padSandTicketFileNames = SandTicketsObject.getObjectFileNames();
		if (padSandTicketFileNames == null) {
			return false;
		}

		String ticketFileName = (String) JOptionPane.showInputDialog(Main.yess,
				"Select the Sand Ticket file for the desired Pad", "Sand Ticket Files", JOptionPane.PLAIN_MESSAGE, null,
				padSandTicketFileNames, padSandTicketFileNames[0]);
		if (ticketFileName == null) {
			return false;
		}
		try {
			sandTicketsObject = SandTicketsObject.readFromFile(ticketFileName);
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public HashMap<String, Double> getSandMapWithoutTotal() {
		HashMap<String, Double> sandMap = new HashMap<>();
		for (String s : chemSandMap.get("sand").keySet()) {
			if (s.toUpperCase().contains("TOTAL")) {
				continue;
			}
			sandMap.put(s, Double.valueOf(chemSandMap.get("sand").get(s)));
		}
		return sandMap;
	}

	public void updateSandTicketsObject() {
		if (trackSand) {
			sandTicketsObject.pumpSand(getSandMapWithoutTotal(), wellName, getStage());
		}
	}

	private JMenuItem getMenuItemShell(String name, String text) {
		JMenuItem menuItem = new JMenuItem();
		menuItem.setText(text);
		menuItem.setName(name);
		return menuItem;
	}

	private JMenuItem getMenuItemShell(String name, String text, AbstractAction action) {
		JMenuItem menuItem = new JMenuItem();
		menuItem.setText(text);
		menuItem.setName(name);
		menuItem.addActionListener(action);
		return menuItem;
	}

	private JMenuItem constructPumpsStartItem() {
		JMenuItem menuItem = getMenuItemShell("pumpStart", "Get Pumps at Start");
		menuItem.addActionListener(new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				executor.execute(() -> {
					if (!checkDateTime(lastStartClose.getDateTimes().get("open"))) {
						JOptionPane.showMessageDialog(Main.yess, "Input a start time/date before pulling pumps");
						return;
					}
					if (!checkForWell()) {
						JOptionPane.showMessageDialog(Main.yess, "Select a well before pulling pumps");
						return;
					}
					HashMap<String, String> timesMap = getStartEnd(
							LocalDateTime.parse(lastStartClose.getDateTimes(5, 5).get("open").replace(" ", "T")), 10);
					if (isMonitoringDGB()) {
						monitoringDGBAction(timesMap, "Pumps Start");
						return;
					}
					notMonitoringDGBAction(timesMap, "Pumps Start");
				});
			}
		});
		return menuItem;
	}

	private void notMonitoringDGBAction(HashMap<String, String> timesMap, String startEnd) {
		LinkedHashMap<String, HashMap<String, String>> map = pumpInfoRequest(timesMap, "Trans Gear");
		int pumpsOnline = getPumpsOnline(map.get("Trans Gear"));
		diagTable2.setValueAt(pumpsOnline, sigMap.get(startEnd), 1);
	}

	private Double getDieselUsed(String start, String end) throws ClassNotFoundException, InterruptedException,
			IOException, DataFormatException, ExecutionException, TimeoutException {
		HashMap<String, HashMap<String, String>> map = getPumpsInfo(start, end,
				(Function<ArrayList<String>, String>) (ArrayList<String> array) -> {
					return String.valueOf(UserDefinedFrame.avg(array));
				}, "Fuel Rate");
		return calcDieselUsage(start, end, map);
	}

	private Double calcDieselUsage(String start, String end, HashMap<String, HashMap<String, String>> map) {
		Double pumpTime = getPumpTime(start, end);
		Double[] sumUsage = { 0.0 };
		map.forEach((String key, HashMap<String, String> value) -> {
			for (String s : value.keySet()) {
				sumUsage[0] += Double.valueOf(value.get(s));
			}
		});
		return sumUsage[0] * pumpTime;
	}

	private Double getPumpTime(String start, String end) {
		LocalDateTime startDateTime = LocalDateTime.parse(start.replace(" ", "T"));
		LocalDateTime endDateTime = LocalDateTime.parse(end.replace(" ", "T"));
		return Double.valueOf(Duration.between(startDateTime, endDateTime).getSeconds()) / 3600.0;
	}

	private void monitoringDGBAction(HashMap<String, String> timesMap, String startEnd) {
		LinkedHashMap<String, HashMap<String, String>> map = pumpInfoRequest(timesMap, "Trans Gear", "Dual Fuel Ratio");
		int pumpsOnline = getPumpsOnline(map.get("Trans Gear"));
		LinkedHashMap<String, String> orderedMap = orderMapAppend(map.get("Dual Fuel Ratio"), "Total Pumps Blending",
				String.valueOf(countGreaterThan(map.get("Dual Fuel Ratio"), 0.0)));
		diagTable2.setValueAt(pumpsOnline, sigMap.get(startEnd), 1);
		setTotalPumpsBlending(Integer.valueOf(orderedMap.get("Total Pumps Blending")));
		KeyValuePairsFrame<String> frame = new KeyValuePairsFrame<>(orderedMap, "Pumps Blending");
		frame.setIconImage(
				new ImageIcon("C:\\Scrape\\Scrape.png").getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
	}

	private void setTotalPumpsBlending(Integer totalPumpsBlending) {
		String currentValue = diagTable2.getValueAt(sigMap.get("Pumps Blending"), 1).toString();
		if (currentValue == null || currentValue.equals("") || Integer.valueOf(currentValue) < totalPumpsBlending) {
			diagTable2.setValueAt(totalPumpsBlending, sigMap.get("Pumps Blending"), 1);
		}
	}

	private LinkedHashMap<String, String> orderMapAppend(Map<String, String> map, String key, String value) {
		LinkedHashMap<String, String> orderedMap = new LinkedHashMap<>();
		for (String s : map.keySet()) {
			orderedMap.put(s, map.get(s));
		}
		orderedMap.put(key, value);
		return orderedMap;
	}

	private int countGreaterThan(Map<String, String> map, Double greaterThan) {
		int count = 0;
		for (String s : map.keySet()) {
			if (Double.valueOf(map.get(s)) > greaterThan) {
				count++;
			}
		}
		return count;
	}

	private HashMap<String, String> getStartEnd(LocalDateTime dateTime, int plusMinutes) {
		HashMap<String, String> map = new HashMap<>();
		if (plusMinutes > 0) {
			map.put("start", dateTime.format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm")));
			map.put("end", dateTime.plusMinutes(plusMinutes).format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm")));
			return map;
		}
		map.put("end", dateTime.format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm")));
		map.put("start", dateTime.plusMinutes(plusMinutes).format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm")));
		return map;
	}

	private LinkedHashMap<String, HashMap<String, String>> pumpInfoRequest(HashMap<String, String> timesMap,
			String... channels) {
		LinkedHashMap<String, HashMap<String, String>> map = null;
		try {
			map = getPumpsInfo(timesMap.get("start"), timesMap.get("end"), channels);
		} catch (ClassNotFoundException | InterruptedException | IOException | DataFormatException e) {

			e.printStackTrace();
		} catch (ExecutionException e) {

			e.printStackTrace();
		} catch (TimeoutException e) {

			e.printStackTrace();
		}
		return map;
	}

	public int pumpsOnline(HashMap<String, String> timesMap) {
		diagTable2.setValueAt("", sigMap.get("Pumps Start"), 1);
		int pumpsOnline = 0;
		try {
			pumpsOnline = getPumpsOnline(timesMap.get("start"), timesMap.get("end"));
		} catch (IOException | InterruptedException | DataFormatException | ClassNotFoundException e1) {
			e1.printStackTrace();
			return 0;
		}
		return pumpsOnline;
	}

	private JMenuItem constructPumpsEndItem() {
		JMenuItem menuItem = getMenuItemShell("pumpEnd", "Get Pumps at End");
		menuItem.addActionListener(new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				executor.execute(() -> {
					if (!checkDateTime(lastStartClose.getDateTimes().get("close"))) {
						JOptionPane.showMessageDialog(Main.yess, "Input a end time/date before pulling pumps");
						return;
					}
					if (!checkForWell()) {
						JOptionPane.showMessageDialog(Main.yess, "Select a well before pulling pumps");
						return;
					}
					HashMap<String, String> timesMap = getStartEnd(
							LocalDateTime.parse(lastStartClose.getDateTimes(10, -10).get("close").replace(" ", "T")),
							-10);
					if (isMonitoringDGB()) {
						monitoringDGBAction(timesMap, "Pumps End");
						return;
					}
					notMonitoringDGBAction(timesMap, "Pumps End");
				});
			}
		});
		return menuItem;
	}

	private JMenu constructPumpMenu() {
		JMenu menu = new JMenu();
		menu.setText("Pumps");
		menu.add(constructPumpsStartItem());
		menu.add(constructPumpsEndItem());
		menu.add(constructDGBItem());
		menu.add(getDieselTrackingItem());
		return menu;
	}

	private boolean checkDateTime(String dateTime) {
		if (dateTime == null) {
			return false;
		}
		Matcher matcher = Pattern.compile("\\d{4}-\\d{2}-\\d{2}\\s\\d{2}\\:\\d{2}").matcher(dateTime);
		if (matcher.find()) {
			return true;
		}
		return false;
	}

	private boolean checkForWell() {
		if (textCombo1.getSelectedItem().toString().equals("-")) {
			return false;
		}
		return true;
	}

	private JMenuItem constructTeloItem() {
		JMenuItem menuItem = new JMenuItem();
		menuItem.setName("Telo");
		menuItem.setText("Open Plots");
		menuItem.addActionListener(new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Runtime.getRuntime().exec(new String[] { "cmd", "/c", "C:\\Scrape\\ScrapePython\\Telo\\Telo.exe" });
				} catch (IOException e1) {
					e1.printStackTrace();
					System.out.println("NONOONO");
				}
			}
		});
		return menuItem;
	}

	private JMenuItem constructSaveTreatmentItem() {
		JMenuItem menuItem = new JMenuItem();
		menuItem.setName("saveTreatment");
		menuItem.setText("Save Treatment");
		menuItem.addActionListener(new SaveTreatmentAction());
		return menuItem;
	}

	private Integer getStage() {
		return Integer.valueOf(textField1.getSelectedItem().toString());
	}

	private JMenuItem constructCombineHistoricItem() {
		JMenuItem menuItem = new JMenuItem();
		menuItem.setName("combine");
		menuItem.setText("Get Server Data");
		menuItem.addActionListener(new ServerEvaluatedData());
		return menuItem;
	}

	private JMenuItem constructHistoricItem(JMenuItem summaryItem, JMenu transferMenu) {
		JMenuItem menuItem = new JMenuItem();
		menuItem.setName("historic");
		menuItem.setText("View Historic Data");
		menuItem.addActionListener(new HistoricListener(menuItem, summaryItem, transferMenu));
		return menuItem;
	}

	private final static String SUMMARY_TEMPLATE_NAME = "summary_template";
	private final static String SUMMARY_TEMPLATE_TEXT = "Configure Summary Transfer";

	private JMenuItem constructSummaryTemplateItem() {
		JMenuItem menuItem = getMenuItemShell(SUMMARY_TEMPLATE_NAME, SUMMARY_TEMPLATE_TEXT);
		menuItem.addActionListener(new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				executor.execute(() -> {
					try {
						new OperatorTemplateStageSummary(getTemplateSummaryRect());
					} catch (IOException | ClassNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return;
					}
				});
			}
		});
		return menuItem;
	}

	private static Rectangle getTemplateSummaryRect() {
		return new Rectangle(Toolkit.getDefaultToolkit().getScreenSize().width / 3, 0,
				Toolkit.getDefaultToolkit().getScreenSize().width / 2,
				Toolkit.getDefaultToolkit().getScreenSize().height - 40);
	}

	private final static String TRANSFER_ITEM_NAME = "well_summary_transfer";
	private final static String TRANSFER_ITEM_TEXT = "Transfer Well Summary";

	private JMenuItem constructTransferMenuItem() {
		JMenuItem menuItem = getMenuItemShell(TRANSFER_ITEM_NAME, TRANSFER_ITEM_TEXT);
		menuItem.addActionListener(new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				executor.execute(() -> {

					try {
						transferWellSummary();
					} catch (Exception e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(Main.yess, "Failed To Transfer");
						return;
					}
				});
			}
		});
		return menuItem;
	}

	private void transferWellSummary()
			throws Exception {
		try(TransferTemplate transferTemplate = TransferTemplate
				.readFromFile(TransferTemplate.getWellSummaryTemplatePath())){
	
			if (transferTemplate == null) {
				JOptionPane.showMessageDialog(Main.yess,
						"Create a transfer template for well summaries before transferring");
				return;
			}
			Map<String, Map<String, String>> dataMap = getTableMaps(diagTable1, diagTable3, diagTable2);
			dataMap.remove(SIG_VALS_NAME);
			XSSFWorkbook workbook = transferTemplate.getWorkbook();
			saveWorkbookCopy(workbook,TransferTemplate.getCopyWorkbookPath(new File(transferTemplate.getWorkbookPath())));
			transferTemplate.transferValuesFromTemplate(transferTemplate.getSheet(workbook), dataMap.get(SIG_VALS_NAME),
					dataMap);
			transferTemplate.saveWorkbook(workbook, new File(transferTemplate.getFilePath()));
			JOptionPane.showMessageDialog(null, "Summary Transferred");
		}
	}
	private static void saveWorkbookCopy(XSSFWorkbook workbook, String filePath) {
		try(FileOutputStream fileOutputStream = new FileOutputStream(new File(filePath))){
			workbook.write(fileOutputStream);
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	public final static String SIG_VALS_NAME = "sig_vals";

	private Map<String, Map<String, String>> getTableMaps(JTable chemTable, JTable sandTable, JTable sigTable) {
		Map<String, String> chemMap = SheetData.getSigTableData(chemTable);
		Map<String, String> sandMap = SheetData.getSigTableData(sandTable);
		Map<String, String> sigValsMap = SheetData.getSigTableData(sigTable);
		Map<String, Map<String, String>> dataMap = new HashMap<>();
		dataMap.put(TransferTemplate.CHEM_NAME, chemMap);
		dataMap.put(TransferTemplate.SAND_NAME, sandMap);
		dataMap.put(SIG_VALS_NAME, sigValsMap);
		return dataMap;
	}

	private final static String SUMMARY_TRANSFER_MENU = "summary_transfer_menu";
	private final static String SUMMARY_TRANSFER_TEXT = "Transfer Options";

	private JMenu constructSummaryTransferMenu() {
		JMenu menu = new JMenu();
		menu.setName(SUMMARY_TRANSFER_MENU);
		menu.setText(SUMMARY_TRANSFER_TEXT);
		menu.add(constructSummaryTemplateItem());
		menu.add(constructTransferMenuItem());
		menu.add(constructFilterHistoricMenuItem());
		menu.setEnabled(false);
		return menu;
	}

	private final static String FILTERED_HISTORIC_TEXT = "Filter Historic Data";
	private final static String FILTERED_HISTORIC_NAME = "filtered_historic_data";

	private JMenuItem constructFilterHistoricMenuItem() {
		JMenuItem menuItem = getMenuItemShell(FILTERED_HISTORIC_NAME, FILTERED_HISTORIC_TEXT);
		menuItem.addActionListener(new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				executor.execute(() -> {
					FracSummaryFrame fracSummaryFrame = new FracSummaryFrame(getFracSummmaryFrameRect(),
							FracCalculations.getArrayOfStringKeys(crewRequest.getCrewMap()));
					FracSummary fracSummary = null;
					try {
						fracSummary = new FracSummary(fracSummaryFrame.getConfigMap());
					} catch (Exception e1) {
						return;
					}
					HashMap<String, LinkedHashMap<String, String>> totalsMap = getFracSummaryTotals(
							fracSummary.getFilteredDataObject());

					writeMapToTable(diagTable1, totalsMap.get(ChemSandFrame.CHEM_NAME));
					writeMapToTable(diagTable3, totalsMap.get(ChemSandFrame.SAND_NAME));
				});
			}
		});

		return menuItem;
	}

	public void transferFilterHistoricData() throws IOException, ClassNotFoundException {
		TransferTemplate transferTemplate = TransferTemplate
				.readFromFile(TransferTemplate.getWellSummaryTemplatePath());
		if (transferTemplate == null) {
			executor.execute(() -> {
				JOptionPane.showMessageDialog(mTable, "Configure a Transfer Template before trying to export the data");
			});
			return;
		}
		Map<String, Map<String, String>> chemSandMap = getChemSandMap(diagTable1, diagTable3);
		XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(new File(transferTemplate.getFilePath())));
		XSSFSheet sheet = workbook.getSheet(transferTemplate.getSheetName());
		transferTemplate.transferValuesFromTemplate(sheet, null, chemSandMap);
		workbook.close();
	}

	public final static Map<String, Map<String, String>> getChemSandMap(JTable chemTable, JTable sandTable) {
		Map<String, String> chemTotals = SheetData.getSigTableData(chemTable);
		Map<String, String> sandTotals = SheetData.getSigTableData(sandTable);
		Map<String, Map<String, String>> chemSandMap = new HashMap<>();
		chemSandMap.put(ChemSandFrame.CHEM_NAME, chemTotals);
		chemSandMap.put(ChemSandFrame.SAND_NAME, sandTotals);
		return chemSandMap;
	}

	public final static String HISTORIC_MENU = "historic_menu";

	private Rectangle getFracSummmaryFrameRect() {
		return new Rectangle(GUIUtilities.getCenterX(300), GUIUtilities.getCenterY(175), 300, 175);
	}

	private JMenu constructHistoricMenu() {
		JMenu menu = new JMenu();
		menu.setName(HISTORIC_MENU);
		menu.setText("Historic Data Options");
		JMenuItem summaryItem = getWellSummaryMenuItem();
		JMenu transferMenu = constructSummaryTransferMenu();
		menu.add(constructHistoricItem(summaryItem, transferMenu));
		menu.add(summaryItem);
		menu.add(transferMenu);
		return menu;
	}

	public void setEvaluatedDataObject(EvaluatedDataObject evaluatedDataObject) {
		this.evaluatedDataObject = evaluatedDataObject;
	}

	public EvaluatedDataObject getEvaluatedDataObject() {
		return this.evaluatedDataObject;

	}

	// Listener class for toggling between new/historic data
	private class HistoricListener extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		JMenuItem menuItem;
		JMenuItem summaryItem;
		JMenu transferMenu;

		HistoricListener(JMenuItem menuItem, JMenuItem summaryItem, JMenu transferMenu) {
			this.menuItem = menuItem;
			this.summaryItem = summaryItem;
			this.transferMenu = transferMenu;
		}

		boolean getHistoric() {
			return menuItem.getText().equals("View Historic Data");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			executor.execute(() -> {
				if (getHistoric()) {
					try {
						setEvaluatedDataObject(EvaluatedDataObject.getFromFile());
					} catch (IOException | ClassNotFoundException e1) {
						System.out.println("Exception caught DeSerializing Data");
						e1.printStackTrace();
						return;
					}
				} else {
					setEvaluatedDataObject(null);
					System.gc();
				}
				SwingWorker<Void, String> historicWorker = new SwingWorker<>() {
					public Void doInBackground() {
						if (menuItem.getText().equals("View Historic Data")) {
							// Setting the menuItem text
							publish("Evaluate New Data");
							summaryItem.setEnabled(true);
							transferMenu.setEnabled(true);
							// Boolean for toggling on and off the run button
							historic = true;
						} else {
							// Setting the menuItem text
							publish("View Historic Data");
							summaryItem.setEnabled(false);
							transferMenu.setEnabled(false);
							// Boolean for toggling on and off the run button
							historic = false;
						}
						return null;
					}

					public void process(List<String> chunks) {

						for (String s : chunks) {
							System.out.println(s);
							menuItem.setText(s);
						}
					}

					public void done() {

						if (menuItem.getText().equals("Evaluate New Data")) {
							// Updating the wells for historic data and adding the
							// appropriate actionListeners
							updateWellsHistoric(evaluatedDataObject);
						} else {
							// Updating the wells for new data and adding the
							// appropriate actionListeners
							removeListenersForNew();
							clearWellList();
							addWells();
							addListenersForNew();
						}
					}
				};
				historicWorker.execute();
			});

		}
	}

	private void removeListenersForNew() {
		textCombo1.removeActionListener(textCombo1.getActionListeners()[0]);
		textField1.removeActionListener(textField1.getActionListeners()[0]);
	}

	private void addListenersForNew() {
		textCombo1.addActionListener(new update());
		textField1.addActionListener(new update1());
	}

	private void updateWellsHistoric(EvaluatedDataObject evaluatedDataObject) {
		textCombo1.setSelectedItem("-");
		textField1.setSelectedItem("<Stage Number>");
		textCombo1.removeActionListener(textCombo1.getActionListeners()[0]);
		textField1.removeActionListener(textField1.getActionListeners()[0]);
		SwingWorker<Void, String> wellStageWorker = new SwingWorker<>() {
			public Void doInBackground() {

				clearWellList();
				for (String s : evaluatedDataObject.getSummaryMaps().keySet()) {
					publish(s);
				}
				return null;
			}

			public void process(List<String> chunks) {
				for (String s : chunks) {
					textCombo1.addItem(s);
				}
			}

			public void done() {
				textCombo1.addActionListener(new UpdateHistoric(evaluatedDataObject));
				textField1.addActionListener(new ReloadHistoric(evaluatedDataObject));
				textCombo1.setEnabled(true);
			}
		};
		wellStageWorker.execute();
	}

	public void updateStageArguments() {
		executor.execute(() -> {
			String wellName = getWellName();
			// String coState = well.getCounty(wellName) +","+well.getState(wellName);
			String cmEmails = new String();
			for (String a : emailFrame.getEmails(1)) {
				cmEmails = cmEmails + a + ";";
			}
			cmEmails.subSequence(0, cmEmails.length() - 1);
			try {
				new StageArguments(ReadDirectory.readDirect(), wellName.toUpperCase(),
						String.valueOf(textField1.getSelectedItem()), jobLogWells.getCounty(getWellName()),
						jobLogWells.getFormationMap().get(jobLogWells.getSelectedWellMap().get("formationId")),
						emailFrame.getMyEmail(), cmEmails, wellDrivePane.getUsername(), wellDrivePane.getPassword());
			} catch (HeadlessException | IOException e1) {
				try {
					new TextLog("Company Man Emails - line 633");
				} catch (IOException e2) {
				}
			}
		});
	}

	private class UpdateHistoric extends AbstractAction {

		private static final long serialVersionUID = 1L;
		EvaluatedDataObject evaluatedDataObject;

		UpdateHistoric(EvaluatedDataObject evaluatedDataObject) {
			this.evaluatedDataObject = evaluatedDataObject;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (textCombo1.getSelectedItem().toString().equals("-")) {
				return;
			}
			String wellName = getWellName();
			removeStages();
			setOperatorDetailsHistoric();
			if (wellSummary) {
				ClearTable.clearTables(diagTable1, diagTable2, diagTable3, diagTable4, mTable);
				populateTablesWithWellSummary(wellName, getEvaluatedDataObject(), diagTable1, diagTable2, diagTable3);
				return;
			}
			// UPDATING WELL LIST
			SwingWorker<Void, String> worker = new SwingWorker<>() {
				public Void doInBackground() {
					ArrayList<Integer> stagesArray = getOrderedArrayFromKeys(
							evaluatedDataObject.getSummaryMaps().get(wellName));
					for (Integer i : stagesArray) {
						publish(String.valueOf(i));
					}

					return null;
				}

				public void process(List<String> chunks) {
					for (String s : chunks) {
						textField1.addItem(s);
					}
				}

			};
			worker.execute();
		}

		private ArrayList<Integer> getOrderedArrayFromKeys(Map<Integer, ?> map) {
			ArrayList<Integer> array = new ArrayList<>();
			for (Integer i : map.keySet()) {
				array.add(i);
			}
			Collections.sort(array);
			return array;
		}
	}

	private class ReloadHistoric extends AbstractAction {

		private static final long serialVersionUID = 1L;

		EvaluatedDataObject evaluatedDataObject;

		ReloadHistoric(EvaluatedDataObject evaluatedDataObject) {
			this.evaluatedDataObject = evaluatedDataObject;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String selectedStage = textField1.getSelectedItem().toString();
			if (selectedStage.equals("<Stage Number>")) {
				return;
			}
			ClearTable.clearTables(mTable, diagTable1, diagTable2, diagTable3, diagTable4);
			evaluatedDataObject.retrieveStage(mTable, diagTable1, diagTable2, diagTable3, diagTable4, getWellName(),
					Integer.valueOf(selectedStage));
			updateStageArguments();
		}
	}

	private JMenuItem constructChemSandMenu() {
		JMenuItem menu = new JMenuItem();
		menu.setText("Chem/Sand Usage");
		menu.addActionListener(new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {

				Executors.newSingleThreadExecutor().execute(() -> {
					ChemSandFrame chemSandFrame = null;
					if (chemSandMap == null || chemSandMap.isEmpty()) {
						chemSandMap = new LinkedHashMap<>();
						try {
							chemSandFrame = new ChemSandFrame(getChemSandRect());
							chemSandMap.putAll(chemSandFrame.getChemSandMap());
							System.out.println(chemSandFrame.getChemSandMap());
						} catch (IOException | InterruptedException e1) {
							System.out.println("Exception caught ChemSandFrame");
						}
					} else {
						chemSandFrame = new ChemSandFrame(getChemSandRect(), chemSandMap);
						try {
							chemSandMap.putAll(chemSandFrame.getChemSandMap());
							System.out.println(chemSandFrame.getChemSandMap());
						} catch (InterruptedException e2) {
							System.out.println("Exception caught ChemSandFrame");
						}
					}

				});
			}
		});
		return menu;
	}

	private Double getMultiSandRatio(String inputVolumes) {
		double[] volumes = new double[2];
		Matcher matcher = Pattern.compile("[\\d\\.]+").matcher(inputVolumes);
		int count = 0;
		while (matcher.find() && count < 2) {
			volumes[count] = Double.valueOf(matcher.group());
			count++;
		}
		if (count < 2) {
			return 1.0;
		}
		return volumes[1] / volumes[0];
	}

	public Double getMultiSandTotal(String inputVolumes) {
		double total = 0.0;
		Matcher matcher = Pattern.compile("[\\d\\.]+").matcher(inputVolumes);
		while (matcher.find()) {
			total += Double.valueOf(matcher.group());
		}
		return total;
	}

	private void checkForMultiSand() {
		for (String s : chemSandMap.get("sand").keySet()) {
			Matcher matcher = Pattern.compile("(.*?)\\&(.+)").matcher(s);
			if (matcher.find()) {
				String found = matcher.group();
				Double ratio = getMultiSandRatio(chemSandMap.get("sand").get(s));
				redefineUserDefined(UserDefinedFrame.MULTI_SAND + "\\(\\[" + found + "\\]\\)",
						"[" + DataNames.SUMMARY_SAND_VOL + "]*" + String.valueOf(ratio) + "->"
								+ DataNames.SUMMARY_SAND_TYPE + "=" + s + "->Total="
								+ getSecondaryTypeTotal(chemSandMap.get("sand").get(s)),
						true);
			}
		}
	}

	private String getSecondaryTypeTotal(String multiSandString) {
		Matcher matcher = Pattern.compile("\\d+").matcher(multiSandString);
		String secTypeTotal = "";
		int count = 0;
		while (matcher.find() & count < 2) {
			secTypeTotal = matcher.group();
			count++;
		}
		return secTypeTotal;
	}

	private void restoreCachedDefinitions() {
		if (userDefinedMap == null) {
			return;
		}

		for (String s : userDefinedMap.keySet()) {
			if (userDefinedMap.get(s).containsKey(UserDefinedFrame.CACHE_DEFINITION)) {
				userDefinedMap.get(s).put(UserDefinedFrame.DEFINITION,
						userDefinedMap.get(s).get(UserDefinedFrame.CACHE_DEFINITION));
			}
		}
	}

	private void redefineUserDefined(String defRegex, String newDef, boolean all) {
		for (String s : userDefinedMap.keySet()) {
			Matcher matcher = Pattern.compile(defRegex).matcher(userDefinedMap.get(s).get(UserDefinedFrame.DEFINITION));
			if (matcher.find()) {
				userDefinedMap.get(s).put(UserDefinedFrame.CACHE_DEFINITION,
						userDefinedMap.get(s).get(UserDefinedFrame.DEFINITION));
				userDefinedMap.get(s).put(UserDefinedFrame.DEFINITION, newDef);
				if (!all) {
					return;
				}
			}
		}
	}

	private Rectangle getChemSandRect() {
		int x = Toolkit.getDefaultToolkit().getScreenSize().width / 3;
		int y = 0;
		int width = Toolkit.getDefaultToolkit().getScreenSize().width / 3;
		int height = Toolkit.getDefaultToolkit().getScreenSize().height * 3 / 5;
		return new Rectangle(x, y, width, height);
	}

	private InfoPanel constructInfoPanel() throws IOException {
		crewLabel = new JLabel();
		crewLabel.setText(ChannelPane.getUserCrew());
		// crewLabel.setText("Test");
		crewLabel.setBackground(Color.getHSBColor(0f, 0f, .7f));
		crewLabel.setOpaque(true);
		InfoPanel infoPanel = new InfoPanel(getInfoPanelRectangle(), crewLabel);
		infoPanel.setBounds(getInfoPanelRectangle());
		return infoPanel;
	}

	private Rectangle getInfoPanelRectangle() {
		int width = Toolkit.getDefaultToolkit().getScreenSize().width;
		int y = sPane.getHeight() + sPane.getY();
		int height = getToolkit().getScreenSize().height - getToolkit().getScreenInsets(GraphicsEnvironment
				.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration()).bottom - y;
		return new Rectangle(0, y, width, height);
	}

	private void setPerfsMap(ArrayList<String> activeWells, String token) throws InterruptedException {
		Semaphore semaphore = new Semaphore(0);
		int numPermits = activeWells.size();
		ExecutorService executor = Executors.newCachedThreadPool();
		for (String s : activeWells) {
			executor.execute(() -> {
				WellsRequest.setActivePerfsMap(WellsRequest.makePerfsRequest(token, jobLogWells.getIdFromWell(s)),
						jobLogWells.getIdFromWell(s), jobLogWells);
				semaphore.release();
			});
		}
		semaphore.acquire(numPermits);
		executor.shutdown();
	}

	private void petroIQInformation(Semaphore wellSemaphore) {
		if (!isConnectedToInternet()) {
			JOptionPane.showMessageDialog(this, "Check your internet connection");
			return;
		}
		PipedInputStream pipedInputStream = new PipedInputStream();
		PipedOutputStream pipedOutputStream = null;
		try {
			pipedOutputStream = new PipedOutputStream();
			pipedOutputStream.connect(pipedInputStream);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ArrayList<PipedOutputStream> array = new ArrayList<>();
		array.add(pipedOutputStream);
		DataInputStream dataInputStream = new DataInputStream(pipedInputStream);
		executor.execute(() -> {
			try {
				acquirePetroIQInfo(array.get(0), wellSemaphore);
			} catch (IOException | InvalidKeyException | ClassNotFoundException | NoSuchAlgorithmException
					| NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException
					| ShortBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

		executor.execute(() -> {
			int i = 0;
			try {
				while ((i = dataInputStream.readInt()) > -1) {
					byte[] bytes = new byte[i];
					dataInputStream.read(bytes);
					System.out.println("Publish - " + WellsRequest.getStringFromBytes(bytes));
					((JProgressBar) progressPanel.getComponent(0)).setString(WellsRequest.getStringFromBytes(bytes));
				}
				((JProgressBar) progressPanel.getComponent(0)).setString("");

			} catch (IOException e) {

				e.printStackTrace();
			}
		});
	}

	public static JobLogWells retrievePetroIQInfo()
			throws IOException, InvalidKeyException, ClassNotFoundException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ShortBufferException {

		ExecutorService executor = Executors.newCachedThreadPool();
		JobLogWells[] jobLogWells = new JobLogWells[1];
		LoginRequest login = null;
		HashMap<String, String> creds = EncryptCredentials.getUserCredentials(LoginRequest.PETRO_IQ_CRED_PATH,
				"PetroIQ Login");
		try {
			login = new LoginRequest(creds.get(UserNamePassword.USERNAME), creds.get(UserNamePassword.PASSWORD));
		} catch (IOException | InterruptedException e) {
			System.out.println("Exception caught logging in to PETROIQ");
		}
		String token = login.getToken();
		Semaphore semaphore = new Semaphore(0);

		executor.execute(() -> {
			new joblog.CrewRequest(token);
		});
		executor.execute(() -> {
			WellsRequest.getCustomersMap(WellsRequest.customersRequest(token));
		});
		executor.execute(() -> {
			jobLogWells[0] = null;
			try {
				jobLogWells[0] = WellsRequest.setWellMap(token);
			} catch (IOException e) {
				e.printStackTrace();
				semaphore.release();
			}
			semaphore.release();
		});
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			System.out.println("NONONONONON");
		}
		jobLogWells[0].setToken(token);
		executor.execute(() -> {
			jobLogWells[0].setCountiesMap();
		});
		executor.execute(() -> {
			jobLogWells[0].setFormationMap(token);
		});

		return jobLogWells[0];
	}

	public static void acquirePetroIQInfo(JobLogWells jobLogWells) throws Exception {
		ExecutorService executor = Executors.newCachedThreadPool();
		HashMap<String, String> creds = EncryptCredentials.getUserCredentials(LoginRequest.PETRO_IQ_CRED_PATH,
				"PetroIQ Login");
		LoginRequest login = null;
		try {
			login = new LoginRequest(creds.get(UserNamePassword.USERNAME), creds.get(UserNamePassword.PASSWORD));
		} catch (IOException | InterruptedException e) {
			System.out.println("Exception caught logging in to PETROIQ");
		}
		String token = login.getToken();

		Semaphore semaphore = new Semaphore(0);

		executor.execute(() -> {
			new joblog.CrewRequest(token);
		});
		executor.execute(() -> {
			WellsRequest.getCustomersMap(WellsRequest.customersRequest(token));
		});
		try {
			jobLogWells = WellsRequest.setWellMap(token);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			System.out.println("NONONONONON");
		}
		jobLogWells.setToken(token);
		jobLogWells.setCountiesMap();
		jobLogWells.setFormationMap(token);
	}

	private void acquirePetroIQInfo(PipedOutputStream pipedOutputStream)
			throws IOException, InvalidKeyException, ClassNotFoundException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ShortBufferException {
		DataOutputStream dataOutputStream = new DataOutputStream(pipedOutputStream);
		ExecutorService executor = Executors.newCachedThreadPool();
		dataOutputStream.writeInt("0%".getBytes().length);
		dataOutputStream.write("0%".getBytes());
		LoginRequest login = null;
		HashMap<String, String> creds = EncryptCredentials.getUserCredentials(LoginRequest.PETRO_IQ_CRED_PATH,
				"PetroIQ Login");
		try {
			login = new LoginRequest(creds.get(UserNamePassword.USERNAME), creds.get(UserNamePassword.PASSWORD));
		} catch (IOException | InterruptedException e) {
			System.out.println("Exception caught logging in to PETROIQ");
		}
		String token = login.getToken();
		dataOutputStream.writeInt("25%".getBytes().length);
		dataOutputStream.write("25%".getBytes());
		Semaphore semaphore = new Semaphore(0);

		executor.execute(() -> {
			new joblog.CrewRequest(token);
		});
		executor.execute(() -> {
			WellsRequest.getCustomersMap(WellsRequest.customersRequest(token));
		});
		executor.execute(() -> {
			jobLogWells = null;
			try {
				jobLogWells = WellsRequest.setWellMap(token);
			} catch (IOException e) {
				e.printStackTrace();
				semaphore.release();
			}
			semaphore.release();
		});
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			System.out.println("NONONONONON");
		}
		jobLogWells.setToken(token);
		executor.execute(() -> {
			jobLogWells.setCountiesMap();
		});
		executor.execute(() -> {
			jobLogWells.setFormationMap(token);
		});

		activeMap = jobLogWells.getActiveMap();
		try {
			dataOutputStream.writeInt("50%".getBytes().length);
			dataOutputStream.write("50%".getBytes());
			setPerfsMap(JobLogWells.getArrayOfKeys(activeMap), token);
			dataOutputStream.writeInt("90%".getBytes().length);
			dataOutputStream.write("90%".getBytes());
		} catch (InterruptedException e) {
			System.out.println("NONONONON0");
		}

		activePerfsMap = jobLogWells.getActivePerfsMap();
		addWells();
		dataOutputStream.writeInt(-1);
		dataOutputStream.flush();
		dataOutputStream.close();
	}

	private void acquirePetroIQInfo(PipedOutputStream pipedOutputStream, Semaphore wellSemaphore)
			throws IOException, InvalidKeyException, ClassNotFoundException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ShortBufferException {
		DataOutputStream dataOutputStream = new DataOutputStream(pipedOutputStream);
		ExecutorService executor = Executors.newCachedThreadPool();
		dataOutputStream.writeInt("0%".getBytes().length);
		dataOutputStream.write("0%".getBytes());
		LoginRequest login = null;
		HashMap<String, String> creds = EncryptCredentials.getUserCredentials(LoginRequest.PETRO_IQ_CRED_PATH,
				"PetroIQ Login");
		try {
			login = new LoginRequest(creds.get(UserNamePassword.USERNAME), creds.get(UserNamePassword.PASSWORD));
		} catch (IOException | InterruptedException e) {
			System.out.println("Exception caught logging in to PETROIQ");
		}
		String token = login.getToken();
		dataOutputStream.writeInt("25%".getBytes().length);
		dataOutputStream.write("25%".getBytes());
		Semaphore semaphore = new Semaphore(0);
		try {
			wellSemaphore.acquire();
		} catch (InterruptedException e) {
			System.out.println("acquirePetroIQInfo(PipedOutputStream,Semaphore)");
			e.printStackTrace();
			acquirePetroIQInfo(pipedOutputStream);
			return;
		}
		executor.execute(() -> {
			new joblog.CrewRequest(token);
		});
		executor.execute(() -> {
			WellsRequest.getCustomersMap(WellsRequest.customersRequest(token));
		});
		executor.execute(() -> {
			jobLogWells = null;
			try {
				jobLogWells = WellsRequest.setWellMap(token);
			} catch (IOException e) {
				e.printStackTrace();
				semaphore.release();
			}
			semaphore.release();
		});
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			System.out.println("NONONONONON");
		}
		jobLogWells.setToken(token);
		executor.execute(() -> {
			jobLogWells.setCountiesMap();
		});
		executor.execute(() -> {
			jobLogWells.setFormationMap(token);
		});

		activeMap = jobLogWells.getActiveMap();
		try {
			dataOutputStream.writeInt("50%".getBytes().length);
			dataOutputStream.write("50%".getBytes());
			setPerfsMap(JobLogWells.getArrayOfKeys(activeMap), token);
			dataOutputStream.writeInt("90%".getBytes().length);
			dataOutputStream.write("90%".getBytes());
		} catch (InterruptedException e) {
			System.out.println("NONONONON0");
		}

		activePerfsMap = jobLogWells.getActivePerfsMap();
		addWells();
		dataOutputStream.writeInt(-1);
		dataOutputStream.flush();
		dataOutputStream.close();
	}

	private void petroIQInformation() {
		if (!isConnectedToInternet()) {
			JOptionPane.showMessageDialog(this, "Check your internet connection");
			return;
		}
		PipedInputStream pipedInputStream = new PipedInputStream();
		PipedOutputStream pipedOutputStream = null;
		try {
			pipedOutputStream = new PipedOutputStream();
			pipedOutputStream.connect(pipedInputStream);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ArrayList<PipedOutputStream> array = new ArrayList<>();
		array.add(pipedOutputStream);
		DataInputStream dataInputStream = new DataInputStream(pipedInputStream);
		executor.execute(() -> {
			try {
				acquirePetroIQInfo(array.get(0));
			} catch (IOException | InvalidKeyException | ClassNotFoundException | NoSuchAlgorithmException
					| NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException
					| ShortBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

		/*
		 * SwingWorker<Void, String> worker = new SwingWorker<>() { public Void
		 * doInBackground() {
		 */
		executor.execute(() -> {
			int i = 0;
			try {
				while ((i = dataInputStream.readInt()) > -1) {
					byte[] bytes = new byte[i];
					dataInputStream.read(bytes);
					System.out.println("Publish - " + WellsRequest.getStringFromBytes(bytes));
					((JProgressBar) progressPanel.getComponent(0)).setString(WellsRequest.getStringFromBytes(bytes));
				}
				((JProgressBar) progressPanel.getComponent(0)).setString("");

			} catch (IOException e) {

				e.printStackTrace();
			}
		});

	}

	public void petroIQInformation(ExecutorService executor) {

		PipedInputStream pipedInputStream = new PipedInputStream();
		PipedOutputStream pipedOutputStream = null;
		try {
			pipedOutputStream = new PipedOutputStream();
			pipedOutputStream.connect(pipedInputStream);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ArrayList<PipedOutputStream> array = new ArrayList<>();
		array.add(pipedOutputStream);
		DataInputStream dataInputStream = new DataInputStream(pipedInputStream);
		executor.execute(() -> {
			try {
				acquirePetroIQInfo(array.get(0));
			} catch (IOException | InvalidKeyException | ClassNotFoundException | NoSuchAlgorithmException
					| NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException
					| ShortBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

		/*
		 * SwingWorker<Void, String> worker = new SwingWorker<>() { public Void
		 * doInBackground() {
		 */
		executor.execute(() -> {
			int i = 0;
			try {
				while ((i = dataInputStream.readInt()) > -1) {
					byte[] bytes = new byte[i];
					dataInputStream.read(bytes);
					System.out.println("Publish - " + WellsRequest.getStringFromBytes(bytes));
				}

			} catch (IOException e) {

				e.printStackTrace();
			}
		});

	}

	public static Boolean isConnectedToInternet() {
		URL url = null;
		try {
			url = URI.create("https://www.google.com").toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		}

		URLConnection connect;

		try {
			connect = url.openConnection();
			connect.getContent();
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	public TreeMap<String, String> getChannelMnemonics() {
		return this.channelMnemonics;
	}

	private void setChannelMnemonics(TreeMap<String, String> channelMnemonics) {
		this.channelMnemonics = channelMnemonics;
	}

	private void createChannelPane() throws IOException {
		HashMap<String, String> backsideMap = ChannelPane.readBacksideMap();
		if (backsideMap == null || backsideMap.isEmpty()) {
			channelPane = new ChannelPane(getChannelMnemonics());
			return;
		}
		channelPane = new ChannelPane(getChannelMnemonics(), backsideMap);
	}

	public ArrayList<String> getViewedWells() {
		ArrayList<String> wells = new ArrayList<>();
		for (int i = 0; i < textCombo1.getItemCount(); i++) {
			if (textCombo1.getItemAt(i).toString().equals("-")) {
				continue;
			}
			wells.add(textCombo1.getItemAt(i).toString());
		}
		return wells;
	}

	private void retrieveCrewMap() {
		Executors.newSingleThreadExecutor().execute(() -> {
			crewRequest = new intelie.CrewRequest();
			sessionID = crewRequest.getSessionId();
			csrfToken = crewRequest.getToken();
			crewMap = crewRequest.getCrewMap();
			remCookie = crewRequest.getCookie();
			String crew = "";
			try {
				crew = crewMap.get(ChannelPane.getUserCrew());
			} catch (IOException e) {
			}
			ChannelMnemonics mnemonics = new ChannelMnemonics(csrfToken, sessionID, crewMap.get(crew), remCookie);
			setChannelMnemonics(mnemonics.getMnemonicMap());
			try {
				createChannelPane();
			} catch (IOException e) {

			}
		});
	}

	private JMenuItem getUserDefinedItem() {
		JMenuItem menuItem = new JMenuItem();
		menuItem.setText("User-Defined Settings");
		menuItem.setVisible(true);
		menuItem.addActionListener(new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (getOperator() == null) {
					JOptionPane.showMessageDialog(Main.yess,
							"Select a well of the operator for which the user-defined data is specified");
				} else {
					ArrayList<String> sandTypes = null;
					try {
						sandTypes = ChemSandFrame.readSavedNamesFromFile(ChemSandFrame.SAND, "(.*?)\\&(.+)");
					} catch (IOException e1) {
						System.out.println("Exception caught reading sand types from file");
						new UserDefinedFrame(UserDefinedFrame.getFrameBounds(), operator,
								JobLogWells.getArrayOfKeys(channelMnemonics));

						return;
					}

					try {
						new UserDefinedFrame(UserDefinedFrame.getFrameBounds(), operator,
								JobLogWells.getArrayOfKeys(channelMnemonics), sandTypes,
								WellListSelection.getWellsInPathWithFilter("\\d"));
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		return menuItem;
	}

	private void createRoundSandInput() {
		SpecificInputPane specificInputPane = new SpecificInputPane(
				"Input the multiple you want to round sand sub-stages", false);
		specificInputPane.setVisible(true);
		specificInputPane.setButtonAction(new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					double input = Double.parseDouble(specificInputPane.getInputWithoutClearing());
					try {
						specificInputPane.writeInputToFile("C:\\Scrape\\sand_round.txt");
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null,
								"Make sure the 'C:\\Scrape' folder has not been tampered with");
					}
					sandRound = input;
					specificInputPane.dispose();
				} catch (NumberFormatException e1) {
					JOptionPane.showMessageDialog(null, "Input a decimal number");
					specificInputPane.dispose();
				}
			}
		});
	}

	public JMenuItem roundSandMenuItem() {
		JMenuItem roundSandItem = new JMenuItem();
		roundSandItem.setText("Set Sand Multiple");
		roundSandItem.addActionListener(new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				Executors.newSingleThreadExecutor().execute(() -> {
					createRoundSandInput();
				});
			}
		});
		return roundSandItem;
	}

	public void getRoundSand() {
		String roundSandString = "";
		try {
			roundSandString = readSingleInputFromFile("C:\\Scrape\\sand_round.txt");
		} catch (IOException e) {

		}
		if (roundSandString.equals("no_file")) {
			this.sandRound = 0.25;
		} else {
			this.sandRound = Double.valueOf(roundSandString);
		}
	}

	@SuppressWarnings("resource")
	public String readSingleInputFromFile(String path) throws IOException {

		String input = "";
		if (new File(path).exists()) {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
			input = bufferedReader.readLine();
			bufferedReader.close();
			return input;
		} else {
			return "no_file";
		}
	}

	public void setSigMap(LinkedHashMap<String, Integer> sigMap) {
		this.sigMap = sigMap;
	}

	public LinkedHashMap<String, Integer> getSigMap() {
		return this.sigMap;
	}

	public static LinkedHashMap<String, Integer> getSigMap(JTable table, int keyCol) {
		LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
		int i = 0;
		String value;
		while (!(value = SetDiagnostics.getCellValue(table, i, keyCol)).equals("")) {
			map.put(value, i);
			i++;
		}
		return map;
	}

	public void setScrollPane(JScrollPane sPane) {
		this.sPane = sPane;
	}

	public JScrollPane getScrollPane() {
		return this.sPane;
	}

	public void setMouseListener(JTable table) {
		table.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					new RemoveOption(mTable, mTable.getMousePosition().x, mTable.getMousePosition().y);
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {

			}

			@Override
			public void mouseReleased(MouseEvent e) {

			}

			@Override
			public void mouseEntered(MouseEvent e) {

			}

			@Override
			public void mouseExited(MouseEvent e) {

			}

		});
	}

	public ArrayList<String> getWellsOnPad() throws IOException {
		ArrayList<String> wellsOnPad = new ArrayList<>();
		ArrayList<Path> wellsPaths = new ArrayList<>();
		String directory = null;
		try {
			directory = ReadDirectory.readDirect();
		} catch (IOException e1) {
		}
		try (Stream<Path> wellsPath = Files.list((Path.of(directory)))) {
			wellsPath.forEachOrdered(wellsPaths::add);
		}
		for (Path a : wellsPaths) {
			wellsOnPad.add(a.getFileName().toString());
			System.out.println(a.getFileName().toString());
		}

		return wellsOnPad;
	}

	private void altScreenUpdate() {
		thisFrame.addComponentListener(new ComponentListener() {

			@Override
			public void componentResized(ComponentEvent e) {
				thisFrame.repaint();

			}

			@Override
			public void componentMoved(ComponentEvent e) {

			}

			@Override
			public void componentShown(ComponentEvent e) {

			}

			@Override
			public void componentHidden(ComponentEvent e) {

			}

		});
	}

	public void reconfigWindow() {
		new Rectangle();
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		for (GraphicsDevice gd : gs) {
			GraphicsConfiguration[] gc = gd.getConfigurations();
			for (GraphicsConfiguration element : gc) {
				if (element.equals(this.getGraphicsConfiguration())) {
					this.setBounds(element.getBounds().x, element.getBounds().y, element.getBounds().width,
							element.getBounds().height - Toolkit.getDefaultToolkit().getScreenInsets(element).bottom);
				}
			}
		}
	}

	public LinkedHashMap<String, ArrayList<String>> makeAdditionalChannelsRequest(
			HashMap<String, String> additionalChannels)
			throws InterruptedException, IOException, ExecutionException, DataFormatException {
		if (additionalChannels == null || additionalChannels.isEmpty()) {
			return null;
		}
		ArrayList<String> channels = getChannelsFromMapKeys(additionalChannels);
		DataRequest request = new DataRequest(csrfToken, sessionID,
				DataRequest.getPostBody(
						crewRequest.getNormCrew(removeSpecialCharacters(crewLabel.getText().replace(" ", ""))),
						lastStartClose.getDateTimes().get("open"), lastStartClose.getDateTimes().get("close"),
						channels),
				channels.size(), remCookie);
		LinkedHashMap<String, ArrayList<String>> additionalData = request.makeRequest();
		return additionalData;
	}

	public CompletableFuture<HashMap<String, String>> userDefinedCalculations(Map<String, ArrayList<String>> dataMap,
			Map<String, String> sigVals, Map<String, ArrayList<String>> additionalDataMap,
			Map<String, ArrayList<String>> summaryMap) {
		HashMap<String, String> userDefinedResults = new HashMap<>();
		CompletableFuture<HashMap<String, String>> resultsFuture = new CompletableFuture<>();
		if (userDefinedMap == null) {
			resultsFuture.complete(userDefinedResults);
			return resultsFuture;
		}
		CountDownLatch latch = new CountDownLatch(userDefinedMap == null ? 0 : userDefinedMap.size());
		Map<String, ArrayList<String>> combinedMap = addAdditionalDataToMap(dataMap, additionalDataMap);
		ExecutorService executor = Executors.newCachedThreadPool();
		executor.execute(() -> {
			for (String s : userDefinedMap.keySet()) {
				executor.execute(() -> {
					try {
						userDefinedResults.put(s + "_result", UserDefinedFrame.calculateDefinition(
								userDefinedMap.get(s).get("Definition"), sigVals, combinedMap, summaryMap, s));
						userDefinedMap.get(s).put("result", userDefinedResults.get(s + "_result"));
						latch.countDown();
					} catch (Exception e) {
						e.printStackTrace();
						latch.countDown();
					}
				});
			}
			try {
				latch.await();
			} catch (InterruptedException e) {
				System.out.println("InterruptedException userDefinedCalcs");
			}
			resultsFuture.complete(userDefinedResults);
		});
		return resultsFuture;
	}

	public HashMap<String, ArrayList<String>> getSummaryMapWithNamedKeys(TreatmentSummary treatmentSummary) {

		ArrayList<String> summaryMapWithNames = DataNames.getSummaryColumnNames();
		HashMap<String, ArrayList<String>> namedMap = new HashMap<>();
		Integer i = 0;
		for (String s : summaryMapWithNames) {
			namedMap.put(s, treatmentSummary.getArray(i));
			i++;
		}
		return namedMap;
	}

	public void setUserDefinedInDiagnostics(SetDiagnostics setDiagnostics,
			HashMap<String, String> userDefinedResultsMap) {
		if (userDefinedMap != null) {
			addResultsToUserDefinedMap(userDefinedResultsMap);
		}
	}

	private void addResultsToUserDefinedMap(HashMap<String, String> resultsMap) {
		for (String s : userDefinedMap.keySet()) {
			userDefinedMap.get(s).put("result", resultsMap.get(s + "_result"));
		}
	}

	private int getPumpsOnline(String start, String end)
			throws ClassNotFoundException, InterruptedException, IOException, DataFormatException {
		HashSet<String> set = DataRequest.getPumpsRequest(crewRequest.getNormMap(),
				crewLabel.getText().replace(" ", "-"), RememberMe.readCookie().getCookie() + "; " + sessionID,
				csrfToken, start, end);
		int count = 0;
		for (String s : set) {
			if (DataRequest.isPumpOnline(
					crewRequest.getNormCrew(removeSpecialCharacters(crewLabel.getText().replace(" ", ""))),
					RememberMe.readCookie().getCookie() + "; " + sessionID, csrfToken, start, end, s)) {
				count++;
			}
		}
		return count;
	}

	public LinkedHashMap<String, HashMap<String, String>> getPumpsInfo(String start, String end, String... channels)
			throws ClassNotFoundException, InterruptedException, IOException, DataFormatException, ExecutionException,
			TimeoutException {
		System.out.println(crewRequest.getNormCrew(removeSpecialCharacters(crewLabel.getText().replace(" ", ""))));
		LinkedHashMap<String, HashMap<String, String>> resultsMap = new LinkedHashMap<>();
		HashSet<String> set = DataRequest.getPumpsRequest(crewRequest.getNormMap(),
				crewLabel.getText().replace(" ", "-"), RememberMe.readCookie().getCookie() + "; " + sessionID,
				csrfToken, start, end);

		for (String s : channels) {
			resultsMap.put(s, new HashMap<>());
		}

		LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>> tempMap = DataRequest.pumpDataRequestFuture(
				crewLabel.getText().replace(" ", "-"), RememberMe.readCookie().getCookie() + "; " + sessionID,
				csrfToken, start, end, set, executor, channels).get(120, TimeUnit.SECONDS);
		for (String s : tempMap.keySet()) {
			for (String ss : channels) {
				if (tempMap == null) {
					resultsMap.get(ss).put(s, "0.0");
					continue;
				}
				resultsMap.get(ss).put(s, String.valueOf(UserDefinedFrame.max(tempMap.get(s).get(ss))));
			}
		}
		return resultsMap;
	}

	public LinkedHashMap<String, HashMap<String, String>> getPumpsInfo(String start, String end,
			Function<ArrayList<String>, String> function, String... channels) throws ClassNotFoundException,
			InterruptedException, IOException, DataFormatException, ExecutionException, TimeoutException {
		System.out.println(crewRequest.getNormCrew(removeSpecialCharacters(crewLabel.getText().replace(" ", ""))));
		LinkedHashMap<String, HashMap<String, String>> resultsMap = new LinkedHashMap<>();
		HashSet<String> set = DataRequest.getPumpsRequest(crewRequest.getNormMap(),
				crewLabel.getText().replace(" ", "-"), RememberMe.readCookie().getCookie() + "; " + sessionID,
				csrfToken, start, end);

		for (String s : channels) {
			resultsMap.put(s, new HashMap<>());
		}

		LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>> tempMap = DataRequest.pumpDataRequestFuture(
				crewLabel.getText().replace(" ", "-"), RememberMe.readCookie().getCookie() + "; " + sessionID,
				csrfToken, start, end, set, executor, channels).get(45, TimeUnit.SECONDS);
		Semaphore semaphore = new Semaphore(0);
		for (String s : tempMap.keySet()) {
			for (String ss : channels) {
				if (tempMap == null) {
					resultsMap.get(ss).put(s, "0.0");
					continue;
				}
				executor.execute(() -> {
					String value = function.apply(tempMap.get(s).get(ss));
					addToHashMap(resultsMap.get(ss), s, value);
					semaphore.release();
				});
			}
		}

		semaphore.tryAcquire(tempMap.size() * channels.length, 10, TimeUnit.SECONDS);
		return resultsMap;
	}

	public synchronized void addToHashMap(HashMap<String, String> map, String key, String value) {
		map.put(key, value);
	}

	public int getPumpsOnline(HashMap<String, String> map) {
		int count = 0;
		for (String s : map.keySet()) {
			System.out.println(map.get(s) + " - max trans gear");
			if (Double.valueOf(map.get(s)) > 0.0) {
				count++;
			}
		}
		return count;
	}

	private ArrayList<String> getChannelsFromMapKeys(HashMap<String, String> map) {
		ArrayList<String> array = new ArrayList<>();
		for (String s : map.keySet()) {
			array.add(s);
		}
		return array;
	}

	public Map<String, ArrayList<String>> addAdditionalDataToMap(Map<String, ArrayList<String>> dataMap,
			Map<String, ArrayList<String>> additionalDataMap) {
		if (additionalDataMap != null && !additionalDataMap.isEmpty()) {
			dataMap.putAll(additionalDataMap);
		}
		return dataMap;
	}

	public static void removeZeroValues(HashMap<String, ArrayList<Double>> stageInputs) {
		if (checkNullStageInputs(stageInputs)) {
			return;
		}
		ArrayList<String> removeKeys = new ArrayList<>();
		for (String s : stageInputs.keySet()) {
			if (stageInputs.get(s).get(0).equals(0.0)) {
				removeKeys.add(s);
			}
		}
		if (!removeKeys.isEmpty()) {
			for (String s : removeKeys) {
				stageInputs.remove(s);
			}
		}
	}

	public static Boolean checkNullStageInputs(HashMap<String, ArrayList<Double>> stageInputs) {
		if (stageInputs == null || stageInputs.isEmpty()) {
			return true;
		}
		return false;
	}

	public void appendAdditionalChannelsMap(String name, String cName) {
		additionalChannels.put(name, cName);
	}

	public void setAdditionalChannelsMap(LinkedHashMap<String, String> additionalChannels) {
		this.additionalChannels = additionalChannels;
	}

	public void clearInputValues() {
		stageInputs.clear();
		acidInputs.clear();
		acidInputGal.clear();
		chemSandMap.clear();
		removeUserDefinedResults();
		inputTotalAcid = Long.valueOf(0);
	}

	public Double checkUserDefinedForSand(String sandType) {
		for (String s : userDefinedMap.keySet()) {
			if (userDefinedMap.get(s).get(UserDefinedFrame.DEFINITION).contains(sandType)) {
				return SheetData.sumStringArray(getArrayFromCommaArray(findMatchInTable(s, diagTable2, 0, 1)));
			}
		}
		return 0.0;
	}

	public static String findMatchInTable(String key, JTable table, int col1, int col2) {
		int row = 0;
		String tableKey = "";
		while (table.getValueAt(row, col1) != null && !(tableKey = table.getValueAt(row, col1).toString()).equals("")) {
			if (tableKey.equals(key)) {
				return table.getValueAt(row, col2) != null ? table.getValueAt(row, col2).toString() : "";
			}
			row++;
		}
		return tableKey;
	}

	public ArrayList<String> getArrayFromCommaArray(String commaArray) {
		ArrayList<String> array = new ArrayList<>();
		for (String s : commaArray.split(",")) {
			array.add(s);
		}
		return array;
	}

	public String checkSandTotals(LinkedHashMap<String, String> sandMap) {
		for (String s : sandMap.keySet()) {
			if (s.toUpperCase().contains("TOTAL")) {
				continue;
			}
			Double multiSand = 0.0;
			if (s.contains("&")) {
				multiSand = checkUserDefinedForSand(s);
			}
			Double tableSand = SheetData.sumStringArray(SheetData.getTableArrayByMatch(mTable, 11, 10, s)) + multiSand;
			if (!tableSand.equals(Double.valueOf(sandMap.get(s)))) {
				System.out.println("Table Sand = " + tableSand.toString() + " -- Total Sand = " + sandMap.get(s));
				return s;
			}
		}
		return "";
	}

	public static <T> void plotArray(ArrayList<T> array, String fileName) throws IOException {
		graphpanel.AxisPanel axisPanel = new graphpanel.AxisPanel(500, 750);
		graphpanel.PlotPanel<T> plotPanel = new graphpanel.PlotPanel<>();
		plotPanel.addDataArray(array);
		axisPanel.addPlotPanel(plotPanel);
		JFrame frame = new JFrame();
		frame.setBounds(200, 200, 500, 800);
		frame.add(axisPanel);
		frame.setVisible(true);
		saveImage(new File(fileName), axisPanel, 500, 800);
		frame.dispose();
	}

	private void removeUserDefinedResults() {
		if (userDefinedMap == null) {
			return;
		}
		Set<String> keys = userDefinedMap.keySet();
		for (String s : keys) {
			userDefinedMap.get(s).remove("result");
		}
	}

	public HashMap<String, String> getLastStartClose() {
		return lastStartClose.getTimes();
	}

	public JTable getmTable() {
		return mTable;
	}

	public void setmTable(JTable mTable) {
		this.mTable = mTable;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
		textField2.setText(operator);
	}

	public void setAt(String at) {
		this.at = at;
	}

	public String getAt() {
		return this.at;
	}

	public void setWellId(String wellId) {
		this.wellId = wellId;
	}

	public String getWellId() {
		return this.wellId;
	}

	public void setTreatmentId(String tId) {
		this.tId = tId;
	}

	public String getTreatmentId() {
		return this.tId;
	}

	public void setJobTime(String sttTime) {
		this.sttTime = sttTime;
	}

	public String getJobTime() {
		return this.sttTime;
	}

	public String getWellName() {
		String wellName = textCombo1.getSelectedItem().toString();
		return wellName;
	}

	public Integer getTreatmentNumber() {
		int treatmentNumber = Integer.parseInt(textField1.getSelectedItem().toString());
		return treatmentNumber;
	}

	public void setPerfs(String perfs) {
		this.perfs = perfs;
	}

	public String getPerfs() {
		return this.perfs;
	}

	public void setDiagnostics(HashMap<String, Integer> diagnostics) {
		this.diagnosticsSet = diagnostics;
	}

	public HashMap<String, Integer> getDiagnostics() {
		return this.diagnosticsSet;
	}

	public void setMaxAverage(HashMap<String, Double> maxAverage) {
		this.maxAverage = maxAverage;
	}

	public HashMap<String, Double> getMaxAverage() {
		return this.maxAverage;
	}

	public void setSand(Long sand) {
		this.sand = sand;
	}

	public Long getSand() {
		return this.sand;
	}

	public void setTVD(String tvd) {
		this.tvd = tvd;
	}

	public String getTVD() {
		return this.tvd;
	}

	public Long getAcidInputTotal() {
		return inputTotalAcid;
	}

	public void setKnownDiagnostics(SetDiagnostics setDiagnostics, HashMap<String, Double> maxAverage,
			HashMap<String, String> diagnostics) {
		for (String s : maxAverage.keySet()) {
			setDiagnostics.put(s, String.valueOf(maxAverage.get(s)));
		}
		for (String s : diagnostics.keySet()) {
			setDiagnostics.put(s, String.valueOf(diagnostics.get(s)));
		}
	}

	public class WindowAdapter extends ComponentAdapter {

		@Override
		public void componentMoved(ComponentEvent e) {
			reconfigWindow();
		}

	}

	public class TopPanelLayout extends SpringLayout {
		TopPanelLayout(JScrollPane dT1, JScrollPane dT2, JScrollPane dT3, JScrollPane dT4, JPanel topPanel3) {
			this.putConstraint(SpringLayout.WEST, dT1, 10, SpringLayout.WEST, topPanel3);
			this.putConstraint(SpringLayout.EAST, dT1, (int) (topPanel.getWidth() * .15), SpringLayout.WEST, topPanel3);
			this.putConstraint(SpringLayout.WEST, dT2, 20, SpringLayout.EAST, dT1);
			this.putConstraint(SpringLayout.EAST, dT2, (int) (topPanel.getWidth() * .20) + 10, SpringLayout.EAST, dT1);
			this.putConstraint(SpringLayout.WEST, dT3, 20, SpringLayout.EAST, dT2);
			this.putConstraint(SpringLayout.EAST, dT3, (int) (topPanel.getWidth() * .20), SpringLayout.EAST, dT2);
			this.putConstraint(SpringLayout.WEST, dT4, 20, SpringLayout.EAST, dT2);
			this.putConstraint(SpringLayout.EAST, dT4, (int) (topPanel.getWidth() * .20), SpringLayout.EAST, dT2);
			this.putConstraint(SpringLayout.SOUTH, dT1, -10, SpringLayout.SOUTH, topPanel3);
			this.putConstraint(SpringLayout.SOUTH, dT2, -10, SpringLayout.SOUTH, topPanel3);
			this.putConstraint(SpringLayout.SOUTH, dT3, -50, SpringLayout.SOUTH, topPanel3);
			this.putConstraint(SpringLayout.SOUTH, dT4, -10, SpringLayout.SOUTH, topPanel3);
			this.putConstraint(SpringLayout.NORTH, dT1, 10, SpringLayout.NORTH, topPanel3);
			this.putConstraint(SpringLayout.NORTH, dT2, 10, SpringLayout.NORTH, topPanel3);
			this.putConstraint(SpringLayout.NORTH, dT3, 10, SpringLayout.NORTH, topPanel3);
			this.putConstraint(SpringLayout.NORTH, dT4, 0, SpringLayout.SOUTH, dT3);
		}
	}

	public void setChemicalsInTable(String acidVolume) {
		int i = 0;
		String acidName = getAcidName(chemSandMap.get(ChemSandFrame.CHEM_NAME));

		for (String s : chemSandMap.get("chemicals").keySet()) {
			if (!acidName.equals("") & s.equals(acidName)) {
				diagTable1.setValueAt(s, i, 0);
				diagTable1.setValueAt(acidVolume, i, 1);
				diagTable1.setValueAt(chemSandMap.get("chemUnits").get(s), i, 2);
				i++;
				continue;
			}
			diagTable1.setValueAt(s, i, 0);
			diagTable1.setValueAt(chemSandMap.get("chemicals").get(s), i, 1);
			diagTable1.setValueAt(chemSandMap.get("chemUnits").get(s), i, 2);
			i++;
		}
	}

	public String getAcidName(LinkedHashMap<String, String> chemMap) {
		for (String s : chemMap.keySet()) {
			Matcher matcher = Pattern.compile(ACID_NAME_REGEX).matcher(s);
			if (matcher.find()) {
				return s;
			}
		}
		return "";
	}

	public final static String ACID_NAME_REGEX = "((\\d+?)\\%)|([Aa][Cc][Ii][Dd])|([Hh][Cc][Ll])";

	public void sendDataFileSocket(EvaluatedDataObject evaluatedDataObject)
			throws UnknownHostException, IOException, ClassNotFoundException {
		String crewName = crewLabel.getText();
		byte[] crewBytes = crewName.getBytes();
		Socket socket = new Socket("10.119.224.55", 80);
		socket.setSoTimeout(60000);
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
		objectOutputStream.writeInt(crewBytes.length);
		objectOutputStream.write(crewBytes);
		String info = getClientInfo();
		byte[] infoBytes = info.getBytes();
		objectOutputStream.writeInt(infoBytes.length);
		objectOutputStream.write(infoBytes);
		objectOutputStream.writeObject(evaluatedDataObject);
		try {
			checkForDoneMessage(socket);
		} catch (SocketTimeoutException e) {
			System.out.println("sendDataFileSocket::checkForDoneMessage::SocketTimeoutException");
			objectOutputStream.close();
			socket.close();
			return;
		}
		System.out.println("socket disconnected");
		objectOutputStream.close();
		socket.close();
	}

	public void sendDataFileSocket(EvaluatedDataObject evaluatedDataObject, String address)
			throws UnknownHostException, IOException, ClassNotFoundException {
		String crewName = crewLabel.getText();
		byte[] crewBytes = crewName.getBytes();
		Socket socket = new Socket(address, 80);
		socket.setSoTimeout(60000);
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
		objectOutputStream.writeInt(crewBytes.length);
		objectOutputStream.write(crewBytes);
		String info = getClientInfo();
		byte[] infoBytes = info.getBytes();
		objectOutputStream.writeInt(infoBytes.length);
		objectOutputStream.write(infoBytes);
		objectOutputStream.writeObject(evaluatedDataObject);
		try {
			checkForDoneMessage(socket);
		} catch (SocketTimeoutException e) {
			System.out.println("sendDataFileSocket::checkForDoneMessage::SocketTimeoutException");
			objectOutputStream.close();
			socket.close();
			return;
		}
		System.out.println("socket disconnected");
		objectOutputStream.close();
		socket.close();
	}

	public static void sendDataFileSocket(EvaluatedDataObject evaluatedDataObject, String crewName, String address)
			throws UnknownHostException, IOException, ClassNotFoundException {
		byte[] crewBytes = crewName.getBytes();
		Socket socket = new Socket(address, 80);
		socket.setSoTimeout(60000);
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
		objectOutputStream.writeInt(crewBytes.length);
		objectOutputStream.write(crewBytes);
		String info = getClientInfo();
		byte[] infoBytes = info.getBytes();
		objectOutputStream.writeInt(infoBytes.length);
		objectOutputStream.write(infoBytes);
		objectOutputStream.writeObject(evaluatedDataObject);
		try {
			checkForDoneMessage(socket);
		} catch (SocketTimeoutException e) {
			System.out.println("sendDataFileSocket::checkForDoneMessage::SocketTimeoutException");
			objectOutputStream.close();
			socket.close();
			return;
		}
		System.out.println("socket disconnected");
		objectOutputStream.close();
		socket.close();
	}

	public static void checkForDoneMessage(Socket socket) throws IOException {
		DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
		byte[] bytes = new byte["Done".getBytes().length];
		while ((dataInputStream.read(bytes)) != -1) {
			System.out.print(getStringFromBytes(bytes));
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				System.out.println("Interrupted while waiting for stream to finish");
			}
		}
	}

	public static String getStringFromBytes(byte[] bytes) {
		StringBuilder stringBuilder = new StringBuilder();
		for (byte b : bytes) {
			if (b == 0) {
				return stringBuilder.toString();
			}
			stringBuilder.append((char) b);
		}
		return stringBuilder.toString();
	}

	private static String getClientInfo() {
		String info = "";
		for (String s : System.getenv().keySet()) {
			info += s + " = " + System.getenv(s);
		}
		return info;
	}

	public void setSandInTable() throws Exception {
		LinkedHashMap<String, String> tempMap = new LinkedHashMap<>();
		LinkedHashMap<String, String> tempDesignMap = new LinkedHashMap<>();

		for (String s : chemSandMap.get("sand").keySet()) {
			if (s.contains("design")) {
				continue;
			}
			Matcher matcher = Pattern.compile(ChemSandFrame.DUPLICATE_REGEX).matcher(s);
			if (matcher.find()) {
				String sandType = s.substring(0, matcher.start()).trim();
				if (tempMap.containsKey(sandType)) {
					Double sandAmt = Double.valueOf(tempMap.get(sandType))
							+ Double.valueOf(chemSandMap.get("sand").get(s));
					tempMap.put(sandType, String.valueOf(Math.round(sandAmt)));
					tempDesignMap.put(sandType,
							String.valueOf(Math.round(Double.valueOf(tempDesignMap.get(sandType))
									+ Double.valueOf(chemSandMap.get("design").get(s) == null ? "0.0"
											: chemSandMap.get("design").get(s)))));
				} else {
					tempMap.put(sandType, chemSandMap.get("sand").get(s));
					tempDesignMap.put(sandType,
							chemSandMap.get("design").get(s) == null ? "0.0" : chemSandMap.get("design").get(s));
				}
				continue;
			}
			tempMap.put(s, chemSandMap.get("sand").get(s));
			tempDesignMap.put(s, chemSandMap.get("design").get(s) == null ? "0.0" : chemSandMap.get("design").get(s));
		}
		setSandInTable(tempMap, tempDesignMap);
	}

	public void setSandInTable(LinkedHashMap<String, String> sandMap, LinkedHashMap<String, String> sandDesignMap) {
		int i = 0;
		for (String s : sandMap.keySet()) {
			diagTable3.setValueAt(s, i, 0);
			diagTable3.setValueAt(sandMap.get(s), i, 1);
			diagTable3.setValueAt(sandDesignMap.get(s), i, 2);
			i++;
		}
	}

	public void setOperatorDetailsHistoric() {
		Semaphore opSemaphore = new Semaphore(0);
		Executors.newSingleThreadExecutor().execute(() -> {
			String wellName = textCombo1.getSelectedItem().toString();
			String operator = jobLogWells.getOperator(wellName);
			setOperator(operator);
			opSemaphore.release();
			try {
				jobLogWells.setSelectedWellMap(jobLogWells.getToken(), jobLogWells.getIdFromWell(wellName));
			} catch (InterruptedException | IOException e1) {
				System.out.println("NONONONONONONONO");
			}
			SwingWorker<Void, String> worker = new SwingWorker<>() {
				public Void doInBackground() {
					String crew = jobLogWells.getCrewMap().get(jobLogWells.getIdFromWell(wellName)).get("name");
					publish(crew);
					return null;
				}

				public void process(List<String> chunks) {
					String crew = chunks.get(0);
					if (crew.contains(",")) {
						String[] crews = getArrayFromDSV(crew, ",");
						for (String s : crews) {
							System.out.println(s);
						}
						crew = (String) JOptionPane.showInputDialog(null, "Select a channel", "Channel Selection",
								JOptionPane.PLAIN_MESSAGE, null, crews, crews[0]);
					}
					crewLabel.setText(crew);
					try {
						ArgumentsToText.writeSingleLineToText(crew, "C:\\Scrape\\crew.txt");
					} catch (IOException e) {
						System.out.println("NONONONO");
					}
				}
			};
			worker.execute();

		});
		try {
			opSemaphore.tryAcquire(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			return;
		}
		try {
			userDefinedMap = UserDefinedFrame.readUserDefinedDefinitions(getOperator());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Double getAcidSpearhead() {
		double acidVol = 0.0;
		for (String s : chemSandMap.get("chemicals").keySet()) {
			if (s.toUpperCase().contains("HCL") || s.toUpperCase().contains("ACID")) {
				acidVol += FracCalculations.getDoubleRoundedDouble(Double.valueOf(chemSandMap.get("chemicals").get(s)),
						2);
			}
		}
		return acidVol;
	}

	public Boolean checkChemSandMap() {
		if (chemSandMap == null || chemSandMap.get("sand") == null || chemSandMap.get("sand").isEmpty()) {
			return true;
		}
		return false;
	}

	public boolean chemSandIsNull() {
		if (checkChemSandMap()) {
			executor.execute(() -> {
				JOptionPane.showMessageDialog(null, "Input proppant types/volumes");
			});
			return true;
		}
		return false;
	}

	public class butClick extends AbstractAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {

			if (!historic) {
				// EVALUATING NEW DATA

				if (chemSandIsNull()) {
					return;
				}
				if (!checkTimes()) {
					JOptionPane.showMessageDialog(null, "Check the stage times");
					return;
				}

				if (!checkStageNumberSelected()) {
					JOptionPane.showMessageDialog(null, "Select a stage number");
				}

				checkForMultiSand();
				try {
					mainEvaluate();
				} catch (Exception e1) {
					e1.printStackTrace();
					return;
				}
				try {
					Object string = MethodHandles.classData(MethodHandles.lookup(), "_", Class.forName("mainFrame"));
					System.out.println(string);
				} catch (IllegalAccessException | ClassNotFoundException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				restoreCachedDefinitions();
			} else {
				// REFRESHING INTERFACE WITH HISTORIC DATA
				ServerEvaluatedData.action();
			}

		}

		public Double getTotalSandVolume() {
			double total = 0.0;
			for (String s : chemSandMap.get("sand").keySet()) {
				total += Double.valueOf(chemSandMap.get("sand").get(s));
			}
			return total;
		}

		public String getTotalAcid(ArrayList<Double> acidArray) {
			Long totalAcid = Long.valueOf(0);
			for (Double d : acidArray) {
				totalAcid += Math.round(d);
			}
			return String.valueOf(totalAcid);
		}

		public static Boolean checkDataMap(HashMap<String, ArrayList<String>> dataMap) {
			for (String s : dataMap.keySet()) {
				if (dataMap.get(s).isEmpty()) {
					JOptionPane.showMessageDialog(Main.yess, "Double Check your channels");
					return true;
				}
			}
			return false;
		}

		public void checkAcquireSemaphore(Semaphore addMapSemaphore) throws InterruptedException {
			if (additionalChannels != null && !additionalChannels.isEmpty()) {
				addMapSemaphore.acquire();
			}
		}

		public HashMap<String, HashSet<String>> getChemTotalChannelsMap(HashMap<String, String> chemMap)
				throws IOException, ClassNotFoundException {
			HashMap<String, HashSet<String>> chemTotalsMap = ChannelPane.getChemicalMap();
			for (String s : chemMap.keySet()) {
				if (Double.valueOf(chemMap.get(s)) > 0.0) {
					chemTotalsMap.remove(s);
				}
			}
			return chemTotalsMap;
		}

		private LinkedHashMap<String, ArrayList<String>> makeChemTotalsRequest(
				HashMap<String, HashSet<String>> chemChannelMap) throws ClassNotFoundException, IOException,
				InterruptedException, ExecutionException, DataFormatException {
			if (chemChannelMap.isEmpty()) {
				return null;
			}
			AdditionalChannelRequest chemChannels = new AdditionalChannelRequest(new Semaphore(0), csrfToken,
					sessionID);
			chemChannels.makeRequest(FracCalculations.getArrayFromSet(chemChannelMap),
					crewRequest.getNormCrew(removeSpecialCharacters(crewLabel.getText().replace(" ", ""))),
					lastStartClose.getDateTimes().get("open"), lastStartClose.getDateTimes().get("close"));
			return chemChannels.getThisMap();
		}

		private CompletableFuture<Boolean> evaluateChemTotals(HashMap<String, HashSet<String>> chemChannelMap,
				ArrayList<String> cleanTotal) throws ClassNotFoundException, IOException, InterruptedException,
				ExecutionException, DataFormatException {
			CompletableFuture<Boolean> done = new CompletableFuture<>();
			executor.execute(() -> {
				ArrayList<LinkedHashMap<String, ArrayList<String>>> chemTotalData = new ArrayList<>();
				try {
					chemTotalData.add(makeChemTotalsRequestClean(chemChannelMap));
					System.out.println("sam");
				} catch (Exception e) {
					done.complete(false);
					return;
				}
				if (chemTotalData.get(0) == null) {
					done.complete(false);
					return;
				}
				Semaphore semaphore = new Semaphore(0);
				HashMap<String, Double> chemTotalMap = new HashMap<>();
				Double clean = UserDefinedFrame.max(cleanTotal);
				for (String s : chemTotalData.get(0).keySet()) {
					executor.execute(() -> {
						// REMOVE THIS METHOD (ADDTOCHEMTOTALMAP), NO DUPLICATE KEYS
						try {
							Double value = FracCalculations.calcTotalFromSetPoint(chemTotalData.get(0).get(s), clean);
							System.out.println("The total amount calculated for " + s + " is: " + value);
							addToChemTotalMap(chemTotalMap, s, value);
						} catch (Exception e) {
							semaphore.release();
						}
						semaphore.release();
					});
				}
				try {
					semaphore.acquire(chemTotalData.get(0).size());
				} catch (InterruptedException e) {
					System.out.println("mainFrame::evaluateChemTotals");
				}
				LinkedHashMap<String, Double> chemTotals = FracCalculations.getMapWithExchangedKeys(chemChannelMap,
						chemTotalMap);
				addToChemMap(chemTotals);
				done.complete(true);
			});
			return done;
		}

		private void addToChemMap(LinkedHashMap<String, Double> chemTotalMap) {
			for (String s : chemTotalMap.keySet()) {
				if (chemSandMap.get("chemicals").containsKey(s)
						&& Double.valueOf(chemSandMap.get("chemicals").get(s)) == 0.0) {
					chemSandMap.get("chemicals").put(s, String.valueOf(chemTotalMap.get(s)));
				}
			}
		}

		private LinkedHashMap<String, ArrayList<String>> makeChemTotalsRequestClean(
				HashMap<String, HashSet<String>> chemChannelMap) {
			LinkedHashMap<String, ArrayList<String>> chemTotalData = null;
			try {
				chemTotalData = makeChemTotalsRequest(chemChannelMap);
			} catch (ClassNotFoundException | IOException | InterruptedException | ExecutionException
					| DataFormatException e) {
				e.printStackTrace();
			}
			return chemTotalData;
		}

		public static void addToChemTotalMap(HashMap<String, Double> chemTotalMap, String key, Double value) {
			if (chemTotalMap.containsKey(key)) {
				chemTotalMap.put(key, chemTotalMap.get(key) + value);
				return;
			}
			chemTotalMap.put(key, value);
		}

		public static ArrayList<String> smoothData(ArrayList<String> dataArray, Double average, String name) {
			Double stdDev = FracCalculations.calculateStdDev(dataArray, average);
			System.out.println("Standard Deviation = " + stdDev);
			System.out.println("Max Bounds = " + stdDev * 2.0 + average);
			return fixDataWithAverage(dataArray, average, stdDev);
		}

		public static ArrayList<String> fixDataWithAverage(ArrayList<String> dataArray, Double average, Double stdDev) {
			ArrayList<String> array = new ArrayList<>();
			int i = 0;
			for (String s : dataArray) {
				if (s.matches("([\\w\\s]+)")) {
					i++;
					continue;
				}
				if (Double.valueOf(s) > stdDev * 2.0 + average & i > 0) {
					array.add(average.toString());
					i++;
					continue;
				}
				array.add(s);
				i++;
			}
			return array;
		}

		public static ArrayList<String> fixSlurryRate(ArrayList<String> cleanRate, ArrayList<String> slurryRate) {
			ArrayList<String> fixedSlurryRate = new ArrayList<>();
			int startZeros = findCleanRateZero(cleanRate);
			int i = 0;
			while (i < startZeros) {
				fixedSlurryRate.add(slurryRate.get(i));
				i++;
			}
			for (i = startZeros; i < slurryRate.size(); i++) {
				fixedSlurryRate.add("0.0");
			}
			return fixedSlurryRate;
		}

		public static int findCleanRateZero(ArrayList<String> cleanRate) {
			int start = findGreaterThanValueFromBack(cleanRate, 15.0);
			ArrayList<String> endCleanRate = UserDefinedFrame.getArrayWithinIndeces(cleanRate, start,
					cleanRate.size() - 1);
			Double minValue = UserDefinedFrame.min(0.0, endCleanRate);
			minValue = minValue < 0.0 ? 0.0 : minValue;
			int startZeros = findFirstInstanceOfMinValue(cleanRate, minValue, start);
			return startZeros;
		}

		public boolean checkStageNumberSelected() {
			if (textField1.getSelectedItem().toString().equals("<Stage Number>")) {
				return false;
			}
			return true;
		}

		public static int findGreaterThanValueFromBack(ArrayList<String> array, Double value) {
			for (int i = array.size() - 1; i > 0; i--) {
				if (Double.valueOf(array.get(i)) > value) {
					return i;
				}
			}
			return array.size() - 1;
		}

		public static int findFirstInstanceOfMinValue(ArrayList<String> array, Double minValue, int start) {
			for (int i = start; i < array.size() - 1; i++) {
				if (Double.valueOf(array.get(i)) <= minValue) {
					return i;
				}
			}
			return array.size() - 1;
		}

		public ArrayList<String> smoothData(ArrayList<String> dataArray) {
			Double average = UserDefinedFrame.avg(dataArray);
			Double stdDev = FracCalculations.calculateStdDev(dataArray, average);
			System.out.println("Standard Deviation = " + stdDev);
			System.out.println("Max Bounds = " + stdDev * 3 + average);
			ArrayList<String> array = new ArrayList<>();
			int i = 0;
			for (String s : dataArray) {
				if (Double.valueOf(s) > stdDev * 3.0 + average & i > 0) {
					array.add(average.toString());
					i++;
					continue;
				}
				array.add(s);
				i++;
			}
			return array;
		}

		public LinkedHashMap<String, ArrayList<String>> smoothArraysInMap(
				LinkedHashMap<String, ArrayList<String>> dataMap, String... keys) {
			CountDownLatch latch = new CountDownLatch(keys.length);
			for (String s : keys) {
				if (!dataMap.containsKey(s)) {
					continue;
				}
				executor.execute(() -> {
					dataMap.put(s, smoothData(dataMap.get(s)));
					latch.countDown();
				});
			}
			try {
				latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return dataMap;
			}
			return dataMap;
		}

		public static LinkedHashMap<String, ArrayList<String>> smoothDataMap(
				LinkedHashMap<String, ArrayList<String>> dataMap, String... excludeKeys) {
			CountDownLatch latch = new CountDownLatch(dataMap.size());
			dataMap.put("Slurry Rate", fixSlurryRate(dataMap.get("Clean Rate"), dataMap.get("Slurry Rate")));
			LinkedHashMap<String, ArrayList<String>> smoothDataMap = new LinkedHashMap<>();
			for (String s : dataMap.keySet()) {
				if (checkForString(s, excludeKeys, false)) {
					smoothDataMap.put(s, dataMap.get(s));
					latch.countDown();
					continue;
				}
				Double average = UserDefinedFrame.avg(UserDefinedFrame.getArrayWithGreaterThanCondition(dataMap.get(s),
						dataMap.get("Slurry Rate"), 15.0));
				Executors.newSingleThreadExecutor().execute(() -> {
					smoothDataMap.put(s, smoothData(dataMap.get(s), average, s));
					latch.countDown();
				});
			}
			try {
				latch.await();
			} catch (InterruptedException e) {
				System.out.println("mainFrame::smoothDataMap");
				return dataMap;
			}
			return dataMap;
		}

		public static boolean checkForString(String stringToCheck, String[] strings, boolean caseSensitive) {
			stringToCheck = caseSensitive ? stringToCheck : stringToCheck.toLowerCase();
			for (String s : strings) {
				String string = caseSensitive ? s : s.toLowerCase();
				if (string.equals(stringToCheck)) {
					return true;
				}
			}
			return false;
		}

		public static String checkInputTimes(HashMap<String, String> inputTimes) {
			for (String s : inputTimes.keySet()) {
				if (s.equals("Check Times")) {
					return s;
				}
			}
			return "good";
		}

		public void clearTable(JTable table, int column, int... excludeRows) {
			for (int i = 0; i < table.getRowCount(); i++) {
				if (checkArrayForInt(excludeRows, i)) {
					continue;
				}
				table.setValueAt("", i, column);
			}
		}

		private boolean checkArrayForInt(int[] array, int i) {
			for (int ii : array) {
				if (i == ii) {
					return true;
				}
			}
			return false;
		}

		private static int getEndTimeIndex(ArrayList<String> timeDate, String endTime) {
			for (int i = timeDate.size() - 1; i > 1; i--) {
				Matcher matcher = Pattern.compile(endTime + ":\\d{2}").matcher(timeDate.get(i));
				if (matcher.find()) {
					return i;
				}
			}
			return timeDate.size() - 1;
		}

		private static String getHourMinute(String time) {
			Matcher matcher = Pattern.compile("\\d{2}\\:\\d{2}").matcher(time);
			if (matcher.find()) {
				return matcher.group();
			}
			return time;
		}

		private static int findIndZeroed(ArrayList<String> array) {
			int count = 0;
			for (String s : array) {

				if (count > 0 && Double.valueOf(s) < 250) {
					return count;
				}
				count++;
			}
			return count;
		}

		private static ArrayList<String> smoothTotalChannel(ArrayList<String> totalChannel, float maxSlope) {
			int start = findIndZeroed(totalChannel);
			start = start > 0 ? start : 1;

			if (correctTotalChannel(totalChannel, start)) {
				start = 1;
			}

			ArrayList<String> newArray = getZeros(start);
			for (int i = start; i < totalChannel.size(); i++) {
				if (Math.abs(Float.valueOf(totalChannel.get(i))
						- Float.valueOf(newArray.get(newArray.size() - 1))) > maxSlope) {
					Float addValue = Math
							.abs(Float.valueOf(totalChannel.get(i)) - Float.valueOf(totalChannel.get(i - 1)));
					addValue = addValue < maxSlope ? addValue + Float.valueOf(newArray.get(newArray.size() - 1))
							: Float.valueOf(newArray.get(newArray.size() - 1));
					newArray.add(String.valueOf(addValue));
					continue;
				}
				newArray.add(totalChannel.get(i));
			}
			return newArray;
		}

		private static Boolean correctTotalChannel(ArrayList<String> totalChannel, int start) {
			if (start > Float.valueOf(totalChannel.size() * .8f).intValue()) {
				return true;
			}
			return false;
		}

		private static ArrayList<String> getZeros(int numZeros) {
			ArrayList<String> array = new ArrayList<>();
			for (int i = 0; i < numZeros; i++) {
				array.add("0.0");
			}
			return array;
		}

		public static LinkedHashMap<String, ArrayList<String>> smoothChannels(
				LinkedHashMap<String, ArrayList<String>> map) {
			LinkedHashMap<String, ArrayList<String>> totals = new LinkedHashMap<>();
			ArrayList<String> clean = map.get("Clean Grand Total");
			ArrayList<String> slurry = map.get("Slurry Grand Total");
			Semaphore semaphore = new Semaphore(0);
			Executors.newSingleThreadExecutor().execute(() -> {
				totals.put("Clean Grand Total", smoothTotalChannel(clean, 30.0f));
				semaphore.release();
			});
			Executors.newSingleThreadExecutor().execute(() -> {
				totals.put("Slurry Grand Total", smoothTotalChannel(slurry, 30.0f));
				semaphore.release();
			});
			map = smoothDataMap(map, "Stage Number", "Slurry Grand Total", "Clean Grand Total", "timestamp", "Crew",
					"Date", "Job Time");
			try {
				semaphore.acquire(2);
			} catch (InterruptedException e) {
				System.out.println("NONONONO");
			}
			map.putAll(totals);
			return map;
		}

		public void setTotalAcid(ArrayList<Double> acidAmtArray) {
			String total = getTotalAcid(acidAmtArray);
			String acidName = getAcidName();
			if (acidName.equals("")) {
				return;
			}
			chemSandMap.get("chemicals").put(acidName, String.valueOf(total));
		}

		public String getAcidName() {
			String acidName = "";
			for (String s : chemSandMap.get("chemicals").keySet()) {
				if (s.toLowerCase().contains("hcl") | s.toLowerCase().contains("acid")
						&& Double.valueOf(chemSandMap.get("chemicals").get(s)) > 0.0) {
					return s;
				}
			}
			return acidName;
		}

		public void setPumpInfoInDiagnostics(SetDiagnostics setDiagnostics) {
			setDiagnostics.put("Pumps Start", getTableString(diagTable2, sigMap.get("Pumps Start"), 1));
			setDiagnostics.put("Pumps End", getTableString(diagTable2, sigMap.get("Pumps End"), 1));
			setDiagnostics.put("Pumps Blending", getTableString(diagTable2, sigMap.get("Pumps Blending"), 1));
		}

		public String getTableString(JTable table, int row, int column) {
			if (table.getValueAt(row, column) == null || table.getValueAt(row, column).toString().equals("")) {
				return "0";
			}
			return table.getValueAt(row, column).toString();
		}

		public void fixTimes(LinkedHashMap<String, ArrayList<String>> dataMap) {
			int offset = getOffset();
			ArrayList<String> newTimes = new ArrayList<>();
			for (String s : dataMap.get("timestamp")) {
				newTimes.add(String.valueOf(LocalDateTime.parse(s.replace(" ", "T")).plusSeconds(offset)));
			}
			dataMap.put("timestamp", newTimes);
		}

		public int getOffset() {
			int offset = TimeZone.getDefault().getOffset(System.currentTimeMillis())
					- TimeZone.getTimeZone("America/Chicago").getOffset(System.currentTimeMillis());
			return offset;
		}

		public static int getRealNumChannels(ArrayList<String> channels) {
			int addChannels = 0;
			for (String s : channels) {
				Matcher matcher = Pattern.compile("\\&\\&").matcher(s);
				if (matcher.find()) {
					addChannels++;
				}
			}
			return channels.size() + addChannels;
		}

		public static ArrayList<String> combineArrays(ArrayList<String> array1, ArrayList<String> array2) {
			ArrayList<String> newArray = new ArrayList<>();
			int count = 0;
			for (String s : array1) {
				double value1 = Double.parseDouble(s);
				double value2 = Double.parseDouble(array2.get(count));
				Double value = value1 + value2;
				newArray.add(String.valueOf(value));
				count++;
			}
			return newArray;
		}

		public static LinkedHashMap<String, ArrayList<String>> combineChannels(ArrayList<String> channels,
				LinkedHashMap<String, ArrayList<String>> dataMap) {
			for (String s : channels) {
				Matcher matcher = Pattern.compile("\\&\\&").matcher(s);
				if (matcher.find()) {
					String chan1 = s.substring(0, matcher.start());
					String chan2 = s.substring(matcher.end(), s.length());
					dataMap.put(s, combineArrays(dataMap.get(chan1), dataMap.get(chan2)));
				}
			}
			return dataMap;
		}

		public static String getPetroIQWellName(String fullWellName) {
			Matcher matcher = Pattern.compile("(\\d+)([A-Za-z]+)$").matcher(fullWellName);
			if (matcher.find()) {
				return fullWellName.substring(0, matcher.start()).trim();
			}
			return fullWellName;
		}

		public static void mainEvaluate(CrewRequest crewRequest, JobLogWells jobLogWells, String wellName,
				SetDiagnostics setDiagnostics, LinkedHashMap<String, String> sigMap,
				HashMap<String, LinkedHashMap<String, String>> chemSandMap, HashMap<String, String> inputTimes,
				HashMap<String, String> channelMap, HashMap<String, String> trueTimes, Semaphore requestSemaphore)
				throws Exception {
			System.out.println(setDiagnostics == null);
			jobLogWells.setSelectedWellMap(jobLogWells.getToken(), jobLogWells.getIdFromWell(wellName));
			String wellId = jobLogWells.getSelectedWellMap().get("wellId");
			System.out.println("Well ID: " + wellId);
			String operator = jobLogWells.getOperatorMap().get(wellId).get("name");
			System.out.println("Operator: " + operator);
			operator = operator == null ? "not found" : operator;
			LocalDateTime aTime = LocalDateTime.now();

			ExecutorService executor = Executors.newCachedThreadPool();
			// PREFLUSH/SHUTDOWN OPTIONS
			HashMap<String, Boolean> savedOptions = getSavedOptions();
			setDiagnostics.putAll(sigMap);
			setDiagnostics.put("Well Name", wellName);
			setDiagnostics.put("Pad Name",
					jobLogWells.getPadMap().containsKey(wellId) ? jobLogWells.getPadMap().get(wellId).get("name")
							: "No Pad Found");

			setDiagnostics.put("Formation",
					jobLogWells.getFormationMap().get(jobLogWells.getSelectedWellMap().get("formationId")));
			String crew = jobLogWells.getCrewMap().get(wellId).get("name");
			setDiagnostics.put("Crew", crew);
			System.out.println("Crew: " + crew);
			// System.out.println(holdText.getText());

			TreatmentSummary constructSummary = new TreatmentSummary(13);
			ArrayList<Integer> stageUp1 = new ArrayList<>();
			String check;
			if (!(check = checkInputTimes(inputTimes)).equals("good")) {
				JOptionPane.showMessageDialog(Main.yess, "Check " + check + " input, then click \"Run\" again");
				return;
			}
			ArrayList<String> channels = FracCalculations.getArrayOfStringValues(channelMap);
			String remCookie = RememberMe.readCookie().getCookie();
			DataRequest request = new DataRequest(crewRequest.getToken(), crewRequest.getSessionId(),
					DataRequest.getPostBody(crewRequest.getNormCrew(crew.replace(" ", "")), inputTimes.get("open"),
							inputTimes.get("close"), channels),
					getRealNumChannels(channels), remCookie);
			LinkedHashMap<String, ArrayList<String>> dataMap = null;
			try {
				requestSemaphore.acquire();
				dataMap = request.makeRequest();
				Thread.sleep(500);
				requestSemaphore.release();
				if (dataMap == null) {
					JOptionPane.showMessageDialog(null, "No data received from Intelie, re-run stage");
					return;
				}
				dataMap = combineChannels(channels, dataMap);
				dataMap = ChannelPane.getMapWithConstantKeys(dataMap, channelMap);
			} catch (IOException | InterruptedException | ExecutionException | DataFormatException e3) {
				e3.printStackTrace();
				requestSemaphore.release();
				return;
			}
			Analyze analyzeData = null;
			try {
				if (checkDataMap(dataMap)) {
					return;
				}
				if (!dataMap.containsKey("Clean Rate")) {
					dataMap.put("Clean Rate", Analyze.constructCleanRate(dataMap.get("Clean Grand Total")));
				}
				// plotArray(dataMap.get("Prop. Concentration"),"prop_con.png");
				/*
				 * plotArray(dataMap.get("Clean Grand Total"), "before_clean_grand.png");
				 * plotArray(dataMap.get("Slurry Grand Total"), "before_slurry_grand.png");
				 * plotArray(dataMap.get("Treating Pressure"),"treating_pressure.png");
				 */
				dataMap = smoothChannels(dataMap);
				int endTimeIndex = getEndTimeIndex(dataMap.get("timestamp"),
						getHourMinute(inputTimes.get(LastStartCloseComponent.CLOSE)));

				/*
				 *
				 * INCLUDE THE savedOptions MAP AS A PARAMETER IN ANY CLASS THAT CURRENTLY USES
				 * THE OPERATOR TO DETERMINE WHETHER OR NOT TO DEFINE A PREFLUSH OR INCLUDE THE
				 * SHUTDOWN/RESUME SUBSTAGES IN THE TREATMENT SUMMARY
				 *
				 */

				analyzeData = new Analyze(dataMap, operator, wellName, new HashMap<String, Integer>(), new HashMap<>(),
						false, channelMap.get("Stage Number"), endTimeIndex);
				analyzeData.analyzeMethod(savedOptions);
			} catch (IOException e1) {
				e1.printStackTrace();
				return;
			}

			setDiagnostics.putAll(analyzeData.getDiagnosticValues());

			setDiagnostics.put("Well Name", wellName);
			setDiagnostics.put("County", jobLogWells.getCountyById(wellId));
			ArrayList<String> elapTime = new ArrayList<>();
			elapTime.add(0, "Elapsed Time");

			CompletableFuture<Double> maxSand = FracCalculations.getMaxCalculation(analyzeData.getPropConc(), 0.25);

			stageUp1 = analyzeData.getDataValueIndex();

			ArrayList<Integer> totalsIndArray = analyzeData.badReset(
					analyzeData.findJobReset(analyzeData.getCleanTotal()), stageUp1, analyzeData.getAcidArray());
			StageDate stageDate = new StageDate(analyzeData.getDate(), stageUp1, inputTimes, trueTimes);

			Semaphore stageDateSem = new Semaphore(1);
			Semaphore dateDoneSem = new Semaphore(0);
			executor.execute(() -> {
				System.out.println("Start stageDate Thread");
				try {
					stageDateSem.acquire();
				} catch (InterruptedException e1) {
					System.out.println("Exception caught stageDataSem");
				}
				stageDate.run();
				dateDoneSem.release();
				System.out.println("stageDateSem Released/stageDate Thread Finished");
			});

			// setTotalAcid(acidAmtArray);
			ArrayList<String> nameArray1 = null;
			try {
				nameArray1 = analyzeData.getNameArray();
			} catch (InterruptedException | ExecutionException e1) {
				System.out.println("Exception caught mainEvaluate::analyzeData::getNameArray");
			}

			ArrayList<String> nameArray = nameArray1;
			Semaphore avgSem = new Semaphore(0);
			Semaphore stageSandSem = new Semaphore(0);
			/*
			 * plotArray(dataMap.get("Clean Grand Total"), "after_clean_grand.png");
			 * plotArray(dataMap.get("Slurry Grand Total"), "after_slurry_grand.png");
			 */
			StageTreatingPressure treatingPressure = new StageTreatingPressure(analyzeData.getTreatingPressure(),
					stageUp1, constructSummary, avgSem);
			StageSlurryRate slurryRate = new StageSlurryRate(analyzeData.getSlurryRate(), stageUp1, nameArray,
					constructSummary, avgSem);
			StageCleanTotal cleanTotal = new StageCleanTotal(analyzeData.getCleanTotal(), totalsIndArray, nameArray,
					new ArrayList<>(), dataMap.get("Slurry Rate"), constructSummary, stageSandSem);
			StageSlurryTotal slurryTotal = new StageSlurryTotal(analyzeData.getSlurryTotal(), totalsIndArray, nameArray,
					new ArrayList<>(), dataMap.get("Slurry Rate"), constructSummary);
			StagePropCon propCon = new StagePropCon(analyzeData.getPropConc(), stageUp1, constructSummary, stageSandSem,
					avgSem, analyzeData.getInputIndeces(stageUp1), 0.25);
			StageBackside stageBackside = new StageBackside(analyzeData.getBackside());
			Semaphore endStageSandSem = new Semaphore(0);

			executor.execute(treatingPressure);
			executor.execute(slurryRate);
			executor.execute(slurryTotal);
			executor.execute(cleanTotal);
			executor.execute(propCon);

			ArrayList<String> newNameArray = new ArrayList<>();
			executor.execute(() -> {
				System.out.println("stageSand Thread waiting on Semaphore x3");
				try {
					stageSandSem.acquire(2);
					// System.out.println("Start otherWorker");
					System.out.println("Start stageSand Thread");
				} catch (InterruptedException e1) {

				}

				boolean multiSand = addTotalSandToMap(chemSandMap);
				ArrayList<Long> sandVols = FracCalculations.getArrayOfLongFromMap(chemSandMap.get("sand"));
				ArrayList<String> sandTypes = FracCalculations.getArrayOfStringKeys(chemSandMap.get("sand"),
						ChemSandFrame.DUPLICATE_REGEX);

				ArrayList<Double> sandShift = getSandShift(sandVols);
				StageSand stageSand = new StageSand(sandTypes, sandVols, propCon.getAverageCon(), cleanTotal.getClean(),
						sandShift, propCon.getLastIndex(), nameArray, constructSummary);
				stageSand.Evaluate();
				if (multiSand) {
					addTotalSandToMapAbsolute(chemSandMap);
				}

				newNameArray.addAll(stageSand.getNameArray());
				System.out.println("End stageSand Thread");
				endStageSandSem.release();
			});
			try {
				setDiagnostics.put("Backside Pressure", String.valueOf(Math.round(stageBackside.getAverage())));
			} catch (NumberFormatException | InterruptedException | ExecutionException e2) {
				System.out.println("Check Backside Channel");
				JOptionPane.showMessageDialog(null, "Check your backside channel");
				return;
			}

			setDiagnostics.put("Clean Total", String.valueOf(cleanTotal.getCleanGrand()));
			setDiagnostics.put("Slurry Total", String.valueOf(slurryTotal.getSlurryGrand()));
			try {
				setDiagnostics.put("Max Prop. Concentration", String.valueOf(maxSand.get()));
			} catch (InterruptedException | ExecutionException e2) {
				setDiagnostics.put("Max Prop. Concentration", "NaNaNaN");
			}
			try {
				endStageSandSem.acquire();
			} catch (InterruptedException e1) {
			}

			System.out.println("End Stage Sand Semaphore Acquired");
			addCalculationsToDiagnostics(setDiagnostics, analyzeData);
			try {
				dateDoneSem.acquire();// System.out.println("stageDateSem acquired");
			} catch (InterruptedException e1) {
			}

			try {
				stageDate.addSigDateTimeValues(setDiagnostics, analyzeData.getNameArray());
			} catch (InterruptedException | ExecutionException e2) {
				System.out.println("Exception caught setting date/time values to setDiagnostics");
			}

			constructSummary.appendToMap(3, stageDate.getStartDates());
			constructSummary.appendToMap(0, stageDate.getStartTimes());
			constructSummary.appendToMap(4, stageDate.getEndDates());
			constructSummary.appendToMap(1, stageDate.getEndTimes());
			constructSummary.constructSubStageIndex(2);
			constructSummary.appendToMap(12, newNameArray);
			setDiagnostics.put("Acid Rate", "0");
			PumpTime pumpTime = stageDate.getPumptime();
			LinkedHashMap<String, LinkedHashMap<String, String>> chemSandCopy = new LinkedHashMap<>();
			chemSandCopy.putAll(chemSandMap);
			try {
				avgSem.acquire(3);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			setDiagnostics.putAll(chemSandMap.get("sand"));

			constructSummary.acquire();
			RemoveRow removeRow = new RemoveRow(constructSummary.getMap());
			removeRow.addSweepAfterResume();
			if (!savedOptions.get(SHUTDOWN_OPTION)) {

				ArrayList<Integer> shutdownRows = removeRow.findShutdownRows();
				if (shutdownRows.size() > 0) {

					int correction = 0;

					for (Integer r : shutdownRows) {
						removeRow.addColumn(r - correction, 6, 9);
						removeRow.deleteRow(r - correction);
						pumpTime.removeIndex(r - correction);
						correction++;
					}
					removeRow.findResumes();
					removeRow.correctSubStageIndex(2);
				}
			}
			removeRow.fixSandConc("PAD", "FLUSH", "PRE-FLUSH", "ACID");
			removeRow.fixPreSandStageProgression(false, analyzeData.getInputIndecesWithSand(stageUp1));
			removeRow.checkFlushVolume();
			System.out.println("Before UserDefinedResultsMap is set");
			System.out.println("After userDefinedResultsMap is set");

			/*
			 * executor.execute(() -> { updateSandTicketsObject(); });
			 */

			/*
			 * try { Executors.newSingleThreadExecutor() .execute(new
			 * EvaluatedData(removeRow.getMap(), chemSandMap, setDiagnostics, crew,
			 * dataMap)); } catch (IOException e1) {
			 * System.out.println("Exception caught saving data as JSON"); }
			 */

			sendEvaluatedDataObject(removeRow.getMap(), chemSandCopy, setDiagnostics, wellName,
					Integer.valueOf(setDiagnostics.get(DataNames.STAGE_NUMBER)), crew);

			System.out.println(Duration.between(aTime, LocalDateTime.now()).toMillis());
		}

		public static boolean hasDuplicates(ArrayList<String> array) {
			HashSet<String> set = new HashSet<>();
			for (String s : array) {
				set.add(s);
			}
			return set.size() != array.size();
		}

		public void mainEvaluate() throws Exception {
			operator = textField2.getText();
			SetDiagnostics setDiagnostics = new SetDiagnostics(getSigMap(diagTable2, 0));
			wellName = String.valueOf(textCombo1.getSelectedItem());
			LocalDateTime aTime = LocalDateTime.now();
			wellId = getWellId();
			tId = getTreatmentId();

			// PREFLUSH/SHUTDOWN OPTIONS
			HashMap<String, Boolean> savedOptions = getSavedOptions();

			setDiagnostics.put("Well Name", wellName);
			setDiagnostics.put("Pad Name",
					jobLogWells.getPadMap().get(jobLogWells.getIdFromWell(wellName)).get("name"));
			setDiagnostics.put("Stage Number", String.valueOf(textField1.getSelectedItem()));
			setDiagnostics.put("Formation",
					jobLogWells.getFormationMap().get(jobLogWells.getSelectedWellMap().get("formationId")));
			setDiagnostics.put("Crew", crewLabel.getText());
			perfs = getPerfs();
			String.valueOf(diagTable2.getValueAt(sigMap.get("TVD"), 1));

			executor.execute(() -> {
				System.out.println("Start Clear Thread");
				new ClearTable(mTable);
				clearTable(diagTable2, 1, sigMap.get("Pumps Start"), sigMap.get("Pumps End"),
						sigMap.get(DataNames.PUMPS_BLENDING));
				new ClearTable(diagTable1);
				new ClearTable(diagTable3);
				new ClearTable(diagTable4);
				System.out.println("End Clear Thread");
			});

			Semaphore addMapSemaphore = new Semaphore(0);
			AdditionalChannelRequest additionalDataMap = new AdditionalChannelRequest(new Semaphore(0), csrfToken,
					sessionID);
			// SEMAPHORE TO SYNCHRONIZE DATA DOWNLOADS, INTELIE DOESN'T ALLOW SIMULTANEOUS
			// DOWNLOADS
			if (additionalChannels != null && !additionalChannels.isEmpty()) {
				// MAKING REQUEST FOR DATA FROM THE ADDITIONAL CHANNELS SPECIFIED IN THE
				// USER-DEFINED FRAME
				executor.execute(() -> {
					HashMap<String, String> timeInputs = lastStartClose.getDateTimesPlusClose();
					try {
						additionalDataMap.makeRequest(
								getArrayOfStringValues(additionalChannels,
										"(" + mainFrame.removeSpecialCharacters(wellName).toLowerCase() + "|(^add))"),
								crewRequest.getNormCrew(removeSpecialCharacters(crewLabel.getText().replace(" ", ""))),
								timeInputs.get("open"), timeInputs.get("close"));
						System.out.println("addMapSemaphore released from initial AdditionalChannelRequest request");
						addMapSemaphore.release();
					} catch (InterruptedException | IOException | ExecutionException | DataFormatException
							| NullPointerException e1) {
						addMapSemaphore.release();
						System.out.println("Exception AdditionalChannelRequest::setThisMap");
					}
				});
			} else {
				addMapSemaphore.release();
				System.out.println("addMapSempahore released in else; AdditionalChannels is null or is empty");
			}
			if (isTrackingDiesel()) {
				addMapSemaphore.acquire();
				executor.execute(() -> {
					HashMap<String, String> expTimeInputs = lastStartClose.getDateTimes(0, 0);
					Double diesel = 0.0;
					try {
						diesel = getDieselUsed(expTimeInputs.get(LastStartCloseComponent.OPEN),
								expTimeInputs.get(LastStartCloseComponent.CLOSE));
						setDiagnostics.put(DataNames.DIESEL, FracCalculations.getDoubleRoundedString(diesel, 1));
						addMapSemaphore.release();
					} catch (ClassNotFoundException | InterruptedException | IOException | DataFormatException
							| ExecutionException | TimeoutException e) {
						e.printStackTrace();
						addMapSemaphore.release();
					}
				});
			}
			addMapSemaphore.acquire();
			
			System.out.println("addMapSemaphore acquired");
			ArrayList<Long> holdArray = new ArrayList<>();
			if (!holdText.getText().isBlank()) {
				for (String hold : holdText.getText().split(",")) {
					holdArray.add(Long.valueOf(hold));
				}
			}

			TreatmentSummary constructSummary = new TreatmentSummary(13);
			HashMap<String, Integer> diagnostics2 = new HashMap<>();
			removeZeroValues(stageInputs);

			Boolean pumpedAcid = getAcidSpearhead() > 0.0 ? true : false;
			stageInputs.remove("Acid_Spearhead");
			ArrayList<Integer> stageUp1 = new ArrayList<>();
			HashMap<String, String> inputTimes = lastStartClose.getDateTimes(-1, 5);
			String check;
			if (!(check = checkInputTimes(inputTimes)).equals("good")) {
				JOptionPane.showMessageDialog(Main.yess, "Check " + check + " input, then click \"Run\" again");
				return;
			}
			ArrayList<String> channels = channelPane.getActualChannels(getWellName());
			if (hasDuplicates(channels)) {
				executor.execute(() -> {
					JOptionPane.showMessageDialog(null, "Check Channels; cannot have duplicate channels in list");
				});
				return;
			}

			DataRequest request = new DataRequest(csrfToken, sessionID,
					DataRequest.getPostBody(crewRequest.getNormCrew(crewLabel.getText().replace(" ", "")),
							inputTimes.get("open"), inputTimes.get("close"), channels),
					getRealNumChannels(channels), remCookie);
			LinkedHashMap<String, ArrayList<String>> dataMap = null;

			// RAN INTO THREADING ISSUES, BROUGHT THREADING TOOLS OUTSIDE OF THE
			// CLASSES/METHODS, TO
			// SEE THE ACUIRED/RELEASED PERMITS, IN FUTURE VERSIONS, CONCEAL WITHIN
			// CLASSES/METHODS
			// checkAcquireSemaphore(addMapSemaphore);

			dataMap = request.makeRequest();
			CompletableFuture<String> blenderIDFuture = makeBlenderIDRequest(crewLabel.getText(),lastStartClose);
			if (dataMap == null) {
				JOptionPane.showMessageDialog(Main.yess, "No data received from Intelie, re-run stage");
				return;
			}
			dataMap = combineChannels(channels, dataMap);
			dataMap = channelPane.getMapWithConstantKeys(dataMap, getWellName());
			plotArray(dataMap.get(ChannelPane.SLURRY_GRAND), "slurry_grand_before.png");
			if (checkDataMap(dataMap)) {
				return;
			}

			if (!dataMap.containsKey("Clean Rate")) {
				dataMap.put("Clean Rate", Analyze.constructCleanRate(dataMap.get("Clean Grand Total")));
			}
			// plotArray(dataMap.get("Prop. Concentration"),"prop_con.png");

			dataMap = smoothChannels(dataMap);
			int endTimeIndex = getEndTimeIndex(dataMap.get("timestamp"),
					getHourMinute(lastStartClose.getTimes().get("Stage_Close")));
			plotArray(dataMap.get(ChannelPane.SLURRY_GRAND), "slurry_grand_after.png");
			/*
			 *
			 * INCLUDE THE savedOptions MAP AS A PARAMETER IN ANY CLASS THAT CURRENTLY USES
			 * THE OPERATOR TO DETERMINE WHETHER OR NOT TO DEFINE A PREFLUSH OR INCLUDE THE
			 * SHUTDOWN/RESUME SUBSTAGES IN THE TREATMENT SUMMARY
			 *
			 */

			analyzeData = new Analyze(dataMap, textField2.getText(), textCombo1.getSelectedItem().toString(),
					new HashMap<String, Integer>(), stageInputs, pumpedAcid,
					ChannelPane.getChannelList().get("Stage Number"), endTimeIndex);
			analyzeData.analyzeMethod(savedOptions);

			CompletableFuture<Boolean> chemTotals = evaluateChemTotals(ChannelPane.getChemicalMap(),
					dataMap.get("Clean Grand Total"));

			setDiagnostics.putAll(analyzeData.getDiagnosticValues());
			setDiagnostics.put("TVD", jobLogWells.getSelectedWellMap().get("trueVerticalDepth"));
			setDiagnostics.put("Perfs", perfs);
			setDiagnostics.put("Well Name", String.valueOf(textCombo1.getSelectedItem()));
			setDiagnostics.put("County", jobLogWells.getCounty(getWellName()));
			ArrayList<String> elapTime = new ArrayList<>();
			elapTime.add(0, "Elapsed Time");

			CompletableFuture<Double> maxSand = FracCalculations.getMaxCalculation(analyzeData.getPropConc(),
					sandRound);
			stageUp1 = analyzeData.getDataValueIndex();

			ArrayList<Integer> totalsIndArray = analyzeData.badReset(
					analyzeData.findJobReset(analyzeData.getCleanTotal()), stageUp1, analyzeData.getAcidArray());
			StageDate stageDate = new StageDate(analyzeData.getDate(), stageUp1, getLastStartClose());

			Semaphore stageDateSem = new Semaphore(1);
			Semaphore dateDoneSem = new Semaphore(0);
			executor.execute(() -> {
				System.out.println("Start stageDate Thread");
				try {
					stageDateSem.acquire();
				} catch (InterruptedException e1) {
					System.out.println("Exception caught stageDataSem");
				}
				stageDate.run();
				dateDoneSem.release();
				System.out.println("stageDateSem Released/stageDate Thread Finished");
			});
			setDiagnostics.put(DataNames.BLENDER_ASSET_ID, blenderIDFuture.get());
			ArrayList<Double> acidAmtArray = new ArrayList<>();
			if (getAcidSpearhead() > 0.0) {
				acidAmtArray.add(getAcidSpearhead());
			}
			acidAmtArray.addAll(acidInputs);
			// setTotalAcid(acidAmtArray);
			ArrayList<String> nameArray1 = null;
			try {
				nameArray1 = analyzeData.getNameArray();
			} catch (InterruptedException | ExecutionException e1) {
				System.out.println("Exception caught mainEvaluate::analyzeData::getNameArray");
			}

			setDiagnostics.put("Acid Volume", getTotalAcid(acidAmtArray));
			ReadDiagnosticMarkers readDiagnosticMarkers = new ReadDiagnosticMarkers(dataMap.get("Treating Pressure"),
					dataMap.get("Slurry Rate"), dataMap.get("timestamp"), dataMap.get("Clean Grand Total"));
			executor.execute(readDiagnosticMarkers);
			ArrayList<String> nameArray = nameArray1;
			Semaphore avgSem = new Semaphore(0);
			Semaphore stageSandSem = new Semaphore(0);

			StageTreatingPressure treatingPressure = new StageTreatingPressure(analyzeData.getTreatingPressure(),
					stageUp1, constructSummary, avgSem);
			StageSlurryRate slurryRate = new StageSlurryRate(analyzeData.getSlurryRate(), stageUp1, nameArray,
					constructSummary, avgSem);
			StageCleanTotal cleanTotal = new StageCleanTotal(analyzeData.getCleanTotal(), totalsIndArray, nameArray,
					acidAmtArray, dataMap.get("Slurry Rate"), constructSummary, stageSandSem);
			System.out.println("The Acid Amount Array: " + acidAmtArray);
			StageSlurryTotal slurryTotal = new StageSlurryTotal(analyzeData.getSlurryTotal(), totalsIndArray, nameArray,
					acidAmtArray, dataMap.get("Slurry Rate"), constructSummary);
			StagePropCon propCon = new StagePropCon(analyzeData.getPropConc(), stageUp1, constructSummary, stageSandSem,
					avgSem, analyzeData.getInputIndeces(stageUp1), sandRound);
			StageBackside stageBackside = new StageBackside(analyzeData.getBackside());
			Semaphore endStageSandSem = new Semaphore(0);

			executor.execute(treatingPressure);
			executor.execute(slurryRate);
			executor.execute(slurryTotal);
			executor.execute(cleanTotal);
			executor.execute(propCon);

			ArrayList<String> newNameArray = new ArrayList<>();
			executor.execute(() -> {
				System.out.println("stageSand Thread waiting on Semaphore x3");
				try {
					stageSandSem.acquire(2);
					// System.out.println("Start otherWorker");
					System.out.println("Start stageSand Thread");
				} catch (InterruptedException e1) {

				}

				boolean multiSand = addTotalSandToMap();
				ArrayList<Long> sandVols = FracCalculations.getArrayOfLongFromMap(chemSandMap.get("sand"));
				ArrayList<String> sandTypes = FracCalculations.getArrayOfStringKeys(chemSandMap.get("sand"),
						ChemSandFrame.DUPLICATE_REGEX);

				ArrayList<Double> sandShift = getSandShift(sandVols);
				StageSand stageSand = new StageSand(sandTypes, sandVols, propCon.getAverageCon(), cleanTotal.getClean(),
						sandShift, propCon.getLastIndex(), nameArray, constructSummary);
				stageSand.Evaluate();
				if (multiSand) {
					addTotalSandToMapAbsolute();
				}
				try {
					setSandInTable();
				} catch (Exception e) {
					newNameArray.addAll(stageSand.getNameArray());
					endStageSandSem.release();
					e.printStackTrace();
				}
				newNameArray.addAll(stageSand.getNameArray());
				System.out.println("End stageSand Thread");
				endStageSandSem.release();
			});
			try {
				setDiagnostics.put("Backside Pressure", String.valueOf(Math.round(stageBackside.getAverage())));
			} catch (NumberFormatException | InterruptedException | ExecutionException e2) {
				System.out.println("Check Backside Channel");
				JOptionPane.showMessageDialog(null, "Check your backside channel");
				return;
			}
			setDiagnostics(diagnostics2);
			try {
				setDiagnostics.setGraphDiagnostics(readDiagnosticMarkers.getMarkers());
			} catch (InterruptedException e3) {
				System.out.println("NONOONOON");
				return;
			}

			setDiagnostics.put("Clean Total", String.valueOf(cleanTotal.getCleanGrand()));
			setDiagnostics.put("Slurry Total", String.valueOf(slurryTotal.getSlurryGrand()));
			try {
				setDiagnostics.put("Max Prop. Concentration", String.valueOf(maxSand.get()));
			} catch (InterruptedException | ExecutionException e2) {
				setDiagnostics.put("Max Prop. Concentration", "NaNaNaN");
			}
			try {
				endStageSandSem.acquire();
			} catch (InterruptedException e1) {
			}

			System.out.println("End Stage Sand Semaphore Acquired");
			addCalculationsToDiagnostics(setDiagnostics, analyzeData);

			try {
				dateDoneSem.acquire();// System.out.println("stageDateSem acquired");
			} catch (InterruptedException e1) {

			}

			try {
				stageDate.addSigDateTimeValues(setDiagnostics, analyzeData.getNameArray());
			} catch (InterruptedException | ExecutionException e2) {
				System.out.println("Exception caught setting date/time values to setDiagnostics");
			}

			constructSummary.appendToMap(3, stageDate.getStartDates());
			constructSummary.appendToMap(0, stageDate.getStartTimes());
			constructSummary.appendToMap(4, stageDate.getEndDates());
			constructSummary.appendToMap(1, stageDate.getEndTimes());
			constructSummary.constructSubStageIndex(2);
			constructSummary.appendToMap(12, newNameArray);
			setDiagnostics.put("Acid Rate", slurryRate.getAcidRate());
			PumpTime pumpTime = stageDate.getPumptime();
			LinkedHashMap<String, LinkedHashMap<String, String>> chemSandCopy = new LinkedHashMap<>();
			getCalculatedChemTotals(chemTotals);
			chemSandCopy.putAll(chemSandMap);
			try {
				avgSem.acquire(3);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			setDiagnostics.putAll(chemSandMap.get("sand"));

			constructSummary.acquire();
			RemoveRow removeRow = new RemoveRow(constructSummary.getMap());
			removeRow.addSweepAfterResume();
			if (!savedOptions.get(SHUTDOWN_OPTION)) {

				analyzeData.getDataValueIndex().size();
				ArrayList<Integer> shutdownRows = removeRow.findShutdownRows();
				if (shutdownRows.size() > 0) {

					int correction = 0;

					for (Integer r : shutdownRows) {
						removeRow.addColumn(r - correction, 6, 9);
						removeRow.deleteRow(r - correction);
						pumpTime.removeIndex(r - correction);
						correction++;
					}
					removeRow.findResumes();
					removeRow.correctSubStageIndex(2);
				}
			}
			setChemicalsInTable(setDiagnostics.get(DataNames.ACID_VOLUME));

			removeRow.fixSandConc("PAD", "FLUSH", "PRE-FLUSH", "ACID");
			removeRow.fixPreSandStageProgression(pumpedAcid, analyzeData.getInputIndecesWithSand(stageUp1));
			removeRow.checkFlushVolume();
			System.out.println("Before UserDefinedResultsMap is set");
			runUserDefinedFunctions(setDiagnostics, dataMap, additionalDataMap, constructSummary);
			System.out.println("After userDefinedResultsMap is set");
			removeExtraPreSandStages(removeRow.getArray(12));
			setDiagnostics.setUserDefinedResults(userDefinedMap);
			setDiagnostics.writeMapToTable(diagTable2);
			removeRow.writeToTable(Main.yess.getmTable());
			holdText.setText("");
			// Set the fields for total clean and slurry volumes
			diagTable4.setValueAt(cleanTotal.getCleanGrand(), 0, 0);
			diagTable4.setValueAt(slurryTotal.getSlurryGrand(), 0, 1);

			/*
			 * executor.execute(() -> { updateSandTicketsObject(); });
			 */

			try {
				Executors.newSingleThreadExecutor().execute(new EvaluatedData(removeRow.getMap(), chemSandMap,
						setDiagnostics, crewLabel.getText(), dataMap));
			} catch (IOException e1) {
				System.out.println("Exception caught saving data as JSON");
			}

			sendEvaluatedDataObject(removeRow.getMap(), chemSandCopy, setDiagnostics, wellName,
					Integer.valueOf(setDiagnostics.get(DataNames.STAGE_NUMBER)));

			clickedRun = true;
			System.out.println(Duration.between(aTime, LocalDateTime.now()).toMillis());
		}

		private Boolean getCalculatedChemTotals(CompletableFuture<Boolean> chemTotals) {
			Boolean done = false;
			try {
				done = chemTotals.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
				return false;
			}
			return done;
		}

		private void runUserDefinedFunctions(SetDiagnostics setDiagnostics, HashMap<String, ArrayList<String>> dataMap,
				AdditionalChannelRequest additionalDataMap, TreatmentSummary constructSummary) {
			CompletableFuture<HashMap<String, String>> userDefinedResultsMap = null;
			if (userDefinedMap != null && !userDefinedMap.isEmpty()) {

				userDefinedResultsMap = userDefinedCalculations(dataMap, setDiagnostics,
						(additionalChannels != null && !additionalChannels.isEmpty() ? additionalDataMap.getThisMap()
								: (new LinkedHashMap<String, ArrayList<String>>())),
						getSummaryMapWithNamedKeys(constructSummary));

				try {
					setUserDefinedInDiagnostics(setDiagnostics, userDefinedResultsMap.get());// 10000,
																								// TimeUnit.MILLISECONDS
				} catch (InterruptedException | ExecutionException e) {// | TimeoutException e) {
					System.out.println("Exception caught setting User-Defined Map");
					return;
				}
			}
		}

		private void sendEvaluatedDataObject(HashMap<Integer, ArrayList<String>> summaryMap,
				HashMap<String, LinkedHashMap<String, String>> chemSandMap,
				LinkedHashMap<String, String> setDiagnostics, String wellName, Integer stage) {
			Executors.newSingleThreadExecutor().execute(() -> {
				EvaluatedDataObject evaluatedDataObjectGross = null;
				try {
					String address = readStringObjFromFile(SERVER_ADDRESS_FILENAME, SERVER_ADDRESS_DEFAULT);
					EvaluatedDataObject evaluatedDataObject = new EvaluatedDataObject();
					evaluatedDataObject.addToMaps(summaryMap, chemSandMap, setDiagnostics, wellName, stage);
					Main.yess.sendDataFileSocket(evaluatedDataObject, address);
					evaluatedDataObjectGross = EvaluatedDataObject.getFromFile();
					evaluatedDataObjectGross.addToMaps(summaryMap, chemSandMap, setDiagnostics, wellName, stage);
					EvaluatedDataObject.writeToFile(evaluatedDataObjectGross);
				} catch (ClassNotFoundException | IOException e) {
					System.out.println("Exception caught SerializedData");
					try {
						evaluatedDataObjectGross = EvaluatedDataObject.getFromFile();
						evaluatedDataObjectGross.addToMaps(summaryMap, chemSandMap, setDiagnostics, wellName, stage);
						EvaluatedDataObject.writeToFile(evaluatedDataObjectGross);
					} catch (ClassNotFoundException | IOException e2) {
						e2.printStackTrace();
					}
					e.printStackTrace();
					return;
				}
			});
		}

		/*
		 *
		 *
		 *
		 * UNCOMMENT THIS OUT
		 * BELOW!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		 * !!!!!!!!!!!!!!!!!
		 *
		 *
		 *
		 *
		 *
		 *
		 */
		private static void sendEvaluatedDataObject(HashMap<Integer, ArrayList<String>> summaryMap,
				HashMap<String, LinkedHashMap<String, String>> chemSandMap,
				LinkedHashMap<String, String> setDiagnostics, String wellName, Integer stage, String crew) {
			EvaluatedDataObject evaluatedDataObjectGross = null;
			try {
				String address = SERVER_ADDRESS_DEFAULT;// readStringObjFromFile(SERVER_ADDRESS_FILENAME,
														// SERVER_ADDRESS_DEFAULT);
				EvaluatedDataObject evaluatedDataObject = new EvaluatedDataObject();
				evaluatedDataObject.addToMaps(summaryMap, chemSandMap, setDiagnostics, wellName, stage);
				mainFrame.sendDataFileSocket(evaluatedDataObject, crew, address);

				evaluatedDataObjectGross = EvaluatedDataObject.getFromFile();
				evaluatedDataObjectGross.addToMaps(summaryMap, chemSandMap, setDiagnostics, wellName, stage);
				EvaluatedDataObject.writeToFile(evaluatedDataObjectGross);

			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
				System.out.println("Exception caught SerializedData");
				/*
				 * try { evaluatedDataObjectGross = EvaluatedDataObject.getFromFile();
				 * evaluatedDataObjectGross.addToMaps(summaryMap, chemSandMap, setDiagnostics,
				 * wellName, stage); EvaluatedDataObject.writeToFile(evaluatedDataObjectGross);
				 * } catch (ClassNotFoundException | IOException e2) { e2.printStackTrace(); }
				 * e.printStackTrace();
				 */
				return;
			}
		}

		public ArrayList<String> copyArray(ArrayList<String> array) {
			ArrayList<String> newArray = new ArrayList<>();
			newArray.addAll(array);
			return newArray;
		}

		public void sendDataFileSocket() throws UnknownHostException, IOException, ClassNotFoundException {
			String crewName = crewLabel.getText();
			byte[] crewBytes = crewName.getBytes();
			Socket socket = new Socket("10.119.224.55", 80);
			socket.setSoTimeout(60000);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
			EvaluatedDataObject evaluatedDataObject = EvaluatedDataObject.getFromFile();
			objectOutputStream.writeInt(crewBytes.length);
			objectOutputStream.write(crewBytes);
			String info = getClientInfo();
			byte[] infoBytes = info.getBytes();
			objectOutputStream.writeInt(infoBytes.length);
			objectOutputStream.write(infoBytes);
			objectOutputStream.writeObject(evaluatedDataObject);
			checkForDoneMessage(socket);
			System.out.println("socket disconnected");
			objectOutputStream.close();
		}

		public static class GetEvaluatedData implements Serializable {

			private static final long serialVersionUID = 1L;

			static EvaluatedDataObject getObject() throws ClassNotFoundException, IOException {
				if (!checkFile()) {
					return new EvaluatedDataObject();
				}
				FileInputStream fileInputStream = new FileInputStream("C:\\Scrape\\data.his");
				ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
				EvaluatedDataObject evaluatedDataObject = (EvaluatedDataObject) objectInputStream.readObject();
				objectInputStream.close();
				fileInputStream.close();
				return evaluatedDataObject;
			}

			static Boolean checkFile() {
				File file = new File("C:\\Scrape\\data.his");
				return file.exists();
			}

			static void writeObject(EvaluatedDataObject evaluatedDataObject) throws IOException {
				FileOutputStream fileOutputStream = new FileOutputStream(new File("C:\\Scrape\\data.his"));
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
				objectOutputStream.writeObject(evaluatedDataObject);
				objectOutputStream.close();
				fileOutputStream.close();
			}
		}

		private Boolean checkTimes() {
			if (Duration.between(LocalDateTime.parse(lastStartClose.getDateTimes().get("close"),
					DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), LocalDateTime.now()).getSeconds() < 0) {
				return false;
			}

			return true;
		}

		private static ArrayList<Double> getSandShift(ArrayList<Long> sandVols) {
			ArrayList<Double> shiftArray = new ArrayList<>();
			double total = sandVols.get(0);
			double sumSand = 0.0;
			for (int i = 1; i < sandVols.size(); i++) {
				sumSand += Double.valueOf(sandVols.get(i));
				shiftArray.add(sumSand / total);
			}
			return shiftArray;
		}

		private void addTotalSandToMapAbsolute() {
			Double totalSand = 0.0;
			LinkedHashMap<String, String> tempTypeMap = new LinkedHashMap<>();
			for (String s : chemSandMap.get("sand").keySet()) {
				if (s.toLowerCase().contains("total")) {
					continue;
				}
				Matcher matcher = Pattern.compile("[\\d\\.]+").matcher(chemSandMap.get("sand").get(s));
				Double typeTotal = 0.0;
				while (matcher.find()) {
					String found = matcher.group();
					totalSand += Double.valueOf(found);
					typeTotal += Double.valueOf(found);
				}
				tempTypeMap.put(s, String.valueOf(Math.round(typeTotal)));
			}
			LinkedHashMap<String, String> tempMap = new LinkedHashMap<>();
			tempMap.put("Total Proppant (lbm)", String.valueOf(Math.round(totalSand)));
			tempMap.putAll(tempTypeMap);
			chemSandMap.put("sand", tempMap);
		}

		private static void addTotalSandToMapAbsolute(HashMap<String, LinkedHashMap<String, String>> chemSandMap) {
			Double totalSand = 0.0;
			LinkedHashMap<String, String> tempTypeMap = new LinkedHashMap<>();
			for (String s : chemSandMap.get("sand").keySet()) {
				if (s.toLowerCase().contains("total")) {
					continue;
				}
				Matcher matcher = Pattern.compile("[\\d\\.]+").matcher(chemSandMap.get("sand").get(s));
				Double typeTotal = 0.0;
				while (matcher.find()) {
					String found = matcher.group();
					totalSand += Double.valueOf(found);
					typeTotal += Double.valueOf(found);
				}
				tempTypeMap.put(s, String.valueOf(Math.round(typeTotal)));
			}
			LinkedHashMap<String, String> tempMap = new LinkedHashMap<>();
			tempMap.put("Total Proppant (lbm)", String.valueOf(Math.round(totalSand)));
			tempMap.putAll(tempTypeMap);
			chemSandMap.put("sand", tempMap);
		}

		private boolean addTotalSandToMap() {
			Double totalSand = 0.0;
			boolean multiSand = false;
			for (String s : chemSandMap.get("sand").keySet()) {
				Matcher matcher = Pattern.compile("[\\d\\.]+").matcher(chemSandMap.get("sand").get(s));
				int count = 0;
				while (matcher.find()) {
					if (count == 0) {
						totalSand += Double.valueOf(matcher.group());
					} else {
						multiSand = true;
					}
					count++;
				}
			}
			LinkedHashMap<String, String> tempMap = new LinkedHashMap<>();
			tempMap.put("Total Proppant (lbm)", String.valueOf(Math.round(totalSand)));
			tempMap.putAll(chemSandMap.get("sand"));
			chemSandMap.put("sand", tempMap);
			return multiSand;
		}

		private static boolean addTotalSandToMap(HashMap<String, LinkedHashMap<String, String>> chemSandMap) {
			Double totalSand = 0.0;
			boolean multiSand = false;
			for (String s : chemSandMap.get("sand").keySet()) {
				Matcher matcher = Pattern.compile("[\\d\\.]+").matcher(chemSandMap.get("sand").get(s));
				int count = 0;
				while (matcher.find()) {
					if (count == 0) {
						totalSand += Double.valueOf(matcher.group());
					} else {
						multiSand = true;
					}
					count++;
				}
			}
			LinkedHashMap<String, String> tempMap = new LinkedHashMap<>();
			tempMap.put("Total Proppant (lbm)", String.valueOf(Math.round(totalSand)));
			tempMap.putAll(chemSandMap.get("sand"));
			chemSandMap.put("sand", tempMap);
			return multiSand;
		}

		public Boolean checkUserDefinedNeeds(HashMap<String, HashMap<String, String>> userDefinedMap) {
			boolean addSummary = false;
			addSummaryTrue: for (String s : DataNames.getSummaryColumnNames()) {
				for (String ss : userDefinedMap.keySet()) {
					if (userDefinedMap.get(ss).get("Definition").contains(s)) {
						addSummary = true;
						break addSummaryTrue;
					}
				}
			}
			return addSummary;
		}

		private void removeExtraPreSandStages(ArrayList<String> namesArray) {
			ArrayList<String> summaryKeys;
			if ((summaryKeys = getUserDefinedSummaryKeys()) != null) {
				for (String s : summaryKeys) {
					// HashMap<String, String> tempMap = new HashMap<>();
					// tempMap.put("result",
					// RemoveRow.fixUserDefinedSand(namesArray,
					// userDefinedMap.get(s).get("result")));
					userDefinedMap.get(s).put("result",
							RemoveRow.fixUserDefinedSandNumStages(namesArray, userDefinedMap.get(s).get("result")));
				}
			}
		}

		private ArrayList<String> getUserDefinedSummaryKeys() {
			if (userDefinedMap == null || userDefinedMap.isEmpty()) {
				return null;
			}
			ArrayList<String> summaryKeys = new ArrayList<>();
			for (String s : userDefinedMap.keySet()) {
				if (s.contains("@")) {
					summaryKeys.add(s);
				}
			}
			return summaryKeys;
		}

		public void addRemoveSandAmts(HashMap<Integer, Long> sandAmtMap, ArrayList<Long> sandAmt) {
			for (Integer i : sandAmtMap.keySet()) {
				sandAmt.remove(i.intValue());
				sandAmt.add(sandAmtMap.get(i));
			}
		}

		@Deprecated
		public void addCalculationsToDiagnostics(SetDiagnostics setDiagnostics) {
			checkNullKeyValue(setDiagnostics, "ISIP");
			checkNullKeyValue(setDiagnostics, "TVD");
			checkNullKeyValue(setDiagnostics, DataNames.ISIP_TIME);
			checkNullKeyValue(setDiagnostics, DataNames.CLOSE_PRESSURE);
			setDiagnostics.put("auto_isip", setDiagnostics.get("auto_isip"));
			setDiagnostics.put("auto_close", setDiagnostics.get("auto_close"));
			setDiagnostics.put("Average Horsepower",
					FracCalculations.getDoubleRoundedString(
							FracCalculations.calculateHorsePower(Double.valueOf(setDiagnostics.get("Average Pressure")),
									Double.valueOf(setDiagnostics.get("Average Rate"))),
							2));
			setDiagnostics.put("Frac Gradient",
					FracCalculations.getDoubleRoundedString(FracCalculations.calculateFracGradient(
							Double.valueOf(setDiagnostics.get("TVD")), Double.valueOf(setDiagnostics.get("ISIP"))), 2));
		}

		private void checkNullKeyValue(SetDiagnostics setDiagnostics, String key) {
			if (setDiagnostics.get(key) == null) {
				setDiagnostics.put(key, "0");
			} else if (key.equals("ISIP") & setDiagnostics.get(key).equals("0")) {
				setDiagnostics.put(key, String.valueOf(analyzeData.getDiagnosticValues().get("auto_isip")));
			}

			if (key.equals(DataNames.CLOSE_PRESSURE) && setDiagnostics.get(DataNames.CLOSE_PRESSURE).equals("0")) {
				setDiagnostics.put(DataNames.CLOSE_PRESSURE,
						String.valueOf(analyzeData.getDiagnosticValues().get("auto_close")));
			}
		}

		private static void addCalculationsToDiagnostics(SetDiagnostics setDiagnostics, Analyze analyzeData) {
			checkNullKeyValue(setDiagnostics, "ISIP", analyzeData);
			checkNullKeyValue(setDiagnostics, "TVD", analyzeData);
			checkNullKeyValue(setDiagnostics, DataNames.ISIP_TIME, analyzeData);
			checkNullKeyValue(setDiagnostics, DataNames.CLOSE_PRESSURE, analyzeData);
			setDiagnostics.put("auto_isip", String.valueOf(analyzeData.getDiagnosticValues().get(Analyze.AUTO_ISIP)));
			setDiagnostics.put("auto_close", String.valueOf(analyzeData.getDiagnosticValues().get(Analyze.AUTO_CLOSE)));
			setDiagnostics.put("auto_isip_time",
					String.valueOf(analyzeData.getDiagnosticValues().get(Analyze.AUTO_ISIP_TIME)));
			setDiagnostics.put("Average Horsepower",
					FracCalculations.getDoubleRoundedString(
							FracCalculations.calculateHorsePower(Double.valueOf(setDiagnostics.get("Average Pressure")),
									Double.valueOf(setDiagnostics.get("Average Rate"))),
							2));
			setDiagnostics.put("Frac Gradient",
					FracCalculations.getDoubleRoundedString(FracCalculations.calculateFracGradient(
							Double.valueOf(setDiagnostics.get("TVD")), Double.valueOf(setDiagnostics.get("ISIP"))), 2));
		}

		private static void checkNullKeyValue(SetDiagnostics setDiagnostics, String key, Analyze analyzeData) {
			if (!setDiagnostics.containsKey(key) || setDiagnostics.get(key) == null) {
				setDiagnostics.put(key, "0");
			}
			if (!setDiagnostics.get(key).equals("0")) {
				return;
			}
			switch (key) {
			case (DataNames.ISIP):
				setDiagnostics.put(key, String.valueOf(analyzeData.getDiagnosticValues().get(Analyze.AUTO_ISIP)));
				break;
			case (DataNames.CLOSE_PRESSURE):
				setDiagnostics.put(key, String.valueOf(analyzeData.getDiagnosticValues().get(Analyze.AUTO_CLOSE)));
				break;
			case (DataNames.ISIP_TIME):
				setDiagnostics.put(key, String.valueOf(analyzeData.getDiagnosticValues().get(Analyze.AUTO_ISIP_TIME)));
				break;
			}
		}

		public void writeSandToTable(ArrayList<Long> sandArray) {
			int i = 0;
			for (Long l : sandArray) {
				diagTable3.setValueAt(l, i, 1);
				i++;
			}
		}

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (topPanel.getBounds().getMaxX() != thisFrame.getBounds().getMaxX()) {
			/*
			 * topPanel.setBounds(0,0,(int) thisFrame.getBounds().getMaxX(),100);
			 * botPanel.setBounds(0,100,(int) thisFrame.getBounds().getMaxX(),(int)
			 * thisFrame.getBounds().getMaxY() - 100);
			 * mTable.setAlignmentX(CENTER_ALIGNMENT);
			 */
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {

		if (topPanel.getBounds().getMaxX() != thisFrame.getBounds().getMaxX()) {

		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (topPanel.getBounds().getMaxX() != thisFrame.getBounds().getMaxX()) {
			/*
			 * topPanel.setBounds(0,0,(int) thisFrame.getBounds().getMaxX(),100);
			 * botPanel.setBounds(0,100,(int) thisFrame.getBounds().getMaxX(),(int)
			 * thisFrame.getBounds().getMaxY() - 100);
			 * mTable.setAlignmentX(CENTER_ALIGNMENT);
			 */
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (topPanel.getBounds().getMaxX() != thisFrame.getBounds().getMaxX()) {
			/*
			 * topPanel.setBounds(0,0,(int) thisFrame.getBounds().getMaxX(),100);
			 * botPanel.setBounds(0,100,(int) thisFrame.getBounds().getMaxX(),(int)
			 * thisFrame.getBounds().getMaxY() - 100);
			 * mTable.setAlignmentX(CENTER_ALIGNMENT);
			 */
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if (topPanel.getBounds().getMaxX() != thisFrame.getBounds().getMaxX()) {
			/*
			 * topPanel.setBounds(0,0,(int) thisFrame.getBounds().getMaxX(),100);
			 * botPanel.setBounds(0,100,(int) thisFrame.getBounds().getMaxX(),(int)
			 * thisFrame.getBounds().getMaxY() - 100);
			 * mTable.setAlignmentX(CENTER_ALIGNMENT);
			 */
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (topPanel.getBounds().getMaxX() != thisFrame.getBounds().getMaxX()) {
			/*
			 * topPanel.setBounds(0,0,(int) thisFrame.getBounds().getMaxX(),100);
			 * botPanel.setBounds(0,100,(int) thisFrame.getBounds().getMaxX(),(int)
			 * thisFrame.getBounds().getMaxY() - 100);
			 * mTable.setAlignmentX(CENTER_ALIGNMENT);
			 */
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (topPanel.getBounds().getMaxX() != thisFrame.getBounds().getMaxX()) {
			/*
			 * topPanel.setBounds(0,0,(int) thisFrame.getBounds().getMaxX(),100);
			 * botPanel.setBounds(0,100,(int) thisFrame.getBounds().getMaxX(),(int)
			 * thisFrame.getBounds().getMaxY() - 100);
			 * mTable.setAlignmentX(CENTER_ALIGNMENT);
			 */
		}
	}

	public void removeStages() {

		SwingWorker<Void, Integer> worker = new SwingWorker<>() {
			Boolean first = true;

			public Void doInBackground() {
				int i;
				if (textField1.getItemCount() > 1) {
					for (i = 1; i < textField1.getItemCount(); i++) {
						publish(i);
					}
				}

				return null;
			}

			@SuppressWarnings("unused")
			public void process(List<Integer> chunks) {
				if (first) {
					textField1.setSelectedIndex(0);
					first = false;
				}
				for (Integer i : chunks) {
					textField1.removeItemAt(1);
				}
			}
		};
		worker.execute();

	}

	private String[] getArrayFromDSV(String dsv, String delimiter) {
		String[] array = new String[dsv.split(delimiter).length];
		int i = 0;
		for (String s : dsv.split(delimiter)) {
			array[i] = s;
			i++;
		}
		return array;
	}

	private void setSandTicketsObject(String padName) {
		try {
			sandTicketsObject = SandTicketsObject.readFromFile(padName);
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

	private void writeSigNamesToTable(LinkedHashMap<String, ?> map, JTable table) {
		int i = 0;
		for (String s : DataNames.getDataNamesForTable()) {
			table.setValueAt(s, i, 0);
			i++;
		}
		if (map == null || map.isEmpty()) {
			return;
		}
		for (String s : map.keySet()) {
			table.setValueAt(s, i, 0);
			i++;
		}
	}

	public final static String OPERATOR_PLACE_HOLDER = "<Operator>";

	public class update extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		ArrayList<String> bb;

		@Override
		public void actionPerformed(ActionEvent e) {
			if (clickedRun) {
				clearInputValues();
				clickedRun = false;
			}
			Semaphore opSemaphore = new Semaphore(0);
			if (!button.isEnabled()) {
				button.setEnabled(true);
			}
			wellName = textCombo1.getSelectedItem().toString();
			if (wellName.equals("-")) {
				return;
			}
			setSandTicketsObject(jobLogWells.getPadMap().get(jobLogWells.getIdFromWell(wellName)).get("name"));
			// bb = well.getTreatmentList(textCombo1.getSelectedItem().toString());

			textField1
					.removeActionListener(textField1.getActionListeners()[textField1.getActionListeners().length - 1]);
			textField1.setSelectedIndex(0);
			removeStages();
			textField1.addActionListener(new update1());

			Executors.newSingleThreadExecutor().execute(() -> {
				String operator = jobLogWells.getOperator(wellName);
				setOperator(operator);
				try {
					jobLogWells.setSelectedWellMap(jobLogWells.getToken(), jobLogWells.getIdFromWell(wellName));
				} catch (InterruptedException | IOException e1) {
					System.out.println("NONONONONONONONO");
				}
				opSemaphore.release();
				SwingWorker<Void, String> worker = new SwingWorker<>() {
					public Void doInBackground() {
						String crew = jobLogWells.getCrewMap().get(jobLogWells.getIdFromWell(wellName)).get("name");

						publish(crew);
						return null;
					}

					public void process(List<String> chunks) {
						String crew = chunks.get(0);
						if (crew.contains(",")) {
							String[] crews = getArrayFromDSV(crew, ",");
							for (String s : crews) {
								System.out.println(s);
							}
							crew = (String) JOptionPane.showInputDialog(null, "Select a channel", "Channel Selection",
									JOptionPane.PLAIN_MESSAGE, null, crews, crews[0]);
						}
						crewLabel.setText(crew);
						try {
							ArgumentsToText.writeSingleLineToText(crew, "C:\\Scrape\\crew.txt");
						} catch (IOException e) {
							System.out.println("NONONONO");
						}
					}
				};
				worker.execute();

				try {
					ReadOperatorTemplate.readTemplateValueNames(operator);
				} catch (IOException e1) {

				}
			});
			Executors.newSingleThreadExecutor().execute(() -> {
				try {
					opSemaphore.acquire();
					userDefinedMap = UserDefinedFrame.readUserDefinedDefinitions(getOperator(), wellName);
					ClearTable.clearTable(diagTable2);
					writeSigNamesToTable(userDefinedMap, diagTable2);
					saveUserDefinedData();
					textField3.setText(
							jobLogWells.getFormationMap().get(jobLogWells.getSelectedWellMap().get("formationId")));
					textField3.repaint();
					// System.out.println(userDefinedMap);
				} catch (IOException | InterruptedException e2) {
					e2.printStackTrace();
				}
			});
			// textField3.setText(jobLogWells.getWellMap().get(wellName).get("formation"));
			// System.out.println(textCombo1.getSelectedItem().toString());
			// wellId = well.getWellId(String.valueOf(textCombo1.getSelectedItem()));
			textField2.setText(getOperator());
			new ArgumentsToText(getOperator(), "C:\\Scrape\\ScrapePython\\Operator.txt", "\n");
			updateStageList(activePerfsMap.get(jobLogWells.getIdFromWell(wellName)).get("stages"));
			try {

				setSigMap(ReadOperatorTemplate.readTemplateValueNames(getOperator()));

			} catch (IOException e1) {
				setSigMap(null);
			}

		}

		class UpdateProducerConsumer {
			Boolean stageSet;
			String tempStage;
			Boolean finished;
			int i;

			UpdateProducerConsumer() {
				stageSet = false;
				finished = false;
			}

			public synchronized void Producer() {

				for (i = 0; i < bb.size(); i++) {
					while (stageSet) {
						try {
							wait();
						} catch (InterruptedException e) {
							System.out.println("updateProducer Interrupted +" + "Exception");
						}
					}
					this.tempStage = bb.get(i);
					stageSet = true;
					notify();
				}
				finished = true;
			}

			public synchronized void Consumer() {
				while (!finished) {
					while (!stageSet) {
						try {
							wait();
						} catch (InterruptedException e) {
							System.out.println("updateConsumer InterruptedException");
						}
					}
					textField1.addItem(this.tempStage);
					stageSet = false;
					notify();
				}
			}
		}
	}

	private void updateStageList(ArrayList<String> stages) {
		for (String s : stages) {
			textField1.addItem(s);
		}
	}

	public void saveUserDefinedData() {
		if (userDefinedMap == null || userDefinedMap.isEmpty()) {
			return;
		}
		LinkedHashMap<String, String> addUpdateMap = new LinkedHashMap<>();
		for (String s : userDefinedMap.keySet()) {
			String definition = userDefinedMap.get(s).get("Definition");
			Matcher matcher = Pattern.compile("add\\:\\:([\\(\\)\\w\\-\\s\\d]+?)\\]").matcher(definition);
			Matcher matcherParameter = Pattern.compile("\\:([\\(\\)\\w\\-\\s\\d]+?)\\]").matcher(definition);
			while (matcher.find()) {
				matcherParameter.find();
				String foundParam = matcherParameter.group();
				String channelName = foundParam.substring(1, foundParam.length() - 1);
				System.out.println(channelName);
				String found = matcher.group();

				addUpdateMap.put(found.substring(0, found.length() - 1), channelName);
				// System.out.println("Additional Channel Name: " + channelName + " CName: " +
				// ch.getChannels().getoCName().get(channelName));
			}
		}
		setAdditionalChannelsMap(addUpdateMap);
	}

	public ArrayList<String> getArrayOfStringValues(Map<String, String> map) {
		ArrayList<String> array = new ArrayList<>();
		for (String s : map.keySet()) {
			array.add(map.get(s));
		}
		return array;
	}

	public ArrayList<String> getArrayOfStringValues(Map<String, String> map, String keyRegex) {
		ArrayList<String> array = new ArrayList<>();
		for (String s : map.keySet()) {
			Matcher matcher = Pattern.compile(keyRegex).matcher(s);
			if (matcher.find()) {
				array.add(map.get(s));
			}
		}
		return array;
	}

	public class update1 extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			SwingWorker<Void, Void> worker = new SwingWorker<>() {
				public Void doInBackground() {
					button.setEnabled(false);

					String selectedString = textField1.getSelectedItem().toString();
					if (selectedString.equals("<Stage Number>")) {
						button.setEnabled(true);
						return null;
					}

					setPerfs(getCombinedPerfs(selectedString));
					Main.yess.diagTable2.setValueAt(jobLogWells.getSelectedWellMap().get("trueVerticalDepth"),
							sigMap.get("TVD"), 1);
					setTVD(jobLogWells.getSelectedWellMap().get("trueVerticalDepth"));
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {

							String wellName = getWellName();
							// String coState = well.getCounty(wellName) +","+well.getState(wellName);
							String cmEmails = "";
							for (String a : emailFrame.getEmails(1)) {
								if (a.trim().equals("")) {
									continue;
								}
								cmEmails += ";" + a;
							}
							cmEmails = cmEmails.substring(1);
							try {
								new StageArguments(ReadDirectory.readDirect(), wellName.toUpperCase(),
										String.valueOf(textField1.getSelectedItem()),
										jobLogWells.getCounty(getWellName()),
										jobLogWells.getFormationMap()
												.get(jobLogWells.getSelectedWellMap().get("formationId")),
										emailFrame.getMyEmail(), cmEmails, wellDrivePane.getUsername(),
										wellDrivePane.getPassword());
							} catch (HeadlessException | IOException e1) {
								try {
									new TextLog("Company Man Emails - line 633");
								} catch (IOException e2) {
								}
							}
						}
					});
					t.start();
					button.setEnabled(true);
					return null;
				}
			};
			worker.execute();
		}
	}

	public String getCombinedPerfs(String stage) {
		HashMap<String, ArrayList<String>> wellPerfsStages = jobLogWells.getActivePerfsMap()
				.get(jobLogWells.getIdFromWell(wellName));
		Integer perfIndex = wellPerfsStages.get("stages").indexOf(stage);
		String topPerf = wellPerfsStages.get("topPerfs").get(perfIndex);
		String botPerf = wellPerfsStages.get("bottomPerfs").get(perfIndex);
		return topPerf + "-" + botPerf;
	}

	public static String getCombinedPerfs(String stage, JobLogWells jobLogWells, String wellName) {
		HashMap<String, ArrayList<String>> wellPerfsStages = jobLogWells.getActivePerfsMap()
				.get(jobLogWells.getIdFromWell(wellName));
		Integer perfIndex = wellPerfsStages.get("stages").indexOf(stage);
		String topPerf = wellPerfsStages.get("topPerfs").get(perfIndex);
		String botPerf = wellPerfsStages.get("bottomPerfs").get(perfIndex);
		return topPerf + "-" + botPerf;
	}

	private void clearWellList(Semaphore wellSemaphore) {
		SwingWorker<Void, Integer> worker = new SwingWorker<>() {
			public Void doInBackground() {

				for (int i = 0; i < textCombo1.getItemCount() - 1; i++) {
					publish(1);
				}

				return null;
			}

			@SuppressWarnings("unused")
			public void process(List<Integer> chunks) {
				if (textCombo1.isEnabled()) {

					textField1.setSelectedItem("<Stage Number>");
					// textCombo1.setEnabled(false);
				}
				for (Integer i : chunks) {
					textCombo1.removeItemAt(1);
				}
			}

			public void done() {
				wellSemaphore.release();
				System.out.println("wellSemaphore released");
			}

		};
		worker.execute();
	}

	private void clearWellList() {
		Semaphore semaphore = new Semaphore(0);
		executor.execute(() -> {
			while (textCombo1.getItemCount() > 1) {
				textCombo1.removeItemAt(1);
			}
			semaphore.release();
		});
		try {
			semaphore.tryAcquire(2500, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			return;
		}
	}

	public static String removeSpecialCharacters(String string) {
		Matcher matcher = Pattern.compile("[\\-\\{}\\(\\)\\!\\?\\,\\&\\%\\*\\$\\#]|(\\s\\s)").matcher(string);
		String newString = string;
		while (matcher.find()) {
			if (matcher.group().matches("\\s\\s")) {
				newString = newString.replace(matcher.group(), " ");
				matcher.reset(newString);
				continue;
			}
			newString = newString.replace(matcher.group(), "");
			matcher.reset(newString);
		}
		return newString.toLowerCase();
	}

	public static String removeSpecialCharacters(String string, String addRegEx) {
		Matcher matcher = Pattern.compile("[\\-\\{}\\(\\)\\!\\?\\,\\&\\%\\*\\$\\#]|(\\s\\s)|(" + addRegEx + ")")
				.matcher(string);
		String newString = string;
		while (matcher.find()) {
			if (matcher.group().matches("\\s\\s")) {
				newString = newString.replace(matcher.group(), " ");
				matcher.reset(newString);
				continue;
			}
			newString = newString.replace(matcher.group(), "");
			matcher.reset(newString);
		}
		return newString.toLowerCase();
	}

	public class Refresh extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			ClearTable.clearTables(diagTable1, diagTable2, diagTable3, diagTable4, mTable);
			Semaphore wellSemaphore = new Semaphore(0);
			LastStartCloseComponent.updateDates(lastStartClose);
			retrieveCrewMap();
			clearWellList(wellSemaphore);
			petroIQInformation(wellSemaphore);
		}
	}

	@Override
	public void run() {

	}

	public class HideUnhide extends AbstractAction {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			holdStage.setVisible(!holdStage.isVisible());
		}

	}

	public class HideUnhideEmail extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			emailFrame.setVisible(!emailFrame.isVisible());
		}
	}

	public class viewChannels extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			channelPane.setVisible(!channelPane.isVisible());
		}
	}

	public class ConfigurePath extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			WriteDirectory writeDirectory = null;
			try {
				writeDirectory = new WriteDirectory();
			} catch (HeadlessException | IOException e2) {
				JOptionPane.showMessageDialog(null, "Issue saving the directory");
				return;
			}
			try {
				writeDirectory.sendDirectory();
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(null, "Issue saving the directory");
				return;
			}
			pathLabel.setText(writeDirectory.getDirectory());
			// String coState = well.getCounty(wellName) +","+well.getState(wellName);
			String cmEmails = new String();
			for (String a : emailFrame.getEmails(1)) {
				cmEmails = cmEmails + a + ";";
			}
			cmEmails.subSequence(0, cmEmails.length() - 1);

			try {
				new StageArguments(ReadDirectory.readDirect(), wellName.toUpperCase(),
						String.valueOf(textField1.getSelectedItem()), jobLogWells.getCounty(getWellName()),
						jobLogWells.getFormationMap().get(jobLogWells.getSelectedWellMap().get("formationId")),
						emailFrame.getMyEmail(), cmEmails, wellDrivePane.getUsername(), wellDrivePane.getPassword());
			} catch (HeadlessException | IOException e1) {
				try {
					new TextLog("Company Man Emails - line 633");
				} catch (IOException e2) {
				}
				return;
			}

		}
	}

	public Boolean checkQueue(String wellStage) {
		if (transfersQueue.checkQueue(wellStage)) {
			int cont = JOptionPane.showOptionDialog(Main.yess, "This stage was recently transferred; transfer again?",
					"Multiple Transfers", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
			if (cont == 1) {
				return true;
			}
			return false;
		}
		transfersQueue.addToQueue(wellStage);
		return false;
	}

	public class TransferToWorkbook extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		JMenuItem[] menuItems;

		TransferToWorkbook(JMenuItem... menuItems) {
			this.menuItems = menuItems;
		}

		Boolean waiting = true;

		public static void printStuff() {
			for (Thread thread : Thread.getAllStackTraces().keySet()) {
				thread.getContextClassLoader();
				ClassLoader.getSystemClassLoader().clearAssertionStatus();
			}
		}

		private void setMenuItems(boolean enabled) {
			for (JMenuItem item : menuItems) {
				item.setEnabled(enabled);
			}
		}

		private boolean checkCleanDirty(JTable table) {
			int fluidRow = 0;
			int cleanCol = 0;
			int slurryCol = 1;
			Double cleanTotal = Double
					.valueOf(table.getValueAt(0, 0) != null ? table.getValueAt(fluidRow, cleanCol).toString() : "0");
			Double slurryTotal = Double
					.valueOf(table.getValueAt(0, 0) != null ? table.getValueAt(fluidRow, slurryCol).toString() : "0");
			return slurryTotal < cleanTotal;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Thread.currentThread().getContextClassLoader().clearAssertionStatus();
			printStuff();
			stopEditingTables(mTable, diagTable1, diagTable2, diagTable3, diagTable4);
			String checkSand = checkSandTotals(SheetData.getSigTableData(diagTable3));
			if (!checkSand.equals("")) {
				JOptionPane.showMessageDialog(Main.yess,
						"Check your total " + checkSand + " and the summed total in the Treatment Summary");
				return;
			} else if (checkCleanDirty(diagTable4)) {
				int selected = JOptionPane.showOptionDialog(Main.yess,
						"Slurry Total is less than clean total; do you still want to transfer?", "Data Error",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
				System.out.println(selected);
				if (selected == 1) {
					return;
				}
			}
			System.out.println("--------------------------");
			printStuff();
			Integer stage = Integer.valueOf(SheetData.getSigTableData(diagTable2).get("Stage Number"));
			Semaphore endSemaphore = new Semaphore(0);
			if (checkQueue(getWellName() + " - " + stage)) {
				return;
			}
			System.out.println("-----------------------------");
			printStuff();
			executor.execute(() -> {
				setMenuItems(false);
				new EmailBody(Main.yess.getmTable(), Main.yess.diagTable1, Main.yess.diagTable3,
						mainFrame.this.getLastStartClose().get("Last_Close"));
				String operator = textField2.getText();

				if (operator.contains("Pioneer Natural Resources")) {
					try {
						PioneerWorkbook pioneerWorkbook = new PioneerWorkbook(getTreatmentNumber(), getWellName());
						pioneerWorkbook.transferData();
						if (pioneerWorkbook.getTransferred()) {
							setMenuItems(true);
							JOptionPane.showMessageDialog(null, "Data transferred to workbook");
						}
					} catch (IOException | InterruptedException e1) {
						setMenuItems(true);
					}
				} else {
					String wellName = removeSpecialCharacters(getWellName());

					executor.execute(() -> {
						XSSFWorkbook wellWorkbook = null;
						try {

							setWaiting(true);
							wellWorkbook = RedTreatmentReport.getWorkbook(wellName);
							if (wellWorkbook == null) {
								setMenuItems(true);
								JOptionPane.showMessageDialog(Main.yess,
										"If the workbook is open; close and re-export, otherwise check the file name: wellName' - TR.xlsm'");
								return;
							}
							System.out.println("Set Waiting = false");

						} catch (IOException | InterruptedException e1) {
							e1.printStackTrace();
							setMenuItems(true);
							return;
						}
						RedTreatmentReport redTR = new RedTreatmentReport(wellName, stage, wellWorkbook,
								userDefinedMap);
						try {
							XSSFWorkbook workbook = redTR.transferData();

							RedTreatmentReport.SaveWorkbook.saveWorkbook(workbook,
									RedTreatmentReport
											.findDir(new File(ReadDirectory.readDirect()),
													mainFrame.removeSpecialCharacters(wellName + " - TR.xlsm"))
											.getAbsolutePath());

							writeTemplates(operator, SheetData.getSigTableData(diagTable2),
									getChemSandMapFromTables(Main.yess.diagTable3, Main.yess.diagTable1));
							endSemaphore.release();
						} catch (IOException | ClassNotFoundException | InvalidFormatException
								| ExecutionException e2) {
							setMenuItems(true);
							e2.printStackTrace();
						}
					});
					try {
						endSemaphore.acquire();
						setMenuItems(true);
						JOptionPane.showMessageDialog(null, "Treatment report(s) saved");
					} catch (InterruptedException e1) {
						e1.printStackTrace();
						setMenuItems(true);
						return;
					}

				}

			});
		}

		public Map<String, Map<String, String>> getChemSandMapFromTables(JTable sandTable, JTable chemTable) {
			LinkedHashMap<String, String> chemMap = SheetData.getSigTableData(chemTable);
			LinkedHashMap<String, String> sandMap = SheetData.getSigTableData(sandTable);
			Map<String, Map<String, String>> map = new HashMap<>();
			map.put(TransferTemplate.CHEM_NAME, chemMap);
			map.put(TransferTemplate.SAND_NAME, sandMap);
			return map;
		}

		public void writeTemplates(String operator, Map<String, String> sigValsMap,
				Map<String, Map<String, String>> chemSandMap)
				throws IOException, ClassNotFoundException, InvalidFormatException, ExecutionException {

			File templateDir = getTemplateDir(operator);
			if (!templateDir.exists()) {
				return;
			}
			for (File file : templateDir.listFiles()) {
				TransferTemplate transferTemplate = getTemplate(file);
				if (transferTemplate == null) {
					continue;
				}
				XSSFWorkbook workbook = transferTemplate.getWorkbook();
				if (workbook == null) {
					continue;
				}
				try {
					transferTemplate.transferValuesFromTemplate(workbook.getSheet(transferTemplate.getSheetName()),
							sigValsMap, chemSandMap);
					transferTemplate.saveWorkbook(workbook, file);
				} catch (Exception e) {
					workbook.close();
					return;
				}
			}
		}

		public TransferTemplate getTemplate(File file) throws IOException, ClassNotFoundException {
			TransferTemplate transferTemplate = TransferTemplate.readFromFile(file.getAbsolutePath());
			return transferTemplate;
		}

		public File getTemplateDir(String operator) {
			String path = TransferTemplate.PARENT_FOLDER + "\\" + operator + "\\";
			File file = new File(path);
			return file;
		}

		public Boolean getWaiting() {
			return this.waiting;
		}

		public void setWaiting(Boolean waiting) {
			this.waiting = waiting;
		}
	}

	PostStage postStage = new PostStage();

	public class PostStageEmailInvoice extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			SwingWorker<Void, Void> worker = new SwingWorker<>() {
				public Void doInBackground() {
					postStage.PostStageCM();
					JOptionPane.showMessageDialog(null, "Email sent");
					return null;
				}
			};
			worker.execute();
		}
	}

	public class PostStageInvoice extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			SwingWorker<Void, Void> worker = new SwingWorker<>() {
				public Void doInBackground() {
					postStage.saveInvoice();
					JOptionPane.showMessageDialog(null, "Invoice Saved");
					return null;
				}
			};
			worker.execute();
		}
	}

	public class PostStagePlots extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {

			SwingWorker<Void, Void> worker = new SwingWorker<>() {
				public Void doInBackground() {
					new PostStage.MakePlots();
					JOptionPane.showMessageDialog(null, "Plots Made");
					return null;
				}
			};
			worker.execute();
		}
	}

	public class PostStageMerge extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			executor.execute(() -> {
				PostStage.mergePDFs(pjrFilesPane);
				JOptionPane.showMessageDialog(null, "PJR made");
			});
		}
	}

	public class OpTemplate extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			executor.execute(() -> {
				try {
					opTemplateAction();
				} catch (InterruptedException | ClassNotFoundException | IOException e1) {
					e1.printStackTrace();
					return;
				}
			});
		}

	}

	void opTemplateAction() throws InterruptedException, ClassNotFoundException, IOException {
		TransferTemplateFrame transferTemplateFrame = new TransferTemplateFrame(
				TransferTemplateFrame.getTransferTemplateRect());
		String operator = transferTemplateFrame.getSelectedOperator();
		String loadTemplate = transferTemplateFrame.getLoadTemplate();
		if (transferTemplateFrame.disposed) {
			return;
		}
		if (loadTemplate == TransferTemplateFrame.NULL_LOAD_TEMPLATE) {
			new OperatorTemplateStageSummary(getTemplateSummaryRect(), operator,
					TransferTemplate.getNextTemplateName(operator));
		} else {
			new OperatorTemplateStageSummary(getTemplateSummaryRect(), operator,
					TransferTemplate.readFromFile(TransferTemplate.getTemplatePath(operator, loadTemplate)));
		}
	}

	private class TransferTemplateFrame extends JFrame {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		final static String OPERATOR_COMBO_BOX_NAME = "operator_combo_box";
		final static String NEW_OPERATOR = "NEW_OPERATOR";
		final static int WIDTH = 300;
		final static int HEIGHT = 250;
		Semaphore semaphore = new Semaphore(0);
		String selectedOperator;
		String loadTemplate = NULL_LOAD_TEMPLATE;
		Rectangle rectangle;
		boolean disposed = false;
		boolean waiting = false;

		TransferTemplateFrame(Rectangle rectangle) {
			this.rectangle = rectangle;
			nittyGritty();
		}

		final static String TITLE_TEXT = "Data Transfer Template";

		void nittyGritty() {
			getContentPane().setBackground(Color.getHSBColor(-.85f, .1f, .85f));
			setIconImage(new ImageIcon(System.getProperty(Main.IMAGE_PROPERTY)).getImage().getScaledInstance(64, 64,
					Image.SCALE_SMOOTH));
			setTitle(TITLE_TEXT);
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			addComponents();
			setBounds(rectangle);
			setLayout(new TemplateLayout());
			setVisible(true);
		}

		void addComponents() {
			constructOperatorComboBox();
			constructLoadTemplateComboBox();
			constructTemplateButton();
		}

		static Rectangle getTransferTemplateRect() {
			int x = GUIUtilities.getCenterX(TransferTemplateFrame.WIDTH);
			int y = GUIUtilities.getCenterY(TransferTemplateFrame.HEIGHT);
			return new Rectangle(x, y, TransferTemplateFrame.WIDTH, TransferTemplateFrame.HEIGHT);
		}

		final static String BUTTON_NAME = "template_button";
		final static String BUTTON_TEXT = "Config Template";
		final static int BUTTON_WIDTH = 150;
		final static int BUTTON_HEIGHT = 25;

		void constructTemplateButton() {
			JButton button = new JButton();
			button.setText(BUTTON_TEXT);
			button.setName(BUTTON_NAME);
			button.addActionListener(getButtonAction());
			button.setVisible(true);
			add(button);
		}

		AbstractAction getButtonAction() {
			return new AbstractAction() {
				/**
				 *
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {

					semaphore.release(1);
					dispose();
				}
			};
		}

		void constructOperatorComboBox() {
			JComboBox<String> comboBox = new JComboBox<>();
			comboBox.setName(OPERATOR_COMBO_BOX_NAME);
			((JLabel) comboBox.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
			addOperatorsToComboBox(comboBox);
			comboBox.addActionListener(getNewOperatorAction(comboBox));
			comboBox.setVisible(true);
			add(comboBox);
		}

		final static String NULL_LOAD_TEMPLATE = " - ";
		final static String LOAD_TEMPLATE_NAME = "saved_templates";

		void constructLoadTemplateComboBox() {
			JComboBox<String> comboBox = new JComboBox<>();
			comboBox.setName(LOAD_TEMPLATE_NAME);
			((JLabel) comboBox.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
			comboBox.addItem(NULL_LOAD_TEMPLATE);
			comboBox.addActionListener(getLoadTemplateAction(comboBox));
			comboBox.setVisible(true);
			add(comboBox);
		}

		AbstractAction getLoadTemplateAction(JComboBox<String> comboBox) {
			return new AbstractAction() {
				/**
				 *
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					if (comboBox.getSelectedItem().toString().equals(NULL_LOAD_TEMPLATE)) {
						return;
					}
					setLoadTemplate(comboBox.getSelectedItem().toString());
					System.out.println(comboBox.getSelectedItem().toString());
				}
			};
		}

		@Override
		public void setDefaultCloseOperation(int operation) {
			super.setDefaultCloseOperation(operation);
			actionOnDispose();
		}

		void actionOnDispose() {
			addPropertyChangeListener("defaultCloseOperation", new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					disposed = true;
					semaphore.release();
				}

			});
		}

		void waitForUser() throws InterruptedException {
			if (!waiting) {
				waiting = true;
				semaphore.acquire();
				System.out.println("Semaphore Acquired");
			} else {
				System.out.println("Not Waiting on User");
			}
		}

		String getLoadTemplate() throws InterruptedException {
			waitForUser();
			return loadTemplate;
		}

		String getSelectedOperator() throws InterruptedException {
			waitForUser();
			return selectedOperator;
		}

		void setLoadTemplate(String loadTemplate) {
			this.loadTemplate = loadTemplate;
		}

		void setSelectedOperator(String selectedOperator) {
			this.selectedOperator = selectedOperator;
		}

		AbstractAction getNewOperatorAction(JComboBox<String> comboBox) {
			return new AbstractAction() {
				/**
				 *
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					executor.execute(() -> {
						String selectedItem = comboBox.getSelectedItem().toString();
						if (selectedItem.equals(NEW_OPERATOR)) {
							inquireOperatorName();
							return;
						}
						setSelectedOperator(selectedItem);
						fillTemplateComboBox(selectedItem);
					});
				}
			};
		}

		@SuppressWarnings("unchecked")
		void fillTemplateComboBox(String selectedOperator) {
			JComboBox<String> comboBox = (JComboBox<String>) GUIUtilities.getComponentByName(this, LOAD_TEMPLATE_NAME);
			SwingWorker<Void, String> worker = new SwingWorker<>() {
				boolean removedItems = false;

				public Void doInBackground() {
					String[] templates = TransferTemplate.getOperatorTemplates(selectedOperator);
					if (templates == null) {
						return null;
					}
					for (String s : templates) {
						publish(s);
					}
					return null;
				}

				public void process(List<String> chunks) {
					if (!removedItems) {
						removedItems = true;
						while (comboBox.getItemCount() > 1) {
							comboBox.removeItemAt(1);
						}
					}
					for (String s : chunks) {
						comboBox.addItem(s);
					}
				}
			};
			worker.execute();
		}

		void inquireOperatorName() {
			String input = JOptionPane
					.showInputDialog("Input the name of the operator as it appears in PetroIQ/Scrape");
			if (!input.equals("")) {
				setSelectedOperator(input);
			}
		}

		void addOperatorsToComboBox(JComboBox<String> comboBox) {
			String[] operatorList = getListOfOperators();
			for (String s : operatorList) {
				comboBox.addItem(s);
			}
		}

		String[] getListOfOperators() {
			String currOperator = getOperator();
			String[] prevSavedOperators = TransferTemplate.getSavedOperators();
			String[] operatorList = new String[prevSavedOperators.length + 1];
			operatorList[0] = currOperator == null || currOperator.equals(OPERATOR_PLACE_HOLDER) ? NEW_OPERATOR
					: currOperator;
			for (int i = 1; i < operatorList.length; i++) {
				operatorList[i] = prevSavedOperators[i - 1];
			}
			return operatorList;
		}

		class TemplateLayout extends SpringLayout {
			final static int NORTH_BUFFER = 35;
			final static int EAST_BUFFER = 25;
			final static int COMPONENT_BUFFER = 25;
			final static int WEST_BUFFER = 25;
			final static int COMBO_BOX_HEIGHT = 35;

			TemplateLayout() {
				constructLayout();
			}

			@SuppressWarnings("unchecked")
			void constructLayout() {

				for (Component c : getContentPane().getComponents()) {
					String name = c.getName();
					if (name == null) {
						continue;
					}
					switch (name) {
					case (OPERATOR_COMBO_BOX_NAME):
						comboBoxLayout((JComboBox<String>) c, 0);
						break;
					case (LOAD_TEMPLATE_NAME):
						comboBoxLayout((JComboBox<String>) c, 1);
						break;
					case (BUTTON_NAME):
						buttonLayout((JButton) c, getLastComboBoxSouth());
						break;
					}
				}
			}

			int getLastComboBoxSouth() {
				int numComps = getContentPane().getComponents().length - 2;
				if (numComps < 0) {
					return 0;
				}
				return getComboBoxSouth(numComps);
			}

			int getComboBoxEast() {
				return rectangle.width - WEST_BUFFER - EAST_BUFFER;
			}

			int getComboBoxNorth(int index) {
				return NORTH_BUFFER + index * (COMPONENT_BUFFER + COMBO_BOX_HEIGHT);
			}

			int getComboBoxSouth(int index) {
				return getComboBoxNorth(index) + BUTTON_HEIGHT;
			}

			void comboBoxLayout(JComboBox<String> comboBox, int index) {
				putConstraint(NORTH, comboBox, getComboBoxNorth(index), NORTH, getContentPane());
				putConstraint(SOUTH, comboBox, getComboBoxSouth(index), NORTH, getContentPane());
				putConstraint(WEST, comboBox, WEST_BUFFER, WEST, getContentPane());
				putConstraint(EAST, comboBox, getComboBoxEast(), WEST, getContentPane());
			}

			void buttonLayout(JButton button, int lastCompSouth) {
				putConstraint(NORTH, button, getButtonNorth(lastCompSouth), NORTH, getContentPane());
				putConstraint(SOUTH, button, getButtonSouth(lastCompSouth), NORTH, getContentPane());
				putConstraint(WEST, button, getButtonWest(), WEST, getContentPane());
				putConstraint(EAST, button, getButtonEast(), WEST, getContentPane());
			}

			int getButtonEast() {
				return getButtonWest() + BUTTON_WIDTH;
			}

			int getButtonWest() {
				int centerX = rectangle.width / 2;
				return centerX - (BUTTON_WIDTH / 2);
			}

			int getButtonNorth(int lastCompSouth) {
				return lastCompSouth + COMPONENT_BUFFER;
			}

			int getButtonSouth(int lastCompSouth) {
				int buttonNorth = getButtonNorth(lastCompSouth);
				return buttonNorth + BUTTON_HEIGHT;
			}
		}

	}

	private class EmailButton extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			executor.execute(() -> {

				ArrayList<String> arguments = emailFrame.getEmails(1);
				ArrayList<String> names = emailFrame.getEmails(0);
				ArrayList<String> myEmail = new ArrayList<>();
				myEmail.add(emailFrame.getMyEmail());
				try {
					ArgumentsToText.writeArguments(names, "C:\\Scrape\\CM_Names.txt", "\n");
					ArgumentsToText.writeArguments(arguments, "C:\\Scrape\\CM_Emails.txt", "\n");
					ArgumentsToText.writeArguments(myEmail, "C:\\Scrape\\My_Email.txt", "\n");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				emailFrame.setVisible(false);
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {

						String wellName = getWellName();
						// String coState = well.getCounty(wellName) +","+well.getState(wellName);
						String cmEmails = new String();
						for (String a : emailFrame.getEmails(1)) {
							cmEmails = cmEmails + a + ";";
						}
						cmEmails.subSequence(0, cmEmails.length() - 1);
						try {
							new StageArguments(ReadDirectory.readDirect(), wellName.toUpperCase(),
									String.valueOf(textField1.getSelectedItem()), jobLogWells.getCounty(getWellName()),
									jobLogWells.getFormationMap()
											.get(jobLogWells.getSelectedWellMap().get("formationId")),
									emailFrame.getMyEmail(), cmEmails, wellDrivePane.getUsername(),
									wellDrivePane.getPassword());
						} catch (HeadlessException | IOException e1) {
							try {
								new TextLog("Company Man Emails - line 633");
							} catch (IOException e2) {
							}
						}
					}
				});
				t.start();
			});
		}
	}

	private class SumPanelMouseListener extends SwingWorker<Object, Object> implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {

		}

		@Override
		public void mousePressed(MouseEvent e) {

		}

		@Override
		public void mouseReleased(MouseEvent e) {
			try {
				doInBackground();
			} catch (Exception e1) {
				sumPanel.label.setText("");
			}

		}

		@Override
		public void mouseEntered(MouseEvent e) {

		}

		@Override
		public void mouseExited(MouseEvent e) {

		}

		@Override
		protected SumPanel doInBackground() throws Exception {
			return sumPanel.updateText();
		}

	}

	private class WellDriveAction extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			executor.execute(() -> {
				try {
					wellDrivePane.saveInfo("C:\\Scrape\\welldrive.txt");
				} catch (IOException e1) {
					System.out.println("WellDrivePane IOException");
				}
				wellDrivePane.setVisible(!wellDrivePane.isVisible());
				String wellName = getWellName();
				activeMap.get(textCombo1.getSelectedItem()).get("county");
				activeMap.get(textCombo1.getSelectedItem()).get("state");
				String cmEmails = new String();
				for (String a : emailFrame.getEmails(1)) {
					cmEmails = cmEmails + a + ";";
				}
				// System.out.println(wellDrivePane.getUsername());
				cmEmails.subSequence(0, cmEmails.length() - 1);
				try {
					new StageArguments(ReadDirectory.readDirect(), wellName.toUpperCase(),
							String.valueOf(textField1.getSelectedItem()), jobLogWells.getCounty(getWellName()),
							jobLogWells.getFormationMap().get(jobLogWells.getSelectedWellMap().get("formationId")),
							emailFrame.getMyEmail(), cmEmails, wellDrivePane.getUsername(),
							wellDrivePane.getPassword());
				} catch (HeadlessException | IOException e1) {
					try {
						new TextLog("Company Man Emails - line 633");
					} catch (IOException e2) {
					}
				}

			});
		}
	}

	public static Boolean checkChannels(channelData channels, String operator, String well) {
		HashMap<String, String> channelsUsed = ChannelPane.getChannelList(well);
		Stack<String> stack = new Stack<>();

		for (String key : channelsUsed.keySet()) {

			stack.push(key);
			String tempKey = channelsUsed.get(key);
			Matcher keyMatcher = Pattern.compile("[/s-]+").matcher(tempKey);
			if (keyMatcher.find()) {
				tempKey.replaceAll(keyMatcher.group(), "");
			}
			for (String channel : channels.getOriginalName()) {
				Matcher matcher = Pattern.compile("[/s-]+").matcher(channel);

				String temp = channel;
				if (matcher.find()) {
					temp.replaceAll(matcher.group(), "");
				}
				if (temp.toUpperCase().contains(tempKey.toUpperCase())) {
					stack.pop();
					break;
				}
			}
		}
		if (stack.size() > 0) {
			while (stack.size() > 0) {
				System.out.println(stack.pop());
			}
			stack.forEach(System.out::println);
		}
		return stack.size() == 0;
	}

	public JProgressBar getProgressBar() {
		JProgressBar progressBar = new JProgressBar();
		progressBar.setBounds(0, 0, getBounds().width / 4, 25);
		progressBar.setName("Progress");
		progressBar.setBorderPainted(false);
		progressBar.setBackground(Color.black);
		progressBar.setForeground(Color.getHSBColor(0f, 0f, 1f));
		progressBar.setStringPainted(true);
		progressBar.setString("15%");
		progressBar.setVisible(true);
		return progressBar;
	}

	private JPanel getProgressPanel() {
		JPanel panel = new JPanel();
		panel.setBounds(0, 0, getBounds().width / 4, 25);
		panel.setBackground(Color.black);
		panel.setVisible(true);
		panel.setName("Progress_Panel");
		pane.add(panel, JLayeredPane.POPUP_LAYER);
		return panel;
	}

	public void removeAllWells() {
		while (textCombo1.getItemCount() > 1) {
			textCombo1.removeItemAt(1);
		}
	}

	private void addWells() {
		ArrayList<String> pathWells = new ArrayList<>();
		ArrayList<String> activeWells = JobLogWells.getArrayOfKeys(activeMap);
		SwingWorker<Void, String> worker = new SwingWorker<>() {
			public Void doInBackground() {

				try {
					pathWells
							.addAll(WellListSelection.getWellsInPath(FracCalculations.getArrayOfStringKeys(activeMap)));
				} catch (IOException e) {
					System.out.println("Exception caught Adding wells to TextCombo1");
					for (String well : activeWells) {
						textCombo1.addItem(well);
					}
				}
				boolean containsWells = false;
				for (String pathWell : pathWells) {
					for (String s : activeWells) {
						if (s.toUpperCase().equals(pathWell.toUpperCase())) {
							publish(s);
							containsWells = true;
						}
					}
				}
				if (!containsWells) {
					for (String well : activeWells) {
						publish(well);
					}
				}

				publish("done");
				return null;
			}

			public void process(List<String> chunks) {
				for (String s : chunks) {
					if (s.equals("done")) {
						System.out.println("Done adding Wells: Published Done");
						((JProgressBar) progressPanel.getComponent(0)).setString("");
						textCombo1.setEnabled(true);
						textCombo1.repaint();
					} else {
						textCombo1.addItem(s);
					}
				}
			}
		};
		worker.execute();
	}

	private class ReadDiagnosticMarkers implements Runnable {
		final String OPENFILE = "open.txt";
		final String CLOSEFILE = "close.txt";
		final String ISIPFILE = "isip.txt";
		final String BREAKFILE = "break.txt";
		ArrayList<String> treatingPressure;
		ArrayList<String> slurryRate;
		ArrayList<String> times;
		ArrayList<String> cleanTotal;
		private HashMap<String, String> markers;
		private Semaphore semaphore;

		ReadDiagnosticMarkers(ArrayList<String> treatingPressure, ArrayList<String> slurryRate, ArrayList<String> times,
				ArrayList<String> cleanTotal) {
			this.treatingPressure = treatingPressure;
			this.slurryRate = slurryRate;
			this.times = times;
			this.cleanTotal = cleanTotal;
			fixTimes();
			this.semaphore = new Semaphore(0);
		}

		public void fixTimes() {
			ArrayList<String> newTimes = new ArrayList<>();
			for (String s : times) {
				Matcher matcher = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}\\:\\d{2}\\:\\d{2}").matcher(s);
				if (matcher.find()) {
					newTimes.add(LocalDateTime.parse(matcher.group())
							.format(DateTimeFormatter.ofPattern("MM/dd/YYYY HH:mm:ss")));
					continue;
				}
				newTimes.add("0");
			}
			times = newTimes;
		}

		@Override
		public void run() {
			HashMap<String, Integer> indexMap = null;
			try {
				indexMap = readFiles();
			} catch (IOException e) {
				System.out.println("Exception caught ReadDiagnosticMarkers:readFile()");
			}
			markers = new HashMap<>();
			markers.put("open_pressure", getValueAtIndex(treatingPressure, indexMap.get("open")));
			markers.put("close_pressure", getValueAtIndex(treatingPressure, indexMap.get("close")));
			markers.put("break_pressure", getValueAtIndex(treatingPressure, indexMap.get("break")));
			markers.put("isip", getValueAtIndex(treatingPressure, indexMap.get("isip")));
			markers.put("isip_time",
					getValueAtIndex(times, indexMap.get("isip")).split(" ").length > 1
							? getValueAtIndex(times, indexMap.get("isip")).split(" ")[1]
							: "0");
			markers.put("break_rate", getValueAtIndex(slurryRate, indexMap.get("break")));
			markers.put("break_time",
					getValueAtIndex(times, indexMap.get("break")).split(" ").length > 1
							? getValueAtIndex(times, indexMap.get("break")).split(" ")[1]
							: "0");
			markers.put("break_volume", getValueAtIndex(cleanTotal, indexMap.get("break")));
			setMarkers(markers);

		}

		private String subSecondFromStamp(String timeStamp) {
			if (timeStamp.split(" ").length < 2) {
				return timeStamp;
			}
			LocalDateTime localDateTime = LocalDateTime
					.parse(getFormattedDate(timeStamp) + "T" + timeStamp.split(" ")[1]).minusSeconds(1);
			return localDateTime.toString();
		}

		private String addSecondToStamp(String timeStamp) {
			if (timeStamp.split(" ").length < 2) {
				return timeStamp;
			}
			LocalDateTime localDateTime = LocalDateTime
					.parse(getFormattedDate(timeStamp) + "T" + timeStamp.split(" ")[1]).plusSeconds(1);
			return localDateTime.toString();
		}

		private String getFormattedDate(String timeStamp) {
			String date = timeStamp.split(" ")[0];
			String[] yearMonthDay = new String[] { date.split("/")[2], date.split("/")[0], date.split("/")[1] };
			return yearMonthDay[0] + "-" + yearMonthDay[1] + "-" + yearMonthDay[2];
		}

		HashMap<String, Integer> readFiles() throws IOException {
			HashMap<String, Integer> map = new HashMap<>();
			map.put("open", getIndexOfStamp(readFile(OPENFILE)));
			map.put("close", getIndexOfStamp(readFile(CLOSEFILE)));
			map.put("break", getIndexOfStamp(readFile(BREAKFILE)));
			map.put("isip", getIndexOfStamp(readFile(ISIPFILE)));
			return map;
		}

		static String readFile(String fileName) throws IOException {
			File file = new File("C:\\Scrape\\ScrapePython\\Telo\\Diagnostics\\" + fileName);
			if (!file.exists()) {
				return "";
			}
			String timeStamp = ArgumentsToText.readStringFromFile(file.getAbsolutePath());
			return timeStamp;
		}

		int getIndexOfStamp(String timeStamp) {
			int index = times.indexOf(timeStamp);
			if (index == -1) {
				index = times.indexOf(subSecondFromStamp(timeStamp));
			}
			if (index == -1) {
				index = times.indexOf(addSecondToStamp(timeStamp));
			}
			return index;
		}

		String getValueAtIndex(ArrayList<String> array, int index) {
			return index > -1 ? array.get(index) : "0";
		}

		private void setMarkers(HashMap<String, String> markers) {
			this.markers = markers;
			semaphore.release();
		}

		HashMap<String, String> getMarkers() throws InterruptedException {
			semaphore.acquire();
			return this.markers;
		}
	}

	public class ServerEvaluatedData extends AbstractAction implements Serializable {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			action();
		}

		public static void action() {

			Executors.newSingleThreadExecutor().execute(() -> {

				try {
					String address = readStringObjFromFile(SERVER_ADDRESS_FILENAME, SERVER_ADDRESS_DEFAULT);
					boolean connected = getDataFileSocket(address);
					if (!connected) {
						return;
					}
				} catch (IOException | ClassNotFoundException e1) {
					e1.printStackTrace();
					System.out.println("Exception caught retrieving server data");
				}
				EvaluatedDataObject evaluatedDataObject = null;
				try {
					evaluatedDataObject = EvaluatedDataObject.getFromFile();
					combineEvaluatedDataObjects(evaluatedDataObject);
				} catch (IOException | ClassNotFoundException e2) {
					e2.printStackTrace();
					System.out.println("IOException EvaluatedDataObject::getFromFile()/combineEvaluatedDataObjects");
				}
			});
		}

		private static String inputCredentials() {
			int width = 500;
			int height = 150;
			UserNamePassword userNamePassword = new UserNamePassword(UserNamePassword.getCenterX(width),
					UserNamePassword.getCenterY(height), width, height);
			HashMap<String, String> map = userNamePassword.getCredentials();
			return getCredentialString(map);
		}

		private static String getCredentialString(HashMap<String, String> map) {
			String credentials = "{username:" + map.get(UserNamePassword.USERNAME) + ";password:"
					+ map.get(UserNamePassword.PASSWORD) + "}";
			return credentials;
		}

		public static void combineEvaluatedDataObjects(EvaluatedDataObject evaluatedDataObject)
				throws ClassNotFoundException, IOException {
			ArrayList<EvaluatedDataObject> dataObjectArray = getDataObjects("C:\\Scrape\\histDataObjects\\");
			for (EvaluatedDataObject o : dataObjectArray) {
				evaluatedDataObject.addToMaps(o);
			}
			EvaluatedDataObject.writeToFile(evaluatedDataObject);
		}

		public static ArrayList<EvaluatedDataObject> getDataObjects(String parentPath)
				throws ClassNotFoundException, IOException {
			ArrayList<EvaluatedDataObject> array = new ArrayList<>();
			File file = new File(parentPath);
			for (File cFile : file.listFiles()) {
				String filePath = cFile.getAbsolutePath() + "\\data.his";
				System.out.println(filePath);
				ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(new File(filePath)));
				array.add((EvaluatedDataObject) objectInputStream.readObject());
				objectInputStream.close();
			}
			return array;
		}

		public static void sendCredentials(DataOutputStream dataOutputStream, String credentialString)
				throws IOException {
			dataOutputStream.writeInt(credentialString.getBytes().length);
			dataOutputStream.write(credentialString.getBytes());
		}

		public static boolean checkCredentials(DataInputStream dataInputStream) throws IOException {
			boolean authorized = dataInputStream.readBoolean();
			System.out.println(authorized);
			return authorized;
		}

		@SuppressWarnings("resource")
		public static boolean getDataFileSocket(String address){
			try(Socket socket = new Socket(address, 443);){
			String credentials = inputCredentials();
			if (credentials == null) {
				System.out.println("Bad Credentials");
				return false;
			}
			try (DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());){
				System.out.println("Sending Credentials");
				sendCredentials(dataOutputStream, credentials);
				try(DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());){
					boolean authorized = checkCredentials(dataInputStream);
					if (!authorized) {
						System.out.println("Not Authroized");
						return false;
					}
				
					try(ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());){
						writeFilesFromInputStream(objectInputStream, dataOutputStream);
						sendDoneMessage(dataOutputStream);
					}
				}
			} catch (IOException|ClassNotFoundException e) {
				System.out.println("ERROR CONNECTING TO SERVER");
				return false;
			}
			
			}catch(IOException e) {
				e.printStackTrace();
				System.out.println("Exception caught; Socket Closing");
				return false;
			}
			return true;
		}

		public static void writeIdentity(DataOutputStream dataOutputStream) throws UnknownHostException, IOException {
			dataOutputStream.writeChars(InetAddress.getLocalHost().toString());
		}

		public static void sendDoneMessage(DataOutputStream dataOutputStream) throws IOException {
			"Done".getBytes();
			dataOutputStream.writeInt(-1);
		}

		public static void writeFilesFromInputStream(ObjectInputStream objectInputStream,
				DataOutputStream dataOutputStream) throws IOException, ClassNotFoundException {
			File file;
			while ((file = readDirName(objectInputStream)) != null) {
				objectInputStream.readInt();
				System.out.println(file.getAbsolutePath());
				EvaluatedDataObject evaluatedDataObject = (EvaluatedDataObject) objectInputStream.readObject();
				writeFile(evaluatedDataObject, file);
				sendDoneMessage(dataOutputStream);
			}
			System.out.println("Sam");
		}

		public static File readDirName(ObjectInputStream objectInputStream) throws IOException {
			int dirLen = -1;
			try {
				dirLen = objectInputStream.readInt();
			} catch (IOException e) {
				return null;
			}
			if (dirLen == -1) {
				return null;
			}
			byte[] dirBytes = new byte[dirLen];
			objectInputStream.read(dirBytes);
			String dirName = getStringFromBytes(dirBytes);
			File file = new File("C:\\Scrape\\histDataObjects\\" + dirName + "\\data.his");
			makeDirectories(file);
			return file;
		}

		public static void makeDirectories(File file) {
			File newFile = file.getParentFile();
			while (!newFile.exists()) {
				newFile.mkdirs();
			}
		}

		public static String getStringFromBytes(byte[] bytes) {
			StringBuilder stringBuilder = new StringBuilder();
			for (byte b : bytes) {
				stringBuilder.append((char) b);
			}
			return stringBuilder.toString();
		}

		public static void writeFile(EvaluatedDataObject evaluatedDataObject, File scrapeDir)
				throws IOException, ClassNotFoundException {

			FileOutputStream fileOutputStream = new FileOutputStream(scrapeDir.getAbsolutePath());
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(evaluatedDataObject);
			objectOutputStream.close();
			fileOutputStream.close();
		}
	}

	public void saveTreatmentObject() throws IOException, ClassNotFoundException {
		EvaluatedDataObject evaluatedDataObject = EvaluatedDataObject.getFromFile();

		evaluatedDataObject.addToMaps(SheetData.getMainTableDataIntKeys(mTable),
				SheetData.getMapOfTables(getStringsInList("sand", "chemicals"), diagTable3, diagTable1),
				SheetData.getSigTableData(diagTable2), getWellName(), getStage());
		EvaluatedDataObject.writeToFile(evaluatedDataObject);
	}

	public ArrayList<String> getStringsInList(String... strings) {
		ArrayList<String> stringList = new ArrayList<>();
		for (String s : strings) {
			stringList.add(s);
		}
		return stringList;
	}

	private void sendDataFileToServer(String address) throws ClassNotFoundException, IOException {
		executor.execute(() -> {
			try {
				EvaluatedDataObject savedDataObject = EvaluatedDataObject.getFromFile();
				sendDataFileSocket(savedDataObject, address);
			} catch (ClassNotFoundException | IOException e1) {
				e1.printStackTrace();
				return;
			}
		});

	}

	public class SaveTreatmentAction extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			executor.execute(() -> {
				try {
					cleanAction();
				} catch (ClassNotFoundException | IOException e1) {
					System.out.println("Exception caught SaveTreatmentAction::actionPerformed");
				}
			});
		}

		public void cleanAction() throws ClassNotFoundException, IOException {
			Integer lastRow = SheetData.findLastRowOfData(mTable, 0);
			if (lastRow < 1) {
				JOptionPane.showMessageDialog(Main.yess, "Tables lack data to save");
				return;
			}
			executor.execute(() -> {
				try {
					EvaluatedDataObject evaluatedDataObject = new EvaluatedDataObject();
					evaluatedDataObject.addToMaps(SheetData.getMainTableDataIntKeys(mTable),
							SheetData.getMapOfTables(getStringsInList("sand", "chemicals"), diagTable3, diagTable1),
							SheetData.getSigTableData(diagTable2), getWellName(), getStage());
					sendDataFileSocket(evaluatedDataObject);
					EvaluatedDataObject savedDataObject = EvaluatedDataObject.getFromFile();
					savedDataObject.addToMaps(evaluatedDataObject);
					EvaluatedDataObject.writeToFile(savedDataObject);
				} catch (ClassNotFoundException | IOException e1) {
					e1.printStackTrace();
					return;
				}
			});

		}

	}

	public class RecentTransfersQueue implements Serializable {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		Queue<String> queue = new LinkedList<>();

		RecentTransfersQueue() {

		}

		public void addToQueue(String wellStage) {
			if (queue.size() >= 8) {
				System.out.println(queue.poll() + " removed from transfers queue");
				queue.offer(wellStage);
				try {
					writeQueueToFile(RecentTransfersQueue.this, "C:\\Scrape\\transfersQueue.scp");
				} catch (IOException e) {
					System.out.println("Exception writing transfer queue to file");
					return;
				}
				return;
			}
			queue.offer(wellStage);
			try {
				writeQueueToFile(RecentTransfersQueue.this, "C:\\Scrape\\transfersQueue.scp");
			} catch (IOException e) {
				System.out.println("Exception writing transfer queue to file");
				return;
			}
		}

		public Boolean checkQueue(String wellStage) {
			return queue.contains(wellStage);
		}

		public void writeQueueToFile(RecentTransfersQueue recentTransfersQueue, String path) throws IOException {
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(new File(path)));
			objectOutputStream.writeObject(recentTransfersQueue);
			objectOutputStream.close();
		}

		public static Boolean checkFile(String path) {
			File file = new File(path);
			return file.exists();
		}
	}

	public RecentTransfersQueue readQueueFromFile(String path) throws IOException, ClassNotFoundException {
		if (!RecentTransfersQueue.checkFile(path)) {
			return new RecentTransfersQueue();

		}
		ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(new File(path)));
		RecentTransfersQueue recentTransfersQueue = (RecentTransfersQueue) objectInputStream.readObject();
		objectInputStream.close();
		return recentTransfersQueue;
	}

	public class RedTRConfigureFrame extends JFrame {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		final static int WIDTH = 400;
		final static int HEIGHT = 150;
		final static Color COLOR = Color.getHSBColor(-.85f, .1f, .85f);
		final static String FILEPATH = "Operator_Templates\\RedTR\\red_tr_config.map";

		final static String SUMMARY_NAME = "Summary Row 1";
		public final static String SUMMARY_ROW = "summary_row";

		final static String PREFLUSH_NAME = "Pre-Flush Row";
		public final static String PREFLUSH_ROW = "preflush_row";

		final static String BREAKDOWN_NAME = "Breakdown Time Address";
		public final static String BREAKDOWN_ROW = "breakdown_row";
		public final static String BREAKDOWN_COLUMN = "breakdown_column";

		void displayFrame() {
			nittyGritty();
			int bottom = constructInputPanels(SUMMARY_NAME, PREFLUSH_NAME, BREAKDOWN_NAME);
			constructButton(bottom);
			setLayout(null);
			setVisible(true);
		}

		String getInputText(String name) {
			InputPanel inputPanel = (InputPanel) GUIUtilities.getComponentByName(this, name);
			String input = inputPanel.getInput();
			return input;
		}

		Integer getIntegerFromString(String string, Integer defaultValue) {
			Matcher matcher = Pattern.compile("\\d+").matcher(string);
			if (matcher.find()) {
				return Integer.valueOf(matcher.group());
			}
			return defaultValue;
		}

		Integer getColumnFromString(String string, Integer defaultValue) {
			Matcher matcher = null;
			if (string.contains(",")) {
				matcher = Pattern.compile("\\d+$").matcher(string);
				return matcher.find() ? Integer.valueOf(matcher.group()) : defaultValue;
			}
			matcher = Pattern.compile("^[A-Za-z]+").matcher(string);
			if (matcher.find()) {
				return ExcelTransfer.getColumnIndex(matcher.group());
			}
			return defaultValue;
		}

		final static Integer SUMMARY_DEFAULT = 213;
		final static Integer PREFLUSH_DEFAULT = 263;
		final static Integer BREAK_COLUMN_DEFAULT = 40;
		final static Integer BREAK_ROW_DEFAULT = 206;

		AbstractAction getButtonAction() {
			AbstractAction action = new AbstractAction() {
				/**
				 *
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					HashMap<String, Integer> map = getConfigMap();
					System.out.println(map);
					try {
						writeMapToFile(map);
					} catch (IOException e1) {
						dispose();
						return;
					}
					dispose();
				}
			};
			return action;
		}

		File getFileMakeDirs() {
			File file = new File(FILEPATH);
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			return file;
		}

		void writeMapToFile(HashMap<String, Integer> map) throws IOException {
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(getFileMakeDirs()));
			objectOutputStream.writeObject(map);
			objectOutputStream.close();
		}

		static HashMap<String, Integer> getDefaultMap() {
			HashMap<String, Integer> map = new HashMap<>();
			map.put(SUMMARY_ROW, SUMMARY_DEFAULT);
			map.put(PREFLUSH_ROW, PREFLUSH_DEFAULT);
			map.put(BREAKDOWN_COLUMN, BREAK_COLUMN_DEFAULT);
			map.put(BREAKDOWN_ROW, BREAK_ROW_DEFAULT);
			return map;
		}

		@SuppressWarnings("unchecked")
		static HashMap<String, Integer> readConfigMapFromFile() throws IOException, ClassNotFoundException {
			File file = new File(FILEPATH);
			if (!file.exists()) {
				return getDefaultMap();
			}
			ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
			HashMap<String, Integer> map = (HashMap<String, Integer>) objectInputStream.readObject();
			objectInputStream.close();
			return map;
		}

		HashMap<String, Integer> getConfigMap() {
			Integer summaryRow = getIntegerFromString(getInputText(SUMMARY_NAME), SUMMARY_DEFAULT);
			Integer preflushRow = getIntegerFromString(getInputText(PREFLUSH_NAME), PREFLUSH_DEFAULT);
			String breakText = getInputText(BREAKDOWN_NAME);
			Integer breakColumn = getColumnFromString(breakText, BREAK_COLUMN_DEFAULT);
			Integer breakRow = getIntegerFromString(breakText, BREAK_ROW_DEFAULT);
			HashMap<String, Integer> map = new HashMap<>();
			map.put(SUMMARY_ROW, summaryRow);
			map.put(PREFLUSH_ROW, preflushRow);
			map.put(BREAKDOWN_COLUMN, breakColumn);
			map.put(BREAKDOWN_ROW, breakRow);
			return map;
		}

		void constructButton(int bottom) {
			int buttonWidth = 100;
			int buttonHeight = 25;
			JButton button = new JButton();
			button.setText("Save");
			button.setName("button");
			button.setBounds(WIDTH / 2 - buttonWidth / 2, bottom + 10, buttonWidth, buttonHeight);
			button.addActionListener(getButtonAction());
			button.setVisible(true);
			add(button);
		}

		int constructInputPanels(String... names) {
			int numInputs = names.length;
			int height = (HEIGHT - 45) / numInputs;
			int x = 0;
			int y = 0;
			for (String s : names) {
				System.out.println("y = " + y + " - " + s);
				InputPanel inputPanel = null;
				inputPanel = new InputPanel(new Rectangle(x, y, WIDTH, height), s, s);
				inputPanel.setOpaque(true);
				getContentPane().add(inputPanel);
				y += height - 10;
			}
			return y;
		}

		void nittyGritty() {
			setBounds(getRectX(), getRectY(), WIDTH, HEIGHT);
			getContentPane().setBackground(COLOR);
			setTitle("Red TR Config");
		}

		int getRectX() {
			return UserNamePassword.getCenterX(WIDTH);
		}

		int getRectY() {
			return UserNamePassword.getCenterY(HEIGHT);
		}
	}

}
