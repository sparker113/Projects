import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Stack;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

public class LiveDataRequest {
	private String token;
	private String uri1;
	private String requestBody0;
	private String requestBody;
	private Integer lastTime;
	private Long startBase;
	private Long addBase;
	private int n;
	private Boolean connect = false;
	private Boolean newData = false;
	private Scanner scanWrite;
	private Scanner scanArray;
	Semaphore semRequest = new Semaphore(1);
	Semaphore semWrite = new Semaphore(0);
	Semaphore semCount = new Semaphore(1);
	private Boolean reset;
	private Integer from;
	private String treatmentID;
	private String startTime;
	private String wellID;
	private channelData channels;
	private HashMap<String, HashMap<String, String>> wellListMap;
	private HashMap<String, String> channelList;
	private LiveDataFrame liveDataFrame;
	private DataMapJSon mapJSon;
	private Stack stack;
	private int y;
	private int stackSize;
	LiveWellList liveWellList;
	int i = 0;

	LiveDataRequest(String token, Stack stack, int stackSize) {
		this.liveWellList = new LiveWellList(token);
		wellListMap = liveWellList.getWellMap();
		this.reset = true;
		this.n = -1;
		this.token = token;
		this.stack = stack;
		this.y = (int) stack.peek();
		this.liveDataFrame = new LiveDataFrame(wellListMap);

		this.stackSize = stackSize;
		liveDataFrame.setLayout(null);
	}

	public String getRequestBody() {
		return this.requestBody0;
	}

	public void updateLiveWellList() {
		this.liveWellList = new LiveWellList(token);
	}

	public void setStartBase(String wellName) {
		this.startBase = Long.valueOf(wellListMap.get(wellName).get("baseTreatmentDateTime"));
	}

	public void constructRequest() {
		HashMap<String, String> channelList = ChannelPane.getChannelList(Main.yess.getWellName());
		System.out.println("In Live Data: " + channelList);
		String channelCall = "";
		String wellName = liveDataFrame.infoPanel.getWell();
		System.out.println(wellName);
		setStartBase(wellName);
		this.wellID = wellListMap.get(wellName).get("wellId");
		System.out.println("Well ID - " + wellID);
		this.treatmentID = wellListMap.get(wellName).get("treatmentId");
		System.out.println("Treatment ID - " + treatmentID);
		this.uri1 = "https://api.fracpro.ai:4000/api/v1/wells/" + wellID + "/treatments/" + treatmentID
				+ "/flowPaths/0/getDataChart" + "?authToken=";
		System.out.println("URI - " + uri1);
		Channels channelSet = new Channels();
		channelSet.setChannels(token, wellListMap.get(wellName).get("wellId"),
				wellListMap.get(wellName).get("treatmentId"));
		channels = channelSet.getChannels();
		this.mapJSon = new DataMapJSon(channels, channelList);
		String operator = liveDataFrame.infoPanel.getOperator();

		for (String a : channels.getOriginalName()) {
			Matcher matcher = Pattern.compile("\\d\\d").matcher(a);
			if (a.contains(channelList.get("Treating Pressure")) && !matcher.find()) {
				channelCall = channelCall + channels.getoCName().get(a) + "\",\"";
				System.out.println("Pressure Channel: " + a);
			} else if (a.contains(channelList.get("Slurry Rate"))) {
				channelCall = channelCall + channels.getoCName().get(a) + "\",\"";
			} else if (a.contains(channelList.get("Slurry Grand Total"))) {
				channelCall = channelCall + channels.getoCName().get(a) + "\",\"";
			} else if (a.contains(channelList.get("Clean Grand Total"))) {
				channelCall = channelCall + channels.getoCName().get(a) + "\",\"";
			} else if (a.contains(channelList.get("Stage Number"))) {
				channelCall = channelCall + channels.getoCName().get(a) + "\",\"";
			} else if (a.contains(channelList.get("Prop. Concentration"))) {
				channelCall = channelCall + channels.getoCName().get(a) + "\",\"";
			} else if (a.contains(channelList.get("Backside"))
					&& a.contains(wellName.split(" ")[wellName.split(" ").length - 1])) {
				channelCall = channelCall + channels.getoCName().get(a) + "\",\"";
			} else if (a.contains(channelList.get("Plot Prop. Concentration"))) {
				channelCall = channelCall + channels.getoCName().get(a) + "\",\"";
			}
			/*
			 * if(!operator.contains("Pioneer Natural Resources") &&
			 * a.contains("Blender Density")) { channelCall = channelCall +
			 * channels.getoCName().get(a) +"\",\""; }
			 */
		}
		Integer a = channelCall.lastIndexOf(",");
		channelCall = (String) channelCall.subSequence(0, a);
		String reqBody = new String("{\"wellId\":" + wellID + ",\"treatmentId\":" + treatmentID + ",\"items\":[\""
				+ channelCall + "],\"flowPathType\":0,\"isSeparateRealTimeData\":true,\"baseTreatmentDataTime\":\"");
		setRequestI(reqBody);
		setLastTime(Long.valueOf(0));
	}

	public void setRequestI(String request) {
		this.requestBody = request;
	}

	public void setLastTime(Long lastBaseTime) {
		Long newStart = lastBaseTime + startBase;
		LocalDateTime dateFromBase = LocalDateTime.ofEpochSecond(newStart, 0, ZoneOffset.UTC);
		String newTime = dateFromBase.format(DateTimeFormatter.ofPattern("YYYY-MM-dd")) + "T"
				+ dateFromBase.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + ".000Z\"}";
		setRequestBody0(newTime);
	}

	public void setRequestBody0(String lastTime) {
		this.requestBody0 = this.requestBody + lastTime;
	}

	public String getURI() throws IOException, InterruptedException {
		LiveDataTokenString token2 = new LiveDataTokenString(wellID, treatmentID, token);
		String queryToken = token2.getToken();
		return queryToken;
	}

	public void makeRequest(String requestBody) throws IOException, InterruptedException {
		String uri2 = uri1 + getURI();
		System.out.println("makeRequest");
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = (HttpRequest.newBuilder().header("content-type", "application/json")
				.header("accept", "appliction/json").header("authorization", token)
				.POST(HttpRequest.BodyPublishers.ofString(requestBody)).uri(URI.create(uri2)).build());
		while (!reset) {
			Thread.sleep(100);
		}
		semCount.release();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		semRequest.acquire();
		if (connect) {
			requestCount();
		}
		System.out.println(getCount());
		scanArray = new Scanner(response.body());
		semWrite.release();
	}

	public void arrayData() throws InterruptedException, IOException {

		semWrite.acquire();
		// System.out.println("Array Data");
		// System.out.println("SAM " + getCount());

		if (getCount() == 0) {
			mapJSon.initialJSon(scanArray);
		} else {
			mapJSon.addJSon(scanArray);
		}

		Long lastIndex = Long.valueOf(mapJSon.getLastIndex());
		setLastTime(lastIndex);
		liveDataFrame.dataPanel.updateLabels(mapJSon.getChannelIndeces(), mapJSon.getLastValues());
		i++;
		semRequest.release();
	}

	public void writeData(FileWriter fileWriter, CompletableFuture<String> futureString)
			throws IOException, ExecutionException, InterruptedException {
		String a = futureString.get();
		fileWriter.append(a);
		fileWriter.append("\n");
	}

	public static FileWriter getWriter() {
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter("C:\\Scrape\\stagedata.txt");
			fileWriter.write("");
		} catch (IOException e) {

		}

		return fileWriter;
	}

	public void requestCount() {
		this.n++;
	}

	public int getCount() {
		System.out.println(this.n);
		return this.n;
	}

	public void resetCount() {
		this.n = -1;
	}

	public void countdown(int from) {
		this.from = from;
		int i;
		try {
			semCount.acquire();
		} catch (InterruptedException e) {
		}
		reset = false;
		for (i = from; i >= 0; i--) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {

			}
			System.out.println(i);
		}
		reset = true;
	}

	public Boolean getReset() {
		return this.reset;
	}

	// public static newTreatmentTime(Integer lastTime,
	public void initiate() {
		LiveDataReturn liveDataReturn = new LiveDataReturn(this);
	}

	public class LiveDataFrame extends JFrame {
		private HashMap<String, HashMap<String, String>> wellMap;
		LiveInformationPanel infoPanel;
		LiveDataPanel dataPanel;
		JMenuBar menu;
		int mainX;
		int mainY;
		int mainW;
		int mainH;

		LiveDataFrame(HashMap<String, HashMap<String, String>> wellMap) {
			super();
			this.wellMap = wellMap;
			this.setLayout(null);
			this.mainX = 200;
			this.mainY = LiveDataRequest.this.y;
			this.mainW = 1200;
			this.mainH = 200;
			this.setBounds(mainX, mainY, mainW, mainH);
			this.infoPanel = new LiveInformationPanel(wellMap);
			this.dataPanel = new LiveDataPanel();
			this.infoPanel.setBounds(0, 0, (int) (mainW * .25), mainH);
			this.dataPanel.setBounds((int) (mainW * .25), 0, (int) (mainW * .75), mainH);
			this.menu = constructMenu();
			this.setTitle("Live Data Values");
			Image scrape = new ImageIcon("C:\\Scrape\\Scrape.png").getImage().getScaledInstance(64, 64,
					Image.SCALE_SMOOTH);
			this.setIconImage(scrape);
			this.setDefaultCloseOperation(1);
			this.setJMenuBar(menu);
			this.add(infoPanel);
			this.add(dataPanel);
			this.setAlwaysOnTop(true);
			this.setResizable(false);
			this.setVisible(true);
		}

		@Override
		public void setDefaultCloseOperation(int i) {
			stack.pop();
			stackSize = stackSize - 1;
			this.dispose();
		}

		public JMenuBar constructMenu() {
			JMenuBar menu = new JMenuBar();
			JMenu update = new JMenu("Update Wells");
			update.addActionListener(new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					updateLiveWellList();
					wellMap = liveWellList.getWellMap();
					infoPanel.wellNames.removeAllItems();
					for (String a : wellMap.keySet()) {
						infoPanel.wellNames.addItem(a);
					}
				}
			});
			update.setVisible(true);
			update.setOpaque(true);
			menu.setOpaque(true);
			menu.setVisible(true);
			menu.add(update);

			return menu;
		}

		private class LiveDataPanel extends JPanel {
			int w;
			int h;
			int x;
			int y;
			LinkedList<JPanel> chanLabels = new LinkedList<>();
			LinkedList<JPanel> dataLabels = new LinkedList<>();

			LiveDataPanel() {

				this.setBackground(Color.white);
				addLabels();
				LabelsSpringLayout layout = new LabelsSpringLayout(chanLabels, dataLabels, this);
				this.setLayout(layout);
				this.setVisible(true);
			}

			public void addLabels() {
				HashMap<String, String> channelLabels = ChannelPane.getChannelList();
				Integer i = 0;
				for (String a : channelLabels.keySet()) {
					JPanel label = constructDataLabel(i, a);
					label.setVisible(true);
					this.add(label);
					dataLabels.add(label);
					i++;
				}
				i = 0;
				for (String a : channelLabels.keySet()) {
					// System.out.println("At Construction "+a);
					JPanel chanLabel = constructChannelLabel(i, a);
					this.add(chanLabel);
					chanLabels.add(chanLabel);
					i++;
				}
			}

			public JPanel constructDataLabel(int i, String name) {
				System.out.println("In Construction " + name);
				JPanel panel = new JPanel();
				JLabel label = new JLabel();
				int labelX = i / 7 * this.w;
				int labelY = this.h / 2;
				int labelW = this.w / 7;
				int labelH = this.h / 2;
				panel.setBounds(labelX, labelY, labelW, labelH);
				panel.setBackground(Color.LIGHT_GRAY);
				panel.setName(name);
				label.setBackground(Color.LIGHT_GRAY);
				label.setText("0.0");
				label.setOpaque(true);
				panel.add(label);
				label.setVerticalAlignment(SwingConstants.CENTER);
				label.setHorizontalAlignment(SwingConstants.CENTER);
				panel.setOpaque(true);
				panel.setVisible(true);
				return panel;
			}

			public JPanel constructChannelLabel(int i, String text) {
				JPanel panel = new JPanel();
				JLabel label = new JLabel();
				int labelX = i / 7 * this.w;
				int labelY = 0;
				int labelW = this.w / 7;
				int labelH = this.h / 2;
				panel.setBounds(labelX, labelY, labelW, labelH);
				panel.setBackground(Color.LIGHT_GRAY);
				label.setBackground(Color.LIGHT_GRAY);
				label.setHorizontalAlignment(SwingConstants.CENTER);
				label.setVerticalAlignment(SwingConstants.CENTER);
				label.setOpaque(true);
				label.setText(text);
				panel.add(label);
				panel.setOpaque(true);
				panel.setVisible(true);
				return panel;
			}

			public void updateLabels(HashMap<Integer, String> channelIndeces, HashMap<Integer, String> data) {
				System.out.println(channelIndeces);
				System.out.println(data);
				for (JPanel a : dataLabels) {
					for (Integer b : channelIndeces.keySet()) {
						if (channelIndeces.get(b).equals(a.getName())) {
							// System.out.println("At Evaluation " + a.getName());
							System.out.println(data.get(b));
							((JLabel) a.getComponent(0)).setText(data.get(b));
							break;
						}
					}
				}

			}

			public class LabelsSpringLayout extends SpringLayout {
				LabelsSpringLayout(LinkedList<JPanel> channelPanels, LinkedList<JPanel> dataPanels, JPanel mainPanel) {
					Integer i;
					Integer size = channelPanels.size() - 1;
					for (i = 0; i < channelPanels.size(); i++) {
						Integer w1 = (int) Math
								.round(Double.valueOf(i) / Double.valueOf(size) * Double.valueOf(mainW * .75));
						Integer w2 = (int) Math
								.round(Double.valueOf(i + 1) / Double.valueOf(size) * Double.valueOf(mainW * .75));
						Integer h1 = mainH / 2;
						Integer h2 = mainH;
						this.putConstraint(SpringLayout.NORTH, channelPanels.get(i), 0, SpringLayout.NORTH, mainPanel);
						this.putConstraint(SpringLayout.SOUTH, channelPanels.get(i), h1, SpringLayout.NORTH, mainPanel);
						this.putConstraint(SpringLayout.WEST, channelPanels.get(i), w1, SpringLayout.WEST, mainPanel);
						this.putConstraint(SpringLayout.EAST, channelPanels.get(i), w2, SpringLayout.WEST, mainPanel);
						this.putConstraint(SpringLayout.NORTH, dataPanels.get(i), h1, SpringLayout.NORTH, mainPanel);
						this.putConstraint(SpringLayout.SOUTH, dataPanels.get(i), h2, SpringLayout.NORTH, mainPanel);
						this.putConstraint(SpringLayout.WEST, dataPanels.get(i), w1, SpringLayout.WEST, mainPanel);
						this.putConstraint(SpringLayout.EAST, dataPanels.get(i), w2, SpringLayout.WEST, mainPanel);

					}
				}
			}
		}

		private class LiveInformationPanel extends JPanel {
			private JComboBox<String> wellNames;
			private JLabel stageLabel;
			private JLabel operatorLabel;
			private JButton button;
			private HashMap<String, HashMap<String, String>> wellMap;
			private int w;
			private int h;

			LiveInformationPanel(HashMap<String, HashMap<String, String>> wellMap) {
				this.w = mainW * 1 / 4;
				this.h = mainH;
				this.setBounds(0, 0, w, h);
				this.wellMap = wellMap;
				this.wellNames = constructNamesField();
				this.stageLabel = constructStageLabel();
				this.operatorLabel = constructOperatorLabel();
				this.button = constructButton();

				this.setBackground(Color.black);
				this.setOpaque(true);
				addComps();
				InfoLayout infoLayout = new InfoLayout(wellNames, stageLabel, operatorLabel, button, this);
				this.setLayout(infoLayout);
				this.setVisible(true);
			}

			public String getWell() {
				String wellName = String.valueOf(wellNames.getSelectedItem());
				return wellName;
			}

			public String getOperator() {
				String operator = operatorLabel.getText();
				return operator;
			}

			public void addComps() {
				this.add(wellNames);
				this.add(stageLabel);
				this.add(operatorLabel);
				this.add(button);
			}

			public JComboBox<String> constructNamesField() {
				JComboBox comboBox = new JComboBox();
				for (String a : wellMap.keySet()) {
					comboBox.addItem(a);
				}
				comboBox.addActionListener(new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						constructRequest();
					}
				});
				((JLabel) comboBox.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
				comboBox.setBounds((int) .1 * w, (int) .05 * h, (int) .8 * w, (int) .15 * h);
				comboBox.setOpaque(true);
				comboBox.setVisible(true);
				comboBox.addActionListener(new updateStage());
				return comboBox;
			}

			public JLabel constructStageLabel() {
				JLabel label = new JLabel();
				label.setBounds((int) .1 * w, (int) .25 * h, (int) .8 * w, (int) .15 * h);
				label.setBackground(Color.WHITE);
				label.setHorizontalAlignment(SwingConstants.CENTER);
				label.setOpaque(true);
				return label;
			}

			public JLabel constructOperatorLabel() {
				JLabel label = new JLabel();
				label.setBounds((int) .1 * w, (int) .5 * h, (int) .8 * w, (int) .15 * h);
				label.setBackground(Color.WHITE);
				label.setHorizontalAlignment(SwingConstants.CENTER);
				label.setOpaque(true);
				return label;
			}

			public JButton constructButton() {
				JButton b = new JButton();
				b.setBounds((int) .25 * w, (int) .575 * h, (int) .5 * w, (int) .2 * h);
				b.setText("Connect");
				b.addActionListener(new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (!connect) {
							connect = true;
							initiate();
							b.setText("Disconnect");
						} else {
							connect = false;
							resetCount();
							if (semWrite.availablePermits() > 0) {
								try {
									semWrite.acquire();
								} catch (InterruptedException e1) {
								}
							}
							b.setText("Connect");
						}
					}
				});
				return b;
			}

			public void setButtonAction(ActionListener e) {
				this.button.addActionListener(e);
			}

			class updateStage extends AbstractAction {
				@Override
				public void actionPerformed(ActionEvent e) {
					String well = String.valueOf(wellNames.getSelectedItem());
					stageLabel.setText(wellMap.get(well).get("treatmentNumber"));
					operatorLabel.setText(wellMap.get(well).get("operatorCompanyName"));
				}
			}
		}

		public class InfoLayout extends SpringLayout {
			InfoLayout(JComboBox wellNames, JLabel label1, JLabel label2, JButton button, JPanel panel) {
				this.putConstraint(SpringLayout.NORTH, wellNames, (int) (mainH * .1), SpringLayout.NORTH, panel);
				this.putConstraint(SpringLayout.SOUTH, wellNames, (int) (mainH * .2), SpringLayout.NORTH, panel);
				this.putConstraint(SpringLayout.WEST, wellNames, (int) (mainW * .025), SpringLayout.WEST, panel);
				this.putConstraint(SpringLayout.EAST, wellNames, (int) (mainW * .225), SpringLayout.WEST, panel);
				this.putConstraint(SpringLayout.NORTH, label1, (int) (mainH * .25), SpringLayout.NORTH, panel);
				this.putConstraint(SpringLayout.SOUTH, label1, (int) (mainH * .35), SpringLayout.NORTH, panel);
				this.putConstraint(SpringLayout.WEST, label1, (int) (mainW * .025), SpringLayout.WEST, panel);
				this.putConstraint(SpringLayout.EAST, label1, (int) (mainW * .225), SpringLayout.WEST, panel);
				this.putConstraint(SpringLayout.NORTH, label2, (int) (mainH * .4), SpringLayout.NORTH, panel);
				this.putConstraint(SpringLayout.SOUTH, label2, (int) (mainH * .5), SpringLayout.NORTH, panel);
				this.putConstraint(SpringLayout.WEST, label2, (int) (mainW * .025), SpringLayout.WEST, panel);
				this.putConstraint(SpringLayout.EAST, label2, (int) (mainW * .225), SpringLayout.WEST, panel);
				this.putConstraint(SpringLayout.NORTH, button, (int) (mainH * .55), SpringLayout.NORTH, panel);
				this.putConstraint(SpringLayout.SOUTH, button, (int) (mainH * .675), SpringLayout.NORTH, panel);
				this.putConstraint(SpringLayout.WEST, button, (int) (mainW * .075), SpringLayout.WEST, panel);
				this.putConstraint(SpringLayout.EAST, button, (int) (mainW * .175), SpringLayout.WEST, panel);
			}
		}
	}

	public class LiveDataReturn implements Runnable {
		private LiveDataRequest liveDataRequest;
		private FileWriter fileWriter;
		private Thread t;
		private ExecutorService executor;
		Runnable requestRun;
		Runnable writeRun;
		Runnable countdownRun;
		Runnable writeQueueRun;

		LiveDataReturn(LiveDataRequest liveDataRequest) {
			this.liveDataRequest = liveDataRequest;
			this.fileWriter = LiveDataRequest.getWriter();
			requestRun = () -> {
				while (connect) {
					try {
						liveDataRequest.makeRequest(liveDataRequest.getRequestBody());
					} catch (IOException | InterruptedException e) {
						System.out.println("runRequest");
					}
				}

			};
			writeRun = () -> {
				while (connect) {
					try {
						liveDataRequest.arrayData();
					} catch (InterruptedException | IOException e) {
					}
				}

			};
			countdownRun = () -> {
				while (connect) {
					liveDataRequest.countdown(4);
				}
			};
			writeQueueRun = () -> {
				while (connect || mapJSon.getQueue().peek() != null) {
					try {
						liveDataRequest.writeData(fileWriter, mapJSon.emptyQueue());
					} catch (InterruptedException | IOException | ExecutionException e) {
						System.out.println("WriteQueueRun Interrupted");
					}
				}
				try {
					fileWriter.flush();
					fileWriter.close();
				} catch (IOException e) {
				}
				executor.shutdown();
			};
			this.executor = Executors.newFixedThreadPool(4);
			this.t = new Thread(this, "Live Data Return");
			this.t.start();
		}

		@Override
		public void run() {
			executor.execute(requestRun);
			executor.execute(writeRun);
			executor.execute(countdownRun);
			executor.execute(writeQueueRun);
		}
	}

	public class LiveDataTokenString {
		private String wellID;
		private String treatmentID;
		private String token;

		LiveDataTokenString(String wellID, String treatmentID, String token) {
			this.wellID = wellID;
			this.treatmentID = treatmentID;
			this.token = token;
		}

		public String getToken() throws IOException, InterruptedException {

			String body = initialRequest();
			System.out.println(body);
			String token = body.split(":")[1].split(",")[0].replace("\"", "");
			return token;
		}

		public String initialRequest() throws IOException, InterruptedException {
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest req = (HttpRequest.newBuilder().header("content-type", "application/json")
					.header("accept", "application/json").header("authorization", token)
					.uri(URI.create("https://api.fracpro.ai:4000/api/v1/tokens/auth/wells/" + wellID + "/treatments/"
							+ treatmentID))
					.build());
			HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
			String body = resp.body();
			return body;
		}
	}

	public class RunFromTextData {
		RunFromTextData() {

		}
	}
}
