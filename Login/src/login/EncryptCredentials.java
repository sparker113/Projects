package login;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.swing.JOptionPane;

public class EncryptCredentials implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4741954831144605023L;
	public static final String MASTER_USERNAME_PATH = GenerateEncryption.rootPath + "\\master\\username.txt";
	public static final String MASTER_PASSWORD_PATH = GenerateEncryption.rootPath + "\\master\\password.txt";
	public static final String USERNAME_PATH = GenerateEncryption.rootPath + "\\username.txt";
	public static final String PASSWORD_PATH = GenerateEncryption.rootPath + "\\password.txt";
	public static final String CREDENTIALS_PATH = getRuntimeDir() + "\\credentials\\credentials.scp";
	public static final String CREDENTIALS_BASE_PATH = "credentials\\";
	public static final String CREDENTIALS_FILENAME = "credentials.scp";

	public EncryptCredentials() {

	}

	// SHOULD ONLY BE USED WITHIN THE CREDENTIAL HOSTING LOCATION
	public static void updateHostStoredPassword() throws IOException, ClassNotFoundException, InvalidKeyException,
			NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		String password = JOptionPane.showInputDialog("INPUT NEW SERVER PASSWORD");
		GenerateEncryption generateEncryption = GenerateEncryption
				.readObjectFromFile(GenerateEncryption.findMasterObjectPath());
		if (generateEncryption == null) {
			generateEncryption = createGenerateEncryption();
		}
		generateEncryption.setCipherMode(Cipher.ENCRYPT_MODE, generateEncryption.getPublicKey());
		byte[] encryptPassword = generateEncryption.getCipher(Cipher.ENCRYPT_MODE, generateEncryption.getPublicKey())
				.doFinal(password.getBytes());
		GenerateEncryption.writeEncryptedToFile(generateEncryption, GenerateEncryption.findMasterObjectPath());
		writeEncryptedToFile(encryptPassword, MASTER_PASSWORD_PATH);
	}

	public static void updateClientStoredPassword() throws IOException, ClassNotFoundException, InvalidKeyException,
			NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		String password = JOptionPane.showInputDialog("INPUT PASSWORD");
		GenerateEncryption generateEncryption = GenerateEncryption.readObjectFromFile();
		if (generateEncryption == null) {
			generateEncryption = createGenerateEncryption();
		}
		generateEncryption.setCipherMode(Cipher.ENCRYPT_MODE, generateEncryption.getPublicKey());
		byte[] encryptPassword = generateEncryption.getCipher(Cipher.ENCRYPT_MODE, generateEncryption.getPublicKey())
				.doFinal(password.getBytes());
		GenerateEncryption.writeEncryptedToFile(generateEncryption);
		writeEncryptedToFile(encryptPassword, PASSWORD_PATH);
	}

	private final static int WIDTH = 450;
	private final static int HEIGHT = 150;

	private static void updateUserCredentials() throws IOException, ClassNotFoundException, InvalidKeyException,
			NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		UserNamePassword userNamePassword = new UserNamePassword(UserNamePassword.getCenterX(WIDTH),
				UserNamePassword.getCenterY(HEIGHT), WIDTH, HEIGHT);
		HashMap<String, String> map = userNamePassword.getCredentials();
		if (map == null) {
			return;
		}
		String env = getEnvDetails();
		GenerateEncryption generateEncryption = GenerateEncryption.readObjectFromFile();
		if (generateEncryption == null) {
			generateEncryption = createGenerateEncryption();
		}
		generateEncryption.setCipherMode(Cipher.ENCRYPT_MODE, generateEncryption.getPublicKey());
		byte[] encryptPassword = generateEncryption.getCipher(Cipher.ENCRYPT_MODE, generateEncryption.getPublicKey())
				.doFinal(getCredentialString(env, map).getBytes());
		GenerateEncryption.writeEncryptedToFile(generateEncryption);
		writeEncryptedToFile(encryptPassword, CREDENTIALS_PATH);
	}

	public static Boolean updateUserCredentials(Function<HashMap<String, String>, Boolean> authFunc)
			throws IOException, ClassNotFoundException, InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		UserNamePassword userNamePassword = new UserNamePassword(UserNamePassword.getCenterX(WIDTH),
				UserNamePassword.getCenterY(HEIGHT), WIDTH, HEIGHT);
		HashMap<String, String> map = userNamePassword.getCredentials();
		String env = getEnvDetails();
		Boolean authorized = authFunc.apply(map);
		if (!authorized) {
			return authorized;
		}
		GenerateEncryption generateEncryption = GenerateEncryption.readObjectFromFile();
		if (generateEncryption == null) {
			generateEncryption = createGenerateEncryption();
		}
		generateEncryption.setCipherMode(Cipher.ENCRYPT_MODE, generateEncryption.getPublicKey());
		byte[] encryptPassword = generateEncryption.getCipher(Cipher.ENCRYPT_MODE, generateEncryption.getPublicKey())
				.doFinal(getCredentialString(env, map).getBytes());
		GenerateEncryption.writeEncryptedToFile(generateEncryption);
		writeEncryptedToFile(encryptPassword, CREDENTIALS_PATH);
		return authorized;
	}
	public static Boolean updateUserCredentials(Function<HashMap<String, String>, Boolean> authFunc,UserNamePassword userNamePassword)
			throws IOException, ClassNotFoundException, InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		HashMap<String, String> map = userNamePassword.getCredentials();
		String env = getEnvDetails();
		Boolean authorized = authFunc.apply(map);
		if (!authorized) {
			return authorized;
		}
		GenerateEncryption generateEncryption = GenerateEncryption.readObjectFromFile();
		if (generateEncryption == null) {
			generateEncryption = createGenerateEncryption();
		}
		generateEncryption.setCipherMode(Cipher.ENCRYPT_MODE, generateEncryption.getPublicKey());
		byte[] encryptPassword = generateEncryption.getCipher(Cipher.ENCRYPT_MODE, generateEncryption.getPublicKey())
				.doFinal(getCredentialString(env, map).getBytes());
		GenerateEncryption.writeEncryptedToFile(generateEncryption);
		writeEncryptedToFile(encryptPassword, CREDENTIALS_PATH);
		return authorized;
	}
	public static String getCredentialsPath(String relativePath) {
		return CREDENTIALS_BASE_PATH+relativePath+"\\"+CREDENTIALS_FILENAME;
	}
	public static String getEncryptionPath(String relativePath) {
		return CREDENTIALS_BASE_PATH+relativePath+GenerateEncryption.OBJECT_FILENAME;
	}
	public static Boolean updateUserCredentials(Function<HashMap<String, String>, Boolean> authFunc
			,UserNamePassword userNamePassword,String relativePath)
			throws IOException, ClassNotFoundException, InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		HashMap<String, String> map = userNamePassword.getCredentials();
		String env = getEnvDetails();
		Boolean authorized = authFunc.apply(map);
		if (!authorized) {
			return authorized;
		}
		GenerateEncryption generateEncryption = GenerateEncryption.readObjectFromFile(getEncryptionPath(relativePath));
		if (generateEncryption == null) {
			generateEncryption = createGenerateEncryption(relativePath);
		}
		generateEncryption.setCipherMode(Cipher.ENCRYPT_MODE, generateEncryption.getPublicKey());
		byte[] encryptPassword = generateEncryption.getCipher(Cipher.ENCRYPT_MODE, generateEncryption.getPublicKey())
				.doFinal(getCredentialString(env, map).getBytes());
		GenerateEncryption.writeEncryptedToFile(generateEncryption,getEncryptionPath(relativePath));
		writeEncryptedToFile(encryptPassword, getCredentialsPath(relativePath));
		return authorized;
	}
	public static Boolean updateUserCredentials(Function<HashMap<String, String>, Boolean> authFunc,String relativePath)
			throws IOException, ClassNotFoundException, InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		UserNamePassword userNamePassword = new UserNamePassword(UserNamePassword.getCenterX(WIDTH),
				UserNamePassword.getCenterY(HEIGHT), WIDTH, HEIGHT);
		HashMap<String, String> map = userNamePassword.getCredentials();
		String env = getEnvDetails();
		Boolean authorized = authFunc.apply(map);
		if (!authorized) {
			return authorized;
		}
		GenerateEncryption generateEncryption = GenerateEncryption.readObjectFromFile(getEncryptionPath(relativePath));
		if (generateEncryption == null) {
			generateEncryption = createGenerateEncryption(relativePath);
		}
		generateEncryption.setCipherMode(Cipher.ENCRYPT_MODE, generateEncryption.getPublicKey());
		byte[] encryptPassword = generateEncryption.getCipher(Cipher.ENCRYPT_MODE, generateEncryption.getPublicKey())
				.doFinal(getCredentialString(env, map).getBytes());
		GenerateEncryption.writeEncryptedToFile(generateEncryption,getEncryptionPath(relativePath));
		writeEncryptedToFile(encryptPassword, getCredentialsPath(relativePath));
		return authorized;
	}

	public static boolean isSameEnv(String savedEnvString) {
		String currentEnv = getEnvDetails();
		if (currentEnv.equals(savedEnvString)) {
			return true;
		}
		return false;
	}

	public static String getEnvDetailsFromString(String credentialsString) {
		return credentialsString.split(";")[0];
	}
	public static UserNamePassword getLoginWindow(String title) {
		UserNamePassword userNamePassword = UserNamePassword.getWindowWithTitle(title);
		return userNamePassword;
	}
	public static HashMap<String, String> getUserCredentials(Function<HashMap<String, String>, Boolean> authFunc,String relativePath,String loginName)
			throws IOException, ClassNotFoundException, InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ShortBufferException {
		Boolean authorized = false;
		if (!checkForFile(getCredentialsPath(relativePath))) {
			authorized = updateUserCredentials(authFunc,getLoginWindow(loginName),relativePath);
			if (!authorized) {
				return null;
			}
			HashMap<String, String> map = getUserCredentials(relativePath,loginName);
			return map;
		}
		GenerateEncryption generateEncryption = null;
		String credentialString = null;
		try {
			generateEncryption = GenerateEncryption.readObjectFromFile(getEncryptionPath(relativePath));
			credentialString = getDecryptedText(getCredentialsPath(relativePath), generateEncryption);
		} catch (Exception e) {
			e.printStackTrace();
			authorized = updateUserCredentials(authFunc,getLoginWindow(loginName),relativePath);
			return getUserCredentials(1,relativePath,loginName);
		}
		
		HashMap<String, String> map = getCredentialMap(credentialString);
		if (isSameEnv(map.get(ENVIRONMENT)) & map.get(PASSWORD) != null && !map.get(PASSWORD).isBlank()) {
			return map;
		}
		updateUserCredentials(authFunc,getLoginWindow(loginName),relativePath);
		credentialString = getDecryptedText(getCredentialsPath(relativePath), generateEncryption);
		map = getCredentialMap(credentialString);
		if (!authFunc.apply(map)) {
			return null;
		}
		return map;
	}
	public static HashMap<String, String> getUserCredentials(Function<HashMap<String, String>, Boolean> authFunc)
			throws IOException, ClassNotFoundException, InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ShortBufferException {
		Boolean authorized = false;
		if (!checkForFile()) {
			authorized = updateUserCredentials(authFunc);
			if (!authorized) {
				return null;
			}
			HashMap<String, String> map = getUserCredentials();
			return map;
		}
		GenerateEncryption generateEncryption = null;
		String credentialString = null;
		try {
			generateEncryption = GenerateEncryption.readObjectFromFile();
			credentialString =  getDecryptedText(CREDENTIALS_PATH, generateEncryption);
		} catch (Exception e) {
			e.printStackTrace();
			authorized = updateUserCredentials(authFunc);
			if (!authorized) {
				return null;
			}
			return getUserCredentials(1);
		}
		HashMap<String, String> map = getCredentialMap(credentialString);
		if (isSameEnv(map.get(ENVIRONMENT)) & map.get(PASSWORD) != null && !map.get(PASSWORD).isBlank()) {
			return map;
		}
		updateUserCredentials(authFunc);
		credentialString = getDecryptedText(CREDENTIALS_PATH, generateEncryption);
		map = getCredentialMap(credentialString);
		if (!authFunc.apply(map)) {
			return null;
		}
		return map;
	}
	public static HashMap<String, String> getUserCredentials(String relativePath,String loginName)
			throws IOException, ClassNotFoundException, InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ShortBufferException {
		if (!checkForFile(getCredentialsPath(relativePath))) {
			updateUserCredentials(relativePath,loginName);
			return getUserCredentials(relativePath,loginName);
		}
		GenerateEncryption generateEncryption = null;
		String credentialString = null;
		try {
			generateEncryption = GenerateEncryption.readObjectFromFile(getEncryptionPath(relativePath));
			credentialString = getDecryptedText(getCredentialsPath(relativePath),getEncryptionPath(relativePath),generateEncryption);
		} catch (Exception e) {
			e.printStackTrace();
			updateUserCredentials(relativePath,loginName);
			return getUserCredentials(1,relativePath,loginName);
		}
		HashMap<String, String> map = getCredentialMap(credentialString);
		if (isSameEnv(map.get("env"))) {
			return map;
		}
		updateUserCredentials(relativePath,loginName);
		credentialString = getDecryptedText(getCredentialsPath(relativePath),getEncryptionPath(relativePath), generateEncryption);
		return getCredentialMap(credentialString);
	}
	public static HashMap<String, String> getUserCredentials()
			throws IOException, ClassNotFoundException, InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ShortBufferException {
		if (!checkForFile()) {
			updateUserCredentials();
			return getUserCredentials();
		}
		GenerateEncryption generateEncryption = null;
		String credentialString = null;
		try {
			generateEncryption = GenerateEncryption.readObjectFromFile();
			credentialString = getDecryptedText(CREDENTIALS_PATH, generateEncryption);
		} catch (Exception e) {
			e.printStackTrace();
			updateUserCredentials();
			return getUserCredentials(1);
		}
		
		HashMap<String, String> map = getCredentialMap(credentialString);
		if (isSameEnv(map.get("env"))) {
			return map;
		}
		updateUserCredentials();
		credentialString = getDecryptedText(CREDENTIALS_PATH, generateEncryption);
		return getCredentialMap(credentialString);
	}
	public static HashMap<String, String> getUserCredentials(int count,String relativePath,String loginName)
			throws IOException, ClassNotFoundException, InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ShortBufferException {
		if (count > 2) {
			JOptionPane.showMessageDialog(null, "Access Denied");
			return null;
		}
		if (!checkForFile()) {
			updateUserCredentials(relativePath,loginName);
			return getUserCredentials(count++,relativePath,loginName);
		}
		GenerateEncryption generateEncryption = null;
		String credentialString = null;
		try {
			generateEncryption = GenerateEncryption.readObjectFromFile(getEncryptionPath(relativePath));
			credentialString = getDecryptedText(getCredentialsPath(relativePath),getEncryptionPath(relativePath), generateEncryption);
		} catch (Exception e) {
			e.printStackTrace();
			updateUserCredentials(relativePath,loginName);
			return getUserCredentials(count++,relativePath,loginName);
		}
		
		return getCredentialMap(credentialString);
	}
	public static HashMap<String, String> getUserCredentials(int count)
			throws IOException, ClassNotFoundException, InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ShortBufferException {
		if (count > 2) {
			JOptionPane.showMessageDialog(null, "Access Denied");
			return null;
		}
		if (!checkForFile()) {
			updateUserCredentials();
			return getUserCredentials(count++);
		}
		GenerateEncryption generateEncryption = null;
		String credentialString = null;
		try {
			generateEncryption = GenerateEncryption.readObjectFromFile();
			credentialString = getDecryptedText(CREDENTIALS_PATH, generateEncryption);
		} catch (Exception e) {
			e.printStackTrace();
			updateUserCredentials();
			return getUserCredentials(count++);
		}
		
		return getCredentialMap(credentialString);
	}

	public static boolean checkForFile(String filePath) {
		File file = new File(filePath);
		return file.exists();
	}

	public static boolean checkForFile() {
		File file = new File(CREDENTIALS_PATH);
		return file.exists();
	}

	public static void updateUserCredentials(String relativePath,String loginName)
			throws IOException, ClassNotFoundException, InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		UserNamePassword userNamePassword = getLoginWindow(loginName);
		HashMap<String, String> map = userNamePassword.getCredentials();
		String env = getEnvDetails();
		GenerateEncryption generateEncryption = GenerateEncryption.readObjectFromFile(getEncryptionPath(relativePath));
		if (generateEncryption == null) {
			generateEncryption = createGenerateEncryption(relativePath);
		}
		generateEncryption.setCipherMode(Cipher.ENCRYPT_MODE, generateEncryption.getPublicKey());
		byte[] encryptPassword = generateEncryption.getCipher(Cipher.ENCRYPT_MODE, generateEncryption.getPublicKey())
				.doFinal(getCredentialString(env, map).getBytes());
		GenerateEncryption.writeEncryptedToFile(generateEncryption,getEncryptionPath(relativePath));
		writeEncryptedToFile(encryptPassword, getCredentialsPath(relativePath));
	}

	public static HashMap<String, String> getCredentialMap(String credentialString) {
		HashMap<String, String> map = new HashMap<>();
		Matcher matcher = Pattern.compile("[^\\;]+").matcher(credentialString);
		String found;
		while (matcher.find() && (found = matcher.group()).split("\\:\\:\\:").length > 1) {
			map.put(found.split("\\:\\:\\:")[0], found.split("\\:\\:\\:")[1]);
		}
		return map;
	}

	public static String getEnvDetails() {
		Map<String, String> envMap = System.getenv();
		return envMap.get("USERDOMAIN") + "@" + envMap.get("LOGONSERVER") + envMap.get("COMPUTERNAME");
	}

	private static String getCredentialString(String machineID, HashMap<String, String> map) {
		String credentialString = "";
		for (String s : map.keySet()) {
			credentialString += ";" + s + ":::" + map.get(s);
		}
		return ENVIRONMENT + ":::" + machineID + credentialString;
	}

	// KEY FOR THE ENVIRONMENT VALUE IN CREDENTIALS MAP
	public final static String ENVIRONMENT = "env";
	// KEY FOR THE USERNAME VALUE IN CREDENTIALS MAP
	public final static String USERNAME = "username";
	// KEY FOR THE PASSWORD VALUE IN CREDENTIALS MAP
	public final static String PASSWORD = "password";

	// SHOULD ONLY BE USED WITHIN THE CREDENTIAL HOSTING LOCATION
	public static GenerateEncryption createGenerateEncryption()
			throws IOException, ClassNotFoundException, NoSuchAlgorithmException {
		GenerateEncryption generateEncryption = GenerateEncryption.getInstance();
		GenerateEncryption.writeEncryptedToFile(generateEncryption);
		return generateEncryption;
	}
	public static GenerateEncryption createGenerateEncryption(String relativePath)
			throws IOException, ClassNotFoundException, NoSuchAlgorithmException {
		GenerateEncryption generateEncryption = GenerateEncryption.getInstance();
		GenerateEncryption.writeEncryptedToFile(generateEncryption,getEncryptionPath(relativePath));
		return generateEncryption;
	}

	public static String getRuntimeDir() {
		try {
			Process process = Runtime.getRuntime().exec(new String[] { "cmd.exe", "/c", "cd" });
			InputStream inputStream = process.getInputStream();
			byte[] bytes = new byte[1024];
			inputStream.read(bytes);
			process.destroy();
			return GenerateEncryption.getStringFromBytes(bytes).replaceAll("[\\s\\r\\n]", "");
		} catch (IOException e) {
			return "";
		}
	}

	public static boolean challengeCreds(byte[] challenge, GenerateEncryption generateEncryption)
			throws InvalidKeyException, ClassNotFoundException, IllegalBlockSizeException, BadPaddingException,
			NoSuchAlgorithmException, NoSuchPaddingException, ShortBufferException, IOException {
		byte[] masterBytes = getFileByteBuffer(MASTER_PASSWORD_PATH).array();
		return getDecryptedText(challenge, generateEncryption).equals(getDecryptedText(masterBytes,
				GenerateEncryption.readObjectFromFile(GenerateEncryption.findMasterObjectPath())));
	}

	public static String getDecryptedText(String fileName)
			throws IOException, ClassNotFoundException, IllegalBlockSizeException, BadPaddingException,
			NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, ShortBufferException {
		ByteBuffer inputBuffer = getFileByteBuffer(fileName);
		GenerateEncryption generateEncryption = GenerateEncryption.readObjectFromFile();
		byte[] decryptedArray = decryptBuffer(inputBuffer, GenerateEncryption.readObjectFromFile()
				.getCipher(Cipher.DECRYPT_MODE, generateEncryption.getPrivateKey()));
		return GenerateEncryption.getStringFromBytes(decryptedArray);
	}

	public static String getDecryptedText(String fileName, GenerateEncryption generateEncryption)
			throws IOException, ClassNotFoundException, IllegalBlockSizeException, BadPaddingException,
			NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, ShortBufferException {
		ByteBuffer inputBuffer = getFileByteBuffer(fileName);
		byte[] decryptedArray = decryptBuffer(inputBuffer, generateEncryption
				.getCipher(Cipher.DECRYPT_MODE, generateEncryption.getPrivateKey()));
		return GenerateEncryption.getStringFromBytes(decryptedArray);
	}
	
	public static String getDecryptedText(String credentialsPath,String encryptionPath, GenerateEncryption generateEncryption)
			throws IOException, ClassNotFoundException, IllegalBlockSizeException, BadPaddingException,
			NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, ShortBufferException {
		ByteBuffer inputBuffer = getFileByteBuffer(credentialsPath);
		byte[] decryptedArray = decryptBuffer(inputBuffer, generateEncryption
				.getCipher(Cipher.DECRYPT_MODE, generateEncryption.getPrivateKey()));
		return GenerateEncryption.getStringFromBytes(decryptedArray);
	}

	public static String getDecryptedText(byte[] bytes, GenerateEncryption generateEncryption)
			throws IOException, ClassNotFoundException, IllegalBlockSizeException, BadPaddingException,
			NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, ShortBufferException {
		ByteBuffer inputBuffer = ByteBuffer.wrap(bytes);
		byte[] decryptedArray = decryptBuffer(inputBuffer, GenerateEncryption.readObjectFromFile()
				.getCipher(Cipher.DECRYPT_MODE, generateEncryption.getPrivateKey()));
		return GenerateEncryption.getStringFromBytes(decryptedArray);
	}

	public static String getStringFromArrayBytes(byte[][] array) {
		StringBuilder stringBuilder = new StringBuilder();
		for (byte[] bytes : array) {
			stringBuilder.append(GenerateEncryption.getStringFromBytes(bytes));
		}
		return stringBuilder.toString();
	}

	public static byte[][] getByteArrays(long length, int buffer) {
		int numBuffers = (int) ((length / buffer) + 1);
		byte[][] bytes = new byte[numBuffers][buffer];
		return bytes;
	}

	public static byte[][] getFileBytePackets(String fileName) throws IOException {
		File file = new File(fileName);
		FileInputStream fileInputStream = new FileInputStream(file);
		byte[][] bytesSqr = getByteArrays(file.length(), 1024);
		int count = 0;
		while (count < bytesSqr.length && (fileInputStream.read(bytesSqr[count])) > -1) {
			count++;
		}
		fileInputStream.close();
		return bytesSqr;
	}

	public static byte[] getFileBytes(InputStream inputStream, int length) throws IOException {
		byte[] bytes = new byte[length];
		inputStream.read(bytes);
		inputStream.close();
		return bytes;
	}

	public static ByteBuffer getFileByteBuffer(String fileName) throws IOException {
		File file = new File(fileName);
		FileInputStream fileInputStream = new FileInputStream(file);
		ByteBuffer byteBuffer = ByteBuffer.wrap(getFileBytes(fileInputStream, (int) file.length()));
		return byteBuffer;
	}

	public static byte[] decryptBuffer(ByteBuffer byteBuffer, Cipher cipher)
			throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
		ByteBuffer outputBuffer = ByteBuffer.allocate(byteBuffer.capacity());
		cipher.doFinal(byteBuffer, outputBuffer);
		return outputBuffer.array();
	}

	public static byte[][] decryptPackets(byte[][] packets, Cipher cipher)
			throws IllegalBlockSizeException, BadPaddingException {
		if (packets[0] == null) {
			return null;
		}
		int count = 0;
		byte[][] decryptedPackets = new byte[1][GenerateEncryption.KEY_LENGTH];
		for (byte[] bytes : packets) {
			byte[] bytes2 = new byte[2048];

			bytes2 = cipher.update(bytes);
			decryptedPackets[count] = bytes2;
			count++;
		}
		return decryptedPackets;
	}

	public static Byte[] getObjectWrappedBytes(byte[] bytes) {
		Byte[] bytesObj = new Byte[bytes.length];
		int i = 0;
		for (i = 0; i < bytes.length; i++) {
			bytesObj[i] = bytes[i];
		}
		return bytesObj;
	}

	public static String decryptBytes(byte[] bytes, GenerateEncryption generateEncryption)
			throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, ClassNotFoundException, IOException {

		return GenerateEncryption.getStringFromBytes(
				generateEncryption.getCipher(Cipher.DECRYPT_MODE, generateEncryption.getPrivateKey()).doFinal(bytes));
	}

	public static byte[] getEncryptedBytes(GenerateEncryption generateEncryption, String string)
			throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, ClassNotFoundException, IOException {
		Cipher cipher = generateEncryption.getCipher(Cipher.ENCRYPT_MODE, generateEncryption.getPublicKey());
		return cipher.doFinal(string.getBytes());
	}

	public static byte[] getEncryptedBytes(GenerateEncryption generateEncryption, byte[] bytes)
			throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, ClassNotFoundException, IOException {
		Cipher cipher = generateEncryption.getCipher(Cipher.ENCRYPT_MODE, generateEncryption.getPublicKey());
		return cipher.doFinal(bytes);
	}

	public static byte[] getEncryptedBytes(Cipher cipher, PublicKey publicKey, byte[] string)
			throws IllegalBlockSizeException, BadPaddingException {
		return cipher.doFinal(string);
	}

	public static void writeEncryptedToFile(byte[] encryptedBytes, String fileName) throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(GenerateEncryption.getFile(fileName));
		fileOutputStream.write(encryptedBytes);
		fileOutputStream.flush();
		fileOutputStream.close();
	}

	public static void saveEncryptedLogin(String username, String password)
			throws IOException, ClassNotFoundException, InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		GenerateEncryption generateEncryption = GenerateEncryption.readObjectFromFile();
		Cipher cipher = GenerateEncryption.generateDefaultCipher(Cipher.ENCRYPT_MODE,
				generateEncryption.getPublicKey());
		writeEncryptedToFile(cipher.doFinal(username.getBytes()), USERNAME_PATH);
		writeEncryptedToFile(cipher.doFinal(password.getBytes()), MASTER_PASSWORD_PATH);
	}

}
