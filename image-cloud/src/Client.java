import java.io.*;
import java.net.*;

public class Client {

	public static void main(String args[]) {
		
		try {
			
			// Get server and port
			String server = args[0];
			int port = Integer.parseInt(args[1]);
			
			// Creates socket
			Socket s = new Socket(server, port);
			
			// Read zip file on server
			InputStream is = s.getInputStream();
			DataInputStream dis = new DataInputStream(is);
			
			// Carry out instructions
			// open files
			// if 1, do histogram method on filename
			// if 1, do edge method on filename
			// save modified images in zip folder
			
			// send files to server
			
			// Close socket
			s.close();
		}
		catch(Exception e) {
			System.out.println("Exception " + e);
		}
	}
}
