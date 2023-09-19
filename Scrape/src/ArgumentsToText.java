import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ArgumentsToText implements Runnable {
	private ArrayList<String> args = new ArrayList<>();
	private String fileName;
	private String delimiter;
	private Thread t;

	ArgumentsToText() {
	}

	ArgumentsToText(ArrayList<String> args) {
		this.args = args;
	}

	ArgumentsToText(ArrayList<String> args, String fileName, String delimiter) {
		this.args = args;
		this.fileName = fileName;
		this.delimiter = delimiter;
		this.t = new Thread(this, "ArgumentsToText");
		t.start();
	}

	ArgumentsToText(String oneArg, String fileName, String delimiter) {
		this.args.add(oneArg);
		this.fileName = fileName;
		this.delimiter = delimiter;
		this.t = new Thread(this, "One_Argument_To_Text");
		t.start();
	}

	public void writeArgumentsThreaded() throws IOException {
		FileWriter fileWriter = new FileWriter(new File(fileName));
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		fileWriter.write("");
		for (String s : args) {
			bufferedWriter.append(s);
			bufferedWriter.append(delimiter);
		}
		bufferedWriter.close();
	}

	public static void writeSingleLineToText(String arg, String fileName) throws IOException {
		FileWriter fileWriter = new FileWriter(fileName);
		fileWriter.write(arg);
		fileWriter.flush();
		fileWriter.close();

	}

	public static String readStringFromFile(String fileName) throws IOException {
		File file = new File(fileName);
		if (!file.exists()) {
			return "";
		}
		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		String arg = bufferedReader.readLine();
		bufferedReader.close();
		return arg;
	}

	@Override
	public void run() {
		try {
			writeArgumentsThreaded();
		} catch (IOException e) {
			System.out.println("IOException writeArgumentsThreaded");
		}
	}

	public static void writeArguments(ArrayList<String> arguments, String fileName, String delimiter)
			throws IOException {
		FileWriter fileWriter = new FileWriter(new File(fileName));
		fileWriter.write("");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		for (String s : arguments) {
			bufferedWriter.append(s);
			bufferedWriter.append(delimiter);
		}
		bufferedWriter.flush();
		bufferedWriter.close();
	}

	public static ArrayList<String> readArguments(String fileName) throws IOException {
		ArrayList<String> arguments = new ArrayList<>();
		FileReader fileReader = new FileReader(fileName);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String temp;
		while ((temp = bufferedReader.readLine()) != null) {
			arguments.add(temp);
		}
		bufferedReader.close();
		return arguments;
	}

	public void readWriteArguments(String fromFile, String toFile, String delimiter) throws IOException {
		ArrayList<String> arguments = readArguments(fromFile);
		writeArguments(arguments, toFile, delimiter);
	}
}
