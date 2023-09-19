import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;

import exceltransfer.DataNames;
import exceltransfer.OperatorTemplateStageSummary;

public class OperatorTemplateSummary extends OperatorTemplateStageSummary {
	public final static String DEFAULT_NAME = "tranfer_1";
	OperatorTemplateSummary(Rectangle rectangle, String operator) throws IOException, ClassNotFoundException {
		super(rectangle, operator,DEFAULT_NAME);
	}

	@Override
	public void addDefaultBoxList(SigValuesComboBox box) {
		ArrayList<String> dataNames = DataNames.getDataNamesForSummary();
		for (String s : DataNames.getDataNames()) {
			dataNames.add(s);
		}
		try {
			dataNames.addAll(DataNames.readUserDefinedNames(operator));
		} catch (IOException e) {
			System.out.println("IOException reading user_defined names");
		}
		box.addAll(dataNames);
	}

	@Override
	public void constructInputPanels() {
		this.add(new InputPanel(getPanelRectangle(1), String.format("<html><div style=\"width\":%dpx>%s</div></html>",
				getPanelWidth() / 2, getWorkbookSuffixPrompt()), "Workbook"));
		this.add(new InputPanel(getPanelRectangle(2), String.format("<html><div style=\"width\":%dpx>%s</div></html>",
				getPanelWidth() / 2, getStageSummaryPrompt()), "Worksheet"));
		this.add(new InputPanel(getPanelRectangle(3), String.format("<html><div style=\"width\":%dpx>%s</div></html>",
				getPanelWidth() / 2, getStageOffsetPrompt()), "Offset"));
	}

	public String getStageOffsetPrompt() {
		String prompt = "If there is an offset between the rows in the 'Stage 1' sheet and subsequent stage sheet; "
				+ "input the row offset for the 'Stage 1' sheet";
		return prompt;
	}

	@Override
	public String getWorkbookSuffixPrompt() {
		String prompt = "What is the suffix of the file name containing the 'Treatment Summary'?"
				+ " Example: For 'WELL 1H - TR.xlsm'; you would input ' - TR.xlsm'";
		return prompt;
	}

	@Override
	public String getStageSummaryPrompt() {
		String sheetPrompt = "What is the stage sheet name format? Input 'Stage #'"
				+ " if each stage sheet is prefixed by 'Stage ' followed by the stage number.";
		return sheetPrompt;
	}


}
