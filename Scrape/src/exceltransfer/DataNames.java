package exceltransfer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class DataNames {
	public final static String AVERAGE_PRESSURE = "Average Pressure";
	public final static String AVERAGE_RATE = "Average Rate";
	public final static String WELL_NAME = "Well Name";
	public final static String STAGE_NUMBER = "Stage Number";
	public final static String START_DATE = "Start Date";
	public final static String START_TIME = "Start Time";
	public final static String END_DATE = "End Date";
	public final static String PUMPS_START = "Pumps Start";
	public final static String PUMPS_END = "Pumps End";
	public final static String FRAC_GRADIENT = "Frac Gradient";
	public final static String TVD = "TVD";
	public final static String ISIP = "ISIP";
	public final static String PAD_NAME = "Pad Name";
	public final static String PERFS = "Perfs";
	public final static String CLOSE_PRESSURE = "Close Pressure";
	public final static String OPEN_PRESSURE = "Open Pressure";
	public final static String MAX_PRESSURE = "Max Pressure";
	public final static String MAX_RATE = "Max Rate";
	public final static String BREAK_PRESSURE = "Breakdown Pressure";
	public final static String BREAK_TIME = "Breakdown Time";
	public final static String BREAK_VOLUME = "Breakdown Volume";
	public final static String BREAK_RATE = "Breakdown Rate";
	public final static String END_TIME = "End Time";
	public final static String CLEAN_GRAND = "Clean Total";
	public final static String SLURRY_GRAND = "Slurry Total";
	public final static String DIESEL = "Diesel";
	public final static String PUMPS_BLENDING = "Pumps Blending";
	public final static String CHEMICAL_NAME_START = "Chemical Name Range Start";
	public final static String CHEMICAL_VOLUME_START = "Chemical Volume Range Start";
	public final static String SAND_NAME_START = "Sand Name Range Start";
	public final static String SAND_VOLUME_START = "Sand Volume Range Start";
	public final static String CHEMICAL_NAME_END = "Chemical Name Range End";
	public final static String CHEMICAL_VOLUME_END = "Chemical Volume Range End";
	public final static String SAND_NAME_END = "Sand Name Range End";
	public final static String SAND_VOLUME_END = "Sand Volume Range End";
	public final static String PUMP_TIME = "Pump Time";
	public final static String CREW = "Crew";
	public final static String ACID_VOLUME = "Acid Volume";
	public final static String START_TIME_DATE = "Start Time Date";
	public final static String END_TIME_DATE = "End Time Date";
	public final static String ISIP_TIME = "ISIP Time";
	public final static String BACKSIDE_PRESSURE = "Backside Pressure";
	public final static String PUMPDOWN = "Pumpdown";
	public final static String PUMPDOWN_ACID = "Pumpdown Acid";
	public final static String AVERAGE_HORSEPOWER = "Average Horsepower";
	public final static String MAX_PROP_CON = "Max Prop. Concentration";
	public final static String ACID_RATE = "Acid Rate";
	public final static String CLEAN_TOTAL = "Clean Total";
	public final static String SLURRY_TOTAL = "Slurry Total";
	public final static String PRESSURE_AT_MAX_RATE = "Pres. At Max Rate";
	public final static String SAND_START_RATE = "Sand Start Rate";
	public final static String VOLUME_DESIGN_RATE = "Vol. To Design Rate";
	public final static String DOWNTIME = "Downtime";
	public final static String USER_DEFINED = "User-Defined";
	public final static String COMMENTS = "Comments";
	public final static String COUNTY = "County";
	public final static String BLENDER_ASSET_ID = "Blender Asset ID";


	//// FINISH FOR EACH DATA NAME

	public final static String AVERAGE_SUFFIX = "_AVERAGE";

	public final static String TOTAL_SUFFIX = "_TOTAL";

	public final static String EMPTY_CELL_VALUE = "Next Empty Cell";

	public final static String SAND = "sand";
	public final static String CHEMICALS = "chemicals";

	private final static String VOLUME = "volume";

	private final static String NAME = "name";

	public final static String SUMMARY_PROP_CON = "PROPPANT CON";

	public final static String SUMMARY_SAND_SUBSTAGE_VOLUME = "SAND VOLUME";

	public final static String SUMMARY_START_TIME = "START TIME";

	public final static String SUMMARY_END_TIME = "END TIME";
	public final static String SUMMARY_STAGE_NUMBER = "STAGE NUMBER";
	public final static String SUMMARY_START_DATE = "START DATE";
	public final static String SUMMARY_END_DATE = "END DATE";
	public final static String SUMMARY_CLEAN_BBLS = "CLEAN BBLS";
	public final static String SUMMARY_AVG_PRESSURE = "AVERAGE PRESSURE";
	public final static String SUMMARY_AVG_RATE = "AVERAGE RATE";
	public final static String SUMMARY_SLURRY_BBLS = "SLURRY BBLS";
	public final static String SUMMARY_SAND_TYPE = "SAND TYPE";
	public final static String SUMMARY_SAND_VOL = "SAND VOLUME";
	public final static String SUMMARY_SUB_STAGE = "SUBSTAGE";
	private static boolean arrayContainsValue(String[] array, String value) {
		for (String s : array) {
			if (s.equals(value)) {
				return true;
			}
		}
		return false;
	}
	public static ArrayList<String> getArrayObject(String... strings) {
		ArrayList<String> array = new ArrayList<>();
		for (String s : strings) {
			array.add(s);
		}
		return array;
	}

	public static String[] getChemDataRangeEndNames() {
		return new String[] {CHEMICAL_NAME_END,CHEMICAL_VOLUME_END};
	}

	public static String[] getChemDataRangeStartNames() {
		return new String[] {CHEMICAL_NAME_START,CHEMICAL_VOLUME_START};
	}

	public static String getChemSandKey(String rangeName) {
		if(rangeName.toLowerCase().contains(SAND)) {
			return SAND;
		}
		return CHEMICALS;

	}
	public static String[] getDataNames() {
		String[] sigVals = new String[] { "", PAD_NAME, WELL_NAME, STAGE_NUMBER, START_DATE, START_TIME, END_DATE,
				END_TIME, START_TIME_DATE, END_TIME_DATE, ISIP, ISIP_TIME, OPEN_PRESSURE, CLOSE_PRESSURE,
				AVERAGE_PRESSURE, MAX_PRESSURE, AVERAGE_RATE, MAX_RATE, BACKSIDE_PRESSURE, BREAK_PRESSURE, BREAK_TIME,
				BREAK_VOLUME, BREAK_RATE, PUMP_TIME, SAND_VOLUME_START, SAND_VOLUME_END, CHEMICAL_VOLUME_START,
				CHEMICAL_VOLUME_END, SAND_NAME_START, SAND_NAME_END, CHEMICAL_NAME_START, CHEMICAL_NAME_END, PUMPDOWN,
				PUMPDOWN_ACID, AVERAGE_HORSEPOWER, FRAC_GRADIENT, MAX_PROP_CON, ACID_RATE, CLEAN_TOTAL, SLURRY_TOTAL,
				PRESSURE_AT_MAX_RATE, ACID_VOLUME, SAND_START_RATE, VOLUME_DESIGN_RATE, PERFS, DOWNTIME, TVD,
				USER_DEFINED, COMMENTS, COUNTY, PUMPS_START, PUMPS_END, PUMPS_BLENDING, DIESEL,BLENDER_ASSET_ID };
		return sigVals;
	}
	public static String[] getDataNames(ArrayList<String> additionalValues) {

		String[] sigVals = new String[] { PAD_NAME, WELL_NAME, STAGE_NUMBER, START_DATE, START_TIME, END_DATE, END_TIME,
				START_TIME_DATE, END_TIME_DATE, ISIP, ISIP_TIME, OPEN_PRESSURE, CLOSE_PRESSURE, AVERAGE_PRESSURE,
				MAX_PRESSURE, AVERAGE_RATE, MAX_RATE, BACKSIDE_PRESSURE, BREAK_PRESSURE, BREAK_TIME, BREAK_VOLUME,
				BREAK_RATE, PUMP_TIME, SAND_VOLUME_START, SAND_VOLUME_END, CHEMICAL_VOLUME_START, CHEMICAL_VOLUME_END,
				SAND_NAME_START, SAND_NAME_END, CHEMICAL_NAME_START, CHEMICAL_NAME_END, PUMPDOWN, PUMPDOWN_ACID,
				AVERAGE_HORSEPOWER, FRAC_GRADIENT, MAX_PROP_CON, ACID_RATE, CLEAN_TOTAL, SLURRY_TOTAL,
				PRESSURE_AT_MAX_RATE, ACID_VOLUME, SAND_START_RATE, VOLUME_DESIGN_RATE, PERFS, DOWNTIME, TVD,
				USER_DEFINED, COMMENTS, COUNTY, PUMPS_START, PUMPS_END, PUMPS_BLENDING, DIESEL,BLENDER_ASSET_ID };
		String[] addVals = new String[sigVals.length + additionalValues.size()];
		int count = 0;
		for (String s : sigVals) {
			addVals[count] = s;
			count++;
		}
		for (String s : additionalValues) {
			addVals[count] = s;
			count++;
		}
		return addVals;
	}
	public static String[] getDataNames(boolean emptyOption) {
		if (emptyOption) {
			return getDataNames();
		}
		String[] sigVals = new String[] { PAD_NAME, WELL_NAME, STAGE_NUMBER, START_DATE, START_TIME, END_DATE, END_TIME,
				START_TIME_DATE, END_TIME_DATE, ISIP, ISIP_TIME, OPEN_PRESSURE, CLOSE_PRESSURE, AVERAGE_PRESSURE,
				MAX_PRESSURE, AVERAGE_RATE, MAX_RATE, BACKSIDE_PRESSURE, BREAK_PRESSURE, BREAK_TIME, BREAK_VOLUME,
				BREAK_RATE, PUMP_TIME, SAND_VOLUME_START, SAND_VOLUME_END, CHEMICAL_VOLUME_START, CHEMICAL_VOLUME_END,
				SAND_NAME_START, SAND_NAME_END, CHEMICAL_NAME_START, CHEMICAL_NAME_END, PUMPDOWN, PUMPDOWN_ACID,
				AVERAGE_HORSEPOWER, FRAC_GRADIENT, MAX_PROP_CON, ACID_RATE, CLEAN_TOTAL, SLURRY_TOTAL,
				PRESSURE_AT_MAX_RATE, ACID_VOLUME, SAND_START_RATE, VOLUME_DESIGN_RATE, PERFS, DOWNTIME, TVD,
				USER_DEFINED, COMMENTS, COUNTY, PUMPS_START, PUMPS_END, PUMPS_BLENDING, DIESEL,BLENDER_ASSET_ID };
		return sigVals;
	}
	/*
	 *
	 *
	 *
	 * SLURRY_GRAND + TOTAL_SUFFIX, CLEAN_GRAND + TOTAL_SUFFIX, PUMP_TIME +
	 * TOTAL_SUFFIX, SAND_VOLUME_START + TOTAL_SUFFIX, SAND_VOLUME_END +
	 * TOTAL_SUFFIX, CHEMICAL_VOLUME_START + TOTAL_SUFFIX, CHEMICAL_VOLUME_END +
	 * TOTAL_SUFFIX
	 *
	 */
	public static String[] getDataNames(String[] sigVals, ArrayList<String> additionalValues) {
		String[] addVals = new String[sigVals.length + additionalValues.size()];
		int count = 0;
		for (String s : sigVals) {
			addVals[count] = s;
			count++;
		}
		for (String s : additionalValues) {
			addVals[count] = s;
		}
		return addVals;
	}
	public static String[] getDataNamesAppend(String suffix, String... exception) {
		String[] sigVals = getDataNames();

		return sigVals;
	}
	private static String[] getDataNamesAppend(String[] dataNames, String suffix, String... exceptions) {
		String[] newSigVals = new String[dataNames.length];
		int i = 0;
		for (String s : dataNames) {
			if (arrayContainsValue(exceptions, s)) {
				newSigVals[i] = s;
				i++;
				continue;
			}

			newSigVals[i] = s + suffix;
			i++;

		}
		return newSigVals;
	}
	public static ArrayList<String> getDataNamesForSummary() {
		String[] sigVals = { "First Start Time Cell", "First End Time Cell", "First Stage Number Cell",
				"First Start Date Cell", "First End Date Cell", "First Prop Con Cell", "First Clean Total Cell",
				"First Average Pressure Cell", "First Average Rate Cell", "First Slurry Total Cell",
				"First Sand Type Cell", "First Sand Volume Cell", "First Substage Name Cell" };
		ArrayList<String> summaryDataNames = new ArrayList<>();
		for (String s : sigVals) {
			summaryDataNames.add(s);
		}
		return summaryDataNames;
	}
	public static String[] getDataNamesForTable() {
		String[] sigVals = new String[] { "Pad Name", "Well Name", "Stage Number", "Start Date", "Start Time",
				"End Date", "End Time", "ISIP", "ISIP Time", "Open Pressure", "Close Pressure", "Average Pressure",
				"Max Pressure", "Average Rate", "Max Rate", "Backside Pressure", "Breakdown Pressure", "Breakdown Time",
				"Breakdown Volume", "Breakdown Rate", "Pump Time", "Average Horsepower", "Frac Gradient",
				"Max Prop. Concentration", "Acid Rate", "Clean Total", "Slurry Total", "Pres. At Max Rate",
				"Acid Volume", "Sand Start Rate", "Vol. To Design Rate", "Perfs", "Downtime", "TVD", "Pumpdown",
				"Pumpdown Acid", "Comments", "County", "Pumps Start", "Pumps End", "Pumps Blending", DIESEL,BLENDER_ASSET_ID };
		return sigVals;
	}
	public static String[] getDataNamesForWellSummary(String... addTotalToName) {

		String[] sigVals = getDataNames();

		String[] newSigVals = getDataNames(getDataNamesAppend(sigVals, AVERAGE_SUFFIX, CREW, WELL_NAME, START_TIME,
				STAGE_NUMBER, END_TIME, START_DATE, END_DATE, PAD_NAME, BREAK_TIME), getArrayObject(addTotalToName));
		return newSigVals;
	}
	public static String getOppNameVol(String rangeName) {
		String sandChem = getChemSandKey(rangeName);
		String volName = getVolumeName(rangeName);
		switch(sandChem) {
		case(SAND):
			return volName.equals(VOLUME)?SAND_NAME_START:SAND_VOLUME_START;
		case(CHEMICALS):
			return volName.equals(VOLUME)?CHEMICAL_NAME_START:CHEMICAL_VOLUME_START;
		}
		return "";
	}
	public static String[] getSandDataRangeEndNames() {
		return new String[] {SAND_NAME_END,SAND_VOLUME_END};
	}
	public static String[] getSandDataRangeStartNames() {
		return new String[] {SAND_NAME_START,SAND_VOLUME_START};
	}
	public static ArrayList<String> getSummaryColumnNames() {
		String[] namesArray = { SUMMARY_START_TIME, SUMMARY_END_TIME, SUMMARY_STAGE_NUMBER, SUMMARY_START_DATE,
				SUMMARY_END_DATE, SUMMARY_PROP_CON, SUMMARY_CLEAN_BBLS, SUMMARY_AVG_PRESSURE, SUMMARY_AVG_RATE,
				SUMMARY_SLURRY_BBLS, SUMMARY_SAND_TYPE, SUMMARY_SAND_VOL, SUMMARY_SUB_STAGE };

		ArrayList<String> summaryColumnNames = new ArrayList<>();
		for (String s : namesArray) {
			summaryColumnNames.add(s);
		}
		return summaryColumnNames;
	}

	private static String getVolumeName(String rangeName) {
		return rangeName.toLowerCase().contains(VOLUME)?VOLUME:NAME;
	}

	public static String[] putValueAtIndexInArray(String[] array, String addValue, int index) {
		String[] newArray = new String[array.length + 1];
		int i = 0;
		for (String s : array) {
			if (i == index) {
				newArray[i] = addValue;
				i++;
			}
			newArray[i] = s;
			i++;
		}
		return newArray;
	}

	public static ArrayList<String> readUserDefinedNames(String operator) throws IOException {
		ArrayList<String> userDefined = new ArrayList<>();
		String path = TransferTemplate.PARENT_FOLDER+"\\" + operator + "\\User_Defined.txt";
		File file = new File(path);
		if (!file.exists()) {
			return userDefined;
		}
		BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
		String temp;
		while ((temp = bufferedReader.readLine()) != null) {
			userDefined.add(temp);
		}
		bufferedReader.close();
		return userDefined;
	}

}
