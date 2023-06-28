package data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

public class SystemCommands {
	
	public static void disconnectFromInternet() throws IOException{
		Process process = Runtime.getRuntime().exec(new String[] {"cmd","/c","netsh wlan disconnect"});
		printStreamByLine(process.getInputStream());
		process.destroy();
	}
	private static void printStreamByLine(InputStream inputStream) throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String line;
		while((line=reader.readLine())!=null) {
			System.out.println(line);
		}
	}
	@SuppressWarnings("resource")
	public static String getWifiPassword(String wifiName) throws IOException{
		Process process = Runtime.getRuntime().exec(new String[] {"cmd","/c","netsh wlan show profile name=\""+wifiName+"\" key=clear"});
		Scanner scanner = new Scanner(process.getInputStream());
		while(scanner.hasNext()) {
			String line = scanner.next();
			if(line.contains("Key Content")) {
				return line.split(":")[1].trim();
			}
		}
		return "";
	}
	public static void connectToWifi(String wifiName) throws IOException,InterruptedException{
		Process process = Runtime.getRuntime().exec(new String[] {"cmd","/c","netsh wlan connect name=\""+wifiName+"\""});
		printStreamByLine(process.getInputStream());
		process.destroy();
		Thread.sleep(5000);
	}
	public static String getWifiName() throws IOException{
		Process process = Runtime.getRuntime().exec(new String[] {"cmd","/c","netsh wlan show interfaces"});
		InputStream inputStream = process.getInputStream();
		Scanner scanner = new Scanner(inputStream);
		scanner.useDelimiter("\n");
		String ssid = "";
		while(scanner.hasNext()) {
			String line = scanner.next();
			if(line.contains("SSID")) {
				ssid = line.split(":")[1].trim();
				
				break;
			}
		}
		process.destroy();
		scanner.close();
		return ssid;
	}
}
