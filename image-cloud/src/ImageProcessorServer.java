import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ImageProcessorServer 
{
	// Buffer size for file write
	private final int BUFFER = 2048;

	private int port;
	private ServerSocket serverSocket;
	private Socket clientSocket;

	public ImageProcessorServer(int port) throws IOException
	{
		this.port = port;
		this.serverSocket = new ServerSocket(port);
		this.clientSocket = null;
		System.out.println("ServerSocket bound to port: " + this.port);
	}

	// Find and connect to client
	// Returns the name of the client
	public Socket connectToClient() throws IOException
	{
		this.clientSocket = serverSocket.accept();
		System.out.println("Accepted ClientSocket on port: " + this.port);
		return this.clientSocket;
	}

	public String sendPackageToClient(String zipDir) throws Exception
	{
		// File output buffer streams (to client socket)
		OutputStream cos = this.clientSocket.getOutputStream();
		PrintWriter out = new PrintWriter(cos, true);
		InputStream is = clientSocket.getInputStream();
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		String toClient = null;
		String fromClient = null;
		String packageName = null;

		toClient = "READY";
		out.println(toClient);
		System.out.println("Server: " + toClient);

		while ((fromClient = in.readLine()) != null) 
		{   
			System.out.println("Client: " + fromClient);
			if (fromClient.equals("SEND NAME"))
			{
				packageName = getPackageName(zipDir);
				toClient = packageName;
				out.println(toClient);
				System.out.println("Server: " + toClient);
			}
			if (fromClient.equals("SEND PACKAGE"))
			{
				// Send data to client
				sendPackage(zipDir, cos);				
			}
			if (fromClient.equals("RECEIVED"))
			{
				/*toClient = "BYE";
		    	out.println(toClient);
		    	System.out.println("Server: " + toClient);
				 */		    }
			if (fromClient.equals("BYE"))
			{
				break;
			}
			if (fromClient.equals("RETURNING"))
			{
				String zipPackage = receivePackageFromClient(zipDir,is);
				toClient = "BYE";
		    	out.println(toClient);
		    	System.out.println("Server: " + toClient);		
			}
		}
		return zipDir;
	}

	public void close() throws IOException
	{
		this.serverSocket.close();
		System.out.println("Closed ServerSocket on port : " + this.port);
		this.clientSocket.close();
		System.out.println("Closed ClientSocket on port : " + this.port);
	}

	// Send package name
	private String getPackageName(String zipDir) throws IOException
	{
		String[] splitSlash = zipDir.split("\\\\");
		String end = splitSlash[(splitSlash.length)-1];
		int dot = end.lastIndexOf(".");
		String packageName = end.substring(0,dot);
		return packageName;
	}

	private void sendPackage(String zipDir, OutputStream cos) throws IOException
	{
		cos.flush();
		System.out.println("Sending package: " + zipDir);
		// File input buffer streams
		byte data[] = new byte[this.BUFFER];
		FileInputStream fis = new FileInputStream(zipDir);
		BufferedInputStream bis = new BufferedInputStream(fis);
		// Write data from server to client
		int count;
		while((count = bis.read(data,0,this.BUFFER)) >= 0)
		{
			cos.write(data,0,count);
		}
		fis.close();
		cos.flush();
		System.out.println("Package sent");
	}

	// Reads package data stream from server and saves to disk
	private String receivePackageFromClient(String name, InputStream is) throws Exception
	{
		//InputStream is = this.serverSocket.getInputStream();
		// Download zip file from server
		// Create I/O buffers
		String fileName = name;
		byte[] data = new byte[BUFFER];
		// Input buffer
		DataInputStream dis = new DataInputStream(is);
		BufferedInputStream bis = new BufferedInputStream(dis, BUFFER);
		// Output buffer
		FileOutputStream fos = new FileOutputStream(fileName);
		// Write data
		int count;
		while((count = bis.read(data, 0, BUFFER)) == BUFFER)
		{
			fos.write(data,0,count);
		}
		fos.write(data,0,count);
		System.out.println("Package received: " + fileName);
		fos.close();
		return fileName;
	}

}