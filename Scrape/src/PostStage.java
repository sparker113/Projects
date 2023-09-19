import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class PostStage implements Runnable {
	Boolean plotsMade = false;
	PJRFilesPane pjrFilesPane;

	PostStage(PJRFilesPane pjrFilesPane) {
		this.pjrFilesPane = pjrFilesPane;
	}

	PostStage() {

	}

	public static void mergePDFs(PJRFilesPane pjrFilesPane) {
		Processes.treatmentPDF();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}
		String mergeString = getChecks(pjrFilesPane);
		new MergePDFs(mergeString);
	}

	@Override
	public synchronized void run() {
		new TRpdf();
		new ReportPlots();
		while (!plotsMade)
			try {
				wait();
			} catch (InterruptedException e) {
				try {
					new TextLog("PostStage Interrupted");
				} catch (IOException e1) {
				}
			}
		String mergeString = getChecks(pjrFilesPane);
		new MergePDFs(mergeString);
		JOptionPane.showMessageDialog(null, "PJR complete");
	}

	public static String getChecks(PJRFilesPane pjrFilesPane) {
		int i = 1;
		StringBuilder mergeArgs = new StringBuilder();
		for (PJRFilesPane.CheckBox a : pjrFilesPane.getCheckList()) {
			if (a.isChecked()) {
				mergeArgs.append("\".");
				mergeArgs.append(i);
				mergeArgs.append("\" ");
			}
			i++;
		}
		String mergeString = mergeArgs.substring(0, mergeArgs.length() - 1);
		System.out.println(mergeString);
		return mergeString;
	}

	synchronized void PostStageCM() {
		/*
		 * String well = String.valueOf(Main.yess.textCombo1.getSelectedItem()); try {
		 * String suffix =
		 * ReadDirectory.readDirect().split("\\")[ReadDirectory.readDirect().split("\
		 * \").length - 1] + " - Invoices.xlsm"; XSSFWorkbook workbook =
		 * InvoiceEvaluate.GetSpreadSheet.getWorkbook(suffix); XSSFSheet sheet =
		 * workbook.getSheet(well); InvoiceEvaluate evaluate = new
		 * InvoiceEvaluate(sheet,workbook); workbook = evaluate.evaluate();
		 * saveWorkbook(workbook); } catch (IOException e) { }
		 */

		new CMemail();
		JOptionPane.showMessageDialog(null, "Company man email sent");
	}

	public void saveInvoice() {
		new InvoicePDF();
	}

	public void saveWorkbook(XSSFWorkbook workbook) throws FileNotFoundException, IOException {
		String pad = ReadDirectory.readDirect().split("\\")[ReadDirectory.readDirect().split("\\").length - 1];
		FileOutputStream fOS = new FileOutputStream(ReadDirectory.readDirect() + "\\" + pad + " - Invoices.xlsm");
		workbook.write(fOS);
	}

	public static class FileUploads {
		FileUploads(String type) {
			new HTTPpython(type);
		}
	}

	public static class ScrapeWA {
		ScrapeWA() {
			new WAoutlookScrape();
		}
	}

	public static class MakePlots {
		MakePlots() {
			new ReportPlots();
		}
	}
}
