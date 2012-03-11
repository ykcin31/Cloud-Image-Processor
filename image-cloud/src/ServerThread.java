import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread implements Runnable {
	// SETUP fields
	// -----------------------------------------------------------------------------------------------------------------
	// Enter buffer size for file write
	private final int BUFFER = 2048;
	// Enter retry counter
	private final int RETRYS = 5;
	// Enter setting for printing to console
	private final boolean PRINT = false;
	// -----------------------------------------------------------------------------------------------------------------
	// Directory of package
	private String packageName = null;
	// Directory to save package
	private String completeDir = null;
	private ServerSocket serverSocket;
	private Socket clientSocket;

	public ServerThread(String packageName, String completeDir,
			ServerSocket serverSocket) throws IOException {
		this.packageName = packageName;
		this.completeDir = completeDir;
		this.serverSocket = serverSocket;
		this.clientSocket = null;
	}

	public void run() {
		int retryCounter = 0;
		while (retryCounter < this.RETRYS) {
			retryCounter++;
			try {
				// Connect and send data to client
				this.clientSocket = this.serverSocket.accept();
				// Create input and output socket streams
				OutputStream cos = this.clientSocket.getOutputStream();
				InputStream is = this.clientSocket.getInputStream();
				// Send package to client
				sendPackageToClient(this.packageName, cos, is);
				// Wait for returned, processed package and save it (overwrite
				// original zip file)
				receivePackageFromClient(this.packageName, this.completeDir,
						cos, is);
				// Close socket
				close();
				break;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// Initiate conversation with client and send data file
	public void sendPackageToClient(String zipDir, OutputStream cos,
			InputStream is) throws Exception {
		// File output buffer streams (to client socket)
		PrintWriter out = new PrintWriter(cos, true);
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		String toClient = null;
		String fromClient = null;
		String packageName = null;

		// Initiate conversation with client
		toClient = "READY";
		out.println(toClient);
		printToConsole("Server: " + toClient);

		while ((fromClient = in.readLine()) != null) {
			printToConsole("Client: " + fromClient);
			if (fromClient.equals("SEND NAME")) {
				packageName = getPackageName(zipDir);
				toClient = packageName;
				out.println(toClient);
				printToConsole("Server: " + toClient);
			}
			if (fromClient.equals("SEND PACKAGE")) {
				// Send data to client
				sendPackageData(zipDir, cos);
			}
			if (fromClient.equals("RECEIVED")) {
				break;
			}
		}
	}

	// Return abbreviated package name
	private String getPackageName(String zipDir) throws IOException {
		String[] splitSlash = zipDir.split("\\\\");
		String end = splitSlash[(splitSlash.length) - 1];
		int dot = end.lastIndexOf(".");
		String packageName = end.substring(0, dot);
		return packageName;
	}

	// Writes package data to client
	private void sendPackageData(String zipDir, OutputStream cos)
			throws IOException {
		cos.flush();
		printToConsole("Sending package: " + zipDir);
		// File input buffer streams
		byte data[] = new byte[this.BUFFER];
		FileInputStream fis = new FileInputStream(zipDir);
		BufferedInputStream bis = new BufferedInputStream(fis);
		// Write data from server to client
		int count;
		while ((count = bis.read(data, 0, this.BUFFER)) >= 0) {
			cos.write(data, 0, count);
		}
		fis.close();
		cos.flush();
		printToConsole("Package " + zipDir + " sent");
	}

	// Received processed package returned from client
	private String receivePackageFromClient(String zipDir, String comDir,
			OutputStream cos, InputStream is) throws IOException, Exception {
		// File output buffer streams (to client socket)
		PrintWriter out = new PrintWriter(cos, true);
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		String toClient = null;
		String fromClient = null;
		String zipPackage = null;
		while ((fromClient = in.readLine()) != null) {
			if (fromClient.equals("RETURNING")) {
				zipPackage = receivePackageData(zipDir, comDir, is);
				toClient = "BYE";
				out.println(toClient);
				printToConsole("Server: " + toClient);
			}
			if (fromClient.equals("BYE")) {
				break;
			}
		}
		return zipPackage;
	}

	// Reads package data from server and saves to disk
	private String receivePackageData(String name, String comDir, InputStream is)
			throws Exception {
		// Create I/O buffers
		String[] split = name.split("\\\\");
		String n = split[split.length - 1];
		String fileName = comDir + n;
		byte[] data = new byte[BUFFER];
		// Input buffer
		DataInputStream dis = new DataInputStream(is);
		BufferedInputStream bis = new BufferedInputStream(dis, BUFFER);
		// Output buffer
		FileOutputStream fos = new FileOutputStream(fileName);
		// Write data
		int count;
		while ((count = bis.read(data, 0, BUFFER)) == BUFFER) {
			fos.write(data, 0, count);
		}
		fos.write(data, 0, count);
		printToConsole("Package received: " + fileName);
		fos.close();
		return fileName;
	}

	public void close() throws IOException {
		this.clientSocket.close();
	}

	// Print to System console
	private void printToConsole(String s) {
		if (this.PRINT == true) {
			System.out.println(s);
		}
	}
}
