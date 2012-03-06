import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;

public class ImageProcessorClient 
{
	// Name of server
	private final String SERVER = "server";
	// Port number on server
	private final int PORT = 8080;
	// Directory on client station to store data
	private final String FILEPATH = "C:\\Users\\Nick\\Desktop\\image-cloud\\data\\clients\\";
	// Buffer size
	private final int BUFFER = 2048;
	// Instructions.txt text delimiter
	private final String DELIMITER = " <~> ";
	private Socket serverSocket;

	public void main(String args[]) 
	{	
		try 
		{		
			// Create socket connection with server
			this.serverSocket = connectToServer();
			// Retrieve package from server
			String packageZipDir = receivePackageFromServer();
			// Extract contents of package zip file
			String packageDir = extractPackage(packageZipDir);
			// Create listing of images from package
			ArrayList<String> listing = listImages(packageDir);
			// Carry out instructions
			String txt = packageDir + "instructions.txt";
			// Create "completed" directory for storage of completed files 
			String completedDir = packageDir + "completed\\";
			// Process images
			for(int i = 0; i < listing.size(); i++)
			{
				int[] operations = getInstructions(listing.get(i),txt);
				for(int j = 0; j < operations.length; j++)
				{
					//convert image to byte data
					if(operations[j] == 1)
					{
						//perform operationj;s
						//(have algorithms return byte data)
					}
					//save processed image in completed folder.
				}
			}
					
			// Return a .txt file containing directory of completed images to server
			
	
			// Close socket
			serverSocket.close();
		}

		catch(Exception e) 
		{
			System.out.println("Exception " + e);
		}
	}

	private Socket connectToServer() throws Exception
	{
		Socket serverSocket = new Socket(this.SERVER, this.PORT);
		return serverSocket;
	}

	private String receivePackageFromServer() throws Exception
	{
		BufferedInputStream bis;
		InputStream is = this.serverSocket.getInputStream();
		
		// Get name of job (String data)
		String name = null;
		bis = new BufferedInputStream(is);
		InputStreamReader isr = new InputStreamReader(bis, "US-ASCII");
		StringBuffer instr = new StringBuffer();
		
		// Read the socket's InputStream and append to a StringBuffer
		int c;
		while ( (c = isr.read()) != 13) instr.append( (char) c);
		name = instr.toString();  
		
		// Download zip file from server
		// Create I/O buffers
		String dirName = this.FILEPATH + name + ".zip";
		byte[] data = new byte[BUFFER];
		// Input buffer
		DataInputStream dis = new DataInputStream(is);
		bis = new BufferedInputStream(dis, BUFFER);
		// Output buffer
		FileOutputStream fos = new FileOutputStream(dirName);
		// Write data
		int count;
		while((count = bis.read(data, 0, this.BUFFER))!=-1)
		{
			fos.write(data,0,count);
		}
		bis.close();
		fos.close();
		return dirName;
	}
	
	private String extractPackage(String zipDir) throws IOException
	{
		Zipper z = new Zipper();
		String packageDir = z.extract(zipDir, FILEPATH);
		return packageDir;
	}

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

	private int[] getInstructions(String image, String txt) throws NumberFormatException, IOException
	{
		String[] splits = image.split("\\\\");
		String name = splits[splits.length-1];
		// Open the file c:\test.txt as a buffered reader
		BufferedReader bf = new BufferedReader(new FileReader(txt));
		String line;
		int[] operations = null;
		int count = 0;
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
		return operations;
	}
	
}