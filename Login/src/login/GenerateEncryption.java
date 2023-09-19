package login;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class GenerateEncryption implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -912110602465417410L;
	public transient Cipher cipher;
	public static String suite = "RSA/ECB/PKCS1Padding";
	public static String rootPath = findRootPath();
	private String algo = "RSA";
	private PublicKey publicKey;
	private PrivateKey privateKey;
	public static int port = 8080;
	public static String serverName = "serverName";///////////////////// SET AFTER CONFIGURING SERVER
	public final static String OBJECT_PATH = findObjectPath();
	public final static String OBJECT_FILENAME = "generate_encryption.scp";
	public final static int KEY_LENGTH = 2048;

	public GenerateEncryption(String cipherSuite) {
		suite = cipherSuite;
	}

	public GenerateEncryption(String cipherSuite, String path) {
		suite = cipherSuite;
		rootPath = path;
	}

	public GenerateEncryption(String cipherSuite, String path, String specServerName) {
		serverName = specServerName;
	}

	public GenerateEncryption() {

	}

	public static String findRootPath() {
		return EncryptCredentials.getRuntimeDir().replaceAll("[\\s\\n\\r]", "") + "\\credentials";
	}

	public void setCipher(Cipher cipher) {
		this.cipher = cipher;
	}

	public Cipher getCipher(int mode, Key key)
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
		return generateCipher(mode, key, suite);
	}

	public static Key readKeyFromFile(String fileName) throws IOException, ClassNotFoundException {
		File file = new File(fileName);
		if (!file.exists()) {
			return null;
		}
		ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
		Key key = (Key) objectInputStream.readObject();
		objectInputStream.close();
		return key;
	}

	public static String findObjectDir() {
		File file = new File(OBJECT_PATH);
		return file.getParent();
	}

	public static GenerateEncryption getInstance(String serverName) throws NoSuchAlgorithmException, IOException {
		return new GenerateEncryption(suite, rootPath, serverName).generateKeys();
	}

	public static GenerateEncryption getInstance() throws NoSuchAlgorithmException, IOException {
		return new GenerateEncryption(suite, rootPath, serverName).generateKeys();
	}

	private static String findObjectPath() {
		String root = findRootPath();
		return root.replaceAll("[\\s\\n\\r]", "") + "\\" + OBJECT_FILENAME;
	}

	public static String findMasterObjectPath() {
		String root = findRootPath();
		return root.replaceAll("[\\s\\n\\r]", "") + "\\master\\" + OBJECT_FILENAME;
	}

	public void setAlgorithm(String algo) {
		this.algo = algo;
	}

	public void setPath(String path) {
		rootPath = path;
	}

	public static File checkForCredentials() {
		File file = new File(OBJECT_PATH);
		if (!file.exists()) {
			return null;
		}
		return file;
	}

	public PublicKey getPublicKey() {
		return this.publicKey;
	}

	public PrivateKey getPrivateKey() {
		return this.privateKey;
	}

	public static GenerateEncryption readObjectFromFile() throws IOException, ClassNotFoundException,
			NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
		File file;
		if ((file = checkForCredentials()) == null) {
			GenerateEncryption generateEncryption = GenerateEncryption.getInstance();
			generateEncryption.setCipherMode(Cipher.DECRYPT_MODE, generateEncryption.getPrivateKey());
			return generateEncryption;
		}
		ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
		GenerateEncryption generateEncryption = (GenerateEncryption) objectInputStream.readObject();
		generateEncryption.setCipherMode(Cipher.DECRYPT_MODE, generateEncryption.getPrivateKey());
		return generateEncryption;
	}
	////CHANGE THIS TO CHECK FOR FILE
	@SuppressWarnings("finally")
	public static GenerateEncryption readObjectFromFile(String fileName) throws IOException, ClassNotFoundException,
			NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
		File file = getFile(fileName);
		if(!file.exists()) {
			GenerateEncryption generateEncryption = GenerateEncryption.getInstance();
			generateEncryption.setCipherMode(Cipher.DECRYPT_MODE, generateEncryption.getPrivateKey());
			return generateEncryption;
		}
		GenerateEncryption generateEncryption = null;
		try(ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file))){
			generateEncryption = (GenerateEncryption) objectInputStream.readObject();
			generateEncryption.setCipherMode(Cipher.DECRYPT_MODE, generateEncryption.getPrivateKey());
		}finally {
			return generateEncryption;
		}
		
	}

	private String getFilePath(String publicPrivate) {
		return rootPath + "\\" + publicPrivate;
	}

	// PASS ENCRYPTED PASSWORD FOR THE SERVER TO THIS OVERLOADED METHOD WITH
	// ALONG WITH THE GENERATEENCRYPTION OBJECT USED TO DO SO
	public static GenerateEncryption requestObjectFromServer(byte[] encryptedPassword,
			GenerateEncryption generateEncryption) throws NoSuchAlgorithmException, IOException {
		return GenerateEncryption.getInstance();
	}

	// PASS THE UNENCRYPTED PASSWORD AS A BYTE[] AND A GENERATEENCRYPTION OBJECT
	// WILL
	// BE CREATED USING DEFAULT VALUES TO ENCRYPT THE PASSWORD AND SENT IN THE
	// REQUEST TO THE SERVER
	public static GenerateEncryption requestObjectFromServer(byte[] passwordBytes)
			throws IllegalBlockSizeException, BadPaddingException, IOException, ClassNotFoundException,
			NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
		GenerateEncryption generateEncryption = GenerateEncryption.getInstance();
		byte[] encryptedBytes = EncryptCredentials.getEncryptedBytes(generateEncryption, passwordBytes);
		Socket socket = getSocketObject(port, serverName);
		ObjectOutputStream objectOutputStream = getObjectOutputStream(socket.getOutputStream());
		int size = getObjectSize(generateEncryption);
		objectOutputStream.write((byte) size);
		objectOutputStream.writeObject(generateEncryption);
		objectOutputStream.write((byte) encryptedBytes.length);
		objectOutputStream.write(encryptedBytes);
		return receiveResponse(socket.getInputStream());
	}

	public static GenerateEncryption receiveResponse(InputStream inputStream)
			throws IOException, ClassNotFoundException {
		if (!waitForDone(inputStream)) {
			return null;
		}
		ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
		GenerateEncryption generateEncryption = (GenerateEncryption) objectInputStream.readObject();
		return generateEncryption;
	}

	private static boolean waitForDone(InputStream inputStream) throws IOException {
		byte[] bytes = new byte[64];
		int i;
		String response = "";
		while ((i = inputStream.read(bytes)) > -1) {
			response += getStringFromBytes(bytes);
		}
		return Boolean.valueOf(response);
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

	public static String getStringFromBytes(Byte[] bytes) {
		StringBuilder stringBuilder = new StringBuilder();
		for (byte b : bytes) {
			if (b == 0) {
				return stringBuilder.toString();
			}
			stringBuilder.append((char) b);
		}
		return stringBuilder.toString();
	}

	public static int getObjectSize(Object object) throws IOException {
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
			objectOutputStream.writeObject(object);
			return byteArrayOutputStream.size();
		}
	}

	public static ObjectOutputStream getObjectOutputStream(OutputStream outputStream)
			throws FileNotFoundException, IOException {
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
		return objectOutputStream;
	}

	public static Socket getSocketObject(int port, String path) throws IOException {
		Socket socket = new Socket();
		socket.connect(InetSocketAddress.createUnresolved(path, port));
		return socket;
	}

	public static void writeEncryptedToFile(GenerateEncryption generateEncryption) throws IOException {
		System.out.println(OBJECT_PATH);
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(getFile(OBJECT_PATH)));
		objectOutputStream.writeObject(generateEncryption);
		objectOutputStream.flush();
		objectOutputStream.close();
	}

	public static void writeEncryptedToFile(GenerateEncryption generateEncryption, String filePath) throws IOException {
		System.out.println(filePath);
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(getFile(filePath)));
		objectOutputStream.writeObject(generateEncryption);
		objectOutputStream.flush();
		objectOutputStream.close();
	}

	public GenerateEncryption generateKeys() throws NoSuchAlgorithmException, IOException {
		KeyPair keyPair = generateKeyPair();
		setKeys(keyPair);
		return this;
	}

	// GETS THE FILE SPECIFIED BY THE PATH AND MAKES THE DIR PATH IF NECESSARY
	public static File getFile(String filePath) throws SecurityException {
		File file = new File(filePath);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		return file;
	}

	public void setCipherMode(int cipherMode, Key key)
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
		if (cipher == null) {
			setCipher(generateDefaultCipher(cipherMode, key));
		}
		setCipher(generateCipher(cipherMode, key, suite));
	}

	public void setKeys(KeyPair keyPair) {
		this.publicKey = keyPair.getPublic();
		this.privateKey = keyPair.getPrivate();
	}

	public void writeKeyToFile(Key key, String filePath) throws IOException {
		File file = getFile(filePath);
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
		objectOutputStream.writeObject(key);
		objectOutputStream.flush();
		objectOutputStream.close();
	}

	private final static String PRIVATE_FILENAME = "private_key";
	private final static String PUBLIC_FILENAME = "public_key";
	public final static String PUBLIC = findObjectDir() + "\\" + PUBLIC_FILENAME;
	public final static String PRIVATE = findObjectDir() + "\\" + PRIVATE_FILENAME;
	public final static String MASTER_PUBLIC = findObjectDir() + "master\\" + PUBLIC_FILENAME;
	public final static String MASTER_PRIVATE = findObjectDir() + "master\\" + PRIVATE_FILENAME;

	private void writeKeysToFile() throws IOException {
		File file = new File(findObjectDir());
		file = getFile(file.getParent());
		writeKeyToFile(getPublicKey(), PUBLIC);
		writeKeyToFile(getPrivateKey(), PRIVATE);

	}

	public KeyPair generateKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algo);
		keyPairGenerator.initialize(2048);
		return keyPairGenerator.generateKeyPair();
	}

	public static Cipher generateDefaultCipher(int mode, Key key)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		Cipher cipher = Cipher.getInstance(suite);
		cipher.init(mode, key);
		return cipher;
	}

	public static Cipher generateCipher(int mode, Key key, String suite)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		Cipher cipher = Cipher.getInstance(suite);
		cipher.init(mode, key);
		return cipher;
	}
}
