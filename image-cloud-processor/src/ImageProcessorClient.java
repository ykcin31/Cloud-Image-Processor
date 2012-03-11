import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import javax.imageio.*;
import org.apache.commons.io.FileUtils;

public class ImageProcessorClient 
{
	// SETUP fields
	//-----------------------------------------------------------------------------------------------------------------
	// Enter IP address of server
	private final String SERVER = "localhost";
	// Enter port number on server for connections to processor clients
	private final int PORT = 5555;
	// Enter directory on this station to store data
	private final String FILEPATH = "C:\\Users\\Nick\\Desktop\\image-cloud\\data\\client\\";
	// Enter setting for printing to console
	private final boolean PRINT = false;
	//-----------------------------------------------------------------------------------------------------------------
	private final int BUFFER = 2048;
	// Instructions.txt text delimiter
	private final String DELIMITER = " <~> ";

	private Socket serverSocket;

	public ImageProcessorClient()
	{

	}

	public void run() 
	{	
		try 
		{	
			// Create socket connection with server
			this.serverSocket = new Socket(this.SERVER, this.PORT);
			printToConsole("Client connected to: " + this.SERVER + ", " + PORT);
			// Listen for commands from server
			OutputStream cos = serverSocket.getOutputStream();
			PrintWriter out = new PrintWriter(cos, true);
			InputStream is = serverSocket.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader in = new BufferedReader(isr);
			String fromServer = null;
			String toServer = null;
			String packageName = null;

			while ((fromServer = in.readLine()) != null) 
			{
				printToConsole("Server: " + fromServer);
				// Server is ready to send package
				if (fromServer.equals("READY"))
				{
					toServer = "SEND NAME";
					out.println(toServer);
					printToConsole("Client: " + toServer);
					while ((fromServer = in.readLine()) == null)
					{
						;
					}
					packageName = fromServer;
					printToConsole("Server: " + fromServer);

					toServer = "SEND PACKAGE";
					out.println(toServer);
					printToConsole("Client: " + toServer);

					// Create directory, if not exist
					File dirName = new File(this.FILEPATH);
					if(!dirName.exists())
					{
						dirName.mkdirs();
					}

					// Receive data from server
					String zipPackage = receivePackageFromServer(packageName,is);

					toServer = "RECEIVED";
					out.println(toServer);
					printToConsole("Client: " + toServer);

					// Process
					String returnDir = processPackage(zipPackage);
					
					// Return processed data to server
					toServer = "RETURNING";
					out.println(toServer);
					printToConsole("Client: " + toServer);
					sendPackage(returnDir, cos);
				}	
				if (fromServer.equals("BYE"))
				{
					break;
				}
			}
			serverSocket.close();
			printToConsole("Client closed: " + this.SERVER + ", " + PORT);
		}

		catch(Exception e) 
		{
			e.printStackTrace();
		}
	}

	public void close() throws IOException
	{
		serverSocket.close();
	}
	
	// Process data in .zip package
	private String processPackage(String zipDir) throws Exception
	{
		// Extract contents of package
		String packageDir = extractPackage(zipDir);
		// Create listing of images in package
		ArrayList<String> listing = listImages(packageDir);
		// Create directory for processed images
		File comDir = new File(FILEPATH + "complete\\");
		FileUtils.deleteDirectory(comDir);
			comDir.mkdirs();
		String comName = comDir.toString() + "\\";
		// Name of instructions text file
		String txtName = packageDir + "instructions.txt";
		// Perform processing

		// Process images
		for(int i = 0; i < listing.size(); i++)
		{
			String imageName = packageDir + listing.get(i);
			printToConsole("Processing: " + imageName);
			// File name
			String[] splits = imageName.split("\\\\");
			String name = splits[splits.length-1];
			// Read image
			BufferedImage img = ImageIO.read(new File(imageName));
			Raster imgRaster = img.getData();
			int[] operations = getInstructions(imageName,txtName);
			//for(int j = 0; j < operations.length; j++)
			//{
				if(operations[0] == 1)
				{				
					HistogramEqualizationThreaded f = new HistogramEqualizationThreaded(imgRaster);
					imgRaster = f.process();
				}
			//}
			// Save result
			img.setData(imgRaster);
			File out = new File(comName + name);
			int p = name.lastIndexOf(".");
			String ext = name.substring(p+1);
			ImageIO.write(img, ext, out);
			printToConsole("Completed: " + imageName);
		}
		String returnDir = compressPackage(comName);
		FileUtils.deleteDirectory(new File(packageDir));
		return returnDir;
	}
	
	// Reads package data stream from server and saves to disk
	private String receivePackageFromServer(String name, InputStream is) throws Exception
	{
		//InputStream is = this.serverSocket.getInputStream();
		// Download zip file from server
		// Create I/O buffers
		String fileName = this.FILEPATH + name + ".zip";
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
		printToConsole("Package received");
		fos.close();
		return fileName;
	}	
	
	private void sendPackage(String zipDir, OutputStream cos) throws IOException
	{
		cos.flush();
		printToConsole("Sending package: " + zipDir);
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
		printToConsole("Package sent");
	}
	
	// Returns directory of extracted zip folder
	private String extractPackage(String zipDir) throws IOException
	{
		Zipper z = new Zipper();
		String packageDir = z.extract(zipDir, FILEPATH);
		return packageDir;
	}

	// Returns directory of compressed zip folder
	private String compressPackage(String comName) throws Exception
	{
		File comDir = new File(comName);
		ArrayList<String> files = new ArrayList<String>(Arrays.asList(comDir.list()));
		for(int i = 0; i < files.size(); i++)
		{
			files.set(i, comName + files.get(i));
		}
		Zipper z = new Zipper();
		String outDir = FILEPATH + "complete.zip";
		z.compress(files, outDir);
		return outDir;
	}
	
	// Returns a list of image file names on directory
	private ArrayList<String> listImages(String dir)
	{
		File d = new File(dir);
		String[] files = d.list();
		ArrayList<String> listing = new ArrayList<String>();
		for (int j = 0; j < files.length; j++)
		{
			// Image file address
			String address =  files[j].toString();
			// Check for image file extension compatibility (.jpg, .png, .gif)
			int mid = address.lastIndexOf(".");
			String ext = address.substring(mid+1);
			if (ext.equalsIgnoreCase("jpg")||ext.equalsIgnoreCase("jpeg")||ext.equalsIgnoreCase("jpe")||ext.equalsIgnoreCase("gif")||ext.equalsIgnoreCase("png"))
			{
				listing.add(address);
			}
		}
		return listing;
	}

	// Returns an int array indicating the operations to perform on corresponding "image" 
	private int[] getInstructions(String image, String txt) throws NumberFormatException, IOException
	{
		String[] splits = image.split("\\\\");
		String name = splits[splits.length-1];
		// Open the file c:\test.txt as a buffered reader
		BufferedReader bf = new BufferedReader(new FileReader(txt));
		String line;
		int[] operations = null;
		while ((line = bf.readLine()) != null)
		{
			String[] s = line.split(this.DELIMITER);
			if(s[0].equals(name))
			{
				operations = new int[s.length-1];
				for(int i = 0; i < operations.length; i++)
				{
					operations[i] = Integer.parseInt(s[i+1]);
				}
			}
		}
		bf.close();
		return operations;
	}
	// Print to System console
	private void printToConsole(String s) {
		if (this.PRINT == true) {
			System.out.println(s);
		}
	}

}