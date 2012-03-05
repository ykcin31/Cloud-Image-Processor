import java.io.*;
import java.net.*;
import java.util.*;

public class serversocket {
	
	public static void main(String args[]) {
			try {
			
				int port = Integer.parseInt(args[0]);//Get port - input
				
				//Create server socket
				ServerSocket ss = new ServerSocket(port);
				
				while(true) {//Infinite Loop
					Socket s = ss.accept(); //accept incoming requests
					
					//Send packet to client
					OutputStream os = s.getOutputStream();
					DataOutputStream dos = new DataOutputStream(os);
					//created zip file written to client
					
					//receive and unpack zip file
					//Display new files on site
					
					//Close socket
					s.close();
					}
			}
			catch(Exception e) {
				System.out.println("Exception: " + e);
			}
	}
}
