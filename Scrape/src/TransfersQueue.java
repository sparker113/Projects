import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransfersQueue implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	transient ExecutorService executor;
	Queue<String> queue;
	int size;

	TransfersQueue(int size) {
		executor = Executors.newCachedThreadPool();
		this.queue = new LinkedList<>();
		this.size = size;
	}

	public void addToQueue(String wellStage) {
		executor.execute(() -> {
			if (queue.size() >= size) {
				System.out.println(queue.poll());
				queue.offer(wellStage);
				return;
			}
			queue.offer(wellStage);
			try {
				writeObjectToFile(this);
			} catch (IOException e) {
				System.out.println("Exception caught writing queue");
			}
		});
	}

	public Boolean checkQueue(String wellStage) {
		return queue.contains(wellStage);
	}

	public static TransfersQueue readFromFile() throws IOException, ClassNotFoundException {
		if (!checkFile("C:\\Scrape\\transfersQueue.scp")) {
			return new TransfersQueue(10);
		}
		ObjectInputStream objectInputStream = new ObjectInputStream(
				new FileInputStream(new File("C:\\Scrape\\transfersQueue.scp")));
		TransfersQueue transfersQueue = (TransfersQueue) objectInputStream.readObject();
		transfersQueue.executor = Executors.newCachedThreadPool();
		objectInputStream.close();
		return transfersQueue;
	}

	public static void writeObjectToFile(TransfersQueue transfersQueue) throws IOException {
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(
				new FileOutputStream(new File("C:\\Scrape\\transfersQueue.scp")));
		objectOutputStream.writeObject(transfersQueue);
		objectOutputStream.close();
	}

	private static Boolean checkFile(String path) {
		File file = new File(path);
		return file.exists();
	}
}
