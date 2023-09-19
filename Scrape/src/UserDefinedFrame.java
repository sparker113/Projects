import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.CellRendererPane;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import exceltransfer.DataNames;
import exceltransfer.OperatorTemplateStageSummary;
import exceltransfer.TransferTemplate;

public class UserDefinedFrame extends JFrame {
	String operator;
	Rectangle rectangle;
	ArrayList<String> userDefined;
	ArrayList<String> channelMnemonics;
	ArrayList<String> sandTypes;
	ArrayList<String> directoryWells;
	JTable table;
	final static int FIRST = 0;
	final static int LAST = 1;

	UserDefinedFrame(Rectangle rectangle, String operator, ArrayList<String> channelMnemonics,
			ArrayList<String> sandTypes, ArrayList<String> directoryWells) {
		this.operator = operator;
		this.rectangle = rectangle;
		this.channelMnemonics = channelMnemonics;
		this.sandTypes = sandTypes;
		this.userDefined = getUserDefinedNames();
		this.directoryWells = directoryWells;
		if (checkNull()) {
			return;
		}
		nittyGritty();
		constructScrollPane();
		constructButtons();
		constructAddButton();
		constructSaveButton();
		constructSumButton();
		this.setLayout(new UserDefinedLayout());
		this.setVisible(true);
	}

	UserDefinedFrame(Rectangle rectangle, String operator, ArrayList<String> channelMnemonics) {
		this.operator = operator;
		this.rectangle = rectangle;
		this.channelMnemonics = channelMnemonics;
		this.userDefined = getUserDefinedNames();
		if (checkNull()) {
			return;
		}
		nittyGritty();
		constructScrollPane();
		constructButtons();
		constructAddButton();
		constructSaveButton();
		constructSumButton();
		this.setLayout(new UserDefinedLayout());
		this.setVisible(true);
	}

	public static String calculateDefinition(String definition, Map<String, String> sigVals,
			Map<String, ArrayList<String>> dataMap, Map<String, ArrayList<String>> summaryMap, String key) {
		if (definition.contains("\"")) {
			return definition.replace("\"", "");
		} else if (key.contains("Summary@")) {
			return calcArrayOperation(summaryMap, definition);
		}
		definition = replaceValueHoldersSigVals(definition, sigVals);
		while (!findFunctions(definition).equals("")) {
			String function = findFunctions(definition);
			definition = definition.replace(function, calculateFunction(function.split("\\(")[0],
					function.split("\\(")[1].split("\\)")[0], dataMap, summaryMap));
		}
		String temp;
		while ((temp = getParenthetical(definition)) != definition) {
			String temp2 = evaluateWithinParenthesis(temp);
			definition = definition.replace(temp, temp2);
		}
		String answer = evaluateWithinParenthesis(definition);
		return answer.replace("\\(", "").replace("\\)", "");
	}

	public static String calculateExpression(String valueCalcString) {
		String temp;
		while ((temp = getParenthetical(valueCalcString)) != valueCalcString) {
			String temp2 = evaluateWithinParenthesis(temp);
			valueCalcString = valueCalcString.replace(temp, temp2);
		}
		String answer = evaluateWithinParenthesis(valueCalcString);
		return answer.replace("\\(", "").replace("\\)", "");
	}

	public static Rectangle getFrameBounds() {
		int x = Toolkit.getDefaultToolkit().getScreenSize().width / 3;
		int y = Toolkit.getDefaultToolkit().getScreenSize().height / 4;
		int width = Toolkit.getDefaultToolkit().getScreenSize().width / 3;
		int height = Toolkit.getDefaultToolkit().getScreenSize().height / 2;
		return new Rectangle(x, y, width, height);
	}

	public final static String WELL_SPECIFIC_DATA = "wellSpecificData";

	public static boolean checkWellSpecific(String definition, String wellName) {
		Matcher matcher = Pattern.compile(WELL_SPECIFIC_DATA + "\\((.+?)\\)$").matcher(definition);
		if (matcher.find()) {
			String wellInString = getWellFromWellSpecString(matcher.group());
			return mainFrame.removeSpecialCharacters(wellInString).equals(mainFrame.removeSpecialCharacters(wellName));
		}
		return true;
	}

	public static String getWellFromWellSpecString(String wellSpecString) {
		Matcher matcher = Pattern.compile(",\\[(.+?)\\]\\)$").matcher(wellSpecString);
		if (matcher.find()) {
			String found = matcher.group();
			System.out.println("The well is: " + found.substring(2, found.length() - 2));
			return found.substring(2, found.length() - 2);
		}
		return wellSpecString;
	}

	private String[] getMnemonicsArray() {
		String[] mnemonics = new String[channelMnemonics.size()];
		int i = 0;
		for (String s : channelMnemonics) {
			mnemonics[i] = s;
			i++;
		}
		return mnemonics;
	}

	/*
	 * MAKE IT TO WHERE FUNCTIONS THAT TAKE AN ARBITRARY NUMBER OF ARGUMENTS WILL
	 * ADD ANOTHER PLACE HOLDER HOLDER WHEN A VALUE IS INPUT AND REMOVE THE PLACE
	 * HOLDER WHEN IT IS SAVED
	 */

	private final static String WELL_SPEC_FUNC_STRING = WELL_SPECIFIC_DATA + "(|Channel_For_Data_Set|,|Well_Name|)";

	private void constructButtons() {
		constructButton("split", "|Value_To_Split|", "|Delimiter|", "|Desired_Index|");
		constructButton("sqrt", "|Target_For_Operation|");
		constructButton("min", "|Channels_For_Data_Set|");
		constructButton("max", "|Channels_For_Data_Set|");
		constructButton("avg", "|Channels_For_Data_Set|");
		constructButton("stdDev", "|Channels_For_Data_Set|");
		constructButton("firstIndex", "|Channel_For_Data_Set|");
		constructButton("lastIndex", "|Summary_Column|");
		constructButton("getDataGreaterThan", "|Channel_For_Data_Set|,|Condition_Value|");
		constructButton("wellSpecificData", getWellSpecAction(WELL_SPEC_FUNC_STRING));
		constructButton(MULTI_SAND, "|Combined_Sand_Type|");
	}

	public final static String MULTI_SAND = "Multi_Sand_SubStage";

	private void constructSumButton() {
		JButton button = new JButton();
		button.setName("Add_Summary");
		button.setText("Add Column To Summary");
		button.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String dataName = JOptionPane.showInputDialog("Enter the name of the User-Defined data below");
				table.setValueAt("Summary@" + dataName, getLastRow(), 0);
				ArrayList<String> userDefinedNames = new ArrayList<>();
				try {
					ArrayList<String> prevUserDefined = OperatorTemplateStageSummary.readOperatorUserDefined(operator);
					if (prevUserDefined != null) {
						userDefinedNames.addAll(prevUserDefined);
					}
					userDefinedNames.add(dataName);
					writeNamesToText(userDefinedNames, operator);
				} catch (IOException e1) {
					System.out.println("IOException caught UserDefinedFrame::writeNamesToText");
				}
			}
		});
		this.add(button);
	}

	private Boolean checkNull() {
		if (userDefined == null) {
			String message = "You must add user-defined names in the" + " template for the operator before "
					+ "you can define their computations";
			JOptionPane.showMessageDialog(Main.yess,
					String.format("<html><div style=\"WIDTH\":%dpv>%s</div></html>", 400, message));
			return true;
		}
		return false;
	}

	private void nittyGritty() {
		this.setBounds(rectangle);
		this.setTitle("User-Define");
		this.setIconImage(
				new ImageIcon("C:\\Scrape\\Scrape.png").getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.getContentPane().setBackground(Color.getHSBColor(-.85f, .1f, .85f));
	}

	private JTable constructTable() {
		this.table = new JTable(100, 3);
		table.setBounds(0, 0, rectangle.width, rectangle.height * 3 / 5);
		table.getColumnModel().getColumn(0).setHeaderValue("User-Defined Name");
		table.getColumnModel().getColumn(1).setHeaderValue("Definition");
		table.getColumnModel().getColumn(2).setHeaderValue("Pro-Petro TR Address *IA");
		table.addKeyListener(new TableKeyPressed(table));
		table.setCellSelectionEnabled(true);
		
		table.addMouseListener(new DataPopupMouseListener());
		table.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (table.getSelectedColumn() == 0 && !e.isControlDown()) {
					e.consume();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub

			}

		});
		try {
			populateTable(table, userDefined);
		} catch (IOException e) {
			System.out.println("Exception caught populating table");
		}
		return table;
	}

	private void constructScrollPane() {
		JScrollPane scrollPane = new JScrollPane(constructTable());
		scrollPane.setName("scroll");
		scrollPane.setVisible(true);
		this.add(scrollPane);
	}

	private void populateTable(JTable table, ArrayList<String> userDefined) throws IOException {
		HashMap<String, HashMap<String, String>> userDefinedMap = readUserDefinedDefinitions(operator);
		if (userDefinedMap != null && !userDefinedMap.isEmpty()) {
			int i = 0;
			for (String s : userDefined) {
				table.setValueAt(s, i, 0);
				if (userDefinedMap.get(s) == null) {
					i++;
					continue;
				}
				table.setValueAt(userDefinedMap.get(s).get("Definition"), i, 1);
				table.setValueAt(userDefinedMap.get(s).get("Location"), i, 2);
				i++;
			}
		} else if (!userDefined.isEmpty()) {
			int i = 0;
			for (String s : userDefined) {
				table.setValueAt(s, i, 0);
				i++;
			}
		}

	}

	private void constructAddButton() {
		JButton button = new JButton();
		button.setName("Add_User_Defined");
		button.setText("Add User Defined");
		button.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String dataName = JOptionPane.showInputDialog("Enter the name of the User-Defined data below");
				table.setValueAt(dataName, getLastRow(), 0);
				try {
					ArrayList<String> prevUserDefined = OperatorTemplateStageSummary.readOperatorUserDefined(operator);
					if (prevUserDefined == null) {
						prevUserDefined = new ArrayList<>();
					}
					prevUserDefined.add(dataName);
					writeNamesToText(prevUserDefined, operator);
				} catch (IOException e1) {
					System.out.println("IOException caught UserDefinedFrame::writeNamesToText");
				}
			}
		});
		button.setVisible(true);
		this.add(button);
	}

	public static void writeNamesToText(ArrayList<String> userDefinedNames, String operator) throws IOException {
		File file = new File(TransferTemplate.PARENT_FOLDER+"\\" + operator + "\\User_Defined.txt");
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdir();
		}
		writeToText(userDefinedNames, file.getAbsolutePath());

	}

	private static void writeToText(ArrayList<String> array, String path) throws IOException {
		FileWriter fileWriter = new FileWriter(path);
		fileWriter.write("");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		for (String s : array) {
			bufferedWriter.append(s);
			bufferedWriter.newLine();
		}
		bufferedWriter.flush();
		bufferedWriter.close();
	}

	private int getLastRow() {
		int lastRow = 0;
		for (int i = 0; i < table.getRowCount(); i++) {
			if (table.getValueAt(i, 0) == null || String.valueOf(table.getValueAt(i, 0)).equals("")) {
				lastRow = i;
				break;
			}
		}
		return lastRow;
	}

	private void constructButton(String text, String... type) {
		JButton button = new JButton();
		button.setName(text);
		button.setText(text);
		button.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String previous = "";
				if (table.getValueAt(table.getSelectedRow(), 1) != null) {
					previous = String.valueOf(table.getValueAt(table.getSelectedRow(), table.getSelectedColumn()));
				}

				String addString = "";
				for (String s : type) {
					addString = addString + s + ",";
				}
				addString = addString.substring(0, addString.length() - 1);
				addString = addString + ")";
				table.setValueAt(previous + text + "(" + addString, table.getSelectedRow(), 1);
			}
		});
		button.setVisible(true);
		this.add(button);
	}

	private void constructButton(String text, AbstractAction abstractAction) {
		JButton button = new JButton();
		button.setName(text);
		button.setText(text);
		button.addActionListener(abstractAction);
		button.setVisible(true);
		this.add(button);
	}

	private AbstractAction getWellSpecAction(String template) {
		AbstractAction action = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int funcCol = 1;
				int row = getTableRow();
				String cellString = table.getValueAt(row, funcCol) != null ? table.getValueAt(row, funcCol).toString()
						: "";
				if (cellString.equals("")) {
					table.setValueAt(template, row, funcCol);
					return;
				}
				table.setValueAt(getStringFromClick(template, cellString), row, funcCol);
			}
		};
		return action;
	}

	private static String getStringFromClick(String template, String cellString) {
		Matcher matcher = Pattern.compile("\\|(.+?)\\|").matcher(template);
		if (matcher.find()) {
			return template.replace(matcher.group(), cellString);
		}
		return cellString + template;
	}

	private int getTableRow() {
		if (table.getSelectedRow() == -1) {
			return getNextEmptyRow(1);
		}
		return table.getSelectedRow();
	}

	private int getNextEmptyRow(int column) {
		int row = 0;
		while (table.getValueAt(row, column) != null && !table.getValueAt(row, column).toString().equals("")) {
			row++;
		}
		return row;
	}

	private void constructSaveButton() {
		JButton button = new JButton();
		button.setName("save");
		button.setText("SAVE");
		button.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					writeToTextFile(operator);
					writeUserDefinedNames(operator);
				} catch (IOException e1) {
					System.out.println("Exception caught: UserDefinedFrame::writeToTextFile()");
				}
				UserDefinedFrame.this.dispose();
			}
		});
		button.setVisible(true);
		add(button);
	}

	public static String removePlaceHolders(String cellText) {
		Matcher matcher = Pattern.compile("(,?)(\\|[\\w\\s\\d]*?\\|)").matcher(cellText);
		String newString = cellText;
		while (matcher.find()) {
			String found = matcher.group();
			if (found.charAt(0) == ',') {
				newString = newString.substring(0, matcher.start())
						+ newString.substring(matcher.end(), newString.length());
			} else {
				newString = removeWholeFunc(newString, matcher.start() - 1);
			}
			matcher.reset(newString);
		}
		return newString;
	}

	private static String removeWholeFunc(String text, int start) {
		int beginOfFunc = findBegOfLastWord(text, start);
		int end = beginOfFunc;
		for (int i = beginOfFunc; i <= text.length(); i++) {
			if (text.charAt(i) == ')') {
				end = i + 1;
				break;
			}
		}
		return beginOfFunc > 0 ? text.substring(0, beginOfFunc) + text.substring(end, text.length())
				: end == text.length() ? "0" : text.substring(end);
	}

	private static int findBegOfLastWord(String text, int start) {
		for (int i = start; i >= 0; i--) {
			Matcher matcher = Pattern.compile("[\\(\\w]").matcher(String.valueOf(text.charAt(i)));
			if (!matcher.find()) {
				return i;
			}
		}
		return 0;
	}

	private String getStringToWrite() {
		String userDefinitions = "";
		int lastRow = findLastRow();
		for (int i = 0; i <= lastRow; i++) {
			for (int ii = 0; ii < table.getColumnCount(); ii++) {
				String cellText = checkNullCell(table.getValueAt(i, ii));
				userDefinitions = userDefinitions + removePlaceHolders(cellText) + "<<";
			}
			userDefinitions = userDefinitions.substring(0, userDefinitions.length() - 2);
			userDefinitions += "\n";
		}
		if (userDefinitions.isEmpty()) {
			return "";
		} else {
			return userDefinitions;
		}
	}

	private String getNameStringToWrite() {
		String userDefinedNames = "";
		int i = 0;
		while (table.getValueAt(i, 0) != null && String.valueOf(table.getValueAt(i, 0)) != "") {
			userDefinedNames += String.valueOf(table.getValueAt(i, 0)) + "\n";
			i++;
		}
		if (i == 0) {
			return "";
		}
		return userDefinedNames.substring(0, userDefinedNames.length() - 1);
	}

	private void writeUserDefinedNames(String operator) throws IOException {
		String userDefinedNames = getNameStringToWrite();
		BufferedWriter bufferedWriter = new BufferedWriter(
				new FileWriter(TransferTemplate.PARENT_FOLDER+"\\" + operator + "\\User_Defined.txt"));
		for (String s : userDefinedNames.split("\n")) {
			bufferedWriter.append(s);
			bufferedWriter.newLine();
		}
		bufferedWriter.flush();
		bufferedWriter.close();
	}

	private String checkNullCell(Object tableCell) {
		if (tableCell == null || tableCell.equals("")) {
			return "0";
		}
		return String.valueOf(tableCell);
	}

	private void writeToTextFile(String operator) throws IOException {
		File file = new File(TransferTemplate.PARENT_FOLDER+"\\" + operator + "\\User_Definitions.txt");
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdir();
		}
		String userDefinitions = getStringToWrite();
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
		for (String s : userDefinitions.split("\n")) {
			bufferedWriter.append(s);
			bufferedWriter.newLine();
		}
		bufferedWriter.flush();
		bufferedWriter.close();
	}

	private static String reverseString(String statement) {
		StringBuilder reverseBuilder = new StringBuilder();
		for (int i = 0; i < statement.length(); i++) {
			reverseBuilder.insert(0, statement.charAt(i));
		}
		return reverseBuilder.substring(0);
	}

	private static String getTermFromReverse(String reverseStatement, int start) {
		Matcher matcher = Pattern.compile("((\\d|\\.)+)").matcher(reverseStatement);
		String term = "";
		if (matcher.find(start)) {
			term = reverseString(reverseStatement.substring(start, matcher.end()));
		} else {
			System.out.println("Incomplete operation in User-Defined Definitions");
			return "00000";
		}
		return term;
	}

	public static String calculateString(String term1, String term2, String operation) {

		switch (operation) {
		case ("^"):
			return FracCalculations.getDoubleRoundedString(Math.pow(Double.valueOf(term1), Double.valueOf(term2)), 2);
		case ("*"):
			return FracCalculations.getDoubleRoundedString(Double.valueOf(term1) * Double.valueOf(term2), 2);
		case ("/"):
			return FracCalculations.getDoubleRoundedString(Double.valueOf(term1) / Double.valueOf(term2), 2);
		case ("+"):
			return FracCalculations.getDoubleRoundedString(Double.valueOf(term1) + Double.valueOf(term2), 2);
		case ("-"):
			return FracCalculations.getDoubleRoundedString(Double.valueOf(term1) - Double.valueOf(term2), 2);
		}
		return "0";
	}

	private static String[] getOperations() {
		String[] operands = { "\\^", "\\*", "/", "\\+", "\\-" };
		return operands;
	}

	private static String termOfOperation(String statement, int start) {
		Matcher matcher = Pattern.compile("((\\d|\\.)+)").matcher(statement);
		String term = "";
		if (matcher.find(start)) {
			term = statement.substring(start, matcher.end());
		} else {
			// JOptionPane.showMessageDialog(null,"Incomplete operation in User-Defined
			// Definitions");
			System.out.println("Incomplete operation in User-defined Definitions: " + statement);
			return "0";
		}
		return term;
	}

	private static String nextOperation(String statement, String regex) {
		Matcher matcher = Pattern.compile("." + regex).matcher(statement);
		String firstTerm = "";
		String secondTerm = "";
		String newStatement = statement;
		while (matcher.find()) {
			Matcher matcher2 = Pattern.compile(regex).matcher(newStatement);
			matcher2.find();
			int location = matcher2.end();
			secondTerm = termOfOperation(newStatement, location);
			firstTerm = getTermFromReverse(reverseString(newStatement), newStatement.length() - location + 1);
			newStatement = newStatement.replace(firstTerm + regex.replace("\\", "") + secondTerm,
					calculateString(firstTerm, secondTerm, regex.replace("\\", "").replace("\\d", "")));
		}
		return newStatement;
	}

	private static String evaluateWithinParenthesis(String statement) {
		for (String s : getOperations()) {
			statement = nextOperation(statement, s);
		}
		return statement.replace("(", "").replace(")", "");
	}

	private static String getParenthetical(String definition) {
		Stack<Integer> openStack = new Stack();
		for (int i = 0; i < definition.length(); i++) {
			if (definition.charAt(i) == '(') {
				openStack.push(i);
			} else if (definition.charAt(i) == ')') {
				return definition.substring(openStack.pop().intValue(), i + 1);
			}
		}
		return definition;
	}

	private static String replaceValueHoldersSigVals(String definition, Map<String, String> sigVals) {
		System.out.println("Definition Before Redefining: " + definition);
		definition = redefineDefinition(definition);
		System.out.println("Definition After Redefining: " + definition);
		Matcher matcher = Pattern.compile("\\[((\\d|\\w|\\s|\\.)+)\\]").matcher(definition);
		String replacement = definition;
		while (matcher.find()) {
			String placeHolder = matcher.group();
			if (sigVals.keySet().contains(placeHolder.substring(1, placeHolder.length() - 1))) {
				replacement = replacement.replace(placeHolder,
						sigVals.get(placeHolder.substring(1, placeHolder.length() - 1)));
			}
		}
		return replacement;
	}

	private static String calculateFunction(String function, String parameter, Map<String, ArrayList<String>> dataMap,
			Map<String, ArrayList<String>> summaryMap) {
		int numParams = parameter.split(",").length;

		switch (function) {
		case ("split"):
			return split(parameter.split(",")[0], parameter.split(",")[1], Integer.valueOf(parameter.split(",")[2]));
		case ("min"):
			ArrayList<String>[] paramArrayMin = getArrayOfDataSets(parameter, dataMap);
			return String.valueOf(min(paramArrayMin));
		case ("max"):
			ArrayList<String>[] paramArrayMax = getArrayOfDataSets(parameter, dataMap);
			return String.valueOf(max(paramArrayMax));
		case ("avg"):
			ArrayList<String>[] paramArrayAvg = getArrayOfDataSets(parameter, dataMap);
			return String.valueOf(avg(paramArrayAvg));
		case ("sqrt"):
			String withinFunction = evaluateWithinParenthesis(parameter);
			return String.valueOf(Math.sqrt(Double.valueOf(withinFunction)));
		case ("lastIndex"):
			String lIndexKey = parameter.substring(1, parameter.length() - 1).replace("add::", "");
			return summaryMap.containsKey(lIndexKey) ? lastIndex(summaryMap.get(lIndexKey))
					: (dataMap.containsKey(lIndexKey) ? lastIndex(dataMap.get(lIndexKey)) : "0.0");
		case ("firstIndex"):
			String fIndexKey = parameter.substring(1, parameter.length() - 1).replace("add::", "");
			if(dataMap.containsKey(fIndexKey)) {
				dataMap.get(fIndexKey).remove(0);
			}
			return summaryMap.containsKey(fIndexKey) ? firstIndex(summaryMap.get(fIndexKey))
					: (dataMap.containsKey(fIndexKey) ? firstIndex(dataMap.get(fIndexKey)) : "0.0");
		case ("stdDev"):
			return String.valueOf(FracCalculations.calculateStdDev(getArrayOfDataSets(parameter, dataMap)[0]));
		case ("getDataGreaterThan"):
			String key = getDataGreaterThan(dataMap, parameter);
			return "[" + key + "]";
		default:
			return "NO NO NO";
		}
	}

	private static String getDataGreaterThan(Map<String, ArrayList<String>> dataMap, String parameter) {
		String[] params = getParameters(parameter);
		if (params.length < 2) {
			return "null";
		}
		String key = getUnusedKey(dataMap);
		if (params.length == 2) {
			dataMap.put(key, getArrayWithGreaterThanCondition(
					dataMap.get(params[0].substring(1, params[0].length() - 1)), Double.valueOf(params[1])));
			return key;
		}
		dataMap.put(key, getArrayWithGreaterThanCondition(dataMap.get(params[0].substring(1, params[0].length() - 1)),
				dataMap.get(params[1].substring(1, params[1].length() - 1)), Double.valueOf(params[2])));
		return key;
	}

	private static String[] getParameters(String parameters) {
		Matcher matcher = Pattern.compile("([^,$])+").matcher(parameters);
		String[] params = new String[0];
		while (matcher.find()) {
			String found = matcher.group();
			found = found.contains("add::") ? found.replace("add::", "") : found;
			params = addIndexToArray(params);
			params[params.length - 1] = found;
		}
		System.out.println("Parameters: " + params);
		return params;
	}

	private static String[] addIndexToArray(String[] stringArray) {
		int numIndeces = stringArray.length + 1;
		String[] newArray = new String[numIndeces];
		if (numIndeces == 1) {
			return newArray;
		}
		int i = 0;
		for (String s : stringArray) {
			newArray[i] = s;
			i++;
		}
		return newArray;
	}

	private static String getUnusedKey(Map<String, ArrayList<String>> dataMap) {
		int i = 0;
		while (dataMap.containsKey(String.valueOf(i))) {
			i++;
		}
		return String.valueOf(i);
	}

	private static String calcArrayOperation(Map<String, ArrayList<String>> summaryMap, String definition) {
		if (getMatchParams(definition) != null) {
			return calcArrayOperationForMatching(summaryMap, definition);
		}
		ArrayList<String> array = null;
		String key = getKeyFromDefinition(definition);

		if (key.equals("")) {
			return "";
		} else {
			array = summaryMap.get(key);
		}
		String operation = "";
		for (String s : getOperations()) {
			Matcher matcher = Pattern.compile(s).matcher(definition);
			if (matcher.find()) {
				operation = matcher.group();
				break;
			}
		}
		String operand = findOperandForArrayOp(definition);
		Double total = checkForTotalInDef(definition);
		if (total > 0.0) {
			return getCommaArray(array, operation, operand, total);
		}
		return getCommaArray(array, operation, operand);
	}

	private static Double checkForTotalInDef(String definition) {
		Matcher matcher = Pattern.compile("Total=\\d+").matcher(definition);
		if (matcher.find()) {
			Matcher matcher2 = Pattern.compile("\\d+").matcher(matcher.group());
			if (matcher2.find()) {
				return Double.valueOf(matcher2.group());
			}
		}
		return 0.0;
	}

	private static String calcArrayOperationForMatching(Map<String, ArrayList<String>> summaryMap, String definition) {
		String key = getKeyFromDefinition(definition);
		Map.Entry<String, String> matchParams = getMatchParams(definition);
		ArrayList<String> matchArray = getArrayWhenArrayMatches(summaryMap.get(key),
				summaryMap.get(matchParams.getKey()), matchParams.getValue());
		String operation = getOperationFromMatchingDef(definition);
		String operand = findOperandForArrayOp(getOperand(definition));
		Double total = checkForTotalInDef(definition);
		if (total > 0.0) {
			return getCommaArray(matchArray, operand, operation, total);
		}
		return getCommaArray(matchArray, operation, operand);
	}

	private static String getOperand(String definition) {
		Matcher matcher = Pattern.compile("[\\d\\.]+").matcher(definition);
		if (matcher.find()) {
			return matcher.group();
		}
		return "0";
	}

	private static String getOperationFromMatchingDef(String definition) {
		for (String s : getOperations()) {
			Matcher matcher = Pattern.compile(s).matcher(definition);
			if (matcher.find()) {
				return matcher.group();
			}
		}
		return "*";
	}

	private static Map.Entry<String, String> getMatchParams(String definition) {
		Matcher matcher = Pattern.compile("\\-\\>(.+?)\\=").matcher(definition);

		if (matcher.find()) {
			String found = matcher.group();
			String key = found.substring(2, found.length() - 1);
			Matcher valueMatch = Pattern.compile("(.+?)->").matcher(definition.substring(matcher.end()));

			String matchValue = valueMatch.find() ? valueMatch.group().substring(0, valueMatch.group().length() - 2)
					: definition.substring(matcher.end());
			return Map.entry(key, matchValue);
		}
		return null;
	}

	private static ArrayList<String> getArrayWhenArrayMatches(ArrayList<String> valueArray,
			ArrayList<String> matchArray, String valueMatch) {
		ArrayList<String> validArray = new ArrayList<>();
		int count = 0;
		for (String s : matchArray) {
			if (s.equals(valueMatch)) {
				validArray.add(valueArray.get(count));
				count++;
				continue;
			}
			validArray.add("0.0");
			count++;
		}
		return validArray;
	}

	private static String getCommaArray(ArrayList<String> array, String operand, String operation) {
		String commaArray = "";
		for (String s : array) {
			if (s.equals("")) {
				commaArray += "0.0,";
			} else {
				commaArray += Math.round(Double.valueOf(calculateString(s, operation, operand))) + ",";
			}
		}
		return commaArray.substring(0, commaArray.length() - 1);
	}

	private static String getCommaArray(ArrayList<String> array, String operand, String operation, Double total) {
		String commaArray = "";
		for (String s : array) {
			if (s.equals("")) {
				commaArray += "0.0,";
			} else {
				commaArray += Math.round(Double.valueOf(calculateString(s, operand, operation))) + ",";
			}
		}
		ArrayList<String> stringArray = getArrayFromCommaArray(commaArray.substring(0, commaArray.length() - 1));
		Double doubleTotal = SheetData.sumStringArray(stringArray);
		if (!doubleTotal.equals(total)) {
			stringArray = fixArrayTotal(stringArray, total - doubleTotal);
			return getCommaArrayFromArray(stringArray);
		}
		return commaArray.substring(0, commaArray.length() - 1);
	}

	private static String getCommaArrayFromArray(ArrayList<String> array) {
		String commaArray = "";
		for (String s : array) {
			commaArray += "," + s;
		}
		return commaArray.substring(1);
	}

	private static ArrayList<String> fixArrayTotal(ArrayList<String> stringArray, Double difference) {
		int lastNonZero = findLastNonZeroIndex(stringArray);
		double value = Double.parseDouble(stringArray.get(lastNonZero));
		Double replaceValue = value + difference;
		stringArray.remove(lastNonZero);
		stringArray.add(lastNonZero, String.valueOf(replaceValue));
		return stringArray;
	}

	private static int findLastNonZeroIndex(ArrayList<String> stringArray) {
		for (int i = stringArray.size() - 1; i >= 0; i--) {
			if (Double.valueOf(stringArray.get(i)) > 0.0) {
				return i;
			}
		}
		return 0;
	}

	private static ArrayList<String> getArrayFromCommaArray(String commaArray) {
		ArrayList<String> array = new ArrayList<>();
		for (String s : commaArray.split(",")) {
			array.add(s);
		}
		return array;
	}

	private static String findOperandForArrayOp(String definition) {
		Matcher matcher = Pattern.compile("(\\d|\\.)+").matcher(definition);
		if (matcher.find()) {
			return matcher.group();
		}
		return "0";
	}

	private static String getKeyFromDefinition(String definition) {
		Matcher matcher = Pattern.compile("\\[([\\w\\s]+)\\]").matcher(definition);
		if (matcher.find()) {
			String key = matcher.group();
			return key.substring(1, key.length() - 1);
		}
		return "";
	}

	private static int findNonEmptyString(ArrayList<String> array, int firstLast) {

		switch (firstLast) {
		case (0):
			int i = 0;
			for (String s : array) {
				if (!s.isBlank()) {
					return i;
				}
				i++;
			}
			break;
		case (1):
			for (int ii = array.size() - 1; ii >= 0; ii--) {
				if (!array.get(ii).isBlank()) {
					return ii;
				}
			}
			return array.size() - 1;
		}
		return 0;
	}

	private static ArrayList<String>[] getArrayOfDataSets(String parameter, Map<String, ArrayList<String>> dataMap) {
		int i = 0;
		ArrayList<String>[] paramArray = new ArrayList[1];
		for (String s : parameter.split(",")) {
			Matcher matcher = Pattern.compile("\\:\\:").matcher(s);
			String trimmed;
			if (matcher.find()) {
				trimmed = s.substring(matcher.end(), s.length() - 1);
			} else {
				trimmed = s.substring(1, s.length() - 1);
			}
			if (dataMap.keySet().contains(trimmed)) {
				paramArray = addIndexToArray(paramArray);
				paramArray[i] = dataMap.get(trimmed);
				i++;
			}
		}
		return paramArray;
	}

	@SuppressWarnings("unchecked")
	private static ArrayList<String>[] addIndexToArray(ArrayList<String>[] array) {
		if (array.length == 1 && array[0] == null) {
			return array;
		}
		ArrayList<String>[] newArray = new ArrayList[array.length + 1];
		for (int i = 0; i < array.length; i++) {
			newArray[i] = array[i];
		}
		return newArray;
	}

	private static String findFunctions(String definition) {

		System.out.println(definition);
		Matcher matcher = Pattern.compile("\\w+\\(([^\\)|\\(]+)\\)").matcher(definition);
		if (matcher.find()) {
			return matcher.group();
		} else {
			return "";
		}
	}

	public static String redefineDefinition(String definition) {
		Matcher matcher = Pattern.compile("^wellSpecificData\\(").matcher(definition);
		if (matcher.find()) {
			// String found = matcher.group();
			// Matcher chanMatcher =
			// Pattern.compile("\\[add\\:\\:\\:(.+?)\\]").matcher(found);
			Matcher wellMatcher = Pattern.compile(",\\[(.+?)\\)$").matcher(definition);
			int startWellParam = wellMatcher.find() ? wellMatcher.start() : definition.length();
			String userDefinition = definition.substring(matcher.end(), startWellParam);
			return userDefinition;
		}
		return definition;
	}

	public final static String DEFINITION = "Definition";
	public final static String LOCATION = "Location";
	public final static String CACHE_DEFINITION = "cache_definition";

	public static LinkedHashMap<String, HashMap<String, String>> readUserDefinedDefinitions(String operator)
			throws IOException {
		File file = new File(TransferTemplate.PARENT_FOLDER+"\\" + operator + "\\User_Definitions.txt");
		if (!file.exists()) {
			return null;
		}
		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		LinkedHashMap<String, HashMap<String, String>> userDefinedMap = new LinkedHashMap<>();
		String temp;
		while ((temp = bufferedReader.readLine()) != null && !temp.equals("")) {

			String key = temp.split("<<")[0];
			userDefinedMap.put(key, new HashMap<String, String>());
			userDefinedMap.get(key).put(DEFINITION, temp.split("<<")[1]);
			userDefinedMap.get(key).put(LOCATION, temp.split("<<")[2]);
		}

		bufferedReader.close();
		return userDefinedMap;
	}

	public static LinkedHashMap<String, HashMap<String, String>> readUserDefinedDefinitions(String operator,
			String wellName) throws IOException {
		File file = new File(TransferTemplate.PARENT_FOLDER+"\\" + operator + "\\User_Definitions.txt");
		if (!file.exists()) {
			return null;
		}
		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		LinkedHashMap<String, HashMap<String, String>> userDefinedMap = new LinkedHashMap<>();
		String temp;
		while ((temp = bufferedReader.readLine()) != null && !temp.equals("")) {
			if (!checkWellSpecific(temp.split("<<")[1], wellName)) {
				continue;
			}
			String key = temp.split("<<")[0];
			userDefinedMap.put(key, new HashMap<String, String>());
			userDefinedMap.get(key).put(DEFINITION, temp.split("<<")[1]);
			userDefinedMap.get(key).put(LOCATION, temp.split("<<")[2]);
		}
		bufferedReader.close();
		return userDefinedMap;
	}

	private int findLastRow() {
		int i = 0;
		while (table.getValueAt(i, 0) != null && !table.getValueAt(i, 0).toString().equals("")) {
			i++;
		}
		return i - 1;
	}

	private ArrayList<String> getUserDefinedNames() {
		ArrayList<String> userDefined = new ArrayList<>();
		try {
			userDefined.addAll(DataNames.readUserDefinedNames(operator));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "First, make the templates for this operator;"
					+ " then, use this window to define the user defined types");
			return null;
		}
		return userDefined;
	}

	public static Double sqrt(Double value) {
		return Math.sqrt(value);
	}

	public static String split(String target, String delimiter, int targetIndex) {
		return target.split(delimiter)[targetIndex].strip();
	}

	private static int getMaximumAllowableArraySize(ArrayList<String>[] arrays) {
		if (arrays.length == 0) {
			return 0;
		}
		int minSize = arrays[0].size();
		for (ArrayList<String> a : arrays) {
			if (a.size() < minSize) {
				minSize = a.size();
			}
		}
		return minSize;
	}

	private static int getMaximumAllowableArraySize(ArrayList<String>[] arrays, int numShort) {
		if (arrays.length == 0) {
			return 0;
		}
		int minSize = arrays[0].size();
		for (ArrayList<String> a : arrays) {
			if (a.size() < minSize) {
				minSize = a.size();
			}
		}
		return minSize - numShort;
	}

	@SafeVarargs
	@SuppressWarnings("unchecked")
	public static Double min(ArrayList<String>... dataArray) {
		Double min = 0.0;
		Double addedValues = 0.0;
		for (int i = 0; i < getMaximumAllowableArraySize(dataArray, 10); i += 10) {
			if (i == 0) {
				min = maxOfXIndeces(10, i, dataArray);
				continue;
			}
			addedValues = maxOfXIndeces(10, i, dataArray);
			min = addedValues < Math.abs(min) & addedValues >= 0.1 ? addedValues : min;
		}

		return min;
	}

	@SafeVarargs
	public static Double min(Double floor, ArrayList<String>... dataArray) {
		Double min = floor;
		Double addedValues = 0.0;
		for (int i = 0; i < getMaximumAllowableArraySize(dataArray, 10); i += 10) {
			if (i == 0) {
				min = maxOfXIndeces(10, i, dataArray);
				continue;
			}
			addedValues = maxOfXIndeces(10, i, dataArray);
			min = addedValues < Math.abs(min) & addedValues >= floor ? addedValues : min;
		}

		return min;
	}

	public static Double maxOfXIndeces(int numOfAddIndeces, int start, ArrayList<String>... dataArray) {
		Double max = 0.0;

		here: for (int i = start; i < start + numOfAddIndeces; i++) {
			Double added = 0.0;
			for (ArrayList<String> array : dataArray) {
				if (i > array.size() - 1) {
					break here;
				}
				added = added + Double.valueOf(array.get(i));
			}
			max = added > max ? added : max;
		}
		return max;
	}

	public static String firstIndex(ArrayList<String> summaryArray) {
		return summaryArray.get(findNonEmptyString(summaryArray, FIRST));
	}

	public static String lastIndex(ArrayList<String> summaryArray) {
		return summaryArray.get(findNonEmptyString(summaryArray, LAST));
	}

	public static Double minOfXIndeces(int numOfAddIndeces, int start, ArrayList<String>... dataArray) {
		Double min = null;
		here: for (int i = start; i < start + numOfAddIndeces; i++) {
			Double added = 0.0;
			for (ArrayList<String> array : dataArray) {
				if (i > array.size() - 1) {
					break here;
				}
				added = added + Double.valueOf(array.get(i));
			}
			min = min != null && added < min ? added : min != null ? min : added;
		}
		return min != null ? min : 0.0;
	}

	public static Double addIndexOfArrays(int index, ArrayList<String>... dataArray) {
		double value = 0.0;
		for (ArrayList<String> array : dataArray) {
			value += Double.valueOf(array.get(index));
		}
		return value;
	}

	@SafeVarargs
	@SuppressWarnings("unchecked")
	public static Double max(ArrayList<String>... dataArray) {
		Double maximum = 0.0;
		Double max = 0.0;
		Double addedValues = 0.0;
		for (int i = 0; i < getMaximumAllowableArraySize(dataArray); i++) {
			addedValues = 0.0;
			addedValues = minOfXIndeces(4, i, dataArray);
			max = addedValues > max ? addedValues : max;
		}
		maximum = max;
		return FracCalculations.getDoubleRoundedDouble(maximum, 2);
	}

	public static Double max(boolean smooth, ArrayList<String>... dataArray) {
		Double maximum = 0.0;
		Double max = 0.0;
		Double addedValues = 0.0;
		for (int i = 0; i < getMaximumAllowableArraySize(dataArray); i++) {
			addedValues = 0.0;
			addedValues = smooth ? minOfXIndeces(4, i, dataArray) : addIndexOfArrays(i, dataArray);
			max = addedValues > max ? addedValues : max;
		}
		maximum = max;
		return FracCalculations.getDoubleRoundedDouble(maximum, 2);
	}

	private static ArrayList<Integer> getEqualPartArrays(ArrayList<?> array, int subArrays) {
		ArrayList<Integer> indexArray = new ArrayList<>();
		int sizeArrays = array.size() / subArrays;
		indexArray.add(0);
		for (int i = 1; i < subArrays; i++) {
			indexArray.add(i * sizeArrays);
		}
		indexArray.add(array.size());
		return indexArray;
	}

	public static ArrayList<String> getArrayWithGreaterThanCondition(ArrayList<String> getFromArray,
			ArrayList<String> conditionArray, Double greaterThan, Double maxValue) {
		ArrayList<String> array = new ArrayList<>();

		int i = 0;
		while (i < getFromArray.size() && i < conditionArray.size()) {
			Matcher matcher = Pattern.compile("[^\\d|\\.]").matcher(conditionArray.get(i));
			if (matcher.find()) {
				i++;
				continue;
			}
			Double value = Double.valueOf(getFromArray.get(i));
			if (Double.valueOf(conditionArray.get(i)) >= greaterThan && value < maxValue) {
				array.add(String.valueOf(value));
			}
			i++;
		}
		return array;
	}
	public static ArrayList<String> getArrayWithBoundaryCondition(ArrayList<String> getFromArray,
			Double greaterThan, Double lessThan) {
		ArrayList<String> array = new ArrayList<>();
		for(String s:getFromArray) {
			Matcher matcher = Pattern.compile("[^\\d\\.]").matcher(s);
			if(matcher.find()) {
				continue;
			}
			double value = Double.parseDouble(s);
			if(value>greaterThan&value<lessThan) {
				array.add(s);
			}
		}
		return array;
	}
	public static ArrayList<String> getArrayWithBoundaryCondition(ArrayList<String> getFromArray,
			ArrayList<String> conditionArray, Double greaterThan, Double lessThan) {
		ArrayList<String> array = new ArrayList<>();
		int i = 0;
		while(i< getFromArray.size() && i < conditionArray.size()) {
			Matcher matcher = Pattern.compile("[^\\d\\.]").matcher(conditionArray.get(i));
			if(matcher.find()) {
				i++;
				continue;
			}
			double value = Double.parseDouble(conditionArray.get(i));
			if(value>greaterThan&value<lessThan) {
				array.add(getFromArray.get(i));
			}
			i++;
		}
		return array;
	}

	public static ArrayList<String> getArrayWithGreaterThanCondition(ArrayList<String> getFromArray,
			ArrayList<String> conditionArray, Double greaterThan) {
		ArrayList<String> array = new ArrayList<>();
		int i = 0;
		while (i < getFromArray.size() && i < conditionArray.size()) {
			Matcher matcher = Pattern.compile("[^\\d|\\.]").matcher(conditionArray.get(i));
			if (matcher.find()) {
				i++;
				continue;
			}
			if (Double.valueOf(conditionArray.get(i)) >= greaterThan) {
				array.add(getFromArray.get(i));
			}
			i++;
		}
		return array;
	}

	public static ArrayList<String> getArrayWithGreaterThanCondition(ArrayList<String> getFromArray,
			Double greaterThan) {
		ArrayList<String> array = new ArrayList<>();
		int i = 0;
		while (i < getFromArray.size()) {
			Matcher matcher = Pattern.compile("[^\\d|\\.]").matcher(getFromArray.get(i));
			if (matcher.find()) {
				i++;
				continue;
			}
			if (Double.valueOf(getFromArray.get(i)) >= greaterThan) {
				array.add(getFromArray.get(i));
			}
			i++;
		}
		return array;
	}

	public static ArrayList<String> getArrayWithLessThanCondition(ArrayList<String> getFromArray,
			ArrayList<String> conditionArray, Double lessThan) {
		ArrayList<String> array = new ArrayList<>();
		int i = 0;
		while (i < getFromArray.size() && i < conditionArray.size()) {
			Matcher matcher = Pattern.compile("[^\\d|\\.]").matcher(conditionArray.get(i));
			if (matcher.find()) {
				i++;
				continue;
			}
			if (Double.valueOf(conditionArray.get(i)) <= lessThan) {
				array.add(getFromArray.get(i));
			}
			i++;
		}
		return array;
	}

	public static ArrayList<String> getArrayWithinIndeces(ArrayList<String> array, int lowerBound, int upperBound) {
		ArrayList<String> newArray = new ArrayList<>();
		for (int i = lowerBound; i < upperBound; i++) {
			if (i > array.size()) {
				return newArray;
			}
			newArray.add(array.get(i));
		}
		return newArray;
	}

	public static ArrayList<String> getArrayWithLessThanCondition(ArrayList<String> getFromArray, Double lessThan) {
		ArrayList<String> array = new ArrayList<>();
		int i = 0;
		while (i < getFromArray.size()) {
			Matcher matcher = Pattern.compile("[^\\d|\\.]").matcher(getFromArray.get(i));
			if (matcher.find()) {
				i++;
				continue;
			}
			if (Double.valueOf(getFromArray.get(i)) <= lessThan) {
				array.add(getFromArray.get(i));
			}
			i++;
		}
		return array;
	}

	@SafeVarargs
	public static Double avg(ArrayList<String>... dataArray) {
		Double average = 0.0;
		ArrayList<Double> addedArray = new ArrayList<>();
		Double added = 0.0;
		for (int i = 0; i < getMaximumAllowableArraySize(dataArray); i++) {
			added = 0.0;
			for (ArrayList<String> array : dataArray) {

				added += array.get(i).matches("([A-Za-z\\s]+)") ? 0.0 : Double.valueOf(array.get(i));
			}
			addedArray.add(added);
		}
		SplitAverage<Double> splitAverage = null;
		try {
			splitAverage = new SplitAverage<>(getEqualPartArrays(addedArray, 5), addedArray);
		} catch (InterruptedException e) {
			System.out.println("Split Average Interrupted");
		}
		double avg = 0.0;
		int count = 0;
		ArrayList<Double> weights = new ArrayList<>();
		ArrayList<Double> averages = splitAverage.getAverages();
		for (Double a : averages) {
			avg += a;
		}
		average = avg / averages.size();
		return FracCalculations.getDoubleRoundedDouble(average, 2);
	}

	private class DataPopupMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() > MouseEvent.BUTTON1) {
				new DataPopup(e.getXOnScreen(), e.getYOnScreen(), e.getY());
			}

		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub

		}

	}

	private class UserDefinedLayout extends SpringLayout {
		int lowestComponentY = 0;

		UserDefinedLayout() {
			layoutConstruction();
		}

		void layoutConstruction() {
			int buttonColumn = 0;
			int buttonRow = 1;
			int buttonWidth = (rectangle.width - 12) / 3;
			for (Component c : getContentPane().getComponents()) {
				String name = c.getName();
				switch (name) {
				case ("scroll"):
					scrollLayout((JScrollPane) c);
					break;
				case ("save"):
					saveButtonLayout((JButton) c);
					break;
				case ("Add_User_Defined"):
					addButtonLayout((JButton) c);
					break;
				case ("Add_Summary"):
					sumButtonLayout((JButton) c);
					break;
				default:
					buttonColumn++;
					buttonLayout((JButton) c, buttonWidth, 25, buttonRow, buttonColumn);
					buttonRow += buttonColumn / 3;
					buttonColumn %= 3;
				}
			}
		}

		void addButtonLayout(JButton button) {
			putConstraint(NORTH, button, lowestComponentY + 15, NORTH, getContentPane());
			putConstraint(SOUTH, button, lowestComponentY + 40, NORTH, getContentPane());
			putConstraint(WEST, button, saveButtonX(150, 1), WEST, getContentPane());
			putConstraint(EAST, button, saveButtonX(150, 1) + 150, WEST, getContentPane());
		}

		void saveButtonLayout(JButton button) {
			putConstraint(NORTH, button, lowestComponentY + 15, NORTH, getContentPane());
			putConstraint(SOUTH, button, lowestComponentY + 40, NORTH, getContentPane());
			putConstraint(WEST, button, saveButtonX(150, 2), WEST, getContentPane());
			putConstraint(EAST, button, saveButtonX(150, 2) + 150, WEST, getContentPane());
		}

		void sumButtonLayout(JButton button) {
			putConstraint(NORTH, button, lowestComponentY + 15, NORTH, getContentPane());
			putConstraint(SOUTH, button, lowestComponentY + 40, NORTH, getContentPane());
			putConstraint(WEST, button, saveButtonX(150, 3), WEST, getContentPane());
			putConstraint(EAST, button, saveButtonX(150, 3) + 150, WEST, getContentPane());
		}

		int saveButtonX(int width, int rightLeft) {
			int centerX = Math.round(rectangle.width * rightLeft / 4);
			return centerX - width / 2;
		}

		void buttonLayout(JButton button, int width, int height, int row, int column) {
			int y = getStartButtonY();

			putConstraint(NORTH, button, y + (height * (row - 1)), NORTH, getContentPane());
			putConstraint(SOUTH, button, y + (height * row), NORTH, getContentPane());
			putConstraint(WEST, button, getButtonX(column, width), WEST, getContentPane());
			putConstraint(EAST, button, getButtonX(column, width) + width, WEST, getContentPane());
			lowestComponentY = row * height + y;
		}

		void scrollLayout(JScrollPane scrollPane) {
			putConstraint(NORTH, scrollPane, 0, NORTH, getContentPane());
			putConstraint(SOUTH, scrollPane, rectangle.height * 3 / 5, NORTH, getContentPane());
			putConstraint(WEST, scrollPane, 0, WEST, getContentPane());
			putConstraint(EAST, scrollPane, rectangle.width - 12, WEST, getContentPane());
		}

		int getButtonX(int column, int width) {
			int numCols = rectangle.width / width;
			double doubleX = (column - 1) / Double.valueOf(numCols);
			Long longX = Math.round(doubleX * Double.valueOf(rectangle.width));
			return longX.intValue();
		}

		int getStartButtonY() {
			return rectangle.height * 3 / 5;
		}
	}

	private class DataPopup extends JFrame {
		int localY;
		Rectangle rectangle;

		DataPopup(int x, int y, int localY) {
			this.rectangle = new Rectangle(x, y, 600, 50 + 40);
			this.localY = localY;
			nittyGritty();
			construct();
			this.setVisible(true);

		}

		void nittyGritty() {
			this.setBounds(rectangle);
			this.getContentPane().setBackground(Color.getHSBColor(-.85f, .1f, .85f));
			this.setIconImage(
					new ImageIcon("C:\\Scrape\\Scrape.png").getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
			this.setTitle("Data Names");
		}

		void construct() {
			constructComboBox("Data_Array");
			constructComboBox("Data_Value");
			constructComboBox("User_Defined");
			constructComboBox("Summary_Array");
			getContentPane().setLayout(new DataPopupLayout());

		}

		String[] addFromFile(String[] list) {
			ArrayList<String> sandTypes = null;
			try {
				sandTypes = ArgumentsToText.readArguments("C:\\Scrape\\sand.txt");
			} catch (IOException e) {
				System.out.println("NONONOONO");
				return list;
			}
			String[] newArray = new String[list.length + sandTypes.size()];
			int i = 0;
			for (String s : list) {
				newArray[i] = s;
				i++;
			}
			for (String s : sandTypes) {
				newArray[i] = s;
			}
			return newArray;
		}

		void constructComboBox(String name) {
			JComboBox<String> comboBox = new JComboBox<>();
			comboBox.setRenderer(new PopupCellRenderer());
			switch (name) {
			case ("Data_Array"):
				comboBox.addItem("<Channels>");
				comboBox.addItem("Prop. Concentration");
				comboBox.addItem("Clean Grand Total");
				comboBox.addItem("Treating Pressure");
				comboBox.addItem("Slurry Rate");
				comboBox.addItem("Slurry Grand Total");
				comboBox.addItem("Stage Number");
				comboBox.addItem("Backside");
				comboBox.addItem("Plot Prop. Concentration");
				comboBox.addItem("Additional Channel");
				comboBoxNittyGritty(name, comboBox);
				this.add(comboBox);
				break;
			case ("Data_Value"):
				String[] sigVals = DataNames.getDataNamesForTable();
				sigVals = addFromFile(sigVals);
				comboBox.addItem("<Sig Val Data>");
				for (String s : sigVals) {
					comboBox.addItem(s);
				}
				if (sandTypes != null) {
					for (String s : sandTypes) {
						System.out.println(s);
						comboBox.addItem(s);
					}
					for (String s : directoryWells) {
						comboBox.addItem(s);
					}
				}
				comboBoxNittyGritty(name, comboBox);
				this.add(comboBox);
				break;
			case ("User_Defined"):
				comboBox.addItem("<User Defined>");
				for (String s : userDefined) {
					comboBox.addItem(s);
				}
				comboBoxNittyGritty(name, comboBox);
				this.add(comboBox);
				break;
			case ("Summary_Array"):
				comboBox.addItem("<Treatment Summary>");
				for (String s : DataNames.getSummaryColumnNames()) {
					comboBox.addItem(s);
				}
				comboBox.addItem(MULTI_SAND);
				comboBoxNittyGritty(name, comboBox);
				this.add(comboBox);
			}
		}

		void comboBoxNittyGritty(String name, JComboBox<String> comboBox) {
			comboBox.setName(name);
			comboBox.setEditable(false);
			comboBox.setAlignmentX(SwingConstants.CENTER);
			comboBox.addPopupMenuListener(new DataPopupListener());
			comboBox.setLightWeightPopupEnabled(false);
			// addActionsToMenu(comboBox);
			comboBox.setVisible(true);
		}

		int getRowAtPoint(int y) {
			return y / table.getRowHeight();
		}

		class DataPopupLayout extends SpringLayout {
			DataPopupLayout() {
				construct();
			}

			void construct() {
				int i = 1;
				for (Component c : getContentPane().getComponents()) {
					comboBoxLayout(i, (JComboBox<String>) c);
					i++;
				}
			}

			void comboBoxLayout(int index, JComboBox<String> comboBox) {
				putConstraint(NORTH, comboBox, 15, NORTH, getContentPane());
				putConstraint(SOUTH, comboBox, 35, NORTH, getContentPane());
				putConstraint(WEST, comboBox, getBoxX(index) + 5, WEST, getContentPane());
				putConstraint(EAST, comboBox, getBoxX(index + 1) - 5, WEST, getContentPane());
			}

			int getBoxX(int index) {
				return (index - 1) * (rectangle.width / 4);
			}

		}

		@SuppressWarnings("unused")
		private class PopupCellRenderer extends DefaultListCellRenderer {
			@Override
			@SuppressWarnings("rawtypes")
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				setToolTipText((String) value);
				return this;
			}
		}

		class DataPopupListener implements PopupMenuListener {
			CellRendererPane cellRendererPane;

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				@SuppressWarnings("unchecked")
				JComboBox<String> comboBox = (JComboBox<String>) e.getSource();
				if (comboBox.getSelectedItem().toString().contains("<")) {
					return;
				}
				String sourceText = comboBox.getSelectedItem().toString();
				if (sourceText.contains("Additional Channel")) {
					Executors.newSingleThreadExecutor().execute(() -> {
						String[] mnemonics = getMnemonicsArray();
						String selectedData = (String) JOptionPane.showInputDialog(
								((Component) e.getSource()).getParent(), "Select a channel", "Channel Selection",
								JOptionPane.PLAIN_MESSAGE, null, mnemonics, mnemonics[1]);
						String text = modifyForPlaceHolder(getPreviousText(), "[add::" + selectedData + "]", true);

						table.setValueAt(text, getRowAtPoint(localY), 1);
					});
					dispose();
				} else {
					String text = modifyForPlaceHolder(getPreviousText(),
							"[" + comboBox.getSelectedItem().toString() + "]", false);
					table.setValueAt(text, getRowAtPoint(localY), 1);
					dispose();
				}
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}

			String getPreviousText() {
				if (table.getValueAt(getRowAtPoint(localY), 1) == null) {
					return "";
				} else {
					return table.getValueAt(getRowAtPoint(localY), 1).toString();
				}
			}

			String findSubString(String previousText) {
				Matcher matcher = Pattern.compile("\\|(\\w+)\\|").matcher(previousText);
				while (matcher.find()) {
					String found = matcher.group();
					return (found);
				}
				return "";
			}

			String modifyForPlaceHolder(String previousText, String selectedText, boolean extend) {
				String subString = findSubString(previousText);
				String replacement = extend ? selectedText + "," + subString : selectedText;
				if (!subString.isEmpty()) {
					return previousText.replace(subString, replacement);
				}
				return previousText + replacement;
			}
		}

	}
}
