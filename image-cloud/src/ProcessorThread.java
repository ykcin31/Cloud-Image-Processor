import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;


public class ProcessorThread implements Runnable 
{
	// Delimiter in instructions.txt
	final String DELIMITER = " <~> ";

	private String file;
	private String path;

	public ProcessorThread(String file, String path) throws IOException 
	{
		// Contains full file name of zip. file, including path
		this.file = file;
		// Save directory name
		this.path = path;
	}

	@Override
	public void run() 
	{
		// Extract parcel contents
		try 
		{
			Zipper z = new Zipper();
			String dir = z.extract(this.file, this.path);
			ArrayList<String> listing = listImages(dir);
			String txt = dir+"instructions.txt";
			for(int i = 0; i < listing.size(); i++)
			{
				int[] operations = getInstructions(listing.get(i),txt);
				for(int j = 0; j < operations.length; j++)
				{
					if(operations[j] == 1)
					{
						//perform operationj;s
					}
				}
			}

		} 
		catch (IOException e) {
			e.printStackTrace();
		}
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
			String[] s = line.split(DELIMITER);
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
