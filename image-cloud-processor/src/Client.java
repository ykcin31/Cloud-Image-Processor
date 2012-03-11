import java.io.IOException;

public class Client {
	public static void main(String[] args) throws IOException {
		while (true) {
			try {
				ImageProcessorThreaded ipc = new ImageProcessorThreaded();
				ipc.go();
				ipc.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
